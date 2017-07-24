package com.cooee.framework.function.DynamicEntry;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cooee.framework.utils.StringUtils;


public class OperateDynamicData
{
	
	public int dynamicType; //类型：1：文件夹；2：虚图标；3：虚链接
	public String dynamicID; //动态入口id
	public String iconPath; //本身图标的路径
	public long screen = -1; //在桌面的屏幕
	public int cellX = -1; //在桌面的位置X
	public int cellY = -1; //在桌面的位置Y
	public boolean mIsDeskShow; //在桌面是否显示
	public String mDeskName; //在桌面的英文名
	public String mDeskNameCN; //在桌面的中文名
	public String mDeskNameTW; //在桌面的繁体名
	public boolean mDeskHot; //在桌面是否显示N标
	public boolean mIsShow; //在主菜单是否显示
	public String mName; //在主菜单的英文名
	public String mNameCN; //在主菜单的中文名
	public String mNameTW; //在主菜单的繁体名
	public boolean mIsShowHot; //在主菜单是否显示N标
	public String mPkgnameOrAddr; //虚图标的包名或虚链接的网址
	public String downloadTip; //虚图标的下载提示语
	//public boolean isDel; //是否要删除此入口
	public int menuPos = -1;//主菜单中的位置，小于0表示和app一起排序，否则表示从第一个icon开始算起的位置
	public boolean canDelete = true;// 图标是否能够删除,0为不能删除，1为能删除
	public boolean keepItem; //默认配置的图标，在更新时是否优先保留
	public int flagOne = -1; //暂时无用，便于将来的扩充
	public int flagTwo = -1; //暂时无用，便于将来的扩充
	public boolean mShowInWidgetList;
	public ArrayList<OperateDynamicItem> mDynamicItems; //每一项
	static private final int CHARNUM = 11;
	public int mAppDownloadType;//dynamicEntry1010 3和2都是从APPstore下载 dynamicEntry
	public int mAppSize;
	public String mWeblinkPkg = null;
	public boolean isDefault = false;
	
	public OperateDynamicData(
			OperateDynamicData data )
	{
		this.dynamicType = data.dynamicType;
		this.dynamicID = data.dynamicID;
		this.iconPath = data.iconPath;
		this.screen = data.screen;
		this.cellX = data.cellX;
		this.cellY = data.cellY;
		this.mIsDeskShow = data.mIsDeskShow;
		this.mDeskName = data.mDeskName;
		this.mDeskNameCN = data.mDeskNameCN;
		this.mDeskNameTW = data.mDeskNameTW;
		this.mDeskHot = data.mDeskHot;
		this.mIsShow = data.mIsShow;
		this.mName = data.mName;
		this.mNameCN = data.mNameCN;
		this.mNameTW = data.mNameTW;
		this.mIsShowHot = data.mIsShowHot;
		this.mPkgnameOrAddr = data.mPkgnameOrAddr;
		this.downloadTip = data.downloadTip;
		this.menuPos = data.menuPos;
		this.canDelete = data.canDelete;
		this.keepItem = data.keepItem;
		this.flagOne = data.flagOne;
		this.flagTwo = data.flagTwo;
		this.mAppDownloadType = data.mAppDownloadType;//dynamicEntry1010
		this.mAppSize = data.mAppSize;
		this.mShowInWidgetList = data.mShowInWidgetList;
		this.mDynamicItems = new ArrayList<OperateDynamicItem>();
		OperateDynamicItem newItem = null;
		for( OperateDynamicItem item : data.mDynamicItems )
		{
			newItem = new OperateDynamicItem( item );
			this.mDynamicItems.add( newItem );
		}
		this.mWeblinkPkg = data.mWeblinkPkg;
	}
	
	public OperateDynamicData()
	{
		mShowInWidgetList = false;
		menuPos = -1;
		mDynamicItems = new ArrayList<OperateDynamicItem>();
	}
	
