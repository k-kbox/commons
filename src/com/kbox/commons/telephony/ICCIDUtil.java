package com.kbox.commons.telephony;

import java.util.HashMap;
import java.util.Map;

public class ICCIDUtil {

	public static Operator getOperator(String iccid) {
		if (iccid != null && iccid.length() >= 20) {
			if (iccid.startsWith("898600")) {
				return OperatorUtil.CMCC;
			}
			else if (iccid.startsWith("898601")) {
				return OperatorUtil.UNICOM;
			}
			else if (iccid.startsWith("898603")) {
				return OperatorUtil.TELCOM;
			}
		}
		return OperatorUtil.UNKN;
	}

	public static Province getProvince(String iccid) {
		String p = ProvinceUtil.UNKN.getCode();
		if (iccid != null && iccid.length() >= 20) {
			if (iccid.startsWith("898600")) {
				String pn = iccid.substring(8, 10);
				Map<String, String> map = provinces.get(iccid.substring(0, 6));
				if (map != null) p = map.get(pn);
			}
			else if (iccid.startsWith("898601")) {
				String pn = iccid.substring(9, 11);
				Map<String, String> map = provinces.get(iccid.substring(0, 6));
				if (map != null) p = map.get(pn);
			}
			else if (iccid.startsWith("898603")) {
				return ProvinceUtil.getProvince(CityUtil.getCity(iccid.substring(9, 12)).getProvince());
			}
		}
		if (p == null || p.length() <= 0) {
			p = ProvinceUtil.UNKN.getCode();
		}
		return ProvinceUtil.getProvinceByName(p);
	}

	private static Map<String, Map<String, String>> provinces;
	static {
		provinces = new HashMap<String, Map<String,String>>();
		{
			// 01：北京 02：天津 03：河北 04：山西 05：内蒙古 06：辽宁 07：吉林 08：黑龙江
			// 　　09：上海 l0：江苏 11：浙江 12：安徽 13：福建 14：江西 15：山东 16：河南
			// 　　17：湖北 18：湖南 19：广东 20：广西 21：海南 22：四川 23：贵州 24：云南
			// 　　25：西藏 26：陕西 27：甘肃 28：青海 29：宁夏 30：新疆 31：重庆
			Map<String, String>	map  = new HashMap<String, String>();
			map.put("01", "北京");
			map.put("02", "天津");
			map.put("03", "河北");
			map.put("04", "山西");
			map.put("05", "内蒙古");
			map.put("06", "辽宁");
			map.put("07", "吉林");
			map.put("08", "黑龙江");
			map.put("09", "上海");
			map.put("10", "江苏");
			map.put("11", "浙江");
			map.put("12", "安徽");
			map.put("13", "福建");
			map.put("14", "江西");
			map.put("15", "山东");
			map.put("16", "河南");
			map.put("17", "湖北");
			map.put("18", "湖南");
			map.put("19", "广东");
			map.put("20", "广西");
			map.put("21", "海南");
			map.put("22", "四川");
			map.put("23", "贵州");
			map.put("24", "云南");
			map.put("25", "西藏");
			map.put("26", "陕西");
			map.put("27", "甘肃");
			map.put("28", "青海");
			map.put("29", "宁夏");
			map.put("30", "新疆");
			map.put("31", "重庆");
			provinces.put("898600", map);
		}
		{
			//10内蒙古 11北京 13天津 17山东 18河北 19山西 30安徽 31上海 34江苏
			//36浙江 38福建 50海南 51广东 59广西 70青海 71湖北 74湖南 75江西
			//76河南 79西藏 81四川 83重庆 84陕西 85贵州 86云南 87甘肃 88宁夏
			//89新疆 90吉林 91辽宁 97黑龙江
			Map<String, String>	map  = new HashMap<String, String>();
			map.put("11", "北京");
			map.put("13", "天津");
			map.put("18", "河北");
			map.put("19", "山西");
			map.put("10", "内蒙古");
			map.put("91", "辽宁");
			map.put("90", "吉林");
			map.put("97", "黑龙江");
			map.put("31", "上海");
			map.put("34", "江苏");
			map.put("36", "浙江");
			map.put("30", "安徽");
			map.put("38", "福建");
			map.put("75", "江西");
			map.put("17", "山东");
			map.put("76", "河南");
			map.put("71", "湖北");
			map.put("74", "湖南");
			map.put("51", "广东");
			map.put("59", "广西");
			map.put("50", "海南");
			map.put("81", "四川");
			map.put("85", "贵州");
			map.put("86", "云南");
			map.put("79", "西藏");
			map.put("84", "陕西");
			map.put("87", "甘肃");
			map.put("70", "青海");
			map.put("88", "宁夏");
			map.put("89", "新疆");
			map.put("83", "重庆");
			provinces.put("898601", map);
		}
	}

}
