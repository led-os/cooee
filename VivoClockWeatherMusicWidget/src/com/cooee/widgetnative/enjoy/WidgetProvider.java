package com.cooee.widgetnative.enjoy;


import com.cooee.widgetnative.enjoy.manager.ClockManager;
import com.cooee.widgetnative.enjoy.manager.WeatherManager;
import com.cooee.widgetnative.enjoy.manager.WidgetViewManager;
import com.cooee.widgetnative.enjoy.service.ClockWeatherMusicService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;


public class WidgetProvider extends AppWidgetProvider
{
	
	private final String TAG = "WidgetProvider";
	public static final String CLICK_CLOCK_LAYOUT = "com.cooee.widgetnative.enjoy.click_clock_layout";
	public static final String CLICK_WEATHER_LAYOUT = "com.cooee.widgetnative.enjoy.click_weather_layout";
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		super.onReceive( context , intent );
		String action = intent.getAction();
		//点击时钟
		if( CLICK_CLOCK_LAYOUT.equals( action ) )
		{
			ClockManager.getInstance( context ).onClick();
		}
		else if( CLICK_WEATHER_LAYOUT.equals( action ) )
		{
			WeatherManager.getInstance( context ).onClick();
		}
		else
		{
			//其他的都启动service,确保service在运行
			WidgetViewManager.getWidgetViewManager( context ).updateAllWidget();
		}
	}
	
	@Override
	public void onEnabled(
			Context context )
	{
		super.onEnabled( context );
		WidgetViewManager.getWidgetViewManager( context ).updateAllWidget();
	}
	
	@Override
	public void onUpdate(
			Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		WidgetViewManager.getWidgetViewManager( context ).updateAllWidget();
	}
	
	@Override
	public void onDisabled(
			Context context )
	{
		super.onDisabled( context );
		context.stopService( new Intent( context , ClockWeatherMusicService.class ) );
	}
}
