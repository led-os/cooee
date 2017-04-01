package com.example.demo.service;

import com.example.demo.R;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;
/**
 * 
 * @author zhaolinger
 *	调用系统的图库
 */
public class GalleryService extends Service{
	private static final String ACTION = "Intent.ACTION_PICK";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//动态注册广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION);
		this.registerReceiver(receiver, filter);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(ACTION)){
				Toast.makeText(context, "调用图库成功！", Toast.LENGTH_SHORT).show();
			}
		}
	};

}
