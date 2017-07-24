// xiatian add whole file //需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
package com.cooee.phenix.WorkspaceMenu;


import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class WorkspaceMenuVerticalListContent extends ListView implements OnItemClickListener
{
	
	private static final String TAG = "WorkspaceMenuVerticalListContent";
	private Context mContext = null;
	private ArrayList<WorkspaceMenuVerticalListItemInfo> mItemInfoList = new ArrayList<WorkspaceMenuVerticalListItemInfo>();
	private WorkspaceMenuVerticalListAdapter mListItemsAdapter = null;
	private Launcher mLauncher;
	
	public WorkspaceMenuVerticalListContent(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		mContext = context;
	}
	
	@Override
	public void onFinishInflate()
	{
		super.onFinishInflate();
		setupViews();
		loadAndBindListViewData();
	}
	
	private void setupViews()
	{
		setChoiceMode( ListView.CHOICE_MODE_SINGLE );
		setOnItemClickListener( this );
	}
	
	private void loadAndBindListViewData()
	{
		loadListViewData();
		bindListViewData();
	}
	
	private void loadListViewData()
	{
		if( mItemInfoList.size() == 0 )
		{
			String[] mListItemsOrderKey = LauncherDefaultConfig.getStringArray( R.array.config_workspace_menu_vertical_list_order );
			for( String mItemOrderKey : mListItemsOrderKey )
			{
				mItemInfoList.add( new WorkspaceMenuVerticalListItemInfo( mItemOrderKey ) );
			}
		}
	}
	
	private void bindListViewData()
	{
		mListItemsAdapter = new WorkspaceMenuVerticalListAdapter( mContext , mItemInfoList );
		setAdapter( mListItemsAdapter );
	}
	
	@Override
	public void onItemClick(
			AdapterView<?> parent ,
			View view ,
			int position ,
			long id )
	{
		WorkspaceMenuVerticalList mParent = (WorkspaceMenuVerticalList)getParent();
		if( mParent.isAnimationRuning() )
		{
			return;
		}
		WorkspaceMenuVerticalListItemInfo mListItemInfo = (WorkspaceMenuVerticalListItemInfo)view.getTag();
		String mIdKey = mListItemInfo.getIdKey();
		if( WorkspaceMenuVerticalListItemInfo.ID_KEY_WIDGET.equals( mIdKey ) )
		{
			mLauncher.enterWidgets( view );
		}
		else if( WorkspaceMenuVerticalListItemInfo.ID_KEY_WALLPAPER.equals( mIdKey ) )
		{
			mLauncher.enterBeautyCenterTabWallpaper( view );
		}
		else if( WorkspaceMenuVerticalListItemInfo.ID_KEY_THEME.equals( mIdKey ) )
		{
			mLauncher.enterBeautyCenterTabTheme( view );
		}
		else if( WorkspaceMenuVerticalListItemInfo.ID_KEY_EDIT_MODE.equals( mIdKey ) )
		{
			mLauncher.enterEditModeFromWorkspaceMenu();
		}
		else if( WorkspaceMenuVerticalListItemInfo.ID_KEY_LAUNCHER_SETTINGS.equals( mIdKey ) )
		{
			mLauncher.enterLauncherSettings( view );
		}
		else if( WorkspaceMenuVerticalListItemInfo.ID_KEY_SYSTEM_SETTINGS.equals( mIdKey ) )
		{
			mLauncher.enterSystemSettings( view );
		}
		else
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "onItemClick - unkown mIdKey:" , mIdKey ) );
		}
		mParent.hideNoAnim();
	}
	
	public void setLauncher(
			Launcher mLauncher )
	{
		this.mLauncher = mLauncher;
	}
}
