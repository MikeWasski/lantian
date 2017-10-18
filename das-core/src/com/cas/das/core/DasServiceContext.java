package com.cas.das.core;

import com.cas.platform.service.web.ServletServiceContext;

/**
 * 数据接入服务上下文
 * 
 * @author NiuLei
 */
public class DasServiceContext extends ServletServiceContext {

	/**
	 * 上下文实例
	 */
	private static DasServiceContext context = new DasServiceContext();

	/**
	 * 构造
	 */
	private DasServiceContext() {
	}

	/**
	 * 获取上下文实例
	 * 
	 * @return
	 */
	public static DasServiceContext getContext() {
		return context;
	}

	@Override
	public String getRelativeServiceSettingPath() {
		return "/cas-pe-setting/das/services";
	}

}
