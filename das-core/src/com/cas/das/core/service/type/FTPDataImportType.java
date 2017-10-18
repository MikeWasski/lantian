package com.cas.das.core.service.type;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import net.jlrnt.dbc.DataManager;

import com.cas.das.core.C3P0DataSource;
import com.cas.das.core.ftp.MsgCodecFactory;
import com.cas.das.core.ftp.MyServiceIoHandler;
import com.cas.das.core.service.ext.TimedCallMonitor;
import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceType;
import com.cas.platform.service.def.param.TextParam;

/**
 * FTP数据导入
 * 
 * @author yushun-gong
 */
public class FTPDataImportType extends ServiceType {

	@Override
	public boolean startupService(ServiceHandle handle) {
		
		try {
			// 定义数据库链接信息
			String dbDriver = ((TextParam) handle.getProcessorInnerParam("driverClass")).getValue();
			String dbUrl = ((TextParam) handle.getProcessorInnerParam("url")).getValue();
			String dbUsr = ((TextParam) handle.getProcessorInnerParam("user")).getValue();
			String dbPwd = ((TextParam) handle.getProcessorInnerParam("password")).getValue();

			C3P0DataSource DBSource = C3P0DataSource.build(dbDriver, dbUrl, dbUsr, dbPwd, 1, 1, 10, 1, 2800);
			DataManager dataManger = new DataManager();
			dataManger.setLocalSource(DBSource);

			handle.setAttribute("dataManager", dataManger);
			
			server(dataManger);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	private void server(DataManager dataManager){
		Integer port = 53421;
		NioSocketAcceptor acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
		MyServiceIoHandler serverIoHandler = new MyServiceIoHandler(dataManager);
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.setBacklog(500);
		acceptor.getSessionConfig().setReuseAddress(true);
		/* 设置无延时发送 */
		acceptor.getSessionConfig().setTcpNoDelay(true);
		/* 设置接受信息缓冲区的大小 */
		acceptor.getSessionConfig().setReceiveBufferSize(5 * 1024);
		/* 设置读取信息缓冲区大小 */
		acceptor.getSessionConfig().setReadBufferSize(5 * 1024);
		/* 设置发送缓冲区大小 */
		acceptor.getSessionConfig().setSendBufferSize(1024);
		/* 设置空闲等待时间 *///读写 通道均在60*15秒内无任何操作就进入空闲状态
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60 * 15);
		/* 设置自定义报文编码格式 */
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MsgCodecFactory("UTF-8")));
		acceptor.setHandler(serverIoHandler);

		try {
			acceptor.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean shutdownService(ServiceHandle handle) {
		TimedCallMonitor monitor = (TimedCallMonitor) handle.getAttribute("monitor");
		if (null == monitor) {
			return false;
		}

		monitor.shutdown();
		handle.removeAttribute("monitor");

		return true;
	}

}
