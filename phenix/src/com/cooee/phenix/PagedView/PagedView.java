package com.cooee.phenix.PagedView;


import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.DragCellLayoutListener;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Workspace;
import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.effects.EffectFactory;
import com.cooee.phenix.effects.EffectInfo;
import com.cooee.phenix.effects.IEffect;
import com.cooee.phenix.pageIndicators.PageIndicator;
import com.cooee.phenix.pageIndicators.PageMarkerResources;


/**
 * An abstraction of the original Workspace which supports browsing through a
 * sequential list of "pages"
 */
public abstract class PagedView extends ViewGroup implements ViewGroup.OnHierarchyChangeListener
{
	
	private static final String TAG = "PagedView";
	private static final boolean DEBUG = false;
	protected static final int INVALID_PAGE = -1;
	// the min drag distance for a fling to register, to prevent random page shifts
	private static final int MIN_LENGTH_FOR_FLING = 25;
	public static int PAGE_SNAP_ANIMATION_DURATION = 350;
	protected static final int SLOW_PAGE_SNAP_ANIMATION_DURATION = 950;
	protected static final float NANOTIME_DIV = 1000000000.0f;
	private static final float OVERSCROLL_ACCELERATE_FACTOR = 2;
	private static final float OVERSCROLL_DAMP_FACTOR = 0.14f;
	private static final float RETURN_TO_ORIGINAL_PAGE_THRESHOLD = 0.33f;
	// The page is moved more than halfway, automatically move to the next page on touch up.
	protected static float SIGNIFICANT_MOVE_THRESHOLD = 0.4f;
	// The following constants need to be scaled based on density. The scaled versions will be
	// assigned to the corresponding member variables below.
	// zhangjin@2015/07/21 UPD START
	//private static final int FLING_THRESHOLD_VELOCITY = 500;
	private static final int FLING_THRESHOLD_VELOCITY = 100;
	// zhangjin@2015/07/21 UPD END
	private static final int MIN_SNAP_VELOCITY = 1500;
	// zhangjin@2015/07/21 UPD START
	//private static final int MIN_FLING_VELOCITY = 250;
	private static final int MIN_FLING_VELOCITY = 400;
	// zhangjin@2015/07/21 UPD END
	// zhangjin@2015/09/14 ADD START
	public static float PAGE_ON_TOUCH_ACCELERATE = 1.4f;
	// zhangjin@2015/09/14 ADD END	
	// zhangjin@2015/09/16 ADD START
	public static int MAX_TOUCH_SLOP = 10;
	// zhangjin@2015/09/16 ADD END
	// We are disabling touch interaction of the widget region for factory ROM.
	private static final boolean DISABLE_TOUCH_INTERACTION = false;
	private static final boolean DISABLE_TOUCH_SIDE_PAGES = true;
	public static final int INVALID_RESTORE_PAGE = -1001;
	/**
	 * mFreeScroll 两种状态：1：在编辑模式时候，up松手以后不会继续滑动；2：在非编辑模式时候，up松手以后可以继续滑动
	 */
	private boolean mFreeScroll = false;
	private int mFreeScrollMinScrollX = -1;
	private int mFreeScrollMaxScrollX = -1;
	static final int AUTOMATIC_PAGE_SPACING = -1;
	protected int mFlingThresholdVelocity;
	protected int mMinFlingVelocity;
	protected int mMinSnapVelocity;
	protected float mDensity;
	protected float mSmoothingTime;
	protected float mTouchX;
	protected boolean mFirstLayout = true;
	private int mNormalChildHeight = -1;
	protected int mCurrentPage;
	protected int mRestorePage = INVALID_RESTORE_PAGE;
	protected int mChildCountOnLastLayout;
	protected int mNextPage = INVALID_PAGE;
	protected int mMaxScrollX;
	protected Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private float mParentDownMotionX;
	private float mParentDownMotionY;
	private float mDownMotionX;
	private float mDownMotionY;
	private float mDownScrollX;
	private float mDragViewBaselineLeft;
	protected float mLastMotionX;
	// zhangjin@2015/09/15 ADD START
	protected float mLastMoveX;
	// zhangjin@2015/09/15 ADD END
	protected float mLastMotionXRemainder;
	protected float mLastMotionY;
	protected float mTotalMotionX;
	private boolean mCancelTap;
	private int[] mPageScrolls;
	protected final static int TOUCH_STATE_REST = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;
	protected final static int TOUCH_STATE_PREV_PAGE = 2;
	protected final static int TOUCH_STATE_NEXT_PAGE = 3;
	protected final static int TOUCH_STATE_REORDERING = 4;
	protected final static float ALPHA_QUANTIZE_LEVEL = 0.0001f;
	protected int mTouchState = TOUCH_STATE_REST;
	protected boolean mForceScreenScrolled = false;
	protected OnLongClickListener mLongClickListener;
	protected int mTouchSlop;
	private int mPagingTouchSlop;
	private int mMaximumVelocity;
	protected int mPageSpacing;
	protected int mPageLayoutPaddingTop;
	protected int mPageLayoutPaddingBottom;
	protected int mPageLayoutPaddingLeft;
	protected int mPageLayoutPaddingRight;
	protected int mPageLayoutWidthGap;
	protected int mPageLayoutHeightGap;
	protected int mCellCountX = 0;
	protected int mCellCountY = 0;
	protected boolean mCenterPagesVertically;
	protected boolean mAllowOverScroll = true;
	protected int mUnboundedScrollX;
	protected int[] mTempVisiblePagesRange = new int[2];
	protected boolean mForceDrawAllChildrenNextFrame;
	// mOverScrollX is equal to getScrollX() when we're within the normal scroll range. Otherwise
	// it is equal to the scaled overscroll position. We use a separate value so as to prevent
	// the screens from continuing to translate beyond the normal bounds.
	protected int mOverScrollX;
	protected static final int INVALID_POINTER = -1;
	protected int mActivePointerId = INVALID_POINTER;
	private PageSwitchListener mPageSwitchListener;
	protected ArrayList<Boolean> mDirtyPageContent;
	// If true, syncPages and syncPageItems will be called to refresh pages
	protected boolean mContentIsRefreshable = true;
	// If true, modify alpha of neighboring pages as user scrolls left/right
	protected boolean mFadeInAdjacentScreens = false;
	// It true, use a different slop parameter (pagingTouchSlop = 2 * touchSlop) for deciding
	// to switch to a new page
	protected boolean mUsePagingTouchSlop = true;
	// If true, the subclass should directly update scrollX itself in its computeScroll method
	// (SmoothPagedView does this)
	protected boolean mDeferScrollUpdate = false;
	protected boolean mDeferLoadAssociatedPagesUntilScrollCompletes = false;
	protected boolean mIsPageMoving = false;
	// All syncs and layout passes are deferred until data is ready.
	protected boolean mIsDataReady = false;
	protected boolean mAllowLongPress = true;
	// Page Indicator
	private int mPageIndicatorViewId;
	private PageIndicator mPageIndicator;
	private boolean mAllowPagedViewAnimations = true;
	// The viewport whether the pages are to be contained (the actual view may be larger than the
	// viewport)
	private Rect mViewport = new Rect();
	// Reordering
	// We use the min scale to determine how much to expand the actually PagedView measured
	// dimensions such that when we are zoomed out, the view is not clipped
	private int REORDERING_DROP_REPOSITION_DURATION = 200;
	protected int REORDERING_REORDER_REPOSITION_DURATION = 300;
	protected int REORDERING_ZOOM_IN_OUT_DURATION = 250;
	private int REORDERING_SIDE_PAGE_HOVER_TIMEOUT = 80;
	private float mMinScale = 1f;
	private boolean mUseMinScale = false;
	protected View mDragView;
	protected AnimatorSet mZoomInOutAnim;
	private Runnable mSidePageHoverRunnable;
	private int mSidePageHoverIndex = -1;
	// This variable's scope is only for the duration of startReordering() and endReordering()
	private boolean mReorderingStarted = false;
	// This variable's scope is for the duration of startReordering() and after the zoomIn()
	// animation after endReordering()
	private boolean mIsReordering;
	// The runnable that settles the page after snapToPage and animateDragViewToOriginalPosition
	private int NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT = 2;
	private int mPostReorderingPreZoomInRemainingAnimationCount;
	private Runnable mPostReorderingPreZoomInRunnable;
	// Convenience/caching
	private Matrix mTmpInvMatrix = new Matrix();
	private float[] mTmpPoint = new float[2];
	private int[] mTmpIntPoint = new int[2];
	private Rect mTmpRect = new Rect();
	private Rect mAltTmpRect = new Rect();
	// Bouncer
	private boolean mTopAlignPageWhenShrinkingForBouncer = false;
	protected final Rect mInsets = new Rect();
	protected int mFirstChildLeft;
	/***************************************/
	protected int mLastScreenCenter = -1;
	protected boolean isComputeWallpaperOffset;
	protected boolean isMoving;
	protected int widthScreen;
	protected int halfWidthScreen;
	protected int xCenterLastPage;
	protected int xCenterFirstPage;
	protected int xCurPageCenter;
	protected boolean isRtl;
	protected IEffect curView;
	protected IEffect nextView;
	protected int pageWidth;
	protected int pageHeight;
	private Launcher mLauncher;
	private int mMinimumWidth;
	protected float mLayoutScale = 1.0f;
	protected boolean isSuccessCutPage = false; //切页是否成功
	protected EffectInfo mCurentAnimInfo;
	private boolean overviewModeDrawDragViewInvalidate = false;//cheyingkun add	//解决“编辑模式下，长按一页进行拖动不切页，拖动过程中出现黑色竖线。”的问题【i_0010657】
	// zhangjin@2015/07/24 UPD START
	//protected static float CAMERA_DISTANCE = 2500;
	protected static float CAMERA_DISTANCE = 2000;
	// zhangjin@2015/07/24 UPD END
	private boolean mStartEffectEnd = true;//切页特效：判断是否正在执行特效,wanghongjian add
	protected boolean mIsFirstOrLastEffect = true;//切页特效：在第一页或者最后一页是否执行特效 wanghongjian add
	//<i_0010089> liuhailin@2015-04-02 modify begin
	//标记是否是删除空白页面
	protected boolean isStripEmptyScreens = false;
	//<i_0010089> liuhailin@2015-04-02 modify end
	private boolean isLoop = false;// zhujieping ADD ,是否支持循环滚动滑页
	// zhangjin@2015/08/25 ADD START
	private EffectFactory mEffectFactory = null;
	// zhangjin@2015/08/25 ADD END
	/**workspace处于编辑模式?*/
	protected boolean isOverViewModel = false;//cheyingkun add	//phenix仿S5效果,编辑模式页面指示器
	private DragCellLayoutListener mDragCellLayoutListener = null;//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	protected boolean ismStartEffectEnd()
	{
		return mStartEffectEnd;
	}
	
	protected void setStartEffectEnd(
			boolean isEnd )
	{
		mStartEffectEnd = isEnd;
	}
	
	public boolean getFreeScroll()
	{
		return mFreeScroll //
				|| isOverViewModel;//cheyingkun add	//phenix仿S5效果,编辑模式页面指示器
	}
	
	public interface PageSwitchListener
	{
		
		void onPageSwitch(
				View newPage ,
				int newPageIndex );
	}
	
	public PagedView(
			Context context )
	{
		this( context , null );
	}
	
