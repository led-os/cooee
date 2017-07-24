package com.cooee.theme;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.cooee.phenix.R;


public class ThemeChangeDialog extends Dialog
{
	
	public ThemeChangeDialog(
			Context context ,
			String strMessage )
	{
		this( context , R.style.CategoryCustomProgressDialog , strMessage );
	}
	
	public ThemeChangeDialog(
			Context context ,
			int theme ,
			String strMessage )
	{
		super( context , theme );
		this.setContentView( R.layout.default_loading_progress );
		this.getWindow().getAttributes().gravity = Gravity.CENTER;
		TextView tvMsg = (TextView)this.findViewById( R.id.startLoader_state );
		if( tvMsg != null )
		{
			tvMsg.setText( strMessage );
		}
	}
}
