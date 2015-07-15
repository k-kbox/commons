package com.kbox.commons.fs;

import java.io.File;

import com.kbox.commons.crypt.Md5Helper;


import net.sf.json.JSONObject;

public class FSInfo {

	private FSType type;
	private String path;
	private String name;
	private String ext;
	private long size;
	private String md5;
	private String display;
	private String url;

	public FSInfo() {
		
	}
	
	public FSInfo(FSType type, String path, String name) {
		this.type = type;
		this.path = path;
		File file = new File(path);
		this.name = name;
		this.ext = name.substring(name.lastIndexOf("."));
		this.size = file.length();
		this.md5 = Md5Helper.getMd5(file);
		this.display = "";
		this.url = "";
	}
	
//	public FSInfo(String path, String type, String name, String ext, 
//			long size, String md5, String display, String url) {
//		super();
//		this.path = path;
//		this.type = type;
//		this.name = name;
//		this.ext = ext;
//		this.size = size;
//		this.md5 = md5;
//		this.display = display;
//		this.url = url;
//	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public FSType getType() {
		return type;
	}

	public void setType(FSType type) {
		this.type = type;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toJsonString() {
		JSONObject obj = new JSONObject();
		obj.put("type", type.toString());
		obj.put("name", name);
		obj.put("size", size);
		obj.put("md5", md5);
		obj.put("display", display);
		obj.put("url", url);
		return obj.toString();
	}
}
