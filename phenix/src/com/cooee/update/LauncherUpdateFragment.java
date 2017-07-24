package com.cooee.update;


import java.io.File;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.update.UpdateBtnFragment.UpdateBtnCallBack;
import com.cooee.update.UpdateDownloadBtnFragment.UpdateDownloadBtnCallBack;
import com.cooee.update.taskManager.Listener;
import com.cooee.update.taskManager.TaskManager;
import com.cooee.update.taskManager.TaskResult;

import cool.sdk.Uiupdate.UiupdateHelper;
import cool.sdk.download.manager.DlMethod;


/**
 * @author zhangjin
 *
 */
public class LauncherUpdateFragment extends Fragment implements UpdateBtnCallBack , UpdateDownloadBtnCallBack
{
	
	private static final String TAG = "UpdateUi.LauncherUpdateFragment";
	public static int BG_SRC_COLOR = 0xffefeedc;
	public static int BG_DST_COLOR = 0xfffaf9f4;
	public static int NOFITY_ID = 20150701;
	private TextView mVerisonName;
	private TextView mUpdateListTitle;
	private TextView mUpdateList;
	// gaominghui@2017/01/06 ADD START "隐私声明和服务条款"
	private TextView mUpdatePrivacyStatement;
	// gaominghui@2017/01/06 ADD END "隐私声明和服务条款"
	private UpdateBtnFragment mUpdateBtnFrag;//update button container
	private UpdateDownloadBtnFragment mUpdateDownFrag;//update progress container
	//////////////////////////////////////////////////////
	public static String KEY_FIND_NEW = "FIND_NEW";
	public static String KEY_NOFITY_ID = "NOFITY_ID";
	private static DownloadTask mDownTask;
	private UpdateFrontTask mFrontTask;
	/** 全局Context用来弹出安装框，Notify等*/
	private Context mGlobalContext;
	private boolean isUpdateBtnEnable = false;//更新按钮是否能点击
	private static LauncherUpdateFragment currFrag = null;
	
	public LauncherUpdateFragment()
	{
		currFrag = this;
	}
	
	public static LauncherUpdateFragment getCurFrag()
	{
		return currFrag;
	}
	
