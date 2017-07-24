package com.cooee.phenix.Folder.kmob;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


// cheyingkun add whole file //文件夹推荐应用
public class NativeData
{
	
	private String summary = "";//广告摘要
	private String headline = "";//广告标题//====================
	private String adcategory = "";//广告种类
	private String appRating = "";//广告星级
	private String adlogo = "";//广告logo图标的url////================
	private String details = "";//广告详情介绍
	private String adlogoWidth = "";//广告logo图片宽度
	private String adlogoHeight = "";//广告logo图片高度
	private String review = "";//广告评论数
	private String appinstalls = "";//广告安装量
	private String download = "";//广告下载量
	private String adplaceid = "";//广告位id
	private String adid = "";//广告id
	private String clickurl = "";//广告点击的url
	private String interactiontype = "";//广告点击是要进浏览器还是下载的type
	private String open_type = "";//若是打开浏览器,则选择打开内部浏览器还是外部浏览器
	private String hurl = "";//H5 Naive的url
	private String hdetailurl = "";//H5详情页的url
	private String pkgname = "";//包名
	private String appsize = "";//包名大小
	private String version = "";//版本号
	private String versionname = "";//版本名
	private String ctimg = "";//img数组对象,表示很多个广告图,返回jsonArray
	private String hiimg = "";//img数组对象,表示很多个高清广告图,返回jsonArray
	private String click_record_url = "";//广告的点击追踪url
	/**广告logo图标*/
	private Bitmap adlogoBitmap;
	/**广告位图信息*/
	private NativeWallpaperData mNativeWallpaperData;//cheyingkun add	//一键换壁纸需求。（剩余：动态图标、自定义事件统计）
	
	public NativeData(
			String summary ,
			String headline ,
			String adcategory ,
			String appRating ,
			String adlogo ,
			String details ,
			String adlogoWidth ,
			String adlogoHeight ,
			String review ,
			String appinstalls ,
			String download ,
			String adplaceid ,
			String adid ,
			String clickurl ,
			String interactiontype ,
			String open_type ,
			String hurl ,
			String hdetailurl ,
			String pkgname ,
			String appsize ,
			String version ,
			String versionname ,
			String ctimg ,
			String hiimg ,
			String click_record_url )
	{
		this.summary = summary;
		this.headline = headline;
		this.adcategory = adcategory;
		this.appRating = appRating;
		this.adlogo = adlogo;
		this.details = details;
		this.adlogoWidth = adlogoWidth;
		this.adlogoHeight = adlogoHeight;
		this.review = review;
		this.appinstalls = appinstalls;
		this.download = download;
		this.adplaceid = adplaceid;
		this.adid = adid;
		this.clickurl = clickurl;
		this.interactiontype = interactiontype;
		this.open_type = open_type;
		this.hurl = hurl;
		this.hdetailurl = hdetailurl;
		this.pkgname = pkgname;
		this.appsize = appsize;
		this.version = version;
		this.versionname = versionname;
		this.ctimg = ctimg;
		this.hiimg = hiimg;
		this.click_record_url = click_record_url;
	}
	
	public String getHurl()
	{
		return hurl;
	}
	
	public void setHurl(
			String hurl )
	{
		this.hurl = hurl;
	}
	
	public String getHdetailurl()
	{
		return hdetailurl;
	}
	
	public void setHdetailurl(
			String hdetailurl )
	{
		this.hdetailurl = hdetailurl;
	}
	
	public String getPkgname()
	{
		return pkgname;
	}
	
	public void setPkgname(
			String pkgname )
	{
		this.pkgname = pkgname;
	}
	
	public String getAppsize()
	{
		return appsize;
	}
	
