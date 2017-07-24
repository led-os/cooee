package com.cooee.phenix.compat;


import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cooee.phenix.Utilities;


public abstract class UserManagerCompat
{
	
	protected UserManagerCompat()
	{
	}
	
	private static final Object sInstanceLock = new Object();
	private static UserManagerCompat sInstance;
	
	public static UserManagerCompat getInstance(
			Context context )
	{
		synchronized( sInstanceLock )
		{
			if( sInstance == null )
			{
				if( Utilities.ATLEAST_LOLLIPOP )
				{
					sInstance = new UserManagerCompatVL( context.getApplicationContext() );
				}
				else if( Utilities.ATLEAST_JB_MR1 )
				{
					sInstance = new UserManagerCompatV17( context.getApplicationContext() );
				}
				else
				{
					sInstance = new UserManagerCompatV16();
				}
			}
			return sInstance;
		}
	}
	
	/**
	 * Creates a cache for users.
	 */
	public abstract void enableAndResetCache();
	
	public abstract List<UserHandleCompat> getUserProfiles();
	
	public abstract long getSerialNumberForUser(
			UserHandleCompat user );
	
	public abstract UserHandleCompat getUserForSerialNumber(
			long serialNumber );
	
	public abstract Drawable getBadgedDrawableForUser(
			Drawable unbadged ,
			UserHandleCompat user );
	
	public abstract CharSequence getBadgedLabelForUser(
			CharSequence label ,
			UserHandleCompat user );
	
	public abstract long getUserCreationTime(
			UserHandleCompat user );
}
