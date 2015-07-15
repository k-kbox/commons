package com.kbox.commons.fs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import com.kbox.commons.config.ConfigUtil;


public class FSUtil {

	public static void publish(FSInfo fs) {
		publish(fs, true);
	}
	
	public static void publish(FSInfo fs, boolean del) {
		try {
			File file = new File(fs.getPath());
			File dest = new File(ConfigUtil.get("upload.dir") + "/" + fs.getType().toString() + "/" + fs.getMd5() + fs.getExt());
			if (!dest.exists()) {
				dest.getParentFile().mkdirs();
			}
			if (!file.getPath().equals(dest.getPath())) {
				copy(file, dest);
				if (del) {
					file.delete();
				}
				fs.setPath(dest.getPath());
			}
			fs.setUrl(ConfigUtil.get("download.url") + "/" + fs.getType().toString() + "/" + fs.getMd5() + fs.getExt());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getDisplay(FSInfo fs) {
		try {
			BufferedImage img = ImageIO.read(new File(fs.getPath()));
			fs.setDisplay(img.getWidth() + "x" + img.getHeight());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return fs.getDisplay();
	}
	
	public static void copy(File file, File dest) {
		try {
			FileInputStream fis = new FileInputStream(file);
	        FileOutputStream fos = new FileOutputStream(dest);  
	        byte[] buffer = new byte[1024];  
	        int length;
	        while ((length = fis.read(buffer)) != -1) {  
	            fos.write(buffer, 0, length);  
	        }  
	        fis.close();
	        fos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
