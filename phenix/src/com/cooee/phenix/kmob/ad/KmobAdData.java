package com.cooee.phenix.kmob.ad;


import android.graphics.Bitmap;


public class KmobAdData
{
	
	private String summary = "";//广告摘要
	private String headline = "";//广告标题
	private String adcategory = "";//广告种类
	private String appRating = "";//广告的星级
	private String adlogo = "";//广告的logo图片的url
	private String details = "";//广告的详情介绍	
	private String adlogoWidth = "";//广告logo图片的宽度
	private String adlogoHeight = "";//广告logo图片的高度
	private String review = "";//广告的评论数
	private String appinstalls = "";//广告的安装量
	private String download = "";//广告的下载量
	private String adplaceid = "";//广告位id
	private String adid = "";//广告id
	private String clickurl = "";//广告点击的url
	private String interactiontype = "";//广告点击是要下载还是点击去浏览器的type
	private String open_type = "";//若是进浏览器则选择是进外部浏览器还是内部浏览器
	private String hurl = "";//H5Native的url
	private String hdetailurl = "";//H5详情页面的url
	private String pkgname = "";//包名
	private String appsize = "";//包体大小
	private String version = "";//版本号
	private String versionname = "";//版本名
	private String ctimg = "";//Img对象数组，表示很多个广告图，返回JSONArray
	private String hiimg = "";//Img对象数组，表示很多个高清广告图，返回JSONArray
	private String click_record_url = "";//YM广告点击监控url
	private Bitmap adHiimg = null;
	private String otherInfo = "";//广告原始Json数据
	
	public KmobAdData(
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
			String click_record_url ,
			String otherInfo )
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
		this.otherInfo = otherInfo;
	}
	
	public KmobAdData()
	{
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
	
	public Bitmap getAdHiimg()
	{
		return adHiimg;
	}
	
	public void setAdHiimg(
			Bitmap adHiimg )
	{
		this.adHiimg = adHiimg;
	}
	
	public void recycle()
	{
		if( this.adHiimg != null && this.adHiimg.isRecycled() )
		{
			this.adHiimg.recycle();
		}
	}
	
	/**
	 * @return the otherInfo
	 */
	public String getOtherInfo()
	{
		return otherInfo;
	}
	
	/**
	 * @param otherInfo the otherInfo to set
	 */
	public void setOtherInfo(
			String otherInfo )
	{
		this.otherInfo = otherInfo;
	}
}
