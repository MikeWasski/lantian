package com.cas.das.core.ftp;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class MessageDecoder extends CumulativeProtocolDecoder {

	private static Logger log = Logger.getLogger(MessageDecoder.class);

	private String charset = "UTF-8";

	public MessageDecoder(String charset) {
		this.charset = charset;
	}

	
	protected synchronized boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
			throws Exception {
		if (in.remaining() >= 18) {
			in.mark();
			byte[] lenBytes = new byte[2];
			in.get(lenBytes, 0, 2);// 读取2个字节
			String recString = new String(lenBytes, charset);
			if("##".equals(recString)){
				byte[] len = new byte[4];
				in.get(len, 0, 4);
				//得出正文总长度
				int length = Integer.parseInt(new String(len, charset));
				if(in.remaining() >= (length+9)){
					byte[] buf = new byte[length+9];
					in.get(buf, 0, length+9);
					out.write(new String(buf,charset));
					if(in.remaining() > 0){
						log.warn("粘包长度 package left=" + in.remaining() + " data=" + in.toString());
					}
					return true;
				}else{
					log.warn("********** 数据报文不完整 **********  断包长度： " + in.remaining() + " 报文总长度： " + (length));
				}
			}
			in.reset();
		}else if("Heartbeat thread".length() == in.remaining()){
			byte[] buf = new byte["Heartbeat thread".length()];
			in.get(buf, 0, "Heartbeat thread".length());
			out.write(new String(buf,charset));
			return true;
		}
		return false;
	}
}