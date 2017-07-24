package com.cooee.phenix.pageIndicators;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class PageMarkerResources
{
	
	//fulijuan start	//页面指示器支持本地化
	//fulijuan del start
	//int activeId;  
	//int inactiveId;  
	//fulijuan del end
	//fulijuan add start
	Drawable activeDrawable;
	Drawable inactiveDrawable;
	
	//fulijuan add end
	//fulijuan end
		//fulijuan start	//页面指示器支持本地化
		//fulijuan del start
//	public PageMarkerResources(
//			Context context )
//	{
//		activeId = R.drawable.ic_pageindicator_normal_page_current; 
//		inactiveId = R.drawable.ic_pageindicator_normal_page_default;
	//}
	//fulijuan del end
	//fulijuan end

	
	//fulijuan start	//页面指示器支持本地化
	//fulijuan del start	
	//	public PageMarkerResources(
	//				int aId ,
	//				int iaId )
	//		{
	//			activeId = aId;
	//			inactiveId = iaId;
	//		}
	//fulijuan del end
	//fulijuan add start
	public PageMarkerResources(
			Drawable activeDrawable ,
			Drawable inactiveDrawable )
	{
		this.activeDrawable = activeDrawable;
		this.inactiveDrawable = inactiveDrawable;
	}
	//fulijuan add end
	//fulijuan end
}
