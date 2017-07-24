package com.cooee.phenix;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Workspace.State;
import com.cooee.phenix.AppList.KitKat.AppsCustomizeCellLayout;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.Folder.FolderIcon.FolderRingAnimator;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.CellAndSpan;
import com.cooee.phenix.data.CellInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.LauncherAppWidgetInfo;
import com.cooee.phenix.data.PendingAddWidgetInfo;
import com.cooee.phenix.effects.IEffect;


public class CellLayout extends ViewGroup implements IEffect
//
, IOnThemeChanged//zhujieping add，换主题不重启
{
	
	//修改文件编码格式
	static final String TAG = "CellLayout";
	private Launcher mLauncher;
	private int mCellWidth;
	private int mCellHeight;
	private int mFixedCellWidth;
	private int mFixedCellHeight;
	private int mCountX;
	private int mCountY;
	private int mOriginalWidthGap;
	private int mOriginalHeightGap;
	private int mWidthGap;
	private int mHeightGap;
	private int mMaxGap;
	private boolean mScrollingTransformsDirty = false;
	private final Rect mRect = new Rect();
	private final CellInfo mCellInfo = new CellInfo();
	// These are temporary variables to prevent having to allocate a new object just to
	// return an (x, y) value from helper functions. Do NOT use them to maintain other state.
	private final int[] mTmpXY = new int[2];
	private final int[] mTmpPoint = new int[2];
	int[] mTempLocation = new int[2];
	boolean[][] mOccupied;
	boolean[][] mTmpOccupied;
	private boolean mLastDownOnOccupiedCell = false;
	private OnTouchListener mInterceptTouchListener;
	private ArrayList<FolderRingAnimator> mFolderOuterRings = new ArrayList<FolderRingAnimator>();
	// lvjiangbin add start 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果    
	private int[] mFolderLeaveBehindCell = { -1 , -1 };
	// lvjiangbin add end 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果   
	private float FOREGROUND_ALPHA_DAMPER = 0.65f;
	private int mForegroundAlpha = 0;
	private float mBackgroundAlpha;
	private float mBackgroundAlphaMultiplier = 1.0f;
	public Drawable mNormalBackground;
	private Drawable mActiveGlowBackground;
	private Drawable mOverScrollForegroundDrawable;
	private Drawable mOverScrollLeft;
	private Drawable mOverScrollRight;
	private Rect mBackgroundRect;
	private Rect mForegroundRect;
	private int mForegroundPadding;
	// These values allow a fixed measurement to be set on the CellLayout.
	private int mFixedWidth = -1;
	private int mFixedHeight = -1;
	// If we're actively dragging something over this screen, mIsDragOverlapping is true
	private boolean mIsDragOverlapping = false;
	boolean mUseActiveGlowBackground = false;
	// These arrays are used to implement the drag visualization on x-large screens.
	// They are used as circular arrays, indexed by mDragOutlineCurrent.
	private Rect[] mDragOutlines = new Rect[4];
	private float[] mDragOutlineAlphas = new float[mDragOutlines.length];
	private InterruptibleInOutAnimator[] mDragOutlineAnims = new InterruptibleInOutAnimator[mDragOutlines.length];
	// Used as an index into the above 3 arrays; indicates which is the most current value.
	private int mDragOutlineCurrent = 0;
	private final Paint mDragOutlinePaint = new Paint();
	private BubbleTextView mPressedOrFocusedIcon;
	private HashMap<CellLayout.LayoutParams , Animator> mReorderAnimators = new HashMap<CellLayout.LayoutParams , Animator>();
	private HashMap<View , ReorderHintAnimation> mShakeAnimators = new HashMap<View , ReorderHintAnimation>();
	private boolean mItemPlacementDirty = false;
	// When a drag operation is in progress, holds the nearest cell to the touch point
	private final int[] mDragCell = new int[2];
	private boolean mDragging = false;
	private TimeInterpolator mEaseOutInterpolator;
	private ShortcutAndWidgetContainer mShortcutsAndWidgets;
	private boolean mIsHotseat = false;
	private float mHotseatScale = 1f;
	public static final int MODE_DRAG_OVER = 0;
	public static final int MODE_ON_DROP = 1;
	public static final int MODE_ON_DROP_EXTERNAL = 2;
	public static final int MODE_ACCEPT_DROP = 3;
	private static final boolean DESTRUCTIVE_REORDER = false;
	private static final boolean DEBUG_VISUALIZE_OCCUPIED = false;
	public static final int LANDSCAPE = 0;
	public static final int PORTRAIT = 1;
	private static final float REORDER_HINT_MAGNITUDE = 0.12f;
	private static final int REORDER_ANIMATION_DURATION = 150;
	private float mReorderHintAnimationMagnitude;
	private ArrayList<View> mIntersectingViews = new ArrayList<View>();
	private Rect mOccupiedRect = new Rect();
	private int[] mDirectionVector = new int[2];
	int[] mPreviousReorderDirection = new int[2];
	private static final int INVALID_DIRECTION = -100;
	private DropTarget.DragEnforcer mDragEnforcer;
	private Rect mTempRect = new Rect();
	private final static PorterDuffXfermode sAddBlendMode = new PorterDuffXfermode( PorterDuff.Mode.ADD );
	private final static Paint sPaint = new Paint();
	public View celllayoutxy[][] = null;
	private boolean mIsFunctionPage = false;//是否为功能页（酷生活、音乐页和相机页），需要特殊处理：1、隐藏mShortcutsAndWidgets；2、onLayout中要特殊处理“左上右下”四个值。
	private boolean mIsDefaultPage = false; //xiatian add	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
	private int mBackgroundTopPadding = -1;
	private int mBackgroundBottomPadding = -1;
	private int mBackgroundLeftPadding = -1;
	private int mBackgroundRightPadding = -1;
	//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
	private ImageView mOverViewHomeView = null; //编辑模式下设置主页的小房子的view
	private int celllayoutPaddingLeft = 0; //编辑模式下celllayout的左边距
	private int celllayoutPaddingTop = 0; //编辑模式下celllayout的上边距
	private int celllayoutPaddingBottom = 0; //编辑模式下celllayout的底边距
	private int homeViewLeftPosition = 0; //编辑模式下homeview的左边距
	private int homeViewTopPosition = 0; //编辑模式下homeview的上边距
	private int homeViewBottomPosition = 0; //编辑模式下homeview的底边距
	//gaominghui add end
	//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
	private ArrayList<FolderIcon.PreviewBackground> mFolderBackgrounds = new ArrayList<FolderIcon.PreviewBackground>();
	FolderIcon.PreviewBackground mFolderBg = new FolderIcon.PreviewBackground();
	Paint mFolderBgPaint = new Paint();
	//zhujieping add end
	//cheyingkun add start	//修正widget行列数的计算方式
	public static ArrayList<Integer> cellWidgetMaxWidthList;
	public static ArrayList<Integer> cellWidgetMaxHeightList;
	
	//cheyingkun add end
	public CellLayout(
			Context context )
	{
		this( context , null );
	}
	
	public CellLayout(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public CellLayout(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		mDragEnforcer = new DropTarget.DragEnforcer( context );
		// A ViewGroup usually does not draw, but CellLayout needs to draw a rectangle to show
		// the user where a dragged item will land when dropped.
		setWillNotDraw( false );
		setClipToPadding( false );
		mLauncher = (Launcher)context;
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.CellLayout , defStyle , 0 );
		mCellWidth = mCellHeight = -1;
		mFixedCellHeight = mFixedCellHeight = -1;
		mWidthGap = mOriginalWidthGap = 0;
		mHeightGap = mOriginalHeightGap = 0;
		mMaxGap = Integer.MAX_VALUE;
		mCountX = (int)grid.getNumColumns();
		mCountY = (int)grid.getNumRows();
		mOccupied = new boolean[mCountX][mCountY];
		mTmpOccupied = new boolean[mCountX][mCountY];
		mPreviousReorderDirection[0] = INVALID_DIRECTION;
		mPreviousReorderDirection[1] = INVALID_DIRECTION;
		a.recycle();
		setAlwaysDrawnWithCacheEnabled( false );
		final Resources res = getResources();
		mHotseatScale = (float)grid.getHotseatIconSize() / grid.getIconSize();
		//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
		{
			mOverViewHomeView = new ImageView( context );
			celllayoutPaddingLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_celllayout_padding_left );
			celllayoutPaddingTop = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_celllayout_padding_top );
			celllayoutPaddingBottom = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_celllayout_padding_bottom );
			homeViewLeftPosition = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_home_view_padding_left );
			homeViewTopPosition = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_home_view_padding_top );
			homeViewBottomPosition = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_home_view_padding_bottom );
		}
		//gaominghui add end
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		if( Build.VERSION.SDK_INT >= 22 )
		{
			mNormalBackground = ResourcesCompat.getDrawable( res , R.drawable.celllayout_screenpanel , null );
			mActiveGlowBackground = ResourcesCompat.getDrawable( res , R.drawable.screenpanel_hover , null );
			mOverScrollLeft = ResourcesCompat.getDrawable( res , R.drawable.overscroll_glow_left , null );
			mOverScrollRight = ResourcesCompat.getDrawable( res , R.drawable.overscroll_glow_right , null );
			//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
			if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
				mOverViewHomeView.setImageDrawable( ResourcesCompat.getDrawable( res , R.drawable.editmode_home_unselected , null ) );
			//gaominghui add end
		}
		else
		{
			mNormalBackground = res.getDrawable( R.drawable.celllayout_screenpanel );
			mActiveGlowBackground = res.getDrawable( R.drawable.screenpanel_hover );
			mOverScrollLeft = res.getDrawable( R.drawable.overscroll_glow_left );
			mOverScrollRight = res.getDrawable( R.drawable.overscroll_glow_right );
			//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
			if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
				mOverViewHomeView.setImageDrawable( res.getDrawable( R.drawable.editmode_home_selected ) );
			//gaominghui add end
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		mForegroundPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.workspace_overscroll_drawable_padding );
		mReorderHintAnimationMagnitude = ( REORDER_HINT_MAGNITUDE * grid.getIconWidthSizePx() );
		mNormalBackground.setFilterBitmap( true );
		mActiveGlowBackground.setFilterBitmap( true );
		// Initialize the data structures used for the drag visualization.
		mEaseOutInterpolator = new DecelerateInterpolator( 2.5f ); // Quint ease out
		mDragCell[0] = mDragCell[1] = -1;
		for( int i = 0 ; i < mDragOutlines.length ; i++ )
		{
			mDragOutlines[i] = new Rect( -1 , -1 , -1 , -1 );
		}
		// When dragging things around the home screens, we show a green outline of
		// where the item will land. The outlines gradually fade out, leaving a trail
		// behind the drag path.
		// Set up all the animations that are used to implement this fading.
		final int duration = LauncherDefaultConfig.getInt( R.integer.config_dragOutlineFadeTime );
		final float fromAlphaValue = 0;
		final float toAlphaValue = (float)LauncherDefaultConfig.getInt( R.integer.config_dragOutlineMaxAlpha );
		Arrays.fill( mDragOutlineAlphas , fromAlphaValue );
		for( int i = 0 ; i < mDragOutlineAnims.length ; i++ )
		{
			final InterruptibleInOutAnimator anim = new InterruptibleInOutAnimator( this , duration , fromAlphaValue , toAlphaValue );
			anim.getAnimator().setInterpolator( mEaseOutInterpolator );
			final int thisIndex = i;
			anim.getAnimator().addUpdateListener( new AnimatorUpdateListener() {
				
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					final Bitmap outline = (Bitmap)anim.getTag();
					// If an animation is started and then stopped very quickly, we can still
					// get spurious updates we've cleared the tag. Guard against this.
					if( outline == null )
					{
						@SuppressWarnings( "all" )
						// suppress dead code warning
						final boolean debug = false;
						if( debug )
						{
							Object val = animation.getAnimatedValue();
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , StringUtils.concat( "anim " , thisIndex , " update: " , val , ", isStopped " , anim.isStopped() ) );
						}
						// Try to prevent it from continuing to run
						animation.cancel();
					}
					else
					{
						mDragOutlineAlphas[thisIndex] = (Float)animation.getAnimatedValue();
						CellLayout.this.invalidate( mDragOutlines[thisIndex] );
					}
				}
			} );
			// The animation holds a reference to the drag outline bitmap as long is it's
			// running. This way the bitmap can be GCed when the animations are complete.
			anim.getAnimator().addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					if( (Float)( (ValueAnimator)animation ).getAnimatedValue() == 0f )
					{
						anim.setTag( null );
					}
				}
			} );
			mDragOutlineAnims[i] = anim;
		}
		mBackgroundRect = new Rect();
		mForegroundRect = new Rect();
		mShortcutsAndWidgets = new ShortcutAndWidgetContainer( context );
		mShortcutsAndWidgets.setCellDimensions( mCellWidth , mCellHeight , mWidthGap , mHeightGap , mCountX , mCountY );
		if( !LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )//cheyingkun add	//自定义桌面布局
		{
			//xiatian add start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL && !( this instanceof AppsCustomizeCellLayout ) )
			{//均分桌面剩余空间（CellLayout的左右padding和图标间的间距设置一致。）,zhujieping,改成中间的均分，两边的padding可以调
				setPadding( grid.getCelllayoutXPadding() , getPaddingTop() , grid.getCelllayoutXPadding() , getPaddingBottom() );
			}
		}
		//xiatian add end
		addView( mShortcutsAndWidgets );
		//zhujieping add start
		mBackgroundTopPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_celllayout_background_top_padding );
		mBackgroundBottomPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_celllayout_background_bottom_padding );
		mBackgroundLeftPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_celllayout_background_left_padding );
		mBackgroundRightPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_celllayout_background_right_padding );
		//zhujieping add end
	}
	
	public void enableHardwareLayer(
			boolean hasLayer )
	{
		mShortcutsAndWidgets.setLayerType( hasLayer ? LAYER_TYPE_HARDWARE : LAYER_TYPE_NONE , sPaint );
	}
	
	public void buildHardwareLayer()
	{
		mShortcutsAndWidgets.buildLayer();
	}
	
	public float getChildrenScale()
	{
		return mIsHotseat ? mHotseatScale : 1.0f;
	}
	
	public void setCellDimensions(
			int width ,
			int height )
	{
		mFixedCellWidth = mCellWidth = width;
		mFixedCellHeight = mCellHeight = height;
		mShortcutsAndWidgets.setCellDimensions( mCellWidth , mCellHeight , mWidthGap , mHeightGap , mCountX , mCountY );
	}
	
	public void setGridSize(
			int x ,
			int y )
	{
		mCountX = x;
		mCountY = y;
		mOccupied = new boolean[mCountX][mCountY];
		mTmpOccupied = new boolean[mCountX][mCountY];
		mTempRectStack.clear();
		mShortcutsAndWidgets.setCellDimensions( mCellWidth , mCellHeight , mWidthGap , mHeightGap , mCountX , mCountY );
		requestLayout();
	}
	
	// Set whether or not to invert the layout horizontally if the layout is in RTL mode.
	public void setInvertIfRtl(
			boolean invert )
	{
		mShortcutsAndWidgets.setInvertIfRtl( invert );
	}
	
	private void invalidateBubbleTextView(
			BubbleTextView icon )
	{
		final int padding = icon.getPressedOrFocusedBackgroundPadding();
		invalidate(
				icon.getLeft() + getPaddingLeft() - padding ,
				icon.getTop() + getPaddingTop() - padding ,
				icon.getRight() + getPaddingLeft() + padding ,
				icon.getBottom() + getPaddingTop() + padding );
	}
	
	void setOverScrollAmount(
			float r ,
			boolean left )
	{
		if( left && mOverScrollForegroundDrawable != mOverScrollLeft )
		{
			mOverScrollForegroundDrawable = mOverScrollLeft;
		}
		else if( !left && mOverScrollForegroundDrawable != mOverScrollRight )
		{
			mOverScrollForegroundDrawable = mOverScrollRight;
		}
		r *= FOREGROUND_ALPHA_DAMPER;
		mForegroundAlpha = (int)Math.round( ( r * 255 ) );
		mOverScrollForegroundDrawable.setAlpha( mForegroundAlpha );
		invalidate();
	}
	
	void setPressedOrFocusedIcon(
			BubbleTextView icon )
	{
		// We draw the pressed or focused BubbleTextView's background in CellLayout because it
		// requires an expanded clip rect (due to the glow's blur radius)
		BubbleTextView oldIcon = mPressedOrFocusedIcon;
		mPressedOrFocusedIcon = icon;
		if( oldIcon != null )
		{
			invalidateBubbleTextView( oldIcon );
		}
		if( mPressedOrFocusedIcon != null )
		{
			invalidateBubbleTextView( mPressedOrFocusedIcon );
		}
	}
	
	void setIsDragOverlapping(
			boolean isDragOverlapping )
	{
		if( mIsDragOverlapping != isDragOverlapping )
		{
			mIsDragOverlapping = isDragOverlapping;
			setUseActiveGlowBackground( mIsDragOverlapping );
			invalidate();
		}
	}
	
	void setUseActiveGlowBackground(
			boolean use )
	{
		mUseActiveGlowBackground = use;
	}
	
	boolean getIsDragOverlapping()
	{
		return mIsDragOverlapping;
	}
	
	protected void setOverscrollTransformsDirty(
			boolean dirty )
	{
		mScrollingTransformsDirty = dirty;
	}
	
	protected void resetOverscrollTransforms()
	{
		if( mScrollingTransformsDirty )
		{
			setOverscrollTransformsDirty( false );
			setTranslationX( 0 );
			setRotationY( 0 );
			// It doesn't matter if we pass true or false here, the important thing is that we
			// pass 0, which results in the overscroll drawable not being drawn any more.
			setOverScrollAmount( 0 , false );
			setPivotX( getMeasuredWidth() / 2 );
			setPivotY( getMeasuredHeight() / 2 );
		}
	}
	
	@Override
	protected void onDraw(
			Canvas canvas )
	{
		// When we're large, we are either drawn in a "hover" state (ie when dragging an item to
		// a neighboring page) or with just a normal background (if backgroundAlpha > 0.0f)
		// When we're small, we are either drawn normally or in the "accepts drops" state (during
		// a drag). However, we also drag the mini hover background *over* one of those two
		// backgrounds
		if( mBackgroundAlpha > 0.0f )
		{
			Drawable bg;
			if( mUseActiveGlowBackground )
			{
				// In the mini case, we draw the active_glow bg *over* the active background
				bg = mActiveGlowBackground;
			}
			else
			{
				bg = mNormalBackground;
			}
			bg.setAlpha( (int)( mBackgroundAlpha * mBackgroundAlphaMultiplier * 255 ) );
			bg.setBounds( mBackgroundRect );
			bg.draw( canvas );
		}
		final Paint paint = mDragOutlinePaint;
		for( int i = 0 ; i < mDragOutlines.length ; i++ )
		{
			final float alpha = mDragOutlineAlphas[i];
			if( alpha > 0 )
			{
				final Rect r = mDragOutlines[i];
				mTempRect.set( r );
				Utilities.scaleRectAboutCenter( mTempRect , getChildrenScale() );
				final Bitmap b = (Bitmap)mDragOutlineAnims[i].getTag();
				paint.setAlpha( (int)( alpha + .5f ) );
				canvas.drawBitmap( b , null , mTempRect , paint );
			}
		}
		// We draw the pressed or focused BubbleTextView's background in CellLayout because it
		// requires an expanded clip rect (due to the glow's blur radius)
		if( mPressedOrFocusedIcon != null )
		{
			final int padding = mPressedOrFocusedIcon.getPressedOrFocusedBackgroundPadding();
			final Bitmap b = mPressedOrFocusedIcon.getPressedOrFocusedBackground();
			if( b != null )
			{
				int offset = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - ( mCountX * mCellWidth );
				offset -= ( mCountX - 1 ) * getWidthGap();//xiatian add	//fix bug：解决“当mWidthGap不为0时，文件夹内view的mPressedOrFocusedIcon位置右移”的问题。
				int left = getPaddingLeft() + (int)Math.ceil( offset / 2f );
				int top = getPaddingTop();
				canvas.drawBitmap( b , mPressedOrFocusedIcon.getLeft() + left - padding , mPressedOrFocusedIcon.getTop() + top - padding , null );
			}
		}
		if( DEBUG_VISUALIZE_OCCUPIED )
		{
			int[] pt = new int[2];
			ColorDrawable cd = new ColorDrawable( Color.RED );
			cd.setBounds( 0 , 0 , mCellWidth , mCellHeight );
			for( int i = 0 ; i < mCountX ; i++ )
			{
				for( int j = 0 ; j < mCountY ; j++ )
				{
					if( mOccupied[i][j] )
					{
						cellToPoint( i , j , pt );
						canvas.save();
						canvas.translate( pt[0] , pt[1] );
						cd.draw( canvas );
						canvas.restore();
					}
				}
			}
		}
		int previewOffset = FolderRingAnimator.sPreviewHeightSize;
		// The folder outer / inner ring image(s)
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		for( int i = 0 ; i < mFolderOuterRings.size() ; i++ )
		{
			FolderRingAnimator fra = mFolderOuterRings.get( i );
			Drawable d;
			int width , height;
			cellToPoint( fra.mCellX , fra.mCellY , mTempLocation );
			View child = getChildAt( fra.mCellX , fra.mCellY );
			if( child != null )
			{
				int centerX = mTempLocation[0] + mCellWidth / 2;
				//文件夹预览图（一个图标覆盖到另一个图标或者文件夹上会生成文件夹预览图）的图标在绘制时的中心点的y坐标
				int centerY = mTempLocation[1] + previewOffset / 2 + child.getPaddingTop() + grid.getFolderBackgroundOffset();//xiatian add note	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。//BubbleTextView重载方法“getPaddingTop”
				// Draw outer ring, if it exists
				if( FolderIcon.HAS_OUTER_RING )
				{
					d = FolderRingAnimator.sSharedOuterRingDrawable;
					width = (int)( fra.getOuterRingWidthSize() * getChildrenScale() );
					height = (int)( fra.getOuterRingHeightSize() * getChildrenScale() );
					canvas.save();
					canvas.translate( centerX - width / 2 , centerY - height / 2 );
					d.setBounds( 0 , 0 , width , height );
					d.draw( canvas );
					canvas.restore();
				}
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
					// zhujieping@2015/03/17 DEL START
					// Draw inner ring
					d = FolderRingAnimator.sSharedInnerRingDrawable;
					width = (int)( fra.getInnerRingSize() * getChildrenScale() );
					height = width;
					canvas.save();
					canvas.translate( centerX - width / 2 , centerY - width / 2 );
					d.setBounds( 0 , 0 , width , height );
					d.draw( canvas );
					canvas.restore();
					// zhujieping@2015/03/17 DEL END
				}
			}
		}
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
			// i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果    BEGIN 吕江滨
			if( mFolderLeaveBehindCell[0] >= 0 && mFolderLeaveBehindCell[1] >= 0 )
			{
				Drawable d = FolderIcon.sSharedFolderLeaveBehind;
				int width = d.getIntrinsicWidth();
				int height = d.getIntrinsicHeight();
				cellToPoint( mFolderLeaveBehindCell[0] , mFolderLeaveBehindCell[1] , mTempLocation );
				View child = getChildAt( mFolderLeaveBehindCell[0] , mFolderLeaveBehindCell[1] );
				if( child != null )
				{
					int centerX = mTempLocation[0] + mCellWidth / 2;
					int centerY = mTempLocation[1] + previewOffset / 2 + child.getPaddingTop() + grid.getFolderBackgroundOffset();
					canvas.save();
					canvas.translate( centerX - width / 2 , centerY - width / 2 );
					d.setBounds( 0 , 0 , width , height );
					d.draw( canvas );
					canvas.restore();
				}
			}
			// i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果    END 吕江滨
		}
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		for( int i = 0 ; i < mFolderBackgrounds.size() ; i++ )
		{
			FolderIcon.PreviewBackground bg = mFolderBackgrounds.get( i );
			cellToPoint( bg.delegateCellX , bg.delegateCellY , mTempLocation );
			canvas.save();
			canvas.translate( mTempLocation[0] , mTempLocation[1] );
			bg.drawBackground( canvas , mFolderBgPaint );
			canvas.restore();
		}
		//zhujieping add end
	}
	
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		super.dispatchDraw( canvas );
		if( mForegroundAlpha > 0 )
		{
			mOverScrollForegroundDrawable.setBounds( mForegroundRect );
			mOverScrollForegroundDrawable.draw( canvas );
		}
	}
	
	public void showFolderAccept(
			FolderRingAnimator fra )
	{
		mFolderOuterRings.add( fra );
	}
	
	public void hideFolderAccept(
			FolderRingAnimator fra )
	{
		if( mFolderOuterRings.contains( fra ) )
		{
			mFolderOuterRings.remove( fra );
		}
		invalidate();
	}
	
	// lvjiangbin add start 解决 ：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果    
	public void setFolderLeaveBehindCell(
			int x ,
			int y )
	{
		mFolderLeaveBehindCell[0] = x;
		mFolderLeaveBehindCell[1] = y;
		invalidate();
	}
	
	public void clearFolderLeaveBehind()
	{
		mFolderLeaveBehindCell[0] = -1;
		mFolderLeaveBehindCell[1] = -1;
		invalidate();
	}
	
	// lvjiangbin add end  解决：i_0011396: 【文件夹】展开底边栏上的文件夹时背景有显示文件夹的框框，但在桌面上展开文件夹时无文件夹框显示，建议统一效果 
	@Override
	public boolean shouldDelayChildPressedState()
	{
		return false;
	}
	
	public void restoreInstanceState(
			SparseArray<Parcelable> states )
	{
		//<i_0010439> liuhailin@2015-03-11 modify begin
		try
		{
			dispatchRestoreInstanceState( states );
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "failed to restoreInstanceState" , e.getStackTrace().toString() ) );
		}
		//<i_0010439> liuhailin@2015-03-11 modify end
	}
	
	@Override
	public void cancelLongPress()
	{
		super.cancelLongPress();
		// Cancel long press for all children
		final int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			final View child = getChildAt( i );
			child.cancelLongPress();
		}
	}
	
	public void setOnInterceptTouchListener(
			View.OnTouchListener listener )
	{
		mInterceptTouchListener = listener;
	}
	
	public int getCountX()
	{
		return mCountX;
	}
	
	public int getCountY()
	{
		return mCountY;
	}
	
	public void setIsHotseat(
			boolean isHotseat )
	{
		mIsHotseat = isHotseat;
		mShortcutsAndWidgets.setIsHotseat( isHotseat );
	}
	
	public boolean addViewToCellLayout(
			View child ,
			int index ,
			int childId ,
			LayoutParams params ,
			boolean markCells )
	{
		final LayoutParams lp = params;
		// Hotseat icons - remove text
		if( child instanceof BubbleTextView )
		{
			BubbleTextView bubbleChild = (BubbleTextView)child;
			//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
			//			boolean mTextVisible = !mIsHotseat;//xiatian del
			boolean mTextVisible = bubbleChild.isShowTextInCellLayout( mIsHotseat );//xiatian add
			//xiatian end
			bubbleChild.setTextVisibility( mTextVisible );
		}
		else if( child instanceof FolderIcon )
		{
			FolderIcon mFolderIcon = (FolderIcon)child;
			//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
			//			boolean mTextVisible = !mIsHotseat;//xiatian del
			boolean mTextVisible = mFolderIcon.isShowTextInCellLayout( mIsHotseat );//xiatian add
			//xiatian end
			mFolderIcon.setTextVisible( mTextVisible );
		}
		child.setScaleX( getChildrenScale() );
		child.setScaleY( getChildrenScale() );
		// Generate an id for each view, this assumes we have at most 256x256 cells
		// per workspace screen
		if( lp.cellX >= 0 && lp.cellX <= mCountX - 1 && lp.cellY >= 0 && lp.cellY <= mCountY - 1 )
		{
			// If the horizontal or vertical span is set to -1, it is taken to
			// mean that it spans the extent of the CellLayout
			if( lp.cellHSpan < 0 )
				lp.cellHSpan = mCountX;
			if( lp.cellVSpan < 0 )
				lp.cellVSpan = mCountY;
			child.setId( childId );
			mShortcutsAndWidgets.addView( child , index , lp );
			if( markCells )
				markCellsAsOccupiedForView( child );
			return true;
		}
		return false;
	}
	
	/**
	 * 移除CellLayout中的view
	 * 专属页使用,专属页直接加入了CellLayout中,其他的移除方法无法移除
	 * @author yangtianyu 2016-7-14
	 */
	public void removeViewFromCell(
			View view )
	{
		super.removeView( view );
	}
	
	@Override
	public void removeAllViews()
	{
		clearOccupiedCells();
		mShortcutsAndWidgets.removeAllViews();
	}
	
	@Override
	public void removeAllViewsInLayout()
	{
		if( mShortcutsAndWidgets.getChildCount() > 0 )
		{
			clearOccupiedCells();
			mShortcutsAndWidgets.removeAllViewsInLayout();
		}
	}
	
	public void removeViewWithoutMarkingCells(
			View view )
	{
		mShortcutsAndWidgets.removeView( view );
	}
	
	@Override
	public void removeView(
			View view )
	{
		markCellsAsUnoccupiedForView( view );
		mShortcutsAndWidgets.removeView( view );
	}
	
	@Override
	public void removeViewAt(
			int index )
	{
		markCellsAsUnoccupiedForView( mShortcutsAndWidgets.getChildAt( index ) );
		mShortcutsAndWidgets.removeViewAt( index );
	}
	
	@Override
	public void removeViewInLayout(
			View view )
	{
		markCellsAsUnoccupiedForView( view );
		mShortcutsAndWidgets.removeViewInLayout( view );
	}
	
	@Override
	public void removeViews(
			int start ,
			int count )
	{
		for( int i = start ; i < start + count ; i++ )
		{
			markCellsAsUnoccupiedForView( mShortcutsAndWidgets.getChildAt( i ) );
		}
		mShortcutsAndWidgets.removeViews( start , count );
	}
	
	@Override
	public void removeViewsInLayout(
			int start ,
			int count )
	{
		for( int i = start ; i < start + count ; i++ )
		{
			markCellsAsUnoccupiedForView( mShortcutsAndWidgets.getChildAt( i ) );
		}
		mShortcutsAndWidgets.removeViewsInLayout( start , count );
	}
	
	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		if( getParent() instanceof Workspace )
		{
			Workspace workspace = (Workspace)getParent();
			mCellInfo.setScreenId( workspace.getIdForScreen( this ) );
		}
	}
	
	public void setTagToCellInfoForPoint(
			int touchX ,
			int touchY )
	{
		final CellInfo cellInfo = mCellInfo;
		Rect frame = mRect;
		final int x = touchX + getScrollX();
		final int y = touchY + getScrollY();
		final int count = mShortcutsAndWidgets.getChildCount();
		boolean found = false;
		for( int i = count - 1 ; i >= 0 ; i-- )
		{
			final View child = mShortcutsAndWidgets.getChildAt( i );
			final LayoutParams lp = (LayoutParams)child.getLayoutParams();
			if( ( child.getVisibility() == VISIBLE || child.getAnimation() != null ) && lp.isLockedToGrid )
			{
				child.getHitRect( frame );
				float scale = child.getScaleX();
				frame = new Rect( child.getLeft() , child.getTop() , child.getRight() , child.getBottom() );
				// The child hit rect is relative to the CellLayoutChildren parent, so we need to
				// offset that by this CellLayout's padding to test an (x,y) point that is relative
				// to this view.
				frame.offset( getPaddingLeft() , getPaddingTop() );
				frame.inset( (int)( frame.width() * ( 1f - scale ) / 2 ) , (int)( frame.height() * ( 1f - scale ) / 2 ) );
				if( frame.contains( x , y ) )
				{
					cellInfo.setCell( child );
					cellInfo.setCellX( lp.cellX );
					cellInfo.setCellY( lp.cellY );
					cellInfo.setSpanX( lp.cellHSpan );
					cellInfo.setSpanY( lp.cellVSpan );
					found = true;
					break;
				}
			}
		}
		mLastDownOnOccupiedCell = found;
		if( !found )
		{
			final int cellXY[] = mTmpXY;
			pointToCellExact( x , y , cellXY );
			cellInfo.setCell( null );
			cellInfo.setCellX( cellXY[0] );
			cellInfo.setCellY( cellXY[1] );
			cellInfo.setSpanX( 1 );
			cellInfo.setSpanY( 1 );
		}
		setTag( cellInfo );
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		// First we clear the tag to ensure that on every touch down we start with a fresh slate,
		// even in the case where we return early. Not clearing here was causing bugs whereby on
		// long-press we'd end up picking up an item from a previous drag operation.
		final int action = ev.getAction();
		if( action == MotionEvent.ACTION_DOWN )
		{
			clearTagCellInfo();
		}
		if( mInterceptTouchListener != null && mInterceptTouchListener.onTouch( this , ev ) )
		{
			return true;
		}
		if( action == MotionEvent.ACTION_DOWN )
		{
			setTagToCellInfoForPoint( (int)ev.getX() , (int)ev.getY() );
		}
		return false;
	}
	
	private void clearTagCellInfo()
	{
		final CellInfo cellInfo = mCellInfo;
		cellInfo.setCell( null );
		cellInfo.setCellX( -1 );
		cellInfo.setCellY( -1 );
		cellInfo.setSpanX( 0 );
		cellInfo.setSpanY( 0 );
		setTag( cellInfo );
	}
	
	public CellInfo getTag()
	{
		return (CellInfo)super.getTag();
	}
	
	/**
	 * Given a point, return the cell that strictly encloses that point
	 * @param x X coordinate of the point
	 * @param y Y coordinate of the point
	 * @param result Array of 2 ints to hold the x and y coordinate of the cell
	 */
	void pointToCellExact(
			int x ,
			int y ,
			int[] result )
	{
		final int hStartPadding = getPaddingLeft();
		final int vStartPadding = getPaddingTop();
		result[0] = ( x - hStartPadding ) / ( mCellWidth + mWidthGap );
		result[1] = ( y - vStartPadding ) / ( mCellHeight + mHeightGap );
		final int xAxis = mCountX;
		final int yAxis = mCountY;
		if( result[0] < 0 )
			result[0] = 0;
		if( result[0] >= xAxis )
			result[0] = xAxis - 1;
		if( result[1] < 0 )
			result[1] = 0;
		if( result[1] >= yAxis )
			result[1] = yAxis - 1;
	}
	
	/**
	 * Given a point, return the cell that most closely encloses that point
	 * @param x X coordinate of the point
	 * @param y Y coordinate of the point
	 * @param result Array of 2 ints to hold the x and y coordinate of the cell
	 */
	void pointToCellRounded(
			int x ,
			int y ,
			int[] result )
	{
		pointToCellExact( x + ( mCellWidth / 2 ) , y + ( mCellHeight / 2 ) , result );
	}
	
	/**
	 * Given a cell coordinate, return the point that represents the upper left corner of that cell
	 *
	 * @param cellX X coordinate of the cell
	 * @param cellY Y coordinate of the cell
	 *
	 * @param result Array of 2 ints to hold the x and y coordinate of the point
	 */
	void cellToPoint(
			int cellX ,
			int cellY ,
			int[] result )
	{
		final int hStartPadding = getPaddingLeft();
		final int vStartPadding = getPaddingTop();
		result[0] = hStartPadding + cellX * ( mCellWidth + mWidthGap );
		result[1] = vStartPadding + cellY * ( mCellHeight + mHeightGap );
	}
	
	/**
	 * Given a cell coordinate, return the point that represents the center of the cell
	 *
	 * @param cellX X coordinate of the cell
	 * @param cellY Y coordinate of the cell
	 *
	 * @param result Array of 2 ints to hold the x and y coordinate of the point
	 */
	void cellToCenterPoint(
			int cellX ,
			int cellY ,
			int[] result )
	{
		regionToCenterPoint( cellX , cellY , 1 , 1 , result );
	}
	
	/**
	 * Given a cell coordinate and span return the point that represents the center of the regio
	 *
	 * @param cellX X coordinate of the cell
	 * @param cellY Y coordinate of the cell
	 *
	 * @param result Array of 2 ints to hold the x and y coordinate of the point
	 */
	void regionToCenterPoint(
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			int[] result )
	{
		final int hStartPadding = getPaddingLeft();
		final int vStartPadding = getPaddingTop();
		result[0] = hStartPadding + cellX * ( mCellWidth + mWidthGap ) + ( spanX * mCellWidth + ( spanX - 1 ) * mWidthGap ) / 2;
		result[1] = vStartPadding + cellY * ( mCellHeight + mHeightGap ) + ( spanY * mCellHeight + ( spanY - 1 ) * mHeightGap ) / 2;
	}
	
	/**
	* Given a cell coordinate and span fills out a corresponding pixel rect
	*
	* @param cellX X coordinate of the cell
	* @param cellY Y coordinate of the cell
	* @param result Rect in which to write the result
	*/
	void regionToRect(
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			Rect result )
	{
		final int hStartPadding = getPaddingLeft();
		final int vStartPadding = getPaddingTop();
		final int left = hStartPadding + cellX * ( mCellWidth + mWidthGap );
		final int top = vStartPadding + cellY * ( mCellHeight + mHeightGap );
		result.set( left , top , left + ( spanX * mCellWidth + ( spanX - 1 ) * mWidthGap ) , top + ( spanY * mCellHeight + ( spanY - 1 ) * mHeightGap ) );
	}
	
	public float getDistanceFromCell(
			float x ,
			float y ,
			int[] cell )
	{
		cellToCenterPoint( cell[0] , cell[1] , mTmpPoint );
		float distance = (float)Math.sqrt( Math.pow( x - mTmpPoint[0] , 2 ) + Math.pow( y - mTmpPoint[1] , 2 ) );
		return distance;
	}
	
	public int getCellWidth()
	{
		return mCellWidth;
	}
	
	public int getCellHeight()
	{
		return mCellHeight;
	}
	
	public int getWidthGap()
	{
		return mWidthGap;
	}
	
	public int getHeightGap()
	{
		return mHeightGap;
	}
	
	Rect getContentRect(
			Rect r )
	{
		if( r == null )
		{
			r = new Rect();
		}
		int left = getPaddingLeft();
		int top = getPaddingTop();
		int right = left + getWidth() - getPaddingLeft() - getPaddingRight();
		int bottom = top + getHeight() - getPaddingTop() - getPaddingBottom();
		r.set( left , top , right , bottom );
		return r;
	}
	
	/** Return a rect that has the cellWidth/cellHeight (left, top), and
	 * widthGap/heightGap (right, bottom) */
	static void getMetrics(
			Rect metrics ,
			int paddedMeasureWidth ,
			int paddedMeasureHeight ,
			int countX ,
			int countY )
	{
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		metrics.set( grid.calculateCellWidth( paddedMeasureWidth , countX ) , grid.calculateCellHeight( paddedMeasureHeight , countY ) , 0 , 0 );
	}
	
	public void setFixedSize(
			int width ,
			int height )
	{
		mFixedWidth = width;
		mFixedHeight = height;
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		int widthSpecMode = MeasureSpec.getMode( widthMeasureSpec );
		int heightSpecMode = MeasureSpec.getMode( heightMeasureSpec );
		int widthSize = MeasureSpec.getSize( widthMeasureSpec );
		int heightSize = MeasureSpec.getSize( heightMeasureSpec );
		int childWidthSize = widthSize - ( getPaddingLeft() + getPaddingRight() );
		int childHeightSize = heightSize - ( getPaddingTop() + getPaddingBottom() );
		if( mFixedCellWidth < 0 || mFixedCellHeight < 0 )
		{
			//xiatian add	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
			//xiatian del start
			//			int cw = grid.calculateCellWidth( childWidthSize , mCountX );
			//			int ch = grid.calculateCellHeight( childHeightSize , mCountY );
			//xiatian del end
			//xiatian add start
			int cw = -1;
			int ch = -1;
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				if( mIsHotseat )
				{
					//cheyingkun add start	//自定义桌面布局
					if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
					{
						ch = grid.getCellHeightPx();
						cw = grid.getCellWidthPx();
						if( grid.isCustomLayoutNormalIcon() )
						{//小图标
							mWidthGap = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_small_icon );
						}
						else
						{//大图标
							mWidthGap = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_big_icon );
						}
						mOriginalWidthGap = mWidthGap;
						mOriginalHeightGap = mHeightGap;
					}
					else
					//cheyingkun add end
					{
						cw = grid.getCellWidthPx();
						ch = grid.calculateCellHeight( childHeightSize , mCountY );
						mOriginalWidthGap = mWidthGap = ( childWidthSize - cw * mCountX ) / ( mCountX - 1 );
						mOriginalHeightGap = mHeightGap;
					}
				}
				//xiatian add start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。适配主菜单布局。
				else if( this instanceof AppsCustomizeCellLayout )
				{
					//mWidthGap
					cw = (int)( grid.getCellWidthPx() //
					* LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() );//cheyingkun add	//主菜单图标缩放比。默认为1。
					mWidthGap = ( childWidthSize - cw * mCountX ) / ( mCountX - 1 );//中间的均分，左右可配置
					if( mWidthGap < 0 )
					{
						mWidthGap = 0;
					}
					//mHeightGap
					ch = (int)( grid.getCellHeightPx()//
					* LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() );//cheyingkun add	//主菜单图标缩放比。默认为1。
					mHeightGap = ( childHeightSize - ch * mCountY ) / ( mCountY - 1 );//中间均分，上下可配
					if( mHeightGap < 0 )
					{
						mHeightGap = 0;
					}
					mOriginalWidthGap = mWidthGap;
					mOriginalHeightGap = mHeightGap;
					setPadding( grid.getAppsCelllayoutXPadding() , 0 , grid.getAppsCelllayoutXPadding() , 0 );
				}
				//xiatian add end
				else
				{
					//cheyingkun add start	//自定义桌面布局
					if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
					{//自定义布局开关
						cw = grid.getCellWidthPx();
						ch = grid.getCellHeightPx();
						if( grid.isCustomLayoutNormalIcon() )
						{//小图标
							mWidthGap = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_small_icon );
							mHeightGap = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_height_gap_small_icon );
						}
						else
						{//大图标
							mWidthGap = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_big_icon );
							mHeightGap = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_height_gap_big_icon );
						}
					}
					else
					//cheyingkun add
					{
						//mWidthGap
						cw = grid.getCellWidthPx();
						mCountX = (int)grid.getNumColumns();
						if( grid.getCellWidthGapPx() == -1 )
						{
							childWidthSize = widthSize - ( getPaddingLeft() + getPaddingRight() );
							mWidthGap = ( childWidthSize - cw * mCountX ) / ( mCountX - 1 );
						}
						else
						{//均分桌面剩余空间（CellLayout的左右padding和图标间的间距设置一致。）
							mWidthGap = grid.getCellWidthGapPx();
						}
						if( mWidthGap < 0 )
						{
							mWidthGap = 0;
						}
						//mHeightGap
						ch = grid.getCellHeightPx();
						mCountY = (int)grid.getNumRows();
						childHeightSize = heightSize - ( getPaddingTop() + getPaddingBottom() );
						mHeightGap = ( childHeightSize - ch * mCountY ) / ( mCountY - 1 );
						if( mHeightGap < 0 )
						{
							mHeightGap = 0;
						}
						//cheyingkun add start	//修正widget行列数的计算方式
						if( !mIsFunctionPage && ( grid.getCellWidthGapPx() != mWidthGap || grid.getCellHeightGapPx() != mHeightGap ) )
						{
							grid.setCellWidthGapPx( mWidthGap );
							grid.setCellHeightGapPx( mHeightGap );
							CellLayout.clearCellWidgetList();
							CellLayout.initCellWidgetList();
						}
						//cheyingkun add end
					}
					mOriginalWidthGap = mWidthGap;
					mOriginalHeightGap = mHeightGap;
				}
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				cw = grid.calculateCellWidth( childWidthSize , mCountX );
				ch = grid.calculateCellHeight( childHeightSize , mCountY );
			}
			//xiatian add end
			//xiatian end
			if( cw != mCellWidth || ch != mCellHeight )
			{
				mCellWidth = cw;
				mCellHeight = ch;
				mShortcutsAndWidgets.setCellDimensions( mCellWidth , mCellHeight , mWidthGap , mHeightGap , mCountX , mCountY );
			}
		}
		int newWidth = childWidthSize;
		int newHeight = childHeightSize;
		if( mFixedWidth > 0 && mFixedHeight > 0 )
		{
			newWidth = mFixedWidth;
			newHeight = mFixedHeight;
		}
		else if( widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED )
		{
			throw new RuntimeException( "CellLayout cannot have UNSPECIFIED dimensions" );
		}
		int numWidthGaps = mCountX - 1;
		int numHeightGaps = mCountY - 1;
		if( mOriginalWidthGap < 0 || mOriginalHeightGap < 0 )
		{
			int hSpace = childWidthSize;
			int vSpace = childHeightSize;
			int hFreeSpace = hSpace - ( mCountX * mCellWidth );
			int vFreeSpace = vSpace - ( mCountY * mCellHeight );
			mWidthGap = Math.min( mMaxGap , numWidthGaps > 0 ? ( hFreeSpace / numWidthGaps ) : 0 );
			mHeightGap = Math.min( mMaxGap , numHeightGaps > 0 ? ( vFreeSpace / numHeightGaps ) : 0 );
			// zhangjin@2016/06/07 DEL START
			//mShortcutsAndWidgets.setCellDimensions( mCellWidth , mCellHeight , mWidthGap , mHeightGap , mCountX , mCountY );
			// zhangjin@2016/06/07 DEL END
		}
		else
		{
			mWidthGap = mOriginalWidthGap;
			mHeightGap = mOriginalHeightGap;
		}
		// zhangjin@2016/06/07 ADD START
		mShortcutsAndWidgets.setCellDimensions( mCellWidth , mCellHeight , mWidthGap , mHeightGap , mCountX , mCountY );
		// zhangjin@2016/06/07 ADD END
		int count = getChildCount();
		int maxWidth = 0;
		int maxHeight = 0;
		for( int i = 0 ; i < count ; i++ )
		{
			View child = getChildAt( i );
			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( newWidth , MeasureSpec.EXACTLY );
			int childheightMeasureSpec = MeasureSpec.makeMeasureSpec( newHeight , MeasureSpec.EXACTLY );
			child.measure( childWidthMeasureSpec , childheightMeasureSpec );
			maxWidth = Math.max( maxWidth , child.getMeasuredWidth() );
			maxHeight = Math.max( maxHeight , child.getMeasuredHeight() );
		}
		if( mFixedWidth > 0 && mFixedHeight > 0 )
		{
			setMeasuredDimension( maxWidth , maxHeight );
		}
		else
		{
			setMeasuredDimension( widthSize , heightSize );
		}
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int l ,
			int t ,
			int r ,
			int b )
	{
		int offset = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - ( mCountX * mCellWidth + Math.max( mCountX - 1 , 0 ) * mWidthGap );
		int left = getPaddingLeft() + (int)Math.ceil( offset / 2f );
		int top = getPaddingTop();
		int bottom = getPaddingBottom();
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View child = getChildAt( i );
			//cheyingkun add	//phenix1.1稳定版移植酷生活
			//			child.layout( left , top , left + r - l , top + b - t );//cheyingkun del
			//cheyingkun add start
			//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
			if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
			{
				if( child instanceof ImageView )
				{
					LayoutParams lp = (LayoutParams)child.getLayoutParams();
					child.layout( getMeasuredWidth() / 2 - homeViewLeftPosition , -homeViewTopPosition , getMeasuredWidth() / 2 + homeViewLeftPosition , homeViewBottomPosition );
					break;
				}
			}
			//gaominghui add end
			if( child.getLayoutParams() instanceof LayoutParams )
			{
				LayoutParams lp = (LayoutParams)child.getLayoutParams();
				if( mIsFunctionPage )
				{
					child.layout( lp.leftMargin , top + lp.topMargin , r - l - lp.rightMargin , b - t - lp.bottomMargin );
				}
				else
				{
					//lvjiangbin change begin 计算cellWidthGapPx时候并没有将shortcutAndWidgetContent的padding计算在内  现在添加。 解决 i_0012814: 【文件夹】运营文件夹拖动到最右边一列，右上角数字图标被截
					child.layout(
							left + lp.leftMargin - mShortcutsAndWidgets.getPaddingLeft() ,
							top + lp.topMargin ,
							-left + r - l - lp.rightMargin + mShortcutsAndWidgets.getPaddingRight() + mShortcutsAndWidgets.getPaddingLeft() ,
							-bottom + b - t - lp.bottomMargin );//zhujieping modify,否则设置bottom无效
					//				child.layout(
					//						left + lp.leftMargin  ,
					//						top + lp.topMargin ,
					//						-left + r - l - lp.rightMargin ,
					//						-top * 2 + b - t - lp.bottomMargin );
					//lvjiangbin change end
				}
			}
			//cheyingkun add end
			//cheyingkun end
		}
	}
	
	@Override
	protected void onSizeChanged(
			int w ,
			int h ,
			int oldw ,
			int oldh )
	{
		super.onSizeChanged( w , h , oldw , oldh );
		// Expand the background drawing bounds by the padding baked into the background drawable
		Rect padding = new Rect();
		mNormalBackground.getPadding( padding );
		//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
		{
			mBackgroundRect.set( -celllayoutPaddingLeft , -celllayoutPaddingTop , w + celllayoutPaddingLeft , h + celllayoutPaddingBottom );
		}
		//gaominghui add end
		else if( mBackgroundTopPadding != -1 && mBackgroundBottomPadding != -1 && mBackgroundLeftPadding != -1 && mBackgroundRightPadding != -1 )
		{
			mBackgroundRect.set( -mBackgroundLeftPadding , -mBackgroundTopPadding , w + mBackgroundRightPadding , h + mBackgroundBottomPadding );
		}
		else
		{
			mBackgroundRect.set( -padding.left , -padding.top , w + padding.right , h + padding.bottom );
		}
		mForegroundRect.set( mForegroundPadding , mForegroundPadding , w - mForegroundPadding , h - mForegroundPadding );
	}
	
	@Override
	protected void setChildrenDrawingCacheEnabled(
			boolean enabled )
	{
		mShortcutsAndWidgets.setChildrenDrawingCacheEnabled( enabled );
	}
	
	@Override
	protected void setChildrenDrawnWithCacheEnabled(
			boolean enabled )
	{
		mShortcutsAndWidgets.setChildrenDrawnWithCacheEnabled( enabled );
	}
	
	public float getBackgroundAlpha()
	{
		return mBackgroundAlpha;
	}
	
	public void setBackgroundAlphaMultiplier(
			float multiplier )
	{
		if( mBackgroundAlphaMultiplier != multiplier )
		{
			mBackgroundAlphaMultiplier = multiplier;
			invalidate();
		}
	}
	
	public float getBackgroundAlphaMultiplier()
	{
		return mBackgroundAlphaMultiplier;
	}
	
	public void setBackgroundAlpha(
			float alpha )
	{
		if( mBackgroundAlpha != alpha )
		{
			mBackgroundAlpha = alpha;
			invalidate();
		}
	}
	
	public void setShortcutAndWidgetAlpha(
			float alpha )
	{
		final int childCount = getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			getChildAt( i ).setAlpha( alpha );
		}
	}
	
	public ShortcutAndWidgetContainer getShortcutsAndWidgets()
	{
		//gaominghui add start  //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
		int childCount = getChildCount();
		if( childCount > 0 )
		{
			for( int i = 0 ; i < getChildCount() ; i++ )
			{
				if( getChildAt( i ) instanceof ShortcutAndWidgetContainer )
				{
					return (ShortcutAndWidgetContainer)getChildAt( i );
				}
			}
		}
		//gaominghui add end 
		return null;
	}
	
	public View getChildAt(
			int x ,
			int y )
	{
		return mShortcutsAndWidgets.getChildAt( x , y );
	}
	
	public boolean animateChildToPosition(
			final View child ,
			int cellX ,
			int cellY ,
			int duration ,
			int delay ,
			boolean permanent ,
			boolean adjustOccupied )
	{
		ShortcutAndWidgetContainer clc = getShortcutsAndWidgets();
		boolean[][] occupied = mOccupied;
		if( !permanent )
		{
			occupied = mTmpOccupied;
		}
		if( clc.indexOfChild( child ) != -1 )
		{
			final LayoutParams lp = (LayoutParams)child.getLayoutParams();
			final ItemInfo info = (ItemInfo)child.getTag();
			// We cancel any existing animations
			if( mReorderAnimators.containsKey( lp ) )
			{
				mReorderAnimators.get( lp ).cancel();
				mReorderAnimators.remove( lp );
			}
			final int oldX = lp.x;
			final int oldY = lp.y;
			if( adjustOccupied )
			{
				occupied[lp.cellX][lp.cellY] = false;
				occupied[cellX][cellY] = true;
			}
			lp.isLockedToGrid = true;
			if( permanent )
			{
				lp.cellX = cellX;
				lp.cellY = cellY;
				info.setCellX( cellX );
				info.setCellY( cellY );
			}
			else
			{
				lp.tmpCellX = cellX;
				lp.tmpCellY = cellY;
			}
			clc.setupLp( lp );
			lp.isLockedToGrid = false;
			final int newX = lp.x;
			final int newY = lp.y;
			lp.x = oldX;
			lp.y = oldY;
			// Exit early if we're not actually moving the view
			if( oldX == newX && oldY == newY )
			{
				lp.isLockedToGrid = true;
				return true;
			}
			ValueAnimator va = LauncherAnimUtils.ofFloat( child , 0f , 1f );
			va.setDuration( duration );
			mReorderAnimators.put( lp , va );
			va.addUpdateListener( new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					float r = ( (Float)animation.getAnimatedValue() ).floatValue();
					lp.x = (int)( ( 1 - r ) * oldX + r * newX );
					lp.y = (int)( ( 1 - r ) * oldY + r * newY );
					child.requestLayout();
				}
			} );
			va.addListener( new AnimatorListenerAdapter() {
				
				boolean cancelled = false;
				
				public void onAnimationEnd(
						Animator animation )
				{
					// If the animation was cancelled, it means that another animation
					// has interrupted this one, and we don't want to lock the item into
					// place just yet.
					if( !cancelled )
					{
						lp.isLockedToGrid = true;
						child.requestLayout();
					}
					if( mReorderAnimators.containsKey( lp ) )
					{
						mReorderAnimators.remove( lp );
					}
				}
				
				public void onAnimationCancel(
						Animator animation )
				{
					cancelled = true;
				}
			} );
			va.setStartDelay( delay );
			va.start();
			return true;
		}
		return false;
	}
	
	/**
	 * Estimate where the top left cell of the dragged item will land if it is dropped.
	 *
	 * @param originX The X value of the top left corner of the item
	 * @param originY The Y value of the top left corner of the item
	 * @param spanX The number of horizontal cells that the item spans
	 * @param spanY The number of vertical cells that the item spans
	 * @param result The estimated drop cell X and Y.
	 */
	void estimateDropCell(
			int originX ,
			int originY ,
			int spanX ,
			int spanY ,
			int[] result )
	{
		final int countX = mCountX;
		final int countY = mCountY;
		// pointToCellRounded takes the top left of a cell but will pad that with
		// cellWidth/2 and cellHeight/2 when finding the matching cell
		pointToCellRounded( originX , originY , result );
		// If the item isn't fully on this screen, snap to the edges
		int rightOverhang = result[0] + spanX - countX;
		if( rightOverhang > 0 )
		{
			result[0] -= rightOverhang; // Snap to right
		}
		result[0] = Math.max( 0 , result[0] ); // Snap to left
		int bottomOverhang = result[1] + spanY - countY;
		if( bottomOverhang > 0 )
		{
			result[1] -= bottomOverhang; // Snap to bottom
		}
		result[1] = Math.max( 0 , result[1] ); // Snap to top
	}
	
	void visualizeDropLocation(
			View v ,
			Bitmap dragOutline ,
			int originX ,
			int originY ,
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			boolean resize ,
			Point dragOffset ,
			Rect dragRegion )
	{
		final int oldDragCellX = mDragCell[0];
		final int oldDragCellY = mDragCell[1];
		if( dragOutline == null || v == null )
		{
			return;
		}
		if( cellX != oldDragCellX || cellY != oldDragCellY )
		{
			mDragCell[0] = cellX;
			mDragCell[1] = cellY;
			// Find the top left corner of the rect the object will occupy
			final int[] topLeft = mTmpPoint;
			cellToPoint( cellX , cellY , topLeft );
			int left = topLeft[0];
			int top = topLeft[1];
			if( v != null && dragOffset == null )
			{
				// When drawing the drag outline, it did not account for margin offsets
				// added by the view's parent.
				MarginLayoutParams lp = (MarginLayoutParams)v.getLayoutParams();
				left += lp.leftMargin;
				top += lp.topMargin;
				// Offsets due to the size difference between the View and the dragOutline.
				// There is a size difference to account for the outer blur, which may lie
				// outside the bounds of the view.
				top += ( v.getHeight() - dragOutline.getHeight() ) / 2;
				// We center about the x axis
				left += ( ( mCellWidth * spanX ) + ( ( spanX - 1 ) * mWidthGap ) - dragOutline.getWidth() ) / 2;
			}
			else
			{
				if( dragOffset != null && dragRegion != null )
				{
					// Center the drag region *horizontally* in the cell and apply a drag
					// outline offset
					left += dragOffset.x + ( ( mCellWidth * spanX ) + ( ( spanX - 1 ) * mWidthGap ) - dragRegion.width() ) / 2;
					//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
					//xiatian del start
					//					int cHeight = getShortcutsAndWidgets().getCellContentHeight();
					//					int cellPaddingY = (int)Math.max( 0 , ( ( mCellHeight - cHeight ) / 2f ) );
					//xiatian del end
					int cellPaddingY = getCellPaddingY();//xiatian add
					//xiatian end
					//dragview投影的绝对top
					top += dragOffset.y + cellPaddingY;
					//cheyingkun add start	//解决“长按桌面图标，投影偏上”的问题。
					if( v != null )
					{
						DeviceProfile grid = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
						//这里不应该计算v.getpaddingtop，这是拖动图标的paddingtop，应该计算要放下位置的paddingtop
						if( mIsHotseat )
						{
							top += grid.getHotseatItemPaddingTopInCell();//celllayout设置了padding，这里再加
						}
						else
						{
							top += grid.getItemPaddingTopInCell();
						}
					}
					//cheyingkun add end
				}
				else
				{
					// Center the drag outline in the cell
					left += ( ( mCellWidth * spanX ) + ( ( spanX - 1 ) * mWidthGap ) - dragOutline.getWidth() ) / 2;
					top += ( ( mCellHeight * spanY ) + ( ( spanY - 1 ) * mHeightGap ) - dragOutline.getHeight() ) / 2;
				}
			}
			final int oldIndex = mDragOutlineCurrent;
			mDragOutlineAnims[oldIndex].animateOut();
			mDragOutlineCurrent = ( oldIndex + 1 ) % mDragOutlines.length;
			Rect r = mDragOutlines[mDragOutlineCurrent];
			r.set( left , top , left + dragOutline.getWidth() , top + dragOutline.getHeight() );
			if( resize )
			{
				cellToRect( cellX , cellY , spanX , spanY , r );
			}
			mDragOutlineAnims[mDragOutlineCurrent].setTag( dragOutline );
			mDragOutlineAnims[mDragOutlineCurrent].animateIn();
		}
	}
	
	public void clearDragOutlines()
	{
		final int oldIndex = mDragOutlineCurrent;
		mDragOutlineAnims[oldIndex].animateOut();
		mDragCell[0] = mDragCell[1] = -1;
	}
	
	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 *
	 * @param pixelX The X location at which you want to search for a vacant area.
	 * @param pixelY The Y location at which you want to search for a vacant area.
	 * @param spanX Horizontal span of the object.
	 * @param spanY Vertical span of the object.
	 * @param result Array in which to place the result, or null (in which case a new array will
	 *        be allocated)
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	int[] findNearestVacantArea(
			int pixelX ,
			int pixelY ,
			int spanX ,
			int spanY ,
			int[] result )
	{
		return findNearestVacantArea( pixelX , pixelY , spanX , spanY , null , result );
	}
	
	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 *
	 * @param pixelX The X location at which you want to search for a vacant area.
	 * @param pixelY The Y location at which you want to search for a vacant area.
	 * @param minSpanX The minimum horizontal span required
	 * @param minSpanY The minimum vertical span required
	 * @param spanX Horizontal span of the object.
	 * @param spanY Vertical span of the object.
	 * @param result Array in which to place the result, or null (in which case a new array will
	 *        be allocated)
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	int[] findNearestVacantArea(
			int pixelX ,
			int pixelY ,
			int minSpanX ,
			int minSpanY ,
			int spanX ,
			int spanY ,
			int[] result ,
			int[] resultSpan )
	{
		return findNearestVacantArea( pixelX , pixelY , minSpanX , minSpanY , spanX , spanY , null , result , resultSpan );
	}
	
	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 *
	 * @param pixelX The X location at which you want to search for a vacant area.
	 * @param pixelY The Y location at which you want to search for a vacant area.
	 * @param spanX Horizontal span of the object.
	 * @param spanY Vertical span of the object.
	 * @param ignoreOccupied If true, the result can be an occupied cell
	 * @param result Array in which to place the result, or null (in which case a new array will
	 *        be allocated)
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	int[] findNearestArea(
			int pixelX ,
			int pixelY ,
			int spanX ,
			int spanY ,
			View ignoreView ,
			boolean ignoreOccupied ,
			int[] result )
	{
		return findNearestArea( pixelX , pixelY , spanX , spanY , spanX , spanY , ignoreView , ignoreOccupied , result , null , mOccupied );
	}
	
	private final Stack<Rect> mTempRectStack = new Stack<Rect>();
	
	private void lazyInitTempRectStack()
	{
		if( mTempRectStack.isEmpty() )
		{
			for( int i = 0 ; i < mCountX * mCountY ; i++ )
			{
				mTempRectStack.push( new Rect() );
			}
		}
	}
	
	private void recycleTempRects(
			Stack<Rect> used )
	{
		while( !used.isEmpty() )
		{
			mTempRectStack.push( used.pop() );
		}
	}
	
	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 *
	 * @param pixelX The X location at which you want to search for a vacant area.
	 * @param pixelY The Y location at which you want to search for a vacant area.
	 * @param minSpanX The minimum horizontal span required
	 * @param minSpanY The minimum vertical span required
	 * @param spanX Horizontal span of the object.
	 * @param spanY Vertical span of the object.
	 * @param ignoreOccupied If true, the result can be an occupied cell
	 * @param result Array in which to place the result, or null (in which case a new array will
	 *        be allocated)
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	int[] findNearestArea(
			int pixelX ,
			int pixelY ,
			int minSpanX ,
			int minSpanY ,
			int spanX ,
			int spanY ,
			View ignoreView ,
			boolean ignoreOccupied ,
			int[] result ,
			int[] resultSpan ,
			boolean[][] occupied )
	{
		lazyInitTempRectStack();
		// mark space take by ignoreView as available (method checks if ignoreView is null)
		markCellsAsUnoccupiedForView( ignoreView , occupied );
		// For items with a spanX / spanY > 1, the passed in point (pixelX, pixelY) corresponds
		// to the center of the item, but we are searching based on the top-left cell, so
		// we translate the point over to correspond to the top-left.
		pixelX -= ( mCellWidth + mWidthGap ) * ( spanX - 1 ) / 2f;
		pixelY -= ( mCellHeight + mHeightGap ) * ( spanY - 1 ) / 2f;
		// Keep track of best-scoring drop area
		final int[] bestXY = result != null ? result : new int[2];
		double bestDistance = Double.MAX_VALUE;
		final Rect bestRect = new Rect( -1 , -1 , -1 , -1 );
		final Stack<Rect> validRegions = new Stack<Rect>();
		final int countX = mCountX;
		final int countY = mCountY;
		if( minSpanX <= 0 || minSpanY <= 0 || spanX <= 0 || spanY <= 0 || spanX < minSpanX || spanY < minSpanY )
		{
			return bestXY;
		}
		for( int y = 0 ; y < countY - ( minSpanY - 1 ) ; y++ )
		{
			inner:
			for( int x = 0 ; x < countX - ( minSpanX - 1 ) ; x++ )
			{
				int ySize = -1;
				int xSize = -1;
				if( ignoreOccupied )
				{
					// First, let's see if this thing fits anywhere
					for( int i = 0 ; i < minSpanX ; i++ )
					{
						for( int j = 0 ; j < minSpanY ; j++ )
						{
							if( occupied[x + i][y + j] )
							{
								continue inner;
							}
						}
					}
					xSize = minSpanX;
					ySize = minSpanY;
					// We know that the item will fit at _some_ acceptable size, now let's see
					// how big we can make it. We'll alternate between incrementing x and y spans
					// until we hit a limit.
					boolean incX = true;
					boolean hitMaxX = xSize >= spanX;
					boolean hitMaxY = ySize >= spanY;
					while( !( hitMaxX && hitMaxY ) )
					{
						if( incX && !hitMaxX )
						{
							for( int j = 0 ; j < ySize ; j++ )
							{
								if( x + xSize > countX - 1 || occupied[x + xSize][y + j] )
								{
									// We can't move out horizontally
									hitMaxX = true;
								}
							}
							if( !hitMaxX )
							{
								xSize++;
							}
						}
						else if( !hitMaxY )
						{
							for( int i = 0 ; i < xSize ; i++ )
							{
								if( y + ySize > countY - 1 || occupied[x + i][y + ySize] )
								{
									// We can't move out vertically
									hitMaxY = true;
								}
							}
							if( !hitMaxY )
							{
								ySize++;
							}
						}
						hitMaxX |= xSize >= spanX;
						hitMaxY |= ySize >= spanY;
						incX = !incX;
					}
					incX = true;
					hitMaxX = xSize >= spanX;
					hitMaxY = ySize >= spanY;
				}
				final int[] cellXY = mTmpXY;
				cellToCenterPoint( x , y , cellXY );
				// We verify that the current rect is not a sub-rect of any of our previous
				// candidates. In this case, the current rect is disqualified in favour of the
				// containing rect.
				Rect currentRect = mTempRectStack.pop();
				currentRect.set( x , y , x + xSize , y + ySize );
				boolean contained = false;
				for( Rect r : validRegions )
				{
					if( r.contains( currentRect ) )
					{
						contained = true;
						break;
					}
				}
				validRegions.push( currentRect );
				double distance = Math.sqrt( Math.pow( cellXY[0] - pixelX , 2 ) + Math.pow( cellXY[1] - pixelY , 2 ) );
				if( ( distance <= bestDistance && !contained ) || currentRect.contains( bestRect ) )
				{
					bestDistance = distance;
					bestXY[0] = x;
					bestXY[1] = y;
					if( resultSpan != null )
					{
						resultSpan[0] = xSize;
						resultSpan[1] = ySize;
					}
					bestRect.set( currentRect );
				}
			}
		}
		// re-mark space taken by ignoreView as occupied
		markCellsAsOccupiedForView( ignoreView , occupied );
		// Return -1, -1 if no suitable location found
		if( bestDistance == Double.MAX_VALUE )
		{
			bestXY[0] = -1;
			bestXY[1] = -1;
		}
		recycleTempRects( validRegions );
		return bestXY;
	}
	
	/**
	* Find a vacant area that will fit the given bounds nearest the requested
	* cell location, and will also weigh in a suggested direction vector of the
	* desired location. This method computers distance based on unit grid distances,
	* not pixel distances.
	*
	* @param cellX The X cell nearest to which you want to search for a vacant area.
	* @param cellY The Y cell nearest which you want to search for a vacant area.
	* @param spanX Horizontal span of the object.
	* @param spanY Vertical span of the object.
	* @param direction The favored direction in which the views should move from x, y
	* @param exactDirectionOnly If this parameter is true, then only solutions where the direction
	*        matches exactly. Otherwise we find the best matching direction.
	* @param occoupied The array which represents which cells in the CellLayout are occupied
	* @param blockOccupied The array which represents which cells in the specified block (cellX,
	*        cellY, spanX, spanY) are occupied. This is used when try to move a group of views.
	* @param result Array in which to place the result, or null (in which case a new array will
	*        be allocated)
	* @return The X, Y cell of a vacant area that can contain this object,
	*         nearest the requested location.
	*/
	private int[] findNearestArea(
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			int[] direction ,
			boolean[][] occupied ,
			boolean blockOccupied[][] ,
			int[] result )
	{
		// Keep track of best-scoring drop area
		final int[] bestXY = result != null ? result : new int[2];
		float bestDistance = Float.MAX_VALUE;
		int bestDirectionScore = Integer.MIN_VALUE;
		final int countX = mCountX;
		final int countY = mCountY;
		for( int y = 0 ; y < countY - ( spanY - 1 ) ; y++ )
		{
			inner:
			for( int x = 0 ; x < countX - ( spanX - 1 ) ; x++ )
			{
				// First, let's see if this thing fits anywhere
				for( int i = 0 ; i < spanX ; i++ )
				{
					for( int j = 0 ; j < spanY ; j++ )
					{
						if( occupied[x + i][y + j] && ( blockOccupied == null || blockOccupied[i][j] ) )
						{
							continue inner;
						}
					}
				}
				float distance = (float)Math.sqrt( ( x - cellX ) * ( x - cellX ) + ( y - cellY ) * ( y - cellY ) );
				int[] curDirection = mTmpPoint;
				computeDirectionVector( x - cellX , y - cellY , curDirection );
				// The direction score is just the dot product of the two candidate direction
				// and that passed in.
				int curDirectionScore = direction[0] * curDirection[0] + direction[1] * curDirection[1];
				boolean exactDirectionOnly = false;
				boolean directionMatches = direction[0] == curDirection[0] && direction[0] == curDirection[0];
				if( ( directionMatches || !exactDirectionOnly ) && Float.compare( distance , bestDistance ) < 0 || ( Float.compare( distance , bestDistance ) == 0 && curDirectionScore > bestDirectionScore ) )
				{
					bestDistance = distance;
					bestDirectionScore = curDirectionScore;
					bestXY[0] = x;
					bestXY[1] = y;
				}
			}
		}
		// Return -1, -1 if no suitable location found
		if( bestDistance == Float.MAX_VALUE )
		{
			bestXY[0] = -1;
			bestXY[1] = -1;
		}
		return bestXY;
	}
	
	private boolean addViewToTempLocation(
			View v ,
			Rect rectOccupiedByPotentialDrop ,
			int[] direction ,
			ItemConfiguration currentState )
	{
		CellAndSpan c = currentState.map.get( v );
		boolean success = false;
		markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , false );
		markCellsForRect( rectOccupiedByPotentialDrop , mTmpOccupied , true );
		findNearestArea( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , direction , mTmpOccupied , null , mTempLocation );
		if( mTempLocation[0] >= 0 && mTempLocation[1] >= 0 )
		{
			c.setX( mTempLocation[0] );
			c.setY( mTempLocation[1] );
			success = true;
		}
		markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , true );
		return success;
	}
	
	/**
	 * This helper class defines a cluster of views. It helps with defining complex edges
	 * of the cluster and determining how those edges interact with other views. The edges
	 * essentially define a fine-grained boundary around the cluster of views -- like a more
	 * precise version of a bounding box.
	 */
	private class ViewCluster
	{
		
		final static int LEFT = 0;
		final static int TOP = 1;
		final static int RIGHT = 2;
		final static int BOTTOM = 3;
		ArrayList<View> views;
		ItemConfiguration config;
		Rect boundingRect = new Rect();
		int[] leftEdge = new int[mCountY];
		int[] rightEdge = new int[mCountY];
		int[] topEdge = new int[mCountX];
		int[] bottomEdge = new int[mCountX];
		boolean leftEdgeDirty , rightEdgeDirty , topEdgeDirty , bottomEdgeDirty , boundingRectDirty;
		
		@SuppressWarnings( "unchecked" )
		public ViewCluster(
				ArrayList<View> views ,
				ItemConfiguration config )
		{
			this.views = (ArrayList<View>)views.clone();
			this.config = config;
			resetEdges();
		}
		
		void resetEdges()
		{
			for( int i = 0 ; i < mCountX ; i++ )
			{
				topEdge[i] = -1;
				bottomEdge[i] = -1;
			}
			for( int i = 0 ; i < mCountY ; i++ )
			{
				leftEdge[i] = -1;
				rightEdge[i] = -1;
			}
			leftEdgeDirty = true;
			rightEdgeDirty = true;
			bottomEdgeDirty = true;
			topEdgeDirty = true;
			boundingRectDirty = true;
		}
		
		void computeEdge(
				int which ,
				int[] edge )
		{
			int count = views.size();
			for( int i = 0 ; i < count ; i++ )
			{
				CellAndSpan cs = config.map.get( views.get( i ) );
				switch( which )
				{
					case LEFT:
						int left = cs.getX();
						for( int j = cs.getY() ; j < cs.getY() + cs.getSpanY() ; j++ )
						{
							if( ( j >= 0 && j < edge.length ) && ( left < edge[j] || edge[j] < 0 ) )//zhujieping add,增加防止数据越界的保护
							{
								edge[j] = left;
							}
						}
						break;
					case RIGHT:
						int right = cs.getX() + cs.getSpanX();
						for( int j = cs.getY() ; j < cs.getY() + cs.getSpanY() ; j++ )
						{
							if( ( j >= 0 && j < edge.length ) && right > edge[j] )//zhujieping add,增加防止数据越界的保护
							{
								edge[j] = right;
							}
						}
						break;
					case TOP:
						int top = cs.getY();
						for( int j = cs.getX() ; j < cs.getX() + cs.getSpanX() ; j++ )
						{
							if( ( j >= 0 && j < edge.length ) && ( top < edge[j] || edge[j] < 0 ) )//zhujieping add,增加防止数据越界的保护
							{
								edge[j] = top;
							}
						}
						break;
					case BOTTOM:
						int bottom = cs.getY() + cs.getSpanY();
						for( int j = cs.getX() ; j < cs.getX() + cs.getSpanX() ; j++ )
						{
							if( ( j >= 0 && j < edge.length ) && bottom > edge[j] )//zhujieping add,增加防止数据越界的保护
							{
								edge[j] = bottom;
							}
						}
						break;
				}
			}
		}
		
		boolean isViewTouchingEdge(
				View v ,
				int whichEdge )
		{
			CellAndSpan cs = config.map.get( v );
			int[] edge = getEdge( whichEdge );
			switch( whichEdge )
			{
				case LEFT:
					for( int i = cs.getY() ; i < cs.getY() + cs.getSpanY() ; i++ )
					{
						if( edge[i] == cs.getX() + cs.getSpanX() )
						{
							return true;
						}
					}
					break;
				case RIGHT:
					for( int i = cs.getY() ; i < cs.getY() + cs.getSpanY() ; i++ )
					{
						if( edge[i] == cs.getX() )
						{
							return true;
						}
					}
					break;
				case TOP:
					for( int i = cs.getX() ; i < cs.getX() + cs.getSpanX() ; i++ )
					{
						if( edge[i] == cs.getY() + cs.getSpanY() )
						{
							return true;
						}
					}
					break;
				case BOTTOM:
					for( int i = cs.getX() ; i < cs.getX() + cs.getSpanX() ; i++ )
					{
						if( edge[i] == cs.getY() )
						{
							return true;
						}
					}
					break;
			}
			return false;
		}
		
		void shift(
				int whichEdge ,
				int delta )
		{
			for( View v : views )
			{
				CellAndSpan c = config.map.get( v );
				switch( whichEdge )
				{
					case LEFT:
						c.setX( c.getX() - delta );
						break;
					case RIGHT:
						c.setX( c.getX() + delta );
						break;
					case TOP:
						c.setY( c.getY() - delta );
						break;
					case BOTTOM:
					default:
						c.setY( c.getY() + delta );
						break;
				}
			}
			resetEdges();
		}
		
		public void addView(
				View v )
		{
			views.add( v );
			resetEdges();
		}
		
		public Rect getBoundingRect()
		{
			if( boundingRectDirty )
			{
				boolean first = true;
				for( View v : views )
				{
					CellAndSpan c = config.map.get( v );
					if( first )
					{
						boundingRect.set( c.getX() , c.getY() , c.getX() + c.getSpanX() , c.getY() + c.getSpanY() );
						first = false;
					}
					else
					{
						boundingRect.union( c.getX() , c.getY() , c.getX() + c.getSpanX() , c.getY() + c.getSpanY() );
					}
				}
			}
			return boundingRect;
		}
		
		public int[] getEdge(
				int which )
		{
			switch( which )
			{
				case LEFT:
					return getLeftEdge();
				case RIGHT:
					return getRightEdge();
				case TOP:
					return getTopEdge();
				case BOTTOM:
				default:
					return getBottomEdge();
			}
		}
		
		public int[] getLeftEdge()
		{
			if( leftEdgeDirty )
			{
				computeEdge( LEFT , leftEdge );
			}
			return leftEdge;
		}
		
		public int[] getRightEdge()
		{
			if( rightEdgeDirty )
			{
				computeEdge( RIGHT , rightEdge );
			}
			return rightEdge;
		}
		
		public int[] getTopEdge()
		{
			if( topEdgeDirty )
			{
				computeEdge( TOP , topEdge );
			}
			return topEdge;
		}
		
		public int[] getBottomEdge()
		{
			if( bottomEdgeDirty )
			{
				computeEdge( BOTTOM , bottomEdge );
			}
			return bottomEdge;
		}
		
		PositionComparator comparator = new PositionComparator();
		
		class PositionComparator implements Comparator<View>
		{
			
			int whichEdge = 0;
			
			public int compare(
					View left ,
					View right )
			{
				if( !( left instanceof View && right instanceof View ) )
				{
					return 0;
				}
				CellAndSpan l = config.map.get( left );
				CellAndSpan r = config.map.get( right );
				switch( whichEdge )
				{
					case LEFT:
						return ( r.getX() + r.getSpanX() ) - ( l.getX() + l.getSpanX() );
					case RIGHT:
						return l.getX() - r.getX();
					case TOP:
						return ( r.getY() + r.getSpanY() ) - ( l.getY() + l.getSpanY() );
					case BOTTOM:
					default:
						return l.getY() - r.getY();
				}
			}
		}
		
		public void sortConfigurationForEdgePush(
				int edge )
		{
			comparator.whichEdge = edge;
			Collections.sort( config.sortedViews , comparator );
		}
	}
	
	private boolean pushViewsToTempLocation(
			ArrayList<View> views ,
			Rect rectOccupiedByPotentialDrop ,
			int[] direction ,
			View dragView ,
			ItemConfiguration currentState )
	{
		ViewCluster cluster = new ViewCluster( views , currentState );
		Rect clusterRect = cluster.getBoundingRect();
		int whichEdge;
		int pushDistance;
		boolean fail = false;
		// Determine the edge of the cluster that will be leading the push and how far
		// the cluster must be shifted.
		if( direction[0] < 0 )
		{
			whichEdge = ViewCluster.LEFT;
			pushDistance = clusterRect.right - rectOccupiedByPotentialDrop.left;
		}
		else if( direction[0] > 0 )
		{
			whichEdge = ViewCluster.RIGHT;
			pushDistance = rectOccupiedByPotentialDrop.right - clusterRect.left;
		}
		else if( direction[1] < 0 )
		{
			whichEdge = ViewCluster.TOP;
			pushDistance = clusterRect.bottom - rectOccupiedByPotentialDrop.top;
		}
		else
		{
			whichEdge = ViewCluster.BOTTOM;
			pushDistance = rectOccupiedByPotentialDrop.bottom - clusterRect.top;
		}
		// Break early for invalid push distance.
		if( pushDistance <= 0 )
		{
			return false;
		}
		// Mark the occupied state as false for the group of views we want to move.
		for( View v : views )
		{
			CellAndSpan c = currentState.map.get( v );
			markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , false );
		}
		// We save the current configuration -- if we fail to find a solution we will revert
		// to the initial state. The process of finding a solution modifies the configuration
		// in place, hence the need for revert in the failure case.
		currentState.save();
		// The pushing algorithm is simplified by considering the views in the order in which
		// they would be pushed by the cluster. For example, if the cluster is leading with its
		// left edge, we consider sort the views by their right edge, from right to left.
		cluster.sortConfigurationForEdgePush( whichEdge );
		while( pushDistance > 0 && !fail )
		{
			for( View v : currentState.sortedViews )
			{
				// For each view that isn't in the cluster, we see if the leading edge of the
				// cluster is contacting the edge of that view. If so, we add that view to the
				// cluster.
				if( !cluster.views.contains( v ) && v != dragView )
				{
					if( cluster.isViewTouchingEdge( v , whichEdge ) )
					{
						LayoutParams lp = (LayoutParams)v.getLayoutParams();
						if( !lp.canReorder )
						{
							// The push solution includes the all apps button, this is not viable.
							fail = true;
							break;
						}
						cluster.addView( v );
						CellAndSpan c = currentState.map.get( v );
						// Adding view to cluster, mark it as not occupied.
						markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , false );
					}
				}
			}
			pushDistance--;
			// The cluster has been completed, now we move the whole thing over in the appropriate
			// direction.
			cluster.shift( whichEdge , 1 );
		}
		boolean foundSolution = false;
		clusterRect = cluster.getBoundingRect();
		// Due to the nature of the algorithm, the only check required to verify a valid solution
		// is to ensure that completed shifted cluster lies completely within the cell layout.
		if( !fail && clusterRect.left >= 0 && clusterRect.right <= mCountX && clusterRect.top >= 0 && clusterRect.bottom <= mCountY )
		{
			foundSolution = true;
		}
		else
		{
			currentState.restore();
		}
		// In either case, we set the occupied array as marked for the location of the views
		for( View v : cluster.views )
		{
			CellAndSpan c = currentState.map.get( v );
			markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , true );
		}
		return foundSolution;
	}
	
	private boolean addViewsToTempLocation(
			ArrayList<View> views ,
			Rect rectOccupiedByPotentialDrop ,
			int[] direction ,
			View dragView ,
			ItemConfiguration currentState )
	{
		if( views.size() == 0 )
			return true;
		boolean success = false;
		Rect boundingRect = null;
		// We construct a rect which represents the entire group of views passed in
		for( View v : views )
		{
			CellAndSpan c = currentState.map.get( v );
			if( boundingRect == null )
			{
				boundingRect = new Rect( c.getX() , c.getY() , c.getX() + c.getSpanX() , c.getY() + c.getSpanY() );
			}
			else
			{
				boundingRect.union( c.getX() , c.getY() , c.getX() + c.getSpanX() , c.getY() + c.getSpanY() );
			}
		}
		// Mark the occupied state as false for the group of views we want to move.
		for( View v : views )
		{
			CellAndSpan c = currentState.map.get( v );
			markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , false );
		}
		boolean[][] blockOccupied = new boolean[boundingRect.width()][boundingRect.height()];
		int top = boundingRect.top;
		int left = boundingRect.left;
		// We mark more precisely which parts of the bounding rect are truly occupied, allowing
		// for interlocking.
		for( View v : views )
		{
			CellAndSpan c = currentState.map.get( v );
			markCellsForView( c.getX() - left , c.getY() - top , c.getSpanX() , c.getSpanY() , blockOccupied , true );
		}
		markCellsForRect( rectOccupiedByPotentialDrop , mTmpOccupied , true );
		findNearestArea( boundingRect.left , boundingRect.top , boundingRect.width() , boundingRect.height() , direction , mTmpOccupied , blockOccupied , mTempLocation );
		// If we successfuly found a location by pushing the block of views, we commit it
		if( mTempLocation[0] >= 0 && mTempLocation[1] >= 0 )
		{
			int deltaX = mTempLocation[0] - boundingRect.left;
			int deltaY = mTempLocation[1] - boundingRect.top;
			for( View v : views )
			{
				CellAndSpan c = currentState.map.get( v );
				c.setX( c.getX() + deltaX );
				c.setY( c.getY() + deltaY );
			}
			success = true;
		}
		// In either case, we set the occupied array as marked for the location of the views
		for( View v : views )
		{
			CellAndSpan c = currentState.map.get( v );
			markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , true );
		}
		return success;
	}
	
	private void markCellsForRect(
			Rect r ,
			boolean[][] occupied ,
			boolean value )
	{
		markCellsForView( r.left , r.top , r.width() , r.height() , occupied , value );
	}
	
	// This method tries to find a reordering solution which satisfies the push mechanic by trying
	// to push items in each of the cardinal directions, in an order based on the direction vector
	// passed.
	private boolean attemptPushInDirection(
			ArrayList<View> intersectingViews ,
			Rect occupied ,
			int[] direction ,
			View ignoreView ,
			ItemConfiguration solution )
	{
		if( ( Math.abs( direction[0] ) + Math.abs( direction[1] ) ) > 1 )
		{
			// If the direction vector has two non-zero components, we try pushing
			// separately in each of the components.
			int temp = direction[1];
			direction[1] = 0;
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			direction[1] = temp;
			temp = direction[0];
			direction[0] = 0;
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			// Revert the direction
			direction[0] = temp;
			// Now we try pushing in each component of the opposite direction
			direction[0] *= -1;
			direction[1] *= -1;
			temp = direction[1];
			direction[1] = 0;
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			direction[1] = temp;
			temp = direction[0];
			direction[0] = 0;
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			// revert the direction
			direction[0] = temp;
			direction[0] *= -1;
			direction[1] *= -1;
		}
		else
		{
			// If the direction vector has a single non-zero component, we push first in the
			// direction of the vector
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			// Then we try the opposite direction
			direction[0] *= -1;
			direction[1] *= -1;
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			// Switch the direction back
			direction[0] *= -1;
			direction[1] *= -1;
			// If we have failed to find a push solution with the above, then we try
			// to find a solution by pushing along the perpendicular axis.
			// Swap the components
			int temp = direction[1];
			direction[1] = direction[0];
			direction[0] = temp;
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			// Then we try the opposite direction
			direction[0] *= -1;
			direction[1] *= -1;
			if( pushViewsToTempLocation( intersectingViews , occupied , direction , ignoreView , solution ) )
			{
				return true;
			}
			// Switch the direction back
			direction[0] *= -1;
			direction[1] *= -1;
			// Swap the components back
			temp = direction[1];
			direction[1] = direction[0];
			direction[0] = temp;
		}
		return false;
	}
	
	private boolean rearrangementExists(
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			int[] direction ,
			View ignoreView ,
			ItemConfiguration solution )
	{
		// Return early if get invalid cell positions
		if( cellX < 0 || cellY < 0 )
			return false;
		mIntersectingViews.clear();
		mOccupiedRect.set( cellX , cellY , cellX + spanX , cellY + spanY );
		// Mark the desired location of the view currently being dragged.
		if( ignoreView != null )
		{
			CellAndSpan c = solution.map.get( ignoreView );
			if( c != null )
			{
				c.setX( cellX );
				c.setY( cellY );
			}
		}
		Rect r0 = new Rect( cellX , cellY , cellX + spanX , cellY + spanY );
		Rect r1 = new Rect();
		for( View child : solution.map.keySet() )
		{
			if( child == ignoreView )
				continue;
			CellAndSpan c = solution.map.get( child );
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			r1.set( c.getX() , c.getY() , c.getX() + c.getSpanX() , c.getY() + c.getSpanY() );
			if( Rect.intersects( r0 , r1 ) )
			{
				if( !lp.canReorder )
				{
					return false;
				}
				mIntersectingViews.add( child );
			}
		}
		// First we try to find a solution which respects the push mechanic. That is,
		// we try to find a solution such that no displaced item travels through another item
		// without also displacing that item.
		if( attemptPushInDirection( mIntersectingViews , mOccupiedRect , direction , ignoreView , solution ) )
		{
			return true;
		}
		// Next we try moving the views as a block, but without requiring the push mechanic.
		if( addViewsToTempLocation( mIntersectingViews , mOccupiedRect , direction , ignoreView , solution ) )
		{
			return true;
		}
		// Ok, they couldn't move as a block, let's move them individually
		for( View v : mIntersectingViews )
		{
			if( !addViewToTempLocation( v , mOccupiedRect , direction , solution ) )
			{
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Returns a pair (x, y), where x,y are in {-1, 0, 1} corresponding to vector between
	 * the provided point and the provided cell
	 */
	private void computeDirectionVector(
			float deltaX ,
			float deltaY ,
			int[] result )
	{
		double angle = Math.atan( ( (float)deltaY ) / deltaX );
		result[0] = 0;
		result[1] = 0;
		if( Math.abs( Math.cos( angle ) ) > 0.5f )
		{
			result[0] = (int)Math.signum( deltaX );
		}
		if( Math.abs( Math.sin( angle ) ) > 0.5f )
		{
			result[1] = (int)Math.signum( deltaY );
		}
	}
	
	private void copyOccupiedArray(
			boolean[][] occupied )
	{
		for( int i = 0 ; i < mCountX ; i++ )
		{
			for( int j = 0 ; j < mCountY ; j++ )
			{
				occupied[i][j] = mOccupied[i][j];
			}
		}
	}
	
	ItemConfiguration simpleSwap(
			int pixelX ,
			int pixelY ,
			int minSpanX ,
			int minSpanY ,
			int spanX ,
			int spanY ,
			int[] direction ,
			View dragView ,
			boolean decX ,
			ItemConfiguration solution )
	{
		// Copy the current state into the solution. This solution will be manipulated as necessary.
		copyCurrentStateToSolution( solution , false );
		// Copy the current occupied array into the temporary occupied array. This array will be
		// manipulated as necessary to find a solution.
		copyOccupiedArray( mTmpOccupied );
		// We find the nearest cell into which we would place the dragged item, assuming there's
		// nothing in its way.
		int result[] = new int[2];
		result = findNearestArea( pixelX , pixelY , spanX , spanY , result );
		boolean success = false;
		// First we try the exact nearest position of the item being dragged,
		// we will then want to try to move this around to other neighbouring positions
		success = rearrangementExists( result[0] , result[1] , spanX , spanY , direction , dragView , solution );
		if( !success )
		{
			// We try shrinking the widget down to size in an alternating pattern, shrink 1 in
			// x, then 1 in y etc.
			if( spanX > minSpanX && ( minSpanY == spanY || decX ) )
			{
				return simpleSwap( pixelX , pixelY , minSpanX , minSpanY , spanX - 1 , spanY , direction , dragView , false , solution );
			}
			else if( spanY > minSpanY )
			{
				return simpleSwap( pixelX , pixelY , minSpanX , minSpanY , spanX , spanY - 1 , direction , dragView , true , solution );
			}
			solution.isSolution = false;
		}
		else
		{
			solution.isSolution = true;
			solution.dragViewX = result[0];
			solution.dragViewY = result[1];
			solution.dragViewSpanX = spanX;
			solution.dragViewSpanY = spanY;
		}
		return solution;
	}
	
	private void copyCurrentStateToSolution(
			ItemConfiguration solution ,
			boolean temp )
	{
		int childCount = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			View child = mShortcutsAndWidgets.getChildAt( i );
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			CellAndSpan c;
			if( temp )
			{
				c = new CellAndSpan( lp.tmpCellX , lp.tmpCellY , lp.cellHSpan , lp.cellVSpan );
			}
			else
			{
				c = new CellAndSpan( lp.cellX , lp.cellY , lp.cellHSpan , lp.cellVSpan );
			}
			solution.add( child , c );
		}
	}
	
	private void copySolutionToTempState(
			ItemConfiguration solution ,
			View dragView )
	{
		for( int i = 0 ; i < mCountX ; i++ )
		{
			for( int j = 0 ; j < mCountY ; j++ )
			{
				mTmpOccupied[i][j] = false;
			}
		}
		int childCount = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			View child = mShortcutsAndWidgets.getChildAt( i );
			if( child == dragView )
				continue;
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			CellAndSpan c = solution.map.get( child );
			if( c != null )
			{
				lp.tmpCellX = c.getX();
				lp.tmpCellY = c.getY();
				lp.cellHSpan = c.getSpanX();
				lp.cellVSpan = c.getSpanY();
				markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , mTmpOccupied , true );
			}
		}
		markCellsForView( solution.dragViewX , solution.dragViewY , solution.dragViewSpanX , solution.dragViewSpanY , mTmpOccupied , true );
	}
	
	private void animateItemsToSolution(
			ItemConfiguration solution ,
			View dragView ,
			boolean commitDragView )
	{
		boolean[][] occupied = DESTRUCTIVE_REORDER ? mOccupied : mTmpOccupied;
		for( int i = 0 ; i < mCountX ; i++ )
		{
			for( int j = 0 ; j < mCountY ; j++ )
			{
				occupied[i][j] = false;
			}
		}
		int childCount = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			View child = mShortcutsAndWidgets.getChildAt( i );
			if( child == dragView )
				continue;
			CellAndSpan c = solution.map.get( child );
			if( c != null )
			{
				animateChildToPosition( child , c.getX() , c.getY() , REORDER_ANIMATION_DURATION , 0 , DESTRUCTIVE_REORDER , false );
				markCellsForView( c.getX() , c.getY() , c.getSpanX() , c.getSpanY() , occupied , true );
			}
		}
		if( commitDragView )
		{
			markCellsForView( solution.dragViewX , solution.dragViewY , solution.dragViewSpanX , solution.dragViewSpanY , occupied , true );
		}
	}
	
	// This method starts or changes the reorder hint animations
	private void beginOrAdjustHintAnimations(
			ItemConfiguration solution ,
			View dragView ,
			int delay )
	{
		int childCount = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			View child = mShortcutsAndWidgets.getChildAt( i );
			if( child == dragView )
				continue;
			CellAndSpan c = solution.map.get( child );
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			if( c != null )
			{
				ReorderHintAnimation rha = new ReorderHintAnimation( child , lp.cellX , lp.cellY , c.getX() , c.getY() , c.getSpanX() , c.getSpanY() );
				rha.animate();
			}
		}
	}
	
	// Class which represents the reorder hint animations. These animations show that an item is
	// in a temporary state, and hint at where the item will return to.
	class ReorderHintAnimation
	{
		
		View child;
		float finalDeltaX;
		float finalDeltaY;
		float initDeltaX;
		float initDeltaY;
		float finalScale;
		float initScale;
		private static final int DURATION = 300;
		Animator a;
		
		public ReorderHintAnimation(
				View child ,
				int cellX0 ,
				int cellY0 ,
				int cellX1 ,
				int cellY1 ,
				int spanX ,
				int spanY )
		{
			regionToCenterPoint( cellX0 , cellY0 , spanX , spanY , mTmpPoint );
			final int x0 = mTmpPoint[0];
			final int y0 = mTmpPoint[1];
			regionToCenterPoint( cellX1 , cellY1 , spanX , spanY , mTmpPoint );
			final int x1 = mTmpPoint[0];
			final int y1 = mTmpPoint[1];
			final int dX = x1 - x0;
			final int dY = y1 - y0;
			finalDeltaX = 0;
			finalDeltaY = 0;
			if( dX == dY && dX == 0 )
			{
			}
			else
			{
				if( dY == 0 )
				{
					finalDeltaX = -Math.signum( dX ) * mReorderHintAnimationMagnitude;
				}
				else if( dX == 0 )
				{
					finalDeltaY = -Math.signum( dY ) * mReorderHintAnimationMagnitude;
				}
				else
				{
					double angle = Math.atan( (float)( dY ) / dX );
					finalDeltaX = (int)( -Math.signum( dX ) * Math.abs( Math.cos( angle ) * mReorderHintAnimationMagnitude ) );
					finalDeltaY = (int)( -Math.signum( dY ) * Math.abs( Math.sin( angle ) * mReorderHintAnimationMagnitude ) );
				}
			}
			initDeltaX = child.getTranslationX();
			initDeltaY = child.getTranslationY();
			finalScale = getChildrenScale() - 4.0f / child.getWidth();
			initScale = child.getScaleX();
			this.child = child;
		}
		
		void animate()
		{
			if( mShakeAnimators.containsKey( child ) )
			{
				ReorderHintAnimation oldAnimation = mShakeAnimators.get( child );
				oldAnimation.cancel();
				mShakeAnimators.remove( child );
				if( finalDeltaX == 0 && finalDeltaY == 0 )
				{
					completeAnimationImmediately();
					return;
				}
			}
			if( finalDeltaX == 0 && finalDeltaY == 0 )
			{
				return;
			}
			ValueAnimator va = LauncherAnimUtils.ofFloat( child , 0f , 1f );
			a = va;
			va.setRepeatMode( ValueAnimator.REVERSE );
			va.setRepeatCount( ValueAnimator.INFINITE );
			va.setDuration( DURATION );
			va.setStartDelay( (int)( Math.random() * 60 ) );
			va.addUpdateListener( new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					float r = ( (Float)animation.getAnimatedValue() ).floatValue();
					float x = r * finalDeltaX + ( 1 - r ) * initDeltaX;
					float y = r * finalDeltaY + ( 1 - r ) * initDeltaY;
					child.setTranslationX( x );
					child.setTranslationY( y );
					float s = r * finalScale + ( 1 - r ) * initScale;
					child.setScaleX( s );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( "i_0011202" , StringUtils.concat( "zjp celllayout animate setScaleX s = " , s ) );
					}
					child.setScaleY( s );
				}
			} );
			va.addListener( new AnimatorListenerAdapter() {
				
				public void onAnimationRepeat(
						Animator animation )
				{
					// We make sure to end only after a full period
					initDeltaX = 0;
					initDeltaY = 0;
					initScale = getChildrenScale();
				}
			} );
			mShakeAnimators.put( child , this );
			va.start();
		}
		
		private void cancel()
		{
			if( a != null )
			{
				a.cancel();
			}
		}
		
		private void completeAnimationImmediately()
		{
			if( a != null )
			{
				a.cancel();
			}
			AnimatorSet s = LauncherAnimUtils.createAnimatorSet();
			a = s;
			s.playTogether(
					LauncherAnimUtils.ofFloat( child , "scaleX" , getChildrenScale() ) ,
					LauncherAnimUtils.ofFloat( child , "scaleY" , getChildrenScale() ) ,
					LauncherAnimUtils.ofFloat( child , "translationX" , 0f ) ,
					LauncherAnimUtils.ofFloat( child , "translationY" , 0f ) );
			s.setDuration( REORDER_ANIMATION_DURATION );
			s.setInterpolator( new android.view.animation.DecelerateInterpolator( 1.5f ) );
			s.start();
		}
	}
	
	private void completeAndClearReorderHintAnimations()
	{
		for( ReorderHintAnimation a : mShakeAnimators.values() )
		{
			a.completeAnimationImmediately();
		}
		mShakeAnimators.clear();
	}
	
	private void commitTempPlacement()
	{
		for( int i = 0 ; i < mCountX ; i++ )
		{
			for( int j = 0 ; j < mCountY ; j++ )
			{
				mOccupied[i][j] = mTmpOccupied[i][j];
			}
		}
		int childCount = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			View child = mShortcutsAndWidgets.getChildAt( i );
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			ItemInfo info = (ItemInfo)child.getTag();
			// We do a null check here because the item info can be null in the case of the
			// AllApps button in the hotseat.
			if( info != null )
			{
				if( info.getCellX() != lp.tmpCellX || info.getCellY() != lp.tmpCellY || info.getSpanX() != lp.cellHSpan || info.getSpanY() != lp.cellVSpan )
				{
					info.setRequiresDbUpdate( true );
				}
				lp.cellX = lp.tmpCellX;
				lp.cellY = lp.tmpCellY;
				info.setSpanX( lp.cellHSpan );
				info.setSpanY( lp.cellVSpan );
				info.setCellX( lp.cellX );
				info.setCellY( lp.cellY );
			}
		}
		mLauncher.getWorkspace().updateItemLocationsInDatabase( this );
	}
	
	public void setUseTempCoords(
			boolean useTempCoords )
	{
		int childCount = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < childCount ; i++ )
		{
			LayoutParams lp = (LayoutParams)mShortcutsAndWidgets.getChildAt( i ).getLayoutParams();
			lp.useTmpCoords = useTempCoords;
		}
	}
	
	ItemConfiguration findConfigurationNoShuffle(
			int pixelX ,
			int pixelY ,
			int minSpanX ,
			int minSpanY ,
			int spanX ,
			int spanY ,
			View dragView ,
			ItemConfiguration solution )
	{
		int[] result = new int[2];
		int[] resultSpan = new int[2];
		findNearestVacantArea( pixelX , pixelY , minSpanX , minSpanY , spanX , spanY , null , result , resultSpan );
		if( result[0] >= 0 && result[1] >= 0 )
		{
			copyCurrentStateToSolution( solution , false );
			solution.dragViewX = result[0];
			solution.dragViewY = result[1];
			solution.dragViewSpanX = resultSpan[0];
			solution.dragViewSpanY = resultSpan[1];
			solution.isSolution = true;
		}
		else
		{
			solution.isSolution = false;
		}
		return solution;
	}
	
	public void prepareChildForDrag(
			View child )
	{
		markCellsAsUnoccupiedForView( child );
	}
	
	/* This seems like it should be obvious and straight-forward, but when the direction vector
	needs to match with the notion of the dragView pushing other views, we have to employ
	a slightly more subtle notion of the direction vector. The question is what two points is
	the vector between? The center of the dragView and its desired destination? Not quite, as
	this doesn't necessarily coincide with the interaction of the dragView and items occupying
	those cells. Instead we use some heuristics to often lock the vector to up, down, left
	or right, which helps make pushing feel right.
	*/
	private void getDirectionVectorForDrop(
			int dragViewCenterX ,
			int dragViewCenterY ,
			int spanX ,
			int spanY ,
			View dragView ,
			int[] resultDirection )
	{
		int[] targetDestination = new int[2];
		findNearestArea( dragViewCenterX , dragViewCenterY , spanX , spanY , targetDestination );
		Rect dragRect = new Rect();
		regionToRect( targetDestination[0] , targetDestination[1] , spanX , spanY , dragRect );
		dragRect.offset( dragViewCenterX - dragRect.centerX() , dragViewCenterY - dragRect.centerY() );
		Rect dropRegionRect = new Rect();
		getViewsIntersectingRegion( targetDestination[0] , targetDestination[1] , spanX , spanY , dragView , dropRegionRect , mIntersectingViews );
		int dropRegionSpanX = dropRegionRect.width();
		int dropRegionSpanY = dropRegionRect.height();
		regionToRect( dropRegionRect.left , dropRegionRect.top , dropRegionRect.width() , dropRegionRect.height() , dropRegionRect );
		int deltaX = ( dropRegionRect.centerX() - dragViewCenterX ) / spanX;
		int deltaY = ( dropRegionRect.centerY() - dragViewCenterY ) / spanY;
		if( dropRegionSpanX == mCountX || spanX == mCountX )
		{
			deltaX = 0;
		}
		if( dropRegionSpanY == mCountY || spanY == mCountY )
		{
			deltaY = 0;
		}
		if( deltaX == 0 && deltaY == 0 )
		{
			// No idea what to do, give a random direction.
			resultDirection[0] = 1;
			resultDirection[1] = 0;
		}
		else
		{
			computeDirectionVector( deltaX , deltaY , resultDirection );
		}
	}
	
	// For a given cell and span, fetch the set of views intersecting the region.
	private void getViewsIntersectingRegion(
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			View dragView ,
			Rect boundingRect ,
			ArrayList<View> intersectingViews )
	{
		if( boundingRect != null )
		{
			boundingRect.set( cellX , cellY , cellX + spanX , cellY + spanY );
		}
		intersectingViews.clear();
		Rect r0 = new Rect( cellX , cellY , cellX + spanX , cellY + spanY );
		Rect r1 = new Rect();
		final int count = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View child = mShortcutsAndWidgets.getChildAt( i );
			if( child == dragView )
				continue;
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			r1.set( lp.cellX , lp.cellY , lp.cellX + lp.cellHSpan , lp.cellY + lp.cellVSpan );
			if( Rect.intersects( r0 , r1 ) )
			{
				mIntersectingViews.add( child );
				if( boundingRect != null )
				{
					boundingRect.union( r1 );
				}
			}
		}
	}
	
	boolean isNearestDropLocationOccupied(
			int pixelX ,
			int pixelY ,
			int spanX ,
			int spanY ,
			View dragView ,
			int[] result )
	{
		result = findNearestArea( pixelX , pixelY , spanX , spanY , result );
		getViewsIntersectingRegion( result[0] , result[1] , spanX , spanY , dragView , null , mIntersectingViews );
		return !mIntersectingViews.isEmpty();
	}
	
	void revertTempState()
	{
		if( !isItemPlacementDirty() || DESTRUCTIVE_REORDER )
			return;
		final int count = mShortcutsAndWidgets.getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View child = mShortcutsAndWidgets.getChildAt( i );
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			if( lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY )
			{
				lp.tmpCellX = lp.cellX;
				lp.tmpCellY = lp.cellY;
				animateChildToPosition( child , lp.cellX , lp.cellY , REORDER_ANIMATION_DURATION , 0 , false , false );
			}
		}
		completeAndClearReorderHintAnimations();
		setItemPlacementDirty( false );
	}
	
	boolean createAreaForResize(
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			View dragView ,
			int[] direction ,
			boolean commit )
	{
		int[] pixelXY = new int[2];
		regionToCenterPoint( cellX , cellY , spanX , spanY , pixelXY );
		// First we determine if things have moved enough to cause a different layout
		ItemConfiguration swapSolution = simpleSwap( pixelXY[0] , pixelXY[1] , spanX , spanY , spanX , spanY , direction , dragView , true , new ItemConfiguration() );
		setUseTempCoords( true );
		if( swapSolution != null && swapSolution.isSolution )
		{
			// If we're just testing for a possible location (MODE_ACCEPT_DROP), we don't bother
			// committing anything or animating anything as we just want to determine if a solution
			// exists
			copySolutionToTempState( swapSolution , dragView );
			setItemPlacementDirty( true );
			animateItemsToSolution( swapSolution , dragView , commit );
			if( commit )
			{
				commitTempPlacement();
				completeAndClearReorderHintAnimations();
				setItemPlacementDirty( false );
			}
			else
			{
				beginOrAdjustHintAnimations( swapSolution , dragView , REORDER_ANIMATION_DURATION );
			}
			mShortcutsAndWidgets.requestLayout();
		}
		return swapSolution.isSolution;
	}
	
	int[] createArea(
			int pixelX ,
			int pixelY ,
			int minSpanX ,
			int minSpanY ,
			int spanX ,
			int spanY ,
			View dragView ,
			int[] result ,
			int resultSpan[] ,
			int mode )
	{
		// First we determine if things have moved enough to cause a different layout
		result = findNearestArea( pixelX , pixelY , spanX , spanY , result );
		if( resultSpan == null )
		{
			resultSpan = new int[2];
		}
		// When we are checking drop validity or actually dropping, we don't recompute the
		// direction vector, since we want the solution to match the preview, and it's possible
		// that the exact position of the item has changed to result in a new reordering outcome.
		if( ( mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL || mode == MODE_ACCEPT_DROP ) && mPreviousReorderDirection[0] != INVALID_DIRECTION )
		{
			mDirectionVector[0] = mPreviousReorderDirection[0];
			mDirectionVector[1] = mPreviousReorderDirection[1];
			// We reset this vector after drop
			if( mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL )
			{
				mPreviousReorderDirection[0] = INVALID_DIRECTION;
				mPreviousReorderDirection[1] = INVALID_DIRECTION;
			}
		}
		else
		{
			getDirectionVectorForDrop( pixelX , pixelY , spanX , spanY , dragView , mDirectionVector );
			mPreviousReorderDirection[0] = mDirectionVector[0];
			mPreviousReorderDirection[1] = mDirectionVector[1];
		}
		ItemConfiguration swapSolution = simpleSwap( pixelX , pixelY , minSpanX , minSpanY , spanX , spanY , mDirectionVector , dragView , true , new ItemConfiguration() );
		// We attempt the approach which doesn't shuffle views at all
		ItemConfiguration noShuffleSolution = findConfigurationNoShuffle( pixelX , pixelY , minSpanX , minSpanY , spanX , spanY , dragView , new ItemConfiguration() );
		ItemConfiguration finalSolution = null;
		if( swapSolution.isSolution && swapSolution.area() >= noShuffleSolution.area() )
		{
			finalSolution = swapSolution;
		}
		else if( noShuffleSolution.isSolution )
		{
			finalSolution = noShuffleSolution;
		}
		boolean foundSolution = true;
		if( !DESTRUCTIVE_REORDER )
		{
			setUseTempCoords( true );
		}
		if( finalSolution != null )
		{
			result[0] = finalSolution.dragViewX;
			result[1] = finalSolution.dragViewY;
			resultSpan[0] = finalSolution.dragViewSpanX;
			resultSpan[1] = finalSolution.dragViewSpanY;
			// If we're just testing for a possible location (MODE_ACCEPT_DROP), we don't bother
			// committing anything or animating anything as we just want to determine if a solution
			// exists
			if( mode == MODE_DRAG_OVER || mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL )
			{
				if( !DESTRUCTIVE_REORDER )
				{
					copySolutionToTempState( finalSolution , dragView );
				}
				setItemPlacementDirty( true );
				animateItemsToSolution( finalSolution , dragView , mode == MODE_ON_DROP );
				if( !DESTRUCTIVE_REORDER && ( mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL ) )
				{
					commitTempPlacement();
					completeAndClearReorderHintAnimations();
					setItemPlacementDirty( false );
				}
				else
				{
					beginOrAdjustHintAnimations( finalSolution , dragView , REORDER_ANIMATION_DURATION );
				}
			}
		}
		else
		{
			foundSolution = false;
			result[0] = result[1] = resultSpan[0] = resultSpan[1] = -1;
		}
		if( ( mode == MODE_ON_DROP || !foundSolution ) && !DESTRUCTIVE_REORDER )
		{
			setUseTempCoords( false );
		}
		mShortcutsAndWidgets.requestLayout();
		return result;
	}
	
	void setItemPlacementDirty(
			boolean dirty )
	{
		mItemPlacementDirty = dirty;
	}
	
	boolean isItemPlacementDirty()
	{
		return mItemPlacementDirty;
	}
	
	private class ItemConfiguration
	{
		
		HashMap<View , CellAndSpan> map = new HashMap<View , CellAndSpan>();
		private HashMap<View , CellAndSpan> savedMap = new HashMap<View , CellAndSpan>();
		ArrayList<View> sortedViews = new ArrayList<View>();
		boolean isSolution = false;
		int dragViewX , dragViewY , dragViewSpanX , dragViewSpanY;
		
		void save()
		{
			// Copy current state into savedMap
			for( View v : map.keySet() )
			{
				map.get( v ).copy( savedMap.get( v ) );
			}
		}
		
		void restore()
		{
			// Restore current state from savedMap
			for( View v : savedMap.keySet() )
			{
				savedMap.get( v ).copy( map.get( v ) );
			}
		}
		
		void add(
				View v ,
				CellAndSpan cs )
		{
			map.put( v , cs );
			savedMap.put( v , new CellAndSpan() );
			sortedViews.add( v );
		}
		
		int area()
		{
			return dragViewSpanX * dragViewSpanY;
		}
	}
	
	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 *
	 * @param pixelX The X location at which you want to search for a vacant area.
	 * @param pixelY The Y location at which you want to search for a vacant area.
	 * @param spanX Horizontal span of the object.
	 * @param spanY Vertical span of the object.
	 * @param ignoreView Considers space occupied by this view as unoccupied
	 * @param result Previously returned value to possibly recycle.
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	int[] findNearestVacantArea(
			int pixelX ,
			int pixelY ,
			int spanX ,
			int spanY ,
			View ignoreView ,
			int[] result )
	{
		return findNearestArea( pixelX , pixelY , spanX , spanY , ignoreView , true , result );
	}
	
	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 *
	 * @param pixelX The X location at which you want to search for a vacant area.
	 * @param pixelY The Y location at which you want to search for a vacant area.
	 * @param minSpanX The minimum horizontal span required
	 * @param minSpanY The minimum vertical span required
	 * @param spanX Horizontal span of the object.
	 * @param spanY Vertical span of the object.
	 * @param ignoreView Considers space occupied by this view as unoccupied
	 * @param result Previously returned value to possibly recycle.
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	int[] findNearestVacantArea(
			int pixelX ,
			int pixelY ,
			int minSpanX ,
			int minSpanY ,
			int spanX ,
			int spanY ,
			View ignoreView ,
			int[] result ,
			int[] resultSpan )
	{
		return findNearestArea( pixelX , pixelY , minSpanX , minSpanY , spanX , spanY , ignoreView , true , result , resultSpan , mOccupied );
	}
	
	/**
	 * Find a starting cell position that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 *
	 * @param pixelX The X location at which you want to search for a vacant area.
	 * @param pixelY The Y location at which you want to search for a vacant area.
	 * @param spanX Horizontal span of the object.
	 * @param spanY Vertical span of the object.
	 * @param ignoreView Considers space occupied by this view as unoccupied
	 * @param result Previously returned value to possibly recycle.
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	public int[] findNearestArea(
			int pixelX ,
			int pixelY ,
			int spanX ,
			int spanY ,
			int[] result )
	{
		return findNearestArea( pixelX , pixelY , spanX , spanY , null , false , result );
	}
	
	boolean existsEmptyCell()
	{
		return findCellForSpan( null , 1 , 1 );
	}
	
	/**
	 * Finds the upper-left coordinate of the first rectangle in the grid that can
	 * hold a cell of the specified dimensions. If intersectX and intersectY are not -1,
	 * then this method will only return coordinates for rectangles that contain the cell
	 * (intersectX, intersectY)
	 *
	 * @param cellXY The array that will contain the position of a vacant cell if such a cell
	 *               can be found.
	 * @param spanX The horizontal span of the cell we want to find.
	 * @param spanY The vertical span of the cell we want to find.
	 *
	 * @return True if a vacant cell of the specified dimension was found, false otherwise.
	 */
	public boolean findCellForSpan(
			int[] cellXY ,
			int spanX ,
			int spanY )
	{
		return findCellForSpanThatIntersectsIgnoring( cellXY , spanX , spanY , -1 , -1 , null , mOccupied );
	}
	
	/**
	 * Like above, but ignores any cells occupied by the item "ignoreView"
	 *
	 * @param cellXY The array that will contain the position of a vacant cell if such a cell
	 *               can be found.
	 * @param spanX The horizontal span of the cell we want to find.
	 * @param spanY The vertical span of the cell we want to find.
	 * @param ignoreView The home screen item we should treat as not occupying any space
	 * @return
	 */
	boolean findCellForSpanIgnoring(
			int[] cellXY ,
			int spanX ,
			int spanY ,
			View ignoreView )
	{
		return findCellForSpanThatIntersectsIgnoring( cellXY , spanX , spanY , -1 , -1 , ignoreView , mOccupied );
	}
	
	/**
	 * Like above, but if intersectX and intersectY are not -1, then this method will try to
	 * return coordinates for rectangles that contain the cell [intersectX, intersectY]
	 *
	 * @param spanX The horizontal span of the cell we want to find.
	 * @param spanY The vertical span of the cell we want to find.
	 * @param ignoreView The home screen item we should treat as not occupying any space
	 * @param intersectX The X coordinate of the cell that we should try to overlap
	 * @param intersectX The Y coordinate of the cell that we should try to overlap
	 *
	 * @return True if a vacant cell of the specified dimension was found, false otherwise.
	 */
	boolean findCellForSpanThatIntersects(
			int[] cellXY ,
			int spanX ,
			int spanY ,
			int intersectX ,
			int intersectY )
	{
		return findCellForSpanThatIntersectsIgnoring( cellXY , spanX , spanY , intersectX , intersectY , null , mOccupied );
	}
	
	/**
	 * The superset of the above two methods
	 */
	boolean findCellForSpanThatIntersectsIgnoring(
			int[] cellXY ,
			int spanX ,
			int spanY ,
			int intersectX ,
			int intersectY ,
			View ignoreView ,
			boolean occupied[][] )
	{
		// mark space take by ignoreView as available (method checks if ignoreView is null)
		markCellsAsUnoccupiedForView( ignoreView , occupied );
		boolean foundCell = false;
		while( true )
		{
			int startX = 0;
			if( intersectX >= 0 )
			{
				startX = Math.max( startX , intersectX - ( spanX - 1 ) );
			}
			int endX = mCountX - ( spanX - 1 );
			if( intersectX >= 0 )
			{
				endX = Math.min( endX , intersectX + ( spanX - 1 ) + ( spanX == 1 ? 1 : 0 ) );
			}
			int startY = 0;
			if( intersectY >= 0 )
			{
				startY = Math.max( startY , intersectY - ( spanY - 1 ) );
			}
			int endY = mCountY - ( spanY - 1 );
			if( intersectY >= 0 )
			{
				endY = Math.min( endY , intersectY + ( spanY - 1 ) + ( spanY == 1 ? 1 : 0 ) );
			}
			for( int y = startY ; y < endY && !foundCell ; y++ )
			{
				inner:
				for( int x = startX ; x < endX ; x++ )
				{
					for( int i = 0 ; i < spanX ; i++ )
					{
						for( int j = 0 ; j < spanY ; j++ )
						{
							if( occupied[x + i][y + j] )
							{
								// small optimization: we can skip to after the column we just found
								// an occupied cell
								x += i;
								continue inner;
							}
						}
					}
					if( cellXY != null )
					{
						cellXY[0] = x;
						cellXY[1] = y;
					}
					foundCell = true;
					break;
				}
			}
			if( intersectX == -1 && intersectY == -1 )
			{
				break;
			}
			else
			{
				// if we failed to find anything, try again but without any requirements of
				// intersecting
				intersectX = -1;
				intersectY = -1;
				continue;
			}
		}
		// re-mark space taken by ignoreView as occupied
		markCellsAsOccupiedForView( ignoreView , occupied );
		return foundCell;
	}
	
	/**
	 * A drag event has begun over this layout.
	 * It may have begun over this layout (in which case onDragChild is called first),
	 * or it may have begun on another layout.
	 */
	void onDragEnter()
	{
		mDragEnforcer.onDragEnter();
		mDragging = true;
	}
	
	/**
	 * Called when drag has left this CellLayout or has been completed (successfully or not)
	 */
	void onDragExit()
	{
		mDragEnforcer.onDragExit();
		// This can actually be called when we aren't in a drag, e.g. when adding a new
		// item to this layout via the customize drawer.
		// Guard against that case.
		if( mDragging )
		{
			mDragging = false;
		}
		// Invalidate the drag data
		mDragCell[0] = mDragCell[1] = -1;
		mDragOutlineAnims[mDragOutlineCurrent].animateOut();
		mDragOutlineCurrent = ( mDragOutlineCurrent + 1 ) % mDragOutlineAnims.length;
		revertTempState();
		setIsDragOverlapping( false );
	}
	
	/**
	 * Mark a child as having been dropped.
	 * At the beginning of the drag operation, the child may have been on another
	 * screen, but it is re-parented before this method is called.
	 *
	 * @param child The child that is being dropped
	 */
	void onDropChild(
			View child )
	{
		if( child != null )
		{
			LayoutParams lp = (LayoutParams)child.getLayoutParams();
			lp.dropped = true;
			child.requestLayout();
		}
	}
	
	/**
	 * Computes a bounding rectangle for a range of cells
	 *
	 * @param cellX X coordinate of upper left corner expressed as a cell position
	 * @param cellY Y coordinate of upper left corner expressed as a cell position
	 * @param cellHSpan Width in cells
	 * @param cellVSpan Height in cells
	 * @param resultRect Rect into which to put the results
	 */
	public void cellToRect(
			int cellX ,
			int cellY ,
			int cellHSpan ,
			int cellVSpan ,
			Rect resultRect )
	{
		final int cellWidth = mCellWidth;
		final int cellHeight = mCellHeight;
		final int widthGap = mWidthGap;
		final int heightGap = mHeightGap;
		final int hStartPadding = getPaddingLeft();
		final int vStartPadding = getPaddingTop();
		int width = cellHSpan * cellWidth + ( ( cellHSpan - 1 ) * widthGap );
		int height = cellVSpan * cellHeight + ( ( cellVSpan - 1 ) * heightGap );
		int x = hStartPadding + cellX * ( cellWidth + widthGap );
		int y = vStartPadding + cellY * ( cellHeight + heightGap );
		resultRect.set( x , y , x + width , y + height );
	}
	
	//cheyingkun del start	//修正widget行列数的计算方式【i_0014998】
	//	/**
	//	 * Computes the required horizontal and vertical cell spans to always
	//	 * fit the given rectangle.
	//	 *
	//	 * @param width Width in pixels
	//	 * @param height Height in pixels
	//	 * @param result An array of length 2 in which to store the result (may be null).
	//	 */
	//	public static int[] rectToCell(
	//			int width ,
	//			int height ,
	//			int[] result )
	//	{
	//		LauncherAppState app = LauncherAppState.getInstance();
	//		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
	//		Rect padding = grid.getWorkspacePadding( CellLayout.PORTRAIT );
	//		// Always assume we're working with the smallest span to make sure we
	//		// reserve enough space in both orientations.
	//		int parentWidth = grid.calculateCellWidth( grid.getWidthPx() - padding.left - padding.right , (int)grid.getNumColumns() );
	//		int parentHeight = grid.calculateCellHeight( grid.getHeightPx() - padding.top - padding.bottom , (int)grid.getNumRows() );
	//		int smallerSize = Math.min( parentWidth , parentHeight );
	//		// Always round up to next largest cell
	//		int spanX = (int)Math.ceil( width / (float)smallerSize );
	//		int spanY = (int)Math.ceil( height / (float)smallerSize );
	//		if( result == null )
	//		{
	//			return new int[]{ spanX , spanY };
	//		}
	//		result[0] = spanX;
	//		result[1] = spanY;
	//		return result;
	//	}
	//cheyingkun del end
	public int[] cellSpansToSize(
			int hSpans ,
			int vSpans )
	{
		int[] size = new int[2];
		size[0] = hSpans * mCellWidth + ( hSpans - 1 ) * mWidthGap;
		size[1] = vSpans * mCellHeight + ( vSpans - 1 ) * mHeightGap;
		return size;
	}
	
	/**
	 * Calculate the grid spans needed to fit given item
	 */
	public void calculateSpans(
			ItemInfo info )
	{
		final int minWidth;
		final int minHeight;
		if( info instanceof LauncherAppWidgetInfo )
		{
			minWidth = ( (LauncherAppWidgetInfo)info ).getMinWidth();
			minHeight = ( (LauncherAppWidgetInfo)info ).getMinHeight();
		}
		else if( info instanceof PendingAddWidgetInfo )
		{
			minWidth = ( (PendingAddWidgetInfo)info ).getMinWidth();
			minHeight = ( (PendingAddWidgetInfo)info ).getMinHeight();
		}
		else
		{
			// It's not a widget, so it must be 1x1
			info.setSpanX( 1 );
			info.setSpanY( 1 );
			return;
		}
		//cheyingkun start	//修正widget行列数的计算方式【i_0014998】
		//		int[] spans = rectToCell( minWidth , minHeight , null );//cheyingkun del
		int[] spans = rectToCellWidget( minWidth , minHeight , null );//cheyingkun add
		//cheyingkun end
		info.setSpanX( spans[0] );
		info.setSpanY( spans[1] );
	}
	
	/**
	 * Find the first vacant cell, if there is one.
	 *
	 * @param vacant Holds the x and y coordinate of the vacant cell
	 * @param spanX Horizontal cell span.
	 * @param spanY Vertical cell span.
	 *
	 * @return True if a vacant cell was found
	 */
	public boolean getVacantCell(
			int[] vacant ,
			int spanX ,
			int spanY )
	{
		return findVacantCell( vacant , spanX , spanY , mCountX , mCountY , mOccupied );
	}
	
	static boolean findVacantCell(
			int[] vacant ,
			int spanX ,
			int spanY ,
			int xCount ,
			int yCount ,
			boolean[][] occupied )
	{
		for( int y = 0 ; y < yCount ; y++ )
		{
			for( int x = 0 ; x < xCount ; x++ )
			{
				boolean available = !occupied[x][y];
				out:
				for( int i = x ; i < x + spanX - 1 && x < xCount ; i++ )
				{
					for( int j = y ; j < y + spanY - 1 && y < yCount ; j++ )
					{
						available = available && !occupied[i][j];
						if( !available )
							break out;
					}
				}
				if( available )
				{
					vacant[0] = x;
					vacant[1] = y;
					return true;
				}
			}
		}
		return false;
	}
	
	private void clearOccupiedCells()
	{
		for( int x = 0 ; x < mCountX ; x++ )
		{
			for( int y = 0 ; y < mCountY ; y++ )
			{
				mOccupied[x][y] = false;
			}
		}
	}
	
	public void onMove(
			View view ,
			int newCellX ,
			int newCellY ,
			int newSpanX ,
			int newSpanY )
	{
		markCellsAsUnoccupiedForView( view );
		markCellsForView( newCellX , newCellY , newSpanX , newSpanY , mOccupied , true );
	}
	
	public void markCellsAsOccupiedForView(
			View view )
	{
		markCellsAsOccupiedForView( view , mOccupied );
	}
	
	public void markCellsAsOccupiedForView(
			View view ,
			boolean[][] occupied )
	{
		if( view == null || view.getParent() != mShortcutsAndWidgets )
			return;
		LayoutParams lp = (LayoutParams)view.getLayoutParams();
		markCellsForView( lp.cellX , lp.cellY , lp.cellHSpan , lp.cellVSpan , occupied , true );
	}
	
	public void markCellsAsUnoccupiedForView(
			View view )
	{
		markCellsAsUnoccupiedForView( view , mOccupied );
	}
	
	public void markCellsAsUnoccupiedForView(
			View view ,
			boolean occupied[][] )
	{
		if( view == null || view.getParent() != mShortcutsAndWidgets )
			return;
		LayoutParams lp = (LayoutParams)view.getLayoutParams();
		markCellsForView( lp.cellX , lp.cellY , lp.cellHSpan , lp.cellVSpan , occupied , false );
	}
	
	private void markCellsForView(
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			boolean[][] occupied ,
			boolean value )
	{
		if( cellX < 0 || cellY < 0 )
			return;
		for( int x = cellX ; x < cellX + spanX && x < mCountX ; x++ )
		{
			for( int y = cellY ; y < cellY + spanY && y < mCountY ; y++ )
			{
				occupied[x][y] = value;
			}
		}
	}
	
	public void setContentGap(
			int gapx ,
			int gapy )
	{
		mWidthGap = mOriginalWidthGap = gapx;
		mHeightGap = mOriginalHeightGap = gapy;
	}
	
	public ViewGroup getChildrenLayout()
	{
		return getShortcutsAndWidgets();
	}
	
	public int getDesiredWidth()
	{
		//xiatian start	//fix bug：解决“在智能分类不可以显示“更多应用”图标的前提下进行智能分类，分类完成后将智能分类的文件夹中最后一个图标移出文件夹形成空文件夹，点击打开空文件夹时桌面重启”的问题。
		//		return getPaddingLeft() + getPaddingRight() + ( mCountX * mCellWidth ) + ( Math.max( ( mCountX - 1 ) , 0 ) * mWidthGap );//xiatian del
		return getPaddingLeft() + getPaddingRight() + ( ( mCountX == 0 ? 1 : mCountX ) * mCellWidth ) + ( Math.max( ( ( mCountX == 0 ? 1 : mCountX ) - 1 ) , 0 ) * mWidthGap );//xiatian add
		//xiatian end
	}
	
	public int getDesiredHeight()
	{
		//xiatian start	//fix bug：解决“在智能分类不可以显示“更多应用”图标的前提下进行智能分类，分类完成后将智能分类的文件夹中最后一个图标移出文件夹形成空文件夹，点击打开空文件夹时桌面重启”的问题。
		//		return getPaddingTop() + getPaddingBottom() + ( mCountY * mCellHeight ) + ( Math.max( ( mCountY - 1 ) , 0 ) * mHeightGap );//xiatian del
		return getPaddingTop() + getPaddingBottom() + ( ( mCountY == 0 ? 1 : mCountY ) * mCellHeight ) + ( Math.max( ( ( mCountY == 0 ? 1 : mCountY ) - 1 ) , 0 ) * mHeightGap );//xiatian add
		//xiatian end
	}
	
	// zhujieping@2015/06/19 ADD START
	public int getDesiredHeight(
			int cols )
	{
		return getPaddingTop() + getPaddingBottom() + ( ( cols == 0 ? 1 : cols ) * mCellHeight ) + ( Math.max( ( ( cols == 0 ? 1 : cols ) - 1 ) , 0 ) * mHeightGap );
	}
	
	public int getDesiredWidth(
			int row )
	{
		return getPaddingLeft() + getPaddingRight() + ( ( row == 0 ? 1 : row ) * mCellWidth ) + ( Math.max( ( ( row == 0 ? 1 : row ) - 1 ) , 0 ) * mWidthGap );
	}
	
	// zhujieping@2015/06/19 ADD END
	public boolean isOccupied(
			int x ,
			int y )
	{
		if( x < mCountX && y < mCountY )
		{
			return mOccupied[x][y];
		}
		else
		{
			throw new RuntimeException( "Position exceeds the bound of this CellLayout" );
		}
	}
	
	@Override
	public ViewGroup.LayoutParams generateLayoutParams(
			AttributeSet attrs )
	{
		return new CellLayout.LayoutParams( getContext() , attrs );
	}
	
	@Override
	protected boolean checkLayoutParams(
			ViewGroup.LayoutParams p )
	{
		return p instanceof CellLayout.LayoutParams;
	}
	
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p )
	{
		return new CellLayout.LayoutParams( p );
	}
	
	public static class CellLayoutAnimationController extends LayoutAnimationController
	{
		
		public CellLayoutAnimationController(
				Animation animation ,
				float delay )
		{
			super( animation , delay );
		}
		
		@Override
		protected long getDelayForView(
				View view )
		{
			return (int)( Math.random() * 150 );
		}
	}
	
	public static class LayoutParams extends ViewGroup.MarginLayoutParams
	{
		
		/**
		 * Horizontal location of the item in the grid.
		 */
		@ViewDebug.ExportedProperty
		public int cellX;
		/**
		 * Vertical location of the item in the grid.
		 */
		@ViewDebug.ExportedProperty
		public int cellY;
		/**
		 * Temporary horizontal location of the item in the grid during reorder
		 */
		public int tmpCellX;
		/**
		 * Temporary vertical location of the item in the grid during reorder
		 */
		public int tmpCellY;
		/**
		 * Indicates that the temporary coordinates should be used to layout the items
		 */
		public boolean useTmpCoords;
		/**
		 * Number of cells spanned horizontally by the item.
		 */
		@ViewDebug.ExportedProperty
		public int cellHSpan;
		/**
		 * Number of cells spanned vertically by the item.
		 */
		@ViewDebug.ExportedProperty
		public int cellVSpan;
		/**
		 * Indicates whether the item will set its x, y, width and height parameters freely,
		 * or whether these will be computed based on cellX, cellY, cellHSpan and cellVSpan.
		 */
		public boolean isLockedToGrid = true;
		/**
		 * Indicates that this item should use the full extents of its parent.
		 */
		public boolean isFullscreen = false;
		/**
		 * Indicates whether this item can be reordered. Always true except in the case of the
		 * the AllApps button.
		 */
		public boolean canReorder = true;
		// X coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		int x;
		// Y coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		int y;
		boolean dropped;
		
		public LayoutParams(
				Context c ,
				AttributeSet attrs )
		{
			super( c , attrs );
			cellHSpan = 1;
			cellVSpan = 1;
		}
		
		public LayoutParams(
				ViewGroup.LayoutParams source )
		{
			super( source );
			cellHSpan = 1;
			cellVSpan = 1;
		}
		
		public LayoutParams(
				LayoutParams source )
		{
			super( source );
			this.cellX = source.cellX;
			this.cellY = source.cellY;
			this.cellHSpan = source.cellHSpan;
			this.cellVSpan = source.cellVSpan;
		}
		
		public LayoutParams(
				int cellX ,
				int cellY ,
				int cellHSpan ,
				int cellVSpan )
		{
			super( LayoutParams.MATCH_PARENT , LayoutParams.MATCH_PARENT );
			this.cellX = cellX;
			this.cellY = cellY;
			this.cellHSpan = cellHSpan;
			this.cellVSpan = cellVSpan;
		}
		
		public void setup(
				int cellWidth ,
				int cellHeight ,
				int widthGap ,
				int heightGap ,
				boolean invertHorizontally ,
				int colCount )
		{
			if( isLockedToGrid )
			{
				final int myCellHSpan = cellHSpan;
				final int myCellVSpan = cellVSpan;
				int myCellX = useTmpCoords ? tmpCellX : cellX;
				int myCellY = useTmpCoords ? tmpCellY : cellY;
				if( invertHorizontally )
				{
					myCellX = colCount - myCellX - cellHSpan;
				}
				width = myCellHSpan * cellWidth + ( ( myCellHSpan - 1 ) * widthGap ) - leftMargin - rightMargin;
				height = myCellVSpan * cellHeight + ( ( myCellVSpan - 1 ) * heightGap ) - topMargin - bottomMargin;
				x = (int)( myCellX * ( cellWidth + widthGap ) + leftMargin );
				y = (int)( myCellY * ( cellHeight + heightGap ) + topMargin );
			}
		}
		
		public String toString()
		{
			return StringUtils.concat( "(" , this.cellX , ", " , this.cellY , ")" );
		}
		
		public void setWidth(
				int width )
		{
			this.width = width;
		}
		
		public int getWidth()
		{
			return width;
		}
		
		public void setHeight(
				int height )
		{
			this.height = height;
		}
		
		public int getHeight()
		{
			return height;
		}
		
		public void setX(
				int x )
		{
			this.x = x;
		}
		
		public int getX()
		{
			return x;
		}
		
		public void setY(
				int y )
		{
			this.y = y;
		}
		
		public int getY()
		{
			return y;
		}
	}
	
	// This class stores info for two purposes:
	// 1. When dragging items (mDragInfo in Workspace), we store the View, its cellX & cellY,
	//    its spanX, spanY, and the screen it is on
	// 2. When long clicking on an empty cell in a CellLayout, we save information about the
	//    cellX and cellY coordinates and which page was clicked. We then set this as a tag on
	//    the CellLayout that was long clicked
	public boolean lastDownOnOccupiedCell()
	{
		return mLastDownOnOccupiedCell;
	}
	
	@Override
	public View getView()
	{
		return this;
	}
	
	@Override
	public int getChildrenCellX(
			int index )
	{
		return ( (CellLayout.LayoutParams)( getChildrenLayout().getChildAt( index ).getLayoutParams() ) ).cellX;
	}
	
	@Override
	public int getChildrenCellY(
			int index )
	{
		return ( (CellLayout.LayoutParams)( getChildrenLayout().getChildAt( index ).getLayoutParams() ) ).cellY;
	}
	
	@Override
	public View getChildOnPageAt(
			int i )
	{
		return getShortcutsAndWidgets().getChildAt( i );
	}
	
	@Override
	public int getCellWidthGap()
	{
		return getWidthGap();
	}
	
	@Override
	public int getCellHeightGap()
	{
		return getHeightGap();
	}
	
	@Override
	public int getPageChildCount()
	{
		return getShortcutsAndWidgets().getChildCount();
	}
	
	// zhujieping@2015/05/26 ADD START
	public void setIsFunctionPage(
			boolean mIsFunctionPage )
	{
		this.mIsFunctionPage = mIsFunctionPage;
		//cheyingkun add start	//phenix1.1稳定版移植酷生活
		if( mIsFunctionPage )
		{
			this.mShortcutsAndWidgets.setVisibility( View.GONE );
		}
		//cheyingkun add end
	}
	
	public State getState()
	{
		return mLauncher.getWorkspace().getState();
	}
	
	// zhujieping@2015/05/26 ADD END
	//cheyingkun add start	//解决：拖拽文件夹时不松手,pc端卸载文件夹内图标,拖动的图标和影子没更新的问题（bug:0009717）
	/**桌面图标刷新后强制刷新拖拽view的影子
	 * visualizeDropLocation方法会匹配是否改变了格子,改变了才刷新底部影子图标;
	 * visualizeDropLocationNoMatterOldDragCell该方法是拖拽文件夹过程中,文件夹内图标发生变化(1.被卸载2.变灰或者变亮),所以不做格子改变的比较,强制画一次
	 */
	void visualizeDropLocationNoMatterOldDragCell(
			View v ,
			Bitmap dragOutline ,
			int originX ,
			int originY ,
			int cellX ,
			int cellY ,
			int spanX ,
			int spanY ,
			boolean resize ,
			Point dragOffset ,
			Rect dragRegion )
	{
		if( dragOutline == null )
		{
			return;
		}
		//cheyingkun start	//拖动文件夹到插件上不松手,卸载文件夹内的应用,dragview的投影位置错误.(bug:0010156)
		//把mDragCell中的数据复制给cellX和cellY,mDragCell中保存的是上次的位置
		//cheyingkun del start
		//		mDragCell[0] = cellX;
		//		mDragCell[1] = cellY;
		//cheyingkun del end
		//cheyingkun add start
		cellX = mDragCell[0];
		cellY = mDragCell[1];
		//cheyingkun add end
		//cheyingkun end
		// Find the top left corner of the rect the object will occupy
		final int[] topLeft = mTmpPoint;
		cellToPoint( cellX , cellY , topLeft );
		int left = topLeft[0];
		int top = topLeft[1];
		if( v != null && dragOffset == null )
		{
			// When drawing the drag outline, it did not account for margin offsets
			// added by the view's parent.
			MarginLayoutParams lp = (MarginLayoutParams)v.getLayoutParams();
			left += lp.leftMargin;
			top += lp.topMargin;
			// Offsets due to the size difference between the View and the dragOutline.
			// There is a size difference to account for the outer blur, which may lie
			// outside the bounds of the view.
			top += ( v.getHeight() - dragOutline.getHeight() ) / 2;
			// We center about the x axis
			left += ( ( mCellWidth * spanX ) + ( ( spanX - 1 ) * mWidthGap ) - dragOutline.getWidth() ) / 2;
		}
		else
		{
			if( dragOffset != null && dragRegion != null )
			{
				// Center the drag region *horizontally* in the cell and apply a drag
				// outline offset
				left += dragOffset.x + ( ( mCellWidth * spanX ) + ( ( spanX - 1 ) * mWidthGap ) - dragRegion.width() ) / 2;
				int cHeight = getShortcutsAndWidgets().getCellContentHeight();
				int cellPaddingY = (int)Math.max( 0 , ( ( mCellHeight - cHeight ) / 2f ) );
				top += dragOffset.y + cellPaddingY;
			}
			else
			{
				// Center the drag outline in the cell
				left += ( ( mCellWidth * spanX ) + ( ( spanX - 1 ) * mWidthGap ) - dragOutline.getWidth() ) / 2;
				top += ( ( mCellHeight * spanY ) + ( ( spanY - 1 ) * mHeightGap ) - dragOutline.getHeight() ) / 2;
			}
		}
		final int oldIndex = mDragOutlineCurrent;
		mDragOutlineAnims[oldIndex].animateOut();
		mDragOutlineCurrent = ( oldIndex + 1 ) % mDragOutlines.length;
		Rect r = mDragOutlines[mDragOutlineCurrent];
		r.set( left , top , left + dragOutline.getWidth() , top + dragOutline.getHeight() );
		if( resize )
		{
			cellToRect( cellX , cellY , spanX , spanY , r );
		}
		mDragOutlineAnims[mDragOutlineCurrent].setTag( dragOutline );
		mDragOutlineAnims[mDragOutlineCurrent].animateIn();
	}
	//cheyingkun add end
	;
	
	private int getCellPaddingY()
	{
		int mCellPaddingY = 0;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			int cHeight = getShortcutsAndWidgets().getCellContentHeight();
			mCellPaddingY = (int)Math.max( 0 , ( ( mCellHeight - cHeight ) / 2f ) );
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			mCellPaddingY = 0;
		}
		//xiatian add end
		return mCellPaddingY;
	}
	
	//cheyingkun add start	//卸载应用后，其后面的图标是否自动前移。true为自动前移；false为不移动。默认为false。
	/**
	 * 找空位算法
	 * 找到当前cellLayout在删除item后面的空位
	 * @param spanX	行数
	 * @param spanY 列数
	 * @param removeCellY 删除item的位置
	 * @param removeCellX 删除item的位置
	 * @return 返回第一个空位的坐标
	 */
	public int[] findNearestAreaAfterRemoveItem(
			int removeCellX ,
			int removeCellY ,
			int spanX ,
			int spanY )
	{
		if( mOccupied == null || spanX < 1 || spanY < 1 || removeCellX < 0 || removeCellY < 0 )
		{
			return null;
		}
		int countX = getCountX();
		int countY = getCountY();
		int[] bestXY = new int[]{ -1 , -1 };
		boolean[][] occupied = mOccupied;
		for( int y = removeCellY ; y < countY - ( spanY - 1 ) ; y++ )
		{
			inner:
			for( int x = ( y == removeCellY ? removeCellX : 0 ) ; x < countX - ( spanX - 1 ) ; x++ )
			{
				for( int i = 0 ; i < spanX ; i++ )
				{
					for( int j = 0 ; j < spanY ; j++ )
					{
						if( occupied[x + i][y + j] )
						{
							continue inner;
						}
					}
				}
				bestXY[0] = x;
				bestXY[1] = y;
				return bestXY;
			}
		}
		return bestXY;
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
	public boolean isDefaultPage()
	{
		return mIsDefaultPage;
	}
	
	public void setDefaultPage(
			boolean mIsDefaultPage )
	{
		this.mIsDefaultPage = mIsDefaultPage;
	}
	
	//xiatian add end
	// gaominghui@2017/01/04 ADD START 兼容android 4.0,必须要重写该方法，否则报空指针； 0014804：切换桌面模式打开双层模式，返回桌面点击主菜单，无法进入双层模式，并且桌面会重启；
	/**
	 *
	 * @see android.view.View#getCameraDistance()
	 * @auther gaominghui  2017年1月4日
	 */
	@Override
	public float getCameraDistance()
	{
		float cameraDistance = 0;
		if( Build.VERSION.SDK_INT > 15 )
		{
			cameraDistance = super.getCameraDistance();
		}
		return cameraDistance;
	}
	// gaominghui@2017/01/04 ADD END 兼容android 4.0,必须要重写该方法，否则报空指针； 0014804：切换桌面模式打开双层模式，返回桌面点击主菜单，无法进入双层模式，并且桌面会重启；
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
		//lvjiangbin del 20151202  begin
		//		if( isFavoritesPage() )
		//		{
		//			FavoritesManager.getInstance().onThemeChanged( arg0 , arg1 );
		//			return;
		//		}
		//lvjiangbin del 20151202  end
		int mCount = getChildCount();
		for( int i = 0 ; i < mCount ; i++ )
		{
			View mView = getChildAt( i );
			if( mView instanceof RelativeLayout )
			{//mBackGroundViewGroup
			}
			else if( mView instanceof ShortcutAndWidgetContainer )
			{
				ShortcutAndWidgetContainer mShortcutAndWidgetContainer = (ShortcutAndWidgetContainer)mView;
				mShortcutAndWidgetContainer.onThemeChanged( arg0 , arg1 );
			}
		}
	}
	//zhujieping add end
	;
	
	//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
	public void addOverViewModeHomeView()
	{
		//gaominghui add start //从配置文件里读取布局宽高
		//FrameLayout.LayoutParams mHomeParams = new FrameLayout.LayoutParams( 185 , 110 );
		FrameLayout.LayoutParams mHomeParams = new FrameLayout.LayoutParams(
				LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_home_view_layout_width ) ,
				LauncherDefaultConfig.getDimensionPixelSize( R.dimen.overview_home_view_layout_height ) );
		//gaominghui add end //从配置文件里读取布局宽高
		mHomeParams.gravity = Gravity.CENTER_HORIZONTAL;
		mOverViewHomeView.setVisibility( View.GONE );
		addView( mOverViewHomeView , mHomeParams );
		hasAddHomeButtonView = true;
	}
	
	public boolean hasAddHomeButtonView = false;
	
	/**
	 *
	 * @param v
	 * @author gaominghui 2017年2月10日
	 */
	public void setOverViewHomePage()
	{
		Workspace wp = (Workspace)this.getParent();
		if( wp.isInOverviewMode() )
		{
			int index = wp.indexOfChild( this );
			int mDefaultPage = index;
			//xiatian start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
			//xiatian del start
			//			if( wp.hasFavoritesPage() )
			//			{
			//				mDefaultPage--;
			//			}
			//xiatian del end
			mDefaultPage -= wp.getFunctionPagesInNormalPageLeftNum();//xiatian add
			//xiatian end
			wp.setDefaultPage( mDefaultPage );
			int childCount = wp.getChildCount();
			if( index < childCount )
			{
				for( int i = 0 ; i < childCount ; i++ )
				{
					CellLayout cl = (CellLayout)wp.getChildAt( i );
					if( index != i )
					{
						cl.getHomeView().setImageDrawable( getResources().getDrawable( R.drawable.editmode_home_unselected ) );
						cl.mNormalBackground = ResourcesCompat.getDrawable( getResources() , R.drawable.celllayout_screenpanel , null );
						cl.invalidate( mBackgroundRect );
						cl.invalidate();
						cl.setDefaultPage( false );
					}
					else
					{
						cl.setDefaultPage( true );
					}
				}
			}
			mOverViewHomeView.setImageDrawable( getResources().getDrawable( R.drawable.editmode_home_selected ) );
			mNormalBackground = ResourcesCompat.getDrawable( getResources() , R.drawable.screenpanel_shape , null );
			invalidate();
		}
	}
	
	public ImageView getHomeView()
	{
		if( mOverViewHomeView != null )
		{
			return mOverViewHomeView;
		}
		return null;
	}
	
	public void setNormalBackgroundShape()
	{
		mNormalBackground = ResourcesCompat.getDrawable( getResources() , R.drawable.screenpanel_shape , null );
		invalidate();
	}
	
	public void setNormalBackground()
	{
		mNormalBackground = ResourcesCompat.getDrawable( getResources() , R.drawable.celllayout_screenpanel , null );
		invalidate();
	}
	
	public void setEditModeHomeViewVisible(
			int visibility )
	{
		if( visibility != mOverViewHomeView.getVisibility() )
		{
			mOverViewHomeView.setVisibility( visibility );
		}
	}
	
	public int getEditModeHomeViewVisible()
	{
		return mOverViewHomeView.getVisibility();
	}
	//gaominghui add end
	;
	
	//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
	public void addFolderBackground(
			FolderIcon.PreviewBackground bg )
	{
		if( !mFolderBackgrounds.contains( bg ) )
			mFolderBackgrounds.add( bg );
	}
	
	public void removeFolderBackground(
			FolderIcon.PreviewBackground bg )
	{
		mFolderBackgrounds.remove( bg );
	}
	//zhujieping add end
	;
	
	//cheyingkun add start	//修正widget行列数的计算方式
	/**初始化计算widget格子数的列表*/
	public static void initCellWidgetList()
	{
		//根据行列数和cell之间的空隙初始化列表
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		if( cellWidgetMaxWidthList == null )
		{
			cellWidgetMaxWidthList = new ArrayList<Integer>();
		}
		if( cellWidgetMaxHeightList == null )
		{
			cellWidgetMaxHeightList = new ArrayList<Integer>();
		}
		for( int i = 0 ; i < grid.getNumColumns() ; i++ )
		{
			//宽度=(i*cell格子之间的间隙)+(i+1)*cell格子宽度
			int maxWidth = i * grid.getCellWidthGapPx() + ( i + 1 ) * grid.getCellWidthPx();
			cellWidgetMaxWidthList.add( maxWidth );
		}
		for( int i = 0 ; i < grid.getNumRows() ; i++ )
		{
			//高度=(i*cell格子之间的间隙)+(i+1)*cell格子高度
			int maxWidth = i * grid.getCellHeightGapPx() + ( i + 1 ) * grid.getCellHeightPx();
			cellWidgetMaxHeightList.add( maxWidth );
		}
	}
	
	public static void clearCellWidgetList()
	{
		//清空之前的数据
		if( cellWidgetMaxWidthList != null && cellWidgetMaxWidthList.size() > 0 )
		{
			cellWidgetMaxWidthList.clear();
		}
		if( cellWidgetMaxHeightList != null && cellWidgetMaxHeightList.size() > 0 )
		{
			cellWidgetMaxHeightList.clear();
		}
	}
	
	/**
	 * Computes the required horizontal and vertical cell spans to always
	 * fit the given rectangle.
	 *
	 * @param width Width in pixels
	 * @param height Height in pixels
	 * @param result An array of length 2 in which to store the result (may be null).
	 */
	public static int[] rectToCellWidget(
			int width ,
			int height ,
			int[] result )
	{
		//计算格子数,拿到能放下widget的最小格子数
		int spanX = 0;
		int spanY = 0;
		if( cellWidgetMaxWidthList == null || cellWidgetMaxWidthList.size() <= 0 )
		{
			initCellWidgetList();
		}
		for( Integer w : cellWidgetMaxWidthList )
		{
			spanX++;
			if( width <= w )
			{
				break;
			}
		}
		for( Integer h : cellWidgetMaxHeightList )
		{
			spanY++;
			if( height <= h )
			{
				break;
			}
		}
		// Always round up to next largest cell
		if( result == null )
		{
			return new int[]{ spanX , spanY };
		}
		result[0] = spanX;
		result[1] = spanY;
		return result;
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	public boolean isFunctionPage()
	{
		return mIsFunctionPage;
	}
	//xiatian add end
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public void setCellLayoutChildAlpha(
			float alpha )
	{
		for( int i = 0 ; i < getChildCount() ; i++ )
		{
			View child = getChildAt( i );
			child.setAlpha( alpha );
		}
	}
	//zhujieping add end
}
