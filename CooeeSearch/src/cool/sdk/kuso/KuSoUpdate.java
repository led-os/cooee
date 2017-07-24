package cool.sdk.kuso;


import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_cb;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_result;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public class KuSoUpdate extends UpdateHelper
{
	
	private static final String TAG = "KuSoUpdate";
	private static final String ACTION_KUSO_REQUEST = "3702";
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
	private KusoData kusoData = new KusoData();
	CoolDLMgr dlMgrIcon;
	Object dlMgrIconSync = new Object();
	Object kusoDataSync = new Object();
	private KusoEngineInfo engineInfo;
	private static final String BACKUP_KEY = "result.content";
	public static final int OPERATE_RESULT_HAVE_UPDATE = 0;//有更新
	public static final int OPERATE_RESULT_HAVE_NO_UPDATE = 100;//无更新（仍使用运营数据）
	public static final int OPERATE_RESULT_DONOT_USE_UPDATE = 200;//不使用运营数据，使用本地配置
	private int rc0 = OPERATE_RESULT_DONOT_USE_UPDATE;//运营结果
	private boolean c0 = false;//true为运营搜索，false为是否运营搜索使用本地配置
	
	//	//xiatian add start 需求：通过更新运营数据来动态运营出搜索或关闭运营使用本地配置
	//	private static IKuSoUpdateCallbacks mIKuSoUpdateCallbacks = null;
	//	
	//	public interface IKuSoUpdateCallbacks
	//	{
	//		
	//		public void notifyKuSoSwitch(
	//				boolean old_commonShowSearch ,
	//				boolean old_favoritesShowSearch ,
	//				boolean new_commonShowSearch ,
	//				boolean new_favoritesShowSearch );
	//	}
	//	
	//	public static void setCallbacks(
	//			IKuSoUpdateCallbacks mCallbacks )
	//	{
	//		mIKuSoUpdateCallbacks = mCallbacks;
	//	}
	//	
	//	//xiatian add end
	public CoolDLMgr getCoolDLMgrIcon()
	{
		synchronized( dlMgrIconSync )
		{
			if( dlMgrIcon == null )
			{
				dlMgrIcon = KuSo.CoolDLMgr( context , "DICON" );
				dlMgrIcon.dl_mgr.setMaxConnectionCount( 3 );
				dlMgrIcon.dl_mgr.setDownloadPath( dlMgrIcon.getInternalPath() );
				dlMgrIcon.setCheckPathEverytime( false );
			}
		}
		return dlMgrIcon;
	}
	
	protected KuSoUpdate(
			Context context )
	{
		super( context , "KuSo" , config );
		// TODO Auto-generated constructor stub
		Log.v( TAG , "KuSoUpdate() context:" + context.getPackageName() );
		this.context = context;
		resetData( getString( BACKUP_KEY ) );//读取运营数据
	}
	
	private void resetData(
			String content )
	{
		Log.v( TAG , "resetData() content:" + content );
		if( content != null )
		{
			try
			{
				JSONObject resJson = new JSONObject( content );
				rc0 = resJson.optInt( "rc0" );
				Log.v( TAG , "resetData() rc0:" + rc0 );
				if( rc0 == OPERATE_RESULT_HAVE_UPDATE )
				{
					String resConfig = resJson.getString( "config" );
					JSONObject cJson = new JSONObject( resConfig );
					c0 = cJson.optInt( "c0" ) == 1;
					synchronized( kusoDataSync )
					{
						kusoData.setC0( c0 );
						Log.v( TAG , "resetData() 运营搜索--C0:" + kusoData.isC0() );
						if( c0 )
						{
							int c1 = cJson.optInt( "c1" );
							config.UPDATE_DEFAULT_MINUTES = c1;
							kusoData.setC1( c1 );
							kusoData.setC2( cJson.optString( "c2" ) );
							kusoData.setC3( cJson.optInt( "c3" ) == 1 );
							kusoData.setC4( cJson.optInt( "c4" ) == 1 );
							kusoData.setC5( cJson.optString( "c5" ) );
							String temWords = cJson.optString( "c6" );
							if( temWords != null && !temWords.equals( "" ) )
							{
								kusoData.getC6().clear();
								String[] words = temWords.split( "," );
								for( int i = 0 ; i < words.length ; i++ )
								{
									kusoData.getC6().add( words[i] );
								}
							}
							else
							{
								kusoData.getC6().clear();//重新投放shellid时，热词c6默认为空
							}
							kusoData.setC7( cJson.optInt( "c7" ) );
							kusoData.setC8( cJson.optInt( "c8" ) );
							Log.v( TAG , "resetData() 版本号--C2:" + kusoData.getC2() );
							Log.v( TAG , "resetData() 打开方式浏览器？--C3:" + kusoData.isC3() );
							Log.v( TAG , "resetData() 显示运营页？--C4:" + kusoData.isC4() );
							Log.v( TAG , "resetData() 运营页url--C5:" + kusoData.getC5() );
							Log.v( TAG , "resetData() 热词--C6:" + kusoData.getC6() + " size:" + kusoData.getC6().size() + " empty:" + kusoData.getC6().isEmpty() );
							Log.v( TAG , "resetData() 国内--C7:" + kusoData.getC7() );
							Log.v( TAG , "resetData() 国外--C8:" + kusoData.getC8() );
							Log.v( TAG , "resetData() cJson:" + cJson.toString() );
							kusoData.getEngines().clear();
							JSONArray eJsonArray = resJson.getJSONArray( "engine" );
							for( int i = 0 ; i < eJsonArray.length() ; i++ )
							{
								JSONObject eJson = eJsonArray.getJSONObject( i );
								engineInfo = new KusoEngineInfo();
								engineInfo.setR1( eJson.optString( "r1" ) );
								engineInfo.setR2( eJson.optString( "r2" ) );
								engineInfo.setR3( eJson.optString( "r3" ) );
								engineInfo.setR4( eJson.optString( "r4" ) );
								engineInfo.setR5( eJson.optString( "r5" ) );
								engineInfo.setR6( eJson.optInt( "r6" ) == 1 );
								kusoData.getEngines().add( engineInfo );
							}
						}
					}
				}
				else if( rc0 == OPERATE_RESULT_DONOT_USE_UPDATE )
				{
					synchronized( kusoDataSync )
					{
						kusoData.setData( new KusoData() );//不使用运营数据则将kusoData设为初始值
					}
				}
				else
				//OPERATE_RESULT_HAVE_NO_UPDATE
				{
					synchronized( kusoDataSync )
					{
					}
				}
			}
			catch( Exception e )
			{
			}
		}
	}
	
	@Override
	protected boolean OnUpdate(
			final Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		Log.v( TAG , "OnUpdate()" );
		//		boolean old_commonShowSearch = enableShowCommonPageSearch();
		//		boolean old_favoritesShowSearch = enableShowFavoritesPageSearch();
		String c2 = getString( "c2" );
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , KuSo.h12 , KuSo.h13 );
		reqJson.put( "Action" , ACTION_KUSO_REQUEST );
		reqJson.put( "p1" , c2 == null ? "0" : c2 );
		reqJson.put( "p2" , "3.0.0" );
		reqJson.put( "p3" , 1 );
		reqJson.put( "p4" , Locale.getDefault().toString() );
		Log.v( TAG , "OnUpdate() req:" + reqJson.toString() );
		Log.v( TAG , "OnUpdate() url:" + UrlUtil.getDataServerUrl() );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			Log.v( TAG , "OnUpdate() rsp:(error)" + result.httpCode + " exception:" + result.exception );
			return false;
		}
		Log.v( TAG , "OnUpdate() rsp:" + result.httpCode + " content:" + result.content );
		JSONObject resJson = new JSONObject( result.content );
		rc0 = resJson.optInt( "rc0" );
		Log.v( TAG , "OnUpdate() rc0:" + rc0 );
		if( rc0 != OPERATE_RESULT_HAVE_NO_UPDATE )
		{
			setValue( BACKUP_KEY , result.content );
			resetData( result.content );
			getCoolDLMgrIcon();
			MyIconCoolDLCallback iconDownloadCB = new MyIconCoolDLCallback() {
				
				@Override
				public void onDoing(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					Log.v( TAG , "OnUpdate() onDoing:" + arg2.getCurBytes() );
				}
				
				@Override
				public void onFail(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					Log.v( TAG , "OnUpdate() onFail:" + arg2.getURL() );
				}
				
				@Override
				public void onSuccess(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					Log.v( TAG , "OnUpdate() onSuccess:" + arg2.getURL() );
				}
				
				@Override
				public void onDoing(
						dl_info arg0 ) throws Exception
				{
					// TODO Auto-generated method stub
					Log.v( TAG , "OnUpdate() onDoing2:" + arg0.getCurBytes() );
				}
				
				@Override
				public void onFail(
						dl_result arg0 ,
						dl_info arg1 ) throws Exception
				{
					// TODO Auto-generated method stub
					Log.v( TAG , "OnUpdate() onFail2:" + arg1.getURL() );
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
					Log.v( TAG , "OnUpdate() onSuccess1:" + arg0.getURL() );
				}
			};
			SharedPreferences sp = context.getSharedPreferences( "sp_setting" , Context.MODE_PRIVATE );
			Editor editor = sp.edit();
			editor.putBoolean( "flag" , true );
			editor.commit();
			KusoData data = getKusoData();
			//		boolean new_commonShowSearch = enableShowCommonPageSearch();
			//		boolean new_favoritesShowSearch = enableShowFavoritesPageSearch();
			//		notifyKuSoSwitch( old_commonShowSearch , old_favoritesShowSearch , new_commonShowSearch , new_favoritesShowSearch );//运营酷搜
			if( data.isC0() )
			{
				Log.v( TAG , "OnUpdate() data.getEngines().size():" + data.getEngines().size() );
				for( int i = 0 ; i < data.getEngines().size() ; i++ )
				{
					String url = data.getEngines().get( i ).getR3();
					if( url != null )
					{
						dl_info info = dlMgrIcon.UrlGetInfo( url );
						if( info == null || !info.IsDownloadSuccess() )
						{
							dlMgrIcon.UrlDownload( url , iconDownloadCB );
						}
					}
				}
			}
		}
		return true;
	}
	
	public KusoData getKusoData()
	{
		KusoData data = new KusoData();
		synchronized( kusoDataSync )
		{
			data.setData( kusoData );
		}
		return data;
	}
	//	/**
	//	 * 通知IKuSoUpdateCallbacks处理搜索的运营（普通页和酷生活各自是否需要打开或关闭搜索）
	//	 * @param old_commonShowSearch 普通页是否已经显示搜索
	//	 * @param old_favoritesShowSearch 酷生活是否已经显示搜索
	//	 * @param new_commonShowSearch 普通页是否需要显示搜索
	//	 * @param new_favoritesShowSearch 酷生活是否需要显示搜索
	//	 */
	//	private void notifyKuSoSwitch(
	//			boolean old_commonShowSearch ,
	//			boolean old_favoritesShowSearch ,
	//			boolean new_commonShowSearch ,
	//			boolean new_favoritesShowSearch )
	//	{
	//		Log.v(
	//				TAG ,
	//				"notifyKuSoSwitch() mIKuSoUpdateCallbacks:" + mIKuSoUpdateCallbacks + " old_commonShowSearch:" + old_commonShowSearch + " old_favoritesShowSearch:" + old_favoritesShowSearch + " new_commonShowSearch:" + new_commonShowSearch + " new_favoritesShowSearch:" + new_favoritesShowSearch );
	//		if( mIKuSoUpdateCallbacks != null )
	//		{
	//			mIKuSoUpdateCallbacks.notifyKuSoSwitch( old_commonShowSearch , old_favoritesShowSearch , new_commonShowSearch , new_favoritesShowSearch );
	//		}
	//	}
}
