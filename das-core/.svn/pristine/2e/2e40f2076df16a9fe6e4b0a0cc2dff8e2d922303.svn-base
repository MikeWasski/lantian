package com.cas.das.core.service.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
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
import net.jlrnt.dbc.DataMap;

import com.cas.das.core.util.RecordFileUtils;
import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceProcessor;
import com.cas.platform.service.def.param.TextParam;

/**
 * 高能雷达数据导入。（江苏省气象局专用）
 * <p>
 * 遍历文件夹所有文件，按照日期导入数据，导入后，在导入记录文件 监测文件夹下十进制雷达数据，读取后插入数据库。
 * </p>
 * 
 * @author xiang_wang
 */
public class JsLidarDataProcessor extends ServiceProcessor {

	// 雷达监测因子前缀
	private static final String PREFIX_PART = "PART";

	// 数据记录位置前缀
	private static final String FILE_PREFIX = "JS";

	@Override
	public void process(ServiceHandle handle, Object argument) {
		// 读取所需参数
		DataManager dataManager = (DataManager) handle.getAttribute("dataManager");
		String siteId = ((TextParam) handle.getProcessorOuterParam("siteId")).getValue();
		Map<String, String> factorMap = getAllFactorColumnMap(dataManager);
		String siteRecordCode = ((TextParam) handle.getProcessorOuterParam("siteRecordCode")).getValue();
		File directory = (File) handle.getAttribute("directory");

		// 确定读取数据日期
		// 判断记录文件是否存在，存在则在记录文件的位置开始读取数据，不存在则读取所有数据
		File recordFile = RecordFileUtils.createRecordFile(FILE_PREFIX, siteRecordCode);

		// 判断记录时间是否存在
		String recordDate = RecordFileUtils.getRecordDate(recordFile);

		// 遍历所有文件夹，读取并记录文件。当遍历日期小于记录时间时，跳过
		System.out.println("Process lidar data:" + siteRecordCode);
		File[] yearFiles = directory.listFiles();
		for (int yearIndex = 0; yearIndex < yearFiles.length; yearIndex++) {

			// 年文件夹操作
			String recordDateYear = recordDate.substring(0, 4);
			String yearName = yearFiles[yearIndex].getName();
			if (recordDateYear.compareTo(yearName) <= 0) {

				// 月文件夹操作
				File[] monthFiles = yearFiles[yearIndex].listFiles();
				for (int monthIndex = 0; monthIndex < monthFiles.length; monthIndex++) {

					String recordDateMonth = recordDate.substring(0, 6);
					String monthName = monthFiles[monthIndex].getName();

					if (recordDateMonth.compareTo(monthName) <= 0) {

						// 天文件夹操作
						File[] dayFiles = monthFiles[monthIndex].listFiles();
						for (int dayIndex = 0; dayIndex < dayFiles.length; dayIndex++) {

							String dayName = dayFiles[dayIndex].getName();

							if (recordDate.compareTo(dayName) <= 0) {

								// 每天的数据操作
								File[] processFiles = dayFiles[dayIndex].listFiles();
								if (processFiles.length > 0) {
									// 如果记录时间比读取时间早，则更新记录时间
									if (recordDate.compareTo(dayName) < 0) {
										recordDate = dayName;
										RecordFileUtils.resetRecordDate(recordFile, recordDate);
									}

									for (int i = 0; i < processFiles.length; i++) {

										File processFile = processFiles[i];
										// 判断当天的文件是否接入
										if (!RecordFileUtils.isContentExists(recordFile, processFile.getName())) {

											// 文件未接入时，接入并记录
											try {
												parseRadarData(dataManager, processFile, siteId, factorMap);
											} catch (Exception e) {
												e.printStackTrace();
												continue;
											}
											RecordFileUtils.writeToFile(recordFile, processFile.getName());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 解析雷达数据并入库
	 * 
	 * @param dm
	 * @param childFile
	 * @param siteId
	 * @param factorMap
	 */
	private void parseRadarData(DataManager dm, File childFile, String siteId, Map<String, String> factorMap) throws Exception {

		String encoding = "GBK";
		if (childFile.exists()) { // 判断文件是否存在

			System.out.println("Start to parse lidar file:" + childFile.getName());

			InputStreamReader read = new InputStreamReader(new FileInputStream(childFile), encoding);// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			String mtTime = null;
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

			// BUG修改
			// 插入数据和上一条数据之间相差4:30分钟以内的数据跳过
			if (!isDataInsert(dm, siteId, mtTime)) {
				read.close();
				return;
			}

			// 拼接SQL
			String insertSql = parseSql(sqlItemMap, unHeightValueMap, factorMap, heightFactorCodeArr, heightValueList);
			dm.executeUpdate(insertSql);

			read.close();
		}
	}

	/**
	 * 判断是否插入数据库。<br/>
	 * 时间间隔太短的数据不进行数据插入。
	 */
	private boolean isDataInsert(DataManager dm, String siteId, String mtTime) {

		String sql = "select top 1 convert(VARCHAR(19), datatime, 120) as datatime from dbo.MT_DATA_JSON_2017 where siteId = " + siteId
				+ " and mt='LIDAR_PARTICULATE' order by datatime DESC";

		List<DataMap> dataList = null;
		try {
			dataList = dm.getDataList(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}

		if (dataList == null || dataList.size() == 0) {
			return true;
		}

		String lastTime = (String) dataList.get(0).get("datatime");

		// 时间相差4：30以内的数据不进行插入
		if (getDistanceTimes(mtTime, lastTime) < 270) {
			return false;
		}

		return true;
	}

	/**
	 * 计算两个日期的时间差（秒）
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static long getDistanceTimes(String str1, String str2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one = null;
		Date two = null;
		long sec = 0;
		try {
			one = df.parse(str1);
			two = df.parse(str2);
			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			sec = diff / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sec;
	}

	/**
	 * 拼接插入SQL
	 * 
	 * @param sqlItemMap
	 * @param unHeightValueMap
	 * @param factorMap
	 * @param heightFactorCodeArr
	 * @param heightValueList
	 * @return
	 */
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
