package com.cooee.phenix.data;


import java.io.Serializable;

import android.content.ContentValues;
import android.content.Intent;

import com.cooee.phenix.LauncherSettings;


// 添加智能分类功能 , change by shlt@2015/02/12 ADD START
public class EnhanceItemInfo extends ItemInfo implements Serializable
{
	
	//	private static final long serialVersionUID = 1L;
	//<数据库字段更新> liuhailin@2015-03-23 modify begin
	public static final String INTENT_KEY_CATEGORY_FOLDER_ID = "categoryFolderId";
	public static final String INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM = "isOperateVirtualItem";
	public static final String INTENT_KEY_ICON_PATH = "iconPath";
	public static final String INTENT_KEY_IS_OPERATE_VIRTURAL_MORE_APP_ITEM = "isOperateVirtualMoreAppItem";
	/**
	 * 运营相关的内容信息
	 */
	Intent operateIntent;
	public boolean mCanUninstall = true;//虚图标是否可卸载
	public static final String IS_CAN_UNINSTALL = "mIsCanUninstall";
	
	//<数据库字段更新> liuhailin@2015-03-23 modify end
	public EnhanceItemInfo()
	{
		super();
		initOperateIntent();
	}
	
	public EnhanceItemInfo(
			EnhanceItemInfo enhanceItemInfo )
	{
		super( enhanceItemInfo );
		initOperateIntent();
	}
	
	//<数据库字段更新> liuhailin@2015-03-23 del begin
	//private boolean isHotseatDefaultItem = false;//是否是default_workspace.xml中配置的底边栏图标
	//private boolean isFirstPageDefault8Items = false;//是否是default_workspace.xml中配置的第一页中的默认8个图标
	//private int categoryFolderId = Integer.MAX_VALUE;//智能分类后，info所在文件夹在智能分类数据中所对应的文件夹id
	//private boolean canUninstall = true;//是否可以被卸载
	//private boolean canDrag = true;//是否可以被拖动
	//private boolean isOperateVirtualItem = false;//是否是虚图标
	//private String iconPath = null;//虚图标icon的路径
	//private boolean isOperateVirtualMoreAppItem = false;//事都是虚图标的“更多应用”图标
	//<数据库字段更新> liuhailin@2015-03-23 del end
	//<数据库字段更新> liuhailin@2015-03-23 modify begin
	private void initOperateIntent()
	{
		if( operateIntent == null )
		{
			operateIntent = new Intent();
			//xiatian start	//智能分类的数据，存储错误。（intent中同一个key,存boolean读int,某些framework的返回值是空,另一些会直接报错。）
			//			operateIntent.putExtra( INTENT_KEY_CATEGORY_FOLDER_ID , false );//xiatian del
			operateIntent.putExtra( INTENT_KEY_CATEGORY_FOLDER_ID , Integer.MAX_VALUE );//xiatian add
			//xiatian end
			operateIntent.putExtra( INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , false );
			operateIntent.putExtra( INTENT_KEY_ICON_PATH , "" );
			operateIntent.putExtra( INTENT_KEY_IS_OPERATE_VIRTURAL_MORE_APP_ITEM , false );
		}
	}
	
	public Intent getOperateIntent()
	{
		return operateIntent;
	}
	
	//<数据库字段更新> liuhailin@2015-03-23 modify end
	//<数据库字段更新> liuhailin@2015-03-24 del begin
	//public void getExtrasByIntent(
	//		Intent intent )
	//{
	//	if( intent != null )
	//	{
	//		//<数据库字段更新> liuhailin@2015-03-23 del begin
	//		//this.isHotseatDefaultItem = intent.getBooleanExtra( "isHotseatDefaultItem" , false );
	//		//this.isFirstPageDefault8Items = intent.getBooleanExtra( "isFirstPageDefault8Items" , false );
	//		//<数据库字段更新> liuhailin@2015-03-23 del end
	//		//this.categoryFolderId = intent.getIntExtra( INTENT_KEY_CATEGORY_FOLDER_ID , Integer.MAX_VALUE );
	//		this.canUninstall = intent.getBooleanExtra( "canUninstall" , true );
	//		this.canDrag = intent.getBooleanExtra( "canDrag" , true );
	//		//this.isOperateVirtualItem = intent.getBooleanExtra( "isOperateVirtualItem" , false );
	//		//this.iconPath = intent.getStringExtra( "iconPath" );
	//		//this.isOperateVirtualMoreAppItem = intent.getBooleanExtra( "isOperateVirtualMoreAppItem" , false );
	//	}
	//}
	//public Intent addExtrasToIntent(
	//		Intent intent )
	//{
	//	//<数据库字段更新> liuhailin@2015-03-23 del begin
	//	//intent.putExtra( "isHotseatDefaultItem" , isHotseatDefaultItem );
	//	//intent.putExtra( "isFirstPageDefault8Items" , isFirstPageDefault8Items );
	//	//<数据库字段更新> liuhailin@2015-03-23 del end
	//	//intent.putExtra( INTENT_KEY_CATEGORY_FOLDER_ID , categoryFolderId );
	//	intent.putExtra( "canUninstall" , canUninstall );
	//	intent.putExtra( "canDrag" , canDrag );
	//	//intent.putExtra( "isOperateVirtualItem" , isOperateVirtualItem );
	//	//intent.putExtra( "iconPath" , iconPath );
	//	//intent.putExtra( "isOperateVirtualMoreAppItem" , isOperateVirtualMoreAppItem );
	//	return intent;
	//}
	//<数据库字段更新> liuhailin@2015-03-24 del end
	//<数据库字段更新> liuhailin@2015-03-26 add begin
	@Override
	public void onAddToDatabase(
			ContentValues values )
	{
		// TODO Auto-generated method stub
		super.onAddToDatabase( values );
		String uri = ( operateIntent != null ? operateIntent.toUri( 0 ) : null );
		values.put( LauncherSettings.Favorites.OPERATE_INTENT , uri );
		//Log.d( "EnhanceItemInfo" , "uri = " + uri );
	}
	
