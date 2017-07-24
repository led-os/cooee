package com.cooee.phenix;


import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.DLManager.DlNotifyManager;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerByBroadcast;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerByBroadcastXH;
import com.cooee.framework.function.OperateAPK.OperateAPKManager;
import com.cooee.framework.function.OperateExplorer.OperateExplorer;
import com.cooee.framework.function.OperateFavorites.OperateFavorites;
import com.cooee.framework.function.OperateMediaPluginPage.OperateMediaPluginDataManager;
import com.cooee.framework.function.OperateUmeng.OperateUmeng;
import com.cooee.phenix.AppList.KitKat.WidgetPreviewLoader;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.launcherSettings.LauncherSettingsActivity;
import com.cooee.theme.ThemeReceiver;
import com.cooee.wallpaper.host.util.SystemBarTintManager;

import cool.sdk.Category.CategoryUpdate;
import cool.sdk.search.SearchHelper;


public class LauncherAppState extends BaseAppState
{
	
	private static final String TAG = "LauncherAppState";
	private static final String SHARED_PREFERENCES_KEY = "com.cooee.phenix.prefs";
	public static final String REMOVE_TCRADMOUNT_GRAY_APP = "remove_TCradMount_gray_app";//cheyingkun add	//deleteGreyApp(删除灰化图标的广播字符串)
	public static final String ACTION_HIDEAPP = "com.example.cnpalms.HIDEAPP";//lvjiangbin add for xiangcheng hidapp 
	private LauncherModel mModel;
	private IconCache mIconCache;
	private WidgetPreviewLoader.CacheDb mWidgetPreviewCacheDb;
	private float mScreenDensity;
	//xiatian add start
	//【备注】
	//对mLongPressTimeout进行如下说明：
	//	该值需要比ViewConfiguration.getLongPressTimeout()-ViewConfiguration.getTapTimeout()的结果要小，否则先响应View.java中的CheckLongPressHelper，而不是BubbleTextView.java中的CheckLongPressHelper;
	//xiatian add end
	private int mLongPressTimeout = 300;
	private static WeakReference<LauncherProvider> sLauncherProvider;
	private static LauncherAppState INSTANCE;
	private DynamicGrid mDynamicGrid;
	private LauncherSettingsActivity mLauncherSettingsActivity = null;//xiatian add	//fix bug：解决“在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，桌面特效为之前的桌面特效（没有保存刚刚在屏幕切页特效界面中的选择）”的问题。【i_0010438】
	private UnreadHelper mUnreadHelper = null;//xiatian add	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
	private Launcher mLauncher = null;
	private int mFullScreenOriginalHeight = -1;
	private LocalBroadcastManager mLocalBroadcastManager = null;//本地广播使用，目前为当运营文件夹应用下载完毕以后使用， wanghongjian add
	//cheyingkun add start	//是否监听飞利浦壁纸改变广播【c_0003456】
	public final static String WALLPAPER_DESIRED_WIDTH = "wallpaperDesiredWidth";
	public final static String WALLPAPER_DESIRED_HEIGHT = "wallpaperDesiredHeight";
	public final static String WALLPAPER_CHANGED_PHILIPS = "com.cooee.philips.wallpaperChanged";
	//cheyingkun add end
	private SystemBarTintManager mSystemBarTintManager = null;;
	// gaominghui@2016/12/14 ADD START
	public static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 0x00000100;//View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	public static final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 0x00000400;//View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	public static final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 0x00000200;//View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	public static final int FLAG_TRANSLUCENT_STATUS = 0x04000000;// WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
	public static final int FLAG_TRANSLUCENT_NAVIGATION = 0x08000000;//WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
	
	// gaominghui@2016/12/14 ADD END
	public static LauncherAppState getInstance()
	{
		if( INSTANCE == null )
		{
			INSTANCE = new LauncherAppState();
		}
		return INSTANCE;
	}
	
	public static LauncherAppState getInstanceNoCreate()
	{
		return INSTANCE;
	}
	
	public Context getContext()
	{
		return sContext;
	}
	
