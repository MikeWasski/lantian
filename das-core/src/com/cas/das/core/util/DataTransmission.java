package com.cas.das.core.util;

/*
 * 数据传输协议接口
 * 
 **/
public interface DataTransmission {
	
	/**
	 * 调用此方法可生成直接可发送的数据包
	 * */
	public byte[] getDataPacket();
}
