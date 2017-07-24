package com.cooee.weather;


/**
 * 天气资源图片的相关类
 * @author fulijuan
 */
public class WeatherIMG
{
	
	// 雾尘  weather_data_fog.png
	public static int FOGS = -1;
	// 阴天  weather_data_overcast.png
	public static int OVERCASTS = -1;
	// 多云  weather_data_mostcloudy.png
	public static int MOSTCLOUDYS = -1;
	// 雨  weather_data_rain.png
	public static int RAINS = -1;
	// 雪  weather_data_snow.png
	public static int SNOWS = -1;
	// 晴  weather_data_sunny.png
	public static int SUNNYS = -1;
	// 雷  weather_data_thunderstorm.png
	public static int THUNDERSTORMS = -1;
	// 没有对应数据时 默认显示
	public static int UNKONWN = -1;
	
	/**
	 * 初始化天气图片资源
	 * @param weather_fog_drawable_id
	 * @param weather_mostcloudy_drawable_id
	 * @param weather_overcast_drawable_id
	 * @param weather_rain_drawable_id
	 * @param weather_snow_drawable_id
	 * @param weather_sunny_drawable_id
	 * @param weather_thunderstorm_id
	 * @param weather_unknow_drawable_id
	 */
	public static void initResId(
			int weather_fog_drawable_id ,
			int weather_mostcloudy_drawable_id ,
			int weather_overcast_drawable_id ,
			int weather_rain_drawable_id ,
			int weather_snow_drawable_id ,
			int weather_sunny_drawable_id ,
			int weather_thunderstorm_id ,
			int weather_unknow_drawable_id )
	{
		FOGS = weather_fog_drawable_id;
		MOSTCLOUDYS = weather_mostcloudy_drawable_id;
		OVERCASTS = weather_overcast_drawable_id;
		RAINS = weather_rain_drawable_id;
		SNOWS = weather_snow_drawable_id;
		SUNNYS = weather_sunny_drawable_id;
		THUNDERSTORMS = weather_thunderstorm_id;
		UNKONWN = weather_unknow_drawable_id;
	}
	
	/**
	 * 根据天气index，获取对应的天气图片资源
	 * @param weather_index
	 * @return
	 */
	public static int getWeatherDataImageIdByIndex(
			String weather_index )
	{
		if( "WEATHER_OVERCAST".equals( weather_index ) )
			return WeatherIMG.OVERCASTS;
		if( "WEATHER_FOG".equals( weather_index ) || "WEATHER_HAZE".equals( weather_index ) )
			return WeatherIMG.FOGS;
		if( "WEATHER_CLOUDY".equals( weather_index ) )
			return WeatherIMG.MOSTCLOUDYS;
		if( "WEATHER_RAIN".equals( weather_index ) || "WEATHER_LIGHTRAIN".equals( weather_index ) || "WEATHER_RAINSTORM".equals( weather_index ) || "WEATHER_STORM".equals( weather_index ) )
			return WeatherIMG.RAINS;
		if( "WEATHER_SNOW".equals( weather_index ) || "WEATHER_SLEET".equals( weather_index ) )
			return WeatherIMG.SNOWS;
		if( "WEATHER_FINE".equals( weather_index ) || "WEATHER_HOT".equals( weather_index ) )
			return WeatherIMG.SUNNYS;
		if( "WEATHER_THUNDERSTORM".equals( weather_index ) )
			return WeatherIMG.THUNDERSTORMS;
		else
			return WeatherIMG.UNKONWN;
	}
	
	/**
	 * 根据天气情况，获取对应的天气图片资源
	 * @param condition
	 * @return
	 */
	public static int getWeatherDataImageIdByCondition(
			String condition )
	{
		WeatherCondition.Condition c = WeatherCondition.getConditionString( condition );
		int image = 0;
		switch( c )
		{
			case WEATHER_FINE:
			case WEATHER_HOT:
				image = WeatherIMG.SUNNYS;
				break;
			case WEATHER_CLOUDY:
				image = WeatherIMG.MOSTCLOUDYS;
				break;
			case WEATHER_OVERCAST:
				image = WeatherIMG.OVERCASTS;
				break;
			case WEATHER_HAZE:
			case WEATHER_FOG:
				image = WeatherIMG.FOGS;
				break;
			case WEATHER_SLEET:
			case WEATHER_SNOW:
				image = WeatherIMG.SNOWS;
				break;
			case WEATHER_THUNDERSTORM:
				image = WeatherIMG.THUNDERSTORMS;
				break;
			case WEATHER_STORM:
			case WEATHER_LIGHTRAIN:
			case WEATHER_RAIN:
			case WEATHER_RAINSTORM:
				image = WeatherIMG.RAINS;
				break;
			default:
				image = WeatherIMG.UNKONWN;
				break;
		}
		return image;
	}
}