	//<数据库字段更新> liuhailin@2015-03-26 add end
	public long getContainer()
	{
		return container;
	}
	
	public void setContainer(
			long container )
	{
		this.container = container;
	}
	
	//<数据库字段更新> liuhailin@2015-03-24 del begin
	public void setCanUninstall(
			boolean canUninstall )
	{
		this.mCanUninstall = canUninstall;
	}
	
	public boolean isCanUninstall()
	{
		return mCanUninstall;
	}
	
	//<数据库字段更新> liuhailin@2015-03-24 del end
	public long getScreenId()
	{
		return screenId;
	}
	
	public void setScreenId(
			long screenId )
	{
		this.screenId = screenId;
	}
	
	//<数据库字段更新> liuhailin@2015-03-23 del begin
	//public boolean isFirstPageDefault8Items()
	//{
	//	return isFirstPageDefault8Items;
	//}
	//
	//public boolean isHotseatDefaultItem()
	//{
	//	return isHotseatDefaultItem;
	//}
	//<数据库字段更新> liuhailin@2015-03-23 del end
	//<数据库字段更新> liuhailin@2015-03-26 modify begin
	public int getCategoryFolderId()
	{
		if( operateIntent != null )
		{
			return operateIntent.getIntExtra( INTENT_KEY_CATEGORY_FOLDER_ID , Integer.MAX_VALUE );
		}
		return Integer.MAX_VALUE;
	}
	
	public void setCategoryFolderId(
			int categoryFolderId )
	{
		if( operateIntent != null )
		{
			operateIntent.putExtra( INTENT_KEY_CATEGORY_FOLDER_ID , categoryFolderId );
		}
	}
	
	//<数据库字段更新> liuhailin@2015-03-26 modify end
	public long getId()
	{
		return id;
	}
	
	public int getCellX()
	{
		return cellX;
	}
	
	public int getCellY()
	{
		return cellY;
	}
	
	//<数据库字段更新> liuhailin@2015-03-24 del begin
	//public boolean isCanDrag()
	//{
	//	return canDrag;
	//}
	//
	//public void setCanDrag(
	//		boolean canDrag )
	//{
	//	this.canDrag = canDrag;
	//}
	//<数据库字段更新> liuhailin@2015-03-24 del end
	//<数据库字段更新> liuhailin@2015-03-24 del begin
	public boolean isOperateVirtualItem()
	{
		if( operateIntent != null )
		{
			return operateIntent.getBooleanExtra( INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , false );
		}
		return false;
	}
	
	public void setOperateVirtualItem(
			boolean isOperateVirtualItem )
	{
		if( operateIntent != null )
		{
			operateIntent.putExtra( INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , isOperateVirtualItem );
		}
	}
	
	public String getIconPath()
	{
		if( operateIntent != null )
		{
			return operateIntent.getStringExtra( INTENT_KEY_ICON_PATH );
		}
		return null;
	}
	
	public void setIconPath(
			String iconPath )
	{
		if( operateIntent != null )
		{
			operateIntent.putExtra( INTENT_KEY_ICON_PATH , iconPath );
		}
	}
	
	//<数据库字段更新> liuhailin@2015-03-24 del end
	//<数据库字段更新> liuhailin@2015-03-23 del begin
	//public void setHotseatDefaultItem(
	//		boolean isHotseatDefaultItem )
	//{
	//	this.isHotseatDefaultItem = isHotseatDefaultItem;
	//}
	//
	//public void setFirstPageDefault8Items(
	//		boolean isFirstPageDefault8Items )
	//{
	//	this.isFirstPageDefault8Items = isFirstPageDefault8Items;
	//}
	//<数据库字段更新> liuhailin@2015-03-23 del end
	//<数据库字段更新> liuhailin@2015-03-23 del begin
	public boolean isOperateVirtualMoreAppItem()
	{
		if( operateIntent != null )
		{
			if( isOperateVirtualItem() && operateIntent.getBooleanExtra( INTENT_KEY_IS_OPERATE_VIRTURAL_MORE_APP_ITEM , false ) )
			{
				return true;
			}
		}
		return false;
	}
	
	public void setOperateVirtualMoreAppItem(
			boolean isOperateVirtualMoreAppItem )
	{
		if( operateIntent != null )
		{
			setOperateVirtualItem( true );
			operateIntent.putExtra( INTENT_KEY_IS_OPERATE_VIRTURAL_MORE_APP_ITEM , isOperateVirtualMoreAppItem );
		}
	}
	//<数据库字段更新> liuhailin@2015-03-23 del end
	;
	
	//xiatian add start	//fix bug：解决“智能分类文件夹中的推荐应用，点击后无法下载（提示应用未安装）”的问题。
	public void setOperateIntent(
			Intent operateIntent )
	{
		this.operateIntent = operateIntent;
	}
	//xiatian add end
	;
}
