package cool.sdk.KmobConfig;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_cb;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_result;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public class KmobConfigUpdate extends UpdateHelper
{
	
	private static final String TAG = "KmobConfigUpdate";
	private static final String ACTION_KMOBCONFIG_REQUEST = "3710";
	static UpdateConfig config;
	static
	{
		config = new UpdateConfig();
		config.UPDATE_DEFAULT_MINUTES = 24 * 60;// 默认更新间隔
		config.UPDATE_MIN_MINUTES = 8 * 60;// 最小更新间隔
		config.UPDATE_MAX_MINUTES = 30 * 24 * 60;// 最大更新间隔
		config.MAX_UPDATE_TIMES_PER_DAY = 3;// 每天最大更新次数
		config.RETRY_TIMES_WHEN_ONLINE = 2;// 有网络下的重试次数
	};
	
	abstract class MyIconCoolDLCallback extends dl_cb implements CoolDLCallback
	{
	}
	
	protected Context context;
	private KmobConfigData kmobConfigData = KmobConfigData.getInstance();
	/*CoolDLMgr dlMgrIcon;
	Object dlMgrIconSync = new Object();*/
	Object kmobConfigDataSync = new Object();
	//private KusoEngineInfo engineInfo;
	private static final String BACKUP_KEY = "result.content";
	public static final int OPERATE_RESULT_HAVE_UPDATE = 0;//有更新
	public static final int OPERATE_RESULT_HAVE_NO_UPDATE = 100;//无更新（仍使用运营数据）
	public static final int OPERATE_RESULT_DONOT_USE_UPDATE = 200;//不使用运营数据，使用本地配置
	private int rc0 = OPERATE_RESULT_DONOT_USE_UPDATE;//运营结果
	private int c0 = 0;//广告开关：0:全部关闭1: 全部打开2: 分广告位控制
	
	protected KmobConfigUpdate(
			Context context )
	{
		super( context , "KmobConfig" , config );
		// TODO Auto-generated constructor stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "KmobConfigUpdate() context:" , context.getPackageName() ) );
		this.context = context;
		resetData( getString( BACKUP_KEY ) );//读取运营数据
	}
	
	private void resetData(
			String content )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "resetData() content:" , content ) );
		if( content != null )
		{
			try
			{
				JSONObject resJson = new JSONObject( content );
				rc0 = resJson.optInt( "rc0" );
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , StringUtils.concat( "resetData() rc0:" , rc0 ) );
				if( rc0 == OPERATE_RESULT_HAVE_UPDATE )
				{
					c0 = resJson.optInt( "c0" );
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "resetData() 运营广告--c0:" , c0 ) );
					synchronized( kmobConfigDataSync )
					{
						kmobConfigData.setC0( c0 );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( TAG , StringUtils.concat( "resetData() 运营广告--C0:" , kmobConfigData.isC0() ) );
						switch( c0 )
						{
							case 0://关闭广告
								break;
							case 1://广告全部打开
								readAdConfig( resJson , true );
								break;
							case 2://分广告为控制
								readAdConfig( resJson , false );
								JSONArray eJsonArray = resJson.getJSONArray( "c4" );
								Map<String , KmobAdPlaceIDConfig> c4ConfigMap = null;
								if( eJsonArray.length() > 0 )
								{
									c4ConfigMap = new HashMap<String , KmobAdPlaceIDConfig>();
									KmobAdPlaceIDConfig eConfig = null;
									for( int i = 0 ; i < eJsonArray.length() ; i++ )
									{
										eConfig = new KmobAdPlaceIDConfig();
										JSONObject obj = eJsonArray.getJSONObject( i );
										eConfig.setAdplaceId( obj.optString( "id" ) );
										boolean c4on = ( obj.optInt( "on" ) == 1 );
										eConfig.setOn( c4on );
										eConfig.setShows( obj.optLong( "shows" ) );
										eConfig.setShowtime( obj.optString( "showtime" ) );
										eConfig.setReqGap( obj.optLong( "reqgap" ) );
										c4ConfigMap.put( obj.optString( "id" ) , eConfig );
									}
									kmobConfigData.setC4( c4ConfigMap );
								}
								break;
							default:
								break;
						}
					}
				}
				else if( rc0 == OPERATE_RESULT_DONOT_USE_UPDATE )
				{
					synchronized( kmobConfigDataSync )
					{
						kmobConfigData.setData( new KmobConfigData() );//不使用运营数据则将kusoData设为初始值
					}
				}
				else
				//OPERATE_RESULT_HAVE_NO_UPDATE
				{
					synchronized( kmobConfigDataSync )
					{
					}
				}
			}
			catch( Exception e )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "exception e = " , e.toString() ) );
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "kmobConfigData = " , kmobConfigData.toString() ) );
		}
		else
		{
			synchronized( kmobConfigDataSync )
			{
				kmobConfigData.setData( new KmobConfigData() );//不使用运营数据则将kusoData设为初始值
			}
		}
	}
	
	/**
	 *
	 * @param resJson
	 * @param config
	 * @throws JSONException
	 * @author gaominghui 2016年6月14日
	 */
	private void readAdConfig(
			JSONObject resJson ,
			boolean allOn ) throws JSONException
	{
		KmobAdPlaceIDConfig config = new KmobAdPlaceIDConfig();
		int mC1 = resJson.optInt( "c1" );
		setGapMinute( mC1 );
		//config.UPDATE_DEFAULT_MINUTES = c1;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "mC1" , mC1 ) );
		kmobConfigData.setC1( mC1 );
		kmobConfigData.setC2( resJson.optString( "c2" ) );
		JSONObject objectJson = resJson.getJSONObject( "c3" );
		if( objectJson.optString( "id" ) != null )
		{
			config.setAdplaceId( objectJson.optString( "id" ) );
		}
		config.setShows( objectJson.optLong( "shows" ) );
		config.setShowtime( objectJson.optString( "showtime" ) );
		config.setReqGap( objectJson.optLong( "reqgap" ) );
		boolean on = allOn ? allOn : ( objectJson.optInt( "on" ) == 1 );
		config.setOn( on );
		kmobConfigData.setC3( config );
	}
	
	@Override
	protected boolean OnUpdate(
			final Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "OnUpdate()" );
		//		boolean old_commonShowSearch = enableShowCommonPageSearch();
		//		boolean old_favoritesShowSearch = enableShowFavoritesPageSearch();
		String c2 = getString( "c2" );
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , KmobConfig.h12 , KmobConfig.h13 );
		reqJson.put( "Action" , ACTION_KMOBCONFIG_REQUEST );
		reqJson.put( "p1" , c2 == null ? "0" : c2 );
		reqJson.put( "p2" , "3.0.0" );
		reqJson.put( "p3" , 1 );
		reqJson.put( "p4" , Locale.getDefault().toString() );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "OnUpdate() req:" , reqJson.toString() ) );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "OnUpdate() url:" , UrlUtil.getDataServerUrl() ) );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "OnUpdate() rsp:(error),httpCode:" , result.httpCode , "-exception:" , result.exception ) );
			return false;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "OnUpdate() rsp,httpCode:" , result.httpCode , "-content:" , result.content ) );
		JSONObject resJson = new JSONObject( result.content );
		rc0 = resJson.optInt( "rc0" );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "OnUpdate() rc0:" , rc0 ) );
		if( rc0 != OPERATE_RESULT_HAVE_NO_UPDATE )
		{
			setValue( BACKUP_KEY , result.content );
			if( result.content != null )
			{
				resetData( result.content );
			}
			/*getCoolDLMgrIcon();*/
			MyIconCoolDLCallback iconDownloadCB = new MyIconCoolDLCallback() {
				
				@Override
				public void onDoing(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "OnUpdate() onDoing:" , arg2.getCurBytes() ) );
				}
				
				@Override
				public void onFail(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "OnUpdate() onFail:" , arg2.getURL() ) );
				}
				
				@Override
				public void onSuccess(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "OnUpdate() onSuccess:" , arg2.getURL() ) );
				}
				
				@Override
				public void onDoing(
						dl_info arg0 ) throws Exception
				{
					// TODO Auto-generated method stub
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "OnUpdate() onDoing2:" , arg0.getCurBytes() ) );
				}
				
				@Override
				public void onFail(
						dl_result arg0 ,
						dl_info arg1 ) throws Exception
				{
					// TODO Auto-generated method stub
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "OnUpdate() onFail2:" , arg1.getURL() ) );
				}
				
				@Override
				public void onStart(
						dl_info arg0 ) throws Exception
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onSuccess(
						dl_info arg0 ) throws Exception
				{
					// TODO Auto-generated method stub
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "OnUpdate() onSuccess1:" , arg0.getURL() ) );
				}
			};
			/*SharedPreferences sp = context.getSharedPreferences( "sp_setting" , Context.MODE_PRIVATE );
			Editor editor = sp.edit();
			editor.putBoolean( "flag" , true );
			editor.commit();*/
		}
		return true;
	}
	/*public void getKmobConfigData()
	{
		resetData( getString( BACKUP_KEY , null ) );
		String content
		if(  != null )
		{
			try
			{
				JSONObject resJson = new JSONObject( content );
				rc0 = resJson.optInt( "rc0" );
				Log.v( TAG , "resetData() rc0:" + rc0 );
				if( rc0 == OPERATE_RESULT_HAVE_UPDATE )
				{
					c0 = resJson.optInt( "c0" );
					Log.v( TAG , "resetData() 运营广告--c0:" + c0 );
					synchronized( kmobConfigDataSync )
					{
						kmobConfigData.setC0( c0 );
						Log.v( TAG , "resetData() 运营广告--C0:" + kmobConfigData.isC0() );
						switch( c0 )
						{
							case 0://关闭广告
								break;
							case 1://广告全部打开
								readAdConfig( resJson , true );
								break;
							case 2://分广告为控制
								readAdConfig( resJson , false );
								JSONArray eJsonArray = resJson.getJSONArray( "c4" );
								Map<String , KmobAdPlaceIDConfig> c4ConfigMap = null;
								if( eJsonArray.length() > 0 )
								{
									c4ConfigMap = new HashMap<String , KmobAdPlaceIDConfig>();
									for( int i = 0 ; i < eJsonArray.length() ; i++ )
									{
										JSONObject obj = eJsonArray.getJSONObject( i );
										KmobAdPlaceIDConfig eConfig = new KmobAdPlaceIDConfig();
										eConfig.setAdplaceId( obj.optString( "id" ) );
										boolean c4on = ( obj.optInt( "on" ) == 1 );
										eConfig.setOn( c4on );
										eConfig.setShows( obj.optLong( "shows" ) );
										eConfig.setShowtime( obj.optString( "showtime" ) );
										eConfig.setReqGap( obj.optLong( "reqgap" ) );
										c4ConfigMap.put( obj.optString( "id" ) , eConfig );
									}
									kmobConfigData.setC4( c4ConfigMap );
								}
								break;
							default:
								break;
						}
					}
				}
				else if( rc0 == OPERATE_RESULT_DONOT_USE_UPDATE )
				{
					synchronized( kmobConfigDataSync )
					{
						kmobConfigData.setData( new KmobConfigData() );//不使用运营数据则将kusoData设为初始值
					}
				}
				else
				//OPERATE_RESULT_HAVE_NO_UPDATE
				{
					synchronized( kmobConfigDataSync )
					{
					}
				}
			}
			catch( Exception e )
			{
				Log.e( TAG , "exception e = " + e );
			}
	}*/
}
