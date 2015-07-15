package com.kbox.commons.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipHelper {

	public static byte[] zip(byte[] bytes) {
		if (bytes != null) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream gzip = new GZIPOutputStream(out);
				gzip.write(bytes);
				gzip.close();
				return out.toByteArray();
			}
			catch (Exception e) {
				
			}
		}
		return new byte[0];
	}
	
	public static byte[] unzip(byte[] bytes) {
		if (bytes != null) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
			    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			    GZIPInputStream gunzip = new GZIPInputStream(in);
			    byte[] buffer = new byte[256];
			    int n;
			    while ((n = gunzip.read(buffer)) >= 0) {
			      out.write(buffer, 0, n);
			    }
			    return out.toByteArray();
			}
			catch (Exception e) {
				
			}
		}
		return new byte[0];
	}
}
