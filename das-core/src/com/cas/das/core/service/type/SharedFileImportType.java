package com.cas.das.core.service.type;

import java.io.File;

import com.cas.das.core.service.ext.TimedCallMonitor;
import com.cas.platform.service.ServiceHandle;
import com.cas.platform.service.ServiceType;
import com.cas.platform.service.def.param.TextParam;

/**
 * 共享目录高能雷达导入
 * 
 * @author NiuLei
 */
public class SharedFileImportType extends ServiceType {

	@Override
	public boolean startupService(ServiceHandle handle) {
		TextParam monitorDirParam = (TextParam) handle.getServiceTypeParam("monitorDir");
		String monitorDir = monitorDirParam.getValue();
		TextParam intervalTimeParam = (TextParam) handle.getServiceTypeParam("intervalTime");
		String intervalTime = intervalTimeParam.getValue();
		try {
			// 定义监测目录
			File directory = new File(monitorDir);
			//Path path = Paths.get(directory.toURI());
			handle.setAttribute("directory", directory);

			// 定义监测类型
			TimedCallMonitor monitor = new TimedCallMonitor(handle,Integer.parseInt(intervalTime));
			monitor.startup();

			handle.setAttribute("monitor", monitor);
		} catch (Exception e) {
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
