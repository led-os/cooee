package com.coco.theme.themebox.service;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.coco.download.Assets;
import com.coco.theme.themebox.util.Log;
import com.iLoong.launcher.theme.IThemeService;


public class ThemesDB
{
	
	//public static String LAUNCHER_PACKAGENAME = "com.cool.launcher";
	public static String LAUNCHER_UNI_PACKAGENAME = "com.cooee.unilauncher";
	public static String LAUNCHER_PACKAGENAME = "com.cooeeui.turbolauncher";
	public static String ACTION_LAUNCHER_RESTART = "com.coco.launcher.restart";
	public static String ACTION_LAUNCHER_APPLY_THEME = "com.coco.launcher.apply_theme";
	public static String default_theme_package_name = null;
	public static final String ACTION_LAUNCHER_CLICK_THEME = "com.cooee.launcher.click_theme";
	Context mContext;
	
	public ThemesDB(
			Context context )
	{
		mContext = context;
		try
		{
			Intent mintent = new Intent();
			mintent.setAction( "com.iLoong.launcher.theme.IThemeService" );
			mintent.setPackage( LAUNCHER_PACKAGENAME );
			mContext.bindService( mintent , mconn , Context.BIND_AUTO_CREATE );
		}
		catch( Exception e )
		{
			Log.e( "com.cooee.theme" , "ThemesDB bindService e " + e );
		}
	}
	
	public ThemeConfig getTheme()
	{
		ThemeConfig themeconf = new ThemeConfig();
		String curTheme = Assets.getTheme( mContext , "theme" );
		if( curTheme == null || curTheme.trim().length() == 0 )
		{
			curTheme = ThemesDB.default_theme_package_name;
		}
		themeconf.theme = curTheme;
		return themeconf;
	}
	
	public void SaveThemes(
			ThemeConfig themeconf )
	{
		Intent intent = new Intent( ThemesDB.ACTION_LAUNCHER_APPLY_THEME );
		intent.putExtra( "theme_status" , 1 );
		intent.putExtra( "theme" , themeconf.theme );
		mContext.sendBroadcast( intent );
		Log.v( "com.cooee.theme" , "save theme 1:" + themeconf.theme );
	}
	
	public void SaveThemes(
			String themeconf )
	{
		try
		{
			if( mBinder != null )
			{
				Log.v( "com.cooee.theme" , "aidl apply Theme!!!" + themeconf );
				mBinder.applyTheme( themeconf );
			}
			else
			{
				sendClickThemeBroadCast( themeconf );
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v( "com.cooee.theme" , "aidl e = " + e );
			sendClickThemeBroadCast( themeconf );
		}
		if( mBinder != null )
		{
			mContext.unbindService( mconn );
			Log.v( "com.cooee.theme" , "aidl unbindService!!!" );
			mBinder = null;
		}
	}
	
	/**
	 *应用主题时发送action = "com.cooee.launcher.click_theme"的广播，桌面回去收这个广播换主题
	 * @param themeconf
	 * @author gaominghui 2017年5月4日
	 */
	private void sendClickThemeBroadCast(
			String themeconf )
	{
		// @gaominghui2016/01/08 ADD START 为了防止不在我们的桌面启动美化中心仍可正常应用主题，
		//应用主题的逻辑就是，如果当前桌面不是我们桌面，发送该广播，桌面会把我们的桌面启来并应用主题
		//Intent intent = new Intent( ThemesDB.ACTION_LAUNCHER_APPLY_THEME );
		Intent intent = new Intent( ThemesDB.ACTION_LAUNCHER_CLICK_THEME );
		intent.putExtra( "selected_launcher" , LAUNCHER_PACKAGENAME );
		intent.putExtra( "theme_pkg_name" , themeconf );
		// @gaominghui2016/01/08 ADD END
		//gaominghui add start //配合phenix桌面需求修改，美化中心应用主题发送广播给桌面告诉桌面是来自美化中心应用主题【c_0004693】
		intent.putExtra( "apply_theme_from_beautycenter" , true );
		//gaominghui add end //配合phenix桌面需求修改，美化中心应用主题发送广播给桌面告诉桌面是来自美化中心应用主题【c_0004693】
		intent.putExtra( "theme_status" , 1 );
		intent.putExtra( "theme" , themeconf );
		mContext.sendBroadcast( intent );
		Log.v( "com.cooee.theme" , "save theme 2:" + themeconf );
	}
	
	private static IThemeService mBinder = null;
	private ServiceConnection mconn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(
				ComponentName name )
		{
			// TODO Auto-generated method stub
			mBinder = null;
		}
		
		@Override
		public void onServiceConnected(
				ComponentName name ,
				IBinder service )
		{
			// TODO Auto-generated method stub
			if( mBinder == null )
			{
				mBinder = IThemeService.Stub.asInterface( service );
				Log.i( "com.cooee.theme" , "aidl onServiceConnected !!!" );
			}
		}
	};
}
