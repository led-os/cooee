package com.cooee.widget.TranWeatherClock;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.cooee.weather.download.DownloadHelper;
import com.cooee.weather.download.DownloadHelper.DownloadListener;
import com.cooee.widget.ClearWeatherClock.R;


public class DownloadActivity extends Activity implements OnClickListener
{
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.weather_download_layout );
		getLauncherIcon();
		this.findViewById( R.id.download_layout_left_button ).setOnClickListener( this );
		this.findViewById( R.id.download_layout_right_button ).setOnClickListener( this );
	}
	
	private void getLauncherIcon()
	{
		final Intent intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_HOME );
		final ResolveInfo res = this.getPackageManager().resolveActivity( intent , 0 );
		if( res == null )
		{
			this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.client_icon );
		}
		else
		{
			if( res.activityInfo == null )
			{
				this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.client_icon );
			}
			else
			{
				String packageName = res.activityInfo.packageName;
				if( packageName == null || "".equals( packageName ) || "null".equals( packageName ) )
				{
					this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.client_icon );
				}
				else
				{
					String coolLauncherPackageNameString = "com.cool";
					String uniLauncherPackageNameString = "com.cooee";
					if( packageName.indexOf( coolLauncherPackageNameString ) > -1 || packageName.indexOf( uniLauncherPackageNameString ) > -1 )
					{
						Drawable icon = getAppIconDrawableByPkgName( this , packageName );
						if( icon == null )
						{
							this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.client_icon );
						}
						else
						{
							this.findViewById( R.id.app_ic ).setBackgroundDrawable( icon );
						}
					}
					else
					{
						this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.client_icon );
					}
				}
			}
		}
	}
	
	public Drawable getAppIconDrawableByPkgName(
			Context context ,
			String pkgName )
	{
		Drawable icon = null;
		PackageManager packageManager = context.getPackageManager();
		try
		{
			PackageInfo info = context.getPackageManager().getPackageInfo( pkgName , 0 );
			ApplicationInfo applicationInfo = info.applicationInfo;
			icon = applicationInfo.loadIcon( packageManager );
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return icon;
	}
	
	@Override
	public void onClick(
			View v )
	{
		switch( v.getId() )
		{
			case R.id.download_layout_right_button:
				String packageName = AppConfig.getInstance( DownloadActivity.this.getApplicationContext() ).getDefaultPackage();
				Log.i( "test" , "WeatherProvider.packageName = " + packageName );
				DownloadHelper.download( DownloadActivity.this.getApplicationContext() , new DownloadListener() {
					
					@Override
					public void setProxy(
							Object obj )
					{
						// TODO Auto-generated method stub
						System.out.println( "DownloadListener , setProxy()" );
					}
					
					@Override
					public void onDownloadSuccess()
					{
						// TODO Auto-generated method stub
						System.out.println( "DownloadListener , onDownloadSuccess()" );
						//					isStartDownLoadClient = false;
					}
					
					@Override
					public void onDownloadFail()
					{
						// TODO Auto-generated method stub
						System.out.println( "DownloadListener , onDownloadFail()" );
						//					isStartDownLoadClient = false;
					}
					
					@Override
					public void onDownloadProgress(
							int progress )
					{
						// TODO Auto-generated method stub
						System.out.println( "DownloadListener , onDownloadProgress() : " + progress );
					}
					
					@Override
					public void onInstallSuccess(
							String packageName )
					{
						final PackageManager pm = DownloadActivity.this.getPackageManager();
						Intent intent = pm.getLaunchIntentForPackage( WeatherProvider.defaultPackage );
						if( intent != null )
						{
							DownloadActivity.this.startActivity( intent );
						}
						WeatherProvider.hasClient = true;
					}
				} , "天气客户端" , packageName , true , true );
			case R.id.download_layout_left_button:
			default:
				DownloadActivity.this.finish();
				break;
		}
	}
}
