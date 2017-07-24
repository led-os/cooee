/***/
package com.coco.wallpaper.wallpaperbox;


import com.iLoong.base.themebox.R;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;


/**
 * @author gaominghui  2015年9月24日
 */
public class WallpaperDialog extends DialogFragment
{
	
	private RadioGroup radioGroup;
	private IWallpaperDialog wallpaperInterface;
	private ProgressDialog setLockWallpaperDialog;
	
	/**
	 * @param from_whichActivity
	 */
	public WallpaperDialog(
			IWallpaperDialog activity )
	{
		super();
		wallpaperInterface = activity;
	}
	
	@Override
	public View onCreateView(
			LayoutInflater inflater ,
			ViewGroup container ,
			Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.brzh_setwallpepr_dialog , null );
		radioGroup = (RadioGroup)view.findViewById( R.id.wallpaper_radioGroup );
		return view;
	}
	
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setCancelable( true );
		//可以设置dialog的显示风格，如style为STYLE_NO_TITLE，将不显示title。theme为0，表示由系统选择合适的theme。
		int style = DialogFragment.STYLE_NO_FRAME , theme = 0;
		setStyle( style , theme );
	}
	
	/*// gaominghui@2015/09/16 ADD START 设置锁屏壁纸的等待对话框
	private void showSetLockWallpaperDialog()
	{
		setLockWallpaperDialog = new ProgressDialog(mContext);
		setLockWallpaperDialog.setSecondaryProgress( ProgressDialog.STYLE_SPINNER );
		setLockWallpaperDialog.setMessage( this.getResources().getString( R.string.changingWallpaper ) );
		setLockWallpaperDialog.setCancelable( false );
		setLockWallpaperDialog.show();
	}
	
	public void cancelSetLockWallpaperDialog()
	{
		if( setLockWallpaperDialog != null )
			setLockWallpaperDialog.dismiss();
	}*/
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onActivityCreated( savedInstanceState );
		radioGroup.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(
					RadioGroup group ,
					int checkedId )
			{
				// TODO Auto-generated method stub
				switch( checkedId )
				{
					case R.id.setWallpaper_radioButton:
						wallpaperInterface.setDesktopWallpaper();
						break;
					case R.id.setLockWallpaper_radioButton:
						//showSetLockWallpaperDialog();
						wallpaperInterface.setLockWallpaper();
						//cancelSetLockWallpaperDialog();
						break;
					case R.id.setBoth_radioButton:
						wallpaperInterface.setDesktopWallpaper();
						wallpaperInterface.setLockWallpaper();
						break;
					default:
						break;
				}
				dismiss();
			}
		} );
	}
}
