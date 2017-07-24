package com.cooee.framework.app;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;


public class BaseAppState
{
	
	protected static Context sContext;
	private static Activity mActivityInstance = null;
	public static String mActivityClass[] = {
			"com.iLoong.launcher.MList.Main_FirstActivity" ,
			"com.iLoong.launcher.MList.Main_SecondActivity" ,
			"com.iLoong.launcher.MList.Main_ThreeActivity" ,
			"com.iLoong.launcher.MList.Main_FourthActicity" ,
			// zhangjin@2015/12/30 bug i_0013199 ADD START
			"com.cooee.update.UpdateActivity"
	// zhangjin@2015/12/30 ADD END
	};
	
	//<phenix modify> liuhailin@2015-01-27 modify begin
	public static boolean isApkInstalled(
			ComponentName mComponentName )
	{
		if( ( sContext == null ) || ( mComponentName == null ) || ( "".equals( mComponentName ) ) )
		{
			return false;
		}
		PackageManager mPackageManager = sContext.getPackageManager();
		Intent intent = new Intent();
		intent.setComponent( mComponentName );
		if( mPackageManager.queryIntentActivities( intent , 0 ).size() == 0 )
		{
			return false;
		}
		return true;
	}
	//<phenix modify> liuhailin@2015-01-27 modify end
	;
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public static boolean isApkInstalled(
			String packageName )
	{
		if( ( sContext == null ) || ( packageName == null ) || ( "".equals( packageName ) ) )
		{
			return false;
		}
		PackageManager mPackageManager = sContext.getPackageManager();
		boolean mIsApkInstalled = false;
		try
		{
			mPackageManager.getPackageInfo( packageName , PackageManager.GET_ACTIVITIES );
			mIsApkInstalled = true;
		}
		catch( PackageManager.NameNotFoundException e )
		{
			//捕捉到异常,说明未安装  
			mIsApkInstalled = false;
		}
		return mIsApkInstalled;
	}
	
