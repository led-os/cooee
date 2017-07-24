package com.cooee.favorites.manager;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.ad.nearby.KmobAdMessage;
import com.cooee.favorites.ad.news.KmobNewsMessage;
import com.cooee.favorites.apps.FavoritesAppData;
import com.cooee.favorites.apps.FavoritesAppManager;
import com.cooee.favorites.apps.MonitorThread;
import com.cooee.favorites.clings.FavoritesClingsView;
import com.cooee.favorites.data.AppInfo;
import com.cooee.favorites.data.NearByItem;
import com.cooee.favorites.db.FavoritesHelper;
import com.cooee.favorites.news.NewsView;
import com.cooee.favorites.recommended.FavoriteMainView;
import com.cooee.favorites.utils.Tools;
import com.cooee.favorites.view.FavoritesViewGroup;
import com.cooee.favorites.view.FavoritesViewGroupsParent;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.cooee.uniex.wrap.IFavoriteClings;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.FavoriteControl.FavoriteControlHelper;


public class FavoritesManager
{
	
	// zhangjin@2016/06/08 ADD START
	private static final String FAVORITES_NEARBY_LOCAL = "nearby";
	private static final String FAVORITES_NEARBY_LOCAL_ITEM_TITLE = "title";
	private static final String FAVORITES_NEARBY_LOCAL_ITEM_TITLE_CN = "title_zh_rCN";
	private static final String FAVORITES_NEARBY_LOCAL_ITEM_TITLE_TW = "title_zh_rTW";
	private static final String FAVORITES_NEARBY_LOCAL_ITEM_IMG = "img";
	private static final String FAVORITES_NEARBY_LOCAL_ITEM_CMP = "cmp";
	private static final String FAVORITES_NEARBY_LOCAL_ITEM_URL = "url";
	private static final String FAVORITES_NEARBY_LOCAL_ITEM_EXTRA = "extra_";
	// zhangjin@2016/06/08 ADD END
	private static FavoritesManager instance = null;
	private Context containerContext; // 主context
	private Context proxyContext;
	private Context pluginContext;// 自身context
	private FavoritesViewGroup view;// 推荐、附近、新闻
	/**酷生活返回的view(包含酷生活内容和引导页)*/
	private FavoritesViewGroupsParent mFavoritesViewGroupsParent;//cheyingkun add	//酷生活引导页
	private CopyOnWriteArrayList<AppInfo> lastAppData = null;
	private FavoritesHelper helper;
	private int mFlag = -1;
	private FavoritesReceiver receiver;
	private View mNewsView;
	private NewsView mNewsViewCooee;
	private FavoriteMainView mFavoriteMainView;
	private FavoritesClingsView mFavoritesClingsView;//cheyingkun add	//酷生活引导页
	private FavoritesConfig config;
	private FavoritesPlugin plugin;
	public static final String OPERATE_SWITCH = "operate_switch";
	public static final String NEWS_STATE = "news_state";
	/**引导页消失时,各个模块延迟动画的因子*/
	private int showFavoritesViewDelayNum = 0;
	private int favoritesViewAnimShowDelay = 0;
	//
	private RequestQueue queue;
	//
	private static final int FAVORITES_STATE_ERROR = -1;
	private static final int FAVORITES_STATE_COOEE_NEWS_COLLAPSE = 0;//cooee新闻，半屏
	private static final int FAVORITES_STATE_COOEE_NEWS_EXPANDED = 1;//cooee新闻，全屏
	private boolean isLoadFinish = false;
	
	public static FavoritesManager getInstance()
	{
		if( instance == null )
		{
			synchronized( FavoritesManager.class )
			{
				if( instance == null )
				{
					instance = new FavoritesManager();
				}
			}
		}
		return instance;
	}
	
