package com.cooee.phenix.data;


import android.content.Intent;
import android.graphics.Bitmap;


/**
 * Represents an item in the launcher.
 */
public class PendingInstallShortcutInfo
{
	
	Intent data;
	Intent launchIntent;
	String name;
	Bitmap icon;
	Intent.ShortcutIconResource iconResource;
	
	public PendingInstallShortcutInfo(
			Intent rawData ,
			String shortcutName ,
			Intent shortcutIntent )
	{
		data = rawData;
		name = shortcutName;
		launchIntent = shortcutIntent;
	}
	
	public Intent.ShortcutIconResource getIconResource()
	{
		return iconResource;
	}
	
	public void setIconResource(
			Intent.ShortcutIconResource iconResource )
	{
		this.iconResource = iconResource;
	}
	
	public Intent getData()
	{
		return data;
	}
	
	public void setData(
			Intent data )
	{
		this.data = data;
	}
	
	public Intent getLaunchIntent()
	{
		return launchIntent;
	}
	
	public void setLaunchIntent(
			Intent launchIntent )
	{
		this.launchIntent = launchIntent;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(
			String name )
	{
		this.name = name;
	}
	
	public Bitmap getIcon()
	{
		return icon;
	}
	
	public void setIcon(
			Bitmap icon )
	{
		this.icon = icon;
	}
}
