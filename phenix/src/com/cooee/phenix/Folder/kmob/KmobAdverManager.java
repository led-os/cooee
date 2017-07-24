package com.cooee.phenix.Folder.kmob;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.cooee.framework.function.OperateExpandData.OperateNativeData;
import com.cooee.framework.function.OperateExpandData.OperateNativeData.IOperateNativeDataCallbacks;
import com.cooee.framework.utils.LauncherConfigUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.DynamicGrid;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.util.Tools;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.kmob.kmobsdk.NativeAdData;

import cool.sdk.download.manager.DlMethod;


// cheyingkun add start //合入79App插屏广告
public class KmobAdverManager//
implements IOperateNativeDataCallbacks//cheyingkun add	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
{
	
	private static KmobAdverManager mKmobAdverManager;
	private static String TAG = "KmobAdverManager";
	private AdBaseView mIntersititalView = null;
	private Context mContext;
	/**是否显示log信息*/
	private boolean DEBUG = true;
	//
	//
	//79App
	private SharedPreferences prefs_79App;
	/**插屏广告SharePreference名称(79App)*/
	private final String intersititalAdverSharedPreferencesName = "IntersititalAdver79App";
	/**插屏广告上一天初始化的时间(79App)*/
	private final String intersititalAdverLastDayTime79App = "intersititalAdverLastDayTime79App";
	/**插屏广告上次初始化的时间(79App)*/
	private final String intersititalAdverLastTime79App = "intersititalAdverLastTime79App";
	/**插屏广告初始化次数(79App)*/
	private final String intersititalAdverShowTimes79App = "intersititalAdverShowTimes79App";
	//
	//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
	//文件夹推荐应用
	private SharedPreferences prefs_native;
	/**原生广告SharePreference名称(79App)*/
	private final String nativeAdverSharedPreferencesName = "NativeAdver";
	/**文件夹推荐应用开关字符串*/
	private final String OPERATE_NATIVE_DATA_SWITCH = "operateNativeDataSwitch";
	/**文件夹推荐应用刷新间隔字符串*/
	private final String REFRESH_INTERVAL_OPERATE_NATIVE = "refreshInterval";
	/**文件夹推荐应用是否wifi下更新字符串*/
	private final String WIFI_ONLY_OPERATE_NATIVE = "wifiOnly";
	//
	/**是否开启文件夹推荐应用,默认true*/
	private boolean switch_enable_native_data_for_folder = true;
	/**默认刷新的时间间隔,单位毫秒*/
	private long refreshIntervalOperateNative = 24 * 60 * 60 * 1000;
	/**是否wifi更新,默认true*/
	private boolean wifiOnlyOperateNative = true;
	/**上次刷新时间的毫秒数*/
	private Long lastRefreshTimeOperateNative = (long)0;
	//cheyingkun add end
	/**插屏广告已经显示过*/
	private boolean mIntersititalViewIsOld = false;//cheyingkun add	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	// zhangjin@2016/03/29 ADD START
	private String mSn = "";
	
	// zhangjin@2016/03/29 ADD END
	private KmobAdverManager(
			Context mContext )
	{
		this.mContext = mContext;
	}
	
	public static KmobAdverManager getKmobAdverManager(
			Context mContext )
	{
		if( mKmobAdverManager == null && mContext != null )
		{
			synchronized( TAG )
			{
				if( mKmobAdverManager == null && mContext != null )
				{
					mKmobAdverManager = new KmobAdverManager( mContext );
					OperateNativeData.setCallbacks( mKmobAdverManager );//cheyingkun add	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
				}
			}
		}
		return mKmobAdverManager;
	}
	
	/*************************插屏广告 start*****************************/
	// zhangjin@2016/05/03 DEL START
	///**
	// * 初始化插屏广告
	// */
	//public void initIntersititalAdver()
	//{
	//	//cheyingkun add start	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//	init79AppTimeData();
	//	boolean canInitIntersititalAdver79App = canInitIntersititalAdver79App();
	//	Log.i( "intersititalAdve" , " Launcher onPause canInitIntersititalAdver79App : " + canInitIntersititalAdver79App );
	//	if( !canInitIntersititalAdver79App )
	//	{
	//		return;
	//	}
	//	//cheyingkun add end	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//	if( mContext == null )
	//	{
	//		return;
	//	}
	//	//cheyingkun start	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//	//cheyingkun del start
	//	//		KmobManager.setAdSrcId( KmobManager.ADSRC_SEVENNINEAPP );
	//	//		KmobManager.setPublisherId( "4472" , KmobManager.ADSRC_SEVENNINEAPP , mContext );//如果使用79App广告资源，请传递该id，改为4472.
	//	//cheyingkun del end
	//	String adPlaceId = mContext.getResources().getString( R.string.intersitital_adver_place_id );//cheyingkun add
	//	//cheyingkun end
	//	//1.生成插屏广告
	//	// zhangjin@2016/03/29 ADD START
	//	KmobManager.setChannel( getSn() );
	//	// zhangjin@2016/03/29 ADD END
	//	mIntersititalView = KmobManager.createIntersitital( adPlaceId , mContext );
	//	//2.添加监听器
	//	mIntersititalView.addAdViewListener( new AdViewListener() {
	//		
	//		@Override
	//		public void onAdShow(
	//				String info )
	//		{
	//			Log.d( "intersititalAdve" , " initIntersititalAdver AdViewListener onAdShow info " + info );
	//			mIntersititalViewIsOld = true;//cheyingkun add	//解决“进入第三方应用，按home键返回桌面显示广告，再次进入后返回，广告多次显示”的问题。【i_0013280】
	//		}
	//		
	//		@Override
	//		public void onAdReady(
	//				String space_id )
	//		{
	//			Log.d( "intersititalAdve" , " initIntersititalAdver AdViewListener onAdReady space_id " + space_id );
	//			mIntersititalViewIsOld = false;//cheyingkun add	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//		}
	//		
	//		@Override
	//		public void onAdFailed(
	//				String reason )
	//		{
	//			Log.d( "intersititalAdve" , " initIntersititalAdver AdViewListener onAdFailed reason " + reason );
	//			mIntersititalViewIsOld = false;//cheyingkun add	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//		}
	//		
	//		@Override
	//		public void onAdClick(
	//				String arg0 )
	//		{
	//			Log.d( "intersititalAdve" , " initIntersititalAdver AdViewListener onAdClick arg0 " + arg0 );
	//		}
	//		
	//		@Override
	//		public void onAdClose(
	//				String info )
	//		{
	//			Log.d( "intersititalAdve" , " initIntersititalAdver AdViewListener onAdClose arg0 " + info );
	//			mIntersititalViewIsOld = true;//cheyingkun add	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//		}
	//		
	//		@Override
	//		public void onAdCancel(
	//				String info )
	//		{
	//			Log.d( "intersititalAdve" , " initIntersititalAdver AdViewListener onAdCancel arg0 " + info );
	//		}
	//	} );
	//}
	//
	///**
	// * 显示插屏广告
	// */
	//public void showIntersititalAdver()
	//{
	//	if( mIntersititalView != null && mIntersititalView.isAdReady()//
	//			&& !mIntersititalViewIsOld )//cheyingkun add	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//	{
	//		Log.i( "intersititalAdve" , " KmobAdverManager showIntersititalAdver " );
	//		//显示广告之前设置广告资源类型
	//		//				KmobManager.setAdSrcId( KmobManager.ADSRC_SEVENNINEAPP );//cheyingkun del	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
	//		mIntersititalView.showAd();//3.在图片缓存好以后就可以调用显示接口
	//	}
	//}
	//
	///**
	// * 初始化插屏广告时间数据
	// */
	//private void init79AppTimeData()
	//{
	//	if( mContext != null )
	//	{
	//		prefs_79App = mContext.getSharedPreferences( intersititalAdverSharedPreferencesName , Activity.MODE_PRIVATE );
	//		long longInitTime = prefs_79App.getLong( intersititalAdverLastDayTime79App , -1 );
	//		if( longInitTime == -1 )
	//		{
	//			Editor edit = prefs_79App.edit();
	//			long time = System.currentTimeMillis();
	//			edit.putLong( intersititalAdverLastDayTime79App , time );
	//			edit.putLong( intersititalAdverLastTime79App , 0 );
	//			edit.putInt( intersititalAdverShowTimes79App , 0 );
	//			edit.commit();
	//		}
	//	}
	//}
	//
	///**
	// * 根据时间判断是否可以请求广告
	// * @return
	// */
	//private boolean canInitIntersititalAdver79App()
	//{
	//	if( prefs_79App != null )
	//	{
	//		Editor edit = prefs_79App.edit();
	//		long time = System.currentTimeMillis();//系统时间
	//		long laseDayTime = prefs_79App.getLong( intersititalAdverLastDayTime79App , 0 );//上一天的时间
	//		long laseTimeTime = prefs_79App.getLong( intersititalAdverLastTime79App , 0 );//上一次的时间
	//		int times = prefs_79App.getInt( intersititalAdverShowTimes79App , 0 );//显示次数
	//		//如果时间大于一天
	//		if( Math.abs( time - laseDayTime ) >= 24 * 60 * 60 * 1000 )
	//		{
	//			times = 1;
	//			laseDayTime = time;
	//			laseTimeTime = time;
	//			edit.putLong( intersititalAdverLastDayTime79App , laseDayTime );//上一天的时间
	//			edit.putLong( intersititalAdverLastTime79App , laseTimeTime );//上一次的时间
	//			edit.putInt( intersititalAdverShowTimes79App , times );//显示次数
	//			edit.commit();
	//			return true;
	//		}
	//		//小于一天
	//		else if( times < 5 )//次数小于5,更新时间
	//		{
	//			//比较上一次显示时间(间隔超过二十分钟则可以显示)
	//			if( Math.abs( time - laseTimeTime ) >= 20 * 60 * 1000 )
	//			{
	//				times++;
	//				//次数更新
	//				//上次显示时间更新
	//				laseTimeTime = time;
	//				edit.putLong( intersititalAdverLastTime79App , laseTimeTime );//上一次的时间
	//				edit.putInt( intersititalAdverShowTimes79App , times );//显示次数
	//				edit.commit();
	//				return true;
	//			}
	//			//小于二十分钟不显示
	//			else
	//			{
	//				return false;
	//			}
	//		}
	//		//小于一天,并且次数大于5,不显示
	//		else
	//		{
	//			return false;
	//		}
	//	}
	//	else
	//	{
	//		return false;
	//	}
	//}
	//
	// zhangjin@2016/05/03 DEL END
	/*************************插屏广告 end*****************************/
	/*************************原生广告 start*****************************/
	private AdBaseView mNativeAdverView = null;
	public final static int folderNativeAdverAdverRequestNum = 4;//cheyingkun add	//解决“文件夹推荐应用获取数据不够四个时，引起桌面重启”的问题。【c_0004308】
	
	/**
	 * 初始化原生广告
	 * @param folder 
	 */
	public void initNativeAdverAdver(
			final Folder folder )
	{
		if( mNativeAdverView != null )
		{
			mNativeAdverView.onDestroy();
		}
		//		KmobManager.setAdSrcId( KmobManager.ADSRC_KMOB );//cheyingkun del	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
		String adPlaceId = LauncherDefaultConfig.getString( R.string.native_adver_place_id );
		KmobManager.setContext( mContext.getApplicationContext() ); //gaominghui add kmob初始化context
		// zhangjin@2016/03/29 ADD START
		KmobManager.setChannel( getSn() );
		// zhangjin@2016/03/29 ADD END
		mNativeAdverView = KmobManager.createNative( adPlaceId , folder.getContext() , folderNativeAdverAdverRequestNum );
		mNativeAdverView.addAdViewListener( new AdViewListener() {
			
			@Override
			public void onAdShow(
					String info )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAdReady(
					final String info )
			{
				if( folder != null )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.w( "operateNativeData" , StringUtils.concat( " NativeAdActivity onAdReady info " , info ) );
					//生成打开文件夹时可以显示的view,在下次打开时显示
					ArrayList<NativeData> createNativeDataByInfo = createNativeDataByInfo( info );
					folder.getNativerAdverLogoBitmap( createNativeDataByInfo );
				}
			}
			
			@Override
			public void onAdFailed(
					String reason )
			{
				if( folder != null )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.w( "operateNativeData" , StringUtils.concat( " NativeAdActivity onAdFailed reason " , reason ) );
					folder.setGetDataForNativeAdver( false );
					folder.nativeAdverRefreshStopAnimation();
				}
			}
			
			@Override
			public void onAdClose(
					String info )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAdClick(
					String info )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAdCancel(
					String info )
			{
				// TODO Auto-generated method stub
			}
		} );
	}
	
	/**
	 * 通过传入的ifo生成很多个nativeAdData，如果是一个广告，则info类型为JsonObject，多个广告类型，则为JsonArray，建议解析的时候先进行检测
	 * @param info
	 * @return
	 */
	private ArrayList<NativeData> createNativeDataByInfo(
			String info )
	{
		ArrayList<NativeData> allData = new ArrayList<NativeData>();
		if( info != null )
		{
			try
			{
				JSONObject object = new JSONObject( info );//此时若不是jsonObject，则会抛出异常
				NativeData adData = createNativeData( object );
				allData.add( adData );
			}
			catch( Exception e )
			{
				try
				{
					JSONArray array = new JSONArray( info );
					for( int i = 0 ; i < array.length() ; i++ )
					{
						JSONObject object = array.getJSONObject( i );
						NativeData adData = createNativeData( object );
						allData.add( adData );
					}
				}
				catch( Exception e2 )
				{
					// TODO: handle exception
				}
			}
		}
		return allData;
	}
	
	/**
	 * 通过广告传入的数据生成一个NativeAdData
	 * @param object
	 * @return
	 */
	private NativeData createNativeData(
			JSONObject object )
	{
		String summary = "";
		String headline = "";
		String adcategory = "";
		String appRating = "";
		String adlogo = "";
		String details = "";
		String adlogoWidth = "";
		String adlogoHeight = "";
		String review = "";
		String appinstalls = "";
		String download = "";
		String adplaceid = "";
		String adid = "";
		String clickurl = "";
		String interactiontype = "";
		String open_type = "";
		String hurl = "";
		String hdetailurl = "";
		String pkgname = "";
		String appsize = "";
		String version = "";
		String versionname = "";
		String ctimg = "";
		String hiimg = "";
		String click_record_url = "";//cheyingkun add	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
		try
		{
			if( object.has( NativeAdData.SUMMARY_TAG ) )
			{
				summary = object.getString( NativeAdData.SUMMARY_TAG );
			}
			if( object.has( NativeAdData.HEADLINE_TAG ) )
			{
				headline = object.getString( NativeAdData.HEADLINE_TAG );
			}
			if( object.has( NativeAdData.ADCATEGORY_TAG ) )
			{
				adcategory = object.getString( NativeAdData.ADCATEGORY_TAG );
			}
			if( object.has( NativeAdData.APPRATING_TAG ) )
			{
				appRating = object.getString( NativeAdData.APPRATING_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_TAG ) )
			{
				adlogo = object.getString( NativeAdData.ADLOGO_TAG );
			}
			if( object.has( NativeAdData.DETAILS_TAG ) )
			{
				details = object.getString( NativeAdData.DETAILS_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_WIDTH_TAG ) )
			{
				adlogoWidth = object.getString( NativeAdData.ADLOGO_WIDTH_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_HEIGHT_TAG ) )
			{
				adlogoHeight = object.getString( NativeAdData.ADLOGO_HEIGHT_TAG );
			}
			if( object.has( NativeAdData.REVIEW_TAG ) )
			{
				review = object.getString( NativeAdData.REVIEW_TAG );
			}
			if( object.has( NativeAdData.APPINSTALLS_TAG ) )
			{
				appinstalls = object.getString( NativeAdData.APPINSTALLS_TAG );
			}
			if( object.has( NativeAdData.DOWNLOAD_TAG ) )
			{
				download = object.getString( NativeAdData.DOWNLOAD_TAG );
			}
			if( object.has( NativeAdData.ADPLACE_ID_TAG ) )
			{
				adplaceid = object.getString( NativeAdData.ADPLACE_ID_TAG );
			}
			if( object.has( NativeAdData.AD_ID_TAG ) )
			{
				adid = object.getString( NativeAdData.AD_ID_TAG );
			}
			if( object.has( NativeAdData.CLICKURL_TAG ) )
			{
				clickurl = object.getString( NativeAdData.CLICKURL_TAG );
			}
			if( object.has( NativeAdData.INTERACTION_TYPE_TAG ) )
			{
				interactiontype = object.getString( NativeAdData.INTERACTION_TYPE_TAG );
			}
			if( object.has( NativeAdData.OPEN_TYPE_TAG ) )
			{
				open_type = object.getString( NativeAdData.OPEN_TYPE_TAG );
			}
			if( object.has( NativeAdData.HURL_TAG ) )
			{
				hurl = object.getString( NativeAdData.HURL_TAG );
			}
			if( object.has( NativeAdData.HDETAILURL_TAG ) )
			{
				hdetailurl = object.getString( NativeAdData.HDETAILURL_TAG );
			}
			if( object.has( NativeAdData.PKGNAME_TAG ) )
			{
				pkgname = object.getString( NativeAdData.PKGNAME_TAG );
			}
			if( object.has( NativeAdData.APPSIZE_TAG ) )
			{
				appsize = object.getString( NativeAdData.APPSIZE_TAG );
			}
			if( object.has( NativeAdData.VERSION_TAG ) )
			{
				version = object.getString( NativeAdData.VERSION_TAG );
			}
			if( object.has( NativeAdData.VERSIONNAME_TAG ) )
			{
				versionname = object.getString( NativeAdData.VERSIONNAME_TAG );
			}
			if( object.has( NativeAdData.CTIMG_TAG ) )
			{
				ctimg = object.getString( NativeAdData.CTIMG_TAG );
			}
			if( object.has( NativeAdData.HIIMG_TAG ) )
			{
				hiimg = object.getString( NativeAdData.HIIMG_TAG );
			}
			//cheyingkun add start	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
			if( object.has( NativeAdData.CLICK_RECORD_URL_TAG ) )
			{
				click_record_url = object.getString( NativeAdData.CLICK_RECORD_URL_TAG );
			}
			//cheyingkun add end
			return new NativeData(
					summary ,
					headline ,
					adcategory ,
					appRating ,
					adlogo ,
					details ,
					adlogoWidth ,
					adlogoHeight ,
					review ,
					appinstalls ,
					download ,
					adplaceid ,
					adid ,
					clickurl ,
					interactiontype ,
					open_type ,
					hurl ,
					hdetailurl ,
					pkgname ,
					appsize ,
					version ,
					versionname ,
					ctimg ,
					hiimg ,
					click_record_url );
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "operateNativeData" , StringUtils.concat( " createNativeData addAdView e " , e.toString() ) );
		}
		return null;
	}
	
	/**文件夹推荐应用,view的点击事件监听*/
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(
				View v )
		{
			switch( v.getId() )
			{
				case R.id.native_adver_icon_1:
				case R.id.native_adver_icon_2:
				case R.id.native_adver_icon_3:
				case R.id.native_adver_icon_4:
					NativeData data = (NativeData)v.getTag();
					//cheyingkun start	//更换79插屏广告、文件夹推荐应用的广告位id、KMobAd_APP_ID
					//					KmobManager.onClickDone( data.getAdplaceid() , data.getAdid() , data.getClickurl() , data.getInteractiontype() , data.getOpen_type() );//cheyingkun del
					//cheyikngun add start
					KmobManager.onClickDone(
							data.getAdplaceid() ,
							data.getAdid() ,
							data.getClickurl() ,
							data.getInteractiontype() ,
							data.getOpen_type() ,
							data.getPkgname() ,
							data.getClick_record_url() ,
							data.getHeadline() ,
							data.getDownload() ,
							data.getSummary() ,
							data.getAdlogo() ,
							false );//cheyingkun add	//解决“点击文件夹下方推荐应用下载，下载过程中切换模式，此时在点击此应用下载，提示正在下载中。状态栏无下载显示。”的问题。【i_0013284】
					//cheyinkgun add end
					//cheyingkun end
					break;
				case R.id.native_adver_refresh:
					Folder mFolder = (Folder)v.getTag();
					if( !mFolder.isGetDataForNativeAdver()// 
							//判断是否允许刷新(使用忽略时间的方法)
							&& canShowOperateNativeDataWithOutTime()//cheyingkun add	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
					)
					{
						mFolder.setGetDataForNativeAdver( true );
						mFolder.nativeAdverRefreshStartAnimation();
						initNativeAdverAdver( mFolder );
					}
					break;
				default:
					break;
			}
		}
	};
	
	/**获取文件夹推荐图标的view*/
	@SuppressWarnings( "unchecked" )
	public View getNativerAdverViewForFolder(
			final Context context ,
			ArrayList<NativeData> createNativeDataByInfo ,
			Folder folder )
	{
		View nativeAdverOpenFolderLayout;
		ArrayList<BubbleTextView> nativeAdverIcon;//四个图标的列表
		if( folder.getNativeAdverViewLayout() == null )
		{//如果是第一次,则创建view
			LayoutInflater inflater = LayoutInflater.from( context );
			nativeAdverOpenFolderLayout = inflater.inflate( R.layout.native_adver_open_folder_layout , null );
			//加载icon
			nativeAdverOpenFolderLayout.setVisibility( View.VISIBLE );
			final BubbleTextView icon_one = (BubbleTextView)nativeAdverOpenFolderLayout.findViewById( R.id.native_adver_icon_1 );
			final BubbleTextView icon_two = (BubbleTextView)nativeAdverOpenFolderLayout.findViewById( R.id.native_adver_icon_2 );
			final BubbleTextView icon_three = (BubbleTextView)nativeAdverOpenFolderLayout.findViewById( R.id.native_adver_icon_3 );
			final BubbleTextView icon_four = (BubbleTextView)nativeAdverOpenFolderLayout.findViewById( R.id.native_adver_icon_4 );
			View nativeAdverRefresh = nativeAdverOpenFolderLayout.findViewById( R.id.native_adver_refresh );
			folder.setNtiveAdverRefresh( nativeAdverRefresh );//设置旋转的view,方便点击时旋转和暂停
			nativeAdverRefresh.setOnClickListener( listener );//刷新view点击监听
			nativeAdverIcon = new ArrayList<BubbleTextView>();
			nativeAdverIcon.add( icon_one );
			nativeAdverIcon.add( icon_two );
			nativeAdverIcon.add( icon_three );
			nativeAdverIcon.add( icon_four );
			nativeAdverRefresh.setTag( folder );//刷新view的tag保存所属的Folder,为了方便刷新逻辑
			nativeAdverOpenFolderLayout.setTag( nativeAdverIcon );//运营布局view的tag设置四个图标列表,为了方便刷新逻辑
			LauncherAppState app = LauncherAppState.getInstance();
			DynamicGrid mDynamicGrid = app.getDynamicGrid();
			DeviceProfile grid = mDynamicGrid.getDeviceProfile();
			//设置每个icon的大小和边距
			int cellWidthPx = grid.getCellWidthPx();
			int cellHeightPx = grid.getCellHeightPx();
			int widthPx = grid.getWidthPx();
			//计算出间距
			int icon_gap = ( widthPx - cellWidthPx * folderNativeAdverAdverRequestNum ) / ( folderNativeAdverAdverRequestNum + 2 );
			//布局左右1.5倍间距
			nativeAdverOpenFolderLayout.setPadding( (int)( icon_gap * 1.5f ) , nativeAdverOpenFolderLayout.getPaddingTop() , (int)( icon_gap * 1.5f ) , nativeAdverOpenFolderLayout.getPaddingBottom() );
			for( int i = 0 ; i < folderNativeAdverAdverRequestNum ; i++ )
			{
				BubbleTextView bubbleTextView = nativeAdverIcon.get( i );
				RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams)bubbleTextView.getLayoutParams();
				layoutParams.width = cellWidthPx;
				layoutParams.height = cellHeightPx //
						- grid.getItemPaddingTopInCell() - grid.getItemPaddingBottomInCell();//cheyingkun add start	//解决“文件夹推荐应用名称显示不全”的问题。【i_0013225】
				//图标之间1倍间距
				if( i > 0 )
				{
					layoutParams.leftMargin = icon_gap;
				}
			}
		}
		else
		{//如果是第二次,从folder里拿到view
			nativeAdverOpenFolderLayout = folder.getNativeAdverViewLayout();
			//从view里拿到图标列表
			nativeAdverIcon = (ArrayList<BubbleTextView>)nativeAdverOpenFolderLayout.getTag();
		}
		//设置每个icon的图片和title
		IconCache mIconCache = LauncherAppState.getInstance().getIconCache();
		Drawable drawable = mContext.getResources().getDrawable( R.drawable.theme_default_folder_icon_bg );
		for( int i = 0 ; i < folderNativeAdverAdverRequestNum ; i++ )
		{
			Bitmap icon;
			String title;
			NativeData nativeData = createNativeDataByInfo.get( i );
			title = nativeData.getHeadline();
			if( TextUtils.isEmpty( title ) )
			{
				title = StringUtils.concat( "title is null" , i );
				nativeData.setHeadline( title );
			}
			try
			{
				nativeData = createNativeDataByInfo.get( i );
				title = nativeData.getHeadline();
				icon = nativeData.getAdlogoBitmap();
			}
			catch( Exception e )
			{
				icon = Tools.drawableToBitmap( drawable );
			}
			BubbleTextView bubbleTextView = nativeAdverIcon.get( i );
			ShortcutInfo shortcutInfo = createShortcutInfo( icon , title );
			bubbleTextView.applyFromShortcutInfo( shortcutInfo , mIconCache );
			bubbleTextView.setVisibility( View.VISIBLE );
			bubbleTextView.setOnClickListener( listener );
			bubbleTextView.setTag( nativeData );
			String adSpaceid = LauncherDefaultConfig.getString( R.string.native_adver_place_id );
			KmobManager.onNativeAdShow( adSpaceid , nativeData.getAdid() );
		}
		return nativeAdverOpenFolderLayout;
	}
	
	/**创建快捷方式信息*/
	private ShortcutInfo createShortcutInfo(
			Bitmap bitmap ,
			String title )
	{
		Bitmap bmp = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bitmap , LauncherAppState.getInstance().getContext() , true , false );
		Intent intent = new Intent();
		return OperateDynamicMain.createShortcutInfo( bmp , title , intent , 0 );
	}
	
	//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
	@Override
	public void notifyOperateNativeDataChanged(
			String string )
	{
		if( DEBUG )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "operateNativeData" , StringUtils.concat( " notifyOperateNativeDataChanged - string == " , string ) );
		}
		//解析json数据,更新配置信息
		try
		{
			JSONObject json = new JSONObject( string );
			//获取开关
			switch_enable_native_data_for_folder = json.getBoolean( OPERATE_NATIVE_DATA_SWITCH );
			//获取时间间隔(把天数转化成毫秒)
			int refreshIntervalDay = json.getInt( REFRESH_INTERVAL_OPERATE_NATIVE );
			refreshIntervalOperateNative = refreshIntervalDay * 24 * 60 * 60 * 1000;
			//获取是否wifi更新
			wifiOnlyOperateNative = json.getBoolean( WIFI_ONLY_OPERATE_NATIVE );
			//开关状态,wifiOnly,更新间隔,保存. 上次更新时间不保存,这样每次启动桌面时,都可以刷新一次
			Editor edit = prefs_native.edit();
			edit.putBoolean( OPERATE_NATIVE_DATA_SWITCH , switch_enable_native_data_for_folder );
			edit.putBoolean( WIFI_ONLY_OPERATE_NATIVE , wifiOnlyOperateNative );
			edit.putLong( REFRESH_INTERVAL_OPERATE_NATIVE , refreshIntervalOperateNative );
			edit.commit();
			if( DEBUG )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.i( "operateNativeData" , StringUtils.concat( " notifyOperateNativeDataChanged SWITCH_ENABLE_NATIVE_DATA_FOR_FOLDER : " , switch_enable_native_data_for_folder ) );
					Log.i( "operateNativeData" , StringUtils.concat( " notifyOperateNativeDataChanged refreshIntervalDay : " , refreshIntervalDay ) );
					Log.i( "operateNativeData" , StringUtils.concat( " notifyOperateNativeDataChanged wifiOnlyOperatenative : " , wifiOnlyOperateNative ) );
				}
			}
		}
		catch( JSONException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "operateNativeData" , StringUtils.concat( " notifyOperateNativeDataChanged updateOperateNativeDataSwitch:  " , e.toString() ) );
		}
	}
	
	/**根据运营数据判断是否可以显示*/
	public boolean canShowOperateNativeData()
	{
		if( !DlMethod.IsNetworkAvailable( mContext ) )
		{//没有网络连接
			return false;
		}
		long time = System.currentTimeMillis();
		if( switch_enable_native_data_for_folder//运营开关 
				&& lastRefreshTimeOperateNative + refreshIntervalOperateNative <= time//时间
		)
		{
			if( wifiOnlyOperateNative )
			{//只在wifi下更新
				if( DlMethod.IsWifiConnected( mContext ) )
				{
					return true;
				}
				else
				{//提示信息?
					return false;
				}
			}
			else
			{//数据更新
				return true;
			}
		}
		return false;
	}
	
	/**根据运营数据判断是否可以显示(忽略时间)*/
	public boolean canShowOperateNativeDataWithOutTime()
	{
		if( !DlMethod.IsNetworkAvailable( mContext ) )
		{//没有网络连接
			return false;
		}
		if( switch_enable_native_data_for_folder//运营开关 
		)
		{
			if( wifiOnlyOperateNative )
			{//只在wifi下更新
				if( DlMethod.IsWifiConnected( mContext ) )
				{
					return true;
				}
				else
				{//提示信息?
					return false;
				}
			}
			else
			{//数据更新
				return true;
			}
		}
		return false;
	}
	
	/**初始化运营配置*/
	public void initOperateNativeData()
	{
		prefs_native = mContext.getSharedPreferences( nativeAdverSharedPreferencesName , Context.MODE_PRIVATE );
		boolean defValue = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_native_data_for_folder );
		switch_enable_native_data_for_folder = prefs_native.getBoolean( OPERATE_NATIVE_DATA_SWITCH , defValue );
		wifiOnlyOperateNative = prefs_native.getBoolean( WIFI_ONLY_OPERATE_NATIVE , true );
		refreshIntervalOperateNative = prefs_native.getLong( REFRESH_INTERVAL_OPERATE_NATIVE , 24 * 60 * 60 * 1000 );
		if( DEBUG )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "operateNativeData" , StringUtils.concat( " initOperateNativeData SWITCH_ENABLE_NATIVE_DATA_FOR_FOLDER : " , switch_enable_native_data_for_folder ) );
				Log.d( "operateNativeData" , StringUtils.concat( " initOperateNativeData refreshIntervalOperateNative : " , refreshIntervalOperateNative ) );
				Log.d( "operateNativeData" , StringUtils.concat( " initOperateNativeData wifiOnlyOperateNative : " , wifiOnlyOperateNative ) );
			}
		}
	};
	
	/**更新上次自动刷新的时间,用户手动点击刷新不做时间判断*/
	public void updateLastRefreshTimeForOperateNative()
	{
		lastRefreshTimeOperateNative = System.currentTimeMillis();
		if( DEBUG )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "operateNativeData" , StringUtils.concat( " updateLastRefreshTimeForOperateNative lastRefreshTimeOperateNative : " , lastRefreshTimeOperateNative ) );
		}
	}
	
	//cheyingkun add end
	/*************************原生广告 end*****************************/
	//cheyingkun add start	//一键换壁纸需求。（剩余：动态图标、自定义事件统计）
	/*************************一键换壁纸 start*****************************/
	// zhangjin@2016/05/03 DEL START
	//private AdBaseView mWallpaperAdverView = null;
	//
	///**
	// * 初始化壁纸广告
	// * @param wallpaper 
	// */
	//public void initWallpaperAdverAdver()
	//{
	//	if( mWallpaperAdverView != null )
	//	{
	//		mWallpaperAdverView.onDestroy();
	//	}
	//	OneKeyChangeWallpaperManager.getInstance( mContext ).setGetDataForWallpaperAdver( true );
	//	String adPlaceId = mContext.getResources().getString( R.string.wallpaper_adver_place_id );
	//	// zhangjin@2016/03/29 ADD START
	//	KmobManager.setChannel( getSn() );
	//	// zhangjin@2016/03/29 ADD END
	//	mWallpaperAdverView = KmobManager.createNative( adPlaceId , mContext , 5 );
	//	mWallpaperAdverView.addAdViewListener( new AdViewListener() {
	//		
	//		@Override
	//		public void onAdShow(
	//				String info )
	//		{
	//			// TODO Auto-generated method stub
	//		}
	//		
	//		@Override
	//		public void onAdReady(
	//				final String info )
	//		{
	//			Log.w( "operateWallpaperData" , " WallpaperAdver onAdReady info " + info );
	//			ArrayList<NativeData> createWallpaperDataByInfo = createNativeDataByInfo( info );
	//			OneKeyChangeWallpaperManager.getInstance( mContext ).getWallpaperAdverImage( createWallpaperDataByInfo );
	//		}
	//		
	//		@Override
	//		public void onAdFailed(
	//				String reason )
	//		{
	//			Log.w( "operateWallpaperData" , " WallpaperAdver onAdFailed reason " + reason );
	//			OneKeyChangeWallpaperManager.getInstance( mContext ).getWallpaperAdverImage( null );
	//		}
	//		
	//		@Override
	//		public void onAdClose(
	//				String info )
	//		{
	//		}
	//		
	//		@Override
	//		public void onAdClick(
	//				String info )
	//		{
	//		}
	//		
	//		@Override
	//		public void onAdCancel(
	//				String info )
	//		{
	//		}
	//	} );
	//}
	// zhangjin@2016/05/03 DEL END
	/*************************一键换壁纸 end*****************************/
	//cheyingkun add start	//一键换壁纸需求。（剩余：动态图标、自定义事件统计）
	public void onResume()
	{
		if( mIntersititalView != null )
		{
			mIntersititalView.onResume();
		}
		if( mNativeAdverView != null )
		{
			mNativeAdverView.onResume();
		}
		// zhangjin@2016/05/03 DEL START
		//if( mWallpaperAdverView != null )
		//{
		//	mWallpaperAdverView.onResume();
		//}
		// zhangjin@2016/05/03 DEL END
	}
	
	public void onPause()
	{
		if( mIntersititalView != null )
		{
			mIntersititalView.onPause();
		}
		if( mNativeAdverView != null )
		{
			mNativeAdverView.onPause();
		}
		// zhangjin@2016/05/03 DEL START
		//if( mWallpaperAdverView != null )
		//{
		//	mWallpaperAdverView.onPause();
		//}
		// zhangjin@2016/05/03 DEL END
	}
	
	public void onDestroy()
	{
		if( mIntersititalView != null )
		{
			mIntersititalView.onDestroy();
		}
		if( mNativeAdverView != null )
		{
			mNativeAdverView.onDestroy();
		}
		// zhangjin@2016/05/03 DEL START
		//if( mWallpaperAdverView != null )
		//{
		//	mWallpaperAdverView.onDestroy();
		//}
		// zhangjin@2016/05/03 DEL END
	}
	
	// zhangjin@2016/03/29 ADD START
	public String getSn()
	{
		if( TextUtils.isEmpty( mSn ) == false )
		{
			return mSn;
		}
		mSn = LauncherConfigUtils.getSN( mContext );
		return mSn;
	}
	// zhangjin@2016/03/29 ADD END
}
//cheyingkun add end
