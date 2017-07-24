package com.cooee.framework.function.DynamicEntry;


import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.cooee.framework.utils.StringUtils;


public class OperateDynamicItem
{
	
	public String mPackageName; //包名/虚链接时是网址
	public String mTitle; //英文名称
	public String mCNTitle; //中文名称
	public String mTWTitle; //繁体名称
	public Bitmap mIconBitmap; //图标
	public String mBitmapPath; //图标的图片路径
	public String mResID; //资源id（目前下载用不到，用于统计）
	public String mDownloadTip; //下载的时候文字提示
	public int dynamicType; //类型：2：虚图标；3：虚链接
	public int mAppDownloadType; //3和2都是从APPstore下载 dynamicEntry1010	
	public int mAppSize;//appSize的大小是多少
	public String mVersion;//版本号
	public String mWeblinkPkg = null;
	
	public OperateDynamicItem()
	{
	}
	
	public OperateDynamicItem(
			OperateDynamicItem item )
	{
		this.mPackageName = item.mPackageName;
		this.mTitle = item.mTitle;
		this.mCNTitle = item.mCNTitle;
		this.mTWTitle = item.mTWTitle;
		this.mIconBitmap = item.mIconBitmap;
		this.mResID = item.mResID;
		this.mDownloadTip = item.mDownloadTip;
		this.mBitmapPath = item.mBitmapPath;
		this.dynamicType = item.dynamicType;
		this.mAppDownloadType = item.mAppDownloadType; //dynamicEntry1010
		this.mAppSize = item.mAppSize;
		this.mVersion = item.mVersion;
		this.mWeblinkPkg = item.mWeblinkPkg;
	}
	
	public String getDynamicItemTitle()
	{
		switch( OperateDynamicUtils.getCurLanguage() )
		{
			case 1:
				return mCNTitle;
			case 2:
				return mTWTitle;
		}
		return mTitle;
	}
	
	public boolean dynamicItemequals(
			OperateDynamicItem item )
	{
		return this.mPackageName.equals( item.mPackageName );
	}
	
	public JSONObject toJSON()
	{
		JSONObject res = new JSONObject();
		try
		{
			res.put( "f0" , dynamicType );
			res.put( "f1" , mPackageName );
			res.put( "f2" , mDownloadTip );
			res.put( "f6" , mCNTitle );
			res.put( "f7" , mTitle );
			res.put( "f8" , mTWTitle );
			res.put( "f9" , mBitmapPath );
			res.put( "f10" , mAppDownloadType );//dynamicEntry1010
			res.put( "f5" , mAppSize );
			res.put( "f3" , mVersion );
			res.put( "f11" , mWeblinkPkg );
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
			return StringUtils.concat( "DynamicItem=" , toJSON().toString( 4 ) );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return StringUtils.concat( "DynamicItem mPackageName:" , mPackageName , " is wrong" );
		}
	}
}
