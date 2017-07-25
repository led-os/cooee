package com.cooee.app.cooeeweather.dataprovider;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.dataprovider.weatherwebservice.FLAG_UPDATE;
import com.cooee.app.cooeeweather.filehelp.Log;


public class cooeeServiceQuery
{
	
	private static final String TAG = "com.cooee.weather.dataprovider.cooeeServiceQuery";
	// COOEE 预报URL
	public final static String COOEE_FORCAST_URL_INLAND = "http://widget.coeeland.com/w2/tianqi2.ashx?city=%s&m=2603&l=320480&f=2424&s=B01_HVGA&imsi=460008623197253&sc=+8613800210500&iccid=898600810910f6287253";
	private static String Update_city = null;
	//weijie_20121210_01
	private static final int TIMEOUT_CONNECT = 10 * 1000;//设置请求超时10秒钟  
	private static final int TIMEOUT_SOCKET = 30 * 1000; //设置等待数据超时时间10秒钟 
	
	public static weatherdataentity CooeeWeatherDataUpdate(
			String city_num )
	{
		weatherdataentity dataentity = null;
		// weather forcast data
		if( city_num == null )
		{
			Log.v( TAG , "No postalcode" );
			return null;
		}
		Update_city = city_num;
		Reader responseReader = null;
		//weijie_20121210_01 
		BasicHttpParams httpParameters = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
		HttpConnectionParams.setConnectionTimeout( httpParameters , TIMEOUT_CONNECT );// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.  
		HttpConnectionParams.setSoTimeout( httpParameters , TIMEOUT_SOCKET );
		HttpClient client = new DefaultHttpClient( httpParameters );
		//END
		//original 
		//HttpClient client = new DefaultHttpClient();
		try
		{
			String encode_city = URLEncoder.encode( city_num , "UTF-8" );
			Log.v( "" , "encode_city is " + encode_city );
			HttpGet request = new HttpGet( String.format( COOEE_FORCAST_URL_INLAND , encode_city ) );
			System.out.println( "shlt , uri : " + String.format( COOEE_FORCAST_URL_INLAND , encode_city ) );
			HttpResponse response = client.execute( request );
			int status = response.getStatusLine().getStatusCode();
			Log.d( TAG , "Request returned status " + status );
			if( status == 200 )
			{
				HttpEntity entity = response.getEntity();
				Header header = response.getFirstHeader( "Date" );
				Date date = null;
				long timeUTC = 0;
				Log.e( "weijie" , header.getName() );
				Log.e( "weijie" , header.getValue() );
				if( header == null )
				{
					date = new Date( System.currentTimeMillis() );
					timeUTC = date.getTime();
				}
				else
				{
					//					timeUTC = getUTCTimeMillis(header.getValue());
					//					Log.e("weijie", "timeUTC="+timeUTC);
					//					if(timeUTC == 0)
					date = new Date( header.getValue() );
					timeUTC = date.getTime();
					Log.e( "weijie" , "date = " + date + "; timeUTC = " + timeUTC );
				}
				responseReader = new InputStreamReader( entity.getContent() , "GB2312" );
				dataentity = CooeeWeatherParseWeatherData( responseReader , timeUTC );
				dataentity.setLastUpdateTime( timeUTC );
				com.cooee.app.cooeeweather.dataprovider.weatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_SUCCES;
			}
			else
			{
				//shanjie deal with other
				weatherwebservice.Update_Result_Flag = FLAG_UPDATE.WEBSERVICE_ERROR;
			}
			client.getConnectionManager().shutdown();
		}
		catch( Exception e )
		{
			Log.e( TAG , "HttpResponse: request error!" );
			e.printStackTrace();
			com.cooee.app.cooeeweather.dataprovider.weatherwebservice.Update_Result_Flag = FLAG_UPDATE.WEBSERVICE_ERROR;
		}
		return dataentity;
	}
	
