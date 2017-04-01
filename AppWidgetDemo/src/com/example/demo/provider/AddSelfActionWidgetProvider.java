package com.example.demo.provider;

import com.example.demo.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
/**
 * WidgetProvider自己处理 小部件界面事件
 * @author zhaolinger
 *
 */
public class AddSelfActionWidgetProvider extends AppWidgetProvider{
	
	public static final String CLICK_WIDGET_ACTION = "com.example.demo.provider.click_widget.layout";
	private static final String TAG = "AddSelfActionWidgetProvider";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
		Log.i(TAG, "——onReceive——");
		if(intent.getAction().equals(CLICK_WIDGET_ACTION)){
			//Toast.makeText(context, "点击了小部件", Toast.LENGTH_SHORT).show();
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			remoteViews.setTextViewText(R.id.wg_tv,  "no say hello~");
//			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//			ComponentName componentName = new ComponentName(context, AddSelfActionWidgetProvider.class);
//			int[] appIds = appWidgetManager.getAppWidgetIds( componentName );
//			appWidgetManager.updateAppWidget(appIds,remoteViews);
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName componentName = new ComponentName(context, AddSelfActionWidgetProvider.class);
			appWidgetManager.updateAppWidget(componentName,remoteViews);
		}
	}
	
	

	/**
	 * AppWidget实例被添加到桌面的时候触发 或者 到达指定的更新时间触发
	 * 初始界面  & 绑定远程view 
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(TAG, "——onUpdate——");
		/**
		 * PendingIntent 延迟的intent 携带自己指定的action 开启广播组件
		 */
		Intent intent = new Intent(CLICK_WIDGET_ACTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,0) ;
		/**
		 * RemoteViews 远程的view 关联widget_layout视图 对视图控件进行设置
		 */
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		remoteViews.setOnClickPendingIntent(R.id.wg_tv, pendingIntent);
		/**
		 * updateAppWidget 更新界面
		 */
		appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);
	}

	/**
	 * AppWidget实例第一次被拖拽到桌面的时候触发
	 */
	@Override
	public void onEnabled(Context context) {
		// TODO 
		super.onEnabled(context);
		Log.i(TAG, "——onEnabled——");
	}
	
	/**
	 * AppWidget实例被移除出桌面的时候触发
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO 
		super.onDeleted(context, appWidgetIds);
		Log.i(TAG, "——onDeleted——");
	}

	/**
	 * 桌面上最后一个AppWidget实例被移除出桌面的时候触发
	 */
	@Override
	public void onDisabled(Context context) {
		// TODO 
		super.onDisabled(context);
		Log.i(TAG, "——onDisabled——");
	}
	

}