	public void init(
			FavoritesPlugin plugin ,
			FavoritesConfig config )
	{
		this.plugin = plugin;
		this.config = config;
		this.containerContext = plugin.getContainerContext();
		this.proxyContext = plugin.getProxyContext();
		this.pluginContext = plugin.getPluginContext();
		helper = new FavoritesHelper( containerContext );
		initAdId();
		initViews();
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
		filter.addAction( Intent.ACTION_SCREEN_ON );
		filter.addAction( Intent.ACTION_SCREEN_OFF );
		filter.addAction( ConnectivityManager.CONNECTIVITY_ACTION );
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		filter.addAction( WifiManager.NETWORK_STATE_CHANGED_ACTION );
		filter.addAction( WifiManager.WIFI_STATE_CHANGED_ACTION );
		filter.addAction( "android.intent.action.PHONE_STATE" );
		filter.addAction( "android.intent.action.USER_PRESENT" );
		filter.addAction( "android.provider.Telephony.SMS_RECEIVED" );
		receiver = new FavoritesReceiver( pluginContext , mNewsViewCooee );
		containerContext.getApplicationContext().registerReceiver( receiver , filter );
		//cheyingkun add start	//解决“通话界面手机电流增高”的问题【c_0004610】
		if( this.config != null && this.config.getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() ) )
		//cheyingkun add end
		{
			// ，5.0以下通过服务，每隔一秒获取最上层activity，来更新常用应用，但5.0以上需要系统权限，才能获取到系统最上层activity，因此通过点击来更新常用应用
			if( Build.VERSION.SDK_INT < 21 || isSystemApp( containerContext ) )
			{
				new MonitorThread( containerContext ).start();
			}
		}
		favoritesViewAnimShowDelay = pluginContext.getResources().getInteger( R.integer.favorites_view_anim_show_delay );//cheyingkun add	//修改酷生活S5引导页动画。
		queue = Volley.newRequestQueue( containerContext );
	}
	
	@SuppressWarnings( "deprecation" )
	public void initViews()
	{
		//内容view
		view = (FavoritesViewGroup)LayoutInflater.from( pluginContext ).cloneInContext( pluginContext ).inflate( R.layout.page_main , null );
		if( config.getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
		{
			view.setBackgroundDrawable( pluginContext.getResources().getDrawable( R.drawable.simplelauncher_background ) );
		}
		final boolean isShow = isShowNews();
		if( isShow )
		{
			mNewsViewCooee = new NewsView( pluginContext , containerContext );
		}
		AsyncTask.execute( new Runnable() {//上传新闻开关的统计
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						JSONObject obj = new JSONObject();
						try
						{
							obj.put( "param1" , NEWS_STATE + ":" + isShow );
						}
						catch( JSONException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try
						{
							StatisticsExpandNew.onCustomEvent(
									containerContext ,
									OPERATE_SWITCH ,
									plugin.getSN() ,
									plugin.getAppID() ,
									CooeeSdk.cooeeGetCooeeId( containerContext ) ,
									plugin.getProductType() ,
									FavoritesPlugin.PluginPackageName ,
									FavoritesPlugin.UPLOAD_VERSION + "" ,
									obj );
						}
						catch( NoSuchMethodError e )
						{
							try
							{
								StatisticsExpandNew.onCustomEvent(
										containerContext ,
										OPERATE_SWITCH ,
										plugin.getSN() ,
										plugin.getAppID() ,
										CooeeSdk.cooeeGetCooeeId( containerContext ) ,
										plugin.getProductType() ,
										FavoritesPlugin.PluginPackageName ,
										obj );
							}
							catch( NoSuchMethodError e1 )
							{
								StatisticsExpandNew.onCustomEvent( containerContext , OPERATE_SWITCH , plugin.getProductType() , FavoritesPlugin.PluginPackageName , obj );
							}
						}
						HashMap<String , String> map = new HashMap<String , String>();
						map.put( NEWS_STATE , isShow + "" );
						if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
						{
							MobclickAgent.onEvent( containerContext , OPERATE_SWITCH , map );
						}
						try
						{
							StatisticsExpandNew.onCustomEvent(
									FavoritesManager.getInstance().getContainerContext() ,
									OPERATE_SWITCH ,
									FavoritesPlugin.SN ,
									FavoritesPlugin.APPID ,
									CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
									FavoritesPlugin.PRODUCTTYPE ,
									FavoritesPlugin.PluginPackageName ,
									FavoritesPlugin.UPLOAD_VERSION + "" ,
									new JSONObject( map ) );
						}
						catch( NoSuchMethodError e )
						{
							try
							{
								StatisticsExpandNew.onCustomEvent(
										FavoritesManager.getInstance().getContainerContext() ,
										OPERATE_SWITCH ,
										FavoritesPlugin.SN ,
										FavoritesPlugin.APPID ,
										CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
										FavoritesPlugin.PRODUCTTYPE ,
										FavoritesPlugin.PluginPackageName ,
										new JSONObject( map ) );
							}
							catch( NoSuchMethodError e1 )
							{
								StatisticsExpandNew.onCustomEvent(
										FavoritesManager.getInstance().getContainerContext() ,
										OPERATE_SWITCH ,
										FavoritesPlugin.PRODUCTTYPE ,
										FavoritesPlugin.PluginPackageName ,
										new JSONObject( map ) );
							}
						}
					}
				} );
		mFavoriteMainView = new FavoriteMainView( pluginContext );
		mNewsView = mNewsViewCooee;
		view.setup( mFavoriteMainView , mNewsView );
		if( mNewsViewCooee != null )
			view.setStickCallback( mNewsViewCooee );
		//cheyingkun add start	//酷生活引导页
		boolean switchEnableClings = config.getBoolean( FavoriteConfigString.getEnableFavoritesClingsKey() , FavoriteConfigString.isEnableFavoritesClingsDefaultValue() );
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( FavoritesManager.getInstance().getContainerContext() );
		boolean canShowClings = mSharedPreferences.getBoolean( FavoritesClingsView.FAVORITE_CLING_KEY , true );
		boolean enable_s5 = config.getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() );
		if( switchEnableClings && canShowClings )
		{//显示酷生活引导页
			mFavoritesClingsView = new FavoritesClingsView( pluginContext , switchEnableClings , enable_s5 );
		}
		//cheyingkun add end
		//组装内容和引导页
		mFavoritesViewGroupsParent = new FavoritesViewGroupsParent( pluginContext , view , mFavoritesClingsView );
	}
	
	public boolean isShowNews()
	{
		String control = FavoriteControlHelper.getInstance( containerContext ).getString( FavoriteControlHelper.ENABLE_NEWS );
		if( control != null )
		{
			return control.equals( "true" );
		}
		if( config.getBoolean( FavoriteConfigString.getEnableNewsKey() , FavoriteConfigString.isEnableNewsDefaultValue() ) )
		{
			return true;
		}
		return false;
	}
	
	public FavoritesConfig getConfig()
	{
		return this.config;
	}
	
	public Context getContainerContext()
	{
		return this.containerContext;
	}
	
	public Context getProxyContext()
	{
		return this.proxyContext;
	}
	
	public Context getPluginContext()
	{
		return this.pluginContext;
	}
	
	public View getView()
	{
		return this.mFavoritesViewGroupsParent;
	}
	
	public void onLoadFinish()
	{
		isLoadFinish = true;
	}
	
	public boolean isLoadFinish()
	{
		return isLoadFinish;
	}
	
	public void initAdId()
	{
		KmobNewsMessage.setAdPlaceId( AdPlaceIdManager.getInstance().getNewsAdId()[0] );
		KmobAdMessage.setAdPlaceId( AdPlaceIdManager.getInstance().getNearbyAdId() );
	}
	
	public void onPageFinishedLoading()
	{
		if( lastAppData != null )
		{
			mFavoriteMainView.bindApp( lastAppData );
			mFavoriteMainView.bindNearby();//cheyingkun add	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
		}
	}
	
	public void onShow()
	{
		Log.d( "web" , "onShow" );
		//		if( mNewsViewCooee != null )
		//		{
		//			mNewsViewCooee.refreshNewsIfNotRresh( pluginContext );//刷新新闻放到滑动这页再开始刷新
		//		}
		//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
		if( config != null && config.getBoolean( FavoriteConfigString.getEnableDemoAdWhenOnShow() , FavoriteConfigString.isEnableDemoAdWhenOnShowDefaultValue() ) ){
			mFavoritesViewGroupsParent.onShow();
		}
		//fulijuan add end
	}
	
	public void onHide()
	{
		Log.d( "web" , "onHide" );
		//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
		if(config != null && config.getBoolean( FavoriteConfigString.getEnableDemoAdWhenOnShow() , FavoriteConfigString.isEnableDemoAdWhenOnShowDefaultValue() )){
			mFavoritesViewGroupsParent.onHide();
		}
		//fulijuan add end
	}
	
	public void bindApp(
			CopyOnWriteArrayList<AppInfo> data )
	{
		Log.v( "lvjiangbin" , "bindApp mFavoriteMainView=" + mFavoriteMainView );
		lastAppData = data;
		if( mFavoriteMainView == null )
		{
			return;
		}
		mFavoriteMainView.bindApp( data );
	}
	
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	public void bindNearby()
	{
		mFavoriteMainView.bindNearby();
	}
	
	//cheyingkun add end
	/**
	 * 从数据库中加载常用应用
	 * @param context
	 */
	public void loadFavoritesAppsFromDb(
			Context context )
	{
		synchronized( helper )
		{
			FavoritesAppData.clear();
			SQLiteDatabase db = helper.getReadableDatabase();
			String where = " launchTimes > 0 ";
			Cursor c = db.query( FavoritesHelper.TABLE_FAVORITES_APPS , null , where , null , null , null , null );
			try
			{
				final int idIndex = c.getColumnIndexOrThrow( FavoritesHelper.Favorites._ID );
				final int intentIndex = c.getColumnIndexOrThrow( FavoritesHelper.Favorites.INTENT );
				final int launchTimesIndex = c.getColumnIndexOrThrow( FavoritesHelper.Favorites.LAUNCH_TIMES );
				AppInfo appInfo;
				Intent intent;
				String intentDescription;
				long id;
				while( c.moveToNext() )
				{
					try
					{
						id = c.getLong( idIndex );
						intentDescription = c.getString( intentIndex );
						intent = Intent.parseUri( intentDescription , 0 );
						appInfo = FavoritesAppData.getApplicationInfoFromAll( intent.getComponent() );
						if( appInfo != null )
						{
							appInfo.setId( id );
							appInfo.launchTimes = c.getLong( launchTimesIndex );
							FavoritesAppData.add( appInfo );
						}
						else
						{
							deleteFavoritesFromDatabase( context , id );
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			}
			finally
			{
				if( c != null )
				{
					c.close();
					db.close();
				}
			}
		}
	}
	
	/**
	 * 保存常用的应用到数据库
	 * @param context
	 * @param apps
	 * @author for 常用应用（新功能） hp@2015/09/23 ADD START
	 */
	public void saveFavoritesToDatabase(
			Context context ,
			ArrayList<AppInfo> apps )
	{
		for( int i = 0 ; i < apps.size() ; i++ )
		{
			AppInfo appInfo = apps.get( i );
			if( appInfo.getId() == AppInfo.NO_ID )
			{
				addFavoritesToDatabase( context , appInfo );
			}
			else
			{
				updateFavoritesToDatabase( context , appInfo );
			}
		}
	}
	
	/**
	 * 把常用应用写入数据库中
	 * @param context
	 * @param appInfo
	 */
	public void addFavoritesToDatabase(
			Context context ,
			AppInfo appInfo )
	{
		synchronized( helper )
		{
			final ContentValues values = new ContentValues();
			SQLiteDatabase db = helper.getReadableDatabase();
			String titleStr = appInfo.getTitle() != null ? appInfo.getTitle().toString() : null;
			String uri = appInfo.getIntent() != null ? appInfo.getIntent().toUri( 0 ) : null;
			values.put( FavoritesHelper.Favorites.TITLE , titleStr );
			values.put( FavoritesHelper.Favorites.INTENT , uri );
			values.put( FavoritesHelper.Favorites.LAUNCH_TIMES , appInfo.launchTimes );
			long result = db.insert( FavoritesHelper.TABLE_FAVORITES_APPS , null , values );
			appInfo.setId( result );
			db.close();
		}
	}
	
	/**
	 * 更新数据库中常用应用
	 * @param context
	 * @param appInfo
	 */
	public void updateFavoritesToDatabase(
			Context context ,
			AppInfo appInfo )
	{
		synchronized( helper )
		{
			final ContentValues values = new ContentValues();
			SQLiteDatabase db = helper.getReadableDatabase();
			String titleStr = appInfo.getTitle() != null ? appInfo.getTitle().toString() : null;
			String uri = appInfo.getIntent() != null ? appInfo.getIntent().toUri( 0 ) : null;
			values.put( FavoritesHelper.Favorites.TITLE , titleStr );
			values.put( FavoritesHelper.Favorites.INTENT , uri );
			values.put( FavoritesHelper.Favorites.LAUNCH_TIMES , appInfo.launchTimes );
			db.update( FavoritesHelper.TABLE_FAVORITES_APPS , values , "_id=?" , new String[]{ appInfo.getId() + "" } );
			db.close();
		}
	}
	
	/**
	 * 从数据库中删除联系应用
	 * @param context
	 * @param id
	 */
	public void deleteFavoritesFromDatabase(
			Context context ,
			long id )
	{
		synchronized( helper )
		{
			SQLiteDatabase db = helper.getReadableDatabase();
			db.delete( FavoritesHelper.TABLE_FAVORITES_APPS , "_id=?" , new String[]{ id + "" } );
			db.close();
		}
	}
	
	/**
	 * 从数据库中加载广告
	 * @param context
	 */
	public void loadAndBindFavoritesAd(
			Context context )
	{
		synchronized( helper )
		{
			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( FavoritesManager.getInstance().getContainerContext() );
			if( !mSharedPreferences.getBoolean( KmobAdMessage.AD_GET_FIRST , true ) )
			{
				return;
			}
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor c = db.query( FavoritesHelper.TABLE_FAVORITES_AD , null , null , null , null , null , null );
			Log.v( "lvjiangbin" , "loadAndBindFavoritesAd  " );
			try
			{
				final int idIndex = c.getColumnIndexOrThrow( FavoritesHelper.Favorites._ID );
				final int adIdIndex = c.getColumnIndexOrThrow( FavoritesHelper.AD_PLACE_ID );
				while( c.moveToNext() )
				{
					int id = (int)c.getLong( idIndex );
					String adId = c.getString( adIdIndex );
					Log.v( "lvjiangbin" , "loadAndBindFavoritesAd id = " + id );
					Message message = new Message();
					message.what = KmobAdMessage.MSG_REFRESH_HAS_DATA;
					message.arg1 = id - 1;
					message.obj = adId;
					handler.sendMessage( message );
				}
			}
			finally
			{
				if( c != null )
				{
					c.close();
					db.close();
				}
			}
			return;
		}
	}
	
	public void reloadAndBindFavoritesAd()
	{
		synchronized( helper )
		{
			helper.checkPluginDateBase();//cheyingkun add	//解决“LauncherPhenix_V1.1.0.41895.20160516.apk之前版本，热更新最新版酷生活后，运营附近广告桌面重启”的问题【i_0014690】
			for( int i = 0 ; i < KmobAdMessage.MSG_FRESHSUGGESTION_COUNT ; i++ )
			{
				KmobAdMessage.getKmobMessage( FavoritesManager.getInstance().containerContext , queue , handler , i );
			}
		}
	}
	
	/**
	 * 广告数据加到数据库 zhengkai  add 
	 * @param context
	 * @param title
	 * @param impression_record_url
	 * @param app_package_name
	 * @param click_record_url
	 * @param yeahmobi_icon
	 */
	public void addFavoritesAdToDatabase(
			Context context ,
			String title ,
			String adPlaceId ,
			String adId ,
			Bitmap yeahmobi_icon ,
			String adData )
	{
		synchronized( helper )
		{
			final ContentValues values = new ContentValues();
			SQLiteDatabase db = helper.getReadableDatabase();
			values.put( FavoritesHelper.AD_TITLE , title );
			values.put( FavoritesHelper.AD_PLACE_ID , adPlaceId );
			values.put( FavoritesHelper.AD_ID , adId );
			byte[] data = Tools.bitmaptoByte( yeahmobi_icon );
			values.put( FavoritesHelper.AD_ICON , data );
			values.put( FavoritesHelper.AD_DATA , adData );
			long id_ = db.insert( FavoritesHelper.TABLE_FAVORITES_AD , null , values );
			db.close();
			Log.w( "KmobAdMessage" , "addFavoritesAdToDatabase id_ = : " + id_ );
		}
	}
	
	/**
	 * 广告数据加到数据库 zhengkai  add 
	 * @param context
	 * @param title
	 * @param impression_record_url
	 * @param app_package_name
	 * @param click_record_url
	 * @param yeahmobi_icon
	 */
	public void updateFavoritesAdToDatabase(
			Context context ,
			String title ,
			String adPlaceId ,
			String adId ,
			Bitmap yeahmobi_icon ,
			String adData )
	{
		synchronized( helper )
		{
			final ContentValues values = new ContentValues();
			SQLiteDatabase db = helper.getReadableDatabase();
			values.put( FavoritesHelper.AD_TITLE , title );
			values.put( FavoritesHelper.AD_PLACE_ID , adPlaceId );
			values.put( FavoritesHelper.AD_ID , adId );
			byte[] data = Tools.bitmaptoByte( yeahmobi_icon );
			values.put( FavoritesHelper.AD_ICON , data );
			values.put( FavoritesHelper.AD_DATA , adData );
			db.update( FavoritesHelper.TABLE_FAVORITES_AD , values , FavoritesHelper.AD_PLACE_ID + "=?" , new String[]{ adPlaceId } );
			db.close();
		}
	}
	
	/**
	 * 判断数据是否有数据  zhengkai  add 
	 * @param context
	 * @return
	 */
	public boolean isAdDatabaseExit(
			Context context ,
			String adPlaceId )
	{
		Cursor c = getAdDatabaseForAdId( adPlaceId );
		if( c.moveToFirst() )
		{
			c.close();
			return true;
		}
		else
		{
			c.close();
			return false;
		}
	}
	
	public Cursor getAdDatabaseForAdId(
			String adPlaceId )
	{
		synchronized( helper )
		{
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor c = db.query( FavoritesHelper.TABLE_FAVORITES_AD , null , FavoritesHelper.AD_PLACE_ID + "=?" , new String[]{ adPlaceId } , null , null , null );
			return c;
		}
	}
	
	/**
	 * 删除广告数据库       zhengkai  add 
	 * @param context
	 * @param id
	 */
	public void deleteFavoritesAdFromDatabase(
			Context context ,
			String adPlaceId )
	{
		synchronized( helper )
		{
			SQLiteDatabase db = helper.getReadableDatabase();
			db.delete( FavoritesHelper.TABLE_FAVORITES_AD , FavoritesHelper.AD_PLACE_ID + "=?" , new String[]{ adPlaceId } );
			db.close();
		}
	}
	
	public void loadAndBindApps(
			HashMap<ComponentName , Bitmap> hashmap )
	{
		FavoritesAppData.mApps.clear();
		FavoritesAppData.addAppsToAll( hashmap );
		FavoritesAppData.filterApps( containerContext );
		// Make sure the default favorites is loaded, if needed
		FavoritesAppManager.getInstance().loadDefaultFavoritesIfNecessary( FavoritesAppData.mApps );
		loadFavoritesAppsFromDb( containerContext );
		FavoritesAppManager.getInstance().bindFavoritesApps();
	}
	
	public void onThemeChanged(
			HashMap<ComponentName , Bitmap> hashmap )
	{
		// TODO Auto-generated method stub
		//cheyingkun add start	//解决“非原生桌面，卸载应用引起重启”的问题。【c_0004418】
		//检查常用应用和所有应用的info信息,如果传入的map中没有,则删除。否则bindFavoritesApps中会空指针
		checkInfo( hashmap , FavoritesAppData.mApps );
		checkInfo( hashmap , FavoritesAppData.datas );
		//cheyingkun add end
		FavoritesAppManager.getInstance().bindFavoritesApps();
	}
	
	//cheyingkun add start	//解决“非原生桌面，卸载应用引起重启”的问题。
	private void checkInfo(
			HashMap<ComponentName , Bitmap> hashmap ,
			CopyOnWriteArrayList<AppInfo> infoList )
	{
		if( hashmap == null || infoList == null )
		{
			return;
		}
		Bitmap bitmap = null;
		Set<ComponentName> keySet = hashmap.keySet();
		ArrayList<AppInfo> removeInfo = new ArrayList<AppInfo>();
		for( AppInfo info : infoList )
		{
			//判断map中是否有这一项,如果没有,说明是卸载掉的应用,需要同步删除列表
			if( keySet.contains( info.getComponentName() ) )
			{
				if( FavoritesAppManager.getInstance().getFavoritesGetDataCallBack() == null )//zhujieping,phenix换主题不重启,常用应用不显示并且常用区域没消失.
				{
					bitmap = hashmap.get( info.getComponentName() );
					if( bitmap != null && !bitmap.isRecycled() )
					{
						info.setIconBitmap( bitmap );
					}
					else
					{
						removeInfo.add( info );
					}
				}
				else
				{
					info.setIconBitmap( null );
				}
			}
			else
			{
				removeInfo.add( info );
			}
		}
		for( AppInfo appInfo : removeInfo )
		{
			infoList.remove( appInfo );
		}
	}
	
	//cheyingkun add end
	public void updateState(
			float progress )
	{
	}
	
	public void onResume(
			final List<ComponentName> componentList )
	{
		if( ( Build.VERSION.SDK_INT < 21 || isSystemApp( containerContext ) ) || componentList == null || componentList.size() == 0 )
		{
			return;
		}
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				for( ComponentName comp : componentList )
				{
					FavoritesAppData.updateTimes( comp );
				}
				if( FavoritesAppData.isNewAdd )
				{
					FavoritesAppManager.getInstance().updateFavoritesApps();
					FavoritesAppData.isNewAdd = false;
				}
				if( FavoritesAppData.isUpdate )
				{
					FavoritesAppManager.getInstance().updateFavoritesApps();
					FavoritesAppData.isUpdate = false;
				}
				componentList.clear();
			}
		} );
	}
	
	public void onUpdate()
	{
		// TODO Auto-generated method stub
	}
	
	public boolean isSystemApp(
			Context context )
	{
		if( mFlag == -1 )
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = null;
			try
			{
				packageInfo = packageManager.getPackageInfo( FavoritesManager.getInstance().getContainerContext().getPackageName() , 0 );
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( packageInfo != null )
			{
				mFlag = packageInfo.applicationInfo.flags;
			}
		}
		if( ( mFlag & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0 || ( mFlag & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
		{
			return true;
		}
		return false;
	}
	
	public void onDestroy()
	{
		if( receiver != null && containerContext != null )
		{
			containerContext.getApplicationContext().unregisterReceiver( receiver );
			receiver = null;
		}
	}
	
	public void newsNetworkChanged(
			int type )
	{
		if( mNewsViewCooee != null )
		{
			mNewsViewCooee.networkChanged( type );
		}
	}
	
	public void newsSourceChanged(
			int source )
	{
		if( mNewsViewCooee != null )
		{
			mNewsViewCooee.newsSourceChanged( source );
		}
	}
	
	public void adPlaceChanged(
			String place )
	{
		if( mNewsViewCooee != null )
		{
			mNewsViewCooee.adPlaceChanged( place );
		}
	}
	
	public void notifyNewsChange(
			//服务器通知界面变化
			boolean enable )
	{
		Log.v( "COOL" , "zjp = " + mNewsViewCooee + " " + view );
		if( ( enable && mNewsView == null ) || ( !enable && mNewsView != null ) )//新闻开关改变，上传log
		{
			JSONObject obj = new JSONObject();
			try
			{
				obj.put( "param1" , NEWS_STATE + ":" + enable );
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try
			{
				StatisticsExpandNew.onCustomEvent(
						containerContext ,
						OPERATE_SWITCH ,
						plugin.getSN() ,
						plugin.getAppID() ,
						CooeeSdk.cooeeGetCooeeId( containerContext ) ,
						plugin.getProductType() ,
						FavoritesPlugin.PluginPackageName ,
						FavoritesPlugin.UPLOAD_VERSION + "" ,
						obj );
			}
			catch( NoSuchMethodError e )
			{
				try
				{
					StatisticsExpandNew.onCustomEvent(
							containerContext ,
							OPERATE_SWITCH ,
							plugin.getSN() ,
							plugin.getAppID() ,
							CooeeSdk.cooeeGetCooeeId( containerContext ) ,
							plugin.getProductType() ,
							FavoritesPlugin.PluginPackageName ,
							obj );
				}
				catch( NoSuchMethodError e1 )
				{
					StatisticsExpandNew.onCustomEvent( containerContext , OPERATE_SWITCH , plugin.getProductType() , FavoritesPlugin.PluginPackageName , obj );
				}
			}
			HashMap<String , String> map = new HashMap<String , String>();
			map.put( NEWS_STATE , enable + "" );
			if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
			{
				MobclickAgent.onEvent( containerContext , OPERATE_SWITCH , map );
			}
			try
			{
				StatisticsExpandNew.onCustomEvent(
						FavoritesManager.getInstance().getContainerContext() ,
						OPERATE_SWITCH ,
						FavoritesPlugin.SN ,
						FavoritesPlugin.APPID ,
						CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
						FavoritesPlugin.PRODUCTTYPE ,
						FavoritesPlugin.PluginPackageName ,
						FavoritesPlugin.UPLOAD_VERSION + "" ,
						new JSONObject( map ) );
			}
			catch( NoSuchMethodError e )
			{
				try
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							OPERATE_SWITCH ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName ,
							new JSONObject( map ) );
				}
				catch( NoSuchMethodError e1 )
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							OPERATE_SWITCH ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName ,
							new JSONObject( map ) );
				}
			}
		}
		if( enable )
		{
			//打开新闻
			view.post( new Runnable() {
				
				@Override
				public void run()
				{
					//cheyingkun add start	//解决“服务器运营无法打开酷生活新闻”的问题。【i_0014380】
					//如果当前新闻已经是cooee新闻,直接返回
					if( mNewsView != null && mNewsView instanceof NewsView )
					{
						return;
					}
					//cheyingkun add end
					view.removeNewsView();
					mNewsView = null;
					mNewsViewCooee = new NewsView( pluginContext , containerContext );
					view.addNewsView( mNewsViewCooee );
					view.setStickCallback( mNewsViewCooee );
					mNewsView = mNewsViewCooee;
					if( mNewsViewCooee != null )
					{
						mNewsViewCooee.refreshNewsIfNotRresh( pluginContext );//开始刷新
					}
				}
			} );
		}
		else
		{
			if( mNewsView != null )
			{
				view.post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						view.removeNewsView();
						mNewsView = null;
						mNewsViewCooee = null;//cheyingkun add	//解决“运营搜狐新闻后,运营关闭新闻再打开,搜狐新闻不显示”的问题【i_0014442】
					}
				} );
			}
		}
	}
	
	public void notifyNearbyChange(
			boolean enable )
	{
		if( enable )
		{
			view.post( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					Log.v( "COOL" , "zjp notifyNearbyChange add " + mFavoriteMainView );
					if( mFavoriteMainView != null )
						mFavoriteMainView.addNearbyView();
					// zhangjin@2016/06/08 ADD START
					bindNearByLocal();
					// zhangjin@2016/06/08 ADD END
					//cheyingkun add start	//解决“服务器关闭附近，附近处显示空白”的问题【i_0014374】
					if( mFavoriteMainView != null )
					{
						mFavoriteMainView.notifyNearbyChange();
					}
					//cheyingkun add end
				}
			} );
		}
		else
		{
			view.post( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					Log.v( "COOL" , "zjp notifyNearbyChange remove " + mFavoriteMainView );
					if( mFavoriteMainView != null )
						mFavoriteMainView.removeNearbyView();
				}
			} );
		}
	}
	
	public void onIconSizeChanged(
			int changedSize )
	{
		if( mFavoriteMainView != null )
		{
			mFavoriteMainView.onIconSizeChanged( changedSize );
		}
		//cheyingkun add start	//酷生活支持动态修改图标大小
		if( view != null )
		{
			view.onIconSizeChanged( mFavoriteMainView , mNewsView );
		}
		//cheyingkun add end
	}
	
	public String getSn()
	{
		// TODO Auto-generated method stub
		return plugin.getSN();
	}
	
	public void expandFavorites()
	{
		if( view != null )
		{
			view.expandFavorites();
		}
	}
	
	//cheyingkun add start	//酷生活引导页
	private IFavoriteClings mIFavoriteClings;
	
	/**引导页接口,为了点击引导页按钮后,目前:桌面搜索栏同步做动画*/
	public IFavoriteClings getIFavoriteClings()
	{
		return mIFavoriteClings;
	}
	
	public void setIFavoriteClings(
			IFavoriteClings mIFavoriteClings )
	{
		this.mIFavoriteClings = mIFavoriteClings;
	}
	
	public void startFavoritesClingRemoveAnimation()
	{
		if( view != null )
		{
			mFavoritesViewGroupsParent.removeFavoritesClingsAnimation();
		}
	}
	
	/**
	 * 酷生活显示桌面搜索
	 * @return
	 */
	public boolean favoriteShowLauncherSearch()
	{
		boolean enableShowLauncherSearch = config.getBoolean( FavoriteConfigString.getEnableShowLauncherSearchKey() , FavoriteConfigString.isEnableShowLauncherSearchDefaultValue() );
		boolean enableShowFavoritesSearch = config.getBoolean( FavoriteConfigString.getEnableShowFavoritesSearchKey() , FavoriteConfigString.isEnableShowFavoritesSearchDefaultValue() );
		if( enableShowLauncherSearch && enableShowFavoritesSearch )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getStatusBarHeight()
	{
		int result = 0;
		int resourceId = containerContext.getResources().getIdentifier( "status_bar_height" , "dimen" , "android" );
		if( resourceId > 0 )
		{
			result = containerContext.getResources().getDimensionPixelSize( resourceId );
		}
		return result;
	}
	
	public boolean isShowFavoriteClings()
	{
		if( mFavoritesClingsView != null && mFavoritesClingsView.getParent() != null )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//修改酷生活S5引导页动画。
	public int getShowFavoritesViewDelayNum()
	{
		return showFavoritesViewDelayNum;
	}
	
	public void setShowFavoritesViewDelayNum(
			int showFavoritesViewDelayNum )
	{
		this.showFavoritesViewDelayNum = showFavoritesViewDelayNum;
	}
	
	public int getFavoritesViewAnimShowDelay()
	{
		return favoritesViewAnimShowDelay;
	}
	//cheyingkun add end
	;
	
	//cheyinkgun add start	//酷生活界面优化(常用应用未加载出来时,预留高度)
	public int getFirstLinearHeight()
	{
		float icon_text_space = pluginContext.getResources().getDimension( R.dimen.icon_text_space );
		int iconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
		return (int)( FavoriteMainView.ICON_PADDING_TOP_BOTTOM * 2 + Tools.getFontHeight( FavoriteMainView.ICON_TEXT_SIZE ) + icon_text_space + iconSize );
	}
	
	//cheyingkun add end
	//	private int successedCount = 0;
	private static String TAG = "FavoritesManager";
	private Handler handler = new Handler( Looper.getMainLooper() ) {
		
		public void handleMessage(
				Message msg )
		{
			int what = msg.what;
			switch( what )
			{
				case KmobAdMessage.MSG_DOWNLOAD_IMAG_SUCEESS:
					addDataToFavoriteAdvertisementGroup( msg.arg1 , (String)msg.obj );
					Log.w( TAG , "handleMessage 图片下载成功" + msg.arg1 );
					break;
				case KmobAdMessage.MSG_DOWNLOAD_IMAG_FAILURE:
					//					count++;
					//					Log.w( TAG , "handleMessage 图片下载失败" + count );
					break;
				case KmobAdMessage.MSG_REQUEST_DATA_FAILURE:
					break;
				case KmobAdMessage.MSG_REFRESH_HAS_DATA:
					addDataToFavoriteAdvertisementGroup( msg.arg1 , (String)msg.obj );
					Log.w( TAG , "handleMessage 有缓存好的数据直接加载" );
					break;
				default:
					break;
			}
		};
	};
	
	/**zhengkai @2015/11/7 add
	 * 从数据库取数据添加到热门推荐中
	 */
	private void addDataToFavoriteAdvertisementGroup(
			int index ,
			String adid )
	{
		AppInfo info = new AppInfo();
		Cursor cursor = FavoritesManager.getInstance().getAdDatabaseForAdId( adid );
		int titleIndex = cursor.getColumnIndex( FavoritesHelper.AD_TITLE );
		int adPlaceIdIndex = cursor.getColumnIndex( FavoritesHelper.AD_PLACE_ID );
		int iconIndex = cursor.getColumnIndex( FavoritesHelper.AD_ICON );
		int adIdIndex = cursor.getColumnIndex( FavoritesHelper.AD_ID );
		int adDataIndex = cursor.getColumnIndex( FavoritesHelper.AD_DATA );
		while( cursor.moveToNext() )
		{
			String title = cursor.getString( titleIndex );
			String adPlaceId = cursor.getString( adPlaceIdIndex );
			String adId = cursor.getString( adIdIndex );
			String adData = cursor.getString( adDataIndex );
			byte[] data = cursor.getBlob( iconIndex );
			Bitmap yeahmobiIcon = BitmapFactory.decodeByteArray( data , 0 , data.length );
			info.setIconBitmap( yeahmobiIcon );
			info.setAdType( AppInfo.KMOB_AD );
			info.SetAdId( adId );
			info.setAdPlaceId( adPlaceId );
			info.setTitle( title );
			info.setAdData( adData );
		}
		//防止内存泄漏
		if( !cursor.isClosed() )
			cursor.close();
		mFavoriteMainView.bindAdData( info , index );
	}
	
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	public void clearFavoritesView()
	{
		if( mFavoriteMainView != null )
		{
			mFavoriteMainView.clearFavoritesView();
		}
		if( view != null )
		{
			view.removeAllViews();
		}
		if( receiver != null && containerContext != null )
		{
			containerContext.getApplicationContext().unregisterReceiver( receiver );
			receiver = null;
		}
	}
	//cheyingkun add end
	;
	
	// zhangjin@2016/06/08 ADD START
	public void bindNearByLocal()
	{
		try
		{
			JSONObject configJson = null;
			Log.d( "MM" , "bindNearByLocal " + pluginContext );
			String basepath = config.getString( FavoriteConfigString.getNearbyLocalPathKey() , FavoriteConfigString.getNearbyLocalPathDefaultValue() );
			configJson = getConfigFromFile( pluginContext , "config_favorite_local.ini" );
			JSONArray locals = configJson.getJSONArray( FAVORITES_NEARBY_LOCAL );
			String lan = Locale.getDefault().getLanguage();
			int curLanguage = 0;
			if( lan.equals( "zh" ) )
			{
				lan = Locale.getDefault().toString();
				if( lan.equals( "zh_TW" ) )
					curLanguage = 1;
				else
					curLanguage = 0;
			}
			else
			{
				curLanguage = 2;
			}
			for( int i = 0 ; i < locals.length() ; i++ )
			{
				JSONObject temp = locals.getJSONObject( i );
				try
				{
					String url = temp.getString( FAVORITES_NEARBY_LOCAL_ITEM_URL );
					String cmp = temp.getString( FAVORITES_NEARBY_LOCAL_ITEM_CMP );
					String title = temp.getString( FAVORITES_NEARBY_LOCAL_ITEM_TITLE );
					String titleCN = temp.getString( FAVORITES_NEARBY_LOCAL_ITEM_TITLE_CN );
					String titleTW = temp.getString( FAVORITES_NEARBY_LOCAL_ITEM_TITLE_TW );
					String img = temp.getString( FAVORITES_NEARBY_LOCAL_ITEM_IMG );
					NearByItem item = new NearByItem();
					item.setUrl( url );
					item.setCmp( cmp );
					if( curLanguage == 0 )
					{
						item.setTitle( titleCN );
					}
					else if( curLanguage == 1 )
					{
						item.setTitle( titleTW );
					}
					else if( curLanguage == 2 )
					{
						item.setTitle( title );
					}
					else
					{
						item.setTitle( title );
						//
					}
					item.setBmp( BitmapFactory.decodeFile( basepath + img ) );
					ArrayList<String> extraList = new ArrayList<String>();
					for( int index = 1 ; ; index++ )
					{
						if( temp.has( FAVORITES_NEARBY_LOCAL_ITEM_EXTRA + index ) )
						{
							extraList.add( temp.getString( FAVORITES_NEARBY_LOCAL_ITEM_EXTRA + index ) );
						}
						else
						{
							break;
						}
					}
					item.setExtra( extraList );
					mFavoriteMainView.bindNearByLocal( item , i );
				}
				catch( Exception JSONException )
				{
				}
			}
		}
		catch( Exception e )
		{
		}
	}
	
	private JSONObject getConfigFromFile(
			Context context ,
			String fileName )
	{
		String configfile = config.getString( FavoriteConfigString.getNearbyLocalPathKey() , FavoriteConfigString.getNearbyLocalPathDefaultValue() );
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream( new File( configfile + File.separator + fileName ) );
			String config = readTextFile( inputStream );
			JSONObject jObject;
			try
			{
				jObject = new JSONObject( config );
				return jObject;
			}
			catch( JSONException e1 )
			{
				e1.printStackTrace();
			}
		}
		catch( IOException e )
		{
			Log.e( "tag" , e.getMessage() );
		}
		return null;
	}
	
	private String readTextFile(
			InputStream inputStream )
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		int len;
		try
		{
			while( ( len = inputStream.read( buf ) ) != -1 )
			{
				outputStream.write( buf , 0 , len );
			}
			outputStream.close();
			inputStream.close();
		}
		catch( IOException e )
		{
		}
		return outputStream.toString();
	}
	// zhangjin@2016/06/08 ADD END
	;
	
	//cheyingkun add start	//新闻全屏模式时添加删除联系人(应用),不改变新闻y值
	public boolean isNewsExpandedMode()
	{
		return view == null ? false : view.isNewsExpandedMode();
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//解决“本地配置搜狐新闻，服务器配置COOEE新闻，获取服务器数据后，新闻全屏无法下滑到主页”的问题。【i_0014085】
	private boolean isEnableNews()
	{
		if( config != null && config.getBoolean( FavoriteConfigString.getEnableNewsKey() , FavoriteConfigString.isEnableNewsDefaultValue() ) )
		{
			return true;
		}
		return false;
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//酷生活支持动态修改图标大小
	public float getHeaderViewHeight()
	{
		return view.getHeaderViewHeight();
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	public void updateFavoritesAppsIcon(
			List<ComponentName> componentName ,
			List<Bitmap> bitmap )
	{
		if( componentName == null || bitmap == null )
		{
			Log.e( "" , "cyk FavoritesManager updateFavoritesAppsIcon return " );
			return;
		}
		HashMap<ComponentName , Bitmap> hashmap = new HashMap<ComponentName , Bitmap>();
		for( int i = 0 ; i < componentName.size() ; i++ )
		{
			hashmap.put( componentName.get( i ) , bitmap.get( i ) );
		}
		//更新数据
		FavoritesAppData.updateAppIcon( hashmap );
	}
	//cheyingkun add end
	;
	
	public boolean isNearbyShow(
			Context context )
	{
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )//老人桌面不显示附近
		{
			return false;
		}
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableNearbyKey() , FavoriteConfigString.isEnableNearbyDefaultValue() ) )
		{
			return true;
		}
		String control = FavoriteControlHelper.getInstance( context ).getString( FavoriteControlHelper.ENABLE_NEARBY , null );
		if( control != null && control.equals( "true" ) )
		{
			return true;
		}
		return false;
	}
	
	public int getFavoritesState()
	{
		if( view != null )
		{
			if( view.isNewsCollapseMode() )
			{
				return FAVORITES_STATE_COOEE_NEWS_COLLAPSE;
			}
			else if( view.isNewsExpandedMode() )
			{
				return FAVORITES_STATE_COOEE_NEWS_EXPANDED;
			}
		}
		return FAVORITES_STATE_ERROR;
	}
	
	//cheyinkgun add start	//酷生活适配VERSION==6的版本(该版本之前,没有搜狐新闻)
	/**
	 * 
	 * 当前版本是否大于传入的版本值
	 * @return
	 */
	public boolean isMoreThanTheVersion(
			int hostVersion ,
			int pluginVersion )
	{
		int lastHostVersion = FavoriteConfigString.getHostVersionCodeValue();
		int lastPluginVersion = 0;
		//		Log.d( TAG , " cyk isMoreThanTheVersion hostVersion: " + hostVersion + " pluginVersion: " + pluginVersion );
		if( config != null )
		{
			//获取当前host版本(config是桌面传入的)
			lastHostVersion = config.getInt( FavoriteConfigString.getHostVersionCodeKey() , FavoriteConfigString.getHostVersionCodeValue() );
			//			Log.d( TAG , " cyk isMoreThanTheVersion lastHostVersion: " + lastHostVersion );
			if( lastHostVersion <= 0//host版本桌面没有传过来
					|| hostVersion < 0//host版本无法区分时,想用酷生活版本区分一下逻辑(比如修改了FavoriteConfigString中的key)
			)
			{
				//host版本没有传入,则根据plugin的版本进行判断
				if( plugin != null )
				{
					//获取安装桌面时,桌面assets下面酷生活的版本
					//(因为我们编译是先编酷生活,再编译桌面.这里使用桌面当前版本号代替,暂时不支持第三方桌面)
					try
					{
						PackageManager manager = containerContext.getPackageManager();
						PackageInfo info = manager.getPackageInfo( containerContext.getPackageName() , 0 );
						lastPluginVersion = info.versionCode;
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
					//					Log.d( TAG , " cyk isMoreThanTheVersion lastPluginVersion: " + lastPluginVersion );
					if( lastPluginVersion >= pluginVersion )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
			else
			{
				if( lastHostVersion >= hostVersion )
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	//cheyinkgun add end
}
