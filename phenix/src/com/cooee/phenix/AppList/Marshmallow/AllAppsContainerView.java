package com.cooee.phenix.AppList.Marshmallow;


import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.BaseContainerView;
import com.cooee.phenix.CellLayout;
import com.cooee.phenix.DeleteDropTarget;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.DragSource;
import com.cooee.phenix.DropTarget;
import com.cooee.phenix.ILauncherTransitionable;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Workspace;
import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.AppList.KitKat.AppsCustomizeTabHost;
import com.cooee.phenix.AppList.KitKat.AppsView;
import com.cooee.phenix.AppList.Nougat.favorites.FavoritesAppManager;
import com.cooee.phenix.AppList.Nougat.favorites.FavoritesReceiver;
import com.cooee.phenix.AppList.Nougat.favorites.MonitorThread;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.WorkspaceMenu.WorkspaceMenuVerticalList;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.util.ComponentKey;
import com.cooee.phenix.util.Thunk;


/**
 * A merge algorithm that merges every section indiscriminately.
 */
final class FullMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm
{
	
	@Override
	public boolean continueMerging(
			AlphabeticalAppsList.SectionInfo section ,
			AlphabeticalAppsList.SectionInfo withSection ,
			int sectionAppCount ,
			int numAppsPerRow ,
			int mergeCount )
	{
		// Don't merge the predicted apps
		if( section.firstAppItem.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE )
		{
			return false;
		}
		// Otherwise, merge every other section
		return true;
	}
}

/**
 * The logic we use to merge multiple sections.  We only merge sections when their final row
 * contains less than a certain number of icons, and stop at a specified max number of merges.
 * In addition, we will try and not merge sections that identify apps from different scripts.
 */
final class SimpleSectionMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm
{
	
	private int mMinAppsPerRow;
	private int mMinRowsInMergedSection;
	private int mMaxAllowableMerges;
	private CharsetEncoder mAsciiEncoder;
	
	public SimpleSectionMergeAlgorithm(
			int minAppsPerRow ,
			int minRowsInMergedSection ,
			int maxNumMerges )
	{
		mMinAppsPerRow = minAppsPerRow;
		mMinRowsInMergedSection = minRowsInMergedSection;
		mMaxAllowableMerges = maxNumMerges;
		mAsciiEncoder = Charset.forName( "US-ASCII" ).newEncoder();
	}
	
	@Override
	public boolean continueMerging(
			AlphabeticalAppsList.SectionInfo section ,
			AlphabeticalAppsList.SectionInfo withSection ,
			int sectionAppCount ,
			int numAppsPerRow ,
			int mergeCount )
	{
		// Don't merge the predicted apps
		if( section.firstAppItem.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE )
		{
			return false;
		}
		// Continue merging if the number of hanging apps on the final row is less than some
		// fixed number (ragged), the merged rows has yet to exceed some minimum row count,
		// and while the number of merged sections is less than some fixed number of merges
		int rows = sectionAppCount / numAppsPerRow;
		int cols = sectionAppCount % numAppsPerRow;
		// Ensure that we do not merge across scripts, currently we only allow for english and
		// native scripts so we can test if both can just be ascii encoded
		boolean isCrossScript = false;
		if( section.firstAppItem != null && withSection.firstAppItem != null )
		{
			isCrossScript = mAsciiEncoder.canEncode( section.firstAppItem.sectionName ) != mAsciiEncoder.canEncode( withSection.firstAppItem.sectionName );
		}
		return ( 0 < cols && cols < mMinAppsPerRow ) && rows < mMinRowsInMergedSection && mergeCount < mMaxAllowableMerges && !isCrossScript;
	}
}

/**
 * The all apps view container.
 */
