package com.cooee.phenix.editmode.item;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.cooee.phenix.editmode.interfaces.IEditControlCallBack;
import com.cooee.theme.ThemeManager;


public class EditModelThemeItem extends EditModelItem
{
	
	private Intent mThemeIntent = null;
	
	public Intent getThemeIntent()
	{
		return mThemeIntent;
	}
	
	public void setThemeIntent(
			Intent themeIntent )
	{
		this.mThemeIntent = themeIntent;
	}
	
	@Override
	public void onItemClick(
			IEditControlCallBack callback ,
			Context context )
	{
		// TODO Auto-generated method stub
		ComponentName componentName = mThemeIntent.getComponent();
		if( componentName != null && ThemeManager.BEAUTY_CENTER_PACKAGE_NAME.equals( componentName.getPackageName() ) )
		{
			if( callback != null )
			{
				callback.enterOrDownloadBeautyCenter( ThemeManager.BEAUTY_CENTER_TAB_THEME_CLASS_NAME );
			}
		}
		else
		{
			context.sendBroadcast( mThemeIntent );
		}
	}
}
