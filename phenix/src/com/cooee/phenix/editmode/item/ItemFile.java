package com.cooee.phenix.editmode.item;


import android.content.Context;
import android.graphics.Bitmap;

import com.cooee.util.Tools;


public class ItemFile
{
	
	/**描述壁纸来源*/
	public enum ItemFileFromEnum
	{
		selfresource , selfassets , otherresource , otherassets , custom ,
	}
	
	/**图片文件或者资源id*/
	private String fileName;
	private ItemFileFromEnum fileFrom;
	private int width;
	private int height;
	
	public ItemFile(
			String fileName ,
			ItemFileFromEnum from ,
			int w ,
			int h )
	{
		this.fileName = fileName;
		this.fileFrom = from;
		width = w;
		height = h;
	}
	
	public Bitmap getBitmap(
			Context context )
	{
		if( fileFrom == ItemFileFromEnum.selfresource )
		{
			int id = Integer.parseInt( fileName );
			Bitmap bmp = Tools.readBitmapFromResourceId( context , id );
			return Tools.BitmapToSmallBitamp( bmp , width , height );
		}
		else if( fileFrom == ItemFileFromEnum.selfassets )
		{
		}
		return null;
	}
}
