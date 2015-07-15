package com.kbox.commons.network;

import javax.servlet.http.HttpServletRequest;

import com.kbox.commons.telephony.Province;
import com.kbox.commons.telephony.ProvinceUtil;

public class IPUtil {
	public static String getIpAddr(HttpServletRequest request) {

		String ip = request.getHeader("x-forwarded-for");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		return ip;
	}

	public static Province getProvince(String ip) {
		return ProvinceUtil.UNKN;
	}
}