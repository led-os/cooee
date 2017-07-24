package com.cooee.phenix;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.cooee.framework.function.Category.CategoryParse;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherSettings.Favorites;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicModel;
import com.cooee.phenix.config.ProviderConfig;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.VirtualInfo;
import com.cooee.phenix.util.ZhiKeShortcutManager;
import com.cooee.util.Tools;


public class LauncherProvider extends ContentProvider
{
	
	private static final String TAG = "Launcher.LauncherProvider";
	private static final boolean LOGD = false;
	private static final String DATABASE_NAME = "launcher.db";
	private static final int DATABASE_VERSION = 16;
	static final String OLD_AUTHORITY = "com.android.launcher2.settings";
	static final String AUTHORITY = ProviderConfig.AUTHORITY;
	//<phenix modify> liuhailin@2015-01-22 modify begin
	static final String TABLE_FAVORITES_DRAWER = "favorites_drawer";
	static final String TABLE_FAVORITES_CORE = "favorites_core";
	static final String TABLE_WORKSPACE_SCREENS_DRAWER = "workspaceScreens_drawer";
	static final String TABLE_WORKSPACE_SCREENS_CORE = "workspaceScreens_core";
	static final String TABLE_OPERATE_FOLDER = "operate_folder";
	//<phenix modify> liuhailin@2015-01-22 modify end
	static final String PARAMETER_NOTIFY = "notify";
	static final String UPGRADED_FROM_OLD_DATABASE = "UPGRADED_FROM_OLD_DATABASE";
	//<i_0010504> liuhailin@2015-03-13 modify begin
	public static final String EMPTY_DATABASE_CREATED = "EMPTY_DATABASE_CREATED";
	//<i_0010504> liuhailin@2015-03-13 modify end
	static final String DEFAULT_WORKSPACE_RESOURCE_ID = "DEFAULT_WORKSPACE_RESOURCE_ID";
	private static final String ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE = "com.android.launcher.action.APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE";
	/**
	 * {@link Uri} triggered at any registered {@link android.database.ContentObserver} when
	 * {@link AppWidgetHost#deleteHost()} is called during database creation.
	 * Use this to recall {@link AppWidgetHost#startListening()} if needed.
	 */
	static final Uri CONTENT_APPWIDGET_RESET_URI = Uri.parse( StringUtils.concat( "content://" , AUTHORITY , "/appWidgetReset" ) );
	private DatabaseHelper mOpenHelper;
	private static boolean sJustLoadedFromOldDb;
	//WangLei add start //实现默认配置AppWidget的流程
	/**用Launcher可以调用startActivityForResult方法，请求第三方绑定AppWidget*/
	private static Launcher mLauncher;
	/**请求绑定的requestCode，注意和Launcher保持一致*/
	private static final int REQUEST_BIND_DEFAULT_APPWIDGET = 12;
	/**添加一个同步锁，当请求绑定插件时，直到点击取消或确定再继续加载流程*/
	private static Object lock = new Object();
	//WangLei add end
	;
	//chenliang start	//解决“由于安卓api23以上短信的intent‘vnd.android-dir/mms-sms’不再支持”导致桌面底边栏缺少信息图标的问题。
	//chenliang del start
	//	//xiatian add start	//为了我们出去的公版在演示的时候能底边栏的四个图标能够查找到相应的应用，当底边栏item的包类名都为空时，按照screen来获取intent，并按照intent来查找合适的应用。
	//	static final String[] DEFAULT_HOTSEAT_ITEMS_INTENT = //当默认配置中配置的底边栏item的包类名都为空时，按照screen来获取intent，并按照intent来查找合适的应用
	//	{ "intent:#Intent;action=android.intent.action.DIAL;end" , //拨号
	//			"intent:content://com.android.contacts/contacts#Intent;action=android.intent.action.VIEW;end" , //联系人
	//			"intent:#Intent;action=android.intent.action.MAIN;type=vnd.android-dir/mms-sms;end" , //信息		
	//			"intent:http://www.google.cn#Intent;action=android.intent.action.VIEW;end" //浏览器
	//	};
	//	//xiatian add end
	//chenliang del end
	;
	//chenliang add start
	final static String DEFAULT_HOTSET_ITEM_KEY_DIAL = "intent:#Intent;action=android.intent.action.DIAL;end"; //拨号
	final static String DEFAULT_HOTSET_ITEM_KEY_CONTACTS = "intent:content://com.android.contacts/contacts#Intent;action=android.intent.action.VIEW;end"; //联系人
	final static String DEFAULT_HOTSET_ITEM_KEY_SMS = "intent:#Intent;action=android.intent.action.MAIN;type=vnd.android-dir/mms-sms;end"; //信息（api<23）
	final static String DEFAULT_HOTSET_ITEM_KEY_HIGH_SMS = "#Intent;action=android.intent.action.MAIN;category=android.intent.category.APP_MESSAGING;end"; //信息（api>=23）
	final static String DEFAULT_HOTSET_ITEM_KEY_WEBSITE = "intent:http://www.google.cn#Intent;action=android.intent.action.VIEW;end"; //浏览器
	//chenliang add end
	//chenliang end
	public static final String FOLDER_TITLE_RESOURCE_NAME_KEY = "FolderTitleResourceNameKey";//xiatian add	//fix bug：解决“桌面上默认配置的文件夹，在没修改文件夹名称之前，切换语言后文件夹的名称没有切换为相应语言”的问题。【c_0003355】
	public static final String ITEM_TITLE_RESOURCE_NAME_KEY = "ItemTitleResourceNameKey";//xiatian add	//fix bug：解决“桌面上默认配置快捷方式和虚图标，切换语言后图标的名称没有切换为相应语言”的问题。
	//cheyingkun add start	//开放default_workspace.xml给客户配置(路径/system/launcher/default_workspace.xml)
	/**单层外部默认配置路径*/
	//xiatian start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
	//	public static final String CUSTOM_DEFAULT_CONFIG = "/system/launcher/launcher_default_config.xml";//xiatian del
	public static final String CUSTOM_DEFAULT_LAYOUT_FILE_NAME_CORE = "default_workspace.xml";//xiatian add
	//xiatian end
	/**双层外部配置路径*/
	//xiatian start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
	//xiatian del start
	//	public static final String CUSTOM_DEFAULT_LAYOUT_FILENAME_PUBLIC_DOUBLE = "/system/launcher/default_workspace_double.xml";//cheyingkun add	//单双层默认图标独立配置文件
	//xiatian del end
	public static final String CUSTOM_DEFAULT_LAYOUT_FILE_NAME_DRAWER = "default_workspace_double.xml";//xiatian add
	//xiatian end
	private static final String IS_CUSTOM_XML = "isCustomXml";
	
	//cheyingkun add end
	@Override
	public boolean onCreate()
	{
		final Context context = getContext();
		//配置文件加载流程修改 , change by shlt@2015/03/04 ADD START
		LauncherDefaultConfig.setApplicationContext( context );
		//配置文件加载流程修改 , change by shlt@2015/03/04 ADD END
		//cheyingkun add start	//解决“下载主题安装至SD卡，应用该主题后，关机拔掉T卡，开机后桌面应用图标仍显示为之前主题的图标大小”的问题。【i_0011898】
		LauncherAppState.setApplicationContext( context );
		LauncherAppState.getInstance();
		//cheyingkun add end
		mOpenHelper = new DatabaseHelper( context );
		LauncherAppState.setLauncherProvider( this );
		return true;
	}
	
	@Override
	public String getType(
			Uri uri )
	{
		SqlArguments args = new SqlArguments( uri , null , null );
		if( TextUtils.isEmpty( args.where ) )
		{
			//<phenix modify> liuhailin@2015-01-22 modify begin
			//return "vnd.android.cursor.dir/" + args.table;
			return StringUtils.concat( "vnd.android.cursor.dir/" , DatabaseHelper.getTabNameByArg( args.table ) );
			//<phenix modify> liuhailin@2015-01-22 modify end
		}
		else
		{
			//<phenix modify> liuhailin@2015-01-22 modify begin
			//return "vnd.android.cursor.item/" + args.table;
			return StringUtils.concat( "vnd.android.cursor.item/" , DatabaseHelper.getTabNameByArg( args.table ) );
			//<phenix modify> liuhailin@2015-01-22 modify end
		}
	}
	
	@Override
	public Cursor query(
			Uri uri ,
			String[] projection ,
			String selection ,
			String[] selectionArgs ,
			String sortOrder )
	{
		SqlArguments args = new SqlArguments( uri , selection , selectionArgs );
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		//<phenix modify> liuhailin@2015-01-22 modify begin
		//【待优化点 - 备注】
		//		1、每次调用context.getContentResolver().query(...)方法时，都会走到该处
		//		2、现在的情况是每次args.table只有两种情况：TABLE_FAVORITES_DRAWER或TABLE_WORKSPACE_SCREENS_DRAWER
		//		3、后续优化点：
		//			(1)把args.table的类型改为TABLE_FAVORITES和TABLE_WORKSPACE_SCREENS，之前TABLE_FAVORITES_DRAWER和TABLE_WORKSPACE_SCREENS_DRAWER容易造成误解，误以为是双层模式的数据表。
		//qb.setTables( args.table );
		qb.setTables( DatabaseHelper.getTabNameByArg( args.table ) );
		//<phenix modify> liuhailin@2015-01-22 modify end
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor result = qb.query( db , projection , args.where , args.args , null , null , sortOrder );
		result.setNotificationUri( getContext().getContentResolver() , uri );
		return result;
	}
	
	private static long dbInsertAndCheck(
			DatabaseHelper helper ,
			SQLiteDatabase db ,
			String table ,
			String nullColumnHack ,
			ContentValues values )
	{
		if( !values.containsKey( LauncherSettings.Favorites._ID ) )
		{
			throw new RuntimeException( "Error: attempting to add item without specifying an id" );
		}
		return db.insert( table , nullColumnHack , values );
	}
	
	private static void deleteId(
			SQLiteDatabase db ,
			long id )
	{
		Uri uri = LauncherSettings.Favorites.getContentUri( id , false );
		SqlArguments args = new SqlArguments( uri , null , null );
		//<phenix modify> liuhailin@2015-01-22 modify begin
		//db.delete( args.table , args.where , args.args );
		db.delete( DatabaseHelper.getTabNameByArg( args.table ) , args.where , args.args );
		//<phenix modify> liuhailin@2015-01-22 modify end
	}
	
	@Override
	public Uri insert(
			Uri uri ,
			ContentValues initialValues )
	{
		SqlArguments args = new SqlArguments( uri );
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		addModifiedTime( initialValues );
		//<phenix modify> liuhailin@2015-01-22 modify begin
		//final long rowId = dbInsertAndCheck( mOpenHelper , db , args.table , null , initialValues );
		final long rowId = dbInsertAndCheck( mOpenHelper , db , DatabaseHelper.getTabNameByArg( args.table ) , null , initialValues );
		//<phenix modify> liuhailin@2015-01-22 modify end
		if( rowId <= 0 )
			return null;
		uri = ContentUris.withAppendedId( uri , rowId );
		sendNotify( uri );
		return uri;
	}
	
