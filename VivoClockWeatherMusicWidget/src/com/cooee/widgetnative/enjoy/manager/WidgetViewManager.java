package com.cooee.widgetnative.enjoy.manager;


import com.cooee.widgetnative.enjoy.R;
import com.cooee.widgetnative.enjoy.WidgetProvider;
import com.cooee.widgetnative.enjoy.service.ClockWeatherMusicService;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


public class WidgetViewManager
{
	
	private static WidgetViewManager mWidgetViewManager;
	private static String TAG = "ClockManager";
	private RemoteViews remoteview;
	private Context mContext;
	
	private WidgetViewManager(
			Context mContext )
	{
		this.mContext = mContext;
		initConfig();
	}
	
	public static WidgetViewManager getWidgetViewManager(
			Context mContext )
	{
		if( mWidgetViewManager == null )
		{
			synchronized( TAG )
			{
				if( mWidgetViewManager == null )
				{
					mWidgetViewManager = new WidgetViewManager( mContext );
				}
			}
		}
		return mWidgetViewManager;
	}
	
	private void initConfig()
	{
		ClockManager.getInstance( mContext );
		WeatherManager.getInstance( mContext );
		MusicManager.getInstance( mContext );
	}
	
	/**每次update创建一个remoteview*/
	private void initRemoteView()
	{
		remoteview = new RemoteViews( mContext.getPackageName() , R.layout.widget_layout );
	}
	
	/**初始化view点击事件和是否可见*/
	private void initClickView()
	{
		if( remoteview != null )
		{
			//时钟
			ClockManager.getInstance( mContext ).initClickView( remoteview );
			//天气
			WeatherManager.getInstance( mContext ).initClickView( remoteview );
			//音乐
			MusicManager.getInstance( mContext ).initClickView( remoteview );
		}
	}
	
	/**widget更新*/
	public void updateAllWidget()
	{
		Log.d( TAG , "cyk updateAllWidget " );
		initRemoteView();
		initClickView();
		//更新时钟
		ClockManager.getInstance( mContext ).updateAllWidget( remoteview );
		//更新天气
		WeatherManager.getInstance( mContext ).updateAllWidget( remoteview );
		//更新音乐
		MusicManager.getInstance( mContext ).updateAllWidget( remoteview );
		//更新插件
		AppWidgetManager appWidgetManger = AppWidgetManager.getInstance( mContext );
		int[] appIds = appWidgetManger.getAppWidgetIds( new ComponentName( mContext , WidgetProvider.class ) );
		appWidgetManger.updateAppWidget( appIds , remoteview );
		//
		mContext.startService( new Intent( mContext , ClockWeatherMusicService.class ) );
	}
}
