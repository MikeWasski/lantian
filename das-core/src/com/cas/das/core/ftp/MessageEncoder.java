package com.cas.das.core.ftp;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class MessageEncoder extends ProtocolEncoderAdapter {

	private String charset="UTF-8";

	public MessageEncoder(String charset) {
		this.charset = charset;
	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buf = IoBuffer.allocate(1024).setAutoExpand(true);
		String strOut = message.toString();
		buf.putString(strOut,Charset.forName(charset).newEncoder());
		buf.flip();
		out.write(buf);
	}

}
