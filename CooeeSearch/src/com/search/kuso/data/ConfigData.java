package com.search.kuso.data;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


public class ConfigData
{
	
	private static final String DEFAULT_LAYOUT_FILENAME_EXTERNAL = Environment.getRootDirectory() + "/launcher/kuso_app_default.xml";
	//	private static final String DEFAULT_LAYOUT_FILENAME = "kuso/kuso_default.xml";
	private static final Object GENERAl_CONFIG = "general_config";
	public static boolean engine_CH = true;
	public static String engine[] = { "http://m.yz.sm.cn/s?from=wy200848&q=" , "https://www.google.com/search?q=" };
	public static String engine_haosou[] = { "http://m.haosou.com/s?src=home&srcg=cs_boruizhiheng_2&q=" , "https://www.google.com/search?q=" };
	
	public ConfigData(
			Context context )
	{
		//		LoadConfigXml( context );
	}
	
	final private void LoadConfigXml(
			Context context )
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			SAXParser parser = factory.newSAXParser();
			XMLReader xmlreader = parser.getXMLReader();
			MyConfigHandler handler = new MyConfigHandler();
			xmlreader.setContentHandler( handler );
			InputSource xmlin;
			File f = new File( DEFAULT_LAYOUT_FILENAME_EXTERNAL );
			Log.i( "data" , "path " + DEFAULT_LAYOUT_FILENAME_EXTERNAL );
			//			checkSDcard();
			if( f.exists() )
			{
				Log.i( "data" , "loading config form sdcard" );
				FileInputStream fileInputStream = new FileInputStream( f );
				xmlin = new InputSource( fileInputStream );
				xmlreader.parse( xmlin );
				handler = null;
				xmlin = null;
			}
			else
			{
				Log.i( "data" , "loading config form apk" );
				//				xmlin = new InputSource( context.getAssets().open( DEFAULT_LAYOUT_FILENAME ) );
				//				xmlreader.parse( xmlin );
				handler = null;
				xmlin = null;
			}
			// Utils3D.showPidMemoryInfo("default2");
		}
		catch( ParserConfigurationException e )
		{
			e.printStackTrace();
		}
		catch( SAXException e )
		{
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	class MyConfigHandler extends DefaultHandler
	{
		
		private void configGeneral(
				Attributes atts )
		{
			String temp = atts.getValue( "engine_CH" );
			if( temp != null && temp.equals( "true" ) )
			{
				engine_CH = true;
			}
			else
			{
				engine_CH = false;
			}
		}
		
		public void startElement(
				String namespaceURI ,
				String localName ,
				String qName ,
				Attributes atts ) throws SAXException
		{
			if( localName.equals( "resources" ) )
			{
			}
			else if( localName.equals( GENERAl_CONFIG ) )
			{
				configGeneral( atts );
			}
		}
	}
}
