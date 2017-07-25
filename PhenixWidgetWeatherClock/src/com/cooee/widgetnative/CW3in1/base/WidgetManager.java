package com.cooee.widgetnative.CW3in1.base;


import org.json.JSONException;

import com.cooee.widgetnative.CW3in1.R;
import com.cooee.widgetnative.CW3in1.ClockWeather.ClockManager;
import com.cooee.widgetnative.CW3in1.ClockWeather.WeatherManager;
import com.cooee.widgetnative.CW3in1.base.utils.StatisticsUtils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class WidgetManager
{
	
	public Context mContext;
	public static float WIDGET_WIDTH = 260;
	public static float WIDGET_HEIGHT = 260;
	public boolean showWeatherVeiw = true;
	public boolean showClockVeiw = true;
	/***/
	private RemoteViews mRemoteViews;
	public static WidgetManager mWidgetManager;
	public static final String TAG = "WidgetManager";
	
	private WidgetManager(
			Context mContext )
	{
		this.mContext = mContext;
		initConfig();
		initView();
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
	
	private void initConfig()
	{
		WIDGET_WIDTH = mContext.getResources().getDimension( R.dimen.widget_width_min );
		WIDGET_HEIGHT = mContext.getResources().getDimension( R.dimen.widget_height_min );
		showClockVeiw = mContext.getResources().getBoolean( R.bool.show_clockView );
		showWeatherVeiw = mContext.getResources().getBoolean( R.bool.weather_default_is_available );
	}
	
	private void initView()
	{
		mRemoteViews = new RemoteViews( mContext.getPackageName() , R.layout.widget_layout );
		int visibility = View.VISIBLE;
		//日期
		Intent intentDateClick = new Intent( WidgetProvider.CLICK_DATE_LAYOUT );
		PendingIntent pendingDateIntent = PendingIntent.getBroadcast( mContext , 0 , intentDateClick , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.date_textview , pendingDateIntent );
		//时钟
		if( showClockVeiw )
		{
			Intent intentClockClick = new Intent( WidgetProvider.CLICK_CLOCK_LAYOUT );
			PendingIntent pendingClockIntent = PendingIntent.getBroadcast( mContext , 0 , intentClockClick , 0 );
			mRemoteViews.setOnClickPendingIntent( R.id.clock_layout , pendingClockIntent );
		}
		visibility = showClockVeiw ? View.VISIBLE : View.GONE;
		mRemoteViews.setViewVisibility( R.id.clock_layout , visibility );
		//天气
		if( showWeatherVeiw )
		{
			Intent intentWeatherClick = new Intent( WidgetProvider.CLICK_WEATHER_LAYOUT );
			PendingIntent pendingWeatherIntent = PendingIntent.getBroadcast( mContext , 0 , intentWeatherClick , 0 );
			mRemoteViews.setOnClickPendingIntent( R.id.city_textview , pendingWeatherIntent );
			mRemoteViews.setOnClickPendingIntent( R.id.weather_city , pendingWeatherIntent );
			mRemoteViews.setOnClickPendingIntent( R.id.temperature_current , pendingWeatherIntent );
			mRemoteViews.setOnClickPendingIntent( R.id.temperature_range , pendingWeatherIntent );
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
		if( mRemoteViews != null )
		{
			mRemoteViews.setViewVisibility( R.id.city_textview , visibility );
			mRemoteViews.setViewVisibility( R.id.weather_city , visibility );
			mRemoteViews.setViewVisibility( R.id.temperature_current , visibility );
			mRemoteViews.setViewVisibility( R.id.temperature_range , visibility );
		}
	}
	
	public void onClick(
			String action )
	{
		//点击时钟布局
		if( action.equals( WidgetProvider.CLICK_CLOCK_LAYOUT ) )
		{
			Log.i( "test" , "flj CLICK_CLOCK_LAYOUT" );
			ClockManager.getInstance( mContext ).onClickClock();
		}
		//点击日期
		else
			if( action.equals( WidgetProvider.CLICK_DATE_LAYOUT ) )
		{
			Log.i( "test" , "flj CLICK_DATE_LAYOUT" );
			ClockManager.getInstance( mContext ).onclickDate();
		}
		//点击天气
		else
				if( action.equals( WidgetProvider.CLICK_WEATHER_LAYOUT ) )
		{
			Log.i( "test" , "flj CLICK_WEATHER_LAYOUT" );
			WeatherManager.getInstance( mContext ).onClick();
		}
		else
		{
			Log.d( TAG , "其他" );
			//启动时钟,防止被杀死
			mContext.startService( new Intent( mContext , WidgetService.class ) );
		}
	}
	
	public RemoteViews getRemoteViews()
	{
		return mRemoteViews;
	}
	
	public void initWidgetView(
			Context context )
	{
		if( showClockVeiw )
		{
			ClockManager mClockManager = ClockManager.getInstance( context );
			mClockManager.isLanguage();
			mClockManager.clockTimeChanged();
			mClockManager.updateClockView();
			context.startService( new Intent( context , WidgetService.class ) );
		}
		if( showWeatherVeiw )
		{
			WeatherManager mWeatherManager = WeatherManager.getInstance( context );
			mWeatherManager.updateWeatherView();
			context.startService( new Intent( context , WidgetService.class ) );
		}
	}
	
	public void updateAppWidget()
	{
		if( mRemoteViews == null )
		{
			Log.d( TAG , " widgetProvider --> updateAppWidget  rv == null : " );
		}
		//每次new一个rv?
		initView();
		//同步显示信息
		initWidgetView( mContext );
		//更新插件
		AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance( mContext );
		int[] appIds = mAppWidgetManager.getAppWidgetIds( new ComponentName( mContext , WidgetProvider.class ) );
		mAppWidgetManager.updateAppWidget( appIds , mRemoteViews );
	}
	
	public void doStatistics()
	{
		try
		{
			StatisticsUtils.getInstance( mContext ).olapStatistics();
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
