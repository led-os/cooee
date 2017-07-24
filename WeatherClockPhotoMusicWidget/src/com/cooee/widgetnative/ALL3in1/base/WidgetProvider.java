package com.cooee.widgetnative.ALL3in1.base;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.widgetnative.ALL3in1.ClockWeather.ClockManager;
import com.cooee.widgetnative.ALL3in1.ClockWeather.WeatherManager;
import com.cooee.widgetnative.ALL3in1.Photo.PhotoManager;


// AppWidgetProvider 是 BroadcastReceiver 的子类，本质是个 广播接收器，它专门用来接收来自 Widget组件的各种请求(用Intent传递过来)
public class WidgetProvider extends AppWidgetProvider
{
	
	public static final String CLICK_CLOCK_LAYOUT = "com.cooee.widgetnative.ALL3in1.click.clock_layout";
	public static final String CLICK_WEATHER_LAYOUT = "com.cooee.widgetnative.ALL3in1.click.weather_layout";
	public static final String CLICK_DATE_LAYOUT = "com.cooee.widgetnative.ALL3in1.click.date_layout";
	public static final String CLICK_PHOTO_LAYOUT = "com.cooee.widgetnative.ALL3in1.click.photo_layout";
	private final String TAG = "WidgetProvider";
	//
	//音乐
	public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
	public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
	//
	public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
	public static final String META_CHANGED = "com.android.music.metachanged";
	
	//每个请求都会传递给onReceive方法，该方法根据Intent参数中的action类型来决定自己处理还是分发给下面四个特殊的方法。   
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		Log.i( TAG , " cyk widgetProvider --> onReceive intent: " + intent );
		super.onReceive( context , intent );
		String action = intent.getAction();
		//点击相册插件
		if( action.equals( CLICK_PHOTO_LAYOUT ) )
		{
			PhotoManager.getInstance( context ).onclick();
		}
		//点击时钟布局
		else if( action.equals( WidgetProvider.CLICK_CLOCK_LAYOUT ) )
		{
			ClockManager.getInstance( context ).onClickClock();
		}
		//点击日期
		else if( action.equals( WidgetProvider.CLICK_DATE_LAYOUT ) )
		{
			ClockManager.getInstance( context ).onclickDate();
		}
		//点击天气
		else if( action.equals( WidgetProvider.CLICK_WEATHER_LAYOUT ) )
		{
			WeatherManager.getInstance( context ).onClick();
		}
		else
		{
			Log.d( TAG , "cyk 其他" );
			//			context.startService( new Intent( context , WidgetService.class ) );
			//更新插件
			WidgetManager.getInstance( context ).updateAppWidget();
		}
	}
	
	//如果Widget自动更新时间到了、或者其他会导致Widget发生变化的事件发生，或者说Intent的值是android.appwidget.action.APPWIDGET_UPDATE，那么会调用onUpdate，下面三个方法类似   
	@Override
	public void onUpdate(
			Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		//AppWidgetManager 顾名思义是AppWidget的管理器，appWidgetIds 桌面上所有的widget都会被分配一个唯一的ID标识，那么这个数组就是他们的列表   
		Log.i( TAG , " cyk widgetProvider --> onUpdate" );
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		//		context.startService( new Intent( context , WidgetService.class ) );
		WidgetManager.getInstance( context ).updateAppWidget();
	}
	
	//当一个App Widget从桌面上删除时调用   
	@Override
	public void onDeleted(
			Context context ,
			int[] appWidgetIds )
	{
		Log.i( TAG , " cyk widgetProvider --> onDeleted" );
		super.onDeleted( context , appWidgetIds );
	}
	
	//当这个App Widget第一次被放在桌面上时调用(同一个App Widget可以被放在桌面上多次，所以会有这个说法)   
	@Override
	public void onEnabled(
			Context context )
	{
		Log.i( TAG , " cyk widgetProvider --> onEnabled" );
		super.onEnabled( context );
		//		context.startService( new Intent( context , WidgetService.class ) );
		WidgetManager.getInstance( context ).updateAppWidget();
	}
	
	//当这个App Widget的最后一个实例被从桌面上移除时会调用该方法。   
	@Override
	public void onDisabled(
			Context context )
	{
		Log.i( TAG , " cyk widgetProvider --> onDisabled" );
		super.onDisabled( context );
		//停止时钟、音乐服务
		context.stopService( new Intent( context , WidgetService.class ) );
	}
}
