package com.cooee.favorites.news.data;


import org.json.JSONObject;


public class NewsItem
{
	
	private int notifyType = 0;//默认0就是新闻，1是提示刷新间隔
	private String groupId;//新闻的groupid
	private String mTitle;//新闻题目
	private String mSite;//新闻来源
	private int mComments;//评论条数
	private String showTime;//显示在页面时间字串
	private String mHQImageUrl;//大图
	private String mImageList[];//多张图片
	private String mThumbImage;//小图，显示在最左边
	private String newsUrl;//新闻链接
	private long displayTime;//实际显示的时间戳
	private String siteAndComments;
	private JSONObject otherInfo;//其他信息，例如广告的一些信息
	private int mHQWidth = -1;//高清图的宽
	private int mHQHeight = -1;//高清图的高
	
	public String getTitle()
	{
		return mTitle;
	}
	
	public void setTitle(
			String mTitle )
	{
		this.mTitle = mTitle;
	}
	
	public String getSite()
	{
		return mSite;
	}
	
	public void setSite(
			String mSite )
	{
		this.mSite = mSite;
	}
	
	public int getComments()
	{
		return mComments;
	}
	
	public void setComments(
			int mComments )
	{
		this.mComments = mComments;
	}
	
	public String getShowTime()
	{
		return showTime != null ? showTime : "";
	}
	
	public void setShowTime(
			String showTime )
	{
		this.showTime = showTime;
	}
	
	public String getHQImageUrl()
	{
		return mHQImageUrl;
	}
	
	public void setHQImageUrl(
			String mHQImageUrl )
	{
		this.mHQImageUrl = mHQImageUrl;
	}
	
	public String[] getImageList()
	{
		return mImageList;
	}
	
	public void setImageList(
			String mImageList[] )
	{
		this.mImageList = mImageList;
	}
	
	public String getThumbImage()
	{
		return mThumbImage;
	}
	
	public void setThumbImage(
			String mThumbImage )
	{
		this.mThumbImage = mThumbImage;
	}
	
	public String getNewsUrl()
	{
		return newsUrl;
	}
	
	public void setNewsUrl(
			String newsUrl )
	{
		this.newsUrl = newsUrl;
	}
	
	public long getDisplayTime()
	{
		return displayTime;
	}
	
	public void setDisplayTime(
			long displayTime )
	{
		this.displayTime = displayTime;
	}
	
	public JSONObject getOtherInfo()
	{
		return otherInfo;
	}
	
	public void setOtherInfo(
			JSONObject otherInfo )
	{
		this.otherInfo = otherInfo;
	}
	
	public String getSiteAndComments()
	{
		return siteAndComments;
	}
	
	public void setSiteAndComments(
			String siteAndComments )
	{
		this.siteAndComments = siteAndComments;
	}
	
	public String getGroupId()
	{
		return groupId;
	}
	
	public void setGroupId(
			String groupId )
	{
		this.groupId = groupId;
	}
	
	public int getHQWidth()
	{
		return mHQWidth;
	}
	
	public void setHQWidth(
			int mHQWidth )
	{
		this.mHQWidth = mHQWidth;
	}
	
	public int getHQHeight()
	{
		return mHQHeight;
	}
	
	public void setHQHeight(
			int mHQHeight )
	{
		this.mHQHeight = mHQHeight;
	}
	
	public int getNotifyType()
	{
		return notifyType;
	}
	
	public void setNotifyType(
			int notifyType )
	{
		this.notifyType = notifyType;
	}
}
