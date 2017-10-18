package com.cas.das.core.service.processor;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.jlrnt.common.util.DateUtils;
import net.jlrnt.common.util.json.JsonUtils;
import net.jlrnt.dbc.DataManager;
import net.jlrnt.dbc.DataMap;
import net.jlrnt.dbc.Transaction;

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
 * 抓取全国城市的常规数据
 * <p>
 * 从输入的时间和数据库最新的数据中 最新的时间开始抓取数据，每次抓取下一小时的数据，知道抓取的最新的数据位置。 正常运行后，定时调用该方法。
 * </p>
 * 
 * @author yushun_gong
 */
public class SpriderNormalDataOfCountryProcessor extends ServiceProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public void process(ServiceHandle handle, Object argument) {
		String method = ((TextParam) handle.getProcessorInnerParam("method")).getValue();
		DataManager dataManager = (DataManager) handle.getAttribute("dataManager");
		// 获取数据库最后记录时间
		String timeLast = queryTime(dataManager);
		// 如果数据库没数据，从当天零时开始导入
		if (null == timeLast || "".equals(timeLast)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			Date date = new Date();
			timeLast = sdf.format(date);
		}
		// 获取数据库最后记录时间到当前时间前一个小时的所有时间数（精确到小时）
		List<String> timeList = getHourBetweenDates(timeLast);
		if (null == timeList || 0 == timeList.size()) {
			System.out.println("It's up to date");
		} else {
			for (String timePoint : timeList) {
				// 获取指定时间全国AQI数据
				Map<String, Object> cityAqiInfoMap = null;
				Map<String, Object> resultMap = null;
				Map<String, Object> dataMap = null;
				List<Map<String,String>> rowsList = new ArrayList<>();
				if ("spriderRequest".equals(method)) {
					// cityAqiInfoMap = spriderRequest
					// TODO www.zq12369.com网站只能抓取最新AQI数据，该网站数据暂时无法使用
				} else {
					cityAqiInfoMap = spriderCountryData(timePoint + ":00:00");
				}
				Boolean success = (Boolean)cityAqiInfoMap.get("success");
				if (success) {
					resultMap = (Map<String, Object>)cityAqiInfoMap.get("result");
					dataMap = (Map<String, Object>)resultMap.get("data");
					rowsList = (List<Map<String,String>>)dataMap.get("rows");
					// 解析数据并入库
					insertData(rowsList, dataManager);
					System.out.println(timePoint+" : Insert successfully");
				} else {
					System.out.println(timePoint + " : No data.");
				}

			}
			System.out.println("Import to latest data.");
		}
	}

	/**
	 * 获取指定时间到当前时间段内的所有时间数（精确到小时） 返回值格式（yyyy-mm-dd hh）
	 * */
	private List<String> getHourBetweenDates(String timeLast) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		List<String> subList = new ArrayList<String>();
		Calendar nowCalendar = Calendar.getInstance();
		String timeNow = sdf.format(nowCalendar.getTime());
		List<String> list = getAllHourBetweenDates(timeLast.substring(0, 10), timeNow.substring(0, 10));
		int startIndex = 0;
		int lastIndex = 0;
		int i = 0;
		for (String string : list) {
			if (string.equals(timeLast.substring(0, 13))) {
				startIndex = i;
			}
			if (string.equals(timeNow.substring(0, 13))) {
				lastIndex = i;
			}
			i++;
		}
		for (int j = (startIndex + 1); j < lastIndex; j++) {
			subList.add(list.get(j));
		}
		return subList;
	}

	/**
	 * 获取2个时间段内的所有时间数(精确到小时) 时间格式为yyyy-MM-dd 返回值格式（yyyy-mm-dd hh）
	 * */
	public List<String> getAllHourBetweenDates(String beginTime, String endTime) {
		List<String> hourList = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date beginTimeValue = null;
		Date endTimeValue = null;
		try {
			beginTimeValue = sdf.parse(beginTime.substring(0, 10));
			endTimeValue = sdf.parse(endTime.substring(0, 10));
		} catch (ParseException e) {

			e.printStackTrace();
		}
		List<Date> dateList = DateUtils.getBetweenDates(beginTimeValue, endTimeValue);
		List<String> dateStringList = new ArrayList<String>();
		for (int i = 0; i < dateList.size(); i++) {
			String stringDates = sdf.format(dateList.get(i));
			dateStringList.add(stringDates);
		}

		for (int i = 0; i < dateStringList.size(); i++) {
			String hourValue = null;
			for (int j = 0; j < 24; j++) {
				String hour = null;
				if (j < 10) {
					hour = "0" + j;
				} else {
					hour = j + "";
				}
				hourValue = dateStringList.get(i) + " " + hour;
				hourList.add(hourValue);
			}
		}

		return hourList;
	}

	/**
	 * 查询全国AQI数据最后写入时间
	 * */
	private String queryTime(DataManager dm) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		String time = null;
		try {
			Transaction t = dm.beginTransaction();
			String sql = "SELECT TOP 1 time FROM SPRIDER_DATA_AQI ORDER BY time DESC";
			List<DataMap> data = dm.getDataList(t, sql);
			if (null == data || data.isEmpty()) {
				return null;
			}
			t.commit();
			Date now = (Date) data.get(0).get("time");
			time = sdf.format(now);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return time;

	}

	/**
	 * 解析数据并入库
	 * 
	 * */
	private void insertData(List<Map<String, String>> cityAqiList, DataManager dm) {
		try {
			for (int i = 0; i < cityAqiList.size(); i++) {
				Transaction t = dm.beginTransaction();
				Map<String, String> cityAqiMap = cityAqiList.get(i);
				String sql = eneratingSQL(cityAqiMap);
				dm.executeUpdate(t, sql);
				t.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成SQL语句
	 */
	private String eneratingSQL(Map<String, String> cityAqiMap) {
		String code = cityAqiMap.get("cityid");
		String time = cityAqiMap.get("time");
		String cityname = cityAqiMap.get("cityname");
		String aqi = cityAqiMap.get("aqi");
		String pm2_5 = cityAqiMap.get("pm2_5");
		String pm10 = cityAqiMap.get("pm10");
		String so2 = cityAqiMap.get("so2");
		String no2 = cityAqiMap.get("no2");
		String co = cityAqiMap.get("co");
		String o3 = cityAqiMap.get("o3");
		String rank = cityAqiMap.get("rank");
		String primary_pollutant = cityAqiMap.get("primary_pollutant");
		String latitude = cityAqiMap.get("latitude");
		String longitude = cityAqiMap.get("longitude");
		StringBuilder sql = new StringBuilder(
				"insert into SPRIDER_DATA_AQI (time,cityname,aqi,pm2_5,pm10,so2,no2,co,o3,rank,primary_pollutant,latitude,longitude,code) VALUES (");
		sql.append("'").append(time).append("','").append(cityname).append("',").append(aqi).append(",").append(pm2_5).append(",").append(pm10)
				.append(",").append(so2).append(",").append(no2).append(",").append(co).append(",").append(o3).append(",").append(rank).append(",'")
				.append(primary_pollutant).append("',").append(latitude).append(",").append(longitude).append(",").append(code).append(")");
		return sql.toString();
	}

	/**
	 * 抓取全国监测点数据
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> spriderCountryData(String timeLast) {

		Map<String, Object> serialize = new HashMap<>();
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		// 每种抓取需要确定参数，确定后，使用SpriderUtil.decodeAqiStudyParam
		// 先解码，看参数是什么样的，再使用SpriderUtil.encodeAqiStudyParam进行编码。
		LinkedHashMap<String, Object> object = new LinkedHashMap<String, Object>();
		object.put("timepoint",timeLast);
		object.put("type", "HOUR");
		String secret =SpriderUtil.buildParam("GETMAPDATA",object);
		params.add(new BasicNameValuePair("d", secret));
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

	/**
	 * 抓取全国监测点数据(另一网址)
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> spriderRequest() {
		Map<String, Object> serialize = new HashMap<>();
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("appId", "a01901d3caba1f362d69474674ce477f"));
		params.add(new BasicNameValuePair("lat1", "MjcuODk1MjM5"));
		params.add(new BasicNameValuePair("lat2", "NTAuMTI2MjA4"));
		params.add(new BasicNameValuePair("level", "NQ=="));
		params.add(new BasicNameValuePair("lng1", "NDUuNzY4MDU3"));
		params.add(new BasicNameValuePair("lng2", "MTg3LjA1OTI5Nw=="));
		params.add(new BasicNameValuePair("method", "R0VUQ0lUWUxJU1RNT0JJTEU="));
		params.add(new BasicNameValuePair("secret", "d5e24d265bcd2c29a5f77fdb49e885bc"));

		HttpPost httppost = new HttpPost("https://www.zq12369.com/api/zhenqiapi.php");
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httppost.setHeader("Accept", "*/*");
		httppost.setHeader("Accept-Encoding", "gzip, deflate, br");
		httppost.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		httppost.setHeader("Connection", "keep-alive");
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		httppost.setHeader(
				"Cookie",
				"CNZZDATA1254317176=1287961089-1486534208-https%253A%252F%252Fwww.aqistudy.cn%252F%7C1496370592; UM_distinctid=15c66931dea464-04c3a01fc6c8cc8-46524130-1fa400-15c66931deb3dc; usercenter=120.305456%2C26.826187; userzoom=8; showBanner=0; usertype=POINT");
		httppost.setHeader("Host", "www.zq12369.com");
		httppost.setHeader("Referer", "https://www.zq12369.com/map.php");
		httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");
		httppost.setHeader("X-Requested-With", "XMLHttpRequest");

		// 发送请求
		HttpResponse httpresponse;
		try {
			httpresponse = httpclient.execute(httppost);
			HttpEntity entity = httpresponse.getEntity();
			String str = EntityUtils.toString(entity);
			Object obj = CallJs(str);

			String string = decodeUnicode(String.valueOf(obj));
			// 获取返回数据
			serialize = (Map<String, Object>) JsonUtils.unSerialize(string);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return serialize;
	}

	public Object CallJs(String str) throws FileNotFoundException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");

		InputStream resourceAsStream = SpriderNormalDataOfCountryProcessor.class.getClassLoader().getResourceAsStream("config/my.js");
		InputStreamReader reader = new InputStreamReader(resourceAsStream);

		engine.put("data", str);
		Object object = null;
		try {
			object = engine.eval(reader);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * unicode 转 UTF-8
	 * 
	 * @param theString
	 * @return
	 */
	private String decodeUnicode(String theString) {
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

}
