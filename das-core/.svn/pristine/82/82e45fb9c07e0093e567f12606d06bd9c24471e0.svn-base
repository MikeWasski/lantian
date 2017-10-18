package com.cas.das.core.service.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.jlrnt.dbc.DataManager;
import net.jlrnt.dbc.DataMap;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceProcessor;
import com.cas.platform.service.def.param.TextParam;

/**
 * 高能雷达状态数据导入。
 * <p>
 * 遍历文件夹所有文件，按照日期导入数据，导入后，在导入记录文件 监测文件夹下十进制雷达数据，读取后插入数据库。
 * </p>
 * 
 * @author xiang_wang
 */
public class LidarStatusDataProcessor extends ServiceProcessor {

	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	@Override
	public void process(ServiceHandle handle, Object argument) {
		// 读取所需参数
		DataManager dataManager = (DataManager) handle.getAttribute("dataManager");
		String siteId = ((TextParam) handle.getProcessorOuterParam("siteId")).getValue();
		File directory = (File) handle.getAttribute("directory");

		// 遍历所有文件，读取记录信息并导入数据库，把读取后的记录文件按照时间分类存放
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {

			File file = files[i];
			// 文件夹跳过
			if (file.isDirectory()) {
				continue;
			}

			// 解析文件入库并移动文件
			parseRadarStatus(dataManager, file, siteId);
		}
	}

