package com.coco.shortcut.shortcutbox;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.ActivityManager;
import com.coco.theme.themebox.MainActivity;
import com.coco.theme.themebox.PreViewGallery;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.database.model.DownloadThemeItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;
import com.coco.theme.themebox.util.ContentConfig;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Tools;
import com.cooee.shell.sdk.CooeeSdk;
import com.iLoong.base.themebox.R;


public class OperateContentPreviewActivity extends Activity
{
	
	private ScrollView previewScroll;
	private DownModule downModule;
	private RelativeLayout relativeNormal;
	private RelativeLayout relativeDownload;
	private OperateInformation information;
	private SeekBar scrollGallery;
	private PreViewGallery galleryPreview;
	private String packageName;
	private String destClassName;
	private Context mContext;
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		ActivityManager.pushActivity( this );
		super.onCreate( savedInstanceState );
		this.requestWindowFeature( Window.FEATURE_NO_TITLE );
		mContext = this;
		setContentView( R.layout.preview_hot );
		scrollGallery = (SeekBar)findViewById( R.id.scrollGallery );
		galleryPreview = (PreViewGallery)findViewById( R.id.galleryPreview );
		downModule = DownModule.getInstance( this );
		Intent intent = this.getIntent();
		packageName = intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME );
		destClassName = intent.getStringExtra( UtilsBase.EXTRA_CLASS_NAME );
		if( destClassName == null || destClassName.equals( "" ) )
		{
			destClassName = "";
		}
		CooeeSdk.initCooeeSdk( this );
		loadOperateInformation( true );
		updateShowInfo();
		{
			scrollGallery.setThumbOffset( -2 );
			scrollGallery.setEnabled( false );
			galleryPreview.setOnItemSelectedListener( new OnItemSelectedListener() {
				
				@Override
				public void onItemSelected(
						AdapterView<?> parent ,
						View view ,
						int position ,
						long id )
				{
					scrollGallery.setMax( parent.getAdapter().getCount() - 1 );
					scrollGallery.setProgress( position );
				}
				
				@Override
				public void onNothingSelected(
						AdapterView<?> parent )
				{
					scrollGallery.setMax( parent.getAdapter().getCount() - 1 );
					scrollGallery.setProgress( 0 );
				}
			} );
		}
		previewScroll = (ScrollView)findViewById( R.id.previewScroll );
		final int apiLevel = Build.VERSION.SDK_INT;
		if( apiLevel >= 9 )
		{ // 2.3
			previewScroll.setOverScrollMode( View.OVER_SCROLL_NEVER );
		}
		relativeNormal = (RelativeLayout)findViewById( R.id.layoutNormal );
		relativeDownload = (RelativeLayout)findViewById( R.id.layoutDownload );
		reLayoutScroll();
		IntentFilter screenFilter = new IntentFilter();
		screenFilter.addAction( UtilsBase.ACTION_PREVIEW_CHANGED );
		screenFilter.addAction( UtilsBase.ACTION_DOWNLOAD_SIZE_CHANGED );
		screenFilter.addAction( UtilsBase.ACTION_DOWNLOAD_STATUS_CHANGED );
		registerReceiver( previewReceiver , screenFilter );
		// 注册删除事件
		IntentFilter pkgFilter = new IntentFilter();
		pkgFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		pkgFilter.addAction( Intent.ACTION_PACKAGE_ADDED );
		pkgFilter.addDataScheme( "package" );
		registerReceiver( previewReceiver , pkgFilter );
		updateShowStatus();
		// 监听返回按钮
		LinearLayout btnReturn = (LinearLayout)findViewById( R.id.btnReturn );
		btnReturn.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				finish();
			}
		} );
		// 分享
		ImageButton btnShare = (ImageButton)findViewById( R.id.btnShare );
		btnShare.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
			}
		} );
		// 更多
		Button btnMore = (Button)findViewById( R.id.btnMore );
		btnMore.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				smoothScrollMore();
			}
		} );
		if( FunctionConfig.isThemeMoreShow() )
		{
			btnMore.setVisibility( View.VISIBLE );
		}
		else
			btnMore.setVisibility( View.GONE );
		// 打开按钮
		Button btnApply = (Button)findViewById( R.id.btnApply );
		btnApply.setText( R.string.open_operate );
		btnApply.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				if( destClassName == null || destClassName.equals( "" ) )
				{
					destClassName = queryClassName( packageName );
				}
				if( destClassName != null && !destClassName.equals( "" ) )
				{
					try
					{
						Intent intent = new Intent( Intent.ACTION_MAIN );
						ComponentName comp = new ComponentName( packageName , destClassName );
						intent.setComponent( comp );
						startActivity( intent );
					}
					catch( Exception e )
					{
						e.printStackTrace();
						Toast.makeText( mContext , R.string.activity_not_found , Toast.LENGTH_SHORT ).show();
					}
				}
				// @2015/03/11 ADD START 友盟统计美华中心退出
				if( FunctionConfig.isUmengStatistics_key() )
				{
					MainActivity.statisticsExitBeautyCenter( mContext );
				}
				// @2015/03/11 ADD END
				ActivityManager.KillActivity();
			}
		} );
		// 安装
		Button btnInstall = (Button)findViewById( R.id.btnInstall );
		btnInstall.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				downModule.installApk( packageName , DownloadList.Operate_Type );
			}
		} );
		// 暂停
		relativeDownload.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( relativeDownload.findViewById( R.id.linearDownload ).getVisibility() == View.VISIBLE )
				{
					Intent intent = new Intent( UtilsBase.ACTION_PAUSE_DOWNLOAD_APK );
					intent.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , packageName );
					if( information != null )
						intent.putExtra( "apkname" , information.getDisplayName() );
					else
						intent.putExtra( "apkname" , packageName );
					sendBroadcast( intent );
					switchToPause();
				}
				else
				{
					Intent intent = new Intent( UtilsBase.ACTION_START_DOWNLOAD_APK );
					intent.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , packageName );
					if( information != null )
						intent.putExtra( "apkname" , information.getDisplayName() );
					else
						intent.putExtra( "apkname" , packageName );
					sendBroadcast( intent );
					switchToDownloading();
				}
			}
		} );
		// 下载按钮
		Button btnDown = (Button)findViewById( R.id.btnDownload );
		btnDown.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( !com.coco.theme.themebox.StaticClass.isAllowDownloadWithToast( mContext ) )
				{
					return;
				}
				String enginePKG = information.getEnginepackname();
				if( enginePKG != null && !enginePKG.equals( "" ) && !enginePKG.equals( "null" ) )
				{
					if( !Tools.isAppInstalled( mContext , enginePKG ) )
					{// 第三方引擎没有安装
						Tools.showNoticeDialog( mContext , enginePKG , information.getEnginedesc() , information.getEngineurl() , information.getEnginesize() );
						return;
					}
				}
				DownloadThemeService dSv = new DownloadThemeService( mContext );
				DownloadThemeItem dItem = dSv.queryByPackageName( packageName , DownloadList.Operate_Type );
				if( dItem == null )
				{
					// //下载统计
					// StatisticsExpand.StartDown(mContext, packageName);
					// Log.v("Statistics","down packname ="+packageName);
					new File( PathTool.getDownloadingApp( packageName ) ).delete();
					dItem = new DownloadThemeItem();
					dItem.copyFromThemeInfo( information.getInfoItem() );
					dItem.setDownloadStatus( DownloadStatus.StatusDownloading );
					dSv.insertItem( dItem );
				}
				else
				{
					// //继续下载统计
					// StatisticsExpand.ContinueDown(mContext, packageName);
					// Log.v("Statistics","continuedown packname ="+packageName);
				}
				loadOperateInformation( false );
				updateShowStatus();
				Intent intent = new Intent();
				intent.setAction( UtilsBase.ACTION_START_DOWNLOAD_APK );
				intent.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , packageName );
				if( information != null )
					intent.putExtra( "apkname" , information.getDisplayName() );
				else
					intent.putExtra( "apkname" , packageName );
				sendBroadcast( intent );
			}
		} );
		// 购买
		Button butBuy = (Button)findViewById( R.id.btnBuy );
		//		butBuy.setOnClickListener( new View.OnClickListener() {
		//			
		//			@Override
		//			public void onClick(
		//					View v )
		//			{
		//				// TODO Auto-generated method stub
		//				CooeePaymentInfo paymentInfo = new CooeePaymentInfo();
		//				paymentInfo.setPrice( information.getPrice() );
		//				paymentInfo.setPayDesc( information.getDisplayName() );
		//				String payid = information.getPricePoint();
		//				Log.v( "themebox" , "payid lock = " + payid );
		//				if( payid == null || payid.equals( "" ) || payid.equals( "null" ) )
		//				{
		//					paymentInfo.setPayId( FunctionConfig.getCooeePayID( information.getPrice() ) );
		//				}
		//				else
		//				{
		//					paymentInfo.setPayId( payid );
		//				}
		//				paymentInfo.setCpId( information.getThirdparty() );
		//				paymentInfo.setPayName( information.getDisplayName() );
		//				paymentInfo.setPayType( CooeePaymentInfo.PAY_TYPE_EVERY_TIME );
		//				paymentInfo.setNotify( new PaymentResultReceiver() );
		//				CooeePayment.getInstance().startPayService( mContext , paymentInfo );
		//			}
		//		} );
		// 删除
		Button btnDelete = (Button)findViewById( R.id.btnDelete );
		btnDelete.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// //删除统计
				// StatisticsExpand.Delete(mContext, packageName);
				// Log.v("Statistics","delete packname ="+packageName);
				//
				Intent intent = new Intent( UtilsBase.ACTION_PAUSE_DOWNLOAD_APK );
				intent.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , packageName );
				if( information != null )
					intent.putExtra( "apkname" , information.getDisplayName() );
				else
					intent.putExtra( "apkname" , packageName );
				sendBroadcast( intent );
				DownloadThemeService dSv = new DownloadThemeService( mContext );
				dSv.deleteItem( packageName , DownloadList.Operate_Type );
				new File( PathTool.getDownloadingApp( packageName ) ).delete();
				// ***********
				intent = new Intent( UtilsBase.ACTION_DOWNLOAD_STATUS_CHANGED );
				intent.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , packageName );
				sendBroadcast( intent );
				updateShowStatus();
			}
		} );
		// 卸载按钮
		Button btnUninstall = (Button)findViewById( R.id.btnUninstall );
		btnUninstall.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// 卸载统计
				// StatisticsExpand.StartDown(mContext, packageName);
				// Log.v("Statistics","uninstall packname ="+packageName);
				removePackage();
			}
		} );
	}
	
	@Override
	protected void onNewIntent(
			Intent intent )
	{
		// TODO Auto-generated method stub
		super.onNewIntent( intent );
		packageName = intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME );
		destClassName = intent.getStringExtra( UtilsBase.EXTRA_CLASS_NAME );
		String path = intent.getStringExtra( "CustomRootPath" );
		if( destClassName == null || destClassName.equals( "" ) )
		{
			destClassName = "";
		}
		loadOperateInformation( true );
		updateShowInfo();
		updateShowStatus();
		if( previewScroll.getScrollY() != 0 )
		{
			previewScroll.smoothScrollTo( 0 , 0 );
		}
	}
	
	private void updateProgressSize()
	{
		if( findViewById( R.id.linearDownload ).getVisibility() == View.VISIBLE )
		{
			ProgressBar progressBar = (ProgressBar)findViewById( R.id.progressBarDown );
			progressBar.setProgress( information.getDownloadPercent() );
			TextView text = (TextView)findViewById( R.id.textDownPercent );
			text.setText( getString( R.string.textDownloading , information.getDownloadPercent() ) );
		}
		else
		{
			ProgressBar progressBar = (ProgressBar)findViewById( R.id.progressBarPause );
			progressBar.setProgress( information.getDownloadPercent() );
			TextView text = (TextView)findViewById( R.id.textPausePercent );
			text.setText( getString( R.string.textPause , information.getDownloadPercent() ) );
		}
	}
	
	@Override
	protected void onDestroy()
	{
		ActivityManager.popupActivity( this );
		unregisterReceiver( previewReceiver );
		downModule.dispose();
		if( galleryPreview.getAdapter() != null )
		{
			BaseAdapter adapter = (BaseAdapter)galleryPreview.getAdapter();
			if( adapter instanceof PreviewAdapter )
			{
				( (PreviewAdapter)adapter ).onDestory();
			}
		}
		super.onDestroy();
	}
	
	private void updateShowInfo()
	{
		TextView text = (TextView)findViewById( R.id.textAppName );
		text.setText( information.getDisplayName() );
		TextView viewAuthor = (TextView)findViewById( R.id.author );
		TextView viewInfo = (TextView)findViewById( R.id.info );
		if( !FunctionConfig.isThemeMoreShow() )
		{
			viewAuthor.setVisibility( View.GONE );
			viewInfo.setVisibility( View.GONE );
			return;
		}
		else
		{
			viewAuthor.setVisibility( View.VISIBLE );
			viewInfo.setVisibility( View.VISIBLE );
		}
		String author = getString( R.string.previewSize , information.getApplicationSize() / 1024 , information.getAuthor( mContext ) );
		viewAuthor.setText( author );
		String systemLauncher = Locale.getDefault().getLanguage().toString();
		String info = "";
		if( systemLauncher.equals( "zh" ) )
		{
			info = getString( R.string.previewIntroduction ) + "\n" + information.getIntroduction();
		}
		else
		{
			String introduction = information.getIntroduction_en();
			if( introduction.equals( "" ) )
			{
				introduction = information.getIntroduction();
			}
			info = getString( R.string.previewIntroduction ) + "\n" + introduction;
		}
		viewInfo.setText( info );
	}
	
	private void updateInforButton()
	{
		findViewById( R.id.btnDelete ).setVisibility( View.GONE );
		findViewById( R.id.btnUninstall ).setVisibility( View.GONE );
		if( information.isInstalled( this ) )
		{
			if( information.isSystem() )
			{
				return;
			}
			findViewById( R.id.btnUninstall ).setVisibility( View.VISIBLE );
			return;
		}
		if( information.isDownloaded( mContext ) && information.getDownloadStatus() != DownloadStatus.StatusDownloading )
		{
			findViewById( R.id.btnDelete ).setVisibility( View.VISIBLE );
		}
	}
	
	private void updateShowStatus()
	{
		updateInforButton();
		findViewById( R.id.btnSetting ).setVisibility( View.GONE );
		findViewById( R.id.btnShare ).setVisibility( View.GONE );
		relativeDownload.setClickable( false );
		if( information.isInstalled( this ) )
		{
			relativeDownload.setVisibility( View.GONE );
			relativeNormal.setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.GONE );
			relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.GONE );
			findViewById( R.id.btnShare ).setVisibility( View.GONE );
			relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.GONE );
			return;
		}
		if( !information.isDownloaded( mContext ) )
		{
			boolean ispay = Tools.isContentPurchased( this , DownloadList.Operate_Type , packageName );
			if( FunctionConfig.isPriceVisible() && information.getPrice() > 0 && !ispay )
			{
				relativeNormal.setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.VISIBLE );
				return;
			}
			else
			{
				relativeDownload.setVisibility( View.GONE );
				relativeNormal.setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.GONE );
				return;
			}
		}
		if( information.getDownloadStatus() == DownloadStatus.StatusFinish )
		{
			relativeDownload.setVisibility( View.GONE );
			relativeNormal.setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.GONE );
			relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.GONE );
			relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.GONE );
			return;
		}
		relativeDownload.setClickable( true );
		if( information.getDownloadStatus() == DownloadStatus.StatusDownloading )
		{
			switchToDownloading();
		}
		else
		{
			switchToPause();
		}
	}
	
	private void switchToDownloading()
	{
		relativeDownload.setVisibility( View.VISIBLE );
		relativeNormal.setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearDownload ).setVisibility( View.VISIBLE );
		relativeDownload.findViewById( R.id.linearPause ).setVisibility( View.GONE );
		updateProgressSize();
	}
	
	private void switchToPause()
	{
		relativeDownload.setVisibility( View.VISIBLE );
		relativeNormal.setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearDownload ).setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearPause ).setVisibility( View.VISIBLE );
		updateProgressSize();
	}
	
	private void loadOperateInformation(
			boolean reloadGallery )
	{
		OperateService service = new OperateService( mContext );
		information = service.queryOperate( packageName , destClassName );
		if( information.isInstalled( this ) )
		{
			Context dstContext = null;
			try
			{
				dstContext = createPackageContext( packageName , Context.CONTEXT_IGNORE_SECURITY );
			}
			catch( NameNotFoundException e )
			{
				e.printStackTrace();
				return;
			}
			ContentConfig destContent = new ContentConfig();
			destContent.loadOperateConfig( dstContext );
			information.loadInstallDetail( dstContext , destContent );
		}
		if( reloadGallery )
		{
			galleryPreview.setAdapter( new PreviewAdapter( this , packageName , downModule ) );
		}
	}
	
	private String queryClassName(
			String pkgName )
	{
		OperateService service = new OperateService( this );
		ComponentName comName = service.queryComponent( pkgName );
		if( comName == null )
		{
			return "";
		}
		return comName.getClassName();
	}
	
	private BroadcastReceiver previewReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			String actionName = intent.getAction();
			if( actionName.equals( UtilsBase.ACTION_PREVIEW_CHANGED ) )
			{
				SpinnerAdapter apt = galleryPreview.getAdapter();
				if( apt != null && apt instanceof PreviewAdapter )
				{
					( (PreviewAdapter)apt ).reload();
					scrollGallery.setMax( galleryPreview.getCount() );
					scrollGallery.setProgress( galleryPreview.getSelectedItemPosition() );
				}
			}
			else if( actionName.equals( UtilsBase.ACTION_DOWNLOAD_STATUS_CHANGED ) )
			{
				// ***********
				String name = intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME );
				if( name.equals( packageName ) )
				{
					loadOperateInformation( false );
					updateShowStatus();
				}
			}
			else if( actionName.equals( UtilsBase.ACTION_DOWNLOAD_SIZE_CHANGED ) )
			{
				if( packageName.equals( intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME ) ) )
				{
					information.setDownloadSize( intent.getIntExtra( UtilsBase.EXTRA_DOWNLOAD_SIZE , 0 ) );
					information.setTotalSize( intent.getIntExtra( UtilsBase.EXTRA_TOTAL_SIZE , 0 ) );
					updateProgressSize();
				}
			}
			else if( Intent.ACTION_PACKAGE_REMOVED.equals( actionName ) )
			{
				String actionPkgName = intent.getData().getSchemeSpecificPart();
				if( actionPkgName.equals( packageName ) )
				{
					finish();
				}
			}
			else if( Intent.ACTION_PACKAGE_ADDED.equals( actionName ) )
			{
				String actionPkgName = intent.getData().getSchemeSpecificPart();
				if( actionPkgName.equals( packageName ) )
				{
					destClassName = queryClassName( packageName );
					loadOperateInformation( true );
					updateShowInfo();
					updateShowStatus();
				}
			}
		}
	};
	private boolean drawScroll = true;
	
	private void reLayoutScroll()
	{
		previewScroll.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout()
			{
				final int pictureHeight = findViewById( R.id.preview_picture ).getLayoutParams().height;
				Log.d( "PreviewHotActivity" , "reLayoutScroll,pictureH=" + pictureHeight + ",scrollH=" + previewScroll.getHeight() );
				findViewById( R.id.preview_picture ).getLayoutParams().height = previewScroll.getHeight();
				if( pictureHeight == previewScroll.getHeight() )
				{
					drawScroll = true;
					previewScroll.getViewTreeObserver().removeGlobalOnLayoutListener( this );
				}
				else
				{
					drawScroll = false;
				}
			}
		} );
		previewScroll.getViewTreeObserver().addOnPreDrawListener( new OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw()
			{
				if( drawScroll )
				{
					previewScroll.getViewTreeObserver().removeOnPreDrawListener( this );
				}
				return drawScroll;
			}
		} );
	}
	
	private void smoothScrollMore()
	{
		if( previewScroll.getScrollY() != 0 )
		{
			previewScroll.smoothScrollTo( 0 , 0 );
		}
		else
		{
			previewScroll.smoothScrollTo( 0 , previewScroll.getMaxScrollAmount() );
		}
	}
	
	private void removePackage()
	{
		String delApkPackname = "package:" + packageName;
		Uri packageURI = Uri.parse( delApkPackname );
		Intent uninstallIntent = new Intent( Intent.ACTION_DELETE , packageURI );
		startActivity( uninstallIntent );
	}
	
	//	class PaymentResultReceiver implements CooeePaymentResultNotify
	//	{
	//		
	//		public void paymentResult(
	//				int resultCode ,
	//				CooeePaymentInfo paymentInfo )
	//		{
	//			switch( resultCode )
	//			{
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_SUCCESS:
	//					Toast.makeText( mContext , "计费成功" , Toast.LENGTH_SHORT ).show();
	//					Tools.writePurchasedData( mContext , DownloadList.Operate_Type , packageName );
	//					// Message message = iapHandler.obtainMessage(BILLING_FINISH);
	//					// message.sendToTarget();
	//					Intent intent = new Intent( UtilsBase.ACTION_DOWNLOAD_STATUS_CHANGED );
	//					intent.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , packageName );
	//					mContext.sendBroadcast( intent );
	//					break;
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_FAIL:
	//					Toast.makeText( mContext , "计费失败" + paymentInfo.getVersionName() , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_CANCEL_BY_USER:
	//					Toast.makeText( mContext , "用户取消付费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 1:
	//					Toast.makeText( mContext , "配置免费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 2:
	//					Toast.makeText( mContext , "不需要重复计费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 3:
	//					Toast.makeText( mContext , "无可用指令" , Toast.LENGTH_SHORT ).show();
	//					break;
	//			}
	//		}
	//	}
	private class PreviewAdapter extends BaseAdapter
	{
		
		// 定义Content
		private Context mContext;
		private DownModule downModule;
		private String packageName;
		private List<Bitmap> previewImages = new ArrayList<Bitmap>();
		private boolean needDownImage = false;
		private final int VIEW_TYPE_IMAGE = 0;
		private final int VIEW_TYPE_DOWNLOADING = 1;
		private final int VIEW_TYPE_COUNT = 2;
		private Bitmap imgThumb;
		
		// 构造
		public PreviewAdapter(
				Context cxt ,
				String pkgName ,
				DownModule down )
		{
			mContext = cxt;
			downModule = down;
			packageName = pkgName;
			loadImage();
		}
		
		public void onDestory()
		{
			for( Bitmap bmp : previewImages )
			{
				if( bmp != null && !bmp.isRecycled() )
				{
					bmp.recycle();
					bmp = null;
				}
			}
			if( imgThumb != null && !imgThumb.isRecycled() )
			{
				imgThumb.recycle();
				imgThumb = null;
			}
		}
		
		private void loadImage()
		{
			String[] strArray = PathTool.getPreviewLists( packageName );
			previewImages.clear();
			if( strArray != null && strArray.length != 0 )
			{
				needDownImage = false;
				for( String imgPath : strArray )
				{
					try
					{
						previewImages.add( BitmapFactory.decodeFile( imgPath ) );
					}
					catch( OutOfMemoryError e )
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				needDownImage = true;
			}
		}
		
		public void reload()
		{
			loadImage();
			needDownImage = false;
			notifyDataSetChanged();
		}
		
		// 获取图片的个数
		@Override
		public int getCount()
		{
			if( previewImages.size() == 0 )
			{
				return 1;
			}
			return previewImages.size();
		}
		
		// 获取图片在库中的位置
		@Override
		public Object getItem(
				int position )
		{
			return position;
		}
		
		// 获取图片在库中的ID
		@Override
		public long getItemId(
				int position )
		{
			return position;
		}
		
		@Override
		public int getItemViewType(
				int position )
		{
			if( previewImages.size() == 0 )
			{
				return VIEW_TYPE_DOWNLOADING;
			}
			return VIEW_TYPE_IMAGE;
		}
		
		@Override
		public int getViewTypeCount()
		{
			super.getViewTypeCount();
			return VIEW_TYPE_COUNT;
		}
		
		// 将图片取出来
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			if( previewImages.size() == 0 )
			{
				if( needDownImage )
				{
					downModule.downloadPreview( packageName , DownloadList.Operate_Type );
				}
			}
			int viewType = getItemViewType( position );
			if( viewType == VIEW_TYPE_DOWNLOADING )
			{
				if( convertView != null )
				{
					return convertView;
				}
				View retView = View.inflate( mContext , R.layout.gallery_item_downloading , null );
				ImageView imgView = (ImageView)retView.findViewById( R.id.img );
				if( imgThumb == null )
					imgThumb = BitmapFactory.decodeFile( PathTool.getThumbFile( packageName ) );
				if( imgThumb != null )
				{
					imgView.setImageBitmap( imgThumb );
				}
				return retView;
			}
			ImageView imageView = (ImageView)convertView;
			if( imageView == null )
			{
				// 要取出图片，即要定义一个ImageView来存
				imageView = new ImageView( mContext );
				imageView.setLayoutParams( new Gallery.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT ) );
				imageView.setScaleType( ImageView.ScaleType.FIT_CENTER );
			}
			// 设置显示比例类型
			imageView.setImageBitmap( previewImages.get( position ) );
			return imageView;
		}
	}
}
