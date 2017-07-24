package com.cooee.phenix.Functions.DynamicEntry;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.phenix.R;


public class DynamicEntryServiceActivity extends Activity
{
	
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		Intent intent = getIntent();
		int disclaimer = intent.getIntExtra( "disclaimer" , -1 );
		if( disclaimer == -1 )
		{
			finish();
		}
		setContentView( R.layout.dynamic_disclaimer_dialog );
		Button exit = (Button)findViewById( R.id.exit );
		Button confirm_ok = (Button)findViewById( R.id.confirm_ok );
		confirm_ok.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				OperateDynamicProxy.getInstance().notifyDisclaimerCancel();
				OperateDynamicProxy.getInstance().showDynamicUpdateWaiteDialog();
				finish();
			}
		} );
		if( 0 == disclaimer )
		{
			exit.setVisibility( View.VISIBLE );
			exit.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					finish();
				}
			} );
		}
		else
		{
			exit.setVisibility( View.GONE );
		}
	}
}