	public static boolean isSDCardExist()
	{
		if( android.os.Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	
	public static void saveBitmap(
			Bitmap mBitmap ,
			String mSaveName )
	{
		if( sContext == null )
		{
			return;
		}
		String mSavePath = getSDCardPath();
		if( mSavePath == null )
		{
			return;
		}
		//保存Bitmap   
		FileOutputStream fos = null;
		try
		{
			File path = new File( mSavePath );
			//文件  
			String filepath = StringUtils.concat( mSavePath , File.separator , mSaveName , ".png" );
			File file = new File( filepath );
			if( !path.exists() )
			{
				path.mkdirs();
			}
			if( !file.exists() )
			{
				file.createNewFile();
			}
			fos = new FileOutputStream( file );
			if( fos != null )
			{
				mBitmap.compress( Bitmap.CompressFormat.PNG , 90 , fos );
				fos.flush();
				fos.close();
				Toast.makeText( sContext , StringUtils.concat( "图片文件已保存至T卡" , filepath ) , Toast.LENGTH_LONG ).show();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( fos != null )
			{
				try
				{
					fos.flush();
					fos.close();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static String getSDCardPath()
	{
		if( sContext == null )
		{
			return null;
		}
		File sdcardDir = null;
		//获取T卡是否准备就绪  
		if( isSDCardExist() )
		{
			sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.toString();
		}
		Toast.makeText( sContext , "请检查T卡是否正常" , Toast.LENGTH_LONG ).show();
		return null;
	}
	
	//WangLei add start //实现默认配置AppWidget的流程
	/**
	 * 当应用安装在SD卡中时，附带的插件不可用，所以在添加AppWidget时先判断一下，避免无用功
	 * @param packageName AppWidget所在的应用包名
	 * @param PppackageManager Android包管理类，用于根据包名获取应用信息
	 * @return boolean 应用安装在SD卡，return true ,else return false
	 */
	public static boolean isAppInstalledSdcard(
			String packageName ,
			PackageManager packageManager )
	{
		try
		{
			ApplicationInfo applicationInfo = packageManager.getApplicationInfo( packageName , 0 );
			return ( applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE ) != 0;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
			return true;
		}
	}
	//WangLei add end
	;
	
	//xiatian add start	//桌面支持配置隐藏特定的activity界面。
	public static boolean hideAppList(
			Context context ,
			String packageName )
	{
		return hideAppList( context , packageName , "" );
	}
	
	public static boolean hideAppList(
			Context context ,
			String packageName ,
			String className )
	{
		if( packageName == null )
		{
			return false;
		}
		if( packageName.equals( context.getPackageName() ) )
		{
			if( isMicroEntryClass( className ) )
			{
				return false;
			}
			return true;
		}
		else if( packageName.startsWith( "com.coco.themes." ) )
		{
			return true;
		}
		//xiatian add start	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
		if( isInDelayShowAppList( context , packageName , className ) )
		{
			if( isInAllreadyDelayShowAppList( context , packageName , className ) )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		//xiatian add end
		if( BaseDefaultConfig.mHideAppList.size() > 0 )
		{
			for( ComponentName mComponentName : BaseDefaultConfig.mHideAppList )
			{
				//先看下，是不是隐藏apk的所有支持显示到桌面的activity
				if( mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( "" ) )
				{
					return true;
				}
				//再看下，是不是隐藏apk的某个支持显示到桌面的activity
				if( mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( className ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	//xiatian add end
	;
	
	public static boolean isMicroEntryClass(
			String className )
	{
		if( className != null )
		{
			for( String item : mActivityClass )
			{
				if( item.equals( className ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public int getStatusBarHeight()
	{
		int result = 0;
		Context slaveContext = null;
		Resources mResources = null;
		int resourceId = 0;
		String mPackageName = "android";
		try
		{
			slaveContext = sContext.createPackageContext( mPackageName , Context.CONTEXT_IGNORE_SECURITY );
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		if( slaveContext != null )
		{
			mResources = slaveContext.getResources();
			resourceId = ResourceUtils.getDimenResourceIdByReflectIfNecessary( -1 , mResources , mPackageName , "status_bar_height" );
			if( resourceId > 0 )
			{
				result = mResources.getDimensionPixelSize( resourceId );
			}
		}
		return result;
	}
	
	//xiatian add start	//桌面支持配置特定的activity的显示名称。
	public static String getAppReplaceTitle(
			String mDefaultTitle ,
			Context mContext ,
			ComponentName mKey )
	{
		if( mKey == null )
		{
			return mDefaultTitle;
		}
		String ret = "";
		int mTitleId = -1;
		if( BaseDefaultConfig.mAppReplaceTitleList.size() > 0 )
		{
			if( BaseDefaultConfig.mAppReplaceTitleList.get( mKey ) != null )
			{
				mTitleId = BaseDefaultConfig.mAppReplaceTitleList.get( mKey ).intValue();
			}
		}
		if( mTitleId == -1 )
		{
			ret = mDefaultTitle;
		}
		else
		{
			ret = BaseDefaultConfig.getString( mTitleId );
		}
		return ret;
	}
	//xiatian add end
	;
	
	public static void setActivityInstance(
			Activity mActivity )
	{
		mActivityInstance = mActivity;
	}
	
	public static Activity getActivityInstance()
	{
		return mActivityInstance;
	};
	
	//xiatian add start	//桌面支持配置隐藏特定的widget插件。
	public static boolean hideWidgetList(
			String packageName ,
			String className )
	{
		if( packageName == null )
		{
			return false;
		}
		if( BaseDefaultConfig.mHideWidgetList.size() > 0 )
		{
			for( ComponentName mComponentName : BaseDefaultConfig.mHideWidgetList )
			{
				//先看下，是不是隐藏整个widget的apk中所有支持显示到桌面的widget插件
				if( mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( "" ) )
				{
					return true;
				}
				//再看下，是不是隐藏widget的apk中特定支持显示到桌面的widget插件
				if( mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( className ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	//xiatian add end
	;
	
	//xiatian add start	//桌面支持配置隐藏特定的快捷方式插件。
	public static boolean hideShortcutList(
			String packageName ,
			String className )
	{
		if( packageName == null )
		{
			return false;
		}
		if( BaseDefaultConfig.mHideShortcutList.size() > 0 )
		{
			for( ComponentName mComponentName : BaseDefaultConfig.mHideShortcutList )
			{
				//先看下，是不是隐藏整个apk中所有支持显示到桌面的快捷方式插件
				if( mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( "" ) )
				{
					return true;
				}
				//再看下，是不是隐藏整个apk中特定支持显示到桌面的快捷方式插件
				if( mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( className ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	//xiatian add end
	;
	
	public static boolean isWifiEnabled(
			Context context )
	{
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo networkINfo = cm.getActiveNetworkInfo();
		if( networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_WIFI )
		{
			return true;
		}
		return false;
	}
	
	//xiatian add start	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
	public static boolean isInDelayShowAppList(
			Context context ,
			String packageName ,
			String className )
	{
		if( packageName == null )
		{
			return false;
		}
		if( className == null )
		{
			className = "";
		}
		if( BaseDefaultConfig.mDelayShowAppList.size() > 0 )
		{
			for( ComponentName mComponentName : BaseDefaultConfig.mDelayShowAppList.keySet() )
			{
				if( mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( className ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isInAllreadyDelayShowAppList(
			Context context ,
			String packageName ,
			String className )
	{
		if( packageName == null )
		{
			return false;
		}
		if( className == null )
		{
			className = "";
		}
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getActivityInstance() );
		String mAllreadyDelayShowAppList = mSharedPreferences.getString( BaseDefaultConfig.ALLREADY_DELAY_SHOW_APP_LIST_KEY , "" );
		String[] mItemStrList = mAllreadyDelayShowAppList.split( ";" );
		int mCount = mItemStrList.length;
		for( int i = 0 ; i < mCount ; i++ )
		{
			ComponentName mComponentName = ComponentName.unflattenFromString( mItemStrList[i] );
			if( mComponentName != null && mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( className ) )
			{
				return true;
			}
		}
		return false;
	}
	
	public static void add2AllreadyDelayShowAppList(
			Context context ,
			String packageName ,
			String className )
	{
		if( packageName == null || className == null )
		{
			return;
		}
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getActivityInstance() );
		String mAllreadyDelayShowAppListBefore = mSharedPreferences.getString( BaseDefaultConfig.ALLREADY_DELAY_SHOW_APP_LIST_KEY , "" );
		String[] mItemStrList = mAllreadyDelayShowAppListBefore.split( ";" );
		int mCount = mItemStrList.length;
		for( int i = 0 ; i < mCount ; i++ )
		{
			ComponentName mComponentName = ComponentName.unflattenFromString( mItemStrList[i] );
			if( mComponentName != null && mComponentName.getPackageName().equals( packageName ) && mComponentName.getClassName().equals( className ) )
			{
				return;
			}
		}
		String mAllreadyDelayShowAppListAfter = StringUtils.concat(
				mAllreadyDelayShowAppListBefore ,
				( TextUtils.isEmpty( mAllreadyDelayShowAppListBefore ) ? ( "" ) : ( ";" ) ) ,
				packageName ,
				File.separator ,
				className );
		mSharedPreferences.edit().putString( BaseDefaultConfig.ALLREADY_DELAY_SHOW_APP_LIST_KEY , mAllreadyDelayShowAppListAfter ).commit();
	}
	
	public static String[] getToShowApksInDelayShowAppList(
			Context context ,
			long mUseTime )
	{
		String[] mPackages = null;
		ArrayList<String> mPackagesList = new ArrayList<String>();
		for( Map.Entry<ComponentName , Long> entry : BaseDefaultConfig.mDelayShowAppList.entrySet() )
		{
			ComponentName mKey = (ComponentName)entry.getKey();
			String mPackageName = mKey.getPackageName();
			String mClassName = mKey.getClassName();
			if( mKey != null && isInAllreadyDelayShowAppList( context , mPackageName , mClassName ) )
			{
				continue;
			}
			Long mValue = (Long)entry.getValue();
			if( mValue < mUseTime )
			{
				add2AllreadyDelayShowAppList( context , mPackageName , mClassName );
				mPackagesList.add( mPackageName );
			}
		}
		int mCount = mPackagesList.size();
		if( mCount > 0 )
		{
			mPackages = new String[mCount];
			for( int i = 0 ; i < mCount ; i++ )
			{
				mPackages[i] = mPackagesList.get( i );
			}
		}
		return mPackages;
	}
	//xiatian add end
	;
	
	public static boolean isAlreadyCategory(
			Context mContext )
	{
		SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences( mContext );
		String value = mSharedPrefs.getString( "classificationTime"/*OperateHelp.ClassificationTime*/, null );
		if( value == null )
		{
			return false;
		}
		return true;
	}
	
	public static void setComponentDisabled(
			Context context ,
			ComponentName mComponentName )
	{
		if( mComponentName == null )
		{
			return;
		}
		try
		{
			PackageManager mPackageManager = context.getPackageManager();
			if( mPackageManager.getComponentEnabledSetting( mComponentName ) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED )
			{
				mPackageManager.setComponentEnabledSetting( mComponentName , PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	//xiatian add start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
	public static boolean isLayoutRTL()
	{
		boolean mIsLayoutRTL = false;
		if(
		//
		( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 )
		//
		&& ( sContext != null )
		//
		&& sContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL
		//
		)
		{
			mIsLayoutRTL = true;
		}
		return mIsLayoutRTL;
	}
	//xiatian add end
}
