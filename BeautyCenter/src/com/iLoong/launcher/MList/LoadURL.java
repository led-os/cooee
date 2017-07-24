package com.iLoong.launcher.MList;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.cooee.shell.sdk.CooeeSdk;

import cool.sdk.common.CoolMethod;
import cool.sdk.common.DES;


class PhoneInfoma
{
	
	String h1;
	int h2;
	String h3;
	String h4;
	String h5;
	String h6;
	String h7;
	String h8;
	String h9;
	String h10;
	String h11;
	int h12;
}

public class LoadURL
{
	
	static PhoneInfoma phoneInfoma = new PhoneInfoma();
	static final int MicrVersion = 2;
	
	public static String loadUrl(
			Context context )
	{
		try
		{
			String str = "h1:" + phoneInfoma.h1 + "|" + "h2:" + phoneInfoma.h2 + "|" + "h3:" + phoneInfoma.h3 + "|" + "h4:" + phoneInfoma.h4 + "|" + "h5:" + phoneInfoma.h5 + "|" + "h6:" + phoneInfoma.h6 + "|" + "h7:" + phoneInfoma.h7 + "|" + "h8:" + phoneInfoma.h8 + "|" + "h9:" + phoneInfoma.h9 + "|" + "h10:" + phoneInfoma.h10 + "|" + "h11:" + phoneInfoma.h11 + "|" + "h12:" + phoneInfoma.h12 + "|";
			return str;
		}
		catch( Throwable e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String Base64Str(
			Context context ,
			int id )
	{
		String deskey = "8ufD05pL";
		String str = loadUrl( context ) + "h13:" + id;
		byte[] bs = null;
		try
		{
			bs = DES.ecb_encode( deskey , str );
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder( "" );
		// byte[] bs = str.getBytes();
		int bit;
		for( int i = 0 ; i < bs.length ; i++ )
		{
			bit = ( bs[i] & 0x0f0 ) >> 4;
			sb.append( chars[bit] );
			bit = bs[i] & 0x0f;
			sb.append( chars[bit] );
		}
		return sb.toString().trim();
	}
	
	public static String hexStr2Str(
			String hexStr )
	{
		String deskey = "8ufD05pL";
		byte[] bytes = null;
		try
		{
			bytes = DES.ecb_decode( deskey , DES.String2Byte( hexStr ) );
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String( bytes );
	}
	
	// /**
	// * 解密
	// * @param data 待解密字符串
	// * @param key 解密私钥，长度不能够小于8位
	// * @return 解密后的字节数组
	// * @throws Exception 异常
	// */
	public static void initPhoneInfoma(
			Context context )
	{
		int simIdx = 1;
		PackageInfo pkgInfo = null;
		try
		{
			pkgInfo = context.getPackageManager().getPackageInfo( context.getPackageName() , 0 );
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		phoneInfoma.h1 = context.getPackageName();
		phoneInfoma.h2 = pkgInfo.versionCode;
		phoneInfoma.h3 = pkgInfo.versionName;
		try
		{
			phoneInfoma.h4 = CoolMethod.getSn( context );
		}
		catch( Exception e )
		{
			phoneInfoma.h4 = "";
		}
		try
		{
			phoneInfoma.h5 = CoolMethod.getAppID( context );
		}
		catch( Exception e )
		{
			phoneInfoma.h5 = "";
		}
		try
		{
			phoneInfoma.h6 = CooeeSdk.cooeeGetCooeeId( context );
		}
		catch( Exception e )
		{
			phoneInfoma.h6 = "";
		}
		try
		{
			phoneInfoma.h7 = CooeeSdk.cooeeGetImsi( context , simIdx );
		}
		catch( Exception e )
		{
			phoneInfoma.h7 = "";
		}
		try
		{
			phoneInfoma.h8 = CooeeSdk.cooeeGetIccid( context , simIdx );
		}
		catch( Exception e )
		{
			phoneInfoma.h8 = "";
		}
		try
		{
			//不采集
			//phoneInfoma.h9 = CooeeSdk.cooeeGetPhoneNumber( context , simIdx );
			phoneInfoma.h9 = "";
		}
		catch( Exception e )
		{
			phoneInfoma.h9 = "";
		}
		try
		{
			phoneInfoma.h10 = CooeeSdk.cooeeGetSmsSc( context , simIdx );
		}
		catch( Exception e )
		{
			phoneInfoma.h10 = "";
		}
		try
		{
			phoneInfoma.h11 = CooeeSdk.cooeeGetLcdWidth( context ) + "*" + CooeeSdk.cooeeGetLcdHeight( context );
		}
		catch( Exception e )
		{
			phoneInfoma.h11 = "";
		}
		phoneInfoma.h12 = MicrVersion;
	}
}
