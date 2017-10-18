package com.cas.das.core.entity;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import com.cas.das.core.util.DataTransmission;

/**
 * 发送数据包实现类
 * */

public class MessageSending implements Runnable {
	private LinkedBlockingQueue<DataTransmission> queue;
	private IoSession session;
	private boolean type;

	public MessageSending(LinkedBlockingQueue<DataTransmission> queue,
			IoSession session,boolean type) {
		super();
		this.queue = queue;
		this.session = session;
		this.type = type;
	}
	

	public boolean isType() {
		return type;
	}


	public void setType(boolean type) {
		this.type = type;
	}


	/*@Override
	public void run() {
		while (type) {
			if (!queue.isEmpty()) {
				try {
					String data = queue.take().getDataPacket();
					//System.out.println("发送数据包:" + data);
					session.write(data);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}*/
	@Override
	public void run() {
		while (type) {
			if (!queue.isEmpty()) {
				try {
					byte[] data = queue.take().getDataPacket();
					System.out.println("发送数据包一次");
					//session.write(data);
					sendData(data,session);
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void sendData(byte[] data,IoSession session){
		WriteFuture writeFuture = session.write(data);
		writeFuture.join();
		if(!writeFuture.isWritten()){
			sendData(data,session);
		}
	}

}
