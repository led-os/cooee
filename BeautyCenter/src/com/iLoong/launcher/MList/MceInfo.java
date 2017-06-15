package com.iLoong.launcher.MList;


import com.cooee.shell.sdk.CooeeSdk;

import cool.sdk.common.CoolMethod;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;


public class MceInfo
{
	
	String h01;
	int h02;
	String h03;
	String h04;
	String h05;
	String h06;
	String h07;
	String h08;
	String h09;
	String h10;
	String h11;
	int h12;
	int h13;
	String h16;
	String h18;
	String h19;
	private static MceInfo mceInfo = null;
	
	public static MceInfo instance(
			Context context )
	{
		if( null == mceInfo )
		{
			mceInfo = new MceInfo( context );
		}
		return mceInfo;
	}
	
	public MceInfo(
			Context context )
	{
		// TODO Auto-generated constructor stub
		try
		{
			h01 = context.getPackageName();
		}
		catch( Exception e )
		{
			h01 = "";
		}
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
		try
		{
			h02 = pkgInfo.versionCode;
			h03 = pkgInfo.versionName;
		}
		catch( Exception e )
		{
			h02 = 0;
			h03 = "";
		}
		try
		{
			h04 = CoolMethod.getSn( context );
		}
		catch( Exception e )
		{
			h04 = "";
		}
		try
		{
			h05 = CoolMethod.getAppID( context );
		}
		catch( Exception e )
		{
			h05 = "";
		}
		try
		{
			h06 = CooeeSdk.cooeeGetCooeeId( context );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			h06 = "";
		}
		int simIdx = 1;
		try
		{
			h07 = CooeeSdk.cooeeGetImsi( context , simIdx );
		}
		catch( Exception e )
		{
			h07 = "";
		}
		try
		{
			h08 = CooeeSdk.cooeeGetIccid( context , 0 );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			h08 = "";
		}
		//	try
		//		{
		//			h09 CooeeSdk.cooeeGetPhoneNumber( context , 0 );
		//		}
		//		catch( Exception e )
		//		{
		//			e.printStackTrace();
		//			h09 = "";
		//		}
		h09 = "";
		try
		{
			h10 = CooeeSdk.cooeeGetSmsSc( context , 0 );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			h10 = "";
		}
		try
		{
			h11 = CooeeSdk.cooeeGetLcdWidth( context ) + "*" + CooeeSdk.cooeeGetLcdHeight( context );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			h11 = "";
		}
		h12 = LoadURL.MicrVersion;
		try
		{
			h13 = -1;
		}
		catch( Exception e )
		{
			h13 = -1;
		}
		try
		{
			h16 = CooeeSdk.cooeeGetCurNetworkType( context );
		}
		catch( Exception e )
		{
			h16 = "";
		}
		try
		{
			h18 = CooeeSdk.cooeeGetLAC( context );
		}
		catch( Exception e )
		{
			h18 = "";
		}
		try
		{
			h19 = CooeeSdk.cooeeGetCID( context );
		}
		catch( Exception e )
		{
			h19 = "";
		}
	}
	
	public String getH01()
	{
		return h01;
	}
	
	public int getH02()
	{
		return h02;
	}
	
	public String getH03()
	{
		return h03;
	}
	
	public String getH04()
	{
		return h04;
	}
	
	public String getH05()
	{
		return h05;
	}
	
	public String getH06()
	{
		return h06;
	}
	
	public String getH07()
	{
		return h07;
	}
	
	public String getH08()
	{
		return h08;
	}
	
	public String getH09()
	{
		return h09;
	}
	
	public String getH10()
	{
		return h10;
	}
	
	public String getH11()
	{
		return h11;
	}
	
	public int getH12()
	{
		return h12;
	}
	
	public int getH13()
	{
		return h13;
	}
	
	public String getH16()
	{
		return h16;
	}
	
	public String getH18()
	{
		return h18;
	}
	
	public String getH19()
	{
		return h19;
	}
	//	public static MceInfo getMceInfo()
	//	{
	//		return mceInfo;
	//	}
}
