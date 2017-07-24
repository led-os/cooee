package com.cooee.phenix.Functions.Category;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;

import cool.sdk.Category.CategoryConstant;


public class CategoryEmptyActivity extends Activity
{
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		setVisible( false );
		int category_prompt = getIntent().getIntExtra( CategoryConstant.CATEGORY_PROMPT , 0 );
		if( category_prompt == 1 )
		{
			Intent intent = new Intent();
			Context context = getApplicationContext();
			intent.setClass( context , Launcher.class );
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			context.startActivity( intent );
			//悬浮模式不分类
			if( LauncherAppState.getActivityInstance() != null )
				OperateHelp.getInstance( this ).startCategory();
		}
		else
		{
		}
		finish();
	}
}
