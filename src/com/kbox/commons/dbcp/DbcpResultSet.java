package com.kbox.commons.dbcp;

/**
 * 
 * @author jun.huang
 * 
 */

public class DbcpResultSet {
	public DbcpResultSet() {
	}

	public void close() {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {

		}

		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (Exception e) {

		}
	}

	public java.sql.ResultSet getRs() {
		return rs;
	}

	public void setRs(java.sql.ResultSet rs) {
		this.rs = rs;
	}

	public java.sql.Statement getStmt() {
		return stmt;
	}

	public void setStmt(java.sql.Statement stmt) {
		this.stmt = stmt;
	}

	private java.sql.ResultSet rs;
	private java.sql.Statement stmt;
}
