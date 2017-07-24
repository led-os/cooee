package com.cooee.phenix.musicpage;


// MusicPage
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cooee.phenix.musicandcamerapage.utils.ViewUtils;


public class CooeeTextView extends TextView
{
	
	public CooeeTextView(
			Context context )
	{
		this( context , null );
	}
	
	public CooeeTextView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public CooeeTextView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	public int getTextHeight()
	{
		return ViewUtils.getTextHeight( this );
	}
	
	public int getMarginTop()
	{
		return ViewUtils.getMarginTop( this );
	}
}
