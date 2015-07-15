package com.kbox.commons.dbcp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author jun.huang
 * 
 */
public class ModelGenerator<T> {
	private Class<?> c;

	private Logger logger = Logger.getLogger("com.dbcp.generator");

	public ModelGenerator(Class<?> c) {
		this.c = c;
	}

	public T generate(String pool, String sql) {
		T t = null;
		DbcpResultSet dbrs = ConnectionManager.getInstance().executeQuery(pool,
				sql);
		if (dbrs != null) {
			java.sql.ResultSet rs = dbrs.getRs();
			try {
				if (rs != null && rs.next()) {
					t = generate(rs);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				dbrs.close();
			}
		}
		return t;
	}

	public List<T> generateList(String pool, String sql) {
		List<T> list = null;
		DbcpResultSet dbrs = ConnectionManager.getInstance().executeQuery(pool,
				sql);
		if (dbrs != null) {
			list = new ArrayList<T>();
			java.sql.ResultSet rs = dbrs.getRs();
			try {
				while (rs != null && rs.next()) {
					T t = generate(rs);
					if (t != null) {
						list.add(t);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				dbrs.close();
			}
		}
		return list;
	}

	// @SuppressWarnings(value = { "unchecked" })
	@SuppressWarnings("unchecked")
	private T generate(java.sql.ResultSet rs) {
		T t = null;
		try {
			if (rs != null) {
				// rs.getMetaData()
				if (c.equals(String.class)) {
					t = (T) rs.getString(1);
				} else if (c.equals(Integer.class)) {
					t = (T) Integer.valueOf(rs.getInt(1));
				} else if (c.equals(Date.class)) {
					t = (T) rs.getTimestamp(1);
				} else {
					t = (T) c.newInstance();
					Field[] fd = t.getClass().getDeclaredFields();
					String fdtype = "";
					String fdname = "";

					for (int n = 0; n < fd.length; n++) {
						fdtype = fd[n].getGenericType().toString();
						// System.out.println("type:" + fdtype);
						fdname = fd[n].getName();
						// System.out.println("name:" + fdname);
						if (fdname.equals("serialVersionUID")
								|| fdname.equals("table")) {
							continue;
						}
						Method md = t.getClass().getMethod(
								"set" + fdname.substring(0, 1).toUpperCase()
										+ fdname.substring(1),
								new Class[] { fd[n].getType() });

						try {
							if (fdtype.equals("int")
									|| fdtype.equals("class java.lang.Integer")) {
								// logger.debug("int or Integer");
								md.invoke(t, rs.getInt(fdname));
								// fd[n].set(t, rs.getInt(fdname));
							} else if (fdtype.equals("class java.lang.String")) {
								// logger.debug("String");
								/**
								ResultSetMetaData rsmd = rs.getMetaData();
								int nc = 1;
								for (nc = 1; nc <= rsmd.getColumnCount(); nc++)
								{
									//System.out.println("column " + nc + " name " + rsmd.getColumnName(nc));
									if (rsmd.getColumnName(nc).equalsIgnoreCase(fdname))
									{
										break;
									}
								}
								if (nc > rsmd.getColumnCount())
									continue;
								
								//System.out.println("column " + nc + " name is [" +fdname + "] type is [" + rsmd.getColumnTypeName(nc) + "]");
								if (rsmd.getColumnTypeName(nc).equalsIgnoreCase("NUMBER"))
								{
									md.invoke(t, String.valueOf(rs.getFloat(fdname)));
								}
								else /**/
								{
									md.invoke(
											t,
											rs.getString(fdname) != null ? rs
													.getString(fdname) : "");
								}
							} else if (fdtype.equals("float")
									|| fdtype.equals("class java.lang.Float")) {
								// logger.debug("float or Float");
								md.invoke(t, rs.getFloat(fdname));
							} else if (fdtype.equals("class java.util.Date")) {
								// logger.debug("Date");
								md.invoke(t, rs.getTimestamp(fdname));
								// logger.info(fdname + ":" +
								// rs.getTimestamp(fdname));
							} else {
								// logger.debug("else");
								md.invoke(t, rs.getString(fdname));
							}
						} catch (Exception e) {
							logger.error(fdname + ":" + e.getMessage());
							// md.invoke(t, fd[n].getGenericType());
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return t;
	}

	public String insertSQL(T t) {
		String strSQL = "";
		String strtable = "";
		String strfields = "";
		String strvalues = "";

		Field[] fd = t.getClass().getDeclaredFields();
		String fdtype = "";
		String fdname = "";
		try {
			Method md1 = t.getClass().getMethod("getTable");
			strtable = (String) md1.invoke(t);

			for (int n = 0; n < fd.length; n++) {
				fdtype = fd[n].getGenericType().toString();
				// System.out.println("type:" + fdtype);
				fdname = fd[n].getName();
				// System.out.println("name:" + fdname);
				if (fdname.equals("serialVersionUID") || fdname.equals("table")) {
					continue;
				}

				if (strfields.equals("")) {
					strfields += fdname;
				} else {
					strfields += "," + fdname;
				}

				Method md = t.getClass().getMethod(
						"get" + fdname.substring(0, 1).toUpperCase()
								+ fdname.substring(1));

				// System.out.println(md.invoke(t));

				if (fdtype.equals("int")
						|| fdtype.equals("class java.lang.Integer")) {
					Integer nf = (Integer) md.invoke(t);
					// System.out.println(nf);
					if (strvalues.equals("")) {
						strvalues += nf.toString();
					} else {
						strvalues += "," + nf.toString();
					}
					// fd[n].set(t, rs.getInt(fdname));
				} else if (fdtype.equals("class java.lang.String")) {
					String sf = (String) md.invoke(t);
					if (strvalues.equals("")) {
						strvalues += "'" + sf + "'";
					} else {
						strvalues += ",'" + sf + "'";
					}
				} else if (fdtype.equals("float")
						|| fdtype.equals("class java.lang.Float")) {
					Float ff = (Float) md.invoke(t);
					if (strvalues.equals("")) {
						strvalues += ff.toString();
					} else {
						strvalues += "," + ff.toString();
					}
				} else if (fdtype.equals("class java.util.Date")) {
					// System.out.println("date");
					Date df = (Date) md.invoke(t);
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					if (strvalues.equals("")) {
						strvalues += "to_date('" + sdf.format(df)
								+ "','yyyy-mm-dd hh24:mi:ss')";
					} else {
						strvalues += ",to_date('" + sdf.format(df)
								+ "','yyyy-mm-dd hh24:mi:ss')";
					}
				} else {
					String sf = (String) md.invoke(t);
					if (strvalues.equals("")) {
						strvalues += "'" + sf + "'";
					} else {
						strvalues += ",'" + sf + "'";
					}
				}
			}
			strSQL = "insert into " + strtable + "(" + strfields + ") values("
					+ strvalues + ")";
		} catch (Exception e) {
			System.out.println(e);
		}
		// System.out.println(strSQL);
		// logger.info("insert:" + strSQL);
		return strSQL;
	}

	public String deleteSQL(T t, String strcondition) {
		String strSQL = "";
		try {
			Method md = t.getClass().getMethod("getTable");
			String strtable = (String) md.invoke(t);

			strSQL = "delete from " + strtable;
			if (strcondition != null && !strcondition.equals("")) {
				strSQL += " where " + strcondition;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return strSQL;
	}

	public String updateSQL(T t, String strcondition) {
		String strSQL = "";
		String strtable = "";
		String strset = "";

		Field[] fd = t.getClass().getDeclaredFields();
		String fdtype = "";
		String fdname = "";
		try {
			Method md1 = t.getClass().getMethod("getTable");
			strtable = (String) md1.invoke(t);

			for (int n = 0; n < fd.length; n++) {
				fdtype = fd[n].getGenericType().toString();
				// System.out.println("type:" + fdtype);
				fdname = fd[n].getName();
				// System.out.println("name:" + fdname);
				if (fdname.equals("serialVersionUID") || fdname.equals("table")) {
					continue;
				}

				if (strset.equals("")) {
					strset += fdname;
				} else {
					strset += "," + fdname;
				}

				Method md = t.getClass().getMethod(
						"get" + fdname.substring(0, 1).toUpperCase()
								+ fdname.substring(1));

				// System.out.println(md.invoke(t));

				if (fdtype.equals("int")
						|| fdtype.equals("class java.lang.Integer")) {
					Integer nf = (Integer) md.invoke(t);
					// System.out.println(nf);
					strset += "=" + nf.toString();
					// fd[n].set(t, rs.getInt(fdname));
				} else if (fdtype.equals("class java.lang.String")) {
					String sf = (String) md.invoke(t);
					strset += "='" + sf + "'";
				} else if (fdtype.equals("float")
						|| fdtype.equals("class java.lang.Float")) {
					Float ff = (Float) md.invoke(t);
					strset += "=" + ff.toString();
				} else if (fdtype.equals("class java.util.Date")) {
					// System.out.println("date");
					Date df = (Date) md.invoke(t);
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					strset += "=to_date('" + sdf.format(df)
							+ "','yyyy-mm-dd hh24:mi:ss')";
				} else {
					String sf = (String) md.invoke(t);
					strset += "='" + sf + "'";
				}
			}
			strSQL = "update " + strtable + " set " + strset;
			if (strcondition != null && !strcondition.equals("")) {
				strSQL += " where " + strcondition;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return strSQL;
	}

	public String selectSQL(T t, String strcondition, String strsort) {
		String strSQL = "";
		try {
			Method md = t.getClass().getMethod("getTable");
			String strtable = (String) md.invoke(t);

			strSQL = "select * from " + strtable;

			if (strcondition != null && !strcondition.equals("")) {
				strSQL += " where " + strcondition;
			}

			if (strsort != null && !strsort.equals("")) {
				strSQL += " order by " + strsort;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		// System.out.println(strSQL);
		// logger.info("select:" + strSQL);
		return strSQL;
	}
}
