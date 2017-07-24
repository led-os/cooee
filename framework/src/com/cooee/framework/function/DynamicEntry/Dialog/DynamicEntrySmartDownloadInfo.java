package com.cooee.framework.function.DynamicEntry.Dialog;


import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;


public class DynamicEntrySmartDownloadInfo
{
	
	private String pkgName;
	private String title;
	private String path;
	private Bitmap bitmap;
	private int size;
	private int downloadType = OperateDynamicUtils.NORMAL_DOWNLOAD;
	
	public DynamicEntrySmartDownloadInfo()
	{
	}
	
	public DynamicEntrySmartDownloadInfo(
			String pkgName ,
			String title ,
			String path )
	{
		this.pkgName = pkgName;
		this.title = title;
		this.path = path;
	}
	
	public DynamicEntrySmartDownloadInfo(
			String pkgName ,
			String title ,
			String path ,
			Bitmap bitmap )
	{
		this( pkgName , title , path );
		this.bitmap = bitmap;
	}
	
	public DynamicEntrySmartDownloadInfo(
			String pkgName ,
			String title ,
			String path ,
			int size )
	{
		this( pkgName , title , path );
		this.size = size;
	}
	
	public DynamicEntrySmartDownloadInfo(
			String pkgName ,
			String title ,
			String path ,
			int size ,
			int downloadType )
	{
		this( pkgName , title , path , size );
		this.setDownloadType( downloadType );
	}
	
	public String getPkgName()
	{
		return pkgName;
	}
	
	public void setPkgName(
			String pkgName )
	{
		this.pkgName = pkgName;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(
			String title )
	{
		this.title = title;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public void setPath(
			String path )
	{
		this.path = path;
	}
	
	public Bitmap getBitmap()
	{
		return bitmap;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public void setSize(
			int size )
	{
		this.size = size;
	}
	
	public JSONObject toJSON()
	{
		JSONObject res = new JSONObject();
		try
		{
			res.put( "pkgName" , pkgName );
			res.put( "title" , title );
			res.put( "path" , path );
			res.put( "size" , size );
			res.put( "downloadType" , downloadType );
			return res;
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public int getDownloadType()
	{
		return downloadType;
	}
	
	public void setDownloadType(
			int downloadType )
	{
		this.downloadType = downloadType;
	}
}
