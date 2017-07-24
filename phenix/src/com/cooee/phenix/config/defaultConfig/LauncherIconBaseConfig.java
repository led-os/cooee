package com.cooee.phenix.config.defaultConfig;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.theme.ThemeManager;


public class LauncherIconBaseConfig
{
	
	private static final String TAG = "DefaultLayoutOther";
	//	public static ArrayList<ResolveInfo> allApp;
	public static ArrayList<DefaultIcon> defaultIcon = new ArrayList<DefaultIcon>();
	public static ArrayList<DefaultIcon> defaultIconBase = new ArrayList<DefaultIcon>();
	static LauncherIconBaseConfig launcherIconBaseConfig;
	public static String config_default_theme_package_name = null;
	
	public LauncherIconBaseConfig()
	{
		launcherIconBaseConfig = this;
	}
	
	public static void initIconBase(
			Context context )
	{
		defaultIconBase.clear();
		String[] iconTitle = LauncherDefaultConfig.getStringArray( R.array.icon_title );
		String[] iconImage = LauncherDefaultConfig.getStringArray( R.array.icon_image );
		String[] iconClassName = LauncherDefaultConfig.getStringArray( R.array.icon_class );
		String[] iconPackageName = LauncherDefaultConfig.getStringArray( R.array.icon_package );
		for( int i = 0 ; i < iconTitle.length ; i++ )
		{
			DefaultIcon temp = new DefaultIcon();
			temp.title = iconTitle[i];
			Iterator<DefaultIcon> ite = defaultIconBase.iterator();
			while( ite.hasNext() )
			{
				DefaultIcon icon = ite.next();
				if( icon.title.equals( temp.title ) )
					ite.remove();
			}
			temp.imageName = iconImage[i];
			temp.pkgName = iconPackageName[i];
			temp.className = iconClassName[i];
			if( temp.className.equals( "noting" ) )
				temp.className = "";
			defaultIconBase.add( temp );
		}
		setDefaultIcon( defaultIconBase );
	}
	
	public static void setDefaultIcon(
			List<DefaultIcon> mDefaultIcon )
	{
		int i = 0;
		int size = mDefaultIcon.size();
		if( size < 1 )
			return;
		for( i = 0 ; i < size ; i++ )
		{
			mDefaultIcon.get( i ).pkgNameArray = splitString( ";" , mDefaultIcon.get( i ).pkgName );
			mDefaultIcon.get( i ).classNameArray = splitString( ";" , mDefaultIcon.get( i ).className );
		}
	}
	
	public static void initDefaultIconByResolveInfo(
			ResolveInfo info )
	{
		String mPackageName = info.activityInfo.applicationInfo.packageName;
		String mClassName = info.activityInfo.name;
		String mIconNameInThemeList = getIconNameInThemeList( mPackageName , mClassName );
		if( TextUtils.isEmpty( mIconNameInThemeList ) == false )
		{
			changeIcon( info , mIconNameInThemeList );
		}
	}
	
