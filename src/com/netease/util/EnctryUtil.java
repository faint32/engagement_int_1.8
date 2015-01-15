package com.netease.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EnctryUtil {

	public static String getMd5Hash(String input) { 
		try {
			// 对有道图片裁剪服务器进行特殊处理
			if (input.length() > 40 && input.charAt(15) == '.'
					&& ".ydstatic.com/image?".equals(input.substring(15, 35))) {
				input = input.substring(39);
			}

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);

			while (md5.length() < 32)
				md5 = "0" + md5;

			return md5;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public String base64Encode(String src) {
		if (src == null)
			return null;
		return new String(Base64.encode(src.getBytes()));
	}

	static public String base64Decode(String src) {
		if (src == null)
			return null;

		return new String(Base64.decode(src.getBytes()));
	}

	private final static char[] hexDigits = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	private static Cipher cp = null;

	public static String bytesToHexString(byte[] byteArray) {
		StringBuilder sb = new StringBuilder();
		String stmp = null;
		for (int n = 0; n < byteArray.length; n++) {
			stmp = (java.lang.Integer.toHexString(byteArray[n] & 0XFF));
			if (stmp.length() == 1)
				sb.append("0").append(stmp);
			else
				sb.append(stmp);
		}
		return sb.toString().toUpperCase();
	}

	public static String byteToHexString(byte b) {
		StringBuilder sb = new StringBuilder();
		byte temp = b;
		temp = (byte) (temp & 0x0F);
		sb.append(hexDigits[temp]);
		temp = (byte) (b >>> 4);
		temp = (byte) (temp & 0x0F);
		sb.append(hexDigits[temp]);
		return sb.toString();
	}

	public static byte charsToByte(char low, char high) {
		String temp = "" + high + low;
		int val = Integer.parseInt(temp, 16);
		return (byte) val;
	}

	public static byte[] stringToBytes(String text) {
		byte[] b = text.getBytes();
		if (0 != b.length % 2)
			throw new IllegalArgumentException("长度不是偶数");
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	public static String encryptForAES(String content, byte[] key) {
		String result = null;
		try {
			byte[] bytesContent = content.getBytes("UTF-8");
			result = encryptForAES(bytesContent, key);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
    
	public static String encryptForAES(String content, String key)
			throws Exception {
		byte[] bytesContent = content.getBytes("UTF-8");
		byte[] keyBytes = stringToBytes(key);
		return encryptForAES(bytesContent, keyBytes);
	}

	public static byte[] hex2byte(String s) {
		byte[] b = s.getBytes();
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("长度不是偶数");
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	public static String encryptForAES(byte[] bytesContent, byte[] key) {
		String encryptContent = null;
		try {
			if (cp == null)
				cp = Cipher.getInstance("AES");
			SecretKey encryptKey = new SecretKeySpec(key, "AES");
			cp.init(Cipher.ENCRYPT_MODE, encryptKey);
			encryptContent = bytesToHexString(cp.doFinal(bytesContent));
		} catch (Exception e) {
			e.printStackTrace();
			;
		}
		return encryptContent;
	}

	public static String decryptForAES(String encryptContent, String key)
			throws Exception {
		byte[] bytes = stringToBytes(encryptContent);
		byte[] keyBytes = stringToBytes(key);
		return decryptForAES(bytes, keyBytes);
	}

	public static String decryptForAES(String encryptContent, byte[] key)
			throws Exception {
		byte[] bytes = stringToBytes(encryptContent);
		return decryptForAES(bytes, key);
	}

	public static String decryptForAES(byte[] content, byte[] key) {
		String result = null;
		try {
			if (cp == null)
				cp = Cipher.getInstance("AES");
			SecretKey decryptKey = new SecretKeySpec(key, "AES");
			cp.init(Cipher.DECRYPT_MODE, decryptKey);
			byte[] cipherByte = cp.doFinal(content);
			result = new String(cipherByte, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
