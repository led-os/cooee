// xiatian add whole file //需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
package com.cooee.phenix.WorkspaceMenu;


import android.text.TextUtils;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;


public class WorkspaceMenuVerticalListItemInfo
{
	
	private static final String TAG = "WorkspaceMenuVerticalListItemInfo";
	String mIdKey = "";
	static final String ID_KEY_WIDGET = "小组件";
	static final String ID_KEY_WALLPAPER = "壁纸";
	static final String ID_KEY_THEME = "主题";
	static final String ID_KEY_EDIT_MODE = "编辑模式";
	static final String ID_KEY_LAUNCHER_SETTINGS = "桌面设置";
	static final String ID_KEY_SYSTEM_SETTINGS = "系统设置";
	int mIconResouceId = -1;
	int mTitleResouceId = -1;
	
	public WorkspaceMenuVerticalListItemInfo(
			String mIdKey )
	{
		if( TextUtils.isEmpty( mIdKey ) )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "mIdKey isEmpty" ) );
		}
		setIdKey( mIdKey );
	}
	
	private void setIdKey(
			String mIdKey )
	{
		this.mIdKey = mIdKey;
		int mIconResouceId = -1;
		int mTitleResouceId = -1;
		if( ID_KEY_WIDGET.equals( mIdKey ) )
		{
			mIconResouceId = R.drawable.workspace_menu_vertical_list_item_icon_widgets_selector;
			mTitleResouceId = R.string.workspace_menu_vertical_list_item_title_widget;
		}
		else if( ID_KEY_WALLPAPER.equals( mIdKey ) )
		{
			mIconResouceId = R.drawable.workspace_menu_vertical_list_item_icon_wallpaper_selector;
			mTitleResouceId = R.string.workspace_menu_vertical_list_item_title_wallpaper;
		}
		else if( ID_KEY_THEME.equals( mIdKey ) )
		{
			mIconResouceId = R.drawable.workspace_menu_vertical_list_item_icon_theme_selector;
			mTitleResouceId = R.string.workspace_menu_vertical_list_item_title_theme;
		}
		else if( ID_KEY_EDIT_MODE.equals( mIdKey ) )
		{
			mIconResouceId = R.drawable.workspace_menu_vertical_list_item_icon_edit_mode_selector;
			mTitleResouceId = R.string.workspace_menu_vertical_list_item_title_edit_mode;
		}
		else if( ID_KEY_LAUNCHER_SETTINGS.equals( mIdKey ) )
		{
			mIconResouceId = R.drawable.workspace_menu_vertical_list_item_icon_launcher_settings_selector;
			mTitleResouceId = R.string.workspace_menu_vertical_list_item_title_launcher_settings;
		}
		else if( ID_KEY_SYSTEM_SETTINGS.equals( mIdKey ) )
		{
			mIconResouceId = R.drawable.workspace_menu_vertical_list_item_icon_system_settings_selector;
			mTitleResouceId = R.string.workspace_menu_vertical_list_item_title_system_settings;
		}
		else
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "unkown mIdKey:" , mIdKey ) );
		}
		setIconResouceId( mIconResouceId );
		setTitleResouceId( mTitleResouceId );
	}
	
	public String getIdKey()
	{
		return mIdKey;
	}
	
	private void setIconResouceId(
			int mIconResouceId )
	{
		this.mIconResouceId = mIconResouceId;
	}
	
	public int getIconResouceId()
	{
		return mIconResouceId;
	}
	
	private void setTitleResouceId(
			int mTitleResouceId )
	{
		this.mTitleResouceId = mTitleResouceId;
	}
	
	public int getTitleResouceId()
	{
		return mTitleResouceId;
	}
}
