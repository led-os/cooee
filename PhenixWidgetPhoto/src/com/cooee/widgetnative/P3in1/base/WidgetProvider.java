package com.cooee.widgetnative.P3in1.base;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


// AppWidgetProvider 是 BroadcastReceiver 的子类，本质是个 广播接收器，它专门用来接收来自 Widget组件的各种请求(用Intent传递过来)
public class WidgetProvider extends AppWidgetProvider
{
	
	public static final String CLICK_PHOTO_SHOW = "com.cooee.widgetnative.P3in1.click.photo_layout";
	private final String TAG = "WidgetProvider";
	
	//每个请求都会传递给onReceive方法，该方法根据Intent参数中的action类型来决定自己处理还是分发给下面四个特殊的方法。   
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		Log.i( TAG , "cyk widgetProvider --> onReceive intent: " + intent );
		super.onReceive( context , intent );
		final Context mcontext = context;
		//点击小部件
		if( intent.getAction().equals( CLICK_PHOTO_SHOW ) )
		{
			WidgetManager.getInstance( mcontext ).onclick();
		}
		else
		{
			WidgetManager.getInstance( mcontext ).updateAppWidget( context );
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
		Log.i( TAG , "cyk widgetProvider --> onUpdate" );
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		WidgetManager.getInstance( context ).updateAppWidget( context );
		//添加插件统计
		WidgetManager.getInstance( context ).doStatistics();
	}
	
	//当一个App Widget从桌面上删除时调用   
	@Override
	public void onDeleted(
			Context context ,
			int[] appWidgetIds )
	{
		Log.i( TAG , "cyk widgetProvider --> onDeleted" );
		super.onDeleted( context , appWidgetIds );
	}
	
	//当这个App Widget第一次被放在桌面上时调用(同一个App Widget可以被放在桌面上多次，所以会有这个说法)   
	@Override
	public void onEnabled(
			Context context )
	{
		Log.i( TAG , "cyk widgetProvider --> onEnabled" );
		super.onEnabled( context );
	}
	
	//当这个App Widget的最后一个实例被从桌面上移除时会调用该方法。   
	@Override
	public void onDisabled(
			Context context )
	{
		Log.i( TAG , "cyk widgetProvider --> onDisabled" );
		super.onDisabled( context );
	}
}
