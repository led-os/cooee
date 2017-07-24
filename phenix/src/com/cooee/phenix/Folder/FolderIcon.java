package com.cooee.phenix.Folder;


import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.CellLayout;
import com.cooee.phenix.CheckLongPressHelper;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.DragLayer;
import com.cooee.phenix.DragView;
import com.cooee.phenix.DropTarget.DragObject;
import com.cooee.phenix.DynamicGrid;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAnimUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Workspace;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.FolderInfo.FolderListener;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.theme.ThemeManager;
import com.cooee.util.Tools;


/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends LinearLayout implements FolderListener
//
, IOnThemeChanged//zhujieping add .换主题不重启
{
	
	private Launcher mLauncher;
	private Folder mFolder;
	private FolderInfo mInfo;
	private static boolean sStaticValuesDirty = true;
	private CheckLongPressHelper mLongPressHelper;
	// The number of icons to display in the
	private static final int NUM_ITEMS_IN_PREVIEW = 3;
	private static final int CONSUMPTION_ANIMATION_DURATION = 100;
	private static final int DROP_IN_ANIMATION_DURATION = 400;
	private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
	private static final int FINAL_ITEM_ANIMATION_DURATION = 200;
	//// The degree to which the inner ring grows when accepting drop
	private static final float INNER_RING_GROWTH_FACTOR = 0.15f;
	// The degree to which the outer ring is scaled in its natural state
	// zhujieping@2015/03/18 UPD START
	//	private static final float OUTER_RING_GROWTH_FACTOR = 0.3f;
	private static float OUTER_RING_GROWTH_FACTOR = 0.25f;//拖动图标生成文件夹时，值大放大的大，调小一点
	// zhujieping@2015/03/18 UPD END
	// Flag as to whether or not to draw an outer ring. Currently none is designed.
	public static final boolean HAS_OUTER_RING = true;
	// The amount of vertical spread between items in the stack [0...1]
	private static final float PERSPECTIVE_SHIFT_FACTOR = 0.24f;
	// The degree to which the item in the back of the stack is scaled [0...1]
	// (0 means it's not scaled at all, 1 means it's scaled to nothing)
	private static final float PERSPECTIVE_SCALE_FACTOR = 0.35f;
	public static Drawable sSharedFolderLeaveBehind = null;
	private ImageView mPreviewBackground;
	private BubbleTextView mFolderName;
	FolderRingAnimator mFolderRingAnimator = null;
	// These variables are all associated with the drawing of the preview; they are stored
	// as member variables for shared usage and to avoid computation on each frame
	private int mIntrinsicIconWidthSize;
	private int mIntrinsicIconHeightSize;
	private int mTotalWidth = -1;
	private int mTotalHeight = -1;
	boolean mAnimating = false;
	private float mBaselineIconScale;
	private int mBaselineIconSize;
	private int mAvailableSpaceInPreview;
	private int mPreviewOffsetX;
	private int mPreviewOffsetY;
	private float mMaxPerspectiveShift;
	private Rect mOldBounds = new Rect();
	private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams( 0 , 0 , 0 , 0 );
	private PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams( 0 , 0 , 0 , 0 );
	private ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();
	private static float iconScaleFactor = 0.22f;
	private static int iconMargin = 5;
	private static int mPerviewIconPaddingLeft = 0;
	private static int mPerviewIconPaddingTop = 0;
	//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
	private static int mPerviewIconMarginXInItemStyle1 = 0;
	private static int mPerviewIconMarginYInItemStyle1 = 0;
	private static int mPerviewIconPaddingLeftInItemStyle1 = 0;
	private static int mPerviewIconPaddingTopInItemStyle1 = 0;
	private static float mImageValidRectXPaddingPercent = 0;
	private static float mImageValidRectYPaddingPercent = 0;
	//xiatian add end
	private static int mCountX = 3;
	private static int mCountY = 3;
	public static final int FOLDER_CUSTOM = 0;//原始文件夹打开样式
	public static final int FOLDER_FULLSCREEN = 1;//文件夹打开全屏样式
	/**当前文件夹的样式*/
	public static int folderStyle = FOLDER_CUSTOM;//cheyingkun add	//文件夹需求(长按显示边框)
	private static final int DISPLAY_NFLAG_MAX = 10;
	private final static String OperateFolderHotPrefix = "operatefolderhot";
	private final int HOT_TEXT_SIZE = 13;
	private static Paint mPaint;
	private static Drawable mHotBackground;
	private Rect mHotRect = new Rect();
	//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
	PreviewBackground mBackground = new PreviewBackground();
	Paint mBgPaint = new Paint();
	Bitmap mMask = null;
	PorterDuffXfermode xfermode = new PorterDuffXfermode( PorterDuff.Mode.DST_OUT );
	boolean isDrawMask = false;
	//zhujieping add end
	;
	
	public FolderIcon(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		init( context );
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//xiatian del start	
		//		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.FolderIcon );
		//		iconScaleFactor = a.getFloat( R.styleable.FolderIcon_iconScale , 0.22f );
		//		iconMargin = a.getDimensionPixelSize( R.styleable.FolderIcon_iconMargin , 5 );
		//		mCountX = a.getInt( R.styleable.FolderIcon_mCountX , 3 );
		//		mCountY = a.getInt( R.styleable.FolderIcon_mCountY , 3 );
		//		a.recycle();
		//xiatian del end	
		//		initConfig( context , attrs );//xiatian add	
		//xiatian end
	}
	
	public FolderIcon(
			Context context )
	{
		super( context );
		init( context );
	}
	
	private void init(
			Context context )
	{
		mLongPressHelper = new CheckLongPressHelper( this );
		initPerviewGrowthFactor();//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		initFolderStyle();//cheyingkun add	//文件夹需求(长按显示边框)
		initPerviewItemsConfig( getContext() );
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		{
			if( Build.VERSION.SDK_INT < 18 && ThemeManager.getInstance().currentThemeIsSystemTheme() )
				mMask = Tools.drawableToBitmap( getResources().getDrawable( R.drawable.theme_default_folder_icon_mask ) , Utilities.sIconWidth , Utilities.sIconWidth );
		}
		//zhujieping add end
	}
	
	private static void initPerviewGrowthFactor()
	{//文件夹预览图的放大比例
		//cheyingkun add start	//文件夹预览图层叠效果
		if(
		//
		( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
		//
		|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
		//
		)
		{
			OUTER_RING_GROWTH_FACTOR = 0.3f;
		}
		else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS )
		//cheyingkun add end	//文件夹预览图层叠效果
		{
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				OUTER_RING_GROWTH_FACTOR = 0.25f;
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				OUTER_RING_GROWTH_FACTOR = 0.15f;
			}
			//xiatian add end
		}
		//cheyingkun add start	//文件夹预览图层叠效果
		else
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "FolderIcon" , " LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE configException  " );
		}
		//cheyingkun add end	//文件夹预览图层叠效果
	}
	
	public boolean isDropEnabled()
	{
		final ViewGroup cellLayoutChildren = (ViewGroup)getParent();
		final ViewGroup cellLayout = (ViewGroup)cellLayoutChildren.getParent();
		final Workspace workspace = (Workspace)cellLayout.getParent();
		return !workspace.isSmall();
	}
	
	public static FolderIcon fromXml(
			int resId ,
			Launcher launcher ,
			ViewGroup group ,
			FolderInfo folderInfo ,
			IconCache iconCache )
	{
		@SuppressWarnings( "all" )
		// suppress dead code warning
		final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
		if( error )
		{
			throw new IllegalStateException( "DROP_IN_ANIMATION_DURATION must be greater than INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items is dependent on this" );
		}
		FolderIcon icon = (FolderIcon)LayoutInflater.from( launcher ).inflate( resId , group , false );
		icon.setClipToPadding( false );
		icon.mFolderName = (BubbleTextView)icon.findViewById( R.id.folder_icon_name );
		//xiatian add start	//图标名称和文件夹名称，是否显示文字阴影。true为显示（详细配置见<style name="WorkspaceIcon">），false为不显示。
		if( LauncherDefaultConfig.SWITCH_ENABLE_TITLE_SHADOW == false )
		{
			icon.mFolderName.getPaint().clearShadowLayer();
		}
		//xiatian add end
		//xiatian add start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//文件夹名称的左右间距（不使用“config_icon_title_and_foldericon_title_padding_left”和“config_icon_title_and_foldericon_title_padding_right”）。
			Resources mResources = launcher.getResources();
			int mPaddingLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_icon_title_and_foldericon_title_padding_left );
			int mPaddingTop = icon.mFolderName.getPaddingTop();
			int mPaddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_icon_title_and_foldericon_title_padding_right );
			int mPaddingBottom = icon.mFolderName.getPaddingBottom();
			icon.mFolderName.setPadding( mPaddingLeft , mPaddingTop , mPaddingRight , mPaddingBottom );
		}
		//cheyingkun add start	//自定义桌面布局
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{
				Resources mResources = launcher.getResources();
				int mPaddingLeft = icon.mFolderName.getPaddingLeft();
				int mPaddingRight = icon.mFolderName.getPaddingRight();
				int mPaddingBottom = icon.mFolderName.getPaddingBottom();
				//和兴六部图标大小变化需求	//cheyingkun add start
				LauncherAppState app = LauncherAppState.getInstance();
				DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
				int mPaddingTop;
				if( grid.isCustomLayoutNormalIcon() )
				{//小图标
					mPaddingTop = grid.getDefaultGapBetweenIconAndText();
				}
				else
				{//大图标
					mPaddingTop = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_icon_padding_gop_text_and_icon_big_icon );
				}
				icon.mFolderName.setPadding( mPaddingLeft , mPaddingTop , mPaddingRight , mPaddingBottom );
			}
		}
		//cheyingkun add end
		//xiatian add end
		//0010396: 【文件夹】英文状态下，智能分类后的文件夹名称仍然是中文 , change by shlt@2015/03/10 UPD START
		//icon.mFolderName.setText( folderInfo.title );
		icon.mFolderName.setText( folderInfo.getTitle() );
		//0010396: 【文件夹】英文状态下，智能分类后的文件夹名称仍然是中文 , change by shlt@2015/03/10 UPD END
		icon.mPreviewBackground = (ImageView)icon.findViewById( R.id.preview_background );
		// zhujieping@2015/03/17 ADD START
		//xiatian add start	//需求：适配“新主题”，兼容“老主题”。
		//xiatian del start
		//		if( launcher.mThemeManager.getCurrentThemeDescription() != null )
		//		{
		//			Drawable mDrawable = ThemeManager.getInstance().getDrawableIgnoreSystemTheme( LauncherDefaultConfig.THEME_FOLDER_ICON_DIR );
		//			if( mDrawable != null )
		//			{
		//				icon.mPreviewBackground.setImageDrawable( mDrawable );
		//			}
		//		}
		//xiatian del end
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//xiatian add start
		if( ThemeManager.getInstance()/*launcher.mThemeManager*/!= null )
		{
			//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			//			icon.mPreviewBackground.setImageDrawable( ThemeManager.getInstance().getFolderIconBg() );//xiatian del
			setFolderIconBg( icon );//xiatian add
			//xiatian end
			// zhujieping@2015/06/11 ADD START,换主题，读取主题中的文件夹信息,防止图标溢出【c_0002953】
			if( ThemeManager.getInstance().getCurrentThemeDescription() != null && !ThemeManager.getInstance().getCurrentThemeDescription().mSystem )
			{
				//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
				//xiatian del start	
				//				iconScaleFactor = (float)( ThemeManager.getInstance().getInt( "folder_icon_scale_factor" , (int)( iconScaleFactor * 100 ) ) / 100f );
				//				mCountX = ThemeManager.getInstance().getInt( "folder_icon_row_num" , mCountX );
				//				mCountY = ThemeManager.getInstance().getInt( "folder_transform_num" , mCountY * mCountX ) / mCountX;
				//				int offset = ThemeManager.getInstance().getInt( "folder_front_margin_offset" , 0 );
				//				iconScaleFactor = ( grid.getFolderIconWidthSizePx() - offset * 2 - iconMargin * ( mCountX - 1 ) ) * 1.0f / mCountX / grid.getIconWidthSizePx();
				//xiatian del end	
				initPerviewItemsConfig( launcher );//xiatian add
				//xiatian end
			}
			// zhujieping@2015/06/11 ADD END
		}
		//xiatian add end
		//xiatian end
		// zhujieping@2015/03/17 ADD END
		// Offset the preview background to center this view accordingly
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)icon.mPreviewBackground.getLayoutParams();
		lp.topMargin = grid.getFolderBackgroundOffset();
		lp.width = grid.getFolderIconWidthSizePx();
		lp.height = grid.getFolderIconHeightSizePx();
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		if( ThemeManager.getInstance().currentThemeIsSystemTheme() && LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		{
			if( Build.VERSION.SDK_INT >= 18 )
			{
				icon.mPreviewBackground.setVisibility( View.INVISIBLE );
			}
		}
		//zhujieping add end
		//cheyingkun add start	//默认图标样式下,添加图标和文字之间的间距配置(文件夹图标和名称的间距)【c_0004390】
		LinearLayout.LayoutParams lpFolderName = (LinearLayout.LayoutParams)icon.mFolderName.getLayoutParams();
		lpFolderName.topMargin = getFolderIconTitleOffset( launcher );
		//cheyingkun add end
		icon.setTag( folderInfo );
		icon.setOnClickListener( launcher );
		icon.mInfo = folderInfo;
		icon.mLauncher = launcher;
		Folder folder = null;
		if( folderStyle == FolderIcon.FOLDER_CUSTOM )
		{
			folder = Folder.fromXml( launcher );
		}
		else if( folderStyle == FolderIcon.FOLDER_FULLSCREEN )
		{
			folder = FolderFullScreen.fromXml( launcher );
		}
		folder.setDragController( launcher.getDragController() );
		folder.setFolderIcon( icon );
		folder.bind( folderInfo );
		icon.mFolder = folder;
		icon.mFolderRingAnimator = new FolderRingAnimator( launcher , icon );
		folderInfo.addListener( icon );
		return icon;
	}
	
	@Override
	protected Parcelable onSaveInstanceState()
	{
		sStaticValuesDirty = true;
		return super.onSaveInstanceState();
	}
	
	public static class FolderRingAnimator
	{
		
		public int mCellX;
		public int mCellY;
		private CellLayout mCellLayout;
		public float mOuterRingWidthSize;
		public float mOuterRingHeightSize;
		public float mInnerRingSize;
		public FolderIcon mFolderIcon = null;
		public static Drawable sSharedOuterRingDrawable = null;
		public static Drawable sSharedInnerRingDrawable = null;
		public static int sPreviewWidthSize = -1;
		public static int sPreviewHeightSize = -1;
		public static int sPreviewPadding = -1;
		private ValueAnimator mAcceptAnimator;
		private ValueAnimator mNeutralAnimator;
		
		@SuppressWarnings( "deprecation" )
		public FolderRingAnimator(
				Launcher launcher ,
				FolderIcon folderIcon )
		{
			mFolderIcon = folderIcon;
			Resources res = launcher.getResources();
			// We need to reload the static values when configuration changes in case they are
			// different in another configuration
			if( sStaticValuesDirty )
			{
				if( Looper.myLooper() != Looper.getMainLooper() )
				{
					throw new RuntimeException( "FolderRingAnimator loading drawables on non-UI thread " + Thread.currentThread() );
				}
				//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
				//xiatian del start
				//				LauncherAppState app = LauncherAppState.getInstance();
				//				DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
				//				sPreviewWidthSize = grid.getFolderIconWidthSizePx();
				//				sPreviewHeightSize = grid.getFolderIconHeightSizePx();
				//xiatian del end
				//xiatian add start
				initPerviewGrowthFactor();
				sPreviewWidthSize = getPreviewWidthSize();
				sPreviewHeightSize = getPreviewHeightSize();
				//xiatian add end
				//xiatian end
				sPreviewPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.folder_preview_padding );
				//xiatian start	//需求：适配“新主题”，兼容“老主题”。
				//				sSharedOuterRingDrawable = res.getDrawable( R.drawable.folder_icon_bg );//xiatian del
				//xiatian add start
				if( ThemeManager.getInstance() != null )
				{
					sSharedOuterRingDrawable = ThemeManager.getInstance().getFolderIconBg();
				}
				else
				{
					sSharedOuterRingDrawable = res.getDrawable( R.drawable.theme_default_folder_icon_bg );
				}
				//xiatian add end
				//xiatian end
				//cheyingkun add start	//文件夹预览图层叠效果
				if(
				//
				( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
				//
				|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
				//
				)
				{
					sSharedInnerRingDrawable = res.getDrawable( R.drawable.portal_ring_inner_nolip_holo );
					sSharedFolderLeaveBehind = res.getDrawable( R.drawable.portal_ring_rest );
				}
				//cheyingkun add end	//文件夹预览图层叠效果
				sStaticValuesDirty = false;
			}
		}
		
		public void animateToAcceptState()
		{
			if( mNeutralAnimator != null )
			{
				mNeutralAnimator.cancel();
			}
			mAcceptAnimator = LauncherAnimUtils.ofFloat( mCellLayout , 0f , 1f );
			mAcceptAnimator.setDuration( CONSUMPTION_ANIMATION_DURATION );
			final int previewWidthSize = sPreviewWidthSize;
			final int previewHeightSize = sPreviewHeightSize;
			mAcceptAnimator.addUpdateListener( new AnimatorUpdateListener() {
				
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					final float percent = (Float)animation.getAnimatedValue();
					mOuterRingWidthSize = ( 1 + percent * OUTER_RING_GROWTH_FACTOR ) * previewWidthSize;
					mOuterRingHeightSize = ( 1 + percent * OUTER_RING_GROWTH_FACTOR ) * previewHeightSize;
					//cheyingkun add start	//文件夹预览图层叠效果
					if(
					//
					( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
					//
					|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
					//
					)
					{
						mInnerRingSize = ( 1 + percent * INNER_RING_GROWTH_FACTOR ) * previewWidthSize;
					}
					//cheyingkun add end	//文件夹预览图层叠效果
					if( mCellLayout != null )
					{
						mCellLayout.invalidate();
					}
				}
			} );
			mAcceptAnimator.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationStart(
						Animator animation )
				{
					//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
					////xiatian del start
					//					if( mFolderIcon != null )
					//					{
					//						mFolderIcon.mPreviewBackground.setVisibility( INVISIBLE );
					//					}
					//xiatian del end
					onStartToAcceptState();//xiatian add 
					//xiatian end
				}
			} );
			mAcceptAnimator.start();
		}
		
		public void animateToNaturalState()
		{
			if( mAcceptAnimator != null )
			{
				mAcceptAnimator.cancel();
			}
			mNeutralAnimator = LauncherAnimUtils.ofFloat( mCellLayout , 0f , 1f );
			mNeutralAnimator.setDuration( CONSUMPTION_ANIMATION_DURATION );
			final int previewWidthSize = sPreviewWidthSize;
			final int previewHeightSize = sPreviewHeightSize;
			mNeutralAnimator.addUpdateListener( new AnimatorUpdateListener() {
				
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					final float percent = (Float)animation.getAnimatedValue();
					mOuterRingWidthSize = ( 1 + ( 1 - percent ) * OUTER_RING_GROWTH_FACTOR ) * previewWidthSize;
					mOuterRingHeightSize = ( 1 + ( 1 - percent ) * OUTER_RING_GROWTH_FACTOR ) * previewHeightSize;
					//cheyingkun add start	//文件夹预览图层叠效果
					if(
					//
					( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
					//
					|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
					//
					)
					{
						mInnerRingSize = ( 1 + ( 1 - percent ) * INNER_RING_GROWTH_FACTOR ) * previewWidthSize;
					}
					//cheyingkun add end	//文件夹预览图层叠效果
					if( mCellLayout != null )
					{
						mCellLayout.invalidate();
					}
				}
			} );
			mNeutralAnimator.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
					//xiatian del start
					//					if( mCellLayout != null )
					//					{
					//						mCellLayout.hideFolderAccept( FolderRingAnimator.this );
					//					}
					//					if( mFolderIcon != null )
					//					{
					//						mFolderIcon.mPreviewBackground.setVisibility( VISIBLE );
					//					}
					//xiatian del end
					onEndToNaturalState();//xiatian add 
					//xiatian end
				}
			} );
			mNeutralAnimator.start();
		}
		
		// Location is expressed in window coordinates
		public void getCell(
				int[] loc )
		{
			loc[0] = mCellX;
			loc[1] = mCellY;
		}
		
		// Location is expressed in window coordinates
		public void setCell(
				int x ,
				int y )
		{
			mCellX = x;
			mCellY = y;
		}
		
		public void setCellLayout(
				CellLayout layout )
		{
			mCellLayout = layout;
		}
		
		public float getOuterRingWidthSize()
		{
			return mOuterRingWidthSize;
		}
		
		public float getOuterRingHeightSize()
		{
			return mOuterRingHeightSize;
		}
		
		public float getInnerRingSize()
		{
			return mInnerRingSize;
		}
		
		public static int getPreviewWidthSize()
		{
			int mPreviewWidthSize = 0;
			LauncherAppState app = LauncherAppState.getInstance();
			DynamicGrid mDynamicGrid = app.getDynamicGrid();
			if( mDynamicGrid != null )
			{
				DeviceProfile grid = mDynamicGrid.getDeviceProfile();
				if( grid != null )
				{
					//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
					if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
					{
						mPreviewWidthSize = grid.getFolderIconWidthSizePx();
					}
					else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
					{//文件夹打开后，每一个view的宽度。
						mPreviewWidthSize = grid.getSignleViewAvailableWidthPx();
					}
					//xiatian add end
				}
			}
			return mPreviewWidthSize;
		}
		
		public static int getPreviewHeightSize()
		{
			int mPreviewHeightSize = 0;
			LauncherAppState app = LauncherAppState.getInstance();
			DynamicGrid mDynamicGrid = app.getDynamicGrid();
			if( mDynamicGrid != null )
			{
				DeviceProfile grid = mDynamicGrid.getDeviceProfile();
				if( grid != null )
				{
					//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
					if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
					{
						mPreviewHeightSize = grid.getFolderIconHeightSizePx();
					}
					else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
					{////文件夹预览图（一个图标覆盖到另一个图标或者文件夹上会生成文件夹预览图）的图标的高度。
						mPreviewHeightSize = grid.getSignleViewAvailableHeightPx();
					}
					//xiatian add end
				}
			}
			return mPreviewHeightSize;
		}
		
		private void onStartToAcceptState()
		{
			if( mFolderIcon != null )
			{
				//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
				//						mFolderIcon.mPreviewBackground.setVisibility( INVISIBLE );//xiatian del
				//xiatian add start
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
				{
					mFolderIcon.mPreviewBackground.setVisibility( INVISIBLE );
				}
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
				{//拖动一个图标到一个文件夹上面，当文件夹预览图出现时，隐藏文件夹背景和文件夹名字。
					// gaominghui@2016/12/14 ADD START 兼容Android 4.0
					//mFolderIcon.setBackground(null);
					mFolderIcon.setBackgroundDrawable( null );
					// gaominghui@2016/12/14 ADD END 兼容Android 4.0
					mFolderIcon.setTextVisible( false );
				}
				//xiatian add end
				//xiatian end
			}
		}
		
		private void onEndToNaturalState()
		{
			if( mCellLayout != null )
			{
				mCellLayout.hideFolderAccept( FolderRingAnimator.this );
			}
			if( mFolderIcon != null )
			{
				//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
				//						mFolderIcon.mPreviewBackground.setVisibility( VISIBLE );//xiatian del
				//xiatian add start
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
				{
					mFolderIcon.mPreviewBackground.setVisibility( VISIBLE );
				}
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
				{//拖动一个图标到一个文件夹上面后再从文件夹上移开，当文件夹预览图消失时，显示文件夹背景和文件夹名字。
					// gaominghui@2016/12/14 ADD START 兼容Android 4.0
					//mFolderIcon.setBackground( ThemeManager.getInstance().getFolderIconBg() );
					mFolderIcon.setBackgroundDrawable( ThemeManager.getInstance().getFolderIconBg() );
					// gaominghui@2016/12/14 ADD END 兼容Android 4.0
					mFolderIcon.setTextVisible( true );
				}
				//xiatian add end
				//xiatian end
			}
		}
	}
	
	public Folder getFolder()
	{
		return mFolder;
	}
	
	public FolderInfo getFolderInfo()
	{
		return mInfo;
	}
	
	private boolean willAcceptItem(
			ItemInfo item )
	{
		//xiatian start	//整理代码：整理接口willAcceptItem
		//xiatian del start
		//		final int itemType = item.getItemType();
		//		boolean mItemWillAcceptItem = ( itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
		//		//
		//		//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//		//该图标是否可以放入文件夹
		//		|| itemType == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL
		//		//xiatian add end
		//		//
		//		);
		//xiatian del end
		boolean mItemWillAcceptItem = item.willAcceptItem();//xiatian add
		//xiatian end
		return( ( mItemWillAcceptItem ) && !mFolder.isFull() && item != mInfo && !mInfo.getOpened() );
	}
	
	public boolean acceptDrop(
			Object dragInfo )
	{
		final ItemInfo item = (ItemInfo)dragInfo;
		return !mFolder.isDestroyed() && willAcceptItem( item );
	}
	
	public void addItem(
			ShortcutInfo item )
	{
		mInfo.add( item );
	}
	
	public void onDragEnter(
			Object dragInfo )
	{
		if( mFolder.isDestroyed() || !willAcceptItem( (ItemInfo)dragInfo ) )
			return;
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams)getLayoutParams();
		CellLayout layout = (CellLayout)getParent().getParent();
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
			mBackground.animateToAccept( layout , lp.cellX , lp.cellY );
		}
		else
		//zhujieping add end
		{
			mFolderRingAnimator.setCell( lp.cellX , lp.cellY );
			mFolderRingAnimator.setCellLayout( layout );
			mFolderRingAnimator.animateToAcceptState();
			layout.showFolderAccept( mFolderRingAnimator );
		}
	}
	
	public void onDragOver(
			Object dragInfo )
	{
	}
	
	public void performCreateAnimation(
			final ShortcutInfo destInfo ,
			final View destView ,
			final ShortcutInfo srcInfo ,
			final DragView srcView ,
			Rect dstRect ,
			float scaleRelativeToDragLayer ,
			Runnable postAnimationRunnable )
	{
		// These correspond two the drawable and view that the icon was dropped _onto_
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		Drawable animateDrawable = ( (TextView)destView ).getCompoundDrawables()[1];//xiatian del
		Drawable animateDrawable = ( destView instanceof BubbleTextView ) ? ( (BubbleTextView)destView ).getIcon() : ( (TextView)destView ).getCompoundDrawables()[1];//xiatian add
		//xiatian end
		computePreviewDrawingParams( animateDrawable.getIntrinsicWidth() , animateDrawable.getIntrinsicHeight() , destView.getMeasuredWidth() , destView.getMeasuredHeight() );
		// This will animate the first item from it's position as an icon into its
		// position as the first item in the preview
		animateFirstItem( animateDrawable , INITIAL_ITEM_ANIMATION_DURATION , false , null );
		addItem( destInfo );
		// This will animate the dragView (srcView) into the new folder
		onDrop( srcInfo , srcView , dstRect , scaleRelativeToDragLayer , 1 , postAnimationRunnable , null );
	}
	
	public void performDestroyAnimation(
			final View finalView ,
			Runnable onCompleteRunnable )
	{
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		Drawable animateDrawable = ((TextView)finalView ).getCompoundDrawables()[1];//xiatian del
		Drawable animateDrawable = ( finalView instanceof BubbleTextView ) ? ( (BubbleTextView)finalView ).getIconForPerview() : ( (TextView)finalView ).getCompoundDrawables()[1];//xiatian add
		//xiatian end
		computePreviewDrawingParams( animateDrawable.getIntrinsicWidth() , animateDrawable.getIntrinsicHeight() , finalView.getMeasuredWidth() , finalView.getMeasuredHeight() );
		// This will animate the first item from it's position as an icon into its
		// position as the first item in the preview
		animateFirstItem( animateDrawable , FINAL_ITEM_ANIMATION_DURATION , true , onCompleteRunnable );
	}
	
	public void onDragExit(
			Object dragInfo )
	{
		onDragExit();
	}
	
	public void onDragExit()
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
			mBackground.animateToRest();
		}
		else
		//zhujieping add end
		{
			mFolderRingAnimator.animateToNaturalState();
		}
	}
	
	private void onDrop(
			final ShortcutInfo item ,
			DragView animateView ,
			Rect finalRect ,
			float scaleRelativeToDragLayer ,
			int index ,
			Runnable postAnimationRunnable ,
			DragObject d )
	{
		item.setCellX( -1 );
		item.setCellY( -1 );
		// Typically, the animateView corresponds to the DragView; however, if this is being done
		// after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
		// will not have a view to animate
		if( animateView != null )
		{
			DragLayer dragLayer = mLauncher.getDragLayer();
			Rect from = new Rect();
			dragLayer.getViewRectRelativeToSelf( animateView , from );
			Rect to = finalRect;
			if( to == null )
			{
				to = new Rect();
				Workspace workspace = mLauncher.getWorkspace();
				// Set cellLayout and this to it's final state to compute final animation locations
				workspace.setFinalTransitionTransform( (CellLayout)getParent().getParent() );
				float scaleX = getScaleX();
				float scaleY = getScaleY();
				setScaleX( 1.0f );
				setScaleY( 1.0f );
				scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf( this , to );
				// Finished computing final animation locations, restore current state
				setScaleX( scaleX );
				setScaleY( scaleY );
				workspace.resetTransitionTransform( (CellLayout)getParent().getParent() );
			}
			int[] center = new int[2];
			//cheyingkun add start	//文件夹预览图层叠效果
			if(
			//
			( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
			//
			|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
			//
			)
			{
				float scale = getLocalCenterForIndex( index , center );
				center[0] = (int)Math.round( scaleRelativeToDragLayer * center[0] );
				center[1] = (int)Math.round( scaleRelativeToDragLayer * center[1] );
				to.offset( center[0] - animateView.getMeasuredWidth() / 2 , center[1] - animateView.getMeasuredHeight() / 2 );
				float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;
				float finalScale = scale * scaleRelativeToDragLayer;
				dragLayer.animateView(
						animateView ,
						from ,
						to ,
						finalAlpha ,
						1 ,
						1 ,
						finalScale ,
						finalScale ,
						DROP_IN_ANIMATION_DURATION ,
						new DecelerateInterpolator( 2 ) ,
						new AccelerateInterpolator( 2 ) ,
						postAnimationRunnable ,
						DragLayer.ANIMATION_END_DISAPPEAR ,
						null );
			}
			else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS || LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
			//cheyingkun add end	//文件夹预览图层叠效果
			{
				// zhujieping@2015/03/19 ADD START
				//文件夹预览改变，动画缩放比例、位置改变
				float scale = iconScaleFactor;//getLocalCenterForIndex( index , center );
				Point point = getIconPoint( index );
				int mCellWidth = 0;
				int mCellHeight = 0;
				int mAnimateViewWidth = 0;
				int mAnimateViewHeight = 0;
				//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
				{
					mCellWidth = mIntrinsicIconWidthSize;
					mCellHeight = mIntrinsicIconHeightSize;
					mAnimateViewWidth = animateView.getMeasuredWidth();
					mAnimateViewHeight = animateView.getMeasuredHeight();
				}
				else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
				{
					mCellWidth = mTotalWidth;
					mCellHeight = mTotalHeight;
					mAnimateViewWidth = animateView.getWidth();
					mAnimateViewHeight = animateView.getHeight();
				}
				//xiatian add end
				center[0] = (int)( point.x + iconScaleFactor * mCellWidth / 2 );//(int)Math.round( scaleRelativeToDragLayer * center[0] );
				center[1] = (int)( point.y + +iconScaleFactor * mCellHeight / 2 );//(int)Math.round( scaleRelativeToDragLayer * center[1] );
				to.offset( center[0] - mAnimateViewWidth / 2 , center[1] - mAnimateViewHeight / 2 );
				float finalAlpha = index < ( mCountX * mCountY ) ? 1.0f : 0;//index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;
				float finalScale = scale * scaleRelativeToDragLayer;
				int animationEndType = index < ( mCountX * mCountY ) ? DragLayer.ANIMATION_END_FADE_OUT : DragLayer.ANIMATION_END_DISAPPEAR;
				dragLayer.animateView(
						animateView ,
						from ,
						to ,
						finalAlpha ,
						1 ,
						1 ,
						finalScale ,
						finalScale ,
						DROP_IN_ANIMATION_DURATION ,
						new DecelerateInterpolator( 2 ) ,
						new AccelerateInterpolator( 2 ) ,
						postAnimationRunnable ,
						animationEndType ,
						null );
				// zhujieping@2015/03/19 ADD END
			}
			//cheyingkun add start	//文件夹预览图层叠效果
			else
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "FolderIcon" , " LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE configException  " );
			}
			//cheyingkun add end	//文件夹预览图层叠效果
			addItem( item );
			mHiddenItems.add( item );
			mFolder.hideItem( item );
			postDelayed( new Runnable() {
				
				public void run()
				{
					mHiddenItems.remove( item );
					mFolder.showItem( item );
					invalidate();
				}
			} , DROP_IN_ANIMATION_DURATION );
		}
		else
		{
			addItem( item );
			//cheyingkun add start	//解决“文件夹内只有一个图标，托动此图标至卸载区域提示是否卸载时选择取消，返回桌面时应用图标消失”的问题。【i_0010416】
			//xiatian start	//需求:修改智能分类后文件夹解散的逻辑。（fix bug：解决“智能分类成功后将桌面文件夹内最后一个应用托至卸载区域，提示确认卸载是占击取消，返回桌面后文件夹消失”的问题。）
			//xiatian del start
			//				int childCount = mFolder.mInfo.getContents().size();
			//				if( childCount <= 1 )
			//xiatian del end
			if( Folder.isNeedDeleteFolder( mInfo , mLauncher ) )//xiatian add
			//xiatian end
			{
				mFolder.setDestroyed( false );
				mFolder.replaceFolderWithFinalItem();
			}
			//cheyingkun add end
		}
	}
	
	public void onDrop(
			DragObject d )
	{
		ShortcutInfo item;
		if( d.dragInfo instanceof AppInfo )
		{
			// Came from all apps -- make a copy
			item = ( (AppInfo)d.dragInfo ).makeShortcut();
		}
		else
		{
			item = (ShortcutInfo)d.dragInfo;
		}
		mFolder.notifyDrop();
		onDrop( item , d.dragView , null , 1.0f , mInfo.getContents().size() , d.postAnimationRunnable , d );
	}
	
	private void computePreviewDrawingParams(
			int drawableWidthSize ,
			int drawableHeightSize ,
			int totalWidth ,
			int totalHeight )
	{
		if( mIntrinsicIconWidthSize != drawableWidthSize || mIntrinsicIconHeightSize != drawableHeightSize || mTotalWidth != totalWidth || mTotalHeight != totalHeight )
		{
			//cheyingkun add start	//文件夹预览图层叠效果
			if(
			//
			( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
			//
			|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
			//
			)
			{
				//xiatian del start	//删除已经不用的代码（“由于foldericon的文件夹内小图标的预览方式由原生的层叠显示改为苹果的宫格显示，一些代码不再使用”）。
				LauncherAppState app = LauncherAppState.getInstance();
				DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
				mIntrinsicIconWidthSize = drawableWidthSize;
				mTotalWidth = totalWidth;
				final int previewSize = mPreviewBackground.getLayoutParams().height;
				final int previewPadding = FolderRingAnimator.sPreviewPadding;
				mAvailableSpaceInPreview = ( previewSize - 2 * previewPadding );
				// cos(45) = 0.707  + ~= 0.1) = 0.8f
				int adjustedAvailableSpace = (int)( ( mAvailableSpaceInPreview / 2 ) * ( 1 + 0.8f ) );
				int unscaledHeight = (int)( mIntrinsicIconWidthSize * ( 1 + PERSPECTIVE_SHIFT_FACTOR ) );
				mBaselineIconScale = ( 1.0f * adjustedAvailableSpace / unscaledHeight );
				mBaselineIconSize = (int)( mIntrinsicIconWidthSize * mBaselineIconScale );
				mMaxPerspectiveShift = mBaselineIconSize * PERSPECTIVE_SHIFT_FACTOR;
				mPreviewOffsetX = ( mTotalWidth - mAvailableSpaceInPreview ) / 2;
				mPreviewOffsetY = previewPadding + grid.getFolderBackgroundOffset();
				//xiatian del end
			}
			else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS )
			//cheyingkun add end	//文件夹预览图层叠效果
			{
				mIntrinsicIconWidthSize = drawableWidthSize;
				mIntrinsicIconHeightSize = drawableHeightSize;
				mTotalWidth = totalWidth;
				mTotalHeight = totalHeight;
			}
			//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
			else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
			{
				mIntrinsicIconWidthSize = drawableWidthSize;
				mIntrinsicIconHeightSize = drawableHeightSize;
				mTotalWidth = totalWidth;
				mTotalHeight = totalHeight;
				if( Build.VERSION.SDK_INT >= 18 )
				{
					DeviceProfile grid = mLauncher.getDeviceProfile();
					mBackground.setup( getResources().getDisplayMetrics() , grid , this , mTotalWidth , getPaddingTop() );
				}
			}
			//zhujieping add end
			//cheyingkun add start	//文件夹预览图层叠效果
			else
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "FolderIcon" , " LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE configException  " );
			}
			//cheyingkun add end	//文件夹预览图层叠效果
		}
	}
	
	private void computePreviewDrawingParams(
			Drawable d )
	{
		computePreviewDrawingParams( d.getIntrinsicWidth() , d.getIntrinsicHeight() , getMeasuredWidth() , getMeasuredHeight() );
	}
	
	class PreviewItemDrawingParams
	{
		
		PreviewItemDrawingParams(
				float transX ,
				float transY ,
				float scale ,
				int overlayAlpha )
		{
			this.transX = transX;
			this.transY = transY;
			this.scale = scale;
			this.overlayAlpha = overlayAlpha;
		}
		
		float transX;
		float transY;
		float scale;
		int overlayAlpha;
		Drawable drawable;
	}
	
	//xiatian del start	//删除已经不用的代码（“由于foldericon的文件夹内小图标的预览方式由原生的层叠显示改为苹果的宫格显示，一些代码不再使用”）。
	private float getLocalCenterForIndex(
			int index ,
			int[] center )
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
			mParams = computePreviewItemDrawingParams( Math.min( NUM_ITEMS_IN_PREVIEW , index ) , mParams );
			mParams.transX += mPreviewOffsetX;
			mParams.transY += mPreviewOffsetY;
			float offsetX = mParams.transX + ( mParams.scale * mIntrinsicIconWidthSize ) / 2;
			float offsetY = mParams.transY + ( mParams.scale * mIntrinsicIconWidthSize ) / 2;
			center[0] = (int)Math.round( offsetX );
			center[1] = (int)Math.round( offsetY );
			return mParams.scale;
		}
		return 0;
	}
	
	private PreviewItemDrawingParams computePreviewItemDrawingParams(
			int index ,
			PreviewItemDrawingParams params )
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
			index = NUM_ITEMS_IN_PREVIEW - index - 1;
			float r = ( index * 1.0f ) / ( NUM_ITEMS_IN_PREVIEW - 1 );
			float scale = ( 1 - PERSPECTIVE_SCALE_FACTOR * ( 1 - r ) );
			float offset = ( 1 - r ) * mMaxPerspectiveShift;
			float scaledSize = scale * mBaselineIconSize;
			float scaleOffsetCorrection = ( 1 - scale ) * mBaselineIconSize;
			// We want to imagine our coordinates from the bottom left, growing up and to the
			// right. This is natural for the x-axis, but for the y-axis, we have to invert things.
			float transY = mAvailableSpaceInPreview - ( offset + scaledSize + scaleOffsetCorrection ) + getPaddingTop();
			float transX = 0;
			if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
			{
				transX = offset + scaleOffsetCorrection;
			}
			else
			{
				transX = ( mAvailableSpaceInPreview - scaledSize ) / 2;
			}
			float totalScale = mBaselineIconScale * scale;
			final int overlayAlpha = (int)( 80 * ( 1 - r ) );
			if( params == null )
			{
				params = new PreviewItemDrawingParams( transX , transY , totalScale , overlayAlpha );
			}
			else
			{
				params.transX = transX;
				params.transY = transY;
				params.scale = totalScale;
				params.overlayAlpha = overlayAlpha;
			}
		}
		return params;
	}
	
	private void drawPreviewItem(
			Canvas canvas ,
			PreviewItemDrawingParams params )
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
			canvas.save();
			canvas.translate( params.transX + mPreviewOffsetX , params.transY + mPreviewOffsetY );
			canvas.scale( params.scale , params.scale );
			Drawable d = params.drawable;
			if( d != null )
			{
				mOldBounds.set( d.getBounds() );
				d.setBounds( 0 , 0 , mIntrinsicIconWidthSize , mIntrinsicIconWidthSize );
				d.setFilterBitmap( true );
				d.setColorFilter( Color.argb( params.overlayAlpha , 255 , 255 , 255 ) , PorterDuff.Mode.SRC_ATOP );
				d.draw( canvas );
				d.clearColorFilter();
				d.setFilterBitmap( false );
				d.setBounds( mOldBounds );
			}
			canvas.restore();
		}
	}
	
	//xiatian del end
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		int saveLayer = -1;
		if( ThemeManager.getInstance().currentThemeIsSystemTheme() && LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		{
			if( Build.VERSION.SDK_INT < 18 )
			{
				saveLayer = canvas.saveLayer(
						0 ,
						0 ,
						getWidth() ,
						getHeight() ,
						null ,
						Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG );
			}
		}
		//zhujieping add end
		super.dispatchDraw( canvas );
		if( mFolder == null )
		{
			//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
			if( saveLayer != -1 )
				canvas.restoreToCount( saveLayer );
			//zhujieping add end
			return;
		}
		if( mFolder.getItemCount() == 0 && !mAnimating )
		{
			//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
			if( saveLayer != -1 )
				canvas.restoreToCount( saveLayer );
			//zhujieping add end
			return;
		}
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		if( ThemeManager.getInstance().currentThemeIsSystemTheme() && LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		{
			if( Build.VERSION.SDK_INT >= 18 )
			{
				if( !mBackground.drawingDelegated() )
				{
					mBackground.drawBackground( canvas , mBgPaint );
				}
				mBackground.clipCanvas( canvas );
			}
		}
		//zhujieping add end
		ArrayList<View> items = mFolder.getItemsInReadingOrder();
		Drawable d = null;
		TextView v;
		// Update our drawing parameters if necessary
		if( mAnimating )
		{
			computePreviewDrawingParams( mAnimParams.drawable );
		}
		else
		{
			v = (TextView)items.get( 0 );
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			//			d = v.getCompoundDrawables()[1];//xiatian del
			d = ( v instanceof BubbleTextView ) ? ( (BubbleTextView)v ).getIconForPerview() : v.getCompoundDrawables()[1];//xiatian add
			//xiatian end
			computePreviewDrawingParams( d );
		}
		//cheyingkun add start	//文件夹预览图层叠效果
		int nItemsInPreview = 0;
		if(
		//
		( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
		//
		|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
		//
		)
		{
			nItemsInPreview = Math.min( items.size() , NUM_ITEMS_IN_PREVIEW );
		}
		else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS || LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		//cheyingkun add end	//文件夹预览图层叠效果
		{
			// zhujieping@2015/03/18 ADD START
			nItemsInPreview = Math.min( items.size() , mCountX * mCountY );
			// zhujieping@2015/03/18 ADD END
		}
		//cheyingkun add start	//文件夹预览图层叠效果
		else
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "FolderIcon" , " LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE configException  " );
		}
		//cheyingkun add end	//文件夹预览图层叠效果
		if( !mAnimating )
		{
			//cheyingkun add start	//文件夹预览图层叠效果
			if(
			//
			( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
			//
			|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
			//
			)
			{
				for( int i = nItemsInPreview - 1 ; i >= 0 ; i-- )
				{
					v = (TextView)items.get( i );
					if( !mHiddenItems.contains( v.getTag() ) )
					{
						d = v.getCompoundDrawables()[1];
						mParams = computePreviewItemDrawingParams( i , mParams );
						mParams.drawable = d;
						drawPreviewItem( canvas , mParams );
					}
				}
			}
			else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS || LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
			//cheyingkun add end	//文件夹预览图层叠效果
			{
				Point point = null;
				for( int i = 0 ; i < nItemsInPreview ; i++ )
				{
					v = (TextView)items.get( i );
					if( !mHiddenItems.contains( v.getTag() ) )
					{
						point = getIconPoint( i );
						drawPreviewItem( canvas , point.x , point.y , v );
					}
				}
			}
			//cheyingkun add start	//文件夹预览图层叠效果
			else
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "FolderIcon" , " LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE configException  " );
			}
			//cheyingkun add end	//文件夹预览图层叠效果
		}
		else
		{
			//cheyingkun add start	//文件夹预览图层叠效果
			if(
			//
			( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
			//
			|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
			//
			)
			{
				drawPreviewItem( canvas , mAnimParams );
			}
			else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS || LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
			//cheyingkun add end	//文件夹预览图层叠效果
			{
				drawPreviewItem( canvas , mAnimParams.transX , mAnimParams.transY , mAnimParams.scale , mAnimParams.drawable );
			}
			//cheyingkun add start	//文件夹预览图层叠效果
			else
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "FolderIcon" , " LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE configException  " );
			}
			//cheyingkun add end	//文件夹预览图层叠效果
		}
		drawOperateHot( canvas );
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		if(
		//
		ThemeManager.getInstance().currentThemeIsSystemTheme()
		//
		&& LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7
		//
		&& Build.VERSION.SDK_INT < 18
		//
		)
		{
			if( mMask != null )
			{
				canvas.save();
				canvas.translate( mPreviewBackground.getLeft() , mPreviewBackground.getTop() );
				mBgPaint.setXfermode( xfermode );
				canvas.drawBitmap( mMask , 0f , 0f , mBgPaint );
				canvas.restore();
			}
		}
		if( saveLayer != -1 )
			canvas.restoreToCount( saveLayer );
		//zhujieping add end
	}
	
	// zhujieping@2015/03/18 ADD START
	//计算每个icon对应的位置
	private Point getIconPoint(
			int index )
	{
		if( index >= ( mCountX * mCountY ) )
		{
			index = mCountX * mCountY - 1;
		}
		Point point = new Point();
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		{
			float iconWidthSize = ( mIntrinsicIconWidthSize * iconScaleFactor );
			float iconHeightSize = ( mIntrinsicIconHeightSize * iconScaleFactor );
			float left = ( mPreviewBackground.getLayoutParams().width - mCountX * iconWidthSize - ( mCountX - 1 ) * iconMargin ) / 2 + mPerviewIconPaddingLeft;
			float top = ( mPreviewBackground.getLayoutParams().height - mCountY * iconHeightSize - ( mCountY - 1 ) * iconMargin ) / 2 + mPerviewIconPaddingTop;
			if( mPreviewBackground.getWidth() > 0 )
			{
				point.x = mPreviewBackground.getLeft() + left + index % mCountX * ( iconWidthSize + iconMargin );
				point.y = mPreviewBackground.getTop() + top + index / mCountX * ( iconHeightSize + iconMargin );
			}
			else
			{
				point.x = ( mTotalWidth - mPreviewBackground.getLayoutParams().width ) / 2 + left + index % mCountX * ( iconWidthSize + iconMargin );
				point.y = getPaddingTop() + top + index / mCountX * ( iconHeightSize + iconMargin );
			}
		}
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//计算文件夹中的应用，在文件夹预览图中的位置
			float iconWidthSize = ( mTotalWidth * ( 1 - mImageValidRectXPaddingPercent * 2 ) * iconScaleFactor );
			float iconHeightSize = ( mTotalHeight * ( 1 - mImageValidRectYPaddingPercent * 2 ) * iconScaleFactor );
			float left = mTotalWidth * mImageValidRectXPaddingPercent + mPerviewIconPaddingLeftInItemStyle1;
			float top = mTotalHeight * mImageValidRectYPaddingPercent + mPerviewIconPaddingTopInItemStyle1;
			point.x = left + index % mCountX * ( iconWidthSize + mPerviewIconMarginXInItemStyle1 );
			point.y = top + index / mCountX * ( iconHeightSize + mPerviewIconMarginYInItemStyle1 );
		}
		//xiatian add end
		return point;
	}
	
	private void drawPreviewItem(
			Canvas canvas ,
			float x ,
			float y ,
			View items )
	{
		TextView v = (TextView)items;
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		Drawable d = v.getCompoundDrawables()[1];//xiatian del
		Drawable d = ( v instanceof BubbleTextView ) ? ( (BubbleTextView)v ).getIconForPerview() : v.getCompoundDrawables()[1];//xiatian add
		//xiatian end
		drawPreviewItem( canvas , x , y , iconScaleFactor , d );
	}
	
	private void drawPreviewItem(
			Canvas canvas ,
			float x ,
			float y ,
			float scale ,
			Drawable d )
	{
		canvas.save();
		canvas.translate( x , y );
		canvas.scale( scale , scale );
		if( d != null )
		{
			int mBoundsRight = 0;
			int mBoundsBottom = 0;
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				mBoundsRight = mIntrinsicIconWidthSize;
				mBoundsBottom = mIntrinsicIconHeightSize;
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{//Drawable的mBoundsRight和mBoundsBottom要设置为mTotalWidth和mTotalHeight，若设置为mIntrinsicIconWidthSize和mIntrinsicIconHeightSize，在“d.draw( canvas );”之后，d会被改变。
				mBoundsRight = mTotalWidth;
				mBoundsBottom = mTotalHeight;
			}
			//xiatian add end
			d.setBounds( 0 , 0 , mBoundsRight , mBoundsBottom );
			d.setFilterBitmap( true );
			d.setColorFilter( Color.argb( 0 , 0 , 0 , 0 ) , PorterDuff.Mode.SRC_ATOP );
			if( d instanceof BitmapDrawable )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "" , "icon draw:" + ( (BitmapDrawable)d ).getBitmap() );
			}
			d.draw( canvas );
			d.clearColorFilter();
			d.setFilterBitmap( false );
		}
		canvas.restore();
	}
	
	class Point
	{
		
		float x;
		float y;
	}
	
	// zhujieping@2015/03/18 ADD END
	private void animateFirstItem(
			final Drawable d ,
			int duration ,
			final boolean reverse ,
			final Runnable onCompleteRunnable )
	{
		//cheyingkun add start	//文件夹预览图层叠效果
		float scale = 0;
		float transX = 0;
		float transY = 0;
		PreviewItemDrawingParams finalParams = null;
		if(
		//
		( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_KITKAT )
		//
		|| ( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW )
		//
		)
		{
			finalParams = computePreviewItemDrawingParams( 0 , null );
			scale = 1.0f;
			transX = ( mAvailableSpaceInPreview - d.getIntrinsicWidth() ) / 2;
			transY = ( mAvailableSpaceInPreview - d.getIntrinsicHeight() ) / 2 + getPaddingTop();
		}
		else if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS || LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		//cheyingkun add end	//文件夹预览图层叠效果
		{
			// zhujieping@2015/03/19 ADD START
			//文件夹预览改变，缩放比例、位置改变
			Point point = getIconPoint( 0 );
			finalParams = new PreviewItemDrawingParams( point.x , point.y , iconScaleFactor , 0 );
			scale = 1.0f;
			float width = getWidth() > 0 ? getWidth() : mTotalWidth;
			transX = ( width - mIntrinsicIconWidthSize ) / 2;
			transY = getPaddingTop();//xiatian add note	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。//FolderIcon重载方法“getPaddingTop”
			// zhujieping@2015/03/19 ADD END
		}
		//cheyingkun add start	//文件夹预览图层叠效果
		else
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "FolderIcon" , " LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE configException  " );
		}
		final float scale0 = scale;
		final float transX0 = transX;
		final float transY0 = transY;
		final PreviewItemDrawingParams finalParams0 = finalParams;
		//cheyingkun add end	//文件夹预览图层叠效果
		mAnimParams.drawable = d;
		ValueAnimator va = LauncherAnimUtils.ofFloat( this , 0f , 1.0f );
		va.addUpdateListener( new AnimatorUpdateListener() {
			
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				float progress = (Float)animation.getAnimatedValue();
				if( reverse )
				{
					progress = 1 - progress;
					mPreviewBackground.setAlpha( progress );
				}
				mAnimParams.transX = transX0 + progress * ( finalParams0.transX - transX0 );
				mAnimParams.transY = transY0 + progress * ( finalParams0.transY - transY0 );
				mAnimParams.scale = scale0 + progress * ( finalParams0.scale - scale0 );
				invalidate();
			}
		} );
		va.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
				mAnimating = true;
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				mAnimating = false;
				if( onCompleteRunnable != null )
				{
					onCompleteRunnable.run();
				}
			}
		} );
		va.setDuration( duration );
		va.start();
	}
	
	public void setTextVisible(
			boolean visible )
	{
		if( visible )
		{
			mFolderName.setVisibility( VISIBLE );
		}
		else
		{
			mFolderName.setVisibility( INVISIBLE );
		}
	}
	
	public boolean getTextVisible()
	{
		return mFolderName.getVisibility() == VISIBLE;
	}
	
	public void onItemsChanged()
	{
		invalidate();
		requestLayout();
	}
	
	public void onAdd(
			ShortcutInfo item )
	{
		invalidate();
		requestLayout();
	}
	
	public void onRemove(
			ShortcutInfo item )
	{
		//		mInfo.getOpened() = false;
		//		mFolder.onCloseComplete();
		//		mLauncher.closeFolder( mFolder );
		invalidate();
		requestLayout();
	}
	
	public void onTitleChanged(
			CharSequence title )
	{
		mFolderName.setText( title.toString() );
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	@Override
	public void itemIconChange(
			ShortcutInfo shortcutInfo )
	{
		invalidate();
		requestLayout();
	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	
	@Override
	public boolean onTouchEvent(
			MotionEvent event )
	{
		// Call the superclass onTouchEvent first, because sometimes it changes the state to
		// isPressed() on an ACTION_UP
		boolean result = super.onTouchEvent( event );
		switch( event.getAction() )
		{
			case MotionEvent.ACTION_DOWN:
				mLongPressHelper.postCheckForLongPress();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mLongPressHelper.cancelLongPress();
				break;
		}
		return result;
	}
	
	@Override
	public void cancelLongPress()
	{
		super.cancelLongPress();
		mLongPressHelper.cancelLongPress();
	}
	
	private static void initPerviewItemsConfig(
			Context mContext )
	{
		//cheyingkun add start	//文件夹预览图层叠效果
		if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_GRIDS || LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		//cheyingkun add end	//文件夹预览图层叠效果
		{
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				ThemeManager mThemeManager = ThemeManager.getInstance();
				iconScaleFactor = (float)( mThemeManager.getInt( "folder_icon_scale_factor" , 22 ) / 100f );
				mCountX = mThemeManager.getInt( "folder_icon_row_num" , 3 );
				mCountY = mThemeManager.getInt( "folder_transform_num" , 9 ) / mCountX;
				iconMargin = mThemeManager.getInt( "folder_front_margin_offset" , 4 );
				mPerviewIconPaddingLeft = mThemeManager.getInt( "folder_front_padding_left" , 0 );
				mPerviewIconPaddingTop = mThemeManager.getInt( "folder_front_padding_top" , 0 );
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{//关联一些配置
				Resources mResources = mContext.getResources();
				iconScaleFactor = Float.valueOf( LauncherDefaultConfig.getString( R.string.config_item_style_1_folder_preview_icon_scale ) );
				mPerviewIconMarginXInItemStyle1 = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_margin_x );
				mPerviewIconMarginYInItemStyle1 = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_margin_y );
				mCountX = LauncherDefaultConfig.getInt( R.integer.config_item_style_1_folder_preview_max_count_x );
				mCountY = LauncherDefaultConfig.getInt( R.integer.config_item_style_1_folder_preview_max_count_y );
				mImageValidRectXPaddingPercent = Float.valueOf( LauncherDefaultConfig.getString( R.string.config_item_style_1_image_valid_rect_x_padding_percent ) );
				mImageValidRectYPaddingPercent = Float.valueOf( LauncherDefaultConfig.getString( R.string.config_item_style_1_image_valid_rect_y_padding_percent ) );
				mPerviewIconPaddingLeftInItemStyle1 = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_padding_left );
				mPerviewIconPaddingTopInItemStyle1 = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_padding_top );
			}
			//xiatian add end
		}
	}
	
	//	private static void reloadConfig(
	//			Launcher launcher )
	//	{
	//		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
	//		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
	//		{
	//			LauncherAppState app = LauncherAppState.getInstance();
	//			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
	//			iconScaleFactor = (float)( ThemeManager.getInstance().getInt( "folder_icon_scale_factor" , (int)( iconScaleFactor * 100 ) ) / 100f );
	//			mCountX = ThemeManager.getInstance().getInt( "folder_icon_row_num" , mCountX );
	//			mCountY = ThemeManager.getInstance().getInt( "folder_transform_num" , mCountY * mCountX ) / mCountX;
	//			int offset = ThemeManager.getInstance().getInt( "folder_front_margin_offset" , 0 );
	//			iconScaleFactor = ( grid.getFolderIconWidthSizePx() - offset * 2 - iconMargin * ( mCountX - 1 ) ) * 1.0f / mCountX / grid.getIconWidthSizePx();
	//		}
	//		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
	//		{//关联一些配置
	//			Resources mResources = launcher.getResources();
	//			iconScaleFactor = Float.valueOf( mResources.getString( R.string.config_item_style_1_folder_preview_icon_scale ) );
	//			mPerviewIconMarginXInItemStyle1 = mResources.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_margin_x );
	//			mPerviewIconMarginYInItemStyle1 = mResources.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_margin_y );
	//			mCountX = mResources.getInteger( R.integer.config_item_style_1_folder_preview_max_count_x );
	//			mCountY = mResources.getInteger( R.integer.config_item_style_1_folder_preview_max_count_y );
	//			mImageValidRectXPaddingPercent = Float.valueOf( mResources.getString( R.string.config_item_style_1_image_valid_rect_x_padding_percent ) );
	//			mImageValidRectYPaddingPercent = Float.valueOf( mResources.getString( R.string.config_item_style_1_image_valid_rect_y_padding_percent ) );
	//			mPerviewIconPaddingLeftInItemStyle1 = mResources.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_padding_left );
	//			mPerviewIconPaddingTopInItemStyle1 = mResources.getDimensionPixelSize( R.dimen.config_item_style_1_folder_preview_icon_padding_top );
	//		}
	//		//xiatian add end
	//	}
	private static void setFolderIconBg(
			FolderIcon icon )
	{
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			if( ThemeManager.getInstance() != null )
			{
				//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
				if( ThemeManager.getInstance().currentThemeIsSystemTheme() && LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
				{
					icon.mPreviewBackground.setImageDrawable( getAndroid7FolderMaskBg( icon.getContext() , R.drawable.theme_default_folder_icon_mask_bg ) );
				}
				else
					//zhujieping add end
					icon.mPreviewBackground.setImageDrawable( ThemeManager.getInstance().getFolderIconBg() );
			}
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//文件夹设置背景
			icon.mPreviewBackground.setImageDrawable( null );
			// gaominghui@2016/12/14 UPD START 兼容Android 4.0
			//icon.setBackground( ThemeManager.getInstance().getFolderIconBg() );
			icon.setBackgroundDrawable( ThemeManager.getInstance().getFolderIconBg() );
			// gaominghui@2016/12/14 UPD END 兼容Android 4.0
		}
		//xiatian add end
	}
	
	@Override
	public int getPaddingTop()
	{
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//文件夹预览图（一个图标覆盖到另一个图标或者文件夹上会生成文件夹预览图）的图标在绘制时的中心点的y坐标
			return 0;
		}
		//xiatian add end
		return super.getPaddingTop();
	}
	
	public boolean isShowTextInCellLayout(
			boolean mIsHotseatCellLayout )
	{
		boolean ret = true;
		if( mIsHotseatCellLayout )
		{
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				//xiatian add start	//底边栏图标是否显示名称。true为显示名称；false为不显示。默认为false。
				if( LauncherDefaultConfig.SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE )
				{
					ret = true;
				}
				else
				//xiatian add end
				{
					ret = false;
				}
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				ret = true;
			}
		}
		//xiatian add end
		return ret;
	}
	
	public boolean needShowHot(
			FolderInfo folderInfo )
	{
		if( folderInfo.getOperateIntent() != null )
		{
			if( folderInfo.getFolderType() == LauncherSettings.Favorites.FOLDER_TYPE_OPERATE_DYNAMIC && !folderInfo.getOpened() )
				return folderInfo.getOperateIntent().getBooleanExtra( OperateDynamicUtils.DYNAMIC_HOT , false );
		}
		return false;
	}
	
	/**
	 * 绘制运营文件夹的hot图标
	 * @param canvas
	 */
	private void drawOperateHot(
			Canvas canvas )
	{
		if( !Constants.NEW_ICON_DISPLAY_NUM )
		{
			return;
		}
		FolderInfo folderinfo = getFolderInfo();
		if( folderinfo != null && needShowHot( folderinfo ) )//虚链接不显示下载的图标
		{
			//0-2个更新什么都不显示，3-9显示数字。别的直接显示图标
			if( Constants.NEW_ICON_DISPLAY_NUM )
			{
				int newIconCount = folderinfo.getOperateIntent().getIntExtra( Constants.NEW_ICON_COUNT , 0 );
				if( newIconCount < 3 )
				{
					return;
				}
				else if( 3 <= newIconCount && newIconCount < DISPLAY_NFLAG_MAX )
				{
					drawHotBitmap( String.valueOf( newIconCount ) , canvas );
				}
				else
				{
					drawHotBitmap( "N" , canvas );
				}
			}
		}
	}
	
	public void creatHotBackgroundRect()
	{
		if( mHotBackground == null )
		{
			mHotBackground = getResources().getDrawable( R.drawable.icon_and_folder_icon_tip_hot_bg_shape );
			mHotBackground.setBounds( new Rect( 0 , 0 , mHotBackground.getIntrinsicWidth() , mHotBackground.getIntrinsicHeight() ) );
		}
		int width = mHotBackground.getIntrinsicWidth();
		int height = mHotBackground.getIntrinsicHeight();
		int right = getWidth() / 2 + mPreviewBackground.getLayoutParams().width / 2;
		int translateX = (int)( right - width / 2.0f );
		int translateY = (int)( getPaddingTop() - height / 2.0f );
		if( ( translateX + width ) > getWidth() || translateX < 0 )
		{
			translateX = getWidth() - width;
		}
		if( translateY < 0 )
		{
			translateY = 0;
		}
		mHotRect.set( translateX , translateY , width + translateX , height + translateY );
		//		Rect r = new Rect( translateX , translateY , width + translateX , height + translateY );
		//		mHotBackground.setBounds( r );
	}
	
	public void drawHotBitmap(
			String drawText ,
			Canvas canvas )
	{
		if( mPaint == null )
		{
			mPaint = new Paint();
			mPaint.setAntiAlias( true );
			mPaint.setColor( Color.WHITE );
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			final float density = metrics.density;
			mPaint.setTextSize( HOT_TEXT_SIZE * density );
		}
		canvas.save();
		canvas.translate( mHotRect.left , mHotRect.top );
		int textWidth = (int)mPaint.measureText( drawText , 0 , drawText.length() );
		mHotBackground.draw( canvas );
		int x = (int)( ( mHotBackground.getIntrinsicWidth() - textWidth ) / 2.0f );
		FontMetrics fm = mPaint.getFontMetrics();
		int y = (int)( ( mHotBackground.getIntrinsicHeight() - ( fm.descent - fm.ascent ) ) / 2.0f - fm.ascent );
		canvas.drawText( drawText , x , y , mPaint );
		canvas.restore();
	}
	
	@Override
	protected void onSizeChanged(
			int w ,
			int h ,
			int oldw ,
			int oldh )
	{
		// TODO Auto-generated method stub
		super.onSizeChanged( w , h , oldw , oldh );
		creatHotBackgroundRect();
	}
	
	//cheyingkun add start	//文件夹需求(长按显示边框)
	private void initFolderStyle()
	{
		Resources res = getResources();
		int mConfigFolderStyle = LauncherDefaultConfig.getInt( R.integer.config_folder_style );
		if( mConfigFolderStyle == FOLDER_CUSTOM )
		{
			folderStyle = FOLDER_CUSTOM;
		}
		else if( mConfigFolderStyle == FOLDER_FULLSCREEN )
		{
			folderStyle = FOLDER_FULLSCREEN;
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//解决“改变系统字体后，飞利浦图标样式下，文件夹和图标名称偏移”的问题。【c_0003610】
	/**
	 * 根据系统自己大小获取文件夹名称的偏移(飞利浦图标样式中)
	 * @param launcher 
	 * @return 
	 */
	private static int getFolderIconTitleOffsetPercentBySystemFontSize(
			Launcher launcher )
	{
		String gapBetweenIconAndText = "1";
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		if( launcher == null )
		{
			return (int)( grid.getFolderIconHeightSizePx() * -Float.valueOf( gapBetweenIconAndText ) );
		}
		Resources resources = launcher.getResources();
		float fontSize = resources.getConfiguration().fontScale;
		if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_SMALL )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_folder_icon_title_offset_percent_small );
		}
		else if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_NORMAL )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_folder_icon_title_offset_percent_normal );
		}
		else if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_LARGE )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_folder_icon_title_offset_percent_large );
		}
		else if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_HUGE )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_folder_icon_title_offset_percent_huge );
		}
		return (int)( grid.getFolderIconHeightSizePx() * -Float.valueOf( gapBetweenIconAndText ) );
	}
	
	//cheyingkun add end
	//cheyingkun add start	//默认图标样式下,添加图标和文字之间的间距配置【c_0004390】
	private static int getFolderIconTitleOffset(
			Launcher launcher )
	{
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//文件夹名称显示位置
			return getFolderIconTitleOffsetPercentBySystemFontSize( launcher );//调整文字显示的位置//待修改
		}
		//xiatian add end
		else
		{
			//不是飞利浦图标样式,不是自定义布局
			if( !LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{//文件夹名称显示位置
				LauncherAppState app = LauncherAppState.getInstance();
				DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
				return grid.getDefaultGapBetweenIconAndText();
			}
			return 0;
		}
	}
	
	//cheyingkun add end
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
		if( ( arg1 instanceof Handler ) == false )
		{
			return;
		}
		initPerviewItemsConfig( getContext() );
		final ThemeManager mThemeManager = ThemeManager.getInstance();
		//zhujieping add start,解决“换主题后，图标拖动的文件夹下面时，文件夹背景还是使用上一个主题的背景”的问题
		FolderRingAnimator.sPreviewWidthSize = FolderRingAnimator.getPreviewWidthSize();
		FolderRingAnimator.sPreviewHeightSize = FolderRingAnimator.getPreviewHeightSize();
		FolderRingAnimator.sSharedOuterRingDrawable = ThemeManager.getInstance().getFolderIconBg();
		//zhujieping add end
		//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
		if( LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
		{
			if( Build.VERSION.SDK_INT < 18 && ThemeManager.getInstance().currentThemeIsSystemTheme() )
			{
				if( mMask == null || mMask.isRecycled() )
					mMask = Tools.drawableToBitmap( getResources().getDrawable( R.drawable.theme_default_folder_icon_mask ) , Utilities.sIconWidth , Utilities.sIconWidth );
			}
			else
			{
				if( mMask != null && mMask.isRecycled() )
				{
					mMask.recycle();
				}
			}
		}
		//zhujieping add end
		if( mThemeManager != null )
		{//文件夹背板
			Runnable r = new Runnable() {
				
				public void run()
				{
					DeviceProfile grid = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mPreviewBackground.getLayoutParams();
					lp.topMargin = grid.getFolderBackgroundOffset();
					lp.width = grid.getFolderIconWidthSizePx();
					lp.height = grid.getFolderIconHeightSizePx();
					//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
					if( ThemeManager.getInstance().currentThemeIsSystemTheme() && LauncherDefaultConfig.CONFIG_FOLDER_ICON_PREVIEW_STYLE == LauncherDefaultConfig.FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 )
					{
						if( Build.VERSION.SDK_INT < 18 )
						{
							mPreviewBackground.setImageDrawable( getAndroid7FolderMaskBg( getContext() , R.drawable.theme_default_folder_icon_mask_bg ) );
							mPreviewBackground.setVisibility( View.VISIBLE );
						}
						else
						{
							mPreviewBackground.setImageDrawable( mThemeManager.getFolderIconBg() );
							mBackground.setup( getResources().getDisplayMetrics() , grid , FolderIcon.this , mTotalWidth , getPaddingTop() );
							mPreviewBackground.setVisibility( View.INVISIBLE );
						}
					}
					else
					//zhujieping end
					{
						mPreviewBackground.setImageDrawable( mThemeManager.getFolderIconBg() );
						mPreviewBackground.setVisibility( View.VISIBLE );
					}
				}
			};
			post( r );
		}
		if( mFolder != null )
		{//items
			mFolder.onThemeChanged( arg0 , arg1 );
		}
		postInvalidate();
	}
	
	//zhujieping add start //需求：拓展配置项“config_folder_icon_preview_style”，添加可配置项2。2为“安卓7.1”样式。
	public static Drawable getAndroid7FolderMaskBg(
			Context context ,
			int drawable_id )
	{
		Drawable drawable = context.getResources().getDrawable( drawable_id );
		return new BitmapDrawable( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( drawable , context , true ) );
	}
	
	public void setFolderBackground(
			PreviewBackground bg )
	{
		mBackground = bg;
		mBackground.setInvalidateDelegate( this );
	}
	
	/**
	     * This object represents a FolderIcon preview background. It stores drawing / measurement
	     * information, handles drawing, and animation (accept state <--> rest state).
	     */
	public static class PreviewBackground
	{
		
		private float mScale = 1f;
		private float mColorMultiplier = 1f;
		private Path mClipPath = new Path();
		private int mStrokeWidth;
		private View mInvalidateDelegate;
		public int previewSize;
		private int basePreviewOffsetX;
		private int basePreviewOffsetY;
		private CellLayout mDrawingDelegate;
		public int delegateCellX;
		public int delegateCellY;
		// When the PreviewBackground is drawn under an icon (for creating a folder) the border
		// should not occlude the icon
		public boolean isClipping = true;
		// Drawing / animation configurations
		private static final float ACCEPT_SCALE_FACTOR = 1.25f;
		private static final float ACCEPT_COLOR_MULTIPLIER = 1.5f;
		// Expressed on a scale from 0 to 255.
		private static final int BG_OPACITY = 165;
		private static final int MAX_BG_OPACITY = 165;
		private static final int BG_INTENSITY = 255;
		private static final int SHADOW_OPACITY = 80;
		ValueAnimator mScaleAnimator;
		
		public void setup(
				DisplayMetrics dm ,
				DeviceProfile grid ,
				View invalidateDelegate ,
				int availableSpace ,
				int topPadding )
		{
			mInvalidateDelegate = invalidateDelegate;
			this.previewSize = grid.getFolderIconWidthSizePx();
			basePreviewOffsetX = ( availableSpace - this.previewSize ) / 2;
			basePreviewOffsetY = grid.getFolderBackgroundOffset() + topPadding;
			mStrokeWidth = Utilities.pxFromDp( 1 , dm );
			invalidate();
		}
		
		int getRadius()
		{
			return previewSize / 2;
		}
		
		int getScaledRadius()
		{
			return (int)( mScale * getRadius() );
		}
		
		int getOffsetX()
		{
			return basePreviewOffsetX - ( getScaledRadius() - getRadius() );
		}
		
		int getOffsetY()
		{
			return basePreviewOffsetY - ( getScaledRadius() - getRadius() );
		}
		
		void invalidate()
		{
			int radius = getScaledRadius();
			mClipPath.reset();
			mClipPath.addCircle( radius , radius , radius , Path.Direction.CW );
			if( mInvalidateDelegate != null )
			{
				mInvalidateDelegate.invalidate();
			}
			if( mDrawingDelegate != null )
			{
				mDrawingDelegate.invalidate();
			}
		}
		
		void setInvalidateDelegate(
				View invalidateDelegate )
		{
			mInvalidateDelegate = invalidateDelegate;
			invalidate();
		}
		
		public void drawBackground(
				Canvas canvas ,
				Paint paint )
		{
			canvas.save();
			canvas.translate( getOffsetX() , getOffsetY() );
			paint.reset();
			paint.setStyle( Paint.Style.FILL );
			paint.setXfermode( null );
			paint.setAntiAlias( true );
			int alpha = (int)Math.min( MAX_BG_OPACITY , BG_OPACITY * mColorMultiplier );
			paint.setColor( Color.argb( alpha , BG_INTENSITY , BG_INTENSITY , BG_INTENSITY ) );
			float radius = getScaledRadius();
			canvas.drawCircle( radius , radius , radius , paint );
			canvas.clipPath( mClipPath , Region.Op.DIFFERENCE );
			paint.setStyle( Paint.Style.STROKE );
			paint.setColor( Color.TRANSPARENT );
			paint.setShadowLayer( mStrokeWidth , 0 , mStrokeWidth , Color.argb( SHADOW_OPACITY , 0 , 0 , 0 ) );
			canvas.drawCircle( radius , radius , radius , paint );
			canvas.restore();
		}
		
		// It is the callers responsibility to save and restore the canvas.
		private void clipCanvas(
				Canvas canvas )
		{
			canvas.translate( getOffsetX() , getOffsetY() );
			canvas.clipPath( mClipPath );
			canvas.translate( -getOffsetX() , -getOffsetY() );
		}
		
		private void delegateDrawing(
				CellLayout delegate ,
				int cellX ,
				int cellY )
		{
			if( mDrawingDelegate != delegate )
			{
				delegate.addFolderBackground( this );
			}
			mDrawingDelegate = delegate;
			delegateCellX = cellX;
			delegateCellY = cellY;
			invalidate();
		}
		
		private void clearDrawingDelegate()
		{
			if( mDrawingDelegate != null )
			{
				mDrawingDelegate.removeFolderBackground( this );
			}
			mDrawingDelegate = null;
			invalidate();
		}
		
		private boolean drawingDelegated()
		{
			return mDrawingDelegate != null;
		}
		
		private void animateScale(
				float finalScale ,
				float finalMultiplier ,
				final Runnable onStart ,
				final Runnable onEnd )
		{
			final float scale0 = mScale;
			final float scale1 = finalScale;
			final float bgMultiplier0 = mColorMultiplier;
			final float bgMultiplier1 = finalMultiplier;
			if( mScaleAnimator != null )
			{
				mScaleAnimator.cancel();
			}
			mScaleAnimator = LauncherAnimUtils.ofFloat( null , 0f , 1.0f );
			mScaleAnimator.addUpdateListener( new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					float prog = animation.getAnimatedFraction();
					mScale = prog * scale1 + ( 1 - prog ) * scale0;
					mColorMultiplier = prog * bgMultiplier1 + ( 1 - prog ) * bgMultiplier0;
					invalidate();
				}
			} );
			mScaleAnimator.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationStart(
						Animator animation )
				{
					if( onStart != null )
					{
						onStart.run();
					}
				}
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					if( onEnd != null )
					{
						onEnd.run();
					}
					mScaleAnimator = null;
				}
			} );
			mScaleAnimator.setDuration( CONSUMPTION_ANIMATION_DURATION );
			mScaleAnimator.start();
		}
		
		public void animateToAccept(
				final CellLayout cl ,
				final int cellX ,
				final int cellY )
		{
			Runnable onStart = new Runnable() {
				
				@Override
				public void run()
				{
					delegateDrawing( cl , cellX , cellY );
				}
			};
			animateScale( ACCEPT_SCALE_FACTOR , ACCEPT_COLOR_MULTIPLIER , onStart , null );
		}
		
		public void animateToRest()
		{
			// This can be called multiple times -- we need to make sure the drawing delegate
			// is saved and restored at the beginning of the animation, since cancelling the
			// existing animation can clear the delgate.
			final CellLayout cl = mDrawingDelegate;
			final int cellX = delegateCellX;
			final int cellY = delegateCellY;
			Runnable onStart = new Runnable() {
				
				@Override
				public void run()
				{
					delegateDrawing( cl , cellX , cellY );
				}
			};
			Runnable onEnd = new Runnable() {
				
				@Override
				public void run()
				{
					clearDrawingDelegate();
				}
			};
			animateScale( 1f , 1f , onStart , onEnd );
		}
	}
	//zhujieping add end
}
