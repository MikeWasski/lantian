package com.cas.das.core.ftpclient;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.cas.das.core.util.CodeCheckUtil;

public class MessageDecoder extends CumulativeProtocolDecoder {

	private static Logger log = Logger.getLogger(MessageDecoder.class);

	private String charset;

	public MessageDecoder(String charset) {
		this.charset = charset;
	}

	@Override
	protected synchronized boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
			throws Exception {
		in.mark();
		byte[] rqByte = new byte[4];
		in.get(rqByte);
		String dataType = new String(rqByte,charset);
		if("JC16".equals(dataType) || "JC07".equals(dataType) || "JC08".equals(dataType) || "JZ21".equals(dataType) || "JZ22".equals(dataType)|| "JC20".equals(dataType)){
			// 按照数据回补指令（JC16）数据包结构截取数据字段
			// 按照仪器状态数据传输协议（JC07、JC07）数据包结构截取数据字段
			// 按照大型设备传输协议（JC21、JC22）数据包结构截取数据字段
			// 创建map存放数据
			Map<String,String> map = new HashMap<String,String>();
			map.put("dataType", dataType);
			StringBuilder request = new StringBuilder(dataType);
			rqByte = new byte[10];
			in.get(rqByte);
			String siteCode = new String(rqByte,charset);
			map.put("siteCode", siteCode);
			request.append(siteCode);
			rqByte = new byte[19];
			in.get(rqByte);
			String dataTime = new String(rqByte,charset);
			map.put("dataTime", dataTime);
			request.append(dataTime);
			rqByte = new byte[4];
			in.get(rqByte);
			String dataLength = new String(rqByte,charset);
			request.append(dataLength);
			//如果是JC21或JC22请求，添加截取总包数和包号字段
			if("JZ21".equals(dataType) || "JZ22".equals(dataType) || "JC20".equals(dataType)){
				rqByte = new byte[2];
				in.get(rqByte);
				String totalPackage = new String(rqByte,charset);
				map.put("totalPackage", totalPackage);
				request.append(totalPackage);
				rqByte = new byte[2];
				in.get(rqByte);
				String packetNumber = new String(rqByte,charset);
				request.append(packetNumber);
				map.put("packetNumber", packetNumber);
			}
			if(in.remaining() >= (Integer.parseInt(dataLength,16)+3+3+2+4)){
				rqByte = new byte[3];
				in.get(rqByte);
				String headDelimiter = new String(rqByte,charset);
				request.append(headDelimiter);
				rqByte = new byte[Integer.parseInt(dataLength,16)];
				in.get(rqByte);
				String data = new String(rqByte,charset);
				map.put("data", data);
				request.append(data);
				rqByte = new byte[3];
				in.get(rqByte);
				String tailDelimiter = new String(rqByte,charset);
				request.append(tailDelimiter);
				rqByte = new byte[2];
				in.get(rqByte);
				String checkValue = new String(rqByte,charset);
				request.append(checkValue);
				rqByte = new byte[4];
				in.get(rqByte);
				String terminator = new String(rqByte,charset);
				request.append(terminator);
				//System.out.println(request.toString());
				if(CodeCheckUtil.checksumXOR(request.toString())){
					out.write(map);
				}else{
					System.out.println("校验值不正确");
					log.warn("校验值不正确");
				}
				if (in.remaining() > 0) {
					System.out.println("粘包长度 package left=" + in.remaining());
					log.warn("粘包长度 package left=" + in.remaining());
					return true;
				}
				return false;
			}
			System.out.println("********** 数据报文不完整 **********  断包长度： " + in.remaining());
			log.warn("********** 数据报文不完整 **********  断包长度： " + in.remaining());
			in.reset();
			return false;
		} 
		//如果是错误请求头，就清空缓存区
		int i = in.remaining();
		byte[] rqByte1 = new byte[i];
		in.get(rqByte1);
		//String dataType1 = new String(rqByte1,charset);
		System.out.println("不是指定请求:"+dataType);
		log.warn("不是指定请求:"+dataType);
		return false;
	}

}
