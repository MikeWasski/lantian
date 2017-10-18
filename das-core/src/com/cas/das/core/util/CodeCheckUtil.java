package com.cas.das.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 * 与数据包发送有关的数据字段压缩、校验、计算长度、格式化...相关方法工具类
 * */
public class CodeCheckUtil {
	public static void main(String[] args) {
		String str = "JZ2112345678902017-09-15 00:00:0000130101@@@2017-09-15 00:00:00tek2A####";
		String str2 = "JC1698765432102017-03-01 13:39:110048@@@JZ16|SO2|2017-02-28 11:00:00|2017-03-01 11:00:00|1|3|192.168.10.148:9900tek0b####";
		
		System.out.println(checksumXOR(str));
		//System.out.println(checksumLength(str2));
		
	}
	
	/**
	 * 对长度过大的字节数组（待发送数据）按照65535字节一个进行分包
	 * */
	public static List<byte[]> toSubcontract(byte[] zip) {
		List<byte[]> lists = new ArrayList<byte[]>();
		double dou = 65535d;
		int num = (int) Math.ceil((double) zip.length / dou);
		for (int i = 0; i < num; i++) {
			if (i != num - 1) {
				byte[] sb = subBytes(zip, (int) (i * dou), (int) dou);
				lists.add(sb);

			} else {
				byte[] sb = subBytes(zip, (int) (i * dou),
						(int) (zip.length - (i * dou)));
				lists.add(sb);
			}
		}
		return lists;

	}
	
	/**
	 * src:原始数组 begin:起始位置 count:要截取的长度
	 * */
	public static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		System.arraycopy(src, begin, bs, 0, count);
		return bs;
	}
	
	/**
	 * BZip2数据压缩方法
	 * */
	public static byte[] compress(byte srcBytes[]) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BZip2CompressorOutputStream bcos = new BZip2CompressorOutputStream(out);
		bcos.write(srcBytes);
		bcos.close();
		return out.toByteArray();
	}
	
	/**
	 * BZip2数据解压方式
	 * */
	@SuppressWarnings("resource")
	public static byte[] uncompress(byte[] bytes) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		try {
			BZip2CompressorInputStream ungzip = new BZip2CompressorInputStream(in);
			byte[] buffer = new byte[2048];
			int n;
			while ((n = ungzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.toByteArray();
	}
	
	/**
	 * gzip数据压缩方法
	 * */
	public static String gzip(String primStr) throws Exception {
		if (primStr == null || primStr.length() == 0) {
			return primStr;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		GZIPOutputStream gzip = null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(primStr.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (gzip != null) {
				try {
					gzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// return new sun.misc.BASE64Encoder().encode(out.toByteArray());
		return new String(out.toByteArray(), "UTF-8");
	}
	
	/**
	 * 输入完整请求字符串校验数据部分长度（错误）
	 * */
	@SuppressWarnings("unused")
	private static boolean checksumLength(String str) {
		String[] substr = str.split("@@@");
		String length = substr[0].substring(substr[0].length() - 4,
				substr[0].length());
		String length2 = lengthFormat(substr[1].split("tek")[0].length());
		return length.equals(length2) ? true : false;

	}
	
	/**
	 * 十进制数转十六进制字符串，长度固定4字符
	 * */
	public static String lengthFormat(int length) {
		String strs = Integer.toHexString(length);
		String s = "";
		if (4 > strs.length()) {
			for (int i = 0; i < (4 - strs.length()); i++) {
				s += "0";
			}
		} else if (4 < strs.length()) {
			return "0000";
		}
		return s + strs;
	}
	
	/**
	 * 输入完整请求字符串校校验异或码
	 * */
	public static boolean checksumXOR(String str) {
		if (null == str) {
			return false;
		}
		String substr = str.substring(0, str.length() - 6);
		String code = str.split("tek")[1].substring(0, 2);
		String sode2 = getCode(substr);
		return code.equalsIgnoreCase(sode2) ? true : false;
	}
	
	/**
	 * 生成数据部分校验码
	 * */
	public static String getCode(String dataPacket) {
		byte byt = 0x00;
		try {
			for (byte b : dataPacket.getBytes("GB2312")) {
				byt ^= b;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// 校验码部分
		String check = toHex(byt);
		return check;

	}
	
	/**
	 * 生成数据部分校验码
	 * */
	public static String getCode(byte[] dataPacket) {
		byte byt = 0x00;
		for (byte b : dataPacket) {
			byt ^= b;
		}
		// 校验码部分
		String check = toHex(byt);
		return check;

	}
	
	/**
	 * 把byte 转化为两位十六进制数字符串
	 * */
	public static String toHex(byte b) {
		String result = Integer.toHexString(b & 0xFF);
		if (result.length() == 1) {
			result = '0' + result;
		}
		return result;
	}
	
	/**
	 * 把int 转化为两位十六进制数字符串
	 * */
	public static String toHex(int b) {
		String result = Integer.toHexString(b);
		if (result.length() == 1) {
			result = '0' + result;
		}
		return result;
	}
	
	/**
	 * 获取字节数组的十六进制字符串表示形式，用于把数据包数据部分压缩后字节数组转化为字符串
	 * */
	public static String byteArrToHexStr(byte[] byteArr) throws Exception {
		int iLen = byteArr.length;
		// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = byteArr[i];
			// 把负数转换为正数
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// 小于等于0F的数需要在前面补0
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}
	
	/**
	 * 把字节流的十六进制字符串表示形式再转化为字节流
	 * */
	public static byte[] hexStrToByteArr(String str) throws Exception {
		byte[] arrB = str.getBytes();
		int iLen = arrB.length;
		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] byteArr = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			byteArr[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return byteArr;
	}
	
	//把数据包类推入队列
	public synchronized static void sendQueue(LinkedBlockingQueue<DataTransmission> queue,DataTransmission entity){
		try {
			queue.put(entity);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public synchronized static void sendQueue(LinkedBlockingQueue<DataTransmission> queue,List<DataTransmission> entities){
		try {
			for (DataTransmission dataTransmission : entities) {
				queue.put(dataTransmission);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
