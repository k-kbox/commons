/**
 * 
 */
package com.kbox.commons.telephony;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.utils.string.StringUtils;

/**
 * @author jun.huang
 *
 */
public class ProvinceUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
//	private static final String[] provinceCode = {
//		"ah", "bj", "cq", "fj", "gd", "gs", "gx", "gz", "hb", 
//		"heb", "hen", "hlj", "hn", "hun", "jl", "js", "jx", "ln", 
//		"nm", "nx", "qh", "sc", "sd", "sh", "shx", "sx", "tj", 
//		"xj", "xz", "yn", "zj", "UNKN"
//	};
//	
//	private static final String[] provinceName = {
//		"安徽", "北京", "重庆", "福建", "广东", "甘肃", "广西", "贵州", "湖北", 
//		"河北", "河南", "黑龙江", "海南", "湖南", "吉林", "江苏", "江西", "辽宁", 
//		"内蒙古", "宁夏", "青海", "四川", "山东", "上海", "陕西", "山西", "天津", 
//		"新疆", "西藏", "云南", "浙江", "UNKN"
//	};

	/**
	 * 
	 * @return
	 */
	public static List<Province> getProvince()
	{
		List<Province> l = new ArrayList<Province>();
		for (Entry<String, Province> e : provinces.entrySet())
			l.add(new Province(e.getValue()));
		return l;
	}
	
	/**
	 * 
	 * @param provinceId
	 * @return
	 */
	public static Province getProvince(Integer id)
	{
		if (provinces.containsKey(id))
			return new Province(provinces.get(id));
		return null;
	}
	
	/**
	 * 
	 * @param provinceCode
	 * @return
	 */
	public static Province getProvince(String code)
	{
		for (Entry<String, Province> e : provinces.entrySet())
			if (e.getValue().getCode().equals(code))
				return new Province(e.getValue());
		return null;
	}
	public static Province getProvinceByName(String provinceName)
	{
		for (Entry<String, Province> e : provinces.entrySet())
			if (e.getValue().getName().equals(provinceName))
				return e.getValue();
		return UNKN;
	}

	private static final String[][] province; 
	private static final Map<String, Province> provinces; 
	public static final Province UNKN;
	static {
		province = new String[][] {
				{"sx", "山西"},
				{"fj", "福建"},
				{"hn", "海南"},
				{"qh", "青海"},
				{"ln", "辽宁"},
				{"shx", "陕西"},
				{"tj", "天津"},
				{"gd", "广东"},
				{"ah", "安徽"},
				{"zj", "浙江"},
				{"xj", "新疆"},
				{"js", "江苏"},
				{"sh", "上海"},
				{"nm", "内蒙古"},
				{"sd", "山东"},
				{"gz", "贵州"},
				{"xz", "西藏"},
				{"gs", "甘肃"},
				{"heb", "河北"},
				{"hb", "湖北"},
				{"hen", "河南"},
				{"jl", "吉林"},
				{"hlj", "黑龙江"},
				{"hun", "湖南"},
				{"yn", "云南"},
				{"gx", "广西"},
				{"nx", "宁夏"},
				{"cq", "重庆"},
				{"bj", "北京"},
				{"jx", "江西"},
				{"sc", "四川"},
				{"UNKN", "UNKN"}
			};

		provinces = new LinkedHashMap<String, Province>();
		for (int n = 0; n < province.length; n++)
		{
			provinces.put(province[n][0], 
					new Province(StringUtils.fromInt(n + 1), province[n][0], province[n][1]));
		}

		UNKN = provinces.get("UNKN");
	}

	
}
