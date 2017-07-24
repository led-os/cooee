package com.cooee.phenix.data;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.CellLayout;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.util.Tools;


/**
 * Represents a launchable icon on the workspaces and in folders.
 */
//添加智能分类功能 , change by shlt@2015/02/09 UPD START
//class ShortcutInfo extends ItemInfo
public class ShortcutInfo extends EnhanceItemInfo
//
implements IOnThemeChanged //zhujieping add,换主题不重启
//添加智能分类功能 , change by shlt@2015/02/09 UPD END
{
	
	private static final String TAG = "ShortcutInfo";
	//	private static final long serialVersionUID = 1L;
	/**
	 * The intent used to start the application.
	 */
	Intent intent;
	/**
	 * Indicates whether the icon comes from an application's resource (if false)
	 * or from a custom Bitmap (if true.)
	 */
	boolean customIcon;
	/**
	 * Indicates whether we're using the default fallback icon instead of something from the
	 * app.
	 */
	//添加智能分类功能 , change by shlt@2015/02/12 UPD START
	//boolean usingFallbackIcon;
	boolean usingFallbackIcon;
	//添加智能分类功能 , change by shlt@2015/02/12 UPD END
	/**
	 * If isShortcut=true and customIcon=false, this contains a reference to the
	 * shortcut icon as an application's resource.
	 */
	Intent.ShortcutIconResource iconResource;
	/**
	 * The application icon.
	 */
	//添加智能分类功能 , change by shlt@2015/02/12 UPD START
	//private Bitmap mIcon;
	protected Bitmap mIcon;
	//添加智能分类功能 , change by shlt@2015/02/12 UPD END
	long firstInstallTime;
	int flags = 0;
	private int shortcutType = LauncherSettings.Favorites.SHORTCUT_TYPE_NORMAL;
	//cheyingkun add start	//解决“常用应用显示动态时，改变日期后返回桌面，酷生活和桌面的动态图标不一样”的问题【i_0014330】
	/**
	 * A bitmap backups version of the application icon.
	 */
	Bitmap iconBitmapBackup;
	
	//cheyingkun add end
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//ShortcutInfo()
	public ShortcutInfo()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	public void setIntent(
			Intent intent )
	{
		this.intent = intent;
		setCanUninstall( intent.getBooleanExtra( IS_CAN_UNINSTALL , true ) );
		//<数据库字段更新> liuhailin@2015-03-24 del begin
		////getExtrasByIntent( intent );
		//<数据库字段更新> liuhailin@2015-03-24 del end
	}
	
	//protected Intent getIntent()
	public Intent getIntent()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		//<数据库字段更新> liuhailin@2015-03-24 del begin
		////intent = addExtrasToIntent( intent );
		//<数据库字段更新> liuhailin@2015-03-24 del end
		return intent;
	}
	
	public ShortcutInfo(
			Context context ,
			ShortcutInfo info )
	{
		super( info );
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
		//title = info.title.toString();
		if( info.getTitle() == null )
		{
			info.setTitle( "name = null" );
		}
		super.setTitle( info.getTitle().toString() );
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
		intent = new Intent( info.getIntent() );
		if( info.getIconResource() != null )
		{
			iconResource = new Intent.ShortcutIconResource();
			iconResource.packageName = info.getIconResource().packageName;
			iconResource.resourceName = info.getIconResource().resourceName;
		}
		mIcon = info.mIcon; // TODO: should make a copy here.  maybe we don't need this ctor at all
		customIcon = info.getIsCustomIcon();
		if( !( intent.getComponent() == null || intent.getComponent().getPackageName() == null ) )
		{
			initFlagsAndFirstInstallTime( getPackageInfo( context , intent.getComponent().getPackageName() ) );
		}
	}
	
	/** TODO: Remove this.  It's only called by ApplicationInfo.makeShortcut. */
	public ShortcutInfo(
			AppInfo info )
	{
		super( info );
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
		//title = info.title.toString();
		if( info.getTitle() == null )
		{
			info.setTitle( "name = null" );
		}
		super.setTitle( info.getTitle().toString() );
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
		intent = new Intent( info.getIntent() );
		customIcon = false;
		flags = info.getFlags();
		firstInstallTime = info.getFirstInstallTime();
	}
	
	public static PackageInfo getPackageInfo(
			Context context ,
			String packageName )
	{
		PackageInfo pi = null;
		try
		{
			PackageManager pm = context.getPackageManager();
			pi = pm.getPackageInfo( packageName , 0 );
		}
		catch( NameNotFoundException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "ShortcutInfo" , StringUtils.concat( "PackageManager.getPackageInfo failed for " , packageName ) );
		}
		return pi;
	}
	
	public void initFlagsAndFirstInstallTime(
			PackageInfo pi )
	{
		flags = AppInfo.initFlags( pi );
		firstInstallTime = AppInfo.initFirstInstallTime( pi );
	}
	
	public void setIcon(
			Bitmap b )
	{
		mIcon = b;
	}
	
	public Bitmap getIcon(
			IconCache iconCache )
	{
		if( mIcon == null )
		{
			updateIcon( iconCache );
		}
		//cheyingkun add start	//解决“动态图标几率性显示透明的问题”的问题【c_0004400】
		if( mIcon.isRecycled() )//如果图标被释放,则更新图标
		{
			if( intent != null )
			{
				iconCache.remove( intent.getComponent() );
			}
			updateIcon( iconCache );
		}
		//cheyingkun add end
		//cheyingkun add start	//TCardMountT9SearchError(T卡挂载,灰色图标状态下,使用T9搜索搜到挂载的应用,点击后桌面异常终止)
		if( getIconUnavailable() == null//
				&& !getAvailable()//cheyingkun add	//解决“底边栏的百度浏览器图标变成机器人”的问题【i_0011643】
		// 
		)
		{
			setIconUnavailable( Tools.getGrayBitmap( mIcon ) );
		}
		return getAvailable() ? mIcon : getIconUnavailable();//如果可用,返回原图标bitmap,如果不可用,返回灰化后的图标
		//cheyingkun add end
	}
	
	public Bitmap getIcon()
	{
		return mIcon;
	}
	
	public void updateIcon(
			IconCache iconCache )
	{
		if( isOperateVirtualMoreAppItem() )
		{
			mIcon = iconCache.getOperateVirtualMoreAppIcon();
		}
		else
		{
			mIcon = iconCache.getIcon( intent );
		}
		usingFallbackIcon = iconCache.isDefaultIcon( mIcon );
	}
	
	/**
	 * Creates the application intent based on a component name and various launch flags.
	 * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
	 *
	 * @param className the class name of the component representing the intent
	 * @param launchFlags the launch flags
	 */
	public final void setActivity(
			Context context ,
			ComponentName className ,
			int launchFlags )
	{
		intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_LAUNCHER );
		intent.setComponent( className );
		intent.setFlags( launchFlags );
		itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
		initFlagsAndFirstInstallTime( getPackageInfo( context , intent.getComponent().getPackageName() ) );
	}
	
	@Override
	public void onAddToDatabase(
			ContentValues values )
	{
		super.onAddToDatabase( values );
		String titleStr = title != null ? title.toString() : null;
		values.put( LauncherSettings.BaseLauncherColumns.TITLE , titleStr );
		//<数据库字段更新> liuhailin@2015-03-23 del begin
		////添加智能分类功能 , change by shlt@2015/02/09 UPD START
		//xiatian start	//fix bug：解决“先打开一个应用，再进行智能分类后，数据库中该数据的intent中，多了附加参数‘SourceBounds’”的问题。
		//【引起的问题】
		//		1、桌面中打开微信后进行智能分类
		//		2、清空微信数据后再次打开微信，并在微信的“询问是否创建快捷方式”界面选择创建快捷方式
		//		3、此时，能在桌面创建快捷方式
		//【问题原因】
		//		1、ShortcutInfo的intent中的附加参数‘SourceBounds’，实在launcher.java中的	public void onClick( View v )方法中添加的
		//		2、ShortcutInfo的intent中的附加参数‘SourceBounds’的作用是：在startActivity的时候，将图标在桌面中的位置信息当做附加参数传递出去，以备打开的activity的onCreate方法中使用
		//		3、进行智能分类时，会调用本方法，将intent中的附加参数‘SourceBounds’写入数据库中
		//		4、LauncherModel的static boolean shortcutExists( Context context ,String title ,Intent intent )方法中，由于数据库中的intent多了附加参数‘SourceBounds’导致错误。
		//【解决方案】
		//		本方法中，不将intent中的附加参数‘SourceBounds’在存入数据库。
		//		String uri = intent != null ? intent.toUri( 0 ) : null;//xiatian del
		//xiatian add start
		String uri = null;
		if( intent != null )
		{
			Intent mIntent = new Intent( intent );
			mIntent.setSourceBounds( null );
			uri = mIntent.toUri( 0 );
		}
		//xiatian add end
		//xiatian end
		//String uri = null;
		//if( intent != null )
		//{
		//	intent.putExtra( "categoryFolderId" , getCategoryFolderId() );
		//	//<数据库字段更新> liuhailin@2015-03-23 del begin
		//	//intent.putExtra( "isHotseatDefaultItem" , isHotseatDefaultItem() );
		//	//intent.putExtra( "isFirstPageDefault8Items" , isFirstPageDefault8Items() );
		//	//<数据库字段更新> liuhailin@2015-03-23 del end
		//	intent.putExtra( "canUninstall" , isCanUninstall() );
		//	//
		//	uri = intent.toUri( 0 );
		//}
		////添加智能分类功能 , change by shlt@2015/02/09 UPD END
		//<数据库字段更新> liuhailin@2015-03-23 del end
		values.put( LauncherSettings.BaseLauncherColumns.INTENT , uri );
		if( customIcon )
		{
			values.put( LauncherSettings.BaseLauncherColumns.ICON_TYPE , LauncherSettings.BaseLauncherColumns.ICON_TYPE_BITMAP );
			writeBitmap( values , mIcon );
		}
		else
		{
			if( !usingFallbackIcon )
			{
				writeBitmap( values , mIcon );
			}
			values.put( LauncherSettings.BaseLauncherColumns.ICON_TYPE , LauncherSettings.BaseLauncherColumns.ICON_TYPE_RESOURCE );
			if( iconResource != null )
			{
				values.put( LauncherSettings.BaseLauncherColumns.ICON_PACKAGE , iconResource.packageName );
				values.put( LauncherSettings.BaseLauncherColumns.ICON_RESOURCE , iconResource.resourceName );
			}
		}
	}
	
	@Override
	public String toString()
	{
		return StringUtils.concat(
				TAG ,
				"(title:" ,
				( title == null ? "null" : title.toString() ) ,
				"-intent:" ,
				( intent == null ? "null" : intent.toUri( 0 ) ) ,
				"-id:" ,
				getId() ,
				"-type:" ,
				getItemType() ,
				"-container:" ,
				getContainer() ,
				"-screen:" ,
				getScreenId() ,
				"-cellX:" ,
				getCellX() ,
				"-cellY:" ,
				getCellY() ,
				"-spanX:" ,
				getSpanX() ,
				"-spanY:" ,
				getSpanY() ,
				"-dropPos:" ,
				getDropPos() ,
				")" );
	}
	
	public static void dumpShortcutInfoList(
			String tag ,
			String label ,
			ArrayList<ShortcutInfo> list )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( tag , StringUtils.concat( label , " size=" , list.size() ) );
			for( ShortcutInfo info : list )
			{
				Log.d( tag , StringUtils.concat( "   title=\"" , info.title , " icon=" + info.getIcon() , " isCustomIcon=" , info.getIsCustomIcon() ) );
			}
		}
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public int getFlags()
	{
		return flags;
	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	
	public int getShortcutType()
	{
		return shortcutType;
	}
	
	public void setShortcutType(
			int shortcutType )
	{
		this.shortcutType = shortcutType;
	}
	
	public long getFirstInstallTime()
	{
		return firstInstallTime;
	}
	
	public void setFirstInstallTime(
			long firstInstallTime )
	{
		this.firstInstallTime = firstInstallTime;
	}
	
	public boolean getIsCustomIcon()
	{
		return customIcon;
	}
	
	public void setIsCustomIcon(
			boolean customIcon )
	{
		this.customIcon = customIcon;
	}
	
	public Intent.ShortcutIconResource getIconResource()
	{
		return iconResource;
	}
	
	public void setIconResource(
			Intent.ShortcutIconResource iconResource )
	{
		this.iconResource = iconResource;
	}
	
	public boolean getIsUsingFallbackIcon()
	{
		return usingFallbackIcon;
	}
	
	public void setIsUsingFallbackIcon(
			boolean usingFallbackIcon )
	{
		this.usingFallbackIcon = usingFallbackIcon;
	}
	
	//cheyingkun add start	//TCardMountT9SearchError(T卡挂载,灰色图标状态下,使用T9搜索搜到挂载的应用,点击后桌面异常终止)
	public void setAvailable(
			boolean available )
	{
		super.setAvailable( available );
		if( this.getAvailable() == available )
		{
			return;
		}
		if( !available )//如果设置为不可用,图标灰色处理
		{
			if( getIconUnavailable() == null )
			{
				setIconUnavailable( Tools.getGrayBitmap( mIcon ) );
			}
		}
		else
		{
			if( getIconUnavailable() != null )
			{
				setIconUnavailable( null );
			}
		}
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
	public VirtualInfo makeVirtual()
	{
		return new VirtualInfo( this );
	}
	//xiatian add end
	;
	
	//xiatian add start	//整理代码：整理接口willAcceptDrop
	@Override
	public boolean willAcceptDrop()
	{
		// zhujieping@2015/07/28 ADD START
		if( getShortcutType() == LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DYNAMIC_ICON_DELETE )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		// zhujieping@2015/07/28 ADD END 
		boolean ret = false;
		ret = super.willAcceptDrop();
		if( ret == false )
		{
			if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION )
			{
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
				{
					return ( getFlags() & AppInfo.DOWNLOADED_FLAG ) != 0;
				}
				else
				{
					return true;
				}
			}
			else if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT )
			{
				return isCanUninstall();
			}
			else if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL )
			{//虚图标是否可删除
				VirtualInfo mVirtualInfo = makeVirtual();
				return mVirtualInfo.willAcceptDrop();
			}
		}
		return ret;
	}
	//xiatian add end
	;
	
	//xiatian add start	//整理代码：整理接口creatView
	public View creatView(
			Launcher mLauncher ,
			CellLayout cellLayout ,
			IconCache mIconCache )
	{
		super.creatView( mLauncher , cellLayout , mIconCache );
		return mLauncher.createShortcut( R.layout.application , cellLayout , this );
	}
	
	//xiatian add end
	// zhujieping@2015/07/14 ADD START
	public boolean isOperateIconItem()//是否为运营图标
	{
		if( intent != null )
		{
			return( intent.getStringExtra( OperateDynamicMain.FOLDER_VERSION ) != null );
		}
		return false;
	}
	
	// zhujieping@2015/07/14 ADD END
	//cheyingkun add start	//解决“常用应用显示动态时，改变日期后返回桌面，酷生活和桌面的动态图标不一样”的问题【i_0014330】
	public Bitmap getIconBitmapBackup()
	{
		return iconBitmapBackup;
	}
	
	public void setIconBitmapBackup(
			Bitmap iconBitmapBackup )
	{
		if( this.iconBitmapBackup != null && this.iconBitmapBackup.isRecycled() )
		{
			this.iconBitmapBackup.recycle();
			this.iconBitmapBackup = null;
		}
		this.iconBitmapBackup = iconBitmapBackup;
	}
	
	//cheyingkun add end
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
		updateIconWhenOnThemeChanged( (IconCache)arg0 );
	}
	
	public void updateIconWhenOnThemeChanged(
			IconCache iconCache )
	{
		//这里不释放ShortcutInfo的mIcon，释放ShortcutInfo的mIcon的相关操作，放在BubbleTextView的onThemeChanged方法中
		if( isOperateVirtualMoreAppItem() )
		{
			mIcon = iconCache.getOperateVirtualMoreAppIcon();
		}
		//zhujieping add start	//切换主题时launcher重启以及更换主题，运营图标显示不正确
		else if( isOperateIconItem() || isOperateVirtualItem() )
		{
			String path = intent.getStringExtra( OperateDynamicUtils.BITMAP_PATH_KEY );
			if( path != null )
			{
				Bitmap bitmap;
				try
				{
					bitmap = BitmapFactory.decodeStream( LauncherAppState.getActivityInstance().getAssets().open( path ) );
				}
				catch( IOException ex )
				{
					bitmap = BitmapFactory.decodeFile( path );
				}
				if( bitmap != null )
				{
					mIcon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bitmap , LauncherAppState.getActivityInstance() , true );
				}
			}
			else
			{
				mIcon = iconCache.getIcon( intent );
			}
		}
		//zhujieping add end
		else
		{
			int mItemType = getItemType();
			if( mItemType == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL )
			{
				mIcon = iconCache.getIcon( intent.getComponent() , null , null );
				saveBitmap( mIcon , getTitle() + ".png" );
			}
			else
			{//ITEM_TYPE_SHORTCUT和ITEM_TYPE_APPLICATION
				if( mItemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT )
				{
					//图片的释放都放到iconcache的onrecycle方法中
					if( customIcon )
					{
						Bitmap mBitmap = getIconFromCursor();
						if( mBitmap != null )
						{
							mIcon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( mBitmap , LauncherAppState.getInstance().getContext() , true );
							usingFallbackIcon = false;
							return;
						}
					}
					if( defaultWorkspaceItemType != LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE_NONE && getIntent() != null && getIntent().getComponent() != null )
					{
						String mPackageName = getIntent().getComponent().getPackageName();
						String mClassName = getIntent().getComponent().getClassName();
						Bitmap mBitmap = iconCache.reloadIcon( mPackageName , mClassName , iconResource , null , mIcon );//对于默认配置的快捷方式图标，是配置了特定图标的，换主题时，需要对配置的特定图标进行处理，而不是已经处理过的mIcon
						if( mBitmap != null )
						{
							iconCache.add( new ComponentName( mPackageName , mClassName ) , mBitmap , getTitle() );
						}
					}
				}
				mIcon = iconCache.getIcon( intent );
			}
		}
		usingFallbackIcon = iconCache.isDefaultIcon( mIcon );
	}
	
	public void saveBitmap(
			Bitmap bm ,
			String name )
	{
		Log.e( TAG , "保存图片" );
		File f = new File( "mnt/sdcard/cooee/" , name );
		if( f.exists() )
		{
			f.delete();
		}
		try
		{
			FileOutputStream out = new FileOutputStream( f );
			bm.compress( Bitmap.CompressFormat.PNG , 90 , out );
			out.flush();
			out.close();
			Log.i( TAG , "已经保存" );
		}
		catch( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Bitmap getIconFromCursor()
	{
		Bitmap ret = null;
		Context mContext = LauncherAppState.getInstance().getContext();
		final ContentResolver cr = mContext.getContentResolver();
		Cursor c = cr.query( //
				LauncherSettings.Favorites.CONTENT_URI ,
				//
				new String[]{ LauncherSettings.Favorites._ID , LauncherSettings.Favorites.ICON } ,
				//
				"_id=?" ,
				//
				new String[]{ "" + getId() } ,
				//
				null//
				);
		try
		{
			if( c.moveToFirst() )
			{
				final int iconIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON );
				byte[] data = c.getBlob( iconIndex );
				ret = BitmapFactory.decodeByteArray( data , 0 , data.length );
			}
		}
		catch( SQLException ex )
		{
		}
		finally
		{
			if( c != null )
			{
				c.close();
			}
		}
		if( c != null )
		{
			c.close();
		}
		return ret;
	}
	//zhujieping add end
}
