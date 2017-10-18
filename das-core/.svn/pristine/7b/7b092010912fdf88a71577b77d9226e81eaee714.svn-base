package com.cas.das.core.service.ext;

import java.io.IOException;

import com.cas.platform.service.ServiceHandle;

/**
 * 定时调用监控器
 * 
 * @author xiang_wang
 */
public class TimedCallMonitor {

	/**
	 * 监视线程
	 */
	private TimedCallThread timedCallThread;

	/**
	 * 构造
	 * 
	 * @param path
	 * @param handle
	 * @throws IOException
	 */
	public TimedCallMonitor(ServiceHandle handle, int intervalTime) throws IOException {

		if (null == handle) {
			throw new IOException("Error: can not instance FtpMonitor, AccessServiceHandle is null.");
		}

		timedCallThread = new TimedCallThread(handle, intervalTime);
	}

	public void startup() {
		timedCallThread.start();
	}

	public void shutdown() {
		timedCallThread.stopFlg = true;
	}

	public static void main(String args[]) throws IOException, InterruptedException {
	}

}

/**
 * 定时调用线程。
 */
class TimedCallThread extends Thread {

	/**
	 * 间隔时间(秒)
	 */
	int intervalTime;

	/**
	 * 服务操作类
	 */
	ServiceHandle handle;

	/**
	 * 关闭线程表示
	 */
	boolean stopFlg = false;
	
	/**
	 * 构造
	 * 
	 * @param path
	 * @param handle
	 * @throws IOException
	 */
	TimedCallThread(ServiceHandle handle, int intervalTime) throws IOException {
		this.handle = handle;
		this.intervalTime = intervalTime;
	}

	@Override
	public void run() {

		while (!stopFlg) {
			try {
				handle.getServiceProcessor().process(handle, null);
				System.out.println("Run over !");
				try {
					Thread.sleep(intervalTime * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

}
