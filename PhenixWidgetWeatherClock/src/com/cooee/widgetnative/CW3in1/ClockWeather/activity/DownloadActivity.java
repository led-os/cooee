package com.cooee.widgetnative.CW3in1.ClockWeather.activity;


import com.cooee.widgetnative.CW3in1.R;
import com.ha.hb.BaseDownloadHelper;
import com.ha.hb.BaseDownloadHelper.DownloadListener;

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


public class DownloadActivity extends Activity implements OnClickListener
{
	
	static
	{
		BaseDownloadHelper.initResId(
				R.layout.download_notification_layout ,
				R.id.download_notification_title_id ,
				R.id.download_notification_percent_tip_id ,
				R.id.download_notification_progress_bar_id ,
				R.string.download_toast_tip_insert_SD ,
				R.string.download_toast_tip_internet_err ,
				R.string.download_toast_tip_downloading ,
				R.string.download_notification_tip_downloading ,
				R.string.download_notification_tip_download_fail ,
				R.string.download_notification_tip_download_finish ,
				R.drawable.download_notification_icon );
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.weather_download_activity_layout );
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
			this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.weather_download_icon );
		}
		else
		{
			if( res.activityInfo == null )
			{
				this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.weather_download_icon );
			}
			else
			{
				String packageName = res.activityInfo.packageName;
				if( packageName == null || "".equals( packageName ) || "null".equals( packageName ) )
				{
					this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.weather_download_icon );
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
							this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.weather_download_icon );
						}
						else
						{
							this.findViewById( R.id.app_ic ).setBackgroundDrawable( icon );
						}
					}
					else
					{
						this.findViewById( R.id.app_ic ).setBackgroundResource( R.drawable.weather_download_icon );
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
				String packageName = getResources().getString( R.string.weather_default_packageName );
				Log.i( "test" , "WeatherProvider.packageName = " + packageName );
				BaseDownloadHelper.download( DownloadActivity.this.getApplicationContext() , DownloadActivity.this.getApplicationContext() , new DownloadListener() {
					
					@Override
					public void setProxy(
							Object obj )
					{
						// TODO Auto-generated method stub
						Log.i( "test" , "DownloadListener , setProxy()" );
					}
					
					@Override
					public void onDownloadSuccess()
					{
						// TODO Auto-generated method stub
						Log.i( "test" , "DownloadListener , onDownloadSuccess()" );
					}
					
					@Override
					public void onDownloadFail()
					{
						// TODO Auto-generated method stub
						Log.i( "test" , "DownloadListener , onDownloadFail()" );
						//						Toast.makeText( DownloadActivity.this , R.string.notify_download_fail , Toast.LENGTH_SHORT ).show();
						//						DownloadActivity.this.finish();
					}
					
					@Override
					public void onDownloadProgress(
							int progress )
					{
						// TODO Auto-generated method stub
						Log.i( "test" , "DownloadListener , onDownloadProgress() : " + progress );
					}
					
					@Override
					public void onInstallSuccess(
							String packageName )
					{
						Log.i( "test" , "DownloadListener , onInstallSuccess() packageName : " + packageName );
						//						final PackageManager pm = DownloadActivity.this.getPackageManager();
						//						Intent intent = pm.getLaunchIntentForPackage( packageName );
						//						if( intent != null )
						//						{
						//							DownloadActivity.this.startActivity( intent );
						//						}
					}
					
					@Override
					public void showMessage(
							String arg0 )
					{
						// TODO Auto-generated method stub
					}
				} , "天气客户端" , packageName , true );
			case R.id.download_layout_left_button:
			default:
				DownloadActivity.this.finish();
				break;
		}
	}
}
