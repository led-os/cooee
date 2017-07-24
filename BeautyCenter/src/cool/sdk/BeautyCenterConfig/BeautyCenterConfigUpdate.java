package cool.sdk.BeautyCenterConfig;


import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.manager.DlMethod;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


/**
 * 运营桌面开关    
 * 请求     Action：3705
 * @author gaominghui
 *
 */
public abstract class BeautyCenterConfigUpdate extends UpdateHelper
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
	private int c0 = 0; //是否显示“点传文件夹”的开关。0关闭；1开启。
	private int c2 = 0; //强制开启/关闭（暂时无用）
	private int c3 = 0; //是否使用“友盟统计”的开关。0关闭；1开启。
	
	protected BeautyCenterConfigUpdate(
			Context context )
	{
		super( context , BeautyCenterConfig.h13 , config );
		// TODO Auto-generated constructor stub
		this.context = context;
		c0 = this.getInt( "c0" , 0 );
		c2 = this.getInt( "c2" , 0 );
		c3 = this.getInt( "c3" , 0 );
	}
	
	static int adhajd = 0;
	
	@Override
	protected boolean OnUpdate(
			Context context ) throws Exception
	{
		if( !DlMethod.IsNetworkAvailable( context ) )
		{
			return false;
		}
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , BeautyCenterConfig.h12 , BeautyCenterConfig.h13 );
		reqJson.put( "Action" , ACTION_CONFIG_REQUEST );
		reqJson.put( "p1" , "com.cooee.unilauncher" );//p1-产品名字(包名)
		//p2无法区分为哪个开关的状态，逻辑有问题，待修改
		reqJson.put( "p2" , getCurStatus( context ) );//p2-产品状态(开/关)
		Log.v( "COOL" , "BeautyCenterConfigUpdate req:" + reqJson.toString() );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			Log.v( "COOL" , "BeautyCenterConfigUpdate rsp:(error)" + result.httpCode + " " + result.exception );
			return false;
		}
		Log.v( "COOL" , "BeautyCenterConfigUpdate rsp:" + result.httpCode + " " + result.content );
		JSONObject resJson = new JSONObject( result.content );
		int retcode = resJson.optInt( "rc0" );
		if( retcode == 0 )//有更新记录
		{
			config.UPDATE_DEFAULT_MINUTES = resJson.optLong( "c1" );
			c0 = resJson.optInt( "c0" );
			c2 = resJson.optInt( "c2" );
			c3 = resJson.optInt( "c3" );
			notifyBeautyCenterConfig();
			setValue( "c0" , c0 );
			setValue( "c2" , c2 );
			setValue( "c3" , c3 );
			setValue( "result.content" , result.content );
			return true;
		}
		else if( retcode == 200 )//全关 
		{
			c0 = 0;
			c2 = 0;
			c3 = 0;
			notifyBeautyCenterConfig();
			setValue( "c0" , c0 );
			setValue( "c2" , c2 );
			setValue( "c3" , c3 );
			setValue( "result.content" , "" );
			return true;
		}
		else if( retcode == 100 )//无更新
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//待修改
	private int getCurStatus(
			Context context )
	{
		return 0;
	}
	
	private void notifyBeautyCenterConfig()
	{//通知launcher
		Log.v( "COOL" , "LauncherConfigUpdate c0:" + c0 + "-c2:" + c2 + "-c3:" + c3 );
		OperateUmeng.notifyUmengSwitch( c3 == 0 ? false : true ); //运营友盟（详见“OperateUmeng”中说明）
	}
}
