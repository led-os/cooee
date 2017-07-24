package com.cooee.util;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cooee.phenix.R;


// cheyingkun add start //统一phenix桌面弹出提示框的风格。
public class DefaultDialog extends Dialog implements android.view.View.OnClickListener
{
	
	private OnClickListener clickListener;
	private Bundle mTag;
	private Button exitButton;
	private Button positiveButton;
	private Button negativeButton;
	private TextView warn;
	private TextView contentText;
	
	public DefaultDialog(
			Context context )
	{
		super( context , R.style.Disclaimer_dialog );
		setContentView( R.layout.launcher_default_dialog );
		//找控件
		findView();
		//设置监听
		setListener();
	}
	
	public DefaultDialog(
			Context context ,
			int theme )
	{
		super( context , theme );
		setContentView( R.layout.launcher_default_dialog );
		//找控件
		findView();
		//设置监听
		setListener();
	}
	
	private void findView()
	{
		warn = (TextView)findViewById( R.id.warn );
		contentText = (TextView)findViewById( R.id.dialog_content );
		exitButton = (Button)findViewById( R.id.exit );
		positiveButton = (Button)findViewById( R.id.dialog_button_positive );
		negativeButton = (Button)findViewById( R.id.dialog_button_negative );
	}
	
	private void setListener()
	{
		exitButton.setOnClickListener( this );
		positiveButton.setOnClickListener( this );
		negativeButton.setOnClickListener( this );
	}
	
	/**
	 * Interface definition for a callback to be invoked when a view is clicked.
	 */
	public interface OnClickListener
	{
		
		/**
		 * Called when a view has been clicked.
		 *
		 * @param v The view that was clicked.
		 */
		void onClickPositive(
				View v );
		
		void onClickNegative(
				View v );
		
		void onClickExit(
				View v );
	}
	
	@Override
	public void onClick(
			View v )
	{
		DefaultDialog.this.dismiss();
		switch( v.getId() )
		{
			case R.id.exit:
				clickListener.onClickExit( v );
				break;
			case R.id.dialog_button_positive:
				clickListener.onClickPositive( v );
				break;
			case R.id.dialog_button_negative:
				clickListener.onClickNegative( v );
				break;
			default:
				break;
		}
	}
	
	public void setOnClickListener(
			OnClickListener listener )
	{
		clickListener = listener;
	}
	
	public void setTitle(
			String title )
	{
		warn.setText( title );
	}
	
	public void setContentText(
			String title )
	{
		contentText.setText( title );
	}
	
	public void setTitle(
			int resid )
	{
		warn.setText( resid );
	}
	
	public void setPositiveButtonText(
			int resid )
	{
		positiveButton.setText( resid );
	}
	
	public void setNegativeButtonText(
			int resid )
	{
		negativeButton.setText( resid );
	}
	
	public void setPositiveButtonText(
			String title )
	{
		positiveButton.setText( title );
	}
	
	public void setNegativeButtonText(
			String title )
	{
		negativeButton.setText( title );
	}
	
	public void setContentText(
			int resid )
	{
		contentText.setText( resid );
	}
	
	public Bundle getTag()
	{
		return mTag;
	}
	
	public void setTag(
			Bundle tag )
	{
		this.mTag = tag;
	}
}
//cheyingkun add end
