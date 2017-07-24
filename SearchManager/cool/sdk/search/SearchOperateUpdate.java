package cool.sdk.search;


import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public class SearchOperateUpdate extends UpdateHelper
{
	
	private static final String TAG = "SearchOperateUpdate";
	private static final String ACTION_KUSO_REQUEST = "3702";
	private static final int h12 = 3;
	private static final String h13 = "KuSo";
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
	protected Context context;
	private static final String BACKUP_KEY = "result.content";
	public static final int OPERATE_RESULT_HAVE_UPDATE = 0;//有更新
	public static final int OPERATE_RESULT_HAVE_NO_UPDATE = 100;//无更新（仍使用运营数据）
	public static final int OPERATE_RESULT_DONOT_USE_UPDATE = 200;//不使用运营数据，使用本地配置
	private int rc0 = OPERATE_RESULT_DONOT_USE_UPDATE;//运营结果
	private boolean c0 = false;//true为运营搜索，false为是否运营搜索使用本地配置
	private int c8 = 0;//10:运营打开solo，else:运营关solo
	
	protected SearchOperateUpdate(
			Context context )
	{
		super( context , "KuSo" , config );
		// TODO Auto-generated constructor stub
		Log.v( TAG , "SearchOperateUpdate() context:" + context.getPackageName() );
		this.context = context;
		resetData( getString( BACKUP_KEY ) );//读取运营数据
	}
	
	/**
	* 是否使用运营数据
	* @return true:使用；false:不使用
	*/
	protected boolean useOperateData()
	{
		boolean rlt = ( rc0 != OPERATE_RESULT_DONOT_USE_UPDATE );
		Log.v( TAG , "useOperateData():" + rlt + " rc0:" + rc0 );
		return rlt;
	}
	
	/**
	* 运营是否打开我们的搜索
	* @return true:打开；false:未打开
	*/
	protected boolean useOperateSearchSwitch()
	{
		boolean rlt = false;
		if( useOperateData() )
		{
			rlt = c0;
		}
		else
		{
			rlt = false;
		}
		Log.v( TAG , "useOperateSearchSwitch():" + rlt + " c0:" + c0 );
		return rlt;
	}
	
	/**
	* 是否使用我们的搜索
	* @return true:使用；false:不使用
	*/
	public boolean enableCooeeSearch()
	{
		boolean rlt = true;
		if( useOperateSearchSwitch() )
		{
			rlt = true;
		}
		else
		{
			rlt = SearchConfig.SWITCH_ENABLE_COOEE_SEARCH_DEFAULT;
		}
		Log.v( TAG , "enableCooeeSearch():" + rlt );
		return rlt;
	}
	
	/**
	* 是否使用solo搜索（不考虑语言环境）（20160524运营需求：永远返回false）
	* @return true:使用；false:不使用
	*/
	public boolean enableSoloSearch()
	{
		// jubingcheng@2016/05/24 DEL START
		//boolean rlt = true;
		//// jubingcheng@2016/04/25 UPD START 产品需求：无视本地SOLO配置，只有运营打开SOLO时才会进SOLO
		////if( enableCooeeSearch() )
		////{
		////	if( useOperateSearchSwitch() )
		////	{
		////		if( c8 == 10 )//运营打开solo
		////		{
		////			rlt = true;
		////		}
		////		else
		////		{
		////			rlt = false;
		////		}
		////	}
		////	else
		////	{
		////		rlt = SearchConfig.SWITCH_ENABLE_SOLO_DEFAULT;
		////	}
		////}
		////else
		////{
		////	rlt = false;
		////}
		//if( useOperateSearchSwitch() && c8 == 10 )
		//{
		//	rlt = true;
		//}
		//else
		//{
		//	rlt = false;
		//}
		//// jubingcheng@2016/04/25 UPD END
		//Log.v( TAG , "enableSoloSearch():" + rlt + " c8:" + c8 );
		//return rlt;
		// jubingcheng@2016/05/24 DEL END
		Log.v( TAG , "enableSoloSearch():false" );
		return false;
	}
	
	/**
	* 普通页是否显示搜索
	* @return true:显示；false:不显示
	*/
	public boolean enableShowCommonPageSearch()
	{
		boolean rlt = false;
		if( useOperateSearchSwitch() )
		{
			rlt = true;
		}
		else
		{
			rlt = SearchConfig.SWITCH_ENABLE_COMMON_PAGE_SEARCH_DEFAULT;
		}
		Log.v( TAG , "enableShowCommonPageSearch():" + rlt );
		return rlt;
	}
	
	/**
	* 酷生活是否显示搜索
	* @return true:显示；false:不显示
	*/
	public boolean enableShowFavoritesPageSearch()
	{
		boolean rlt = false;
		if( useOperateSearchSwitch() )
		{
			rlt = true;
		}
		else
		{
			rlt = SearchConfig.SWITCH_ENABLE_FAVORITES_PAGE_SEARCH_DEFAULT;
		}
		Log.v( TAG , "enableShowFavoritesPageSearch():" + rlt );
		return rlt;
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
					Log.v( TAG , "resetData() c0:" + c0 );
					if( c0 )
					{
						c8 = cJson.optInt( "c8" );
						Log.v( TAG , "resetData() c8:" + c8 );
					}
					Log.v( TAG , "resetData() cJson:" + cJson.toString() );
				}
				else if( rc0 == OPERATE_RESULT_DONOT_USE_UPDATE )//不使用运营数据则将数据设为初始值
				{
					c0 = false;
					c8 = 0;
				}
				else
				//OPERATE_RESULT_HAVE_NO_UPDATE
				{
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
		boolean old_commonShowSearch = enableShowCommonPageSearch();
		boolean old_favoritesShowSearch = enableShowFavoritesPageSearch();
		String c2 = getString( "c2" );
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , h12 , h13 );
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
			boolean new_commonShowSearch = enableShowCommonPageSearch();
			boolean new_favoritesShowSearch = enableShowFavoritesPageSearch();
			notifyKuSoSwitch( old_commonShowSearch , old_favoritesShowSearch , new_commonShowSearch , new_favoritesShowSearch );//运营酷搜
		}
		return true;
	}
	
	//通知IKuSoUpdateCallbacks处理搜索的运营 start
	private static IKuSoUpdateCallbacks mIKuSoUpdateCallbacks = null;
	
	public interface IKuSoUpdateCallbacks
	{
		
		public void notifyKuSoSwitch(
				boolean old_commonShowSearch ,
				boolean old_favoritesShowSearch ,
				boolean new_commonShowSearch ,
				boolean new_favoritesShowSearch );
	}
	
	public static void setCallbacks(
			IKuSoUpdateCallbacks mCallbacks )
	{
		mIKuSoUpdateCallbacks = mCallbacks;
	}
	
	/**
	* 通知IKuSoUpdateCallbacks处理搜索的运营
	* @param old_commonShowSearch 普通页是否已经显示搜索
	* @param old_favoritesShowSearch 酷生活是否已经显示搜索
	* @param new_commonShowSearch 普通页是否需要显示搜索
	* @param new_favoritesShowSearch 酷生活是否需要显示搜索
	*/
	private void notifyKuSoSwitch(
			boolean old_commonShowSearch ,
			boolean old_favoritesShowSearch ,
			boolean new_commonShowSearch ,
			boolean new_favoritesShowSearch )
	{
		Log.v(
				TAG ,
				"notifyKuSoSwitch() old_common:" + old_commonShowSearch + " old_favorites:" + old_favoritesShowSearch + " new_common:" + new_commonShowSearch + " new_favorites:" + new_favoritesShowSearch + " mIKuSoUpdateCallbacks:" + mIKuSoUpdateCallbacks );
		if( mIKuSoUpdateCallbacks != null )
		{
			mIKuSoUpdateCallbacks.notifyKuSoSwitch( old_commonShowSearch , old_favoritesShowSearch , new_commonShowSearch , new_favoritesShowSearch );
		}
	}
	//通知IKuSoUpdateCallbacks处理搜索的运营 end
}
