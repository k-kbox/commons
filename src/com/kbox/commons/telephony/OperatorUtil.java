package com.kbox.commons.telephony;


public class OperatorUtil {

	public static Operator CMCC;
	public static Operator UNICOM;
	public static Operator TELCOM;
	public static Operator UNKN;
	static {
		CMCC = new Operator("CMCC", "中国移动");
		UNICOM = new Operator("UNICOM", "中国联通");
		TELCOM = new Operator("TELCOM", "中国电信");
		UNKN = new Operator("UNKN", "UNKN");
	}
	
	public static Operator getOperator(String operator) {
		if (operator.equals(CMCC.getCode())) {
			return CMCC;
		}
		else if (operator.equals(UNICOM.getCode())) {
			return UNICOM;
		}
		else if (operator.equals(TELCOM.getCode())) {
			return TELCOM;
		}
		return UNKN;
	}
	
}
