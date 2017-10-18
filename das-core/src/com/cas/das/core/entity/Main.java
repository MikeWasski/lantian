package com.cas.das.core.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.cas.das.core.util.CodeCheckUtil;
/**
 * 测试类，没有实际用途
 * */
public class Main {
	public static void main(String[] args) {
		/*Heartbeat ht = new Heartbeat();
		ht.setDataType("JC09");
		ht.setSiteCode("4001001000");
		ht.setTerminator("####");
		sendDataPacket(ht.getDataPacket());*/
		
		
		/*String value = "JZ16,2017-02-28 12:00:00,SO2,0.021,;JZ16,2017-02-28 13:00:00,SO2,0.02,;JZ16,2017-02-28 14:00:00,SO2,0.021,;JZ16,2017-02-28 15:00:00,SO2,0.023,;JZ16,2017-02-28 16:00:00,SO2,0.022,;JZ16,2017-02-28 17:00:00,SO2,0.022,;JZ16,2017-02-28 18:00:00,SO2,0.021,;JZ16,2017-02-28 19:00:00,SO2,0.021,;JZ16,2017-02-28 20:00:00,SO2,0.021,;JZ16,2017-02-28 21:00:00,SO2,0.021,;JZ16,2017-02-28 22:00:00,SO2,0.022,;JZ16,2017-02-28 23:00:00,SO2,0.021,;JZ16,2017-03-01 00:00:00,SO2,0.022,;JZ16,2017-03-01 01:00:00,SO2,0.022,;JZ16,2017-03-01 02:00:00,SO2,0.022,;JZ16,2017-03-01 03:00:00,SO2,0.022,;JZ16,2017-03-01 04:00:00,SO2,0.021,;JZ16,2017-03-01 05:00:00,SO2,0.02,;JZ16,2017-03-01 06:00:00,SO2,0.022,;JZ16,2017-03-01 07:00:00,SO2,0.021,;JZ16,2017-03-01 08:00:00,SO2,0.021,;JZ16,2017-03-01 09:00:00,SO2,0.022,;JZ16,2017-03-01 10:00:00,SO2,0.022,;JZ16,2017-03-01 11:00:00,SO2,0.022,;";
		byte[] zip = null;
		try {
			//对字符串进行压缩
			zip = CodeCheckUtil.compress(value.getBytes("GB2312"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}*/
		//得到压缩和分包后的数据字符串
		/*List<byte[]> zips = CodeCheckUtil.toSubcontract(zip);
		DataTransmissionProtocol dp = new DataTransmissionProtocol();
		dp.setDataType("JZ12");
		dp.setSiteCode("4001001000");
		dp.setDataTime("2017-09-27 14:01:00");
		dp.setTotalPackage(1);
		dp.setPacketNumber(zips.size());
		dp.setHeadDelimiter("@@@");
		dp.setData(zips.get(0));
		dp.setTailDelimiter("tek");
		dp.setTerminator("####");*/
		//sendDataPacket(dp.getDataPacket());
		
		/*ResponseProtocol is = new ResponseProtocol();
		is.setDataType("JC17");
		is.setSiteCode("4001001000");
		is.setHeadDelimiter("@@@");
		is.setData("2017-3-1 13:39:11|2017-2-28 11:00:00|2017-3-1 11:00:00|JZ16|3|3|SO2|192.168.10.148:9900|0;");
		is.setTailDelimiter("tek");
		is.setTerminator("####");
		System.out.println(new String(is.getDataPacket()));*/
		//sendDataPacket(is.getDataPacket());
		
		List<String> jsonStrs = new ArrayList<String>();
		if(jsonStrs.size()!=0){
			for (String jsonStr : jsonStrs) {
				byte[] zip = null;
				try {
					// 对字符串进行压缩
					zip = CodeCheckUtil.compress(jsonStr
							.getBytes("GB2312"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				// 得到压缩和分包后的数据字符串
				List<byte[]> zips = CodeCheckUtil
						.toSubcontract(zip);
				for (int i = 0; i < zips.size(); i++) {
					DataReconciliationProtocol protocol = new DataReconciliationProtocol();
					protocol.setDataType("JC20");
					protocol.setSiteCode("9876543210");
					protocol.setTotalPackage(zips.size());
					protocol.setPacketNumber(i + 1);
					protocol.setHeadDelimiter("@@@");
					protocol.setData(zips.get(i));
					protocol.setTailDelimiter("tek");
					protocol.setTerminator("####");
					System.out.println(new String(protocol.getDataPacket()));
				}
			}
		}else{
			DataReconciliationProtocol protocol = new DataReconciliationProtocol();
			protocol.setDataType("JC20");
			protocol.setSiteCode("9876543210");
			protocol.setTotalPackage(1);
			protocol.setPacketNumber(0);
			protocol.setHeadDelimiter("@@@");
			protocol.setData(new byte[0]);
			protocol.setTailDelimiter("tek");
			protocol.setTerminator("####");
			System.out.println(new String(protocol.getDataPacket()));
		}
		
	}
	
	private static void sendDataPacket(String byteArray) {
		Socket socket = null;
		OutputStream os = null;
		try {
			socket = new Socket("10.5.11.116", 53421);//203.91.44.5  10.5.11.116
			os = socket.getOutputStream(); 
			os.write(byteArray.getBytes("GB2312"));
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != socket){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null != os){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
