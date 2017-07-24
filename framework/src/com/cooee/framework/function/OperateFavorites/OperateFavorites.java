// lvjiangbin add 运营酷生活的开关
package com.cooee.framework.function.OperateFavorites;


import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class OperateFavorites
{
	
	private static final String TAG = "OperateFavorites";
	private static IOperateFavoritesCallbacks mOperateFavoritesCallbacks = null;
	public final static String OPERATE_FAVORITES_SWITCH_KEY = "OperateFavoritesSwitchKey";
	
	public interface IOperateFavoritesCallbacks
	{
		
		public void notifyFavoritesSwitch(
				boolean isShow );
	}
	
	public static void setCallbacks(
			IOperateFavoritesCallbacks mCallbacks )
	{
		mOperateFavoritesCallbacks = mCallbacks;
	}
	
	public static void notifyFavoritesSwitch(
			boolean isShow )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "notifyFavoritesSwitch - isShow:" , isShow ) );
		if( mOperateFavoritesCallbacks == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "notifyUmengSwitch - return[( mOperateUmengCallbacks == null )]" );
			return;
		}
		mOperateFavoritesCallbacks.notifyFavoritesSwitch( isShow );
	}
}
