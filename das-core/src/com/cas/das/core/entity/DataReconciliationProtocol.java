package com.cas.das.core.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cas.das.core.util.CodeCheckUtil;
import com.cas.das.core.util.DataTransmission;

/*
 * 大型设备远程数据回传指令-监测数据传输（JC20）协议生成实体类
 **/

public class DataReconciliationProtocol implements DataTransmission {
	private String dataType;// 数据类型
	private String siteCode;// 子站边号
	private String dataTime;// 数据时标
	private String dataLength;// 数据长度
	private String totalPackage;// 总包数
	private String packetNumber;// 包号
	private String headDelimiter;// 头部分隔符
	private byte[] data;// 数据部分
	private String tailDelimiter;// 尾部分隔符
	private String checkValue;// 校验值
	private String terminator;// 结束符

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public String getTotalPackage() {
		return totalPackage;
	}

	public void setTotalPackage(int totalPackage) {
		this.totalPackage = CodeCheckUtil.toHex(totalPackage);
	}

	public String getPacketNumber() {
		return packetNumber;
	}

	public void setPacketNumber(int packetNumber) {
		this.packetNumber = CodeCheckUtil.toHex(packetNumber);
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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] bytes) {
		this.data = bytes;
		// 计算出压缩数据字符串长度换算成十六进制数然后赋值
		this.dataLength = CodeCheckUtil.lengthFormat(data.length);
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

	/**
	 * 此方法在所有set方法之后调用
	 * */
	private void setCheckValue() {
		// 拼接校验部分字符串
		String subString = dataType + siteCode + dataTime + dataLength + totalPackage + packetNumber + headDelimiter ;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(subString.getBytes("GB2312"));
			baos.write(data);
			baos.write(tailDelimiter.getBytes("GB2312"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] all = baos.toByteArray();
		//获取异或校验码并赋值
		this.checkValue = CodeCheckUtil.getCode(all);
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
		String subString = dataType + siteCode + dataTime + dataLength
				+ totalPackage + packetNumber + headDelimiter;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(subString.getBytes("GB2312"));
			baos.write(data);
			baos.write(tailDelimiter.getBytes("GB2312"));
			baos.write(checkValue.getBytes("GB2312"));
			baos.write(terminator.getBytes("GB2312"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] all = baos.toByteArray();
		return all;
	}
}
