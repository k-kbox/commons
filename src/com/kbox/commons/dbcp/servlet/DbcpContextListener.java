package com.kbox.commons.dbcp.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 
 * @author jun.huang
 *
 * @example
 * &lt;listener&gt;<br/>
 * &nbsp;&nbsp;&lt;listener-class&gt;com.utils.dbcp.servlet.DbcpContextListener&lt;/listener-class&gt;<br/>
 * &lt;/listener&gt;<br/>
 * &lt;context-param&gt;<br/>
 * &nbsp;&nbsp;&lt;param-name&gt;dbcp&lt;/param-name&gt;<br/>
 * &nbsp;&nbsp;&lt;param-value&gt;WEB-INF/classes/dbcp.properties&lt;/param-value&gt;<br/>
 * &lt;/context-param&gt;
 * 
 */
public class DbcpContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		com.utils.dbcp.ConnectionManager.getInstance().release(null);
		org.apache.log4j.Logger.getLogger(this.getClass()).info("dbcp destoryed!");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
		
        logger.info("dbcp init...");
		
		String file = arg0.getServletContext().getInitParameter("dbcp");
		if (file == null || file.length() == 0)
		{
			file = "WEB-INF/classes/dbcp.properties";
		}
		String propertiesFile = arg0.getServletContext().getRealPath("/") + file;
		java.util.Properties props = new java.util.Properties();
		try {
			logger.info("dbcp properties: " + propertiesFile);
		    props.load(new java.io.FileInputStream(propertiesFile));
		} catch (Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		com.utils.dbcp.ConnectionManager.setProperties(props);
		com.utils.dbcp.ConnectionManager.getInstance();
		
		logger.info("dbcp initialized.");
	}

}
