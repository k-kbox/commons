package com.kbox.commons.telephony;

public class IMSIUtil {

	public String getOperator(String imsi) {
		String yys = "UNKN";
		if (imsi != null && imsi.length() > 0) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")
					|| imsi.startsWith("46007")) {
				yys = "中国移动";
			} else if (imsi.startsWith("46001")) {
				yys = "中国联通";
			} else if (imsi.startsWith("46003")) {
				yys = "中国电信";
			} else {
				// 默认是中国移动
				yys = "中国移动";
			}
		} else {
			// 默认是中国移动
			yys = "中国移动";
		}
		return yys;
	}
	
}
