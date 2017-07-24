package com.search.kuso;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.cooee.search.R;


public class WebviewLoadingDlg extends Dialog
{
	
	Context mContext = null;
	ImageView view = null;
	Animation rotateAnimation = null;
	
	public WebviewLoadingDlg(
			Context mContext )
	{
		// TODO Auto-generated constructor stub
		super( mContext , android.R.style.Theme_Translucent_NoTitleBar );
		this.mContext = mContext;
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		//		MyR RR = MyR.getMyR( mContext.getApplicationContext() );
		//		if( RR == null )
		//		{
		//			CloseDlg();
		//		}
		setContentView( R.layout.kuso_cool_ml_webview_loading_dlg );
		view = (ImageView)findViewById( R.id.kuso_cool_ml_ivprocessimg );
		rotateAnimation = new RotateAnimation( 0f , 360f , Animation.RELATIVE_TO_SELF , 0.5f , Animation.RELATIVE_TO_SELF , 0.5f );
		rotateAnimation.setDuration( 1500 );
		rotateAnimation.setRepeatMode( Animation.RESTART );
		rotateAnimation.setRepeatCount( -1 );
		AnimationSet aset = new AnimationSet( true );
		aset.setInterpolator( new LinearInterpolator() );
		aset.addAnimation( rotateAnimation );
		view.startAnimation( aset );
	}
	
	static Dialog ssdialog;
	
	public static void ShowDlg(
			Context mContext )
	{
		// TODO Auto-generated method stub
		Log.v( "CATE_HL" , "Show process dlg" );
		if( null != ssdialog )
		{
			ssdialog.dismiss();
			ssdialog = null;
		}
		ssdialog = new WebviewLoadingDlg( mContext );
		ssdialog.show();
	}
	
	public static void CloseDlg()
	{
		// TODO Auto-generated method stub
		Log.v( "CATE_HL" , "close process dlg" );
		if( null != ssdialog )
		{
			ssdialog.dismiss();
			ssdialog = null;
		}
	}
}
