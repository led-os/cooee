package com.cooee.framework.config;


// MusicPage CameraPage
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;


public class ConfigUtils
{
	
	private ConfigHandler handler = null;
	public final static int FROM_ASSET = 0;
	public final static int FROM_PHONE = 1;
	private Context mContext;
	
	public ConfigUtils()
	{
	}
	
	public void loadConfig(
			Context context ,
			String configPath )
	{
		loadConfig( context , configPath , FROM_ASSET );
	}
	
	public void loadConfig(
			Context context ,
			String configPath ,
			int from )
	{
		mContext = context;
		InputStream xmlInputStream = null;
		if( from == FROM_ASSET )
		{
			//zhujieping add start //客户手机出现重启log，根据log修改
			try
			{
				if( configPath.startsWith( "assets/" ) )
					configPath = configPath.substring( 7 , configPath.length() );
				xmlInputStream = context.getAssets().open( configPath );
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//			Thread.currentThread().getContextClassLoader().getResourceAsStream( configPath );
			//zhujieping add end
		}
		else
		{
			try
			{
				xmlInputStream = new FileInputStream( configPath );
			}
			catch( FileNotFoundException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if( xmlInputStream != null )
		{
			SAXParserFactory factoey = SAXParserFactory.newInstance();
			try
			{
				SAXParser parser = factoey.newSAXParser();
				XMLReader xmlreader = parser.getXMLReader();
				handler = new ConfigHandler( context );
				xmlreader.setContentHandler( handler );
				InputSource xmlin = new InputSource( xmlInputStream );
				xmlreader.parse( xmlin );
				xmlin = null;
				xmlInputStream.close();
				xmlInputStream = null;
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
	}
	
	public int getInteger(
			String name )
	{
		return getInteger( name , 0 );
	}
	
	public int getInteger(
			String name ,
			int defaultValue )
	{
		if( handler != null && handler.getmInteger().containsKey( name ) )
		{
			return handler.getmInteger().get( name );
		}
		else
		{
			return defaultValue;
		}
	}
	
	public int getDimensionPixelOffset(
			String name ,
			int defaultValue )
	{
		if( handler != null && handler.getmDimen().containsKey( name ) )
		{
			return (int)( handler.getmDimen().get( name ).value + 0.5f );
		}
		else
		{
			return defaultValue;
		}
	}
	
	public float getDimension(
			String name ,
			float defaultValue )
	{
		if( handler != null && handler.getmDimen().containsKey( name ) )
		{
			return handler.getmDimen().get( name ).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	public float getDimension(
			String name )
	{
		return getDimension( name , 0f );
	}
	
	public int getDimensionPixelOffset(
			String name )
	{
		return getDimensionPixelOffset( name , 0 );
	}
	
	public float getFloat(
			String name )
	{
		return getFloat( name , 0 );
	}
	
	public float getFloat(
			String name ,
			float defaultValue )
	{
		if( handler != null && handler.getmFloat().containsKey( name ) )
		{
			return handler.getmFloat().get( name );
		}
		else
		{
			return defaultValue;
		}
	}
	
	public String getString(
			String name )
	{
		return getString( name , null );
	}
	
	public String getString(
			String name ,
			String defaultValue )
	{
		if( handler != null && handler.getmString().containsKey( name ) )
		{
			return handler.getmString().get( name ).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	public boolean getBoolean(
			String name )
	{
		return getBoolean( name , false );
	}
	
	public boolean getBoolean(
			String name ,
			boolean defaultValue )
	{
		if( handler != null && handler.getmBoolean().containsKey( name ) )
		{
			return handler.getmBoolean().get( name ).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	public ArrayList<String> getStringArray(
			String name ,
			ArrayList<String> defaultValue )
	{
		if( handler != null && handler.getmStringArray().containsKey( name ) )
		{
			return handler.getmStringArray().get( name );
		}
		else
		{
			return defaultValue;
		}
	}
	
	public ArrayList<String> getStringArray(
			String name )
	{
		return getStringArray( name , null );
	}
	
	public int getDimensionPixelSize(
			String name )
	{
		return getDimensionPixelSize( name , 0 );
	}
	
	//这个方法对应android的resource.getDimensionPixelSize,Android的这个方法不管后缀是px还是dp，返回值都是（值*density）
	public int getDimensionPixelSize(
			String name ,
			int defaultValue )
	{
		if( handler != null && handler.getmDimen().containsKey( name ) )
		{
			ConfigDimen dimen = handler.getmDimen().get( name );
			if( dimen.type.equals( "px" ) )
			{
				dimen.value = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP , dimen.value , mContext.getResources().getDisplayMetrics() );
			}
			return (int)( dimen.value + 0.5f );
		}
		else
		{
			return defaultValue;
		}
	}
	
	class ConfigHandler extends DefaultHandler
	{
		
		private HashMap<String , Integer> mInteger = new HashMap<String , Integer>();
		private HashMap<String , Float> mFloat = new HashMap<String , Float>();
		private HashMap<String , ConfigDimen> mDimen = new HashMap<String , ConfigDimen>();
		private HashMap<String , ConfigString> mString = new HashMap<String , ConfigString>();
		private HashMap<String , ConfigBoolean> mBoolean = new HashMap<String , ConfigBoolean>();
		private HashMap<String , ArrayList<String>> mStringArray = new HashMap<String , ArrayList<String>>();
		private ArrayList<String> stringList = new ArrayList<String>();
		private String parentTag = "";
		private String parentName = "";
		private Context mContext;
		private StringBuilder builder;
		private ConfigDimen configDimen = null;
		private ConfigBoolean configBoolean = null;
		private ConfigString configString = null;
		private String integerName = null;
		//xiatian add start	//需求：本地化（配置文件）支持“integer-array”类型数据。
		private HashMap<String , ArrayList<Integer>> mIntegerArray = new HashMap<String , ArrayList<Integer>>();
		private ArrayList<Integer> mIntegerList = new ArrayList<Integer>();
		//xiatian add end
		;
		
		public HashMap<String , Integer> getmInteger()
		{
			return mInteger;
		}
		
		public HashMap<String , ConfigString> getmString()
		{
			return mString;
		}
		
		public HashMap<String , ConfigBoolean> getmBoolean()
		{
			return mBoolean;
		}
		
		public HashMap<String , Float> getmFloat()
		{
			return mFloat;
		}
		
		public HashMap<String , ArrayList<String>> getmStringArray()
		{
			return mStringArray;
		}
		
		public HashMap<String , ConfigDimen> getmDimen()
		{
			return mDimen;
		}
		
		public ConfigHandler(
				Context context )
		{
			mContext = context;
		}
		
		@Override
		public void endDocument() throws SAXException
		{
			// TODO Auto-generated method stub
			super.endDocument();
		}
		
		@Override
		public void endElement(
				String uri ,
				String localName ,
				String qName ) throws SAXException
		{
			// TODO Auto-generated method stub
			if( localName.equals( "dimen" ) )
			{
				String value = builder.toString();
				if( configDimen != null && value.trim().length() > 0 )
				{
					if( value.contains( "px" ) )
					{
						value = value.substring( 0 , value.indexOf( "px" ) );
						configDimen.type = "px";
						configDimen.value = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_PX , Float.parseFloat( value ) , mContext.getResources().getDisplayMetrics() );
					}
					else if( value.contains( "dp" ) )
					{
						value = value.substring( 0 , value.indexOf( "dp" ) );
						configDimen.type = "dp";
						configDimen.value = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP , Float.parseFloat( value ) , mContext.getResources().getDisplayMetrics() );
					}
					else if( value.contains( "dip" ) )
					{
						configDimen.type = "dip";
						value = value.substring( 0 , value.indexOf( "dip" ) );
						configDimen.value = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP , Float.parseFloat( value ) , mContext.getResources().getDisplayMetrics() );
					}
					else if( value.contains( "sp" ) )
					{
						configDimen.type = "sp";
						value = value.substring( 0 , value.indexOf( "sp" ) );
						configDimen.value = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_SP , Float.parseFloat( value ) , mContext.getResources().getDisplayMetrics() );
					}
					mDimen.put( configDimen.name , configDimen );
				}
			}
			else if( localName.equals( "boolean" ) || localName.equals( "bool" ) )
			{
				String value = builder.toString();
				if( configBoolean != null )
				{
					if( value == null || value.length() == 0 )
					{
						configBoolean.value = false;
					}
					else
					{
						configBoolean.value = Boolean.parseBoolean( value );
					}
					mBoolean.put( configBoolean.name , configBoolean );
				}
			}
			else if( localName.equals( "integer" ) )
			{
				String value = builder.toString();
				if( integerName != null )
				{
					if( !TextUtils.isEmpty( value ) )
					{
						mInteger.put( integerName , Integer.parseInt( value ) );
					}
					integerName = null;
				}
			}
			else if( localName.equals( "item" ) )
			{
				if( parentTag.equals( "string-array" ) )
				{
					stringList.add( builder.toString() );
				}
				//xiatian add start	//需求：本地化（配置文件）支持“integer-array”类型数据。
				else if( parentTag.equals( "integer-array" ) )
				{
					mIntegerList.add( Integer.parseInt( builder.toString() ) );
				}
				//xiatian add end
			}
			else if( localName.equals( "string-array" ) )
			{
				if( stringList.size() > 0 )
				{
					ArrayList<String> value = new ArrayList<String>();
					value.addAll( stringList );
					mStringArray.put( parentName , value );
					parentTag = "";
					parentName = "";
					stringList.clear();
				}
			}
			else if( localName.equals( "string" ) )
			{
				if( configString != null )
				{
					configString.value = builder.toString();
					mString.put( configString.name , configString );
				}
			}
			//xiatian add start	//需求：本地化（配置文件）支持“integer-array”类型数据。
			else if( localName.equals( "integer-array" ) )
			{
				if( mIntegerList.size() > 0 )
				{
					ArrayList<Integer> value = new ArrayList<Integer>();
					value.addAll( mIntegerList );
					mIntegerArray.put( parentName , value );
					parentTag = "";
					parentName = "";
					mIntegerList.clear();
				}
			}
			//xiatian add end
		}
		
		@Override
		public void startDocument() throws SAXException
		{
			// TODO Auto-generated method stub
			super.startDocument();
			builder = new StringBuilder();
		}
		
		@Override
		public void characters(
				char[] ch ,
				int start ,
				int length ) throws SAXException
		{
			// TODO Auto-generated method stub
			super.characters( ch , start , length );
			builder.append( ch , start , length ); // 将读取的字符数组追加到builder中
		}
		
		@Override
		public void startElement(
				String uri ,
				String localName ,
				String qName ,
				Attributes atts ) throws SAXException
		{
			// TODO Auto-generated method stub
			builder.setLength( 0 );
			if( localName.equals( "dimen" ) )
			{
				configDimen = new ConfigDimen();
				builder.setLength( 0 );
				String name = atts.getValue( "name" );
				String type = atts.getValue( "type" );
				configDimen.name = name;
				configDimen.type = type;
			}
			else if( localName.equals( "boolean" ) || localName.equals( "bool" ) )
			{
				configBoolean = new ConfigBoolean();
				configBoolean.name = atts.getValue( "name" );
			}
			else if( localName.equals( "integer" ) )
			{
				integerName = atts.getValue( "name" );
			}
			else if( localName.equals( "interger" ) )
			{
				String name = atts.getValue( "name" );
				String value = atts.getValue( "value" );
				String type = atts.getValue( "type" );
				if( type == null || type.length() == 0 || type.equals( "px" ) )
				{
					mInteger.put( name , Integer.parseInt( value ) );
				}
				else if( type.equals( "dp" ) || type.equals( "dip" ) )
				{
					mInteger.put( name , dip2px( mContext , Integer.parseInt( value ) ) );
				}
			}
			else if( localName.equals( "float" ) )
			{
				String name = atts.getValue( "name" );
				String value = atts.getValue( "value" );
				String type = atts.getValue( "type" );
				if( type == null || type.length() == 0 || type.equals( "px" ) )
				{
					mFloat.put( name , Float.parseFloat( value ) );
				}
				else if( type.equals( "dp" ) || type.equals( "dip" ) )
				{
					final float scale = mContext.getResources().getDisplayMetrics().density;
					mFloat.put( name , ( Float.parseFloat( value ) * scale ) );
				}
			}
			else if( localName.equals( "string" ) )
			{
				configString = new ConfigString();
				configString.name = atts.getValue( "name" );
			}
			else if( localName.equals( "string-array" ) )
			{
				parentName = atts.getValue( "name" );
				parentTag = "string-array";
				stringList = new ArrayList<String>();
				if( mStringArray.containsKey( parentName ) )
				{
					mStringArray.remove( parentName );
				}
			}
			//xiatian add start	//需求：本地化（配置文件）支持“integer-array”类型数据。
			else if( localName.equals( "integer-array" ) )
			{
				parentName = atts.getValue( "name" );
				parentTag = "integer-array";
				mIntegerList = new ArrayList<Integer>();
				if( mIntegerArray.containsKey( parentName ) )
				{
					mIntegerArray.remove( parentName );
				}
			}
			//xiatian add end
		}
		
		public int dip2px(
				Context context ,
				float dipValue )
		{
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int)( dipValue * scale + 0.5f );
		}
		
		//xiatian add start	//需求：本地化（配置文件）支持“integer-array”类型数据。
		public HashMap<String , ArrayList<Integer>> getIntegerArray()
		{
			return mIntegerArray;
		}
		//xiatian add end
		;
	}
	
	//xiatian add start	//需求：本地化（配置文件）支持“integer-array”类型数据。
	public int[] getIntegerArray(
			String name ,
			int[] defaultValue )
	{
		if( handler != null )
		{
			HashMap<String , ArrayList<Integer>> mIntegerArray = handler.getIntegerArray();
			if( mIntegerArray.containsKey( name ) )
			{
				ArrayList<Integer> mCurValue = mIntegerArray.get( name );
				if( mCurValue != null )
				{
					int[] mIntArray = new int[mCurValue.size()];
					for( int i = 0 ; i < mCurValue.size() ; i++ )
					{
						int mItem = mCurValue.get( i );
						mIntArray[i] = mItem;
					}
					return mIntArray;
				}
			}
		}
		return defaultValue;
	}
	//xiatian add end
	;
}
