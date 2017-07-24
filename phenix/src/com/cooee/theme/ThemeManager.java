package com.cooee.theme;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.cooee.center.pub.provider.PubProviderHelper;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.util.Tools;
import com.cooee.wallpaperManager.WallpaperManagerBase;


interface OnThemeChangeListener
{
	
	public void OnThemeChange();
}

public class ThemeManager
//
implements IOnThemeChanged//zhujieping add，换主题不重启
{
	
	private static final String TAG = "ThemeManager";
	public static final String BEAUTY_CENTER_PACKAGE_NAME = "com.iLoong.base.themebox";
	public static final String BEAUTY_CENTER_TAB_THEME_CLASS_NAME = "com.cooee.BeautyCenter.tabTheme";
	public static final String BEAUTY_CENTER_TAB_WALLPAPER_CLASS_NAME = "com.cooee.BeautyCenter.tabWallpaper";
	public static final String BEAUTY_CENTER_TAB_LOCKER_CLASS_NAME = "com.cooee.BeautyCenter.tabLocker";
	public static final String BEAUTY_CENTER_TAB_THEME_TAG_NAME = "tagTheme";
	public static final String BEAUTY_CENTER_TAB_WALLPAPER_TAG_NAME = "tagWallpaper";
	public static final String BEAUTY_CENTER_TAB_LOCKER_TAG_NAME = "tagLock";
	private static ThemeManager mInstance;
	private static Context mContext;
	private ThemeDescription mThemeDescription;
	private boolean mDirty = true;
	//xiatian add start	//需求：完善底边栏图标读取主题中的图片的逻辑（详见ThemeManager.java中的“HOTSEAT_ALL_APPS_BUTTON_ICON_XXX”）。
	//【备注】查找图片的逻辑如下：
	//	1、当前主题为默认主题
	//		1.1、没有本地化配置，使用res中的hotseat_all_apps_button_selector
	//		1.2、有本地化配置
	//			1.2.1、优先读取本地化目录下theme/hotseatbar文件夹中的hotseat_all_apps_button_icon_normal.png和hotseat_all_apps_button_icon_focus.png
	//			1.2.2、在上述步骤中（1.2.1）没找到相应资源，则使用res中的hotseat_all_apps_button_selector
	//	2、当前主题为非默认主题
	//		2.1、优先读取主题apk的assets/theme/hotseatbar文件夹中的hotseat_all_apps_button_icon_normal.png和hotseat_all_apps_button_icon_focus.png
	//		2.2、在上述步骤中（2.1）没有同时找到两张图片的相应资源，则读取assets/theme/hotseatbar/s3app.png
	//		2.3、在上述步骤中（2.2）没有同时找到相应资源，则使用res中的hotseat_all_apps_button_selector
	private Drawable mHotseatAllAppsButtonIcon = null;
	private static final String HOTSEAT_ALL_APPS_BUTTON_ICON_NORMAL_PATH = "theme/hotseatbar/hotseat_all_apps_button_icon_normal.png";
	private static final String HOTSEAT_ALL_APPS_BUTTON_ICON_FOCUS_PATH = "theme/hotseatbar/hotseat_all_apps_button_icon_focus.png";
	private static final String HOTSEAT_ALL_APPS_BUTTON_ICON_OLD_PATH = "theme/hotseatbar/s3app.png";
	//xiatian add end
	;
	
	public ThemeManager(
			Context context )
	{
		mInstance = this;
		mContext = context;
		init();
	}
	
	public static ThemeManager getInstance()
	{
		return mInstance;
	}
	
	/**
	 *  查询手机里的主题列表
	 * 
	 * @param void
	 * @return ArrayList<ResolveInfo>  the list of ResolveInfo
	 */
	private ArrayList<ResolveInfo> getItemsList()
	{
		ArrayList<ResolveInfo> reslist = new ArrayList<ResolveInfo>();
		Intent intent = null;
		intent = new Intent( "com.coco.themes" , null );
		List<ResolveInfo> themesinfo = mContext.getPackageManager().queryIntentActivities( intent , 0 );
		//		Collections.sort( themesinfo , new ResolveInfo.DisplayNameComparator( mContext.getPackageManager() ) );
		// zhujieping@2015/03/12 UPD START
		//桌面是默认主题，也要加入列表
		Intent systemmain = new Intent( "android.intent.action.MAIN" , null );
		systemmain.addCategory( "android.intent.category.LAUNCHER" );
		systemmain.addCategory( "android.intent.category.Theme" );//避免像微入口那样的程序被加入到主题里面
		systemmain.setPackage( mContext.getPackageName() );
		Iterator<ResolveInfo> it = mContext.getPackageManager().queryIntentActivities( systemmain , 0 ).iterator();
		while( it.hasNext() )
		{
			ResolveInfo resinfo = it.next();
			//微入口不应该加入到主题 里面去
			if( !resinfo.activityInfo.processName.equals( "com.iLoong.Second" ) )
			{
				reslist.add( resinfo );
			}
		}
		// zhujieping@2015/03/12 UPD END
		int themescount = themesinfo.size();
		for( int index = 0 ; index < themescount ; index++ )
		{
			ResolveInfo resinfo = themesinfo.get( index );
			reslist.add( resinfo );
		}
		return reslist;
	}
	
	/**
	 *  通过ResolveInfo创建ThemeDescription
	 * 
	 * @param ResolveInfo  the ResolveInfo of cooee theme
	 * @return ThemeDescription  
	 */
	private ThemeDescription getThemeDescription(
			ResolveInfo resinfo )
	{
		Context slaveContext = null;
		try
		{
			if( !resinfo.activityInfo.applicationInfo.packageName.equals( mContext.getPackageName() ) )
			{
				slaveContext = mContext.createPackageContext( resinfo.activityInfo.applicationInfo.packageName , Context.CONTEXT_IGNORE_SECURITY );
			}
			else
			{
				slaveContext = mContext;
			}
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		if( slaveContext == null )
		{
			return null;
		}
		//通过ResolveInfo创建ThemeDescription
		ThemeDescription themeDes = CreateThemeDescription( slaveContext , resinfo );
		return themeDes;
	}
	
	/**
	 *  ThemeManager 初始化当前主题
	 * 
	 * @param void
	 * @return void  
	 */
	private void init()
	{
		ResolveInfo resinfo = null;
		String currentThemePackageName = null;
		ArrayList<ResolveInfo> localResDesList = getItemsList();
		Iterator<ResolveInfo> it = localResDesList.iterator();
		// zhujieping@2015/03/12 UPD START
		////get current Theme,if Preferences has not theme then currentTeme is FeatureConfig.config_default_theme_package_name
		////		SharedPreferences prefs = mContext.getSharedPreferences( "theme" , Activity.MODE_WORLD_WRITEABLE );
		////		currentThemePackageName = prefs.getString( "theme" , mContext.getResources().getString( R.string.config_default_theme_package_name ) );
		////PubProviderHelper.addOrUpdateValue( "theme" , "theme" , currentThemePackageName );
		currentThemePackageName = PubProviderHelper.queryValue( "theme" , "theme" );
		//cheyingkun add start	//解决“设置美化中心主题后，退出桌面卸载主题再进入桌面，因为主题找不到导致桌面异常”的问题。【c_0003192】
		//【问题原因】初始化主题信息时，从数据库得到currentThemePackageName！=null 但是主题被卸载，导致mThemeDescription==null
		//【解决方案】判断当前包名的应用是否安装,如果未安装,则设置默认主题
		boolean apkInstalled = LauncherAppState.isApkInstalled( currentThemePackageName );
		if( !apkInstalled
		//cheyingkun add end
		|| currentThemePackageName == null//
		)
		{
			if( LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME != null )
			{
				boolean found = false;
				while( it.hasNext() )
				{
					resinfo = it.next();
					if( resinfo.activityInfo.applicationInfo.packageName.equals( LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME ) )
					{
						found = true;
						break;
					}
				}
				if( found )
				{
					currentThemePackageName = LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME;
				}
				else
				{
					currentThemePackageName = mContext.getPackageName();
				}
			}
			else
			{
				currentThemePackageName = mContext.getPackageName();
			}
			PubProviderHelper.addOrUpdateValue( "theme" , "theme" , currentThemePackageName );
			//cheyingkun add start	//解决“设置美化中心主题后，退出桌面卸载主题再进入桌面，因为主题找不到导致桌面异常”的问题。【c_0003192】
			if( currentThemePackageName != null && !apkInstalled )
			{
				PubProviderHelper.addOrUpdateValue( "theme" , "theme_status" , "1" );
			}
			//cheyingkun add end
		}
		it = localResDesList.iterator();
		// zhujieping@2015/03/12 UPD END
		while( it.hasNext() )
		{
			resinfo = it.next();
			if( currentThemePackageName != null && resinfo.activityInfo.applicationInfo.packageName.equals( currentThemePackageName ) )
			{
				mThemeDescription = getThemeDescription( resinfo );
				break;
			}
		}
		if( mThemeDescription != null )
		{
			mThemeDescription.mUse = true;
		}
	}
	
	public Context getContext()
	{
		return mThemeDescription.getContext();
	}
	
	public BitmapDrawable getDrawableIgnoreSystemTheme(
			String filename )
	{
		InputStream instr = null;
		try
		{
			if( mThemeDescription != null )
			{
				instr = mThemeDescription.getContext().getAssets().open( filename );
			}
		}
		catch( IOException e )
		{
		}
		if( instr == null )
		{
			return null;
		}
		Bitmap mBitmapSource = Tools.getImageFromInStream( instr );
		//xiatian start	//解决“使用有背板的主题，文件夹图标后面加了背板”的问题。【c_0004637】
		//		Bitmap bmp = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( mBitmapSource , mContext , true );//xiatian del
		Bitmap bmp = Utilities.createIconBitmap( mBitmapSource , mContext , Utilities.sIconWidth , Utilities.sIconHeight , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight , true , false );//xiatian add
		//xiatian end
		return new BitmapDrawable( bmp );
	}
	
	public Drawable getDrawableFromResource(
			String mDrawableName ,
			int mDrawableId )
	{//先从当前主题读Drawable，读不到的话再从默认主题读Drawable
		Drawable ret = mThemeDescription.getDrawableFromResource( mDrawableName );
		if( ret == null )
		{
			ret = mContext.getResources().getDrawable( mDrawableId );
		}
		return ret;
	}
	
	/**
	 *  获取当前主题的图片资源
	 * 
	 * @param filename 图片资源的路径
	 * @return Bitmap   
	 */
	public Bitmap getBitmapIgnoreSystemTheme(
			String filename )
	{
		InputStream instr = null;
		try
		{
			if( mThemeDescription != null )
			{
				instr = mThemeDescription.getContext().getAssets().open( filename );
			}
		}
		catch( IOException e )
		{
		}
		if( instr == null )
		{
			return null;
		}
		return Tools.getImageFromInStream( instr );
	}
	
	/**
	 *  获取当前主题的图片资源,当获取不到则获取系统主题对应路径下的资源
	 * 
	 * @param filename 图片资源的路径
	 * @return Bitmap   
	 */
	public Bitmap getBitmap(
			String filename )
	{
		InputStream instr = null;
		Bitmap bitmap = null;
		try
		{
			if( mThemeDescription != null )
				instr = mThemeDescription.getContext().getAssets().open( filename );
		}
		catch( IOException e )
		{
		}
		if( instr != null )
		{
			try
			{
				bitmap = Tools.getImageFromInStream( instr );
			}
			finally
			{
				if( instr != null )
				{
					try
					{
						instr.close();
					}
					catch( IOException e )
					{
						// TODO Auto-generated catch block
					}
				}
			}
		}
		return bitmap;
	}
	
	/**
	 *  判断主题下的对应文件是否存在
	 * 
	 * @param filename 文件路径
	 * @return boolean  true 存在 false 不存在
	 */
	public boolean isFileExistIgnoreSystem(
			String fileName )
	{
		InputStream instr = null;
		try
		{
			instr = mThemeDescription.getContext().getAssets().open( fileName );
		}
		catch( IOException e )
		{
		}
		if( instr != null )
		{
			try
			{
				instr.close();
				return true;
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public int getInt(
			String key ,
			int defValue )
	{
		Integer result;
		result = mThemeDescription.getInteger( key );
		if( result == null )
		{
			return defValue;
		}
		return result.intValue();
	}
	
	public String getString(
			String key ,
			String defValue )
	{
		String result = null;
		result = mThemeDescription.getString( key );
		if( result == null )
		{
			return defValue;
		}
		return result;
	}
	
	public boolean getBoolean(
			String key ,
			boolean defValue )
	{
		Boolean result = false;
		result = mThemeDescription.getBoolean( key );
		if( result == null )
		{
			return defValue;
		}
		return result.booleanValue();
	}
	
	//<i_0008508> liuhailin@2014-12-17 modify begin
	public float getFloat(
			String key ,
			float defValue )
	{
		Float result;
		result = mThemeDescription.getFloat( key );
		if( result == null )
		{
			return defValue;
		}
		return result.floatValue();
	}
	
	//<i_0008508> liuhailin@2014-12-17 modify end
	/**
	 *  鑾峰彇涓婚鐨勭浉鍏抽厤缃枃浠剁殑鏂囦欢鍐呭
	 * 
	 * @param filename 鏂囦欢鍚�
	 * @return InputStream 鏂囦欢娴�
	 */
	public InputStream getFile(
			String filename )
	{
		InputStream instr = null;
		try
		{
			instr = mThemeDescription.getContext().getAssets().open( filename );
		}
		catch( IOException e )
		{
		}
		return instr;
	}
	
	/**
	 *  根据ResolveInfo创建ThemeDescription对象
	 * 
	 * @param context 上下文
	 * @param resinfo the object instance of ResolveInfo
	 * @return ThemeDescription 当前主题的ThemeDescription
	 */
	private ThemeDescription CreateThemeDescription(
			Context context ,
			ResolveInfo resinfo )
	{
		//xiatian start	//解决“非默认主题，也读取本地化配置”的问题。
		//		ThemeDescription themeDescription = new ThemeDescription( context );//xiatian del
		ThemeDescription themeDescription = new ThemeDescription( context , mContext.getPackageName().equals( resinfo.activityInfo.applicationInfo.packageName ) );//xiatian add
		//xiatian end
		themeDescription.componentName = new ComponentName( resinfo.activityInfo.applicationInfo.packageName , resinfo.activityInfo.name );
		String defaultTheme = mContext.getPackageName();
		if( LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME != null )
			defaultTheme = LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME;
		if( resinfo.activityInfo.applicationInfo.packageName.equals( defaultTheme ) )
			themeDescription.title = "默认主题";
		else
			themeDescription.title = resinfo.loadLabel( mContext.getPackageManager() );
		themeDescription.mBuiltIn = ( resinfo.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0;
		//xiatian del start	//解决“非默认主题，也读取本地化配置”的问题。
		//		// zhujieping@2015/03/26 UPD START
		//		themeDescription.mSystem = mContext.getPackageName().equals( resinfo.activityInfo.applicationInfo.packageName );
		//		// zhujieping@2015/03/26 UPD END
		//xiatian del end
		return themeDescription;
	}
	
	public ThemeDescription getCurrentThemeDescription()
	{
		return mThemeDescription;
	}
	
	/**
	 *  应用壁纸的接口
	 *  
	 * @param void
	 * @return void  
	 */
	@SuppressLint( "ServiceCast" )
	public void ApplyWallpaper()
	{
		if( mThemeDescription != null )
		{
			String theme_status = PubProviderHelper.queryValue( "theme" , "theme_status" );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "theme_status === " , theme_status ) );
			try
			{
				if( Integer.parseInt( theme_status ) == 0 )
				{
					// gaominghui@2017/01/09 ADD START 更換WallpaperManagerBase.jar包后改段代码不需要调用
					/*//lvjiangbin begin   phenix桌面，壁纸设置为单屏，480X854的单屏壁纸在插T卡前是正常显示，插入T卡后，重新开机壁纸被拉伸（必现），现象如图所示，[c_4433]
					if( LauncherDefaultConfig.SWITCH_ENABLE_MTK_SET_WALLPAPER )
					{
						WallpaperManagerBase.getInstance( mContext ).setWallpaperDimensionSingleScreenMode();
					}
					////lvjiangbin end   phenix桌面，壁纸设置为单屏，480X854的单屏壁纸在插T卡前是正常显示，插入T卡后，重新开机壁纸被拉伸（必现），现象如图所示，[c_4433]
					*/
					// gaominghui@2017/01/09 ADD END 更換WallpaperManagerBase.jar包后改段代码不需要调用
					return;
				}
			}
			catch( Exception e )
			{
				// TODO: handle exception
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , e.getMessage() );
			}
			WallpaperManager wpm = (WallpaperManager)mContext.getSystemService( Context.WALLPAPER_SERVICE );
			// zhujieping@2015/03/26 UPDATE START
			// 原先是通过id生成bitmap，然后设置壁纸，现直接用id设置壁纸，提高速度
			//			Bitmap bmp = mThemeDescription.getBitmapFromResource( "default_wallpaper" );
			//			if( bmp != null )
			//			{
			//				try
			//				{
			//					wpm.setBitmap( bmp );
			//				}
			//				catch( IOException e )
			//				{
			//					e.printStackTrace();
			//				}
			//				if( !bmp.isRecycled() )
			//				{
			//					bmp.recycle();
			//					bmp = null;
			//				}
			//			}
			if( currentThemeIsSystemTheme() )
			{
				//cheyingkun add start	//默认壁纸本地化。【c_0003753】
				//如果自定义默认壁纸
				if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME ) )
				{
					//获取用户自定义目录下的壁纸
					File file = new File( LauncherDefaultConfig.CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME );
					InputStream is = null;
					InputStream newIs = null;
					if( file.exists() )
					{
						try
						{
							is = new FileInputStream( LauncherDefaultConfig.CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME );
							newIs = new FileInputStream( LauncherDefaultConfig.CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME );
						}
						catch( FileNotFoundException e )
						{
							e.printStackTrace();
						}
					}
					else
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( "" , StringUtils.concat( "cyk !file.exists() : " , LauncherDefaultConfig.CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME ) );
					}
					if( is == null || newIs == null )
					{
						return;
					}
					WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimension( is , newIs );
				}
				else
				//cheyingkun add end
				{
					boolean result = false;
					if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )
					{
						//获取用户自定义目录下的壁纸
						File file = new File( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/wallpaper/default.jpg" ) );
						InputStream is = null;
						InputStream newIs = null;
						if( file.exists() )
						{
							try
							{
								is = new FileInputStream( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/wallpaper/default.jpg" ) );
								newIs = new FileInputStream( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/wallpaper/default.jpg" ) );
							}
							catch( FileNotFoundException e )
							{
								e.printStackTrace();
							}
						}
						else
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "" , StringUtils.concat( "cyk !file.exists() : " , LauncherDefaultConfig.CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME ) );
						}
						if( is != null && newIs != null )
						{
							result = true;
							WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimension( is , newIs );
						}
					}
					if( !result )
					{
						int id = -1;
						//xiatian start	//需求：默认主题壁纸外置（使用包名为“config_custom_default_wallpaper_package_name”的res/drawable中的资源“config_custom_default_wallpaper_resource_name”），若“config_custom_default_wallpaper_package_name”和“config_custom_default_wallpaper_resource_name”其中有一个配置为空，则使用桌面默认主题的壁纸。
						//				id = mThemeDescription.getResourceID( "default_wallpaper" );//xiatian del
						//xiatian add start
						Resources mResources = null;
						// gaominghui@2017/01/09 UPD START  WallpaperManagerBase这个类删掉了，统一调用WallpaperManagerBase.jar里面的方法
						//需求：默认主题壁纸外置（使用包名为“config_custom_default_wallpaper_package_name”的res/drawable中的资源“config_custom_default_wallpaper_resource_name”），若“config_custom_default_wallpaper_package_name”和“config_custom_default_wallpaper_resource_name”其中有一个配置为空，则使用桌面默认主题的壁纸。
						//String custom_default_wallpaper_package_name = WallpaperManagerBase.CONFIG_CUSTOM_DEFAULT_WALLPAPER_PACKAGE_NAME;
						//String custom_default_wallpaper_resource_name = WallpaperManagerBase.CONFIG_CUSTOM_DEFAULT_WALLPAPER_RESOURCE_NAME;
						String custom_default_wallpaper_package_name = LauncherDefaultConfig.getString( R.string.config_custom_default_wallpaper_package_name );
						String custom_default_wallpaper_resource_name = LauncherDefaultConfig.getString( R.string.config_custom_default_wallpaper_resource_name );
						// gaominghui@2017/01/09 UPD END WallpaperManagerBase这个类删掉了，统一调用WallpaperManagerBase.jar里面的方法
						if( !( TextUtils.isEmpty( custom_default_wallpaper_package_name ) || ( TextUtils.isEmpty( custom_default_wallpaper_resource_name ) ) ) )
						{
							Context slaveContext = null;
							try
							{
								slaveContext = mContext.createPackageContext( custom_default_wallpaper_package_name , Context.CONTEXT_IGNORE_SECURITY );
							}
							catch( NameNotFoundException e )
							{
								e.printStackTrace();
							}
							if( slaveContext != null )
							{
								mResources = slaveContext.getResources();
								id = ResourceUtils.getDrawableResourceIdByReflectIfNecessary( 0 , mResources , custom_default_wallpaper_package_name , custom_default_wallpaper_resource_name );
							}
						}
						else
						{
							id = mThemeDescription.getResourceID( "default_wallpaper" );
							mResources = mContext.getResources();
						}
						//xiatian add end
						//xiatian end
						if( id > 0 )
						//				try
						//					{
						//						wpm.setResource( id );
						//					}
						//					catch( IOException e )
						//					{
						//						e.printStackTrace();
						//					}
						{
							//xiatian start	//需求：默认主题壁纸外置（使用包名为“config_custom_default_wallpaper_package_name”的res/drawable中的资源“config_custom_default_wallpaper_resource_name”），若“config_custom_default_wallpaper_package_name”和“config_custom_default_wallpaper_resource_name”其中有一个配置为空，则使用桌面默认主题的壁纸。
							//xiatian del start
							//					InputStream is = mContext.getResources().openRawResource( id );
							//					InputStream newIs = mContext.getResources().openRawResource( id );
							//xiatian del end
							//xiatian add start
							if( mResources == null )
							{
								return;
							}
							InputStream is = mResources.openRawResource( id );
							InputStream newIs = mResources.openRawResource( id );
							if( is == null || newIs == null )
							{
								return;
							}
							//xiatian add end
							//xiatian end
							WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimension( is , newIs );
						}
					}
				}
			}
			// zhujieping@2015/03/26 UPDATE END
			else
			{
				//				InputStream is = null;
				//				String pathName = null;
				//				{
				//					pathName = "wallpaper/default.jpg";
				//					is = mThemeDescription.getStream( pathName );
				//				}
				//				if( is != null )
				//					try
				//					{
				//						wpm.setStream( ( is ) );
				//						is.close();
				//					}
				//					catch( IOException e )
				//					{
				//						e.printStackTrace();
				//					}
				String pathName = "wallpaper/default.jpg";
				InputStream is = mThemeDescription.getStream( pathName );
				InputStream newIs = mThemeDescription.getStream( pathName );
				WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimension( is , newIs );
			}
			//zhujieping add start,换完壁纸后调用一下该方法，防止模糊壁纸时出现异常
			wpm.forgetLoadedWallpaper();
			// zhujieping add end
			//将耗时的操作放置线程中
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( LauncherAppState.getInstance().getContext() );
					if( currentThemeIsSystemTheme() )
					{
						pref.edit().putString( "currentWallpaper" , "default" ).commit();
						PubProviderHelper.addOrUpdateValue( "wallpaper" , "currentWallpaper" , "default" );
					}
					else
					{
						pref.edit().putString( "currentWallpaper" , "other" ).commit();
						PubProviderHelper.addOrUpdateValue( "wallpaper" , "currentWallpaper" , "other" );
					}
					pref.edit().putBoolean( "userDefinedWallpaper" , false ).commit();
					PubProviderHelper.addOrUpdateValue( "wallpaper" , "userDefinedWallpaper" , "false" );
					pref.edit().putBoolean( "cooeechange" , true ).commit();
					PubProviderHelper.addOrUpdateValue( "wallpaper" , "cooeechange" , "true" );
					PubProviderHelper.addOrUpdateValue( "theme" , "theme_status" , "0" );
				}
			} ).start();
		}
	}
	
	public boolean getDataIsDirty()
	{
		return mDirty;
	}
	
	public void Reset()
	{
		mDirty = false;
	}
	
	/**
	 *  卸载当前主题时候,调用此方法恢复默认主题
	 *  
	 * @param Packname 卸载的主题包名
	 * @return void  
	 */
	public void RemovePackage(
			String packageName )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "RemovePackage" );
		if( mThemeDescription != null && mThemeDescription.componentName.getPackageName().equals( packageName ) )
		{
			// zhujieping@2015/03/12 ADD START
			String defaultTheme = null;
			//卸载的是当前正在使用的默认主题或者没有配置默认主题，此时主题变为launcher自带的主题，否则变为配置的默认主题
			if( packageName.equals( LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME ) || LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME == null )
			{
				defaultTheme = mContext.getPackageName();
			}
			else
			{
				defaultTheme = LauncherDefaultConfig.CONFIG_DEFAULT_THEME_PACKAGE_NAME;
			}
			// zhujieping@2015/03/12 ADD END
			Intent intent = new Intent( ThemeReceiver.ACTION_LAUNCHER_APPLY_THEME );
			intent.putExtra( "theme_status" , 1 );
			intent.putExtra( "theme" , defaultTheme );
			mContext.sendBroadcast( intent );
		}
	}
	
	public boolean currentThemeIsSystemTheme()
	{
		if( mThemeDescription == null )
		{
			return true;
		}
		if( mThemeDescription.mSystem )
			return true;
		else
		{
			return false;
		}
	}
	
	public String getCurrentThemeFileDir(
			String dirName ,
			boolean autoAdapt )
	{
		return getAssetFileDir( mThemeDescription.getContext() , dirName , autoAdapt );
	}
	
	public String getAssetFileDir(
			Context context ,
			String dirName ,
			boolean autoAdapt )
	{
		String defaultPrefix = "";
		String filePrefix = null;
		if( dirName.contains( "/" ) )
		{
			filePrefix = dirName.substring( 0 , dirName.indexOf( "/" ) );
			dirName = dirName.substring( dirName.indexOf( "/" ) + 1 );
			if( filePrefix.contains( "-" ) )
			{
				defaultPrefix = filePrefix.substring( 0 , filePrefix.indexOf( "-" ) );
			}
			else
			{
				defaultPrefix = filePrefix;
			}
		}
		else
		{
			filePrefix = "";
		}
		boolean find = false;
		if( autoAdapt )
		{
			if( filePrefix != null && filePrefix.length() > 0 )
			{
				if( !( filePrefix.contains( "-" ) ) )
				{
					filePrefix = this.getSpecificThemeDir( context , filePrefix );
				}
			}
			if( filePrefix.equals( this.getSpecificThemeDir( context , defaultPrefix ) ) )
			{
				find = checkDirExist( context , filePrefix , dirName );
				if( !find )
				{
					filePrefix = defaultPrefix;
				}
			}
		}
		if( !find )
		{
			find = checkDirExist( context , filePrefix , dirName );
		}
		if( find )
		{
			if( filePrefix != null && filePrefix.length() > 0 )
			{
				return StringUtils.concat( filePrefix , File.separator , dirName );
			}
			else
			{
				return dirName;
			}
		}
		else
		{
			return null;
		}
	}
	
	public Context getCurrentThemeContext()
	{
		return mThemeDescription.getContext();
	}
	
	public String getSpecificThemeDir(
			Context context ,
			String prefix )
	{
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return StringUtils.concat( prefix , "-" , metrics.heightPixels , "x" , metrics.widthPixels );
	}
	
	public boolean checkDirExist(
			Context widgetContext ,
			String filePrefix ,
			String dirName )
	{
		boolean find = false;
		try
		{
			if( dirName.startsWith( "/" ) )
			{
				dirName = dirName.substring( 1 );
			}
			if( dirName.endsWith( "/" ) )
			{
				dirName = dirName.substring( 0 , dirName.lastIndexOf( "/" ) );
			}
			while( dirName.contains( "/" ) )
			{
				filePrefix = StringUtils.concat( filePrefix , "/" , dirName.substring( 0 , dirName.indexOf( "/" ) ) );
				dirName = dirName.substring( dirName.indexOf( "/" ) + 1 );
			}
			String[] themeArray = widgetContext.getAssets().list( filePrefix );
			for( String tmpTheme : themeArray )
			{
				if( tmpTheme.equals( dirName ) )
				{
					find = true;
					break;
				}
			}
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return find;
	}
	
	public Bitmap getBitmapOrderIgnoreSystemTheme(
			String filename )
	{
		InputStream instr = null;
		Bitmap bitmap = null;
		try
		{
			instr = mThemeDescription.getContext().getAssets().open( filename );
		}
		catch( IOException e )
		{
		}
		if( instr == null && currentThemeIsSystemTheme() )
		{
			try
			{
				instr = mThemeDescription.getContext().getAssets().open( filename );
			}
			catch( IOException e )
			{
			}
		}
		if( instr != null )
		{
			try
			{
				bitmap = Tools.getImageFromInStream( instr );
			}
			finally
			{
				if( instr != null )
				{
					try
					{
						instr.close();
					}
					catch( IOException e )
					{
					}
				}
			}
		}
		return bitmap;
	}
	
	//icon bg mask cover , change by shlt@2014/11/11 ADD START
	private List<Bitmap> iconBgs = null;
	private List<Bitmap> mask = null;
	private List<Bitmap> cover = null;
	
	private int randomIndex(
			int lenght )
	{
		if( lenght == 0 )
			return -1;
		else if( lenght == 1 )
			return 0;
		else
			return (int)( Math.random() * lenght );
	}
	
	// zhangjin@2015/09/02 ADD START
	public Bitmap getIconBg()
	{
		return getIconBg( true );
	}
	
	// zhangjin@2015/09/02 ADD END
	public Bitmap getIconBg(
			boolean isRandom )
	{
		// zhangjin@2016/06/17 UPD START
		//if( iconBgs == null || iconBgs.size() == 0 )
		if( iconBgs == null )
		// zhangjin@2016/06/17 UPD END
		{
			iconBgs = new LinkedList<Bitmap>();
			for( int i = 0 ; ; i++ )
			{
				// zhujieping@2015/03/13 ADD START
				//				Bitmap bmp = getBitmap( "theme/iconbg/icon_" + i + ".png" );
				Bitmap bmp = null;
				if( currentThemeIsSystemTheme() && !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )//系统主题，配置了主题本地化，读取本地化里面的图片
				{
					bmp = getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/iconbg/icon_" , i , ".png" ) );
				}
				if( bmp == null )
					bmp = mThemeDescription.getBitmapFromResource( StringUtils.concat( "theme_third_party_bg_" , i ) );
				if( bmp == null )
				{
					bmp = getBitmap( StringUtils.concat( "theme/iconbg/icon_" , i , ".png" ) );
				}
				// zhujieping@2015/03/13 ADD END
				if( bmp == null )
				{
					break;
				}
				else
				{
					Bitmap bmp2 = Utilities.createIconBitmap( bmp , mContext , Utilities.sIconWidth , Utilities.sIconHeight , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight , true , true );
					iconBgs.add( bmp2 );
				}
			}
		}
		// zhangjin@2015/09/02 ADD START
		//int index = randomIndex( iconBgs.size() );
		int index = -1;
		if( iconBgs.size() == 0 )
		{
			index = -1;
		}
		else
		{
			if( isRandom )
			{
				index = randomIndex( iconBgs.size() );
			}
			else
			{
				index = 0;
			}
		}
		// zhangjin@2015/09/02 ADD END
		return index == -1 ? null : iconBgs.get( index );
	}
	
	// zhangjin@2015/09/02 ADD START
	public Bitmap getIconMask()
	{
		return getIconMask( true );
	}
	
	// zhangjin@2015/09/02 ADD END
	public Bitmap getIconMask(
			boolean isRandom )
	{
		// zhangjin@2016/06/17 UPD START
		//if( mask == null || mask.size() == 0 )
		if( mask == null )
		// zhangjin@2016/06/17 UPD END
		{
			mask = new LinkedList<Bitmap>();
			//
			for( int i = 0 ; ; i++ )
			{
				// zhujieping@2015/03/13 ADD START
				//				Bitmap bmp = getBitmap( "theme/iconbg/mask_" + i + ".png" );
				Bitmap bmp = null;
				if( currentThemeIsSystemTheme() && !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )//系统主题，配置了主题本地化，读取本地化里面的图片
				{
					bmp = getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/iconbg/mask_" , i , ".png" ) );
				}
				if( bmp == null )
					bmp = mThemeDescription.getBitmapFromResource( StringUtils.concat( "theme_third_party_mask_" , i ) );
				if( bmp == null )
				{
					bmp = getBitmap( StringUtils.concat( "theme/iconbg/mask_" , i , ".png" ) );
				}
				// zhujieping@2015/03/13 ADD END
				if( bmp == null )
				{
					break;
				}
				else
				{
					Bitmap bmp2 = Utilities.createIconBitmap( bmp , mContext , Utilities.sIconWidth , Utilities.sIconHeight , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight , true , true );
					mask.add( bmp2 );
				}
			}
			if( mask.size() == 0 )
			{
				// zhujieping@2015/03/13 ADD START
				//				Bitmap bmp = getBitmap( "theme/iconbg/mask.png" );
				Bitmap bmp = null;
				bmp = mThemeDescription.getBitmapFromResource( "theme_third_party_mask.png" );
				if( bmp == null )
				{
					bmp = getBitmap( "theme/iconbg/mask.png" );
				}
				// zhujieping@2015/03/13 ADD END
				if( bmp != null )
				{
					Bitmap bmp2 = Utilities.createIconBitmap( bmp , mContext , Utilities.sIconWidth , Utilities.sIconHeight , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight , true , true );
					mask.add( bmp2 );
				}
			}
		}
		// zhangjin@2015/09/02 UPD START
		//int index = randomIndex( mask.size() );
		int index = -1;
		if( mask.size() == 0 )
		{
			index = -1;
		}
		else
		{
			if( isRandom )
			{
				index = randomIndex( mask.size() );
			}
			else
			{
				index = 0;
			}
		}
		// zhangjin@2015/09/02 ADD END
		return index == -1 ? null : mask.get( index );
	}
	
	// zhangjin@2015/09/02 ADD START
	public Bitmap getIconCover()
	{
		return getIconCover( true );
	}
	
	// zhangjin@2015/09/02 ADD END
	public Bitmap getIconCover(
			boolean isRandom )
	{
		// zhangjin@2016/06/17 UPD START
		//if( cover == null || cover.size() == 0 )
		if( cover == null )
		// zhangjin@2016/06/17 UPD END
		{
			cover = new LinkedList<Bitmap>();
			//
			for( int i = 0 ; ; i++ )
			{
				// zhujieping@2015/03/13 ADD START
				//				Bitmap bmp = getBitmap( "theme/iconbg/icon_cover_plate_" + i + ".png" );
				Bitmap bmp = null;
				if( currentThemeIsSystemTheme() && !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )//系统主题，配置了主题本地化，读取本地化里面的图片
				{
					bmp = getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/iconbg/icon_cover_plate_" , i , ".png" ) );
				}
				if( bmp == null )
					bmp = mThemeDescription.getBitmapFromResource( StringUtils.concat( "theme_third_party_cover_" , i ) );
				if( bmp == null )
				{
					bmp = getBitmap( StringUtils.concat( "theme/iconbg/icon_cover_plate_" , i , ".png" ) );
				}
				// zhujieping@2015/03/13 ADD END
				if( bmp == null )
				{
					break;
				}
				else
				{
					Bitmap bmp2 = Utilities.createIconBitmap( bmp , mContext , Utilities.sIconWidth , Utilities.sIconHeight , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight , true , true );
					cover.add( bmp2 );
				}
			}
			if( cover.size() == 0 )
			{
				// zhujieping@2015/03/13 ADD START
				Bitmap bmp = null;
				bmp = mThemeDescription.getBitmapFromResource( "theme_third_party_cover.png" );
				if( bmp == null )
				{
					bmp = getBitmap( "theme/iconbg/icon_cover_plate.png" );
				}
				// zhujieping@2015/03/13 ADD END
				if( bmp != null )
				{
					Bitmap bmp2 = Utilities.createIconBitmap( bmp , mContext , Utilities.sIconWidth , Utilities.sIconHeight , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight , true , true );
					cover.add( bmp2 );
				}
			}
		}
		// zhangjin@2015/09/02 UPD START
		//int index = randomIndex( cover.size() );
		int index = -1;
		if( cover.size() == 0 )
		{
			index = -1;
		}
		else
		{
			if( isRandom )
			{
				index = randomIndex( cover.size() );
			}
			else
			{
				index = 0;
			}
		}
		// zhangjin@2015/09/02 UPD END
		return index == -1 ? null : cover.get( index );
	}
	//icon bg mask cover , change by shlt@2014/11/11 ADD END
	;
	
	//xiatian add start	//需求：适配“新主题”，兼容“老主题”。
	public Drawable getFolderIconBg()
	{
		Drawable drawable = null;
		if( mThemeDescription != null && mThemeDescription.mSystem == true )
		{
			if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )
			{
				try
				{
					InputStream instr = new FileInputStream( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , File.separator , LauncherDefaultConfig.THEME_FOLDER_ICON_DIR ) );
					drawable = new BitmapDrawable( LauncherAppState.getInstance().getContext().getResources() , instr );
				}
				catch( FileNotFoundException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if( drawable == null )
			{
				drawable = mContext.getResources().getDrawable( R.drawable.theme_default_folder_icon_bg );
				//xiatian start	//解决“使用有背板的主题，文件夹图标后面加了背板”的问题。【c_0004637】
				//				Bitmap mBitmapReSize = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( drawable , mContext , true );//xiatian del
				Bitmap mBitmapReSize = Utilities.createIconBitmap(
						drawable ,
						mContext ,
						Utilities.sIconWidth ,
						Utilities.sIconHeight ,
						Utilities.sIconTextureWidth ,
						Utilities.sIconTextureHeight ,
						true );//xiatian add
				//xiatian end
				drawable = new BitmapDrawable( mBitmapReSize );
			}
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				Utilities.resizeIconDrawable( drawable );
			}
			//xiatian add end
			return drawable;
		}
		drawable = getDrawableIgnoreSystemTheme( LauncherDefaultConfig.THEME_FOLDER_ICON_DIR );
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			Utilities.resizeIconDrawable( drawable );
		}
		//xiatian add end
		return drawable;
	}
	//xiatian add end
	;
	
	//zhujieping add start
	public static Bitmap getBitmapFromLocal(
			String fileName )
	{
		InputStream instr = null;
		Bitmap bitmap = null;
		try
		{
			instr = new FileInputStream( fileName );
		}
		catch( IOException e )
		{
		}
		if( instr != null )
		{
			try
			{
				bitmap = Tools.getImageFromInStream( instr );
			}
			finally
			{
				if( instr != null )
				{
					try
					{
						instr.close();
					}
					catch( IOException e )
					{
						// TODO Auto-generated catch block
					}
				}
			}
		}
		return bitmap;
	}
	//zhujieping add end
	;
	
	//xiatian add start	//需求：完善底边栏图标读取主题中的图片的逻辑（详见ThemeManager.java中的“HOTSEAT_ALL_APPS_BUTTON_ICON_XXX”）。
	public Drawable getHotseatAllAppsButtonIcon()
	{
		if( mHotseatAllAppsButtonIcon == null )
		{
			Resources mResources = mContext.getResources();
			Bitmap mIconNormalBitmap = null;
			Bitmap mIconFocusBitmap = null;
			if( currentThemeIsSystemTheme() )
			{//默认主题
				if( TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )
				{//没有本地化配置，使用res中的hotseat_all_apps_button_selector
					mHotseatAllAppsButtonIcon = mResources.getDrawable( R.drawable.hotseat_all_apps_button_selector );
				}
				else
				{//有本地化配置
					//读取本地化目录下theme/hotseatbar文件夹中的hotseat_all_apps_button_icon_normal.png和hotseat_all_apps_button_icon_focus.png
					mIconNormalBitmap = getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , File.separator , HOTSEAT_ALL_APPS_BUTTON_ICON_NORMAL_PATH ) );
					mIconFocusBitmap = getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , File.separator , HOTSEAT_ALL_APPS_BUTTON_ICON_FOCUS_PATH ) );
					if( mIconNormalBitmap != null && mIconFocusBitmap != null )
					{
						mIconNormalBitmap = Utilities.createIconBitmap(
								mIconNormalBitmap ,
								mContext ,
								Utilities.sIconWidth ,
								Utilities.sIconHeight ,
								Utilities.sIconTextureWidth ,
								Utilities.sIconTextureHeight ,
								true ,
								true );
						mIconFocusBitmap = Utilities.createIconBitmap(
								mIconFocusBitmap ,
								mContext ,
								Utilities.sIconWidth ,
								Utilities.sIconHeight ,
								Utilities.sIconTextureWidth ,
								Utilities.sIconTextureHeight ,
								true ,
								true );
						StateListDrawable mIconSelector = new StateListDrawable();
						BitmapDrawable mIconNormalDrawable = new BitmapDrawable( mIconNormalBitmap );
						BitmapDrawable mIconFocusDrawable = new BitmapDrawable( mIconFocusBitmap );
						mIconSelector.addState( new int[]{ android.R.attr.state_pressed , android.R.attr.state_enabled }/* View.PRESSED_ENABLED_STATE_SET */, mIconFocusDrawable );
						mIconSelector.addState( new int[]{ android.R.attr.state_enabled }/* View.ENABLED_STATE_SET */, mIconNormalDrawable );
						mIconSelector.addState( new int[]{}/* View.EMPTY_STATE_SET */, mIconNormalDrawable );
						mHotseatAllAppsButtonIcon = mIconSelector;
					}
					if( mHotseatAllAppsButtonIcon == null )
					{//本地化配置中，没找到相应资源，则使用res中的hotseat_all_apps_button_selector
						mHotseatAllAppsButtonIcon = mResources.getDrawable( R.drawable.hotseat_all_apps_button_selector );
					}
				}
			}
			else
			{//非默认主题
				//读取主题apk的assets/theme/hotseatbar文件夹中的hotseat_all_apps_button_icon_normal.png和hotseat_all_apps_button_icon_focus.png
				mIconNormalBitmap = getBitmap( HOTSEAT_ALL_APPS_BUTTON_ICON_NORMAL_PATH );
				mIconFocusBitmap = getBitmap( HOTSEAT_ALL_APPS_BUTTON_ICON_FOCUS_PATH );
				if( mIconNormalBitmap != null && mIconFocusBitmap != null )
				{
					mIconNormalBitmap = Utilities.createIconBitmap(
							mIconNormalBitmap ,
							mContext ,
							Utilities.sIconWidth ,
							Utilities.sIconHeight ,
							Utilities.sIconTextureWidth ,
							Utilities.sIconTextureHeight ,
							true ,
							true );
					mIconFocusBitmap = Utilities.createIconBitmap(
							mIconFocusBitmap ,
							mContext ,
							Utilities.sIconWidth ,
							Utilities.sIconHeight ,
							Utilities.sIconTextureWidth ,
							Utilities.sIconTextureHeight ,
							true ,
							true );
					StateListDrawable mIconSelector = new StateListDrawable();
					BitmapDrawable mIconNormalDrawable = new BitmapDrawable( mIconNormalBitmap );
					BitmapDrawable mIconFocusDrawable = new BitmapDrawable( mIconFocusBitmap );
					mIconSelector.addState( new int[]{ android.R.attr.state_pressed , android.R.attr.state_enabled }/* View.PRESSED_ENABLED_STATE_SET */, mIconFocusDrawable );
					mIconSelector.addState( new int[]{ android.R.attr.state_enabled }/* View.ENABLED_STATE_SET */, mIconNormalDrawable );
					mIconSelector.addState( new int[]{}/* View.EMPTY_STATE_SET */, mIconNormalDrawable );
					mHotseatAllAppsButtonIcon = mIconSelector;
				}
				if( mHotseatAllAppsButtonIcon == null )
				{//主题apk的assets/theme/hotseatbar文件夹中没找到hotseat_all_apps_button_icon_normal.png或者hotseat_all_apps_button_icon_focus.png的时候；读取assets/theme/hotseatbar/s3app.png
					Bitmap mIconBitmap = getBitmap( HOTSEAT_ALL_APPS_BUTTON_ICON_OLD_PATH );
					if( mIconBitmap != null )
					{
						mIconBitmap = Utilities.createIconBitmap(
								mIconBitmap ,
								mContext ,
								Utilities.sIconWidth ,
								Utilities.sIconHeight ,
								Utilities.sIconTextureWidth ,
								Utilities.sIconTextureHeight ,
								true ,
								true );
						mHotseatAllAppsButtonIcon = new BitmapDrawable( mIconBitmap );
					}
				}
				if( mHotseatAllAppsButtonIcon == null )
				{//主题apk的assets/theme/hotseatbar中没有hotseat_all_apps_button_icon_normal.png和hotseat_all_apps_button_icon_focus.png，以及s3app.png，则使用res中的hotseat_all_apps_button_selector
					mHotseatAllAppsButtonIcon = mResources.getDrawable( R.drawable.hotseat_all_apps_button_selector );
				}
			}
		}
		return mHotseatAllAppsButtonIcon;
	}
	
	//xiatian add end
	//zhujieping add start
	public void applyWallpaperInThread()
	{
		new Thread( new Runnable() {
			
			public void run()
			{
				ApplyWallpaper();
			}
		} ).start();
	}
	
	public boolean need2ChangeTheme(
			Intent intent )
	{
		String m2ApplyThemePackageName = intent.getStringExtra( "theme" );
		if( TextUtils.isEmpty( m2ApplyThemePackageName ) )
		{
			return false;
		}
		if( !LauncherDefaultConfig.getBoolean( R.bool.switch_enable_apply_theme_when_new_theme_is_current_theme ) )//zhujieping add //添加配置项“switch_enable_apply_theme_when_new_theme_is_current_theme”，当要换的新主题就是当前主题时，是否进行更换，true为更换，false不更换，默认为false
			if( mThemeDescription.componentName.getPackageName().equals( m2ApplyThemePackageName ) )
			{
				return false;
			}
		return true;
	}
	
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		//新主题相关数据写入数据库
		Intent mIntent = (Intent)arg0;
		PubProviderHelper.addOrUpdateValue( "theme" , "theme" , mIntent.getStringExtra( "theme" ) );//主题包名
		PubProviderHelper.addOrUpdateValue( "theme" , "theme_status" , String.valueOf( mIntent.getIntExtra( "theme_status" , 1 ) ) );//是否换壁纸
		//mThemeDescription
		init();
		//bg
		if( iconBgs != null )
		{
			if( iconBgs.size() > 0 )
			{
				int count = iconBgs.size();
				for( int i = 0 ; i < count ; i++ )
				{
					Bitmap mBitmap = iconBgs.get( i );
					if( mBitmap != null && mBitmap.isRecycled() == false )
					{
						mBitmap.recycle();
					}
				}
				iconBgs.clear();
			}
			// zhangjin@2016/03/16 ADD START
			iconBgs = null;
			// zhangjin@2016/03/16 ADD END
		}
		//mask
		if( mask != null )
		{
			if( mask.size() > 0 )
			{
				int count = mask.size();
				for( int i = 0 ; i < count ; i++ )
				{
					Bitmap mBitmap = mask.get( i );
					if( mBitmap != null && mBitmap.isRecycled() == false )
					{
						mBitmap.recycle();
					}
				}
				mask.clear();
			}
			// zhangjin@2016/03/16 ADD START
			mask = null;
			// zhangjin@2016/03/16 ADD END
		}
		//cover
		if( cover != null )
		{
			if( cover.size() > 0 )
			{
				int count = cover.size();
				for( int i = 0 ; i < count ; i++ )
				{
					Bitmap mBitmap = cover.get( i );
					if( mBitmap != null && mBitmap.isRecycled() == false )
					{
						mBitmap.recycle();
					}
				}
				cover.clear();
			}
			// zhangjin@2016/03/16 ADD START
			cover = null;
			// zhangjin@2016/03/16 ADD END
		}
		//mHotseatAllAppsButtonIcon
		if( mHotseatAllAppsButtonIcon != null )//bitmapdrawable中的bitmap不能释放，res.getDrawable这个方法每次得到的drawable对象不一样，但是((BitmapDrawable)drawable).getBitmap()是同一个值
		{
			mHotseatAllAppsButtonIcon = null;
		}
		//换壁纸
		applyWallpaperInThread();
	}
	//zhujieping add end
	;
	
	//xiatian add start	//需求：添加“一键换主题”功能（1、虚图标；2、点击后，从已经安装的其他主题中，随机换一个）。
	public String getThemePackageNameInRandom()
	{
		String ret = null;
		String currentThemePackageName = PubProviderHelper.queryValue( "theme" , "theme" );
		ArrayList<String> mThemePackageNameListExceptCurTheme = new ArrayList<String>();
		ArrayList<ResolveInfo> localResDesList = getItemsList();
		Iterator<ResolveInfo> it = localResDesList.iterator();
		while( it.hasNext() )
		{
			ResolveInfo mThemeItemResinfo = it.next();
			String mThemeItemPackageName = mThemeItemResinfo.activityInfo.applicationInfo.packageName;
			if( mThemeItemPackageName.equals( currentThemePackageName ) == false )
			{
				mThemePackageNameListExceptCurTheme.add( mThemeItemPackageName );
			}
		}
		int mThemePackageNameListExceptCurThemeSize = mThemePackageNameListExceptCurTheme.size();
		if( mThemePackageNameListExceptCurThemeSize > 0 )//xiatian add	//解决“只有一个默认主题时，点击一键换主题图标，这时桌面重启”的问题。
		{
			int index = 0;
			if( mThemePackageNameListExceptCurThemeSize > 1 )
			{
				index = randomIndex( mThemePackageNameListExceptCurThemeSize );
			}
			ret = mThemePackageNameListExceptCurTheme.get( index );
		}
		return ret;
	}
	//xiatian add end
	;
}
