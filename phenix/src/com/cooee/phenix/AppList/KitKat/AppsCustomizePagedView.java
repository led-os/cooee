package com.cooee.phenix.AppList.KitKat;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridLayout.Spec;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.AppWidgetResizeFrame;
import com.cooee.phenix.CellLayout;
import com.cooee.phenix.DeleteDropTarget;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.DragController;
import com.cooee.phenix.DragLayer;
import com.cooee.phenix.DragSource;
import com.cooee.phenix.DropTarget;
import com.cooee.phenix.FastBitmapDrawable;
import com.cooee.phenix.FocusHelper;
import com.cooee.phenix.ILauncherTransitionable;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAnimUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherModel;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.SearchDropTargetBar.DropTargetListener;
import com.cooee.phenix.ShortcutAndWidgetContainer;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Workspace;
import com.cooee.phenix.PagedView.PagedView;
import com.cooee.phenix.PagedView.PagedViewCellLayout;
import com.cooee.phenix.PagedView.PagedViewGridLayout;
import com.cooee.phenix.PagedView.PagedViewIcon;
import com.cooee.phenix.PagedView.PagedViewWidget;
import com.cooee.phenix.PagedView.PagedViewWithDraggableItems;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.config.defaultConfig.LauncherIconBaseConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.PendingAddItemInfo;
import com.cooee.phenix.data.PendingAddShortcutInfo;
import com.cooee.phenix.data.PendingAddWidgetInfo;
import com.cooee.phenix.iconhouse.IconHouseManager;
import com.cooee.phenix.pageIndicators.PageIndicator;
import com.cooee.util.Tools;
import com.iLoong.launcher.SetupMenu.cut;


/**
 * A simple callback interface which also provides the results of the task.
 */
interface AsyncTaskCallback
{
	
	void run(
			AppsCustomizeAsyncTask task ,
			AsyncTaskPageData data );
}

/**
 * The data needed to perform either of the custom AsyncTasks.
 */
class AsyncTaskPageData
{
	
	enum Type
	{
		LoadWidgetPreviewData
	}
	
	AsyncTaskPageData(
			int p ,
			ArrayList<Object> l ,
			int cw ,
			int ch ,
			AsyncTaskCallback bgR ,
			AsyncTaskCallback postR ,
			WidgetPreviewLoader w )
	{
		page = p;
		items = l;
		generatedImages = new ArrayList<Bitmap>();
		maxImageWidth = cw;
		maxImageHeight = ch;
		doInBackgroundCallback = bgR;
		postExecuteCallback = postR;
		widgetPreviewLoader = w;
	}
	
	void cleanup(
			boolean cancelled )
	{
		// Clean up any references to source/generated bitmaps
		if( generatedImages != null )
		{
			if( cancelled )
			{
				for( int i = 0 ; i < generatedImages.size() ; i++ )
				{
					widgetPreviewLoader.recycleBitmap( items.get( i ) , generatedImages.get( i ) );
				}
			}
			generatedImages.clear();
		}
	}
	
	int page;
	ArrayList<Object> items;
	ArrayList<Bitmap> sourceImages;
	ArrayList<Bitmap> generatedImages;
	int maxImageWidth;
	int maxImageHeight;
	AsyncTaskCallback doInBackgroundCallback;
	AsyncTaskCallback postExecuteCallback;
	WidgetPreviewLoader widgetPreviewLoader;
}

/**
 * A generic template for an async task used in AppsCustomize.
 */
class AppsCustomizeAsyncTask extends AsyncTask<AsyncTaskPageData , Void , AsyncTaskPageData>
{
	
	AppsCustomizeAsyncTask(
			int p ,
			AsyncTaskPageData.Type ty )
	{
		page = p;
		threadPriority = Process.THREAD_PRIORITY_DEFAULT;
		dataType = ty;
	}
	
	@Override
	protected AsyncTaskPageData doInBackground(
			AsyncTaskPageData ... params )
	{
		if( params.length != 1 )
			return null;
		// Load each of the widget previews in the background
		params[0].doInBackgroundCallback.run( this , params[0] );
		return params[0];
	}
	
	@Override
	protected void onPostExecute(
			AsyncTaskPageData result )
	{
		// All the widget previews are loaded, so we can just callback to inflate the page
		result.postExecuteCallback.run( this , result );
	}
	
	void setThreadPriority(
			int p )
	{
		threadPriority = p;
	}
	
	void syncThreadPriority()
	{
		Process.setThreadPriority( threadPriority );
	}
	
	// The page that this async task is associated with
	AsyncTaskPageData.Type dataType;
	int page;
	int threadPriority;
}

/**
 * The Apps/Customize page that displays all the applications, widgets, and shortcuts.
 */
public class AppsCustomizePagedView extends PagedViewWithDraggableItems implements View.OnClickListener , View.OnKeyListener , DragSource , PagedViewIcon.PressedCallback , PagedViewWidget.ShortPressListener , ILauncherTransitionable , DropTarget , DropTargetListener , IOnThemeChanged//zhujieping add,换主题不重启
{
	
	static final String TAG = "AppsCustomizePagedView";
	
	/**
	 * The different content types that this paged view can show.
	 */
	public enum ContentType
	{
		Applications , Widgets
	}
	
	private ContentType mContentType = ContentType.Applications;
	// Refs
	private Launcher mLauncher;
	private DragController mDragController;
	private final LayoutInflater mLayoutInflater;
	private final PackageManager mPackageManager;
	// Save and Restore
	private int mSaveInstanceStateItemIndex = -1;
	private PagedViewIcon mPressedIcon;
	// Content
	private ArrayList<AppInfo> mApps;
	private ArrayList<Object> mWidgets;
	// Cling
	private boolean mHasShownAllAppsCling;
	private int mClingFocusedX;
	private int mClingFocusedY;
	// Caching
	private Canvas mCanvas;
	private IconCache mIconCache;
	// Dimens
	private int mContentWidthWidgets , mContentHeightWidgets;//cheyingkun add	//主菜单和小部件页面指示器、页面底边距分开配置(修正主菜单界面打开动态图标界面跳动问题)
	private int mContentWidthApps , mContentHeightApps;
	private int mWidgetCountX , mWidgetCountY;
	private int mWidgetWidthGap , mWidgetHeightGap;
	private PagedViewCellLayout mWidgetSpacingLayout;
	private int mNumAppsPages;
	private int mNumWidgetPages;
	// Relating to the scroll and overscroll effects
	Workspace.ZInterpolator mZInterpolator = new Workspace.ZInterpolator( 0.5f );
	private static float TRANSITION_SCALE_FACTOR = 0.74f;
	private static float TRANSITION_PIVOT = 0.65f;
	private static float TRANSITION_MAX_ROTATION = 22;
	private static final boolean PERFORM_OVERSCROLL_ROTATION = true;
	@SuppressWarnings( "unused" )
	private AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator( 0.9f );
	private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator( 4 );
	//桌面核心、抽屉形式随意切换 , change by shlt@2015/01/23 DEL START
	//public static int CONFIG_LAUNCHER_STYLE = LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER;
	//桌面核心、抽屉形式随意切换 , change by shlt@2015/01/23 DEL END
	// Previews & outlines
	ArrayList<AppsCustomizeAsyncTask> mRunningTasks;
	private static final int sPageSleepDelay = 200;
	private Runnable mInflateWidgetRunnable = null;
	private Runnable mBindWidgetRunnable = null;
	static final int WIDGET_NO_CLEANUP_REQUIRED = -1;
	static final int WIDGET_PRELOAD_PENDING = 0;
	static final int WIDGET_BOUND = 1;
	static final int WIDGET_INFLATED = 2;
	int mWidgetCleanupState = WIDGET_NO_CLEANUP_REQUIRED;
	int mWidgetLoadingId = -1;
	PendingAddWidgetInfo mCreateWidgetInfo = null;
	private boolean mDraggingWidget = false;
	private Toast mWidgetInstructionToast;
	// Deferral of loading widget previews during launcher transitions
	private boolean mInTransition;
	private ArrayList<AsyncTaskPageData> mDeferredSyncWidgetPageItems = new ArrayList<AsyncTaskPageData>();
	private ArrayList<Runnable> mDeferredPrepareLoadWidgetPreviewsTasks = new ArrayList<Runnable>();
	private Rect mTmpRect = new Rect();
	// Used for drawing shortcut previews
	BitmapCache mCachedShortcutPreviewBitmap = new BitmapCache();
	PaintCache mCachedShortcutPreviewPaint = new PaintCache();
	CanvasCache mCachedShortcutPreviewCanvas = new CanvasCache();
	// Used for drawing widget previews
	CanvasCache mCachedAppWidgetPreviewCanvas = new CanvasCache();
	RectCache mCachedAppWidgetPreviewSrcRect = new RectCache();
	RectCache mCachedAppWidgetPreviewDestRect = new RectCache();
	PaintCache mCachedAppWidgetPreviewPaint = new PaintCache();
	WidgetPreviewLoader mWidgetPreviewLoader;
	private boolean mInBulkBind;
	private boolean mNeedToUpdatePageCountsAndInvalidateData;
	private boolean isAppsLoop = false;
	private boolean isWidgetLoop = false;
	//cheyingkun add start	//主菜单排序和空位
	private boolean installAppSortEnd = false;//安装应用是否排列在最后
	private int[] applistEveryPageVacantNum;//主菜单每页空置的个数的数组
	private String[] applistDefaultComponents;//主菜单默认配置的的包类名
	//cheyingkun add end
	//cheyingkun add start	//主菜单和小部件页面指示器、页面底边距分开配置
	/**小部件、主菜单的页面指示器*/
	private PageIndicator mPageIndicator;
	private float appsPageIndicatorMarginBottom = 0f;
	private float widgetsPageIndicatorMarginBottom = 0f;
	private float appsPageIndicatorOffsetTop = 0f;
	private float appsPageIndicatorOffsetBottom = 0f;
	private float widgetsPageIndicatorOffset = 0f;
	//cheyingkun add end
	public static final int SORT_NAME = 1;
	public static final int SORT_INSTALL = 0;
	public static final int SORT_USE = 2;
	public static final int SORT_FACTORY = 3;
	public static int sortAppCheckId = SORT_NAME;
	private Dialog sortDialog = null;
	private int[] sortArray = null;
	public static final int NORMAL_MODE = 0;
	public static final int HIDE_MODE = 1;
	public static final int EDIT_MODE = 2;
	public static final int UNINSTALL_MODE = 3;//zhujieping add,增加卸载模式（图标右上角显示卸载标识，点击安装的应用可卸载,长按无作用）
	private int mApps_mode = NORMAL_MODE;
	private ArrayList<AppInfo> mHideApps;
	private View mCurrentDragView = null;
	private int preSearchBarVisiblity = -1;
	private int preSearchBarIndex = -1;
	private int preSearchBarTopMargin = -1;
	private int preHeight = -1;
	private static final String WIDGET_KEY_NAME = "com.cooee."; //chenliang add		//需求：编辑模式下，进入小部件界面，将Cooee的插件优先放在小部件列表的最前面。
	
