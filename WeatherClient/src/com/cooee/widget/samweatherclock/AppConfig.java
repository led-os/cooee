package com.cooee.widget.samweatherclock;


import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

import com.cooee.app.cooeeweather.util.CooeeLocationTool;


public class AppConfig
{
	
	private static AppConfig mInstance = null;
	private static Context mContext = null;
	
	public static AppConfig getInstance(
			Context context )
	{
		if( mInstance != null )
		{
			return mInstance;
		}
		AppConfig ins = new AppConfig();
		ins.loadXml( context );
		mContext = context;
		mInstance = ins;
		return mInstance;
	}
	
	private boolean isHuaweiStyle = true;
	private boolean isPosition = true;
	private String defaultCity = "北京";
	private boolean showDisclaimer = true;
	private boolean isDoovShare = false;
	private boolean isMerge = false;
	private int isUpdateWhenOpen = 0;
	
	public boolean isHuaweiStyle()
	{
		return isHuaweiStyle;
	}
	
	public String getDefaultCity()
	{
		if( mContext != null && isPosition() )
		{
			defaultCity = null;
			String locationInfo = CooeeLocationTool.getInstance( mContext ).getLocation();
			if( null != locationInfo )
			{
				Log.i( "weatherDataService" , "locationInfo = " + locationInfo );
				defaultCity = locationInfo.substring( locationInfo.indexOf( "," ) + 1 ).trim();
				Log.i( "weatherDataService" , "getDefaultCity ---defaultCity = " + defaultCity );
			}
		}
		return defaultCity;
	}
	
	private AppConfig()
	{
	}
	
	private void loadXml(
			Context context )
	{
		InputStream xmlStream = null;
		XmlPullParser xmlPull = null;
		try
		{
			xmlStream = context.getAssets().open( "appconfig.xml" );
			xmlPull = XmlPullParserFactory.newInstance().newPullParser();
			xmlPull.setInput( xmlStream , "UTF-8" );
			int eventType = xmlPull.getEventType();
			while( eventType != XmlPullParser.END_DOCUMENT )
			{
				switch( eventType )
				{
					case XmlPullParser.START_TAG:
					{//
						if( "item".equals( xmlPull.getName() ) )
						{
							String itemName = getAttributeValue( xmlPull , "name" , "" );
							String itemValue = getAttributeValue( xmlPull , "value" , "" );
							readItem( itemName , itemValue );
						}
					}
						break;
					default:
						break;
				}
				eventType = xmlPull.next();
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		catch( XmlPullParserException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( xmlStream != null )
			{
				try
				{
					xmlStream.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getAttributeValue(
			XmlPullParser pull ,
			String attName ,
			String defaultValue )
	{
		for( int i = 0 ; i < pull.getAttributeCount() ; i++ )
		{
			if( pull.getAttributeName( i ).equals( attName ) )
			{
				return pull.getAttributeValue( i );
			}
		}
		return defaultValue;
	}
	
	private void readItem(
			String itemName ,
			String itemValue )
	{
		Log.v( "AppConfig" , itemName + "=" + itemValue );
		if( itemName.equals( "isHuaweiStyle" ) )
		{
			isHuaweiStyle = itemValue.equals( "true" );
		}
		else if( "isPosition".equals( itemName ) )
		{
			isPosition = itemValue.equals( "true" );
		}
		else if( "defaultCity".equals( itemName ) )
		{
			defaultCity = itemValue;
		}
		else if( "showDisclaimer".equals( itemName ) )
		{
			showDisclaimer = itemValue.equals( "true" );
		}
		else if( "isDoovShare".equals( itemName ) )
		{
			isDoovShare = itemValue.equals( "true" );
		}
		else if( "isMerge".equals( itemName ) )
		{
			isMerge = itemValue.equals( "true" );
		}
		else if( "isUpdateWhenOpen".equals( itemName ) )
		{
			isUpdateWhenOpen = itemValue.equals( "true" ) ? 1 : 0;
		}
		else
		{
			Log.e( "AppConfig" , "ERROR item:" + itemName + "=" + itemValue );
		}
	}
	
	public boolean isPosition()
	{
		return isPosition;
	}
	
	public boolean showDisclaimer()
	{
		return showDisclaimer;
	}
	
	public boolean isDoovShare()
	{
		return isDoovShare;
	}
	
	public boolean isMerge()
	{
		return isMerge;
	}
	
	public int isUpdateWhenOpen()
	{
		return isUpdateWhenOpen;
	}
}
