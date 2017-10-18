package com.cas.das.core.service.processor;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.cas.das.core.util.SpriderUtil;
import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceProcessor;
import com.cas.platform.service.def.param.TextParam;

/**
 * 抓取指定城市的常规数据
 * <p>
 * 从输入的时间和数据库最新的数据中 最新的时间开始抓取数据，每次抓取下一小时的数据，知道抓取的最新的数据位置。 正常运行后，定时调用该方法。
 * </p>
 * 
 * @author xiang_wang
 */
public class SpriderNormalDataByCityProcessor extends ServiceProcessor {

	// 常规监测类型CODE
	private static final String MT_NORMAL_CODE = "MT_NORMAL";

	@Override
	public void process(ServiceHandle handle, Object argument) {
		DataManager dm = (DataManager) handle.getAttribute("dataManager");
		String cityName = ((TextParam) handle.getProcessorOuterParam("cityName")).getValue();
		String startTime = ((TextParam) handle.getProcessorOuterParam("startTime")).getValue();
		String tableName = ((TextParam) handle.getProcessorOuterParam("tableName")).getValue();
		String siteId = ((TextParam) handle.getProcessorOuterParam("siteId")).getValue();

		String mtCode = MT_NORMAL_CODE;
		String typeCode = "HOUR";

		// 判断查询的开始时间
		String startTimeInput = getLastDataTime(dm, startTime, tableName, siteId, mtCode);

		// 当开始时间为null时不做插入操作
		if (startTimeInput == null) {
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		try {
			Date startTimeDate = sdf.parse(startTimeInput);
			Date endTimeDate = new Date();
			while (startTimeDate.getTime() < endTimeDate.getTime()) {
				long timeInterval = endTimeDate.getTime() - startTimeDate.getTime();
				// 默认时间间隔为一天
				long maxTimeInterval = (long) 24 * 3600 * 1000;
				long realTimeInterval = (long) 23 * 3600 * 1000;
				// 如果时间间隔超过一天就把结束时间设为开始时间-1天,如果时间间隔在一天内则插入完数据后返回
				if (timeInterval > maxTimeInterval) {
					Date endTimeDateTemp = new Date(startTimeDate.getTime() + maxTimeInterval);
					Date endTimeDateInput = new Date(startTimeDate.getTime() + realTimeInterval);
					insertSelectTimeRangeData(dm, cityName, typeCode, sdf.format(startTimeDate), sdf.format(endTimeDateInput), siteId, mtCode,
							tableName);
					startTimeDate = endTimeDateTemp;
				} else {
					insertSelectTimeRangeData(dm, cityName, typeCode, sdf.format(startTimeDate), sdf.format(endTimeDate), siteId, mtCode, tableName);
					return;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void insertSelectTimeRangeData(DataManager dm, String cityName, String typeCode, String startTimeInput, String endTimeInput,
			String siteId, String mtCode, String tableName) {
		// 网上抓取常规数据
		Map<String, Object> dataMapNormal = normalDataSpriderFunc("GETCITYWEATHER", cityName, startTimeInput, endTimeInput, typeCode);
		Map<String, Object> dataMapAQI = normalDataSpriderFunc("GETDETAIL", cityName, startTimeInput, endTimeInput, typeCode);
		Map<String, Object> resultNormal = (Map<String, Object>) dataMapNormal.get("result");
		Map<String, Object> resultAQI = (Map<String, Object>) dataMapAQI.get("result");
		Map<String, Object> dataNormal = (Map<String, Object>) resultNormal.get("data");
		Map<String, Object> dataAQI = (Map<String, Object>) resultAQI.get("data");

		// 提取常规数据
		List<Map<String, String>> datasNormal = (List<Map<String, String>>) dataNormal.get("rows");
		List<Map<String, String>> datasAQI = (List<Map<String, String>>) dataAQI.get("rows");

		// 将常规数据插入数据库
		insertData(dm, datasNormal, datasAQI, siteId, mtCode, tableName);
	}

	// 常规数据网络抓取方法
	@SuppressWarnings("unchecked")
	private Map<String, Object> normalDataSpriderFunc(String method,String cityName, String startTime, String endTime, String type) {
		Map<String, Object> serialize = new HashMap<>();
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		// 每种抓取需要确定参数，确定后，使用SpriderUtil.decodeAqiStudyParam
		// 先解码，看参数是什么样的，再使用SpriderUtil.encodeAqiStudyParam进行编码。
		LinkedHashMap<String, Object> object = new LinkedHashMap<String, Object>();
		object.put("city",cityName);
		object.put("endTime",endTime);
		object.put("startTime",startTime);
		object.put("type",type);
		String param = SpriderUtil.buildParam(method, object);

		params.add(new BasicNameValuePair("d", param));
		HttpPost httppost = new HttpPost("https://www.aqistudy.cn/apinew/aqistudyapi.php");
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httppost.setHeader("Accept", "*/*");
		httppost.setHeader("Accept-Encoding", "gzip, deflate, br");
		httppost.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httppost.setHeader("Connection", "keep-alive");
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		httppost.setHeader("Cookie",
				"UM_distinctid=15d0c2e02e51bd-0a17c179b31878-474f0820-1fa400-15d0c2e02e8a5e; CNZZDATA1254317176=458838566-1499143250-%7C1501125522");
		httppost.setHeader("Host", "www.aqistudy.cn");
		httppost.setHeader("Referer", "https://www.aqistudy.cn/html/city_map.html?v=1.6");
		httppost.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
		httppost.setHeader("X-Requested-With", "XMLHttpRequest");

		// 发送请求
		HttpResponse httpresponse;
		try {
			httpresponse = httpclient.execute(httppost);
			HttpEntity entity = httpresponse.getEntity();
			String str = EntityUtils.toString(entity);
			// 获取数据后，使用SpriderUtil.decodeAqiStudyResp进行解密。
			String obj = SpriderUtil.decodeAqiStudyResp(str);
			String string = decodeUnicode(obj);
			// 获取返回数据
			serialize = (Map<String, Object>) JsonUtils.unSerialize(string);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serialize;
	}

	// 常规数据插入数据库方法
	private void insertData(DataManager dm, List<Map<String, String>> datasNormal, List<Map<String, String>> datasAQI, String siteId, String mtCode,
			String tableName) {
		SimpleDateFormat sdf19 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Map<String, String>> valuesMap = new LinkedHashMap<String, Map<String, String>>();

		for (Map<String, String> data : datasAQI) {
			String time = data.get("time");
			if (valuesMap.get(time) == null) {
				valuesMap.put(time, new HashMap<String, String>());
			}
			Map<String, String> values = valuesMap.get(time);

			values.put("normalO3", data.get("o3"));
			values.put("normalNO2", data.get("no2"));
			values.put("normalCO", data.get("co"));
			values.put("normalSO2", data.get("so2"));
			values.put("normalPM10", data.get("pm10"));
			values.put("normalPM25", data.get("pm2_5"));
		}
		for (Map<String, String> data : datasNormal) {
			String time = data.get("time");
			if (valuesMap.get(time) == null) {
				valuesMap.put(time, new HashMap<String, String>());
			}
			Map<String, String> values = valuesMap.get(time);

			values.put("normalTemp", data.get("temp"));
			values.put("normalRH", data.get("humi"));
			values.put("normalWS", data.get("wse"));
			values.put("normalWD", getWindDirection(data.get("wd")));
		}

		Map<String, Integer> timeDetals = new HashMap<String, Integer>();
		Map<String, String> factorColumnMap = getFactorColumnMap();
		String sqlCombine = "";
		for (String timeData : valuesMap.keySet()) {
			Map<String, String> values = valuesMap.get(timeData);
			StringBuilder colSB = new StringBuilder();
			StringBuilder valSB = new StringBuilder();
			Date date = null;
			Calendar now = Calendar.getInstance();
			try {
				date = sdf19.parse(timeData);
				now.setTime(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			timeDetals.put("year", now.get(Calendar.YEAR));
			timeDetals.put("month", now.get(Calendar.MONTH) + 1);
			timeDetals.put("day", now.get(Calendar.DAY_OF_MONTH));
			timeDetals.put("hour", now.get(Calendar.HOUR_OF_DAY));
			timeDetals.put("minute", now.get(Calendar.MINUTE));
			timeDetals.put("second", now.get(Calendar.SECOND));

			colSB.append("INSERT INTO ").append(tableName)
					.append(" (siteId, mt, datatime, createTs, updateTs, year, month, day, hour, minute, second");
			valSB.append(" VALUES (").append(siteId).append(",'").append(mtCode).append("', '").append(timeData).append("' ,GETDATE(), GETDATE(), ")
					.append(timeDetals.get("year")).append(",").append(timeDetals.get("month")).append(",").append(timeDetals.get("day")).append(",")
					.append(timeDetals.get("hour")).append(",").append(timeDetals.get("minute")).append(",").append(timeDetals.get("second"));

			for (String factor : values.keySet()) {
				String columnNo = factorColumnMap.get(factor);
				if (null != columnNo) {
					colSB.append(", ").append("f").append(columnNo);
					valSB.append(", ").append(values.get(factor));
				}
			}
			colSB.append(")");
			valSB.append(")");

			colSB.append(valSB.toString());
			sqlCombine = colSB.toString();
			try {
				dm.executeUpdate(sqlCombine);
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			System.out.println("Sprider normal data- siteId:siteId, time:" + timeData);
		}
	}

	// 解析风向转化为角度
	private String getWindDirection(String string) {

		String windDire = "";
		switch (string) {
		case "北风":
			windDire = "0";
			break;
		case "东北风":
			windDire = "45";
			break;
		case "东风":
			windDire = "90";
			break;
		case "东南风":
			windDire = "135";
			break;
		case "南风":
			windDire = "180";
			break;
		case "西南风":
			windDire = "225";
			break;
		case "西风":
			windDire = "270";
			break;
		case "西北风":
			windDire = "315";
			break;
		default:
			windDire = "";
		}
		return windDire;
	}

	// 因子和列的对应关系，暂时写定值
	private Map<String, String> getFactorColumnMap() {

		Map<String, String> map = new HashMap<String, String>();

		map.put("normalSO2", "001");
		map.put("normalNO2", "002");
		map.put("normalO3", "003");
		map.put("normalCO", "004");
		map.put("normalPM10", "005");
		map.put("normalPM25", "006");
		map.put("normalWS", "007");
		map.put("normalWD", "008");
		map.put("normalTemp", "009");
		map.put("normalRH", "010");
		map.put("normalPre", "011");

		return map;
	}

	/**
	 * unicode 转 UTF-8
	 * 
	 * @param theString
	 * @return
	 */
	private static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';

					else if (aChar == 'n')

						aChar = '\n';

					else if (aChar == 'f')

						aChar = '\f';

					outBuffer.append(aChar);
				}

			} else
				outBuffer.append(aChar);
		}

		return outBuffer.toString();
	}

	/**
	 * 判断插入数据的开始时间，比较数据库中的时间和输入时间选择哪个作为开始时间
	 * 
	 * @param dm
	 * @param startsTime
	 * @param tableName
	 * @param siteId
	 * @param mtCode
	 * @return
	 */
	private String getLastDataTime(DataManager dm, String startsTime, String tableName, String siteId, String mtCode) {
		String sql = "SELECT TOP 1 CONVERT(VARCHAR(19), datatime, 120) as datatime FROM " + tableName + " WHERE siteId = " + siteId + "and mt='"
				+ mtCode + "' ORDER BY datatime DESC";
		DataMap dataMap = null;
		try {
			dataMap = dm.getUniqueData(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// 当数据库中不存在数据时，返回输入时间
		if (null == dataMap || dataMap.size() == 0) {
			return startsTime;
		}

		String mtTimeString = (String) dataMap.get("datatime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date mtTime = sdf.parse(mtTimeString);
			Date startTimeD = sdf.parse(startsTime);
			long mtTimeL = mtTime.getTime();
			long startTimeL = startTimeD.getTime();
			long systimeL = new Date().getTime();

			// 当数据库中时间和当前时间差小于一个小时时，返回null作为标记参数
			if (Math.abs(systimeL - mtTimeL) <= 2 * 3600 * 1000) {
				return null;
			}

			// 当数据库中最大时间大于输入时间时，返回数据库中时间字符串，否则返回输入时间字符串
			if (mtTimeL > startTimeL) {
				return mtTimeString;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return startsTime;
	}
}
