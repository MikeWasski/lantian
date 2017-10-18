package com.cas.das.core.entity;

import java.util.concurrent.LinkedBlockingQueue;

import com.cas.das.core.util.CodeCheckUtil;
import com.cas.das.core.util.DataTransmission;
/**
 * 心跳包生成实体类
 * */
public class HeartbeatSend implements Runnable{
	private LinkedBlockingQueue<DataTransmission> queue;
	private String intervalTime;
	private boolean type;
	private String siteCode;
	
	public HeartbeatSend(LinkedBlockingQueue<DataTransmission> queue,String intervalTime,boolean type,String siteCode){
		super();
		this.queue = queue;
		this.intervalTime = intervalTime;
		this.type = type;
		this.siteCode = siteCode;
	}
	
	
	public boolean isType() {
		return type;
	}


	public void setType(boolean type) {
		this.type = type;
	}


	@Override
	public void run() {
		while(type){
			try {
				Thread.sleep(Integer.parseInt(intervalTime)*1000);
				Heartbeat ht = new Heartbeat();
				ht.setDataType("JC12");
				ht.setSiteCode(siteCode);
				ht.setTerminator("####");
				//queue.put(ht);
				CodeCheckUtil.sendQueue(queue,ht);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
	}

}
