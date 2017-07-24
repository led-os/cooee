package com.cooee.framework.function.Category;


import java.util.Collection;
import java.util.Map;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;

import cool.sdk.Category.CategoryConstant;
import cool.sdk.Category.CategoryHelper;
import cool.sdk.Category.RecommendApkInfo;
import cool.sdk.Category.RecommendInfo;


public class CategoryParse
{
	
	private static CategoryParse instance;
	public static SharedPreferences mPreferences;
	public final static int CATEGORY_NOTIFYID = 30141012;
	private Object mLock = new Object();
	
	public static CategoryParse getInstance()
	{
		if( instance == null )
		{
			synchronized( CategoryParse.class )
			{
				if( instance == null )
				{
					instance = new CategoryParse();
					if( mPreferences == null )
					{
						mPreferences = BaseAppState.getActivityInstance().getSharedPreferences( CategoryConstant.PREFERENCE_KEY , Context.MODE_PRIVATE );
					}
				}
			}
		}
		return instance;
	}
	
	public void init()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( CategoryConstant.TAG , "CategoryParse - init" );
		//创建一个线程，从数据库中获取分类所需的MAP
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				CategoryHelper.getInstance( BaseAppState.getActivityInstance().getApplicationContext() ).UpdateMapData();
			}
		} ).start();
	}
	
	public void showNotify()
	{
		String title = BaseDefaultConfig.getString( R.string.scattered_icon );
		String subTitle = BaseDefaultConfig.getString( R.string.tap_icon );
		Intent intent = new Intent();
		intent.setClassName( BaseAppState.getActivityInstance() , CategoryConstant.EMPTY_ACTIVITY );
		PendingIntent contentIntent = PendingIntent.getActivity( BaseAppState.getActivityInstance() , 0 , intent , 0 );
		// gaominghui@2016/12/14 ADD START
		Notification notification;
		Builder notificationBuilder = new Notification.Builder( BaseAppState.getActivityInstance() ).setSmallIcon( OperateDynamicProxy.getLauncherIcon() )//小图标
				.setTicker( title )//和小图标一起出的提示语
				.setContentTitle( title )//主标题
				.setContentText( subTitle )//副标题
				.setContentIntent( contentIntent );
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
		{
			notification = notificationBuilder.build();
		}
		else
		{
			notification = notificationBuilder.getNotification();
		}
		// gaominghui@2016/12/14 ADD END
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		NotificationManager mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.cancel( CATEGORY_NOTIFYID );
		mNotificationManager.notify( CATEGORY_NOTIFYID , notification );
	}
	
	private void showCategoryNotification()
	{
		long curTime = System.currentTimeMillis();
		long prompt_time = mPreferences.getLong( CategoryConstant.CATEGORY_PROMPT_TIME , 0 );
		int prompt_count = mPreferences.getInt( CategoryConstant.CATEGORY_PROMPT_COUNT , 0 );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "category" , StringUtils.concat( "category curTime:" , curTime , "-prompt_time:" , prompt_time , "-prompt_count:" , prompt_count ) );
		if( prompt_count < 15 && Math.abs( curTime - prompt_time ) > 24 * 60 * 60 * 1000 )
		{
			showCategoryNotificationContent();
			mPreferences.edit().putLong( CategoryConstant.CATEGORY_PROMPT_TIME , curTime ).commit();
			mPreferences.edit().putInt( CategoryConstant.CATEGORY_PROMPT_COUNT , ++prompt_count ).commit();
			mPreferences.edit().putBoolean( CategoryConstant.CATEGORY_NOTIFY , true ).commit();
		}
	}
	
	public void cancelNotify()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( CategoryConstant.TAG , "CategoryParse - cancelNotify" );
		NotificationManager mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( BaseAppState.getActivityInstance().NOTIFICATION_SERVICE );
		mNotificationManager.cancel( CATEGORY_NOTIFYID );
		mPreferences.edit().putBoolean( CategoryConstant.CATEGORY_NOTIFY , false ).commit();
	}
	
	private void showCategoryNotificationContent()
	{
		int[] title_id = { R.string.notification_title_0 , R.string.notification_title_1 , R.string.notification_title_2 , R.string.notification_title_3 , R.string.notification_title_4 };
		int[] subtitle_id = {
				R.string.notification_subtitle_0 ,
				R.string.notification_subtitle_1 ,
				R.string.notification_subtitle_2 ,
				R.string.notification_subtitle_3 ,
				R.string.notification_subtitle_4 };
		int index = (int)( Math.random() * 5 );
		String title = BaseDefaultConfig.getString( title_id[index] );
		String subTitle = BaseDefaultConfig.getString( subtitle_id[index] );
		Intent intent = new Intent();
		intent.putExtra( CategoryConstant.CATEGORY_PROMPT , 1 );
		intent.setClassName( BaseAppState.getActivityInstance() , CategoryConstant.EMPTY_ACTIVITY );
		PendingIntent contentIntent = PendingIntent.getActivity( BaseAppState.getActivityInstance() , 0 , intent , 0 );
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		Notification notification;
		Builder notificationBuilder = new Notification.Builder( BaseAppState.getActivityInstance() ).setSmallIcon( OperateDynamicProxy.getLauncherIcon() )//小图标
				.setTicker( title )//和小图标一起出的提示语
				.setContentTitle( title )//主标题
				.setContentText( subTitle )//副标题
				.setContentIntent( contentIntent );
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
		{
			notification = notificationBuilder.build();
		}
		else
		{
			notification = notificationBuilder.getNotification();
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		NotificationManager mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.cancel( CATEGORY_NOTIFYID );
		mNotificationManager.notify( CATEGORY_NOTIFYID , notification );
	}
	
	public void showCategoryNotifyResume()
	{
		if( mPreferences.getBoolean( CategoryConstant.CATEGORY_NOTIFY , false ) )
		{
			showCategoryNotificationContent();
		}
	}
	
	public void addOrRemoveCategoryEntry(
			boolean isAdd )
	{
		if( !isAdd )
		{
			cancelNotify();
		}
		CategoryHelper.getInstance( BaseAppState.getActivityInstance() ).addOrRemoveCategoryEntry( isAdd );
	}
	
	public void bgRequestComplete(
			Context context )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "category" , "category---bgRequestComplete!" );
		final boolean isCanCategory = CategoryHelper.getInstance( BaseAppState.getActivityInstance() ).canDoCategory();
		if( BaseDefaultConfig.CONFIG_LAUNCHER_STYLE == BaseDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			if( BaseDefaultConfig.CONFIG_CATEGORY_TYPE == CategoryConstant.OPERATE_CATEGORY )
			{
				if( !isCanCategory )
				{
					cancelNotify();
				}
				if( getBackgroundSwitch() != isCanCategory )
				{
					setCategoryBackgroundSwitch( isCanCategory );
					CategoryHelper.getInstance( BaseAppState.getActivityInstance() ).addOrRemoveCategoryEntry( isCanCategory );
				}
			}
			if( !CategoryHelper.getInstance( context ).isCategoryState() && isCanCategory )
			{
				( BaseAppState.getActivityInstance() ).runOnUiThread( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						if(
						//
						isCanCategory
						//
						&& ( BaseDefaultConfig.SWITCH_ENABLE_CATEGORY_SHOW_NOTIFICATION /* //xiatian add	//智能分类功能开启后，是否允许在通知栏显示建议智能分类的通知。 true为允许显示通知，false为不允许显示通知。默认为true。 */)
						//
						)
						{
							showCategoryNotification();
						}
					}
				} );
			}
		}
	}
	
	public int getCategoryAppID(
			String pkgName )
	{
		if( pkgName != null )
		{
			CategoryHelper helper = CategoryHelper.getInstance( BaseAppState.getActivityInstance() );
			if( helper.isCategoryState() )
			{
				Map<Integer , RecommendInfo> map = helper.RecommendInfoMap;
				if( map == null )
				{
					return helper.getRecommendInfoFolderId( pkgName );
				}
				else
				{
					Collection<RecommendInfo> collection = map.values();
					for( RecommendInfo info : collection )
					{
						Collection<RecommendApkInfo> collectionItems = info.getApkinfoMap().values();
						for( RecommendApkInfo apkInfo : collectionItems )
						{
							if( apkInfo.getPkgName().equals( pkgName ) )
							{
								return info.getFolderID();
							}
						}
					}
				}
			}
		}
		return CategoryConstant.UN_KNOW;
	}
	
	public static boolean getBackgroundSwitch()
	{
		if( mPreferences == null )
		{
			mPreferences = BaseAppState.getActivityInstance().getSharedPreferences( CategoryConstant.PREFERENCE_KEY , Context.MODE_PRIVATE );
		}
		if( mPreferences != null )
		{
			return mPreferences.getBoolean( CategoryConstant.CATEGORY_BACKGROUND_SWITCH , false );
		}
		return false;
	}
	
	public static boolean canShowCategory()
	{
		if( BaseDefaultConfig.CONFIG_CATEGORY_TYPE == CategoryConstant.CANNOT_CATEGORY )
		{
			return false;
		}
		if( BaseDefaultConfig.CONFIG_CATEGORY_TYPE == CategoryConstant.CAN_CATEGORY )
		{
			return true;
		}
		if( BaseDefaultConfig.CONFIG_CATEGORY_TYPE == CategoryConstant.OPERATE_CATEGORY )
		{
			return getBackgroundSwitch();
		}
		return true;
	}
	
	public static void processCategoryEntry()
	{
		if( BaseDefaultConfig.CONFIG_LAUNCHER_STYLE == BaseDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			if( BaseDefaultConfig.CONFIG_CATEGORY_TYPE == CategoryConstant.OPERATE_CATEGORY )
			{
				// TODO Auto-generated method stub
				boolean tem = CategoryHelper.getInstance( BaseAppState.getActivityInstance() ).canDoCategory();
				if( getBackgroundSwitch() != tem )
				{
					setCategoryBackgroundSwitch( tem );
					CategoryHelper.getInstance( BaseAppState.getActivityInstance() ).addOrRemoveCategoryEntry( tem );
				}
			}
		}
	}
	
	public static void setCategoryBackgroundSwitch(
			boolean can )
	{
		if( mPreferences == null )
		{
			mPreferences = BaseAppState.getActivityInstance().getSharedPreferences( CategoryConstant.PREFERENCE_KEY , Context.MODE_PRIVATE );
		}
		mPreferences.edit().putBoolean( CategoryConstant.CATEGORY_BACKGROUND_SWITCH , can ).commit();
	}
	
	public static void categoryOnResume()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( !BaseAppState.isAlreadyCategory( BaseAppState.getActivityInstance() ) )
				{
					processCategoryEntry();
				}
			}
		} ).start();
	}
}
