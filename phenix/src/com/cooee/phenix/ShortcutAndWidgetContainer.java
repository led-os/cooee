package com.cooee.phenix;


import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.PagedView.PagedViewIcon;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.LauncherAppWidgetInfo;


public class ShortcutAndWidgetContainer extends ViewGroup
//
implements IOnThemeChanged//zhujieping add，换主题不重启
{
	
	static final String TAG = "CellLayoutChildren";
	// These are temporary variables to prevent having to allocate a new object just to
	// return an (x, y) value from helper functions. Do NOT use them to maintain other state.
	private final int[] mTmpCellXY = new int[2];
	private final WallpaperManager mWallpaperManager;
	private boolean mIsHotseatLayout;
	private int mCellWidth;
	private int mCellHeight;
	private int mWidthGap;
	private int mHeightGap;
	private int mCountX;
	private int mCountY;
	private boolean mInvertIfRtl = false;
	
	public ShortcutAndWidgetContainer(
			Context context )
	{
		super( context );
		mWallpaperManager = WallpaperManager.getInstance( context );
	}
	
	public void setCellDimensions(
			int cellWidth ,
			int cellHeight ,
			int widthGap ,
			int heightGap ,
			int countX ,
			int countY )
	{
		mCellWidth = cellWidth;
		mCellHeight = cellHeight;
		mWidthGap = widthGap;
		mHeightGap = heightGap;
		mCountX = countX;
		mCountY = countY;
	}
	
	public View getChildAt(
			int x ,
			int y )
	{
		final int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View child = getChildAt( i );
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams)child.getLayoutParams();
			if( ( lp.cellX <= x ) && ( x < lp.cellX + lp.cellHSpan ) && ( lp.cellY <= y ) && ( y < lp.cellY + lp.cellVSpan ) )
			{
				return child;
			}
		}
		return null;
	}
	
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		@SuppressWarnings( "all" )
		// suppress dead code warning
		final boolean debug = false;
		if( debug )
		{
			// Debug drawing for hit space
			Paint p = new Paint();
			p.setColor( 0x6600FF00 );
			for( int i = getChildCount() - 1 ; i >= 0 ; i-- )
			{
				final View child = getChildAt( i );
				final CellLayout.LayoutParams lp = (CellLayout.LayoutParams)child.getLayoutParams();
				canvas.drawRect( lp.x , lp.y , lp.x + lp.width , lp.y + lp.height , p );
			}
		}
		super.dispatchDraw( canvas );
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		int count = getChildCount();
		int widthSpecSize = MeasureSpec.getSize( widthMeasureSpec );
		int heightSpecSize = MeasureSpec.getSize( heightMeasureSpec );
		setMeasuredDimension( widthSpecSize , heightSpecSize );
		for( int i = 0 ; i < count ; i++ )
		{
			View child = getChildAt( i );
			if( child.getVisibility() != GONE )
			{
				measureChild( child );
			}
		}
	}
	
	public void setupLp(
			CellLayout.LayoutParams lp )
	{
		lp.setup( mCellWidth , mCellHeight , mWidthGap , mHeightGap , invertLayoutHorizontally() , mCountX );
	}
	
	// Set whether or not to invert the layout horizontally if the layout is in RTL mode.
	public void setInvertIfRtl(
			boolean invert )
	{
		mInvertIfRtl = invert;
	}
	
	public void setIsHotseat(
			boolean isHotseat )
	{
		mIsHotseatLayout = isHotseat;
	}
	
	int getCellContentWidth()
	{
		final LauncherAppState app = LauncherAppState.getInstance();
		final DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		return Math.min( getMeasuredHeight() , mIsHotseatLayout ? grid.getHotseatCellWidthPx() : grid.getCellWidthPx() );
	}
	
	int getCellContentHeight()
	{
		final LauncherAppState app = LauncherAppState.getInstance();
		final DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		return Math.min( getMeasuredHeight() , mIsHotseatLayout ? grid.getHotseatCellHeightPx() : grid.getCellHeightPx() );
	}
	
	public void measureChild(
			View child )
	{
		final LauncherAppState app = LauncherAppState.getInstance();
		final DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		final int cellWidth = mCellWidth;
		final int cellHeight = mCellHeight;
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams)child.getLayoutParams();
		if( !lp.isFullscreen )
		{
			lp.setup( cellWidth , cellHeight , mWidthGap , mHeightGap , invertLayoutHorizontally() , mCountX );
			if( child instanceof LauncherAppWidgetHostView )
			{
				// Widgets have their own padding, so skip
				//chenliang add start	//解决“拖拽特定尺寸大小的插件到下一页或者拖拽到屏幕最右侧有白线出现时，松手后导致桌面重启”的问题。【i_0015035】
				Object mTag = child.getTag();
				if( mTag instanceof LauncherAppWidgetInfo )
				{
					LauncherAppWidgetHostView mAppWidgetHostView = ( (LauncherAppWidgetHostView)child );
					LauncherAppWidgetInfo mLauncherAppWidgetInfo = (LauncherAppWidgetInfo)mTag;
					if( mLauncherAppWidgetInfo.getMinSpanX() < 0 || mLauncherAppWidgetInfo.getMinSpanY() < 0 )
					{
						int[] minSpan = Launcher.getMinSpanForWidget( getContext() , mAppWidgetHostView.getAppWidgetInfo() );
						mLauncherAppWidgetInfo.setMinSpanX( minSpan[0] );
						mLauncherAppWidgetInfo.setMinSpanY( minSpan[1] );
					}
				}
				//chenliang add end
			}
			else
			{
				// Otherwise, center the icon
				int cHeight = getCellContentHeight();
				int cellPaddingY = (int)Math.max( 0 , ( ( lp.height - cHeight ) / 2f ) );
				//xiatian start	//支持配置图标名称和文件夹名称的左右边距。
				//xiatian del start
				//				int mPaddingLeft = (int)( grid.edgeMarginPx / 2f );
				//				int mPaddingRight = (int)( grid.edgeMarginPx / 2f );
				//xiatian del end
				//xiatian add start
				// gaominghui@2016/12/14 ADD START 兼容android 4.0
				int mPaddingLeft;
				int mPaddingRight;
				if( Build.VERSION.SDK_INT > 16 )
				{
					mPaddingLeft = child.getPaddingStart();
					mPaddingRight = child.getPaddingEnd();
				}
				else
				{
					mPaddingLeft = child.getPaddingLeft();
					mPaddingRight = child.getPaddingRight();
				}
				// gaominghui@2016/12/14 ADD END 兼容android 4.0
				//xiatian add end
				//xiatian end
				//xiatian start	//需求：桌面布局，将”图标之间无间隙“改为“图标之间有间隙”。
				//xiatian del start
				//				int mPaddingTop = cellPaddingY;
				//				int mPaddingBottom = 0;
				//xiatian del end
				//xiatian add start
				int mPaddingTop = 0;
				int mPaddingBottom = 0;
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
				{
					//cheyingkun add start	//自定义桌面布局
					if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
					{
						mPaddingTop = grid.getItemPaddingTopInCell();
						mPaddingBottom = grid.getItemPaddingBottomInCell();
					}
					else
					//cheyingkun add end
					{
						//cheyingkun del start	//默认图标样式下,添加图标和文字之间的间距配置(底边栏和图标一样设置上下边距)【c_0004390】
						if( mIsHotseatLayout )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE )
							{
								mPaddingTop = grid.getHotseatItemPaddingTopInCell();//zhujieping add
								mPaddingBottom = grid.getHotseatItemPaddingBottomInCell();
							}
							else
							{
								mPaddingTop = mPaddingBottom = grid.getHotseatItemPaddingTopInCell();//zhujieping add
							}
						}
						else
						//cheyingkun del end
						//cheyingkun add start	//主菜单图标缩放比。默认为1。
						if( child instanceof PagedViewIcon )
						{
							mPaddingTop = (int)( grid.getItemPaddingTopInCell() * LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() );
							mPaddingBottom = grid.getItemPaddingBottomInCell();
						}
						else
						//cheyingkun add end
						{
							mPaddingTop = grid.getItemPaddingTopInCell();
							mPaddingBottom = grid.getItemPaddingBottomInCell();//mPaddingTop;
						}
					}
				}
				else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
				{
					mPaddingTop = cellPaddingY;
					mPaddingBottom = 0;
				}
				//xiatian add end
				//xiatian end
				child.setPadding( mPaddingLeft , mPaddingTop , mPaddingRight , mPaddingBottom );
			}
		}
		else
		{
			lp.x = 0;
			lp.y = 0;
			lp.width = getMeasuredWidth();
			lp.height = getMeasuredHeight();
		}
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( lp.width , MeasureSpec.EXACTLY );
		int childheightMeasureSpec = MeasureSpec.makeMeasureSpec( lp.height , MeasureSpec.EXACTLY );
		child.measure( childWidthMeasureSpec , childheightMeasureSpec );
	}
	
	private boolean invertLayoutHorizontally()
	{
		return mInvertIfRtl && isLayoutRtl();
	}
	
	public boolean isLayoutRtl()
	{
		//return( getLayoutDirection() == LAYOUT_DIRECTION_RTL );
		//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
		//		return Tools.isLayoutRTL( this );//xiatian del
		return LauncherAppState.isLayoutRTL();//xiatian add 
		//xiatian end
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int l ,
			int t ,
			int r ,
			int b )
	{
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			final View child = getChildAt( i );
			if( child.getVisibility() != GONE )
			{
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams)child.getLayoutParams();
				int childLeft = lp.x;
				int childTop = lp.y;
				child.layout( childLeft , childTop , childLeft + lp.width , childTop + lp.height );
				if( lp.dropped )
				{
					lp.dropped = false;
					final int[] cellXY = mTmpCellXY;
					getLocationOnScreen( cellXY );
					mWallpaperManager.sendWallpaperCommand( getWindowToken() , WallpaperManager.COMMAND_DROP , cellXY[0] + childLeft + lp.width / 2 , cellXY[1] + childTop + lp.height / 2 , 0 , null );
				}
			}
		}
	}
	
	@Override
	public boolean shouldDelayChildPressedState()
	{
		return false;
	}
	
	@Override
	public void requestChildFocus(
			View child ,
			View focused )
	{
		super.requestChildFocus( child , focused );
		if( child != null )
		{
			Rect r = new Rect();
			child.getDrawingRect( r );
			requestRectangleOnScreen( r );
		}
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
	
	@Override
	protected void setChildrenDrawingCacheEnabled(
			boolean enabled )
	{
		final int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			final View view = getChildAt( i );
			view.setDrawingCacheEnabled( enabled );
			// Update the drawing caches
			if( !view.isHardwareAccelerated() && enabled )
			{
				view.buildDrawingCache( true );
			}
		}
	}
	
	@Override
	protected void setChildrenDrawnWithCacheEnabled(
			boolean enabled )
	{
		super.setChildrenDrawnWithCacheEnabled( enabled );
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
		int mCount = getChildCount();
		for( int i = 0 ; i < mCount ; i++ )
		{
			View mView = getChildAt( i );
			if( mView instanceof BubbleTextView )
			{
				BubbleTextView mBubbleTextView = (BubbleTextView)mView;
				mBubbleTextView.onThemeChanged( arg0 , arg1 );
			}
			else if( mView instanceof PagedViewIcon )
			{
				PagedViewIcon mPagedViewIcon = (PagedViewIcon)mView;
				mPagedViewIcon.onThemeChanged( arg0 , arg1 );
			}
			else if( mView instanceof FolderIcon )
			{
				FolderIcon mFolderIcon = (FolderIcon)mView;
				mFolderIcon.onThemeChanged( arg0 , arg1 );
			}
			else if( mView instanceof LauncherAppWidgetHostView )
			{
				LauncherAppWidgetHostView mLauncherAppWidgetHostView = (LauncherAppWidgetHostView)mView;
				mLauncherAppWidgetHostView.onThemeChanged( arg0 , arg1 );
			}
		}
	}
}
