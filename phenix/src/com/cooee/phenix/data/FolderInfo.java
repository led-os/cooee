package com.cooee.phenix.data;


import java.util.ArrayList;

import android.content.ContentValues;
import android.content.res.Resources;
import android.view.View;

import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.CellLayout;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherProvider;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/**
 * Represents a folder containing shortcuts or apps.
 */
//添加智能分类功能 , change by shlt@2015/02/09 UPD START
//class FolderInfo extends ItemInfo
public class FolderInfo extends EnhanceItemInfo
//添加智能分类功能 , change by shlt@2015/02/09 UPD END
{
	
	/**
	 * Whether this folder has been opened
	 */
	boolean opened;
	/**
	 * The apps and shortcuts
	 */
	ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();
	ArrayList<FolderListener> listeners = new ArrayList<FolderListener>();
	int folderType = LauncherSettings.Favorites.FOLDER_TYPE_NORMAL;
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//FolderInfo()
	public FolderInfo()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
	}
	
	/**
	 * Add an app or shortcut
	 *
	 * @param item
	 */
	public void add(
			ShortcutInfo item )
	{
		if( isCanAddToContents( item ) )
		{
			contents.add( item );
			for( int i = 0 ; i < listeners.size() ; i++ )
			{
				listeners.get( i ).onAdd( item );
			}
			itemsChanged();
		}
	}
	
	public boolean isCanAddToContents(
			ShortcutInfo item )
	{
		if( item == null )
		{
			return false;
		}
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE && LauncherAppState.isAlreadyCategory( LauncherAppState.getInstance().getContext() ) )//单层模式且是智能分类的情况下
			for( ShortcutInfo info : contents )
			{
				if( info.getIntent() != null && item.getIntent() != null )
				{
					if( info.getIntent().getComponent() != null && item.getIntent().getComponent() != null )
					{
						if( info.getIntent().getComponent().toString().equals( item.getIntent().getComponent().toString() ) )//防止文件夹中出现两个相同文件夹
						{
							return false;
						}
					}
				}
			}
		return true;
	}
	
	/**
	 * Remove an app or shortcut. Does not change the DB.
	 *
	 * @param item
	 */
	public void remove(
			ShortcutInfo item )
	{
		contents.remove( item );
		for( int i = 0 ; i < listeners.size() ; i++ )
		{
			listeners.get( i ).onRemove( item );
		}
		itemsChanged();
	}
	
	@Override
	public void setTitle(
			//WangLei start //bug:0010329 智能分类生成文件夹后，打开文件夹编辑文件夹名称成功后关闭文件夹，文件夹的名称没有改变
			//CharSequence title )  //WangLei del
			String title ) //WangLei add 
	//WangLei end
	{
		this.title = title;
		for( int i = 0 ; i < listeners.size() ; i++ )
		{
			//xiatian start	//fix bug：解决“桌面上默认配置的文件夹，在没修改文件夹名称之前，切换语言后文件夹的名称没有切换为相应语言”的问题。【c_0003355】
			//			String mTitle = title;//xiatian del
			String mTitle;
			if( title.toString().contains( LauncherProvider.FOLDER_TITLE_RESOURCE_NAME_KEY ) )//zjp，智能分类文件夹名字无法更改
			{
				mTitle = getTitle();
			}
			else
			{
				mTitle = title;
			}
			//xiatian end
			listeners.get( i ).onTitleChanged( mTitle );
		}
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public void itemIconChange(
			ShortcutInfo shortcutInfo )
	{
		for( int i = 0 ; i < listeners.size() ; i++ )
		{
			listeners.get( i ).itemIconChange( shortcutInfo );
		}
	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	
	@Override
	public void onAddToDatabase(
			ContentValues values )
	{
		super.onAddToDatabase( values );
		values.put( LauncherSettings.Favorites.TITLE , title.toString() );
		//添加智能分类功能 , change by shlt@2015/02/09 UPD START
		//values.put( LauncherSettings.BaseLauncherColumns.INTENT , getIntent().toUri( 0 ) );
		//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	}
	
	//<数据库字段更新> liuhailin@2015-03-24 del begin
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	//public void setIntent(
	//		Intent intent )
	//{
	//	setOperateIntent( intent );
	//}
	//	@Override
	//	public Intent getIntent()
	//	{
	//		return getOperateIntent();
	//	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	//<数据库字段更新> liuhailin@2015-03-24 del end
	public void addListener(
			FolderListener listener )
	{
		listeners.add( listener );
	}
	
	void removeListener(
			FolderListener listener )
	{
		if( listeners.contains( listener ) )
		{
			listeners.remove( listener );
		}
	}
	
	void itemsChanged()
	{
		for( int i = 0 ; i < listeners.size() ; i++ )
		{
			listeners.get( i ).onItemsChanged();
		}
	}
	
	@Override
	public String getTitle()
	{
		// TODO Auto-generated method stub
		int folderId = getCategoryFolderId();
		if( folderId != Integer.MAX_VALUE )
		{
			if( folderId == 0 || folderId == -1 )//系统应用或更多应用
			{
				String categoryName;
				if( folderId == 0 )//系统应用
				{
					categoryName = "category_folder_system";
				}
				else
				{
					categoryName = "category_folder_moreapp";//更多应用
				}
				String name = ResourceUtils.getStringByReflectIfNecessary( categoryName );
				if( name != null )
				{
					return name;
				}
			}
			else
			{
				String name = ResourceUtils.getStringByReflectIfNecessary( StringUtils.concat( "category_folder" , folderId ) );
				if( name != null )
				{
					return name;
				}
			}
		}
		//xiatian add start	//fix bug：解决“桌面上默认配置的文件夹，在没修改文件夹名称之前，切换语言后文件夹的名称没有切换为相应语言”的问题。【c_0003355】
		if( title.toString().contains( LauncherProvider.FOLDER_TITLE_RESOURCE_NAME_KEY ) )
		{
			Resources mResources = LauncherAppState.getInstance().getContext().getResources();
			String mTitleResourceName = title.subSequence( LauncherProvider.FOLDER_TITLE_RESOURCE_NAME_KEY.length() , title.length() ).toString();
			int titleId = mResources.getIdentifier( mTitleResourceName , null , null );
			if( titleId > 0 )
			{
				title = LauncherDefaultConfig.getString( titleId );
			}
		}
		//xiatian add end
		return super.getTitle();
	}
	
	@Override
	public void unbind()
	{
		super.unbind();
		listeners.clear();
	}
	
	public interface FolderListener
	{
		
		public void onAdd(
				ShortcutInfo item );
		
		public void onRemove(
				ShortcutInfo item );
		
		public void onTitleChanged(
				CharSequence title );
		
		public void onItemsChanged();
		
		//添加智能分类功能 , change by shlt@2015/02/09 ADD START
		void itemIconChange(
				ShortcutInfo shortcutInfo );
		//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	}
	
	@Override
	public String toString()
	{
		return StringUtils.concat(
				"FolderInfo(id:" ,
				getId() ,
				"-type:" ,
				getItemType() ,
				"-container:" ,
				getContainer() ,
				"-screen:" ,
				getScreenId() ,
				"-cellX:" ,
				getCellX() ,
				"-cellY:" ,
				getCellY() ,
				"-spanX:" ,
				getSpanX() ,
				"-spanY:" ,
				getSpanY() ,
				"-dropPos:" ,
				getDropPos() ,
				")" );
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public ArrayList<ShortcutInfo> getContents()
	{
		return contents;
	}
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	;
	
	public int getFolderType()
	{
		return folderType;
	}
	
	public void setFolderType(
			int folderType )
	{
		this.folderType = folderType;
	}
	
	public boolean getOpened()
	{
		return opened;
	}
	
	public void setOpened(
			boolean opened )
	{
		this.opened = opened;
	}
	
	//xiatian add start	//整理代码：整理接口willAcceptDrop
	@Override
	public boolean willAcceptDrop()
	{
		boolean ret = false;
		ret = super.willAcceptDrop();
		if( ret == false )
		{
			if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_FOLDER )
			{
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
				{
					ret = true;
				}
				else
				{
					ArrayList<ShortcutInfo> mContents = getContents();
					if( ( mContents == null ) || ( mContents.size() == 0 ) )
					{
						ret = true;
					}
				}
			}
		}
		return ret;
	}
	//xiatian add end
	;
	
	//xiatian add start	//整理代码：整理接口creatView
	public View creatView(
			Launcher mLauncher ,
			CellLayout cellLayout ,
			IconCache mIconCache )
	{
		super.creatView( mLauncher , cellLayout , mIconCache );
		return FolderIcon.fromXml( R.layout.folder_icon , mLauncher , cellLayout , this , mIconCache );
	}
	//xiatian add end
}
