package com.cooee.app.cooeeweather.view;


import java.text.SimpleDateFormat;

import com.cooee.app.cooeeweather.dataentity.WeatherCondition;
import com.cooee.widget.samweatherclock.R;


public class WeatherConditionImage
{
	
	static int weather = 0;
	@SuppressWarnings( "unused" )
	private static String TAG = "com.cooee.weather.dataentity.WeatherCondition";
	
	public static int getConditionImage(
			String condition )
	{
		return getConditionImage( condition , false );
	}
	
	public static int getConditionImageHuawei(
			String condition )
	{
		return getConditionImageHuawei( condition , false );
	}
	
	public static int getConditionImageHuawei(
			String condition ,
			boolean need_day )
	{
		WeatherCondition.Condition c = WeatherCondition.convertCondition( condition );
		int image = 0;
		SimpleDateFormat sDateFormat = new SimpleDateFormat( "HH" );
		String date = sDateFormat.format( new java.util.Date() );
		boolean day = false;
		// 鍒ゆ柇涓虹櫧澶╂垨澶滄櫄
		if( date.compareTo( "18" ) < 0 && date.compareTo( "06" ) > 0 )
		{
			// 6鐐癸�?8鐐�?负鐧藉ぉ
			day = true;
		}
		// 濡傛灉涓嶉渶瑕佸垽鏂櫧澶╂垨澶滄櫄锛屽垯榛樿涓虹櫧澶�?
		if( !need_day )
			day = true;
		switch( c )
		{
			case WEATHER_FINE:
				if( day )
					image = R.drawable.weather_sunny_day_1;
				else
					image = R.drawable.weather_sunny_night_1;
				break;
			case WEATHER_CLOUDY:
				if( day )
					image = R.drawable.weather_cloudy_day_1;
				else
					image = R.drawable.weather_cloudy_night_1;
				break;
			case WEATHER_OVERCAST:
				if( day )
					image = R.drawable.weather_overcast_day_1;
				else
					image = R.drawable.weather_overcast_night_1;
				break;
			case WEATHER_SNOW:
				image = R.drawable.weather_snow_1;
				break;
			case WEATHER_THUNDERSTORM:
				image = R.drawable.weather_thunderstorms_1;
				break;
			case WEATHER_SLEET:
			case WEATHER_LIGHTRAIN:
				image = R.drawable.weather_shower_1;
				break;
			case WEATHER_RAIN:
			case WEATHER_STORM:
			case WEATHER_RAINSTORM:
				image = R.drawable.weather_rain_1;
				break;
			case WEATHER_FOG:
				image = R.drawable.weather_fog_1;
				break;
			default:
				image = R.drawable.weather_haze_1;
				break;
		}
		return image;
	}
	
	public static int getConditionImage(
			String condition ,
			boolean need_day )
	{
		WeatherCondition.Condition c = WeatherCondition.convertCondition( condition );
		int image = 0;
		SimpleDateFormat sDateFormat = new SimpleDateFormat( "HH" );
		String date = sDateFormat.format( new java.util.Date() );
		boolean day = false;
		// 鍒ゆ柇涓虹櫧澶╂垨澶滄櫄
		if( date.compareTo( "18" ) < 0 && date.compareTo( "06" ) > 0 )
		{
			// 6鐐癸�?8鐐�?负鐧藉ぉ
			day = true;
		}
		// 濡傛灉涓嶉渶瑕佸垽鏂櫧澶╂垨澶滄櫄锛屽垯榛樿涓虹櫧澶�?
		if( !need_day )
			day = true;
		switch( c )
		{
			case WEATHER_FINE:
				if( day )
					image = R.drawable.sunny_day_medium;
				else
					image = R.drawable.sunny_night_medium;
				break;
			case WEATHER_CLOUDY:
				if( day )
					image = R.drawable.cloudy_day_medium;
				else
					image = R.drawable.cloudy_night_medium;
				break;
			case WEATHER_OVERCAST:
				image = R.drawable.overcast_medium;
				break;
			case WEATHER_SNOW:
				image = R.drawable.snow_medium;
				break;
			case WEATHER_THUNDERSTORM:
				image = R.drawable.thunderstrom_medium;
				break;
			case WEATHER_SLEET:
			case WEATHER_STORM:
			case WEATHER_LIGHTRAIN:
			case WEATHER_RAIN:
			case WEATHER_RAINSTORM:
				image = R.drawable.rain_medium;
				break;
			case WEATHER_FOG:
			default:
				image = R.drawable.overcast_medium;
				break;
		}
		return image;
	}
	
