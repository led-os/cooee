package com.cooee.phenix.Functions.DynamicEntry;


import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;

import cool.sdk.Category.CategoryHelper;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class OperateActivity extends Activity
{
	
	private String mPkgName = null;
	private CharSequence mTitle = null;
	private OperateDynamicDL mOperateDynamicDL = null;
	private boolean enableDLManager = false;
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mOperateDynamicDL = new OperateDynamicDL( this );
		enableDLManager = LauncherDefaultConfig.getBoolean( R.bool.switch_enable_operate_folder_use_download_manager );
		Intent intent = getIntent();
		mPkgName = intent.getStringExtra( OperateDynamicMain.PKGNAME_ID );
		//		ShortcutInfo info = OperateDynamicMain.getmAllOperateIcon().get( mPkgName );
		if( mPkgName != null )
		{
			int state = intent.getIntExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , Constants.DL_STATUS_NOTDOWN );//判断运营文件夹中的图标是否下载完成
			mTitle = intent.getStringExtra( "title" );
			if( state == Constants.DL_STATUS_SUCCESS )
			{//若是下载完成，则直接跳出提示安装
				installApkByPkgName( mPkgName );
			}
			else
			{//若未下载完成，则跳出下载提示框
				setContentView( R.layout.dynamic_download_dialog_layout );
				Button exit = (Button)findViewById( R.id.exit );
				TextView iconText = (TextView)findViewById( R.id.download_icontext );
				Button download = (Button)findViewById( R.id.download );
				exit.setOnClickListener( clickListener );
				download.setOnClickListener( clickListener );
				iconText.setText( mTitle );
				iconText.setCompoundDrawables( null , Utilities.createIconDrawable( (Bitmap)intent.getParcelableExtra( "bitmap" ) ) , null , null );
				this.setFinishOnTouchOutside( false );
			}
		}
		else
		{
			finish();
		}
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(
				View v )
		{
			// TODO Auto-generated method stub
			if( v.getId() == R.id.exit )
			{
				finish();
			}
			else if( v.getId() == R.id.download )
			{
				if( enableDLManager )
				{
					DlManager.getInstance().downloadFile( getApplicationContext() , mTitle.toString() , mPkgName , false );
				}
				else
				{
					mOperateDynamicDL.downloadApp( getApplicationContext() , mPkgName , mTitle.toString() );
				}
				finish();
			}
		}
	};
	
	/**
	 * 通过包名打开安装界面
	 * @param pkgName
	 */
	private void installApkByPkgName(
			String pkgName )
	{
		if( enableDLManager )
		{
			DlManager.getInstance().installApkByPackageName( pkgName );
		}
		else
		{
			CoolDLMgr dlMgr = CategoryHelper.getInstance( this ).getCoolDLMgrApk();
			dl_info dlInfo = dlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
			Intent intent = new Intent( Intent.ACTION_VIEW );
			intent.setDataAndType( Uri.fromFile( new File( dlInfo.getFilePath() ) ) , "application/vnd.android.package-archive" );
			startActivity( intent );
		}
		finish();
	}
}
