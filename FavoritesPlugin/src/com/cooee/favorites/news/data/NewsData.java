package com.cooee.favorites.news.data;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.ad.news.KmobNewsMessage;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.utils.CooeeLocation;
import com.cooee.favorites.utils.NetworkAvailableUtils;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.kmob.kmobsdk.NativeAdData;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.FavoriteControl.FavoriteControlHelper;
import cool.sdk.log.CoolLog;


public class NewsData
{
	
	public static final String TAG = "news";
	public static final int NEWS_FROM_TOUTIAO = 0;
	public static final int NEWS_FROM_HUBII_WITH_AD = 1;
	public static final int NEWS_FROM_HUBII_WITHOUT_AD = 2;
	public static final int NEWS_FROM_DIANKU = 3;
	public static int newsSource = NEWS_FROM_TOUTIAO;
	//toutiao
	private final String URL_ACCESS_TOKEN = "http://open.snssdk.com/auth/access/device/";
	private final String URL_STREAM = "http://open.snssdk.com/data/stream/v3/?";
	private final String URL_PASS_BACK = "http://open.snssdk.com/action/push/";
	private final String partner_name = "launcherkuyu";
	private final String secure_key = "54980f8849ed4bf53d628cae47383528";
	private HashMap<String , String> accessTokenParams = null;
	private String accessToken = null;
	private long min_time = 0;
	private long max_time = 0;
	private long cursor = 0;
	private final String count = "50";
	//hubii
	private final String URL_HUBII_STREAM_WITH_AD = "http://ns1.newsportal.hk";
	private final String URL_HUBII_STREAM_WITHOUT_AD = "http://api.hubii.com";
	private final String Topics_News = "522504d18588881182161cd1";
	//	private String LanguageID = "5165222bbbddbd146800000b";//默认值为中文的id
	private final String CHANNEL_ID = "fcc7895f-a3c3-11e5-a237-00163e003029";
	//dianku
	private final String dk_secure_key = "679a5e995881b52c3635c242fa559e2e5f9a9f516fd7fefad3d9100f79f074ca65e3072ac1e6f28c71b7fa2b98ff0a1917b18c4a7cfdc060893794037145ac42b60b780672dc86362acf6f56c539c72a5d680eabdf09f85d8b40ac69db4c91ceb13bf133a0e1b0dbf080068111a6f2d925472fe25345a1615f313d446a957d86";
	private final String URL_DIANKU_STREAM = "http://news.news2048.com/project/news/index.php";
	private boolean gettingAccessToken = false;
	private boolean refreshing = false;
	private boolean fetching = false;
	private boolean gettingCategroy = false;
	private Callbacks mCallbacks;
	private RequestQueue mQueue;
	private Context mContext;
	private JSONArray mAdData;
	private JSONArray mCustomAdData;
	private ArrayList<NewsItem> mTempItems = new ArrayList<NewsItem>();//用来更新数据，更新完成后，都放到mItems，然后通知界面变化，防止出现在更新过程中，出现不同步的情况
	private HashMap<String , ArrayList<NewsItem>> mItems;
	private KmobNewsMessage mKmobMessage;
	private int adPlace[] = new int[]{ 5 , 15 , 22 , 29 , 36 };
	private boolean locating = false;
	private JSONArray listCountry = new JSONArray();
	private JSONArray enterList;
	private SharedPreferences mPreferences;
	private final int PASSBACK_LENGTH = 20;//
	private static CoolLog Log;
	private StringRequest mRequest;
	private boolean isIniting = false;
	private final int MAX_REQUERST_COUNT = 2;
	private Handler mHandler;
	private int initial_newsSource = newsSource;
	private ArrayList<CategoryItem> mCategoryItems = new ArrayList<CategoryItem>();
	private final String QUEUE_TAG = "queue_tag";
	private String defaultCountry[] = null;
	private CooeeLocation mLocation;
	private String last_refresh_id = null;
	private boolean isCheckCountry = false;
	
