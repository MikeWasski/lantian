package com.cas.das.core.ftpclient;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class ClientCodecFactory implements ProtocolCodecFactory{

	// 这里注册自己编写的解码工具
		private final MessageEncoder encoder; // 编码
		private final MessageDecoder decoder; // 解码

		public ClientCodecFactory(String charsetEncoder,String charsetDecoder) {
			encoder = new MessageEncoder(charsetEncoder);
			decoder = new MessageDecoder(charsetDecoder);
		}

		@Override
		public ProtocolEncoder getEncoder(IoSession session) throws Exception {
			return encoder;
		}

		@Override
		public ProtocolDecoder getDecoder(IoSession session) throws Exception {
			return decoder;
		}

}