	@Override
	public int bulkInsert(
			Uri uri ,
			ContentValues[] values )
	{
		SqlArguments args = new SqlArguments( uri );
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try
		{
			int numValues = values.length;
			for( int i = 0 ; i < numValues ; i++ )
			{
				addModifiedTime( values[i] );
				//<phenix modify> liuhailin@2015-01-22 modify begin
				//if( dbInsertAndCheck( mOpenHelper , db , args.table , null , values[i] ) < 0 )
				if( dbInsertAndCheck( mOpenHelper , db , DatabaseHelper.getTabNameByArg( args.table ) , null , values[i] ) < 0 )
				//<phenix modify> liuhailin@2015-01-22 modify end
				{
					return 0;
				}
			}
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
		sendNotify( uri );
		return values.length;
	}
	
	@Override
	public int delete(
			Uri uri ,
			String selection ,
			String[] selectionArgs )
	{
		SqlArguments args = new SqlArguments( uri , selection , selectionArgs );
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		//<phenix modify> liuhailin@2015-01-22 modify begin
		//int count = db.delete( args.table , args.where , args.args );
		int count = db.delete( DatabaseHelper.getTabNameByArg( args.table ) , args.where , args.args );
		//<phenix modify> liuhailin@2015-01-22 modify end
		if( count > 0 )
			sendNotify( uri );
		return count;
	}
	
	@Override
	public int update(
			Uri uri ,
			ContentValues values ,
			String selection ,
			String[] selectionArgs )
	{
		SqlArguments args = new SqlArguments( uri , selection , selectionArgs );
		addModifiedTime( values );
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		//<phenix modify> liuhailin@2015-01-22 modify begin
		//int count = db.update( args.table , values , args.where , args.args );
		int count = db.update( DatabaseHelper.getTabNameByArg( args.table ) , values , args.where , args.args );
		//<phenix modify> liuhailin@2015-01-22 modify end
		if( count > 0 )
			sendNotify( uri );
		return count;
	}
	
	private void sendNotify(
			Uri uri )
	{
		String notify = uri.getQueryParameter( PARAMETER_NOTIFY );
		if( notify == null || "true".equals( notify ) )
		{
			getContext().getContentResolver().notifyChange( uri , null );
		}
		// always notify the backup agent
		//LauncherBackupAgentHelper.dataChanged(getContext());
	}
	
	private void addModifiedTime(
			ContentValues values )
	{
		values.put( LauncherSettings.ChangeLogColumns.MODIFIED , System.currentTimeMillis() );
	}
	
	public long generateNewItemId()
	{
		return mOpenHelper.generateNewItemId();
	}
	
	public void updateMaxItemId(
			long id )
	{
		mOpenHelper.updateMaxItemId( id );
	}
	
	public long generateNewScreenId()
	{
		return mOpenHelper.generateNewScreenId();
	}
	
	// This is only required one time while loading the workspace during the
	// upgrade path, and should never be called from anywhere else.
	public void updateMaxScreenId(
			long maxScreenId )
	{
		mOpenHelper.updateMaxScreenId( maxScreenId );
	}
	
	/**
	 * @param Should we load the old db for upgrade? first run only.
	 */
	synchronized public boolean justLoadedOldDb()
	{
		String spKey = LauncherAppState.getSharedPreferencesKey();
		SharedPreferences sp = getContext().getSharedPreferences( spKey , Context.MODE_PRIVATE );
		boolean loadedOldDb = false || sJustLoadedFromOldDb;
		sJustLoadedFromOldDb = false;
		//配置文件加载流程修改 , change by shlt@2015/03/04 UPD START
		//if( sp.getBoolean( UPGRADED_FROM_OLD_DATABASE , false ) )
		if( sp.getBoolean( StringUtils.concat( UPGRADED_FROM_OLD_DATABASE , DatabaseHelper.getFavoritesTabName() ) , false ) )
		//配置文件加载流程修改 , change by shlt@2015/03/04 UPD END
		{
			SharedPreferences.Editor editor = sp.edit();
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD START
			//editor.remove( UPGRADED_FROM_OLD_DATABASE+ );
			editor.remove( StringUtils.concat( UPGRADED_FROM_OLD_DATABASE , DatabaseHelper.getFavoritesTabName() ) );
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD END
			editor.commit();
			loadedOldDb = true;
		}
		return loadedOldDb;
	}
	
	/**
	 * @param workspaceResId that can be 0 to use default or non-zero for specific resource
	 */
	synchronized public void loadDefaultFavoritesIfNecessary(
			int origWorkspaceResId )
	{
		String spKey = LauncherAppState.getSharedPreferencesKey();
		SharedPreferences sp = getContext().getSharedPreferences( spKey , Context.MODE_PRIVATE );
		//<phenix modify> liuhailin@2015-01-22 del begin
		// if( sp.getBoolean( EMPTY_DATABASE_CREATED , false ) )
		//配置文件加载流程修改 , change by shlt@2015/03/04 ADD START
		if( sp.getBoolean( StringUtils.concat( EMPTY_DATABASE_CREATED , DatabaseHelper.getFavoritesTabName() ) , false ) )
		//配置文件加载流程修改 , change by shlt@2015/03/04 ADD END
		//<phenix modify> liuhailin@2015-01-22 del end
		{
			int workspaceResId = origWorkspaceResId;
			// Use default workspace resource if none provided
			if( workspaceResId == 0 )
			{
				workspaceResId = sp.getInt( DEFAULT_WORKSPACE_RESOURCE_ID , R.xml.default_workspace );
			}
			// Populate favorites table with initial favorites
			SharedPreferences.Editor editor = sp.edit();
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD START
			//editor.remove( EMPTY_DATABASE_CREATED );
			editor.remove( StringUtils.concat( EMPTY_DATABASE_CREATED , DatabaseHelper.getFavoritesTabName() ) );
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD END
			if( origWorkspaceResId != 0 )
			{
				editor.putInt( DEFAULT_WORKSPACE_RESOURCE_ID , origWorkspaceResId );
			}
			boolean isFirst = sp.getBoolean( "loadOperate" , true );//保证显性文件夹的数据只加载一次
			mIsFirst = true;//gaominghuihui add//解决"当主页面上只有小组件，智能分类后，该页面空白"的问题【i_0014888】。
			if( isFirst )
			{
				editor.putBoolean( "loadOperate" , false );
			}
			//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "cyk_bug : c_0003400" , "cyk loadDefaultFavoritesIfNecessary: " );
			}
			//cheyingkun add end
			//cheyingkun add start	//加载默认配置之前清空数据库【c_0003400】
			//【问题原因】合兴一部(桑菲)项目中，根据出问题时的数据库分析：默认配置加载两次，第一次加载配置不完整，第二次加载的默认配置因为占用位置相同无法显示。默认配置文件夹中的图标显示为第一次不完整的配置，导致图标丢失。
			//【解决方案】加载默认配置之前，清空数据库中的信息
			try
			{
				mOpenHelper.getWritableDatabase().execSQL( StringUtils.concat( "delete from " , LauncherProvider.DatabaseHelper.getFavoritesTabName() ) );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "cyk_bug : c_0003400" , "cyk loadDefaultFavoritesIfNecessary delete ok" );
			}
			catch( SQLException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "cyk_bug : c_0003400" , "cyk loadDefaultFavoritesIfNecessary delete error" );
				e.printStackTrace();
			}
			//cheyingkun add end
			mOpenHelper.loadDefaultConfig( mOpenHelper.getWritableDatabase() , workspaceResId , isFirst );
			mOpenHelper.setFlagJustLoadedOldDb();
			mOpenHelper.writeDefaultEntySeat( sp );//cheyingkun add	//新安装应用不放在默认配置的空格子上
			editor.commit();
		}
		//gaominghui add start //解决"当主页面上只有小组件，智能分类后，该页面空白"的问题【i_0014888】。
		else
		{
			mIsFirst = false;//是否为第一次加载的标志位
		}
		//gaominghui add end
		mOpenHelper.readDefaultEntySeat( sp );//cheyingkun add	//新安装应用不放在默认配置的空格子上
	}
	
	private static interface ContentValuesCallback
	{
		
		public void onRow(
				ContentValues values );
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper
	{
		
		private static final String TAG_FAVORITES = "favorites";
		private static final String TAG_FAVORITE = "favorite";
		private static final String TAG_CLOCK = "clock";
		private static final String TAG_SEARCH = "search";
		private static final String TAG_APPWIDGET = "appwidget";
		private static final String TAG_SHORTCUT = "shortcut";
		private static final String TAG_FOLDER = "folder";
		private static final String TAG_EXTRA = "extra";
		private static final String TAG_INCLUDE = "include";
		private static final String TAG_VIRTUAL = "virtual";//xiatian add	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		private final Context mContext;
		private final AppWidgetHost mAppWidgetHost;
		private long mMaxItemId = -1;
		private long mMaxScreenId = -1;
		private static final String TAG_EMPTY_SEAT = "empty_seat";//cheyingkun add	//桌面支持配置空位【c_0003636】
		/**是否是编译过的默认配置文件,这个参数会影响stringChangeToResId方法如何解析title和icon字符串*/
		boolean isCustomXml = false;//cheyingkun add	//单双层默认图标独立配置文件
		
		DatabaseHelper(
				Context context )
		{
			super( context , DATABASE_NAME , null , DATABASE_VERSION );
			mContext = context;
			mAppWidgetHost = new AppWidgetHost( context , Launcher.APPWIDGET_HOST_ID );
			// In the case where neither onCreate nor onUpgrade gets called, we read the maxId from
			// the DB here
			if( mMaxItemId == -1 )
			{
				mMaxItemId = initializeMaxItemId( getWritableDatabase() );
			}
			if( mMaxScreenId == -1 )
			{
				mMaxScreenId = initializeMaxScreenId( getWritableDatabase() );
			}
			//配置文件加载流程修改 , change by shlt@2015/03/04 ADD START
			//xiatian add start	//fix bug：解决“双层模式下，删除桌面层的最后一个图标后重启桌面，桌面会恢复默认设置”的问题。【i_0010533】
			//【问题原因】当数据库中桌面的数据库表里没有任何一条记录时，方法setFlagEmptyDbCreated中会设置标志位“EMPTY_DATABASE_CREATED + DatabaseHelper.getFavoritesTabName()”为true，以便重启加载默认配置。
			//【解决方案】双层模式，桌面上全部都是快捷方式，故允许数据库中桌面的数据库表里没有任何一条记录。当双层模式时，不需要此保护。
			//xiatian start	//fix bug：解决“第一次进入双层模式时，桌面没有加载默认配置（默认桌面配置和默认底边栏配置）”的问题。
			//			if(  TABLE_FAVORITES_CORE.equals( getFavoritesTabName() ) )//xiatian del
			//xiatian add start
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( mContext );
			String mIsFirstInDrawerKey = LauncherDefaultConfig.getString( R.string.is_first_in_drawer_launcher_style );
			Boolean mIsFirstInDrawer = sp.getBoolean( mIsFirstInDrawerKey , true );
			String mFavoritesTabName = getFavoritesTabName();
			if( ( TABLE_FAVORITES_CORE.equals( mFavoritesTabName ) ) || ( TABLE_FAVORITES_DRAWER.equals( mFavoritesTabName ) && mIsFirstInDrawer == true ) )
			//xiatian add end
			//xiatian end
			//xiatian add end
			{
				SQLiteDatabase db = getWritableDatabase();
				Cursor cursor = db.rawQueryWithFactory( null , StringUtils.concat( "select * from " , mFavoritesTabName ) , null , null );
				if( cursor != null )
				{
					if( cursor.getCount() <= 0 )
					{
						setFlagEmptyDbCreated();
						//xiatian add start	//fix bug：解决“第一次进入双层模式时，桌面没有加载默认配置（默认桌面配置和默认底边栏配置）”的问题。
						if( TABLE_FAVORITES_DRAWER.equals( mFavoritesTabName ) )
						{
							SharedPreferences.Editor editor = sp.edit();
							editor.putBoolean( mIsFirstInDrawerKey , false );
							editor.commit();
						}
						//xiatian add end
					}
					cursor.close();
				}
			}
			//配置文件加载流程修改 , change by shlt@2015/03/04 ADD END
		}
		
		/**
		 * Send notification that we've deleted the {@link AppWidgetHost},
		 * probably as part of the initial database creation. The receiver may
		 * want to re-call {@link AppWidgetHost#startListening()} to ensure
		 * callbacks are correctly set.
		 */
		private void sendAppWidgetResetNotify()
		{
			final ContentResolver resolver = mContext.getContentResolver();
			resolver.notifyChange( CONTENT_APPWIDGET_RESET_URI , null );
		}
		
		@Override
		public void onCreate(
				SQLiteDatabase db )
		{
			if( LOGD )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "creating new launcher database" );
			mMaxItemId = 1;
			mMaxScreenId = 0;
			//<phenix modify> liuhailin@2015-01-22 del begin
			//db.execSQL( "CREATE TABLE " + TABLE_FAVORITES_DRAWER + " (" + "_id INTEGER PRIMARY KEY," + "title TEXT," + "intent TEXT," + "container INTEGER," + "screen INTEGER," + "cellX INTEGER," + "cellY INTEGER," + "spanX INTEGER," + "spanY INTEGER," + "itemType INTEGER," + "appWidgetId INTEGER NOT NULL DEFAULT -1," + "isShortcut INTEGER," + "iconType INTEGER," + "iconPackage TEXT," + "iconResource TEXT," + "icon BLOB," + "uri TEXT," + "displayMode INTEGER," + "appWidgetProvider TEXT," + "modified INTEGER NOT NULL DEFAULT 0" + ");" );
			addFavoritesTabe( db );
			addFavoritesTabeCore( db );
			addWorkspacesTable( db );
			addWorkspacesTableCore( db );
			addOperateFolderTab( db );
			//<phenix modify> liuhailin@2015-01-22 del end
			// Database was just created, so wipe any previous widgets
			if( mAppWidgetHost != null )
			{
				mAppWidgetHost.deleteHost();
				sendAppWidgetResetNotify();
			}
			// Try converting the old database
			ContentValuesCallback permuteScreensCb = new ContentValuesCallback() {
				
				public void onRow(
						ContentValues values )
				{
					int container = values.getAsInteger( LauncherSettings.Favorites.CONTAINER );
					if( container == Favorites.CONTAINER_DESKTOP )
					{
						int screen = values.getAsInteger( LauncherSettings.Favorites.SCREEN );
						screen = (int)upgradeLauncherDb_permuteScreens( screen );
						values.put( LauncherSettings.Favorites.SCREEN , screen );
					}
				}
			};
			Uri uri = Uri.parse( StringUtils.concat( "content://" , Settings.AUTHORITY , "/old_favorites?notify=true" ) );
			if( !convertDatabase( db , uri , permuteScreensCb , true ) )
			{
				//<phenix modify> liuhailin@2015-03-12 del begin
				//以下代码只是针对原生态的launcher2进行查找，com.android.launcher2.settings
				//对于我们launcher不需要，所以直接去掉改代码 
				//// Try and upgrade from the Launcher2 db
				//uri = LauncherSettings.Favorites.OLD_CONTENT_URI;
				//if( !convertDatabase( db , uri , permuteScreensCb , false ) )
				//{
				//	// If we fail, then set a flag to load the default workspace
				//	setFlagEmptyDbCreated();
				//	return;
				//}
				setFlagEmptyDbCreated();
				return;
				//<phenix modify> liuhailin@2015-03-12 del end
			}
			// Right now, in non-default workspace cases, we want to run the final
			// upgrade code (ie. to fix workspace screen indices -> ids, etc.), so
			// set that flag too.
			setFlagJustLoadedOldDb();
		}
		
		private void addWorkspacesTable(
				SQLiteDatabase db )
		{
			db.execSQL( StringUtils.concat(
					"CREATE TABLE " ,
					TABLE_WORKSPACE_SCREENS_DRAWER ,
					" (" ,
					LauncherSettings.WorkspaceScreens._ID ,
					" INTEGER," ,
					LauncherSettings.WorkspaceScreens.SCREEN_RANK ,
					" INTEGER," ,
					LauncherSettings.ChangeLogColumns.MODIFIED ,
					" INTEGER NOT NULL DEFAULT 0" ,
					");" ) );
		}
		
		//<phenix modify> liuhailin@2015-01-22 modify begin
		private void addWorkspacesTableCore(
				SQLiteDatabase db )
		{
			db.execSQL( StringUtils.concat(
					"CREATE TABLE " ,
					TABLE_WORKSPACE_SCREENS_CORE ,
					" (" ,
					LauncherSettings.WorkspaceScreens._ID ,
					" INTEGER," ,
					LauncherSettings.WorkspaceScreens.SCREEN_RANK ,
					" INTEGER," ,
					LauncherSettings.ChangeLogColumns.MODIFIED ,
					" INTEGER NOT NULL DEFAULT 0" ,
					");" ) );
		}
		
		private void addOperateFolderTab(
				SQLiteDatabase db )
		{
			db.execSQL( StringUtils.concat(
					"CREATE TABLE " ,
					TABLE_OPERATE_FOLDER ,
					" (" ,
					"_id INTEGER PRIMARY KEY," ,
					"title TEXT," ,
					"intent TEXT," ,
					"container INTEGER," ,
					"screen INTEGER," ,
					"cellX INTEGER," ,
					"cellY INTEGER," ,
					"spanX INTEGER," ,
					"spanY INTEGER," ,
					"defWorkspaceItemType INTEGER," ,
					"itemType INTEGER," ,
					"appWidgetId INTEGER NOT NULL DEFAULT -1," ,
					"isShortcut INTEGER," ,
					"iconType INTEGER," ,
					"iconPackage TEXT," ,
					"iconResource TEXT," ,
					"icon BLOB," ,
					"uri TEXT," ,
					"displayMode INTEGER," ,
					"appWidgetProvider TEXT," ,
					"operateIntent TEXT," ,
					"modified INTEGER NOT NULL DEFAULT 0" ,
					");" ) );
		}
		
		private void addFavoritesTabe(
				SQLiteDatabase db )
		{
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			//db.execSQL( "CREATE TABLE " + TABLE_FAVORITES_DRAWER + " (" + "_id INTEGER PRIMARY KEY," + "title TEXT," + "intent TEXT," + "container INTEGER," + "screen INTEGER," + "cellX INTEGER," + "cellY INTEGER," + "spanX INTEGER," + "spanY INTEGER," + "itemType INTEGER," + "appWidgetId INTEGER NOT NULL DEFAULT -1," + "isShortcut INTEGER," + "iconType INTEGER," + "iconPackage TEXT," + "iconResource TEXT," + "icon BLOB," + "uri TEXT," + "displayMode INTEGER," + "appWidgetProvider TEXT," + "modified INTEGER NOT NULL DEFAULT 0" + ");" );
			db.execSQL( StringUtils.concat(
					"CREATE TABLE " ,
					TABLE_FAVORITES_DRAWER ,
					" (" ,
					"_id INTEGER PRIMARY KEY," ,
					"title TEXT," ,
					"intent TEXT," ,
					"container INTEGER," ,
					"screen INTEGER," ,
					"cellX INTEGER," ,
					"cellY INTEGER," ,
					"spanX INTEGER," ,
					"spanY INTEGER," ,
					"defWorkspaceItemType INTEGER," ,
					"itemType INTEGER," ,
					"appWidgetId INTEGER NOT NULL DEFAULT -1," ,
					"isShortcut INTEGER," ,
					"iconType INTEGER," ,
					"iconPackage TEXT," ,
					"iconResource TEXT," ,
					"icon BLOB," ,
					"uri TEXT," ,
					"displayMode INTEGER," ,
					"appWidgetProvider TEXT," ,
					"operateIntent TEXT," ,
					"modified INTEGER NOT NULL DEFAULT 0" ,
					");" ) );
			//<数据库字段更新> liuhailin@2015-03-23 modify end
		}
		
		private void addFavoritesTabeCore(
				SQLiteDatabase db )
		{
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			//db.execSQL( "CREATE TABLE " + TABLE_FAVORITES_CORE + " (" + "_id INTEGER PRIMARY KEY," + "title TEXT," + "intent TEXT," + "container INTEGER," + "screen INTEGER," + "cellX INTEGER," + "cellY INTEGER," + "spanX INTEGER," + "spanY INTEGER," + "itemType INTEGER," + "appWidgetId INTEGER NOT NULL DEFAULT -1," + "isShortcut INTEGER," + "iconType INTEGER," + "iconPackage TEXT," + "iconResource TEXT," + "icon BLOB," + "uri TEXT," + "displayMode INTEGER," + "appWidgetProvider TEXT," + "modified INTEGER NOT NULL DEFAULT 0" + ");" );
			db.execSQL( StringUtils.concat(
					"CREATE TABLE " ,
					TABLE_FAVORITES_CORE ,
					" (" ,
					"_id INTEGER PRIMARY KEY," ,
					"title TEXT," ,
					"intent TEXT," ,
					"container INTEGER," ,
					"screen INTEGER," ,
					"cellX INTEGER," ,
					"cellY INTEGER," ,
					"spanX INTEGER," ,
					"spanY INTEGER," ,
					"defWorkspaceItemType INTEGER," ,
					"itemType INTEGER," ,
					"appWidgetId INTEGER NOT NULL DEFAULT -1," ,
					"isShortcut INTEGER," ,
					"iconType INTEGER," ,
					"iconPackage TEXT," ,
					"iconResource TEXT," ,
					"icon BLOB," ,
					"uri TEXT," ,
					"displayMode INTEGER," ,
					"appWidgetProvider TEXT," ,
					"operateIntent TEXT," ,
					"modified INTEGER NOT NULL DEFAULT 0" ,
					");" ) );
			//<数据库字段更新> liuhailin@2015-03-23 modify end
		}
		
		public static String getFavoritesTabName()
		{
			String mFavoritesTabName = null;
			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
			{
				mFavoritesTabName = TABLE_FAVORITES_CORE;
			}
			else if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
			{
				mFavoritesTabName = TABLE_FAVORITES_DRAWER;
			}
			else
			{
				throw new IllegalStateException( StringUtils.concat( "getFavoritesTabName - error CONFIG_LAUNCHER_STYLE: " , LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE ) );
			}
			return mFavoritesTabName;
		}
		
		public static String getWorkspacesTabName()
		{
			String mWorkspacesTabName = null;
			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
			{
				mWorkspacesTabName = TABLE_WORKSPACE_SCREENS_CORE;
			}
			else if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
			{
				mWorkspacesTabName = TABLE_WORKSPACE_SCREENS_DRAWER;
			}
			else
			{
				throw new IllegalStateException( StringUtils.concat( "getWorkspacesTabName - error CONFIG_LAUNCHER_STYLE: " , LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE ) );
			}
			return mWorkspacesTabName;
		}
		
		public static String getTabNameByArg(
				String value )
		{
			if( TABLE_FAVORITES_DRAWER.equals( value ) )
			{
				return getFavoritesTabName();
			}
			else if( TABLE_WORKSPACE_SCREENS_DRAWER.equals( value ) )
			{
				return getWorkspacesTabName();
			}
			return value;
		}
		//<phenix modify> liuhailin@2015-01-22 modify end
		;
		
		//xiatian start	fix bug：解决“1、未进行智能分类前，先将第一页所有图标移到其他页面后，再进行智能分类，分类后第一页页面和第一页所有图标都丢失；2、进行智能分类后，先将第一页所有图标移到其他页面后，再进行智能分类，分类后只显示一个空白页面，所有图标都丢失”的问题。【i_0011277】
		//		private//xiatian del
		public//xiatian add
		//xiatian end
		void setFlagJustLoadedOldDb()
		{
			String mFavoritesTabName = DatabaseHelper.getFavoritesTabName();
			String spKey = LauncherAppState.getSharedPreferencesKey();
			SharedPreferences sp = mContext.getSharedPreferences( spKey , Context.MODE_PRIVATE );
			SharedPreferences.Editor editor = sp.edit();
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD START
			//editor.putBoolean( UPGRADED_FROM_OLD_DATABASE+ , true );
			//editor.putBoolean( EMPTY_DATABASE_CREATED+ , false );
			editor.putBoolean( StringUtils.concat( UPGRADED_FROM_OLD_DATABASE , mFavoritesTabName ) , true );
			editor.putBoolean( StringUtils.concat( EMPTY_DATABASE_CREATED , mFavoritesTabName ) , false );
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD END
			editor.commit();
		}
		
		private void setFlagEmptyDbCreated()
		{
			String mFavoritesTabName = DatabaseHelper.getFavoritesTabName();
			String spKey = LauncherAppState.getSharedPreferencesKey();
			SharedPreferences sp = mContext.getSharedPreferences( spKey , Context.MODE_PRIVATE );
			SharedPreferences.Editor editor = sp.edit();
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD START
			//editor.putBoolean( EMPTY_DATABASE_CREATED+ , true );
			//editor.putBoolean( UPGRADED_FROM_OLD_DATABASE+ , false );
			editor.putBoolean( StringUtils.concat( EMPTY_DATABASE_CREATED , mFavoritesTabName ) , true );
			editor.putBoolean( StringUtils.concat( UPGRADED_FROM_OLD_DATABASE , mFavoritesTabName ) , false );
			//配置文件加载流程修改 , change by shlt@2015/03/04 UPD END
			editor.commit();
		}
		
		// We rearrange the screens from the old launcher
		// 12345 -> 34512
		private long upgradeLauncherDb_permuteScreens(
				long screen )
		{
			if( screen >= 2 )
			{
				return screen - 2;
			}
			else
			{
				return screen + 3;
			}
		}
		
		private boolean convertDatabase(
				SQLiteDatabase db ,
				Uri uri ,
				ContentValuesCallback cb ,
				boolean deleteRows )
		{
			if( LOGD )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "converting database from an older format, but not onUpgrade" );
			boolean converted = false;
			final ContentResolver resolver = mContext.getContentResolver();
			Cursor cursor = null;
			try
			{
				cursor = resolver.query( uri , null , null , null , null );
			}
			catch( Exception e )
			{
				// Ignore
			}
			// We already have a favorites database in the old provider
			if( cursor != null )
			{
				try
				{
					if( cursor.getCount() > 0 )
					{
						converted = copyFromCursor( db , cursor , cb ) > 0;
						if( converted && deleteRows )
						{
							resolver.delete( uri , null , null );
						}
					}
				}
				finally
				{
					cursor.close();
				}
			}
			if( converted )
			{
				// Convert widgets from this import into widgets
				if( LOGD )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , "converted and now triggering widget upgrade" );
				convertWidgets( db );
				// Update max item id
				mMaxItemId = initializeMaxItemId( db );
				if( LOGD )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "mMaxItemId: " , mMaxItemId ) );
			}
			return converted;
		}
		
		private int copyFromCursor(
				SQLiteDatabase db ,
				Cursor c ,
				ContentValuesCallback cb )
		{
			final int idIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites._ID );
			final int intentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.INTENT );
			final int titleIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.TITLE );
			final int iconTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON_TYPE );
			final int iconIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON );
			final int iconPackageIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON_PACKAGE );
			final int iconResourceIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON_RESOURCE );
			final int containerIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CONTAINER );
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			final int defWorkspaceItemType = c.getColumnIndexOrThrow( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE );
			final int operateIntentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.OPERATE_INTENT );
			//<数据库字段更新> liuhailin@2015-03-23 modify end
			final int itemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ITEM_TYPE );
			final int screenIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SCREEN );
			final int cellXIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLX );
			final int cellYIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLY );
			final int uriIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.URI );
			final int displayModeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.DISPLAY_MODE );
			ContentValues[] rows = new ContentValues[c.getCount()];
			int i = 0;
			while( c.moveToNext() )
			{
				ContentValues values = new ContentValues( c.getColumnCount() );
				values.put( LauncherSettings.Favorites._ID , c.getLong( idIndex ) );
				values.put( LauncherSettings.Favorites.INTENT , c.getString( intentIndex ) );
				values.put( LauncherSettings.Favorites.TITLE , c.getString( titleIndex ) );
				values.put( LauncherSettings.Favorites.ICON_TYPE , c.getInt( iconTypeIndex ) );
				values.put( LauncherSettings.Favorites.ICON , c.getBlob( iconIndex ) );
				values.put( LauncherSettings.Favorites.ICON_PACKAGE , c.getString( iconPackageIndex ) );
				values.put( LauncherSettings.Favorites.ICON_RESOURCE , c.getString( iconResourceIndex ) );
				values.put( LauncherSettings.Favorites.CONTAINER , c.getInt( containerIndex ) );
				//<数据库字段更新> liuhailin@2015-03-23 modify begin
				values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , c.getInt( defWorkspaceItemType ) );
				//<数据库字段更新> liuhailin@2015-03-23 modify end
				values.put( LauncherSettings.Favorites.ITEM_TYPE , c.getInt( itemTypeIndex ) );
				values.put( LauncherSettings.Favorites.APPWIDGET_ID , -1 );
				values.put( LauncherSettings.Favorites.SCREEN , c.getInt( screenIndex ) );
				values.put( LauncherSettings.Favorites.CELLX , c.getInt( cellXIndex ) );
				values.put( LauncherSettings.Favorites.CELLY , c.getInt( cellYIndex ) );
				values.put( LauncherSettings.Favorites.URI , c.getString( uriIndex ) );
				values.put( LauncherSettings.Favorites.DISPLAY_MODE , c.getInt( displayModeIndex ) );
				if( cb != null )
				{
					cb.onRow( values );
				}
				rows[i++] = values;
			}
			int total = 0;
			if( i > 0 )
			{
				db.beginTransaction();
				try
				{
					int numValues = rows.length;
					for( i = 0 ; i < numValues ; i++ )
					{
						//<phenix modify> liuhailin@2015-01-22 modify begin
						//if( dbInsertAndCheck( this , db , TABLE_FAVORITES_DRAWER , null , rows[i] ) < 0 )
						if( dbInsertAndCheck( this , db , getFavoritesTabName() , null , rows[i] ) < 0 )
						//<phenix modify> liuhailin@2015-01-22 modify end
						{
							return 0;
						}
						else
						{
							total++;
						}
					}
					db.setTransactionSuccessful();
				}
				finally
				{
					db.endTransaction();
				}
			}
			return total;
		}
		
		@Override
		public void onUpgrade(
				SQLiteDatabase db ,
				int oldVersion ,
				int newVersion )
		{
			if( LOGD )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "onUpgrade triggered: " , oldVersion ) );
			int version = oldVersion;
			if( version < 3 )
			{
				// upgrade 1,2 -> 3 added appWidgetId column
				db.beginTransaction();
				try
				{
					// Insert new column for holding appWidgetIds
					db.execSQL( "ALTER TABLE favorites ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;" );
					db.setTransactionSuccessful();
					version = 3;
				}
				catch( SQLException ex )
				{
					// Old version remains, which means we wipe old data
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , ex.getMessage() , ex );
				}
				finally
				{
					db.endTransaction();
				}
				// Convert existing widgets only if table upgrade was successful
				if( version == 3 )
				{
					convertWidgets( db );
				}
			}
			if( version < 4 )
			{
				version = 4;
			}
			// Where's version 5?
			// - Donut and sholes on 2.0 shipped with version 4 of launcher1.
			// - Passion shipped on 2.1 with version 6 of launcher3
			// - Sholes shipped on 2.1r1 (aka Mr. 3) with version 5 of launcher 1
			//   but version 5 on there was the updateContactsShortcuts change
			//   which was version 6 in launcher 2 (first shipped on passion 2.1r1).
			// The updateContactsShortcuts change is idempotent, so running it twice
			// is okay so we'll do that when upgrading the devices that shipped with it.
			if( version < 6 )
			{
				// We went from 3 to 5 screens. Move everything 1 to the right
				db.beginTransaction();
				try
				{
					db.execSQL( "UPDATE favorites SET screen=(screen + 1);" );
					db.setTransactionSuccessful();
				}
				catch( SQLException ex )
				{
					// Old version remains, which means we wipe old data
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , ex.getMessage() , ex );
				}
				finally
				{
					db.endTransaction();
				}
				// We added the fast track.
				if( updateContactsShortcuts( db ) )
				{
					version = 6;
				}
			}
			if( version < 7 )
			{
				// Version 7 gets rid of the special search widget.
				convertWidgets( db );
				version = 7;
			}
			if( version < 8 )
			{
				// Version 8 (froyo) has the icons all normalized.  This should
				// already be the case in practice, but we now rely on it and don't
				// resample the images each time.
				normalizeIcons( db );
				version = 8;
			}
			if( version < 9 )
			{
				// The max id is not yet set at this point (onUpgrade is triggered in the ctor
				// before it gets a change to get set, so we need to read it here when we use it)
				if( mMaxItemId == -1 )
				{
					mMaxItemId = initializeMaxItemId( db );
				}
				// Add default hotseat icons
				loadDefaultConfig( db , R.xml.update_workspace , false );
				version = 9;
			}
			// We bumped the version three time during JB, once to update the launch flags, once to
			// update the override for the default launch animation and once to set the mimetype
			// to improve startup performance
			if( version < 12 )
			{
				// Contact shortcuts need a different set of flags to be launched now
				// The updateContactsShortcuts change is idempotent, so we can keep using it like
				// back in the Donut days
				updateContactsShortcuts( db );
				version = 12;
			}
			if( version < 13 )
			{
				// With the new shrink-wrapped and re-orderable workspaces, it makes sense
				// to persist workspace screens and their relative order.
				mMaxScreenId = 0;
				// This will never happen in the wild, but when we switch to using workspace
				// screen ids, redo the import from old launcher.
				sJustLoadedFromOldDb = true;
				addWorkspacesTable( db );
				version = 13;
			}
			if( version < 14 )
			{
				db.beginTransaction();
				try
				{
					// Insert new column for holding widget provider name
					db.execSQL( "ALTER TABLE favorites ADD COLUMN appWidgetProvider TEXT;" );
					db.setTransactionSuccessful();
					version = 14;
				}
				catch( SQLException ex )
				{
					// Old version remains, which means we wipe old data
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , ex.getMessage() , ex );
				}
				finally
				{
					db.endTransaction();
				}
			}
			if( version < 15 )
			{
				db.beginTransaction();
				try
				{
					// Insert new column for holding update timestamp
					db.execSQL( "ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;" );
					db.execSQL( "ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;" );
					db.setTransactionSuccessful();
					version = 15;
				}
				catch( SQLException ex )
				{
					// Old version remains, which means we wipe old data
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , ex.getMessage() , ex );
				}
				finally
				{
					db.endTransaction();
				}
			}
			// wanghongjian@2015/04/22 UPD START 解决因在运营文件夹第一次安装时候，由于新增加数据库表，而导致的重启的bug
			if( version < 16 )
			{
				db.beginTransaction();
				try
				{
					addOperateFolderTab( db );
					db.setTransactionSuccessful();
					version = 16;
				}
				catch( SQLException ex )
				{
					// Old version remains, which means we wipe old data
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , ex.getMessage() , ex );
				}
				finally
				{
					db.endTransaction();
				}
			}
			// wanghongjian@2015/04/22 UPD END
			if( version != DATABASE_VERSION )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Destroying all old data." );
				db.execSQL( StringUtils.concat( "DROP TABLE IF EXISTS " , TABLE_FAVORITES_DRAWER ) );
				db.execSQL( StringUtils.concat( "DROP TABLE IF EXISTS " , TABLE_WORKSPACE_SCREENS_DRAWER ) );
				//<phenix modify> liuhailin@2015-01-22 modify begin
				db.execSQL( StringUtils.concat( "DROP TABLE IF EXISTS " , TABLE_FAVORITES_CORE ) );
				db.execSQL( StringUtils.concat( "DROP TABLE IF EXISTS " , TABLE_WORKSPACE_SCREENS_CORE ) );
				//<phenix modify> liuhailin@2015-01-22 modify end
				db.execSQL( StringUtils.concat( "DROP TABLE IF EXISTS " , TABLE_OPERATE_FOLDER ) );
				onCreate( db );
			}
		}
		
		private boolean updateContactsShortcuts(
				SQLiteDatabase db )
		{
			final String selectWhere = buildOrWhereString( Favorites.ITEM_TYPE , new int[]{ Favorites.ITEM_TYPE_SHORTCUT } );
			Cursor c = null;
			final String actionQuickContact = "com.android.contacts.action.QUICK_CONTACT";
			db.beginTransaction();
			try
			{
				// Select and iterate through each matching widget
				//<phenix modify> liuhailin@2015-01-22 modify begin
				//c = db.query( TABLE_FAVORITES_DRAWER , new String[]{ Favorites._ID , Favorites.INTENT } , selectWhere , null , null , null , null );
				c = db.query( getFavoritesTabName() , new String[]{ Favorites._ID , Favorites.INTENT } , selectWhere , null , null , null , null );
				//<phenix modify> liuhailin@2015-01-22 modify end
				if( c == null )
					return false;
				if( LOGD )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "found upgrade cursor count=" , c.getCount() ) );
				final int idIndex = c.getColumnIndex( Favorites._ID );
				final int intentIndex = c.getColumnIndex( Favorites.INTENT );
				while( c.moveToNext() )
				{
					long favoriteId = c.getLong( idIndex );
					final String intentUri = c.getString( intentIndex );
					if( intentUri != null )
					{
						try
						{
							final Intent intent = Intent.parseUri( intentUri , 0 );
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "Home" , intent.toString() );
							final Uri uri = intent.getData();
							if( uri != null )
							{
								final String data = uri.toString();
								if( ( Intent.ACTION_VIEW.equals( intent.getAction() ) || actionQuickContact.equals( intent.getAction() ) ) && ( data.startsWith( "content://contacts/people/" ) || data
										.startsWith( "content://com.android.contacts/contacts/lookup/" ) ) )
								{
									final Intent newIntent = new Intent( actionQuickContact );
									// When starting from the launcher, start in a new, cleared task
									// CLEAR_WHEN_TASK_RESET cannot reset the root of a task, so we
									// clear the whole thing preemptively here since
									// QuickContactActivity will finish itself when launching other
									// detail activities.
									newIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
									newIntent.putExtra( Launcher.INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION , true );
									newIntent.setData( uri );
									// Determine the type and also put that in the shortcut
									// (that can speed up launch a bit)
									newIntent.setDataAndType( uri , newIntent.resolveType( mContext ) );
									final ContentValues values = new ContentValues();
									values.put( LauncherSettings.Favorites.INTENT , newIntent.toUri( 0 ) );
									String updateWhere = StringUtils.concat( Favorites._ID , "=" , favoriteId );
									//<phenix modify> liuhailin@2015-01-22 modify begin
									//db.update( TABLE_FAVORITES_DRAWER , values , updateWhere , null );
									db.update( getFavoritesTabName() , values , updateWhere , null );
									//<phenix modify> liuhailin@2015-01-22 modify end
								}
							}
						}
						catch( RuntimeException ex )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , "Problem upgrading shortcut" , ex );
						}
						catch( URISyntaxException e )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , "Problem upgrading shortcut" , e );
						}
					}
				}
				db.setTransactionSuccessful();
			}
			catch( SQLException ex )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Problem while upgrading contacts" , ex );
				return false;
			}
			finally
			{
				db.endTransaction();
				if( c != null )
				{
					c.close();
				}
			}
			return true;
		}
		
		private void normalizeIcons(
				SQLiteDatabase db )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "normalizing icons" );
			db.beginTransaction();
			Cursor c = null;
			SQLiteStatement update = null;
			try
			{
				boolean logged = false;
				update = db.compileStatement( "UPDATE favorites SET icon=? WHERE _id=?" );
				c = db.rawQuery( StringUtils.concat( "SELECT _id, icon FROM favorites WHERE iconType=" , Favorites.ICON_TYPE_BITMAP ) , null );
				final int idIndex = c.getColumnIndexOrThrow( Favorites._ID );
				final int iconIndex = c.getColumnIndexOrThrow( Favorites.ICON );
				while( c.moveToNext() )
				{
					long id = c.getLong( idIndex );
					byte[] data = c.getBlob( iconIndex );
					try
					{
						Bitmap bitmap = Utilities.resampleIconBitmap( BitmapFactory.decodeByteArray( data , 0 , data.length ) , mContext );
						if( bitmap != null )
						{
							update.bindLong( 1 , id );
							data = ItemInfo.flattenBitmap( bitmap );
							if( data != null )
							{
								update.bindBlob( 2 , data );
								update.execute();
							}
							bitmap.recycle();
						}
					}
					catch( Exception e )
					{
						if( !logged )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , StringUtils.concat( "Failed normalizing icon " , id ) , e );
						}
						else
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , StringUtils.concat( "Also failed normalizing icon " , id ) );
						}
						logged = true;
					}
				}
				db.setTransactionSuccessful();
			}
			catch( SQLException ex )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Problem while allocating appWidgetIds for existing widgets" , ex );
			}
			finally
			{
				db.endTransaction();
				if( update != null )
				{
					update.close();
				}
				if( c != null )
				{
					c.close();
				}
			}
		}
		
		// Generates a new ID to use for an object in your database. This method should be only
		// called from the main UI thread. As an exception, we do call it when we call the
		// constructor from the worker thread; however, this doesn't extend until after the
		// constructor is called, and we only pass a reference to LauncherProvider to LauncherApp
		// after that point
		public long generateNewItemId()
		{
			if( mMaxItemId < 0 )
			{
				throw new RuntimeException( "Error: max item id was not initialized" );
			}
			mMaxItemId += 1;
			return mMaxItemId;
		}
		
		public void updateMaxItemId(
				long id )
		{
			mMaxItemId = id + 1;
		}
		
		private long initializeMaxItemId(
				SQLiteDatabase db )
		{
			//<phenix modify> liuhailin@2015-01-22 del begin
			//Cursor c = db.rawQuery( "SELECT MAX(_id) FROM favorites" , null );
			Cursor c = db.rawQuery( StringUtils.concat( "SELECT MAX(_id) FROM " , getFavoritesTabName() ) , null );
			//<phenix modify> liuhailin@2015-01-22 del end
			// get the result
			final int maxIdIndex = 0;
			long id = -1;
			if( c != null && c.moveToNext() )
			{
				id = c.getLong( maxIdIndex );
			}
			if( c != null )
			{
				c.close();
			}
			if( id == -1 )
			{
				throw new RuntimeException( "Error: could not query max item id" );
			}
			return id;
		}
		
		// Generates a new ID to use for an workspace screen in your database. This method
		// should be only called from the main UI thread. As an exception, we do call it when we
		// call the constructor from the worker thread; however, this doesn't extend until after the
		// constructor is called, and we only pass a reference to LauncherProvider to LauncherApp
		// after that point
		public long generateNewScreenId()
		{
			if( mMaxScreenId < 0 )
			{
				throw new RuntimeException( "Error: max screen id was not initialized" );
			}
			mMaxScreenId += 1;
			return mMaxScreenId;
		}
		
		public void updateMaxScreenId(
				long maxScreenId )
		{
			mMaxScreenId = maxScreenId;
		}
		
		private long initializeMaxScreenId(
				SQLiteDatabase db )
		{
			//<phenix modify> liuhailin@2015-01-22 modify begin
			//Cursor c = db.rawQuery( "SELECT MAX(" + LauncherSettings.WorkspaceScreens._ID + ") FROM " + TABLE_WORKSPACE_SCREENS_DRAWER , null );
			Cursor c = db.rawQuery( StringUtils.concat( "SELECT MAX(" , LauncherSettings.WorkspaceScreens._ID , ") FROM " , getWorkspacesTabName() ) , null );
			//<phenix modify> liuhailin@2015-01-22 modify end
			// get the result
			final int maxIdIndex = 0;
			long id = -1;
			if( c != null && c.moveToNext() )
			{
				id = c.getLong( maxIdIndex );
			}
			if( c != null )
			{
				c.close();
			}
			if( id == -1 )
			{
				throw new RuntimeException( "Error: could not query max screen id" );
			}
			return id;
		}
		
		/**
		 * Upgrade existing clock and photo frame widgets into their new widget
		 * equivalents.
		 */
		private void convertWidgets(
				SQLiteDatabase db )
		{
			final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( mContext );
			final int[] bindSources = new int[]{ Favorites.ITEM_TYPE_WIDGET_CLOCK , Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME , Favorites.ITEM_TYPE_WIDGET_SEARCH , };
			final String selectWhere = buildOrWhereString( Favorites.ITEM_TYPE , bindSources );
			Cursor c = null;
			db.beginTransaction();
			try
			{
				// Select and iterate through each matching widget
				//<phenix modify> liuhailin@2015-01-22 modify begin
				//c = db.query( TABLE_FAVORITES_DRAWER , new String[]{ Favorites._ID , Favorites.ITEM_TYPE } , selectWhere , null , null , null , null );
				c = db.query( getFavoritesTabName() , new String[]{ Favorites._ID , Favorites.ITEM_TYPE } , selectWhere , null , null , null , null );
				//<phenix modify> liuhailin@2015-01-22 modify end
				if( LOGD )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "found upgrade cursor count=" , c.getCount() ) );
				final ContentValues values = new ContentValues();
				while( c != null && c.moveToNext() )
				{
					long favoriteId = c.getLong( 0 );
					int favoriteType = c.getInt( 1 );
					// Allocate and update database with new appWidgetId
					try
					{
						int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
						if( LOGD )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , StringUtils.concat( "allocated appWidgetId=" , appWidgetId , " for favoriteId=" , favoriteId ) );
						}
						values.clear();
						values.put( Favorites.ITEM_TYPE , Favorites.ITEM_TYPE_APPWIDGET );
						values.put( Favorites.APPWIDGET_ID , appWidgetId );
						// Original widgets might not have valid spans when upgrading
						if( favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH )
						{
							values.put( LauncherSettings.Favorites.SPANX , 4 );
							values.put( LauncherSettings.Favorites.SPANY , 1 );
						}
						else
						{
							values.put( LauncherSettings.Favorites.SPANX , 2 );
							values.put( LauncherSettings.Favorites.SPANY , 2 );
						}
						String updateWhere = StringUtils.concat( Favorites._ID , "=" , favoriteId );
						//<phenix modify> liuhailin@2015-01-22 modify begin
						//db.update( TABLE_FAVORITES_DRAWER , values , updateWhere , null );
						db.update( getFavoritesTabName() , values , updateWhere , null );
						//<phenix modify> liuhailin@2015-01-22 modify end
						if( favoriteType == Favorites.ITEM_TYPE_WIDGET_CLOCK )
						{
							// TODO: check return value
							// gaominghui@2016/12/14 ADD START
							if( Build.VERSION.SDK_INT >= 16 )
								appWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , new ComponentName( "com.android.alarmclock" , "com.android.alarmclock.AnalogAppWidgetProvider" ) );
							else
								Tools.bindAppWidgetId( appWidgetManager , appWidgetId , new ComponentName( "com.android.alarmclock" , "com.android.alarmclock.AnalogAppWidgetProvider" ) );
							// gaominghui@2016/12/14 ADD END
						}
						else if( favoriteType == Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME )
						{
							// TODO: check return value
							// gaominghui@2016/12/14 ADD START
							if( Build.VERSION.SDK_INT >= 16 )
								appWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , new ComponentName( "com.android.camera" , "com.android.camera.PhotoAppWidgetProvider" ) );
							else
								Tools.bindAppWidgetId( appWidgetManager , appWidgetId , new ComponentName( "com.android.camera" , "com.android.camera.PhotoAppWidgetProvider" ) );
							// gaominghui@2016/12/14 ADD END
						}
						else if( favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH )
						{
							// TODO: check return value
							// gaominghui@2016/12/14 ADD START
							if( Build.VERSION.SDK_INT >= 16 )
								appWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , getSearchWidgetProvider() );
							else
								Tools.bindAppWidgetId( appWidgetManager , appWidgetId , getSearchWidgetProvider() );
							// gaominghui@2016/12/14 ADD END
						}
					}
					catch( RuntimeException ex )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.e( TAG , "Problem allocating appWidgetId" , ex );
					}
				}
				db.setTransactionSuccessful();
			}
			catch( SQLException ex )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Problem while allocating appWidgetIds for existing widgets" , ex );
			}
			finally
			{
				db.endTransaction();
				if( c != null )
				{
					c.close();
				}
			}
			// Update max item id
			mMaxItemId = initializeMaxItemId( db );
			if( LOGD )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "mMaxItemId: " , mMaxItemId ) );
		}
		
		private static final void beginDocument(
				XmlPullParser parser ,
				String firstElementName ) throws XmlPullParserException , IOException
		{
			int type;
			while( ( type = parser.next() ) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT )
			{
				;
			}
			if( type != XmlPullParser.START_TAG )
			{
				throw new XmlPullParserException( "No start tag found" );
			}
			if( !parser.getName().equals( firstElementName ) )
			{
				throw new XmlPullParserException( StringUtils.concat( "Unexpected start tag: found " , parser.getName() , ", expected " , firstElementName ) );
			}
		}
		
		public void loadDefaultConfig(
				SQLiteDatabase db ,
				int workspaceResourceId ,
				boolean isFirst )
		{
			//智能分类也会重新loadfavorites，但在load之前，先删除了favorites表，但没有删除运营文件夹的表，所以智能分类不需要重新加载默认运营文件夹
			InputStream is = readDefaultWorkspaceFile();//cheyingkun add	//开放default_workspace.xml给客户配置(路径/system/launcher/default_workspace.xml)
			loadFavorites( db , is , workspaceResourceId );
			if( isFirst )
				OperateDynamicModel.loadDefaultConfig( db );
		}
		
		//cheyingkun add start	//开放default_workspace.xml给客户配置(路径/system/launcher/default_workspace.xml)
		/**
		 * Loads the default set of favorite packages from an xml file.
		 *
		 * @param db The database to write the values into
		 * @param filterContainerId The specific container id of items to load
		 */
		private int loadFavorites(
				SQLiteDatabase db ,
				InputStream inputStream ,
				int workspaceResourceId )
		{
			Intent intent = new Intent( Intent.ACTION_MAIN , null );
			intent.addCategory( Intent.CATEGORY_LAUNCHER );
			ContentValues values = new ContentValues();
			if( LOGD )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , String.format( "Loading favorites from resid=0x%08x" , workspaceResourceId ) );
			PackageManager packageManager = mContext.getPackageManager();
			int i = 0;
			try
			{
				//获取默认配置的xml解析器
				XmlPullParser parser = getDefaultWorkspaceXmlPullParser( inputStream );//cheyingkun add	//单双层默认图标独立配置文件
				beginDocument( parser , TAG_FAVORITES );
				final int depth = parser.getDepth();
				int type;
				while( ( ( type = parser.next() ) != XmlPullParser.END_TAG || parser.getDepth() > depth ) && type != XmlPullParser.END_DOCUMENT )
				{
					if( type != XmlPullParser.START_TAG )
					{
						continue;
					}
					boolean added = false;
					final String name = parser.getName();
					//获取当前标签的属性和值,放入map中
					HashMap<String , String> xmlMap = new HashMap<String , String>();
					for( int j = 0 ; j < parser.getAttributeCount() ; j++ )
					{
						String attributeName = parser.getAttributeName( j );
						String attributeValue = parser.getAttributeValue( j );
						xmlMap.put( attributeName , attributeValue );
					}
					xmlMap.put( IS_CUSTOM_XML , String.valueOf( isCustomXml ) );
					Set<String> keySet = xmlMap.keySet();
					// Assuming it's a <favorite> at this point
					long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
					if( keySet.contains( LauncherSettings.Favorites.CONTAINER ) )
					{
						container = Long.valueOf( xmlMap.get( LauncherSettings.Favorites.CONTAINER ) );
					}
					String screen = "0";
					if( keySet.contains( LauncherSettings.Favorites.SCREEN ) )
					{
						screen = xmlMap.get( LauncherSettings.Favorites.SCREEN );
					}
					String cellX = "0";
					if( keySet.contains( LauncherSettings.Favorites.CELLX ) )
					{
						cellX = xmlMap.get( LauncherSettings.Favorites.CELLX );
					}
					String cellY = "0";
					if( keySet.contains( LauncherSettings.Favorites.CELLY ) )
					{
						cellY = xmlMap.get( LauncherSettings.Favorites.CELLY );
					}
					values.clear();
					values.put( LauncherSettings.Favorites.CONTAINER , container );
					//<数据库字段更新> liuhailin@2015-03-20 modify begin
					if( container == LauncherSettings.Favorites.CONTAINER_DESKTOP )
					{
						values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE_DESKTOP );
					}
					else if( container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
					{
						values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE_HOTSEAT );
					}
					//<数据库字段更新> liuhailin@2015-03-20 modify end
					//cheyingkun add start	//单双层分开配置(底边栏)
					if( container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
					{
						cellX = String.valueOf( Integer.parseInt( cellX ) );
						screen = cellX;
					}
					//cheyingkun add end
					values.put( LauncherSettings.Favorites.SCREEN , screen );
					values.put( LauncherSettings.Favorites.CELLX , cellX );
					values.put( LauncherSettings.Favorites.CELLY , cellY );
					if( LOGD )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							final String title = xmlMap.get( LauncherSettings.Favorites.TITLE );
							final String pkg = xmlMap.get( LauncherSettings.Favorites.PACKAGE_NAME );
							final String something = title != null ? title : pkg;
							Log.v( TAG , String.format(
									( "%" + ( 2 * ( depth + 1 ) ) + "s<%s%s c=%d s=%s x=%s y=%s>" ) ,
									"" ,
									name ,
									( something == null ? "" : ( " \"" + something + "\"" ) ) ,
									container ,
									screen ,
									cellX ,
									cellY ) );
						}
					}
					if( TAG_FAVORITE.equals( name ) )
					{
						long id = addAppShortcut( db , values , packageManager , intent , xmlMap );
						added = id >= 0;
					}
					else if( TAG_SEARCH.equals( name ) )
					{
						added = addSearchWidget( db , values );
					}
					else if( TAG_CLOCK.equals( name ) )
					{
						added = addClockWidget( db , values );
					}
					else if( TAG_APPWIDGET.equals( name ) )
					{
						String packageName = xmlMap.get( LauncherSettings.Favorites.PACKAGE_NAME );
						String className = xmlMap.get( LauncherSettings.Favorites.CLASS_NAME );
						ComponentName cn = new ComponentName( packageName , className );
						boolean canAddAppWidget = canAddAppWidget( cn , packageManager );
						if( canAddAppWidget )
						{
							String spanXStr = xmlMap.get( LauncherSettings.Favorites.SPANX );
							String spanYStr = xmlMap.get( LauncherSettings.Favorites.SPANY );
							int spanX = Integer.valueOf( spanXStr );
							int spanY = Integer.valueOf( spanYStr );
							added = addAppWidget( db , values , cn , spanX , spanY );
						}
						else
						{
							added = false;
						}
					}
					else if( TAG_SHORTCUT.equals( name ) )
					{
						//xiatian start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
						//						long id = addUriShortcut( db , values , a );//xiatian del
						long id = addShortcut( db , values , xmlMap );//xiatian add
						//xiatian end
						added = id >= 0;
					}
					//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
					else if( TAG_VIRTUAL.equals( name ) )
					{
						long id = addVirtual( db , values , xmlMap );
						added = id >= 0;
					}
					//xiatian add end
					else if( TAG_FOLDER.equals( name ) )
					{
						String titleStr = xmlMap.get( LauncherSettings.Favorites.TITLE );
						int titleResId = stringChangeToResId( titleStr , "@string/" , "string" , isCustomXml );
						String title;
						//xiatian start	//fix bug：解决“桌面上默认配置的文件夹，在没修改文件夹名称之前，切换语言后文件夹的名称没有切换为相应语言”的问题。【c_0003355】
						//xiatian del start
						//						if( titleResId != 0 )
						//						{
						//							title = mContext.getResources().getString( titleResId );
						//						}
						//						else
						//						{
						//							title = mContext.getResources().getString( R.string.folder_name );
						//						}
						//xiatian del end
						//xiatian add start
						if( titleResId == 0 )
						{
							titleResId = R.string.folder_name;
						}
						title = StringUtils.concat( FOLDER_TITLE_RESOURCE_NAME_KEY , mContext.getResources().getResourceName( titleResId ) );
						//xiatian add end
						//xiatian end
						values.put( LauncherSettings.Favorites.TITLE , title );
						long folderId = addFolder( db , values );
						added = folderId >= 0;
						ArrayList<Long> folderItems = new ArrayList<Long>();
						int folderDepth = parser.getDepth();
						while( ( type = parser.next() ) != XmlPullParser.END_TAG || parser.getDepth() > folderDepth )
						{
							if( type != XmlPullParser.START_TAG )
							{
								continue;
							}
							final String folder_item_name = parser.getName();
							HashMap<String , String> xmlFolderItemMap = new HashMap<String , String>();
							for( int j = 0 ; j < parser.getAttributeCount() ; j++ )
							{
								String attributeName = parser.getAttributeName( j );
								String attributeValue = parser.getAttributeValue( j );
								xmlFolderItemMap.put( attributeName , attributeValue );
							}
							xmlFolderItemMap.put( IS_CUSTOM_XML , String.valueOf( isCustomXml ) );
							values.clear();
							values.put( LauncherSettings.Favorites.CONTAINER , folderId );
							values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE_FOLDER );//xiatian add	//fix bug：解决“默认配置中配置在文件夹中的item，会参与智能分类”的问题。
							if( LOGD )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								{
									final String pkg = xmlMap.get( LauncherSettings.Favorites.PACKAGE_NAME );
									final String uri = xmlMap.get( LauncherSettings.Favorites.URI );
									Log.v( TAG , String.format( ( "%" + ( 2 * ( folderDepth + 1 ) ) + "s<%s \"%s\">" ) , "" , folder_item_name , uri != null ? uri : pkg ) );
								}
							}
							if( TAG_FAVORITE.equals( folder_item_name ) && folderId >= 0 )
							{
								long id = addAppShortcut( db , values , packageManager , intent , xmlFolderItemMap );
								if( id >= 0 )
								{
									folderItems.add( id );
								}
							}
							else if( TAG_SHORTCUT.equals( folder_item_name ) && folderId >= 0 )
							{
								//xiatian start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
								//						long id = addUriShortcut( db , values , ar );//xiatian del
								long id = addShortcut( db , values , xmlFolderItemMap );//xiatian add
								//xiatian end
								if( id >= 0 )
								{
									folderItems.add( id );
								}
							}
							//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
							else if( TAG_VIRTUAL.equals( folder_item_name ) && folderId >= 0 )
							{
								long id = addVirtual( db , values , xmlFolderItemMap );
								if( id >= 0 )
								{
									folderItems.add( id );
								}
							}
							//xiatian add end
							else
							{
								throw new RuntimeException( "Folders can contain only shortcuts" );
							}
						}
						// We can only have folders with >= 2 items, so we need to remove the
						// folder and clean up if less than 2 items were included, or some
						// failed to add, and less than 2 were actually added
						if( folderItems.size() < 2 && folderId >= 0 )
						{
							// We just delete the folder and any items that made it
							deleteId( db , folderId );
							if( folderItems.size() > 0 )
							{
								deleteId( db , folderItems.get( 0 ) );
							}
							added = false;
						}
					}
					//cheyingkun add start	//桌面支持配置空位【c_0003636】
					else if( TAG_EMPTY_SEAT.equals( name ) )
					{
						addEmptySeat( screen , cellX , cellY );
					}
					//cheyingkun add end
					if( added )
						i++;
				}
			}
			catch( XmlPullParserException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Got exception parsing favorites." , e );
			}
			catch( IOException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Got exception parsing favorites." , e );
			}
			catch( RuntimeException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Got exception parsing favorites." , e );
			}
			// Update the max item id after we have loaded the database
			if( mMaxItemId == -1 )
			{
				mMaxItemId = initializeMaxItemId( db );
			}
			return i;
		}
		
		/**字符串转换成资源id*/
		private int stringChangeToResId(
				String itemIconStr ,
				String containStr ,
				String defType ,
				boolean isCompiledXml )
		{
			if( TextUtils.isEmpty( itemIconStr ) || TextUtils.isEmpty( containStr ) || TextUtils.isEmpty( defType ) )
			{
				return 0;
			}
			int resId = 0;
			if( isCompiledXml )
			{
				if( itemIconStr.contains( "@" ) )
				{
					itemIconStr = itemIconStr.replace( "@" , "" );
					try
					{
						resId = Integer.valueOf( itemIconStr );
					}
					catch( NumberFormatException e )
					{
						resId = 0;
					}
				}
			}
			else
			{
				if( itemIconStr.contains( containStr ) )
				{
					itemIconStr = itemIconStr.replace( containStr , "" );
					Resources res = LauncherAppState.getInstance().getContext().getResources();
					resId = res.getIdentifier( itemIconStr , defType , LauncherAppState.getInstance().getContext().getPackageName() );
				}
			}
			return resId;
		}
		
		/**读取客户配置的default_workspace.xml的配置文件*/
		private InputStream readDefaultWorkspaceFile()
		{
			InputStream is = null;
			//cheyingkun add start	//单双层默认图标独立配置文件
			//单双层读取不同的配置文件
			//xiatian start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
			//			String mCustomDefaultLayoutFileFullPath;//xiatian del
			String mFileName;//xiatian add
			//xiatian end
			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
			{
				//xiatian start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
				//				path = CUSTOM_DEFAULT_LAYOUT_FILENAME_PUBLIC;//xiatian del
				mFileName = CUSTOM_DEFAULT_LAYOUT_FILE_NAME_CORE;//xiatian add
				//xiatian end
			}
			else if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
			{
				//xiatian start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
				//				path = CUSTOM_DEFAULT_LAYOUT_FILENAME_PUBLIC_DOUBLE;//xiatian del
				mFileName = CUSTOM_DEFAULT_LAYOUT_FILE_NAME_DRAWER;//xiatian add
				//xiatian end
			}
			else
			{
				throw new IllegalStateException( StringUtils.concat( "readDefaultWorkspaceFile - error CONFIG_LAUNCHER_STYLE: " , LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE ) );
			}
			//cheyingkun add end
			String mCustomDefaultLayoutFileFullPath = StringUtils.concat( LauncherDefaultConfig.CUSTOM_DEFAULT_CONFIG_PATH , File.separator , mFileName );//xiatian add	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
			File f = new File( mCustomDefaultLayoutFileFullPath );
			if( f.exists() )
			{
				try
				{
					is = new FileInputStream( f.getAbsolutePath() );
				}
				catch( FileNotFoundException e )
				{
					is = null;
				}
			}
			return is;
		}
		
		//cheyingkun add end
		private long addAppShortcut(
				SQLiteDatabase db ,
				ContentValues values ,
				PackageManager packageManager ,
				Intent intent ,
				HashMap<String , String> xmlMap )
		{
			long id = -1;
			ActivityInfo info;
			//底边栏图标intent查找适配 , change by shlt@2015/02/03 DEL START
			ComponentName cn = getAppShortcutComponentName( packageManager , xmlMap );
			if( cn != null )
			{
				String packageName = cn.getPackageName();
				String className = cn.getClassName();
				//底边栏图标intent查找适配 , change by shlt@2015/02/03 DEL END
				try
				{
					//底边栏图标intent查找适配 , change by shlt@2015/02/03 DEL START
					//ComponentName cn;
					//底边栏图标intent查找适配 , change by shlt@2015/02/03 DEL END
					try
					{
						//底边栏图标intent查找适配 , change by shlt@2015/02/03 DEL START
						//cn = new ComponentName( packageName , className );
						//底边栏图标intent查找适配 , change by shlt@2015/02/03 DEL END
						info = packageManager.getActivityInfo( cn , 0 );
					}
					catch( PackageManager.NameNotFoundException nnfe )
					{
						String[] packages = packageManager.currentToCanonicalPackageNames( new String[]{ packageName } );
						cn = new ComponentName( packages[0] , className );
						info = packageManager.getActivityInfo( cn , 0 );
					}
					id = generateNewItemId();
					intent.setComponent( cn );
					//xiatian add start	//需求：当默认配置中的“应用”图标（default_workspace.xml中的“favorite”标签）配置为浏览器时，支持配置默认主页。 
					String mBrowserDefaultUri = xmlMap.get( LauncherSettings.Favorites.BROWSER_DEFAULT_URI );
					if( mBrowserDefaultUri != null )
					{
						intent.setAction( Intent.ACTION_VIEW );
						intent.setData( Uri.parse( mBrowserDefaultUri ) );
					}
					//xiatian add end
					intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
					values.put( Favorites.INTENT , intent.toUri( 0 ) );
					values.put( Favorites.TITLE , info.loadLabel( packageManager ).toString() );
					values.put( Favorites.ITEM_TYPE , Favorites.ITEM_TYPE_APPLICATION );
					values.put( Favorites.SPANX , 1 );
					values.put( Favorites.SPANY , 1 );
					values.put( Favorites._ID , generateNewItemId() );
					//<phenix modify> liuhailin@2015-01-22 modify begin
					//if( dbInsertAndCheck( this , db , TABLE_FAVORITES_DRAWER , null , values ) < 0 )
					if( dbInsertAndCheck( this , db , getFavoritesTabName() , null , values ) < 0 )
					//<phenix modify> liuhailin@2015-01-22 modify end
					{
						return -1;
					}
				}
				catch( PackageManager.NameNotFoundException e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.w( TAG , StringUtils.concat( "Unable to add favorite: " , packageName , "/" , className ) , e );
				}
			}
			return id;
		}
		
		/**
		 *获取最正确的ComponentName 
		 */
		private ComponentName getAppShortcutComponentName(
				PackageManager packageManager ,
				HashMap<String , String> xmlMap )
		{
			//在default_workapce.xml中将需要的属性get出来
			String packageName = xmlMap.get( LauncherSettings.Favorites.PACKAGE_NAME );
			String className = xmlMap.get( LauncherSettings.Favorites.CLASS_NAME );
			ComponentName finalComponentName = null;
			//xiatian add start	//为了我们出去的公版在演示的时候能底边栏的四个图标能够查找到相应的应用，当底边栏item的包类名都为空时，按照screen来获取intent，并按照intent来查找合适的应用。
			if( TextUtils.isEmpty( packageName ) && TextUtils.isEmpty( className ) )
			{
				String mDefaultHotseatItemIntent = getDefaultHotseatItemIntent( xmlMap );//需要接受那种intent的apk
				if( !TextUtils.isEmpty( mDefaultHotseatItemIntent ) )//matchIntent是空就不去干什么了，直接返回配置的PackageName和ClassName
				{
					try
					{
						Intent searchIntent = Intent.parseUri( mDefaultHotseatItemIntent , 0 );
						List<ResolveInfo> list = packageManager.queryIntentActivities( searchIntent , 0 );//查询所有能接受指定intent的apk
						if( list != null && list.size() > 0 )
						{
							//如果没有可替换包类名和优先使用apk，就那内置第一个apk
							if( finalComponentName == null )
							{
								for( ResolveInfo resolveInfo : list )
								{
									int flag = resolveInfo.activityInfo.applicationInfo.flags;
									if( ( flag & ApplicationInfo.FLAG_SYSTEM ) != 0 || ( flag & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
									{
										finalComponentName = new ComponentName( resolveInfo.activityInfo.applicationInfo.packageName , resolveInfo.activityInfo.name );
										break;
									}
								}
							}
							//如果连内置的都没有，就使用第一个吧
							if( finalComponentName == null )
							{
								ResolveInfo resolveInfo = list.get( 0 );
								finalComponentName = new ComponentName( resolveInfo.activityInfo.applicationInfo.packageName , resolveInfo.activityInfo.name );
							}
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			}
			//xiatian add end
			//如果什么都没有，你配什么PackageName和ClassName我就给你神马！
			if( finalComponentName == null )
				finalComponentName = new ComponentName( packageName , className );
			return finalComponentName;
		}
		
		private long addFolder(
				SQLiteDatabase db ,
				ContentValues values )
		{
			values.put( Favorites.ITEM_TYPE , Favorites.ITEM_TYPE_FOLDER );
			values.put( Favorites.SPANX , 1 );
			values.put( Favorites.SPANY , 1 );
			long id = generateNewItemId();
			values.put( Favorites._ID , id );
			//<phenix modify> liuhailin@2015-01-22 modify begin
			//if( dbInsertAndCheck( this , db , TABLE_FAVORITES_DRAWER , null , values ) <= 0 )
			if( dbInsertAndCheck( this , db , getFavoritesTabName() , null , values ) <= 0 )
			//<phenix modify> liuhailin@2015-01-22 modify end
			{
				return -1;
			}
			else
			{
				return id;
			}
		}
		
		private ComponentName getSearchWidgetProvider()
		{
			SearchManager searchManager = (SearchManager)mContext.getSystemService( Context.SEARCH_SERVICE );
			// gaominghui@2016/12/14 UPD START
			////ComponentName searchComponent = searchManager.getGlobalSearchActivity();
			ComponentName searchComponent = null;
			if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
			{
				searchComponent = searchManager.getGlobalSearchActivity();
			}
			else
			{
				List<SearchableInfo> list = searchManager.getSearchablesInGlobalSearch();
				if( list != null && list.size() > 0 )
				{
					searchComponent = list.get( 0 ).getSearchActivity();
				}
			}
			// gaominghui@2016/12/14 UPD END
			if( searchComponent == null )
				return null;
			return getProviderInPackage( searchComponent.getPackageName() );
		}
		
		/**
		 * Gets an appwidget provider from the given package. If the package contains more than
		 * one appwidget provider, an arbitrary one is returned.
		 */
		private ComponentName getProviderInPackage(
				String packageName )
		{
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( mContext );
			List<AppWidgetProviderInfo> providers = appWidgetManager.getInstalledProviders();
			if( providers == null )
				return null;
			final int providerCount = providers.size();
			for( int i = 0 ; i < providerCount ; i++ )
			{
				ComponentName provider = providers.get( i ).provider;
				if( provider != null && provider.getPackageName().equals( packageName ) )
				{
					return provider;
				}
			}
			return null;
		}
		
		private boolean addSearchWidget(
				SQLiteDatabase db ,
				ContentValues values )
		{
			ComponentName cn = getSearchWidgetProvider();
			return addAppWidget( db , values , cn , 4 , 1 );
		}
		
		private boolean addClockWidget(
				SQLiteDatabase db ,
				ContentValues values )
		{
			ComponentName cn = new ComponentName( "com.android.alarmclock" , "com.android.alarmclock.AnalogAppWidgetProvider" );
			return addAppWidget( db , values , cn , 2 , 2 );
		}
		
		private boolean canAddAppWidget(
				ComponentName cn ,
				PackageManager packageManager )
		{
			if( cn == null )
			{
				return false;
			}
			//WangLei add start //实现默认配置AppWidget的流程
			if( LauncherAppState.isAppInstalledSdcard( cn.getPackageName() , packageManager ) == true )
			{//应用装在T卡中的时候，该应用所包含的插件，通过AppWidgetManager获取不到
				return false;
			}
			//WangLei add end
			boolean hasPackage = true;
			try
			{
				packageManager.getReceiverInfo( cn , 0 );
			}
			catch( Exception e )
			{
				String[] packages = packageManager.currentToCanonicalPackageNames( new String[]{ cn.getPackageName() } );
				cn = new ComponentName( packages[0] , cn.getClassName() );
				try
				{
					packageManager.getReceiverInfo( cn , 0 );
				}
				catch( Exception e1 )
				{
					hasPackage = false;
				}
			}
			return hasPackage;
		}
		
		private boolean addAppWidget(
				SQLiteDatabase db ,
				ContentValues values ,
				ComponentName cn ,
				int spanX ,
				int spanY )
		{
			boolean allocatedAppWidgets = false;
			final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( mContext );
			try
			{
				int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
				values.put( Favorites.ITEM_TYPE , Favorites.ITEM_TYPE_APPWIDGET );
				values.put( Favorites.SPANX , spanX );
				values.put( Favorites.SPANY , spanY );
				values.put( Favorites.APPWIDGET_ID , appWidgetId );
				values.put( Favorites.APPWIDGET_PROVIDER , cn.flattenToString() );
				values.put( Favorites._ID , generateNewItemId() );
				//<phenix modify> liuhailin@2015-01-22 modify begin
				//dbInsertAndCheck( this , db , TABLE_FAVORITES_DRAWER , null , values );
				dbInsertAndCheck( this , db , getFavoritesTabName() , null , values );
				//<phenix modify> liuhailin@2015-01-22 modify end
				allocatedAppWidgets = true;
				// TODO: need to check return value
				//WangLei add //实现默认配置AppWidget的流程
				//appWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , cn );//WangLei del
				//WangLei add start 
				if( Build.VERSION.SDK_INT >= 15 ) //现在桌面最低支持API-19,以后可能会改，所以加上这个判断
				{
					/**检查是否绑定成功*/
					boolean isBind = false;
					// gaominghui@2016/12/14 ADD START 兼容 android 4.0
					if( Build.VERSION.SDK_INT >= 16 )
						isBind = appWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , cn );
					else
						isBind = Tools.bindAppWidgetId( appWidgetManager , appWidgetId , cn );
					// gaominghui@2016/12/14 ADD END 兼容 android 4.0
					if( !isBind )
					{
						/**直接绑定失败，请求系统绑定*/
						try
						{
							Intent bindIntent = new Intent( AppWidgetManager.ACTION_APPWIDGET_BIND );
							/**获取插件配置的基本信息*/
							bindIntent.putExtra( LauncherSettings.Favorites.CELLX , values.getAsInteger( LauncherSettings.Favorites.CELLX ) );
							bindIntent.putExtra( LauncherSettings.Favorites.CELLY , values.getAsInteger( LauncherSettings.Favorites.CELLY ) );
							bindIntent.putExtra( LauncherSettings.Favorites.SPANX , values.getAsInteger( LauncherSettings.Favorites.SPANX ) );
							bindIntent.putExtra( LauncherSettings.Favorites.SPANY , values.getAsInteger( LauncherSettings.Favorites.SPANY ) );
							bindIntent.putExtra( LauncherSettings.Favorites.SCREEN , values.getAsLong( LauncherSettings.Favorites.SCREEN ) );
							/**请求绑定插件操作必须的参数*/
							bindIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , appWidgetId );
							bindIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_PROVIDER , cn );
							mLauncher.startActivityForResultNeedCatchException( bindIntent , REQUEST_BIND_DEFAULT_APPWIDGET );//zhujieping add，这里要调用能够抛出异常的方法，出现异常则不执行下面的startlock，否则会一直锁死
							startLock();
						}
						catch( Exception ex )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , "request bind failed" );
						}
					}
				}
				else
				{
					//当前SDK版本为4.4,没有这个方法，以后支持低版本后(API15以下)，可直接调用此方法去直接绑定插件
					//appWidgetManager.bindAppWidgetId( appWidgetId , cn ); 
				}
				//WangLei add end
				//WangLei end
			}
			catch( RuntimeException ex )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "Problem allocating appWidgetId" , ex );
			}
			return allocatedAppWidgets;
		}
		
		private long
		//xiatian start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//		addUriShortcut//xiatian del
		addShortcut//xiatian add
		//xiatian end
		(
				SQLiteDatabase db ,
				ContentValues values ,
				HashMap<String , String> xmlMap )
		{
			//xiatian start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
			//xiatian del start
			//			Resources r = mContext.getResources();
			//			final int iconResId = a.getResourceId( R.styleable.Favorite_icon , 0 );
			//			final int titleResId = a.getResourceId( R.styleable.Favorite_title , 0 );
			//			Intent intent;
			//			String uri = null;
			//			try
			//			{
			//				uri = a.getString( R.styleable.Favorite_uri );
			//				intent = Intent.parseUri( uri , 0 );
			//			}
			//			catch( URISyntaxException e )
			//			{
			//				Log.w( TAG , "Shortcut has malformed uri: " + uri );
			//				return -1; // Oh well
			//			}
			//			if( iconResId == 0 || titleResId == 0 )
			//			{
			//				Log.w( TAG , "Shortcut is missing title or icon resource ID" );
			//				return -1;
			//			}
			//			long id = generateNewItemId();
			//			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			//			values.put( Favorites.INTENT , intent.toUri( 0 ) );
			//			values.put( Favorites.TITLE , r.getString( titleResId ) );
			//			values.put( Favorites.ITEM_TYPE , Favorites.ITEM_TYPE_SHORTCUT );
			//			values.put( Favorites.SPANX , 1 );
			//			values.put( Favorites.SPANY , 1 );
			//			values.put( Favorites.ICON_TYPE , Favorites.ICON_TYPE_RESOURCE );
			//			values.put( Favorites.ICON_PACKAGE , mContext.getPackageName() );
			//			values.put( Favorites.ICON_RESOURCE , r.getResourceName( iconResId ) );
			//			values.put( Favorites._ID , id );
			//			//<phenix modify> liuhailin@2015-01-22 modify begin
			//			//if( dbInsertAndCheck( this , db , TABLE_FAVORITES_DRAWER , null , values ) < 0 )
			//			if( dbInsertAndCheck( this , db , getFavoritesTabName() , null , values ) < 0 )
			//			//<phenix modify> liuhailin@2015-01-22 modify end
			//			{
			//				return -1;
			//			}
			//			return id;
			//xiatian del end
			return addUriShortcut( db , values , Favorites.ITEM_TYPE_SHORTCUT , xmlMap );//xiatian add
			//xiatian end
		}
		
		//xiatian add start	//为了我们出去的公版在演示的时候能底边栏的四个图标能够查找到相应的应用，当底边栏item的包类名都为空时，按照screen来获取intent，并按照intent来查找合适的应用。
		private String getDefaultHotseatItemIntent(
				HashMap<String , String> xmlMap )
		{
			String mIntent = null;
			//cheyingkun add start	//开放default_workspace.xml给客户配置(路径/system/launcher/default_workspace.xml)
			if( xmlMap == null )
			{
				return null;
			}
			String containerStr = xmlMap.get( LauncherSettings.Favorites.CONTAINER );
			String mIndexStr = xmlMap.get( LauncherSettings.Favorites.CELLX );
			Long container = Long.valueOf( containerStr );
			Integer mIndex = Integer.valueOf( mIndexStr );
			String key = xmlMap.get( "key" );
			//cheyingkun add start	end
			if( container != LauncherSettings.Favorites.CONTAINER_HOTSEAT )
			{
				return null;
			}
			//cheyingkun add start	//配置双层模式下主菜单图标位置。可配置-1（自适应居中）、0（底边栏第一个图标）、1（底边栏第二个图标）等等（必须小于底边栏列数config_hotseat_columns_double）。当配置为-1时：底边栏列数为奇数则为居中，底边栏列数为偶数则第“底边栏列数除以2，再加1”个图标。【c_0004381】
			//不能写死固定的数字,因为不确定底边栏个数(和主菜单位置)
			DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
			if(
			//
			( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER /*//cheyingkun add	//单双层分开配置(底边栏 )*/)
			//
			&& mIndex > deviceProfile.getHotseatAllAppsRank()
			//
			)
			//cheyingkun add end
			{
				//为了使用同一个配置适配单双层，我们现在底边栏的顺序是0、1、3、4；“2”留给双层模式主菜单入口。
				//所以，我们现在在此加特殊处理，修正mIndex。
				mIndex--;
			}
			//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
			if( key != null )
			{
				if( key.equals( "phone" ) )
				{
					mIndex = 0;
				}
				else if( key.equals( "contact" ) )
				{
					mIndex = 1;
				}
				else if( key.equals( "message" ) )
				{
					mIndex = 2;
				}
				else if( key.equals( "browser" ) )
				{
					mIndex = 3;
				}
			}
			//zhujieping add end
			//chenliang start	//解决“由于安卓api23以上短信的intent‘vnd.android-dir/mms-sms’不再支持”导致桌面底边栏缺少信息图标的问题。
			//chenliang del start
			//			if( mIndex > -1 && mIndex < DEFAULT_HOTSEAT_ITEMS_INTENT.length )
			//			{
			//				mIntent = DEFAULT_HOTSEAT_ITEMS_INTENT[mIndex];
			//			}
			//chenliang del end
			//chenliang add start
			if( mIndex > -1 )
			{
				switch( mIndex )
				{
					case 0:
						mIntent = DEFAULT_HOTSET_ITEM_KEY_DIAL;
						break;
					case 1:
						mIntent = DEFAULT_HOTSET_ITEM_KEY_CONTACTS;
						break;
					case 2:
						if( Build.VERSION.SDK_INT >= 23 )
						{
							mIntent = DEFAULT_HOTSET_ITEM_KEY_HIGH_SMS;
						}
						else
						{
							mIntent = DEFAULT_HOTSET_ITEM_KEY_SMS;
						}
						break;
					case 3:
						mIntent = DEFAULT_HOTSET_ITEM_KEY_WEBSITE;
						break;
					default:
						break;
				}
				//chenliang add end
				//chenliang end
			}
			return mIntent;
		}
		//xiatian add end
		;
		
		//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		private long addUriShortcut(
				SQLiteDatabase db ,
				ContentValues values ,
				int mItemType ,
				HashMap<String , String> xmlMap )
		{
			//cheyingkun add start	//开放default_workspace.xml给客户配置(路径/system/launcher/default_workspace.xml)
			if( xmlMap == null )
			{
				return -1;
			}
			boolean isCustonXml = Boolean.valueOf( xmlMap.get( IS_CUSTOM_XML ) );
			String iconStr = xmlMap.get( LauncherSettings.Favorites.ICON );
			String titleStr = xmlMap.get( LauncherSettings.Favorites.TITLE );
			final int iconResId = stringChangeToResId( iconStr , "@drawable/" , "drawable" , isCustonXml );
			final int titleResId = stringChangeToResId( titleStr , "@string/" , "string" , isCustonXml );
			String uri = xmlMap.get( LauncherSettings.Favorites.URI );
			//cheyingkun add end
			Resources r = mContext.getResources();
			Intent intent;
			try
			{
				intent = Intent.parseUri( uri , 0 );
			}
			catch( URISyntaxException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , StringUtils.concat( "Shortcut has malformed uri: " , uri ) );
				return -1; // Oh well
			}
			if( iconResId == 0 || titleResId == 0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Shortcut is missing title or icon resource ID" );
				return -1;
			}
			//cheyingkun add start	//修改桌面默认配置(双层模式不显示智能分类虚图标)
			if( Favorites.ITEM_TYPE_VIRTUAL == mItemType )
			{
				int virtaulaType = intent.getIntExtra( VirtualInfo.VIRTUAL_TYPE , VirtualInfo.VIRTUAL_TYPE_ERROR );
				if( virtaulaType == VirtualInfo.VIRTUAL_TYPE_CATEGORY_ENTRY )
				{
					if(
					//
					( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
					//
					|| ( CategoryParse.canShowCategory() == false )
					//
					)
					{
						return -1;
					}
				}
			}
			//cheyingkun add end
			long id = generateNewItemId();
			//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(不设置flags)
			ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( mContext );
			if( !( intent != null && mZhiKeShortcutManager.isZhiKeShortcut( intent.getComponent() ) ) )
			//cheyingkun add end
			{
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			}
			values.put( Favorites.INTENT , intent.toUri( 0 ) );
			//xiatian start	//fix bug：解决“桌面上默认配置快捷方式和虚图标，切换语言后图标的名称没有切换为相应语言”的问题。
			//			String mTitle = r.getString( titleResId );//xiatian del
			String mTitle = StringUtils.concat( LauncherProvider.ITEM_TITLE_RESOURCE_NAME_KEY , r.getResourceName( titleResId ) );//xiatian add
			//xiatian end
			values.put( Favorites.TITLE , mTitle );
			values.put( Favorites.ITEM_TYPE , mItemType );
			values.put( Favorites.SPANX , 1 );
			values.put( Favorites.SPANY , 1 );
			values.put( Favorites.ICON_TYPE , Favorites.ICON_TYPE_RESOURCE );
			values.put( Favorites.ICON_PACKAGE , mContext.getPackageName() );
			values.put( Favorites.ICON_RESOURCE , r.getResourceName( iconResId ) );
			values.put( Favorites._ID , id );
			//<phenix modify> liuhailin@2015-01-22 modify begin
			//if( dbInsertAndCheck( this , db , TABLE_FAVORITES_DRAWER , null , values ) < 0 )
			if( dbInsertAndCheck( this , db , getFavoritesTabName() , null , values ) < 0 )
			//<phenix modify> liuhailin@2015-01-22 modify end
			{
				return -1;
			}
			return id;
		}
		
		private long addVirtual(
				SQLiteDatabase db ,
				ContentValues values ,
				HashMap<String , String> xmlMap )
		{
			return addUriShortcut( db , values , Favorites.ITEM_TYPE_VIRTUAL , xmlMap );
		}
		
		//xiatian add end
		//cheyingkun add start	//桌面支持配置空位【c_0003636】
		private void addEmptySeat(
				String screen ,
				String cellX ,
				String cellY )
		{
			try
			{
				EmtySeatEntity mEmtySeatEntity = new EmtySeatEntity( Integer.valueOf( screen ) , Integer.valueOf( cellX ) , Integer.valueOf( cellY ) );
				LauncherModel.mEmtySeatEntityList.add( mEmtySeatEntity );
			}
			catch( NumberFormatException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "cyk LauncherProvider loadFavorites addEmptySeat error" );
			}
			return;
		}
		
		/**
		 * 保存默认配置的空格子
		 * @param sp 保存到SharedPreferences里。注意跟@{@link #readDefaultEntySeat(SharedPreferences)}里的sp保持一致才能正确读写
		 */
		public void writeDefaultEntySeat(
				SharedPreferences sp )
		{
			if( sp != null && LauncherModel.mEmtySeatEntityList.size() != 0 )
			{
				Editor edit = sp.edit();
				Set<String> mEmtySeatEntityWriteSet = new HashSet<String>();
				for( EmtySeatEntity mEmtySeatEntity : LauncherModel.mEmtySeatEntityList )
				{
					String string = mEmtySeatEntity.toString();
					mEmtySeatEntityWriteSet.add( string );
				}
				edit.putStringSet( TAG_EMPTY_SEAT , mEmtySeatEntityWriteSet );
				edit.commit();
			}
		}
		
		/**
		 * 读取默认配置的空格子
		 * @param sp 保存到SharedPreferences里。注意跟@{@link #writeDefaultEntySeat(SharedPreferences)}里的sp保持一致才能正确读写
		 */
		public void readDefaultEntySeat(
				SharedPreferences sp )
		{
			if( sp != null && LauncherModel.mEmtySeatEntityList.size() == 0 )
			{
				Set<String> mEmtySeatEntityReadSet = sp.getStringSet( TAG_EMPTY_SEAT , null );
				if( mEmtySeatEntityReadSet != null && mEmtySeatEntityReadSet.size() > 0 )
				{
					for( String string : mEmtySeatEntityReadSet )
					{
						EmtySeatEntity mEmtySeatEntity = EmtySeatEntity.stringToEmtySeatEntity( string );
						if( mEmtySeatEntity != null )
						{
							LauncherModel.mEmtySeatEntityList.add( mEmtySeatEntity );
						}
					}
				}
			}
		}
		
		//cheyingkun add end
		//cheyingkun add start	//单双层默认图标独立配置文件
		private XmlPullParser getDefaultWorkspaceXmlPullParser(
				InputStream inputStream )
		{
			XmlPullParser parser = null;
			//如果字符流不为空,说明客户配置了,按照客户配置来
			if( inputStream != null )
			{
				isCustomXml = false;
				InputStreamReader fileInputStream = new InputStreamReader( inputStream );
				parser = Xml.newPullParser();
				try
				{
					parser.setInput( fileInputStream );
				}
				catch( XmlPullParserException e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , " getDefaultWorkspace: Got exception parsing favorites." , e );
					e.printStackTrace();
				}
			}
			//否则解析我们代码中的 R.xml.default_workspace 
			else
			{
				isCustomXml = true;
				//单双层读取不同的配置文件
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
				{
					parser = mContext.getResources().getXml( R.xml.default_workspace );
				}
				else if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
				{
					parser = mContext.getResources().getXml( R.xml.default_workspace_double );
				}
				else
				{
					throw new IllegalStateException( StringUtils.concat( "getDefaultWorkspaceXmlPullParser - error CONFIG_LAUNCHER_STYLE: " , LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE ) );
				}
			}
			return parser;
		}
		//cheyingkun add end
	}
	
	/**
	 * Build a query string that will match any row where the column matches
	 * anything in the values list.
	 */
	static String buildOrWhereString(
			String column ,
			int[] values )
	{
		StringBuilder selectWhere = new StringBuilder();
		for( int i = values.length - 1 ; i >= 0 ; i-- )
		{
			selectWhere.append( column ).append( "=" ).append( values[i] );
			if( i > 0 )
			{
				selectWhere.append( " OR " );
			}
		}
		return selectWhere.toString();
	}
	
	static class SqlArguments
	{
		
		public final String table;
		public final String where;
		public final String[] args;
		
		SqlArguments(
				Uri url ,
				String where ,
				String[] args )
		{
			if( url.getPathSegments().size() == 1 )
			{
				this.table = url.getPathSegments().get( 0 );
				this.where = where;
				this.args = args;
			}
			else if( url.getPathSegments().size() != 2 )
			{
				throw new IllegalArgumentException( StringUtils.concat( "Invalid URI: " , url ) );
			}
			else if( !TextUtils.isEmpty( where ) )
			{
				throw new UnsupportedOperationException( StringUtils.concat( "WHERE clause not supported: " , url ) );
			}
			else
			{
				this.table = url.getPathSegments().get( 0 );
				this.where = StringUtils.concat( "_id=" , ContentUris.parseId( url ) );
				this.args = null;
			}
		}
		
		SqlArguments(
				Uri url )
		{
			if( url.getPathSegments().size() == 1 )
			{
				table = url.getPathSegments().get( 0 );
				where = null;
				args = null;
			}
			else
			{
				throw new IllegalArgumentException( StringUtils.concat( "Invalid URI: " , url ) );
			}
		}
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public SQLiteDatabase getProviderDB()
	{
		if( null == mOpenHelper )
		{
			return null;
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return db;
	}
	
	public DatabaseHelper getOpenHelper()
	{
		return mOpenHelper;
	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	
	//WangLei add start //实现默认配置AppWidget的流程
	static void setLauncher(
			Launcher launcher )
	{
		mLauncher = launcher;
	}
	
	/**开始同步锁*/
	static void startLock()
	{
		synchronized( lock )
		{
			try
			{
				lock.wait();
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	/**停止同步锁*/
	static void stopLock()
	{
		synchronized( lock )
		{
			lock.notify();
		}
	}
	
	//WangLei add end
	// gaominghui@2017/02/14 ADD START //解决"当主页面上只有小组件，智能分类后，该页面空白"的问题【i_0014888】。
	//记录是否为第一次加载的标志位
	private boolean mIsFirst = true;
	
	public boolean isFirst()
	{
		return mIsFirst;
	}
	// gaominghui@2017/02/14 ADD END
}
