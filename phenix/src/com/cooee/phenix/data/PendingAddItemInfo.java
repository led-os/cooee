package com.cooee.phenix.data;


import android.content.ComponentName;


/**
 * We pass this object with a drag from the customization tray
 */
public class PendingAddItemInfo extends ItemInfo
{
	
	/**
	 * The component that will be created.
	 */
	ComponentName componentName;
	
	public ComponentName getComponentName()
	{
		return componentName;
	}
	
	public void setComponentName(
			ComponentName componentName )
	{
		this.componentName = componentName;
	}
}
