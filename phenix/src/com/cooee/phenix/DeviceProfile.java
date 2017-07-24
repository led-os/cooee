package com.cooee.phenix;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.theme.ThemeManager;


public class DeviceProfile
//
implements IOnThemeChanged
{
	
	private String name;
	private float minWidthDps;
	private float minHeightDps;
	private float numRows;
	private float numColumns;
	private float iconSize;
	private float iconTextSize;
	private float numHotseatIcons;
	private float hotseatIconSize;
	private int celllayoutXPadding;
	private int desiredWorkspaceLeftRightMarginPx;
	private int desiredWorkspaceTopMarginPx;
	private int desiredWorkspaceBottomMarginPx;
	private int edgeMarginPx;
	private Rect defaultWidgetPadding;
	private int widthPx;
	private int heightPx;
	private int availableWidthPx;
	private int availableHeightPx;
	private int iconWidthSizePx;
	private int iconHeightSizePx;
	private int iconTextSizePx;
	private int cellWidthPx;
	private int cellHeightPx;
	//xiatian add start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
	private int cellWidthGapPx = -1;//桌面的剩余空间分为“桌面列数（numColumns） + 1” 等分
	private int cellHeightGapPx = -1;//cheyingkun add	//修正widget行列数的计算方式
	private int itemPaddingXInCell = 0;//使得文字的宽度大于图片宽度	//cellWidthPx = iconWidthSizePx + 2*itemPaddingXInCell
	private int itemPaddingTopInCell = 0;//cellHeightPx = iconHeightSizePx + 文字高度 + itemPaddingTopInCell+itemPaddingBottomInCell
	private int itemPaddingBottomInCell = 0;
	//xiatian add end
	private int folderBackgroundOffset;
	private int folderIconWidthSizePx;
	private int folderIconHeightSizePx;
	private int folderCellWidthPx;
	private int folderCellHeightPx;
	private int hotseatCellWidthPx;
	private int hotseatCellHeightPx;
	private int hotseatBarXpadding;
	private int hotseatBarToppadding;
	private int hotseatBarBottompadding;
	private int hotseatBarHeightPx;
	private int hotseatAllAppsRank;
	private int allAppsNumRows;
	private int allAppsNumCols;
	private int searchBarSpaceWidthPx;
	private int searchBarSpaceMaxWidthPx;
	private int searchBarSpaceHeightPx;
	private int searchBarHeightPx;
	private int searchBarXPadding;
	private int searchBarTopPadding;
	private int searchBarBottomPadding;
	private int pageIndicatorHeightPx;
	private int pageIndicatorYPadding;
	private int dropBarSpaceHeightPx;
	//cheyingkun add start	//自定义桌面布局
	private boolean isCustomLayoutNormalIcon = true;
	/**默认样式下图片和文字之间的间距*/
	private int defaultGapBetweenIconAndText = 0;
	private int appsCelllayoutXPadding = 0;
	private int appsedgeMarginPx = 0;
	private int hotseatItemPaddingTopInCell;//底板栏图标的上下padding
	private int hotseatItemPaddingBottomInCell;
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	private float workspaceSpringLoadShrinkFactor;
	private int workspaceSpringLoadedBottomSpace;
	
	//zhujieping add end
	public boolean isCustomLayoutNormalIcon()
	{
		return isCustomLayoutNormalIcon;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//phenix仿S5效果,编辑模式页面指示器
	/**
	 * 页面指示器的y值(编辑模式)
	 */
	float pageIndicatorYInOverviewMode = 0;
	/**
	 * 页面指示器的y值(正常模式)
	 */
	float pageIndicatorYInNormal = 0;
	//cheyingkun add end
	/**主菜单图标放大倍数*/
	float allappsIconScale = 1.0f;//cheyingkun add	//主菜单图标缩放比。默认为1。
	private ArrayList<DeviceProfile> mProfiles;
	
	DeviceProfile(
			String n ,
			float w ,
			float h ,
			float r ,
			float c ,
			float is ,
			float its ,
			float hs ,
			float his )
	{
		// Ensure that we have an odd number of hotseat items (since we need to place all apps)
		if(
		//
		( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//
		&& hs % 2 == 0
		//
		)
		{
			throw new RuntimeException( "All Device Profiles must have an odd number of hotseat spaces" );
		}
		if( LauncherDefaultConfig.ENABLE_HOTSEAT_FUNCTION_BUTTON && hs % 2 == 0 )
		{
			throw new RuntimeException( "All Device Profiles must have an odd number of hotseat spaces" );
		}
		name = n;
		minWidthDps = w;
		minHeightDps = h;
		numRows = r;
		numColumns = c;
		iconSize = is;
		iconTextSize = its;
		numHotseatIcons = hs;
		hotseatIconSize = his;
	}
	
	DeviceProfile(
			Context context ,
			ArrayList<DeviceProfile> profiles ,
			float minWidth ,
			float minHeight ,
			int wPx ,
			int hPx ,
			int awPx ,
			int ahPx ,
			Resources resources )
	{
		//xiatian start	//整理代码：“初始化基本配置参数”相关。
		//xiatian del start
		//		DisplayMetrics dm = resources.getDisplayMetrics();
		//		ArrayList<DeviceProfileQuery> points = new ArrayList<DeviceProfileQuery>();
		//		minWidthDps = minWidth;
		//		minHeightDps = minHeight;
		//		ComponentName cn = new ComponentName( context.getPackageName() , this.getClass().getName() );
		//		defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget( context , cn , null );
		//		edgeMarginPx = resources.getDimensionPixelSize( R.dimen.dynamic_grid_edge_margin );
		//		desiredWorkspaceLeftRightMarginPx = 2 * edgeMarginPx;
		//		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		//		{
		//			pageIndicatorHeightPx = resources.getDimensionPixelSize( R.dimen.dynamic_grid_page_indicator_height );
		//		}
		//		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//		{
		//			pageIndicatorHeightPx = resources.getDimensionPixelSize( R.dimen.config_item_style_1_dynamic_grid_page_indicator_height );
		//		}
		//		//xiatian add end
		//		//cheyingkun add start	//桌面配置几行几列【c_0003360】
		//		int tempNumRows = resources.getInteger( R.integer.config_workspace_rows );
		//		if( tempNumRows <= 0 )
		//		{
		//			for( DeviceProfile p : profiles )
		//			{
		//				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.numRows ) );
		//			}
		//			numRows = Math.round( invDistWeightedInterpolate( minWidth , minHeight , points ) );
		//		}
		//		else
		//		{
		//			numRows = tempNumRows;
		//		}
		//		// Interpolate the columns
		//		int tempNumColumns = resources.getInteger( R.integer.config_workspace_columns );
		//		if( tempNumColumns <= 0 )
		//		{
		//			points.clear();
		//			for( DeviceProfile p : profiles )
		//			{
		//				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.numColumns ) );
		//			}
		//			numColumns = Math.round( invDistWeightedInterpolate( minWidth , minHeight , points ) );
		//		}
		//		else
		//		{
		//			numColumns = tempNumColumns;
		//		}
		//		//cheyingkun add end
		//		// Interpolate the icon size
		//		points.clear();
		//		for( DeviceProfile p : profiles )
		//		{
		//			points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.iconSize ) );
		//		}
		//		// zhujieping@2015/04/01 UPDATE START
		//		//图标大小读取dimens.xml中配置的大小，类似魅族这种比较特殊的分辨率在res下新建文件夹values-1800x1080，然后dimens.xml中配置app_icon_size的大小
		//		//		iconSize = invDistWeightedInterpolate( minWidth , minHeight , points );
		//		//		iconSizePx = DynamicGrid.pxFromDp( iconSize , dm );
		//		TypedValue value = new TypedValue();
		//		resources.getValue( R.dimen.app_icon_size , value , true );
		//		iconSize = (int)TypedValue.complexToFloat( value.data );
		//		iconWidthSizePx = iconHeightSizePx = resources.getDimensionPixelSize( R.dimen.app_icon_size );
		//		// zhujieping@2015/04/01 UPDATE END
		//		// Interpolate the icon text size
		//		points.clear();
		//		for( DeviceProfile p : profiles )
		//		{
		//			points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.iconTextSize ) );
		//		}
		//		try
		//		{
		//			iconTextSize = Float.parseFloat( context.getResources().getString( R.string.config_icon_text_size ) );
		//			if( iconTextSize < 0 )
		//			{
		//				iconTextSize = -iconTextSize;
		//			}
		//		}
		//		catch( NumberFormatException e )
		//		{
		//			iconTextSize = invDistWeightedInterpolate( minWidth , minHeight , points );
		//		}
		//		iconTextSizePx = DynamicGrid.pxFromSp( iconTextSize , dm );
		//		// Interpolate the hotseat size
		//		//cheyingkun add start	//桌面配置几行几列【c_0003360】
		//		if( ( tempNumRows <= 0 && tempNumColumns <= 0 ) || numColumns % 2 != 0 )
		//		{
		//			points.clear();
		//			for( DeviceProfile p : profiles )
		//			{
		//				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.numHotseatIcons ) );
		//			}
		//			numHotseatIcons = Math.round( invDistWeightedInterpolate( minWidth , minHeight , points ) );
		//		}
		//		else
		//		{
		//			if( (  LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE ) && !LauncherDefaultConfig.ENABLE_HOTSEAT_FUNCTION_BUTTON )
		//			{
		//				numHotseatIcons = (int)numColumns;
		//			}
		//			else
		//			{
		//				numHotseatIcons = (int)numColumns + 1;
		//			}
		//		}
		//		//cheyingkun add end
		//		// Interpolate the hotseat icon size
		//		points.clear();
		//		for( DeviceProfile p : profiles )
		//		{
		//			points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.hotseatIconSize ) );
		//		}
		//		//Hotseat
		//		//WangLei add start //bug:c_0003022 改变dimens.xml中控制图标大小的参数app_icon_size，底边栏图标的大小没有随之 改变
		//		//【原因】下面获取hotseatIconSize的方法得到的值在不同的分辨率下都是一样，在CellLayout里会使用hotseatIconSize/iconSize得到底边栏的缩放值
		//		//CellLayout里addView时，会根据父View是否是底边栏给子View设置不同的scale值，导致改变app_icon_size对底边栏图标的大小没有影响
		//		//【解决方案】添加一个配置选项switch_enable_hotseatIconsize_sameAs_iconSize，为true时，hoeseatIconSize和iconSize相同，为false，使用原来的方法获取
		//		boolean hotseatIconSize_sameAs_iconSize = resources.getBoolean( R.bool.switch_enable_hotseatIconsize_sameAs_iconSize );
		//		if( hotseatIconSize_sameAs_iconSize
		//		//
		//		|| LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE //xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		//
		//		)
		//		{
		//			hotseatIconSize = iconSize;//从而CellLayout的mHotseatScale为1
		//		}
		//		else
		//		{
		//			//WangLei add end
		//			hotseatIconSize = invDistWeightedInterpolate( minWidth , minHeight , points );
		//		}
		//		hotseatAllAppsRank = (int)( numHotseatIcons / 2 );
		//		// Calculate other vars based on Configuration
		//		updateFromConfiguration( resources , wPx , hPx , awPx , ahPx );
		//		// Search Bar
		//		searchBarSpaceMaxWidthPx = resources.getDimensionPixelSize( R.dimen.dynamic_grid_search_bar_max_width );
		//		searchBarSpaceWidthPx = Math.min( searchBarSpaceMaxWidthPx , widthPx );
		//		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		//		{
		//			searchBarHeightPx = resources.getDimensionPixelSize( R.dimen.dynamic_grid_search_bar_height );
		//			searchBarSpaceHeightPx = searchBarHeightPx + 2 * edgeMarginPx;
		//		}
		//		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//		{
		//			searchBarHeightPx = resources.getDimensionPixelSize( R.dimen.config_item_style_1_dynamic_grid_search_bar_height );
		//			int edgeMarginPxWhenItemStyle1 = resources.getDimensionPixelSize( R.dimen.config_item_style_1_dynamic_grid_edge_margin );
		//			searchBarSpaceHeightPx = searchBarHeightPx + 2 * edgeMarginPxWhenItemStyle1;
		//		}
		//		//xiatian add end
		//		dropBarSpaceHeightPx = resources.getDimensionPixelSize( R.dimen.dynamic_grid_drop_bar_height );
		//		// Calculate the actual text height
		//		Paint textPaint = new Paint();
		//		textPaint.setTextSize( iconTextSizePx );
		//		FontMetrics fm = textPaint.getFontMetrics();
		//		cellWidthPx = iconWidthSizePx;
		//		//cheyingkun add start	//图标名称和文件夹名称的文字显示行数（需要同步修改“WorkspaceIcon”，详见“Phenix桌面“修改配置”说明.pdf”）。
		//		int textLines = resources.getInteger( R.integer.config_icon_text_lines );
		//		cellHeightPx = iconHeightSizePx + (int)Math.ceil( fm.bottom - fm.top ) * textLines;
		//		//cheyingkun add end
		//		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		//		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//		{
		//			iconHeightSizePx = cellHeightPx;
		//		}
		//		//xiatian add end
		//		// At this point, if the cells do not fit into the available height, then we need
		//		// to shrink the icon size
		//		/*
		//		Rect padding = getWorkspacePadding(isLandscape ?
		//		        CellLayout.LANDSCAPE : CellLayout.PORTRAIT);
		//		int h = (int) (numRows * cellHeightPx) + padding.top + padding.bottom;
		//		if (h > availableHeightPx) {
		//		    float delta = h - availableHeightPx;
		//		    int deltaPx = (int) Math.ceil(delta / numRows);
		//		    iconSizePx -= deltaPx;
		//		    iconSize = DynamicGrid.dpiFromPx(iconSizePx, dm);
		//		    cellWidthPx = iconSizePx;
		//		    cellHeightPx = iconSizePx + (int) Math.ceil(fm.bottom - fm.top);
		//		}
		//		*/
		//		// Hotseat
		//		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		//		hotseatBarHeightPx = iconHeightSizePx + 4 * edgeMarginPx;//xiatian del
		//		//xiatian add start
		//		hotseatBarHeightPx = 0;
		//		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		//		{
		//			hotseatBarHeightPx = iconHeightSizePx + 4 * edgeMarginPx;
		//		}
		//		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//		{
		//			hotseatBarHeightPx = getSignleViewAvailableHeightPx();
		//		}
		//		//xiatian add end
		//		//xiatian end
		//		hotseatCellWidthPx = iconWidthSizePx;
		//		hotseatCellHeightPx = iconHeightSizePx;
		//		// Folder
		//		folderCellWidthPx = cellWidthPx + 3 * edgeMarginPx;
		//		folderCellHeightPx = cellHeightPx + (int)( ( 3f / 2f ) * edgeMarginPx );
		//		// zhujieping@2015/03/17 UPD START
		//		//文件夹大小改为跟普通icon一样大，offset值为0
		//		//folderBackgroundOffset = -edgeMarginPx;
		//		folderBackgroundOffset = 0;
		//		// zhujieping@2015/03/17 UPD END
		//		folderIconWidthSizePx = iconWidthSizePx + 2 * -folderBackgroundOffset;
		//		folderIconHeightSizePx = iconHeightSizePx + 2 * -folderBackgroundOffset;
		//xiatian del end
		//xiatian add start
		mProfiles = profiles;
		initCommonConfigs( resources , wPx , hPx , awPx , ahPx , minWidth , minHeight , context );
		initSearchDropTargetBarConfigs( resources );
		initPageIndicatorConfigs( resources );
		initWorkspaceConfigs( profiles , resources , false );
		initAppsCustomizeConfigs( resources );//cheyingkun add	//整理完善图标检测功能【c_0004366】
		initHotseatConfigs( profiles , resources );
		if( !LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )//cheyingkun add	//自定义桌面布局
		{
			checkWorkspaceAndAppsCustomizeConfig( profiles , resources );
		}
		initDropBarConfigs( resources );
		//xiatian add end
		//xiatian end
	}
	
	//xiatian del start	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
	//	public void setSearchBarSpcaseHeight(
	//			boolean install ,
	//			int statusBarHeight )
	//	{
	//		if( install )
	//		{
	//			searchBarHeightPx = searchBarSpaceHeightPx + statusBarHeight;
	//		}
	//		else
	//		{
	//			searchBarHeightPx = dropBarSpaceHeightPx;
	//		}
	//	}
	//xiatian del end
	//xiatian del start	//整理代码：“初始化基本配置参数”相关整。现猜测该方法的本意是“手机的基本配置参数改变时，重载基本配置”，但是该操作无意义，手机基本配置参数改变时，需要重载所有配置、所有布局和所有图片。故，废除该方法。
	//	void updateFromConfiguration(
	//			Resources resources ,
	//			int wPx ,
	//			int hPx ,
	//			int awPx ,
	//			int ahPx )
	//	{
	//		widthPx = wPx;
	//		heightPx = hPx;
	//		availableWidthPx = awPx;
	//		availableHeightPx = ahPx;
	//		//cheyingkun add start	//主菜单支持配置列数（修改主菜单排序算法，支持以下三种情况：1、同时配置行列；2、只配行或者只配列；3、行列都不配)
	//		int defaultAppListNumCols = resources.getInteger( R.integer.config_applist_columns );
	//		if( defaultAppListNumCols > 0 )
	//		{
	//			allAppsNumCols = defaultAppListNumCols;
	//		}
	//		else
	//		//cheyingkun add end
	//		{
	//			Rect padding = getWorkspacePadding( isLandscape ? CellLayout.LANDSCAPE : CellLayout.PORTRAIT );
	//			allAppsNumCols = ( getAvailableWidthPx() - padding.left - padding.right - 2 * edgeMarginPx ) / ( iconWidthSizePx + 2 * edgeMarginPx );
	//		}
	//		//cheyingkun add start	//主菜单支持配置行数（修改主菜单排序算法，支持以下三种情况：1、同时配置行列；2、只配行或者只配列；3、行列都不配)
	//		int defaultAppListNumRows = resources.getInteger( R.integer.config_applist_rows );
	//		if( defaultAppListNumRows > 0 )
	//		{
	//			allAppsNumRows = defaultAppListNumRows;
	//		}
	//		else
	//		//cheyingkun add end
	//		{
	//			int pageIndicatorOffset = resources.getDimensionPixelSize( R.dimen.apps_customize_page_indicator_offset );
	//			if( isLandscape )
	//			{
	//				allAppsNumRows = ( getAvailableHeightPx() - pageIndicatorOffset - 4 * edgeMarginPx ) / ( iconWidthSizePx + iconTextSizePx + 2 * edgeMarginPx );
	//			}
	//			else
	//			{
	//				allAppsNumRows = (int)numRows + 1;
	//			}
	//		}
	//		//cheyingkun add end
	//	}
	//xiatian del end
	private float dist(
			PointF p0 ,
			PointF p1 )
	{
		return (float)Math.sqrt( ( p1.x - p0.x ) * ( p1.x - p0.x ) + ( p1.y - p0.y ) * ( p1.y - p0.y ) );
	}
	
	private float weight(
			PointF a ,
			PointF b ,
			float pow )
	{
		float d = dist( a , b );
		if( d == 0f )
		{
			return Float.POSITIVE_INFINITY;
		}
		return (float)( 1f / Math.pow( d , pow ) );
	}
	
	private float invDistWeightedInterpolate(
			float width ,
			float height ,
			ArrayList<DeviceProfileQuery> points )
	{
		float sum = 0;
		float weights = 0;
		float pow = 5;
		float kNearestNeighbors = 3;
		final PointF xy = new PointF( width , height );
		ArrayList<DeviceProfileQuery> pointsByNearness = points;
		Collections.sort( pointsByNearness , new Comparator<DeviceProfileQuery>() {
			
			public int compare(
					DeviceProfileQuery a ,
					DeviceProfileQuery b )
			{
				if( !( a instanceof DeviceProfileQuery && b instanceof DeviceProfileQuery ) )
				{
					return 0;
				}
				return (int)( dist( xy , a.dimens ) - dist( xy , b.dimens ) );
			}
		} );
		for( int i = 0 ; i < pointsByNearness.size() ; ++i )
		{
			DeviceProfileQuery p = pointsByNearness.get( i );
			if( i < kNearestNeighbors )
			{
				float w = weight( xy , p.dimens , pow );
				if( w == Float.POSITIVE_INFINITY )
				{
					return p.value;
				}
				weights += w;
			}
		}
		for( int i = 0 ; i < pointsByNearness.size() ; ++i )
		{
			DeviceProfileQuery p = pointsByNearness.get( i );
			if( i < kNearestNeighbors )
			{
				float w = weight( xy , p.dimens , pow );
				sum += w * p.value / weights;
			}
		}
		return sum;
	}
	
	public Rect getWorkspacePadding(
			int orientation )
	{
		Rect padding = new Rect();
		if( orientation == CellLayout.LANDSCAPE )
		{
			// Pad the left and right of the workspace with search/hotseat bar sizes
			padding.set( searchBarHeightPx , edgeMarginPx , hotseatBarHeightPx , edgeMarginPx );
		}
		else
		{
			int mStatusBarHeight = LauncherAppState.getInstance().getStatusBarHeight();//xiatian add	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
			// Pad the top and bottom of the workspace with search/hotseat bar sizes
			int mWorkspacePaddingBottom = 0;
			int mWorkspacePaddingTop = 0;
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				mWorkspacePaddingBottom = hotseatBarHeightPx + pageIndicatorHeightPx + desiredWorkspaceBottomMarginPx;
				//					mWorkspacePaddingTop = searchBarHeightPx;//xiatian del	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{//对空间五等分，确保hotseat中view的cellHeight和workspace中view的cellHeight相同
				int virtualKayHeightPx = LauncherAppState.getInstance().getVirtualKayHeightPx();
				mWorkspacePaddingBottom = ( getHeightPx() - searchBarSpaceHeightPx - mStatusBarHeight - pageIndicatorHeightPx - virtualKayHeightPx ) / 5 + pageIndicatorHeightPx;
				//					mWorkspacePaddingTop = searchBarSpaceHeightPx + mStatusBarHeight;//xiatian del	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
			}
			//xiatian add end
			//xiatian start	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
			//xiatian del start
			//				//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
			//				if( LauncherDefaultConfig.SWITCH_ENABLE_HAS_FIXED_VIRTUAL_KEY )
			//				{
			//					mWorkspacePaddingTop = searchBarSpaceHeightPx + mStatusBarHeight;
			//				}
			//				//cheyingkun add end
			//xiatian del end
			mWorkspacePaddingTop = LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE ? ( searchBarSpaceHeightPx + mStatusBarHeight ) : mStatusBarHeight;//xiatian add
			//xiatian end
			//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机（桌面不全屏）。【c_0003770】
			//如果有虚拟按键，虚拟按键桌面不全屏，所以workspace上边距减去状态栏高度
			if( LauncherAppState.getInstance().isVirtualMenuShown() )
			{
				mWorkspacePaddingTop -= mStatusBarHeight;
			}
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				mWorkspacePaddingTop += desiredWorkspaceTopMarginPx;
			}
			//cheyingkun add end
			//cheyingkun add start	//自定义桌面布局
			int workspacePaddingLeft = 0;
			int workspacePaddingRight = 0;
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{
				//根据手机宽度、格子宽度、格子横向间距、列数、算出workspace左右边距
				workspacePaddingLeft = workspacePaddingRight = (int)( ( availableWidthPx - cellWidthPx * numColumns - cellWidthGapPx * ( numColumns - 1 ) ) / 2 );
			}
			else
			//cheyingkun add
			{
				workspacePaddingLeft = desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left;
				workspacePaddingRight = desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.right;
			}
			padding.set( //
					workspacePaddingLeft ,
					mWorkspacePaddingTop ,
					workspacePaddingRight ,
					mWorkspacePaddingBottom );
		}
		return padding;
	}
	
	// The rect returned will be extended to below the system ui that covers the workspace
	Rect getHotseatRect()
	{
		return new Rect( 0 , getAvailableHeightPx() - hotseatBarHeightPx , availableWidthPx , Integer.MAX_VALUE );
	}
	
	int calculateCellWidth(
			int width ,
			int countX )
	{
		return width / countX;
	}
	
	int calculateCellHeight(
			int height ,
			int countY )
	{
		return height / countY;
	}
	
	public boolean isPhone()
	{
		return true;
	}
	
	public void layout(
			Launcher launcher )
	{
		//xiatian start	//整理代码：“桌面相关控件的布局”相关。
		//xiatian del start
		//		//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
		//		LauncherAppState appState = LauncherAppState.getInstance();
		//		int virtualKayHeightPx = 0;
		//		int statusBarHeightReal = 0;
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_HAS_FIXED_VIRTUAL_KEY )
		//		{
		//			virtualKayHeightPx = appState.getVirtualKayHeightPx();
		//			statusBarHeightReal = appState.getStatusBarHeight();
		//		}
		//		//cheyingkun add end
		//		FrameLayout.LayoutParams lp;
		//		Resources res = launcher.getResources();
		//		boolean hasVerticalBarLayout = isVerticalBarLayout();
		//		// Layout the search bar space
		//		View searchBar = launcher.getSearchBar();
		//		lp = (FrameLayout.LayoutParams)searchBar.getLayoutParams();
		//		int statusBarHeight = launcher.getStatusBarHeight( false );
		//		if( hasVerticalBarLayout )
		//		{
		//			// Vertical search bar
		//			lp.gravity = Gravity.TOP | Gravity.LEFT;
		//			lp.width = searchBarSpaceHeightPx;
		//			lp.height = LayoutParams.MATCH_PARENT;
		//			searchBar.setPadding( 0 , 2 * edgeMarginPx , 0 , 2 * edgeMarginPx );
		//		}
		//		else
		//		{
		//			// Horizontal search bar
		//			lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		//			lp.width = searchBarSpaceWidthPx;
		//			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR )
		//			{
		//				//xiatian start	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
		//				//不再动态改变参数“searchBarHeightPx”的值
		//				//				lp.height = searchBarHeightPx;//xiatian del
		//				lp.height = searchBarSpaceHeightPx + statusBarHeight;//xiatian add
		//				//xiatian end
		//				int top = 0;
		//				int bottom = 0;//xiatian add	//让searchBar居中
		//				//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		//				{
		//					//xiatian start	//让searchBar居中
		//					//xiatian del start
		//					//					top = 2 * edgeMarginPx + statusBarHeight;
		//					//					bottom = 0;
		//					//xiatian del end
		//					//xiatian add start
		//					top = edgeMarginPx + statusBarHeight;
		//					bottom = edgeMarginPx;
		//					//xiatian add end
		//					//xiatian end
		//				}
		//				else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//				{
		//					top = statusBarHeight;
		//					bottom = 0;
		//				}
		//				//xiatian add end
		//				//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
		//				if( LauncherDefaultConfig.SWITCH_ENABLE_HAS_FIXED_VIRTUAL_KEY )
		//				{
		//					lp.topMargin = statusBarHeightReal;
		//				}
		//				//cheyingkun add end
		//				searchBar.setPadding( 2 * edgeMarginPx , top , 2 * edgeMarginPx , bottom );
		//			}
		//			else
		//			{
		//				lp.height = searchBarHeightPx + 2 * edgeMarginPx;
		//				searchBar.setPadding( 2 * edgeMarginPx , 2 * edgeMarginPx , 2 * edgeMarginPx , 0 );
		//			}
		//		}
		//		searchBar.setLayoutParams( lp );
		//		// Layout the search bar
		//		View mSearchBar = launcher.getSearchBar();
		//		LayoutParams vglp = mSearchBar.getLayoutParams();
		//		vglp.width = LayoutParams.MATCH_PARENT;
		//		vglp.height = LayoutParams.MATCH_PARENT;
		//		mSearchBar.setLayoutParams( vglp );
		//		// Layout the voice proxy
		//		// zhujieping@2015/05/27 DEL START
		//		//View voiceButtonProxy = launcher.findViewById( R.id.voice_button_proxy );
		//		//if( voiceButtonProxy != null )
		//		//{
		//		//	if( hasVerticalBarLayout )
		//		//	{
		//		//		// TODO: MOVE THIS INTO SEARCH BAR MEASURE
		//		//	}
		//		//	else
		//		//	{
		//		//		lp = (FrameLayout.LayoutParams)voiceButtonProxy.getLayoutParams();
		//		//		lp.gravity = Gravity.TOP | Gravity.END;
		//		//		lp.width = ( widthPx - searchBarSpaceWidthPx ) / 2 + 2 * iconSizePx;
		//		//		lp.height = searchBarSpaceHeightPx;
		//		//	}
		//		//}
		//		// zhujieping@2015/05/27 DEL END
		//		// Layout the workspace
		//		View workspace = launcher.findViewById( R.id.workspace );
		//		lp = (FrameLayout.LayoutParams)workspace.getLayoutParams();
		//		lp.gravity = Gravity.CENTER;
		//		//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_HAS_FIXED_VIRTUAL_KEY )
		//		{
		//			lp.bottomMargin = virtualKayHeightPx;
		//			lp.topMargin = statusBarHeightReal;
		//		}
		//		//cheyingkun add end
		//		Rect padding = getWorkspacePadding( isLandscape ? CellLayout.LANDSCAPE : CellLayout.PORTRAIT );
		//		workspace.setPadding( padding.left , padding.top , padding.right , padding.bottom );
		//		workspace.setLayoutParams( lp );
		//		// Layout the hotseat
		//		View hotseat = launcher.findViewById( R.id.hotseat );
		//		lp = (FrameLayout.LayoutParams)hotseat.getLayoutParams();
		//		if( hasVerticalBarLayout )
		//		{
		//			// Vertical hotseat
		//			lp.gravity = Gravity.RIGHT;
		//			lp.width = hotseatBarHeightPx;
		//			lp.height = LayoutParams.MATCH_PARENT;
		//			hotseat.setPadding( 0 , 2 * edgeMarginPx , 2 * edgeMarginPx , 2 * edgeMarginPx );
		//		}
		//		else if( isTablet() )
		//		{
		//			// Pad the hotseat with the grid gap calculated above
		//			int gridGap = (int)( ( widthPx - 2 * edgeMarginPx - ( numColumns * cellWidthPx ) ) / ( 2 * ( numColumns + 1 ) ) );
		//			int gridWidth = (int)( ( numColumns * cellWidthPx ) + ( ( numColumns - 1 ) * gridGap ) );
		//			int hotseatGap = (int)Math.max( 0 , ( gridWidth - ( numHotseatIcons * hotseatCellWidthPx ) ) / ( numHotseatIcons - 1 ) );
		//			lp.gravity = Gravity.BOTTOM;
		//			lp.width = LayoutParams.MATCH_PARENT;
		//			lp.height = hotseatBarHeightPx;
		//			hotseat.setPadding( 2 * edgeMarginPx + gridGap + hotseatGap , 0 , 2 * edgeMarginPx + gridGap + hotseatGap , 2 * edgeMarginPx );
		//		}
		//		else
		//		{
		//			// For phones, layout the hotseat without any bottom margin
		//			// to ensure that we have space for the folders
		//			lp.gravity = Gravity.BOTTOM;
		//			lp.width = LayoutParams.MATCH_PARENT;
		//			int mHotseatPaddingLeft = 0;
		//			int mHotseatPaddingRight = 0;
		//			//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//			//			lp.height = hotseatBarHeightPx;//xiatian del
		//			//xiatian add start
		//			int mHotseatLPHeight = 0;
		//			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		//			{
		//				mHotseatLPHeight = hotseatBarHeightPx;
		//				mHotseatPaddingLeft = 2 * edgeMarginPx;
		//				mHotseatPaddingRight = 2 * edgeMarginPx;
		//			}
		//			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//			{//底边栏高度（确保view显示完整）
		//				mHotseatLPHeight = getSignleViewAvailableHeightPx();
		//				Rect mWorkspacePadding = getWorkspacePadding( isLandscape ? CellLayout.LANDSCAPE : CellLayout.PORTRAIT );
		//				mHotseatPaddingLeft = mWorkspacePadding.left;
		//				mHotseatPaddingRight = mWorkspacePadding.right;
		//			}
		//			lp.height = mHotseatLPHeight;
		//			//xiatian add end
		//			//xiatian end
		//			//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
		//			if( LauncherDefaultConfig.SWITCH_ENABLE_HAS_FIXED_VIRTUAL_KEY )
		//			{
		//				lp.bottomMargin = virtualKayHeightPx;
		//			}
		//			//cheyingkun add end
		//			hotseat.findViewById( R.id.layout ).setPadding( mHotseatPaddingLeft , 0 , mHotseatPaddingRight , 0 );//----
		//		}
		//		hotseat.setLayoutParams( lp );
		//		// Layout the page indicators
		//		View pageIndicator = launcher.findViewById( R.id.page_indicator );
		//		if( pageIndicator != null )
		//		{
		//			if( hasVerticalBarLayout )
		//			{
		//				// Hide the page indicators when we have vertical search/hotseat
		//				pageIndicator.setVisibility( View.GONE );
		//			}
		//			else
		//			{
		//				// Put the page indicators above the hotseat
		//				lp = (FrameLayout.LayoutParams)pageIndicator.getLayoutParams();
		//				lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		//				lp.width = LayoutParams.WRAP_CONTENT;
		//				lp.height = LayoutParams.WRAP_CONTENT;
		//				//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//				//				lp.bottomMargin = hotseatBarHeightPx;//xiatian del
		//				//xiatian add start
		//				int mPageIndicatorLPBottomMargin = 0;
		//				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		//				{
		//					mPageIndicatorLPBottomMargin = hotseatBarHeightPx;
		//				}
		//				else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//				{//页面指示器底边距
		//					mPageIndicatorLPBottomMargin = getSignleViewAvailableHeightPx();
		//				}
		//				//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
		//				if( LauncherDefaultConfig.SWITCH_ENABLE_HAS_FIXED_VIRTUAL_KEY )
		//				{
		//					lp.bottomMargin = mPageIndicatorLPBottomMargin + virtualKayHeightPx;
		//				}
		//				else
		//				//cheyingkun add end
		//				{
		//					lp.bottomMargin = mPageIndicatorLPBottomMargin;
		//				}
		//				//xiatian add end
		//				//xiatian end
		//				pageIndicator.setLayoutParams( lp );
		//			}
		//		}
		//		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		//		{//重新布局
		//			hotseat.requestLayout();
		//		}
		//		//xiatian add end
		//xiatian del end
		//xiatian add start
		layoutSearchBar( launcher );
		layoutSearchDropTargetBar( launcher );
		layoutWorkspace( launcher );
		layoutHotseat( launcher );
		layoutPageIndicator( launcher );
		//xiatian add end
		//xiatian end
	}
	
	public int getCellWidthPx()
	{
		return cellWidthPx;
	}
	
	public int getCellWidthGapPx()
	{
		return cellWidthGapPx;
	}
	
	//cheyingkun add start	//修正widget行列数的计算方式
	public int getCellHeightGapPx()
	{
		return cellHeightGapPx;
	}
	
	public void setCellHeightGapPx(
			int cellHeightGapPx )
	{
		this.cellHeightGapPx = cellHeightGapPx;
	}
	
	public void setCellWidthGapPx(
			int cellWidthGapPx )
	{
		this.cellWidthGapPx = cellWidthGapPx;
	}
	
	//cheyingkun add end
	public int getCellHeightPx()
	{
		return cellHeightPx;
	}
	
	public float getNumRows()
	{
		return numRows;
	}
	
	public float getNumColumns()
	{
		return numColumns;
	}
	
	public float getIconTextSize()
	{
		return iconTextSize;
	}
	
	public int getEdgeMarginPx()
	{
		return edgeMarginPx;
	}
	
	public int getAllAppsNumRows()
	{
		return allAppsNumRows;
	}
	
	public int getAllAppsNumCols()
	{
		return allAppsNumCols;
	}
	
	public int getIconWidthSizePx()
	{
		return iconWidthSizePx;
	}
	
	public int getIconHeightSizePx()
	{
		return iconHeightSizePx;
	}
	
	public int getFolderCellWidthPx()
	{
		return folderCellWidthPx;
	}
	
	public int getFolderCellHeightPx()
	{
		return folderCellHeightPx;
	}
	
	public int getAvailableWidthPx()
	{
		return availableWidthPx;
	}
	
	public int getAvailableHeightPx()
	{
		//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机（桌面不全屏）。【c_0003770】
		//如果有虚拟按键，虚拟按键桌面不全屏，workspace上边距减去状态栏高度。所以显示区域应该减去状态栏高度
		int mStatusBarHeight = 0;
		if( LauncherAppState.getInstance().isVirtualMenuShown() )
		{
			mStatusBarHeight = LauncherAppState.getInstance().getStatusBarHeight();
		}
		//cheyingkun add end
		return availableHeightPx - mStatusBarHeight;
	}
	
	public int getFolderBackgroundOffset()
	{
		return folderBackgroundOffset;
	}
	
	public int getFolderIconWidthSizePx()
	{
		return folderIconWidthSizePx;
	}
	
	public int getFolderIconHeightSizePx()
	{
		return folderIconHeightSizePx;
	}
	
	public int getSignleViewAvailableWidthPx()
	{
		Rect mRect = getWorkspacePadding( CellLayout.PORTRAIT );
		return calculateCellWidth( getAvailableWidthPx() - mRect.left - mRect.right , (int)getNumRows() );
	}
	
	public int getSignleViewAvailableHeightPx()
	{
		Rect mRect = getWorkspacePadding( CellLayout.PORTRAIT );
		return calculateCellHeight( getAvailableHeightPx() - mRect.top - mRect.bottom , (int)getNumColumns() );
	}
	
	//cheyingkun add start	//默认图标样式下,添加图标和文字之间的间距配置【c_0004390】
	public int getDefaultGapBetweenIconAndText()
	{
		return defaultGapBetweenIconAndText;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
	public int getWidthPx()
	{
		return widthPx;
	}
	
	public int getHeightPx()
	{
		return heightPx;
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//整理代码：“初始化基本配置参数”相关。
	void initCommonConfigs(
			Resources resources ,
			int wPx ,
			int hPx ,
			int awPx ,
			int ahPx ,
			float minWidth ,
			float minHeight ,
			Context context )
	{
		widthPx = wPx;
		heightPx = hPx;
		availableWidthPx = awPx;
		availableHeightPx = ahPx;
		minWidthDps = minWidth;
		minHeightDps = minHeight;
		ComponentName cn = new ComponentName( context.getPackageName() , this.getClass().getName() );
		defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget( context , cn , null );
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
		{//自定义布局,开关打开
			isCustomLayoutNormalIcon = isCustomLayoutNormalIcon( resources , context );
			if( isCustomLayoutNormalIcon )
			{//小图标
				edgeMarginPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_edge_margin );
			}
			else
			{//大图标
				edgeMarginPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_edge_margin_big_icon );
			}
		}
		else
		{//开关关闭
			edgeMarginPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_edge_margin );
		}
		//cheyingkun add start	//主菜单图标缩放比。默认为1。
		allappsIconScale = Float.valueOf( LauncherDefaultConfig.getString( R.string.config_allapps_icon_scale ) );
		//cheyingkun add end
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		workspaceSpringLoadedBottomSpace = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_min_spring_loaded_space );
		int expectedWorkspaceHeight = availableHeightPx - hotseatBarHeightPx - pageIndicatorHeightPx;
		float minRequiredHeight = dropBarSpaceHeightPx + workspaceSpringLoadedBottomSpace;
		workspaceSpringLoadShrinkFactor = Math
				.min( LauncherDefaultConfig.getInt( R.integer.config_workspaceSpringLoadShrinkPercentage ) / 100.0f , 1 - ( minRequiredHeight / expectedWorkspaceHeight ) );
		//zhujieping add end
	}
	
	//cheyingkun add start	//和兴六部图标大小变化需求
	/**
	 * 是否是自定义布局小图标
	 * @param resources
	 * @param context
	 * @return
	 */
	boolean isCustomLayoutNormalIcon(
			Resources resources ,
			Context context )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
		{
			SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
			String mKey = LauncherDefaultConfig.getString( R.string.setting_key_app_icon_size );
			String mCurrentValue = defaultSharedPreferences.getString( mKey , "0" );
			//0 小图标
			if( "0".equals( mCurrentValue ) )
			{//小图标
				return true;
			}
			else
			{//大图标
				return false;
			}
		}
		else
		{//没打开开关
			return false;
		}
	};
	
	//cheyingkun add end
	void initWorkspaceConfigs(
			ArrayList<DeviceProfile> profiles ,
			Resources resources ,
			Boolean isReload )
	{
		celllayoutXPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_workspace_celllayout_x_padding );
		//【workspace左右边距】
		desiredWorkspaceLeftRightMarginPx = 2 * edgeMarginPx;
		//【workspace上下边距】
		desiredWorkspaceTopMarginPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_workspace_padding_top );
		desiredWorkspaceBottomMarginPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_workspace_padding_bottom );
		//cheyingkun add start	//自定义桌面布局
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
		{//自定义布局开关
			if( isCustomLayoutNormalIcon )
			{//小图标
				desiredWorkspaceTopMarginPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_workspace_padding_top );
			}
			else
			{//大图标
				desiredWorkspaceTopMarginPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_workspace_padding_top_big_icon );
			}
		}
		//cheyingkun add end	//自定义桌面布局
		ArrayList<DeviceProfileQuery> points = new ArrayList<DeviceProfileQuery>();
		//【workspace的行数】
		//cheyingkun add start	//桌面配置几行几列【c_0003360】
		int tempNumRows = LauncherDefaultConfig.getInt( R.integer.config_workspace_rows );
		if( tempNumRows > 0 )
		{
			numRows = tempNumRows;
		}
		else
		//cheyingkun add end
		{
			for( DeviceProfile p : profiles )
			{
				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.numRows ) );
			}
			numRows = Math.round( invDistWeightedInterpolate( minWidthDps , minHeightDps , points ) );
			points.clear();
		}
		//【workspace的列数】
		//cheyingkun add start	//桌面配置几行几列【c_0003360】
		int tempNumColumns = LauncherDefaultConfig.getInt( R.integer.config_workspace_columns );
		if( tempNumColumns > 0 )
		{
			numColumns = tempNumColumns;
		}
		else
		//cheyingkun add end
		{
			for( DeviceProfile p : profiles )
			{
				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.numColumns ) );
			}
			numColumns = Math.round( invDistWeightedInterpolate( minWidthDps , minHeightDps , points ) );
		}
		//【workspace中图标的宽高】
		// zhujieping@2015/04/01 UPDATE START
		//图标大小读取dimens.xml中配置的大小，类似魅族这种比较特殊的分辨率在res下新建文件夹values-1800x1080，然后dimens.xml中配置app_icon_size的大小
		//zhujieping del start
		//		points.clear();
		//		for( DeviceProfile p : profiles )
		//		{
		//			points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.iconSize ) );
		//		}
		//		iconSize = invDistWeightedInterpolate( minWidth , minHeight , points );
		//		iconSizePx = DynamicGrid.pxFromDp( iconSize , dm );
		//zhujieping del end
		//zhujieping add start
		if( isReload == false )
		{//isReload==true，则是重新计算相关参数。不要重新计算“iconSize”、“iconWidthSizePx”和“iconHeightSizePx”。
			TypedValue value = new TypedValue();
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{//自定义布局开关
				if( isCustomLayoutNormalIcon )
				{//小图标
					iconWidthSizePx = iconHeightSizePx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size );
					iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
				}
				else
				{//大图标
					iconWidthSizePx = iconHeightSizePx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_app_icon_size_big_icon );
					iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
				}
			}
			else
			{
				if( ThemeManager.getInstance() == null || ThemeManager.getInstance().currentThemeIsSystemTheme() )
				{
					iconWidthSizePx = iconHeightSizePx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size );
					iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
				}
				else
				{
					iconWidthSizePx = iconHeightSizePx = ThemeManager.getInstance().getInt( "app_icon_size" , LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size ) );
					iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
				}
			}
		}
		//zhujieping add end
		// zhujieping@2015/04/01 UPDATE END
		//【workspace中图标的名字的字体大小】
		try
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{
				if( isCustomLayoutNormalIcon )
				{//小图标
					iconTextSize = Float.parseFloat( LauncherDefaultConfig.getString( R.string.config_icon_text_size ) );
				}
				else
				{//大图标
					iconTextSize = Float.parseFloat( LauncherDefaultConfig.getString( R.string.config_icon_text_size_big_icon ) );
				}
			}
			else
			{
				iconTextSize = Float.parseFloat( LauncherDefaultConfig.getString( R.string.config_icon_text_size ) );
			}
			if( iconTextSize < 0 )
			{
				iconTextSize = -iconTextSize;
			}
		}
		catch( NumberFormatException e )
		{
			points.clear();
			for( DeviceProfile p : profiles )
			{
				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.iconTextSize ) );
			}
			iconTextSize = invDistWeightedInterpolate( minWidthDps , minHeightDps , points );
		}
		DisplayMetrics dm = resources.getDisplayMetrics();
		iconTextSizePx = DynamicGrid.pxFromSp( iconTextSize , dm );
		//Calculate the single cell Width and Height
		//【workspace中每一个格子的宽高】
		Paint textPaint = new Paint();
		textPaint.setTextSize( iconTextSizePx );
		FontMetrics fm = textPaint.getFontMetrics();
		//xiatian start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
		//		cellWidthPx = iconWidthSizePx;//xiatian del
		//xiatian add start
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{//自定义布局开关
				if( isCustomLayoutNormalIcon )
				{//小图标
					itemPaddingXInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_x_padding );
					itemPaddingTopInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_top_padding );
					itemPaddingBottomInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_bottom_padding );
				}
				else
				{//大图标
					itemPaddingXInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_x_padding_big_icon );
					itemPaddingTopInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_top_padding_big_icon );
					itemPaddingBottomInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_bottom_padding_big_icon );
				}
			}
			else
			{
				itemPaddingXInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_x_padding );
				itemPaddingTopInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_top_padding );
				itemPaddingBottomInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_icon_bottom_padding );
			}
		}
		cellWidthPx = iconWidthSizePx + 2 * itemPaddingXInCell;
		//xiatian add end
		//xiatian end
		//cheyingkun start	//图标名称和文件夹名称的文字显示行数（需要同步修改“WorkspaceIcon”，详见“Phenix桌面“修改配置”说明.pdf”）。
		//		int textLines = 1;//cheyingkun del
		int textLines = LauncherDefaultConfig.getInt( R.integer.config_icon_text_lines );//cheyingkun add
		//cheyingkun end
		defaultGapBetweenIconAndText = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_default_gap_between_icon_and_text );//cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(读取默认配置)【c_0004390】
		//xiatian start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
		//		cellHeightPx = iconHeightSizePx + (int)Math.ceil( fm.bottom - fm.top ) * textLines;//xiatian del
		//xiatian add start
		cellHeightPx = iconHeightSizePx + (int)Math.ceil( fm.bottom - fm.top ) * textLines + itemPaddingTopInCell + itemPaddingBottomInCell;
		//cheyingkun add start	//自定义桌面布局
		//cheyingun add end
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			//cheyingkun add start	//自定义桌面布局
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{//自定义布局开关
				if( isCustomLayoutNormalIcon )
				{//小图标
					cellWidthGapPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_small_icon );
					cellHeightPx += defaultGapBetweenIconAndText;
				}
				else
				{//大图标
					cellWidthGapPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_big_icon );
					cellHeightPx += LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_icon_padding_gop_text_and_icon_big_icon );
				}
			}
			else
			//cheyingkun add end
			{
				int mWorkSpaceWidth = widthPx - ( desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left ) - ( desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.right );
				cellWidthGapPx = -1;// (int)( ( mWorkSpaceWidth - cellWidthPx * numColumns ) / ( numColumns - 1 ) );
				cellHeightPx += defaultGapBetweenIconAndText;//cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(桌面格子高度)【c_0004390】
			}
		}
		//xiatian add end
		//xiatian end
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			iconHeightSizePx = cellHeightPx;
		}
		//xiatian add end
		//【workspace中文件夹相关】
		//【workspace中文件夹中每个格子的宽高】
		folderCellWidthPx = cellWidthPx + 3 * edgeMarginPx;
		folderCellHeightPx = cellHeightPx + (int)( ( 3f / 2f ) * edgeMarginPx );
		// zhujieping@2015/03/17 UPD START
		//文件夹大小改为跟普通icon一样大，offset值为0
		//folderBackgroundOffset = -edgeMarginPx;
		folderBackgroundOffset = 0;
		// zhujieping@2015/03/17 UPD END
		//【workspace中文件夹中每个图标的宽高】
		folderIconWidthSizePx = iconWidthSizePx + 2 * -folderBackgroundOffset;
		folderIconHeightSizePx = iconHeightSizePx + 2 * -folderBackgroundOffset;
		//cheyingkun add start	//整理完善图标检测功能【c_0004366】
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "" , StringUtils.concat( "cyk DeviceProfile initWorkspaceConfigs cellHeightPx:" , cellHeightPx , "-cellWidthPx:" , cellWidthPx ) );
		}
		//cheyingkun add end
	}
	
	void initPageIndicatorConfigs(
			Resources resources )
	{
		//【pageIndicator的高度】
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			pageIndicatorYPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_page_indicator_y_padding );
			pageIndicatorHeightPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_page_indicator_height ) + pageIndicatorYPadding * 2;
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			pageIndicatorHeightPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_dynamic_grid_page_indicator_height );
		}
		//xiatian add end
	}
	
	void initHotseatConfigs(
			ArrayList<DeviceProfile> profiles ,
			Resources resources )
	{
		ArrayList<DeviceProfileQuery> points = new ArrayList<DeviceProfileQuery>();
		int tempNumColumns = 0;
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			tempNumColumns = LauncherDefaultConfig.getInt( R.integer.config_hotseat_columns );
		}
		//cheyingkun add start	//单双层分开配置(底边栏)
		else
		{
			tempNumColumns = LauncherDefaultConfig.getInt( R.integer.config_hotseat_columns_double );
		}
		//cheyingkun add end
		//【hotseat中显示图标的列数】
		//xiatian add start	//配置底边栏显示图标的列数。 
		if( tempNumColumns > 0 )
		{
			numHotseatIcons = tempNumColumns;
		}
		else
		//xiatian add end
		{
			for( DeviceProfile p : profiles )
			{
				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.numHotseatIcons ) );
			}
			numHotseatIcons = Math.round( invDistWeightedInterpolate( minWidthDps , minHeightDps , points ) );
		}
		//【hotseat中图标的宽高】
		//Interpolate the hotseat icon size
		//Hotseat
		//WangLei add start //bug:c_0003022 改变dimens.xml中控制图标大小的参数app_icon_size，底边栏图标的大小没有随之 改变
		//【原因】下面获取hotseatIconSize的方法得到的值在不同的分辨率下都是一样，在CellLayout里会使用hotseatIconSize/iconSize得到底边栏的缩放值
		//CellLayout里addView时，会根据父View是否是底边栏给子View设置不同的scale值，导致改变app_icon_size对底边栏图标的大小没有影响
		//【解决方案】添加一个配置选项switch_enable_hotseatIconsize_sameAs_iconSize，为true时，hoeseatIconSize和iconSize相同，为false，使用原来的方法获取
		boolean hotseatIconSize_sameAs_iconSize = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_hotseatIconsize_sameAs_iconSize );
		if(
		//
		hotseatIconSize_sameAs_iconSize
		//
		|| LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE //xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//
		)
		{
			hotseatIconSize = iconSize;//从而CellLayout的mHotseatScale为1
		}
		else
		//WangLei add end
		{
			points.clear();
			for( DeviceProfile p : profiles )
			{
				points.add( new DeviceProfileQuery( p.minWidthDps , p.minHeightDps , p.hotseatIconSize ) );
			}
			hotseatIconSize = invDistWeightedInterpolate( minWidthDps , minHeightDps , points );
		}
		//【hotseat中主菜单入口的位置】
		//cheyingkun add start	//配置双层模式下主菜单图标位置。可配置-1（自适应居中）、0（底边栏第一个图标）、1（底边栏第二个图标）等等（必须小于底边栏列数config_hotseat_columns_double）。当配置为-1时：底边栏列数为奇数则为居中，底边栏列数为偶数则第“底边栏列数除以2，再加1”个图标。【c_0004381】
		//zhujieping add start//需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		if(
		//
		( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//
				&& LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE != LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT //zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		//
		)
		{
			hotseatAllAppsRank = (int)numHotseatIcons;
		}
		else
		//zhujieping add end
		{
			int defaultAllAppsRank = LauncherDefaultConfig.getInt( R.integer.config_allapps_position_in_hotseat );
			if( defaultAllAppsRank < 0 || defaultAllAppsRank > numHotseatIcons )
			{
				hotseatAllAppsRank = (int)( numHotseatIcons / 2 );
			}
			else
			{
				hotseatAllAppsRank = defaultAllAppsRank;
			}
		}
		//cheyingkun add end
		//【hotseat中的每个格子的宽高】
		hotseatCellWidthPx = iconWidthSizePx;
		//cheyingkun add start	//自定义桌面布局
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
		{//自定义布局开关
			if( isCustomLayoutNormalIcon )
			{//小图标
				hotseatBarToppadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_top_padding );
				hotseatBarBottompadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_bottom_padding );
			}
			else
			{//大图标
				hotseatBarBottompadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_bottom_padding_big_icon );
				hotseatBarToppadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_top_padding_big_icon );
			}
		}
		//cheyingkun add end
		else
		{
			hotseatBarToppadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_top_padding );
			hotseatBarBottompadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_bottom_padding );
		}
		hotseatItemPaddingTopInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_icon_top_padding );
		hotseatItemPaddingBottomInCell = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_icon_bottom_padding );
		if( LauncherDefaultConfig.SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE )
		{
			//cellHeightPx是桌面图标的高度（包括itemPaddingBottomInCell和itemPaddingTopInCell），底边栏的单独配置
			hotseatCellHeightPx = ( cellHeightPx - itemPaddingBottomInCell + hotseatItemPaddingBottomInCell - itemPaddingTopInCell + hotseatItemPaddingTopInCell ) + hotseatBarToppadding + hotseatBarBottompadding;
		}
		else
		{
			hotseatCellHeightPx = iconHeightSizePx //
					+ hotseatBarToppadding + hotseatBarBottompadding + hotseatItemPaddingTopInCell + hotseatItemPaddingTopInCell;//cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(底边栏不显示文字的高度=icon+padding)【c_0004390】
		}
		//【hotseat中的最上层（根节点）控件的高度】
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		hotseatBarHeightPx = iconHeightSizePx + 4 * edgeMarginPx;//xiatian del
		//xiatian add start
		hotseatBarHeightPx = 0;
		hotseatBarXpadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_hotseat_x_padding );
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			//cheyingkun start	//默认图标样式下,添加图标和文字之间的间距配置(底边栏高度和格子高度一样高,不额外添加边距)【c_0004390】
			//cheyingkun del start
			//			//cheyingkun add start	//自定义桌面布局
			//			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			//			{
			//				hotseatBarHeightPx = hotseatCellHeightPx;
			//			}
			//			else
			//			//cheyingkun add end
			//			{
			//				hotseatBarHeightPx = hotseatCellHeightPx + 4 * edgeMarginPx;
			//			}
			//cheyingkun del end
			hotseatBarHeightPx = hotseatCellHeightPx;//cheyingkun add
			//cheyingkun end
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			hotseatBarHeightPx = getSignleViewAvailableHeightPx();
		}
		//xiatian add end
		//xiatian end
	}
	
	void initSearchDropTargetBarConfigs(
			Resources resources )
	{
		//【searchBar的最大宽度】
		searchBarSpaceMaxWidthPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_search_bar_max_width );
		//【searchBar的实际宽度】
		searchBarSpaceWidthPx = Math.min( searchBarSpaceMaxWidthPx , widthPx );
		//【searchBar的最大高度和实际高度】
		searchBarXPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_search_bar_x_padding );
		searchBarTopPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_search_bar_top_padding );
		searchBarBottomPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_search_bar_bottom_padding );
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			searchBarHeightPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_search_bar_height );
			//			searchBarSpaceHeightPx = searchBarHeightPx + 2 * edgeMarginPx;
			searchBarSpaceHeightPx = searchBarHeightPx + searchBarTopPadding + searchBarBottomPadding;
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			searchBarHeightPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_dynamic_grid_search_bar_height );
			int edgeMarginPxWhenItemStyle1 = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_dynamic_grid_edge_margin );
			searchBarSpaceHeightPx = searchBarHeightPx + 2 * edgeMarginPxWhenItemStyle1;
		}
		//xiatian add end
	}
	
	void initDropBarConfigs(
			Resources resources )
	{
		//【dropBar的实际高度】
		dropBarSpaceHeightPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_drop_bar_height );
	}
	
	void initAppsCustomizeConfigs(
			Resources resources )
	{
		//【appsCustomize的布局配置参数】
		appsCelllayoutXPadding = LauncherDefaultConfig.getIntDimension( R.dimen.dynamic_grid_apps_customize_celllayout_x_padding );
		appsedgeMarginPx = LauncherDefaultConfig.getIntDimension( R.dimen.dynamic_grid_apps_edge_margin );
		//【appsCustomize的行列数】
		//cheyingkun add start	//主菜单支持配置行数（修改主菜单排序算法，支持以下三种情况：1、同时配置行列；2、只配行或者只配列；3、行列都不配)
		int defaultAppListNumCols = LauncherDefaultConfig.getInt( R.integer.config_applist_rows );
		if( defaultAppListNumCols > 0 )
		{
			allAppsNumCols = defaultAppListNumCols;
		}
		else
		//cheyingkun add end
		{
			Rect padding = getWorkspacePadding( CellLayout.PORTRAIT );
			allAppsNumCols = ( getAvailableWidthPx() - padding.left - padding.right - 2 * appsedgeMarginPx ) / ( cellWidthPx );
		}
		//cheyingkun add start	//主菜单支持配置列数（修改主菜单排序算法，支持以下三种情况：1、同时配置行列；2、只配行或者只配列；3、行列都不配)
		int defaultAppListNumRows = LauncherDefaultConfig.getInt( R.integer.config_applist_columns );
		if( defaultAppListNumRows > 0 )
		{
			allAppsNumRows = defaultAppListNumRows;
		}
		else
		//cheyingkun add end
		{
			allAppsNumRows = (int)numRows + 1;
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//整理代码：“桌面相关控件的布局”相关。
	void layoutSearchBar(
			Launcher launcher )
	{
		//cheyingkun add start	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		if( !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
		{
			return;
		}
		//cheyingkun add end
		View mSearchBar = launcher.getSearchBar();
		if( mSearchBar != null && ( mSearchBar instanceof AppWidgetHostView ) == false )//xiatian add	//适配5.1全局搜索（5.1的全局搜索是将AppWidgetHostView加到mSearchDropTargetBar中），5.1以下的全局搜索机制通不过5.1系统的cts。
		{
			LayoutParams vglp = mSearchBar.getLayoutParams();
			vglp.width = LayoutParams.WRAP_CONTENT;
			vglp.height = LayoutParams.MATCH_PARENT;
			mSearchBar.setLayoutParams( vglp );
		}
	}
	
	void layoutSearchDropTargetBar(
			Launcher launcher )
	{
		View mSearchDropTargetBar = launcher.getSearchDropTargetBar();
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)mSearchDropTargetBar.getLayoutParams();
		int statusBarHeight = launcher.getStatusBarHeight( false );
		// zhangjin@2016/05/11 ADD START
		if( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW
		//
		|| LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT )//zhujieping add start //拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。
		{
			statusBarHeight = 0;
		}
		// zhangjin@2016/05/11 ADD END
		{
			// Horizontal search bar
			lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			lp.width = searchBarSpaceWidthPx;
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
			{
				//xiatian start	//需求：默认显示搜索栏并且使用全局搜索前提下，若手机中没有支持全局搜索的应用，则切换为酷搜。
				//不再动态改变参数“searchBarHeightPx”的值
				//				lp.height = searchBarHeightPx;//xiatian del
				lp.height = searchBarSpaceHeightPx + statusBarHeight;//xiatian add
				//xiatian end
				int top = 0;
				int bottom = 0;//xiatian add	//让searchBar居中
				//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
				{
					//xiatian start	//让searchBar居中
					//xiatian del start
					//					top = 2 * edgeMarginPx + statusBarHeight;
					//					bottom = 0;
					//xiatian del end
					//xiatian add start
					top = searchBarTopPadding + statusBarHeight;
					bottom = searchBarBottomPadding;
					//xiatian add end
					//xiatian end
					mSearchDropTargetBar.setPadding( searchBarXPadding , top , searchBarXPadding , bottom );
				}
				else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
				{
					top = statusBarHeight;
					bottom = 0;
					mSearchDropTargetBar.setPadding( 2 * edgeMarginPx , top , 2 * edgeMarginPx , bottom );
				}
				//xiatian add end
			}
			else
			{
				if( LauncherAppState.getInstance().isVirtualMenuShown() )
					lp.setMargins( 0 , -launcher.getStatusBarHeight( true ) , 0 , 0 );
				lp.height = searchBarHeightPx + 2 * edgeMarginPx;
				mSearchDropTargetBar.setPadding( 2 * edgeMarginPx , edgeMarginPx , 2 * edgeMarginPx , edgeMarginPx );
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
				if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
				{
					View searchbar = launcher.getSearchBar();
					if( searchbar != null )
					{
						FrameLayout.LayoutParams vglp = (android.widget.FrameLayout.LayoutParams)searchbar.getLayoutParams();
						vglp.width = FrameLayout.LayoutParams.MATCH_PARENT;
						vglp.height = FrameLayout.LayoutParams.MATCH_PARENT;
						vglp.setMargins(
								mSearchDropTargetBar.getPaddingLeft() ,
								mSearchDropTargetBar.getPaddingTop() - statusBarHeight ,
								mSearchDropTargetBar.getPaddingRight() ,
								mSearchDropTargetBar.getPaddingBottom() );
					}
					mSearchDropTargetBar.setPadding( 0 , statusBarHeight , 0 , 0 );
				}
				else
				{
					mSearchDropTargetBar.setPadding( 0 , 0 , 0 , 0 );
				}
			}
		}
		mSearchDropTargetBar.setLayoutParams( lp );
	}
	
	void layoutWorkspace(
			Launcher launcher )
	{
		View workspace = launcher.findViewById( R.id.workspace );
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)workspace.getLayoutParams();
		lp.gravity = Gravity.CENTER;
		Rect padding = getWorkspacePadding( CellLayout.PORTRAIT );
		workspace.setPadding( padding.left , padding.top , padding.right , padding.bottom );
		workspace.setLayoutParams( lp );
	}
	
	void layoutHotseat(
			Launcher launcher )
	{
		View hotseat = launcher.findViewById( R.id.hotseat );
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)hotseat.getLayoutParams();
		// For phones, layout the hotseat without any bottom margin
		// to ensure that we have space for the folders
		lp.gravity = Gravity.BOTTOM;
		lp.width = LayoutParams.MATCH_PARENT;
		int mHotseatPaddingLeft = 0;
		int mHotseatPaddingRight = 0;
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//			lp.height = hotseatBarHeightPx;//xiatian del
		//xiatian add start
		int mHotseatLPHeight = 0;
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			mHotseatLPHeight = hotseatBarHeightPx;
			//cheyingkun add start	//自定义桌面布局
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{
				Rect mWorkspacePadding = getWorkspacePadding( CellLayout.PORTRAIT );
				mHotseatPaddingLeft = mWorkspacePadding.left;
				mHotseatPaddingRight = mWorkspacePadding.right;
			}
			else
			//cheyingkun add end
			{
				mHotseatPaddingLeft = hotseatBarXpadding;
				mHotseatPaddingRight = hotseatBarXpadding;
			}
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//底边栏高度（确保view显示完整）
			mHotseatLPHeight = getSignleViewAvailableHeightPx();
			Rect mWorkspacePadding = getWorkspacePadding( CellLayout.PORTRAIT );
			mHotseatPaddingLeft = mWorkspacePadding.left;
			mHotseatPaddingRight = mWorkspacePadding.right;
		}
		lp.height = mHotseatLPHeight;
		//xiatian add end
		//xiatian end
		hotseat.findViewById( R.id.layout ).setPadding( mHotseatPaddingLeft , hotseatBarToppadding , mHotseatPaddingRight , hotseatBarBottompadding );
		hotseat.setLayoutParams( lp );
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//重新布局
			hotseat.requestLayout();
		}
		//xiatian add end
	}
	
	void layoutPageIndicator(
			Launcher launcher )
	{
		View pageIndicator = launcher.findViewById( R.id.page_indicator );
		if( pageIndicator != null )
		{
			// Put the page indicators above the hotseat
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)pageIndicator.getLayoutParams();
			lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
			lp.width = LayoutParams.WRAP_CONTENT;
			lp.height = LayoutParams.WRAP_CONTENT;
			//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			//				lp.bottomMargin = hotseatBarHeightPx;//xiatian del
			//xiatian add start
			int mPageIndicatorLPBottomMargin = 0;//页面指示器底边距
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				mPageIndicatorLPBottomMargin = hotseatBarHeightPx + pageIndicatorYPadding;
				//					lp.topMargin = pageIndicatorYPadding;
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				mPageIndicatorLPBottomMargin = getSignleViewAvailableHeightPx();
			}
			//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机
			float offsetY = LauncherDefaultConfig.getFloatDimension( R.dimen.config_item_style_1_pageIndicator_offset_y );//cheyingkun add	//飞利浦图标样式适配480*854带虚拟按键手机(调整布局、替换图标)。【c_0003557】
			mPageIndicatorLPBottomMargin -= offsetY;
			//cheyingkun add end
			//zhujieping modify start,带导航栏的手机，底层会给bottomMargin自动再加上导航栏的高度，换主题不重启时，再次设置时bottomMargin会导致indicator的位置不对，所以这边设置padding,效果是一样的
			//			lp.bottomMargin = mPageIndicatorLPBottomMargin;
			pageIndicator.setPadding( 0 , 0 , 0 , mPageIndicatorLPBottomMargin );
			//zhujieping modify end
			//xiatian add end
			//xiatian end
			pageIndicator.setLayoutParams( lp );
			//xiatian add start	//需求：配置项“dynamic_grid_page_indicator_height”支持本地化。
			View mPageIndicatorNormal = pageIndicator.findViewById( R.id.pageIndicatorNormal );
			if( mPageIndicatorNormal != null )
			{
				FrameLayout.LayoutParams mPageIndicatorNormalLP = (FrameLayout.LayoutParams)mPageIndicatorNormal.getLayoutParams();
				mPageIndicatorNormalLP.height = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_page_indicator_height );
				mPageIndicatorNormal.setLayoutParams( mPageIndicatorNormalLP );
			}
			View mPageIndicatorCaret = pageIndicator.findViewById( R.id.pageIndicatorCaret );
			if( mPageIndicatorCaret != null )
			{
				FrameLayout.LayoutParams mPageIndicatorCaretLP = (FrameLayout.LayoutParams)mPageIndicatorCaret.getLayoutParams();
				mPageIndicatorCaretLP.height = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.dynamic_grid_page_indicator_height );
				mPageIndicatorCaret.setLayoutParams( mPageIndicatorCaretLP );
			}
			//xiatian add end
		}
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//解决“文件夹推荐应用名称显示不全”的问题。【i_0013225】
	public int getItemPaddingTopInCell()
	{
		return itemPaddingTopInCell;
	}
	
	public int getItemPaddingBottomInCell()
	{
		return itemPaddingBottomInCell;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//整理完善图标检测功能【c_0004366】
	/**
	 * 检查workspace、AppsCustomize图标显示大小
	 * @param profiles
	 * @param resources
	 */
	private void checkWorkspaceAndAppsCustomizeConfig(
			ArrayList<DeviceProfile> profiles ,
			Resources resources )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( "" , StringUtils.concat( "cyk DeviceProfile checkWorkspaceConfig iconSize:" , iconSize , "-iconWidthSizePx:" , iconWidthSizePx ) );
		}
		//mIsNeedAdjustConfig
		//【备注】
		//	1、AdjustConfig需要numRows、cellHeightPx和getWorkspacePadding三个参数，
		//	2、getWorkspacePadding需要searchBarHeightPx、edgeMarginPx、hotseatBarHeightPx、widthPx、heightPx、numColumns、cellWidthPx、pageIndicatorHeightPx、availableHeightPx、desiredWorkspaceLeftRightMarginPx和defaultWidgetPadding
		//	3、由于1和2，所以AdjustConfig的相关操作放在initCommonConfigs、initSearchBarConfigs、initPageIndicatorConfigs、initWorkspaceConfigs和initHotseatConfigs和initAppsCustomizeConfigs之后
		//	4、由于AdjustConfig涉及到“iconSize”、“iconWidthSizePx”和“iconHeightSizePx”三个参数，所以要重新initWorkspaceConfigs和initHotseatConfigs和initAppsCustomizeConfigs
		//	5、由于initDropBarConfigs和AdjustConfig无关，故放到最后
		int workspaceSize = checkWorkspaceConfig();
		int appsCustomizeConfigSize = checkAppsCustomizeConfig( resources );
		//如果workspace或者appsCustomize超出,那么缩小图标大小.保证显示不出问题
		if( workspaceSize > 0 || appsCustomizeConfigSize > 0 )
		{
			//缩小图标大小
			iconWidthSizePx = iconHeightSizePx -= Math.max( workspaceSize , appsCustomizeConfigSize );
			DisplayMetrics dm = resources.getDisplayMetrics();
			iconSize = DynamicGrid.dpiFromPx( iconHeightSizePx , dm );
			//重新计算相关参数
			initWorkspaceConfigs( profiles , resources , true );
			initAppsCustomizeConfigs( resources );
			initHotseatConfigs( profiles , resources );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.e( "" , StringUtils.concat( "cyk DeviceProfile checkWorkspaceConfig iconSize:" , iconSize , "-iconWidthSizePx:" , iconWidthSizePx ) );
		}
	}
	
	/**
	 * 检测workspace空间是否足够显示
	 * @return 返回单个cell超出范围的大小
	 */
	private int checkWorkspaceConfig()
	{
		int deltaWPx = 0;
		int deltaHPx = 0;
		Rect padding = getWorkspacePadding( CellLayout.PORTRAIT );
		//检测高度
		int h = (int)( numRows * cellHeightPx ) + padding.top + padding.bottom;
		if( h > availableHeightPx )
		{
			float delta = h - availableHeightPx;//需要的高度比屏幕高度高多少
			deltaHPx = (int)Math.ceil( delta / ( numRows + 1 ) );//这里+1因为平分差值的时候需要计算底边栏
		}
		//cheyingkun add start	//自定义桌面布局
		//检测宽度
		int w = (int)( numColumns * cellWidthPx ) + padding.left + padding.right;
		if( w > availableWidthPx )
		{
			float delta = w - availableWidthPx;
			deltaWPx = (int)Math.ceil( delta / numColumns );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "" , StringUtils.concat(
					"cyk DeviceProfile checkWorkspaceConfig h:" ,
					h ,
					"-w:" ,
					w ,
					"-availableHeightPx:" ,
					availableHeightPx ,
					"-availableWidthPx:" ,
					availableWidthPx ,
					"-deltaWPx: " ,
					deltaWPx ,
					"-deltaHPx " ,
					deltaHPx ) );
		}
		//cheyingkun add end
		return Math.max( deltaWPx , deltaHPx );
	}
	
	/**
	 * 检测AppsCustomize空间是否足够显示
	 * @param resources 
	 * @return 返回单个cell超出范围的大小
	 */
	private int checkAppsCustomizeConfig(
			Resources resources )
	{
		//单层模式,返回0
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			return 0;
		}
		//双层模式,检测主菜单宽高
		int deltaWPx = 0;
		int deltaHPx = 0;
		Rect padding = getAppsCustomizePagedViewPadding();
		//状态栏
		int mStatusBarHeight = LauncherAppState.getInstance().getStatusBarHeight();
		//主菜单界面整体布局的底边距
		float pageIndicatorOffsetBottom = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.apps_customize_page_indicator_offset_bottom_apps );
		float pageIndicatorOffsetTop = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.apps_customize_page_indicator_offset_top_apps );
		//检测高度
		float h = (int)( allAppsNumRows * cellHeightPx * allappsIconScale ) + padding.top + padding.bottom//
				+ pageIndicatorOffsetBottom + pageIndicatorOffsetTop + mStatusBarHeight;
		if( h > availableHeightPx )
		{
			float delta = h - availableHeightPx;
			deltaHPx = (int)Math.ceil( delta / allAppsNumRows / allappsIconScale );
		}
		//检测宽度
		int w = (int)( allAppsNumCols * cellWidthPx * allappsIconScale ) + padding.left + padding.right;
		if( w > availableWidthPx )
		{
			float delta = w - availableWidthPx;
			deltaWPx = (int)Math.ceil( delta / allAppsNumCols / allappsIconScale );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "" , StringUtils.concat(
					"cyk DeviceProfile checkAppsCustomizeConfig h:" ,
					h ,
					"-w:" ,
					w ,
					"-availableHeightPx:" ,
					availableHeightPx ,
					"-availableWidthPx:" ,
					availableWidthPx ,
					"-deltaWPx:" ,
					deltaWPx ,
					"-deltaHPx:" ,
					deltaHPx ,
					"-allAppsNumRows:" ,
					allAppsNumRows ,
					"-allAppsNumCols:" ,
					allAppsNumCols ) );
		}
		return Math.max( deltaWPx , deltaHPx );
	}
	
	/**
	 * 获取主菜单界面内边距（主菜单界面外边距直接在xml文件里设置，计算高度时需要手动计算）
	 * @return
	 */
	public Rect getAppsCustomizePagedViewPadding()
	{
		Rect padding = new Rect();
		padding.set( appsedgeMarginPx , 2 * appsedgeMarginPx , appsedgeMarginPx , 2 * appsedgeMarginPx );
		return padding;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
	public void initPageIndicatorY(
			Launcher launcher )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR )
		{
			View pageIndicator = launcher.findViewById( R.id.page_indicator );
			float mPageIndicatorYOffsetInEditMode = LauncherDefaultConfig.getFloatDimension( R.dimen.config_overview_pageIndicator_y_offset );
			setPageIndicatorYInOverviewMode( pageIndicator.getY() - mPageIndicatorYOffsetInEditMode );
			setPageIndicatorYInNormal( pageIndicator.getY() );
		}
	}
	
	public float getPageIndicatorYInOverviewMode()
	{
		return pageIndicatorYInOverviewMode;
	}
	
	public void setPageIndicatorYInOverviewMode(
			float pageIndicatorYInOverviewMode )
	{
		this.pageIndicatorYInOverviewMode = pageIndicatorYInOverviewMode;
	}
	
	public float getPageIndicatorYInNormal()
	{
		return pageIndicatorYInNormal;
	}
	
	public void setPageIndicatorYInNormal(
			float pageIndicatorYInNormal )
	{
		this.pageIndicatorYInNormal = pageIndicatorYInNormal;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//主菜单图标缩放比。默认为1。
	public float getAllappsIconScale()
	{
		return allappsIconScale;
	}
	//cheyingkun add end
	;
	
	public float getHotseatIconSize()
	{
		return hotseatIconSize;
	}
	
	public float getIconSize()
	{
		return iconSize;
	}
	
	public int getCelllayoutXPadding()
	{
		return celllayoutXPadding;
	}
	
	public int getAppsCelllayoutXPadding()
	{
		return appsCelllayoutXPadding;
	}
	
	public float getMinWidthDps()
	{
		return minWidthDps;
	}
	
	public float getMinHeightDps()
	{
		return minHeightDps;
	}
	
	public float getNumHotseatIcons()
	{
		return numHotseatIcons;
	}
	
	public int getHotseatAllAppsRank()
	{
		return hotseatAllAppsRank;
	}
	
	public int getSearchBarSpaceHeightPx()
	{
		return searchBarSpaceHeightPx;
	}
	
	public int getHotseatCellWidthPx()
	{
		return hotseatCellWidthPx;
	}
	
	public int getHotseatCellHeightPx()
	{
		return hotseatCellHeightPx;
	}
	
	public int getHotseatBarToppadding()
	{
		return hotseatBarToppadding;
	}
	
	public int getHotseatBarBottompadding()
	{
		return hotseatBarBottompadding;
	}
	
	public int getHotseatItemPaddingBottomInCell()
	{
		return hotseatItemPaddingBottomInCell;
	}
	
	public int getHotseatItemPaddingTopInCell()
	{
		return hotseatItemPaddingTopInCell;
	}
	
	//zhujieping add start,换主题时重新获取icon大小,这个方法是截取initworkspaceconfig方法中跟icon大小相关的修改
	private void reloadWorkspaceConfig(
			Resources resources )
	{
		//isReload==true，则是重新计算相关参数。不要重新计算“iconSize”、“iconWidthSizePx”和“iconHeightSizePx”。
		TypedValue value = new TypedValue();
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
		{//自定义布局开关
			if( isCustomLayoutNormalIcon )
			{//小图标
				iconWidthSizePx = iconHeightSizePx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size );
				iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
			}
			else
			{//大图标
				iconWidthSizePx = iconHeightSizePx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_app_icon_size_big_icon );
				iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
			}
		}
		else
		{
			if( ThemeManager.getInstance() == null || ThemeManager.getInstance().currentThemeIsSystemTheme() )
			{
				iconWidthSizePx = iconHeightSizePx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size );
				iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
			}
			else
			{
				iconWidthSizePx = iconHeightSizePx = ThemeManager.getInstance().getInt( "app_icon_size" , LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size ) );
				iconSize = DynamicGrid.dpiFromPx( iconWidthSizePx , resources.getDisplayMetrics() );
			}
		}
		Paint textPaint = new Paint();
		textPaint.setTextSize( iconTextSizePx );
		FontMetrics fm = textPaint.getFontMetrics();
		cellWidthPx = iconWidthSizePx + 2 * itemPaddingXInCell;
		int textLines = LauncherDefaultConfig.getInt( R.integer.config_icon_text_lines );//cheyingkun add
		cellHeightPx = iconHeightSizePx + (int)Math.ceil( fm.bottom - fm.top ) * textLines + itemPaddingTopInCell + itemPaddingBottomInCell;
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{//自定义布局开关
				if( isCustomLayoutNormalIcon )
				{//小图标
					cellWidthGapPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_small_icon );
					cellHeightPx += defaultGapBetweenIconAndText;
				}
				else
				{//大图标
					cellWidthGapPx = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_cell_width_gap_big_icon );
					cellHeightPx += LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_icon_padding_gop_text_and_icon_big_icon );
				}
			}
			else
			{
				cellWidthGapPx = -1;// (int)( ( mWorkSpaceWidth - cellWidthPx * numColumns ) / ( numColumns - 1 ) );
				cellHeightPx += defaultGapBetweenIconAndText;//cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(桌面格子高度)【c_0004390】
			}
		}
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			iconHeightSizePx = cellHeightPx;
		}
		folderCellWidthPx = cellWidthPx + 3 * edgeMarginPx;
		folderCellHeightPx = cellHeightPx + (int)( ( 3f / 2f ) * edgeMarginPx );
		folderBackgroundOffset = 0;
		folderIconWidthSizePx = iconWidthSizePx + 2 * -folderBackgroundOffset;
		folderIconHeightSizePx = iconHeightSizePx + 2 * -folderBackgroundOffset;
	}
	
	private void reloadHotseatConfig(
			Resources resources )
	{
		boolean hotseatIconSize_sameAs_iconSize = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_hotseatIconsize_sameAs_iconSize );
		if(
		//
		hotseatIconSize_sameAs_iconSize
		//
		|| LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE //xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//
		)
		{
			hotseatIconSize = iconSize;//从而CellLayout的mHotseatScale为1
		}
		hotseatCellWidthPx = iconWidthSizePx;
		if( LauncherDefaultConfig.SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE )
		{
			hotseatCellHeightPx = ( cellHeightPx - itemPaddingBottomInCell + hotseatItemPaddingBottomInCell - itemPaddingTopInCell + hotseatItemPaddingTopInCell ) + hotseatBarToppadding + hotseatBarBottompadding;
		}
		else
		{
			hotseatCellHeightPx = iconHeightSizePx //
					+ hotseatBarToppadding + hotseatBarBottompadding + hotseatItemPaddingTopInCell + hotseatItemPaddingTopInCell;//cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(底边栏不显示文字的高度=icon+padding)【c_0004390】
		}
		hotseatBarHeightPx = 0;
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			hotseatBarHeightPx = hotseatCellHeightPx;
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			hotseatBarHeightPx = getSignleViewAvailableHeightPx();
		}
	}
	
	@Override
	public void onThemeChanged(
			Object arg0 ,
			final Object arg1 )
	{
		// TODO Auto-generated method stub
		if( !( arg0 instanceof Resources ) )
		{
			return;
		}
		Resources resources = (Resources)arg0;
		reloadWorkspaceConfig( resources );
		reloadHotseatConfig( resources );
		if( !LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )//cheyingkun add	//自定义桌面布局
		{
			checkWorkspaceAndAppsCustomizeConfig( mProfiles , resources );
		}
		if( arg1 instanceof Launcher )
		{
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				Utilities.setIconSize( getIconWidthSizePx() , getIconHeightSizePx() );
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				Utilities.setIconSize( getSignleViewAvailableWidthPx() , getSignleViewAvailableHeightPx() );
			}
			( (Launcher)arg1 ).runOnUiThread( new Runnable() {
				
				public void run()
				{
					layoutWorkspace( (Launcher)arg1 );
					layoutHotseat( (Launcher)arg1 );
					layoutPageIndicator( (Launcher)arg1 );
				}
			} );
		}
	}
	
	//zhujieping add end
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public float getWorkspaceSpringLoadShrinkFactor()
	{
		return workspaceSpringLoadShrinkFactor;
	}
	
	public int getDropBarSpaceHeightPx()
	{
		return dropBarSpaceHeightPx;
	}
	
	public int getWorkspaceSpringLoadedBottomSpace()
	{
		return workspaceSpringLoadedBottomSpace;
	}
	//zhujieping add end
}
