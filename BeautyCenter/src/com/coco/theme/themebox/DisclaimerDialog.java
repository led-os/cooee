package com.coco.theme.themebox;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.iLoong.base.themebox.R;


public class DisclaimerDialog extends Dialog
{
	
	private Context mContext;
	private OnClickListener clickListener;
	private CheckBox mCheckBox;
	private boolean isNeedWarnningNextTime = false;
	
	public DisclaimerDialog(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	public DisclaimerDialog(
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
		Log.v( "oncreate" , "onCreate" );
		super.onCreate( savedInstanceState );
		//inflater = LayoutInflater.from(mContext);
		//contentView = inflater.inflate(R.layout.delete_dialog_layout, null);
		this.setContentView( R.layout.disclaimer_dialog_layout );
		setTitle( R.string.warn );
		mCheckBox = (CheckBox)findViewById( R.id.check_box );
		mCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(
					CompoundButton buttonView ,
					boolean isChecked )
			{
				// TODO Auto-generated method stub
				isNeedWarnningNextTime = isChecked;
			}
		} );
		Button exitButton = (Button)findViewById( R.id.dialog_button_exit );
		Button iKnowButton = (Button)findViewById( R.id.dialog_button_ok );
		exitButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				clickListener.onClick( v , false , isNeedWarnningNextTime );
			}
		} );
		iKnowButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				clickListener.onClick( v , true , isNeedWarnningNextTime );
			}
		} );
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
	
	public void setOnClickListener(
			OnClickListener l )
	{
		clickListener = l;
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
		void onClick(
				View v ,
				boolean isConfirm ,
				boolean isChecked );
	}
}