	public NewsData(
			Context cxt ,
			RequestQueue queue ,
			HashMap<String , ArrayList<NewsItem>> list ,
			Callbacks cb )
	{
		this.mContext = cxt;
		this.mQueue = queue;
		this.mItems = list;
		this.mCallbacks = cb;
		Log = new CoolLog( cxt );
		mKmobMessage = new KmobNewsMessage( NewsData.this );
		mHandler = new Handler( FavoritesManager.getInstance().getContainerContext().getMainLooper() );
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				init();
				try
				{
					refresh( null );
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} ).start();
	}
	
	public void init()
	{
		isIniting = true;
		try
		{
			String control = FavoriteControlHelper.getInstance( mContext ).getC6();
			if( control != null && control != "" )
			{
				JSONObject obj = new JSONObject( control );
				initial_newsSource = obj.getInt( FavoriteControlHelper.NEWS_SOURCE );
				updateAdplace( obj.getString( FavoriteControlHelper.AD_PLACE ) );
			}
			else
			{
				initial_newsSource = mContext.getResources().getInteger( R.integer.config_news_source );
				if( initial_newsSource == -1 )
				{
					initial_newsSource = NEWS_FROM_TOUTIAO;
				}
				updateAdplace( FavoritesManager.getInstance().getPluginContext().getResources().getString( R.string.config_ad_place ) );
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			initial_newsSource = mContext.getResources().getInteger( R.integer.config_news_source );
			if( initial_newsSource == -1 )
			{
				initial_newsSource = NEWS_FROM_TOUTIAO;
			}
			updateAdplace( FavoritesManager.getInstance().getPluginContext().getResources().getString( R.string.config_ad_place ) );
		}
		//cheyingkun add start	//运营关闭新闻后，再运营打开，新闻刷新不出来【i_0014803】
		if( initial_newsSource == NewsData.NEWS_FROM_DIANKU )
		{
			initial_newsSource = NewsData.NEWS_FROM_TOUTIAO;
		}
		//cheyingkun add end
		defaultCountry = mContext.getResources().getString( R.string.default_country ).split( "," );
		newsSource = initial_newsSource;
		handleGetTouTiaoAccessTokenParams();
		isIniting = false;
	}
	
	public String getMacAddress(
			Context context )
	{
		// 获取mac地址：
		String macAddress = "000000000000";
		try
		{
			WifiManager wifiMgr = (WifiManager)context.getSystemService( Context.WIFI_SERVICE );
			WifiInfo info = ( null == wifiMgr ? null : wifiMgr.getConnectionInfo() );
			if( null != info )
			{
				if( !TextUtils.isEmpty( info.getMacAddress() ) )
				{
					macAddress = info.getMacAddress().replace( ":" , "" );
					Log.v( "web" , "mac:" + macAddress );
				}
				else
					return macAddress;
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return macAddress;
		}
		return macAddress;
	}
	
	private void handleGetTouTiaoAccessTokenParams()
	{
		accessTokenParams = new HashMap<String , String>();
		Context act = mContext;
		TelephonyManager TelephonyMgr = (TelephonyManager)act.getSystemService( Context.TELEPHONY_SERVICE );
		String deviceid = TelephonyMgr.getDeviceId();
		if( deviceid == null )
			deviceid = "";
		accessTokenParams.put( "udid" , deviceid );
		accessTokenParams.put( "openudid" , Secure.getString( act.getContentResolver() , Secure.ANDROID_ID ) );
		accessTokenParams.put( "os" , "Android" );
		accessTokenParams.put( "mc" , getMacAddress( act ) );
		accessTokenParams.put( "os_version" , Build.VERSION.RELEASE );
		accessTokenParams.put( "os_api" , String.valueOf( Build.VERSION.SDK_INT ) );
		accessTokenParams.put( "device_model" , Build.MANUFACTURER );
		accessTokenParams.put( "resolution" , act.getResources().getDisplayMetrics().widthPixels + "x" + act.getResources().getDisplayMetrics().heightPixels );
		accessTokenParams.put( "display_density" , getDensity( act ) );
		//			jsonObj.put( "carrier" , "" );
		accessTokenParams.put( "language" , act.getResources().getConfiguration().locale.getLanguage() );
		mPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
		String country = mPreferences.getString( "selected_country" , null );
		if( country != null )
		{
			accessTokenParams.put( "Name" , country.split( "&" )[0] );
			accessTokenParams.put( "Code" , country.split( "&" )[1] );
			if( mCallbacks != null )
			{
				mCallbacks.updateCountryName( country );
			}
			if( initial_newsSource != NEWS_FROM_DIANKU )
			{
				if( !country.split( "&" )[1].equals( "cn" ) )//如果选择的国家不是中国，就一定要用hubbi，是中国就根据配置来
				{
					newsSource = NEWS_FROM_HUBII_WITH_AD;
				}
				else
				{
					newsSource = initial_newsSource;
				}
			}
			checkCountryIfChanged();//，这时候读取的是上一次使用的国家，如果这时候wifi定位的国家已经发生变化，也要提醒用户
		}
		if( newsSource == NEWS_FROM_TOUTIAO )
		{
			String clicks = mPreferences.getString( "clickNews" , null );
			if( clicks == null )
			{
				enterList = new JSONArray();
			}
			else
			{
				try
				{
					enterList = new JSONArray( clicks );
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					enterList = new JSONArray();
				}
			}
		}
	}
	
	public void refresh(
			final String categoryId ) throws JSONException
	{
		if( !NetworkAvailableUtils.isNetworkAvailable( mContext ) )
		{
			if( mCallbacks != null )
			{
				mCallbacks.NetworkError( categoryId );
			}
			return;
		}
		Log.v( TAG , "refresh news categoryId = " + categoryId );
		if( accessTokenParams == null )
		{
			Log.v( TAG , "accessTokenParams==null,return" );
			// Stop the ion-refresher from spinning
			init();
		}
		if( refreshing || fetching || gettingAccessToken || isIniting || gettingCategroy || locating )
		{
			Log.v( TAG , "refreshing or fetching,return " + refreshing + " " + fetching + " " + gettingAccessToken + " " + isIniting + " " + gettingCategroy + " " + locating );
			// Stop the ion-refresher from spinning
			return;
		}
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( mCallbacks != null )
				{
					mCallbacks.NewsStartRefreshing( categoryId );
				}
				//				if( mAdData == null || mAdData.length() < 5 )//广告有实时性，每次刷新都去请求
				if( accessTokenParams.get( "Code" ) == null )//定位，若是国外则使用hubii，国内在根据配置来，默认头条
				{
					getCountryCodeByLocation( categoryId , true );
				}
				else
				{
					if( mCategoryItems.size() == 0 )
					{
						Log.v( "news" , "getCategoryItem categoryId = " + categoryId );
						getCategoryItem( categoryId , true );
					}
					else
					{
						if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD || newsSource == NEWS_FROM_DIANKU )
						{
							mKmobMessage.getKmobMessage( mContext );
							getStream( categoryId , true );
						}
						else
						{
							if( accessToken == null )
							{
								getAccessToken( categoryId );
							}
							else
							{
								mKmobMessage.getKmobMessage( mContext );
								getStream( categoryId , true );
							}
						}
					}
					checkCountryIfChanged();
				}
			}
		} );
	}
	
	public static String getDensity(
			Context context )
	{
		int dpi = context.getResources().getDisplayMetrics().densityDpi;
		// gaominghui@2017/01/05 ADD START 兼容 Android 4.0
		switch( dpi )
		{
			case DisplayMetrics.DENSITY_LOW:
				return "ldpi";
			case DisplayMetrics.DENSITY_MEDIUM:
				return "mdpi";
			case DisplayMetrics.DENSITY_HIGH:
				return "hdpi";
			case DisplayMetrics.DENSITY_XHIGH:
				return "xhdpi";
			default:
				if( Build.VERSION.SDK_INT >= 16 )
				{
					if( dpi == DisplayMetrics.DENSITY_XXHIGH )
					{
						return "xxhdpi";
					}
				}
				else if( Build.VERSION.SDK_INT >= 18 )
				{
					if( dpi == DisplayMetrics.DENSITY_XXXHIGH )
					{
						return "xxxhdpi";
					}
				}
				return "mdpi";
		}
		// gaominghui@2017/01/05 ADD END 兼容 Android 4.0
	}
	
	//	public void getLanguageId()//该方法没有调用，删掉
	//	{
	//		AssetManager asset = mContext.getAssets();
	//		try
	//		{
	//			InputStream is = asset.open( "www/languageList.json" );
	//			byte[] buffer = new byte[is.available()];
	//			is.read( buffer );
	//			String json = new String( buffer );
	//			JSONArray jsonArray = new JSONArray( json );
	//			for( int i = 0 ; i < jsonArray.length() ; i++ )
	//			{
	//				if( jsonArray.getJSONObject( i ).getString( "Code" ) == accessTokenParams.get( "language" ) )
	//				{
	//					LanguageID = jsonArray.getJSONObject( i ).getString( "ID" );
	//				}
	//			}
	//		}
	//		catch( IOException e )
	//		{
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		catch( JSONException e )
	//		{
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	}
	public void getCountryCodeByLocation(
			final String categoryId ,
			final boolean isrefresh )
	{
		if( locating )
		{
			return;
		}
		locating = true;
		if( listCountry.length() == 0 )
		{
			InputStream is = mContext.getResources().openRawResource( R.raw.countrylist );
			byte[] buffer;
			try
			{
				buffer = new byte[is.available()];
				is.read( buffer );
				String json = new String( buffer );
				JSONArray jsonArray = new JSONArray( json );
				for( int i = 0 ; i < jsonArray.length() ; i++ )
				{
					listCountry.put( jsonArray.get( i ) );
				}
			}
			catch( IOException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if( mLocation == null )
			mLocation = new CooeeLocation( mContext );
		mLocation.setTryNum( 3 );
		mLocation.setUseIpLocationAndListener( true , new CooeeLocation.IpRequestListener() {
			
			@Override
			public void onLocationChanged(
					String countryCode ,
					String countryName ,
					int error )
			{
				// TODO Auto-generated method stub
				Log.v( "news" , "countryCode = " + countryCode );
				if( !TextUtils.isEmpty( countryCode ) )
				{
					String code = countryCode.toLowerCase();
					accessTokenParams.put( "Code" , code );
					if( newsSource != NEWS_FROM_DIANKU )
					{
						if( "cn".equals( code ) )
						{
							newsSource = initial_newsSource;
						}
						else
						{
							newsSource = NEWS_FROM_HUBII_WITH_AD;
						}
					}
					for( int i = 0 ; i < listCountry.length() ; i++ )
					{
						try
						{
							JSONObject obj = listCountry.getJSONObject( i );
							if( obj.getString( "Code" ).equals( code ) )
							{
								accessTokenParams.put( "Name" , obj.getString( "Name" ) );
								break;
							}
						}
						catch( JSONException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else
				{
					if( defaultCountry != null && defaultCountry.length >= 2 )
					{
						accessTokenParams.put( "Name" , defaultCountry[0] );
						accessTokenParams.put( "Code" , defaultCountry[1] );
						if( newsSource != NEWS_FROM_DIANKU )
						{
							if( "cn".equals( defaultCountry[1] ) )
							{
								newsSource = initial_newsSource;
							}
							else
							{
								newsSource = NEWS_FROM_HUBII_WITH_AD;
							}
						}
					}
					else
					{
						accessTokenParams.put( "Code" , "cn" );
						accessTokenParams.put( "Name" , "中国" );
						newsSource = initial_newsSource;
					}
				}
				mPreferences.edit().putString( "selected_country" , accessTokenParams.get( "Name" ) + "&" + accessTokenParams.get( "Code" ) ).commit();
				mCallbacks.updateCountryName( accessTokenParams.get( "Name" ) + "&" + accessTokenParams.get( "Code" ) );
				locating = false;
				try
				{
					if( isrefresh )
					{
						refresh( categoryId );
					}
					else
					{
						fetch( categoryId );
					}
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} );
		mLocation.startLocation();
	}
	
	public void getStream(
			final String categoryId ,
			final boolean refresh )
	{
		Log.v( TAG , "get stream..." );
		if( refresh )
		{
			refreshing = true;
		}
		else
			fetching = true;
		String action = null;
		Log.v( TAG , " newsSource = " + newsSource );
		if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD )
		{
			action = "use_hubii_news";
		}
		else if( newsSource == NEWS_FROM_TOUTIAO )
		{
			action = "use_toutiao_news";
		}
		else if( newsSource == NEWS_FROM_DIANKU )
		{
			action = "use_dianku_news";
		}
		if( action != null )
		{
			FavoritesConfig config = FavoritesManager.getInstance().getConfig();
			if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
			{
				MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , action );
			}
			try
			{
				StatisticsExpandNew.onCustomEvent(
						FavoritesManager.getInstance().getContainerContext() ,
						action ,
						FavoritesPlugin.SN ,
						FavoritesPlugin.APPID ,
						CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
						FavoritesPlugin.PRODUCTTYPE ,
						FavoritesPlugin.PluginPackageName ,
						FavoritesPlugin.UPLOAD_VERSION + "" ,
						null );
			}
			catch( NoSuchMethodError e )
			{
				try
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							action ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName );
				}
				catch( NoSuchMethodError e1 )
				{
					StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , action , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
				}
			}
		}
		String url = null;
		int requestMethod = Request.Method.GET;
		if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD )
		{
			if( newsSource == NEWS_FROM_HUBII_WITH_AD )
				url = URL_HUBII_STREAM_WITH_AD + "/v1/articles?";
			else
				url = URL_HUBII_STREAM_WITHOUT_AD + "/articles?";
			url += "country=" + accessTokenParams.get( "Code" );
			if( "cn".equals( accessTokenParams.get( "Code" ) ) )//hubii更新api，languageid跟系统语言一致，zh，en这种
				url += "&languages=zh";
			if( categoryId != null && !categoryId.equals( "news_recommend" ) )
			{
				url += "&topics=" + categoryId;
			}
			url += "&limit=" + count;
			if( refresh )
				url += "&offset=" + 0;
			else
				url += "&offset=" + mTempItems.size();
			url += "&channel=" + CHANNEL_ID;
		}
		else if( newsSource == NEWS_FROM_DIANKU )
		{
			url = URL_DIANKU_STREAM;
			requestMethod = Request.Method.POST;
		}
		else
		{
			if( categoryId == null || categoryId.equals( "news_recommend" ) )
			{
				url = URL_STREAM;
			}
			else
			{
				url = URL_STREAM + "category=" + categoryId;
			}
			String timestamp = String.valueOf( System.currentTimeMillis() / 1000 );
			Random rd = new Random();
			String nonce = String.valueOf( rd.nextInt( 999 ) + 1 );
			url += "&access_token=" + accessToken;
			url += "&timestamp=" + timestamp;
			url += "&nonce=" + nonce;
			url += "&signature=" + getSignature( secure_key , timestamp , nonce );
			url += "&partner=" + partner_name;
			if( refresh )
				url += "&min_behot_time=" + max_time;
			else
				url += "&max_behot_time=" + min_time;
			url += "&count=" + count;
		}
		Log.v( TAG , "url:" + url );
		mRequest = new StringRequest( requestMethod , url , new Response.Listener<String>() {
			
			@Override
			public void onResponse(
					String reponse )
			{
				doRequestWhileSuccess( categoryId , reponse , refresh );
			}
		} , new Response.ErrorListener() {
			
			@Override
			public void onErrorResponse(
					VolleyError error )
			{
				Log.v( TAG , "response error " + error.getMessage() + " categoryId = " + categoryId );
				doRequestWhileFail( categoryId , refresh );
			}
		} ) {
			
			@Override
			public Map<String , String> getHeaders() throws AuthFailureError
			{
				// TODO Auto-generated method stub
				if( newsSource == NEWS_FROM_DIANKU )
				{
					Map<String , String> map = new HashMap<String , String>();
					map.put( "Content-Type" , "application/json" );
					return map;
				}
				else
				{
					return super.getHeaders();
				}
			}
			
			@Override
			public byte[] getBody() throws AuthFailureError
			{
				// TODO Auto-generated method stub
				if( newsSource == NEWS_FROM_DIANKU )
				{
					Random rd = new Random();
					JSONObject obj = new JSONObject( accessTokenParams );
					String nonce = String.valueOf( rd.nextInt( 999 ) + 1 );
					String timestamp = String.valueOf( System.currentTimeMillis() / 1000 );
					String signature = getSignature( dk_secure_key , timestamp , nonce );
					try
					{
						obj.put( "signature" , signature );
						obj.put( "timestamp" , timestamp );
						obj.put( "nonce" , nonce );
						obj.put( "country_code" , accessTokenParams.get( "Code" ) );
						if( refresh )
						{
							if( mTempItems.size() > 0 )
								obj.put( "refresh" , 1 );
						}
						else
						{
							obj.put( "max_behot_time" , cursor );
						}
					}
					catch( JSONException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.v( TAG , "cursor = " + obj.toString() );
					return obj.toString().getBytes();
				}
				else
				{
					return super.getBody();
				}
			}
		};
		mRequest.setRetryPolicy( new DefaultRetryPolicy( 4000 , MAX_REQUERST_COUNT , 0.5f ) );//控制在10s内，重复次数设置为2次
		mRequest.setTag( categoryId );
		mQueue.add( mRequest );
	}
	
	private void doRequestWhileSuccess(
			String categoryId ,
			String reponse ,
			boolean refresh )
	{
		Log.v( TAG , "reponse = success " + categoryId );
		if( refresh )
			refreshing = false;
		else
			fetching = false;
		mRequest = null;
		boolean isSuccess = true;
		if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD )
		{
			try
			{
				JSONArray obj = new JSONArray( reponse );//添加保护，jsonarray不能转换成jsonobject
				onStreamReady( categoryId , reponse , refresh );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
				isSuccess = false;
			}
		}
		else
		{
			try
			{
				JSONObject obj = new JSONObject( reponse );
				int ret = obj.getInt( "ret" );
				if( ret == 0 )
				{
					onStreamReady( categoryId , obj.getString( "data" ) , refresh );
				}
				else
				{
					if( newsSource == NEWS_FROM_DIANKU )
					{
						if( ret == -200 || ret == -300 )
						{
							if( mCallbacks != null )
							{
								if( refresh )
									mCallbacks.NewsRefreshEnd( categoryId , Callbacks.NO_UPDATE_NEWS );
								else
									mCallbacks.NewsFetchEnd( categoryId , Callbacks.NO_MORE_NEWS );
								return;
							}
						}
					}
					Log.v( TAG , ret + "," + obj.getString( "msg" ) );
					isSuccess = false;
				}
			}
			catch( JSONException e )
			{
				e.printStackTrace();
				isSuccess = false;
			}
		}
		if( mCallbacks != null )
		{
			if( isSuccess )
			{
				if( refresh )
					mCallbacks.NewsRefreshEnd( categoryId , Callbacks.SUCCESS );
				else
					mCallbacks.NewsFetchEnd( categoryId , Callbacks.SUCCESS );
			}
			else
			{
				if( refresh )
					mCallbacks.NewsRefreshEnd( categoryId , Callbacks.FAIL );
				else
					mCallbacks.NewsFetchEnd( categoryId , Callbacks.FAIL );
			}
		}
	}
	
	private void doRequestWhileFail(
			String categoryId ,
			boolean refresh )
	{
		if( refresh )
			refreshing = false;
		else
			fetching = true;
		mRequest = null;
		if( refresh )
			mCallbacks.NewsRefreshEnd( categoryId , Callbacks.FAIL );
		else
			mCallbacks.NewsFetchEnd( categoryId , Callbacks.FAIL );
	}
	
	public String getFrontRequestCategoryAndStop(
			String nowcategoryId )
	{
		String front = null;
		if( mRequest != null )
		{
			Log.v( TAG , "mRequest = " + mRequest.getTag() + " " + nowcategoryId );
			if( mRequest.getTag() != null && mRequest.getTag().equals( nowcategoryId ) )
			{
				return null;
			}
			front = (String)mRequest.getTag();
			mRequest.cancel();
		}
		refreshing = false;
		fetching = false;
		return front;
	}
	
	public void stopRefresh()
	{
		if( mQueue != null )
		{
			mQueue.cancelAll( QUEUE_TAG );
			if( mRequest != null )
				mRequest.cancel();
		}
		refreshing = false;
		fetching = false;
		gettingAccessToken = false;
		gettingCategroy = false;
	}
	
	public void getAccessToken(
			final String categoryId )
	{
		Log.v( TAG , "get access token..." );
		gettingAccessToken = true;
		String url = URL_ACCESS_TOKEN;
		String timestamp = String.valueOf( System.currentTimeMillis() / 1000 );
		Random rd = new Random();
		String nonce = String.valueOf( rd.nextInt( 999 ) + 1 );
		url += "?";
		url += "timestamp=" + timestamp;
		url += "&nonce=" + nonce;
		url += "&signature=" + getSignature( secure_key , timestamp , nonce );
		url += "&partner=" + partner_name;
		Log.v( TAG , "url:" + url );
		StringRequest jsonObjectRequest = new StringRequest( Request.Method.POST , url , new Response.Listener<String>() {
			
			@Override
			public void onResponse(
					String response )
			{
				gettingAccessToken = false;
				Log.v( TAG , "get access token success! response " + response );
				try
				{
					JSONObject obj = new JSONObject( response );
					int ret = obj.getInt( "ret" );
					if( ret == 0 )
					{
						accessToken = obj.getJSONObject( "data" ).getString( "access_token" );
						refresh( categoryId );
					}
					else
					{
						Log.v( TAG , ret + "," + obj.getString( "msg" ) + "," + obj.getString( "data" ) );
						mCallbacks.NewsRefreshEnd( categoryId , Callbacks.FAIL );
					}
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					mCallbacks.NewsRefreshEnd( categoryId , Callbacks.FAIL );
				}
			}
		} , new Response.ErrorListener() {
			
			@Override
			public void onErrorResponse(
					VolleyError error )
			{
				gettingAccessToken = false;
				Log.v( TAG , "get access token error! " + error );
				mCallbacks.NewsRefreshEnd( categoryId , Callbacks.FAIL );
			}
		} ) {
			
			@Override
			protected Map<String , String> getParams() throws AuthFailureError
			{
				// TODO Auto-generated method stub
				return accessTokenParams;
			}
			
			@Override
			public Map<String , String> getHeaders() throws AuthFailureError
			{
				// TODO Auto-generated method stub
				Map<String , String> map = new HashMap<String , String>();
				map.put( "Content-Type" , "application/x-www-form- urlencoded" );
				return map;
			}
		};
		jsonObjectRequest.setRetryPolicy( new DefaultRetryPolicy( 4000 , MAX_REQUERST_COUNT , 0.5f ) );//控制在10s内，重复次数设置为2次
		//		jsonObjectRequest.setTag( TAG );
		//		jsonObjectRequest.setShouldCache( true );
		jsonObjectRequest.setTag( QUEUE_TAG );
		mQueue.add( jsonObjectRequest );
	}
	
	public void onStreamReady(
			final String categoryId ,
			final String data ,
			final boolean refresh )
	{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				try
				{
					mTempItems.clear();
					if( mItems.get( categoryId ) != null )
						mTempItems.addAll( mItems.get( categoryId ) );
					JSONArray obj;
					if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD )
					{
						obj = new JSONArray( data );
						if( refresh )
							mTempItems.clear();//因为hubii的新闻没有对应的时间戳，刷新内容一致
					}
					else if( newsSource == NEWS_FROM_TOUTIAO )
					{
						obj = new JSONArray( data );
					}
					else
					{
						JSONObject temp = new JSONObject( data );
						if( temp.has( "cursor" ) )
							cursor = temp.getLong( "cursor" );
						obj = temp.getJSONArray( "data" );
					}
					JSONObject subobj;
					if( refresh )
					{
						NewsItem temp = null;
						if( newsSource == NEWS_FROM_TOUTIAO || newsSource == NEWS_FROM_DIANKU )
						{
							for( NewsItem item : mTempItems )
							{
								if( "专题".equals( item.getSite() ) )//专题置顶，找到list中的专题
								{
									temp = item;
									break;
								}
							}
						}
						for( int i = obj.length() - 1 ; i >= 0 ; i-- )
						{
							subobj = obj.getJSONObject( i );
							try
							{
								addNewsItem( subobj , refresh );
								addAdItemInNews( i , refresh );
							}
							catch( JSONException e )
							{
								e.printStackTrace();
							}
						}
						if( newsSource == NEWS_FROM_TOUTIAO || newsSource == NEWS_FROM_DIANKU )//头条刷新时，每次都会返回专题的数据，导致重复，删除掉重复的，并保持专题数据置顶位置
						{
							if( mTempItems.size() > 0 )
							{
								if( temp != null )
								{
									mTempItems.remove( temp );
									if( mTempItems.size() == 0 || !mTempItems.get( 0 ).getSite().equals( "专题" ) )//若第一项不是专题，则加上去
									{
										mTempItems.add( 0 , temp );
									}
								}
							}
						}
					}
					else
					{
						for( int i = 0 ; i < obj.length() ; i++ )
						{
							subobj = obj.getJSONObject( i );
							try
							{
								addAdItemInNews( i , refresh );
								addNewsItem( subobj , refresh );
							}
							catch( JSONException e )
							{
								e.printStackTrace();
							}
						}
					}
					int length = mTempItems.size();
					Log.v( "news" , "mTempItems.size() = " + mTempItems.size() + " last_refresh_id = " + last_refresh_id );
					if( length > 0 )
					{
						max_time = mTempItems.get( 0 ).getDisplayTime();
						min_time = mTempItems.get( length - 1 ).getDisplayTime();
						Log.v( "zjp" , "mTempItems = " + mTempItems.get( length - 1 ).getDisplayTime() );
						if( last_refresh_id != null )
						{
							int index = -1;
							NewsItem remove = null;
							for( int i = mTempItems.size() - 1 ; i >= 0 ; i-- )
							{
								NewsItem item = mTempItems.get( i );
								if( item.getNotifyType() != 0 )
								{
									remove = item;
								}
								if( i > 0 )
								{
									if( last_refresh_id.equals( item.getGroupId() ) )
									{
										index = i;
									}
								}
							}
							if( index > 0 && index < mTempItems.size() )
							{
								NewsItem item = new NewsItem();
								item.setNotifyType( 1 );
								mTempItems.add( index , item );
							}
							if( remove != null )
							{
								mTempItems.remove( remove );
							}
						}
						if( "专题".equals( mTempItems.get( 0 ).getSite() ) && mTempItems.size() > 1 )//专题是置顶
						{
							last_refresh_id = mTempItems.get( 1 ).getGroupId();
						}
						else
						{
							last_refresh_id = mTempItems.get( 0 ).getGroupId();
						}
					}
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
				if( mItems.get( categoryId ) != null )
				{
					mItems.get( categoryId ).clear();
					mItems.get( categoryId ).addAll( mTempItems );
				}
			}
		} );
	}
	
	public void addAdItemInNews(
			int index ,
			boolean refresh ) throws JSONException
	{
		String newsId = FavoritesManager.getInstance().getConfig().getString( FavoriteConfigString.getNewsAdPlaceIdKey() , FavoriteConfigString.getNewsAdPlaceIdValue() );
		if( !TextUtils.isEmpty( newsId ) )
		{
			if( ( refresh && index == 0 ) )
			{
				addAdItem( refresh , 0 , mCustomAdData );
				return;
			}
		}
		else
		{
			if( refresh && index == 1 )//这样保证一刷新界面上就能看到我们的广告
			{
				addAdItem( refresh , 0 , mAdData );
				return;
			}
		}
		if( !refresh && index == mTempItems.size() )
		{
			addAdItem( refresh , 0 , mAdData );
			return;
		}
		for( int i = 0 ; i < adPlace.length ; i++ )
		{
			if( refresh )//refresh时数据是从后往前加的
			{
				if( !TextUtils.isEmpty( newsId ) )
				{
					if( mCustomAdData != null && mCustomAdData.length() > 0 )//这个说明是从第一条会加上一条广告
					{
						if( index == adPlace[i] - 2 - i )//这边的广告位置要算上第一条和自己本身，所以减2减i
						{
							addAdItem( refresh , i + 1 , mAdData );
							break;
						}
					}
					else
					{
						if( index == adPlace[i] - 1 - i )//这边第一条不是广告，只要-1-i
						{
							addAdItem( refresh , i + 1 , mAdData );
							break;
						}
					}
				}
				else
				{
					if( index == adPlace[i] - 2 - i )//这个情况是默认在刷出来的第二条加上一条广告
					{
						addAdItem( refresh , i + 2 , mAdData );// 这个方法中的第二个参数表示之前当前这条广告之前还要显示多少条广告，因为是从后往前加，防止获取到的广告数量不够，广告显示到后面
						break;
					}
				}
			}
			else
			{
				if( mTempItems.size() == adPlace[i] - 1 )
				{
					addAdItem( refresh , i , mAdData );
					break;
				}
			}
		}
	}
	
	//新增一条新闻
	public void addNewsItem(
			JSONObject subobj ,
			boolean refresh ) throws JSONException
	{
		final NewsItem item = new NewsItem();
		if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD )
		{
			item.setTitle( subobj.getString( "Header" ) );
			item.setSite( "" );
			item.setGroupId( subobj.getString( "ArticleId" ) );
			StringRequest stringRquest = new StringRequest( subobj.getString( "Publication" ) , new Response.Listener<String>() {
				
				@Override
				public void onResponse(
						String response )
				{
					try
					{
						JSONObject obj = new JSONObject( response );
						item.setSite( obj.getString( "Name" ) );
						item.setSiteAndComments( item.getSite() );
					}
					catch( JSONException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} , new Response.ErrorListener() {
				
				@Override
				public void onErrorResponse(
						VolleyError error )
				{
				}
			} );
			stringRquest.setTag( QUEUE_TAG );
			mQueue.add( stringRquest );
			item.setShowTime( subobj.getString( "Published" ).replace( "T" , "" ).replace( "Z" , "" ).substring( 5 , 10 ) );
			if( newsSource == NEWS_FROM_HUBII_WITH_AD )
				item.setNewsUrl( URL_HUBII_STREAM_WITH_AD + subobj.getString( "URL" ) );
			else
			{
				StringRequest request = new StringRequest( URL_HUBII_STREAM_WITHOUT_AD + "/articles/" + subobj.getString( "ArticleId" ) , new Response.Listener<String>() {
					
					@Override
					public void onResponse(
							String response )
					{
						try
						{
							JSONObject obj = new JSONObject( response );
							item.setNewsUrl( obj.getString( "URL" ) );
						}
						catch( JSONException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} , new Response.ErrorListener() {
					
					@Override
					public void onErrorResponse(
							VolleyError error )
					{
					}
				} );
				request.setTag( QUEUE_TAG );
				mQueue.add( request );
			}
			item.setImageList( null );
			if( subobj.has( "Image" ) )
			{
				JSONObject img = subobj.getJSONObject( "Image" );
				if( img.has( "url" ) && img.getString( "url" ) != null && img.getString( "url" ) != "" )
				{
					int width = img.getInt( "width" );
					int height = img.getInt( "height" );
					if( width * 1.0 / height >= 1.8 && width * 1.0 / height <= 2.3 )
					{// 防止图片过长或过窄变形
						item.setHQImageUrl( img.getString( "url" ) );
					}
					else
					{
						item.setThumbImage( img.getString( "url" ) );
					}
				}
			}
		}
		else
		{
			item.setGroupId( subobj.getString( "group_id" ) );//新闻的id，用于回传
			item.setTitle( subobj.getString( "title" ) );
			if( subobj.has( "label" ) && !TextUtils.isEmpty( subobj.getString( "label" ) ) )
			{
				item.setSite( subobj.getString( "label" ) );
			}
			else
			{
				item.setSite( subobj.getString( "source" ) );
			}
			item.setComments( subobj.getInt( "comment_count" ) );
			item.setSiteAndComments( item.getSite() + " " + FavoritesManager.getInstance().getPluginContext().getString( R.string.news_comments ) + " " + item.getComments() );
			if( subobj.has( "our_news_url" ) && !TextUtils.isEmpty( subobj.getString( "our_news_url" ) ) )
				item.setNewsUrl( subobj.getString( "our_news_url" ) );
			else if( subobj.has( "article_url" ) && !TextUtils.isEmpty( subobj.getString( "article_url" ) ) )
				item.setNewsUrl( subobj.getString( "article_url" ) );
			else
				item.setNewsUrl( subobj.getString( "toutiao_wap_url" ) );
			if( subobj.has( "behot_time" ) && !TextUtils.isEmpty( subobj.getString( "behot_time" ) ) )
			{
				item.setDisplayTime( subobj.getLong( "behot_time" ) );
				item.setShowTime( new java.text.SimpleDateFormat( "MM-dd " ).format( new java.util.Date( subobj.getLong( "behot_time" ) * 1000 ) ) );
			}
			else
			{
				item.setShowTime( new java.text.SimpleDateFormat( "MM-dd " ).format( new java.util.Date( subobj.getLong( "publish_time" ) * 1000 ) ) );
				item.setDisplayTime( subobj.getLong( "display_time" ) );
			}
			//            long delta = subobj.getLong( "display_time" ) -  subobj.getLong( "publish_time" );
			//            if (delta <= 60 && delta > 0){
			//                item.setShowTime( "刚刚" )  ;
			//            }else if(delta <= 60*60 && delta > 60){
			//                item.setShowTime (delta/60+ "分钟前");
			//            }else if(delta > 60*60 && delta <= 60*60*24){
			//                item.setShowTime ( delta/60/60+ "小时前");
			//            }else
			//			{
			//				item.setShowTime( new java.text.SimpleDateFormat( "MM-dd " ).format( new java.util.Date( subobj.getLong( "publish_time" ) * 1000 ) ) );
			//			}
			if( subobj.has( "images" ) && !TextUtils.isEmpty( subobj.getString( "images" ) ) )
			{
				JSONArray array = subobj.getJSONArray( "images" );
				if( array.length() > 0 )
				{
					int width = array.getJSONObject( 0 ).getInt( "width" );
					int height = array.getJSONObject( 0 ).getInt( "height" );
					if( width * 1.0 / height >= 1.8 && width * 1.0 / height <= 2.3 )
					{
						item.setHQImageUrl( array.getJSONObject( 0 ).getJSONArray( "urls" ).getString( 0 ) );
					}
					else
					{
						item.setThumbImage( array.getJSONObject( 0 ).getJSONArray( "urls" ).getString( 0 ) );
					}
				}
			}
			if( subobj.has( "list_images" ) && !TextUtils.isEmpty( subobj.getString( "list_images" ) ) )
			{
				JSONArray array = subobj.getJSONArray( "list_images" );
				String list[] = new String[array.length()];
				int index = 0;
				for( int i = 0 ; i < array.length() ; i++ )
				{
					JSONArray urls = array.getJSONObject( i ).getJSONArray( "urls" );
					if( urls != null && urls.length() > 0 )
					{
						list[index] = urls.getString( 0 );
						index++;
					}
				}
				if( index > 2 )
				{
					item.setImageList( list );
					item.setHQImageUrl( null );
					item.setThumbImage( null );
				}
				else if( index == 1 )
				{
					if( item.getThumbImage() == null && item.getHQImageUrl() == null )
					{
						item.setThumbImage( list[0] );
					}
				}
			}
			//头条新闻接口更新
			if( subobj.has( "middle_image" ) && !TextUtils.isEmpty( subobj.getString( "middle_image" ) ) )
			{
				JSONObject obj = subobj.getJSONObject( "middle_image" );
				if( obj.has( "url" ) )
				{
					item.setThumbImage( subobj.getJSONObject( "middle_image" ).getString( "url" ) );
					item.setImageList( null );
					item.setHQImageUrl( null );
				}
			}
			if( subobj.has( "image_list" ) && !TextUtils.isEmpty( subobj.getString( "image_list" ) ) )
			{
				JSONArray array = subobj.getJSONArray( "image_list" );
				if( array.length() > 0 )
				{
					String list[] = new String[array.length()];
					int index = 0;
					for( int i = 0 ; i < array.length() ; i++ )
					{
						list[index] = array.getJSONObject( i ).getString( "url" );
						index++;
					}
					if( index > 2 )
					{
						item.setImageList( list );
						item.setThumbImage( null );
						item.setHQImageUrl( null );
					}
					else
					{
						item.setThumbImage( list[0] );
						item.setImageList( null );
						item.setHQImageUrl( null );
					}
				}
			}
			if( subobj.has( "large_image_list" ) && !TextUtils.isEmpty( subobj.getString( "large_image_list" ) ) )
			{
				JSONArray array = subobj.getJSONArray( "large_image_list" );
				if( array.length() > 0 )
				{
					item.setHQImageUrl( array.getJSONObject( 0 ).getString( "url" ) );
					item.setThumbImage( null );
					item.setImageList( null );
				}
			}
		}
		if( refresh )
			mTempItems.add( 0 , item );
		else
		{
			mTempItems.add( item );
		}
	}
	
	public void updateAdData(
			JSONArray array )
	{
		//		if( mAdData == null )
		//		{
		//			mAdData = array;
		//		}
		//		else
		//		{
		//			if( array != null )
		//			{
		//				for( int index = 0 ; index < array.length() ; index++ )
		//				{
		//					try
		//					{
		//						mAdData.put( mAdData.length() , array.getJSONObject( index ) );
		//					}
		//					catch( JSONException e )
		//					{
		//						// TODO Auto-generated catch block
		//						e.printStackTrace();
		//					}
		//				}
		//			}
		//		}
		if( mAdData != null )
		{
			mAdData = null;
		}
		mAdData = array;
	}
	
	public void updateCustomAdData(
			JSONArray array )
	{
		if( mCustomAdData != null )
		{
			mCustomAdData = null;
		}
		mCustomAdData = array;
	}
	
	@SuppressLint( "NewApi" )
	public void addAdItem(
			boolean fresh ,
			int index ,
			JSONArray mAdArray ) throws JSONException
	{
		if( mAdArray != null && mAdArray.length() > 0 )
		{
			if( fresh )
			{
				if( index > mAdArray.length() )
				{//刷新时，保证广告都在显示在前面
					return;
				}
			}
			JSONObject obj = mAdArray.getJSONObject( 0 );
			NewsItem item = new NewsItem();
			item.setTitle( obj.getString( NativeAdData.SUMMARY_TAG ) );//产品要求广告的摘要代替标题
			if( TextUtils.isEmpty( item.getTitle() ) )
				item.setTitle( obj.getString( NativeAdData.HEADLINE_TAG ) );
			//				item.setTitle( FavoritesManager.getInstance().getPluginContext().getString( R.string.news_title_null ) );
			item.setSite( FavoritesManager.getInstance().getPluginContext().getString( R.string.news_recommend ) );
			item.setSiteAndComments( item.getSite() );
			item.setComments( 0 );
			item.setShowTime( "" );
			//			if( obj.has( NativeAdData.HIIMG_TAG ) ),广告的高清图是高度大于宽度的
			//			{
			//				try
			//				{
			//					String temp = obj.getString( NativeAdData.HIIMG_TAG );
			//					JSONArray array = new JSONArray( temp );
			//					item.setHQImageUrl( array.getJSONObject( 0 ).getString( "url" ) );
			//				}
			//				catch( Exception e )
			//				{
			//				}
			//			}
			//			else
			{
				String temp = obj.getString( NativeAdData.CTIMG_TAG );
				try
				{
					JSONArray array = new JSONArray( temp );
					if( array != null && array.length() > 0 )
					{
						if( array.length() >= 3 )
						{
							String list[] = new String[]{ array.getJSONObject( 1 ).getString( "url" ) , array.getJSONObject( 0 ).getString( "url" ) , array.getJSONObject( 2 ).getString( "url" ) };
							int i = new Random().nextInt( 2 );
							if( i == 0 )//随机显示三张小图和一张小图的
							{
								item.setThumbImage( array.getJSONObject( 0 ).getString( "url" ) );
							}
							else
							{
								item.setImageList( list );
							}
						}
						else if( array.length() == 1 )//只有一张说明是高清图
						{
							item.setHQImageUrl( array.getJSONObject( 0 ).getString( "url" ) );
							item.setHQWidth( array.getJSONObject( 0 ).getInt( "width" ) );
							item.setHQHeight( array.getJSONObject( 0 ).getInt( "height" ) );
						}
						else
						{
							item.setThumbImage( array.getJSONObject( 0 ).getString( "url" ) );
						}
					}
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
			}
			JSONObject other = new JSONObject( obj.toString() );
			//			other.put( NativeAdData.AD_ID_TAG , obj.getString( NativeAdData.AD_ID_TAG ) );
			//			other.put( NativeAdData.ADPLACE_ID_TAG , obj.getString( NativeAdData.AD_ID_TAG ) );
			//			other.put( NativeAdData.CLICKURL_TAG , obj.getString( NativeAdData.CLICKURL_TAG ) );
			//			other.put( NativeAdData.INTERACTION_TYPE_TAG , obj.getString( NativeAdData.INTERACTION_TYPE_TAG ) );
			//			other.put( NativeAdData.OPEN_TYPE_TAG , obj.getString( NativeAdData.OPEN_TYPE_TAG ) );
			item.setOtherInfo( other );
			if( fresh )
				mTempItems.add( 0 , item );
			else
				mTempItems.add( item );
			Log.v( "zjp" , "mTempItems = " + mTempItems.size() );
			if( Build.VERSION.SDK_INT > 19 )
				mAdArray.remove( 0 );
			else
				JSONArray_Remove( 0 , mAdArray );
			if( mCallbacks != null )
				mCallbacks.onNativeAdShow( item );
		}
	}
	
	public void JSONArray_Remove(
			int index ,
			JSONArray array )
	{
		if( index < 0 )
			return;
		Field valuesField;
		try
		{
			valuesField = JSONArray.class.getDeclaredField( "values" );
			valuesField.setAccessible( true );
			List<Object> values = (List<Object>)valuesField.get( array );
			if( index >= values.size() )
				return;
			values.remove( index );
		}
		catch( NoSuchFieldException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IllegalArgumentException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getSignature(
			String secure_key ,
			String timestamp ,
			String nonce )
	{
		ArrayList<String> list = new ArrayList<String>();
		list.add( secure_key );
		list.add( timestamp );
		list.add( nonce );
		Collections.sort( list );
		String temp = list.get( 0 ) + list.get( 1 ) + list.get( 2 );
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance( "SHA-1" );
			md.update( temp.getBytes( "UTF-8" ) );
			byte[] result = md.digest();
			return bytetoString( result );
		}
		catch( NoSuchAlgorithmException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String bytetoString(
			byte[] digest )
	{
		String str = "";
		String tempStr = "";
		for( int i = 0 ; i < digest.length ; i++ )
		{
			tempStr = ( Integer.toHexString( digest[i] & 0xff ) );
			if( tempStr.length() == 1 )
			{
				str = str + "0" + tempStr;
			}
			else
			{
				str = str + tempStr;
			}
		}
		return str.toLowerCase();
	}
	
	//开始获取更多新闻
	public void fetch(
			final String categoryId ) throws JSONException
	{
		if( !NetworkAvailableUtils.isNetworkAvailable( mContext ) )
		{
			if( mCallbacks != null )
			{
				mCallbacks.NetworkError( categoryId );
			}
			return;
		}
		if( refreshing || fetching || gettingAccessToken || gettingCategroy || locating )
		{
			if( mCallbacks != null )
				mCallbacks.NewsFetchEnd( categoryId , Callbacks.FAIL );
			return;
		}
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( accessTokenParams.get( "Code" ) == null )
				{
					getCountryCodeByLocation( categoryId , false );
				}
				else
				{
					if( mCategoryItems.size() == 0 )
					{
						getCategoryItem( categoryId , false );
					}
					else
					{
						if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD || newsSource == NEWS_FROM_DIANKU )
						{
							mKmobMessage.getKmobMessage( mContext );
							getStream( categoryId , false );
						}
						else
						{
							if( accessToken == null )
							{
								getAccessToken( categoryId );
							}
							else
							{
								mKmobMessage.getKmobMessage( mContext );
								getStream( categoryId , false );
							}
						}
						checkCountryIfChanged();
					}
				}
			}
		} );
	}
	
	public void updateCountry(
			String country )
	{
		// TODO Auto-generated method stub
		try
		{
			if( country != null )
			{
				accessTokenParams.put( "Code" , country.split( "&" )[1] );
				accessTokenParams.put( "Name" , country.split( "&" )[0] );
				mPreferences.edit().putString( "selected_country" , country ).commit();
			}
			max_time = 0;
			min_time = 0;
			cursor = 0;
			mCategoryItems.clear();
			if( initial_newsSource != NEWS_FROM_DIANKU )
			{
				if( country.split( "&" )[1].equals( "cn" ) )
				{
					newsSource = initial_newsSource;
				}
				else
				{
					newsSource = NEWS_FROM_HUBII_WITH_AD;
				}
			}
			refresh( null );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	public String setNewsDataSource(
			int source )
	{
		//cheyingkun add start	//点酷新闻不维护,如果是点酷则改为默认新闻(头条)【i_0014710】
		if( source == NEWS_FROM_DIANKU )
		{
			source = NEWS_FROM_TOUTIAO;
		}
		//cheyingkun add end
		initial_newsSource = source;
		if( source == NEWS_FROM_DIANKU || "cn".equals( accessTokenParams.get( "Code" ) ) )//点酷新闻也可更新，是中国，才可通过服务器设置新闻源，不是中国，则用hubii
		{
			newsSource = source;
			return accessTokenParams.get( "Name" ) + "&" + accessTokenParams.get( "Code" );
		}
		return null;
		//		if( mCallbacks != null )
		//			mCallbacks.updateNewsSource( source );
	}
	
	public void updateAdplace(
			String place )
	{
		String[] temp = place.split( "," );
		adPlace = new int[temp.length];
		for( int i = 0 ; i < temp.length ; i++ )
		{
			adPlace[i] = Integer.valueOf( temp[i] );
		}
	}
	
	public JSONArray getCountryList()
	{
		return listCountry;
	}
	
	public void toutiaoPassback()//回传
	{
		Log.v( TAG , "get pass back..." );
		if( accessToken == null || enterList.length() == 0 || !NetworkAvailableUtils.isNetworkAvailable( mContext ) )
		{
			return;
		}
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				String url = URL_PASS_BACK;
				String timestamp = String.valueOf( System.currentTimeMillis() / 1000 );
				Random rd = new Random();
				String nonce = String.valueOf( rd.nextInt( 999 ) + 1 );
				url += "?";
				url += "timestamp=" + timestamp;
				url += "&nonce=" + nonce;
				url += "&signature=" + getSignature( secure_key , timestamp , nonce );
				url += "&partner=" + partner_name;
				Log.v( TAG , "url:" + url );
				try
				{
					JSONObject json = new JSONObject();
					json.put( "access_token" , accessToken );
					json.put( "actions" , enterList.toString() );
					Log.v( TAG , "content = " + json.toString() );
					String result = doPost( url , json );
					Log.v( TAG , "toutiaoPassback result = " + result );
					if( result != null )
					{
						JSONObject obj = new JSONObject( result );
						if( obj.getInt( "ret" ) == 0 )
						{
							Log.v( TAG , "passback success  " );
							enterList = new JSONArray();
							mPreferences.edit().putString( "clickNews" , enterList.toString() ).commit();
						}
						else
						{
							Log.v( TAG , "passback error =  " + result );
						}
					}
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} );
	}
	
	public String doPost(
			String url ,
			JSONObject json )
	{
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost( url );
		try
		{
			StringEntity s = new StringEntity( json.toString() );
			s.setContentEncoding( "UTF-8" );
			s.setContentType( "application/json" );//发送json数据需要设置contentType
			post.setEntity( s );
			HttpResponse res = client.execute( post );
			if( res.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
			{
				HttpEntity entity = res.getEntity();
				String result = EntityUtils.toString( res.getEntity() );// 返回json格式：
				return result;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void addClickItem(
			NewsItem item )
	{
		if( newsSource == NEWS_FROM_TOUTIAO )//点击新闻，记录下数据
		{
			JSONObject obj = new JSONObject();
			try
			{
				obj.put( "timestamp" , System.currentTimeMillis() / 1000 );
				obj.put( "type" , "enter" );
				obj.put( "id" , "" );
				JSONObject data = new JSONObject();
				data.put( "group_id" , item.getGroupId() );
				obj.put( "data" , data.toString() );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
			if( enterList == null )
				enterList = new JSONArray();
			enterList.put( obj );
			mPreferences.edit().putString( "clickNews" , enterList.toString() ).commit();
			if( enterList.length() >= PASSBACK_LENGTH )//当达到一定条数，回传
			{
				toutiaoPassback();
			}
		}
	}
	
	public void clearAllData()
	{
		// TODO Auto-generated method stub
		if( mTempItems != null )
		{
			mTempItems.clear();
		}
	}
	
	private void getCategoryItem(
			final String categoryId ,
			final boolean refresh )
	{
		gettingCategroy = true;
		if( newsSource == NEWS_FROM_HUBII_WITH_AD || newsSource == NEWS_FROM_HUBII_WITHOUT_AD )
		{
			final String country_code = accessTokenParams.get( "Code" );
			if( country_code != null )
			{
				String url = URL_HUBII_STREAM_WITH_AD + "/v1/categories?country=" + country_code + "&channel=" + CHANNEL_ID;
				Log.v( TAG , "getCategoryItem url = " + url );
				StringRequest request = new StringRequest( url , new Response.Listener<String>() {
					
					@Override
					public void onResponse(
							String response )
					{
						Log.v( TAG , "getCategoryItem success " + response );
						gettingCategroy = false;
						boolean isSuccess = false;
						try
						{
							JSONArray array = new JSONArray( response );
							for( int i = 0 ; i < array.length() ; i++ )
							{
								JSONObject obj = array.getJSONObject( i );
								String name;
								if( TextUtils.isEmpty( obj.getString( "native_name" ) ) )
								{
									name = obj.getString( "name" );
								}
								else
								{
									name = obj.getString( "native_name" );
								}
								if( "cn".equals( country_code ) && !isChinese( name ) )
								{
									continue;
								}
								CategoryItem item = new CategoryItem();
								item.setCategoryId( obj.getString( "code" ) );
								item.setCategoryName( name );
								mCategoryItems.add( item );
							}
							isSuccess = true;
						}
						catch( JSONException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if( isSuccess )
						{
							try
							{
								if( mCategoryItems.size() == 0 )
								{
									CategoryItem item = new CategoryItem();
									item.setCategoryName( "" );
									item.setCategoryId( "news_recommend" );
									mCategoryItems.add( item );
								}
								if( mCallbacks != null )
								{
									mCallbacks.updateCategroy( Callbacks.SUCCESS , mCategoryItems );
								}
								String id = categoryId;
								if( id == null )
								{
									id = mCategoryItems.get( 0 ).getCategoryId();
								}
								if( refresh )
									refresh( id );
								else
									fetch( id );
							}
							catch( JSONException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							if( mCallbacks != null )
							{
								mCallbacks.updateCategroy( Callbacks.FAIL , null );
							}
						}
					}
				} , new Response.ErrorListener() {
					
					@Override
					public void onErrorResponse(
							VolleyError arg0 )
					{
						// TODO Auto-generated method stub
						Log.v( TAG , "getCategoryItem error = " + arg0 );
						gettingCategroy = false;
						if( mCallbacks != null )
						{
							mCallbacks.updateCategroy( Callbacks.FAIL , null );
						}
					}
				} );
				request.setRetryPolicy( new DefaultRetryPolicy( 4000 , MAX_REQUERST_COUNT , 0.5f ) );//控制在10s内
				request.setTag( QUEUE_TAG );
				mQueue.add( request );
			}
		}
		else if( newsSource == NEWS_FROM_DIANKU )
		{
			CategoryItem item = new CategoryItem();
			item.setCategoryName( "" );
			item.setCategoryId( "news_recommend" );
			mCategoryItems.add( item );
			if( mCallbacks != null )
			{
				mCallbacks.updateCategroy( Callbacks.SUCCESS , mCategoryItems );
			}
			gettingCategroy = false;
			try
			{
				String id = categoryId;
				if( id == null )
				{
					id = mCategoryItems.get( 0 ).getCategoryId();
				}
				if( refresh )
					refresh( id );
				else
					fetch( id );
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			InputStream is = mContext.getResources().openRawResource( R.raw.toutiao_category );
			byte[] buffer;
			try
			{
				buffer = new byte[is.available()];
				is.read( buffer );
				String json = new String( buffer );
				JSONArray jsonArray = new JSONArray( json );
				for( int i = 0 ; i < jsonArray.length() ; i++ )
				{
					CategoryItem item = new CategoryItem();
					JSONObject obj = jsonArray.getJSONObject( i );
					item.setCategoryId( obj.getString( "code" ) );
					item.setCategoryName( obj.getString( "native_name" ) );
					mCategoryItems.add( item );
				}
				if( mCallbacks != null )
				{
					mCallbacks.updateCategroy( Callbacks.SUCCESS , mCategoryItems );//通知界面更新
				}
				gettingCategroy = false;
				try
				{
					String id = categoryId;
					if( id == null )
					{
						id = mCategoryItems.get( 0 ).getCategoryId();
					}
					if( refresh )
						refresh( id );
					else
						fetch( id );
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch( IOException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void printStackTrace(
			String tag )
	{
		String info = null;
		ByteArrayOutputStream baos = null;
		PrintStream printStream = null;
		try
		{
			baos = new ByteArrayOutputStream();
			printStream = new PrintStream( baos );
			new Throwable().printStackTrace( printStream );
			byte[] data = baos.toByteArray();
			info = new String( data );
			data = null;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		long threadId = Thread.currentThread().getId();
		Log.v( tag , "Thread.getName()=" + Thread.currentThread().getName() + " id=" + threadId + " state=" + Thread.currentThread().getState() );
		Log.v( tag , "Stack[" + info + "]" );
	}
	
	public boolean isChinese(
			String str )
	{
		String regEx = "[\\u4e00-\\u9fa5]+";
		Pattern p = Pattern.compile( regEx );
		Matcher m = p.matcher( str );
		if( m.find() )
			return true;
		else
			return false;
	}
	
	public void checkCountryIfChanged()
	{
		if( locating || isCheckCountry || accessTokenParams == null || accessTokenParams.get( "Code" ) == null )
		{
			return;
		}
		Log.v( TAG , "checkCountryIfChanged " + accessTokenParams.get( "Code" ) + " locating = " + locating + " isCheckCountry = " + isCheckCountry );
		isCheckCountry = true;
		if( listCountry.length() == 0 )
		{
			InputStream is = mContext.getResources().openRawResource( R.raw.countrylist );
			byte[] buffer;
			try
			{
				buffer = new byte[is.available()];
				is.read( buffer );
				String json = new String( buffer );
				JSONArray jsonArray = new JSONArray( json );
				for( int i = 0 ; i < jsonArray.length() ; i++ )
				{
					listCountry.put( jsonArray.get( i ) );
				}
			}
			catch( IOException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if( mLocation == null )
		{
			mLocation = new CooeeLocation( mContext );
			mLocation.setTryNum( 3 );
		}
		mLocation.setUseIpLocationAndListener( true , new CooeeLocation.IpRequestListener() {
			
			@Override
			public void onLocationChanged(
					String countryCode ,
					String countryName ,
					int error )
			{
				// TODO Auto-generated method stub
				Log.v( "news" , "check countryCode = " + countryCode + " countryName = " + countryName );
				if( !TextUtils.isEmpty( countryCode ) )
				{
					String code = countryCode.toLowerCase();
					String notify = mPreferences.getString( "notify_country" , null );//提醒过一次，记录下来，下次就不再提示
					Log.v( "news" , "notify = " + notify + " accessTokenParams.get( Code ) = " + accessTokenParams.get( "Code" ) );
					if( !code.equals( notify ) )
					{
						mPreferences.edit().remove( "notify_country" ).commit();
					}
					if( !code.equals( accessTokenParams.get( "Code" ) ) )
					{
						if( !code.equals( notify ) )
						{
							if( mCallbacks != null )
							{
								String name = "";
								for( int i = 0 ; i < listCountry.length() ; i++ )
								{
									try
									{
										JSONObject obj = listCountry.getJSONObject( i );
										if( obj.getString( "Code" ).equals( code ) )
										{
											name = obj.getString( "Name" );
											break;
										}
									}
									catch( JSONException e )
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								if( TextUtils.isEmpty( name ) )
								{
									if( !TextUtils.isEmpty( countryName ) )
									{
										name = countryName;
									}
								}
								mCallbacks.notifyCountryChanged( name , code );
								mPreferences.edit().putString( "notify_country" , code ).commit();
							}
						}
					}
				}
				isCheckCountry = false;
			}
		} );
		mLocation.startLocation();
	}
}
