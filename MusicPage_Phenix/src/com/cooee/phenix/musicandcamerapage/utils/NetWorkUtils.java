package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class NetWorkUtils
{
	
	public static boolean isNetworkAvailable(
			Context context )
	{
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		context = null;
		if( cm == null )
		{
			return false;
		}
		NetworkInfo[] info = cm.getAllNetworkInfo();
		cm = null;
		if( info != null )
		{
			for( int i = 0 ; i < info.length ; i++ )
			{
				if( info[i] != null )
				{
					if( info[i].getState() == NetworkInfo.State.CONNECTED )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , StringUtils.concat( "isNetworkAvailable , info[i]:" , info[i].getTypeName() ) );
						info = null;
						return true;
					}
				}
			}
		}
		info = null;
		return false;
	}
}
