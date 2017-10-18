package com.cas.das.core.ftp;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.jlrnt.dbc.DataManager;
import net.jlrnt.dbc.Transaction;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MyServiceIoHandler extends IoHandlerAdapter{
	private DataManager dataManager;

	public MyServiceIoHandler(DataManager dataManager){
		this.dataManager = dataManager;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		sessionClosed(session);
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		super.inputClosed(session);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		
		String doas = message.toString();
		if("Heartbeat thread".equals(doas)){
			System.out.println(doas);
		}else{
			String monitorTime = doas.split("&&")[1].split(",")[0].split("=")[1];
			String so2 = doas.split("&&")[1].split(",")[1].split("=")[1];
			String no2 = doas.split("&&")[1].split(",")[2].split("=")[1];
			String o3 = doas.split("&&")[1].split(",")[3].split("=")[1];
			monitorTime = monitorTime.substring(0, 19);
			
			StringBuffer sql = new StringBuffer("insert into MT_DATA_JSON_2017 (minute,datatime,second,siteId,mt,month,year,hour,day,f007,f008,f009,createTs,updateTs) VALUES (");
			Map<String, Object> sqlItemMap = new LinkedHashMap<String, Object>();
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(monitorTime);
			Calendar now = Calendar.getInstance();
			now.setTime(date);
			sqlItemMap.put("minute", now.get(Calendar.MINUTE));
			sqlItemMap.put("datatime", "'" + monitorTime + "'");
			sqlItemMap.put("second", now.get(Calendar.SECOND));

			String id = doas.split("&&")[2].split("<")[0];
			sqlItemMap.put("siteId", Integer.parseInt(id));
			sqlItemMap.put("mt", "'MT_DOAS'");
			sqlItemMap.put("month", (now.get(Calendar.MONTH) + 1));
			sqlItemMap.put("year", now.get(Calendar.YEAR));
			sqlItemMap.put("hour", now.get(Calendar.HOUR_OF_DAY));
			sqlItemMap.put("day", now.get(Calendar.DAY_OF_MONTH));
			sqlItemMap.put("f007", "'"+so2+"'");
			sqlItemMap.put("f008", "'"+no2+"'");
			sqlItemMap.put("f009", "'"+o3+"'");
			StringBuffer insertSb = new StringBuffer();
			Set<String> keyset = sqlItemMap.keySet();
			for (String key : keyset) {
				insertSb.append(sqlItemMap.get(key)).append(",");
			}
			sql.append(insertSb.toString()).append("GETDATE(),GETDATE())");
			
			Transaction t = dataManager.beginTransaction();
			System.out.println(sql.toString());
			//dataManager.executeUpdate(t, sql.toString());
			t.commit();
			System.out.println("Doas Insert Successfully");
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, "ok");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
	}
}
