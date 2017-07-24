package com.cooee.phenix.data;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.CellLayout;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherModel;
import com.cooee.phenix.LauncherProvider;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.compat.UserHandleCompat;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/**
 * Represents an item in the launcher.
 */
//添加智能分类功能 , change by shlt@2015/02/09 UPD START
//class ItemInfo
public class ItemInfo
//添加智能分类功能 , change by shlt@2015/02/09 UPD END
{
	
	public static final int NO_ID = -1;
	/**
	 * The id in the settings database for this item
	 */
	long id = NO_ID;
	/**
	 * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_FOLDER}, or
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
	 */
	int itemType;
	/**
	 * The id of the container that holds this item. For the desktop, this will be 
	 * {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
	 * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
	 * it will be the id of the folder.
	 */
	long container = NO_ID;
	/**
	 * Iindicates the screen in which the shortcut appears.
	 */
	long screenId = -1;
	/**
	 * Indicates the X position of the associated cell.
	 */
	int cellX = -1;
	/**
	 * Indicates the Y position of the associated cell.
	 */
	int cellY = -1;
	/**
	 * Indicates the X cell span.
	 */
	int spanX = 1;
	/**
	 * Indicates the Y cell span.
	 */
	int spanY = 1;
	/**
	 * Indicates the minimum X cell span.
	 */
	int minSpanX = 1;
	/**
	 * Indicates the minimum Y cell span.
	 */
	int minSpanY = 1;
	/**
	 * Indicates that this item needs to be updated in the db
	 */
	boolean requiresDbUpdate = false;
	/**
	 * Title of the item
	 */
	CharSequence title;
	//<数据库字段更新> liuhailin@2015-03-23 add begin
	int defaultWorkspaceItemType;
	//<数据库字段更新> liuhailin@2015-03-23 add end
	/**
	 * The position of the item in a drag-and-drop operation.
	 */
	int[] dropPos = null;
	// zhangjin@2016/05/05 ADD START
	public UserHandleCompat user;
	// zhangjin@2016/05/05 ADD END
	//cheyingkun add start	//TCardMountT9SearchError(T卡挂载,灰色图标状态下,使用T9搜索搜到挂载的应用,点击后桌面异常终止)
	/**灰化的bitmap*/
	private Bitmap mIconUnavailable;
	/**该快捷方式是否可用true 可用,false 不可用(不可用显示灰化bitmap) 默认可用(T卡的app在T卡拔出时变为不可用)*/
	private boolean available = true;
	
	public Bitmap getIconUnavailable()
	{
		return mIconUnavailable;
	}
	
	public void setIconUnavailable(
			Bitmap mIconUnavailable )
	{
		if( mIconUnavailable == null && this.mIconUnavailable != null )
		{
			this.mIconUnavailable.recycle();
		}
		this.mIconUnavailable = mIconUnavailable;
	}
	
	public void setAvailable(
			boolean available )
	{
		this.available = available;
	}
	
	public boolean getAvailable()
	{
		return available;
	}
	//cheyingkun add end
	;
	
	public ItemInfo()
	{
		// zhangjin@2016/05/05 ADD START
		user = UserHandleCompat.myUserHandle();
		// zhangjin@2016/05/05 ADD END
	}
	
	ItemInfo(
			ItemInfo info )
	{
		id = info.id;
		cellX = info.getCellX();
		cellY = info.getCellY();
		spanX = info.getSpanX();
		spanY = info.getSpanY();
		screenId = info.getScreenId();
		itemType = info.getItemType();
		container = info.getContainer();
		//<数据库字段更新> liuhailin@2015-03-23 add begin
		defaultWorkspaceItemType = info.getDefaultWorkspaceItemType();
		//<数据库字段更新> liuhailin@2015-03-23 add end
		// tempdebug:
		// zhangjin@2016/05/05 ADD START
		user = info.user;
		// zhangjin@2016/05/05 ADD END
		LauncherModel.checkItemInfo( this );
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//protected Intent getIntent()
	public Intent getIntent()
	//添加智能分类功能 , change by shlt@2015/02/09 UPD END
	{
		throw new RuntimeException( "Unexpected Intent" );
	}
	
	/**
	 * Write the fields of this item to the DB
	 * 
	 * @param values
	 */
	public void onAddToDatabase(
			ContentValues values )
	{
		values.put( LauncherSettings.BaseLauncherColumns.ITEM_TYPE , itemType );
		values.put( LauncherSettings.Favorites.CONTAINER , container );
		//<数据库字段更新> liuhailin@2015-03-23 add begin
		values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , defaultWorkspaceItemType );
		//<数据库字段更新> liuhailin@2015-03-23 add end
		values.put( LauncherSettings.Favorites.SCREEN , screenId );
		values.put( LauncherSettings.Favorites.CELLX , cellX );
		values.put( LauncherSettings.Favorites.CELLY , cellY );
		values.put( LauncherSettings.Favorites.SPANX , spanX );
		values.put( LauncherSettings.Favorites.SPANY , spanY );
	}
	
	public void updateValuesWithCoordinates(
			ContentValues values ,
			int cellX ,
			int cellY )
	{
		values.put( LauncherSettings.Favorites.CELLX , cellX );
		values.put( LauncherSettings.Favorites.CELLY , cellY );
	}
	
	public static byte[] flattenBitmap(
			Bitmap bitmap )
	{
		// Try go guesstimate how much space the icon will take when serialized
		// to avoid unnecessary allocations/copies during the write.
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
		ByteArrayOutputStream out = new ByteArrayOutputStream( size );
		try
		{
			bitmap.compress( Bitmap.CompressFormat.PNG , 100 , out );
			out.flush();
			out.close();
			return out.toByteArray();
		}
		catch( IOException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( "Favorite" , "Could not write icon" );
			return null;
		}
	}
	
	public static void writeBitmap(
			ContentValues values ,
			Bitmap bitmap )
	{
		if( bitmap != null )
		{
			byte[] data = flattenBitmap( bitmap );
			values.put( LauncherSettings.Favorites.ICON , data );
		}
	}
	
	/**
	 * It is very important that sub-classes implement this if they contain any references
	 * to the activity (anything in the view hierarchy etc.). If not, leaks can result since
	 * ItemInfo objects persist across rotation and can hence leak by holding stale references
	 * to the old view hierarchy / activity.
	 */
	public void unbind()
	{
	}
	
	@Override
	public String toString()
	{
		return StringUtils.concat(
				"Item(id:" ,
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
	
	//添加智能分类功能 , change by shlt@2015/02/12 ADD START
	public String getTitle()
	{
		//0010396: 【文件夹】英文状态下，智能分类后的文件夹名称仍然是中文 , change by shlt@2015/03/10 UPD START
		//return title.toString();
		//title存储的有可能是中文名或者英文名，也有可能是“中文+cooee+英文”的名称，如果包含“+cooee+”，就把它拆开，根据当前语言环境返回不同名称
		String stringTitle = title.toString();
		if( stringTitle != null && stringTitle.contains( "+cooee+" ) )
		{
			String[] titles = stringTitle.split( "\\+cooee\\+" );
			//
			Locale l = Locale.getDefault();
			String language = l.getLanguage().toLowerCase();
			String country = l.getCountry().toLowerCase();
			if( "zh".equals( language ) /*&& "cn".equals( country )*/)
			{
				if( "tw".equals( country ) && titles.length >= 3 )//繁体
				{
					return titles[2];
				}
				return titles[0];
			}
			else
			{
				return titles[1];
			}
		}
		//xiatian add start	//fix bug：解决“桌面上默认配置快捷方式和虚图标，切换语言后图标的名称没有切换为相应语言”的问题。
		else if( stringTitle.contains( LauncherProvider.ITEM_TITLE_RESOURCE_NAME_KEY ) )
		{
			Resources mResources = LauncherAppState.getInstance().getContext().getResources();
			String mTitleResourceName = stringTitle.subSequence( LauncherProvider.ITEM_TITLE_RESOURCE_NAME_KEY.length() , stringTitle.length() ).toString();
			int titleId = mResources.getIdentifier( mTitleResourceName , null , null );
			if( titleId > 0 )
			{
				return LauncherDefaultConfig.getString( titleId );
			}
			else
			{
				return stringTitle;
			}
		}
		//xiatian add end
		else
		{
			return stringTitle;
		}
		//0010396: 【文件夹】英文状态下，智能分类后的文件夹名称仍然是中文 , change by shlt@2015/03/10 UPD END
	}
	
	public void setTitle(
			String title )
	{
		this.title = title;
	}
	
	//添加智能分类功能 , change by shlt@2015/02/12 ADD END
	public int getItemType()
	{
		return itemType;
	}
	
	public void setItemType(
			int itemType )
	{
		this.itemType = itemType;
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(
			long id )
	{
		this.id = id;
	}
	
	public long getContainer()
	{
		return container;
	}
	
	public void setContainer(
			long container )
	{
		this.container = container;
	}
	
	public long getScreenId()
	{
		return screenId;
	}
	
	public void setScreenId(
			long screenId )
	{
		this.screenId = screenId;
	}
	
	public int getCellX()
	{
		return cellX;
	}
	
	public void setCellX(
			int cellX )
	{
		this.cellX = cellX;
	}
	
	public int getCellY()
	{
		return cellY;
	}
	
	public void setCellY(
			int cellY )
	{
		this.cellY = cellY;
	}
	
	public int getSpanX()
	{
		return spanX;
	}
	
	public void setSpanX(
			int spanX )
	{
		this.spanX = spanX;
	}
	
	public int getSpanY()
	{
		return spanY;
	}
	
	public void setSpanY(
			int spanY )
	{
		this.spanY = spanY;
	}
	
	public void setTitle(
			CharSequence title )
	{
		this.title = title;
	}
	
	public int getMinSpanX()
	{
		return minSpanX;
	}
	
	public void setMinSpanX(
			int minSpanX )
	{
		this.minSpanX = minSpanX;
	}
	
	public int getMinSpanY()
	{
		return minSpanY;
	}
	
	public void setMinSpanY(
			int minSpanY )
	{
		this.minSpanY = minSpanY;
	}
	
	public boolean getRequiresDbUpdate()
	{
		return requiresDbUpdate;
	}
	
	public void setRequiresDbUpdate(
			boolean requiresDbUpdate )
	{
		this.requiresDbUpdate = requiresDbUpdate;
	}
	
	public int getDefaultWorkspaceItemType()
	{
		return defaultWorkspaceItemType;
	}
	
	public void setDefaultWorkspaceItemType(
			int defaultWorkspaceItemType )
	{
		this.defaultWorkspaceItemType = defaultWorkspaceItemType;
	}
	
	public int[] getDropPos()
	{
		return dropPos;
	}
	
	public void setDropPos(
			int[] dropPos )
	{
		this.dropPos = dropPos;
	}
	
	//xiatian add start	//整理代码：整理接口willAcceptDrop
	public boolean willAcceptDrop()
	{
		boolean ret = false;
		if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET )
		{
			ret = true;
		}
		return ret;
	}
	//xiatian add end
	;
	
	//xiatian add start	//整理代码：整理接口acceptDrop
	public boolean acceptDrop()
	{
		boolean ret = false;
		if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
		//
		//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//该图标是否相应drop事件
		|| getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL
		//xiatian add end
		//
		)
		{
			ret = true;
		}
		return ret;
	}
	//xiatian add end
	;
	
	//xiatian add start	//整理代码：整理接口willAcceptItem
	public boolean willAcceptItem()
	{
		boolean ret = false;
		if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
		//
		//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//该图标是否可以放入文件夹
		|| getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL
		//xiatian add end
		//
		)
		{
			ret = true;
		}
		return ret;
	}
	//xiatian add end
	;
	
	//xiatian add start	//整理代码：整理接口willBecomeShortcut
	public boolean willBecomeShortcut()
	{
		boolean ret = false;
		if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
		//
		//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//该图标是否可以放入文件夹
		|| getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL
		//xiatian add end
		//
		)
		{
			ret = true;
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
		if( !(
		//
		getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
		//
		|| getItemType() == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
		//
		|| getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL
		//
		|| getItemType() == LauncherSettings.Favorites.ITEM_TYPE_FOLDER
		//
		) )
		{
			throw new IllegalStateException( StringUtils.concat( "Unknown item type: " , getItemType() ) );
		}
		return null;
	}
	
	//xiatian add end
	public String getTitleString()
	{
		return title.toString();
	}
}
