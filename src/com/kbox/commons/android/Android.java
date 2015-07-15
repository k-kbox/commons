package com.kbox.commons.android;

public class Android {
	private String android_id = "";
	private String model = "";
	private String sdk_int = "0";
	private String sdk = "0";
	private String manufacturer = "";
	private String brand = "";
	private String board = "";
	private String product = "";
	private String device = "";
	
	public String getAndroidOs() {
		return AndroidOs.getOS(sdk);
	}
	
	public String getAndroidDevice() {
		if (model.length() > brand.length()
				&& model.substring(0, brand.length()).equalsIgnoreCase(brand)){
			return model;
		}
		else {
			return brand + " " + model;
		}
	}
	
	public String getAndroid_id() {
		return android_id;
	}
	public String getModel() {
		return model;
	}
	public String getSdk_int() {
		return sdk_int;
	}
	public String getSdk() {
		return sdk;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public String getBrand() {
		return brand;
	}
	public String getBoard() {
		return board;
	}
	public String getProduct() {
		return product;
	}
	public String getDevice() {
		return device;
	}
}