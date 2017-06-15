﻿package com.coco.lock2.lockbox.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;

import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Tools;


public class ContentConfig
{
	
	private static final String LOG_TAG = "ContentConfig";
	private DocumentBuilderFactory factory = null;
	private String thumbPath = "";
	private String previewPath = "";
	private String[] previewFiles = new String[]{};
	private String lockStyleValue = "";
	private String settingClassName = "";
	private String packageName = "";
	private String applicationName = "";
	private int versionCode = 0;
	private String versionName = "";
	private long applicationSize = 0;
	private String author = "";
	private String introduction = "";
	private String updateTime = "";
	private String applicationName_en = "";
	private String introduction_en = "";
	private String wrapName = "";
	private String thirdPackageName = "";//锁屏配置的第三方应用包名
	private String thirdClassName = "";
	private boolean reflection = false;
	private String backgroundPathString = "";
	
	public boolean getReflection()
	{
		return reflection;
	}
	
	public boolean isSettingExist()
	{
		return !settingClassName.equals( "" );
	}
	
	public String getSettingClassName()
	{
		return settingClassName;
	}
	
	public String getLockStyleValue()
	{
		return lockStyleValue;
	}
	
	public int getVersionCode()
	{
		return versionCode;
	}
	
	public String getVersionName()
	{
		return versionName;
	}
	
	public String getPackageName()
	{
		return packageName;
	}
	
	public String getThirdPackageName()
	{
		return thirdPackageName;
	}
	
	public String getThirdClassName()
	{
		return thirdClassName;
	}
	
	public String getApplicationName()
	{
		return applicationName;
	}
	
	public String getApplicationName_en()
	{
		return applicationName_en;
	}
	
	public long getApplicationSize()
	{
		return applicationSize;
	}
	
	public String getAuthor()
	{
		return author;
	}
	
	public String getIntroduction()
	{
		return introduction;
	}
	
	public String getIntroduction_en()
	{
		return introduction_en;
	}
	
	public String getUpdateTime()
	{
		return updateTime;
	}
	
	public String getWrapName()
	{
		return wrapName;
	}
	
	public String getBackgroundPathString()
	{
		return backgroundPathString;
	}
	
