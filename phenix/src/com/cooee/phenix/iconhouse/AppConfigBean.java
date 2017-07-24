package com.cooee.phenix.iconhouse;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.cooee.phenix.config.defaultConfig.DefaultIcon;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/**
 * @author zhangjin
 *此类用来从对应的数据中解析，应用配置数据，可以用来进行图标替换或者动态图标
 */
public class AppConfigBean
{
	
	public ArrayList<DefaultIcon> mAppList = new ArrayList<DefaultIcon>();
	
	public AppConfigBean(
			Context context ,
			int titlelistId ,
			int imageListId ,
			int pkgListId ,
			int classListId ,
			int houseProviderId )
	{
		//cheyingkun add start	//解决“和兴一部桑飞项目，刷机第一次开机默认配置文件夹中图标缺失”的问题。【c_0003400】
		String[] iconTitle = null;
		try
		{
			iconTitle = LauncherDefaultConfig.getStringArray( titlelistId );
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "cyk_bug : c_0003400" , "AppConfigBean: LauncherDefaultConfig.getStringArray( titlelistId )" );
			new Throwable().printStackTrace();
		}
		if( iconTitle == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "cyk_bug : c_0003400" , "AppConfigBean:  iconTitle == null  return " );
			return;
		}
		//cheyingkun add end
		String[] iconImage;
		try
		{
			iconImage = LauncherDefaultConfig.getStringArray( imageListId );
		}
		catch( Exception e )
		{
			iconImage = new String[iconTitle.length];
		}
		String[] iconPackageName;
		try
		{
			iconPackageName = LauncherDefaultConfig.getStringArray( pkgListId );
		}
		catch( Exception e )
		{
			iconPackageName = new String[iconTitle.length];
		}
		String[] iconClassName;
		try
		{
			iconClassName = LauncherDefaultConfig.getStringArray( classListId );
		}
		catch( Exception e )
		{
			iconClassName = new String[iconTitle.length];
		}
		String[] iconHouseProvider;
		try
		{
			iconHouseProvider = LauncherDefaultConfig.getStringArray( houseProviderId );
		}
		catch( Exception e )
		{
			iconHouseProvider = new String[iconTitle.length];
		}
		for( int i = 0 ; i < iconTitle.length ; i++ )
		{
			DefaultIcon temp = new DefaultIcon();
			temp.title = iconTitle[i];
			Iterator<DefaultIcon> ite = mAppList.iterator();
			while( ite.hasNext() )
			{
				DefaultIcon icon = ite.next();
				if( icon.title.equals( temp.title ) )
					ite.remove();
			}
			temp.imageName = iconImage[i];
			temp.pkgName = iconPackageName[i];
			temp.className = iconClassName[i];
			if( temp.className.equals( "noting" ) )
				temp.className = "";
			temp.houseProvider = iconHouseProvider[i];
			mAppList.add( temp );
		}
		setupList( mAppList );
	}
	
	public void setupList(
			List<DefaultIcon> mDefaultIcon )
	{
		int i = 0;
		int size = mDefaultIcon.size();
		if( size < 1 )
			return;
		for( i = 0 ; i < size ; i++ )
		{
			mDefaultIcon.get( i ).pkgNameArray = splitString( ";" , mDefaultIcon.get( i ).pkgName );
			mDefaultIcon.get( i ).classNameArray = splitString( ";" , mDefaultIcon.get( i ).className );
		}
	}
	
	/**
	 * 通过分隔符拆�?返回List
	 * @param regularExpression 分隔�?
	 * @param allName 要拆分的字符�?
	 * @return 拆分后的结果
	 */
	public List<String> splitString(
			String regularExpression ,
			String allName )
	{
		List<String> stringArray = new ArrayList<String>();
		if( allName == null || allName.length() <= 0 )
		{
			return null;
		}
		String[] result = allName.split( regularExpression );
		if( result.length <= 0 )
		{
			return null;
		}
		else
		{
			stringArray = Arrays.asList( result );
			return stringArray;
		}
	}
	
	public ArrayList<DefaultIcon> getAppList()
	{
		return mAppList;
	}
}
