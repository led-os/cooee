package com.cooee.update;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.cooee.phenix.R;


public class UpdateActivity extends Activity
{
	
	private static final String TAG = "UpdateUi.UpdateActivity";
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		getWindow().requestFeature( Window.FEATURE_ACTION_BAR_OVERLAY );
		int version = Build.VERSION.SDK_INT;
		if( version >= 11 )
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayShowHomeEnabled( false );
		}
		setActionBar();
		// Display the fragment as the main content.		
		Fragment mainFrag = new LauncherUpdateFragment();
		Intent intent = getIntent();
		mainFrag.setArguments( intent.getExtras() );
		getFragmentManager().beginTransaction().replace( android.R.id.content , mainFrag ).commit();
	}
	
	private void setActionBar()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled( true );
		actionBar.setCustomView( R.layout.uiupdate_actionbar );
		Drawable back = new ColorDrawable( 0x00000000 );
		actionBar.setBackgroundDrawable( back );
		/////////////////////////////////////////////////////////////////////////////////////////////				
		View btn = findViewById( R.id.actionbarbtn );
		if( btn != null )
		{
			btn.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					UpdateActivity.this.finish();
				}
			} );
		}
	}
}
