package com.iLoong.launcher.desktop;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


// cheyingkun add start //免责声明布局
/**
 * 免责声明对话框
 * @author cheyingkun
 */
public class Disclaimer extends Dialog implements View.OnClickListener
{
	
	private OnClickListener clickListener;
	private CheckBox mCheckBox;
	private Button iKnowButton;
	private Button exitButton;
	private Button positiveButton;
	private Button negativeButton;
	//dialog当前样式
	private int currentStyle = 0;
	
	public Disclaimer(
			Context context ,
			int theme ,
			int style )
	{
		super( context , theme );
		currentStyle = style;
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.disclaimer_dialog_layout );
		mCheckBox = (CheckBox)findViewById( R.id.check_box );
		mCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(
					CompoundButton buttonView ,
					boolean isChecked )
			{
				//notice 下次是否通知（此处标志位，当选择了确认时(勾选了复选框时)为false，默认是true）
				if( isChecked )
				{
					saveLoadFlag( false );
				}
				else
				{
					saveLoadFlag( true );
				}
			}
		} );
		iKnowButton = (Button)findViewById( R.id.dialog_button_ok );
		iKnowButton.setOnClickListener( this );
		exitButton = (Button)findViewById( R.id.exit );
		exitButton.setOnClickListener( this );
		positiveButton = (Button)findViewById( R.id.dialog_button_positive );
		positiveButton.setOnClickListener( this );
		negativeButton = (Button)findViewById( R.id.dialog_button_negative );
		negativeButton.setOnClickListener( this );
		if( currentStyle == DisclaimerManager.LAUNCHRE_ONCREATE_DISCLAIMER )
		{
			positiveButton.setVisibility( View.GONE );
			negativeButton.setVisibility( View.GONE );
		}
		else
		{
			mCheckBox.setVisibility( View.GONE );
			iKnowButton.setVisibility( View.GONE );
		}
	}
	
	private void saveLoadFlag(
			boolean flag )
	{
		SharedPreferences prefs = getContext().getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
		prefs.edit().putBoolean( "notice" , flag ).commit();
	}
	
	@Override
	public boolean onKeyUp(
			int keyCode ,
			KeyEvent event )
	{
		switch( event.getKeyCode() )
		{
			case KeyEvent.KEYCODE_BACK:
				if( currentStyle == DisclaimerManager.LAUNCHRE_ONCREATE_DISCLAIMER )
				{
					return true;
				}
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
		void onClickDisclaimerDialogButton(
				View v ,
				int currentStyle );
	}
	
	/**
	 * 是否要显示免责声明对话框
	 * @return true 显示 false 不显示
	 */
	public static boolean isNeedShowDisclaimer()
	{
		if( !LauncherDefaultConfig.SWITCH_ENABLE_DISCLAIMER )
		{
			return false;
		}
		else
		{
			SharedPreferences prefs = LauncherAppState.getInstance().getContext().getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
			boolean notice = prefs.getBoolean( "notice" , true );
			return notice;
		}
	}
	
	// cheyingkun add start//免责声明布局(事件处理)
	@Override
	public void onClick(
			View v )
	{
		Disclaimer.this.dismiss();
		switch( v.getId() )
		{
			case R.id.dialog_button_ok:
				boolean isChecked = mCheckBox.isChecked();
				//notice 下次是否通知（此处标志位，当选择了确认时(勾选了复选框时)为false，默认是true）
				if( isChecked )
				{
					saveLoadFlag( false );
				}
				else
				{
					saveLoadFlag( true );
				}
				clickListener.onClickDisclaimerDialogButton( v , currentStyle );
				break;
			case R.id.exit:
				clickListener.onClickDisclaimerDialogButton( v , currentStyle );
				break;
			case R.id.dialog_button_positive:
				saveLoadFlag( false );
				clickListener.onClickDisclaimerDialogButton( v , currentStyle );
				break;
			case R.id.dialog_button_negative:
				clickListener.onClickDisclaimerDialogButton( v , currentStyle );
				break;
			default:
				break;
		}
	}
	// cheyingkun add end
}
//cheyingkun add end