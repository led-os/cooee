package com.cooee.update;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.update.view.TextProgressBar;


public class UpdateDownloadBtnFragment extends Fragment
{
	
	private static final String TAG = "UpdateUi.UpdateDownloadBtnFragment";
	/** 动画时长*/
	public static long ANIM_DURA = 1000;
	private TextProgressBar mDownProgress;
	private UpdateDownloadBtnCallBack mCallBack;
	private int mProgress = 0;
	private boolean mIsDownloading = false;
	
	@Override
	public View onCreateView(
			LayoutInflater inflater ,
			ViewGroup container ,
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		return inflater.inflate( R.layout.uiupdate_progress , container , false );
	}
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onActivityCreated( savedInstanceState );
		mDownProgress = (TextProgressBar)this.getActivity().findViewById( R.id.updateProgressBar );
		mDownProgress.setProgress( mProgress );
		mDownProgress.setText( R.string.updateUpdating );
		if( mDownProgress != null )
		{
			mDownProgress.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , " down progress onClick" );
					if( mCallBack != null )
					{
						mCallBack.progressBtnClick();
					}
				}
			} );
			mDownProgress.setOnLongClickListener( new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(
						View v )
				{
					// TODO Auto-generated method stub
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , " down progress onLongClick" );
					showStopDialog();
					return true;
				}
			} );
		}
	}
	
	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		updateIndicator();
	}
	
	public void setProgress(
			int progress )
	{
		mProgress = progress;
		if( mDownProgress != null )
		{
			updateIndicator();
		}
	}
	
	private void showStopDialog()
	{
		AlertDialog.Builder buidler = new AlertDialog.Builder( this.getActivity() );
		buidler.setTitle( LauncherDefaultConfig.getString( R.string.updateNotifyTicker ) );
		buidler.setMessage( LauncherDefaultConfig.getString( R.string.updateDialogMsg ) );
		buidler.setPositiveButton( LauncherDefaultConfig.getString( R.string.updateDialogYes ) , new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(
					DialogInterface dialog ,
					int which )
			{
				// TODO Auto-generated method stub
				if( mCallBack != null )
				{
					mCallBack.stopDownload();
				}
			}
		} );
		buidler.setNegativeButton( LauncherDefaultConfig.getString( R.string.updateDialogNo ) , null );
		buidler.create().show();
	}
	
	public void setDownloading(
			boolean isDownloading )
	{
		mIsDownloading = isDownloading;
		updateIndicator();
	}
	
	private void updateIndicator()
	{
		if( mIsDownloading )
		{
			setDownloadIndicator();
		}
		else
		{
			setPauseIndicator();
		}
	}
	
	private void setDownloadIndicator()
	{
		if( mDownProgress == null || !UpdateUtil.FragUpdatable( this ) )
		{
			return;
		}
		mDownProgress.setText( R.string.updateUpdating );
		mDownProgress.setProgress( mProgress );
	}
	
	private void setPauseIndicator()
	{
		if( mDownProgress == null || !UpdateUtil.FragUpdatable( this ) )
		{
			return;
		}
		mDownProgress.setText( R.string.updatePause );
		mDownProgress.setProgress( mProgress );
	}
	
	public void setCallBack(
			UpdateDownloadBtnCallBack callBack )
	{
		this.mCallBack = callBack;
	}
	
	public static interface UpdateDownloadBtnCallBack
	{
		
		public void stopDownload();
		
		public void progressBtnClick();
	}
}
