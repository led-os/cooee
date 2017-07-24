package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;


public class MemoryUtils
{
	
	public static long getCurEnabledMemory(
			Context context )
	{
		// 获取android当前可用内存大小
		ActivityManager am = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		context = null;
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo( mi );
		am = null;
		return mi.availMem / ( 1024 * 1024 );
	}
}
