package com.cooee.update;


import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.cooee.phenix.data.ItemInfo;


public class UpdateIconManager
{
	
	private static final String TAG = "UpdateIconManager";
	private static UpdateIconManager mInstance = null;
	private Context mGlobalContext = null;
	private boolean isHasUpdate = false;
	
	public static UpdateIconManager getInstance()
	{
		synchronized( UpdateIconManager.class )
		{
			if( mInstance == null )
			{
				mInstance = new UpdateIconManager();
			}
		}
		return mInstance;
	}
	
	public List<ResolveInfo> getUpdateResolveInfo()
	{
		if( UpdateNotificationManager.getInstance().isShowUpdateIcon() )
		{
			PackageManager packageManager = getGlobalContext().getPackageManager();
			Intent intent = new Intent();
			intent.setClassName( getGlobalContext() , UpdateActivity.class.getName() );
			return packageManager.queryIntentActivities( intent , 0 );
		}
		return null;
	}
	
	public ComponentName getUpdateCmp()
	{
		ComponentName cmp = new ComponentName( getGlobalContext().getPackageName() , UpdateActivity.class.getName() );
		return cmp;
	}
	
	public boolean isLauncherUpdateIcon(
			ItemInfo itemInfo )
	{
		ComponentName cmp = null;
		if( itemInfo != null )
		{
			Intent mIntent = itemInfo.getIntent();
			if( mIntent != null )
			{
				cmp = mIntent.getComponent();
			}
		}
		if( cmp == null )
		{
			return false;
		}
		String packageName = cmp.getPackageName();
		String className = cmp.getClassName();
		if( packageName.equals( getGlobalContext().getPackageName() ) && className.equals( UpdateActivity.class.getName() ) )
		{
			return true;
		}
		return false;
	}
	
	public boolean isHasUpdate()
	{
		return isHasUpdate;
	}
	
	public void setHasUpdate(
			boolean hasUpdate )
	{
		this.isHasUpdate = hasUpdate;
	}
	
	private Context getGlobalContext()
	{
		if( mGlobalContext == null )
		{
			mGlobalContext = UpdateUiManager.getInstance().getGlobalContext();
		}
		return mGlobalContext;
	}
}
