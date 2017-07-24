package com.cooee.phenix.data;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.CellLayout;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.util.ComponentKey;
import com.iLoong.launcher.MList.MeLauncherInterface;


/**
 * Represents an app in AllAppsView.
 */
//添加智能分类功能 , change by shlt@2015/02/09 UPD START
//class AppInfo extends ItemInfo
public class AppInfo extends EnhanceItemInfo implements IOnThemeChanged//zhujieping add,换主题不重启
//添加智能分类功能 , change by shlt@2015/02/09 UPD END
{
	
	private static final String TAG = "AppInfo";
	/**
	 * The intent used to start the application.
	 */
	Intent intent;
	/**
	 * A bitmap version of the application icon.
	 */
	Bitmap iconBitmap;
	/**
	 * The time at which the app was first installed.
	 */
	long firstInstallTime;
	ComponentName componentName;
	public static final int DOWNLOADED_FLAG = 1;
	public static final int UPDATED_SYSTEM_APP_FLAG = 2;
	int flags = 0;
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	String versionName = null;
	String versionCode = null;
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	//cheyingkun add start	//解决“常用应用显示动态时，改变日期后返回桌面，桌面重启”的问题【c_0004419】
	/**
	 * A bitmap backups version of the application icon.
	 */
	Bitmap iconBitmapBackup;
	private long lastUpdateTime = -1;
	private SharedPreferences mPreferences = null;
	private boolean isHideIcon = false;
	
	//cheyingkun add end
	AppInfo()
	{
		itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	public void setIntent(
			Intent intent )
	{
		this.intent = intent;
		//<数据库字段更新> liuhailin@2015-03-26 del begin
		//getExtrasByIntent( intent );
		//<数据库字段更新> liuhailin@2015-03-26 del end
	}
	
	//protected Intent getIntent()
	@Override
	public Intent getIntent()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		//<数据库字段更新> liuhailin@2015-03-26 del begin
		//intent = addExtrasToIntent( intent );
		//<数据库字段更新> liuhailin@2015-03-26 del end
		return intent;
	}
	
	public Bitmap getIconBitmap()
	{
		return iconBitmap;
	}
	
	public void setIconBitmap(
			Bitmap mIconBitmap )
	{
		iconBitmap = mIconBitmap;
	}
	
	// zhangjin@2015/08/31 ADD START
	public void updateIcon(
			IconCache iconCache )
	{
		iconBitmap = iconCache.getIcon( intent );
	}
	
	// zhangjin@2015/08/31 ADD END
	/**
	 * Must not hold the Context.
	 */
	public AppInfo(
			PackageManager pm ,
			ResolveInfo info ,
			IconCache iconCache ,
			HashMap<Object , CharSequence> labelCache )
	{
		final String packageName = info.activityInfo.applicationInfo.packageName;
		this.setComponentName( new ComponentName( packageName , info.activityInfo.name ) );
		this.setContainer( ItemInfo.NO_ID );
		this.setActivity( componentName , Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		try
		{
			PackageInfo pi = pm.getPackageInfo( packageName , 0 );
			flags = initFlags( pi );
			firstInstallTime = initFirstInstallTime( pi );
			versionName = pi.versionName;
			versionCode = String.valueOf( pi.versionCode );
		}
		catch( NameNotFoundException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "PackageManager.getApplicationInfo failed for " , packageName ) );
		}
		iconCache.getTitleAndIcon( this , info , labelCache );
		if(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//
		|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
		//
		)//主菜单有menu按钮，才需要lastupdatetime
		{
			int sysVersion = Integer.parseInt( VERSION.SDK );
			if( sysVersion < 9 )
			{
				boolean installed = false;
				if( ( flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
				{
					installed = true;
				}
				else if( ( flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) == 0 )
				{
					installed = true;
				}
				if( installed )
				{
					String dir = info.activityInfo.applicationInfo.publicSourceDir;
					lastUpdateTime = new File( dir ).lastModified();
				}
			}
			else
			{
				try
				{
					PackageInfo packageInfo = pm.getPackageInfo( packageName , 0 );
					lastUpdateTime = packageInfo.lastUpdateTime;
				}
				catch( NameNotFoundException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 判断应用的类型,系统应用返回0 ,用户安装应用返回1,传入空值时返回-1(目前传入空值的情况是因为,应用安装在T卡,T卡挂载后,更新图标为灰色,用PackageManager根据包名来回去信息时获取不到值)
	 * @param pi
	 * @return
	 */
	public static int initFlags(
			PackageInfo pi )
	{
		//cheyingkun add start	//deleteGreyApp(灰化图标可删除)
		if( pi == null )
		{
			return 1;//1表示第三方应用,0表示系统应用
		}
		//cheyingkun add end
		int appFlags = pi.applicationInfo.flags;
		int flags = 0;
		if( ( appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) == 0 )
		{
			flags |= DOWNLOADED_FLAG;
			if( ( appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
			{
				flags |= UPDATED_SYSTEM_APP_FLAG;
			}
		}
		return flags;
	}
	
	public static long initFirstInstallTime(
			PackageInfo pi )
	{
		//cheyingkun add start	//解决“重复安装时，T卡应用变得无法卸载”的问题。【i_0011422】
		if( pi == null )
		{
			return 0;
		}
		//cheyingkun add end
		return pi.firstInstallTime;
	}
	
	public AppInfo(
			AppInfo info )
	{
		super( info );
		componentName = info.getComponentName();
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
		//title = info.title.toString();
		if( info.getTitle() == null )
		{
			info.setTitle( "name = null" );
		}
		setTitle( info.getTitle().toString() );
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
		intent = new Intent( info.getIntent() );
		flags = info.getFlags();
		firstInstallTime = info.getFirstInstallTime();
	}
	
	/**
	 * Creates the application intent based on a component name and various launch flags.
	 * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
	 *
	 * @param className the class name of the component representing the intent
	 * @param launchFlags the launch flags
	 */
	final void setActivity(
			ComponentName className ,
			int launchFlags )
	{
		intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_LAUNCHER );
		intent.setComponent( className );
		intent.setFlags( launchFlags );
		itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
	}
	
	@Override
	public String toString()
	{
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
		if( getTitle() == null )
		{
			setTitle( "name = null" );
		}
		//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
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
				"-container;" ,
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
	
	public static void dumpApplicationInfoList(
			String tag ,
			String label ,
			ArrayList<AppInfo> list )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( tag , StringUtils.concat( label , " size=" , list.size() ) );
			for( AppInfo info : list )
			{
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
				//Log.d( tag , "   title=\"" + info.title + "\" iconBitmap=" + info.getIconBitmap() + " firstInstallTime=" + info.getFirstInstallTime() );
				if( info.getTitle() == null )
				{
					info.setTitle( "name = null" );
				}
				Log.d( tag , StringUtils.concat( "   title=\"" , info.getTitle().toString() , "\" iconBitmap=" + info.getIconBitmap() , " firstInstallTime=" , info.getFirstInstallTime() ) );
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
			}
		}
	}
	
	public ShortcutInfo makeShortcut()
	{
		return new ShortcutInfo( this );
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public ComponentName getComponentName()
	{
		return componentName;
	}
	
	public ComponentName setComponentName(
			ComponentName componentName )
	{
		return this.componentName = componentName;
	}
	
	public int getFlags()
	{
		return flags;
	}
	
	public String getVersionCode()
	{
		return versionCode;
	}
	
	public String getVersionName()
	{
		return versionName;
	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	
	@Override
	public void setTitle(
			String title )
	{
		super.setTitle( title );
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
	
	//xiatian add start	//整理代码：整理接口willAcceptDrop
	@Override
	public boolean willAcceptDrop()
	{
		boolean ret = false;
		ret = super.willAcceptDrop();
		if( ret == false )
		{
			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER && getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION )
			{
				//ME_RTFSC 
				//return (appInfo.getFlags() & AppInfo.DOWNLOADED_FLAG) != 0;
				if( ( getFlags() & DOWNLOADED_FLAG ) != 0 )
				{
					return true;
				}
				else if( MeLauncherInterface.getInstance().MeIsMicroEntry( getComponentName().getClassName() ) )
				{
					return true;
				}
				else
				{
					return false;
				}
				//ME_RTFSC
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
		ShortcutInfo mShortcutInfo = null;
		if( getContainer() == NO_ID )
		{
			// Came from all apps -- make a copy
			mShortcutInfo = makeShortcut();
		}
		else
		{
			throw new IllegalStateException( "ERROR [dragInfo instanceof AppInfo,But container!= NO_ID]" );
		}
		return mLauncher.createShortcut( R.layout.application , cellLayout , mShortcutInfo );
	}
	
	//xiatian add end
	// zhangjin@2016/05/05 ADD START
	public ComponentKey toComponentKey()
	{
		return new ComponentKey( componentName , user );
	}
	
	// zhangjin@2016/05/05 ADD END
	//cheyingkun add start	//解决“常用应用显示动态时，改变日期后返回桌面，桌面重启”的问题【c_0004419】
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
	public long getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public int getUseFrequency(
			Context context )
	{
		int useFrequency = 0;
		if( mPreferences == null )
		{
			mPreferences = context.getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , Context.MODE_PRIVATE );
		}
		if( intent != null && intent.getComponent() != null )
		{
			useFrequency = mPreferences.getInt( StringUtils.concat( "FREQUENCY:" , intent.getComponent().toString() ) , 0 );
		}
		//Log.d("launcher", "intent,frequency="+intent.getComponent().toString()+","+useFrequency);
		return useFrequency;
	}
	
	public void removeUseFrequency(
			Context context )
	{
		if( mPreferences == null )
		{
			mPreferences = context.getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , Context.MODE_PRIVATE );
		}
		if( componentName != null )
		{
			mPreferences.edit().remove( StringUtils.concat( "FREQUENCY:" , intent.getComponent().toString() ) ).commit();
		}
	}
	
	public void removeHide(
			Context context )
	{
		setIconHide( context , false );
	}
	
	public void initHideIcon(
			Context context )
	{
		if( mPreferences == null )
		{
			mPreferences = context.getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , Context.MODE_PRIVATE );
		}
		if( componentName != null )
			isHideIcon = mPreferences.getBoolean( StringUtils.concat( "HIDE:" , componentName.toString() ) , false );
	}
	
	public void setIconHide(
			Context context ,
			boolean isHide )
	{
		if( mPreferences == null )
		{
			mPreferences = context.getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , Context.MODE_PRIVATE );
		}
		if( componentName != null )
			if( isHide )
			{
				mPreferences.edit().putBoolean( StringUtils.concat( "HIDE:" , componentName.toString() ) , true ).commit();
			}
			else
			{
				mPreferences.edit().remove( StringUtils.concat( "HIDE:" , componentName.toString() ) ).commit();
			}
		isHideIcon = isHide;
	}
	
	public boolean isHideIcon()
	{
		return isHideIcon;
	}
	
	//zhujieping add end
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
		//图片的释放都放到iconcache的onrecycle方法中
		IconCache iconCache = (IconCache)arg0;
		iconBitmap = iconCache.getIcon( intent );
	}
}
