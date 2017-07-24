package com.cooee.phenix.Functions.Category;


import com.cooee.phenix.IconCache;
import com.cooee.phenix.data.ShortcutInfo;


public class OperateVirtualInfo extends ShortcutInfo
{
	
	//	private static final long serialVersionUID = 1L;
	@Override
	public void updateIcon(
			IconCache iconCache )
	{
		if( isOperateVirtualMoreAppItem() )
		{
			mIcon = iconCache.getOperateVirtualMoreAppIcon();
			setIsUsingFallbackIcon( iconCache.isDefaultIcon( mIcon ) );
		}
		else
		{
			super.updateIcon( iconCache );
		}
	}
}
