package com.cooee.phenix;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.function.DynamicEntry.DLManager.StatisticsHandle;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.AppList.Marshmallow.AllAppsContainerView;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.Functions.DynamicEntry.OperateFolderDatabase;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.LauncherAppWidgetInfo;
import com.cooee.phenix.data.PendingAddItemInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.iLoong.launcher.MList.MeLauncherInterface;


public class DeleteDropTarget extends ButtonDropTarget
{
	
	private static int DELETE_ANIMATION_DURATION = 285;
	private static int FLING_DELETE_ANIMATION_DURATION = 350;
	private static float FLING_TO_DELETE_FRICTION = 0.035f;
	private static int MODE_FLING_DELETE_TO_TRASH = 0;
	private static int MODE_FLING_DELETE_ALONG_VECTOR = 1;
	private final int mFlingDeleteMode = MODE_FLING_DELETE_ALONG_VECTOR;
	private ColorStateList mOriginalTextColor;
	private TransitionDrawable mUninstallDrawable;
	private TransitionDrawable mRemoveDrawable;
	private TransitionDrawable mCurrentDrawable;
	private boolean mWaitingForUninstall = false;
	//ME_RTFSC [start]
	private Context mContext = null;
	
	//ME_RTFSC [end]
	public DeleteDropTarget(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public DeleteDropTarget(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		//ME_RTFSC [start]
		mContext = context;
		//ME_RTFSC [end]
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		// Get the drawable
		mOriginalTextColor = getTextColors();
		// Get the hover color
		Resources r = getResources();
		mHoverColor = r.getColor( R.color.delete_target_hover_tint );
		mUninstallDrawable = (TransitionDrawable)r.getDrawable( R.drawable.uninstall_target_selector );
		mRemoveDrawable = (TransitionDrawable)r.getDrawable( R.drawable.remove_target_selector );
		mRemoveDrawable.setCrossFadeEnabled( true );
		mUninstallDrawable.setCrossFadeEnabled( true );
		// The current drawable is set to either the remove drawable or the uninstall drawable 
		// and is initially set to the remove drawable, as set in the layout xml.
		mCurrentDrawable = (TransitionDrawable)getCurrentDrawable();
	}
	
	private boolean isAllAppsApplication(
			DragSource source ,
			Object info )
	{
		return ( source instanceof AppsCustomizePagedView
				//
				|| source instanceof AllAppsContainerView )/* //zhujieping add //解决“config_applist_style配置为1或2时，从主菜单拖动图标到桌面，不显示应用信息和卸载（系统应用不显示卸载，只显示应用信息）”的问题。 */
				//
				&& ( info instanceof AppInfo );
	}
	
	private boolean isAllAppsWidget(
			DragSource source ,
			Object info )
	{
		if( source instanceof AppsCustomizePagedView )
		{
			if( info instanceof PendingAddItemInfo )
			{
				PendingAddItemInfo addInfo = (PendingAddItemInfo)info;
				switch( addInfo.getItemType() )
				{
					case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
					case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
						return true;
				}
			}
		}
		return false;
	}
	
	private boolean isDragSourceWorkspaceOrFolder(
			DragObject d )
	{
		return ( d.dragSource instanceof Workspace ) || ( d.dragSource instanceof Folder );
	}
	
	private boolean isWorkspaceOrFolderApplication(
			DragObject d )
	{
		return isDragSourceWorkspaceOrFolder( d ) && ( d.dragInfo instanceof ShortcutInfo );
	}
	
	private boolean isWorkspaceOrFolderWidget(
			DragObject d )
	{
		return isDragSourceWorkspaceOrFolder( d ) && ( d.dragInfo instanceof LauncherAppWidgetInfo );
	}
	
	private boolean isWorkspaceFolder(
			DragObject d )
	{
		return ( d.dragSource instanceof Workspace ) && ( d.dragInfo instanceof FolderInfo );
	}
	
	public void setHoverColor()
	{
		mCurrentDrawable.startTransition( mTransitionDuration );
		setTextColor( mHoverColor );
	}
	
	public void resetHoverColor()
	{
		mCurrentDrawable.resetTransition();
		setTextColor( mOriginalTextColor );
	}
	
	@Override
	public boolean acceptDrop(
			DragObject d )
	{
		return willAcceptDrop( d.dragInfo );
	}
	
	public static boolean willAcceptDrop(
			Object info )
	{
		if( info instanceof ItemInfo )
		{
			ItemInfo item = (ItemInfo)info;
			//xiatian start	//整理代码：整理接口willAcceptDrop
			//xiatian del start
			//			if( item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET || item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT )
			//			{
			//				return true;
			//			}
			//			//xiatian start	//需求:修改智能分类后文件夹解散的逻辑。（fix bug：解决“智能分类完成后将智能分类的文件夹中最后一个图标移出文件夹形成空文件夹,再长按该文件夹后，删除框不出现”的问题。）
			//			//xiatian del start
			//			//			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_FOLDER )
			//			//			{
			//			//				return true;
			//			//			}
			//			//xiatian del end
			//			//xiatian add start
			//			if( item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_FOLDER )
			//			{
			//				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
			//				{
			//					return true;
			//				}
			//				else
			//				{
			//					FolderInfo mFolderInfo = (FolderInfo)info;
			//					ArrayList<ShortcutInfo> mContents = mFolderInfo.getContents();
			//					if( ( mContents == null ) || ( mContents.size() == 0 ) )
			//					{
			//						return true;
			//					}
			//				}
			//			}
			//			//xiatian add end
			//			//xiatian end
			//			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && item instanceof AppInfo )
			//			{
			//				AppInfo appInfo = (AppInfo)info;
			//				//ME_RTFSC
			//				if( ( appInfo.getFlags() & AppInfo.DOWNLOADED_FLAG ) != 0 )
			//				{
			//					return true;
			//				}
			//				else if( MeLauncherInterface.getInstance().MeIsMicroEntry( appInfo.getComponentName().getClassName() ) )
			//				{
			//					return true;
			//				}
			//				else
			//				{
			//					return false;
			//				}
			//				//return (appInfo.getFlags() & AppInfo.DOWNLOADED_FLAG) != 0;
			//				//ME_RTFSC
			//			}
			//			if( item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && item instanceof ShortcutInfo )
			//			{
			//				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
			//				{
			//					ShortcutInfo shortcutInfo = (ShortcutInfo)info;
			//					return ( shortcutInfo.getFlags() & AppInfo.DOWNLOADED_FLAG ) != 0;
			//				}
			//				else
			//				{
			//					return true;
			//				}
			//			}
			//			//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
			//			if( item.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL && item instanceof ShortcutInfo )
			//			{//虚图标是否可删除
			//				VirtualInfo mVirtualInfo = ( (ShortcutInfo)info ).makeVirtual();
			//				if( mVirtualInfo.getIsCanUninstall() )
			//				{
			//					return true;
			//				}
			//				else
			//				{
			//					return false;
			//				}
			//			}
			//			//xiatian add end
			//xiatian del end
			return item.willAcceptDrop();//xiatian add
			//xiatian end
		}
		return false;
	}
	
	@Override
	public void onDragStart(
			DragSource source ,
			Object info ,
			int dragAction )
	{
		super.onDragStart( source , info , dragAction );
		boolean isVisible = true;
		//xiatian start	//fix bug：解决“单层模式下，长按抬起桌面或者文件夹中可卸载应用时，垃圾筐处提示‘叉号’图标和‘删除’字符串（应该提示‘垃圾筐’图标和‘卸载’字符串）”的问题。
		//		boolean useUninstallLabel =  ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER ) && isAllAppsApplication( source , info );//xiatian del
		//xiatian add start
		boolean useUninstallLabel = false;
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{//单层模式
			if( isUninstallFromWorkspace( source , info ) )
			{//长按抬起桌面或者文件夹中的可卸载应用时，垃圾筐处提示“垃圾筐”图标和“卸载”字符串
				useUninstallLabel = true;
			}
		}
		else
		{//双层模式
			if( isAllAppsApplication( source , info ) )
			{//长按抬起主菜单中的可卸载应用时，垃圾筐处提示“垃圾筐”图标和“卸载”字符串
				useUninstallLabel = true;
			}
		}
		//xiatian add end
		//xiatian end
		// If we are dragging an application from AppsCustomize, only show the control if we can
		// delete the app (it was downloaded), and rename the string to "uninstall" in such a case.
		// Hide the delete target if it is a widget from AppsCustomize.
		if(
		//
		!willAcceptDrop( info )
		//
		|| isAllAppsWidget( source , info )
		//
		|| ( isOperateDynamicFolder( info ) && ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER ) )
		//
		)
		{
			isVisible = false;
		}
		if(
		//
		info instanceof AppInfo
		//
		|| ( info instanceof ShortcutInfo && ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE ) )
		//
		)
		{
			ComponentName comp;
			int flag;
			if( info instanceof AppInfo )
			{
				comp = ( (AppInfo)info ).getComponentName();
				flag = ( (AppInfo)info ).getFlags();
			}
			else
			{
				comp = ( (ShortcutInfo)info ).getIntent().getComponent();
				flag = ( (ShortcutInfo)info ).getFlags();
			}
			int ret = MeLauncherInterface.getInstance().MeIconOnDropOptType( mContext , comp );
			//如果是微入口图标，并且允许被删除,是否内置都显示 “卸载”
			if( 0 == ret && true != isVisible )
			{
				isVisible = true;
			}
			//如果是微入口图标，并且不允许被删除，不显示垃圾桶
			else if( 1 == ret )
			{
				isVisible = false;
			}
			//-1说明不是微入口
		}
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		setDropTargetVisible( useUninstallLabel , isVisible );
	}
	
	@Override
	public void setDropTargetVisible(
			boolean ... args )
	{
		boolean useUninstallLabel = args[0];
		boolean isVisible = args[1];
		if( Build.VERSION.SDK_INT >= 17 )
		{
			if( useUninstallLabel )
			{
				setCompoundDrawablesRelativeWithIntrinsicBounds( mUninstallDrawable , null , null , null );
			}
			else
			{
				setCompoundDrawablesRelativeWithIntrinsicBounds( mRemoveDrawable , null , null , null );
			}
		}
		else
		{
			if( useUninstallLabel )
			{
				setCompoundDrawablesWithIntrinsicBounds( mUninstallDrawable , null , null , null );
			}
			else
			{
				setCompoundDrawablesWithIntrinsicBounds( mRemoveDrawable , null , null , null );
			}
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		mCurrentDrawable = (TransitionDrawable)getCurrentDrawable();
		mActive = isVisible;
		resetHoverColor();
		( (ViewGroup)getParent() ).setVisibility( isVisible ? View.VISIBLE : View.GONE );
		if( getText().length() > 0 )
		{
			setText( useUninstallLabel ? R.string.delete_target_uninstall_label : R.string.delete_target_remove_label );
		}
		if( isVisible && !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
		{
			if( Build.VERSION.SDK_INT >= 16 )
			{
				mLauncher.getWorkspace().setSystemUiVisibility( View.SYSTEM_UI_FLAG_FULLSCREEN );
			}
			else
			{
				WindowManager.LayoutParams attrs = mLauncher.getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				mLauncher.getWindow().setAttributes( attrs );
			}
		}
	}
	/**
	 * 判断是否为运营文件夹，若是运营文件夹，则不需要显示删除界面
	 * @param info
	 * @return
	 */
	private boolean isOperateDynamicFolder(
			Object info )
	{
		if( info instanceof FolderInfo && ( (FolderInfo)info ).getFolderType() == LauncherSettings.Favorites.FOLDER_TYPE_OPERATE_DYNAMIC )
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void onDragEnd()
	{
		super.onDragEnd();
		mActive = false;
		if( Build.VERSION.SDK_INT >= 16 )
		{
			mLauncher.getWorkspace().setSystemUiVisibility( View.SYSTEM_UI_FLAG_VISIBLE );
		}
		else
		{
			WindowManager.LayoutParams attrs = mLauncher.getWindow().getAttributes();
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
			mLauncher.getWindow().setAttributes( attrs );
		}
	}
	
	public void onDragEnter(
			DragObject d )
	{
		super.onDragEnter( d );
		setHoverColor();
	}
	
	public void onDragExit(
			DragObject d )
	{
		super.onDragExit( d );
		if( d == null || !d.dragComplete )
		{
			resetHoverColor();
		}
		else
		{
			// Restore the hover color if we are deleting
			d.dragView.setColor( mHoverColor );
		}
	}
	
	private void animateToTrashAndCompleteDrop(
			final DragObject d )
	{
		final DragLayer dragLayer = mLauncher.getDragLayer();
		final Rect from = new Rect();
		dragLayer.getViewRectRelativeToSelf( d.dragView , from );
		final Rect to = getIconRect( d.dragView.getMeasuredWidth() , d.dragView.getMeasuredHeight() , mCurrentDrawable.getIntrinsicWidth() , mCurrentDrawable.getIntrinsicHeight() );
		final float scale = (float)to.width() / from.width();
		// zhujieping@2015/03/26 DEL START
		//mSearchDropTargetBar.deferOnDragEnd();
		// zhujieping@2015/03/26 DEL END
		deferCompleteDropIfUninstalling( d );
		Runnable onAnimationEndRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				completeDrop( d );
				// zhujieping@2015/03/26 UPDATE START，当workspace处于编辑模式时，搜索框不出现【i_0010652】
				//上方被注释掉的mSearchDropTargetBar.deferOnDragEnd()置标志位，因而此处mSearchDropTargetBar.onDragEnd()方法只是置回标志位
				//而下方的DragLayer.ANIMATION_END_DISAPPEAR这个属性会再次回调mSearchDropTargetBar.onDragEnd()，此时执行卸载框移除、搜索框弹出的动画
				//当处于编辑模式时，mSearchDropTargetBar.onDragEnd( true )只执行卸载框移除动画，搜索框不出现，再置标志位，回调中置回标志位。
				//mSearchDropTargetBar.onDragEnd();
				if( mLauncher.getWorkspace() != null && mLauncher.getWorkspace().isInOverviewMode() )
				{
					mSearchDropTargetBar.onDragEnd( true );
					mSearchDropTargetBar.deferOnDragEnd();
				}
				else
				{
					mSearchDropTargetBar.deferOnDragEnd();
					mSearchDropTargetBar.onDragEnd();
				}
				// zhujieping@2015/03/26 UPDATE END
				//<i_0010088> liuhailin@2015-03-12 del begin
				//mLauncher.exitSpringLoadedDragMode();
				//退回到workspace界面
				mLauncher.exitSpringLoadedDragModeDelayed( true , false , null );
				//<i_0010088> liuhailin@2015-03-12 del end
			}
		};
		dragLayer.animateView(
				d.dragView ,
				from ,
				to ,
				scale ,
				1f ,
				1f ,
				0.1f ,
				0.1f ,
				DELETE_ANIMATION_DURATION ,
				new DecelerateInterpolator( 2 ) ,
				new LinearInterpolator() ,
				onAnimationEndRunnable ,
				DragLayer.ANIMATION_END_DISAPPEAR ,
				null );
	}
	
	private void deferCompleteDropIfUninstalling(
			DragObject d )
	{
		mWaitingForUninstall = false;
		if( isUninstallFromWorkspace( d ) )
		{
			if( d.dragSource instanceof Folder )
			{
				( (Folder)d.dragSource ).deferCompleteDropAfterUninstallActivity();
			}
			else if( d.dragSource instanceof Workspace )
			{
				( (Workspace)d.dragSource ).deferCompleteDropAfterUninstallActivity();
			}
			mWaitingForUninstall = true;
		}
	}
	
	private boolean isUninstallFromWorkspace(
			DragObject d )
	{
		if(
		//
		( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		//
		&& isWorkspaceOrFolderApplication( d )
		//
		)
		{
			ShortcutInfo shortcut = (ShortcutInfo)d.dragInfo;
			if( shortcut.getIntent() != null && shortcut.getIntent().getComponent() != null )
			{
				if( shortcut.getIntent().getComponent().getPackageName().equals( getContext().getPackageName() ) && shortcut.getIntent().getComponent().getClassName()
						.equals( "com.cooee.wallpaper.host.WallpaperMainActivity" ) )//zhujieping，桌面配置的图标，在写入数据库的时候，加上了Intent.CATEGORY_LAUNCHER这个属性,导致下面返回true，如果桌面配置了一键换壁纸，长按卸载，则会将桌面卸载掉，因此这里返回false，不让被卸载
				{
					return false;
				}
				Set<String> categories = shortcut.getIntent().getCategories();
				boolean includesLauncherCategory = false;
				if( categories != null )
				{
					for( String category : categories )
					{
						if( category.equals( Intent.CATEGORY_LAUNCHER ) )
						{
							includesLauncherCategory = true;
							break;
						}
					}
				}
				return includesLauncherCategory;
			}
		}
		return false;
	}
	
	private void completeDrop(
			DragObject d )
	{
		ItemInfo item = (ItemInfo)d.dragInfo;
		boolean wasWaitingForUninstall = mWaitingForUninstall;
		mWaitingForUninstall = false;
		//ME_RTFSC [start]
		if(
		//
		item instanceof AppInfo
		//
		|| ( item instanceof ShortcutInfo && ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE ) )
		//
		)
		{
			ComponentName comp;
			if( item instanceof AppInfo )
			{
				comp = ( (AppInfo)item ).getComponentName();
			}
			else
			{
				comp = ( (ShortcutInfo)item ).getIntent().getComponent();
			}
			int ret = MeLauncherInterface.getInstance().MeIconOnDropOptType( mContext , comp );
			//是微入口的图标,允许被删除
			if( 0 == ret )
			{
				PackageManager pkgMgr = mContext.getPackageManager();
				pkgMgr.setComponentEnabledSetting( comp , PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP );
				PreferenceManager.getDefaultSharedPreferences( mContext.getApplicationContext() ).edit().putBoolean( StringUtils.concat( "ME_HIDE:" , comp.getClassName() ) , true ).commit();
				MeLauncherInterface.getInstance().setDialog( mLauncher , MeLauncherInterface.getInstance().MeGetIconResIDByClass( comp.getClassName() ) );
				MeLauncherInterface.getInstance().LogDelete( mLauncher , comp.getClassName() );
				return;
			}
			//			//是微入口的图标,不允许被删除中
			//			else if( 1 == ret )
			//			{
			//				Toast.makeText( mContext , R.string.cool_ml_MeIcon_cannot_uninstall , Toast.LENGTH_SHORT ).show();
			//				return;
			//			}
		}
		//ME_RTFSC [end]
		if( isAllAppsApplication( d.dragSource , item ) )
		{
			// Uninstall the application if it is being dragged from AppsCustomize
			AppInfo appInfo = (AppInfo)item;
			//			//ME_RTFSC [start]
			//			int ret = MeLauncherInterface.getInstance().MeIconOnDropOptType( mContext , appInfo.getComponentName() );
			//			//是微入口的图标,允许被删除
			//			if( 0 == ret )
			//			{
			//				PackageManager pkgMgr = mContext.getPackageManager();
			//				pkgMgr.setComponentEnabledSetting( appInfo.getComponentName() , PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP );
			//				PreferenceManager.getDefaultSharedPreferences( mContext.getApplicationContext() ).edit().putBoolean( "ME_HIDE:" + appInfo.getComponentName().getClassName() , true ).commit();
			//				MeLauncherInterface.getInstance().setDialog( mLauncher , MeLauncherInterface.getInstance().MeGetIconResIDByClass( appInfo.getComponentName().getClassName() ) );
			//				return;
			//			}
			//			//是微入口的图标,不允许被删除中
			//			else if( 1 == ret )
			//			{
			//				Toast.makeText( mContext , R.string.cool_ml_MeIcon_cannot_uninstall , Toast.LENGTH_SHORT ).show();
			//				return;
			//			}
			//			//ME_RTFSC [end]
			mLauncher.startApplicationUninstallActivity( appInfo.getComponentName() , appInfo.getFlags() );
		}
		else if( isUninstallFromWorkspace( d ) )
		{
			ShortcutInfo shortcut = (ShortcutInfo)item;
			if( shortcut.getIntent() != null && shortcut.getIntent().getComponent() != null )
			{
				final ComponentName componentName = shortcut.getIntent().getComponent();
				final DragSource dragSource = d.dragSource;
				int flags = AppInfo.initFlags( ShortcutInfo.getPackageInfo( getContext() , componentName.getPackageName() ) );
				//cheyingkun start	//deleteGreyApp(灰化图标可删除)bug:i_0009469
				//					mWaitingForUninstall = mLauncher.startApplicationUninstallActivity( componentName , flags );//cheyingkun del
				//cheyingkun add start
				if( !shortcut.getAvailable() )
				{
					//					mWaitingForUninstall = true;//cheyingkun del	//T卡挂载状态,删除灰色图标,无法长按第二个桌面图标
					deleteGreyApp( dragSource , componentName.getPackageName() );
				}
				else
				{
					mWaitingForUninstall = mLauncher.startApplicationUninstallActivity( componentName , flags );
					//cheyingkun add start	//飞利浦卸载应用自动排序（逻辑完善）
					Workspace workspace = mLauncher.getWorkspace();
					if( workspace.isEnableAnimAfterUninstall() )
					{
						workspace.clearRemoveList();
					}
					//cheyingkun add end
				}
				//cheyingkun add end
				//cheyingkun end
				if( mWaitingForUninstall )
				{
					final Runnable checkIfUninstallWasSuccess = new Runnable() {
						
						@Override
						public void run()
						{
							mWaitingForUninstall = false;
							String packageName = componentName.getPackageName();
							List<ResolveInfo> activities = AllAppsList.findActivitiesForPackage( getContext() , packageName );
							boolean uninstallSuccessful = activities.size() == 0;
							if( dragSource instanceof Folder )
							{
								( (Folder)dragSource ).onUninstallActivityReturned( uninstallSuccessful );
							}
							else if( dragSource instanceof Workspace )
							{
								( (Workspace)dragSource ).onUninstallActivityReturned( uninstallSuccessful );
							}
						}
					};
					mLauncher.addOnResumeCallback( checkIfUninstallWasSuccess );
				}
			}
		}
		else if( isWorkspaceOrFolderApplication( d ) )
		{
			LauncherModel.deleteItemFromDatabase( mLauncher , item );
			if( item instanceof ShortcutInfo )
			{
				if( ( (ShortcutInfo)item ).isOperateIconItem() )
				{
					OperateFolderDatabase.delete( item );//运营文件夹的数据库也得删掉
					String pkg = ( (ShortcutInfo)item ).getIntent().getStringExtra( OperateDynamicMain.PKGNAME_ID );
					if( pkg != null && DlManager.getInstance().getPkgNameCurrentState( pkg ) != Constants.DL_STATUS_NOTDOWN )//若下载中，也要从下载管理器中删除
					{
						DlManager.getInstance().dealOperateIconRemove( pkg );
					}
					StatisticsHandle.DynamicEntryDelete( ( (ShortcutInfo)item ).getIntent() , item.getTitle() );
				}
			}
		}
		else if( isWorkspaceFolder( d ) )
		{
			// Remove the folder from the workspace and delete the contents from launcher model
			FolderInfo folderInfo = (FolderInfo)item;
			mLauncher.removeFolder( folderInfo );
			LauncherModel.deleteFolderContentsFromDatabase( mLauncher , folderInfo );
			if( folderInfo.getOperateIntent() != null )
			{
				int id = folderInfo.getOperateIntent().getIntExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID , -1 );
				if( id != -1 )
					StatisticsHandle.DynamicEntryDelete( folderInfo.getTitle() , id );
			}
		}
		else if( isWorkspaceOrFolderWidget( d ) )
		{
			// Remove the widget from the workspace
			mLauncher.removeAppWidget( (LauncherAppWidgetInfo)item );
			LauncherModel.deleteItemFromDatabase( mLauncher , item );
			final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo)item;
			final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
			if( appWidgetHost != null )
			{
				// Deleting an app widget ID is a void call but writes to disk before returning
				// to the caller...
				new Thread( "deleteAppWidgetId" ) {
					
					public void run()
					{
						appWidgetHost.deleteAppWidgetId( launcherAppWidgetInfo.getAppWidgetId() );
					}
				}.start();
			}
		}
		if( wasWaitingForUninstall && !mWaitingForUninstall )
		{
			if( d.dragSource instanceof Folder )
			{
				( (Folder)d.dragSource ).onUninstallActivityReturned( false );
			}
			else if( d.dragSource instanceof Workspace )
			{
				( (Workspace)d.dragSource ).onUninstallActivityReturned( false );
			}
		}
	}
	
	public void onDrop(
			DragObject d )
	{
		animateToTrashAndCompleteDrop( d );
	}
	
	/**
	 * Creates an animation from the current drag view to the delete trash icon.
	 */
	private AnimatorUpdateListener createFlingToTrashAnimatorListener(
			final DragLayer dragLayer ,
			DragObject d ,
			PointF vel ,
			ViewConfiguration config )
	{
		final Rect to = getIconRect( d.dragView.getMeasuredWidth() , d.dragView.getMeasuredHeight() , mCurrentDrawable.getIntrinsicWidth() , mCurrentDrawable.getIntrinsicHeight() );
		final Rect from = new Rect();
		dragLayer.getViewRectRelativeToSelf( d.dragView , from );
		// Calculate how far along the velocity vector we should put the intermediate point on
		// the bezier curve
		float velocity = Math.abs( vel.length() );
		float vp = Math.min( 1f , velocity / ( config.getScaledMaximumFlingVelocity() / 2f ) );
		int offsetY = (int)( -from.top * vp );
		int offsetX = (int)( offsetY / ( vel.y / vel.x ) );
		final float y2 = from.top + offsetY; // intermediate t/l
		final float x2 = from.left + offsetX;
		final float x1 = from.left; // drag view t/l
		final float y1 = from.top;
		final float x3 = to.left; // delete target t/l
		final float y3 = to.top;
		final TimeInterpolator scaleAlphaInterpolator = new TimeInterpolator() {
			
			@Override
			public float getInterpolation(
					float t )
			{
				return t * t * t * t * t * t * t * t;
			}
		};
		return new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				final DragView dragView = (DragView)dragLayer.getAnimatedView();
				float t = ( (Float)animation.getAnimatedValue() ).floatValue();
				float tp = scaleAlphaInterpolator.getInterpolation( t );
				float initialScale = dragView.getInitialScale();
				float finalAlpha = 0.5f;
				float scale = dragView.getScaleX();
				float x1o = ( ( 1f - scale ) * dragView.getMeasuredWidth() ) / 2f;
				float y1o = ( ( 1f - scale ) * dragView.getMeasuredHeight() ) / 2f;
				float x = ( 1f - t ) * ( 1f - t ) * ( x1 - x1o ) + 2 * ( 1f - t ) * t * ( x2 - x1o ) + ( t * t ) * x3;
				float y = ( 1f - t ) * ( 1f - t ) * ( y1 - y1o ) + 2 * ( 1f - t ) * t * ( y2 - x1o ) + ( t * t ) * y3;
				dragView.setTranslationX( x );
				dragView.setTranslationY( y );
				dragView.setScaleX( initialScale * ( 1f - tp ) );
				dragView.setScaleY( initialScale * ( 1f - tp ) );
				dragView.setAlpha( finalAlpha + ( 1f - finalAlpha ) * ( 1f - tp ) );
			}
		};
	}
	
	/**
	 * Creates an animation from the current drag view along its current velocity vector.
	 * For this animation, the alpha runs for a fixed duration and we update the position
	 * progressively.
	 */
	private static class FlingAlongVectorAnimatorUpdateListener implements AnimatorUpdateListener
	{
		
		private DragLayer mDragLayer;
		private PointF mVelocity;
		private Rect mFrom;
		private long mPrevTime;
		private boolean mHasOffsetForScale;
		private float mFriction;
		private final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator( 0.75f );
		
		public FlingAlongVectorAnimatorUpdateListener(
				DragLayer dragLayer ,
				PointF vel ,
				Rect from ,
				long startTime ,
				float friction )
		{
			mDragLayer = dragLayer;
			mVelocity = vel;
			mFrom = from;
			mPrevTime = startTime;
			mFriction = 1f - ( dragLayer.getResources().getDisplayMetrics().density * friction );
		}
		
		@Override
		public void onAnimationUpdate(
				ValueAnimator animation )
		{
			final DragView dragView = (DragView)mDragLayer.getAnimatedView();
			float t = ( (Float)animation.getAnimatedValue() ).floatValue();
			long curTime = AnimationUtils.currentAnimationTimeMillis();
			if( !mHasOffsetForScale )
			{
				mHasOffsetForScale = true;
				float scale = dragView.getScaleX();
				float xOffset = ( ( scale - 1f ) * dragView.getMeasuredWidth() ) / 2f;
				float yOffset = ( ( scale - 1f ) * dragView.getMeasuredHeight() ) / 2f;
				mFrom.left += xOffset;
				mFrom.top += yOffset;
			}
			mFrom.left += ( mVelocity.x * ( curTime - mPrevTime ) / 1000f );
			mFrom.top += ( mVelocity.y * ( curTime - mPrevTime ) / 1000f );
			dragView.setTranslationX( mFrom.left );
			dragView.setTranslationY( mFrom.top );
			dragView.setAlpha( 1f - mAlphaInterpolator.getInterpolation( t ) );
			mVelocity.x *= mFriction;
			mVelocity.y *= mFriction;
			mPrevTime = curTime;
		}
	};
	
	private AnimatorUpdateListener createFlingAlongVectorAnimatorListener(
			final DragLayer dragLayer ,
			DragObject d ,
			PointF vel ,
			final long startTime ,
			final int duration ,
			ViewConfiguration config )
	{
		final Rect from = new Rect();
		dragLayer.getViewRectRelativeToSelf( d.dragView , from );
		return new FlingAlongVectorAnimatorUpdateListener( dragLayer , vel , from , startTime , FLING_TO_DELETE_FRICTION );
	}
	
	public void onFlingToDelete(
			final DragObject d ,
			int x ,
			int y ,
			PointF vel )
	{
		final boolean isAllApps = d.dragSource instanceof AppsCustomizePagedView;
		// Don't highlight the icon as it's animating
		d.dragView.setColor( 0 );
		d.dragView.updateInitialScaleToCurrentScale();
		// Don't highlight the target if we are flinging from AllApps
		if( isAllApps )
		{
			resetHoverColor();
		}
		if( mFlingDeleteMode == MODE_FLING_DELETE_TO_TRASH )
		{
			// Defer animating out the drop target if we are animating to it
			mSearchDropTargetBar.deferOnDragEnd();
			mSearchDropTargetBar.finishAnimations();
		}
		final ViewConfiguration config = ViewConfiguration.get( mLauncher );
		final DragLayer dragLayer = mLauncher.getDragLayer();
		final int duration = FLING_DELETE_ANIMATION_DURATION;
		final long startTime = AnimationUtils.currentAnimationTimeMillis();
		// NOTE: Because it takes time for the first frame of animation to actually be
		// called and we expect the animation to be a continuation of the fling, we have
		// to account for the time that has elapsed since the fling finished.  And since
		// we don't have a startDelay, we will always get call to update when we call
		// start() (which we want to ignore).
		final TimeInterpolator tInterpolator = new TimeInterpolator() {
			
			private int mCount = -1;
			private float mOffset = 0f;
			
			@Override
			public float getInterpolation(
					float t )
			{
				if( mCount < 0 )
				{
					mCount++;
				}
				else if( mCount == 0 )
				{
					mOffset = Math.min( 0.5f , (float)( AnimationUtils.currentAnimationTimeMillis() - startTime ) / duration );
					mCount++;
				}
				return Math.min( 1f , mOffset + t );
			}
		};
		AnimatorUpdateListener updateCb = null;
		if( mFlingDeleteMode == MODE_FLING_DELETE_TO_TRASH )
		{
			updateCb = createFlingToTrashAnimatorListener( dragLayer , d , vel , config );
		}
		else if( mFlingDeleteMode == MODE_FLING_DELETE_ALONG_VECTOR )
		{
			updateCb = createFlingAlongVectorAnimatorListener( dragLayer , d , vel , startTime , duration , config );
		}
		deferCompleteDropIfUninstalling( d );
		Runnable onAnimationEndRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				// If we are dragging from AllApps, then we allow AppsCustomizePagedView to clean up
				// itself, otherwise, complete the drop to initiate the deletion process
				if( !isAllApps )
				{
					mLauncher.exitSpringLoadedDragMode();
					completeDrop( d );
				}
				mLauncher.getDragController().onDeferredEndFling( d );
			}
		};
		dragLayer.animateView( d.dragView , updateCb , duration , tInterpolator , onAnimationEndRunnable , DragLayer.ANIMATION_END_DISAPPEAR , null );
	}
	
	public boolean isWaitingForUninstall()
	{
		return mWaitingForUninstall;
	}
	
	private boolean isUninstallFromWorkspace(
			DragSource source ,
			Object info )
	{
		DragObject mDragObject = new DragObject();
		mDragObject.dragSource = source;
		mDragObject.dragInfo = info;
		return isUninstallFromWorkspace( mDragObject );
	}
	
	/**
	 * 删除灰色图标
	 * @param dragSource 长按拖动图标的父容器
	 * @param packageName	要删除的包名
	 */
	public void deleteGreyApp(
			DragSource dragSource ,
			String packageName )
	{
		if( dragSource == null || packageName == null || mLauncher == null )
		{
			return;
		}
		//		mWaitingForUninstall = true;//cheyingkun del 	//T卡挂载状态,删除灰色图标,无法长按第二个桌面图标(bug:0009973)
		//直接删除桌面图标
		if( dragSource instanceof Folder )
		{
			( (Folder)dragSource ).onUninstallActivityReturned( true );
		}
		else if( dragSource instanceof Workspace )
		{
			( (Workspace)dragSource ).onUninstallActivityReturned( true );
		}
		//cheyingkun add start	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
		//删除灰色图标时,更新保存T卡应用的文件信息
		TCardMountManager mTCardMountManager = TCardMountManager.getInstance( mContext );
		mTCardMountManager.readFromFile();//从文件中读取挂载信息
		Map<Intent , Bitmap> mountInfo = mTCardMountManager.getMountInfo();//得到挂载信息的map
		Set<Intent> keySet = mountInfo.keySet();
		ArrayList<Intent> deleteAppIntentList = new ArrayList<Intent>();//cheyingkun add	//解决“删除灰色图标并更新挂载信息时,待删除图标的包名与挂载信息中多条记录的包名匹配,则删除多条挂载信息”的问题
		for( Intent intent : keySet )
		{
			if( intent.getComponent().getPackageName().equals( packageName ) )//删除map中对应的key
			{
				//cheyingkun start	//解决“删除灰色图标并更新挂载信息时,待删除图标的包名与挂载信息中多条记录的包名匹配,则删除多条挂载信息”的问题
				//cheyingkun del start
				//				mountInfo.remove( intent );
				//				break;
				//cheyingkun del end
				deleteAppIntentList.add( intent );//cheyingkun add
				//cheyingkun end
			}
		}
		//cheyingkun add start	//解决“删除灰色图标并更新挂载信息时,待删除图标的包名与挂载信息中多条记录的包名匹配,则删除多条挂载信息”的问题
		for( Intent intent : deleteAppIntentList )
		{
			mountInfo.remove( intent );
		}
		deleteAppIntentList.clear();
		//cheyingkun end
		mTCardMountManager.writeToFile();//把map写到文件
		//cheyingkun add end
		//删除数据库中的信息
		String strRemoveGreyApp = LauncherAppState.REMOVE_TCRADMOUNT_GRAY_APP;
		Intent removeGreyAppIntent = new Intent( strRemoveGreyApp );
		removeGreyAppIntent.putExtra( strRemoveGreyApp , packageName );
		mLauncher.sendBroadcast( removeGreyAppIntent );
	}
	//cheyingkun add end
	;
	
}
