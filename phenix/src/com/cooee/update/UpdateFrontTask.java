package com.cooee.update;


import android.content.Context;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.update.taskManager.Task;
import com.cooee.update.taskManager.TaskParam;
import com.cooee.update.taskManager.TaskResult;

import cool.sdk.Uiupdate.UiupdateHelper;


/**
 * 用户主动更新时，获取更新Task
 * @author zhangjin
 *
 */
public class UpdateFrontTask extends Task
{
	
	private static final String TAG = "UpdateUi.UpdateFrontTask";
	private Context mContext;
	
	public UpdateFrontTask(
			Context context ,
			TaskParam param )
	{
		super( param );
		mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * -1:已经是最新版本，0:未下载，1:已经下载好
	 * @see com.iLoong.launcher.update.taskManager.Task#runInBack()
	 */
	@Override
	public void runInBack()
	{
		// TODO Auto-generated method stub
		int ret = -1;
		TaskResult result = new TaskResult();
		try
		{
			ret = UiupdateHelper.getInstance( mContext ).doUiupdateFront();
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( TAG , StringUtils.concat( "runInBack " , e.toString() ) );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "runInBack  ret " , ret ) );
		result.mCode = ret;
		publishResult( result );
	}
}
