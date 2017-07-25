package com.cooee.app.cooeeweather.lunarCalendar;


import android.content.res.Resources;

import com.cooee.widget.samweatherclock.R;


public class DateFormatter
{
	
	public static final String SOLAR_TERM = "solarTerm";
	public static final String GREGORIAN_FESTIVALS = "gregorianFestivals";
	public static final String LUNAR_FESTIVALS = "lunarFestivals";
	public static final String LUNAR_YEAR = "lunarYear";
	public static final String LUNAR_MONTH = "lunarMonth";
	public static final String LUNAR_DAY = "lunarDay";
	public static final String GAN_ZHI_YEAR = "ganZhiYear";
	public static final String GAN_ZHI_MONTH = "ganZhiMonth";
	public static final String GAN_ZHI_DAY = "ganZhiDay";
	public static final String CHINESE_ZODIAC = "chinese_zodiac";
	private Resources resources;
	
	public DateFormatter(
			Resources resources )
	{
		this.resources = resources;
	}
	
	private String getArrayString(
			int resid ,
			int index )
	{
		return resources.getStringArray( resid )[index];
	}
	
	public CharSequence getDayName(
			LunarCalendar lc )
	{
		StringBuilder result = new StringBuilder();
		int day = lc.getLunar( LunarCalendar.LUNAR_DAY );
		if( day < 11 )
		{
			result.append( getArrayString( R.array.chinesePrefix , 0 ) );
			result.append( getArrayString( R.array.chineseDigital , day ) );
		}
		else if( day < 20 )
		{
			result.append( getArrayString( R.array.chinesePrefix , 1 ) );
			result.append( getArrayString( R.array.chineseDigital , day - 10 ) );
		}
		else if( day == 20 )
		{
			result.append( getArrayString( R.array.chineseDigital , 2 ) );
			result.append( getArrayString( R.array.chineseDigital , 10 ) );
		}
		else if( day < 30 )
		{
			result.append( getArrayString( R.array.chinesePrefix , 2 ) );
			result.append( getArrayString( R.array.chineseDigital , day - 20 ) );
		}
		else
		{
			result.append( getArrayString( R.array.chineseDigital , 3 ) );
			result.append( getArrayString( R.array.chineseDigital , 10 ) );
		}
		return result;
	}
	
	public CharSequence getMonthName(
			LunarCalendar lc )
	{
		StringBuilder result = new StringBuilder();
		if( lc.getLunar( LunarCalendar.LUNAR_IS_LEAP ) == 1 )
		{
			result.append( getArrayString( R.array.chinesePrefix , 6 ) );
		}
		int month = lc.getLunar( LunarCalendar.LUNAR_MONTH );
		switch( month )
		{
			case 1:
				result.append( getArrayString( R.array.chinesePrefix , 3 ) );
				break;
			case 11:
				result.append( getArrayString( R.array.chinesePrefix , 4 ) );
				break;
			case 12:
				result.append( getArrayString( R.array.chinesePrefix , 5 ) );
				break;
			default:
				result.append( getArrayString( R.array.chineseDigital , month ) );
				break;
		}
		result.append( getArrayString( R.array.chineseTime , 1 ) );
		return result;
	}
	
	public CharSequence getYearName(
			LunarCalendar lc )
	{
		StringBuilder result = new StringBuilder();
		int year = lc.getLunar( LunarCalendar.LUNAR_YEAR );
		int resid = R.array.chineseDigital;
		result.append( getArrayString( resid , ( year / 1000 ) % 10 ) );
		result.append( getArrayString( resid , ( year / 100 ) % 10 ) );
		result.append( getArrayString( resid , ( year / 10 ) % 10 ) );
		result.append( getArrayString( resid , year % 10 ) );
		result.append( getArrayString( R.array.chineseTime , 0 ) );
		return result;
	}
}
