/**
 * 
 */
package com.kbox.commons.dbcp;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author jun.huang
 * 
 */

// @SuppressWarnings("unchecked")
public class ConnectionPool {
	private Vector<Connection> freeConnections = new Vector<Connection>();
	private Map<String, Long> waitTime = new HashMap<String, Long>();
	private final long MAX_WAIT_TIME = 4 * 60 * 60 * 1000L;

	private String url;

	private String user;
	private String password;

	private int maxConn;
	private int initConn;

	private static int numCreate = 0;
	private static int numIdle = 0;
	private static int numActive = 0;

	private Logger logger = Logger.getLogger("com.dbcp.pool");

	public ConnectionPool() {

	}

	public ConnectionPool(String url, String user, String password,
			int maxConn, int normalConn) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.maxConn = maxConn;
		this.initConn = normalConn;

		for (int i = 0; i < normalConn; i++) {
			Connection c = newConnection();
			if (c != null) {
				// logger.info("create a new connection success!");
				freeConnections.addElement(c);
				waitTime.put(c.toString(), System.currentTimeMillis());
				numIdle++;
				numCreate++;
			}
		}
	}

	public synchronized void freeConnection(Connection con) {
		freeConnections.addElement(con);
		numIdle++;
		numActive--;
		notifyAll();
		logger.debug("free a connection:" + con);
	}

	public synchronized Connection getConnection() {
		Connection con = null;

		if (freeConnections.size() > 0) {
			numIdle--;

			con = (Connection) freeConnections.firstElement();
			freeConnections.removeElementAt(0);
			
			if (System.currentTimeMillis() - waitTime.get(con.toString()) >= MAX_WAIT_TIME)
			{
				try {
					con.close();
					logger.info("waittime to long, close connection, remove from pool!");
					numCreate--;
					waitTime.remove(con.toString());
					return getConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			else
				waitTime.put(con.toString(), System.currentTimeMillis());
			
			try {
				if (con.isClosed()) {
					logger.info("connection closed, remove from pool!");
					numCreate--;
					waitTime.remove(con.toString());
					return getConnection();
				}
			} catch (Exception e) {
				logger.error(e + ":remove a connection from pool!");
				numCreate--;
				waitTime.remove(con.toString());
				return getConnection();
			}
		} else if (maxConn == 0 || numCreate < maxConn) {
			con = newConnection();
			if (con != null) {
				numCreate++;
			}
		}
		
		numActive++;
		logger.debug("get a connection:" + con);
		return con;

	}

	public synchronized Connection getConnection(long timeout) {
		long startTime = new Date().getTime();
		Connection con;
		while ((con = getConnection()) == null) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {

			}

			if ((new Date().getTime() - startTime) >= timeout) {
				return null;
			}
		}
		return con;
	}

	public synchronized void release() {
		Enumeration<Connection> allConnections = freeConnections.elements();
		while (allConnections.hasMoreElements()) {
			Connection con = (Connection) allConnections.nextElement();
			try {
				con.close();
				numIdle--;
			} catch (SQLException e) {
				logger.error(e + ":cannot close the connection in pool!");
			}
		}
		freeConnections.removeAllElements();
		numActive = 0;
	}

	private Connection newConnection() {
		Connection con = null;
		try {
			Properties prop = new  Properties();
			if (user != null)
			{
	            prop.setProperty("user", user);
	            prop.setProperty("password", password);
			}
            prop.setProperty("oracle.jdbc.V8Compatible", "true");
            con = DriverManager.getConnection(url, prop);
            /*
			if (user == null) {
				con = DriverManager.getConnection(url);
			} else {
				con = DriverManager.getConnection(url, user, password);
			}
			*/
			logger.info("created a new connection:" + con);
			waitTime.put(con.toString(), System.currentTimeMillis());
			return con;
		} catch (SQLException e) {
			logger.info("cannot create a new connection [url:" + url + ";err:"
					+ e.getMessage() + "]");
		}
		return null;

	}

	public String getPoolStatus() {
		return "max: " + maxConn + "  " + "create: " + numCreate + "  "
				+ "idle: " + numIdle + "  " + "active: " + numActive + "\r\n";
	}

	public int getNumCreate() {
		return numCreate;
	}

	public int getNumIdle() {
		return numIdle;
	}

	public int getNumActive() {
		return numActive;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the maxConn
	 */
	public int getMaxConn() {
		return maxConn;
	}

	/**
	 * @param maxConn
	 *            the maxConn to set
	 */
	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}

	/**
	 * @return the initConn
	 */
	public int getInitConn() {
		return initConn;
	}

	/**
	 * @param initConn
	 *            the initConn to set
	 */
	public void setInitConn(int initConn) {
		this.initConn = initConn;
	}

}
