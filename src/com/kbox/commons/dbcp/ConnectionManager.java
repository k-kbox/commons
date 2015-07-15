/**
 * 
 */
package com.kbox.commons.dbcp;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * @author jun.huang
 * 
 */
public class ConnectionManager {
	static private ConnectionManager instance = null;

	private Logger logger = Logger.getLogger("com.dbcp.manager");

	static public final String MYSQL = "mysql";
	static public final String ORACLE = "oracle";

	Map<String, ConnectionPool> pools = null;
	Map<String, ConnectionConfig> configs = null;
	Map<String, Driver> drivers = null;

	private static java.util.Properties properties;

	static synchronized public ConnectionManager getInstance() {
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	public static void setProperties(java.util.Properties props) {
		properties = props;
	}

	protected ConnectionManager() {
		try {
			pools = new HashMap<String, ConnectionPool>();
			configs = new HashMap<String, ConnectionConfig>();
			drivers = new HashMap<String, Driver>();

			if (properties == null) {
				properties = new java.util.Properties();
				properties.load(new java.io.FileInputStream(this.getClass()
						.getResource("/dbcp.properties").getFile()));
			}
			
			String dbpool = properties.getProperty("dbcp.pool");
			if (dbpool != null && dbpool.length() > 0) {
				String[] dbpools = dbpool.split(",");
				for (String pn : dbpools) {
					if (pn != null && pn.length() > 0) {
						ConnectionConfig cfg = new ConnectionConfig();
						cfg.setType(properties.getProperty("dbcp.pool.type."
								+ pn));
						cfg.setMaxConn(Integer.parseInt(properties
								.getProperty("dbcp.pool.maxConnect." + pn)));
						cfg.setInitConn(Integer.parseInt(properties
								.getProperty("dbcp.pool.normalConnect." + pn)));
						cfg.setDriverName(properties
								.getProperty("dbcp.pool.driver." + pn));
						cfg.setUrl(properties
								.getProperty("dbcp.pool.url." + pn));
						cfg.setUser(properties.getProperty("dbcp.pool.user."
								+ pn));
						cfg.setPassword(properties
								.getProperty("dbcp.pool.password." + pn));
						configs.put(pn, cfg);
					}
				}
			}
			loadDrivers();
			createPools();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	private void loadDrivers() {
		for (Entry<String, ConnectionConfig> cfg : configs.entrySet()) {
			try {
				Driver driver = (Driver) Class.forName(
						cfg.getValue().getDriverName()).newInstance();
				DriverManager.registerDriver(driver);
				logger.info("load jdbc driver success! "
						+ cfg.getValue().getDriverName());
			} catch (Exception e) {
				logger.error("cannot load jdbc driver: "
						+ cfg.getValue().getDriverName() + ", error:" + e);
			}
		}
	}

	private void createPools() {
		for (Entry<String, ConnectionConfig> cfg : configs.entrySet()) {
			ConnectionPool pool = new ConnectionPool(cfg.getValue().getUrl(),
					cfg.getValue().getUser(), cfg.getValue().getPassword(), cfg
							.getValue().getMaxConn(), cfg.getValue()
							.getInitConn());
			if (pool != null) {
				pools.put(cfg.getKey(), pool);
				logger.info("create connecton pool [" + cfg.getKey()
						+ "] success!");
			}
		}
	}

	private Connection getConnection(String pool) {
		if (pools.get(pool) != null) {
			return pools.get(pool).getConnection();
		}
		return null;
	}

	// private Connection getConnection(long time) {
	// if (pool != null) {
	// return pool.getConnection(time);
	// }
	// return null;
	// }

	private void freeConnection(String pool, Connection con) {
		if (pools.get(pool) != null) {
			pools.get(pool).freeConnection(con);
		}
	}

	public String getType(String pool) {
		if (configs.containsKey(pool))
			return configs.get(pool).getType();
		else
			return "";
	}

	public int getnum(String pool) {
		return pools.get(pool).getNumIdle();
	}

	public int getnumActive(String pool) {
		return pools.get(pool).getNumActive();
	}

	public String getPoolStatus(String pool) {
		String ret = "";
		if (pool != null && pool.length() > 0)
			ret = "pool name: " + pool + "\r\n"
					+ pools.get(pool).getPoolStatus();
		else {
			for (Entry<String, ConnectionPool> p : pools.entrySet()) {
				ret += "pool name: " + p.getKey() + "\r\n"
						+ p.getValue().getPoolStatus();
				ret += " \r\n";
			}
		}
		return ret;
	}

	public synchronized void release(String pool) {
		if (pool != null && pool.length() > 0) {
			pools.get(pool).release();
			try {
				DriverManager.deregisterDriver(drivers.get(pool));
				logger.info("relase jdbc driver success! "
						+ drivers.get(pool).getClass().getName());
			} catch (SQLException e) {
				logger.error("cannot relase jdbc driver:"
						+ drivers.get(pool).getClass().getName());
			}
		} else {
			for (Entry<String, Driver> driver : drivers.entrySet()) {
				pools.get(driver.getKey()).release();
				try {
					DriverManager.deregisterDriver(driver.getValue());
					logger.info("relase jdbc driver success! "
							+ driver.getValue().getClass().getName());
				} catch (SQLException e) {
					logger.error("cannot relase jdbc driver:"
							+ driver.getValue().getClass().getName());
				}
			}
		}
	}

	public boolean execute(String pool, String sql) {
		boolean result = false;
		
		logger.debug("pool:" + pool + ";sql:" + sql);
		
		Connection con = getConnection(pool);
		Statement stmt = null;
		if (con == null) {
			logger.error("pool:" + pool
					+ ";execute: No enough connection exist!");
			logger.error("pool:" + pool + ";sql:" + sql);
			return false;
		}

		try {
			stmt = con.createStatement();
			result = stmt.execute(sql);
			result = true;
		} catch (SQLException e) {
			logger.error("pool:" + pool + ";execute:" + e.getMessage());
			logger.error("pool:" + pool + ";sql:" + sql);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
		freeConnection(pool, con);
		return result;
	}

	public boolean executeBatch(String pool, String[] sql) {
		boolean result = false;

		for (int n = 0; n < sql.length; n++)
			logger.debug("pool:" + pool + ";sql:" + sql[n]);
		
		Connection con = getConnection(pool);
		Statement stmt = null;
		boolean autoCommit = false;

		if (con == null) {
			logger.error("pool:" + pool
					+ ";executeBatch: No enough connection exist!");
			for (int n = 0; n < sql.length; n++)
				logger.error("pool:" + pool + ";sql:" + sql[n]);
			return false;
		}

		try {
			autoCommit = con.getAutoCommit();
			con.setAutoCommit(false);
			stmt = con.createStatement();
			for (int n = 0; n < sql.length; n++) {
				if (sql[n] != null && sql[n].length() > 0) {
					stmt.addBatch(sql[n]);
				}
			}
			stmt.executeBatch();
			con.commit();
			result = true;
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException ex) {
				logger.error("pool:" + pool + ";executeBatch:"
						+ ex.getMessage());
			}
			logger.error("pool:" + pool + ";executeBatch:" + e.getMessage());
			for (int n = 0; n < sql.length; n++)
				logger.error("pool:" + pool + ";sql:" + sql[n]);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.setAutoCommit(autoCommit);
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}

		freeConnection(pool, con);
		return result;
	}

	public DbcpResultSet executeQuery(String pool, String sql) {
		DbcpResultSet result = null;
		
		logger.debug("pool:" + pool + ";sql:" + sql);
		
		Connection con = getConnection(pool);
		if (con == null) {
			logger.error("pool:" + pool
					+ ";executeQuery: No enough connection exist!");
			logger.error("pool:" + pool + ";sql:" + sql);
			return null;
		}

		try {
			Statement stmt = con.createStatement();
			result = new DbcpResultSet();
			result.setStmt(stmt);
			result.setRs(stmt.executeQuery(sql));
		} catch (SQLException e) {
			logger.error("pool:" + pool + ";executeQuery:" + e.getMessage());
			logger.error("pool:" + pool + ";sql:" + sql);
		}
		freeConnection(pool, con);
		return result;
	}

	public boolean executeUpdate(String pool, String sql) {
		boolean result = false;
		
		logger.debug("pool:" + pool + ";sql:" + sql);
		
		Connection con = getConnection(pool);
		if (con == null) {
			logger.error("pool:" + pool
					+ ";executeUpdate: No enough connection exist!");
			logger.error("pool:" + pool + ";sql:" + sql);
			return false;
		}

		try {
			Statement stmt = con.createStatement();
			result = stmt.execute(sql);
			stmt.close();
			result = true;
		} catch (SQLException e) {
			logger.error("pool:" + pool + ";executeUpdate:" + e.getMessage());
			logger.error("pool:" + pool + ";sql:" + sql);
		}
		freeConnection(pool, con);
		return result;
	}
}
