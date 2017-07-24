package com.cooee.framework.function.DynamicEntry.DLManager;


import java.util.ArrayList;

import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class DlCallback implements CoolDLCallback
{
	
	private ArrayList<DlObserverInterface> observers = new ArrayList<DlObserverInterface>();
	private DownloadingItem dlItem;
	private static final String TAG = "DlCallback";
	
	public DlCallback(
			DownloadingItem dlItem )
	{
		this.dlItem = dlItem;
		this.dlItem.callback = this;
	}
	
	public void Attach(
			DlObserverInterface o )
	{
		observers.add( o );
	}
	
	public void Detach(
			DlObserverInterface o )
	{
		observers.remove( o );
	}
	
	public void NotifyUpdate()
	{
		int size = observers.size();
		for( int i = 0 ; i < size ; i++ )
		{
			observers.get( i ).update( dlItem );
		}
	}
	
	@Override
	public void onDoing(
			CoolDLResType arg0 ,
			String pkgName ,
			dl_info dlInfo )
	{
		// TODO Auto-generated method stub
		final dl_info dlFinalInfo = dlInfo;
		Runnable runnable = new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				dlItem.state = Constants.DL_STATUS_ING;
				dlItem.notifyID = dlFinalInfo.getID();
				dlItem.progress = (int)( (float)dlFinalInfo.getCurBytes() / dlFinalInfo.getTotalBytes() * 100 );
				if( dlItem.progress > 98 )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "OnDoing progress:" , dlItem.progress , "-title:" , dlItem.title ) );
				}
				NotifyUpdate();
			}
		};
		BaseAppState.getActivityInstance().runOnUiThread( runnable );
	}
	
	@Override
	public void onFail(
			CoolDLResType arg0 ,
			String pkgName ,
			dl_info dlInfo )
	{
		Runnable runnable = new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				dlItem.state = Constants.DL_STATUS_FAIL;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , StringUtils.concat( "onFail title:" , dlItem.title , "-dlItem.progress:" , dlItem.progress ) );
				NotifyUpdate();
			}
		};
		BaseAppState.getActivityInstance().runOnUiThread( runnable );
	}
	
	@Override
	public void onSuccess(
			CoolDLResType arg0 ,
			String pkgName ,
			dl_info dlInfo )
	{
		// 这里因为有两个dlCallback在竞争，有肯能一个Callback已经发送了
		// success,不需要重复发送，避免在下载完成后重复提示安装问题
		Runnable runnable = new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( dlItem.state != Constants.DL_STATUS_SUCCESS )
				{
					dlItem.state = Constants.DL_STATUS_SUCCESS;
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "onSuccess  title:" , dlItem.title , "-dlItem.progress:" , dlItem.progress ) );
					NotifyUpdate();
					OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
					client.upateDownloadItemState( dlItem.packageName , Constants.DL_STATUS_SUCCESS );
				}
				else
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "DlManager" , StringUtils.concat( "onSuccess  DlCallback title:" , dlItem.title , "-dlItem.progress:" , dlItem.progress ) );
				}
			}
		};
		BaseAppState.getActivityInstance().runOnUiThread( runnable );
		// TODO Auto-generated method stub
	}
}
