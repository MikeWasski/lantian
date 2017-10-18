package com.cas.das.core.entity;

import java.io.UnsupportedEncodingException;

import com.cas.das.core.util.CodeCheckUtil;
import com.cas.das.core.util.DataTransmission;
/*
 * 数据状态传输协议生成实体类
 * 
 **/
public class InstrumentStatus implements DataTransmission {
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

	public void setDataTime(String dataTime) {
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
		this.data = data;
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