	public AppsCustomizePagedView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		// zhangjin@2015/07/24 UPD START
		//CAMERA_DISTANCE = 6500;		
		// zhangjin@2015/07/24 UPD END
		mLayoutInflater = LayoutInflater.from( context );
		mPackageManager = context.getPackageManager();
		mApps = new ArrayList<AppInfo>();
		mWidgets = new ArrayList<Object>();
		mIconCache = ( LauncherAppState.getInstance() ).getIconCache();
		mCanvas = new Canvas();
		mRunningTasks = new ArrayList<AppsCustomizeAsyncTask>();
		// Save the default widget preview background
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.AppsCustomizePagedView , 0 , 0 );
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		mWidgetWidthGap = mWidgetHeightGap = grid.getEdgeMarginPx();
		mWidgetCountX = a.getInt( R.styleable.AppsCustomizePagedView_widgetCountX , 2 );
		mWidgetCountY = a.getInt( R.styleable.AppsCustomizePagedView_widgetCountY , 2 );
		mClingFocusedX = a.getInt( R.styleable.AppsCustomizePagedView_clingFocusedX , 0 );
		mClingFocusedY = a.getInt( R.styleable.AppsCustomizePagedView_clingFocusedY , 0 );
		a.recycle();
		mWidgetSpacingLayout = new PagedViewCellLayout( getContext() );
		// The padding on the non-matched dimension for the default widget preview icons
		// (top + bottom)
		mFadeInAdjacentScreens = false;
		//xiatian start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
		//		isAppsLoop = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_apps_loop_slide );//xiatian del
		isAppsLoop = LauncherDefaultConfig.SWITCH_ENABLE_APPLIST_LOOP_SLIDE;//xiatian add
		//xiatian end
		//xiatian start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
		//		isWidgetLoop = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_widget_loop_slide );//xiatian del
		isWidgetLoop = LauncherDefaultConfig.SWITCH_ENABLE_WIDGET_LOOP_SLIDE;//xiatian add
		//xiatian end
		//cheyingkun add start	//主菜单和小部件页面指示器、页面底边距分开配置
		appsPageIndicatorMarginBottom = LauncherDefaultConfig.getFloatDimension( R.dimen.apps_customize_page_indicator_margin_apps );
		widgetsPageIndicatorMarginBottom = LauncherDefaultConfig.getFloatDimension( R.dimen.apps_customize_page_indicator_margin_widgets );
		appsPageIndicatorOffsetBottom = LauncherDefaultConfig.getFloatDimension( R.dimen.apps_customize_page_indicator_offset_bottom_apps );
		appsPageIndicatorOffsetTop = LauncherDefaultConfig.getFloatDimension( R.dimen.apps_customize_page_indicator_offset_top_apps );
		widgetsPageIndicatorOffset = LauncherDefaultConfig.getFloatDimension( R.dimen.apps_customize_page_indicator_offset_widgets );
		//cheyingkun add end
		//zhujieping add start
		if(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_SORT_APP )//zhujieping add //拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
		)
		{
			//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
			if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6 )
			{
				sortAppCheckId = PreferenceManager.getDefaultSharedPreferences( getContext() ).getInt( "sort_app" , SORT_INSTALL );
			}
			else
			//zhujieping add end
			{
				sortAppCheckId = PreferenceManager.getDefaultSharedPreferences( getContext() ).getInt( "sort_app" , SORT_NAME );
			}
			mHideApps = new ArrayList<AppInfo>();
		}
		//zhujieping add end
	}
	
	@Override
	protected void init()
	{
		super.init();
		mCenterPagesVertically = false;
		Context context = getContext();
		Resources r = context.getResources();
		setDragSlopeThreshold( LauncherDefaultConfig.getInt( R.integer.config_appsCustomizeDragSlopeThreshold ) / 100f );
		//		initAnimationStyle();
	}
	
	public void onFinishInflate()
	{
		super.onFinishInflate();
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//cheyingkun add start	//整理完善图标检测功能【c_0004366】
		Rect padding = grid.getAppsCustomizePagedViewPadding();
		setPadding( padding.left , padding.top , padding.right , padding.bottom );
		//cheyingkun add end
		//cheyingkun add start	//主菜单排序和空位
		mCellCountX = (int)grid.getAllAppsNumCols();
		mCellCountY = (int)grid.getAllAppsNumRows();
		initAppListDefaultData();
		//cheyingkun add end
	}
	
	/** Returns the item index of the center item on this page so that we can restore to this
	 *  item index when we rotate. */
	private int getMiddleComponentIndexOnCurrentPage()
	{
		int i = -1;
		if( getPageCount() > 0 )
		{
			int currentPage = getCurrentPage();
			if( mContentType == ContentType.Applications )
			{
				AppsCustomizeCellLayout layout = (AppsCustomizeCellLayout)getPageAt( currentPage );
				ShortcutAndWidgetContainer childrenLayout = layout.getShortcutsAndWidgets();
				int numItemsPerPage = mCellCountX * mCellCountY;
				int childCount = childrenLayout.getChildCount();
				if( childCount > 0 )
				{
					i = ( currentPage * numItemsPerPage ) + ( childCount / 2 );
				}
			}
			else if( mContentType == ContentType.Widgets )
			{
				int numApps = mApps.size();
				PagedViewGridLayout layout = (PagedViewGridLayout)getPageAt( currentPage );
				int numItemsPerPage = mWidgetCountX * mWidgetCountY;
				int childCount = layout.getChildCount();
				if( childCount > 0 )
				{
					i = numApps + ( currentPage * numItemsPerPage ) + ( childCount / 2 );
				}
			}
			else
			{
				throw new RuntimeException( "Invalid ContentType" );
			}
		}
		return i;
	}
	
	/** Get the index of the item to restore to if we need to restore the current page. */
	public int getSaveInstanceStateIndex()
	{
		if( mSaveInstanceStateItemIndex == -1 )
		{
			mSaveInstanceStateItemIndex = getMiddleComponentIndexOnCurrentPage();
		}
		return mSaveInstanceStateItemIndex;
	}
	
	/** Returns the page in the current orientation which is expected to contain the specified
	 *  item index. */
	int getPageForComponent(
			int index )
	{
		if( index < 0 )
			return 0;
		if( index < mApps.size() )
		{
			int numItemsPerPage = mCellCountX * mCellCountY;
			return( index / numItemsPerPage );
		}
		else
		{
			int numItemsPerPage = mWidgetCountX * mWidgetCountY;
			return ( index - mApps.size() ) / numItemsPerPage;
		}
	}
	
	/** Restores the page for an item at the specified index */
	public void restorePageForIndex(
			int index )
	{
		if( index < 0 )
			return;
		mSaveInstanceStateItemIndex = index;
	}
	
	private void updatePageCounts()
	{
		mNumWidgetPages = (int)Math.ceil( mWidgets.size() / (float)( mWidgetCountX * mWidgetCountY ) );
		//cheyingkun add start	//主菜单排序和空位
		int vacantCount = 0;
		if( applistEveryPageVacantNum != null && applistEveryPageVacantNum.length > 0 )
		{
			for( int i = 0 ; i < applistEveryPageVacantNum.length ; i++ )
			{
				vacantCount += applistEveryPageVacantNum[i];
			}
		}
		//cheyingkun add end
		mNumAppsPages = (int)Math.ceil( ( (float)mApps.size() + vacantCount ) / ( mCellCountX * mCellCountY ) );
	}
	
	protected void onDataReady(
			int width ,
			int height )
	{
		if( mWidgetPreviewLoader == null )
		{
			mWidgetPreviewLoader = new WidgetPreviewLoader( mLauncher );
		}
		// Now that the data is ready, we can calculate the content width, the number of cells to
		// use for each page
		//		LauncherAppState app = LauncherAppState.getInstance();
		//		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		mWidgetSpacingLayout.setPadding( mPageLayoutPaddingLeft , mPageLayoutPaddingTop , mPageLayoutPaddingRight , mPageLayoutPaddingBottom );
		//cheyingkun del start	//主菜单配需和空位
		//		mCellCountX = (int)grid.getAllAppsNumCols();
		//		mCellCountY = (int)grid.getAllAppsNumRows();
		//cheyingkun del end
		updatePageCounts();
		// Force a measure to update recalculate the gaps
		measureWidgetCellWidthAndHeight();//cheyingkun add	//主菜单和小部件页面指示器、页面底边距分开配置(修正小部件界面的宽高)
		AppsCustomizeTabHost host = (AppsCustomizeTabHost)getTabHost();
		final boolean hostIsTransitioning = host.isTransitioning();
		// Restore the page
		int page = getPageForComponent( mSaveInstanceStateItemIndex );
		invalidatePageData( Math.max( 0 , page ) , hostIsTransitioning );
		// Show All Apps cling if we are finished transitioning, otherwise, we will try again when
		// the transition completes in AppsCustomizeTabHost (otherwise the wrong offsets will be
		// returned while animating)
		if( !hostIsTransitioning )
		{
			post( new Runnable() {
				
				@Override
				public void run()
				{
					showAllAppsCling();
				}
			} );
		}
	}
	
	void showAllAppsCling()
	{
		if( !mHasShownAllAppsCling && isDataReady() )
		{
			mHasShownAllAppsCling = true;
			// Calculate the position for the cling punch through
			int[] offset = new int[2];
			int[] pos = mWidgetSpacingLayout.estimateCellPosition( mClingFocusedX , mClingFocusedY );
			mLauncher.getDragLayer().getLocationInDragLayer( this , offset );
			// PagedViews are centered horizontally but top aligned
			// Note we have to shift the items up now that Launcher sits under the status bar
			pos[0] += ( getMeasuredWidth() - mWidgetSpacingLayout.getMeasuredWidth() ) / 2 + offset[0];
			pos[1] += offset[1] - mLauncher.getDragLayer().getPaddingTop();
		}
	}
	
	public void setDataIsReady(
			boolean isready )
	{
		mIsDataReady = isready;
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		int width = MeasureSpec.getSize( widthMeasureSpec );
		int height = MeasureSpec.getSize( heightMeasureSpec );
		if( preHeight > 0 )
		{
			if( preHeight != height )
			{
				if( Math.abs( preHeight - height ) == LauncherAppState.getInstance().getNavigationBarHeight() )//这个判断说明是动态导航栏的变化
				{
					onUIChanged( height );
				}
			}
		}
		preHeight = height;
		if( !isDataReady() )
		{
			//<phenix modify> liuhailin@2015-03-10 modify begin
			//if( ( ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE ) || !mApps.isEmpty() ) && !mWidgets.isEmpty() )
			if( ( !mApps.isEmpty() ) && !mWidgets.isEmpty() )
			//<phenix modify> liuhailin@2015-03-10 modify end
			{
				setDataIsReady();
				setMeasuredDimension( width , height );
				onDataReady( width , height );
			}
		}
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
	}
	
	public synchronized void onPackagesUpdated(
			ArrayList<Object> widgetsAndShortcuts )
	{
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		// Get the list of widgets and shortcuts
		mWidgets.clear();
		ArrayList<Object> mWidgetsNotCooee = new ArrayList<Object>(); //chenliang add		//需求：编辑模式下，进入小部件界面，将Cooee的插件优先放在小部件列表的最前面。
		for( Object o : widgetsAndShortcuts )
		{
			if( o instanceof AppWidgetProviderInfo )
			{
				AppWidgetProviderInfo widget = (AppWidgetProviderInfo)o;
				if( LauncherAppState.hideWidgetList( widget.provider.getPackageName() , widget.provider.getClassName() ) )
				{
					continue;
				}
				widget.label = widget.label.trim();
				if( widget.minWidth > 0 && widget.minHeight > 0 )
				{
					// Ensure that all widgets we show can be added on a workspace of this size
					int[] spanXY = Launcher.getSpanForWidget( mLauncher , widget );
					int[] minSpanXY = Launcher.getMinSpanForWidget( mLauncher , widget );
					int minSpanX = Math.min( spanXY[0] , minSpanXY[0] );
					int minSpanY = Math.min( spanXY[1] , minSpanXY[1] );
					float mNumColumns = grid.getNumColumns();
					float mNumRows = grid.getNumRows();
					if( minSpanX <= (int)mNumColumns && minSpanY <= (int)mNumRows )
					{
						//chenliang start	//需求：编辑模式下，进入小部件界面，将Cooee的插件优先放在小部件列表的最前面。
						//						mWidgets.add( widget );		//chenliang del
						//chenliang add start
						if( widget.provider.getPackageName().contains( WIDGET_KEY_NAME ) )
						{
							mWidgets.add( widget );
						}
						else
						{
							mWidgetsNotCooee.add( widget );
						}
						//chenliang add end
						//chenliang end
					}
					else
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.e( TAG , StringUtils.concat(
									"Widget " ,
									widget.provider.toString() ,
									" can not fit on this device minSpanX:" ,
									minSpanX ,
									"-minSpanY:" ,
									minSpanY ,
									"-mNumColumns:" ,
									mNumColumns ,
									"-mNumRows:" ,
									mNumRows ) );
					}
				}
				else
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.e(
								TAG ,
								StringUtils.concat( "Widget " , widget.provider.toString() , " has invalid dimensions widget.minWidth:" , widget.minWidth , "-widget.minHeight:" , widget.minHeight ) );
					}
				}
			}
			else
			{
				// just add shortcuts
				mWidgetsNotCooee.add( o );
			}
		}
		mWidgets.addAll( mWidgetsNotCooee ); //chenliang add		//编辑模式下，进入小部件界面，将Cooee的插件优先放在小部件列表的最前面。
		updatePageCountsAndInvalidateData();
	}
	
	public void setBulkBind(
			boolean bulkBind )
	{
		if( bulkBind )
		{
			mInBulkBind = true;
		}
		else
		{
			mInBulkBind = false;
			if( mNeedToUpdatePageCountsAndInvalidateData )
			{
				updatePageCountsAndInvalidateData();
			}
		}
	}
	
	private void updatePageCountsAndInvalidateData()
	{
		if( mInBulkBind )
		{
			mNeedToUpdatePageCountsAndInvalidateData = true;
		}
		else
		{
			updatePageCounts();
			mLauncher.runOnUiThread( new Runnable() {
				
				@Override
				public void run()
				{
					invalidateOnDataChange();
					mNeedToUpdatePageCountsAndInvalidateData = false;
				}
			} );
		}
	}
	
	@Override
	public void onClick(
			View v )
	{
		// When we have exited all apps or are in transition, disregard clicks
		if( !mLauncher.isAllAppsVisible() || mLauncher.getWorkspace().isSwitchingState() )
			return;
		if( v instanceof PagedViewIcon )
		{
			// Animate some feedback to the click
			if( mApps_mode == HIDE_MODE )
			{
				( (PagedViewIcon)v ).changeCheckedState();
				return;
			}
			//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
			if( mApps_mode == UNINSTALL_MODE )
			{
				if( v.getTag() instanceof AppInfo )
				{
					AppInfo appInfo = (AppInfo)v.getTag();
					if( ( appInfo.getFlags() & AppInfo.DOWNLOADED_FLAG ) != 0 )
					{
						mLauncher.startApplicationUninstallActivity( appInfo.getComponentName() , appInfo.getFlags() );
					}
				}
				return;
			}
			//zhujieping add end
			final AppInfo appInfo = (AppInfo)v.getTag();
			// Lock the drawable state to pressed until we return to Launcher
			if( mPressedIcon != null )
			{
				mPressedIcon.lockDrawableState();
			}
			mLauncher.startActivitySafely( v , appInfo.getIntent() , appInfo );
			mLauncher.getStats().recordLaunch( appInfo.getIntent() );
		}
		else if( v instanceof PagedViewWidget )
		{
			// Let the user know that they have to long press to add a widget
			if( mWidgetInstructionToast != null )
			{
				mWidgetInstructionToast.cancel();
			}
			mWidgetInstructionToast = Toast.makeText( getContext() , R.string.long_press_widget_to_add , Toast.LENGTH_SHORT );
			mWidgetInstructionToast.show();
			// Create a little animation to show that the widget can move
			float offsetY = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dragViewOffsetY );
			final ImageView p = (ImageView)v.findViewById( R.id.widget_preview );
			AnimatorSet bounce = LauncherAnimUtils.createAnimatorSet();
			ValueAnimator tyuAnim = LauncherAnimUtils.ofFloat( p , "translationY" , offsetY );
			tyuAnim.setDuration( 125 );
			ValueAnimator tydAnim = LauncherAnimUtils.ofFloat( p , "translationY" , 0f );
			tydAnim.setDuration( 100 );
			bounce.play( tyuAnim ).before( tydAnim );
			bounce.setInterpolator( new AccelerateInterpolator() );
			bounce.start();
		}
	}
	
	public boolean onKey(
			View v ,
			int keyCode ,
			KeyEvent event )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		{
			return FocusHelper.handleAppsCustomizeKeyEvent( v , keyCode , event );
		}
		//cheyingkun add start	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		else
		{
			return false;
		}
		//cheyingkun add end
	}
	
	/*
	 * PagedViewWithDraggableItems implementation
	 */
	@Override
	protected void determineDraggingStart(
			android.view.MotionEvent ev )
	{
		// Disable dragging by pulling an app down for now.
	}
	
	private void beginDraggingApplication(
			View v )
	{
		mLauncher.getWorkspace().onDragStartedWithItem( v );
		mLauncher.getWorkspace().beginDragShared( v , this );
	}
	
	Bundle getDefaultOptionsForWidget(
			Launcher launcher ,
			PendingAddWidgetInfo info )
	{
		Bundle options = null;
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 )
		{
			AppWidgetResizeFrame.getWidgetSizeRanges( mLauncher , info.getSpanX() , info.getSpanY() , mTmpRect );
			Rect padding = AppWidgetHostView.getDefaultPaddingForWidget( mLauncher , info.getComponentName() , null );
			float density = getResources().getDisplayMetrics().density;
			int xPaddingDips = (int)( ( padding.left + padding.right ) / density );
			int yPaddingDips = (int)( ( padding.top + padding.bottom ) / density );
			options = new Bundle();
			options.putInt( AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH , mTmpRect.left - xPaddingDips );
			options.putInt( AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT , mTmpRect.top - yPaddingDips );
			options.putInt( AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH , mTmpRect.right - xPaddingDips );
			options.putInt( AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT , mTmpRect.bottom - yPaddingDips );
		}
		return options;
	}
	
	private void preloadWidget(
			final PendingAddWidgetInfo info )
	{
		final AppWidgetProviderInfo pInfo = info.getAppWidgetProviderInfo();
		final Bundle options = getDefaultOptionsForWidget( mLauncher , info );
		if( pInfo.configure != null )
		{
			info.setBindOptions( options );
			return;
		}
		mWidgetCleanupState = WIDGET_PRELOAD_PENDING;
		mBindWidgetRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				mWidgetLoadingId = mLauncher.getAppWidgetHost().allocateAppWidgetId();
				// Options will be null for platforms with JB or lower, so this serves as an
				// SDK level check.
				if( options == null )
				{
					// gaominghui@2016/12/14 ADD START
					boolean bindAppWidgetIdIfAllowed;
					if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
					{
						bindAppWidgetIdIfAllowed = AppWidgetManager.getInstance( mLauncher ).bindAppWidgetIdIfAllowed( mWidgetLoadingId , info.getComponentName() );
					}
					else
					{
						bindAppWidgetIdIfAllowed = Tools.bindAppWidgetId( AppWidgetManager.getInstance( mLauncher ) , mWidgetLoadingId , info.getComponentName() );
					}
					if( bindAppWidgetIdIfAllowed )
					{
						mWidgetCleanupState = WIDGET_BOUND;
					}
					// gaominghui@2016/12/14 ADD END
				}
				else
				{
					// gaominghui@2016/12/14 ADD START
					boolean bindAppWidgetIdIfAllowed;
					if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
					{
						bindAppWidgetIdIfAllowed = AppWidgetManager.getInstance( mLauncher ).bindAppWidgetIdIfAllowed( mWidgetLoadingId , info.getComponentName() , options );
					}
					else
					{
						bindAppWidgetIdIfAllowed = Tools.bindAppWidgetId( AppWidgetManager.getInstance( mLauncher ) , mWidgetLoadingId , info.getComponentName() );
					}
					if( bindAppWidgetIdIfAllowed )
					{
						mWidgetCleanupState = WIDGET_BOUND;
					}
					// gaominghui@2016/12/14 ADD END
				}
			}
		};
		post( mBindWidgetRunnable );
		mInflateWidgetRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				if( mWidgetCleanupState != WIDGET_BOUND )
				{
					return;
				}
				AppWidgetHostView hostView = mLauncher.getAppWidgetHost().createView( getContext() , mWidgetLoadingId , pInfo );
				info.setAppWidgetHostView( hostView );
				mWidgetCleanupState = WIDGET_INFLATED;
				hostView.setVisibility( INVISIBLE );
				int[] unScaledSize = mLauncher.getWorkspace().estimateItemSize( info.getSpanX() , info.getSpanY() , info , false );
				// We want the first widget layout to be the correct size. This will be important
				// for width size reporting to the AppWidgetManager.
				DragLayer.LayoutParams lp = new DragLayer.LayoutParams( unScaledSize[0] , unScaledSize[1] );
				lp.x = lp.y = 0;
				lp.customPosition = true;
				hostView.setLayoutParams( lp );
				mLauncher.getDragLayer().addView( hostView );
			}
		};
		post( mInflateWidgetRunnable );
	}
	
	@Override
	public void onShortPress(
			View v )
	{
		// We are anticipating a long press, and we use this time to load bind and instantiate
		// the widget. This will need to be cleaned up if it turns out no long press occurs.
		if( mCreateWidgetInfo != null )
		{
			// Just in case the cleanup process wasn't properly executed. This shouldn't happen.
			cleanupWidgetPreloading( false );
		}
		mCreateWidgetInfo = new PendingAddWidgetInfo( (PendingAddWidgetInfo)v.getTag() );
		preloadWidget( mCreateWidgetInfo );
	}
	
	private void cleanupWidgetPreloading(
			boolean widgetWasAdded )
	{
		if( !widgetWasAdded )
		{
			// If the widget was not added, we may need to do further cleanup.
			PendingAddWidgetInfo info = mCreateWidgetInfo;
			mCreateWidgetInfo = null;
			if( mWidgetCleanupState == WIDGET_PRELOAD_PENDING )
			{
				// We never did any preloading, so just remove pending callbacks to do so
				removeCallbacks( mBindWidgetRunnable );
				removeCallbacks( mInflateWidgetRunnable );
			}
			else if( mWidgetCleanupState == WIDGET_BOUND )
			{
				// Delete the widget id which was allocated
				if( mWidgetLoadingId != -1 )
				{
					mLauncher.getAppWidgetHost().deleteAppWidgetId( mWidgetLoadingId );
				}
				// We never got around to inflating the widget, so remove the callback to do so.
				removeCallbacks( mInflateWidgetRunnable );
			}
			else if( mWidgetCleanupState == WIDGET_INFLATED )
			{
				// Delete the widget id which was allocated
				if( mWidgetLoadingId != -1 )
				{
					mLauncher.getAppWidgetHost().deleteAppWidgetId( mWidgetLoadingId );
				}
				// The widget was inflated and added to the DragLayer -- remove it.
				AppWidgetHostView widget = info.getAppWidgetHostView();
				mLauncher.getDragLayer().removeView( widget );
			}
		}
		mWidgetCleanupState = WIDGET_NO_CLEANUP_REQUIRED;
		mWidgetLoadingId = -1;
		mCreateWidgetInfo = null;
		PagedViewWidget.resetShortPressTarget();
	}
	
	@Override
	public void cleanUpShortPress(
			View v )
	{
		if( !mDraggingWidget )
		{
			cleanupWidgetPreloading( false );
		}
	}
	
	private boolean beginDraggingWidget(
			View v )
	{
		mDraggingWidget = true;
		// Get the widget preview as the drag representation
		ImageView image = (ImageView)v.findViewById( R.id.widget_preview );
		PendingAddItemInfo createItemInfo = (PendingAddItemInfo)v.getTag();
		// If the ImageView doesn't have a drawable yet, the widget preview hasn't been loaded and
		// we abort the drag.
		if( image.getDrawable() == null )
		{
			mDraggingWidget = false;
			return false;
		}
		// Compose the drag image
		Bitmap preview;
		Bitmap outline;
		float scale = 1f;
		Point previewPadding = null;
		if( createItemInfo instanceof PendingAddWidgetInfo )
		{
			// This can happen in some weird cases involving multi-touch. We can't start dragging
			// the widget if this is null, so we break out.
			if( mCreateWidgetInfo == null )
			{
				return false;
			}
			PendingAddWidgetInfo createWidgetInfo = mCreateWidgetInfo;
			createItemInfo = createWidgetInfo;
			int spanX = createItemInfo.getSpanX();
			int spanY = createItemInfo.getSpanY();
			int[] size = mLauncher.getWorkspace().estimateItemSize( spanX , spanY , createWidgetInfo , true );
			FastBitmapDrawable previewDrawable = (FastBitmapDrawable)image.getDrawable();
			float minScale = 1.25f;
			int maxWidth , maxHeight;
			maxWidth = Math.min( (int)( previewDrawable.getIntrinsicWidth() * minScale ) , size[0] );
			maxHeight = Math.min( (int)( previewDrawable.getIntrinsicHeight() * minScale ) , size[1] );
			int[] previewSizeBeforeScale = new int[1];
			preview = mWidgetPreviewLoader.generateWidgetPreview(
					createWidgetInfo.getComponentName() ,
					createWidgetInfo.getPreviewImage() ,
					createWidgetInfo.getIcon() ,
					spanX ,
					spanY ,
					maxWidth ,
					maxHeight ,
					null ,
					previewSizeBeforeScale );
			// Compare the size of the drag preview to the preview in the AppsCustomize tray
			int previewWidthInAppsCustomize = Math.min( previewSizeBeforeScale[0] , mWidgetPreviewLoader.maxWidthForWidgetPreview( spanX ) );
			scale = previewWidthInAppsCustomize / (float)preview.getWidth();
			// The bitmap in the AppsCustomize tray is always the the same size, so there
			// might be extra pixels around the preview itself - this accounts for that
			if( previewWidthInAppsCustomize < previewDrawable.getIntrinsicWidth() )
			{
				int padding = ( previewDrawable.getIntrinsicWidth() - previewWidthInAppsCustomize ) / 2;
				previewPadding = new Point( padding , 0 );
			}
		}
		else
		{
			PendingAddShortcutInfo createShortcutInfo = (PendingAddShortcutInfo)v.getTag();
			//cheyingkun start	//获取高分辨率代码整理和注释。
			//			Drawable icon = mIconCache.getFullResIcon( createShortcutInfo.getActivityInfo() );//cheyingkun del
			Drawable icon = mIconCache.getFullResIcon( createShortcutInfo.getActivityInfo() , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight );//cheyingkun add
			//cheyingkun end
			preview = Bitmap.createBitmap( icon.getIntrinsicWidth() , icon.getIntrinsicHeight() , Bitmap.Config.ARGB_8888 );
			mCanvas.setBitmap( preview );
			mCanvas.save();
			WidgetPreviewLoader.renderDrawableToBitmap( icon , preview , 0 , 0 , icon.getIntrinsicWidth() , icon.getIntrinsicHeight() );
			mCanvas.restore();
			mCanvas.setBitmap( null );
			createItemInfo.setSpanX( 1 );
			createItemInfo.setSpanY( 1 );
		}
		// Don't clip alpha values for the drag outline if we're using the default widget preview
		boolean clipAlpha = !( createItemInfo instanceof PendingAddWidgetInfo && ( ( (PendingAddWidgetInfo)createItemInfo ).getPreviewImage() == 0 ) );
		// Save the preview for the outline generation, then dim the preview
		outline = Bitmap.createScaledBitmap( preview , preview.getWidth() , preview.getHeight() , false );
		// Start the drag
		mLauncher.getWorkspace().onDragStartedWithItem( createItemInfo , outline , clipAlpha );
		mDragController.startDrag( image , preview , this , createItemInfo , DragController.DRAG_ACTION_COPY , previewPadding , scale );
		outline.recycle();
		preview.recycle();
		return true;
	}
	
	@Override
	protected boolean beginDragging(
			final View v )
	{
		if( !super.beginDragging( v ) )
			return false;
		if( v instanceof PagedViewIcon )
		{
			beginDraggingApplication( v );
			if( mApps_mode == EDIT_MODE )
			{
				mCurrentDragView = v;
				mCurrentDragView.setVisibility( View.INVISIBLE );
				AppsCustomizeTabHost host = (AppsCustomizeTabHost)getTabHost();
				host.setStateBarShow( false , true );
				return true;
			}
		}
		else if( v instanceof PagedViewWidget )
		{
			if( !beginDraggingWidget( v ) )
			{
				return false;
			}
		}
		// We delay entering spring-loaded mode slightly to make sure the UI
		// thready is free of any work.
		postDelayed( new Runnable() {
			
			@Override
			public void run()
			{
				// We don't enter spring-loaded mode if the drag has been cancelled
				if( mLauncher.getDragController().isDragging() )
				{
					// Reset the alpha on the dragged icon before we drag
					resetDrawableState();
					// Go into spring loaded mode (must happen before we startDrag())
					//zhujieping  start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
					//mLauncher.enterSpringLoadedDragMode();//zhujieping del
					//zhujieping add start
					AppsCustomizePagedView.ContentType type = AppsCustomizePagedView.ContentType.Applications;
					if( mDraggingWidget )
					{
						type = AppsCustomizePagedView.ContentType.Widgets;
					}
					mLauncher.enterSpringLoadedDragMode( type );
					//zhujieping add end
					//zhujieping  end
				}
			}
		} , 150 );
		return true;
	}
	
	/**
	 * Clean up after dragging.
	 *
	 * @param target where the item was dragged to (can be null if the item was flung)
	 */
	private void endDragging(
			View target ,
			boolean isFlingToDelete ,
			boolean success )
	{
		if( isFlingToDelete || !success || ( target != mLauncher.getWorkspace() && !( target instanceof DeleteDropTarget ) ) )
		{
			// Exit spring loaded mode if we have not successfully dropped or have not handled the
			// drop in Workspace
			mLauncher.exitSpringLoadedDragMode();
		}
	}
	
	@Override
	public View getContent()
	{
		return null;
	}
	
	@Override
	public void onLauncherTransitionPrepare(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		mInTransition = true;
		if( toWorkspace )
		{
			cancelAllTasks();
		}
		//cheyingkun add start	//主菜单和小部件页面指示器、页面底边距分开配置
		else
		{
			changeMarginBottom();
		}
		//cheyingkun add end
	}
	
	@Override
	public void onLauncherTransitionStart(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
	}
	
	@Override
	public void onLauncherTransitionStep(
			Launcher l ,
			float t )
	{
	}
	
	@Override
	public void onLauncherTransitionEnd(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		mInTransition = false;
		for( AsyncTaskPageData d : mDeferredSyncWidgetPageItems )
		{
			onSyncWidgetPageItems( d );
		}
		mDeferredSyncWidgetPageItems.clear();
		for( Runnable r : mDeferredPrepareLoadWidgetPreviewsTasks )
		{
			r.run();
		}
		mDeferredPrepareLoadWidgetPreviewsTasks.clear();
		mForceDrawAllChildrenNextFrame = !toWorkspace;
	}
	
	@Override
	public void onDropCompleted(
			View target ,
			DragObject d ,
			boolean isFlingToDelete ,
			boolean success )
	{
		// Return early and wait for onFlingToDeleteCompleted if this was the result of a fling
		if( isFlingToDelete )
			return;
		endDragging( target , false , success );
		// Display an error message if the drag failed due to there not being enough space on the
		// target layout we were dropping on.
		if( !success )
		{
			boolean showOutOfSpaceMessage = false;
			if( target instanceof Workspace )
			{
				int currentScreen = mLauncher.getCurrentWorkspaceScreen();
				Workspace workspace = (Workspace)target;
				CellLayout layout = (CellLayout)workspace.getChildAt( currentScreen );
				ItemInfo itemInfo = (ItemInfo)d.dragInfo;
				if( layout != null )
				{
					layout.calculateSpans( itemInfo );
					showOutOfSpaceMessage = !layout.findCellForSpan( null , itemInfo.getSpanX() , itemInfo.getSpanY() );
				}
			}
			if( showOutOfSpaceMessage )
			{
				mLauncher.showOutOfSpaceMessage( false );
			}
			d.deferDragViewCleanupPostAnimation = false;
		}
		cleanupWidgetPreloading( success );
		mDraggingWidget = false;
		//zhujieping add start，编辑模式下拖动的view显示出来
		if( mCurrentDragView != null )
		{
			mCurrentDragView.setVisibility( View.VISIBLE );
			mCurrentDragView = null;
		}
		//			if( mApps_mode == EDIT_MODE )//这个放到searchdroptarget中droptargetanim动画结束的回调中执行
		//			{
		//				AppsCustomizeTabHost host = (AppsCustomizeTabHost)getTabHost();
		//				host.setStateBarShow( true , true );
		//			}
		//zhujieping add end
	}
	
	@Override
	public void onFlingToDeleteCompleted()
	{
		// We just dismiss the drag when we fling, so cleanup here
		endDragging( null , true , true );
		cleanupWidgetPreloading( false );
		mDraggingWidget = false;
	}
	
	@Override
	public boolean supportsFlingToDelete()
	{
		return true;
	}
	
	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		cancelAllTasks();
	}
	
	public void clearAllWidgetPages()
	{
		cancelAllTasks();
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View v = getPageAt( i );
			if( v instanceof PagedViewGridLayout )
			{
				( (PagedViewGridLayout)v ).removeAllViewsOnPage();
				mDirtyPageContent.set( i , true );
			}
		}
	}
	
	private void cancelAllTasks()
	{
		// Clean up all the async tasks
		Iterator<AppsCustomizeAsyncTask> iter = mRunningTasks.iterator();
		while( iter.hasNext() )
		{
			AppsCustomizeAsyncTask task = (AppsCustomizeAsyncTask)iter.next();
			task.cancel( false );
			iter.remove();
			mDirtyPageContent.set( task.page , true );
			// We've already preallocated the views for the data to load into, so clear them as well
			View v = getPageAt( task.page );
			if( v instanceof PagedViewGridLayout )
			{
				( (PagedViewGridLayout)v ).removeAllViewsOnPage();
			}
		}
		mDeferredSyncWidgetPageItems.clear();
		mDeferredPrepareLoadWidgetPreviewsTasks.clear();
	}
	
	public void setContentType(
			ContentType type )
	{
		int page = getCurrentPage();
		if( mContentType != type )
		{
			page = 0;
		}
		mContentType = type;
		changeMarginBottom();//zhujieping，小组件和apps的页面指示器位置配置的不一致，切换后要重新设置margin，否则小组件会被截取掉
		if( mContentType == ContentType.Widgets )
		{
			setLoop( isWidgetLoop );
		}
		else
		{
			setLoop( isAppsLoop );
		}
		invalidatePageData( page , true );
	}
	
	public ContentType getContentType()
	{
		return mContentType;
	}
	
	protected void snapToPage(
			int whichPage ,
			int delta ,
			int duration )
	{
		super.snapToPage( whichPage , delta , duration );
		// Update the thread priorities given the direction lookahead
		Iterator<AppsCustomizeAsyncTask> iter = mRunningTasks.iterator();
		while( iter.hasNext() )
		{
			AppsCustomizeAsyncTask task = (AppsCustomizeAsyncTask)iter.next();
			int pageIndex = task.page;
			if( ( mNextPage > mCurrentPage && pageIndex >= mCurrentPage ) || ( mNextPage < mCurrentPage && pageIndex <= mCurrentPage ) )
			{
				task.setThreadPriority( getThreadPriorityForPage( pageIndex ) );
			}
			else
			{
				task.setThreadPriority( Process.THREAD_PRIORITY_LOWEST );
			}
		}
	}
	
	/*
	 * Apps PagedView implementation
	 */
	private void setVisibilityOnChildren(
			ViewGroup layout ,
			int visibility )
	{
		int childCount = layout.getChildCount();
		for( int i = 0 ; i < childCount ; ++i )
		{
			layout.getChildAt( i ).setVisibility( visibility );
		}
	}
	
	private void setupPageApps(
			AppsCustomizeCellLayout layout )
	{
		layout.setGridSize( mCellCountX , mCellCountY );
		// Note: We force a measure here to get around the fact that when we do layout calculations
		// immediately after syncing, we don't have a proper width.  That said, we already know the
		// expected page width, so we can actually optimize by hiding all the TextView-based
		// children that are expensive to measure, and let that happen naturally later.
		setVisibilityOnChildren( layout , View.GONE );
		int widthSpec = MeasureSpec.makeMeasureSpec( mContentWidthApps , MeasureSpec.AT_MOST );
		int heightSpec = MeasureSpec.makeMeasureSpec( mContentHeightApps , MeasureSpec.AT_MOST );
		layout.setMinimumWidth( getPageContentWidth() );
		layout.measure( widthSpec , heightSpec );
		setVisibilityOnChildren( layout , View.VISIBLE );
	}
	
	public void syncAppsPageItems(
			int page ,
			boolean immediate )
	{
		//cheyingkun add start	//主菜单排序和空位
		int currentPageVacant = 0;//当前页空位个数
		int prePageVacantCount = 0;//page页之前 所有的空位个数
		if( applistEveryPageVacantNum != null )
		{
			if( applistEveryPageVacantNum.length > page )
			{
				currentPageVacant = applistEveryPageVacantNum[page];
			}
			for( int i = 0 ; i < page ; i++ )
			{
				if( applistEveryPageVacantNum.length > i )
				{
					prePageVacantCount += applistEveryPageVacantNum[i];
				}
			}
		}
		//cheyingkun add end
		// ensure that we have the right number of items on the pages
		final boolean isRtl = isLayoutRtl();
		int numCells = mCellCountX * mCellCountY;
		int startIndex = page * numCells - prePageVacantCount;
		int endIndex = Math.min( startIndex + numCells - currentPageVacant , mApps.size() );
		AppsCustomizeCellLayout layout = (AppsCustomizeCellLayout)getPageAt( page );
		layout.removeAllViewsOnPage();
		ArrayList<Object> items = new ArrayList<Object>();
		ArrayList<Bitmap> images = new ArrayList<Bitmap>();
		for( int i = startIndex ; i < endIndex ; ++i )
		{
			AppInfo info;
			if( sortArray != null && mApps.size() == sortArray.length )
			{
				info = mApps.get( sortArray[i] );
			}
			else
			{
				info = mApps.get( i );
			}
			PagedViewIcon icon = (PagedViewIcon)mLayoutInflater.inflate( R.layout.apps_customize_application , layout , false );
			icon.applyFromApplicationInfo( info , true , this );
			icon.setOnClickListener( this );
			icon.setOnLongClickListener( this );
			icon.setOnTouchListener( this );
			icon.setOnKeyListener( this );
			int index = i - startIndex;
			int x = index % mCellCountX;
			int y = index / mCellCountX;
			if( isRtl )
			{
				x = mCellCountX - x - 1;
			}
			//zhujieping add start，在隐藏模式时，卸载或安装应用，applist会重新加载，所有的icon要显示checkbox
			if( mApps_mode == HIDE_MODE )
			{
				icon.setCheckboxShow( true );
			}
			//zhujieping add end
			//zhujieping add start，在卸载模式时，卸载或安装应用，applist会重新加载，所有的icon要显示右上角的卸载按钮
			else if( mApps_mode == UNINSTALL_MODE )
			{
				icon.setUninstallIconShow( true );
			}
			//zhujieping add end
			layout.addViewToCellLayout( icon , -1 , i , new CellLayout.LayoutParams( x , y , 1 , 1 ) , false );
			items.add( info );
			images.add( info.getIconBitmap() );
		}
		enableHwLayersOnVisiblePages();
	}
	
	/**
	 * A helper to return the priority for loading of the specified widget page.
	 */
	private int getWidgetPageLoadPriority(
			int page )
	{
		// If we are snapping to another page, use that index as the target page index
		int toPage = mCurrentPage;
		if( mNextPage > -1 )
		{
			toPage = mNextPage;
		}
		// We use the distance from the target page as an initial guess of priority, but if there
		// are no pages of higher priority than the page specified, then bump up the priority of
		// the specified page.
		Iterator<AppsCustomizeAsyncTask> iter = mRunningTasks.iterator();
		int minPageDiff = Integer.MAX_VALUE;
		while( iter.hasNext() )
		{
			AppsCustomizeAsyncTask task = (AppsCustomizeAsyncTask)iter.next();
			minPageDiff = Math.abs( task.page - toPage );
		}
		int rawPageDiff = Math.abs( page - toPage );
		return rawPageDiff - Math.min( rawPageDiff , minPageDiff );
	}
	
	/**
	 * Return the appropriate thread priority for loading for a given page (we give the current
	 * page much higher priority)
	 */
	private int getThreadPriorityForPage(
			int page )
	{
		// TODO-APPS_CUSTOMIZE: detect number of cores and set thread priorities accordingly below
		int pageDiff = getWidgetPageLoadPriority( page );
		if( pageDiff <= 0 )
		{
			return Process.THREAD_PRIORITY_LESS_FAVORABLE;
		}
		else if( pageDiff <= 1 )
		{
			return Process.THREAD_PRIORITY_LOWEST;
		}
		else
		{
			return Process.THREAD_PRIORITY_LOWEST;
		}
	}
	
	private int getSleepForPage(
			int page )
	{
		int pageDiff = getWidgetPageLoadPriority( page );
		return Math.max( 0 , pageDiff * sPageSleepDelay );
	}
	
	/**
	 * Creates and executes a new AsyncTask to load a page of widget previews.
	 */
	private void prepareLoadWidgetPreviewsTask(
			int page ,
			ArrayList<Object> widgets ,
			int cellWidth ,
			int cellHeight ,
			int cellCountX )
	{
		// Prune all tasks that are no longer needed
		Iterator<AppsCustomizeAsyncTask> iter = mRunningTasks.iterator();
		while( iter.hasNext() )
		{
			AppsCustomizeAsyncTask task = (AppsCustomizeAsyncTask)iter.next();
			int taskPage = task.page;
			int lower = getAssociatedLowerPageBound( mCurrentPage );
			int upper = getAssociatedUpperPageBound( mCurrentPage );
			if( lower > upper )
			{
				if( taskPage > upper && taskPage < lower )
				{
					task.cancel( false );
					iter.remove();
				}
				else
				{
					task.setThreadPriority( getThreadPriorityForPage( taskPage ) );
				}
			}
			else
			{
				if( taskPage < lower || taskPage > upper )
				{
					task.cancel( false );
					iter.remove();
				}
				else
				{
					task.setThreadPriority( getThreadPriorityForPage( taskPage ) );
				}
			}
		}
		// We introduce a slight delay to order the loading of side pages so that we don't thrash
		final int sleepMs = getSleepForPage( page );
		AsyncTaskPageData pageData = new AsyncTaskPageData( page , widgets , cellWidth , cellHeight , new AsyncTaskCallback() {
			
			@Override
			public void run(
					AppsCustomizeAsyncTask task ,
					AsyncTaskPageData data )
			{
				try
				{
					try
					{
						Thread.sleep( sleepMs );
					}
					catch( Exception e )
					{
					}
					loadWidgetPreviewsInBackground( task , data );
				}
				finally
				{
					if( task.isCancelled() )
					{
						data.cleanup( true );
					}
				}
			}
		} , new AsyncTaskCallback() {
			
			@Override
			public void run(
					AppsCustomizeAsyncTask task ,
					AsyncTaskPageData data )
			{
				mRunningTasks.remove( task );
				if( task.isCancelled() )
					return;
				// do cleanup inside onSyncWidgetPageItems
				onSyncWidgetPageItems( data );
			}
		} , mWidgetPreviewLoader );
		// Ensure that the task is appropriately prioritized and runs in parallel
		AppsCustomizeAsyncTask t = new AppsCustomizeAsyncTask( page , AsyncTaskPageData.Type.LoadWidgetPreviewData );
		t.setThreadPriority( getThreadPriorityForPage( page ) );
		t.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR , pageData );
		mRunningTasks.add( t );
	}
	
	/*
	 * Widgets PagedView implementation
	 */
	private void setupPageWidgets(
			PagedViewGridLayout layout )
	{
		// Note: We force a measure here to get around the fact that when we do layout calculations
		// immediately after syncing, we don't have a proper width.
		int widthSpec = MeasureSpec.makeMeasureSpec( mContentWidthWidgets , MeasureSpec.AT_MOST );
		int heightSpec = MeasureSpec.makeMeasureSpec( mContentHeightWidgets , MeasureSpec.AT_MOST );
		layout.setMinimumWidth( getPageContentWidth() );
		layout.measure( widthSpec , heightSpec );
	}
	
	public void syncWidgetPageItems(
			final int page ,
			final boolean immediate )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "cyk:bug_0014477" , StringUtils.concat( " syncWidgetPageItems start page: " , page , " immediate: " , immediate ) );
		int numItemsPerPage = mWidgetCountX * mWidgetCountY;
		// Calculate the dimensions of each cell we are giving to each widget
		final ArrayList<Object> items = new ArrayList<Object>();
		int contentWidth = mContentWidthWidgets;
		final int cellWidth = ( ( contentWidth - mPageLayoutPaddingLeft - mPageLayoutPaddingRight - ( ( mWidgetCountX - 1 ) * mWidgetWidthGap ) ) / mWidgetCountX );
		int contentHeight = mContentHeightWidgets;
		final int cellHeight = ( ( contentHeight - mPageLayoutPaddingTop - mPageLayoutPaddingBottom - ( ( mWidgetCountY - 1 ) * mWidgetHeightGap ) ) / mWidgetCountY );
		// Prepare the set of widgets to load previews for in the background
		int offset = page * numItemsPerPage;
		for( int i = offset ; i < Math.min( offset + numItemsPerPage , mWidgets.size() ) ; ++i )
		{
			items.add( mWidgets.get( i ) );
		}
		// Prepopulate the pages with the other widget info, and fill in the previews later
		final PagedViewGridLayout layout = (PagedViewGridLayout)getPageAt( page );
		layout.setColumnCount( layout.getCellCountX() );
		for( int i = 0 ; i < items.size() ; ++i )
		{
			Object rawInfo = items.get( i );
			PendingAddItemInfo createItemInfo = null;
			PagedViewWidget widget = (PagedViewWidget)mLayoutInflater.inflate( R.layout.apps_customize_widget , layout , false );
			if( rawInfo instanceof AppWidgetProviderInfo )
			{
				// Fill in the widget information
				AppWidgetProviderInfo info = (AppWidgetProviderInfo)rawInfo;
				createItemInfo = new PendingAddWidgetInfo( info , null , null );
				// Determine the widget spans and min resize spans.
				int[] spanXY = Launcher.getSpanForWidget( mLauncher , info );
				createItemInfo.setSpanX( spanXY[0] );
				createItemInfo.setSpanY( spanXY[1] );
				int[] minSpanXY = Launcher.getMinSpanForWidget( mLauncher , info );
				createItemInfo.setMinSpanX( minSpanXY[0] );
				createItemInfo.setMinSpanY( minSpanXY[1] );
				widget.applyFromAppWidgetProviderInfo( info , -1 , spanXY , mWidgetPreviewLoader );
				widget.setTag( createItemInfo );
				widget.setShortPressListener( this );
			}
			else if( rawInfo instanceof ResolveInfo )
			{
				// Fill in the shortcuts information
				ResolveInfo info = (ResolveInfo)rawInfo;
				createItemInfo = new PendingAddShortcutInfo( info.activityInfo );
				createItemInfo.setItemType( LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT );
				createItemInfo.setComponentName( new ComponentName( info.activityInfo.packageName , info.activityInfo.name ) );
				widget.applyFromResolveInfo( mPackageManager , info , mWidgetPreviewLoader );
				widget.setTag( createItemInfo );
			}
			widget.setOnClickListener( this );
			widget.setOnLongClickListener( this );
			widget.setOnTouchListener( this );
			widget.setOnKeyListener( this );
			// Layout each widget
			int ix = i % mWidgetCountX;
			int iy = i / mWidgetCountX;
			// gaominghui@2016/12/14 ADD START 兼容android4.0
			GridLayout.LayoutParams lp;
			Spec rowSpec;
			if( Build.VERSION.SDK_INT >= 16 )
			{
				rowSpec = GridLayout.spec( iy , GridLayout.START );
			}
			else
			{
				rowSpec = GridLayout.spec( iy , GridLayout.LEFT );
			}
			lp = new GridLayout.LayoutParams( rowSpec , GridLayout.spec( ix , GridLayout.TOP ) );
			// gaominghui@2016/12/14 ADD END 兼容android4.0
			lp.width = cellWidth;
			lp.height = cellHeight;
			lp.setGravity( Gravity.TOP | Gravity.START );
			if( ix > 0 )
				lp.leftMargin = mWidgetWidthGap;
			if( iy > 0 )
				lp.topMargin = mWidgetHeightGap;
			layout.addView( widget , lp );
		}
		// wait until a call on onLayout to start loading, because
		// PagedViewWidget.getPreviewSize() will return 0 if it hasn't been laid out
		// TODO: can we do a measure/layout immediately?
		layout.setOnLayoutListener( new Runnable() {
			
			public void run()
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "cyk:bug_0014477" , StringUtils.concat( " syncWidgetPageItems Load the widget previews start: page: " , page , " immediate: " , immediate ) );
				// Load the widget previews
				int maxPreviewWidth = cellWidth;
				int maxPreviewHeight = cellHeight;
				if( layout.getChildCount() > 0 )
				{
					PagedViewWidget w = (PagedViewWidget)layout.getChildAt( 0 );
					int[] maxSize = w.getPreviewSize();
					maxPreviewWidth = maxSize[0];
					maxPreviewHeight = maxSize[1];
				}
				mWidgetPreviewLoader.setPreviewSize( maxPreviewWidth , maxPreviewHeight , mWidgetSpacingLayout );
				if( immediate )
				{
					AsyncTaskPageData data = new AsyncTaskPageData( page , items , maxPreviewWidth , maxPreviewHeight , null , null , mWidgetPreviewLoader );
					loadWidgetPreviewsInBackground( null , data );
					onSyncWidgetPageItems( data );
				}
				else
				{
					if( mInTransition )
					{
						mDeferredPrepareLoadWidgetPreviewsTasks.add( this );
					}
					else
					{
						prepareLoadWidgetPreviewsTask( page , items , maxPreviewWidth , maxPreviewHeight , mWidgetCountX );
					}
				}
				layout.setOnLayoutListener( null );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "cyk:bug_0014477" , StringUtils.concat( " syncWidgetPageItems Load the widget previews end: page: " , page , " immediate: " , immediate ) );
			}
		} );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "cyk:bug_0014477" , StringUtils.concat( " syncWidgetPageItems end page: " , page , " immediate: " , immediate ) );
	}
	
	private void loadWidgetPreviewsInBackground(
			AppsCustomizeAsyncTask task ,
			AsyncTaskPageData data )
	{
		// loadWidgetPreviewsInBackground can be called without a task to load a set of widget
		// previews synchronously
		if( task != null )
		{
			// Ensure that this task starts running at the correct priority
			task.syncThreadPriority();
		}
		// Load each of the widget/shortcut previews
		ArrayList<Object> items = data.items;
		ArrayList<Bitmap> images = data.generatedImages;
		int count = items.size();
		for( int i = 0 ; i < count ; ++i )
		{
			if( task != null )
			{
				// Ensure we haven't been cancelled yet
				if( task.isCancelled() )
					break;
				// Before work on each item, ensure that this task is running at the correct
				// priority
				task.syncThreadPriority();
			}
			images.add( mWidgetPreviewLoader.getPreview( items.get( i ) ) );
		}
	}
	
	private void onSyncWidgetPageItems(
			AsyncTaskPageData data )
	{
		if( mInTransition )
		{
			mDeferredSyncWidgetPageItems.add( data );
			return;
		}
		try
		{
			int page = data.page;
			PagedViewGridLayout layout = (PagedViewGridLayout)getPageAt( page );
			ArrayList<Object> items = data.items;
			int count = items.size();
			for( int i = 0 ; i < count ; ++i )
			{
				PagedViewWidget widget = (PagedViewWidget)layout.getChildAt( i );
				if( widget != null )
				{
					Bitmap preview = data.generatedImages.get( i );
					widget.applyPreview( new FastBitmapDrawable( preview ) , i );
				}
			}
			enableHwLayersOnVisiblePages();
			// Update all thread priorities
			Iterator<AppsCustomizeAsyncTask> iter = mRunningTasks.iterator();
			while( iter.hasNext() )
			{
				AppsCustomizeAsyncTask task = (AppsCustomizeAsyncTask)iter.next();
				int pageIndex = task.page;
				task.setThreadPriority( getThreadPriorityForPage( pageIndex ) );
			}
		}
		finally
		{
			data.cleanup( false );
		}
	}
	
	@Override
	public void syncPages()
	{
		disablePagedViewAnimations();
		removeAllViews();
		cancelAllTasks();
		Context context = getContext();
		if( mContentType == ContentType.Applications )
		{
			for( int i = 0 ; i < mNumAppsPages ; ++i )
			{
				AppsCustomizeCellLayout layout = new AppsCustomizeCellLayout( context );
				setupPageApps( layout );
				//zhujieping del，这里删除，设置其父的paddingtop
				// zhujieping@2015/04/17 ADD START,launcher中加入属性WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS，导致布局位置从状态栏开始，这里要paddingtop状态栏的高度
				//				layout.setPadding( layout.getPaddingLeft() , mLauncher.getStatusBarHeight( false ) , layout.getPaddingRight() , layout.getPaddingBottom() );
				// zhujieping@2015/04/17 ADD END
				addView( layout , new PagedView.LayoutParams( LayoutParams.MATCH_PARENT , LayoutParams.MATCH_PARENT ) );
			}
		}
		else if( mContentType == ContentType.Widgets )
		{
			for( int j = 0 ; j < mNumWidgetPages ; ++j )
			{
				PagedViewGridLayout layout = new PagedViewGridLayout( context , mWidgetCountX , mWidgetCountY );
				setupPageWidgets( layout );
				//zhujieping del，这里删除，设置其父的paddingtop
				// zhujieping@2015/04/17 ADD START,launcher中加入属性WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS，导致布局位置从状态栏开始，这里要paddingtop状态栏的高度
				//				layout.setPadding( layout.getPaddingLeft() , mLauncher.getStatusBarHeight( false ) , layout.getPaddingRight() , layout.getPaddingBottom() );
				// zhujieping@2015/04/17 ADD END
				addView( layout , new PagedView.LayoutParams( LayoutParams.MATCH_PARENT , LayoutParams.MATCH_PARENT ) );
			}
		}
		else
		{
			throw new RuntimeException( "Invalid ContentType" );
		}
		//zhujieping add start,隐藏模式时，隐藏最后一页的所有图标，mCurrentPage不对，导致不刷新，显示空白页
		if( mCurrentPage < 0 )
		{
			mCurrentPage = 0;
		}
		if( mCurrentPage >= getChildCount() )
		{
			mCurrentPage = getChildCount() - 1;
		}
		//zhujieping add end
		enablePagedViewAnimations();
	}
	
	@Override
	public void syncPageItems(
			int page ,
			boolean immediate )
	{
		if( mContentType == ContentType.Widgets )
		{
			syncWidgetPageItems( page , immediate );
		}
		else
		{
			syncAppsPageItems( page , immediate );
		}
	}
	
	// We want our pages to be z-ordered such that the further a page is to the left, the higher
	// it is in the z-order. This is important to insure touch events are handled correctly.
	public View getPageAt(
			int index )
	{
		return getChildAt( indexToPage( index ) );
	}
	
	@Override
	public int indexToPage(
			int index )
	{
		return getChildCount() - index - 1;
	}
	
	// In apps customize, we have a scrolling effect which emulates pulling cards off of a stack.
	//	@Override
	//	protected void screenScrolled(
	//			int screenCenter )
	//	{
	//		int curPage = ( this.getScrollX() + halfWidthScreen ) / widthScreen;
	//		xCurPageCenter = getScrollForPage( curPage ) + halfWidthScreen;
	//		isRtl = ( screenCenter - xCurPageCenter > 0 ) ? true : false;
	//		int nextPage = isRtl ? curPage + 1 : curPage - 1;
	//		View view = getPageAt( curPage );
	//		if( view instanceof PagedViewGridLayout )
	//		{
	//			curView = null;
	//		}
	//		else
	//		{
	//			curView = (IEffect)getPageAt( curPage );
	//		}
	//		if( curView != null )
	//		{
	//			View view2 = getPageAt( nextPage );
	//			if( view2 instanceof PagedViewGridLayout )
	//			{
	//				nextView = null;
	//			}
	//			else
	//			{
	//				this.nextView = (IEffect)getPageAt( nextPage );
	//				this.pageWidth = getScaledMeasuredWidth( curView );
	//				this.pageHeight = curView.getMeasuredHeight();
	//			}
	//		}
	//		if( curView != null )
	//		{
	//			curView.setCameraDistance( mDensity * CAMERA_DISTANCE );
	//			//计算滑动百分比:利用可见视窗中轴和当前页中轴差值
	//			int delta = screenCenter - xCurPageCenter;
	//			float percentageScroll = delta / ( widthScreen * 1.0f );
	//			//切页过程动画实现
	//			if( isRtl )
	//			{
	//				if( nextView != null )
	//				{
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , pageWidth , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , false );
	//					mCurentAnimInfo.getTransformationMatrix( nextView , percentageScroll - 1 , pageWidth , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , true );
	//				}
	//				else
	//				{
	//					percentageScroll = getNewPercentageScroll( percentageScroll );
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , false );
	//				}
	//			}
	//			else
	//			{
	//				if( nextView != null )
	//				{
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , pageWidth , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , false );
	//					mCurentAnimInfo.getTransformationMatrix( nextView , percentageScroll + 1 , pageWidth , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , true );
	//				}
	//				else
	//				{
	//					percentageScroll = getNewPercentageScroll( percentageScroll );
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , false );
	//				}
	//			}
	//		}
	//		//		final boolean isRtl = isLayoutRtl();
	//		//		super.screenScrolled( screenCenter );
	//		//		for( int i = 0 ; i < getChildCount() ; i++ )
	//		//		{
	//		//			View v = getPageAt( i );
	//		//			if( v != null )
	//		//			{
	//		//				float scrollProgress = getScrollProgress( screenCenter , v , i );
	//		//				float interpolatedProgress;
	//		//				float translationX;
	//		//				float maxScrollProgress = Math.max( 0 , scrollProgress );
	//		//				float minScrollProgress = Math.min( 0 , scrollProgress );
	//		//				if( isRtl )
	//		//				{
	//		//					translationX = maxScrollProgress * v.getMeasuredWidth();
	//		//					interpolatedProgress = mZInterpolator.getInterpolation( Math.abs( maxScrollProgress ) );
	//		//				}
	//		//				else
	//		//				{
	//		//					translationX = minScrollProgress * v.getMeasuredWidth();
	//		//					interpolatedProgress = mZInterpolator.getInterpolation( Math.abs( minScrollProgress ) );
	//		//				}
	//		//				float scale = ( 1 - interpolatedProgress ) + interpolatedProgress * TRANSITION_SCALE_FACTOR;
	//		//				float alpha;
	//		//				if( isRtl && ( scrollProgress > 0 ) )
	//		//				{
	//		//					alpha = mAlphaInterpolator.getInterpolation( 1 - Math.abs( maxScrollProgress ) );
	//		//				}
	//		//				else if( !isRtl && ( scrollProgress < 0 ) )
	//		//				{
	//		//					alpha = mAlphaInterpolator.getInterpolation( 1 - Math.abs( scrollProgress ) );
	//		//				}
	//		//				else
	//		//				{
	//		//					//  On large screens we need to fade the page as it nears its leftmost position
	//		//					alpha = mLeftScreenAlphaInterpolator.getInterpolation( 1 - scrollProgress );
	//		//				}
	//		//				v.setCameraDistance( mDensity * CAMERA_DISTANCE );
	//		//				int pageWidth = v.getMeasuredWidth();
	//		//				int pageHeight = v.getMeasuredHeight();
	//		//				if( PERFORM_OVERSCROLL_ROTATION )
	//		//				{
	//		//					float xPivot = isRtl ? 1f - TRANSITION_PIVOT : TRANSITION_PIVOT;
	//		//					boolean isOverscrollingFirstPage = isRtl ? scrollProgress > 0 : scrollProgress < 0;
	//		//					boolean isOverscrollingLastPage = isRtl ? scrollProgress < 0 : scrollProgress > 0;
	//		//					if( i == 0 && isOverscrollingFirstPage )
	//		//					{
	//		//						// Overscroll to the left
	//		//						v.setPivotX( xPivot * pageWidth );
	//		//						v.setRotationY( -TRANSITION_MAX_ROTATION * scrollProgress );
	//		//						scale = 1.0f;
	//		//						alpha = 1.0f;
	//		//						// On the first page, we don't want the page to have any lateral motion
	//		//						translationX = 0;
	//		//					}
	//		//					else if( i == getChildCount() - 1 && isOverscrollingLastPage )
	//		//					{
	//		//						// Overscroll to the right
	//		//						v.setPivotX( ( 1 - xPivot ) * pageWidth );
	//		//						v.setRotationY( -TRANSITION_MAX_ROTATION * scrollProgress );
	//		//						scale = 1.0f;
	//		//						alpha = 1.0f;
	//		//						// On the last page, we don't want the page to have any lateral motion.
	//		//						translationX = 0;
	//		//					}
	//		//					else
	//		//					{
	//		//						v.setPivotY( pageHeight / 2.0f );
	//		//						v.setPivotX( pageWidth / 2.0f );
	//		//						v.setRotationY( 0f );
	//		//					}
	//		//				}
	//		//				v.setTranslationX( translationX );
	//		//				v.setScaleX( scale );
	//		//				v.setScaleY( scale );
	//		//				v.setAlpha( alpha );
	//		//				// If the view has 0 alpha, we set it to be invisible so as to prevent
	//		//				// it from accepting touches
	//		//				if( alpha == 0 )
	//		//				{
	//		//					v.setVisibility( INVISIBLE );
	//		//				}
	//		//				else if( v.getVisibility() != VISIBLE )
	//		//				{
	//		//					v.setVisibility( VISIBLE );
	//		//				}
	//		//			}
	//		//		}
	//		//		enableHwLayersOnVisiblePages();
	//	}
	private void enableHwLayersOnVisiblePages()
	{
		final int screenCount = getChildCount();
		getVisiblePages( mTempVisiblePagesRange );
		int leftScreen = mTempVisiblePagesRange[0];
		int rightScreen = mTempVisiblePagesRange[1];
		int forceDrawScreen = -1;
		if( leftScreen == rightScreen )
		{
			// make sure we're caching at least two pages always
			if( rightScreen < screenCount - 1 )
			{
				rightScreen++;
				forceDrawScreen = rightScreen;
			}
			else if( leftScreen > 0 )
			{
				leftScreen--;
				forceDrawScreen = leftScreen;
			}
		}
		else
		{
			forceDrawScreen = leftScreen + 1;
		}
		for( int i = 0 ; i < screenCount ; i++ )
		{
			final View layout = (View)getPageAt( i );
			if( !( leftScreen <= i && i <= rightScreen && ( i == forceDrawScreen || shouldDrawChild( layout ) ) ) )
			{
				layout.setLayerType( LAYER_TYPE_NONE , null );
			}
		}
		for( int i = 0 ; i < screenCount ; i++ )
		{
			final View layout = (View)getPageAt( i );
			if( leftScreen <= i && i <= rightScreen && ( i == forceDrawScreen || shouldDrawChild( layout ) ) )
			{
				if( layout.getLayerType() != LAYER_TYPE_HARDWARE )
				{
					layout.setLayerType( LAYER_TYPE_HARDWARE , null );
				}
			}
		}
	}
	
	//	protected void overScroll(
	//			float amount )
	//	{
	//		acceleratedOverScroll( amount );
	//	}
	/**
	 * Used by the parent to get the content width to set the tab bar to
	 * @return
	 */
	public int getPageContentWidth()
	{
		//cheyingkun add start	//主菜单和小部件页面指示器、页面底边距分开配置(修正主菜单界面打开动态图标界面跳动问题)
		if( getContentType() == ContentType.Applications )
		{
			return mContentWidthApps;
		}
		else if( getContentType() == ContentType.Widgets )
		{
			return mContentWidthWidgets;
		}
		return 0;
		//cheyingkun add end
	}
	
	@Override
	protected void onPageEndMoving()
	{
		super.onPageEndMoving();
		mForceDrawAllChildrenNextFrame = true;
		// We reset the save index when we change pages so that it will be recalculated on next
		// rotation
		mSaveInstanceStateItemIndex = -1;
	}
	
	/*
	 * AllAppsView implementation
	 */
	public void setup(
			Launcher launcher ,
			DragController dragController )
	{
		mLauncher = launcher;
		mDragController = dragController;
		//zhujieping  start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		//zhujieping del start
		//		mPageIndicator = (PageIndicator)mLauncher.getmAppsCustomizeTabHost().findViewById( R.id.page_indicator );//cheyingkun add	//主菜单和小部件页面指示器、页面底边距分开配置
		//zhujieping del end
		//zhujieping add start
		ViewGroup indicatorParent = (ViewGroup)mLauncher.getmAppsCustomizeTabHost().findViewById( R.id.page_indicator );
		View normal = indicatorParent.findViewById( R.id.pageIndicatorNormal );
		View caret = indicatorParent.findViewById( R.id.pageIndicatorCaret );
		mPageIndicator = (PageIndicator)normal;
		indicatorParent.removeView( caret );
		//zhujieping add end
		//zhujieping  end
	}
	
	/**
	 * We should call thise method whenever the core data changes (mApps, mWidgets) so that we can
	 * appropriately determine when to invalidate the PagedView page data.  In cases where the data
	 * has yet to be set, we can requestLayout() and wait for onDataReady() to be called in the
	 * next onMeasure() pass, which will trigger an invalidatePageData() itself.
	 */
	private void invalidateOnDataChange()
	{
		if( !isDataReady() )
		{
			// The next layout pass will trigger data-ready if both widgets and apps are set, so
			// request a layout to trigger the page data when ready.
			requestLayout();
		}
		else
		{
			cancelAllTasks();
			invalidatePageData();
		}
	}
	
	public void setApps(
			ArrayList<AppInfo> list )
	{
		mApps = list;
		//zhujieping，单层模式无需排序，单mApp一定要赋值，否则小组件会不显示
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )//这个里面是对mapps进行排序，单层的没有必要
		{
			// zhujieping@2015/03/13 ADD START
			if(
			//
			LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
			//
			|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
			//
			)
			{
				if( mHideApps != null )//隐藏模式的单独存放在mHideApps中
					for( AppInfo mInfo : mApps )
					{
						mInfo.initHideIcon( mLauncher );
						if( mInfo.isHideIcon() )
						{
							mHideApps.add( mInfo );
						}
					}
				mApps.removeAll( mHideApps );
				sortApp( sortAppCheckId , false );
			}
			//xiatian add start	//优化桌面启动速度，去掉不必要的耗时操作（单层模式下，bindAllApplications方法中设置主菜单时，不对apps进行耗时的排序操作）。
			else if(
			//
			( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
			//
			&& ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_KITKAT )
			//
			)
			//xiatian add end
			{
				//cheyingkun add start	//主菜单排序和空位
				if( applistDefaultComponents != null && applistDefaultComponents.length > 0 )
				{
					//根据包类名排序list(默认配置的包类名放在前面,剩下的list按照字母排序)
					ArrayList<ComponentName> mComponentName = getDefaultComponentName( applistDefaultComponents );//默认配的包类名列表
					mApps = sortListByDefaultComponentName( mComponentName , list );//根据包类名对list进行排序
				}
				else
				//cheyingkun add end
				{
					//					Collections.sort( mApps , LauncherModel.getAppNameComparator() );//按“应用名称”排序
					// yangxiaoming start //优化开机速度（解决Collections.sort耗时）  2015/05/28
					// yangxiaoming delete add
					// 使用这种方法排序非常影响效率
					// Collections.sort( mApps , new ShortcutReplaceIconComparator() );//按“是否是主题的图标”进行排序
					// yangxiaoming delete end
					// yangxiaoming add start
					// 改用传统的List方法排序，效率提高明显
					mApps = LauncherModel.sortAppByDefaultIcon( list );
					// yangxiaoming add end
					// yangxiaoming 
				}
			}
			// zhujieping@2015/03/13 ADD END
			updatePageCountsAndInvalidateData();
		}
	}
	
	private void addAppsWithoutInvalidate(
			ArrayList<AppInfo> list )
	{
		// We add it in place, in alphabetical order
		int count = list.size();
		for( int i = 0 ; i < count ; ++i )
		{
			AppInfo info = list.get( i );
			//zhujieping add start
			if(
			//
			LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
			//
			|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
			//
			)
			{
				info.initHideIcon( mLauncher );
				if( mHideApps != null && info.isHideIcon() )
				{
					if( !isListContainsAppInfo( mHideApps , info ) )//mHideApps不包含info，才加入
						mHideApps.add( info );
				}
				else
				{
					if( !isListContainsAppInfo( mApps , info ) )//mApps不包含info，才加入，已包含则不能加入
						mApps.add( info );
				}
				continue;
			}
			//zhujieping add end
			//cheyingkun add start	//主菜单排序和空位
			if( installAppSortEnd )//主菜单界面安装应用是否排列在最后
			{
				mApps.add( info );
			}
			else
			//cheyingkun add end
			{
				int index = Collections.binarySearch( mApps , info , LauncherModel.getAppNameComparator() );
				if( index < 0 )
				{
					mApps.add( -( index + 1 ) , info );
				}
			}
		}
		if(
		//
		LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
		//
		&& (
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//zhujieping del start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
		//|| LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6
		//zhujieping del end
		)//S6的主菜单新安装的应用不用排序
			//
		)
		{
			sortApp( sortAppCheckId , false );
		}
	}
	
	private boolean isListContainsAppInfo(
			ArrayList<AppInfo> list ,
			AppInfo info )
	{
		for( AppInfo item : list )
		{
			if( item != null )
			{
				if( item.getComponentName().toString().equals( info.getComponentName().toString() ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public void addApps(
			ArrayList<AppInfo> list )
	{
		//xiatian add start	//fix bug：解决“在主菜单支持显示'应用市场'的前提下，卸载和安装带有'应用市场'的应用时，'应用市场'图标没有及时的消失和出现”的问题。
		if( mLauncher != null )
		{
			mLauncher.updateAppMarketIconWhenAppsChanged();
		}
		//xiatian add end
		//<phenix modify> liuhailin@2015-03-10 del begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//<phenix modify> liuhailin@2015-03-10 del end
		//{
		addAppsWithoutInvalidate( list );
		if( mContentType != ContentType.Widgets )//cheyingkun add	//解决“多次安装卸载同一个能产生小部件的应用，安装成功后，该应用的小部件几率性显示为机器人”的问题。【i_0011098】
		{
			updatePageCountsAndInvalidateData();
		}
		//}
	}
	
	private int findAppByComponent(
			List<AppInfo> list ,
			AppInfo item )
	{
		ComponentName removeComponent = item.getIntent().getComponent();
		int length = list.size();
		for( int i = 0 ; i < length ; ++i )
		{
			AppInfo info = list.get( i );
			if( info.getIntent().getComponent().equals( removeComponent ) )
			{
				return i;
			}
		}
		return -1;
	}
	
	private void removeAppsWithoutInvalidate(
			ArrayList<AppInfo> list )
	{
		// loop through all the apps and remove apps that have the same component
		int length = list.size();
		for( int i = 0 ; i < length ; ++i )
		{
			AppInfo info = list.get( i );
			int removeIndex = findAppByComponent( mApps , info );
			if( removeIndex > -1 )
			{
				mApps.remove( removeIndex );
			}
			if( mHideApps != null )
			{
				int ri = findAppByComponent( mHideApps , info );
				if( ri > -1 )
				{
					mHideApps.remove( ri );
				}
			}
		}
		if(
		//
		LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
		//
		&& (
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
		//
		)
		//
		)
		{
			sortApp( sortAppCheckId , false );
		}
	}
	
	public void removeApps(
			ArrayList<AppInfo> appInfos )
	{
		//xiatian add start	//fix bug：解决“在主菜单支持显示'应用市场'的前提下，卸载和安装带有'应用市场'的应用时，'应用市场'图标没有及时的消失和出现”的问题。
		if( mLauncher != null )
		{
			mLauncher.updateAppMarketIconWhenAppsChanged();
		}
		//xiatian add end
		//<phenix modify> liuhailin@2015-03-10 del begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//<phenix modify> liuhailin@2015-03-10 del end
		//{
		removeAppsWithoutInvalidate( appInfos );
		if( mContentType != ContentType.Widgets )//cheyingkun add	//解决“多次安装卸载同一个能产生小部件的应用，安装成功后，该应用的小部件几率性显示为机器人”的问题。【i_0011098】
		{
			updatePageCountsAndInvalidateData();
		}
		//}
	}
	
	public void updateApps(
			ArrayList<AppInfo> list )
	{
		//xiatian add start	//fix bug：解决“在主菜单支持显示'应用市场'的前提下，卸载和安装带有'应用市场'的应用时，'应用市场'图标没有及时的消失和出现”的问题。
		if( mLauncher != null )
		{
			mLauncher.updateAppMarketIconWhenAppsChanged();
		}
		//xiatian add end
		// We remove and re-add the updated applications list because it's properties may have
		// changed (ie. the title), and this will ensure that the items will be in their proper
		// place in the list.
		//<phenix modify> liuhailin@2015-03-10 del begin
		//if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//<phenix modify> liuhailin@2015-03-10 del end
		//{
		removeAppsWithoutInvalidate( list );
		addAppsWithoutInvalidate( list );
		if( mContentType != ContentType.Widgets )//cheyingkun add	//解决“多次安装卸载同一个能产生小部件的应用，安装成功后，该应用的小部件几率性显示为机器人”的问题。【i_0011098】
		{
			updatePageCountsAndInvalidateData();
		}
		//}
	}
	
	public void reset()
	{
		// If we have reset, then we should not continue to restore the previous state
		mSaveInstanceStateItemIndex = -1;
		AppsCustomizeTabHost tabHost = getTabHost();
		String tag = tabHost.getCurrentTabTag();
		if( tag != null )
		{
			if( !tag.equals( tabHost.getTabTagForContentType( ContentType.Applications ) ) )
			{
				tabHost.setCurrentTabFromContent( ContentType.Applications );
			}
		}
		if( mCurrentPage != 0 )
		{
			invalidatePageData( 0 );
		}
	}
	
	private AppsCustomizeTabHost getTabHost()
	{
		return (AppsCustomizeTabHost)mLauncher.findViewById( R.id.apps_customize_pane );
	}
	
	public void dumpState()
	{
		// TODO: Dump information related to current list of Applications, Widgets, etc.
		AppInfo.dumpApplicationInfoList( TAG , "mApps" , mApps );
		dumpAppWidgetProviderInfoList( TAG , "mWidgets" , mWidgets );
	}
	
	private void dumpAppWidgetProviderInfoList(
			String tag ,
			String label ,
			ArrayList<Object> list )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( tag , StringUtils.concat( label , " size=" , list.size() ) );
			for( Object i : list )
			{
				if( i instanceof AppWidgetProviderInfo )
				{
					AppWidgetProviderInfo info = (AppWidgetProviderInfo)i;
					Log.d( tag , StringUtils.concat(
							"label:" ,
							info.label ,
							"-previewImage:" ,
							info.previewImage ,
							"-resizeMode:" ,
							info.resizeMode ,
							"-configure:" ,
							info.configure.toString() ,
							"-initialLayout:" ,
							info.initialLayout ,
							"-minWidth;" ,
							info.minWidth ,
							"-minHeight:" ,
							info.minHeight ) );
				}
				else if( i instanceof ResolveInfo )
				{
					ResolveInfo info = (ResolveInfo)i;
					Log.d( tag , StringUtils.concat( "label:" , info.loadLabel( mPackageManager ) , "-icon:" , info.icon ) );
				}
			}
		}
	}
	
	public void surrender()
	{
		// TODO: If we are in the middle of any process (ie. for holographic outlines, etc) we
		// should stop this now.
		// Stop all background tasks
		cancelAllTasks();
	}
	
	@Override
	public void iconPressed(
			PagedViewIcon icon )
	{
		// Reset the previously pressed icon and store a reference to the pressed icon so that
		// we can reset it on return to Launcher (in Launcher.onResume())
		if( mPressedIcon != null )
		{
			mPressedIcon.resetDrawableState();
		}
		mPressedIcon = icon;
	}
	
	public void resetDrawableState()
	{
		if( mPressedIcon != null )
		{
			mPressedIcon.resetDrawableState();
			mPressedIcon = null;
		}
	}
	
	/*
	 * We load an extra page on each side to prevent flashes from scrolling and loading of the
	 * widget previews in the background with the AsyncTasks.
	 */
	final static int sLookBehindPageCount = 2;
	final static int sLookAheadPageCount = 2;
	
	protected int getAssociatedLowerPageBound(
			int page )
	{
		final int count = getChildCount();
		if( isLoop() )
		{
			if( count < sLookBehindPageCount + sLookAheadPageCount + 1 )
			{
				return 0;
			}
			if( page - sLookBehindPageCount >= 0 )
			{
				return page - sLookBehindPageCount;
			}
			else
			{
				return Math.max( page - sLookBehindPageCount + count , 0 );
			}
		}
		int windowSize = Math.min( count , sLookBehindPageCount + sLookAheadPageCount + 1 );
		int windowMinIndex = Math.max( Math.min( page - sLookBehindPageCount , count - windowSize ) , 0 );
		return windowMinIndex;
	}
	
	protected int getAssociatedUpperPageBound(
			int page )
	{
		final int count = getChildCount();
		if( isLoop() )
		{
			if( count < sLookBehindPageCount + sLookAheadPageCount + 1 )
			{
				return count - 1;
			}
			return ( page + sLookAheadPageCount ) % count;
		}
		int windowSize = Math.min( count , sLookBehindPageCount + sLookAheadPageCount + 1 );
		int windowMaxIndex = Math.min( Math.max( page + sLookAheadPageCount , windowSize - 1 ) , count - 1 );
		return windowMaxIndex;
	}
	
	//	public void initAnimationStyle()
	//	{
	//		int num = 0;
	//		if( mLauncher.getSelect_efffects_workspace() == EffectFactory.getAllEffects().size() )
	//		{//random
	//			mCurentAnimInfo = EffectFactory.getEffect( num = new Random().nextInt( EffectFactory.getAllEffects().size() ) + 1 );
	//		}
	//		else
	//		{
	//			mCurentAnimInfo = EffectFactory.getEffect( mLauncher.getSelect_efffects_workspace() + 1 );
	//		}
	//	}
	//	
	public void restoreAppsCustomizePagedView()
	{
		for( int i = 0 ; i < this.getChildCount() ; i++ )
		{
			View page = null;
			if( ( page = this.getChildAt( i ) ) != null )
			{
				if( page instanceof AppsCustomizeCellLayout )
				{
					AppsCustomizeCellLayout pagedViewCellLayout = (AppsCustomizeCellLayout)page;
					pagedViewCellLayout.setTranslationX( 0f );
					pagedViewCellLayout.setTranslationY( 0f );
					pagedViewCellLayout.setRotation( 0f );
					pagedViewCellLayout.setRotationX( 0f );
					pagedViewCellLayout.setRotationY( 0f );
					pagedViewCellLayout.setAlpha( 1f );
					pagedViewCellLayout.setScaleX( 1f );
					pagedViewCellLayout.setScaleY( 1f );
					ShortcutAndWidgetContainer pagedViewCellLayoutChildren = null;
					//					, "------" + pagedViewCellLayout.getChildAt( 0 ).getClass().getName() );
					if( pagedViewCellLayout.getChildAt( 0 ) != null )
					{
						pagedViewCellLayoutChildren = (ShortcutAndWidgetContainer)pagedViewCellLayout.getChildAt( 0 );
					}
					for( int j = 0 ; j < pagedViewCellLayoutChildren.getChildCount() ; j++ )
					{
						View view = null;
						if( ( view = pagedViewCellLayoutChildren.getChildAt( j ) ) == null )
						{
							continue;
						}
						view.setTranslationX( 0f );
						view.setTranslationY( 0f );
						view.setRotation( 0f );
						view.setRotationX( 0f );
						view.setRotationY( 0f );
						view.setAlpha( 1f );
						view.setScaleX( 1f );
						view.setScaleY( 1f );
					}
				}
				else if( page instanceof PagedViewGridLayout )
				{
					PagedViewGridLayout pagedViewGridLayout = (PagedViewGridLayout)page;
					pagedViewGridLayout.setTranslationX( 0f );
					pagedViewGridLayout.setTranslationY( 0f );
					pagedViewGridLayout.setRotation( 0f );
					pagedViewGridLayout.setRotationX( 0f );
					pagedViewGridLayout.setRotationY( 0f );
					pagedViewGridLayout.setAlpha( 1f );
					pagedViewGridLayout.setScaleX( 1f );
					pagedViewGridLayout.setScaleY( 1f );
					PagedViewWidget pagedViewWidget = null;
					for( int j = 0 ; j < pagedViewGridLayout.getChildCount() ; j++ )
					{
						if( pagedViewGridLayout.getChildAt( j ) != null )
						{
							pagedViewWidget = (PagedViewWidget)pagedViewGridLayout.getChildAt( j );
							pagedViewWidget.setTranslationX( 0f );
							pagedViewWidget.setTranslationY( 0f );
							pagedViewWidget.setRotation( 0f );
							pagedViewWidget.setRotationX( 0f );
							pagedViewWidget.setRotationY( 0f );
							pagedViewWidget.setAlpha( 1f );
							pagedViewWidget.setScaleX( 1f );
							pagedViewWidget.setScaleY( 1f );
						}
					}
				}
				else
				{
					continue;
				}
			}
			else
			{
				continue;
			}
		}
	}
	
	@Override
	protected void restView()
	{
		restoreAppsCustomizePagedView();
	}
	
	// zhujieping@2015/03/13 ADD START
	//图标进行替换的排在前面
	private class ShortcutReplaceIconComparator implements Comparator<AppInfo>
	{
		
		@Override
		public int compare(
				AppInfo lhs ,
				AppInfo rhs )
		{
			if( !( lhs instanceof AppInfo && rhs instanceof AppInfo ) )
			{
				return 0;
			}
			// TODO Auto-generated method stub
			boolean lhasReplace = LauncherIconBaseConfig.hasReplaceIcon( lhs.getComponentName().getPackageName() , lhs.getComponentName().getClassName() );
			boolean rhasReplace = LauncherIconBaseConfig.hasReplaceIcon( rhs.getComponentName().getPackageName() , rhs.getComponentName().getClassName() );
			if( lhasReplace && !rhasReplace )
			{
				return -1;
			}
			else if( !lhasReplace && rhasReplace )
			{
				return 1;
			}
			return 0;
		}
	}
	
	// zhujieping@2015/03/13 ADD END
	//cheyingkun add start	//主菜单排序和空位
	/**
	 * 把读到的字符串数组转换成ComponentName
	 * @param applistDefaultComponents
	 * @return
	 */
	private ArrayList<ComponentName> getDefaultComponentName(
			String[] applistDefaultComponents )
	{
		ArrayList<ComponentName> mComponentName = new ArrayList<ComponentName>();
		if( applistDefaultComponents != null )
		{
			for( String componentStr : applistDefaultComponents )
			{
				if( componentStr != null && componentStr.length() > 0// 
						&& !"none".equals( componentStr ) )
				{
					String[] split = componentStr.split( "/" );
					if( split.length == 2 )
					{
						mComponentName.add( new ComponentName( split[0] , split[1] ) );
					}
				}
			}
		}
		return mComponentName;
	}
	
	/**
	 * 根据mComponentName对list进行排序,把mComponentName中的应用放到前面
	 * @param mComponentName 
	 * @param list
	 * @return
	 */
	private ArrayList<AppInfo> sortListByDefaultComponentName(
			ArrayList<ComponentName> mComponentName ,
			ArrayList<AppInfo> list )
	{
		if( mComponentName != null && list != null )
		{
			HashMap<ComponentName , AppInfo> mMap = new HashMap<ComponentName , AppInfo>();
			for( AppInfo appInfo : list )//把list中的信息写入map中,key为ComponentName  value为appInfo
			{
				mMap.put( appInfo.getComponentName() , appInfo );
			}
			list.clear();//清空list
			Set<ComponentName> keySet = mMap.keySet();
			for( ComponentName componentName : mComponentName )//循环mComponentName
			{
				if( keySet.contains( componentName ) && !list.contains( componentName ) )//如果componentName在map中,并且不在list中
				{
					list.add( mMap.get( componentName ) );//list add
					mMap.remove( componentName );//map remove
				}
			}
			//把map剩余的值加到list中
			Collection<AppInfo> values = mMap.values();
			ArrayList<AppInfo> list2 = new ArrayList<AppInfo>( values );
			Collections.sort( list2 , LauncherModel.getAppNameComparator() );
			list.addAll( list2 );
		}
		return list;
	}
	
	/** 
	 * 初始化辉烨-主菜单配置和预留空位的相关数据
	 */
	private void initAppListDefaultData()
	{
		installAppSortEnd = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_new_item_show_in_the_last_of_applist );
		applistDefaultComponents = LauncherDefaultConfig.getStringArray( R.array.applist_default_components );
		if( applistDefaultComponents.length > 0 )
		{
			int numCells = mCellCountX * mCellCountY;//每页有多少个图标
			int num = (int)Math.ceil( ( (float)applistDefaultComponents.length ) / ( mCellCountX * mCellCountY ) );//配置的包类名共多少页
			applistEveryPageVacantNum = new int[num];
			for( int i : applistEveryPageVacantNum )//每页空格数复制为0
			{
				applistEveryPageVacantNum[i] = 0;
			}
			for( int i = 0 ; i < applistDefaultComponents.length ; i++ )//根据包类名计算每页的空格数
			{
				if( applistDefaultComponents[i] == null || applistDefaultComponents[i].length() == 0// 
						|| "none".equals( applistDefaultComponents[i] ) )
				{
					applistEveryPageVacantNum[i / numCells]++;
				}
			}
		}
	}
	//cheyingkun add end
	;
	
	//WangLei add start //bug:c_0003047 //主菜单上未接来电和未读短信更新不及时
	public void updateUnreadNumberByComponent(
			ComponentName componentName ,
			final int unreadNum )
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		if( childrenLayouts == null || childrenLayouts.size() == 0 )
		{
			return;
		}
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				final View childView = layout.getChildAt( i );
				Object tag = childView.getTag();
				if( tag instanceof AppInfo )
				{
					AppInfo appInfo = (AppInfo)tag;
					final Intent intent = appInfo.getIntent();
					final ComponentName name = intent.getComponent();
					if( Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
					{
						if( componentName.equals( name ) && childView instanceof PagedViewIcon )
						{
							PagedViewIcon pagedViewIcon = (PagedViewIcon)childView;
							pagedViewIcon.applyFromApplicationInfo( appInfo , false , this );
						}
					}
				}
			}
		}
	}
	
	ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainers()
	{
		if( mContentType == ContentType.Widgets )
		{
			return null;
		}
		ArrayList<ShortcutAndWidgetContainer> childLayouts = new ArrayList<ShortcutAndWidgetContainer>();
		int screenCount = getChildCount();
		for( int i = 0 ; i < screenCount ; i++ )
		{
			childLayouts.add( ( (CellLayout)getChildAt( i ) ).getShortcutsAndWidgets() );
		}
		return childLayouts;
	}
	//WangLei add end
	;
	
	// zhangjin@2015/08/31 ADD START
	public void updateIconHouse(
			ComponentName componentName )
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		if( childrenLayouts == null || childrenLayouts.size() == 0 )
		{
			for( int i = 0 ; i < mApps.size() ; i++ )
			{
				AppInfo appInfo;
				if( sortArray != null && mApps.size() == sortArray.length )
				{
					appInfo = mApps.get( sortArray[i] );
				}
				else
				{
					appInfo = mApps.get( i );
				}
				if( appInfo.getComponentName().equals( componentName ) )
				{
					appInfo.updateIcon( mIconCache );
					//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
					appInfo.setIconBitmapBackup( Bitmap.createBitmap( appInfo.getIconBitmap() ) );
					IconHouseManager.getInstance().updateFavoritesIconHouseApps( componentName , appInfo.getIconBitmapBackup() );
					//cheyingkun add end
				}
			}
			return;
		}
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				final View childView = layout.getChildAt( i );
				Object tag = childView.getTag();
				if( tag instanceof AppInfo )
				{
					AppInfo appInfo = (AppInfo)tag;
					final Intent intent = appInfo.getIntent();
					final ComponentName name = intent.getComponent();
					if( Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
					{
						if( componentName.equals( name ) && childView instanceof PagedViewIcon )
						{
							PagedViewIcon pagedViewIcon = (PagedViewIcon)childView;
							appInfo.updateIcon( mIconCache );
							pagedViewIcon.updateIcon( appInfo );
							//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
							appInfo.setIconBitmapBackup( Bitmap.createBitmap( appInfo.getIconBitmap() ) );
							IconHouseManager.getInstance().updateFavoritesIconHouseApps( componentName , appInfo.getIconBitmapBackup() );
							//cheyingkun add end
						}
					}
				}
			}
		}
	}
	
	// zhangjin@2015/08/31 ADD END
	ArrayList<ShortcutAndWidgetContainer> getVisibleShortcutAndWidgetContainers()
	{
		if( mContentType == ContentType.Widgets || this.getChildCount() == 0 )
		{
			return null;
		}
		ArrayList<ShortcutAndWidgetContainer> childLayouts = new ArrayList<ShortcutAndWidgetContainer>();
		int screen = this.getCurrentPage();
		childLayouts.add( ( (CellLayout)getPageAt( screen ) ).getShortcutsAndWidgets() );
		return childLayouts;
	}
	
	public boolean isCmpVisible(
			final ComponentName componentName )
	{
		boolean canUpdate = false;
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getVisibleShortcutAndWidgetContainers();
		if( childrenLayouts == null || childrenLayouts.size() == 0 )
		{
			return canUpdate;
		}
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				final View childView = layout.getChildAt( i );
				Object tag = childView.getTag();
				if( tag instanceof AppInfo )
				{
					AppInfo appInfo = (AppInfo)tag;
					final Intent intent = appInfo.getIntent();
					final ComponentName name = intent.getComponent();
					if( Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
					{
						if( componentName.equals( name ) )
						{
							canUpdate = true;
							return canUpdate;
						}
					}
				}
			}
		}
		return canUpdate;
	}
	
	//cheyingkun add start	//主菜单和小部件页面指示器、页面底边距分开配置
	private void changeMarginBottom()
	{
		FrameLayout.LayoutParams mPageIndicatorLP = (FrameLayout.LayoutParams)mPageIndicator.getLayoutParams();
		FrameLayout.LayoutParams customizePagedViewLp = (FrameLayout.LayoutParams)this.getLayoutParams();
		if( getContentType() == ContentType.Applications )
		{
			mPageIndicatorLP.bottomMargin = (int)appsPageIndicatorMarginBottom;
			customizePagedViewLp.bottomMargin = (int)appsPageIndicatorOffsetBottom;
			customizePagedViewLp.topMargin = (int)appsPageIndicatorOffsetTop;
		}
		else if( getContentType() == ContentType.Widgets )
		{
			mPageIndicatorLP.bottomMargin = (int)widgetsPageIndicatorMarginBottom;
			customizePagedViewLp.bottomMargin = (int)widgetsPageIndicatorOffset;
			customizePagedViewLp.topMargin = 0;
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//主菜单和小部件页面指示器、页面底边距分开配置(修正小部件界面的宽高)
	/**计算widget宽高的方法*/
	private void measureWidgetCellWidthAndHeight()
	{
		int mContentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int mContentHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
		int offset = (int)( appsPageIndicatorOffsetBottom + appsPageIndicatorOffsetTop - widgetsPageIndicatorOffset );
		if( getContentType() == ContentType.Applications )
		{
			mContentWidthApps = mContentWidth;
			mContentHeightApps = mContentHeight;
			//
			mContentWidthWidgets = mContentWidth;
			mContentHeightWidgets = mContentHeight + offset;
		}
		else if( getContentType() == ContentType.Widgets )
		{
			mContentWidthWidgets = mContentWidth;
			mContentHeightWidgets = mContentHeight;
			//
			mContentWidthApps = mContentWidth;
			mContentHeightApps = mContentHeight - offset;
		}
		else
		{
			return;
		}
		int widthSpec = MeasureSpec.makeMeasureSpec( mContentWidth , MeasureSpec.AT_MOST );
		int heightSpec = MeasureSpec.makeMeasureSpec( mContentHeight , MeasureSpec.AT_MOST );
		mWidgetSpacingLayout.measure( widthSpec , heightSpec );
	}
	
	//cheyingkun add end
	//zhujieping add start	//小组件适配动态导航栏	
	private void onUIChanged(
			int height )
	{
		int offset = (int)( appsPageIndicatorOffsetBottom + appsPageIndicatorOffsetTop - widgetsPageIndicatorOffset );
		int mContentHeight = (int)( height - getPaddingTop() - getPaddingBottom() - widgetsPageIndicatorOffset );
		mContentHeightWidgets = mContentHeight;
		mContentHeightApps = mContentHeight - offset;
		int contentWidth = mContentWidthWidgets;
		final int cellWidth = ( ( contentWidth - mPageLayoutPaddingLeft - mPageLayoutPaddingRight - ( ( mWidgetCountX - 1 ) * mWidgetWidthGap ) ) / mWidgetCountX );
		int contentHeight = mContentHeightWidgets;
		final int cellHeight = ( ( contentHeight - mPageLayoutPaddingTop - mPageLayoutPaddingBottom - ( ( mWidgetCountY - 1 ) * mWidgetHeightGap ) ) / mWidgetCountY );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "mContentHeightWidgets:" , mContentHeightWidgets , "-cellHeight:" , cellHeight ) );
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			View child = getChildAt( i );
			if( child instanceof PagedViewGridLayout )
			{
				PagedViewGridLayout layout = (PagedViewGridLayout)child;
				setupPageWidgets( layout );
				for( int j = 0 ; j < layout.getChildCount() ; j++ )
				{
					if( layout.getChildAt( j ) instanceof PagedViewWidget )
					{
						PagedViewWidget widget = (PagedViewWidget)layout.getChildAt( j );
						if( widget.getLayoutParams() != null )
						{
							int ix = j % mWidgetCountX;
							int iy = j / mWidgetCountX;
							GridLayout.LayoutParams lp = (GridLayout.LayoutParams)widget.getLayoutParams();
							lp.width = cellWidth;
							lp.height = cellHeight;
							lp.setGravity( Gravity.TOP | Gravity.START );
							if( ix > 0 )
								lp.leftMargin = mWidgetWidthGap;
							if( iy > 0 )
								lp.topMargin = mWidgetHeightGap;
						}
					}
				}
			}
		}
	}
	
	//zhujieping add end
	//zhujieping add start
	private class OrderDialog
	{
		
		private RadioGroup mRadioGrop;
		private RadioButton mRadioFactory;
		private RadioButton mRadioFrequency;
		private RadioButton mRadioInstall;
		private RadioButton mRadioName;
		private Button mCancelButton;
		
		Dialog createDialog()
		{
			final Dialog dialog = new Dialog( getContext() , R.style.Theme_buttom_dialog );
			dialog.getWindow();
			dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
			//zhujieping add start //拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
			if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_SORT_APP )
			{
				dialog.setContentView( R.layout.app_menu_sortapp_dialog_lxt );
			}
			else
			//zhujieping add end
			{
				dialog.setContentView( R.layout.app_menu_sortapp_dialog );
			}
			dialog.setCancelable( true );
			mRadioGrop = (RadioGroup)dialog.findViewById( R.id.RadioGroup );
			mCancelButton = (Button)dialog.findViewById( R.id.btnDialogCancel );
			mRadioFactory = (RadioButton)mRadioGrop.findViewById( R.id.RadioFactory );
			mRadioFrequency = (RadioButton)mRadioGrop.findViewById( R.id.RadioFrequency );
			mRadioInstall = (RadioButton)mRadioGrop.findViewById( R.id.RadioInstall );
			mRadioName = (RadioButton)mRadioGrop.findViewById( R.id.RadioName );
			dialog.setOnShowListener( new DialogInterface.OnShowListener() {
				
				public void onShow(
						DialogInterface dialog )
				{
					switch( sortAppCheckId )
					{
						case SORT_NAME:
							mRadioGrop.check( R.id.RadioName );
							break;
						case SORT_INSTALL:
							mRadioGrop.check( R.id.RadioInstall );
							break;
						case SORT_USE:
							mRadioGrop.check( R.id.RadioFrequency );
							break;
						case SORT_FACTORY:
							mRadioGrop.check( R.id.RadioFactory );
							break;
					}
				}
			} );
			mCancelButton.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					dialog.dismiss();
				}
			} );
			mRadioFactory.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					//if( sortAppCheckId != SORT_FACTORY )//zhujieping del,当桌面app有变化时，点击当前选中的，应该要重新排序，这边不需要进行判断
					{
						sortApp( SORT_FACTORY , true );
					}
					dialog.dismiss();
				}
			} );
			mRadioFrequency.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					//if( sortAppCheckId != SORT_USE )//zhujieping del,当桌面app有变化时，点击当前选中的，应该要重新排序，这边不需要进行判断
					{
						sortApp( SORT_USE , true );
					}
					dialog.dismiss();
				}
			} );
			mRadioInstall.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					//if( sortAppCheckId != SORT_INSTALL )//zhujieping del,当桌面app有变化时，点击当前选中的，应该要重新排序，这边不需要进行判断
					{
						sortApp( SORT_INSTALL , true );
					}
					dialog.dismiss();
				}
			} );
			mRadioName.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					//if( sortAppCheckId != SORT_NAME )////zhujieping del,当桌面app有变化时，点击当前选中的，应该要重新排序，这边不需要进行判断
					{
						sortApp( SORT_NAME , true );
					}
					dialog.dismiss();
				}
			} );
			return dialog;
		}
	}
	
	public void sortApp(
			int checkId ,
			boolean refresh )
	{
		if( checkId != sortAppCheckId )
		{
			sortAppCheckId = checkId;
			PreferenceManager.getDefaultSharedPreferences( getContext() ).edit().putInt( "sort_app" , sortAppCheckId ).commit();
			//zhujieping add start //拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
			AppsCustomizeTabHost host = (AppsCustomizeTabHost)getTabHost();
			host.notifySortTypeChanged( sortAppCheckId );
			//zhujieping add end
		}
		sortArray = new int[mApps.size()];
		switch( sortAppCheckId )
		{
			case SORT_NAME:
			{
				String[] nameKey = new String[mApps.size()];
				for( int i = 0 ; i < mApps.size() ; i++ )
				{
					nameKey[i] = mApps.get( i ).getTitle().replaceAll( " " , "" ).replaceAll( " " , "" );// 注意！！两个空格不一样
				}
				cut.sortByAlpha( 1 , nameKey , sortArray );
				break;
			}
			case SORT_INSTALL:
				int[] installKey = new int[mApps.size()];
				String[] nameKey = new String[mApps.size()];
				for( int i = 0 ; i < mApps.size() ; i++ )
				{
					installKey[i] = (int)( mApps.get( i ).getLastUpdateTime() / 1000 );
					nameKey[i] = mApps.get( i ).getTitle().replaceAll( " " , "" ).replaceAll( " " , "" );// 注意！！两个空格不一�?
				}
				if( LauncherDefaultConfig.LAST_INSTALLED_APP_SORT_ON_HEAD )
				{
					cut.sort( 0 , installKey , sortArray );
				}
				else
				{
					cut.sort( 1 , installKey , sortArray );
				}
				cut.sortNameAfterInstall( nameKey , installKey , sortArray );
				break;
			case SORT_USE:
				int[] useKey = new int[mApps.size()];
				for( int i = 0 ; i < mApps.size() ; i++ )
				{
					useKey[i] = (int)( mApps.get( i ).getUseFrequency( getContext() ) );
				}
				cut.sort( 0 , useKey , sortArray );
				break;
			case SORT_FACTORY:
				break;
		}
		if( refresh )
		{
			updatePageCountsAndInvalidateData();
		}
	}
	
	public void showSortDialog()
	{
		if( sortDialog == null )
		{
			sortDialog = new OrderDialog().createDialog();
		}
		sortDialog.show();
	}
	
	public void hideSortDailog()
	{
		if( sortDialog != null && sortDialog.isShowing() )
		{
			sortDialog.dismiss();
		}
	}
	
	public void setAppsMode(
			int mode )
	{
		if( mode == mApps_mode )
		{
			return;
		}
		int preMode = mApps_mode;
		mApps_mode = mode;
		if( mode == HIDE_MODE )
		{
			setAppsHideMode( true );
		}
		else if( preMode == HIDE_MODE )
		{
			setAppsHideMode( false );
		}
		else if( mode == EDIT_MODE )
		{
			if( mLauncher.getSearchDropTargetBar() != null )
			{
				if( mLauncher.getSearchDropTargetBar().getParent() != null && mLauncher.getSearchDropTargetBar().getParent() instanceof ViewGroup )
				{
					ViewGroup parent = (ViewGroup)mLauncher.getSearchDropTargetBar().getParent();
					preSearchBarIndex = parent.indexOfChild( mLauncher.getSearchDropTargetBar() );
				}
				if( !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
				{
					FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams)mLauncher.getSearchDropTargetBar().getLayoutParams();
					preSearchBarTopMargin = params.topMargin;
					params.setMargins( 0 , mLauncher.getStatusBarHeight( true ) , 0 , 0 );
				}
				preSearchBarVisiblity = mLauncher.getSearchDropTargetBar().getVisibility();
				mLauncher.getSearchDropTargetBar().setVisibility( View.VISIBLE );
				mLauncher.getSearchDropTargetBar().bringToFront();
				mLauncher.getSearchDropTargetBar().setDropTargetListener( this );
			}
			mDragController.addDropTargetAtIndex( this , 0 );
		}
		else if( preMode == EDIT_MODE )
		{
			if( mLauncher.getSearchDropTargetBar() != null )
			{
				if( preSearchBarVisiblity != -1 )
					mLauncher.getSearchDropTargetBar().setVisibility( preSearchBarVisiblity );
				preSearchBarVisiblity = -1;
				if( !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && preSearchBarTopMargin != -1 )
				{
					FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams)mLauncher.getSearchDropTargetBar().getLayoutParams();
					params.setMargins( 0 , preSearchBarTopMargin , 0 , 0 );
				}
				preSearchBarTopMargin = -1;
				if( mLauncher.getSearchDropTargetBar().getParent() != null && mLauncher.getSearchDropTargetBar().getParent() instanceof ViewGroup )
				{
					ViewGroup parent = (ViewGroup)mLauncher.getSearchDropTargetBar().getParent();
					if( preSearchBarIndex >= 0 && preSearchBarIndex < parent.getChildCount() )
					{
						parent.removeView( mLauncher.getSearchDropTargetBar() );
						parent.addView( mLauncher.getSearchDropTargetBar() , preSearchBarIndex );
					}
				}
				preSearchBarIndex = -1;
			}
			mDragController.removeDropTarget( this );
		}
		//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
		else if( mode == UNINSTALL_MODE )
		{
			setAppsUninstallMode( true );
		}
		else if( preMode == UNINSTALL_MODE )
		{
			setAppsUninstallMode( false );
		}
		//zhujieping add end
	}
	
	private void setAppsHideMode(
			boolean isHideMode )
	{
		if( isHideMode )
		{
			if( mHideApps != null && mHideApps.size() > 0 )//这段是把所有已经隐藏的显示出来
			{
				mApps.addAll( mHideApps );
				sortApp( sortAppCheckId , true );
				mHideApps.clear();
			}
		}
		for( int i = 0 ; i < this.getChildCount() ; i++ )
		{
			View page = null;
			if( ( page = this.getChildAt( i ) ) != null )
			{
				if( page instanceof AppsCustomizeCellLayout )
				{
					AppsCustomizeCellLayout pagedViewCellLayout = (AppsCustomizeCellLayout)page;
					ShortcutAndWidgetContainer pagedViewCellLayoutChildren = null;
					if( pagedViewCellLayout.getChildAt( 0 ) != null )
					{
						pagedViewCellLayoutChildren = (ShortcutAndWidgetContainer)pagedViewCellLayout.getChildAt( 0 );
					}
					for( int j = 0 ; j < pagedViewCellLayoutChildren.getChildCount() ; j++ )
					{
						View view = null;
						if( ( view = pagedViewCellLayoutChildren.getChildAt( j ) ) == null )
						{
							continue;
						}
						if( view instanceof PagedViewIcon )
						{
							PagedViewIcon icon = (PagedViewIcon)view;
							icon.setCheckboxShow( isHideMode );
						}
					}
				}
			}
		}
		if( !isHideMode )
		{
			if( mHideApps != null )//这段是将所有打钩要隐藏掉的隐藏
			{
				for( AppInfo mInfo : mApps )
				{
					mInfo.initHideIcon( mLauncher );
					if( mInfo.isHideIcon() )
					{
						mHideApps.add( mInfo );
					}
				}
				if( mHideApps.size() > 0 )
				{
					mApps.removeAll( mHideApps );
					sortApp( sortAppCheckId , true );
				}
			}
		}
	}
	
	public int getAppsMode()
	{
		return mApps_mode;
	}
	
	@Override
	public boolean onLongClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( mApps_mode == HIDE_MODE
		//
		|| mApps_mode == UNINSTALL_MODE )//zhujieping add //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
		{
			return false;
		}
		return super.onLongClick( v );
	}
	
	//zhujieping add end
	@Override
	public boolean isDropEnabled()
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void onDrop(
			DragObject dragObject )
	{
		// TODO Auto-generated method stub
		if( mCurrentDragView != null )
		{
			mCurrentDragView.setVisibility( View.VISIBLE );
			mCurrentDragView = null;
		}
		dragObject.deferDragViewCleanupPostAnimation = false;
	}
	
	@Override
	public void onDragEnter(
			DragObject dragObject )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onDragOver(
			DragObject dragObject )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onDragExit(
			DragObject dragObject )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onFlingToDelete(
			DragObject dragObject ,
			int x ,
			int y ,
			PointF vec )
	{
		// Do nothing
	}
	
	@Override
	public boolean acceptDrop(
			DragObject dragObject )
	{
		// TODO Auto-generated method stub
		if( dragObject.dragSource instanceof AppsCustomizePagedView )
			return true;
		return false;
	}
	
	@Override
	public void getHitRectRelativeToDragLayer(
			Rect outRect )
	{
		// Do nothing
	}
	
	@Override
	public void getLocationInDragLayer(
			int[] loc )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void dropTargetAnimEnd()
	{
		// TODO Auto-generated method stub
		if( mApps_mode == EDIT_MODE )
		{
			AppsCustomizeTabHost host = (AppsCustomizeTabHost)getTabHost();
			host.setStateBarShow( true , true );
		}
	}
	
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		if( ( arg0 instanceof IconCache ) == false )
		{
			return;
		}
		for( AppInfo info : mApps )
		{
			info.onThemeChanged( arg0 , arg1 );
		}
		int mCurrentPageIndex = getCurrentPage();
		View children = getChildAt( mCurrentPageIndex );
		if( !( children instanceof CellLayout ) )
		{
			return;
		}
		CellLayout mCellLayout = (CellLayout)children;
		if( mCellLayout != null )
		{
			mCellLayout.onThemeChanged( arg0 , arg1 );
		}
		int mCount = getChildCount();
		for( int i = 0 ; i < mCount ; i++ )
		{
			if( i == mCurrentPageIndex )
			{
				continue;
			}
			mCellLayout = (CellLayout)getChildAt( i );
			mCellLayout.onThemeChanged( arg0 , arg1 );
		}
	}
	
	//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
	private void setAppsUninstallMode(
			boolean isUninstallMode )
	{
		for( int i = 0 ; i < this.getChildCount() ; i++ )
		{
			View page = null;
			if( ( page = this.getChildAt( i ) ) != null )
			{
				if( page instanceof AppsCustomizeCellLayout )
				{
					AppsCustomizeCellLayout pagedViewCellLayout = (AppsCustomizeCellLayout)page;
					ShortcutAndWidgetContainer pagedViewCellLayoutChildren = null;
					if( pagedViewCellLayout.getChildAt( 0 ) != null )
					{
						pagedViewCellLayoutChildren = (ShortcutAndWidgetContainer)pagedViewCellLayout.getChildAt( 0 );
					}
					for( int j = 0 ; j < pagedViewCellLayoutChildren.getChildCount() ; j++ )
					{
						View view = null;
						if( ( view = pagedViewCellLayoutChildren.getChildAt( j ) ) == null )
						{
							continue;
						}
						if( view instanceof PagedViewIcon )
						{
							PagedViewIcon icon = (PagedViewIcon)view;
							icon.setUninstallIconShow( isUninstallMode );
						}
					}
				}
			}
		}
	}
	//zhujieping add end
	;
	
	//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
	public void setApplistLoop(
			boolean isLoop )
	{
		this.isAppsLoop = isLoop;
	}
	//xiatian add end
	;
	
	//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
	public void setWidgetLoop(
			boolean isLoop )
	{
		this.isWidgetLoop = isLoop;
	}
	//xiatian add end
	//zhujieping add start //拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
	public int getCurrentSortType()
	{
		return sortAppCheckId;
	}
	//zhujieping add end
}
