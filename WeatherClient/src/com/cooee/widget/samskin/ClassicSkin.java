package com.cooee.widget.samskin;


import java.util.Calendar;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.filehelp.Log;
import com.cooee.widget.samweatherclock.R;
import com.cooee.widget.samweatherclock.WeatherConditionImage;
import com.cooee.widget.samweatherclock.WeatherProvider;


public class ClassicSkin extends baseskin
{
	
	private static boolean f_defaultcity = false;
	
	public int getLayout()
	{
		return R.layout.weatherlayout;
	}
	
	public void updateViews(
			Context context ,
			int widgetId ,
			RemoteViews rv )
	{
		updateTime( context , widgetId , rv );
		String postalCode = WeatherProvider.getPostalCode( context , widgetId );
		weatherdataentity dataEntity = readData( context , rv , widgetId , postalCode );
		updateWeather( context , rv , widgetId , postalCode , dataEntity );
	}
	
	public static void updateTime(
			Context context ,
			int widgetId ,
			RemoteViews rv )
	{
		// com.android.settings.DateTimeSettingsSetupWizard
		Intent clockIntent = new Intent();
		/*
		 * clockIntent.setClassName("com.android.settings",
		 * "com.android.settings.DateTimeSettings");
		 */
		String release = android.os.Build.VERSION.RELEASE;
		String version = "2.4";
		if( release.compareTo( version ) > 0 )
		{
			//weijie popup the datetime's activity by android 4.0 OS
			clockIntent.setAction( "android.settings.DATE_SETTINGS" );
			clockIntent.addCategory( "android.intent.category.VOICE_LAUNCH" );
			clockIntent.addCategory( "android.intent.category.DEFAULT" );
			//clockIntent.setClassName("com.android.settings",
			//		"com.android.settings.DateTimeSettingsSetupWizard");
		}
		else
		{
			clockIntent.setClassName( "com.android.settings" , "com.android.settings.DateTimeSettings" );
		}
		PendingIntent pendintent = PendingIntent.getActivity( context , widgetId , clockIntent , PendingIntent.FLAG_UPDATE_CURRENT );
		rv.setOnClickPendingIntent( R.id.weLayout , pendintent );
		// com.cooee.weather.Weather
		Intent weatherActivity = new Intent();
		weatherActivity.setClassName( "com.cooee.widget.samweatherclock" , "com.cooee.widget.samweatherclock.MainActivity" );
		//		weatherActivity.setClassName("com.cooee.weather",
		//				"com.cooee.weather.Weather");
		weatherActivity.putExtra( "userId" , widgetId );
		//		if(f_defaultcity)
		//			weatherActivity.putExtra("defaultcity", context.getResources().getString(R.string.defaultcity));
		//		else
		weatherActivity.putExtra( "defaultcity" , "none" );
		PendingIntent weatherintent = PendingIntent.getActivity( context , widgetId , weatherActivity , PendingIntent.FLAG_UPDATE_CURRENT );
		rv.setOnClickPendingIntent( R.id.curweatherImage , weatherintent );
		// get calendar
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get( Calendar.HOUR_OF_DAY );
		int min = calendar.get( Calendar.MINUTE );
		ContentResolver cv = context.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString( cv , android.provider.Settings.System.TIME_12_24 );
		if( strTimeFormat != null && strTimeFormat.equals( "12" ) )
		{
			rv.setViewVisibility( R.id.AM_PM , View.VISIBLE );
			if( hour > 11 )
			{
				if( hour != 12 )
					hour -= 12;
				rv.setTextViewText( R.id.AM_PM , "PM" );
			}
			else
			{
				if( hour == 0 )
					hour = 12;
				rv.setTextViewText( R.id.AM_PM , "AM" );
			}
		}
		else
		{
			rv.setViewVisibility( R.id.AM_PM , View.INVISIBLE );
		}
		/*if (strTimeFormat != null && strTimeFormat.equals("24")) {
			rv.setViewVisibility(R.id.AM_PM, View.INVISIBLE);
		} else {
			rv.setViewVisibility(R.id.AM_PM, View.VISIBLE);
			if (hour > 11) {
				if (hour != 12)
					hour -= 12;
				rv.setTextViewText(R.id.AM_PM, "PM");
			} else {
				if (hour == 0) hour = 12;
				rv.setTextViewText(R.id.AM_PM, "AM");
			}
		}*/
		rv.setImageViewResource( R.id.dateLeft1 , R.drawable.time_0 + hour / 10 );
		rv.setImageViewResource( R.id.dateLeft2 , R.drawable.time_0 + hour % 10 );
		//weijie_20121122_01 再开机初始化的时候，在时间没有显示的时候，不让时间中间的":"显示出来
		//这里将每一次刷新的时候，将":"一起刷新出来
		rv.setImageViewResource( R.id.timedot , R.drawable.time_dot );
		rv.setImageViewResource( R.id.dateRigth1 , R.drawable.time_0 + min / 10 );
		rv.setImageViewResource( R.id.dateRigth2 , R.drawable.time_0 + min % 10 );
		int month = 0 , day = 0 , week = 0;
		month = calendar.get( Calendar.MONTH ) + 1;
		day = calendar.get( Calendar.DAY_OF_MONTH );
		week = calendar.get( Calendar.DAY_OF_WEEK ) - 1;
		String str = context.getResources().getString( R.string.date_widget_format_string );
		String sTextshow = String.format( str , context.getString( R.string.monthA + month - 1 ) , day , context.getString( R.string.week0 + week ) );
		rv.setTextViewText( R.id.tadayDate , sTextshow );
	}
	
