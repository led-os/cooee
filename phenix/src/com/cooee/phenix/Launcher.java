package com.cooee.phenix;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog.Builder;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.TelephonyManager;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.WallpaperPickerActivity;
import com.cooee.CheckIntegrity.CheckIntegrityManager;
import com.cooee.CheckIntegrity.CheckIntegrityModeBase;
import com.cooee.center.pub.provider.PubContentProvider;
import com.cooee.center.pub.provider.PubProviderHelper;
import com.cooee.favorites.host.FavoriteConfigString;
import com.cooee.favorites.host.FavoritesPageManager;
import com.cooee.framework.CheckIntegrity.CheckIntegrityModeDownload;
import com.cooee.framework.CheckIntegrity.CheckIntegrityModeFavorites;
import com.cooee.framework.CheckIntegrity.CheckIntegrityModeKpsh;
import com.cooee.framework.CheckIntegrity.CheckIntegrityModeLauncher;
import com.cooee.framework.CheckIntegrity.CheckIntegrityModeMicroEntry;
import com.cooee.framework.CheckIntegrity.CheckIntegrityModeSearch;
import com.cooee.framework.CheckIntegrity.CheckIntegrityModeStatistics;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.Category.CategoryParse;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageCallBack;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerByBroadcast;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerByBroadcastXH;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerCustomerDSWY;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerCustomerRY;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerCustomerSingleTrackXH;
import com.cooee.framework.function.NotifyLauncherSnapPage.NotifyLauncherSnapPageManagerCustomerXH;
import com.cooee.framework.function.OperateAPK.OperateAPKManager.IOperateAPKCallbacks;
import com.cooee.framework.function.OperateExplorer.OperateExplorer;
import com.cooee.framework.function.OperateExplorer.OperateExplorer.IOperateExplorerCallbacks;
import com.cooee.framework.function.OperateFavorites.OperateFavorites;
import com.cooee.framework.function.OperateFavorites.OperateFavorites.IOperateFavoritesCallbacks;
import com.cooee.framework.function.OperateMediaPluginPage.OperateMediaPluginDataManager;
import com.cooee.framework.function.OperateMediaPluginPage.OperateMediaPluginDataManager.IOperateMediaPluginCallBack;
import com.cooee.framework.function.OperateUmeng.OperateUmeng;
import com.cooee.framework.function.OperateUmeng.OperateUmeng.IOperateUmengCallbacks;
import com.cooee.framework.function.Statistics.StatisticsBXUpdate;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.JarURLMonitor;
import com.cooee.framework.utils.LauncherConfigUtils;
import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.framework.utils.signer.SignerUtil;
import com.cooee.phenix.DropTarget.DragObject;
import com.cooee.phenix.LauncherModel.Callbacks;
import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.AppList.KitKat.AppsCustomizeTabHost;
import com.cooee.phenix.AppList.KitKat.AppsView;
import com.cooee.phenix.AppList.Marshmallow.AllAppsContainerView;
import com.cooee.phenix.AppList.Marshmallow.AllAppsTransitionController;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.Folder.kmob.KmobAdverManager;
import com.cooee.phenix.Functions.Category.ActivityUtils;
import com.cooee.phenix.Functions.Category.OperateHelp;
import com.cooee.phenix.Functions.Category.ProgressView;
import com.cooee.phenix.Functions.DefaultLauncherGuide.DefaultLauncherGuideManager;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicModel.IOperateCallbacks;
import com.cooee.phenix.Functions.DynamicEntry.OperateFolderDatabase;
import com.cooee.phenix.PagedView.PagedView;
import com.cooee.phenix.WorkspaceMenu.WorkspaceMenuVerticalList;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.config.defaultConfig.LauncherIconBaseConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.CellInfo;
import com.cooee.phenix.data.EnhanceItemInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.LauncherAppWidgetInfo;
import com.cooee.phenix.data.PendingAddWidgetInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.phenix.editmode.EditModeEntity;
import com.cooee.phenix.editmode.interfaces.IEditControlCallBack;
import com.cooee.phenix.iconhouse.IconHouseManager;
import com.cooee.phenix.launcherSettings.LauncherEffectFragment;
import com.cooee.phenix.launcherSettings.LauncherSettingsActivity;
import com.cooee.phenix.launcherSettings.PreferenceBaseSettingActivity;
import com.cooee.phenix.loading.LauncherLoading;
import com.cooee.phenix.musicpage.MusicView;
import com.cooee.phenix.util.Thunk;
import com.cooee.phenix.util.ZhiKeShortcutManager;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsBaseNew;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.theme.ThemeChangeDialog;
import com.cooee.theme.ThemeManager;
import com.cooee.theme.ThemeReceiver;
import com.cooee.theme.WallpaperManagerUtil;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.cooee.uniex.wrap.IFavoritesReady;
import com.cooee.update.UpdateIconManager;
import com.cooee.update.UpdateNotificationManager;
import com.cooee.update.UpdateUiManager;
import com.cooee.update.UpdateUtil;
import com.cooee.util.DecorateUtils;
import com.cooee.util.DownloadManager;
import com.cooee.util.Tools;
import com.cooee.wallpaper.host.WallpaperHostManager;
import com.cooee.wallpaper.wrap.IWallpaperCallbacks;
import com.cooee.wallpaper.wrap.WallpaperConfigString;
import com.cooee.wallpaperManager.WallpaperManagerBase;
import com.iLoong.launcher.desktop.Disclaimer;
import com.iLoong.launcher.desktop.DisclaimerManager;
import com.iLoong.launcher.desktop.WallpaperChooser;
import com.kpsh.sdk.KpshSdk;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.Category.CategoryHelper;
import cool.sdk.Category.CategoryUpdate.ICategoryUpdateCallbacks;
import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.KmobConfig.KmobConfigData;
import cool.sdk.KmobConfig.KmobConfigHelper;
import cool.sdk.MicroEntry.MicroEntryHelper;
import cool.sdk.Pspread.PspreadHelper;
import cool.sdk.Uiupdate.UiupdateHelper;
import cool.sdk.common.CoolMethod;
import cool.sdk.kuso.KuSoHelper;
import cool.sdk.search.SearchActivityManager;
import cool.sdk.search.SearchHelper;
import cool.sdk.search.SearchOperateUpdate.IKuSoUpdateCallbacks;
import cool.sdk.update.UpdateManagerImpl;


/**
 * Default launcher application.
 */
public class Launcher extends Activity implements View.OnClickListener , OnLongClickListener , LauncherModel.Callbacks , View.OnTouchListener
//
, OperateHelp.CategoryListener//智能分类的推荐应用
//
, IOperateCallbacks//运营文件夹
//
, Disclaimer.OnClickListener//免责申明
//
, IOperateAPKCallbacks//xiatian add	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
//
, IKuSoUpdateCallbacks//xiatian add	//需求：运营酷搜（通过服务器配置开关来决定桌面显示或者隐藏酷搜）。
//
, IOperateUmengCallbacks//xiatian add	//需求：运营友盟（详见“OperateUmeng”中说明）。
//
, ICategoryUpdateCallbacks//xiatian add	//添加智能分类请求数据的Callback
//
, IOperateFavoritesCallbacks//lvjiangbin add //添加酷生活开关可运营
//
, IWallpaperCallbacks//zhujieping add //一键换壁纸的回调
//
, IFavoritesReady
//
, IOperateExplorerCallbacks//xiatian add	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
//
, IEditControlCallBack//zhujieping add //编辑模式二级界面回调的接口
//
, IOnThemeChanged//zhujieping add //换主题不重启
//
, INotifyLauncherSnapPageCallBack//xiatian add	//通知桌面切页（代码框架）。详见“INotifyLauncherSnapPageManager”中的备注。
, IOperateMediaPluginCallBack//gaominghui add  //需求：支持后台运营音乐页和相机页
{
	
	static final String TAG = "Launcher";
	static final boolean LOGD = false;
	static final boolean PROFILE_STARTUP = false;
	static final boolean DEBUG_WIDGETS = false;
	static final boolean DEBUG_STRICT_MODE = false;
	static final boolean DEBUG_RESUME_TIME = false;
	static final boolean DEBUG_DUMP_LOG = false;
	private static final int REQUEST_CREATE_SHORTCUT = 1;
	private static final int REQUEST_CREATE_APPWIDGET = 5;
	private static final int REQUEST_PICK_APPLICATION = 6;
	private static final int REQUEST_PICK_SHORTCUT = 7;
	private static final int REQUEST_PICK_APPWIDGET = 9;
	private static final int REQUEST_PICK_WALLPAPER = 10;
	private static final int REQUEST_BIND_APPWIDGET = 11;
	/**请求绑定AppWidget的requestCode*/
	private static final int REQUEST_BIND_DEFAULT_APPWIDGET = 12; //WangLei add //实现默认配置AppWidget的流程
	//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
	private static final int REQUEST_PICK_APPWIDGET_FOR_SYSTEM_PICK_ACTIVITY = 13;
	private static final int REQUEST_CREATE_APPWIDGET_FOR_SYSTEM_PICK_ACTIVITY = 14;
	//xiatian add end
	/**
	 * IntentStarter uses request codes starting with this. This must be greater than all activity
	 * request codes used internally.
	 */
	protected static final int REQUEST_LAST = 100;
	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
	static final int SCREEN_COUNT = 5;
	private static final String PREFERENCES = "launcher.preferences";
	// To turn on these properties, type
	// adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
	static final String DUMP_STATE_PROPERTY = "launcher_dump_state";
	// The Intent extra that defines whether to ignore the launch animation
	static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION = "com.cooee.phenix.getIntent().extra.shortcut.INGORE_LAUNCH_ANIMATION";
	// Type: int
	private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
	// Type: int
	private static final String RUNTIME_STATE = "launcher.state";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
	// Type: boolean
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
	// Type: long
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
	// Type: parcelable
	private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";
	private static final String TOOLBAR_ICON_METADATA_NAME = "com.android.launcher.toolbar_icon";
	private static final String TOOLBAR_SEARCH_ICON_METADATA_NAME = "com.android.launcher.toolbar_search_icon";
	private static final String TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME = "com.android.launcher.toolbar_voice_search_icon";
	public static final String SHOW_WEIGHT_WATCHER = "debug.show_mem";
	public static final boolean SHOW_WEIGHT_WATCHER_DEFAULT = false;
	public static boolean SHOW_MARKET_BUTTON = false;
	
	/** The different states that Launcher can be in. */
	enum State
	{
		NONE , WORKSPACE , APPS_CUSTOMIZE , APPS_CUSTOMIZE_SPRING_LOADED
	};
	
	private State mState = State.WORKSPACE;
	private AnimatorSet mStateAnimation;
	static final int APPWIDGET_HOST_ID = 1024;
	public static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
	private static final int EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT = 600;
	private static final int SHOW_CLING_DURATION = 250;
	private static final int DISMISS_CLING_DURATION = 200;
	private static final Object sLock = new Object();
	// How long to wait before the new-shortcut animation automatically pans the workspace
	private static int NEW_APPS_PAGE_MOVE_DELAY = 500;
	private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 5;
	private static int NEW_APPS_ANIMATION_DELAY = 500;
	private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
	private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();
	private LayoutInflater mInflater;
	private Workspace mWorkspace;
	private View mLauncherView;
	private DragLayer mDragLayer;
	private DragController mDragController;
	private View mWeightWatcher;
	private AppWidgetManager mAppWidgetManager;
	private LauncherAppWidgetHost mAppWidgetHost;
	private ItemInfo mPendingAddInfo = new ItemInfo();
	private AppWidgetProviderInfo mPendingAddWidgetInfo;
	private int[] mTmpAddItemCellCoordinates = new int[2];
	private FolderInfo mFolderInfo;
	private Hotseat mHotseat;
	private ViewGroup mOverviewPanel;
	//cheyingkun add start	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
	/**phenix样式的启动页*/
	private LauncherLoading mLauncherLoadingPhenix;
	/**转圈圈样式的启动页*/
	private View mLauncherLoadingProgress;
	//cheyingkun add end
	;
	//cheyingkun add start //查找Sim卡状态的对象TelephonyManager
	private TelephonyManager telephonyManager;
	//cheyingkun add end
	// zhangjin@2016/05/05 ADD START
	@Thunk
	AllAppsContainerView mAppsView;
	// zhangjin@2016/05/05 ADD END
	;
	private SearchDropTargetBar mSearchDropTargetBar;
	private AppsCustomizeTabHost mAppsCustomizeTabHost;
	
	public AppsCustomizeTabHost getmAppsCustomizeTabHost()
	{
		return mAppsCustomizeTabHost;
	}
	
	private AppsCustomizePagedView mAppsCustomizeContent;
	private boolean mAutoAdvanceRunning = false;
	private View mSearchBar;
	private Bundle mSavedState;
	// We set the state in both onCreate and then onNewIntent in some cases, which causes both
	// scroll issues (because the workspace may not have been measured yet) and extra work.
	// Instead, just save the state that we need to restore Launcher to, and commit it in onResume.
	private State mOnResumeState = State.NONE;
	private SpannableStringBuilder mDefaultKeySsb = null;
	private boolean mWorkspaceLoading = true;
	private boolean mPaused = true;
	private boolean mRestoring;
	private boolean mWaitingForResult;
	private boolean mOnResumeNeedsLoad;
	private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();
	private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<Runnable>();
	// Keep track of whether the user has left launcher
	private static boolean sPausedFromUserAction = false;
	private Bundle mSavedInstanceState;
	private LauncherModel mModel;
	private IconCache mIconCache;
	private boolean mUserPresent = true;
	private boolean mVisible = false;
	private boolean mHasFocus = false;
	private boolean mAttached = false;
	private static LocaleConfiguration sLocaleConfiguration = null;
	private static HashMap<Long , FolderInfo> sFolders = new HashMap<Long , FolderInfo>();
	private View.OnTouchListener mHapticFeedbackTouchListener;
	// Related to the auto-advancing of widgets
	private final int ADVANCE_MSG = 1;
	private final int mAdvanceInterval = 20000;
	private final int mAdvanceStagger = 250;
	private long mAutoAdvanceSentTime;
	private long mAutoAdvanceTimeLeft = -1;
	private HashMap<View , AppWidgetProviderInfo> mWidgetsToAdvance = new HashMap<View , AppWidgetProviderInfo>();
	// Determines how long to wait after a rotation before restoring the screen orientation to
	// match the sensor state.
	private final int mRestoreScreenOrientationDelay = 500;
	// External icons saved in case of resource changes, orientation, etc.
	private static Drawable.ConstantState[] sGlobalSearchIcon = new Drawable.ConstantState[2];
	private static Drawable.ConstantState[] sVoiceSearchIcon = new Drawable.ConstantState[2];
	private static Drawable.ConstantState[] sAppMarketIcon = new Drawable.ConstantState[2];
	private Intent mAppMarketIntent = null;
	private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();
	static final ArrayList<String> sDumpLogs = new ArrayList<String>();
	static Date sDateStamp = new Date();
	static DateFormat sDateFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT , DateFormat.SHORT );
	static long sRunStart = System.currentTimeMillis();
	static final String CORRUPTION_EMAIL_SENT_KEY = "corruptionEmailSent";
	// We only want to get the SharedPreferences once since it does an FS stat each time we get
	// it from the context.
	private SharedPreferences mSharedPrefs;
	private static ArrayList<ComponentName> mIntentsOnWorkspaceFromUpgradePath = null;
	// Holds the page that we need to animate to, and the icon views that we need to animate up
	// when we scroll to that page on resume.
	private ImageView mFolderIconImageView;
	private Bitmap mFolderIconBitmap;
	private Canvas mFolderIconCanvas;
	private Rect mRectForFolderAnimation = new Rect();
	private BubbleTextView mWaitingForResume;
	private Runnable mBuildLayersRunnable = new Runnable() {
		
		public void run()
		{
			if( mWorkspace != null )
			{
				mWorkspace.buildPageHardwareLayers();
			}
		}
	};
	private static ArrayList<PendingAddArguments> sPendingAddList = new ArrayList<PendingAddArguments>();
	//<phenix modify> liuhailin@2015-01-26 modify begin
	public ThemeManager mThemeManager;
	//<phenix modify> liuhailin@2015-01-26 modify end
	;
	private EditModeEntity mEditModeEntity;
	
	private static class PendingAddArguments
	{
		
		int requestCode;
		Intent intent;
		long container;
		long screenId;
		int cellX;
		int cellY;
	}
	
	private OperateDynamicMain mOperateDynamicMain = null;
	private Stats mStats;
	/*************************特效添加*****************************/
	private int select_efffects_workspace = 0;
	private int select_effects_applist = 0; //WangLei add //桌面和主菜单特效的分离
	/*************************特效添加*****************************/
	;
	//cheyingkun add start	//我们自己的统计Statistics
	private SharedPreferences prefs;
	//cheyingkun add end
	public static final int TCARDMOUNT_UPDATE_APP_BITMAP = 16;//cheyingkun add 	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)需要更新T卡图标消息的标记
	private ProgressView mAllAppLoadHint;//cheyingkun add	//加载主菜单提示信息【c_0003106】
	/** 桌面是否加载完成 */
	private boolean isLoadFinish = false;//cheyingkun add	//桌面加载速度优化(代码优化)
	private ProgressView mWidgetListLoadHint;//cheyingkun add	//启动速度优化(小部件更新放在加载完成之后)
	;
	//xiatian add start	//适配5.1全局搜索（5.1的全局搜索是将AppWidgetHostView加到mSearchDropTargetBar中），5.1以下的全局搜索机制通不过5.1系统的cts。
	private static final String SEARCH_BAR_WIDGET_ID = "search_bar_widget_id";
	private static final String SEARCH_BAR_WIDGET_PROVIDER = "search_bar_widget_provider";
	//xiatian add end
	;
	private CheckIntegrityManager mCheckIntegrityManager;//xiatian add	//CheckIntegrity（代码框架）
	public static boolean search_need_restart = false;//cheyingkun add	//修改运营酷搜逻辑(改为锁屏时桌面重启)
	private boolean favoritesViewRemove = false;//cheyingkun add	//服务器关闭酷生活后，释放资源。
	private static Launcher mLauncher;
	public static long sTime_applicationCreateStart;
	/**编辑模式底部按钮的父类*/
	private ViewGroup overviewPanelButtons;//cheyingkun add	//编辑模式底部按键支持按键(逻辑优化)
	private boolean needRestart = false;
	private final String SIGNER_NORMAL_MD5 = "76f2686613e97f72a8ea2cd457189896";//cheyingkun add	//移植UNI3签名校验功能
	private final int EVENT_ADD_APP_USE_FREQUENCY = 17;//zhujieping add,当app点击时，记录app的点击次数
	//xiatian add start	//修改menu键的消息处理逻辑：由“onPrepareOptionsMenu中处理menu键逻辑（onPrepareOptionsMenu每次都返回false，确保menu键每次onKeyDown后都调用onPrepareOptionsMenu方法）”改为“onKeyDown和onKeyUp中处理menu键逻辑”。
	//【备注】
	//	1、长按menu键到松手的过程中的消息分发机制为：收到onKeyDown->再次onKeyDown。此时mKeyEvent.isLongPress()为true->收到onKeyUp事件
	//	2、由于上述原因，添加下面的标志位，不处理接下来这次的up事件。
	private boolean mIgnoreNextMenuKeyUpEventForMenuKeyLongPress = false;//当menu键响应长按事件后，要忽略后面的up事件（长按menu键到松手的过程中的消息分发机制为：收到onKeyDown->再次onKeyDown。此时mKeyEvent.isLongPress()为true->收到up事件）
	//xiatian add end
	;
	Intent mBrowserIntentWithOperateHomeWebset = null;//xiatian add	//“运营浏览器主页”的功能：解决内存泄漏。
	private WorkspaceMenuVerticalList mWorkspaceMenuVerticalList = null;//xiatian add	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
	private final String ONRESUME_UPDATE_WIGET_VIEW = "com.cooee.phenix.onResume.UpdateWidgetView";//gaominghui add //配合解决“2D插件清除数据后不刷新界面的问题”【i_0014950】
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	private AllAppsTransitionController mAllAppsController;
	private LauncherStateTransitionAnimation mStateTransitionAnimation;
	//zhujieping add end
	;
	//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
	private OnSharedPreferenceChangeListener mDefaultSharedPreferencesListener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences ,
				String key )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "onSharedPreferenceChanged key=" + key );
			if(
			//
			LauncherDefaultConfig.SWITCH_ENABLE_SHOW_WORKSPACE_SCROLL_TYPE_IN_LAUNCHER_SETTINGS
			//
			&& key == LauncherDefaultConfig.CONFIG_WORKSPACE_SCROLL_TYPE_KEY
			//
			)
			{
				mWorkspace.setLoop( LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_LOOP_SLIDE );
			}
			//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
			if(
			//
			LauncherDefaultConfig.SWITCH_ENABLE_SHOW_APPLIST_SCROLL_TYPE_IN_LAUNCHER_SETTINGS
			//
			&& key == LauncherDefaultConfig.CONFIG_APPLIST_SCROLL_TYPE_KEY
			//
			)
			{
				mAppsCustomizeContent.setApplistLoop( LauncherDefaultConfig.SWITCH_ENABLE_APPLIST_LOOP_SLIDE );
			}
			//xiatian add end
			//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
			if(
			//
			LauncherDefaultConfig.SWITCH_ENABLE_SHOW_WIDGET_SCROLL_TYPE_IN_LAUNCHER_SETTINGS
			//
			&& key == LauncherDefaultConfig.CONFIG_WIDGET_SCROLL_TYPE_KEY
			//
			)
			{
				mAppsCustomizeContent.setWidgetLoop( LauncherDefaultConfig.SWITCH_ENABLE_WIDGET_LOOP_SLIDE );
			}
			//xiatian add end
		}
	};
	//xiatian add end
	;
	
	public int getSelect_efffects_workspace()
	{
		return select_efffects_workspace;
	}
	
	public void setSelect_efffects_workspace(
			int select_efffects )
	{
		this.select_efffects_workspace = select_efffects;
	}
	
	public AppsCustomizePagedView getmAppsCustomizeContent()
	{
		return mAppsCustomizeContent;
	}
	
	//	public int getSelect_efffects_appcustom()
	//	{
	//		return select_efffects_appcustom;
	//	}
	//	
	//	public void setSelect_efffects_appcustom(
	//			int select_efffects )
	//	{
	//		this.select_efffects_appcustom = select_efffects;
	//	}
	private static boolean isPropertyEnabled(
			String propertyName )
	{
		return Log.isLoggable( propertyName , Log.VERBOSE );
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		sTime_applicationCreateStart = System.currentTimeMillis();
		if( DEBUG_STRICT_MODE )
		{
			StrictMode.setThreadPolicy( new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or .detectAll() for all detectable problems
					.penaltyLog().build() );
			StrictMode.setVmPolicy( new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build() );
		}
		super.onCreate( savedInstanceState );
		if( mLauncher != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "launcher oncreate restart" );//通过其他桌面启动桌面，按home键启动，又会响应一次oncreate，这里return掉，重新启动
			needRestart = true;
			return;
		}
		mLauncher = this;
		// zhangjin@2015/08/27 ADD START
		initIconHouse();//cheyingkun add	//解决“安装phenix几率性重启”的问题。（动态图标空指针，动态图标初始化太晚，提前到广播注册前面）
		// zhangjin@2015/08/27 ADD END
		new DefaultLauncherGuideManager( getApplicationContext() , this , this );//xiatian add	//设置默认桌面引导
		LauncherAppState.setActivityInstance( this );
		//		OperateHelp.getInstance( this ).setCategoryListener( this );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】	//cheyingkun del	//优化加载速度(智能分类初始化放到加载完成之后)
		LauncherAppState.setApplicationContext( getApplicationContext() );
		LauncherAppState app = LauncherAppState.getInstance();
		//LauncherMode里获取Launcher的弱引用
		mModel = app.setLauncher( this );
		// chenchen add start 初始化TelephonyManager对象
		telephonyManager = (TelephonyManager)this.getSystemService( Context.TELEPHONY_SERVICE );// 取得相关系统服务
		mModel.setSystemService( telephonyManager );
		// chenchen add end
		// Determine the dynamic grid properties
		//<phenix modify> liuhailin@2015-01-26 modify begin,zhujieping 主题的初始化提前，DeviceProfile中需要读取主题中的数据
		initLauncherDefaultConfingData();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "----Launcher--  onCreate() initLauncherDefaultConfingData end" );
		// gaominghui@2017/01/09 ADD START
		initWallpaperManagerBase();
		// gaominghui@2017/01/09 ADD END
		initThemeData();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "----Launcher--  onCreate() initThemeData end" );
		//<phenix modify> liuhailin@2015-01-26 modify end
		Point smallestSize = new Point();
		Point largestSize = new Point();
		Point realSize = new Point();
		Display display = getWindowManager().getDefaultDisplay();
		// gaominghui@2016/12/14 ADD START
		if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
			display.getCurrentSizeRange( smallestSize , largestSize );
		else
			DecorateUtils.getCurrentSizeRange( this , smallestSize , largestSize );
		// gaominghui@2016/12/14 ADD END
		display.getRealSize( realSize );
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics( dm );
		// Lazy-initialize the dynamic grid
		DeviceProfile grid = app.initDynamicGrid(
				this ,
				Math.min( smallestSize.x , smallestSize.y ) ,
				Math.min( largestSize.x , largestSize.y ) ,
				realSize.x ,
				realSize.y ,
				dm.widthPixels ,
				dm.heightPixels );
		// the LauncherApplication should call this, but in case of Instrumentation it might not be present yet
		mSharedPrefs = getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , Context.MODE_PRIVATE );
		PreferenceManager.getDefaultSharedPreferences( getApplicationContext() ).registerOnSharedPreferenceChangeListener( mDefaultSharedPreferencesListener );//xiatian add	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		//xiatian add start	//fix bug：解决“打开USB存储模式后桌面上安装在SD卡中的应用图标正常变灰，再强制重启桌面后，变灰的SD卡应用图标全部变成机器人”的问题。【i_0009678】
		//【原因】TCardMountManager初始化过晚导致在loadWorkspace中的TCardMountManager相关代码无效
		//【解决方案】提前初始化TCardMountManager
		TCardMountManager.getInstance( this ).setLauncherHandler( this.getHandler() );//cheyikngkun add	//挂载T卡,删除灰色图标,桌面异常终止.bug:i_0009743(因为初始化T卡挂载管理类时,没有先setLauncher,LauncherMolder中的launcher为空,导致后续操作未执行)
		//xiatian add end
		//get IconCache,init at LauncherApplication
		mIconCache = app.getIconCache();
		mIconCache.flushInvalidIcons( grid );
		mDragController = new DragController( this );
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		if(
		//
		LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
		//	
				&& ( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT )//zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		//
		)
		{
			mAllAppsController = new AllAppsTransitionController( this );
			mStateTransitionAnimation = new LauncherStateTransitionAnimation( this , mAllAppsController );
		}
		//zhujieping add end
		mInflater = getLayoutInflater();
		mStats = new Stats( this );
		mAppWidgetManager = AppWidgetManager.getInstance( this );
		//监听widget改变，之后在Model里面回调处理的结果
		mAppWidgetHost = new LauncherAppWidgetHost( this , APPWIDGET_HOST_ID );
		mAppWidgetHost.startListening();
		// If we are getting an onCreate, we can actually preempt onResume and unset mPaused here,
		// this also ensures that any synchronous binding below doesn't re-trigger another
		// LauncherModel load.
		mPaused = false;
		//调试信息的文件存储设置
		if( PROFILE_STARTUP )
		{
			android.os.Debug.startMethodTracing( StringUtils.concat( Environment.getExternalStorageDirectory() , "/launcher" ) );
		}
		//		startStatisticOnLauncherCreate();//cheyingkun add	//我们自己的统计Statistics	//cheyingkun del	//优化加载速度(统计放到桌面加载完再初始化)
		checkForLocaleChange();
		// zhujieping@2015/04/15 UPD START,加上这个属性，在状态栏设置隐藏时，布局不随着变化
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.e( TAG , StringUtils.concat( "app.isVirtualMenuShown( this ) = " , app.isVirtualMenuShown() ) );
		if( !app.isVirtualMenuShown() )
		{
			// zhangjin@2016/05/10 UPD START
			//getWindow().addFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
			if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_KITKAT )
			{
				getWindow().addFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
			}
			// zhangjin@2016/05/10 UPD END
		}
		// zhujieping@2015/04/15 UPD END
		setContentView( R.layout.launcher );
		setupViews();
		// zhujieping@2015/04/15 DEL START，挪到下方执行
		//grid.layout( this );
		// zhujieping@2015/04/15 DEL END
		registerContentObservers();
		lockAllApps();
		mSavedState = savedInstanceState;
		restoreState( mSavedState );
		if( PROFILE_STARTUP )
		{
			android.os.Debug.stopMethodTracing();
		}
		if( !mRestoring )
		{
			if( sPausedFromUserAction )
			{
				// If the user leaves launcher, then we should just load items asynchronously when
				// they return.
				mModel.startLoader( true , -1 );
			}
			else
			{
				// We only load the page synchronously if the user rotates (or triggers a
				// configuration change) while launcher is in the foreground
				mModel.startLoader( true , mWorkspace.getCurrentPage() );
			}
		}
		// For handling default keys
		mDefaultKeySsb = new SpannableStringBuilder();
		Selection.setSelection( mDefaultKeySsb , 0 );
		IntentFilter filter = new IntentFilter( Intent.ACTION_CLOSE_SYSTEM_DIALOGS );
		registerReceiver( mCloseSystemDialogsReceiver , filter );
		updateGlobalIcons();
		updateAppMarketIcon( SHOW_MARKET_BUTTON );//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		//xiatian del start	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
		//不再动态改变参数“searchBarHeightPx”的值
		//		if( getSearchBar().getBackground() != null )
		//		{
		//			grid.setSearchBarSpcaseHeight( true , getStatusBarHeight( false ) );
		//		}
		//		else
		//		{
		//			grid.setSearchBarSpcaseHeight( false , getStatusBarHeight( false ) );
		//		}
		//xiatian del end
		grid.layout( this );
		//xiatian add start	//通知桌面切页：“晨想”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerByBroadcast.java”中的备注。
		NotifyLauncherSnapPageManagerByBroadcast mNotifyLauncherSnapPageManagerByBroadcast = NotifyLauncherSnapPageManagerByBroadcast.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcast != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcast.setCallBack( this );
		}
		//xiatian add end
		//xiatian add start	//通知桌面切页：“德盛伟业”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerDSWY.java”中的备注。
		NotifyLauncherSnapPageManagerCustomerDSWY mNotifyLauncherSnapPageManagerCustomerDSWY = NotifyLauncherSnapPageManagerCustomerDSWY.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerDSWY != null )
		{
			mNotifyLauncherSnapPageManagerCustomerDSWY.setCallBack( this );
		}
		//xiatian add end
		//xiatian add start	//通知桌面切页：“锐益”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerRY.java”中的备注。
		NotifyLauncherSnapPageManagerCustomerRY mNotifyLauncherSnapPageManagerCustomerRY = NotifyLauncherSnapPageManagerCustomerRY.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerRY != null )
		{
			mNotifyLauncherSnapPageManagerCustomerRY.register( this );
			mNotifyLauncherSnapPageManagerCustomerRY.setCallBack( this );
		}
		//xiatian add end
		//xiatian add start	//通知桌面切页：“讯虎”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerCustomerXH.java”中的备注。
		NotifyLauncherSnapPageManagerCustomerXH mNotifyLauncherSnapPageManagerCustomerXH = NotifyLauncherSnapPageManagerCustomerXH.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerXH != null )
		{
			mNotifyLauncherSnapPageManagerCustomerXH.setCallBack( this );
		}
		//xiatian add end
		//gaominghui add start  //通知桌面切页：“讯虎”定制功能（单向光感切页）。详见“NotifyLauncherSnapPageManagerCustomerSingleTrackXH.java"中的备注
		NotifyLauncherSnapPageManagerCustomerSingleTrackXH mNotifyLauncherSnapPageManagerCustomerSingleTrackXH = NotifyLauncherSnapPageManagerCustomerSingleTrackXH.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerSingleTrackXH != null )
		{
			mNotifyLauncherSnapPageManagerCustomerSingleTrackXH.register( this );
			mNotifyLauncherSnapPageManagerCustomerSingleTrackXH.setCallBack( this );
		}
		//gaominghui add end
		//gaominghui add start //通知桌面切页：“讯虎”定制功能（双向光感切页）。详见“NotifyLauncherSnapPageManagerByBroadcastXH.java"中的备注
		NotifyLauncherSnapPageManagerByBroadcastXH mNotifyLauncherSnapPageManagerByBroadcastXH = NotifyLauncherSnapPageManagerByBroadcastXH.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcastXH != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcastXH.setCallBack( this );
		}
		//gaominghui add end
		// cheyingkun add start //免责声明布局(launcher启动时的免责声明)
		if( Disclaimer.isNeedShowDisclaimer() )
		{
			hideCurPageViews();
			DisclaimerManager.getInstance( this ).showDisclaimer( DisclaimerManager.LAUNCHRE_ONCREATE_DISCLAIMER , this );// cheyingkun add //免责声明布局
		}
		else
		{
			//			showFirstRunCling();//cheyingkun del	//修改新手引导显示逻辑，启动页显示完再显示新手引导。
			showLoadingPage();//cheyingkun add	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
		}
		// cheyingkun add end 
		UpdateManagerImpl.setLoadFinish( false );//cheyingkun add	//允许更新方法添加免责声明和桌面加载完成的判断
		if( LauncherDefaultConfig.SWITCH_ONE_KEY_CHANGE_WALLPAPER )
		{
			HashMap<String , Object> launcherConfigMap = new HashMap<String , Object>();
			launcherConfigMap.put( WallpaperConfigString.ENABLE_UMENG , LauncherDefaultConfig.SWITCH_ENABLE_UMENG );
			// gaominghui@2017/01/09 UPD START
			//launcherConfigMap.put( WallpaperConfigString.LAUNCHER_SET_WALLPAPER_DIMENSIONS , WallpaperManagerBase.disable_set_wallpaper_dimensions );
			launcherConfigMap.put( WallpaperConfigString.LAUNCHER_SET_WALLPAPER_DIMENSIONS , WallpaperManagerBase.get_disableSetWallpaperDimensions() );
			// gaominghui@2017/01/09 UPD END
			launcherConfigMap.put( WallpaperConfigString.CUSTOM_WALLPAPERS_PATH , LauncherDefaultConfig.CONFIG_CUSTOM_WALLPAPERS_PATH );
			WallpaperHostManager.getInstance( this ).init( launcherConfigMap );
			WallpaperHostManager.getInstance( this ).setWallpaperCallbacks( this );
		}
		// zhangjin@2016/04/26 ADD START
		UpdateUiManager.setGlobalContext( Launcher.this );
		// zhangjin@2016/04/26 ADD END
		//cheyingkun add start	//移植UNI3签名校验功能
		//签名校验
		if( isNeedCheckSignerMD5( this ) )
		{
			if( checkSignerMD5() == false )
			{
				showErrorDialog();
			}
		}
		//cheyingkun add end
		initFavoritesConfig();//cheyingkun add	//解决“打开酷生活引导页，切页到酷生活后，点击引导页按钮，搜索栏显示异常”的问题
		//gaominghui add start //支持通过AIDL切换主题，解决“手机第一次开机，应用主题慢”的问题【c_0004675】
		startThemeService();
		//gaominghui add end //支持通过AIDL切换主题，解决“手机第一次开机，应用主题慢”的问题【c_0004675】
	}
	
	//<phenix modify> liuhailin@2015-01-26 modify begin
	private void initLauncherDefaultConfingData()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "----Launcher--  onCreate() initLauncherDefaultConfingData start" );
		if( LauncherDefaultConfig.getInt( R.integer.config_item_style ) != BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		{
			PubContentProvider.init();
			PubProviderHelper.SetContext( LauncherAppState.getInstance().getContext() );
		}
		LauncherIconBaseConfig.initIconBase( LauncherAppState.getInstance().getContext() );
		LauncherIconBaseConfig.MergeIconBaseWithDefaultIcon();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "----Launcher--  onCreate()  initLauncherDefaultConfingData end" );
	}
	
	//cheyingkun add start	//解决“安装phenix几率性重启”的问题。（动态图标空指针，动态图标初始化太晚，提前到广播注册前面）
	//解决“默认替换图标初始化太早导致替换失效”的问题（默认替换图标和动态图标初始化分开调用）
	//默认替换图标和动态图标的初始化分开,如果替换图标初始化被提前,会导致替换无效
	private void initIconHouse()
	{
		// zhangjin@2015/08/27 ADD START
		IconHouseManager.getInstance().setUp( this );
		// zhangjin@2015/08/27 ADD END
	}
	
	//cheyingkun add end
	// gaominghui@2017/01/09 ADD START 初始化相关壁纸开关的状态信息
	private void initWallpaperManagerBase()
	{
		boolean disable_move_wallpaper = !LauncherDefaultConfig.getBoolean( R.bool.switch_enable_move_wallpaper );//cheyingkun add	//是否支持壁纸滑动。true为支持；false为不支持。默认true。
		if( Build.BRAND.contains( "Xiaomi" ) )
		{
			disable_move_wallpaper = true;
		}
		WallpaperManagerBase.set_disableSetWallpaperDimensions( !LauncherDefaultConfig.getBoolean( R.bool.switch_enable_set_wallpaper_dimensions ) );//chenliang add //添加配置项“switch_enable_set_wallpaper_dimensions”，壁纸是否设置尺寸，true为允许，false为不允许。默认为true。【c_0004653】
		WallpaperManagerBase.set_disableMoveWallpaper( disable_move_wallpaper );
		WallpaperManagerBase.set_MTKSetWallpaperSize( LauncherDefaultConfig.SWITCH_ENABLE_MTK_SET_WALLPAPER );
	}
	
	// gaominghui@2017/01/09 ADD END 初始化相关壁纸开关的状态信息
	private void initThemeData()
	{
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "----Launcher--  onCreate() new Thread initThemeData start" );
			mThemeManager = new ThemeManager( LauncherAppState.getInstance().getContext() );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "----Launcher--  onCreate() new Thread initThemeData 000" );
			//			new Thread( new Runnable() {
			//				
			//				public void run()
			//				{
			//					if( ThemeManager.getInstance() != null )
			//					{
			//						ThemeManager.getInstance().ApplyWallpaper();
			//					}
			//					// zhujieping@2015/03/26 DEL START,上个方法中已经有写入数据库
			//					//PubProviderHelper.addOrUpdateValue( "theme" , "theme_status" , String.valueOf( 0 ) );
			//					// zhujieping@2015/03/26 DEL END
			//				}
			//			} ).start();
			mThemeManager.applyWallpaperInThread();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "----Launcher--  onCreate() new Thread initThemeData end" );
		}
	}
	
	//<phenix modify> liuhailin@2015-01-26 modify end
	protected void onUserLeaveHint()
	{
		super.onUserLeaveHint();
		sPausedFromUserAction = true;
	}
	
	protected void invalidateFavoritesPage()
	{
		if( mWorkspace == null || mWorkspace.getScreenOrder().isEmpty() )
		{
			// Not bound yet, wait for bindScreens to be called.
			return;
		}
		boolean mIsHaveFavoritesPage = mWorkspace.hasFavoritesPage();
		if( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES )
		{
			if( mIsHaveFavoritesPage == false )
			{
				// Create the custom content page and call the subclass to populate it.
				mWorkspace.createAndAddFavoritesPage();
			}
		}
		else
		{
			if( mIsHaveFavoritesPage )
			{
				//			mWorkspace.removeFavoritesPage();//cheyingkun del	//服务器关闭酷生活后，释放资源。
			}
		}
	}
	
	private void updateGlobalIcons()
	{
		//cheyingkun add start	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		if( !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
		{
			return;
		}
		//cheyingkun add end
		boolean searchVisible = false;
		boolean voiceVisible = false;
		int coi = getCurrentOrientationIndexForGlobalIcons();
		if( sGlobalSearchIcon[coi] == null )
		{
			searchVisible = updateSearchBarSearchButton();
		}
		else
		{
			updateSearchBarSearchButton( sGlobalSearchIcon[coi] );
			searchVisible = true;
		}
		//cheyingkun add start	//搜索栏是否支持显示语音搜索的按钮。true为支持；false为不支持。默认true。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_SHOW_VOICE_BUTTON == false )
		{
			hideSearchBarVoiceButton();
			voiceVisible = false;
		}
		else
		//cheyingkun add end
		{
			if( sVoiceSearchIcon[coi] == null )
			{
				voiceVisible = updateSearchBarVoiceButton( searchVisible );
			}
			else
			{
				updateSearchBarVoiceButton( sVoiceSearchIcon[coi] );
				voiceVisible = true;
			}
		}
		if( mSearchDropTargetBar != null )
		{
			mSearchDropTargetBar.onSearchPackagesChanged( searchVisible , voiceVisible );
		}
		changeGlobalSearchToKuSoIfNecessary();//xiatian add	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
	}
	
	private void checkForLocaleChange()
	{
		if( sLocaleConfiguration == null )
		{
			new AsyncTask<Void , Void , LocaleConfiguration>() {
				
				@Override
				protected LocaleConfiguration doInBackground(
						Void ... unused )
				{
					LocaleConfiguration localeConfiguration = new LocaleConfiguration();
					readConfiguration( Launcher.this , localeConfiguration );
					return localeConfiguration;
				}
				
				@Override
				protected void onPostExecute(
						LocaleConfiguration result )
				{
					sLocaleConfiguration = result;
					checkForLocaleChange(); // recursive, but now with a locale configuration
				}
			}.execute();
			return;
		}
		final Configuration configuration = getResources().getConfiguration();
		final String previousLocale = sLocaleConfiguration.locale;
		final String locale = configuration.locale.toString();
		final int previousMcc = sLocaleConfiguration.mcc;
		final int mcc = configuration.mcc;
		final int previousMnc = sLocaleConfiguration.mnc;
		final int mnc = configuration.mnc;
		boolean localeChanged = !locale.equals( previousLocale ) || mcc != previousMcc || mnc != previousMnc;
		if( localeChanged )
		{
			sLocaleConfiguration.locale = locale;
			sLocaleConfiguration.mcc = mcc;
			sLocaleConfiguration.mnc = mnc;
			mIconCache.flush();
			final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
			new Thread( "WriteLocaleConfiguration" ) {
				
				@Override
				public void run()
				{
					writeConfiguration( Launcher.this , localeConfiguration );
				}
			}.start();
		}
	}
	
	private static class LocaleConfiguration
	{
		
		public String locale;
		public int mcc = -1;
		public int mnc = -1;
	}
	
	private static void readConfiguration(
			Context context ,
			LocaleConfiguration configuration )
	{
		DataInputStream in = null;
		try
		{
			in = new DataInputStream( context.openFileInput( PREFERENCES ) );
			configuration.locale = in.readUTF();
			configuration.mcc = in.readInt();
			configuration.mnc = in.readInt();
		}
		catch( FileNotFoundException e )
		{
			// Ignore
		}
		catch( IOException e )
		{
			// Ignore
		}
		finally
		{
			if( in != null )
			{
				try
				{
					in.close();
				}
				catch( IOException e )
				{
					// Ignore
				}
			}
		}
	}
	
	private static void writeConfiguration(
			Context context ,
			LocaleConfiguration configuration )
	{
		DataOutputStream out = null;
		try
		{
			out = new DataOutputStream( context.openFileOutput( PREFERENCES , MODE_PRIVATE ) );
			out.writeUTF( configuration.locale );
			out.writeInt( configuration.mcc );
			out.writeInt( configuration.mnc );
			out.flush();
		}
		catch( FileNotFoundException e )
		{
			// Ignore
		}
		catch( IOException e )
		{
			//noinspection ResultOfMethodCallIgnored
			context.getFileStreamPath( PREFERENCES ).delete();
		}
		finally
		{
			if( out != null )
			{
				try
				{
					out.close();
				}
				catch( IOException e )
				{
					// Ignore
				}
			}
		}
	}
	
	public Stats getStats()
	{
		return mStats;
	}
	
	public LayoutInflater getInflater()
	{
		return mInflater;
	}
	
	public DragLayer getDragLayer()
	{
		return mDragLayer;
	}
	
	public boolean isDraggingEnabled()
	{
		// We prevent dragging when we are loading the workspace as it is possible to pick up a view
		// that is subsequently removed from the workspace in startBinding().
		return !mModel.isLoadingWorkspace();
	}
	
	/**
	 * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
	 * a configuration step, this allows the proper animations to run after other transitions.
	 */
	private boolean completeAdd(
			PendingAddArguments args )
	{
		boolean result = false;
		switch( args.requestCode )
		{
			case REQUEST_PICK_APPLICATION:
				completeAddApplication( args.intent , args.container , args.screenId , args.cellX , args.cellY );
				break;
			case REQUEST_PICK_SHORTCUT:
				processShortcut( args.intent );
				break;
			case REQUEST_CREATE_SHORTCUT:
				completeAddShortcut( args.intent , args.container , args.screenId , args.cellX , args.cellY );
				result = true;
				break;
			case REQUEST_CREATE_APPWIDGET:
				int appWidgetId = args.intent.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , -1 );
				completeAddAppWidget( appWidgetId , args.container , args.screenId , null , null );
				result = true;
				break;
		}
		// Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
		// if you turned the screen off and then back while in All Apps, Launcher would not
		// return to the workspace. Clearing mAddInfo.getContainer() here fixes this issue
		resetAddInfo();
		return result;
	}
	
	@Override
	protected void onActivityResult(
			final int requestCode ,
			final int resultCode ,
			final Intent data )
	{
		// Reset the startActivity waiting flag
		mWaitingForResult = false;
		if( requestCode == REQUEST_BIND_APPWIDGET )
		{
			int appWidgetId = data != null ? data.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , -1 ) : -1;
			if( resultCode == RESULT_CANCELED )
			{
				completeTwoStageWidgetDrop( RESULT_CANCELED , appWidgetId );
			}
			else if( resultCode == RESULT_OK )
			{
				addAppWidgetImpl( appWidgetId , mPendingAddInfo , null , mPendingAddWidgetInfo );
			}
			return;
		}
		else if(
		//
		( LauncherDefaultConfig.CONFIG_CUSTOMER_WALLPAPER_COMPONENT_NAME != null /* //xiatian add	//飞利浦需求，将美化中心改为他们的壁纸设置。 */)
		//
		&& requestCode == REQUEST_PICK_WALLPAPER
		//
		)
		{
			if( resultCode == RESULT_OK && mWorkspace.isInOverviewMode() )
			{
				exitEditMode( false );//zhujieping modify,调用launcher中的同一个方法，方便告诉编辑模式二级界面当前状态
			}
			return;
		}
		//WangLei add start //实现默认配置AppWidget的流程
		else if( requestCode == REQUEST_BIND_DEFAULT_APPWIDGET )
		{
			int appWidgetId = data != null ? data.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , -1 ) : -1;
			if( resultCode == RESULT_OK )
			{
				AppWidgetProviderInfo providerInfo = mAppWidgetManager.getAppWidgetInfo( appWidgetId );
				ItemInfo pendingWidgetInfo = new ItemInfo();
				pendingWidgetInfo.setCellX( data.getIntExtra( LauncherSettings.Favorites.CELLX , -1 ) );
				pendingWidgetInfo.setCellY( data.getIntExtra( LauncherSettings.Favorites.CELLY , -1 ) );
				pendingWidgetInfo.setSpanX( data.getIntExtra( LauncherSettings.Favorites.SPANX , -1 ) );
				pendingWidgetInfo.setSpanY( data.getIntExtra( LauncherSettings.Favorites.SPANY , -1 ) );
				pendingWidgetInfo.setScreenId( data.getLongExtra( LauncherSettings.Favorites.SCREEN , 0 ) );
				pendingWidgetInfo.setContainer( LauncherSettings.Favorites.CONTAINER_DESKTOP );
				completeAddWidgetToDb( appWidgetId , pendingWidgetInfo , null , providerInfo );
			}
			else if( requestCode == RESULT_CANCELED )
			{
				completeTwoStageWidgetDrop( RESULT_CANCELED , appWidgetId );
			}
			/**点击确定或取消后，停止同步锁，继续加载流程*/
			LauncherProvider provider = LauncherAppState.getLauncherProvider();
			if( provider != null )
			{
				provider.stopLock();
			}
			return;
		}
		//WangLei add end
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		if(
		//
		( requestCode == REQUEST_PICK_APPWIDGET_FOR_SYSTEM_PICK_ACTIVITY )
		//
		&& resultCode == RESULT_OK
		//
		)
		{
			addAppWidgetFromPickForSystemPickActivity( data );
			return;
		}
		else if( requestCode == REQUEST_CREATE_APPWIDGET_FOR_SYSTEM_PICK_ACTIVITY && resultCode == RESULT_OK )
		{
			addAppWidgetFromPickTwoStageForSystemPickActivity( data );
			return;
		}
		//xiatian add end
		boolean delayExitSpringLoadedMode = false;
		boolean isWidgetDrop = ( requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET );
		// We have special handling for widgets
		if( isWidgetDrop )
		{
			int appWidgetId = data != null ? data.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , -1 ) : -1;
			if( appWidgetId < 0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the widget configuration activity." );
				completeTwoStageWidgetDrop( RESULT_CANCELED , appWidgetId );
				mWorkspace.stripEmptyScreens();//在添加小组件页面返回的appWidgetId为-1时，删除了空白页面
			}
			else
			{
				completeTwoStageWidgetDrop( resultCode , appWidgetId );
			}
			return;
		}
		// The pattern used here is that a user PICKs a specific application,
		// which, depending on the target, might need to CREATE the actual target.
		// For example, the user would PICK_SHORTCUT for "Music playlist", and we
		// launch over to the Music app to actually CREATE_SHORTCUT.
		if( resultCode == RESULT_OK && mPendingAddInfo.getContainer() != ItemInfo.NO_ID )
		{
			final PendingAddArguments args = new PendingAddArguments();
			args.requestCode = requestCode;
			args.intent = data;
			args.container = mPendingAddInfo.getContainer();
			args.screenId = mPendingAddInfo.getScreenId();
			args.cellX = mPendingAddInfo.getCellX();
			args.cellY = mPendingAddInfo.getCellY();
			if( isWorkspaceLocked() )
			{
				sPendingAddList.add( args );
			}
			else
			{
				delayExitSpringLoadedMode = completeAdd( args );
			}
		}
		else if( resultCode == RESULT_CANCELED )
		{
			mWorkspace.stripEmptyScreens();
		}
		mDragLayer.clearAnimatedView();
		// Exit spring loaded mode if necessary after cancelling the configuration of a widget
		exitSpringLoadedDragModeDelayed( ( resultCode != RESULT_CANCELED ) , delayExitSpringLoadedMode , null );
	}
	
	private void completeTwoStageWidgetDrop(
			final int resultCode ,
			final int appWidgetId )
	{
		CellLayout cellLayout = (CellLayout)mWorkspace.getScreenWithId( mPendingAddInfo.getScreenId() );
		Runnable onCompleteRunnable = null;
		int animationType = 0;
		AppWidgetHostView boundWidget = null;
		if( resultCode == RESULT_OK )
		{
			animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
			final AppWidgetHostView layout = mAppWidgetHost.createView( this , appWidgetId , mPendingAddWidgetInfo );
			boundWidget = layout;
			onCompleteRunnable = new Runnable() {
				
				@Override
				public void run()
				{
					completeAddAppWidget( appWidgetId , mPendingAddInfo.getContainer() , mPendingAddInfo.getScreenId() , layout , null );
					exitSpringLoadedDragModeDelayed( ( resultCode != RESULT_CANCELED ) , false , null );
				}
			};
		}
		else if( resultCode == RESULT_CANCELED )
		{
			animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
			//xiatian add start	//fix bug：解决“在编辑模式下，拖动小组件中的‘手机百度’6.3.1版本自带的‘(4x2百度时钟)’插件新建一个页面，并在新页面放下插件，在弹出的‘选择时钟样式’界面点击返回键后，桌面的新页面没有删除”的问题。【i_0010423】
			//【问题原因】在添加小组件页面返回RESULT_CANCELED并且appWidgetId不为-1时，没有删除空白页面
			//【解决方案】删除空白页面
			mWorkspace.stripEmptyScreens();
			//xiatian add end
			onCompleteRunnable = new Runnable() {
				
				@Override
				public void run()
				{
					exitSpringLoadedDragModeDelayed( ( resultCode != RESULT_CANCELED ) , false , null );
				}
			};
		}
		if( mDragLayer.getAnimatedView() != null )
		{
			mWorkspace.animateWidgetDrop( mPendingAddInfo , cellLayout , (DragView)mDragLayer.getAnimatedView() , onCompleteRunnable , animationType , boundWidget , true );
		}
		else
		{
			// The animated view may be null in the case of a rotation during widget configuration
			onCompleteRunnable.run();
		}
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		FirstFrameAnimatorHelper.setIsVisible( false );
		onStopNotify();//xiatian add	//在桌面onStop的时候，发送广播通知客户。配置为空则不发送广播。默认为空。	
		// zhangjin@2016/06/01 ADD START
		JarURLMonitor.getInstance( this ).cleanCache();
		// zhangjin@2016/06/01 ADD END
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		FirstFrameAnimatorHelper.setIsVisible( true );
		// gaominghui@2017/01/09 ADD START
		WallpaperManagerBase.getInstance( this ).setWallpaperDimensionOnStart();
		// gaominghui@2017/01/09 ADD END
		//cheyingkun add start	//是否监听飞利浦壁纸改变广播【c_0003456】
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOMER_PHILIPS_WALLPAPER_CHANGED_NOTIFY )
		{
			// gaominghui@2017/01/09 UPD START
			//WallpaperManagerBase.getInstance( this ).setWallpaperDimensionBySharedPreferencesOnStart();
			WallpaperManagerUtil.setWallpaperDimensionBySharedPreferencesOnStart( this );
			// gaominghui@2017/01/09 UPD END
		}
		//cheyingkun add end
	}
	
	@Override
	protected void onResume()
	{
		//xiatian del start	//fix bug：解决“在phenix桌面没有设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，再进入phenix桌面之后，桌面特效为‘经典’特效”的问题。【i_0010438】
		//【问题原因】
		//		1、由于先走LauncherSettingActivity的onDestroy方法，将当前特效保存到了SharedPreferences之中，所以此处得到的特效值正确。
		//		2、由于“mWorkspace.initAnimationStyle( mWorkspace );”和“mAppsCustomizeContent.initAnimationStyle( mAppsCustomizeContent );”这两行代码中，
		//			都会进入PagedView.java的如下方法中的标注处，导致桌面特效为‘经典’特效。
		//		protected void initAnimationStyle(
		//				PagedView pagedView )
		//		{
		//			int num = 0;
		//			EffectFactory effectFactory = new EffectFactory( curView );
		//			if( pagedView instanceof AppsCustomizePagedView )
		//			{
		//				if( mLauncher.getSelect_efffects_workspace() == effectFactory.getAllEffects().size() )
		//				{
		//					mCurentAnimInfo = effectFactory.getEffect( num = new Random().nextInt( effectFactory.getAllEffects().size() ) + 1 );
		//				}
		//				else
		//				{
		//					mCurentAnimInfo = effectFactory.getEffect( mLauncher.getSelect_efffects_workspace() + 1 );
		//				}
		//			}
		//			else
		//			{
		//				if( mLauncher.getmWorkspace() != null && mLauncher.getmWorkspace().isInOverviewMode() )
		//				{
		//【错误原因：进入该if分支】					mCurentAnimInfo = effectFactory.getEffect( 1 );
		//				}
		//				else
		//				{
		//					if( mLauncher.getSelect_efffects_workspace() == effectFactory.getAllEffects().size() )
		//					{
		//						mCurentAnimInfo = effectFactory.getEffect( num = new Random().nextInt( effectFactory.getAllEffects().size() ) + 1 );
		//					}
		//					else
		//					{
		//						mCurentAnimInfo = effectFactory.getEffect( mLauncher.getSelect_efffects_workspace() + 1 );
		//					}
		//				}
		//			}
		//		}
		//		3、错误原因是由于备注2的两行代码调用过早，workspace的标志位mState还没有设置为“NORMAL”,从而造成条件“if( mLauncher.getmWorkspace() != null && mLauncher.getmWorkspace().isInOverviewMode() )”成立，产生如上bug。
		//		4、workspace的标志位mState会在，下面的“if( mOnResumeState == State.WORKSPACE )”分支中的showWorkspace方法中设置为“NORMAL”
		//【解决方案】将特效的初始化操作，后移到桌面状态设置正常之后。
		//		//<phenix modify> liuhailin@2015-02-28 modify begin
		//		String effectsValue = PreferenceManager.getDefaultSharedPreferences( this ).getString( getResources().getString( R.string.setting_key_launcher_effects ) , "0" );
		//		//setSelect_efffects_workspace( mSharedPrefs.getInt( SELECT_EFFECT_PREF_WORKSPACE , 0 ) );
		//		setSelect_efffects_workspace( Integer.valueOf( effectsValue ) );
		//		//<phenix modify> liuhailin@2015-02-28 modify end
		//		mWorkspace.initAnimationStyle( mWorkspace );
		//		mAppsCustomizeContent.initAnimationStyle( mAppsCustomizeContent );
		//xiatian del end
		if( needRestart )
		{
			super.onResume();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "PROCEDURE Launcher onResume() killProcess!!" );
			android.os.Process.killProcess( android.os.Process.myPid() );
			return;
		}
		long startTime = 0;
		if( DEBUG_RESUME_TIME )
		{
			startTime = System.currentTimeMillis();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "Launcher.onResume()" );
		}
		super.onResume();
		//xiatian add start	//设置默认桌面引导
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE && !DefaultLauncherGuideManager.getInstance().isOnlyLauncher( mLauncher ) )
		{
			SharedPreferences mSharedPreferences = getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
			if( mSharedPreferences.getBoolean( "first_run" , true ) == false )
			{
				DefaultLauncherGuideManager.getInstance().checkDefaultLauncherAndShowGuideDialog( false , this );
			}
		}
		//xiatian add end
		//cheyingkun add start	//是否开启“状态栏透明”和“导航栏透明”效果，安卓4.4以上有效。
		if( LauncherDefaultConfig.SWITCH_ENABLE_STATUS_BAR_AND_NAVIGATION_BAR_TRANSPARENT )
		{
			LauncherAppState.getInstance().setStatusBarAndNavigationBarTransparent();
		}
		//cheyingkun add end
		// Restore the previous launcher state
		if( mOnResumeState == State.WORKSPACE )
		{
			showWorkspace( false );
		}
		else if( mOnResumeState == State.APPS_CUSTOMIZE )
		{
			// zhangjin@2016/05/06 UPD START
			//showAllApps( false , AppsCustomizePagedView.ContentType.Applications , false );
			// zhangjin@2016/05/06 UPD START
			//showAllApps( false , AppsCustomizePagedView.ContentType.Applications , false );
			if(
			//
			LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW
			//
			|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
			//
			)
			{
				showAppsView( false /* animated */, false /* resetListToTop */, true /* updatePredictedApps */, false /* focusSearchBar */, AppsCustomizePagedView.ContentType.Applications );
			}
			else
			{
				showAllApps( false , AppsCustomizePagedView.ContentType.Applications , false );
			}
			// zhangjin@2016/05/06 UPD END
		}
		reloadAnimationStyleNotRestore();//xiatian add	//fix bug：解决“在phenix桌面没有设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，再进入phenix桌面之后，桌面特效为‘经典’特效”的问题。【i_0010438】
		mOnResumeState = State.NONE;
		// Background was set to gradient in onPause(), restore to black if in all apps.
		mPaused = false;
		//cheyingkun add start	//解决“连续点击发送双开应用广播的按钮，回到桌面后图标和开关状态不匹配”的问题【c_0004466】
		ZhiKeShortcutManager.getInstance( mLauncher ).setLauncherPaused( mPaused );
		ZhiKeShortcutManager.getInstance( mLauncher ).clearShortcutIntentList();
		//cheyingkun add end
		sPausedFromUserAction = false;
		if( mRestoring || mOnResumeNeedsLoad )
		{
			mWorkspaceLoading = true;
			mModel.startLoader( true , -1 );
			mRestoring = false;
			mOnResumeNeedsLoad = false;
		}
		if( mBindOnResumeCallbacks.size() > 0 )
		{
			// We might have postponed some bind calls until onResume (see waitUntilResume) --
			// execute them here
			long startTimeCallbacks = 0;
			if( DEBUG_RESUME_TIME )
			{
				startTimeCallbacks = System.currentTimeMillis();
			}
			if( mAppsCustomizeContent != null )
			{
				mAppsCustomizeContent.setBulkBind( true );
			}
			for( int i = 0 ; i < mBindOnResumeCallbacks.size() ; i++ )
			{
				mBindOnResumeCallbacks.get( i ).run();
			}
			if( mAppsCustomizeContent != null )
			{
				mAppsCustomizeContent.setBulkBind( false );
			}
			mBindOnResumeCallbacks.clear();
			if( DEBUG_RESUME_TIME )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "Time spent processing callbacks in onResume: " , ( System.currentTimeMillis() - startTimeCallbacks ) ) );
			}
		}
		if( mOnResumeCallbacks.size() > 0 )
		{
			for( int i = 0 ; i < mOnResumeCallbacks.size() ; i++ )
			{
				mOnResumeCallbacks.get( i ).run();
			}
			mOnResumeCallbacks.clear();
		}
		// Reset the pressed state of icons that were locked in the press state while activities
		// were launching
		if( mWaitingForResume != null )
		{
			// Resets the previous workspace icon press state
			mWaitingForResume.setStayPressed( false );
		}
		if( mAppsCustomizeContent != null )
		{
			// Resets the previous all apps icon press state
			mAppsCustomizeContent.resetDrawableState();
		}
		// It is possible that widgets can receive updates while launcher is not in the foreground.
		// Consequently, the widgets will be inflated in the orientation of the foreground activity
		// (framework issue). On resuming, we ensure that any widgets are inflated for the current
		// orientation.
		getWorkspace().reinflateWidgetsIfNecessary();
		// Process any items that were added while Launcher was away.
		InstallShortcutReceiver.disableAndFlushInstallQueue( this );
		// Update the voice search button proxy
		// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
		//		//WangLei start //bug:0010441 ////当语音搜索图标不可用时，点击整个搜索框都相应onClickSearchButton
		//		//updateVoiceButtonProxyVisible( false ); //WangLei del
		//		updateVoiceButtonProxyVisible( true ); //WangLei add
		//		//WangLei end
		// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
		// Again, as with the above scenario, it's possible that one or more of the global icons
		// were updated in the wrong orientation.
		updateGlobalIcons();
		updateAppMarketIcon( SHOW_MARKET_BUTTON );//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		if( DEBUG_RESUME_TIME )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Time spent in onResume: " , ( System.currentTimeMillis() - startTime ) ) );
		}
		if( mWorkspace.getFavoritesPageCallbacks() != null )
		{
			// If we are resuming and the custom content is the current page, we call onShow().
			// It is also poassible that onShow will instead be called slightly after first layout
			// if PagedView#setRestorePage was set to the custom content page in onCreate().
			if( mWorkspace.isOnOrMovingToFavoritesPage() )
			{
				mWorkspace.getFavoritesPageCallbacks().onShow();
			}
		}
		mWorkspace.updateInteractionForState();
		mWorkspace.onResume();
		//cheyingkun add start	//优化加载速度(统计和sdk放到桌面加载完再初始化)
		statisOnResume();
		umengStatisOnResume();
		//cheyingkun add end
		onResumeNotify();//xiatian add	//在桌面onResume的时候，发送广播通知客户。配置为空则不发送广播。默认为空。	
		if( isLoadFinish )
		{
			CategoryParse.categoryOnResume();
		}
		if( mWorkspace.hasFavoritesPage() )
		{
			FavoritesPageManager.getInstance( this ).onResume();//cheyingkun add	//phenix1.1稳定版移植酷生活
		}
		// YANGTIANYU@2016/07/19 ADD START
		// 专属页onResume
		if( mWorkspace.hasCameraPage() )
		{
			CameraView.getInstance().onResume();
		}
		if( mWorkspace.hasMusicPage() )
		{
			MusicView.getInstance().onResume();
		}
		// YANGTIANYU@2016/07/19 ADD END
		// @gaominghui 2016/02/23 ADD START Kpsh推送所需要添加的代码
		if( !isHasFocusByFocusChanged() )//huwenhao add	//同步修改点：“【uni3.0公版】【Kpsh】修改连续按HOME键桌面会卡顿的问题【鞠冰诚】”
		{
			resumeKpshSdk();
		}
		// @gaominghui 2016/02/23 ADD END Kpsh推送所需要添加的代码
		//gaominghui add start //配合解决“2D插件清除数据后不刷新界面的问题”【i_0014950】	
		Intent widgetUpdateIntent = new Intent( ONRESUME_UPDATE_WIGET_VIEW );
		sendBroadcast( widgetUpdateIntent );
		//gaominghui add end
		//xiatian add start	//通知桌面切页：“德盛伟业”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerDSWY.java”中的备注。
		NotifyLauncherSnapPageManagerCustomerDSWY mNotifyLauncherSnapPageManagerCustomerDSWY = NotifyLauncherSnapPageManagerCustomerDSWY.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerDSWY != null )
		{
			mNotifyLauncherSnapPageManagerCustomerDSWY.register( this );
		}
		//xiatian add end
		//gaominghui add start //通知桌面切页：“讯虎”定制功能（双向光感切页）。详见“NotifyLauncherSnapPageManagerByBroadcastXH.java"中的备注
		NotifyLauncherSnapPageManagerByBroadcastXH mNotifyLauncherSnapPageManagerByBroadcastXH = NotifyLauncherSnapPageManagerByBroadcastXH.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcastXH != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcastXH.enableGesture( this );
		}
		//gaominghui add end
	}
	
	// @gaominghui 2016/02/23 ADD START Kpsh推送所需要添加的方法
	public boolean isHasFocusByFocusChanged()
	{
		return mHasFocus;
	}
	
	// @gaominghui2016/02/25 ADD START Kpsh需要添加的相关代码
	// 需要的参数
	// 1 Context 桌面应用程序的Context
	// 2  String 桌面的包名
	// 3 int 桌面是否内置(1 表示内置，0表示没有内置)
	// 4 String  桌面的包名或者桌面的标题
	private void resumeKpshSdk()
	{
		int buildIn = 0;
		if( isLoadFinish )
		{
			int mFlag = 0;
			try
			{
				mFlag = getPackageManager().getPackageInfo( this.getPackageName() , 0 ).applicationInfo.flags;
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( ( mFlag & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0 || ( mFlag & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
			{
				buildIn = 1;
			}
			KpshSdk.setTopApp( this , getPackageName() , buildIn , getPackageName() );
		}
	}
	
	// @gaominghui2016/02/25 ADD END
	@Override
	protected void onPause()
	{
		// Ensure that items added to Launcher are queued until Launcher returns
		InstallShortcutReceiver.enableInstallQueue();
		super.onPause();
		//xiatian add start	//设置默认桌面引导
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE )
		{
			DefaultLauncherGuideManager.getInstance().removeMessageShowGuideDialog();
		}
		//xiatian add end
		mPaused = true;
		ZhiKeShortcutManager.getInstance( mLauncher ).setLauncherPaused( mPaused );//cheyingkun add	//解决“连续点击发送双开应用广播的按钮，回到桌面后图标和开关状态不匹配”的问题【c_0004466】
		mDragController.cancelDrag();
		mDragController.resetLastGestureUpTime();
		// We call onHide() aggressively. The custom content callbacks should be able to
		// debounce excess onHide calls.
		if( mWorkspace.getFavoritesPageCallbacks() != null )
		{
			mWorkspace.getFavoritesPageCallbacks().onHide();
		}
		umengStatisOnPause();//cheyingkun add	//优化加载速度(统计放到加载完成之后)
		// zhujieping@2015/08/24 ADD START,文件夹通知或搭配销售，原先是通过activity显示，在onpause状态就finish，现在到onpause状态也消失
		Folder folder = mWorkspace.getOpenFolder();
		if( folder != null )
		{
			folder.hideDynamicDialog();
		}
		// zhujieping@2015/08/24 ADD END
		if( mWorkspace.hasFavoritesPage() )
		{
			FavoritesPageManager.getInstance( this ).onPause();//cheyingkun add	//phenix1.1稳定版移植酷生活
		}
		// YANGTIANYU@2016/07/19 ADD START
		// 专属页onPause
		if( mWorkspace.hasCameraPage() )
		{
			CameraView.getInstance().onPause();
		}
		if( mWorkspace.hasMusicPage() )
		{
			MusicView.getInstance().onPause();
		}
		// YANGTIANYU@2016/07/19 ADD END
		if( mWaitingForResume != null )
		{
			// Resets the previous workspace icon press state
			mWaitingForResume.setStayPressed( false );//这个是点击图片周围会有一圈高亮，onpause时就不用绘制，无须等到onresume
		}
		//gaominghui add start //通知桌面切页：“讯虎”定制功能（双向光感切页）。详见“NotifyLauncherSnapPageManagerByBroadcastXH.java"中的备注
		NotifyLauncherSnapPageManagerByBroadcastXH mNotifyLauncherSnapPageManagerByBroadcastXH = NotifyLauncherSnapPageManagerByBroadcastXH.getInstance();
		if( mNotifyLauncherSnapPageManagerByBroadcastXH != null )
		{
			mNotifyLauncherSnapPageManagerByBroadcastXH.disableGesture( this );
		}
		//gaominghui add end
	}
	
	/**
	 * 将Logcat中的日志导出为文件并储存在手机中
	 * 为了解决那些难以复现的bug而创建的方法
	 * 
	 * @param fileName 文件名
	 * @author yangxiaoming
	 * @date 2015-05-18
	 */
	public static void getLogCat(
			String fileName )
	{
		SimpleDateFormat formatter = new SimpleDateFormat( "-MM-dd-HH-mm-ss" );
		Date curDate = new Date( System.currentTimeMillis() );
		String str = formatter.format( curDate );
		try
		{
			File filename = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , "/" , fileName , "-date" , str , ".txt" ) );
			filename.createNewFile();
			String cmd = StringUtils.concat( "logcat -d -f " , filename.getAbsolutePath() );
			Runtime.getRuntime().exec( cmd );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	protected void onFinishBindingItems()
	{
		//chenliang add start	//解决“打开‘switch_enable_overview_show_pageIndicator’开关后，桌面加载完成后第一次进入编辑模式页面指示器的位置会变化”的问题。【c_0004625】
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR ) //编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
		{
			LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().initPageIndicatorY( this );
		}
		//chenliang add end
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "PROCEDURE TIME_DEFAULT_LAYOUT:" , ( System.currentTimeMillis() - sTime_applicationCreateStart ) ) );
		}
	}
	
	public void resetSearchDropTargetBarScroll()
	{
		mSearchDropTargetBar.animate().translationY( 0 ).start();
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			getSearchBar().animate().translationY( 0 ).start();
		}
	}
	
	public interface FavoritesPageCallbacks
	{
		
		// Custom content is completely shown
		public void onShow();
		
		// Custom content is completely hidden
		public void onHide();
		
		// Custom content scroll progress changed. From 0 (not showing) to 1 (fully showing).
		public void onScrollProgressChanged(
				float progress );
	}
	
	public void enterSystemSettings(
			View v )
	{
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		boolean mIsFromWorkspaceMenuListItem = isWorkspaceMenuListItem( v );//xiatian add	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】	
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		//cheyingkun add start	//添加友盟统计自定义事件(编辑模式系统设置)
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			if( mIsFromOverviewPanelButton )
			{
				MobclickAgent.onEvent( this , UmengStatistics.ENTER_SETTING_BY_EDIT_MODE );
			}
			//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
			else if( mIsFromWorkspaceMenuListItem )
			{
			}
			//xiatian add end
		}
		//cheyingkun add end
		Intent settings = new Intent( android.provider.Settings.ACTION_SETTINGS );
		settings.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		startActivity( settings );
	}
	
	// The custom content needs to offset its content to account for the SearchDropTargetBar
	public int getTopOffsetForFavoritesPage()
	{
		return mWorkspace.getPaddingTop();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		// Flag the loader to stop early before switching
		mModel.stopLoader();
		if( mAppsCustomizeContent != null )
		{
			mAppsCustomizeContent.surrender();
		}
		return Boolean.TRUE;
	}
	
	private boolean isfirstTimeWindowHasFocus = true;
	
	// We can't hide the IME if it was forced open.  So don't bother
	@Override
	public void onWindowFocusChanged(
			boolean hasFocus )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG && isfirstTimeWindowHasFocus && hasFocus )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "PROCEDURE TIME_VISIBLE:" , ( System.currentTimeMillis() - sTime_applicationCreateStart ) ) );
			isfirstTimeWindowHasFocus = false;
		}
		super.onWindowFocusChanged( hasFocus );
		mHasFocus = hasFocus;
		if( hasFocus )
		{
			if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_WINDOW_GET_FOCUS_BROADCAST_ACTION ) )
			{
				sendBroadcast( new Intent( LauncherDefaultConfig.CONFIG_WINDOW_GET_FOCUS_BROADCAST_ACTION ) );
			}
		}
		else
		{
			if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_WINDOW_LOST_FOCUS_BROADCAST_ACTION ) )
			{
				sendBroadcast( new Intent( LauncherDefaultConfig.CONFIG_WINDOW_LOST_FOCUS_BROADCAST_ACTION ) );
			}
		}
	}
	
	private boolean acceptFilter()
	{
		final InputMethodManager inputManager = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE );
		return !inputManager.isFullscreenMode();
	}
	
	@Override
	public boolean onKeyUp(
			int keyCode ,
			KeyEvent event )
	{
		//gaominghui add start //"点击数字键打开拨号界面"的功能V2：* # 键的处理，由onKeyDown改成 onKeyUp。【c_0004705】
		if( LauncherDefaultConfig.SWITCH_ENABLE_PRESS_NUMS_OR_STAR_OR_POUND_TO_DAIL )
		{
			String phoneNumber = "";
			switch( keyCode )
			{
				case KeyEvent.KEYCODE_POUND:
					phoneNumber = "#";
					break;
				case KeyEvent.KEYCODE_STAR:
					phoneNumber = "*";
					break;
			}
			if( !TextUtils.isEmpty( phoneNumber ) )
			{
				Intent intent = new Intent( Intent.ACTION_DIAL , Uri.parse( StringUtils.concat( "tel:" , phoneNumber ) ) );
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity( intent );
			}
		}
		//gaominghui add end 
		// zhangjin@2016/04/19 ADD START
		if( LauncherDefaultConfig.HERUNXIN_BIG_LAUNCHER && event.isCanceled() == false )
		{
			String mKeycode = "-1";
			switch( event.getKeyCode() )
			{
				case KeyEvent.KEYCODE_1:
					mKeycode = "1";
					break;
				case KeyEvent.KEYCODE_2:
					mKeycode = "2";
					break;
				case KeyEvent.KEYCODE_3:
					mKeycode = "3";
					break;
				case KeyEvent.KEYCODE_4:
					mKeycode = "4";
					break;
				case KeyEvent.KEYCODE_5:
					mKeycode = "5";
					break;
				case KeyEvent.KEYCODE_6:
					mKeycode = "6";
					break;
				case KeyEvent.KEYCODE_7:
					mKeycode = "7";
					break;
				case KeyEvent.KEYCODE_8:
					mKeycode = "8";
					break;
				case KeyEvent.KEYCODE_9:
					mKeycode = "9";
					break;
				case KeyEvent.KEYCODE_0:
					mKeycode = "0";
					break;
				case KeyEvent.KEYCODE_POUND:
					mKeycode = "#";
					break;
				case KeyEvent.KEYCODE_STAR:
					mKeycode = "*";
					break;
				default:
					break;
			}
			if( keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_3 || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5 || keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7 || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9 )
			{
				Intent mintent = new Intent( Intent.ACTION_MAIN );
				mintent.addCategory( Intent.CATEGORY_LAUNCHER );
				mintent.putExtra( "PhenixLauncher.event.getKeyCode()" , mKeycode );//传入keycode值
				mintent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				ComponentName cn = new ComponentName( "com.android.dialer" , "com.android.dialer.DialtactsActivity" );
				mintent.setComponent( cn );
				Launcher.this.startActivity( mintent );// keycode in dialer
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "isNumberPressd=true + entry dialer" );
				return true;
			}
		}
		// zhangjin@2016/04/19 ADD END
		//xiatian add start	//修改menu键的消息处理逻辑：由“onPrepareOptionsMenu中处理menu键逻辑（onPrepareOptionsMenu每次都返回false，确保menu键每次onKeyDown后都调用onPrepareOptionsMenu方法）”改为“onKeyDown和onKeyUp中处理menu键逻辑”。
		if(
		//
		( keyCode == KeyEvent.KEYCODE_MENU )
		//
		&& ( LauncherDefaultConfig.CONFIG_MENU_KEY_STYLE == LauncherDefaultConfig.MENU_KEY_STYLE_RESPONSE_IN_ON_KEY/* //xiatian add	//meun键响应事件的类型。0为在onKeyDown和onKeyUp中响应事件（支持menu键点击和长按）；1为在onPrepareOptionsMenu中响应事件（不支持menu键长按）。默认为0。 */)
		//
		)
		{
			//			Log.v( "menu key" , "up" );
			//xiatian add start	//meun键点击后（menu键的onKeyUp事件当做点击事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）或退出编辑模式（当前是编辑模式）；1为打开“最近任务”界面；2为打开“竖直列表”样式的桌面菜单。默认为0。
			//xiatian del start
			//			if( mIgnoreNextMenuKeyUpEventForMenuKeyLongPress )
			//			{
			//				mIgnoreNextMenuKeyUpEventForMenuKeyLongPress = false;
			//				//				Log.v( "menu key" , "up - IgnoreMenuKeyUpEventForMenuKeyLongPress" );
			//			}
			//			else
			//			{
			//				if( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_ENTER_OR_EXIT_EDIT_MODE )//cheyingkun add	//是否支持长按menu键进入编辑模式。true为支持；false为不支持。默认false。【c_0003959】
			//				{
			//					enterOrExitEditMode();
			//				}
			//			}
			//xiatian del end
			onMenuKeyUp();//xiatian add
			//xiatian end
		}
		//xiatian add end
		return super.onKeyUp( keyCode , event );
	}
	
	@Override
	public boolean onKeyDown(
			int keyCode ,
			KeyEvent event )
	{
		final int uniChar = event.getUnicodeChar();
		final boolean handled = super.onKeyDown( keyCode , event );
		final boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace( uniChar );
		if( !handled && acceptFilter() && isKeyNotWhitespace )
		{
			boolean gotKey = TextKeyListener.getInstance().onKeyDown( mWorkspace , mDefaultKeySsb , keyCode , event );
			if( gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0 )
			{
				// something usable has been typed - start a search
				// the typed text will be retrieved and cleared by
				// showSearchDialog()
				// If there are multiple keystrokes before the search dialog takes focus,
				// onSearchRequested() will be called for every keystroke,
				// but it is idempotent, so it's fine.
				//xiatian add start	//是否支持"点击数字键打开拨号界面"的功能。true为支持，false为不支持。默认false。
				if( LauncherDefaultConfig.SWITCH_ENABLE_PRESS_NUMS_OR_STAR_OR_POUND_TO_DAIL )
				{
					String phoneNumber = "";
					switch( keyCode )
					{
						case KeyEvent.KEYCODE_0:
							phoneNumber = "0";
							break;
						case KeyEvent.KEYCODE_1:
							phoneNumber = "1";
							break;
						case KeyEvent.KEYCODE_2:
							phoneNumber = "2";
							break;
						case KeyEvent.KEYCODE_3:
							phoneNumber = "3";
							break;
						case KeyEvent.KEYCODE_4:
							phoneNumber = "4";
							break;
						case KeyEvent.KEYCODE_5:
							phoneNumber = "5";
							break;
						case KeyEvent.KEYCODE_6:
							phoneNumber = "6";
							break;
						case KeyEvent.KEYCODE_7:
							phoneNumber = "7";
							break;
						case KeyEvent.KEYCODE_8:
							phoneNumber = "8";
							break;
						case KeyEvent.KEYCODE_9:
							phoneNumber = "9";
							break;
					//gaominghui delete start //"点击数字键打开拨号界面"的功能V2：* # 键的处理，由onKeyDown改成 onKeyUp。【c_0004705】
					/*case KeyEvent.KEYCODE_POUND:
						phoneNumber = "#";
						break;
					case KeyEvent.KEYCODE_STAR:
						phoneNumber = "*";
						break;*/
					//gaominghui delete end 
					}
					if( !TextUtils.isEmpty( phoneNumber ) )
					{
						Intent intent = new Intent( Intent.ACTION_DIAL , Uri.parse( StringUtils.concat( "tel:" , phoneNumber ) ) );
						intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						startActivity( intent );
					}
				}
				else
				//xiatian add end
				{
					// zhangjin@2016/04/19 UPD START
					//return onSearchRequested();
					if( LauncherDefaultConfig.HERUNXIN_BIG_LAUNCHER == false )
					{
						return onSearchRequested();
					}
					// zhangjin@2016/04/19 UPD END
				}
			}
		}
		// Eat the long press event so the keyboard doesn't come up.
		if(
		//
		( keyCode == KeyEvent.KEYCODE_MENU )
		//
		&& ( LauncherDefaultConfig.CONFIG_MENU_KEY_STYLE == LauncherDefaultConfig.MENU_KEY_STYLE_RESPONSE_IN_ON_KEY/* //xiatian add	//meun键响应事件的类型。0为在onKeyDown和onKeyUp中响应事件（支持menu键点击和长按）；1为在onPrepareOptionsMenu中响应事件（不支持menu键长按）。默认为0。 */)
		//
		)
		{
			//			Log.v( "menu key" , "down" );
			if( event.isLongPress() )
			{
				//				Log.v( "menu key" , "isLongPress" );
				//xiatian add start	//meun键长按后（menu键的onKeyDown事件中判断mKeyEvent.isLongPress()为true，则当做长按事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）；1为打开“最近任务”界面。默认为-1。
				//xiatian del start
				//				//cheyingkun add start	//是否支持长按menu键进入编辑模式。true为支持；false为不支持。默认false。【c_0003959】
				//				if( LauncherDefaultConfig.CONFIG_MENU_LONG_CLICK_STYLE == LauncherDefaultConfig.MENU_LONG_CLICK_STYLE_ENTER_EDIT_MODE )
				//				{
				//					mIgnoreNextMenuKeyUpEventForMenuKeyLongPress = true;//xiatian add	//修改menu键的消息处理逻辑：由“onPrepareOptionsMenu中处理menu键逻辑（onPrepareOptionsMenu每次都返回false，确保menu键每次onKeyDown后都调用onPrepareOptionsMenu方法）”改为“onKeyDown和onKeyUp中处理menu键逻辑”。
				//					enterEditMode();
				//				}
				//				//cheyingkun add end	//是否支持长按menu键进入编辑模式。true为支持；false为不支持。默认false。【c_0003959】
				//				return true;
				//xiatian del end
				return( ( onMenuKeyLongPress() == true ) ? true : handled );//xiatian add
				//xiatian end
			}
		}
		return handled;
	}
	
	private String getTypedText()
	{
		return mDefaultKeySsb.toString();
	}
	
	private void clearTypedText()
	{
		mDefaultKeySsb.clear();
		mDefaultKeySsb.clearSpans();
		Selection.setSelection( mDefaultKeySsb , 0 );
	}
	
	/**
	 * Given the integer (ordinal) value of a State enum instance, convert it to a variable of type
	 * State
	 */
	private static State intToState(
			int stateOrdinal )
	{
		State state = State.WORKSPACE;
		final State[] stateValues = State.values();
		for( int i = 0 ; i < stateValues.length ; i++ )
		{
			if( stateValues[i].ordinal() == stateOrdinal )
			{
				state = stateValues[i];
				break;
			}
		}
		return state;
	}
	
	/**
	 * Restores the previous state, if it exists.
	 *
	 * @param savedState The previous state.
	 */
	private void restoreState(
			Bundle savedState )
	{
		if( savedState == null )
		{
			return;
		}
		State state = intToState( savedState.getInt( RUNTIME_STATE , State.WORKSPACE.ordinal() ) );
		if( state == State.APPS_CUSTOMIZE )
		{
			mOnResumeState = State.APPS_CUSTOMIZE;
		}
		int currentScreen = savedState.getInt( RUNTIME_STATE_CURRENT_SCREEN , PagedView.INVALID_RESTORE_PAGE );
		if( currentScreen != PagedView.INVALID_RESTORE_PAGE )
		{
			mWorkspace.setRestorePage( currentScreen );
		}
		final long pendingAddContainer = savedState.getLong( RUNTIME_STATE_PENDING_ADD_CONTAINER , -1 );
		final long pendingAddScreen = savedState.getLong( RUNTIME_STATE_PENDING_ADD_SCREEN , -1 );
		if( pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1 )
		{
			mPendingAddInfo.setContainer( pendingAddContainer );
			mPendingAddInfo.setScreenId( pendingAddScreen );
			mPendingAddInfo.setCellX( savedState.getInt( RUNTIME_STATE_PENDING_ADD_CELL_X ) );
			mPendingAddInfo.setCellY( savedState.getInt( RUNTIME_STATE_PENDING_ADD_CELL_Y ) );
			mPendingAddInfo.setSpanX( savedState.getInt( RUNTIME_STATE_PENDING_ADD_SPAN_X ) );
			mPendingAddInfo.setSpanY( savedState.getInt( RUNTIME_STATE_PENDING_ADD_SPAN_Y ) );
			mPendingAddWidgetInfo = savedState.getParcelable( RUNTIME_STATE_PENDING_ADD_WIDGET_INFO );
			mWaitingForResult = true;
			mRestoring = true;
		}
		boolean renameFolder = savedState.getBoolean( RUNTIME_STATE_PENDING_FOLDER_RENAME , false );
		if( renameFolder )
		{
			long id = savedState.getLong( RUNTIME_STATE_PENDING_FOLDER_RENAME_ID );
			mFolderInfo = mModel.getFolderById( this , sFolders , id );
			mRestoring = true;
		}
		// Restore the AppsCustomize tab
		if( mAppsCustomizeTabHost != null )
		{
			String curTab = savedState.getString( "apps_customize_currentTab" );
			if( curTab != null )
			{
				mAppsCustomizeTabHost.setContentTypeImmediate( mAppsCustomizeTabHost.getContentTypeForTabTag( curTab ) );
				mAppsCustomizeContent.loadAssociatedPages( mAppsCustomizeContent.getCurrentPage() );
			}
			int currentIndex = savedState.getInt( "apps_customize_currentIndex" );
			mAppsCustomizeContent.restorePageForIndex( currentIndex );
		}
	}
	
	/**
	 * Finds all the views we need and configure them properly.
	 */
	private void setupViews()
	{
		final DragController dragController = mDragController;
		mLauncherView = findViewById( R.id.launcher );
		mDragLayer = (DragLayer)findViewById( R.id.drag_layer );
		mWorkspace = (Workspace)mDragLayer.findViewById( R.id.workspace );
		// gaominghui@2016/12/14 ADD START
		if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
			mLauncherView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION );
		else
			mLauncherView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );
		// gaominghui@2016/12/14 ADD END
		// Setup the drag layer
		mDragLayer.setup( this , dragController );
		// Setup the hotseat
		mHotseat = (Hotseat)findViewById( R.id.hotseat );
		if( mHotseat != null )
		{
			mHotseat.setup( this );
			mHotseat.setOnLongClickListener( this );
		}
		//Setup the OverviewPanel
		mOverviewPanel = (ViewGroup)findViewById( R.id.overview_panel );
		if( mOverviewPanel != null )
		{
			setupOverviewPanelViews();
			//<phenix modify> liuhailin@2015-01-27 modify end
			mOverviewPanel.setAlpha( 0f );
			// Setup the workspace
		}
		mWorkspace.setHapticFeedbackEnabled( false );
		mWorkspace.setOnLongClickListener( this );
		mWorkspace.setup( dragController );
		dragController.addDragListener( mWorkspace );
		// Get the search/delete bar
		mSearchDropTargetBar = (SearchDropTargetBar)mDragLayer.findViewById( R.id.search_drop_target_bar_id );
		// Setup AppsCustomize
		mAppsCustomizeTabHost = (AppsCustomizeTabHost)findViewById( R.id.apps_customize_pane );
		mAppsCustomizeContent = (AppsCustomizePagedView)mAppsCustomizeTabHost.findViewById( R.id.apps_customize_pane_content );
		mAppsCustomizeContent.setup( this , dragController );
		// Setup the drag controller (drop targets have to be added in reverse order in priority)
		dragController.setDragScoller( mWorkspace );
		dragController.setScrollView( mDragLayer );
		dragController.setMoveTarget( mWorkspace );
		dragController.addDropTarget( mWorkspace );
		if( mSearchDropTargetBar != null )
		{
			mSearchDropTargetBar.setup( this , dragController );
			//<测试> hongqingquan@2015-03-18 modify begin
			//mSearchDropTargetBar.setVisibility( View.GONE );
			//<测试> hongqingquan@2015-03-18 modify end
		}
		if( LauncherDefaultConfig.getBoolean( R.bool.debug_memory_enabled ) )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "adding WeightWatcher" );
			mWeightWatcher = new WeightWatcher( this );
			mWeightWatcher.setAlpha( 0.5f );
			( (FrameLayout)mLauncherView ).addView( mWeightWatcher , new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.WRAP_CONTENT , Gravity.BOTTOM ) );
			boolean show = shouldShowWeightWatcher();
			mWeightWatcher.setVisibility( show ? View.VISIBLE : View.GONE );
		}
		// zhangjin@2016/05/05 ADD START
		// Setup Apps and Widgets
		if(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT/*//zhujieping add //需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		{
			if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
			{
				mAppsView = (AllAppsContainerView)LayoutInflater.from( mLauncher ).inflate( R.layout.all_apps , null );
			}
			//zhujieping add start	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。
			else if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT )
			{
				mAppsView = (AllAppsContainerView)LayoutInflater.from( mLauncher ).inflate( R.layout.all_apps_nougat , null );
			}
			//zhujieping add end
			if( mAppsView.getParent() == null )
			{
				mDragLayer.addView( mAppsView );
			}
			mAppsView.setVisibility( View.GONE );//初始化的visible是gone的
			mAppsView.setSearchBarController( mAppsView.newDefaultAppSearchController() );
		}
		// zhangjin@2016/05/05 ADD END
		// Setup Apps and Widgets
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		if( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_WORKSPACE_MENU_VERTICAL_LIST )
		{
			mWorkspaceMenuVerticalList = (WorkspaceMenuVerticalList)LayoutInflater.from( this ).inflate( R.layout.workspace_menu_vertical_list , null );
			mWorkspaceMenuVerticalList.setVisibility( View.INVISIBLE );
			if( mWorkspaceMenuVerticalList.getParent() == null )
			{
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.MATCH_PARENT );
				params.gravity = Gravity.CENTER_HORIZONTAL;
				mDragLayer.addView( mWorkspaceMenuVerticalList , params );
				mWorkspaceMenuVerticalList.setLauncher( this );
			}
		}
		//xiatian add end
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		if(
		//
		LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
		//	
				&& LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		//
		)
		{
			mDragLayer.setupAllAppsTransitionController( mAllAppsController );
			//zhujieping add start //7.0进入主菜单动画改成也支持4.4主菜单样式
			if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_KITKAT )
			{
				mAllAppsController.setupViews( mAppsCustomizeTabHost , mHotseat , mWorkspace );
			}
			else
			//zhujieping add end
			{
				mAllAppsController.setupViews( mAppsView , mHotseat , mWorkspace );
			}
		}
		//zhujieping add end
		indexOfWorkspace = mDragLayer.indexOfChild( mWorkspace );//fulijuan add		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）   //当广告显示时，使得搜索栏显示在广告下面
	}
	
	/**
	 * Creates a view representing a shortcut.
	 *
	 * @param info The data structure describing the shortcut.
	 *
	 * @return A View inflated from R.layout.application.
	 */
	View createShortcut(
			ShortcutInfo info )
	{
		return createShortcut( R.layout.application , (ViewGroup)mWorkspace.getChildAt( mWorkspace.getCurrentPage() ) , info );
	}
	
	/**
	 * Creates a view representing a shortcut inflated from the specified resource.
	 *
	 * @param layoutResId The id of the XML layout used to create the shortcut.
	 * @param parent The group the shortcut belongs to.
	 * @param info The data structure describing the shortcut.
	 *
	 * @return A View inflated from layoutResId.
	 */
	public View createShortcut(
			int layoutResId ,
			ViewGroup parent ,
			ShortcutInfo info )
	{
		BubbleTextView favorite = (BubbleTextView)mInflater.inflate( layoutResId , parent , false );
		favorite.applyFromShortcutInfo( info , mIconCache );
		favorite.setOnClickListener( this );
		return favorite;
	}
	
	/**
	 * Add an application shortcut to the workspace.
	 *
	 * @param data The intent describing the application.
	 * @param cellInfo The position on screen where to create the shortcut.
	 */
	void completeAddApplication(
			Intent data ,
			long container ,
			long screenId ,
			int cellX ,
			int cellY )
	{
		final int[] cellXY = mTmpAddItemCellCoordinates;
		final CellLayout layout = getCellLayout( container , screenId );
		// First we check if we already know the exact location where we want to add this item.
		if( cellX >= 0 && cellY >= 0 )
		{
			cellXY[0] = cellX;
			cellXY[1] = cellY;
		}
		else if( !layout.findCellForSpan( cellXY , 1 , 1 ) )
		{
			showOutOfSpaceMessage( isHotseatLayout( layout ) );
			return;
		}
		final ShortcutInfo info = mModel.getShortcutInfo( getPackageManager() , data , this );
		if( info != null )
		{
			info.setActivity( this , data.getComponent() , Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			info.setContainer( ItemInfo.NO_ID );
			mWorkspace.addApplicationShortcut( info , layout , container , screenId , cellXY[0] , cellXY[1] , isWorkspaceLocked() , cellX , cellY );
		}
		else
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "Couldn't find ActivityInfo for selected application: " , data.toUri( 0 ) ) );
		}
	}
	
	/**
	 * Add a shortcut to the workspace.
	 *
	 * @param data The intent describing the shortcut.
	 * @param cellInfo The position on screen where to create the shortcut.
	 */
	private void completeAddShortcut(
			Intent data ,
			long container ,
			long screenId ,
			int cellX ,
			int cellY )
	{
		int[] cellXY = mTmpAddItemCellCoordinates;
		int[] touchXY = mPendingAddInfo.getDropPos();
		CellLayout layout = getCellLayout( container , screenId );
		boolean foundCellSpan = false;
		ShortcutInfo info = mModel.infoFromShortcutIntent( this , data , null );
		if( info == null )
		{
			return;
		}
		//xiatian add start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
		//拖动图标生成1X1快捷方式时,先将将原始图片存入数据库后，再对info的icon加背板、蒙版和盖板
		ShortcutInfo mOldInfo = new ShortcutInfo( this , info );
		if( !( mOldInfo.getIntent().getComponent() != null && "com.cooee.wallpaper.host.WallpaperMainActivity".equals( mOldInfo.getIntent().getComponent().getClassName() ) ) )//一键换壁纸的图标不跟着主题走
			info.setIcon( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( info.getIcon() , this , false ) );//info的icon加背板、蒙版和盖板
		//xiatian add end
		final View view = createShortcut( info );
		// First we check if we already know the exact location where we want to add this item.
		if( cellX >= 0 && cellY >= 0 )
		{
			cellXY[0] = cellX;
			cellXY[1] = cellY;
			foundCellSpan = true;
			// If appropriate, either create a folder or add to an existing folder
			if( mWorkspace.createUserFolderIfNecessary( view , container , layout , cellXY , 0 , true , null , null ) )
			{
				return;
			}
			DragObject dragObject = new DragObject();
			dragObject.dragInfo = info;
			if( mWorkspace.addToExistingFolderIfNecessary( view , layout , cellXY , 0 , dragObject , true ) )
			{
				return;
			}
		}
		else if( touchXY != null )
		{
			// when dragging and dropping, just find the closest free spot
			int[] result = layout.findNearestVacantArea( touchXY[0] , touchXY[1] , 1 , 1 , cellXY );
			foundCellSpan = ( result != null );
		}
		else
		{
			foundCellSpan = layout.findCellForSpan( cellXY , 1 , 1 );
		}
		if( !foundCellSpan )
		{
			showOutOfSpaceMessage( isHotseatLayout( layout ) );
			return;
		}
		//xiatian start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
		//拖动图标生成1X1快捷方式时,先将将原始图片存入数据库后，再对info的icon加背板、蒙版和盖板
		//		LauncherModel.addItemToDatabase( this , info , container , screenId , cellXY[0] , cellXY[1] , false );//xiatian del
		LauncherModel.addItemToDatabase( this , mOldInfo , container , screenId , cellXY[0] , cellXY[1] , false );//xiatian add
		//xiatian end
		//cheyingkun add start	//解决“飞利浦图标样式时，添加小部件里的1*1插件到桌面，再拖动改插件到文件夹内，插件图标表现不一”的问题。【c_0003639】
		info.setContainer( container );
		info.setCellX( cellXY[0] );
		info.setCellY( cellXY[1] );
		info.setScreenId( mOldInfo.getScreenId() );
		info.setId( mOldInfo.getId() );
		//cheyingkun add end
		view.setTag( info );//cheyingkun add	//解决“长按1X1小部件添加到桌面，再长按删除快捷方式。重启桌面，被删除的快捷方式又出现”的问题。【c_0003267】
		if( !mRestoring )
		{
			mWorkspace.addInScreen( view , container , screenId , cellXY[0] , cellXY[1] , 1 , 1 , isWorkspaceLocked() );
		}
	}
	
	static int[] getSpanForWidget(
			Context context ,
			ComponentName component ,
			int minWidth ,
			int minHeight )
	{
		Rect padding = AppWidgetHostView.getDefaultPaddingForWidget( context , component , null );
		// We want to account for the extra amount of padding that we are adding to the widget
		// to ensure that it gets the full amount of space that it has requested
		int requiredWidth = minWidth + padding.left + padding.right;
		int requiredHeight = minHeight + padding.top + padding.bottom;
		//cheyingkun start	//修正widget行列数的计算方式
		//		int[] ret = CellLayout.rectToCell( requiredWidth , requiredHeight , null );//cheyingkun del
		int[] ret = CellLayout.rectToCellWidget( requiredWidth , requiredHeight , null );//cheyingkun add
		//cheyingkun end
		//xiatian add start	//添加保护，修正插件所占格子数，确保不超出最大格子数。
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		if( ret[0] > grid.getNumColumns() )
		{
			ret[0] = (int)grid.getNumColumns();
		}
		if( ret[1] > grid.getNumRows() )
		{
			ret[1] = (int)grid.getNumRows();
		}
		//xiatian add end
		return ret;
	}
	
	public static int[] getSpanForWidget(
			Context context ,
			AppWidgetProviderInfo info )
	{
		return getSpanForWidget( context , info.provider , info.minWidth , info.minHeight );
	}
	
	public static int[] getMinSpanForWidget(
			Context context ,
			AppWidgetProviderInfo info )
	{
		return getSpanForWidget( context , info.provider , info.minResizeWidth , info.minResizeHeight );
	}
	
	static int[] getSpanForWidget(
			Context context ,
			PendingAddWidgetInfo info )
	{
		return getSpanForWidget( context , info.getComponentName() , info.getMinWidth() , info.getMinHeight() );
	}
	
	static int[] getMinSpanForWidget(
			Context context ,
			PendingAddWidgetInfo info )
	{
		return getSpanForWidget( context , info.getComponentName() , info.getMinResizeWidth() , info.getMinResizeHeight() );
	}
	
	/**
	 * Add a widget to the workspace.
	 *
	 * @param appWidgetId The app widget id
	 * @param cellInfo The position on screen where to create the widget.
	 */
	private void completeAddAppWidget(
			final int appWidgetId ,
			long container ,
			long screenId ,
			AppWidgetHostView hostView ,
			AppWidgetProviderInfo appWidgetInfo )
	{
		if( appWidgetInfo == null )
		{
			appWidgetInfo = mAppWidgetManager.getAppWidgetInfo( appWidgetId );
		}
		// Calculate the grid spans needed to fit this widget
		CellLayout layout = getCellLayout( container , screenId );
		int[] minSpanXY = getMinSpanForWidget( this , appWidgetInfo );
		int[] spanXY = getSpanForWidget( this , appWidgetInfo );
		// Try finding open space on Launcher screen
		// We have saved the position to which the widget was dragged-- this really only matters
		// if we are placing widgets on a "spring-loaded" screen
		int[] cellXY = mTmpAddItemCellCoordinates;
		int[] touchXY = mPendingAddInfo.getDropPos();
		int[] finalSpan = new int[2];
		boolean foundCellSpan = false;
		if( mPendingAddInfo.getCellX() >= 0 && mPendingAddInfo.getCellY() >= 0 )
		{
			cellXY[0] = mPendingAddInfo.getCellX();
			cellXY[1] = mPendingAddInfo.getCellY();
			spanXY[0] = mPendingAddInfo.getSpanX();
			spanXY[1] = mPendingAddInfo.getSpanY();
			foundCellSpan = true;
		}
		else if( touchXY != null )
		{
			// when dragging and dropping, just find the closest free spot
			int[] result = layout.findNearestVacantArea( touchXY[0] , touchXY[1] , minSpanXY[0] , minSpanXY[1] , spanXY[0] , spanXY[1] , cellXY , finalSpan );
			spanXY[0] = finalSpan[0];
			spanXY[1] = finalSpan[1];
			foundCellSpan = ( result != null );
		}
		else
		{
			//xiatian start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“桌面菜单的小组件添加插件时，由于格子数错误导致的插件和其他图标重叠”的问题。）
			//xiatian del start
			//			int mSpanX = minSpanXY[0];
			//			int mSpanY = minSpanXY[1];
			//xiatian del end
			//xiatian add start
			int mSpanX = spanXY[0];
			int mSpanY = spanXY[1];
			//xiatian add end
			//xiatian end
			foundCellSpan = layout.findCellForSpan( cellXY , mSpanX , mSpanY );
		}
		if( !foundCellSpan )
		{
			if( appWidgetId != -1 )
			{
				// Deleting an app widget ID is a void call but writes to disk before returning
				// to the caller...
				new Thread( "deleteAppWidgetId" ) {
					
					public void run()
					{
						mAppWidgetHost.deleteAppWidgetId( appWidgetId );
					}
				}.start();
			}
			showOutOfSpaceMessage( isHotseatLayout( layout ) );
			return;
		}
		// Build Launcher-specific widget info and save to database
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo( appWidgetId , appWidgetInfo.provider );
		launcherInfo.setSpanX( spanXY[0] );
		launcherInfo.setSpanY( spanXY[1] );
		int mMinSpanX = 0;
		int mMinSpanY = 0;
		if( mPendingAddInfo.getMinSpanX() != 0 )
		{
			mMinSpanX = mPendingAddInfo.getMinSpanX();
		}
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“进入编辑模式的小组件界面添加插件后，再进入桌面菜单的小组件添加插件，这时添加到桌面的插件宽高都减一”的问题。【i_0014856】）
		else
		{
			mMinSpanX = minSpanXY[0];
		}
		//xiatian add end
		if( mPendingAddInfo.getMinSpanY() != 0 )
		{
			mMinSpanY = mPendingAddInfo.getMinSpanY();
		}
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“进入编辑模式的小组件界面添加插件后，再进入桌面菜单的小组件添加插件，这时添加到桌面的插件宽高都减一”的问题。【i_0014856】）
		else
		{
			mMinSpanY = minSpanXY[1];
		}
		//xiatian add end
		launcherInfo.setMinSpanX( mMinSpanX );
		launcherInfo.setMinSpanY( mMinSpanY );
		launcherInfo.setCellX( cellXY[0] );
		launcherInfo.setCellY( cellXY[1] );
		launcherInfo.setScreenId( screenId );
		launcherInfo.setContainer( LauncherSettings.Favorites.CONTAINER_DESKTOP );
		mWorkspace.checkItemIsOccupied( launcherInfo );//在此也要判断一下widget查找的空位是否已经被占据，若果被占据则重新查找 wanghongjian add 【bug：i_0012037】
		LauncherModel.addItemToDatabase( this , launcherInfo , container , launcherInfo.getScreenId() , launcherInfo.getCellX() , launcherInfo.getCellY() , false );
		if( !mRestoring )
		{
			if( hostView == null )
			{
				// Perform actual inflation because we're live
				launcherInfo.setAppWidgetHostView( mAppWidgetHost.createView( this , appWidgetId , appWidgetInfo ) );
				launcherInfo.getAppWidgetHostView().setAppWidget( appWidgetId , appWidgetInfo );
			}
			else
			{
				// The AppWidgetHostView has already been inflated and instantiated
				launcherInfo.setAppWidgetHostView( hostView );
			}
			launcherInfo.getAppWidgetHostView().setTag( launcherInfo );
			launcherInfo.getAppWidgetHostView().setVisibility( View.VISIBLE );
			launcherInfo.notifyWidgetSizeChanged( this );
			mWorkspace.addInScreen(
					launcherInfo.getAppWidgetHostView() ,
					container ,
					launcherInfo.getScreenId() ,
					launcherInfo.getCellX() ,
					launcherInfo.getCellY() ,
					launcherInfo.getSpanX() ,
					launcherInfo.getSpanY() ,
					isWorkspaceLocked() );
			addWidgetToAutoAdvanceIfNeeded( launcherInfo.getAppWidgetHostView() , appWidgetInfo );
		}
		resetAddInfo();
	}
	
	//fulijuan add start	//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）   //当广告显示时，使得搜索栏显示在广告下面
	private int indexOfWorkspace = -1;
	private boolean isWorkspaceToFront = false;
	
	public void bringWorkspaceToFront()
	{
		if( isWorkspaceToFront && mWorkspace != null )
		{
			mWorkspace.bringToFront();
			isWorkspaceToFront = false;
		}
	}
	//fulijuan add end
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			final String action = intent.getAction();
			if( Intent.ACTION_SCREEN_OFF.equals( action ) )
			{
				//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
				if(
				//
				LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_WORKSPACE_MENU_VERTICAL_LIST
				//
				&& ( mWorkspaceMenuVerticalList != null )
				//
				&& ( mWorkspaceMenuVerticalList.getVisibility() == View.VISIBLE )
				//
				)
				{//灭屏时，隐藏桌面菜单
					mWorkspaceMenuVerticalList.hideNoAnim();
				}
				//xiatian add end
				//cheyingkun add start	//修改运营酷搜逻辑(改为锁屏时桌面重启)
				if( search_need_restart )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "PROCEDURE Launcher onReceive() ACTION_SCREEN_OFF search_need_restart killProcess!!" );
					search_need_restart = false;
					android.os.Process.killProcess( android.os.Process.myPid() );
				}
				//cheyingkun add end
				//cheyingkun add start	//服务器关闭酷生活后，释放资源。
				if( favoritesViewRemove )
				{
					favoritesViewRemove = false;
					if( mWorkspace.hasFavoritesPage() )//cheyingkun add	//服务器关闭酷生活,桌面移除酷生活view前判断酷生活是否存在
					{
						mWorkspace.removeFavoritesPage();
						//clearFavoritesView
						FavoritesPageManager.getInstance( Launcher.this ).clearFavoritesView();
					}
				}
				//cheyingkun add end
				//gaominghui add start  //需求：支持后台运营音乐页和相机页
				if( !LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW && mWorkspace.hasMusicPage() )
				{
					mWorkspace.removeMusicPage();
				}
				if( !LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW && mWorkspace.hasCameraPage() )
				{
					mWorkspace.removeCameraPage();
				}
				//gaominghui add end
				mUserPresent = false;
				mDragLayer.clearAllResizeFrames();
				updateRunning();
				//xiatian del start	//需求：在编辑模式下灭屏，不退出编辑模式。
				//				// Reset AllApps to its initial state only if we are not in the middle of
				//				// processing a multi-step drop
				//				if( mAppsCustomizeTabHost != null && mPendingAddInfo.getContainer() == ItemInfo.NO_ID )
				//				{
				//					showWorkspace( false );
				//				}
				//xiatian del end
			}
			else if( Intent.ACTION_USER_PRESENT.equals( action ) )
			{
				mUserPresent = true;
				updateRunning();
			}
			//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）   //当广告显示时，使得搜索栏显示在广告下面
			else if( "com.cooee.favorites.searchbar.enable".equals( intent.getAction() ) )
			{
				if( indexOfWorkspace >= 0 && indexOfWorkspace < mDragLayer.getChildCount() )
				{
					mDragLayer.removeView( mWorkspace );
					mDragLayer.addView( mWorkspace , indexOfWorkspace );
				}
			}
			else if( "com.cooee.favorites.searchbar.disable".equals( intent.getAction() ) )
			{
				isWorkspaceToFront = true;
			}
			//fulijuan add end
		}
	};
	
	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		// Listen for broadcasts related to user-presence
		final IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_SCREEN_OFF );
		filter.addAction( Intent.ACTION_USER_PRESENT );
		//fulijuan add start	//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）   //当广告显示时，使得搜索栏显示在广告下面
		filter.addAction( "com.cooee.favorites.searchbar.enable" );
		filter.addAction( "com.cooee.favorites.searchbar.disable" );
		//fulijuna add end
		registerReceiver( mReceiver , filter );
		FirstFrameAnimatorHelper.initializeDrawListener( getWindow().getDecorView() );
		mAttached = true;
		mVisible = true;
	}
	
	@Override
	public void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		mVisible = false;
		if( mAttached )
		{
			unregisterReceiver( mReceiver );
			mAttached = false;
		}
		updateRunning();
	}
	
	public void onWindowVisibilityChanged(
			int visibility )
	{
		mVisible = visibility == View.VISIBLE;
		updateRunning();
		// The following code used to be in onResume, but it turns out onResume is called when
		// you're in All Apps and click home to go to the workspace. onWindowVisibilityChanged
		// is a more appropriate event to handle
		if( mVisible )
		{
			mAppsCustomizeTabHost.onWindowVisible();
			// gaominghui@2016/12/14 ADD START 兼容android4.0
			if( !mWorkspaceLoading && Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
			// gaominghui@2016/12/14 ADD END API 兼容android4.0
			{
				final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
				// We want to let Launcher draw itself at least once before we force it to build
				// layers on all the workspace pages, so that transitioning to Launcher from other
				// apps is nice and speedy.
				observer.addOnDrawListener( new ViewTreeObserver.OnDrawListener() {
					
					private boolean mStarted = false;
					
					public void onDraw()
					{
						if( mStarted )
							return;
						mStarted = true;
						// We delay the layer building a bit in order to give
						// other message processing a time to run.  In particular
						// this avoids a delay in hiding the IME if it was
						// currently shown, because doing that may involve
						// some communication back with the app.
						mWorkspace.postDelayed( mBuildLayersRunnable , 500 );
						final ViewTreeObserver.OnDrawListener listener = this;
						mWorkspace.post( new Runnable() {
							
							public void run()
							{
								if( mWorkspace != null && mWorkspace.getViewTreeObserver() != null )
								{
									mWorkspace.getViewTreeObserver().removeOnDrawListener( listener );
								}
							}
						} );
						return;
					}
				} );
			}
			// When Launcher comes back to foreground, a different Activity might be responsible for
			// the app market intent, so refresh the icon
			if( SHOW_MARKET_BUTTON )
			{
				updateAppMarketIcon();
			}
			clearTypedText();
		}
	}
	
	private void sendAdvanceMessage(
			long delay )
	{
		mHandler.removeMessages( ADVANCE_MSG );
		Message msg = mHandler.obtainMessage( ADVANCE_MSG );
		mHandler.sendMessageDelayed( msg , delay );
		mAutoAdvanceSentTime = System.currentTimeMillis();
	}
	
	private void updateRunning()
	{
		boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
		if( autoAdvanceRunning != mAutoAdvanceRunning )
		{
			mAutoAdvanceRunning = autoAdvanceRunning;
			if( autoAdvanceRunning )
			{
				long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
				sendAdvanceMessage( delay );
			}
			else
			{
				if( !mWidgetsToAdvance.isEmpty() )
				{
					mAutoAdvanceTimeLeft = Math.max( 0 , mAdvanceInterval - ( System.currentTimeMillis() - mAutoAdvanceSentTime ) );
				}
				mHandler.removeMessages( ADVANCE_MSG );
				mHandler.removeMessages( 0 ); // Remove messages sent using postDelayed()
			}
		}
	}
	
	private final Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			if( msg.what == ADVANCE_MSG )
			{
				int i = 0;
				for( View key : mWidgetsToAdvance.keySet() )
				{
					final View v = key.findViewById( mWidgetsToAdvance.get( key ).autoAdvanceViewId );
					final int delay = mAdvanceStagger * i;
					if( v instanceof Advanceable )
					{
						postDelayed( new Runnable() {
							
							public void run()
							{
								( (Advanceable)v ).advance();
							}
						} , delay );
					}
					i++;
				}
				sendAdvanceMessage( mAdvanceInterval );
			}
			//cheyingkun add start	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
			//收到消息后更新appBitmap	
			else if( msg.what == TCARDMOUNT_UPDATE_APP_BITMAP )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , "  收到消息,更新bitmap  " );
				ArrayList<AppInfo> apps = (ArrayList<AppInfo>)msg.obj;
				Launcher.this.AppsUpdatedBitmap( apps );
				Launcher.this.mWorkspace.updateDragView( apps );//cheyingkun add	//打开usb存储设备,图标灰色,长按灰色图标不松手,拔掉usb线时不再停止拖拽,改为更新dragview
			}
			//cheyingkun add end
			//zhujieping add start,记录应用点击次数
			else if( msg.what == EVENT_ADD_APP_USE_FREQUENCY )
			{
				int useFrequency = mSharedPrefs.getInt( (String)msg.obj , 0 );
				mSharedPrefs.edit().putInt( (String)msg.obj , useFrequency + 1 ).commit();
			}
			//zhujieping add end
		}
	};
	
	void addWidgetToAutoAdvanceIfNeeded(
			View hostView ,
			AppWidgetProviderInfo appWidgetInfo )
	{
		if( appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1 )
			return;
		View v = hostView.findViewById( appWidgetInfo.autoAdvanceViewId );
		if( v instanceof Advanceable )
		{
			mWidgetsToAdvance.put( hostView , appWidgetInfo );
			( (Advanceable)v ).fyiWillBeAdvancedByHostKThx();
			updateRunning();
		}
	}
	
	void removeWidgetToAutoAdvance(
			View hostView )
	{
		if( mWidgetsToAdvance.containsKey( hostView ) )
		{
			mWidgetsToAdvance.remove( hostView );
			updateRunning();
		}
	}
	
	public void removeAppWidget(
			LauncherAppWidgetInfo launcherInfo )
	{
		removeWidgetToAutoAdvance( launcherInfo.getAppWidgetHostView() );
		launcherInfo.setAppWidgetHostView( null );
	}
	
	public void showOutOfSpaceMessage(
			boolean isHotseatLayout )
	{
		int strId = ( isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space );
		Toast.makeText( this , getString( strId ) , Toast.LENGTH_SHORT ).show();
	}
	
	public LauncherAppWidgetHost getAppWidgetHost()
	{
		return mAppWidgetHost;
	}
	
	public LauncherModel getModel()
	{
		return mModel;
	}
	
	public void closeSystemDialogs()
	{
		getWindow().closeAllPanels();
		// Whatever we were doing is hereby canceled.
		mWaitingForResult = false;
	}
	
	@Override
	protected void onNewIntent(
			Intent intent )
	{
		long startTime = 0;
		if( DEBUG_RESUME_TIME )
		{
			startTime = System.currentTimeMillis();
		}
		super.onNewIntent( intent );
		//zhujieping add start	//换主题不重启
		if( isThemeChanging() )
		{
			return;
		}
		//zhujieping add end
		// Close the menu
		if( needRestart )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "PROCEDURE Launcher onNewIntent() needRestart return" );
			return;
		}
		if( Intent.ACTION_MAIN.equals( intent.getAction() ) )
		{
			// also will cancel mWaitingForResult.
			closeSystemDialogs();
			//xiatian add start	//设置默认桌面引导
			if( LauncherDefaultConfig.SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE )
			{
				DefaultLauncherGuideManager.getInstance().closeDefaultLauncherGuideDialog();
			}
			//xiatian add end
			final boolean alreadyOnHome = mHasFocus && ( ( intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT ) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
			if( mWorkspace == null )
			{
				// Can be cases where mWorkspace is null, this prevents a NPE
				return;
			}
			//xiatian add start	//fix bug：解决“在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，桌面特效为之前的桌面特效（没有保存刚刚在屏幕切页特效界面中的选择）”的问题。【i_0010438】
			//【问题原因】
			//		1、在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home键回到默认主页的代码流程是：onNewIntent方法(Launcher.java)—>onResume方法(Launcher.java)—>onDestroy方法(LauncherSettingActivity.java)
			//		2、由于onDestroy方法(LauncherSettingActivity.java)保存桌面特效，onResume方法(Launcher.java)中初始化桌面特效，但是onDestroy方法(LauncherSettingActivity.java)在onResume方法(Launcher.java)之后，导致桌面特效不正确。
			//【解决方案】在onNewIntent方法(Launcher.java)中主动保存桌面特效。
			LauncherSettingsActivity mLauncherSettingsActivity = LauncherAppState.getInstance().getLauncherSettingsActivity();
			if( mLauncherSettingsActivity != null )
			{
				mLauncherSettingsActivity.saveWorkspaceEfffect();
			}
			//xiatian add end
			Folder openFolder = mWorkspace.getOpenFolder();
			// In all these cases, only animate if we're already on home
			mWorkspace.exitWidgetResizeMode();
			if( alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() && openFolder == null )
			{
				mWorkspace.moveToDefaultScreen( true );
			}
			closeFolder();
			exitSpringLoadedDragMode();
			// If we are already on home, then just animate back to the workspace,
			// otherwise, just wait until onResume to set the state back to Workspace
			if( alreadyOnHome )
			{
				showWorkspace( true );
			}
			else
			{
				mOnResumeState = State.WORKSPACE;
			}
			final View v = getWindow().peekDecorView();
			if( v != null && v.getWindowToken() != null )
			{
				InputMethodManager imm = (InputMethodManager)getSystemService( INPUT_METHOD_SERVICE );
				imm.hideSoftInputFromWindow( v.getWindowToken() , 0 );
			}
			// Reset the apps customize page
			if( mAppsCustomizeTabHost != null )
			{
				mAppsCustomizeTabHost.reset();
			}
			// zhangjin@2016/05/09 ADD START
			// Reset the apps view
			if(
			//
			( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
			//
			|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
			//
			)
			{
				if( !alreadyOnHome && mAppsView != null )
				{
					mAppsView.scrollToTop();
				}
			}
			// zhangjin@2016/05/09 ADD END
			//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
			if(
			//
			LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_WORKSPACE_MENU_VERTICAL_LIST
			//
			&& ( mWorkspaceMenuVerticalList != null )
			//
			&& ( mWorkspaceMenuVerticalList.getVisibility() == View.VISIBLE )
			//
			)
			{//home键时，隐藏桌面菜单
				mWorkspaceMenuVerticalList.hideNoAnim();
			}
			//xiatian add end
		}
		if( DEBUG_RESUME_TIME )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Time spent in onNewIntent: " , ( System.currentTimeMillis() - startTime ) ) );
		}
	}
	
	@Override
	public void onRestoreInstanceState(
			Bundle state )
	{
		super.onRestoreInstanceState( state );
		for( int page : mSynchronouslyBoundPages )
		{
			mWorkspace.restoreInstanceStateForChild( page );
		}
	}
	
	@Override
	protected void onSaveInstanceState(
			Bundle outState )
	{
		if( mWorkspace.getChildCount() > 0 )
		{
			outState.putInt( RUNTIME_STATE_CURRENT_SCREEN , mWorkspace.getRestorePage() );
		}
		super.onSaveInstanceState( outState );
		outState.putInt( RUNTIME_STATE , mState.ordinal() );
		// zhujieping@2015/07/22 DEL START
		//// We close any open folder since it will not be re-opened, and we need to make sure
		//// this state is reflected.
		//closeFolder();
		// zhujieping@2015/07/22 DEL END
		if( mPendingAddInfo.getContainer() != ItemInfo.NO_ID && mPendingAddInfo.getScreenId() > -1 && mWaitingForResult )
		{
			outState.putLong( RUNTIME_STATE_PENDING_ADD_CONTAINER , mPendingAddInfo.getContainer() );
			outState.putLong( RUNTIME_STATE_PENDING_ADD_SCREEN , mPendingAddInfo.getScreenId() );
			outState.putInt( RUNTIME_STATE_PENDING_ADD_CELL_X , mPendingAddInfo.getCellX() );
			outState.putInt( RUNTIME_STATE_PENDING_ADD_CELL_Y , mPendingAddInfo.getCellY() );
			outState.putInt( RUNTIME_STATE_PENDING_ADD_SPAN_X , mPendingAddInfo.getSpanX() );
			outState.putInt( RUNTIME_STATE_PENDING_ADD_SPAN_Y , mPendingAddInfo.getSpanY() );
			outState.putParcelable( RUNTIME_STATE_PENDING_ADD_WIDGET_INFO , mPendingAddWidgetInfo );
		}
		if( mFolderInfo != null && mWaitingForResult )
		{
			outState.putBoolean( RUNTIME_STATE_PENDING_FOLDER_RENAME , true );
			outState.putLong( RUNTIME_STATE_PENDING_FOLDER_RENAME_ID , mFolderInfo.getId() );
		}
		// Save the current AppsCustomize tab
		if( mAppsCustomizeTabHost != null )
		{
			String currentTabTag = mAppsCustomizeTabHost.getCurrentTabTag();
			if( currentTabTag != null )
			{
				outState.putString( "apps_customize_currentTab" , currentTabTag );
			}
			int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
			outState.putInt( "apps_customize_currentIndex" , currentIndex );
		}
	}
	
	@Override
	public void onDestroy()
	{
		//chenliang add start	//解决在“在安卓7.0手机上，切换系统语言或者字体大小等操作后按back键返回桌面，然后启动应用再返回桌面时偶先有很长的延时”的问题。【c_0004672】
		//【原因】：android7.0手机上，由于在onDestory方法中调用killProcess方法或者其他原因导致进程被杀死，桌面重新启动就会出现上述问题。
		//这段代码放在super后面的话会出现null指针异常，所以放在super前面执行。
		if( Build.VERSION.SDK_INT >= 24 )
		{
			if( mWorkspace.hasCameraPage() )
			{
				CameraView.getInstance().deleteInstance();
			}
			if( mWorkspace.hasMusicPage() )
			{
				MusicView.getInstance().deleteInstance();
			}
			if( mWorkspace.hasFavoritesPage() )
			{
				mWorkspace.removeFavoritesPage();
				//clearFavoritesView
				FavoritesPageManager.getInstance( Launcher.this ).clearFavoritesView();
			}
			LauncherEffectFragment.releaseStaticVariable();
			mLauncher = null;
		}
		//chenliang add end
		super.onDestroy();
		// Remove all pending runnables
		mHandler.removeMessages( ADVANCE_MSG );
		mHandler.removeMessages( 0 );
		mWorkspace.removeCallbacks( mBuildLayersRunnable );
		// Stop callbacks from LauncherModel
		LauncherAppState app = ( LauncherAppState.getInstance() );
		mModel.stopLoader();
		app.setLauncher( null );
		try
		{
			mAppWidgetHost.stopListening();
		}
		catch( NullPointerException ex )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( TAG , "problem while stopping AppWidgetHost during Launcher destruction" , ex );
		}
		mAppWidgetHost = null;
		mWidgetsToAdvance.clear();
		TextKeyListener.getInstance().release();
		// Disconnect any of the callbacks and drawables associated with ItemInfos on the workspace
		// to prevent leaking Launcher activities on orientation change.
		if( mModel != null )
		{
			mModel.unbindItemInfosAndClearQueuedBindRunnables();
			//cheyingkun add start	//解决“恢复出厂设置后会出现缺少相机应用图标”的问题。【c_0004169】
			if( LauncherDefaultConfig.getBoolean( R.bool.switch_enable_setAllAppsLoaded_onDestroy ) )
			{
				mModel.setAllAppsLoaded( false );
			}
			//cheyingkun add end
		}
		getContentResolver().unregisterContentObserver( mWidgetObserver );
		unregisterReceiver( mCloseSystemDialogsReceiver );
		mDragLayer.clearAllResizeFrames();
		( (ViewGroup)mWorkspace.getParent() ).removeAllViews();
		mWorkspace.removeAllViews();
		mWorkspace = null;
		mDragController = null;
		//xiatian add start	//通知桌面切页：“锐益”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerRY.java”中的备注。
		NotifyLauncherSnapPageManagerCustomerRY mNotifyLauncherSnapPageManagerCustomerRY = NotifyLauncherSnapPageManagerCustomerRY.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerRY != null )
		{
			mNotifyLauncherSnapPageManagerCustomerRY.unRegister( this );
		}
		//xiatian add end
		//gaominghui add start  //通知桌面切页：“讯虎”定制功能（单向光感切页）。详见“NotifyLauncherSnapPageManagerCustomerSingleTrackXH.java"中的备注
		NotifyLauncherSnapPageManagerCustomerSingleTrackXH mNotifyLauncherSnapPageManagerCustomerSingleTrackXH = NotifyLauncherSnapPageManagerCustomerSingleTrackXH.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerSingleTrackXH != null )
		{
			mNotifyLauncherSnapPageManagerCustomerSingleTrackXH.unRegister( this );
		}
		//gaominghui add end
		//gaominghui add start //支持通过AIDL切换主题，解决“手机第一次开机，应用主题慢”的问题【c_0004675】
		stopThemeService();
		//gaominghui add end //支持通过AIDL切换主题，解决“手机第一次开机，应用主题慢”的问题【c_0004675】
		LauncherAnimUtils.onDestroyActivity();
		DlManager.getInstance().removeAllNotify();
		//cheyingkun del start	//解决“切换单双层反应过慢”的问题。【i_0012595】
		//		//cheyingkun add start	//添加友盟统计自定义事件(程序结束前保存友盟统计数据)
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		//		{
		//			MobclickAgent.onKillProcess( this );
		//		}
		//		//cheyingkun add end
		//cheyingkun del end
		PreferenceManager.getDefaultSharedPreferences( getApplicationContext() ).unregisterOnSharedPreferenceChangeListener( mDefaultSharedPreferencesListener );//xiatian add	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		if( Build.VERSION.SDK_INT < 24 )//chenliang add	//解决在“在安卓7.0手机上，切换系统语言或者字体大小等操作后按back键返回桌面，然后启动应用再返回桌面时偶先有很长的延时”的问题。【c_0004672】
		{
			//语言切换，有些静态没有销毁，导致异常，现直接杀掉进程。 hupeng@2015/07/04 ADD START
			android.os.Process.killProcess( android.os.Process.myPid() );
			//语言切换，有些静态没有销毁，导致异常，现直接杀掉进程。 hupeng@2015/07/04 ADD END
		}
	}
	
	public DragController getDragController()
	{
		return mDragController;
	}
	
	//	zhujieping add，这个方法里不捕获异常，交给调用的地方捕获
	public void startActivityForResultNeedCatchException(
			Intent intent ,
			int requestCode ) throws Exception
	{
		if( requestCode >= 0 )
			mWaitingForResult = true;
		super.startActivityForResult( intent , requestCode );
	}
	
	@Override
	public void startActivityForResult(
			Intent intent ,
			int requestCode )
	{
		if( requestCode >= 0 )
			mWaitingForResult = true;
		//xiatian start	//添加保护，确保抛出异常时，launcher不重启。（现发现鼎智阿里云手机调用该方法时，如果intent中action的为“android.appwidget.action.APPWIDGET_BIND”，会没有规律的分别抛出异常“e==null”和“ActivityNotFoundException”）
		//		super.startActivityForResult( intent , requestCode );//xiatian del
		//xiatian add start
		try
		{
			super.startActivityForResult( intent , requestCode );
		}
		catch( Exception e )
		{
			if( e == null )
			{
				Toast.makeText( this , R.string.unknow_error , Toast.LENGTH_SHORT ).show();
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "Unable to startActivityForResult. tag=" , TAG , " intent=" , intent.toUri( 0 ) , " requestCode=" , requestCode ) );
			}
			else if( e instanceof ActivityNotFoundException )
			{
				Toast.makeText( this , R.string.activity_not_found , Toast.LENGTH_SHORT ).show();
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "Unable to startActivityForResult. tag=" , TAG , " intent=" , intent.toUri( 0 ) , " requestCode=" , requestCode ) , e );
			}
		}
		//xiatian add end
		//xiatian end
	}
	
	/**
	 * Indicates that we want global search for this activity by setting the globalSearch
	 * argument for {@link #startSearch} to true.
	 */
	@Override
	public void startSearch(
			String initialQuery ,
			boolean selectInitialQuery ,
			Bundle appSearchData ,
			boolean globalSearch )
	{
		// zhangjin@2016/05/10 UPD START
		//showWorkspace( true );
		//zhujieping modify,当主菜单有搜索时，调用这个方法，判断是否在workspace的状态，然后决定是否显示workspace
		if( mState == State.WORKSPACE )
		{
			showWorkspace( true );
		}
		// zhangjin@2016/05/10 UPD END
		if( initialQuery == null )
		{
			// Use any text typed in the launcher as the initial query
			initialQuery = getTypedText();
		}
		if( appSearchData == null )
		{
			appSearchData = new Bundle();
			appSearchData.putString( "source" , "launcher-search" );
		}
		Rect sourceBounds = new Rect();
		if( mSearchDropTargetBar != null )
		{
			sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
		}
		startSearch( initialQuery , selectInitialQuery , appSearchData , sourceBounds );
	}
	
	public void startSearch(
			String initialQuery ,
			boolean selectInitialQuery ,
			Bundle appSearchData ,
			Rect sourceBounds )
	{
		// cheyingkun add start //免责声明布局(点击搜索框时的免责声明)
		if( Disclaimer.isNeedShowDisclaimer() )
		{
			DisclaimerManager.getInstance( this ).showDisclaimer( DisclaimerManager.VISIT_NETWORK_DISCLAIMER_SEARCH , this );
		}
		else
		// cheyingkun add end
		{
			SearchActivityManager.getInstance( this ).startSearchActivity( this , initialQuery , selectInitialQuery , appSearchData , sourceBounds );
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(
			Menu menu )
	{
		//xiatian start	//修改menu键的消息处理逻辑：由“onPrepareOptionsMenu中处理menu键逻辑（onPrepareOptionsMenu每次都返回false，确保menu键每次onKeyDown后都调用onPrepareOptionsMenu方法）”改为“onKeyDown和onKeyUp中处理menu键逻辑”。
		//【备注】
		//	1、之前的方案在onPrepareOptionsMenu中处理menu键的消息处理逻辑。
		//	2、是通过onPrepareOptionsMenu每次都返回false，确保menu键每次onKeyDown后都调用onPrepareOptionsMenu方法。
		//xiatian del start
		//		super.onPrepareOptionsMenu( menu );
		//		//cheyingkun add start	//是否支持长按menu键进入编辑模式。true为支持；false为不支持。默认false。【c_0003959】
		//		if( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_ENTER_OR_EXIT_EDIT_MODE )
		//		{
		//			return entryOrExitEditMode();
		//		}
		//		//cheyingkun add end	//是否支持长按menu键进入编辑模式。true为支持；false为不支持。默认false。【c_0003959】
		//		return false;
		//xiatian del end
		//xiatian add start
		boolean ret = super.onPrepareOptionsMenu( menu );
		//xiatian add start	//meun键响应事件的类型。0为在onKeyDown和onKeyUp中响应事件（支持menu键点击和长按）；1为在onPrepareOptionsMenu中响应事件（不支持menu键长按）。默认为0。
		if( LauncherDefaultConfig.CONFIG_MENU_KEY_STYLE == LauncherDefaultConfig.MENU_KEY_STYLE_RESPONSE_IN_ON_PREPARE_OPTIONS_MENU )
		{
			onMenuKeyUp();
			ret = false;
		}
		//xiatian add end
		return ret;
		//xiatian add end
		//xiatian end
	}
	
	//cheyingkun add start	//是否支持长按menu键进入编辑模式。true为支持；false为不支持。默认false。【c_0003959】
	private void enterOrExitEditMode()
	{
		if( mState == State.WORKSPACE )
		{
			if( mWorkspace.isInOverviewMode() )
			{
				exitEditMode( true );
			}
			else
			{
				enterEditMode();
			}
		}
	}
	//cheyingkun add end	//是否支持长按menu键进入编辑模式。true为支持；false为不支持。默认false。【c_0003959】
	;
	
	private void enterEditMode()
	{
		if( canEnterOverviewMode() )
		{
			mWorkspace.enterOverviewMode();
		}
	}
	
	private void exitEditMode(
			boolean animated )
	{
		if( canExitOverviewMode() )
		{
			mWorkspace.exitOverviewMode( animated );
			//zhujieping add start 
			if( mEditModeEntity != null )
			{
				mEditModeEntity.exitSecondaryEditMode( mOverviewPanel );
			}
			//zhujieping add end
		}
	}
	
	@Override
	public boolean onSearchRequested()
	{
		//cheyingkun add start	//添加友盟统计自定义事件(搜索栏)
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( this , UmengStatistics.ENTER_SEARCH_BAR );
		}
		//cheyingkun add end
		startSearch( null , false , null , true );
		// Use a custom animation for launching search
		return true;
	}
	
	//0010328: 【智能分类】桌面智能分类时menu键进入编辑模式，分类成功后桌面报停止运行 , change by shlt@2015/03/06 ADD START
	private boolean canEnterOverviewMode()
	{
		return(
		//
		mState == State.WORKSPACE
		//
		&& ( mWorkspace != null )
		//
		&& ( mWorkspace.isInOverviewMode() == false )
		//
		&& ( isFirstRunCling() == false )
		//
		&& ( isFirstRunWorkspaceCling() == false )
		//
		&& ( isWorkspaceLocked() == false )
		//
		&& ( OperateHelp.getInstance( this ).isGettingOperateDate() == false/*//智能分类*/)
		//
		&& ( mModel.isLoadingWorkspace() == false/*//workspace 正在加载*/)
		//
		&& ( mStateAnimation == null || ( mStateAnimation != null && mStateAnimation.isRunning() == false ) /*//动画正在执行*/)
		//
		);
	}
	//0010328: 【智能分类】桌面智能分类时menu键进入编辑模式，分类成功后桌面报停止运行 , change by shlt@2015/03/06 ADD END
	;
	
	private boolean canClickOverviewPanelButton()
	{
		return(
		//
		mState == State.WORKSPACE
		//
		&& ( mWorkspace != null )
		//
		&& ( mWorkspace.isInOverviewMode() )
		//
		&& ( mStateAnimation == null || ( mStateAnimation != null && mStateAnimation.isRunning() == false ) /*//动画正在执行*/)
		//
		);
	}
	
	private boolean canExitOverviewMode()
	{
		return(
		//
		mState == State.WORKSPACE
		//
		&& ( mWorkspace != null )
		//
		&& ( mWorkspace.isInOverviewMode() )
		//
		&& ( mWorkspace.getFreeScroll()/*//只有当mFreeScroll为true的时候，可以退出编辑模式 bug:0010834 wanghongjian add*/)
		//
		);
	}
	
	public boolean isWorkspaceLocked()
	{
		return mWorkspaceLoading || mWaitingForResult || isWaitingForUninstall();
	}
	
	private void resetAddInfo()
	{
		mPendingAddInfo.setContainer( ItemInfo.NO_ID );
		mPendingAddInfo.setScreenId( -1 );
		//xiatian start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“进入编辑模式的小组件界面添加插件后，再进入桌面菜单的小组件添加插件，这时添加到桌面的插件宽高都减一”的问题。【i_0014856】）
		//xiatian del start
		//		int mCellX = mPendingAddInfo.getCellY() - 1;
		//		int mSpanX = mPendingAddInfo.getSpanY() - 1;
		//		int mMinSpanX = mPendingAddInfo.getMinSpanY() - 1;
		//xiatian del end
		//xiatian add start
		int mCellX = -1;
		int mSpanX = 0;
		int mMinSpanX = 0;
		//xiatian add end
		//xiatian end
		mPendingAddInfo.setCellX( mCellX );
		mPendingAddInfo.setSpanX( mSpanX );
		mPendingAddInfo.setMinSpanX( mMinSpanX );
		mPendingAddInfo.setDropPos( null );
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“先编辑模式的小组件页面拖动一个插件A到任一页面a,再桌面菜单的小组件界面添加一个高度小于A的插件B到任一页面b,最后长按插件B，将插件B托起，移动到任一没有足够空间放下插件B的页面c，然后放下插件B，此时桌面重启”的问题。【i_0014861】）
		//【问题原因】
		//	1、由于编辑模式的小组件页面拖动一个插件A到任一页面a时，resetAddInfo方法中没有设置mPendingAddInfo的mSpanY
		//	2、在桌面菜单的小组件界面添加一个高度小于A的插件B到任一页面b时，completeAddAppWidget方法中由于mPendingAddInfo的mMinSpanY不为0，导致将插件B的MinSpanY设置为mPendingAddInfo的mMinSpanY
		//	3、在长按插件B，将插件B托起，移动到任一没有足够空间放下插件B的页面c，然后放下插件B时，在workspace.java的onDrop方法中调用Celllayout.java的createArea方法，然后调用findConfigurationNoShuffle，在findConfigurationNoShuffle调用findNearestVacantArea时，传递的参数minSpanY出错，导致Celllayout.java的createArea方法返回值错误
		//	4、由于步骤3，导致LauncherModel.modifyItemInDatabase中错误设置了LauncherAppWidgetInfo的mSpanX=0和mSpanY=0
		//	5、接下来Workapce.java的onDrop方法调用animateWidgetDrop方法，当Workapce.java的animateWidgetDrop方法调用createWidgetBitmap方法时，由于步骤4设置mSpanX和mSpanY都为0，导致createWidgetBitmap中计算图片的宽高为0，故而生成bitmap时抛异常，最终桌面重启。
		int mCellY = -1;
		int mSpanY = 0;
		int mMinSpanY = 0;
		mPendingAddInfo.setCellY( mCellY );
		mPendingAddInfo.setSpanY( mSpanY );
		mPendingAddInfo.setMinSpanY( mMinSpanY );
		//xiatian add end
	}
	
	void addAppWidgetImpl(
			final int appWidgetId ,
			ItemInfo info ,
			AppWidgetHostView boundWidget ,
			AppWidgetProviderInfo appWidgetInfo )
	{
		// gaominghui@2016/12/14 ADD START
		if( appWidgetInfo == null )
		{
			appWidgetInfo = mAppWidgetManager.getAppWidgetInfo( appWidgetId );
		}
		// gaominghui@2016/12/14 ADD END
		if( appWidgetInfo.configure != null )
		{
			mPendingAddWidgetInfo = appWidgetInfo;
			// Launch over to configure widget, if needed
			Intent intent = new Intent( AppWidgetManager.ACTION_APPWIDGET_CONFIGURE );
			intent.setComponent( appWidgetInfo.configure );
			intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , appWidgetId );
			Utilities.startActivityForResultSafely( this , intent , REQUEST_CREATE_APPWIDGET );
		}
		else
		{
			// Otherwise just add it
			completeAddAppWidget( appWidgetId , info.getContainer() , info.getScreenId() , boundWidget , appWidgetInfo );
			// Exit spring loaded mode if necessary after adding the widget
			exitSpringLoadedDragModeDelayed( true , false , null );
		}
	}
	
	protected void moveToFavoritesPageScreen(
			boolean animate )
	{
		// Close any folders that may be open.
		closeFolder();
		mWorkspace.moveToFavoritesPage( animate );
	}
	
	/**
	 * Process a shortcut drop.
	 *
	 * @param componentName The name of the component
	 * @param screenId The ID of the screen where it should be added
	 * @param cell The cell it should be added to, optional
	 * @param position The location on the screen where it was dropped, optional
	 */
	void processShortcutFromDrop(
			ComponentName componentName ,
			long container ,
			long screenId ,
			int[] cell ,
			int[] loc )
	{
		resetAddInfo();
		mPendingAddInfo.setContainer( container );
		mPendingAddInfo.setScreenId( screenId );
		mPendingAddInfo.setDropPos( loc );
		if( cell != null )
		{
			mPendingAddInfo.setCellX( cell[0] );
			mPendingAddInfo.setCellY( cell[1] );
		}
		Intent createShortcutIntent = new Intent( Intent.ACTION_CREATE_SHORTCUT );
		createShortcutIntent.setComponent( componentName );
		processShortcut( createShortcutIntent );
	}
	
	/**
	 * Process a widget drop.
	 *
	 * @param info The PendingAppWidgetInfo of the widget being added.
	 * @param screenId The ID of the screen where it should be added
	 * @param cell The cell it should be added to, optional
	 * @param position The location on the screen where it was dropped, optional
	 */
	void addAppWidgetFromDrop(
			PendingAddWidgetInfo info ,
			long container ,
			long screenId ,
			int[] cell ,
			int[] span ,
			int[] loc )
	{
		resetAddInfo();
		info.setContainer( container );
		info.setScreenId( screenId );
		mPendingAddInfo.setContainer( container );
		mPendingAddInfo.setScreenId( screenId );
		mPendingAddInfo.setDropPos( loc );
		mPendingAddInfo.setMinSpanX( info.getMinSpanX() );
		mPendingAddInfo.setMinSpanY( info.getMinSpanY() );
		if( cell != null )
		{
			mPendingAddInfo.setCellX( cell[0] );
			mPendingAddInfo.setCellY( cell[1] );
		}
		if( span != null )
		{
			mPendingAddInfo.setSpanX( span[0] );
			mPendingAddInfo.setSpanY( span[1] );
		}
		AppWidgetHostView hostView = info.getAppWidgetHostView();
		int appWidgetId;
		if( hostView != null )
		{
			appWidgetId = hostView.getAppWidgetId();
			addAppWidgetImpl( appWidgetId , info , hostView , info.getAppWidgetProviderInfo() );
		}
		else
		{
			// In this case, we either need to start an activity to get permission to bind
			// the widget, or we need to start an activity to configure the widget, or both.
			appWidgetId = getAppWidgetHost().allocateAppWidgetId();
			Bundle options = info.getBindOptions();
			boolean success = false;
			if( options != null )
			{
				// gaominghui@2016/12/14 ADD START
				if( Build.VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN )
					success = mAppWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , info.getComponentName() , options );
				else if( Build.VERSION.SDK_INT == VERSION_CODES.JELLY_BEAN )
					success = mAppWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , info.getComponentName() );
				else
				{
					success = Tools.bindAppWidgetId( mAppWidgetManager , appWidgetId , info.getComponentName() );
				}
				// gaominghui@2016/12/14 ADD END
			}
			else
			{
				// gaominghui@2016/12/14 ADD START
				if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
					success = mAppWidgetManager.bindAppWidgetIdIfAllowed( appWidgetId , info.getComponentName() );
				else
				{
					Tools.bindAppWidgetId( mAppWidgetManager , appWidgetId , info.getComponentName() );
					success = true;
				}
				// gaominghui@2016/12/14 ADD END
			}
			if( success )
			{
				addAppWidgetImpl( appWidgetId , info , null , info.getAppWidgetProviderInfo() );
			}
			else
			{
				mPendingAddWidgetInfo = info.getAppWidgetProviderInfo();
				// gaominghui@2016/12/14 ADD START 兼容android4.0
				if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
				{
					Intent intent = new Intent( AppWidgetManager.ACTION_APPWIDGET_BIND );
					intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , appWidgetId );
					intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_PROVIDER , info.getComponentName() );
					// TODO: we need to make sure that this accounts for the options bundle.
					// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
					startActivityForResult( intent , REQUEST_BIND_APPWIDGET );
				}
				// gaominghui@2016/12/14 ADD ENDs 兼容android4.0
			}
		}
	}
	
	void processShortcut(
			Intent intent )
	{
		// Handle case where user selected "Applications"
		String applicationName = LauncherDefaultConfig.getString( R.string.group_applications );
		String shortcutName = intent.getStringExtra( Intent.EXTRA_SHORTCUT_NAME );
		if( applicationName != null && applicationName.equals( shortcutName ) )
		{
			Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
			mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
			Intent pickIntent = new Intent( Intent.ACTION_PICK_ACTIVITY );
			pickIntent.putExtra( Intent.EXTRA_INTENT , mainIntent );
			pickIntent.putExtra( Intent.EXTRA_TITLE , getText( R.string.title_select_application ) );
			Utilities.startActivityForResultSafely( this , pickIntent , REQUEST_PICK_APPLICATION );
		}
		else
		{
			Utilities.startActivityForResultSafely( this , intent , REQUEST_CREATE_SHORTCUT );
		}
	}
	
	FolderIcon addFolder(
			CellLayout layout ,
			long container ,
			final long screenId ,
			int cellX ,
			int cellY )
	{
		final FolderInfo folderInfo = new FolderInfo();
		//xiatian start	//需求：新建的文件夹名称由显示为空白改为显示为“文件夹”。
		//		folderInfo.title = getText( R.string.folder_name );//xiatian del
		folderInfo.setTitle( getText( R.string.folder_name ).toString() );//xiatian add
		//xiatian end
		// Update the model
		LauncherModel.addItemToDatabase( Launcher.this , folderInfo , container , screenId , cellX , cellY , false );
		sFolders.put( folderInfo.getId() , folderInfo );
		// Create the view
		FolderIcon newFolder = FolderIcon.fromXml( R.layout.folder_icon , this , layout , folderInfo , mIconCache );
		mWorkspace.addInScreen( newFolder , container , screenId , cellX , cellY , 1 , 1 , isWorkspaceLocked() );
		// Force measure the new folder icon
		CellLayout parent = mWorkspace.getParentCellLayoutForView( newFolder );
		parent.getShortcutsAndWidgets().measureChild( newFolder );
		return newFolder;
	}
	
	public void removeFolder(
			FolderInfo folder )
	{
		sFolders.remove( folder.getId() );
	}
	
	/**
	 * Registers various content observers. The current implementation registers
	 * only a favorites observer to keep track of the favorites applications.
	 */
	private void registerContentObservers()
	{
		ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver( LauncherProvider.CONTENT_APPWIDGET_RESET_URI , true , mWidgetObserver );
	}
	
	@Override
	public boolean dispatchKeyEvent(
			KeyEvent event )
	{
		//xiatian add start	//通知桌面切页：“德盛伟业”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerDSWY.java”中的备注。
		NotifyLauncherSnapPageManagerCustomerDSWY mNotifyLauncherSnapPageManagerCustomerDSWY = NotifyLauncherSnapPageManagerCustomerDSWY.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerDSWY != null )
		{
			if(
			//
			mNotifyLauncherSnapPageManagerCustomerDSWY.isSnapToLeft( event )
			//
			|| mNotifyLauncherSnapPageManagerCustomerDSWY.isSnapToRight( event )
			//
			)
			{
				return true;
			}
		}
		//xiatian add end
		//xiatian add start	//通知桌面切页：“讯虎”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerCustomerXH.java”中的备注。
		NotifyLauncherSnapPageManagerCustomerXH mNotifyLauncherSnapPageManagerCustomerXH = NotifyLauncherSnapPageManagerCustomerXH.getInstance();
		if( mNotifyLauncherSnapPageManagerCustomerXH != null )
		{
			if( mNotifyLauncherSnapPageManagerCustomerXH.isSnapToRight( new Object[]{ event , this } ) )
			{
				return true;
			}
		}
		//xiatian add end
		if( event.getAction() == KeyEvent.ACTION_DOWN )
		{
			switch( event.getKeyCode() )
			{
				case KeyEvent.KEYCODE_HOME:
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					// gaominghui@2016/11/23 ADD START
					//	0004553: phenix， 双层桌面，待机界面和主菜单界面按音量下键不会弹出音量条，在原生桌面和其它的界面都会弹出音量条
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						if( isPropertyEnabled( DUMP_STATE_PROPERTY ) )
						{
							dumpState();
							return true;
						}
					}
					// gaominghui@2016/11/23 ADD END
					break;
			}
		}
		else if( event.getAction() == KeyEvent.ACTION_UP )
		{
			switch( event.getKeyCode() )
			{
				case KeyEvent.KEYCODE_HOME:
					return true;
			}
			//xiatian add start	//需求：支持某些特定按键触发桌面切页。
			if( LauncherDefaultConfig.isNeedSnapToLeft( event ) )
			{
				notifyLauncherSnapToLeft();
			}
			else if( LauncherDefaultConfig.isNeedSnapToRight( event ) )
			{
				notifyLauncherSnapToRight();
			}
			//xiatian add end
		}
		//cheyingkun add start	//编辑模式底部按键支持按键(逻辑优化)
		if( mWorkspace.isInOverviewMode() )
		{
			checkOverviewPanelButtonsFocus();
		}
		//cheyingkun add end
		return super.dispatchKeyEvent( event );
	}
	
	/**
	 * 取消mStateAnimation动画
	 */
	private void cancelStateAnimation()
	{
		if( mStateAnimation != null )
		{
			mStateAnimation.setDuration( 0 );
			mStateAnimation.cancel();
			mStateAnimation = null;
		}
	}
	
	@Override
	public void onBackPressed()
	{
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		if(
		//
		( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_WORKSPACE_MENU_VERTICAL_LIST )
		//
		&& ( mWorkspaceMenuVerticalList != null )
		//
		&& ( mWorkspaceMenuVerticalList.isAnimationRuning() || mWorkspaceMenuVerticalList.getVisibility() == View.VISIBLE ) )
		{
			if( mWorkspaceMenuVerticalList.isAnimationRuning() )
			{//动画正在播放时：不响应返回键。
			}
			else if( mWorkspaceMenuVerticalList.getVisibility() == View.VISIBLE )
			{//动画播放完毕，并且menu处于显示状态：隐藏menu。
				mWorkspaceMenuVerticalList.hideWithAnim();
			}
			return;
		}
		else
		//xiatian add end
		if( isAllAppsVisible() )
		{
			//zhujieping add start,主菜单处理返回键，如果主菜单处理了不需要桌面处理就返回true，否则返回false
			if( mAppsCustomizeTabHost.onBackPressed() )
			{
				return;
			}
			//zhujieping add end
			if( mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Applications )
			{
				showWorkspace( true );
			}
			else
			{
				//xiatian start	//fix bug：解决“双层模式时，在打开开关'SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST'的前提下，进入主菜单并切换到小组件tab后，点击返回键回到的是编辑模式”的问题。
				//				showOverviewMode( true );//xiatian del
				//xiatian add start
				if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_TAB )
				{
					int mTabsContainerVisibility = mAppsCustomizeTabHost.getTabsContainerVisibility();
					if( mTabsContainerVisibility == View.GONE )
					{
						showOverviewMode( true );
					}
					else
					{
						showWorkspace( true );
					}
				}
				else
				{
					showOverviewMode( true );
				}
				//xiatian add end
				//xiatian end
			}
		}
		else if( mWorkspace.isInOverviewMode() )
		{
			//zhujieping add start,二级界面处理，则不退出编辑模式
			if( mEditModeEntity != null )
			{
				if( mEditModeEntity.onBackPressed( mOverviewPanel ) )
				{
					return;
				}
			}
			//zhujieping add end
			// 当退出编辑模式的时候，若mStateAnimation(小部件hideapp动画)仍然还在执行，则此时先置位动画，再取消动画 wanghongjian@2015/04/01 ADD START
			if( mStateAnimation != null && mStateAnimation.isRunning() )
			{
				cancelStateAnimation();
			}
			// wanghongjian@2015/04/01 ADD END
			exitEditMode( true );
		}
		else if( mWorkspace.getOpenFolder() != null )
		{
			Folder openFolder = mWorkspace.getOpenFolder();
			if( openFolder.isEditingName() )
			{
				openFolder.dismissEditingName();
			}
			else
			{
				closeFolder();
			}
		}
		else
		{
			mWorkspace.exitWidgetResizeMode();
			// Back button is a no-op here, but give at least some feedback for the button press
			mWorkspace.showOutlinesTemporarily();
		}
		if( mWorkspace.hasFavoritesPage() )
		{
			FavoritesPageManager.getInstance( this ).onBackPressed();//cheyingkun add	//phenix1.1稳定版移植酷生活
			if( mWorkspace.isFavoritesPageByPageIndex( mWorkspace.getCurrentPage() ) )
			{//back键，返回默认页
				mWorkspace.moveToDefaultScreen( true );
			}
		}
	}
	
	/**
	 * Re-listen when widgets are reset.
	 */
	private void onAppWidgetReset()
	{
		if( mAppWidgetHost != null )
		{
			mAppWidgetHost.startListening();
		}
	}
	
	/**
	 * Launches the intent referred by the clicked shortcut.
	 *
	 * @param v The view representing the clicked shortcut.
	 */
	public void onClick(
			View v )
	{
		// Make sure that rogue clicks don't get through while allapps is launching, or after the
		// view has detached (it's possible for this to happen if the view is removed mid touch).
		if( v.getWindowToken() == null )
		{
			return;
		}
		if( !mWorkspace.isFinishedSwitchingState() )
		{
			return;
		}
		if( isWaitingForUninstall() )
		{//等待卸载反馈时，不响应点击事件
			return;
		}
		if( v instanceof Workspace )
		{
			if( !LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE ) //gaominghui add //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
			{
				if( mWorkspace.isInOverviewMode() )
				{
					exitEditMode( true );
				}
				return;
			}
		}
		if( v instanceof CellLayout )
		{
			if( mWorkspace.isInOverviewMode() )
			{
				mWorkspace.exitOverviewMode( mWorkspace.indexOfChild( v ) , true );
			}
			//zhujieping add start 
			if( mEditModeEntity != null )
			{
				mEditModeEntity.exitSecondaryEditMode( mOverviewPanel );
			}
			//zhujieping add end
		}
		Object tag = v.getTag();
		if( tag instanceof ShortcutInfo )
		{
			// Open shortcut
			final ShortcutInfo shortcut = (ShortcutInfo)tag;
			final Intent intent = shortcut.getIntent();
			// Check for special shortcuts
			if( intent != null && intent.getComponent() != null )
			{
				final String shortcutClass = intent.getComponent().getClassName();
				if( shortcutClass.equals( WidgetAdder.class.getName() ) )
				{
					showAllApps( true , AppsCustomizePagedView.ContentType.Widgets , true );
					return;
				}
				else if( shortcutClass.equals( MemoryDumpActivity.class.getName() ) )
				{
					MemoryDumpActivity.startDump( this );
					return;
				}
				else if( shortcutClass.equals( ToggleWeightWatcher.class.getName() ) )
				{
					toggleShowWeightWatcher();
					return;
				}
			}
			if( mOperateDynamicMain != null )
			{
				boolean isHide = mOperateDynamicMain.hideOperateFolderHot( shortcut );
				if( isHide )
					v.invalidate();
			}
			// Start activities
			int[] pos = new int[2];
			v.getLocationOnScreen( pos );
			if( intent != null )
			{
				intent.setSourceBounds( new Rect( pos[0] , pos[1] , pos[0] + v.getWidth() , pos[1] + v.getHeight() ) );
				LauncherAppState app = LauncherAppState.getInstance();
				DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
				intent.putExtra( "launcherIconSizePx" , grid.getIconHeightSizePx() );
				intent.putExtra( "paddingTop" , v.getPaddingTop() );
			}
			boolean success = startActivitySafely( v , intent , tag );
			//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
			if( ( ( //
			tag instanceof EnhanceItemInfo //
					&& ( (EnhanceItemInfo)tag ) != null //
			&& ( (EnhanceItemInfo)tag ).getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL ) //
			|| ( tag instanceof ShortcutInfo && ( (ShortcutInfo)tag ).isOperateIconItem() ) ) && v instanceof BubbleTextView //
			)
			{
				if( LauncherAppState.isApkInstalled( intent.getComponent() ) == false || LauncherAppState.isApkInstalled( intent.getStringExtra( OperateDynamicMain.PKGNAME_ID ) ) == false )
				{
					success = false;//当item是虚图标并且应用没有安装时，不需要mWaitingForResume
				}
			}
			//xiatian add end
			mStats.recordLaunch( intent , shortcut );
			if( success && v instanceof BubbleTextView )
			{
				mWaitingForResume = (BubbleTextView)v;
				mWaitingForResume.setStayPressed( true );
			}
		}
		else if( tag instanceof FolderInfo )
		{
			if( v instanceof FolderIcon )
			{
				FolderIcon fi = (FolderIcon)v;
				handleFolderClick( fi );
			}
		}
		// zhangjin@2016/05/25 ADD START
		else if( tag instanceof AppInfo )
		{
			startAppShortcutOrInfoActivity( v );
		}
		// zhangjin@2016/05/25 ADD END
	}
	
	public boolean onTouch(
			View v ,
			MotionEvent event )
	{
		return false;
	}
	
	/**
	 * Event handler for the search button
	 *
	 * @param v The view that was clicked.
	 */
	public void onClickSearchButton(
			View v )
	{
		// jubingcheng@2016/10/19 ADD START 解决双击搜索栏后搜索显示桌面界面的问题
		if( mPaused )
			return;
		// jubingcheng@2016/10/19 ADD END
		// zhujieping@2015/03/24 ADD START
		//startdrag或者enddrag时，mSearchBar执行动画时设置LayerType为LAYER_TYPE_HARDWARE，动画结束置为LAYER_TYPE_NONE，或者alpha动画时不响应点击事件【i_0010659】
		// zhangjin@2016/05/10 UPD START
		//if( mSearchBar != null && ( mSearchBar.getLayerType() == View.LAYER_TYPE_HARDWARE || mSearchBar.getAlpha() < 1 ) )
		//{
		//	return;
		//}
		// zhangjin@2016/05/06 UPD START
		//showAllApps( true , AppsCustomizePagedView.ContentType.Applications , true );
		if(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		{
			if( ( mState == State.WORKSPACE ) && ( mSearchBar != null && ( mSearchBar.getLayerType() == View.LAYER_TYPE_HARDWARE || mSearchBar.getAlpha() < 1 ) ) )
			{
				return;
			}
		}
		// zhangjin@2016/05/10 UPD END
		// zhujieping@2015/03/24 ADD END
		v.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );
		onSearchRequested();
	}
	
	/**
	 * Event handler for the voice button
	 *
	 * @param v The view that was clicked.
	 */
	public void onClickVoiceButton(
			View v )
	{
		// jubingcheng@2016/10/19 ADD START 解决双击搜索栏后搜索显示桌面界面的问题
		if( mPaused )
			return;
		// jubingcheng@2016/10/19 ADD END
		// zhujieping@2015/03/24 ADD START
		//startdrag或者enddrag时，mSearchBar执行动画时设置LayerType为LAYER_TYPE_HARDWARE，动画结束置为LAYER_TYPE_NONE，或者alpha动画时不响应点击事件【i_0010659】
		if( mSearchBar != null && ( mSearchBar.getLayerType() == View.LAYER_TYPE_HARDWARE || mSearchBar.getAlpha() < 1 ) )
		{
			return;
		}
		// zhujieping@2015/03/24 ADD END
		v.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );
		if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE )
		{//使用酷搜时，点击语音搜索按钮后打开酷搜
			onClickSearchButton( v );
		}
		else
		{
			startVoice();
		}
	}
	
	public void startVoice()
	{
		try
		{
			final SearchManager searchManager = (SearchManager)getSystemService( Context.SEARCH_SERVICE );
			ComponentName activityName = null;
			// gaominghui@2016/12/14 ADD START
			//activityName = searchManager.getGlobalSearchActivity();
			activityName = getGlobalSearchActivity( searchManager );
			// gaominghui@2016/12/14 ADD END
			Intent intent = new Intent( RecognizerIntent.ACTION_WEB_SEARCH );
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			//WangLei start //bug:c_0002924  //当全局搜索和语音搜索不是同一个应用时，点击搜索框的语音搜索图标，提示"未安装应用"
			//【原因】在启动语音搜索的Activity时，使用的是全局搜索的包名，当全局搜索和语音搜索不是同一个应用提供的功能，启动Activity时找不到对应的ComponentName从而提示"未安装应用"
			//【解决方案】在启动语音搜索功能时，根据global search activity是否可以处理voice search来获取语音搜索实际的ComponentName
			//WangLei del start
			//  if( activityName != null )
			//  {
			//     intent.setPackage( activityName.getPackageName() );			
			//  }
			//WangLei del end
			//WangLei add start
			ComponentName voiceActivityName = null;
			if( activityName != null )
			{
				// Check if the global search activity handles voice search
				Intent voiceIntent = new Intent( RecognizerIntent.ACTION_WEB_SEARCH );
				voiceIntent.setPackage( activityName.getPackageName() );
				voiceActivityName = intent.resolveActivity( getPackageManager() );
			}
			if( voiceActivityName == null )
			{
				// Fallback: check if an activity other than the global search activity
				// resolves this
				Intent voiceIntent = new Intent( RecognizerIntent.ACTION_WEB_SEARCH );
				voiceActivityName = voiceIntent.resolveActivity( getPackageManager() );
			}
			if( voiceActivityName != null )
			{
				intent.setComponent( voiceActivityName );
			}
			else
			{
				if( activityName != null )
				{
					intent.setPackage( activityName.getPackageName() );
				}
			}
			//WangLei add end 
			//WangLei end
			startActivity( null , intent , "onClickVoiceButton" );
		}
		catch( ActivityNotFoundException e )
		{
			Intent intent = new Intent( RecognizerIntent.ACTION_WEB_SEARCH );
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			startActivitySafely( null , intent , "onClickVoiceButton" );
		}
	}
	
	// gaominghui@2016/12/14 ADD START
	public ComponentName getGlobalSearchActivity(
			SearchManager manager )
	{
		ComponentName globalSearchActivity = null;
		if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
		{
			globalSearchActivity = manager.getGlobalSearchActivity();
		}
		else
		{
			List<SearchableInfo> list = manager.getSearchablesInGlobalSearch();
			if( list != null && list.size() > 0 )
			{
				globalSearchActivity = list.get( 0 ).getSearchActivity();
			}
		}
		return globalSearchActivity;
	}
	
	// gaominghui@2016/12/14 ADD END
	/**
	 * Event handler for the "grid" button that appears on the home screen, which
	 * enters all apps mode.
	 *
	 * @param v The view that was clicked.
	 */
	public void onClickAllAppsButton(
			View v )
	{
		// zhangjin@2016/05/06 UPD START
		//showAllApps( true , AppsCustomizePagedView.ContentType.Applications , true );
		if(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
				|| LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT//zhujieping add //7.0进入主菜单动画改成也支持4.4主菜单样式 //zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		)
		{
			showAppsView( true /* animated */, true /* resetListToTop */, true /* updatePredictedApps */, false /* focusSearchBar */, AppsCustomizePagedView.ContentType.Applications );
		}
		else
		{
			showAllApps( true , AppsCustomizePagedView.ContentType.Applications , true );
		}
		// zhangjin@2016/05/06 UPD END
	}
	
	public void onTouchDownAllAppsButton(
			View v )
	{
		// Provide the same haptic feedback that the system offers for virtual keys.
		v.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );
	}
	
	public void performHapticFeedbackOnTouchDown(
			View v )
	{
		// Provide the same haptic feedback that the system offers for virtual keys.
		v.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );
	}
	
	public View.OnTouchListener getHapticFeedbackTouchListener()
	{
		if( mHapticFeedbackTouchListener == null )
		{
			mHapticFeedbackTouchListener = new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(
						View v ,
						MotionEvent event )
				{
					//chenliang add start	//解决“编辑模式下，打开小部件界面后返回，在小部件界面动画结束前，点击底边栏按钮，这时按钮会有点击反馈（动画未结束的时候，不响应点击按钮，故按钮不应该有点击反馈）”的问题。【i_0014917】
					if( mStateAnimation != null && mStateAnimation.isRunning() )
					{
						return true;
					}
					//chenliang add end
					if( ( event.getAction() & MotionEvent.ACTION_MASK ) == MotionEvent.ACTION_DOWN )
					{
						v.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );
					}
					return false;
				}
			};
		}
		return mHapticFeedbackTouchListener;
	}
	
	public void onClickAppMarketButton(
			View v )
	{
		if( SHOW_MARKET_BUTTON )
		{
			if( mAppMarketIntent != null )
			{
				startActivitySafely( v , mAppMarketIntent , "app market" );
			}
			else
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , "Invalid app market intent." );
			}
		}
	}
	
	/**
	 * Called when the user stops interacting with the launcher.
	 * This implies that the user is now on the homescreen and is not doing housekeeping.
	 */
	protected void onInteractionEnd()
	{
	}
	
	/**
	 * Called when the user starts interacting with the launcher.
	 * The possible interactions are:
	 *  - open all apps
	 *  - reorder an app shortcut, or a widget
	 *  - open the overview mode.
	 * This is a good time to stop doing things that only make sense
	 * when the user is on the homescreen and not doing housekeeping.
	 */
	protected void onInteractionBegin()
	{
	}
	
	void startApplicationDetailsActivity(
			ComponentName componentName )
	{
		String packageName = componentName.getPackageName();
		Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS , Uri.fromParts( "package" , packageName , null ) );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS );
		startActivitySafely( null , intent , "startApplicationDetailsActivity" );
	}
	
	// returns true if the activity was started
	public boolean startApplicationUninstallActivity(
			ComponentName componentName ,
			int flags )
	{
		if( ( flags & AppInfo.DOWNLOADED_FLAG ) == 0 )
		{
			// System applications cannot be installed. For now, show a toast explaining that.
			// We may give them the option of disabling apps this way.
			int messageId = R.string.uninstall_system_app_text;
			Toast.makeText( this , messageId , Toast.LENGTH_SHORT ).show();
			return false;
		}
		else
		{
			String packageName = componentName.getPackageName();
			String className = componentName.getClassName();
			Intent intent = new Intent( Intent.ACTION_DELETE , Uri.fromParts( "package" , packageName , className ) );
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS );
			startActivity( intent );
			return true;
		}
	}
	
	public boolean startActivity(
			View v ,
			Intent intent ,
			Object tag )
	{
		boolean mIsBrowserByPackageName = false;
		boolean mIsBrowserByCategory = false;
		//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
		boolean mIsUseOperateUri = false;
		String mBrowerPackageName = "";
		//xiatian add end
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		//xiatian add start	//fix bug：解决“点击桌面配置的‘美化中心’快捷方式（或者虚图标）进入美化中心后，美化中心中相关数据（默认主题，壁纸。。。）不正确”的问题。
		if( intent != null )
		{
			mIsBrowserByCategory = isBrowserByCategory( intent.getCategories() );
			//xiatian add start	//“运营浏览器主页”的功能：添加通过Category来判断点击的图标是否为浏览器的方法。
			if(
			//
			LauncherDefaultConfig.SWITCH_ENABLE_OPERATE_EXPLORER
			//
			&& TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE ) == false
			//
			&& mIsBrowserByCategory
			//
			)
			{
				//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
				mIsUseOperateUri = true;
				mBrowerPackageName = Intent.CATEGORY_BROWSABLE;
				//xiatian add end
				if( mBrowserIntentWithOperateHomeWebset == null )//xiatian add	//“运营浏览器主页”的功能：解决内存泄漏。
				{
					//xiatian start	//“运营浏览器主页”的功能：解决内存泄漏。
					//					Intent mBrowserIntentWithOperateHomeWebset = new Intent( Intent.ACTION_VIEW );//xiatian del
					mBrowserIntentWithOperateHomeWebset = new Intent( Intent.ACTION_VIEW );//xiatian add
					//xiatian end
					//xiatian start	//“运营浏览器主页”的功能：解决“由于某些浏览器的主界面没有打开指定uri的功能，从而打不开网页”的问题。
					//【备注】
					//	1、某些应用的主应用如搜狗搜索（com.sogou.activity.src/.SplashActivity）和360好搜（com.qihoo.haosou/.activity.SplashActivity）并不具备打开指定uri的功能，
					//	2、此时需要去启动该包名下具备此功能的activity如com.sogou.activity.src/SogouBrowser和com.qihoo.haosou/.activity.BrowserActivity
					//					mBrowserIntentWithOperateHomeWebset.addCategory( Intent.CATEGORY_DEFAULT );//xiatian del
					//xiatian end
				}
				//xiatian start	//“运营浏览器主页”的功能：解决“由于某些浏览器的主界面没有打开指定uri的功能，从而打不开网页”的问题。
				//【备注】
				//	1、某些应用的主应用如搜狗搜索（com.sogou.activity.src/.SplashActivity）和360好搜（com.qihoo.haosou/.activity.SplashActivity）并不具备打开指定uri的功能，
				//	2、此时需要去启动该包名下具备此功能的activity如com.sogou.activity.src/SogouBrowser和com.qihoo.haosou/.activity.BrowserActivity
				//				mBrowserIntentWithOperateHomeWebset.setComponent( null );//xiatian del
				mBrowserIntentWithOperateHomeWebset.setPackage( null );//xiatian add
				//xiatian end
				mBrowserIntentWithOperateHomeWebset.setData( Uri.parse( LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE ) );
				intent = mBrowserIntentWithOperateHomeWebset;
			}
			//xiatian add end
			ComponentName mComponentName = intent.getComponent();
			if( mComponentName != null )
			{
				String mPackageName = mComponentName.getPackageName();
				mIsBrowserByPackageName = isBrowserByPackageName( mPackageName );
				if( mPackageName.equals( ThemeManager.BEAUTY_CENTER_PACKAGE_NAME ) )
				{
					bindThemeActivityData( intent );
				}
				//xiatian add start	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
				else if(
				//
				LauncherDefaultConfig.SWITCH_ENABLE_OPERATE_EXPLORER
				//
				&& TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE ) == false
				//
				&& ( mIsBrowserByPackageName ) )
				{
					//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
					mBrowerPackageName = mPackageName;
					mIsUseOperateUri = true;
					//xiatian add end
					if( mBrowserIntentWithOperateHomeWebset == null )//xiatian add	//“运营浏览器主页”的功能：解决内存泄漏。
					{
						//xiatian start	//“运营浏览器主页”的功能：解决内存泄漏。
						//					Intent mBrowserIntentWithOperateHomeWebset = new Intent( Intent.ACTION_VIEW );//xiatian del
						mBrowserIntentWithOperateHomeWebset = new Intent( Intent.ACTION_VIEW );//xiatian add
						//xiatian end
						//xiatian start	//“运营浏览器主页”的功能：解决“由于某些浏览器的主界面没有打开指定uri的功能，从而打不开网页”的问题。
						//【备注】
						//	1、某些应用的主应用如搜狗搜索（com.sogou.activity.src/.SplashActivity）和360好搜（com.qihoo.haosou/.activity.SplashActivity）并不具备打开指定uri的功能，
						//	2、此时需要去启动该包名下具备此功能的activity如com.sogou.activity.src/SogouBrowser和com.qihoo.haosou/.activity.BrowserActivity
						//					mBrowserIntentWithOperateHomeWebset.addCategory( Intent.CATEGORY_DEFAULT );//xiatian del
						//xiatian end
					}
					//xiatian start	//“运营浏览器主页”的功能：解决“由于某些浏览器的主界面没有打开指定uri的功能，从而打不开网页”的问题。
					//【备注】
					//	1、某些应用的主应用如搜狗搜索（com.sogou.activity.src/.SplashActivity）和360好搜（com.qihoo.haosou/.activity.SplashActivity）并不具备打开指定uri的功能，
					//	2、此时需要去启动该包名下具备此功能的activity如com.sogou.activity.src/SogouBrowser和com.qihoo.haosou/.activity.BrowserActivity
					//				mBrowserIntentWithOperateHomeWebset.setComponent( mComponentName );//xiatian del
					mBrowserIntentWithOperateHomeWebset.setPackage( mPackageName );//xiatian add
					//xiatian end
					mBrowserIntentWithOperateHomeWebset.setData( Uri.parse( LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE ) );
					intent = mBrowserIntentWithOperateHomeWebset;
				}
				//xiatian add end
			}
		}
		//xiatian add end
		try
		{
			//cheyingkun add start	//添加友盟统计自定义事件(桌面设置图标)
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				ComponentName component = intent.getComponent();
				if( component != null && component.equals( new ComponentName( "com.cooee.phenix" , "com.cooee.phenix.launcherSettings.LauncherSettingsActivity" ) ) )
				{
					MobclickAgent.onEvent( this , UmengStatistics.ENTER_LAUNCHER_SETTING_BY_ICON );
				}
			}
			//cheyingkun add end
			//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
			if( mIsBrowserByPackageName || mIsBrowserByCategory )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
				{
					Map<String , String> map = new HashMap<String , String>();
					map.put( UmengStatistics.KEY_ENABLE_OPERATE , String.valueOf( mIsUseOperateUri ) );
					map.put( UmengStatistics.KEY_BROWSER_PACKAGE , mBrowerPackageName );
					MobclickAgent.onEvent( this , UmengStatistics.BROWSER_BOOT_TIMES , map );
				}
				JSONObject obj = new JSONObject();
				try
				{
					obj.put( "param1" , StringUtils.concat( UmengStatistics.KEY_ENABLE_OPERATE , ":" , mIsUseOperateUri ) );
					obj.put( "param2" , StringUtils.concat( UmengStatistics.KEY_BROWSER_PACKAGE , ":" , mBrowerPackageName ) );
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
				StatisticsExpandNew.onCustomEvent(
						this ,
						UmengStatistics.BROWSER_BOOT_TIMES ,
						LauncherConfigUtils.getSN( this ) ,
						LauncherConfigUtils.getAppID( this ) ,
						LauncherConfigUtils.cooeeGetCooeeId( this ) ,
						4 ,
						getPackageName() ,
						String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) ,
						obj );
			}
			//xiatian add end
			// Only launch using the new animation if the shortcut has not opted out (this is a
			// private contract between launcher and may be ignored in the future).
			boolean useLaunchAnimation = ( v != null ) && !intent.hasExtra( INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION );
			if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN && useLaunchAnimation )
			{
				ActivityOptions opts = ActivityOptions.makeScaleUpAnimation( v , 0 , 0 , v.getMeasuredWidth() , v.getMeasuredHeight() );
				startActivity( intent , opts.toBundle() );
			}
			else
			{
				startActivity( intent );
			}
			return true;
		}
		catch( SecurityException e )
		{
			Toast.makeText( this , R.string.activity_not_found , Toast.LENGTH_SHORT ).show();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat(
						"Launcher does not have the permission to launch " ,
						intent.toUri( 0 ) ,
						". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity. " ,
						"tag=" ,
						tag ) , e );
		}
		return false;
	}
	
	public boolean startActivitySafely(
			View v ,
			Intent intent ,
			Object tag )
	{
		//添加智能分类功能 , change by shlt@2015/02/27 START
		//添加智能分类功能 , change by shlt@2015/02/27 DEL START
		//boolean success = false;
		//try
		//{
		//	success = startActivity( v , intent , tag );
		//}
		//catch( ActivityNotFoundException e )
		//{
		//	Toast.makeText( this , R.string.activity_not_found , Toast.LENGTH_SHORT ).show();
		//	Log.e( TAG , "Unable to launch. tag=" + tag + " intent=" + intent , e );
		//}
		//return success;
		//添加智能分类功能 , change by shlt@2015/02/27 DEL END
		// 【内部需求】添加 给2345天气客户端带来的激活，活跃olap统计（2345天气客户端包名com.tianqiwhite）
		if( intent.getComponent() != null && intent.getComponent().getPackageName() != null && intent.getComponent().getPackageName().equals( "com.tianqiwhite" ) && intent.getComponent()
				.getClassName() != null && intent.getComponent().getClassName().equals( "com.tianqiyubao2345.activity.CoveryActivity" ) )
		{
			if( prefs.getBoolean( "2345weather_first_run" , true ) )
			{
				StatisticsExpandNew.register( this , LauncherConfigUtils.getSN( this ) , LauncherConfigUtils.getAppID( this ) , LauncherConfigUtils.cooeeGetCooeeId( this ) , 4 , intent.getComponent()
						.getPackageName() , String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) );
				prefs.edit().putBoolean( "2345weather_first_run" , false ).apply();
			}
			else
			{
				StatisticsExpandNew.use( this , LauncherConfigUtils.getSN( this ) , LauncherConfigUtils.getAppID( this ) , LauncherConfigUtils.cooeeGetCooeeId( this ) , 4 , intent.getComponent()
						.getPackageName() , String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) );
			}
		}
		// 【内部需求】 添加给2345天气客户端带来的激活，活跃olap统计
		// @gaominghui 2016/02/22 ADD START Kpsh需要添加的代码
		// for launcher 启动任意app的同时通知KPSHService，启动应用程序的包名，是否内置
		notifyKpshService( tag );
		// for launcher 启动任意app的同时通知KPSHService，启动应用程序的包名，是否内置
		// @gaominghui2016/02/22 ADD END Kpsh需要添加的代码
		if(
		//
		(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_SORT_APP )//zhujieping add 
		)
		//
		&& intent != null
		//
		&& intent.getComponent() != null
		//
		)//主菜单有menu菜单，支持按使用频率排序，才去记录
		{
			Message msg = new Message();
			msg.what = EVENT_ADD_APP_USE_FREQUENCY;
			msg.obj = StringUtils.concat( "FREQUENCY:" , intent.getComponent().toString() );
			mHandler.sendMessageDelayed( msg , 2000 );//延时，防止影响应用启动速度
		}
		return ActivityUtils.startActivitySafely( this , v , intent , tag );//添加智能分类功能 , change by shlt@2015/02/27 ADD
		//添加智能分类功能 , change by shlt@2015/02/27 END
	}
	
	// @gaominghui2016/02/22 ADD START Kpsh需要添加的方法
	private void notifyKpshService(
			final Object tag )
	{
		final Context context = this;
		new Thread() {
			
			@Override
			public void run()
			{
				try
				{
					if( tag instanceof ShortcutInfo )
					{
						ShortcutInfo sInfo = (ShortcutInfo)tag;
						if( sInfo.getIntent() != null && sInfo.getIntent().getComponent() != null )
						{
							String title = sInfo.getTitle();
							String pkgName = null;
							pkgName = sInfo.getIntent().getComponent().getPackageName();
							int isBuildIn = 0;
							if( ( sInfo.getFlags() & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0 )
							{
								isBuildIn = 1;
							}
							if( pkgName != null )
							{
								KpshSdk.setTopApp( context , pkgName , isBuildIn , title );
							}
						}
					}
				}
				catch( Exception e )
				{
				}
			}
		}.start();
	}
	
	// @gaominghui 2016/02/22 ADD END Kpsh需要添加的方法
	private void handleFolderClick(
			FolderIcon folderIcon )
	{
		final FolderInfo info = folderIcon.getFolderInfo();
		Folder openFolder = mWorkspace.getFolderForTag( info );
		// If the folder info reports that the associated folder is open, then verify that
		// it is actually opened. There have been a few instances where this gets out of sync.
		if( info.getOpened() && openFolder == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d(
						TAG ,
						StringUtils.concat( "Folder info marked as open, but associated folder is not open. Screen: " , info.getScreenId() , " (" , info.getCellX() , ", " , info.getCellY() , ")" ) );
			info.setOpened( false );
		}
		if( !info.getOpened() && !folderIcon.getFolder().isDestroyed() )
		{
			// Close any open folder
			closeFolder();
			// Open the requested folder
			openFolder( folderIcon );
		}
		else
		{
			// Find the open folder...
			int folderScreen;
			if( openFolder != null )
			{
				folderScreen = mWorkspace.getPageForView( openFolder );
				// .. and close it
				closeFolder( openFolder );
				if( folderScreen != mWorkspace.getCurrentPage() )
				{
					// Close any folder open on the current screen
					closeFolder();
					// Pull the folder onto this screen
					openFolder( folderIcon );
				}
			}
		}
	}
	
	/**
	 * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
	 * in the DragLayer in the exact absolute location of the original FolderIcon.
	 */
	private void copyFolderIconToImage(
			FolderIcon fi )
	{
		final int width = fi.getMeasuredWidth();
		final int height = fi.getMeasuredHeight();
		// Lazy load ImageView, Bitmap and Canvas
		if( mFolderIconImageView == null )
		{
			mFolderIconImageView = new ImageView( this );
		}
		if( mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width || mFolderIconBitmap.getHeight() != height )
		{
			mFolderIconBitmap = Bitmap.createBitmap( width , height , Bitmap.Config.ARGB_8888 );
			mFolderIconCanvas = new Canvas( mFolderIconBitmap );
		}
		DragLayer.LayoutParams lp;
		if( mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams )
		{
			lp = (DragLayer.LayoutParams)mFolderIconImageView.getLayoutParams();
		}
		else
		{
			lp = new DragLayer.LayoutParams( width , height );
		}
		// The layout from which the folder is being opened may be scaled, adjust the starting
		// view size by this scale factor.
		float scale = mDragLayer.getDescendantRectRelativeToSelf( fi , mRectForFolderAnimation );
		lp.customPosition = true;
		lp.x = mRectForFolderAnimation.left;
		lp.y = mRectForFolderAnimation.top;
		lp.width = (int)( scale * width );
		lp.height = (int)( scale * height );
		mFolderIconCanvas.drawColor( 0 , PorterDuff.Mode.CLEAR );
		fi.draw( mFolderIconCanvas );
		mFolderIconImageView.setImageBitmap( mFolderIconBitmap );
		if( fi.getFolder() != null )
		{
			mFolderIconImageView.setPivotX( fi.getFolder().getPivotXForIconAnimation() );
			mFolderIconImageView.setPivotY( fi.getFolder().getPivotYForIconAnimation() );
		}
		// Just in case this image view is still in the drag layer from a previous animation,
		// we remove it and re-add it.
		if( mDragLayer.indexOfChild( mFolderIconImageView ) != -1 )
		{
			mDragLayer.removeView( mFolderIconImageView );
		}
		mDragLayer.addView( mFolderIconImageView , lp );
		if( fi.getFolder() != null )
		{
			fi.getFolder().bringToFront();
		}
	}
	
	private void growAndFadeOutFolderIcon(
			FolderIcon fi )
	{
		if( fi == null )
			return;
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 0 );
		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat( "scaleX" , 1.5f );
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat( "scaleY" , 1.5f );
		//cheyingkun add start	//文件夹预览图层叠效果
		if(
		//
		( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
		//
		|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
		//
		)
		//cheyingkun add end	//文件夹预览图层叠效果
		{
			// lvjiangbin add start 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果
			FolderInfo info = (FolderInfo)fi.getTag();
			if( info.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
			{
				CellLayout cl = (CellLayout)fi.getParent().getParent();
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams)fi.getLayoutParams();
				cl.setFolderLeaveBehindCell( lp.cellX , lp.cellY );
			}
			// lvjiangbin add end 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果
		}
		// Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
		copyFolderIconToImage( fi );
		fi.setVisibility( View.INVISIBLE );
		ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder( mFolderIconImageView , alpha , scaleX , scaleY );
		oa.setDuration( LauncherDefaultConfig.getInt( R.integer.config_folderAnimDuration ) );
		oa.start();
	}
	
	private void shrinkAndFadeInFolderIcon(
			final FolderIcon fi )
	{
		if( fi == null )
			return;
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 1.0f );
		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat( "scaleX" , 1.0f );
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat( "scaleY" , 1.0f );
		final CellLayout cl = (CellLayout)fi.getParent().getParent();
		// We remove and re-draw the FolderIcon in-case it has changed
		mDragLayer.removeView( mFolderIconImageView );
		copyFolderIconToImage( fi );
		ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder( mFolderIconImageView , alpha , scaleX , scaleY );
		oa.setDuration( LauncherDefaultConfig.getInt( R.integer.config_folderAnimDuration ) );
		oa.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				if( cl != null )
				{
					//cheyingkun add start	//文件夹预览图层叠效果
					if(
					//
					( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
					//
					|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
					//
					)
					//cheyingkun add end	//文件夹预览图层叠效果
					{
						// lvjiangbin add start 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果
						cl.clearFolderLeaveBehind();
						// lvjiangbin add end 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果
					}
					// Remove the ImageView copy of the FolderIcon and make the original visible.
					mDragLayer.removeView( mFolderIconImageView );
					fi.setVisibility( View.VISIBLE );
				}
			}
		} );
		oa.start();
	}
	
	/**
	 * Opens the user folder described by the specified tag. The opening of the folder
	 * is animated relative to the specified View. If the View is null, no animation
	 * is played.
	 *
	 * @param folderInfo The FolderInfo describing the folder to open.
	 */
	public void openFolder(
			FolderIcon folderIcon )
	{
		if( folderIcon == null )
		{
			return;
		}
		Folder folder = folderIcon.getFolder();
		FolderInfo info = folder.getInfo();
		info.setOpened( true );
		// Just verify that the folder hasn't already been added to the DragLayer.
		// There was a one-off crash where the folder had a parent already.
		if( folder.getParent() == null )
		{
			mDragLayer.addView( folder );
			mDragController.addDropTarget( (DropTarget)folder );
		}
		else
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( TAG , StringUtils.concat( "Opening folder (" + folder , ") which already has a parent (" + folder.getParent() , ")." ) );
		}
		//cheyingkun add start	//添加友盟统计自定义事件
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			String title = info.getTitle();
			if( title.equals( LauncherDefaultConfig.getString( R.string.default_folder ) ) )
			{
				//phenix tools文件夹
				MobclickAgent.onEvent( this , UmengStatistics.ENTER_PHENIX_TOOLS_FOLDER );
			}
		}
		//cheyingkun add end
		folder.animateOpen();
		growAndFadeOutFolderIcon( folderIcon );
	}
	
	public void closeFolder()
	{
		Folder folder = mWorkspace.getOpenFolder();
		if( folder != null )
		{
			if( folder.hideDynamicDialog() )
			{
				return;
			}
			if( folder.isEditingName() )
			{
				folder.dismissEditingName();
			}
			closeFolder( folder );
			// Dismiss the folder cling
			dismissFolderCling( null );
		}
	}
	
	void closeFolder(
			Folder folder )
	{
		folder.getInfo().setOpened( false );
		ViewGroup parent = (ViewGroup)folder.getParent().getParent();
		if( parent != null )
		{
			FolderIcon fi = (FolderIcon)mWorkspace.getViewForTag( folder.getInfo() );
			shrinkAndFadeInFolderIcon( fi );
		}
		folder.animateClosed();
	}
	
	public boolean onLongClick(
			View v )
	{
		if( !isDraggingEnabled() )
			return false;
		if( isWorkspaceLocked() )
			return false;
		if( mState != State.WORKSPACE )
			return false;
		// zhujieping@2015/04/17 ADD START,桌面还在加载中，不可长按
		if( mModel.isLoaderTaskRunning() )
			return false;
		// zhujieping@2015/04/17 ADD END
		//cheyingkun add start	//桌面转换状态时屏蔽长按事件【i_0011700】【i_0011701】
		//【问题原因】长按百度4*2插件至桌面，选择模拟时钟或者数字时钟后,快速长按小部件位置
		//现象1.整个celllayout被托起并且转换状态动画暂停住,桌面显示异常
		//现象2.再次进入编辑模式,点击编辑模式退出时, 点击menu键, mStateAnimation != null && mStateAnimation.isRunning() 判断为true 无法进入编辑模式
		//【解决方案】状态转换期间屏蔽长按事件
		if( mWorkspace.isSwitchingState() )
		{
			return false;
		}
		//cheyingkun add end
		//xiatian add start	//桌面适应手机虚拟按键
		if( !LauncherAppState.getInstance().isVirtualMenuShown() )
		{
			// zhangjin@2016/05/10 UPD START
			//getWindow().addFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
			if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_KITKAT )
			{
				getWindow().addFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
			}
			// zhangjin@2016/05/10 UPD END
		}
		else
		{
			getWindow().clearFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
		}
		//xiatian add end
		if( v instanceof Workspace )
		{
			if( !mWorkspace.isInOverviewMode() )
			{
				if(
				//
				( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_BLANK_OF_LONGCLICK/* //gaominghui add	//添加配置项“switch_enable_response_blank_of_longclick”，桌面空白处是否响应长按消息。true为响应长按，false为不响应长按。默认true。 */)
				//
				&& mWorkspace.enterOverviewMode()
				//
				)
				{
					mWorkspace.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS , HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING );
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		if( !( v instanceof CellLayout ) )
		{
			v = (View)v.getParent().getParent();
		}
		resetAddInfo();
		CellInfo longClickCellInfo = (CellInfo)v.getTag();
		// This happens when long clicking an item with the dpad/trackball
		if( longClickCellInfo == null )
		{
			return true;
		}
		// The hotseat touch handling does not go through Workspace, and we always allow long press
		// on hotseat items.
		final View itemUnderLongClick = longClickCellInfo.getCell();
		boolean allowLongPress = isHotseatLayout( v ) || mWorkspace.allowLongPress( v );
		if( allowLongPress && !mDragController.isDragging() )
		{
			if( itemUnderLongClick == null )
			{
				// User long pressed on empty space
				//				mWorkspace.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS , HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING );//gaominghui del	//添加配置项“switch_enable_response_blank_of_longclick”，桌面空白处是否响应长按消息。true为响应长按，false为不响应长按。默认true。
				// Disabling reordering until we sort out some issues.
				if( mWorkspace.isInOverviewMode() )
				{
					mWorkspace.startReordering( v );
				}
				else
				{
					//gaominghui add start	//添加配置项“switch_enable_response_blank_of_longclick”，桌面空白处是否响应长按消息。true为响应长按，false为不响应长按。默认true。
					if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_BLANK_OF_LONGCLICK == false )
					{
						return true;
					}
					//gaominghui add end
					mWorkspace.enterOverviewMode();
				}
				mWorkspace.performHapticFeedback( HapticFeedbackConstants.LONG_PRESS , HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING );//gaominghui add	//添加配置项“switch_enable_response_blank_of_longclick”，桌面空白处是否响应长按消息。true为响应长按，false为不响应长按。默认true。
			}
			else
			{
				if( !( itemUnderLongClick instanceof Folder ) )
				{
					// User long pressed on an item
					// zhangjin@2016/04/05 UPD START
					//mWorkspace.startDrag( longClickCellInfo );
					if( LauncherDefaultConfig.HERUNXIN_BIG_LAUNCHER && itemUnderLongClick instanceof LauncherAppWidgetHostView )
					{
						return true;
					}
					mWorkspace.startDrag( longClickCellInfo );
					// zhangjin@2016/04/05 UPD END
				}
			}
		}
		return true;
	}
	
	boolean isHotseatLayout(
			View layout )
	{
		return mHotseat != null && layout != null && ( layout instanceof CellLayout ) && ( layout == mHotseat.getLayout() );
	}
	
	Hotseat getHotseat()
	{
		return mHotseat;
	}
	
	public View getOverviewPanel()
	{
		return mOverviewPanel;
	}
	
	public SearchDropTargetBar getSearchDropTargetBar()
	{
		return mSearchDropTargetBar;
	}
	
	/**
	 * Returns the CellLayout of the specified container at the specified screen.
	 */
	public CellLayout getCellLayout(
			long container ,
			long screenId )
	{
		if( container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
		{
			if( mHotseat != null )
			{
				return mHotseat.getLayout();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return (CellLayout)mWorkspace.getScreenWithId( screenId );
		}
	}
	
	public Workspace getWorkspace()
	{
		return mWorkspace;
	}
	
	public boolean isAllAppsVisible()
	{
		return ( mState == State.APPS_CUSTOMIZE ) || ( mOnResumeState == State.APPS_CUSTOMIZE );
	}
	
	/**
	 * Helper method for the cameraZoomIn/cameraZoomOut animations
	 * @param view The view being animated
	 * @param scaleFactor The scale factor used for the zoom
	 */
	private void setPivotsForZoom(
			View view ,
			float scaleFactor )
	{
		view.setPivotX( view.getWidth() / 2.0f );
		view.setPivotY( view.getHeight() / 2.0f );
	}
	
	private void dispatchOnLauncherTransitionPrepare(
			View v ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionPrepare( this , animated , toWorkspace );
		}
	}
	
	private void dispatchOnLauncherTransitionStart(
			View v ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionStart( this , animated , toWorkspace );
		}
		// Update the workspace transition step as well
		dispatchOnLauncherTransitionStep( v , 0f );
	}
	
	private void dispatchOnLauncherTransitionStep(
			View v ,
			float t )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionStep( this , t );
		}
	}
	
	private void dispatchOnLauncherTransitionEnd(
			View v ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionEnd( this , animated , toWorkspace );
		}
		// Update the workspace transition step as well
		dispatchOnLauncherTransitionStep( v , 1f );
	}
	
	/**
	 * Things to test when changing the following seven functions.
	 *   - Home from workspace
	 *          - from center screen
	 *          - from other screens
	 *   - Home from all apps
	 *          - from center screen
	 *          - from other screens
	 *   - Back from all apps
	 *          - from center screen
	 *          - from other screens
	 *   - Launch app from workspace and quit
	 *          - with back
	 *          - with home
	 *   - Launch app from all apps and quit
	 *          - with back
	 *          - with home
	 *   - Go to a screen that's not the default, then all
	 *     apps, and launch and app, and go back
	 *          - with back
	 *          -with home
	 *   - On workspace, long press power and go back
	 *          - with back
	 *          - with home
	 *   - On all apps, long press power and go back
	 *          - with back
	 *          - with home
	 *   - On workspace, power off
	 *   - On all apps, power off
	 *   - Launch an app and turn off the screen while in that app
	 *          - Go back with home key
	 *          - Go back with back key  TODO: make this not go to workspace
	 *          - From all apps
	 *          - From workspace
	 *   - Enter and exit car mode (becuase it causes an extra configuration changed)
	 *          - From all apps
	 *          - From the center workspace
	 *          - From another workspace
	 */
	/**
	 * Zoom the camera out from the workspace to reveal 'toView'.
	 * Assumes that the view to show is anchored at either the very top or very bottom
	 * of the screen.
	 */
	private void showAppsCustomizeHelper(
			final boolean animated ,
			final boolean springLoaded )
	{
		AppsCustomizePagedView.ContentType contentType = mAppsCustomizeContent.getContentType();
		showAppsCustomizeHelper( animated , springLoaded , contentType );
	}
	
	private void showAppsCustomizeHelper(
			final boolean animated ,
			final boolean springLoaded ,
			final AppsCustomizePagedView.ContentType contentType )
	{
		cancelStateAnimation();
		final Resources res = getResources();
		final int duration = LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeZoomInTime );
		final int fadeDuration = LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeFadeInTime );
		final float scale = (float)LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeZoomScaleFactor );
		final View fromView = mWorkspace;
		// zhangjin@2016/05/06 UPD START
		//final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;
		View tragetView = null;
		if(
		//
		(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		//
		&& mAppsView != null
		//
		&& contentType == AppsCustomizePagedView.ContentType.Applications
		//
		)
		{
			tragetView = mAppsView;
		}
		else
		{
			tragetView = mAppsCustomizeTabHost;
		}
		final View toView = tragetView;
		// zhangjin@2016/05/06 UPD END
		if( contentType == AppsCustomizePagedView.ContentType.Applications )
		{
			showAppsCustomizeHelperTabsContainer( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE != LauncherDefaultConfig.APPLIST_BAR_STYLE_NO_BAR );
		}
		else
		{
			if( !springLoaded )//zhujieping add,当springLoaded为true时，是拖动小组件失败回到小组件页，tab栏保持与之前相同，此处不需要设置；当springLoaded为false时，是从桌面编辑页面进入小组件，需要隐藏tab栏，此处设置不显示，i_0011122
			{
				showAppsCustomizeHelperTabsContainer( false );
			}
		}
		final int startDelay = LauncherDefaultConfig.getInt( R.integer.config_workspaceAppsCustomizeAnimationStagger );
		setPivotsForZoom( toView , scale );
		// Shrink workspaces away if going to AppsCustomize from workspace
		Animator workspaceAnim = mWorkspace.getChangeStateAnimation( Workspace.State.SMALL , animated );
		//核心桌面不能添加小组件 , change by shlt@2015/01/23 DEL START
		//<i_0010072> liuhailin@2015-03-04 modify begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//<i_0010072> liuhailin@2015-03-04 modify end
		//核心桌面不能添加小组件 , change by shlt@2015/01/23 DEL END
		{
			// Set the content type for the all apps space
			mAppsCustomizeTabHost.setContentTypeImmediate( contentType );
		}
		if( animated )
		{
			toView.setScaleX( scale );
			toView.setScaleY( scale );
			final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator( toView );
			scaleAnim.scaleX( 1f ).scaleY( 1f ).setDuration( duration ).setInterpolator( new Workspace.ZoomOutInterpolator() );
			toView.setVisibility( View.VISIBLE );
			toView.setAlpha( 0f );
			final ObjectAnimator alphaAnim = LauncherAnimUtils.ofFloat( toView , "alpha" , 0f , 1f ).setDuration( fadeDuration );
			alphaAnim.setInterpolator( new DecelerateInterpolator( 1.5f ) );
			alphaAnim.addUpdateListener( new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					if( animation == null )
					{
						throw new RuntimeException( "animation is null" );
					}
					float t = (Float)animation.getAnimatedValue();
					dispatchOnLauncherTransitionStep( fromView , t );
					dispatchOnLauncherTransitionStep( toView , t );
				}
			} );
			// toView should appear right at the end of the workspace shrink
			// animation
			mStateAnimation = LauncherAnimUtils.createAnimatorSet();
			mStateAnimation.play( scaleAnim ).after( startDelay );
			mStateAnimation.play( alphaAnim ).after( startDelay );
			mStateAnimation.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationStart(
						Animator animation )
				{
					// Prepare the position
					toView.setTranslationX( 0.0f );
					toView.setTranslationY( 0.0f );
					toView.setVisibility( View.VISIBLE );
					toView.bringToFront();
				}
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					dispatchOnLauncherTransitionEnd( fromView , animated , false );
					dispatchOnLauncherTransitionEnd( toView , animated , false );
					// Hide the search bar
					if( mSearchDropTargetBar != null )
					{
						mSearchDropTargetBar.hideSearchBar( false );
					}
					showAppListLoadHint( contentType );//cheyingkun add 	//加载主菜单提示信息【c_0003106】
					showWidgetListLoadHint( contentType );//cheyingkun add	//启动速度优化(小部件更新放在加载完成之后)
					// zhangjin@2016/05/09 ADD START
					if(
					//
					(
					//
					( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
					//
					|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
					//
					)
					//
					&& ( mAppsView != null )
					// 
					&& ( contentType == AppsCustomizePagedView.ContentType.Applications/* //cheyingkun add	//解决“配置6.0主菜单样式后，小部件界面长按往桌面添加小部件，界面显示异常”的问题【c_0004387】 */)
					//
					)
					{
						mAppsView.getContentView().setVisibility( View.VISIBLE );
					}
					// zhangjin@2016/05/09 ADD END
				}
			} );
			// gaominghui@2016/12/14 ADD START 兼容Android 4.0
			if( Build.VERSION.SDK_INT <= 15 )
			{
				mStateAnimation.start();
			}
			if( workspaceAnim != null )
			{
				mStateAnimation.play( workspaceAnim );
				if( Build.VERSION.SDK_INT <= 15 )
				{
					mStateAnimation.start();
				}
			}
			// gaominghui@2016/12/14 ADD END 兼容Android 4.0
			boolean delayAnim = false;
			dispatchOnLauncherTransitionPrepare( fromView , animated , false );
			dispatchOnLauncherTransitionPrepare( toView , animated , false );
			// If any of the objects being animated haven't been measured/laid out
			// yet, delay the animation until we get a layout pass
			// zhangjin@2016/05/09 UPD START
			//if( ( ( (ILauncherTransitionable)toView ).getContent().getMeasuredWidth() == 0 ) || ( mWorkspace.getMeasuredWidth() == 0 ) || ( toView.getMeasuredWidth() == 0 ) )
			if( ( toView instanceof ILauncherTransitionable && ( (ILauncherTransitionable)toView ).getContent().getMeasuredWidth() == 0 ) || ( mWorkspace.getMeasuredWidth() == 0 ) || ( toView
					.getMeasuredWidth() == 0 ) )
			// zhangjin@2016/05/09 UPD END
			{
				delayAnim = true;
			}
			final AnimatorSet stateAnimation = mStateAnimation;
			final Runnable startAnimRunnable = new Runnable() {
				
				public void run()
				{
					// Check that mStateAnimation hasn't changed while
					// we waited for a layout/draw pass
					if( mStateAnimation != stateAnimation )
						return;
					setPivotsForZoom( toView , scale );
					dispatchOnLauncherTransitionStart( fromView , animated , false );
					dispatchOnLauncherTransitionStart( toView , animated , false );
					LauncherAnimUtils.startAnimationAfterNextDraw( mStateAnimation , toView );
				}
			};
			if( delayAnim )
			{
				final ViewTreeObserver observer = toView.getViewTreeObserver();
				observer.addOnGlobalLayoutListener( new OnGlobalLayoutListener() {
					
					public void onGlobalLayout()
					{
						startAnimRunnable.run();
						// gaominghui@2016/12/14 ADD START
						if( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN )
							toView.getViewTreeObserver().removeGlobalOnLayoutListener( this );
						else
						{
							toView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
						}
						// gaominghui@2016/12/14 ADD END
					}
				} );
			}
			else
			{
				startAnimRunnable.run();
			}
		}
		else
		{
			toView.setTranslationX( 0.0f );
			toView.setTranslationY( 0.0f );
			toView.setScaleX( 1.0f );
			toView.setScaleY( 1.0f );
			toView.setAlpha( 1f );
			toView.setVisibility( View.VISIBLE );
			toView.bringToFront();
			if( !springLoaded )
			{
				// Hide the search bar
				if( mSearchDropTargetBar != null )
				{
					mSearchDropTargetBar.hideSearchBar( false );
				}
			}
			dispatchOnLauncherTransitionPrepare( fromView , animated , false );
			dispatchOnLauncherTransitionStart( fromView , animated , false );
			dispatchOnLauncherTransitionEnd( fromView , animated , false );
			dispatchOnLauncherTransitionPrepare( toView , animated , false );
			dispatchOnLauncherTransitionStart( toView , animated , false );
			dispatchOnLauncherTransitionEnd( toView , animated , false );
		}
	}
	
	/**
	 * Zoom the camera back into the workspace, hiding 'fromView'.
	 * This is the opposite of showAppsCustomizeHelper.
	 * @param animated If true, the transition will be animated.
	 */
	private void hideAppsCustomizeHelper(
			final Workspace.State toState ,
			final boolean animated ,
			final boolean springLoaded ,
			final Runnable onCompleteRunnable )
	{
		cancelStateAnimation();
		Resources res = getResources();
		final int duration = LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeZoomOutTime );
		final int fadeOutDuration = LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeFadeOutTime );
		final float scaleFactor = (float)LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeZoomScaleFactor );
		// zhangjin@2016/05/09 UPD START
		//final View fromView = mAppsCustomizeTabHost;		
		View tragetView = null;
		AppsCustomizePagedView.ContentType contentType = mAppsCustomizeContent.getContentType();//cheyingkun add	//解决“配置6.0主菜单样式后，小部件界面长按往桌面添加小部件，界面显示异常”的问题【c_0004387】
		// zhangjin@2016/07/13 UPD START bug c_0004354
		//if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW && mAppsView != null && mAppsView.isShown() )
		if(
		//
		(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		//
		&& ( mAppsView != null )
		//
		&& ( contentType == AppsCustomizePagedView.ContentType.Applications/* //cheyingkun add	//解决“配置6.0主菜单样式后，小部件界面长按往桌面添加小部件，界面显示异常”的问题【c_0004387】 */)
		//
		)
		// zhangjin@2016/07/13 UPD END
		{
			tragetView = mAppsView;
		}
		else
		{
			tragetView = mAppsCustomizeTabHost;
			//zhujieping add，主菜单不显示了，退回普通模式
		}
		final View fromView = tragetView;
		// zhangjin@2016/05/09 UPD END
		final View toView = mWorkspace;
		Animator workspaceAnim = null;
		if( toState == Workspace.State.NORMAL )
		{
			int stagger = LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeWorkspaceAnimationStagger );
			workspaceAnim = mWorkspace.getChangeStateAnimation( toState , animated , stagger , -1 );
		}
		else if( toState == Workspace.State.SPRING_LOADED || toState == Workspace.State.OVERVIEW )
		{
			workspaceAnim = mWorkspace.getChangeStateAnimation( toState , animated );
		}
		setPivotsForZoom( fromView , scaleFactor );
		if( toState != Workspace.State.OVERVIEW )//cheyingkun add	//解决“点击menu键进入编辑模式，点击小部件后，点击返回键。底边栏四个图标会闪一下再消失。”的问题【0010677】
		{
			showHotseat( animated );
		}
		if( animated )
		{
			//chenliang del start	//解决“由于Android7.0,AimatiorSet.playTogether播放自定义属性动画会导致isRunning方法一直返回true引起编辑界面底边栏4个图标点击都无效”的问题。
			//			final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator( fromView );
			//			scaleAnim.scaleX( scaleFactor ).scaleY( scaleFactor ).setDuration( duration ).setInterpolator( new Workspace.ZoomInInterpolator() );
			//chenliang del end
			final ObjectAnimator alphaAnim = LauncherAnimUtils.ofFloat( fromView , "alpha" , 1f , 0f ).setDuration( fadeOutDuration );
			alphaAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
			alphaAnim.addUpdateListener( new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					float t = 1f - (Float)animation.getAnimatedValue();
					dispatchOnLauncherTransitionStep( fromView , t );
					dispatchOnLauncherTransitionStep( toView , t );
				}
			} );
			mStateAnimation = LauncherAnimUtils.createAnimatorSet();
			dispatchOnLauncherTransitionPrepare( fromView , animated , true );
			dispatchOnLauncherTransitionPrepare( toView , animated , true );
			mAppsCustomizeContent.pauseScrolling();
			mStateAnimation.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					fromView.setVisibility( View.GONE );
					dispatchOnLauncherTransitionEnd( fromView , animated , true );
					dispatchOnLauncherTransitionEnd( toView , animated , true );
					if( onCompleteRunnable != null )
					{
						onCompleteRunnable.run();
					}
					mAppsCustomizeContent.updateCurrentPageScroll();
					mAppsCustomizeContent.resumeScrolling();
					mWorkspace.checkSelectedPageWhenChangeState( toState == Workspace.State.SPRING_LOADED || toState == Workspace.State.OVERVIEW );//cheyingkun add	//phenix仿S5效果,编辑模式页面背景选中效果
				}
			} );
			//chenliang add start	//解决“由于Android7.0,AimatiorSet.playTogether播放自定义属性动画会导致isRunning方法一直返回true引起编辑界面底边栏4个图标点击都无效”的问题。
			if( Build.VERSION.SDK_INT >= 24 )
			{
				ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat( fromView , "scaleX" , scaleFactor );
				ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat( fromView , "scaleY" , scaleFactor );
				scaleXAnim.setDuration( duration ).setInterpolator( new Workspace.ZoomInInterpolator() );
				scaleYAnim.setDuration( duration ).setInterpolator( new Workspace.ZoomInInterpolator() );
				mStateAnimation.playTogether( scaleXAnim , scaleYAnim , alphaAnim );
			}
			else
			{
				final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator( fromView );
				scaleAnim.scaleX( scaleFactor ).scaleY( scaleFactor ).setDuration( duration ).setInterpolator( new Workspace.ZoomInInterpolator() );
				mStateAnimation.playTogether( scaleAnim , alphaAnim );
			}
			//chenliang add end		
			// gaominghui@2016/12/14 ADD START 兼容Android 4.0
			if( Build.VERSION.SDK_INT <= 15 )
			{
				mStateAnimation.start();
			}
			if( workspaceAnim != null )
			{
				mStateAnimation.play( workspaceAnim );
				if( Build.VERSION.SDK_INT <= 15 )
				{
					mStateAnimation.start();
				}
			}
			// gaominghui@2016/12/14 ADD END  兼容Android 4.0
			dispatchOnLauncherTransitionStart( fromView , animated , true );
			dispatchOnLauncherTransitionStart( toView , animated , true );
			// gaominghui@2016/12/14 ADD START 兼容android4.0
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
			{
				LauncherAnimUtils.startAnimationAfterNextDraw( mStateAnimation , toView );
			}
			// gaominghui@2016/12/14 ADD END  兼容android4.0
		}
		else
		{
			fromView.setVisibility( View.GONE );
			dispatchOnLauncherTransitionPrepare( fromView , animated , true );
			dispatchOnLauncherTransitionStart( fromView , animated , true );
			dispatchOnLauncherTransitionEnd( fromView , animated , true );
			dispatchOnLauncherTransitionPrepare( toView , animated , true );
			dispatchOnLauncherTransitionStart( toView , animated , true );
			dispatchOnLauncherTransitionEnd( toView , animated , true );
			mWorkspace.checkSelectedPageWhenChangeState( toState == Workspace.State.SPRING_LOADED || toState == Workspace.State.OVERVIEW );//cheyingkun add	//phenix仿S5效果,编辑模式页面背景选中效果
		}
		hideAppListLoadHint();//cheyingkun add 	//加载主菜单提示信息【c_0003106】
		hideWidgetListLoadHint();//cheyingkun add	//启动速度优化(小部件更新放在加载完成之后)
	}
	
	@Override
	public void onTrimMemory(
			int level )
	{
		super.onTrimMemory( level );
		if( level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE )
		{
			mAppsCustomizeTabHost.onTrimMemory();
		}
	}
	
	public void showWorkspace(
			boolean animated )
	{
		showWorkspace( animated , null );
	}
	
	protected void showWorkspace()
	{
		showWorkspace( true );
	}
	
	void showWorkspace(
			boolean animated ,
			Runnable onCompleteRunnable )
	{
		if( mWorkspace.isInOverviewMode() )
		{
			exitEditMode( animated );
		}
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		if(
		//
		LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
		//
				&& LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT//zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		//
		&& mAllAppsController != null
		//
		&& mStateTransitionAnimation != null
		//
		&& ( ( mState != State.WORKSPACE || mWorkspace.getState() != Workspace.State.NORMAL ) || mAllAppsController.isTransitioning() )
		//
		&& ( !mAppsCustomizeContent.isShown() || ( mAppsCustomizeContent.isShown() && mAppsCustomizeContent.getContentType() != AppsCustomizePagedView.ContentType.Widgets ) )
		//
		&& !( mState == State.APPS_CUSTOMIZE_SPRING_LOADED && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Widgets )//从小组件拖动小组件回到桌面，走else
		//
		&& !( mState == State.APPS_CUSTOMIZE && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Widgets )//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		)
		{
			mWorkspace.setVisibility( View.VISIBLE );
			mStateTransitionAnimation.startAnimationToWorkspace( mState , mWorkspace.getState() , Workspace.State.NORMAL , animated , onCompleteRunnable );
			// Set focus to the AppsCustomize button
		}
		else
		//zhujieping add end
		{
			if( mState != State.WORKSPACE )
			{
				if( LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_APPLIST_TO_WORKSPACE <= 0 )
				{
					animated = false;
				}
				mWorkspace.setVisibility( View.VISIBLE );
				hideAppsCustomizeHelper( Workspace.State.NORMAL , animated , false , onCompleteRunnable );
				// Show the search bar (only animate if we were showing th e drop target bar in spring
				// loaded mode)
			}
		}
		boolean wasInSpringLoadedMode = ( mState != State.WORKSPACE );
		if( mSearchDropTargetBar != null )
		{
			mSearchDropTargetBar.showSearchBar( animated && wasInSpringLoadedMode );
		}
		// Change the state *after* we've called all the transition code
		mState = State.WORKSPACE;
		// Resume the auto-advance of widgets
		mUserPresent = true;
		updateRunning();
		onWorkspaceShown( animated );
		//WangLei add start //桌面和主菜单特效的分离
		/**双层模式时由主菜单切换到桌面，更新切页特效*/
		String effectValue = "0";
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			effectValue = preferences.getString( LauncherDefaultConfig.getString( R.string.setting_key_launcher_effects ) , "0" );
		}
		else
		{
			effectValue = preferences.getString( LauncherDefaultConfig.getString( R.string.setting_key_workspace_effect ) , "0" );
		}
		setSelect_efffects_workspace( Integer.parseInt( effectValue ) );
		if( mWorkspace != null )
		{
			mWorkspace.initAnimationStyle( mWorkspace );
		}
		//WangLei add end
	}
	
	void showOverviewMode(
			boolean animated )
	{
		mWorkspace.setVisibility( View.VISIBLE );
		hideAppsCustomizeHelper( Workspace.State.OVERVIEW , animated , false , null );
		mState = State.WORKSPACE;
		onWorkspaceShown( animated );
	}
	
	public void onWorkspaceShown(
			boolean animated )
	{
	}
	
	// zhangjin@2016/05/06 ADD START
	/**
	* Shows the apps view.
	*/
	public void showAppsView(
			boolean animated ,
			boolean resetListToTop ,
			boolean updatePredictedApps ,
			boolean focusSearchBar ,
			AppsCustomizePagedView.ContentType contentType )
	{
		if( resetListToTop )
		{
			if( mAppsView != null )
				mAppsView.scrollToTop();
		}
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		if(
		//
		LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
		//
		&& contentType == AppsCustomizePagedView.ContentType.Applications
		//
		&& mAllAppsController != null
		//
		&& mStateTransitionAnimation != null
		//
				&& LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT//zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		//
		)
		{
			if( !( mState == State.WORKSPACE || mState == State.APPS_CUSTOMIZE_SPRING_LOADED || ( mState == State.APPS_CUSTOMIZE && mAllAppsController.isTransitioning() ) ) )
			{
				return;
			}
			//xiatian add start	//解决“桌面滑动过程中，快速点击主菜单入口，然后长按主菜单图标，这时桌面还处在切页状态，没有复位”的问题。【i_0013307】
			if( mWorkspace.isScrollPage() )
			{
				mWorkspace.stopEffecf();
			}
			//xiatian add end
			//cheyingkun add start	//解决“双层模式、删除默认配置的时钟插件、打开循环切页、关掉酷生活、打开音乐页后，循环切页后主菜单点击不进去”的问题【c_0004425】
			if( mWorkspace.isFavoritesPageByPageIndex( mWorkspace.getCurrentPage() ) )
			{
				return;
			}
			//cheyingkun add end
			mAppsCustomizeTabHost.setContentTypeImmediate( contentType );//mAppsCustomizeTabHost置回对应的type
			mStateTransitionAnimation.startAnimationToAllApps( mWorkspace.getState() , animated , focusSearchBar );
		}
		else
		//zhujieping add end
		{
			if( mState != State.WORKSPACE )
				return;
			//xiatian add start	//解决“桌面滑动过程中，快速点击主菜单入口，然后长按主菜单图标，这时桌面还处在切页状态，没有复位”的问题。【i_0013307】
			if( mWorkspace.isScrollPage() )
			{
				mWorkspace.stopEffecf();
			}
			//xiatian add end
			//cheyingkun add start	//解决“双层模式、删除默认配置的时钟插件、打开循环切页、关掉酷生活、打开音乐页后，循环切页后主菜单点击不进去”的问题【c_0004425】
			if( mWorkspace.isFavoritesPageByPageIndex( mWorkspace.getCurrentPage() ) )
			{
				return;
			}
			//cheyingkun add end
			showAppsCustomizeHelper( animated , false , contentType );
		}
		mAppsCustomizeTabHost.requestFocus();
		// Change the state *after* we've called all the transition code
		mState = State.APPS_CUSTOMIZE;
		// Pause the auto-advance of widgets until we are out of AllApps
		mUserPresent = false;
		updateRunning();
		closeFolder();
		//zhujieping add end //7.0进入主菜单动画改成也支持4.4主菜单样式
		//WangLei add start //桌面和主菜单特效的分离
		/**双层模式进入主菜单时更新特效*/
		if( contentType == AppsCustomizePagedView.ContentType.Applications )
		{
			String effectsValue = PreferenceManager.getDefaultSharedPreferences( this ).getString( LauncherDefaultConfig.getString( R.string.setting_key_applist_effect ) , "0" );
			setSelect_effects_applist( Integer.parseInt( effectsValue ) );
			if( mAppsCustomizeContent != null )
			{
				mAppsCustomizeContent.initAnimationStyle( mAppsCustomizeContent );
			}
		}
		//WangLei add end
		//zhujieping add end
	}
	
	public DeviceProfile getDeviceProfile()
	{
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		return grid;
	}
	// zhangjin@2016/05/06 ADD END
	;
	
	void showAllApps(
			boolean animated ,
			AppsCustomizePagedView.ContentType contentType ,
			boolean resetPageToZero )
	{
		if( mState != State.WORKSPACE )
			return;
		if( resetPageToZero )
		{
			mAppsCustomizeTabHost.reset();
		}
		//xiatian add start	//解决“桌面滑动过程中，快速点击主菜单入口，然后长按主菜单图标，这时桌面还处在切页状态，没有复位”的问题。【i_0013307】
		if( mWorkspace.isScrollPage() )
		{
			mWorkspace.stopEffecf();
		}
		//xiatian add end
		//cheyingkun add start	//解决“双层模式、删除默认配置的时钟插件、打开循环切页、关掉酷生活、打开音乐页后，循环切页后主菜单点击不进去”的问题【c_0004425】
		if( mWorkspace.isFavoritesPageByPageIndex( mWorkspace.getCurrentPage() ) )
		{
			return;
		}
		//cheyingkun add end
		if( LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_APPLIST <= 0 )
		{
			animated = false;
		}
		showAppsCustomizeHelper( animated , false , contentType );
		mAppsCustomizeTabHost.requestFocus();
		// Change the state *after* we've called all the transition code
		mState = State.APPS_CUSTOMIZE;
		// Pause the auto-advance of widgets until we are out of AllApps
		mUserPresent = false;
		updateRunning();
		closeFolder();
		//WangLei add start //桌面和主菜单特效的分离
		/**双层模式进入主菜单时更新特效*/
		if( contentType == AppsCustomizePagedView.ContentType.Applications )
		{
			String effectsValue = PreferenceManager.getDefaultSharedPreferences( this ).getString( LauncherDefaultConfig.getString( R.string.setting_key_applist_effect ) , "0" );
			setSelect_effects_applist( Integer.parseInt( effectsValue ) );
			if( mAppsCustomizeContent != null )
			{
				mAppsCustomizeContent.initAnimationStyle( mAppsCustomizeContent );
			}
		}
		//WangLei add end
	}
	
	public void enterSpringLoadedDragMode(
			AppsCustomizePagedView.ContentType contentType )
	{
		if( isAllAppsVisible() )
		{
			//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
			if(
			//
			contentType == AppsCustomizePagedView.ContentType.Applications
			//
					&& LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT//zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
			//
			)
			{
				mStateTransitionAnimation.startAnimationToWorkspace( mState , mWorkspace.getState() , Workspace.State.SPRING_LOADED , true /* animated */, null /* onCompleteRunnable */);
			}
			else
			//zhujieping add end
			{
				hideAppsCustomizeHelper( Workspace.State.SPRING_LOADED , true , true , null );
			}
			mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
		}
	}
	
	public void exitSpringLoadedDragModeDelayed(
			final boolean successfulDrop ,
			boolean extendedDelay ,
			final Runnable onCompleteRunnable )
	{
		if( mState != State.APPS_CUSTOMIZE_SPRING_LOADED )
			return;
		mHandler.postDelayed( new Runnable() {
			
			@Override
			public void run()
			{
				if( successfulDrop )
				{
					// Before we show workspace, hide all apps again because
					// exitSpringLoadedDragMode made it visible. This is a bit hacky; we should
					// clean up our state transition functions
					mAppsCustomizeTabHost.setVisibility( View.GONE );
					showWorkspace( true , onCompleteRunnable );
					Launcher.this.mWorkspace.initAnimationStyle( Launcher.this.mWorkspace );
					Launcher.this.mWorkspace.restoreWorkspace();
				}
				else
				{
					exitSpringLoadedDragMode();
				}
			}
		} , ( extendedDelay ? EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT : EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT ) );
	}
	
	public void exitSpringLoadedDragMode()
	{
		if( mState == State.APPS_CUSTOMIZE_SPRING_LOADED )
		{
			final boolean animated = true;
			final boolean springLoaded = true;
			//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
			if(
			//
			( mAppsCustomizeContent != null && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Applications )
					//
					&& LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT
			//
			)
			{
				mStateTransitionAnimation.startAnimationToAllApps( mWorkspace.getState() , animated , false );
			}
			else
			//zhujieping add end
			{
				
				showAppsCustomizeHelper( animated , springLoaded );
			}
			mState = State.APPS_CUSTOMIZE;
		}
		// Otherwise, we are not in spring loaded mode, so don't do anything.
	}
	
	void lockAllApps()
	{
		// TODO
	}
	
	void unlockAllApps()
	{
		// TODO
	}
	
	/**
	 * Shows the hotseat area.
	 */
	void showHotseat(
			boolean animated )
	{
		if( animated )
		{
			if( mHotseat.getAlpha() != 1f )
			{
				int duration = 0;
				if( mSearchDropTargetBar != null )
				{
					duration = mSearchDropTargetBar.getTransitionInDuration();
				}
				mHotseat.animate().alpha( 1f ).setDuration( duration );
			}
		}
		else
		{
			mHotseat.setAlpha( 1f );
		}
	}
	
	/**
	 * Hides the hotseat area.
	 */
	void hideHotseat(
			boolean animated )
	{
		if( animated )
		{
			if( mHotseat.getAlpha() != 0f )
			{
				int duration = 0;
				if( mSearchDropTargetBar != null )
				{
					duration = mSearchDropTargetBar.getTransitionOutDuration();
				}
				mHotseat.animate().alpha( 0f ).setDuration( duration );
			}
		}
		else
		{
			mHotseat.setAlpha( 0f );
		}
	}
	
	/**
	 * Add an item from all apps or customize onto the given workspace screen.
	 * If layout is null, add to the current screen.
	 */
	void addExternalItemToScreen(
			ItemInfo itemInfo ,
			final CellLayout layout )
	{
		if( !mWorkspace.addExternalItemToScreen( itemInfo , layout ) )
		{
			showOutOfSpaceMessage( isHotseatLayout( layout ) );
		}
	}
	
	/** Maps the current orientation to an index for referencing orientation correct global icons */
	private int getCurrentOrientationIndexForGlobalIcons()
	{
		// default - 0, landscape - 1
		switch( getResources().getConfiguration().orientation )
		{
			case Configuration.ORIENTATION_LANDSCAPE:
				return 1;
			default:
				return 0;
		}
	}
	
	private Drawable getExternalPackageToolbarIcon(
			ComponentName activityName ,
			String resourceName )
	{
		try
		{
			PackageManager packageManager = getPackageManager();
			// Look for the toolbar icon specified in the activity meta-data
			Bundle metaData = packageManager.getActivityInfo( activityName , PackageManager.GET_META_DATA ).metaData;
			if( metaData != null )
			{
				int iconResId = metaData.getInt( resourceName );
				if( iconResId != 0 )
				{
					Resources res = packageManager.getResourcesForActivity( activityName );
					return res.getDrawable( iconResId );
				}
			}
		}
		catch( NameNotFoundException e )
		{
			// This can happen if the activity defines an invalid drawable
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( TAG , StringUtils.concat( "Failed to load toolbar icon; " , activityName.flattenToShortString() , " not found" ) , e );
		}
		catch( Resources.NotFoundException nfe )
		{
			// This can happen if the activity defines an invalid drawable
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( TAG , StringUtils.concat( "Failed to load toolbar icon from " , activityName.flattenToShortString() ) , nfe );
		}
		return null;
	}
	
	// if successful in getting icon, return it; otherwise, set button to use default drawable
	private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(
			int buttonId ,
			ComponentName activityName ,
			int fallbackDrawableId ,
			String toolbarResourceName )
	{
		Drawable toolbarIcon = getExternalPackageToolbarIcon( activityName , toolbarResourceName );
		Resources r = getResources();
		int w = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.toolbar_external_icon_width );
		int h = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.toolbar_external_icon_height );
		TextView button = (TextView)findViewById( buttonId );
		// If we were unable to find the icon via the meta-data, use a generic one
		if( toolbarIcon == null )
		{
			toolbarIcon = r.getDrawable( fallbackDrawableId );
			toolbarIcon.setBounds( 0 , 0 , w , h );
			if( button != null )
			{
				button.setCompoundDrawables( toolbarIcon , null , null , null );
			}
			return null;
		}
		else
		{
			toolbarIcon.setBounds( 0 , 0 , w , h );
			if( button != null )
			{
				button.setCompoundDrawables( toolbarIcon , null , null , null );
			}
			return toolbarIcon.getConstantState();
		}
	}
	
	// if successful in getting icon, return it; otherwise, set button to use default drawable
	private Drawable.ConstantState updateButtonWithIconFromExternalActivity(
			int buttonId ,
			ComponentName activityName ,
			int fallbackDrawableId ,
			String toolbarResourceName )
	{
		ImageView button = (ImageView)findViewById( buttonId );
		Drawable toolbarIcon = null;
		if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE )
		{
			if(
			//
			buttonId == R.id.search_button
			//
			|| buttonId == R.id.voice_button
			//
			)
			{
				toolbarIcon = getResources().getDrawable( fallbackDrawableId );
			}
		}
		else
		{
			toolbarIcon = getExternalPackageToolbarIcon( activityName , toolbarResourceName );
		}
		if( button != null )
		{
			// If we were unable to find the icon via the meta-data, use a
			// generic one
			if( toolbarIcon == null )
			{
				button.setImageResource( fallbackDrawableId );
			}
			else
			{
				button.setImageDrawable( toolbarIcon );
			}
		}
		return toolbarIcon != null ? toolbarIcon.getConstantState() : null;
	}
	
	private void updateTextButtonWithDrawable(
			int buttonId ,
			Drawable d )
	{
		TextView button = (TextView)findViewById( buttonId );
		button.setCompoundDrawables( d , null , null , null );
	}
	
	private void updateButtonWithDrawable(
			int buttonId ,
			Drawable.ConstantState d )
	{
		ImageView button = (ImageView)findViewById( buttonId );
		if( button != null )
			button.setImageDrawable( d.newDrawable( getResources() ) );
	}
	
	private void invalidatePressedFocusedStates(
			View container ,
			View button )
	{
		if( container instanceof HolographicLinearLayout )
		{
			HolographicLinearLayout layout = (HolographicLinearLayout)container;
			layout.invalidatePressedFocusedStates();
		}
	}
	
	public View getSearchBar()
	{
		if(
		// 
		( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE/*//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题*/)
		//
		&& mSearchBar == null
		//
		)
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.v( "SearchBar" , StringUtils.concat( "00 - SearchBarType:" , LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE ) );
				Log.v( "SearchBar" , StringUtils.concat( "01 - sdk:" , Build.VERSION.SDK_INT ) );
			}
			//xiatian add start	//适配5.1全局搜索（5.1的全局搜索是将AppWidgetHostView加到mSearchDropTargetBar中），5.1以下的全局搜索机制通不过5.1系统的cts。
			if(
			//
			LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_GLOBAL_SEARCH
			//
			&& ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 )
			//
			)
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.v( "SearchBar" , "02 - [SEARCH_BAR_STYLE_GLOBAL_SEARCH] and [>= Build.VERSION_CODES.LOLLIPOP_MR1] - to create widget search bar" );
				}
				AppWidgetProviderInfo searchProvider = Utilities.getSearchWidgetProvider( this );
				if( searchProvider == null )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.v( "SearchBar" , "03 - error[ searchProvider == null ]" );
					}
					return null;
				}
				Bundle opts = new Bundle();
				opts.putInt( AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY , AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX );
				SharedPreferences sp = getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , MODE_PRIVATE );
				int widgetId = sp.getInt( SEARCH_BAR_WIDGET_ID , -1 );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.v( "SearchBar" , StringUtils.concat( "04 - widgetId:" , widgetId ) );
				}
				AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo( widgetId );
				if(
				//
				!searchProvider.provider.flattenToString().equals( sp.getString( SEARCH_BAR_WIDGET_PROVIDER , null ) )
				//
				|| ( widgetInfo == null )
				//
				|| !widgetInfo.provider.equals( searchProvider.provider )
				//
				)
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.v( "SearchBar" , "05 - adjust" );
						if( !searchProvider.provider.flattenToString().equals( sp.getString( SEARCH_BAR_WIDGET_PROVIDER , null ) ) )
						{
							Log.v( "SearchBar" , StringUtils.concat( "05.0.0 - searchProvider.provider:" , searchProvider.provider.flattenToString() ) );
							Log.v( "SearchBar" , StringUtils.concat( "05.0.1 - sp.getString( SEARCH_BAR_WIDGET_PROVIDER , null ):" , sp.getString( SEARCH_BAR_WIDGET_PROVIDER , null ) ) );
						}
						else if( widgetInfo == null )
						{
							Log.v( "SearchBar" , "05.1 - [ widgetInfo == null ]" );
						}
						else
						{
							Log.v( "SearchBar" , StringUtils.concat( "05.2.0 - widgetInfo.provider:" , widgetInfo.provider.toString() ) );
							Log.v( "SearchBar" , StringUtils.concat( "05.2.1 - searchProvider.provider:" , searchProvider.provider.toString() ) );
						}
					}
					// A valid widget is not already bound.
					if( widgetId > -1 )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.v( "SearchBar" , "06 - del[ widgetId > -1 ]" );
						}
						mAppWidgetHost.deleteAppWidgetId( widgetId );
						widgetId = -1;
					}
					// Try to bind a new widget
					widgetId = mAppWidgetHost.allocateAppWidgetId();
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.v( "SearchBar" , StringUtils.concat( "07.0 - widgetId:" , widgetId ) );
						Log.v( "SearchBar" , StringUtils.concat( "07.1 - searchProvider.getProfile():" , searchProvider.getProfile().toString() ) );
						Log.v( "SearchBar" , StringUtils.concat( "07.2 - searchProvider.provider:" , searchProvider.provider.toString() ) );
						Log.v( "SearchBar" , StringUtils.concat( "07.3 - opts:" , opts.toString() ) );
					}
					if( !mAppWidgetManager.bindAppWidgetIdIfAllowed( widgetId , searchProvider.getProfile() , searchProvider.provider , opts ) )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.v( "SearchBar" , "08 - del[ bindAppWidget error ]" );
						}
						mAppWidgetHost.deleteAppWidgetId( widgetId );
						widgetId = -1;
					}
					sp.edit().putInt( SEARCH_BAR_WIDGET_ID , widgetId ).putString( SEARCH_BAR_WIDGET_PROVIDER , searchProvider.provider.flattenToString() ).commit();
				}
				if( widgetId != -1 )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.v( "SearchBar" , "09 - [ widgetId != -1 ] - to create widget search bar" );
					}
					mSearchBar = mAppWidgetHost.createView( this , widgetId , searchProvider );
					( (AppWidgetHostView)mSearchBar ).updateAppWidgetOptions( opts );
					mSearchBar.setPadding( 0 , 0 , 0 , 0 );
					mSearchDropTargetBar.addView( mSearchBar );
				}
				//cheyingkun add start	//修改运营酷搜逻辑(改为锁屏时桌面重启)
				else
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.v( "SearchBar" , "10 - [ widgetId == -1 ] - to create kuso search bar" );
					}
					mSearchBar = mInflater.inflate( R.layout.search_bar , mSearchDropTargetBar , false );
					mSearchDropTargetBar.addView( mSearchBar );
				}
				//cheyingkun add end
			}
			else
			//xiatian add end
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.v( "SearchBar" , "11 - [SEARCH_BAR_STYLE_GLOBAL_SEARCH] or [>= Build.VERSION_CODES.LOLLIPOP_MR1] - to create kuso search bar" );
				}
				mSearchBar = mInflater.inflate( R.layout.search_bar , mSearchDropTargetBar , false );
				mSearchDropTargetBar.addView( mSearchBar );
			}
		}
		return mSearchBar;
	}
	
	protected boolean updateSearchBarSearchButton()
	{
		final View searchButtonContainer = findViewById( R.id.search_button_container );
		final ImageView searchButton = (ImageView)findViewById( R.id.search_button );
		final View voiceButtonContainer = findViewById( R.id.voice_button_container );
		final View voiceButton = findViewById( R.id.voice_button );
		ComponentName activityName = null;
		//xiatian start	//添加配置项“config_search_bar_type”，搜索栏中搜索的配置参数。0为酷搜，1为安卓的全局搜索。默认为0。（详见BaseDefaultConfig.java中的“SEARCH_BAR_STYLE_XXX”）
		//		activityName = searchManager.getGlobalSearchActivity();//xiatian del
		//xiatian add start
		if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE )
		{//给activityName赋值，确保（1）不再调用getGlobalSearchActivity，（2）显示search button，（3）显示图片search_logo_google_bg
			activityName = new ComponentName( getPackageName() , "com.search.kuso.SearchT9Main" );
		}
		//xiatian add end
		//xiatian end
		else
		{
			final SearchManager searchManager = (SearchManager)getSystemService( Context.SEARCH_SERVICE );
			// gaominghui@2016/12/14 ADD START 兼容Android 4.0
			activityName = getGlobalSearchActivity( searchManager );
			// gaominghui@2016/12/14 ADD END  兼容Android 4.0
		}
		if( activityName != null )
		{
			int coi = getCurrentOrientationIndexForGlobalIcons();
			int mSearchBarSearchButtonIconFallbackId = R.drawable.search_bar_search_button_icon_fallback_selector;
			sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity( R.id.search_button , activityName , mSearchBarSearchButtonIconFallbackId , TOOLBAR_SEARCH_ICON_METADATA_NAME );
			if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_GLOBAL_SEARCH )
			{
				if( sGlobalSearchIcon[coi] == null )
				{
					sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity( R.id.search_button , activityName , mSearchBarSearchButtonIconFallbackId , TOOLBAR_ICON_METADATA_NAME );
				}
			}
			if( searchButtonContainer != null )
				searchButtonContainer.setVisibility( View.VISIBLE );
			if( searchButton != null )
				searchButton.setVisibility( View.VISIBLE );
			invalidatePressedFocusedStates( searchButtonContainer , searchButton );
			return true;
		}
		else
		{
			// We disable both search and voice search when there is no global search provider
			if( searchButtonContainer != null )
				searchButtonContainer.setVisibility( View.GONE );
			if( voiceButtonContainer != null )
				voiceButtonContainer.setVisibility( View.GONE );
			if( searchButton != null )
				searchButton.setVisibility( View.GONE );
			if( voiceButton != null )
				voiceButton.setVisibility( View.GONE );
			// zhujieping@2015/03/16 ADD START
			//当搜索框没有对应的应用时，搜索框不显示，sGlobalSearchIcon应为空
			int coi = getCurrentOrientationIndexForGlobalIcons();
			sGlobalSearchIcon[coi] = null;
			// zhujieping@2015/03/16 ADD END
			// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
			//			//WangLei start //bug:0010441 //当语音搜索图标不可用时，点击整个搜索框都相应onClickSearchButton
			//			//updateVoiceButtonProxyVisible( false ); //WangLei del
			//			updateVoiceButtonProxyVisible( true ); //WangLei add
			//			//WangLei end
			// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
			return false;
		}
	}
	
	protected void updateSearchBarSearchButton(
			Drawable.ConstantState d )
	{
		if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE )
		{
			return;
		}
		final View searchButtonContainer = findViewById( R.id.search_button_container );
		final View searchButton = (ImageView)findViewById( R.id.search_button );
		updateButtonWithDrawable( R.id.search_button , d );
		invalidatePressedFocusedStates( searchButtonContainer , searchButton );
	}
	
	protected boolean updateSearchBarVoiceButton(
			boolean searchVisible )
	{
		//cheyingkun add start	//搜索栏是否支持显示语音搜索的按钮。true为支持；false为不支持。默认true。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_SHOW_VOICE_BUTTON == false )
		{
			return false;
		}
		//cheyingkun add end
		final View voiceButtonContainer = findViewById( R.id.voice_button_container );
		final View voiceButton = findViewById( R.id.voice_button );
		// We only show/update the voice search icon if the search icon is enabled as well
		ComponentName activityName = null;
		if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE )
		{//给activityName赋值，确保（1）不再调用getGlobalSearchActivity，（2）显示voice button，（3）显示图片search_bar_voice_button_icon_fallback
			activityName = new ComponentName( getPackageName() , "com.search.kuso.SearchT9Main" );
		}
		else
		{
			final SearchManager searchManager = (SearchManager)getSystemService( Context.SEARCH_SERVICE );
			// gaominghui@2016/12/14 UPD START
			//ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();
			ComponentName globalSearchActivity = getGlobalSearchActivity( searchManager );
			// gaominghui@2016/12/14 UPD END
			if( globalSearchActivity != null )
			{
				// Check if the global search activity handles voice search
				Intent intent = new Intent( RecognizerIntent.ACTION_WEB_SEARCH );
				intent.setPackage( globalSearchActivity.getPackageName() );
				activityName = intent.resolveActivity( getPackageManager() );
			}
			if( activityName == null )
			{
				// Fallback: check if an activity other than the global search activity
				// resolves this
				Intent intent = new Intent( RecognizerIntent.ACTION_WEB_SEARCH );
				activityName = intent.resolveActivity( getPackageManager() );
			}
		}
		if( searchVisible && activityName != null )
		{
			int mSearchBarVoiceButtonIconFallbackId = R.drawable.search_bar_voice_button_icon_fallback;
			int coi = getCurrentOrientationIndexForGlobalIcons();
			sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity( R.id.voice_button , activityName , mSearchBarVoiceButtonIconFallbackId , TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME );
			if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_GLOBAL_SEARCH )
			{
				if( sVoiceSearchIcon[coi] == null )
				{
					sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity( R.id.voice_button , activityName , mSearchBarVoiceButtonIconFallbackId , TOOLBAR_ICON_METADATA_NAME );
				}
			}
			if( voiceButtonContainer != null )
				voiceButtonContainer.setVisibility( View.VISIBLE );
			if( voiceButton != null )
				voiceButton.setVisibility( View.VISIBLE );
			// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
			//			updateVoiceButtonProxyVisible( false );
			// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
			invalidatePressedFocusedStates( voiceButtonContainer , voiceButton );
			return true;
		}
		else
		{
			if( voiceButtonContainer != null )
				voiceButtonContainer.setVisibility( View.GONE );
			if( voiceButton != null )
				voiceButton.setVisibility( View.GONE );
			// zhujieping@2015/03/20 ADD START
			//当搜索框没有对应的应用时，搜索框不显示，sVoiceSearchIcon应为空
			int coi = getCurrentOrientationIndexForGlobalIcons();
			sVoiceSearchIcon[coi] = null;
			// zhujieping@2015/03/20 ADD END
			// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
			//			//WangLei start //bug:0010441 //当语音搜索图标不可用时，点击整个搜索框都相应onClickSearchButton
			//			//updateVoiceButtonProxyVisible( false ); //WangLei del
			//			updateVoiceButtonProxyVisible( true ); //WangLei add
			//			//WangLei end
			// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
			return false;
		}
	}
	
	protected void updateSearchBarVoiceButton(
			Drawable.ConstantState d )
	{
		if( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE )
		{
			return;
		}
		final View voiceButtonContainer = findViewById( R.id.voice_button_container );
		final View voiceButton = findViewById( R.id.voice_button );
		updateButtonWithDrawable( R.id.voice_button , d );
		invalidatePressedFocusedStates( voiceButtonContainer , voiceButton );
	}
	// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
	//	public void updateVoiceButtonProxyVisible(
	//			boolean forceDisableVoiceButtonProxy )
	//	{
	//		final View voiceButtonProxy = findViewById( R.id.voice_button_proxy );
	//		if( voiceButtonProxy != null )
	//		{
	//			boolean visible = !forceDisableVoiceButtonProxy && mWorkspace.shouldVoiceButtonProxyBeVisible();
	//			voiceButtonProxy.setVisibility( visible ? View.VISIBLE : View.GONE );
	//			voiceButtonProxy.bringToFront();
	//		}
	//	}
	//
	//	/**
	//	 * This is an overrid eot disable the voice button proxy.  If disabled is true, then the voice button proxy
	//	 * will be hidden regardless of what shouldVoiceButtonProxyBeVisible() returns.
	//	 */
	//	public void disableVoiceButtonProxy(
	//			boolean disabled )
	//	{
	//		updateVoiceButtonProxyVisible( disabled );
	//	}
	// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
	;
	
	/**
	 * Sets the app market icon
	 */
	private void updateAppMarketIcon()
	{
		if( SHOW_MARKET_BUTTON )
		{
			final View marketButton = findViewById( R.id.market_button );
			Intent intent = new Intent( Intent.ACTION_MAIN ).addCategory( Intent.CATEGORY_APP_MARKET );
			// Find the app market activity by resolving an intent.
			// (If multiple app markets are installed, it will return the ResolverActivity.)
			ComponentName activityName = intent.resolveActivity( getPackageManager() );
			if( activityName != null )
			{
				int coi = getCurrentOrientationIndexForGlobalIcons();
				mAppMarketIntent = intent;
				sAppMarketIcon[coi] = updateTextButtonWithIconFromExternalActivity( R.id.market_button , activityName , R.drawable.market_icon_in_app_tab_host , TOOLBAR_ICON_METADATA_NAME );
				marketButton.setVisibility( View.VISIBLE );
				marketButton.setEnabled( true );//xiatian add	//fix bug：解决“在主菜单支持显示'应用市场'的前提下，在'应用市场'图标消失时安装带有'应用市场'的应用，'应用市场'图标出现后不响应点击事件”的问题。
			}
			else
			{
				// We should hide and disable the view so that we don't try and restore the visibility
				// of it when we swap between drag & normal states from IconDropTarget subclasses.
				marketButton.setVisibility( View.GONE );
				marketButton.setEnabled( false );
			}
		}
	}
	
	private void updateAppMarketIcon(
			Drawable.ConstantState d )
	{
		if( SHOW_MARKET_BUTTON )
		{
			// Ensure that the new drawable we are creating has the approprate toolbar icon bounds
			Resources r = getResources();
			Drawable marketIconDrawable = d.newDrawable( r );
			int w = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.toolbar_external_icon_width );
			int h = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.toolbar_external_icon_height );
			marketIconDrawable.setBounds( 0 , 0 , w , h );
			updateTextButtonWithDrawable( R.id.market_button , marketIconDrawable );
		}
	}
	
	/**
	 * Receives notifications when system dialogs are to be closed.
	 */
	private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver
	{
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			closeSystemDialogs();
		}
	}
	
	/**
	 * Receives notifications whenever the appwidgets are reset.
	 */
	private class AppWidgetResetObserver extends ContentObserver
	{
		
		public AppWidgetResetObserver()
		{
			super( new Handler() );
		}
		
		@Override
		public void onChange(
				boolean selfChange )
		{
			onAppWidgetReset();
		}
	}
	
	/**
	 * If the activity is currently paused, signal that we need to run the passed Runnable
	 * in onResume.
	 *
	 * This needs to be called from incoming places where resources might have been loaded
	 * while we are paused.  That is becaues the Configuration might be wrong
	 * when we're not running, and if it comes back to what it was when we
	 * were paused, we are not restarted.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 *
	 * @return true if we are currently paused.  The caller might be able to
	 * skip some work in that case since we will come back again.
	 */
	private boolean waitUntilResume(
			Runnable run ,
			boolean deletePreviousRunnables )
	{
		if( mPaused )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "Deferring update until onResume" );
			if( deletePreviousRunnables )
			{
				while( mBindOnResumeCallbacks.remove( run ) )
				{
				}
			}
			mBindOnResumeCallbacks.add( run );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean waitUntilResume(
			Runnable run )
	{
		return waitUntilResume( run , false );
	}
	
	public void addOnResumeCallback(
			Runnable run )
	{
		mOnResumeCallbacks.add( run );
	}
	
	/**
	 * If the activity is currently paused, signal that we need to re-run the loader
	 * in onResume.
	 *
	 * This needs to be called from incoming places where resources might have been loaded
	 * while we are paused.  That is becaues the Configuration might be wrong
	 * when we're not running, and if it comes back to what it was when we
	 * were paused, we are not restarted.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 *
	 * @return true if we are currently paused.  The caller might be able to
	 * skip some work in that case since we will come back again.
	 */
	public boolean setLoadOnResume()
	{
		if( mPaused )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "setLoadOnResume" );
			mOnResumeNeedsLoad = true;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public int getCurrentWorkspaceScreen()
	{
		if( mWorkspace != null )
		{
			return mWorkspace.getCurrentPage();
		}
		else
		{
			return SCREEN_COUNT / 2;
		}
	}
	
	/**
	 * Refreshes the shortcuts shown on the workspace.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void startBinding()
	{
		// If we're starting binding all over again, clear any bind calls we'd postponed in
		// the past (see waitUntilResume) -- we don't need them since we're starting binding
		// from scratch again
		mBindOnResumeCallbacks.clear();
		// Clear the workspace because it's going to be rebound
		mWorkspace.clearDropTargets();
		mWorkspace.removeAllWorkspaceScreens();
		mWidgetsToAdvance.clear();
		if( mHotseat != null )
		{
			mHotseat.resetLayout();
		}
	}
	
	@Override
	public void bindScreens(
			ArrayList<Long> orderedScreenIds )
	{
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcher bindScreens: orderedScreenIds " , orderedScreenIds ) );
		}
		//cheyingkun add end
		bindAddScreens( orderedScreenIds );
		// If there are no screens, we need to have an empty screen
		if( orderedScreenIds.size() == 0 )
		{
			mWorkspace.addExtraEmptyScreen();
		}
		// Create the custom content page (this call updates mDefaultScreen which calls
		// setCurrentPage() so ensure that all pages are added before calling this).
		// The actual content of the custom page will be added during onFinishBindingItems().
		if( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES && !mWorkspace.hasFavoritesPage() )
		{
			mWorkspace.createAndAddFavoritesPage();
		}
		// YANGTIANYU@2016/06/17 ADD START
		// 加相机页
		if( LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW && !mWorkspace.hasCameraPage() )
		{
			mWorkspace.createAndAddCameraPage();
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW && !mWorkspace.hasMusicPage() )
		{
			mWorkspace.createAndAddMusicPage();
		}
		// YANGTIANYU@2016/06/17 ADD END
	}
	
	@Override
	public void bindAddScreens(
			ArrayList<Long> orderedScreenIds )
	{
		int count = orderedScreenIds.size();
		for( int i = 0 ; i < count ; i++ )
		{
			if( mWorkspace.canInsertNewScreen() )//xiatian add	//限制桌面最大页数
			{
				mWorkspace.insertNewWorkspaceScreenBeforeEmptyScreen( orderedScreenIds.get( i ) );
			}
		}
	}
	
	private boolean shouldShowWeightWatcher()
	{
		String spKey = LauncherAppState.getSharedPreferencesKey();
		SharedPreferences sp = getSharedPreferences( spKey , Context.MODE_PRIVATE );
		boolean show = sp.getBoolean( SHOW_WEIGHT_WATCHER , SHOW_WEIGHT_WATCHER_DEFAULT );
		return show;
	}
	
	private void toggleShowWeightWatcher()
	{
		String spKey = LauncherAppState.getSharedPreferencesKey();
		SharedPreferences sp = getSharedPreferences( spKey , Context.MODE_PRIVATE );
		boolean show = sp.getBoolean( SHOW_WEIGHT_WATCHER , true );
		show = !show;
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean( SHOW_WEIGHT_WATCHER , show );
		editor.commit();
		if( mWeightWatcher != null )
		{
			mWeightWatcher.setVisibility( show ? View.VISIBLE : View.GONE );
		}
	}
	
	/**
	 * @param isInstallApp 是否是安装操作。安装应用时，需要做一些预置操作，比如关文件夹、退出编辑模式等。
	 * 						通过这个标志位控制是否需要进行预置操作 
	 */
	public void bindItemsAdded(
			final ArrayList<Long> newScreens ,
			final ArrayList<ItemInfo> addNotAnimated ,
			final ArrayList<ItemInfo> addAnimated ,
			final ArrayList<AppInfo> addedItems ,
			final boolean isInstallApp ,
			final boolean isLoadFinish )//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				bindItemsAdded( newScreens , addNotAnimated , addAnimated , addedItems , isInstallApp , isLoadFinish );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , "cyk launcher bindItemsAdded " );
		}
		//cheyingkun add end
		if( isInstallApp )//cheyingkun add	//解决“桌面启动后立即点击打开文件夹，文件夹打开后，过会会自动关闭”的问题。【i_0014084】
		{
			//WangLei add start //bug:0010374 //安装新应用时退出编辑模式
			if(
			//
			( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE/* //xiatian add	//需求：在双层模式时，在编辑模式下安装应用时，不退出编辑模式。 */)
			//
			&& ( mWorkspace != null && mWorkspace.isInOverviewMode() )
			//
			)
			{
				//xiatian start	//fix bug：解决“在双层模式下，在编辑模式长按一个页面时，pc端安装应用，安装成功之后，桌面自动退出编辑模式，但此时被长按的页面所有图标呈托起状态”的问题。【i_0010545】
				//【备注】目前bindAppsAdded时，会跳到安装应用的界面后播放动画显示安装应用，所以在退出编辑模式下托起页面的状态时，放下页面后不要“自动滑动到指定页面”或“自动滑动到最近的页面”。
				//			mWorkspace.exitOverviewMode( false );//xiatian del
				mWorkspace.exitOverviewModeIfInReordering( false , false );//xiatian add
				//xiatian end
			}
			//WangLei add end
			//WangLei add start //bug:0010086 //安装新应用时关闭打开的文件夹
			if( mWorkspace != null )
			{
				closeFolder();
				// YANGTIANYU@2016/07/19 ADD START
				// 安装完新应用后会跳转到新应用所在的页面,如果当前页为相机页且正在预览,需要关闭预览界面
				if( mWorkspace.isCameraPage( mWorkspace.getCurrentPage() ) )
				{
					CameraView.getInstance().stopCamera();
				}
				// YANGTIANYU@2016/07/19 ADD END
			}
			//WangLei add end
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_INSTALL_OPERATEICON_INFOLDER && addedItems != null )//当新安装的应用是运营文件夹中的，同时打开此开关，直接更新运营文件夹中的数据，并删除掉要添加在桌面的数据
		{
			if( mOperateDynamicMain != null )
			{
				mOperateDynamicMain.updateAddedIconInOperateFolder( addNotAnimated , addAnimated , addedItems );
			}
		}
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , "cyk launcher bindItemsAdded: bindAddScreens " );
		}
		//cheyingkun add end
		// Add the new screens
		bindAddScreens( newScreens );
		// We add the items without animation on non-visible pages, and with
		// animations on the new page (which we will try and snap to).
		if( !addNotAnimated.isEmpty() )
		{
			bindItems( addNotAnimated , 0 , addNotAnimated.size() , false , null , isLoadFinish );//zhujieping modify,接口更新
		}
		if( !addAnimated.isEmpty() )
		{
			bindItems( addAnimated , 0 , addAnimated.size() , true , null , isLoadFinish );//zhujieping modify
		}
		// Remove the extra empty screen
		mWorkspace.removeExtraEmptyScreen();
		//xiatian start	//解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”前提下，加载item的时候，发现并删除位于功能页的前一页的空白默认页（默认页数大于桌面页面数）后，当前页面没有跳到默认页面（第0页）”的问题。
		//		mWorkspace.stripEmptyScreens();//xiatian del
		mWorkspace.stripEmptyScreens( isLoadFinish );//xiatian add
		//xiatian end
		//<i_0010072> liuhailin@2015-03-04 modify begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && addedApps != null && mAppsCustomizeContent != null )
		if( addedItems != null && mAppsCustomizeContent != null )
		//<i_0010072> liuhailin@2015-03-04 modify end
		{
			mAppsCustomizeContent.addApps( addedItems );
		}
		// zhangjin@2016/05/06 ADD START
		if(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		{
			if( addedItems != null && mAppsView != null )
			{
				mAppsView.addApps( addedItems );
			}
		}
		// zhangjin@2016/05/06 ADD END
		if( mOperateDynamicMain != null && addedItems != null /*&& (  LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )*/)
		{
			mOperateDynamicMain.removeFolderIcon( addedItems );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "lvjiangbin" , "bindItemsAdded" );
		if( mWorkspace.hasFavoritesPage() )
		{
			//cheyingkun add start	//phenix1.1稳定版移植酷生活
			HashMap<ComponentName , Bitmap> map = mModel.getAllIcons();
			FavoritesPageManager.getInstance( this ).setAllApp( map );
			//cheyingkun add end
		}
	}
	
	/**
	 * Bind the items start-end from the list.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindItems(
			final ArrayList<ItemInfo> shortcuts ,
			final int start ,
			final int end ,
			final boolean forceAnimateIcons ,
			final Runnable runnable ,
			final boolean isLoadFinish )//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
	{
		// zhujieping@2015/06/03 UPD START
		Runnable r = null;
		if( runnable != null )
			r = runnable;
		else
			r = new Runnable() {
				
				public void run()
				{
					bindItems( shortcuts , start , end , forceAnimateIcons , null , isLoadFinish );
				}
			};
		// zhujieping@2015/06/03 UPD END
		if( waitUntilResume( r ) )
		{
			return;
		}
		// Get the list of added shortcuts and intersect them with the set of shortcuts here
		final AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
		final Collection<Animator> bounceAnims = new ArrayList<Animator>();
		final boolean animateIcons = forceAnimateIcons && canRunNewAppsAnimation()// 
				&& ( isLoadFinish || LauncherDefaultConfig.SWITCH_ENABLE_BIND_ITEMS_ANIMATE_IN_LOADING ) && hasWindowFocus();//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页//add hasWindowFocus  避免一键换壁纸时候滑页
		Workspace workspace = mWorkspace;
		long newShortcutsScreenId = -1;
		for( int i = start ; i < end ; i++ )
		{
			final ItemInfo item = shortcuts.get( i );
			// Short circuit if we are loading dock items for a configuration which has no dock
			if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT && mHotseat == null )
			{
				//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "cyk_bug : c_0003400" , "cyk launcher bindItems:  item.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT && mHotseat == null  " );
				}
				//cheyingkun add end
				continue;
			}
			switch( item.getItemType() )
			{
				case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
				case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
				case LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL://xiatian add	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
					ShortcutInfo info = (ShortcutInfo)item;
					View shortcut = createShortcut( info );
					//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( "cyk_bug : c_0003400" , "cyk launcher bindItems: info " + info );
					}
					//cheyingkun add end
					mWorkspace.checkItemIsOccupied( item );// 编辑模式点击百度时钟添加至新页面，此时安装应用时候，应用安装成功后再点击任意时钟样式，此时在安装应用算好的空位此时已经被小组件占据，原先处理逻辑为直接抛出异常让launcher重启，现在改为，当被占据以后，再去查找最新的位置并显示 wanghongjian add【bug：i_0012037】
					//wanghongjian del start【bug：i_0012037】
					/*
					 * TODO: FIX collision case
					 */
					//					if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP )
					//					{
					//						CellLayout cl = mWorkspace.getScreenWithId( item.getScreenId() );
					//						if( cl != null && cl.isOccupied( item.getCellX() , item.getCellY() ) )
					//						{
					//							throw new RuntimeException( "OCCUPIED" );
					//						}
					//					}
					//wanghongjian del end 【bug：i_0012037】
					if( info.isOperateIconItem() && info.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP )
					{
						if( OperateDynamicMain.getmOldOperateFolderVersion() == null )
						{
							String version = info.getIntent().getStringExtra( OperateDynamicMain.FOLDER_VERSION );
							OperateDynamicMain.setmOldOperateFolderVersion( version );
						}
						info.setShortcutType( LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC );
						OperateDynamicMain.updateOperateIcons( shortcut );
						OperateDynamicMain.addShortcutToAllOperateIcon( info );
					}
					workspace.addInScreenFromBind( shortcut , item.getContainer() , item.getScreenId() , item.getCellX() , item.getCellY() , 1 , 1 );
					//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】
					//添加特定快捷方式时,修改设置界面的开关值
					if( item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT && item instanceof ShortcutInfo )
					{
						ShortcutInfo mShortcutInfo = (ShortcutInfo)item;
						ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( mLauncher );
						if( mShortcutInfo.getIntent() != null && mShortcutInfo.getIntent().getComponent() != null //cheyingkun add	//解决“添加插件后删除，桌面重启”的问题【i_0014438】(非空判断)
								&& mZhiKeShortcutManager.isZhiKeShortcut( mShortcutInfo.getIntent().getComponent() )// 
						)
						{
							String zhikeShortcutSettingKey = mZhiKeShortcutManager.getZhikeShortcutSettingKey( mShortcutInfo.getIntent() );
							mZhiKeShortcutManager.setZhikeShortcutSettingValues( zhikeShortcutSettingKey , 1 );
						}
					}
					//cheyingkun add end
					if( animateIcons )
					{
						// Animate all the applications up now
						shortcut.setAlpha( 0f );
						shortcut.setScaleX( 0f );
						shortcut.setScaleY( 0f );
						bounceAnims.add( createNewAppBounceAnimation( shortcut , i ) );
						newShortcutsScreenId = item.getScreenId();
					}
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
					FolderInfo mFolderInfo = (FolderInfo)item;
					//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( "cyk_bug : c_0003400" , "cyk launcher bindItems: mFolderInfo " + mFolderInfo );
					}
					//cheyingkun add end
					if( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER.equals( mFolderInfo.getOperateIntent().getAction() ) )
					{
						mFolderInfo.setFolderType( LauncherSettings.Favorites.FOLDER_TYPE_OPERATE_DYNAMIC );
					}
					FolderIcon newFolder = FolderIcon.fromXml( R.layout.folder_icon , this , (ViewGroup)workspace.getChildAt( workspace.getCurrentPage() ) , (FolderInfo)item , mIconCache );
					workspace.checkItemIsOccupied( mFolderInfo );
					workspace.addInScreenFromBind( newFolder , mFolderInfo.getContainer() , mFolderInfo.getScreenId() , mFolderInfo.getCellX() , mFolderInfo.getCellY() , 1 , 1 );
					ArrayList<ShortcutInfo> contents = mFolderInfo.getContents();
					//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】
					//添加特定快捷方式时,修改设置界面的开关值
					for( ShortcutInfo shortcutInfo : contents )
					{
						ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( mLauncher );
						if( shortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT //
								&& shortcutInfo.getIntent() != null && shortcutInfo.getIntent().getComponent() != null//cheyingkun add	//解决“添加插件后删除，桌面重启”的问题【i_0014438】(非空判断)
								&& mZhiKeShortcutManager.isZhiKeShortcut( shortcutInfo.getIntent().getComponent() )//
						)
						{
							String zhikeShortcutSettingKey = mZhiKeShortcutManager.getZhikeShortcutSettingKey( shortcutInfo.getIntent() );
							mZhiKeShortcutManager.setZhikeShortcutSettingValues( zhikeShortcutSettingKey , 1 );
						}
					}
					//cheyingkun add end
					if( mFolderInfo.getFolderType() == LauncherSettings.Favorites.FOLDER_TYPE_OPERATE_DYNAMIC )
					{
						//这部分挪到文件夹中,zhujieping
						//						if( OperateDynamicMain.getmOldOperateFolderVersion() == null )
						//						{
						//							String version = ( (FolderInfo)item ).getOperateIntent().getStringExtra( OperateDynamicMain.FOLDER_VERSION );
						//							OperateDynamicMain.setmOldOperateFolderVersion( version );
						//						}
						OperateDynamicMain.getmAllOperateFolderIcons().add( newFolder );
						//						OperateDynamicMain.addShortcutToAllOperateIcon( ( (FolderInfo)item ) );
					}
					//xiatian add start	//添加保护，以防mFolderInfo没有添加到sFolders中
					if( sFolders.containsKey( mFolderInfo.getId() ) == false )
					{
						sFolders.put( mFolderInfo.getId() , mFolderInfo );
					}
					//xiatian add end
					//xiatian add start	//添加文件夹时，也支持动画
					if( animateIcons )
					{
						// animate all the folders up now
						newFolder.setAlpha( 0f );
						newFolder.setScaleX( 0f );
						newFolder.setScaleY( 0f );
						bounceAnims.add( createNewAppBounceAnimation( newFolder , i ) );
						newShortcutsScreenId = item.getScreenId();
					}
					//xiatian add end
					break;
				default:
					//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( "cyk_bug : c_0003400" , "cyk launcher bindItems: Invalid Item Type " );
					}
					//cheyingkun add end
					throw new RuntimeException( "Invalid Item Type" );
			}
		}
		if( animateIcons )
		{
			// Animate to the correct page
			if( newShortcutsScreenId > -1 )
			{
				long currentScreenId = mWorkspace.getScreenIdForPageIndex( mWorkspace.getNextPage() );
				final int newScreenIndex = mWorkspace.getPageIndexForScreenId( newShortcutsScreenId );
				final Runnable startBounceAnimRunnable = new Runnable() {
					
					public void run()
					{
						anim.playTogether( bounceAnims );
						anim.start();
					}
				};
				if( newShortcutsScreenId != currentScreenId )
				{
					// We post the animation slightly delayed to prevent slowdowns
					// when we are loading right after we return to launcher.
					mWorkspace.postDelayed( new Runnable() {
						
						public void run()
						{
							mWorkspace.snapToPage( newScreenIndex );
							mWorkspace.postDelayed( startBounceAnimRunnable , NEW_APPS_ANIMATION_DELAY );
						}
					} , NEW_APPS_PAGE_MOVE_DELAY );
				}
				else
				{
					mWorkspace.postDelayed( startBounceAnimRunnable , NEW_APPS_ANIMATION_DELAY );
				}
			}
		}
		//xiatian add start	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
		UnreadHelper mUnreadHelper = LauncherAppState.getInstance().getUnreadHelper();
		if( mUnreadHelper != null )
		{
			mUnreadHelper.onChangeBySelf();
		}
		//xiatian add end
		workspace.requestLayout();
	}
	
	/**
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindFolders(
			final HashMap<Long , FolderInfo> folders )
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				bindFolders( folders );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		sFolders.clear();
		sFolders.putAll( folders );
	}
	
	/**
	 * Add the views for a widget to the workspace.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAppWidget(
			final LauncherAppWidgetInfo item )
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				bindAppWidget( item );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
		if( DEBUG_WIDGETS )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "bindAppWidget: " + item );
		}
		final Workspace workspace = mWorkspace;
		final int appWidgetId = item.getAppWidgetId();
		final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo( appWidgetId );
		if( DEBUG_WIDGETS )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "bindAppWidget: id=" , item.getAppWidgetId() , " belongs to component " , appWidgetInfo.provider.toString() ) );
		}
		item.setAppWidgetHostView( mAppWidgetHost.createView( this , appWidgetId , appWidgetInfo ) );
		item.getAppWidgetHostView().setTag( item );
		item.onBindAppWidget( this );
		workspace.addInScreen( item.getAppWidgetHostView() , item.getContainer() , item.getScreenId() , item.getCellX() , item.getCellY() , item.getSpanX() , item.getSpanY() , false );
		addWidgetToAutoAdvanceIfNeeded( item.getAppWidgetHostView() , appWidgetInfo );
		workspace.requestLayout();
		if( DEBUG_WIDGETS )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "bound widget id=" , item.getAppWidgetId() , " in " , ( SystemClock.uptimeMillis() - start ) , "ms" ) );
		}
	}
	
	public void onPageBoundSynchronously(
			int page )
	{
		mSynchronouslyBoundPages.add( page );
	}
	
	/**
	 * Callback saying that there aren't any more items to bind.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void finishBindingItems(
			final boolean upgradePath )
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				finishBindingItems( upgradePath );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		if( mSavedState != null )
		{
			if( !mWorkspace.hasFocus() )
			{
				mWorkspace.getChildAt( mWorkspace.getCurrentPage() ).requestFocus();
			}
			mSavedState = null;
		}
		mWorkspace.restoreInstanceStateForRemainingPages();
		// If we received the result of any pending adds while the loader was running (e.g. the
		// widget configuration forced an orientation change), process them now.
		for( int i = 0 ; i < sPendingAddList.size() ; i++ )
		{
			completeAdd( sPendingAddList.get( i ) );
		}
		sPendingAddList.clear();
		// Update the market app icon as necessary (the other icons will be managed in response to
		// package changes in bindSearchablesChanged()
		if( SHOW_MARKET_BUTTON )
		{
			updateAppMarketIcon();
		}
		//mWorkspaceLoading = false; //WangLei del //0010961 //在桌面图标没有完全加载出来之前，禁止长按事件
		if( upgradePath )
		{
			mWorkspace.getUniqueComponents( true , null );
			mIntentsOnWorkspaceFromUpgradePath = mWorkspace.getUniqueComponents( true , null );
		}
		mWorkspace.post( new Runnable() {
			
			@Override
			public void run()
			{
				onFinishBindingItems();
			}
		} );
		//chenliang del start	//解决“打开‘switch_enable_overview_show_pageIndicator’开关后，桌面加载完成后第一次进入编辑模式页面指示器的位置会变化”的问题。【c_0004625】
		//		//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
		//		{
		//			LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().initPageIndicatorY( this );
		//		}
		//		//cheyingkun add end
		//chenliang del end
		//xiatian add start	//解决“双层，默认主页配置1并且第一页配置默认配置的item，加载桌面后，多了一个空白页面”的问题。（没有走到bindItemsAdded中stripEmptyScreens）
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		{
			mWorkspace.stripEmptyScreens();
		}
		//xiatian add end
	}
	
	public boolean isAllAppsButtonRank(
			int rank )
	{
		if( mHotseat != null )
		{
			return mHotseat.isAllAppsButtonRank( rank );
		}
		return false;
	}
	
	private boolean canRunNewAppsAnimation()
	{
		long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
		return diff > ( NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000 );
	}
	
	private ValueAnimator createNewAppBounceAnimation(
			View v ,
			int i )
	{
		ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(
				v ,
				PropertyValuesHolder.ofFloat( "alpha" , 1f ) ,
				PropertyValuesHolder.ofFloat( "scaleX" , 1f ) ,
				PropertyValuesHolder.ofFloat( "scaleY" , 1f ) );
		bounceAnim.setDuration( InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION );
		bounceAnim.setStartDelay( i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY );
		bounceAnim.setInterpolator( new SmoothPagedView.OvershootInterpolator() );
		return bounceAnim;
	}
	
	@Override
	public void bindSearchablesChanged()
	{
		//cheyingkun add start	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		if( !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
		{
			return;
		}
		//cheyingkun add end
		boolean searchVisible = updateSearchBarSearchButton();
		boolean voiceVisible = updateSearchBarVoiceButton( searchVisible );
		if( mSearchDropTargetBar != null )
		{
			mSearchDropTargetBar.onSearchPackagesChanged( searchVisible , voiceVisible );
		}
		//xiatian start	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
		//xiatian del start
		//		//zhujieping,搜索栏变化，重新布局
		//		LauncherAppState app = LauncherAppState.getInstance();
		//		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//		if( getSearchBar().getBackground() != null )
		//		{
		//			grid.setSearchBarSpcaseHeight( true , getStatusBarHeight( false ) );
		//		}
		//		else
		//		{
		//			grid.setSearchBarSpcaseHeight( false , getStatusBarHeight( false ) );
		//		}
		//		grid.layout( this );
		//xiatian del end
		changeGlobalSearchToKuSoIfNecessary();//xiatian	add
		//xiatian end
	}
	
	/**
	 * Add the icons for all apps.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAllApplications(
			final ArrayList<AppInfo> apps )
	{
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			if( mIntentsOnWorkspaceFromUpgradePath != null )
			{
				if( LauncherModel.UPGRADE_USE_MORE_APPS_FOLDER )
				{
					getHotseat().addAllAppsFolder( mIconCache , apps , mIntentsOnWorkspaceFromUpgradePath , Launcher.this , mWorkspace );
				}
				mIntentsOnWorkspaceFromUpgradePath = null;
			}
		}
		if( mAppsCustomizeContent != null )
		{
			mAppsCustomizeContent.setApps( apps );
		}
		// zhangjin@2016/05/06 ADD START
		if(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		{
			if( mAppsView != null )
			{
				mAppsView.setApps( apps );
			}
		}
		// zhangjin@2016/05/06 ADD END
		mWorkspaceLoading = false; //WangLei add start //0010961 //在桌面图标没有完全加载出来之前，禁止长按事件
		hideAppListLoadHint();//cheyingkun add	//加载主菜单提示信息【c_0003106】
		if( mOperateDynamicMain == null )
		{
			mOperateDynamicMain = new OperateDynamicMain( this , this , mModel );
			if( OperateDynamicProxy.context != null )
			{
				OperateDynamicProxy.getInstance().start( mOperateDynamicMain.getOperateDynamicModel() );
			}
		}
		updateOperateFolder();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "PROCEDURE TIME_LOADED_ALLAPP:" , ( System.currentTimeMillis() - sTime_applicationCreateStart ) ) );
		}
	}
	
	public void updateOperateFolder()
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				updateOperateFolder();
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( mOperateDynamicMain.isExitOperateFolderDB() )
				{
					final String operateFolderVersion = mOperateDynamicMain.findOperateFolderVersion();
					runOnUiThread( new Runnable() {
						
						public void run()
						{
							exitOverviewMode();
						}
					} );
					//因为这之中判断条件要等到binditem之后才能得到，所以跟binditem保持一致
					if( OperateDynamicMain.getmOldOperateFolderVersion() == null/*为null表示此时launcher中的favorite数据库中没有运营文件夹的信息*/)
					{
						// TODO Auto-generated method stub
						if( !mOperateDynamicMain.isOperateFolderRunning() )//true说明正在运营文件夹正在加载过程中
						{
							mOperateDynamicMain.setOperateFolderRunning( true );
							OperateDynamicMain.setmOldOperateFolderVersion( operateFolderVersion );
							mOperateDynamicMain.addOperateFolderFromDb();//此流程不删除原先favorite中的数据库信息
						}
					}
					else
					{
						if( !OperateDynamicMain.getmOldOperateFolderVersion().equals( operateFolderVersion ) || mOperateDynamicMain.isShortcutSizeChange() )
						{
							//此块流程与从服务器解析数据生成folderInfolist并加载到launcher上的流程一直
							// TODO Auto-generated method stub
							if( !mOperateDynamicMain.isOperateFolderRunning() )//true说明正在运营文件夹正在加载过程中
							{
								mOperateDynamicMain.setOperateFolderRunning( true );
								ArrayList<ItemInfo> allFolderInfos = mOperateDynamicMain.getFolderInfoListByOperateDB();
								mOperateDynamicMain.deleteFolderAllDataBase();//这个是从运营文件夹的数据库中加载，避免有些数据没有被删除，导致重复
								boolean isUpdate = mOperateDynamicMain.clearAndAddFolderByFolderList( Launcher.this , allFolderInfos );
								if( !isUpdate )
								{
									OperateFolderDatabase.addFolderInfosToDatabase( Launcher.this , LauncherAppState.getLauncherProvider().getProviderDB() , allFolderInfos );
								}
							}
						}
					}
				}
			}
		} ).start();
	}
	
	@Override
	public boolean isAllowToOperate()
	{
		boolean isAllowToOperate = true;
		if( mDragController != null )
		{
			isAllowToOperate = !mDragController.isDragging();
		}
		return isAllowToOperate;
	}
	
	@Override
	public void removeEmptyScreen()
	{
		if( mWorkspace != null )
		{
			mWorkspace.stripEmptyScreens();
		}
	}
	
	@Override
	public ArrayList<Long> getScreenOrder()
	{
		ArrayList<Long> order = new ArrayList<Long>();
		if( mWorkspace != null )
		{
			//			order.addAll( mWorkspace.getScreenOrder() );
			order.addAll( mWorkspace.getScreensWhichCanAddShortcut() );//这里是返回给运营文件夹的，要移除custompage和empty，否则运营文件夹添加在custompage上，出不来
		}
		return order;
	}
	
	@Override
	public void bindOperateFoloderEnd()
	{
		if( OperateHelp.getInstance( this ) != null )
		{
			if( OperateHelp.getInstance( this ).hasStartOperateFolderRun() )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , "whj OperateStart 运营文件夹加载结束，开始执行智能分类 " );
				OperateHelp.getInstance( this ).startCategory();
			}
			else if( OperateHelp.getInstance( this ).hasStopOperateFolderRun() )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , "whj OperateStart 运营文件夹加载结束，开始执行stop智能分类 " );
				OperateHelp.getInstance( this ).stopCategory();
			}
		}
	}
	
	@Override
	public void removeFolderIconByFolderInfo(
			final ArrayList<View> operateIcons )
	{
		runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				for( int i = 0 ; i < operateIcons.size() ; i++ )
				{
					View icon = operateIcons.get( i );
					if( icon instanceof BubbleTextView )//当图标时文件夹中
					{
						Object obj = icon.getTag();
						if( obj != null && obj instanceof ShortcutInfo )
						{
							ShortcutInfo info = (ShortcutInfo)obj;
							if( info.getContainer() != LauncherSettings.Favorites.CONTAINER_DESKTOP )
							{
								FolderIcon folder = mWorkspace.getFolderForId( info.getContainer() );//LauncherAppState.getInstance().getModel().getBgFolders().get( info.getContainer() );
								if( folder != null )
								{
									folder.getFolderInfo().remove( info );
									continue;
								}
							}
						}
					}
					CellLayout celllayout = mWorkspace.getParentCellLayoutForView( icon );
					if( celllayout != null )
					{
						celllayout.removeView( icon );
						//				FolderInfo folderInfo = icon.getFolderInfo();zhujieping del,删除数据库的操作放到线程中了
						//				LauncherModel.deleteItemFromDatabase( this , folderInfo );
						//				for( int j = 0 ; j < folderInfo.getContents().size() ; j++ )
						//				{
						//					LauncherModel.deleteItemFromDatabase( this , folderInfo.getContents().get( j ) );
						//				}
						if( icon instanceof DropTarget )
						{
							mDragController.removeDropTarget( (DropTarget)icon );
						}
					}
				}
				removeEmptyScreen();
			}
		} );
	}
	
	@Override
	public void exitOverviewMode()
	{
		if( mWorkspace != null && mWorkspace.isInOverviewMode() )
		{
			mWorkspace.exitOverviewModeIfInReordering( false , false );
		}
	}
	
	/**
	 * A package was updated.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAppsUpdated(
			final ArrayList<AppInfo> apps )
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				bindAppsUpdated( apps );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		if( mWorkspace != null )
		{
			mWorkspace.updateShortcuts( apps );
		}
		//<i_0010072> liuhailin@2015-03-04 modify begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && mAppsCustomizeContent != null )
		if( mAppsCustomizeContent != null )
		//<i_0010072> liuhailin@2015-03-04 modify end
		{
			mAppsCustomizeContent.updateApps( apps );
		}
		// zhangjin@2016/05/06 ADD START
		if(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		{
			if( mAppsView != null )
			{
				mAppsView.updateApps( apps );
			}
		}
		// zhangjin@2016/05/06 ADD END
	}
	
	/**
	 * A package was uninstalled.  We take both the super set of packageNames
	 * in addition to specific applications to remove, the reason being that
	 * this can be called when a package is updated as well.  In that scenario,
	 * we only remove specific components from the workspace, where as
	 * package-removal should clear all items by package name.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindComponentsRemoved(
			final ArrayList<String> packageNames ,
			final ArrayList<AppInfo> appInfos ,
			final boolean packageRemoved )
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				bindComponentsRemoved( packageNames , appInfos , packageRemoved );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		if( packageRemoved )
		{
			// zhujieping@2015/03/24 UPD START
			//closeFolder();
			//			closeFolderWithoutAnim();//xiatian del	//删除应用时，不关闭打开的文件夹（不知谷歌为啥要关闭文件夹）
			// zhujieping@2015/03/24 UPD END
			mWorkspace.removeItemsByPackageName( packageNames );
		}
		else
		{
			mWorkspace.removeItemsByApplicationInfo( appInfos );
		}
		// Notify the drag controller
		// zhujieping@2015/05/12 UPD START,例如删除的小组件没有launcher属性，即在桌面没有icon时，size为0。这一段的操作是拖动的图标、小组件正好为卸载的apk，取消拖动
		if( appInfos.size() > 0 )
		{
			mDragController.onAppsRemoved( appInfos , this );
		}
		else
		{
			if( packageNames.size() > 0 )
			{
				mDragController.onPackagesRemoved( packageNames , this );
			}
		}
		// zhujieping@2015/05/12 UPD END
		//<i_0010072> liuhailin@2015-03-04 modify begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && mAppsCustomizeContent != null )
		if( mAppsCustomizeContent != null )
		//<i_0010072> liuhailin@2015-03-04 modify end
		{
			mAppsCustomizeContent.removeApps( appInfos );
		}
		// zhangjin@2016/05/09 ADD START
		if(
		//
		(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		//
		&& ( mAppsView != null )
		//
		)
		{
			mAppsView.removeApps( appInfos );
		}
		// zhangjin@2016/05/09 ADD END
		if( mWorkspace.hasFavoritesPage() )
		{
			FavoritesPageManager.getInstance( this ).removeApps( packageNames );//cheyingkun add	//phenix1.1稳定版移植酷生活
		}
	}
	
	/**
	 * A number of packages were updated.
	 */
	private ArrayList<Object> mWidgetsAndShortcuts;
	private Runnable mBindPackagesUpdatedRunnable = new Runnable() {
		
		public void run()
		{
			bindPackagesUpdated( mWidgetsAndShortcuts );
			mWidgetsAndShortcuts = null;
		}
	};
	
	public synchronized void bindPackagesUpdated(
			final ArrayList<Object> widgetsAndShortcuts )
	{
		if( waitUntilResume( mBindPackagesUpdatedRunnable , true ) )
		{
			mWidgetsAndShortcuts = widgetsAndShortcuts;
			return;
		}
		// Update the widgets pane
		//<i_0010072> liuhailin@2015-03-04 modify begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && mAppsCustomizeContent != null )
		if( mAppsCustomizeContent != null )
		//<i_0010072> liuhailin@2015-03-04 modify end
		{
			mAppsCustomizeContent.onPackagesUpdated( widgetsAndShortcuts );
		}
		// zhangjin@2016/05/09 ADD START
		if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		{
			//mAppsView.onPackagesUpdated( widgetsAndShortcuts );
		}
		// zhangjin@2016/05/09 ADD END
	}
	
	/* Cling related */
	private boolean isClingsEnabled()
	{
		if( !LauncherDefaultConfig.SWITCH_ENABLE_CLINGS )
		{
			return false;
		}
		// For now, limit only to phones
		// disable clings when running in a test harness
		if( ActivityManager.isRunningInTestHarness() )
			return false;
		// Restricted secondary users (child mode) will potentially have very few apps
		// seeded when they start up for the first time. Clings won't work well with that
		//        boolean supportsLimitedUsers =
		//                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
		//        Account[] accounts = AccountManager.get(this).getAccounts();
		//        if (supportsLimitedUsers && accounts.length == 0) {
		//            UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);
		//            Bundle restrictions = um.getUserRestrictions();
		//            if (restrictions.getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false)) {
		//               return false;
		//            }
		//        }
		return true;
	}
	
	private Cling initCling(
			int clingId ,
			int scrimId ,
			boolean animate ,
			boolean dimNavBarVisibilty )
	{
		Cling cling = (Cling)findViewById( clingId );
		View scrim = null;
		if( scrimId > 0 )
		{
			scrim = findViewById( R.id.cling_scrim );
		}
		if( cling != null )
		{
			cling.init( this , scrim );
			cling.show( animate , SHOW_CLING_DURATION );
			if( dimNavBarVisibilty )
			{
				cling.setSystemUiVisibility( cling.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LOW_PROFILE );
			}
		}
		return cling;
	}
	
	private void dismissCling(
			final Cling cling ,
			final Runnable postAnimationCb ,
			final String flag ,
			int duration ,
			boolean restoreNavBarVisibilty )
	{
		// To catch cases where siblings of top-level views are made invisible, just check whether
		// the cling is directly set to GONE before dismissing it.
		if( cling != null && cling.getVisibility() != View.GONE )
		{
			final Runnable cleanUpClingCb = new Runnable() {
				
				public void run()
				{
					cling.cleanup();
					// We should update the shared preferences on a background thread
					new Thread( "dismissClingThread" ) {
						
						public void run()
						{
							SharedPreferences.Editor editor = mSharedPrefs.edit();
							editor.putBoolean( flag , true );
							editor.commit();
						}
					}.start();
					if( postAnimationCb != null )
					{
						postAnimationCb.run();
					}
				}
			};
			if( duration <= 0 )
			{
				cleanUpClingCb.run();
			}
			else
			{
				cling.hide( duration , cleanUpClingCb );
			}
			if( restoreNavBarVisibilty )
			{
				cling.setSystemUiVisibility( cling.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LOW_PROFILE );
			}
		}
	}
	
	private void removeCling(
			int id )
	{
		final View cling = findViewById( id );
		if( cling != null )
		{
			final ViewGroup parent = (ViewGroup)cling.getParent();
			parent.post( new Runnable() {
				
				@Override
				public void run()
				{
					parent.removeView( cling );
				}
			} );
		}
	}
	
	private boolean skipCustomClingIfNoAccounts()
	{
		Cling cling = (Cling)findViewById( R.id.workspace_cling );
		boolean customCling = cling.getDrawIdentifier().equals( "workspace_custom" );
		if( customCling )
		{
			AccountManager am = AccountManager.get( this );
			if( am == null )
				return false;
			Account[] accounts = am.getAccountsByType( "com.google" );
			return accounts.length == 0;
		}
		return false;
	}
	
	public void showFirstRunCling()
	{
		if( isClingsEnabled() && !mSharedPrefs.getBoolean( Cling.FIRST_RUN_CLING_DISMISSED_KEY , false ) && !skipCustomClingIfNoAccounts() )
		{
			// If we're not using the default workspace layout, replace workspace cling
			// with a custom workspace cling (usually specified in an overlay)
			// For now, only do this on tablets
			Cling cling = (Cling)findViewById( R.id.first_run_cling );
			if( cling != null )
			{
				String sbHintStr = getFirstRunClingSearchBarHint();
				if( !sbHintStr.isEmpty() )
				{
					TextView sbHint = (TextView)cling.findViewById( R.id.search_bar_hint );
					sbHint.setText( sbHintStr );
					sbHint.setVisibility( View.VISIBLE );
				}
			}
			initCling( R.id.first_run_cling , 0 , false , true );
		}
		else
		{
			removeCling( R.id.first_run_cling );
			if( !isClingsEnabled() )//zhujieping add，当开关为关时，直接remove掉其他cling，view不remove掉，还是占用内存的（workspace_cling是要在显示first_run_cling后，点击了icon，才会被remove掉，开关关时，first_run_cling这个不会显示，workspace_cling就无法被隐藏，folder_cling是要操作文件夹后才被remove）
			{
				removeCling( R.id.workspace_cling );
				removeCling( R.id.folder_cling );
			}
		}
	}
	
	protected String getFirstRunClingSearchBarHint()
	{
		return "";
	}
	
	protected int getFirstRunFocusedHotseatAppDrawableId()
	{
		return -1;
	}
	
	protected ComponentName getFirstRunFocusedHotseatAppComponentName()
	{
		return null;
	}
	
	protected int getFirstRunFocusedHotseatAppRank()
	{
		return -1;
	}
	
	protected String getFirstRunFocusedHotseatAppBubbleTitle()
	{
		return "";
	}
	
	protected String getFirstRunFocusedHotseatAppBubbleDescription()
	{
		return "";
	}
	
	public void showFirstRunWorkspaceCling()
	{
		// Enable the clings only if they have not been dismissed before
		if( isClingsEnabled() && !mSharedPrefs.getBoolean( Cling.WORKSPACE_CLING_DISMISSED_KEY , false ) )
		{
			Cling c = initCling( R.id.workspace_cling , 0 , false , true );
			// Set the focused hotseat app if there is one
			c.setFocusedHotseatApp(
					getFirstRunFocusedHotseatAppDrawableId() ,
					getFirstRunFocusedHotseatAppRank() ,
					getFirstRunFocusedHotseatAppComponentName() ,
					getFirstRunFocusedHotseatAppBubbleTitle() ,
					getFirstRunFocusedHotseatAppBubbleDescription() );
		}
		else
		{
			removeCling( R.id.workspace_cling );
		}
	}
	
	public Cling showFirstRunFoldersCling()
	{
		// Enable the clings only if they have not been dismissed before
		if( isClingsEnabled() && !mSharedPrefs.getBoolean( Cling.FOLDER_CLING_DISMISSED_KEY , false ) )
		{
			Cling cling = initCling( R.id.folder_cling , R.id.cling_scrim , true , true );
			return cling;
		}
		else
		{
			removeCling( R.id.folder_cling );
			return null;
		}
	}
	
	protected SharedPreferences getSharedPrefs()
	{
		return mSharedPrefs;
	}
	
	public boolean isFolderClingVisible()
	{
		Cling cling = (Cling)findViewById( R.id.folder_cling );
		if( cling != null )
		{
			return cling.getVisibility() == View.VISIBLE;
		}
		return false;
	}
	
	public void dismissFirstRunCling(
			View v )
	{
		Cling cling = (Cling)findViewById( R.id.first_run_cling );
		Runnable cb = new Runnable() {
			
			public void run()
			{
				// Show the workspace cling next
				showFirstRunWorkspaceCling();
			}
		};
		dismissCling( cling , cb , Cling.FIRST_RUN_CLING_DISMISSED_KEY , DISMISS_CLING_DURATION , false );
		// Fade out the search bar for the workspace cling coming up
		mSearchDropTargetBar.hideSearchBar( true );
		Intent intent = new Intent();
		intent.setAction( "com.android.launcher.RESTORE_LAUNCHER" );
		sendBroadcast( intent );
	}
	
	public void dismissWorkspaceCling(
			View v )
	{
		Cling cling = (Cling)findViewById( R.id.workspace_cling );
		Runnable cb = null;
		if( v == null )
		{
			cb = new Runnable() {
				
				public void run()
				{
					mWorkspace.enterOverviewMode();
				}
			};
		}
		dismissCling( cling , cb , Cling.WORKSPACE_CLING_DISMISSED_KEY , DISMISS_CLING_DURATION , true );
		// Fade in the search bar
		mSearchDropTargetBar.showSearchBar( true );
		//xiatian add start	//设置默认桌面引导
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE && !DefaultLauncherGuideManager.getInstance().isOnlyLauncher( mLauncher ) )
		{
			DefaultLauncherGuideManager.getInstance().checkDefaultLauncherAndShowGuideDialog( true , this );
		}
		//xiatian add end
	}
	
	public void dismissFolderCling(
			View v )
	{
		Cling cling = (Cling)findViewById( R.id.folder_cling );
		dismissCling( cling , null , Cling.FOLDER_CLING_DISMISSED_KEY , DISMISS_CLING_DURATION , true );
	}
	
	/**
	 * Prints out out state for debugging.
	 */
	public void dumpState()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat(
					"BEGIN launcher3 dump state for launcher " + this ,
					"-mSavedState=" ,
					mSavedState.toString() ,
					"-mWorkspaceLoading=" ,
					mWorkspaceLoading ,
					"-mRestoring=" ,
					mRestoring ,
					"-mWaitingForResult=" ,
					mWaitingForResult ,
					"-mSavedInstanceState=" ,
					mSavedInstanceState.toString() ,
					"-sFolders.size=" ,
					sFolders.size() ) );
		mModel.dumpState();
		if( mAppsCustomizeContent != null )
		{
			mAppsCustomizeContent.dumpState();
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "END launcher3 dump state" );
	}
	
	@Override
	public void dump(
			String prefix ,
			FileDescriptor fd ,
			PrintWriter writer ,
			String[] args )
	{
		super.dump( prefix , fd , writer , args );
		synchronized( sDumpLogs )
		{
			writer.println( " " );
			writer.println( "Debug logs: " );
			for( int i = 0 ; i < sDumpLogs.size() ; i++ )
			{
				writer.println( StringUtils.concat( "  " , sDumpLogs.get( i ) ) );
			}
		}
	}
	
	public static void dumpDebugLogsToConsole()
	{
		if( DEBUG_DUMP_LOG )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				synchronized( sDumpLogs )
				{
					Log.d( TAG , "" );
					Log.d( TAG , "*********************" );
					Log.d( TAG , "Launcher debug logs: " );
					for( int i = 0 ; i < sDumpLogs.size() ; i++ )
					{
						Log.d( TAG , StringUtils.concat( "  " , sDumpLogs.get( i ) ) );
					}
					Log.d( TAG , "*********************" );
					Log.d( TAG , "" );
				}
			}
		}
	}
	
	public static void addDumpLog(
			String tag ,
			String log ,
			boolean debugLog )
	{
		if( debugLog )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( tag , log );
		}
		if( DEBUG_DUMP_LOG )
		{
			sDateStamp.setTime( System.currentTimeMillis() );
			synchronized( sDumpLogs )
			{
				sDumpLogs.add( StringUtils.concat( sDateFormat.format( sDateStamp ) , ": " , tag , ", " , log ) );
			}
		}
	}
	
	public void dumpLogsToLocalData()
	{
		if( DEBUG_DUMP_LOG )
		{
			new Thread( "DumpLogsToLocalData" ) {
				
				@Override
				public void run()
				{
					boolean success = false;
					sDateStamp.setTime( sRunStart );
					String FILENAME = StringUtils.concat(
							sDateStamp.getMonth() ,
							"-" ,
							sDateStamp.getDay() ,
							"_" ,
							sDateStamp.getHours() ,
							"-" ,
							sDateStamp.getMinutes() ,
							"_" ,
							sDateStamp.getSeconds() ,
							".txt" );
					FileOutputStream fos = null;
					File outFile = null;
					try
					{
						outFile = new File( getFilesDir() , FILENAME );
						outFile.createNewFile();
						fos = new FileOutputStream( outFile );
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
					if( fos != null )
					{
						PrintWriter writer = new PrintWriter( fos );
						writer.println( " " );
						writer.println( "Debug logs: " );
						synchronized( sDumpLogs )
						{
							for( int i = 0 ; i < sDumpLogs.size() ; i++ )
							{
								writer.println( StringUtils.concat( "  " , sDumpLogs.get( i ) ) );
							}
						}
						writer.close();
					}
					try
					{
						if( fos != null )
						{
							fos.close();
							success = true;
						}
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	//<phenix modify> liuhailin@2015-01-27 modify begin
	public void enterLauncherSettings(
			View v )
	{
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		boolean mIsFromWorkspaceMenuListItem = isWorkspaceMenuListItem( v );//xiatian add	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		//cheyingkun add start	//添加友盟统计自定义事件(编辑模式桌面设置)
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			if( mIsFromOverviewPanelButton )
			{
				MobclickAgent.onEvent( this , UmengStatistics.ENTER_LAUNCHER_SETTING_BY_EDIT_MODE );
			}
			//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
			else if( mIsFromWorkspaceMenuListItem )
			{
			}
			//xiatian add end
		}
		//cheyingkun add end
		Intent mIntent = new Intent();
		//ComponentName mComponentName = new ComponentName( LauncherAppState.getInstance().getContext().getPackageName() , "com.cooee.setting.DesktopSettingActivity" );
		ComponentName mComponentName = new ComponentName( LauncherAppState.getInstance().getContext().getPackageName() , "com.cooee.phenix.launcherSettings.LauncherSettingsActivity" );
		mIntent.setComponent( mComponentName );
		//xiatian add start	//优化“编辑模式底边栏配置为打开桌面设置的某个二级菜单”的方法（不再使用“config_overview_panel_button_fallback_1_intent”的方案）。
		if( mIsFromOverviewPanelButton )
		{//【（WorkspaceMenuVerticalList）备注：待扩展（添加配置，支持menu list item打开桌面设置的二级菜单）】
			int mHeaderIndex = -1;
			boolean mIsOnBackPressedFinishSettings = false;
			Object mTag = v.getTag();
			if( mTag instanceof String )
			{
				String mTagStr = (String)mTag;
				String[] mTagStrList = mTagStr.split( ";" );
				if( mTagStrList.length == 2 )
				{
					try
					{
						mHeaderIndex = Integer.valueOf( mTagStrList[0] );
					}
					catch( NumberFormatException e )
					{
						mHeaderIndex = PreferenceBaseSettingActivity.HEADER_ID_UNDEFINED;
					}
					if( mHeaderIndex < PreferenceBaseSettingActivity.HEADER_ID_UNDEFINED || mHeaderIndex > PreferenceBaseSettingActivity.HEADER_ID_APPLIST_EFFECT_IN_DRAWER )
					{
						mHeaderIndex = PreferenceBaseSettingActivity.HEADER_ID_UNDEFINED;
					}
					if( mHeaderIndex != PreferenceBaseSettingActivity.HEADER_ID_UNDEFINED )
					{
						int mIsOnBackPressedFinishSettingsInt = 0;
						try
						{
							mIsOnBackPressedFinishSettingsInt = Integer.valueOf( mTagStrList[1] );
						}
						catch( NumberFormatException e )
						{
							mIsOnBackPressedFinishSettingsInt = 0;
						}
						mIsOnBackPressedFinishSettings = ( mIsOnBackPressedFinishSettingsInt == 0 ) ? false : true;
						Integer.valueOf( mTagStrList[0] );
					}
				}
			}
			if( mHeaderIndex != PreferenceBaseSettingActivity.HEADER_ID_UNDEFINED )
			{
				mIntent.putExtra( "headerIndex" , mHeaderIndex );
				mIntent.putExtra( "onBackPressedFinishSettings" , mIsOnBackPressedFinishSettings );
			}
		}
		//xiatian add end
		startActivity( mIntent );
	}
	
	//cheyingkun add start	//是否启动原生壁纸设置界面
	protected ComponentName getWallpaperPickerComponent()
	{
		return new ComponentName( getPackageName() , WallpaperPickerActivity.class.getName() );
	}
	//cheyingkun add end	//是否启动原生壁纸设置界面
	;
	
	public void enterBeautyCenter(
			View v )
	{//保留该方法，仅提供一键换壁纸使用。
		enterOrDownloadBeautyCenter( ThemeManager.BEAUTY_CENTER_TAB_THEME_CLASS_NAME );
	}
	
	/**
	 * TODO bind the data for start beauty center app
	 * @param intent
	 * @reutrn void
	 */
	private void bindThemeActivityData(
			Intent intent )
	{
		Bundle bundle = new Bundle(); // 创建Bundle对象
		bundle.putString( "launcherApplyThemeAction" , ThemeReceiver.ACTION_LAUNCHER_APPLY_THEME );
		//zhujieping delete,根据log看，若美化中心收到这个值且不为空的话，美化中心点击变换主题时，会发送两个广播，launcher收到这两个广播都会重启。若其中一个广播收到比较慢的话，就会出现黑屏现象。
		//		bundle.putString( "launcherRestartAction" , ThemeReceiver.ACTION_LAUNCHER_RESTART );
		bundle.putString( "defaultThemePackageName" , LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME );
		bundle.putString( "launcherPackageName" , LauncherAppState.getInstance().getContext().getPackageName() );
		// gaominghui@2017/01/09 UPD START
		//bundle.putBoolean( "disableSetWallpaperDimensions" , WallpaperManagerBase.disable_set_wallpaper_dimensions );
		bundle.putBoolean( "disableSetWallpaperDimensions" , WallpaperManagerBase.get_disableSetWallpaperDimensions() );
		// gaominghui@2017/01/09 UPD END
		bundle.putString( "launcher_authority" , PubContentProvider.LAUNCHER_AUTHORITY );
		//xiatian add start	//需求：美化中心，tab页可配置。
		// gaominghui@2016/10/24 ADD START 美化中心tab页显示可通过桌面参数控制
		bundle.putBoolean( "enableShowLock" , LauncherDefaultConfig.SWITCH_ENABLE_SHOW_BEAUTYCENTER_LOCK_TAB );
		bundle.putBoolean( "enableShowWidget" , false );
		bundle.putBoolean( "enableShowTheme" , LauncherDefaultConfig.SWITCH_ENABLE_SHOW_BEAUTYCENTER_THEME_TAB );
		bundle.putBoolean( "enableShowWallpaper" , LauncherDefaultConfig.SWITCH_ENABLE_SHOW_BEAUTYCENTER_WALLPAPER_TAB );
		// gaominghui@2016/10/24 ADD END 美化中心tab页显示可通过桌面参数控制
		//xiatian add end
		bundle.putString( "customWallpaperPath" , LauncherDefaultConfig.CONFIG_CUSTOM_WALLPAPERS_PATH );//xiatian add	//德盛伟业需求：本地化默认壁纸路径。客户可配置的桌面壁纸路径，如"/system/wallpapers"，再在该路径下放置客户的壁纸图片。配置为空则显示"\assets\launcher\wallpapers"中的壁纸。
		intent.putExtras( bundle ); // 把Bundle装入Intent里面
	}
	//<phenix modify> liuhailin@2015-01-27 modify end
	;
	
	//xiatian add start	//fix bug：解决“在phenix桌面没有设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，再进入phenix桌面之后，桌面特效为‘经典’特效”的问题。【i_0010438】
	private void reloadAnimationStyleNotRestore()
	{
		//1、reloadAnimationStyle
		//WangLei start //桌面和主菜单特效的分离
		//WangLei del start
		//String effectsValue = PreferenceManager.getDefaultSharedPreferences( this ).getString( getResources().getString( R.string.setting_key_launcher_effects ) , "0" );
		//setSelect_efffects_workspace( Integer.valueOf( effectsValue ) );
		//WangLei del end
		//WangLei add start
		String effectsValue = "0";
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			effectsValue = PreferenceManager.getDefaultSharedPreferences( this ).getString( LauncherDefaultConfig.getString( R.string.setting_key_launcher_effects ) , "0" );
		}
		else
		{
			effectsValue = PreferenceManager.getDefaultSharedPreferences( this ).getString( LauncherDefaultConfig.getString( R.string.setting_key_workspace_effect ) , "0" );
		}
		setSelect_efffects_workspace( Integer.valueOf( effectsValue ) );
		effectsValue = PreferenceManager.getDefaultSharedPreferences( this ).getString( LauncherDefaultConfig.getString( R.string.setting_key_applist_effect ) , "0" );
		setSelect_effects_applist( Integer.parseInt( effectsValue ) );
		//WangLei add end
		//WangLei end
		if( mWorkspace != null )
		{
			mWorkspace.initAnimationStyle( mWorkspace );
		}
		//fulijuan xiugai start //单层的时候 小组件特效也应初始化
		if( mAppsCustomizeContent != null && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Widgets)
		{
			mAppsCustomizeContent.initAnimationStyle( mAppsCustomizeContent );
		}
		//fulijuan xiugai end
		//WangLei start //桌面和主菜单特效的分离   //fulijuan xiugai 小组件也应被分离
		/**当前为双层模式主菜单界面时才初始化AppsCustomePagedView的特效*/
		//if( mAppsCustomizeContent != null ) //WangLei del
		//WangLei add start
		if(
		//
		mAppsCustomizeContent != null
		//
		&& ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//
		&& mWorkspace.getState() == Workspace.State.SMALL
		//
		)
		//WangLei add end	
		//WangLei end
		{
			AppsCustomizePagedView.ContentType contentType = mAppsCustomizeContent.getContentType();
			if( contentType == AppsCustomizePagedView.ContentType.Applications || contentType == AppsCustomizePagedView.ContentType.Widgets)//fulijuan xiugai
			{
				mAppsCustomizeContent.initAnimationStyle( mAppsCustomizeContent );
			}
		}
		//2、NotRestore
		//		mWorkspace.restoreWorkspace();
		//		mAppsCustomizeContent.restoreAppsCustomizePagedView();
	}
	//xiatian add end
	;
	
	//WangLei add start //bug:0010453 //安装、更新或卸载应用时，回调此方法
	public void onAppsChanged(
			String pkgName ,
			String action )
	{
		if( mDragLayer != null )
		{
			/**安装、更新或卸载应用时，退出调整插件大小模式*/
			mDragLayer.clearAllResizeFrames();
		}
		//zhujieping add start
		if( Intent.ACTION_PACKAGE_REMOVED.equals( action ) || Intent.ACTION_PACKAGE_ADDED.equals( action ) )
		{
			if( mEditModeEntity != null )
			{
				mEditModeEntity.onAppsUpdate( pkgName , action );
			}
		}
		//zhujieping add end
	}
	//WangLei add end
	;
	
	//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
	@Override
	public void onBeforeStartCategory()
	{
		// TODO Auto-generated method stub
		//退出编辑模式
		if( mWorkspace != null && mWorkspace.isInOverviewMode() )
		{
			exitEditMode( false );
		}
		closeFolder();//cheyingkun add	//智能分类前关闭文件夹。【i_0012310】
		//		if( mOperateDynamicMain != null ),移到oncategroysucess中
		//		{
		//			mOperateDynamicMain.clearAllIconData();
		//		}
	}
	//cheyingkun add end
	;
	
	//WangLei add start //bug:0011044  单层模式时将桌面除时间插件之外所有的图标拖动到一个文件夹里，让桌面只有一页。之后进行智能分类，成功后切换页面到第二页。恢复布局，桌面停止运行
	public void onStopCategorySucess()
	{
		/**当恢复布局从旧的数据库里读取信息后，设置桌面当前页为默认页*/
		if( mOperateDynamicMain != null )
		{
			mOperateDynamicMain.clearAllIconData();
		}
		runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( mWorkspace != null )
				{
					//cheyingkun start	//解决“智能分类前后，页面数相同时，分类后页面没有跳到默认页并且文件夹点击无法打开”的问题。【i_0011212】
					//					mWorkspace.setCurrentPageOnly( true , true );//cheyingkun del
					mWorkspace.setCurrentPageNoInvalidate();//cheyingkun add
					//cheyingkun end
				}
			}
		} );
	}
	
	//智能分类的圈圈结束消失了,表示分类结束或者恢复结束 wanghongjian add
	@Override
	public void onDismissCategoryProgressView()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , "whj OperateStart 智能分类结束" );
		//cheyingkun del start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
		//				if( mModel != null )
		//				{
		//					mModel.restartOnReceive();
		//				}
		//cheyingkun del end
		if( mOperateDynamicMain != null )
		{
			mOperateDynamicMain.reStartLoadData();
		}
	}
	
	@Override
	public OperateDynamicMain getOperateDynamicMain()
	{
		return mOperateDynamicMain;//获得运营文件夹实例
	}
	
	/**智能分类成功后的后续操作*/
	public void onCategorySucess()
	{
		//zhujieping start，这个本来在onBeforeStartCategory中，但分类失败时，数据不可以清运营数据，成功时才清运营数据
		if( mOperateDynamicMain != null )
		{
			mOperateDynamicMain.clearAllIconData();
		}
		//zhujieping end
		/**将桌面当前页设置为桌面默认页*/
		//cheyingkun start	//解决“智能分类前后，页面数相同时，分类后页面没有跳到默认页并且文件夹点击无法打开”的问题。【i_0011212】
		//cheyingkun del start
		//		if( mWorkspace != null )
		//		{
		//			mWorkspace.setCurrentPageOnly( false , false );
		//		}
		//cheyingkun del end
		//cheyingkun add start
		runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( mWorkspace != null )
				{
					mWorkspace.setCurrentPageNoInvalidate();
				}
			}
		} );
		//cheyingkun add end
		//cheyingkun end
	}
	//WangLei add end
	;
	
	//xiatian add start	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
	public Handler getHandler()
	{
		return mHandler;
	}
	
	public void updateUnreadNumberByComponent(
			final ComponentName mComponentName ,
			final int mUnreadNum )
	{
		if( mComponentName == null )
		{
			return;
		}
		UnreadHelper mUnreadHelper = LauncherAppState.getInstance().getUnreadHelper();
		if( mUnreadHelper != null )
		{
			mUnreadHelper.saveUnreadNumByComponentName( mComponentName , mUnreadNum );
		}
		if( mHandler != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "xiatian -- updateUnreadNumberByComponent" , "(mHandler!=null)" );
			mHandler.post( new Runnable() {
				
				public void run()
				{
					if( mWorkspace != null )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "xiatian -- updateUnreadNumberByComponent" , "(mWorkspace!=null)-1" );
						mWorkspace.updateUnreadNumberByComponent( mComponentName , mUnreadNum );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "xiatian -- updateUnreadNumberByComponent" , "(mWorkspace!=null)-2" );
					}
					//WangLei add start //bug:c_0003047 //主菜单上未接来电和未读短信更新不及时
					if( mAppsCustomizeContent != null && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Applications )
					{
						mAppsCustomizeContent.updateUnreadNumberByComponent( mComponentName , mUnreadNum );
					}
					//WangLei add end
				}
			} );
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//需求：在文件夹打开状态下进入编辑模式，立刻关闭文件夹（不播放文件夹关闭的相关动画）。
	public void closeFolderWithoutAnim()
	{
		Folder folder = mWorkspace.getOpenFolder();
		if( folder != null )
		{
			folder.hideDynamicDialog();
			folder.hideNativeAdverDialog();//cheyingkun add	//文件夹推荐应用
			if( folder.isEditingName() )
			{
				folder.dismissEditingName();
			}
			closeFolderWithoutAnim( folder );
			// Dismiss the folder cling
			dismissFolderCling( null );
		}
	}
	
	void closeFolderWithoutAnim(
			Folder folder )
	{
		folder.getInfo().setOpened( false );
		ViewGroup parent = (ViewGroup)folder.getParent().getParent();
		if( parent != null )
		{
			FolderIcon fi = (FolderIcon)mWorkspace.getViewForTag( folder.getInfo() );
			shrinkAndFadeInFolderIconWithoutAnim( fi );
		}
		folder.closeWithoutAnim();
	}
	
	private void shrinkAndFadeInFolderIconWithoutAnim(
			final FolderIcon fi )
	{
		if( fi == null )
			return;
		final CellLayout cl = (CellLayout)fi.getParent().getParent();
		// We remove and re-draw the FolderIcon in-case it has changed
		mDragLayer.removeView( mFolderIconImageView );
		copyFolderIconToImage( fi );
		mFolderIconImageView.setAlpha( 1f );
		mFolderIconImageView.setScaleX( 1f );
		mFolderIconImageView.setScaleY( 1f );
		if( cl != null )
		{
			//cheyingkun add start	//文件夹预览图层叠效果
			if(
			//
			( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
			//
			|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
			//
			)
			//cheyingkun add start	//文件夹预览图层叠效果
			{
				// lvjiangbin add start 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果
				cl.clearFolderLeaveBehind();
				// lvjiangbin add end 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果
			}
			// Remove the ImageView copy of the FolderIcon and make the original visible.
			mDragLayer.removeView( mFolderIconImageView );
			fi.setVisibility( View.VISIBLE );
		}
	}
	//xiatian add end
	;
	
	//WangLei add start //实现默认配置AppWidget的流程
	/**当请求绑定成功后，只将插件的信息保存在数据库中，不必添加插件到桌面*/
	public void completeAddWidgetToDb(
			final int appWidgetId ,
			ItemInfo pendingWidgetInfo ,
			AppWidgetHostView boundWidget ,
			AppWidgetProviderInfo appWidgetInfo )
	{
		if( appWidgetInfo == null )
		{
			appWidgetInfo = mAppWidgetManager.getAppWidgetInfo( appWidgetId );
		}
		int[] minSpanXY = getMinSpanForWidget( this , appWidgetInfo );
		int[] spanXY = new int[2];
		int[] cellXY = new int[2];
		if( pendingWidgetInfo.getCellX() >= 0 && pendingWidgetInfo.getCellY() >= 0 )
		{
			cellXY[0] = pendingWidgetInfo.getCellX();
			cellXY[1] = pendingWidgetInfo.getCellY();
			spanXY[0] = pendingWidgetInfo.getSpanX();
			spanXY[1] = pendingWidgetInfo.getSpanY();
		}
		else
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "the cellX and cellY must greater than -1" );
			return;
		}
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo( appWidgetId , appWidgetInfo.provider );
		launcherInfo.setSpanX( spanXY[0] );
		launcherInfo.setSpanY( spanXY[1] );
		launcherInfo.setMinSpanX( minSpanXY[0] );
		launcherInfo.setMinSpanY( minSpanXY[1] );
		LauncherModel.addItemToDatabase( this , launcherInfo , pendingWidgetInfo.getContainer() , pendingWidgetInfo.getScreenId() , cellXY[0] , cellXY[1] , false );
	}
	//WangLei add end
	;
	
	private void setupOverviewPanelViews()
	{
		setupOverviewPanelButtons();
		//cheyingkun add start	//编辑模式是否显示提示信息（提示信息内容为“拖动页面可改变页面位置”）。true为显示；false为不显示。默认false。【c_0004055】
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_PANEL_TEXT_HINT )
		{
			View textHint = findViewById( R.id.overview_panel_text_hint_id );
			textHint.setVisibility( View.VISIBLE );
		}
		//cheyingkun add end
		if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		{
			//cheyingkun add start	//编辑模式底部按键支持按键(逻辑优化)
			overviewPanelButtons = (ViewGroup)mLauncher.getOverviewPanel().findViewById( R.id.overview_panel_button );
			overviewPanelButtons.setFocusable( true );
			overviewPanelButtons.setOnKeyListener( new OverviewPanelButtonKeyEventListener() );
			//cheyingkun add end
		}
	}
	
	private void setupOverviewPanelButtons()
	{
		ViewGroup mOverviewPanelButtons = (ViewGroup)findViewById( R.id.overview_panel_button );
		int mOverviewPanelButtonNum = mOverviewPanelButtons.getChildCount();
		for( int i = 0 ; i < mOverviewPanelButtonNum ; i++ )
		{
			View mOverviewPanelButton = mOverviewPanelButtons.getChildAt( i );
			//xiatian add start	//添加配置项“switch_enable_show_beauty_center_in_edit_mode_hotseat”，是否在“编辑模式底边栏”中显示“美化中心”按钮。true显示；false不显示。默认true。
			int mOverviewPanelButtonId = mOverviewPanelButton.getId();
			if(
			//
			LauncherDefaultConfig.getBoolean( R.bool.switch_enable_show_beauty_center_in_edit_mode_hotseat ) == false
			//
			&& mOverviewPanelButtonId == R.id.overview_panel_button_beauty_center_id
			//
			)
			{
				mOverviewPanelButton.setVisibility( View.GONE );
			}
			else
			//xiatian add end
			//xiatian add start	//需求：配置项“config_applistbar_style”为“0”前提下，在双层模式时，不显示“编辑模式底边栏”中的“小组件”按钮。
			if(
			//
			LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
			//
			&& LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_TAB
			//
			&& mOverviewPanelButtonId == R.id.overview_panel_button_widgets_id
			//
			)
			{
				mOverviewPanelButton.setVisibility( View.GONE );
			}
			else
			//xiatian add end
			{
				mOverviewPanelButton.setOnTouchListener( getHapticFeedbackTouchListener() );
			}
		}
	}
	
	//从folder类中移出来 , change by shlt@2014/12/02 ADD START
	public void showHotseat()
	{
		mHotseat.setVisibility( View.VISIBLE );
	}
	
	public void hideHotseat()
	{
		mHotseat.setVisibility( View.INVISIBLE );
	}
	//从folder类中移出来 , change by shlt@2014/12/02 ADD END
	;
	
	public boolean isWaitingForUninstall()
	{
		boolean ret = false;
		if( mSearchDropTargetBar != null )
		{
			DeleteDropTarget mDeleteDropTarget = mSearchDropTargetBar.getDeleteDropTarget();
			if( mDeleteDropTarget != null && mDeleteDropTarget.isWaitingForUninstall() )
			{
				ret = true;
			}
		}
		return ret;
	};
	
	//xiatian add start	//fix bug：解决“双层模式时，在打开开关'SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST'的前提下，点击编辑模式下的小组件入口进入的小组件选择界面上方会显示appbar”的问题。
	private void showAppsCustomizeHelperTabsContainer(
			boolean mIsShowTabsContainer )
	{
		if( mAppsCustomizeTabHost != null )
		{
			mAppsCustomizeTabHost.setTabsContainerVisibility( mIsShowTabsContainer );
		}
	}
	//xiatian add end
	;
	
	// zhujieping@2015/05/27 DEL START,该id与search_bar.xml中的重复，在布局中被移除，
	//private void hideVoiceBar()
	//{
	//		findViewById( R.id.voice_button_proxy ).setVisibility( View.GONE );
	//}
	//
	//private void showVoiceBar()
	//{
	//		findViewById( R.id.voice_button_proxy ).setVisibility( View.VISIBLE );
	//}
	// zhujieping@2015/05/27 DEL END
	private void hidePageIndicator()
	{
		findViewById( R.id.page_indicator ).setVisibility( View.GONE );
	}
	
	private void showPageIndicator()
	{
		findViewById( R.id.page_indicator ).setVisibility( View.VISIBLE );
	}
	
	private void hideSearchBar()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			View mSearchBar = getSearchBar();
			mSearchBar.setVisibility( View.GONE );
		}
	}
	
	private void showSearchBar()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			View mSearchBar = getSearchBar();
			mSearchBar.setVisibility( View.VISIBLE );
		}
	}
	
	private void hideWorkspaceView()
	{
		this.getWorkspace().setVisibility( View.INVISIBLE );
	}
	
	private void showWorkspaceView()
	{
		this.getWorkspace().setVisibility( View.VISIBLE );
	}
	
	public void hideCurPageViews()
	{
		hideHotseat();//隐藏底边栏
		hidePageIndicator();//隐藏页面指示器
		hideWorkspaceView();//隐藏workspace
		//<i_0010910> liuhailin@2015-04-09 modify begin
		hideSearchBar();
		//<i_0010910> liuhailin@2015-04-09 modify end
		// zhujieping@2015/05/27 DEL START
		//hideVoiceBar();
		// zhujieping@2015/05/27 DEL END
	}
	
	public void showCurPageViews()
	{
		showHotseat();//关闭文件夹动画完成后,显示底边栏//xiatian add
		showPageIndicator();//显示页面指示器
		showWorkspaceView();//显示workspace
		//<i_0010910> liuhailin@2015-04-09 modify begin
		showSearchBar();
		//<i_0010910> liuhailin@2015-04-09 modify end
		// zhujieping@2015/05/27 DEL START
		//showVoiceBar();
		// zhujieping@2015/05/27 DEL END
	}
	
	// zhujieping@2015/04/15 ADD START
	public int getStatusBarHeight(
			//isReal为true时，是得到状态栏的实际高度。为false时，判断是否有虚拟按键，有虚拟按键的返回0，没有虚拟按键的返回实际状态栏高度
			boolean isReal )
	{
		int result = 0;
		boolean mIsVirtualMenuShown = LauncherAppState.getInstance().isVirtualMenuShown();
		if( isReal || !mIsVirtualMenuShown )
		{
			Context slaveContext = null;
			Resources mResources = null;
			int resourceId = 0;
			String mPackageName = "android";
			try
			{
				slaveContext = createPackageContext( mPackageName , Context.CONTEXT_IGNORE_SECURITY );
			}
			catch( NameNotFoundException e )
			{
				e.printStackTrace();
			}
			if( slaveContext != null )
			{
				mResources = slaveContext.getResources();
				resourceId = ResourceUtils.getDimenResourceIdByReflectIfNecessary( -1 , mResources , mPackageName , "status_bar_height" );
				if( resourceId > 0 )
				{
					result = mResources.getDimensionPixelSize( resourceId );
				}
			}
			return result;
		}
		else
		{
			return 0;
		}
	}
	
	public boolean hasGlobalSearch()
	{
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE == false//桌面不显示搜索
		//				//-1屏不显示搜索
		//				&& ( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES && LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE == false )//cheyingkun add	//phenix1.1稳定版移植酷生活 
		//		)
		//		{
		//			return false;
		//		}
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_COOEE_SEARCH )
		//		{
		//			return false;
		//		}
		final SearchManager searchManager = (SearchManager)getSystemService( Context.SEARCH_SERVICE );
		// gaominghui@2016/12/14 UPD START
		//ComponentName activityName = searchManager.getGlobalSearchActivity();
		ComponentName activityName = getGlobalSearchActivity( searchManager );
		// gaominghui@2016/12/14 UPD END
		if( activityName != null )
		{
			return true;
		}
		return false;
	}
	// zhujieping@2015/04/15 ADD END
	;
	
	//cheyingkun add start	//我们自己的统计Statistics
	private void initStatisticData()
	{
		prefs = this.getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
		StatisticsBaseNew.setApplicationContext( this );
	}
	
	private void startStatisticOnLauncherCreate()
	{
		initStatisticData();
		if( prefs.getBoolean( "first_run" , true ) )
		{
			//cheyingkun start	//更新uibase.jar包版本到35486(统计增加免责处理，当launcher的免责为否时，不发统计数据，默认发送)
			//StatisticsExpandNew.register( this , sn , appid , CooeeSdk.cooeeGetCooeeId( this ) , 1 , this.getApplication().getPackageName() , "" + launcherVersion );//cheyingkun del
			StatisticsExpandNew.register( this , LauncherConfigUtils.getSN( this ) , LauncherConfigUtils.getAppID( this ) , LauncherConfigUtils.cooeeGetCooeeId( this ) , 1 , this.getApplication()
					.getPackageName() , String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) , true );//cheyingkun add
			//cheyingkun end
			//cheyingkun add start	//第一次运行桌面时,统计酷生活开关默认配置(umeng统计和内部统计)
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "cyk launcher startStatisticOnLauncherCreate " );
			boolean switchEnableFavoritesDefault = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_favorites );
			//umeng统计
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				HashMap<String , String> map = new HashMap<String , String>();
				map.put( UmengStatistics.SWITCH_ENABLE_FAVORITES_DEFAULT , String.valueOf( switchEnableFavoritesDefault ) );
				MobclickAgent.onEvent( this , UmengStatistics.SWITCH_ENABLE_FAVORITES_DEFAULT , map );
			}
			//内部统计
			JSONObject obj = new JSONObject();
			try
			{
				obj.put( "param1" , StringUtils.concat( UmengStatistics.SWITCH_ENABLE_FAVORITES_DEFAULT , ":" , switchEnableFavoritesDefault ) );
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StatisticsExpandNew.onCustomEvent(
					this ,
					UmengStatistics.SWITCH_ENABLE_FAVORITES_DEFAULT ,
					LauncherConfigUtils.getSN( this ) ,
					LauncherConfigUtils.getAppID( this ) ,
					LauncherConfigUtils.cooeeGetCooeeId( this ) ,
					4 ,
					getPackageName() ,
					String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) ,
					obj );
			//cheyingkun add end
		}
		else
		{
			//cheyingkun start	//更新uibase.jar包版本到35486(统计增加免责处理，当launcher的免责为否时，不发统计数据，默认发送)
			//StatisticsExpandNew.startUp( this , sn , appid , CooeeSdk.cooeeGetCooeeId( this ) , 1 , this.getApplication().getPackageName() , "" + launcherVersion );//cheyingkun del
			StatisticsExpandNew.startUp( this , LauncherConfigUtils.getSN( this ) , LauncherConfigUtils.getAppID( this ) , LauncherConfigUtils.cooeeGetCooeeId( this ) , 1 , this.getApplication()
					.getPackageName() , String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) , true );//cheyingkun add
			//cheyingkun end
		}
		//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
		if(
		//
		LauncherDefaultConfig.SWITCH_ENABLE_OPERATE_EXPLORER
		//
		&& TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE ) == false
		//
		)
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				MobclickAgent.onEvent( this , UmengStatistics.ENABLE_OPERATE_EXPLORER );
			}
			StatisticsExpandNew.onCustomEvent(
					this ,
					UmengStatistics.ENABLE_OPERATE_EXPLORER ,
					LauncherConfigUtils.getSN( this ) ,
					LauncherConfigUtils.getAppID( this ) ,
					LauncherConfigUtils.cooeeGetCooeeId( this ) ,
					4 ,
					getPackageName() ,
					String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) ,
					null );
		}
		//xiatian add end
		//cheyingkun add start	//同步修改点“【uni】【4.0】【需求】判断kpsh功能是否完整，并上传统计。【朱节平】”
		//		KpshStatistics.kpshFunctionStatistics( this );  lvjiangbin delete
		//cheyingkun add end
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//fix bug：解决“在主菜单支持显示'应用市场'的前提下，卸载和安装带有'应用市场'的应用时，'应用市场'图标没有及时的消失和出现”的问题。
	public void updateAppMarketIconWhenAppsChanged()
	{
		if( SHOW_MARKET_BUTTON == false )
		{
			return;
		}
		int coi = getCurrentOrientationIndexForGlobalIcons();
		sAppMarketIcon[coi] = null;
		updateAppMarketIcon();
		if( sAppMarketIcon[coi] != null )
		{
			updateAppMarketIcon( sAppMarketIcon[coi] );
		}
	}
	//xiatian add end
	;
	
	private boolean isFirstRunCling()
	{
		if( isClingsEnabled() && !mSharedPrefs.getBoolean( Cling.FIRST_RUN_CLING_DISMISSED_KEY , false ) )
		{
			Cling cling = (Cling)findViewById( R.id.first_run_cling );
			if( cling != null )
			{
				return cling.getVisibility() == View.VISIBLE;
			}
			return false;
		}
		return false;
	}
	
	private boolean isFirstRunWorkspaceCling()
	{
		if( isClingsEnabled() && !mSharedPrefs.getBoolean( Cling.WORKSPACE_CLING_DISMISSED_KEY , false ) )
		{
			Cling cling = (Cling)findViewById( R.id.workspace_cling );
			if( cling != null )
			{
				return cling.getVisibility() == View.VISIBLE;
			}
			return false;
		}
		return false;
	}
	
	//WangLei add start //桌面和主菜单特效的分离
	public void setSelect_effects_applist(
			int allApp_effects )
	{
		this.select_effects_applist = allApp_effects;
	}
	
	public int getSelect_effects_applist()
	{
		return select_effects_applist;
	}
	
	//WangLei add end
	// zhujieping@2015/06/08 ADD START
	@Override
	public void removewaitUntilResumeRunnable(
			List<Runnable> list )
	{
		// TODO Auto-generated method stub
		for( Runnable r : list )/*移除等待onresume后要执行的runnable*/
		{
			if( mBindOnResumeCallbacks.indexOf( r ) != -1 )
			{
				mBindOnResumeCallbacks.remove( r );
			}
		}
	}
	
	@Override
	public void removeFolderInfo(
			ArrayList<ItemInfo> newInfos )
	{
		// TODO Auto-generated method stub
		//现在数据库中不仅仅有运营文件夹，还有虚链接以及放在桌面的虚图标
		ArrayList<ItemInfo> list = new ArrayList<ItemInfo>( LauncherAppState.getInstance().getModel().getBgWorkspaceItems() );
		Iterator<ItemInfo> coll = list.iterator();
		while( coll.hasNext() )
		{
			//删除数据库
			ItemInfo info = coll.next();
			if( info instanceof FolderInfo )
			{
				FolderInfo fi = (FolderInfo)info;
				if( OperateDynamicMain.shouldOperateFolderDelete( fi ) )
				{
					LauncherModel.deleteItemFromDatabase( this , fi );
					OperateFolderDatabase.delete( fi );
				}
				for( ShortcutInfo si : fi.getContents() )
				{
					if( si.isOperateIconItem() && OperateDynamicMain.shouldOperateShortcutDelete( si , newInfos ) )
					{
						LauncherModel.deleteItemFromDatabase( this , si );
						OperateFolderDatabase.delete( si );
					}
				}
			}
			else if( info instanceof ShortcutInfo )
			{
				ShortcutInfo si = (ShortcutInfo)info;
				if( si.isOperateIconItem() && OperateDynamicMain.shouldOperateShortcutDelete( si , newInfos ) )
				{
					LauncherModel.deleteItemFromDatabase( this , si );
					OperateFolderDatabase.delete( si );
				}
			}
		}
	}
	
	// zhujieping@2015/06/08 ADD END
	//cheyingkun add start	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
	public void AppsUpdatedBitmap(
			final ArrayList<AppInfo> apps )
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				AppsUpdatedBitmap( apps );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		if( mWorkspace != null )
		{
			mWorkspace.updateAppInfosBitmap( apps );
		}
		//cheyingkun add start	//解决“单层模式挂载T卡，切换到双层模式取消挂载，allAPP中T卡应用丢失”的问题。【i_0011427】
		if( mAppsCustomizeContent != null && apps.size() > 0 )
		{
			AppInfo appInfo = apps.get( 0 );
			if( appInfo.getAvailable() )
			{
				mAppsCustomizeContent.addApps( apps );
			}
			else
			{
				mAppsCustomizeContent.removeApps( apps );
			}
		}
		//cheyingkun add end
		// zhangjin@2016/05/06 ADD START
		if(
		//
		( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		{
			if( mAppsView != null && apps.size() > 0 )
			{
				AppInfo appInfo = apps.get( 0 );
				if( appInfo.getAvailable() )
				{
					mAppsView.addApps( apps );
				}
				else
				{
					mAppsView.removeApps( apps );
				}
			}
		}
		// zhangjin@2016/05/06 ADD END
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//加载主菜单提示信息【c_0003106】
	/**
	 * 显示主菜单加载提示信息
	 * @param contentType
	 */
	private void showAppListLoadHint(
			AppsCustomizePagedView.ContentType contentType )
	{
		if( mWorkspaceLoading && contentType == AppsCustomizePagedView.ContentType.Applications )
		{
			if( mAllAppLoadHint == null )
			{
				mAllAppLoadHint = new ProgressView( this );
			}
			mAllAppLoadHint.setLoadState( R.string.allApp_load_hint );
			mDragLayer.addView( mAllAppLoadHint.getProgressView() );
		}
	}
	
	/**
	 * 隐藏主菜单加载提示信息
	 */
	private void hideAppListLoadHint()
	{
		if( mAllAppLoadHint != null && mAllAppLoadHint.getProgressView().getParent() != null )
		{
			mDragLayer.removeView( mAllAppLoadHint.getProgressView() );
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//优化加载速度(统计和sdk放到桌面加载完再初始化)
	private void initStatisFirstRun()
	{
		//cheyingkun add start	//我们自己的统计Statistics
		if( prefs != null && prefs.getBoolean( "first_run" , true ) )
		{
			prefs.edit().putBoolean( "first_run" , false ).commit();
		}
		//cheyingkun add end
	}
	
	/**
	 * 厂商编号sn作为渠道名称上传友盟统计
	 */
	public void initUmengChannel()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			String channel = LauncherConfigUtils.getSN( this );
			AnalyticsConfig.setChannel( channel );
		}
	}
	
	private void statisOnResume()
	{
		if( isLoadFinish )
		{
			//cheyingkun start	//更新uibase.jar包版本到35486(统计增加免责处理，当launcher的免责为否时，不发统计数据，默认发送)
			//StatisticsExpandNew.use( this , sn , appid , CooeeSdk.cooeeGetCooeeId( this ) , 1 , this.getApplication().getPackageName() , "" + launcherVersion );//cheyingkun add	//我们自己的统计Statistics	//cheyingkun del
			StatisticsExpandNew.use( this , LauncherConfigUtils.getSN( this ) , LauncherConfigUtils.getAppID( this ) , LauncherConfigUtils.cooeeGetCooeeId( this ) , 1 , this.getApplication()
					.getPackageName() , String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) , !Disclaimer.isNeedShowDisclaimer() );//cheyingkun add
			//cheyingkun end
		}
	}
	
	private void umengStatisOnResume()
	{
		if(
		//
		( LauncherDefaultConfig.SWITCH_ENABLE_UMENG /* //xiatian add	//需求：运营友盟（详见“OperateUmeng”中说明）。 */)
		//
		&& isLoadFinish
		//
		&& ( !Disclaimer.isNeedShowDisclaimer() /*//cheyingkun add start //免责声明布局(友盟统计添加免责声明判断)*/)
		//
		)
		{
			MobclickAgent.onResume( this ); //WangLei add //添加友盟统计分析功能
		}
	}
	
	private void umengStatisOnPause()
	{
		if(
		//
		( LauncherDefaultConfig.SWITCH_ENABLE_UMENG /* //xiatian add	//需求：运营友盟（详见“OperateUmeng”中说明）。 */)
		//
		&& isLoadFinish
		//
		&& ( !Disclaimer.isNeedShowDisclaimer() /*//cheyingkun add start //免责声明布局(友盟统计添加免责声明判断)*/)
		//
		)
		{
			MobclickAgent.onPause( this ); //WangLei add //添加友盟统计分析功能
		}
	}
	
	private void initCooeeSdk()
	{
		if( isLoadFinish //
				&& !Disclaimer.isNeedShowDisclaimer()// cheyingkun add start //免责声明布局(初始化sdk添加免责声明判断)
		)
		{
			//ME_RTFSC [start]
			KpshSdk.setAppKpshTag( Launcher.this , Launcher.this.getPackageName() );
			//gaominghui start 更新了CooeeShellSdk的jar包（1.1.3.20170519）后，修改了phenix桌面调用CooeeShellSdk的初始化方法，防止桌面报停
			//CooeeSdk.initCooeeSdk( Launcher.this );//gaominghui del
			CooeeSdk.initCooeeSdk( Launcher.this , false );//gaominghui add
			//gaominghui end
			UpdateManagerImpl.Resume( Launcher.this );
			//ME_RTFSC [end]
		}
	}
	
	private void initOperateHelp()
	{
		OperateHelp.getInstance( this ).setCategoryListener( this );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
	}
	
	private void initOperateDynamic()
	{
		DlManager.getInstance().getDownloadHandle().onResume();//下载管理器的上次没处理完的数据的初始化
		DlManager.getInstance().getReDownloadHelperHandle().startImproperStopTasks( this );//WIFI继续下载
		if( OperateDynamicProxy.getInstance() != null )//没有处理的免责声明
		{
			OperateDynamicProxy.getInstance().dealDismissDisclaimer();
		}
	}
	
	@Override
	public void loadFinish()
	{
		if( prefs == null )
		{
			Runnable mRunnable1 = new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					//统计
					//					Assets.initAssets( Launcher.this );//zhujieping,放到launcherconfigutil中初始化了
					startStatisticOnLauncherCreate();
					initStatisFirstRun();
					initUmengChannel();
					//cheyingkun add start	//启动速度优化(小部件更新放在加载完成之后)
					if( !isLoadFinish )
					{
						// Update customization drawer _after_ restoring the states
						if( mAppsCustomizeContent != null )
						{
							mAppsCustomizeContent.onPackagesUpdated( LauncherModel.getSortedWidgetsAndShortcuts( Launcher.this ) );
						}
						hideWidgetListLoadHint();
					}
					//cheyingkun add end
					isLoadFinish = true;//cheyingkun add	//桌面加载速度优化(代码优化)
					UpdateManagerImpl.setLoadFinish( true );//cheyingkun add	//允许更新方法添加免责声明和桌面加载完成的判断
					//chenliang del start	//优化launcher加载完成后有卡顿的现象：1.使用线程池节省线程的开销，2.调整prefs参数判断是否初始化的位置，初始化完成后不再开启一条新的线程。
					//					statisOnResume();
					//					//umeng统计
					//					umengStatisOnResume();
					//					//智能分类
					//					initOperateHelp();
					//					//sdk
					//					initCooeeSdk();
					//					initOperateDynamic();
					//					//cheyingkun add start	//phenix1.1稳定版移植酷生活
					//					//lvjiangbin -1 page add start
					//					//cheyingkun add start	//phenix1.1稳定版移植酷生活
					//					//					initFavoritesPlugin(); //酷生活不再 在此处进行初始化
					//					//cheyingkun add end	//phenix1.1稳定版移植酷生活
					//					if( FavoritesPageManager.getInstance( mLauncher ).isInitFinish() )
					//					{
					//						setFavoritesAllApps();
					//					}
					//					//lvjiangbin -1 page add end
					//					//cheyingkun add end
					//					checkIntegrity();//xiatian add	//CheckIntegrity（代码框架）
					//					//对于自更新下来的版本：（1）检查错误信息并上传；（2）连续2次崩溃，弹出卸载应用的提示界面
					//					if( isNeedCheckError( Launcher.this ) && checkErrorAndUpload() )//
					//					{
					//						UpdateUtil.uninstallErrorApk( getApplicationContext() , getPackageName() );//
					//					}
					//					// zhangjin@2015/12/24 ADD START					
					//					new Thread( new Runnable() {
					//						
					//						public void run()
					//						{
					//							//xiatian add start	//优化第一次联网时的逻辑。
					//							//【备注】第一次联网时，会new以下类。放到此处处理new，优化第一次联网时，性能很差的手机，卡顿的问题
					//							MicroEntryHelper.getInstance( Launcher.this );
					//							DynamicEntryHelper.getInstance( Launcher.this );
					//							CategoryHelper.getInstance( Launcher.this );
					//							KmobConfigHelper.getInstance( Launcher.this );//kmob 专属页
					//							KuSoHelper.getInstance( Launcher.this );//kuso add
					//							StatisticsBXUpdate.getInstance( Launcher.this );//StatisticsBXUpdate add
					//							PspreadHelper.getInstance( Launcher.this );
					//							//xiatian add end
					//							UpdateManagerImpl.addUniqueListener( UiupdateHelper.getInstance( Launcher.this ) );
					//						}
					//					} ).start();
					//					checkUpdatePrompt();
					//chenliang del end
				}
			};
			//			new Thread(mRunnable1).start();		//chenliang del
			//chenliang add start
			Runnable mRunnable2 = new Runnable() {
				
				public void run()
				{
					statisOnResume();
					//umeng统计
					umengStatisOnResume();
					//智能分类
					initOperateHelp();
					//sdk
					initCooeeSdk();
					initOperateDynamic();
					//cheyingkun add start	//phenix1.1稳定版移植酷生活
					//lvjiangbin -1 page add start
					//cheyingkun add start	//phenix1.1稳定版移植酷生活
					//					initFavoritesPlugin(); //酷生活不再 在此处进行初始化
					//cheyingkun add end	//phenix1.1稳定版移植酷生活
					if( FavoritesPageManager.getInstance( mLauncher ).isInitFinish() )
					{
						setFavoritesAllApps();
					}
					//lvjiangbin -1 page add end
					//cheyingkun add end
					checkIntegrity();//xiatian add	//CheckIntegrity（代码框架）
					//对于自更新下来的版本：（1）检查错误信息并上传；（2）连续2次崩溃，弹出卸载应用的提示界面
					if( isNeedCheckError( Launcher.this ) && checkErrorAndUpload() )//
					{
						UpdateUtil.uninstallErrorApk( getApplicationContext() , getPackageName() );//
					}
					// zhangjin@2015/12/24 ADD START
					checkUpdatePrompt();
				}
			};
			Runnable mRunnable3 = new Runnable() {
				
				public void run()
				{
					//xiatian add start	//优化第一次联网时的逻辑。
					//【备注】第一次联网时，会new以下类。放到此处处理new，优化第一次联网时，性能很差的手机，卡顿的问题
					MicroEntryHelper.getInstance( Launcher.this );
					DynamicEntryHelper.getInstance( Launcher.this );
					CategoryHelper.getInstance( Launcher.this );
					KmobConfigHelper.getInstance( Launcher.this );//kmob 专属页
					KuSoHelper.getInstance( Launcher.this );//kuso add
					StatisticsBXUpdate.getInstance( Launcher.this );//StatisticsBXUpdate add
					PspreadHelper.getInstance( Launcher.this );
					//xiatian add end
					UpdateManagerImpl.addUniqueListener( UiupdateHelper.getInstance( Launcher.this ) );
				}
			};
			//yangmengchao add start //统计各个模块的开关项（默认开关和当前开关） ：01、一键换壁纸02、相机页03、音乐页04、酷生活05、文件夹推荐应用06、相机页广告07、音乐页广告
			Runnable mRunnable4 = new Runnable() {
				
				public void run()
				{
					JSONObject append;
					String mSwitchValues = "switchValues";
					String mDefValue = "defValue";
					String mCurrentValue = "currentValue";
					String mCurrentValueMusicLyric = "currentValueMusicLyric";
					String mCurrentValueMusicAlbum = "currentValueMusicAlbum";
					String mSwitchOnekeyChangeWallpaperShowKey = "switch_one_key_change_wallpaper";
					String mSwitchCameraPageShowKey = "switch_enable_camerapage_show";
					String mSwitchMusicPageShowKey = "switch_enable_musicpage_show";
					String mSwitchFavoritesShowKey = "switch_enable_favorites";
					String mSwitchNativeDataForFolderShowKey = "switch_enable_native_data_for_folder";
					String mSwitchCameraPageADShowKey = "switch_enable_camerapage_ad_show";
					String mSwitchMusicPageADShowKey = "switch_enable_musicpage_ad_show";
					try
					{
						JSONObject objAboutStatistics = new JSONObject();
						//一键换壁纸
						JSONObject mSwitchVauleOnekeyChangeWallpaper = new JSONObject();
						JSONObject mSwitchVauleListOnekeyChangeWallpaper = new JSONObject();
						mSwitchVauleListOnekeyChangeWallpaper.put( mDefValue , LauncherDefaultConfig.getBoolean( R.bool.switch_onekey_change_wallpaper ) );
						mSwitchVauleOnekeyChangeWallpaper.put( mSwitchValues , mSwitchVauleListOnekeyChangeWallpaper );
						objAboutStatistics.put( mSwitchOnekeyChangeWallpaperShowKey , mSwitchVauleOnekeyChangeWallpaper );
						//相机页
						SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
						JSONObject mSwitchValuesListCamerapage = new JSONObject();
						mSwitchValuesListCamerapage.put( mDefValue , LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW );
						mSwitchValuesListCamerapage.put(
								mCurrentValue ,
								mSharedPreferences.getBoolean( OperateMediaPluginDataManager.OPERATE_CAMERAPAGE_SWITCH_KEY , LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW ) );
						JSONObject mSwitchValuesCamerapage = new JSONObject();
						mSwitchValuesCamerapage.put( mSwitchValues , mSwitchValuesListCamerapage );
						objAboutStatistics.put( mSwitchCameraPageShowKey , mSwitchValuesCamerapage );
						//音乐页
						JSONObject mSwitchValuesListMusicpage = new JSONObject();
						mSwitchValuesListMusicpage.put( mDefValue , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_musicpage_show ) );
						mSwitchValuesListMusicpage.put(
								mCurrentValue ,
								mSharedPreferences.getBoolean( OperateMediaPluginDataManager.OPERATE_MUSICPAGE_SWITCH_KEY , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_musicpage_show ) ) );
						JSONObject mSwitchValuesMusicpage = new JSONObject();
						mSwitchValuesMusicpage.put( mSwitchValues , mSwitchValuesListMusicpage );
						objAboutStatistics.put( mSwitchMusicPageShowKey , mSwitchValuesMusicpage );
						//酷生活
						JSONObject mSwitchValuesListFavorites = new JSONObject();
						mSwitchValuesListFavorites.put( mDefValue , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_favorites ) );
						mSwitchValuesListFavorites.put(
								mCurrentValue ,
								mSharedPreferences.getBoolean( OperateFavorites.OPERATE_FAVORITES_SWITCH_KEY , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_favorites ) ) );
						JSONObject mSwitchValuesFavorites = new JSONObject();
						mSwitchValuesFavorites.put( mSwitchValues , mSwitchValuesListFavorites );
						objAboutStatistics.put( mSwitchFavoritesShowKey , mSwitchValuesFavorites );
						//文件夹推荐应用
						SharedPreferences sp = mLauncher.getSharedPreferences( "NativeAdver" , Context.MODE_PRIVATE );
						JSONObject mSwitchValuesListFolder = new JSONObject();
						mSwitchValuesListFolder.put( mDefValue , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_native_data_for_folder ) );
						mSwitchValuesListFolder.put( mCurrentValue , sp.getBoolean( "operateNativeDataSwitch" , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_native_data_for_folder ) ) );
						JSONObject mSwitchValuesFolder = new JSONObject();
						mSwitchValuesFolder.put( mSwitchValues , mSwitchValuesListFolder );
						objAboutStatistics.put( mSwitchNativeDataForFolderShowKey , mSwitchValuesFolder );
						String result = KmobConfigHelper.getInstance( mLauncher ).getString( "result.content" );//从数据库拿到当前有关广告开关值的内容
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG + "ymc" , "result : " + result );
						boolean mBooleanCurrentValueCamera = false;
						boolean mBooleanCurrentValueMusicLyric = false;
						boolean mBooleanCurrentValueMusicAlbum = false;
						if( result != null )
						{
							JSONObject resJson = new JSONObject( result );
							int rc0 = resJson.optInt( "rc0" );
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG + "ymc" , "rc0 : " + rc0 );
							if( rc0 == 0 || rc0 == 100 ) //有更新和无更新都使用运营数据
							{
								//分情况
								int c0 = resJson.optInt( "c0" );
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.d( TAG + "ymc" , "c0 : " + c0 );
								switch( c0 )
								{
									case 0://广告关闭
										mBooleanCurrentValueCamera = false;
										mBooleanCurrentValueMusicLyric = false;
										mBooleanCurrentValueMusicAlbum = false;
										break;
									case 1://广告打开
										mBooleanCurrentValueCamera = true;
										mBooleanCurrentValueMusicLyric = true;
										mBooleanCurrentValueMusicAlbum = true;
										break;
									case 2://分广告位控制开关
										JSONArray jsonArray = resJson.getJSONArray( "c4" );
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
											Log.d( TAG + "ymc" , "jsonArray : " + jsonArray.toString() );
										if( jsonArray.length() > 0 )
										{
											for( int i = 0 ; i < jsonArray.length() ; i++ )
											{
												JSONObject obj = jsonArray.getJSONObject( i );
												switch( obj.optString( "id" ) )
												{
													case KmobConfigData.CAMERA_ADPLACE_ID://相机页
														mBooleanCurrentValueCamera = ( obj.optInt( "on" ) == 1 );
														break;
													case KmobConfigData.LYRIC_ADPLACE_ID://音乐页歌词位
														mBooleanCurrentValueMusicLyric = ( obj.optInt( "on" ) == 1 );
														break;
													case KmobConfigData.ALBUM_ADPLACE_ID://音乐页专辑封面位
														mBooleanCurrentValueMusicAlbum = ( obj.optInt( "on" ) == 1 );
														break;
													default:
														break;
												}
											}
										}
										break;
									default:
										break;
								}
							}
							else
								if( rc0 == 200 ) //使用本地配置
							{
								mBooleanCurrentValueCamera = BaseDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_AD_SHOW;
								mBooleanCurrentValueMusicLyric = BaseDefaultConfig.SWITCH_ENABLE_MUSICPAGE_AD_SHOW;
								mBooleanCurrentValueMusicAlbum = BaseDefaultConfig.SWITCH_ENABLE_MUSICPAGE_AD_SHOW;
							}
						}
						//相机页广告
						JSONObject mSwitchValuesListCamerapageAD = new JSONObject();
						mSwitchValuesListCamerapageAD.put( mDefValue , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_camerapage_ad_show ) );
						mSwitchValuesListCamerapageAD.put( mCurrentValue , mBooleanCurrentValueCamera );
						JSONObject mSwitchValuesCamerapageAD = new JSONObject();
						mSwitchValuesCamerapageAD.put( mSwitchValues , mSwitchValuesListCamerapageAD );
						objAboutStatistics.put( mSwitchCameraPageADShowKey , mSwitchValuesCamerapageAD );
						//音乐页广告
						JSONObject mSwitchValuesListMusicpageAD = new JSONObject();
						mSwitchValuesListMusicpageAD.put( mDefValue , LauncherDefaultConfig.getBoolean( R.bool.switch_enable_musicpage_ad_show ) );
						mSwitchValuesListMusicpageAD.put( mCurrentValueMusicLyric , mBooleanCurrentValueMusicLyric );
						mSwitchValuesListMusicpageAD.put( mCurrentValueMusicAlbum , mBooleanCurrentValueMusicAlbum );
						JSONObject mSwitchValuesMusicpagesAD = new JSONObject();
						mSwitchValuesMusicpagesAD.put( "switchValues" , mSwitchValuesListMusicpageAD );
						objAboutStatistics.put( "switch_enable_musicpage_ad_show" , mSwitchValuesMusicpagesAD );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG + "ymc" , "objAboutStatistics : " + objAboutStatistics.toString() );
						append = new JSONObject();
						append.put( "param1" , objAboutStatistics );
						//统计处理
						StatisticsExpandNew.onCustomEvent(
								mLauncher ,
								"statisticsaboutmodulesswitches" ,
								LauncherConfigUtils.getSN( mLauncher ) ,
								LauncherConfigUtils.getAppID( mLauncher ) ,
								LauncherConfigUtils.cooeeGetCooeeId( mLauncher ) ,
								4 ,
								getPackageName() ,
								String.valueOf( LauncherConfigUtils.getLauncherVersion( mLauncher ) ) ,
								append );
					}
					catch( Exception e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			//yangmengchao add end 
			//由多个线程同时进行改为单个线程依序进行。
			ExecutorService runTaskPool = Executors.newSingleThreadExecutor();
			ArrayList<Runnable> initTaskList = new ArrayList<Runnable>();
			initTaskList.add( mRunnable1 );
			initTaskList.add( mRunnable2 );
			initTaskList.add( mRunnable3 );
			initTaskList.add( mRunnable4 ); //yangmengchao add //统计各个模块的开关项（默认开关和当前开关）  ：01、一键换壁纸02、相机页03、音乐页04、酷生活05、文件夹推荐应用06、相机页广告07、音乐页广告
			for( int i = 0 ; i < initTaskList.size() ; i++ )
			{
				runTaskPool.execute( initTaskList.get( i ) );
			}
			//chenliang add end
		}
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			final PackageManager packageManager = getPackageManager();
			final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
			mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
			List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "cyk_bug : c_0003400" , "cyk launcher loadFinish: apps********start " );
			for( int i = 0 ; i < apps.size() ; i++ )
			{
				ResolveInfo app = apps.get( i );
				final String packageName = app.activityInfo.applicationInfo.packageName;
				ComponentName componentName = new ComponentName( packageName , app.activityInfo.name );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcher loadFinish: componentName " , componentName.toString() ) );
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcher loadFinish: apps********end " , apps.size() ) );
		}
		KmobAdverManager.getKmobAdverManager( this ).initOperateNativeData();//cheyingkun add	//文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
		//cheyingkun add end
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
	public void downloadApkCooeeDialog(
			final String mTitle ,
			final String mPackageName ,
			final boolean mIsInstallAfterDownloadComplete )
	{
		DownloadManager.downloadApkCooeeDialog( this , mTitle , mPackageName , mIsInstallAfterDownloadComplete );
	}
	
	//xiatian add end
	@Override
	public FolderInfo getSameOperateFolder(
			String dynamicID )
	{
		// TODO Auto-generated method stub
		if( dynamicID == null )
		{
			return null;
		}
		HashMap<Long , FolderInfo> bgFolders = LauncherAppState.getInstance().getModel().getBgFolders();
		Set<Long> keys = bgFolders.keySet();
		for( Long key : keys )
		{
			FolderInfo info = bgFolders.get( key );
			if( info != null )
			{
				if( info.getOperateIntent() != null )
				{
					String id = info.getOperateIntent().getStringExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID );
					if( dynamicID.equals( id ) )
					{
						return info;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public void clearOperateIcon(
			String[] pkgNames )
	{
		// TODO Auto-generated method stub
		if( mOperateDynamicMain != null )
		{
			for( String pkg : pkgNames )
			{
				mOperateDynamicMain.removeFolderIcon( pkg );
			}
		}
	}
	
	//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
	@Override
	public void bindDefaultPage(
			final Callbacks oldCallbacks )
	{
		Runnable r = new Runnable() {
			
			public void run()
			{
				bindDefaultPage( oldCallbacks );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		mWorkspace.bindDefaultPage();
	}
	
	//xiatian add end
	// cheyingkun add start //免责声明布局
	@Override
	public void onClickDisclaimerDialogButton(
			View v ,
			int style )
	{
		if( style == DisclaimerManager.LAUNCHRE_ONCREATE_DISCLAIMER )
		{
			switch( v.getId() )
			{
				case R.id.dialog_button_ok:
					//					showFirstRunCling();//cheyingkun del	//修改新手引导显示逻辑，启动页显示完再显示新手引导。
					showLoadingPage();//cheyingkun add	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
					showCurPageViews();
					initCooeeSdk();
					statisOnResume();
					umengStatisOnResume();
					break;
				case R.id.exit:
					finish();
					break;
			}
		}
		else if( style == DisclaimerManager.VISIT_NETWORK_DISCLAIMER_SEARCH )
		{
			switch( v.getId() )
			{
				case R.id.dialog_button_positive:
					onSearchRequested();
					break;
			}
		}
	}
	
	// cheyingkun add end
	@Override
	public Folder getOpenFolderNotInPause()
	{
		// TODO Auto-generated method stub
		if( !mPaused )
		{
			return mWorkspace.getOpenFolder();
		}
		return null;
	}
	
	@Override
	public ArrayList<View> getAllShortcutInworkspace()
	{
		// TODO Auto-generated method stub
		if( mWorkspace != null )
			return mWorkspace.getAllShortcutInworkspace();
		return null;
	}
	
	//xiatian add start	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
	@Override
	public void showAPKS(
			String[] packages )
	{
		if( mModel == null )
		{
			return;
		}
		mModel.enqueuePackageUpdatedApp( 1/* PackageUpdatedTask.OP_OP_ADD */, packages );
	}
	
	@Override
	public boolean canShowAPKS()
	{
		if( mModel == null )
		{
			return false;
		}
		return true;
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
	private void showLoadingPage()
	{
		//cheyingkun add start	//解决“由于启动页消失从桌面加载结束提前到当前页bind结束，在开启免责声明并且当前页bind结束桌面没加载结束时,点击免责声明继续使用按钮后,启动页不消失”的问题。
		//启动页消失从加载结束提前到当前页bind结束,相应的修改启动页显示的判断逻辑
		if( mModel != null && !mModel.isFinishBindWrokspaceCurrentScreen() )
		//cheyingkun add end
		{
			switch( LauncherDefaultConfig.CONFIG_LOADING_PAGE_STYLE )
			{
				case LauncherDefaultConfig.LOADING_PAGE_STYLE_PROGRESS:
					showLauncherLoadingProgress();
					break;
				case LauncherDefaultConfig.LOADING_PAGE_STYLE_DEFAULT:
					showLauncherLoadingDefault();
					break;
				default:
					break;
			}
		}
	}
	
	public void dismissLoadingPage()
	{
		switch( LauncherDefaultConfig.CONFIG_LOADING_PAGE_STYLE )
		{
			case LauncherDefaultConfig.LOADING_PAGE_STYLE_PROGRESS:
				dismissLauncherLoadingProgress();
				break;
			case LauncherDefaultConfig.LOADING_PAGE_STYLE_DEFAULT:
				dismissLauncherLoadingDefault();
				break;
			default:
				break;
		}
		//cheyingkun add start	//修改新手引导显示逻辑，启动页显示完再显示新手引导。
		runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				showFirstRunCling();
			}
		} );
		//cheyingkun add end
	}
	
	private void showLauncherLoadingDefault()
	{
		if( mLauncherLoadingPhenix == null )
		{
			//			mLauncherLoadingPhenix = (LauncherLoading)mDragLayer.findViewById( R.id.launcher_loading );
			mLauncherLoadingPhenix = (LauncherLoading)LayoutInflater.from( this ).inflate( R.layout.loading , null );
		}
		if( mLauncherLoadingPhenix.getParent() == null )
		{
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.MATCH_PARENT );
			mDragLayer.addView( mLauncherLoadingPhenix , params );
		}
		mLauncherLoadingPhenix.setLoadFinish( false );
		mLauncherLoadingPhenix.showLauncherLoadingAnim();
	}
	
	private void dismissLauncherLoadingDefault()
	{
		runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( mLauncherLoadingPhenix != null )
				{
					mLauncherLoadingPhenix.setLoadFinish( true );
					mLauncherLoadingPhenix.dismissLauncherLoadingAnim();
				}
			}
		} );
	}
	
	private void showLauncherLoadingProgress()
	{
		if( mLauncherLoadingProgress == null )
		{
			ProgressView progressView = new ProgressView( this );
			progressView.setLoadState( LauncherDefaultConfig.getString( R.string.loading_launcher_progress_text ) );
			mLauncherLoadingProgress = progressView.getProgressView();
			mLauncherLoadingProgress.setBackgroundResource( R.drawable.startload_progress_bg_black );
		}
		if( mLauncherLoadingProgress.getParent() == null )
		{
			mDragLayer.addView( mLauncherLoadingProgress );
		}
	}
	
	private void dismissLauncherLoadingProgress()
	{
		runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( mLauncherLoadingProgress != null && mLauncherLoadingProgress.getParent() != null && mLauncherLoadingProgress.getParent() instanceof DragLayer )
				{
					mDragLayer.removeView( mLauncherLoadingProgress );
					mLauncherLoadingProgress = null;
					//xiatian add start	//设置默认桌面引导
					if( !LauncherDefaultConfig.SWITCH_ENABLE_CLINGS )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE && !DefaultLauncherGuideManager.getInstance().isOnlyLauncher( mLauncher ) )
						{
							DefaultLauncherGuideManager.getInstance().checkDefaultLauncherAndShowGuideDialog( true , Launcher.this );
						}
					}
					//xiatian add end
				}
			}
		} );
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//需求：编辑模式底边栏配置打开特定界面。预留两个可配置的button，这两个button配置intent即可打开特定界面（详见“BaseDefaultConfig”中说明）。
	public void enterOverviewPanelButtonFallback_1(
			View v )
	{
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		if( LauncherDefaultConfig.CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_1_INTENT == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "OVERVIEW_PANEL_BUTTON" , "( FALLBACK_1_COMPONENTNAME == null )" );
			return;
		}
		startActivity( LauncherDefaultConfig.CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_1_INTENT );
	}
	
	public void enterOverviewPanelButtonFallback_2(
			View v )
	{
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		if( LauncherDefaultConfig.CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_2_INTENT == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "OVERVIEW_PANEL_BUTTON" , "( FALLBACK_2_COMPONENTNAME == null )" );
			return;
		}
		startActivity( LauncherDefaultConfig.CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_2_INTENT );
	}
	//xiatian add end
	;
	
	public void enterWidgets(
			View v )
	{
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		boolean mIsFromWorkspaceMenuListItem = isWorkspaceMenuListItem( v );//xiatian add	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		//cheyingkun add start	//添加友盟统计自定义事件(编辑模式小部件)
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			if( mIsFromOverviewPanelButton )
			{
				MobclickAgent.onEvent( this , UmengStatistics.ENTER_WIDGETS_BY_EDIT_MODE );
			}
			//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
			else if( mIsFromWorkspaceMenuListItem )
			{
			}
			//xiatian add end
		}
		//cheyingkun add end
		if( mIsFromOverviewPanelButton )
		{
			showAllApps( true , AppsCustomizePagedView.ContentType.Widgets , true );
		}
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		else if( mIsFromWorkspaceMenuListItem )
		{
			showSystemPickWidgetActivity();
		}
		//xiatian add end
	}
	
	// zhangjin@2015/08/31 ADD START
	public void updateIconHouse(
			final ComponentName componentName )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "updateIcon " , componentName.toString() ) );
		if( mWorkspace != null )
		{
			mWorkspace.updateIconHouse( componentName );
		}
		if( mAppsCustomizeContent != null && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Applications )
		{
			mAppsCustomizeContent.updateIconHouse( componentName );
		}
	}
	
	// zhangjin@2015/08/31 ADD END
	// zhangjin@2015/09/01 ADD START
	public boolean isCmpVisible(
			final ComponentName componentName )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "canUpdate " , componentName.toString() ) );
		if( mWorkspace != null && mWorkspace.isShown() && mWorkspace.isCmpVisible( componentName ) )
		{
			return true;
		}
		if( mAppsCustomizeContent != null && mAppsCustomizeContent.isShown() && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Applications )
		{
			return mAppsCustomizeContent.isCmpVisible( componentName );
		}
		return false;
	}
	
	// zhangjin@2015/09/01 ADD END
	// zhangjin@2015/12/24 ADD START
	//更新提示
	private void checkUpdatePrompt()
	{
		SharedPreferences pref = getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		long updateVersion = pref.getLong( UpdateUiManager.KEY_UPDATE_VERSION_IN_SP , 0 );
		int runcount = pref.getInt( UpdateUiManager.KEY_UPDATE_PROMPT_COUNT_IN_SP , -1 );
		//更新图标显示更新标志
		long curVersion = Long.parseLong( UpdateUiManager.getInstance().getVersionCode() );
		UpdateIconManager.getInstance().setHasUpdate( updateVersion > curVersion );
		//更新消息通知
		if( runcount != -1 )
		{
			int display = pref.getInt( UpdateUiManager.KEY_UPDATE_DISPLAY_IN_SP , 1 );
			UpdateNotificationManager.getInstance().startUpdatePrompt( runcount , updateVersion , display );
		}
	}
	
	public void changeUpdateIcon()
	{
		updateUnreadNumberByComponent( UpdateIconManager.getInstance().getUpdateCmp() , 0 );
	}
	// zhangjin@2015/12/24 ADD END
	;
	
	//xiatian add start	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
	public void changeGlobalSearchToKuSoIfNecessary()
	{
		if(
		//
		(
		//
		( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE/* //桌面显示搜索 */)
		//
		|| ( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES && LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE /* //cheyingkun add	//phenix1.1稳定版移植酷生活  */)
		//
		)
		//
		&& ( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_GLOBAL_SEARCH/* //是安卓的全局搜索 */)
		//
		&& ( hasGlobalSearch() == false /* //没有支持全局搜索的apk */)
		//
		)
		{//默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
			LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE = LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE;
			reloadGlobalIcons();
		}
	}
	//xiatian add end
	;
	
	private void reloadGlobalIcons()
	{
		//cheyingkun add start	//修改运营酷搜逻辑(改为锁屏时桌面重启)
		//		mSearchDropTargetBar.removeView( getSearchBar() );
		//		mSearchBar = null;
		//		getSearchBar();
		//		if( mSearchDropTargetBar != null )
		//		{
		//			mSearchDropTargetBar.setupByReloadGlobalIcons( this );
		//		}
		//cheyingkun add end
		int coi = getCurrentOrientationIndexForGlobalIcons();
		sGlobalSearchIcon[coi] = null;
		updateGlobalIcons();
		updateAppMarketIcon( SHOW_MARKET_BUTTON );//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//启动速度优化(小部件更新放在加载完成之后)
	/**
	 * 显示小部件加载提示信息
	 * @param contentType
	 */
	private void showWidgetListLoadHint(
			AppsCustomizePagedView.ContentType contentType )
	{
		if( !isLoadFinish && contentType == AppsCustomizePagedView.ContentType.Widgets )//小部件 
		{
			if( mWidgetListLoadHint == null )
			{
				mWidgetListLoadHint = new ProgressView( this );
			}
			mWidgetListLoadHint.setLoadState( R.string.allApp_load_hint );
			mDragLayer.addView( mWidgetListLoadHint.getProgressView() );
		}
	}
	
	/**
	 * 隐藏小部件加载提示信息
	 */
	private void hideWidgetListLoadHint()
	{
		runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( mWidgetListLoadHint != null && mWidgetListLoadHint.getProgressView().getParent() != null )
				{
					mDragLayer.removeView( mWidgetListLoadHint.getProgressView() );
				}
			}
		} );
	}
	//cheyingkun add end
	;
	
	/**
	 * 切到上一页
	 */
	public void scrollToLeft()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "scrollToLeft" , "scrollToLeft" );
		if( mState == State.WORKSPACE )//xiatian add	//需求：拓展config_scroll_by_broadcast的功能，支持“主菜单”和“小组件页面”切页。【c_0004647】
		{
			mWorkspace.exitWidgetResizeMode();
			if( canSnapPage() )
			{
				mWorkspace.scrollLeft( mWorkspace.isLoop() );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "scrollToLeft" , "mWorkspace.scrollLeft()" );
			}
		}
		//xiatian add start	//需求：拓展config_scroll_by_broadcast的功能，支持“主菜单”和“小组件页面”切页。【c_0004647】
		else if( mState == State.APPS_CUSTOMIZE )
		{
			if( getAppsMode() == AppsCustomizePagedView.NORMAL_MODE )
			{
				if( mAppsCustomizeContent.getVisibility() == View.VISIBLE )
				{
					mAppsCustomizeContent.scrollLeft( mAppsCustomizeContent.isLoop() );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "scrollToLeft" , "mAppsCustomizeContent.scrollLeft()" );
				}
			}
		}
		//xiatian add end
	}
	
	/**
	 * 切到下一页
	 */
	public void scrollToRight()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "scrollToRight" , "scrollToRight" );
		if( mState == State.WORKSPACE )//xiatian add	//需求：拓展config_scroll_by_broadcast的功能，支持“主菜单”和“小组件页面”切页。【c_0004647】
		{
			mWorkspace.exitWidgetResizeMode();
			if( canSnapPage() )
			{
				mWorkspace.scrollRight( mWorkspace.isLoop() );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "scrollToRight" , "mWorkspace.scrollRight()" );
			}
		}
		//xiatian add start	//需求：拓展config_scroll_by_broadcast的功能，支持“主菜单”和“小组件页面”切页。【c_0004647】
		else if( mState == State.APPS_CUSTOMIZE )
		{
			if( getAppsMode() == AppsCustomizePagedView.NORMAL_MODE )
			{
				if( mAppsCustomizeContent.getVisibility() == View.VISIBLE )
				{
					mAppsCustomizeContent.scrollRight( mAppsCustomizeContent.isLoop() );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "scrollToRight" , "mAppsCustomizeContent.scrollRight()" );
				}
			}
		}
		//xiatian add end
	}
	
	public boolean canSnapPage()
	{
		boolean ret = false;
		Folder openFolder = mWorkspace.getOpenFolder();
		if(
		//
		openFolder == null
		//
		&& mState == State.WORKSPACE
		//
		&& !mWorkspace.isTouchActive()
		//
		&& ( OperateHelp.getInstance( this ).isGettingOperateDate() == false/* 正在智能分类，不切页 */)
		//
		&& ( mOperateDynamicMain != null && mOperateDynamicMain.isOperateFolderRunning() == false/* 运营文件夹正在往桌面加载的过程中，不切页（这是切页会很卡） */)
		//
		)
		{
			ret = true;
		}
		return ret;
	}
	
	//xiatian add start	//需求：运营友盟（详见“OperateUmeng”中说明）。
	@Override
	public void notifyUmengSwitch(
			final boolean arg0 )
	{
		Runnable mRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "OperateUmeng" , StringUtils.concat( "launcher - notifyUmengSwitch:" , arg0 ) );
				if( arg0 )
				{//use Umeng
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG == false )
					{
						LauncherDefaultConfig.SWITCH_ENABLE_UMENG = true;
						SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
						Editor mEditor = mSharedPreferences.edit();
						mEditor.putInt( OperateUmeng.OPERATE_UMENG_NEED_ENABLE_UMENG_SWITCH_KEY , 1 );
						mEditor.commit();
					}
				}
				else
				{//unuse Umeng
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					{
						LauncherDefaultConfig.SWITCH_ENABLE_UMENG = false;
						SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
						Editor mEditor = mSharedPreferences.edit();
						mEditor.putInt( OperateUmeng.OPERATE_UMENG_NEED_ENABLE_UMENG_SWITCH_KEY , 0 );
						mEditor.commit();
					}
				}
			}
		};
		runOnUiThread( mRunnable );
	}
	//xiatian add end
	;
	
	//xiatian add start	//在桌面onResume的时候，发送广播通知客户。配置为空则不发送广播。默认为空。	
	public void onResumeNotify()
	{
		if( TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_ONRESUME_BROADCAST_ACTION ) )
		{
			return;
		}
		sendBroadcast( new Intent( LauncherDefaultConfig.CONFIG_ONRESUME_BROADCAST_ACTION ) );
	}
	//xiatian add end	
	;
	
	//xiatian add start	//在桌面onStop的时候，发送广播通知客户。配置为空则不发送广播。默认为空。	
	public void onStopNotify()
	{
		if( TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_ONSTOP_BROADCAST_ACTION ) )
		{
			return;
		}
		sendBroadcast( new Intent( LauncherDefaultConfig.CONFIG_ONSTOP_BROADCAST_ACTION ) );
	}
	
	//xiatian add end
	@Override
	public void removeViewFromWorkspace(
			final HashSet<ComponentName> cns ,
			final boolean isDeleteInfolder )
	{
		// TODO Auto-generated method stub
		if( cns == null || cns.size() <= 0 )
		{
			return;
		}
		Runnable r = new Runnable() {
			
			public void run()
			{
				removeViewFromWorkspace( cns , isDeleteInfolder );
			}
		};
		if( waitUntilResume( r ) )
		{
			return;
		}
		if( mWorkspace != null )
		{
			mWorkspace.removeItemsByComponentName( cns , isDeleteInfolder );
		}
	}
	
	//xiatian add start	//添加智能分类请求数据的Callback
	@Override
	public boolean canCategoryUpdate()
	{
		return true;
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//应用自动创建快捷方式时，优化判断是否存在的方法。【c_0003813】
	/**
	 * 跟桌面所有的图标进行比较,检测快捷方式是否存在
	 */
	@Override
	public boolean shortcutExistsByWorkspaceItems(
			String name ,
			Intent intent )
	{
		if( name == null || intent == null )
		{
			return false;
		}
		ComponentName mComponentNameSource = intent.getComponent();
		if( mComponentNameSource == null )
		{
			return false;
		}
		if( mWorkspace == null )
		{
			return false;
		}
		//比较桌面所有的图标
		int childCount = mWorkspace.getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			CellLayout currentLayout = (CellLayout)getWorkspace().getChildAt( i );
			if( shortcutExistsByCellLayout( currentLayout , name , intent ) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断cellLayout中是否存在名称和包类名相同的图标
	 * @param currentLayout 目标cellLayout
	 * @param name 要判断的名称
	 * @param intent 要判断的intent
	 * @return true存在,false不存在
	 */
	private boolean shortcutExistsByCellLayout(
			final CellLayout currentLayout ,
			final String name ,
			final Intent intent )
	{
		if( name == null || intent == null )
		{
			return false;
		}
		ComponentName mComponentNameSource = intent.getComponent();
		if( mComponentNameSource == null )
		{
			return false;
		}
		if( currentLayout == null )
		{
			return false;
		}
		int count = currentLayout.getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View view = currentLayout.getChildAt( i );
			if( view instanceof ShortcutAndWidgetContainer )
			{
				ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer)view;
				int itemCount = shortcutAndWidgetContainer.getChildCount();
				for( int j = 0 ; j < itemCount ; j++ )
				{
					View v = shortcutAndWidgetContainer.getChildAt( j );
					if( v instanceof FolderIcon )//文件夹
					{
						FolderIcon folderIcon = (FolderIcon)v;
						Folder folder = folderIcon.getFolder();
						int folderChildCount = folder.getChildCount();
						for( int k = 0 ; k < folderChildCount ; k++ )
						{
							View folderChild = folder.getChildAt( k );
							if( folderChild instanceof ScrollView )
							{
								ScrollView mScrollView = (ScrollView)folderChild;
								int mScrollViewChildCount = mScrollView.getChildCount();
								for( int l = 0 ; l < mScrollViewChildCount ; l++ )
								{
									View mScrollViewChild = mScrollView.getChildAt( l );
									if( mScrollViewChild instanceof CellLayout )
									{
										CellLayout mCellLayout = (CellLayout)mScrollViewChild;
										shortcutExistsByCellLayout( mCellLayout , name , intent );
									}
								}
							}
						}
					}
					else if( v instanceof BubbleTextView )//图标
					{
						BubbleTextView mBubbleTextView = (BubbleTextView)v;
						Object tag = mBubbleTextView.getTag();
						if( tag instanceof ShortcutInfo )
						{
							ShortcutInfo info = (ShortcutInfo)tag;
							String infoTitle = info.getTitle();
							Intent infoIntent = info.getIntent();
							if( !TextUtils.isEmpty( infoTitle ) && infoIntent != null )
							{
								ComponentName infoComponent = infoIntent.getComponent();
								if( infoComponent != null )
								{
									if( infoTitle.equals( name ) && infoComponent.equals( mComponentNameSource ) )
									{
										return true;
									}
								}
							}
						}
					}
					else if( v instanceof LauncherAppWidgetHostView )//插件
					{
					}
					else
					{
					}
				}
			}
		}
		return false;
	}
	
	//cheyingkun add end
	@Override
	public void notifyKuSoSwitch(
			boolean mCommonShowSearchOld ,
			boolean mFavoritesShowSearchOld ,
			boolean mCommonShowSearchNew ,
			boolean mFavoritesShowSearchNew )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "OperateKuso" , StringUtils.concat(
					"launcher - notifyKuSoSwitch:old_commonShowSearch = " ,
					mCommonShowSearchOld ,
					" old_favoritesShowSearch=  " ,
					mFavoritesShowSearchOld ,
					" new_commonShowSearch=  " ,
					mCommonShowSearchNew ,
					" new_favoritesShowSearch=  " ,
					mFavoritesShowSearchNew ) );
		//获取开关当前的值
		boolean mCooeeSearchOld = ( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE );
		if( SearchHelper.getInstance( this ).enableCooeeSearch() )
		{
			LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE = LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE;
		}
		boolean mCooeeSearchNew = ( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "OperateKuso" , StringUtils.concat( "launcher - notifyKuSoSwitch: new_cooeeSearch = " , LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE , " old_cooeeSearch=  " , mCooeeSearchOld ) );
		//
		//
		if(
		//
		mCommonShowSearchOld == mCommonShowSearchNew
		//
		&& mFavoritesShowSearchOld == mFavoritesShowSearchNew
		//
		&& ( mCooeeSearchOld == mCooeeSearchNew/* //cheyingkun add	//完善运营酷搜相关逻辑 */)
		//
		)
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "OperateKuso" , " return " );
			return;
		}
		search_need_restart = true;//cheyingkun add	//修改运营酷搜逻辑(改为锁屏时桌面重启)
		//cheyingkun del start	//修改运营酷搜逻辑(改为锁屏时桌面重启)
		//		Runnable mRunnable = new Runnable() {
		//			
		//			@Override
		//			public void run()
		//			{
		//				//修改版(图标边距分开逻辑__搜索从有到无界面异常)
		//				LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = new_commonShowSearch;
		//				LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE = new_favoritesShowSearch;
		//				LauncherDefaultConfig.SWITCH_ENABLE_ASK_SEARCH = getResources().getBoolean( R.bool.switch_enable_ask_search );
		//				if( LauncherDefaultConfig.SWITCH_ENABLE_COOEE_SEARCH )
		//				{//酷搜
		//					LauncherDefaultConfig.SWITCH_ENABLE_ASK_SEARCH = false;//cheyingkun add	//搜索栏支持铂睿智恒ASK搜索【c_0004075】
		//				}
		//				//布局调整
		//				if( ( !old_commonShowSearch && new_commonShowSearch ) || ( !old_favoritesShowSearch && new_favoritesShowSearch ) )
		//				{//搜索从无到有
		//				}
		//				else if( ( old_commonShowSearch && !new_commonShowSearch ) || ( old_favoritesShowSearch && !new_favoritesShowSearch ) )
		//				{//搜索从有到无
		//					//调整桌面布局
		//					LauncherAppState app = LauncherAppState.getInstance();
		//					DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//					grid.layout( mLauncher );
		//					//酷生活重新设置边距
		//					mWorkspace.setCustomScreenPadding();
		//					reloadGlobalIcons();
		//				}
		//				//
		//				//酷搜状态变换 
		//				if( LauncherDefaultConfig.SWITCH_ENABLE_COOEE_SEARCH != oldCooeeSearch )
		//				{//更新搜索栏图标
		//					reloadGlobalIcons();
		//				}
		//				//
		//				//
		//				//修改版(图标边距混合逻辑__)
		//				/*if( LauncherDefaultConfig.SWITCH_ENABLE_COOEE_SEARCH )//cheyingkun add	//完善运营酷搜相关逻辑
		//				{//打开酷搜或者打开本地酷搜开关
		//					if( new_commonShowSearch || new_favoritesShowSearch )
		//					{//如果显示搜索栏,更新搜索栏图标
		//						LauncherDefaultConfig.SWITCH_ENABLE_ASK_SEARCH = false;//cheyingkun add	//搜索栏支持铂睿智恒ASK搜索【c_0004075】
		//					}
		//					if( !new_commonShowSearch || !new_favoritesShowSearch )
		//					{
		//						//调整桌面布局
		//						LauncherAppState app = LauncherAppState.getInstance();
		//						DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//						grid.layout( mLauncher );
		//						//酷生活重新设置边距
		//						mWorkspace.setCustomScreenPadding();
		//					}
		//				}
		//				else if( !LauncherDefaultConfig.SWITCH_ENABLE_COOEE_SEARCH )//cheyingkun add	//完善运营酷搜相关逻辑
		//				{//关闭运营酷搜并且本地关闭酷搜开关
		//					//0、更新开关
		//					//cheyingkun add start	//优化客户搜索和运营酷搜相关逻辑
		//					LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = KuSoHelper.getInstance( Launcher.this ).enableShowCommonPageSearch();
		//					LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE = KuSoHelper.getInstance( Launcher.this ).enableShowFavoritesPageSearch();
		//					//cheyingkun add end
		//					LauncherDefaultConfig.SWITCH_ENABLE_ASK_SEARCH = getResources().getBoolean( R.bool.switch_enable_ask_search );
		//					//cheyingkun add end
		//					//1、没有任何搜索，主动设置不显示搜索
		//					boolean mIsShowGlobalSearch = hasGlobalSearch();
		//					if( mIsShowGlobalSearch == false//没有全局搜索
		//							//没有ask搜索
		//							&& !LauncherDefaultConfig.SWITCH_ENABLE_ASK_SEARCH//cheyingkun add	//搜索栏支持铂睿智恒ASK搜索【c_0004075】
		//					)
		//					{//隐藏搜索栏
		//						LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = false;
		//						//cheyingkun add start	//phenix1.1稳定版移植酷生活
		//						if( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES )
		//						{
		//							LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE = false;
		//						}
		//						//cheyingkun add end
		//						//如果没有globalSearch,并且没有ask搜索(应该调整桌面布局)
		//						LauncherAppState app = LauncherAppState.getInstance();
		//						DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//						grid.layout( mLauncher );
		//						//酷生活重新设置边距
		//						mWorkspace.setCustomScreenPadding();//cheyingkun add	//优化客户搜索和运营酷搜相关逻辑
		//					}
		//					//2、本地配置不显示搜索
		//					if( !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE || !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE )
		//					{
		//						//调整桌面布局
		//						LauncherAppState app = LauncherAppState.getInstance();
		//						DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//						grid.layout( mLauncher );
		//						//酷生活重新设置边距
		//						mWorkspace.setCustomScreenPadding();
		//					}
		//					//3、重进加载搜索框
		//					reloadGlobalIcons();
		//					//cheyingkun add start	//优化客户搜索和运营酷搜相关逻辑
		//					if( old_commonShowSearch && old_favoritesShowSearch //
		//						&& new_commonShowSearch && !new_favoritesShowSearch )
		//					{//调整-1屏布局
		//						mWorkspace.setCustomScreenPadding();
		//					}
		//					//cheyingkun add end
		//					}
		//					*/
		//			}
		//		};
		//		runOnUiThread( mRunnable );
		//cheyingkun del end
	}
	
	//xiatian add start	//CheckIntegrity（代码框架）
	private void checkIntegrity()
	{
		mCheckIntegrityManager = new CheckIntegrityManager( this );
		if( mCheckIntegrityManager.isNeed2CheckIntegrity() )
		{
			ArrayList<CheckIntegrityModeBase> mNeed2CheckIntegrityModeList = new ArrayList<CheckIntegrityModeBase>();
			mNeed2CheckIntegrityModeList.add( new CheckIntegrityModeLauncher() );//xiatian add	//CheckIntegrity（添加：检查“桌面模块”完整性）
			mNeed2CheckIntegrityModeList.add( new CheckIntegrityModeDownload() );//xiatian add	//CheckIntegrity（添加：检查“下载模块”完整性）
			mNeed2CheckIntegrityModeList.add( new CheckIntegrityModeStatistics() );//xiatian add	//CheckIntegrity（添加：检查“统计模块”完整性）
			mNeed2CheckIntegrityModeList.add( new CheckIntegrityModeFavorites() );//zhujieping add //CheckIntegrity（添加：检查“酷生活”完整性）
			mNeed2CheckIntegrityModeList.add( new CheckIntegrityModeKpsh() );//lvjiangbin add //CheckIntegrity（添加：检查“Kpsh”完整性）
			mNeed2CheckIntegrityModeList.add( new CheckIntegrityModeSearch() );//lvjiangbin add //CheckIntegrity（添加：检查搜索完整性）
			mNeed2CheckIntegrityModeList.add( new CheckIntegrityModeMicroEntry() );//xiatian add	//CheckIntegrity（添加：检查“微入口”完整性）
			mCheckIntegrityManager.addNeed2CheckIntegrityModeList( mNeed2CheckIntegrityModeList );
			mCheckIntegrityManager.checkIntegrity();
		}
	}
	//xiatian add end
	;
	
	// zhangjin@2016/04/26 ADD START
	public String getVersionCode()
	{
		PackageInfo info;
		try
		{
			info = getPackageManager().getPackageInfo( getPackageName() , 0 );
			return String.valueOf( info.versionCode );
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return "0";
	}
	
	public String getVersionName()
	{
		PackageInfo info;
		try
		{
			info = getPackageManager().getPackageInfo( getPackageName() , 0 );
			return info.versionName;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 是否需要检查错误信息，更新后的系统应用则需要，否则不需要
	 * @param context
	 * @return
	 */
	private boolean isNeedCheckError(
			Context context )
	{
		try
		{
			PackageManager pm = context.getPackageManager();
			String packageName = context.getPackageName();
			ApplicationInfo appInfo = pm.getApplicationInfo( packageName , PackageManager.MATCH_DEFAULT_ONLY );
			if( appInfo != null && ( appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
			{
				return true;
			}
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 检查错误信息并上传
	 * @return
	 */
	private boolean checkErrorAndUpload()
	{
		if( isSaveErrorAndUpload() )
		{
			String content = getError();
			return saveErrorAndUpload( content );
		}
		return false;
	}
	
	/**
	 * 获得错误信息(json格式转换string)
	 * @return
	 */
	private String getError()
	{
		String errorStr = getErrorLog();
		if( TextUtils.isEmpty( errorStr ) )
		{
			return null;
		}
		//错误信息（json格式）协议
		String errorContent = null;
		int versionCode = 0;
		String versionName = null;
		String sn = null;
		String appID = null;
		JSONArray destArray = new JSONArray();
		UiupdateHelper updateHelper = UiupdateHelper.getInstance( getApplicationContext() );
		String content = updateHelper.readErrorFile();//已经保存的错误信息。新的错误追加但是协议格式保存。
		try
		{
			if( TextUtils.isEmpty( content ) )
			{
				versionCode = Integer.valueOf( getVersionCode() );
				versionName = getVersionName();
				sn = CoolMethod.getSn( getApplicationContext() );
				appID = CoolMethod.getAppID( getApplicationContext() );
			}
			else
			{
				JSONObject json = new JSONObject( content );
				JSONArray errorArray = json.getJSONArray( "error" );
				for( int i = 0 ; i < errorArray.length() ; i++ )
				{
					destArray.put( errorArray.get( i ) );
				}
				versionCode = json.getInt( "versionCode" );
				versionName = json.getString( "versionName" );
				sn = json.getString( "sn" );
				appID = json.getString( "appID" );
			}
			destArray.put( errorStr );
			//json格式
			JSONObject destJson = new JSONObject();
			destJson.put( "versionCode" , versionCode );
			destJson.put( "versionName" , versionName );
			destJson.put( "sn" , sn );
			destJson.put( "appID" , appID );
			destJson.put( "error" , destArray );
			errorContent = destJson.toString();
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return errorContent;
	}
	
	/**
	 * 保存错误信息并上传
	 * @return
	 */
	private boolean saveErrorAndUpload(
			String content )
	{
		if( TextUtils.isEmpty( content ) )
		{
			return false;
		}
		UiupdateHelper updateHelper = UiupdateHelper.getInstance( getApplicationContext() );
		updateHelper.writeErrorFile( content );
		if( isUploadError() )
		{
			updateHelper.doErrorLogRequest();
			recordUploadErrorTime();
		}
		return true;
	}
	
	/**
	 * 是否上传错误信息
	 * @return
	 */
	private boolean isUploadError()
	{
		long time = System.currentTimeMillis() / 1000;//得到系统时间的（单位是秒）
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		long lastTime = prefs.getLong( "upload_error_time" , 0 );
		//大于24小时
		if( time - lastTime > 24 * 3600 )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 记录上次上传错误新的时间点
	 */
	private void recordUploadErrorTime()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		long time = System.currentTimeMillis() / 1000;//得到系统时间的（单位是秒）
		prefs.edit().putLong( "upload_error_time" , time ).commit();
	}
	
	/**
	 * 是否要保存错误信息并上传
	 * @return
	 */
	private boolean isSaveErrorAndUpload()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		int count = prefs.getInt( "error_happen_continuous_count" , 0 );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "isSaveErrorAndUpload error happen continuous count=" , count ) );
		//连续2次崩溃
		if( count > 1 )
		{
			prefs.edit().remove( "error_happen_continuous_count" ).commit();//已经保存，清除记录
			prefs.edit().remove( "error_happen_time" ).commit();//已经保存，清除记录
			return true;
		}
		return false;
	}
	
	/**
	 * 读取是否有未处理的报错信息，每次读取后都会将error.log清空。
	 * 
	 * @return 返回未处理的报错信息或null
	 */
	private String getErrorLog()
	{
		File fileErrorLog = new File( LauncherApplication.PATH_ERROR_LOG );
		String content = null;
		FileInputStream fis = null;
		try
		{
			if( fileErrorLog.exists() )
			{
				byte[] data = new byte[(int)fileErrorLog.length()];
				fis = new FileInputStream( fileErrorLog );
				fis.read( data );
				content = new String( data );
				data = null;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if( fis != null )
				{
					fis.close();
				}
				if( fileErrorLog.exists() )
				{
					fileErrorLog.delete();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		return content;
	}
	
	// zhangjin@2016/04/26 ADD END
	@Override
	public void notifyFavoritesSwitch(
			final boolean isShow )
	{
		//cheyingkun add start	//解决“服务器运营关闭酷生活、运营搜狐新闻，在触发酷生活页面消失前，桌面切页图标消失”的问题
		//【问题原因】服务器关闭酷生活后，锁屏时才触发酷生活消失。在拿到服务器数据后，锁屏之前，由于开关改变时及时的，页面改变有延迟导致
		//(搜狐新闻返回键无法返回首页、卸载应用常用应用不更新等问题)
		//【解决方案】调用LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES的地方，改为调用mWorkspace.hasFavoritesPage()判断页面是否存在来解决
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "" , StringUtils.concat( "cyk notifyFavoritesSwitch isShow: " , isShow , " LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES: " , LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES ) );
		//cheyingkun add start	//服务器关闭酷生活后，释放资源。
		if( isShow == LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES )
		{
			return;
		}
		//cheyingkun add end
		Runnable mRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
				Editor mEditor = mSharedPreferences.edit();
				mEditor.putBoolean( OperateFavorites.OPERATE_FAVORITES_SWITCH_KEY , isShow );
				mEditor.commit();
				//cheyingkun add start	//服务器关闭酷生活后，释放资源。
				LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES = isShow;
				//cheyingkun add start	//添加运营酷生活umeng统计和内部统计
				//友盟统计
				String value = isShow ? UmengStatistics.NOTIFY_FAVORITES_SWITCH_OPEN : UmengStatistics.NOTIFY_FAVORITES_SWITCH_CLOSE;
				if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
				{
					HashMap<String , String> map = new HashMap<String , String>();
					map.put( UmengStatistics.NOTIFY_FAVORITES_SWITCH , value );
					MobclickAgent.onEvent( Launcher.this , UmengStatistics.NOTIFY_FAVORITES_SWITCH , map );
				}
				//内部统计
				JSONObject obj = new JSONObject();
				try
				{
					obj.put( "param1" , UmengStatistics.NOTIFY_FAVORITES_SWITCH + ":" + value );
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
				StatisticsExpandNew.onCustomEvent(
						Launcher.this ,
						UmengStatistics.NOTIFY_FAVORITES_SWITCH ,
						LauncherConfigUtils.getSN( Launcher.this ) ,
						LauncherConfigUtils.getAppID( Launcher.this ) ,
						LauncherConfigUtils.cooeeGetCooeeId( Launcher.this ) ,
						4 ,
						getPackageName() ,
						String.valueOf( LauncherConfigUtils.getLauncherVersion( Launcher.this ) ) ,
						obj );
				//cheyingkun add end
				if( isShow )
				{//从无到有
					//初始化酷生活配置
					initFavoritesConfig();//cheyingkun add	//解决“本地配置关闭酷生活,服务器运营后无法打开”的问题。
					//初始化
					initFavoritesPlugin();
					//lvjiangbin delete  begin 在onFavoritesReady处理
					//					//setAllAPps
					//					if( FavoritesPageManager.getInstance( mLauncher ).isInitFinish() )
					//					{
					//						setFavoritesAllApps();
					//					}
					//lvjiangbin delete  end 在onFavoritesReady处理
				}
				favoritesViewRemove = !isShow;
				//workspace
				//lvjiangbin delete  begin 在onFavoritesReady处理
				//				invalidateFavoritesPage();
				//lvjiangbin delete  end 在onFavoritesReady处理
				//cheyingkun add end
			}
		};
		runOnUiThread( mRunnable );
	}
	
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	public void initFavoritesPlugin()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES )
		{
			FavoritesPageManager.getInstance( mLauncher ).initPluginAfterInitFavoritesConfig();
			FavoritesPageManager.getInstance( mLauncher ).setFavoriteClingsCallBack( mWorkspace );//cheyingkun add	//酷生活引导页
		}
	}
	
	private void setFavoritesAllApps()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES )
		{
			//cheyingkun add start	//phenix1.1稳定版移植酷生活
			HashMap<ComponentName , Bitmap> map = mModel.getAllIcons();
			FavoritesPageManager.getInstance( this ).setAllApp( map );
			//cheyingkun add end
		}
	}
	//cheyingkun add end
	;
	
	// zhangjin@2016/05/10 ADD START
	@Thunk
	void startAppShortcutOrInfoActivity(
			View v )
	{
		Object tag = v.getTag();
		final ShortcutInfo shortcut;
		final Intent intent;
		if( tag instanceof ShortcutInfo )
		{
			shortcut = (ShortcutInfo)tag;
			intent = shortcut.getIntent();
			int[] pos = new int[2];
			v.getLocationOnScreen( pos );
			intent.setSourceBounds( new Rect( pos[0] , pos[1] , pos[0] + v.getWidth() , pos[1] + v.getHeight() ) );
		}
		else if( tag instanceof AppInfo )
		{
			shortcut = null;
			intent = ( (AppInfo)tag ).getIntent();
		}
		else
		{
			throw new IllegalArgumentException( "Input must be a Shortcut or AppInfo" );
		}
		boolean success = startActivitySafely( v , intent , tag );
		if( success && v instanceof BubbleTextView )
		{
			mWaitingForResume = (BubbleTextView)v;
			mWaitingForResume.setStayPressed( true );
		}
	}
	// zhangjin@2016/05/10 ADD END
	;
	
	@Override
	public void onFavoritesReady()
	{
		//cheyingkun add end
		Runnable mRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				FavoritesPageManager.isLoadFavoritesFinish = true;//cheyingkun add	//酷生活编辑失败,初始化完成标志放在回调里修改
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "lvjangbin" , "init onFavoritesReady" );
				SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
				Editor mEditor = mSharedPreferences.edit();
				mEditor.putBoolean( OperateFavorites.OPERATE_FAVORITES_SWITCH_KEY , true );
				mEditor.commit();
				//cheyingkun add start	//服务器关闭酷生活后，释放资源。
				LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES = true;
				favoritesViewRemove = false;
				//workspace
				invalidateFavoritesPage();
				getWorkspace().changeFavoritesPage();
				setFavoritesAllApps();
				//cheyingkun add end
			}
		};
		runOnUiThread( mRunnable );
	}
	
	//cheyingkun add start	//编辑模式底部按键支持按键(逻辑优化)
	private void checkOverviewPanelButtonsFocus()
	{
		if( overviewPanelButtons != null && !overviewPanelButtons.hasFocus() )
		{
			boolean hasFocus = false;
			int childCount = overviewPanelButtons.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				View childAt = overviewPanelButtons.getChildAt( i );
				hasFocus = childAt.hasFocus();
				if( hasFocus )
				{
					break;
				}
			}
			if( !hasFocus )
			{
				overviewPanelButtons.requestFocus();
			}
		}
	}
	
	//cheyingkun add end
	//zhujieping add，重启launcher时，先将launcher给finish掉
	public static void finishSelf()
	{
		if( mLauncher != null )
		{
			mLauncher.finish();
			mLauncher = null;//置空，否则qmi手机切换单双层模式时，桌面重启
		}
	}
	
	//cheyingkun add start	//移植UNI3签名校验功能
	/**
	 * 校验签名MD5码
	 * @return
	 */
	private boolean checkSignerMD5()
	{
		String signerMD5 = SignerUtil.getSignerMD5( getApplicationContext() );
		if( TextUtils.isEmpty( signerMD5 ) )
		{
			return false;
		}
		if( signerMD5.equals( SIGNER_NORMAL_MD5 ) )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 签名错误，显示警告对话框
	 */
	private void showErrorDialog()
	{
		Builder builder = new Builder( this );
		builder.setCancelable( false );
		builder.setMessage( "桌面签名错误，请使用正确的签名。" );
		builder.setTitle( "警告" );
		builder.setPositiveButton( "确定" , new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(
					DialogInterface dialog ,
					int which )
			{
				dialog.dismiss();
				android.os.Process.killProcess( android.os.Process.myPid() );
			}
		} );
		builder.create().show();
	}
	
	/**
	 * 是否需要校验签名MD5，系统应用则需要，否则不需要
	 * @param context
	 * @return
	 */
	private boolean isNeedCheckSignerMD5(
			Context context )
	{
		try
		{
			PackageManager pm = context.getPackageManager();
			String packageName = context.getPackageName();
			ApplicationInfo appInfo = pm.getApplicationInfo( packageName , PackageManager.MATCH_DEFAULT_ONLY );
			if( appInfo != null && ( appInfo.flags & ApplicationInfo.FLAG_SYSTEM ) > 0 )
			{
				return true;
			}
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return false;
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//搜索栏是否支持显示语音搜索的按钮。true为支持；false为不支持。默认true。
	protected void hideSearchBarVoiceButton()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_SHOW_VOICE_BUTTON )
		{
			return;
		}
		//不显示语音搜索图标
		final View voiceButtonContainer = findViewById( R.id.voice_button_container );
		final View voiceButton = findViewById( R.id.voice_button );
		if( voiceButtonContainer != null )
			voiceButtonContainer.setVisibility( View.GONE );
		if( voiceButton != null )
			voiceButton.setVisibility( View.GONE );
		int coi = getCurrentOrientationIndexForGlobalIcons();
		sVoiceSearchIcon[coi] = null;
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//解决“加载桌面时，先显示第一页，再跳到默认主页”的问题（由“加载item完毕之后，再跳到默认主页”改为“在加载item之前，直接跳到默认主页”）。【c_0004499】
	@Override
	public int getDefaultPageFromSharedPreferences()
	{
		return mWorkspace.getDefaultPageFromSharedPreferences();
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
	private void updateAppMarketIcon(
			boolean show )
	{
		if( show )
		{
			// If we have a saved version of these external icons, we load them up immediately
			int coi = getCurrentOrientationIndexForGlobalIcons();
			if( sAppMarketIcon[coi] == null )
			{
				if( SHOW_MARKET_BUTTON )
				{
					updateAppMarketIcon();
				}
			}
			else
			{
				updateAppMarketIcon( sAppMarketIcon[coi] );
			}
		}
	}
	//cheyingkun add end
	;
	
	//zhujieping add start
	public void showWorkspaceIfNotEidtMode()
	{
		if( getAppsMode() == AppsCustomizePagedView.EDIT_MODE )
		{
			return;
		}
		showWorkspace();
	}
	
	public int getAppsMode()
	{
		if( mAppsCustomizeContent != null )
		{
			return mAppsCustomizeContent.getAppsMode();
		}
		return AppsCustomizePagedView.NORMAL_MODE;
	}
	//zhujieping add end
	;
	
	//xiatian add start	//meun键点击后（menu键的onKeyUp事件当做点击事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）或退出编辑模式（当前是编辑模式）；1为打开“最近任务”界面。默认为0。
	private void onMenuKeyUp()
	{
		if( mState == State.APPS_CUSTOMIZE && mAppsCustomizeTabHost != null )//当前在主菜单状态下
		{
			if( mAppsCustomizeTabHost.onMenuPressed() )
			{
				return;
			}
		}
		if( mIgnoreNextMenuKeyUpEventForMenuKeyLongPress )
		{
			mIgnoreNextMenuKeyUpEventForMenuKeyLongPress = false;
			//			Log.v( "menu key" , "up - IgnoreMenuKeyUpEventForMenuKeyLongPress" );
		}
		//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		else if( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_WORKSPACE_MENU_VERTICAL_LIST )
		{
			showOrHideWorkspaceMenuVerticalList();
		}
		//xiatian add end
		else
		{
			if( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_NONE )
			{//do nothing
			}
			else if( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_ENTER_OR_EXIT_EDIT_MODE )
			{
				enterOrExitEditMode();
			}
			else if( LauncherDefaultConfig.CONFIG_MENU_CLICK_STYLE == LauncherDefaultConfig.MENU_CLICK_STYLE_OPEN_RECENTS_ACTIVITY )
			{
				enterRecents();
			}
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//meun键长按后（menu键的onKeyDown事件中判断mKeyEvent.isLongPress()为true，则当做长按事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）；1为打开“最近任务”界面。默认为-1。
	private boolean onMenuKeyLongPress()
	{
		boolean ret = false;
		if( LauncherDefaultConfig.CONFIG_MENU_LONG_CLICK_STYLE == LauncherDefaultConfig.MENU_LONG_CLICK_STYLE_NONE )
		{//do nothing
			mIgnoreNextMenuKeyUpEventForMenuKeyLongPress = true;
		}
		else if( LauncherDefaultConfig.CONFIG_MENU_LONG_CLICK_STYLE == LauncherDefaultConfig.MENU_LONG_CLICK_STYLE_ENTER_EDIT_MODE )
		{
			ret = true;
			mIgnoreNextMenuKeyUpEventForMenuKeyLongPress = true;//xiatian add	//修改menu键的消息处理逻辑：由“onPrepareOptionsMenu中处理menu键逻辑（onPrepareOptionsMenu每次都返回false，确保menu键每次onKeyDown后都调用onPrepareOptionsMenu方法）”改为“onKeyDown和onKeyUp中处理menu键逻辑”。
			enterEditMode();
		}
		else if( LauncherDefaultConfig.CONFIG_MENU_LONG_CLICK_STYLE == LauncherDefaultConfig.MENU_LONG_CLICK_STYLE_OPEN_RECENTS_ACTIVITY )
		{
			ret = true;
			mIgnoreNextMenuKeyUpEventForMenuKeyLongPress = true;//xiatian add	//修改menu键的消息处理逻辑：由“onPrepareOptionsMenu中处理menu键逻辑（onPrepareOptionsMenu每次都返回false，确保menu键每次onKeyDown后都调用onPrepareOptionsMenu方法）”改为“onKeyDown和onKeyUp中处理menu键逻辑”。
			enterRecents();
		}
		return ret;
	}
	//xiatian add end
	;
	
	public void enterRecents()
	{
		String mRecentsAction = null;
		if( VERSION.SDK_INT < 14/* 4.0 */)
		{//小于4.0（不包括4.0）
			mRecentsAction = "com.android.systemui.recent.action.TOGGLE_RECENTS";
		}
		else if( VERSION.SDK_INT >= 23/* 6.0 */)
		{//大于等于6.0（包括6.0）
			mRecentsAction = "com.android.systemui.recents.TOGGLE_RECENTS";
		}
		else
		{//大于等于4.0到小于6.0之间（包括4.0，不包括6.0）
			mRecentsAction = "com.android.systemui.recents.SHOW_RECENTS";
		}
		Intent intent = new Intent( mRecentsAction );
		int mFlags = 0;
		if( VERSION.SDK_INT < 14/* 4.0 */)
		{//小于4.0（不包括4.0）
			mFlags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
		}
		else
		{//大于等于4.0以上（包括4.0）
			mFlags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_TASK_ON_HOME;
		}
		intent.setFlags( mFlags );
		String mClassName = null;
		if( VERSION.SDK_INT < 21/* 5.0.1 */)
		{//小于5.1（不包括5.1）
			mClassName = "com.android.systemui.recent.RecentsActivity";
		}
		else
		{//大于等于5.1以上（包括5.1）
			mClassName = "com.android.systemui.recents.RecentsActivity";
		}
		ComponentName recents = new ComponentName( "com.android.systemui" , mClassName );
		intent.setComponent( recents );
		startActivity( intent );
	}
	
	public void enterBeautyCenterTabTheme(
			View v )
	{//编辑模式底边栏按钮调用该方法
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		boolean mIsFromWorkspaceMenuListItem = isWorkspaceMenuListItem( v );//xiatian add	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		//cheyingkun add start	//添加友盟统计自定义事件(编辑模式美化中心)
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			if( mIsFromOverviewPanelButton )
			{
				MobclickAgent.onEvent( this , UmengStatistics.ENTER_BEAUTY_CENTER_BY_EDIT_MODE );
			}
			//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
			else if( mIsFromWorkspaceMenuListItem )
			{
			}
			//xiatian add end
		}
		//cheyingkun add end
		if( LauncherDefaultConfig.CONFIG_EDIT_MODE_BUTTON_ENTER_THEME_STYLE == LauncherDefaultConfig.EDIT_MODE_BUTTON_ENTER_THEME_SECONDARY_INTEFACE )
		{
			enterSecondaryEditMode( EditModeEntity.TAB_THEME_KEY , true );
		}
		else
		{
			enterOrDownloadBeautyCenter( ThemeManager.BEAUTY_CENTER_TAB_THEME_CLASS_NAME );
		}
	}
	
	public void enterBeautyCenterTabWallpaper(
			View v )
	{//编辑模式底边栏按钮调用该方法
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		boolean mIsFromWorkspaceMenuListItem = isWorkspaceMenuListItem( v );//xiatian add	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		//xiatian start	//编辑模式底边栏按钮配置为壁纸时，打开不同的壁纸选择界面。0为美化中心的壁纸tab；1为launcher3的壁纸选择界面；2为uni3的壁纸选择界面；3为客户自定义的壁纸选择界面。默认为0。
		//xiatian del start
		//		//cheyingkun add start	//是否启动原生壁纸设置界面
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_LAUNCHER3_WALLPAPER_PICKER )
		//		{
		//			Intent mIntent = new Intent( Intent.ACTION_SET_WALLPAPER );
		//			ComponentName wallpaperPickerComponent = getWallpaperPickerComponent();
		//			mIntent.setComponent( wallpaperPickerComponent );
		//			startActivityForResult( mIntent , REQUEST_PICK_WALLPAPER );
		//			return;
		//		}
		//		//cheyingkun add end	//是否启动原生壁纸设置界面
		//		//xiatian add start	//飞利浦需求，将美化中心改为他们的壁纸设置。
		//		if( LauncherDefaultConfig.CONFIG_CUSTOMER_WALLPAPER_COMPONENT_NAME != null )
		//		{
		//			Intent mIntent = new Intent( Intent.ACTION_SET_WALLPAPER );
		//			mIntent.setComponent( LauncherDefaultConfig.CONFIG_CUSTOMER_WALLPAPER_COMPONENT_NAME );
		//			startActivityForResult( mIntent , REQUEST_PICK_WALLPAPER );
		//			return;
		//		}
		//		//xiatian add end
		//		//cheyingkun add start	//添加友盟统计自定义事件(编辑模式美化中心)
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		//		{
		//			MobclickAgent.onEvent( this , UmengStatistics.ENTER_BEAUTY_CENTER_BY_EDIT_MODE );
		//		}
		//		//cheyingkun add end
		//		enterOrDownloadBeautyCenter( ThemeManager.BEAUTY_CENTER_TAB_WALLPAPER_CLASS_NAME );
		//xiatian del end
		//xiatian add start
		if( LauncherDefaultConfig.CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE == LauncherDefaultConfig.EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_BEAUTY_CENTER )
		{
			//cheyingkun add start	//添加友盟统计自定义事件(编辑模式美化中心)
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				if( mIsFromOverviewPanelButton )
				{
					MobclickAgent.onEvent( this , UmengStatistics.ENTER_BEAUTY_CENTER_BY_EDIT_MODE );
				}
				//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
				else if( mIsFromWorkspaceMenuListItem )
				{
				}
				//xiatian add end
			}
			//cheyingkun add end
			enterOrDownloadBeautyCenter( ThemeManager.BEAUTY_CENTER_TAB_WALLPAPER_CLASS_NAME );
		}
		//xiatian add start	//需求：拓展配置项“config_edit_mode_button_enter_wallpaper_style”，添加可配置项4。4为打开“系统的选择壁纸应用的界面”。
		else if( LauncherDefaultConfig.CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE == LauncherDefaultConfig.EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_PICK_ACTIVITY )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				if( mIsFromWorkspaceMenuListItem )
				{
				}
			}
			showSystemPickWallpaperActivity();
		}
		//xiatian add end
		else if( LauncherDefaultConfig.CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE == LauncherDefaultConfig.EDIT_MODE_BUTTON_ENTER_WALLPAPER_SECONDARY_INTEFACE )
		{
			enterSecondaryEditMode( EditModeEntity.TAB_WALLPAPER_KEY , true );
		}
		else
		{
			ComponentName mWallpaperPickerComponent = null;
			if( LauncherDefaultConfig.CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE == LauncherDefaultConfig.EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_LAUNCHER3 )
			{
				mWallpaperPickerComponent = new ComponentName( getPackageName() , WallpaperPickerActivity.class.getName() );
			}
			else if( LauncherDefaultConfig.CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE == LauncherDefaultConfig.EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_UNI3 )
			{
				mWallpaperPickerComponent = new ComponentName( getPackageName() , WallpaperChooser.class.getName() );
			}
			else if( LauncherDefaultConfig.CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE == LauncherDefaultConfig.EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_CUSTOMER )
			{
				mWallpaperPickerComponent = LauncherDefaultConfig.CONFIG_CUSTOMER_WALLPAPER_COMPONENT_NAME;
			}
			Intent mIntent = new Intent( Intent.ACTION_SET_WALLPAPER );
			mIntent.setComponent( mWallpaperPickerComponent );
			startActivityForResult( mIntent , REQUEST_PICK_WALLPAPER );
		}
		//xiatian add end
		//xiatian end
	}
	
	public void enterBeautyCenterTabLock(
			View v )
	{//编辑模式底边栏按钮调用该方法
		boolean mIsFromOverviewPanelButton = isOverviewPanelButton( v );
		//cheyingkun add start	//解决“双层模式下，进入主菜单快速返回桌面，点击屏幕下方，应响应编辑模式下的四个菜单”的问题【i_0014420】
		if(
		//
		( mIsFromOverviewPanelButton )
		//
		&& ( canClickOverviewPanelButton() == false )
		//
		)
		{
			return;
		}
		//cheyingkun add end
		//cheyingkun add start	//添加友盟统计自定义事件(编辑模式美化中心)
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			if( mIsFromOverviewPanelButton )
			{
				MobclickAgent.onEvent( this , UmengStatistics.ENTER_BEAUTY_CENTER_BY_EDIT_MODE );
			}
		}
		//cheyingkun add end
		enterOrDownloadBeautyCenter( ThemeManager.BEAUTY_CENTER_TAB_LOCKER_CLASS_NAME );
	}
	
	public void enterOrDownloadBeautyCenter(
			String mTabClassName )
	{
		if( LauncherAppState.isApkInstalled( ThemeManager.BEAUTY_CENTER_PACKAGE_NAME ) == false )
		{
			//<i_0010336> liuhailin@2015-03-10 modify begin
			//DownloadManager.downloadApkDialog( Launcher.this , R.mipmap.ic_launcher_home , "美化中心" , ThemeManager.BEAUTY_CENTER_PACKAGE_NAME , true );
			DownloadManager.downloadApkCooeeDialog(
					Launcher.this ,
					LauncherDefaultConfig.getString( R.string.overview_panel_button_beauty_center_string ) ,
					ThemeManager.BEAUTY_CENTER_PACKAGE_NAME ,
					true );
			//<i_0010336> liuhailin@2015-03-10 modify end
		}
		else
		{
			Intent mIntent = new Intent();
			ComponentName mComponentName = new ComponentName( ThemeManager.BEAUTY_CENTER_PACKAGE_NAME , mTabClassName );
			mIntent.setComponent( mComponentName );
			bindThemeActivityData( mIntent );
			startActivity( mIntent );
		}
	}
	
	//xiatian add start	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
	@Override
	public void notifyExplorerSwitch(
			boolean enable ,
			String url )
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( this );
		if(
		//
		enable
		//
		&& TextUtils.isEmpty( url ) == false
		//
		&& ( url.equals( "about:blank" ) || isValidUrl( url ) )
		//
		)
		{
			if( url.equals( "about:blank" ) == false )
			{
				if( url.startsWith( "https" ) )
				{
					url.replaceFirst( "https" , "http" );
				}
				else if( url.startsWith( "Http" ) )
				{
					url.replaceFirst( "Http" , "http" );
				}
				else if( url.startsWith( "Https" ) )
				{
					url.replaceFirst( "Https" , "http" );
				}
				else if( url.startsWith( "rtsp" ) )
				{
					url.replaceFirst( "rtsp" , "http" );
				}
				else if( url.startsWith( "Rtsp" ) )
				{
					url.replaceFirst( "Rtsp" , "http" );
				}
				if( url.startsWith( "http" ) == false )
				{
					url = StringUtils.concat( "http://" , url );
				}
			}
			LauncherDefaultConfig.SWITCH_ENABLE_OPERATE_EXPLORER = true;
			LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE = url;
			//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				MobclickAgent.onEvent( this , UmengStatistics.OPERATE_ON_TIMES );
			}
			StatisticsExpandNew.onCustomEvent(
					this ,
					UmengStatistics.OPERATE_ON_TIMES ,
					LauncherConfigUtils.getSN( this ) ,
					LauncherConfigUtils.getAppID( this ) ,
					LauncherConfigUtils.cooeeGetCooeeId( this ) ,
					4 ,
					getPackageName() ,
					String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) ,
					null );
			//xiatian add end
		}
		else
		{
			LauncherDefaultConfig.SWITCH_ENABLE_OPERATE_EXPLORER = false;
			LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE = "";
			//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				MobclickAgent.onEvent( this , UmengStatistics.OPERATE_OFF_TIMES );
			}
			StatisticsExpandNew.onCustomEvent(
					this ,
					UmengStatistics.OPERATE_OFF_TIMES ,
					LauncherConfigUtils.getSN( this ) ,
					LauncherConfigUtils.getAppID( this ) ,
					LauncherConfigUtils.cooeeGetCooeeId( this ) ,
					4 ,
					getPackageName() ,
					String.valueOf( LauncherConfigUtils.getLauncherVersion( this ) ) ,
					null );
			//xiatian add end
		}
		sp.edit().putBoolean( OperateExplorer.OPERATE_EXPLORER_ENABLE_KEY , LauncherDefaultConfig.SWITCH_ENABLE_OPERATE_EXPLORER ).commit();
		sp.edit().putString( OperateExplorer.OPERATE_EXPLORER_HOME_WEBSITE_KEY , LauncherDefaultConfig.CONFIG_OPERATE_EXPLORER_HOME_WEBSITE ).commit();
	}
	
	//xiatian add start	//“运营浏览器主页”的功能：添加通过Category来判断点击的图标是否为浏览器的方法。
	private boolean isBrowserByCategory(
			Set<String> mCategories )
	{
		if( mCategories != null && mCategories.contains( Intent.CATEGORY_BROWSABLE ) )
		{
			return true;
		}
		return false;
	}
	//xiatian add end
	;
	
	private boolean isBrowserByPackageName(
			String packageName )
	{
		if( TextUtils.isEmpty( packageName ) )
		{
			return false;
		}
		String mFilterBrowserIntentUri = "intent:http://www.baidu.com#Intent;action=android.intent.action.VIEW;end";
		Intent mFilterBrowserIntent = null;
		try
		{
			mFilterBrowserIntent = Intent.parseUri( mFilterBrowserIntentUri , 0 );
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "isBrowser - parseUri error" );
			return false;
		}
		if( mFilterBrowserIntent != null )
		{
			PackageManager mPackageManager = getPackageManager();
			List<ResolveInfo> mBrowserResolveInfoList = mPackageManager.queryIntentActivities( mFilterBrowserIntent , 0 );//查询所有能接受“指定intent（打开网页）”的apk
			if( mBrowserResolveInfoList != null && mBrowserResolveInfoList.size() > 0 )
			{
				for( ResolveInfo mBrowserResolveInfo : mBrowserResolveInfoList )
				{
					String mDestPackageName = null;
					if( mBrowserResolveInfo.activityInfo != null && mBrowserResolveInfo.activityInfo.applicationInfo != null )
					{
						mDestPackageName = mBrowserResolveInfo.activityInfo.applicationInfo.packageName;
					}
					if( TextUtils.isEmpty( mDestPackageName ) == false && packageName.equals( mDestPackageName ) )
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean isValidUrl(
			String mUrl )
	{
		boolean ret = false;
		if( Patterns.WEB_URL.matcher( mUrl ).matches() )
		{
			//符合网页的标准格式  
			ret = true;
		}
		else
		{
			//不符合网页的标准格式
			ret = false;
		}
		return ret;
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//解决“打开酷生活引导页，切页到酷生活后，点击引导页按钮，搜索栏显示异常”的问题
	private void initFavoritesConfig()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES )
		{
			DeviceProfile grid = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
			HashMap<String , Object> launcherConfigMap = new HashMap<String , Object>();
			launcherConfigMap.put( FavoriteConfigString.LAUNCHER_ICON_SIZEPX , grid.getIconHeightSizePx() );
			launcherConfigMap.put( FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT , grid.getSearchBarSpaceHeightPx() );
			launcherConfigMap.put( FavoriteConfigString.ENABLE_UMENG , LauncherDefaultConfig.SWITCH_ENABLE_UMENG );
			launcherConfigMap.put( FavoriteConfigString.ENABLE_COOEE_SEARCH , LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_COOEE );
			launcherConfigMap.put( FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH , LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE );
			launcherConfigMap.put( FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH , LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE );
			FavoritesPageManager.getInstance( mLauncher ).initFavoritesConfig( launcherConfigMap , false );
		}
	}
	
	public boolean isShowFavoritesClings()
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mLauncher );
		boolean canShowClings = mSharedPreferences.getBoolean( "showFavoriteClingsView" , true );
		FavoritesConfig config = FavoritesPageManager.getInstance( this ).getConfig();
		boolean switch_enable_favorites_clings = config.getBoolean( FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS , FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS_DEFAULTVALUE );
		return switch_enable_favorites_clings && canShowClings;
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//添加酷生活时回调其onPause和onResume；解决编辑模式下进美化中心没有移除酷生活的导航栏监听窗口的问题
	public void launcherOnPausedAndOnResumeCallBackFavorites()
	{
		if( mWorkspace.hasFavoritesPage() )
		{
			if( mPaused )
			{
				FavoritesPageManager.getInstance( this ).onPause();
			}
			else
			{
				FavoritesPageManager.getInstance( this ).onResume();
			}
		}
	}
	//cheyingkun add end
	;
	
	private boolean isOverviewPanelButton(
			View mCurView )
	{
		boolean mIsOverviewPanelButton = false;
		View mCurViewParent = (View)mCurView.getParent();
		int mCurViewParentId = mCurViewParent.getId();
		if( mCurViewParentId == R.id.overview_panel_button )
		{
			mIsOverviewPanelButton = true;
		}
		return mIsOverviewPanelButton;
	}
	
	//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
	private void showOrHideWorkspaceMenuVerticalList()
	{
		if( mWorkspaceMenuVerticalList != null )
		{
			int mFinalVisibility = View.VISIBLE;
			int mCurVisibility = mWorkspaceMenuVerticalList.getVisibility();
			if( mCurVisibility == View.VISIBLE )
			{
				mFinalVisibility = View.INVISIBLE;
			}
			if( mFinalVisibility == View.VISIBLE )
			{
				if( canShowWorkspaceMenuVerticalList() )
				{
					//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“在插件调整大小模式下，点击menu键显示桌面菜单，没有退出插件调整大小模式（插件调整大小编辑框没有消失）”的问题。【i_0014860】）
					if( mWorkspace != null )
					{//显示桌面菜单时，退出插件调整大小模式
						mWorkspace.exitWidgetResizeMode();
					}
					//xiatian add end
					mWorkspaceMenuVerticalList.showWithAnim();
				}
			}
			else if( mFinalVisibility == View.INVISIBLE )
			{
				mWorkspaceMenuVerticalList.hideWithAnim();
			}
		}
	}
	
	private boolean canShowWorkspaceMenuVerticalList()
	{
		return(
		//
		mState == State.WORKSPACE
		//
		&& ( mWorkspace != null )
		//
		&& ( mWorkspace.isInNormalMode() )
		//
		&& ( mWorkspace.isFunctionPageByPageIndex( mWorkspace.getCurrentPage() ) == false )
		//
		&& ( mWorkspace.isPageMoving() == false )
		//
		&& ( mWorkspace.getOpenFolder() == null )
		//
		&& ( isFirstRunCling() == false )
		//
		&& ( isFirstRunWorkspaceCling() == false )
		//
		&& ( isWorkspaceLocked() == false )
		//
		&& ( OperateHelp.getInstance( this ).isGettingOperateDate() == false/*//智能分类*/)
		//
		&& ( mModel.isLoadingWorkspace() == false/*//workspace 正在加载*/)
		//
		&& ( mStateAnimation == null || ( mStateAnimation != null && mStateAnimation.isRunning() == false ) /*//动画正在执行*/)
		//
		);
	}
	
	private boolean isWorkspaceMenuListItem(
			View mCurView )
	{
		boolean mIsFromWorkspaceMenuListItem = false;
		int mViewId = mCurView.getId();
		if( mViewId == R.id.workspace_menu_vertical_list_item_id )
		{
			mIsFromWorkspaceMenuListItem = true;
		}
		return mIsFromWorkspaceMenuListItem;
	}
	
	public void enterEditModeFromWorkspaceMenu()
	{
		enterEditMode();
	}
	
	private void showSystemPickWidgetActivity()
	{
		int mAppWidgetId = mAppWidgetHost.allocateAppWidgetId();
		Intent mPickWidgetIntent = new Intent( AppWidgetManager.ACTION_APPWIDGET_PICK );
		mPickWidgetIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , mAppWidgetId );
		String mPickWidgetActivityTitle = LauncherDefaultConfig.getString( R.string.pick_widget_activity_title );
		if( TextUtils.isEmpty( mPickWidgetActivityTitle ) == false )
		{
			mPickWidgetIntent.putExtra( Intent.EXTRA_TITLE , mPickWidgetActivityTitle );
		}
		startActivityForResult( mPickWidgetIntent , REQUEST_PICK_APPWIDGET_FOR_SYSTEM_PICK_ACTIVITY );
	}
	
	private void addAppWidgetFromPickForSystemPickActivity(
			Intent data )
	{
		if( mWorkspace == null )
		{
			return;
		}
		int appWidgetId = data.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , -1 );
		AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo( appWidgetId );
		if( appWidget.configure != null )
		{//TwoStage
			Intent intent = new Intent( AppWidgetManager.ACTION_APPWIDGET_CONFIGURE );
			intent.setComponent( appWidget.configure );
			intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , appWidgetId );
			Utilities.startActivityForResultSafely( this , intent , REQUEST_CREATE_APPWIDGET_FOR_SYSTEM_PICK_ACTIVITY );
		}
		else
		{//add widget
			//xiatian start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“生成一个新页面，再删除新页面，再生成一个新页面，然后在新页面点击menu，进入桌面菜单的小组件添加任一插件，此时桌面重启”的问题。【i_0014866】）
			//			long mScreenId = mWorkspace.getCurrentScreen();//xiatian del
			long mScreenId = mWorkspace.getScreenIdForPageIndex( mWorkspace.getCurrentScreen() );//xiatian add
			//xiatian end
			completeAddAppWidget( appWidgetId , LauncherSettings.Favorites.CONTAINER_DESKTOP , mScreenId , null , null );
		}
	}
	
	private void addAppWidgetFromPickTwoStageForSystemPickActivity(
			Intent data )
	{
		if( mWorkspace == null )
		{
			return;
		}
		int appWidgetId = data.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID , -1 );
		//xiatian start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。（解决“生成一个新页面，再删除新页面，再生成一个新页面，然后在新页面点击menu，进入桌面菜单的小组件添加任一插件，此时桌面重启”的问题。【i_0014866】）
		//		long mScreenId = mWorkspace.getCurrentScreen();//xiatian del
		long mScreenId = mWorkspace.getScreenIdForPageIndex( mWorkspace.getCurrentScreen() );//xiatian add
		//xiatian end
		completeAddAppWidget( appWidgetId , LauncherSettings.Favorites.CONTAINER_DESKTOP , mScreenId , null , null );
	}
	//xiatian add end
	;
	
	//xiatian add start	//需求：拓展配置项“config_edit_mode_button_enter_wallpaper_style”，添加可配置项4。4为打开“系统的选择壁纸应用的界面”。
	private void showSystemPickWallpaperActivity()
	{
		Intent mPickWallpaperIntent = new Intent( Intent.ACTION_SET_WALLPAPER );
		String mPickWallpaperActivityTitle = LauncherDefaultConfig.getString( R.string.pick_wallpaper_activity_title );
		if( TextUtils.isEmpty( mPickWallpaperActivityTitle ) == false )
		{
			mPickWallpaperIntent = Intent.createChooser( mPickWallpaperIntent , mPickWallpaperActivityTitle );
		}
		startActivity( mPickWallpaperIntent );
	}
	//xiatian add end
	;
	
	//zhujieping add start
	private void enterSecondaryEditMode(
			String tab ,
			boolean isAnim )
	{
		if( mEditModeEntity == null )
		{
			mEditModeEntity = (EditModeEntity)LayoutInflater.from( this ).inflate( R.layout.editmode_secondary , null );
			mEditModeEntity.setEditControlCallBack( this );
		}
		View brother = mOverviewPanel.findViewById( R.id.overview_panel_button );
		if( mEditModeEntity.getParent() == null )
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , brother.getHeight() );
			params.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE );
			mOverviewPanel.addView( mEditModeEntity , params );
		}
		mEditModeEntity.showHorizontalListView( tab , brother , isAnim );
	}
	//zhujieping add end
	;
	
	//zhujieping add start,换主题不重启
	private ThemeChangeDialog mThemeChangingProgressView;
	private static boolean mIsThemeChanging;
	
	@Override
	public void onThemeChanged(
			final Object arg0 ,
			final Object arg1 )
	{
		mIsThemeChanging = true;
		//gaominghui add start //添加配置项“switch_enable_exit_overview_mode_when_apply_theme_frome_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。
		if( arg0 instanceof Intent )
		{
			boolean apply_theme_from_beautycenter = ( (Intent)arg0 ).getBooleanExtra( "apply_theme_from_beautycenter" , false );
			if( LauncherDefaultConfig.SWITCH_ENABLE_EXIT_OVERVIEW_MODE_WHEN_APPLY_THEME_FROM_BEAUTYCENTER//
					&& apply_theme_from_beautycenter //
					&& mWorkspace != null //
					&& mWorkspace.isInOverviewMode() )
			{
				mWorkspace.exitOverviewMode( false );
			}
		}
		//gaominghui adde end //添加配置项“switch_enable_exit_overview_mode_when_apply_theme_frome_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。
		if( arg1 instanceof Boolean && (Boolean)arg1 )
		{
			showThemeChangingProgressView();
		}
		closeFolderWithoutAnim();//关闭打开的文件夹（由于文件夹打开，导致workspace没有draw,已经开关文件夹的模糊背景没法更新。）
		final Handler mHandler = getHandler();
		Runnable mRunnableChangeTheme = new Runnable() {
			
			public void run()
			{
				//ThemeManager
				if( arg0 instanceof Intent )
				{
					ThemeManager mThemeManager = ThemeManager.getInstance();
					mThemeManager.onThemeChanged( arg0 , arg1 );
				}
				//mIconCache
				LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().onThemeChanged( getResources() , Launcher.this );//读取图标大小
				mIconCache.onThemeChanged( null , null );
				//1、处于编辑模式，就先处理Workspace，再处理Hotseat
				//2、处于正常模式，就先处理Hotseat，再处理Workspace
				if( mWorkspace != null && mWorkspace.isInOverviewMode() )
				{
					mWorkspace.onThemeChanged( mIconCache , mHandler );
				}
				if( mHotseat != null )
				{
					mHotseat.onThemeChanged( mIconCache , mHandler );
				}
				if( mWorkspace != null && mWorkspace.isInOverviewMode() == false )
				{
					mWorkspace.onThemeChanged( mIconCache , mHandler );
				}
				if( mEditModeEntity != null )
				{
					mEditModeEntity.onThemeChanged( null , null );//在换主题的时候添加编辑模式监听 wanghongjian add
				}
				IOnThemeChanged target = null;
				if(
				//
				(
				//
				( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW )
				//
				|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
				//
				)
				//
				&& ( mAppsView != null )
				//
				)
				{
					target = mAppsView;
				}
				else
				{
					target = mAppsCustomizeTabHost;
				}
				target.onThemeChanged( mIconCache , mHandler );
				//lvjiangbin -1 page add start
				if( mWorkspace.hasFavoritesPage() )
				{
					HashMap<ComponentName , Bitmap> map = mModel.getAllIcons();
					FavoritesPageManager mFavoritesPageManager = FavoritesPageManager.getInstance( Launcher.this );
					mFavoritesPageManager.setIconSize( Utilities.sIconWidth );
					mFavoritesPageManager.reLoadAndBindApps( map );
				}
				//lvjiangbin -1 page add start
				IconHouseManager.getInstance().onThemeChanged( arg0 , arg1 );//zhujieping add  //需求：桌面动态图标支持随主题变化
				mIconCache.onRecycle();
				Runnable mRunnableChangeThemeComplete = new Runnable() {
					
					public void run()
					{
						mIsThemeChanging = false;
						if( arg1 instanceof Boolean && (Boolean)arg1 )
						{
							hideThemeChangingProgressView();
						}
						//gaominghui add start   //添加配置项“switch_enable_customer_lj_notify_apply_theme”，应用主题后，是否通知客户主题已经更换，true为通知，false为不通知，默认为false。【c_0004704】。
						if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOMER_LJ_NOTIFY_APPLY_THEME && arg0 != null && arg0 instanceof Intent )
						{
							notifyLeJinThemeChanged( ( (Intent)arg0 ).getStringExtra( "theme" ) );
						}
						//gaominghui add end
					}
				};
				mHandler.postDelayed( mRunnableChangeThemeComplete , 150 );
			}
		};
		new Thread( mRunnableChangeTheme ).start();
	}
	
	public static boolean isThemeChanging()
	{
		return mIsThemeChanging;
	}
	
	private void showThemeChangingProgressView()
	{
		if( mThemeChangingProgressView == null )
		{
			mThemeChangingProgressView = new ThemeChangeDialog( this , getResources().getString( R.string.message_theme_changing ) );
		}
		mThemeChangingProgressView.setCancelable( false );
		mThemeChangingProgressView.show();
	}
	
	private void hideThemeChangingProgressView()
	{
		if( mThemeChangingProgressView != null )
		{
			mThemeChangingProgressView.dismiss();
			mThemeChangingProgressView = null;
		}
	}
	
	public void applyTheme(
			final Intent intent ,
			final boolean mIsShowLoadingLoadingProgressView )
	{//注意：必须在主线程调用。
		ThemeManager mThemeManager = ThemeManager.getInstance();
		if( mThemeManager != null )
		{
			if( mIsThemeChanging == false && mThemeManager.need2ChangeTheme( intent ) )
			{
				//gaominghui add start //支持通过AIDL切换主题，解决“手机第一次开机，应用主题慢”的问题【c_0004675】
				//必须post到主线程，否则主题切换不成功
				mHandler.post( new Runnable() {
					
					@Override
					public void run()
					{
						onThemeChanged( intent , mIsShowLoadingLoadingProgressView );
					}
				} );
				//gaominghui add end //支持通过AIDL切换主题，解决“手机第一次开机，应用主题慢”的问题【c_0004675】
			}
		}
	}
	
	//zhujieping add end
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public AppsView getAppsView(
			AppsCustomizePagedView.ContentType contentType )
	{
		if( contentType == AppsCustomizePagedView.ContentType.Applications )
		{
			//zhujieping add start //7.0进入主菜单动画改成也支持4.4主菜单样式
			if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_KITKAT )
			{
				return mAppsCustomizeTabHost;
			}
			else
			//zhujieping add end
			{
				return mAppsView;
			}
		}
		return null;
	}
	
	public View getStartViewForAllAppsRevealAnimation()
	{
		return true ? mWorkspace.getPageIndicator() : mHotseat.getAllAppsButton();
	}
	
	/**
	* Updates the workspace and interaction state on state change, and return the animation to this
	* new state.
	*/
	public Animator startWorkspaceStateChangeAnimation(
			Workspace.State toState ,
			boolean animated ,
			HashMap<View , Integer> layerViews )
	{
		Workspace.State fromState = mWorkspace.getState();
		Animator anim = mWorkspace.setStateWithAnimation( toState , animated , layerViews );
		//		updateInteraction( fromState , toState );
		return anim;
	}
	
	public void onLongClickAllAppsButton(
			View v )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "onLongClickAllAppsButton" );
		//showAllApps( true , AppsCustomizePagedView.ContentType.Applications , true );
		if( ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW
		//
				|| LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT )
				//
				|| LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT )//zhujieping add //需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。
		{
			showAppsView( true /* animated */, true /* resetListToTop */, true /* updatePredictedApps */, true /* focusSearchBar */, AppsCustomizePagedView.ContentType.Applications );
		}
		else
		{
			showAllApps( true , AppsCustomizePagedView.ContentType.Applications , true );
		}
	}
	
	public boolean isAllAppsContainerViewVisible()
	{
		return ( mState == State.APPS_CUSTOMIZE && !( mAppsCustomizeContent.isShown() && mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Widgets ) ) || ( mOnResumeState == State.APPS_CUSTOMIZE );
	}
	//zhujieping add end
	;
	
	//xiatian add start	//通知桌面切页（代码框架）。详见“INotifyLauncherSnapPageManager”中的备注。
	@Override
	public void notifyLauncherSnapToLeft()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( TAG , "notifyLauncherSnapToLeft" );
		}
		scrollToLeft();
	}
	
	@Override
	public void notifyLauncherSnapToRight()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( TAG , "notifyLauncherSnapToRight" );
		}
		scrollToRight();
	}
	//xiatian add end
	;
	
	//gaominghui add start 
	private void startThemeService()
	{
		Intent intent = new Intent( "com.iLoong.launcher.theme.IThemeService" );
		intent.setPackage( getPackageName() );
		startService( intent );
	}
	
	private void stopThemeService()
	{
		Intent intent = new Intent( "com.iLoong.launcher.theme.IThemeService" );
		intent.setPackage( getPackageName() );
		stopService( intent );
	}
	//gaominghui add end
	;
	
	public boolean onClickCategoryEntry()
	{
		boolean ret = true;
		Intent mIntent = new Intent();
		ComponentName mComponentName = new ComponentName( getPackageName() , LauncherSettingsActivity.class.getName() );
		mIntent.setComponent( mComponentName );
		mIntent.putExtra( "headerId" , R.id.setting_launcher_classification );
		startActivity( mIntent );
		return ret;
	}
	
	//xiatian add start	//需求：添加“一键换主题”功能（1、虚图标；2、点击后，从已经安装的其他主题中，随机换一个）。
	public boolean onClickOneKeyApplyTheme()
	{
		boolean ret = false;
		ThemeManager mThemeManager = ThemeManager.getInstance();
		if( mThemeManager != null )
		{
			String mThemePackageName = mThemeManager.getThemePackageNameInRandom();
			//xiatian add start	//解决“只有一个默认主题时，点击一键换主题图标，这时桌面重启”的问题。
			if( TextUtils.isEmpty( mThemePackageName ) )
			{
				Toast.makeText( this , R.string.one_key_apply_theme_error_tip , Toast.LENGTH_SHORT ).show();
			}
			else
			//xiatian add end
			{
				Intent intent = new Intent();
				intent.putExtra( "theme_status" , 1 );
				intent.putExtra( "theme" , mThemePackageName );
				//gaominghui add start //添加配置项“switch_enable_exit_overview_mode_when_apply_theme_from_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。
				intent.putExtra( "apply_theme_from_beautycenter" , false );
				//gaominghui add end
				Log.i( TAG , "applyTheme themeConfig = " + mThemePackageName );
				//gaominghui add start //美化中心应用主题，桌面会黑一下【i_0015112】
				ThemeReceiver.applyTheme( null , intent , false );
				//gaominghui add end
			}
			return true;
		}
		return ret;
	}
	
	//xiatian add end
	//gaominghui add start   //添加配置项“switch_enable_customer_lj_notify_apply_theme”，应用主题后，是否通知客户主题已经更换，true为通知，false为不通知，默认为false。【c_0004704】。
	public void notifyLeJinThemeChanged(
			String theme )
	{
		try
		{
			Class<?> ServiceManager = Class.forName( "android.os.ServiceManager" );
			java.lang.reflect.Method m = ServiceManager.getMethod( "getService" , String.class );
			android.os.IBinder binder = (android.os.IBinder)m.invoke( null , "activity" );
			if( binder != null )
			{
				android.os.Parcel data = android.os.Parcel.obtain();
				android.os.Parcel reply = android.os.Parcel.obtain();
				data.writeInterfaceToken( "android.app.IActivityManager" );
				data.writeString( theme );
				binder.transact( 303 , data , reply , 0 );
				reply.readException();
				data.recycle();
				reply.recycle();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	//gaominghui add end
	;
	
	//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	public boolean isOverviewAnimOrStateAnim()
	{
		if( mWorkspace.isOverviewAnimRunning() )
		{
			return true;
		}
		if( mStateAnimation != null && mStateAnimation.isRunning() )
		{
			return true;
		}
		return false;
	}
	//zhujieping add end
	//gaominghui add start //需求：支持后台运营音乐页和相机页
	/**
	 *
	 * @see com.cooee.framework.function.OperateMediaPluginPage.OperateMediaPluginDataManager.IOperateMediaPluginCallBack#notifyCameraPageSwitch(boolean)
	 * @auther gaominghui  2017年6月26日
	 */
	@Override
	public void notifyCameraPageSwitch(
			boolean arg0 )
	{
		if( arg0 == LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW )
		{
			return;
		}
		LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW = arg0;
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean( OperateMediaPluginDataManager.OPERATE_CAMERAPAGE_SWITCH_KEY , arg0 );
		mEditor.commit();
		Runnable mRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW && !mWorkspace.hasCameraPage() )
				{
					mWorkspace.createAndAddCameraPage();
				}
			}
		};
		runOnUiThread( mRunnable );
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.OperateMediaPluginPage.OperateMediaPluginDataManager.IOperateMediaPluginCallBack#notifyMusicPageSwitch(boolean)
	 * @auther gaominghui  2017年6月26日
	 */
	@Override
	public void notifyMusicPageSwitch(
			boolean arg0 )
	{
		if( arg0 == LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW )
		{
			return;
		}
		LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW = arg0;
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean( OperateMediaPluginDataManager.OPERATE_MUSICPAGE_SWITCH_KEY , arg0 );
		mEditor.commit();
		Runnable mRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW && !mWorkspace.hasMusicPage() )
				{
					mWorkspace.createAndAddMusicPage();
				}
			}
		};
		runOnUiThread( mRunnable );
	}
	//gaominghui add end
}
