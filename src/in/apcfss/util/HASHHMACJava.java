package in.apcfss.util;

import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;

public class HASHHMACJava {
	
	/*
	public static String encode(String key, String data) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);

		return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
	}

	public static void main(String[] args) throws Exception {
		System.out.println(encode("key", "The quick brown fox jumps over the lazy dog"));
	}
	*/
	
	public static void main(String[] args) {
		try {
			// byte[] hmacSha256 = calcHmacSha256("secret123".getBytes("UTF-8"), "hello world".getBytes("UTF-8"));
			
			byte[] hmacSha256 = calcHmacSha256("15081947".getBytes("UTF-8"), "cino=MHAU010092312018".getBytes("UTF-8"));
			System.out.println(String.format("Hex: %032x", new BigInteger(1, hmacSha256)));
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	static public byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
		byte[] hmacSha256 = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
			mac.init(secretKeySpec);
			hmacSha256 = mac.doFinal(message);
		} catch (Exception e) {
			throw new RuntimeException("Failed to calculate hmac-sha256", e);
		}
		return hmacSha256;
	}
	
	
	public static String hmac256Decrypt(byte[] secretKey, byte[] hmac256) {
		String message = "";
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec sks = new SecretKeySpec(secretKey, "HmacSHA256");
			mac.init(sks);
			
			
			
			// message = sks.hashCode()
			
			return message;
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate HMACSHA256 encrypt ");
		}
	}
	
	/*
	
	public static byte[] hmac256(String secretKey, String message) {
		try {
			return hmac256(secretKey.getBytes("UTF-8"), message.getBytes("UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate HMACSHA256 encrypt", e);
		}
	}

	public static byte[] hmac256(byte[] secretKey, byte[] message) {
		byte[] hmac256 = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec sks = new SecretKeySpec(secretKey, "HmacSHA256");
			mac.init(sks);
			hmac256 = mac.doFinal(message);
			return hmac256;
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate HMACSHA256 encrypt ");
		}
	}

	public static void main(String args[]) {
		// byte[] hmacSha256 = hmac256("secreT1_", "Hello world from Java!");
		byte[] hmacSha256 = hmac256("15081947", "cino=MHAU010092312018");
		System.out.println(String.format("Hex: %032x", new BigInteger(1, hmacSha256)));

		String base64HmacSha256 = Base64.getEncoder().encodeToString(hmacSha256);
		System.out.println("Base64: " + base64HmacSha256);
		
		
		
		
	}
	
	
	
	
	*/

}
