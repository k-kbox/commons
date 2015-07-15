package com.kbox.commons.network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.kbox.commons.crypt.Md5Helper;

public class HttpUtil {


	private static Logger log = Logger.getLogger(HttpUtil.class);
	
	private static HttpGet getHttpGet(String url, Map<String, String> params,  
            String encode) {  
        StringBuffer buf = new StringBuffer(url);  
        if (params != null) {  
            // 地址增加?或者&  
            String flag = (url.indexOf('?') == -1) ? "?" : "&";  
            // 添加参数  
            for (String name : params.keySet()) {  
                buf.append(flag);  
                buf.append(name);  
                buf.append("=");  
                try {  
                    String param = params.get(name);  
                    if (param == null) {  
                        param = "";  
                    }  
                    buf.append(URLEncoder.encode(param, encode));  
                } catch (UnsupportedEncodingException e) {  
                    log.error("URLEncoder Error,encode=" + encode + ",param="  
                            + params.get(name), e);  
                }  
                flag = "&";  
            }  
        }  
        HttpGet httpGet = new HttpGet(buf.toString());  
        return httpGet;  
    }  
	
	public static void downloadFile(String url, String path, String md5) {
		if (url == null || url.length() == 0 || !url.startsWith("http://")) {
			return;
		}
		File file = new File(path);
		if (file.exists() && Md5Helper.getMd5(file).equals(md5)) {
			return;
		}
		downloadFile(url, path);
	}
	
	public static void downloadFile(String url, String path) {
		if (url == null || url.length() == 0 || !url.startsWith("http://")) {
			return;
		}
		HttpClient client = null;  
        try {  
            // 创建HttpClient对象  
            client = new DefaultHttpClient();  
            // 获得HttpGet对象  
            HttpGet httpGet = getHttpGet(url, null, null);  
            // 发送请求获得返回结果  
            HttpResponse response = client.execute(httpGet);  
            // 如果成功  
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
                byte[] result = EntityUtils.toByteArray(response.getEntity());  
                BufferedOutputStream bw = null;  
                try {  
                    // 创建文件对象  
                    File f = new File(path);  
                    // 创建文件路径  
                    if (!f.getParentFile().exists())  
                        f.getParentFile().mkdirs();  
                    // 写入文件  
                    bw = new BufferedOutputStream(new FileOutputStream(path));  
                    bw.write(result);  
                } catch (Exception e) {  
                    log.error("保存文件错误,path=" + path + ",url=" + url, e);  
                } finally {  
                    try {  
                        if (bw != null)  
                            bw.close();  
                    } catch (Exception e) {  
                        log.error(  
                                "finally BufferedOutputStream shutdown close",  
                                e);  
                    }  
                }  
            }  
            // 如果失败  
            else {  
                StringBuffer errorMsg = new StringBuffer();  
                errorMsg.append("httpStatus:");  
                errorMsg.append(response.getStatusLine().getStatusCode());  
                errorMsg.append(response.getStatusLine().getReasonPhrase());  
                errorMsg.append(", Header: ");  
                Header[] headers = response.getAllHeaders();  
                for (Header header : headers) {  
                    errorMsg.append(header.getName());  
                    errorMsg.append(":");  
                    errorMsg.append(header.getValue());  
                }  
                log.error("HttpResonse Error:" + errorMsg);  
            }  
        } catch (ClientProtocolException e) {  
            log.error("下载文件保存到本地,http连接异常,path=" + path + ",url=" + url, e);  
            // throw e;  
        } catch (IOException e) {  
            log.error("下载文件保存到本地,文件操作异常,path=" + path + ",url=" + url, e);  
            // throw e;  
        } finally {  
            try {  
                client.getConnectionManager().shutdown();  
            } catch (Exception e) {  
                log.error("finally HttpClient shutdown error", e);  
            }  
        }  
	}
	
	public static String uploadFile(String path, String uploadUrl) {
		// 定义请求url
		// String uri = ConfigUtil.get("upload.apk.url");
		// 实例化http客户端
		HttpClient httpClient = new DefaultHttpClient();
		// 实例化post提交方式
		HttpPost post = new HttpPost(uploadUrl);
		// 添加json参数
		try {
			// 实例化参数对象
			MultipartEntity params = new MultipartEntity();
			// 设置上传文件
			File file = new File(path);
			// 文件参数内容
			FileBody fileBody = new FileBody(file);
			// 添加文件参数
			params.addPart("file", fileBody);
			params.addPart("fileName", new StringBody(file.getName()));
			// 将参数加入post请求体中
			post.setEntity(params);
			// 执行post请求并得到返回对象 [ 到这一步我们的请求就开始了 ]
			HttpResponse resp = httpClient.execute(post);
			// 解析返回请求结果
			HttpEntity entity = resp.getEntity();
			InputStream is = entity.getContent();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			StringBuffer buffer = new StringBuffer();
			String temp;
			while ((temp = reader.readLine()) != null) {
				buffer.append(temp);
			}
			return buffer.toString();
			// System.out.println(buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return "";
	}
}
