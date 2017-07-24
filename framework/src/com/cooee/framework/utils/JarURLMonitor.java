package com.cooee.framework.utils;


import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarFile;

import android.content.Context;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;


// hack to remove memory leak in JarURLConnectionImpl
// from http://stackoverflow.com/questions/14610350/android-memory-leak-in-apache-harmonys-jarurlconnectionimpl
public class JarURLMonitor
{
	
	private static final String TAG = "JarURLMonitor";
	private static JarURLMonitor mInstance;
	private Field jarCacheField;
	
	public static JarURLMonitor getInstance(
			Context context )
	{
		if( mInstance == null )
		{
			synchronized( JarURLMonitor.class )
			{
				if( mInstance == null )
				{
					mInstance = new JarURLMonitor( context );
				}
			}
		}
		return mInstance;
	}
	
	public void cleanCache()
	{
		new Thread( "JarURLMonitor" ) {
			
			@Override
			public void run()
			{
				try
				{
					mInstance.checkJarCache();
				}
				catch( Exception e )
				{
					// log
				}
			}
		}.start();
	}
	
	private JarURLMonitor(
			Context context )
	{
		// get jar cache field
		try
		{
			final Class<?> cls = Class.forName( "libcore.net.url.JarURLConnectionImpl" );
			jarCacheField = cls.getDeclaredField( "jarCache" );
			jarCacheField.setAccessible( true );
		}
		catch( Exception e )
		{
			// log
		}
	}
	
	private void checkJarCache() throws Exception
	{
		@SuppressWarnings( "unchecked" )
		final HashMap<URL , JarFile> jarCache = (HashMap<URL , JarFile>)jarCacheField.get( null );
		final Iterator<Map.Entry<URL , JarFile>> iterator = jarCache.entrySet().iterator();
		while( iterator.hasNext() )
		{
			final Map.Entry<URL , JarFile> entry = iterator.next();
			final JarFile jarFile = entry.getValue();
			final String file = jarFile.getName();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( " checkJarCache " , file ) );
			if( file.endsWith( "apk" ) || file.endsWith( "KmobAdSdk.jar" ) )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( " checkJarCache match " , file ) );
				try
				{
					jarFile.close();
					iterator.remove();
				}
				catch( Exception e )
				{
					// log
				}
			}
		}
	}
}
