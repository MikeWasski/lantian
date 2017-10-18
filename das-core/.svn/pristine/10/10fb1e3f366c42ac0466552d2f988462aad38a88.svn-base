package com.cas.das.core.service.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.jlrnt.common.Const;
import net.jlrnt.common.util.IoUtils;
import net.jlrnt.common.util.json.JsonUtils;
import net.jlrnt.dbc.DataManager;
import net.jlrnt.dbc.DataMap;

import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceProcessor;

/**
 * 臭氧雷达数据接入
 * @author mo_deng
 * <p>
 * 遍历文件夹所有文件，按照日期成对导入臭氧数据和消光数据，导入后，在导入记录文件中记录最新数据时间， 并定时导入最新数据文件，读取后插入数据库。
 * </p>
 */
public class OzoneLidarDataProcessor extends ServiceProcessor {
	
	/**
	 * 服务配置路径
	 */
	public static final String SERVICES_SETTING_PATH = "/cas-pe-setting/das/ozoneservices";

	/**
	 * 创建事件名
	 */
	public static final String CREATE_EVENT_NAME = "ENTRY_CREATE";

	/**
	 * 删除事件名
	 */
	public static final String DELETE_EVENT_NAME = "ENTRY_DELETE";

	@Override
	public void process(ServiceHandle handle, Object argument) {
		// 获取页面设置的文件目录、站点CODE和数据库信息
		File directory = new File((String) handle.getAttribute("directory"));
		DataManager dataManager = (DataManager) handle.getAttribute("dataManager");
		String siteId = ((com.cas.platform.service.def.param.TextParam) handle.getProcessorOuterParam("mtSiteId")).getValue();
		
		// 获取本地存放数据库中最新时间数据时间的文件路径
		String noteFilePath = Const.USER_DIR_PATH + SERVICES_SETTING_PATH;
		File dir = null;
		try {
			dir = IoUtils.getDirectory(noteFilePath);
			if (null == dir) {
				System.err.println("Error: can not create directory.");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String noteFileName = siteId + "OzoneFileName.txt";
		//判断记录文件是否存在：不存在时创建该文件
		if (!new File(noteFilePath + "/" + noteFileName).exists()) {
			File noteFile = new File(dir, noteFileName);
			//创建标记，最新数据时间
			int dateMax = -1;
			//遍历目标文件夹
			File[] listFiles = directory.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				File ozoneConcenFile = listFiles[i];
				String childFileName = ozoneConcenFile.getName();
				//找到以臭氧浓度结尾文件，并从文件名中提取文件数据生成时间
				if (childFileName.endsWith("臭氧浓度.txt")) {
					String date = childFileName.substring(0, 8);
					int dateNum = Integer.parseInt(date);
					if (dateMax < dateNum) {
						dateMax = dateNum;
					}
					//寻找与臭氧浓度文件同一时间生成的消光数据时间
					File ozoneExtinFile = new File((String) handle.getAttribute("directory") + "\\" + date + "消光.txt");
					//如找到该文件则将两个文件中数据一同入库
					if (ozoneExtinFile.isFile()) {
						parseRadarData(dataManager, ozoneConcenFile, ozoneExtinFile, siteId);
						System.out.println(ozoneConcenFile + "和" + ozoneExtinFile + "文件导入成功！");
					} else {
						continue;
					}
				}
			}
			if(dateMax != -1) {
				dateMax -= 1;
			}
			byte[] bytes = String.valueOf(dateMax).getBytes();
			int b = bytes.length;
			try {
				FileOutputStream fos = new FileOutputStream(noteFile);
				fos.write(bytes, 0, b);
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (new File(noteFilePath + "/" + noteFileName).isFile()) {
			File noteFile = new File(noteFilePath + "/" + noteFileName);
			String encoding = "GBK";
			StringBuilder resultString = new StringBuilder();
			try {
				InputStreamReader read = new InputStreamReader(new FileInputStream(noteFile), encoding);
				@SuppressWarnings("resource")
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineText = null;
				while ((lineText = bufferedReader.readLine()) != null) {
					resultString.append(lineText);
				}
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String maxdate = resultString.toString();
			File[] listFiles = directory.listFiles();
			String maxDateTemp = "";
			for (int i = 0; i < listFiles.length; i++) {
				File childFile = listFiles[i];
				String childFileName = childFile.getName();
				if (childFileName.endsWith("臭氧浓度.txt")) {
					String date = childFileName.substring(0, 8);
					int dateNum = Integer.parseInt(date);
					int dateNumMax = Integer.MIN_VALUE;
					if (null != maxdate && !"".equals(maxdate)) {
						dateNumMax = Integer.parseInt(maxdate);
					}
					if (dateNumMax < dateNum) {
						File ocFile = new File((String) handle.getAttribute("directory") + "\\" + date + "消光.txt");
						if (ocFile.isFile()) {
							parseRadarData(dataManager, childFile, ocFile, siteId);
							System.out.println(childFile + "和" + ocFile + "文件导入成功！");
							if("".equals(maxDateTemp)) {
								maxDateTemp = date;
							} else
							if (Integer.parseInt(date) > Integer.parseInt(maxDateTemp)) {
								maxDateTemp = date;
							}
						} else {
							continue;
						}
					}
				}
			}
			if (!"".equals(maxDateTemp)) {
				Integer dateTemp = Integer.parseInt(maxDateTemp);
				dateTemp -= 1;
				maxDateTemp = String.valueOf(dateTemp);
				byte[] bytes = maxDateTemp.getBytes();
				try {
					FileOutputStream fos = new FileOutputStream(noteFile);
					fos.write(bytes);
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 解析臭氧浓度雷达数据并入库
	 * 
	 * @param dataManager
	 * @param childFile
	 * @param ocFile
	 * @param siteId
	 */
	private void parseRadarData(DataManager dm, File ozoneConcenFile, File ozoneExtinFile, String siteId) {
		/* 文件行计数 */
		int i = 0;
		/* 单行列数计数 */
		int factorNum = 0;
		String factorHeight = "";
		String mtCode = "LIDAR_OZONE";
		String tableName = "MT_DATA_JSON_2017";
		
		//读取文件拼接时间和高度数据
		List<String> jsonTimeList = new ArrayList<String>();
		List<String> jsonTimeList2 = new ArrayList<String>();
		List<Map<String, String>> jsonHeightList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> jsonHeightList2 = new ArrayList<Map<String, String>>();
		String encoding = "GBK";
		
		if (ozoneConcenFile.isFile() && ozoneConcenFile.exists()) {
			try {
				InputStreamReader read = new InputStreamReader(new FileInputStream(ozoneConcenFile), encoding);
				@SuppressWarnings("resource")
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					i++;
					String[] content = lineTxt.split(",");
					/* 解析第一行时间数据 */
					if (i == 1) {
						factorNum = content.length;
						for (int j = 1; j < factorNum; j++) {
							jsonTimeList.add(j - 1, content[j]);
							Map<String, String> tempMap = new LinkedHashMap<String, String>();
							jsonHeightList.add(j - 1, tempMap);
						}
					}
					if (i > 2) {
						// 这步判断是否有必要
						if (content.length != factorNum) {
							continue;
						}
						factorHeight = content[0];
						for (int k = 1; k < factorNum; k++) {
							if (content[k].trim().equals("1.#QNAN0")) {
								content[k] = "0";
							}else if (null != content[k] && content[k].trim().length() > 0) {
								if(Float.parseFloat(content[k].trim()) > 200.0) {
									content[k] = "0";
								}
							}
							jsonHeightList.get(k - 1).put(factorHeight, content[k]);
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (ozoneExtinFile.isFile() && ozoneExtinFile.exists()) {
			try {
				InputStreamReader read = new InputStreamReader(new FileInputStream(ozoneExtinFile), encoding);
				@SuppressWarnings("resource")
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				i = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					i++;
					String[] content = lineTxt.split(",");
					/* 解析第一行时间数据 */
					if (i == 1) {
						factorNum = content.length;
						for (int j = 1; j < factorNum; j++) {
							jsonTimeList2.add(j - 1, content[j]);
							Map<String, String> tempMap = new LinkedHashMap<String, String>();
							jsonHeightList2.add(j - 1, tempMap);
						}
					}
					if (i > 2) {
						// 这步判断是否有必要
						if (content.length != factorNum) {
							continue;
						}
						factorHeight = content[0];
						for (int k = 1; k < factorNum; k++) {
							jsonHeightList2.get(k - 1).put(factorHeight, content[k]);
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		getSql(dm, tableName, jsonTimeList, mtCode, siteId, jsonHeightList, jsonTimeList2, jsonHeightList2);
	}

	private void getSql(DataManager dm, String tableName, List<String> jsonTimeList, String mtCode, String siteId,
			List<Map<String, String>> jsonHeightList, List<String> jsonTimeList2, List<Map<String, String>> jsonHeightList2) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql = "INSERT INTO " + tableName + " (siteId, mt, datatime, createTs, updateTs, year, month, day, hour, minute, second, f001, f002)";
		String time = "";
		Map<String, Integer> timeDetails = new HashMap<String, Integer>();
		String valSql = "";
		String sqlCombine = "";
		for (int i = 0; i < jsonTimeList.size(); i++) {
			time = jsonTimeList.get(i);
			Date date = null;
			Calendar now = Calendar.getInstance();
			try{
				if(!time.trim().equals("")) {
					date = sdf.parse(time);
				} else {
					return;
				}
				now.setTime(date);
				time = sdf.format(now.getTime());
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			timeDetails.put("ye", now.get(Calendar.YEAR));
			timeDetails.put("mo", (now.get(Calendar.MONTH) + 1));
			timeDetails.put("da", now.get(Calendar.DAY_OF_MONTH));
			timeDetails.put("ho", now.get(Calendar.HOUR_OF_DAY));
			timeDetails.put("mi", now.get(Calendar.MINUTE));
			timeDetails.put("se", now.get(Calendar.SECOND));
			valSql = "VALUES (" + siteId + ", '" + mtCode + "', '" + time + "', GETDATE(), GETDATE(), " + timeDetails.get("ye") + "," 
					+ timeDetails.get("mo") + "," + timeDetails.get("da") + "," + timeDetails.get("ho") + "," + timeDetails.get("mi") + "," +
					timeDetails.get("se") + ",'" + JsonUtils.serialize(jsonHeightList.get(i)) + "', '" + JsonUtils.serialize(jsonHeightList2.get(i))
					+ "')";
			sqlCombine = sql + valSql;
			if(isDataExist(dm, time, siteId, tableName, mtCode)){
				continue;
			}
			try {
				dm.executeUpdate(sqlCombine);
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private boolean isDataExist(DataManager dm, String time, String siteId, String tableName, String mtCode) {
		String sql = "select count(id) as count from " + tableName + " where siteId =" + siteId + " and mt='" + 
	mtCode + "' and convert(VARCHAR(19), datatime, 120) = '" + time + "'";
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
