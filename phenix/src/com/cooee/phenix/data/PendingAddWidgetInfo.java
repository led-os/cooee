package com.cooee.phenix.data;


import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.os.Bundle;
import android.os.Parcelable;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherSettings;


/**
 * We pass this object with a drag from the customization tray
 */
public class PendingAddWidgetInfo extends PendingAddItemInfo
{
	
	int minWidth;
	int minHeight;
	int minResizeWidth;
	int minResizeHeight;
	int previewImage;
	int icon;
	AppWidgetProviderInfo info;
	AppWidgetHostView boundWidget;
	Bundle bindOptions = null;
	// Any configuration data that we want to pass to a configuration activity when
	// starting up a widget
	String mimeType;
	Parcelable configurationData;
	
	public PendingAddWidgetInfo(
			AppWidgetProviderInfo i ,
			String dataMimeType ,
			Parcelable data )
	{
		itemType = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
		this.info = i;
		componentName = i.provider;
		minWidth = i.minWidth;
		minHeight = i.minHeight;
		minResizeWidth = i.minResizeWidth;
		minResizeHeight = i.minResizeHeight;
		previewImage = i.previewImage;
		icon = i.icon;
		if( dataMimeType != null && data != null )
		{
			mimeType = dataMimeType;
			configurationData = data;
		}
	}
	
	// Copy constructor
	public PendingAddWidgetInfo(
			PendingAddWidgetInfo copy )
	{
		minWidth = copy.getMinWidth();
		minHeight = copy.getMinHeight();
		minResizeWidth = copy.getMinResizeWidth();
		minResizeHeight = copy.getMinResizeHeight();
		previewImage = copy.getPreviewImage();
		icon = copy.icon;
		info = copy.info;
		boundWidget = copy.getAppWidgetHostView();
		mimeType = copy.mimeType;
		configurationData = copy.configurationData;
		componentName = copy.getComponentName();
		itemType = copy.getItemType();
		spanX = copy.getSpanX();
		spanY = copy.getSpanY();
		minSpanX = copy.getMinSpanX();
		minSpanY = copy.getMinSpanY();
		bindOptions = copy.getBindOptions() == null ? null : (Bundle)copy.getBindOptions().clone();
	}
	
	@Override
	public String toString()
	{
		return StringUtils.concat( "PendingAddWidgetInfo - Widget:" , componentName.toString() );
	}
	
	public int getMinWidth()
	{
		return minWidth;
	}
	
	public void setMinWidth(
			int minWidth )
	{
		this.minWidth = minWidth;
	}
	
	public int getMinHeight()
	{
		return minHeight;
	}
	
	public void setMinHeight(
			int minHeight )
	{
		this.minHeight = minHeight;
	}
	
	public int getMinResizeWidth()
	{
		return minResizeWidth;
	}
	
	public void setMinResizeWidth(
			int minResizeWidth )
	{
		this.minResizeWidth = minResizeWidth;
	}
	
	public int getMinResizeHeight()
	{
		return minResizeHeight;
	}
	
	public void setMinResizeHeight(
			int minResizeHeight )
	{
		this.minResizeHeight = minResizeHeight;
	}
	
	public int getPreviewImage()
	{
		return previewImage;
	}
	
	public void setPreviewImage(
			int previewImage )
	{
		this.previewImage = previewImage;
	}
	
	public int getIcon()
	{
		return icon;
	}
	
	public void setIcon(
			int icon )
	{
		this.icon = icon;
	}
	
	public AppWidgetProviderInfo getAppWidgetProviderInfo()
	{
		return info;
	}
	
	public void setAppWidgetProviderInfo(
			AppWidgetProviderInfo info )
	{
		this.info = info;
	}
	
	public AppWidgetHostView getAppWidgetHostView()
	{
		return boundWidget;
	}
	
	public void setAppWidgetHostView(
			AppWidgetHostView boundWidget )
	{
		this.boundWidget = boundWidget;
	}
	
	public Bundle getBindOptions()
	{
		return bindOptions;
	}
	
	public void setBindOptions(
			Bundle bindOptions )
	{
		this.bindOptions = bindOptions;
	}
	
	public String getMimeType()
	{
		return mimeType;
	}
	
	public void setMimeType(
			String mimeType )
	{
		this.mimeType = mimeType;
	}
	
	public Parcelable getConfigurationData()
	{
		return configurationData;
	}
	
	public void setConfigurationData(
			Parcelable configurationData )
	{
		this.configurationData = configurationData;
	}
}
