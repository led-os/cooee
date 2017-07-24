package com.cooee.weather;


import java.util.ArrayList;

import android.provider.BaseColumns;


/**
 * 根据以下两个uri获取的数据抽取出来的公共实体类
 * ① WEATHER_URI + "/" + postalCode
 * ② WEATHER_URI + "/" + postalCode + "/detail"
 */
public class WeatherEntity
{
	
	/**
	 * ① WEATHER_URI + "/" + postalCode  field name  start
	 */
	public static final String UPDATE_MILIS = "updateMilis";
	public static final String POSTALCODE = "postalCode";
	public static final String FORECASTDATE = "forecastDate";
	public static final String TEMPF = "tempF";
	public static final String TEMPC = "tempC";
	public static final String HUMIDITY = "humidity";
	public static final String WINDCONDITION = "windCondition";
	public static final String LAST_UPDATE_TIME = "lastUpdateTime";
	public static final String IS_CONFIGURED = "isConfigured";
	public static final String TEMPH = "tempH";
	public static final String TEMPL = "tempL";
	/**
	 *  ① WEATHER_URI + "/" + postalCode  field name  end
	 */
	//相同的field name  start
	public static final String ICON = "icon";
	public static final String CONDITION = "condition";
	public static final String CITY = "city";
	//相同的field name  end
	/**
	 *  ② WEATHER_URI + "/" + postalCode + "/detail"  field name  start
	 */
	public static final String DAYOFWEEK = "dayOfWeek";
	public static final String LOW = "low";
	public static final String HIGHT = "hight";
	/**
	 *  ② WEATHER_URI + "/" + postalCode + "/detail"  field name  end
	 */
	//① WEATHER_URI + "/" + postalCode  projection
	public static final String[] projection = new String[]{
			UPDATE_MILIS ,
			CITY ,
			POSTALCODE ,
			FORECASTDATE ,
			CONDITION ,
			TEMPF ,
			TEMPC ,
			HUMIDITY ,
			ICON ,
			WINDCONDITION ,
			LAST_UPDATE_TIME ,
			IS_CONFIGURED ,
			TEMPH ,
			TEMPL };
	//② WEATHER_URI + "/" + postalCode + "/detail"  detailProjection
	public static final String[] detailProjection = new String[]{ BaseColumns._ID , CITY , DAYOFWEEK , LOW , HIGHT , ICON , CONDITION , };
	/**
	 * ① WEATHER_URI + "/" + postalCode属性 start
	 */
	private ArrayList<WeatherEntity> details = new ArrayList<WeatherEntity>();
	private Integer updateMilis; // 更新时间问隔，单位为小时
	private String postalCode;
	private Long forecastDate; // 天气预报的发布时间，由Date.toLong()得到
	private Integer tempF;
	private Integer tempC; //对应curTempC
	private String humidity; // 
	private String windCondition; // 风力
	private Long lastUpdateTime; // 上一次更新时间，由Date.toLong()得到
	private Integer isConfigured;
	private Integer tempH; //对应HTempC
	private Integer tempL; //对应LTempC
	public String weather_index;//天气大类
	/**
	 * ① WEATHER_URI + "/" + postalCode属性 end
	 */
	//相同的属性 start
	private Integer id;
	private String city; //对应cityName
	private String icon;
	private String condition; // 天气情况，如晴，雨，多云  //对应weather
	//相同的属性 end
	/**
	 *  ② WEATHER_URI + "/" + postalCode + "/detail"属性 start
	 */
	private Integer dayOfWeek;
	private Integer low;
	private Integer hight;
	private Integer widgetId;
	
	/**
	 *  ② WEATHER_URI + "/" + postalCode + "/detail"属性  end
	 */
	/*	public void setTestData()
		{
			id = 0;
			updateMilis = 0;
			city = "上海";
			postalCode = "上海";
			forecastDate = 0L;
			condition = "多云";
			tempF = 0;
			tempC = 27;
			humidity = "30%";
			icon = "/img/cloudy.gif";
			windCondition = "4-5级东南风";
			lastUpdateTime = 0L;
			isConfigured = 1;
			WeatherForecastEntity forecastEntity = new WeatherForecastEntity();
			forecastEntity.setTestData();
			details.add( forecastEntity );
			details.add( forecastEntity );
			details.add( forecastEntity );
			details.add( forecastEntity );
		}*/
	public Integer getUpdateMilis()
	{
		return updateMilis;
	}
	
	public void setUpdateMilis(
			Integer updateMilis )
	{
		this.updateMilis = updateMilis;
	}
	