	public String getDynamicEntryTitle(
			boolean isDesk )
	{
		//添加防错包含，如果retString为NULL，设定默认为Folder
		String retString = null;
		switch( OperateDynamicUtils.getCurLanguage() )
		{
			case 1:
				retString = isDesk ? mDeskNameCN : mNameCN;
				break;
			case 2:
				retString = isDesk ? mDeskNameTW : mNameTW;
				break;
			default:
				retString = isDesk ? mDeskName : mName;
				break;
		}
		if( retString == null )
		{
			retString = "Folder";
		}
		if( retString.length() > CHARNUM )
		{
			retString = retString.substring( 0 , CHARNUM );
		}
		return retString;
	}
	
	public void setDynamicEntryTitle(
			String name ,
			boolean isDesk )
	{
		switch( OperateDynamicUtils.getCurLanguage() )
		{
			case 1:
				if( isDesk )
				{
					mDeskNameCN = name;
				}
				else
				{
					mNameCN = name;
				}
				break;
			case 2:
				if( isDesk )
				{
					mDeskNameTW = name;
				}
				else
				{
					mNameTW = name;
				}
				break;
			default:
				if( isDesk )
				{
					mDeskName = name;
				}
				else
				{
					mName = name;
				}
				break;
		}
	}
	
	public JSONObject toJSON()
	{
		//	r1	list	数字	入口ID [入口ID是客户端标示或者定位入口的唯一ID。]
		//  r2  list    类型：1：文件夹 2：应用程序 3：网页链接
		//	r3	list	对象，字符	在桌面的英文名
		//	r4	list	对象，字符	在桌面的中文名称
		//	r5	list	对象，字符	在桌面的繁体名称
		//	r3_1	list	对象，字符	在主菜单的英文名称
		//	r4_1	list	对象，字符	在主菜单的中文名称
		//	r5_1	list	对象，字符	在主菜单的繁体名称
		//	r6	list	对象，数字	应用程序列表 0:不显示 1:显示
		//	r7	list	对象，数字	桌面 0:不显示 1:显示
		//	r8	list	对象，数字	快捷方式显示屏幕位置x
		//	r9	list	对象，数字	快捷方式显示屏幕位置y
		//	r10	list	对象，字符	图标地址url
		//	r11	list	对象，字符	网页链接入口url地址或packname
		//  r12 list    对象，数字      桌面是否显示N标
		//  r12_1 list    对象，数字     主菜单是否显示N标
		JSONArray jsonArray = new JSONArray();
		for( OperateDynamicItem item : mDynamicItems )
		{
			jsonArray.put( item.toJSON() );
		}
		JSONObject res = new JSONObject();
		try
		{
			res.put( "r1" , dynamicID );
			res.put( "r2" , dynamicType );
			res.put( "r3" , mDeskName );
			res.put( "r4" , mDeskNameCN );
			res.put( "r5" , mDeskNameTW );
			res.put( "r3_1" , mName );
			res.put( "r4_1" , mNameCN );
			res.put( "r5_1" , mNameTW );
			res.put( "r6" , mIsShow );
			res.put( "r7" , screen );
			res.put( "r7_1" , mIsDeskShow );
			res.put( "r8" , cellX );
			res.put( "r9" , cellY );
			res.put( "r10" , iconPath );
			res.put( "r11" , mPkgnameOrAddr );
			if( dynamicType == OperateDynamicUtils.VIRTUAL_APP )
			{
				res.put( "r11_1" , downloadTip );
				res.put( "f10" , mAppDownloadType );//dynamicEntry1010
			}
			if( dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
			{
				res.put( "f11" , mWeblinkPkg );
			}
			res.put( "r12" , mDeskHot );
			res.put( "r12_1" , mIsShowHot );
			//res.put( "r20" , iconPath );
			//res.put( "r21" , mIsDeskShow );
			res.put( OperateDynamicUtils.DYNAMIC_DATA_CAN_DELETE , canDelete );
			res.put( OperateDynamicUtils.DYNAMIC_DATA_KEEP_ITEM , keepItem );
			//res.put( "r22" , mIsShow );
			if( dynamicType == OperateDynamicUtils.FOLDER )
			{
				res.put( "folder" , jsonArray );
			}
			return res;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString()
	{
		try
		{
			return StringUtils.concat( "DynamicData=" , toJSON().toString( 4 ) );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return StringUtils.concat( "DynamicData mDeskNameCN:" , mDeskNameCN , "-dynamicID:" , dynamicID , " is wrong" );
		}
	}
}
