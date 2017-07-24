/* 文件名: ThemeDescription.java 2014年8月26日
 * 
 * 描述:桌面主题的信息类,包含了主题相关内容的所有信息
 * 
 * 作者: cooee */
package com.cooee.theme;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.util.Tools;


public class ThemeDescription
{
	
	private static final String TAG = "ThemeDescription";
	public static final String HOME_DIR = "theme/";
	public static final String PREVIEW_DIR = "theme/preview/";
	private static final String PREVIEW_FILENAME = StringUtils.concat( PREVIEW_DIR , "preview.xml" );
	private static final String CONFIG_FILENAME = StringUtils.concat( HOME_DIR , "config.xml" );
	private static final String ICON_FILENAME = StringUtils.concat( HOME_DIR , "icon/icon.xml" );
	private static final String TAG_WIDGET_THEME = "widget_theme";
	private static final String TAG_THEME = "themepreview";
	private static final String TAG_VERSION = "version";
	private static final String TAG_INFO = "info";
	private static final String TAG_ITEM = "item";
	public ComponentName componentName;
	public CharSequence title;
	public boolean mUse = false;
	public boolean mSystem = false;
	public boolean mBuiltIn = false;
	public String themeversion;
	public String themedata;
	public String themeauthor;
	public String themename;
	public String themetype;
	public String themefeedback;
	public String widgettheme;
	public ArrayList<String> themeimgs = new ArrayList<String>();
	private HashMap<String , Integer> mInteger = new HashMap<String , Integer>();
	private HashMap<String , String> mStrings = new HashMap<String , String>();
	private HashMap<String , Boolean> mBooleans = new HashMap<String , Boolean>();
	private HashMap<String , Float> mFloat = new HashMap<String , Float>();
	private HashMap<String , String> mIcons = new HashMap<String , String>();
	private Context mContext;
	
	public ThemeDescription(
			Context context ,
			boolean mDefaultTheme /* //xiatian add	//解决“非默认主题，也读取本地化配置”的问题。  */)
	{
		mContext = context;
		mSystem = mDefaultTheme;//xiatian add	//解决“非默认主题，也读取本地化配置”的问题。
		/*解析主题APK里的preview.xml文件*/
		PreViewHandler handler = new PreViewHandler();
		LoadXml( PREVIEW_FILENAME , handler );
		/*解析主题APK里的config.xml文件*/
		ConfigHandler cfghandler = new ConfigHandler();
		LoadXml( CONFIG_FILENAME , cfghandler );
		/*解析主题APK里的icon.xml文件*/
		IconHandler iconhandler = new IconHandler();
		LoadXml( ICON_FILENAME , iconhandler );
	}
	
	public ThemeDescription(
			Context context ,
			String defaultFileName )
	{
		mContext = context;
		ConfigHandler cfghandler = new ConfigHandler();
		LoadXml( defaultFileName , cfghandler );
	}
	
	public void destroy()
	{
		mContext = null;
		componentName = null;
	}
	
	public Context getContext()
	{
		return mContext;
	}
	
