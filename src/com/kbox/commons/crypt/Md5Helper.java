package com.kbox.commons.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Helper {

	protected static MessageDigest messageDigest = null;  
    static{
        try{  
            messageDigest = MessageDigest.getInstance("MD5");  
        }catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        }  
    }  
        
	public static String getMd5(File file) {
		String md5 = "";
		if (file.exists()) {
			try {
				FileInputStream in = new FileInputStream(file);  
	            FileChannel ch = in.getChannel();  
	            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());  
	            messageDigest.update(byteBuffer); 
	            in.close();
	            return bufferToHex(messageDigest.digest());  
			}
			catch (Exception e) {
				
			}
		}
		return md5;
	}
	
	private static String bufferToHex(byte[] bytes) {
		StringBuffer hex = new StringBuffer();
		for (byte b : bytes) {
			String h = Integer.toHexString(b & 0xFF);
			if (h.length() == 1)
				hex.append("0");
			hex.append(h);
		}
		return hex.toString();
	}
}
