package com.cooee.phenix;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.Property;
import android.util.SparseArray;
import android.view.Choreographer;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.cooee.favorites.host.FavoritesPageManager;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.framework.wallpaper.IWallpaperOffsetInterpolator;
import com.cooee.framework.wallpaper.WallpaperOffsetManager;
import com.cooee.phenix.Launcher.FavoritesPageCallbacks;
import com.cooee.phenix.LauncherSettings.Favorites;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.Folder.FolderIcon.FolderRingAnimator;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.PagedView.PagedViewIcon;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.CellInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.LauncherAppWidgetInfo;
import com.cooee.phenix.data.PendingAddItemInfo;
import com.cooee.phenix.data.PendingAddShortcutInfo;
import com.cooee.phenix.data.PendingAddWidgetInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.phenix.effects.CuboidEffect;
import com.cooee.phenix.effects.EffectInfo;
import com.cooee.phenix.effects.IEffect;
import com.cooee.phenix.iconhouse.IconHouseManager;
import com.cooee.phenix.musicpage.MusicView;
import com.cooee.phenix.pageIndicators.PageIndicator;
import com.cooee.phenix.pageIndicators.PageMarkerResources;
import com.cooee.phenix.util.ZhiKeShortcutManager;
import com.cooee.theme.ThemeManager;
import com.cooee.uniex.wrap.IFavoriteClings;
import com.cooee.util.DecorateUtils;
import com.cooee.util.DefaultDialog;
import com.cooee.util.Tools;
import com.cooee.wallpaperManager.WallpaperManagerBase;
import com.umeng.analytics.MobclickAgent;


/**
 * The workspace is a wide area with a wallpaper and a finite number of pages.
 * Each page contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends SmoothPagedView implements DropTarget , DragSource , DragScroller , View.OnTouchListener , DragController.DragListener , ILauncherTransitionable , ViewGroup.OnHierarchyChangeListener , Insettable
//
, IFavoriteClings
//
, IOnThemeChanged//换主题
{
	
	private static final String TAG = "Launcher.Workspace";
	// Y rotation to apply to the workspace screens
	private static final float WORKSPACE_OVERSCROLL_ROTATION = 24f;
	private static final int CHILDREN_OUTLINE_FADE_OUT_DELAY = 0;
	private static final int CHILDREN_OUTLINE_FADE_OUT_DURATION = 375;
	private static final int CHILDREN_OUTLINE_FADE_IN_DURATION = 100;
	private static final int BACKGROUND_FADE_OUT_DURATION = 350;
	private static final int ADJACENT_SCREEN_DROP_DURATION = 300;
	//private static final int FLING_THRESHOLD_VELOCITY = 500;
	private static final int FLING_THRESHOLD_VELOCITY = 100;
	// zhangjin@2015/07/21 UPD END
	private static final float ALPHA_CUTOFF_THRESHOLD = 0.01f;
	// These animators are used to fade the children's outlines
	private ObjectAnimator mChildrenOutlineFadeInAnimation;
	private ObjectAnimator mChildrenOutlineFadeOutAnimation;
	private float mChildrenOutlineAlpha = 0;
	// These properties refer to the background protection gradient used for AllApps and Customize
	private ValueAnimator mBackgroundFadeInAnimation;
	private ValueAnimator mBackgroundFadeOutAnimation;
	private Drawable mBackground;
	boolean mDrawBackground = true;
	private float mBackgroundAlpha = 0;
	private static final long CUSTOM_CONTENT_GESTURE_DELAY = 200;
	private long mTouchDownTime = -1;
	private LayoutTransition mLayoutTransition;
	private final WallpaperManager mWallpaperManager;
	private IBinder mWindowToken;
	//xiatian del start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
	//	private int mOriginalDefaultPage;
	//	private int mDefaultPage;
	//xiatian del end
	private int mScreenNumMax = 20;//xiatian add	//限制桌面最大页数
	private ShortcutAndWidgetContainer mDragSourceInternal;//该参数只用于workspace
	// The screen id used for the empty screen always present to the right.
	private final static long EXTRA_EMPTY_SCREEN_ID = -201;
	/**酷生活页ScreenId*/
	private final static long FUNCTION_FAVORITES_PAGE_SCREEN_ID = -301;
	/**音乐页ScreenId*/
	private final static long FUNCTION_MUSIC_PAGE_SCREEN_ID = -111;
	/**相机页ScreenId*/
	private final static long FUNCTION_CAMERA_PAGE_SCREEN_ID = -121;
	/**编辑模式下，可增加空白页时，显示的+号页的ScreenId*/
	private final static long EXTRA_ADD_PAGE_SCREEN_ID = -131;//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	private HashMap<Long , CellLayout> mWorkspaceScreens = new HashMap<Long , CellLayout>();
	private ArrayList<Long> mScreenOrder = new ArrayList<Long>();
	/**
	 * CellInfo for the cell that is currently being dragged
	 */
	private CellInfo mDragInfo;
	/**
	 * Target drop area calculated during last acceptDrop call.
	 */
	private int[] mTargetCell = new int[2];
	private int mDragOverX = -1;
	private int mDragOverY = -1;
	static Rect mLandscapeCellLayoutMetrics = null;
	static Rect mPortraitCellLayoutMetrics = null;
	FavoritesPageCallbacks mFavoritesPageCallbacks;
	boolean mIsFavoritesPageShowing;
	//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	//	private float mFavoritesPageScrollProgressLast = -1f;
	//	private float mMediaPageScrollProgressLast = -1f;
	//xiatian del end
	private int mLastPage = -1;//上一个的页面Id。在切页开始时记录。
	/**
	 * The CellLayout that is currently being dragged over
	 */
	private CellLayout mDragTargetLayout = null;
	/**
	 * The CellLayout that we will show as glowing
	 */
	private CellLayout mDragOverlappingLayout = null;
	/**
	 * The CellLayout which will be dropped to
	 */
	private CellLayout mDropToLayout = null;
	private View mCurrentDragView;//cheyingkun add	//解决“取消T卡挂载模式，长按文件夹内灰色图标，被长按的图标没有变亮”的问题。【i_0011410】
	private Launcher mLauncher;
	private IconCache mIconCache;
	private DragController mDragController;
	// These are temporary variables to prevent having to allocate a new object just to
	// return an (x, y) value from helper functions. Do NOT use them to maintain other state.
	private int[] mTempCell = new int[2];
	private int[] mTempPt = new int[2];
	private int[] mTempEstimate = new int[2];
	private float[] mDragViewVisualCenter = new float[2];
	private float[] mTempCellLayoutCenterCoordinates = new float[2];
	private Matrix mTempInverseMatrix = new Matrix();
	private SpringLoadedDragController mSpringLoadedDragController;
	private float mSpringLoadedShrinkFactor;
	private float mOverviewModeShrinkFactor;
	private int mOverviewModePageOffset;
	//cheyingkun add start	//飞利浦卸载应用自动排序（逻辑完善）
	/**卸载的应用列表*/
	private ArrayList<ItemInfo> removeList = new ArrayList<ItemInfo>();
	private boolean mEnableSortAfterUninstall;
	//cheyingkun add end
	;
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	private float[] mHotseatAlpha = new float[]{ 1 , 1 , 1 };
	private static final int HOTSEAT_STATE_ALPHA_INDEX = 2;
	private WorkspaceStateTransitionAnimation mStateTransitionAnimation;
	//zhujieping add end
	;
	
	// State variable that indicates whether the pages are small (ie when you're
	// in all apps or customize mode)
	public enum State
	{
		NORMAL , SPRING_LOADED , SMALL , OVERVIEW
	};
	
	private State mState = State.NORMAL;
	private boolean mIsSwitchingState = false;
	boolean mAnimatingViewIntoPlace = false;
	boolean mIsDragOccuring = false;
	boolean mChildrenLayersEnabled = true;
	private boolean mStripScreensOnPageStopMoving = false;
	/** Is the user is dragging an item near the edge of a page? */
	private boolean mInScrollArea = false;
	private HolographicOutlineHelper mOutlineHelper;
	private Bitmap mDragOutline = null;
	private final Rect mTempRect = new Rect();
	private final int[] mTempXY = new int[2];
	private int[] mTempVisiblePagesRange = new int[2];
	private boolean mOverscrollTransformsSet;
	private float mLastOverscrollPivotX;
	public static final int DRAG_BITMAP_PADDING = 2;
	private boolean mWorkspaceFadeInAdjacentScreens;
	WallpaperOffsetInterpolator mWallpaperOffset;
	private Runnable mDelayedResizeRunnable;
	private Runnable mDelayedSnapToPageRunnable;
	private Point mDisplaySize = new Point();
	private int mCameraDistance;
	// Variables relating to the creation of user folders by hovering shortcuts over shortcuts
	private static final int FOLDER_CREATION_TIMEOUT = 0;
	private static final int REORDER_TIMEOUT = 250;
	private final Alarm mFolderCreationAlarm = new Alarm();
	private final Alarm mReorderAlarm = new Alarm();
	private FolderRingAnimator mDragFolderRingAnimator = null;
	private FolderIcon mDragOverFolderIcon = null;
	private boolean mCreateUserFolderOnDrop = false;
	private boolean mAddToExistingFolderOnDrop = false;
	private DropTarget.DragEnforcer mDragEnforcer;
	private float mMaxDistanceForFolderCreation;
	// Variables relating to touch disambiguation (scrolling workspace vs. scrolling a widget)
	private float mXDown;
	private float mYDown;
	final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float)Math.PI / 6;
	final static float MAX_SWIPE_ANGLE = (float)Math.PI / 3;
	final static float TOUCH_SLOP_DAMPING_FACTOR = 4;
	// Relating to the animation of items being dropped externally
	public static final int ANIMATE_INTO_POSITION_AND_DISAPPEAR = 0;
	public static final int ANIMATE_INTO_POSITION_AND_REMAIN = 1;
	public static final int ANIMATE_INTO_POSITION_AND_RESIZE = 2;
	public static final int COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION = 3;
	public static final int CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION = 4;
	// Related to dragging, folder creation and reordering
	private static final int DRAG_MODE_NONE = 0;
	private static final int DRAG_MODE_CREATE_FOLDER = 1;
	private static final int DRAG_MODE_ADD_TO_FOLDER = 2;
	private static final int DRAG_MODE_REORDER = 3;
	private int mDragMode = DRAG_MODE_NONE;
	private int mLastReorderX = -1;
	private int mLastReorderY = -1;
	private SparseArray<Parcelable> mSavedStates;
	private final ArrayList<Integer> mRestoredPages = new ArrayList<Integer>();
	// These variables are used for storing the initial and final values during workspace animations
	private int mSavedScrollX;
	private float mSavedRotationY;
	private float mSavedTranslationX;
	private float mCurrentScale;
	private float mNewScale;
	private float[] mOldBackgroundAlphas;
	private float[] mOldAlphas;
	private float[] mNewBackgroundAlphas;
	private float[] mNewAlphas;
	private int mLastChildCount = -1;
	private float mTransitionProgress;
	private Runnable mDeferredAction;
	private boolean mDeferDropAfterUninstall;
	private boolean mUninstallSuccessful;
	/*********************************/
	private boolean isSampling;
	private int nMoving;
	private boolean isRoundAbout;
	private float xShift;
	private float xTouch = 0;
	private final Runnable mBindPages = new Runnable() {
		
		@Override
		public void run()
		{
			mLauncher.getModel().bindRemainingSynchronousPages();
		}
	};
	//WangLei add start //bug:0010281 //拖动小部件至当前页面的左右两边页面，当左右页面高亮但还未切页时松手，添加成功后没有跳转到显示插件的页面
	/**添加插件需要切页时的时长*/
	private static final int SNAP_WHEN_DROP_DURATION = 300;
	/**如果添加插件时需要切页，切页时长与添加插件的线程等待时长之间的差值*/
	private static final int ADD_PAAWIDGET_AFTER_SNAP = 50;
	//WangLei add end
	;
	//<i_0010089> liuhailin@2015-04-03 modify begin
	private int mPageShiftX = 0;//记录删除页面的下一页将要设置的X坐标点。
	//<i_0010089> liuhailin@2015-04-03 modify end
	// zhujieping@2015/05/26 ADD START，custompage为true时在最左边，false在最右边，cutompage不执行特效
	private CuboidEffect mCuboidEffect;
	// zhujieping@2015/05/26 ADD END
	;
	//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
	//下面六种模式下，默认主页分开存储。
	//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，默认主页错误”的问题。
	private static final String DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE_KEY_CORE = "default_page_bind_with_celllayout_index_in_workspace_key_core";//默认主页（与Workspace的第几页绑定）：单层
	private static final String DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE_KEY_DRAWER = "default_page_bind_with_celllayout_index_in_workspace_key_draw";//默认主页（与Workspace的第几页绑定）：双层
	private static final String DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE_KEY_CORE_IN_CATEGORY = "default_page_bind_with_celllayout_index_in_workspace_key_core_in_category";//默认主页（与Workspace的第几页绑定）：智能分类模式
	//xiatian add end
	private static final String DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_KEY_CORE = "default_page_bind_with_celllayout_key_core";//默认主页（与CellLayout绑定）：单层
	private static final String DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_KEY_DRAWER = "default_page_bind_with_celllayout_key_draw";//默认主页（与CellLayout绑定）：双层
	private static final String DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_KEY_CORE_IN_CATEGORY = "default_page_bind_with_celllayout_key_core_in_category";//默认主页（与CellLayout绑定）：智能分类模式
	//xiatian add end
	;
	// zhangjin@2015/08/04 ADD START i_11917 
	private Animator mChangeStateAnim;
	// zhangjin@2015/08/04 ADD END
	;
	//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
	private FolderIcon.PreviewBackground mFolderCreateBg;
	//zhujieping add end
	;
	private Animator overviewAnim = null;//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	/**
	 * Used to inflate the Workspace from XML.
	 *
	 * @param context The application's context.
	 * @param attrs The attributes set containing the Workspace's customization values.
	 */
	public Workspace(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	/**
	 * Used to inflate the Workspace from XML.
	 *
	 * @param context The application's context.
	 * @param attrs The attributes set containing the Workspace's customization values.
	 * @param defStyle Unused.
	 */
	public Workspace(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		// zhangjin@2015/07/24 UPD START
		//CAMERA_DISTANCE = 1000;		
		// zhangjin@2015/07/24 UPD END
		mContentIsRefreshable = false;
		mOutlineHelper = HolographicOutlineHelper.obtain( context );
		mDragEnforcer = new DropTarget.DragEnforcer( context );
		// With workspace, data is available straight from the get-go
		setDataIsReady();
		mLauncher = (Launcher)context;
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		//xiatian add start	//解决“2017/04/11 14:37:10”引起的一系列问题。
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT )
		//【备注】
		//	“config_folder_style”设置为0的前提下
		//		1、“桌面有文件夹时，重启桌面，重启后，所有页面都显示空白”的问题；
		//		2、“拖动桌面图标生成文件夹后，所有页面都显示空白”的问题；
		//		3、“拖动桌面图标放入文件夹后，所有页面都显示空白”的问题；
		//		4、“点击打开文件夹后，所有页面都显示空白”的问题；
		//xiatian add end
		{
			mStateTransitionAnimation = new WorkspaceStateTransitionAnimation( mLauncher , this );
		}
		//zhujieping add end
		final Resources res = getResources();
		mWorkspaceFadeInAdjacentScreens = LauncherDefaultConfig.getBoolean( R.bool.config_workspaceFadeAdjacentScreens );
		mFadeInAdjacentScreens = false;
		mWallpaperManager = WallpaperManager.getInstance( context );
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.Workspace , defStyle , 0 );
		mSpringLoadedShrinkFactor = LauncherDefaultConfig.getInt( R.integer.config_workspaceSpringLoadShrinkPercentage ) / 100.0f;
		mOverviewModeShrinkFactor = LauncherDefaultConfig.getInt( R.integer.config_workspaceOverviewShrinkPercentage ) / 100.0f;
		mOverviewModePageOffset = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_mode_page_offset );
		mCameraDistance = LauncherDefaultConfig.getInt( R.integer.config_cameraDistance );
		//xiatian start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
		//mOriginalDefaultPage = mDefaultPage = a.getInt( R.styleable.Workspace_defaultScreen , 1 );//xiatian del
		initDefaultPage();//xiatian add
		//xiatian end
		mScreenNumMax = a.getInt( R.styleable.Workspace_screenNumMax , 20 );//xiatian add	//限制桌面最大页数
		a.recycle();
		setOnHierarchyChangeListener( this );
		setHapticFeedbackEnabled( false );
		initWorkspace();
		// zhujieping@2015/04/29 ADD START
		//xiatian start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		//		boolean isLoop = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_workspace_loop_slide );//xiatian del
		boolean isLoop = LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_LOOP_SLIDE;//xiatian add
		//xiatian end
		setLoop( isLoop );
		// zhujieping@2015/04/29 ADD END
		// Disable multitouch across the workspace/all apps/customize tray
		setMotionEventSplittingEnabled( true );
		xTouch = 0;
		isRoundAbout = false;
		isSampling = true;
		isComputeWallpaperOffset = false;
		nMoving = 0;
		// zhujieping@2015/05/26 ADD START, 初始化
		mCuboidEffect = new CuboidEffect( -1 , null );
		//xiatian del start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		//		if( isLoop )//zhujieping,这里不调用isLoop()(isLoop()方法中判断子view个数是否大于1，这里是初始化，子view个数为0)
		//		{
		//			mCuboidEffect.setMaxScroll( 1.0f );
		//			mCuboidEffect.setLoop( true );
		//		}
		//		else
		//		{
		//			mCuboidEffect.setMaxScroll( 0.5f );
		//			mCuboidEffect.setLoop( false );
		//		}
		//		// zhujieping@2015/05/26 ADD END
		//xiatian del end
		mEnableSortAfterUninstall = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_sort_after_uninstall );//cheyingkun add	//飞利浦卸载应用自动排序（逻辑完善）
	}
	
	@Override
	public void setInsets(
			Rect insets )
	{
		mInsets.set( insets );
	}
	
	// estimate the size of a widget with spans hSpan, vSpan. return MAX_VALUE for each
	// dimension if unsuccessful
	public int[] estimateItemSize(
			int hSpan ,
			int vSpan ,
			ItemInfo itemInfo ,
			boolean springLoaded )
	{
		int[] size = new int[2];
		if( getChildCount() > 0 )
		{
			// Use the first non-custom page to estimate the child position
			//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			//			int mFirstNormalPageIndex = hasFavoritesPage() ? 1 : 0;//xiatian del
			//xiatian add start
			int mFirstNormalPageIndex = -1;
			for( int i = 0 ; i < getChildCount() ; i++ )
			{
				CellLayout mChild = (CellLayout)getChildAt( i );
				if( mChild.isFunctionPage() == false )
				{
					mFirstNormalPageIndex = i;
					break;
				}
			}
			if( mFirstNormalPageIndex >= 0 )
			//xiatian add end
			//xiatian end
			{
				CellLayout mFirstNormalPage = (CellLayout)getChildAt( mFirstNormalPageIndex );
				if( mFirstNormalPage != null )
				{
					Rect r = estimateItemPosition( mFirstNormalPage , itemInfo , 0 , 0 , hSpan , vSpan );
					size[0] = r.width();
					size[1] = r.height();
					if( springLoaded )
					{
						size[0] *= mSpringLoadedShrinkFactor;
						size[1] *= mSpringLoadedShrinkFactor;
					}
				}
			}
			return size;
		}
		else
		{
			size[0] = Integer.MAX_VALUE;
			size[1] = Integer.MAX_VALUE;
			return size;
		}
	}
	
	public Rect estimateItemPosition(
			CellLayout cl ,
			ItemInfo pendingInfo ,
			int hCell ,
			int vCell ,
			int hSpan ,
			int vSpan )
	{
		Rect r = new Rect();
		cl.cellToRect( hCell , vCell , hSpan , vSpan , r );
		return r;
	}
	
	public void onDragStart(
			final DragSource source ,
			Object info ,
			int dragAction )
	{
		mIsDragOccuring = true;
		updateChildrenLayersEnabled( false );
		mLauncher.onInteractionBegin();
		setChildrenBackgroundAlphaMultipliers( 1f );
		// Prevent any Un/InstallShortcutReceivers from updating the db while we are dragging
		InstallShortcutReceiver.enableInstallQueue();
		UninstallShortcutReceiver.enableUninstallQueue();
		post( new Runnable() {
			
			@Override
			public void run()
			{
				if( mIsDragOccuring )
				{
					resetSearchPos();
					removeFunctionPagesFromScreenOnlyWhenDragStart();
					addExtraEmptyScreenWhenDragStart();
					moveToFirstNormalPageIfNecessaryWhenDragStart();//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
				}
			}
		} );
		// zhujieping@2015/06/11 ADD START,从右向左的布局，只有两个图标的文件夹，拖动图标出去放下同时删除文件夹，mLayoutTransition会回调动画，导致桌面会闪现到前一页
		//		if( isLayoutRtl() )//zhujieping del //从左向右的布局中，第一页只有一个图标，将这个拖动到新增页时，新增页面与相机页重叠【i_0015074】
		disableLayoutTransitions();
		// zhujieping@2015/06/11 ADD END
	}
	
	public void onDragEnd()
	{
		mIsDragOccuring = false;
		updateChildrenLayersEnabled( false );
		// Re-enable any Un/InstallShortcutReceiver and now process any queued items
		InstallShortcutReceiver.disableAndFlushInstallQueue( getContext() );
		UninstallShortcutReceiver.disableAndFlushUninstallQueue( getContext() );
		removeExtraEmptyScreen();
		// YANGTIANYU@2016/07/27 ADD START
		mMediaPageHandler.sendEmptyMessage( ADD_MEDIAPAGE_TO_SCREEN );
		// YANGTIANYU@2016/07/27 ADD END
		mDragSourceInternal = null;
		mLauncher.onInteractionEnd();
		// zhujieping@2015/06/11 ADD START
		//		if( isLayoutRtl() ) //从左向右的布局中，第一页只有一个图标，将这个拖动到新增页时，新增页面与相机页重叠【i_0015074】
		enableLayoutTransitions();//ondragstart中设置disable，ondragend置回enable
		// zhujieping@2015/06/11 ADD END
		updateHomePageIndicator();//xiatian add	//fix bug：解决“特定页面（酷生活、主页、音乐页和相机页）的页面指示器显示特定图标时，页面指示器显示错误（重复以及错位）”的问题。
	}
	
	/**
	 * Initializes various states for this workspace.
	 */
	protected void initWorkspace()
	{
		Context context = getContext();
		//		mCurrentPage = mDefaultPage;//xiatian del	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		mIconCache = app.getIconCache();
		setWillNotDraw( false );
		setClipChildren( false );
		setClipToPadding( false );
		setChildrenDrawnWithCacheEnabled( true );
		// This is a bit of a hack to account for the fact that we translate the workspace
		// up a bit, and still need to draw the background covering the whole screen.
		setMinScale( mOverviewModeShrinkFactor - 0.2f );
		setupLayoutTransition();
		final Resources res = getResources();
		try
		{
			mBackground = res.getDrawable( R.drawable.apps_customize_bg );
		}
		catch( Resources.NotFoundException e )
		{
			// In this case, we will skip drawing background protection
		}
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
		{
			mWallpaperOffset = new WallpaperOffsetInterpolator();
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		Display display = mLauncher.getWindowManager().getDefaultDisplay();
		display.getSize( mDisplaySize );
		mMaxDistanceForFolderCreation = ( 0.55f * grid.getIconWidthSizePx() );
		mFlingThresholdVelocity = (int)( FLING_THRESHOLD_VELOCITY * mDensity );
		//		initAnimationStyle();
	}
	
	private void setupLayoutTransition()
	{
		// We want to show layout transitions when pages are deleted, to close the gap.
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		if( Build.VERSION.SDK_INT >= 16 )
		{
			mLayoutTransition = new LayoutTransition();
			mLayoutTransition.enableTransitionType( LayoutTransition.DISAPPEARING );
			mLayoutTransition.enableTransitionType( LayoutTransition.CHANGE_DISAPPEARING );
			mLayoutTransition.disableTransitionType( LayoutTransition.APPEARING );
			mLayoutTransition.disableTransitionType( LayoutTransition.CHANGE_APPEARING );
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		setLayoutTransition( mLayoutTransition );
	}
	
	void enableLayoutTransitions()
	{
		setLayoutTransition( mLayoutTransition );
	}
	
	void disableLayoutTransitions()
	{
		setLayoutTransition( null );
	}
	
	@Override
	protected int getScrollMode()
	{
		return SmoothPagedView.X_LARGE_MODE;
	}
	
	@Override
	public void onChildViewAdded(
			View parent ,
			View child )
	{
		if( !( child instanceof CellLayout ) )
		{
			throw new IllegalArgumentException( "A Workspace can only have CellLayout children." );
		}
		CellLayout cl = ( (CellLayout)child );
		cl.setOnInterceptTouchListener( this );
		cl.setClickable( true );
		super.onChildViewAdded( parent , child );
	}
	
	protected boolean shouldDrawChild(
			View child )
	{
		final CellLayout cl = (CellLayout)child;
		return super.shouldDrawChild( child ) && ( mIsSwitchingState || cl.getShortcutsAndWidgets().getAlpha() > 0 || cl.getBackgroundAlpha() > 0 );
	}
	
	/**
	 * @return The open folder on the current screen, or null if there is none
	 */
	Folder getOpenFolder()
	{
		DragLayer dragLayer = mLauncher.getDragLayer();
		int count = dragLayer.getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View child = dragLayer.getChildAt( i );
			if( child instanceof Folder )
			{
				Folder folder = (Folder)child;
				if( folder.getInfo().getOpened() )
					return folder;
			}
		}
		return null;
	}
	
	boolean isTouchActive()
	{
		return mTouchState != TOUCH_STATE_REST;
	}
	
	public void removeAllWorkspaceScreens()
	{
		// Disable all layout transitions before removing all pages to ensure that we don't get the
		// transition animations competing with us changing the scroll when we add pages or the
		// custom content screen
		disableLayoutTransitions();
		// Since we increment the current page when we call addFavoritesPage via bindScreens
		// (and other places), we need to adjust the current page back when we clear the pages
		if( hasFavoritesPage() )
		{
			removeFavoritesPage();
		}
		if( hasCameraPage() || hasMusicPage() )
		{
			removeMediaPages();
		}
		// Remove the pages and clear the screen models
		//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		//0010405: 【桌面】桌面编辑模式将某页面移动位置后再选择智能分类或回到之前，桌面重新刷新后该页面图标显示异常 , change by shlt@2015/03/09 ADD START
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			View childView = null;
			if( ( childView = getChildAt( i ) ) != null )
			{
				if( childView instanceof CellLayout )
				{
					CellLayout cell = (CellLayout)childView;
					cell.removeAllViews();
				}
			}
		}
		//0010405: 【桌面】桌面编辑模式将某页面移动位置后再选择智能分类或回到之前，桌面重新刷新后该页面图标显示异常 , change by shlt@2015/03/09 ADD END
		//cheyingkun add end
		removeAllViews();
		mScreenOrder.clear();
		mWorkspaceScreens.clear();
		// Re-enable the layout transitions
		enableLayoutTransitions();
	}
	
	public long insertNewWorkspaceScreenBeforeEmptyScreen(
			long screenId )
	{
		// Find the index to insert this view into.  If the empty screen exists, then
		// insert it before that.
		int mNewPageIndex = mScreenOrder.indexOf( EXTRA_EMPTY_SCREEN_ID );
		if( mNewPageIndex < 0 )
		{
			mNewPageIndex = mScreenOrder.size();
		}
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“第一次加载时，非默认页面显示在第一个“媒体页”（“相机页”和“音乐页”）前面”的问题。）
		//xiatian del start
		//		// YANGTIANYU@2016/06/20 ADD START
		//		// TODO 跟下面跑的一样的,但是这里好像没啥用处啊
		//		int mCameraPageIndex = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		//		int mMusicPageIndex = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		//		mCameraPageIndex = mCameraPageIndex < 0 ? mNewPageIndex : mCameraPageIndex;
		//		mMusicPageIndex = mMusicPageIndex < 0 ? mNewPageIndex : mMusicPageIndex;
		//		mNewPageIndex = Math.min( mNewPageIndex , Math.min( mCameraPageIndex , mMusicPageIndex ) );
		//		// YANGTIANYU@2016/06/20 ADD END
		//xiatian del 
		//xiatian add start
		int mFavoritesPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		if( mFavoritesPageIndexInViewGroup != -1 )
		{
			int mFavoritesPageIndexKey = LauncherDefaultConfig.getFavoritesPagePosition();
			if( mFavoritesPageIndexKey > 0 )
			{
				mNewPageIndex = Math.min( mNewPageIndex , mFavoritesPageIndexInViewGroup );
			}
		}
		int mCameraPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		if( mCameraPageIndexInViewGroup != -1 )
		{
			int mCameraPageIndexKey = LauncherDefaultConfig.getCameraPagePosition();
			if( mCameraPageIndexKey > 0 )
			{
				mNewPageIndex = Math.min( mNewPageIndex , mCameraPageIndexInViewGroup );
			}
		}
		int mMusicPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		if( mMusicPageIndexInViewGroup != -1 )
		{
			int mMusicPageIndexKey = LauncherDefaultConfig.getMusicPagePosition();
			if( mMusicPageIndexKey > 0 )
			{
				mNewPageIndex = Math.min( mNewPageIndex , mMusicPageIndexInViewGroup );
			}
		}
		//xiatian add end
		//xiatian end
		return insertNewWorkspaceScreen( screenId , mNewPageIndex );
	}
	
	public int getInsertNewPageIndex()
	{
		int mNewPageIndex = getChildCount();
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“第一次加载时，非默认页面显示在第一个“媒体页”（“相机页”和“音乐页”）前面”的问题。）
		//xiatian del start
		//		// YANGTIANYU@2016/06/20 ADD START
		//		int mCameraPageIndex = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		//		int mMusicPageIndex = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		//		mCameraPageIndex = mCameraPageIndex < 0 ? getChildCount() : mCameraPageIndex;
		//		mMusicPageIndex = mMusicPageIndex < 0 ? getChildCount() : mMusicPageIndex;
		//		mNewPageIndex = Math.min( mNewPageIndex , Math.min( mCameraPageIndex , mMusicPageIndex ) );
		//		// YANGTIANYU@2016/06/20 ADD END
		//xiatian del 
		//xiatian add start
		int mFavoritesPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		if( mFavoritesPageIndexInViewGroup != -1 )
		{
			int mFavoritesPageIndexKey = LauncherDefaultConfig.getFavoritesPagePosition();
			if( mFavoritesPageIndexKey > 0 )
			{
				mNewPageIndex = Math.min( mNewPageIndex , mFavoritesPageIndexInViewGroup );
			}
		}
		int mCameraPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		if( mCameraPageIndexInViewGroup != -1 )
		{
			int mCameraPageIndexKey = LauncherDefaultConfig.getCameraPagePosition();
			if( mCameraPageIndexKey > 0 )
			{
				mNewPageIndex = Math.min( mNewPageIndex , mCameraPageIndexInViewGroup );
			}
		}
		int mMusicPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		if( mMusicPageIndexInViewGroup != -1 )
		{
			int mMusicPageIndexKey = LauncherDefaultConfig.getMusicPagePosition();
			if( mMusicPageIndexKey > 0 )
			{
				mNewPageIndex = Math.min( mNewPageIndex , mMusicPageIndexInViewGroup );
			}
		}
		//xiatian add end
		//xiatian end
		return mNewPageIndex;
	}
	
	public long insertNewWorkspaceScreen(
			long screenId )
	{
		int mNewPageIndex = getInsertNewPageIndex();
		return insertNewWorkspaceScreen( screenId , mNewPageIndex );
	}
	
	public long insertNewWorkspaceScreen(
			long screenId ,
			int insertIndex )
	{
		if( mWorkspaceScreens.containsKey( screenId ) )
		{
			throw new RuntimeException( StringUtils.concat( "Screen id " , screenId , " already exists!" ) );
		}
		CellLayout mNewPageView = (CellLayout)mLauncher.getLayoutInflater().inflate( R.layout.workspace_screen , null );
		mNewPageView.setOnLongClickListener( mLongClickListener );
		mNewPageView.setOnClickListener( mLauncher );
		mNewPageView.setSoundEffectsEnabled( false );
		//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
		{
			if( screenId != FUNCTION_FAVORITES_PAGE_SCREEN_ID && screenId != FUNCTION_MUSIC_PAGE_SCREEN_ID && screenId != FUNCTION_CAMERA_PAGE_SCREEN_ID )
			{
				mNewPageView.addOverViewModeHomeView();
			}
		}
		//gaominghui add end
		mWorkspaceScreens.put( screenId , mNewPageView );
		mScreenOrder.add( insertIndex , screenId );
		addView( mNewPageView , insertIndex );
		//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）。
		if( LauncherDefaultConfig.isAllowEmptyScreen() )
		{
			if( mState == State.OVERVIEW )
			{
				mNewPageView.setBackgroundAlpha( 1.0f );
				if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
				{
					if( screenId != FUNCTION_FAVORITES_PAGE_SCREEN_ID && screenId != FUNCTION_MUSIC_PAGE_SCREEN_ID && screenId != FUNCTION_CAMERA_PAGE_SCREEN_ID )
					{
						mNewPageView.setEditModeHomeViewVisible( View.VISIBLE );
					}
				}
			}
		}
		//zhujieping add end
		return screenId;
	}
	
	public void createAndAddFavoritesPage()
	{
		CellLayout mFavoritesPageView = (CellLayout)mLauncher.getLayoutInflater().inflate( R.layout.workspace_screen , null );
		//cheyingkun add start	//phenix1.1稳定版移植酷生活
		mFavoritesPageView.setPadding( 0 , 0 , 0 , 0 );//cheyingkun add	//优化客户搜索和运营酷搜相关逻辑
		//cheyingkun add end
		int mFavoritesPageIndexInViewGroup = 0;
		mWorkspaceScreens.put( FUNCTION_FAVORITES_PAGE_SCREEN_ID , mFavoritesPageView );
		mFavoritesPageIndexInViewGroup = getToAddFunctionPageIndexInViewGroupBeforeAddToScreenOrderAndViewGroup( FUNCTION_FAVORITES_PAGE_SCREEN_ID );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		mScreenOrder.add( ( mFavoritesPageIndexInViewGroup == -1 ? mScreenOrder.size() : mFavoritesPageIndexInViewGroup ) , FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		//cheyingkun start	//原生方式适配虚拟按键(酷生活页)
		//		addFullScreenPage( mFavoritesPageView , isFavoritesPageLeft );//cheyingkun del
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//xiatian del start
		//		addFavoritesPage( mFavoritesPageView );//cheyingkun add
		//xiatian del end
		addFavoritesPage( mFavoritesPageView , mFavoritesPageIndexInViewGroup );//xiatian add
		//xiatian end
		//cheyingkun end
		// Ensure that the current page and default page are maintained.
		//xiatian del start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		if( isFavoritesPageLeft )
		//		{
		//			//xiatian start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
		//			//mDefaultPage = mOriginalDefaultPage + 1;;//xiatian del
		//			setDefaultPage( mOriginalDefaultPage + 1 );//xiatian add
		//			//xiatian end
		//		}
		//xiatian del end
		//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		if( LauncherDefaultConfig.getFavoritesPagePosition() > 0 )
		{
			if( mRestorePage != INVALID_RESTORE_PAGE )
			{
				int restore = Math.max( 0 , Math.min( mRestorePage , getPageCount() - 1 ) );//防止启动时主页为酷生活
				if( getScreenIdForPageIndex( restore ) == FUNCTION_FAVORITES_PAGE_SCREEN_ID )
				{
					mRestorePage = restore - 1;
				}
			}
		}
		else
		//xiatian add end
		{
			if( mRestorePage != INVALID_RESTORE_PAGE )
			{
				if( mFavoritesPageIndexInViewGroup <= mRestorePage )//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
				{
					mRestorePage = mRestorePage + 1;
				}
			}
			else
			{
				int mCurrentPage = getCurrentPage();
				if( mFavoritesPageIndexInViewGroup <= mCurrentPage )//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
				{
					setCurrentPage( mCurrentPage + 1 );
				}
			}
		}
		initFavoritesPage( mFavoritesPageView );
	}
	
	public void removeFavoritesPage()
	{
		CellLayout mFavoritesPageView = getScreenWithId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		if( mFavoritesPageView == null )
		{
			throw new RuntimeException( "Expected custom content screen to exist" );
		}
		//cheyingkun add start	//phenix1.1稳定版移植酷生活
		if( hasFavoritesPage() )
		{
			FavoritesPageManager.getInstance( this.getContext() ).onHide();
		}
		//cheyingkun add end
		//cheyingkun  start	//phenix1.1稳定版移植酷生活
		//		removeFavoritesPageView();//cheyingkun del
		mFavoritesPageView.removeAllViews();//cheyingkun add
		//cheyingkun end
		mWorkspaceScreens.remove( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		int mFavoritesPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		mScreenOrder.remove( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		removeView( mFavoritesPageView );
		if( mFavoritesPageCallbacks != null )
		{
			mFavoritesPageCallbacks.onScrollProgressChanged( 0 );
			mFavoritesPageCallbacks.onHide();
		}
		mFavoritesPageCallbacks = null;
		// Ensure that the current page and default page are maintained.
		//xiatian del	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		if( isFavoritesPageLeft )//zhujieping add，左右适配
		//		{
		//			//xiatian start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
		//			//mDefaultPage = mOriginalDefaultPage - 1;;//xiatian del
		//			setDefaultPage( mOriginalDefaultPage - 1 );//xiatian add
		//			//xiatian end
		//		}
		//xiatian del end
		if( mRestorePage != INVALID_RESTORE_PAGE )
		{
			if( mFavoritesPageIndexInViewGroup < mRestorePage )//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			{
				mRestorePage = mRestorePage - 1;
			}
		}
		else
		{
			int mCurrentPage = getCurrentPage();
			if( mFavoritesPageIndexInViewGroup < mCurrentPage )//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			{
				setCurrentPage( mCurrentPage - 1 );
			}
		}
	}
	
	public void addToFavoritesPage(
			View customContent ,
			FavoritesPageCallbacks mFavoritesPageCallbacks ,
			String description )
	{
		CellLayout mFavoritesPageView = getScreenWithId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		if( mFavoritesPageView == null )
		{
			throw new RuntimeException( "Expected custom content screen to exist" );
		}
		// Add the custom content to the full screen custom page
		int spanX = mFavoritesPageView.getCountX();
		int spanY = mFavoritesPageView.getCountY();
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams( 0 , 0 , spanX , spanY );
		lp.canReorder = false;
		lp.isFullscreen = true;
		if( customContent instanceof Insettable )
		{
			( (Insettable)customContent ).setInsets( mInsets );
		}
		mFavoritesPageView.removeAllViews();
		mFavoritesPageView.addViewToCellLayout( customContent , 0 , 0 , lp , true );
		this.mFavoritesPageCallbacks = mFavoritesPageCallbacks;
	}
	
	public void addExtraEmptyScreenWhenDragStart()
	{
		if( mDragSourceInternal != null )
		{//DragSource From Workspace
			boolean lastChildOnScreen = false;
			boolean childOnFinalScreen = false;
			if( mDragSourceInternal.getChildCount() == 1 )
			{
				lastChildOnScreen = true;
			}
			CellLayout cl = (CellLayout)mDragSourceInternal.getParent();
			if( indexOfChild( cl ) == getChildCount() - 1 )
			{
				childOnFinalScreen = true;
			}
			// If this is the last item on the final screen
			//zhujieping start //配置config_empty_screen_id_in_core后，当最末页只有一个图标时，拖动图标向后无法生成新页面【i_0015246】
			if( !LauncherDefaultConfig.isAllowEmptyScreen() && lastChildOnScreen && childOnFinalScreen )//zhujieping add
			//			if( lastChildOnScreen && childOnFinalScreen )//zhujieping del
			//zhujieping end
			{
				return;
			}
		}
		//xiatian start	//解决“酷生活打开的前提下，桌面只剩一个空白普通页面时，长按底边栏图标、底边栏文件夹或者底边栏文件夹打开后里面的图标后，桌面仍然创建加号页面”的问题【i_0014995】
		//xiatian del start
		//		//xiatian add start	//fix bug：解决“桌面只剩一个空白页面的时候，长按添加插件时会添加加号空白页”的问题【i_0012319】
		//		int mCount = getChildCount();
		//		if( mCount == 1 )
		//		{
		//			CellLayout mCellLayout = (CellLayout)getChildAt( 0 );
		//			ShortcutAndWidgetContainer mShortcutAndWidgetContainer = mCellLayout.getShortcutsAndWidgets();
		//			if( mShortcutAndWidgetContainer.getChildCount() == 0 )
		//			{//workspace只有一个空白页面时，在长按抬起不在添加screenId为“EXTRA_EMPTY_SCREEN_ID”的空白页面
		//				return;
		//			}
		//		}
		//		//xiatian add end
		//xiatian del end
		//xiatian add start
		else
		{//DragSource From Hotseat Or Folder
			CellLayout mLastNormalCellLayout = (CellLayout)getChildAt( getChildCount() - 1 );
			ShortcutAndWidgetContainer mShortcutAndWidgetContainer = mLastNormalCellLayout.getShortcutsAndWidgets();
			if( !LauncherDefaultConfig.isAllowEmptyScreen() )//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
			{
				if( mShortcutAndWidgetContainer.getChildCount() == 0 )
				{//workspace只有一个空白页面时，在长按抬起不在添加screenId为“EXTRA_EMPTY_SCREEN_ID”的空白页面
					return;
				}
			}
		}
		//xiatian add end
		//xiatian end
		if(
		//
		( canInsertNewScreen()/* //xiatian add	//限制桌面最大页数 */)
		//
		&& !mWorkspaceScreens.containsKey( EXTRA_EMPTY_SCREEN_ID )
		//
		)
		{
			insertNewWorkspaceScreen( EXTRA_EMPTY_SCREEN_ID );
		}
	}
	
	// zhujieping@2015/07/01 ADD START
	ArrayList<Long> getScreensWhichCanAddShortcut()//获取可以加view的screens
	{
		ArrayList<Long> list = new ArrayList<Long>();
		list.addAll( mScreenOrder );
		if( list.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID ) >= 0 )
		{
			list.remove( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		}
		if( list.indexOf( EXTRA_EMPTY_SCREEN_ID ) >= 0 )
		{
			list.remove( EXTRA_EMPTY_SCREEN_ID );
		}
		// YANGTIANYU@2016/06/20 ADD START
		list.remove( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		list.remove( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		// YANGTIANYU@2016/06/20 ADD END
		return list;
	}
	
	// zhujieping@2015/07/01 ADD END
	public boolean addExtraEmptyScreen()
	{
		if(
		//
		canInsertNewScreen()//xiatian add	//限制桌面最大页数
				//
				&& !mWorkspaceScreens.containsKey( EXTRA_EMPTY_SCREEN_ID )
		//
		)
		{
			insertNewWorkspaceScreen( EXTRA_EMPTY_SCREEN_ID );
			return true;
		}
		return false;
	}
	
	public void removeExtraEmptyScreen()
	{
		//WangLei start //bug:0010403 在页面没有完全加载前长按已经加载的图标，等桌面加载完成时会删除空白页，导致最后一页被删除，页面指示器删除ic_pageindicator_add_page图标
		//【解决方案】如果有图标正在被长按弹起，则不删除多余的空白页
		//if( hasExtraEmptyScreen() ) //WangLei del
		if( hasExtraEmptyScreen() && !mIsDragOccuring )//WangLei add
		//WangLei end
		{
			// YANGTIANYU@2016/07/27 ADD START
			// 如果当前页面位置在即将删除的空白页，先将页面设置到空白页的前一页
			if( getCurrentPage() >= getChildCount() - 2 )
			{
				setCurrentPage( getChildCount() - 2 );
			}
			// YANGTIANYU@2016/07/27 ADD END
			CellLayout cl = mWorkspaceScreens.get( EXTRA_EMPTY_SCREEN_ID );
			mWorkspaceScreens.remove( EXTRA_EMPTY_SCREEN_ID );
			mScreenOrder.remove( EXTRA_EMPTY_SCREEN_ID );
			removeView( cl );
		}
	}
	
	public boolean hasExtraEmptyScreen()
	{
		int mChildCount = getChildCount();
		int mFunctionPagesInNormalPageLeftNum =
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//( hasFavoritesPage() ? 1 : 0 )//xiatian del
		getFunctionPagesInNormalPageLeftNum()//xiatian add
		//xiatian end
		;
		mChildCount -= mFunctionPagesInNormalPageLeftNum;
		return mWorkspaceScreens.containsKey( EXTRA_EMPTY_SCREEN_ID ) && mChildCount > 1;
	}
	
	public long commitExtraEmptyScreen()
	{
		int index = getPageIndexForScreenId( EXTRA_EMPTY_SCREEN_ID );
		CellLayout cl = mWorkspaceScreens.get( EXTRA_EMPTY_SCREEN_ID );
		mWorkspaceScreens.remove( EXTRA_EMPTY_SCREEN_ID );
		mScreenOrder.remove( EXTRA_EMPTY_SCREEN_ID );
		long newId = LauncherAppState.getLauncherProvider().generateNewScreenId();
		mWorkspaceScreens.put( newId , cl );
		if( index < 0 )
		{
			index = 0;
		}
		mScreenOrder.add( index , newId );//与mWorkspaceScreens保持位置一致,zhujieping add
		// Update the page indicator marker
		if( getPageIndicator() != null )
		{
			getPageIndicator().updateMarker( index , getPageIndicatorMarker( index ) );
		}
		// Update the model for the new screen
		mLauncher.getModel().updateWorkspaceScreenOrder( mLauncher , mScreenOrder );
		return newId;
	}
	
	public CellLayout getScreenWithId(
			long screenId )
	{
		CellLayout layout = mWorkspaceScreens.get( screenId );
		return layout;
	}
	
	public long getIdForScreen(
			CellLayout layout )
	{
		Iterator<Long> iter = mWorkspaceScreens.keySet().iterator();
		while( iter.hasNext() )
		{
			long id = iter.next();
			if( mWorkspaceScreens.get( id ) == layout )
			{
				return id;
			}
		}
		return -1;
	}
	
	public int getPageIndexForScreenId(
			long screenId )
	{
		return indexOfChild( mWorkspaceScreens.get( screenId ) );
	}
	
	public long getScreenIdForPageIndex(
			int index )
	{
		if( 0 <= index && index < mScreenOrder.size() )
		{
			return mScreenOrder.get( index );
		}
		return -1;
	}
	
	ArrayList<Long> getScreenOrder()
	{
		return mScreenOrder;
	}
	
	//xiatian add start	//解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”前提下，加载item的时候，发现并删除位于功能页的前一页的空白默认页（默认页数大于桌面页面数）后，当前页面没有跳到默认页面（第0页）”的问题。
	public void stripEmptyScreens()
	{
		stripEmptyScreens( true );
	}
	//xiatian add end
	;
	
	public void stripEmptyScreens(
			//
			final boolean isLoadFinish //xiatian add	//解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”前提下，加载item的时候，发现并删除位于功能页的前一页的空白默认页（默认页数大于桌面页面数）后，当前页面没有跳到默认页面（第0页）”的问题。 
	//
	)
	{
		//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
		if( LauncherDefaultConfig.isAllowEmptyScreen() )
		{
			return;
		}
		//zhujieping add end
		if( isPageMoving() )
		{
			mStripScreensOnPageStopMoving = true;
			return;
		}
		boolean mIsDelDefaultPage = false;//xiatian add	//解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，再删除默认主页那个页面，这时默认主页没有变为前一页”的问题。
		int currentPage = getNextPage();
		ArrayList<Long> removeScreens = new ArrayList<Long>();
		for( Long id : mWorkspaceScreens.keySet() )
		{
			CellLayout cl = mWorkspaceScreens.get( id );
			if( id >= 0 && cl.getShortcutsAndWidgets().getChildCount() == 0 )
			{
				removeScreens.add( id );
			}
		}
		// We enforce at least one page to add new items to. In the case that we remove the last
		// such screen, we convert the last screen to the empty screen
		int mFunctionPagesInNormalPageLeftNum =
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//( hasFavoritesPage() ? 1 : 0 )//xiatian del
		getFunctionPagesInNormalPageLeftNum()//xiatian add
		//xiatian end
		;
		int minScreens = 1 + mFunctionPagesInNormalPageLeftNum;
		int pageShift = 0;
		for( Long id : removeScreens )
		{
			CellLayout cl = mWorkspaceScreens.get( id );
			//xiatian add start	//解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，再删除默认主页那个页面，这时默认主页没有变为前一页”的问题。
			int mDefaultPageIndex = getDefaultPageIndex();
			if( indexOfChild( cl ) == mDefaultPageIndex )
			{
				mIsDelDefaultPage = true;
				if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE )
				{//更新SharedPreferences中存储的默认主页
					mDefaultPageIndex--;
					//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
					//xiatian del start
					//					if( hasFavoritesPage() )
					//					{
					//						mDefaultPageIndex--;
					//					}
					//xiatian del end
					mDefaultPageIndex -= mFunctionPagesInNormalPageLeftNum;//xiatian add
					//xiatian end
					setDefaultPage( mDefaultPageIndex );
				}
			}
			//xiatian add end
			//只删除掉该删除的空白页ID 【bug：0011479】wanghongjian add
			//			mWorkspaceScreens.remove( id );
			//			mScreenOrder.remove( id );
			//只删除掉该删除的空白页ID 【bug：0011479】wanghongjian end
			if( getChildCount() > minScreens )
			{
				//只删除掉该删除的空白页ID 【bug：0011479】wanghongjian add
				mWorkspaceScreens.remove( id );
				mScreenOrder.remove( id );
				if( indexOfChild( cl ) < currentPage )
				{
					pageShift++;
					if( !isLayoutRtl() )//zhujieping add，从右向左时，布局反方向，不需要做该处理
					{
						//<i_0010089> liuhailin@2015-04-02 modify begin
						isStripEmptyScreens = true;
						//删除中间页面的时候,后面的页面要移动至前面页的X坐标点的值。
						mPageShiftX = getViewportWidth() * ( currentPage - pageShift );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( "liuhailin" , "stripEmptyScreens isStripEmptyScreens = true" );
						//<i_0010089> liuhailin@2015-04-02 modify end
					}
				}
				removeView( cl );
				// zhujieping@2015/04/08 ADD START，mDragView被移除要置为空，i_0010895
				if( mDragView != null )
				{
					if( cl == mDragView )
					{
						mDragView = null;
					}
				}
				// zhujieping@2015/04/08 ADD END
			}
			else
			{
				// if this is the last non-custom content screen, convert it to the empty screen
				//当只有一页空白页的时候，此时不删除空白页，但不能将此空白页的ScreendId置位空白页的ID，该celllayout有大于0的ID 【bug：0011479】wanghongjian add
				//				mWorkspaceScreens.put( EXTRA_EMPTY_SCREEN_ID , cl );
				//				mScreenOrder.add( EXTRA_EMPTY_SCREEN_ID );
				//当只有一页空白页的时候，此时不删除空白页，但不能将此空白页的ScreendId置位空白页的ID，该celllayout有大于0的ID 【bug：0011479】wanghongjian end
			}
		}
		if( !removeScreens.isEmpty() )
		{
			// Update the model if we have changed any screens
			mLauncher.getModel().updateWorkspaceScreenOrder( mLauncher , mScreenOrder );
			//xiatian add start	//解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，再删除默认主页那个页面，这时默认主页始终为第0页且不和页面绑定”的问题。【i_0014576】
			if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
			{
				if( mIsDelDefaultPage )
				{
					int mChildCount = getChildCount();
					for( int i = 0 ; i < mChildCount ; i++ )
					{
						View mView = getChildAt( i );
						if( mView instanceof CellLayout && isFavoritesPageByPageIndex( i ) == false )
						{
							CellLayout mFirstCellLayout = (CellLayout)mView;
							mFirstCellLayout.setDefaultPage( true );
							int mDefaultPageIndex = i;
							//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
							//xiatian del start
							//					if( hasFavoritesPage() )
							//					{
							//						mDefaultPageIndex--;
							//					}
							//xiatian del end
							mDefaultPageIndex -= mFunctionPagesInNormalPageLeftNum;//xiatian add
							//xiatian end
							setDefaultPage( mDefaultPageIndex );
							break;
						}
					}
				}
			}
			//xiatian add end
		}
		//xiatian add start	//解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”前提下，加载item的时候，发现并删除位于功能页的前一页的空白默认页（默认页数大于桌面页面数）后，当前页面没有跳到默认页面（第0页）”的问题。
		if(
		//
		( isLoadFinish == false )
		//
		&& ( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
		//
		&& ( mIsDelDefaultPage )
		//
		)
		{
			setCurrentPage( getDefaultPageIndex() );
		}
		else
		//xiatian add end
		if( pageShift >= 0 )
		{
			int mCurrentPage = currentPage - pageShift;
			//xiatian add start	//解决“加载item的时候，发现并删除位于功能页的前一页的空白页后，当前页面没有跳到功能页前一页”的问题。
			if( isMusicPage( mCurrentPage ) )
			{
				mCurrentPage--;
				if( isCameraPage( mCurrentPage ) )
				{
					mCurrentPage--;
				}
			}
			else if( isCameraPage( mCurrentPage ) )
			{
				mCurrentPage--;
			}
			//xiatian add end
			setCurrentPage( mCurrentPage );
		}
		updateHomePageIndicator();//xiatian add	//解决“删除一整个页面（删除页面上最后一个应用图标或者插件）后，默认主页的图标没有及时更新”的问题。【i_0014851】
	}
	
	// See implementation for parameter definition.
	void addInScreen(
			View child ,
			long container ,
			long screenId ,
			int x ,
			int y ,
			int spanX ,
			int spanY )
	{
		addInScreen( child , container , screenId , x , y , spanX , spanY , false , false );
	}
	
	// At bind time, we use the rank (screenId) to compute x and y for hotseat items.
	// See implementation for parameter definition.
	public void addInScreenFromBind(
			View child ,
			long container ,
			long screenId ,
			int x ,
			int y ,
			int spanX ,
			int spanY )
	{
		addInScreen( child , container , screenId , x , y , spanX , spanY , false , true );
	}
	
	// See implementation for parameter definition.
	void addInScreen(
			View child ,
			long container ,
			long screenId ,
			int x ,
			int y ,
			int spanX ,
			int spanY ,
			boolean insert )
	{
		addInScreen( child , container , screenId , x , y , spanX , spanY , insert , false );
	}
	
	/**
	 * Adds the specified child in the specified screen. The position and dimension of
	 * the child are defined by x, y, spanX and spanY.
	 *
	 * @param child The child to add in one of the workspace's screens.
	 * @param screenId The screen in which to add the child.
	 * @param x The X position of the child in the screen's grid.
	 * @param y The Y position of the child in the screen's grid.
	 * @param spanX The number of cells spanned horizontally by the child.
	 * @param spanY The number of cells spanned vertically by the child.
	 * @param insert When true, the child is inserted at the beginning of the children list.
	 * @param computeXYFromRank When true, we use the rank (stored in screenId) to compute
	 *                          the x and y position in which to place hotseat items. Otherwise
	 *                          we use the x and y position to compute the rank.
	 */
	void addInScreen(
			View child ,
			long container ,
			long screenId ,
			int x ,
			int y ,
			int spanX ,
			int spanY ,
			boolean insert ,
			boolean computeXYFromRank )
	{
		if( container > 0 )//zjp,说明不是加到桌面的
		{
			return;
		}
		Object tag = child.getTag();
		if( container == LauncherSettings.Favorites.CONTAINER_DESKTOP )
		{
			if( getScreenWithId( screenId ) == null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "Skipping child, screenId " , screenId , " not found" ) );
				// DEBUGGING - Print out the stack trace to see where we are adding from
				new Throwable().printStackTrace();
				return;
			}
		}
		if( screenId == EXTRA_EMPTY_SCREEN_ID )
		{
			// This should never happen
			throw new RuntimeException( "Screen id should not be EXTRA_EMPTY_SCREEN_ID" );
		}
		final CellLayout layout;
		if( container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
		{
			layout = mLauncher.getHotseat().getLayout();
			child.setOnKeyListener( null );
			if( computeXYFromRank )
			{
				x = mLauncher.getHotseat().getCellXFromOrder( (int)screenId );
				y = mLauncher.getHotseat().getCellYFromOrder( (int)screenId );
			}
			else
			{
				screenId = mLauncher.getHotseat().getOrderInHotseat( x , y );
			}
		}
		else
		{
			layout = getScreenWithId( screenId );
			if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
			{
				child.setOnKeyListener( new IconKeyEventListener() );
			}
			//xiatian add start	//解决“关闭开关switch_enable_response_onkeylistener的前提下，adb模拟KEYCODE_DPAD_LEFT和KEYCODE_DPAD_RIGHT时，满足一定条件后：1、会切页；2、图标获取焦点”的问题。
			//【问题原因】
			//	1、adb模拟KEYCODE_DPAD_LEFT和KEYCODE_DPAD_RIGHT时，由于图标支持焦点，导致BubbleTextView.java会调用requestFocus
			//	2、从而调用ShortcutAndWidgetContainer.java的requestChildFocus方法：判断条件成立时，会给view画“焦点框”
			//	3、进而调用PagedView.java的requestChildFocus方法：判断条件成立时，导致桌面切页
			//【解决方案】图标不支持焦点
			else
			{
				child.setFocusable( false );
			}
			//xiatian add end
		}
		ViewGroup.LayoutParams genericLp = child.getLayoutParams();
		CellLayout.LayoutParams lp;
		if( genericLp == null || !( genericLp instanceof CellLayout.LayoutParams ) )
		{
			lp = new CellLayout.LayoutParams( x , y , spanX , spanY );
		}
		else
		{
			lp = (CellLayout.LayoutParams)genericLp;
			lp.cellX = x;
			lp.cellY = y;
			lp.cellHSpan = spanX;
			lp.cellVSpan = spanY;
		}
		if( spanX < 0 && spanY < 0 )
		{
			lp.isLockedToGrid = false;
		}
		// Get the canonical child id to uniquely represent this view in this screen
		int childId = LauncherModel.getCellLayoutChildId( container , screenId , x , y , spanX , spanY );
		boolean markCellsAsOccupied = !( child instanceof Folder );
		if( !layout.addViewToCellLayout( child , insert ? 0 : -1 , childId , lp , markCellsAsOccupied ) )
		{
			// TODO: This branch occurs when the workspace is adding views
			// outside of the defined grid
			// maybe we should be deleting these items from the LauncherModel?
			Launcher.addDumpLog( TAG , StringUtils.concat( "Failed to add to item at (" , lp.cellX , "," , lp.cellY , ") to CellLayout" ) , true );
			//cheyingkun add start	//解决“双层模式往满屏幕桌面拖一个应用时，偶现桌面重启”【c_0003088】
			if( isDragging() )
			{
				mDragController.cancelDrag();
			}
			return;
			//cheyingkun add end
		}
		if( !( child instanceof Folder ) )
		{
			child.setHapticFeedbackEnabled( false );
			child.setOnLongClickListener( mLongClickListener );
		}
		if( child instanceof DropTarget )
		{
			mDragController.addDropTarget( (DropTarget)child );
		}
	}
	
	/**
	 * Called directly from a CellLayout (not by the framework), after we've been added as a
	 * listener via setOnInterceptTouchEventListener(). This allows us to tell the CellLayout
	 * that it should intercept touch events, which is not something that is normally supported.
	 */
	@Override
	public boolean onTouch(
			View v ,
			MotionEvent event )
	{
		return ( isSmall() || !isFinishedSwitchingState() ) || ( !isSmall() && indexOfChild( v ) != mCurrentPage );
	}
	
	@Override
	public boolean onTouchEvent(
			MotionEvent ev )
	{
		switch( ev.getAction() & MotionEvent.ACTION_MASK )
		{
			case MotionEvent.ACTION_POINTER_UP:
				//								mMultiTouchStop = spacing( ev );
				//								// show previews
				//								multiTouch();
				//								mMultiTouchStart = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				isComputeWallpaperOffset = true;
				isMoving = true;
				if( nMoving > 8 )
				{
					isSampling = false;
					nMoving++;
				}
				//标记是否迂回移动
				if( ( ev.getX() - xTouch ) * xShift < 0 )
				{
					isRoundAbout = true;
				}
				xShift = ev.getX() - xTouch;
				xTouch = ev.getX();
				break;
			case MotionEvent.ACTION_UP:
				isMoving = false;
				isRoundAbout = false;
				isSampling = true;
				nMoving = 0;
				break;
		}
		return super.onTouchEvent( ev );
	}
	
	public boolean isSwitchingState()
	{
		return mIsSwitchingState;
	}
	
	/** This differs from isSwitchingState in that we take into account how far the transition
	 *  has completed. */
	public boolean isFinishedSwitchingState()
	{
		return !mIsSwitchingState || ( mTransitionProgress > 0.5f );
	}
	
	@Override
	protected void onWindowVisibilityChanged(
			int visibility )
	{
		mLauncher.onWindowVisibilityChanged( visibility );
	}
	
	@Override
	public boolean dispatchUnhandledMove(
			View focused ,
			int direction )
	{
		if( isSmall() || !isFinishedSwitchingState() )
		{
			// when the home screens are shrunken, shouldn't allow side-scrolling
			return false;
		}
		//cheyingkun add start	//解决“文件夹命名时，滑动搜狗输入键盘，页面会跟随滑动”的问题【i_0010171】
		Folder folder = getOpenFolder();
		if( folder != null && folder.isEditingName() )
		{
			return true;
		}
		//cheyingkun add end
		return super.dispatchUnhandledMove( focused , direction );
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		switch( ev.getAction() & MotionEvent.ACTION_MASK )
		{
			case MotionEvent.ACTION_DOWN:
				mXDown = ev.getX();
				mYDown = ev.getY();
				//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
				int index = -1;
				if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE && isInOverviewMode() )
				{
					for( int i = 0 ; i < getChildCount() ; i++ )
					{
						if( isCelllayoutTouchView( ev , getChildAt( i ) ) )
						{
							index = i;
							break;
						}
					}
					if( index >= 0 && index < getChildCount() && getChildAt( index ) != mExtraAddPageScreen )//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）。
					{
						CellLayout clCellLayout = (CellLayout)getChildAt( index );
						View view;
						if( clCellLayout != null )
						{
							view = clCellLayout.getHomeView();
							if( isCurrentTouchView( ev , view ) )
							{
								clCellLayout.setOverViewHomePage();
								return false;
							}
						}
					}
				}
				//gaominghui add end
				mTouchDownTime = System.currentTimeMillis();
				break;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
				if( mTouchState == TOUCH_STATE_REST )
				{
					final CellLayout currentPage = (CellLayout)getChildAt( mCurrentPage );
					if( !currentPage.lastDownOnOccupiedCell() )
					{
						onWallpaperTap( ev );
					}
				}
		}
		return super.onInterceptTouchEvent( ev );
	}
	
	protected void reinflateWidgetsIfNecessary()
	{
		final int clCount = getChildCount();
		for( int i = 0 ; i < clCount ; i++ )
		{
			CellLayout cl = (CellLayout)getChildAt( i );
			ShortcutAndWidgetContainer swc = cl.getShortcutsAndWidgets();
			final int itemCount = swc.getChildCount();
			for( int j = 0 ; j < itemCount ; j++ )
			{
				View v = swc.getChildAt( j );
				if( v.getTag() instanceof LauncherAppWidgetInfo )
				{
					LauncherAppWidgetInfo info = (LauncherAppWidgetInfo)v.getTag();
					LauncherAppWidgetHostView lahv = (LauncherAppWidgetHostView)info.getAppWidgetHostView();
					if( lahv != null && lahv.orientationChangedSincedInflation() )
					{
						mLauncher.removeAppWidget( info );
						// Remove the current widget which is inflated with the wrong orientation
						cl.removeView( lahv );
						mLauncher.bindAppWidget( info );
					}
				}
			}
		}
	}
	
	@Override
	protected void determineScrollingStart(
			MotionEvent ev )
	{
		if( !isFinishedSwitchingState() )
			return;
		float deltaX = ev.getX() - mXDown;
		float absDeltaX = Math.abs( deltaX );
		float absDeltaY = Math.abs( ev.getY() - mYDown );
		if( Float.compare( absDeltaX , 0f ) == 0//
				|| ( isInOverviewMode() && isReordering( true ) )//cheyingkun add	//解决“编辑模式下长安页面横向滑动时,所有界面跟着一起滑动”的问题
		)
			return;
		float slope = absDeltaY / absDeltaX;
		float theta = (float)Math.atan( slope );
		if( absDeltaX > mTouchSlop || absDeltaY > mTouchSlop )
		{
			cancelCurrentPageLongPress();
		}
		// zhujieping@2015/05/21 DEL START,支持custompage有回弹的效果
		//		boolean passRightSwipesToFavoritesPage = ( mTouchDownTime - mFavoritesPageShowTime ) > CUSTOM_CONTENT_GESTURE_DELAY;
		//boolean swipeInIgnoreDirection = isLayoutRtl() ? deltaX < 0 : deltaX > 0;
		//if( swipeInIgnoreDirection && getScreenIdForPageIndex( getCurrentPage() ) == CUSTOM_CONTENT_SCREEN_ID && passRightSwipesToFavoritesPage )
		//{
		//	// Pass swipes to the right to the custom content page.
		//	return;
		//}
		// zhujieping@2015/05/21 DEL END
		if( theta > MAX_SWIPE_ANGLE )
		{
			// Above MAX_SWIPE_ANGLE, we don't want to ever start scrolling the workspace
			return;
		}
		else if( theta > START_DAMPING_TOUCH_SLOP_ANGLE )
		{
			// Above START_DAMPING_TOUCH_SLOP_ANGLE and below MAX_SWIPE_ANGLE, we want to
			// increase the touch slop to make it harder to begin scrolling the workspace. This
			// results in vertically scrolling widgets to more easily. The higher the angle, the
			// more we increase touch slop.
			theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
			float extraRatio = (float)Math.sqrt( ( theta / ( MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE ) ) );
			super.determineScrollingStart( ev , 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio );
		}
		else
		{
			// Below START_DAMPING_TOUCH_SLOP_ANGLE, we don't do anything special
			super.determineScrollingStart( ev );
		}
	}
	
	protected void onPageBeginMoving()
	{
		super.onPageBeginMoving();
		//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		if( mCuboidEffect != null )
		{
			if( isLoop() )
			{
				mCuboidEffect.setMaxScroll( 1.0f );
				mCuboidEffect.setLoop( true );
			}
			else
			{
				mCuboidEffect.setMaxScroll( 0.5f );
				mCuboidEffect.setLoop( false );
			}
		}
		//xiatian add end
		if( isHardwareAccelerated() )
		{
			updateChildrenLayersEnabled( false );
		}
		else
		{
			if( mNextPage != INVALID_PAGE )
			{
				// we're snapping to a particular screen
				enableChildrenCache( mCurrentPage , mNextPage );
			}
			else
			{
				// this is when user is actively dragging a particular screen, they might
				// swipe it either left or right (but we won't advance by more than one screen)
				enableChildrenCache( mCurrentPage - 1 , mCurrentPage + 1 );
			}
		}
		// If we are not fading in adjacent screens, we still need to restore the alpha in case the
		// user scrolls while we are transitioning (should not affect dispatchDraw optimizations)
		if( !mWorkspaceFadeInAdjacentScreens )
		{
			for( int i = 0 ; i < getChildCount() ; ++i )
			{
				( (CellLayout)getPageAt( i ) ).setShortcutAndWidgetAlpha( 1f );
			}
		}
		// YANGTIANYU@2016/06/29 ADD START
		mLastPage = mCurrentPage;
		onMediaPagesBeginMoving();
		// YANGTIANYU@2016/06/29 ADD END
	}
	
	protected void onPageEndMoving()
	{
		super.onPageEndMoving();
		if( isHardwareAccelerated() )
		{
			updateChildrenLayersEnabled( false );
		}
		else
		{
			clearChildrenCache();
		}
		if( mDragController.isDragging() )
		{
			if( isSmall() )
			{
				// If we are in springloaded mode, then force an event to check if the current touch
				// is under a new page (to scroll to)
				mDragController.forceTouchMove();
			}
		}
		if( mDelayedResizeRunnable != null )
		{
			mDelayedResizeRunnable.run();
			mDelayedResizeRunnable = null;
		}
		if( mDelayedSnapToPageRunnable != null )
		{
			mDelayedSnapToPageRunnable.run();
			mDelayedSnapToPageRunnable = null;
		}
		if( mStripScreensOnPageStopMoving )
		{
			stripEmptyScreens();
			mStripScreensOnPageStopMoving = false;
		}
		// YANGTIANYU@2016/06/29 ADD START
		if( mLastPage != mCurrentPage )
		{
			onMediaPagesEndMoving();
		}
		// YANGTIANYU@2016/06/29 ADD END
		if( !FavoritesPageManager.isLoadFavoritesFinish && mCurrentPage == mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID ) )
		{
			mLauncher.initFavoritesPlugin();
		}
	}
	
	@Override
	protected void notifyPageSwitchListener()
	{
		//编辑模式下面,nextpage异常修正
		checkNextPageInOverViewModel();//cheyingkun add	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
		super.notifyPageSwitchListener();
		if( hasFavoritesPage() )
		{
			if( getNextPage() == 0 && !mIsFavoritesPageShowing )
			{
				mIsFavoritesPageShowing = true;
				if( mFavoritesPageCallbacks != null )
				{
					mFavoritesPageCallbacks.onShow();
					// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
					//				//WangLei start //bug:0010441 //当语音搜索图标不可用时，点击整个搜索框都相应onClickSearchButton
					//				//mLauncher.updateVoiceButtonProxyVisible( false );
					//				mLauncher.updateVoiceButtonProxyVisible( true ); //WangLei add
					//				//WangLei end
					// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
				}
				FavoritesPageManager.getInstance( this.getContext() ).onShow();//cheyingkun add	//phenix1.1稳定版移植酷生活
			}
			else if( getNextPage() != 0 && mIsFavoritesPageShowing )
			{
				mIsFavoritesPageShowing = false;
				if( mFavoritesPageCallbacks != null )
				{
					mFavoritesPageCallbacks.onHide();
					mLauncher.resetSearchDropTargetBarScroll();
					// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
					//				mLauncher.updateVoiceButtonProxyVisible( false );
					// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
				}
				FavoritesPageManager.getInstance( this.getContext() ).onHide();//cheyingkun add	//phenix1.1稳定版移植酷生活
			}
		}
		//编辑模式切页选中效果
		checkSelectedPageInOverView();//cheyingkun add	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
	}
	
	protected FavoritesPageCallbacks getFavoritesPageCallbacks()
	{
		return mFavoritesPageCallbacks;
	}
	
	protected void snapToPage(
			int whichPage ,
			Runnable r )
	{
		if( mDelayedSnapToPageRunnable != null )
		{
			mDelayedSnapToPageRunnable.run();
		}
		mDelayedSnapToPageRunnable = r;
		snapToPage( whichPage , SLOW_PAGE_SNAP_ANIMATION_DURATION );
	}
	
	protected void snapToScreenId(
			long screenId ,
			Runnable r )
	{
		snapToPage( getPageIndexForScreenId( screenId ) , r );
	}
	
	@TargetApi( 16 )
	class WallpaperOffsetInterpolator implements Choreographer.FrameCallback , IWallpaperOffsetInterpolator
	{
		
		float mFinalOffset = 0.0f;
		float mCurrentOffset = 0.5f; // to force an initial update
		boolean mWaitingForUpdate;
		Choreographer mChoreographer;
		Interpolator mInterpolator;
		boolean mAnimating;
		long mAnimationStartTime;
		float mAnimationStartOffset;
		private final int ANIMATION_DURATION = 250;
		//xiatian add start	//整理代码：关于“MIN_PARALLAX_PAGE_SPAN”
		//【备注】关于“MIN_PARALLAX_PAGE_SPAN”
		//1、长按抬起图标，导致添加“加号页”时，壁纸相对位移不变化
		//2、桌面有“加号”页的前提下：
		//	2.1、如果“可滑动的页面”的页面总数（包含“加号”页）大于等于三的时候，滑动到“加号”页时，壁纸不滑动
		//	2.2、如果“可滑动的页面”的页面总数（包含“加号”页）小于三的时候，滑动到“加号”页时，壁纸滑动
		//xiatian add end
		// Don't use all the wallpaper for parallax until you have at least this many pages
		private final int MIN_PARALLAX_PAGE_SPAN = 3;
		int mNumScreens;
		
		public WallpaperOffsetInterpolator()
		{
			mChoreographer = Choreographer.getInstance();
			mInterpolator = new DecelerateInterpolator( 1.5f );
			WallpaperOffsetManager.getInstance().setWallpaperOffsetInterpolator( this );
		}
		
		@Override
		public void doFrame(
				long frameTimeNanos )
		{
			updateOffset( false );
		}
		
		private void updateOffset(
				boolean force )
		{
			if( mWaitingForUpdate || force )
			{
				mWaitingForUpdate = false;
				if( computeScrollOffset() && mWindowToken != null )
				{
					try
					{
						setWallpaperOffsetSteps();
					}
					catch( IllegalArgumentException e )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.e( TAG , StringUtils.concat( "Error updating wallpaper offset: " , e.toString() ) );
					}
				}
			}
		}
		
		public boolean computeScrollOffset()
		{
			final float oldOffset = mCurrentOffset;
			if( mAnimating )
			{
				long durationSinceAnimation = System.currentTimeMillis() - mAnimationStartTime;
				float t0 = durationSinceAnimation / (float)ANIMATION_DURATION;
				float t1 = mInterpolator.getInterpolation( t0 );
				mCurrentOffset = mAnimationStartOffset + ( mFinalOffset - mAnimationStartOffset ) * t1;
				mAnimating = durationSinceAnimation < ANIMATION_DURATION;
			}
			else
			{
				mCurrentOffset = mFinalOffset;
			}
			if( Math.abs( mCurrentOffset - mFinalOffset ) > 0.0000001f )
			{
				scheduleUpdate();
			}
			if( Math.abs( oldOffset - mCurrentOffset ) > 0.0000001f )
			{
				return true;
			}
			return false;
		}
		
		private float wallpaperOffsetForCurrentScroll()
		{
			if( getChildCount() <= 1 )
			{
				return 0;
			}
			//<i_0010089> liuhailin@2015-04-03 modify begin
			if( isStripEmptyScreens )
			{
				//如果不是最后一页,则需要移动壁纸背景页面,最后一页将按照系统原来流程执行。
				if( getCurrentPage() + 1 < getChildCount() )
				{
					float tempOffset = Math.min( 1 , getScrollX() / (float)( getViewportWidth() * ( getChildCount() ) ) );
					//计算壁纸需要显示的位置比例
					tempOffset = Math.max( 0 , tempOffset );
					int firstIndex = 0;
					//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
					//移除launcher3关于壁纸滑动的逻辑：位于最左边的“CustomPage”不属于“可滑动的页面”（对于“可滑动的页面”，页面滑动时，壁纸也滑动）。
					//					if( hasFavoritesPage() )
					//					{
					//						firstIndex = 1;
					//					}
					//xiatian del end
					int lastIndex = getChildCount() - 1;
					if( isLayoutRtl() )
					{
						int temp = firstIndex;
						firstIndex = lastIndex;
						lastIndex = temp;
					}
					int firstPageScrollX = getScrollForPage( firstIndex );
					int scrollRange = getScrollForPage( lastIndex ) - firstPageScrollX;
					if( scrollRange == 0 )
					{
						return 0;
					}
					//按照页面的数量来计算当前页面的壁纸实际显示位置的比例。
					float result = getPageIndexIngoreLayoutDirection( getCurrentPage() ) * getViewportWidth() / (float)scrollRange;//zhujieping modify
					//如果超出实际比例,则需要使用实际比例,不然动画完成后将会看见一次壁纸回位的过程
					if( tempOffset <= result )
					{
						return result;
					}
					return tempOffset;
				}
			}
			//<i_0010089> liuhailin@2015-04-03 modify end
			int firstIndex = 0;
			//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			//移除launcher3关于壁纸滑动的逻辑：位于最左边的“CustomPage”不属于“可滑动的页面”（对于“可滑动的页面”，页面滑动时，壁纸也滑动）。
			//					if( hasFavoritesPage() )
			//					{//Exclude the leftmost page
			//						firstIndex = 1;
			//					}
			//xiatian del end
			// Exclude the last extra empty screen (if we have > MIN_PARALLAX_PAGE_SPAN pages)
			int lastIndex = getChildCount() - 1;
			//xiatian start	//整理代码：关于“MIN_PARALLAX_PAGE_SPAN”
			//xiatian del start
			//【备注】numEmptyScreensToIgnore这个方法，不符合方法的“功能单一性”这个这个原则（不应该将“ getChildCount() >= MIN_PARALLAX_PAGE_SPAN ”放到的其中）。
			//			lastIndex -= numEmptyScreensToIgnore();//xiatian del
			//xiatian del end
			//xiatian add start
			if(
			//
			hasExtraEmptyScreen()
			//
			&& ( getChildCount() >= MIN_PARALLAX_PAGE_SPAN )
			//
			)
			{
				lastIndex--;
			}
			//xiatian add end
			//xiatian end
			if( isLayoutRtl() )
			{
				int temp = firstIndex;
				firstIndex = lastIndex;
				lastIndex = temp;
			}
			int firstPageScrollX = getScrollForPage( firstIndex );
			int scrollRange = getScrollForPage( lastIndex ) - firstPageScrollX;
			if( scrollRange == 0 )
			{
				return 0;
			}
			else
			{
				// TODO: do different behavior if it's  a live wallpaper?
				// Sometimes the left parameter of the pages is animated during a layout transition;
				// this parameter offsets it to keep the wallpaper from animating as well
				int offsetForLayoutTransitionAnimation = isLayoutRtl() ? getPageAt( getChildCount() - 1 ).getLeft() - getFirstChildLeft() : 0;
				int adjustedScroll = getScrollX() - firstPageScrollX - offsetForLayoutTransitionAnimation;
				float offset = Math.min( 1 , adjustedScroll / (float)scrollRange );
				offset = Math.max( 0 , offset );
				// zhujieping@2015/05/04 ADD START,循环滑页时，壁纸跟随滑动
				if( isLoop() )
				{
					if( mOverScrollX < 0 )
					{
						offset = Math.abs( mOverScrollX ) * 1.0f / getViewportWidth();
					}
					else if( mOverScrollX > mMaxScrollX )
					{
						offset = Math.min( 1 , 1 - ( mOverScrollX - mMaxScrollX ) * 1.0f / getViewportWidth() );
					}
				}
				// zhujieping@2015/05/04 ADD END
				//xiatian del start	//整理代码：删除关于“MIN_PARALLAX_PAGE_SPAN”的冗余代码
				//【备注】该段代码，结果都是“offset*=1”，故删除。
				//				// Don't use up all the wallpaper parallax until you have at least
				//				// MIN_PARALLAX_PAGE_SPAN pages
				//				int numScrollingPages = getScrollingPagesNum();
				//				int parallaxPageSpan = Math.max( MIN_PARALLAX_PAGE_SPAN , numScrollingPages - 1 );
				//				// On RTL devices, push the wallpaper offset to the right if we don't have enough
				//				// pages (ie if numScrollingPages < MIN_PARALLAX_PAGE_SPAN)
				//				int padding = isLayoutRtl() ? parallaxPageSpan - numScrollingPages + 1 : 0;
				//				//xiatian start	//fix bug：解决“壁纸显示不正确”的问题。
				//				//【原因】offset不对
				//				//				offset *= (float)( ( padding + numScrollingPages - 1 ) / parallaxPageSpan );//xiatian del
				//				offset *= (float)( ( isLayoutRtl() == false ) ? 1 : ( ( padding + numScrollingPages - 1 ) / parallaxPageSpan ) );//xiatian add
				//				//xiatian end
				//xiatian del end
				return offset;
			}
		}
		//xiatian del start	//整理代码：关于“MIN_PARALLAX_PAGE_SPAN”
		//【备注】numEmptyScreensToIgnore这个方法，不符合方法的“功能单一性”这个这个原则（不应该将“ getChildCount() >= MIN_PARALLAX_PAGE_SPAN ”放到的其中）。
		//		private int numEmptyScreensToIgnore()
		//		{
		//			int numScrollingPages = getChildCount();
		//			//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//			//移除launcher3关于壁纸滑动的逻辑：位于最左边的“CustomPage”不属于“可滑动的页面”（对于“可滑动的页面”，页面滑动时，壁纸也滑动）。
		//			//			if( hasFavoritesPage() )
		//			//			{
		//			//				numScrollingPages--;
		//			//			}
		//			//xiatian del end
		//			if( numScrollingPages >= MIN_PARALLAX_PAGE_SPAN && hasExtraEmptyScreen() )
		//			{
		//				return 1;
		//			}
		//			else
		//			{
		//				return 0;
		//			}
		//		}
		//xiatian del end
		;
		
		private int getScrollingPagesNum()
		{
			int numScrollingPages = getChildCount();
			//xiatian start	//整理代码：关于“MIN_PARALLAX_PAGE_SPAN”
			//xiatian del start
			//【备注】numEmptyScreensToIgnore这个方法，不符合方法的“功能单一性”这个这个原则（不应该将“ getChildCount() >= MIN_PARALLAX_PAGE_SPAN ”放到的其中）。
			//			numScrollingPages -= numEmptyScreensToIgnore();//xiatian del
			//xiatian del end
			//xiatian add start
			if(
			//
			hasExtraEmptyScreen()
			//
			&& ( getChildCount() >= MIN_PARALLAX_PAGE_SPAN )
			//
			)
			{
				numScrollingPages--;
			}
			//xiatian add end
			//xiatian end
			//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			//移除launcher3关于壁纸滑动的逻辑：位于最左边的“CustomPage”不属于“可滑动的页面”（对于“可滑动的页面”，页面滑动时，壁纸也滑动）。
			//			if( hasFavoritesPage() )
			//			{
			//				numScrollingPages--;
			//			}
			//xiatian del end
			return numScrollingPages;
		}
		
		public void syncWithScroll()
		{
			float offset = wallpaperOffsetForCurrentScroll();
			if( mWallpaperOffset != null )
				mWallpaperOffset.setFinalX( offset );
			updateOffset( true );
		}
		
		public float getCurrX()
		{
			return mCurrentOffset;
		}
		
		public float getFinalX()
		{
			return mFinalOffset;
		}
		
		private void animateToFinal()
		{
			mAnimating = true;
			mAnimationStartOffset = mCurrentOffset;
			mAnimationStartTime = System.currentTimeMillis();
		}
		
		private void setWallpaperOffsetSteps()
		{
			// Set wallpaper offset steps (1 / (number of screens - 1))
			//cheyingkun add start	//是否支持壁纸滑动。true为支持；false为不支持。默认true。
			// gaominghui@2017/01/09 UPD START
			//if( WallpaperManagerBase.disable_move_wallpaper )
			if( WallpaperManagerBase.get_disableMoveWallpaper() )
			// gaominghui@2017/01/09 UPD END
			{
				mWallpaperManager.setWallpaperOffsetSteps( 0.5f , 0 );
				mWallpaperManager.setWallpaperOffsets( mWindowToken , 0.5f , 0 );
			}
			else
			//cheyingkun add ends
			{
				if( mWallpaperOffset != null )
					mWallpaperManager.setWallpaperOffsets( mWindowToken , mWallpaperOffset.getCurrX() , 0.5f );
				mWallpaperManager.setWallpaperOffsetSteps( 1.0f / ( getChildCount() - 1 ) , 1.0f );
			}
		}
		
		public void setFinalX(
				float x )
		{
			scheduleUpdate();
			mFinalOffset = Math.max( 0f , Math.min( x , 1.0f ) );
			int mScrollingPagesNum = getScrollingPagesNum();
			if( mScrollingPagesNum != mNumScreens )
			{
				if( mNumScreens > 0 )
				{
					// Don't animate if we're going from 0 screens
					animateToFinal();
				}
				mNumScreens = mScrollingPagesNum;
			}
		}
		
		private void scheduleUpdate()
		{
			if( !mWaitingForUpdate )
			{
				mChoreographer.postFrameCallback( this );
				mWaitingForUpdate = true;
			}
		}
		
		public void jumpToFinal()
		{
			mCurrentOffset = mFinalOffset;
		}
		
		@Override
		public float getWallpaperXOffset()
		{
			int width = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAvailableWidthPx();
			return mCurrentOffset * width;
		}
		
		@Override
		public float getWallpaperYOffset()
		{
			return 0;
		}
	}
	
	@Override
	public void computeScroll()
	{
		super.computeScroll();
		// zhangjin@2015/09/10 UPD START
		//mWallpaperOffset.syncWithScroll();
		this.post( new Runnable() {
			
			@Override
			public void run()
			{
				if( mWallpaperOffset != null )
					mWallpaperOffset.syncWithScroll();
			}
		} );
		// zhangjin@2015/09/10 UPD END
		//<i_0010089> liuhailin@2015-04-03 modify begin
		//当删除页面后,后面一页移动至指定坐标点(mPageShiftX)后将标志位复位
		if( getScrollX() == mPageShiftX )
		{
			isStripEmptyScreens = false;
		}
		//<i_0010089> liuhailin@2015-04-03 modify end
	}
	
	void showOutlines()
	{
		if( !isSmall() && !mIsSwitchingState )
		{
			if( mChildrenOutlineFadeOutAnimation != null )
				mChildrenOutlineFadeOutAnimation.cancel();
			if( mChildrenOutlineFadeInAnimation != null )
				mChildrenOutlineFadeInAnimation.cancel();
			mChildrenOutlineFadeInAnimation = LauncherAnimUtils.ofFloat( this , "childrenOutlineAlpha" , 1.0f );
			mChildrenOutlineFadeInAnimation.setDuration( CHILDREN_OUTLINE_FADE_IN_DURATION );
			mChildrenOutlineFadeInAnimation.start();
		}
	}
	
	void hideOutlines()
	{
		if( !isSmall() && !mIsSwitchingState )
		{
			if( mChildrenOutlineFadeInAnimation != null )
				mChildrenOutlineFadeInAnimation.cancel();
			if( mChildrenOutlineFadeOutAnimation != null )
				mChildrenOutlineFadeOutAnimation.cancel();
			mChildrenOutlineFadeOutAnimation = LauncherAnimUtils.ofFloat( this , "childrenOutlineAlpha" , 0.0f );
			mChildrenOutlineFadeOutAnimation.setDuration( CHILDREN_OUTLINE_FADE_OUT_DURATION );
			mChildrenOutlineFadeOutAnimation.setStartDelay( CHILDREN_OUTLINE_FADE_OUT_DELAY );
			mChildrenOutlineFadeOutAnimation.start();
		}
	}
	
	// zhujieping@2015/03/26 ADD START
	void stopOutlinesFadeOuntAnimation()
	{
		if( mChildrenOutlineFadeOutAnimation != null && mChildrenOutlineFadeOutAnimation.isRunning() )
			mChildrenOutlineFadeOutAnimation.cancel();
	}
	
	// zhujieping@2015/03/26 ADD END
	// zhangjin@2015/08/04 ADD START i_11917
	void stopOutlinesFadeInAnimation()
	{
		if( mChildrenOutlineFadeInAnimation != null && mChildrenOutlineFadeInAnimation.isRunning() )
			mChildrenOutlineFadeInAnimation.cancel();
	}
	
	// zhangjin@2015/08/04 ADD END
	public void showOutlinesTemporarily()
	{
		if( !mIsPageMoving && !isTouchActive() )
		{
			snapToPage( mCurrentPage );
		}
	}
	
	public void setChildrenOutlineAlpha(
			float alpha )
	{
		mChildrenOutlineAlpha = alpha;
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			CellLayout cl = (CellLayout)getChildAt( i );
			cl.setBackgroundAlpha( alpha );
		}
	}
	
	public float getChildrenOutlineAlpha()
	{
		return mChildrenOutlineAlpha;
	}
	
	void disableBackground()
	{
		mDrawBackground = false;
	}
	
	void enableBackground()
	{
		mDrawBackground = true;
	}
	
	private void animateBackgroundGradient(
			float finalAlpha ,
			boolean animated )
	{
		if( mBackground == null )
			return;
		if( mBackgroundFadeInAnimation != null )
		{
			mBackgroundFadeInAnimation.cancel();
			mBackgroundFadeInAnimation = null;
		}
		if( mBackgroundFadeOutAnimation != null )
		{
			mBackgroundFadeOutAnimation.cancel();
			mBackgroundFadeOutAnimation = null;
		}
		float startAlpha = getBackgroundAlpha();
		if( finalAlpha != startAlpha )
		{
			if( animated )
			{
				mBackgroundFadeOutAnimation = LauncherAnimUtils.ofFloat( this , startAlpha , finalAlpha );
				mBackgroundFadeOutAnimation.addUpdateListener( new AnimatorUpdateListener() {
					
					public void onAnimationUpdate(
							ValueAnimator animation )
					{
						setBackgroundAlpha( ( (Float)animation.getAnimatedValue() ).floatValue() );
					}
				} );
				mBackgroundFadeOutAnimation.setInterpolator( new DecelerateInterpolator( 1.5f ) );
				mBackgroundFadeOutAnimation.setDuration( BACKGROUND_FADE_OUT_DURATION );
				mBackgroundFadeOutAnimation.start();
			}
			else
			{
				setBackgroundAlpha( finalAlpha );
			}
		}
	}
	
	public void setBackgroundAlpha(
			float alpha )
	{
		if( alpha != mBackgroundAlpha )
		{
			mBackgroundAlpha = alpha;
			invalidate();
		}
	}
	
	public float getBackgroundAlpha()
	{
		return mBackgroundAlpha;
	}
	
	float backgroundAlphaInterpolator(
			float r )
	{
		float pivotA = 0.1f;
		float pivotB = 0.4f;
		if( r < pivotA )
		{
			return 0;
		}
		else if( r > pivotB )
		{
			return 1.0f;
		}
		else
		{
			return ( r - pivotA ) / ( pivotB - pivotA );
		}
	}
	
	private void updatePageAlphaValues(
			int screenCenter )
	{
		boolean isInOverscroll = mOverScrollX < 0 || mOverScrollX > mMaxScrollX;
		if( mWorkspaceFadeInAdjacentScreens && mState == State.NORMAL && !mIsSwitchingState && !isInOverscroll )
		{
			for( int i = 0 ; i < getChildCount() ; i++ )
			{
				CellLayout child = (CellLayout)getChildAt( i );
				if( child != null )
				{
					float scrollProgress = getScrollProgress( screenCenter , child , i );
					float alpha = 1 - Math.abs( scrollProgress );
					child.getShortcutsAndWidgets().setAlpha( alpha );
					if( !mIsDragOccuring )
					{
						child.setBackgroundAlphaMultiplier( backgroundAlphaInterpolator( Math.abs( scrollProgress ) ) );
					}
					else
					{
						child.setBackgroundAlphaMultiplier( 1f );
					}
				}
			}
		}
	}
	
	private void setChildrenBackgroundAlphaMultipliers(
			float a )
	{
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			CellLayout child = (CellLayout)getChildAt( i );
			child.setBackgroundAlphaMultiplier( a );
		}
	}
	
	public boolean hasFavoritesPage()
	{
		return( mScreenOrder.size() > 0 && mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID ) != -1 /*mScreenOrder.get( 0 ) == CUSTOM_CONTENT_SCREEN_ID */);//新闻页有在左右的两种情况，zhujieping modify
	}
	
	public boolean isOnOrMovingToFavoritesPage()
	{
		return hasFavoritesPage() && getNextPage() == 0;
	}
	
	//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	//	private void updateStateForFavoritesPage(
	//			int screenCenter )
	//	{
	//		float translationX = 0;
	//		float progress = 0;
	//		if( hasFavoritesPage() )
	//		{
	//			int index = mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
	//			int scrollDelta = getScrollX() - getScrollForPage( index ) - getLayoutTransitionOffsetForPage( index );
	//			float scrollRange = getScrollForPage( index + 1 ) - getScrollForPage( index );
	//			if( isLoop() && ( mOverScrollX < 0 || mOverScrollX > mMaxScrollX ) )
	//			{
	//				if( isLayoutRtl() )
	//				{
	//					scrollRange = Math.abs( scrollRange );
	//					translationX = scrollRange - Math.abs( mOverScrollX + scrollRange ) % scrollRange;
	//				}
	//				else
	//				{
	//					translationX = -Math.abs( mOverScrollX + scrollRange ) % scrollRange;
	//				}
	//				progress = Math.abs( ( translationX ) / scrollRange );
	//			}
	//			else
	//			{
	//				translationX = scrollRange - scrollDelta;
	//				if( !isLoop() && mOverScrollX < 0 )//zhujieping add,新闻页回弹，背景透明度不需要变化
	//				{
	//					//cheyingkun start	//解决“酷生活页向左滑动，桌面变亮”的问题。【c_0004171】
	//					//						progress = -1;//cheyingkun del
	//					progress = mFavoritesPageScrollProgressLast;//cheyingkun add
	//					//cheyingkun end
	//				}
	//				else
	//				{
	//					progress = ( scrollRange - scrollDelta ) / scrollRange;
	//				}
	//				if( isLayoutRtl() )
	//				{
	//					translationX = Math.min( 0 , translationX );
	//				}
	//				else
	//				{
	//					translationX = Math.max( 0 , translationX );
	//				}
	//				progress = Math.max( 0 , progress );
	//			}
	//		}
	//		if( Float.compare( progress , mFavoritesPageScrollProgressLast ) == 0 )
	//			return;
	//		CellLayout cc = mWorkspaceScreens.get( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
	//		if( progress > 0 && cc.getVisibility() != VISIBLE && !isSmall() )
	//		{
	//			cc.setVisibility( VISIBLE );
	//			//cheyingkun add start	//phenix1.1稳定版移植酷生活
	//			if( hasFavoritesPage() )
	//			{
	//				FavoritesPageManager.getInstance( this.getContext() ).onShow();
	//			}
	//			//cheyingkun add end
	//		}
	//		mFavoritesPageScrollProgressLast = progress;
	//		setBackgroundAlpha( progress * 0.8f );
	//		if( mLauncher.getHotseat() != null )
	//		{
	//			mLauncher.getHotseat().setTranslationX( translationX );
	//		}
	//		if( getPageIndicator() != null )
	//		{
	//			getPageIndicator().setTranslationX( translationX );
	//		}
	//		if( !isFavoritesPageShowSearch() )
	//		{
	//			if( !( isLoop() && ( mOverScrollX <= 0 || mOverScrollX >= mMaxScrollX ) && ( hasMusicPage() || hasCameraPage() ) ) )
	//			{
	//				mLauncher.getSearchDropTargetBar().setTranslationX( translationX );
	//			}
	//		}
	//		if( mFavoritesPageCallbacks != null )
	//		{
	//			mFavoritesPageCallbacks.onScrollProgressChanged( progress );
	//		}
	//	}
	//xiatian del end
	@Override
	protected OnClickListener getPageIndicatorClickListener()
	{
		//【备注】
		//	1、为啥“支持辅助模式时，点击页面指示器不进入编辑模式”？
		//	2、后续添加配置项“是否支持点击页面指示器进入编辑模式”。
		AccessibilityManager am = (AccessibilityManager)getContext().getSystemService( Context.ACCESSIBILITY_SERVICE );
		if( !am.isTouchExplorationEnabled() )
		{
			return null;
		}
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				enterOverviewMode();
			}
		};
		return listener;
	}
	
	public void screenScrolledStandardUI(
			int screenCenter )
	{
		if( mState == State.SPRING_LOADED )
		{
			return;
		}
		if( mCurentAnimInfo == null )
		{
			return;
		}
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			View v = getPageAt( i );
			if( v != null )
			{
				v.setRotationX( 0f );
				v.setRotationY( 0f );
				v.setTranslationX( 0f );
				v.setRotation( 0f );
				v.setAlpha( 1.0f );
			}
		}
	}
	
	//	protected float getScrollProgress(
	//			int screenCenter ,
	//			View v ,
	//			int page )
	//	{
	//		final int halfScreenSize = getViewportWidth() / 2;
	//		int delta = screenCenter - ( getScrollForPage( page ) + halfScreenSize );
	//		int count = getChildCount();
	//		final int totalDistance;
	//		int adjacentPage = page + 1;
	//		if( ( delta < 0 && !isLayoutRtl() ) || ( delta > 0 && isLayoutRtl() ) )
	//		{
	//			adjacentPage = page - 1;
	//		}
	//		if( adjacentPage < 0 || adjacentPage > count - 1 )
	//		{
	//			totalDistance = v.getMeasuredWidth() + mPageSpacing;
	//		}
	//		else
	//		{
	//			totalDistance = Math.abs( getScrollForPage( adjacentPage ) - getScrollForPage( page ) );
	//		}
	//		float scrollProgress = delta / ( totalDistance * 1.0f );
	//		scrollProgress = Math.min( scrollProgress , getMaxScrollProgress() );
	//		scrollProgress = Math.max( scrollProgress , -getMaxScrollProgress() );
	//		return scrollProgress;
	//	}
	//	public void screenScrolledStandardUI2(
	//			int screenCenter )
	//	{
	//		int curPage = ( this.getScrollX() + halfWidthScreen ) / widthScreen;
	//		xCurPageCenter = getScrollForPage( curPage ) + halfWidthScreen;
	//		isRtl = ( screenCenter - xCurPageCenter > 0 ) ? true : false;
	//		int nextPage = isRtl ? curPage + 1 : curPage - 1;
	//		if( isSampling || isRoundAbout )
	//		{
	//			//计算切页初始数据
	//			curView = (IEffect)getPageAt( curPage );
	//			if( curView != null )
	//			{
	//				nextView = (IEffect)getPageAt( nextPage );
	//				pageWidth = getScaledMeasuredWidth( curView );
	//				pageHeight = curView.getMeasuredHeight();
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
	//				if( nextView != null && screenCenter <= xCenterLastPage )
	//				{
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , false , false );
	//					mCurentAnimInfo.getTransformationMatrix(
	//							nextView ,
	//							percentageScroll - 1 ,
	//							widthScreen ,
	//							pageHeight ,
	//							mDensity * CAMERA_DISTANCE ,
	//							screenCenter - xCenterLastPage > 0 ,
	//							true ,
	//							false );
	//				}
	//				else
	//				{
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , false , true );
	//				}
	//			}
	//			else
	//			{
	//				if( nextView != null && screenCenter >= xCenterFirstPage )
	//				{
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , false , false );
	//					mCurentAnimInfo.getTransformationMatrix(
	//							nextView ,
	//							percentageScroll + 1 ,
	//							widthScreen ,
	//							pageHeight ,
	//							mDensity * CAMERA_DISTANCE ,
	//							screenCenter - xCenterFirstPage < 0 ,
	//							true ,
	//							false );
	//				}
	//				else
	//				{
	//					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , false , true );
	//				}
	//			}
	//		}
	//	}
	// zhujieping@2015/05/28 ADD START，控制特效，与pagedview不一样的地方时FavoritesPage执行的是标准特效
	protected void screenScrolledWithFavoritesPage(
			int screenCenter )
	{
		changeChildAlpha( screenCenter );
		// zhujieping@2015/06/09 UPD START
		int tempPage = ( this.getScrollX() + halfWidthScreen ) / widthScreen;
		int curPage = getPageIndexIngoreLayoutDirection( tempPage );
		xCurPageCenter = getScrollForPage( curPage ) + halfWidthScreen;
		isRtl = ( screenCenter - xCurPageCenter > 0 ) ? true : false;
		int nextPage = isRtl ? tempPage + 1 : tempPage - 1;
		// zhujieping@2015/06/09 UPD END
		if( isLoop() )
		{
			if( nextPage >= getPageCount() )
			{
				nextPage = 0;
			}
			if( nextPage < 0 )
			{
				nextPage = getPageCount() - 1;
			}
			int pre = getPrePageIndex( curPage );
			mCurentAnimInfo.stopCellLayoutChildTransformation( getChildAt( pre ) );
			//chenliang add start	//解决“在第一页为普通页面，最后一页为功能页，并开启桌面循环切页的前提下，快速的从普通页滑到最后一页，再滑到第一页时，应用图标不及时显示”的问题。【i_0014985】
			if( isFunctionPageByPageIndex( curPage ) && !isFunctionPageByPageIndex( nextPage ) )
			{
				if( mCurentAnimInfo.mAllEffectViews.isEmpty() == false )
				{
					mCurentAnimInfo.stopEffecf();
				}
			}
			//chenliang add end
		}
		nextPage = getPageIndexIngoreLayoutDirection( nextPage );
		//计算切页初始数据
		curView = (IEffect)getPageAt( curPage );
		if( curView != null )
		{
			// zhangjin@2015/09/08 ADD START nextView 为空时，先清除上一次的数据
			if( nextPage == -1 && nextView != null )
			{
				if( mCurentAnimInfo != null )
				{
					mCurentAnimInfo.stopEffecf();
				}
			}
			// zhangjin@2015/09/08 ADD END
			nextView = (IEffect)getPageAt( nextPage );
			if( pageWidth == 0 || pageHeight == 0 )
			{
				pageWidth = getScaledMeasuredWidth( curView );
				pageHeight = curView.getMeasuredHeight();
			}
		}
		if( curView != null )
		{
			// zhangjin@2015/09/08 UPD START
			//curView.setCameraDistance( mDensity * CAMERA_DISTANCE );
			// gaominghui@2016/12/14 ADD START 兼容android 4.0
			if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
			{
				if( curView.getCameraDistance() != mDensity * CAMERA_DISTANCE )
				{
					curView.setCameraDistance( mDensity * CAMERA_DISTANCE );
				}
			}
			else
			{
				curView.setCameraDistance( mDensity * CAMERA_DISTANCE );
			}
			// gaominghui@2016/12/14 ADD END 兼容android 4.0
			// zhangjin@2015/09/08 UPD END
			//计算滑动百分比:利用可见视窗中轴和当前页中轴差值
			int delta = screenCenter - xCurPageCenter;
			float percentageScroll = delta / ( widthScreen * 1.0f );
			if( percentageScroll != 0 )
			{
				setStartEffectEnd( false );
			}
			//切页过程动画实现
			boolean isFunctionPageOrToFunctionPage = ( isFunctionPageByPageIndex( curPage ) || isFunctionPageByPageIndex( nextPage ) );
			if( isRtl )
			{
				if( nextView != null && screenCenter <= xCenterLastPage )
				{
					EffectInfo effectInfo;
					if( isFunctionPageOrToFunctionPage )
					{
					//yangmengchao add start //添加配置项“switch_enable_effect_in_function_pages”，功能页是否支持切页特效。true为支持；false为不支持。默认为false。
						if( LauncherDefaultConfig.SWITCH_ENABLE_EFFECT_IN_FUNCTION_PAGES )
						{
							effectInfo = mCurentAnimInfo;
						}
						else
						//yangmengchao add end
						{
							effectInfo = mCuboidEffect;
						}
					}
					else
					{
						effectInfo = mCurentAnimInfo;
					}
					effectInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , false , false );
					effectInfo.getTransformationMatrix( nextView , percentageScroll - 1 , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , true , false );
				}
				else
				{
					EffectInfo effectInfo;
					if( isFunctionPageOrToFunctionPage )
					{
					//yangmengchao add start //添加配置项“switch_enable_effect_in_function_pages”，功能页是否支持切页特效。true为支持；false为不支持。默认为false。
						if( LauncherDefaultConfig.SWITCH_ENABLE_EFFECT_IN_FUNCTION_PAGES )
						{
							effectInfo = mCurentAnimInfo;
						}
						else
						//yangmengchao add end
						{
							effectInfo = mCuboidEffect;
						}
						 	
					}
					else
					{
						effectInfo = mCurentAnimInfo;
					}
					float temp;
					boolean isEffect;
					boolean overscroll;
					if( ( isLoop() && ( mOverScrollX + getViewportWidth() < 0 || mOverScrollX - mMaxScrollX > getViewportWidth() ) && getChildCount() == 2 ) )
					{
						temp = percentageScroll - 2;
						isEffect = false;
						overscroll = false;
					}
					else
					{
						temp = percentageScroll;
						isEffect = mIsFirstOrLastEffect;
						overscroll = screenCenter - xCenterLastPage > 0;
					}
					effectInfo.getTransformationMatrix( curView , temp , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , overscroll , false , isEffect );
					if( isLoop() && nextView != null )
					{
						effectInfo.getTransformationMatrix( nextView , percentageScroll - 1 , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , false , true , false );
					}
				}
			}
			else
			{
				if( nextView != null && screenCenter >= xCenterFirstPage )
				{
					EffectInfo effectInfo;
					if( isFunctionPageOrToFunctionPage )
					{
					//yangmengchao add start //添加配置项“switch_enable_effect_in_function_pages”，功能页是否支持切页特效。true为支持；false为不支持。默认为false。
						if( LauncherDefaultConfig.SWITCH_ENABLE_EFFECT_IN_FUNCTION_PAGES )
						{
							effectInfo = mCurentAnimInfo;
						}
						else
						//yangmengchao add end
						{
							effectInfo = mCuboidEffect;
						}
							
					}
					else
					{
						effectInfo = mCurentAnimInfo;
					}
					effectInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , false , false );
					effectInfo.getTransformationMatrix( nextView , percentageScroll + 1 , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , true , false );
				}
				else
				{
					EffectInfo effectInfo;
					if( isFunctionPageOrToFunctionPage )
					{
					//yangmengchao add start //添加配置项“switch_enable_effect_in_function_pages”，功能页是否支持切页特效。true为支持；false为不支持。默认为false。
						if( LauncherDefaultConfig.SWITCH_ENABLE_EFFECT_IN_FUNCTION_PAGES )
						{
							effectInfo = mCurentAnimInfo;
						}
						else
						//yangmengchao add end 
						{
							effectInfo = mCuboidEffect;
						}
						
					}
					else
					{
						effectInfo = mCurentAnimInfo;
					}
					float temp;
					boolean isEffect;
					boolean overscroll;
					if( ( isLoop() && ( mOverScrollX + getViewportWidth() < 0 || mOverScrollX - mMaxScrollX > getViewportWidth() ) && getChildCount() == 2 ) )
					{
						temp = 2 + percentageScroll;
						isEffect = false;
						overscroll = false;
					}
					else
					{
						temp = percentageScroll;
						isEffect = mIsFirstOrLastEffect;
						overscroll = screenCenter - xCenterFirstPage < 0;
					}
					effectInfo.getTransformationMatrix( curView , temp , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , overscroll , false , isEffect );
					if( isLoop() && nextView != null )
					{
						effectInfo.getTransformationMatrix( nextView , percentageScroll + 1 , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , false , true , false );
					}
				}
			}
			//zhujieping add start //两个手指连续滑动，当前两页都不是媒体页时要置位
			if( !isFunctionPageOrToFunctionPage )
			{
				if( mCuboidEffect != null )
				{
					mCuboidEffect.stopEffecf();
				}
			}
			//zhujieping add end
			if( percentageScroll == 0 && mTouchState != TOUCH_STATE_SCROLLING )
			{
				stopEffecf();
			}
		}
	}
	
	// zhujieping@2015/05/28 ADD END
	@Override
	protected void screenScrolled(
			int screenCenter )
	{
		if( mLastScreenCenter == -1 )
		{
			screenScrolledStandardUI( screenCenter );//初始加载所有页
		}
		else
		{
			// zhangjin@2015/07/31 修改问题，在双层模式下，切页有特效 UPD START
			//if( mState != State.OVERVIEW )//在编辑模式下不要执行特效
			//	//				super.screenScrolled( screenCenter );
			//	//			screenScrolledStandardUI2( screenCenter ); //切页特效实现
			//	screenScrolledWithFavoritesPage( screenCenter );//当滑动时有新闻页时，不支持特效（即特效为标准）zhujieping add
			if( isSmall() == false )
			{
				screenScrolledWithFavoritesPage( screenCenter );//当滑动时有新闻页时，不支持特效（即特效为标准）zhujieping add
			}
			// zhangjin@2015/07/31 UPD END
		}
		updatePageAlphaValues( screenCenter );
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//xiatian del start
		//		// zhujieping@2015/05/26 ADD START
		//		//滑动到新闻页时，底边栏、页面指示器以及搜索框等不显示，并随着页面移动
		//		updateStateForFavoritesPage( screenCenter );
		//		// zhujieping@2015/05/26 ADD END
		//		// YANGTIANYU@2016/06/21 ADD START
		//		// 滑动到第一个专属页时,不需要显示桌面上的搜索框
		//		updateStateForMediaPage();
		//		// YANGTIANYU@2016/06/21 ADD END
		//xiatian del end
		updateStateForFunctionPages();//xiatian add
		//xiatian end
		//		final boolean isRtl = isLayoutRtl();
		//		super.screenScrolled( screenCenter );
		//		updatePageAlphaValues( screenCenter );
		//		updateStateForFavoritesPage( screenCenter );
		//		enableHwLayersOnVisiblePages();
		//		boolean shouldOverScroll = ( mOverScrollX < 0 && ( !hasFavoritesPage() || isLayoutRtl() ) ) || ( mOverScrollX > mMaxScrollX && ( !hasFavoritesPage() || !isLayoutRtl() ) );
		//		if( shouldOverScroll )
		//		{
		//			int index = 0;
		//			float pivotX = 0f;
		//			final float leftBiasedPivot = 0.25f;
		//			final float rightBiasedPivot = 0.75f;
		//			final int lowerIndex = 0;
		//			final int upperIndex = getChildCount() - 1;
		//			final boolean isLeftPage = mOverScrollX < 0;
		//			index = ( !isRtl && isLeftPage ) || ( isRtl && !isLeftPage ) ? lowerIndex : upperIndex;
		//			pivotX = isLeftPage ? rightBiasedPivot : leftBiasedPivot;
		//			CellLayout cl = (CellLayout)getChildAt( index );
		//			float scrollProgress = getScrollProgress( screenCenter , cl , index );
		//			cl.setOverScrollAmount( Math.abs( scrollProgress ) , isLeftPage );
		//			float rotation = -WORKSPACE_OVERSCROLL_ROTATION * scrollProgress;
		//			cl.setRotationY( rotation );
		//			if( !mOverscrollTransformsSet || Float.compare( mLastOverscrollPivotX , pivotX ) != 0 )
		//			{
		//				mOverscrollTransformsSet = true;
		//				mLastOverscrollPivotX = pivotX;
		//				cl.setCameraDistance( mDensity * mCameraDistance );
		//				cl.setPivotX( cl.getMeasuredWidth() * pivotX );
		//				cl.setPivotY( cl.getMeasuredHeight() * 0.5f );
		//				cl.setOverscrollTransformsDirty( true );
		//			}
		//		}
		//		else
		//		{
		//			if( mOverscrollTransformsSet )
		//			{
		//				mOverscrollTransformsSet = false;
		//				( (CellLayout)getChildAt( 0 ) ).resetOverscrollTransforms();
		//				( (CellLayout)getChildAt( getChildCount() - 1 ) ).resetOverscrollTransforms();
		//			}
		//		}
	}
	
	//	@Override
	//	protected void overScroll(
	//			float amount )
	//	{
	//		acceleratedOverScroll( amount );
	//	}
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		mWindowToken = getWindowToken();
		computeScroll();
		mDragController.setWindowToken( mWindowToken );
	}
	
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		mWindowToken = null;
	}
	
	protected void onResume()
	{
		if( getPageIndicator() != null )
		{
			//【备注】
			//	1、为啥“支持辅助模式时，点击页面指示器不进入编辑模式”？
			//	2、后续添加配置项“是否支持点击页面指示器进入编辑模式”。
			// In case accessibility state has changed, we need to perform this on every
			// attach to window
			OnClickListener listener = getPageIndicatorClickListener();
			if( listener != null )
			{
				getPageIndicator().setOnClickListener( listener );
			}
		}
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		if( mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount() )
		{
			// gaominghui@2016/12/14 ADD START 兼容android 4.0
			if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
			{
				mWallpaperOffset.syncWithScroll();
				mWallpaperOffset.jumpToFinal();
			}
			// gaominghui@2016/12/14 ADD END 兼容android 4.0
		}
		super.onLayout( changed , left , top , right , bottom );
	}
	
	@Override
	protected void onDraw(
			Canvas canvas )
	{
		// Draw the background gradient if necessary
		if( mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground )
		{
			int alpha = (int)( mBackgroundAlpha * 255 );
			mBackground.setAlpha( alpha );
			mBackground.setBounds( getScrollX() , 0 , getScrollX() + getMeasuredWidth() , getMeasuredHeight() );
			mBackground.draw( canvas );
		}
		super.onDraw( canvas );
		// Call back to LauncherModel to finish binding after the first draw
		post( mBindPages );
	}
	
	boolean isDrawingBackgroundGradient()
	{
		return( mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground );
	}
	
	@Override
	protected boolean onRequestFocusInDescendants(
			int direction ,
			Rect previouslyFocusedRect )
	{
		if( !mLauncher.isAllAppsVisible() )
		{
			final Folder openFolder = getOpenFolder();
			if( openFolder != null )
			{
				return openFolder.requestFocus( direction , previouslyFocusedRect );
			}
			else
			{
				return super.onRequestFocusInDescendants( direction , previouslyFocusedRect );
			}
		}
		return false;
	}
	
	@Override
	public int getDescendantFocusability()
	{
		if( isSmall() )
		{
			return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
		}
		return super.getDescendantFocusability();
	}
	
	@Override
	public void addFocusables(
			ArrayList<View> views ,
			int direction ,
			int focusableMode )
	{
		if( !mLauncher.isAllAppsVisible() )
		{
			final Folder openFolder = getOpenFolder();
			if( openFolder != null )
			{
				openFolder.addFocusables( views , direction );
			}
			else
			{
				super.addFocusables( views , direction , focusableMode );
			}
		}
	}
	
	public boolean isSmall()
	{
		return mState == State.SMALL || mState == State.SPRING_LOADED || mState == State.OVERVIEW;
	}
	
	void enableChildrenCache(
			int fromPage ,
			int toPage )
	{
		if( fromPage > toPage )
		{
			final int temp = fromPage;
			fromPage = toPage;
			toPage = temp;
		}
		final int screenCount = getChildCount();
		fromPage = Math.max( fromPage , 0 );
		toPage = Math.min( toPage , screenCount - 1 );
		for( int i = fromPage ; i <= toPage ; i++ )
		{
			final CellLayout layout = (CellLayout)getChildAt( i );
			layout.setChildrenDrawnWithCacheEnabled( true );
			layout.setChildrenDrawingCacheEnabled( true );
		}
	}
	
	void clearChildrenCache()
	{
		final int screenCount = getChildCount();
		for( int i = 0 ; i < screenCount ; i++ )
		{
			final CellLayout layout = (CellLayout)getChildAt( i );
			layout.setChildrenDrawnWithCacheEnabled( false );
			// In software mode, we don't want the items to continue to be drawn into bitmaps
			if( !isHardwareAccelerated() )
			{
				layout.setChildrenDrawingCacheEnabled( false );
			}
		}
	}
	
	private void updateChildrenLayersEnabled(
			boolean force )
	{
		boolean small = mState == State.SMALL || mState == State.OVERVIEW || mIsSwitchingState;
		boolean enableChildrenLayers = force || small || mAnimatingViewIntoPlace || isPageMoving();
		if( enableChildrenLayers != mChildrenLayersEnabled )
		{
			mChildrenLayersEnabled = enableChildrenLayers;
			if( mChildrenLayersEnabled )
			{
				enableHwLayersOnVisiblePages();
			}
			else
			{
				for( int i = 0 ; i < getPageCount() ; i++ )
				{
					final CellLayout cl = (CellLayout)getChildAt( i );
					cl.enableHardwareLayer( false );
				}
			}
		}
	}
	
	private void enableHwLayersOnVisiblePages()
	{
		if( mChildrenLayersEnabled )
		{
			final int screenCount = getChildCount();
			getVisiblePages_EX( mTempVisiblePagesRange );
			int leftScreen = mTempVisiblePagesRange[0];
			int rightScreen = mTempVisiblePagesRange[1];
			if( leftScreen == rightScreen )
			{
				// make sure we're caching at least two pages always
				if( rightScreen < screenCount - 1 )
				{
					rightScreen++;
				}
				else if( leftScreen > 0 )
				{
					leftScreen--;
				}
			}
			final CellLayout customScreen = mWorkspaceScreens.get( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			for( int i = 0 ; i < screenCount ; i++ )
			{
				final CellLayout layout = (CellLayout)getPageAt( i );
				// enable layers between left and right screen inclusive, except for the
				// customScreen, which may animate its content during transitions.
				boolean enableLayer = layout != customScreen && leftScreen <= i && i <= rightScreen && shouldDrawChild( layout );
				layout.enableHardwareLayer( enableLayer );
			}
		}
	}
	
	public void buildPageHardwareLayers()
	{
		// force layers to be enabled just for the call to buildLayer
		updateChildrenLayersEnabled( true );
		if( getWindowToken() != null )
		{
			final int childCount = getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				CellLayout cl = (CellLayout)getChildAt( i );
				cl.buildHardwareLayer();
			}
		}
		updateChildrenLayersEnabled( false );
	}
	
	protected void onWallpaperTap(
			MotionEvent ev )
	{
		final int[] position = mTempCell;
		getLocationOnScreen( position );
		int pointerIndex = ev.getActionIndex();
		position[0] += (int)ev.getX( pointerIndex );
		position[1] += (int)ev.getY( pointerIndex );
		mWallpaperManager.sendWallpaperCommand(
				getWindowToken() ,
				ev.getAction() == MotionEvent.ACTION_UP ? WallpaperManager.COMMAND_TAP : WallpaperManager.COMMAND_SECONDARY_TAP ,
				position[0] ,
				position[1] ,
				0 ,
				null );
	}
	
	/*
	 * This interpolator emulates the rate at which the perceived scale of an object changes
	 * as its distance from a camera increases. When this interpolator is applied to a scale
	 * animation on a view, it evokes the sense that the object is shrinking due to moving away
	 * from the camera.
	 */
	public static class ZInterpolator implements TimeInterpolator
	{
		
		private float focalLength;
		
		public ZInterpolator(
				float foc )
		{
			focalLength = foc;
		}
		
		public float getInterpolation(
				float input )
		{
			return ( 1.0f - focalLength / ( focalLength + input ) ) / ( 1.0f - focalLength / ( focalLength + 1.0f ) );
		}
	}
	
	/*
	 * The exact reverse of ZInterpolator.
	 */
	static class InverseZInterpolator implements TimeInterpolator
	{
		
		private ZInterpolator zInterpolator;
		
		public InverseZInterpolator(
				float foc )
		{
			zInterpolator = new ZInterpolator( foc );
		}
		
		public float getInterpolation(
				float input )
		{
			return 1 - zInterpolator.getInterpolation( 1 - input );
		}
	}
	
	/*
	 * ZInterpolator compounded with an ease-out.
	 */
	static class ZoomOutInterpolator implements TimeInterpolator
	{
		
		private final DecelerateInterpolator decelerate = new DecelerateInterpolator( 0.75f );
		private final ZInterpolator zInterpolator = new ZInterpolator( 0.13f );
		
		public float getInterpolation(
				float input )
		{
			return decelerate.getInterpolation( zInterpolator.getInterpolation( input ) );
		}
	}
	
	/*
	 * InvereZInterpolator compounded with an ease-out.
	 */
	static class ZoomInInterpolator implements TimeInterpolator
	{
		
		private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator( 0.35f );
		private final DecelerateInterpolator decelerate = new DecelerateInterpolator( 3.0f );
		
		public float getInterpolation(
				float input )
		{
			return decelerate.getInterpolation( inverseZInterpolator.getInterpolation( input ) );
		}
	}
	
	private final ZoomInInterpolator mZoomInInterpolator = new ZoomInInterpolator();
	
	/*
	*
	* We call these methods (onDragStartedWithItemSpans/onDragStartedWithSize) whenever we
	* start a drag in Launcher, regardless of whether the drag has ever entered the Workspace
	*
	* These methods mark the appropriate pages as accepting drops (which alters their visual
	* appearance).
	*
	*/
	public void onDragStartedWithItem(
			View v )
	{
		final Canvas canvas = new Canvas();
		// The outline is used to visualize where the item will land if dropped
		mDragOutline = createDragOutline( v , canvas , DRAG_BITMAP_PADDING );
	}
	
	public void onDragStartedWithItem(
			PendingAddItemInfo info ,
			Bitmap b ,
			boolean clipAlpha )
	{
		final Canvas canvas = new Canvas();
		int[] size = estimateItemSize( info.getSpanX() , info.getSpanY() , info , false );
		// The outline is used to visualize where the item will land if dropped
		mDragOutline = createDragOutline( b , canvas , DRAG_BITMAP_PADDING , size[0] , size[1] , clipAlpha );
	}
	
	public void exitWidgetResizeMode()
	{
		if( mLauncher != null )
		{
			DragLayer mDragLayer = mLauncher.getDragLayer();
			if( mDragLayer != null )
			{
				mDragLayer.clearAllResizeFrames();
			}
		}
	}
	
	private void initAnimationArrays()
	{
		final int childCount = getChildCount();
		if( mLastChildCount == childCount )
			return;
		mOldBackgroundAlphas = new float[childCount];
		mOldAlphas = new float[childCount];
		mNewBackgroundAlphas = new float[childCount];
		mNewAlphas = new float[childCount];
	}
	
	Animator getChangeStateAnimation(
			final State state ,
			boolean animated )
	{
		return getChangeStateAnimation( state , animated , 0 , -1 );
	}
	
	@Override
	protected void getOverviewModePages(
			int[] range )
	{
		int start = 0;
		int end = getChildCount() - 1;
		if( hasFavoritesPage() )
		{
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“编辑模式下，可滑动区域错误”的问题）
			if( LauncherDefaultConfig.getFavoritesPagePosition() > 0 )
			{
				end--;
			}
			else
			//xiatian add end
			{
				start++;
			}
		}
		if( hasCameraPage() )
		{
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“编辑模式下，可滑动区域错误”的问题）
			if( LauncherDefaultConfig.getCameraPagePosition() < 0 )
			{
				start++;
			}
			else
			//xiatian add end
			{
				end--;
			}
		}
		if( hasMusicPage() )
		{
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“编辑模式下，可滑动区域错误”的问题）
			if( LauncherDefaultConfig.getMusicPagePosition() < 0 )
			{
				start++;
			}
			else
			//xiatian add end
			{
				end--;
			}
		}
		range[0] = Math.max( 0 , Math.min( start , getChildCount() - 1 ) );
		range[1] = Math.max( 0 , end );
	}
	
	protected void onStartReordering()
	{
		super.onStartReordering();
		showOutlines();
		// Reordering handles its own animations, disable the automatic ones.
		disableLayoutTransitions();
	}
	
	protected void onEndReordering()
	{
		super.onEndReordering();
		hideOutlines();
		mScreenOrder.clear();
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			CellLayout cl = ( (CellLayout)getChildAt( i ) );
			//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
			{//在桌面编辑模式下，退出celllayout排序状态时，重新设置默认主页
				if( cl.isDefaultPage() )
				{
					int mDefaultPageIndex = i;
					//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
					//xiatian del start
					//					//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
					//					if( hasFavoritesPage() )
					//					{
					//						mDefaultPageIndex--;
					//					}
					//					//xiatian add end
					//xiatian del end
					mDefaultPageIndex -= getFunctionPagesInNormalPageLeftNum();//xiatian add
					//xiatian end
					setDefaultPage( mDefaultPageIndex );
				}
			}
			//xiatian add end
			mScreenOrder.add( getIdForScreen( cl ) );
		}
		mLauncher.getModel().updateWorkspaceScreenOrder( mLauncher , mScreenOrder );
		// Re-enable auto layout transitions for page deletion.
		enableLayoutTransitions();
	}
	
	public boolean isInOverviewMode()
	{
		return mState == State.OVERVIEW;
	}
	
	public boolean enterOverviewMode()
	{
		// zhangjin@2016/03/29 ADD START
		if( LauncherDefaultConfig.HERUNXIN_BIG_LAUNCHER )
		{
			return false;
		}
		// zhangjin@2016/03/29 ADD END
		if( isFunctionPageByPageIndex( getCurrentPage() ) || isScrollPage() )
		{
			return false;
		}
		//WangLei add start //bug:0010172  //进入编辑模式时关闭打开的文件夹
		if( mLauncher != null )
		{
			//xiatian start	//需求：在文件夹打开状态下进入编辑模式，立刻关闭文件夹（不播放文件夹关闭的相关动画）。
			//			mLauncher.closeFolder();//xiatian del
			mLauncher.closeFolderWithoutAnim();//xiatian add
			//xiatian end
		}
		//WangLei add end
		exitWidgetResizeMode();//xiatian add	//fix bug：解决“在插件调整大小模式下，点击menu键进入编辑模式，没有退出插件调整大小模式（插件调整大小编辑框没有消失）”的问题。【i_0010263】
		//cheyingkun add start	//添加友盟统计自定义事件(编辑模式)
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( mLauncher , UmengStatistics.ENTER_EDIT_MODE );
		}
		//cheyingkun add end
		enableOverviewMode( true , -1 , true );
		return true;
	}
	
	public void exitOverviewMode(
			boolean animated )
	{
		exitOverviewMode( -1 , animated );
	}
	
	public void exitOverviewMode(
			int snapPage ,
			boolean animated )
	{
		enableOverviewMode( false , snapPage , animated );
	}
	
	private void enableOverviewMode(
			final boolean enable ,
			int snapPage ,
			boolean animated )
	{
		State finalState = Workspace.State.OVERVIEW;
		if( !enable )
		{
			finalState = Workspace.State.NORMAL;
		}
		if( enable )
		{
			if( LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_EDITMODE <= 0 )
			{
				animated = false;
			}
		}
		else
		{
			if( LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_EDITMODE_TO_WORKSPACE <= 0 )
			{
				animated = false;
			}
		}
		overviewAnim = getChangeStateAnimation( finalState , animated , 0 , snapPage );//zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		if( overviewAnim != null )
		{
			onTransitionPrepare();
			overviewAnim.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(
						Animator arg0 )
				{
					onTransitionEnd();
					initAnimationStyle( Workspace.this );
					restoreWorkspace();
					checkSelectedPageWhenChangeState( enable );//cheyingkun add	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
					//xiatian add start	//fix bug：解决“特定页面（酷生活、主页、音乐页和相机页）的页面指示器显示特定图标时，页面指示器显示错误（重复以及错位）”的问题。
					if( enable == false )
					{
						updateHomePageIndicator();
					}
					//xiatian add end
				}
			} );
			overviewAnim.start();
		}
		//xiatian add start	//fix bug：解决“在双层模式下，在编辑模式长按一个页面时，pc端安装应用，安装成功之后，桌面自动退出编辑模式，但此时被长按的页面所有图标呈托起状态”的问题。【i_0010545】
		else
		{
			initAnimationStyle( Workspace.this );
			checkSelectedPageWhenChangeState( enable );//cheyingkun add	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
			//xiatian add start	//fix bug：解决“特定页面（酷生活、主页、音乐页和相机页）的页面指示器显示特定图标时，页面指示器显示错误（重复以及错位）”的问题。
			if( enable == false )
			{
				updateHomePageIndicator();
			}
			//xiatian add end
		}
		//xiatian add end
		//gaominghui add start   //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
		{
			int childCount = getChildCount();
			int index = getDefaultPageIndex();
			for( int i = 0 ; i < childCount ; i++ )
			{
				CellLayout cl = (CellLayout)getChildAt( i );
				if( enable )
				{
					cl.setEditModeHomeViewVisible( View.VISIBLE );
				}
				else
				{
					cl.setEditModeHomeViewVisible( View.GONE );
				}
				if( index == i )
				{
					cl.getHomeView().setImageDrawable( getResources().getDrawable( R.drawable.editmode_home_selected ) );
					cl.setNormalBackgroundShape();
					invalidate();
				}
				else
				{
					cl.getHomeView().setImageDrawable( getResources().getDrawable( R.drawable.editmode_home_unselected ) );
					cl.setNormalBackground();
					invalidate();
				}
			}
		}
		//gaominghui add end
	}
	
	int getOverviewModeTranslationY()
	{
		int childHeight = getNormalChildHeight();
		int viewPortHeight = getViewportHeight();
		int scaledChildHeight = (int)( mOverviewModeShrinkFactor * childHeight );
		int offset = ( viewPortHeight - scaledChildHeight ) / 2;
		int offsetDelta = mOverviewModePageOffset - offset + mInsets.top;
		return offsetDelta;
	}
	
	boolean shouldVoiceButtonProxyBeVisible()
	{
		if( isOnOrMovingToFavoritesPage() )
		{
			return false;
		}
		if( mState != State.NORMAL )
		{
			return false;
		}
		return true;
	}
	
	public void updateInteractionForState()
	{
		if( mState != State.NORMAL )
		{
			mLauncher.onInteractionBegin();
		}
		else
		{
			mLauncher.onInteractionEnd();
		}
	}
	
	public void setState(
			State state )
	{
		mState = state;
		updateInteractionForState();
	}
	
	public State getState()
	{
		return mState;
	}
	
	Animator getChangeStateAnimation(
			final State state ,
			boolean animated ,
			int delay ,
			int snapPage )
	{
		if( mState == state )
		{
			return null;
		}
		// Initialize animation arrays for the first time if necessary
		//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
		if( LauncherDefaultConfig.isAllowEmptyScreen() )
		{
			if( state == State.OVERVIEW )
			{
				addExtraAddPageScreen();
				mLauncher.getSearchDropTargetBar().setDeleteDropTargetVisibility( true );
				setDragCellLayoutListener( mLauncher.getSearchDropTargetBar() );
			}
			else
			{
				removeExtraAddPageScreen();
				mLauncher.getSearchDropTargetBar().setDeleteDropTargetVisibility( false );
				setDragCellLayoutListener( null );
			}
		}
		//zhujieping add end
		initAnimationArrays();
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		final State oldState = mState;
		final boolean oldStateIsNormal = ( oldState == State.NORMAL );
		final boolean oldStateIsSpringLoaded = ( oldState == State.SPRING_LOADED );
		final boolean oldStateIsSmall = ( oldState == State.SMALL );
		final boolean oldStateIsOverview = ( oldState == State.OVERVIEW );
		setState( state );
		final boolean stateIsNormal = ( state == State.NORMAL );
		final boolean stateIsSpringLoaded = ( state == State.SPRING_LOADED );
		final boolean stateIsSmall = ( state == State.SMALL );
		final boolean stateIsOverview = ( state == State.OVERVIEW );
		float finalBackgroundAlpha = ( stateIsSpringLoaded || stateIsOverview ) ? 1.0f : 0f;
		// yangxiaoming start 2015-05-18 由于i_0011156难复现，特在此打上Log
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "i_0011156" , StringUtils.concat( "Workspace getChangeStateAnimation - oldState:" + oldState , "-newState:" + state , "-finalBackgroundAlpha:" , finalBackgroundAlpha ) );
		}
		// yangxiaoming end
		float finalHotseatAndPageIndicatorAlpha = ( stateIsOverview || stateIsSmall ) ? 0f : 1f;
		float finalOverviewPanelAlpha = stateIsOverview ? 1f : 0f;
		float finalSearchBarAlpha = !stateIsNormal ? 0f : 1f;
		float finalWorkspaceTranslationY = stateIsOverview ? getOverviewModeTranslationY() : 0;
		boolean workspaceToAllApps = ( oldStateIsNormal && stateIsSmall );
		boolean allAppsToWorkspace = ( oldStateIsSmall && stateIsNormal );
		boolean workspaceToOverview = ( oldStateIsNormal && stateIsOverview );
		boolean overviewToWorkspace = ( oldStateIsOverview && stateIsNormal );
		mNewScale = 1.0f;
		if( oldStateIsOverview )
		{
			//xiatian add start	//fix bug：解决“在双层模式下，在编辑模式长按一个页面时，pc端安装应用，安装成功之后，桌面自动退出编辑模式，但此时被长按的页面所有图标呈托起状态”的问题。【i_0010545】
			if( isReordering( false ) )
			{
				endReorderingForceAndWithoutAnim( !overviewToWorkspace , true );
			}
			else
			//xiatian add end
			{
				disableFreeScroll( snapPage );
			}
			//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false.( 解决“编辑模式点击添加小组件后返回桌面，这时设置主页图标显示在桌面”的问题。【i_0014848】)
			//当从编辑模式切换到其他模式时应该把设置主页的小房子隐藏掉
			hideEditModeHomeView();
			//gaominghui add end
		}
		else if( stateIsOverview )
		{
			enableFreeScroll();
			//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。(解决“编辑模式点击添加小组件后返回桌面，这时设置主页图标显示在桌面”的问题。【i_0014879】)
			if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
			{
				if( oldStateIsSmall )
				{
					for( int i = 0 ; i < getChildCount() ; i++ )
					{
						CellLayout cl = (CellLayout)getChildAt( i );
						if( cl.getEditModeHomeViewVisible() == View.GONE )
						{
							cl.setEditModeHomeViewVisible( View.VISIBLE );
						}
					}
				}
			}
			//gaominghui add end
		}
		if( state != State.NORMAL )
		{
			if( stateIsSpringLoaded )
			{
				mNewScale = mSpringLoadedShrinkFactor;
			}
			else if( stateIsOverview )
			{
				mNewScale = mOverviewModeShrinkFactor;
			}
			else if( stateIsSmall )
			{
				mNewScale = mOverviewModeShrinkFactor - 0.3f;
			}
			if( workspaceToAllApps )
			{
				updateChildrenLayersEnabled( false );
			}
		}
		final int duration;
		if( workspaceToAllApps )
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_APPLIST;
		}
		else if( workspaceToOverview )
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_EDITMODE;
		}
		else if( overviewToWorkspace )
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_EDITMODE_TO_WORKSPACE;
		}
		else
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_APPLIST_TO_WORKSPACE;
		}
		// zhujieping@2015/04/27 ADD START，进入编辑模式时，先清除draglayer的动画，若拖动图标放入文件夹中，会有动画，此时进入编辑模式，会闪现空白页，【i_0011101】
		DragLayer mDragLayer = mLauncher.getDragLayer();
		if( stateIsOverview && mDragLayer != null )
		{
			mDragLayer.clearAnimatedView();
		}
		// zhujieping@2015/04/27 ADD END
		// zhujieping@2015/03/25 ADD START
		//这个动画也会改变cellLayout的setBackgroundAlpha，与下方要执行的动画冲突，同理searchbar，因此停掉动画【i_0010653】
		stopOutlinesFadeOuntAnimation();
		mLauncher.getSearchDropTargetBar().stopSearchBarAnim();
		// zhujieping@2015/03/25 ADD END		
		// zhangjin@2015/08/04 ADD START i_11917
		stopOutlinesFadeInAnimation();
		// zhangjin@2015/08/04  ADD END
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			final CellLayout cl = (CellLayout)getChildAt( i );
			boolean isCurrentPage = ( i == getNextPage() );
			float initialAlpha = cl.getShortcutsAndWidgets().getAlpha();
			// YANGTIANYU@2016/07/26 ADD START
			if( isCameraPage( i ) )
			{
				initialAlpha = CameraView.getInstance().getCameraPageView( mLauncher ).getAlpha();
			}
			if( isMusicPage( i ) )
			{
				initialAlpha = MusicView.getInstance().getMusicPageView( mLauncher ).getAlpha();
			}
			// YANGTIANYU@2016/07/26 ADD END
			float finalAlpha = stateIsSmall ? 0f : 1f;
			// If we are animating to/from the small state, then hide the side pages and fade the
			// current page in
			if( !mIsSwitchingState )
			{
				if( workspaceToAllApps || allAppsToWorkspace )
				{
					if( allAppsToWorkspace && isCurrentPage )
					{
						initialAlpha = 0f;
					}
					else if( !isCurrentPage )
					{
						initialAlpha = finalAlpha = 0f;
					}
					cl.setShortcutAndWidgetAlpha( initialAlpha );
				}
			}
			mOldAlphas[i] = initialAlpha;
			mNewAlphas[i] = finalAlpha;
			if( animated )
			{
				mOldBackgroundAlphas[i] = cl.getBackgroundAlpha();
				mNewBackgroundAlphas[i] = finalBackgroundAlpha;
			}
			else
			{
				cl.setBackgroundAlpha( finalBackgroundAlpha );
				cl.setShortcutAndWidgetAlpha( finalAlpha );
			}
		}
		final View searchBar = mLauncher.getSearchBar();
		final View overviewPanel = mLauncher.getOverviewPanel();
		final View hotseat = mLauncher.getHotseat();
		// zhujieping@2015/04/07 ADD START
		//编辑模式下，搜索框不显示。进入编辑模式时置标志位，退出时置回标志位。例如智能分类不成功时，退出编辑模式时animated为false，所以应该放在if的外面。
		mLauncher.getSearchDropTargetBar().setSearchBarIfHide( stateIsOverview );
		// zhujieping@2015/04/07 ADD END
		if( animated )
		{
			anim.setDuration( duration );
			LauncherViewPropertyAnimator scale = new LauncherViewPropertyAnimator( this );
			scale.scaleX( mNewScale ).scaleY( mNewScale ).translationY( finalWorkspaceTranslationY ).setInterpolator( mZoomInInterpolator );
			anim.play( scale );
			for( int index = 0 ; index < getChildCount() ; index++ )
			{
				final int i = index;
				final CellLayout cl = (CellLayout)getChildAt( i );
				float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
				if( mOldAlphas[i] == 0 && mNewAlphas[i] == 0 )
				{
					cl.setBackgroundAlpha( mNewBackgroundAlphas[i] );
					cl.setShortcutAndWidgetAlpha( mNewAlphas[i] );
				}
				else
				{
					if( mOldAlphas[i] != mNewAlphas[i] || currentAlpha != mNewAlphas[i] )
					{
						// YANGTIANYU@2016/07/26 UPD START
						//LauncherViewPropertyAnimator alphaAnim = new LauncherViewPropertyAnimator( cl.getShortcutsAndWidgets() );
						LauncherViewPropertyAnimator alphaAnim = null;
						if( isCameraPage( i ) )
						{
							alphaAnim = new LauncherViewPropertyAnimator( CameraView.getInstance().getCameraPageView( mLauncher ) );
						}
						else if( isMusicPage( i ) )
						{
							alphaAnim = new LauncherViewPropertyAnimator( MusicView.getInstance().getMusicPageView( mLauncher ) );
						}
						else
						{
							alphaAnim = new LauncherViewPropertyAnimator( cl.getShortcutsAndWidgets() );
						}
						// YANGTIANYU@2016/07/26 UPD END
						alphaAnim.alpha( mNewAlphas[i] ).setInterpolator( mZoomInInterpolator );
						anim.play( alphaAnim );
					}
					if( mOldBackgroundAlphas[i] != 0 || mNewBackgroundAlphas[i] != 0 )
					{
						ValueAnimator bgAnim = LauncherAnimUtils.ofFloat( cl , 0f , 1f );
						bgAnim.setInterpolator( mZoomInInterpolator );
						bgAnim.addUpdateListener( new LauncherAnimatorUpdateListener() {
							
							public void onAnimationUpdate(
									float a ,
									float b )
							{
								//xiatian start	//添加保护，防止数组越界
								//								cl.setBackgroundAlpha( a * mOldBackgroundAlphas[i] + b * mNewBackgroundAlphas[i] );//xiatian del
								//xiatian add start
								float mAlpha = -1;
								int mOldBackgroundAlphasIndex = i;
								int mNewBackgroundAlphasIndex = i;
								if( mOldBackgroundAlphas.length <= i )
								{
									mOldBackgroundAlphasIndex = mOldBackgroundAlphas.length - 1;
								}
								if( mNewBackgroundAlphas.length <= i )
								{
									mNewBackgroundAlphasIndex = mNewBackgroundAlphas.length - 1;
								}
								mAlpha = a * mOldBackgroundAlphas[mOldBackgroundAlphasIndex] + b * mNewBackgroundAlphas[mNewBackgroundAlphasIndex];
								cl.setBackgroundAlpha( mAlpha );
								//xiatian add end
								//xiatian end
							}
						} );
						anim.play( bgAnim );
					}
				}
			}
			ObjectAnimator pageIndicatorAlpha = null;
			ObjectAnimator pageIndicatorY = null;//cheyingkun add	//phenix仿S5效果,编辑模式页面指示器
			View pageIndicatorParent = mLauncher.findViewById( R.id.page_indicator );
			if( pageIndicatorParent != null && getPageIndicator() != null )
			{
				//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
				if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
				{
					LauncherAppState app = LauncherAppState.getInstance();
					float pageIndicatorYInOverviewMode = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInOverviewMode();
					float pageIndicatorYInNormal = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInNormal();
					float y = stateIsOverview ? pageIndicatorYInOverviewMode : pageIndicatorYInNormal;
					pageIndicatorY = ObjectAnimator.ofFloat( pageIndicatorParent , "y" , y );//zhujieping //pageIndicatorParent是getPageIndicator()的父，这里设置pageIndicatorParent的y
					float finalAlpha = stateIsSmall ? 0f : 1f;
					pageIndicatorAlpha = ObjectAnimator.ofFloat( getPageIndicator() , "alpha" , finalAlpha );
				}
				else
				//cheyingkun add end
				{
					pageIndicatorAlpha = ObjectAnimator.ofFloat( getPageIndicator() , "alpha" , finalHotseatAndPageIndicatorAlpha );
				}
			}
			ObjectAnimator hotseatAlpha = ObjectAnimator.ofFloat( hotseat , "alpha" , finalHotseatAndPageIndicatorAlpha );
			ObjectAnimator overviewPanelAlpha = ObjectAnimator.ofFloat( overviewPanel , "alpha" , finalOverviewPanelAlpha );
			overviewPanelAlpha.addListener( new AlphaUpdateListener( overviewPanel ) );
			hotseatAlpha.addListener( new AlphaUpdateListener( hotseat ) );
			//cheyingkun add start	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			ObjectAnimator searchBarAlpha = null;
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && searchBar != null )
			//cheyingkun add end
			{
				searchBarAlpha = ObjectAnimator.ofFloat( searchBar , "alpha" , finalSearchBarAlpha );
				searchBarAlpha.addListener( new AlphaUpdateListener( searchBar ) );
			}
			if( workspaceToOverview )
			{
				hotseatAlpha.setInterpolator( new DecelerateInterpolator( 2 ) );
			}
			else if( overviewToWorkspace )
			{
				overviewPanelAlpha.setInterpolator( new DecelerateInterpolator( 2 ) );
			}
			if( getPageIndicator() != null )
			{
				if( pageIndicatorAlpha != null )
				{
					pageIndicatorAlpha.addListener( new AlphaUpdateListener( getPageIndicator() ) );
					anim.play( pageIndicatorAlpha );
				}
				//cheyingkun add start	//phenix仿S5效果,编辑模式页面指示器
				if( pageIndicatorY != null )
				{
					anim.play( pageIndicatorY );
				}
				//cheyingkun add end
			}
			anim.play( overviewPanelAlpha );
			anim.play( hotseatAlpha );
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && searchBarAlpha != null )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			{
				anim.play( searchBarAlpha );
			}
			anim.setStartDelay( delay );
		}
		else
		{
			overviewPanel.setAlpha( finalOverviewPanelAlpha );
			AlphaUpdateListener.updateVisibility( overviewPanel );
			hotseat.setAlpha( finalHotseatAndPageIndicatorAlpha );
			AlphaUpdateListener.updateVisibility( hotseat );
			View pageIndicatorParent = mLauncher.findViewById( R.id.page_indicator );
			if( pageIndicatorParent != null && getPageIndicator() != null )
			{
				//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
				if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
				{
					LauncherAppState app = LauncherAppState.getInstance();
					float pageIndicatorYInOverviewMode = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInOverviewMode();
					float pageIndicatorYInNormal = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInNormal();
					float y = stateIsOverview ? pageIndicatorYInOverviewMode : pageIndicatorYInNormal;
					float finalAlpha = stateIsSmall ? 0f : 1f;
					getPageIndicator().setAlpha( finalAlpha );
					pageIndicatorParent.setY( y );//zhujieping //pageIndicatorParent是getPageIndicator()的父，这里设置pageIndicatorParent的y
				}
				else
				//cheyingkun add end
				{
					getPageIndicator().setAlpha( finalHotseatAndPageIndicatorAlpha );
				}
				AlphaUpdateListener.updateVisibility( getPageIndicator() );
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && searchBar != null )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			{
				searchBar.setAlpha( finalSearchBarAlpha );
				AlphaUpdateListener.updateVisibility( searchBar );
			}
			updateFunctionPagesVisibility();
			setScaleX( mNewScale );
			setScaleY( mNewScale );
			setTranslationY( finalWorkspaceTranslationY );
		}
		// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
		//		//WangLei start //bug:0010441 //当语音搜索图标不可用时，点击整个搜索框都相应onClickSearchButton
		//		//mLauncher.updateVoiceButtonProxyVisible( false );
		//		mLauncher.updateVoiceButtonProxyVisible( true ); //WangLei add 
		//		//WangLei add end
		// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
		// yangxiaoming start 2015-05-18 由于i_0011156难复现，特在此打上Log
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "i_0011156" , StringUtils.concat( "stateIsOverview is:" , stateIsOverview ) );
			//			Launcher.getLogCat( "i_0011156" );
		}
		// yangxiaoming end
		if( stateIsSpringLoaded )
		{
			// Right now we're covered by Apps Customize
			// Show the background gradient immediately, so the gradient will
			// be showing once AppsCustomize disappears
			animateBackgroundGradient( LauncherDefaultConfig.getInt( R.integer.config_springLoadedBgAlpha ) / 100f , false );
		}
		else if( stateIsOverview )
		{
			animateBackgroundGradient( LauncherDefaultConfig.getInt( R.integer.config_overviewModeBgAlpha ) / 100f , animated );
		}
		else
		{
			// Fade the background gradient away
			animateBackgroundGradient( 0f , animated );
		}
		// zhangjin@2015/07/31 增加状态切换动画保护 ADD START  i_11917
		if( mChangeStateAnim != null && mChangeStateAnim.isRunning() )
		{
			mChangeStateAnim.cancel();
		}
		mChangeStateAnim = anim;
		// zhangjin@2015/07/31 ADD END
		return anim;
	}
	
	static class AlphaUpdateListener implements AnimatorUpdateListener , AnimatorListener
	{
		
		View view;
		
		public AlphaUpdateListener(
				View v )
		{
			view = v;
		}
		
		@Override
		public void onAnimationUpdate(
				ValueAnimator arg0 )
		{
			updateVisibility( view );
		}
		
		public static void updateVisibility(
				View view )
		{
			if( view.getAlpha() < ALPHA_CUTOFF_THRESHOLD && view.getVisibility() != GONE )
			{
				view.setVisibility( GONE );
			}
			else if( view.getAlpha() > ALPHA_CUTOFF_THRESHOLD && view.getVisibility() != VISIBLE )
			{
				view.setVisibility( VISIBLE );
			}
		}
		
		@Override
		public void onAnimationCancel(
				Animator arg0 )
		{
		}
		
		@Override
		public void onAnimationEnd(
				Animator arg0 )
		{
			updateVisibility( view );
		}
		
		@Override
		public void onAnimationRepeat(
				Animator arg0 )
		{
		}
		
		@Override
		public void onAnimationStart(
				Animator arg0 )
		{
			// We want the views to be visible for animation, so fade-in/out is visible
			if( view != null )
				view.setVisibility( VISIBLE );
		}
	}
	
	@Override
	public void onLauncherTransitionPrepare(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		onTransitionPrepare();
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
		mTransitionProgress = t;
	}
	
	@Override
	public void onLauncherTransitionEnd(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		onTransitionEnd();
	}
	
	private void onTransitionPrepare()
	{
		mIsSwitchingState = true;
		// Invalidate here to ensure that the pages are rendered during the state change transition.
		invalidate();
		updateChildrenLayersEnabled( false );
		hideFunctionPagesIfNecessary();
	}
	
	void updateFunctionPagesVisibility()
	{
		int visibility = mState == Workspace.State.NORMAL ? VISIBLE : INVISIBLE;
		//zhujieping start	//解决“小组件界面选择邮件，进入二级选择界面后，点击home键，返回桌面后，相机页、音乐页内容消失”的问题（这时，功能页已经从mScreenOrder中被移除，不能通过mScreenOrder判断，应该由mWorkspaceScreens判断）。【i_0015180】
		//boolean mIsHaveFavoritesPage = hasFavoritesPage();//zhujieping del
		boolean mIsHaveFavoritesPage = mWorkspaceScreens.get( FUNCTION_FAVORITES_PAGE_SCREEN_ID ) != null;//zhujieping add
		//zhujieping end
		if( mIsHaveFavoritesPage )
		{
			mWorkspaceScreens.get( FUNCTION_FAVORITES_PAGE_SCREEN_ID ).setVisibility( visibility );
			//cheyingkun add start	//phenix1.1稳定版移植酷生活
			if( visibility == INVISIBLE )
			{
				FavoritesPageManager.getInstance( this.getContext() ).onHide();
			}
			else
			{
				FavoritesPageManager.getInstance( this.getContext() ).onShow();
			}
			//cheyingkun add end
		}
		// YANGTIANYU@2016/06/20 ADD START
		// TODO 我不是很懂这个地方是干啥用的,难道是个保护措施？
		//是的,保护措施!进入退出编辑模式会走到getChangeStateAnimation
		//getChangeStateAnimation分为动画和不做动画两种情况,这里是不做动画时、酷生活、音乐页、相机页的状态保护
		//zhujieping start	//解决“小组件界面选择邮件，进入二级选择界面后，点击home键，返回桌面后，相机页、音乐页内容消失”的问题（这时，功能页已经从mScreenOrder中被移除，不能通过mScreenOrder判断，应该由mWorkspaceScreens判断）。【i_0015180】
		//boolean mIsHaveCameraPage = hasCameraPage();;//zhujieping del
		boolean mIsHaveCameraPage = mWorkspaceScreens.get( FUNCTION_CAMERA_PAGE_SCREEN_ID ) != null;//zhujieping add
		//zhujieping end
		if( mIsHaveCameraPage )
		{
			mWorkspaceScreens.get( FUNCTION_CAMERA_PAGE_SCREEN_ID ).setVisibility( visibility );
		}
		//zhujieping start	//解决“小组件界面选择邮件，进入二级选择界面后，点击home键，返回桌面后，相机页、音乐页内容消失”的问题（这时，功能页已经从mScreenOrder中被移除，不能通过mScreenOrder判断，应该由mWorkspaceScreens判断）。【i_0015180】
		//boolean mIsHaveMusicPage = hasMusicPage();;//zhujieping del
		boolean mIsHaveMusicPage = mWorkspaceScreens.get( FUNCTION_MUSIC_PAGE_SCREEN_ID ) != null;//zhujieping add
		//zhujieping end
		if( mIsHaveMusicPage )
		{
			mWorkspaceScreens.get( FUNCTION_MUSIC_PAGE_SCREEN_ID ).setVisibility( visibility );
		}
		// YANGTIANYU@2016/06/20 ADD END
		//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR//
				&& ( mIsHaveFavoritesPage || mIsHaveCameraPage || mIsHaveMusicPage ) )
		{
			showOrHideFunctionPagesPageIndicator( visibility == INVISIBLE ? false : true );
		}
		//cheyingkun add end
	}
	
	void showFunctionPagesIfNecessary()
	{
		boolean show = mState == Workspace.State.NORMAL;
		//zhujieping start	//解决“小组件界面选择邮件，进入二级选择界面后，点击home键，返回桌面后，相机页、音乐页内容消失”的问题（这时，功能页已经从mScreenOrder中被移除，不能通过mScreenOrder判断，应该由mWorkspaceScreens判断）。【i_0015180】
		//boolean mIsHaveFavoritesPage = hasFavoritesPage();//zhujieping del
		boolean mIsHaveFavoritesPage = mWorkspaceScreens.get( FUNCTION_FAVORITES_PAGE_SCREEN_ID ) != null;//zhujieping add
		//zhujieping end
		if( show && mIsHaveFavoritesPage )
		{
			mWorkspaceScreens.get( FUNCTION_FAVORITES_PAGE_SCREEN_ID ).setVisibility( VISIBLE );
			FavoritesPageManager.getInstance( this.getContext() ).onShow();//cheyingkun add	//phenix1.1稳定版移植酷生活
		}
		// YANGTIANYU@2016/06/20 ADD START
		show |= mState == Workspace.State.SMALL;
		if( show )
		{
			//zhujieping start	//解决“小组件界面选择邮件，进入二级选择界面后，点击home键，返回桌面后，相机页、音乐页内容消失”的问题（这时，功能页已经从mScreenOrder中被移除，不能通过mScreenOrder判断，应该由mWorkspaceScreens判断）。【i_0015180】
			//if( hasCameraPage() )//zhujieping del
			if( mWorkspaceScreens.get( FUNCTION_CAMERA_PAGE_SCREEN_ID ) != null )//zhujieping add
			//zhujieping end
			{
				mWorkspaceScreens.get( FUNCTION_CAMERA_PAGE_SCREEN_ID ).setVisibility( VISIBLE );
			}
			//zhujieping start	//解决“小组件界面选择邮件，进入二级选择界面后，点击home键，返回桌面后，相机页、音乐页内容消失”的问题（这时，功能页已经从mScreenOrder中被移除，不能通过mScreenOrder判断，应该由mWorkspaceScreens判断）。【i_0015180】
			//if( hasMusicPage() )//zhujieping del
			if( mWorkspaceScreens.get( FUNCTION_MUSIC_PAGE_SCREEN_ID ) != null )//zhujieping add
			//zhujieping end
			{
				mWorkspaceScreens.get( FUNCTION_MUSIC_PAGE_SCREEN_ID ).setVisibility( VISIBLE );
			}
			showOrHideFunctionPagesPageIndicator( true );
		}
		// YANGTIANYU@2016/06/20 ADD END
	}
	
	void hideFunctionPagesIfNecessary()
	{
		boolean hide = mState != Workspace.State.NORMAL;
		hide &= mState != Workspace.State.SMALL;//cheyingkun add	//解决“进入主菜单快速返回，多次操作后，酷生活变暗”的问题【i_0014432】
		if( hide )
		{
			CellLayout mFavoritesPage = mWorkspaceScreens.get( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			if( mFavoritesPage != null )
			{
				mFavoritesPage.setVisibility( INVISIBLE );
				//cheyingkun add start	//phenix1.1稳定版移植酷生活
				if( hasFavoritesPage() )
				{
					FavoritesPageManager.getInstance( this.getContext() ).onHide();
				}
				//cheyingkun add end
			}
			// YANGTIANYU@2016/06/20 ADD START
			CellLayout mCameraPage = mWorkspaceScreens.get( FUNCTION_CAMERA_PAGE_SCREEN_ID );
			if( mCameraPage != null )
			{
				mCameraPage.setVisibility( INVISIBLE );
			}
			CellLayout mMusicPage = mWorkspaceScreens.get( FUNCTION_MUSIC_PAGE_SCREEN_ID );
			if( mMusicPage != null )
			{
				mMusicPage.setVisibility( INVISIBLE );
			}
			// YANGTIANYU@2016/06/20 ADD END
			showOrHideFunctionPagesPageIndicator( false );
		}
		//		//cheyingkun add start	//phenix仿S5效果,编辑模式页面指示器
		//		if( hide )
		//		{
		//			showOrHideFunctionPagesPageIndicator( false );
		//		}
		//		else
		//		{
		//			showOrHideFunctionPagesPageIndicator( true );
		//		}
		//		//cheyingkun add end
		//zhujieping add start //相机页打开摄像头，进入主菜单时，相机页关闭摄像头
		CellLayout mCameraPage = mWorkspaceScreens.get( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		if( mCameraPage != null )
		{
			CameraView.getInstance().stopCamera();
		}
		//zhujieping add end
	}
	
	private void onTransitionEnd()
	{
		mIsSwitchingState = false;
		updateChildrenLayersEnabled( false );
		// The code in getChangeStateAnimation to determine initialAlpha and finalAlpha will ensure
		// ensure that only the current page is visible during (and subsequently, after) the
		// transition animation.  If fade adjacent pages is disabled, then re-enable the page
		// visibility after the transition animation.
		if( !mWorkspaceFadeInAdjacentScreens )
		{
			for( int i = 0 ; i < getChildCount() ; i++ )
			{
				final CellLayout cl = (CellLayout)getChildAt( i );
				cl.setShortcutAndWidgetAlpha( 1f );
			}
		}
		showFunctionPagesIfNecessary();
	}
	
	@Override
	public View getContent()
	{
		return this;
	}
	
	/**
	 * Draw the View v into the given Canvas.
	 *
	 * @param v the view to draw
	 * @param destCanvas the canvas to draw on
	 * @param padding the horizontal and vertical padding to use when drawing
	 */
	private void drawDragView(
			View v ,
			Canvas destCanvas ,
			int padding ,
			boolean pruneToDrawable )
	{
		final Rect clipRect = mTempRect;
		v.getDrawingRect( clipRect );
		boolean textVisible = false;
		destCanvas.save();
		if( v instanceof TextView && pruneToDrawable )
		{
			//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
			//xiatian del start
			//			Drawable d = ( (TextView)v ).getCompoundDrawables()[1];
			//			int mClipRectRight = d.getIntrinsicWidth() + padding;
			//			int mClipRectBottom = d.getIntrinsicHeight() + padding;
			//xiatian del end
			//xiatian add start
			Drawable d = null;
			int mClipRectRight = 0;
			int mClipRectBottom = 0;
			if( v instanceof BubbleTextView )
			{
				d = ( (BubbleTextView)v ).getIcon();
				mClipRectRight = ( (BubbleTextView)v ).getIconWidth() + padding;
				mClipRectBottom = ( (BubbleTextView)v ).getIconHeight() + padding;
			}
			else
			{
				d = ( (TextView)v ).getCompoundDrawables()[1];
				mClipRectRight = d.getIntrinsicWidth() + padding;
				mClipRectBottom = d.getIntrinsicHeight() + padding;
			}
			//xiatian add end
			//xiatian end
			clipRect.set( 0 , 0 , mClipRectRight , mClipRectBottom );
			destCanvas.translate( padding / 2 , padding / 2 );
			if( d != null )
			{
				d.draw( destCanvas );
			}
		}
		else
		{
			if( v instanceof FolderIcon )
			{
				// For FolderIcons the text can bleed into the icon area, and so we need to
				// hide the text completely (which can't be achieved by clipping).
				if( ( (FolderIcon)v ).getTextVisible() )
				{
					( (FolderIcon)v ).setTextVisible( false );
					textVisible = true;
				}
			}
			else if( v instanceof BubbleTextView )
			{
				final BubbleTextView tv = (BubbleTextView)v;
				clipRect.bottom = tv.getExtendedPaddingTop() - (int)BubbleTextView.PADDING_V + tv.getLayout().getLineTop( 0 );
			}
			else if( v instanceof TextView )
			{
				final TextView tv = (TextView)v;
				clipRect.bottom = tv.getExtendedPaddingTop() - tv.getCompoundDrawablePadding() + tv.getLayout().getLineTop( 0 );
			}
			destCanvas.translate( -v.getScrollX() + padding / 2 , -v.getScrollY() + padding / 2 );
			destCanvas.clipRect( clipRect , Op.REPLACE );
			v.draw( destCanvas );
			// Restore text visibility of FolderIcon if necessary
			if( textVisible )
			{
				if( v instanceof FolderIcon )
				{
					( (FolderIcon)v ).setTextVisible( true );
				}
			}
		}
		destCanvas.restore();
	}
	
	/**
	 * Returns a new bitmap to show when the given View is being dragged around.
	 * Responsibility for the bitmap is transferred to the caller.
	 */
	public Bitmap createDragBitmap(
			View v ,
			Canvas canvas ,
			int padding )
	{
		Bitmap b;
		if( v instanceof TextView )
		{
			//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
			//xiatian del start
			//			Drawable d = ( (TextView)v ).getCompoundDrawables()[1];
			//			int width = d.getIntrinsicWidth() + padding;
			//			int height = d.getIntrinsicHeight() + padding;
			//xiatian del end
			//xiatian add start
			int width = 0;
			int height = 0;
			if( v instanceof BubbleTextView )
			{
				width = ( (BubbleTextView)v ).getIconWidth() + padding;
				height = ( (BubbleTextView)v ).getIconHeight() + padding;
			}
			else
			{
				Drawable d = ( (TextView)v ).getCompoundDrawables()[1];
				//cheyingkun start	//phenix仿S5效果,解决“主菜单长按图标被截掉”的问题
				//cheyingkun del start
				//				width = d.getIntrinsicWidth() + padding;
				//				height = d.getIntrinsicHeight() + padding;
				//cheyingkun del end
				//cheyingkun add start
				float allappsIconScale = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale();
				width = (int)( d.getIntrinsicWidth() * allappsIconScale + padding );
				height = (int)( d.getIntrinsicHeight() * allappsIconScale + padding );
				//cheyingkun add end
				//cheyingkun end
			}
			//xiatian add end
			//xiatian end
			b = Bitmap.createBitmap( width , height , Bitmap.Config.ARGB_8888 );
		}
		else
		{
			b = Bitmap.createBitmap( v.getWidth() + padding , v.getHeight() + padding , Bitmap.Config.ARGB_8888 );
		}
		canvas.setBitmap( b );
		drawDragView( v , canvas , padding , true );
		canvas.setBitmap( null );
		return b;
	}
	
	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
	 * Responsibility for the bitmap is transferred to the caller.
	 */
	private Bitmap createDragOutline(
			View v ,
			Canvas canvas ,
			int padding )
	{
		//		final int outlineColor = getResources().getColor( R.color.outline_color );//xiatian del	//需求：长按图标、文件夹和插件时，将“图标轮廓”改为“图标投影”
		final Bitmap b = Bitmap.createBitmap( v.getWidth() + padding , v.getHeight() + padding , Bitmap.Config.ARGB_8888 );
		canvas.setBitmap( b );
		drawDragView( v , canvas , padding , true );
		//		mOutlineHelper.applyMediumExpensiveOutlineWithBlur( b , canvas , outlineColor , outlineColor );//xiatian del	//需求：长按图标、文件夹和插件时，将“图标轮廓”改为“图标投影”
		canvas.setBitmap( null );
		return b;
	}
	
	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
	 * Responsibility for the bitmap is transferred to the caller.
	 */
	private Bitmap createDragOutline(
			Bitmap orig ,
			Canvas canvas ,
			int padding ,
			int w ,
			int h ,
			boolean clipAlpha )
	{
		//		final int outlineColor = getResources().getColor( R.color.outline_color );//xiatian del	//需求：长按图标、文件夹和插件时，将“图标轮廓”改为“图标投影”
		final Bitmap b = Bitmap.createBitmap( w , h , Bitmap.Config.ARGB_8888 );
		canvas.setBitmap( b );
		Rect src = new Rect( 0 , 0 , orig.getWidth() , orig.getHeight() );
		float scaleFactor = Math.min( ( w - padding ) / (float)orig.getWidth() , ( h - padding ) / (float)orig.getHeight() );
		int scaledWidth = (int)( scaleFactor * orig.getWidth() );
		int scaledHeight = (int)( scaleFactor * orig.getHeight() );
		Rect dst = new Rect( 0 , 0 , scaledWidth , scaledHeight );
		// center the image
		dst.offset( ( w - scaledWidth ) / 2 , ( h - scaledHeight ) / 2 );
		canvas.drawBitmap( orig , src , dst , null );
		//		mOutlineHelper.applyMediumExpensiveOutlineWithBlur( b , canvas , outlineColor , outlineColor , clipAlpha );//xiatian del	//需求：长按图标、文件夹和插件时，将“图标轮廓”改为“图标投影”
		canvas.setBitmap( null );
		return b;
	}
	
	void startDrag(
			com.cooee.phenix.data.CellInfo cellInfo )
	{
		View child = cellInfo.getCell();
		// Make sure the drag was started by a long press as opposed to a long click.
		if( !child.isInTouchMode() )
		{
			return;
		}
		//cheyingkun start	//解决“拖动图标到垃圾框，松手快速点击桌面空白处，卸载提示出来后点击取消，之前拖动的图标消失”的问题。（bug：0010055）
		//		mDragInfo = cellInfo;//cheyingkun del
		//cheyingkun add start
		if( mDragInfo == null )
		{
			mDragInfo = new CellInfo();
		}
		cellInfo.cloneCellInfo( mDragInfo );
		//cheyingkun add end
		//cheyingkun end
		child.setVisibility( INVISIBLE );
		CellLayout layout = (CellLayout)child.getParent().getParent();
		layout.prepareChildForDrag( child );
		child.clearFocus();
		child.setPressed( false );
		final Canvas canvas = new Canvas();
		// The outline is used to visualize where the item will land if dropped
		mDragOutline = createDragOutline( child , canvas , DRAG_BITMAP_PADDING );
		beginDragShared( child , this );
	}
	
	public void beginDragShared(
			View child ,
			DragSource source )
	{
		// The drag bitmap follows the touch point around on the screen
		final Bitmap b = createDragBitmap( child , new Canvas() , DRAG_BITMAP_PADDING );
		final int bmpWidth = b.getWidth();
		final int bmpHeight = b.getHeight();
		float scale = mLauncher.getDragLayer().getLocationInDragLayer( child , mTempXY );
		int dragLayerX = Math.round( mTempXY[0] - ( bmpWidth - scale * child.getWidth() ) / 2 );
		int dragLayerY = Math.round( mTempXY[1] - ( bmpHeight - scale * bmpHeight ) / 2 - DRAG_BITMAP_PADDING / 2 );
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		Point dragVisualizeOffset = null;
		Rect dragRect = null;
		if( child instanceof BubbleTextView || child instanceof PagedViewIcon )
		{
			int top = 0;
			int left = 0;
			int right = 0;
			int bottom = 0;
			if( child instanceof PagedViewIcon )
			{
				//cheyingkun start	//phenix仿S5效果,解决“主菜单长按图标被截掉”的问题
				//cheyingkun del start
				//				int iconSizeWidth = grid.getIconWidthSizePx();
				//				int iconSizeHeight = grid.getIconHeightSizePx();
				//cheyingkun del end
				//cheyingkun add start
				int iconSizeWidth = (int)( grid.getIconWidthSizePx() * grid.getAllappsIconScale() );
				int iconSizeHeight = (int)( grid.getIconHeightSizePx() * grid.getAllappsIconScale() );
				//cheyingkun add end
				//cheyingkun end
				top = child.getPaddingTop();
				left = ( bmpWidth - iconSizeWidth ) / 2;
				right = left + iconSizeWidth;
				bottom = top + iconSizeHeight;
				dragLayerY += top;
			}
			else
			{
				BubbleTextView mBubbleTextView = (BubbleTextView)child;
				//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
				//xiatian del start
				//					iconSizeWidth = grid.getIconWidthSizePx();
				//					iconSizeHeight = grid.getIconHeightSizePx();
				//xiatian del end
				//xiatian add start
				int iconSizeWidth = mBubbleTextView.getIconWidth();
				int iconSizeHeight = mBubbleTextView.getIconHeight();
				//xiatian add end
				//xiatian end
				top = mBubbleTextView.getPaddingTop();
				left = ( bmpWidth - iconSizeWidth ) / 2;
				right = left + iconSizeWidth;
				bottom = top + iconSizeHeight;
				dragLayerY += top;
			}
			// Note: The drag region is used to calculate drag layer offsets, but the
			// dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
			dragVisualizeOffset = new Point( -DRAG_BITMAP_PADDING / 2 , DRAG_BITMAP_PADDING / 2 );
			dragRect = new Rect( left , top , right , bottom );
		}
		else if( child instanceof FolderIcon )
		{
			int previewSize = grid.getFolderIconWidthSizePx();
			dragRect = new Rect( 0 , child.getPaddingTop() , child.getWidth() , previewSize );
		}
		// Clear the pressed state if necessary
		if( child instanceof BubbleTextView )
		{
			BubbleTextView icon = (BubbleTextView)child;
			icon.clearPressedOrFocusedBackground();
		}
		mCurrentDragView = child;//cheyingkun add	//解决“取消T卡挂载模式，长按文件夹内灰色图标，被长按的图标没有变亮”的问题。【i_0011410】
		mDragController.startDrag( b , dragLayerX , dragLayerY , source , child.getTag() , DragController.DRAG_ACTION_MOVE , dragVisualizeOffset , dragRect , scale );
		if( ( child.getParent() instanceof ShortcutAndWidgetContainer )
		//
		&& ( ( (ItemInfo)child.getTag() ).getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP ) //xiatian add	//“mDragSourceInternal”参数只用于workspace
		)
		{
			mDragSourceInternal = (ShortcutAndWidgetContainer)child.getParent();
		}
		b.recycle();
	}
	
	void addApplicationShortcut(
			ShortcutInfo info ,
			CellLayout target ,
			long container ,
			long screenId ,
			int cellX ,
			int cellY ,
			boolean insertAtFirst ,
			int intersectX ,
			int intersectY )
	{
		View view = mLauncher.createShortcut( R.layout.application , target , (ShortcutInfo)info );
		final int[] cellXY = new int[2];
		target.findCellForSpanThatIntersects( cellXY , 1 , 1 , intersectX , intersectY );
		addInScreen( view , container , screenId , cellXY[0] , cellXY[1] , 1 , 1 , insertAtFirst );
		LauncherModel.addOrMoveItemInDatabase( mLauncher , info , container , screenId , cellXY[0] , cellXY[1] );
	}
	
	public boolean transitionStateShouldAllowDrop()
	{
		return( ( !isSwitchingState() || mTransitionProgress > 0.5f ) && mState != State.SMALL );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean acceptDrop(
			DragObject d )
	{
		// If it's an external drop (e.g. from All Apps), check if it should be accepted
		CellLayout dropTargetLayout = mDropToLayout;
		if( d.dragSource != this )
		{
			// Don't accept the drop if we're not over a screen at time of drop
			if( dropTargetLayout == null )
			{
				return false;
			}
			if( !transitionStateShouldAllowDrop() )
				return false;
			mDragViewVisualCenter = getDragViewVisualCenter( d.x , d.y , d.xOffset , d.yOffset , d.dragView , mDragViewVisualCenter );
			// We want the point to be mapped to the dragTarget.
			if( mLauncher.isHotseatLayout( dropTargetLayout ) )
			{
				mapPointFromSelfToHotseatLayout( mLauncher.getHotseat() , mDragViewVisualCenter );
			}
			else
			{
				mapPointFromSelfToChild( dropTargetLayout , mDragViewVisualCenter , null );
			}
			int spanX = 1;
			int spanY = 1;
			if( mDragInfo != null )
			{
				final CellInfo dragCellInfo = mDragInfo;
				spanX = dragCellInfo.getSpanX();
				spanY = dragCellInfo.getSpanY();
			}
			else
			{
				final ItemInfo dragInfo = (ItemInfo)d.dragInfo;
				spanX = dragInfo.getSpanX();
				spanY = dragInfo.getSpanY();
			}
			int minSpanX = spanX;
			int minSpanY = spanY;
			if( d.dragInfo instanceof PendingAddWidgetInfo )
			{
				minSpanX = ( (PendingAddWidgetInfo)d.dragInfo ).getMinSpanX();
				minSpanY = ( (PendingAddWidgetInfo)d.dragInfo ).getMinSpanY();
			}
			mTargetCell = findNearestArea( (int)mDragViewVisualCenter[0] , (int)mDragViewVisualCenter[1] , minSpanX , minSpanY , dropTargetLayout , mTargetCell );
			float distance = dropTargetLayout.getDistanceFromCell( mDragViewVisualCenter[0] , mDragViewVisualCenter[1] , mTargetCell );
			if( willCreateUserFolder( (ItemInfo)d.dragInfo , dropTargetLayout , mTargetCell , distance , true ) )
			{
				return true;
			}
			if( willAddToExistingUserFolder( (ItemInfo)d.dragInfo , dropTargetLayout , mTargetCell , distance ) )
			{
				return true;
			}
			int[] resultSpan = new int[2];
			mTargetCell = dropTargetLayout.createArea(
					(int)mDragViewVisualCenter[0] ,
					(int)mDragViewVisualCenter[1] ,
					minSpanX ,
					minSpanY ,
					spanX ,
					spanY ,
					null ,
					mTargetCell ,
					resultSpan ,
					CellLayout.MODE_ACCEPT_DROP );
			boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;
			// Don't accept the drop if there's no room for the item
			if( !foundCell )
			{
				// Don't show the message if we are dropping on the AllApps button and the hotseat
				// is full
				boolean isHotseat = mLauncher.isHotseatLayout( dropTargetLayout );
				if( mTargetCell != null && isHotseat )
				{
					Hotseat hotseat = mLauncher.getHotseat();
					if( hotseat.isAllAppsButtonRank( hotseat.getOrderInHotseat( mTargetCell[0] , mTargetCell[1] ) ) )
					{
						return false;
					}
				}
				mLauncher.showOutOfSpaceMessage( isHotseat );
				return false;
			}
		}
		long screenId = getIdForScreen( dropTargetLayout );
		if( screenId == EXTRA_EMPTY_SCREEN_ID )
		{
			commitExtraEmptyScreen();
		}
		return true;
	}
	
	boolean willCreateUserFolder(
			ItemInfo info ,
			CellLayout target ,
			int[] targetCell ,
			float distance ,
			boolean considerTimeout )
	{
		// zhangjin@2016/03/29 ADD START
		if( LauncherDefaultConfig.HERUNXIN_BIG_LAUNCHER )
		{
			return false;
		}
		// zhangjin@2016/03/29 ADD END
		if( distance > mMaxDistanceForFolderCreation )
			return false;
		View dropOverView = target.getChildAt( targetCell[0] , targetCell[1] );
		if( dropOverView != null )
		{
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams)dropOverView.getLayoutParams();
			if( lp.useTmpCoords && ( lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.tmpCellY ) )
			{
				return false;
			}
		}
		boolean hasntMoved = false;
		if( mDragInfo != null )
		{
			hasntMoved = dropOverView == mDragInfo.getCell();
		}
		if( dropOverView == null || hasntMoved || ( considerTimeout && !mCreateUserFolderOnDrop ) )
		{
			return false;
		}
		boolean aboveShortcut = ( dropOverView.getTag() instanceof ShortcutInfo );
		//xiatian start	//整理代码：整理接口willBecomeShortcut
		//xiatian del start
		//		boolean willBecomeShortcut = ( info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
		//		//
		//		//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//		//该图标是否可以和其他图标生成文件夹
		//		|| info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL
		//		//xiatian add end
		//		//
		//		);
		//xiatian del end
		boolean willBecomeShortcut = info.willBecomeShortcut();//xiatian add
		//xiatian end
		return( aboveShortcut && willBecomeShortcut );
	}
	
	boolean willAddToExistingUserFolder(
			Object dragInfo ,
			CellLayout target ,
			int[] targetCell ,
			float distance )
	{
		if( distance > mMaxDistanceForFolderCreation )
			return false;
		View dropOverView = target.getChildAt( targetCell[0] , targetCell[1] );
		if( dropOverView != null )
		{
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams)dropOverView.getLayoutParams();
			if( lp.useTmpCoords && ( lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.tmpCellY ) )
			{
				return false;
			}
		}
		if( dropOverView instanceof FolderIcon )
		{
			FolderIcon fi = (FolderIcon)dropOverView;
			if( fi.acceptDrop( dragInfo ) )
			{
				return true;
			}
		}
		return false;
	}
	
	boolean createUserFolderIfNecessary(
			View newView ,
			long container ,
			CellLayout target ,
			int[] targetCell ,
			float distance ,
			boolean external ,
			DragView dragView ,
			Runnable postAnimationRunnable )
	{
		if( distance > mMaxDistanceForFolderCreation )
			return false;
		View v = target.getChildAt( targetCell[0] , targetCell[1] );
		boolean hasntMoved = false;
		if( mDragInfo != null )
		{
			CellLayout cellParent = getParentCellLayoutForView( mDragInfo.getCell() );
			hasntMoved = ( mDragInfo.getCellX() == targetCell[0] && mDragInfo.getCellY() == targetCell[1] ) && ( cellParent == target );
		}
		if( v == null || hasntMoved || !mCreateUserFolderOnDrop )
			return false;
		mCreateUserFolderOnDrop = false;
		final long screenId = ( targetCell == null ) ? mDragInfo.getScreenId() : getIdForScreen( target );
		boolean aboveShortcut = ( v.getTag() instanceof ShortcutInfo );
		boolean willBecomeShortcut = ( newView.getTag() instanceof ShortcutInfo );
		if( aboveShortcut && willBecomeShortcut )
		{
			ShortcutInfo sourceInfo = (ShortcutInfo)newView.getTag();
			ShortcutInfo destInfo = (ShortcutInfo)v.getTag();
			// if the drag started here, we need to remove it from the workspace
			if( !external )
			{
				getParentCellLayoutForView( mDragInfo.getCell() ).removeView( mDragInfo.getCell() );
			}
			Rect folderLocation = new Rect();
			float scale = mLauncher.getDragLayer().getDescendantRectRelativeToSelf( v , folderLocation );
			target.removeView( v );
			FolderIcon fi = mLauncher.addFolder( target , container , screenId , targetCell[0] , targetCell[1] );
			destInfo.setCellX( -1 );
			destInfo.setCellY( -1 );
			sourceInfo.setCellX( -1 );
			sourceInfo.setCellY( -1 );
			// If the dragView is null, we can't animate
			boolean animate = dragView != null;
			if( animate )
			{
				fi.performCreateAnimation( destInfo , v , sourceInfo , dragView , folderLocation , scale , postAnimationRunnable );
			}
			else
			{
				fi.addItem( destInfo );
				fi.addItem( sourceInfo );
			}
			return true;
		}
		return false;
	}
	
	boolean addToExistingFolderIfNecessary(
			View newView ,
			CellLayout target ,
			int[] targetCell ,
			float distance ,
			DragObject d ,
			boolean external )
	{
		if( distance > mMaxDistanceForFolderCreation )
			return false;
		View dropOverView = target.getChildAt( targetCell[0] , targetCell[1] );
		if( !mAddToExistingFolderOnDrop )
			return false;
		mAddToExistingFolderOnDrop = false;
		if( dropOverView instanceof FolderIcon )
		{
			FolderIcon fi = (FolderIcon)dropOverView;
			if( fi.acceptDrop( d.dragInfo ) )
			{
				fi.onDrop( d );
				// if the drag started here, we need to remove it from the workspace
				if( !external )
				{
					getParentCellLayoutForView( mDragInfo.getCell() ).removeView( mDragInfo.getCell() );
				}
				return true;
			}
		}
		return false;
	}
	
	public void onDrop(
			final DragObject d )
	{
		mDragViewVisualCenter = getDragViewVisualCenter( d.x , d.y , d.xOffset , d.yOffset , d.dragView , mDragViewVisualCenter );
		CellLayout dropTargetLayout = mDropToLayout;
		// We want the point to be mapped to the dragTarget.
		if( dropTargetLayout != null )
		{
			if( mLauncher.isHotseatLayout( dropTargetLayout ) )
			{
				mapPointFromSelfToHotseatLayout( mLauncher.getHotseat() , mDragViewVisualCenter );
			}
			else
			{
				mapPointFromSelfToChild( dropTargetLayout , mDragViewVisualCenter , null );
			}
		}
		int snapScreen = -1;
		boolean resizeOnDrop = false;
		if( d.dragSource != this )
		{
			final int[] touchXY = new int[]{ (int)mDragViewVisualCenter[0] , (int)mDragViewVisualCenter[1] };
			onDropExternal( touchXY , d.dragInfo , dropTargetLayout , false , d );
		}
		else if( mDragInfo != null )
		{
			final View cell = mDragInfo.getCell();
			Runnable resizeRunnable = null;
			if( dropTargetLayout != null && !d.cancelled )
			{
				// Move internally
				boolean hasMovedLayouts = ( getParentCellLayoutForView( cell ) != dropTargetLayout );
				boolean hasMovedIntoHotseat = mLauncher.isHotseatLayout( dropTargetLayout );
				long container = hasMovedIntoHotseat ? LauncherSettings.Favorites.CONTAINER_HOTSEAT : LauncherSettings.Favorites.CONTAINER_DESKTOP;
				long screenId = ( mTargetCell[0] < 0 ) ? mDragInfo.getScreenId() : getIdForScreen( dropTargetLayout );
				int spanX = mDragInfo != null ? mDragInfo.getSpanX() : 1;
				int spanY = mDragInfo != null ? mDragInfo.getSpanY() : 1;
				// First we find the cell nearest to point at which the item is
				// dropped, without any consideration to whether there is an item there.
				mTargetCell = findNearestArea( (int)mDragViewVisualCenter[0] , (int)mDragViewVisualCenter[1] , spanX , spanY , dropTargetLayout , mTargetCell );
				float distance = dropTargetLayout.getDistanceFromCell( mDragViewVisualCenter[0] , mDragViewVisualCenter[1] , mTargetCell );
				// If the item being dropped is a shortcut and the nearest drop
				// cell also contains a shortcut, then create a folder with the two shortcuts.
				if( !mInScrollArea && createUserFolderIfNecessary( cell , container , dropTargetLayout , mTargetCell , distance , false , d.dragView , null ) )
				{
					stripEmptyScreens();
					return;
				}
				if( addToExistingFolderIfNecessary( cell , dropTargetLayout , mTargetCell , distance , d , false ) )
				{
					stripEmptyScreens();
					return;
				}
				// Aside from the special case where we're dropping a shortcut onto a shortcut,
				// we need to find the nearest cell location that is vacant
				ItemInfo item = (ItemInfo)d.dragInfo;
				int minSpanX = item.getSpanX();
				int minSpanY = item.getSpanY();
				if( item.getMinSpanX() > 0 && item.getMinSpanY() > 0 )
				{
					minSpanX = item.getMinSpanX();
					minSpanY = item.getMinSpanY();
				}
				int[] resultSpan = new int[2];
				mTargetCell = dropTargetLayout.createArea(
						(int)mDragViewVisualCenter[0] ,
						(int)mDragViewVisualCenter[1] ,
						minSpanX ,
						minSpanY ,
						spanX ,
						spanY ,
						cell ,
						mTargetCell ,
						resultSpan ,
						CellLayout.MODE_ON_DROP );
				boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;
				// if the widget resizes on drop
				if( foundCell && ( cell instanceof AppWidgetHostView ) && ( resultSpan[0] != item.getSpanX() || resultSpan[1] != item.getSpanY() ) )
				{
					resizeOnDrop = true;
					item.setSpanX( resultSpan[0] );
					item.setSpanY( resultSpan[1] );
					AppWidgetHostView awhv = (AppWidgetHostView)cell;
					AppWidgetResizeFrame.updateWidgetSizeRanges( awhv , mLauncher , resultSpan[0] , resultSpan[1] );
				}
				if( getScreenIdForPageIndex( mCurrentPage ) != screenId && !hasMovedIntoHotseat )
				{
					snapScreen = getPageIndexForScreenId( screenId );
					// zhujieping@2015/04/14 UPD START,此处滑页导致在将前一页桌面最后一个图标移至下一页，松手后会先闪出前一页空白页面再显示当前页面，将移动时间设为0，并立即执行computeScroll,i_0010089
					//snapToPage( snapScreen );
					snapToPage( snapScreen , 0 , true );
					// zhujieping@2015/04/14 UPD END
				}
				if( foundCell )
				{
					final ItemInfo info = (ItemInfo)cell.getTag();
					if( hasMovedLayouts )
					{
						// Reparent the view
						getParentCellLayoutForView( cell ).removeView( cell );
						addInScreen( cell , container , screenId , mTargetCell[0] , mTargetCell[1] , info.getSpanX() , info.getSpanY() );
					}
					// update the item's position after drop
					CellLayout.LayoutParams lp = (CellLayout.LayoutParams)cell.getLayoutParams();
					lp.cellX = lp.tmpCellX = mTargetCell[0];
					lp.cellY = lp.tmpCellY = mTargetCell[1];
					lp.cellHSpan = item.getSpanX();
					lp.cellVSpan = item.getSpanY();
					lp.isLockedToGrid = true;
					cell.setId( LauncherModel.getCellLayoutChildId( container , mDragInfo.getScreenId() , mTargetCell[0] , mTargetCell[1] , mDragInfo.getSpanX() , mDragInfo.getSpanY() ) );
					if( container != LauncherSettings.Favorites.CONTAINER_HOTSEAT && cell instanceof LauncherAppWidgetHostView )
					{
						final CellLayout cellLayout = dropTargetLayout;
						// We post this call so that the widget has a chance to be placed
						// in its final location
						final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView)cell;
						AppWidgetProviderInfo pinfo = hostView.getAppWidgetInfo();
						if( pinfo != null && pinfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE )
						{
							final Runnable addResizeFrame = new Runnable() {
								
								public void run()
								{
									if( mState != Workspace.State.OVERVIEW )//cheyingkun add	//解决“长按小部件松手后，在调整小部件大小的编辑框未显示出来时点击menu键，调整小部件大小的编辑框在编辑模式下显示。”的问题【i_0010870】
									{
										DragLayer dragLayer = mLauncher.getDragLayer();
										dragLayer.addResizeFrame( info , hostView , cellLayout );
									}
								}
							};
							resizeRunnable = ( new Runnable() {
								
								public void run()
								{
									if( !isPageMoving() )
									{
										addResizeFrame.run();
									}
									else
									{
										mDelayedResizeRunnable = addResizeFrame;
									}
								}
							} );
						}
					}
					LauncherModel.modifyItemInDatabase( mLauncher , info , container , screenId , lp.cellX , lp.cellY , item.getSpanX() , item.getSpanY() );
				}
				else
				{
					// If we can't find a drop location, we return the item to its original position
					CellLayout.LayoutParams lp = (CellLayout.LayoutParams)cell.getLayoutParams();
					mTargetCell[0] = lp.cellX;
					mTargetCell[1] = lp.cellY;
					CellLayout layout = (CellLayout)cell.getParent().getParent();
					layout.markCellsAsOccupiedForView( cell );
				}
			}
			final CellLayout parent = (CellLayout)cell.getParent().getParent();
			final Runnable finalResizeRunnable = resizeRunnable;
			// Prepare it to be animated into its new position
			// This must be called after the view has been re-parented
			final Runnable onCompleteRunnable = new Runnable() {
				
				@Override
				public void run()
				{
					mAnimatingViewIntoPlace = false;
					updateChildrenLayersEnabled( false );
					if( finalResizeRunnable != null )
					{
						finalResizeRunnable.run();
					}
					stripEmptyScreens();
				}
			};
			mAnimatingViewIntoPlace = true;
			if( d.dragView.hasDrawn() )
			{
				final ItemInfo info = (ItemInfo)cell.getTag();
				if( info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET )
				{
					int animationType = resizeOnDrop ? ANIMATE_INTO_POSITION_AND_RESIZE : ANIMATE_INTO_POSITION_AND_DISAPPEAR;
					animateWidgetDrop( info , parent , d.dragView , onCompleteRunnable , animationType , cell , false );
				}
				else
				{
					int duration = snapScreen < 0 ? -1 : ADJACENT_SCREEN_DROP_DURATION;
					mLauncher.getDragLayer().animateViewIntoPosition( d.dragView , cell , duration , onCompleteRunnable , this );
				}
			}
			else
			{
				d.deferDragViewCleanupPostAnimation = false;
				cell.setVisibility( VISIBLE );
			}
			parent.onDropChild( cell );
		}
	}
	
	public void setFinalScrollForPageChange(
			int pageIndex )
	{
		CellLayout cl = (CellLayout)getChildAt( pageIndex );
		if( cl != null )
		{
			mSavedScrollX = getScrollX();
			mSavedTranslationX = cl.getTranslationX();
			mSavedRotationY = cl.getRotationY();
			final int newX = getScrollForPage( pageIndex );//getScrollForPage( pageIndex );
			setScrollX( newX );
			cl.setTranslationX( 0f );
			cl.setRotationY( 0f );
		}
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		//xiatian add start	//解决“2017/04/11 14:37:10”引起的一系列问题。
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT )//zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		//【备注】
		//	“config_folder_style”设置为0的前提下
		//		1、“桌面有文件夹时，重启桌面，重启后，所有页面都显示空白”的问题；
		//		2、“拖动桌面图标生成文件夹后，所有页面都显示空白”的问题；
		//		3、“拖动桌面图标放入文件夹后，所有页面都显示空白”的问题；
		//		4、“点击打开文件夹后，所有页面都显示空白”的问题；
		//xiatian add end
		{
			setScaleX( mStateTransitionAnimation.getFinalScale() );
			setScaleY( mStateTransitionAnimation.getFinalScale() );
		}
		//zhujieping add end
	}
	
	public void resetFinalScrollForPageChange(
			int pageIndex )
	{
		if( pageIndex >= 0 )
		{
			CellLayout cl = (CellLayout)getChildAt( pageIndex );
			setScrollX( mSavedScrollX );
			cl.setTranslationX( mSavedTranslationX );
			cl.setRotationY( mSavedRotationY );
		}
	}
	
	public void getViewLocationRelativeToSelf(
			View v ,
			int[] location )
	{
		getLocationInWindow( location );
		int x = location[0];
		int y = location[1];
		v.getLocationInWindow( location );
		int vX = location[0];
		int vY = location[1];
		location[0] = vX - x;
		location[1] = vY - y;
	}
	
	public void onDragEnter(
			DragObject d )
	{
		mDragEnforcer.onDragEnter();
		mCreateUserFolderOnDrop = false;
		mAddToExistingFolderOnDrop = false;
		mDropToLayout = null;
		//xiatian start	//这边应该加个判断，解决：“workspace每次onDragEnter的时候，DropLayout都设为getCurrentDropLayout()的值”的问题。
		//		CellLayout layout = getCurrentDropLayout();//xiatian del
		//xiatian add start
		CellLayout layout = null;
		Rect r = new Rect();
		if( mLauncher.getHotseat() != null && !isDragWidget( d ) )
		{
			if( isPointInSelfOverHotseat( d.x , d.y , r ) )
			{
				layout = mLauncher.getHotseat().getLayout();
			}
		}
		if( layout == null )
		{
			layout = getCurrentDropLayout();
		}
		//xiatian add end
		//xiatian end
		setCurrentDropLayout( layout );
		setCurrentDragOverlappingLayout( layout );
	}
	
	/** Return a rect that has the cellWidth/cellHeight (left, top), and
	 * widthGap/heightGap (right, bottom) */
	static Rect getCellLayoutMetrics(
			Launcher launcher ,
			int orientation )
	{
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		Resources res = launcher.getResources();
		Display display = launcher.getWindowManager().getDefaultDisplay();
		Point smallestSize = new Point();
		Point largestSize = new Point();
		// gaominghui@2016/12/14 ADD START 兼容android 4.0
		if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
			display.getCurrentSizeRange( smallestSize , largestSize );
		else
			DecorateUtils.getCurrentSizeRange( launcher , smallestSize , largestSize );
		// gaominghui@2016/12/14 ADD END 兼容android 4.0
		int countX = (int)grid.getNumColumns();
		int countY = (int)grid.getNumRows();
		int constrainedLongEdge = largestSize.y;
		int constrainedShortEdge = smallestSize.y;
		if( orientation == CellLayout.LANDSCAPE )
		{
			if( mLandscapeCellLayoutMetrics == null )
			{
				Rect padding = grid.getWorkspacePadding( CellLayout.LANDSCAPE );
				int width = constrainedLongEdge - padding.left - padding.right;
				int height = constrainedShortEdge - padding.top - padding.bottom;
				mLandscapeCellLayoutMetrics = new Rect();
				mLandscapeCellLayoutMetrics.set( grid.calculateCellWidth( width , countX ) , grid.calculateCellHeight( height , countY ) , 0 , 0 );
			}
			return mLandscapeCellLayoutMetrics;
		}
		else if( orientation == CellLayout.PORTRAIT )
		{
			if( mPortraitCellLayoutMetrics == null )
			{
				Rect padding = grid.getWorkspacePadding( CellLayout.PORTRAIT );
				int width = constrainedShortEdge - padding.left - padding.right;
				int height = constrainedLongEdge - padding.top - padding.bottom;
				mPortraitCellLayoutMetrics = new Rect();
				mPortraitCellLayoutMetrics.set( grid.calculateCellWidth( width , countX ) , grid.calculateCellHeight( height , countY ) , 0 , 0 );
			}
			return mPortraitCellLayoutMetrics;
		}
		return null;
	}
	
	public void onDragExit(
			DragObject d )
	{
		mDragEnforcer.onDragExit();
		// Here we store the final page that will be dropped to, if the workspace in fact
		// receives the drop
		if( mInScrollArea )
		{
			if( isPageMoving() )
			{
				// If the user drops while the page is scrolling, we should use that page as the
				// destination instead of the page that is being hovered over.
				mDropToLayout = (CellLayout)getPageAt( getNextPage() );
			}
			else
			{
				mDropToLayout = mDragOverlappingLayout;
			}
		}
		else
		{
			mDropToLayout = mDragTargetLayout;
		}
		if( mDragMode == DRAG_MODE_CREATE_FOLDER )
		{
			mCreateUserFolderOnDrop = true;
		}
		else if( mDragMode == DRAG_MODE_ADD_TO_FOLDER )
		{
			mAddToExistingFolderOnDrop = true;
		}
		// Reset the scroll area and previous drag target
		onResetScrollArea();
		setCurrentDropLayout( null );
		setCurrentDragOverlappingLayout( null );
		mSpringLoadedDragController.cancel();
		if( !mIsPageMoving )
		{
			hideOutlines();
		}
	}
	
	void setCurrentDropLayout(
			CellLayout layout )
	{
		if( mDragTargetLayout != null )
		{
			mDragTargetLayout.revertTempState();
			mDragTargetLayout.onDragExit();
		}
		mDragTargetLayout = layout;
		if( mDragTargetLayout != null )
		{
			mDragTargetLayout.onDragEnter();
		}
		cleanupReorder( true );
		cleanupFolderCreation();
		setCurrentDropOverCell( -1 , -1 );
	}
	
	void setCurrentDragOverlappingLayout(
			CellLayout layout )
	{
		if( mDragOverlappingLayout != null )
		{
			mDragOverlappingLayout.setIsDragOverlapping( false );
		}
		mDragOverlappingLayout = layout;
		if( mDragOverlappingLayout != null )
		{
			mDragOverlappingLayout.setIsDragOverlapping( true );
		}
		invalidate();
	}
	
	void setCurrentDropOverCell(
			int x ,
			int y )
	{
		if( x != mDragOverX || y != mDragOverY )
		{
			mDragOverX = x;
			mDragOverY = y;
			setDragMode( DRAG_MODE_NONE );
		}
	}
	
	void setDragMode(
			int dragMode )
	{
		if( dragMode != mDragMode )
		{
			if( dragMode == DRAG_MODE_NONE )
			{
				cleanupAddToFolder();
				// We don't want to cancel the re-order alarm every time the target cell changes
				// as this feels to slow / unresponsive.
				cleanupReorder( false );
				cleanupFolderCreation();
			}
			else if( dragMode == DRAG_MODE_ADD_TO_FOLDER )
			{
				cleanupReorder( true );
				cleanupFolderCreation();
			}
			else if( dragMode == DRAG_MODE_CREATE_FOLDER )
			{
				cleanupAddToFolder();
				cleanupReorder( true );
			}
			else if( dragMode == DRAG_MODE_REORDER )
			{
				cleanupAddToFolder();
				cleanupFolderCreation();
			}
			mDragMode = dragMode;
		}
	}
	
	private void cleanupFolderCreation()
	{
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		if(
		//
		ThemeManager.getInstance().currentThemeIsSystemTheme()
		//
		&& LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7
		//
		&& Build.VERSION.SDK_INT >= 18
		//
		)
		{
			if( mFolderCreateBg != null )
			{
				mFolderCreateBg.animateToRest();
			}
			mFolderCreationAlarm.setIOnAlarmListener( null );
			mFolderCreationAlarm.cancelAlarm();
		}
		else
		//zhujieping add end
		{
			if( mDragFolderRingAnimator != null )
			{
				mDragFolderRingAnimator.animateToNaturalState();
				mDragFolderRingAnimator = null;
			}
			mFolderCreationAlarm.setIOnAlarmListener( null );
			mFolderCreationAlarm.cancelAlarm();
		}
	}
	
	private void cleanupAddToFolder()
	{
		if( mDragOverFolderIcon != null )
		{
			mDragOverFolderIcon.onDragExit( null );
			mDragOverFolderIcon = null;
		}
	}
	
	private void cleanupReorder(
			boolean cancelAlarm )
	{
		// Any pending reorders are canceled
		if( cancelAlarm )
		{
			mReorderAlarm.cancelAlarm();
		}
		mLastReorderX = -1;
		mLastReorderY = -1;
	}
	
	/*
	 *
	 * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
	 * coordinate space. The argument xy is modified with the return result.
	 *
	 * if cachedInverseMatrix is not null, this method will just use that matrix instead of
	 * computing it itself; we use this to avoid redundant matrix inversions in
	 * findMatchingPageForDragOver
	 *
	 */
	void mapPointFromSelfToChild(
			View v ,
			float[] xy ,
			Matrix cachedInverseMatrix )
	{
		xy[0] = xy[0] - v.getLeft();
		xy[1] = xy[1] - v.getTop();
	}
	
	boolean isPointInSelfOverHotseat(
			int x ,
			int y ,
			Rect r )
	{
		if( r == null )
		{
			r = new Rect();
		}
		mTempPt[0] = x;
		mTempPt[1] = y;
		mLauncher.getDragLayer().getDescendantCoordRelativeToSelf( this , mTempPt , true );
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		r = grid.getHotseatRect();
		if( r.contains( mTempPt[0] , mTempPt[1] ) )
		{
			return true;
		}
		return false;
	}
	
	void mapPointFromSelfToHotseatLayout(
			Hotseat hotseat ,
			float[] xy )
	{
		mTempPt[0] = (int)xy[0];
		mTempPt[1] = (int)xy[1];
		mLauncher.getDragLayer().getDescendantCoordRelativeToSelf( this , mTempPt , true );
		mLauncher.getDragLayer().mapCoordInSelfToDescendent( hotseat.getLayout() , mTempPt );
		xy[0] = mTempPt[0];
		xy[1] = mTempPt[1];
	}
	
	/*
	 *
	 * Convert the 2D coordinate xy from this CellLayout's coordinate space to
	 * the parent View's coordinate space. The argument xy is modified with the return result.
	 *
	 */
	void mapPointFromChildToSelf(
			View v ,
			float[] xy )
	{
		xy[0] += v.getLeft();
		xy[1] += v.getTop();
	}
	
	static private float squaredDistance(
			float[] point1 ,
			float[] point2 )
	{
		float distanceX = point1[0] - point2[0];
		float distanceY = point2[1] - point2[1];
		return distanceX * distanceX + distanceY * distanceY;
	}
	
	/*
	 *
	 * This method returns the CellLayout that is currently being dragged to. In order to drag
	 * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
	 * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
	 *
	 * Return null if no CellLayout is currently being dragged over
	 *
	 */
	private CellLayout findMatchingPageForDragOver(
			DragView dragView ,
			float originX ,
			float originY ,
			boolean exact )
	{
		// We loop through all the screens (ie CellLayouts) and see which ones overlap
		// with the item being dragged and then choose the one that's closest to the touch point
		final int screenCount = getChildCount();
		CellLayout bestMatchingScreen = null;
		float smallestDistSoFar = Float.MAX_VALUE;
		for( int i = 0 ; i < screenCount ; i++ )
		{
			// The custom content screen is not a valid drag over option
			// YANGTIANYU@2016/06/20 UPD START
			//if( mScreenOrder.get( i ) == CUSTOM_CONTENT_SCREEN_ID )
			//{
			//	continue;
			//}
			if( isFunctionPageByPageIndex( i ) )
			{
				continue;
			}
			// YANGTIANYU@2016/06/20 UPD END
			CellLayout cl = (CellLayout)getChildAt( i );
			final float[] touchXy = { originX , originY };
			// Transform the touch coordinates to the CellLayout's local coordinates
			// If the touch point is within the bounds of the cell layout, we can return immediately
			cl.getMatrix().invert( mTempInverseMatrix );
			mapPointFromSelfToChild( cl , touchXy , mTempInverseMatrix );
			if( touchXy[0] >= 0 && touchXy[0] <= cl.getWidth() && touchXy[1] >= 0 && touchXy[1] <= cl.getHeight() )
			{
				return cl;
			}
			if( !exact )
			{
				// Get the center of the cell layout in screen coordinates
				final float[] cellLayoutCenter = mTempCellLayoutCenterCoordinates;
				cellLayoutCenter[0] = cl.getWidth() / 2;
				cellLayoutCenter[1] = cl.getHeight() / 2;
				mapPointFromChildToSelf( cl , cellLayoutCenter );
				touchXy[0] = originX;
				touchXy[1] = originY;
				// Calculate the distance between the center of the CellLayout
				// and the touch point
				float dist = squaredDistance( touchXy , cellLayoutCenter );
				if( dist < smallestDistSoFar )
				{
					smallestDistSoFar = dist;
					bestMatchingScreen = cl;
				}
			}
		}
		return bestMatchingScreen;
	}
	
	// This is used to compute the visual center of the dragView. This point is then
	// used to visualize drop locations and determine where to drop an item. The idea is that
	// the visual center represents the user's interpretation of where the item is, and hence
	// is the appropriate point to use when determining drop location.
	private float[] getDragViewVisualCenter(
			int x ,
			int y ,
			int xOffset ,
			int yOffset ,
			DragView dragView ,
			float[] recycle )
	{
		float res[];
		if( recycle == null )
		{
			res = new float[2];
		}
		else
		{
			res = recycle;
		}
		// First off, the drag view has been shifted in a way that is not represented in the
		// x and y values or the x/yOffsets. Here we account for that shift.
		x += LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dragViewOffsetX );
		y += LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dragViewOffsetY );
		// These represent the visual top and left of drag view if a dragRect was provided.
		// If a dragRect was not provided, then they correspond to the actual view left and
		// top, as the dragRect is in that case taken to be the entire dragView.
		// R.dimen.dragViewOffsetY.
		int left = x - xOffset;
		int top = y - yOffset;
		// In order to find the visual center, we shift by half the dragRect
		res[0] = left + dragView.getDragRegion().width() / 2;
		res[1] = top + dragView.getDragRegion().height() / 2;
		return res;
	}
	
	private boolean isDragWidget(
			DragObject d )
	{
		return( d.dragInfo instanceof LauncherAppWidgetInfo || d.dragInfo instanceof PendingAddWidgetInfo );
	}
	
	private boolean isExternalDragWidget(
			DragObject d )
	{
		return d.dragSource != this && isDragWidget( d );
	}
	
	public void onDragOver(
			DragObject d )
	{
		// Skip drag over events while we are dragging over side pages
		if( mInScrollArea || mIsSwitchingState || mState == State.SMALL )
			return;
		Rect r = new Rect();
		CellLayout layout = null;
		ItemInfo item = (ItemInfo)d.dragInfo;
		// Ensure that we have proper spans for the item that we are dropping
		if( item.getSpanX() < 0 || item.getSpanY() < 0 )
			throw new RuntimeException( "Improper spans found" );
		mDragViewVisualCenter = getDragViewVisualCenter( d.x , d.y , d.xOffset , d.yOffset , d.dragView , mDragViewVisualCenter );
		//cheyingkun start	//解决“长按桌面图标，投影偏上”的问题。
		//【问题原因】mDragInfo的赋值在workspace的startDrag中。长按文件夹内的图标直接调用了workspace的beginDragShared方法，没有调用startDrag
		//导致当长按文件夹内应用图标并拖动到桌面时，mDragInfo==null，mDragTargetLayout.visualizeDropLocation无法使用child获取上边距
		//【解决方案】 mDragInfo == null时，使用mCurrentDragView（mCurrentDragView在beginDragShared中赋值），确保图标投影正常。
		//		final View child = ( mDragInfo == null ) ? null : mDragInfo.getCell();//cheyingkun del
		final View child = ( mDragInfo == null ) ? mCurrentDragView : mDragInfo.getCell();//cheyingkun add
		//cheyingkun add end
		// Identify whether we have dragged over a side page
		if( isSmall() )
		{
			if( mLauncher.getHotseat() != null && !isExternalDragWidget( d ) )
			{
				if( isPointInSelfOverHotseat( d.x , d.y , r ) )
				{
					layout = mLauncher.getHotseat().getLayout();
				}
			}
			if( layout == null )
			{
				layout = findMatchingPageForDragOver( d.dragView , d.x , d.y , false );
			}
			if( layout != mDragTargetLayout )
			{
				setCurrentDropLayout( layout );
				setCurrentDragOverlappingLayout( layout );
				boolean isInSpringLoadedMode = ( mState == State.SPRING_LOADED );
				if( isInSpringLoadedMode )
				{
					if( mLauncher.isHotseatLayout( layout ) )
					{
						mSpringLoadedDragController.cancel();
					}
					else
					{
						mSpringLoadedDragController.setAlarm( mDragTargetLayout );
					}
				}
			}
		}
		else
		{
			// Test to see if we are over the hotseat otherwise just use the current page
			if( mLauncher.getHotseat() != null && !isDragWidget( d ) )
			{
				if( isPointInSelfOverHotseat( d.x , d.y , r ) )
				{
					layout = mLauncher.getHotseat().getLayout();
				}
			}
			if( layout == null )
			{
				layout = getCurrentDropLayout();
			}
			if( layout != mDragTargetLayout )
			{
				setCurrentDropLayout( layout );
				setCurrentDragOverlappingLayout( layout );
			}
		}
		// Handle the drag over
		if( mDragTargetLayout != null )
		{
			// We want the point to be mapped to the dragTarget.
			if( mLauncher.isHotseatLayout( mDragTargetLayout ) )
			{
				mapPointFromSelfToHotseatLayout( mLauncher.getHotseat() , mDragViewVisualCenter );
			}
			else
			{
				mapPointFromSelfToChild( mDragTargetLayout , mDragViewVisualCenter , null );
			}
			ItemInfo info = (ItemInfo)d.dragInfo;
			int minSpanX = item.getSpanX();
			int minSpanY = item.getSpanY();
			if( item.getMinSpanX() > 0 && item.getMinSpanY() > 0 )
			{
				minSpanX = item.getMinSpanX();
				minSpanY = item.getMinSpanY();
			}
			mTargetCell = findNearestArea( (int)mDragViewVisualCenter[0] , (int)mDragViewVisualCenter[1] , minSpanX , minSpanY , mDragTargetLayout , mTargetCell );
			int reorderX = mTargetCell[0];
			int reorderY = mTargetCell[1];
			setCurrentDropOverCell( mTargetCell[0] , mTargetCell[1] );
			float targetCellDistance = mDragTargetLayout.getDistanceFromCell( mDragViewVisualCenter[0] , mDragViewVisualCenter[1] , mTargetCell );
			final View dragOverView = mDragTargetLayout.getChildAt( mTargetCell[0] , mTargetCell[1] );
			manageFolderFeedback( info , mDragTargetLayout , mTargetCell , targetCellDistance , dragOverView );
			boolean nearestDropOccupied = mDragTargetLayout.isNearestDropLocationOccupied(
					(int)mDragViewVisualCenter[0] ,
					(int)mDragViewVisualCenter[1] ,
					item.getSpanX() ,
					item.getSpanY() ,
					child ,
					mTargetCell );
			if( !nearestDropOccupied )
			{
				mDragTargetLayout.visualizeDropLocation(
						child ,
						mDragOutline ,
						(int)mDragViewVisualCenter[0] ,
						(int)mDragViewVisualCenter[1] ,
						mTargetCell[0] ,
						mTargetCell[1] ,
						item.getSpanX() ,
						item.getSpanY() ,
						false ,
						d.dragView.getDragVisualizeOffset() ,
						d.dragView.getDragRegion() );
			}
			else if(
			//
			( LauncherDefaultConfig.SWITCH_ENABLE_DRAG_ITEM_PUSH_NORMAL_ITEM_IN_WORKSPACE/* //xiatian add	添加配置项“switch_enable_drag_item_push_other_item_in_workspace”，是否支持“被拖动的桌面图标（应用图标、文件夹、插件），推动其他图标”的功能。true为支持；false为不支持。默认true。 */)
			//
			&& ( mDragMode == DRAG_MODE_NONE || mDragMode == DRAG_MODE_REORDER )
			//
			&& ( !mReorderAlarm.alarmPending() )
			//
			&& ( mLastReorderX != reorderX || mLastReorderY != reorderY )
			//
			)
			{
				// Otherwise, if we aren't adding to or creating a folder and there's no pending
				// reorder, then we schedule a reorder
				ReorderAlarmListener listener = new ReorderAlarmListener( mDragViewVisualCenter , minSpanX , minSpanY , item.getSpanX() , item.getSpanY() , d.dragView , child );
				mReorderAlarm.setIOnAlarmListener( listener );
				mReorderAlarm.setAlarm( REORDER_TIMEOUT );
			}
			if( mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER || !nearestDropOccupied )
			{
				if( mDragTargetLayout != null )
				{
					mDragTargetLayout.revertTempState();
				}
			}
		}
	}
	
	private void manageFolderFeedback(
			ItemInfo info ,
			CellLayout targetLayout ,
			int[] targetCell ,
			float distance ,
			View dragOverView )
	{
		boolean userFolderPending = willCreateUserFolder( info , targetLayout , targetCell , distance , false );
		if( mDragMode == DRAG_MODE_NONE && userFolderPending && !mFolderCreationAlarm.alarmPending() )
		{
			mFolderCreationAlarm.setIOnAlarmListener( new FolderCreationAlarmListener( targetLayout , targetCell[0] , targetCell[1] ) );
			mFolderCreationAlarm.setAlarm( FOLDER_CREATION_TIMEOUT );
			return;
		}
		boolean willAddToFolder = willAddToExistingUserFolder( info , targetLayout , targetCell , distance );
		if( willAddToFolder && mDragMode == DRAG_MODE_NONE )
		{
			mDragOverFolderIcon = ( (FolderIcon)dragOverView );
			mDragOverFolderIcon.onDragEnter( info );
			if( targetLayout != null )
			{
				targetLayout.clearDragOutlines();
			}
			setDragMode( DRAG_MODE_ADD_TO_FOLDER );
			return;
		}
		if( mDragMode == DRAG_MODE_ADD_TO_FOLDER && !willAddToFolder )
		{
			setDragMode( DRAG_MODE_NONE );
		}
		if( mDragMode == DRAG_MODE_CREATE_FOLDER && !userFolderPending )
		{
			setDragMode( DRAG_MODE_NONE );
		}
		return;
	}
	
	class FolderCreationAlarmListener implements IOnAlarmListener
	{
		
		CellLayout layout;
		int cellX;
		int cellY;
		
		public FolderCreationAlarmListener(
				CellLayout layout ,
				int cellX ,
				int cellY )
		{
			this.layout = layout;
			this.cellX = cellX;
			this.cellY = cellY;
		}
		
		public void onAlarm(
				Alarm alarm )
		{
			//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
			if(
			//
			ThemeManager.getInstance().currentThemeIsSystemTheme()
			//
			&& LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7
			//
			&& Build.VERSION.SDK_INT >= 18
			//
			)
			{
				if( mFolderCreateBg != null )
				{
					mFolderCreateBg.animateToRest();
				}
				else
				{
					mFolderCreateBg = new FolderIcon.PreviewBackground();
				}
				DeviceProfile grid = mLauncher.getDeviceProfile();
				View child = layout.getChildAt( cellX , cellY );
				mFolderCreateBg.setup( getResources().getDisplayMetrics() , grid , null , child.getMeasuredWidth() , child.getPaddingTop() );
				mFolderCreateBg.delegateCellX = cellX;
				mFolderCreateBg.delegateCellY = cellY;
				mFolderCreateBg.animateToAccept( layout , cellX , cellY );
				layout.addFolderBackground( mFolderCreateBg );
			}
			else
			//zhujieping add end
			{
				if( mDragFolderRingAnimator != null )
				{
					// This shouldn't happen ever, but just in case, make sure we clean up the mess.
					mDragFolderRingAnimator.animateToNaturalState();
				}
				mDragFolderRingAnimator = new FolderRingAnimator( mLauncher , null );
				mDragFolderRingAnimator.setCell( cellX , cellY );
				mDragFolderRingAnimator.setCellLayout( layout );
				mDragFolderRingAnimator.animateToAcceptState();
				layout.showFolderAccept( mDragFolderRingAnimator );
			}
			layout.clearDragOutlines();
			setDragMode( DRAG_MODE_CREATE_FOLDER );
		}
	}
	
	class ReorderAlarmListener implements IOnAlarmListener
	{
		
		float[] dragViewCenter;
		int minSpanX , minSpanY , spanX , spanY;
		DragView dragView;
		View child;
		
		public ReorderAlarmListener(
				float[] dragViewCenter ,
				int minSpanX ,
				int minSpanY ,
				int spanX ,
				int spanY ,
				DragView dragView ,
				View child )
		{
			this.dragViewCenter = dragViewCenter;
			this.minSpanX = minSpanX;
			this.minSpanY = minSpanY;
			this.spanX = spanX;
			this.spanY = spanY;
			this.child = child;
			this.dragView = dragView;
		}
		
		public void onAlarm(
				Alarm alarm )
		{
			int[] resultSpan = new int[2];
			mTargetCell = findNearestArea( (int)mDragViewVisualCenter[0] , (int)mDragViewVisualCenter[1] , minSpanX , minSpanY , mDragTargetLayout , mTargetCell );
			mLastReorderX = mTargetCell[0];
			mLastReorderY = mTargetCell[1];
			mTargetCell = mDragTargetLayout.createArea(
					(int)mDragViewVisualCenter[0] ,
					(int)mDragViewVisualCenter[1] ,
					minSpanX ,
					minSpanY ,
					spanX ,
					spanY ,
					child ,
					mTargetCell ,
					resultSpan ,
					CellLayout.MODE_DRAG_OVER );
			if( mTargetCell[0] < 0 || mTargetCell[1] < 0 )
			{
				mDragTargetLayout.revertTempState();
			}
			else
			{
				setDragMode( DRAG_MODE_REORDER );
			}
			boolean resize = resultSpan[0] != spanX || resultSpan[1] != spanY;
			mDragTargetLayout.visualizeDropLocation(
					child ,
					mDragOutline ,
					(int)mDragViewVisualCenter[0] ,
					(int)mDragViewVisualCenter[1] ,
					mTargetCell[0] ,
					mTargetCell[1] ,
					resultSpan[0] ,
					resultSpan[1] ,
					resize ,
					dragView.getDragVisualizeOffset() ,
					dragView.getDragRegion() );
		}
	}
	
	@Override
	public void getHitRectRelativeToDragLayer(
			Rect outRect )
	{
		// We want the workspace to have the whole area of the display (it will find the correct
		// cell layout to drop to in the existing drag/drop logic.
		mLauncher.getDragLayer().getDescendantRectRelativeToSelf( this , outRect );
	}
	
	/**
	 * Add the item specified by dragInfo to the given layout.
	 * @return true if successful
	 */
	public boolean addExternalItemToScreen(
			ItemInfo dragInfo ,
			CellLayout layout )
	{
		if( layout.findCellForSpan( mTempEstimate , dragInfo.getSpanX() , dragInfo.getSpanY() ) )
		{
			onDropExternal( dragInfo.getDropPos() , (ItemInfo)dragInfo , (CellLayout)layout , false );
			return true;
		}
		mLauncher.showOutOfSpaceMessage( mLauncher.isHotseatLayout( layout ) );
		return false;
	}
	
	private void onDropExternal(
			int[] touchXY ,
			Object dragInfo ,
			CellLayout cellLayout ,
			boolean insertAtFirst )
	{
		onDropExternal( touchXY , dragInfo , cellLayout , insertAtFirst , null );
	}
	
	/**
	 * Drop an item that didn't originate on one of the workspace screens.
	 * It may have come from Launcher (e.g. from all apps or customize), or it may have
	 * come from another app altogether.
	 *
	 * NOTE: This can also be called when we are outside of a drag event, when we want
	 * to add an item to one of the workspace screens.
	 */
	private void onDropExternal(
			final int[] touchXY ,
			final Object dragInfo ,
			final CellLayout cellLayout ,
			boolean insertAtFirst ,
			DragObject d )
	{
		final Runnable exitSpringLoadedRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				mLauncher.exitSpringLoadedDragModeDelayed( true , false , null );
			}
		};
		ItemInfo info = (ItemInfo)dragInfo;
		int spanX = info.getSpanX();
		int spanY = info.getSpanY();
		if( mDragInfo != null )
		{
			spanX = mDragInfo.getSpanX();
			spanY = mDragInfo.getSpanY();
		}
		final long container = mLauncher.isHotseatLayout( cellLayout ) ? LauncherSettings.Favorites.CONTAINER_HOTSEAT : LauncherSettings.Favorites.CONTAINER_DESKTOP;
		final long screenId = getIdForScreen( cellLayout );
		if( !mLauncher.isHotseatLayout( cellLayout ) && screenId != getScreenIdForPageIndex( mCurrentPage ) && mState != State.SPRING_LOADED )
		{
			snapToScreenId( screenId , null );
		}
		if( info instanceof PendingAddItemInfo )
		{
			final PendingAddItemInfo pendingInfo = (PendingAddItemInfo)dragInfo;
			boolean findNearestVacantCell = true;
			if( pendingInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT )
			{
				mTargetCell = findNearestArea( (int)touchXY[0] , (int)touchXY[1] , spanX , spanY , cellLayout , mTargetCell );
				float distance = cellLayout.getDistanceFromCell( mDragViewVisualCenter[0] , mDragViewVisualCenter[1] , mTargetCell );
				if( willCreateUserFolder( (ItemInfo)d.dragInfo , cellLayout , mTargetCell , distance , true ) || willAddToExistingUserFolder(
						(ItemInfo)d.dragInfo ,
						cellLayout ,
						mTargetCell ,
						distance ) )
				{
					findNearestVacantCell = false;
				}
			}
			final ItemInfo item = (ItemInfo)d.dragInfo;
			boolean updateWidgetSize = false;
			if( findNearestVacantCell )
			{
				int minSpanX = item.getSpanX();
				int minSpanY = item.getSpanY();
				if( item.getMinSpanX() > 0 && item.getMinSpanY() > 0 )
				{
					minSpanX = item.getMinSpanX();
					minSpanY = item.getMinSpanY();
				}
				int[] resultSpan = new int[2];
				mTargetCell = cellLayout.createArea(
						(int)mDragViewVisualCenter[0] ,
						(int)mDragViewVisualCenter[1] ,
						minSpanX ,
						minSpanY ,
						info.getSpanX() ,
						info.getSpanY() ,
						null ,
						mTargetCell ,
						resultSpan ,
						CellLayout.MODE_ON_DROP_EXTERNAL );
				if( resultSpan[0] != item.getSpanX() || resultSpan[1] != item.getSpanY() )
				{
					updateWidgetSize = true;
				}
				item.setSpanX( resultSpan[0] );
				item.setSpanY( resultSpan[1] );
			}
			final Runnable onAnimationCompleteRunnable = new Runnable() {
				
				@Override
				public void run()
				{
					// When dragging and dropping from customization tray, we deal with creating
					// widgets/shortcuts/folders in a slightly different way
					switch( pendingInfo.getItemType() )
					{
						case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
							int span[] = new int[2];
							span[0] = item.getSpanX();
							span[1] = item.getSpanY();
							mLauncher.addAppWidgetFromDrop( (PendingAddWidgetInfo)pendingInfo , container , screenId , mTargetCell , span , null );
							break;
						case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
							mLauncher.processShortcutFromDrop( pendingInfo.getComponentName() , container , screenId , mTargetCell , null );
							break;
						default:
							throw new IllegalStateException( StringUtils.concat( "Unknown item type: " , pendingInfo.getItemType() ) );
					}
				}
			};
			//WangLei add start //bug:0010281 //拖动小部件至当前页面的左右两边页面，当左右页面高亮但还未切页时松手，添加成功后没有跳转到显示插件的页面
			//【原因】拖动插件使页面变高亮，当高亮时间等于DragController.SCROLL_DELAY时才会切页，当高亮时间不够时就松手，添加插件成功后退出编辑模式，桌面会显示当前页而不是插件所在的页面
			//【解决方案】新启一个线程，在添加插件的线程开始运行之前，先判断插件将要添加的页面是否是当前页，若不是就先切换到插件添加的页面，再启动添加插件的线程
			Runnable onDropCompleteRunnable = new Runnable() {
				
				@Override
				public void run()
				{
					int pageIndex = indexOfChild( cellLayout );
					boolean changePage = false;
					if( !mLauncher.isHotseatLayout( cellLayout ) && pageIndex != -1 && pageIndex != mCurrentPage )
					{
						snapToPage( pageIndex , SNAP_WHEN_DROP_DURATION );
						changePage = true;
					}
					if( changePage && mLauncher != null && mLauncher.getHandler() != null )
					{
						mLauncher.getHandler().postDelayed( onAnimationCompleteRunnable , SNAP_WHEN_DROP_DURATION - ADD_PAAWIDGET_AFTER_SNAP );
					}
					else
					{
						onAnimationCompleteRunnable.run();
					}
				}
			};
			//WangLei  add end
			View finalView = pendingInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET ? ( (PendingAddWidgetInfo)pendingInfo ).getAppWidgetHostView() : null;
			if( finalView instanceof AppWidgetHostView && updateWidgetSize )
			{
				AppWidgetHostView awhv = (AppWidgetHostView)finalView;
				AppWidgetResizeFrame.updateWidgetSizeRanges( awhv , mLauncher , item.getSpanX() , item.getSpanY() );
			}
			int animationStyle = ANIMATE_INTO_POSITION_AND_DISAPPEAR;
			if( pendingInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET && ( (PendingAddWidgetInfo)pendingInfo ).getAppWidgetProviderInfo().configure != null )
			{
				animationStyle = ANIMATE_INTO_POSITION_AND_REMAIN;
			}
			//WangLei start //bug:0010281 //拖动小部件至当前页面的左右两边页面，当左右页面高亮但还未切页时松手，添加成功后没有跳转到显示插件的页面
			//animateWidgetDrop( info , cellLayout , d.dragView , onAnimationCompleteRunnable , animationStyle , finalView , true ); //WangLei del
			animateWidgetDrop( info , cellLayout , d.dragView , onDropCompleteRunnable , animationStyle , finalView , true ); //WangLei add
			//WangLei end
		}
		else
		{
			// This is for other drag/drop cases, like dragging from All Apps
			//xiatian start	//整理代码：整理接口creatView
			//xiatian del start
			//			View view = null;
			//			switch( info.getItemType() )
			//			{
			//				case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
			//				case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
			//				case LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL://xiatian add	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
			//					//xiatian add start	//备注
			//					//【这里有个潜在错误】
			//					//若（info instanceof AppInfo）但是（info.getContainer() != NO_ID），
			//					//则这一句“view = mLauncher.createShortcut( R.layout.application , cellLayout , (ShortcutInfo)info );”中
			//					//强转AppInfo到ShortcutInfo会报错。
			//					if( info.getContainer() == NO_ID && info instanceof AppInfo )
			//					{
			//						// Came from all apps -- make a copy
			//						info = new ShortcutInfo( (AppInfo)info );
			//					}
			//					else if( info.getContainer() != NO_ID && info instanceof AppInfo )
			//					{//目前不会出现改情况
			//						throw new IllegalStateException( "ERROR [dragInfo instanceof AppInfo,But container!= NO_ID]" );
			//					}
			//					view = mLauncher.createShortcut( R.layout.application , cellLayout , (ShortcutInfo)info );
			//					break;
			//				case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
			//					view = FolderIcon.fromXml( R.layout.folder_icon , mLauncher , cellLayout , (FolderInfo)info , mIconCache );
			//					break;
			//				default:
			//					throw new IllegalStateException( "Unknown item type: " + info.getItemType() );
			//			}
			//xiatian del end
			View view = info.creatView( mLauncher , cellLayout , mIconCache );//xiatian add
			if( info instanceof ShortcutInfo && ( (ShortcutInfo)info ).isOperateIconItem() )//拖动文件夹的icon到桌面，是重新生成的view，因此需要更新替换
			{
				OperateDynamicMain.updateOperateIcons( view );
			}
			//xiatian end
			boolean isAllAppListItem = false;//cheyingkun add	//解决“长按主菜单应用添加到桌面，再长按删除快捷方式。重启桌面，被删除的快捷方式又出现”的问题。【c_0003267】
			//xiatian add start	//fix bug：解决“从主菜单拖图标到桌面后，数据库中没有存title和intent”的问题。
			if( info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && info.getContainer() == NO_ID && info instanceof AppInfo )
			{//主菜单中的item，先转化成ShortcutInfo之后，再存数据库。（AppInfo存数据库，不会存title和intent）
				info = ( (AppInfo)info ).makeShortcut();
				isAllAppListItem = true;//cheyingkun add	//解决“长按主菜单应用添加到桌面，再长按删除快捷方式。重启桌面，被删除的快捷方式又出现”的问题。【c_0003267】
			}
			//xiatian add end
			// First we find the cell nearest to point at which the item is
			// dropped, without any consideration to whether there is an item there.
			if( touchXY != null )
			{
				mTargetCell = findNearestArea( (int)touchXY[0] , (int)touchXY[1] , spanX , spanY , cellLayout , mTargetCell );
				float distance = cellLayout.getDistanceFromCell( mDragViewVisualCenter[0] , mDragViewVisualCenter[1] , mTargetCell );
				d.postAnimationRunnable = exitSpringLoadedRunnable;
				if( createUserFolderIfNecessary( view , container , cellLayout , mTargetCell , distance , true , d.dragView , d.postAnimationRunnable ) )
				{
					return;
				}
				if( addToExistingFolderIfNecessary( view , cellLayout , mTargetCell , distance , d , true ) )
				{
					return;
				}
			}
			if( touchXY != null )
			{
				// when dragging and dropping, just find the closest free spot
				mTargetCell = cellLayout.createArea( (int)mDragViewVisualCenter[0] , (int)mDragViewVisualCenter[1] , 1 , 1 , 1 , 1 , null , mTargetCell , null , CellLayout.MODE_ON_DROP_EXTERNAL );
			}
			else
			{
				cellLayout.findCellForSpan( mTargetCell , 1 , 1 );
			}
			addInScreen( view , container , screenId , mTargetCell[0] , mTargetCell[1] , info.getSpanX() , info.getSpanY() , insertAtFirst );
			cellLayout.onDropChild( view );
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams)view.getLayoutParams();
			cellLayout.getShortcutsAndWidgets().measureChild( view );
			LauncherModel.addOrMoveItemInDatabase( mLauncher , info , container , screenId , lp.cellX , lp.cellY );
			//cheyingkun add start	//解决“长按主菜单应用添加到桌面，再长按删除快捷方式。重启桌面，被删除的快捷方式又出现”的问题。【c_0003267】
			if( isAllAppListItem )
			{
				view.setTag( info );
			}
			//cheyingkun add end
			if( d.dragView != null )
			{
				// We wrap the animation call in the temporary set and reset of the current
				// cellLayout to its final transform -- this means we animate the drag view to
				// the correct final location.
				//WangLei add start //bug:0011011 //双层模式下，长按主菜单的图标到主页面当前页的左右两页，当页面高亮但还未切页时松手。添加成功后，桌面没有跳转到图标所在的页面
				Runnable onAddCompeleteRunnable = new Runnable() {
					
					@Override
					public void run()
					{
						int pageIndex = indexOfChild( cellLayout );
						boolean changePage = false;
						if( !mLauncher.isHotseatLayout( cellLayout ) && pageIndex != -1 && pageIndex != mCurrentPage )
						{
							snapToPage( pageIndex , SNAP_WHEN_DROP_DURATION );
							changePage = true;
						}
						if( changePage && mLauncher.getHandler() != null )
						{
							mLauncher.getHandler().postDelayed( exitSpringLoadedRunnable , SNAP_WHEN_DROP_DURATION - ADD_PAAWIDGET_AFTER_SNAP );
						}
						else
						{
							exitSpringLoadedRunnable.run();
						}
					}
				};
				//WangLei add end
				setFinalTransitionTransform( cellLayout );
				//WangLei start //bug:0011011 //双层模式下，长按主菜单的图标到主页面当前页的左右两页，当页面高亮但还未切页时松手。添加成功后，桌面没有跳转到图标所在的页面
				//mLauncher.getDragLayer().animateViewIntoPosition( d.dragView , view , exitSpringLoadedRunnable ); //WangLei del
				mLauncher.getDragLayer().animateViewIntoPosition( d.dragView , view , onAddCompeleteRunnable ); //WangLei add
				//WangLei end
				resetTransitionTransform( cellLayout );
			}
		}
	}
	
	public Bitmap createWidgetBitmap(
			ItemInfo widgetInfo ,
			View layout )
	{
		int[] unScaledSize = estimateItemSize( widgetInfo.getSpanX() , widgetInfo.getSpanY() , widgetInfo , false );
		int visibility = layout.getVisibility();
		layout.setVisibility( VISIBLE );
		int width = MeasureSpec.makeMeasureSpec( unScaledSize[0] , MeasureSpec.EXACTLY );
		int height = MeasureSpec.makeMeasureSpec( unScaledSize[1] , MeasureSpec.EXACTLY );
		Bitmap b = Bitmap.createBitmap( unScaledSize[0] , unScaledSize[1] , Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( b );
		layout.measure( width , height );
		layout.layout( 0 , 0 , unScaledSize[0] , unScaledSize[1] );
		layout.draw( c );
		c.setBitmap( null );
		layout.setVisibility( visibility );
		return b;
	}
	
	private void getFinalPositionForDropAnimation(
			int[] loc ,
			float[] scaleXY ,
			DragView dragView ,
			CellLayout layout ,
			ItemInfo info ,
			int[] targetCell ,
			boolean external ,
			boolean scale )
	{
		// Now we animate the dragView, (ie. the widget or shortcut preview) into its final
		// location and size on the home screen.
		int spanX = info.getSpanX();
		int spanY = info.getSpanY();
		Rect r = estimateItemPosition( layout , info , targetCell[0] , targetCell[1] , spanX , spanY );
		loc[0] = r.left;
		loc[1] = r.top;
		setFinalTransitionTransform( layout );
		float cellLayoutScale = mLauncher.getDragLayer().getDescendantCoordRelativeToSelf( layout , loc , true );
		resetTransitionTransform( layout );
		float dragViewScaleX;
		float dragViewScaleY;
		if( scale )
		{
			dragViewScaleX = ( 1.0f * r.width() ) / dragView.getMeasuredWidth();
			dragViewScaleY = ( 1.0f * r.height() ) / dragView.getMeasuredHeight();
		}
		else
		{
			dragViewScaleX = 1f;
			dragViewScaleY = 1f;
		}
		// The animation will scale the dragView about its center, so we need to center about
		// the final location.
		loc[0] -= ( dragView.getMeasuredWidth() - cellLayoutScale * r.width() ) / 2;
		loc[1] -= ( dragView.getMeasuredHeight() - cellLayoutScale * r.height() ) / 2;
		scaleXY[0] = dragViewScaleX * cellLayoutScale;
		scaleXY[1] = dragViewScaleY * cellLayoutScale;
	}
	
	public void animateWidgetDrop(
			ItemInfo info ,
			CellLayout cellLayout ,
			DragView dragView ,
			final Runnable onCompleteRunnable ,
			int animationType ,
			final View finalView ,
			boolean external )
	{
		Rect from = new Rect();
		mLauncher.getDragLayer().getViewRectRelativeToSelf( dragView , from );
		int[] finalPos = new int[2];
		float scaleXY[] = new float[2];
		boolean scalePreview = !( info instanceof PendingAddShortcutInfo );
		getFinalPositionForDropAnimation( finalPos , scaleXY , dragView , cellLayout , info , mTargetCell , external , scalePreview );
		//WangLei add start //bug:0010782 在小部件页面拖动一个配置不为空(需要二次弹窗)的插件添加到桌面当前页的左右两页，在左右页面高亮但还未切页时松手，DragView显示在插件要添加的页面之外
		//【原因】1.拖动的插件配置不为空时(需要二次弹窗)，当dropAnim结束后依然会显示在最终位置上，不会清除，直到取消添加或添加成功才会从DragLayer上remove掉
		//     2.计算DragView的最终停留位置时需要获取插件要添加的页面对应的CellLayout、Workspace和DragLayer的scrollX和scrollY
		//     3.因为桌面没有切换到要添加的页面导致获取Workspace的scrollX与正常情况下相差一个屏幕的宽度。
		//     4.添加到当前页左边的页面少一个屏幕宽度，添加到当前页右边的页面多一个屏幕的宽度，finalPos获取错误，导致DragView显示异常
		//【解决方案】在获取到DragView最终显示的位置后，判断位置参数是否正确，不正确的话根据屏幕宽度和当前桌面缩放比例修改过来
		LauncherAppState app = LauncherAppState.getInstance();
		if( app != null )
		{
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			if( grid != null )
			{
				int windowWidth = grid.getAvailableWidthPx();
				if( finalPos[0] > windowWidth )
				{
					finalPos[0] -= windowWidth * mNewScale;
				}
				else if( finalPos[0] < 0 )
				{
					finalPos[0] += windowWidth * mNewScale;
				}
			}
		}
		//WangLei add end
		Resources res = mLauncher.getResources();
		//<i_0010046> liuhailin@2015-03-19 modify begin
		//动画时间调整,使得动画时间不超过普通图标动画的时间。可以解决极限操作时候进入编辑模式的顺序与onDragEnd的顺序不会出现紊乱。
		//int duration = res.getInteger( R.integer.config_dropAnimMaxDuration ) - 200;
		int duration = LauncherDefaultConfig.getInt( R.integer.config_dropAnimMinDuration );
		//<i_0010046> liuhailin@2015-03-19 modify end
		// In the case where we've prebound the widget, we remove it from the DragLayer
		if( finalView instanceof AppWidgetHostView && external )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "6557954 Animate widget drop, final view is appWidgetHostView" );
			mLauncher.getDragLayer().removeView( finalView );
		}
		if( ( animationType == ANIMATE_INTO_POSITION_AND_RESIZE || external ) && finalView != null )
		{
			Bitmap crossFadeBitmap = createWidgetBitmap( info , finalView );
			dragView.setCrossFadeBitmap( crossFadeBitmap );
			dragView.crossFade( (int)( duration * 0.8f ) );
		}
		else if( info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET && external )
		{
			scaleXY[0] = scaleXY[1] = Math.min( scaleXY[0] , scaleXY[1] );
		}
		DragLayer dragLayer = mLauncher.getDragLayer();
		if( animationType == CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION )
		{
			mLauncher.getDragLayer().animateViewIntoPosition( dragView , finalPos , 0f , 0.1f , 0.1f , DragLayer.ANIMATION_END_DISAPPEAR , onCompleteRunnable , duration );
		}
		else
		{
			int endStyle;
			if( animationType == ANIMATE_INTO_POSITION_AND_REMAIN )
			{
				endStyle = DragLayer.ANIMATION_END_REMAIN_VISIBLE;
			}
			else
			{
				endStyle = DragLayer.ANIMATION_END_DISAPPEAR;
				;
			}
			Runnable onComplete = new Runnable() {
				
				@Override
				public void run()
				{
					if( finalView != null )
					{
						finalView.setVisibility( VISIBLE );
					}
					if( onCompleteRunnable != null )
					{
						onCompleteRunnable.run();
					}
				}
			};
			dragLayer.animateViewIntoPosition( dragView , from.left , from.top , finalPos[0] , finalPos[1] , 1 , 1 , 1 , scaleXY[0] , scaleXY[1] , onComplete , endStyle , duration , this );
		}
	}
	
	public void setFinalTransitionTransform(
			CellLayout layout )
	{
		if( isSwitchingState() )
		{
			mCurrentScale = getScaleX();
			setScaleX( mNewScale );
			setScaleY( mNewScale );
		}
	}
	
	public void resetTransitionTransform(
			CellLayout layout )
	{
		if( isSwitchingState() )
		{
			setScaleX( mCurrentScale );
			setScaleY( mCurrentScale );
		}
	}
	
	/**
	 * Return the current {@link CellLayout}, correctly picking the destination
	 * screen while a scroll is in progress.
	 */
	public CellLayout getCurrentDropLayout()
	{
		return (CellLayout)getChildAt( getNextPage() );
	}
	
	/**
	 * Return the current CellInfo describing our current drag; this method exists
	 * so that Launcher can sync this object with the correct info when the activity is created/
	 * destroyed
	 *
	 */
	public CellInfo getDragInfo()
	{
		return mDragInfo;
	}
	
	public int getRestorePage()
	{
		int mRestorePage = getNextPage();
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//xiatian del start
		//		if( hasFavoritesPage() )
		//		{
		//			mRestorePage--;
		//		}
		//xiatian del end
		mRestorePage -= getFunctionPagesInNormalPageLeftNum();//xiatian add
		//xiatian end
		return mRestorePage;
	}
	
	/**
	 * Calculate the nearest cell where the given object would be dropped.
	 *
	 * pixelX and pixelY should be in the coordinate system of layout
	 */
	private int[] findNearestArea(
			int pixelX ,
			int pixelY ,
			int spanX ,
			int spanY ,
			CellLayout layout ,
			int[] recycle )
	{
		return layout.findNearestArea( pixelX , pixelY , spanX , spanY , recycle );
	}
	
	void setup(
			DragController dragController )
	{
		mSpringLoadedDragController = new SpringLoadedDragController( mLauncher );
		mDragController = dragController;
		// hardware layers on children are enabled on startup, but should be disabled until
		// needed
		updateChildrenLayersEnabled( false );
		// zhujieping@2015/03/27 DEL START,此方法设置壁纸的尺寸，现不走这套流程
		//setWallpaperDimension();
		// zhujieping@2015/03/27 DEL END
	}
	
	/**
	 * Called at the end of a drag which originated on the workspace.
	 */
	public void onDropCompleted(
			final View target ,
			final DragObject d ,
			final boolean isFlingToDelete ,
			final boolean success )
	{
		if( mDeferDropAfterUninstall )
		{
			mDeferredAction = new Runnable() {
				
				public void run()
				{
					onDropCompleted( target , d , isFlingToDelete , success );
					mDeferredAction = null;
				}
			};
			return;
		}
		boolean beingCalledAfterUninstall = mDeferredAction != null;
		if( success && !( beingCalledAfterUninstall && !mUninstallSuccessful ) )
		{
			if( target != this && mDragInfo != null )
			{
				CellLayout parentCell = getParentCellLayoutForView( mDragInfo.getCell() );
				if( parentCell != null )
				{
					//cheyingkun start	//飞利浦卸载应用自动排序（逻辑完善）
					//					parentCell.removeView( mDragInfo.getCell() );//cheyingkun del
					//cheyingkun add start
					View cell = mDragInfo.getCell();
					parentCell.removeView( cell );
					if( mEnableSortAfterUninstall && cell != null )
					{
						removeList.add( (ItemInfo)cell.getTag() );
					}
					//cheyingkun add end
					//cheyingkun end
					//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】
					//删除特定快捷方式时,修改设置界面的开关值
					Object tag = cell.getTag();
					if( tag instanceof ShortcutInfo )//cheyingkun add	//解决“添加插件后删除，桌面重启”的问题【i_0014438】(类型判断)
					{
						ShortcutInfo shortcut = (ShortcutInfo)tag;
						Intent intent = shortcut.getIntent();
						ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( mLauncher );
						if( intent != null //cheyingkun add	//解决“添加插件后删除，桌面重启”的问题【i_0014438】(非空判断)
								&& mZhiKeShortcutManager.isZhiKeShortcut( intent.getComponent() ) )
						{
							String zhikeShortcutSettingKey = mZhiKeShortcutManager.getZhikeShortcutSettingKey( intent );
							mZhiKeShortcutManager.setZhikeShortcutSettingValues( zhikeShortcutSettingKey , 0 );
						}
					}
					//cheyingkun add end
				}
				if( mDragInfo.getCell() instanceof DropTarget )
				{
					mDragController.removeDropTarget( (DropTarget)mDragInfo.getCell() );
				}
				// If we move the item to anything not on the Workspace, check if any empty
				// screens need to be removed. If we dropped back on the workspace, this will
				// be done post drop animation.
				stripEmptyScreens();
			}
		}
		else if( mDragInfo != null )
		{
			CellLayout cellLayout;
			if( mLauncher.isHotseatLayout( target ) )
			{
				cellLayout = mLauncher.getHotseat().getLayout();
			}
			else
			{
				cellLayout = getScreenWithId( mDragInfo.getScreenId() );
			}
			//<i_0010065> liuhailin@2015-03-04 modify begin
			if( cellLayout != null )
			{
				cellLayout.onDropChild( mDragInfo.getCell() );
			}
			//cellLayout.onDropChild( mDragInfo.cell );
			//<i_0010065> liuhailin@2015-03-04 modify end
		}
		if( ( d.cancelled || ( beingCalledAfterUninstall && !mUninstallSuccessful ) ) && mDragInfo.getCell() != null )
		{
			mDragInfo.getCell().setVisibility( VISIBLE );
			//WangLei add start //bug:0010829 //长按底边栏应用拖动到删除框，取消删除后拖动文件夹或应用图标至前面拖动的应用在底边栏的位置处，发生重叠现象
			/**取消删除后，应用图标变为可见，同时设置CellLayout相应位置occupied为true*/
			if( mDragInfo.getCell().getParent() != null && mDragInfo.getCell().getParent().getParent() instanceof CellLayout )
			{
				CellLayout layout = (CellLayout)mDragInfo.getCell().getParent().getParent();
				layout.markCellsAsOccupiedForView( mDragInfo.getCell() );
			}
			//WangLei add end
		}
		mDragOutline = null;
		mDragInfo = null;
		mCurrentDragView = null;//cheyingkun add	//解决“取消T卡挂载模式，长按文件夹内灰色图标，被长按的图标没有变亮”的问题。【i_0011410】
	}
	
	public void deferCompleteDropAfterUninstallActivity()
	{
		mDeferDropAfterUninstall = true;
	}
	
	/// maybe move this into a smaller part
	public void onUninstallActivityReturned(
			boolean success )
	{
		mDeferDropAfterUninstall = false;
		mUninstallSuccessful = success;
		if( mDeferredAction != null )
		{
			mDeferredAction.run();
		}
	}
	
	void updateItemLocationsInDatabase(
			CellLayout cl )
	{
		int count = cl.getShortcutsAndWidgets().getChildCount();
		long screenId = getIdForScreen( cl );
		int container = Favorites.CONTAINER_DESKTOP;
		if( mLauncher.isHotseatLayout( cl ) )
		{
			screenId = -1;
			container = Favorites.CONTAINER_HOTSEAT;
		}
		for( int i = 0 ; i < count ; i++ )
		{
			View v = cl.getShortcutsAndWidgets().getChildAt( i );
			ItemInfo info = (ItemInfo)v.getTag();
			// Null check required as the AllApps button doesn't have an item info
			if( info != null && info.getRequiresDbUpdate() )
			{
				info.setRequiresDbUpdate( false );
				LauncherModel.modifyItemInDatabase( mLauncher , info , container , screenId , info.getCellX() , info.getCellY() , info.getSpanX() , info.getSpanY() );
			}
		}
	}
	
	ArrayList<ComponentName> getUniqueComponents(
			boolean stripDuplicates ,
			ArrayList<ComponentName> duplicates )
	{
		ArrayList<ComponentName> uniqueIntents = new ArrayList<ComponentName>();
		getUniqueIntents( (CellLayout)mLauncher.getHotseat().getLayout() , uniqueIntents , duplicates , false );
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			CellLayout cl = (CellLayout)getChildAt( i );
			getUniqueIntents( cl , uniqueIntents , duplicates , false );
		}
		return uniqueIntents;
	}
	
	void getUniqueIntents(
			CellLayout cl ,
			ArrayList<ComponentName> uniqueIntents ,
			ArrayList<ComponentName> duplicates ,
			boolean stripDuplicates )
	{
		int count = cl.getShortcutsAndWidgets().getChildCount();
		ArrayList<View> children = new ArrayList<View>();
		for( int i = 0 ; i < count ; i++ )
		{
			View v = cl.getShortcutsAndWidgets().getChildAt( i );
			children.add( v );
		}
		for( int i = 0 ; i < count ; i++ )
		{
			View v = children.get( i );
			ItemInfo info = (ItemInfo)v.getTag();
			// Null check required as the AllApps button doesn't have an item info
			if( info instanceof ShortcutInfo )
			{
				ShortcutInfo si = (ShortcutInfo)info;
				ComponentName cn = si.getIntent().getComponent();
				Uri dataUri = si.getIntent().getData();
				// If dataUri is not null / empty or if this component isn't one that would
				// have previously showed up in the AllApps list, then this is a widget-type
				// shortcut, so ignore it.
				if( dataUri != null && !dataUri.equals( Uri.EMPTY ) )
				{
					continue;
				}
				if( !uniqueIntents.contains( cn ) )
				{
					uniqueIntents.add( cn );
				}
				else
				{
					if( stripDuplicates )
					{
						cl.removeViewInLayout( v );
						LauncherModel.deleteItemFromDatabase( mLauncher , si );
					}
					if( duplicates != null )
					{
						duplicates.add( cn );
					}
				}
			}
			if( v instanceof FolderIcon )
			{
				FolderIcon fi = (FolderIcon)v;
				ArrayList<View> items = fi.getFolder().getItemsInReadingOrder();
				for( int j = 0 ; j < items.size() ; j++ )
				{
					if( items.get( j ).getTag() instanceof ShortcutInfo )
					{
						ShortcutInfo si = (ShortcutInfo)items.get( j ).getTag();
						ComponentName cn = si.getIntent().getComponent();
						Uri dataUri = si.getIntent().getData();
						// If dataUri is not null / empty or if this component isn't one that would
						// have previously showed up in the AllApps list, then this is a widget-type
						// shortcut, so ignore it.
						if( dataUri != null && !dataUri.equals( Uri.EMPTY ) )
						{
							continue;
						}
						if( !uniqueIntents.contains( cn ) )
						{
							uniqueIntents.add( cn );
						}
						else
						{
							if( stripDuplicates )
							{
								fi.getFolderInfo().remove( si );
								LauncherModel.deleteItemFromDatabase( mLauncher , si );
							}
							if( duplicates != null )
							{
								duplicates.add( cn );
							}
						}
					}
				}
			}
		}
	}
	
	void saveWorkspaceToDb()
	{
		saveWorkspaceScreenToDb( (CellLayout)mLauncher.getHotseat().getLayout() );
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			CellLayout cl = (CellLayout)getChildAt( i );
			saveWorkspaceScreenToDb( cl );
		}
	}
	
	void saveWorkspaceScreenToDb(
			CellLayout cl )
	{
		int count = cl.getShortcutsAndWidgets().getChildCount();
		long screenId = getIdForScreen( cl );
		int container = Favorites.CONTAINER_DESKTOP;
		Hotseat hotseat = mLauncher.getHotseat();
		if( mLauncher.isHotseatLayout( cl ) )
		{
			screenId = -1;
			container = Favorites.CONTAINER_HOTSEAT;
		}
		for( int i = 0 ; i < count ; i++ )
		{
			View v = cl.getShortcutsAndWidgets().getChildAt( i );
			ItemInfo info = (ItemInfo)v.getTag();
			// Null check required as the AllApps button doesn't have an item info
			if( info != null )
			{
				int cellX = info.getCellX();
				int cellY = info.getCellY();
				if( container == Favorites.CONTAINER_HOTSEAT )
				{
					cellX = hotseat.getCellXFromOrder( (int)info.getScreenId() );
					cellY = hotseat.getCellYFromOrder( (int)info.getScreenId() );
				}
				LauncherModel.addItemToDatabase( mLauncher , info , container , screenId , cellX , cellY , false );
			}
			if( v instanceof FolderIcon )
			{
				FolderIcon fi = (FolderIcon)v;
				fi.getFolder().addItemLocationsInDatabase();
			}
		}
	}
	
	@Override
	public boolean supportsFlingToDelete()
	{
		return true;
	}
	
	@Override
	public void onFlingToDelete(
			DragObject d ,
			int x ,
			int y ,
			PointF vec )
	{
		// Do nothing
	}
	
	@Override
	public void onFlingToDeleteCompleted()
	{
		// Do nothing
	}
	
	public boolean isDropEnabled()
	{
		return true;
	}
	
	@Override
	protected void onRestoreInstanceState(
			Parcelable state )
	{
		super.onRestoreInstanceState( state );
	}
	
	@Override
	protected void dispatchRestoreInstanceState(
			SparseArray<Parcelable> container )
	{
		// We don't dispatch restoreInstanceState to our children using this code path.
		// Some pages will be restored immediately as their items are bound immediately, and
		// others we will need to wait until after their items are bound.
		mSavedStates = container;
	}
	
	public void restoreInstanceStateForChild(
			int child )
	{
		if( mSavedStates != null )
		{
			mRestoredPages.add( child );
			CellLayout cl = (CellLayout)getChildAt( child );
			cl.restoreInstanceState( mSavedStates );
		}
	}
	
	public void restoreInstanceStateForRemainingPages()
	{
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			if( !mRestoredPages.contains( i ) )
			{
				restoreInstanceStateForChild( i );
			}
		}
		mRestoredPages.clear();
		mSavedStates = null;
	}
	
	//cheyingkun start	//光感循环切页(德盛伟业)
	//cheyingkun del start
	//	@Override
	//	public void scrollLeft()
	//	{
	//		if( !isSmall() && !mIsSwitchingState )
	//		{
	//			super.scrollLeft();
	//		}
	//		Folder openFolder = getOpenFolder();
	//		if( openFolder != null )
	//		{
	//			openFolder.completeDragExit();
	//		}
	//	}
	//	
	//	@Override
	//	public void scrollRight()
	//	{
	//		if( !isSmall() && !mIsSwitchingState )
	//		{
	//			super.scrollRight();
	//		}
	//		Folder openFolder = getOpenFolder();
	//		if( openFolder != null )
	//		{
	//			openFolder.completeDragExit();
	//		}
	//	}
	//cheyingkun del end
	//cheyingkun add start
	@Override
	public void scrollLeft(
			boolean isLoop )
	{
		if( !isSmall() && !mIsSwitchingState )
		{
			super.scrollLeft( isLoop );
		}
		Folder openFolder = getOpenFolder();
		if( openFolder != null )
		{
			openFolder.completeDragExit();
		}
	}
	
	@Override
	public void scrollRight(
			boolean isLoop )
	{
		if( !isSmall() && !mIsSwitchingState )
		{
			super.scrollRight( isLoop );
		}
		Folder openFolder = getOpenFolder();
		if( openFolder != null )
		{
			openFolder.completeDragExit();
		}
	}
	
	//cheyingkun add end
	//cheyingkun end
	@Override
	public boolean onEnterScrollArea(
			int x ,
			int y ,
			int direction )
	{
		// Ignore the scroll area if we are dragging over the hot seat
		if( mLauncher.getHotseat() != null )
		{
			Rect r = new Rect();
			mLauncher.getHotseat().getHitRect( r );
			if( r.contains( x , y ) )
			{
				return false;
			}
		}
		boolean result = false;
		if( !isSmall() && !mIsSwitchingState && getOpenFolder() == null )
		{
			mInScrollArea = true;
			final int page = getNextPage() + ( direction == DragController.SCROLL_LEFT ? -1 : 1 );
			// We always want to exit the current layout to ensure parity of enter / exit
			setCurrentDropLayout( null );
			if( 0 <= page && page < getChildCount() )
			{
				// Ensure that we are not dragging over to the custom content screen
				// YANGTIANYU@2016/06/20 UPD START
				//if( getScreenIdForPageIndex( page ) == CUSTOM_CONTENT_SCREEN_ID )
				//{
				//	return false;
				//}
				if( isFunctionPageByPageIndex( page ) )
				{
					return false;
				}
				// YANGTIANYU@2016/06/20 UPD END
				CellLayout layout = (CellLayout)getChildAt( page );
				setCurrentDragOverlappingLayout( layout );
				// Workspace is responsible for drawing the edge glow on adjacent pages,
				// so we need to redraw the workspace when this may have changed.
				invalidate();
				result = true;
			}
		}
		return result;
	}
	
	@Override
	public boolean onExitScrollArea()
	{
		boolean result = false;
		if( mInScrollArea )
		{
			invalidate();
			CellLayout layout = getCurrentDropLayout();
			setCurrentDropLayout( layout );
			setCurrentDragOverlappingLayout( layout );
			result = true;
			mInScrollArea = false;
		}
		return result;
	}
	
	private void onResetScrollArea()
	{
		setCurrentDragOverlappingLayout( null );
		mInScrollArea = false;
	}
	
	/**
	 * Returns a specific CellLayout
	 */
	CellLayout getParentCellLayoutForView(
			View v )
	{
		ArrayList<CellLayout> layouts = getWorkspaceAndHotseatCellLayouts();
		for( CellLayout layout : layouts )
		{
			if( layout.getShortcutsAndWidgets().indexOfChild( v ) > -1 )
			{
				return layout;
			}
		}
		return null;
	}
	
	/**
	 * Returns a list of all the CellLayouts in the workspace.
	 */
	ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts()
	{
		ArrayList<CellLayout> layouts = new ArrayList<CellLayout>();
		int screenCount = getChildCount();
		for( int screen = 0 ; screen < screenCount ; screen++ )
		{
			layouts.add( ( (CellLayout)getChildAt( screen ) ) );
		}
		if( mLauncher.getHotseat() != null )
		{
			layouts.add( mLauncher.getHotseat().getLayout() );
		}
		return layouts;
	}
	
	/**
	 * We should only use this to search for specific children.  Do not use this method to modify
	 * ShortcutsAndWidgetsContainer directly. Includes ShortcutAndWidgetContainers from
	 * the hotseat and workspace pages
	 */
	ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainers()
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = new ArrayList<ShortcutAndWidgetContainer>();
		int screenCount = getChildCount();
		for( int screen = 0 ; screen < screenCount ; screen++ )
		{
			childrenLayouts.add( ( (CellLayout)getChildAt( screen ) ).getShortcutsAndWidgets() );
		}
		if( mLauncher.getHotseat() != null )
		{
			childrenLayouts.add( mLauncher.getHotseat().getLayout().getShortcutsAndWidgets() );
		}
		return childrenLayouts;
	}
	
	public Folder getFolderForTag(
			Object tag )
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int count = layout.getChildCount();
			for( int i = 0 ; i < count ; i++ )
			{
				View child = layout.getChildAt( i );
				if( child instanceof Folder )
				{
					Folder f = (Folder)child;
					if( f.getInfo() == tag && f.getInfo().getOpened() )
					{
						return f;
					}
				}
			}
		}
		return null;
	}
	
	public FolderIcon getFolderForId(
			long id )
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int count = layout.getChildCount();
			for( int i = 0 ; i < count ; i++ )
			{
				View child = layout.getChildAt( i );
				if( child instanceof FolderIcon )
				{
					FolderIcon f = (FolderIcon)child;
					if( f.getFolderInfo().getId() == id )
					{
						return f;
					}
				}
			}
		}
		return null;
	}
	
	public View getViewForTag(
			Object tag )
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int count = layout.getChildCount();
			for( int i = 0 ; i < count ; i++ )
			{
				View child = layout.getChildAt( i );
				if( child.getTag() == tag )
				{
					return child;
				}
			}
		}
		return null;
	}
	
	void clearDropTargets()
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int j = 0 ; j < childCount ; j++ )
			{
				View v = layout.getChildAt( j );
				if( v instanceof DropTarget )
				{
					mDragController.removeDropTarget( (DropTarget)v );
				}
			}
		}
	}
	
	// Removes ALL items that match a given package name, this is usually called when a package
	// has been removed and we want to remove all components (widgets, shortcuts, apps) that
	// belong to that package.
	void removeItemsByPackageName(
			final ArrayList<String> packages )
	{
		final HashSet<String> packageNames = new HashSet<String>();
		packageNames.addAll( packages );
		// Filter out all the ItemInfos that this is going to affect
		final HashSet<ItemInfo> infos = new HashSet<ItemInfo>();
		final HashSet<ComponentName> cns = new HashSet<ComponentName>();
		ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
		for( CellLayout layoutParent : cellLayouts )
		{
			ViewGroup layout = layoutParent.getShortcutsAndWidgets();
			int childCount = layout.getChildCount();
			for( int i = 0 ; i < childCount ; ++i )
			{
				View view = layout.getChildAt( i );
				infos.add( (ItemInfo)view.getTag() );
			}
		}
		LauncherModel.ItemInfoFilter filter = new LauncherModel.ItemInfoFilter() {
			
			@Override
			public boolean filterItem(
					ItemInfo parent ,
					ItemInfo info ,
					ComponentName cn )
			{
				if( packageNames.contains( cn.getPackageName() ) )
				{
					cns.add( cn );
					return true;
				}
				return false;
			}
		};
		LauncherModel.filterItemInfos( infos , filter );
		// Remove the affected components
		removeItemsByComponentName( cns );
	}
	
	// Removes items that match the application info specified, when applications are removed
	// as a part of an update, this is called to ensure that other widgets and application
	// shortcuts are not removed.
	void removeItemsByApplicationInfo(
			final ArrayList<AppInfo> appInfos )
	{
		// Just create a hash table of all the specific components that this will affect
		HashSet<ComponentName> cns = new HashSet<ComponentName>();
		for( AppInfo info : appInfos )
		{
			cns.add( info.getComponentName() );
		}
		// Remove all the things
		removeItemsByComponentName( cns );
	}
	
	public void removeItemsByComponentName(
			final HashSet<ComponentName> componentNames )
	{
		removeItemsByComponentName( componentNames , true );
	}
	
	public void removeItemsByComponentName(
			final HashSet<ComponentName> componentNames ,
			boolean isDeleteInFolder )//isDeleteInFolder为true时，表示同时删除文件夹中的icon，false表示只删除桌面的
	{
		ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
		for( final CellLayout layoutParent : cellLayouts )
		{
			/**当前cellLayout的页面数*/
			long cellScreenId = -1;//cheyingkun add	//飞利浦卸载应用自动排序（逻辑完善）
			final ViewGroup layout = layoutParent.getShortcutsAndWidgets();
			//cheyingkun add start//卸载应用后，其后面的图标是否自动前移。true为自动前移；false为不移动。默认为false。
			final Map<ItemInfo , View> children;
			if( mEnableSortAfterUninstall )
			{
				children = new TreeMap<ItemInfo , View>( new Comparator<ItemInfo>() {
					
					//根据行列数,对item进行排序(从左往右,从上往下)
					@Override
					public int compare(
							ItemInfo lhs ,
							ItemInfo rhs )
					{
						if( !( lhs instanceof ItemInfo && rhs instanceof ItemInfo ) )
						{
							return 0;
						}
						if( lhs.getCellY() > rhs.getCellY() )
						{
							return 1;
						}
						else if( lhs.getCellY() == rhs.getCellY() && lhs.getCellX() > rhs.getCellX() )
						{
							return 1;
						}
						else if( lhs.getCellY() == rhs.getCellY() && lhs.getCellX() == rhs.getCellX() )
						{
							return 0;
						}
						else
						{
							return -1;
						}
					}
				} );
			}
			else
			{
				children = new HashMap<ItemInfo , View>();
			}
			//cheyingkun add end
			for( int j = 0 ; j < layout.getChildCount() ; j++ )
			{
				final View view = layout.getChildAt( j );
				if( view.getTag() != null )//在双层桌面，hotseat中的所有应用的tag为空
				{
					children.put( (ItemInfo)view.getTag() , view );
					//cheyingkun add start	//飞利浦卸载应用自动排序（逻辑完善）
					ItemInfo info = (ItemInfo)view.getTag();
					cellScreenId = info.getScreenId();
					//cheyingkun add end
				}
			}
			final ArrayList<View> childrenToRemove = new ArrayList<View>();
			final HashMap<FolderInfo , ArrayList<ShortcutInfo>> folderAppsToRemove = new HashMap<FolderInfo , ArrayList<ShortcutInfo>>();
			LauncherModel.ItemInfoFilter filter = new LauncherModel.ItemInfoFilter() {
				
				@Override
				public boolean filterItem(
						ItemInfo parent ,
						ItemInfo info ,
						ComponentName cn )
				{
					if( parent instanceof FolderInfo )
					{
						if( componentNames.contains( cn ) )
						{
							FolderInfo folder = (FolderInfo)parent;
							ArrayList<ShortcutInfo> appsToRemove;
							if( folderAppsToRemove.containsKey( folder ) )
							{
								appsToRemove = folderAppsToRemove.get( folder );
							}
							else
							{
								appsToRemove = new ArrayList<ShortcutInfo>();
								folderAppsToRemove.put( folder , appsToRemove );
							}
							appsToRemove.add( (ShortcutInfo)info );
							return true;
						}
					}
					else
					{
						if( componentNames.contains( cn ) )
						{
							childrenToRemove.add( children.get( info ) );
							return true;
						}
					}
					return false;
				}
			};
			LauncherModel.filterItemInfos( children.keySet() , filter );
			// Remove all the apps from their folders
			if( isDeleteInFolder )
			{
				for( FolderInfo folder : folderAppsToRemove.keySet() )
				{
					ArrayList<ShortcutInfo> appsToRemove = folderAppsToRemove.get( folder );
					for( ShortcutInfo info : appsToRemove )
					{
						//cheyingkun add start	//卸载应用时判断虚图标是否跟随apk删除
						if( !( info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL// 
						&& !info.makeVirtual().getIsFollowAppUninstall() ) )
						//cheyingkun add end
						{
							//cheyingkun add start	//解决“两个图标重叠生成文件夹，长按此文件夹时PC端将其中一个应用卸载，卸载成功后松开文件夹，松开后桌面停止运行”的问题。【0010786】
							if( isUnInstallAppInDraggingFolder( info ) )//卸载的应用在拖拽的文件夹内
							{
								mDragController.cancelDrag();//停止拖拽
							}
							//cheyingkun add end
							folder.remove( info );
						}
					}
				}
			}
			// Remove all the other children
			for( View child : childrenToRemove )
			{
				// Note: We can not remove the view directly from CellLayoutChildren as this
				// does not re-mark the spaces as unoccupied.
				//cheyingkun add start	//修改桌面默认配置
				Object tag = child.getTag();
				if( !( tag != null && tag instanceof ShortcutInfo//
						&& ( (ShortcutInfo)tag ).getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL//cheyingkun add	//卸载应用时判断虚图标是否跟随apk删除
				&& !( (ShortcutInfo)tag ).makeVirtual().getIsFollowAppUninstall()// 
				) )
				//cheyingkun add end
				{
					layoutParent.removeViewInLayout( child );
					if( child instanceof DropTarget )
					{
						mDragController.removeDropTarget( (DropTarget)child );
					}
					//cheyingkun add start	//卸载应用后，其后面的图标是否自动前移。true为自动前移；false为不移动。默认为false。
					if( mEnableSortAfterUninstall )
					{
						startAnimAfterUninstall( children , (ItemInfo)child.getTag() , layoutParent );//cheyingkun add	//卸载应用后，其后面的图标是否自动前移。true为自动前移；false为不移动。默认为false。
					}
					//cheyingkun add end
				}
			}
			if( childrenToRemove.size() > 0 )
			{
				layout.requestLayout();
				layout.invalidate();
			}
			//cheyingkun add start	//飞利浦卸载应用自动排序（逻辑完善）
			//【问题原因】如果卸载应用先走到onDropCompleted，cellLayout已经removeView，在removeItemsByPackageName时获取所有页面的页面数，并根据packageName获取componentName时出错(获取不到或者个数不对)。
			//		导致卸载了应用的页面,childrenToRemove==0,移动动画的地方走不到
			//如果当前页childrenToRemove等于零,有两种情况
			//1.当前页没有卸载应用(不作处理)
			//2.当前页卸载了应用,childrenToRemove.size=0
			//	2.1当前页卸载了应用但是componentNames.size=0,导致childrenToRemove.size=0
			//	2.2.当前页卸载了应用,componentNames大于0,但不是当前页的图标,而是其它页该应用的快捷方式或者小部件.卸载应用页面的childrenToRemove.size=0
			//【解决方案】把卸载的view的ItemInfo保存到removeList中，如果获取的childrenToRemove 大小为0，则根据removeList中的info信息，做当前页卸载应用的位移动画 
			else
			{
				//如果 childrenToRemove.size() = 0 
				//循环卸载应用列表
				for( ItemInfo removeInfo : removeList )
				{
					//如果removeInfo的页面id等于当前cellLayout的页面id,并且打开开关
					final long screenId = removeInfo.getScreenId();
					if( cellScreenId == screenId && mEnableSortAfterUninstall )
					{
						startAnimAfterUninstall( children , removeInfo , layoutParent );//cheyingkun add	//卸载应用后，其后面的图标是否自动前移。true为自动前移；false为不移动。默认为false。
					}
				}
			}
			//cheyingkun add end
		}
		//cheyingkun add start	//飞利浦卸载应用自动排序（逻辑完善）
		if( mEnableSortAfterUninstall )
		{
			removeList.clear();
		}
		//cheyingkun add end
		// Strip all the empty screens
		stripEmptyScreens();
	}
	
	void updateShortcuts(
			ArrayList<AppInfo> apps )
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int j = 0 ; j < childCount ; j++ )
			{
				final View view = layout.getChildAt( j );
				Object tag = view.getTag();
				if( LauncherModel.isShortcutInfoUpdateable( (ItemInfo)tag ) )
				{
					ShortcutInfo info = (ShortcutInfo)tag;
					final Intent intent = info.getIntent();
					final ComponentName name = intent.getComponent();
					final int appCount = apps.size();
					for( int k = 0 ; k < appCount ; k++ )
					{
						AppInfo app = apps.get( k );
						if( app.getComponentName().equals( name ) )
						{
							BubbleTextView shortcut = (BubbleTextView)view;
							info.updateIcon( mIconCache );
							//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
							//info.title = app.title.toString();
							if( app.getTitle() == null )
							{
								app.setTitle( "name = null" );
							}
							info.setTitle( app.getTitle().toString() );
							//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
							shortcut.applyFromShortcutInfo( info , mIconCache );
						}
					}
				}
			}
		}
	}
	
	private void moveToScreen(
			int page ,
			boolean animate )
	{
		if( !isSmall() )
		{
			if( animate )
			{
				snapToPage( page );
			}
			else
			{
				setCurrentPage( page );
			}
		}
		View child = getChildAt( page );
		if( child != null )
		{
			child.requestFocus();
		}
	}
	
	void moveToDefaultScreen(
			boolean animate )
	{
		//xiatian start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		int mDefaultPageIndex = mDefaultPage;//xiatian del
		int mDefaultPageIndex = getDefaultPageIndex();//xiatian add
		//xiatian end
		if( getCurrentPage() != mDefaultPageIndex )
		{
			moveToScreen( mDefaultPageIndex , animate );
		}
	}
	
	void moveToFavoritesPage(
			boolean animate )
	{
		if( hasFavoritesPage() )
		{
			int ccIndex = getPageIndexForScreenId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			if( animate )
			{
				snapToPage( ccIndex );
			}
			else
			{
				setCurrentPage( ccIndex );
			}
			View child = getChildAt( ccIndex );
			if( child != null )
			{
				child.requestFocus();
			}
		}
	}
	
	@Override
	protected PageMarkerResources getPageIndicatorMarker(
			int pageIndex )
	{
		PageIndicator mPageIndicator = getPageIndicator();
		long screenId = getScreenIdForPageIndex( pageIndex );
		if( screenId == EXTRA_EMPTY_SCREEN_ID )
		{
			int count = mScreenOrder.size();
			//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			//xiatian del start
			//			if( hasFavoritesPage() )
			//			{
			//				count--;
			//			}
			//xiatian del end
			count -= getFunctionPagesInNormalPageLeftNum();//xiatian add
			//xiatian end
			if( count > 1 )
			{
				return mPageIndicator.getAddPageMarkerResources();
			}
		}
		// YANGTIANYU@2016/07/04 ADD START
		// 专属页指示器
		else if( screenId == FUNCTION_CAMERA_PAGE_SCREEN_ID )
		{
			return mPageIndicator.getCameraPageMarkerResources();
		}
		else if( screenId == FUNCTION_MUSIC_PAGE_SCREEN_ID )
		{
			return mPageIndicator.getMusicPageMarkerResources();
		}
		// YANGTIANYU@2016/07/04 ADD END
		//cheyingkun add start	//phenix仿S5效果(页面指示器)
		//酷生活特殊指示器
		else if( screenId == FUNCTION_FAVORITES_PAGE_SCREEN_ID )
		{
			return mPageIndicator.getFavoritesPageMarkerResources();
		}
		return super.getPageIndicatorMarker( pageIndex );
	}
	
	@Override
	public void syncPages()
	{
	}
	
	@Override
	public void syncPageItems(
			int page ,
			boolean immediate )
	{
	}
	
	public void getLocationInDragLayer(
			int[] loc )
	{
		mLauncher.getDragLayer().getLocationInDragLayer( this , loc );
	}
	
	void setScreen(
			int currentScreen )
	{
		if( !mScroller.isFinished() )
			mScroller.abortAnimation();
		mCurrentPage = Math.max( 0 , Math.min( currentScreen , getChildCount() - 1 ) );
		scrollTo( this.getCurrentScreen() * mLauncher.getWindowManager().getDefaultDisplay().getWidth() , 0 );
	}
	
	public int getCurrentScreen()
	{
		return this.mCurrentPage;
	}
	
	public void restoreWorkspace()
	{
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View page = null;
			if( ( page = getChildAt( i ) ) != null )
			{
				if( page instanceof CellLayout )
				{
					page.setTranslationX( 0f );
					page.setTranslationY( 0f );
					page.setRotation( 0f );
					page.setRotationX( 0f );
					page.setRotationY( 0f );
					page.setAlpha( 1f );
					page.setScaleX( 1f );
					page.setScaleY( 1f );
					ShortcutAndWidgetContainer shortcutAndWidgetContainer = ( (CellLayout)page ).getShortcutsAndWidgets();
					for( int j = 0 ; j < shortcutAndWidgetContainer.getChildCount() ; j++ )
					{
						View view = null;
						if( ( view = shortcutAndWidgetContainer.getChildAt( j ) ) == null )
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
		restoreWorkspace();
	}
	
	//xiatian add start	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
	public void updateUnreadNumberByComponent(
			ComponentName mComponentName ,
			final int mUnreadNum )
	{
		if( mComponentName == null )
		{
			return;
		}
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				final View mChildView = layout.getChildAt( i );
				Object tag = mChildView.getTag();
				if( tag instanceof ShortcutInfo )
				{
					ShortcutInfo mShortcutInfo = (ShortcutInfo)tag;
					final Intent intent = mShortcutInfo.getIntent();
					final ComponentName name = intent.getComponent();
					if( mShortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
					{
						if( mComponentName.equals( name ) )
						{
							if( mChildView instanceof BubbleTextView )
							{
								BubbleTextView mBubbleTextView = (BubbleTextView)mChildView;
								mBubbleTextView.applyFromShortcutInfo( mShortcutInfo , mIconCache );
							}
							//return; //WangLei del  //bug:c_0003060 c_0003047 双层模式下桌面有多个电话和短信图标，有未接电话和为读短信时，显示的数目不对 
						}
					}
				}
				if( tag instanceof FolderInfo )
				{
					FolderIcon folderIcon = (FolderIcon)mChildView;
					ArrayList<View> mFolderChildViewList = folderIcon.getFolder().getItemsInReadingOrder();
					for( View mFolderChild : mFolderChildViewList )
					{
						ShortcutInfo mShortcutInfo = (ShortcutInfo)mFolderChild.getTag();
						final Intent intent = mShortcutInfo.getIntent();
						final ComponentName name = intent.getComponent();
						if( mShortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
						{
							if( mComponentName.equals( name ) )
							{
								if( mFolderChild instanceof BubbleTextView )
								{
									BubbleTextView mBubbleTextView = (BubbleTextView)mFolderChild;
									mBubbleTextView.applyFromShortcutInfo( mShortcutInfo , mIconCache );
									folderIcon.invalidate();
								}
								//return; //WangLei del  //bug:c_0003060 c_0003047 双层模式下桌面有多个电话和短信图标，有未接电话和为读短信时，显示的数目不对 
							}
						}
					}
				}
			}
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//fix bug：解决“在双层模式下，在编辑模式长按一个页面时，pc端安装应用，安装成功之后，桌面自动退出编辑模式，但此时被长按的页面所有图标呈托起状态”的问题。【i_0010545】
	public void exitOverviewModeIfInReordering(
			boolean animated ,
			boolean mNeedSnapToPage/* 退出Reordering状态时，是否要“自动滑动到指定页面”或“自动滑动到最近的页面” */)
	{
		exitOverviewModeIfInReordering( -1 , animated , mNeedSnapToPage );
	}
	
	public void exitOverviewModeIfInReordering(
			int snapPage ,
			boolean animated ,
			boolean mNeedSnapToPage /* 退出Reordering状态时，是否要“自动滑动到指定页面（snapPage != -1）”或“自动滑动到最近的页面（snapPage == 1）” */)
	{
		enableOverviewModeIfInReordering( false , snapPage , animated , mNeedSnapToPage );
	}
	
	private void enableOverviewModeIfInReordering(
			final boolean enable ,
			int snapPage ,
			boolean animated ,
			boolean mNeedSnapToPage /* 退出Reordering状态时，是否要“自动滑动到指定页面（snapPage != -1）”或“自动滑动到最近的页面（snapPage == 1）” */)
	{
		State finalState = Workspace.State.OVERVIEW;
		if( !enable )
		{
			finalState = Workspace.State.NORMAL;
		}
		Animator workspaceAnim = getChangeStateAnimationIfInReordering( finalState , animated , 0 , snapPage , mNeedSnapToPage );
		if( workspaceAnim != null )
		{
			onTransitionPrepare();
			workspaceAnim.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(
						Animator arg0 )
				{
					onTransitionEnd();
					initAnimationStyle( Workspace.this );
					restoreWorkspace();
					checkSelectedPageWhenChangeState( enable );//cheyingkun add	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
				}
			} );
			workspaceAnim.start();
		}
		else
		{
			initAnimationStyle( Workspace.this );
			checkSelectedPageWhenChangeState( enable );//cheyingkun add	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
		}
	}
	
	Animator getChangeStateAnimationIfInReordering(
			final State state ,
			boolean animated ,
			int delay ,
			int snapPage ,
			boolean mNeedSnapToPage /* 退出Reordering状态时，是否要“自动滑动到指定页面（snapPage != -1）”或“自动滑动到最近的页面（snapPage == 1）” */)
	{
		if( mState == state )
		{
			return null;
		}
		// Initialize animation arrays for the first time if necessary
		//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
		if( LauncherDefaultConfig.isAllowEmptyScreen() )
		{
			if( state == State.OVERVIEW )
			{
				addExtraAddPageScreen();
				mLauncher.getSearchDropTargetBar().setDeleteDropTargetVisibility( true );
				setDragCellLayoutListener( mLauncher.getSearchDropTargetBar() );
			}
			else
			{
				removeExtraAddPageScreen();
				mLauncher.getSearchDropTargetBar().setDeleteDropTargetVisibility( false );
				setDragCellLayoutListener( null );
			}
		}
		//zhujieping add end
		initAnimationArrays();
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		final State oldState = mState;
		final boolean oldStateIsNormal = ( oldState == State.NORMAL );
		final boolean oldStateIsSpringLoaded = ( oldState == State.SPRING_LOADED );
		final boolean oldStateIsSmall = ( oldState == State.SMALL );
		final boolean oldStateIsOverview = ( oldState == State.OVERVIEW );
		setState( state );
		final boolean stateIsNormal = ( state == State.NORMAL );
		final boolean stateIsSpringLoaded = ( state == State.SPRING_LOADED );
		final boolean stateIsSmall = ( state == State.SMALL );
		final boolean stateIsOverview = ( state == State.OVERVIEW );
		float finalBackgroundAlpha = ( stateIsSpringLoaded || stateIsOverview ) ? 1.0f : 0f;
		float finalHotseatAndPageIndicatorAlpha = ( stateIsOverview || stateIsSmall ) ? 0f : 1f;
		float finalOverviewPanelAlpha = stateIsOverview ? 1f : 0f;
		float finalSearchBarAlpha = !stateIsNormal ? 0f : 1f;
		float finalWorkspaceTranslationY = stateIsOverview ? getOverviewModeTranslationY() : 0;
		boolean workspaceToAllApps = ( oldStateIsNormal && stateIsSmall );
		boolean allAppsToWorkspace = ( oldStateIsSmall && stateIsNormal );
		boolean workspaceToOverview = ( oldStateIsNormal && stateIsOverview );
		boolean overviewToWorkspace = ( oldStateIsOverview && stateIsNormal );
		mNewScale = 1.0f;
		if( oldStateIsOverview )
		{
			if( isReordering( false ) )
			{
				endReorderingForceAndWithoutAnim( !overviewToWorkspace , mNeedSnapToPage );
			}
			else
			{
				disableFreeScroll( snapPage );
			}
			//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。(解决“编辑模式界面安装应用，安装完成后，手机端弹出360助手提示框，桌面退 出编辑模式同时设置主页图标显示在桌面上”的问题。【i_0014882】)
			hideEditModeHomeView();
			//gaominghui add end 
		}
		else if( stateIsOverview )
		{
			enableFreeScroll();
		}
		if( state != State.NORMAL )
		{
			if( stateIsSpringLoaded )
			{
				mNewScale = mSpringLoadedShrinkFactor;
			}
			else if( stateIsOverview )
			{
				mNewScale = mOverviewModeShrinkFactor;
			}
			else if( stateIsSmall )
			{
				mNewScale = mOverviewModeShrinkFactor - 0.3f;
			}
			if( workspaceToAllApps )
			{
				updateChildrenLayersEnabled( false );
			}
		}
		final int duration;
		if( workspaceToAllApps )
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_APPLIST;
		}
		else if( workspaceToOverview )
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_EDITMODE;
		}
		else if( overviewToWorkspace )
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_EDITMODE_TO_WORKSPACE;
		}
		else
		{
			duration = LauncherDefaultConfig.CONFIG_ANIMATION_DURATION_WHEN_APPLIST_TO_WORKSPACE;
		}
		// zhujieping@2015/03/25 ADD START
		//这个动画也会改变cellLayout的setBackgroundAlpha，与下方要执行的动画冲突，同理searchbar，因此停掉动画【i_0010653】
		stopOutlinesFadeOuntAnimation();
		mLauncher.getSearchDropTargetBar().stopSearchBarAnim();
		// zhujieping@2015/03/25 ADD END
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			final CellLayout cl = (CellLayout)getChildAt( i );
			boolean isCurrentPage = ( i == getNextPage() );
			float initialAlpha = cl.getShortcutsAndWidgets().getAlpha();
			float finalAlpha = stateIsSmall ? 0f : 1f;
			// If we are animating to/from the small state, then hide the side pages and fade the
			// current page in
			if( !mIsSwitchingState )
			{
				if( workspaceToAllApps || allAppsToWorkspace )
				{
					if( allAppsToWorkspace && isCurrentPage )
					{
						initialAlpha = 0f;
					}
					else if( !isCurrentPage )
					{
						initialAlpha = finalAlpha = 0f;
					}
					cl.setShortcutAndWidgetAlpha( initialAlpha );
				}
			}
			mOldAlphas[i] = initialAlpha;
			mNewAlphas[i] = finalAlpha;
			if( animated )
			{
				mOldBackgroundAlphas[i] = cl.getBackgroundAlpha();
				mNewBackgroundAlphas[i] = finalBackgroundAlpha;
			}
			else
			{
				cl.setBackgroundAlpha( finalBackgroundAlpha );
				cl.setShortcutAndWidgetAlpha( finalAlpha );
			}
		}
		final View searchBar = mLauncher.getSearchBar();
		final View overviewPanel = mLauncher.getOverviewPanel();
		final View hotseat = mLauncher.getHotseat();
		// zhujieping@2015/04/07 UPD START
		//编辑模式下，搜索框不显示。进入编辑模式时置标志位，退出时置回标志位。例如智能分类不成功退出编辑模式时animated为false，所以应该放在if的外面。
		mLauncher.getSearchDropTargetBar().setSearchBarIfHide( stateIsOverview );
		// zhujieping@2015/04/07 UPD END
		if( animated )
		{
			anim.setDuration( duration );
			LauncherViewPropertyAnimator scale = new LauncherViewPropertyAnimator( this );
			scale.scaleX( mNewScale ).scaleY( mNewScale ).translationY( finalWorkspaceTranslationY ).setInterpolator( mZoomInInterpolator );
			anim.play( scale );
			for( int index = 0 ; index < getChildCount() ; index++ )
			{
				final int i = index;
				final CellLayout cl = (CellLayout)getChildAt( i );
				float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
				if( mOldAlphas[i] == 0 && mNewAlphas[i] == 0 )
				{
					cl.setBackgroundAlpha( mNewBackgroundAlphas[i] );
					cl.setShortcutAndWidgetAlpha( mNewAlphas[i] );
				}
				else
				{
					if( mOldAlphas[i] != mNewAlphas[i] || currentAlpha != mNewAlphas[i] )
					{
						LauncherViewPropertyAnimator alphaAnim = new LauncherViewPropertyAnimator( cl.getShortcutsAndWidgets() );
						alphaAnim.alpha( mNewAlphas[i] ).setInterpolator( mZoomInInterpolator );
						anim.play( alphaAnim );
					}
					if( mOldBackgroundAlphas[i] != 0 || mNewBackgroundAlphas[i] != 0 )
					{
						ValueAnimator bgAnim = LauncherAnimUtils.ofFloat( cl , 0f , 1f );
						bgAnim.setInterpolator( mZoomInInterpolator );
						bgAnim.addUpdateListener( new LauncherAnimatorUpdateListener() {
							
							public void onAnimationUpdate(
									float a ,
									float b )
							{
								//xiatian start	//添加保护，防止数组越界
								//								cl.setBackgroundAlpha( a * mOldBackgroundAlphas[i] + b * mNewBackgroundAlphas[i] );//xiatian del
								//xiatian add start
								float mAlpha = -1;
								int mOldBackgroundAlphasIndex = i;
								int mNewBackgroundAlphasIndex = i;
								if( mOldBackgroundAlphas.length <= i )
								{
									mOldBackgroundAlphasIndex = mOldBackgroundAlphas.length - 1;
								}
								if( mNewBackgroundAlphas.length <= i )
								{
									mNewBackgroundAlphasIndex = mNewBackgroundAlphas.length - 1;
								}
								mAlpha = a * mOldBackgroundAlphas[mOldBackgroundAlphasIndex] + b * mNewBackgroundAlphas[mNewBackgroundAlphasIndex];
								cl.setBackgroundAlpha( mAlpha );
								//xiatian add end
								//xiatian end
							}
						} );
						anim.play( bgAnim );
					}
				}
			}
			ObjectAnimator pageIndicatorAlpha = null;
			ObjectAnimator pageIndicatorY = null;//cheyingkun add	//phenix仿S5效果,编辑模式页面指示器
			View pageIndicatorParent = mLauncher.findViewById( R.id.page_indicator );
			if( pageIndicatorParent != null && getPageIndicator() != null )
			{
				//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
				if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
				{
					LauncherAppState app = LauncherAppState.getInstance();
					float pageIndicatorYInOverviewMode = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInOverviewMode();
					float pageIndicatorYInNormal = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInNormal();
					float y = stateIsOverview ? pageIndicatorYInOverviewMode : pageIndicatorYInNormal;
					pageIndicatorY = ObjectAnimator.ofFloat( pageIndicatorParent , "y" , y );//zhujieping //pageIndicatorParent是getPageIndicator()的父，这里设置pageIndicatorParent的y
					float finalAlpha = stateIsSmall ? 0f : 1f;
					pageIndicatorAlpha = ObjectAnimator.ofFloat( getPageIndicator() , "alpha" , finalAlpha );
				}
				else
				//cheyingkun add end
				{
					pageIndicatorAlpha = ObjectAnimator.ofFloat( getPageIndicator() , "alpha" , finalHotseatAndPageIndicatorAlpha );
				}
			}
			ObjectAnimator hotseatAlpha = ObjectAnimator.ofFloat( hotseat , "alpha" , finalHotseatAndPageIndicatorAlpha );
			ObjectAnimator overviewPanelAlpha = ObjectAnimator.ofFloat( overviewPanel , "alpha" , finalOverviewPanelAlpha );
			overviewPanelAlpha.addListener( new AlphaUpdateListener( overviewPanel ) );
			hotseatAlpha.addListener( new AlphaUpdateListener( hotseat ) );
			//cheyingkun add start	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			ObjectAnimator searchBarAlpha = null;
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && searchBar != null )
			//cheyingkun add end
			{
				searchBarAlpha = ObjectAnimator.ofFloat( searchBar , "alpha" , finalSearchBarAlpha );
				searchBarAlpha.addListener( new AlphaUpdateListener( searchBar ) );
			}
			if( workspaceToOverview )
			{
				hotseatAlpha.setInterpolator( new DecelerateInterpolator( 2 ) );
			}
			else if( overviewToWorkspace )
			{
				overviewPanelAlpha.setInterpolator( new DecelerateInterpolator( 2 ) );
			}
			if( getPageIndicator() != null )
			{
				if( pageIndicatorAlpha != null )
				{
					pageIndicatorAlpha.addListener( new AlphaUpdateListener( getPageIndicator() ) );
					anim.play( pageIndicatorAlpha );
				}
				//cheyingkun add start	//phenix仿S5效果,编辑模式页面指示器
				if( pageIndicatorY != null )
				{
					anim.play( pageIndicatorY );
				}
				//cheyingkun add end
			}
			anim.play( overviewPanelAlpha );
			anim.play( hotseatAlpha );
			anim.play( searchBarAlpha );
			anim.setStartDelay( delay );
		}
		else
		{
			overviewPanel.setAlpha( finalOverviewPanelAlpha );
			AlphaUpdateListener.updateVisibility( overviewPanel );
			hotseat.setAlpha( finalHotseatAndPageIndicatorAlpha );
			AlphaUpdateListener.updateVisibility( hotseat );
			View pageIndicatorParent = mLauncher.findViewById( R.id.page_indicator );
			if( pageIndicatorParent != null && getPageIndicator() != null )
			{
				//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
				if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
				{
					LauncherAppState app = LauncherAppState.getInstance();
					float pageIndicatorYInOverviewMode = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInOverviewMode();
					float pageIndicatorYInNormal = app.getDynamicGrid().getDeviceProfile().getPageIndicatorYInNormal();
					float y = stateIsOverview ? pageIndicatorYInOverviewMode : pageIndicatorYInNormal;
					float finalAlpha = stateIsSmall ? 0f : 1f;
					getPageIndicator().setAlpha( finalAlpha );
					pageIndicatorParent.setY( y );//zhujieping //pageIndicatorParent是getPageIndicator()的父，这里设置pageIndicatorParent的y
				}
				else
				//cheyingkun add end
				{
					getPageIndicator().setAlpha( finalHotseatAndPageIndicatorAlpha );
				}
				AlphaUpdateListener.updateVisibility( getPageIndicator() );
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && searchBar != null )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			{
				searchBar.setAlpha( finalSearchBarAlpha );
				AlphaUpdateListener.updateVisibility( searchBar );
			}
			updateFunctionPagesVisibility();
			setScaleX( mNewScale );
			setScaleY( mNewScale );
			setTranslationY( finalWorkspaceTranslationY );
		}
		// zhujieping@2015/05/27 DEL START,在search_bar布局中也存在这个voice_button，无需重复
		//		//WangLei start //bug:0010441 //当语音搜索图标不可用时，点击整个搜索框都相应onClickSearchButton
		//		//mLauncher.updateVoiceButtonProxyVisible( false );
		//		mLauncher.updateVoiceButtonProxyVisible( true ); //WangLei add 
		//		//WangLei add end
		// zhujieping@2015/05/27 DEL END,在search_bar布局中也存在这个voice_button，无需重复
		if( stateIsSpringLoaded )
		{
			// Right now we're covered by Apps Customize
			// Show the background gradient immediately, so the gradient will
			// be showing once AppsCustomize disappears
			animateBackgroundGradient( LauncherDefaultConfig.getInt( R.integer.config_springLoadedBgAlpha ) / 100f , false );
		}
		else if( stateIsOverview )
		{
			animateBackgroundGradient( LauncherDefaultConfig.getInt( R.integer.config_overviewModeBgAlpha ) / 100f , animated );
		}
		else
		{
			// Fade the background gradient away
			animateBackgroundGradient( 0f , animated );
		}
		// zhangjin@2015/07/31 增加状态切换动画保护 ADD START  i_11917
		if( mChangeStateAnim != null && mChangeStateAnim.isRunning() )
		{
			mChangeStateAnim.cancel();
		}
		mChangeStateAnim = anim;
		// zhangjin@2015/07/31 ADD END
		return anim;
	}
	
	Animator getChangeStateAnimationIfInReordering(
			final State state ,
			boolean animated ,
			boolean mNeedSnapToPage )
	{
		return getChangeStateAnimationIfInReordering( state , animated , 0 , -1 , mNeedSnapToPage );
	}
	
	//xiatian add end
	//cheyingkun add start	//解决“两个图标重叠生成文件夹，长按此文件夹时PC端将其中一个应用卸载，卸载成功后松开文件夹，松开后桌面停止运行”的问题。【0010786】
	/**
	 * 判断 pc端卸载的应用是否在正在拖拽的文件夹中
	 * @param item 删除的应用信息
	 * @return true 拖拽的是文件夹并且pc端卸载的应用在这个文件夹内
	 */
	private boolean isUnInstallAppInDraggingFolder(
			ShortcutInfo info )
	{
		if( info == null )
		{
			return false;
		}
		if( mDragController.isDragging()//正在拖拽
				&& mDragInfo != null//
				&& mDragInfo.getCell() instanceof FolderIcon//拖拽的是文件夹图标
				&& ( (FolderIcon)mDragInfo.getCell() ).getFolderInfo().getId() == info.getContainer() )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	//cheyingkun add end
	;
	
	public boolean isDragging()
	{
		return this.mDragController.isDragging();
	}
	
	//WangLei add start //bug:0011044  单层模式时将桌面除时间插件之外所有的图标拖动到一个文件夹里，让桌面只有一页。之后进行智能分类，成功后切换页面到第二页。恢复布局，桌面停止运行
	/**智能分类成功后设置当前页，当加载桌面数据时会自动跳转到默认页面*/
	//cheyingkun start	//解决“智能分类前后，页面数相同时，分类后页面没有跳到默认页并且文件夹点击无法打开”的问题。【i_0011212】
	//【问题原因】当分类前后页面数相同时，PagedView.java中的onLayout方法1264行：mChildCountOnLastLayout和 getChildCount()相等，不会走到setCurrentPage。没有自动跳转到默认页面。
	//		但是之前的方法setCurrentPageOnly，设置了mCurrentPage和mNextPage却没有跳过去，就出现该问题。
	//【解决方案】取消之前的根据boolean值判断是否跳转。
	//cheyingkun del start
	//	protected void setCurrentPageOnly(
	//			boolean updateCurrentScroll ,
	//			boolean updatePageIndicator )
	//	{
	//		mCurrentPage = Math.max( 0 , Math.min( mDefaultPage , getPageCount() - 1 ) );
	//		mNextPage = INVALID_PAGE;
	//		/**更新当前页面的scroll，防止页面内容丢失*/
	//		if( updateCurrentScroll )
	//		{
	//			updateCurrentPageScroll();
	//		}
	//		/**更新页面指示器，让对应页面的指示器显示正确的样式*/
	//		if( updatePageIndicator )
	//		{
	//			notifyPageSwitchListener();
	//		}
	//	}
	//cheyingkun del end
	//cheyingkun add start
	public void setCurrentPageNoInvalidate()
	{
		//xiatian start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		int mDefaultPageIndex = mDefaultPage;//xiatian del
		//xiatian add start
		//xiatian start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，某些情况下（默认主页数：智能分类前比智能分类后大1、桌面页面数：智能分类前比智能分类后大1）从智能分类状态回到未分类状态后，默认主页错误”的问题。【i_0014555】
		//【问题原因】“分类成功”和“退出分类成功”这两种情况，调用该方法时，并没有重新加载桌面页面（仍是之前状态的桌面页面），从而会导致getDefaultPageIndex()中获取的桌面页面数出错，默认主页会计算出错。
		//【解决方案】此时，不回去默认页面数，直接设置为第0页（没有酷生活时），或者第一页（有酷生活时）。
		//		int mDefaultPageIndex = getDefaultPageIndex();//xiatian del
		//xiatian add start
		int mDefaultPageIndex = 0;
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//xiatian del start
		//		if( hasFavoritesPage() )
		//		{
		//			mDefaultPageIndex++;
		//		}
		//xiatian del end
		mDefaultPageIndex += getFunctionPagesInNormalPageLeftNum();//xiatian add
		//xiatian end
		//xiatian add end
		//xiatian end
		//xiatian add end
		//xiatian end
		mCurrentPage = Math.max( 0 , Math.min( mDefaultPageIndex , getPageCount() - 1 ) );
		mNextPage = INVALID_PAGE;
		/**更新当前页面的scroll，防止页面内容丢失*/
		updateCurrentPageScroll();
		/**更新页面指示器，让对应页面的指示器显示正确的样式*/
		notifyPageSwitchListener();
	}
	
	//cheyingkun add end
	//cheyingkun end
	//WangLei add end
	// zhujieping@2015/05/28 ADD START，增加mFavoritesPageView，后续若要扩展增加类似新闻页的内容，可在getFavoritesPageView中初始化包名即可
	private View mFavoritesPageRootView = null;
	
	public void changeFavoritesPage()
	{
		CellLayout mFavoritesPageView = getScreenWithId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		if( this.mFavoritesPageRootView != null && this.mFavoritesPageRootView.getId() != FavoritesPageManager.LOADING_VIEW_ID )
		{
			return;
		}
		try
		{
			View mFavoritesPageRootViewNew = FavoritesPageManager.getInstance( this.getContext() ).getView();
			if( mFavoritesPageRootViewNew != null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "lvjiangbin" , "mFavoritesPageView:" + this.mFavoritesPageRootView );
				mFavoritesPageView.removeViewFromCell( this.mFavoritesPageRootView );
				this.mFavoritesPageRootView = mFavoritesPageRootViewNew;
				mFavoritesPageView.addView( this.mFavoritesPageRootView );
				mFavoritesPageView.setIsFunctionPage( true );
				if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
				{
					//cheyingkun add start	//桌面支持按键(酷生活桌面交互按键处理) 
					this.mFavoritesPageRootView.setFocusable( true );
					this.mFavoritesPageRootView.setOnKeyListener( new FunctionPagesKeyEventListener() );
					//cheyingkun add end
				}
				mLauncher.launcherOnPausedAndOnResumeCallBackFavorites();//cheyingkun add	//添加酷生活时回调其onPause和onResume；解决编辑模式下进美化中心没有移除酷生活的导航栏监听窗口的问题
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void initFavoritesPage(
			CellLayout cell )
	{
		//cheyingkun add start	//phenix1.1稳定版移植酷生活
		if( hasFavoritesPage() )
		{
			try
			{
				mFavoritesPageRootView = FavoritesPageManager.getInstance( this.getContext() ).getView();
				if( mFavoritesPageRootView != null )
				{
					//chenliang add start	//fix bug:解決“在编辑模式下或者桌面到编辑模式动画未播放完时，酷生活被添加到celllayout中,导致编辑模式出现酷生活页面”的问题。
					if( isInOverviewMode()/* //1，当前处于编辑模式；2，桌面到编辑模式动画未播放完 */)
					{
						cell.setVisibility( View.INVISIBLE );
					}
					//chenliang add end
					cell.addView( mFavoritesPageRootView );
					cell.setIsFunctionPage( true );
					if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
					{
						//cheyingkun add start	//桌面支持按键(酷生活桌面交互按键处理)
						mFavoritesPageRootView.setFocusable( true );
						mFavoritesPageRootView.setOnKeyListener( new FunctionPagesKeyEventListener() );
						//cheyingkun add end
					}
					mLauncher.launcherOnPausedAndOnResumeCallBackFavorites();//cheyingkun add	//添加酷生活时回调其onPause和onResume；解决编辑模式下进美化中心没有移除酷生活的导航栏监听窗口的问题
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "PROCEDURE loadFavoritesPage:" , ( System.currentTimeMillis() - Launcher.sTime_applicationCreateStart ) ) );
		}
		//cheyingkun add end
	}
	
	public boolean isFavoritesPageByPageIndex(
			int index )
	{
		return getScreenIdForPageIndex( index ) == FUNCTION_FAVORITES_PAGE_SCREEN_ID;
	}
	
	public boolean isFunctionPageByPageIndex(
			int index )
	{
		// YANGTIANYU@2016/06/20 UPD START
		//return getScreenIdForPageIndex( index ) == CUSTOM_CONTENT_SCREEN_ID;
		long screenId = getScreenIdForPageIndex( index );
		return screenId == FUNCTION_FAVORITES_PAGE_SCREEN_ID || screenId == FUNCTION_CAMERA_PAGE_SCREEN_ID || screenId == FUNCTION_MUSIC_PAGE_SCREEN_ID;
		// YANGTIANYU@2016/06/20 UPD END
	}
	
	// zhujieping@2015/05/28 ADD END
	//cheyingkun add strat	//TCardMountUpdateAppBitmapOptimization(更新apps中的图标)
	void updateAppInfosBitmap(
			ArrayList<AppInfo> apps )
	{
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int j = 0 ; j < childCount ; j++ )
			{
				final View view = layout.getChildAt( j );
				if( view instanceof FolderIcon )//如果是文件夹,修改文件夹内部图标
				{
					FolderIcon mFolderIcon = (FolderIcon)view;
					Folder mFolder = mFolderIcon.getFolder();
					CellLayout mCellLayout = mFolder.getContent();
					View mView = mCellLayout.getChildAt( 0 );
					ShortcutAndWidgetContainer mShortcutAndWidgetContainer = (ShortcutAndWidgetContainer)mView;
					int folderChilderCount = mShortcutAndWidgetContainer.getChildCount();
					for( int i = 0 ; i < folderChilderCount ; i++ )
					{
						View view2 = mShortcutAndWidgetContainer.getChildAt( i );
						Object tag2 = view2.getTag();
						if( LauncherModel.isShortcutInfoUpdateable( (ItemInfo)tag2 ) )
						{
							ShortcutInfo info = (ShortcutInfo)tag2;
							updateAppInfosBitmap( apps , info , view2 );
						}
					}
					mFolderIcon.invalidate();
				}
				else
				{
					Object tag = view.getTag();
					if( LauncherModel.isShortcutInfoUpdateable( (ItemInfo)tag ) )
					{
						ShortcutInfo info = (ShortcutInfo)tag;
						updateAppInfosBitmap( apps , info , view );
					}
				}
			}
		}
	}
	
	private void updateAppInfosBitmap(
			ArrayList<AppInfo> apps ,
			ShortcutInfo info ,
			View view )
	{
		final Intent intent = info.getIntent();
		final ComponentName name = intent.getComponent();
		final int appCount = apps.size();
		for( int k = 0 ; k < appCount ; k++ )
		{
			AppInfo app = apps.get( k );
			if( app.getComponentName().equals( name ) )
			{
				BubbleTextView shortcut = (BubbleTextView)view;
				info.updateIcon( mIconCache );
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
				//info.title = app.title.toString();
				if( app.getTitle() == null )
				{
					app.setTitle( "name = null" );
				}
				info.setTitle( app.getTitle().toString() );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "更新图标: " , info.getTitle() ) );//cheyingkun add	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
				shortcut.applyFromShortcutInfo( info , mIconCache );
			}
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//解决：拖拽文件夹时不松手,pc端卸载文件夹内图标,拖动的图标和影子没更新的问题（bug:0009717）
	//cheyingkun del start	//打开usb存储设备,图标灰色,长按灰色图标不松手,拔掉usb线时不再停止拖拽,改为更新dragview
	//updateDragViewFolderIcon整理为updateDragViewAndDragOutline,桌面文件夹和应用都调用updateDragViewAndDragOutline来更新dragView
	//	/**
	//	 * 更新拖拽的文件夹
	//	 * @param mFolderIcon 拖拽的文件夹
	//	 */
	//	public void updateDragViewFolderIcon(
	//			FolderIcon mFolderIcon )
	//	{
	//		if( mFolderIcon != null )
	//		{
	//			if( mDragController != null && mDragController.getDragObject() != null )
	//			{
	//				updateDragView( mFolderIcon );//更新dragView图片
	//				updateDragOutline( mFolderIcon , mDragController.getDragObject() );//刷新影子图片
	//			}
	//		}
	//	}
	//cheyingkun del end
	/**
	 * 根据传入的view更新正在正在拖拽的view
	 * @param dragView
	 */
	private void updateDragView(
			View dragView )
	{
		if( dragView == null )
		{
			return;
		}
		if( mDragController != null )
		{
			DragView mDragObjectDragView = mDragController.getDragView();//拿到正在拖拽的view
			if( mDragObjectDragView != null )
			{
				Bitmap bitmap = createDragBitmap( dragView , new Canvas() , DRAG_BITMAP_PADDING );
				bitmap = Tools.resizeBitmap( bitmap , mDragObjectDragView.getWidth() , mDragObjectDragView.getHeight() );
				mDragObjectDragView.setBitmap( bitmap );
				mDragObjectDragView.invalidate();//刷新跟随手指移动的view
			}
		}
	}
	
	private void setDragOutline(
			Bitmap mDragOutline )
	{
		if( mDragOutline != null )
		{
			if( this.mDragOutline != null && !this.mDragOutline.isRecycled() )
			{
				this.mDragOutline.isRecycled();
			}
			this.mDragOutline = mDragOutline;
		}
	}
	
	/**拖拽期间,根据传入的view,桌面图标刷新后强制刷新拖拽view的影子
	 * onDragOver会调用visualizeDropLocation方法,visualizeDropLocation会匹配是否改变了格子,改变了才刷新底部影子图标;
	 * updateDragOutline调用visualizeDropLocationNoMatterOldDragCell,该方法是拖拽文件夹过程中,文件夹内图标发生变化(1.被卸载2.变灰或者变亮),所以不做格子改变的比较,强制画一次
	 */
	private void updateDragOutline(
			View dragView ,
			DragObject d )
	{
		if( d == null || dragView == null )
		{
			return;
		}
		setDragOutline( createDragOutline( dragView , new Canvas() , DRAG_BITMAP_PADDING ) );//设置拖拽的影子
		ItemInfo item = (ItemInfo)d.dragInfo;
		if( item == null )
		{
			mDragController.cancelDrag();
			return;
		}
		final View child = ( mDragInfo == null ) ? null : mDragInfo.getCell();
		if( mDragTargetLayout != null )
		{
			mDragTargetLayout.visualizeDropLocationNoMatterOldDragCell(
					child ,
					mDragOutline ,
					(int)mDragViewVisualCenter[0] ,
					(int)mDragViewVisualCenter[1] ,
					mTargetCell[0] ,
					mTargetCell[1] ,
					item.getSpanX() ,
					item.getSpanY() ,
					false ,
					d.dragView.getDragVisualizeOffset() ,
					d.dragView.getDragRegion() );
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//打开usb存储设备,图标灰色,长按灰色图标不松手,拔掉usb线时不再停止拖拽,改为更新dragview
	/**
	 * 更新dragView,如果正在拖拽并且拖拽的view在传入的app列表中
	 * @param apps
	 */
	public void updateDragView(
			ArrayList<AppInfo> apps )//如果正在拖拽,并且拖拽的view在apps列表中
	{
		if( mDragController != null )
		{
			DragObject dragObject = mDragController.getDragObject();
			//cheyingkun start	//解决“取消T卡挂载模式，长按文件夹内灰色图标，被长按的图标没有变亮”的问题。【i_0011410】
			//			if( dragObject != null && mDragController.isDragging() && mDragInfo != null && mDragInfo.getCell() != null )//cheyingkun del
			if( dragObject != null && mDragController.isDragging() && mCurrentDragView != null )//cheyingkun add
			//cheyingkun end	
			{
				Object dragInfo = dragObject.dragInfo;//长按时,按下的桌面的view的tag
				if( dragInfo instanceof ShortcutInfo )//如果是应用图标信息
				{
					ShortcutInfo mDragViewShortcutInfo = (ShortcutInfo)dragInfo;
					//拿到拖拽的view的intent
					Intent intent = mDragViewShortcutInfo.getIntent();
					if( intent != null )
					{
						//拿到拖拽的view的component
						ComponentName component = mDragViewShortcutInfo.getIntent().getComponent();
						if( component != null )
						{
							if( mLauncher != null )
							{
								TCardMountManager mTCardMountManager = TCardMountManager.getInstance( mLauncher );
								if( mTCardMountManager != null && mTCardMountManager.isAppInstalledInTCard( apps , component.getPackageName() ) )//如果正在拖拽的view是T卡中的应用
								{
									//cheyingkun start	//解决“取消T卡挂载模式，长按文件夹内灰色图标，被长按的图标没有变亮”的问题。【i_0011410】
									//									View currentDragView = mDragInfo.getCell();//cheyingkun del
									View currentDragView = mCurrentDragView;//长按隐藏或移除的view(桌面上的图标和文件夹中的图标,开始拖拽都会赋值给mDragSourceView)//cheyingkun add
									//cheyingkun end
									if( dragObject.dragSource instanceof Folder )//如果是文件夹中的图标被拖起
									{
										//更新文件夹中被托起的view,因为在文件夹中长按图标,会直接移除,导致该应用不在任何cellLayout中,所以需要单独进行更新
										if( currentDragView instanceof BubbleTextView )
										{
											BubbleTextView mBubbleTextView = (BubbleTextView)currentDragView;
											//cheyingkun start	//解决“取消T卡挂载模式，长按文件夹内灰色图标，被长按的图标没有变亮”的问题。【i_0011410】
											//											mBubbleTextView.applyFromShortcutInfo( (ShortcutInfo)mDragInfo.getCell().getTag() , mIconCache );//cheyingkun del
											mBubbleTextView.applyFromShortcutInfo( (ShortcutInfo)mCurrentDragView.getTag() , mIconCache );//cheyingkun add
											//cheyingkun end
										}
									}
									//则更新该应用的dragView
									updateDragViewAndDragOutline( currentDragView );
								}
							}
						}
					}
				}
				else if( dragInfo instanceof FolderInfo )//如果是文件夹信息
				{
					//cheyingkun start	//解决“取消T卡挂载模式，长按文件夹内灰色图标，被长按的图标没有变亮”的问题。【i_0011410】
					//					FolderIcon mFolderIcon = (FolderIcon)mDragInfo.getCell();//cheyingkun del
					FolderIcon mFolderIcon = (FolderIcon)mCurrentDragView;//cheyingkun add
					//cheyingkun end
					//拿到拖拽的view的intent
					CellLayout mContent = mFolderIcon.getFolder().getContent();//拿到文件夹的CellLayout
					ShortcutAndWidgetContainer mShortcutAndWidgetContainer = (ShortcutAndWidgetContainer)( mContent.getChildAt( 0 ) );
					int childrenCount = mShortcutAndWidgetContainer.getChildCount();
					for( int k = 0 ; k < childrenCount ; k++ )//循环文件夹中的所有应用
					{
						BubbleTextView mBubbleTextView = (BubbleTextView)mShortcutAndWidgetContainer.getChildAt( k );
						Intent intent = ( (ShortcutInfo)( mBubbleTextView.getTag() ) ).getIntent();
						if( intent != null )
						{
							ComponentName component = intent.getComponent();
							if( component != null )
							{
								if( mLauncher != null )
								{
									TCardMountManager mTCardMountManager = TCardMountManager.getInstance( mLauncher );
									if( mTCardMountManager != null && mTCardMountManager.isAppInstalledInTCard( apps , component.getPackageName() ) )//如果正在拖拽的view是T卡中的应用
									{
										updateDragViewAndDragOutline( mFolderIcon );
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 根据传入的view更新拖拽的view和影子
	 * 桌面文件夹和应用都调用updateDragViewAndDragOutline来更新dragView
	 * @param view
	 */
	public void updateDragViewAndDragOutline(
			View view )
	{
		if( view != null )
		{
			if( mDragController != null && mDragController.getDragObject() != null )
			{
				updateDragView( view );//更新dragView图片
				updateDragOutline( view , mDragController.getDragObject() );//刷新影子图片
			}
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//卸载应用后，其后面的图标是否自动前移。true为自动前移；false为不移动。默认为false。
	/**
	 * 删除item移动图标
	 * @param children	当前页所有item的map
	 * @param removeInfo	要删除view的itemInfo
	 * @param layoutParent 要删除item所在的CellLayout
	 */
	private void startAnimAfterUninstall(
			Map<ItemInfo , View> children ,
			ItemInfo removeInfo ,
			CellLayout layoutParent )
	{
		if( children == null || removeInfo == null || layoutParent == null )
		{
			return;
		}
		//删除应用的位置信息
		final int removeCellX = removeInfo.getCellX();
		final int removeCellY = removeInfo.getCellY();
		final long container = removeInfo.getContainer();
		final long screenId = removeInfo.getScreenId();
		//动画时间
		final int duration = LauncherDefaultConfig.getInt( R.integer.config_sort_after_uninstall_anim_duration );
		//循环当前页的item
		Set<ItemInfo> keySet = children.keySet();
		for( ItemInfo itemInfo : keySet )
		{
			//widget 不移动
			if( itemInfo instanceof LauncherAppWidgetInfo )
			{
				continue;
			}
			//当前item的位置信息
			final int spanX = itemInfo.getSpanX();
			final int spanY = itemInfo.getSpanY();
			final int oldCellX = itemInfo.getCellX();
			final int oldCellY = itemInfo.getCellY();
			int newCellX = itemInfo.getCellX();
			int newCellY = itemInfo.getCellY();
			//如果当前item在删除的item前面,跳出
			if( oldCellY < removeCellY )
			{
				continue;
			}
			else if( oldCellY == removeCellY && oldCellX <= removeCellX )
			{
				continue;
			}
			//计算删除item图标后面图标的位置信息
			final int[] nearestArea = layoutParent.findNearestAreaAfterRemoveItem( removeCellX , removeCellY , spanX , spanY );
			if( nearestArea != null )
			{
				newCellX = nearestArea[0];
				newCellY = nearestArea[1];
				if( newCellX < 0 || newCellY < 0 )
				{
					continue;
				}
				//新的空位在之前位置的后面,不移动位置
				if( newCellY > oldCellY )
				{
					continue;
				}
				if( newCellY == oldCellY && newCellX >= oldCellX )
				{
					continue;
				}
				//动画
				final View view = children.get( itemInfo );
				CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams)view.getLayoutParams();
				layoutParams.useTmpCoords = false;
				view.setLayoutParams( layoutParams );
				layoutParent.animateChildToPosition( view , newCellX , newCellY , duration , 0 , true , true );
				//更新数据库位置信息
				LauncherModel.modifyItemInDatabase( mLauncher , itemInfo , container , screenId , newCellX , newCellY , spanX , spanY );
			}
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//飞利浦卸载应用自动排序（逻辑完善）
	public void clearRemoveList()
	{
		if( removeList != null )
		{
			removeList.clear();
		}
	}
	
	public boolean isEnableAnimAfterUninstall()
	{
		return mEnableSortAfterUninstall;
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
	private void initDefaultPage()
	{
		//xiatian start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，默认主页错误”的问题。
		//xiatian del start
		//		int mDefaultPage = 0;//xiatian add	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE )
		//		{
		//			mDefaultPage = getDefaultPageDefaultConfig();
		//		}
		//		else if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
		//		{
		//			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getContext() );
		//			String mDefaultPageKey = getDefaultPageKey();
		//			mDefaultPage = mSharedPreferences.getInt( mDefaultPageKey , -1 );
		//			if( mDefaultPage == -1 )
		//			{
		//				mDefaultPage = getDefaultPageDefaultConfig();
		//				mSharedPreferences.edit().putInt( mDefaultPageKey , mDefaultPage ).commit();
		//			}
		//		}
		//		mOriginalDefaultPage = mDefaultPage;
		//xiatian del end
		//xiatian add start
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getContext() );
		String mDefaultPageKey = getDefaultPageKey();
		initDefaultPage( mSharedPreferences , mDefaultPageKey );
		//xiatian add end
		//xiatian end
	}
	
	private void initDefaultPage(
			SharedPreferences mSharedPreferences ,
			String mDefaultPageKey )
	{
		int mDefaultPage = mSharedPreferences.getInt( mDefaultPageKey , -1 );
		if( mDefaultPage == -1 )
		{
			mDefaultPage = getDefaultPageDefaultConfig();
			mSharedPreferences.edit().putInt( mDefaultPageKey , mDefaultPage ).commit();
		}
	}
	
	public int getDefaultPageFromSharedPreferences()
	{
		int ret = 0;
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getContext() );
		String mDefaultPageKey = getDefaultPageKey();
		ret = mSharedPreferences.getInt( mDefaultPageKey , -1 );
		if( ret == -1 )
		{
			initDefaultPage( mSharedPreferences , mDefaultPageKey );
			ret = mSharedPreferences.getInt( mDefaultPageKey , -1 );
		}
		return ret;
	}
	
	private int getDefaultPageDefaultConfig()
	{
		int ret = 0;
		int mConfigId = -1;
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{//单层
			if( LauncherAppState.isAlreadyCategory( getContext() ) )
			{//智能分类
				mConfigId = R.integer.config_workspace_default_page_core_in_category;
			}
			else
			{
				mConfigId = R.integer.config_workspace_default_page_core;
			}
		}
		else
		{
			mConfigId = R.integer.config_workspace_default_page_draw;
		}
		ret = LauncherDefaultConfig.getInt( mConfigId );
		return ret;
	}
	
	private String getDefaultPageKey()
	{
		String key = null;
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{//单层
			if( LauncherAppState.isAlreadyCategory( getContext() ) )
			{//智能分类
				//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，默认主页错误”的问题。
				if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE )
				{
					key = DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE_KEY_CORE_IN_CATEGORY;
				}
				else if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
				//xiatian add end
				{
					key = DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_KEY_CORE_IN_CATEGORY;
				}
			}
			else
			{
				//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，默认主页错误”的问题。
				if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE )
				{
					key = DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE_KEY_CORE;
				}
				else if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
				//xiatian add end
				{
					key = DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_KEY_CORE;
				}
			}
		}
		else
		{
			//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，默认主页错误”的问题。
			if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE )
			{
				key = DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE_KEY_DRAWER;
			}
			else if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
			//xiatian add end
			{
				key = DAFAULT_PAGE_BIND_WITH_CELLLAYOUT_KEY_DRAWER;
			}
		}
		return key;
	}
	
	public void setDefaultPage(
			int mDefaultPage )
	{
		//		this.mDefaultPage = mDefaultPage;//xiatian del	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )//xiatian del	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，默认主页错误”的问题。
		{
			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getContext() );
			mSharedPreferences.edit().putInt( getDefaultPageKey() , mDefaultPage ).commit();
		}
	}
	
	public void bindDefaultPage()
	{
		int mDefaultPageIndex = getDefaultPageIndex();
		if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
		{
			int mCount = getChildCount();
			for( int i = 0 ; i < mCount ; i++ )
			{
				( (CellLayout)getChildAt( i ) ).setDefaultPage( false );
			}
			CellLayout mCellLayout = (CellLayout)getChildAt( mDefaultPageIndex );
			mCellLayout.setDefaultPage( true );
		}
		setCurrentPage( mDefaultPageIndex );//xiatian add	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		updateHomePageIndicator();//xiatian add	//fix bug：解决“特定页面（酷生活、主页、音乐页和相机页）的页面指示器显示特定图标时，页面指示器显示错误（重复以及错位）”的问题。
	}
	
	public int getDefaultPageIndex()
	{
		//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“以下两种情况（（1）酷生活不在普通页左边；（2）除了酷生活之外，还有功能页在普通页左边）导致主页计算错误”的问题。）
		boolean mIsHaveFavoritesPage = hasFavoritesPage();
		boolean mIsFavoritesPageInNormalPageLeft = ( LauncherDefaultConfig.getFavoritesPagePosition() < 0 );
		boolean mIsHaveCameraPage = hasCameraPage();
		boolean mIsCameraPageInNormalPageLeft = ( LauncherDefaultConfig.getCameraPagePosition() < 0 );
		boolean mIsHaveMusicPage = hasMusicPage();
		boolean mIsMusicPageInNormalPageLeft = ( LauncherDefaultConfig.getMusicPagePosition() < 0 );
		//xiatian add end
		//xiatian start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE”后，默认主页错误”的问题。
		//xiatian del start
		//		int ret = 0;
		//		//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE )
		//		{
		//			ret = getDefaultPageDefaultConfig();
		//		}
		//		else if( LauncherDefaultConfig.CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE == BaseDefaultConfig.DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT )
		//		//xiatian add end
		//		{
		//			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getContext() );
		//			String mDefaultPageKey = getDefaultPageKey();
		//			ret = mSharedPreferences.getInt( mDefaultPageKey , -1 );
		//		}
		//xiatian del end
		int ret = getDefaultPageFromSharedPreferences();//xiatian add
		//xiatian end
		//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		if( ret < 0 )
		{
			ret = 0;
		}
		else if( ret > 0 )
		//xiatian add end
		{
			int mChildCount = getChildCount();
			int mLastNormalCellLayoutIndex = mChildCount - 1;
			//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
			if( mIsHaveFavoritesPage )
			{
				mLastNormalCellLayoutIndex--;
			}
			if( mIsHaveCameraPage )
			{
				mLastNormalCellLayoutIndex--;
			}
			if( mIsHaveMusicPage )
			{
				mLastNormalCellLayoutIndex--;
			}
			//xiatian add end
			if( ret > mLastNormalCellLayoutIndex )
			{
				ret = mLastNormalCellLayoutIndex;
				setDefaultPage( ret );
			}
			if( ret <= mLastNormalCellLayoutIndex )
			{
				//xiatian start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
				//xiatian del start
				//		if( mDefaultPage != ret )
				//		{
				//			setDefaultPage( ret );
				//		}
				//xiatian del end
				//xiatian add start
				//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“以下两种情况（（1）酷生活不在普通页左边；（2）除了酷生活之外，还有功能页在普通页左边）导致主页计算错误”的问题。）
				//xiatian del start
				//				if( hasFavoritesPage() )
				//				{
				//					ret++;
				//				}
				//xiatian del end
				//xiatian add start
				if( mIsHaveFavoritesPage && mIsFavoritesPageInNormalPageLeft )
				{
					ret++;
				}
				if( mIsHaveCameraPage && mIsCameraPageInNormalPageLeft )
				{
					ret++;
				}
				if( mIsHaveMusicPage && mIsMusicPageInNormalPageLeft )
				{
					ret++;
				}
				//xiatian add end
				//xiatian end
				//xiatian add end
				//xiatian end
			}
		}
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“以下两种情况（（1）酷生活不在普通页左边；（2）除了酷生活之外，还有功能页在普通页左边）导致主页计算错误”的问题。）
		//xiatian del start
		//		//xiatian add start	//fix bug：解决“设置默认主页类型为“DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT”后，默认主页错误”的问题。【i_0004461】
		//		if( hasFavoritesPage() && ret == 0 )
		//		{
		//			ret++;
		//		}
		//		//xiatian add end
		//xiatian del end
		//xiatian add start
		int mFunctionPagesInNormalPageLeftNum = 0;
		if( mIsHaveFavoritesPage && mIsFavoritesPageInNormalPageLeft )
		{
			mFunctionPagesInNormalPageLeftNum++;
		}
		if( mIsHaveCameraPage && mIsCameraPageInNormalPageLeft )
		{
			mFunctionPagesInNormalPageLeftNum++;
		}
		if( mIsHaveMusicPage && mIsMusicPageInNormalPageLeft )
		{
			mFunctionPagesInNormalPageLeftNum++;
		}
		if(
		//
		mFunctionPagesInNormalPageLeftNum > 0
		//
		&& ret < mFunctionPagesInNormalPageLeftNum
		//
		)
		{
			ret = mFunctionPagesInNormalPageLeftNum;
		}
		//xiatian add end
		//xiatian end
		return ret;
	}
	//xiatian add end
	;
	
	public ArrayList<View> getAllShortcutInworkspace()
	{
		ArrayList<View> list = new ArrayList<View>();
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				final View mChildView = layout.getChildAt( i );
				if( mChildView instanceof BubbleTextView || mChildView instanceof FolderIcon )
				{
					list.add( mChildView );
				}
			}
		}
		return list;
	}
	
	/**
	 * 判断item中的位置是否已经被占据，如果被占据，则再去查找一个新的位置使用
	 * @param item
	 */
	public void checkItemIsOccupied(
			ItemInfo item )
	{
		if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP )
		{
			CellLayout cl = getScreenWithId( item.getScreenId() );
			if( cl != null && cl.isOccupied( item.getCellX() , item.getCellY() ) )
			{
				Pair<Long , int[]> coords = findEntryScreenId( item.getSpanX() , item.getSpanY() );
				if( coords == null )
				{
					if( addExtraEmptyScreen() )
					{
						commitExtraEmptyScreen();
						long screenId = getIdForScreen( (CellLayout)( getChildAt( getChildCount() - 1 ) ) );
						coords = new Pair<Long , int[]>( screenId , new int[]{ 0 , 0 } );
					}
				}
				item.setScreenId( coords.first );//将重新查找到的位置赋值
				item.setCellX( coords.second[0] );
				item.setCellY( coords.second[1] );
			}
		}
	}
	
	/**
	 * 查找适合widget存放的空页
	 * @return coords 将找到页的screenID存放在coords的first，cell位置存放在coords的second
	 */
	public Pair<Long , int[]> findEntryScreenId(
			int mSpanX ,
			int mSpanY )
	{
		Pair<Long , int[]> coords = null;
		for( int i = getCurrentPage() ; i < getChildCount() ; i++ )
		{
			View view = getChildAt( i );
			if( view instanceof CellLayout )
			{
				CellLayout layout = (CellLayout)view;
				int[] target = new int[2];
				boolean isfindTargetCell = layout.findCellForSpan( target , mSpanX , mSpanY );
				if( isfindTargetCell )
				{
					coords = new Pair<Long , int[]>( getIdForScreen( layout ) , target );
					snapToPage( i );//找到空位后就跳转到该页
					return coords;
				}
			}
		}
		return coords;
	}
	
	// zhangjin@2015/08/31 ADD START
	public void updateIconHouse(
			ComponentName componentName )
	{
		if( componentName == null )
		{
			return;
		}
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
		for( ShortcutAndWidgetContainer layout : childrenLayouts )
		{
			int childCount = layout.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				final View mChildView = layout.getChildAt( i );
				Object tag = mChildView.getTag();
				if( tag instanceof ShortcutInfo )
				{
					ShortcutInfo mShortcutInfo = (ShortcutInfo)tag;
					final Intent intent = mShortcutInfo.getIntent();
					final ComponentName name = intent.getComponent();
					if( mShortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
					{
						if( componentName.equals( name ) )
						{
							if( mChildView instanceof BubbleTextView )
							{
								BubbleTextView mBubbleTextView = (BubbleTextView)mChildView;
								mShortcutInfo.updateIcon( mIconCache );
								mBubbleTextView.updateIcon( mShortcutInfo , mIconCache );
								//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
								mShortcutInfo.setIconBitmapBackup( Bitmap.createBitmap( mShortcutInfo.getIcon() ) );
								IconHouseManager.getInstance().updateFavoritesIconHouseApps( componentName , mShortcutInfo.getIconBitmapBackup() );
								//cheyingkun add end
							}
						}
					}
				}
				if( tag instanceof FolderInfo )
				{
					FolderIcon folderIcon = (FolderIcon)mChildView;
					ArrayList<View> mFolderChildViewList = folderIcon.getFolder().getItemsInReadingOrder();
					for( View mFolderChild : mFolderChildViewList )
					{
						ShortcutInfo mShortcutInfo = (ShortcutInfo)mFolderChild.getTag();
						final Intent intent = mShortcutInfo.getIntent();
						final ComponentName name = intent.getComponent();
						if( mShortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
						{
							if( componentName.equals( name ) )
							{
								if( mFolderChild instanceof BubbleTextView )
								{
									BubbleTextView mBubbleTextView = (BubbleTextView)mFolderChild;
									mShortcutInfo.updateIcon( mIconCache );
									mBubbleTextView.updateIcon( mShortcutInfo , mIconCache );
									folderIcon.invalidate();
									//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
									mShortcutInfo.setIconBitmapBackup( Bitmap.createBitmap( mShortcutInfo.getIcon() ) );
									IconHouseManager.getInstance().updateFavoritesIconHouseApps( componentName , mShortcutInfo.getIconBitmapBackup() );
									//cheyingkun add end
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void updateIconHouseDragView(
			ComponentName componentName )
	{
		if( mDragController == null )
		{
			return;
		}
		DragObject dragObject = mDragController.getDragObject();
		if( dragObject == null || mDragController.isDragging() == false || mCurrentDragView == null )
		{
			return;
		}
		Object dragInfo = dragObject.dragInfo;//长按时,按下的桌面的view的tag
		if( dragInfo instanceof ShortcutInfo && mCurrentDragView instanceof BubbleTextView )//如果是应用图标信息
		{
			ShortcutInfo mDragViewShortcutInfo = (ShortcutInfo)dragInfo;
			if( mDragViewShortcutInfo.getIntent().getComponent().equals( componentName ) )
			{
				BubbleTextView mBubbleTextView = (BubbleTextView)mCurrentDragView;
				mDragViewShortcutInfo.updateIcon( mIconCache );
				mBubbleTextView.updateIcon( mDragViewShortcutInfo , mIconCache );
			}
		}
	}
	
	// zhangjin@2015/08/31 ADD END
	// zhangjin@2015/09/01 ADD START
	ArrayList<ShortcutAndWidgetContainer> getVisibleShortcutAndWidgetContainers()
	{
		if( getChildCount() == 0 )
		{
			return null;
		}
		ArrayList<ShortcutAndWidgetContainer> childrenLayouts = new ArrayList<ShortcutAndWidgetContainer>();
		int screen = this.getCurrentScreen();
		childrenLayouts.add( ( (CellLayout)getChildAt( screen ) ).getShortcutsAndWidgets() );
		if( mLauncher.getHotseat() != null )
		{
			childrenLayouts.add( mLauncher.getHotseat().getLayout().getShortcutsAndWidgets() );
		}
		return childrenLayouts;
	}
	
	public boolean isCmpVisible(
			final ComponentName componentName )
	{
		boolean canUpdate = false;
		if( componentName == null )
		{
			return canUpdate;
		}
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
				final View mChildView = layout.getChildAt( i );
				Object tag = mChildView.getTag();
				if( tag instanceof ShortcutInfo )
				{
					ShortcutInfo mShortcutInfo = (ShortcutInfo)tag;
					final Intent intent = mShortcutInfo.getIntent();
					final ComponentName name = intent.getComponent();
					if( mShortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
					{
						if( componentName.equals( name ) )
						{
							canUpdate = true;
							return canUpdate;
						}
					}
				}
				// @2015/09/11 DEL START 在文件夹中不刷新
				//if( tag instanceof FolderInfo )
				//{
				//	FolderIcon folderIcon = (FolderIcon)mChildView;
				//	ArrayList<View> mFolderChildViewList = folderIcon.getFolder().getItemsInReadingOrder();
				//	for( View mFolderChild : mFolderChildViewList )
				//	{
				//		ShortcutInfo mShortcutInfo = (ShortcutInfo)mFolderChild.getTag();
				//		final Intent intent = mShortcutInfo.getIntent();
				//		final ComponentName name = intent.getComponent();
				//		if( mShortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
				//		{
				//			if( componentName.equals( name ) )
				//			{
				//				canUpdate = true;
				//				return canUpdate;
				//			}
				//		}
				//	}
				//}
				// @2015/09/11 DEL END
			}
		}
		return false;
	}
	// zhangjin@2015/09/01 ADD END
	;
	
	//xiatian add start	//限制桌面最大页数
	public boolean canInsertNewScreen()
	{
		boolean ret = true;
		if( getNormalPageCount() >= mScreenNumMax )//zhujieping //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）。
		{
			ret = false;
		}
		return ret;
	}
	
	//xiatian add end
	//cheyingkun add start	//phenix1.1稳定版移植酷生活
	/**
	 * -1屏是否显示桌面搜索框
	 * 整理搜索逻辑: 
	 * 	1、桌面显示搜索，-1屏显示搜索，则-1屏显示桌面的搜索；
	 *  2、桌面显示搜索，-1屏不显示搜索，则-1屏不显示搜索；
	 *  3、桌面不显示搜索，-1屏显示搜索，则-1屏显示自己的搜索。
	 * @return 
	 * 
	 */
	private boolean isFavoritesPageShowLauncherSearch()
	{
		if(
		//
		( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE/*//桌面显示搜索*/)
		//
		&& ( hasFavoritesPage()/*//cheyingkun add	//解决“服务器关闭酷生活后，酷生活消失前(锁屏解锁前)，酷生活界面搜索栏显示异常”的问题。【i_0014007】*/)
		//
		&& LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE
		//
		)
		{
			return true;
		}
		return false;
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//如果正在切页，不进入主菜单。【i_0013307】
	public boolean isScrollPage()
	{
		if( mTouchState != TOUCH_STATE_REST || !ismStartEffectEnd() )
		{
			return true;
		}
		if( getNextPage() != getCurrentPage() )
		{
			return true;
		}
		return false;
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//酷生活引导页
	@Override
	public ObjectAnimator removeFavoritesClingsAnimation(
			int duration )
	{
		SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
		DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
		int widthPx = deviceProfile.getWidthPx();
		mSearchDropTargetBar.setTranslationX( -widthPx );
		PropertyValuesHolder searchBarX = PropertyValuesHolder.ofFloat( "x" , 0 );
		ObjectAnimator searchBarAnim = new ObjectAnimator();
		searchBarAnim.setTarget( mSearchDropTargetBar );
		searchBarAnim.setValues( searchBarX );
		searchBarAnim.setDuration( duration );
		return searchBarAnim;
	}
	
	//cheyingkun add end
	/**
	 * 根据专属页ID创建专属页
	 * @param mMediaPageScreenId 专属页ID
	 * @return 
	 * @author yangtianyu 2016-7-29
	 */
	private CellLayout createAndAddMediaPageByScreenId(
			long mMediaPageScreenId )
	{
		int mMediaPageIndexInViewGroup = -1;
		DeviceProfile grid = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
		CellLayout mMediaPageView = (CellLayout)mLauncher.getLayoutInflater().inflate( R.layout.workspace_screen , null );
		mMediaPageView.setOnLongClickListener( mLongClickListener );
		mMediaPageView.setOnClickListener( mLauncher );
		mMediaPageView.setSoundEffectsEnabled( false );
		mWorkspaceScreens.put( mMediaPageScreenId , mMediaPageView );
		mMediaPageIndexInViewGroup = getToAddFunctionPageIndexInViewGroupBeforeAddToScreenOrderAndViewGroup( mMediaPageScreenId );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		mScreenOrder.add( ( mMediaPageIndexInViewGroup == -1 ? mScreenOrder.size() : mMediaPageIndexInViewGroup ) , mMediaPageScreenId );
		addNoSearchPage( mMediaPageView , grid.getSearchBarSpaceHeightPx() , mMediaPageIndexInViewGroup );
		//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		boolean mIsMediaPageInNormalPageLeft = false;
		if(
		//
		( mMediaPageScreenId == FUNCTION_CAMERA_PAGE_SCREEN_ID && LauncherDefaultConfig.getCameraPagePosition() < 0 )
		//
		|| ( mMediaPageScreenId == FUNCTION_MUSIC_PAGE_SCREEN_ID && LauncherDefaultConfig.getMusicPagePosition() < 0 )
		//
		)
		{
			mIsMediaPageInNormalPageLeft = true;
		}
		if( mIsMediaPageInNormalPageLeft )
		{
			if( mRestorePage != INVALID_RESTORE_PAGE )
			{
				if( mMediaPageIndexInViewGroup <= mRestorePage )
				{
					mRestorePage = mRestorePage + 1;
				}
			}
			else
			{
				int mCurrentPage = getCurrentPage();
				if( mMediaPageIndexInViewGroup <= mCurrentPage )
				{
					setCurrentPage( mCurrentPage + 1 );
				}
			}
		}
		else
		//xiatian add end
		{
			if( mRestorePage != INVALID_RESTORE_PAGE )
			{
				int restore = Math.max( 0 , Math.min( mRestorePage , getPageCount() - 1 ) );//防止启动时主页为专属页
				if( getScreenIdForPageIndex( restore ) == mMediaPageScreenId )
				{
					mRestorePage = restore - 1;
				}
			}
		}
		mMediaPageView.setIsFunctionPage( true );
		return mMediaPageView;
	}
	
	/**
	 * 创建相机专属页
	 * @author yangtianyu 2016-7-29
	 */
	public void createAndAddCameraPage()
	{
		final CellLayout mCameraPageView = createAndAddMediaPageByScreenId( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				CameraView.getInstance().initConfig( mLauncher );//zhujieping,这个里面有读取文件的操作，放到线程中
				post( new Runnable() {
					
					public void run()
					{
						View mCameraPageRootView = CameraView.getInstance().getCameraPageView( mLauncher );
						mCameraPageView.addView( mCameraPageRootView );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.v( TAG , StringUtils.concat( "PROCEDURE loadCamera:" , ( System.currentTimeMillis() - Launcher.sTime_applicationCreateStart ) ) );
						}
						if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
						{
							//cheyingkun add start	//相机页桌面切页支持按键
							mCameraPageRootView.setFocusable( true );
							mCameraPageRootView.setOnKeyListener( new FunctionPagesKeyEventListener() );
							//cheyingkun add end
						}
					}
				} );
			}
		} ).start();
	}
	
	/**
	 * 创建音乐专属页
	 * @author yangtianyu 2016-7-29
	 */
	public void createAndAddMusicPage()
	{
		final CellLayout mMusicPageView = createAndAddMediaPageByScreenId( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				MusicView.getInstance().initConfig( mLauncher );
				post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						View mMusicPageRootView = MusicView.getInstance().getMusicPageView( mLauncher );
						mMusicPageView.addView( mMusicPageRootView );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.v( TAG , StringUtils.concat( "PROCEDURE loadMusic:" , ( System.currentTimeMillis() - Launcher.sTime_applicationCreateStart ) ) );
						}
						if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
						{
							//cheyingkun add start	//音乐页桌面切页支持按键
							mMusicPageRootView.setFocusable( true );
							mMusicPageRootView.setOnKeyListener( new FunctionPagesKeyEventListener() );
							//cheyingkun add end
						}
					}
				} );
			}
		} ).start();
	}
	
	/**
	 * 判断index位置的页面是否为相机页
	 * @param index 需要判断的页面的位置
	 * @return 是相机页为true,否则为false
	 * @author yangtianyu 2016-6-17
	 */
	public boolean isCameraPage(
			int index )
	{
		return getScreenIdForPageIndex( index ) == FUNCTION_CAMERA_PAGE_SCREEN_ID;
	}
	
	/**
	 * 判断index位置的页面是否为音乐页
	 * @param index 需要判断的页面的位置
	 * @return 是音乐页为true,否则为false
	 * @author yangtianyu 2016-6-29
	 */
	private boolean isMusicPage(
			int index )
	{
		return getScreenIdForPageIndex( index ) == FUNCTION_MUSIC_PAGE_SCREEN_ID;
	}
	
	// TODO 这个方法有待商榷
	/**
	 * 是否已经存在相机页
	 * @return 存在为true,不存在为false
	 * @author yangtianyu 2016-6-17
	 */
	public boolean hasCameraPage()
	{
		return( mScreenOrder.size() > 0 && mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID ) != -1 );
	}
	
	/**
	 * 是否已经存在音乐页
	 * @return 存在为true,不存在为false
	 * @author yangtianyu 2016-6-20
	 */
	public boolean hasMusicPage()
	{
		return( mScreenOrder.size() > 0 && mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID ) != -1 );
	}
	//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	//	/**
	//	 * 获取专属页的数量
	//	 * @return 专属页的数量
	//	 * @author yangtianyu 2016-6-20
	//	 */
	//	private int getMediaPagesNum()
	//	{
	//		int num = 0;
	//		num += hasCameraPage() ? 1 : 0;
	//		num += hasMusicPage() ? 1 : 0;
	//		return num;
	//	}
	//	
	//	// TODO 当前只考虑专属页在最右侧且不打开循环切页,专属页与-1屏之间默认存在普通页
	//	/**
	//	 * 页面滑动到第一个专属页时,需要隐藏桌面的搜索框（如果桌面显示了搜索框）
	//	 * @author yangtianyu 2016-6-21
	//	 */
	//	private void updateStateForMediaPage()
	//	{
	//		float translationX = 0;
	//		float progress = 0;
	//		if( hasMusicPage() || hasCameraPage() )
	//		{
	//			// 一定有一个专属页存在
	//			// 只考虑专属页在右侧且不打开循环切页
	//			int mCameraPageIndex = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );
	//			int mMusicPageIndex = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );
	//			mCameraPageIndex = mCameraPageIndex < 0 ? getChildCount() - 1 : mCameraPageIndex;
	//			mMusicPageIndex = mMusicPageIndex < 0 ? getChildCount() - 1 : mMusicPageIndex;
	//			int index = Math.min( mCameraPageIndex , mMusicPageIndex );
	//			int scrollDelta = getScrollX() - getScrollForPage( index ) - getLayoutTransitionOffsetForPage( index );
	//			float scrollRange = getScrollForPage( index ) - getScrollForPage( index - 1 );
	//			if( isLoop() && ( mOverScrollX < 0 || mOverScrollX > mMaxScrollX ) )
	//			{
	//				if( isLayoutRtl() )
	//				{
	//					scrollRange = Math.abs( scrollRange );
	//					translationX = -Math.abs( mOverScrollX + scrollRange ) % scrollRange;
	//				}
	//				else
	//				{
	//					translationX = scrollRange - Math.abs( mOverScrollX + scrollRange ) % scrollRange;
	//				}
	//				progress = Math.abs( ( translationX ) / scrollRange );
	//			}
	//			else
	//			{
	//				translationX = -scrollRange - scrollDelta;
	//				// 页面回弹
	//				if( mOverScrollX > mMaxScrollX )
	//				{
	//					progress = -mMediaPageScrollProgressLast;
	//				}
	//				else
	//				// 正常切页
	//				{
	//					progress = ( -scrollRange - scrollDelta ) / scrollRange;
	//				}
	//				// layout中scroll的计算方式,从左到右或从右到左,不用在意
	//				if( isLayoutRtl() )
	//				{
	//					translationX = Math.max( 0 , translationX );
	//				}
	//				else
	//				{
	//					translationX = Math.min( 0 , translationX );
	//				}
	//				progress = Math.abs( Math.min( 0 , progress ) );
	//			}
	//			if( Float.compare( progress , mMediaPageScrollProgressLast ) == 0 )
	//				return;
	//			mMediaPageScrollProgressLast = progress;
	//			if( isLoop() && ( mOverScrollX <= 0 || mOverScrollX >= mMaxScrollX ) )
	//			{
	//				if( hasFavoritesPage() )//专属页直接切换到-1屏
	//				{
	//					if( isFavoritesPageShowSearch() )
	//					{
	//						mLauncher.getSearchDropTargetBar().setTranslationX( translationX );
	//					}
	//				}
	//			}
	//			else if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
	//			{
	//				mLauncher.getSearchDropTargetBar().setTranslationX( translationX );
	//			}
	//		}
	//	}
	//xiatian del end
	;
	
	/**
	 * 页面开始滑动时,如果有必要,则通知对应的专属页处理页面开始滑动的事件
	 * @author yangtianyu 2016-6-29
	 */
	private void onMediaPagesBeginMoving()
	{
		if( isCameraPage( mCurrentPage ) )
		{
			CameraView.getInstance().onPageBeginMoving();
		}
		else if( isMusicPage( mCurrentPage ) )
		{
			MusicView.getInstance().onPageBeginMoving();
		}
	}
	
	/**
	 * 页面滑动结束时,如果有必要,则通知对应的专属页处理页面滑动结束的事件
	 * @author yangtianyu 2016-6-29
	 */
	private void onMediaPagesEndMoving()
	{
		// 先处理从本页面滑动到其他页面的情况
		if( isCameraPage( mLastPage ) )
		{
			CameraView.getInstance().onPageMoveOut();
		}
		else if( isMusicPage( mLastPage ) )
		{
			MusicView.getInstance().onPageMoveOut();
		}
		// 后处理从其他页面滑动到本页面的情况
		if( isCameraPage( mCurrentPage ) )
		{
			CameraView.getInstance().onPageMoveIn();
		}
		else if( isMusicPage( mCurrentPage ) )
		{
			MusicView.getInstance().onPageMoveIn();
		}
	}
	
	/**
	 * 在专属页长按底边栏图标抬起后,需要将搜索条所在的区域位置重置,使其正常显示,以便于显示删除框等内容
	 * @author yangtianyu 2016-7-29
	 */
	private void resetSearchPos()
	{
		if( isCameraPage( getCurrentPage() ) || isMusicPage( getCurrentPage() ) )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
			{
				mLauncher.getSearchDropTargetBar().setTranslationX( 0 );
			}
		}
	}
	
	/**
	 * 将专属页从屏幕上移除（1、从mScreenOrder移除；2、从Workspace的Children中移除；3、在mWorkspaceScreens中保留）
	 * @author yangtianyu 2016-7-22
	 */
	private void removeFunctionPagesFromScreenOnlyWhenDragStart()
	{
		//酷生活
		//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		boolean mIsFavoritesPageInNormalPageRight = ( LauncherDefaultConfig.getFavoritesPagePosition() > 0 );
		if( mIsFavoritesPageInNormalPageRight )
		{
			int mFavoritesPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			if( mFavoritesPageIndexInViewGroup > 0 )
			{
				mScreenOrder.remove( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
				removeView( getScreenWithId( FUNCTION_FAVORITES_PAGE_SCREEN_ID ) );
				if( getCurrentPage() > mFavoritesPageIndexInViewGroup )
					setCurrentPage( getCurrentPage() - 1 );
			}
		}
		//xiatian add end
		//相机页
		//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		boolean mIsCameraPageInNormalPageRight = ( LauncherDefaultConfig.getCameraPagePosition() > 0 );
		if( mIsCameraPageInNormalPageRight )
		//xiatian add end
		{
			// YANGTIANYU@2016/08/19 UPD START
			// i_0014335 
			// 只有当workspace的children中包含该专属页时,才进行删除操作,避免误删
			//long mCameraPageIndex = 0;
			//if( mCameraPageView != null )
			//{
			//	mCameraPageIndex = getPageIndexForScreenId( FUNCTION_CAMERA_PAGE_SCREEN_ID );
			// YANGTIANYU@2016/08/19 UPD END
			long mCameraPageIndexInViewGroup = getPageIndexForScreenId( FUNCTION_CAMERA_PAGE_SCREEN_ID );
			if( mCameraPageIndexInViewGroup > 0 )
			{
				// YANGTIANYU@2016/09/21 ADD START
				// 【i_0014515】从桌面临时移除专属页时,需关闭相机,终止动画,调用动画的end方法。
				// 避免动画执行时移除专属页,导致动画异常停止,动画结束时的部分内容未执行到。
				CameraView.getInstance().onRemoveFromScreen();
				// YANGTIANYU@2016/09/21 ADD END
				// YANGTIANYU@2016/08/19 UPD END
				mScreenOrder.remove( FUNCTION_CAMERA_PAGE_SCREEN_ID );
				removeView( getScreenWithId( FUNCTION_CAMERA_PAGE_SCREEN_ID ) );
				if( getCurrentPage() > mCameraPageIndexInViewGroup )
					setCurrentPage( getCurrentPage() - 1 );
			}
		}
		//音乐页
		//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		boolean mIsMusicPageInNormalPageRight = ( LauncherDefaultConfig.getMusicPagePosition() > 0 );
		if( mIsMusicPageInNormalPageRight )
		//xiatian add end
		{
			// YANGTIANYU@2016/08/19 UPD START
			// i_0014335 
			//long mMusicPageIndex = 0;
			//if( mMusicPageView != null )
			//{
			//	mMusicPageIndex = getPageIndexForScreenId( FUNCTION_MUSIC_PAGE_SCREEN_ID );				
			// YANGTIANYU@2016/08/19 UPD END
			long mMusicPageIndex = getPageIndexForScreenId( FUNCTION_MUSIC_PAGE_SCREEN_ID );
			if( mMusicPageIndex > 0 )
			{
				mScreenOrder.remove( FUNCTION_MUSIC_PAGE_SCREEN_ID );
				removeView( getScreenWithId( FUNCTION_MUSIC_PAGE_SCREEN_ID ) );
				if( getCurrentPage() > mMusicPageIndex )
					setCurrentPage( getCurrentPage() - 1 );
			}
		}
	}
	
	/**将专属页加回到桌面的消息*/
	private final int ADD_MEDIAPAGE_TO_SCREEN = -1111;
	/**处理专属页添加回桌面的handler*/
	private Handler mMediaPageHandler = new Handler() {
		
		@Override
		public void handleMessage(
				android.os.Message msg )
		{
			switch( msg.what )
			{
				case ADD_MEDIAPAGE_TO_SCREEN:
					addFunctionPagesToScreenOnlyWhenDragEnd();
					break;
				default:
					break;
			}
		}
	};
	
	/**
	 * 将专属页加回到屏幕上。通过mWorkspaceScreens中的数据，添加到：1、mScreenOrder中；2、Workspace的Children中
	 * @author yangtianyu 2016-7-22
	 */
	private void addFunctionPagesToScreenOnlyWhenDragEnd()
	{
		// YANGTIANYU@2016/08/19 ADD START
		// i_0014335 
		// 在生成文件夹后立即长按新生成的文件夹,会先执行两次onDragStart后再执行onDragEnd
		// 这种情况下,执行到本方法时其实处于图标抬起的状态,所以此时不应该执行本方法
		if( isDragging() )
			return;
		// YANGTIANYU@2016/08/19 ADD END
		DeviceProfile grid = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
		int mSearchBarSpaceHeightPx = grid.getSearchBarSpaceHeightPx();
		//酷生活
		//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		CellLayout mFavoritesPageView = getScreenWithId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		if(
		//
		mFavoritesPageView != null
		// 
		&& ( mFavoritesPageView.getParent() == null/*//cheyingkun add	//解决“功能页重复添加引起桌面重启”的问题【i_0014441】*/)
		//
		)
		{
			int mFavoritesPageIndexInViewGroup = -1;
			mFavoritesPageIndexInViewGroup = getToAddFunctionPageIndexInViewGroupBeforeAddToScreenOrderAndViewGroup( FUNCTION_FAVORITES_PAGE_SCREEN_ID );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			mScreenOrder.add( ( mFavoritesPageIndexInViewGroup == -1 ? mScreenOrder.size() : mFavoritesPageIndexInViewGroup ) , FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			addFavoritesPage( mFavoritesPageView , mFavoritesPageIndexInViewGroup );
		}
		//xiatian add end
		//相机页
		CellLayout mCameraPageView = getScreenWithId( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		if(
		//
		mCameraPageView != null
		// 
		&& ( mCameraPageView.getParent() == null/*//cheyingkun add	//解决“功能页重复添加引起桌面重启”的问题【i_0014441】*/)
		//
		)
		{
			int mCameraPageIndexInViewGroup = -1;
			mCameraPageIndexInViewGroup = getToAddFunctionPageIndexInViewGroupBeforeAddToScreenOrderAndViewGroup( FUNCTION_CAMERA_PAGE_SCREEN_ID );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			mScreenOrder.add( ( mCameraPageIndexInViewGroup == -1 ? mScreenOrder.size() : mCameraPageIndexInViewGroup ) , FUNCTION_CAMERA_PAGE_SCREEN_ID );
			addNoSearchPage( mCameraPageView , mSearchBarSpaceHeightPx , mCameraPageIndexInViewGroup );
		}
		//音乐页
		CellLayout mMusicPageView = getScreenWithId( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		if(
		//
		mMusicPageView != null
		// 
		&& ( mMusicPageView.getParent() == null/*//cheyingkun add	//解决“功能页重复添加引起桌面重启”的问题【i_0014441】*/)
		//
		)
		{
			int mMusicPageIndexInViewGroup = -1;
			mMusicPageIndexInViewGroup = getToAddFunctionPageIndexInViewGroupBeforeAddToScreenOrderAndViewGroup( FUNCTION_MUSIC_PAGE_SCREEN_ID );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			mScreenOrder.add( ( mMusicPageIndexInViewGroup == -1 ? mScreenOrder.size() : mMusicPageIndexInViewGroup ) , FUNCTION_MUSIC_PAGE_SCREEN_ID );
			addNoSearchPage( mMusicPageView , mSearchBarSpaceHeightPx , mMusicPageIndexInViewGroup );
		}
	}
	
	/**
	 * 完全移除专属页
	 * @author yangtianyu 2016-7-14
	 */
	private void removeMediaPages()
	{
		removeCameraPage();
		removeMusicPage();
		//xiatian del start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//		if( mRestorePage == INVALID_RESTORE_PAGE )
		//		{
		//			setCurrentPage( getCurrentPage() - 1 );
		//		}
		//xiatian del end
	}
	
	/**
	 *移除音乐页(抽出来的方法)
	 * @author gaominghui 2017年6月27日
	 */
	public void removeMusicPage()
	{
		if( hasMusicPage() )
		{
			int mMusicPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			removeMediaPageByScreenId( FUNCTION_MUSIC_PAGE_SCREEN_ID , MusicView.getInstance().getMusicPageView( mLauncher ) );
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			if( mRestorePage != INVALID_RESTORE_PAGE )
			{
				if( mMusicPageIndexInViewGroup < mRestorePage )
				{
					mRestorePage = mRestorePage - 1;
				}
			}
			else
			{
				int mCurrentPage = getCurrentPage();
				if( mMusicPageIndexInViewGroup < mCurrentPage )
				{
					setCurrentPage( mCurrentPage - 1 );
				}
			}
			//xiatian add end
		}
	}
	
	/**
	 *移除相机页(抽出来的方法)
	 * @author gaominghui 2017年6月27日
	 */
	public void removeCameraPage()
	{
		if( hasCameraPage() )
		{
			int mCameraPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			removeMediaPageByScreenId( FUNCTION_CAMERA_PAGE_SCREEN_ID , CameraView.getInstance().getCameraPageView( mLauncher ) );
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			if( mRestorePage != INVALID_RESTORE_PAGE )
			{
				if( mCameraPageIndexInViewGroup < mRestorePage )
				{
					mRestorePage = mRestorePage - 1;
				}
			}
			else
			{
				int mCurrentPage = getCurrentPage();
				if( mCameraPageIndexInViewGroup < mCurrentPage )
				{
					setCurrentPage( mCurrentPage - 1 );
				}
			}
			//xiatian add end
		}
	}
	
	/**
	 * 根据专属页ID删除对应的celllayout与其中的专属页view
	 * @param mMediaPageScreenId 专属页id
	 * @param mMediaPageRootView 专属页view
	 * @author yangtianyu 2016-7-14
	 */
	private void removeMediaPageByScreenId(
			long mMediaPageScreenId ,
			View mMediaPageRootView )
	{
		CellLayout mMediaPageScreen = getScreenWithId( mMediaPageScreenId );
		if( mMediaPageScreen == null )
		{
			throw new RuntimeException( StringUtils.concat( "Expected mMediaPageScreen no exist.--mMediaPageScreenId:" , mMediaPageScreenId ) );
		}
		if( mMediaPageRootView != null )
		{
			mMediaPageScreen.removeViewFromCell( mMediaPageRootView );
		}
		mWorkspaceScreens.remove( mMediaPageScreenId );
		mScreenOrder.remove( mMediaPageScreenId );
		removeView( mMediaPageScreen );
	}
	
	//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
	private void checkNextPageInOverViewModel()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR && isOverViewModel )
		{
			int nextPage = getNextPage();
			//酷生活页
			if( isFavoritesPageByPageIndex( nextPage ) )
			{
				if( isRtl )
				{
					mNextPage -= 1;
				}
				else
				{
					mNextPage += 1;
				}
			}
			//相机页、音乐页
			if( isCameraPage( nextPage ) || isMusicPage( nextPage ) )
			{
				if( isRtl )
				{
					mNextPage += 1;
				}
				else
				{
					mNextPage -= 1;
				}
			}
		}
	}
	
	/**显示或者隐藏酷生活和专属页的页面指示器*/
	private void showOrHideFunctionPagesPageIndicator(
			boolean show )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
		{
			ArrayList<Integer> mFunctionPagesIndex = new ArrayList<Integer>();
			int mFavoritesPageIndex = getPageIndexForScreenId( FUNCTION_MUSIC_PAGE_SCREEN_ID );
			if( mFavoritesPageIndex >= 0 )
			{
				mFunctionPagesIndex.add( mFavoritesPageIndex );
			}
			int mMusicPageIndex = getPageIndexForScreenId( FUNCTION_CAMERA_PAGE_SCREEN_ID );
			if( mMusicPageIndex >= 0 )
			{
				mFunctionPagesIndex.add( mMusicPageIndex );
			}
			int mCameraPageIndex = getPageIndexForScreenId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			if( mCameraPageIndex >= 0 )
			{
				mFunctionPagesIndex.add( mCameraPageIndex );
			}
			getPageIndicator().showOrHideFunctionPagesPageIndicator( mFunctionPagesIndex , show );
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
	/**
	 * 模式改变时,改变选中的页面
	 * @param enable
	 */
	public void checkSelectedPageWhenChangeState(
			boolean enable )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_FREESCROLL )
		{
			CellLayout screenWithId = getScreenWithId( getScreenIdForPageIndex( mCurrentPage ) );
			screenWithId.setUseActiveGlowBackground( enable );
			screenWithId.invalidate();
		}
	}
	
	/**
	 * 编辑模式切页结束后,改变选中的页面
	 */
	public void checkSelectedPageInOverView()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_FREESCROLL && isOverViewModel && isInOverviewMode() )
		{
			if( mCurrentPage != mNextPage )
			{
				CellLayout screenWithId = getScreenWithId( getScreenIdForPageIndex( mCurrentPage ) );
				if( mNextPage == -1 )
				{
					screenWithId.setUseActiveGlowBackground( true );
				}
				else
				{
					screenWithId.setUseActiveGlowBackground( false );
				}
				screenWithId.invalidate();
			}
		}
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//fix bug：解决“特定页面（酷生活、主页、音乐页和相机页）的页面指示器显示特定图标时，页面指示器显示错误（重复以及错位）”的问题。
	private void updateHomePageIndicator()
	{
		int mStartIndex = 0;
		int mEndIndex = getChildCount();
		if( hasFavoritesPage() )
		{
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“页面指示器中主页item的图标没有显示为主页图标”的问题）
			if( LauncherDefaultConfig.getFavoritesPagePosition() > 0 )
			{
				mEndIndex--;
			}
			else
			//xiatian add end
			{
				mStartIndex++;
			}
		}
		if( hasCameraPage() )
		{
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“页面指示器中主页item的图标没有显示为主页图标”的问题）
			if( LauncherDefaultConfig.getCameraPagePosition() < 0 )
			{
				mStartIndex++;
			}
			else
			//xiatian add end
			{
				mEndIndex--;
			}
		}
		if( hasMusicPage() )
		{
			//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“页面指示器中主页item的图标没有显示为主页图标”的问题）
			if( LauncherDefaultConfig.getMusicPagePosition() < 0 )
			{
				mStartIndex++;
			}
			else
			//xiatian add end
			{
				mEndIndex--;
			}
		}
		PageIndicator mPageIndicator = getPageIndicator();
		int mDefaultPageIndex = getDefaultPageIndex();
		for( int i = mStartIndex ; i < mEndIndex ; i++ )
		{
			if( i == mDefaultPageIndex )
			{
				mPageIndicator.updateMarker( i , mPageIndicator.getHomePageMarkerResources() );
			}
			else
			{
				mPageIndicator.updateMarker( i , getPageIndicatorMarker( i ) );
			}
		}
	}
	//xiatian add end
	;
	
	public boolean isFavoritesPageShowSearch()
	{
		if(
		//
		( mLauncher.getSearchDropTargetBar() != null/*新闻页不显示搜索框，zhujieping add*/)
		//
		&& (
		//
		( !isFavoritesPageShowLauncherSearch()/*//并且-1屏不显示桌面搜索	//cheyingkun add	//phenix1.1稳定版移植酷生活*/)
		//cheyingkun add start	//酷生活引导页
				|| (
				//
				isFavoritesPageShowLauncherSearch()
				//
				&& FavoritesPageManager.getInstance( mLauncher ).isShowFavoriteClings()/* //酷生活接口，判断是否正在显示酷生活引导页，酷生活初始化未完成时，这个判断无效 */
				//
				)
		//cheyingkun add end
		|| ( mLauncher.isShowFavoritesClings()/*//判断是否会显示酷生活引导页（酷生活初始化未完成时，桌面通过开关来自己判断，其实是酷生活添加引导页的判断逻辑）//cheyingkun add	//解决“打开酷生活引导页，切页到酷生活后，点击引导页按钮，搜索栏显示异常”的问题*/) )
		//
		)
		{
			return false;
		}
		return true;
	}
	
	//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
	public boolean isInNormalMode()
	{
		return mState == State.NORMAL;
	}
	//xiatian add end
	;
	
	//zhujieping add start
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
		//同步处理当前页
		// YANGTIANYU@2015/12/11 UPD START
		// 编辑模式下，切页动画未完成时，迅速点击更换主题，出现重启
		// 这样修改后，上述情况下会出现当动画完成后，当前页面会较迟完成图标更换
		//CellLayout mCellLayout = (CellLayout)getChildAt( getCurrentPage() );
		int mCurrentPageIndex = getCurrentPage();
		CellLayout mCellLayout = (CellLayout)getChildAt( mCurrentPageIndex );
		// YANGTIANYU@2015/12/11 UPD END
		if( mCellLayout != null )
		{
			mCellLayout.onThemeChanged( arg0 , arg1 );
		}
		//异步处理其他页面
		int mCount = getChildCount();
		for( int i = 0 ; i < mCount ; i++ )
		{
			// YANGTIANYU@2015/12/11 DEL START
			//int mCurrentPageIndex = getCurrentPage();
			// YANGTIANYU@2015/12/11 DEL END
			if( i == mCurrentPageIndex )
			{
				continue;
			}
			mCellLayout = (CellLayout)getChildAt( i );
			mCellLayout.onThemeChanged( arg0 , arg1 );
		}
	}
	//zhujieping add end
	;
	
	//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
	/**
	 * 
	 *判断点击的区域是否为指定view
	 * @param ev
	 * @param v
	 * @return
	 * @author gaominghui 2017年2月10日
	 */
	private boolean isCurrentTouchView(
			MotionEvent ev ,
			View v )
	{
		int[] location = new int[2];
		v.getLocationOnScreen( location );
		int width = (int)( v.getWidth() * mOverviewModeShrinkFactor );
		int height = v.getHeight();
		if( ev.getRawX() > location[0] && location[0] + width > ev.getRawX() && ev.getRawY() > location[1] && ev.getRawY() < location[1] + height )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 判断点击区域是否为celllayout区域
	 *
	 * @param ev
	 * @param v
	 * @return
	 * @author gaominghui 2017年2月10日
	 */
	private boolean isCelllayoutTouchView(
			MotionEvent ev ,
			View v )
	{
		int[] location = new int[2];
		v.getLocationOnScreen( location );
		int width = (int)( v.getWidth() * mOverviewModeShrinkFactor );
		if( ev.getRawX() > location[0] && location[0] + width > ev.getRawX() )
		{
			return true;
		}
		return false;
	}
	
	/**
	 *退出编辑模式时要隐藏掉设置主页的小房子图标
	 * @author gaominghui 2017年2月10日
	 */
	private void hideEditModeHomeView()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
		{
			for( int i = 0 ; i < getChildCount() ; i++ )
			{
				CellLayout cl = (CellLayout)getChildAt( i );
				if( cl.getEditModeHomeViewVisible() == View.VISIBLE )
				{
					cl.setEditModeHomeViewVisible( View.GONE );
				}
			}
		}
	}
	//gaominghui add end
	;
	
	//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	private int getToAddFunctionPageIndexInViewGroupBeforeAddToScreenOrderAndViewGroup(
			long mFunctionPageToAddScreenId )
	{
		int mFunctionPageToAddIndexInViewGroup = 0;
		int mFunctionPageToAddIndexKey = 0;
		boolean mIsEnableFunctionPageOtherA = false;
		int mFunctionPageOtherAIndexKey = 0;
		int mFunctionPageOtherAIndexInViewGroup = -1;
		boolean mIsEnableFunctionPageOtherB = false;
		int mFunctionPageOtherBIndexKey = 0;
		int mFunctionPageOtherBIndexInViewGroup = -1;
		if( mFunctionPageToAddScreenId == FUNCTION_FAVORITES_PAGE_SCREEN_ID )
		{
			mFunctionPageToAddIndexKey = LauncherDefaultConfig.getFavoritesPagePosition();
			mIsEnableFunctionPageOtherA = LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW;
			mFunctionPageOtherAIndexKey = LauncherDefaultConfig.getCameraPagePosition();
			mFunctionPageOtherAIndexInViewGroup = getPageIndexForScreenId( FUNCTION_CAMERA_PAGE_SCREEN_ID );
			mIsEnableFunctionPageOtherB = LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW;
			mFunctionPageOtherBIndexKey = LauncherDefaultConfig.getMusicPagePosition();
			mFunctionPageOtherBIndexInViewGroup = getPageIndexForScreenId( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		}
		else if( mFunctionPageToAddScreenId == FUNCTION_CAMERA_PAGE_SCREEN_ID )
		{
			mFunctionPageToAddIndexKey = LauncherDefaultConfig.getCameraPagePosition();
			mIsEnableFunctionPageOtherA = LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES;
			mFunctionPageOtherAIndexKey = LauncherDefaultConfig.getFavoritesPagePosition();
			mFunctionPageOtherAIndexInViewGroup = getPageIndexForScreenId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			mIsEnableFunctionPageOtherB = LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW;
			mFunctionPageOtherBIndexKey = LauncherDefaultConfig.getMusicPagePosition();
			mFunctionPageOtherBIndexInViewGroup = getPageIndexForScreenId( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		}
		else if( mFunctionPageToAddScreenId == FUNCTION_MUSIC_PAGE_SCREEN_ID )
		{
			mFunctionPageToAddIndexKey = LauncherDefaultConfig.getMusicPagePosition();
			mIsEnableFunctionPageOtherA = LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES;
			mFunctionPageOtherAIndexKey = LauncherDefaultConfig.getFavoritesPagePosition();
			mFunctionPageOtherAIndexInViewGroup = getPageIndexForScreenId( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
			mIsEnableFunctionPageOtherB = LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW;
			mFunctionPageOtherBIndexKey = LauncherDefaultConfig.getCameraPagePosition();
			mFunctionPageOtherBIndexInViewGroup = getPageIndexForScreenId( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		}
		else
		{
			throw new IllegalStateException( StringUtils.concat( "Unknown mFunctionPageToAddScreenId:" , mFunctionPageToAddScreenId ) );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "getToAddFunctionPageIndexInViewGroupBeforeAddToScreenOrderAndViewGroup" , StringUtils.concat(
					"mFunctionPageToAddScreenId:" ,
					mFunctionPageToAddScreenId ,
					"-mFunctionPageToAddIndexKey:" ,
					mFunctionPageToAddIndexKey ,
					"-mFunctionPageOtherASwitch:" ,
					mIsEnableFunctionPageOtherA ,
					"-mFunctionPageOtherAIndexKey:" ,
					mFunctionPageOtherAIndexKey ,
					"-mFunctionPageOtherAIndexInViewGroup:" ,
					mFunctionPageOtherAIndexInViewGroup ,
					"-mFunctionPageOtherBSwitch:" ,
					mIsEnableFunctionPageOtherB ,
					"-mFunctionPageOtherBIndexKey:" ,
					mFunctionPageOtherBIndexKey ,
					"-mFunctionPageOtherBIndexInViewGroup:" ,
					mFunctionPageOtherBIndexInViewGroup ) );
		}
		if(
		//
		mIsEnableFunctionPageOtherA
		//
		&& ( mFunctionPageOtherAIndexKey != 0 )
		//
		&& ( mFunctionPageOtherAIndexInViewGroup != -1 )
		//
		)
		{//【打开“FunctionPageOtherA”】并且【已经添加“FunctionPageOtherA”】：需要考虑后添加的“mFunctionPageToAdd”和已经添加“FunctionPageOtherA”两者的相对位置
			if(
			//
			mIsEnableFunctionPageOtherB
			//
			&& ( mFunctionPageOtherBIndexKey != 0 )
			//
			&& ( mFunctionPageOtherBIndexInViewGroup != -1 )
			//
			)
			{//【打开“mFunctionPageOtherB”】并且【已经添加“mFunctionPageOtherB”】：需要考虑后添加的“mFunctionPageToAdd”和已经添加“mFunctionPageOtherA”、已经添加“mFunctionPageOtherB”三者的相对位置
				if( mFunctionPageOtherAIndexKey > mFunctionPageOtherBIndexKey )
				{//“mFunctionPageOtherA”在“mFunctionPageOtherB”右边
					if( mFunctionPageToAddIndexKey > mFunctionPageOtherAIndexKey )
					{//“mFunctionPageToAdd”在“mFunctionPageOtherA”右边
						if( mFunctionPageOtherAIndexKey > 0 )
						{//“mFunctionPageOtherA”在右边：则“mFunctionPageToAdd”在最右边
							mFunctionPageToAddIndexInViewGroup = -1;
						}
						else
						{//“mFunctionPageOtherA”在左边
							if( mFunctionPageToAddIndexKey > 0 )
							{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在最右边
								mFunctionPageToAddIndexInViewGroup = -1;
							}
							else
							{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在“mFunctionPageOtherA”右边
								mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherAIndexInViewGroup + 1;
							}
						}
					}
					else if( mFunctionPageToAddIndexKey < mFunctionPageOtherBIndexKey )
					{//“mFunctionPageToAdd”在“mFunctionPageOtherB”左边
						if( mFunctionPageOtherBIndexKey < 0 )
						{//“mFunctionPageOtherB”左边：则“mFunctionPageToAdd”在最左边
							mFunctionPageToAddIndexInViewGroup = 0;
						}
						else
						{//“mFunctionPageOtherB”右边
							if( mFunctionPageToAddIndexKey < 0 )
							{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在最左边
								mFunctionPageToAddIndexInViewGroup = 0;
							}
							else
							{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在“mFunctionPageOtherB”左边
								mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherBIndexInViewGroup;
							}
						}
					}
					else
					{//“mFunctionPageToAdd”在“mFunctionPageOtherA”左边，“mFunctionPageToAdd”在“mFunctionPageOtherB”右边
						if( mFunctionPageToAddIndexKey < 0 )
						{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在“mFunctionPageOtherB”右边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherBIndexInViewGroup + 1;
						}
						else
						{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在“mFunctionPageOtherA”左边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherAIndexInViewGroup;
						}
					}
				}
				else
				{//“mFunctionPageOtherA”在“mFunctionPageOtherB”左边
					if( mFunctionPageToAddIndexKey > mFunctionPageOtherBIndexKey )
					{//“mFunctionPageToAdd”在“mFunctionPageOtherB”右边
						if( mFunctionPageOtherBIndexKey > 0 )
						{//“mFunctionPageOtherB”在右边：则“酷生活”在最右边
							mFunctionPageToAddIndexInViewGroup = -1;
						}
						else
						{//“mFunctionPageOtherB”在左边
							if( mFunctionPageToAddIndexKey > 0 )
							{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在最右边
								mFunctionPageToAddIndexInViewGroup = -1;
							}
							else
							{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在“mFunctionPageOtherB”右边
								mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherBIndexInViewGroup + 1;
							}
						}
					}
					else if( mFunctionPageToAddIndexKey < mFunctionPageOtherAIndexKey )
					{//“mFunctionPageToAdd”在“mFunctionPageOtherA”左边
						if( mFunctionPageOtherAIndexKey < 0 )
						{//“mFunctionPageOtherA”左边：则“mFunctionPageToAdd”在最左边
							mFunctionPageToAddIndexInViewGroup = 0;
						}
						else
						{//“mFunctionPageOtherA”右边
							if( mFunctionPageToAddIndexKey < 0 )
							{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在最左边
								mFunctionPageToAddIndexInViewGroup = 0;
							}
							else
							{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在“mFunctionPageOtherA”左边
								mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherAIndexInViewGroup;
							}
						}
					}
					else
					{//“mFunctionPageToAdd”在“mFunctionPageOtherBIndexInViewGroup”左边，“mFunctionPageToAdd”在“mFunctionPageOtherA”右边
						if( mFunctionPageToAddIndexKey < 0 )
						{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在“mFunctionPageOtherA”右边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherAIndexInViewGroup + 1;
						}
						else
						{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在“mFunctionPageOtherB”左边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherBIndexInViewGroup;
						}
					}
				}
			}
			else
			{//1、【关闭“mFunctionPageOtherB”】；2、【打开“mFunctionPageOtherB”】并且【没有添加“mFunctionPageOtherB”】：需要考虑后添加的“mFunctionPageToAdd”和已经添加“mFunctionPageOtherA”两者的相对位置
				if( mFunctionPageToAddIndexKey > mFunctionPageOtherAIndexKey )
				{//“mFunctionPageToAdd”在“mFunctionPageOtherA”右边
					if( mFunctionPageOtherAIndexKey > 0 )
					{//“mFunctionPageOtherA”在右边：则“mFunctionPageToAdd”在最右边
						mFunctionPageToAddIndexInViewGroup = -1;
					}
					else
					{//“mFunctionPageOtherA”在左边
						if( mFunctionPageToAddIndexKey < 0 )
						{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在“mFunctionPageOtherA”右边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherAIndexInViewGroup + 1;
						}
						else
						{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在最右边
							mFunctionPageToAddIndexInViewGroup = -1;
						}
					}
				}
				else
				{//“mFunctionPageToAdd”在“mFunctionPageOtherA”左边
					if( mFunctionPageOtherAIndexKey < 0 )
					{//“mFunctionPageOtherA”在左边：则“mFunctionPageToAdd”在最左边
						mFunctionPageToAddIndexInViewGroup = 0;
					}
					else
					{//“mFunctionPageOtherA”在右边
						if( mFunctionPageToAddIndexKey < 0 )
						{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在最左边
							mFunctionPageToAddIndexInViewGroup = 0;
						}
						else
						{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在“mFunctionPageOtherA”左边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherAIndexInViewGroup;
						}
					}
				}
			}
		}
		else
		{//1、【关闭“mFunctionPageOtherA”】；2、【打开“mFunctionPageOtherA”】并且【没有添加“mFunctionPageOtherA”】：需要考虑后添加的“mFunctionPageToAdd”和“mFunctionPageOtherB”两者的相对位置
			if(
			//
			mIsEnableFunctionPageOtherB
			//
			&& ( mFunctionPageOtherBIndexKey != 0 )
			//
			&& ( mFunctionPageOtherBIndexInViewGroup != -1 )
			//
			)
			{//【打开“mFunctionPageOtherB”】并且【已经添加“mFunctionPageOtherB”】：需要考虑后添加的“mFunctionPageToAdd”和已经添加“mFunctionPageOtherB”两者的相对位置
				if( mFunctionPageToAddIndexKey > mFunctionPageOtherBIndexKey )
				{//“mFunctionPageToAdd”在“mFunctionPageOtherB”右边
					if( mFunctionPageOtherBIndexKey > 0 )
					{//“mFunctionPageOtherB”在右边：则“mFunctionPageToAdd”在最右边
						mFunctionPageToAddIndexInViewGroup = -1;
					}
					else
					{//“mFunctionPageOtherB”在左边
						if( mFunctionPageToAddIndexKey < 0 )
						{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在“mFunctionPageOtherB”右边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherBIndexInViewGroup + 1;
						}
						else
						{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在最右边
							mFunctionPageToAddIndexInViewGroup = -1;
						}
					}
				}
				else
				{//“mFunctionPageToAdd”在“mFunctionPageOtherB”左边
					if( mFunctionPageOtherBIndexKey < 0 )
					{//“mFunctionPageOtherB”在左边：则“mFunctionPageToAdd”在最左边
						mFunctionPageToAddIndexInViewGroup = 0;
					}
					else
					{//“mFunctionPageOtherB”在右边
						if( mFunctionPageToAddIndexKey < 0 )
						{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在最左边
							mFunctionPageToAddIndexInViewGroup = 0;
						}
						else
						{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在“mFunctionPageOtherB”左边
							mFunctionPageToAddIndexInViewGroup = mFunctionPageOtherBIndexInViewGroup;
						}
					}
				}
			}
			else
			{//1、【关闭“mFunctionPageOtherB”】；2、【打开“mFunctionPageOtherB”】并且【没有添加“mFunctionPageOtherB”】：需要考虑后添加的“mFunctionPageToAdd”的位置
				if( mFunctionPageToAddIndexKey < 0 )
				{//“mFunctionPageToAdd”在左边：则“mFunctionPageToAdd”在最左边
					mFunctionPageToAddIndexInViewGroup = 0;
				}
				else
				{//“mFunctionPageToAdd”在右边:则“mFunctionPageToAdd”在最右边
					mFunctionPageToAddIndexInViewGroup = -1;
				}
			}
		}
		//xiatian add end
		return mFunctionPageToAddIndexInViewGroup;
	}
	
	private void updateStateForFunctionPages()
	{
		if( isInNormalMode() == false )
		{
			return;
		}
		boolean mIsWorkspaceLoop = isLoop();
		boolean mIsOverScroll = false;
		if(
		//
		( mOverScrollX < 0 )
		//
		|| mOverScrollX > mMaxScrollX
		//
		)
		{
			mIsOverScroll = true;
		}
		if(
		//
		( mIsWorkspaceLoop == false )
		//
		&& ( mIsOverScroll )
		//
		)
		{//无内容反馈时，不需要特殊处理
			return;
		}
		boolean mIsLayoutRtl = isLayoutRtl();
		int mFavoritesPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		int mCameraPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		int mMusicPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		int mWorkspceScrollX = getScrollX();
		int mPageWidth = getViewportWidth();
		if( mPageWidth == 0 )
		{//获取屏幕参数不正确时，不做相应处理
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.v( TAG , "updateStateForFunctionPages - return -error[ mPageWidth == 0 ]" );
			}
			return;
		}
		int mLeftPageIndex = -1;//参与滑动的两个页面中，左边页面的index
		int mRightPageIndex = -1;//参与滑动的两个页面中，右边页面的index
		float mLeftPageScrollProgress = 0;//即：左边页面，在屏幕外的区域的宽度占总页面宽度的百分比
		if(
		//
		( mIsWorkspaceLoop )
		//
		&& ( mIsOverScroll )
		//
		)
		{
			mRightPageIndex = 0;
			mLeftPageIndex = getChildCount() - 1;
			mLeftPageScrollProgress = ( ( mPageWidth + mOverScrollX ) % mPageWidth ) / (float)mPageWidth;
		}
		else
		{
			mLeftPageScrollProgress = ( mWorkspceScrollX % mPageWidth ) / (float)mPageWidth;
			if( mIsLayoutRtl )
			{
				mRightPageIndex = ( mMaxScrollX - mWorkspceScrollX ) / mPageWidth;
				mLeftPageIndex = mRightPageIndex + 1;
				if(
				//
				( mLeftPageScrollProgress == 0f )
				//
				&& ( ( mMaxScrollX - mWorkspceScrollX ) == mRightPageIndex * mPageWidth )
				//
				)
				{
					mLeftPageScrollProgress = 1f;
				}
			}
			else
			{
				mLeftPageIndex = mWorkspceScrollX / mPageWidth;
				mRightPageIndex = mLeftPageIndex + 1;
				if(
				//
				( mLeftPageScrollProgress == 0f )
				//
				&& ( mWorkspceScrollX == mRightPageIndex * mPageWidth )
				//
				)
				{
					mLeftPageScrollProgress = 1f;
				}
			}
		}
		boolean mIsFavoritesPageScroll = false;
		boolean mIsCameraPageScroll = false;
		boolean mIsMusicPageScroll = false;
		if(
		//
		( mFavoritesPageIndexInViewGroup == mLeftPageIndex )
		//
		|| ( mFavoritesPageIndexInViewGroup == mRightPageIndex )
		//
		)
		{
			mIsFavoritesPageScroll = true;
		}
		if(
		//
		( mCameraPageIndexInViewGroup == mLeftPageIndex )
		//
		|| ( mCameraPageIndexInViewGroup == mRightPageIndex )
		//
		)
		{
			mIsCameraPageScroll = true;
		}
		if(
		//
		( mMusicPageIndexInViewGroup == mLeftPageIndex )
		//
		|| ( mMusicPageIndexInViewGroup == mRightPageIndex )
		//
		)
		{
			mIsMusicPageScroll = true;
		}
		if(
		//
		( mIsFavoritesPageScroll )
		//
		&& (
		//
		( mIsCameraPageScroll == false )
		//
		&& ( mIsMusicPageScroll == false )
		//
		)
		//
		)
		{
			/*
			//酷+普
			//	页面背景的透明度
			//		——随着酷显示的百分比走
			//	搜索栏
			//		酷不显示搜索栏时
			//			——随着普走
			//		酷显示搜索栏时
			//			——不处理
			//	页面指示器
			//		——随着普走
			//	底边栏
			//		——随着普走
			*/
			float mFavoritesPageScrollProgress = 0;
			float mTranslationXForItemsScrollWithNormalPage = 0;
			if( mFavoritesPageIndexInViewGroup == mLeftPageIndex )
			{
				if(
				//
				( mIsLayoutRtl )
				//
				&& ( mIsWorkspaceLoop )
				//
				&& ( mIsOverScroll )
				//
				)
				{
					mFavoritesPageScrollProgress = mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithNormalPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
				}
				else
				{
					mFavoritesPageScrollProgress = 1 - mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithNormalPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
				}
			}
			else
			{
				if(
				//
				( mIsLayoutRtl )
				//
				&& ( mIsWorkspaceLoop )
				//
				&& ( mIsOverScroll )
				//
				)
				{
					mFavoritesPageScrollProgress = 1 - mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithNormalPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
				}
				else
				{
					mFavoritesPageScrollProgress = mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithNormalPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
				}
			}
			//ItemsScrollWithFavoritesPage
			////FavoritesPage(BackgroundAlpha)
			setBackgroundAlpha( mFavoritesPageScrollProgress * 0.8f );
			//ItemsScrollWithNormalPage
			////Hotseat
			Hotseat mHotseat = mLauncher.getHotseat();
			if( mHotseat != null )
			{
				mHotseat.setTranslationX( mTranslationXForItemsScrollWithNormalPage );
			}
			////PageIndicator
			PageIndicator mPageIndicator = getPageIndicator();
			if( mPageIndicator != null )
			{
				mPageIndicator.setTranslationX( mTranslationXForItemsScrollWithNormalPage );
			}
			////SearchDropTargetBar
			if( !isFavoritesPageShowSearch() )
			{
				SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
				if( mSearchDropTargetBar != null )
				{
					mSearchDropTargetBar.setTranslationX( mTranslationXForItemsScrollWithNormalPage );
				}
			}
			//NotifyFavoritesPage
			if( mFavoritesPageCallbacks != null )
			{
				mFavoritesPageCallbacks.onScrollProgressChanged( mFavoritesPageScrollProgress );
			}
		}
		else if(
		//
		( mIsFavoritesPageScroll == false )
		//
		&& (
		//
		( mIsCameraPageScroll && mIsMusicPageScroll == false )
		//
		|| ( mIsCameraPageScroll == false && mIsMusicPageScroll )
		//
		)
		//
		)
		{
			/*
			//媒+普
			//	搜索栏
			//		——随着普走
			*/
			//ItemsScrollWithNormalPage
			////SearchDropTargetBar
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
			{
				SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
				if( mSearchDropTargetBar != null )
				{
					float mTranslationXForItemsScrollWithNormalPage = 0;
					if( mIsCameraPageScroll && mIsMusicPageScroll == false )
					{
						if( mCameraPageIndexInViewGroup == mLeftPageIndex )
						{
							if(
							//
							( mIsLayoutRtl )
							//
							&& ( mIsWorkspaceLoop )
							//
							&& ( mIsOverScroll )
							//
							)
							{
								mTranslationXForItemsScrollWithNormalPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
							}
							else
							{
								mTranslationXForItemsScrollWithNormalPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
							}
						}
						else
						{
							if(
							//
							( mIsLayoutRtl )
							//
							&& ( mIsWorkspaceLoop )
							//
							&& ( mIsOverScroll )
							//
							)
							{
								mTranslationXForItemsScrollWithNormalPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
							}
							else
							{
								mTranslationXForItemsScrollWithNormalPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
							}
						}
					}
					else if( mIsCameraPageScroll == false && mIsMusicPageScroll )
					{
						if( mMusicPageIndexInViewGroup == mLeftPageIndex )
						{
							if(
							//
							( mIsLayoutRtl )
							//
							&& ( mIsWorkspaceLoop )
							//
							&& ( mIsOverScroll )
							//
							)
							{
								mTranslationXForItemsScrollWithNormalPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
							}
							else
							{
								mTranslationXForItemsScrollWithNormalPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
							}
						}
						else
						{
							if(
							//
							( mIsLayoutRtl )
							//
							&& ( mIsWorkspaceLoop )
							//
							&& ( mIsOverScroll )
							//
							)
							{
								mTranslationXForItemsScrollWithNormalPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
							}
							else
							{
								mTranslationXForItemsScrollWithNormalPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
							}
						}
					}
					mSearchDropTargetBar.setTranslationX( mTranslationXForItemsScrollWithNormalPage );
				}
			}
		}
		else if(
		//
		( mIsFavoritesPageScroll )
		//
		&& (
		//
		( mIsCameraPageScroll && mIsMusicPageScroll == false )
		//
		|| ( mIsCameraPageScroll == false && mIsMusicPageScroll )
		//
		)
		//
		)
		{
			/*
			//酷+媒
			//	页面背景的透明度
			//		——随着酷显示的百分比走
			//	搜索栏
			//		酷不显示搜索栏时
			//			——不处理
			//		酷显示搜索栏时
			//			——随着酷走 
			//	页面指示器
			//		——随着媒走
			//	底边栏
			//		——随着媒走
			*/
			float mFavoritesPageScrollProgress = 0;
			float mTranslationXForItemsScrollWithFavoritesPage = 0;
			float mTranslationXForItemsScrollWithMediaPage = 0;
			if( mFavoritesPageIndexInViewGroup == mLeftPageIndex )
			{
				if(
				//
				( mIsLayoutRtl )
				//
				&& ( mIsWorkspaceLoop )
				//
				&& ( mIsOverScroll )
				//
				)
				{
					mFavoritesPageScrollProgress = mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithFavoritesPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
					mTranslationXForItemsScrollWithMediaPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
				}
				else
				{
					mFavoritesPageScrollProgress = 1 - mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithFavoritesPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
					mTranslationXForItemsScrollWithMediaPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
				}
			}
			else
			{
				if(
				//
				( mIsLayoutRtl )
				//
				&& ( mIsWorkspaceLoop )
				//
				&& ( mIsOverScroll )
				//
				)
				{
					mFavoritesPageScrollProgress = 1 - mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithFavoritesPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
					mTranslationXForItemsScrollWithMediaPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
				}
				else
				{
					mFavoritesPageScrollProgress = mLeftPageScrollProgress;
					mTranslationXForItemsScrollWithFavoritesPage = ( 1 - mLeftPageScrollProgress ) * mPageWidth;
					mTranslationXForItemsScrollWithMediaPage = ( 0 - mLeftPageScrollProgress ) * mPageWidth;
				}
			}
			//ItemsScrollWithFavoritesPage
			////FavoritesPage(BackgroundAlpha)
			setBackgroundAlpha( mFavoritesPageScrollProgress * 0.8f );
			////SearchDropTargetBar
			if( isFavoritesPageShowSearch() )
			{
				SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
				if( mSearchDropTargetBar != null )
				{
					mSearchDropTargetBar.setTranslationX( mTranslationXForItemsScrollWithFavoritesPage );
				}
			}
			//ItemsScrollWithMediaPage
			////Hotseat
			Hotseat mHotseat = mLauncher.getHotseat();
			if( mHotseat != null )
			{
				mHotseat.setTranslationX( mTranslationXForItemsScrollWithMediaPage );
			}
			////PageIndicator
			PageIndicator mPageIndicator = getPageIndicator();
			if( mPageIndicator != null )
			{
				mPageIndicator.setTranslationX( mTranslationXForItemsScrollWithMediaPage );
			}
			//NotifyFavoritesPage
			if( mFavoritesPageCallbacks != null )
			{
				mFavoritesPageCallbacks.onScrollProgressChanged( mFavoritesPageScrollProgress );
			}
		}
		else if(
		//
		( mIsFavoritesPageScroll == false )
		//
		&& ( mIsMusicPageScroll == false )
		//
		&& ( mIsCameraPageScroll == false )
		//
		)
		{
			/*
			//普+普（添加保护，确保以下几个模块显示正常）
			//	页面的背景透明度
			//		——0
			//	搜索栏
			//		普不显示搜索栏时
			//			——不处理
			//		普显示搜索栏时
			//			——0
			//	页面指示器
			//		——0
			//	底边栏
			//		——0
			*/
			//ItemsToReset
			////NormalPage(BackgroundAlpha)
			setBackgroundAlpha( 0 * 0.8f );
			////SearchDropTargetBar
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
			{
				SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
				if( mSearchDropTargetBar != null )
				{
					mSearchDropTargetBar.setTranslationX( 0 );
				}
			}
			////Hotseat
			Hotseat mHotseat = mLauncher.getHotseat();
			if( mHotseat != null )
			{
				mHotseat.setTranslationX( 0 );
			}
			////PageIndicator
			PageIndicator mPageIndicator = getPageIndicator();
			if( mPageIndicator != null )
			{
				mPageIndicator.setTranslationX( 0 );
			}
		}
		//fulijuan add start	//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）   //当广告显示时，使得搜索栏显示在广告下面
		if( mIsFavoritesPageScroll )
		{
			mLauncher.bringWorkspaceToFront();
		}
		//fulijuan add end
	}
	
	private void moveToFirstNormalPageIfNecessaryWhenDragStart()
	{
		int mCurPage = getCurrentPage();
		int mFirstNormalPageIndex = -1;
		for( int i = mCurPage ; i < getChildCount() ; i++ )
		{
			CellLayout mChild = (CellLayout)getChildAt( i );
			if( mChild.isFunctionPage() == false )
			{
				mFirstNormalPageIndex = i;
				break;
			}
		}
		if( mCurPage < mFirstNormalPageIndex )
		{
			//chenliang add start	//解决“相机页配置在普通页左边的前提下，在相机页打开照相机后，长按并托起底边栏图标（这时桌面切到到第一个普通页面），然后再次进入相机页，这时拍照界面没有关闭”的问题。【i_0015003】
			if( isCameraPage( mCurPage ) )
			{
				CameraView.getInstance().stopCamera();
			}
			//chenliang add end
			setCurrentPage( mFirstNormalPageIndex );
			//xiatian add start	//解决“当在媒体页长按图标时，切页到第一个普通页面后，看到搜索栏【快速】消失”的问题。
			//【问题原因】
			//	1、在媒体页长按图标后的逻辑：自动由媒体页切页到第一个普通页
			//	2、触发长按消息时，在DragController的startDrag方法中，会调用每个DragListener的onDragStart方法
			//	3、SearchDropTargetBar的onDragStart之前中触发了搜索栏的隐藏动画
			//	4、虽然Workspace的onDragStart在SearchDropTargetBar的onDragStart之前，但是由媒体页切页到第一个普通页面的操作在Workspace的onDragStart的Runnable中，所以搜索栏隐藏在前，桌面切页在后
			//	5、由于从媒体页切页到第一个普通页面的过程也要耗时（这时搜索栏隐藏的动画已经做了一部分），故切页到第一普通页面后，会看到搜索栏【快速】消失
			//【解决方案】
			//	如果是在媒体页长按图标的话，切页到第一个普通页面后，重新播放搜索栏隐藏的动画（1、停止正在播放的搜索栏隐藏动画并复位；2、重新再来一遍搜索栏消失动画）。
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
			{
				SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
				if( mSearchDropTargetBar != null )
				{
					mSearchDropTargetBar.forceHideSearchBarWithAnim();
				}
			}
			//xiatian add end
		}
	}
	
	public int getFunctionPagesInNormalPageLeftNum()
	{
		int mFunctionPagesInNormalPageLeftNum = 0;
		if(
		//
		hasFavoritesPage()
		//
		&& ( LauncherDefaultConfig.getFavoritesPagePosition() < 0 )
		//
		)
		{
			mFunctionPagesInNormalPageLeftNum++;
		}
		if(
		//
		hasCameraPage()
		//
		&& ( LauncherDefaultConfig.getCameraPagePosition() < 0 )
		//
		)
		{
			mFunctionPagesInNormalPageLeftNum++;
		}
		if(
		//
		hasMusicPage()
		//
		&& ( LauncherDefaultConfig.getMusicPagePosition() < 0 )
		//
		)
		{
			mFunctionPagesInNormalPageLeftNum++;
		}
		return mFunctionPagesInNormalPageLeftNum;
	}
	
	public void updateWorkspaceItemsStateOnEndMovingInNormalMode()
	{
		if( isInNormalMode() == false )
		{
			return;
		}
		int mPageWidth = getViewportWidth();
		int mFavoritesPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_FAVORITES_PAGE_SCREEN_ID );
		int mCameraPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_CAMERA_PAGE_SCREEN_ID );
		int mMusicPageIndexInViewGroup = mScreenOrder.indexOf( FUNCTION_MUSIC_PAGE_SCREEN_ID );
		if( mCurrentPage == mFavoritesPageIndexInViewGroup )
		{
			/*
			//酷（添加保护，确保以下几个模块显示正常）
			//	页面的背景透明度
			//		——酷的透明度
			//	搜索栏
			//		酷不显示搜索栏时
			//			——屏幕外
			//		酷显示搜索栏时
			//			——0
			//	页面指示器
			//		——屏幕外
			//	底边栏
			//		——屏幕外
			*/
			//ItemsToReset
			////FavoritesPage(BackgroundAlpha)
			setBackgroundAlpha( 0.8f );
			////SearchDropTargetBar
			SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
			if( mSearchDropTargetBar != null )
			{
				if( !isFavoritesPageShowSearch() )
				{
					mSearchDropTargetBar.setTranslationX( mPageWidth );
				}
				else
				{
					mSearchDropTargetBar.setTranslationX( 0 );
				}
			}
			////Hotseat
			Hotseat mHotseat = mLauncher.getHotseat();
			if( mHotseat != null )
			{
				mHotseat.setTranslationX( mPageWidth );
			}
			////PageIndicator
			PageIndicator mPageIndicator = getPageIndicator();
			if( mPageIndicator != null )
			{
				mPageIndicator.setTranslationX( mPageWidth );
			}
		}
		else if(
		//
		( mCurrentPage == mCameraPageIndexInViewGroup )
		//
		|| ( mCurrentPage == mMusicPageIndexInViewGroup )
		//
		)
		{
			/*
			//媒（添加保护，确保以下几个模块显示正常）
			//	页面的背景透明度
			//		——非酷的透明度
			//	搜索栏
			//		——屏幕外
			//	页面指示器
			//		——0
			//	底边栏
			//		——0
			*/
			//ItemsToReset
			////NormalPage(BackgroundAlpha)
			setBackgroundAlpha( 0f );
			////SearchDropTargetBar
			SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
			if( mSearchDropTargetBar != null )
			{
				mSearchDropTargetBar.setTranslationX( mPageWidth );
			}
			////Hotseat
			Hotseat mHotseat = mLauncher.getHotseat();
			if( mHotseat != null )
			{
				mHotseat.setTranslationX( 0 );
			}
			////PageIndicator
			PageIndicator mPageIndicator = getPageIndicator();
			if( mPageIndicator != null )
			{
				mPageIndicator.setTranslationX( 0 );
			}
		}
		else
		{
			/*
			//普（添加保护，确保以下几个模块显示正常）
			//	页面的背景透明度
			//		——非酷的透明度
			//	搜索栏
			//		普不显示搜索栏时
			//			——屏幕外
			//		普显示搜索栏时
			//			——0
			//	页面指示器
			//		——0
			//	底边栏
			//		——0
			*/
			//ItemsToReset
			////NormalPage(BackgroundAlpha)
			setBackgroundAlpha( 0f );
			////SearchDropTargetBar
			SearchDropTargetBar mSearchDropTargetBar = mLauncher.getSearchDropTargetBar();
			if( mSearchDropTargetBar != null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
				{
					mSearchDropTargetBar.setTranslationX( 0 );
				}
				else
				{
					mSearchDropTargetBar.setTranslationX( mPageWidth );
				}
			}
			////Hotseat
			Hotseat mHotseat = mLauncher.getHotseat();
			if( mHotseat != null )
			{
				mHotseat.setTranslationX( 0 );
			}
			////PageIndicator
			PageIndicator mPageIndicator = getPageIndicator();
			if( mPageIndicator != null )
			{
				mPageIndicator.setTranslationX( 0 );
			}
		}
	}
	//xiatian add end
	;
	
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public boolean workspaceInModalState()
	{
		return mState != State.NORMAL;
	}
	
	// Direction used for moving the workspace and hotseat UI
	public enum Direction
	{
		X( TRANSLATION_X ) , Y( TRANSLATION_Y );
		
		private final Property<View , Float> viewProperty;
		
		Direction(
				Property<View , Float> viewProperty )
		{
			this.viewProperty = viewProperty;
		}
	}
	
	/**
	* Moves the Hotseat UI in the provided direction.
	* @param direction the direction to move the workspace
	* @param translation the amount of shift.
	* @param alpha the alpha for the hotseat page
	*/
	public void setHotseatTranslationAndAlpha(
			Direction direction ,
			float translation ,
			float alpha )
	{
		Property<View , Float> property = direction.viewProperty;
		// Skip the page indicator movement in the vertical bar layout
		if( getPageIndicator() != null )
			property.set( (View)getPageIndicator().getParent() , translation );
		property.set( mLauncher.getHotseat() , translation );
		setHotseatAlphaAtIndex( alpha , direction.ordinal() );
	}
	
	private void setHotseatAlphaAtIndex(
			float alpha ,
			int index )
	{
		mHotseatAlpha[index] = alpha;
		final float hotseatAlpha = mHotseatAlpha[0] * mHotseatAlpha[1] * mHotseatAlpha[2];
		final float pageIndicatorAlpha = mHotseatAlpha[0] * mHotseatAlpha[2];
		mLauncher.getHotseat().setAlpha( hotseatAlpha );
		//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_S8 )
		{
			getPageIndicator().setAlpha( hotseatAlpha );
		}
		else
		//zhujieping add end
		{
			getPageIndicator().setAlpha( pageIndicatorAlpha );
		}
	}
	
	public ValueAnimator createHotseatAlphaAnimator(
			float finalValue )
	{
		if( Float.compare( finalValue , mHotseatAlpha[HOTSEAT_STATE_ALPHA_INDEX] ) == 0 )
		{
			// Return a dummy animator to avoid null checks.
			return ValueAnimator.ofFloat( 0 , 0 );
		}
		else
		{
			ValueAnimator animator = ValueAnimator.ofFloat( mHotseatAlpha[HOTSEAT_STATE_ALPHA_INDEX] , finalValue );
			animator.addUpdateListener( new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator valueAnimator )
				{
					float value = (Float)valueAnimator.getAnimatedValue();
					setHotseatAlphaAtIndex( value , HOTSEAT_STATE_ALPHA_INDEX );
				}
			} );
			AccessibilityManager am = (AccessibilityManager)mLauncher.getSystemService( Context.ACCESSIBILITY_SERVICE );
			final boolean accessibilityEnabled = am.isEnabled();
			animator.addUpdateListener( new WorkspaceStateTransitionAnimation.AlphaUpdateListener( mLauncher.getHotseat() , accessibilityEnabled ) );
			animator.addUpdateListener( new WorkspaceStateTransitionAnimation.AlphaUpdateListener( getPageIndicator() , accessibilityEnabled ) );
			return animator;
		}
	}
	//zhujieping del start //4.4进入主菜单的动画，桌面的springloadedtranslationY为0,7.0进入主菜单动画跟4.4保持一致
	//	public float getSpringLoadedTranslationY()
	//	{
	//		DeviceProfile grid = mLauncher.getDeviceProfile();
	//		if( getChildCount() == 0 )
	//		{
	//			return 0;
	//		}
	//		float scaledHeight = grid.getWorkspaceSpringLoadShrinkFactor() * getNormalChildHeight();
	//		float shrunkTop = mInsets.top + grid.getDropBarSpaceHeightPx();
	//		float shrunkBottom = getViewportHeight() - mInsets.bottom - grid.getWorkspacePadding( CellLayout.PORTRAIT ).bottom - grid.getWorkspaceSpringLoadedBottomSpace();
	//		float totalShrunkSpace = shrunkBottom - shrunkTop;
	//		float desiredCellTop = shrunkTop + ( totalShrunkSpace - scaledHeight ) / 2;
	//		float halfHeight = getHeight() / 2;
	//		float myCenter = getTop() + halfHeight;
	//		float cellTopFromCenter = halfHeight - getChildAt( 0 ).getTop();
	//		float actualCellTop = myCenter - cellTopFromCenter * grid.getWorkspaceSpringLoadShrinkFactor();
	//		return ( desiredCellTop - actualCellTop ) / grid.getWorkspaceSpringLoadShrinkFactor() + LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_workspaceSpringLoadShrink_offset );
	//	}
	//zhujieping add end
	
	public void setWorkspaceYTranslationAndAlpha(
			float translation ,
			float alpha )
	{
		setWorkspaceAndSearchBarTranslationAndAlpha( Direction.Y , translation , alpha );
	}
	
	/**
	 * Moves the workspace UI in the provided direction.
	 * @param direction the direction to move the workspace
	 * @param translation the amount of shift.
	 * @param alpha the alpha for the workspace page
	 */
	private void setWorkspaceAndSearchBarTranslationAndAlpha(
			Direction direction ,
			float translation ,
			float alpha )
	{
		float[] mPageAlpha = new float[]{ 1 , 1 };
		Property<View , Float> property = direction.viewProperty;
		mPageAlpha[direction.ordinal()] = alpha;
		float finalAlpha = mPageAlpha[0] * mPageAlpha[1];
		View currentChild = getChildAt( getCurrentPage() );
		//zhujieping add start //从主菜单往下滑一点，回到主菜单时，闪现桌面图标
		if( Float.compare( finalAlpha , 0 ) == 0 )
		{
			//zhujieping start //设置celllayout的visibility，会开启一个alpha变化的动画（原因未知），动画被打断，导致celllayout半透，这里改为设置workspace的visibility【i_0015058】
			//zhujieping del start
			//			for( int i = getChildCount() - 1 ; i >= 0 ; i-- )
			//			{
			//				View child = getChildAt( i );
			//				child.setVisibility( View.INVISIBLE );
			//			}
			//zhujieping del end
			setVisibility( View.INVISIBLE );
			//zhujiieping end
		}
		else
		{
			//zhujieping start //设置celllayout的visibility，会开启一个alpha变化的动画（原因未知），动画被打断，导致celllayout半透，这里改为设置workspace的visibility【i_0015058】
			//zhujieping del start
			//			for( int i = getChildCount() - 1 ; i >= 0 ; i-- )
			//			{
			//				View child = getChildAt( i );
			//				if( child.getVisibility() != View.VISIBLE )
			//					child.setVisibility( View.VISIBLE );
			//			}
			//zhujieping del end
			//zhujieping add start
			if( getVisibility() != View.VISIBLE )
			{
				setVisibility( View.VISIBLE );
			}
			//zhujieping add end
			//zhujiieping end
		}
		//zhujieping add end
		if( currentChild != null && currentChild instanceof CellLayout )
		{
			property.set( currentChild , translation );
			( (CellLayout)currentChild ).setCellLayoutChildAlpha( finalAlpha );
		}
		// When the animation finishes, reset all pages, just in case we missed a page.
		if( Float.compare( translation , 0 ) == 0 )
		{
			for( int i = getChildCount() - 1 ; i >= 0 ; i-- )
			{
				if( i != getCurrentPage() )
				{
					View child = getChildAt( i );
					if( child instanceof CellLayout )
					{
						property.set( child , translation );
						( (CellLayout)child ).setCellLayoutChildAlpha( finalAlpha );
					}
				}
			}
		}
		mLauncher.getSearchDropTargetBar().setTranslationY( translation );
		mLauncher.getSearchDropTargetBar().setAlpha( finalAlpha );
	}
	
	/**
	* Sets the current workspace {@link State}, returning an animation transitioning the workspace
	* to that new state.
	*/
	public Animator setStateWithAnimation(
			State toState ,
			boolean animated ,
			HashMap<View , Integer> layerViews )
	{
		// Create the animation to the new state
		AnimatorSet workspaceAnim = mStateTransitionAnimation.getAnimationToState( mState , toState , animated , layerViews );
		//		boolean shouldNotifyWidgetChange = !mState.shouldUpdateWidget && toState.shouldUpdateWidget;
		// Update the current state
		mState = toState;
		//		updateAccessibilityFlags();
		//		if( shouldNotifyWidgetChange )
		//		{
		//			mLauncher.notifyWidgetProvidersChanged();
		//		}
		//		if( mOnStateChangeListener != null )
		//		{
		//			mOnStateChangeListener.prepareStateChange( toState , animated ? workspaceAnim : null );
		//		}
		return workspaceAnim;
	}
	
	public WorkspaceStateTransitionAnimation getStateTransitionAnimation()
	{
		return mStateTransitionAnimation;
	}
	
	public void snapToPageFromOverView(
			int whichPage )
	{
		mStateTransitionAnimation.snapToPageFromOverView( whichPage );
	}
	
	public void stopCurrentPageAnimation()
	{
		if( isMusicPage( mCurrentPage ) )
		{
			MusicView.getInstance().onPageMoveOut();//音乐页是在moveout中停止动画的
		}
		if( isCameraPage( mCurrentPage ) )
		{
			CameraView.getInstance().stopCamera();
			CameraView.getInstance().hideDeletePop();
		}
	}
	
	public void startCurrentPageAnimation()
	{
		if( isMusicPage( mCurrentPage ) )
		{
			MusicView.getInstance().onPageMoveIn();
		}
	}
	
	//zhujieping add end
	@Override
	public void stopEffecf()
	{
		super.stopEffecf();
		if( mCuboidEffect != null )
		{
			mCuboidEffect.stopEffecf();
		}
	}
	
	//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	private CellLayout mExtraAddPageScreen = null;
	private DefaultDialog mDeletePageDialog = null;
	private CellLayout toDelete = null;
	
	public void addExtraAddPageScreen()
	{
		if( !canInsertNewScreen() )
		{
			return;
		}
		if( mExtraAddPageScreen == null )
		{
			mExtraAddPageScreen = (CellLayout)mLauncher.getLayoutInflater().inflate( R.layout.workspace_screen , null );
			mExtraAddPageScreen.setOnLongClickListener( mLongClickListener );
			mExtraAddPageScreen.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					addNewEmptyScreen();
				}
			} );
			mExtraAddPageScreen.setSoundEffectsEnabled( false );
			View add = LayoutInflater.from( getContext() ).inflate( R.layout.workspace_add_page_screen , null );
			add.setOnClickListener( null );
			mExtraAddPageScreen.addView( add );
		}
		if( indexOfChild( mExtraAddPageScreen ) == -1 )
		{
			mWorkspaceScreens.put( EXTRA_ADD_PAGE_SCREEN_ID , mExtraAddPageScreen );
			int insertIndex = getInsertNewPageIndex();
			mScreenOrder.add( insertIndex , EXTRA_ADD_PAGE_SCREEN_ID );
			addView( mExtraAddPageScreen , insertIndex );
			if( isInOverviewMode() )
			{
				mExtraAddPageScreen.setBackgroundAlpha( 1.0f );
			}
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(
			MotionEvent ev )
	{
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent( ev );
	}
	
	public void removeExtraAddPageScreen()
	{
		CellLayout remove = mExtraAddPageScreen;
		if( remove == null || indexOfChild( remove ) == -1 )
		{
			return;
		}
		removeView( remove );
		mWorkspaceScreens.remove( remove );
		mScreenOrder.remove( EXTRA_ADD_PAGE_SCREEN_ID );
	}
	
	public void addNewEmptyScreen()
	{
		if( LauncherDefaultConfig.isAllowEmptyScreen() )
		{
			long newId = LauncherAppState.getLauncherProvider().generateNewScreenId();
			int insertIndex = getNormalPageCount() + getFunctionPagesInNormalPageLeftNum();
			insertNewWorkspaceScreen( newId , insertIndex );
			setCurrentPage( insertIndex );
			mLauncher.getModel().updateWorkspaceScreenOrder( mLauncher , mScreenOrder );
			if( !canInsertNewScreen() )
			{
				removeExtraAddPageScreen();
			}
		}
	}
	
	protected boolean handleDeleteDragView(
			View dragview )
	{
		if( getNormalPageCount() <= 1 )
		{
			Toast.makeText( getContext() , R.string.can_not_delete_page , Toast.LENGTH_SHORT ).show();
			return false;
		}
		if( dragview != null && dragview instanceof CellLayout )
		{
			int count = ( (CellLayout)dragview ).getShortcutsAndWidgets().getChildCount();
			if( count == 0 )
			{
				removeCellLayout( (CellLayout)dragview );
				return true;
			}
			else
			{
				//zhujieping add start //添加配置项“config_empty_screen_id_in_core”，单层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
				{
					Toast.makeText( mLauncher , R.string.can_not_delete_not_empty_page , Toast.LENGTH_SHORT ).show();
				}
				else
				//zhujieping add end
				{
					toDelete = (CellLayout)dragview;
					showDeletePageDialog();
				}
			}
		}
		return false;
	}
	
	public void removeCellLayout(
			CellLayout cellLayout )
	{
		ShortcutAndWidgetContainer shortcutAndWidgetContainer = cellLayout.getShortcutsAndWidgets();
		int count = shortcutAndWidgetContainer.getChildCount();
		for( int index = 0 ; index < count ; index++ )
		{
			View child = shortcutAndWidgetContainer.getChildAt( index );
			if( child.getTag() != null && child.getTag() instanceof ItemInfo )
			{
				LauncherModel.deleteItemFromDatabase( getContext() , (ItemInfo)child.getTag() );
			}
		}
		cellLayout.removeAllViews();
		long id = getIdForScreen( cellLayout );
		mWorkspaceScreens.remove( id );
		mScreenOrder.remove( id );
		disableLayoutTransitions();
		removeView( cellLayout );
		enableLayoutTransitions();
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE && isInOverviewMode() )
		{
			int index = mScreenOrder.indexOf( id );
			if( getDefaultPageIndex() == ( index - getFunctionPagesInNormalPageLeftNum() ) )//删除的当前页时默认页
			{
				if( getDefaultPageIndex() > getNormalPageCount() - 1 )
				{
					index = getNormalPageCount() - 1;
				}
				( (CellLayout)getChildAt( index ) ).setOverViewHomePage();
			}
		}
		LauncherAppState.getInstance().getModel().updateWorkspaceScreenOrder( getContext() , mScreenOrder );
		if( LauncherDefaultConfig.isAllowEmptyScreen() && mScreenOrder.indexOf( EXTRA_ADD_PAGE_SCREEN_ID ) == -1 )
		{
			if( canInsertNewScreen() )
			{
				addExtraAddPageScreen();
			}
		}
	}
	
	public void showDeletePageDialog()
	{
		if( mDeletePageDialog == null )
		{
			mDeletePageDialog = new DefaultDialog( mLauncher );
			mDeletePageDialog.setTitle( R.string.delete_page );
			mDeletePageDialog.setContentText( R.string.delete_page_confirm_message );
			mDeletePageDialog.setPositiveButtonText( R.string.positive );
			mDeletePageDialog.setNegativeButtonText( R.string.negative );
			mDeletePageDialog.setOnClickListener( new DefaultDialog.OnClickListener() {
				
				@Override
				public void onClickPositive(
						View v )
				{
					mDeletePageDialog.dismiss();
					if( toDelete != null )
					{
						removeCellLayout( toDelete );
						toDelete = null;
					}
				}
				
				@Override
				public void onClickNegative(
						View v )
				{
					toDelete = null;
				}
				
				@Override
				public void onClickExit(
						View v )
				{
					toDelete = null;
				}
			} );
		}
		mDeletePageDialog.show();
	}
	
	@Override
	public boolean allowLongPress(
			View v )
	{
		// TODO Auto-generated method stubo
		if( mExtraAddPageScreen == v )
		{
			return false;
		}
		return super.allowLongPress( v );
	}
	
	public int getFunctionPagesCount()
	{
		int mFunctionPagesNum = 0;
		if( hasFavoritesPage() )
		{
			mFunctionPagesNum++;
		}
		if( hasCameraPage() )
		{
			mFunctionPagesNum++;
		}
		if( hasMusicPage() )
		{
			mFunctionPagesNum++;
		}
		return mFunctionPagesNum;
	}
	
	public int getNormalPageCount()
	{
		int addPage = indexOfChild( mExtraAddPageScreen ) == -1 ? 0 : 1;
		return mScreenOrder.size() - getFunctionPagesCount() - addPage;//不用getchildcount是防止媒体页被移除掉
	}
	
	protected boolean isChildCanRecording(
			int index )
	{
		if( LauncherDefaultConfig.isAllowEmptyScreen() && indexOfChild( mExtraAddPageScreen ) == index )
		{
			return false;
		}
		return true;
	}
	//zhujieping add end
	//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	public boolean isOverviewAnimRunning()
	{
		if( overviewAnim != null && overviewAnim.isRunning() )
		{
			return true;
		}
		return false;
	}
	//zhujieping add end
}
