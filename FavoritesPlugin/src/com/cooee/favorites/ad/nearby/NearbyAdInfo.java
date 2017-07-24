package com.cooee.favorites.ad.nearby;


public class NearbyAdInfo
{
	
	private String summary = "summary";//广告摘要
	private String headline = "headline";//广告标题
	private String adlogo = "adlogo";//广告的logo图片url
	private String adplaceid = "adplaceid";//广告位id
	private String adid = "adid";//广告id
	private String clickurl = "clickurl";//广告点击的url
	
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
	
	public String getAdlogo()
	{
		return adlogo;
	}
	
	public void setAdlogo(
			String adlogo )
	{
		this.adlogo = adlogo;
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
}
