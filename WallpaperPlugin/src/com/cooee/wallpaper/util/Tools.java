package com.cooee.wallpaper.util;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;


public class Tools
{
	
	public static boolean isSDCardExist()
	{
		if( android.os.Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isApkInstalled(
			Context context ,
			ComponentName mComponentName )
	{
		if( ( context == null ) || ( mComponentName == null ) || ( "".equals( mComponentName ) ) )
		{
			return false;
		}
		PackageManager mPackageManager = context.getPackageManager();
		Intent intent = new Intent();
		intent.setComponent( mComponentName );
		if( mPackageManager.queryIntentActivities( intent , 0 ).size() == 0 )
		{
			return false;
		}
		return true;
	}
	
	public static int dip2px(
			Context context ,
			float dipValue )
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)( dipValue * scale + 0.5f );
	}
}
