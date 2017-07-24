package com.cooee.update;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class UpdateNotifyDialog extends Dialog
{
	
	protected Context mContext;
	protected View.OnClickListener mOnUpdateClickListener;
	protected Button mBtnClose;
	protected Button mBtnUpdate;
	protected TextView mUpdateList;
	protected StringBuilder mUpdateContent = new StringBuilder();
	
	public UpdateNotifyDialog(
			Context context )
	{
		//		this( context ,0);
		this( context , R.style.uiupdate_notify_dialog );
		// TODO Auto-generated constructor stub
	}
	
	public UpdateNotifyDialog(
			Context context ,
			int theme )
	{
		super( context , theme );
		mContext = context;
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "MM" , "onCreate" );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.uiupdate_dialog );
		mBtnClose = (Button)findViewById( R.id.dialog_btn_close );
		mBtnClose.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				UpdateNotifyDialog.this.dismiss();
			}
		} );
		mBtnUpdate = (Button)findViewById( R.id.dialog_btn_update );
		mBtnUpdate.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( mOnUpdateClickListener != null )
				{
					mOnUpdateClickListener.onClick( mBtnUpdate );
				}
				UpdateNotifyDialog.this.dismiss();
			}
		} );
		mUpdateList = (TextView)this.findViewById( R.id.updateList );
		mUpdateList.setText( mUpdateContent );
	}
	
	@Override
	public boolean onKeyUp(
			int keyCode ,
			KeyEvent event )
	{
		// TODO Auto-generated method stub
		//
		switch( event.getKeyCode() )
		{
			case KeyEvent.KEYCODE_BACK:
				return true;
			default:
				break;
		}
		return super.onKeyUp( keyCode , event );
	}
	
	public View.OnClickListener getOnUpdateClickListener()
	{
		return mOnUpdateClickListener;
	}
	
	public void setOnUpdateClickListener(
			View.OnClickListener listener )
	{
		mOnUpdateClickListener = listener;
	}
	
	public void setUpdateList(
			String updateList )
	{
		mUpdateContent.setLength( 0 );
		mUpdateContent.append( updateList );
	}
}
