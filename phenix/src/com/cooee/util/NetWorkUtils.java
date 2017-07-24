package com.cooee.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


// 智能分类添加运营 , change by shlt@2014/12/19 ADD START
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
