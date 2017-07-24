package com.iLoong.launcher.MList;


import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.coco.theme.themebox.MainActivity;
import com.iLoong.base.themebox.R;
import com.kpsh.sdk.KpshMsg;
import com.kpsh.sdk.KpshSdk;


public class ThemeboxMsgReceiver extends BroadcastReceiver
{
	
	//	public final static Map<String , String> map = new HashMap<String , String>() {
	//		
	//		{
	//			put( "launcherApplyThemeAction" , "com.coco.launcher.apply_theme" );
	//			put( "launcherRestartAction" , "com.coco.launcher.restart" );
	//			put( "launcherPackageName" , "com.cool.launcher" );
	//			put(
	//					"galleryPkg" ,
	//					"com.google.android.gallery3d;com.miui.gallery;com.android.gallery;com.cooliris.media;com.htc.album;com.google.android.gallery3d;com.cooliris.media.Gallery;com.sonyericsson.album;com.android.gallery3d;com.sec.android.gallery3d" );
	//			put( "disableSetWallpaperDimensions" , "TRUE" );
	//		}
	//	};
	public void bindCoolThemeActivityData(
			Intent intent ,
			int Module ,
			String strAction ,
			String ActionDescription )
	{
		Bundle bundle = new Bundle(); // 创建Bundle对象
		bundle.putString( "launcherApplyThemeAction" , "com.coco.launcher.apply_theme" );//ok
		bundle.putString( "launcherRestartAction" , "com.coco.launcher.restart" );//ok
		bundle.putString( "launcherPackageName" , "com.cool.launcher" );
		bundle.putString(
				"galleryPkg" ,
				"com.google.android.gallery3d;com.miui.gallery;com.android.gallery;com.cooliris.media;com.htc.album;com.google.android.gallery3d;com.cooliris.media.Gallery;com.sonyericsson.album;com.android.gallery3d;com.sec.android.gallery3d" );
		bundle.putBoolean( "disableSetWallpaperDimensions" , true );
		bundle.putInt( "APP_ID" , Module );
		bundle.putString( "Action" , strAction );
		bundle.putString( "ActionDescription" , ActionDescription );
		intent.putExtras( bundle ); // 把Bundle塞入Intent里面
	}
	
	public void bindUniThemeActivityData(
			Intent intent ,
			int Module ,
			String strAction ,
			String ActionDescription )
	{
		Bundle bundle = new Bundle(); // 创建Bundle对象
		//bundle.putString( "launcherAddWidgetAction" , themeConfig.launcherAddWidgetAction );//ok
		bundle.putString( "launcherApplyThemeAction" , "com.coco.launcher.apply_theme" );//ok
		bundle.putString( "launcherRestartAction" , "com.coco.launcher.restart" );//ok
		bundle.putString( "launcherPackageName" , "com.cool.launcher" );
		bundle.putString(
				"galleryPkg" ,
				"com.google.android.gallery3d;com.miui.gallery;com.android.gallery;com.cooliris.media;com.htc.album;com.google.android.gallery3d;com.cooliris.media.Gallery;com.sonyericsson.album;com.android.gallery3d;com.sec.android.gallery3d" );
		bundle.putBoolean( "disableSetWallpaperDimensions" , true );
		//bundle.putString( "launcher_authority" , iLoongApplication.LAUNCHER_AUTHORITY );
		bundle.putInt( "APP_ID" , Module );
		bundle.putString( "Action" , strAction );
		bundle.putString( "ActionDescription" , ActionDescription );
		intent.putExtras( bundle ); // 把Bundle塞入Intent里面
	}
	
	@SuppressWarnings( "deprecation" )
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		Bundle bundle = intent.getExtras();
		KpshMsg msg = KpshSdk.stringToKpshMsg( bundle );
		MELOG.v( "ME_RTFSC" , "ThemeboxMsgReceiver onReceive() action=" + intent.getAction() );
		if( intent.getAction().equals( "android.intent.action." + context.getPackageName() ) )
		{
			try
			{
				JSONObject jsonObject = new JSONObject( msg.getMsgBody() );
				if( null != jsonObject )
				{
					String NoityContent = jsonObject.getString( "NoityContent" );
					int Module = Integer.parseInt( jsonObject.getString( "Module" ) );
					String strAction = jsonObject.getString( "Action" );
					String ActionDescription = jsonObject.getString( strAction );
					MELOG.v( "ME_RTFSC" , "NoityContent:" + NoityContent + ", Module:" + Module + ", Action:" + strAction + ", ActionDescription:" + ActionDescription );
					Intent ActivtyIntent = new Intent( context , MainActivity.class );
					bindCoolThemeActivityData( ActivtyIntent , Module , strAction , ActionDescription );
					ActivtyIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
					PendingIntent contentItent = PendingIntent.getActivity( context , msg.getMsgId() , ActivtyIntent , PendingIntent.FLAG_UPDATE_CURRENT );
					Notification notification = new Notification();
					notification.icon = R.drawable.theme;
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					notification.when = System.currentTimeMillis();
					notification.tickerText = msg.getMsgTitle();
					notification.setLatestEventInfo( context , msg.getMsgTitle() , NoityContent , contentItent );
					notificationManager.notify( msg.getMsgId() , notification );
					KpshSdk.msgOperationCallback( context , msg.getMsgId() , "SHOW" );
				}
			}
			catch( Exception e )
			{
				// TODO: handle exception
				MELOG.e( "ME_RTFSC" , "CooeeMsgReceiver onReceive error:" + e.toString() );
			}
		}
	}
}
