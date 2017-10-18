package com.cas.das.core.ftpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.cas.das.core.entity.MessageSending;
import com.cas.das.core.util.DataTransmission;
import com.cas.platform.service.ServiceHandle;

public class HeartBeatListener implements IoServiceListener{
	private NioSocketConnector connector;
	private ServiceHandle handle;
	private String ip;
	private String port;
	private boolean type;
	
	public HeartBeatListener(NioSocketConnector connector,ServiceHandle handle,String ip,String port,boolean type){
		this.connector = connector;
		this.handle = handle;
		this.ip = ip;
		this.port = port;
		this.type = type;
	}

	@Override
	public void serviceActivated(IoService arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serviceDeactivated(IoService arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serviceIdle(IoService arg0, IdleStatus arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionClosed(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionCreated(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionDestroyed(IoSession arg0) throws Exception {
		repeatConnect("");
		
	}
	
	@SuppressWarnings("unchecked")
	public void repeatConnect(String content) {
		// 执行到这里表示Session会话关闭了，需要进行重连,我们设置每隔3s重连一次,如果尝试重连5次都没成功的话,就认为服务器端出现问题,不再进行重连操作
		Thread send = (Thread)handle.getAttribute("sendDataThread");
		send.interrupt();
		handle.removeAttribute("sendDataThread");
		int count = 0;// 记录尝试重连的次数
		while (true) {
			try {
				count++;// 重连次数加1
				ConnectFuture future = connector.connect(new InetSocketAddress(ip, Integer.parseInt(port)));
				future.awaitUninterruptibly();// 一直阻塞住等待连接成功
				IoSession session = future.getSession();// 获取Session对象
				if (session.isConnected()) {
					MessageSending messageSending = new MessageSending((LinkedBlockingQueue<DataTransmission>)handle.getAttribute("queue"),session,type);
					Thread sendDataThread = new Thread(messageSending);
					handle.setAttribute("sendDataThread", sendDataThread);
					sendDataThread.start();
					// 表示重连成功
					System.out.println(content +  " : 断线重连" + count + "次之后成功.....");
					count = 0;
					break;
				}
			} catch (Exception e) {
				if (count == 4) {
					System.out.println("断线重连" + 5 + "次之后仍然未成功,结束重连.....");
					break;
				} else {
					System.out.println("本次断线重连失败,3s后进行第" + (count + 1) + "次重连.....");
					try {
						Thread.sleep(3000);
						System.out.println("开始第"
								+ (count + 1) + "次重连.....");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

}
