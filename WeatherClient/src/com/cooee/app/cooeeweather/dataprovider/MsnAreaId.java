package com.cooee.app.cooeeweather.dataprovider;


public class MsnAreaId
{
	
	private String mWeatherLocationName;
	private String mEntityID;
	private String mWeatherLocationCode;
	private String mSreachKey;
	private String mLong;
	private String mLat;
	
	//mWeatherLocationName
	public String AreagetWeatherLocationName()
	{
		return mWeatherLocationName;
	}
	
	public void AreasetForwcastShortDay(
			String WeatherLocationName )
	{
		this.mWeatherLocationName = WeatherLocationName;
	}
	
	//mEntityID
	public String AreagetEntityID()
	{
		return mEntityID;
	}
	
	public void AreasetEntityID(
			String EntityID )
	{
		this.mEntityID = EntityID;
	}
	
	//mWeatherLocationCode
	public String AreagetWeatherLocationCode()
	{
		return mWeatherLocationCode;
	}
	
	public void AreasetWeatherLocationCode(
			String WeatherLocationCode )
	{
		this.mWeatherLocationCode = WeatherLocationCode;
	}
	
	//mSreachKey
	public String AreagetSreachKey()
	{
		return mSreachKey;
	}
	
	public void AreasetSreachKey(
			String SreachKey )
	{
		this.mSreachKey = SreachKey;
	}
	
	//mLong
	public String AreagetLong()
	{
		return mLong;
	}
	
	public void AreasetLong(
			String Long )
	{
		this.mLong = Long;
	}
	
	//mLat
	public String AreagetLat()
	{
		return mLat;
	}
	
	public void AreasetLat(
			String Lat )
	{
		this.mLat = Lat;
	}
}
