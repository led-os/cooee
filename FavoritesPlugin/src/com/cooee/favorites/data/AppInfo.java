/* Copyright (C) 2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License. */
package com.cooee.favorites.data;


import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;


public class AppInfo
{
	
	public static final int NO_ID = -1;
	private String adPlaceId;
	private String adId;
	private String adData;
	private String type;
	public static final String DEFAULT_Ad = "Default_Ad";
	public static final String KMOB_AD = "Kmob_Ad";
	/**
	 * The intent used to start the application.
	 */
	Intent intent;
	/**
	 * A bitmap version of the application icon.
	 */
	Bitmap iconBitmap;
	/**
	 * The time at which the app was first installed.
	 */
	String title;
	long id = NO_ID;
	ComponentName componentName;
	//常用应用（新功能） hp@2015/09/23 ADD START
	/**
	 * The times of the application launch.
	 */
	public long launchTimes = 0;
	
	//常用应用（新功能） hp@2015/09/23 ADD END
	public AppInfo()
	{
	}
	
	public Intent getIntent()
	{
		return intent;
	}
	
	public Bitmap getIconBitmap()
	{
		return iconBitmap;
	}
	
	public void setIconBitmap(
			Bitmap mIconBitmap )
	{
		iconBitmap = mIconBitmap;
	}
	
	/**
	 * Creates the application intent based on a component name and various launch flags.
	 * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
	 *
	 * @param className the class name of the component representing the intent
	 * @param launchFlags the launch flags
	 */
	final void setActivity(
			ComponentName className ,
			int launchFlags )
	{
		intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_LAUNCHER );
		intent.setComponent( className );
		intent.setFlags( launchFlags );
	}
	
	public String getTitle()
	{
		// TODO Auto-generated method stub
		return title;
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public ComponentName getComponentName()
	{
		return componentName;
	}
	
	public ComponentName setComponentName(
			ComponentName componentName )
	{
		this.componentName = componentName;
		this.setActivity( componentName , Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		return this.componentName;
	}
	
	public void setTitle(
			String title )
	{
		this.title = title;
	}
	
	public void setId(
			long id )
	{
		this.id = id;
	}
	
	public long getId()
	{
		return id;
	}
	
	public void SetAdId(
			String adId )
	{
		this.adId = adId;
	}
	
	public String getAdId()
	{
		return adId;
	}
	
	public void setAdData(
			String data )
	{
		this.adData = data;
	}
	
	public String getAdData()
	{
		return adData;
	}
	
	public void setAdType(
			String type )
	{
		this.type = type;
	}
	
	public String getAdType()
	{
		return type;
	}
	
	public void setAdPlaceId(
			String adPlaceId )
	{
		this.adPlaceId = adPlaceId;
	}
	
	public String getAdPlaceId()
	{
		return adPlaceId;
	}
}
