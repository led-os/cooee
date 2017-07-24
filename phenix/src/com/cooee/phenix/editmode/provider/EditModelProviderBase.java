package com.cooee.phenix.editmode.provider;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.cooee.phenix.editmode.item.EditModelItem;


public interface EditModelProviderBase
{
	
	/**
	 * 获得所有的数据方法
	 * @return
	 */
	public ArrayList<EditModelItem> loadAllModelData(
			Context context ,
			String key );
	
	/**
	 * 获得新添加的数据方法
	 * @return
	 */
	public ArrayList<EditModelItem> addNewModelData(
			Context context ,
			List<?> infos ,
			String key );
	
	public void updateModeItem(
			ArrayList<EditModelItem> list );
}
