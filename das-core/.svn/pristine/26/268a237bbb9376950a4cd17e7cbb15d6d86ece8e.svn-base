package com.cas.das.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.jlrnt.common.util.StringUtils;
import net.jlrnt.common.util.json.JsonUtils;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 网络抓取数据工具类。
 * 
 * @author xiang_wang
 */
public class SpriderUtil {

	// AES解密使用KEY
	private static final String AES_SERVER_KEY = "6faf4a2fa46ac1cb";
	private static final String AES_SERVER_IV = "4d6c56abc669f198";

	// AES加密使用KEY
	private static final String AES_CLIENT_KEY = "d0936268a554ed2a";
	private static final String AES_CLIENT_IV = "2441e23aca5285a8";

	// DES解密使用KEY
	private static final String DES_KEY = "863f30c7"; // 863f30c7f96c96fb
	private static final String DES_IV = "9ff4453b";

	// 请求参数固定值
	private static final String APP_ID = "1a45f75b824b2dc628d5955356b5ef18";
	private static final String CLIENT_TYPE = "WEB";

	// 加密算法
	private static Cipher aesCipher = null;
	private static Cipher desCipher = null;
	private static MessageDigest md = null;

	static {
		try {
			// "算法/模式/补码方式"
			aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// "算法/模式/补码方式"
			desCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			md = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建查询参数。<br/>
	 * <p>
	 * method为访问类型方法，例如：GETMAPDATA<br/>
	 * object为这种访问类型所需要的参数，必须声明为LinkedHashMap,且key需要按照字符串从小往大的顺序写入，例如：
	 * 
	 * <code>
	 * Map<String, Object> object = new LinkedHashMap<String, Object>();
	 * object.put("timepoint", "2017-07-26 12:00:00");
	 * object.put("type", "HOUR");
	 * </code>
	 * </p>
	 * 
	 * @param method
	 * @param object
	 * @return
	 */
	public static String buildParam(String method, LinkedHashMap<String, Object> object) {

		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		paramMap.put("appId", APP_ID);
		paramMap.put("method", method);

		String timestamp = String.valueOf(new Date().getTime());
		paramMap.put("timestamp", timestamp);

		paramMap.put("clienttype", CLIENT_TYPE);
		paramMap.put("object", object);

		// md5计算secret
		String jsonObject = JsonUtils.serialize(object);
		String content = APP_ID + method + timestamp + CLIENT_TYPE + jsonObject;
		String secret = SpriderUtil.encryptMd5(content);
		paramMap.put("secret", secret);

		String result = SpriderUtil.encodeAqiStudyParam(JsonUtils.serialize(paramMap));
		return result;
	}

	/**
	 * 解码AQIstudy网站返回值
	 * 
	 * @param value
	 * @return
	 */
	public static String decodeAqiStudyParam(String value) {
		// AES解密
		String aesDeTmp = SpriderUtil.decryptAES(value, SpriderUtil.AES_CLIENT_KEY, SpriderUtil.AES_CLIENT_IV);

		// base64解码
		String baseDeTmp = SpriderUtil.decodeBase64(aesDeTmp);
		return baseDeTmp;
	}
	public static void main(String[] args) {
		String s = decodeAqiStudyParam("tdgHOYxwKdDSgYXe+RLPzYCgLvrddahasI5XXklB4gVLYqab+XRPpMD/oSqnJ/aEmFwzVEUhLnPzRy03+X1BI4qc9EYeRPqiKrT+f1JQExGQ4ii8kKvZhGH+nPffaX/xq5iLB6vblcvBC/L8e6UxdhYmK0hVyxwdK3WjHTI8C+X4ETAsWHSen24ZVtAWyLxA+3rbCRbrdfcYlkj2DgH7kGvr+fxrb3UiytnMZ4MYsNR1pnAlbsGL5kbKkrekWhi7OM48w0Rf7qFO76ryVSQE6CBSRmE82aiTrXRBu2HHauDA2mW3aBlVzqSIfpxY4u6xpadJU0QvBaN4OUFIHAZkhzcx/p48lEmJFyoDXSl7w/zSADlIUS7SKr05CtDEX/L+r0h3AHwv4731HUCEu6q/XcqWkJSCJhtWidEqAuX/OpG0NfW42xx1afSK5QGMVpoI2sJL4y3GYLL0UR3ejKZLdoy6Qa8xN/AWMAoKAc6hM0k=");
		System.out.println(s);
	}

	/**
	 * 编码AQIstudy网站参数
	 * 
	 * @param value
	 * @return
	 */
	public static String encodeAqiStudyParam(String value) {
		// base64编码
		String baseTmp = SpriderUtil.encodeBase64(value);

		// AES编码
		String aesTmp = SpriderUtil.encryptAES(baseTmp, SpriderUtil.AES_CLIENT_KEY, SpriderUtil.AES_CLIENT_IV);
		return aesTmp;
	}

	/**
	 * 解码AQIstudy网站返回值
	 * 
	 * @param value
	 * @return
	 */
	public static String decodeAqiStudyResp(String value) {
		// AES解密
		String aesDeTmp = SpriderUtil.decryptAES(value, SpriderUtil.AES_SERVER_KEY, SpriderUtil.AES_SERVER_IV);

		// DES解密
		String desDeTmp = SpriderUtil.decryptDES(aesDeTmp, SpriderUtil.DES_KEY, SpriderUtil.DES_IV);

		// base64解码
		String baseDeTmp = SpriderUtil.decodeBase64(desDeTmp);
		return baseDeTmp;
	}
	
	/**
	 * BASE64编码
	 * 
	 * @param str
	 * @return
	 */
	public static String encodeBase64(String str) {
		String result = null;
		try {
			result = Base64.encodeBase64String(str.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * BASE64解码
	 * 
	 * @param str
	 * @return
	 */
	public static String decodeBase64(String str) {
		String result = null;
		try {
			result = new String(new BASE64Decoder().decodeBuffer(str));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * AES加密
	 * <p>
	 * 仅用户aqistudy网站抓取数据后的操作
	 * </p>
	 * 
	 * @param str
	 * @param key
	 * @param iv
	 * @return
	 */
	public synchronized static String encryptAES(String str, String key, String iv) {
		String rs = null;
		try {
			byte[] textBytes = str.getBytes("UTF8");
			byte[] keyBytes = key.getBytes("UTF8");
			byte[] ivBytes = iv.getBytes("UTF8");

			AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec, paramSpec);
			byte[] encrypted = aesCipher.doFinal(textBytes);
			rs = Base64.encodeBase64String(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * AES解密
	 * <p>
	 * 仅用户aqistudy网站抓取数据后的操作
	 * </p>
	 * 
	 * @param str
	 * @param key
	 * @param iv
	 * @return
	 */
	public synchronized static String decryptAES(String str, String key, String iv) {
		String rs = null;
		try {
			byte[] textBytes = new BASE64Decoder().decodeBuffer(str);
			byte[] keyBytes = key.getBytes("UTF8");
			byte[] ivBytes = iv.getBytes("UTF8");

			AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
			aesCipher.init(Cipher.DECRYPT_MODE, skeySpec, paramSpec);
			byte[] decrypted = aesCipher.doFinal(textBytes);
			rs = new String(decrypted, "UTF8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * DES加密
	 * <p>
	 * 仅用户aqistudy网站抓取数据后的操作
	 * </p>
	 * 
	 * @param str
	 * @param key
	 * @param iv
	 * @return
	 */
	public synchronized static String encryptDES(String str, String key, String iv) {
		String rs = null;
		try {
			byte[] textBytes = str.getBytes("UTF8");
			byte[] keyBytes = key.getBytes("UTF8");
			byte[] ivBytes = iv.getBytes("UTF8");

			AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "DES");
			desCipher.init(Cipher.ENCRYPT_MODE, skeySpec, paramSpec);
			byte[] encrypted = desCipher.doFinal(textBytes);
			rs = new BASE64Encoder().encode(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * DES解密
	 * <p>
	 * 仅用户aqistudy网站抓取数据后的操作
	 * </p>
	 * 
	 * @param str
	 * @param key
	 * @param iv
	 * @return
	 */
	public synchronized static String decryptDES(String str, String key, String iv) {
		String rs = null;
		try {
			byte[] textBytes = new BASE64Decoder().decodeBuffer(str);
			byte[] keyBytes = key.getBytes("UTF8");
			byte[] ivBytes = iv.getBytes("UTF8");

			AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "DES");
			desCipher.init(Cipher.DECRYPT_MODE, skeySpec, paramSpec);
			byte[] decrypted = desCipher.doFinal(textBytes);
			rs = new String(decrypted, "UTF8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * MD5加密
	 * 
	 * @param value
	 * @return
	 */
	public static String encryptMd5(String value) {

		String result = null;
		try {
			result = StringUtils.byteArrToHexStr(md.digest(value.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
