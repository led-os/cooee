package com.cooee.framework.function.DynamicEntry.DLManager;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.DynamicEntry.DynamicEntryLog;


public class StatisticsHandle
{
	
	private static final String TAG = "StatisticsHandle";
	
	//是否是虚链接
	public static boolean isOperateWebLink(
			Intent intent )
	{
		if( intent == null )
			return false;
		if( intent.getIntExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , 0 ) == OperateDynamicUtils.VIRTUAL_LINK )
		{
			return true;
		}
		return false;
	}
	
	//是否是虚应用
	public static boolean isOperateVirtualApp(
			Intent intent )
	{
		if( intent == null )
			return false;
		if( intent.getIntExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , 0 ) == OperateDynamicUtils.VIRTUAL_APP )
		{
			return true;
		}
		return false;
	}
	
	public static void DynamicEntryClick(
			final String mName ,
			final Intent intent )
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				Context context = BaseAppState.getActivityInstance();
				if( context == null || intent == null )
				{
					return;
				}
				int mID = -1;
				//虚链接
				if( isOperateWebLink( intent ) )
				{
					String id = intent.getStringExtra( "dynamicID" );
					if( id != null )
						mID = Integer.parseInt( id );
				}
				//未安装,非文件夹里面的虚应用
				if( isOperateVirtualApp( intent ) )
				{
					String id = intent.getStringExtra( "dynamicID" );
					if( id != null )
						mID = Integer.parseInt( id );
				}
				if( mID != -1 && mName != null )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "entry Click Name:" , mName ) );
					DynamicEntryLog.LogDynamicEntryClick( context , mID , mName , 1 );
				}
			}
		} ).start();
	}
	
	public static void DynamicEntryClick(
			final String name ,
			final int dynamicID )
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				Context context = BaseAppState.getActivityInstance();
				if( name != null )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "entry Folder Click Name:" , name ) );
					DynamicEntryLog.LogDynamicEntryClick( context , dynamicID , name , 1 );
				}
			}
		} ).start();
	}
	
	public static void DynamicEntryDelete(
			final String name ,
			final int dynamicID )
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				Context context = BaseAppState.getActivityInstance();
				if( context == null || name == null )
				{
					return;
				}
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , StringUtils.concat( "entry Folder Delete Name:" , name ) );
				DynamicEntryLog.LogDynamicEntryDelete( context , OperateDynamicUtils.FOLDER , dynamicID , name , null );
			}
		} ).start();
	}
	
	public static void DynamicEntryDelete(
			final Intent intent ,
			final String mName )
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				Context context = BaseAppState.getActivityInstance();
				if( context == null || intent == null )
				{
					return;
				}
				int mID = -1;
				int mType = -1;
				String mPkgName = null;
				//虚链接
				if( isOperateWebLink( intent ) )
				{
					String id = intent.getStringExtra( "dynamicID" );
					if( id != null )
						mID = Integer.parseInt( id );
					mPkgName = intent.getStringExtra( "pkgName" );
					mType = OperateDynamicUtils.VIRTUAL_LINK;
				}
				//未安装,非文件夹里面的虚应用
				if( isOperateVirtualApp( intent ) )
				{
					String id = intent.getStringExtra( "dynamicID" );
					if( id != null )
						mID = Integer.parseInt( id );
					mPkgName = intent.getStringExtra( "pkgName" );
					mType = OperateDynamicUtils.VIRTUAL_APP;
				}
				if( mID != -1 && mName != null && mPkgName != null && mType != -1 )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "entry  Delete Name:" , mName , "-type:" , mType ) );
					DynamicEntryLog.LogDynamicEntryDelete( context , mType , mID , mName , mPkgName );
				}
			}
		} ).start();
	}
}