	public ArrayList<WeatherEntity> getDetails()
	{
		return details;
	}
	
	public void setDetails(
			ArrayList<WeatherEntity> details )
	{
		this.details = details;
	}
	
	public String getCity()
	{
		return city;
	}
	
	public void setCity(
			String city )
	{
		this.city = city;
	}
	
	public String getPostalCode()
	{
		return postalCode;
	}
	
	public void setPostalCode(
			String postalCode )
	{
		this.postalCode = postalCode;
	}
	
	public Integer getTempF()
	{
		return tempF;
	}
	
	public void setTempF(
			Integer tempF )
	{
		this.tempF = tempF;
	}
	
	public Integer getTempC()
	{
		return tempC;
	}
	
	public void setTempC(
			Integer tempC )
	{
		this.tempC = tempC;
	}
	
	public String getHumidity()
	{
		return humidity;
	}
	
	public void setHumidity(
			String humidity )
	{
		this.humidity = humidity;
	}
	
	public String getWindCondition()
	{
		return windCondition;
	}
	
	public void setWindCondition(
			String windCondition )
	{
		this.windCondition = windCondition;
	}
	
	public void setIsConfigured(
			Integer isConfigured )
	{
		this.isConfigured = isConfigured;
	}
	
	public Integer getIsConfigured()
	{
		return isConfigured;
	}
	
	public void setForecastDate(
			Long forecastDate )
	{
		this.forecastDate = forecastDate;
	}
	
	public Long getForecastDate()
	{
		return forecastDate;
	}
	
	public void setLastUpdateTime(
			Long lastUpdateTime )
	{
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public Long getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public Integer getTempH()
	{
		return tempH;
	}
	
	public void setTempH(
			Integer tempH )
	{
		this.tempH = tempH;
	}
	
	public Integer getTempL()
	{
		return tempL;
	}
	
	public void setTempL(
			Integer tempL )
	{
		this.tempL = tempL;
	}
	
	public String getWeather_index()
	{
		return weather_index;
	}
	
	public void setWeather_index(
			String weather_index )
	{
		this.weather_index = weather_index;
	}
	
	public Integer getId()
	{
		return id;
	}
	
	public void setId(
			Integer id )
	{
		this.id = id;
	}
	
	public Integer getDayOfWeek()
	{
		return dayOfWeek;
	}
	
	public void setDayOfWeek(
			Integer dayOfWeek )
	{
		this.dayOfWeek = dayOfWeek;
	}
	
	public Integer getLow()
	{
		return low;
	}
	
	public void setLow(
			Integer low )
	{
		this.low = low;
	}
	
	public Integer getHight()
	{
		return hight;
	}
	
	public void setHight(
			Integer hight )
	{
		this.hight = hight;
	}
	
	public String getIcon()
	{
		return icon;
	}
	
	public void setIcon(
			String icon )
	{
		this.icon = icon;
	}
	
	public String getCondition()
	{
		return condition;
	}
	
	public void setCondition(
			String condition )
	{
		this.condition = condition;
	}
	
	public void setWidgetId(
			Integer widgetId )
	{
		this.widgetId = widgetId;
	}
	
	public Integer getWidgetId()
	{
		return widgetId;
	}
	
	public String getDstailCity()
	{
		return city;
	}
	
	public void setDetailCity(
			String cityname )
	{
		this.city = cityname;
	}
	
	@Override
	public boolean equals(
			java.lang.Object o )
	{
		if( o != null && o instanceof WeatherEntity )
		{
			boolean result = true;
			// 只使用到了城市名,天气图片index和当前温度,所以相等判断只比较这三个值
			result &= isEqual( this.city , ( (WeatherEntity)o ).getCity() );
			result &= isEqual( this.weather_index , ( (WeatherEntity)o ).getWeather_index() );
			result &= isEqual( this.tempC , ( (WeatherEntity)o ).getTempC() );
			return result;
		}
		return false;
	}
	
	/**
	 * 判断两个Object是否相等
	 * 两个Object都为空视为相等,一个为空且另一个不为空或者两个不为空的值不同视为不相等
	 * @param obj1
	 * @param obj2
	 * @return 相等返回true,不相等返回false
	 * @author yangtianyu 2016-8-24
	 */
	private boolean isEqual(
			Object obj1 ,
			Object obj2 )
	{
		if( obj1 != null )
			return obj1.equals( obj2 );
		else
			if( obj2 != null )
			return false;
		else
			return true;
	}
}
