package cool.sdk.search;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.common.CoolMethod;


public class SearchActivityManager
{
	
	private static final String TAG = "SearchActivityManager";
	private static final String KEY_ENABLE_HAOSOU = "enable_haosou";
	private static final String KEY_ENABLE_USE_EXPLORER_DEFAULT = "KUSO_SWITCH_C3";
	private static final String KEY_ENABLE_SHOW_OPERATE_PAGE_DEFAULT = "KUSO_SWITCH_C4";
	private static final String KEY_EXPLORER_PACKAGE_NAME = "KUSO_EXPLORER_PACKAGE_NAME";
	private static final String KEY_EXPLORER_CLASS_NAME = "KUSO_EXPLORER_CLASS_NAME";
	private static SearchActivityManager mInstance = null;
	private static Context mContext = null;
	//	private static boolean hasInitSoloSdk = false;
	private static String hostPackageName = null;
	private static ComponentName systemSearchComponentName = null;
	private static String defaultExplorerPkgName = null;
	private static String defaultExplorerClsName = null;
	
	private SearchActivityManager(
			Context context )
	{
		mInstance = this;
		mContext = context;
	}
	
	public static SearchActivityManager getInstance(
			Context context )
	{
		if( mInstance == null )
			mInstance = new SearchActivityManager( context );
		return mInstance;
	}
	
	/**
	 * 将本地默认配置传给搜索
	 * @param context
	 * @param enableCommonPageSearchDefault
	 * @param enableFavoritesPageSearchDefault
	 * @param enableCooeeSerachDefault
	 * @param enableSoloDefault
	 * @param enableHaosou
	 */
	public static void initSearchConfig(
			boolean enableCommonPageSearchDefault ,
			boolean enableFavoritesPageSearchDefault ,
			boolean enableCooeeSerachDefault ,
			boolean enableSoloDefault ,
			boolean enableHaosou )
	{
		Log.v(
				TAG ,
				"initSearchConfig() defCommon:" + enableCommonPageSearchDefault + " defFavorites:" + enableFavoritesPageSearchDefault + " defCooee:" + enableCooeeSerachDefault + " defSolo:" + enableSoloDefault + " haosou:" + enableHaosou );
		SearchConfig.SWITCH_ENABLE_COMMON_PAGE_SEARCH_DEFAULT = enableCommonPageSearchDefault;
		SearchConfig.SWITCH_ENABLE_FAVORITES_PAGE_SEARCH_DEFAULT = enableFavoritesPageSearchDefault;
		SearchConfig.SWITCH_ENABLE_COOEE_SEARCH_DEFAULT = enableCooeeSerachDefault;
		SearchConfig.SWITCH_ENABLE_SOLO_DEFAULT = enableSoloDefault;
		SearchConfig.SWITCH_ENABLE_HAOSOU = enableHaosou;
	}
	
	/**
	 * 设置使用kuso时打开搜索结果的方式（未运营时）
	 * @param useExplorer true:浏览器 false:webview
	 */
	public static void setEnableUseExplorerDefault(
			boolean useExplorer )
	{
		SearchConfig.SWITCH_ENABLE_USE_EXPLORER_DEFAULT = useExplorer;
	}
	
	/**
	 * 设置使用kuso时是否显示运营页（未运营时）
	 * @param showOperatePage true:显示 false:不显示
	 */
	public static void setEnableShowOperatePageDefault(
			boolean showOperatePage )
	{
		SearchConfig.SWITCH_ENABLE_SHOW_OPERATE_PAGE_DEFAULT = showOperatePage;
	}
	
