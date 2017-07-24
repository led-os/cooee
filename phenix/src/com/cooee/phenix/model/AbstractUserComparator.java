package com.cooee.phenix.model;


import java.util.Comparator;

import android.content.Context;

import com.cooee.phenix.compat.UserHandleCompat;
import com.cooee.phenix.compat.UserManagerCompat;
import com.cooee.phenix.data.ItemInfo;


/**
 * A comparator to arrange items based on user profiles.
 */
public abstract class AbstractUserComparator<T extends ItemInfo> implements Comparator<T>
{
	
	private final UserManagerCompat mUserManager;
	private final UserHandleCompat mMyUser;
	
	public AbstractUserComparator(
			Context context )
	{
		mUserManager = UserManagerCompat.getInstance( context );
		mMyUser = UserHandleCompat.myUserHandle();
	}
	
	@Override
	public int compare(
			T lhs ,
			T rhs )
	{
		if( !( lhs != null && rhs != null ) )
		{
			return 0;
		}
		if( mMyUser.equals( lhs.user ) )
		{
			return -1;
		}
		else
		{
			Long aUserSerial = mUserManager.getSerialNumberForUser( lhs.user );
			Long bUserSerial = mUserManager.getSerialNumberForUser( rhs.user );
			return aUserSerial.compareTo( bUserSerial );
		}
	}
}
