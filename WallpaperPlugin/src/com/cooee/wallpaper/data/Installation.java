package com.cooee.wallpaper.data;


import android.content.Context;

import com.uuid.control.UuidControl;


public class Installation
{
	
	public synchronized static String id(
			Context context )
	{
		//		return getMyUUID( context );
		return UuidControl.getUniqueID( context );
	}
	//	public static String getMyUUID(
	//			Context context )
	//	{
	//		String androidId = "" + android.provider.Settings.Secure.getString( context.getContentResolver() , android.provider.Settings.Secure.ANDROID_ID );
	//		UUID deviceUuid = new UUID( androidId.hashCode() , androidId.hashCode() );
	//		String uniqueId = deviceUuid.toString();
	//		return uniqueId;
	//	}
}
