package com.cooee.update;


import android.content.Context;
import android.util.Log;

import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.update.taskManager.Listener;
import com.cooee.update.taskManager.Task;
import com.cooee.update.taskManager.TaskManager;
import com.cooee.update.taskManager.TaskParam;
import com.cooee.update.taskManager.TaskResult;

import cool.sdk.Uiupdate.UiupdateHelper;


/**
 * 用户主动下载时，相关Task
 * @author zhangjin
 *
 */
public class DownloadTask extends Task
{
	
	private static final String TAG = "UpdateUi.DownloadTask";
	private Context mContext;
	/** 用于从下载线程接收消息*/
	private Listener mReceiveListener;
	private int mProgress = 0;
	
	public DownloadTask(
			Context context ,
			TaskParam param )
	{
		super( param );
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	@Override
	public void runInBack()
	{
		// TODO Auto-generated method stub		
		try
		{
			mReceiveListener = new Listener() {
				
				@Override
				public void onProgress(
						Object ... progress )
				{
					// TODO Auto-generated method stub
					DownloadTask.this.publishProgress( progress );
					Long pro = (Long)progress[0];
					mProgress = pro.intValue();
				}
				
				@Override
				public void onResult(
						TaskResult result )
				{
					// TODO Auto-generated method stub
					DownloadTask.this.publishResult( result );
				}
			};
			UiupdateHelper.getInstance( mContext ).startDownload( mReceiveListener );
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , e.toString() );
		}
	}
	
	public void stopDownload()
	{
		TaskManager.cancel( getTaskId() );
		UiupdateHelper.getInstance( mContext ).stopDownload();
	}
	
	public void pauseDownload()
	{
		TaskManager.cancel( getTaskId() );
		UiupdateHelper.getInstance( mContext ).pauseDownload();
	}
	
	public int getProgress()
	{
		return mProgress;
	}
}
