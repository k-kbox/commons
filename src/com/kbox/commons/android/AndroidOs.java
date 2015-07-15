package com.kbox.commons.android;

import java.util.HashMap;
import java.util.Map;

public class AndroidOs {

	public static String getOS(String sdk) {
		if (os.containsKey(sdk)) {
			return os.get(sdk);
		}
		return os.get("UNKN");
	}
	
	private static Map<String, String> os;
	static {
		os = new HashMap<String, String>();
		os.put("3", "Android 1.5");
		os.put("4", "Android 1.6");
		os.put("7", "Android 2.1");
		os.put("8", "Android 2.2");
		os.put("9", "Android 2.3.1");
		os.put("10", "Android 2.3.3");
		os.put("11", "Android 3.0");
		os.put("12", "Android 3.1");
		os.put("13", "Android 3.2");
		os.put("14", "Android 4.0");
		os.put("15", "Android 4.0.3");
		os.put("16", "Android 4.1.2");
		os.put("17", "Android 4.2.2");
		os.put("18", "Android 4.3.1");
		os.put("19", "Android 4.4.2");
		os.put("20", "Android 4.4W.2");
		os.put("21", "Android 5.0.1");
		os.put("22", "Android 5.1.1");
		
		os.put("UNKN", "UNKN");
	}
}
