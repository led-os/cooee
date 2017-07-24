package com.cooee.framework.function.DynamicEntry.Dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.launcher.framework.R;


public class DynamicEntryUpdateWaiteDialog extends Dialog
{
	
	public DynamicEntryUpdateWaiteDialog(
			Context context ,
			int theme )
	{
		super( context , theme );
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		setContentView( R.layout.category_wait );
		setCanceledOnTouchOutside( false );
		setCancelable( false );
		setOnCancelListener( new OnCancelListener() {
			
			@Override
			public void onCancel(
					DialogInterface arg0 )
			{
				// TODO Auto-generated method stub
			}
		} );
		OperateDynamicProxy.getInstance().directlyShow();
	}
	
	public void quit(
			boolean success )
	{
		this.dismiss();
		if( success )
		{
			Toast.makeText( getContext() , R.string.dynamic_update_success , Toast.LENGTH_SHORT ).show();
		}
		else
		{
			Toast.makeText( getContext() , R.string.dynamic_update_fail , Toast.LENGTH_SHORT ).show();
		}
	}
}