	public static void setApplicationContext(
			Context context )
	{
		if( sContext != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( Launcher.TAG , "setApplicationContext called twice! old=" + sContext + " new=" + context );
		}
		sContext = context.getApplicationContext();
	}
	
	private LauncherAppState()
	{
		if( sContext == null )
		{
			throw new IllegalStateException( "LauncherAppState inited before app context set" );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( Launcher.TAG , "LauncherAppState inited" );
		if( LauncherDefaultConfig.getBoolean( R.bool.debug_memory_enabled ) )
		{
			MemoryTracker.startTrackingMe( sContext , "L" );
		}
		// set sIsScreenXLarge and mScreenDensity *before* creating icon cache
		mScreenDensity = sContext.getResources().getDisplayMetrics().density;
		mWidgetPreviewCacheDb = new WidgetPreviewLoader.CacheDb( sContext );
		mIconCache = new IconCache( sContext );
		mModel = new LauncherModel( this , mIconCache );
		// Register intent receivers
		IntentFilter filter = new IntentFilter( Intent.ACTION_PACKAGE_ADDED );
		filter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		filter.addAction( Intent.ACTION_PACKAGE_CHANGED );
		filter.addDataScheme( "package" );
		sContext.registerReceiver( mModel , filter );
		filter = new IntentFilter();
		filter.addAction( Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE );
		filter.addAction( Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE );
		filter.addAction( Intent.ACTION_LOCALE_CHANGED );
		filter.addAction( Intent.ACTION_CONFIGURATION_CHANGED );
		sContext.registerReceiver( mModel , filter );
		filter = new IntentFilter();
		filter.addAction( SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED );
		sContext.registerReceiver( mModel , filter );
		filter = new IntentFilter();
		filter.addAction( SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED );
		sContext.registerReceiver( mModel , filter );
		// Register for changes to the favorites
		ContentResolver resolver = sContext.getContentResolver();
		resolver.registerContentObserver( LauncherSettings.Favorites.CONTENT_URI , true , mFavoritesObserver );
		mUnreadHelper = new UnreadHelper( sContext );//xiatian add	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
		//cheyingkun add start	//deleteGreyApp(删除灰化图标的广播字符串)bug:i_0009469
		filter = new IntentFilter();
		filter.addAction( REMOVE_TCRADMOUNT_GRAY_APP );
		sContext.registerReceiver( mModel , filter );
		//cheyingkun add end
		//运营文件夹下载完成以后发送广播给launcher wanghongjian add
		mLocalBroadcastManager = LocalBroadcastManager.getInstance( sContext );
		filter = new IntentFilter();
		filter.addAction( DlNotifyManager.OPERATE_FOLDER_DOWNLOAD_DONE_ACTION );
		mLocalBroadcastManager.registerReceiver( mModel , filter );
		//wanghongjian end
		//cheyingkun add start	//是否监听飞利浦壁纸改变广播
		filter = new IntentFilter();
		filter.addAction( WALLPAPER_CHANGED_PHILIPS );
		sContext.registerReceiver( mModel , filter );
		//cheyingkun add end
		//lvjiangbin add for xiangcheng hindapp start
		filter = new IntentFilter();
		filter.addAction( ACTION_HIDEAPP );
		sContext.registerReceiver( mModel , filter );
		//lvjiangbin add for xiangcheng hindapp  end
		//lvjiangbin add for SIM change start
		filter = new IntentFilter();
		filter.addAction( "android.intent.action.SIM_STATE_CHANGED" );
		sContext.registerReceiver( mModel , filter );
		//lvjiangbin add for  SIM change  end  
		//xiatian add start	//通知桌面切页：“晨想”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerByBroadcast.java”中的备注。
		NotifyLauncherSnapPageManagerByBroadcast mNotifyLauncherSnapPageManagerByBroadcast = NotifyLauncherSnapPageManagerByBroadcast.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcast != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcast.register( sContext );
		}
		//xiatian add end
		//gaominghui add start //通知桌面切页：“讯虎”定制功能（双向光感切页）。详见“NotifyLauncherSnapPageManagerByBroadcastXH.java"中的备注
		NotifyLauncherSnapPageManagerByBroadcastXH mNotifyLauncherSnapPageManagerByBroadcastXH = NotifyLauncherSnapPageManagerByBroadcastXH.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcastXH != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcastXH.register( sContext );
		}
		//gaominghui add end
		//xiatian add start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH )
		{
			filter = new IntentFilter();
			filter.addAction( LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_ACTION );
			sContext.registerReceiver( mModel , filter );
		}
		//xiatian add end
	}
	
	/**
	 * Call from Application.onTerminate(), which is not guaranteed to ever be called.
	 */
	public void onTerminate()
	{
		sContext.unregisterReceiver( mModel );
		ContentResolver resolver = sContext.getContentResolver();
		resolver.unregisterContentObserver( mFavoritesObserver );
		//xiatian add start	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
		if( mUnreadHelper != null )
		{
			mUnreadHelper.onTerminate( resolver );
		}
		//xiatian add end
		if( mLocalBroadcastManager != null )
		{
			mLocalBroadcastManager.unregisterReceiver( mModel );//wanghongjian add
		}
		//xiatian add start	//通知桌面切页：“晨想”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerByBroadcast.java”中的备注。
		NotifyLauncherSnapPageManagerByBroadcast mNotifyLauncherSnapPageManagerByBroadcast = NotifyLauncherSnapPageManagerByBroadcast.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcast != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcast.unRegister( sContext );
		}
		//xiatian add end
		//gaominghui add start //通知桌面切页：“讯虎”定制功能（双向光感切页）。详见“NotifyLauncherSnapPageManagerByBroadcastXH.java"中的备注
		NotifyLauncherSnapPageManagerByBroadcastXH mNotifyLauncherSnapPageManagerByBroadcastXH = NotifyLauncherSnapPageManagerByBroadcastXH.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcastXH != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcastXH.unRegister( sContext );
		}
		//gaominghui add end
	}
	
	/**
	 * Receives notifications whenever the user favorites have changed.
	 */
	private final ContentObserver mFavoritesObserver = new ContentObserver( new Handler() ) {
		
		@Override
		public void onChange(
				boolean selfChange )
		{
			// If the database has ever changed, then we really need to force a reload of the
			// workspace on the next load
			mModel.resetLoadedState( false , true );
			mModel.startLoaderFromBackground();
		}
	};
	
	LauncherModel setLauncher(
			Launcher launcher )
	{
		if( mModel == null )
		{
			throw new IllegalStateException( "setLauncher() called before init()" );
		}
		mModel.initialize( launcher );
		//xiatian add start	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
		if( mUnreadHelper != null )
		{
			mUnreadHelper.initialize( launcher );
		}
		//xiatian add end
		ThemeReceiver.initialize( launcher );//zhujieping add	//换主题不重启
		mLauncher = launcher;
		LauncherProvider.setLauncher( launcher ); //WangLei add  //实现默认配置AppWidget的流程
		OperateAPKManager.setCallbacks( launcher );//xiatian add	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
		SearchHelper.setCallbacks( launcher );//xiatian add	//需求：运营酷搜（通过服务器配置开关来决定桌面显示或者隐藏酷搜）。
		OperateUmeng.setCallbacks( launcher );//xiatian add	//需求：运营友盟（详见“OperateUmeng”中说明）。
		CategoryUpdate.setCallbacks( launcher );//xiatian add	//添加智能分类请求数据的Callback
		OperateFavorites.setCallbacks( launcher );//xiatian add	//添加酷生活开关请求数据的Callback
		OperateExplorer.setCallbacks( launcher );//xiatian add	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
		OperateMediaPluginDataManager.setCallbacks( launcher );//gaominghui add  //需求：支持后台运营音乐页和相机页
		return mModel;
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//IconCache getIconCache()
	public IconCache getIconCache()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		return mIconCache;
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//LauncherModel getModel()
	public LauncherModel getModel()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		return mModel;
	}
	
	public WidgetPreviewLoader.CacheDb getWidgetPreviewCacheDb()
	{
		return mWidgetPreviewCacheDb;
	}
	
	static void setLauncherProvider(
			LauncherProvider provider )
	{
		sLauncherProvider = new WeakReference<LauncherProvider>( provider );
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//static LauncherProvider getLauncherProvider()
	public static LauncherProvider getLauncherProvider()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		return sLauncherProvider.get();
	}
	
	public static String getSharedPreferencesKey()
	{
		return SHARED_PREFERENCES_KEY;
	}
	
	DeviceProfile initDynamicGrid(
			Context context ,
			int minWidth ,
			int minHeight ,
			int width ,
			int height ,
			int availableWidth ,
			int availableHeight )
	{
		if( mDynamicGrid == null )
		{
			mDynamicGrid = new DynamicGrid( context , context.getResources() , minWidth , minHeight , width , height , availableWidth , availableHeight );
		}
		// Update the icon size
		DeviceProfile grid = mDynamicGrid.getDeviceProfile();
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		//		Utilities.setIconSize( grid.getIconWidthSizePx() );//xiatian del
		//xiatian add start
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			Utilities.setIconSize( grid.getIconWidthSizePx() , grid.getIconHeightSizePx() );
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			Utilities.setIconSize( grid.getSignleViewAvailableWidthPx() , grid.getSignleViewAvailableHeightPx() );
		}
		//xiatian add end
		//xiatian end
		//		grid.updateFromConfiguration( context.getResources() , width , height , availableWidth , availableHeight );//xiatian del	//整理代码：“初始化基本配置参数”相关整。现猜测该方法的本意是“手机的基本配置参数改变时，重载基本配置”，但是该操作无意义，手机基本配置参数改变时，需要重载所有配置、所有布局和所有图片。故，废除该方法。
		return grid;
	}
	
	public DynamicGrid getDynamicGrid()
	{
		return mDynamicGrid;
	}
	
	// Need a version that doesn't require an instance of LauncherAppState for the wallpaper picker
	public float getScreenDensity()
	{
		return mScreenDensity;
	}
	
	public int getLongPressTimeout()
	{
		return mLongPressTimeout;
	};
	
	//xiatian add start	//fix bug：解决“在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，桌面特效为之前的桌面特效（没有保存刚刚在屏幕切页特效界面中的选择）”的问题。【i_0010438】
	public void setLauncherSettingsActivity(
			LauncherSettingsActivity mLauncherSettingsActivity )
	{
		this.mLauncherSettingsActivity = mLauncherSettingsActivity;
	}
	
	public LauncherSettingsActivity getLauncherSettingsActivity()
	{
		return mLauncherSettingsActivity;
	}
	//xiatian add end
	;
	
	//xiatian add start	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
	public UnreadHelper getUnreadHelper()
	{
		return mUnreadHelper;
	}
	//xiatian add end
	;
	
	//WangLei add start //桌面适应手机虚拟按键
	/**获取屏幕的全部高度，包括虚拟按键*/
	public int getFullScreenHeight()
	{
		if( mFullScreenOriginalHeight != -1 )
		{
			return mFullScreenOriginalHeight;
		}
		Display originalDisplay = mLauncher.getWindowManager().getDefaultDisplay();
		DisplayMetrics originalDm = new DisplayMetrics();
		try
		{
			@SuppressWarnings( "rawtypes" )
			Class c = Class.forName( "android.view.Display" );
			@SuppressWarnings( "unchecked" )
			Method method = c.getMethod( "getRealMetrics" , DisplayMetrics.class );
			method.invoke( originalDisplay , originalDm );
			int originalHeight = originalDm.heightPixels;
			mFullScreenOriginalHeight = originalHeight;
			return originalHeight;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	/**获取除虚拟按键外屏幕其它部分的高度*/
	public int getUsefulScreenHeight()
	{
		DisplayMetrics dm = new DisplayMetrics();
		Display display = mLauncher.getWindowManager().getDefaultDisplay();
		display.getMetrics( dm );
		//cheyingkun start	//解决“获取屏幕高度出错导致的状态栏和搜索栏重叠”的问题。
		//		int usefulHeight = dm.heightPixels;//cheyingkun del
		int usefulHeight = Math.max( dm.heightPixels , dm.widthPixels );//cheyingkun add
		//cheyingkun end
		return usefulHeight;
	}
	
	/**手机是否有虚拟按键*/
	public boolean isVirtualMenuShown()
	{
		//				boolean ret = false;
		//				int originalHeight = getFullScreenHeight();
		//				int usefulHeight = getUsefulScreenHeight();
		//				if( originalHeight > 0 && usefulHeight > 0 && originalHeight > usefulHeight )
		//				{
		//					ret = true;
		//				}
		//				return ret;
		if( mSystemBarTintManager == null )
			mSystemBarTintManager = new SystemBarTintManager( mLauncher );
		return mSystemBarTintManager.getConfig().hasNavigtionBar();
	}
	//WangLei add end
	;
	
	//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
	/**获取底部虚拟键盘高度*/
	public int getVirtualKayHeightPx()
	{
		LauncherAppState instance = LauncherAppState.getInstance();
		return instance.getFullScreenHeight() - instance.getUsefulScreenHeight();
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//是否开启“状态栏透明”和“导航栏透明”效果，安卓4.4以上有效。
	public void setStatusBarAndNavigationBarTransparent()
	{
		if( mLauncher == null )
		{
			return;
		}
		Window mWindow = mLauncher.getWindow();
		if( mWindow == null )
		{
			return;
		}
		if( VERSION.SDK_INT < 21 )
		{
			if( getTranslucebtStatusFlag() != 0 )
			{
				int mTranslucebtStatusFlag = getTranslucebtStatusFlag();
				mWindow.setFlags( mTranslucebtStatusFlag , mTranslucebtStatusFlag );
			}
		}
		else
		{
			try
			{
				mWindow.getAttributes().systemUiVisibility |= ( View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION );
				mWindow.clearFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION );
				Field drawsSysBackgroundsField = WindowManager.LayoutParams.class.getField( "FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS" );
				mWindow.addFlags( drawsSysBackgroundsField.getInt( null ) );
				Method setStatusBarColorMethod = Window.class.getDeclaredMethod( "setStatusBarColor" , int.class );
				Method setNavigationBarColorMethod = Window.class.getDeclaredMethod( "setNavigationBarColor" , int.class );
				setStatusBarColorMethod.invoke( mWindow , Color.TRANSPARENT );
				setNavigationBarColorMethod.invoke( mWindow , Color.TRANSPARENT );
			}
			catch( NoSuchFieldException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "NoSuchFieldException while setting up transparent bars" );
			}
			catch( NoSuchMethodException ex )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "NoSuchMethodException while setting up transparent bars" );
			}
			catch( IllegalAccessException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "IllegalAccessException while setting up transparent bars" );
			}
			catch( IllegalArgumentException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "IllegalArgumentException while setting up transparent bars" );
			}
			catch( InvocationTargetException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "InvocationTargetException while setting up transparent bars" );
			}
			finally
			{
			}
		}
	}
	
	private int getTranslucebtStatusFlag()
	{
		if( VERSION.SDK_INT < 19 )
			return 0;
		Class<?> object = null;
		try
		{
			object = Class.forName( "android.view.WindowManager$LayoutParams" );
			Field field = object.getDeclaredField( "FLAG_TRANSLUCENT_STATUS" );
			int flag = (Integer)field.get( null );
			return flag;
		}
		catch( Exception e1 )
		{
			e1.printStackTrace();
		}
		return 0;
	}
	//cheyingkun add end
	;
	
	//zhujieping start ,获取导航栏的高度
	public int getNavigationBarHeight()
	{
		if( mSystemBarTintManager == null )
			mSystemBarTintManager = new SystemBarTintManager( mLauncher );
		return mSystemBarTintManager.getConfig().getNavigationBarHeight();
	}
	//zhujieping end
}