	private static long getUTCTimeMillis(
			final int year ,
			final int month ,
			final int day ,
			final int hour ,
			final int minute ,
			final int second ,
			final boolean isGMT )
	{
		DateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		Date date;
		boolean CF_month = false;
		boolean CF_year = false;
		int real_year = year;
		int real_month = month;
		int real_day = day;
		int real_hour = hour;
		int real_minute = minute;
		int real_second = second;
		if( real_hour > 23 && isGMT )
		{
			real_hour = hour % 24;
			switch( month )
			{
			//1月 3月 5月7月8月10月12月
				case 0:
				case 2:
				case 4:
				case 6:
				case 7:
				case 9:
				case 11:
					if( real_day + 1 > 31 )
					{
						real_day = 1;
						CF_month = true;
					}
					else
						real_day += 1;
					break;
				//4月6月9月11月
				case 3:
				case 5:
				case 8:
				case 10:
					if( real_day + 1 > 30 )
					{
						real_day = 1;
						CF_month = true;
					}
					else
						real_day += 1;
					break;
				//2月
				case 1:
					if( year % 4 == 0 )
					{
						if( real_day + 1 > 29 )
						{
							real_day = 1;
							CF_month = true;
						}
						else
							real_day += 1;
					}
					else
					{
						if( real_day + 1 > 28 )
						{
							real_day = 1;
							CF_month = true;
						}
						else
							real_day += 1;
					}
			}
			if( CF_month )
			{
				if( month + 1 > 11 )
				{
					real_month = 0;
					CF_year = true;
				}
				else
				{
					real_month += 1;
				}
			}
			if( CF_year )
			{
				real_year += 1;
			}
		}
		try
		{
			date = format.parse( String.format( "%d-%d-%d %d:%d:%d" , real_year , real_month , real_day , real_hour , real_minute , real_second ) );
		}
		catch( ParseException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return date.getTime();
	}
	
	private static int getmonth(
			String month )
	{
		Log.e( "weijie" , "getmonth month=" + month );
		if( "Jan".equals( month ) )
			return 0;
		else if( "Feb".equals( month ) )
			return 1;
		else if( "Mar".equals( month ) )
			return 2;
		else if( "Apr".equals( month ) )
			return 3;
		else if( "May".equals( month ) )
			return 4;
		else if( "June".equals( month ) )
			return 5;
		else if( "July".equals( month ) )
			return 6;
		else if( "Aug".equals( month ) )
			return 7;
		else if( "Sept".equals( month ) )
			return 8;
		else if( "Oct".equals( month ) )
			return 9;
		else if( "Nov".equals( month ) )
			return 10;
		else if( "Dec".equals( month ) )
			return 11;
		return -1;
	}
	
	private static long getUTCTimeMillis(
			final String time )
	{
		Log.e( "weijie" , "getUTCTimeMillis time=" + time );
		if( time == null )
			return 0;
		String[] splitStr = time.split( " " );
		Log.e( "weijie" , "getUTCTimeMillis splitStrlength=" + splitStr.length );
		if( splitStr == null || splitStr.length != 6 )
			return 0;
		Log.e( "weijie" , "getUTCTimeMillis splitStr[0]=" + splitStr[0] );
		Log.e( "weijie" , "getUTCTimeMillis splitStr[1]=" + splitStr[1] );
		Log.e( "weijie" , "getUTCTimeMillis splitStr[2]=" + splitStr[2] );
		Log.e( "weijie" , "getUTCTimeMillis splitStr[3]=" + splitStr[3] );
		Log.e( "weijie" , "getUTCTimeMillis splitStr[4]=" + splitStr[4] );
		Log.e( "weijie" , "getUTCTimeMillis splitStr[5]=" + splitStr[5] );
		try
		{
			boolean isGMT = splitStr[splitStr.length - 1].equalsIgnoreCase( "GMT" );
			int year = Integer.parseInt( splitStr[3] );
			int day = Integer.parseInt( splitStr[1] );
			int monthtemp = getmonth( splitStr[2] );
			if( monthtemp < 0 )
				return 0;
			int month = monthtemp;
			String[] splitSubStr = splitStr[4].split( ":" );
			int hour = Integer.parseInt( splitSubStr[0] );
			if( isGMT )
				hour += 8;
			int minute = Integer.parseInt( splitSubStr[1] );
			int second = Integer.parseInt( splitSubStr[2] );
			return getUTCTimeMillis( year , month , day , hour , minute , second , isGMT );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * prase data and insert data to object(weatherdataentity)
	 */
	public static weatherdataentity CooeeWeatherParseWeatherData(
			Reader responseReader ,
			long time )
	{
		weatherdataentity dataEntity = new weatherdataentity();
		weatherforecastentity forecastentity = null;
		char[] buffer = new char[1024];
		//String buffer = null;
		try
		{
			//buffer = "<weather><itcount>5</itcount><ver>2015-03-09 14:57:43</ver><it><dt>03.09</dt><xq>星期一</xq><nl>正月十九日</nl><tq>阴,多云</tq><wd>2,11</wd><fx>北风,北风</fx><fl>3-4级,3-4级</fl><sd>59</sd><rr>17:39,</rr><zw>最弱</zw></it><it><dt>03.10</dt><xq>星期二</xq><nl></nl><tq>多云,多云</tq><wd>2,7</wd><fx>北风,北风</fx><fl>微风,微风</fl><sd></sd><rr></rr><zw></zw></it><it><dt>03.11</dt><xq>星期三</xq><nl></nl><tq>多云,多云</tq><wd>5,10</wd><fx>东北风,东北风</fx><fl>微风,微风</fl><sd></sd><rr></rr><zw></zw></it><it><dt>03.12</dt><xq>星期四</xq><nl></nl><tq>阴,多云</tq><wd>8,12</wd><fx>东南风,东南风</fx><fl>微风,微风</fl><sd></sd><rr></rr><zw></zw></it></weather>";
			responseReader.read( buffer );//json 数据解析
			//Log.i( "test" , "buffer = "+buffer.toCharArray() );
			if( buffer.length > 100 )
			{
				String buf = new String( buffer );
				String s[] = buf.split( "</it>" );
				queryCurWeatherData( s[0] , dataEntity , time );
				for( int i = 0 ; i < s.length - 1 ; i++ )
				{
					forecastentity = cooeeForcastDataQuery( s[i] );
					dataEntity.getDetails().add( forecastentity );
				}
			}
			else
			{
				com.cooee.app.cooeeweather.dataprovider.weatherwebservice.Update_Result_Flag = FLAG_UPDATE.INVILIDE_VALUE;
			}
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			com.cooee.app.cooeeweather.dataprovider.weatherwebservice.Update_Result_Flag = FLAG_UPDATE.DATAPROVIDER_ERROR;
		}
		return dataEntity;
	}
	
	/**
	 * prase data and insert data to object(weatherdataentity)
	 */
	public static weatherdataentity CooeeWeatherParseWeatherData(
			Reader responseReader )
	{
		weatherdataentity dataEntity = new weatherdataentity();
		weatherforecastentity forecastentity = null;
		char[] buffer = new char[1024];
		try
		{
			responseReader.read( buffer );//json 数据解析
			if( buffer.length > 100 )
			{
				String buf = new String( buffer );
				String s[] = buf.split( "</it>" );
				queryCurWeatherData( s[0] , dataEntity );
				for( int i = 0 ; i < 4 ; i++ )
				{
					forecastentity = cooeeForcastDataQuery( s[i] );
					dataEntity.getDetails().add( forecastentity );
				}
			}
			else
			{
				com.cooee.app.cooeeweather.dataprovider.weatherwebservice.Update_Result_Flag = FLAG_UPDATE.INVILIDE_VALUE;
			}
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			com.cooee.app.cooeeweather.dataprovider.weatherwebservice.Update_Result_Flag = FLAG_UPDATE.DATAPROVIDER_ERROR;
		}
		return dataEntity;
	}
	
	public static void queryCurWeatherData(
			String s ,
			weatherdataentity dataEntity ,
			long time )
	{
		dataEntity.setCity( Update_city );
		dataEntity.setPostalCode( Update_city );
		String c1[] = s.split( "<wd>" );
		String c2[] = c1[1].split( "</wd>" );
		CooeeCurFormatTemprature( dataEntity , c2[0] );
		String t1[] = s.split( "<tq>" );
		String t2[] = t1[1].split( "</tq>" );
		getCondition( t2[0] , dataEntity );
		getWindData( s , dataEntity );
		getHumidity( s , dataEntity );
		getLunarCalendar( s , dataEntity );
		dataEntity.setTempC( CalTempC( time , dataEntity.getTempH() , dataEntity.getTempL() ) );
	}
	
	public static void queryCurWeatherData(
			String s ,
			weatherdataentity dataEntity )
	{
		dataEntity.setCity( Update_city );
		dataEntity.setPostalCode( Update_city );
		String c1[] = s.split( "<wd>" );
		String c2[] = c1[1].split( "</wd>" );
		CooeeCurFormatTemprature( dataEntity , c2[0] );
		String t1[] = s.split( "<tq>" );
		String t2[] = t1[1].split( "</tq>" );
		getCondition( t2[0] , dataEntity );
		getWindData( s , dataEntity );
		getHumidity( s , dataEntity );
		getLunarCalendar( s , dataEntity );
		getForecastDateAndLastUpdateTime( s , dataEntity );
		getUltravioletRay( s , dataEntity );
		getWeatherTime( s , dataEntity );
		dataEntity.setTempC( CalTempC( dataEntity.getTempH() , dataEntity.getTempL() ) );
	}
	
	private static void getWeatherTime(
			String s ,
			weatherdataentity dataEntity )
	{
		// TODO Auto-generated method stub
		String time1[] = s.split( "<rr>" );
		String time2[] = time1[1].split( "</rr>" );
		dataEntity.setWeathertime( time2[0] );
	}
	
	private static void getUltravioletRay(
			String s ,
			weatherdataentity dataEntity )
	{
		// TODO Auto-generated method stub
		String ray1[] = s.split( "<zw>" );
		String ray2[] = ray1[1].split( "</zw>" );
		dataEntity.setUltravioletray( ray2[0] );
	}
	
	private static void getForecastDateAndLastUpdateTime(
			String s ,
			weatherdataentity dataEntity )
	{
		// TODO Auto-generated method stub
		String date1[] = s.split( "<rr>" );
		String date2[] = date1[1].split( "</rr>" );
		String allDate[] = date2[0].split( "," );
		android.util.Log.v( "" , "allDate[] 0 is " + allDate[0] + " 1 is " + allDate[1] );
		//		dataEntity.setForecastDate(allDate[0]);
		//		dataEntity.setLastUpdateTime(lastUpdateTime)
	}
	
	private static void getLunarCalendar(
			String s ,
			weatherdataentity dataEntity )
	{
		// TODO Auto-generated method stub
		String lunar1[] = s.split( "<nl>" );
		String lunar2[] = lunar1[1].split( "</nl>" );
		dataEntity.setLunarcalendar( lunar2[0] );
	}
	
	private static void getHumidity(
			String s ,
			weatherdataentity dataEntity )
	{
		// TODO Auto-generated method stub
		String humidity1[] = s.split( "<sd>" );
		String humidity2[] = humidity1[1].split( "</sd>" );
		dataEntity.setHumidity( humidity2[0] );
	}
	
	private static void getWindData(
			String s ,
			weatherdataentity dataEntity )
	{
		// TODO Auto-generated method stub
		String wind_type1[] = s.split( "<fx>" );
		String wind_type2[] = wind_type1[1].split( "</fx>" );
		String wind_type[] = wind_type2[0].split( "," );
		String wind_power1[] = s.split( "<fl>" );
		String wind_power2[] = wind_power1[1].split( "</fl>" );
		String wind_power[] = wind_power2[0].split( "," );
		String winddatas = "";
		if( wind_type.length == wind_power.length )
		{
			for( int i = 0 ; i < wind_type.length ; i++ )
			{
				if( i == ( wind_type.length - 1 ) )
				{
					winddatas += wind_type[i] + "," + wind_power[i];
				}
				else
				{
					winddatas += wind_type[i] + "," + wind_power[i] + ";";
				}
			}
		}
		//		android.util.Log.v("", "winddatas is " + winddatas);
		dataEntity.setWindCondition( winddatas );
	}
	
	/**
	 * format the temprature of allday to low and high
	 */
	public static void CooeeCurFormatTemprature(
			weatherdataentity dataEntity ,
			String temp )
	{
		String[] s = temp.split( "\\," );
		String high = s[1];
		String low = s[0];
		dataEntity.setTempL( Integer.parseInt( low ) );
		dataEntity.setTempH( Integer.parseInt( high ) );
	}
	
	public static void getCondition(
			String s ,
			weatherdataentity dataEntity )
	{
		String t[] = s.split( "\\," );
		if( t[0].equals( s ) )
		{
			dataEntity.setCondition( t[0] );
		}
		else
		{
			Date dates = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime( dates );
			int hour = cal.get( Calendar.HOUR_OF_DAY );
			if( hour >= 0 && hour < 18 )
			{
				dataEntity.setCondition( t[0] );
			}
			else
			{
				dataEntity.setCondition( t[1] );
			}
		}
	}
	
	public static weatherforecastentity cooeeForcastDataQuery(
			String s )
	{
		weatherforecastentity forecastentity = new weatherforecastentity();
		String t1[] = s.split( "<tq>" );
		String t2[] = t1[1].split( "</tq>" );
		String t3[] = t2[0].split( "\\," );
		if( t3[0].equals( t3[1] ) )
		{
			forecastentity.setCondition( t3[0] );
		}
		else
		{
			String res = t2[0].replaceAll( "," , "转" );
			forecastentity.setCondition( res );
		}
		String c1[] = s.split( "<wd>" );
		String c2[] = c1[1].split( "</wd>" );
		CooeeFormatTemprature( forecastentity , c2[0] );
		String w1[] = s.split( "<xq>" );
		String w2[] = w1[1].split( "</xq>" );
		int week = formatDayOfWeek( w2[0] );
		forecastentity.setDayOfWeek( week );
		return forecastentity;
	}
	
	/**
	 * format the temprature of allday to low and high
	 */
	public static void CooeeFormatTemprature(
			weatherforecastentity dataEntity ,
			String temp )
	{
		String[] s = temp.split( "\\," );
		String high = s[1];
		String low = s[0];
		dataEntity.setLow( Integer.parseInt( low ) );
		dataEntity.setHight( Integer.parseInt( high ) );
	}
	
	private static Integer CalTempC(
			final long ld ,
			Integer tempH ,
			Integer tempL )
	{
		Integer tempC = 0;
		Calendar cal = Calendar.getInstance();
		Date date = new Date( ld );
		cal.setTime( date );
		int hour = cal.get( Calendar.HOUR_OF_DAY );
		float f;
		if( hour >= 0 && hour < 6 )
		{
			tempC = tempL;
		}
		else if( hour >= 6 && hour <= 14 )
		{
			f = tempH - tempL;
			f = f / ( 14 - 5 ) * ( hour - 5 );
			tempC = tempL + (int)f;
		}
		else if( hour >= 15 && hour <= 20 )
		{
			f = tempH - tempL;
			f = f / ( 20 - 14 ) * ( hour - 14 );
			tempC = tempH - (int)f;
		}
		else if( hour > 20 && hour < 24 )
		{
			tempC = tempL;
		}
		return tempC;
	}
	
	private static Integer CalTempC(
			Integer tempH ,
			Integer tempL )
	{
		Integer tempC = 0;
		Calendar cal = Calendar.getInstance();
		Date date = null;
		// 获取小时
		try
		{
			URL url = new URL( "http://www.bjtime.cn" );// 取得资源对象
			URLConnection uc = url.openConnection();// 生成连接对象
			uc.connect(); // 发出连接
			long ld = uc.getDate(); // 取得网站日期时间
			date = new Date( ld );
		}
		catch( Exception e )
		{
			date = new Date( System.currentTimeMillis() );
		}
		cal.setTime( date );
		int hour = cal.get( Calendar.HOUR_OF_DAY );
		float f;
		if( hour >= 0 && hour < 6 )
		{
			tempC = tempL;
		}
		else if( hour >= 6 && hour <= 14 )
		{
			f = tempH - tempL;
			f = f / ( 14 - 5 ) * ( hour - 5 );
			tempC = tempL + (int)f;
		}
		else if( hour >= 15 && hour <= 20 )
		{
			f = tempH - tempL;
			f = f / ( 20 - 14 ) * ( hour - 14 );
			tempC = tempH - (int)f;
		}
		else if( hour > 20 && hour < 24 )
		{
			tempC = tempL;
		}
		return tempC;
	}
	
	public static int formatDayOfWeek(
			String week )
	{
		int dayofweek = 0;
		if( week.equals( "星期日" ) )
		{
			dayofweek = 0;
		}
		else if( week.equals( "星期一" ) )
		{
			dayofweek = 1;
		}
		else if( week.equals( "星期二" ) )
		{
			dayofweek = 2;
		}
		else if( week.equals( "星期三" ) )
		{
			dayofweek = 3;
		}
		else if( week.equals( "星期四" ) )
		{
			dayofweek = 4;
		}
		else if( week.equals( "星期五" ) )
		{
			dayofweek = 5;
		}
		else if( week.equals( "星期六" ) )
		{
			dayofweek = 6;
		}
		return dayofweek;
	}
}
