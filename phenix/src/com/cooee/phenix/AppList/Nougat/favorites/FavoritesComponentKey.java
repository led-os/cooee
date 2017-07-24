package com.cooee.phenix.AppList.Nougat.favorites;


import android.content.ComponentName;

import com.cooee.framework.utils.StringUtils;


public class FavoritesComponentKey
{
	
	public final ComponentName componentName;
	public int launchTimes = 0;
	
	public FavoritesComponentKey(
			ComponentName componentName )
	{
		this.componentName = componentName;
	}
	
	public FavoritesComponentKey(
			String comp )
	{
		this.componentName = ComponentName.unflattenFromString( comp );
	}

	public String toSaveString()
	{
		return StringUtils.concat( componentName.getPackageName() , '/' , componentName.getClassName() , ":" , launchTimes );
	}
}
