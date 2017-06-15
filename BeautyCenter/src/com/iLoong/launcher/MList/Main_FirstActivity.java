package com.iLoong.launcher.MList;


import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;


public class Main_FirstActivity extends MeMainActivity
{
	
	int app_id = 10005;
	
	public int getId()
	{
		return app_id;
	}
	//	@Override
	//	protected void onCreate(
	//			Bundle savedInstanceState )
	//	{
	//		// TODO Auto-generated method stub
	//		super.onCreate( savedInstanceState );
	//		MELOG.v( "ME_RTFSC" , " ==========  Main_FirstActivity  onCreate==========" );
	//		Intent intent = new Intent();
	//		intent.setClass( getApplicationContext() , MeMainActivity.class );
	//		intent.putExtra( "APP_ID" , getId() );
	//		startActivity( intent );
	//		finish();
	//	}
	//	
	//	@Override
	//	public boolean onKeyDown(
	//			int keyCode ,
	//			KeyEvent event )
	//	{
	//		// TODO Auto-generated method stub
	//		return false;
	//	}
}
