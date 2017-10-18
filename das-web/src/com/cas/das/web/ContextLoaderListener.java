package com.cas.das.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.jlrnt.web.WebContext;

import org.springframework.web.context.WebApplicationContext;

import com.cas.das.core.BeanFactory;
import com.cas.das.core.DasServiceContext;

/**
 * spring web 上下文监听器扩展
 * 
 * @author NiuLei
 */
public class ContextLoaderListener extends org.springframework.web.context.ContextLoaderListener {

	@Override
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		WebContext.buildContext(servletContext); // 获取WebApp路径;
		DasServiceContext.getContext().init(); // 初始化数据接入上下文;
		return super.initWebApplicationContext(servletContext);
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		WebApplicationContext webApplicationContext = getCurrentWebApplicationContext();
		BeanFactory.setWebApplicationContext(webApplicationContext);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		super.contextDestroyed(event);
		DasServiceContext.getContext().destroy();
	}

}
