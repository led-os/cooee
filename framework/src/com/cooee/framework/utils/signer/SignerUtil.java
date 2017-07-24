package com.cooee.framework.utils.signer;


import java.security.MessageDigest;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;


public class SignerUtil
{
	
	/**
	 * Used to build output as Hex
	 */
	private static final char[] DIGITS_LOWER = { '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' };
	
	/**
	 * 获得该context应用的签名的MD5码
	 * @param context
	 * @return
	 */
	public static String getSignerMD5(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		String packageName = context.getPackageName();
		return getSignerMD5( context , packageName );
	}
	
	/**
	 * 获得包名为packageName的应用的签名的MD5码
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getSignerMD5(
			Context context ,
			String packageName )
	{
		String sign = getSigner( context , packageName );
		if( sign == null || sign.length() == 0 )
		{
			return null;
		}
		return md5( sign );
	}
	
	/**
	 * 获得该context应用的签名的字符串
	 * @param context
	 * @return
	 */
	public static String getSigner(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		String packageName = context.getPackageName();
		return getSigner( context , packageName );
	}
	
	/**
	 * 获得包名为packageName的应用的签名的字符串
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getSigner(
			Context context ,
			String packageName )
	{
		Signature[] signatures = getRawSignature( context , packageName );
		if( signatures == null || signatures.length == 0 )
		{
			return null;
		}
		String sign = null;
		StringBuilder builder = new StringBuilder();
		for( Signature signature : signatures )
		{
			builder.append( signature.toCharsString() );
		}
		sign = builder.toString();
		return sign;
	}
	
	/**
	 * 获得该context应用的签名
	 * @param context
	 * @return
	 */
	public static Signature[] getRawSignature(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		String packageName = context.getPackageName();
		return getRawSignature( context , packageName );
	}
	
	/**
	 * 获得包名为packageName的应用的签名
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Signature[] getRawSignature(
			Context context ,
			String packageName )
	{
		if( context == null || packageName == null || packageName.length() == 0 )
		{
			return null;
		}
		PackageInfo info = null;
		PackageManager pm = context.getPackageManager();
		try
		{
			info = pm.getPackageInfo( packageName , PackageManager.GET_SIGNATURES );
		}
		catch( NameNotFoundException e )
		{
			return null;
		}
		if( info == null )
		{
			return null;
		}
		return info.signatures;
	}
	
	/**
	 * encode By MD5
	* @param str
	* @return
	*/
	protected static String md5(
			String str )
	{
		if( str == null )
		{
			return null;
		}
		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance( "MD5" );
			messageDigest.update( str.getBytes() );
			return new String( encodeHex( messageDigest.digest() ) );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	/**
	* Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
	* The returned array will be double the length of the passed array, as it takes two characters to represent any
	* given byte.
	* 
	* @param data a byte[] to convert to Hex characters
	* @return A char[] containing hexadecimal characters
	*/
	protected static char[] encodeHex(
			final byte[] data )
	{
		final int l = data.length;
		final char[] out = new char[l << 1];
		// two characters form the hex value.
		for( int i = 0 , j = 0 ; i < l ; i++ )
		{
			out[j++] = DIGITS_LOWER[( 0xF0 & data[i] ) >>> 4];
			out[j++] = DIGITS_LOWER[0x0F & data[i]];
		}
		return out;
	}
}
