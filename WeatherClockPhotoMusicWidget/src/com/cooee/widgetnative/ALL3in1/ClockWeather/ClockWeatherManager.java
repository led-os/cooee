package com.cooee.widgetnative.ALL3in1.ClockWeather;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.widgetnative.ALL3in1.R;
import com.cooee.widgetnative.ALL3in1.base.WidgetManager;
import com.cooee.widgetnative.ALL3in1.base.WidgetProvider;


public class ClockWeatherManager
{
	
	public Context mContext;
	public boolean showWeatherVeiw = true;
	public boolean showClockVeiw = true;
	/***/
	public static ClockWeatherManager mClockWeatherManager;
	public static final String TAG = "ClockWeatherManager";
	
	private ClockWeatherManager(
			Context mContext )
	{
		this.mContext = mContext;
		initConfig();
		initView();
	}
	
	public static ClockWeatherManager getInstance(
			Context context )
	{
		if( mClockWeatherManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mClockWeatherManager == null && context != null )
				{
					mClockWeatherManager = new ClockWeatherManager( context );
				}
			}
		}
		return mClockWeatherManager;
	}
	
	private void initConfig()
	{
		showClockVeiw = mContext.getResources().getBoolean( R.bool.show_clockView );
		showWeatherVeiw = mContext.getResources().getBoolean( R.bool.show_weatherView );
	}
	
	public void initView()
	{
		RemoteViews mRemoteViews = WidgetManager.getInstance( mContext ).getRemoteViews();
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
		RemoteViews mRemoteViews = WidgetManager.getInstance( mContext ).getRemoteViews();
		if( mRemoteViews != null )
		{
			mRemoteViews.setViewVisibility( R.id.city_textview , visibility );
			mRemoteViews.setViewVisibility( R.id.weather_city , visibility );
			mRemoteViews.setViewVisibility( R.id.temperature_current , visibility );
			mRemoteViews.setViewVisibility( R.id.temperature_range , visibility );
		}
	}
}