	/**
	 * 解析文件入库并移动文件
	 */
	public void parseRadarStatus(DataManager dm, File file, String siteId) {

		// 解析数据文件
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
		String lineTxt = null;
		Map<String, String> contentMap = new HashMap<String, String>();
		try {
			// 第一行数据解析
			lineTxt = bufferedReader.readLine();
			// 第二行数据解析
			lineTxt = bufferedReader.readLine();
			if (lineTxt != null) {
				String[] itemArr = lineTxt.split(" ");
				if (itemArr.length > 4) {
					contentMap.put("mtTime", itemArr[1] + " " + itemArr[2]);
					String statusItems = itemArr[4];
					if (statusItems.length() > 6) {
						contentMap.put("acquisitionCardStatus", String.valueOf(statusItems.charAt(0)));
						contentMap.put("laserState", String.valueOf(statusItems.charAt(1)));
						contentMap.put("collectionBoxStatus", String.valueOf(statusItems.charAt(2)));
						contentMap.put("upsStatus", String.valueOf(statusItems.charAt(3)));
						contentMap.put("waterLevelState", String.valueOf(statusItems.charAt(4)));
						contentMap.put("flashStatus", String.valueOf(statusItems.charAt(5)));
						contentMap.put("qSwitchStatus", String.valueOf(statusItems.charAt(6)));
					}
				}
			}
			// 第三行数据解析
			lineTxt = bufferedReader.readLine();
			if (lineTxt != null) {
				String[] itemArr = lineTxt.split(" ");
				if (itemArr.length > 5) {
					contentMap.put("hardDiskSurplus", parseItemData(itemArr[0]));
					contentMap.put("flashLife", parseItemData(itemArr[1]));
					contentMap.put("waterFlow", parseItemData(itemArr[2]));
					contentMap.put("cgTemperature", parseItemData(itemArr[3]));
					contentMap.put("shgTemperature", parseItemData(itemArr[4]));
					contentMap.put("csTemperature", parseItemData(itemArr[5]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 读取数据为空时，直接返回
		if (contentMap.isEmpty()) {
			moveFileToError(file);
			return;
		}

		// 更新数据库
		SimpleDateFormat sdfBefore = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		SimpleDateFormat sdfAfter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String mtTimePre = contentMap.get("mtTime");
		Date mtTimeDatePre = null;
		try {
			mtTimeDatePre = sdfBefore.parse(mtTimePre);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String mtTime = sdfAfter.format(mtTimeDatePre);

		// 判断数据是否存在
		int updateCount = 0;
		if (isDataExist(dm, mtTime, siteId)) {
			// 更新数据
			System.out.println("LidarStatus:Update data " + siteId + ":" + mtTime);
			updateCount = updateData(dm, siteId, contentMap, mtTime);
		} else {
			// 插入数据
			System.out.println("LidarStatus:Insert data " + siteId + ":" + mtTime);
			updateCount = insertData(dm, siteId, contentMap, mtTime);
		}

		if (updateCount > 0) {
			moveFile(file, mtTime);
		}
	}

	/**
	 * 移动文件到Error目录下
	 */
	private void moveFileToError(File srcFile) {

		String destFilePath = srcFile.getParent() + FILE_SEPARATOR + "error";
		destFilePath += FILE_SEPARATOR + srcFile.getName();
		File destFile = new File(destFilePath);
		createParentDir(destFile);
		try {
			FileUtils.moveFile(srcFile, destFile);
		} catch (IOException e) {
			e.printStackTrace();
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
	 * 根据数据类型，添加报警提示
	 */
	private String getAlarmPrompt(Map<String, String> contentMap) {
		StringBuilder sb = new StringBuilder();

		// 硬盘剩余
		String hardDiskSurplus = contentMap.get("hardDiskSurplus");
		if (!StringUtils.isEmpty(hardDiskSurplus) && !"-1".equals(hardDiskSurplus) && Float.valueOf(hardDiskSurplus) < 0.1) {
			sb.append("硬盘剩余容量过低，请及时清理！").append("<br/>");
		}
		// 闪光灯寿命
		String flashLife = contentMap.get("flashLife");
		if (!StringUtils.isEmpty(flashLife) && !"-1".equals(flashLife) && Float.valueOf(flashLife) < 0.2) {
			sb.append("闪光灯寿命过低，请及时更换！").append("<br/>");
		}
		// 采集卡状态
		String acquisitionCardStatus = contentMap.get("acquisitionCardStatus");
		if (acquisitionCardStatus != null && !"1".equals(acquisitionCardStatus)) {
			sb.append("采集卡异常，请检查原因！").append("<br/>");
		}
		// 激光器状态
		String laserState = contentMap.get("laserState");
		if (laserState != null && !"1".equals(laserState)) {
			sb.append("激光器异常，请检查原因！").append("<br/>");
		}
		// 采集箱状态
		// nothing to do
		// UPS状态
/*		String upsStatus = contentMap.get("upsStatus");
		if (upsStatus != null && !"1".equals(upsStatus)) {
			sb.append("UPS状态异常，可能未安装UPS！").append("<br/>");
		}*/
		// 水位状态
		String waterLevelState = contentMap.get("waterLevelState");
		if (waterLevelState != null && !"1".equals(waterLevelState)) {
			sb.append("水位状态异常，请检查原因！").append("<br/>");
		}
		// 闪光灯状态
		String flashStatus = contentMap.get("flashStatus");
		if (flashStatus != null && !"1".equals(flashStatus)) {
			sb.append("闪光灯状态异常，请检查原因！").append("<br/>");
		}
		// Q开关状态
		String qSwitchStatus = contentMap.get("qSwitchStatus");
		if (qSwitchStatus != null && !"1".equals(qSwitchStatus)) {
			sb.append("Q开关状态异常，请检查原因！").append("<br/>");
		}
		String result = "";
		if (sb.length() > 0) {
			result = sb.substring(0, sb.length() - 5);
		}
		return result;
	}

	/**
	 * 解析获取的字段，如果为NULL，则设置为-1
	 * 
	 * @param value
	 * @return
	 */
	private String parseItemData(String value) {
		String result = value;
		if ("".equals(value) || "NULL".equals(value.toUpperCase())) {
			result = "-1";
		}
		return result;
	}

	/**
	 * 更新数据
	 * 
	 * @param dm
	 * @param siteId
	 * @param contentMap
	 * @param mtTime
	 * @return
	 */
	private int updateData(DataManager dm, String siteId, Map<String, String> contentMap, String mtTime) {

		StringBuilder sqlSb = new StringBuilder();
		sqlSb.append("update LIDAR_DEVICE_STATUS set");
		sqlSb.append(" hardDiskSurplus = ").append(contentMap.get("hardDiskSurplus"));
		sqlSb.append(", flashLife = ").append(contentMap.get("flashLife"));
		sqlSb.append(", acquisitionCardStatus = ").append(contentMap.get("acquisitionCardStatus"));
		sqlSb.append(", laserState = ").append(contentMap.get("laserState"));
		sqlSb.append(", collectionBoxStatus = ").append(contentMap.get("collectionBoxStatus"));
		sqlSb.append(", upsStatus = ").append(contentMap.get("upsStatus"));
		sqlSb.append(", scannerStatus = ").append(contentMap.get("scannerStatus"));
		sqlSb.append(", waterLevelState = ").append(contentMap.get("waterLevelState"));
		sqlSb.append(", flashStatus = ").append(contentMap.get("flashStatus"));
		sqlSb.append(", qSwitchStatus = ").append(contentMap.get("qSwitchStatus"));
		sqlSb.append(", waterFlow = ").append(contentMap.get("waterFlow"));
		sqlSb.append(", cgTemperature = ").append(contentMap.get("cgTemperature"));
		sqlSb.append(", shgTemperature = ").append(contentMap.get("shgTemperature"));
		sqlSb.append(", csTemperature = ").append(contentMap.get("csTemperature"));
		sqlSb.append(", signalToNoiseRatio = ").append(contentMap.get("signalToNoiseRatio"));
		sqlSb.append(", alarmPrompt = '").append(getAlarmPrompt(contentMap));
		sqlSb.append("', updateTs = GETDATE()");

		sqlSb.append(" where siteId = ").append(siteId);
		sqlSb.append(" and convert(VARCHAR(19), datatime, 120) = '").append(mtTime).append("'");

		int updateCount = 0;

		try {
			updateCount = dm.executeUpdate(sqlSb.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return updateCount;
	}

	/**
	 * 插入数据
	 * 
	 * @param dm
	 * @param siteId
	 * @param contentMap
	 * @param mtTime
	 * @return
	 */
	private int insertData(DataManager dm, String siteId, Map<String, String> contentMap, String datatime) {
		StringBuilder sqlSb = new StringBuilder();
		sqlSb.append("insert into LIDAR_DEVICE_STATUS (siteId,datatime,hardDiskSurplus,flashLife,acquisitionCardStatus,laserState,")
				.append("collectionBoxStatus,upsStatus,scannerStatus,waterLevelState,flashStatus,qSwitchStatus,waterFlow,cgTemperature,")
				.append("shgTemperature,csTemperature,signalToNoiseRatio,alarmPrompt,createTs,updateTs) values (");

		sqlSb.append(siteId).append(",'");
		sqlSb.append(datatime).append("',");
		sqlSb.append(contentMap.get("hardDiskSurplus")).append(",");
		sqlSb.append(contentMap.get("flashLife")).append(",");
		sqlSb.append(contentMap.get("acquisitionCardStatus")).append(",");
		sqlSb.append(contentMap.get("laserState")).append(",");
		sqlSb.append(contentMap.get("collectionBoxStatus")).append(",");
		sqlSb.append(contentMap.get("upsStatus")).append(",");
		sqlSb.append(contentMap.get("scannerStatus")).append(",");
		sqlSb.append(contentMap.get("waterLevelState")).append(",");
		sqlSb.append(contentMap.get("flashStatus")).append(",");
		sqlSb.append(contentMap.get("qSwitchStatus")).append(",");
		sqlSb.append(contentMap.get("waterFlow")).append(",");
		sqlSb.append(contentMap.get("cgTemperature")).append(",");
		sqlSb.append(contentMap.get("shgTemperature")).append(",");
		sqlSb.append(contentMap.get("csTemperature")).append(",");
		sqlSb.append(contentMap.get("signalToNoiseRatio")).append(",");
		sqlSb.append("'").append(getAlarmPrompt(contentMap)).append("',");
		sqlSb.append("GETDATE(),GETDATE())");

		int insertCount = 0;

		try {
			insertCount = dm.executeUpdate(sqlSb.toString());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		return insertCount;
	}

	/**
	 * 判断数据是否插入
	 */
	private boolean isDataExist(DataManager dm, String datatime, String siteId) {
		String sql = "select count(id) as count from LIDAR_DEVICE_STATUS where siteId = " + siteId + " and convert(VARCHAR(19), datatime, 120) = '"
				+ datatime + "'";

		DataMap dataMap = null;
		try {
			dataMap = dm.getUniqueData(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Integer count = (Integer) dataMap.get("count");

		if (count > 0) {
			return true;
		}

		return false;
	}
}
