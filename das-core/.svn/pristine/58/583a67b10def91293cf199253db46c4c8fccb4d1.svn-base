package com.cas.das.core;

import org.springframework.web.context.WebApplicationContext;

public class BeanFactory {

	private static WebApplicationContext webApplicationContext;

	public static void setWebApplicationContext(WebApplicationContext webApplicationContext) {
		BeanFactory.webApplicationContext = webApplicationContext;
	}

	public static Object getBean(String beanName) {
		return webApplicationContext.getBean(beanName);
	}

	public static <T> T getBean(Class<T> clazz) {
		return webApplicationContext.getBean(clazz);
	}

}
