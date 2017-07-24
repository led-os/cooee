package com.cooee.framework.utils;


import android.content.res.Resources;

import com.cooee.framework.app.BaseAppState;


public class ResourceUtils
{
	
	public static boolean isNeedGetResourceIdByReflect(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{
		// TODO
		//待完善
		return true;
	}
	
	//R.anim
	public static int getAnimResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.181毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "anim" , mPackageName );
	}
	
	//R.array
	public static int getArrayResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.157毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "array" , mPackageName );
	}
	
	//
	//R.attr
	//	public static int getAttrResourceIdByReflect(
	//			Resources mResource ,
	//			String mPackageName ,
	//			String mResourceName )
	//	{
	//		return mResource.getIdentifier( mResourceName , "attr" , mPackageName );
	//	}
	//
	//R.bool
	public static int getBoolResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.049毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "bool" , mPackageName );
	}
	
	//R.color
	public static int getColorResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.068毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "color" , mPackageName );
	}
	
	//R.dimen
	public static int getDimenResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.033毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "dimen" , mPackageName );
	}
	
	//R.drawable
	public static int getDrawableResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.083毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "drawable" , mPackageName );
	}
	
	//R.id
	public static int getIdResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.051毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "id" , mPackageName );
	}
	
	//R.integer
	public static int getIntegerResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.050毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "integer" , mPackageName );
	}
	
	//R.layout
	public static int getLayoutResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.086毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "layout" , mPackageName );
	}
	
	//R.mipmap
	public static int getMipmapResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.055毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "mipmap" , mPackageName );
	}
	
	//R.string
	public static int getStringResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.065毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "string" , mPackageName );
	}
	
	//R.style	
	public static int getStyleResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.052毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "style" , mPackageName );
	}
	
	//R.styleable
	public static int getStyleableResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.024毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "styleable" , mPackageName );
	}
	
	//R.xml
	public static int getXmlResourceIdByReflectIfNecessary(
			int mResourceId ,
			Resources mResource ,
			String mPackageName ,
			String mResourceName )
	{//反射取资源id耗时（运行1000次，得出的平均值）：0.066毫秒
		if( !isNeedGetResourceIdByReflect( mResourceId , mResource , mResourceName , mPackageName ) )
		{
			return mResourceId;
		}
		return mResource.getIdentifier( mResourceName , "xml" , mPackageName );
	}
	
	public static String getStringByReflectIfNecessary(
			String mResourceName )
	{
		if( BaseAppState.getActivityInstance() != null )
		{
			Resources mResources = BaseAppState.getActivityInstance().getResources();
			int id = getStringResourceIdByReflectIfNecessary( 0 , mResources , BaseAppState.getActivityInstance().getPackageName() , mResourceName );
			if( id > 0 )
			{
				return mResources.getString( id );
			}
		}
		return null;
	}
}