	public static String getIconNameInThemeList(
			String mPackageName ,
			String mClassName )
	{
		String mIconNameInThemeList = null;
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "getIconNameInThemeList: packageName:" , mPackageName , " className:" , mClassName ) );
		for( int inter = 0 ; inter < defaultIcon.size() ; inter++ )
		{
			String allPackageName = defaultIcon.get( inter ).pkgName;
			String allClassName = defaultIcon.get( inter ).className;
			if( null == allPackageName || null == allClassName )
				continue;
			//xiatian start	//解决：“类名中有符号$时，用String的matches方法，返回值出错”的问题
			//			boolean mIsMatch = ( allPackageName.matches( String.format( ".*;%s;.*" , appName ) ) ) && ( allClassName.matches( String.format( ".*;%s;.*" , className ) ) );//xiatian del
			//xiatian add start
			boolean mIsMatch = false;
			List<String> mPackageNameList = defaultIcon.get( inter ).pkgNameArray;//allPackageName.split( ";" );
			List<String> mClassNameList = defaultIcon.get( inter ).classNameArray;//allClassName.split( ";" );
			for( int i = 0 ; i < mPackageNameList.size() ; i++ )
			{
				if( mPackageNameList.get( i ).equals( mPackageName ) )
				{
					for( int j = 0 ; j < mClassNameList.size() ; j++ )
					{
						if( mClassNameList.get( j ).equals( mClassName ) )
						{
							mIconNameInThemeList = defaultIcon.get( inter ).imageName;
							return mIconNameInThemeList;
						}
					}
				}
			}
			//xiatian add end
			//xiatian end
		}
		return mIconNameInThemeList;
	}
	
	static void changeIcon(
			ResolveInfo info ,
			String image )
	{
		if( image == null || info == null )
			return;
		Bitmap tempBitmap = getPackageThemeIconPath( image );
		if( tempBitmap != null )
		{
			LauncherAppState mLauncherAppState = LauncherAppState.getInstance();
			if( mLauncherAppState != null )
			{
				IconCache mIconCache = mLauncherAppState.getIconCache();
				if( mIconCache != null )
				{
					mIconCache.setIcon( info , tempBitmap );
				}
			}
		}
	}
	
	public static Bitmap getPackageThemeIconPath(
			String imageName )
	{
		Bitmap image = null;
		String imagePath;
		if( imageName == null )
		{
			return null;
		}
		//		imagePath = LauncherDefaultConfig.THEME_ICON_FOLDER + "/" + imageName;//xiatian del	//需求：适配“新主题”，兼容“老主题”。
		if( ThemeManager.getInstance() == null )
		{
			return null;
		}
		ThemeManager mThemeManager = ThemeManager.getInstance();
		// zhujieping@2015/03/13 ADD START
		//先从res文件中读取图片，读取不到从assert下读取
		if( mThemeManager.currentThemeIsSystemTheme() && !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )//系统主题，且配置了本地路径，去本地路径下取图片
		{
			String mTagStart = "theme_default_";
			String path;
			if( imageName.startsWith( mTagStart ) )
			{
				path = StringUtils.concat(
						LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ,
						File.separator ,
						LauncherDefaultConfig.THEME_ICON_FOLDER ,
						File.separator ,
						imageName.substring( mTagStart.length() , imageName.length() ) );
			}
			else
			{
				path = StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , File.separator , LauncherDefaultConfig.THEME_ICON_FOLDER , File.separator , imageName );
			}
			image = mThemeManager.getBitmapFromLocal( path );
		}
		if( image == null )
			image = mThemeManager.getCurrentThemeDescription().getBitmapFromResource( imageName );
		if( image != null )
		{
			//xiatian add start	//解决：“可替换列表的图标比其他图标（第三方图标和文件夹图标）略大”的问题。
			//			return image;//xiatian del
			//xiatian add start
			Bitmap image2 = Utilities.createIconBitmap(
					image ,
					mThemeManager.getContext() ,
					Utilities.sIconWidth ,
					Utilities.sIconHeight ,
					Utilities.sIconTextureWidth ,
					Utilities.sIconTextureHeight ,
					true ,
					true );
			return image2;
			//xiatian add end
			//xiatian end
		}
		// zhujieping@2015/03/13 ADD END
		//xiatian add start	//需求：适配“新主题”，兼容“老主题”。
		//【备注】
		//		1、新主题特珠性如下：
		//			（1）“assets\theme\icon\80”下的资源和“assets\theme\iconbg”下的资源放到res目录的对于分辨率的drawable文件夹下
		//			（2）原“assets\theme\icon\80”下的资源，命名规则改为“theme_default_xxx”
		//			（3）原“assets\theme\iconbg”下的资源，命名规则如下修改：
		//				<1>“icon_0”改为“theme_third_party_bg_0”，“icon_1”改为“theme_third_party_bg_1”，以此类推
		//				<2>去掉“mask”，命名为“theme_third_party_mask_0”、“theme_third_party_mask_1”，以此类推
		//				<3>“icon_cover_plate_0”改为“theme_third_party_cover_0”，“icon_cover_plate_1”改为“theme_third_party_cover_1”,以此类推	
		//		2、从以下几个方面，兼容老主题
		//			（1）支持读取“assets\theme\icon\80”下的资源
		//			（2）支持读取“assets\theme\iconbg”下的“icon_0”、“icon_1”、“icon_2”资源
		//			（3）支持读取“assets\theme\iconbg”下的“mask”资源
		//			（4）支持读取“assets\theme\iconbg”下的“icon_cover_plate”、“icon_cover_plate_0”、“icon_cover_plate_1”资源
		String mTagStart = "theme_default_";
		if( imageName.startsWith( mTagStart ) )
		{//读不到资源（兼容老主题：若资源名称为“theme_default_xxx”，则改为“xxx”，并从“assets\theme\icon\80”目录下再次读取资源）
			imageName = imageName.substring( mTagStart.length() , imageName.length() );
		}
		imagePath = StringUtils.concat( LauncherDefaultConfig.THEME_ICON_FOLDER , File.separator , imageName );
		//xiatian add end
		image = mThemeManager.getBitmapIgnoreSystemTheme( imagePath );
		//xiatian add start	//解决：“可替换列表的图标比其他图标（第三方图标和文件夹图标）略大”的问题。
		//			return image;//xiatian del
		//xiatian add start
		Bitmap image2 = Utilities.createIconBitmap(
				image ,
				mThemeManager.getContext() ,
				Utilities.sIconWidth ,
				Utilities.sIconHeight ,
				Utilities.sIconTextureWidth ,
				Utilities.sIconTextureHeight ,
				true ,
				true );
		return image2;
		//xiatian add end
		//xiatian end
	}
	
	public static void MergeIconBaseWithDefaultIcon()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "addToDefaultIcon add iconbase to icon defaultIconBase.size=" , defaultIconBase.size() , " defaultIcon.size=" , defaultIcon.size() ) );
		ArrayList<DefaultIcon> tempDefaultIcons = new ArrayList<DefaultIcon>();
		ArrayList<DefaultIcon> tempDefaultIconBases = new ArrayList<DefaultIcon>();
		tempDefaultIconBases.addAll( defaultIconBase );
		if( defaultIcon.size() != 0 )
		{
			for( int i = 0 ; i < defaultIcon.size() ; i++ )
			{
				DefaultIcon temp = defaultIcon.get( i );
				for( int j = 0 ; j < tempDefaultIconBases.size() ; j++ )
				{
					DefaultIcon icon = tempDefaultIconBases.get( j );
					if( icon.title.equals( temp.title ) )
					{
						temp.pkgName = spliceString( temp.pkgName , ";" , icon.pkgName );
						temp.className = spliceString( temp.className , ";" , icon.className );
						tempDefaultIconBases.remove( icon );
						j--;
					}
				}
				tempDefaultIcons.add( temp );
			}
		}
		tempDefaultIcons.addAll( tempDefaultIconBases );
		defaultIcon.clear();
		defaultIcon = tempDefaultIcons;
	}
	
	/**
	 * 通过分隔符合�?
	 * @param name1 要合并的字符�?
	 * @param regularExpression 合并的分隔符
	 * @param name2 要合并的字符�?
	 * @return
	 */
	public static String spliceString(
			String name1 ,
			String regularExpression ,
			String name2 )
	{
		return StringUtils.concat( name1 , regularExpression , name2 );
	}
	
	/**
	 * 通过分隔符拆�?返回List
	 * @param regularExpression 分隔�?
	 * @param allName 要拆分的字符�?
	 * @return 拆分后的结果
	 */
	public static List<String> splitString(
			String regularExpression ,
			String allName )
	{
		List<String> stringArray = new ArrayList<String>();
		if( allName == null || allName.length() <= 0 )
		{
			return null;
		}
		String[] result = allName.split( regularExpression );
		if( result.length <= 0 )
		{
			return null;
		}
		else
		{
			stringArray = Arrays.asList( result );
			return stringArray;
		}
	}
	
	public static boolean isHasMatch(
			String appName ,
			String cmpName ,
			List<String> mPackageNameList ,
			List<String> mClassNameList )
	{
		boolean mIsMatch = false;
		for( int i = 0 ; i < mPackageNameList.size() ; i++ )
		{
			if( mIsMatch )
			{
				break;
			}
			if( mPackageNameList.get( i ).equals( appName ) )
			{
				for( int j = 0 ; j < mClassNameList.size() ; j++ )
				{
					if( mClassNameList.get( j ).equals( cmpName ) )
					{
						mIsMatch = true;
						break;
					}
				}
			}
		}
		return mIsMatch;
	}
	
	// zhujieping@2015/03/13 ADD START
	public static boolean hasReplaceIcon(
			String appName ,
			String cmpName )
	{
		for( int inter = 0 ; inter < defaultIcon.size() ; inter++ )
		{
			String allPackageName = defaultIcon.get( inter ).pkgName;
			String allClassName = defaultIcon.get( inter ).className;
			if( null == allPackageName || null == allClassName )
				continue;
			//xiatian start	//解决：“类名中有符号$时，用String的matches方法，返回值出错”的问题
			//			boolean mIsMatch = ( allPackageName.matches( String.format( ".*;%s;.*" , appName ) ) ) && ( allClassName.matches( String.format( ".*;%s;.*" , cmpName ) ) );//xiatian del
			//xiatian add start
			boolean mIsMatch = false;
			List<String> mPackageNameList = defaultIcon.get( inter ).pkgNameArray;//allPackageName.split( ";" );
			List<String> mClassNameList = defaultIcon.get( inter ).classNameArray;//allClassName.split( ";" );
			mIsMatch = isHasMatch( appName , cmpName , mPackageNameList , mClassNameList );
			//xiatian add end
			//xiatian end
			if( mIsMatch )
			{
				return isImageExist( defaultIcon.get( inter ).imageName );
			}
		}
		return false;
	}
	
	public static boolean isImageExist(
			String imageName )
	{
		if( imageName == null )
			return false;
		if( ThemeManager.getInstance() == null )
		{
			return false;
		}
		String imagePath;
		if( ThemeManager.getInstance().currentThemeIsSystemTheme() )
		{
			if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )
			{
				String mTagStart = "theme_default_";
				String path;
				if( imageName.startsWith( mTagStart ) )
				{
					path = StringUtils.concat(
							LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ,
							File.separator ,
							LauncherDefaultConfig.THEME_ICON_FOLDER ,
							File.separator ,
							imageName.substring( mTagStart.length() , imageName.length() ) );
				}
				else
				{
					path = StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , File.separator , LauncherDefaultConfig.THEME_ICON_FOLDER , File.separator , imageName );
				}
				File f = new File( path );
				if( f != null && f.exists() )
				{
					return true;
				}
			}
		}
		if( ThemeManager.getInstance().getCurrentThemeDescription().hasBitmapFromResource( imageName ) )
		{
			return true;
		}
		imagePath = StringUtils.concat( LauncherDefaultConfig.THEME_ICON_FOLDER , File.separator , imageName );
		return ThemeManager.getInstance().isFileExistIgnoreSystem( imagePath );
	}
	// zhujieping@2015/03/13 ADD END
	;
	
	//xiatian add start	//需求：添加配置项“mIsIconFollowTheme”，虚图标的显示图标是否跟随主题（从主题中读取相应图标）。true为跟随主题；false为不跟随主题。默认为true。
	public static void initDefaultIconByComponentNameAndTitle(
			ComponentName mComponentName ,
			String mTitle )
	{
		String mPackageName = mComponentName.getPackageName();
		String mClassName = mComponentName.getClassName();
		String mIconNameInThemeList = getIconNameInThemeList( mPackageName , mClassName );
		if( TextUtils.isEmpty( mIconNameInThemeList ) == false )
		{
			changeIcon( mComponentName , mTitle , mIconNameInThemeList );
		}
	}
	
	static void changeIcon(
			ComponentName mComponentName ,
			String mTitle ,
			String image )
	{
		if( image == null || mComponentName == null )
			return;
		Bitmap tempBitmap = getPackageThemeIconPath( image );
		if( tempBitmap != null )
		{
			LauncherAppState mLauncherAppState = LauncherAppState.getInstance();
			if( mLauncherAppState != null )
			{
				IconCache mIconCache = mLauncherAppState.getIconCache();
				if( mIconCache != null )
				{
					mIconCache.setIcon( mComponentName , mTitle , tempBitmap );
				}
			}
		}
	}
	
	//xiatian add end
	//zhujieping add start	//换主题不重启
	public static Bitmap getDefaultIcon(
			ComponentName componentName )
	{
		String appName = componentName.getPackageName();
		String className = componentName.getClassName();
		Log.v( TAG , "getDefaultIcon: packageName:" + appName + " className:" + className );
		for( int inter = 0 ; inter < defaultIcon.size() ; inter++ )
		{
			if( isHasMatch( appName , className , defaultIcon.get( inter ).pkgNameArray , defaultIcon.get( inter ).classNameArray ) )
			{
				Bitmap ret = getPackageThemeIconPath( defaultIcon.get( inter ).imageName );
				if( ret != null )
				{
					ret = Utilities.resampleIconBitmap( ret , LauncherAppState.getInstance().getContext() );
				}
				return ret;
			}
		}
		return null;
	}
	//zhujieping add end
	;
}