	public void setAppsize(
			String appsize )
	{
		this.appsize = appsize;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	public void setVersion(
			String version )
	{
		this.version = version;
	}
	
	public String getVersionname()
	{
		return versionname;
	}
	
	public void setVersionname(
			String versionname )
	{
		this.versionname = versionname;
	}
	
	public String getCtimg()
	{
		return ctimg;
	}
	
	public void setCtimg(
			String ctimg )
	{
		this.ctimg = ctimg;
	}
	
	public String getHiimg()
	{
		return hiimg;
	}
	
	public void setHiimg(
			String hiimg )
	{
		this.hiimg = hiimg;
	}
	
	public String getAdplaceid()
	{
		return adplaceid;
	}
	
	public void setAdplaceid(
			String adplaceid )
	{
		this.adplaceid = adplaceid;
	}
	
	public String getAdid()
	{
		return adid;
	}
	
	public void setAdid(
			String adid )
	{
		this.adid = adid;
	}
	
	public String getClickurl()
	{
		return clickurl;
	}
	
	public void setClickurl(
			String clickurl )
	{
		this.clickurl = clickurl;
	}
	
	public String getInteractiontype()
	{
		return interactiontype;
	}
	
	public void setInteractiontype(
			String interactiontype )
	{
		this.interactiontype = interactiontype;
	}
	
	public String getOpen_type()
	{
		return open_type;
	}
	
	public void setOpen_type(
			String open_type )
	{
		this.open_type = open_type;
	}
	
	public String getSummary()
	{
		return summary;
	}
	
	public void setSummary(
			String summary )
	{
		this.summary = summary;
	}
	
	public String getHeadline()
	{
		return headline;
	}
	
	public void setHeadline(
			String headline )
	{
		this.headline = headline;
	}
	
	public String getAdcategory()
	{
		return adcategory;
	}
	
	public void setAdcategory(
			String adcategory )
	{
		this.adcategory = adcategory;
	}
	
	public String getAppRating()
	{
		return appRating;
	}
	
	public void setAppRating(
			String appRating )
	{
		this.appRating = appRating;
	}
	
	public String getAdlogo()
	{
		return adlogo;
	}
	
	public void setAdlogo(
			String adlogo )
	{
		this.adlogo = adlogo;
	}
	
	public String getDetails()
	{
		return details;
	}
	
	public void setDetails(
			String details )
	{
		this.details = details;
	}
	
	public String getAdlogoWidth()
	{
		return adlogoWidth;
	}
	
	public void setAdlogoWidth(
			String adlogoWidth )
	{
		this.adlogoWidth = adlogoWidth;
	}
	
	public String getAdlogoHeight()
	{
		return adlogoHeight;
	}
	
	public void setAdlogoHeight(
			String adlogoHeight )
	{
		this.adlogoHeight = adlogoHeight;
	}
	
	public String getReview()
	{
		return review;
	}
	
	public void setReview(
			String review )
	{
		this.review = review;
	}
	
	public String getAppinstalls()
	{
		return appinstalls;
	}
	
	public void setAppinstalls(
			String appinstalls )
	{
		this.appinstalls = appinstalls;
	}
	
	public String getDownload()
	{
		return download;
	}
	
	public void setDownload(
			String download )
	{
		this.download = download;
	}
	
	public Bitmap getAdlogoBitmap()
	{
		return adlogoBitmap;
	}
	
	public void setAdlogoBitmap(
			Bitmap adlogoBitmap )
	{
		this.adlogoBitmap = adlogoBitmap;
	}
	
	public String getClick_record_url()
	{
		return click_record_url;
	}
	
	public void setClick_record_url(
			String click_record_url )
	{
		this.click_record_url = click_record_url;
	}
	
	//cheyingkun add start	//一键换壁纸需求。（剩余：动态图标、自定义事件统计）
	public NativeWallpaperData getNativeWallpaperData()
	{
		return mNativeWallpaperData;
	}
	
	public void setNativeWallpaperData(
			NativeWallpaperData mNativeWallpaperData )
	{
		this.mNativeWallpaperData = mNativeWallpaperData;
	}
	
	public class NativeWallpaperData
	{
		
		private String url;
		private int imgwidth;
		private int imgHeight;
		private Bitmap bitmap;
		
		public NativeWallpaperData(
				String url ,
				int imgwidth ,
				int imgHeight )
		{
			this.url = url;
			this.imgwidth = imgwidth;
			this.imgHeight = imgHeight;
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.w( "cyk" , StringUtils.concat( "imgurl " , url , " imgwidth " , imgwidth , " imgHeight " , imgHeight ) );
		}
		
		public String getUrl()
		{
			return url;
		}
		
		public int getImgwidth()
		{
			return imgwidth;
		}
		
		public int getImgHeight()
		{
			return imgHeight;
		}
		
		public Bitmap getBitmap()
		{
			return bitmap;
		}
		
		public void setBitmap(
				Bitmap bitmap )
		{
			this.bitmap = bitmap;
		}
	}
	
	/**
	 * 把字符串转换成广告位图对象(字符串必须符合一定的规范)
	 * @param str 
	 * @return
	 */
	public NativeWallpaperData changeToNativeDataAdverBitmap(
			String str )
	{
		if( TextUtils.isEmpty( str ) )
		{
			return null;
		}
		NativeWallpaperData mNativeWallpaperData = null;
		try
		{
			JSONArray ctimgArray = new JSONArray( str );
			for( int n = 0 ; n < ctimgArray.length() ; n++ )
			{
				JSONObject object = ctimgArray.getJSONObject( n );
				String url = object.getString( "url" );
				String imgwidth = object.getString( "width" );
				String imgHeight = object.getString( "height" );
				mNativeWallpaperData = new NativeWallpaperData( url , Integer.valueOf( imgwidth ) , Integer.valueOf( imgHeight ) );
			}
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		return mNativeWallpaperData;
	}
	//cheyingkun add end
}
