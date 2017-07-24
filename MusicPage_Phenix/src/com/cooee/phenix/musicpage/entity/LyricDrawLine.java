package com.cooee.phenix.musicpage.entity;


// MusicPage
public class LyricDrawLine
{
	
	private String text = null;
	private int start = 0;
	private int end = 0;
	private float x = 0;
	private float y = 0;
	private int textColor = 0xffffff;
	private float fontHeight = 0;
	private float fontSize = 0;
	private int alpha = 255;
	
	public LyricDrawLine(
			String text ,
			int start ,
			int end ,
			float x ,
			float y ,
			int textColor ,
			float fontHeight ,
			float fontSize ,
			int alpha )
	{
		this.text = text;
		this.start = start;
		this.end = end;
		this.x = x;
		this.y = y;
		this.textColor = textColor;
		this.fontHeight = fontHeight;
		this.fontSize = fontSize;
		this.alpha = alpha;
	}
	
	public String getText()
	{
		return text;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
	
	public int getTextColor()
	{
		return textColor;
	}
	
	public float getFontHeight()
	{
		return fontHeight;
	}
	
	public float getFontSize()
	{
		return fontSize;
	}
	
	public int getAlpha()
	{
		return alpha;
	}
}