	public static int getFullConditionImage(
			String condition )
	{
		WeatherCondition.Condition c = WeatherCondition.convertCondition( condition );
		int image = 0;
		SimpleDateFormat sDateFormat = new SimpleDateFormat( "HH" );
		String date = sDateFormat.format( new java.util.Date() );
		boolean day = false;
		// 鍒ゆ柇涓虹櫧澶╂垨澶滄櫄
		if( date.compareTo( "18" ) < 0 && date.compareTo( "06" ) > 0 )
		{
			day = true;
		}
		switch( c )
		{
			case WEATHER_FINE:
				if( day )
					image = R.drawable.weather_sunny;
				else
					image = R.drawable.weather_clear;
				break;
			case WEATHER_CLOUDY:
				if( day )
					image = R.drawable.weather_partly_cloud;
				else
					image = R.drawable.weather_partly_cloud_night;
				break;
			case WEATHER_OVERCAST:
				if( day )
					image = R.drawable.weather_cloudy_day;
				else
					image = R.drawable.weather_cloudy_night;
				break;
			case WEATHER_SNOW:
				if( day )
					image = R.drawable.weather_snow_day;
				else
					image = R.drawable.weather_snow_night;
				break;
			case WEATHER_THUNDERSTORM: // 闆烽�?��ㄥ洜涓烘病鍔ㄧ敾锛屾晥鏋�?��濂斤紝鏀逛负浣跨敤闆ㄥぉ
				if( day )
					// image = R.drawable.weather_thunderstorm_day;
					image = R.drawable.weather_rain_day;
				else
					// image = R.drawable.weather_thunderstorm_night;
					image = R.drawable.weather_rain_night;
				break;
			case WEATHER_SLEET:
			case WEATHER_STORM:
			case WEATHER_LIGHTRAIN:
			case WEATHER_RAIN:
			case WEATHER_RAINSTORM:
				if( day )
					image = R.drawable.weather_rain_day;
				else
					image = R.drawable.weather_rain_night;
				break;
			case WEATHER_HAZE:
			case WEATHER_FOG:
				if( day )
					image = R.drawable.weather_fog_day;
				else
					image = R.drawable.weather_fog_night;
				break;
			default: // 榛樿涓哄浜�
				if( day )
					image = R.drawable.weather_cloudy_day;
				else
					image = R.drawable.weather_cloudy_night;
				break;
		}
		return image;
	}
	
	public static int getFullConditionImageHuawei(
			String condition )
	{
		WeatherCondition.Condition c = WeatherCondition.convertCondition( condition );
		int image = 0;
		SimpleDateFormat sDateFormat = new SimpleDateFormat( "HH" );
		String date = sDateFormat.format( new java.util.Date() );
		boolean day = false;
		// 鍒ゆ柇涓虹櫧澶╂垨澶滄櫄
		if( date.compareTo( "18" ) < 0 && date.compareTo( "06" ) > 0 )
		{
			day = true;
		}
		switch( c )
		{
			case WEATHER_FINE:
				if( day )
					image = R.drawable.bg_sunny;
				else
					image = R.drawable.bg_sunny_night;
				break;
			case WEATHER_CLOUDY:
				if( day )
					image = R.drawable.bg_cloudy;
				else
					image = R.drawable.bg_cloudy_night;
				break;
			case WEATHER_OVERCAST:
				if( day )
					image = R.drawable.bg_overcast;
				else
					image = R.drawable.bg_overcast_night;
				break;
			case WEATHER_SNOW:
				if( day )
					image = R.drawable.bg_snow;
				else
					image = R.drawable.bg_snow;
				break;
			case WEATHER_THUNDERSTORM: //
				if( day )
					// image = R.drawable.weather_thunderstorm_day;
					image = R.drawable.bg_shower;
				else
					// image = R.drawable.weather_thunderstorm_night;
					image = R.drawable.bg_shower;
				break;
			case WEATHER_SLEET:
			case WEATHER_STORM:
			case WEATHER_LIGHTRAIN:
			case WEATHER_RAIN:
			case WEATHER_RAINSTORM:
				if( day )
					image = R.drawable.bg_rain;
				else
					image = R.drawable.bg_rain;
				break;
			case WEATHER_HAZE:
				if( day )
					image = R.drawable.bg_haze;
				else
					image = R.drawable.bg_haze;
				break;
			case WEATHER_FOG:
				if( day )
					image = R.drawable.bg_fog;
				else
					image = R.drawable.bg_fog;
				break;
			default: // 榛樿涓哄浜�
				if( day )
					image = R.drawable.bg_sunny;
				else
					image = R.drawable.bg_sunny_night;
				break;
		}
		return image;
	}
}
