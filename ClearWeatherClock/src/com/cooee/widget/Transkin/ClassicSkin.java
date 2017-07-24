package com.cooee.widget.Transkin;


import java.util.Calendar;

import com.cooee.weather.WeatherCondition;
import com.cooee.weather.WeatherEntity;
import com.cooee.weather.WeatherIMG;
import com.cooee.widget.ClearWeatherClock.R;
import com.cooee.widget.TranWeatherClock.AppConfig;
import com.cooee.widget.TranWeatherClock.WeatherProvider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class ClassicSkin extends BaseSkin
{
	
	/**
	 * 初始化天气图片资源id
	 */
	static
	{
		WeatherIMG.initResIdByIcon(
				R.drawable.weather_data_fog ,
				R.drawable.weather_data_mostcloudy ,
				R.drawable.weather_data_overcast ,
				R.drawable.weather_data_rain ,
				R.drawable.weather_data_snow ,
				R.drawable.weather_data_sunny ,
				R.drawable.weather_data_thunderstorm ,
				R.drawable.weather_icon_no_client );
	}
	
	private static boolean f_defaultcity = false;
	static int[] ids = {
			R.drawable.time_0 ,
			R.drawable.time_1 ,
			R.drawable.time_2 ,
			R.drawable.time_3 ,
			R.drawable.time_4 ,
			R.drawable.time_5 ,
			R.drawable.time_6 ,
			R.drawable.time_7 ,
			R.drawable.time_8 ,
			R.drawable.time_9 };
			
	public int getLayout()
	{
		return R.layout.weatherlayout;
	}
	
	public void updateViews(
			Context context ,
			int widgetId ,
			RemoteViews rv ,
			WeatherEntity dataEntity )
	{
		updateTime( context , widgetId , rv );
		updateWeather( context , rv , widgetId , dataEntity );
	}
	
	public static void updateTime(
			Context context ,
			int widgetId ,
			RemoteViews rv )
	{
		// com.android.settings.DateTimeSettingsSetupWizard
		Intent clockIntent = new Intent();
		if( true )
		{
			String release = android.os.Build.VERSION.RELEASE;
			String version = "2.4";
			String defaultClockComp = AppConfig.getInstance( context ).getDefaultClockComp();
			if( defaultClockComp != null && !"".equals( defaultClockComp ) )
			{
				String[] componentarray = defaultClockComp.split( ";" );
				Log.i( "ClassicSkin" , "componentarray = " + componentarray[0] + ";" + componentarray[1] );
				clockIntent.setClassName( componentarray[0] , componentarray[1] );
			}
			else
				if( release.compareTo( version ) > 0 )
			{
				clockIntent.setAction( "android.settings.DATE_SETTINGS" );
				clockIntent.addCategory( "android.intent.category.VOICE_LAUNCH" );
				clockIntent.addCategory( "android.intent.category.DEFAULT" );
			}
			else
			{
				clockIntent.setClassName( "com.android.settings" , "com.android.settings.DateTimeSettings" );
			}
			PendingIntent pendintent = PendingIntent.getActivity( context , widgetId , clockIntent , PendingIntent.FLAG_UPDATE_CURRENT );
			rv.setOnClickPendingIntent( R.id.weLayout , pendintent );
			Intent intent = new Intent( context , WeatherProvider.class );
			intent.setAction( "com.cooee.widget.TranWeatherClock.WeatherProvider.activity" );
			PendingIntent weatherintent = PendingIntent.getBroadcast( context , widgetId , intent , PendingIntent.FLAG_UPDATE_CURRENT );
			rv.setOnClickPendingIntent( R.id.widget_LinearLayout2 , weatherintent );
			// get calendar
			Calendar calendar = Calendar.getInstance();
			int hour;
			int min = calendar.get( Calendar.MINUTE );
			/*
			 * ContentResolver cv = context.getContentResolver(); String
			 * strTimeFormat = android.provider.Settings.System.getString(cv,
			 * android.provider.Settings.System.TIME_12_24);
			 * 
			 * if (strTimeFormat != null && strTimeFormat.equals("12")) {
			 * rv.setViewVisibility(R.id.AM_PM, View.VISIBLE); if (hour > 11) {
			 * if (hour != 12) hour -= 12; rv.setImageViewResource(R.id.AM_PM,
			 * R.drawable.time_pm); } else { if (hour == 0) hour = 12;
			 * rv.setImageViewResource(R.id.AM_PM, R.drawable.time_am); } } else
			 * { rv.setViewVisibility(R.id.AM_PM, View.INVISIBLE); }
			 */
			/*
			 * if (strTimeFormat != null && strTimeFormat.equals("24")) {
			 * rv.setViewVisibility(R.id.AM_PM, View.INVISIBLE); } else {
			 * rv.setViewVisibility(R.id.AM_PM, View.VISIBLE); if (hour > 11) {
			 * if (hour != 12) hour -= 12; rv.setTextViewText(R.id.AM_PM, "PM");
			 * } else { if (hour == 0) hour = 12; rv.setTextViewText(R.id.AM_PM,
			 * "AM"); } }
			 */
			// am、pm , shlt , start
			boolean is24HourFormat = DateFormat.is24HourFormat( context );
			if( is24HourFormat )
			{
				hour = calendar.get( Calendar.HOUR_OF_DAY );
				rv.setViewVisibility( R.id.time_am_pm , View.GONE );
				rv.setViewVisibility( R.id.dateLeft1 , View.VISIBLE );
			}
			else
			{
				hour = calendar.get( Calendar.HOUR );
				int amPm = calendar.get( Calendar.AM_PM );
				String strAmPm = context.getResources().getString( amPm == Calendar.AM ? R.string.time_am : R.string.time_pm );
				rv.setViewVisibility( R.id.time_am_pm , View.VISIBLE );
				rv.setTextViewText( R.id.time_am_pm , strAmPm );
				hour = hour == 0 ? 12 : hour;
				if( hour < 10 )
					rv.setViewVisibility( R.id.dateLeft1 , View.INVISIBLE );
				else
					rv.setViewVisibility( R.id.dateLeft1 , View.VISIBLE );
			}
			// am、pm , shlt , end
			rv.setImageViewResource( R.id.dateLeft1 , ids[hour / 10] );
			rv.setImageViewResource( R.id.dateLeft2 , ids[hour % 10] );
			// weijie_20121122_01 再开机初始化的时候，在时间没有显示的时候，不让时间中间的":"显示出来
			// 这里将每一次刷新的时候，将":"一起刷新出来
			rv.setImageViewResource( R.id.timedot , R.drawable.time_dot );
			rv.setImageViewResource( R.id.dateRigth1 , ids[min / 10] );
			rv.setImageViewResource( R.id.dateRigth2 , ids[min % 10] );
			int year = 0 , month = 0 , day = 0 , week = 0;
			year = calendar.get( Calendar.YEAR );
			month = calendar.get( Calendar.MONTH ) + 1;
			day = calendar.get( Calendar.DAY_OF_MONTH );
			week = calendar.get( Calendar.DAY_OF_WEEK ) - 1;
			String str = context.getResources().getString( R.string.date_widget_format_string );
			// gaominghui@2016/12/09 ADD START 日期小于10号默认前面加0
			String days = null;
			if( Integer.toString( day ).length() < 2 )
			{
				days = "0" + Integer.toString( day );
			}
			else
			{
				days = Integer.toString( day );
			}
			String sTextshow = String.format( str , context.getString( R.string.week0 + week ) , context.getString( R.string.monthA + month - 1 ) , days , year );
			// gaominghui@2016/12/09 ADD END
			rv.setTextViewText( R.id.tadayDate , sTextshow );
		}
	}
	
	private static void updateWeather(
			Context context ,
			RemoteViews updateViews ,
			int widgetId ,
			WeatherEntity dataEntity )
	{
		if( dataEntity != null )
		{
			Log.i( "test" , "flj: 有数据" );
			if( dataEntity.getCity() != null && !"".equals( dataEntity.getCity() ) )
			{
				updateViews.setTextViewText( R.id.curadress , dataEntity.getCity() );
				Log.i( "test" , "flj: 有数据 getCity " + dataEntity.getCity() );
				updateViews.setViewVisibility( R.id.hava_data , View.VISIBLE );
				updateViews.setViewVisibility( R.id.n_a , View.INVISIBLE );
			}
			else
			{
				updateViews.setTextViewText( R.id.curadress , context.getResources().getString( R.string.my_address ) );
				updateViews.setViewVisibility( R.id.hava_data , View.INVISIBLE );
				updateViews.setViewVisibility( R.id.n_a , View.VISIBLE );
			}
			if( dataEntity.getCondition() != null && !"".equals( dataEntity.getCondition() ) )
			{
				Log.i( "test" , "flj: 有数据 getCondition " + dataEntity.getCondition() );
				updateViews.setTextViewText( R.id.curT , WeatherCondition.getConditionStringByLanguage( context , dataEntity.getCondition() ) );
			}
			if( dataEntity.getTempL() != null && dataEntity.getTempH() != null )
			{
				Log.i( "test" , "flj: 有数据 getTempH " + dataEntity.getTempH() );
				updateViews.setTextViewText( R.id.curweather , ( dataEntity.getTempL() + "~" + dataEntity.getTempH() + "℃" ) );
			}
			if( dataEntity.getWeather_index() != null )
			{
				Log.i( "test" , "flj: 有数据 getWeather_index " + dataEntity.getWeather_index() );
				int imageid = 0;
				// gaominghui@2016/08/02 ADD START
				/**
				 * 当如果对接的是我们的天气客户端时，当收到com.cooee.weather.Weather.action.WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION该广播和com.cooee.weather.Weather.action.WEATHER_CLIENT_CLOSED_UPDATE_LAUNCHER_ACTION该广播时
				两个广播传过来的dataEntity.getWeather_index()的值一个是英文一个是转过的中文，当我们通过天气大类获取图片时如果获取不到很可能是因为收到了我们客户端额Refresh的广播，当获取不到时通过condition在获取一次
				 */
				imageid = WeatherIMG.getWeatherDataImageIdByIndexIcon( dataEntity.getWeather_index() );
				if( imageid == R.drawable.weather_icon_no_client && AppConfig.getInstance( context ).getDefaultPackage().equals( WeatherProvider.DEFAULT_CLIENT ) && dataEntity.getCondition() != null )
				{
					imageid = WeatherIMG.getWeatherDataImageIdByCondition( dataEntity.getCondition() );
				}
				// gaominghui@2016/08/02 ADD END
				updateViews.setImageViewResource( R.id.todayweather , imageid );
			}
		}
	}
}
