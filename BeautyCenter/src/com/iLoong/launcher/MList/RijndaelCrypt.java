package com.iLoong.launcher.MList;


import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
// import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.protocol.HTTP;

import cool.sdk.common.DES;


public class RijndaelCrypt
{
	
	public static final String TAG = "ME_RTFSC";
	private static String TRANSFORMATION = "AES/CBC/PKCS5Padding";// "AES/ECB/PKCS5Padding";
	private static String ALGORITHM = "AES";
	private static Cipher _cipher;
	private static SecretKey _password;
	private static IvParameterSpec _IVParamSpec;
	public static byte[] IV = "OTAPLUS630764DNA".getBytes();// "OTAPLUS897664DNA".getBytes();
	public static String PWD = "2LFD8EAF25OB4DA3BFB2534250AA6EYR";// "2lFD8EAF25OB4DA398O761ECD2437l07";
	
	/**
	 * Constructor
	 * 
	 * @password Public key
	 */
	public RijndaelCrypt(
			String password ,
			byte[] IV )
	{
		try
		{
			// Encode digest
			// MessageDigest digest;
			// digest = MessageDigest.getInstance(DIGEST);
			_password = new SecretKeySpec(
			/* digest.digest( */password.getBytes()/* ) */, ALGORITHM );
			// Initialize objects
			_cipher = Cipher.getInstance( TRANSFORMATION );
			_IVParamSpec = new IvParameterSpec( IV );
		}
		catch( NoSuchAlgorithmException e )
		{
			Log.e( TAG , "No such algorithm " + ALGORITHM , e );
		}
		catch( NoSuchPaddingException e )
		{
			Log.e( TAG , "No such padding PKCS7" , e );
		}
	}
	
	/**
	 * Encryptor.
	 * 
	 * @text bytes to be encrypted
	 * @return Base64 encrypted text
	 */
	public byte[] encryptToByteArray(
			byte[] text )
	{
		byte[] encryptedData;
		try
		{
			_cipher.init( Cipher.ENCRYPT_MODE , _password , _IVParamSpec );
			encryptedData = _cipher.doFinal( text );
		}
		catch( InvalidKeyException e )
		{
			Log.e( TAG , "Invalid key  (invalid encoding, wrong length, uninitialized, etc)." , e );
			return null;
		}
		catch( InvalidAlgorithmParameterException e )
		{
			Log.e( TAG , "Invalid or inappropriate algorithm parameters for " + ALGORITHM , e );
			return null;
		}
		catch( IllegalBlockSizeException e )
		{
			Log.e( TAG , "The length of data provided to a block cipher is incorrect" , e );
			return null;
		}
		catch( BadPaddingException e )
		{
			Log.e( TAG , "The input data but the data is not padded properly." , e );
			return null;
		}
		return encryptedData;
	}
	
	/**
	 * Encryptor.
	 * 
	 * @text bytes to be encrypted
	 * @return Base64 encrypted text
	 */
	public String encrypt(
			byte[] text )
	{
		byte[] encryptedData;
		try
		{
			_cipher.init( Cipher.ENCRYPT_MODE , _password , _IVParamSpec );
			encryptedData = _cipher.doFinal( text );
		}
		catch( InvalidKeyException e )
		{
			Log.e( TAG , "Invalid key  (invalid encoding, wrong length, uninitialized, etc)." , e );
			return null;
		}
		catch( InvalidAlgorithmParameterException e )
		{
			Log.e( TAG , "Invalid or inappropriate algorithm parameters for " + ALGORITHM , e );
			return null;
		}
		catch( IllegalBlockSizeException e )
		{
			Log.e( TAG , "The length of data provided to a block cipher is incorrect" , e );
			return null;
		}
		catch( BadPaddingException e )
		{
			Log.e( TAG , "The input data but the data is not padded properly." , e );
			return null;
		}
		return Base64.encodeToString( encryptedData , Base64.NO_WRAP );
		// String newStr = new String(encryptedData);
		// String newStr2 = Base64.encodeToString(encryptedData,
		// Base64.NO_WRAP);
		// return newStr;
	}
	
	/**
	 * Encryptor.
	 * 
	 * @text String to be encrypted
	 * @return Base64 encrypted text
	 */
	public String encrypt(
			String text )
	{
		return encrypt( text.getBytes() );
	}
	
	/**
	 * Decryptor.
	 * 
	 * @text Base64 bytes to be decrypted
	 * @return decrypted text
	 */
	public String decrypt(
			byte[] text )
	{
		try
		{
			_cipher.init( Cipher.DECRYPT_MODE , _password , _IVParamSpec );
			byte[] decryptedVal = _cipher.doFinal( text );
			return new String( decryptedVal );
		}
		catch( InvalidKeyException e )
		{
			Log.e( TAG , "Invalid key  (invalid encoding, wrong length, uninitialized, etc)." , e );
			return null;
		}
		catch( InvalidAlgorithmParameterException e )
		{
			Log.e( TAG , "Invalid or inappropriate algorithm parameters for " + ALGORITHM , e );
			return null;
		}
		catch( IllegalBlockSizeException e )
		{
			Log.e( TAG , "The length of data provided to a block cipher is incorrect" , e );
			return null;
		}
		catch( BadPaddingException e )
		{
			Log.e( TAG , "The input data but the data is not padded properly." , e );
			return null;
		}
	}
	
	/**
	 * Decryptor.
	 * 
	 * @text Base64 string to be decrypted
	 * @return decrypted text
	 */
	public String decrypt(
			String text )
	{
		return decrypt( text.getBytes() );
	}
	
	public static void test()
	{
		String testStr = "sfjdsfdfjojfoerj";
		byte[] encryStr = null;
		String decryStr = null;
		RijndaelCrypt aes = new RijndaelCrypt( RijndaelCrypt.PWD , RijndaelCrypt.IV );
		try
		{
			encryStr = aes.encryptToByteArray( testStr.toString().getBytes( HTTP.UTF_8 ) );
			decryStr = aes.decrypt( encryStr );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
