package com.cas.das.core.entity;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cas.das.core.util.CodeCheckUtil;
import com.cas.das.core.util.DataTransmission;

/*
 * 数据回补响应生成实体类
 **/

public class ResponseProtocol implements DataTransmission {
	private String dataType;//数据类型
	private String siteCode;//子站边号
	private String dataTime;//数据时标
	private String dataLength;//数据长度
	private String headDelimiter;//头部分隔符
	private String data;//数据部分
	private String tailDelimiter;//尾部分隔符
	private String checkValue;//校验值
	private String terminator;//结束符

	public String getDataType() {
		return dataType;
	}


	public void setDataType(String dataType) {
		this.dataType = dataType;
	}


	public String getSiteCode() {
		return siteCode;
	}


	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}


	public String getDataTime() {
		return dataTime;
	}


	private void setDataTime(String dataTime) {
		this.dataTime = dataTime;
	}


	public String getDataLength() {
		return dataLength;
	}

	public String getHeadDelimiter() {
		return headDelimiter;
	}


	public void setHeadDelimiter(String headDelimiter) {
		this.headDelimiter = headDelimiter;
	}


	public String getData() {
		return data;
	}


	public void setData(String data) {
		String[] strs = data.split("\\|");
		StringBuilder dataStr = new StringBuilder(strs[0]);
		if(dataType.equals("JC17")){
			dataStr.append("|").append(strs[3]).append("|")
					.append(strs[4]).append("|").append(strs[1]).append("|3|3|").append(strs[2]).append("|").append(strs[7]).append("|0;");			
		}else{
			dataStr.append("|").append(strs[3]).append("|")
					.append(strs[4]).append("|").append(strs[1]).append("|1|3|").append(strs[2]).append("|").append(strs[7]).append("|0");
		}
		this.data = dataStr.toString();
		this.dataLength = CodeCheckUtil.lengthFormat(data.length());
	}


	public String getTailDelimiter() {
		return tailDelimiter;
	}


	public void setTailDelimiter(String tailDelimiter) {
		this.tailDelimiter = tailDelimiter;
	}


	public String getCheckValue() {
		return checkValue;
	}


	private void setCheckValue() {
		String str = dataType+siteCode+dataTime+dataLength+headDelimiter+data+tailDelimiter;
		this.checkValue = CodeCheckUtil.getCode(str);
	}


	public String getTerminator() {
		return terminator;
	}


	public void setTerminator(String terminator) {
		this.terminator = terminator;
	}


	@Override
	public byte[] getDataPacket() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String formatdate = format.format(date);
		setDataTime(formatdate);
		
		setCheckValue();
		String str = dataType+siteCode+dataTime+dataLength+headDelimiter+data+tailDelimiter+checkValue+terminator;
		byte[] by = null;
		try {
			by = str.getBytes("GB2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return by;
	}
}
