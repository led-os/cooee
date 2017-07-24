package com.cooee.phenix.compat;


import java.util.HashMap;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.UserManager;

import com.cooee.phenix.util.LongArrayMap;


@TargetApi( Build.VERSION_CODES.JELLY_BEAN_MR1 )
public class UserManagerCompatV17 extends UserManagerCompatV16
{
	
	protected LongArrayMap<UserHandleCompat> mUsers;
	// Create a separate reverse map as LongArrayMap.indexOfValue checks if objects are same
	// and not {@link Object#equals}
	protected HashMap<UserHandleCompat , Long> mUserToSerialMap;
	protected UserManager mUserManager;
	
	UserManagerCompatV17(
			Context context )
	{
		mUserManager = (UserManager)context.getSystemService( Context.USER_SERVICE );
	}
	
	public long getSerialNumberForUser(
			UserHandleCompat user )
	{
		synchronized( this )
		{
			if( mUserToSerialMap != null )
			{
				Long serial = mUserToSerialMap.get( user );
				return serial == null ? 0 : serial;
			}
		}
		return mUserManager.getSerialNumberForUser( user.getUser() );
	}
	
	public UserHandleCompat getUserForSerialNumber(
			long serialNumber )
	{
		synchronized( this )
		{
			if( mUsers != null )
			{
				return mUsers.get( serialNumber );
			}
		}
		return UserHandleCompat.fromUser( mUserManager.getUserForSerialNumber( serialNumber ) );
	}
	
	@Override
	public void enableAndResetCache()
	{
		synchronized( this )
		{
			mUsers = new LongArrayMap<UserHandleCompat>();
			mUserToSerialMap = new HashMap<UserHandleCompat , Long>();
			UserHandleCompat myUser = UserHandleCompat.myUserHandle();
			long serial = mUserManager.getSerialNumberForUser( myUser.getUser() );
			mUsers.put( serial , myUser );
			mUserToSerialMap.put( myUser , serial );
		}
	}
}
