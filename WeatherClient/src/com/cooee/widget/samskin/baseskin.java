package com.cooee.widget.samskin;


import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;


public abstract class baseskin
{
	
	private static final String WEATHER_URI = "content://com.cooee.app.cooeeweather.dataprovider/weather";
	
	public abstract int getLayout();
	
	public abstract void updateViews(
			Context context ,
			int widgetId ,
			RemoteViews rv );
	
	public static weatherdataentity readData(
			Context context ,
			RemoteViews updateViews ,
			int widgetId ,
			String postalCode )
	{
		weatherdataentity dataEntity = null;
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		Uri CONTENT_URI;
		CONTENT_URI = Uri.parse( WEATHER_URI + "/" + postalCode );
		String selection = weatherdataentity.POSTALCODE + "=" + "'" + postalCode + "'";
		cursor = resolver.query( CONTENT_URI , weatherdataentity.projection , selection , null , null );
		if( cursor != null )
		{
			dataEntity = new weatherdataentity();
			if( cursor.moveToFirst() )
			{
				dataEntity.setUpdateMilis( cursor.getInt( 0 ) );
				dataEntity.setCity( cursor.getString( 1 ) );
				dataEntity.setPostalCode( cursor.getString( 2 ) );
				dataEntity.setForecastDate( cursor.getLong( 3 ) );
				dataEntity.setCondition( cursor.getString( 4 ) );
				dataEntity.setTempF( cursor.getInt( 5 ) );
				dataEntity.setTempC( cursor.getInt( 6 ) );
				dataEntity.setHumidity( cursor.getString( 7 ) );
				dataEntity.setIcon( cursor.getString( 8 ) );
				dataEntity.setWindCondition( cursor.getString( 9 ) );
				dataEntity.setLastUpdateTime( cursor.getLong( 10 ) );
				dataEntity.setIsConfigured( cursor.getInt( 11 ) );
				dataEntity.setLunarcalendar( cursor.getString( 12 ) );
				dataEntity.setUltravioletray( cursor.getString( 13 ) );
				dataEntity.setWeathertime( cursor.getString( 14 ) );
			}
			cursor.close();
		}
		int details_count = 0;
		if( dataEntity != null )
		{
			CONTENT_URI = Uri.parse( WEATHER_URI + "/" + postalCode + "/detail" );
			selection = weatherforecastentity.CITY + "=" + "'" + postalCode + "'";
			cursor = resolver.query( CONTENT_URI , weatherforecastentity.forecastProjection , selection , null , null );
			if( cursor != null )
			{
				weatherforecastentity forecast;
				while( cursor.moveToNext() )
				{
					forecast = new weatherforecastentity();
					forecast.setDayOfWeek( cursor.getInt( 2 ) );
					forecast.setLow( cursor.getInt( 3 ) );
					forecast.setHight( cursor.getInt( 4 ) );
					forecast.setIcon( cursor.getString( 5 ) );
					forecast.setCondition( cursor.getString( 6 ) );
					// forecast.setWidgetId(cursor.getInt(6));
					dataEntity.getDetails().add( forecast );
					details_count = details_count + 1;
				}
				cursor.close();
			}
		}
		if( details_count < 4 )
		{
			dataEntity = null;
		}
		return dataEntity;
	}
}
