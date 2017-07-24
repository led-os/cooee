package com.cooee.phenix.pageIndicators;


import java.io.File;
import java.util.ArrayList;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.theme.ThemeManager;


public class PageIndicatorNormal extends PageIndicator
{
	
	@SuppressWarnings( "unused" )
	private static final String TAG = "PageIndicator";
	// Want this to look good? Keep it odd
	private static final boolean MODULATE_ALPHA_ENABLED = false;
	private LayoutInflater mLayoutInflater;
	private int[] mWindowRange = new int[2];
	private int mMaxWindowSize;
	private ArrayList<PageIndicatorMarker> mMarkers = new ArrayList<PageIndicatorMarker>();
	private int mActiveMarkerIndex;
	private PageMarkerResources mFavoritesPageMarkerResources = null;
	private PageMarkerResources mHomePageMarkerResources = null;//xiatian add	//fix bug：解决“特定页面（酷生活、主页、音乐页和相机页）的页面指示器显示特定图标时，页面指示器显示错误（重复以及错位）”的问题。
	private PageMarkerResources mCameraPageMarkerResources = null;
	private PageMarkerResources mMusicPageMarkerResources = null;
	private PageMarkerResources mAddPageMarkerResources = null;
	//fulijuan add start	//页面指示器支持本地化
	private static final int TYPE_DEFAULT = 0;
	private static final int TYPE_FAVORITES = 1;
	private static final int TYPE_HOME = 2;
	private static final int TYPE_CAMERA = 3;
	private static final int TYPE_MUSIC = 4;
	private static final int TYPE_ADD = 5;
	public static final String FAVORITES_PAGE_ICON_CURRENT_PATH = "theme/pageindicator/ic_pageindicator_favorites_page_current.png";
	public static final String FAVORITES_PAGE_ICON_DEFAULT_PATH = "theme/pageindicator/ic_pageindicator_favorites_page_default.png";
	public static final String HOME_PAGE_ICON_CURRENT_PATH = "theme/pageindicator/ic_pageindicator_home_page_current.png";
	public static final String HOME_PAGE_ICON_DEFAULT_PATH = "theme/pageindicator/ic_pageindicator_home_page_default.png";
	public static final String CAMERA_PAGE_ICON_CURRENT_PATH = "theme/pageindicator/ic_pageindicator_camera_page_current.png";
	public static final String CAMERA_PAGE_ICON_DEFAULT_PATH = "theme/pageindicator/ic_pageindicator_camera_page_default.png";
	public static final String MUSIC_PAGE_ICON_CURRENT_PATH = "theme/pageindicator/ic_pageindicator_music_page_current.png";
	public static final String MUSIC_PAGE_ICON_DEFAULT_PATH = "theme/pageindicator/ic_pageindicator_music_page_default.png";
	public static final String NORMAL_PAGE_ICON_CURRENT_PATH = "theme/pageindicator/ic_pageindicator_normal_page_current.png";
	public static final String NORMAL_PAGE_ICON_DEFAULT_PATH = "theme/pageindicator/ic_pageindicator_normal_page_default.png";
	public static final String ADD_PAGE_ICON_PATH = "theme/pageindicator/ic_pageindicator_add_page.png";
	//fulijuan add end
	;
	
	public PageIndicatorNormal(
			Context context )
	{
		this( context , null );
	}
	
	public PageIndicatorNormal(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public PageIndicatorNormal(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.PageIndicator , defStyle , 0 );
		mMaxWindowSize = a.getInteger( R.styleable.PageIndicator_windowSize , 15 );
		//zhujieping add start //读取的配置是普通页的最大页数，这里要加上媒体页，否则达到最大页数时，且在页面能够显示的下时候，会出现指示器没有显示全
		if( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES )
		{
			mMaxWindowSize++;
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_SHOW )
		{
			mMaxWindowSize++;
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_MUSICPAGE_SHOW )
		{
			mMaxWindowSize++;
		}
		//zhujieping add end
		mWindowRange[0] = 0;
		mWindowRange[1] = 0;
		mLayoutInflater = LayoutInflater.from( context );
		a.recycle();
		// Set the layout transition properties
		LayoutTransition transition = getLayoutTransition();
		if( transition != null )
			transition.setDuration( 175 );
	}
	
