package com.cooee.app.cooeeweather.dataentity;


import java.util.ArrayList;


public class weatherdataentity
{
	
	// field name
	public static final String UPDATE_MILIS = "updateMilis";
	public static final String CITY = "city";
	public static final String POSTALCODE = "postalCode";
	public static final String FORECASTDATE = "forecastDate";
	public static final String CONDITION = "condition";
	public static final String TEMPF = "tempF";
	public static final String TEMPC = "tempC";
	public static final String HUMIDITY = "humidity";
	public static final String ICON = "icon";
	public static final String WINDCONDITION = "windCondition";
	public static final String LAST_UPDATE_TIME = "lastUpdateTime";
	public static final String IS_CONFIGURED = "isConfigured";
	public static final String TEMPH = "tempH";
	public static final String TEMPL = "tempL";
	public static final String LUNARCALENDAR = "lunarcalendar";
	public static final String ULTRAVIOLETRAY = "ultravioletray";
	public static final String WEATHERTIME = "weathertime";
	public static final String ENTITYID = "entityid";
	public static final String LOCATIONCODE = "locationcode";
	public static final String DEGREETYPE = "degreetype";
	public static final String TIMEPOSTMARK = "timepostmark";
	// projection
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
			LUNARCALENDAR ,
			ULTRAVIOLETRAY ,
			WEATHERTIME ,
			TEMPH ,
			TEMPL };
	// field
	private ArrayList<weatherforecastentity> details = new ArrayList<weatherforecastentity>();
	private Integer id;
	private Integer updateMilis; // 更新时间问隔，单位为小时
	private String city;
	private String postalCode;
	private Long forecastDate; // 天气预报的发布时间，由Date.toLong()得到
	private String condition; // 天气情况，如晴，雨，多云
	private Integer tempF;
	private Integer tempC;
	private String humidity; // 湿度
	private String icon; // 
	private String windCondition; // 风力
	private Long lastUpdateTime; // 上一次更新时间，由Date.toLong()得到
	private Integer isConfigured;
	private Integer tempH;
	private Integer tempL;
	private String lunarcalendar;//农历
	private String ultravioletray;//紫外线
	private String weathertime;//服务器返回的两个时间
	
	public String getWeathertime()
	{
		return weathertime;
	}
	
	public void setWeathertime(
			String weathertime )
	{
		this.weathertime = weathertime;
	}
	
	public void setTestData()
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
		lunarcalendar = "农历十一月初二";
		ultravioletray = "弱";
		weathertime = "06:37,16:51";
		weatherforecastentity forecastEntity = new weatherforecastentity();
		forecastEntity.setTestData();
		details.add( forecastEntity );
		details.add( forecastEntity );
		details.add( forecastEntity );
		details.add( forecastEntity );
	}
	
	/**
	 * @return the id
	 */
	public Integer getId()
	{
		return id;
	}
	
	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(
			Integer id )
	{
		this.id = id;
	}
	
	/**
	 * @return the updateMilis
	 */
	public Integer getUpdateMilis()
	{
		return updateMilis;
	}
	
	/**
	 * @param updateMilis
	 *            the updateMilis to set
	 */
	public void setUpdateMilis(
			Integer updateMilis )
	{
		this.updateMilis = updateMilis;
	}
	
	public ArrayList<weatherforecastentity> getDetails()
	{
		return details;
	}
	
	public void setDetails(
			ArrayList<weatherforecastentity> details )
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
	
	public String getCondition()
	{
		return condition;
	}
	
	public void setCondition(
			String condition )
	{
		this.condition = condition;
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
	
	public String getIcon()
	{
		return icon;
	}
	
	public void setIcon(
			String icon )
	{
		this.icon = icon;
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
	
	/**
	 * @param isConfigured
	 *            the isConfigured to set
	 */
	public void setIsConfigured(
			Integer isConfigured )
	{
		this.isConfigured = isConfigured;
	}
	
	/**
	 * @return the isConfigured
	 */
	public Integer getIsConfigured()
	{
		return isConfigured;
	}
	
	/**
	 * @param forecastDate
	 *            the forecastDate to set
	 */
	public void setForecastDate(
			Long forecastDate )
	{
		this.forecastDate = forecastDate;
	}
	
	/**
	 * @return the forecastDate
	 */
	public Long getForecastDate()
	{
		return forecastDate;
	}
	
	/**
	 * @param lastUpdateTime
	 *            the lastUpdateTime to set
	 */
	public void setLastUpdateTime(
			Long lastUpdateTime )
	{
		this.lastUpdateTime = lastUpdateTime;
	}
	
	/**
	 * @return the lastUpdateTime
	 */
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
	
	public String getLunarcalendar()
	{
		return lunarcalendar;
	}
	
	public void setLunarcalendar(
			String lunarcalendar )
	{
		this.lunarcalendar = lunarcalendar;
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
	
	public String getUltravioletray()
	{
		return ultravioletray;
	}
	
	public void setUltravioletray(
			String ultravioletray )
	{
		this.ultravioletray = ultravioletray;
	}
}