	public PagedView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public PagedView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.PagedView , defStyle , 0 );
		setPageSpacing( a.getDimensionPixelSize( R.styleable.PagedView_pageSpacing , 0 ) );
		mPageLayoutPaddingTop = a.getDimensionPixelSize( R.styleable.PagedView_pageLayoutPaddingTop , 0 );
		mPageLayoutPaddingBottom = a.getDimensionPixelSize( R.styleable.PagedView_pageLayoutPaddingBottom , 0 );
		mPageLayoutPaddingLeft = a.getDimensionPixelSize( R.styleable.PagedView_pageLayoutPaddingLeft , 0 );
		mPageLayoutPaddingRight = a.getDimensionPixelSize( R.styleable.PagedView_pageLayoutPaddingRight , 0 );
		mPageLayoutWidthGap = a.getDimensionPixelSize( R.styleable.PagedView_pageLayoutWidthGap , 0 );
		mPageLayoutHeightGap = a.getDimensionPixelSize( R.styleable.PagedView_pageLayoutHeightGap , 0 );
		mPageIndicatorViewId = a.getResourceId( R.styleable.PagedView_pageIndicator , -1 );
		mLauncher = (Launcher)context;
		a.recycle();
		setHapticFeedbackEnabled( false );
		init();
	}
	
	public void initAnimationStyle(
			PagedView pagedView )
	{
		int num = 0;
		//WangLei start //切页特效可配置
		//EffectFactory effectFactory = new EffectFactory( curView );//WangLei del
		// zhangjin@2015/08/25 UPD START
		//EffectFactory effectFactory = new EffectFactory( curView , mLauncher ); //WangLei add
		mEffectFactory = new EffectFactory( curView , mLauncher ); //WangLei add
		// zhangjin@2015/08/25 UPD END
		//WangLei end
		// 确保在更换切页特效的时候,将以前老的特效先置位wanghongjian@2015/04/01 UPD START
		if( mCurentAnimInfo != null )
		{
			mCurentAnimInfo.stopEffecf();
		}
		// wanghongjian@2015/04/01 UPD END
		if( pagedView instanceof AppsCustomizePagedView )
		{
			//WangLei start //切页特效可配置
			//WangLei del start
			//if( mLauncher.getSelect_efffects_workspace() == effectFactory.getAllEffects().size() )
			//{
			//	mCurentAnimInfo = effectFactory.getEffect( num = new Random().nextInt( effectFactory.getAllEffects().size() ) + 1 );
			//}
			//WangLei del end
			//WangLei add start
			//  WangLei start //桌面和主菜单特效的分离
			//  WangLei del start 
			//  if( effectFactory.isRandomEffect( mLauncher.getSelect_efffects_workspace() ) )
			//  {
			//      mCurentAnimInfo = effectFactory.getRandomEffect();
			//  }
			//WangLei add end
			//WangLei end
			//  else
			//  {
			//	   mCurentAnimInfo = effectFactory.getEffect( mLauncher.getSelect_efffects_workspace() + 1 );
			//  }
			//  WangLei del end
			//  WangLei add start
			//  更新主菜单的特效现在使用EffectFactory中AllApp相关的方法
			if( mEffectFactory.isAllAppRandomEffect( mLauncher.getSelect_effects_applist() ) )
			{
				mCurentAnimInfo = mEffectFactory.getAppRandomEffectInfo();
			}
			else
			{
				mCurentAnimInfo = mEffectFactory.getAppEffect( mLauncher.getSelect_effects_applist() + 1 );
			}
			//  WangLei add end
			//  WangLei end
		}
		else
		{
			if( mLauncher.getWorkspace() != null && mLauncher.getWorkspace().isInOverviewMode() )
			{
				// zhangjin@2015/07/28 i_0011941 UPD START
				//mCurentAnimInfo = effectFactory.getEffect( 1 );
				mCurentAnimInfo = mEffectFactory.getStandardEffect();
				// zhangjin@2015/07/28 UPD END
			}
			else
			{
				//WangLei start //切页特效可配置
				//WangLei del start
				//if( mLauncher.getSelect_efffects_workspace() == effectFactory.getAllEffects().size() )
				//{
				//	mCurentAnimInfo = effectFactory.getEffect( num = new Random().nextInt( effectFactory.getAllEffects().size() ) + 1 );
				//}
				//WangLei del end
				//WangLei add start
				if( mEffectFactory.isRandomEffect( mLauncher.getSelect_efffects_workspace() ) )
				{
					mCurentAnimInfo = mEffectFactory.getRandomEffect();
				}
				//WangLei add end
				//WangLei end
				else
				{
					mCurentAnimInfo = mEffectFactory.getEffect( mLauncher.getSelect_efffects_workspace() + 1 );
				}
			}
		}
		// zhujieping@2015/05/04 ADD START
		if( isLoop() )
		{
			mCurentAnimInfo.setMaxScroll( 1.0f );
			mCurentAnimInfo.setLoop( true );
		}
		else
		{
			mCurentAnimInfo.setMaxScroll( 0.5f );
			mCurentAnimInfo.setLoop( false );
		}
		// zhujieping@2015/05/04 ADD END
	}
	
	/**
	 * Initializes various states for this workspace.
	 */
	protected void init()
	{
		mDirtyPageContent = new ArrayList<Boolean>();
		mDirtyPageContent.ensureCapacity( 32 );
		mScroller = new Scroller( getContext() , new ScrollInterpolator() );
		mCurrentPage = 0;
		mCenterPagesVertically = true;
		final ViewConfiguration configuration = ViewConfiguration.get( getContext() );
		// zhangjin@2015/07/24 UPD START
		//		mTouchSlop = configuration.getScaledPagingTouchSlop();
		//		mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
		mTouchSlop = (int)( configuration.getScaledPagingTouchSlop() * 0.2 );
		mTouchSlop = mTouchSlop > MAX_TOUCH_SLOP ? MAX_TOUCH_SLOP : mTouchSlop;
		mPagingTouchSlop = (int)( configuration.getScaledPagingTouchSlop() * 0.2 );
		mPagingTouchSlop = mPagingTouchSlop > MAX_TOUCH_SLOP ? MAX_TOUCH_SLOP : mTouchSlop;
		// zhangjin@2015/07/24 UPD END
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mDensity = getResources().getDisplayMetrics().density;
		widthScreen = getResources().getDisplayMetrics().widthPixels;
		// Scale the fling-to-delete threshold by the density
		mFlingThresholdVelocity = (int)( FLING_THRESHOLD_VELOCITY * mDensity );
		mMinFlingVelocity = (int)( MIN_FLING_VELOCITY * mDensity );
		mMinSnapVelocity = (int)( MIN_SNAP_VELOCITY * mDensity );
		setOnHierarchyChangeListener( this );
		initAnimationStyle( this );
		overviewModeDrawDragViewInvalidate = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_OverviewMode_drawDragView_invalidate );//cheyingkun add	//解决“编辑模式下，长按一页进行拖动不切页，拖动过程中出现黑色竖线。”的问题【i_0010657】
		SIGNIFICANT_MOVE_THRESHOLD = LauncherDefaultConfig.getInt( R.integer.config_pagedview_scroll_space_threshold ) / 10f;
		PAGE_SNAP_ANIMATION_DURATION = LauncherDefaultConfig.getInt( R.integer.config_pagedview_snap_animation_duration );
		// zhangjin@2015/09/14 ADD START
		PAGE_ON_TOUCH_ACCELERATE = Float.parseFloat( LauncherDefaultConfig.getString( R.string.page_on_touch_accelerate ) );
		// zhangjin@2015/09/14 ADD END
	}
	
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		// Hook up the page indicator
		ViewGroup parent = (ViewGroup)getParent();
		if( mPageIndicator == null && mPageIndicatorViewId > -1 )
		{
			//zhujieping  start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
			//zhujieping del start
			// mPageIndicator = (PageIndicator)parent.findViewById( mPageIndicatorViewId );
			//zhujieping del end
			//zhujieping add start
			ViewGroup indicatorParent = (ViewGroup)parent.findViewById( mPageIndicatorViewId );
			View normal = indicatorParent.findViewById( R.id.pageIndicatorNormal );
			View caret = indicatorParent.findViewById( R.id.pageIndicatorCaret );
			if(
			//
			LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
			//
			&& LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_NOUGAT
			//
			&& !( this instanceof AppsCustomizePagedView )
			//
			)
			{
				mPageIndicator = (PageIndicator)caret;
				indicatorParent.removeView( normal );
			}
			else
			{
				mPageIndicator = (PageIndicator)normal;
				indicatorParent.removeView( caret );
			}
			//zhujieping  add end
			//zhujieping  end
			mPageIndicator.removeAllMarkers( mAllowPagedViewAnimations );
			ArrayList<PageMarkerResources> markers = new ArrayList<PageMarkerResources>();
			for( int i = 0 ; i < getChildCount() ; ++i )
			{
				markers.add( getPageIndicatorMarker( i ) );
			}
			mPageIndicator.addMarkers( markers , mAllowPagedViewAnimations );
			OnClickListener listener = getPageIndicatorClickListener();
			if( listener != null )
			{
				mPageIndicator.setOnClickListener( listener );
			}
		}
	}
	
	protected OnClickListener getPageIndicatorClickListener()
	{
		return null;
	}
	
	protected void onDetachedFromWindow()
	{
		// Unhook the page indicator
		mPageIndicator = null;
	}
	
	// Convenience methods to map points from self to parent and vice versa
	float[] mapPointFromViewToParent(
			View v ,
			float x ,
			float y )
	{
		mTmpPoint[0] = x;
		mTmpPoint[1] = y;
		v.getMatrix().mapPoints( mTmpPoint );
		mTmpPoint[0] += v.getLeft();
		mTmpPoint[1] += v.getTop();
		return mTmpPoint;
	}
	
	float[] mapPointFromParentToView(
			View v ,
			float x ,
			float y )
	{
		mTmpPoint[0] = x - v.getLeft();
		mTmpPoint[1] = y - v.getTop();
		v.getMatrix().invert( mTmpInvMatrix );
		mTmpInvMatrix.mapPoints( mTmpPoint );
		return mTmpPoint;
	}
	
	void updateDragViewTranslationDuringDrag()
	{
		if( mDragView != null )
		{
			float x = ( mLastMotionX - mDownMotionX ) + ( getScrollX() - mDownScrollX ) + ( mDragViewBaselineLeft - mDragView.getLeft() );
			float y = mLastMotionY - mDownMotionY;
			mDragView.setTranslationX( x );
			mDragView.setTranslationY( y );
			if( DEBUG )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "PagedView.updateDragViewTranslationDuringDrag(): " , x , ", " , y ) );
		}
	}
	
	public void setMinScale(
			float f )
	{
		mMinScale = f;
		mUseMinScale = true;
		requestLayout();
	}
	
	@Override
	public void setScaleX(
			float scaleX )
	{
		super.setScaleX( scaleX );
		if( isReordering( true ) )
		{
			float[] p = mapPointFromParentToView( this , mParentDownMotionX , mParentDownMotionY );
			mLastMotionX = p[0];
			mLastMotionY = p[1];
			updateDragViewTranslationDuringDrag();
		}
	}
	
	// Convenience methods to get the actual width/height of the PagedView (since it is measured
	// to be larger to account for the minimum possible scale)
	protected int getViewportWidth()
	{
		return mViewport.width();
	}
	
	protected int getViewportHeight()
	{
		return mViewport.height();
	}
	
	// Convenience methods to get the offset ASSUMING that we are centering the pages in the
	// PagedView both horizontally and vertically
	int getViewportOffsetX()
	{
		return ( getMeasuredWidth() - getViewportWidth() ) / 2;
	}
	
	int getViewportOffsetY()
	{
		return ( getMeasuredHeight() - getViewportHeight() ) / 2;
	}
	
	public PageIndicator getPageIndicator()
	{
		return mPageIndicator;
	}
	
	protected PageMarkerResources getPageIndicatorMarker(
			int pageIndex )
	{
		//fulijuan start	//页面指示器支持本地化
		//return new PageMarkerResources();//fulijuan del
		//fulijuan add start
		if( null != mPageIndicator )
		{
			return mPageIndicator.getDefaultPageMarkerResources();
		}
		return null;
		//fulijuan add end
		//fulijuan end
	}
	
	public void setPageSwitchListener(
			PageSwitchListener pageSwitchListener )
	{
		mPageSwitchListener = pageSwitchListener;
		if( mPageSwitchListener != null )
		{
			mPageSwitchListener.onPageSwitch( getPageAt( mCurrentPage ) , mCurrentPage );
		}
	}
	
	/**
	 * Note: this is a reimplementation of View.isLayoutRtl() since that is currently hidden api.
	 */
	public boolean isLayoutRtl()
	{
		//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
		//		return Tools.isLayoutRTL( this );//xiatian del
		return LauncherAppState.isLayoutRTL();//xiatian add 
		//xiatian end
	}
	
	/**
	 * Called by subclasses to mark that data is ready, and that we can begin loading and laying
	 * out pages.
	 */
	protected void setDataIsReady()
	{
		mIsDataReady = true;
	}
	
	protected boolean isDataReady()
	{
		return mIsDataReady;
	}
	
	/**
	 * Returns the index of the currently displayed page.
	 *
	 * @return The index of the currently displayed page.
	 */
	public int getCurrentPage()
	{
		return mCurrentPage;
	}
	
	public int getNextPage()
	{
		return ( mNextPage != INVALID_PAGE ) ? mNextPage : mCurrentPage;
	}
	
	protected int getPageCount()
	{
		return getChildCount();
	}
	
	public View getPageAt(
			int index )
	{
		return getChildAt( index );
	}
	
	public int indexToPage(
			int index )
	{
		return index;
	}
	
	protected void setChildAlpha(
			View child ,
			float alpha )
	{
		child.setAlpha( alpha );
	}
	
	/**
	 * Updates the scroll of the current page immediately to its final scroll position.  We use this
	 * in CustomizePagedView to allow tabs to share the same PagedView while resetting the scroll of
	 * the previous tab page.
	 */
	public void updateCurrentPageScroll()
	{
		// If the current page is invalid, just reset the scroll position to zero
		int newX = 0;
		if( 0 <= mCurrentPage && mCurrentPage < getPageCount() )
		{
			newX = getScrollForPage( mCurrentPage );
		}
		scrollTo( newX , 0 );
		mScroller.setFinalX( newX );
		mScroller.forceFinished( true );
	}
	
	/**
	 * Called during AllApps/Home transitions to avoid unnecessary work. When that other animation
	 * ends, {@link #resumeScrolling()} should be called, along with
	 * {@link #updateCurrentPageScroll()} to correctly set the final state and re-enable scrolling.
	 */
	public void pauseScrolling()
	{
		mScroller.forceFinished( true );
	}
	
	/**
	 * Enables scrolling again.
	 * @see #pauseScrolling()
	 */
	public void resumeScrolling()
	{
	}
	
	/**
	 * Sets the current page.
	 */
	protected void setCurrentPage(
			int currentPage )
	{
		if( !mScroller.isFinished() )
		{
			mScroller.abortAnimation();
			// We need to clean up the next page here to avoid computeScrollHelper from
			// updating current page on the pass.
			mNextPage = INVALID_PAGE;
		}
		// don't introduce any checks like mCurrentPage == currentPage here-- if we change the
		// the default
		if( getChildCount() == 0 )
		{
			return;
		}
		mForceScreenScrolled = true;
		mCurrentPage = Math.max( 0 , Math.min( currentPage , getPageCount() - 1 ) );
		updateCurrentPageScroll();
		notifyPageSwitchListener();
		invalidate();
	}
	
	/**
	 * The restore page will be set in place of the current page at the next (likely first)
	 * layout.
	 */
	public void setRestorePage(
			int restorePage )
	{
		mRestorePage = restorePage;
	}
	
	protected void notifyPageSwitchListener()
	{
		if( mPageSwitchListener != null )
		{
			mPageSwitchListener.onPageSwitch( getPageAt( mCurrentPage ) , mCurrentPage );
		}
		// Update the page indicator (when we aren't reordering)
		if( mPageIndicator != null && !isReordering( false ) )
		{
			mPageIndicator.setActiveMarker( getNextPage() );
		}
	}
	
	protected void pageBeginMoving()
	{
		if( !mIsPageMoving )
		{
			mIsPageMoving = true;
			onPageBeginMoving();
		}
	}
	
	protected void pageEndMoving()
	{
		if( mIsPageMoving )
		{
			mIsPageMoving = false;
			onPageEndMoving();
		}
	}
	
	public boolean isPageMoving()
	{
		return mIsPageMoving;
	}
	
	// a method that subclasses can override to add behavior
	protected void onPageBeginMoving()
	{
		//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		if( mCurentAnimInfo != null )
		{
			if( isLoop() )
			{
				mCurentAnimInfo.setMaxScroll( 1.0f );
				mCurentAnimInfo.setLoop( true );
			}
			else
			{
				mCurentAnimInfo.setMaxScroll( 0.5f );
				mCurentAnimInfo.setLoop( false );
			}
		}
		//xiatian add end
	}
	
	// a method that subclasses can override to add behavior
	protected void onPageEndMoving()
	{
	}
	
	/**
	 * Registers the specified listener on each page contained in this workspace.
	 *
	 * @param l The listener used to respond to long clicks.
	 */
	@Override
	public void setOnLongClickListener(
			OnLongClickListener l )
	{
		mLongClickListener = l;
		final int count = getPageCount();
		for( int i = 0 ; i < count ; i++ )
		{
			getPageAt( i ).setOnLongClickListener( l );
		}
		super.setOnLongClickListener( l );
	}
	
	@Override
	public void scrollBy(
			int x ,
			int y )
	{
		scrollTo( mUnboundedScrollX + x , getScrollY() + y );
	}
	
	@Override
	public void scrollTo(
			int x ,
			int y )
	{
		// In free scroll mode, we clamp the scrollX
		if( mFreeScroll //
				|| isOverViewModel //cheyingkun add	//phenix仿S5效果,编辑模式页面指示器
		)
		{
			x = Math.min( x , mFreeScrollMaxScrollX );
			x = Math.max( x , mFreeScrollMinScrollX );
		}
		final boolean isRtl = isLayoutRtl();
		mUnboundedScrollX = x;
		boolean isXBeforeFirstPage = isRtl ? ( x > mMaxScrollX ) : ( x < 0 );
		boolean isXAfterLastPage = isRtl ? ( x < 0 ) : ( x > mMaxScrollX );
		//zhujieping add start //打开循环切页，两根手指连续滑动时，出现空白页
		if( isLoop() && x < -getViewportWidth() )
		{
			scrollTo( (int)( getViewportWidth() * getChildCount() + x ) , y );
			return;
		}
		if( isLoop() && x > mMaxScrollX + getViewportWidth() )
		{
			scrollTo( (int)( x - mMaxScrollX - getViewportWidth() ) , y );
			return;
		}
		//zhujieping add end
		if( isXBeforeFirstPage )
		{
			if( isRtl )//zhujieping add，从右向左布局时firstpage是最右边，因此是要移到mMaxScrollX
			{
				super.scrollTo( mMaxScrollX , y );
			}
			else
			{
				super.scrollTo( 0 , y );
			}
			if( mAllowOverScroll )
			{
				if( isRtl )
				{
					overScroll( x - mMaxScrollX );//overScoll
				}
				else
				{
					overScroll( x );
				}
			}
			else
			{
				if( isRtl )
				{
					super.scrollTo( mMaxScrollX , y );
				}
				else
				{
					super.scrollTo( 0 , y );
				}
			}
		}
		else if( isXAfterLastPage )
		{
			//<i_0010089> liuhailin@2015-04-02 modify begin
			if( isStripEmptyScreens )
			{
				//如果是删除空白页面,我们将以移动页面的方式(scrollTo)来将下一页回位移动至指定坐标。
				//而不是认为是overScroll的方式,即超出页面范围,从屏幕范围外回位。
				super.scrollTo( x , y );
			}
			else
			{
				if( isRtl )
				{
					super.scrollTo( 0 , y );
				}
				else
				{
					super.scrollTo( mMaxScrollX , y );
				}
				if( mAllowOverScroll )
				{
					if( isRtl )
					{
						overScroll( x );
					}
					else
					{
						overScroll( x - mMaxScrollX );
					}
				}
				else
				{
					if( isRtl )
					{
						super.scrollTo( 0 , y );
					}
					else
					{
						super.scrollTo( mMaxScrollX , y );
					}
				}
			}
			//<i_0010089> liuhailin@2015-04-02 modify end
		}
		else
		{
			mOverScrollX = x;
			super.scrollTo( x , y );
		}
		mTouchX = x;
		mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
		// Update the last motion events when scrolling
		if( isReordering( true ) )
		{
			float[] p = mapPointFromParentToView( this , mParentDownMotionX , mParentDownMotionY );
			mLastMotionX = p[0];
			mLastMotionY = p[1];
			updateDragViewTranslationDuringDrag();
		}
	}
	
	// zhangjin@2015/09/09 DEL START
	//long usertime = 0;
	//int time = 0;
	// zhangjin@2015/09/09 DEL END
	/**
	 * 开始切页特效
	 */
	private void startEffectScroll()
	{
		// zhangjin@2015/09/09 DEL START
		//long start = System.currentTimeMillis();
		// zhangjin@2015/09/09 DEL END
		int halfScreenSize = getViewportWidth() / 2;
		int screenCenter = mOverScrollX + halfScreenSize;
		if( screenCenter != mLastScreenCenter || mForceScreenScrolled )
		{
			mForceScreenScrolled = false;
			screenScrolled( screenCenter );
			mLastScreenCenter = screenCenter;
		}
		// zhangjin@2015/09/09 DEL START
		//usertime += ( System.currentTimeMillis() - start );
		//time++;
		// zhangjin@2015/09/09 DEL END
	}
	
	// we moved this functionality to a helper function so SmoothPagedView can reuse it
	protected boolean computeScrollHelper()
	{
		if( mScroller.computeScrollOffset() )
		{
			// Don't bother scrolling if the page does not need to be moved
			if( getScrollX() != mScroller.getCurrX() || getScrollY() != mScroller.getCurrY() || mOverScrollX != mScroller.getCurrX() )
			{
				float scaleX = mFreeScroll ? getScaleX() : 1f;
				int scrollX = (int)( mScroller.getCurrX() * ( 1 / scaleX ) );
				//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
				if( mFreeScroll //
						|| isOverViewModel ){
					if ((scrollX >mFreeScrollMaxScrollX &&  mScroller.getFinalX() * ( 1 / scaleX ) > mFreeScrollMaxScrollX) 
							//
							|| ( scrollX < mFreeScrollMinScrollX && mScroller.getFinalX() * ( 1 / scaleX ) < mFreeScrollMinScrollX ) )
					{
						mScroller.forceFinished( true );
					}
				}
				//zhujieping add end
				scrollTo( scrollX , mScroller.getCurrY() );
				//zhujieping add，此处的scrollto已被复写，需要invalidate
				//				if( scrollX < 0 || scrollX > mMaxScrollX )
				if( scrollX <= 0 || scrollX >= mMaxScrollX )//cheyingkun add	//解决“双层模式、删除默认配置的时钟插件、打开循环切页、关掉酷生活、打开音乐页后，循环切页后主菜单点击不进去”的问题【c_0004425】
				{
					invalidate();
				}
				//zhujieping add
			}
			//<scrollTo已经刷屏了，没有必要再次刷> hongqingquan@2015-04-02 modify begin
			//invalidate();
			else
			{
				invalidate();
			}
			//<scrollTo已经刷屏了，没有必要再次刷> hongqingquan@2015-04-02 modify end
			return true;
		}
		else if( mNextPage != INVALID_PAGE )
		{
			mCurrentPage = Math.max( 0 , Math.min( mNextPage , getPageCount() - 1 ) );
			mNextPage = INVALID_PAGE;
			updateWorkspaceItemsStateOnEndMovingInNormalMode(); //xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			// zhangjin@2015/09/10 UPD START
			//notifyPageSwitchListener();
			this.post( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					notifyPageSwitchListener();
				}
			} );
			// zhangjin@2015/09/10 UPD END
			// Load the associated pages if necessary
			if( mDeferLoadAssociatedPagesUntilScrollCompletes )
			{
				loadAssociatedPages( mCurrentPage );
				mDeferLoadAssociatedPagesUntilScrollCompletes = false;
			}
			// We don't want to trigger a page end moving unless the page has settled
			// and the user has stopped scrolling
			if( mTouchState == TOUCH_STATE_REST )
			{
				pageEndMoving();
			}
			onPostReorderingAnimationCompleted();
			//0010451: 【编辑界面】长按桌面进入编辑界面，点击页面调整顺序时当前点击的页面会闪动. , change by shlt@2015/03/12 DEL START
			//ps：影响未知，如有界面没有被重置回原位，这里可能还要做相关的判断
			//if( !mScroller.computeScrollOffset() )
			//{
			//	restView();
			//	invalidate();
			//}
			//0010451: 【编辑界面】长按桌面进入编辑界面，点击页面调整顺序时当前点击的页面会闪动. , change by shlt@2015/03/12 DEL END
			return true;
		}
		return false;
	}
	
	@Override
	public void computeScroll()
	{
		computeScrollHelper();
	}
	
	protected boolean shouldSetTopAlignedPivotForWidget(
			int childIndex )
	{
		return mTopAlignPageWhenShrinkingForBouncer;
	}
	
	public static class LayoutParams extends ViewGroup.LayoutParams
	{
		
		public boolean isFullScreenPage = false;
		public boolean isNoSearchPage = false;
		public int searchBarHeight = 0;
		public boolean isFavoritesPage = false;//cheyingkun add	//原生方式适配虚拟按键(酷生活页)
		
		/**
		 * {@inheritDoc}
		 */
		public LayoutParams(
				int width ,
				int height )
		{
			super( width , height );
		}
		
		public LayoutParams(
				ViewGroup.LayoutParams source )
		{
			super( source );
		}
	}
	
	protected LayoutParams generateDefaultLayoutParams()
	{
		return new LayoutParams( LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT );
	}
	
	//cheyingkun start	//原生方式适配虚拟按键(酷生活页)
	//cheyingkun del start
	//	public void addFullScreenPage(
	//			View page ,
	//			boolean isLeft )//isLeft为true时，添加到最左边，false添加到最后，zhujieping update
	//	{
	//		LayoutParams lp = generateDefaultLayoutParams();
	//		lp.isFullScreenPage = true;
	//		if( isLeft )
	//			super.addView( page , 0 , lp );
	//		else
	//			super.addView( page , lp );
	//	}
	//cheyingkun del end
	//cheyingkun add start
	public void addFavoritesPage(
			View mPageView ,
			int mPageIndexInViewGroup //xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	)
	{
		LayoutParams lp = generateDefaultLayoutParams();
		lp.isFavoritesPage = true;
		//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//		super.addView( mPageView , 0 , lp );//xiatian del
		super.addView( mPageView , mPageIndexInViewGroup , lp );//xiatian add
		//xiatian end
	}
	//cheyingkun add end
	//cheyingkun end
	;
	
	/**
	 * 添加一个不显示搜索框的页面
	 * @param mMediaPageView 需要添加的页面
	 * @param searchBarHeight 搜索框的高度
	 * @author yangtianyu 2016-7-29
	 */
	public void addNoSearchPage(
			View mMediaPageView ,
			int searchBarHeight ,
			int mMediaPageIndexInViewGroup //xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	)
	{
		LayoutParams lp = generateDefaultLayoutParams();
		lp.isNoSearchPage = true;
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
			lp.searchBarHeight = searchBarHeight;
		super.addView( mMediaPageView , mMediaPageIndexInViewGroup , lp );
	}
	
	public int getNormalChildHeight()
	{
		return mNormalChildHeight;
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		if( !mIsDataReady || getChildCount() == 0 )
		{
			super.onMeasure( widthMeasureSpec , heightMeasureSpec );
			return;
		}
		// We measure the dimensions of the PagedView to be larger than the pages so that when we
		// zoom out (and scale down), the view is still contained in the parent
		int widthMode = MeasureSpec.getMode( widthMeasureSpec );
		int widthSize = MeasureSpec.getSize( widthMeasureSpec );
		int heightMode = MeasureSpec.getMode( heightMeasureSpec );
		int heightSize = MeasureSpec.getSize( heightMeasureSpec );
		// NOTE: We multiply by 1.5f to account for the fact that depending on the offset of the
		// viewport, we can be at most one and a half screens offset once we scale down
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int maxSize = Math.max( dm.widthPixels , dm.heightPixels + mInsets.top + mInsets.bottom );
		int parentWidthSize = 0 , parentHeightSize = 0;
		int scaledWidthSize = 0 , scaledHeightSize = 0;
		if( mUseMinScale )
		{
			parentWidthSize = (int)( 1.5f * maxSize );
			parentHeightSize = maxSize;
			scaledWidthSize = (int)( parentWidthSize / mMinScale );
			scaledHeightSize = (int)( parentHeightSize / mMinScale );
		}
		else
		{
			scaledWidthSize = widthSize;
			scaledHeightSize = heightSize;
		}
		mViewport.set( 0 , 0 , widthSize , heightSize );
		halfWidthScreen = getViewportWidth() >> 1;
		if( widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED )
		{
			super.onMeasure( widthMeasureSpec , heightMeasureSpec );
			return;
		}
		// Return early if we aren't given a proper dimension
		if( widthSize <= 0 || heightSize <= 0 )
		{
			super.onMeasure( widthMeasureSpec , heightMeasureSpec );
			return;
		}
		/* Allow the height to be set as WRAP_CONTENT. This allows the particular case
		 * of the All apps view on XLarge displays to not take up more space then it needs. Width
		 * is still not allowed to be set as WRAP_CONTENT since many parts of the code expect
		 * each page to have the same width.
		 */
		final int verticalPadding = getPaddingTop() + getPaddingBottom();
		final int horizontalPadding = getPaddingLeft() + getPaddingRight();
		// The children are given the same width and height as the workspace
		// unless they were set to WRAP_CONTENT
		if( DEBUG )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( TAG , StringUtils.concat(
						"onMeasure()-widthSize:" ,
						widthSize ,
						"-heightSize:" ,
						heightSize ,
						"-scaledWidthSize:" ,
						scaledWidthSize ,
						"-scaledHeightSize:" ,
						scaledHeightSize ,
						"-parentWidthSize:" ,
						parentWidthSize ,
						"-parentHeightSize:" ,
						parentHeightSize ,
						"-horizontalPadding:" ,
						horizontalPadding ,
						"-verticalPadding:" ,
						verticalPadding ) );
			}
		}
		final int childCount = getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			// disallowing padding in paged view (just pass 0)
			final View child = getPageAt( i );
			if( child.getVisibility() != GONE )
			{
				final LayoutParams lp = (LayoutParams)child.getLayoutParams();
				int childWidthMode;
				int childHeightMode;
				int childWidth;
				int childHeight;
				if( !lp.isFullScreenPage )
				{
					if( lp.width == LayoutParams.WRAP_CONTENT )
					{
						childWidthMode = MeasureSpec.AT_MOST;
					}
					else
					{
						childWidthMode = MeasureSpec.EXACTLY;
					}
					if( lp.height == LayoutParams.WRAP_CONTENT )
					{
						childHeightMode = MeasureSpec.AT_MOST;
					}
					else
					{
						childHeightMode = MeasureSpec.EXACTLY;
					}
					childWidth = widthSize - horizontalPadding;
					childHeight = heightSize - verticalPadding - mInsets.top - mInsets.bottom;
					//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“编辑模式下，页面缩略图的竖直位置，随最后一个页面的不同而不同”的问题）
					if( mNormalChildHeight == -1 )
					{
						mNormalChildHeight = childHeight;
					}
					//xiatian add end
					if( lp.isNoSearchPage )
						childHeight += lp.searchBarHeight;
					//cheyingkun add start	//原生方式适配虚拟按键(酷生活页)
					if( lp.isFavoritesPage )
					{
						childHeight = getViewportHeight() - mInsets.bottom;
					}
					//cheyingkun add end
					//					mNormalChildHeight = childHeight;//xiatian del	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。（解决“编辑模式下，页面缩略图的竖直位置，随最后一个页面的不同而不同”的问题）
				}
				else
				{
					childWidthMode = MeasureSpec.EXACTLY;
					childHeightMode = MeasureSpec.EXACTLY;
					if( mUseMinScale )
					{
						childWidth = getViewportWidth();
						childHeight = getViewportHeight();
					}
					else
					{
						childWidth = widthSize - getPaddingLeft() - getPaddingRight();
						childHeight = heightSize - getPaddingTop() - getPaddingBottom();
					}
				}
				final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( childWidth , childWidthMode );
				final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec( childHeight , childHeightMode );
				child.measure( childWidthMeasureSpec , childHeightMeasureSpec );
			}
		}
		setMeasuredDimension( scaledWidthSize , scaledHeightSize );
		//		if( childCount > 0 )
		//		{
		//			// Calculate the variable page spacing if necessary
		//			if( mAutoComputePageSpacing && mRecomputePageSpacing )
		//			{
		//				// The gap between pages in the PagedView should be equal to the gap from the page
		//				// to the edge of the screen (so it is not visible in the current screen).  To
		//				// account for unequal padding on each side of the paged view, we take the maximum
		//				// of the left/right gap and use that as the gap between each page.
		//				int offset = ( getViewportWidth() - getChildWidth( 0 ) ) / 2;
		//				int spacing = Math.max( offset , widthSize - offset - getChildAt( 0 ).getMeasuredWidth() );
		//				setPageSpacing( spacing );
		//				mRecomputePageSpacing = false;
		//			}
		//		}
	}
	
	public void setPageSpacing(
			int pageSpacing )
	{
		mPageSpacing = pageSpacing;
		requestLayout();
	}
	
	protected int getFirstChildLeft()
	{
		return mFirstChildLeft;
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		if( !mIsDataReady || getChildCount() == 0 )
		{
			return;
		}
		if( DEBUG )
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "PagedView.onLayout()" );
		final int childCount = getChildCount();
		int screenWidth = getViewportWidth();
		int offsetX = getViewportOffsetX();
		int offsetY = getViewportOffsetY();
		// Update the viewport offsets
		mViewport.offset( offsetX , offsetY );
		final boolean isRtl = isLayoutRtl();
		final int startIndex = isRtl ? childCount - 1 : 0;
		final int endIndex = isRtl ? -1 : childCount;
		final int delta = isRtl ? -1 : 1;
		int verticalPadding = getPaddingTop() + getPaddingBottom();
		int childLeft = mFirstChildLeft = offsetX + ( screenWidth - getChildWidth( startIndex ) ) / 2;
		if( mPageScrolls == null || getChildCount() != mChildCountOnLastLayout )
		{
			mPageScrolls = new int[getChildCount()];
		}
		for( int i = startIndex ; i != endIndex ; i += delta )
		{
			final View child = getPageAt( i );
			if( child.getVisibility() != View.GONE )
			{
				final int childWidth = child.getMeasuredWidth();
				final int childHeight = child.getMeasuredHeight();
				LayoutParams lp = (LayoutParams)child.getLayoutParams();
				int childTop;
				if( lp.isFullScreenPage )
				{
					childTop = offsetY;
				}
				else if( lp.isNoSearchPage )
				{
					childTop = offsetY + getPaddingTop() + mInsets.top - lp.searchBarHeight;
				}
				//cheyingkun add start	//原生方式适配虚拟按键(酷生活页)
				else if( lp.isFavoritesPage )
				{
					childTop = offsetY;
				}
				//cheyingkun add end
				else
				{
					childTop = offsetY + getPaddingTop() + mInsets.top;
					if( mCenterPagesVertically )
					{
						childTop += ( getViewportHeight() - mInsets.top - mInsets.bottom - verticalPadding - childHeight ) / 2;
					}
				}
				if( DEBUG )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "\tlayout-child" , i , "--childLeft:" , childLeft , ",childTop:" , childTop , ",childWidth:" , childWidth , ",childHeight:" , childHeight ) );
				child.layout( childLeft , childTop , childLeft + childWidth , childTop + childHeight );
				// We assume the left and right padding are equal, and hence center the pages
				// horizontally
				int scrollOffset = ( getViewportWidth() - childWidth ) / 2;
				//								mPageScrolls[i] = childLeft - scrollOffset - offsetX;
				mPageScrolls[i] = getViewportWidth() * i;
				if( i != endIndex - delta )
				{
					childLeft += childWidth + scrollOffset;
					int nextScrollOffset = ( getViewportWidth() - getChildWidth( i + delta ) ) / 2;
					childLeft += nextScrollOffset;
				}
			}
		}
		if( mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount() )
		{
			setHorizontalScrollBarEnabled( false );
			updateCurrentPageScroll();
			setHorizontalScrollBarEnabled( true );
			mFirstLayout = false;
		}
		if( childCount > 0 )
		{
			// zhujieping@2015/06/09 UPD START，计算max值与布局无关
			final int index = getPageIndexIngoreLayoutDirection( getChildCount() - 1 );
			mMaxScrollX = getScrollForPage( index );
			// zhujieping@2015/06/09 UPD END
		}
		else
		{
			mMaxScrollX = 0;
		}
		if( mScroller.isFinished() && mChildCountOnLastLayout != getChildCount() )
		{
			if( mRestorePage != INVALID_RESTORE_PAGE )
			{
				setCurrentPage( mRestorePage );
				mRestorePage = INVALID_RESTORE_PAGE;
			}
			else
			{
				setCurrentPage( getNextPage() );
			}
		}
		mChildCountOnLastLayout = getChildCount();
		//		if( isReordering( true ) )
		//		{
		//			updateDragViewTranslationDuringDrag();
		//		}
		xCenterFirstPage = halfWidthScreen;//记忆第一页中轴x坐标
		int index = getPageIndexIngoreLayoutDirection( getChildCount() - 1 );
		xCenterLastPage = getScrollForPage( index ) + halfWidthScreen; //记忆最后一页中轴x坐标
	}
	
	protected void changeChildAlpha(
			int screenCenter )
	{
		boolean isInOverscroll = mOverScrollX < 0 || mOverScrollX > mMaxScrollX;
		if( mFadeInAdjacentScreens && !isInOverscroll )
		{
			for( int i = 0 ; i < getChildCount() ; i++ )
			{
				View child = getChildAt( i );
				if( child != null )
				{
					float scrollProgress = getScrollProgress( screenCenter , child , i );
					float alpha = 1 - Math.abs( scrollProgress );
					child.setAlpha( alpha );
				}
			}
			invalidate();
		}
	}
	
	protected void screenScrolled(
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
		}
		nextPage = getPageIndexIngoreLayoutDirection( nextPage );
		//计算切页初始数据
		View view = getPageAt( curPage );
		if( view instanceof PagedViewGridLayout )//小组件页
		{
			if( isLoop() )
			{
				View nextView = getPageAt( nextPage );
				int delta = screenCenter - xCurPageCenter;
				float percentageScroll = delta / ( widthScreen * 1.0f );
				if( view != null && nextView != null )
				{
					if( screenCenter > xCenterLastPage || screenCenter < xCenterFirstPage )
					{
						int translateX = (int)( -getViewportWidth() * percentageScroll );
						view.setTranslationX( translateX );
						nextView.setTranslationX( 0 );
					}
					else
					{
						if( view.getTranslationX() != 0 )
							view.setTranslationX( 0 );
						if( nextView.getTranslationX() != 0 )
							nextView.setTranslationX( 0 );
					}
				}
			}
			//curView = null;//fulijuan xiugai
		}
