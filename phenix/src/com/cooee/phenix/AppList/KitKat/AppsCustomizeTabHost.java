package com.cooee.phenix.AppList.KitKat;


import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.phenix.ILauncherTransitionable;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Insettable;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAnimUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView.ContentType;
import com.cooee.phenix.AppList.Marshmallow.AllAppsContainerView;
import com.cooee.phenix.FocusManager.AppsCustomizeTabKeyEventListener;
import com.cooee.phenix.PagedView.PagedViewGridLayout;
import com.cooee.phenix.PagedView.PagedViewWidget;
import com.cooee.phenix.WorkspaceMenu.WorkspaceMenuVerticalList;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class AppsCustomizeTabHost extends TabHost implements ILauncherTransitionable , TabHost.OnTabChangeListener , Insettable , IOnThemeChanged//zhujieping add,换主题不重启
//
, AppsView//zhujieping add //7.0进入主菜单动画改成也支持4.4主菜单样式
{
	
	static final String LOG_TAG = "AppsCustomizeTabHost";
	private static final String APPS_TAB_TAG = "APPS";
	private static final String WIDGETS_TAB_TAG = "WIDGETS";
	private final LayoutInflater mLayoutInflater;
	private ViewGroup mTabs;
	private ViewGroup mTabsContainer;
	private AppsCustomizePagedView mAppsCustomizePane;
	private FrameLayout mAnimationBuffer;
	private LinearLayout mContent;
	private boolean mInTransition;
	private boolean mTransitioningToWorkspace;
	private boolean mResetAfterTransition;
	private Runnable mRelayoutAndMakeVisible;
	private final Rect mInsets = new Rect();
	private ViewGroup mMenuParent;
	private TextView applistStateTitle;
	private ViewGroup stateBar;
	private Dialog mPopupmenuDialog;
	private ObjectAnimator mStateBarAnim;
	
	public AppsCustomizeTabHost(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		mLayoutInflater = LayoutInflater.from( context );
		mRelayoutAndMakeVisible = new Runnable() {
			
			public void run()
			{
				mTabs.requestLayout();
				if( mTabsContainer != null )
					mTabsContainer.setAlpha( 1f );
			}
		};
	}
	
	/**
	 * Convenience methods to select specific tabs.  We want to set the content type immediately
	 * in these cases, but we note that we still call setCurrentTabByTag() so that the tab view
	 * reflects the new content (but doesn't do the animation and logic associated with changing
	 * tabs manually).
	 */
	public void setContentTypeImmediate(
			AppsCustomizePagedView.ContentType type )
	{
		setOnTabChangedListener( null );
		onTabChangedStart();
		onTabChangedEnd( type );
		setCurrentTabByTag( getTabTagForContentType( type ) );
		setOnTabChangedListener( this );
	}
	
	@Override
	public void setInsets(
			Rect insets )
	{
		mInsets.set( insets );
		FrameLayout.LayoutParams flp = (LayoutParams)mContent.getLayoutParams();
		flp.topMargin = insets.top;
		flp.bottomMargin = insets.bottom;
		flp.leftMargin = insets.left;
		flp.rightMargin = insets.right;
		mContent.setLayoutParams( flp );
	}
	
	/**
	 * Setup the tab host and create all necessary tabs.
	 */
	@Override
	protected void onFinishInflate()
	{
		// Setup the tab host
		setup();
		final ViewGroup apps_bar = (ViewGroup)findViewById( R.id.apps_bar );
		final ViewGroup apps_tabsContainer = (ViewGroup)findViewById( R.id.tabs_container );
		final ViewGroup apps_menu = (ViewGroup)findViewById( R.id.app_menu_group );
		final ViewGroup apps_title = (ViewGroup)findViewById( R.id.apps_title );
		final TabWidget tabs = getTabWidget();
		mTabs = tabs;
		final AppsCustomizePagedView appsCustomizePane = (AppsCustomizePagedView)findViewById( R.id.apps_customize_pane_content );
		if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_TAB )
		{
			mTabsContainer = apps_tabsContainer;
			if( apps_menu != null )
			{
				removeView( apps_menu );
			}
			if( apps_title != null )
				removeView( apps_title );
		}
		else if(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
		//
		)
		{
			if( apps_tabsContainer != null )
				removeView( apps_tabsContainer );
			if( apps_title != null )
				removeView( apps_title );
			if( apps_menu != null )
			{
				apps_menu.setVisibility( View.VISIBLE );
				setupAppsListBarMenu( apps_menu );
			}
		}
		else if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_TITLE
		//
		|| LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_SORT_APP//zhujieping add //拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
		//
		)
		{
			if( apps_tabsContainer != null )
				removeView( apps_tabsContainer );
			if( apps_menu != null )
				removeView( apps_menu );
			if( apps_title != null )
				apps_title.setVisibility( View.VISIBLE );
			//zhujieping add start //拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
			if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_SORT_APP )
			{
				initApplistbarStyleOfSortApp( appsCustomizePane );
			}
			//zhujieping add end
		}
		//zhujieping add start //拓展配置项“config_applistbar_style”，添加可配置项4。4在主菜单上显示搜素栏。
		else if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_SEARCH_BAR )
		{
			if( apps_menu != null )
				removeView( apps_menu );
			if( apps_title != null )
				removeView( apps_title );
			if( apps_tabsContainer != null )
				removeView( apps_tabsContainer );
			View searchBar = LayoutInflater.from( getContext() ).inflate( R.layout.applist_search_bar , null );
			int horizon = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_search_bar_margin_horizon );
			int vertical = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_search_bar_margin_vertical );
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT , LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_search_bar_height ) );
			params.setMargins( horizon , vertical , horizon , vertical );
			apps_bar.addView( searchBar , params );
			searchBar.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					if( getContext() instanceof Launcher )
					{
						( (Launcher)getContext() ).onClickSearchButton( v );
					}
				}
			} );
		}
		//zhujieping add end
		else
		{
			if( apps_bar != null )
				removeView( apps_bar );
		}
		setTabsContainerVisibility( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE != LauncherDefaultConfig.APPLIST_BAR_STYLE_NO_BAR );//xiatian add	//添加配置开关“SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST”
		mAppsCustomizePane = appsCustomizePane;
		mAnimationBuffer = (FrameLayout)findViewById( R.id.animation_buffer );
		mContent = (LinearLayout)findViewById( R.id.apps_customize_content );
		if( tabs == null || mAppsCustomizePane == null )
			throw new Resources.NotFoundException();
		// Configure the tabs content factory to return the same paged view (that we change the
		// content filter on)
		TabContentFactory contentFactory = new TabContentFactory() {
			
			public View createTabContent(
					String tag )
			{
				return appsCustomizePane;
			}
		};
		// Create the tabs
		TextView tabView;
		String label;
		label = LauncherDefaultConfig.getString( R.string.hotseat_all_apps_button_string );
		tabView = (TextView)mLayoutInflater.inflate( R.layout.tab_widget_indicator , tabs , false );
		tabView.setText( label );
		addTab( newTabSpec( APPS_TAB_TAG ).setIndicator( tabView ).setContent( contentFactory ) );
		label = LauncherDefaultConfig.getString( R.string.widgets_tab_label );
		tabView = (TextView)mLayoutInflater.inflate( R.layout.tab_widget_indicator , tabs , false );
		tabView.setText( label );
		addTab( newTabSpec( WIDGETS_TAB_TAG ).setIndicator( tabView ).setContent( contentFactory ) );
		setOnTabChangedListener( this );
		// Setup the key listener to jump between the last tab view and the market icon
		AppsCustomizeTabKeyEventListener keyListener = new AppsCustomizeTabKeyEventListener();
		View lastTab = tabs.getChildTabViewAt( tabs.getTabCount() - 1 );
		if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		{
			lastTab.setOnKeyListener( keyListener );
		}
		View shopButton = findViewById( R.id.market_button );
		if( Launcher.SHOW_MARKET_BUTTON )
		{
			shopButton.setVisibility( View.VISIBLE );
			if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
			{
				shopButton.setOnKeyListener( keyListener );
			}
		}
		else
		{
			shopButton.setVisibility( View.GONE );
			if( mTabsContainer != null )
				mTabsContainer.removeView( shopButton );
		}
		// Hide the tab bar until we measure
		if( mTabsContainer != null )
			mTabsContainer.setAlpha( 0f );
		// zhujieping@2015/04/21 ADD START，launcher中加入属性WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS，导致布局位置从状态栏开始，这里要paddingtop状态栏的高度
		this.setPadding( getPaddingLeft() , ( (Launcher)getContext() ).getStatusBarHeight( false ) , getPaddingRight() , getPaddingBottom() );
		// zhujieping@2015/04/21 ADD END
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		boolean remeasureTabWidth = ( mTabs.getLayoutParams().width <= 0 );
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
		// Set the width of the tab list to the content width
		if( remeasureTabWidth )
		{
			int contentWidth = mAppsCustomizePane.getPageContentWidth();
			if( contentWidth > 0 && mTabs.getLayoutParams().width != contentWidth )
			{
				// Set the width and show the tab bar
				mTabs.getLayoutParams().width = contentWidth;
				mRelayoutAndMakeVisible.run();
			}
			super.onMeasure( widthMeasureSpec , heightMeasureSpec );
		}
	}
	
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		// If we are mid transitioning to the workspace, then intercept touch events here so we
		// can ignore them, otherwise we just let all apps handle the touch events.
		if( mInTransition && mTransitioningToWorkspace )
		{
			return true;
		}
		return super.onInterceptTouchEvent( ev );
	};
	
	@Override
	public boolean onTouchEvent(
			MotionEvent event )
	{
		// Allow touch events to fall through to the workspace if we are transitioning there
		if( mInTransition && mTransitioningToWorkspace )
		{
			return super.onTouchEvent( event );
		}
		// Intercept all touch events up to the bottom of the AppsCustomizePane so they do not fall
		// through to the workspace and trigger showWorkspace()
		if( event.getY() < mAppsCustomizePane.getBottom() )
		{
			return true;
		}
		return super.onTouchEvent( event );
	}
	
	private void onTabChangedStart()
	{
	}
	
	private void reloadCurrentPage()
	{
		mAppsCustomizePane.loadAssociatedPages( mAppsCustomizePane.getCurrentPage() );
		mAppsCustomizePane.requestFocus();
	}
	
	private void onTabChangedEnd(
			AppsCustomizePagedView.ContentType type )
	{
		//cheyingkun start	//分离主菜单、编辑模式、小部件、springLoaded界面的背景透明度的配置
		//		int bgAlpha = (int)( 255 * ( getResources().getInteger( R.integer.config_appsCustomizeSpringLoadedBgAlpha ) / 100f ) );//cheyingkun del
		//cheyingkun add start
		int bgAlpha = 0;
		if( type == AppsCustomizePagedView.ContentType.Applications )
		{
			bgAlpha = (int)( 255 * ( LauncherDefaultConfig.getInt( R.integer.config_applistBgAlpha ) / 100f ) );
		}
		else if( type == AppsCustomizePagedView.ContentType.Widgets )
		{
			bgAlpha = (int)( 255 * ( LauncherDefaultConfig.getInt( R.integer.config_widgetlistBgAlpha ) / 100f ) );
		}
		//cheyingkun add end
		//cheyingkun end
		setBackgroundColor( Color.argb( bgAlpha , 0 , 0 , 0 ) );
		mAppsCustomizePane.setContentType( type );
	}
	
	@Override
	public void onTabChanged(
			String tabId )
	{
		final AppsCustomizePagedView.ContentType type = getContentTypeForTabTag( tabId );
		// Animate the changing of the tab content by fading pages in and out
		final Resources res = getResources();
		final int duration = LauncherDefaultConfig.getInt( R.integer.config_tabTransitionDuration );
		// We post a runnable here because there is a delay while the first page is loading and
		// the feedback from having changed the tab almost feels better than having it stick
		post( new Runnable() {
			
			@Override
			public void run()
			{
				if( mAppsCustomizePane.getMeasuredWidth() <= 0 || mAppsCustomizePane.getMeasuredHeight() <= 0 )
				{
					reloadCurrentPage();
					return;
				}
				// Take the visible pages and re-parent them temporarily to mAnimatorBuffer
				// and then cross fade to the new pages
				int[] visiblePageRange = new int[2];
				mAppsCustomizePane.getVisiblePages( visiblePageRange );
				if( visiblePageRange[0] == -1 && visiblePageRange[1] == -1 )
				{
					// If we can't get the visible page ranges, then just skip the animation
					reloadCurrentPage();
					return;
				}
				ArrayList<View> visiblePages = new ArrayList<View>();
				for( int i = visiblePageRange[0] ; i <= visiblePageRange[1] ; i++ )
				{
					visiblePages.add( mAppsCustomizePane.getPageAt( i ) );
				}
				// We want the pages to be rendered in exactly the same way as they were when
				// their parent was mAppsCustomizePane -- so set the scroll on mAnimationBuffer
				// to be exactly the same as mAppsCustomizePane, and below, set the left/top
				// parameters to be correct for each of the pages
				mAnimationBuffer.scrollTo( mAppsCustomizePane.getScrollX() , 0 );
				// mAppsCustomizePane renders its children in reverse order, so
				// add the pages to mAnimationBuffer in reverse order to match that behavior
				for( int i = visiblePages.size() - 1 ; i >= 0 ; i-- )
				{
					View child = visiblePages.get( i );
					if( child instanceof AppsCustomizeCellLayout )
					{
						( (AppsCustomizeCellLayout)child ).resetChildrenOnKeyListeners();
					}
					else if( child instanceof PagedViewGridLayout )
					{
						( (PagedViewGridLayout)child ).resetChildrenOnKeyListeners();
					}
					PagedViewWidget.setDeletePreviewsWhenDetachedFromWindow( false );
					mAppsCustomizePane.removeView( child );
					PagedViewWidget.setDeletePreviewsWhenDetachedFromWindow( true );
					mAnimationBuffer.setAlpha( 1f );
					mAnimationBuffer.setVisibility( View.VISIBLE );
					LayoutParams p = new FrameLayout.LayoutParams( child.getMeasuredWidth() , child.getMeasuredHeight() );
					p.setMargins( (int)child.getLeft() , (int)child.getTop() , 0 , 0 );
					mAnimationBuffer.addView( child , p );
				}
				// Toggle the new content
				onTabChangedStart();
				onTabChangedEnd( type );
				// Animate the transition
				ObjectAnimator outAnim = LauncherAnimUtils.ofFloat( mAnimationBuffer , "alpha" , 0f );
				outAnim.addListener( new AnimatorListenerAdapter() {
					
					private void clearAnimationBuffer()
					{
						mAnimationBuffer.setVisibility( View.GONE );
						PagedViewWidget.setRecyclePreviewsWhenDetachedFromWindow( false );
						mAnimationBuffer.removeAllViews();
						PagedViewWidget.setRecyclePreviewsWhenDetachedFromWindow( true );
					}
					
					@Override
					public void onAnimationEnd(
							Animator animation )
					{
						clearAnimationBuffer();
					}
					
					@Override
					public void onAnimationCancel(
							Animator animation )
					{
						clearAnimationBuffer();
					}
				} );
				ObjectAnimator inAnim = LauncherAnimUtils.ofFloat( mAppsCustomizePane , "alpha" , 1f );
				inAnim.addListener( new AnimatorListenerAdapter() {
					
					@Override
					public void onAnimationEnd(
							Animator animation )
					{
						reloadCurrentPage();
					}
				} );
				final AnimatorSet animSet = LauncherAnimUtils.createAnimatorSet();
				animSet.playTogether( outAnim , inAnim );
				animSet.setDuration( duration );
				animSet.start();
			}
		} );
	}
	
	public void setCurrentTabFromContent(
			AppsCustomizePagedView.ContentType type )
	{
		setOnTabChangedListener( null );
		setCurrentTabByTag( getTabTagForContentType( type ) );
		setOnTabChangedListener( this );
	}
	
	/**
	 * Returns the content type for the specified tab tag.
	 */
	public AppsCustomizePagedView.ContentType getContentTypeForTabTag(
			String tag )
	{
		if( tag.equals( APPS_TAB_TAG ) )
		{
			return AppsCustomizePagedView.ContentType.Applications;
		}
		else if( tag.equals( WIDGETS_TAB_TAG ) )
		{
			return AppsCustomizePagedView.ContentType.Widgets;
		}
		return AppsCustomizePagedView.ContentType.Applications;
	}
	
	/**
	 * Returns the tab tag for a given content type.
	 */
	public String getTabTagForContentType(
			AppsCustomizePagedView.ContentType type )
	{
		if( type == AppsCustomizePagedView.ContentType.Applications )
		{
			return APPS_TAB_TAG;
		}
		else if( type == AppsCustomizePagedView.ContentType.Widgets )
		{
			return WIDGETS_TAB_TAG;
		}
		return APPS_TAB_TAG;
	}
	
	/**
	 * Disable focus on anything under this view in the hierarchy if we are not visible.
	 */
	@Override
	public int getDescendantFocusability()
	{
		if( getVisibility() != View.VISIBLE )
		{
			return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
		}
		return super.getDescendantFocusability();
	}
	
	public void reset()
	{
		if( mInTransition )
		{
			// Defer to after the transition to reset
			mResetAfterTransition = true;
		}
		else
		{
			// Reset immediately
			mAppsCustomizePane.reset();
		}
	}
	
	private void enableAndBuildHardwareLayer()
	{
		// isHardwareAccelerated() checks if we're attached to a window and if that
		// window is HW accelerated-- we were sometimes not attached to a window
		// and buildLayer was throwing an IllegalStateException
		if( isHardwareAccelerated() )
		{
			// Turn on hardware layers for performance
			setLayerType( LAYER_TYPE_HARDWARE , null );
			// force building the layer, so you don't get a blip early in an animation
			// when the layer is created layer
			buildLayer();
		}
	}
	
	@Override
	public View getContent()
	{
		return mContent;
	}
	
	/* LauncherTransitionable overrides */
	@Override
	public void onLauncherTransitionPrepare(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		mAppsCustomizePane.onLauncherTransitionPrepare( l , animated , toWorkspace );
		mInTransition = true;
		mTransitioningToWorkspace = toWorkspace;
		if( toWorkspace )
		{
			// Going from All Apps -> Workspace
			setVisibilityOfSiblingsWithLowerZOrder( VISIBLE );
		}
		else
		{
			// Going from Workspace -> All Apps
			mContent.setVisibility( VISIBLE );
			// Make sure the current page is loaded (we start loading the side pages after the
			// transition to prevent slowing down the animation)
			//			mAppsCustomizePane.loadAssociatedPages( mAppsCustomizePane.getCurrentPage() , true );//zhujieping del，这个方法在onTabChangedEnd中一步步调用到，这里没有必要重复调用
		}
		if( mResetAfterTransition )
		{
			mAppsCustomizePane.reset();
			mResetAfterTransition = false;
		}
	}
	
	@Override
	public void onLauncherTransitionStart(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( animated )
		{
			enableAndBuildHardwareLayer();
		}
		// Dismiss the workspace cling
		if( toWorkspace )//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
			l.dismissWorkspaceCling( null );
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
		mAppsCustomizePane.onLauncherTransitionEnd( l , animated , toWorkspace );
		mInTransition = false;
		if( animated )
		{
			setLayerType( LAYER_TYPE_NONE , null );
		}
		if( !toWorkspace )
		{
			// Show the all apps cling (if not already shown)
			mAppsCustomizePane.showAllAppsCling();
			// Make sure adjacent pages are loaded (we wait until after the transition to
			// prevent slowing down the animation)
			mAppsCustomizePane.loadAssociatedPages( mAppsCustomizePane.getCurrentPage() );
			// Going from Workspace -> All Apps
			// NOTE: We should do this at the end since we check visibility state in some of the
			// cling initialization/dismiss code above.
			setVisibilityOfSiblingsWithLowerZOrder( INVISIBLE );
		}
	}
	
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
					// zhangjin@2016/05/10 ADD START
					if( child instanceof AllAppsContainerView )
					{
						continue;
					}
					// zhangjin@2016/05/10 ADD END
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
	
	public void onWindowVisible()
	{
		if( getVisibility() == VISIBLE )
		{
			mContent.setVisibility( VISIBLE );
			// We unload the widget previews when the UI is hidden, so need to reload pages
			// Load the current page synchronously, and the neighboring pages asynchronously
			mAppsCustomizePane.loadAssociatedPages( mAppsCustomizePane.getCurrentPage() , true );
			mAppsCustomizePane.loadAssociatedPages( mAppsCustomizePane.getCurrentPage() );
		}
	}
	
	public void onTrimMemory()
	{
		mContent.setVisibility( GONE );
		// Clear the widget pages of all their subviews - this will trigger the widget previews
		// to delete their bitmaps
		mAppsCustomizePane.clearAllWidgetPages();
	}
	
	boolean isTransitioning()
	{
		return mInTransition;
	}
	
	public void setTabsContainerVisibility(
			boolean mIsShowTabsContainer )
	{
		int mIsTabsContainerVisible = View.GONE;
		if( mIsShowTabsContainer )
		{
			mIsTabsContainerVisible = View.VISIBLE;
		}
		View toset = null;
		int preVisibility = -1;
		// zhujieping@2015/04/27 ADD START
		if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_TAB )
		{
			if( mTabsContainer != null )
			{
				toset = mTabsContainer;
			}
		}
		else if(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
		//
		)
		{
			toset = findViewById( R.id.app_menu_group );
		}
		else if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_TITLE )
		{
			toset = findViewById( R.id.apps_title );
		}
		if( toset != null )
		{
			preVisibility = toset.getVisibility();
			if( mIsTabsContainerVisible != preVisibility )
			{
				if( mAppsCustomizePane != null )
					mAppsCustomizePane.setDataIsReady( false );//tab栏状态变化后，重新计算大小
			}
			toset.setVisibility( mIsTabsContainerVisible );
		}
	}
	
	public int getTabsContainerVisibility()
	{
		if( mTabsContainer == null )
		{
			return -1;
		}
		int ret = View.GONE;
		if( mTabsContainer != null )
		{
			ret = mTabsContainer.getVisibility();
		}
		return ret;
	}
	
	public void setupAppsListBarMenu(
			ViewGroup parent )
	{
		if( parent == null )
			return;
		stateBar = (ViewGroup)parent.findViewById( R.id.state_parent );
		applistStateTitle = (TextView)parent.findViewById( R.id.applist_bar_state_title );
		View applistStateDone = findViewById( R.id.applist_bar_state_done );
		OnClickListener listener = new OnClickListener() {
			
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				setAppsCustomizePaneMode( AppsCustomizePagedView.NORMAL_MODE );
			}
		};
		applistStateTitle.setOnClickListener( listener );
		applistStateDone.setOnClickListener( listener );
		mMenuParent = (ViewGroup)parent.findViewById( R.id.menu_parent );
		ImageView mAppsMenu = (ImageView)parent.findViewById( R.id.applist_menu );
		//zhujieping add start	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。
		if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6 )
		{
			String[] mMenuList = LauncherDefaultConfig.getStringArray( R.array.applist_menu_list );
			if( mMenuList.length == 0 )
			{
				throw new RuntimeException( "applist_menu_list must has item" );
			}
			mMenuParent.removeView( mAppsMenu );
			for( String key : mMenuList )//zhujieping，这里将textview加入到父中
			{
				String name = getNameByMenuKey( key );
				TextView tv = new TextView( getContext() );
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.MATCH_PARENT );
				params.gravity = Gravity.CENTER_VERTICAL;
				tv.setPadding( LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_menu_item_gap ) , 0 , 0 , 0 );
				tv.setGravity( Gravity.CENTER_VERTICAL );
				tv.setText( name );
				tv.setTextSize( TypedValue.COMPLEX_UNIT_PX , LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_menu_item_textsize ) );
				mMenuParent.addView( tv , params );
				tv.setTag( key );
				tv.setTextColor( getResources().getColorStateList( R.drawable.applist_menu_item_text_selector ) );
				tv.setOnClickListener( mOnClickListener );
			}
			//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
			stateBar.setBackgroundDrawable( new ColorDrawable( 0x00000000 ) );
			stateBar.removeView( applistStateTitle );
			applistStateTitle = null;
			//zhujieping add end
		}
		else
		//zhujieping add end
		{
			//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
			stateBar.removeView( applistStateDone );
			//zhujieping add end
			mAppsMenu.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					showPopupMenuDialog();
				}
			} );
		}
	}
	
	public void hidePopupMenuDialog()
	{
		if( mPopupmenuDialog != null )
		{
			if( mPopupmenuDialog.isShowing() )
				mPopupmenuDialog.dismiss();
		}
	}
	
	public void showPopupMenuDialog()
	{
		if( mPopupmenuDialog == null )
			mPopupmenuDialog = getPopupMenuDialog( getContext() );
		mPopupmenuDialog.show();
	}
	
	private Dialog getPopupMenuDialog(
			Context context )
	{
		final Dialog mDialog = new Dialog( context , R.style.Theme_buttom_dialog ) {
			
			@Override
			public boolean onKeyUp(
					int keyCode ,
					KeyEvent event )
			{
				if( LauncherDefaultConfig.CONFIG_MENU_KEY_STYLE == LauncherDefaultConfig.MENU_KEY_STYLE_RESPONSE_IN_ON_PREPARE_OPTIONS_MENU )
				{
					return super.onKeyUp( keyCode , event );
				}
				if( keyCode == KeyEvent.KEYCODE_MENU )
				{
					onMenuPressed();
					return true;
				}
				return super.onKeyUp( keyCode , event );
			}
			
			@Override
			public boolean onPrepareOptionsMenu(
					Menu menu )
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.CONFIG_MENU_KEY_STYLE == LauncherDefaultConfig.MENU_KEY_STYLE_RESPONSE_IN_ON_PREPARE_OPTIONS_MENU )
				{
					onMenuPressed();
					return true;
				}
				return super.onPrepareOptionsMenu( menu );
			}
		};
		ListView mListView = new ListView( context );
		mListView.setBackgroundResource( R.drawable.app_menu_dailog_bg_frame );
		ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>( context , R.layout.app_menu_list_item , new String[]{
				getResources().getString( R.string.app_menu_appbar_tab_view_as ) ,
				getResources().getString( R.string.app_menu_appbar_tab_hide ) ,
				getResources().getString( R.string.app_menu_appbar_tab_edit ) } );
		mListView.setAdapter( arrayadapter );
		mDialog.setContentView( mListView );
		//设置监听
		mListView.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// TODO Auto-generated method stub
				mDialog.dismiss();
				if( position == 0 )
				{
					if( mAppsCustomizePane != null )
					{
						mAppsCustomizePane.showSortDialog();
					}
				}
				else if( position == 1 )
				{
					setAppsCustomizePaneMode( AppsCustomizePagedView.HIDE_MODE );
				}
				else if( position == 2 )
				{
					setAppsCustomizePaneMode( AppsCustomizePagedView.EDIT_MODE );
				}
			}
		} );
		Window dialogWindow = mDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity( Gravity.TOP );
		lp.width = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_menu_appslist_menu_width ); // 宽度
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.x = getResources().getDisplayMetrics().widthPixels - lp.width;
		lp.y = mMenuParent.getBottom();
		dialogWindow.setAttributes( lp );
		return mDialog;
	}
	
	public void setAppsCustomizePaneMode(
			int mode )
	{
		if( mode == AppsCustomizePagedView.EDIT_MODE || mode == AppsCustomizePagedView.HIDE_MODE || mode == AppsCustomizePagedView.UNINSTALL_MODE )
		{
			if( applistStateTitle != null )
			{
				if( mode == AppsCustomizePagedView.HIDE_MODE )
					applistStateTitle.setText( R.string.app_menu_appbar_tab_hide );
				else
					applistStateTitle.setText( R.string.app_menu_appbar_tab_edit );
			}
			if( stateBar != null )
				stateBar.setVisibility( View.VISIBLE );
			if( mMenuParent != null )
				mMenuParent.setVisibility( View.INVISIBLE );
		}
		else
		{
			if( stateBar != null )
				stateBar.setVisibility( View.GONE );
			if( mMenuParent != null )
				mMenuParent.setVisibility( View.VISIBLE );
		}
		if( mAppsCustomizePane != null )
		{
			mAppsCustomizePane.setAppsMode( mode );
		}
	}
	
	public void setStateBarShow(
			boolean isShow ,
			boolean isAnim )
	{
		if( stateBar == null )
		{
			return;
		}
		if( !isAnim )
		{
			if( isShow )
			{
				stateBar.setVisibility( View.VISIBLE );
			}
			else
			{
				stateBar.setVisibility( View.INVISIBLE );
			}
		}
		else
		{
			if( mStateBarAnim == null )
			{
				mStateBarAnim = LauncherAnimUtils.ofFloat( stateBar , "translationY" , 0 , -LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_menu_appslist_bar_height ) );
				setupAnimation( mStateBarAnim , stateBar );
			}
			stateBar.setLayerType( View.LAYER_TYPE_HARDWARE , null );
			if( isShow )
			{
				mStateBarAnim.reverse();
			}
			else
			{
				mStateBarAnim.start();
			}
		}
	}
	
	private void setupAnimation(
			ObjectAnimator anim ,
			final View v )
	{
		anim.setInterpolator( new AccelerateInterpolator() );
		anim.setDuration( 200 );
		anim.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				if( v == null )
				{
					return;
				}
				v.setLayerType( View.LAYER_TYPE_NONE , null );
			}
		} );
	}
	
	public boolean onBackPressed()
	{
		if( mAppsCustomizePane.getAppsMode() != AppsCustomizePagedView.NORMAL_MODE )
		{
			setAppsCustomizePaneMode( AppsCustomizePagedView.NORMAL_MODE );
			return true;
		}
		return false;
	}
	
	public boolean onMenuPressed()
	{
		if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5 )
		{
			if( getVisibility() == View.VISIBLE && mAppsCustomizePane.getContentType() == ContentType.Applications )//主菜单应用界面
			{
				if( mPopupmenuDialog == null || !mPopupmenuDialog.isShowing() )
				{
					showPopupMenuDialog();
				}
				else
				{
					hidePopupMenuDialog();
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setVisibility(
			int visibility )
	{
		// TODO Auto-generated method stub
		super.setVisibility( visibility );
		if( visibility != View.VISIBLE )//主菜单隐藏，不显示menu的dialog，并且退出编辑或隐藏模式
		{
			hidePopupMenuDialog();
			setAppsCustomizePaneMode( AppsCustomizePagedView.NORMAL_MODE );
			mAppsCustomizePane.hideSortDailog();
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
		if( mAppsCustomizePane != null )
		{
			mAppsCustomizePane.onThemeChanged( arg0 , arg1 );
		}
	}
	
	//zhujieping add start	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(
				View v )
		{
			if( v.getTag() == null )
			{
				return;
			}
			dealClickByMenuKey( v.getTag().toString() , v );
		}
	};
	
	private void dealClickByMenuKey(
			String tag ,
			View v )
	{
		//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
		if( tag.equals( "uninstall_app" ) )
		{
			setAppsCustomizePaneMode( AppsCustomizePagedView.UNINSTALL_MODE );
		}
		//zhujieping add end
		else if( tag.equals( "hide_app" ) )
		{
			setAppsCustomizePaneMode( AppsCustomizePagedView.HIDE_MODE );
		}
		else if( tag.equals( "search" ) )
		{
			String temp = LauncherDefaultConfig.getString( R.string.applist_search_menu_package_and_classname );
			if( !TextUtils.isEmpty( temp ) )
			{
				String pacs[] = temp.split( "," );
				Intent intent = new Intent();
				intent.setClassName( pacs[0] , pacs[1] );
				if( getContext().getPackageManager().queryIntentActivities( intent , 0 ).size() != 0 )
				{
					getContext().startActivity( intent );
					return;
				}
			}
			if( getContext() instanceof Launcher )
			{
				( (Launcher)getContext() ).onClickSearchButton( v );
			}
		}
		else if( tag.equals( "sort_app" ) )
		{
			if( mAppsCustomizePane != null )
			{
				mAppsCustomizePane.sortApp( AppsCustomizePagedView.SORT_NAME , true );//zhujieping add // 更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
			}
		}
	}
	
	public String getNameByMenuKey(
			String key )
	{
		String title = null;
		if( key.equals( "uninstall_app" ) )//zhujieping modify //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
		{
			title = getResources().getString( R.string.app_menu_appbar_tab_edit );
		}
		else if( key.equals( "hide_app" ) )
		{
			title = getResources().getString( R.string.app_menu_appbar_tab_hide );
		}
		else if( key.equals( "search" ) )
		{
			title = getResources().getString( R.string.app_menu_appbar_tab_search );
		}
		else if( key.equals( "sort_app" ) )
		{
			title = getResources().getString( R.string.app_menu_appbar_tab_a_z );
		}
		else
		{
			throw new RuntimeException( "applist_menu_list must configure correctly" );
		}
		return title;
	}
	//zhujieping add end
	
	//zhujieping add start //7.0进入主菜单动画改成也支持4.4主菜单样式
	@Override
	public boolean shouldContainerScroll(
			MotionEvent ev )
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public View getContentView()
	{
		// TODO Auto-generated method stub
		return mAppsCustomizePane;
	}

	@Override
	public void startAppsSearch()
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void preparePull()
	{
		// TODO Auto-generated method stub
		setTabsContainerVisibility( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE != LauncherDefaultConfig.APPLIST_BAR_STYLE_NO_BAR );
		setContentTypeImmediate( AppsCustomizePagedView.ContentType.Applications );
		setScaleX( 1.0f );
		setScaleY( 1.0f );
	}
	//zhujieping add end
	//zhujieping add start ////拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
	public void initApplistbarStyleOfSortApp(
			AppsCustomizePagedView appsCustomizePane )
	{
		TextView tv = (TextView)findViewById( R.id.textview_title );
		RelativeLayout.LayoutParams param = (android.widget.RelativeLayout.LayoutParams)tv.getLayoutParams();
		param.addRule( RelativeLayout.CENTER_VERTICAL , RelativeLayout.TRUE );
		int sort = appsCustomizePane.getCurrentSortType();
		int padding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_bar_textview_title_padding_horizon );
		tv.setPadding( padding , 0 , padding , 0 );
		param.height = RelativeLayout.LayoutParams.MATCH_PARENT;
		tv.setCompoundDrawablePadding( LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_bar_textview_title_drawable_padding ) );
		notifySortTypeChanged( sort );
		if( Build.VERSION.SDK_INT > 17 )
		{
			param.addRule( RelativeLayout.ALIGN_PARENT_START , RelativeLayout.TRUE );
			param.setMarginStart( LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_bar_textview_title_margin_left ) );
		}
		else
		{
			param.addRule( RelativeLayout.ALIGN_PARENT_LEFT , RelativeLayout.TRUE );
			param.leftMargin = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_bar_textview_title_margin_left );
		}
		tv.setOnClickListener( new OnClickListener() {
			
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				if( mAppsCustomizePane != null )
				{
					mAppsCustomizePane.showSortDialog();
				}
			}
		} );
	}
	public void notifySortTypeChanged(
			int sort )
	{
		if( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_SORT_APP )
		{
			TextView tv = (TextView)findViewById( R.id.textview_title );
			int drawableId = -1;
			if( sort == AppsCustomizePagedView.SORT_NAME )
			{
				drawableId = R.drawable.sort_by_alphabetical;
			}
			else if( sort == AppsCustomizePagedView.SORT_USE )
			{
				drawableId = R.drawable.sort_by_most_used;
			}
			else if( sort == AppsCustomizePagedView.SORT_INSTALL )
			{
				drawableId = R.drawable.sort_by_recently_installed;
			}
			tv.setCompoundDrawablePadding( LauncherDefaultConfig.getDimensionPixelSize( R.dimen.applist_bar_textview_title_drawable_padding ) );
			if( Build.VERSION.SDK_INT > 17 )
			{
				if( drawableId > 0 )
				{
					tv.setCompoundDrawablesRelativeWithIntrinsicBounds( drawableId , 0 , 0 , 0 );
				}
			}
			else
			{
				if( drawableId > 0 )
				{
					tv.setCompoundDrawablesWithIntrinsicBounds( drawableId , 0 , 0 , 0 );
				}
			}
		}
	}
	//zhujieping add end
}
