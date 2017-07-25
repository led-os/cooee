package com.cooee.widget.Transkin;


import android.content.Context;
import android.widget.RemoteViews;


public abstract class baseskin
{
	
	public abstract int getLayout();
	
	public abstract void updateViews(
			Context context ,
			int widgetId ,
			RemoteViews rv );
}
