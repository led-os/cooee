package com.cooee.framework.utils;


import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;


public class StringUtils
{
	
	//	static StringBuffer mStringBuffer = null;//线程安全
	static StringBuilder mStringBuilder = null;//非线程安全
	private static Object mSynLock = new Object();
	
	static public String concat(
			Object ... args )
	{
		synchronized( mSynLock )
		{
			if( mStringBuilder == null )
			{
				mStringBuilder = new StringBuilder();
			}
			mStringBuilder.delete( 0 , mStringBuilder.length() );
			for( Object arg : args )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					//			if( arg == null )
					//			{
					//				Log.v( "StringUtils - concat" , "arg is null" );
					//			}
					//			else
					//			{
					//				Log.v( "StringUtils - concat" , arg.getClass().toString() );
					//				Log.v( "StringUtils - concat" , arg.toString() );
					//			}
				}
				mStringBuilder.append( arg );
			}
			return mStringBuilder.toString();
		}
	}
}
