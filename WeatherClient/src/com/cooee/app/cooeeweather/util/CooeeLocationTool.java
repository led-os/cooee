package com.cooee.app.cooeeweather.util;


import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;


/**
 * 定位工具类,只需要new CooeeLocationTool(context).getLocation(); 即返回 国家 + "," + 城市，
 * 国内是中文， 海外为英文
 * 
 * @author xl_song
 *
 */
public class CooeeLocationTool implements TencentLocationListener
{
	private static final String TAG = "CooeeLocationTool";
	public Context mcontext;
	public String address;
	public TencentLocation mlocation;
	public TencentLocationManager locationManager;
	public TencentLocationRequest locationRequest;
	public LocationThread locationThread;// 异步动态更新地址线程
	public LocationThread2 locationThread2;// 异步海外反地理编码线程
	public Handler handler;
	public Looper looper;
	private static CooeeLocationTool cooeeLocation = null;
	
	private CooeeLocationTool(
			Context context )
	{
		mcontext = context;
	}
	
	public static CooeeLocationTool getInstance(
			Context context )
	{
		if( cooeeLocation == null )
		{
			cooeeLocation = new CooeeLocationTool( context );
		}
		return cooeeLocation;
	}
	
	/**
	 * 
	 * @return the address of the location or null if no network.
	 */
	public String getLocation()
	{
		// @2015/04/14 UPD START测试数据
		Log.i( TAG , "getLocation!!!" );
		mlocation = null;
		// @2015/04/14 UPD END
		String innerAddress = null;
		if( !isNetWorkEnable() )
		{
			//			Toast.makeText( mcontext , "no network connection detected" , Toast.LENGTH_SHORT ).show();
			return innerAddress;
		}
		locationThread = new LocationThread();
		locationThread.start();
		synchronized( locationThread )
		{
			try
			{
				locationThread.wait();// 主线程进入等待
				// 超时
				if( mlocation == null )
				{
					return innerAddress;
				}
				else
				{
					innerAddress = mlocation.getNation() + "," + mlocation.getCity();// 国内定位
				}
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
		if( mlocation != null && mlocation.getCity() == null )// 海外定位
		{
			//return getLocationTest( latitudeLongitude[position][1] , latitudeLongitude[position][0] );
			locationThread2 = new LocationThread2();
			locationThread2.start();
			synchronized( locationThread2 )
			{
				try
				{
					locationThread2.wait();// 主线程等待
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
			return address;
		}
		return innerAddress;
	}
	
	/**
	 * 检查网络状态
	 * 
	 * @return true if WiFi or GPRS is ok
	 */
	private boolean isNetWorkEnable()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)mcontext.getSystemService( Context.CONNECTIVITY_SERVICE );
		boolean phone = connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_MOBILE ).isConnected();
		boolean wifi = connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_WIFI ).isConnected();
		if( phone == true || wifi == true )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation()
	{
		locationManager = TencentLocationManager.getInstance( mcontext );
		locationRequest = TencentLocationRequest.create();
		locationRequest.setRequestLevel( 3 );
		locationRequest.setAllowCache( false );
		locationManager.requestLocationUpdates( locationRequest , this , looper );// 异步更新
		Log.i( "lbs" , "enter location" );
	}
	
	/**
	 * 结束定位
	 */
	private void stopLocation()
	{
		Log.i( "tianyu" , "stopLocation" );
		locationManager.removeUpdates( this );
	}
	
	/**
	 * 地址更新回调方法
	 */
	@Override
	public void onLocationChanged(
			TencentLocation arg0 ,
			int arg1 ,
			String arg2 )
	{
		Log.i( TAG , "enter locationChanged arg1 =" + arg1 );
		if( arg1 == TencentLocation.ERROR_OK )
		{
			synchronized( locationThread )
			{
				mlocation = arg0;
				locationManager.removeUpdates( this );
				locationThread.notify();// 唤醒主线程
				looper.quit();
			}
		}
		else if( arg1 == TencentLocation.ERROR_WGS84 )
		{
			Log.e( TAG , "error 坐标系转换错误" );
		}
		else
		{
			Log.i( TAG , "error" );
		}
	}
	
	@Override
	public void onStatusUpdate(
			String arg0 ,
			int arg1 ,
			String arg2 )
	{
	}
	
	class LocationThread extends Thread
	{
		
		@Override
		public void run()
		{
			Looper.prepare();
			looper = Looper.myLooper();
			handler = new Handler( looper );
			handler.postDelayed( new Timeout() , 10000 );
			startLocation();// 开始定位
			Looper.loop();
		}
	}
	
	/**
	 * 异步海外反地理编码线程
	 * 
	 * @author xl_song
	 *
	 */
	class LocationThread2 extends Thread
	{
		
		@Override
		public void run()
		{
			address = null;
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT , 10000 );
			httpClient.getParams().setParameter( CoreConnectionPNames.SO_TIMEOUT , 10000 );
			String urlString = "http://maps.google.com/maps/api/geocode/json?latlng=" + mlocation.getLatitude() + "," + mlocation.getLongitude() + "&language=en-us&sensor=false";
			HttpGet httpGet = new HttpGet( urlString );
			try
			{
				HttpResponse httpResponse = httpClient.execute( httpGet );
				if( httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
				{
					String result = EntityUtils.toString( httpResponse.getEntity() );
					JSONObject jsonObject = new JSONObject( result );
					if( jsonObject.optJSONArray( "results" ) != null )
					{
						synchronized( locationThread2 )
						{
							JSONArray jsonArray = jsonObject.optJSONArray( "results" ).optJSONObject( 0 ).optJSONArray( "address_components" );
							String country = null;
							String city = null;
							for( int i = 0 ; i < jsonArray.length() ; i++ )
							{
								for( int j = 0 ; j < jsonArray.optJSONObject( i ).optJSONArray( "types" ).length() ; j++ )
								{
									if( jsonArray.optJSONObject( i ).optJSONArray( "types" ).optString( j ).equals( "country" ) )
									{
										country = jsonArray.optJSONObject( i ).optString( "long_name" );
										Log.i( TAG , "country = " + country );
									}
									if( jsonArray.optJSONObject( i ).optJSONArray( "types" ).optString( j ).equals( "locality" ) )
									{
										city = jsonArray.optJSONObject( i ).optString( "long_name" );
										Log.i( TAG , "city = " + city );
									}
								}
							}
							address = country + "," + city;
							Log.i( TAG , "address = " + address );
							//locationThread2.notify();
						}
					}
				}
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
			catch( ClientProtocolException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				//Log.i( TAG , "finally " );
				synchronized( locationThread2 )
				{
					locationThread2.notify();
				}
			}
		}
	}
	
	/**
	 * 超时结束定位类
	 * 
	 * @author xl_song
	 *
	 */
	class Timeout implements Runnable
	{
		
		@Override
		public void run()
		{
			Toast.makeText( mcontext , "10 seconds out, stop location!" , Toast.LENGTH_LONG ).show();
			synchronized( locationThread )
			{
				looper.quit();
				stopLocation();
				locationThread.notify();
			}
		}
	}
	
	/*
	 * 
	 * 
	 * 
	 * 以下为测试代码部分
	 * 
	 * 
	 * 
	 * 
	 * */
	private String mlongitude;
	private String mlatitude;
	private LocationThreadTest locationThreadTest;
	private String[][] latitudeLongitude = {
			{ "13.7278956000" , "100.5241235000" } ,
			{ "13.7278956000" , "100.5241235000" } ,
			{ "19.017978954654208" , "72.88330015625003" } ,
			{ "-23.559582798453302" , "-46.660309462890595" } };
	/*{ "125.19 " , "43.54" } , { "125.01" , "46.36" } , { "104.04" , "30.40" } , { "116.24" , "39.55 " }
	};*/
	private static int position = 0;
	
	/**
	 * 根据输入的经纬度获取位置
	 * @param longitude
	 * @param latitude
	 * @return
	 */
	public String getLocationTest(
			String longitude ,
			String latitude )
	{
		mlongitude = longitude;
		mlatitude = latitude;
		locationThread2 = new LocationThread2();
		locationThread2.start();
		position = ++position % 4;
		synchronized( locationThread2 )
		{
			try
			{
				locationThread2.wait();
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
		return address;
	}
	
	class LocationThreadTest extends Thread
	{
		
		@Override
		public void run()
		{
			address = null;
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT , 10000 );
			httpClient.getParams().setParameter( CoreConnectionPNames.SO_TIMEOUT , 10000 );
			String urlString = "http://maps.google.com/maps/api/geocode/json?latlng=" + mlatitude + "," + mlongitude + "&language=en-us&sensor=false";
			//Log.i( "andy" , "urlString = " + urlString );
			HttpGet httpGet = new HttpGet( urlString );
			try
			{
				HttpResponse httpResponse = httpClient.execute( httpGet );
				if( httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
				{
					//Log.i( "andy" , "address = " );
					String result = EntityUtils.toString( httpResponse.getEntity() );
					//Log.i( "andy" , "result = " + result );
					JSONObject jsonObject = new JSONObject( result );
					if( jsonObject.optJSONArray( "results" ) != null )
					{
						synchronized( locationThreadTest )
						{
							//Log.i( "andy" , "result !=null !!!! " );
							JSONArray jsonArray = jsonObject.optJSONArray( "results" ).optJSONObject( 0 ).optJSONArray( "address_components" );
							String country = null;
							String city = null;
							for( int i = 0 ; i < jsonArray.length() ; i++ )
							{
								for( int j = 0 ; j < jsonArray.optJSONObject( i ).optJSONArray( "types" ).length() ; j++ )
								{
									if( jsonArray.optJSONObject( i ).optJSONArray( "types" ).optString( j ).equals( "country" ) )
									{
										country = jsonArray.optJSONObject( i ).optString( "long_name" );
										Log.i( "lbs" , country );
									}
									if( jsonArray.optJSONObject( i ).optJSONArray( "types" ).optString( j ).equals( "locality" ) )
									{
										city = jsonArray.optJSONObject( i ).optString( "long_name" );
										Log.i( "lbs" , city );
									}
								}
							}
							address = country + "," + city;
							//Log.i( "andy" , "address = " + address );
						}
					}
				}
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
			catch( ClientProtocolException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				synchronized( locationThreadTest )
				{
					//Log.i( "andy" , "finally" );
					locationThreadTest.notify();
				}
			}
		}
	}
}
