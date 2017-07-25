package com.cooee.widgetnative.tango.manager;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.widgetnative.tango.R;
import com.cooee.widgetnative.tango.WidgetProvider;


public class TwinkleClockwidgetManager
{
	
	public Context mContext;
	public static float WIDGET_WIDTH = 260;
	public static float WIDGET_HEIGHT = 260;
	public boolean showWeatherVeiw = true;
	public boolean showClockVeiw = true;
	/***/
	private RemoteViews rv;
	public static TwinkleClockwidgetManager mTwinkleClockwidgetManager;
	public static final String TAG = "TwinkleClockwidgetManager";
	
	private TwinkleClockwidgetManager(
			Context mContext )
	{
		this.mContext = mContext;
		initConfig();
		initView();
	}
	
	public static TwinkleClockwidgetManager getInstance(
			Context context )
	{
		if( mTwinkleClockwidgetManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mTwinkleClockwidgetManager == null && context != null )
				{
					mTwinkleClockwidgetManager = new TwinkleClockwidgetManager( context );
				}
			}
		}
		return mTwinkleClockwidgetManager;
	}
	
	private void initConfig()
	{
		WIDGET_WIDTH = mContext.getResources().getDimension( R.dimen.widget_minwidth );
		WIDGET_HEIGHT = mContext.getResources().getDimension( R.dimen.widget_minheight );
		showClockVeiw = mContext.getResources().getBoolean( R.bool.show_clockView );
		showWeatherVeiw = mContext.getResources().getBoolean( R.bool.show_weatherView );
	}
	
	private void initView()
	{
		rv = new RemoteViews( mContext.getPackageName() , R.layout.widget_layout );
		int visibility = View.VISIBLE;
		//日期
		Intent intentDateClick = new Intent( WidgetProvider.CLICK_DATE_LAYOUT );
		PendingIntent pendingDateIntent = PendingIntent.getBroadcast( mContext , 0 , intentDateClick , 0 );
		rv.setOnClickPendingIntent( R.id.date_textview , pendingDateIntent );
		rv.setOnClickPendingIntent( R.id.weekday_textview , pendingDateIntent );
		//时钟
		if( showClockVeiw )
		{
			Intent intentClockClick = new Intent( WidgetProvider.CLICK_CLOCK_LAYOUT );
			PendingIntent pendingClockIntent = PendingIntent.getBroadcast( mContext , 0 , intentClockClick , 0 );
			rv.setOnClickPendingIntent( R.id.clock_layout , pendingClockIntent );
		}
		visibility = showClockVeiw ? View.VISIBLE : View.GONE;
		rv.setViewVisibility( R.id.clock_layout , visibility );
		//天气
		if( showWeatherVeiw )
		{
			Intent intentWeatherClick = new Intent( WidgetProvider.CLICK_WEATHER_LAYOUT );
			PendingIntent pendingWeatherIntent = PendingIntent.getBroadcast( mContext , 0 , intentWeatherClick , 0 );
			rv.setOnClickPendingIntent( R.id.city_textview , pendingWeatherIntent );
			//			rv.setOnClickPendingIntent( R.id.weather_city , pendingWeatherIntent );
			//			rv.setOnClickPendingIntent( R.id.temperature_current , pendingWeatherIntent );
			//			rv.setOnClickPendingIntent( R.id.temperature_range , pendingWeatherIntent );
		}
		visibility = showWeatherVeiw ? View.VISIBLE : View.GONE;
		setWeatherVisibility( visibility );
	}
	
	/**
	 * 设置天气是否显示
	 * @param visibility
	 */
	public void setWeatherVisibility(
			int visibility )
	{
		if( rv != null )
		{
			rv.setViewVisibility( R.id.city_textview , visibility );
			//			rv.setViewVisibility( R.id.weather_city , visibility );
			//			rv.setViewVisibility( R.id.temperature_current , visibility );
			//			rv.setViewVisibility( R.id.temperature_range , visibility );
		}
	}
	
	public void onClick(
			String action )
	{
		//点击时钟布局
		if( action.equals( WidgetProvider.CLICK_CLOCK_LAYOUT ) )
		{
			ClockCalendarManager.getInstance( mContext ).onClickClock();
		}
		//点击日期
		else if( action.equals( WidgetProvider.CLICK_DATE_LAYOUT ) )
		{
			ClockCalendarManager.getInstance( mContext ).onclickDate();
		}
		//点击天气
		//		else if( action.equals( WidgetProvider.CLICK_WEATHER_LAYOUT ) )
		//		{
		//			WeatherManager.getInstance( mContext ).onClick();
		//		}
		else
		{
			Log.d( TAG , "其他" );
			//启动时钟,防止被杀死
			mContext.startService( new Intent( mContext , ClockAndCalendarService.class ) );
		}
	}
	
	public RemoteViews getRv()
	{
		return rv;
	}
	
	public void initWidgetView(
			Context context )
	{
		if( showClockVeiw )
		{
			ClockCalendarManager.getInstance( context ).isLanguage();
			ClockCalendarManager.getInstance( context ).clockTimeChanged();
			ClockCalendarManager.getInstance( context ).updateClockView();
			context.startService( new Intent( context , ClockAndCalendarService.class ) );
		}
		//		if( showWeatherVeiw )
		//		{
		//			WeatherManager.getInstance( context ).initConfig();
		//			WeatherManager.getInstance( context ).initView();
		//			WeatherManager.getInstance( context ).updateWeatherView();
		//		}
	}
	
	public void updateAppWidget()
	{
		if( rv == null )
		{
			Log.d( TAG , " widgetProvider --> updateAppWidget  rv == null : " );
		}
		//每次new一个rv?
		initView();
		//同步显示信息
		initWidgetView( mContext );
		//更新插件
		AppWidgetManager appWidgetManger = AppWidgetManager.getInstance( mContext );
		int[] appIds = appWidgetManger.getAppWidgetIds( new ComponentName( mContext , WidgetProvider.class ) );
		appWidgetManger.updateAppWidget( appIds , rv );
	}
}
