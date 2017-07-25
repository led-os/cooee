package com.cooee.widget.samweatherclock;


import com.cooee.app.cooeeweather.dataprovider.weatherdataprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class WeatherNewApiBroadcastReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		String action = intent.getAction();
		System.out.println( "shlt , WeatherNewApiBroadcastReceiver , action : " + action );
		if( "com.cooee.app.weatherclientrefreshweather".equals( action ) )
		{// 刷新数据
			System.out.println( "shlt , WeatherNewApiBroadcastReceiver , 刷新数据" );
			intent = new Intent( context , com.cooee.app.cooeeweather.dataprovider.weatherDataService.class );
			intent.setAction( "com.cooee.app.cooeeweather.dataprovider.weatherDataService" );
			intent.putExtra( "forcedUpdate" , 1 );
			context.startService( intent );
		}
		else if( "com.cooee.app.weatherclienturisearch".equals( action ) )
		{// uri search
			System.out.println( "shlt , WeatherNewApiBroadcastReceiver , uri search" );
			intent = new Intent( "com.cooee.app.weatherclienturisearchresult" );
			intent.putExtra( "com.cooee.app.weatherclienturisearchresult.tag" , context.getPackageName() );
			intent.putExtra( "com.cooee.app.weatherclienturisearchresult.listaneruri" , weatherdataprovider.DB_LISTENER_URI );
			intent.putExtra( "com.cooee.app.weatherclienturisearchresult.cityuri" , weatherdataprovider.QUERY_CITY_URI );
			intent.putExtra( "com.cooee.app.weatherclienturisearchresult.weathertodayuri" , weatherdataprovider.QUERY_CUR_WEATHER_URI );
			intent.putExtra( "com.cooee.app.weatherclienturisearchresult.weatherforecasturi" , weatherdataprovider.QUERY_FUTURE_WEATHER_URI );
			context.sendBroadcast( intent );
		}
		context = null;
		intent = null;
		action = null;
	}
}
