package com.cas.das.core.service.type;

import java.io.IOException;

import net.jlrnt.dbc.DataManager;

import com.cas.das.core.C3P0DataSource;
import com.cas.das.core.service.ext.TimedCallMonitor;
import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceType;
import com.cas.platform.service.def.param.TextParam;


/**
 * 定时调用并插入数据库。
 * 
 * <p>
 * 目前应用于：<br/>
 * 1.抓取指定城市的常规数据，SpriderNormalDataByCityProcessor<br/>
 * 2.抓取全国城市的常规数据，SpriderNormalDataOfCountryProcessor<br/>
 * </p>
 * @author xiang_wang
 */
public class TimedCallAndImportDataAccessType extends ServiceType {

	@Override
	public boolean startupService(ServiceHandle handle) {

		TextParam intervalTimeParam = (TextParam) handle.getServiceTypeParam("intervalTime");

		int intervalTime = Integer.parseInt(intervalTimeParam.getValue());

		try {
			// 定义数据库链接信息
			String dbDriver = ((TextParam) handle.getProcessorInnerParam("driverClass")).getValue();
			String dbUrl = ((TextParam) handle.getProcessorInnerParam("url")).getValue();
			String dbUsr = ((TextParam) handle.getProcessorInnerParam("user")).getValue();
			String dbPwd = ((TextParam) handle.getProcessorInnerParam("password")).getValue();

			C3P0DataSource DBSource = C3P0DataSource.build(dbDriver, dbUrl, dbUsr, dbPwd, 1, 1, 10, 1, 2800);
			DataManager dataManager = new DataManager();
			dataManager.setLocalSource(DBSource);

			handle.setAttribute("dataManager", dataManager);

			TimedCallMonitor monitor = new TimedCallMonitor(handle, intervalTime);
			monitor.startup();
			handle.setAttribute("monitor", monitor);

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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
