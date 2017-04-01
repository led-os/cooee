package com.example.demo.provider;

import com.example.demo.R;
import com.example.demo.service.GalleryService;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * WidgetProvider内部启动service处理 小部件界面事件
 * @author zhaolinger
 *
 */
public class StartServiceWidgetProvider extends AppWidgetProvider{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		//初始化小部件界面
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_layout);
		remoteViews.setTextViewText(R.id.wg_tv, "点击进入系统图库");
		//设置小部件点击事件
		Intent intent = new Intent("Intent.ACTION_PICK");
		intent.setType("image/*");
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		remoteViews.setOnClickPendingIntent(R.id.wg_tv, pendingIntent);
		//更新界面
		appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);
		//context.startService(new Intent(context,GalleryService.class));
		
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
	}

	
	
}
