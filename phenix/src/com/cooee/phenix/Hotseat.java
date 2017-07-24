package com.cooee.phenix;


import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.theme.ThemeManager;


public class Hotseat extends FrameLayout
//
implements IOnThemeChanged
{
	
	private static final String TAG = "Hotseat";
	private CellLayout mContent;
	private Launcher mLauncher;
	private int mAllAppsButtonRank;
	private BubbleTextView mAllAppsButton;
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	private int mBackgroundColor;
	private ColorDrawable mBackground;
	//zhujieping add end
	
	public Hotseat(
			Context context )
	{
		this( context , null );
	}
	
	public Hotseat(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public Hotseat(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	public void setup(
			Launcher launcher )
	{
		mLauncher = launcher;
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		mBackgroundColor = getResources().getColor( R.color.hotseat_bg_color );
		mBackground = new ColorDrawable( mBackgroundColor );
		setBackgroundDrawable( mBackground );
		//zhujieping add end
		if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		{
			setOnKeyListener( new HotseatIconKeyEventListener() );
		}
	}
	
	CellLayout getLayout()
	{
		return mContent;
	}
	
	/**
	 * Registers the specified listener on the cell layout of the hotseat.
	 */
	@Override
	public void setOnLongClickListener(
			OnLongClickListener l )
	{
		mContent.setOnLongClickListener( l );
	}
	
	/* Get the orientation invariant order of the item in the hotseat for persistence. */
	int getOrderInHotseat(
			int x ,
			int y )
	{
		return x;
	}
	
	/* Get the orientation specific coordinates given an invariant order in the hotseat. */
	int getCellXFromOrder(
			int rank )
	{
		return rank;
	}
	
	int getCellYFromOrder(
			int rank )
	{
		return 0;
	}
	
	public boolean isAllAppsButtonRank(
			int rank )
	{
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			return false;
		}
		else
		{
			return rank == mAllAppsButtonRank;
		}
	}
	
	/** This returns the coordinates of an app in a given cell, relative to the DragLayer */
	Rect getCellCoordinates(
			int cellX ,
			int cellY )
	{
		Rect coords = new Rect();
		mContent.cellToRect( cellX , cellY , 1 , 1 , coords );
		int[] hotseatInParent = new int[2];
		Utilities.getDescendantCoordRelativeToParent( this , mLauncher.getDragLayer() , hotseatInParent , false );
		coords.offset( hotseatInParent[0] , hotseatInParent[1] );
		// Center the icon
		int cWidth = mContent.getShortcutsAndWidgets().getCellContentWidth();
		int cHeight = mContent.getShortcutsAndWidgets().getCellContentHeight();
		int cellPaddingX = (int)Math.max( 0 , ( ( coords.width() - cWidth ) / 2f ) );
		int cellPaddingY = (int)Math.max( 0 , ( ( coords.height() - cHeight ) / 2f ) );
		coords.offset( cellPaddingX , cellPaddingY );
		return coords;
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		mAllAppsButtonRank = grid.getHotseatAllAppsRank();
		mContent = (CellLayout)findViewById( R.id.layout );
		setCellSize();//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		mContent.setGridSize( (int)grid.getNumHotseatIcons() , 1 );
		mContent.setIsHotseat( true );
		resetLayout();
	}
	
	@SuppressWarnings( "deprecation" )
	void resetLayout()
	{
		//xiatian start	//fix bug：解决“将底边栏图标全部放到桌面后进行智能分类，分类成功后再回复默认布局，这时某些手机(NOAIN A918 4.2.2)底边栏图标还在，但是点击无效”的问题。【i_0011046】
		//【问题原因】某些手机(NOAIN A918 4.2.2)虽然移除了底边栏的view，但是底边栏没有刷新，导致底边栏图标还在，但是点击无效。
		//【解决方案】将“removeAllViewsInLayout”改为“removeAllViews”，removeAllViews比removeAllViewsInLayout多调用“requestLayout()”和“invalidate(true)”，来刷新界面。
		//		mContent.removeAllViewsInLayout();//xiatian del
		mContent.removeAllViews();//xiatian add
		//xiatian end
		if(
		//
		LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER
		// 	
		&& ( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT/* //zhujieping add //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。 */ )//zhujieping //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		//
		)
		{
			// Add the Apps button
			LayoutInflater inflater = LayoutInflater.from( getContext() );
			mAllAppsButton = (BubbleTextView)inflater.inflate( R.layout.hotseat_all_apps_button_layout , mContent , false );
			//xiatian start	//需求：完善底边栏图标读取主题中的图片的逻辑（详见ThemeManager.java中的“HOTSEAT_ALL_APPS_BUTTON_ICON_XXX”）。
			//			Drawable mAllAppsButtonDrawableTop = getContext().getResources().getDrawable( R.drawable.hotseat_all_apps_button_selector );//xiatian del
			Drawable mAllAppsButtonDrawableTop = ThemeManager.getInstance().getHotseatAllAppsButtonIcon();//xiatian add
			//xiatian end
			Utilities.resizeIconDrawable( mAllAppsButtonDrawableTop );
			mAllAppsButton.setCompoundDrawables( null , mAllAppsButtonDrawableTop , null , null );
			mAllAppsButton.setGapBetweenIconAndText();//cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(底边栏“主菜单”图标和名称的间距)【c_0004390】
			if( mLauncher != null )
			{
				mAllAppsButton.setOnTouchListener( mLauncher.getHapticFeedbackTouchListener() );
			}
			// Note: We do this to ensure that the hotseat is always laid out in the orientation of
			// the hotseat in order regardless of which orientation they were added
			int x = getCellXFromOrder( mAllAppsButtonRank );
			int y = getCellYFromOrder( mAllAppsButtonRank );
			CellLayout.LayoutParams lp = new CellLayout.LayoutParams( x , y , 1 , 1 );
			lp.canReorder = false;
			mContent.addViewToCellLayout( mAllAppsButton , -1 , 0 , lp , true );
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		// We don't want any clicks to go through to the hotseat unless the workspace is in
		// the normal state.
		if( mLauncher.getWorkspace().isSmall() )
		{
			return true;
		}
		return false;
	}
	
	void addAllAppsFolder(
			IconCache iconCache ,
			ArrayList<AppInfo> allApps ,
			ArrayList<ComponentName> onWorkspace ,
			Launcher launcher ,
			Workspace workspace )
	{
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			FolderInfo fi = new FolderInfo();
			fi.setCellX( getCellXFromOrder( mAllAppsButtonRank ) );
			fi.setCellY( getCellYFromOrder( mAllAppsButtonRank ) );
			fi.setSpanX( 1 );
			fi.setSpanY( 1 );
			fi.setContainer( LauncherSettings.Favorites.CONTAINER_HOTSEAT );
			fi.setScreenId( mAllAppsButtonRank );
			fi.setItemType( LauncherSettings.Favorites.ITEM_TYPE_FOLDER );
			fi.setTitle( "More Apps" );
			LauncherModel.addItemToDatabase( launcher , fi , fi.getContainer() , fi.getScreenId() , fi.getCellX() , fi.getCellY() , false );
			FolderIcon folder = FolderIcon.fromXml( R.layout.folder_icon , launcher , getLayout() , fi , iconCache );
			workspace.addInScreen( folder , fi.getContainer() , fi.getScreenId() , fi.getCellX() , fi.getCellY() , fi.getSpanX() , fi.getSpanY() );
			for( AppInfo info : allApps )
			{
				Intent mAppInfoIntent = info.getIntent();
				if( mAppInfoIntent != null )
				{
					ComponentName cn = mAppInfoIntent.getComponent();
					if( !onWorkspace.contains( cn ) )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , StringUtils.concat( "Adding to 'more apps': " + mAppInfoIntent.toUri( 0 ) ) );
						ShortcutInfo si = info.makeShortcut();
						fi.add( si );
					}
				}
			}
		}
	}
	
	void addAppsToAllAppsFolder(
			ArrayList<AppInfo> apps )
	{
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			View v = mContent.getChildAt( getCellXFromOrder( mAllAppsButtonRank ) , getCellYFromOrder( mAllAppsButtonRank ) );
			FolderIcon fi = null;
			if( v instanceof FolderIcon )
			{
				fi = (FolderIcon)v;
			}
			else
			{
				return;
			}
			FolderInfo info = fi.getFolderInfo();
			for( AppInfo a : apps )
			{
				ShortcutInfo si = a.makeShortcut();
				info.add( si );
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
		// TODO Auto-generated method stub
		super.onLayout( changed , left , top , right , bottom );
		if( changed )
		{
			setCellSize();//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		}
	}
	
	private void setCellSize()
	{
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( mContent != null && LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//设置底边栏每一个view的宽高。
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			int mCellWidth = grid.getSignleViewAvailableWidthPx();
			int mCellHeight = grid.getSignleViewAvailableHeightPx();
			mContent.setCellDimensions( mCellWidth , mCellHeight );
		}
		//xiatian add end
	}
	
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
		int mCount = getChildCount();
		for( int i = 0 ; i < mCount ; i++ )
		{
			if( getChildAt( i ) instanceof CellLayout )
			{
				CellLayout mCellLayout = (CellLayout)getChildAt( i );
				mCellLayout.onThemeChanged( arg0 , arg1 );
			}
		}
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && mAllAppsButton != null )
		{
			final Drawable mAllAppsButtonDrawableTop = ThemeManager.getInstance().getHotseatAllAppsButtonIcon();//xiatian add
			Utilities.resizeIconDrawable( mAllAppsButtonDrawableTop );
			mAllAppsButton.post( new Runnable() {
				
				public void run()
				{
					mAllAppsButton.setCompoundDrawables( null , mAllAppsButtonDrawableTop , null , null );
				}
			} );
		}
	}
	//zhujieping add end
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public int getBackgroundDrawableColor()
	{
		return mBackgroundColor;
	}
	
	public View getAllAppsButton()
	{
		return mAllAppsButton;
	}
	//zhujieping add end
}