	private static void updateWeather(
			Context context ,
			RemoteViews updateViews ,
			int widgetId ,
			String postalCode ,
			weatherdataentity dataEntity )
	{
		if( dataEntity != null )
		{
			// String strTempc = mDataEntity.getTempC().toString();
			updateViews.setTextViewText( R.id.curadress , dataEntity.getPostalCode() );
			updateViews.setTextViewText( R.id.curweather , dataEntity.getCondition() );
			updateViews.setTextViewText( R.id.curTemperature , dataEntity.getTempC() + "℃" );
			if( dataEntity.getDetails().get( 0 ) != null )
			{
				updateViews.setViewVisibility( R.id.degreescelsius1 , View.VISIBLE );
				updateViews.setViewVisibility( R.id.degreescelsius2 , View.VISIBLE );
				updateViews.setViewVisibility( R.id.tempc_dot1 , View.VISIBLE );
				updateViews.setTextViewText( R.id.TemperatureRange , dataEntity.getDetails().get( 0 ).getHight().toString() );
				updateViews.setTextViewText( R.id.TemperatureLower , dataEntity.getDetails().get( 0 ).getLow().toString() );
			}
			int imageid = WeatherConditionImage.getwidgetConditionImage( dataEntity.getCondition() , true );
			updateViews.setImageViewResource( R.id.land_bg , imageid );
		}
		else
		{
			if( ( postalCode != null ) && ( !( postalCode.equals( "none" ) ) ) )
			{
				updateViews.setTextViewText( R.id.curadress , postalCode );
				// updateViews.setTextViewText(R.id.curweather, "无可用数据");
				updateViews.setTextViewText( R.id.curweather , context.getString( R.string.nodate ) );
			}
			else
			{
				updateViews.setTextViewText( R.id.curadress , context.getString( R.string.selectpostakcode ) + " " );
				updateViews.setTextViewText( R.id.curweather , "" );
			}
			updateViews.setTextViewText( R.id.TemperatureRange , "N/A " );
			updateViews.setViewVisibility( R.id.degreescelsius1 , View.INVISIBLE );
			updateViews.setViewVisibility( R.id.degreescelsius2 , View.INVISIBLE );
			updateViews.setViewVisibility( R.id.tempc_dot1 , View.INVISIBLE );
			updateViews.setTextViewText( R.id.TemperatureLower , "" );
			updateViews.setImageViewResource( R.id.land_bg , R.drawable.weather_default );
		}
	}
}
