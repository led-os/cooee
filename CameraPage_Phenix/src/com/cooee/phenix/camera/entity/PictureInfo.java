package com.cooee.phenix.camera.entity;


import android.graphics.drawable.BitmapDrawable;


// CameraPage
public class PictureInfo
{
	
	private String pictureDate = null;
	private String pictureTime = null;
	private int pictureWeek = 0;
	private String picturePath = null;
	private long timeMillis = 0L;
	//
	private BitmapDrawable drawable = null;
	
	public PictureInfo(
			String pictureDate ,
			String pictureTime ,
			int pictureWeek ,
			String picturePath ,
			long timeMillis )
	{
		this.pictureDate = pictureDate;
		this.pictureTime = pictureTime;
		this.pictureWeek = pictureWeek;
		this.picturePath = picturePath;
		this.timeMillis = timeMillis;
	}
	
	public String getPictureDate()
	{
		return pictureDate;
	}
	
	public String getPictureTime()
	{
		return pictureTime;
	}
	
	public String getPicturePath()
	{
		return picturePath;
	}
	
	public int getPictureWeek()
	{
		return pictureWeek;
	}
	
	public long getTimeMillis()
	{
		return timeMillis;
	}
	
	public BitmapDrawable getDrawable()
	{
		return drawable;
	}
	
	public void setDrawable(
			BitmapDrawable drawable )
	{
		this.drawable = drawable;
	}
}
