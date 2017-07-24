package cool.sdk.Uiupdate;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.framework.utils.signer.SignerUtil;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.update.UpdateDownloadManager;
import com.cooee.update.UpdateNotificationManager;
import com.cooee.update.UpdateUiManager;
import com.cooee.update.UpdateUtil;
import com.cooee.update.taskManager.Listener;
import com.cooee.update.taskManager.Task;
import com.cooee.update.taskManager.TaskManager;
import com.cooee.update.taskManager.TaskResult;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.DlMethod;
import cool.sdk.download.manager.dl_cb;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_result;
import cool.sdk.download.manager.dl_task;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public class UiupdateUpdate extends UpdateHelper
{
	
	private static String TAG = "UpdateUi.COOL";
	private static final String NEW_CONFIG = "uiupdateNewConfig";
	protected static final String ACTION_CONFIG_REQUEST = "3004";
	protected static final String ACTION_ERRORLOG_REQUEST = "3005";
	private UiupdateData newData = new UiupdateData();
	private String curMD5 = null;
	private boolean canNotify = false;
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
	private CoolDLMgr dlMgrApp;
	private Object dlMgrAppSync = new Object();
	
	protected UiupdateUpdate(
			Context context )
	{
		super( context , Uiupdate.h13 , config );
		// TODO Auto-generated constructor stub
		this.context = context;
		initData();
	}
	
	private void getCoolDLMgrApp()
	{
		synchronized( dlMgrAppSync )
		{
			if( dlMgrApp == null )
			{
				dlMgrApp = Uiupdate.CoolDLMgr( context , "DLAUNCHER" );
				dlMgrApp.dl_mgr.setMaxConnectionCount( 3 );
				//dlMgrApp.dl_mgr.setDownloadPath( dlMgrApp.getExternalPath()/*.getInternalPath() */);
				dlMgrApp.setCheckPathEverytime( false );
			}
		}
	}
	
	private void initData()
	{
		String content = this.getString( NEW_CONFIG , null );
		if( content != null )
		{
			try
			{
				JSONObject json = new JSONObject( content );
				newData.setUiupdateData( json );
				newData.setHasDown( json.optBoolean( "hasDown" ) );
				// zhangjin@2015/12/16 ADD START
				checkDownloadFile();
				// zhangjin@2015/12/16 ADD END
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 验证文件的完整性
	 */
	private void checkDownloadFile()
	{
		String path = getApkPath();
		File file = new File( path );
		boolean needReset = false;
		if( file == null || !file.exists() || !file.isFile() || file.length() <= 0 )
		{
			needReset = true;
		}
		if( !needReset )
		{
			dl_info info = dlMgrApp.UrlGetInfo( newData.getR5() );
			if( newData.isHasDown() == true && info != null && info.getCurBytes() != file.length() )
			{
				needReset = true;
			}
		}
		if( needReset )
		{
			newData.setHasDown( false );
			setValue( NEW_CONFIG , newData.toJSON().toString() );
		}
	}
	
	private final String errorFilePath = "/cooee/launcher/errorlog.log";
	
	public String readErrorFile()
	{
		File file = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , errorFilePath ) );
		if( file.exists() )
		{
			InputStreamReader input = null;
			try
			{
				String result = "";
				char[] temBuff = new char[512];
				input = new InputStreamReader( new FileInputStream( file ) );
				int len = 0;
				while( ( len = input.read( temBuff ) ) != -1 )
				{
					String tem = String.valueOf( temBuff );
					if( len < tem.length() )
					{
						tem = tem.substring( 0 , len );
					}
					result += tem;
				}
				return result;
			}
			catch( Exception e )
			{
			}
			finally
			{
				try
				{
					input.close();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public boolean writeErrorFile(
			String content )
	{
		if( TextUtils.isEmpty( content ) )
		{
			return false;
		}
		File file = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , errorFilePath ) );
		FileWriter fw = null;
		BufferedWriter writer = null;
		try
		{
			fw = new FileWriter( file );
			writer = new BufferedWriter( fw );
			writer.write( content );
			writer.flush();
			return true;
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				writer.close();
				fw.close();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public synchronized boolean doErrorLogRequest()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( !DlMethod.IsNetworkAvailable( context ) )
				{
					return;
				}
				String content = readErrorFile();
				if( content == null || content.length() < 2 )
				{
					return;
				}
				try
				{
					JSONObject json = new JSONObject( content );
					JSONArray errorArray = json.getJSONArray( "error" );
					if( errorArray.length() > 5 )
					{
						JSONObject destJson = new JSONObject();
						destJson.put( "versionCode" , json.getInt( "versionCode" ) );
						destJson.put( "versionName" , json.getString( "versionName" ) );
						destJson.put( "sn" , json.getString( "sn" ) );
						destJson.put( "appID" , json.getString( "appID" ) );
						JSONArray destArray = new JSONArray();
						for( int i = errorArray.length() - 1 ; i > errorArray.length() - 6 ; i-- )
						{
							destArray.put( errorArray.get( i ) );
						}
						destJson.put( "error" , destArray );
						content = destJson.toString();
					}
					JSONObject reqJson = JsonUtil.NewRequestJSON( context , Uiupdate.h12 , Uiupdate.h13 );
					reqJson.put( "Action" , ACTION_CONFIG_REQUEST );
					reqJson.put( "errlog" , content );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "UiUpdate Error req:" , reqJson.toString() ) );
					ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
					if( result.exception != null )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( TAG , StringUtils.concat( "UiUpdate Error req:(error),httpCode:" , result.httpCode , "-exception:" , result.exception.toString() ) );
						return;
					}
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "UiUpdate Error req,httpCode:" , result.httpCode , "-content:" , result.content ) );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		} ).start();
		return true;
	}
	
	private synchronized boolean doRequestConfig(
			int dis ,
			int front ,
			String str ) throws Exception
	{
		if( !DlMethod.IsNetworkAvailable( context ) )
		{
			return false;
		}
		//cheyingkun add start	//自更新请求数据统计
		//请求数据
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( context , str );
		}
		//cheyingkun add end
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , Uiupdate.h12 , Uiupdate.h13 );
		reqJson.put( "Action" , ACTION_CONFIG_REQUEST );
		reqJson.put( "p1" , dis );
		reqJson.put( "p2" , front );
		reqJson.put( "p3" , Locale.getDefault().toString() );
		reqJson.put( "p4" , getCurMD5() );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "UiUpdate req:" , reqJson.toString() ) );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "UiUpdate rsp:(error),httpCode:" , result.httpCode , "-exception:" , result.exception.toString() ) );
			return false;
		}
		//cheyingkun add start	//自更新请求数据统计
		//请求数据成功
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			if( UmengStatistics.UPDATE_BY_SELF_AUTO_REQUEST.endsWith( str ) )
			{
				MobclickAgent.onEvent( context , UmengStatistics.UPDATE_BY_SELF_AUTO_REQUEST_SUCCESS );
			}
			else if( UmengStatistics.UPDATE_BY_SELF_MANUAL_REQUEST.endsWith( str ) )
			{
				MobclickAgent.onEvent( context , UmengStatistics.UPDATE_BY_SELF_MANUAL_REQUEST_SUCCESS );
			}
		}
		//cheyingkun add end
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "UiUpdate rsp,httpCode:" , result.httpCode , "-content:" , result.content ) );
		JSONObject resJson = new JSONObject( result.content );
		int retcode = resJson.optInt( "retcode" );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "doRequestConfig resJson retcode=" , retcode ) );
		if( retcode == 0 )
		{
			String r5 = resJson.optString( "r5" );
			long r3 = resJson.optLong( "r3" );
			if( r5 != null && !r5.equals( "" ) && r3 > getCurVersionCode() )
			{
				synchronized( UiupdateUpdate.class )
				{//有新版本
					//cheyingkun add start	//自更新请求数据统计
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					{
						if( UmengStatistics.UPDATE_BY_SELF_AUTO_REQUEST.endsWith( str ) )
						{
							MobclickAgent.onEvent( context , UmengStatistics.UPDATE_BY_SELF_AUTO_REQUEST_HAS_NEW_VERSION );
						}
						else if( UmengStatistics.UPDATE_BY_SELF_MANUAL_REQUEST.endsWith( str ) )
						{
							MobclickAgent.onEvent( context , UmengStatistics.UPDATE_BY_SELF_MANUAL_REQUEST_HAS_NEW_VERSION );
						}
					}
					//cheyingkun add end
					canNotify = true;
					config.UPDATE_DEFAULT_MINUTES = resJson.optLong( "r2" );
					newData.setUiupdateData( resJson );
					if( r3 > newData.getR3() )
					{
						newData.setHasDown( false );
					}
					this.setValue( NEW_CONFIG , newData.toJSON().toString() );
					// zhangjin@2015/12/10 ADD START
					setGapMinute( newData.getR2() );
					// zhangjin@2015/12/10 ADD END
				}
			}
			return true;
		}
		else if( retcode == 100 )
		{
			newData.setUiupdateData( resJson );
			setValue( NEW_CONFIG , newData.toJSON().toString() );
			setGapMinute( newData.getR2() );
			UpdateNotificationManager.getInstance().resetAllPrompt();
			// zhangjin@2016/01/11 减少联网次数 ADD START
			UpdateDownloadManager.getInstance( context ).resetAllPrompt();
			return true;
			// zhangjin@2016/01/11 ADD END		
		}
		return false;
	}
	
	abstract class MyAPPCoolDLCallback extends dl_cb implements CoolDLCallback
	{
	}
	
	@Override
	protected boolean OnUpdate(
			Context arg0 ) throws Exception
	{
		// 自动检测更新
		if( doRequestConfig( isDisplay() , 0 , UmengStatistics.UPDATE_BY_SELF_AUTO_REQUEST ) == false )
		{
			return false;
		}
		String r5 = newData.getR5();
		long r3 = newData.getR3();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "UiUpdate r5:" , r5 ) );
			Log.v( TAG , StringUtils.concat( "UiUpdate r3:" , r3 , "-r12:" , newData.getR12() ) );
		}
		if( !newData.isHasDown() && newData.getR8() == 1 && DlMethod.IsWifiConnected( context ) )
		{
			getCoolDLMgrApp();
			MyAPPCoolDLCallback dlCallback = new MyAPPCoolDLCallback() {
				
				@Override
				public void onDoing(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "UiUpdate onDoing" );
				}
				
				@Override
				public void onFail(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "UiUpdate onFail" );
				}
				
				@Override
				public void onSuccess(
						CoolDLResType arg0 ,
						String arg1 ,
						dl_info arg2 )
				{
					// TODO Auto-generated method stub
					downSuccess();
				}
				
				@Override
				public void onDoing(
						dl_info arg0 ) throws Exception
				{
					// TODO Auto-generated method stub
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "UiUpdate onDoing2" );
				}
				
				@Override
				public void onFail(
						dl_result arg0 ,
						dl_info arg1 ) throws Exception
				{
					// TODO Auto-generated method stub
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "UiUpdate ononFail2" );
				}
				
				@Override
				public void onStart(
						dl_info arg0 ) throws Exception
				{
					// TODO Auto-generated method stub
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "UiUpdate onStart" );
				}
				
				@Override
				public void onSuccess(
						dl_info arg0 ) throws Exception
				{
					// TODO Auto-generated method stub
					downSuccess();
					// zhangjin@2015/12/04 ADD START
					if( newData.getR12() == 1 )
					{
						UpdateUtil.InstallPmApk( context , getApkPath() );
					}
					// zhangjin@2015/12/04 ADD END
				}
			};
			dl_task task = dlMgrApp.UrlNewTask( r5 , dlCallback );
			task.setPath( getApkPath() );
			dlMgrApp.UrlDownload( task );
			//dlMgrApp.UrlDownload( r5 , dlCallback );
		}
		if( canNotify || newData.getR3() > getCurVersionCode() )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "UiUpdate notify launcher " );
			canNotify = false;
			long updateVersion = newData.getR3();
			int display = newData.getR1();
			notifyLauncher( display );
			//更新版本提示。
			UpdateNotificationManager.getInstance().startUpdatePrompt( updateVersion , display );
		}
		// zhangjin@2015/12/04 ADD START	
		if( newData.getR12() == 1 && newData.getR8() == 1 )
		{
			if( newData.isHasDown() )
			{
				UpdateUtil.InstallPmApk( context , getApkPath() );
			}
		}
		// zhangjin@2015/12/04 ADD END
		return true;
	}
	
	private String getPath()
	{
		if( Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) )
		{
			return dlMgrApp.getExternalPath();
		}
		else
		{
			return dlMgrApp.getInternalPath();
		}
	}
	
	private long getCurVersionCode()
	{
		return Long.parseLong( UpdateUiManager.getInstance().getVersionCode() );
	}
	
	private String getCurMD5()
	{
		if( curMD5 == null )
		{
			curMD5 = SignerUtil.getSignerMD5( context );
		}
		//记得去掉
		//curMD5 = "76f2686613e97f72a8ea2cd457189896";
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( " md5 of application Signer is " , curMD5 ) );
		return curMD5;
	}
	
	/*
	 * launcher前台点击菜单
	 * -1:已经是最新版本，0:未下载，1:已经下载好
	 */
	public int doUiupdateFront() throws Exception
	{
		//手动请求更新
		if( doRequestConfig( isDisplay() , 1 , UmengStatistics.UPDATE_BY_SELF_MANUAL_REQUEST ) == false )
		{
			return -1;
		}
		String url = newData.getR5();
		if( url != null && !url.equals( "" ) && newData.getR3() > getCurVersionCode() )
		{
			// zhangjin@2015/12/14 UPD START
			//UpdateNotificationManager.getInstance().startUpdatePrompt( newData.getR3() , isDisplay() );
			UpdateNotificationManager.getInstance().startUpdatePrompt( newData.getR3() , newData.getR1() );
			// zhangjin@2015/12/14 UPD END
			if( newData.isHasDown() )
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
		return -1;
	}
	
	public void downSuccess()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "UiUpdate down success" );
		newData.setHasDown( true );
		setValue( NEW_CONFIG , newData.toJSON().toString() );
		// zhangjin@2015/12/10 ADD START
		setGapMinute( newData.getR2() );
		// zhangjin@2015/12/10 ADD END
	}
	
	//这个是luancher配置的值，是否有显性菜单
	private int isDisplay()
	{
		return UpdateUiManager.getInstance().isDisplay();
	}
	
	private void notifyLauncher(
			int disPlay )
	{
		UpdateUiManager.getInstance().notifyLauncher( disPlay );
	}
	
	/**
	 * 判断新版本是否下载下来
	 * @return
	 */
	public boolean newDataHasDown()
	{
		if( newData == null )
		{
			return false;
		}
		checkDownloadFile();
		return newData.isHasDown() || ( getDownProgress() == 100 );
	}
	
	/**
	 * 获取更新说明json格式
	 * @return
	 */
	public String getUpdateContent()
	{
		if( newData == null )
		{
			return "";
		}
		return newData.getR7();
	}
	
	/**
	 * 获取已经下载的apk路径
	 * @return
	 */
	public String getApkPath()
	{
		getCoolDLMgrApp();
		String path = StringUtils.concat( getPath() , File.separator , newData.getR3() , ".apk" );
		return path;
	}
	
	/**
	 * 用户手动点击下载
	 */
	public void startDownload(
			final Listener listener )
	{
		if( newData == null )
		{
			return;
		}
		String r5 = newData.getR5();
		long r3 = newData.getR3();
		getCoolDLMgrApp();
		MyAPPCoolDLCallback dlCallback = new MyAPPCoolDLCallback() {
			
			@Override
			public void onDoing(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "UiUpdate Front Task onDoing" );
				listener.onProgress( arg2.getCurBytes() * 100 / arg2.getTotalBytes() );
			}
			
			@Override
			public void onFail(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "UiUpdate Front Task onFail" );
				listener.onResult( new TaskResult() );
			}
			
			@Override
			public void onSuccess(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "UiUpdate Front Task  onSuccess" );
				TaskResult ret = new TaskResult();
				ret.mCode = 1;
				listener.onResult( ret );
				downSuccess();
			}
			
			@Override
			public void onDoing(
					dl_info arg0 ) throws Exception
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "UiUpdate Front Task  onDoing2" );
				listener.onProgress( arg0.getCurBytes() * 100 / arg0.getTotalBytes() );
			}
			
			@Override
			public void onFail(
					dl_result arg0 ,
					dl_info arg1 ) throws Exception
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "UiUpdate Front Task ononFail2" );
				listener.onResult( new TaskResult() );
			}
			
			@Override
			public void onStart(
					dl_info arg0 ) throws Exception
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "UiUpdate Front Task onStart" );
			}
			
			@Override
			public void onSuccess(
					dl_info arg0 ) throws Exception
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "UiUpdate Front Task onSuccess2 " );
				TaskResult ret = new TaskResult();
				ret.mCode = 1;
				listener.onResult( ret );
				downSuccess();
			}
		};
		dl_task task = dlMgrApp.UrlNewTask( r5 , dlCallback );
		task.setPath( getApkPath() );
		dlMgrApp.UrlDownload( task );
	}
	
	/**
	 * 用户手动点击取消下载
	 */
	public void stopDownload()
	{
		getCoolDLMgrApp();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "stopDownload" );
		newData.setHasDown( false );
		TaskManager.execute( new Task() {
			
			@Override
			public void runInBack()
			{
				// TODO Auto-generated method stub
				dlMgrApp.UrlStop( newData.getR5() , true );
				setValue( NEW_CONFIG , newData.toJSON().toString() );
				// zhangjin@2015/12/10 ADD START
				setGapMinute( newData.getR2() );
				// zhangjin@2015/12/10 ADD END
			}
		} );
	}
	
	/**
	 * 暂停下载
	 */
	public void pauseDownload()
	{
		getCoolDLMgrApp();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "stopDownload" );
		newData.setHasDown( false );
		TaskManager.execute( new Task() {
			
			@Override
			public void runInBack()
			{
				// TODO Auto-generated method stub
				dlMgrApp.UrlStop( newData.getR5() , false );
				setValue( NEW_CONFIG , newData.toJSON().toString() );
				// zhangjin@2015/12/10 ADD START
				setGapMinute( newData.getR2() );
				// zhangjin@2015/12/10 ADD END
			}
		} );
	}
	
	/**
	 * 获取下载进度
	 * @return
	 */
	public int getDownProgress()
	{
		getCoolDLMgrApp();
		dl_info info = dlMgrApp.UrlGetInfo( newData.getR5() );
		if( info != null && info.getTotalBytes() != 0 )
		{
			return (int)( info.getCurBytes() * 100 / info.getTotalBytes() );
		}
		else
		{
			return 0;
		}
	}
}
