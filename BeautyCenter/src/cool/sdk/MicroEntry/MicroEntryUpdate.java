package cool.sdk.MicroEntry;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import android.content.Context;
import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.CoolMethod;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_cb;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_result;
import cool.sdk.log.CoolLog;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public abstract class MicroEntryUpdate extends UpdateHelper
{
	
	static UpdateConfig config;
	static
	{
		config = new UpdateConfig();
		config.UPDATE_DEFAULT_MINUTES = 4 * 24 * 60;// 默认更新间隔
		config.UPDATE_MIN_MINUTES = 8 * 60;// 最小更新间隔
		config.UPDATE_MAX_MINUTES = 30 * 24 * 60;// 最大更新间隔
		config.MAX_UPDATE_TIMES_PER_DAY = 3;// 每天最大更新次数
		config.RETRY_TIMES_WHEN_ONLINE = 2;//有网络下的重试次数
	};
	protected Context context;
	protected CoolLog Log;
	static final float CONST_SUCCESS_RATE = 0.0f;
	
	protected MicroEntryUpdate(
			Context context )
	{
		super( context , MicroEntry.h13 , config );
		// TODO Auto-generated constructor stub
		this.context = context;
		Log = new CoolLog( context );
	}
	
	public abstract void OnDataChange() throws Exception;
	
	public abstract String getEntryID() throws Exception;
	
	abstract class MyIconCoolDLCallback extends dl_cb implements CoolDLCallback
	{
		
		public int successCount = 0;//本次成功个数
		public int failCount = 0;//本次失败个数
		public int downloadCount = 0;//本次下载个数
		public int totalCount = 0;//总个数
	}
	
	CoolDLMgr dlMgrIcon;
	Object dlMgrIconSync = new Object();
	
	public CoolDLMgr getCoolDLMgrIcon()
	{
		synchronized( dlMgrIconSync )
		{
			if( dlMgrIcon == null )
			{
				dlMgrIcon = MicroEntry.CoolDLMgr( context , "MICON" , 0 );
				dlMgrIcon.dl_mgr.setMaxConnectionCount( 3 );
				dlMgrIcon.dl_mgr.setDownloadPath( dlMgrIcon.getInternalPath() );
				dlMgrIcon.setCheckPathEverytime( false );
			}
		}
		return dlMgrIcon;
	}
	
	private static final String ACTION_FOLDER_REQUEST = "3200";
	//private static final String ACTION_MSG_COLLECT = "3000";
	public static final int PLAFORM_VERSION = 1;
	private static final String DEFAULT_VERSION = "0";
	
	@Override
	protected boolean OnUpdate(
			Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		Log.v( "COOL" , "OnUpdate" );
		getCoolDLMgrIcon();
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , MicroEntry.h12 , MicroEntry.h13 );
		reqJson.put( "Action" , ACTION_FOLDER_REQUEST );
		reqJson.put( "p1" , CoolMethod.getTotalCallTime( context ) );
		reqJson.put( "p2" , CoolMethod.getCallTimes( context ) );
		reqJson.put( "p3" , CoolMethod.getSmsNum( context ) );
		reqJson.put( "p4" , getString( "c2" , DEFAULT_VERSION ) );//配置时间戳
		reqJson.put( "p5" , getString( "c3" , DEFAULT_VERSION ) );//列表时间戳（就是列表文件版本号）
		reqJson.put( "p6" , PLAFORM_VERSION );//微入口平台版本号
		reqJson.put( "p7" , getEntryID() );//显示入口ID
		reqJson.put( "p8" , CoolMethod.getInstallAppCount( context ) );//安装应用个数
		reqJson.put( "p9" , CoolMethod.getAppActiveTime( context ) );//应用激活时间
		reqJson.put( "p10" , CoolMethod.getPhoneTotalUseTime( context ) );//手机总开机时间
		Log.v( "COOL" , "MicroEntryUpdate req:" + reqJson.toString() );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			Log.v( "COOL" , "MicroEntryUpdate rsp:(error)" + result.httpCode + " " + result.exception );
			return false;
		}
		Log.v( "COOL" , "MicroEntryUpdate rsp:" + result.httpCode + " " + result.content );
		JSONObject resJson = new JSONObject( result.content );
		int rc0 = resJson.getInt( "rc0" );
		int rl0 = resJson.getInt( "rl0" );
		if( rc0 == 0 )
		{
			JSONObject config = resJson.getJSONObject( "config" );
			if( config.has( "c0" ) )//免责处理
			{
				setValue( "c0" , config.getInt( "c0" ) );
			}
			if( config.has( "c1" ) )//更新间隔，同文件夹的参数
			{
				setGapMinute( config.getInt( "c1" ) );
			}
			if( config.has( "c2" ) )//配置时间戳
			{
				setValue( "c2" , config.getString( "c2" ) );
			}
		}
		else if( rc0 == 200 )
		{
			setGapMinute( config.UPDATE_DEFAULT_MINUTES );
			setValue( "c2" , DEFAULT_VERSION );
			setValue( "c3" , DEFAULT_VERSION );
		}
		final JSONObject list;
		do
		{
			if( rl0 == 0 )
			{
				JSONObject config = resJson.getJSONObject( "config" );
				if( config.has( "c3" ) )//列表时间戳
				{
					setValue( "c3" , config.getString( "c3" ) );
				}
				//取服务器的配置
				if( resJson.has( "list" ) )
				{
					list = resJson.getJSONObject( "list" );
					//保存服务器的配置
					setValue( "resJson.list" , list.toString() );
					break;
				}
				else
				{
					setValue( "resJson.list" , (String)null );
				}
			}
			else if( rl0 == 100 )
			{
				String resJson_toString = getListString();
				//取存储的配置
				if( resJson_toString != null && resJson_toString.length() > 0 )
				{
					list = new JSONObject( resJson_toString );
					break;
				}
			}
			else if( rl0 == 200 )
			{
				//清空服务器的配置
				setValue( 1 + "r5" , 0 );
				setValue( 2 + "r5" , 0 );
				setValue( 3 + "r5" , 0 );
				setValue( 4 + "r5" , 0 );
				setValue( 1 + "r8" , (String)null );
				setValue( 2 + "r8" , (String)null );
				setValue( 3 + "r8" , (String)null );
				setValue( 4 + "r8" , (String)null );
				setValue( "resJson.list" , (String)null );
				Log.v( "COOL" , "onMyClear!" );
				OnDataChange();
				return true;
			}
			list = null;
		}
		while( false );
		if( list != null )
		{
			Set<String> urlSet = new HashSet<String>();
			Iterator<?> keys = (Iterator<?>)list.keys();
			while( keys.hasNext() )
			{
				String key = (String)keys.next();
				JSONObject item = list.getJSONObject( key );
				//	r1	list	数字	入口ID [入口ID是客户端标示或者定位入口的唯一ID。]
				//	r2	list	对象，字符	英文名称
				//	r3	list	对象，字符	中文名称
				//	r4	list	对象，字符	繁体名称
				//	r5	list	对象，数字	应用程序列表 0:不显示 1:显示
				//	r6	list	对象，数字	桌面 0:不显示 1:显示
				//	r7	list	对象，字符	图标地址url
				//	r8	list	对象，字符	入口url
				//	r9	list	对象，数字	快捷方式显示屏幕位置x
				//	r10	list	对象，数字	快捷方式显示屏幕位置y
				int r1 = item.getInt( "r1" );
				String r2 = item.getString( "r2" );
				String r3 = item.getString( "r3" );
				String r4 = item.getString( "r4" );
				int r5 = item.getInt( "r5" );
				int r6 = item.getInt( "r6" );
				String r7 = item.getString( "r7" );
				String r8 = item.getString( "r8" );
				int r9 = item.getInt( "r9" );
				int r10 = item.getInt( "r10" );
				setValue( r1 + "r5" , r5 );
				setValue( r1 + "r8" , r8 );
				//visible[r1] = r5 == 1;
				if( !urlSet.contains( r7 ) )
				{
					urlSet.add( r7 );
				}
			}
			MyIconCoolDLCallback iconDownloadCB = new MyIconCoolDLCallback() {
				
				static final float successRate = CONST_SUCCESS_RATE;
				
				private void mySuccessCheck()
				{
					if( successCount + failCount != downloadCount )
					{
						return;
					}
					Log.v( "COOL" , "MicroEntry mySuccessCheck:" + successCount + " " + failCount + " " + downloadCount + " " + totalCount + " " + ( 1.0f - (float)failCount / (float)totalCount ) );
					if( (float)failCount / (float)totalCount > 1.0f - successRate )
					{
						return;
					}
					try
					{
						OnDataChange();
					}
					catch( Exception e )
					{
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
				
				@Override
				public void onSuccess(
						CoolDLResType type ,
						String name ,
						dl_info info )
				{
					// TODO Auto-generated method stub
					successCount++;
					Log.v( "COOL" , "MicroEntry iconDownloadCB success:" + name );
					mySuccessCheck();
				}
				
				@Override
				public void onFail(
						CoolDLResType type ,
						String name ,
						dl_info info )
				{
					// TODO Auto-generated method stub
					failCount++;
					Log.v( "COOL" , "MicroEntry iconDownloadCB fail:" + name );
					mySuccessCheck();
				}
				
				@Override
				public void onDoing(
						CoolDLResType type ,
						String name ,
						dl_info info )
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onStart(
						dl_info info ) throws Exception
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onDoing(
						dl_info info ) throws Exception
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onSuccess(
						dl_info info ) throws Exception
				{
					// TODO Auto-generated method stub
					successCount++;
					Log.v( "COOL" , "MicroEntry iconDownloadCB success:" + info.getURL() );
					mySuccessCheck();
				}
				
				@Override
				public void onFail(
						dl_result result ,
						dl_info info ) throws Exception
				{
					// TODO Auto-generated method stub
					failCount++;
					Log.v( "COOL" , "MicroEntry iconDownloadCB fail:" + info.getURL() );
					mySuccessCheck();
				}
			};
			iconDownloadCB.totalCount = urlSet.size();
			//处理url图标
			Iterator<String> iterator = urlSet.iterator();
			while( iterator.hasNext() )
			{
				String url = iterator.next();
				//下载url图标
				dl_info info = dlMgrIcon.UrlGetInfo( url );
				if( info == null || !info.IsDownloadSuccess() )
				{
					iconDownloadCB.downloadCount++;
				}
				else
				{
					iterator.remove();
					Log.v( "COOL" , "DynamicEntry iconDownloadCB already exists:" + url );
				}
			}
			for( String url : urlSet )
			{
				dlMgrIcon.UrlDownload( url , iconDownloadCB );
			}
			if( iconDownloadCB.downloadCount == 0 )
			{
				OnDataChange();
			}
		}
		else
		{
			OnDataChange();
		}
		return true;
	}
	
	public String getListString()
	{
		return getString( "resJson.list" );
	}
	
	public String getEntryUrl(
			int entryId )
	{
		return getString( entryId + "r8" );
	}
	//	
	//	private static void setEnabled(
	//			Context context ,
	//			Class<?> mClass ,
	//			boolean isenable )
	//	{
	//		if( !isenable )
	//		{
	//			setComponentEnabled( context , new ComponentName( context , mClass ) , PackageManager.COMPONENT_ENABLED_STATE_DISABLED );
	//		}
	//		else
	//		{
	//			setComponentEnabled( context , new ComponentName( context , mClass ) , PackageManager.COMPONENT_ENABLED_STATE_ENABLED );
	//		}
	//	}
	//	
	//	private static void setComponentEnabled(
	//			Context context ,
	//			ComponentName compName ,
	//			int newStat )
	//	{
	//		PackageManager pkgMgr = context.getPackageManager();
	//		if( pkgMgr.getComponentEnabledSetting( compName ) != newStat )
	//		{
	//			pkgMgr.setComponentEnabledSetting( compName , newStat , PackageManager.DONT_KILL_APP );
	//		}
	//	}
}
