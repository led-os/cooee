package com.cooee.framework.function.DynamicEntry;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUnInstall.UnInstallItem;
import com.cooee.framework.function.DynamicEntry.Dialog.DynamicEntryUpdateWaiteDialog;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;

import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.DynamicEntry.DynamicEntryLog;
import cool.sdk.DynamicEntry.DynamicEntryUpdate;


public class OperateDynamicProxy
{
	
	private static final String PREFERENCE_KEY = "DynamicEntry";
	private static final String PREFERENCE_DEFAULT = "defaultEntry";
	private static final String PREFERENCE_DEFAULT_IDANDNAME = "defaultIDAndName";
	private static final String PREFERENCE_DYNAMIC = "dynamicContent";
	private static final String PREFERENCE_DISCLAIMER = "disclaimer";//免责申明
	private static final String PREFERENCE_PERTIME = "PerTime";//上次存储时间
	private static final String PREFERENCE_NOTIFY_COUNT = "NotifyCount";//消息弹出次数
	private static final String PREFERENCE_ENTRY_REGISTER = "Entry_register";
	private static final String PREFERENCE_LIST_VERSION = "PREFERENCE_LIST_VERSION";//当前列表版本号
	private static final String PREFERENCE_NEW_DATA = "haveNewData";//新来的数据是否已经被处理
	private static final String PREFERENCE_DYNAMIC_NOTIFY = "dynamic_notify";
	public static String DISCLAIMER_NOTIFY_ACTIVITY = "com.cooee.phenix.Functions.DynamicEntry.DynamicEntryServiceActivity";
	private static OperateDynamicProxy instance;
	public static Context context;
	private static SharedPreferences preferences;
	public static String custom_sn = null;
	private OperateDynamicContentParser mParse;
	private OperateDynamicCallback mClientCallback;
	private OperateDynamicClient client;
	private OperateDynamicUnInstall mUnInstall;
	private boolean bBuildElements = false;
	private static ArrayList<OperateDynamicData> mAllData;
	private static boolean bFirstProcess = true;
	private final Object mLock = new Object();
	private static final int MAX_LOOPS = 10;
	private static final int HAS_NEWDATA = 100;
	private static final int NOT_NEWDATA = 1;
	private DynamicEntryUpdateWaiteDialog mDynamicUpdateWaiteDialog;
	
	private OperateDynamicProxy()
	{
		//context = _context;
		//preferences = context.getSharedPreferences( PREFERENCE_KEY , Context.MODE_PRIVATE );
	}
	
	// 将Context从原来的Activity升级到Application,这样保证在launcher activity没有启动的时候也能够将后台更新的数据保存起来
	public static void initDynamicProxy(
			Context ctx )
	{
		context = ctx;
		preferences = context.getSharedPreferences( PREFERENCE_KEY , Context.MODE_PRIVATE );
	}
	
	public OperateDynamicClient getOperateDynamicClient()
	{
		return client;
	}
	
	private synchronized void notifyOnDataChanged(
			final List<OperateDynamicData> receiveList )
	{
		if( client == null || receiveList.size() == 0 )
		{
			return;
		}
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				int loopCount = 0;
				client.onCreateDynamicInfo( receiveList , DynamicEntryHelper.getInstance( context ).getListVersion() );
				while( true )
				{
					if( loopCount > MAX_LOOPS )
					{
						setNewDataFlag( HAS_NEWDATA );
						break;
					}
					if( client.onDynamicDataChange() )
					{
						setNewDataFlag( NOT_NEWDATA );
						break;
					}
					loopCount++;
					try
					{
						Thread.sleep( 5000 );
					}
					catch( InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} ).start();
	}
	
	public void dealDismissDisclaimer()
	{
		if( preferences.getBoolean( PREFERENCE_DYNAMIC_NOTIFY , false ) )
		{
			showDisclaimerNotify( DynamicEntryHelper.getInstance( context ).getInt( PREFERENCE_DISCLAIMER , -1 ) );
		}
	}
	
