package com.cooee.phenix.compat;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.util.LongArrayMap;


@TargetApi( Build.VERSION_CODES.LOLLIPOP )
public class UserManagerCompatVL extends UserManagerCompatV17
{
	
	private static final String USER_CREATION_TIME_KEY = "user_creation_time_";
	private final PackageManager mPm;
	private final Context mContext;
	
	UserManagerCompatVL(
			Context context )
	{
		super( context );
		mPm = context.getPackageManager();
		mContext = context;
	}
	
	@Override
	public void enableAndResetCache()
	{
		synchronized( this )
		{
			mUsers = new LongArrayMap<UserHandleCompat>();
			mUserToSerialMap = new HashMap<UserHandleCompat , Long>();
			List<UserHandle> users = mUserManager.getUserProfiles();
			if( users != null )
			{
				for( UserHandle user : users )
				{
					long serial = mUserManager.getSerialNumberForUser( user );
					UserHandleCompat userCompat = UserHandleCompat.fromUser( user );
					mUsers.put( serial , userCompat );
					mUserToSerialMap.put( userCompat , serial );
				}
			}
		}
	}
	
	@Override
	public List<UserHandleCompat> getUserProfiles()
	{
		synchronized( this )
		{
			if( mUsers != null )
			{
				List<UserHandleCompat> users = new ArrayList<UserHandleCompat>();
				users.addAll( mUserToSerialMap.keySet() );
				return users;
			}
		}
		List<UserHandle> users = mUserManager.getUserProfiles();
		if( users == null )
		{
			return Collections.emptyList();
		}
		ArrayList<UserHandleCompat> compatUsers = new ArrayList<UserHandleCompat>( users.size() );
		for( UserHandle user : users )
		{
			compatUsers.add( UserHandleCompat.fromUser( user ) );
		}
		return compatUsers;
	}
	
	@Override
	public Drawable getBadgedDrawableForUser(
			Drawable unbadged ,
			UserHandleCompat user )
	{
		return mPm.getUserBadgedIcon( unbadged , user.getUser() );
	}
	
	@Override
	public CharSequence getBadgedLabelForUser(
			CharSequence label ,
			UserHandleCompat user )
	{
		if( user == null )
		{
			return label;
		}
		return mPm.getUserBadgedLabel( label , user.getUser() );
	}
	
	@Override
	public long getUserCreationTime(
			UserHandleCompat user )
	{
		// zhangjin@2016/05/05 DEL START
		//if( Utilities.ATLEAST_MARSHMALLOW )
		//{
		//	return mUserManager.getUserCreationTime( user.getUser() );
		//}
		// zhangjin@2016/05/05 DEL END
		SharedPreferences prefs = mContext.getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , Context.MODE_PRIVATE );
		String key = StringUtils.concat( USER_CREATION_TIME_KEY , getSerialNumberForUser( user ) );
		if( !prefs.contains( key ) )
		{
			prefs.edit().putLong( key , System.currentTimeMillis() ).apply();
		}
		return prefs.getLong( key , 0 );
	}
}
