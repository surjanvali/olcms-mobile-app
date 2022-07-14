package in.apcfss.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ECourtsCryptoHelper {

	public static final String initializationVector="abcdef987654";
	public static final String authenticationKey = "PxaCV2s2kzKI";
	
	public static String encrypt(byte[] payload)
	{
		byte[] iv = initializationVector.getBytes(StandardCharsets.UTF_8); // change with your IV
		byte[] key = authenticationKey.getBytes(StandardCharsets.UTF_8);// change with your Key
		byte[] ivBytes = new byte[16];
		byte[] keyBytes = new byte[16];
		System.arraycopy(iv, 0, ivBytes, 0, iv.length);
		System.arraycopy(key, 0, keyBytes, 0, key.length);
		try {
	        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
	        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes,"AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
	        byte[] encrypted = cipher.doFinal(payload);
	        //return Base64.encodeBase64String(encrypted);
	        
	        return Base64.getEncoder().encodeToString(encrypted);
	        
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException(ex);
	    }
	}
	
	public static String decrypt(byte[] cipherText)
	{
		byte[] iv =initializationVector.getBytes(StandardCharsets.UTF_8);// change with your IV
		byte[] key = authenticationKey.getBytes(StandardCharsets.UTF_8);// change with your Key
	  byte[] ivBytes = new byte[16];
		byte[] keyBytes = new byte[16];
		System.arraycopy(iv, 0, ivBytes, 0, iv.length);
		System.arraycopy(key, 0, keyBytes, 0, key.length);
		try {
	        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
	        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes,"AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
	   
			  byte[] encrypted =
			  cipher.doFinal(Base64.getDecoder().decode(cipherText)); return new
			  String(encrypted,StandardCharsets.UTF_8);
			 
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException(ex);
	    }
	}
	public static void main(String[] args) throws Exception {
		
		
		  // String originalString="state_code=27|dist_code=515|consume_date=2020-01-01|lg_census_flag=C";
		  String originalString="cino=APHC010001302016";
		  String encryptedString = encrypt(originalString.getBytes()) ;
		    String decryptedString = decrypt(encryptedString.getBytes()) ;
		    System.out.println(originalString);
		    System.out.println(""+URLEncoder.encode(encryptedString, "UTF-8" ));
		    System.out.println(decryptedString);
	}
}

