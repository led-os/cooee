package com.cooee.phenix.Folder;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.DragLayer;
import com.cooee.phenix.LauncherAnimUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ShortcutInfo;


public class FolderFullScreen extends Folder
{
	
	private final int mMaxShowConutX = 3;
	private final int mMaxShowConutY = 3;//celllayout显示的最大高度为3行的高
	private int mGapX;
	private int mGapY;
	private int mMarginLeft;
	private int mMarginRight;
	//cheyingkun add start	//文件夹需求(长按显示边框)
	private ImageView folderBorder;
	//cheyingkun add end
	private FoloderFuzzyBackGround mFuzzyBackground;
	
	/**
	 * Used to inflate the Workspace from XML.
	 *
	 * @param context The application's context.
	 * @param attrs The attribtues set containing the Workspace's customization values.
	 */
	public FolderFullScreen(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		//xiatian start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
		//xiatian del end
		//		mMaxCountX = mMaxShowConutX;
		//		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.FolderFullScreen );
		//		mGapX = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mGapX , 0 );
		//		mGapY = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mGapY , 0 );
		//		mMarginLeft = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mMarginLeft , 0 );
		//		mMarginRight = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mMarginRight , 0 );
		//		mMaxNumItems = a.getInteger( R.styleable.FolderFullScreen_mNums , 999 );
		//		mMaxCountY = mMaxNumItems / mMaxCountX;
		//xiatian del end
		initConfigs( context , attrs );//xiatian add
		//xiatian end
	}
	
	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		//xiatian add start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//文件夹打开状态下，文件夹名称的上下边距。
			Resources mResources = mLauncher.getResources();
			int mPaddingLeft = mFolderName.getPaddingLeft();
			int mPaddingTop = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folder_name_padding );
			int mPaddingRight = mFolderName.getPaddingRight();
			int mPaddingBottom = mPaddingTop;
			mFolderName.setPadding( mPaddingLeft , mPaddingTop , mPaddingRight , mPaddingBottom );
		}
		//xiatian add end
		//cheyingkun add start	//文件夹需求(长按显示边框)
		folderBorder = (ImageView)findViewById( R.id.folder_border );
		//cheyingkun add end
		mContent.setContentGap( mGapX , mGapY );
	}
	
	/**
	 * Creates a new UserFolder, inflated from R.layout.user_folder_fullscreen.
	 *
	 * @param context The application's context.
	 *
	 * @return A new UserFolder.
	 */
	static FolderFullScreen fromXml(
			Context context )
	{
		return (FolderFullScreen)LayoutInflater.from( context ).inflate( R.layout.user_folder_fullscreen , null );
	}
	
	private int getContentAreaHeight()//scrollview的高度最大为三行celllayout的高度
	{
		int countY = mContent.getCountY();
		if( countY > mMaxShowConutY )
		{
			countY = mMaxShowConutY;
		}
		return mContent.getDesiredHeight( countY );
	}
	
	@Override
	protected boolean createAndAddShortcut(
			ShortcutInfo item ,
			int color )
	{
		// TODO Auto-generated method stub
		return super.createAndAddShortcut( item , R.color.folderfullscreen_items_text_color );
	}
	
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		//		int width = getPaddingLeft() + getPaddingRight() + mContent.getDesiredWidth();
		//		int height = getFolderHeight();
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		int width = grid.getAvailableWidthPx();
		int height = grid.getHeightPx();
		int contentAreaWidthSpec = MeasureSpec.makeMeasureSpec( mContent.getDesiredWidth() + mMarginLeft + mMarginRight , MeasureSpec.EXACTLY );
		int contentAreaHeightSpec = MeasureSpec.makeMeasureSpec( getContentAreaHeight() , MeasureSpec.EXACTLY );
		mContent.setFixedSize( mContent.getDesiredWidth() , mContent.getDesiredHeight() );
		mScrollView.measure( contentAreaWidthSpec , contentAreaHeightSpec );
		//cheyingkun add start	//文件夹需求(长按显示边框)
		folderBorder.measure( contentAreaWidthSpec , contentAreaHeightSpec );
		//cheyingkun add end
		mFolderName.measure( MeasureSpec.makeMeasureSpec( mContent.getDesiredWidth( mMaxShowConutX ) , MeasureSpec.EXACTLY ) , MeasureSpec.makeMeasureSpec( mFolderNameHeight , MeasureSpec.EXACTLY ) );
		if( dynamicView != null )
		{
			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( width , View.MeasureSpec.EXACTLY );
			int childheightMeasureSpec = MeasureSpec.makeMeasureSpec( height , View.MeasureSpec.EXACTLY );
			dynamicView.measure( childWidthMeasureSpec , childheightMeasureSpec );
		}
		//cheyingkun add start	//文件夹推荐应用
		if( mNativeAdverViewLayout != null )
		{
			//cheyingkun add start	//解决“文件夹推荐应用名称显示不全”的问题。【i_0013225】
			Resources resources = getContext().getResources();
			//title高度
			int titleHeight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.native_adver_title_layout_height );
			//间距
			int nativeAdverIconLayoutMarginTop = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.native_adver_icon_layout_margin_top );
			int nativeAdverIconLayoutMarginBottom = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.native_adver_icon_layout_margin_bottom );
			//图标高度(cell高度-间隙)
			int iconHeight = grid.getCellHeightPx() - grid.getItemPaddingTopInCell() - grid.getItemPaddingBottomInCell();
			//加起来算出layout高度
			int layoutHeight = titleHeight + nativeAdverIconLayoutMarginTop + nativeAdverIconLayoutMarginBottom + iconHeight;
			//cheyingkun add end
			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( width , View.MeasureSpec.EXACTLY );
			int childheightMeasureSpec = MeasureSpec.makeMeasureSpec( layoutHeight , View.MeasureSpec.EXACTLY );
			mNativeAdverViewLayout.measure( childWidthMeasureSpec , childheightMeasureSpec );
		}
		//cheyingkun add end	//文件夹推荐应用
		setMeasuredDimension( width , height );
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int l ,
			int t ,
			int r ,
			int b )
	{
		//设置文件夹布局的上边距
		int top = ( getHeight() - getContentAreaHeight() ) / 2 - mFolderNameHeight;
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mFolderName.getLayoutParams();
		params.topMargin = top;
		//cheyingkun add start	//文件夹需求(长按显示边框)
		params = (FrameLayout.LayoutParams)mScrollView.getLayoutParams();
		params.topMargin = top + mFolderNameHeight;
		params = (FrameLayout.LayoutParams)folderBorder.getLayoutParams();
		params.topMargin = top + mFolderNameHeight;
		//cheyingkun add end
		super.onLayout( changed , l , t , r , b );
	}
	
	public void startEditingFolderName()
	{
		super.startEditingFolderName();
		mInputMethodManager.showSoftInput( mFolderName , 0 );
	}
	
	@Override
	public void getHitRectRelativeToDragLayer(
			Rect outRect )
	{
		//这个是设置文件夹icon拖动不被拉出去的范围
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		outRect.left = mScrollView.getLeft();
		outRect.top = mFolderName.getTop();
		outRect.right = mScrollView.getRight();
		outRect.bottom = mScrollView.getBottom() + grid.getIconHeightSizePx();
	}
	
	protected void centerAboutIcon()
	{
		DragLayer.LayoutParams lp = (DragLayer.LayoutParams)getLayoutParams();
		DragLayer parent = (DragLayer)mLauncher.findViewById( R.id.drag_layer );
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		int width = grid.getAvailableWidthPx();
		int height = grid.getHeightPx();
		float scale = parent.getDescendantRectRelativeToSelf( mFolderIcon , mTempRect );
		int centerX = (int)( mTempRect.left + mTempRect.width() * scale / 2 );
		int centerY = (int)( mTempRect.top + mTempRect.height() * scale / 2 );
		int centeredLeft = centerX - width / 2;
		int centeredTop = centerY - height / 2;
		int folderPivotX = width / 2 + centeredLeft;
		int folderPivotY = height / 2 + centeredTop;
		mScrollView.setPivotX( folderPivotX );
		mScrollView.setPivotY( folderPivotY );
		mFolderName.setPivotX( folderPivotX );
		mFolderName.setPivotY( folderPivotY );
		mFolderIconPivotX = (int)( mFolderIcon.getMeasuredWidth() * ( 1.0f * folderPivotX / width ) );
		mFolderIconPivotY = (int)( mFolderIcon.getMeasuredHeight() * ( 1.0f * folderPivotY / height ) );
		lp.width = width;
		lp.height = height;
		lp.x = 0;
		lp.y = 0;
	}
	
	public void getDescendantRectRelativeToSelf(
			Rect hitRect )
	{
		//这个是设置文件夹按下不退出的区域
		if( hitRect == null )
		{
			hitRect = new Rect();
		}
		if( dynamicView != null && dynamicView.getVisibility() == View.VISIBLE )//有文件夹通知或者搭配销售，点击文件夹区域不关闭文件夹
		{
			hitRect.set( getLeft() , getTop() , getRight() , getBottom() );
		}
		else
		{
			hitRect.top = mFolderName.getTop();
			hitRect.left = mScrollView.getLeft() < mFolderName.getLeft() ? mScrollView.getLeft() : mFolderName.getLeft();
			hitRect.right = mScrollView.getRight() > mFolderName.getRight() ? mScrollView.getRight() : mFolderName.getRight();
			hitRect.bottom = mScrollView.getBottom();
		}
	}
	
	//cheyingkun add start	//文件夹推荐应用
	/**广告区域点击响应判断*/
	public void getDescendantRectRelativeToSelfMore(
			Rect hitRect )
	{
		if( mNativeAdverViewLayout != null )//有文件夹运营，点击文件夹区域不关闭文件夹
		{
			hitRect.set( mNativeAdverViewLayout.getLeft() , mNativeAdverViewLayout.getTop() , mNativeAdverViewLayout.getRight() , mNativeAdverViewLayout.getBottom() );
		}
	}
	
	//cheyingkun add end	//文件夹推荐应用
	protected void positionAndSizeAsIcon()
	{
		setScaleX( 1f );
		setScaleY( 1f );
		setAlpha( 1f );
		setState( STATE_SMALL );
		mScrollView.setScaleX( 0.8f );
		mScrollView.setScaleY( 0.8f );
		mScrollView.setAlpha( 0 );
		mFolderName.setScaleX( 0.8f );
		mFolderName.setScaleY( 0.8f );
		mFolderName.setAlpha( 0 );
		//cheyingkun add start	//文件夹推荐应用
		if( mNativeAdverViewLayout != null && !isGetDataForNativeAdver )
		{
			mNativeAdverViewLayout.setScaleX( 0.8f );
			mNativeAdverViewLayout.setScaleY( 0.8f );
			mNativeAdverViewLayout.setAlpha( 0 );
		}
		//cheyingkun add end	//文件夹推荐应用
	}
	
	@Override
	public void animateOpen()
	{
		if( !( getParent() instanceof DragLayer ) )
			return;
		showNativeAdverDialog();//cheyingkun add	//文件夹推荐应用
		//cheyingkun add start	//文件夹需求(文件夹模糊背景)
		initFuzzyBackground();
		mFuzzyBackground.showFuzzyBackgroundAnimation();
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			mLauncher.getSearchBar().setVisibility( View.GONE );//文件夹内显示垃圾筐
		}
		//cheyingkun add end
		//添加智能分类功能 , change by shlt@2015/02/11 ADD START
		addMoreAppView();
		//添加智能分类功能 , change by shlt@2015/02/11 ADD END
		positionAndSizeAsIcon();
		centerAboutIcon();
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 1 );
		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat( "scaleX" , 1.0f );
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat( "scaleY" , 1.0f );
		ObjectAnimator oas = LauncherAnimUtils.ofPropertyValuesHolder( mScrollView , alpha , scaleX , scaleY );
		ObjectAnimator oaf = LauncherAnimUtils.ofPropertyValuesHolder( mFolderName , alpha , scaleX , scaleY );
		AnimatorSet s = new AnimatorSet();
		//cheyingkun add start	//文件夹推荐应用
		ObjectAnimator oan = null;
		if( mNativeAdverViewLayout != null && !isGetDataForNativeAdver )
		{
			oan = LauncherAnimUtils.ofPropertyValuesHolder( mNativeAdverViewLayout , alpha , scaleX , scaleY );
		}
		if( oan != null )
		{
			s.playTogether( oas , oaf , oan );
		}
		else
		//cheyingkun add end	//文件夹推荐应用
		{
			s.playTogether( oas , oaf );
		}
		s.addListener( mOpenAnimatorListener );
		s.setDuration( mExpandDuration );
		setLayerType( LAYER_TYPE_HARDWARE , null );
		s.start();
	}
	
	@Override
	public void animateClosed()
	{
		// TODO Auto-generated method stub
		if( !( getParent() instanceof DragLayer ) )
			return;
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 0 );
		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat( "scaleX" , 0.8f );
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat( "scaleY" , 0.8f );
		ObjectAnimator oas = LauncherAnimUtils.ofPropertyValuesHolder( mScrollView , alpha , scaleX , scaleY );
		ObjectAnimator oaf = LauncherAnimUtils.ofPropertyValuesHolder( mFolderName , alpha , scaleX , scaleY );
		AnimatorSet s = new AnimatorSet();
		//cheyingkun add start	//文件夹推荐应用
		ObjectAnimator oan = null;
		if( mNativeAdverViewLayout != null && !isGetDataForNativeAdver )
		{
			oan = LauncherAnimUtils.ofPropertyValuesHolder( mNativeAdverViewLayout , alpha , scaleX , scaleY );
		}
		if( oan != null )
		{
			s.playTogether( oas , oaf , oan );
		}
		else
		//cheyingkun add end	//文件夹推荐应用
		{
			s.playTogether( oas , oaf );
		}
		s.addListener( mCloseAnimatorListener );
		s.setDuration( mExpandDuration );
		setLayerType( LAYER_TYPE_HARDWARE , null );
		s.start();
		//cheyingkun add start	//文件夹需求(文件夹模糊背景)
		if( mFuzzyBackground != null )
		{
			mFuzzyBackground.disappearFuzzyBackgroundAnimation();
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			mLauncher.getSearchBar().setVisibility( View.VISIBLE );//文件夹内显示垃圾筐
		}
		//cheyingkun add end
	}
	
	//cheyingkun add start	//文件夹需求(长按显示边框)
	@Override
	public void onDragOver(
			DragObject d )
	{
		folderBorder.setVisibility( View.VISIBLE );
		super.onDragOver( d );
	}
	
	@Override
	public void onDragExit(
			DragObject d )
	{
		folderBorder.setVisibility( View.GONE );
		super.onDragExit( d );
	}
	
	@Override
	public void closeWithoutAnim()
	{
		if( !( getParent() instanceof DragLayer ) )
			return;
		//文件夹需求(文件夹模糊背景)
		if( mFuzzyBackground != null )
		{
			mFuzzyBackground.disappearFuzzyBackgroundWithoutAnimation();
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			//文件夹需求(文件夹模糊背景)
			mLauncher.getSearchBar().setVisibility( View.VISIBLE );//文件夹内显示垃圾筐
		}
		super.closeWithoutAnim();
	}
	
	//文件夹需求(文件夹模糊背景)
	private void initFuzzyBackground()
	{
		DragLayer dragLayer = (DragLayer)getParent();
		if( mFuzzyBackground == null )
		{
			mFuzzyBackground = new FoloderFuzzyBackGround( mLauncher , 10 , LauncherDefaultConfig.getInt( R.integer.config_folderAnimDuration ) );
		}
		DragLayer.LayoutParams lp;
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		lp = new DragLayer.LayoutParams( grid.getAvailableWidthPx() , grid.getHeightPx() );
		if( dragLayer.indexOfChild( mFuzzyBackground ) != -1 )
		{
			dragLayer.removeView( mFuzzyBackground );
		}
		dragLayer.addView( mFuzzyBackground , lp );
		mFuzzyBackground.setBackground();
	}
	//文件夹需求(文件夹模糊背景)
	//cheyingkun add end
	;
	
	private void initConfigs(
			Context context ,
			AttributeSet attrs )
	{
		mMaxCountX = mMaxShowConutX;
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.FolderFullScreen );
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			//文件夹打开状态下，图标的间距
			mGapX = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mGapX , 0 );
			mGapY = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mGapY , 0 );
			//文件夹打开状态下，celllayout距scrollview的左右边距
			mMarginLeft = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mMarginLeft , 0 );
			mMarginRight = a.getDimensionPixelSize( R.styleable.FolderFullScreen_mMarginRight , 0 );
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			Resources mResources = context.getResources();
			//文件夹打开状态下，图标的间距
			mGapX = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folderfullscreen_cell_gapx );
			mGapY = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folderfullscreen_cell_gapy );
			//文件夹打开状态下，celllayout距scrollview的左右边距
			mMarginLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folderfullscreen_marginleft );
			mMarginRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_folderfullscreen_marginright );
		}
		mMaxNumItems = a.getInteger( R.styleable.FolderFullScreen_mNums , 999 );
		mMaxCountY = mMaxNumItems / mMaxCountX;
	}
}