	private void enableLayoutTransitions()
	{
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		if( Build.VERSION.SDK_INT >= 16 )
		{
			LayoutTransition transition = getLayoutTransition();
			if( transition != null )
			{
				transition.enableTransitionType( LayoutTransition.APPEARING );
				transition.enableTransitionType( LayoutTransition.DISAPPEARING );
				transition.enableTransitionType( LayoutTransition.CHANGE_APPEARING );
				transition.enableTransitionType( LayoutTransition.CHANGE_DISAPPEARING );
			}
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
	}
	
	private void disableLayoutTransitions()
	{
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		if( Build.VERSION.SDK_INT >= 16 )
		{
			LayoutTransition transition = getLayoutTransition();
			if( transition != null )
			{
				transition.disableTransitionType( LayoutTransition.APPEARING );
				transition.disableTransitionType( LayoutTransition.DISAPPEARING );
				transition.disableTransitionType( LayoutTransition.CHANGE_APPEARING );
				transition.disableTransitionType( LayoutTransition.CHANGE_DISAPPEARING );
			}
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
	}
	
	void offsetWindowCenterTo(
			int activeIndex ,
			boolean allowAnimations )
	{
		if( activeIndex < 0 )
		{
			new Throwable().printStackTrace();
		}
		int windowSize = Math.min( mMarkers.size() , mMaxWindowSize );
		int hWindowSize = (int)windowSize / 2;
		float hfWindowSize = windowSize / 2f;
		int windowStart = Math.max( 0 , activeIndex - hWindowSize );
		int windowEnd = Math.min( mMarkers.size() , windowStart + mMaxWindowSize );
		windowStart = windowEnd - Math.min( mMarkers.size() , windowSize );
		int windowMid = windowStart + ( windowEnd - windowStart ) / 2;
		boolean windowAtStart = ( windowStart == 0 );
		boolean windowAtEnd = ( windowEnd == mMarkers.size() );
		boolean windowMoved = ( mWindowRange[0] != windowStart ) || ( mWindowRange[1] != windowEnd );
		if( !allowAnimations )
		{
			disableLayoutTransitions();
		}
		// Remove all the previous children that are no longer in the window
		for( int i = getChildCount() - 1 ; i >= 0 ; --i )
		{
			PageIndicatorMarker marker = (PageIndicatorMarker)getChildAt( i );
			int markerIndex = mMarkers.indexOf( marker );
			if( markerIndex < windowStart || markerIndex >= windowEnd )
			{
				removeView( marker );
			}
		}
		// Add all the new children that belong in the window
		for( int i = 0 ; i < mMarkers.size() ; ++i )
		{
			PageIndicatorMarker marker = (PageIndicatorMarker)mMarkers.get( i );
			if( windowStart <= i && i < windowEnd )
			{
				if( indexOfChild( marker ) < 0 )
				{
					addView( marker , i - windowStart );
				}
				if( i == activeIndex )
				{
					marker.activate( windowMoved );
				}
				else
				{
					marker.inactivate( windowMoved );
				}
			}
			else
			{
				marker.inactivate( true );
			}
			if( MODULATE_ALPHA_ENABLED )
			{
				// Update the marker's alpha
				float alpha = 1f;
				if( mMarkers.size() > windowSize )
				{
					if( ( windowAtStart && i > hWindowSize ) || ( windowAtEnd && i < ( mMarkers.size() - hWindowSize ) ) || ( !windowAtStart && !windowAtEnd ) )
					{
						alpha = 1f - Math.abs( ( i - windowMid ) / hfWindowSize );
					}
				}
				marker.animate().alpha( alpha ).setDuration( 500 ).start();
			}
		}
		if( !allowAnimations )
		{
			enableLayoutTransitions();
		}
		mWindowRange[0] = windowStart;
		mWindowRange[1] = windowEnd;
	}
	
	public void addMarker(
			int index ,
			PageMarkerResources marker ,
			boolean allowAnimations )
	{
		index = Math.max( 0 , Math.min( index , mMarkers.size() ) );
		PageIndicatorMarker m = (PageIndicatorMarker)mLayoutInflater.inflate( R.layout.page_indicator_marker , this , false );
		//xiatian add start	//需求：配置项“dynamic_grid_page_indicator_height”支持本地化。
		LinearLayout.LayoutParams mPageIndicatorMarkerLP = (LinearLayout.LayoutParams)m.getLayoutParams();
		mPageIndicatorMarkerLP.width = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_page_indicator_height );
		mPageIndicatorMarkerLP.height = mPageIndicatorMarkerLP.width;
		//xiatian add start	//需求：配置项“config_pageIndicatorMarker_marginStart”和“config_pageIndicatorMarker_marginEnd”支持本地化。
		int mPageIndicatorMarkerMarginStart = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_pageIndicatorMarker_marginStart );
		int mPageIndicatorMarkerMarginEnd = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_pageIndicatorMarker_marginEnd );
		if( Build.VERSION.SDK_INT >= 17/* 4.2.2 */)
		{
			mPageIndicatorMarkerLP.setMarginStart( mPageIndicatorMarkerMarginStart );
			mPageIndicatorMarkerLP.setMarginEnd( mPageIndicatorMarkerMarginEnd );
		}
		else
		{
			mPageIndicatorMarkerLP.setMargins( mPageIndicatorMarkerMarginStart , 0 , mPageIndicatorMarkerMarginEnd , 0 );
		}
		//xiatian add end
		m.setLayoutParams( mPageIndicatorMarkerLP );
		//xiatian add end
		//fulijuan start	//页面指示器支持本地化
		//m.setMarkerDrawables( marker.activeId , marker.inactiveId ); //fulijuan del
		m.setMarkerDrawables( marker.activeDrawable , marker.inactiveDrawable );//fulijuan add
		//fulijuan end
		mMarkers.add( index , m );
		offsetWindowCenterTo( mActiveMarkerIndex , allowAnimations );
	}
	
	public void addMarkers(
			ArrayList<PageMarkerResources> markers ,
			boolean allowAnimations )
	{
		for( int i = 0 ; i < markers.size() ; ++i )
		{
			addMarker( Integer.MAX_VALUE , markers.get( i ) , allowAnimations );
		}
	}
	
	public void updateMarker(
			int index ,
			PageMarkerResources marker )
	{
		PageIndicatorMarker m = mMarkers.get( index );
		//fulijuan start	//页面指示器支持本地化
		//m.setMarkerDrawables( marker.activeId , marker.inactiveId ); //fulijuan del
		m.setMarkerDrawables( marker.activeDrawable , marker.inactiveDrawable ); //fulijuan add
		//fulijuan end
	}
	
	public void removeMarker(
			int index ,
			boolean allowAnimations )
	{
		if( mMarkers.size() > 0 )
		{
			index = Math.max( 0 , Math.min( mMarkers.size() - 1 , index ) );
			mMarkers.remove( index );
			offsetWindowCenterTo( mActiveMarkerIndex , allowAnimations );
		}
	}
	
	public void removeAllMarkers(
			boolean allowAnimations )
	{
		while( mMarkers.size() > 0 )
		{
			removeMarker( Integer.MAX_VALUE , allowAnimations );
		}
	}
	
	public void setActiveMarker(
			int index )
	{
		// Center the active marker
		mActiveMarkerIndex = index;
		offsetWindowCenterTo( index , false );
	}
	
	void dumpState(
			String txt )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			txt = StringUtils.concat( txt , "\tmMarkers: " , mMarkers.size() );
			for( int i = 0 ; i < mMarkers.size() ; ++i )
			{
				PageIndicatorMarker m = mMarkers.get( i );
				txt = StringUtils.concat( txt , "\t\t(" , i , ") " + m );
			}
			txt = StringUtils.concat( txt , "\twindow: [" , mWindowRange[0] , ", " , mWindowRange[1] , "]" );
			txt = StringUtils.concat( txt , "\tchildren: " , getChildCount() );
			for( int i = 0 ; i < getChildCount() ; ++i )
			{
				PageIndicatorMarker m = (PageIndicatorMarker)getChildAt( i );
				txt = StringUtils.concat( txt , "\t\t(" , i , ") " + m );
			}
			Log.v( "" , txt );
			Log.v( "" , "\tactive: " + mActiveMarkerIndex );
		}
	}
	
	//cheyingkun add start	//phenix仿S5效果,编辑模式页面指示器
	public void showOrHideFunctionPagesPageIndicator(
			ArrayList<Integer> mFunctionPagesIndex ,
			boolean isShow )
	{
		disableLayoutTransitions();
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
		{
			if( mFunctionPagesIndex == null || mFunctionPagesIndex.size() == 0 )
			{
				return;
			}
			for( Integer index : mFunctionPagesIndex )
			{
				if( index == -1 || index > mMarkers.size() - 1 )
				{
					continue;
				}
				PageIndicatorMarker pageIndicatorMarker = mMarkers.get( index );
				if( pageIndicatorMarker != null )
				{
					pageIndicatorMarker.setVisibility( isShow ? View.VISIBLE : View.GONE );
				}
			}
		}
		enableLayoutTransitions();
	}
	//cheyingkun add end
	;
	
	public PageMarkerResources getFavoritesPageMarkerResources()
	{
		if( mFavoritesPageMarkerResources == null )
		{
			//fulijuan start	//页面指示器支持本地化
			//mFavoritesPageMarkerResources = new PageMarkerResources( R.drawable.ic_pageindicator_favorites_page_current , R.drawable.ic_pageindicator_favorites_page_default ); //fulijuan del
			mFavoritesPageMarkerResources = getPageMarkerResources( getContext() , TYPE_FAVORITES ); //fulijuan add
			//fulijuan end
		}
		return mFavoritesPageMarkerResources;
	}
	
	//xiatian add start	//fix bug：解决“特定页面（酷生活、主页、音乐页和相机页）的页面指示器显示特定图标时，页面指示器显示错误（重复以及错位）”的问题。
	public PageMarkerResources getHomePageMarkerResources()
	{
		if( mHomePageMarkerResources == null )
		{
			//fulijuan start	//页面指示器支持本地化
			//mHomePageMarkerResources = new PageMarkerResources( R.drawable.ic_pageindicator_home_page_current , R.drawable.ic_pageindicator_home_page_default ); //fulijuan del
			mHomePageMarkerResources = getPageMarkerResources( getContext() , TYPE_HOME ); //fulijuan add
			//fulijuan end
		}
		return mHomePageMarkerResources;
	}
	//xiatian add end
	;
	
	public PageMarkerResources getCameraPageMarkerResources()
	{
		if( mCameraPageMarkerResources == null )
		{
			//fulijuan start	//页面指示器支持本地化
			//mCameraPageMarkerResources = new PageMarkerResources( R.drawable.ic_pageindicator_camera_page_current , R.drawable.ic_pageindicator_camera_page_default ); //fulijuan del
			mCameraPageMarkerResources = getPageMarkerResources( getContext() , TYPE_CAMERA ); //fulijuan add
			//fulijuan end
		}
		return mCameraPageMarkerResources;
	}
	
	public PageMarkerResources getMusicPageMarkerResources()
	{
		if( mMusicPageMarkerResources == null )
		{
			//fulijuan start	//页面指示器支持本地化
			//mMusicPageMarkerResources = new PageMarkerResources( R.drawable.ic_pageindicator_music_page_current , R.drawable.ic_pageindicator_music_page_default ); //fulijuan del
			mMusicPageMarkerResources = getPageMarkerResources( getContext() , TYPE_MUSIC ); //fulijuan add
			//fulijuan end
		}
		return mMusicPageMarkerResources;
	}
	
	public PageMarkerResources getAddPageMarkerResources()
	{
		if( mAddPageMarkerResources == null )
		{
			//fulijuan start	//页面指示器支持本地化
			//mAddPageMarkerResources = new PageMarkerResources( R.drawable.ic_pageindicator_normal_page_current , R.drawable.ic_pageindicator_add_page ); //fulijuan del
			mAddPageMarkerResources = getPageMarkerResources( getContext() , TYPE_ADD ); //fulijuan add
			//fulijuan end
		}
		return mAddPageMarkerResources;
	}
	
	//fulijuan add start	//页面指示器支持本地化
	public PageMarkerResources getDefaultPageMarkerResources()
	{
		return getPageMarkerResources( getContext() , TYPE_DEFAULT );
	}
	
	/**
	 * 
	 * @param context
	 * @param mPageIndicatorType 页面指示器类型
	 * @return
	 */
	private PageMarkerResources getPageMarkerResources(
			Context context ,
			int mPageIndicatorType )
	{
		PageMarkerResources pageMarkerResources;
		String currentPath = null; //未选中状态的指示器本地图片路径
		String defaultPath = null; //选中状态的指示器本地图片路径
		int aId = 0; //未选中状态的指示器资源文件id
		int iaId = 0; //未选中状态的指示器资源文件id
		Resources mResources = context.getResources();
		boolean isUseLocalResource = !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH );
		switch( mPageIndicatorType )
		{
			case TYPE_FAVORITES:
				if( isUseLocalResource )
				{
					currentPath = FAVORITES_PAGE_ICON_CURRENT_PATH;
					defaultPath = FAVORITES_PAGE_ICON_DEFAULT_PATH;
				}
					aId = R.drawable.ic_pageindicator_favorites_page_current;
					iaId = R.drawable.ic_pageindicator_favorites_page_default;
				break;
			case TYPE_HOME:
				if( isUseLocalResource )
				{
					currentPath = HOME_PAGE_ICON_CURRENT_PATH;
					defaultPath = HOME_PAGE_ICON_DEFAULT_PATH;
				}
					aId = R.drawable.ic_pageindicator_home_page_current;
					iaId = R.drawable.ic_pageindicator_home_page_default;
				break;
			case TYPE_CAMERA:
				if( isUseLocalResource )
				{
					currentPath = CAMERA_PAGE_ICON_CURRENT_PATH;
					defaultPath = CAMERA_PAGE_ICON_DEFAULT_PATH;
				}
					aId = R.drawable.ic_pageindicator_camera_page_current;
					iaId = R.drawable.ic_pageindicator_camera_page_default;
				break;
			case TYPE_MUSIC:
				if( isUseLocalResource )
				{
					currentPath = MUSIC_PAGE_ICON_CURRENT_PATH;
					defaultPath = MUSIC_PAGE_ICON_DEFAULT_PATH;
				}
					aId = R.drawable.ic_pageindicator_music_page_current;
					iaId = R.drawable.ic_pageindicator_music_page_default;
				break;
			case TYPE_ADD:
				if( isUseLocalResource )
				{
					currentPath = NORMAL_PAGE_ICON_CURRENT_PATH;
					defaultPath = ADD_PAGE_ICON_PATH;
				}
					aId = R.drawable.ic_pageindicator_normal_page_current;
					iaId = R.drawable.ic_pageindicator_add_page;
				break;
			case TYPE_DEFAULT:
				if( isUseLocalResource )
				{
					currentPath = NORMAL_PAGE_ICON_CURRENT_PATH;
					defaultPath = NORMAL_PAGE_ICON_DEFAULT_PATH;
				}
					aId = R.drawable.ic_pageindicator_normal_page_current;
					iaId = R.drawable.ic_pageindicator_normal_page_default;
				break;
		}
		if( isUseLocalResource )
		{
			Drawable activeDrawable = getDrawableByPageindicatorPath( mResources , currentPath );
			Drawable inactiveDrawable = getDrawableByPageindicatorPath( mResources , defaultPath );
			if( null != activeDrawable && null != inactiveDrawable )
			{
				//获取到本地图片，使用本地图片
				pageMarkerResources = new PageMarkerResources( activeDrawable , inactiveDrawable );
			}
			//没有获取到本地图片，使用默认资源文件
			else
			{
				pageMarkerResources = new PageMarkerResources( ResourcesCompat.getDrawable( mResources , aId , null ) , ResourcesCompat.getDrawable( mResources , iaId , null ) );
			}
		}
		//不支持本地化，使用默认资源文件
		else
		{
			pageMarkerResources = new PageMarkerResources( ResourcesCompat.getDrawable( mResources , aId , null ) , ResourcesCompat.getDrawable( mResources , iaId , null ) );
		}
		return pageMarkerResources;
	}
	
	/**
	 * 
	 * @param pageindicator_path 本地图片路径
	 * @return
	 */
	private Drawable getDrawableByPageindicatorPath(
			Resources mResources ,
			String pageindicator_path )
	{
		Drawable drawable = null;
		Bitmap bitmap = ThemeManager.getInstance().getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , File.separator , pageindicator_path ) );
		if( null != bitmap )
		{
			drawable = new BitmapDrawable( mResources , bitmap );
		}
		return drawable;
	}
	//fulijuan add end
}
