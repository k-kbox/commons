package com.kbox.commons.telephony;

public class City {

	private String code;
	private String name;
	private String province;
	public City(String code, String name, String province) {
		super();
		this.code = code;
		this.name = name;
		this.province = province;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
}
