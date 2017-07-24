package com.cooee.widget.TranWeatherClock;


import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;


public class AppConfig
{
	
	private static AppConfig mInstance = null;
	
	public static AppConfig getInstance(
			Context context )
	{
		if( mInstance != null )
		{
			return mInstance;
		}
		AppConfig ins = new AppConfig();
		ins.loadXml( context );
		mInstance = ins;
		return mInstance;
	}
	
	private String defaultPackage = null;
	private String defaultClockComp = null;
	
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
		if( "defaultPackage".equals( itemName ) )
		{
			defaultPackage = itemValue;
		}
		else if( "defaultClockComp".equals( itemName ) )
		{
			defaultClockComp = itemValue;
		}
		else
		{
			Log.e( "AppConfig" , "ERROR item:" + itemName + "=" + itemValue );
		}
	}
	
	/**
	 * @return the defaultPackage
	 */
	public String getDefaultPackage()
	{
		return defaultPackage;
	}
	
	/**
	 * @param defaultPackage the defaultPackage to set
	 */
	public void setDefaultPackage(
			String defaultPackage )
	{
		this.defaultPackage = defaultPackage;
	}
	
	public String getDefaultClockComp()
	{
		return defaultClockComp;
	}
}
