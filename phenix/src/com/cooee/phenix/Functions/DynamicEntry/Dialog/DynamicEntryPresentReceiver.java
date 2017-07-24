package com.cooee.phenix.Functions.DynamicEntry.Dialog;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;

import cool.sdk.SAManager.SAHelper;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class DynamicEntryPresentReceiver extends BroadcastReceiver
{
	
	private static int notifyID = 30141225;
	private static long lastTime = 0;
	
	@Override
	public void onReceive(
			final Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		if( LauncherAppState.getActivityInstance() == null )
		{
			return;
		}
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( judgeTime() )
				{
					final ArrayList<String> nameList = getNameList();
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( "smart" , StringUtils.concat( "dlList.size=" , nameList.size() ) );
					if( nameList.size() == 0 )
					{
						return;
					}
					final int index;
					if( nameList.size() > 1 )
					{
						index = (int)( Math.random() * 4 );
					}
					else
					{
						index = (int)( Math.random() * 3 );
					}
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( "smart" , StringUtils.concat( "judgeTime---random:" , index ) );
					if( LauncherAppState.getActivityInstance() != null )
					{
						LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								Notification.Builder mBuilder = new Notification.Builder( context );
								mBuilder.setSmallIcon( R.mipmap.ic_launcher_home );
								Notification notification = null;
								// gaominghui@2016/12/14 ADD START
								if( Build.VERSION.SDK_INT >= 16 )
								{
									mBuilder.build();
								}
								else
								{
									mBuilder.getNotification();
								}
								// gaominghui@2016/12/14 ADD END
								notification.flags |= Notification.FLAG_AUTO_CANCEL;
								DLinstallNotification dlRemoteView = new DLinstallNotification( context , nameList , index );
								notification.contentView = dlRemoteView;
								Intent intentTem = new Intent();
								intentTem.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
								Bundle bundle = new Bundle();
								bundle.putString( Constants.MSG , Constants.MSG_DL_INSTALL );
								bundle.putInt( "index" , index );
								bundle.putStringArrayList( "nameList" , nameList );
								intentTem.putExtras( bundle );
								PendingIntent contentIntent = PendingIntent.getActivity( context , notifyID , intentTem , PendingIntent.FLAG_UPDATE_CURRENT );
								notification.contentIntent = contentIntent;
								NotificationManager notificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
								notificationManager.cancel( notifyID );
								notificationManager.notify( notifyID , notification );
								lastTime = System.currentTimeMillis();
							}
						} );
					}
				}
			}
		} );
	}
	
	private boolean judgeTime()
	{
		long curTime = System.currentTimeMillis();
		//每天只出一次，所以不能在同一天
		if( Math.abs( curTime - lastTime ) > 24 * 60 * 60 * 1000 )
		{
			Date toDate = new Date( curTime );
			Calendar c = Calendar.getInstance();
			c.setTime( toDate );
			int weekday = c.get( Calendar.DAY_OF_WEEK );
			int hour = c.get( Calendar.HOUR_OF_DAY );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "smart" , StringUtils.concat( "judgeTime---weekday:" , weekday , "---hour:" , hour ) );
			if( weekday == 1 || weekday == 7 )
			{
				//周末11点后
				if( hour > 10 )
				{
					return true;
				}
			}
			else
			{
				//平时18点后
				if( hour > 17 )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private ArrayList<String> getNameList()
	{
		ArrayList<String> list = new ArrayList<String>();
		SharedPreferences preferences = LauncherAppState.getActivityInstance().getSharedPreferences( "DynamicEntry" , Context.MODE_PRIVATE );
		String content = preferences.getString( "dl_Time_info" , null );
		if( content != null )
		{
			try
			{
				JSONObject res = new JSONObject( content );
				JSONArray array = res.getJSONArray( "dl_time" );
				long maxTime = 0;
				long curTime = System.currentTimeMillis();
				for( int i = array.length() - 1 ; i > -1 ; i-- )
				{
					JSONObject item = array.getJSONObject( i );
					String pkgName = item.getString( "pkgName" );
					if( !OperateDynamicUtils.checkApkExist( LauncherAppState.getActivityInstance() , pkgName ) )
					{
						long time = item.optLong( "time" );
						if( Math.abs( time - curTime ) < 3 * 24 * 60 * 60 * 1000 )
						{
							if( time > maxTime )
							{
								maxTime = time;
								list.add( 0 , pkgName );
							}
							else
							{
								list.add( pkgName );
							}
						}
					}
				}
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	private String getTitleByPkg(
			String pkgName )
	{
		String title = null;
		dl_info info = null;
		info = DlManager.getInstance().getDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
		if( info != null )
		{
			title = (String)info.getValue( "p101" );
		}
		if( title == null )
		{
			info = SAHelper.getInstance( LauncherAppState.getActivityInstance() ).getCoolDLMgrApk().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
			if( info != null )
			{
				title = DlManager.getInstance().getWifiSAHandle().getTitleName( info );
			}
		}
		return title;
	}
	
	private class DLinstallNotification extends RemoteViews
	{
		
		public DLinstallNotification(
				Context context ,
				ArrayList<String> nameList ,
				int index )
		{
			super( context.getPackageName() , R.layout.operate_sa_download_notifaction );
			Bitmap bitmap = DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 0 ) );
			this.setImageViewBitmap( R.id.notificationImage , bitmap );
			this.setViewVisibility( R.id.notify_run , View.VISIBLE );
			String notificationTitle = null;
			if( index != 3 )
			{
				String notificationContent = null;
				if( index == 0 )
				{
					notificationTitle = StringUtils.concat(
							LauncherDefaultConfig.getString( R.string.dynamic_your_dl ) ,
							getTitleByPkg( nameList.get( 0 ) ) ,
							LauncherDefaultConfig.getString( R.string.dynamic_not_dl ) );
					notificationContent = StringUtils.concat( LauncherDefaultConfig.getString( R.string.dynamic_wa_yao ) , ">>" );
				}
				else if( index == 1 )
				{
					notificationTitle = StringUtils.concat( getTitleByPkg( nameList.get( 0 ) ) , LauncherDefaultConfig.getString( R.string.dynamic_many_days ) );
					notificationContent = StringUtils.concat( LauncherDefaultConfig.getString( R.string.dynamic_dl_uninstall ) , ">>" );
				}
				else
				{
					notificationTitle = LauncherDefaultConfig.getString( R.string.dynamic_dl_warnning );
					notificationContent = StringUtils.concat( LauncherDefaultConfig.getString( R.string.dynamic_dl_end ) , getTitleByPkg( nameList.get( 0 ) ) , ">>" );
				}
				this.setTextViewText( R.id.notificationTitle , notificationTitle );
				this.setViewVisibility( R.id.notificationContent , View.VISIBLE );
				this.setTextViewText( R.id.notificationContent , notificationContent );
				this.setTextViewText( R.id.notify_run , LauncherDefaultConfig.getString( R.string.dynmaic_install_immediately ) );
				this.setViewVisibility( R.id.download_app_list , View.GONE );
			}
			else
			{
				notificationTitle = LauncherDefaultConfig.getString( R.string.dynamic_dl_news );
				this.setTextViewText( R.id.notificationTitle , notificationTitle );
				this.setTextViewText( R.id.notify_run , LauncherDefaultConfig.getString( R.string.dynamic_try_on ) );
				if( nameList.size() < 2 )
				{
					this.setViewVisibility( R.id.download_app_list , View.GONE );
				}
				else if( nameList.size() < 3 )
				{
					this.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					this.setViewVisibility( R.id.icon_2 , View.GONE );
					this.setViewVisibility( R.id.icon_3 , View.GONE );
					this.setViewVisibility( R.id.icon_4 , View.GONE );
					this.setViewVisibility( R.id.icon_5 , View.GONE );
					this.setViewVisibility( R.id.text_icon , View.GONE );
				}
				else if( nameList.size() < 4 )
				{
					this.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					this.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 2 ) ) );
					this.setViewVisibility( R.id.icon_3 , View.GONE );
					this.setViewVisibility( R.id.icon_4 , View.GONE );
					this.setViewVisibility( R.id.icon_5 , View.GONE );
					this.setViewVisibility( R.id.text_icon , View.GONE );
				}
				else if( nameList.size() < 5 )
				{
					this.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					this.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 2 ) ) );
					this.setImageViewBitmap( R.id.icon_3 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 3 ) ) );
					this.setViewVisibility( R.id.icon_4 , View.GONE );
					this.setViewVisibility( R.id.icon_5 , View.GONE );
					this.setViewVisibility( R.id.text_icon , View.GONE );
				}
				else if( nameList.size() < 6 )
				{
					this.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					this.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 2 ) ) );
					this.setImageViewBitmap( R.id.icon_3 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 3 ) ) );
					this.setImageViewBitmap( R.id.icon_4 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 4 ) ) );
					this.setViewVisibility( R.id.icon_5 , View.GONE );
					this.setViewVisibility( R.id.text_icon , View.GONE );
				}
				else
				{
					this.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					this.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 2 ) ) );
					this.setImageViewBitmap( R.id.icon_3 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 3 ) ) );
					this.setImageViewBitmap( R.id.icon_4 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 4 ) ) );
					this.setImageViewBitmap( R.id.icon_5 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 5 ) ) );
					if( nameList.size() < 7 )
					{
						this.setViewVisibility( R.id.text_icon , View.GONE );
					}
				}
			}
			this.setViewVisibility( R.id.process , View.GONE );
			this.setViewVisibility( R.id.btn_check , View.GONE );
			this.setViewVisibility( R.id.btn_run , View.GONE );
		}
	}
}
