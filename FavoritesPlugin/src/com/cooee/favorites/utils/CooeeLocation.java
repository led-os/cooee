package com.cooee.favorites.utils;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;


/**
 * 使用 WGS84 坐标定位.
 * <p>
 * 无网络 + 有GPS 条件下, 使用 WGS84 坐标可定位, 而使用 GCJ-02 坐标无法定位!
 * 
 */
public class CooeeLocation implements TencentLocationListener
{
	
	private static final String TAG = "CooeeLoction";
	private Context mContext = null;
	private TencentLocationManager mLocationManager;
	private int defaultTencentLocationRequestNum = 1;
	private int mTencentLocationRequestNum = 0;
	private TencentLocationListener mTencentLocationListener = null;
	private int mRequestLevel = TencentLocationRequest.REQUEST_LEVEL_GEO;
	//static int	REQUEST_LEVEL_ADMIN_AREA 
	//    定位结果信息级别: 包含经纬度, 行政区划.
	//static int	REQUEST_LEVEL_GEO 
	//    定位结果信息级别: 仅包含经纬度坐标表示的地位置(经纬度).
	//static int	REQUEST_LEVEL_NAME 
	//    定位结果信息级别: 包含经纬度, 位置名称, 位置地址.
	//static int	REQUEST_LEVEL_POI 
	//    定位结果信息级别: 包含经纬度, 行政区划, 附近的POI
	private boolean mUseIpLocation = false;
	private static final String SERVICE_BASE_URL = "https://freegeoip.net/json/";
	static RequestQueue mRequestQueue;
	private IpRequestListener mIpRequestListener = null;
	public static int REQUEST_CODE_ERR = -1;
	public static int REQUEST_CODE_OK = 0;
	
	public interface IpRequestListener
	{
		
		/**
		 * 
		 * @param countryCode  国家简写
		 * @param error 0成功  -1失败
		 */
		void onLocationChanged(
				String countryCode ,
				String countryName ,
				int error );
	}
	
	public CooeeLocation(
			Context context )
	{
		mContext = context;
	}
	
	/**
	 * 设置定位回调监听
	 * @param tencentLocationListener
	 */
	public void setTencentLocationListener(
			TencentLocationListener tencentLocationListener )
	{
		mTencentLocationListener = tencentLocationListener;
	}
	
	/**
	 * 如果定位失败 再次尝试次数，默认为1
	 * @param num
	 */
	public void setTryNum(
			int num )
	{
		defaultTencentLocationRequestNum = num;
	}
	
	/**
	 * 设置定位精度 默认只获取经纬度，当设置mUseIpLocation为true时候，此项无效。
	 * @param requestLevel
	 */
	public void setRequestLevel(
			int requestLevel )
	{
		mRequestLevel = requestLevel;
	}
	
	/**
	 * 是置使用ip定位，如果为true，返回结果只有城市码可用。
	 * @param useIpLocation
	 */
	public void setUseIpLocationAndListener(
			boolean useIpLocation ,
			IpRequestListener ipRequestListener )
	{
		mUseIpLocation = useIpLocation;
		mIpRequestListener = ipRequestListener;
	}
	
	public void stopLocation()
	{
		mLocationManager.removeUpdates( this );
	}
	
	// 响应点击"开始"
	public int startLocation()
	{
		if( mUseIpLocation )
		{
			getWebIp( mContext );
			return 0;
		}
		if( mLocationManager == null )
			mLocationManager = TencentLocationManager.getInstance( mContext );
		/* 保证调整坐标系前已停止定位 */
		mLocationManager.removeUpdates( null );
		// 设置 wgs84 坐标系
		//		mLocationManager.setCoordinateType( TencentLocationManager.COORDINATE_TYPE_WGS84 );
		TencentLocationRequest request = TencentLocationRequest.create();
		// 修改定位请求参数, 定位周期 3000 ms
		request.setInterval( 3000 );
		request.setRequestLevel( mRequestLevel );
		return mLocationManager.requestLocationUpdates( request , this );
	}
	
	@Override
	public void onLocationChanged(
			TencentLocation location ,
			int error ,
			String reason )
	{
		if( error == TencentLocation.ERROR_OK )
		{
			if( mTencentLocationListener != null )
				mTencentLocationListener.onLocationChanged( location , error , reason );
			stopLocation();
		}
		else
		{
			mTencentLocationRequestNum++;
			if( mTencentLocationRequestNum > defaultTencentLocationRequestNum )
			{
				stopLocation();
			}
		}
	}
	
	@Override
	public void onStatusUpdate(
			String arg0 ,
			int arg1 ,
			String arg2 )
	{
		// TODO Auto-generated method stub
	}
	
	public void getWebIp(
			Context context )
	{
		if( mRequestQueue == null )
			mRequestQueue = Volley.newRequestQueue( context );
		FakeX509TrustManager.allowAllSSL();
		JsonObjectRequest geoInfoRequest = new JsonObjectRequest( SERVICE_BASE_URL , null , new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(
					JSONObject response )
			{
				try
				{
					String ip = response.getString( "ip" );
					String country_code = response.getString( "country_code" );
					String country_name = response.getString( "country_name" );
					if( mIpRequestListener != null )
						mIpRequestListener.onLocationChanged( country_code , country_name , 0 );
					Log.v( TAG , "ip = " + ip + "-country_code = " + country_code );
				}
				catch( JSONException e )
				{
					Log.e( TAG , "Can't parse Geo Info." , e );
					if( mIpRequestListener != null )
						mIpRequestListener.onLocationChanged( null , null , -1 );
				}
			}
		} , new Response.ErrorListener() {
			
			@Override
			public void onErrorResponse(
					VolleyError error )
			{
				Log.e( TAG , "Can't retrieve Geo Information." , error );
				if( mIpRequestListener != null )
					mIpRequestListener.onLocationChanged( null , null , -1 );
			}
		} );
		geoInfoRequest.setRetryPolicy( new DefaultRetryPolicy( 4000 , 2 , 0.5f ) );//zhujieping modify，控制定位时间在10s内，第一个参数是第一次超时时间，第二个参数是重试次数，第三个是每次重试时的超时时间加上的系数
		mRequestQueue.add( geoInfoRequest );
	}
}
