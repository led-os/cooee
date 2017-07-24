package com.search.kuso;


import java.util.List;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.cooee.search.R;


public class WidgetDemoAppWidgetProviderTest extends AppWidgetProvider
{
	
	static Bitmap bmp;
	static final int MyTextViewId = 1;
	
	public void onUpdate(
			Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		//Toast.makeText(context, "onUpdate!", Toast.LENGTH_SHORT).show();
		Log.e( "ZZY" , "onUpdate" );
		for( int appWidgetId : appWidgetIds )
		{
			RemoteViews views = new RemoteViews( context.getPackageName() , R.layout.kuso_widget_edit_box_bg );
			/*int width = 500;
			int height = 500;
			
			bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas cv = new Canvas(bmp);
			Paint p = new Paint();
			p.setColor(0xFF000000);
			cv.drawRect(0, 0, width, height, p);*/
			//views.setImageViewBitmap(R.id.imageView1, bmp);
			//views.setImageViewUri(R.id.imageView1, Uri.parse("/sdcard/1.png"));
			views.setOnClickPendingIntent( R.id.kuso_search_edit , getLaunchPendingIntent( context , MyTextViewId ) );
			appWidgetManager.updateAppWidget( appWidgetId , views );
		}
	}
	
	@Override
	public void onDeleted(
			Context context ,
			int[] appWidgetIds )
	{
		// TODO Auto-generated method stub
		super.onDeleted( context , appWidgetIds );
		Log.e( "ZZY" , "onDeleted" );
		//Toast.makeText(context, "onDeleted!", Toast.LENGTH_SHORT).show();
	}
	
	//��ӵ�һ�� onEnabled  onReceive	onUpdate onReceive	onUpdate onReceive onDeleted onReceive onDisabled onReceive
	@Override
	public void onEnabled(
			Context context )
	{
		// TODO Auto-generated method stub
		super.onEnabled( context );
		Log.e( "ZZY" , "onEnabled" );
		//Toast.makeText(context, "onEnabled!", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onDisabled(
			Context context )
	{
		// TODO Auto-generated method stub
		super.onDisabled( context );
		Log.e( "ZZY" , "onDisabled" );
		//Toast.makeText(context, "onDisabled!", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		super.onReceive( context , intent );
		Log.e( "ZZY" , "onReceive" );
		//Toast.makeText(context, "onReceive!", Toast.LENGTH_SHORT).show();
		try
		{
			if( intent.hasCategory( Intent.CATEGORY_ALTERNATIVE ) )
			{
				Uri data = intent.getData();
				int buttonId = Integer.parseInt( data.getSchemeSpecificPart() );
				if( buttonId == MyTextViewId )
				{
					Log.e( "ZZY" , "clickMyTextView" );
					//onClickItem(icons, datas.itemList, buttonId);
					//Update();
					//cleanMemory( context );
					Intent intent2 = new Intent();
					intent2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					intent2.setClass( context , SearchT9Main.class );
					//intent2.putExtra( "url:" , ""+"酷蛙斗地主" );
					context.startActivity( intent2 );
				}
				//else if (buttonId < buttonRefresh) 
				{
					//onClickItem(panels, datas.panelList, buttonId
					//		- panelStartId);
					//Update();
				}
				//else if (buttonId == buttonRefresh) 
				{
					//switchResourceOutside(context, true);
				}
			}
		}
		catch( Exception e )
		{
			Log.e( "ZZY" , "onReceive:refresh:error:" + e.getMessage() + e.toString() );
		}
	}
	
	private static PendingIntent getLaunchPendingIntent(
			Context context ,
			int buttonId )
	{
		Intent launchIntent = new Intent();
		launchIntent.setClass( context , WidgetDemoAppWidgetProviderTest.class );
		launchIntent.addCategory( Intent.CATEGORY_ALTERNATIVE );
		launchIntent.setData( Uri.parse( "custom:" + buttonId ) );
		PendingIntent pi = PendingIntent.getBroadcast( context , 0 , launchIntent , 0 );
		return pi;
	}
	
	public static void cleanMemory(
			Context context )
	{
		ActivityManager activityManger = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		if( list != null )
		{
			for( ActivityManager.RunningAppProcessInfo apinfo : list )
			{
				System.out.println( "pid            " + apinfo.pid );
				System.out.println( "processName              " + apinfo.processName );
				System.out.println( "importance            " + apinfo.importance );
				String[] pkgList = apinfo.pkgList;
				if( apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE )
				{
					// Process.killProcess(apinfo.pid);
					for( int j = 0 ; j < pkgList.length ; j++ )
					{
						//2.2以上是过时的,请用killBackgroundProcesses代替
						if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO )
						{
							activityManger.restartPackage( pkgList[j] );
						}
						else
						{
							activityManger.killBackgroundProcesses( pkgList[j] );
						}
					}
				}
			}
		}
	}
}
