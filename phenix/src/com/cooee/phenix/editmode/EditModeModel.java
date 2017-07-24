package com.cooee.phenix.editmode;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.pm.ResolveInfo;

import com.cooee.phenix.AllAppsList;
import com.cooee.phenix.editmode.item.EditModelItem;
import com.cooee.phenix.editmode.provider.EditModelProviderBase;


public class EditModeModel
{
	
	private final String DATA_PKG_NAME = "com.cooee.phenix.editmode.provider.";
	private Context mContext = null;
	/**
	 * 用于存储所有的ModelProvide
	 */
	private HashMap<Object , EditModelProviderBase> mAllModelProvide = new HashMap<Object , EditModelProviderBase>();
	
	public EditModeModel(
			Context context )
	{
		mContext = context;
	}
	
	/**
	 * 通过指定的Key获得对应的数据
	 * @param Key
	 * @return
	 */
	public ArrayList<EditModelItem> loadEditModelItemByKey(
			String Key )
	{
		String className = DATA_PKG_NAME + Key;
		EditModelProviderBase modelData = mAllModelProvide.get( Key );
		if( modelData == null )
		{
			modelData = getModelDataByClassName( className );
			mAllModelProvide.put( Key , modelData );
		}
		if( modelData != null )
		{
			return modelData.loadAllModelData( mContext , Key );
		}
		return null;
	}
	
	public ArrayList<EditModelItem> loadAddedPackageEditModelItem(
			String pkgname ,
			String Key )
	{
		String className = DATA_PKG_NAME + Key;
		EditModelProviderBase modelData = mAllModelProvide.get( Key );
		if( modelData == null )
		{
			modelData = getModelDataByClassName( className );
			mAllModelProvide.put( Key , modelData );
		}
		if( modelData != null )
		{
			List<ResolveInfo> resolveInfos = AllAppsList.findAllActivitiesForPackage( mContext , pkgname );
			return modelData.addNewModelData( mContext , resolveInfos , Key );
		}
		return null;
	}
	
	public HashMap<Object , EditModelProviderBase> getAllModelProvide()
	{
		return mAllModelProvide;
	}
	
	public EditModelProviderBase getModelDataByClassName(
			String className )
	{
		EditModelProviderBase modelData = null;
		try
		{
			modelData = (EditModelProviderBase)Class.forName( className ).newInstance();
		}
		catch( InstantiationException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return modelData;
	}
	
	public void updateEditModelItem(
			String key ,
			ArrayList<EditModelItem> list )
	{
		EditModelProviderBase modelData = mAllModelProvide.get( key );
		if( modelData != null )
		{
			modelData.updateModeItem( list );
		}
	}
}
