package com.cooee.widget.Transkin;


import com.cooee.weather.WeatherEntity;

import android.content.Context;
import android.widget.RemoteViews;


/**
 * 读取天气数据
 * @author fulijuan
 *
 */
public abstract class BaseSkin
{
	
	public abstract int getLayout();
	
	public abstract void updateViews(
			Context context ,
			int widgetId ,
			RemoteViews rv ,
			WeatherEntity dateWeatherdataentity );
}
