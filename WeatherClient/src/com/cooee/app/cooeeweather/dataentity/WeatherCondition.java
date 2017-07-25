package com.cooee.app.cooeeweather.dataentity;


import com.cooee.app.cooeeweather.filehelp.Log;


public class WeatherCondition
{
	
	private static final String TAG = "com.cooee.weather.dataentity.WeatherCondition";
	
	public static enum Condition
	{
		WEATHER_FINE , // 晴朗
		WEATHER_CLOUDY , // 多云
		WEATHER_HAZE , // 雾霾
		WEATHER_OVERCAST , // 阴
		WEATHER_SNOW , // 雪
		WEATHER_SLEET , // 雨夹雪
		WEATHER_THUNDERSTORM , // 雷阵雨
		WEATHER_STORM , // 阵雨
		WEATHER_LIGHTRAIN , // 小雨
		WEATHER_RAIN , // 中雨
		WEATHER_RAINSTORM , // 大雨
		WEATHER_FOG , // 大雾
		WEATHER_HOT , // 炎热
		WEATHER_UNKOWN
	};
	
	public final static String des[][] = { { // 晴朗
			"晴" , "以晴为主" , "晴间多云" , "Clear" , "Fine" , "Mostly Sunny" , "Partly Sunny" , "Fair" , "Sunny" } ,
			{ // 多云
			"多云" , "局部多云" , "Mostly Cloudy" , "Partly Cloudy" , "scattered clouds" , "Cloudy" , "Clouds" , "few clouds" , "broken clouds" } ,
			{ // 雾霾
			"雾霾" , "烟雾" , "沙尘暴" , "浮尘" , "扬沙" , "强沙尘暴" , "霾" , "Smoke" , "Haze" } ,
			{ // 阴
			"阴" , "Overcast" } ,
			{ // 雪
			"雪" , "小雪" , "中雪" , "大雪" , "暴雪" , "阵雪" , "小到中雪" , "中到大雪" , "大到暴雪" , "Light snow" , "Flurries" , "Snow" } ,
			{ // 雨夹雪
			"雨夹雪" , "Sleet" } ,
			{ // 雷阵雨
			"雷阵雨" , "雷阵雨伴有冰雹" , "Thunderstorm" , "T-storms" } ,
			{ // 阵雨
			"阵雨" , "Storm" , "Showers" } ,
			{ // 小雨
			"小雨" , "可能有雨" , "可能有暴风雨" , "Chance of Rain" , "Chance of Storm" , "Light rain" , "sprinkles" , "Light rain" } ,
			{ // 中雨
			"中雨" , "雨" , "小到中雨" , "冻雨" , "Moderate rain" , "Rain" } ,
			{ // 大雨
			"大雨" , "中到大雨" , "Pour" } ,
			{ // 大雨
			"暴雨" , "大到暴雨" , "特大暴雨" , "暴雨到大暴雨" , "大暴雨到特大暴雨" , "Rainstorm" } ,
			{ // 大雾
			"雾" , "Fog" } ,
			{ // 炎热
			"炎热" , "Hot" } };
	public final static Condition con[] = { Condition.WEATHER_FINE , // 晴朗
			Condition.WEATHER_CLOUDY , // 多云
			Condition.WEATHER_HAZE , // 雾霾
			Condition.WEATHER_OVERCAST , // 阴
			Condition.WEATHER_SNOW , // 雪
			Condition.WEATHER_SLEET , // 雨夹雪
			Condition.WEATHER_THUNDERSTORM , // 雷阵雨
			Condition.WEATHER_STORM , // 阵雨
			Condition.WEATHER_LIGHTRAIN , // 小雨
			Condition.WEATHER_RAIN , // 中雨
			Condition.WEATHER_RAINSTORM , // 大雨
			Condition.WEATHER_RAINSTORM , // 大雨
			Condition.WEATHER_FOG , // 大雾
			Condition.WEATHER_HOT , // 炎热
	};
	
	public static Condition convertCondition(
			String s )
	{
		Condition c = Condition.WEATHER_UNKOWN;
		int index = s.indexOf( "转" );
		String new_s = s;
		if( index != -1 )
		{
			new_s = s.substring( 0 , index );
			Log.v( TAG , "s = " + s + ", new_s = " + new_s );
		}
		for( int i = 0 ; i < des.length ; i++ )
		{
			for( int j = 0 ; j < des[i].length ; j++ )
			{
				if( des[i][j].equalsIgnoreCase( new_s ) || ( new_s != null && new_s.toLowerCase().contains( des[i][j].toLowerCase() ) ) )
				{
					c = con[i];
					return c;
				}
			}
		}
		return c;
	}
	
	public static String convertCondition(
			String s ,
			String language )
	{
		int index = s.indexOf( "转" );
		String new_s = s;
		if( index != -1 )
		{
			new_s = s.substring( 0 , index );
			Log.v( TAG , "s = " + s + ", new_s = " + new_s );
		}
		for( int i = 0 ; i < des.length ; i++ )
		{
			for( int j = 0 ; j < des[i].length ; j++ )
			{
				if( des[i][j].equalsIgnoreCase( new_s ) || ( new_s != null && new_s.toLowerCase().contains( des[i][j].toLowerCase() ) ) )
				{
					if( "CN".equals( language ) )
					{
						//Log.i( TAG , "des[i][0] = " + des[i][0] );
						return des[i][0];
					}
					else
					{
						//Log.i( TAG , "des[i][des[i].length - 1] = " + des[i][des[i].length - 1] );
						return des[i][des[i].length - 1];
					}
				}
			}
		}
		Log.i( TAG , "s = " + s );
		return s;
	}
}
