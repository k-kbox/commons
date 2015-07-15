package com.kbox.commons.exec;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class ExecUtil {

	private static Logger logger = Logger.getLogger(ExecUtil.class);
	
	public static String exec(String[] commands) {
		try {
			String charset = "utf-8";
			Runtime rt = Runtime.getRuntime();
			Process proc;
			String os = System.getProperty("os.name");
			if (os.toLowerCase().startsWith("windows")) {
				charset = "gbk";
				// Windows XP, Vista ...
				proc = rt.exec("cmd.exe");
			} else {
				// Unix, Linux ...
				charset = "utf-8";
				proc = rt.exec("/bin/sh");
			}
			// System.out.println(command[0] + " " + command[1] + " " + command[2]);
			DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
			for (String command : commands) {
				logger.info(command);
				if (command.toLowerCase().startsWith("cd ")
						&& os.toLowerCase().startsWith("windows")) {
					String tmp = command.substring(3).trim();
					int pos = tmp.indexOf(":");
					if (pos != -1) {
						String disk = tmp.substring(0, pos + 1);
						// System.out.println(disk);
						dos.writeBytes(disk + "\n");
						dos.flush();
					}
				}
				dos.writeBytes(command + "\n");
				dos.flush();
			}
			dos.writeBytes("exit \n");
			dos.flush();
			DataInputStream dis = new DataInputStream(proc.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(dis, charset));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				logger.info(line);
				sb.append(line + "\r\n");
			}
			br.close();
			dis.close();
			proc.waitFor();
			return sb.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String exec(String command) {
		try {
			String charset = "utf-8";
			Runtime rt = Runtime.getRuntime();
			String[] commands; //aaptPath + " d badging " + apk.getPath() + "\"";
			String os = System.getProperty("os.name");
			if (os.toLowerCase().startsWith("windows")) {
				// Windows XP, Vista ...
				charset = "gbk";
				commands = new String[] {
					"cmd.exe",
					"/C",
					command
				};
			} else {
				// Unix, Linux ...
				charset = "utf-8";
				commands = new String[] {
					"/bin/sh",
					"-c",
					command
				};
			}
			// System.out.println(command[0] + " " + command[1] + " " + command[2]);
			Process proc = rt.exec(commands);
			InputStream is = proc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				logger.info(line);
				sb.append(line + "\r\n");
			}
			br.close();
			is.close();
			return sb.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static void main (String[] args) {
		// delete(new File("E:/home/kbox/ps/tmp/ability/1004"));
		// System.out.println(exec("dir"));
//		System.out.println(exec(new String[] {
//				"D:", "cd D:/k-kbox/games/FJDZ/trunk/FJDZ_pay/", ConfigUtil.get("ant.path", true) + " clean,release"}));
//		System.out.println(exec(new String[] {
//				"cd " + ConfigUtil.get("apktool.dir", true), 
//				"apktool d -f D:/k-kbox/games/FJDZ/trunk/FJDZ.apk -o D:/k-kbox/games/FJDZ/trunk/tmp/game",
//				"apktool d -f D:/k-kbox/games/FJDZ/trunk/FJDZ_pay/bin/FJDZ_pay-release-unsigned.apk -o D:/k-kbox/games/FJDZ/trunk/tmp/game_pay"
//		}));
//		copy(new File("D:/k-kbox/games/FJDZ/trunk/tmp/game_pay"), new File("D:/k-kbox/games/FJDZ/trunk/tmp/game"), true);
//		System.out.println(exec(new String[] {
//				"cd " + ConfigUtil.get("apktool.dir", true), 
//				"apktool b D:/k-kbox/games/FJDZ/trunk/tmp/game -o D:/k-kbox/games/FJDZ/trunk/game_v2.2.1.apk"
//		}));
//		System.out.println(ExecUtil.exec("jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose " +
//				"-keystore " + ConfigUtil.get("signer.keystore", true) + " " +
//				"-storepass " + ConfigUtil.get("signer.storepass") + " " +
//				"-keypass " + ConfigUtil.get("signer.keypass") + " " +
//				"-signedjar D:/k-kbox/games/FJDZ/trunk/game_v2.2.1_signed.apk " +
//				"D:/k-kbox/games/FJDZ/trunk/game_v2.2.1.apk " +
//				ConfigUtil.get("signer.alias")));
	}
}
