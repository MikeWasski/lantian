package com.cas.das.core.service.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.jlrnt.common.util.json.JsonUtils;
import net.jlrnt.dbc.DataManager;

import org.apache.commons.io.FileUtils;

import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceProcessor;
import com.cas.platform.service.def.param.TextParam;

/**
 * 高能雷达数据导入。
 * <p>
 * 监测文件夹下十进制雷达数据，读取后插入数据库，并按照日期进行分类存储。
 * </p>
 * 
 * @author xiang_wang
 */
public class LidarDataProcessor extends ServiceProcessor {

	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/**
	 * 数据记录位置前缀
	 */
	private static final String PREFIX_PART = "PART";

	@Override
	public void process(ServiceHandle handle, Object argument) {
		// 读取所需参数
		DataManager dataManager = (DataManager) handle.getAttribute("dataManager");
		String siteId = ((TextParam) handle.getProcessorOuterParam("siteId")).getValue();
		File directory = (File) handle.getAttribute("directory");
		Map<String, String> factorMap = getAllFactorColumnMap(dataManager);
		// 遍历所有文件，读取记录信息并导入数据库，把读取后的记录文件按照时间分类存放
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {

			File file = files[i];
			// 文件夹跳过
			if (file.isDirectory()) {
				continue;
			}
			// 解析文件入库并移动文件
			try {
				parseLidarData(dataManager, file, siteId, factorMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 解析文件入库并移动文件
	 */
	public void parseLidarData(DataManager dm, File childFile, String siteId, Map<String, String> factorMap) throws Exception {
		String encoding = "GBK";
		String mtTime = null;
		if (childFile.exists()) { // 判断文件是否存在

			System.out.println("Start to parse lidar file- siteId:" + siteId + ",fileName:" + childFile.getName());

			InputStreamReader read = new InputStreamReader(new FileInputStream(childFile), encoding);// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			int lineNum = 0;

			// 高度无关因子
			Map<String, String> unHeightValueMap = new HashMap<String, String>();
			// 高度相关因子
			int heightFactorNum = 0;
			String[] heightFactorCodeArr = null;
			List<LinkedHashMap<String, String>> heightValueList = new ArrayList<LinkedHashMap<String, String>>();
			Map<String, Object> sqlItemMap = new HashMap<String, Object>();
			sqlItemMap.put("siteId", siteId);
			sqlItemMap.put("mt", "'LIDAR_PARTICULATE'");

			while ((lineTxt = bufferedReader.readLine()) != null) {
				lineNum++;
				String[] content = lineTxt.split(";");

				// 处理日期
				if (lineNum == 1) {
					// 第二个项目为开始时间
					String monitorTime = content[1].split("=")[1];
					Date date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(monitorTime);
					Calendar now = Calendar.getInstance();
					now.setTime(date);
					sqlItemMap.put("year", now.get(Calendar.YEAR));
					sqlItemMap.put("month", (now.get(Calendar.MONTH) + 1));
					sqlItemMap.put("day", now.get(Calendar.DAY_OF_MONTH));
					sqlItemMap.put("hour", now.get(Calendar.HOUR_OF_DAY));
					sqlItemMap.put("minute", now.get(Calendar.MINUTE));
					sqlItemMap.put("second", now.get(Calendar.SECOND));

					mtTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now.getTime());
					sqlItemMap.put("datatime", "'" + mtTime + "'");
					// 处理高度无关数据
				} else if (lineNum == 2) {
					setUnHeightValue(unHeightValueMap, content);
					// 高度相关因子
				} else if (lineNum == 3) {
					heightFactorNum = content.length;
					heightFactorCodeArr = content;
					for (int j = 0; j < heightFactorNum; j++) {
						heightValueList.add(j, new LinkedHashMap<String, String>());
					}
				} else {
					double height = 0.0075 * (lineNum - 4);
					if (content.length != heightFactorNum) {
						continue;
					}
					for (int k = 0; k < heightFactorNum; k++) {
						String str = content[k];
						if (str.contains("?")) {
							str = "0";
						}
						// 退偏数值如果为负数时，取绝对值
						if (k == 11 && str.contains("-")) {
							double d = Double.parseDouble(str);
							str = String.valueOf(Math.abs(d));
						}
						heightValueList.get(k).put(height + "", str);
					}
				}
			}

			// 拼接SQL
			String insertSql = parseSql(sqlItemMap, unHeightValueMap, factorMap, heightFactorCodeArr, heightValueList);
			dm.executeUpdate(insertSql);
			read.close();
		}
		moveFile(childFile, mtTime);
	}

	private String parseSql(Map<String, Object> sqlItemMap, Map<String, String> unHeightValueMap, Map<String, String> factorMap,
			String[] heightFactorCodeArr, List<LinkedHashMap<String, String>> heightValueList) {

		StringBuilder insertSb = new StringBuilder();
		insertSb.append("insert into MT_DATA_JSON_2017 (");
		StringBuilder valSb = new StringBuilder();
		valSb.append(" VALUES (");

		// 拼接基本信息
		for (String key : sqlItemMap.keySet()) {
			insertSb.append(key).append(",");
			valSb.append(sqlItemMap.get(key)).append(",");
		}

		// 拼接高度无关信息
		for (String key : unHeightValueMap.keySet()) {
			String factorColumn = factorMap.get(key);
			if (factorColumn != null) {
				insertSb.append("f").append(factorColumn).append(",");
				valSb.append("'").append(unHeightValueMap.get(key)).append("',");
			}
		}

		// 拼接高度相关信息
		for (int i = 0; i < heightFactorCodeArr.length; i++) {
			String factorColumn = factorMap.get(PREFIX_PART + heightFactorCodeArr[i].toUpperCase());
			if (factorColumn != null) {
				insertSb.append("f").append(factorColumn).append(",");
				String jsonValue = JsonUtils.serialize(heightValueList.get(i));
				valSb.append("'").append(jsonValue).append("',");
			}
		}

		insertSb.append("createTs,updateTs)");
		valSb.append("GETDATE(),GETDATE())");

		return insertSb.toString() + valSb.toString();
	}

	/**
	 * 设置高度无关内容
	 * 
	 * @param unHeightValueMap
	 * @param content
	 */
	private void setUnHeightValue(Map<String, String> unHeightValueMap, String[] content) {

		for (String item : content) {
			String[] itemArr = item.split("=");
			unHeightValueMap.put(PREFIX_PART + itemArr[0].toUpperCase(), itemArr[1]);
		}
	}

	/**
	 * 移动文件到新的目录。
	 * <p>
	 * 移动到对应日期的文件夹下<br/>
	 * mtTime:yyyy-MM-dd HH:mm:ss
	 * </p>
	 */
	private void moveFile(File srcFile, String mtTime) {

		if (mtTime.length() < 19) {
			return;
		}
		String year = mtTime.substring(0, 4);
		String month = mtTime.substring(5, 7);
		String day = mtTime.substring(8, 10);

		String destFilePath = srcFile.getParent();
		destFilePath += FILE_SEPARATOR + year;
		destFilePath += FILE_SEPARATOR + year + month;
		destFilePath += FILE_SEPARATOR + year + month + day;
		destFilePath += FILE_SEPARATOR + srcFile.getName();

		File destFile = new File(destFilePath);
		createParentDir(destFile);
		try {
			FileUtils.moveFile(srcFile, destFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 创建父目录
	private void createParentDir(File file) {
		if (!file.getParentFile().exists()) {
			createParentDir(file.getParentFile());
			file.getParentFile().mkdir();
		}
	}

	/**
	 * 获取监测因子和列对应关系
	 * 
	 * @param dm
	 * @return
	 */
	private Map<String, String> getAllFactorColumnMap(DataManager dm) {

		Map<String, String> result = new HashMap<String, String>();

		result.put("partOriginal1".toUpperCase(), "001");
		result.put("partOriginal2".toUpperCase(), "002");
		result.put("partOriginal3".toUpperCase(), "003");
		result.put("partPrrch1".toUpperCase(), "004");
		result.put("partPrrch2".toUpperCase(), "005");
		result.put("partPrrch3".toUpperCase(), "006");
		result.put("partExtin355".toUpperCase(), "007");
		result.put("partExtin532".toUpperCase(), "008");
		result.put("partDepol".toUpperCase(), "009");
		result.put("partSnr1".toUpperCase(), "010");
		result.put("partSnr2".toUpperCase(), "011");
		result.put("partSnr3".toUpperCase(), "012");
		result.put("partGradient".toUpperCase(), "013");
		result.put("partAngstrom".toUpperCase(), "014");
		result.put("partPm10".toUpperCase(), "015");
		result.put("partPm25".toUpperCase(), "016");
		result.put("partAod".toUpperCase(), "017");
		result.put("partVisibility".toUpperCase(), "018");
		result.put("partPbl".toUpperCase(), "019");
		result.put("partCloudbase".toUpperCase(), "020");
		result.put("partCloudpeak".toUpperCase(), "021");
		result.put("partCloudtop".toUpperCase(), "022");
		result.put("partCloudmodel".toUpperCase(), "023");

		return result;
	}
}
