package com.cooee.phenix;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;


/**
 * The edit text that reports back when the back key has been pressed.
 */
public class ExtendedEditText extends EditText
{
	
	/**
	 * Implemented by listeners of the back key.
	 */
	public interface OnBackKeyListener
	{
		
		public boolean onBackKey();
	}
	
	private OnBackKeyListener mBackKeyListener;
	
	public ExtendedEditText(
			Context context )
	{
		super( context );
	}
	
	public ExtendedEditText(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
	}
	
	public ExtendedEditText(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
	}
	
	public void setOnBackKeyListener(
			OnBackKeyListener listener )
	{
		mBackKeyListener = listener;
	}
	
	@Override
	public boolean onKeyPreIme(
			int keyCode ,
			KeyEvent event )
	{
		// If this is a back key, propagate the key back to the listener
		if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
		{
			if( mBackKeyListener != null )
			{
				return mBackKeyListener.onBackKey();
			}
			return false;
		}
		return super.onKeyPreIme( keyCode , event );
	}
}
