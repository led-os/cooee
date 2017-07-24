package com.iLoong.launcher.MList;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.cooee.shell.sdk.CooeeSdk;


public class PhoneInfo
{
	
	String a00;
	String a01;
	String a02;
	String a03;
	String a04;
	String a05;
	String a06;
	String a07;
	String a08;
	String a09;
	String a10;
	String a11;
	String a12;
	String a13;
	String a14;
	String a15;
	String a16;
	String a17;
	String a19;
	String a18;
	String a20;
	String a21;
	int width;
	int hight;
	private static PhoneInfo phoneInfo = null;
	
	public static PhoneInfo instance(
			Context context )
	{
		// TODO Auto-generated method stub
		if( null == phoneInfo )
		{
			phoneInfo = new PhoneInfo( context );
		}
		return phoneInfo;
	}
	
	public PhoneInfo(
			Context context )
	{
		// TODO Auto-generated constructor stub
		initPhoneInfo( context );
	}
	
	public void initPhoneInfo(
			Context context )
	{
		try
		{
			a00 = URLEncoder.encode( CooeeSdk.cooeeGetCooeeId( context ) , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a00 = "";
		}
		try
		{
			a01 = URLEncoder.encode( CooeeSdk.cooeeGetBuildModel() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a01 = "";
		}
		try
		{
			a02 = URLEncoder.encode( CooeeSdk.cooeeGetBuildDisplay() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a02 = "";
		}
		try
		{
			a03 = URLEncoder.encode( CooeeSdk.cooeeGetCustomVersion() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a03 = "";
		}
		try
		{
			a04 = URLEncoder
					.encode(
							( CooeeSdk.cooeeGetLcdWidth( context ) > CooeeSdk.cooeeGetLcdHeight( context ) ) ? ( CooeeSdk.cooeeGetLcdWidth( context ) + "*" + CooeeSdk.cooeeGetLcdHeight( context ) ) : ( CooeeSdk
									.cooeeGetLcdHeight( context ) + "*" + CooeeSdk.cooeeGetLcdWidth( context ) ) ,
							"UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
		}
		try
		{
			a05 = URLEncoder.encode( CooeeSdk.cooeeGetBuildProduct() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a05 = "";
		}
		WindowManager wm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		DisplayMetrics dm = new DisplayMetrics();
		// 获取屏幕信息
		wm.getDefaultDisplay().getMetrics( dm );
		int screenWidth = (int)( dm.widthPixels / dm.density );
		int screenHeight = (int)( dm.heightPixels / dm.density );
		width = screenWidth;
		hight = screenHeight;
		try
		{
			a06 = URLEncoder.encode( CooeeSdk.cooeeGetBuildDevice() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a06 = "";
		}
		try
		{
			a07 = URLEncoder.encode( CooeeSdk.cooeeGetBuildBoard() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a07 = "";
		}
		try
		{
			a08 = URLEncoder.encode( CooeeSdk.cooeeGetBuildManufacturer() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a08 = "";
		}
		try
		{
			a09 = URLEncoder.encode( CooeeSdk.cooeeGetBuildBrand() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a09 = "";
		}
		try
		{
			a10 = URLEncoder.encode( CooeeSdk.cooeeGetBuildBootloader() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a10 = "";
		}
		try
		{
			a11 = URLEncoder.encode( CooeeSdk.cooeeGetBuildBaseband() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a11 = "";
		}
		try
		{
			a12 = URLEncoder.encode( CooeeSdk.cooeeGetBuildHardware() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a12 = "";
		}
		try
		{
			a13 = URLEncoder.encode( CooeeSdk.cooeeGetSerialno() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a13 = "";
		}
		try
		{
			a14 = URLEncoder.encode( CooeeSdk.cooeeGetBuildVersionRelease() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a14 = "";
		}
		try
		{
			a15 = URLEncoder.encode( Integer.toString( CooeeSdk.cooeeGetVersionSdkInt() ) , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a15 = "";
		}
		try
		{
			a16 = URLEncoder.encode( CooeeSdk.cooeeGetKernelVersion() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a16 = "";
		}
		try
		{
			a17 = URLEncoder.encode( CooeeSdk.cooeeGetVersionIncremental() , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a17 = "";
		}
		try
		{
			a18 = URLEncoder.encode( Long.toString( CooeeSdk.cooeeGetTotalMem() ) , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a18 = "";
		}
		try
		{
			a19 = URLEncoder.encode( Long.toString( CooeeSdk.cooeeGetAvailMem( context ) ) , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a19 = null;
		}
		try
		{
			a20 = URLEncoder.encode( Long.toString( CooeeSdk.cooeeGetTotalRom() ) , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a20 = "";
		}
		try
		{
			a21 = URLEncoder.encode( Long.toString( CooeeSdk.cooeeGetAvailRom() ) , "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			a21 = null;
		}
	}
	
	public String getA00()
	{
		return a00;
	}
	
	public String getA01()
	{
		return a01;
	}
	
	public String getA02()
	{
		return a02;
	}
	
	public String getA03()
	{
		return a03;
	}
	
	public String getA04()
	{
		return a04;
	}
	
	public String getA05()
	{
		return a05;
	}
	
	public String getA06()
	{
		return a06;
	}
	
	public String getA07()
	{
		return a07;
	}
	
	public String getA08()
	{
		return a08;
	}
	
	public String getA09()
	{
		return a09;
	}
	
	public String getA10()
	{
		return a10;
	}
	
	public String getA11()
	{
		return a11;
	}
	
	public String getA12()
	{
		return a12;
	}
	
	public String getA13()
	{
		return a13;
	}
	
	public String getA14()
	{
		return a14;
	}
	
	public String getA15()
	{
		return a15;
	}
	
	public String getA16()
	{
		return a16;
	}
	
	public String getA17()
	{
		return a17;
	}
	
	public String getA18()
	{
		return a18;
	}
	
	public String getA19()
	{
		return a19;
	}
	
	public String getA20()
	{
		return a20;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHight()
	{
		return hight;
	}
	
	public String getA21()
	{
		return a21;
	}
}