	@Override
	public View onCreateView(
			LayoutInflater inflater ,
			ViewGroup container ,
			Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.ui_update , container , false );
	}
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );
		UpdateDownloadManager.getInstance( getActivity() ).closeNotifyDialog();
		//初始化所有控件
		mVerisonName = (TextView)this.getActivity().findViewById( R.id.version_name );
		String versionName = shapeVersionName( getVersionName() );
		if( TextUtils.isEmpty( versionName ) == false )
		{
			mVerisonName.setText( versionName );
		}
		//初始化更新内容
		mUpdateList = (TextView)this.getActivity().findViewById( R.id.updateList );
		mUpdateListTitle = (TextView)this.getActivity().findViewById( R.id.updateListTitle );
		//////////////////////////////////////////////////////////////////////		
		isUpdateBtnEnable = false;
		// gaominghui@2017/01/06 ADD START "隐私声明和服务条款"
		mUpdatePrivacyStatement = (TextView)this.getActivity().findViewById( R.id.updatePrivacyStatement );
		mUpdatePrivacyStatement.setHighlightColor( getResources().getColor( android.R.color.transparent ) );
		mUpdatePrivacyStatement.setMovementMethod( LinkMovementMethod.getInstance() );
		// gaominghui@2017/01/06 ADD END "隐私声明和服务条款"
		//初始化更新按钮
		showUpdateBtn();
		//正在下载中，则继续下载
		// zhangjin@2015/12/03 ADD START
		mDownTask = UpdateDownloadManager.getInstance( getActivity() ).getDownTask();
		if( mDownTask != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "onActivityCreated down task is running" );
			onUpdateAnim( true );
			if( mDownTask.getRunState() || mDownTask.getProgress() == 100 )
			{
				showProgressBtn();
			}
			else
			{
				showProgressPause();
			}
			mDownTask.removeListenerByClass( mDownListener.getClass() );
			mDownTask.addListener( mDownListener );
		}
		else if( isFindNewVersion() )
		{
			onUpdateAnim( true );
		}
		else
		{
			mUpdateListTitle.setVisibility( View.INVISIBLE );
			mUpdateList.setText( R.string.updateVersionChecking );
			checkUpdate();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.iLoong.launcher.update.UpdateBtnFragment.UpdateBtnCallBack#updateBtnClick()
	 */
	@Override
	public void updateBtnClick()
	{
		// TODO Auto-generated method stub
		if( isUpdateBtnEnable == false )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "updateBtnClick error for isUpdateBtnEnable is false" );
			return;
		}
		if( UiupdateHelper.getInstance( getActivity() ).newDataHasDown() )
		{
			//直接安装
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "updateBtnClick install " );
			boolean install = UpdateUtil.InstallNormalApk( LauncherUpdateFragment.this.getActivity() , getApkPath() );
			if( install )
			{
				return;
			}
		}
		//step 2 显示进度条，开始下载
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "updateBtnClick showProgress" );
		Runnable runnable = new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				showProgressBtn();
				startDownload();
			}
		};
		if( DlMethod.IsWifiConnected( getActivity() ) == false && DlMethod.IsNetworkAvailable( getActivity() ) )
		{
			showGprsDlgWhenDownload( runnable );
		}
		else
		{
			runnable.run();
		}
		//
		// zhangjin@2015/12/03 DEL START
		// showNotify();
		// zhangjin@2015/12/03 DEL END
	}
	
	/* (non-Javadoc)
	 * @see com.iLoong.launcher.update.UpdateDownloadBtnFragment.UpdateDownloadBtnCallBack#stopDownload()
	 */
	@Override
	public void stopDownload()
	{
		// TODO Auto-generated method stub
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , " stop Download" );
		// zhangjin@2015/12/02 UPD START
		//mDownTask.stopDownload();
		//mDownTask.clearListeners();
		//showUpdateBtn();
		//cancelNotify();
		UpdateDownloadManager.getInstance( getActivity() ).stopDownload();
		mDownTask.removeListenerByClass( mDownListener.getClass() );
		showUpdateBtn();
		//		cancelNotify();
		// zhangjin@2015/12/02 UPD END
	}
	
	/* (non-Javadoc)
	 * @see com.iLoong.launcher.update.UpdateDownloadBtnFragment.UpdateDownloadBtnCallBack#progressBtnClick()
	 */
	@Override
	public void progressBtnClick()
	{
		if( mDownTask != null && mDownTask.getRunState() )
		{
			showProgressPause();
			// zhangjin@2015/12/02 UPD START
			//mDownTask.pauseDownload();
			UpdateDownloadManager.getInstance( getActivity() ).pauseDownload();
			// zhangjin@2015/12/02 UPD END
		}
		else
		{
			Runnable runnable = new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					showProgressBtn();
					startDownload();
				}
			};
			if( DlMethod.IsWifiConnected( getActivity() ) == false && DlMethod.IsNetworkAvailable( getActivity() ) )
			{
				showGprsDlgWhenDownload( runnable );
			}
			else
			{
				runnable.run();
			}
		}
	}
	
	/**
	 * 有更新时播放动画
	 */
	private void onUpdateAnim(
			boolean newVersion )
	{
		//step 1 设置更新说明
		if( newVersion )
		{
			mUpdateListTitle.setVisibility( View.VISIBLE );
			setUpdateContent();
			isUpdateBtnEnable = true;
		}
		else
		{
			if( UpdateUtil.FragUpdatable( mUpdateBtnFrag ) )
			{
				// zhangjin@2015/12/30 i_0013203 ADD START
				if( DlMethod.IsNetworkAvailable( getGlobalContext() ) )
				{
					mUpdateList.setText( R.string.updateNoNewVersion );
				}
				else
				{
					mUpdateList.setText( R.string.updateDownFailInfo );
				}
				// zhangjin@2015/12/30 ADD END
				mUpdateBtnFrag.setClickable( false );
				//isUpdateBtnEnable = true;
			}
		}
	}
	
	/**
	 * 手动检查更新流程
	 */
	private void checkUpdate()
	{
		if( mFrontTask == null )
		{
			mFrontTask = new UpdateFrontTask( LauncherUpdateFragment.this.getActivity() , null );
			Listener downListener = new Listener() {
				
				@Override
				public void onResult(
						TaskResult result )
				{
					TaskResult res = result;
					//-1:已经是最新版本，0:未下载，1:已经下载好
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "front task onResult res= " , res.mCode ) );
					if( res.mCode == -1 )
					{
						onUpdateAnim( false );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , "front task result success" );
					}
					else if( res.mCode == 0 )
					{
						onUpdateAnim( true );
						// zhangjin@2015/12/03 测试用 ADD START
						// showUpdateDialog( LauncherUpdateFragment.this.getActivity() );
						// zhangjin@2015/12/03 ADD END
					}
					else if( res.mCode == 1 )
					{
						onUpdateAnim( true );
						// zhangjin@2015/12/03 测试用 ADD START
						// showUpdateDialog( LauncherUpdateFragment.this.getActivity() );
						// zhangjin@2015/12/03 ADD END						
					}
					else
					{
						// 下载中断或者失败
						onUpdateAnim( false );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , "front task result fail" );
					}
				}
			};
			mFrontTask.addListener( downListener );
		}
		if( mFrontTask.getRunState() == false )
		{
			TaskManager.execute( mFrontTask );
		}
	}
	
	/**
	 * 点击下载时，提示当前为gprs下载
	 */
	private void showGprsDlgWhenDownload(
			final Runnable runable )
	{
		AlertDialog.Builder buidler = new AlertDialog.Builder( this.getActivity() );
		buidler.setTitle( LauncherDefaultConfig.getString( R.string.updateNotifyTicker ) );
		buidler.setMessage( LauncherDefaultConfig.getString( R.string.updateGprsDlgText ) );
		buidler.setPositiveButton( LauncherDefaultConfig.getString( R.string.updateDialogYes ) , new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(
					DialogInterface dialog ,
					int which )
			{
				// TODO Auto-generated method stub
				runable.run();
			}
		} );
		buidler.setNegativeButton( LauncherDefaultConfig.getString( R.string.updateDialogNo ) , null );
		buidler.create().show();
	}
	
	/**
	 * 开始下载
	 */
	private void startDownload()
	{
		mDownTask = UpdateDownloadManager.getInstance( getActivity() ).startDownload();
		if( mDownTask == null )
		{
			return;
		}
		mDownTask.removeListenerByClass( mDownListener.getClass() );
		mDownTask.addListener( mDownListener );
	}
	
	/**
	 * 显示进度条fragment
	 */
	private void showProgressBtn()
	{
		if( !UpdateUtil.FragUpdatable( this ) )
		{
			return;
		}
		if( mUpdateDownFrag == null )
		{
			mUpdateDownFrag = new UpdateDownloadBtnFragment();
			mUpdateDownFrag.setCallBack( this );
		}
		if( mUpdateDownFrag.isVisible() == false )
		{
			FragmentTransaction tran = getActivity().getFragmentManager().beginTransaction();
			tran.replace( R.id.updateBtnFrag , mUpdateDownFrag );
			tran.commit();
		}
		mUpdateDownFrag.setDownloading( true );
		mUpdateDownFrag.setProgress( UiupdateHelper.getInstance( getActivity() ).getDownProgress() );
	}
	
	/**
	 * 显示进度条fragment之暂停模式
	 */
	private void showProgressPause()
	{
		if( !UpdateUtil.FragUpdatable( this ) )
		{
			return;
		}
		if( mUpdateDownFrag == null )
		{
			mUpdateDownFrag = new UpdateDownloadBtnFragment();
			mUpdateDownFrag.setCallBack( this );
		}
		if( mUpdateDownFrag.isVisible() == false )
		{
			FragmentTransaction tran = getActivity().getFragmentManager().beginTransaction();
			tran.replace( R.id.updateBtnFrag , mUpdateDownFrag );
			tran.commit();
		}
		mUpdateDownFrag.setDownloading( false );
		mUpdateDownFrag.setProgress( UiupdateHelper.getInstance( getActivity() ).getDownProgress() );
	}
	
	/**
	 * 显示更新按钮frag
	 */
	private void showUpdateBtn()
	{
		if( !UpdateUtil.FragUpdatable( this ) )
		{
			return;
		}
		if( mUpdateBtnFrag == null )
		{
			mUpdateBtnFrag = new UpdateBtnFragment();
		}
		getActivity().getFragmentManager().beginTransaction().replace( R.id.updateBtnFrag , mUpdateBtnFrag ).commit();
		mUpdateBtnFrag.setCallBack( this );
	}
	
	/**
	 * 设置更新内容
	 */
	private void setUpdateContent()
	{
		String content = UiupdateHelper.getInstance( getGlobalContext() ).getUpdateContent();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( " update content " , content ) );
		content = content.replace( "\\n" , "\n" );
		mUpdateList.setText( content );
	}
	
	/**
	 * 是否发现新版本
	 * @return
	 */
	private boolean isFindNewVersion()
	{
		Bundle bundle = this.getArguments();
		boolean findNew = false;
		if( bundle != null )
		{
			findNew = bundle.getBoolean( KEY_FIND_NEW );
		}
		return findNew;
	}
	
	/**
	 * 去除VersionName中的日期 V3.0.40471.20150611 变为 V3.0.40471 
	 * @param versionName
	 * @return
	 */
	private String shapeVersionName(
			String versionName )
	{
		if( TextUtils.isEmpty( versionName ) )
		{
			return null;
		}
		int index = versionName.lastIndexOf( '.' );
		if( index == -1 )
		{
			index = versionName.length();
		}
		String version = versionName.substring( 0 , index );
		return version;
	}
	
	/**
	 * 获得版本号名称
	 * @return
	 */
	private String getVersionName()
	{
		PackageInfo info;
		try
		{
			info = this.getActivity().getPackageManager().getPackageInfo( this.getActivity().getPackageName() , 0 );
			return info.versionName;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private String getApkPath()
	{
		String path = UiupdateHelper.getInstance( getGlobalContext() ).getApkPath();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( " apk: " , path ) );
		return path;
	}
	
	private boolean isFileExist(
			String filePath )
	{
		File file = new File( filePath );
		if( file == null || !file.exists() || !file.isFile() || file.length() <= 0 )
		{
			return false;
		}
		return true;
	}
	
	private Context getGlobalContext()
	{
		if( mGlobalContext == null )
		{
			mGlobalContext = UpdateUiManager.getInstance().getGlobalContext();
		}
		return mGlobalContext;
	}
	
	/**
	 * 更新下载进度
	 */
	private void updateProgress(
			int progress )
	{
		//step1 更新页面
		//页面和通知栏同步。
		if( mUpdateDownFrag != null && UpdateUtil.FragUpdatable( this ) && mUpdateDownFrag.isVisible() /*&& mDownTask != null && mDownTask.getRunState()*/)
		{
			mUpdateDownFrag.setProgress( progress );
		}
	}
	
	// zhangjin@2015/12/03 ADD START
	private void updateDownSuccess()
	{
		//开始安装后，按钮还原
		if( isFileExist( getApkPath() ) )
		{
			showUpdateBtn();
		}
	}
	
	// zhangjin@2015/12/03 ADD END
	/**
	 * 下载失败
	 */
	public void updateDownFailed()
	{
		showProgressPause();
		if( mDownTask != null )
		{
			// zhangjin@2015/12/02 UPD START
			//mDownTask.pauseDownload();
			UpdateDownloadManager.getInstance( getActivity() ).pauseDownload();
			// zhangjin@2015/12/02 UPD END
		}
		Toast toast = Toast.makeText( this.getActivity() , R.string.updateDownFailInfo , Toast.LENGTH_SHORT );
		toast.setGravity( Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL , 0 , (int)( 1.5 * toast.getYOffset() ) );
		toast.show();
	}
	
	private Listener mDownListener = new Listener() {
		
		@Override
		public void onProgress(
				Object ... progress )
		{
			super.onProgress( progress );
			Long pro = (Long)progress[0];
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "down progress: " , pro ) );
			updateProgress( pro.intValue() );
		}
		
		@Override
		public void onResult(
				TaskResult result )
		{
			TaskResult res = result;
			//-1:下载失败，其他值下载成功
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "down result is " , res.mCode ) );
			if( res.mCode != -1 )
			{
				//下载成功
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "down result success" );
				updateDownSuccess();
			}
			else
			{
				// 下载中断或者失败
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "down result fail" );
				updateDownFailed();
			}
		}
	};
}