	public boolean saveThumb(
			Context remoteContext ,
			String filePath )
	{
		InputStream thumbStream = null;
		FileOutputStream fileOut = null;
		byte[] buffer = new byte[10 * 1024];
		try
		{
			fileOut = new FileOutputStream( filePath );
			thumbStream = remoteContext.getAssets().open( thumbPath );
			int readSize = thumbStream.read( buffer );
			while( readSize > 0 )
			{
				fileOut.write( buffer , 0 , readSize );
				readSize = thumbStream.read( buffer );
			}
			return true;
		}
		catch( FileNotFoundException e )
		{
			e.printStackTrace();
			return false;
		}
		catch( IOException e )
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			if( thumbStream != null )
			{
				try
				{
					thumbStream.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
			if( fileOut != null )
			{
				try
				{
					fileOut.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public Bitmap loadThumbImage(
			Context remoteContext )
	{
		Bitmap result = null;
		try
		{
			result = Tools.getPurgeableBitmap(
					remoteContext.getAssets().open( thumbPath ) ,
					Tools.dip2px( remoteContext , FunctionConfig.getGridWidth() ) ,
					Tools.dip2px( remoteContext , FunctionConfig.getGridHeight() ) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public Bitmap loadPreviewImage(
			Context remoteContext ,
			int index )
	{
		if( index < 0 || index >= previewFiles.length )
		{
			return null;
		}
		Bitmap result = null;
		try
		{
			DisplayMetrics dis = remoteContext.getResources().getDisplayMetrics();
			result = Tools.getPurgeableBitmap( remoteContext.getAssets().open( previewPath + "/" + previewFiles[index] ) , dis.widthPixels / 2 , dis.heightPixels / 2 );
		}
		catch( IOException e )
		{
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	
	public int getPreviewArrayLength()
	{
		return previewFiles.length;
	}
	
	public void reset()
	{
		thumbPath = "";
		previewPath = "";
		previewFiles = new String[]{};
		lockStyleValue = "";
		settingClassName = "";
		versionCode = 0;
		versionName = "";
		packageName = "";
		thirdPackageName = "";
		thirdClassName = "";
		applicationName = "";
		applicationSize = 0;
		author = "";
		introduction = "";
		updateTime = "";
		applicationName_en = "";
		introduction_en = "";
		reflection = false;
		backgroundPathString = "";
	}
	
	public boolean loadConfig(
			Context remoteContext ,
			String className )
	{
		Log.d( LOG_TAG , "loadConfig=" + className );
		String internal = "";
		reset();
		if( factory == null )
		{
			factory = DocumentBuilderFactory.newInstance();
		}
		InputStream configStream = null;
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			configStream = remoteContext.getAssets().open( "config.xml" );
			Document doc = builder.parse( configStream );
			Element rootElement = doc.getDocumentElement();
			Element parentElement = rootElement;
			List<Node> appsList = getChildNodeList( parentElement , "app" );
			for( int i = 0 ; i < appsList.size() ; i++ )
			{
				Node appNode = appsList.get( i );
				if( appNode instanceof Element )
				{
					Element ele = (Element)appNode;
					if( className.equals( ele.getAttribute( "className" ) ) )
					{
						parentElement = ele;
						break;
					}
				}
			}
			internal = getAttributeValue( parentElement , "reflection" , "value" );
			if( internal.equals( "true" ) && isSystemApp( remoteContext ) )
			{
				reflection = true;
			}
			else
			{
				reflection = false;
			}
			try
			{
				backgroundPathString = getAttributeValue( parentElement , "background" , "path" );
			}
			catch( Exception e )
			{
				// TODO: handle exception
				backgroundPathString = "";
			}
			thumbPath = getAttributeValue( parentElement , "thumb" , "path" );
			if( thumbPath.equals( "" ) )
			{
				return false;
			}
			previewPath = getAttributeValue( parentElement , "preview" , "path" );
			if( previewPath.equals( "" ) )
			{
				return false;
			}
			try
			{
				previewFiles = remoteContext.getAssets().list( previewPath );
			}
			catch( IOException e1 )
			{
				e1.printStackTrace();
				return false;
			}
			lockStyleValue = getAttributeValue( parentElement , "settinglockstyle" , "value" );
			settingClassName = getAttributeValue( parentElement , "setting" , "className" );
			thirdPackageName = getAttributeValue( parentElement , "activity" , "packageName" );
			thirdClassName = getAttributeValue( parentElement , "activity" , "className" );
			wrapName = getAttributeValue( parentElement , "viewWrap" , "className" );
			author = getAttributeValue( parentElement , "author" , "name" );
			updateTime = getAttributeValue( parentElement , "updatetime" , "value" );
			introduction = getElementValue( parentElement , "introduction" );
			introduction_en = getElementValue( parentElement , "introductionEn" );
			applicationName_en = remoteContext.getPackageName();
			packageName = remoteContext.getPackageName();
			PackageInfo pkgInfo = remoteContext.getPackageManager().getPackageInfo( packageName , 0 );
			versionCode = pkgInfo.versionCode;
			versionName = pkgInfo.versionName;
			ApplicationInfo appInfo = pkgInfo.applicationInfo;
			applicationName = remoteContext.getPackageManager().getApplicationLabel( appInfo ).toString();
			applicationSize = new File( appInfo.publicSourceDir ).length();
			return true;
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		catch( ParserConfigurationException e )
		{
			e.printStackTrace();
		}
		catch( SAXException e )
		{
			e.printStackTrace();
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( configStream != null )
			{
				try
				{
					configStream.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	private String getAttributeValue(
			Element parent ,
			String tagName ,
			String attName )
	{
		Element ele = getChildElementByTag( parent , tagName );
		if( ele == null )
		{
			return "";
		}
		attName = ele.getAttribute( attName );
		return attName;
	}
	
	private String getElementValue(
			Element parent ,
			String tagName )
	{
		Element ele = getChildElementByTag( parent , tagName );
		if( ele == null )
		{
			return "";
		}
		return ele.getTextContent();
	}
	
	private Element getChildElementByTag(
			Element parent ,
			String name )
	{
		List<Node> nodeList = getChildNodeList( parent , name );
		if( nodeList.size() <= 0 )
		{
			return null;
		}
		Node node = nodeList.get( 0 );
		if( node instanceof Element )
		{
			return (Element)node;
		}
		return null;
	}
	
	private List<Node> getChildNodeList(
			Element parent ,
			String name )
	{
		List<Node> ret = new ArrayList<Node>();
		NodeList childList = parent.getChildNodes();
		for( int i = 0 ; i < childList.getLength() ; i++ )
		{
			Node node = childList.item( i );
			if( node.getNodeName().equals( name ) )
			{
				ret.add( node );
			}
		}
		return ret;
	}
	
	private boolean isSystemApp(
			Context context )
	{
		if( ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM ) != 0 || ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
		{
			Log.d( "StartReceiver" , "isSystemApp" );
			return true;
		}
		Log.d( "StartReceiver" , "not SystemApp" );
		return false;
	}
}
