package com.cooee.phenix.data;


import android.content.pm.ActivityInfo;

import com.cooee.framework.utils.StringUtils;


/**
 * We pass this object with a drag from the customization tray
 */
public class PendingAddShortcutInfo extends PendingAddItemInfo
{
	
	ActivityInfo shortcutActivityInfo;
	
	public PendingAddShortcutInfo(
			ActivityInfo activityInfo )
	{
		shortcutActivityInfo = activityInfo;
	}
	
	@Override
	public String toString()
	{
		return StringUtils.concat( "PendingAddShortcutInfo - Shortcut:" , shortcutActivityInfo.packageName );
	}
	
	public ActivityInfo getActivityInfo()
	{
		return shortcutActivityInfo;
	}
	
	public void setActivityInfo(
			ActivityInfo shortcutActivityInfo )
	{
		this.shortcutActivityInfo = shortcutActivityInfo;
	}
}
