package com.netease.util;


/**
 * ����push�����еļ���, �����������
 * @author phily
 *
 */
public class RSA {
	
	public native static void RsaCreateInstance();

	public synchronized native static void RsaReleaseInstance();

	public native static void RsaSetup();

	public native static byte[] RsaProcess(String rsadata);

	static {
	    try{
	        System.loadLibrary("rsa-jni");
	        
	    } catch(Error e){
	        e.printStackTrace();
	    } catch(Exception ep){
	        ep.printStackTrace();
        }
	}
    
//    static String mModulus = null;
//    static String mPrivateExponet = null;
//    
//    public static void RsaCreateInstance() {
//        
//    }
//
//    public synchronized static void RsaReleaseInstance() {
//        
//    }
//
//    public static void RsaSetup(String exp, String mod) {
//        mModulus = mod;
//        mPrivateExponet = exp;
//    }
//
//    public static byte[] RsaProcess(byte[] rsadata) throws Exception {
//        PrivateKey privateKey = getPrivateKey(mModulus, mPrivateExponet);
//        Cipher cipher = Cipher.getInstance("RSA");
//
//        cipher.init(Cipher.DECRYPT_MODE, privateKey);
//        byte[] deBytes = cipher.doFinal(rsadata);
//        
//        return deBytes;
//    }
//    
//    public static PrivateKey getPrivateKey(String modulus, String privateExponent) throws Exception {
//        BigInteger m = new BigInteger(modulus, 16);
//        BigInteger e = new BigInteger(privateExponent, 16);
//
//        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//        
//        return privateKey;
//    }

}
