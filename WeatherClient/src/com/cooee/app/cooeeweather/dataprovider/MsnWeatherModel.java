package com.cooee.app.cooeeweather.dataprovider;


import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.dataprovider.weatherwebservice.FLAG_UPDATE;


// import com.example.msnweatherdemo.weatherdataentity;
public class MsnWeatherModel
{
	
	private static final String TAG = "YahooWeatherModel";
	private final static String MSNWEATHER_SREACH_URL = "http://weather.service.msn.com/data.aspx?weadegreetype=%s&weasearchstr=%s";
	private final static String MSNWEATHER_CITY_URL = "http://weather.msn.com/data.aspx?weadegreetype=%s&wealocations=%s";
	private final int MSNWEATHER_ERROR = -1;
	private final int MSNWEATHER_SUCCESS = 0;
	public List<MsnXMLData> mMsnXMLData_list = new ArrayList<MsnXMLData>();
	private static final int TIMEOUT_CONNECT = 10 * 1000;// 设置请求超时10秒钟
	private static final int TIMEOUT_SOCKET = 30 * 1000; // 设置等待数据超时时间10秒钟
	private String mTempratrue_mode = "C";
	public static final String[] projection = new String[]{ "timepostmark" , "entityid" , "long" , "lat" , "weatherlocationname" , "weatherlocationcode" };
	public static final String[] sreach_projection = new String[]{
			"entityid" ,
			"long" ,
			"lat" ,
			"weatherlocationname" ,
			"weatherlocationcode" ,
			"degreetype" ,
			"timepostmark" ,
			"winddisplay" ,
			"humidity" ,
			"observationtime" ,
			"skytext" ,
			"temperature" ,
			"skytextday0" ,
			"high0" ,
			"low0" ,
			"skytextday1" ,
			"high1" ,
			"low1" ,
			"skytextday2" ,
			"high2" ,
			"low2" ,
			"skytextday3" ,
			"high3" ,
			"low3" ,
			"skytextday4" ,
			"high4" ,
			"low4" ,
			"shortday" ,
			"date" };
	
	// private HttpClient client = null;
	/*
	 * MsnWeatherModel constructed FUNC.
	 */
	public MsnWeatherModel()
	{
	}
	
	/*
	 * MsnWeatherModel constructed FUNC.
	 */
	public MsnWeatherModel(
			String temp )
	{
		if( null != temp )
			mTempratrue_mode = temp;
	}
	
