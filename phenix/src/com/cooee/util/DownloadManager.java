package com.cooee.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.ha.hb.BaseDownloadHelper;
import com.iLoong.launcher.desktop.Disclaimer;
import com.iLoong.launcher.desktop.DisclaimerManager;


/**
 * Represents an item in the launcher.
 */
public class DownloadManager
{
	
	static
	{
		BaseDownloadHelper.initResId(
				R.layout.download_notification_layout ,
				R.id.download_notification_title_id ,
				R.id.download_notification_percent_tip_id ,
				R.id.download_notification_progress_bar_id ,
				R.string.download_toast_tip_insert_SD ,
				R.string.download_toast_tip_internet_err ,
				R.string.download_toast_tip_downloading ,
				R.string.download_notification_tip_downloading ,
				R.string.download_notification_tip_download_fail ,
				R.string.download_notification_tip_download_finish ,
				R.drawable.download_notification_icon );
	}
	private static AlertDialog alertDialog;
	private static Activity activity;
	private static BaseDownloadHelper.DownloadListener listener = new BaseDownloadHelper.DownloadListener() {
		
		@Override
		public void showMessage(
				String arg0 )
		{
			ToastUtils.showToast( activity , arg0 );
		}
		
		@Override
		public void setProxy(
				Object arg0 )
		{
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onInstallSuccess(
				String arg0 )
		{
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onDownloadSuccess()
		{
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onDownloadProgress(
				int arg0 )
		{
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onDownloadFail()
		{
			// TODO Auto-generated method stub
		}
	};
	
	//<i_0010336> liuhailin@2015-03-10 modify begin
	public static void downloadApkCooeeDialog(
			final Activity activity ,
			final String mTitle ,
			final String mPackageName ,
			final boolean mIsInstallAfterDownloadComplete )
	{
		DownloadManager.activity = activity;//将Activity赋值，否则不能显示toast信息 wanghongjian add 【bug:i_0011972】
		//cheyingkun start	//统一phenix桌面弹出提示框的风格。
		//		final ConfirmDialog mConfirmDialog = new ConfirmDialog( activity , R.style.base_dialog_style );//cheyingkun del
		final DefaultDialog mDefaultDialog = new DefaultDialog( activity );//cheyingkun add
		//cheyingkun end
		mDefaultDialog.setTitle( mTitle );
		mDefaultDialog.setContentText( R.string.to_download_content );
		mDefaultDialog.setPositiveButtonText( R.string.positive );
		mDefaultDialog.setNegativeButtonText( R.string.negative );
		mDefaultDialog.setOnClickListener( new DefaultDialog.OnClickListener() {
			
			@Override
			public void onClickPositive(
					View v )
			{
				mDefaultDialog.dismiss();
				// cheyingkun add start //免责声明布局(下载apk时的免责声明判断)
				if( Disclaimer.isNeedShowDisclaimer() )
				{//下载前,弹出免责声明
					DisclaimerManager.getInstance( activity ).showDisclaimer( DisclaimerManager.VISIT_NETWORK_DISCLAIMER_DOWNLOAD_APK , new Disclaimer.OnClickListener() {
						
						@Override
						public void onClickDisclaimerDialogButton(
								View v ,
								int currentStyle )
						{
							if( DisclaimerManager.VISIT_NETWORK_DISCLAIMER_DOWNLOAD_APK == currentStyle )
							{
								switch( v.getId() )
								{
									case R.id.dialog_button_positive:
										BaseDownloadHelper.download( activity , activity , listener , mTitle , mPackageName , true );
										break;
								}
							}
						}
					} );
				}
				else
				{//直接下载
					BaseDownloadHelper.download( activity , activity , listener , mTitle , mPackageName , true );
				}
				// cheyingkun add end
			}
			
			@Override
			public void onClickNegative(
					View v )
			{
				mDefaultDialog.dismiss();
			}
			
			@Override
			public void onClickExit(
					View v )
			{
			}
		} );
		mDefaultDialog.show();
	}
	
	//<i_0010336> liuhailin@2015-03-10 modify end
	public static void downloadApkDialog(
			final Activity activity ,
			int iconId ,
			final String mTitle ,
			final String mPackageName ,
			final boolean mIsInstallAfterDownloadComplete )
	{
		DownloadManager.activity = activity;
		final View layout = View.inflate( activity , R.layout.apk_download_dialog , null );
		AlertDialog.Builder builder = null;
		builder = new AlertDialog.Builder( activity );
		if( iconId != -1 )
		{
			builder.setIcon( iconId );
		}
		builder.setTitle( mTitle );
		TextView text = (TextView)layout.findViewById( R.id.label );
		text.setText( StringUtils.concat( "\"" , mTitle , "\"" , LauncherDefaultConfig.getString( R.string.to_download_content ) ) );
		text.setTextColor( Color.BLACK );
		builder.setPositiveButton( LauncherDefaultConfig.getString( R.string.to_download_ok ) , new OnClickListener() {
			
			@Override
			public void onClick(
					DialogInterface dialog ,
					int which )
			{
				BaseDownloadHelper.download( activity , activity , listener , mTitle , mPackageName , true );
				dialog.dismiss();
				alertDialog = null;
			}
		} );
		builder.setNegativeButton( LauncherDefaultConfig.getString( R.string.to_download_cancel ) , new OnClickListener() {
			
			@Override
			public void onClick(
					DialogInterface dialog ,
					int which )
			{
				dialog.dismiss();
				alertDialog = null;
			}
		} );
		builder.setOnCancelListener( new OnCancelListener() {
			
			@Override
			public void onCancel(
					DialogInterface dialog )
			{
				// TODO Auto-generated method stub
				alertDialog = null;
			}
		} );
		// builder.create().show();
		builder.setView( layout );
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	public static void closeAlertDialog()
	{
		if( alertDialog != null )
		{
			alertDialog.dismiss();
			alertDialog = null;
		}
	}
}
