package com.cooee.phenix.editmode.item;


import android.content.Context;

import com.cooee.phenix.data.PendingAddItemInfo;
import com.cooee.phenix.editmode.interfaces.IEditControlCallBack;


public class EditModelWidgetItem extends EditModelItem
{
	
	private PendingAddItemInfo mAddItemInfo = null;
	
	public PendingAddItemInfo getAddItemInfo()
	{
		return mAddItemInfo;
	}
	
	public void setAddItemInfo(
			PendingAddItemInfo addItemInfo )
	{
		this.mAddItemInfo = addItemInfo;
	}
	
	@Override
	public void onItemClick(
			IEditControlCallBack callback ,
			Context context )
	{
		// TODO Auto-generated method stub
	}
}
