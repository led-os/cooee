package com.cooee.widget.Transkin;


import java.util.Calendar;

import com.cooee.weather.WeatherEntity;
import com.cooee.weather.WeatherIMG;
import com.cooee.weather.WeatherUtils;
import com.cooee.widget.FindWeatherClock.R;
import com.cooee.widget.TranWeatherClock.WeatherProvider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class ClassicSkin extends baseskin
{
	
	/**
	 * 初始化天气图片资源id
	 */
	static
	{
		WeatherIMG.initWeatherDataImageId(
				R.drawable.weather_icon_front_03 ,
				R.drawable.weather_icon_front_02 ,
				R.drawable.weather_icon_front_03 ,
				R.drawable.weather_icon_front_06 ,
				R.drawable.weather_icon_front_10 ,
				R.drawable.weather_icon_front_01 ,
				R.drawable.weather_icon_front_08 ,
				R.drawable.weather_icon_front_07 ,
				R.drawable.weather_icon_front_18 ,
				R.drawable.weather_icon_front_19 );
		Log.i( "test" , "flj imageid=1==" + R.drawable.weather_icon_front_03 );
		Log.i( "test" , "flj imageid=2==" + R.drawable.weather_icon_front_02 );
		Log.i( "test" , "flj imageid=10==" + R.drawable.weather_icon_front_19 );
		Log.i( "test" , "flj imageid=4==" + R.drawable.weather_icon_front_06 );
		Log.i( "test" , "flj imageid=5==" + R.drawable.weather_icon_front_10 );
		Log.i( "test" , "flj imageid=6==" + R.drawable.weather_icon_front_01 );
		Log.i( "test" , "flj imageid=7==" + R.drawable.weather_icon_front_08 );
		Log.i( "test" , "flj imageid=8==" + R.drawable.weather_icon_front_07 );
		Log.i( "test" , "flj imageid=9==" + R.drawable.weather_icon_front_18 );
	}
	
	public static final String BT_REFRESH_ACTION = "com.android.timer.BT_REFRESH_ACTION";
	private static int imageid;
	
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
		updateWeather( context , rv , widgetId );
	}
	
	public static void updateTime(
			Context context ,
			int widgetId ,
			RemoteViews rv )
	{
		// com.android.settings.DateTimeSettingsSetupWizard
		String co_into = context.getResources().getString( R.string.enter_the_alarm_clock );
		if( true )
		{
			Intent cIntent = new Intent();
			PendingIntent pendintent = null;
			PendingIntent pendintent2 = null;
			Intent cIntent2 = new Intent();
			if( !co_into.equals( "0" ) )
			{
				String release = android.os.Build.VERSION.RELEASE;
				String version = "2.4";
				if( release.compareTo( version ) > 0 )
				{
					// weijie popup the datetime's activity by android 4.0 OS
					cIntent.setAction( "android.settings.DATE_SETTINGS" );
					cIntent.addCategory( "android.intent.category.VOICE_LAUNCH" );
					cIntent.addCategory( "android.intent.category.DEFAULT" );
				}
				else
				{
					cIntent.setClassName( "com.android.settings" , "com.android.settings.DateTimeSettings" );
				}
				pendintent = PendingIntent.getActivity( context , widgetId , cIntent , PendingIntent.FLAG_UPDATE_CURRENT );
			}
			else
			{
				cIntent.setAction( BT_REFRESH_ACTION );
				pendintent = PendingIntent.getBroadcast( context , 0 , cIntent , PendingIntent.FLAG_UPDATE_CURRENT );
				String enter_the_alarm_clock_calendar = context.getResources().getString( R.string.enter_the_alarm_clock_calendar );
				if( enter_the_alarm_clock_calendar.equals( "2" ) )
				{
					cIntent2.setClassName( "com.android.calendar" , "com.android.calendar.AllInOneActivity" );
				}
				else
				{
					String release = android.os.Build.VERSION.RELEASE;
					String version = "2.4";
					if( release.compareTo( version ) > 0 )
					{
						cIntent2.setAction( "android.settings.DATE_SETTINGS" );
						cIntent2.addCategory( "android.intent.category.VOICE_LAUNCH" );
						cIntent2.addCategory( "android.intent.category.DEFAULT" );
					}
					else
					{
						cIntent2.setClassName( "com.android.settings" , "com.android.settings.DateTimeSettings" );
					}
				}
				pendintent2 = PendingIntent.getActivity( context , widgetId , cIntent2 , PendingIntent.FLAG_UPDATE_CURRENT );
			}
			//			rv.setOnClickPendingIntent( R.id.weLayout , pendintent );
			rv.setOnClickPendingIntent( R.id.dateLeft1 , pendintent );
			rv.setOnClickPendingIntent( R.id.dateLeft2 , pendintent );
			rv.setOnClickPendingIntent( R.id.timedot , pendintent );
			rv.setOnClickPendingIntent( R.id.dateRigth1 , pendintent );
			rv.setOnClickPendingIntent( R.id.dateRigth2 , pendintent );
			if( !co_into.equals( "0" ) )
			{
				//				rv.setOnClickPendingIntent( R.id.curadress , pendintent );
				rv.setOnClickPendingIntent( R.id.tadayDate , pendintent );
			}
			else
			{
				//				rv.setOnClickPendingIntent( R.id.curadress , pendintent2 );
				rv.setOnClickPendingIntent( R.id.tadayDate , pendintent2 );
			}
			Intent intent = new Intent( context , WeatherProvider.class );
			intent.setAction( "com.cooee.widget.TranWeatherClock.WeatherProvider.activity" );
			PendingIntent weatherintent = PendingIntent.getBroadcast( context , widgetId , intent , PendingIntent.FLAG_UPDATE_CURRENT );
			rv.setOnClickPendingIntent( R.id.curweather_layout , weatherintent );
			rv.setOnClickPendingIntent( R.id.todayweather , weatherintent );
			//yangmengchao add
			rv.setOnClickPendingIntent( R.id.curadress , weatherintent );
			// get calendar
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get( Calendar.HOUR_OF_DAY );
			int min = calendar.get( Calendar.MINUTE );
			// am、pm , shlt , start
			boolean is24HourFormat = DateFormat.is24HourFormat( context );
			String language = context.getResources().getConfiguration().locale.getCountry();
			if( is24HourFormat )
			{
				rv.setViewVisibility( R.id.AM_PM , View.INVISIBLE );
			}
			else
			{
				rv.setViewVisibility( R.id.AM_PM , View.VISIBLE );
				if( hour < 12 )
				{
					if( "CN".equals( language ) )
					{
						rv.setTextViewText( R.id.AM_PM , "上午" );
						//						rv.setImageViewResource( R.id.AM_PM , R.drawable.time_am_cn );
					}
					else
					{
						rv.setTextViewText( R.id.AM_PM , "AM" );
						//						rv.setImageViewResource( R.id.AM_PM , R.drawable.time_am );
					}
				}
				else
					if( hour == 12 )
				{
					if( "CN".equals( language ) )
					{
						rv.setTextViewText( R.id.AM_PM , "下午" );
						//						rv.setImageViewResource( R.id.AM_PM , R.drawable.time_pm_cn );
					}
					else
					{
						rv.setTextViewText( R.id.AM_PM , "PM" );
						//						rv.setImageViewResource( R.id.AM_PM , R.drawable.time_pm );
					}
				}
				else
				{
					hour -= 12;
					if( "CN".equals( language ) )
					{
						rv.setTextViewText( R.id.AM_PM , "下午" );
						//						rv.setImageViewResource( R.id.AM_PM , R.drawable.time_pm_cn );
					}
					else
					{
						rv.setTextViewText( R.id.AM_PM , "PM" );
						//						rv.setImageViewResource( R.id.AM_PM , R.drawable.time_pm );
					}
				}
			}
			if( hour < 10 )
			{
				rv.setImageViewResource( R.id.dateLeft1 , R.drawable.time_0 );
			}
			else
			{
				rv.setImageViewResource( R.id.dateLeft1 , R.drawable.time_0 + hour / 10 );
			}
			rv.setImageViewResource( R.id.dateLeft2 , R.drawable.time_0 + hour % 10 );
			// weijie_20121122_01 再开机初始化的时候，在时间没有显示的时候，不让时间中间的":"显示出来
			// 这里将每一次刷新的时候，将":"一起刷新出来
			rv.setImageViewResource( R.id.timedot , R.drawable.time_dot );
			rv.setImageViewResource( R.id.dateRigth1 , R.drawable.time_0 + min / 10 );
			rv.setImageViewResource( R.id.dateRigth2 , R.drawable.time_0 + min % 10 );
			// am、pm , shlt , end
			int year = 0 , month = 0 , day = 0 , week = 0;
			year = calendar.get( Calendar.YEAR );
			month = calendar.get( Calendar.MONTH ) + 1;
			day = calendar.get( Calendar.DAY_OF_MONTH );
			week = calendar.get( Calendar.DAY_OF_WEEK ) - 1;
			String str = context.getResources().getString( R.string.date_widget_format_string );
			String sTextshow = String.format( str , year , month , day , context.getString( R.string.week0 + week ) );
			rv.setTextViewText( R.id.tadayDate , sTextshow );
		}
	}
	
	private static void updateWeather(
			Context context ,
			RemoteViews updateViews ,
			int widgetId )
	{
		WeatherEntity dataEntity = WeatherUtils.getCurWeatherEntity();
		if( dataEntity != null )
		{
			Log.i( "test" , "flj dataEntity != null===imageid==" + imageid );
			// String strTempc = mDataEntity.getTempC().toString();
			updateViews.setTextViewText( R.id.curadress , dataEntity.getPostalCode() );
			// shlt , start
			//				updateViews.setTextViewText( R.id.curT , dataEntity.getTempC().toString() + "℃" );
			String hight = dataEntity.getDetails().get( 0 ).getHight().toString();
			String low = dataEntity.getDetails().get( 0 ).getLow().toString();
			if( hight != null && low != null && !"".equals( hight ) && !"null".equals( hight ) && !"".equals( low ) && !"null".equals( low ) )
			{
				String[] hights = hight.split( "" ) , lows = low.split( "" );
				if( hights != null && lows != null )
				{
					if( hights.length == 2 )
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.VISIBLE );
						updateViews.setViewVisibility( R.id.curweather_left1 , View.GONE );
						updateViews.setViewVisibility( R.id.curweather_left0 , View.GONE );
						updateViews.setImageViewResource( R.id.curweather_left2 , R.drawable.temp_0 + Integer.parseInt( hights[1] ) );
					}
					else
						if( hights.length == 3 )
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.VISIBLE );
						if( "-".equals( hights[1] ) )
						{
							updateViews.setViewVisibility( R.id.curweather_left0 , View.VISIBLE );
							updateViews.setViewVisibility( R.id.curweather_left1 , View.GONE );
							updateViews.setImageViewResource( R.id.curweather_left1 , R.drawable.temp_0 + Integer.parseInt( hights[2] ) );
						}
						else
						{
							updateViews.setViewVisibility( R.id.curweather_left0 , View.GONE );
							updateViews.setViewVisibility( R.id.curweather_left1 , View.VISIBLE );
							updateViews.setImageViewResource( R.id.curweather_left1 , R.drawable.temp_0 + Integer.parseInt( hights[1] ) );
							updateViews.setImageViewResource( R.id.curweather_left2 , R.drawable.temp_0 + Integer.parseInt( hights[2] ) );
						}
					}
					else
							if( hights.length == 4 )
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.VISIBLE );
						updateViews.setViewVisibility( R.id.curweather_left0 , View.VISIBLE );
						updateViews.setViewVisibility( R.id.curweather_left1 , View.VISIBLE );
						updateViews.setImageViewResource( R.id.curweather_left1 , R.drawable.temp_0 + Integer.parseInt( hights[2] ) );
						updateViews.setImageViewResource( R.id.curweather_left2 , R.drawable.temp_0 + Integer.parseInt( hights[3] ) );
					}
					else
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.GONE );
					}
					if( lows.length == 2 )
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.VISIBLE );
						updateViews.setViewVisibility( R.id.curweather_right0 , View.GONE );
						updateViews.setViewVisibility( R.id.curweather_right1 , View.GONE );
						updateViews.setImageViewResource( R.id.curweather_right2 , R.drawable.temp_0 + Integer.parseInt( lows[1] ) );
					}
					else
						if( lows.length == 3 )
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.VISIBLE );
						if( "-".equals( lows[1] ) )
						{
							updateViews.setViewVisibility( R.id.curweather_right0 , View.VISIBLE );
							updateViews.setViewVisibility( R.id.curweather_right1 , View.GONE );
							updateViews.setImageViewResource( R.id.curweather_right1 , R.drawable.temp_0 + Integer.parseInt( lows[2] ) );
						}
						else
						{
							updateViews.setViewVisibility( R.id.curweather_right0 , View.GONE );
							updateViews.setViewVisibility( R.id.curweather_right1 , View.VISIBLE );
							updateViews.setImageViewResource( R.id.curweather_right1 , R.drawable.temp_0 + Integer.parseInt( lows[1] ) );
							updateViews.setImageViewResource( R.id.curweather_right2 , R.drawable.temp_0 + Integer.parseInt( lows[2] ) );
						}
					}
					else
							if( lows.length == 4 )
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.VISIBLE );
						updateViews.setViewVisibility( R.id.curweather_right0 , View.VISIBLE );
						updateViews.setViewVisibility( R.id.curweather_right1 , View.VISIBLE );
						updateViews.setImageViewResource( R.id.curweather_right1 , R.drawable.temp_0 + Integer.parseInt( lows[2] ) );
						updateViews.setImageViewResource( R.id.curweather_right2 , R.drawable.temp_0 + Integer.parseInt( lows[3] ) );
					}
					else
					{
						updateViews.setViewVisibility( R.id.curweather_layout , View.GONE );
					}
				}
				else
				{
					updateViews.setViewVisibility( R.id.curweather_layout , View.GONE );
				}
			}
			else
			{
				updateViews.setViewVisibility( R.id.curweather_layout , View.GONE );
			}
			//天气图片
			Log.i( "test" , "flj dataEntity != null===getCondition===" + dataEntity.getDetails().get( 0 ).getCondition() );
			imageid = WeatherIMG.getWeatherDataImageIdByCondition( dataEntity.getDetails().get( 0 ).getCondition() , true );
			Log.i( "test" , "flj dataEntity != null===imageid==2=" + imageid );
			System.out.println( "shlt , updateWeather , imageid : " + imageid );
			updateViews.setImageViewResource( R.id.todayweather , imageid );
			WeatherProvider.setPlayRefreshAnim( false );
			int layout = R.layout.anim_refresh;
			RemoteViews curViews = new RemoteViews( context.getPackageName() , layout );
			updateViews.removeAllViews( R.id.refresh_anim );
			updateViews.addView( R.id.refresh_anim , curViews );
			Intent intent = new Intent( context , WeatherProvider.class );
			intent.setAction( "com.cooee.widget.TranWeatherClock.WeatherProvider.refresh" );
			intent.putExtra( "postalCode" , dataEntity.getPostalCode() );
			intent.putExtra( "widgetId" , widgetId );
			PendingIntent refreshIntent = PendingIntent.getBroadcast( context , widgetId , intent , PendingIntent.FLAG_UPDATE_CURRENT );
			updateViews.setOnClickPendingIntent( R.id.refresh_anim , refreshIntent );
		}
		else
		{
			Log.i( "test" , "flj dataEntity == null===" );
			updateViews.setTextViewText( R.id.curadress , context.getString( R.string.my_address ) );
			updateViews.setViewVisibility( R.id.curweather_layout , View.GONE );
			//				updateViews.setTextViewText( R.id.curweather , "未知" );
			if( imageid != 0 )
			{
				Log.i( "test" , "flj dataEntity == null===imageid==" + imageid );
				updateViews.setImageViewResource( R.id.todayweather , imageid );
			}
			else
			{
				Log.i( "test" , "flj dataEntity == null===imageid==" + imageid );
				updateViews.setImageViewResource( R.id.todayweather , WeatherIMG.getWeatherDataImageIdByCondition( "" , true ) );
			}
		}
	}
}
