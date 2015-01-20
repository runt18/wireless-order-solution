package com.wireless.pojo.token;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import com.wireless.exception.BusinessException;
import com.wireless.exception.TokenError;

public class RSACoder{
    private static final String KEY_ALGORITHM = "RSA";  
  
    private final KeyPair keyPair;
    
	public static void main(String[] args) throws Exception {
		RSACoder rsaCoder = new RSACoder();
        System.out.println("公钥: \n" + rsaCoder.getPublicKey());  
        System.out.println("私钥： \n" + rsaCoder.getPrivateKey());  
        
        System.out.println("公钥加密——私钥解密");  
        String inputStr = "{\"lm\":1421656861426,\"rid\":40,\"tid\":24}";  
        byte[] data = inputStr.getBytes();  
  
        byte[] encodedData = rsaCoder.encryptByPublicKey(data);  
  
        System.out.println(encodedData);
        
        byte[] decodedData = rsaCoder.decryptByPrivateKey(encodedData);  
  
        String outputStr = new String(decodedData);  
        System.out.println("加密前: " + inputStr + " <--> " + "解密后: " + outputStr); 
	}
	
	public RSACoder() throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);  
        keyPairGen.initialize(1024);  
        this.keyPair = keyPairGen.generateKeyPair();
	}
	
	public RSACoder(final String publicKey, final String privateKey){
		PublicKey pubKey = new PublicKey(){

			private static final long serialVersionUID = 1L;

			@Override
			public String getAlgorithm() {
				return KEY_ALGORITHM;
			}

			@Override
			public String getFormat() {
				return new String(getEncoded());
			}

			@Override
			public byte[] getEncoded() {
				return decryptBASE64(publicKey);
			}
			
		};
		
		PrivateKey priKey = new PrivateKey(){
			private static final long serialVersionUID = 1L;

			@Override
			public String getAlgorithm() {
				return KEY_ALGORITHM;
			}

			@Override
			public String getFormat() {
				return new String(getEncoded());
			}

			@Override
			public byte[] getEncoded() {
				return decryptBASE64(privateKey);
			}
			
		};
		this.keyPair = new KeyPair(pubKey, priKey);
	}
	
	/**
	 * 解密<br>
	 * 用私钥解密
	 * 
	 * @param data
	 * @param key
	 * @return
	 */ 
    public byte[] decryptByPrivateKey(byte[] data) throws BusinessException{  
        // 对密钥解密  
        byte[] keyBytes = decryptBASE64(getPrivateKey());  
  
        // 取得私钥  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        try{
	        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
	        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
	  
	        // 对数据解密  
	        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
	        cipher.init(Cipher.DECRYPT_MODE, privateKey);  
	  
	        return cipher.doFinal(data);
        }catch(Exception e){
        	throw new BusinessException(TokenError.TOKEN_DECRYPT_FAIL);
        }
    }  
  
    /** 
     * 解密<br> 
     * 用公钥解密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public byte[] decryptByPublicKey(byte[] data) throws BusinessException {  
    	try{
	        // 对密钥解密  
	        byte[] keyBytes = decryptBASE64(getPublicKey());  
	  
	        // 取得公钥  
	        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
	        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
	        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
	  
	        // 对数据解密  
	        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
	        cipher.init(Cipher.DECRYPT_MODE, publicKey);  
	  
	        return cipher.doFinal(data);
    	}catch(Exception e){
    		throw new BusinessException(TokenError.TOKEN_DECRYPT_FAIL);
    	}
    }  
  
    /** 
     * 加密<br> 
     * 用公钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public byte[] encryptByPublicKey(byte[] data) throws BusinessException {  
    	try{
	        // 对公钥解密  
	        byte[] keyBytes = decryptBASE64(getPublicKey());  
	  
	        // 取得公钥  
	        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
	        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
	        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
	  
	        // 对数据加密  
	        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
	        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
	  
	        return cipher.doFinal(data);
	        
    	}catch(Exception e){
    		throw new BusinessException(TokenError.TOKEN_ENCRYPT_FAIL);
    	}
    }  
  
    /** 
     * 加密<br> 
     * 用私钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public byte[] encryptByPrivateKey(byte[] data) throws BusinessException {  
    	try{
	        // 对密钥解密  
	        byte[] keyBytes = decryptBASE64(getPrivateKey());  
	  
	        // 取得私钥  
	        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
	        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
	        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
	  
	        // 对数据加密  
	        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
	        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
	  
	        return cipher.doFinal(data);
    	}catch(Exception e){
    		throw new BusinessException(TokenError.TOKEN_ENCRYPT_FAIL);
    	}
    }  
  
    /** 
     * 取得私钥 
     *  
     * @param keyMap 
     * @return 
     * @throws Exception 
     */  
    public String getPrivateKey(){  
    	return encryptBASE64(keyPair.getPrivate().getEncoded());
    }  
  
    /** 
     * 取得公钥 
     *  
     * @param keyMap 
     * @return 
     * @throws Exception 
     */  
    public String getPublicKey() {
    	return encryptBASE64(keyPair.getPublic().getEncoded());
    }  
  
    /** 
     * BASE64解密 
     *  
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public byte[] decryptBASE64(String key){  
        return Base64.decodeBase64(key);
    }  
  
    /** 
     * BASE64加密 
     *  
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public String encryptBASE64(byte[] key){  
        return Base64.encodeBase64String(key);  
    } 
}