	public void dealNewData()
	{
		if( context == null )
		{
			return;
		}
		if( mParse == null )
		{
			return;
		}
		if( haveNewData() )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "BACK" , "dealNewData haveNewData" );
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					String parseString = DynamicEntryHelper.getInstance( context ).getListString();
					JSONObject newJsonObject = null;
					if( parseString != null )
					{
						try
						{
							newJsonObject = new JSONObject( parseString );
						}
						catch( JSONException e )
						{
							return;
						}
					}
					List<OperateDynamicData> receiveList = mParse.parseDynamicData( context , newJsonObject , false );
					if( receiveList.size() > 0 )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "BACK" , "dealNewData haveNewData 1111" );
						List<OperateDynamicData> update = mParse.parseContent( receiveList );
						if( update != null && update.size() > 0 )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "BACK" , "dealNewData haveNewData 2222" );
							notifyOnDataChanged( update );
						}
					}
				}
			} ).start();
		}
	}
	
	private void setNewDataFlag(
			int flag )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "BACK" , StringUtils.concat( "setNewDataFlag flag=" , flag ) );
		preferences.edit().putInt( PREFERENCE_NEW_DATA , flag ).commit();
	}
	
	private boolean haveNewData()
	{
		int hasData = preferences.getInt( PREFERENCE_NEW_DATA , 0 );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "BACK" , StringUtils.concat( "haveNewData  1111 flag=" , hasData ) );
		if( hasData == HAS_NEWDATA )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "BACK" , StringUtils.concat( "haveNewData flag=" , hasData ) );
			return true;
		}
		return false;
	}
	
	public void start(
			OperateDynamicClient client )
	{
		this.client = client;
		//先注掉，后期用到再打开
		//		if( Assets.config != null )
		//		{
		//			try
		//			{
		//				JSONObject config = Assets.config.getJSONObject( "config" );
		//				custom_sn = config.getString( "serialno" );
		//			}
		//			catch( JSONException e )
		//			{
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		}
	}
	
	private void buildElements()
	{
		if( bBuildElements )
		{
			return;
		}
		mParse = new OperateDynamicContentParser( context , this );
		mClientCallback = new OperateDynamicCallback( context , this );
		mUnInstall = new OperateDynamicUnInstall( context , preferences , this );
		mAllData = new ArrayList<OperateDynamicData>();
		bBuildElements = true;
	}
	
	// 将Context升级为Application后，这里的参数context没有用
	public static OperateDynamicProxy getInstance()
	{
		synchronized( OperateDynamicProxy.class )
		{
			if( instance == null )
			{
				instance = new OperateDynamicProxy();
				instance.buildElements();
			}
		}
		return instance;
	}
	
	public void notifyDisclaimerCancel()
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		notificationManager.cancel( notifyID );
	}
	
	public void directlyShow()
	{
		final String updateContent = DynamicEntryHelper.getInstance( context ).getListString();
		synchronized( isparsecontentintoui )
		{
			if( isparsecontentintoui )
			{
				return;
			}
			isparsecontentintoui = true;
		}
		try
		{
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					JSONObject newJsonObject = null;
					if( updateContent != null )
					{
						try
						{
							newJsonObject = new JSONObject( updateContent );
						}
						catch( JSONException e )
						{
							// TODO Auto-generated catch block
							//e.printStackTrace();
							synchronized( isparsecontentintoui )
							{
								isparsecontentintoui = false;
							}
							return;
						}
					}
					List<OperateDynamicData> receiveList = mParse.parseDynamicData( context , newJsonObject , false );
					if( receiveList.size() > 0 )
					{
						cancelDisclaimerNotify();
						preferences.edit().putLong( PREFERENCE_PERTIME , 0 ).commit();
						preferences.edit().putInt( PREFERENCE_NOTIFY_COUNT , 0 ).commit();
						boolean isRegister = preferences.getBoolean( PREFERENCE_ENTRY_REGISTER , false );
						if( !isRegister )
						{
							DynamicEntryLog.LogDynamicEntryActive( context , 1 );
							preferences.edit().putBoolean( PREFERENCE_ENTRY_REGISTER , true ).commit();
						}
						List<OperateDynamicData> update = mParse.parseContent( receiveList );
						if( update != null && update.size() > 0 )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "BACK" , "parseContent notifyOnDataChanged" );
							notifyOnDataChanged( update );
						}
					}
					synchronized( isparsecontentintoui )
					{
						isparsecontentintoui = false;
					}
				}
			} ).start();
		}
		catch( Exception e )
		{
			synchronized( isparsecontentintoui )
			{
				isparsecontentintoui = false;
			}
		}
	}
	
	public void setDisclaimer(
			String config )
	{
		try
		{
			if( config == null )
				return; //为空后强转会catch 貌似有些手机还是会挂 这边先做个保护
			int result = Integer.parseInt( config );
			DynamicEntryHelper.getInstance( OperateDynamicProxy.context ).setValue( PREFERENCE_DISCLAIMER , result );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void processDisclaimer()
	{
		String parseString = DynamicEntryHelper.getInstance( context ).getListString();
		parseContent( parseString );
	}
	
	// DownLoad线程和数据层进行交互接口，新数据来后的解析
	public void parseDynamicUpdateData()
	{
		String curVersion = preferences.getString( PREFERENCE_LIST_VERSION , DynamicEntryUpdate.DEFAULT_VERSION );
		String listVersion = DynamicEntryHelper.getInstance( context ).getListVersion();
		if( !curVersion.equals( listVersion ) )
		{
			String parseString = DynamicEntryHelper.getInstance( context ).getListString();
			{
				parseContent( parseString );
			}
		}
	}
	
	public void parseTestOnly(
			final List<OperateDynamicData> newData )
	{
		if( newData.size() == 0 )
		{
			parseContent( null );
			return;
		}
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				List<OperateDynamicData> update = mParse.parseContent( newData );
				notifyOnDataChanged( update );
			}
		} ).start();
	}
	
	Boolean isparsecontentintoui = false;
	
	private void parseContent(
			final String updateContent )
	{
		synchronized( isparsecontentintoui )
		{
			if( isparsecontentintoui )
			{
				return;
			}
			isparsecontentintoui = true;
		}
		try
		{
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					JSONObject newJsonObject = null;
					if( updateContent != null )
					{
						try
						{
							newJsonObject = new JSONObject( updateContent );
						}
						catch( JSONException e )
						{
							// TODO Auto-generated catch block
							//e.printStackTrace();
							synchronized( isparsecontentintoui )
							{
								isparsecontentintoui = false;
							}
							return;
						}
					}
					List<OperateDynamicData> receiveList = mParse.parseDynamicData( context , newJsonObject , false );
					int disclaimer = DynamicEntryHelper.getInstance( OperateDynamicProxy.context ).getInt( PREFERENCE_DISCLAIMER , 0 );
					boolean isRegister = preferences.getBoolean( PREFERENCE_ENTRY_REGISTER , false );
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "Dynamic" , StringUtils.concat( "Dynamic---免责声明:" , disclaimer ) );
					if( receiveList.size() > 0 )
					{
						boolean display = false;
						//免责申明为1时，直接显示
						if( 1 == disclaimer )
						{
							display = true;
						}
						else
						{
							//没有新增的dynamicid时，直接显示
							boolean hasAdd = false;
							List<OperateDynamicData> oldList = processContent();
							mParse.removeUninstallData( receiveList );
							for( OperateDynamicData newData : receiveList )
							{
								if( newData.mIsShow || newData.mIsDeskShow )
								{
									boolean isFind = false;
									for( OperateDynamicData oldData : oldList )
									{
										if( newData.dynamicID.equals( oldData.dynamicID ) )
										{
											isFind = true;
											break;
										}
									}
									if( !isFind )
									{
										//有新增
										hasAdd = true;
										break;
									}
								}
							}
							if( !hasAdd )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "Dynamic" , "没有新增的Dynamicid时，直接显示" );
								display = true;
							}
						}
						if( display )
						{
							if( !isRegister )
							{
								DynamicEntryLog.LogDynamicEntryActive( context , 2 );
								preferences.edit().putBoolean( PREFERENCE_ENTRY_REGISTER , true ).commit();
							}
						}
						else
						{
							long PerTime = preferences.getLong( PREFERENCE_PERTIME , 0 );
							long CurTime = System.currentTimeMillis() / 1000;//OperateDynamicUtils.getServerCurTime( context );
							int NotifyCount = preferences.getInt( PREFERENCE_NOTIFY_COUNT , 0 );
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "Dynamic" , StringUtils.concat( "Dynamic---免责声明---NotifyCount:" , NotifyCount , "-PerTime:" , PerTime , "-CurTime:" , CurTime ) );
							if( 2 == disclaimer && NotifyCount > 4 )
							{
								display = true;
								if( !isRegister )
								{
									DynamicEntryLog.LogDynamicEntryActive( context , 3 );
									preferences.edit().putBoolean( PREFERENCE_ENTRY_REGISTER , true ).commit();
								}
							}
							else if( 0 == disclaimer && NotifyCount > 9 )
							{
								//啥也不干;
							}
							else
							{
								int Day1 = 24 * 60 * 60;//秒为单位
								int Day3 = Day1 * 3;
								if( CurTime != 0 )
								{
									long time = Math.abs( CurTime - PerTime );
									if( time > Day3 || ( time > Day1 && NotifyCount <= 9 ) )
									{
										preferences.edit().putInt( PREFERENCE_NOTIFY_COUNT , NotifyCount + 1 ).commit();
										preferences.edit().putLong( PREFERENCE_PERTIME , CurTime ).commit();
										//显示消息提示框
										showDisclaimerNotify( disclaimer );
									}
								}
							}
						}
						if( display )
						{
							cancelDisclaimerNotify();
							preferences.edit().putLong( PREFERENCE_PERTIME , 0 ).commit();
							preferences.edit().putInt( PREFERENCE_NOTIFY_COUNT , 0 ).commit();
							List<OperateDynamicData> update = mParse.parseContent( receiveList );
							if( update != null && update.size() > 0 )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "BACK" , "parseContent notifyOnDataChanged" );
								notifyOnDataChanged( update );
							}
						}
					}
					String listVersion = DynamicEntryHelper.getInstance( context ).getListVersion();
					preferences.edit().putString( PREFERENCE_LIST_VERSION , listVersion ).commit();
					synchronized( isparsecontentintoui )
					{
						isparsecontentintoui = false;
					}
				}
			} ).start();
		}
		catch( Exception e )
		{
			synchronized( isparsecontentintoui )
			{
				isparsecontentintoui = false;
			}
		}
	}
	
	private static int notifyID = 20140725;
	private static int mIcon = 0;
	
	public static int getLauncherIcon()
	{
		if( mIcon == 0 )
		{
			mIcon = context.getResources().getIdentifier( "ic_launcher_home" , "mipmap" , context.getPackageName() );
		}
		return mIcon;
	}
	
	public void showDisclaimerNotify(
			int disclaimer )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "Dynamic" , "Dynamic----showDisclaimerNotify" );
		if( disclaimer == -1 )
		{
			return;
		}
		if( mIcon == 0 )
		{
			mIcon = getLauncherIcon();
		}
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		final Notification notification = new Notification( mIcon , BaseDefaultConfig.getString( R.string.op_ml_new_content ) , System.currentTimeMillis() );
		Intent notificationIntent = new Intent();
		notificationIntent.setClassName( context , DISCLAIMER_NOTIFY_ACTIVITY );
		notificationIntent.putExtra( "disclaimer" , disclaimer );
		notificationIntent.putExtra( "pkg" , context.getPackageName() );
		PendingIntent contentItent = PendingIntent.getActivity( context , 0 , notificationIntent , PendingIntent.FLAG_CANCEL_CURRENT );
		notification.setLatestEventInfo( context , BaseDefaultConfig.getString( R.string.op_ml_new_content ) , context.getResources().getString( R.string.op_ml_new_content ) , contentItent );
		notificationManager.notify( notifyID , notification );
		preferences.edit().putBoolean( PREFERENCE_DYNAMIC_NOTIFY , true ).commit();
	}
	
	private void cancelDisclaimerNotify()
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		notificationManager.cancel( notifyID );
		preferences.edit().putBoolean( PREFERENCE_DYNAMIC_NOTIFY , false ).commit();
	}
	
	public String getDefaultDynamicDataContent()
	{
		String content = preferences.getString( PREFERENCE_DEFAULT , null );
		return content;
	}
	
	public String DynamicDataToString(
			List<OperateDynamicData> content )
	{
		JSONObject json = new JSONObject();
		for( OperateDynamicData data : content )
		{
			try
			{
				json.put( data.dynamicID , data.toJSON() );
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String retString = json.toString();
		return retString;
	}
	
	public void saveDynamicContent(
			List<OperateDynamicData> content ,
			boolean bFromDefault )
	{
		synchronized( mLock )
		{
			String savaString = DynamicDataToString( content );
			if( bFromDefault )
			{
				mAllData.clear();
				mAllData.addAll( content );
				bFirstProcess = false;
				removeUninstallDefaultItems( content );
				preferences.edit().putString( PREFERENCE_DEFAULT , savaString ).commit();
			}
			preferences.edit().putString( PREFERENCE_DYNAMIC , savaString ).commit();
		}
	}
	
	public void setDefaultDynamicID(
			String IDAndName )
	{
		preferences.edit().putString( PREFERENCE_DEFAULT_IDANDNAME , IDAndName ).commit();
	}
	
	public String getDefaultDynamicID()
	{
		return preferences.getString( PREFERENCE_DEFAULT_IDANDNAME , "" );
	}
	
	public void addUnInstallItem(
			String dynamicID ,
			String packageName ,
			int from ,
			String folderID ,
			boolean isInstalled )
	{
		mUnInstall.addUnInstallItem( dynamicID , packageName , from , folderID , isInstalled );
	}
	
	public ArrayList<UnInstallItem> getUnInstallItems()
	{
		return mUnInstall.getUnInstallItems();
	}
	
	public void saveUnInstallItems()
	{
		mUnInstall.saveUnInstallItems();
	}
	
	private void removeUninstallDefaultItems(
			List<OperateDynamicData> content )
	{
		mUnInstall.removeDefaultItems( content );
	}
	
	// 对于默认配置的运营文件夹，后台修改的名字不生效
	private void reserveDefaultFolderName(
			List<OperateDynamicData> content )
	{
		String defString = getDefaultDynamicID();
		// id,name,type;id,name,type;id,name,type;...
		if( defString == null )
		{
			return;
		}
		String[] idItems;
		String[] defItems = defString.split( OperateDynamicUtils.DYNAMIC_SEMICOLON );
		for( int i = 0 ; i < defItems.length ; i++ )
		{
			idItems = defItems[i].split( OperateDynamicUtils.DYNAMIC_COMMA );
			if( idItems.length > 0 )
			{
				OperateDynamicData defFolderData = getDynamicFolderData( idItems[0] );
				for( OperateDynamicData data : content )
				{
					if( defFolderData != null && data.dynamicType == OperateDynamicUtils.FOLDER && data.dynamicID.equals( idItems[0] ) )
					{
						data.mDeskName = defFolderData.mDeskName;
						data.mDeskNameCN = defFolderData.mDeskNameCN;
						data.mDeskNameTW = defFolderData.mDeskNameTW;
						data.mName = defFolderData.mName;
						data.mNameCN = defFolderData.mNameCN;
						data.mNameTW = defFolderData.mNameTW;
						break;
					}
				}
			}
		}
	}
	
	public void reSetAlldata(
			List<OperateDynamicData> content ,
			boolean bFromDefault )
	{
		synchronized( mLock )
		{
			reserveDefaultFolderName( content );
			mAllData.clear();
			mAllData.addAll( content );
			saveDynamicContent( content , false );
		}
	}
	
	private List<OperateDynamicData> processContent()
	{
		//String content = DynamicEntryUpdate.getInstance( context ).getString( "dynamicContent" , null );
		synchronized( mLock )
		{
			if( bFirstProcess )
			{
				String content = preferences.getString( PREFERENCE_DYNAMIC , null );
				List<OperateDynamicData> data = (ArrayList<OperateDynamicData>)mParse.processContent( content );
				mAllData.clear();
				mAllData.addAll( data );
				bFirstProcess = false;
			}
			return mAllData;
		}
	}
	
	public OperateDynamicData getDynamicFolderData(
			String folderid )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getDynamicFolderData( list , folderid );
	}
	
	// Client调用数据层的回调接口函数
	public boolean containsApp(
			String pkgname )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.containsApp( list , pkgname );
	}
	
	//桌面是否N标
	public boolean getIsShowDesktopHot(
			String dynamicID )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getIsShowDesktopHot( list , dynamicID );
	}
	
	public boolean getIsShowDesktopAppHot(
			String packageName )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getIsShowDesktopAppHot( list , packageName );
	}
	
	public boolean getIsShowAppMainMenuHot(
			String packageName )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getIsShowAppMainMenuHot( list , packageName );
	}
	
	public boolean getIsShowMainMenuHot(
			String dynamicID )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getIsShowMainMenuHot( list , dynamicID );
	}
	
	public void hideOperateFolderHot(
			String dynamicID ,
			boolean isDesk )
	{
		List<OperateDynamicData> list = processContent();
		mClientCallback.hideOperateFolderHot( list , dynamicID , isDesk );
	}
	
	//文件夹项的名称
	public String getItemLocalTitle(
			String pkgName ,
			String dynamicID )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getItemLocalTitle( list , pkgName , dynamicID );
	}
	
	//wifi1118 start
	public String getDynamicIconTitle(
			String pkgName )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getDynamicIconTitle( list , pkgName );
	}
	
	//wifi1118 end
	//文件夹、虚图标、虚链接的名称
	public String getLocalTitle(
			String dynamicID ,
			boolean isDesk )
	{
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getLocalTitle( list , dynamicID , isDesk );
	}
	
	public static List<OperateDynamicData> parseDynamicData(
			String parseString ,
			boolean isLocal )
	{
		JSONObject newJsonObject = null;
		if( parseString != null )
		{
			try
			{
				newJsonObject = new JSONObject( parseString );
			}
			catch( JSONException e )
			{
				return null;
			}
		}
		return OperateDynamicContentParser.parseDynamicData( context , newJsonObject , isLocal );
	}
	
	public String getListVersion()
	{
		return DynamicEntryHelper.getInstance( context ).getListVersion();
	}
	
	public String getDownloadTip(
			String pkgName )
	{
		// 目前的配置中，服务器没有配置英文或其他语言，仅仅在中文或者繁体语言下下载的时候显示提示语，其他
		// 语种用默认提示
		int cur_lang = OperateDynamicUtils.getCurLanguage();
		if( cur_lang != 1 && cur_lang != 2 )
		{
			return null;
		}
		List<OperateDynamicData> list = processContent();
		return mClientCallback.getDownloadTip( list , pkgName );
	}
	
	public void changeFolderName(
			String dynamicID ,
			String newName ,
			boolean isDesk )
	{
		List<OperateDynamicData> list = processContent();
		mClientCallback.changeFolderName( list , dynamicID , newName , isDesk );
	}
	
	private void removeFolderData(
			String pkgName )
	{
		List<OperateDynamicData> list = processContent();
		OperateDynamicUtils.removeOneItem( list , pkgName );
		saveDynamicContent( list , false );
	}
	
	public void noitfyUnInstallApp(
			String dynamicID ,
			String packageName ,
			int from ,
			String folderID ,
			boolean isInstalled )
	{
		ArrayList<UnInstallItem> UnInstallItems = mUnInstall.getUnInstallItems();
		mClientCallback.noitfyUnInstallApp( UnInstallItems , dynamicID , packageName , from , folderID , isInstalled );
		removeFolderData( packageName );
	}
	
	public static void setDisclaimerNotifyActivity(
			String activityName )
	{
		DISCLAIMER_NOTIFY_ACTIVITY = activityName;
	};
	
	public DynamicEntryUpdateWaiteDialog getDynamicUpdateWaiteDialog()
	{
		return mDynamicUpdateWaiteDialog;
	}
	
	public void showDynamicUpdateWaiteDialog()
	{
		if( mDynamicUpdateWaiteDialog == null )
		{
			mDynamicUpdateWaiteDialog = new DynamicEntryUpdateWaiteDialog( BaseAppState.getActivityInstance() , R.style.dynamic_wait_dialog );
			mDynamicUpdateWaiteDialog.show();
		}
	}
	
	public void cancelDynamicUpdateWaiteDialog(
			boolean success )
	{
		if( mDynamicUpdateWaiteDialog != null )
		{
			mDynamicUpdateWaiteDialog.quit( success );
			mDynamicUpdateWaiteDialog = null;
		}
	}
}