	/**
	 * 获取主题的预览default.png预览图
	 * @return 将预览图转成Bitmap并返回
	 */
	public Bitmap getDefaultBitmap()
	{
		Bitmap defaultbitmap = null;
		try
		{
			if( themeimgs.size() > 0 )
			{
				InputStream in = mContext.getAssets().open( StringUtils.concat( PREVIEW_DIR , themeimgs.get( 0 ) ) );
				defaultbitmap = Tools.getImageFromInStream( in , Bitmap.Config.RGB_565 );
				in.close();
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return defaultbitmap;
	}
	
	// zhujieping@2015/03/13 ADD START
	public Bitmap getBitmapFromResource(
			String filename )
	{
		int mNeedCutLen = filename.lastIndexOf( "." );//目前支持".jpg"、".jpge"、".png"和".webp"
		if( mNeedCutLen != -1 )
		{
			filename = filename.substring( 0 , mNeedCutLen );
		}
		Resources mResources = mContext.getResources();
		int imgId = mResources.getIdentifier( filename , "drawable" , mContext.getPackageName() );//图片名字不要加后缀名 imgView.setImageResource(imgId);
		if( imgId > 0 )
		{
			return BitmapFactory.decodeResource( mResources , imgId );
		}
		return null;
	}
	
	public boolean hasBitmapFromResource(
			String filename )
	{
		if( getResourceID( filename ) > 0 )
		{
			return true;
		}
		return false;
	}
	
	public int getResourceID(
			String filename )
	{
		if( filename.endsWith( ".jpg" ) || filename.endsWith( ".png" ) )
		{
			filename = filename.substring( 0 , filename.length() - 4 );
		}
		int imgId = mContext.getResources().getIdentifier( filename , "drawable" , mContext.getPackageName() );//图片名字不要加后缀名 imgView.setImageResource(imgId);
		return imgId;
	}
	
	// zhujieping@2015/03/13 ADD END
	/**
	 * 通过文件路径获取图片资源的Bitmap
	 * @param filename 主题资源的路径
	 * @return 返回资源的Bitmap
	 */
	public Bitmap getBitmap(
			String filename )
	{
		Bitmap bmp = null;
		try
		{
			bmp = Tools.getImageFromInStream( mContext.getAssets().open( filename ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return bmp;
	}
	
	/**
	 * 通过文件路径获取图片资源的InputStream
	 * @param filename 主题资源的路径
	 * @return 返回资源的InputStream
	 */
	public InputStream getStream(
			String filename )
	{
		InputStream stream = null;
		try
		{
			stream = mContext.getAssets().open( getFileForDpi( filename ) );
		}
		catch( IOException e )
		{
			try
			{
				stream = mContext.getAssets().open( StringUtils.concat( "theme/" , filename ) );
			}
			catch( IOException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return stream;
	}
	
	/**
	 * 根据分辨率来获取主题对应分辨率文件夹下的图标路径
	 * @param filename 传入的主题图片资源名
	 * @return 返回带分辨率文件夹的资源路径
	 */
	public String getFileForDpi(
			String filename )
	{
		float mScreenScale = mContext.getResources().getDisplayMetrics().density;
		if( mScreenScale <= 0.75f )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "dpi=0.75" );
			filename = StringUtils.concat( "theme-ldpi/" , filename );
		}
		else if( mScreenScale <= 1f )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "dpi=1" );
			filename = StringUtils.concat( "theme-mdpi/" , filename );
		}
		else if( mScreenScale <= 1.5f )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "dpi=1.5" );
			filename = StringUtils.concat( "theme-hdpi/" , filename );
		}
		else
			filename = StringUtils.concat( "theme-xhdpi/" , filename );
		return filename;
	}
	
	public ArrayList<String> getBitmaps()
	{
		return themeimgs;
	}
	
	public Set<String> getIcons()
	{
		return mIcons.keySet();
	}
	
	/**
	 * 根据主题图标的名称来获取theme/icon/80目录下的图标
	 * @param icon 图标的名称
	 * @return 返回图标的Bitmap对象
	 */
	public Bitmap getIcon(
			String icon )
	{
		Bitmap iconbmp = null;
		String path = new String();
		path = "theme/icon/";
		path += "80";
		path += icon;
		try
		{
			iconbmp = Tools.getImageFromInStream( mContext.getAssets().open( path ) );
		}
		catch( IOException e )
		{
		}
		return iconbmp;
	}
	
	/**
	 * 解析XML的接口方法
	 * @param Filename 需要解析的xml文件
	 * @param handler 解析的DefaultHandler对象
	 */
	public void LoadXml(
			String Filename ,
			DefaultHandler handler )
	{
		SAXParserFactory factoey = SAXParserFactory.newInstance();
		try
		{
			SAXParser parser = factoey.newSAXParser();
			XMLReader xmlreader = parser.getXMLReader();
			xmlreader.setContentHandler( handler );
			InputSource xmlin = null;
			File f;
			//<theme_issue> liuhailin@2014-08-25 del begin
			//if( mContext.getPackageName().equals( RR.getPackageName() ) )
			//{
			//	f = new File( Utils3D.LOCALIZED_ASSETS + Filename );
			//	if( f != null && f.exists() )
			//	{
			//		xmlin = new InputSource( new FileInputStream( f.getAbsolutePath() ) );
			//	}
			//}
			//<theme_issue> liuhailin@2014-08-25 del end
			if( xmlin == null )
			{
				if(
				//
				( mSystem /* //xiatian add	//解决“非默认主题，也读取本地化配置”的问题。  */)
				//
				&& !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH )
				//
				)
				{
					try
					{
						FileInputStream in = new FileInputStream( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , File.separator , Filename ) );
						xmlin = new InputSource( in );
					}
					catch( FileNotFoundException e )
					{
						xmlin = new InputSource( mContext.getAssets().open( Filename ) );
					}
				}
				else
				{
					xmlin = new InputSource( mContext.getAssets().open( Filename ) );
				}
			}
			xmlreader.parse( xmlin );
			handler = null;
			xmlin = null;
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
	
	/**
	 * 获取主题的信息,Type、themeversion、themeauthor、themedata、themefeedback
	 * @param 需要显示的TextView对象
	 */
	public void getInfo(
			TextView text )
	{
		if( ThemeManager.getInstance() == null )
		{
			return;
		}
		//<theme_issue> liuhailin@2014-08-25 modify begin
		//info += context.getString( RR.string.themetype ) + "\n" + themetype;
		//info += "\n\n";
		//info += context.getString( RR.string.themeversion ) + "\n" + themeversion;
		//info += "\n\n";
		//info += context.getString( RR.string.themeauthor ) + "\n" + themeauthor;
		//info += "\n\n";
		//info += context.getString( RR.string.themedata ) + "\n" + themedata;
		//info += "\n\n";
		//info += context.getString( RR.string.themefeedback ) + "\n";
		String info = StringUtils.concat(
				"Type:\n" ,
				themetype ,
				"\n\nthemeversion:\n" ,
				themeversion ,
				"\n\nthemeauthor:\n" ,
				themeauthor ,
				"\n\nthemedata:\n" ,
				themedata ,
				"\n\nthemefeedback:\n" ,
				themefeedback );
		//<theme_issue> liuhailin@2014-08-25 modify end
		text.append( info );
		String link = StringUtils.concat( "<a href=\"" , themefeedback , "\">" , themefeedback , "</a>" );
		text.append( Html.fromHtml( link ) );
	}
	
	/**
	 * 根据key来获取主题preview.xml\config.xml\icon.xml配置文件里的整型值
	 * @param key
	 * @return 对应key的int值
	 */
	public Integer getInteger(
			String key )
	{
		Integer result = mInteger.get( key );
		return result;
	}
	
	/**
	 * 根据key来获取主题preview.xml\config.xml\icon.xml配置文件里的String值
	 * @param key
	 * @return 对应key的整型String值
	 */
	public String getString(
			String key )
	{
		String value = mStrings.get( key );
		return value;
	}
	
	/**
	 * 根据key来获取主题preview.xml\config.xml\icon.xml配置文件里的Boolean值
	 * @param key
	 * @return 对应key的整型Boolean值
	 */
	public Boolean getBoolean(
			String key )
	{
		Boolean result = mBooleans.get( key );
		return result;
	}
	
	//<i_0008508> liuhailin@2014-12-17 modify begin
	public Float getFloat(
			String key )
	{
		Float value = mFloat.get( key );
		return value;
	}
	
	//<i_0008508> liuhailin@2014-12-17 modify end
	/**
	 * 解析preview.xml的辅助类
	 * @author cooee
	 */
	class PreViewHandler extends DefaultHandler
	{
		
		public PreViewHandler()
		{
		}
		
		public void startDocument() throws SAXException
		{
		}
		
		public void endDocument() throws SAXException
		{
		}
		
		public void startElement(
				String namespaceURI ,
				String localName ,
				String qName ,
				Attributes atts ) throws SAXException
		{
			if( localName.equals( TAG_THEME ) )
			{
			}
			else if( localName.equals( TAG_VERSION ) )
			{
				themeversion = atts.getValue( "value" );
			}
			else if( localName.equals( TAG_INFO ) )
			{
				themedata = atts.getValue( "date" );
				themeauthor = atts.getValue( "author" );
				themename = atts.getValue( "name" );
				themetype = atts.getValue( "type" );
				themefeedback = atts.getValue( "feedback" );
			}
			else if( localName.equals( TAG_ITEM ) )
			{
				themeimgs.add( atts.getValue( "image" ) );
			}
			else if( localName.equals( TAG_WIDGET_THEME ) )
			{
				widgettheme = atts.getValue( "theme" );
			}
		}
		
		public void endElement(
				String namespaceURI ,
				String localName ,
				String qName ) throws SAXException
		{
		}
		
		public void characters(
				char ch[] ,
				int start ,
				int length )
		{
		}
	}
	
	/**
	 * 解析config.xml的辅助类
	 * @author cooee
	 */
	class ConfigHandler extends DefaultHandler
	{
		
		public ConfigHandler()
		{
		}
		
		public void startDocument() throws SAXException
		{
		}
		
		public void endDocument() throws SAXException
		{
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
			else if( localName.equals( "interge" ) )
			{
				if( atts.getValue( "type" ).equals( "dip" ) )
				{
					mInteger.put( atts.getValue( "name" ) , Tools.dip2px( mContext , Integer.valueOf( atts.getValue( "value" ) ) ) );
				}
				else
				{
					mInteger.put( atts.getValue( "name" ) , Integer.valueOf( atts.getValue( "value" ) ) );
				}
			}
			else if( localName.equals( "string" ) )
			{
				mStrings.put( atts.getValue( "name" ) , atts.getValue( "value" ) );
			}
			else if( localName.equals( "boolean" ) )
			{
				String val = atts.getValue( "value" );
				if( val != null && val.equals( "true" ) )
				{
					mBooleans.put( atts.getValue( "name" ) , true );
				}
				else
				{
					mBooleans.put( atts.getValue( "name" ) , false );
				}
			}
			else if( localName.equals( "float" ) )
			{
				String name = atts.getValue( "name" );
				String value = atts.getValue( "value" );
				String type = atts.getValue( "type" );
				if( type.equals( "dp" ) || type.equals( "dip" ) )
				{
					final float scale = mContext.getResources().getDisplayMetrics().density;
					mFloat.put( name , ( Float.parseFloat( value ) * scale ) );
				}
				else
				{
					mFloat.put( name , Float.parseFloat( value ) );
				}
			}
		}
		
		public void endElement(
				String namespaceURI ,
				String localName ,
				String qName ) throws SAXException
		{
		}
		
		public void characters(
				char ch[] ,
				int start ,
				int length )
		{
		}
	}
	
	/**
	 * 解析icon.xml的辅助类
	 * @author cooee
	 */
	class IconHandler extends DefaultHandler
	{
		
		public IconHandler()
		{
		}
		
		public void startDocument() throws SAXException
		{
		}
		
		public void endDocument() throws SAXException
		{
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
			else if( localName.equals( "item" ) )
			{
				mIcons.put( atts.getValue( "component" ) , atts.getValue( "image" ) );
			}
		}
		
		public void endElement(
				String namespaceURI ,
				String localName ,
				String qName ) throws SAXException
		{
		}
		
		public void characters(
				char ch[] ,
				int start ,
				int length )
		{
		}
	}
	
	/**
	 * 根据文件夹前缀来获取对应分辨率下的文件夹名
	 * @param context
	 * @param prefix 文件夹前缀
	 * @return 返回带分辨率的文件夹  例如temp-xhdpi
	 */
	public String getAutoAdaptDir(
			Context context ,
			String prefix )
	{
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		if( metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH )
		{
			prefix = StringUtils.concat( prefix , "-xhdpi" );
		}
		else if( metrics.densityDpi == DisplayMetrics.DENSITY_HIGH )
		{
			prefix = StringUtils.concat( prefix , "-hdpi" );
		}
		else if( metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM )
		{
			prefix = StringUtils.concat( prefix , "-mdpi" );
		}
		else if( metrics.densityDpi == DisplayMetrics.DENSITY_LOW )
		{
			prefix = StringUtils.concat( prefix , "-ldpi" );
		}
		return prefix;
	}
	
	/**
	 * 根据key来获取主题preview.xml\config.xml\icon.xml配置文件里的整型值
	 * @param key
	 * @return 对应key的int值
	 */
	public int getSignedInteger(
			String key )
	{
		int result = -999;
		Integer value = mInteger.get( key );
		if( value != null )
		{
			result = value.intValue();
		}
		return result;
	}
	
	/**
	 * autoAdapt 为true 根据带分辨率的文件夹来获取文件的InputStream
	 * autoAdapt 为false 直接获取文件路径下的文件的InputStream
	 * @param autoAdapt true 使用带分辨率的文件路径  false 直接使用传入的文件路径
	 * @param fileName  需要读取的文件路径
	 * @return 文件的InputStream
	 */
	public InputStream getInputStream(
			boolean autoAdapt ,
			String fileName )
	{
		if( fileName == null )
			return null;
		InputStream instr = null;
		AssetManager mAssetManager = mContext.getAssets();
		if( autoAdapt )
		{
			Resources mResources = mContext.getResources();
			String filePrefix = fileName.substring( 0 , fileName.indexOf( "/" ) );
			if( !filePrefix.contains( "-" ) )
			{
				DisplayMetrics mDisplayMetrics = mResources.getDisplayMetrics();
				filePrefix = StringUtils.concat( filePrefix , "-" , mDisplayMetrics.heightPixels , "x" , mDisplayMetrics.widthPixels );
			}
			// 查找精确分辨率如960*540
			try
			{
				String tempFileName = StringUtils.concat( filePrefix , fileName.substring( fileName.indexOf( "/" ) ) );
				instr = mAssetManager.open( tempFileName );
			}
			catch( IOException e )
			{
				instr = null;
			}
			String dpiFilePrefix = this.getAutoAdaptDir( mContext , filePrefix.substring( 0 , filePrefix.indexOf( "-" ) ) );
			if( instr == null )
			{
				String dpiFileName = StringUtils.concat( dpiFilePrefix , fileName.substring( fileName.indexOf( "/" ) ) );
				try
				{
					instr = mAssetManager.open( dpiFileName );
				}
				catch( IOException e )
				{
				}
			}
			// 在不带dpi的目录下寻找资源，目前系统资源统一放在不带dpi的目录，所以首先寻找不带dpi的目录
			if( instr == null )
			{
				filePrefix = fileName.substring( 0 , fileName.indexOf( "/" ) );
				if( !filePrefix.equals( dpiFilePrefix ) )
				{
					try
					{
						instr = mAssetManager.open( fileName );
					}
					catch( IOException e )
					{
					}
				}
			}
		}
		else
		{
			if( instr == null )
			{
				try
				{
					instr = mAssetManager.open( fileName );
				}
				catch( IOException e )
				{
				}
			}
		}
		return instr;
	}
	
	public Drawable getDrawableFromResource(
			String mDrawableName )
	{//从当前主题读Drawable
		int mDrawableId = ResourceUtils.getDrawableResourceIdByReflectIfNecessary( -1 , mContext.getResources() , mContext.getPackageName() , mDrawableName );
		if( mDrawableId > 0 )
		{
			return mContext.getResources().getDrawable( mDrawableId );
		}
		return null;
	}
}
