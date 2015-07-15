/**
 * 
 */
package com.kbox.commons.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigUtil {

	private final static Map<String, Properties> sections;
	static {
		sections = new HashMap<String, Properties>();
		try {
			String section = ".";
			sections.put(section, new Properties());
						
			File dir = new File(ConfigUtil.class.getResource("/").getFile());
			File[] files = dir.listFiles();
			for (int n = 0; n < files.length; n++) {
				if (files[n].getName().endsWith(".cfg")) {
					try {
						Logger.getLogger(ConfigUtil.class).info("[load][file: " + files[n].getPath() + "]");
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(new FileInputStream(files[n]), "UTF-8"));
						String line;
						while ((line = reader.readLine()) != null) {
							line = line.trim();
							
							if (line.length() == 0 || line.startsWith("#"))
								continue;
	
							if (line.matches("\\[.*\\]")) {
								String sec = line.replaceFirst("\\[(.*)\\]", "$1").trim();
								if (!section.equals(sec)) {
									section = sec;
									sections.put(section, new Properties());
								}
							} else if (line.matches(".*=.*")) {
								int i = line.indexOf('=');
								String name = line.substring(0, i).trim();
								String value = line.substring(i + 1).trim();
								sections.get(section).setProperty(name, value);
							}
						}
						reader.close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean containsKey(String section, String key) {
		return (sections.containsKey(section)
				&& sections.get(section).containsKey(key));
	}

	public static boolean containsKey(String key) {
		return containsKey(".", key);
	}
	
	public static String get(String section, String key)
	{
		if (section == null || section.length() == 0)
		{
			section = ".";
		}
		
		if (sections.containsKey(section))
		{
			if (sections.get(section).containsKey(key))
			{
				return sections.get(section).getProperty(key);
			}
			else
			{
				if (!section.equals(".") && sections.get(".").containsKey(key))
				{
					return sections.get(".").getProperty(key);
				}
			}
		}
		return null;
	}
	
	public static String get(String key)
	{
		if (sections.containsKey("."))
			return sections.get(".").getProperty(key);
		return null;
	}
	
	public static String get(String key, boolean checkOs) {
		return get(key + (checkOs ? getOsSuffix() : ""));
	}
	
	public static String get(String section, String key, boolean checkOs) {
		return get(section, key + (checkOs ? getOsSuffix() : ""));
	}
	
	private static String getOsSuffix() {
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("windows")) {
			// Windows XP, Vista ...
			return ".windows";
		} else {
			// Unix, Linux ...
			return ".linux";
		}
	}
}
