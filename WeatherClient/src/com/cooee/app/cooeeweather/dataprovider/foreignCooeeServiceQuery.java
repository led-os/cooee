package com.cooee.app.cooeeweather.dataprovider;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.cooee.StatisticsBase.Assets;
import com.cooee.app.cooeeweather.dataentity.ForeignCitysEntity;
import com.cooee.app.cooeeweather.dataentity.WeatherCondition;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.dataprovider.weatherwebservice.FLAG_UPDATE;
import com.cooee.app.cooeeweather.filehelp.Log;
import com.cooee.widget.samweatherclock.MainActivity;


public class foreignCooeeServiceQuery
{
	
	private static final String TAG = "com.cooee.weather.dataprovider.cooeeServiceQuery";
	// COOEE 预报URL
	//	public final static String COOEE_FORCAST_URL = "http://widget.coeeland.com/w2/tianqi2.ashx?city=%s&m=2603&l=320480&f=2424&s=B01_HVGA&imsi=460008623197253&sc=+8613800210500&iccid=898600810910f6287253";
	//
	//url字段信息
	// 两个url随机选取使用，避免一直使用同一个url
	// d01:城市ID 100010000, d02:cityName, d03:countryName, d04:imsi, d05:imei, d06:机型, d07:渠道ID,
	// d08:客户端包名, d09:所在时区, d10:手机语言, d11:国家代码, d12:硬件信息, d13:分辨率, d14:版本号, d15:本地时间yyyyMMddHHmmss
	private static String COOEE_FORECAST_URL1 = "http://tq01.sh928.com/app/do.ashx";
	private static String COOEE_FORECAST_URL2 = "http://tq01.nt928.com/app/do.ashx";
	private static String COOEE_FORECAST_URL = null;
	private final static String CITY_CONTENT_URI = "content://com.cooee.app.cooeeweather.dataprovider/citys";
	private static String Update_city = null;
	//weijie_20121210_01
	private static final int TIMEOUT_CONNECT = 10 * 1000;//设置请求超时10秒钟  
	private static final int TIMEOUT_SOCKET = 30 * 1000; //设置等待数据超时时间10秒钟 
	
