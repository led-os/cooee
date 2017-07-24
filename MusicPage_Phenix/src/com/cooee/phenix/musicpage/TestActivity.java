package com.cooee.phenix.musicpage;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicandcamerapage.utils.MemoryUtils;


public class TestActivity extends Activity
{
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		//
		long l1 = MemoryUtils.getCurEnabledMemory( this );
		this.setContentView( R.layout.music_page_view_layout );
		long l2 = MemoryUtils.getCurEnabledMemory( this );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , StringUtils.concat( "shlt , :" , ( l2 - l1 ) ) );
	}
}
