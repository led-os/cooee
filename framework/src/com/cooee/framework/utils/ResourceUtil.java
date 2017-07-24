package com.cooee.framework.utils;


import android.content.Context;


public class ResourceUtil
{
	
	public static int getResourceId(
			Context context ,
			String name ,
			String defType )
	{
		if( context == null )
		{
			return 0;
		}
		return context.getResources().getIdentifier( name , defType , context.getPackageName() );
	}
}