	public void startSearchActivity(
			final Context context ,
			String initialQuery ,
			boolean selectInitialQuery ,
			Bundle appSearchData ,
			Rect sourceBounds )
	{
		Log.v( TAG , "startSearchActivity mContext:" + mContext.getPackageName() + " context:" + context.getPackageName() );
		ComponentName systemSearchActivity = null;
		//如果不使用我们的搜索，则使用系统搜索
		if( !SearchHelper.getInstance( mContext ).enableCooeeSearch() )
		{
			if( systemSearchComponentName != null )
			{
				systemSearchActivity = systemSearchComponentName;
			}
			else
			{
				final SearchManager searchManager = (SearchManager)mContext.getSystemService( Context.SEARCH_SERVICE );
				if( VERSION.SDK_INT >= 16 )
				{
					systemSearchActivity = searchManager.getGlobalSearchActivity();
				}
				else
				{
					List<SearchableInfo> list = searchManager.getSearchablesInGlobalSearch();
					if( list != null && list.size() > 0 )
					{
						systemSearchActivity = list.get( 0 ).getSearchActivity();
					}
				}
			}
		}
		if( systemSearchActivity != null )
		{
			Log.v( TAG , "startSearchActivity() system" );
			if( systemSearchComponentName != null )
			{
				Log.v( TAG , "startSearchActivity() systemSearchComponentName:" + systemSearchComponentName.toString() );
				Intent intent = new Intent();
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				intent.setComponent( systemSearchComponentName );
				mContext.startActivity( intent );
			}
			else
			{
				startSystemSearch( mContext , systemSearchActivity , initialQuery , selectInitialQuery , appSearchData , sourceBounds );
			}
			MobclickAgent.onEvent( context , "open_system_search" );
		}
		else
		//如果需要使用我们的搜索，或找不到系统搜索，则使用我们的搜索
		{
			// jubingcheng@2016/05/24 DEL START 运营需求 永远不使用SOLO
			//if( SearchHelper.getInstance( mContext ).enableSoloSearch() && !isNativeEnvironment( mContext ) )
			//{
			//	//solo
			//	Log.v( TAG , "startSearchActivity() solo hasInitSoloSdk:" + hasInitSoloSdk );
			//	if( mContext.getPackageName().equals( context.getPackageName() ) )
			//	{
			//		if( hasInitSoloSdk )
			//		{
			//			SoloSearch.launchNewsFeed( context );
			//		}
			//		else
			//		{
			//			Handler h = new Handler( Looper.getMainLooper() );
			//			h.post( new Runnable() {
			//				
			//				@Override
			//				public void run()
			//				{
			//					// TODO Auto-generated method stub								
			//					initSoloSearchSdk( context );//初始化必须放在主线程执行
			//					SoloSearch.launchNewsFeed( context );
			//				}
			//			} );
			//		}
			//	}
			//	else
			//	//custom
			//	{
			//		Intent intent = new Intent();
			//		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			//		intent.setComponent( new ComponentName( context.getPackageName() , "com.solo.search.SearchActivity" ) );
			//		intent.setFlags( 268435456 );
			//		mContext.startActivity( intent );
			//	}
			//	MobclickAgent.onEvent( context , "open_solo_search" );
			//}
			//else
			// jubingcheng@2016/05/24 DEL END
			{
				//kuso
				Log.v( TAG , "startSearchActivity() kuso" );
				Intent intent = new Intent();
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				intent.setComponent( new ComponentName( context.getPackageName() , "com.search.kuso.SearchT9Main" ) );
				intent.putExtra( KEY_ENABLE_HAOSOU , SearchConfig.SWITCH_ENABLE_HAOSOU );
				intent.putExtra( KEY_ENABLE_USE_EXPLORER_DEFAULT , SearchConfig.SWITCH_ENABLE_USE_EXPLORER_DEFAULT );
				intent.putExtra( KEY_ENABLE_SHOW_OPERATE_PAGE_DEFAULT , SearchConfig.SWITCH_ENABLE_SHOW_OPERATE_PAGE_DEFAULT );
				intent.putExtra( KEY_EXPLORER_PACKAGE_NAME , defaultExplorerPkgName );
				intent.putExtra( KEY_EXPLORER_CLASS_NAME , defaultExplorerClsName );
				context.startActivity( intent );
				MobclickAgent.onEvent( context , "open_kuso_search" );
			}
			// gaominghui@2016/04/14 ADD START 添加olap激活，使用统计
			try
			{
				olapStatistics( context );
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// gaominghui@2016/04/14 ADD END 添加olap激活，使用统计
		}
		MobclickAgent.onEvent( context , "search_click" );
	}
	
	/**
	 * 设置系统搜索的包类名
	 * @param pkgName
	 * @param clsName
	 */
	public static void setSystemSearchComponentName(
			String pkgName ,
			String clsName )
	{
		Log.v( TAG , "setSystemSearchComponentName pkgName:" + pkgName + " clsName:" + clsName );
		if( pkgName == null || pkgName.isEmpty() || clsName == null || clsName.isEmpty() )
		{
			systemSearchComponentName = null;
		}
		else
		{
			systemSearchComponentName = new ComponentName( pkgName , clsName );
		}
	}
	
	/**
	 * 设置默认浏览器的包类名（使用kuso并且用浏览器方式打开时起作用）
	 * @param pkgName
	 * @param clsName
	 */
	public static void setDefaultExplorer(
			String pkgName ,
			String clsName )
	{
		Log.v( TAG , "setDefaultExplorer pkgName:" + pkgName + " clsName:" + clsName );
		if( pkgName == null || pkgName.isEmpty() )
		{
			defaultExplorerPkgName = null;
			defaultExplorerClsName = null;
			return;
		}
		else
		{
			defaultExplorerPkgName = pkgName;
		}
		if( clsName == null || clsName.isEmpty() )
		{
			defaultExplorerClsName = null;
		}
		else
		{
			defaultExplorerClsName = clsName;
		}
	}
	
	// gaominghui@2016/04/15 ADD START
	// gaominghui@2016/04/14 ADD START 添加olap激活和使用的统计
	private static final String CONFIG_FILE_NAME = "config.ini";
	
	/**
	 *olap统计
	 * @throws JSONException
	 * @throws NameNotFoundException
	 * @author gaominghui 2016年4月14日
	 */
	private void olapStatistics(
			Context context ) throws JSONException , NameNotFoundException
	{
		Log.v( TAG , "olapStatistics() context:" + context.getPackageName() + " host:" + hostPackageName );
		if( hostPackageName == null )
		{
			setHostPackageName( context.getPackageName() );
		}
		SharedPreferences prefs = context.getSharedPreferences( "kuso_search" , Activity.MODE_PRIVATE );
		JSONObject tmp = getAssets( context );
		String appid = null;
		String sn = null;
		if( tmp != null )
		{
			appid = tmp.getString( "app_id" );
			sn = tmp.getString( "serialno" );
		}
		//StatisticsExpandNew.setStatiisticsLogEnable( true );
		int versionCode = context.getPackageManager().getPackageInfo( context.getPackageName() , 0 ).versionCode;
		final String append = hostPackageName;
		if( prefs.getBoolean( "first_run" , true ) )
		{
			StatisticsExpandNew.register( context , sn , appid , CooeeSdk.cooeeGetCooeeId( context ) , 4 , "com.cooee.search" , "" + versionCode , append );
			prefs.edit().putBoolean( "first_run" , false ).apply();
		}
		else
		{
			StatisticsExpandNew.use( context , sn , appid , CooeeSdk.cooeeGetCooeeId( context ) , 4 , "com.cooee.search" , "" + versionCode , append );
		}
	}
	
	private static JSONObject getAssets(
			Context context )
	{
		JSONObject config = null;
		try
		{
			AssetManager assetManager = context.getAssets();
			InputStream inputStream = assetManager.open( CONFIG_FILE_NAME );
			String text = readTextFile( inputStream );
			JSONObject jObject = new JSONObject( text );
			config = new JSONObject( jObject.getString( "config" ) );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}
	
	private static String readTextFile(
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
	
	// gaominghui@2016/04/15 ADD END添加olap激活和使用的统计
	/**
	 * 初始化solo搜索SDK（20160524运营需求：无效）
	 * @param context
	 */
	private static void initSoloSearchSdk(
			Context context )
	{
		//		Log.v( TAG , "initSoloSearchSdk() hasInitSoloSdk:" + hasInitSoloSdk );
		//		if( hasInitSoloSdk )
		//			return;
		//		SoloSearch.initialize( context , "1186" ); //solo搜索SDK初始化
		//		hasInitSoloSdk = true;
	}
	
	/**
	 * 启动系统的搜索
	 * @param context
	 * @param systemSearchActivity
	 * @param initialQuery
	 * @param selectInitialQuery
	 * @param appSearchData
	 * @param sourceBounds
	 */
	private static void startSystemSearch(
			Context context ,
			ComponentName systemSearchActivity ,
			String initialQuery ,
			boolean selectInitialQuery ,
			Bundle appSearchData ,
			Rect sourceBounds )
	{
		Intent intent = new Intent( SearchManager.INTENT_ACTION_GLOBAL_SEARCH );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		intent.setComponent( systemSearchActivity );
		// Make sure that we have a Bundle to put source in
		if( appSearchData == null )
		{
			appSearchData = new Bundle();
		}
		else
		{
			appSearchData = new Bundle( appSearchData );
		}
		// Set source to package name of app that starts global search, if not set already.
		if( !appSearchData.containsKey( "source" ) )
		{
			appSearchData.putString( "source" , context.getPackageName() );
		}
		intent.putExtra( SearchManager.APP_DATA , appSearchData );
		if( !TextUtils.isEmpty( initialQuery ) )
		{
			intent.putExtra( SearchManager.QUERY , initialQuery );
		}
		if( selectInitialQuery )
		{
			intent.putExtra( SearchManager.EXTRA_SELECT_QUERY , selectInitialQuery );
		}
		intent.setSourceBounds( sourceBounds );
		try
		{
			context.startActivity( intent );
		}
		catch( ActivityNotFoundException ex )
		{
			Log.e( TAG , "startSystemSearch() activity not found: " + systemSearchActivity + ":" + ex.toString() );
		}
	}
	
	/**
	 * 是否国内环境
	 * @param context
	 * @return true:国内环境 false:海外环境
	 */
	private static boolean isNativeEnvironment(
			Context context )
	{
		String curLan = Locale.getDefault().toString();
		String imsi = CoolMethod.getImsi( context );
		if( curLan.equals( "zh_CN" ) || curLan.equals( "zh_TW" ) || curLan.equals( "zh_HK" ) || imsi.startsWith( "460" ) )
		{
			Log.v( TAG , "isNativeEnvironment() return true" );
			return true;
		}
		Log.v( TAG , "isNativeEnvironment() return false" );
		return false;
	}
	
	public static StartSearchListener mStartSearchListener;
	
	public static void setStartSearchListener(
			StartSearchListener startSearchListener )
	{
		mStartSearchListener = startSearchListener;
	}
	
	public interface StartSearchListener
	{
		
		public void onStartSearch();
	}
	
	/**
	 *功能：设置调用搜索的宿主包名
	 * @auther gaominghui  2016年4月18日
	 */
	public void setHostPackageName(
			String packageName )
	{
		// TODO Auto-generated method stub
		Log.v( TAG , "setHostPackageName() packageName:" + packageName );
		hostPackageName = packageName;
	}
}
