// gaominghui add whole file //支持通过AIDL切换主题
package com.iLoong.launcher.theme;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.theme.ThemeReceiver;


/**
 * 支持通过AIDL切换主题
 * @author gaominghui 2017年5月5日
 */
public class ThemeService extends Service
{
	
	private static final String TAG = "ThemeService";
	private ThemeBinder mBinder = null;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mBinder = new ThemeBinder();
	}
	
	@Override
	public IBinder onBind(
			Intent intent )
	{
		return mBinder;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	private void applyTheme(
			String themeConfig )
	{
		if( !TextUtils.isEmpty( themeConfig ) )
		{
			Intent intent = new Intent();
			intent.putExtra( "theme_status" , 1 );
			intent.putExtra( "theme" , themeConfig );
			//gaominghui add start //添加配置项“switch_enable_exit_overview_mode_when_apply_theme_from_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。
			intent.putExtra( "apply_theme_from_beautycenter" , true );
			//gaominghui add end //添加配置项“switch_enable_exit_overview_mode_when_apply_theme_from_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。
			Log.i( TAG , "applyTheme themeConfig = " + themeConfig );
			//gaominghui add start //美化中心应用主题，桌面会黑一下【i_0015112】
			ThemeReceiver.applyTheme( ThemeService.this , intent , false );
			//gaominghui add end //美化中心应用主题，桌面会黑一下【i_0015112】
		}
	}
	
	public class ThemeBinder extends IThemeService.Stub
	{
		
		@Override
		public void applyTheme(
				String themeConfig ) throws RemoteException
		{
			ThemeService.this.applyTheme( themeConfig );
		}
	}
}