public class AllAppsContainerView extends BaseContainerView implements DragSource , ILauncherTransitionable , View.OnTouchListener , View.OnLongClickListener , AllAppsSearchBarController.Callbacks , View.OnClickListener
//
, IOnThemeChanged//换主题不重启
, AppsView//zhujieping add //7.0进入主菜单动画改成也支持4.4主菜单样式
{
	
	protected static final int MIN_ROWS_IN_MERGED_SECTION_PHONE = 3;
	protected static final int MAX_NUM_MERGES_PHONE = 2;
	@Thunk
	protected Launcher mLauncher;
	@Thunk
	protected AlphabeticalAppsList mApps;
	protected AllAppsGridAdapter mAdapter;
	protected RecyclerView.LayoutManager mLayoutManager;
	protected RecyclerView.ItemDecoration mItemDecoration;
	@Thunk
	protected View mContent;
	@Thunk
	protected View mContainerView;
	@Thunk
	protected View mRevealView;
	@Thunk
	protected AllAppsRecyclerView mAppsRecyclerView;
	@Thunk
	AllAppsSearchBarController mSearchBarController;
	private ViewGroup mSearchBarContainerView;
	private View mSearchBarView;
	private SpannableStringBuilder mSearchQueryBuilder = null;
	protected int mSectionNamesMargin;
	private int mNumAppsPerRow;
	private int mNumPredictedAppsPerRow;
	private int mRecyclerViewTopBottomPadding;
	// This coordinate is relative to this container view
	private final Point mBoundsCheckLastTouchDownPos = new Point( -1 , -1 );
	// This coordinate is relative to its parent
	private final Point mIconLastTouchPos = new Point();
	private View.OnClickListener mSearchClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(
				View v )
		{
			Intent searchIntent = (Intent)v.getTag();
			mLauncher.startActivitySafely( v , searchIntent , null );
		}
	};
	private boolean mInTransition;
	
	public AllAppsContainerView(
			Context context )
	{
		this( context , null );
	}
	
	public AllAppsContainerView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public AllAppsContainerView(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
		Resources res = context.getResources();
		mLauncher = (Launcher)context;
		mSectionNamesMargin = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_grid_view_start_margin );
		mApps = new AlphabeticalAppsList( context );
		mAdapter = getGridAdapter();
		mApps.setAdapter( mAdapter );
		mLayoutManager = mAdapter.getLayoutManager();
		mItemDecoration = mAdapter.getItemDecoration();
		mRecyclerViewTopBottomPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_list_top_bottom_padding );
		mSearchQueryBuilder = new SpannableStringBuilder();
		Selection.setSelection( mSearchQueryBuilder , 0 );
	}
	
	public AllAppsGridAdapter getGridAdapter()
	{
		return new AllAppsGridAdapter( mLauncher , mApps , this , this , this );
	}
	
	/**
	 * Sets the current set of predicted apps.
	 */
	public void setPredictedApps(
			List<ComponentKey> apps )
	{
		mApps.setPredictedApps( apps );
	}
	
	/**
	 * Sets the current set of apps.
	 */
	public void setApps(
			List<AppInfo> apps )
	{
		mApps.setApps( apps );
		//zhujieping add start //6.0主菜单显示常用应用并显示分隔线
		if( LauncherDefaultConfig.SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS )
		{
			FavoritesAppManager.getInstance().loadAndBindApps( getContext() , mApps );
			if( ( Build.VERSION.SDK_INT < 21 || FavoritesReceiver.isSystemApp( getContext() ) ) )
			{
				IntentFilter filter = new IntentFilter();
				filter.addAction( Intent.ACTION_TIME_TICK );
				filter.addAction( Intent.ACTION_TIME_CHANGED );
				filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
				filter.addAction( Intent.ACTION_SCREEN_ON );
				filter.addAction( Intent.ACTION_SCREEN_OFF );
				FavoritesReceiver receiver = new FavoritesReceiver( getContext() );
				getContext().registerReceiver( receiver , filter );
				new MonitorThread( getContext() ).start();
			}
		}
		//zhujieping add end
	}
	
	/**
	 * Adds new apps to the list.
	 */
	public void addApps(
			List<AppInfo> apps )
	{
		mApps.addApps( apps );
		mAdapter.notifyAppsChanged();//xiatian add	//解决“关于安卓6.0主菜单搜索栏进行搜索后，显示的“进入应用市场搜索”按钮。启动桌面后：（1）之前没有支持应用市场的apk前提下，安装支持应用市场的apk后，按钮不显示；（2）卸载唯一的支持应用市场的apk后，按钮不隐藏”的问题。
	}
	
	/**
	 * Updates existing apps in the list
	 */
	public void updateApps(
			List<AppInfo> apps )
	{
		mApps.updateApps( apps );
		mAdapter.notifyAppsChanged();//xiatian add	//解决“关于安卓6.0主菜单搜索栏进行搜索后，显示的“进入应用市场搜索”按钮。启动桌面后：（1）之前没有支持应用市场的apk前提下，安装支持应用市场的apk后，按钮不显示；（2）卸载唯一的支持应用市场的apk后，按钮不隐藏”的问题。
	}
	
	/**
	 * Removes some apps from the list.
	 */
	public void removeApps(
			List<AppInfo> apps )
	{
		mApps.removeApps( apps );
		mAdapter.notifyAppsChanged();//xiatian add	//解决“关于安卓6.0主菜单搜索栏进行搜索后，显示的“进入应用市场搜索”按钮。启动桌面后：（1）之前没有支持应用市场的apk前提下，安装支持应用市场的apk后，按钮不显示；（2）卸载唯一的支持应用市场的apk后，按钮不隐藏”的问题。
	}
	
	/**
	 * Sets the search bar that shows above the a-z list.
	 */
	public void setSearchBarController(
			AllAppsSearchBarController searchController )
	{
		if( mSearchBarController != null )
		{
			throw new RuntimeException( "Expected search bar controller to only be set once" );
		}
		mSearchBarController = searchController;
		mSearchBarController.initialize( mApps , this );
		// Add the new search view to the layout
		View searchBarView = searchController.getView( mSearchBarContainerView );
		mSearchBarContainerView.addView( searchBarView );
		mSearchBarContainerView.setVisibility( View.VISIBLE );
		mSearchBarView = searchBarView;
		setHasSearchBar();
		updateBackgroundAndPaddings();
	}
	
	/**
	 * Scrolls this list view to the top.
	 */
	public void scrollToTop()
	{
		mAppsRecyclerView.scrollToTop();
	}
	
	/**
	 * Returns the content view used for the launcher transitions.
	 */
	public View getContentView()
	{
		return mContainerView;
	}
	
	/**
	 * Returns the all apps search view.
	 */
	public View getSearchBarView()
	{
		return mSearchBarView;
	}
	
	/**
	 * Returns the reveal view used for the launcher transitions.
	 */
	public View getRevealView()
	{
		return mRevealView;
	}
	
	/**
	 * Returns an new instance of the default app search controller.
	 */
	// zhangjin@2016/05/05 DEL START
	public AllAppsSearchBarController newDefaultAppSearchController()
	{
		//xiatian start	//优化“2016/05/25 17:50:26”的修改点中关于6.0主菜单搜索栏的修改。
		//		Context mContext = getContext();//xiatian del
		Context mContext = mLauncher;//xiatian add
		//xiatian end
		return new DefaultAppSearchController( mContext , this , mAppsRecyclerView );
	}
	
	// zhangjin@2016/05/05 DEL END
	/**
	 * Focuses the search field and begins an app search.
	 */
	public void startAppsSearch()
	{
		if( mSearchBarController != null )
		{
			mSearchBarController.focusSearchField();
		}
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		boolean isRtl = Utilities.isRtl( getResources() );
		mAdapter.setRtl( isRtl );
		mContent = findViewById( R.id.content );
		// This is a focus listener that proxies focus from a view into the list view.  This is to
		// work around the search box from getting first focus and showing the cursor.
		View.OnFocusChangeListener focusProxyListener = new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(
					View v ,
					boolean hasFocus )
			{
				if( hasFocus )
				{
					mAppsRecyclerView.requestFocus();
				}
			}
		};
		mSearchBarContainerView = (ViewGroup)findViewById( R.id.search_box_container );
		mSearchBarContainerView.setOnFocusChangeListener( focusProxyListener );
		mContainerView = findViewById( R.id.all_apps_container );
		mContainerView.setOnFocusChangeListener( focusProxyListener );
		mRevealView = findViewById( R.id.all_apps_reveal );
		// Load the all apps recycler view
		mAppsRecyclerView = (AllAppsRecyclerView)findViewById( R.id.apps_list_view );
		mAppsRecyclerView.setApps( mApps );
		mAppsRecyclerView.setLayoutManager( mLayoutManager );
		mAppsRecyclerView.setAdapter( mAdapter );
		mAppsRecyclerView.setHasFixedSize( true );
		if( mItemDecoration != null )
		{
			mAppsRecyclerView.addItemDecoration( mItemDecoration );
		}
		updateBackgroundAndPaddings();
	}
	
	@Override
	public void onBoundsChanged(
			Rect newBounds )
	{
		// zhangjin@2016/05/05 DEL START
		//mLauncher.updateOverlayBounds( newBounds );
		// zhangjin@2016/05/05 DEL END
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		// Update the number of items in the grid before we measure the view
		DeviceProfile grid = mLauncher.getDeviceProfile();
		// zhangjin@2016/05/05 DEL START
		//int availableWidth = !mContentBounds.isEmpty() ? mContentBounds.width() : MeasureSpec.getSize( widthMeasureSpec );
		//DeviceProfile grid = mLauncher.getDeviceProfile();
		//grid.updateAppsViewNumCols( getResources() , availableWidth );
		//if( mNumAppsPerRow != grid.getAllAppsNumCols() || mNumPredictedAppsPerRow != grid.allAppsNumPredictiveCols )
		//{
		//	mNumAppsPerRow = grid.getAllAppsNumCols();
		//	mNumPredictedAppsPerRow = grid.allAppsNumPredictiveCols;
		//	// If there is a start margin to draw section names, determine how we are going to merge
		//	// app sections
		//	boolean mergeSectionsFully = mSectionNamesMargin == 0;
		//	AlphabeticalAppsList.MergeAlgorithm mergeAlgorithm = mergeSectionsFully ? new FullMergeAlgorithm() : new SimpleSectionMergeAlgorithm(
		//			(int)Math.ceil( mNumAppsPerRow / 2f ) ,
		//			MIN_ROWS_IN_MERGED_SECTION_PHONE ,
		//			MAX_NUM_MERGES_PHONE );
		//	mAppsRecyclerView.setNumAppsPerRow( grid , mNumAppsPerRow );
		//	mAdapter.setNumAppsPerRow( mNumAppsPerRow );
		//	mApps.setNumAppsPerRow( mNumAppsPerRow , mNumPredictedAppsPerRow , mergeAlgorithm );
		//}
		if( mNumAppsPerRow != grid.getAllAppsNumCols() )
		{
			mNumAppsPerRow = grid.getAllAppsNumCols();
			mNumPredictedAppsPerRow = mNumAppsPerRow;
			// If there is a start margin to draw section names, determine how we are going to merge
			// app sections
			boolean mergeSectionsFully = mSectionNamesMargin == 0;
			AlphabeticalAppsList.MergeAlgorithm mergeAlgorithm = getMergeAlgorithm();
			mAppsRecyclerView.setNumAppsPerRow( grid , mNumAppsPerRow );
			mAdapter.setNumAppsPerRow( mNumAppsPerRow );
			mApps.setNumAppsPerRow( mNumAppsPerRow , mNumPredictedAppsPerRow , mergeAlgorithm );
		}
		// zhangjin@2016/05/05 DEL END
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
	}
	
	public AlphabeticalAppsList.MergeAlgorithm getMergeAlgorithm()
	{
		boolean mergeSectionsFully = mSectionNamesMargin == 0;
		AlphabeticalAppsList.MergeAlgorithm mergeAlgorithm = mergeSectionsFully ? new FullMergeAlgorithm() : new SimpleSectionMergeAlgorithm( (int)Math.ceil( mLauncher.getDeviceProfile()
				.getAllAppsNumCols() / 2 ) , MIN_ROWS_IN_MERGED_SECTION_PHONE , MAX_NUM_MERGES_PHONE );
		return mergeAlgorithm;
	}
	
	/**
	 * Update the background and padding of the Apps view and children.  Instead of insetting the
	 * container view, we inset the background and padding of the recycler view to allow for the
	 * recycler view to handle touch events (for fast scrolling) all the way to the edge.
	 */
	@Override
	protected void onUpdateBackgroundAndPaddings(
			Rect searchBarBounds ,
			Rect padding )
	{
		boolean isRtl = Utilities.isRtl( getResources() );
		// TODO: Use quantum_panel instead of quantum_panel_shape
		InsetDrawable background = new InsetDrawable( getResources().getDrawable( R.drawable.quantum_panel_shape ) , padding.left , 0 , padding.right , 0 );
		Rect bgPadding = new Rect();
		background.getPadding( bgPadding );
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		//mContainerView.setBackground( background );
		//mRevealView.setBackground( background.getConstantState().newDrawable() );
		mContainerView.setBackgroundDrawable( background );
		mRevealView.setBackgroundDrawable( background.getConstantState().newDrawable() );
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		mAppsRecyclerView.updateBackgroundPadding( bgPadding );
		mAdapter.updateBackgroundPadding( bgPadding );
		// Hack: We are going to let the recycler view take the full width, so reset the padding on
		// the container to zero after setting the background and apply the top-bottom padding to
		// the content view instead so that the launcher transition clips correctly.
		mContent.setPadding( 0 , padding.top , 0 , padding.bottom );
		mContainerView.setPadding( 0 , 0 , 0 , 0 );
		// Pad the recycler view by the background padding plus the start margin (for the section
		// names)
		int startInset = Math.max( mSectionNamesMargin , mAppsRecyclerView.getMaxScrollbarWidth() );
		int topBottomPadding = mRecyclerViewTopBottomPadding;
		if( isRtl )
		{
			mAppsRecyclerView.setPadding( padding.left + mAppsRecyclerView.getMaxScrollbarWidth() , topBottomPadding , padding.right + startInset , topBottomPadding );
		}
		else
		{
			mAppsRecyclerView.setPadding( padding.left + startInset , topBottomPadding , padding.right + mAppsRecyclerView.getMaxScrollbarWidth() , topBottomPadding );
		}
		// Inset the search bar to fit its bounds above the container
		onUpdateSearchBarBackgroundAndPaddings( searchBarBounds );
		setBackgroundDrawable( new ColorDrawable( getResources().getColor( R.color.quantum_bg_color ) ) );//zhujieping add //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	}
	
	protected void onUpdateSearchBarBackgroundAndPaddings(
			Rect searchBarBounds )
	{
		if( mSearchBarView != null )
		{
			Rect backgroundPadding = new Rect();
			if( mSearchBarView.getBackground() != null )
			{
				mSearchBarView.getBackground().getPadding( backgroundPadding );
			}
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mSearchBarContainerView.getLayoutParams();
			lp.leftMargin = searchBarBounds.left - backgroundPadding.left;
			lp.topMargin = searchBarBounds.top - backgroundPadding.top;
			lp.rightMargin = ( getMeasuredWidth() - searchBarBounds.right ) - backgroundPadding.right;
			mSearchBarContainerView.requestLayout();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(
			KeyEvent event )
	{
		// Determine if the key event was actual text, if so, focus the search bar and then dispatch
		// the key normally so that it can process this key event
		// zhangjin@2016/05/09 DEL START
		//if( !mSearchBarController.isSearchFieldFocused() && event.getAction() == KeyEvent.ACTION_DOWN )
		//{
		//	final int unicodeChar = event.getUnicodeChar();
		//	final boolean isKeyNotWhitespace = unicodeChar > 0 && !Character.isWhitespace( unicodeChar ) && !Character.isSpaceChar( unicodeChar );
		//	if( isKeyNotWhitespace )
		//	{
		//		boolean gotKey = TextKeyListener.getInstance().onKeyDown(
		//				this ,
		//				mSearchQueryBuilder ,
		//				event.getKeyCode() ,
		//				event );
		//		if( gotKey && mSearchQueryBuilder.length() > 0 )
		//		{
		//			mSearchBarController.focusSearchField();
		//		}
		//	}
		//}
		// zhangjin@2016/05/09 DEL END
		return super.dispatchKeyEvent( event );
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		return handleTouchEvent( ev );
	}
	
	@SuppressLint( "ClickableViewAccessibility" )
	@Override
	public boolean onTouchEvent(
			MotionEvent ev )
	{
		return handleTouchEvent( ev );
	}
	
	@SuppressLint( "ClickableViewAccessibility" )
	@Override
	public boolean onTouch(
			View v ,
			MotionEvent ev )
	{
		switch( ev.getAction() )
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				mIconLastTouchPos.set( (int)ev.getX() , (int)ev.getY() );
				break;
		}
		return false;
	}
	
	@Override
	public boolean onLongClick(
			View v )
	{
		// Return early if this is not initiated from a touch
		if( !v.isInTouchMode() )
			return false;
		// When we have exited all apps or are in transition, disregard long clicks
		if( !mLauncher.isAllAppsVisible() || mLauncher.getWorkspace().isSwitchingState() )
			return false;
		// Return if global dragging is not enabled
		if( !mLauncher.isDraggingEnabled() )
			return false;
		// Start the drag
		mLauncher.getWorkspace().onDragStartedWithItem( v );//zhujieping,在桌面生成图标阴影
		// zhangjin@2016/05/05 UPD START
		//mLauncher.getWorkspace().beginDragShared( v , mIconLastTouchPos , this , false );
		mLauncher.getWorkspace().beginDragShared( v , this );
		// zhangjin@2016/05/05 UPD END
		//zhujieping add start //拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。
		//这边改成跟AppsCustomizePagedView一致。因为mLauncher.getWorkspace().beginDragShared里面调用的一些方法会post主线程中，下面这个方法要等post中执行完成后执行。
		//解决”6.0、7.0主菜单拖动图标至桌面生成新页面，新页面无页面框“的问题。
		// Enter spring loaded mode
		// We delay entering spring-loaded mode slightly to make sure the UI
		// thready is free of any work.
		postDelayed( new Runnable() {
			
			@Override
			public void run()
			{
				// We don't enter spring-loaded mode if the drag has been cancelled
				if( mLauncher.getDragController().isDragging() )
				{
					// Go into spring loaded mode (must happen before we startDrag())
					//zhujieping start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
					//mLauncher.enterSpringLoadedDragMode();//zhujieping del
					mLauncher.enterSpringLoadedDragMode( AppsCustomizePagedView.ContentType.Applications );//zhujieping add 
					//zhujieping end
				}
			}
		} , 150 );
		//zhujieping add end
		//zhujieping modify,return true,否则容易响应两次onlongclick，出现浮屏现象
		return true;
	}
	
	@Override
	public boolean supportsFlingToDelete()
	{
		return true;
	}
	
	// zhangjin@2016/05/05 DEL START
	//@Override
	//public boolean supportsAppInfoDropTarget()
	//{
	//	return true;
	//}
	//
	//@Override
	//public boolean supportsDeleteDropTarget()
	//{
	//	return false;
	//}
	//
	//@Override
	//public float getIntrinsicIconScaleFactor()
	//{
	//	DeviceProfile grid = mLauncher.getDeviceProfile();
	//	return (float)grid.allAppsIconSizePx / grid.iconSizePx;
	//}
	//
	// zhangjin@2016/05/05 DEL END
	@Override
	public void onFlingToDeleteCompleted()
	{
		// We just dismiss the drag when we fling, so cleanup here
		mLauncher.exitSpringLoadedDragModeDelayed( true , false , null );
	}
	
	@Override
	public void onDropCompleted(
			View target ,
			DropTarget.DragObject d ,
			boolean isFlingToDelete ,
			boolean success )
	{
		if( isFlingToDelete || !success || ( target != mLauncher.getWorkspace() && !( target instanceof DeleteDropTarget ) && !( target instanceof Folder ) ) )
		{
			// Exit spring loaded mode if we have not successfully dropped or have not handled the
			// drop in Workspace
			mLauncher.exitSpringLoadedDragModeDelayed( true , false , null );
		}
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
					showOutOfSpaceMessage = !layout.findCellForSpan( null , itemInfo.getSpanX() , itemInfo.getSpanY() );
				}
			}
			if( showOutOfSpaceMessage )
			{
				mLauncher.showOutOfSpaceMessage( false );
			}
			d.deferDragViewCleanupPostAnimation = false;
		}
	}
	
	@Override
	public void onLauncherTransitionPrepare(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		// Do nothing
		mInTransition = true;
		//zhujieping add start,6.0主菜单显示成功后，设置workspace的visible为invisilbe,解决“主菜单页面右边会出现专属页面图标”的问题【i_0015012】
		if( toWorkspace )
		{
			// Going from All Apps -> Workspace
			setVisibilityOfSiblingsWithLowerZOrder( VISIBLE );
		}
		//zhujieping add end
	}
	
	@Override
	public void onLauncherTransitionStart(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		// Do nothing
	}
	
	@Override
	public void onLauncherTransitionStep(
			Launcher l ,
			float t )
	{
		// Do nothing
	}
	
	@Override
	public void onLauncherTransitionEnd(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( toWorkspace )
		{
			// Reset the search bar and base recycler view after transitioning home
			mSearchBarController.reset();
			mAppsRecyclerView.reset();
		}
		mInTransition = false;
		//zhujieping add start,6.0主菜单显示成功后，设置workspace的visible为invisilbe,解决“主菜单页面右边会出现专属页面图标”的问题【i_0015012】
		if( !toWorkspace )
		{
			setVisibilityOfSiblingsWithLowerZOrder( INVISIBLE );
		}
		////zhujieping add end
	}
	
	/**
	 * Handles the touch events to dismiss all apps when clicking outside the bounds of the
	 * recycler view.
	 */
	private boolean handleTouchEvent(
			MotionEvent ev )
	{
		DeviceProfile grid = mLauncher.getDeviceProfile();
		int x = (int)ev.getX();
		int y = (int)ev.getY();
		switch( ev.getAction() )
		{
			case MotionEvent.ACTION_DOWN:
				if( !mContentBounds.isEmpty() )
				{
					// Outset the fixed bounds and check if the touch is outside all apps
					Rect tmpRect = new Rect( mContentBounds );
					tmpRect.inset( -grid.getIconWidthSizePx() / 2 , 0 );
					if( ev.getX() < tmpRect.left || ev.getX() > tmpRect.right )
					{
						mBoundsCheckLastTouchDownPos.set( x , y );
						return true;
					}
				}
				else
				{
					// Check if the touch is outside all apps
					if( ev.getX() < getPaddingLeft() || ev.getX() > ( getWidth() - getPaddingRight() ) )
					{
						mBoundsCheckLastTouchDownPos.set( x , y );
						return true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if( mBoundsCheckLastTouchDownPos.x > -1 )
				{
					ViewConfiguration viewConfig = ViewConfiguration.get( getContext() );
					float dx = ev.getX() - mBoundsCheckLastTouchDownPos.x;
					float dy = ev.getY() - mBoundsCheckLastTouchDownPos.y;
					float distance = (float)Math.hypot( dx , dy );
					if( distance < viewConfig.getScaledTouchSlop() )
					{
						// The background was clicked, so just go home
						Launcher launcher = (Launcher)getContext();
						launcher.showWorkspace( true );
						return true;
					}
				}
				// Fall through
			case MotionEvent.ACTION_CANCEL:
				mBoundsCheckLastTouchDownPos.set( -1 , -1 );
				break;
		}
		return false;
	}
	
	@Override
	public void onSearchResult(
			String query ,
			ArrayList<ComponentKey> apps )
	{
		if( apps != null )
		{
			mApps.setOrderedFilter( apps );
			mAdapter.setLastSearchQuery( query );
			mAppsRecyclerView.onSearchResultsChanged();
		}
	}
	
	@Override
	public void clearSearchResult()
	{
		mApps.setOrderedFilter( null );
		mAppsRecyclerView.onSearchResultsChanged();
		// Clear the search query
		mSearchQueryBuilder.clear();
		mSearchQueryBuilder.clearSpans();
		Selection.setSelection( mSearchQueryBuilder , 0 );
	}
	
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , StringUtils.concat( "onClick mInTransition = " , mInTransition , " getVisibility() = " , getVisibility() ) );
		if( mInTransition || getVisibility() != View.VISIBLE )//动画中、或者不可见，不可点击
		{
			return;
		}
		if( mLauncher != null )
			mLauncher.onClick( v );
	}
	
	@Override
	public View getContent()
	{
		// TODO Auto-generated method stub
		return mContent;
	}
	
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		mApps.onThemeChanged( arg0 , arg1 );
		post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				mAdapter.notifyDataSetChanged();
			}
		} );
	}
	
	//zhujieping add start,6.0主菜单显示成功后，设置workspace的visible为invisilbe,解决“主菜单页面右边会出现专属页面图标”的问题【i_0015012】
	private void setVisibilityOfSiblingsWithLowerZOrder(
			int visibility )
	{
		ViewGroup parent = (ViewGroup)getParent();
		if( parent == null )
			return;
		View overviewPanel = ( (Launcher)getContext() ).getOverviewPanel();
		final int count = parent.getChildCount();
		if( !isChildrenDrawingOrderEnabled() )
		{
			for( int i = 0 ; i < count ; i++ )
			{
				final View child = parent.getChildAt( i );
				if( child == this )
				{
					break;
				}
				else
				{
					if( child.getVisibility() == GONE || child == overviewPanel )
					{
						continue;
					}
					if( child instanceof AppsCustomizeTabHost )
					{
						continue;
					}
					//xiatian add start	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
					if( child instanceof WorkspaceMenuVerticalList )
					{
						continue;
					}
					//xiatian add end
					child.setVisibility( visibility );
				}
			}
		}
		else
		{
			throw new RuntimeException( "Failed; can't get z-order of views" );
		}
	}
	
	//zhujieping add end
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public boolean shouldContainerScroll(
			MotionEvent ev )
	{
		int[] point = new int[2];
		point[0] = (int)ev.getX();
		point[1] = (int)ev.getY();
		Utilities.mapCoordInSelfToDescendent( mAppsRecyclerView , this , point );
		// IF the MotionEvent is inside the search box, and the container keeps on receiving
		// touch input, container should move down.
		if( mLauncher.getDragLayer().isEventOverView( mSearchBarContainerView , ev ) )
		{
			return true;
		}
		// IF the MotionEvent is inside the thumb, container should not be pulled down.
		if( mAppsRecyclerView.getScrollBar() != null && mAppsRecyclerView.getScrollBar().isNearThumb( point[0] , point[1] ) )
		{
			return false;
		}
		//		// IF a shortcuts container is open, container should not be pulled down.
		//		if( mLauncher.getOpenShortcutsContainer() != null )
		//		{
		//			return false;
		//		}
		// IF scroller is at the very top OR there is no scroll bar because there is probably not
		// enough items to scroll, THEN it's okay for the container to be pulled down.
		if( mAppsRecyclerView.getScrollBar() != null && mAppsRecyclerView.getScrollBar().getThumbOffset().y <= 0 )
		{
			return true;
		}
		if( !mAppsRecyclerView.canScrollVertically( -1 ) )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Resets the state of AllApps.
	 */
	public void reset()
	{
		// Reset the search bar and base recycler view after transitioning home
		scrollToTop();
		mSearchBarController.reset();
		mAppsRecyclerView.reset();
	}
	//zhujieping add end
	
	//zhujieping add start //7.0进入主菜单动画改成也支持4.4主菜单样式
	@Override
	public void preparePull()
	{
		// TODO Auto-generated method stub

	}
	//zhujieping add end

}