//		else
//		{
			curView = (IEffect)getPageAt( curPage );
//		}
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
			// zhangjin@2015/09/10 UPD START
			//curView.setCameraDistance( mDensity * CAMERA_DISTANCE );
			// gaominghui@2017/01/04 ADD START 兼容android 4.0
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
			// gaominghui@2017/01/04 ADD END 兼容android 4.0
			// zhangjin@2015/09/10 UPD END
			//计算滑动百分比:利用可见视窗中轴和当前页中轴差值
			int delta = screenCenter - xCurPageCenter;
			float percentageScroll = delta / ( widthScreen * 1.0f );
			if( percentageScroll != 0 )
			{
				mStartEffectEnd = false;
			}
			//切页过程动画实现
			if( isRtl )
			{
				if( nextView != null && screenCenter <= xCenterLastPage )
				{
					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterLastPage > 0 , false , false );
					mCurentAnimInfo.getTransformationMatrix(
							nextView ,
							percentageScroll - 1 ,
							widthScreen ,
							pageHeight ,
							mDensity * CAMERA_DISTANCE ,
							screenCenter - xCenterLastPage > 0 ,
							true ,
							false );
				}
				else
				{
					mCurentAnimInfo.getTransformationMatrix(
							curView ,
							percentageScroll ,
							widthScreen ,
							pageHeight ,
							mDensity * CAMERA_DISTANCE ,
							screenCenter - xCenterLastPage > 0 ,
							false ,
							mIsFirstOrLastEffect );
					if( isLoop() && nextView != null )
					{
						mCurentAnimInfo.getTransformationMatrix( nextView , percentageScroll - 1 , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , false , true , false );
					}
				}
			}
			else
			{
				if( nextView != null && screenCenter >= xCenterFirstPage )
				{
					mCurentAnimInfo.getTransformationMatrix( curView , percentageScroll , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , screenCenter - xCenterFirstPage < 0 , false , false );
					mCurentAnimInfo.getTransformationMatrix(
							nextView ,
							percentageScroll + 1 ,
							widthScreen ,
							pageHeight ,
							mDensity * CAMERA_DISTANCE ,
							screenCenter - xCenterFirstPage < 0 ,
							true ,
							false );
				}
				else
				{
					mCurentAnimInfo.getTransformationMatrix(
							curView ,
							percentageScroll ,
							widthScreen ,
							pageHeight ,
							mDensity * CAMERA_DISTANCE ,
							screenCenter - xCenterFirstPage < 0 ,
							false ,
							mIsFirstOrLastEffect );
					if( isLoop() && nextView != null )
					{
						mCurentAnimInfo.getTransformationMatrix( nextView , percentageScroll + 1 , widthScreen , pageHeight , mDensity * CAMERA_DISTANCE , false , true , false );
					}
				}
			}
			if( percentageScroll == 0 && mTouchState != TOUCH_STATE_SCROLLING )
			{
				stopEffecf();
			}
		}
	}
	
	/**
	 * 置位所有切页特效的数据 wanghongjian add
	 */
	public void stopEffecf()
	{
		mStartEffectEnd = true;
		if( mCurentAnimInfo != null )
		{
			mCurentAnimInfo.stopEffecf();
		}
		pageWidth = 0;
		pageHeight = 0;
		// zhangjin@2015/09/09 DEL START
		//if( time > 0 )
		//{
		//	float preTime = usertime / ( time * 1f );
		//	Log.v( "" , "preTime " + preTime + " time " + time + " usertime " + usertime + " mTouchState " + mTouchState );
		//	usertime = 0;
		//	time = 0;
		//}
		// zhangjin@2015/09/09 DEL END
	}
	
	protected void enablePagedViewAnimations()
	{
		mAllowPagedViewAnimations = true;
	}
	
	protected void disablePagedViewAnimations()
	{
		mAllowPagedViewAnimations = false;
	}
	
	@Override
	public void onChildViewAdded(
			View parent ,
			View child )
	{
		// Update the page indicator, we don't update the page indicator as we
		// add/remove pages
		//zhujieping ，同步removeMarkerForView中的判断，isReordering()中mIsReordering值在手指松开后的动画结束后才置为false，此时mTouchState的状态已置为TOUCH_STATE_REST，
		//传入的参数为false时，只根据mIsReordering来判断是否add/remove pages，传入true时，还要根据mTouchState的状态进行判断。
		//当mTouchState = TOUCH_STATE_REST，手指已松开，说明不是add/remove pages的状态。所以这边传入参数应为true
		if( mPageIndicator != null && !isReordering( true ) /*!isReordering( false )*/ )
		{
			int pageIndex = indexOfChild( child );
			mPageIndicator.addMarker( pageIndex , getPageIndicatorMarker( pageIndex ) , mAllowPagedViewAnimations );
		}
		// This ensures that when children are added, they get the correct transforms / alphas
		// in accordance with any scroll effects.
		mForceScreenScrolled = true;
		updateFreescrollBounds();
		invalidate();
	}
	
	@Override
	public void onChildViewRemoved(
			View parent ,
			View child )
	{
		mForceScreenScrolled = true;
		updateFreescrollBounds();
		invalidate();
	}
	
	private void removeMarkerForView(
			int index )
	{
		// Update the page indicator, we don't update the page indicator as we
		// add/remove pages
		//zhujieping update【i_0010895】，isReordering()中mIsReordering值在手指松开后的动画结束后才置为false，此时mTouchState的状态已置为TOUCH_STATE_REST，
		//传入的参数为false时，只根据mIsReordering来判断是否add/remove pages，传入true时，还要根据mTouchState的状态进行判断。
		//当mTouchState = TOUCH_STATE_REST，手指已松开，说明不是add/remove pages的状态。所以这边传入参数应为true
		if( mPageIndicator != null && !isReordering( true )/*!isReordering( false )*/)
		{
			mPageIndicator.removeMarker( index , mAllowPagedViewAnimations );
		}
	}
	
	@Override
	public void removeView(
			View v )
	{
		// XXX: We should find a better way to hook into this before the view
		// gets removed form its parent...
		removeMarkerForView( indexOfChild( v ) );
		super.removeView( v );
	}
	
	@Override
	public void removeViewInLayout(
			View v )
	{
		// XXX: We should find a better way to hook into this before the view
		// gets removed form its parent...
		removeMarkerForView( indexOfChild( v ) );
		super.removeViewInLayout( v );
	}
	
	@Override
	public void removeViewAt(
			int index )
	{
		// XXX: We should find a better way to hook into this before the view
		// gets removed form its parent...
		//				removeViewAt( index );//zhujieping del,加上这句，造成死循环
		removeMarkerForView( index );//zhujieping add,跟其他removeview的方法保持一致
		super.removeViewAt( index );
	}
	
	@Override
	public void removeAllViewsInLayout()
	{
		// Update the page indicator, we don't update the page indicator as we
		// add/remove pages
		if( mPageIndicator != null )
		{
			mPageIndicator.removeAllMarkers( mAllowPagedViewAnimations );
		}
		super.removeAllViewsInLayout();
	}
	
	protected int getChildOffset(
			int index )
	{
		if( index < 0 || index > getChildCount() - 1 )
			return 0;
		int offset = getPageAt( index ).getLeft() - getViewportOffsetX();
		return offset;
	}
	
	protected void getOverviewModePages(
			int[] range )
	{
		range[0] = 0;
		range[1] = Math.max( 0 , getChildCount() - 1 );
	}
	
	public void getVisiblePages(
			int[] range )
	{
		final int pageCount = getChildCount();
		mTmpIntPoint[0] = mTmpIntPoint[1] = 0;
		range[0] = -1;
		range[1] = -1;
		if( pageCount > 0 )
		{
			int viewportWidth = getViewportWidth();
			int curScreen = 0;
			int count = getChildCount();
			for( int i = 0 ; i < count ; i++ )
			{
				View currPage = getPageAt( i );
				mTmpIntPoint[0] = 0;
				Utilities.getDescendantCoordRelativeToParent( currPage , this , mTmpIntPoint , false );
				if( mTmpIntPoint[0] > viewportWidth )
				{
					if( range[0] == -1 )
					{
						continue;
					}
					else
					{
						break;
					}
				}
				mTmpIntPoint[0] = currPage.getMeasuredWidth();
				Utilities.getDescendantCoordRelativeToParent( currPage , this , mTmpIntPoint , false );
				if( mTmpIntPoint[0] < 0 )
				{
					if( range[0] == -1 )
					{
						continue;
					}
					else
					{
						break;
					}
				}
				curScreen = i;
				if( range[0] < 0 )
				{
					range[0] = curScreen;
				}
			}
			range[1] = curScreen;
		}
		else
		{
			range[0] = -1;
			range[1] = -1;
		}
	}
	
	/** 
	* @Title: getVisiblePages_EX 
	* @author hongqingquan
	* @Description: TODO(优化此函数) 
	* @param @param range    设定文件 
	* @return void    返回类型 
	* @throws 
	*/
	protected void getVisiblePages_EX(
			int[] range )
	{
		final int pageCount = getChildCount();
		range[0] = -1;
		range[1] = -1;
		if( pageCount > 0 )
		{
			range[0] = 0;
			range[1] = Math.max( 0 , getChildCount() - 1 );
		}
		else
		{
			range[0] = -1;
			range[1] = -1;
		}
	}
	
	protected boolean shouldDrawChild(
			View child )
	{
		return child.getAlpha() > 0 && child.getVisibility() == VISIBLE;
	}
	
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		if( getChildCount() <= 0 )
		{
			return;
		}
		startEffectScroll();
		// Find out which screens are visible; as an optimization we only call draw on them
		final int pageCount = getChildCount();
		if( pageCount > 0 )
		{
			getVisiblePages( mTempVisiblePagesRange );
			final int leftScreen = mTempVisiblePagesRange[0];
			final int rightScreen = mTempVisiblePagesRange[1];
			if( ( leftScreen != -1 && rightScreen != -1 ) || ( isLoop() && ( leftScreen != -1 || rightScreen != -1 ) ) )
			{
				final long drawingTime = getDrawingTime();
				// Clip to the bounds
				canvas.save();
				canvas.clipRect( getScrollX() , getScrollY() , getScrollX() + getRight() - getLeft() , getScrollY() + getBottom() - getTop() );
				// Draw all the children, leaving the drag view for last
				for( int i = pageCount - 1 ; i >= 0 ; i-- )
				{
					final View v = getPageAt( i );
					if( v == mDragView )
						continue;
					if( mForceDrawAllChildrenNextFrame || ( leftScreen <= i && i <= rightScreen && shouldDrawChild( v ) ) )
					{
						if( !( isLoop()
						//
						&& ( ( ( mOverScrollX + getViewportWidth() < 0 || mOverScrollX - mMaxScrollX > getViewportWidth() ) && getChildCount() == 2 )//zhujieping，滑动页面超过一屏距离，且这时候只有两页时，这里不画该view（因为这个view的位置在屏幕范围内，这里画的画会导致重叠，在下面一段中会进行绘制）
						//
						|| ( ( mOverScrollX < 0 && i != 0 ) || ( mOverScrollX > mMaxScrollX && i != getChildCount() - 1 ) && getChildCount() == 2 )//zhujieping,当例如只有两页且风车特效时，第一页向左滑动时，同时给最后一页（第二页）设置了tranlation和rotation，导致算位置时第二页也算在屏幕范围内，这里画第二页导致显示异常
						//
						) ) )
						{
							drawChild( canvas , v , drawingTime );
						}
					}
				}
				// zhujieping@2015/05/04 ADD START,从第一页滑动最后一页时，translate画布，画出最后一页，反之亦然
				if( isLoop() )
				{
					View firstView = getPageAt( 0 );
					View lastView = getPageAt( pageCount - 1 );
					if( mOverScrollX < 0 || mOverScrollX > mMaxScrollX )
					{
						if( !( mForceDrawAllChildrenNextFrame ) )//根据mOverScrollX来判断画最后一页还是第一页
						{
							if( mOverScrollX < 0 )
							{
								int offset = -getViewportWidth() * pageCount - mOverScrollX;
								View viewTodraw;
								if( ( isLayoutRtl() ) )
								{
									viewTodraw = firstView;
								}
								else
								{
									viewTodraw = lastView;
								}
								canvas.translate( offset , 0 );
								drawChild( canvas , viewTodraw , drawingTime );
								canvas.translate( -offset , 0 );
								viewTodraw = null;
								if( mOverScrollX + getViewportWidth() < 0 )
								{
									if( isLayoutRtl() )
									{
										viewTodraw = getChildAt( 1 );
									}
									else
									{
										viewTodraw = getChildAt( pageCount - 2 );
									}
									if( viewTodraw != null )
									{
										canvas.translate( offset , 0 );
										drawChild( canvas , viewTodraw , drawingTime );
										canvas.translate( -offset , 0 );
									}
								}
							}
						}
						if( mOverScrollX > mMaxScrollX )
						{
							int offset = getViewportWidth() * pageCount + ( mMaxScrollX - mOverScrollX );
							View viewTodraw;
							if( isLayoutRtl() )
							{
								viewTodraw = lastView;
							}
							else
							{
								viewTodraw = firstView;
							}
							canvas.translate( offset , 0 );
							drawChild( canvas , viewTodraw , drawingTime );
							canvas.translate( -offset , 0 );
							if( mOverScrollX - mMaxScrollX > getViewportWidth() )
							{
								if( isLayoutRtl() )
								{
									viewTodraw = getChildAt( pageCount - 2 );
								}
								else
								{
									viewTodraw = getChildAt( 1 );
								}
								if( viewTodraw != null )
								{
									canvas.translate( offset , 0 );
									drawChild( canvas , viewTodraw , drawingTime );
									canvas.translate( -offset , 0 );
								}
							}
						}
					}
				}
				// zhujieping@2015/05/04 ADD END
				// Draw the drag view on top (if there is one)
				if( mDragView != null )
				{
					//cheyingkun add start	//解决“编辑模式下，长按一页进行拖动不切页，拖动过程中出现黑色竖线。”的问题【i_0010657】
					//【问题原因】在画mDragView时，没有及时刷新，导致出现很多竖线。（某些配置较低的手机出现该问题）
					//【解决方案】在画mDragView前，先调用invalidate();方法主动刷新一下。
					//		主动刷新会耗时，添加开关switch_enable_OverviewMode_drawDragView_invalidate，默认关闭，如果需要可以打开。
					if( overviewModeDrawDragViewInvalidate//如果启动主动刷新
							&& this instanceof Workspace )
					{
						Workspace mWorkspace = (Workspace)this;
						if( mWorkspace.isInOverviewMode() )//如果是编辑模式
						{
							invalidate();
						}
					}
					//cheyingkun add end
					drawChild( canvas , mDragView , drawingTime );
				}
				mForceDrawAllChildrenNextFrame = false;
				canvas.restore();
			}
		}
	}
	
	@Override
	public boolean requestChildRectangleOnScreen(
			View child ,
			Rect rectangle ,
			boolean immediate )
	{
		int page = indexToPage( indexOfChild( child ) );
		if( page != mCurrentPage || !mScroller.isFinished() )
		{
			snapToPage( page );
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean onRequestFocusInDescendants(
			int direction ,
			Rect previouslyFocusedRect )
	{
		int focusablePage;
		if( mNextPage != INVALID_PAGE )
		{
			focusablePage = mNextPage;
		}
		else
		{
			focusablePage = mCurrentPage;
		}
		View v = getPageAt( focusablePage );
		if( v != null )
		{
			return v.requestFocus( direction , previouslyFocusedRect );
		}
		return false;
	}
	
	@Override
	public boolean dispatchUnhandledMove(
			View focused ,
			int direction )
	{
		// XXX-RTL: This will be fixed in a future CL
		if( direction == View.FOCUS_LEFT )
		{
			if( getCurrentPage() > 0 )
			{
				snapToPage( getCurrentPage() - 1 );
				return true;
			}
		}
		else if( direction == View.FOCUS_RIGHT )
		{
			if( getCurrentPage() < getPageCount() - 1 )
			{
				snapToPage( getCurrentPage() + 1 );
				return true;
			}
		}
		return super.dispatchUnhandledMove( focused , direction );
	}
	
	@Override
	public void addFocusables(
			ArrayList<View> views ,
			int direction ,
			int focusableMode )
	{
		// XXX-RTL: This will be fixed in a future CL
		if( mCurrentPage >= 0 && mCurrentPage < getPageCount() )
		{
			//			getPageAt( mCurrentPage ).addFocusables( views , direction , focusableMode );
			View view = getPageAt( mCurrentPage );
			if( view != null )
			{
				view.addFocusables( views , direction , focusableMode );
			}
		}
		if( direction == View.FOCUS_LEFT )
		{
			//			if( mCurrentPage > 0 )
			//			{
			//				getPageAt( mCurrentPage - 1 ).addFocusables( views , direction , focusableMode );
			//			}
			View view = getPageAt( Math.max( 0 , Math.min( mCurrentPage - 1 , getPageCount() ) ) );
			if( view != null )
			{
				view.addFocusables( views , direction , focusableMode );
			}
		}
		else if( direction == View.FOCUS_RIGHT )
		{
			if( mCurrentPage < getPageCount() - 1 )
			{
				//				getPageAt( mCurrentPage + 1 ).addFocusables( views , direction , focusableMode );
				View view = getPageAt( Math.max( 0 , Math.min( mCurrentPage + 1 , getPageCount() ) ) );
				if( view != null )
				{
					view.addFocusables( views , direction , focusableMode );
				}
			}
		}
	}
	
	/**
	 * If one of our descendant views decides that it could be focused now, only
	 * pass that along if it's on the current page.
	 *
	 * This happens when live folders requery, and if they're off page, they
	 * end up calling requestFocus, which pulls it on page.
	 */
	@Override
	public void focusableViewAvailable(
			View focused )
	{
		View current = getPageAt( mCurrentPage );
		View v = focused;
		while( true )
		{
			if( v == current )
			{
				super.focusableViewAvailable( focused );
				return;
			}
			if( v == this )
			{
				return;
			}
			ViewParent parent = v.getParent();
			if( parent instanceof View )
			{
				v = (View)v.getParent();
			}
			else
			{
				return;
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestDisallowInterceptTouchEvent(
			boolean disallowIntercept )
	{
		if( disallowIntercept )
		{
			// We need to make sure to cancel our long press if
			// a scrollable widget takes over touch events
			final View currentPage = getPageAt( mCurrentPage );
			currentPage.cancelLongPress();
		}
		super.requestDisallowInterceptTouchEvent( disallowIntercept );
	}
	
	/**
	 * Return true if a tap at (x, y) should trigger a flip to the previous page.
	 */
	protected boolean hitsPreviousPage(
			float x ,
			float y )
	{
		int offset = ( getViewportWidth() - getChildWidth( mCurrentPage ) ) / 2;
		if( isLayoutRtl() )
		{
			return( x > ( getViewportOffsetX() + getViewportWidth() - offset + mPageSpacing ) );
		}
		return( x < getViewportOffsetX() + offset - mPageSpacing );
	}
	
	/**
	 * Return true if a tap at (x, y) should trigger a flip to the next page.
	 */
	protected boolean hitsNextPage(
			float x ,
			float y )
	{
		int offset = ( getViewportWidth() - getChildWidth( mCurrentPage ) ) / 2;
		if( isLayoutRtl() )
		{
			return( x < getViewportOffsetX() + offset - mPageSpacing );
		}
		return( x > ( getViewportOffsetX() + getViewportWidth() - offset + mPageSpacing ) );
	}
	
	/** Returns whether x and y originated within the buffered viewport */
	private boolean isTouchPointInViewportWithBuffer(
			int x ,
			int y )
	{
		mTmpRect.set( mViewport.left - mViewport.width() / 2 , mViewport.top , mViewport.right + mViewport.width() / 2 , mViewport.bottom );
		return mTmpRect.contains( x , y );
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		if( DISABLE_TOUCH_INTERACTION )
		{
			return false;
		}
		//zhujieping add start	//换主题不重启
		if( Launcher.isThemeChanging() )
		{
			return false;
		}
		//zhujieping add ends
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onTouchEvent will be called and we do the actual
		 * scrolling there.
		 */
		acquireVelocityTrackerAndAddMovement( ev );
		// Skip touch handling if there are no pages to swipe
		if( getChildCount() <= 0 )
			return super.onInterceptTouchEvent( ev );
		/*
		 * Shortcut the most recurring case: the user is in the dragging
		 * state and he is moving his finger.  We want to intercept this
		 * motion.
		 */
		final int action = ev.getAction();
		if( ( action == MotionEvent.ACTION_MOVE ) && ( mTouchState == TOUCH_STATE_SCROLLING ) )
		{
			return true;
		}
		switch( action & MotionEvent.ACTION_MASK )
		{
			case MotionEvent.ACTION_MOVE:
			{
				/*
				 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
				 * whether the user has moved far enough from his original down touch.
				 */
				if( mActivePointerId != INVALID_POINTER )
				{
					determineScrollingStart( ev );
				}
				// zhangjin@2015/08/25 c_0003340 ADD START
				if( mTouchState == TOUCH_STATE_SCROLLING )
				{
					// Scroll to follow the motion event
					final int pointerIndex = ev.findPointerIndex( mActivePointerId );
					if( pointerIndex == -1 )
						return false;
					final float x = ev.getX( pointerIndex );
					// zhangjin@2015/09/16 UPD START
					//float deltaX = mDownMotionX - x;
					float deltaX = mLastMoveX - x;
					// zhangjin@2015/09/16 UPD END
					// zhangjin@2015/09/14 ADD START
					deltaX *= PAGE_ON_TOUCH_ACCELERATE;
					// zhangjin@2015/09/14 ADD END
					// Only scroll and update mLastMotionX if we have moved some discrete amount.  We
					// keep the remainder because we are actually testing if we've moved from the last
					// scrolled position (which is discrete).
					if( Math.abs( deltaX ) >= 1.0f )
					{
						mTouchX += deltaX;
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						if( !mDeferScrollUpdate )
						{
							scrollBy( (int)deltaX , 0 );
							if( DEBUG )
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.d( TAG , StringUtils.concat( "onTouchEvent().Scrolling: " , deltaX ) );
						}
						else
						{
							invalidate();
						}
						mLastMotionX = x;
						mLastMotionXRemainder = deltaX - (int)deltaX;
					}
				}
				// zhangjin@2015/08/25 ADD END
				// zhangjin@2015/09/15 ADD START
				mLastMoveX = ev.getX();
				// zhangjin@2015/09/15 ADD END
				// if mActivePointerId is INVALID_POINTER, then we must have missed an ACTION_DOWN
				// event. in that case, treat the first occurence of a move event as a ACTION_DOWN
				// i.e. fall through to the next case (don't break)
				// (We sometimes miss ACTION_DOWN events in Workspace because it ignores all events
				// while it's small- this was causing a crash before we checked for INVALID_POINTER)
				break;
			}
			case MotionEvent.ACTION_DOWN:
			{
				final float x = ev.getX();
				final float y = ev.getY();
				// Remember location of down touch
				mDownMotionX = x;
				mDownMotionY = y;
				mDownScrollX = getScrollX();
				mLastMotionX = x;
				mLastMotionY = y;
				// zhangjin@2015/09/15 ADD START
				mLastMoveX = x;
				// zhangjin@2015/09/15 ADD END
				float[] p = mapPointFromViewToParent( this , x , y );
				mParentDownMotionX = p[0];
				mParentDownMotionY = p[1];
				mLastMotionXRemainder = 0;
				mTotalMotionX = 0;
				mActivePointerId = ev.getPointerId( 0 );
				/*
				 * If being flinged and user touches the screen, initiate drag;
				 * otherwise don't.  mScroller.isFinished should be false when
				 * being flinged.
				 */
				final int xDist = Math.abs( mScroller.getFinalX() - mScroller.getCurrX() );
				final boolean finishedScrolling = ( mScroller.isFinished() || xDist < mTouchSlop / 3 );
				if( finishedScrolling )
				{
					mTouchState = TOUCH_STATE_REST;
					if( !mScroller.isFinished() && !mFreeScroll )
					{
						setCurrentPage( getNextPage() );
						pageEndMoving();
					}
				}
				else
				{
					if( isTouchPointInViewportWithBuffer( (int)mDownMotionX , (int)mDownMotionY ) )
					{
						mTouchState = TOUCH_STATE_SCROLLING;
					}
					else
					{
						mTouchState = TOUCH_STATE_REST;
					}
				}
				// check if this can be the beginning of a tap on the side of the pages
				// to scroll the current page
				if( !DISABLE_TOUCH_SIDE_PAGES )
				{
					if( mTouchState != TOUCH_STATE_PREV_PAGE && mTouchState != TOUCH_STATE_NEXT_PAGE )
					{
						if( getChildCount() > 0 )
						{
							if( hitsPreviousPage( x , y ) )
							{
								mTouchState = TOUCH_STATE_PREV_PAGE;
							}
							else if( hitsNextPage( x , y ) )
							{
								mTouchState = TOUCH_STATE_NEXT_PAGE;
							}
						}
					}
				}
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				resetTouchState();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp( ev );
				releaseVelocityTracker();
				break;
		}
		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mTouchState != TOUCH_STATE_REST;
	}
	
	protected void determineScrollingStart(
			MotionEvent ev )
	{
		determineScrollingStart( ev , 1.0f );
	}
	
	/*
	 * Determines if we should change the touch state to start scrolling after the
	 * user moves their touch point too far.
	 */
	protected void determineScrollingStart(
			MotionEvent ev ,
			float touchSlopScale )
	{
		/*
		 * Locally do absolute value. mLastMotionX is set to the y value
		 * of the down event.
		 */
		final int pointerIndex = ev.findPointerIndex( mActivePointerId );
		if( pointerIndex == -1 )
			return;
		// Disallow scrolling if we started the gesture from outside the viewport
		final float x = ev.getX( pointerIndex );
		final float y = ev.getY( pointerIndex );
		if( !isTouchPointInViewportWithBuffer( (int)x , (int)y ) )
			return;
		final int xDiff = (int)Math.abs( x - mLastMotionX );
		final int yDiff = (int)Math.abs( y - mLastMotionY );
		final int touchSlop = Math.round( touchSlopScale * mTouchSlop );
		boolean xPaged = xDiff > mPagingTouchSlop;
		boolean xMoved = xDiff > touchSlop;
		boolean yMoved = yDiff > touchSlop;
		if( xMoved || xPaged || yMoved )
		{
			if( mUsePagingTouchSlop ? xPaged : xMoved )
			{
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
				mTotalMotionX += Math.abs( mLastMotionX - x );
				mLastMotionX = x;
				mLastMotionXRemainder = 0;
				mTouchX = getViewportOffsetX() + getScrollX();
				mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
				pageBeginMoving();
			}
		}
	}
	
	protected float getMaxScrollProgress()
	{
		return 1.0f;
	}
	
	protected void cancelCurrentPageLongPress()
	{
		if( mAllowLongPress )
		{
			//mAllowLongPress = false;
			// Try canceling the long press. It could also have been scheduled
			// by a distant descendant, so use the mAllowLongPress flag to block
			// everything
			final View currentPage = getPageAt( mCurrentPage );
			if( currentPage != null )
			{
				currentPage.cancelLongPress();
			}
		}
	}
	
	protected float getBoundedScrollProgress(
			int screenCenter ,
			View v ,
			int page )
	{
		final int halfScreenSize = getViewportWidth() / 2;
		screenCenter = Math.min( getScrollX() + halfScreenSize , screenCenter );
		screenCenter = Math.max( halfScreenSize , screenCenter );
		return getScrollProgress( screenCenter , v , page );
	}
	
	protected float getScrollProgress(
			int screenCenter ,
			View v ,
			int page )
	{
		final int halfScreenSize = getViewportWidth() / 2;
		//		int totalDistance = v.getMeasuredWidth() + mPageSpacing;
		int delta = screenCenter - ( getScrollForPage( page ) + halfScreenSize );
		float scrollProgress = delta / ( getViewportWidth() * 1.0f );
		scrollProgress = Math.min( scrollProgress , getMaxScrollProgress() );
		scrollProgress = Math.max( scrollProgress , -getMaxScrollProgress() );
		return scrollProgress;
	}
	
	// zhujieping@2015/06/09 UPD START,根据layout布局中的index来获取scroll值。从右向左的布局中，最右边的index为0
	public int getScrollForPage(
			int index )
	{
		if( mPageScrolls == null || index >= mPageScrolls.length || index < 0 )
		{
			return 0;
		}
		else
		{
			if( isLayoutRtl() )
			{
				index = mPageScrolls.length - 1 - index;
			}
			return mPageScrolls[index];
		}
	}
	
	// zhujieping@2015/06/09 UPD END
	// While layout transitions are occurring, a child's position may stray from its baseline
	// position. This method returns the magnitude of this stray at any given time.
	public int getLayoutTransitionOffsetForPage(
			int index )
	{
		if( mPageScrolls == null || index >= mPageScrolls.length || index < 0 )
		{
			return 0;
		}
		else
		{
			View child = getChildAt( index );
			int scrollOffset = ( getViewportWidth() - child.getMeasuredWidth() ) / 2;
			int baselineX = getScrollForPage( index ) + scrollOffset + getViewportOffsetX();
			return (int)( child.getX() - baselineX );
		}
	}
	
	// This curve determines how the effect of scrolling over the limits of the page dimishes
	// as the user pulls further and further from the bounds
	private float overScrollInfluenceCurve(
			float f )
	{
		f -= 1.0f;
		return f * f * f + 1.0f;
	}
	
	protected void acceleratedOverScroll(
			float amount )
	{
		int screenSize = getViewportWidth();
		// We want to reach the max over scroll effect when the user has
		// over scrolled half the size of the screen
		float f = OVERSCROLL_ACCELERATE_FACTOR * ( amount / screenSize );
		if( f == 0 )
			return;
		// Clamp this factor, f, to -1 < f < 1
		if( Math.abs( f ) >= 1 )
		{
			f /= Math.abs( f );
		}
		int overScrollAmount = (int)Math.round( f * screenSize );
		if( amount < 0 )
		{
			mOverScrollX = overScrollAmount;
			super.scrollTo( mOverScrollX , getScrollY() );
		}
		else
		{
			mOverScrollX = mMaxScrollX + overScrollAmount;
			super.scrollTo( mOverScrollX , getScrollY() );
		}
		invalidate();
	}
	
	protected void dampedOverScroll(
			float amount )
	{
		int screenSize = getViewportWidth();
		if( mIsFirstOrLastEffect )
		{
			// zhangjin@2015/09/16 UPD START
			//int halfscreenSize = screenSize / 2;
			int halfscreenSize = (int)( screenSize * 0.25 );
			// zhangjin@2015/09/16 UPD END
			if( amount < 0 )
			{
				if( !isLoop() )//zhujieping，不支持滑页时，桌面支持回弹效果，最大为半个屏宽
				{
					if( amount < -halfscreenSize )
					{
						amount = (int)( -halfscreenSize );
					}
				}
				mOverScrollX = (int)amount;
			}
			else
			{
				if( !isLoop() )
				{
					if( amount > halfscreenSize )
					{
						amount = (int)( halfscreenSize );
					}
				}
				mOverScrollX = mMaxScrollX + (int)amount;
			}
		}
		else
		{
			float f = ( amount / screenSize );
			if( f == 0 )
				return;
			f = f / ( Math.abs( f ) ) * ( overScrollInfluenceCurve( Math.abs( f ) ) );
			// Clamp this factor, f, to -1 < f < 1
			if( Math.abs( f ) >= 1 )
			{
				f /= Math.abs( f );
			}
			int overScrollAmount = (int)Math.round( OVERSCROLL_DAMP_FACTOR * f * screenSize );
			if( amount < 0 )
			{
				mOverScrollX = overScrollAmount;
				super.scrollTo( mOverScrollX , getScrollY() );
			}
			else
			{
				mOverScrollX = mMaxScrollX + overScrollAmount;
				super.scrollTo( mOverScrollX , getScrollY() );
			}
		}
		invalidate();
	}
	
	protected void overScroll(
			float amount )
	{
		dampedOverScroll( amount );
	}
	
	protected float maxOverScroll()
	{
		// Using the formula in overScroll, assuming that f = 1.0 (which it should generally not
		// exceed). Used to find out how much extra wallpaper we need for the over scroll effect
		float f = 1.0f;
		f = f / ( Math.abs( f ) ) * ( overScrollInfluenceCurve( Math.abs( f ) ) );
		return OVERSCROLL_DAMP_FACTOR * f;
	}
	
	public void enableFreeScroll()
	{
		setEnableFreeScroll( true , -1 );
	}
	
	public void disableFreeScroll(
			int snapPage )
	{
		setEnableFreeScroll( false , snapPage );
	}
	
	void updateFreescrollBounds()
	{
		getOverviewModePages( mTempVisiblePagesRange );
		// zhujieping@2015/06/09 UPD START，计算值与布局无关
		if( isLayoutRtl() )
		{
			mFreeScrollMinScrollX = getScrollForPage( mTempVisiblePagesRange[1] );
			mFreeScrollMaxScrollX = getScrollForPage( mTempVisiblePagesRange[0] );
		}
		else
		{
			mFreeScrollMinScrollX = getScrollForPage( mTempVisiblePagesRange[0] );
			mFreeScrollMaxScrollX = getScrollForPage( mTempVisiblePagesRange[1] );
		}
		// zhujieping@2015/06/09 UPD END
	}
	
	private void setEnableFreeScroll(
			boolean freeScroll ,
			int snapPage )
	{
		//cheyingkun add start	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_FREESCROLL )
		{
			isOverViewModel = freeScroll;
		}
		else
		//cheyingkun add end
		{
			mFreeScroll = freeScroll;
		}
		if( snapPage == -1 )
		{
			snapPage = getPageNearestToCenterOfScreen( 0.5f );
		}
		if( !freeScroll )
		{
			snapToPage( snapPage );
		}
		else
		{
			updateFreescrollBounds();
			getOverviewModePages( mTempVisiblePagesRange );
			if( getCurrentPage() < mTempVisiblePagesRange[0] )
			{
				setCurrentPage( mTempVisiblePagesRange[0] );
			}
			else if( getCurrentPage() > mTempVisiblePagesRange[1] )
			{
				setCurrentPage( mTempVisiblePagesRange[1] );
			}
		}
		setEnableOverscroll( !freeScroll );
	}
	
	private void setEnableOverscroll(
			boolean enable )
	{
		mAllowOverScroll = enable;
	}
	
	int getNearestHoverOverPageIndex()
	{
		if( mDragView != null )
		{
			int dragX = (int)( mDragView.getLeft() + ( mDragView.getMeasuredWidth() / 2 ) + mDragView.getTranslationX() );
			getOverviewModePages( mTempVisiblePagesRange );
			int minDistance = Integer.MAX_VALUE;
			int minIndex = indexOfChild( mDragView );
			for( int i = mTempVisiblePagesRange[0] ; i <= mTempVisiblePagesRange[1] ; i++ )
			{
				View page = getPageAt( i );
				int pageX = (int)( page.getLeft() + page.getMeasuredWidth() / 2 );
				int d = Math.abs( dragX - pageX );
				if( d < minDistance )
				{
					minIndex = i;
					minDistance = d;
				}
			}
			return minIndex;
		}
		return -1;
	}
	
	@Override
	public boolean onTouchEvent(
			MotionEvent ev )
	{
		if( DISABLE_TOUCH_INTERACTION )
		{
			return false;
		}
		//zhujieping add start	//换主题不重启
		if( Launcher.isThemeChanging() )
		{
			return false;
		}
		//zhujieping add end
		super.onTouchEvent( ev );
		// Skip touch handling if there are no pages to swipe
		if( getChildCount() <= 0 )
			return super.onTouchEvent( ev );
		acquireVelocityTrackerAndAddMovement( ev );
		final int action = ev.getAction();
		switch( action & MotionEvent.ACTION_MASK )
		{
			case MotionEvent.ACTION_DOWN:
				if( !mScroller.isFinished() )
				{
					mScroller.abortAnimation();
				}
				// Remember where the motion event started
				mDownMotionX = mLastMotionX = ev.getX();
				mDownMotionY = mLastMotionY = ev.getY();
				mDownScrollX = getScrollX();
				float[] p = mapPointFromViewToParent( this , mLastMotionX , mLastMotionY );
				mParentDownMotionX = p[0];
				mParentDownMotionY = p[1];
				mLastMotionXRemainder = 0;
				mTotalMotionX = 0;
				mActivePointerId = ev.getPointerId( 0 );
				if( mTouchState == TOUCH_STATE_SCROLLING )
				{
					pageBeginMoving();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if( mTouchState == TOUCH_STATE_SCROLLING )
				{
					// Scroll to follow the motion event
					final int pointerIndex = ev.findPointerIndex( mActivePointerId );
					if( pointerIndex == -1 )
						return true;
					final float x = ev.getX( pointerIndex );
					float deltaX = mLastMotionX + mLastMotionXRemainder - x;
					// zhangjin@2015/09/14 ADD START
					deltaX *= PAGE_ON_TOUCH_ACCELERATE;
					// zhangjin@2015/09/14 ADD END
					mTotalMotionX += Math.abs( deltaX );
					// Only scroll and update mLastMotionX if we have moved some discrete amount.  We
					// keep the remainder because we are actually testing if we've moved from the last
					// scrolled position (which is discrete).
					if( Math.abs( deltaX ) >= 1.0f )
					{
						mTouchX += deltaX;
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						if( !mDeferScrollUpdate )
						{
							scrollBy( (int)deltaX , 0 );
							if( DEBUG )
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.d( TAG , StringUtils.concat( "onTouchEvent().Scrolling: " , deltaX ) );
						}
						else
						{
							invalidate();
						}
						mLastMotionX = x;
						mLastMotionXRemainder = deltaX - (int)deltaX;
					}
					else
					{
						awakenScrollBars();
					}
				}
				else if( mTouchState == TOUCH_STATE_REORDERING )
				{
					// Update the last motion position
					mLastMotionX = ev.getX();
					mLastMotionY = ev.getY();
					// Update the parent down so that our zoom animations take this new movement into
					// account
					float[] pt = mapPointFromViewToParent( this , mLastMotionX , mLastMotionY );
					mParentDownMotionX = pt[0];
					mParentDownMotionY = pt[1];
					updateDragViewTranslationDuringDrag();
					// Find the closest page to the touch point
					final int dragViewIndex = indexOfChild( mDragView );
					// Change the drag view if we are hovering over the drop target
					if( DEBUG )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.d( TAG , StringUtils.concat(
									"mLastMotionX:" ,
									mLastMotionX ,
									"-mLastMotionY:" ,
									mLastMotionY ,
									"-mParentDownMotionX:" ,
									mParentDownMotionX ,
									"-mParentDownMotionY:" ,
									mParentDownMotionY ) );
						}
					}
					//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
					boolean isReorder = true;
					if( mDragCellLayoutListener != null )
					{
						if( mDragCellLayoutListener.isInDeleteDropTarget( ev ) )
						{
							isReorder = false;
						}
					}
					//zhujieping add end
					final int pageUnderPointIndex = getNearestHoverOverPageIndex();
					//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
					if( !isChildCanRecording( pageUnderPointIndex ) )
					{
						isReorder = false;
					}
					//zhujieping add end
					if( isReorder && pageUnderPointIndex > -1 && pageUnderPointIndex != indexOfChild( mDragView ) )
					{
						mTempVisiblePagesRange[0] = 0;
						mTempVisiblePagesRange[1] = getPageCount() - 1;
						getOverviewModePages( mTempVisiblePagesRange );
						if( mTempVisiblePagesRange[0] <= pageUnderPointIndex && pageUnderPointIndex <= mTempVisiblePagesRange[1] && pageUnderPointIndex != mSidePageHoverIndex && mScroller
								.isFinished() )
						{
							mSidePageHoverIndex = pageUnderPointIndex;
							mSidePageHoverRunnable = new Runnable() {
								
								@Override
								public void run()
								{
									// Setup the scroll to the correct page before we swap the views
									snapToPage( pageUnderPointIndex );
									// For each of the pages between the paged view and the drag view,
									// animate them from the previous position to the new position in
									// the layout (as a result of the drag view moving in the layout)
									int shiftDelta = ( dragViewIndex < pageUnderPointIndex ) ? -1 : 1;
									int lowerIndex = ( dragViewIndex < pageUnderPointIndex ) ? dragViewIndex + 1 : pageUnderPointIndex;
									int upperIndex = ( dragViewIndex > pageUnderPointIndex ) ? dragViewIndex - 1 : pageUnderPointIndex;
									for( int i = lowerIndex ; i <= upperIndex ; ++i )
									{
										View v = getChildAt( i );
										// dragViewIndex < pageUnderPointIndex, so after we remove the
										// drag view all subsequent views to pageUnderPointIndex will
										// shift down.
										int oldX = getViewportOffsetX() + getChildOffset( i );
										int newX = getViewportOffsetX() + getChildOffset( i + shiftDelta );
										// Animate the view translation from its old position to its new
										// position
										AnimatorSet anim = (AnimatorSet)v.getTag( ANIM_TAG_KEY );
										if( anim != null )
										{
											anim.cancel();
										}
										v.setTranslationX( oldX - newX );
										anim = new AnimatorSet();
										anim.setDuration( REORDERING_REORDER_REPOSITION_DURATION );
										anim.playTogether( ObjectAnimator.ofFloat( v , "translationX" , 0f ) );
										anim.start();
										v.setTag( anim );
									}
									removeView( mDragView );
									onRemoveView( mDragView , false );
									addView( mDragView , pageUnderPointIndex );
									onAddView( mDragView , pageUnderPointIndex );
									mSidePageHoverIndex = -1;
									mPageIndicator.setActiveMarker( getNextPage() );
								}
							};
							postDelayed( mSidePageHoverRunnable , REORDERING_SIDE_PAGE_HOVER_TIMEOUT );
						}
					}
					else
					{
						removeCallbacks( mSidePageHoverRunnable );
						mSidePageHoverIndex = -1;
					}
				}
				else
				{
					determineScrollingStart( ev );
				}
				if( !isSuccessCutPage )
				{
					//WangLei start //切页特效可配置
					//if( mLauncher.getSelect_efffects_workspace() == new EffectFactory( curView ).getAllEffects().size() )//WangLei del
					// zhangjin@2015/08/25 UPD START
					//EffectFactory effectFactory = new EffectFactory( curView , mLauncher );
					if( mEffectFactory == null )
					{
						mEffectFactory = new EffectFactory( curView , mLauncher );
					}
					// zhangjin@2015/08/25 UPD END
					if( mEffectFactory.isRandomEffect( mLauncher.getSelect_efffects_workspace() ) )//WangLei add
					//WangLei end
					{
						isSuccessCutPage = true;
						//WangLei del start //桌面和主菜单特效的分离
						//mLauncher.getmAppsCustomizeContent().initAnimationStyle( mLauncher.getmAppsCustomizeContent() );
						//mLauncher.getmAppsCustomizeContent().restoreAppsCustomizePagedView();
						//WangLei del end
						mLauncher.getWorkspace().initAnimationStyle( mLauncher.getWorkspace() );
						mLauncher.getWorkspace().restoreWorkspace();
					}
					//WangLei add start //桌面和主菜单特效的分离
					/**双层模式且当前界面为主菜单，如果选择的特效为随机，每次滑动页面时都更新特效，桌面和主菜单的特效不会同时更新*/
					if(
					//
					( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
					//
					&& mLauncher.getWorkspace().getState() == Workspace.State.SMALL
					//
					&& mEffectFactory.isAllAppRandomEffect( mLauncher.getSelect_effects_applist() )
					//
					)
					{
						isSuccessCutPage = true;
						AppsCustomizePagedView appsCustomizeContent = mLauncher.getmAppsCustomizeContent();
						if( appsCustomizeContent != null && appsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Applications )
						{
							mLauncher.getmAppsCustomizeContent().initAnimationStyle( mLauncher.getmAppsCustomizeContent() );
							mLauncher.getmAppsCustomizeContent().restoreAppsCustomizePagedView();
						}
					}
					//WangLei add end
				}
				break;
			case MotionEvent.ACTION_UP:
				if( mTouchState == TOUCH_STATE_SCROLLING )
				{
					final int activePointerId = mActivePointerId;
					final int pointerIndex = ev.findPointerIndex( activePointerId );
					final float x = ev.getX( pointerIndex );
					final VelocityTracker velocityTracker = mVelocityTracker;
					velocityTracker.computeCurrentVelocity( 1000 , mMaximumVelocity );
					int velocityX = (int)velocityTracker.getXVelocity( activePointerId );
					final int deltaX = (int)( x - mDownMotionX );
					final int pageWidth = getPageAt( mCurrentPage ).getMeasuredWidth();
					boolean isSignificantMove = Math.abs( deltaX ) > pageWidth * SIGNIFICANT_MOVE_THRESHOLD;
					mTotalMotionX += Math.abs( mLastMotionX + mLastMotionXRemainder - x );
					// zhangjin@2015/07/22 UPD START
					//boolean isFling = mTotalMotionX > MIN_LENGTH_FOR_FLING && Math.abs( velocityX ) > mFlingThresholdVelocity;
					boolean isFling = mTotalMotionX > ( MIN_LENGTH_FOR_FLING * mFlingThresholdVelocity / ( Math.abs( velocityX ) + 1 ) ) && Math.abs( velocityX ) > mFlingThresholdVelocity;
					// zhangjin@2015/07/22 UPD END
					if( !mFreeScroll )
					{
						// In the case that the page is moved far to one direction and then is flung
						// in the opposite direction, we use a threshold to determine whether we should
						// just return to the starting page, or if we should skip one further.
						boolean returnToOriginalPage = false;
						if( Math.abs( deltaX ) > pageWidth * RETURN_TO_ORIGINAL_PAGE_THRESHOLD && Math.signum( velocityX ) != Math.signum( deltaX ) && isFling )
						{
							returnToOriginalPage = true;
						}
						int finalPage;
						// We give flings precedence over large moves, which is why we short-circuit our
						// test for a large move if a fling has been registered. That is, a large
						// move to the left and fling to the right will register as a fling to the right.
						final boolean isRtl = isLayoutRtl();
						boolean isDeltaXLeft = isRtl ? deltaX > 0 : deltaX < 0;
						boolean isVelocityXLeft = isRtl ? velocityX > 0 : velocityX < 0;
						if( ( ( isSignificantMove && !isDeltaXLeft && !isFling ) || ( isFling && !isVelocityXLeft ) ) && ( isLoop() || mCurrentPage > 0 ) )
						{
							if( isLoop() && mCurrentPage == 0 && !returnToOriginalPage )//zhujieping,支持循环滑页时，从第一页到最后一页松手时，可被看做是当前是最后一页回弹回去，下面同理
							{
								finalPage = getPrePageIndex( mCurrentPage );
								float nowOver = mLastMotionX + mLastMotionXRemainder - x + mUnboundedScrollX;
								if( isLayoutRtl() )
								{
									setScrollX( (int)nowOver - getViewportWidth() * getPageCount() );
								}
								else
								{
									setScrollX( (int)nowOver + getViewportWidth() * getPageCount() );
								}
							}
							else
							{
								finalPage = returnToOriginalPage ? mCurrentPage : mCurrentPage - 1;
							}
							snapToPageWithVelocity( finalPage , velocityX );
						}
						else if( ( ( isSignificantMove && isDeltaXLeft && !isFling ) || ( isFling && isVelocityXLeft ) ) && ( isLoop() || mCurrentPage < getChildCount() - 1 ) )
						{
							if( isLoop() && mCurrentPage == getChildCount() - 1 && !returnToOriginalPage )
							{
								finalPage = getNextPageIndex( mCurrentPage );
								float nowOver = mLastMotionX + mLastMotionXRemainder - x + mUnboundedScrollX;
								if( isLayoutRtl() )
								{
									setScrollX( (int)nowOver + getViewportWidth() * getPageCount() );
								}
								else
								{
									setScrollX( (int)nowOver - getViewportWidth() * getPageCount() );
								}
							}
							else
							{
								finalPage = returnToOriginalPage ? mCurrentPage : mCurrentPage + 1;
							}
							snapToPageWithVelocity( finalPage , velocityX );
						}
						else
						{
							snapToDestination();
						}
					}
					else if( mTouchState == TOUCH_STATE_PREV_PAGE )
					{
						// at this point we have not moved beyond the touch slop
						// (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
						// we can just page
						int nextPage = Math.max( 0 , mCurrentPage - 1 );
						if( nextPage != mCurrentPage )
						{
							snapToPage( nextPage );
						}
						else
						{
							snapToDestination();
						}
					}
					else
					{
						if( !mScroller.isFinished() )
						{
							mScroller.abortAnimation();
						}
						float scaleX = getScaleX();
						int vX = (int)( -velocityX * scaleX );
						int initialScrollX = (int)( getScrollX() * scaleX );
						//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
						if( mFreeScroll //
								|| isOverViewModel )
						{
							if( !( ( getScrollX() >= mFreeScrollMaxScrollX && vX > 0 ) || ( getScrollX() <= mFreeScrollMinScrollX && vX < 0 ) ) )
							{
								mScroller.fling( initialScrollX , getScrollY() , vX , 0 , Integer.MIN_VALUE , Integer.MAX_VALUE , 0 , 0 );
								invalidate();
							}
						}
						//zhujieping add end
						else
						{
							mScroller.fling( initialScrollX , getScrollY() , vX , 0 , Integer.MIN_VALUE , Integer.MAX_VALUE , 0 , 0 );
							invalidate();
						}
					}
				}
				else if( mTouchState == TOUCH_STATE_NEXT_PAGE )
				{
					// at this point we have not moved beyond the touch slop
					// (otherwise mTouchState would be TOUCH_STATE_SCROLLING), so
					// we can just page
					int nextPage = Math.min( getChildCount() - 1 , mCurrentPage + 1 );
					if( nextPage != mCurrentPage )
					{
						snapToPage( nextPage );
					}
					else
					{
						snapToDestination();
					}
				}
				else if( mTouchState == TOUCH_STATE_REORDERING )
				{
					// Update the last motion position
					mLastMotionX = ev.getX();
					mLastMotionY = ev.getY();
					// Update the parent down so that our zoom animations take this new movement into
					// account
					float[] pt = mapPointFromViewToParent( this , mLastMotionX , mLastMotionY );
					mParentDownMotionX = pt[0];
					mParentDownMotionY = pt[1];
					updateDragViewTranslationDuringDrag();
					//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
					removeCallbacks( mSidePageHoverRunnable );
					resetTouchState();
					isSuccessCutPage = false;
					if( mDragCellLayoutListener != null )
					{
						if( mDragCellLayoutListener.isInDeleteDropTarget( ev ) )
						{
							handleDeleteDragView( mDragView );
							mDragCellLayoutListener.onDropDeleteDropTarget();
							mDragView = null;
						}
					}
					break;
					//zhujieping add end
				}
				else
				{
					if( !mCancelTap )
					{
						onUnhandledTap( ev );
					}
				}
				// Remove the callback to wait for the side page hover timeout
				removeCallbacks( mSidePageHoverRunnable );
				// End any intermediate reordering states
				resetTouchState();
				isSuccessCutPage = false;
				break;
			case MotionEvent.ACTION_CANCEL:
				if( mTouchState == TOUCH_STATE_SCROLLING )
				{
					snapToDestination();
				}
				resetTouchState();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp( ev );
				releaseVelocityTracker();
				break;
		}
		return true;
	}
	
	public void onRemoveView(
			View v ,
			boolean deletePermanently )
	{
	}
	
	public void onRemoveViewAnimationCompleted()
	{
	}
	
	public void onAddView(
			View v ,
			int index )
	{
	}
	
	private void resetTouchState()
	{
		releaseVelocityTracker();
		endReordering();
		mCancelTap = false;
		mTouchState = TOUCH_STATE_REST;
		mActivePointerId = INVALID_POINTER;
	}
	
	protected void onUnhandledTap(
			MotionEvent ev )
	{
		( (Launcher)getContext() ).onClick( this );
	}
	
	@Override
	public boolean onGenericMotionEvent(
			MotionEvent event )
	{
		if( ( event.getSource() & InputDevice.SOURCE_CLASS_POINTER ) != 0 )
		{
			switch( event.getAction() )
			{
				case MotionEvent.ACTION_SCROLL:
				{
					// Handle mouse (or ext. device) by shifting the page depending on the scroll
					final float vscroll;
					final float hscroll;
					if( ( event.getMetaState() & KeyEvent.META_SHIFT_ON ) != 0 )
					{
						vscroll = 0;
						hscroll = event.getAxisValue( MotionEvent.AXIS_VSCROLL );
					}
					else
					{
						vscroll = -event.getAxisValue( MotionEvent.AXIS_VSCROLL );
						hscroll = event.getAxisValue( MotionEvent.AXIS_HSCROLL );
					}
					if( hscroll != 0 || vscroll != 0 )
					{
						boolean isForwardScroll = isLayoutRtl() ? ( hscroll < 0 || vscroll < 0 ) : ( hscroll > 0 || vscroll > 0 );
						if( isForwardScroll )
						{
							//cheyingkun start //光感循环切页(德盛伟业)
							//							scrollRight();//cheyingkun del
							scrollRight( false );//cheyingkun add
							//cheyingkun end
						}
						else
						{
							//cheyingkun start //光感循环切页(德盛伟业)
							//							scrollLeft();//cheyingkun del
							scrollLeft( false );//cheyingkun add
							//cheyingkun end
						}
						return true;
					}
				}
			}
		}
		return super.onGenericMotionEvent( event );
	}
	
	private void acquireVelocityTrackerAndAddMovement(
			MotionEvent ev )
	{
		if( mVelocityTracker == null )
		{
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement( ev );
	}
	
	private void releaseVelocityTracker()
	{
		if( mVelocityTracker != null )
		{
			mVelocityTracker.clear();
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
	private void onSecondaryPointerUp(
			MotionEvent ev )
	{
		final int pointerIndex = ( ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK ) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId( pointerIndex );
		if( pointerId == mActivePointerId )
		{
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionX = mDownMotionX = ev.getX( newPointerIndex );
			mLastMotionY = ev.getY( newPointerIndex );
			mLastMotionXRemainder = 0;
			mActivePointerId = ev.getPointerId( newPointerIndex );
			if( mVelocityTracker != null )
			{
				mVelocityTracker.clear();
			}
		}
	}
	
	@Override
	public void requestChildFocus(
			View child ,
			View focused )
	{
		super.requestChildFocus( child , focused );
		int page = indexToPage( indexOfChild( child ) );
		if( page >= 0 && page != getCurrentPage() && !isInTouchMode() )
		{
			snapToPage( page );
		}
	}
	
	protected int getChildWidth(
			int index )
	{
		final int measuredWidth = getPageAt( index ).getMeasuredWidth();
		final int minWidth = mMinimumWidth;
		return ( minWidth > measuredWidth ) ? minWidth : measuredWidth;
		//		return getPageAt( index ).getMeasuredWidth();
	}
	
	int getPageNearestToPoint(
			float x )
	{
		int index = 0;
		for( int i = 0 ; i < getChildCount() ; ++i )
		{
			if( x < getChildAt( i ).getRight() - getScrollX() )
			{
				return index;
			}
			else
			{
				index++;
			}
		}
		return Math.min( index , getChildCount() - 1 );
	}
	
	public int getPageNearestToCenterOfScreen(
			float threshold )
	{
		int minDistanceFromScreenCenter = Integer.MAX_VALUE;
		int minDistanceFromScreenCenterIndex = -1;
		int screenCenter = getViewportOffsetX() + getScrollX() + ( getViewportWidth() / 2 );
		final int childCount = getChildCount();
		for( int i = 0 ; i < childCount ; ++i )
		{
			View layout = (View)getPageAt( i );
			int childWidth = layout.getMeasuredWidth();
			int halfChildWidth = (int)( childWidth * threshold );
			int childCenter = getViewportOffsetX() + getChildOffset( i ) + halfChildWidth;
			int distanceFromScreenCenter = Math.abs( childCenter - screenCenter );
			if( distanceFromScreenCenter < minDistanceFromScreenCenter )
			{
				minDistanceFromScreenCenter = distanceFromScreenCenter;
				minDistanceFromScreenCenterIndex = i;
			}
		}
		return minDistanceFromScreenCenterIndex;
	}
	
	protected void snapToDestination()
	{
		snapToPage( getPageNearestToCenterOfScreen( SIGNIFICANT_MOVE_THRESHOLD ) , PAGE_SNAP_ANIMATION_DURATION );
	}
	
	private static class ScrollInterpolator implements Interpolator
	{
		
		public ScrollInterpolator()
		{
		}
		
		public float getInterpolation(
				float t )
		{
			t -= 1.0f;
			return t * t * t * t * t + 1;
		}
	}
	
	// We want the duration of the page snap animation to be influenced by the distance that
	// the screen has to travel, however, we don't want this duration to be effected in a
	// purely linear fashion. Instead, we use this method to moderate the effect that the distance
	// of travel has on the overall snap duration.
	float distanceInfluenceForSnapDuration(
			float f )
	{
		f -= 0.5f; // center the values about 0.
		f *= 0.3f * Math.PI / 2.0f;
		return (float)Math.sin( f );
	}
	
	protected void snapToPageWithVelocity(
			int whichPage ,
			int velocity )
	{
		whichPage = Math.max( 0 , Math.min( whichPage , getChildCount() - 1 ) );
		int halfScreenSize = getViewportWidth() / 2;
		final int newX = getScrollForPage( whichPage );//getScrollForPage( whichPage );zhujieping modify
		int delta = newX - mUnboundedScrollX;
		int duration = 0;
		// zhangjin@2015/07/22 UPD START
		//if( Math.abs( velocity ) < mMinFlingVelocity )
		//{
		//	// If the velocity is low enough, then treat this more as an automatic page advance
		//	// as opposed to an apparent physical response to flinging
		//	snapToPage( whichPage , PAGE_SNAP_ANIMATION_DURATION );
		//	return;
		//}
		if( Math.abs( velocity ) < mMinFlingVelocity )
		{
			velocity = mMinFlingVelocity;
		}
		// zhangjin@2015/07/22 UPD END
		// Here we compute a "distance" that will be used in the computation of the overall
		// snap duration. This is a function of the actual distance that needs to be traveled;
		// we keep this value close to half screen size in order to reduce the variance in snap
		// duration as a function of the distance the page needs to travel.
		float distanceRatio = Math.min( 1f , 1.0f * Math.abs( delta ) / ( 2 * halfScreenSize ) );
		float distance = halfScreenSize + halfScreenSize * distanceInfluenceForSnapDuration( distanceRatio );
		velocity = Math.abs( velocity );
		velocity = Math.max( mMinSnapVelocity , velocity );
		// we want the page's snap velocity to approximately match the velocity at which the
		// user flings, so we scale the duration by a value near to the derivative of the scroll
		// interpolator at zero, ie. 5. We use 4 to make it a little slower.
		//<调整划屏的流畅度> hongqingquan@2015-03-13 modify begin
		//duration = 4 * Math.round( 1000 * Math.abs( distance / velocity ) );
		duration = 5 * Math.round( 1000 * Math.abs( distance / velocity ) );
		//<调整划屏的流畅度> hongqingquan@2015-03-13 modify end
		snapToPage( whichPage , delta , duration );
	}
	
	public void snapToPage(
			int whichPage )
	{
		snapToPage( whichPage , PAGE_SNAP_ANIMATION_DURATION );
	}
	
	protected void snapToPageImmediately(
			int whichPage )
	{
		snapToPage( whichPage , PAGE_SNAP_ANIMATION_DURATION , true );
	}
	
	protected void snapToPage(
			int whichPage ,
			int duration )
	{
		snapToPage( whichPage , duration , false );
	}
	
	public void snapToPage(
			int whichPage ,
			int duration ,
			boolean immediate )
	{
		whichPage = Math.max( 0 , Math.min( whichPage , getPageCount() - 1 ) );
		int newX = getScrollForPage( whichPage );//getScrollForPage( whichPage );zhujieping modify
		final int sX = mUnboundedScrollX;
		final int delta = newX - sX;
		snapToPage( whichPage , delta , duration , immediate );
	}
	
	protected void snapToPage(
			int whichPage ,
			int delta ,
			int duration )
	{
		snapToPage( whichPage , delta , duration , false );
	}
	
	protected void snapToPage(
			int whichPage ,
			int delta ,
			int duration ,
			boolean immediate )
	{
		mNextPage = whichPage;
		View focusedChild = getFocusedChild();
		if( focusedChild != null && whichPage != mCurrentPage && focusedChild == getPageAt( mCurrentPage ) )
		{
			focusedChild.clearFocus();
		}
		pageBeginMoving();
		awakenScrollBars( duration );
		if( immediate )
		{
			duration = 0;
		}
		else if( duration == 0 )
		{
			duration = Math.abs( delta );
		}
		if( !mScroller.isFinished() )
		{
			mScroller.abortAnimation();
		}
		mScroller.startScroll( mUnboundedScrollX , 0 , delta , 0 , duration );
		notifyPageSwitchListener();
		// Trigger a compute() to finish switching pages if necessary
		if( immediate )
		{
			computeScroll();
		}
		// Defer loading associated pages until the scroll settles
		mDeferLoadAssociatedPagesUntilScrollCompletes = true;
		mForceScreenScrolled = true;
		invalidate();
	}
	
	//cheyingkun start //光感循环切页(德盛伟业)
	//cheyingkun del start
	//	public void scrollLeft()
	//	{
	//		if( getNextPage() > 0 )
	//			snapToPage( getNextPage() - 1 );
	//	}
	//	
	//	public void scrollRight()
	//	{
	//		if( getNextPage() < getChildCount() - 1 )
	//			snapToPage( getNextPage() + 1 );
	//	}
	//cheyingkun del end
	//cheyingkun add start
	public void scrollLeft(
			boolean isLoop )
	{
		if( getNextPage() > 0 )
			snapToPage( getNextPage() - 1 );
		if( isLoop )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "getCurrentPage() : " , getCurrentPage() , " getChildCount(): " , getChildCount() ) );
			if( getCurrentPage() == 0 )
			{
				mUnboundedScrollX = 0;
				if( getChildCount() > 1 )
				{
					mUnboundedScrollX = getScrollForPage( 1 ) * ( getChildCount() );
				}
				snapToPage( getChildCount() - 1 );
				mCurrentPage = getChildCount() - 1;
			}
		}
	}
	
	public void scrollRight(
			boolean isLoop )
	{
		if( getNextPage() < getChildCount() - 1 )
			snapToPage( getNextPage() + 1 );
		if( isLoop )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "getCurrentPage() : " , getCurrentPage() , " getChildCount(): " , getChildCount() ) );
			if( getCurrentPage() == getChildCount() - 1 )
			{
				mUnboundedScrollX = 0;
				if( getChildCount() > 1 )
				{
					mUnboundedScrollX = -getScrollForPage( 1 );
				}
				snapToPage( 0 );
				mCurrentPage = 0;
			}
		}
	}
	
	//cheyingkun add end
	//cheyingkun end
	public int getPageForView(
			View v )
	{
		int result = -1;
		if( v != null )
		{
			ViewParent vp = v.getParent();
			int count = getChildCount();
			for( int i = 0 ; i < count ; i++ )
			{
				if( vp == getPageAt( i ) )
				{
					return i;
				}
			}
		}
		return result;
	}
	
	/**
	 * @return True is long presses are still allowed for the current touch
	 */
	public boolean allowLongPress(
			View v )//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）。
	{
		return mAllowLongPress;
	}
	
	@Override
	public boolean performLongClick()
	{
		mCancelTap = true;
		return super.performLongClick();
	}
	
	/**
	 * Set true to allow long-press events to be triggered, usually checked by
	 * {@link Launcher} to accept or block dpad-initiated long-presses.
	 */
	public void setAllowLongPress(
			boolean allowLongPress )
	{
		mAllowLongPress = allowLongPress;
	}
	
	public static class SavedState extends BaseSavedState
	{
		
		int currentPage = -1;
		
		SavedState(
				Parcelable superState )
		{
			super( superState );
		}
		
		private SavedState(
				Parcel in )
		{
			super( in );
			currentPage = in.readInt();
		}
		
		@Override
		public void writeToParcel(
				Parcel out ,
				int flags )
		{
			super.writeToParcel( out , flags );
			out.writeInt( currentPage );
		}
		
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			
			public SavedState createFromParcel(
					Parcel in )
			{
				return new SavedState( in );
			}
			
			public SavedState[] newArray(
					int size )
			{
				return new SavedState[size];
			}
		};
	}
	
	public void loadAssociatedPages(
			int page )
	{
		loadAssociatedPages( page , false );
	}
	
	public void loadAssociatedPages(
			int page ,
			boolean immediateAndOnly )
	{
		if( mContentIsRefreshable )
		{
			final int count = getChildCount();
			if( page < count )
			{
				int lowerPageBound = getAssociatedLowerPageBound( page );
				int upperPageBound = getAssociatedUpperPageBound( page );
				if( DEBUG )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( TAG , StringUtils.concat( "loadAssociatedPages,lowerPageBound:" , lowerPageBound , "-upperPageBound:" , upperPageBound , "-page:" , page ) );
						Log.d( "cyk:bug_0014477" , StringUtils.concat(
								"loadAssociatedPages,lowerPageBound:" ,
								lowerPageBound ,
								"-upperPageBound:" ,
								upperPageBound ,
								"-page:" ,
								page ,
								"-immediateAndOnly:" ,
								immediateAndOnly ,
								"-count: " ,
								count ) );
					}
				}
				// First, clear any pages that should no longer be loaded
				if( lowerPageBound > upperPageBound )
				{
					for( int i = 0 ; i < count ; ++i )
					{
						IPage layout = (IPage)getPageAt( i );
						if( ( upperPageBound < i && i < lowerPageBound ) )
						{
							if( layout.getPageChildCount() > 0 )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.e( "cyk:bug_0014477" , StringUtils.concat( "loadAssociatedPages removeallviews1 i = " , i ) );
								layout.removeAllViewsOnPage();
							}
							mDirtyPageContent.set( i , true );
						}
					}
					// Next, load any new pages
					for( int i = 0 ; i < count ; ++i )
					{
						if( ( i != page ) && immediateAndOnly )
						{
							continue;
						}
						if( i <= upperPageBound || i >= lowerPageBound )
						{
							if( mDirtyPageContent.get( i ) )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.e( "cyk:bug_0014477" , StringUtils.concat( "loadAssociatedPages syncPageItems2 i = " , i ) );
								syncPageItems( i , ( i == page ) && immediateAndOnly );
								mDirtyPageContent.set( i , false );
							}
						}
					}
				}
				else
				{
					for( int i = 0 ; i < count ; ++i )
					{
						IPage layout = (IPage)getPageAt( i );
						if( ( i < lowerPageBound ) || ( i > upperPageBound ) )
						{
							if( layout.getPageChildCount() > 0 )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.e( "cyk:bug_0014477" , StringUtils.concat( "loadAssociatedPages removeallviews3 i = " , i ) );
								layout.removeAllViewsOnPage();
							}
							mDirtyPageContent.set( i , true );
						}
					}
					// Next, load any new pages
					for( int i = 0 ; i < count ; ++i )
					{
						if( ( i != page ) && immediateAndOnly )
						{
							continue;
						}
						if( lowerPageBound <= i && i <= upperPageBound )
						{
							if( mDirtyPageContent.get( i ) )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.e( "cyk:bug_0014477" , StringUtils.concat( "loadAssociatedPages syncPageItems4 i = " , i ) );
								syncPageItems( i , ( i == page ) && immediateAndOnly );
								mDirtyPageContent.set( i , false );
							}
						}
					}
				}
			}
		}
	}
	
	protected int getAssociatedLowerPageBound(
			int page )
	{
		if( isLoop() )
		{
			return getPrePageIndex( page );
		}
		return Math.max( 0 , page - 1 );
	}
	
	protected int getAssociatedUpperPageBound(
			int page )
	{
		if( isLoop() )
		{
			return getNextPageIndex( page );
		}
		final int count = getChildCount();
		return Math.min( page + 1 , count - 1 );
	}
	
	/**
	 * This method is called ONLY to synchronize the number of pages that the paged view has.
	 * To actually fill the pages with information, implement syncPageItems() below.  It is
	 * guaranteed that syncPageItems() will be called for a particular page before it is shown,
	 * and therefore, individual page items do not need to be updated in this method.
	 */
	public abstract void syncPages();
	
	/**
	 * This method is called to synchronize the items that are on a particular page.  If views on
	 * the page can be reused, then they should be updated within this method.
	 */
	public abstract void syncPageItems(
			int page ,
			boolean immediate );
	
	protected void invalidatePageData()
	{
		invalidatePageData( -1 , false );
	}
	
	protected void invalidatePageData(
			int currentPage )
	{
		invalidatePageData( currentPage , false );
	}
	
	protected void invalidatePageData(
			int currentPage ,
			boolean immediateAndOnly )
	{
		if( !mIsDataReady )
		{
			return;
		}
		if( mContentIsRefreshable )
		{
			// Force all scrolling-related behavior to end
			mScroller.forceFinished( true );
			mNextPage = INVALID_PAGE;
			// Update all the pages
			syncPages();
			// We must force a measure after we've loaded the pages to update the content width and
			// to determine the full scroll width
			measure( MeasureSpec.makeMeasureSpec( getMeasuredWidth() , MeasureSpec.EXACTLY ) , MeasureSpec.makeMeasureSpec( getMeasuredHeight() , MeasureSpec.EXACTLY ) );
			// Set a new page as the current page if necessary
			if( currentPage > -1 )
			{
				setCurrentPage( Math.min( getPageCount() - 1 , currentPage ) );
			}
			// Mark each of the pages as dirty
			final int count = getChildCount();
			mDirtyPageContent.clear();
			for( int i = 0 ; i < count ; ++i )
			{
				mDirtyPageContent.add( true );
			}
			// Load any pages that are necessary for the current window of views
			loadAssociatedPages( mCurrentPage , immediateAndOnly );
			requestLayout();
		}
		if( isPageMoving() )
		{
			// If the page is moving, then snap it to the final position to ensure we don't get
			// stuck between pages
			snapToDestination();
		}
	}
	
	// Animate the drag view back to the original position
	void animateDragViewToOriginalPosition()
	{
		if( mDragView != null )
		{
			AnimatorSet anim = new AnimatorSet();
			anim.setDuration( REORDERING_DROP_REPOSITION_DURATION );
			anim.playTogether(
					ObjectAnimator.ofFloat( mDragView , "translationX" , 0f ) ,
					ObjectAnimator.ofFloat( mDragView , "translationY" , 0f ) ,
					ObjectAnimator.ofFloat( mDragView , "scaleX" , 1f ) ,
					ObjectAnimator.ofFloat( mDragView , "scaleY" , 1f ) );
			anim.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					onPostReorderingAnimationCompleted();
				}
			} );
			anim.start();
		}
	}
	
	protected void onStartReordering()
	{
		// Set the touch state to reordering (allows snapping to pages, dragging a child, etc.)
		mTouchState = TOUCH_STATE_REORDERING;
		mIsReordering = true;
		// We must invalidate to trigger a redraw to update the layers such that the drag view
		// is always drawn on top
		invalidate();
	}
	
	private void onPostReorderingAnimationCompleted()
	{
		// Trigger the callback when reordering has settled
		--mPostReorderingPreZoomInRemainingAnimationCount;
		if( mPostReorderingPreZoomInRunnable != null && mPostReorderingPreZoomInRemainingAnimationCount == 0 )
		{
			mPostReorderingPreZoomInRunnable.run();
			mPostReorderingPreZoomInRunnable = null;
		}
	}
	
	protected void onEndReordering()
	{
		mIsReordering = false;
	}
	
	public boolean startReordering(
			View v )
	{
		int dragViewIndex = indexOfChild( v );
		if( mTouchState != TOUCH_STATE_REST )
			return false;
		mTempVisiblePagesRange[0] = 0;
		mTempVisiblePagesRange[1] = getPageCount() - 1;
		getOverviewModePages( mTempVisiblePagesRange );
		mReorderingStarted = true;
		// Check if we are within the reordering range
		if( mTempVisiblePagesRange[0] <= dragViewIndex && dragViewIndex <= mTempVisiblePagesRange[1] )
		{
			// Find the drag view under the pointer
			mDragView = getChildAt( dragViewIndex );
			mDragView.animate().scaleX( 1.15f ).scaleY( 1.15f ).setDuration( 100 ).start();
			mDragViewBaselineLeft = mDragView.getLeft();
			disableFreeScroll( -1 );
			onStartReordering();
			return true;
		}
		return false;
	}
	
	public boolean isReordering(
			boolean testTouchState )
	{
		boolean state = mIsReordering;
		if( testTouchState )
		{
			state &= ( mTouchState == TOUCH_STATE_REORDERING );
		}
		return state;
	}
	
	void endReordering()
	{
		// For simplicity, we call endReordering sometimes even if reordering was never started.
		// In that case, we don't want to do anything.
		if( !mReorderingStarted )
			return;
		mReorderingStarted = false;
		// If we haven't flung-to-delete the current child, then we just animate the drag view
		// back into position
		final Runnable onCompleteRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				onEndReordering();
			}
		};
		mPostReorderingPreZoomInRunnable = new Runnable() {
			
			public void run()
			{
				onCompleteRunnable.run();
				enableFreeScroll();
			};
		};
		mPostReorderingPreZoomInRemainingAnimationCount = NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT;
		// Snap to the current page
		snapToPage( indexOfChild( mDragView ) , 0 );
		// Animate the drag view back to the front position
		animateDragViewToOriginalPosition();
	}
	
	private static final int ANIM_TAG_KEY = 100;
	
	@Override
	public boolean onHoverEvent(
			android.view.MotionEvent event )
	{
		return false;
	}
	
	protected int getScaledMeasuredWidth(
			IEffect child )
	{
		// This functions are called enough times that it actually makes a difference in the
		// profiler -- so just inline the max() here
		final int measuredWidth = child.getMeasuredWidth();
		final int minWidth = mMinimumWidth;
		final int maxWidth = ( minWidth > measuredWidth ) ? minWidth : measuredWidth;
		return (int)( maxWidth * mLayoutScale + 0.5f );
	}
	
	protected abstract void restView();
	
	//xiatian add start	//fix bug：解决“在双层模式下，在编辑模式长按一个页面时，pc端安装应用，安装成功之后，桌面自动退出编辑模式，但此时被长按的页面所有图标呈托起状态”的问题。【i_0010545】
	public void endReorderingForceAndWithoutAnim(
			boolean mEnableFreeScroll ,
			boolean mNeedSnapToPage )
	{
		resetTouchStateForceAndWithoutAnim( mEnableFreeScroll , mNeedSnapToPage );
	}
	
	private void resetTouchStateForceAndWithoutAnim(
			boolean mEnableFreeScroll ,
			boolean mNeedSnapToPage )
	{
		releaseVelocityTracker();
		endReorderingWithoutAnim( mEnableFreeScroll , mNeedSnapToPage );
		mCancelTap = false;
		mTouchState = TOUCH_STATE_REST;
		mActivePointerId = INVALID_POINTER;
	}
	
	private void endReorderingWithoutAnim(
			final boolean mEnableFreeScroll ,
			final boolean mNeedSnapToPage )
	{
		// For simplicity, we call endReordering sometimes even if reordering was never started.
		// In that case, we don't want to do anything.
		if( !mReorderingStarted )
			return;
		mReorderingStarted = false;
		// If we haven't flung-to-delete the current child, then we just animate the drag view
		// back into position
		final Runnable onCompleteRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				onEndReordering();
			}
		};
		mPostReorderingPreZoomInRunnable = new Runnable() {
			
			public void run()
			{
				onCompleteRunnable.run();
				if( mEnableFreeScroll )
				{
					enableFreeScroll( mNeedSnapToPage );
				}
				else
				{
					disableFreeScroll( -1 , mNeedSnapToPage );
				}
			};
		};
		mPostReorderingPreZoomInRemainingAnimationCount = NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT;
		// Snap to the current page
		snapToPage( indexOfChild( mDragView ) , 0 );
		// Animate the drag view back to the front position
		animateDragViewToOriginalPositionWithoutAnim();
	}
	
	private void animateDragViewToOriginalPositionWithoutAnim()
	{
		if( mDragView != null )
		{
			mDragView.setTranslationX( 0f );
			mDragView.setTranslationY( 0f );
			mDragView.setScaleX( 1f );
			mDragView.setScaleY( 1f );
			onPostReorderingAnimationCompleted();
		}
	}
	
	protected void enableFreeScroll(
			boolean mNeedSnapToPage )
	{
		setEnableFreeScroll( true , mNeedSnapToPage , -1 );
	}
	
	protected void disableFreeScroll(
			int snapPage ,
			boolean mNeedSnapToPage )
	{
		setEnableFreeScroll( false , mNeedSnapToPage , snapPage );
	}
	
	private void setEnableFreeScroll(
			boolean freeScroll ,
			boolean mNeedSnapToPage ,
			int snapPage )
	{
		//cheyingkun add start	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_FREESCROLL )
		{
			isOverViewModel = freeScroll;
		}
		else
		//cheyingkun add end
		{
			mFreeScroll = freeScroll;
		}
		if( mNeedSnapToPage )
		{
			if( snapPage == -1 )
			{
				snapPage = getPageNearestToCenterOfScreen( 0.5f );
			}
			if( !freeScroll )
			{
				snapToPage( snapPage );
			}
			else
			{
				updateFreescrollBounds();
				getOverviewModePages( mTempVisiblePagesRange );
				if( getCurrentPage() < mTempVisiblePagesRange[0] )
				{
					setCurrentPage( mTempVisiblePagesRange[0] );
				}
				else if( getCurrentPage() > mTempVisiblePagesRange[1] )
				{
					setCurrentPage( mTempVisiblePagesRange[1] );
				}
			}
		}
		setEnableOverscroll( !freeScroll );
	}
	//xiatian add end
	;
	
	public boolean isLoop()
	{
		return(
		//
		isLoop
		//
		&& ( getChildCount() > 1/* 解决“开启桌面循环切页的前提下，当桌面只有一个页面时，滑动页面过程中，动画异常”的问题。【c_0004649】 */)
		//
		);
	}
	
	public void setLoop(
			boolean isLoop )
	{
		this.isLoop = isLoop;
		//xiatian del start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		//		if( mCurentAnimInfo != null )
		//		{
		//			if( isLoop() )
		//			{
		//				mCurentAnimInfo.setMaxScroll( 1.0f );
		//				mCurentAnimInfo.setLoop( true );
		//			}
		//			else
		//			{
		//				mCurentAnimInfo.setMaxScroll( 0.5f );
		//				mCurentAnimInfo.setLoop( false );
		//			}
		//		}
		//xiatian del end
	}
	
	public int getNextPageIndex(
			int page )
	{
		return ( page + 1 ) % getPageCount();
	}
	
	protected int getPrePageIndex(
			int page )
	{
		if( page == 0 )
		{
			return getPageCount() - 1;
		}
		else
		{
			return page - 1;
		}
	}
	
	// zhujieping@2015/06/11 ADD START,mCurrentPage是有方向的，布局从右向左时，滑动最右边时mCurrentPage值是0。而这个方法不跟着方向变化，如最右边时，对应的index就是getChildCount - 1
	public int getPageIndexIngoreLayoutDirection(
			int currentIndex )
	{
		if( currentIndex < 0 || currentIndex >= getChildCount() )
		{
			return -1;
		}
		if( isLayoutRtl() )
		{
			return getChildCount() - 1 - currentIndex;
		}
		return currentIndex;
	}
	// zhujieping@2015/06/11 ADD END
	;
	
	//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	private void updateWorkspaceItemsStateOnEndMovingInNormalMode()
	{
		if( this instanceof Workspace )
		{
			Workspace mWorkspace = (Workspace)this;
			if( mWorkspace.isInNormalMode() )
			{
				mWorkspace.updateWorkspaceItemsStateOnEndMovingInNormalMode();
			}
		}
	}
	//xiatian add end
	//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	protected void setDragCellLayoutListener(
			DragCellLayoutListener listener )
	{
		mDragCellLayoutListener = listener;
	}
	
	protected boolean handleDeleteDragView(
			View dragview )
	{
		return false;
	}
	
	protected boolean isChildCanRecording(
			int index )
	{
		return true;
	}
	//zhujieping add end
}
