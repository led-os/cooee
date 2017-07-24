package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;

import com.cooee.phenix.camera.R;


public class TimeUtils
{
	
	@SuppressLint( "SimpleDateFormat" )
	public static String getCurrentDate(
			Context context )
	{
		StringBuilder result = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis( System.currentTimeMillis() );
		result.append( calendar.get( Calendar.YEAR ) );
		result.append( "." );
		int month = calendar.get( Calendar.MONTH ) + 1;
		if( month < 10 )
			result.append( 0 );
		result.append( month );
		result.append( "." );
		int date = calendar.get( Calendar.DATE );
		if( date < 10 )
			result.append( 0 );
		result.append( date );
		result.append( " " );
		int number = calendar.get( Calendar.DAY_OF_WEEK );//星期表示1-7，是从星期日开始，  
		if( context != null )
			result.append( context.getResources().getStringArray( R.array.camera_page_weekday )[number - 1] );
		return result.toString();
	}
}
