package com.cooee.widgetnative.ALL3in1.ClockWeather.common;


public class WeatherEntity
{
	
	public String cityName;
	public String weather;
	public int curTempC;
	public int HTempC;
	public int LTempC;
	public String weather_index;
	
	public String getWeather_index()
	{
		return weather_index;
	}
	
	public void setWeather_index(
			String weather_index )
	{
		this.weather_index = weather_index;
	}
	
	public String getCityName()
	{
		return cityName;
	}
	
	public void setCityName(
			String cityName )
	{
		this.cityName = cityName;
	}
	
	public String getWeather()
	{
		return weather;
	}
	
	public void setWeather(
			String weather )
	{
		this.weather = weather;
	}
	
	public int getCurTempC()
	{
		return curTempC;
	}
	
	public void setCurTempC(
			int curTempC )
	{
		this.curTempC = curTempC;
	}
	
	public int getHTempC()
	{
		return HTempC;
	}
	
	public void setHTempC(
			int hTempC )
	{
		HTempC = hTempC;
	}
	
	public int getLTempC()
	{
		return LTempC;
	}
	
	public void setLTempC(
			int lTempC )
	{
		LTempC = lTempC;
	}
}
