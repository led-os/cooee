package com.cooee.phenix;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.config.defaultConfig.LauncherIconBaseConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.iconhouse.IconHouseManager;
import com.cooee.theme.ThemeManager;
import com.cooee.util.Tools;


/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache
//
implements IOnThemeChanged//zhujieping add	//换主题不重启
{
	
	@SuppressWarnings( "unused" )
	private static final String TAG = "Launcher.IconCache";
	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
	
	private static class CacheEntry
	{
		
		public Bitmap icon;
		public String title;
	}
	
	private final Bitmap mDefaultIcon;
	private final Context mContext;
	private final PackageManager mPackageManager;
	private final HashMap<ComponentName , CacheEntry> mCache = new HashMap<ComponentName , CacheEntry>( INITIAL_ICON_CACHE_CAPACITY );
	//cheyingkun add start	//是否优先获取高分辨率图标（图标显示清晰）。true为优先获取高分辨率图标，没有高分辨图标则获取低分辨率图标；false为直接获取当前分辨率图标。默认为false。
	//屏幕密度
	private final int lIconDpi = 120;
	private final int mIconDpi = 160;
	private final int hIconDpi = 240;
	private final int xhIConDpi = 320;
	private final int xxhIConDpi = 480;
	private ArrayList<Integer> iconDpiList;
	private int currentIconDpi;//当前屏幕密度
	private boolean switchEnableHigherDipIconSize = false;//是否是否优先获取高分辨率图标（图标显示清晰）。true为优先获取高分辨率图标，没有高分辨图标则获取低分辨率图标；false为直接获取当前分辨率图标。默认为false。
	private final HashMap<ComponentName , CacheEntry> mTempCache = new HashMap<ComponentName , CacheEntry>( INITIAL_ICON_CACHE_CAPACITY );
	//第三方应用的比较标准值(是否获取更高分辨率尺寸下图标的比较值的宽和高)
	public IconCache(
			Context context )
	{
		ActivityManager activityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		mContext = context;
		mPackageManager = context.getPackageManager();
		//cheyingkun add start	//是否优先获取高分辨率图标（图标显示清晰）。true为优先获取高分辨率图标，没有高分辨图标则获取低分辨率图标；false为直接获取当前分辨率图标。默认为false。
		currentIconDpi = activityManager.getLauncherLargeIconDensity();
		iconDpiList = new ArrayList<Integer>();
		iconDpiList.add( lIconDpi );
		iconDpiList.add( mIconDpi );
		iconDpiList.add( hIconDpi );
		iconDpiList.add( xhIConDpi );
		iconDpiList.add( xxhIConDpi );
		switchEnableHigherDipIconSize = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_higher_dip_icon_size );
		//cheyingkun add end
		// need to set mIconDpi before getting default icon
		mDefaultIcon = makeDefaultIcon();
	}
	
	//cheyingkun add start	//是否优先获取高分辨率图标（图标显示清晰）。true为优先获取高分辨率图标，没有高分辨图标则获取低分辨率图标；false为直接获取当前分辨率图标。默认为false。
	public Drawable getFullResDefaultActivityIcon()
	{
		return getFullResIcon( Resources.getSystem() , android.R.mipmap.sym_def_app_icon , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight );
	}
	
	/** 
	 * 获取第三方图标
	 * @param resources
	 * @param iconId
	 * @param mIconDestWidthMin 获取应用图标时,目标宽度的最小值
	 * @param mIconDestHeightMin 获取应用图标时,目标高度的最小值
	 * @return 返回获取到的图标,拿不到图标是返回系统默认的机器人(如果当前屏幕密度下的图标不满足目标尺寸的最小值,则依次获取高分辨率下的图标进行比较)不会改变图片大小,只是获取图片
	 */
	public Drawable getFullResIcon(
			Resources resources ,
			int iconId ,
			float mIconDestMinWidth ,
			float mIconDestMinHeight )
	{
		Drawable d = null;
		try
		{
			if( switchEnableHigherDipIconSize )
			{
				//index当前屏幕密度在列表中的位置
				int index = Collections.binarySearch( iconDpiList , currentIconDpi );
				//如果index<0 表示当前屏幕密度不在常用的五个密度中
				//把当前密度加入列表
				if( index < 0 )
				{
					index = -( index + 1 );
					iconDpiList.add( index , currentIconDpi );
				}
				//循环当前屏幕密度到最大屏幕密度
				for( int i = index ; i < iconDpiList.size() ; i++ )
				{
					//拿到图标
					Drawable tempDrawable = resources.getDrawableForDensity( iconId , iconDpiList.get( i ) );
					if( tempDrawable != null )
					{
						d = tempDrawable;
						//去掉原图的透明像素
						Bitmap cutTransparentPixels = Utilities.cutTransparentPixels( Tools.drawableToBitmap( d ) , true );
						d = Utilities.createIconDrawable( cutTransparentPixels );
						//比较宽高
						int sourceWidth = d.getMinimumWidth();
						int sourceHeight = d.getMinimumHeight();
						//如果拿到图标的宽或者高大于目标尺寸最小的宽高,则跳出循环
						if( mIconDestMinWidth <= sourceWidth || mIconDestMinHeight <= sourceHeight )
						{
							break;
						}
					}
				}
			}
			else
			{
				d = resources.getDrawableForDensity( iconId , currentIconDpi );
			}
		}
		catch( Resources.NotFoundException e )
		{
			d = null;
		}
		return ( d != null ) ? d : getFullResDefaultActivityIcon();
	}
	
	/**
	 * @param packageName
	 * @param iconId
	 * @param mIconDestWidthMin 获取应用图标时,目标宽度的最小值
	 * @param mIconDestHeightMin 获取应用图标时,目标高度的最小值
	 * @return 返回获取到的图标,拿不到图标是返回系统默认的机器人(如果当前屏幕密度下的图标不满足目标尺寸的最小值,则依次获取高分辨率下的图标进行比较)不会改变图片大小,只是获取图片
	 */
	public Drawable getFullResIcon(
			String packageName ,
			int iconId ,
			float mIconDestMinWidth ,
			float mIconDestMinHeight )
	{
		Resources resources;
		try
		{
			resources = mPackageManager.getResourcesForApplication( packageName );
		}
		catch( PackageManager.NameNotFoundException e )
		{
			resources = null;
		}
		if( resources != null )
		{
			if( iconId != 0 )
			{
				return getFullResIcon( resources , iconId , mIconDestMinWidth , mIconDestMinHeight );
			}
		}
		return getFullResDefaultActivityIcon();
	}
	
	/**
	 * @param info
	 * @param mIconDestWidthMin 获取应用图标时,目标宽度的最小值
	 * @param mIconDestHeightMin 获取应用图标时,目标高度的最小值
	 * @return 返回获取到的图标,拿不到图标是返回系统默认的机器人(如果当前屏幕密度下的图标不满足目标尺寸的最小值,则依次获取高分辨率下的图标进行比较)不会改变图片大小,只是获取图片
	 */
	@SuppressWarnings( "deprecation" )
	public Drawable getFullResIcon(
			ActivityInfo info ,
			float mIconDestMinWidth ,
			float mIconDestMinHeight )
	{
		Resources resources;
		try
		{
			resources = mPackageManager.getResourcesForApplication( info.applicationInfo );
		}
		catch( PackageManager.NameNotFoundException e )
		{
			resources = null;
		}
		//cheyingkun add start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "TCardMount" , "获取图标出错" );
			resources = null;
		}
		//cheyingkun add end
		if( resources != null )
		{
			int iconId = info.getIconResource();
			if( iconId != 0 )
			{
				return getFullResIcon( resources , iconId , mIconDestMinWidth , mIconDestMinHeight );
			}
		}
		//cheyingkun add start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
		TCardMountManager mTCardMountManager = TCardMountManager.getInstance( LauncherAppState.getInstance().getContext() );
		if( mTCardMountManager != null )
		{
			Map<Intent , Bitmap> mountInfo = mTCardMountManager.getMountInfo();//挂载信息
			Set<Intent> keySet = mountInfo.keySet();//挂载信息的key集合
			for( Intent appMountInfoIntent : keySet )
			{
				if( appMountInfoIntent != null && info != null )
				{
					if( appMountInfoIntent.getComponent() != null//
							&& info.applicationInfo != null//
							//cheyingkun start	//重启安装卸载T卡的手机，重启手机，开机时T卡应用正常灰色，变亮后几率性显示机器人。
							//应用的类名在info.name中,不是info.applicationInfo.className
							//							&& appMountInfoIntent.getComponent().getClassName().equals( info.applicationInfo.className )//cheyingkun add	//T卡挂载图标根据包类名或者componentName匹配	//cheyingkun del
							&& appMountInfoIntent.getComponent().getClassName().equals( info.name )//cheyingkun add
							//cheyingkun end
							&& appMountInfoIntent.getComponent().getPackageName().equals( info.applicationInfo.packageName ) )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( "TCardMount" , "getFullResIcon(ActivityInfo) :获取图标出错,显示挂载信息中的灰色图标" );
						//cheyingkun start	//解决“单层模式下，挂载T卡切换至双层模式再切换回单层，灰色图标消失”的问题。【i_0011907】
						//						return new BitmapDrawable( mountInfo.get( appMountInfoIntent ) );//cheyingkun del
						return Utilities.createIconDrawable( mountInfo.get( appMountInfoIntent ) );//cheyingkun add
						//cheyingkun end
					}
				}
			}
		}
		//cheyingkun add end
		//cheyingkun add start	//返回机器人图标时打印log信息
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "TCardMount" , "return getFullResDefaultActivityIcon()" );
		//打印所有挂载信息包类名
		Map<Intent , Bitmap> mountInfo = mTCardMountManager.getMountInfo();//挂载信息
		Set<Intent> keySet = mountInfo.keySet();//挂载信息的key集合
		for( Intent appMountInfoIntent : keySet )
		{
			if( appMountInfoIntent != null )
			{
				ComponentName component = appMountInfoIntent.getComponent();
				if( component != null )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , component.toString() );
				}
				else
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , "component= null" );
				}
			}
		}
		//打印返回机器人应用的包类名
		if( info.applicationInfo != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "TCardMount" , StringUtils.concat( "info: packageName = " , info.applicationInfo.packageName ) );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "TCardMount" , StringUtils.concat( "info: className = " , info.name ) );
		//cheyingkun add end
		return getFullResDefaultActivityIcon();
	}
	
	//cheyingkun add end
	private Bitmap makeDefaultIcon()
	{
		Drawable d = getFullResDefaultActivityIcon();
		Bitmap b = Bitmap.createBitmap( Math.max( d.getIntrinsicWidth() , 1 ) , Math.max( d.getIntrinsicHeight() , 1 ) , Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( b );
		d.setBounds( 0 , 0 , b.getWidth() , b.getHeight() );
		d.draw( c );
		c.setBitmap( null );
		return b;
	}
	
	/**
	 * Remove any records for the supplied ComponentName.
	 */
	public void remove(
			ComponentName componentName )
	{
		remove( componentName , false );
	}
	
	// zhangjin@2015/08/31 ADD START
	/**
	 * Remove any records for the supplied ComponentName.
	 */
	public void remove(
			ComponentName componentName ,
			boolean isRecyle )
	{
		synchronized( mCache )
		{
			CacheEntry entry = mCache.get( componentName );
			mCache.remove( componentName );
			if( isRecyle && entry != null && entry.icon != null && entry.icon.isRecycled() == false )
			{
				entry.icon.recycle();
			}
		}
	}
	
	// zhangjin@2015/08/31 ADD END
	/**
	 * Empty out the cache.
	 */
	public void flush()
	{
		synchronized( mCache )
		{
			mCache.clear();
		}
	}
	
	/**
	 * Empty out the cache that aren't of the correct grid size
	 */
	public void flushInvalidIcons(
			DeviceProfile grid )
	{
		synchronized( mCache )
		{
			Iterator<Entry<ComponentName , CacheEntry>> it = mCache.entrySet().iterator();
			while( it.hasNext() )
			{
				final CacheEntry e = it.next().getValue();
				if( e != null && ( e.icon == null || ( e.icon != null && ( e.icon.getWidth() != grid.getIconWidthSizePx() || e.icon.getHeight() != grid.getIconHeightSizePx() ) ) ) )
				{
					it.remove();
				}
			}
		}
	}
	
	/**
	 * Fill in "application" with the icon and label for "info."
	 */
	public void getTitleAndIcon(
			AppInfo application ,
			ResolveInfo info ,
			HashMap<Object , CharSequence> labelCache )
	{
		synchronized( mCache )
		{
			CacheEntry entry = cacheLocked( application.getComponentName() , info , labelCache );
			//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
			//application.title = entry.title;
			application.setTitle( entry.title );
			//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
			application.setIconBitmap( entry.icon );
		}
	}
	
	//<phenix modify> liuhailin@2015-01-26 modify begin
	public void setIcon(
			ResolveInfo info ,
			Bitmap bitmap )
	{
		ComponentName component = new ComponentName( info.activityInfo.applicationInfo.packageName , info.activityInfo.name );
		if( info == null || component == null )
		{
			if( bitmap != null )
			{
				bitmap.recycle();
			}
			return;
		}
		CacheEntry entry = mCache.get( component );
		if( entry == null )
		{
			entry = new CacheEntry();
			mCache.put( component , entry );
			entry.title = info.loadLabel( mPackageManager ).toString();
			Tools.appTitleFineTune( entry.title );//cheyingkun add	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
			if( entry.title == null )
			{
				entry.title = info.activityInfo.name;
			}
			entry.title = LauncherAppState.getAppReplaceTitle( entry.title , mContext , component );//xiatian add	//桌面支持配置特定的activity的显示名称。
			entry.icon = Utilities.resampleIconBitmap( bitmap , mContext );
		}
		else
		{
			if( entry.icon != null )
			{
				entry.icon.recycle();
				//Log.e( TAG , "IconCache.oldIcon:" + entry.icon + " newIcon:" + bitmap + " old.isRecycle:" + entry.icon.isRecycled() );
			}
			entry.icon = Utilities.resampleIconBitmap( bitmap , mContext );
			mCache.put( component , entry );
		}
	}
	//<phenix modify> liuhailin@2015-01-26 modify end
	;
	
	public Bitmap getIcon(
			Intent intent )
	{
		synchronized( mCache )
		{
			final ResolveInfo resolveInfo = mPackageManager.resolveActivity( intent , 0 );
			ComponentName component = intent.getComponent();
			if( resolveInfo == null || component == null )
			{
				//cheyingkun add start	//GreyIconShowDefaultIcon(T卡挂载情况下,重启launcher,灰色图标显示机器人)
				TCardMountManager mTCardMountManager = TCardMountManager.getInstance( LauncherAppState.getInstance().getContext() );
				if( mTCardMountManager != null )
				{
					Map<Intent , Bitmap> mountInfo = mTCardMountManager.getMountInfo();//挂载信息
					Set<Intent> keySet = mountInfo.keySet();//挂载信息的key集合
					for( Intent appMountInfoIntent : keySet )
					{
						if( appMountInfoIntent != null && intent != null )
						{
							if( appMountInfoIntent.getComponent() != null//
									&& intent.getComponent() != null//
									//cheyingkun start	//T卡挂载图标根据包类名或者componentName匹配
									//									&& appMountInfoIntent.getComponent().getPackageName().equals( intent.getComponent().getPackageName() ) )//cheyingkun del
									&& appMountInfoIntent.getComponent().equals( intent.getComponent() ) )//cheyingkun add
							//cheyingkun end
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.d( "TCardMount" , "getIcon:获取图标出错,显示挂载信息中的灰色图标" );//cheyingkun add	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
								return mountInfo.get( appMountInfoIntent );
							}
						}
					}
				}
				//cheyingkun add end
				//cheyingkun add start	//返回机器人图标时打印log信息
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , "return mDefaultIcon" );
				//打印所有挂载信息包类名
				Map<Intent , Bitmap> mountInfo = mTCardMountManager.getMountInfo();//挂载信息
				Set<Intent> keySet = mountInfo.keySet();//挂载信息的key集合
				for( Intent appMountInfoIntent : keySet )
				{
					if( appMountInfoIntent != null )
					{
						ComponentName componentName = appMountInfoIntent.getComponent();
						if( componentName != null )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "TCardMount" , componentName.toString() );
						}
						else
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "TCardMount" , "componentName= null" );
						}
					}
				}
				//打印返回机器人应用的包类名
				if( component != null )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , component.toString() );
				}
				else
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , "intent : component= null" );
				}
				//cheyingkun add end
				return mDefaultIcon;
			}
			CacheEntry entry = cacheLocked( component , resolveInfo , null );
			return entry.icon;
		}
	}
	
	//添加智能分类功能 , change by shlt@2015/02/10 ADD START
	public Bitmap getVirtualIcon(
			ComponentName componentName ,
			String iconPath )
	{
		synchronized( mCache )
		{
			if( componentName == null )
				return null;
			//
			CacheEntry entry = mCache.get( componentName );
			if( entry == null )
			{
				Bitmap virtualIconCoverBitmap = null;//getVirtualIconCover();
				Bitmap bitmap = null;
				if( TextUtils.isEmpty( iconPath ) )
				{
					bitmap = getVirtualIconDefault();
				}
				else
				{
					bitmap = BitmapFactory.decodeFile( iconPath );
				}
				entry = new CacheEntry();
				mCache.put( componentName , entry );
				entry.icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bitmap , mContext , true , false );
				boolean isNeedScale = false;
				//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
				{
					isNeedScale = false;
				}
				//xiatian add end
				entry.icon = Utilities.combineIcon( mContext , entry.icon , null , null , virtualIconCoverBitmap , isNeedScale , 0 , true , false );
			}
			return entry.icon;
		}
	}
	
	public Bitmap getVirtualIconCover()
	{
		ComponentName virtualIconCoverComponentName = new ComponentName( "com.cooee.oilauncher" , "virtual.icon.cover" );
		CacheEntry virtualIconCoverEntry = mCache.get( virtualIconCoverComponentName );
		if( virtualIconCoverEntry == null )
		{
			virtualIconCoverEntry = new CacheEntry();
			virtualIconCoverEntry.icon = Utilities.createIconBitmap(
					mContext.getResources().getDrawable( R.drawable.icon_download_cover ) ,
					mContext ,
					Utilities.sIconWidth ,
					Utilities.sIconHeight ,
					Utilities.sIconTextureWidth ,
					Utilities.sIconTextureHeight ,
					true );
			mCache.put( virtualIconCoverComponentName , virtualIconCoverEntry );
		}
		return virtualIconCoverEntry.icon.copy( Bitmap.Config.ARGB_8888 , false );
	}
	
	public Bitmap getVirtualIconDefault()
	{
		ComponentName componentName = new ComponentName( "com.cooee.oilauncher" , "virtual.icon.default" );
		CacheEntry virtualIconDefault = mCache.get( componentName );
		if( virtualIconDefault == null )
		{
			virtualIconDefault = new CacheEntry();
			virtualIconDefault.icon = Utilities.createIconBitmap(
					mContext.getResources().getDrawable( R.drawable.operate_virtual_item_default_icon ) ,
					mContext ,
					Utilities.sIconWidth ,
					Utilities.sIconHeight ,
					Utilities.sIconTextureWidth ,
					Utilities.sIconTextureHeight ,
					true );
		}
		return virtualIconDefault.icon.copy( Bitmap.Config.ARGB_8888 , false );
	}
	
	public Bitmap getOperateVirtualMoreAppIcon()
	{
		ComponentName componentName = new ComponentName( "com.cooee.oilauncher" , "operate.more.app" );
		CacheEntry operateMoreAppEntry = mCache.get( componentName );
		if( operateMoreAppEntry == null )
		{
			operateMoreAppEntry = new CacheEntry();
			getOperateMoreAppIcon( operateMoreAppEntry );//cheyingkun add	//解决“分类完成后，文件夹内"更多应用“字样处有块黑影”的问题。【i_0011688】
			mCache.put( componentName , operateMoreAppEntry );
		}
		return operateMoreAppEntry.icon;
	}
	//添加智能分类功能 , change by shlt@2015/02/10 ADD END
	;
	
	public Bitmap getIcon(
			ComponentName component ,
			ResolveInfo resolveInfo ,
			HashMap<Object , CharSequence> labelCache )
	{
		synchronized( mCache )
		{
			if( resolveInfo == null && component == null )
			{
				return null;
			}
			CacheEntry entry = cacheLocked( component , resolveInfo , labelCache );
			return entry.icon;
		}
	}
	
	public boolean isDefaultIcon(
			Bitmap icon )
	{
		return mDefaultIcon == icon;
	}
	
	//<phenix modify> liuhailin@2015-01-29 modify begin
	private CacheEntry cacheLocked(
			ComponentName componentName ,
			ResolveInfo info ,
			HashMap<Object , CharSequence> labelCache )
	{
		CacheEntry entry = mCache.get( componentName );
		if( entry == null )
		{
			if( ( ThemeManager.getInstance() != null ) && ( ThemeManager.getInstance().getCurrentThemeDescription() != null ) )
			{
				LauncherIconBaseConfig.initDefaultIconByResolveInfo( info );
				entry = mCache.get( componentName );
				if( entry == null )
				{
					entry = getCacheEntry( componentName , info , labelCache );
				}
			}
			else
			{
				entry = getCacheEntry( componentName , info , labelCache );
			}
		}
		return entry;
	}
	
	private CacheEntry getCacheEntry(
			ComponentName componentName ,
			ResolveInfo info ,
			HashMap<Object , CharSequence> labelCache )
	{
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , StringUtils.concat( " IconCache getCacheEntry componentName: " , componentName.toString() ) );
		}
		//cheyingkun add end
		CacheEntry entry = new CacheEntry();
		mCache.put( componentName , entry );
		ComponentName key = LauncherModel.getComponentNameFromResolveInfo( info );
		if( labelCache != null && labelCache.containsKey( key ) )
		{
			entry.title = labelCache.get( key ).toString();
		}
		else
		{
			entry.title = info.loadLabel( mPackageManager ).toString();
			Tools.appTitleFineTune( entry.title );//cheyingkun add	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
			if( labelCache != null )
			{
				labelCache.put( key , entry.title );
			}
		}
		if( entry.title == null )
		{
			entry.title = info.activityInfo.name;
		}
		entry.title = LauncherAppState.getAppReplaceTitle( entry.title , mContext , componentName );//xiatian add	//桌面支持配置特定的activity的显示名称。
		float mIconDestMinWidth = 0f;
		float mIconDestMinHeight = 0f;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			mIconDestMinWidth = Utilities.sIconWidth;
			mIconDestMinHeight = Utilities.sIconHeight;
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			mIconDestMinWidth = Utilities.sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE;
			mIconDestMinHeight = Utilities.sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE;
		}
		//xiatian add end
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "IconCache getCacheEntry entry.title: " , entry.title ) );
			Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "IconCache getCacheEntry Utilities.sIconWidth:" , Utilities.sIconWidth , "-Utilities.sIconHeight:" , Utilities.sIconHeight ) );
		}
		//cheyingkun add end
		// zhangjin@2015/08/27 UPD START
		//entry.icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( getFullResIcon( info.activityInfo , mIconDestMinWidth , mIconDestMinHeight ) , mContext , false );
		Bitmap bmp = IconHouseManager.getInstance().getIconHouse( info );
		if( bmp != null )
		{
			// zhangjin@2016/05/06 c_0004234 UPD START
			//entry.icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bmp , mContext , true , false , false );
			entry.icon = Utilities.resampleIconBitmap( bmp , mContext );
			// zhangjin@2016/05/06 UPD END
		}
		else
		{
			if( componentName.getClassName().equals( "com.cooee.wallpaper.host.WallpaperMainActivity" ) )//一键换壁纸不更随主题变化
			{
				entry.icon = Utilities.createIconBitmap(
						getFullResIcon( info.activityInfo , mIconDestMinWidth , mIconDestMinHeight ) ,
						mContext ,
						Utilities.sIconWidth ,
						Utilities.sIconHeight ,
						Utilities.sIconTextureWidth ,
						Utilities.sIconTextureHeight ,
						true );
			}
			else
			{
				entry.icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( getFullResIcon( info.activityInfo , mIconDestMinWidth , mIconDestMinHeight ) , mContext , false );
			}
		}
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0004400" , StringUtils.concat( " IconCache getCacheEntry 2 - entry.title:" , entry.title , "-entry.icon:" + entry.icon ) );
		}
		//cheyingkun add end
		return entry;
	}
	
	//<phenix modify> liuhailin@2015-01-29 modify end
	//cheyingkun add start	//解决“分类完成后，文件夹内"更多应用“字样处有块黑影”的问题。【i_0011688】
	/**
	 * 获取"更多应用"图标
	 * @param operateMoreAppEntry
	 */
	private void getOperateMoreAppIcon(
			CacheEntry operateMoreAppEntry )
	{
		if( operateMoreAppEntry == null )
		{
			return;
		}
		ThemeManager tm = ThemeManager.getInstance();
		if( tm == null )
		{//保护
			operateMoreAppEntry.icon = Utilities.createIconBitmap(
					mContext.getResources().getDrawable( R.drawable.operate_more_app_icon_in_sysytem_theme ) ,
					mContext ,
					Utilities.sIconWidth ,
					Utilities.sIconHeight ,
					Utilities.sIconTextureWidth ,
					Utilities.sIconTextureHeight ,
					true );
		}
		else
		{
			Drawable drawable = null;
			//cheyingkun add start	//解决“分类完成后，文件夹内"更多应用“字样处有块黑影”的问题。【i_0011688】
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				drawable = getItemStyle1OperateMoreAppDrawable();
				if( drawable != null )
				{
					operateMoreAppEntry.icon = Tools.drawableToBitmap( drawable );
					return;
				}
			}
			//cheyingkun add end
			if( tm.getCurrentThemeDescription() != null && !tm.getCurrentThemeDescription().mSystem )
			{//非系统主题使用图标“operate_more_app_icon_in_other_theme”
				drawable = mContext.getResources().getDrawable( R.drawable.operate_more_app_icon_in_other_theme );
			}
			else
			{//系统主题使用图标“operate_more_app_icon_in_sysytem_theme”
				drawable = mContext.getResources().getDrawable( R.drawable.operate_more_app_icon_in_sysytem_theme );
			}
			operateMoreAppEntry.icon = Utilities.createIconBitmap(
					drawable ,
					mContext ,
					Utilities.sIconWidth ,
					Utilities.sIconHeight ,
					Utilities.sIconTextureWidth ,
					Utilities.sIconTextureHeight ,
					true );
			//该图标不随主题的缩放值而缩放
			boolean isNeedScale = false;
			operateMoreAppEntry.icon = Utilities.combineIcon( mContext , operateMoreAppEntry.icon , tm.getIconBg() , tm.getIconMask() , tm.getIconCover() , isNeedScale , 0 , true , false );
		}
	}
	
	private Drawable getItemStyle1OperateMoreAppDrawable()
	{
		Drawable drawable = null;
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE != BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			return null;
		}
		int item_style_1_more_app = ResourceUtils.getDrawableResourceIdByReflectIfNecessary( 0 , mContext.getResources() , mContext.getPackageName() , "item_style_1_more_app" );
		if( item_style_1_more_app <= 0 )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "getItemStyle1OperateMoreAppDrawable" , "do not find Drawable item_style_1_more_app" );
		}
		else
		{
			drawable = mContext.getResources().getDrawable( item_style_1_more_app );
		}
		return drawable;
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//需求：添加配置项“mIsIconFollowTheme”，虚图标的显示图标是否跟随主题（从主题中读取相应图标）。true为跟随主题；false为不跟随主题。默认为true。
	public Bitmap getIcon(
			ComponentName mComponentName ,
			String mTitle )
	{
		synchronized( mCache )
		{
			if( mComponentName == null )
			{
				return null;
			}
			CacheEntry entry = cacheLocked( mComponentName , mTitle );
			if( entry == null )
			{
				return null;
			}
			return entry.icon;
		}
	}
	
	private CacheEntry cacheLocked(
			ComponentName mComponentName ,
			String mTitle )
	{
		CacheEntry entry = mCache.get( mComponentName );
		if( entry == null )
		{
			if( ( ThemeManager.getInstance() != null ) && ( ThemeManager.getInstance().getCurrentThemeDescription() != null ) )
			{
				LauncherIconBaseConfig.initDefaultIconByComponentNameAndTitle( mComponentName , mTitle );
				entry = mCache.get( mComponentName );
			}
		}
		return entry;
	}
	
	public void setIcon(
			ComponentName mComponentName ,
			String mTitle ,
			Bitmap bitmap )
	{
		if( TextUtils.isEmpty( mTitle ) || mComponentName == null )
		{
			if( bitmap != null )
			{
				bitmap.recycle();
			}
			return;
		}
		CacheEntry entry = mCache.get( mComponentName );
		if( entry == null )
		{
			entry = new CacheEntry();
			mCache.put( mComponentName , entry );
			entry.title = mTitle;
			Tools.appTitleFineTune( entry.title );//cheyingkun add	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
			entry.title = LauncherAppState.getAppReplaceTitle( entry.title , mContext , mComponentName );//xiatian add	//桌面支持配置特定的activity的显示名称。
			entry.icon = Utilities.resampleIconBitmap( bitmap , mContext );
		}
		else
		{
			if( entry.icon != null )
			{
				entry.icon.recycle();
				//Log.e( TAG , "IconCache.oldIcon:" + entry.icon + " newIcon:" + bitmap + " old.isRecycle:" + entry.icon.isRecycled() );
			}
			entry.icon = Utilities.resampleIconBitmap( bitmap , mContext );
			mCache.put( mComponentName , entry );
		}
	}
	
	//xiatian add end
	//zhujieping add start
	public Drawable getFullResIcon(
			ResolveInfo info ,
			float mIconDestMinWidth ,
			float mIconDestMinHeight )
	{
		return getFullResIcon( info.activityInfo , mIconDestMinWidth , mIconDestMinHeight );
	}
	
	public void add(
			ComponentName componentName ,
			Bitmap mBitmap ,
			String mTitle )
	{
		synchronized( mCache )
		{
			CacheEntry entry = mCache.get( componentName );
			if( entry == null )
			{
				entry = new CacheEntry();
				entry.icon = Utilities.resampleIconBitmap( mBitmap , mContext );
				entry.title = mTitle;
				mCache.put( componentName , entry );
			}
		}
	}
	
	public Drawable getDrawableFromResource(
			Intent.ShortcutIconResource mShortcutIconResource )
	{
		if( mShortcutIconResource != null )
		{
			Drawable drawable = null;
			try
			{
				PackageManager packageManager = LauncherAppState.getInstance().getContext().getPackageManager();
				Resources resources = packageManager.getResourcesForApplication( mShortcutIconResource.packageName );
				if( resources != null )
				{
					final int id = resources.getIdentifier( mShortcutIconResource.resourceName , null , null );
					drawable = getFullResIcon( resources , id , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight );
				}
			}
			catch( Exception e )
			{
				// drop this.  we have other places to look for icons
			}
			return drawable;
		}
		return null;
	}
	
	public Bitmap reloadIcon(
			String mPackageName ,
			String mClassName ,
			Intent.ShortcutIconResource mShortcutIconResource ,
			ResolveInfo mResolveInfo , //cheyingkun add	//换主题不重启（T9搜索部分）【i_0012149】
			Bitmap mBitmapFallback )
	{
		//先看看当前主题的可替换列表中，有没有配置该ComponentName对应的图片
		Bitmap mBitmap = LauncherIconBaseConfig.getDefaultIcon( new ComponentName( mPackageName , mClassName ) );
		if( mBitmap == null )
		{
			Drawable icon = null;
			if( mShortcutIconResource != null )
			{
				icon = getDrawableFromResource( mShortcutIconResource );
			}
			if( mBitmap == null && mResolveInfo != null && icon == null )
			{
				icon = getFullResIcon( mResolveInfo , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight );
			}
			mBitmap = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( icon , LauncherAppState.getInstance().getContext() , false );
			if( mBitmap == null )
			{//通过iconResource没获取到图片，则使用现在的mIcon（保护措施，现在不可能出现该情况）
				mBitmap = mBitmapFallback;
				ThemeManager mThemeManager = ThemeManager.getInstance();
				if( mThemeManager != null )
				{
					mBitmap = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( mBitmap , LauncherAppState.getInstance().getContext() , false );
				}
			}
		}
		return mBitmap;
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
	
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		mTempCache.putAll( mCache );
		flush();
	}
	
	public void onRecycle()
	{

		if( mTempCache != null )
		{
			Set<ComponentName> keys = mTempCache.keySet();
			for( ComponentName componentName : keys )
			{
				CacheEntry entry = mTempCache.get( componentName );
				CacheEntry nowEntry = mCache.get( componentName );
				if( entry != null && entry.icon != null && ( nowEntry == null || entry.icon != nowEntry.icon ) && entry.icon.isRecycled() == false )
				{
					entry.icon.recycle();
				}
			}
			mTempCache.clear();
		}
		

	}
	//zhujieping add end
}
