package com.cooee.kpsh;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.util.Log;


public class CheckUitls
{
	
	public static boolean checkPermissionInAndroidManifest(
			ArrayList<String> mNeedCheckPermissionList ,
			String[] mPermissionListInAM ,
			boolean isShutdown )
	{
		if( mPermissionListInAM != null && mNeedCheckPermissionList.size() > 0 )
		{
			for( String permission : mPermissionListInAM )
			{
				if( mNeedCheckPermissionList.contains( permission ) )
				{
					mNeedCheckPermissionList.remove( permission );
				}
			}
		}
		if( !mNeedCheckPermissionList.isEmpty() )
		{
			if( isShutdown )
			{
				throw new RuntimeException( "AM lost <Permission android:name=\"" + mNeedCheckPermissionList.get( 0 ) + "\"/>" );
			}
			for( String lost : mNeedCheckPermissionList )
			{
				Log.v( "CheckUitls" , "lose permission = " + lost );
			}
			return false;
		}
		return true;
	}
	
	public static boolean checkServiceInAndroidManifest(
			ArrayList<String> mNeed2CheckServiceList ,
			ServiceInfo[] mServiceListInAM ,
			boolean isShutdown )
	{//pi.services
		if( mServiceListInAM != null && mNeed2CheckServiceList.size() > 0 )
		{
			for( int i = 0 ; i < mServiceListInAM.length ; i++ )
			{
				if( mNeed2CheckServiceList.contains( mServiceListInAM[i].name ) )
				{
					mNeed2CheckServiceList.remove( mServiceListInAM[i].name );
				}
			}
		}
		if( !mNeed2CheckServiceList.isEmpty() )
		{
			if( isShutdown )
			{
				throw new RuntimeException( "AM lost <Service android:name=\"" + mNeed2CheckServiceList.get( 0 ) + "\"/>" );
			}
			for( String lost : mNeed2CheckServiceList )
			{
				Log.v( "CheckUitls" , "lose service = " + lost );
			}
			return false;
		}
		return true;
	}
	
	public static boolean checkReceiverInAndroidManifest(
			ArrayList<String> mNeed2CheckReceiverList ,
			ActivityInfo[] mReceiverListInAM ,
			boolean isShutdown )
	{
		if( mReceiverListInAM != null && mNeed2CheckReceiverList.size() > 0 )
		{
			for( int i = 0 ; i < mReceiverListInAM.length ; i++ )
			{
				if( mNeed2CheckReceiverList.contains( mReceiverListInAM[i].name ) )
				{
					mNeed2CheckReceiverList.remove( mReceiverListInAM[i].name );
				}
			}
		}
		if( !mNeed2CheckReceiverList.isEmpty() )
		{
			if( isShutdown )
			{
				throw new RuntimeException( "AM lost <Receiver android:name=\"" + mNeed2CheckReceiverList.get( 0 ) + "\"/>" );
			}
			for( String lost : mNeed2CheckReceiverList )
			{
				Log.v( "CheckUitls" , "lose receiver = " + lost );
			}
			return false;
		}
		return true;
	}
	
	public static boolean checkActivityInAndroidManifest(
			ArrayList<String> mNeed2CheckActivityList ,
			ActivityInfo[] mActivityListInAM ,
			boolean isShutdown )
	{
		if( mActivityListInAM != null && mNeed2CheckActivityList.size() > 0 )
		{
			for( int i = 0 ; i < mActivityListInAM.length ; i++ )
			{
				if( mNeed2CheckActivityList.contains( mActivityListInAM[i].name ) )
				{
					mNeed2CheckActivityList.remove( mActivityListInAM[i].name );
				}
			}
		}
		if( !mNeed2CheckActivityList.isEmpty() )
		{
			if( isShutdown )
			{
				throw new RuntimeException( "AM lost <Activity android:name=\"" + mNeed2CheckActivityList.get( 0 ) + "\"/>" );
			}
			for( String lost : mNeed2CheckActivityList )
			{
				Log.v( "CheckUitls" , "lose activity = " + lost );
			}
			return false;
		}
		return true;
	}
	
	public static boolean checkResource(
			String resources[] ,
			String type ,
			Context context ,
			boolean isShutdown )
	{
		for( String d : resources )
		{
			if( context.getResources().getIdentifier( d , type , context.getPackageName() ) <= 0 )
			{
				if( isShutdown )
				{
					throw new RuntimeException( "AM lost <Resource " + type + "." + d + "\"/>" );
				}
				Log.v( "CheckUitls" , "lose  resource = " + type + "." + d );
				return false;
			}
		}
		return true;
	}
	
	public static boolean checkAssetsResource(
			Context context ,
			String resources[] ,
			boolean isShutdown )
	{
		for( String res : resources )
		{
			int mDirLength = res.lastIndexOf( File.separator );
			String mDir = mDirLength == -1 ? "" : res.substring( 0 , mDirLength );
			String mFileName = res.substring( mDirLength + 1 , res.length() );
			try
			{
				boolean mIsFileExist = false;
				String mDirFiles[] = context.getAssets().list( mDir );
				for( String item : mDirFiles )
				{
					if( item.equals( mFileName ) )
					{
						mIsFileExist = true;
						break;
					}
				}
				if( mIsFileExist == false )
				{
					if( isShutdown )
					{
						throw new RuntimeException( "AM lost <Assets Resource " + res + "/>" );
					}
					Log.v( "CheckUitls" , "lose assets resource = " + res );
					return false;
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		return true;
	}
}
