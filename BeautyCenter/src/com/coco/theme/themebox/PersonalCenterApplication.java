package com.coco.theme.themebox;


import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.coco.theme.themebox.util.FunctionConfig;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.support.stub.MultiDexUtil;


public class PersonalCenterApplication extends Application
{
	
	
	public static final String TAG = "PersonalCenterApplication";
	private String currentLocale;
	
	@Override
	protected void attachBaseContext(
			Context base )
	{
		super.attachBaseContext( base );
		Log.d( TAG , "StubApplication onCreate() initMultiDex start" );
		long begin = System.currentTimeMillis();
		MultiDexUtil.initMultiDex( getBaseContext() );
		long end = System.currentTimeMillis();
		Log.d( TAG , "need time " + ( end - begin ) );
		Log.d( TAG , "StubApplication onCreate() initMultiDex end" );
	}
	
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub		
		super.onCreate();
		currentLocale = this.getResources().getConfiguration().locale.toString();
		//20140512<add by liuhailin begin>
		// This configuration tuning is custom. You can tune every option, you may tune some of them, 
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder( getApplicationContext() ).threadPriority( Thread.NORM_PRIORITY - 2 ).denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator( new Md5FileNameGenerator() ).discCacheFileCount( 1000 )//Set max cache file count in SD card
				.tasksProcessingOrder( QueueProcessingType.LIFO ).enableLogging() // Not necessary in common
				.build();
		//Initialize ImageLoader with configuration
		ImageLoader.getInstance().init( config );
		//20140512<add by liuhailin end>
	}
	
	@Override
	public void onConfigurationChanged(
			Configuration newConfig )
	{
		// TODO Auto-generated method stub
		super.onConfigurationChanged( newConfig );
		if( currentLocale != null )
		{
			if( !currentLocale.equals( newConfig.locale.toString() ) )
			{
				// @2015/03/11 ADD START 友盟统计美华中心退出
				if( FunctionConfig.isUmengStatistics_key() )
				{
					MainActivity.statisticsExitBeautyCenter( this );
				}
				// @2015/03/11 ADD END
				ActivityManager.KillActivity();
				currentLocale = newConfig.locale.toString();
			}
		}
	}
}