	@SuppressWarnings( { "deprecation" , "unused" } )
	public static weatherdataentity CooeeWeatherDataUpdate(
			Context mContext ,
			String cityName )
	{
		weatherdataentity dataentity = null;
		int second = Calendar.getInstance().get( Calendar.SECOND );
		COOEE_FORECAST_URL = ( second % 2 == 0 ? COOEE_FORECAST_URL1 : COOEE_FORECAST_URL2 ) + "?d01=%s&d02=%s&d03=%s&d04=%s&d05=%s&d06=%s&d07=%s&d08=%s&d09=%s&d10=%s&d11=%s&d12=%s&d13=%s&d14=%s&d15=%s";
		// weather forcast data
		if( cityName == null )
		{
			Log.v( TAG , "No postalcode" );
			return null;
		}
		Update_city = cityName;
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
			String[] httpArgs = fillHttpArgs( mContext , cityName );
			HttpGet request = new HttpGet( String.format( COOEE_FORECAST_URL , (Object[])httpArgs ) );
			System.out.println( "shlt , uri : " + String.format( COOEE_FORECAST_URL , (Object[])httpArgs ) );
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
					date = new Date( header.getValue() );
					timeUTC = date.getTime();
				}
				responseReader = new InputStreamReader( entity.getContent() , "UTF8" );//老版本为GB2312 //yangtianyu
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
		try
		{
			responseReader.read( buffer );//json 数据解析
			if( buffer.length > 100 )
			{
				String buf = new String( buffer );
				String s[] = buf.split( "</it>" );
				queryCurWeatherData( s[0] , dataEntity , time );
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
		dataEntity.setTempC( CalTempC( time , dataEntity.getTempH() , dataEntity.getTempL() ) );
	}
	
	private static void getHumidity(
			String s ,
			weatherdataentity dataEntity )
	{
		String humidity1[] = s.split( "<sd>" );
		String humidity2[] = humidity1[1].split( "</sd>" );
		dataEntity.setHumidity( humidity2[0] );
	}
	
	private static void getWindData(
			String s ,
			weatherdataentity dataEntity )
	{
		String wind_type1[] = s.split( "<fx>" );
		String wind_type[] = wind_type1[1].split( "</fx>" );
		String wind_power1[] = s.split( "<fl>" );
		String wind_power[] = wind_power1[1].split( "</fl>" );
		String winddatas = "";
		if( wind_type.length == wind_power.length )
		{
			winddatas += wind_type[0] + "," + wind_power[0];
		}
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
		dataEntity.setTempL( (int)Double.parseDouble( low ) );
		dataEntity.setTempH( (int)Double.parseDouble( high ) );
	}
	
	public static void getCondition(
			String s ,
			weatherdataentity dataEntity )
	{
		String t[] = s.split( "\\," );
		if( t[0].equals( s ) )
		{
			dataEntity.setCondition( getFuzzyWeather( t[0] ) );
		}
		else
		{
			Date dates = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime( dates );
			int hour = cal.get( Calendar.HOUR_OF_DAY );
			if( hour >= 0 && hour < 18 )
			{
				dataEntity.setCondition( getFuzzyWeather( t[0] ) );
			}
			else
			{
				dataEntity.setCondition( getFuzzyWeather( t[1] ) );
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
		if( t3.length < 2 || t3[0].equals( t3[1] ) )
		{
			forecastentity.setCondition( getFuzzyWeather( t3[0] ) );
		}
		else
		{
			StringBuffer tmp = new StringBuffer();
			tmp.append( getFuzzyWeather( t3[0] ) );
			tmp.append( "转" );
			tmp.append( getFuzzyWeather( t3[1] ) );
			//			String res = t2[0].replaceAll( "," , "转" );
			String res = tmp.toString();
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
		dataEntity.setLow( (int)Double.parseDouble( low ) );
		dataEntity.setHight( (int)Double.parseDouble( high ) );
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
	
	public static int formatDayOfWeek(
			String week )
	{
		int dayofweek = 0;
		if( week.equalsIgnoreCase( "Sunday" ) )
		{
			dayofweek = 0;
		}
		else if( week.equalsIgnoreCase( "Monday" ) )
		{
			dayofweek = 1;
		}
		else if( week.equalsIgnoreCase( "Tuesday" ) )
		{
			dayofweek = 2;
		}
		else if( week.equalsIgnoreCase( "Wednesday" ) )
		{
			dayofweek = 3;
		}
		else if( week.equalsIgnoreCase( "Thursday" ) )
		{
			dayofweek = 4;
		}
		else if( week.equalsIgnoreCase( "Friday" ) )
		{
			dayofweek = 5;
		}
		else if( week.equalsIgnoreCase( "Saturday" ) )
		{
			dayofweek = 6;
		}
		return dayofweek;
	}
	
	/**
	 * @author yangtianyu
	 * 获取URL参数数组
	 * d01:城市ID 100010000, d02:cityName, d03:countryName, d04:imsi, d05:imei, d06:机型, d07:渠道ID,
	 * d08:客户端包名, d09:所在时区, d10:手机语言, d11:国家代码, d12:硬件信息, d13:分辨率, d14:版本号, d15:本地时间yyyyMMddHHmmss
	 * @param mContext
	 * @param cityName 城市的英文名
	 * @return 返回URL中需要填充的参数数组，无数据的参数则填入null
	 */
	private static String[] fillHttpArgs(
			Context mContext ,
			String cityName )
	{
		ContentResolver resolver = mContext.getContentResolver();
		android.database.Cursor cursor = null;
		String[] result = new String[15];
		String cityId = null;
		String countryName = null;
		Uri uriTmp = android.net.Uri.parse( CITY_CONTENT_URI );
		cursor = resolver.query( uriTmp , ForeignCitysEntity.projection_abroad_en , ForeignCitysEntity.CITY_EN + " = '" + cityName + "'" , null , null );
		if( cursor != null && cursor.getCount() != 0 )
		{
			cursor.moveToFirst();
			cityId = cursor.getString( ForeignCitysEntity.ID_INDEX );
			countryName = cursor.getString( ForeignCitysEntity.COUNTRY_EN_INDEX );
		}
		result[0] = cityId;
		result[1] = cityName;
		result[2] = countryName;
		//
		String imsi = MainActivity.imsi;
		String imei = MainActivity.imei;
		result[3] = imsi;
		result[4] = imei;
		String model = Build.MODEL;
		result[5] = model;
		String channelId = null;
		JSONObject config;
		try
		{
			config = Assets.config.getJSONObject( "config" );
			//			Log.i( TAG , "fillHttpArgs methord  config = " + config );
			//			Log.i( TAG , "fillHttpArgs methord  Assets.config = " + Assets.config.toString() );
			//			Log.i( TAG , "fillHttpArgs methord  Assets.config.getJSONObject = " + Assets.config.getJSONObject( "config" ) );
			if( config != null )
			{
				channelId = config.getString( "serialno" );
			}
		}
		catch( JSONException e1 )
		{
			e1.printStackTrace();
		}
		result[6] = channelId;
		String packageName = mContext.getPackageName();
		result[7] = packageName;
		String timeZone = null;
		timeZone = TimeZone.getDefault().getDisplayName( false , TimeZone.SHORT );
		result[8] = timeZone;
		String defLanguage = null;
		defLanguage = mContext.getResources().getConfiguration().locale.getLanguage();
		result[9] = defLanguage;
		String countryIso = MainActivity.countryIso;
		result[10] = countryIso;
		String hardInfo = null;
		hardInfo = Build.FINGERPRINT;
		result[11] = hardInfo;
		String resolution = null;
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)mContext.getSystemService( Context.WINDOW_SERVICE );
		windowManager.getDefaultDisplay().getMetrics( displayMetrics );
		resolution = displayMetrics.heightPixels + ":" + displayMetrics.widthPixels;
		result[12] = resolution;
		String version = null;
		PackageManager packageManager = mContext.getPackageManager();
		try
		{
			PackageInfo packInfo = packageManager.getPackageInfo( packageName , 0 );
			version = packInfo.versionName;
		}
		catch( NameNotFoundException e )
		{
			System.out.println( "get version error" );
		}
		result[13] = version;
		SimpleDateFormat currDateFormat = new SimpleDateFormat( "yyyyMMddHHmmss" );
		String curDate = currDateFormat.format( Calendar.getInstance().getTime() );
		result[14] = curDate;
		for( int i = 0 ; i < result.length ; i++ )
		{
			if( result[i] != null )
			{
				try
				{
					result[i] = URLEncoder.encode( result[i] , "UTF-8" );
				}
				catch( UnsupportedEncodingException e )
				{
					result[i] = null;
				}
			}
			if( "".equals( result[i] ) )
			{
				result[i] = null;
			}
		}
		return result;
	}
	
	// TODO 此方法为应急使用，以后还需要作出修改
	/**
	 * 根据天气数据中的关键字模糊匹配天气状态
	 * @param weatherCondition
	 * @return 匹配到的天气状态，未匹配到合适天气则返回原始数据
	 */
	private static String getFuzzyWeather(
			String weatherCondition )
	{
		String tmp = weatherCondition.toLowerCase();
		String result = weatherCondition;
		for( int i = 0 ; i < WeatherCondition.des.length ; i++ )
		{
			for( int j = 0 ; j < WeatherCondition.des[i].length ; j++ )
			{
				if( tmp.contains( WeatherCondition.des[i][j].toLowerCase() ) )
				{
					result = WeatherCondition.des[i][j];
					return result;
				}
			}
		}
		return result;
	}
}
