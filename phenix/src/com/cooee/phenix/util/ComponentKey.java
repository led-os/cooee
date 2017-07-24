package com.cooee.phenix.util;


import java.util.Arrays;

import android.content.ComponentName;
import android.content.Context;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.compat.UserHandleCompat;
import com.cooee.phenix.compat.UserManagerCompat;


public class ComponentKey
{
	
	public final ComponentName componentName;
	public final UserHandleCompat user;
	private final int mHashCode;
	
	public ComponentKey(
			ComponentName componentName ,
			UserHandleCompat user )
	{
		assert ( componentName != null );
		assert ( user != null );
		this.componentName = componentName;
		this.user = user;
		mHashCode = Arrays.hashCode( new Object[]{ componentName , user } );
	}
	
	/**
	 * Creates a new component key from an encoded component key string in the form of
	 * [flattenedComponentString#userId].  If the userId is not present, then it defaults
	 * to the current user.
	 */
	public ComponentKey(
			Context context ,
			String componentKeyStr )
	{
		int userDelimiterIndex = componentKeyStr.indexOf( "#" );
		if( userDelimiterIndex != -1 )
		{
			String componentStr = componentKeyStr.substring( 0 , userDelimiterIndex );
			Long componentUser = Long.valueOf( componentKeyStr.substring( userDelimiterIndex + 1 ) );
			componentName = ComponentName.unflattenFromString( componentStr );
			user = UserManagerCompat.getInstance( context ).getUserForSerialNumber( componentUser.longValue() );
		}
		else
		{
			// No user provided, default to the current user
			componentName = ComponentName.unflattenFromString( componentKeyStr );
			user = UserHandleCompat.myUserHandle();
		}
		mHashCode = Arrays.hashCode( new Object[]{ componentName , user } );
	}
	
	/**
	 * Encodes a component key as a string of the form [flattenedComponentString#userId].
	 */
	public String flattenToString(
			Context context )
	{
		String flattened = componentName.flattenToString();
		if( user != null )
		{
			flattened = StringUtils.concat( flattened , "#" , UserManagerCompat.getInstance( context ).getSerialNumberForUser( user ) );
		}
		return flattened;
	}
	
	@Override
	public int hashCode()
	{
		return mHashCode;
	}
	
	@Override
	public boolean equals(
			Object o )
	{
		ComponentKey other = (ComponentKey)o;
		return other.componentName.equals( componentName ) && other.user.equals( user );
	}
}
