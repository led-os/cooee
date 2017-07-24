package com.cooee.phenix.compat;


import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;


public class UserManagerCompatV16 extends UserManagerCompat
{
	
	UserManagerCompatV16()
	{
	}
	
	public List<UserHandleCompat> getUserProfiles()
	{
		List<UserHandleCompat> profiles = new ArrayList<UserHandleCompat>( 1 );
		profiles.add( UserHandleCompat.myUserHandle() );
		return profiles;
	}
	
	public UserHandleCompat getUserForSerialNumber(
			long serialNumber )
	{
		return UserHandleCompat.myUserHandle();
	}
	
	public Drawable getBadgedDrawableForUser(
			Drawable unbadged ,
			UserHandleCompat user )
	{
		return unbadged;
	}
	
	public long getSerialNumberForUser(
			UserHandleCompat user )
	{
		return 0;
	}
	
	public CharSequence getBadgedLabelForUser(
			CharSequence label ,
			UserHandleCompat user )
	{
		return label;
	}
	
	@Override
	public long getUserCreationTime(
			UserHandleCompat user )
	{
		return 0;
	}
	
	@Override
	public void enableAndResetCache()
	{
	}
}
