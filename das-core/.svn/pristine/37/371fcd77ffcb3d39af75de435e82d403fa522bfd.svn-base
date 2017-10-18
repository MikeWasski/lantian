package com.cas.das.core.service.type;

import net.jlrnt.dbc.DataManager;

import com.cas.das.core.C3P0DataSource;
import com.cas.das.core.service.ext.TimedCallMonitor;
import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceType;
import com.cas.platform.service.def.param.TextParam;

public class OzoneLidarDataAccessType extends ServiceType {

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

	@Override
	public boolean startupService(ServiceHandle handle) {
		TextParam monitorDirParam = (TextParam) handle.getServiceTypeParam("monitorDir");
		TextParam intervalTimeParam = (TextParam) handle.getServiceTypeParam("intervalTime");
		String monitorDir = monitorDirParam.getValue();
		try {
			// 定义监测目录
			int intervalTime = Integer.parseInt(intervalTimeParam.getValue());
			handle.setAttribute("directory", monitorDir);

			// 定义监测类型
			TimedCallMonitor monitor = new TimedCallMonitor(handle, intervalTime);
			// 定义数据库链接信息
			String dbDriver = ((TextParam) handle.getProcessorInnerParam("driverClass")).getValue();
			String dbUrl = ((TextParam) handle.getProcessorInnerParam("url")).getValue();
			String dbUsr = ((TextParam) handle.getProcessorInnerParam("user")).getValue();
			String dbPwd = ((TextParam) handle.getProcessorInnerParam("password")).getValue();

			C3P0DataSource DBSource = C3P0DataSource.build(dbDriver, dbUrl, dbUsr, dbPwd, 1, 1, 10, 1, 2800);
			DataManager dataManger = new DataManager();
			dataManger.setLocalSource(DBSource);

			handle.setAttribute("dataManager", dataManger);
			
			monitor.startup();
			handle.setAttribute("monitor", monitor);			

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
