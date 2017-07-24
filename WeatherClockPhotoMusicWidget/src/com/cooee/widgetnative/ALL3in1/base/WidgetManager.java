package com.cooee.widgetnative.ALL3in1.base;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.cooee.widgetnative.ALL3in1.R;
import com.cooee.widgetnative.ALL3in1.ClockWeather.ClockManager;
import com.cooee.widgetnative.ALL3in1.ClockWeather.ClockWeatherManager;
import com.cooee.widgetnative.ALL3in1.ClockWeather.WeatherManager;
import com.cooee.widgetnative.ALL3in1.Music.MusicManager;
import com.cooee.widgetnative.ALL3in1.Photo.PhotoManager;


public class WidgetManager
{
	
	public Context mContext;
	public static WidgetManager mWidgetManager;
	public static final String TAG = "WidgetManager";
	private RemoteViews mRemoteViews;
	
	private WidgetManager(
			Context mContext )
	{
		this.mContext = mContext;
		//初始化view
		initRemoteViews();
	}
	
	public static WidgetManager getInstance(
			Context context )
	{
		if( mWidgetManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mWidgetManager == null && context != null )
				{
					mWidgetManager = new WidgetManager( context );
				}
			}
		}
		return mWidgetManager;
	}
	
	public RemoteViews getRemoteViews()
	{
		return mRemoteViews;
	}
	
	private void initRemoteViews()
	{
		mRemoteViews = new RemoteViews( mContext.getPackageName() , R.layout.widget_layout );
	}
	
	public synchronized void updateAppWidget()
	{
		Log.d( TAG , "cyk updateAppWidget start " );
		initRemoteViews();
		//同步view显示状态
		initWidgetView( mContext );
		//更新插件
		AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance( mContext );
		int[] appIds = mAppWidgetManager.getAppWidgetIds( new ComponentName( mContext , WidgetProvider.class ) );
		mAppWidgetManager.updateAppWidget( appIds , mRemoteViews );
		Log.d( TAG , "cyk updateAppWidget end " );
	}
	
	public void initWidgetView(
			Context context )
	{
		//初始化时钟插件
		ClockWeatherManager mClockWeatherManager = ClockWeatherManager.getInstance( context );
		mClockWeatherManager.initView();
		if( mClockWeatherManager.showClockVeiw )
		{
			ClockManager mClockManager = ClockManager.getInstance( context );
			mClockManager.isLanguage();
			mClockManager.clockTimeChanged();
			mClockManager.updateClockView();
		}
		if( mClockWeatherManager.showWeatherVeiw )
		{
			WeatherManager mWeatherManager = WeatherManager.getInstance( context );
			mWeatherManager.initConfig();
			mWeatherManager.initView();
			mWeatherManager.updateWeatherView();
		}
		//初始化相册
		PhotoManager mPhotoManager = PhotoManager.getInstance( context );
		mPhotoManager.initRemoteViews( context );
		mPhotoManager.setLastBitmap();
		//初始化音乐插件
		MusicManager mMusicManager = MusicManager.getInstance( context );
		mMusicManager.initView( context );
		mMusicManager.changeAlbumArtClickState( context );
		mMusicManager.initLastMusic();
		//启动时钟、音乐服务
		context.startService( new Intent( context , WidgetService.class ) );
	}
}
