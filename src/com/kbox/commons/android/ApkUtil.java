package com.kbox.commons.android;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.File;
//import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import com.kbox.commons.config.ConfigUtil;
import com.kbox.commons.fs.FSInfo;
import com.kbox.commons.fs.FSType;
import com.kbox.commons.fs.FSUtil;


import net.sf.json.JSONObject;

//import org.apkinfo.api.GetApkInfo;
//import org.apkinfo.api.domain.ApkInfo;



public class ApkUtil {
//	public static String getAPKPageName(File file) {
//		ApkInfo apkInfo;
//		try {
//			apkInfo = GetApkInfo.getApkInfoByFilePath(file.getPath());
//			return apkInfo.getPackageName();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}  
//		return null;
//	}
//	public static String getAPKVersionCode(File file) {
//		ApkInfo apkInfo;
//		try {
//			apkInfo = GetApkInfo.getApkInfoByFilePath(file.getPath());
//			return apkInfo.getVersionName()+"("+apkInfo.getVersionCode()+")";
//		} catch (IOException e) {
//			e.printStackTrace();
//		}  
//		return null;
//	}
	
	// private final static String AAPT_DEFAULT_PATH = "/usr/local/android-sdk-linux/build-tools/22.0.1"; 
	
	public static String extractIcon(String apk, String icon) {
		String path = apk.substring(0, apk.length() - 4) + "_icon/" + icon;
		// File dir = new File(path.substring(0, path.lastIndexOf("\\|/")));
		File file = new File(path);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				extractFileFromApk(apk, icon, path);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	    
    /**
     * 从指定的apk文件里获取指定file的流
     * @param apkpath
     * @param fileName
     * @return
     */
    public static InputStream extractFileFromApk(String apkpath, String fileName) {
        try {
            ZipFile zFile = new ZipFile(apkpath);
            ZipEntry entry = zFile.getEntry(fileName);
            entry.getComment();
            entry.getCompressedSize();
            entry.getCrc();
            entry.isDirectory();
            entry.getSize();
            entry.getMethod();
            InputStream stream = zFile.getInputStream(entry);
            return stream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void extractFileFromApk(String apkpath, String fileName, String outputPath) throws Exception {
        InputStream is = extractFileFromApk(apkpath, fileName);
        
        File file = new File(outputPath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), 1024);
        byte[] b = new byte[1024];
        BufferedInputStream bis = new BufferedInputStream(is, 1024);
        while(bis.read(b) != -1){
            bos.write(b);
        }
        bos.flush();
        is.close();
        bis.close();
        bos.close();
    }
	
	public static ApkInfo getApkInfo(FSInfo apk) {
//		String aaptPath = ConfigUtil.get("aapt.path");
//		if (aaptPath == null || aaptPath.length() == 0) {
//			aaptPath = AAPT_DEFAULT_PATH;
//		}
		ApkInfo apkInfo = new ApkInfo(apk);
		try {
			Runtime rt = Runtime.getRuntime();
			String[] command; //aaptPath + " d badging " + apk.getPath() + "\"";
			String os = System.getProperty("os.name");
			if (os.toLowerCase().startsWith("windows")) {
				// Windows XP, Vista ...
				command = new String[] {
					"cmd.exe",
					"/C",
					ConfigUtil.get("aapt.path.windows") + "/aapt.exe d badging \"" + apk.getPath() + "\""
				};
			} else {
				// Unix, Linux ...
				command = new String[] {
					"/bin/sh",
					"-c",
					ConfigUtil.get("aapt.path.linux") + "/aapt d badging \"" + apk.getPath() + "\""
				};
			}
			// System.out.println(command[0] + " " + command[1] + " " + command[2]);
			Process proc = rt.exec(command);
			InputStream is = proc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			// StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
			//	sb.append(line + "\r\n");
				// System.out.println(line);
				String[] info = line.split(":", 2);
				if ("package".equals(info[0])) {
					String[] tmps = info[1].split(" ");
					for (String tmp : tmps) {
						String[] ss = tmp.split("=", 2);
						if ("name".equals(ss[0])) {
							apkInfo.packageName = ss[1].substring(1, ss[1].length() - 1);
						}
						else if ("versionCode".equals(ss[0])) {
							apkInfo.versionCode = ss[1].substring(1, ss[1].length() - 1);
						}
						else if ("versionName".equals(ss[0])) {
							apkInfo.versionName = ss[1].substring(1, ss[1].length() - 1);
						}
					}
				}
				else if ("sdkVersion".equals(info[0])) {
					apkInfo.sdkVersion = info[1].substring(1, info[1].length() - 1);
				}
				else if ("targetSdkVersion".equals(info[0])) {
					apkInfo.targetSdkVersion = info[1].substring(1, info[1].length() - 1);
				}
				else if ("uses-permission".equals(info[0])) {
					String[] ss = info[1].split("=", 2);
					apkInfo.permissions.add(ss[1].substring(1, ss[1].length() - 1));
				}
				else if ("application-label".equals(info[0])) {
					apkInfo.applicationName = info[1].substring(1, info[1].length() - 1);
				}
				else if ("application".equals(info[0])) {
					String[] tmps = info[1].split(" ");
					for (String tmp : tmps) {
						String[] ss = tmp.split("=", 2);
						if ("icon".equals(ss[0])) {
							String name = ss[1].substring(1, ss[1].length() - 1);
							String path = extractIcon(apk.getPath(), name);
							FSInfo icon = new FSInfo(FSType.icon, path, name);
							FSUtil.getDisplay(icon);
							FSUtil.publish(icon, false);
							apkInfo.icon = icon;
						}
					}
				}
				else if ("launchable-activity".equals(info[0])) {
					String[] tmps = info[1].split(" ");
					for (String tmp : tmps) {
						String[] ss = tmp.split("=", 2);
						if ("name".equals(ss[0])) {
							apkInfo.launcher = ss[1].substring(1, ss[1].length() - 1);
						}
					}
				}
				else if (info[0].startsWith("application-icon-")) {
					String type = info[0].substring("application-icon-".length());
//					FSInfo icon = new FSInfo("", "icon", info[1].substring(1, info[1].length() - 1), 0, "", "", "");
//					extractIcon(apk, icon);
					String name = info[1].substring(1, info[1].length() - 1);
					String path = extractIcon(apk.getPath(), name);
					FSInfo icon = new FSInfo(FSType.icon, path, name);
					FSUtil.getDisplay(icon);
					FSUtil.publish(icon, false);
					apkInfo.icons.put(type, icon);
				}
			}
			br.close();
			// System.out.println("proc.exitValue: " + proc.exitValue());
			return apkInfo;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return apkInfo;
	}
	
	public static class ApkInfo {
		
		public String applicationName;
		
		public String packageName;
		public String versionCode;
		public String versionName;
		public String sdkVersion;
		public String targetSdkVersion;
		
		public String launcher;
		
		public List<String> permissions;
		
		public FSInfo icon;
		public Map<String, FSInfo> icons;

		public FSInfo file;
			
		public ApkInfo(FSInfo file) {
			this.file = file;
			this.applicationName = "";
			this.packageName = "";
			this.versionCode = "";
			this.versionName = "";
			this.sdkVersion = "";
			this.targetSdkVersion = "";
			this.launcher = "";
			this.permissions = new ArrayList<String>();
			this.icons = new LinkedHashMap<String, FSInfo>();
			this.icon = null;
		}
		
		public String toJsonString() {
			JSONObject obj = new JSONObject();
			obj.put("name", applicationName);
			obj.put("package", packageName);
			obj.put("version_code", versionCode);
			obj.put("version_name", versionName);
			obj.put("sdk_version", sdkVersion);
			obj.put("target_sdk_version", targetSdkVersion);
			obj.put("launcher", launcher);
			obj.put("permissions", permissions);
			obj.put("icon", icon.toJsonString());
			JSONObject obj1 = new JSONObject();
			for (Entry<String, FSInfo> e : icons.entrySet()) {
				obj1.put(e.getKey(), e.getValue().toJsonString());
			}
			obj.put("icons", obj1);
			obj.put("file", file.toJsonString());
			return obj.toString();
		}
	}
	
	/****
	package: name='com.j1game.popstar.gfyys' versionCode='12' versionName='1.2' platformBuildVersionName='APKTOOL'
	sdkVersion:'9'
	targetSdkVersion:'9'
	uses-permission: name='android.permission.VIBRATE'
	uses-permission: name='android.permission.INTERNET'
	uses-permission: name='android.permission.ACCESS_WIFI_STATE'
	uses-permission: name='android.permission.ACCESS_NETWORK_STATE'
	uses-permission: name='android.permission.GET_TASKS'
	uses-permission: name='android.permission.WAKE_LOCK'
	uses-permission: name='android.permission.READ_PHONE_STATE'
	uses-permission: name='android.permission.MOUNT_UNMOUNT_FILESYSTEMS'
	uses-permission: name='android.permission.WRITE_EXTERNAL_STORAGE'
	uses-permission: name='android.permission.RESTART_PACKAGES'
	uses-permission: name='com.android.launcher.permission.INSTALL_SHORTCUT'
	uses-permission: name='com.android.launcher.permission.UNINSTALL_SHORTCUT'
	uses-permission: name='com.android.launcher.permission.READ_SETTINGS'
	uses-permission: name='android.permission.ACCESS_NETWORK_STATE'
	uses-permission: name='android.permission.READ_PHONE_STATE'
	uses-permission: name='android.permission.SEND_SMS'
	uses-permission: name='android.permission.INTERNET'
	uses-permission: name='android.permission.ACCESS_WIFI_STATE'
	uses-permission: name='android.permission.WRITE_EXTERNAL_STORAGE'
	uses-permission: name='android.permission.MOUNT_UNMOUNT_FILESYSTEMS'
	uses-permission: name='android.permission.WRITE_EXTERNAL_STORAGE'
	uses-permission: name='android.permission.INTERNET'
	uses-permission: name='android.permission.SEND_SMS'
	uses-permission: name='android.permission.READ_PHONE_STATE'
	uses-permission: name='android.permission.ACCESS_NETWORK_STATE'
	uses-permission: name='android.permission.DISABLE_KEYGUARD'
	uses-permission: name='android.permission.ACCESS_WIFI_STATE'
	uses-permission: name='android.permission.READ_PHONE_STATE'
	uses-permission: name='android.permission.SEND_SMS'
	uses-permission: name='android.permission.WRITE_EXTERNAL_STORAGE'
	uses-permission: name='android.permission.INTERNET'
	uses-permission: name='android.permission.ACCESS_NETWORK_STATE'
	application-label:'萌萌爱消除'
	application-icon-160:'res/drawable-mdpi/ic_launcher.png'
	application-icon-240:'res/drawable-hdpi/ic_launcher.png'
	application-icon-320:'res/drawable-xhdpi/ic_launcher.png'
	application-icon-480:'res/drawable-xxhdpi/ic_launcher.png'
	application: label='萌萌爱消除' icon='res/drawable-mdpi/ic_launcher.png'
	launchable-activity: name='com.klw.seastar.MainActivity'  label='' icon=''
	uses-permission: name='android.permission.READ_EXTERNAL_STORAGE'
	uses-implied-permission: name='android.permission.READ_EXTERNAL_STORAGE' reason='requested WRITE_EXTERNAL_STORAGE'
	feature-group: label=''
	  uses-feature: name='android.hardware.screen.portrait'
	  uses-implied-feature: name='android.hardware.screen.portrait' reason='one or more activities have specified a portrait orientation'
	  uses-feature: name='android.hardware.telephony'
	  uses-implied-feature: name='android.hardware.telephony' reason='requested a telephony permission'
	  uses-feature: name='android.hardware.touchscreen'
	  uses-implied-feature: name='android.hardware.touchscreen' reason='default feature for all apps'
	  uses-feature: name='android.hardware.wifi'
	  uses-implied-feature: name='android.hardware.wifi' reason='requested android.permission.ACCESS_WIFI_STATE permission'
	main
	other-activities
	other-receivers
	other-services
	supports-screens: 'small' 'normal' 'large' 'xlarge'
	supports-any-density: 'true'
	locales: '--_--'
	densities: '160' '240' '320' '480'
	native-code: 'armeabi' 'x86'
	****/
	
	public static void main(String[] a)
	{	
//		File file = new File("e:\\c.apk");
//		System.out.println(ApkUtil.getAPKPageName(file));
//		System.out.println(ApkUtil.getAPKVersionCode(file));
//		
//		System.out.println(getApkInfo(new FSInfo(FSType.apk, "c:/Users/jun.huang/Desktop/MMAXC_v1.2_20150302_a_3in1_313_dkyz_3003994419_20150415.apk",
//				"MMAXC_v1.2_20150302_a_3in1_313_dkyz_3003994419_20150415.apk")).toJsonString());
	}

}
