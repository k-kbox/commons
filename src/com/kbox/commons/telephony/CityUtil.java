package com.kbox.commons.telephony;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CityUtil {

	public static City getCity(String code) {
		if (code != null) {
			if (citys.containsKey(code)) {
				return citys.get(code);
			}
			else {
				if (code.length() == 3 &&
						citys.containsKey("0" + code)) {
					return citys.get("0" + code);
				}
			}
		}
		return UNKN;
	}
	
	public static City getCityByName(String name) {
		for (Entry<String, City> e : citys.entrySet())
			if (e.getValue().getName().equals(name))
				return e.getValue();
		return UNKN;
	}

	private static City UNKN = new City("UNKN", "UNKN", "UNKN");
	private static Map<String, City> citys;
	static {
		citys = new HashMap<String, City>();
		citys.put("010", new City("010", "北京", "bj"));
	}
	
}
