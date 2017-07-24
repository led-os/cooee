package cool.sdk.Pspread;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.OperateExpandData.OperateExpandDataManager;
import com.cooee.framework.function.OperateExplorer.OperateExplorer;
import com.cooee.framework.function.OperateFavorites.OperateFavorites;
import com.cooee.framework.function.OperateMediaPluginPage.OperateMediaPluginDataManager;
import com.cooee.framework.function.OperateUmeng.OperateUmeng;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.manager.DlMethod;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public abstract class PspreadUpdate extends UpdateHelper
{
	
	protected static final String ACTION_CONFIG_REQUEST = "3705";
	private static UpdateConfig config;
	static
	{
		config = new UpdateConfig();
		config.UPDATE_DEFAULT_MINUTES = 3 * 24 * 60;// 默认更新间隔
		config.UPDATE_MIN_MINUTES = 8 * 60;// 最小更新间隔
		config.UPDATE_MAX_MINUTES = 15 * 24 * 60;// 最大更新间隔
		config.MAX_UPDATE_TIMES_PER_DAY = 3;// 每天最大更新次数
		config.RETRY_TIMES_WHEN_ONLINE = 3;//有网络下的重试次数
	};
	protected Context context;
	private int c2 = 0; //强制开启/关闭（暂时无用）
	private int c3 = 0; //是否使用“友盟统计”的开关。0关闭；1开启。
	//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
	private int c4 = 0; //是否使用“猜你喜欢”的开关。0关闭；1开启。
	private int c5 = 0; //是否使用“-1屏”的开关。0关闭；1开启。
	private String c6 = ""; //自定义拓展配置
	
	//cheyingkun add end
	protected PspreadUpdate(
			Context context )
	{
		super( context , Pspread.h13 , config );
		// TODO Auto-generated constructor stub
		this.context = context;
		c2 = this.getInt( "c2" , 0 );
		c3 = this.getInt( "c3" , 0 );
		//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
		c4 = this.getInt( "c4" , 0 );
		c5 = this.getInt( "c5" , 0 );
		c6 = this.getString( "c6" , "" );
		//cheyingkun add end
	}
	
	@Override
	protected boolean OnUpdate(
			Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		if( !DlMethod.IsNetworkAvailable( context ) )
		{
			return false;
		}
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , Pspread.h12 , Pspread.h13 );
		reqJson.put( "Action" , ACTION_CONFIG_REQUEST );
		reqJson.put( "p1" , context.getPackageName() );
		reqJson.put( "p2" , -1 );//当前点传文件夹的状态【已经废除】
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "PspreadUpdate req:" , reqJson.toString() ) );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , StringUtils.concat( "PspreadUpdate rsp:(error),httpCode:" , result.httpCode , "-exception:" , result.exception ) );
			return false;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "PspreadUpdate rsp,httpCode:" , result.httpCode , "-content:" , result.content ) );
		JSONObject resJson = new JSONObject( result.content );
		int retcode = resJson.optInt( "rc0" );
		if( retcode == 0 )
		{
			config.UPDATE_DEFAULT_MINUTES = resJson.optLong( "c1" );
			c2 = resJson.optInt( "c2" );
			c3 = resJson.optInt( "c3" );
			//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
			c4 = resJson.optInt( "c4" , 0 );
			c5 = resJson.optInt( "c5" , 0 );
			c6 = resJson.optString( "c6" , "" );
			//cheyingkun add end
			notifySpreadSwitch();
			setValue( "c2" , c2 );
			setValue( "c3" , c3 );
			//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
			setValue( "c4" , c4 );
			setValue( "c5" , c5 );
			setValue( "c6" , c6 );
			//cheyingkun add end
			setValue( "result.content" , result.content );
			return true;
		}
		else if( retcode == 200 )
		{
			c2 = 0;
			c3 = 0;
			//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
			c4 = 0;
			c5 = 0;
			c6 = "";
			//cheyingkun add end
			notifySpreadSwitch();
			setValue( "c2" , c2 );
			setValue( "c3" , c3 );
			//cheyingkun add start	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
			setValue( "c4" , c4 );
			setValue( "c5" , c5 );
			setValue( "c6" , c6 );
			//cheyingkun add end
			setValue( "result.content" , "" );
			return true;
		}
		else if( retcode == 100 )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void notifySpreadSwitch()
	{//通知launcher
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "PspreadUpdate c2:" , c2 , "-c3:" , c3 , "-c4:" , c4 , "-c5:" , c5 ) );
		OperateUmeng.notifyUmengSwitch( c3 == 0 ? false : true ); //xiatian add	//运营友盟（详见“OperateUmeng”中说明）
		OperateExpandDataManager.notifyOperateExpandDataManager( c6 );//cheyingkun add	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
		OperateFavorites.notifyFavoritesSwitch( c5 == 1 );
		notifyExplorerConfig();//xiatian add	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
		notifyMediaPluginConfig();//gaominghui add  //需求：支持后台运营音乐页和相机页
	}
	
	//xiatian add start	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
	private void notifyExplorerConfig()
	{
		boolean enableOperateExplorer = false;
		String homeWebsite = "";
		try
		{
			JSONObject json = new JSONObject( c6 );
			if( json.has( OperateExplorer.OPERATE_EXPLORER_ENABLE_SIMPLE_KEY ) )
			{
				enableOperateExplorer = json.getBoolean( OperateExplorer.OPERATE_EXPLORER_ENABLE_SIMPLE_KEY );
			}
			if( json.has( OperateExplorer.OPERATE_EXPLORER_HOME_WEBSITE_SIMPLE_KEY ) )
			{
				homeWebsite = json.getString( OperateExplorer.OPERATE_EXPLORER_HOME_WEBSITE_SIMPLE_KEY );
			}
			OperateExplorer.notifyExplorerSwitch( enableOperateExplorer , homeWebsite );
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
	}
	//xiatian add end
	//gaominghui add start //需求：支持后台运营音乐页和相机页
	private void notifyMediaPluginConfig()
	{
		try
		{
			JSONObject json = new JSONObject( c6 );
			if( json.has( OperateMediaPluginDataManager.OPERATE_CAMERAPAGE_SWITCH_KEY ) )
			{
				boolean enableShowCameraPage = json.getBoolean( OperateMediaPluginDataManager.OPERATE_CAMERAPAGE_SWITCH_KEY );
				OperateMediaPluginDataManager.notifyCameraPageSwitch( enableShowCameraPage );
			}
			if( json.has( OperateExplorer.OPERATE_EXPLORER_HOME_WEBSITE_SIMPLE_KEY ) )
			{
				boolean enableShowMusicPage = json.getBoolean( OperateMediaPluginDataManager.OPERATE_MUSICPAGE_SWITCH_KEY );
				OperateMediaPluginDataManager.notifyMusicPageSwitch( enableShowMusicPage );
			}
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
	}
	//gaominghui add end
}