	/*
	 * MsnWeatherSreachByKey this function is getting cities by key. param
	 * String key, [IN] return int , if return YAHOOWEATHER_SUCCESS means
	 * success
	 */
	public int MsnWeatherSreachDataByKey(
			String key )
	{
		if( null == key )
			return MSNWEATHER_ERROR;
		if( key.equals( "" ) )
			return MSNWEATHER_ERROR;
		BasicHttpParams httpParameters = new BasicHttpParams();// Set the
																// timeout in
																// milliseconds
																// until a
																// connection is
																// established.
		HttpConnectionParams.setConnectionTimeout( httpParameters , TIMEOUT_CONNECT );// Set
																						// the
																						// default
																						// socket
																						// timeout
																						// (SO_TIMEOUT)
																						// //
																						// in
																						// milliseconds
																						// which
																						// is
																						// the
																						// timeout
																						// for
																						// waiting
																						// for
																						// data.
		HttpConnectionParams.setSoTimeout( httpParameters , TIMEOUT_SOCKET );
		HttpClient client = new DefaultHttpClient( httpParameters );
		try
		{
			String uri = String.format( MSNWEATHER_SREACH_URL , mTempratrue_mode , key );
			uri = StringTransferred( uri );
			System.out.println( "shlt  , uri : " + uri );
			HttpGet request = new HttpGet( uri );
			HttpResponse response = client.execute( request );
			int status = response.getStatusLine().getStatusCode();
			Log.d( TAG , "Request returned status " + status );
			if( status == 200 )
			{
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				MsnWeatherParseSreachData( is );
				weatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_SREACH_SUCCES;
			}
			else
			{
				weatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_SREACH_FAILED;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			weatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_SREACH_FAILED;
			return MSNWEATHER_ERROR;
		}
		return MSNWEATHER_SUCCESS;
	}
	
	/*
	 * MsnWeatherGetWoid get woids by city name from list param String
	 * city_name, [IN] city name return int , if return YAHOOWEATHER_SUCCESS
	 * means success
	 */
	public String MsnWeatherGetWoidFromList(
			String key )
	{
		if( key == null )
		{
			return "";
		}
		return "";
	}
	
	/*
	 * MsnWeatherByWoid get real weather informations by city's woid param: int
	 * woid,[IN] city's woid return: weatherdataentity data, if return null
	 * means fail.
	 */
	public int MsnWeatherGetDataByWoid(
			String woid )
	{
		if( woid == null )
		{
			return MSNWEATHER_ERROR;
		}
		if( woid.equals( "" ) )
		{
			return MSNWEATHER_ERROR;
		}
		BasicHttpParams httpParameters = new BasicHttpParams();// Set the
																// timeout in
																// milliseconds
																// until a
																// connection is
																// established.
		HttpConnectionParams.setConnectionTimeout( httpParameters , TIMEOUT_CONNECT );// Set
																						// the
																						// default
																						// socket
																						// timeout
																						// (SO_TIMEOUT)
																						// //
																						// in
																						// milliseconds
																						// which
																						// is
																						// the
																						// timeout
																						// for
																						// waiting
																						// for
																						// data.
		HttpConnectionParams.setSoTimeout( httpParameters , TIMEOUT_SOCKET );
		HttpClient client = new DefaultHttpClient( httpParameters );
		// String uri =
		// String.format(MSNWEATHER_CITY_URL,mTempratrue_mode,mTempratrue_mode,woid);
		// uri = StringTransferred(uri);
		// HttpGet request = new HttpGet(uri);
		try
		{
			String uri = String.format( MSNWEATHER_CITY_URL , mTempratrue_mode , woid );
			uri = StringTransferred( uri );
			System.out.println( "shlt  , uri : " + uri );
			HttpGet request = new HttpGet( uri );
			HttpResponse response = client.execute( request );
			int status = response.getStatusLine().getStatusCode();
			Log.d( TAG , "Request returned status " + status );
			if( status == 200 )
			{
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				MsnWeatherParseWeatherData( is );
				weatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_REF_SUCCES;
			}
			else
			{
				weatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_REF_FAILED;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			weatherwebservice.Update_Result_Flag = FLAG_UPDATE.UPDATE_REF_FAILED;
		}
		return MSNWEATHER_SUCCESS;
	}
	
	/*
	 * MsnWeatherParseSreachData getting to be Sreached key,in fact,this is a
	 * XML Parse FUNC. param: InputStream stream,[IN] this data stream from web
	 * return: weatherdataentity data, if return null means fail.
	 */
	public weatherdataentity MsnWeatherParseSreachData(
			InputStream stream )
	{
		weatherdataentity ret = null;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder;
		try
		{
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			Document document = builder.parse( stream );
			// 找到根Element
			Element root = document.getDocumentElement();
			NodeList nodes = root.getElementsByTagName( MsnXMLDataField.WEATHER );
			for( int i = 0 ; i < nodes.getLength() ; i++ )
			{
				MsnXMLData mMsnXMLData = new MsnXMLData();
				;
				Element mElement = (Element)( nodes.item( i ) );
				mMsnXMLData.setEntityID( mElement.getAttribute( MsnXMLDataField.ENTITY_ID ) );
				mMsnXMLData.setLong( mElement.getAttribute( MsnXMLDataField.LONG ) );
				mMsnXMLData.setLat( mElement.getAttribute( MsnXMLDataField.LAT ) );
				mMsnXMLData.setLocationName( mElement.getAttribute( MsnXMLDataField.LOCATION_NAME ) );
				mMsnXMLData.setLocationCode( mElement.getAttribute( MsnXMLDataField.LOCATION_CODE ) );
				mMsnXMLData.setDegreetype( mElement.getAttribute( MsnXMLDataField.DEGREE_TYPE ) );
				mMsnXMLData.setTimeZone( mElement.getAttribute( MsnXMLDataField.TIMEZONE ) );
				mMsnXMLData.setZipcode( mElement.getAttribute( MsnXMLDataField.ZIPCODE ) );
				mMsnXMLData.setAlert( mElement.getAttribute( MsnXMLDataField.ALERT ) );
				MsnCurrentData CurrentData = new MsnCurrentData();
				Element curweather_node = (Element)mElement.getElementsByTagName( MsnXMLDataField.CURRENT ).item( 0 );
				CurrentData.setCurrentWindSpeed( curweather_node.getAttribute( MsnXMLDataField.WINDSPEED ) );
				CurrentData.setCurrentWindDisplay( curweather_node.getAttribute( MsnXMLDataField.WINDDISPLAY ) );
				CurrentData.setCurrentShortDay( curweather_node.getAttribute( MsnXMLDataField.SHORTDAY ) );
				CurrentData.setCurrentDay( curweather_node.getAttribute( MsnXMLDataField.DAY ) );
				CurrentData.setCurrentHumidity( curweather_node.getAttribute( MsnXMLDataField.HUNIDITY ) );
				CurrentData.setCurrentFeelSlike( curweather_node.getAttribute( MsnXMLDataField.FEELSLIKE ) );
				CurrentData.setCurrentObservationPoint( curweather_node.getAttribute( MsnXMLDataField.OBSERVATION_POINT ) );
				CurrentData.setCurrentObservationTime( curweather_node.getAttribute( MsnXMLDataField.OBSERVATION_TIME ) );
				CurrentData.setCurrentDate( curweather_node.getAttribute( MsnXMLDataField.DATE ) );
				CurrentData.setCurrentSkyText( getConditionOffsetString( curweather_node.getAttribute( MsnXMLDataField.SKY_TEXT ) ) );
				CurrentData.setCurrentTemperature( curweather_node.getAttribute( MsnXMLDataField.TEMPERATURE_CURRENT ) );
				mMsnXMLData.MsnCurrentData_list.add( CurrentData );
				NodeList forwcast_nodes = mElement.getElementsByTagName( MsnXMLDataField.FORECAST );
				Log.v( "aaaa" , "forwcast_nodes.getLength() = " + forwcast_nodes.getLength() );
				for( int j = 0 ; j < forwcast_nodes.getLength() ; j++ )
				{
					MsnForwcastsData ForwcastsData = new MsnForwcastsData();
					Element forwcast = (Element)( forwcast_nodes.item( j ) );
					ForwcastsData.setForwcastDate( forwcast.getAttribute( MsnXMLDataField.DATE ) );
					ForwcastsData.setForwcastDay( forwcast.getAttribute( MsnXMLDataField.DAY ) );
					ForwcastsData.setForwcastShortDay( forwcast.getAttribute( MsnXMLDataField.SHORTDAY ) );
					ForwcastsData.setForwcastPrecip( forwcast.getAttribute( MsnXMLDataField.PRECIP ) );
					ForwcastsData.setForwcastSkytextday( getConditionOffsetString( forwcast.getAttribute( MsnXMLDataField.SKY_TEXT_DAY ) ) );
					ForwcastsData.setForwcastTemperatureHigh( forwcast.getAttribute( MsnXMLDataField.TEMPERATURE_HIGH ) );
					ForwcastsData.setForwcastTemperatureLow( forwcast.getAttribute( MsnXMLDataField.TEMPERATURE_LOW ) );
					Log.v( "aaaa" , "forwcast_nodes[" + j + "].DATE = " + forwcast.getAttribute( MsnXMLDataField.DATE ) );
					Log.v( "aaaa" , "forwcast_nodes[" + j + "].DAY = " + forwcast.getAttribute( MsnXMLDataField.DAY ) );
					Log.v( "aaaa" , "forwcast_nodes[" + j + "].SHORTDAY = " + forwcast.getAttribute( MsnXMLDataField.SHORTDAY ) );
					Log.v( "aaaa" , "forwcast_nodes[" + j + "].TEMPERATURE_HIGH = " + forwcast.getAttribute( MsnXMLDataField.TEMPERATURE_HIGH ) );
					Log.v( "aaaa" , "forwcast_nodes[" + j + "].TEMPERATURE_LOW = " + forwcast.getAttribute( MsnXMLDataField.TEMPERATURE_LOW ) );
					mMsnXMLData.MsnForwcastsData_list.add( ForwcastsData );
				}
				mMsnXMLData_list.add( mMsnXMLData );
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			mMsnXMLData_list.clear();
		}
		return ret;
	}
	
	/*
	 * MsnWeatherParseWeatherData get weather data by city's woid,in fact,this
	 * is a XML Parse FUNC. param: BufferedReader responseReader,[IN] this data
	 * stream from web return: weatherdataentity data, if return null means
	 * fail.
	 */
	private weatherdataentity MsnWeatherParseWeatherData(
			InputStream stream )
	{
		weatherdataentity ret = null;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder;
		try
		{
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			Document document = builder.parse( stream );
			// 找到根Element
			Element root = document.getDocumentElement();
			NodeList nodes = root.getElementsByTagName( MsnXMLDataField.WEATHER );
			for( int i = 0 ; i < nodes.getLength() ; i++ )
			{
				MsnXMLData mMsnXMLData = new MsnXMLData();
				;
				Element mElement = (Element)( nodes.item( i ) );
				mMsnXMLData.setEntityID( mElement.getAttribute( MsnXMLDataField.ENTITY_ID ) );
				mMsnXMLData.setLong( mElement.getAttribute( MsnXMLDataField.LONG ) );
				mMsnXMLData.setLat( mElement.getAttribute( MsnXMLDataField.LAT ) );
				mMsnXMLData.setLocationName( mElement.getAttribute( MsnXMLDataField.LOCATION_NAME ) );
				mMsnXMLData.setLocationCode( mElement.getAttribute( MsnXMLDataField.LOCATION_CODE ) );
				mMsnXMLData.setDegreetype( mElement.getAttribute( MsnXMLDataField.DEGREE_TYPE ) );
				mMsnXMLData.setTimeZone( mElement.getAttribute( MsnXMLDataField.TIMEZONE ) );
				mMsnXMLData.setZipcode( mElement.getAttribute( MsnXMLDataField.ZIPCODE ) );
				mMsnXMLData.setAlert( mElement.getAttribute( MsnXMLDataField.ALERT ) );
				MsnCurrentData CurrentData = new MsnCurrentData();
				Element curweather_node = (Element)mElement.getElementsByTagName( MsnXMLDataField.CURRENT ).item( 0 );
				CurrentData.setCurrentWindSpeed( curweather_node.getAttribute( MsnXMLDataField.WINDSPEED ) );
				CurrentData.setCurrentWindDisplay( curweather_node.getAttribute( MsnXMLDataField.WINDDISPLAY ) );
				CurrentData.setCurrentShortDay( curweather_node.getAttribute( MsnXMLDataField.SHORTDAY ) );
				CurrentData.setCurrentDay( curweather_node.getAttribute( MsnXMLDataField.DAY ) );
				CurrentData.setCurrentHumidity( curweather_node.getAttribute( MsnXMLDataField.HUNIDITY ) );
				CurrentData.setCurrentFeelSlike( curweather_node.getAttribute( MsnXMLDataField.FEELSLIKE ) );
				CurrentData.setCurrentObservationPoint( curweather_node.getAttribute( MsnXMLDataField.OBSERVATION_POINT ) );
				CurrentData.setCurrentObservationTime( curweather_node.getAttribute( MsnXMLDataField.OBSERVATION_TIME ) );
				CurrentData.setCurrentDate( curweather_node.getAttribute( MsnXMLDataField.DATE ) );
				CurrentData.setCurrentSkyText( getConditionOffsetString( curweather_node.getAttribute( MsnXMLDataField.SKY_TEXT ) ) );
				CurrentData.setCurrentTemperature( curweather_node.getAttribute( MsnXMLDataField.TEMPERATURE_CURRENT ) );
				mMsnXMLData.MsnCurrentData_list.add( CurrentData );
				NodeList forwcast_nodes = mElement.getElementsByTagName( MsnXMLDataField.FORECAST );
				for( int j = 0 ; j < forwcast_nodes.getLength() ; j++ )
				{
					MsnForwcastsData ForwcastsData = new MsnForwcastsData();
					Element forwcast = (Element)( forwcast_nodes.item( j ) );
					ForwcastsData.setForwcastDate( forwcast.getAttribute( MsnXMLDataField.DATE ) );
					ForwcastsData.setForwcastDay( forwcast.getAttribute( MsnXMLDataField.DAY ) );
					ForwcastsData.setForwcastShortDay( forwcast.getAttribute( MsnXMLDataField.SHORTDAY ) );
					ForwcastsData.setForwcastPrecip( forwcast.getAttribute( MsnXMLDataField.PRECIP ) );
					ForwcastsData.setForwcastSkytextday( getConditionOffsetString( forwcast.getAttribute( MsnXMLDataField.SKY_TEXT_DAY ) ) );
					ForwcastsData.setForwcastTemperatureHigh( forwcast.getAttribute( MsnXMLDataField.TEMPERATURE_HIGH ) );
					ForwcastsData.setForwcastTemperatureLow( forwcast.getAttribute( MsnXMLDataField.TEMPERATURE_LOW ) );
					mMsnXMLData.MsnForwcastsData_list.add( ForwcastsData );
				}
				mMsnXMLData_list.add( mMsnXMLData );
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			mMsnXMLData_list.clear();
		}
		return ret;
	}
	
	/*
	 * public HashMap<String,String> MsnWeatherGetList() { return Msn_city_list;
	 * }
	 */
	public int MsnWeatherSetTempratrueMode(
			String temp )
	{
		if( null == temp )
			return MSNWEATHER_ERROR;
		this.mTempratrue_mode = temp;
		return MSNWEATHER_SUCCESS;
	}
	
	// //////////////////////////////////////////////////
	// xml dao
	public static class MsnXMLDataField
	{
		
		public static final String WEATHER_DATA = "weatherdata";
		public static final String WEATHER = "weather";
		public static final String ENTITY_ID = "entityid";
		public static final String LONG = "long";
		public static final String LAT = "lat";
		public static final String LOCATION_NAME = "weatherlocationname";
		public static final String LOCATION_CODE = "weatherlocationcode";
		public static final String DEGREE_TYPE = "degreetype";
		public static final String TIMEZONE = "timezone";
		public static final String ZIPCODE = "zipcode";
		public static final String ALERT = "alert";
		public static final String TIMEPOSTMARK = "timepostmark";
		public static final String CURRENT = "current";
		public static final String FORECAST = "forecast";
		public static final String WINDSPEED = "windspeed";
		public static final String WINDDISPLAY = "winddisplay";
		public static final String SHORTDAY = "shortday";
		public static final String DAY = "day";
		public static final String HUNIDITY = "humidity"; // 湿度
		public static final String FEELSLIKE = "feelslike";
		public static final String OBSERVATION_POINT = "observationpoint";
		public static final String OBSERVATION_TIME = "observationtime";
		public static final String DATE = "date";
		public static final String SKY_TEXT = "skytext";
		public static final String TEMPERATURE_CURRENT = "temperature";
		public static final String PRECIP = "precip";
		public static final String SKY_TEXT_DAY = "skytextday";
		public static final String TEMPERATURE_HIGH = "high";
		public static final String TEMPERATURE_LOW = "low";
		public static final String SREACHKEY = "sreachkey";
	}
	
	public class MsnCurrentData
	{
		
		private String mWindSpeed;
		private String mWindDisplay;
		private String mShortDay;
		private String mDay;
		private String mHumidity; // 湿度
		private String mFeelSlike;
		private String mObservationPoint;
		private String mObservationTime;
		private String mDate;
		private String mSkyText;
		private String mTemperature;
		
		// ShortDay
		public String getCurrentShortDay()
		{
			return mShortDay;
		}
		
		public void setCurrentShortDay(
				String ShortDay )
		{
			this.mShortDay = ShortDay;
		}
		
		// Day
		public String getCurrentDay()
		{
			return mDay;
		}
		
		public void setCurrentDay(
				String Day )
		{
			this.mDay = Day;
		}
		
		// Date
		public String getCurrentDate()
		{
			return mDate;
		}
		
		public void setCurrentDate(
				String Date )
		{
			this.mDate = Date;
		}
		
		// WindSpeed
		public String getCurrentWindSpeed()
		{
			return mWindSpeed;
		}
		
		public void setCurrentWindSpeed(
				String WindSpeed )
		{
			this.mWindSpeed = WindSpeed;
		}
		
		// WindDisplay
		public String getCurrentWindDisplay()
		{
			return mWindDisplay;
		}
		
		public void setCurrentWindDisplay(
				String WindDisplay )
		{
			this.mWindDisplay = WindDisplay;
		}
		
		// Humidity
		public String getCurrentHumidity()
		{
			return mHumidity;
		}
		
		public void setCurrentHumidity(
				String Humidity )
		{
			this.mHumidity = Humidity;
		}
		
		// FeelSlike
		public String getCurrentFeelSlike()
		{
			return mFeelSlike;
		}
		
		public void setCurrentFeelSlike(
				String FeelSlike )
		{
			this.mFeelSlike = FeelSlike;
		}
		
		// Humidity
		public String getCurrentObservationPoint()
		{
			return mObservationPoint;
		}
		
		public void setCurrentObservationPoint(
				String ObservationPoint )
		{
			this.mObservationPoint = ObservationPoint;
		}
		
		// FeelSlike
		public String getCurrentObservationTime()
		{
			return mObservationTime;
		}
		
		public void setCurrentObservationTime(
				String ObservationTime )
		{
			this.mObservationTime = ObservationTime;
		}
		
		// Humidity
		public String getCurrentSkyText()
		{
			return mSkyText;
		}
		
		public void setCurrentSkyText(
				String SkyText )
		{
			this.mSkyText = SkyText;
		}
		
		// FeelSlike
		public String getCurrentTemperature()
		{
			return mTemperature;
		}
		
		public void setCurrentTemperature(
				String Temperature )
		{
			this.mTemperature = Temperature;
		}
	}
	
	public class MsnForwcastsData
	{
		
		private String mShortDay;
		private String mDay;
		private String mDate;
		private String mPrecip;
		private String mSkytextday;
		private String mTemperatureHigh;
		private String mTemperatureLow;
		
		// ShortDay
		public String getForwcastShortDay()
		{
			return mShortDay;
		}
		
		public void setForwcastShortDay(
				String ShortDay )
		{
			this.mShortDay = ShortDay;
		}
		
		// Day
		public String getForwcastDay()
		{
			return mDay;
		}
		
		public void setForwcastDay(
				String Day )
		{
			this.mDay = Day;
		}
		
		// Date
		public String getForwcastDate()
		{
			return mDate;
		}
		
		public void setForwcastDate(
				String Date )
		{
			this.mDate = Date;
		}
		
		// Precip
		public String getForwcastPrecip()
		{
			return mPrecip;
		}
		
		public void setForwcastPrecip(
				String Precip )
		{
			this.mPrecip = Precip;
		}
		
		// Skytextday
		public String getForwcastSkytextday()
		{
			return mSkytextday;
		}
		
		public void setForwcastSkytextday(
				String Skytextday )
		{
			this.mSkytextday = Skytextday;
		}
		
		// TemperatureHigh
		public String getForwcastTemperatureHigh()
		{
			return mTemperatureHigh;
		}
		
		public void setForwcastTemperatureHigh(
				String TemperatureHigh )
		{
			this.mTemperatureHigh = TemperatureHigh;
		}
		
		// TemperatureLow
		public String getForwcastTemperatureLow()
		{
			return mTemperatureLow;
		}
		
		public void setForwcastTemperatureLow(
				String TemperatureLow )
		{
			this.mTemperatureLow = TemperatureLow;
		}
	}
	
	public class MsnXMLData implements Serializable
	{
		
		private static final long serialVersionUID = 1L;
		private String mLocationName;
		private String mEntityID;
		private String mTimeZone;
		private String mLong;
		private String mLat;
		private String mLocationCode;
		private String mDegreetype;
		private String mZipcode;
		private String mAlert;
		
		// mLocationName
		public String getLocationName()
		{
			return mLocationName;
		}
		
		public void setLocationName(
				String name )
		{
			this.mLocationName = name;
		}
		
		// mEntityID
		public String getEntityID()
		{
			return mEntityID;
		}
		
		public void setEntityID(
				String id )
		{
			this.mEntityID = id;
		}
		
		// mTimeZone
		public String getTimeZone()
		{
			return mTimeZone;
		}
		
		public void setTimeZone(
				String timezone )
		{
			this.mTimeZone = timezone;
		}
		
		// mLong
		public String getLong()
		{
			return mLong;
		}
		
		public void setLong(
				String Long )
		{
			this.mLong = Long;
		}
		
		// mLat
		public String getLat()
		{
			return mLat;
		}
		
		public void setLat(
				String lat )
		{
			this.mLat = lat;
		}
		
		// mLocationCode
		public String getLocationCode()
		{
			return mLocationCode;
		}
		
		public void setLocationCode(
				String LocationCode )
		{
			this.mLocationCode = LocationCode;
		}
		
		// mDegreetype
		public String getDegreetype()
		{
			return mDegreetype;
		}
		
		public void setDegreetype(
				String Degreetype )
		{
			this.mDegreetype = Degreetype;
		}
		
		// mLat
		public String getZipcode()
		{
			return mZipcode;
		}
		
		public void setZipcode(
				String Zipcode )
		{
			this.mZipcode = Zipcode;
		}
		
		// mAlert
		public String getAlert()
		{
			return mAlert;
		}
		
		public void setAlert(
				String Alert )
		{
			this.mAlert = Alert;
		}
		
		// /////////////////current ////////////////////////////////
		public List<MsnCurrentData> MsnCurrentData_list = new ArrayList<MsnCurrentData>();
		public List<MsnForwcastsData> MsnForwcastsData_list = new ArrayList<MsnForwcastsData>();
	}
	
	public static int fomatDayofweek(
			String TodayWeek )
	{
		// 0-6
		String[] DayOfWeek = { "Sun" , "Mon" , "Tue" , "Wed" , "Thu" , "Fri" , "Sat" };
		int i = 0;
		while( !TodayWeek.equals( DayOfWeek[i] ) && i < DayOfWeek.length )
		{
			i++;
		}
		return i;
	}
	
	private weatherdataentity MsnDataForamt2weatherdataentity(
			MsnXMLData Data )
	{
		weatherdataentity ret = null;
		if( null == Data )
			return ret;
		try
		{
			final MsnXMLData data = Data;
			MsnForwcastsData Forwcasts = null;
			MsnCurrentData CurrentData = Data.MsnCurrentData_list.get( 0 );
			ret = new weatherdataentity();
			ret.setCity( Data.getLocationName() );
			ret.setPostalCode( Data.getLocationName() );
			for( int i = 0 ; i < 4 ; i++ )
			{
				Forwcasts = data.MsnForwcastsData_list.get( i );
				weatherforecastentity tmp = new weatherforecastentity();
				tmp.setCondition( Forwcasts.getForwcastSkytextday() );
				tmp.setHight( Integer.parseInt( Forwcasts.getForwcastTemperatureHigh() ) );
				tmp.setLow( Integer.parseInt( Forwcasts.getForwcastTemperatureLow() ) );
				tmp.setDetailCity( Data.getLocationName() );
				tmp.setDayOfWeek( fomatDayofweek( Forwcasts.getForwcastShortDay() ) );
				ret.getDetails().add( tmp );
			}
			ret.setTempH( ret.getDetails().get( 0 ).getHight() );
			ret.setTempL( ret.getDetails().get( 0 ).getLow() );
			ret.setTempC( Integer.parseInt( CurrentData.getCurrentTemperature() ) );
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public String getConditionOffsetString(
			String str )
	{
		String[] str11 = str.split( " /" );
		String[] str12 = str11[0].split( " " );
		if( str12.length > 1 )
		{
			int b = -1;
			int a = str12[1].lastIndexOf( "(" );
			if( a >= 0 )
				b = str12[1].lastIndexOf( ")" );
			if( b > 0 )
			{
				// need get sub string
				return str12[1].substring( a + 1 , b );
			}
			else
				return str12[1];
		}
		else
			return str12[0];
	}
	
	private String StringTransferred(
			String str )
	{
		String ret = str;
		// ret = str.replaceAll("%", "%25");
		// str = str.replaceAll("?", "%3F");
		// ret = ret.replaceAll("&", "%26");
		// ret = ret.replaceAll("|", "%124");
		// ret = ret.replaceAll("=", "%3D");
		// ret = ret.replaceAll("#", "%23");
		// ret = ret.replaceAll("/", "%2F");
		// ret = ret.replaceAll("+", "%2B");
		ret = ret.replaceAll( " " , "%20" );
		return ret;
	}
}
