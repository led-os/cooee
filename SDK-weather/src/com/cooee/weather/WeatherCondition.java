package com.cooee.weather;


import android.content.Context;


/**
 * 天气情况相关的类
 * @author fulijuan 2017-4-13
 *
 */
public class WeatherCondition
{
	
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
	
	/**
	 * 中英文数组
	 */
	private final static String des[][] = {
			{ // 晴朗
					"晴" ,
					"以晴为主" ,
					"晴间多云" ,
					"Clear" ,
					"Sunny" ,
					"Fine" ,
					"Mostly Sunny" ,
					"Partly Sunny" ,
					"Fair" } ,
			{ // 多云
					"多云" ,
					"局部多云" ,
					"Mostly Cloudy" ,
					"Partly Cloudy" ,
					"Cloudy" ,
					"scattered clouds" ,
					"Clouds" ,
					"few clouds" ,
					"broken clouds" } ,
			{ // 雾霾
					"雾霾" ,
					"烟雾" ,
					"沙尘暴" ,
					"浮尘" ,
					"扬沙" ,
					"霾" ,
					"强沙尘暴" ,
					"Smoke" ,
					"Haze" } ,
			{ // 阴
					"阴" ,
					"Overcast" } ,
			{ // 雪
					"雪" ,
					"小雪" ,
					"中雪" ,
					"大雪" ,
					"暴雪" ,
					"阵雪" ,
					"小到中雪" ,
					"中到大雪" ,
					"大到暴雪" ,
					"Light snow" ,
					"Flurries" ,
					"Snow" } ,
			{ // 雨夹雪
					"雨夹雪" ,
					"Sleet" } ,
			{ // 雷阵雨
					"雷阵雨" ,
					"雷阵雨伴有冰雹" ,
					"Thunderstorm" ,
					"T-storms" } ,
			{ // 阵雨
					"阵雨" ,
					"Storm" ,
					"Showers" } ,
			{ // 小雨
					"小雨" ,
					"可能有雨" ,
					"可能有暴风雨" ,
					"Chance of Rain" ,
					"Chance of Storm" ,
					"Light rain" ,
					"sprinkles" ,
					"Light rain" } ,
			{ // 中雨
					"中雨" ,
					"雨" ,
					"小到中雨" ,
					"冻雨" ,
					"Rain" ,
					"Moderate rain" } ,
			{ // 大雨
					"大雨" ,
					"中到大雨" ,
					"Pour" } ,
			{ // 大雨
					"暴雨" ,
					"大到暴雨" ,
					"特大暴雨" ,
					"暴雨到大暴雨" ,
					"大暴雨到特大暴雨" ,
					"Rainstorm" } ,
			{ // 大雾
					"雾" ,
					"Fog" } ,
			{ // 炎热
					"炎热" ,
					"Hot" } };
	/**
	 * 中文数组
	 */
	private final static Condition con[] = {
			Condition.WEATHER_FINE , // 晴朗
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
			Condition.WEATHER_HOT, // 炎热
	};
	
	/**
	 * 获取对应的中文天气字符串
	 * @param s 天气情况字符串
	 * @return 对应天气字符串
	 */
	public static Condition getConditionString(
			String s )
	{
		Condition c = Condition.WEATHER_UNKOWN;
		int index = s.indexOf( "转" );
		String new_s = s;
		if( index != -1 )
		{
			new_s = s.substring( 0 , index );
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
	
	/**
	 * 获取对应的天气字符串，根据天气情况和当前系统语言
	 * @param condition 天气情况
	 * @param language 当前系统语言
	 * @return 对应天气字符串
	 */
	public static String getConditionStringByLanguage(
			Context context ,
			String condition )
	{
		/**获取语言*/
		String language = context.getResources().getConfiguration().locale.getCountry();
		if( condition == null )
			condition = "";
		String result = null;
		int index = condition.indexOf( "转" );
		if( index != -1 )
			condition = condition.substring( 0 , index );
		for( String[] strings : des )
		{
			for( String string : strings )
			{
				if( string.trim().equalsIgnoreCase( condition.trim() ) || ( condition != null && condition.trim().toLowerCase().contains( string.trim().toLowerCase() ) ) )
				{
					if( "CN".equalsIgnoreCase( language ) )
						result = strings[0];
					else
						result = strings[strings.length - 1];
					break;
				}
			}
			if( result != null )
				break;
		}
		return result == null ? "WEATHER_UNKOWN" : result;
	}
}
