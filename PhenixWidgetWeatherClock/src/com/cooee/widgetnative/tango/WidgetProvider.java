package com.cooee.widgetnative.tango;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.widgetnative.tango.manager.ClockAndCalendarService;
import com.cooee.widgetnative.tango.manager.TwinkleClockwidgetManager;


// AppWidgetProvider 是 BroadcastReceiver 的子类，本质是个 广播接收器，它专门用来接收来自 Widget组件的各种请求(用Intent传递过来)
public class WidgetProvider extends AppWidgetProvider
{
	
	public static final String CLICK_CLOCK_LAYOUT = "com.cooee.phenix.widget.weather_clock.click.clock_layout";
	public static final String CLICK_WEATHER_LAYOUT = "com.cooee.phenix.widget.weather_clock.click.weather_layout";
	public static final String CLICK_DATE_LAYOUT = "com.cooee.phenix.widget.weather_clock.click.date_layout";
	private final String TAG = "WidgetProvider";
	
	//每个请求都会传递给onReceive方法，该方法根据Intent参数中的action类型来决定自己处理还是分发给下面四个特殊的方法。   
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		Log.i( TAG , "cyk  widgetProvider --> onReceive intent: " + intent );
		super.onReceive( context , intent );
		TwinkleClockwidgetManager.getInstance( context ).onClick( intent.getAction() );
		TwinkleClockwidgetManager.getInstance( context ).updateAppWidget();
	}
	
	//如果Widget自动更新时间到了、或者其他会导致Widget发生变化的事件发生，或者说Intent的值是android.appwidget.action.APPWIDGET_UPDATE，那么会调用onUpdate，下面三个方法类似   
	@Override
	public void onUpdate(
			Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		//AppWidgetManager 顾名思义是AppWidget的管理器，appWidgetIds 桌面上所有的widget都会被分配一个唯一的ID标识，那么这个数组就是他们的列表   
		Log.i( TAG , "cyk  widgetProvider --> onUpdate" );
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		TwinkleClockwidgetManager.getInstance( context ).updateAppWidget();
	}
	
	//当一个App Widget从桌面上删除时调用   
	@Override
	public void onDeleted(
			Context context ,
			int[] appWidgetIds )
	{
		Log.i( TAG , "cyk  widgetProvider --> onDeleted" );
		super.onDeleted( context , appWidgetIds );
	}
	
	//当这个App Widget第一次被放在桌面上时调用(同一个App Widget可以被放在桌面上多次，所以会有这个说法)   
	@Override
	public void onEnabled(
			Context context )
	{
		Log.i( TAG , "cyk  widgetProvider --> onEnabled" );
		super.onEnabled( context );
		TwinkleClockwidgetManager.getInstance( context ).updateAppWidget();
	}
	
	//当这个App Widget的最后一个实例被从桌面上移除时会调用该方法。   
	@Override
	public void onDisabled(
			Context context )
	{
		Log.i( TAG , "cyk  widgetProvider --> onDisabled" );
		super.onDisabled( context );
		TwinkleClockwidgetManager instance = TwinkleClockwidgetManager.getInstance( context );
		if( instance.showClockVeiw )
		{
			context.stopService( new Intent( context , ClockAndCalendarService.class ) );
		}
	}
}
