package com.cas.das.core;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import net.jlrnt.dbc.DBSource;

/**
 * 数据源实现类(C3P0连接池方式)
 * 
 * @author NiuLei
 */
public class C3P0DataSource extends DBSource {

	/**
	 * C3P0连接池
	 */
	private ComboPooledDataSource cpds = new ComboPooledDataSource();

	/**
	 * 构造
	 * 
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 * @param initialPoolSize
	 * @param minPoolSize
	 * @param maxPoolSize
	 * @param acquireIncrement
	 * @param idleConnectionTestPeriod
	 */
	private C3P0DataSource(String driver, String url, String user, String password, int initialPoolSize,
			int minPoolSize, int maxPoolSize, int acquireIncrement, int idleConnectionTestPeriod) throws Exception {
		cpds.setDriverClass(driver);
		cpds.setJdbcUrl(url);
		cpds.setUser(user);
		cpds.setPassword(password);

		cpds.setInitialPoolSize(initialPoolSize);
		cpds.setMinPoolSize(minPoolSize);
		cpds.setMaxPoolSize(maxPoolSize);
		cpds.setAcquireIncrement(acquireIncrement);
		cpds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return cpds.getConnection();
	}

	@Override
	public void destroy() throws SQLException {
		cpds.close();
	}

	/**
	 * 构造C3P0数据源
	 * 
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 * @param initialPoolSize
	 * @param minPoolSize
	 * @param maxPoolSize
	 * @param acquireIncrement
	 * @param idleConnectionTestPeriod
	 * @return
	 */
	public static C3P0DataSource build(String driver, String url, String user, String password, int initialPoolSize,
			int minPoolSize, int maxPoolSize, int acquireIncrement, int idleConnectionTestPeriod) {
		C3P0DataSource dataSource = null;
		try {
			dataSource = new C3P0DataSource(driver, url, user, password, initialPoolSize, minPoolSize, maxPoolSize,
					acquireIncrement, idleConnectionTestPeriod);
		} catch (Exception e) {
			System.out.println("Error: can not create C3P0 data source.");
		}
		return dataSource;
	}

}
