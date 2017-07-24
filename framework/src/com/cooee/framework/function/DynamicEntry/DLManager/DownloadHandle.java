package com.cooee.framework.function.DynamicEntry.DLManager;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;
import com.iLoong.launcher.MList.MainActivity;
import com.iLoong.launcher.MList.MeApkDlMgrBuilder;
import com.iLoong.launcher.MList.MeApkDownloadManager;

import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.SAManager.SAHelper;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class DownloadHandle
{
	
	//public Context mContext;
	private static final String TAG = "DownloadHandle";
	private static final String PKGNAME_ID = "pkgName";
	private static final String OPERATE_WEBLINKPKG = "weblinkpkg";
	
	DownloadHandle()
	{
	}
	
	public boolean clearUninstallNotification(
			String pkgName )
	{
		return DlManager.getInstance().clearUnistallApkNotification( pkgName );
	}
	
	private Bitmap drawableToBitmap(
			Drawable drawable )
	{
		Bitmap bitmap = Bitmap.createBitmap(
				drawable.getIntrinsicWidth() ,
				drawable.getIntrinsicHeight() ,
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565 );
		Canvas canvas = new Canvas( bitmap );
		drawable.setBounds( 0 , 0 , drawable.getIntrinsicWidth() , drawable.getIntrinsicHeight() );
		drawable.draw( canvas );
		return bitmap;
	}
	
	//根据包名判断此apk是否装在手机上
	public ApplicationInfo getApplicationInfoByPkgName(
			String packageName )
	{
		if( packageName == null || "".equals( packageName ) )
			return null;
		try
		{
			ApplicationInfo info = BaseAppState.getActivityInstance().getPackageManager().getApplicationInfo( packageName , PackageManager.GET_UNINSTALLED_PACKAGES );
			if( info != null )
			{
				return info;
			}
		}
		catch( NameNotFoundException e )
		{
			return null;
		}
		return null;
	}
	
	//根据包名获取应用名称
	public String getAppInfoTitleByPkgName(
			String pkgName )
	{
		PackageManager pm = BaseAppState.getActivityInstance().getPackageManager();
		// 得到系统 安装的所有程序包的PackageInfo对象
		List<PackageInfo> packs = pm.getInstalledPackages( 0 );
		for( PackageInfo info : packs )
		{
			//System.out.println( info.applicationInfo.loadLabel( pm ).toString() + "==>" + info.packageName );
			if( info.packageName.equals( pkgName ) )
			{
				return info.applicationInfo.loadLabel( pm ).toString();
			}
		}
		return null;
	}
	
	//根据包名获取应用icon
	public Drawable getAppInfoIconByPkgName(
			String pkgName )
	{
		PackageManager pm = BaseAppState.getActivityInstance().getPackageManager();
		// 得到系统 安装的所有程序包的PackageInfo对象
		List<PackageInfo> packs = pm.getInstalledPackages( 0 );
		for( PackageInfo info : packs )
		{
			if( info.packageName.equals( pkgName ) )
			{
				return info.applicationInfo.loadIcon( pm );
			}
		}
		return null;
	}
	
	//从一键下载过来的。不让他showDialog
	private CoolDLMgr getDlMgr()
	{
		return DlManager.getInstance().getDlMgr();
	}
	
	public String getDownSuccessFilePath(
			String pkgName )
	{
		dl_info info = getDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
		if( info == null || !info.IsDownloadSuccess() || info.getFilePath().equals( "" ) )
		{
			CoolDLMgr wifidlMgr = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getCoolDLMgrApk();
			info = wifidlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
			if( info == null || !info.IsDownloadSuccess() || info.getFilePath().equals( "" ) )
			{
				//Fixed Me 出现这样的情况应该如何处理呢？目前是直接返回
				return null;
			}
		}
		return info.getFilePath();
	}
	
	//获取用户设置的 默认浏览器 
	private ResolveInfo getUserDefinitionBrowers(
			PackageManager packageMgr )
	{
		Intent intent = ( new Intent( Intent.ACTION_VIEW , Uri.parse( "http://" ) ) );
		intent.addCategory( Intent.CATEGORY_BROWSABLE );
		ResolveInfo bestMatch = packageMgr.resolveActivity( intent , PackageManager.MATCH_DEFAULT_ONLY );
		List<ResolveInfo> allMatches = getAllInstallBrowser( packageMgr );
		boolean found = false;
		for( ResolveInfo ri : allMatches )
		{
			if( bestMatch.activityInfo.name.equals( ri.activityInfo.name ) && bestMatch.activityInfo.applicationInfo.packageName.equals( ri.activityInfo.applicationInfo.packageName ) )
			{
				found = true;
				break;
			}
		}
		if( !found )
		{
			return null;
		}
		return bestMatch;
	}
	
	public boolean enterAppORMarket(
			Context context ,
			String weblink ,
			String uri )
	{
		if( ApkIsInstall( weblink ) )
		{
			return true;
		}
		else
		{
			if( isGoogleLink( uri ) && showMarket( context , uri ) )
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean ApkIsInstall(
			String weblink )
	{
		if( ( weblink != null ) && ( OperateDynamicUtils.checkApkExist( BaseAppState.getActivityInstance() , weblink ) ) )
		{
			DlApkMangerActivity.StartActivityByPackageName( BaseAppState.getActivityInstance() , weblink );
			return true;
		}
		return false;
	}
	
	public boolean isGoogleLink(
			String paramString )
	{
		return ( paramString.indexOf( "https://play.google" ) >= 0 ) || ( paramString.indexOf( "http://play.google" ) >= 0 );
	}
	
	public boolean showMarket(
			Context paramContext ,
			String url )
	{
		try
		{
			Intent launchIntent = paramContext.getPackageManager().getLaunchIntentForPackage( "com.android.vending" );
			if( launchIntent == null )
			{
				return false;
			}
			ComponentName comp = new ComponentName( "com.android.vending" , "com.google.android.finsky.activities.LaunchUrlHandlerActivity" ); // package name and activity
			launchIntent.setComponent( comp );
			launchIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			launchIntent.setData( Uri.parse( url ) );
			paramContext.startActivity( launchIntent );
			return true;
		}
		catch( android.content.ActivityNotFoundException anfe )
		{
			//paramContext.startActivity( new Intent( Intent.ACTION_VIEW , Uri.parse( pkgName ) ) );
		}
		return false;
	}
	
	private boolean StartEntryMEDownUI(
			Context context ,
			Intent intent )
	{
		String pkgNameString = intent.getStringExtra( PKGNAME_ID );
		if( getPkgNameCurrentState( pkgNameString ) != Constants.DL_STATUS_NOTDOWN )//微入口类型的apk，说明被手动下载了
		{
			return false;
		}
		if( AppDownloadTypeHandle.isMeEntryVirtualApp( intent ) )
		{
			if( isOperateUninstallItem( intent ) )
			{
				if( pkgNameString != null && getMeApkDownloadPath( pkgNameString ) == null )
				{
					MeStartEntery( context , pkgNameString );
					return true;
				}
			}
		}
		return false;
	}
	
	public void DownloadApkBegin(
			Context context ,
			Intent intent ,
			String title ,
			Bitmap bitmap )
	{
		int itemType = intent.getIntExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , 1 );
		boolean showAppDownloadDialog = false;
		switch( itemType )
		{
			case OperateDynamicUtils.VIRTUAL_LINK:
			{
				String urlLink = intent.getData().toString();
				if( urlLink == null )
				{
					urlLink = "http://www.baidu.com";
				}
				String pkgNameString = intent.getStringExtra( OPERATE_WEBLINKPKG );
				StatisticsHandle.DynamicEntryClick( title , intent );
				if( pkgNameString != null )
				{
					if( ApkIsInstall( pkgNameString ) == false )
					{
						if( isGoogleLink( urlLink ) && showMarket( context , urlLink ) )
						{
						}
						else
						{
							dealUrlBrowser( context , urlLink );
						}
					}
				}
				else
				{
					dealUrlBrowser( context , urlLink );
				}
			}
				break;
			case OperateDynamicUtils.VIRTUAL_APP:
				//String pkgName = shortcutInfo.intent.getAction();
				//DynamicEntry gzh1011 start
				showAppDownloadDialog = true;
				if( !BaseDefaultConfig.SWITCH_ENABLE_DOWNLOAD_CONFIRM_DIALOG )
				{
					showAppDownloadDialog = false;
				}
				//DynamicEntry gzh1011 end
				// DynamicEntry 增加微入口下载显示界面 begin
				if( StartEntryMEDownUI( context , intent ) == true )
				{
					return;
				}
				// DynamicEnrty 增加微入口下载显示界面 ended
				if( isOperateUninstallItem( intent ) )
				{
					StatisticsHandle.DynamicEntryClick( title , intent );
					dealWhichTypeDialog( context , intent , title , bitmap , showAppDownloadDialog );
				}
				break;
		}
	}
	
	public void dealWhichTypeDialog(
			Context context ,
			Intent intent ,
			String title ,
			Bitmap bitmap ,
			boolean showDialog )
	{
		String pkgName = intent.getStringExtra( PKGNAME_ID );
		int state = getPkgNameCurrentState( pkgName );
		try
		{
			switch( state )
			{
				case Constants.DL_STATUS_ING:
					Toast.makeText( context , BaseDefaultConfig.getString( R.string.download_app_ing ) , 0 ).show();
					break;
				case Constants.DL_STATUS_PAUSE:
				case Constants.DL_STATUS_FAIL:
					DlManager.getInstance().getSharedPreferenceHandle()
							.saveValue( StringUtils.concat( SharedPreferenceHandle.DOWNLOAD_APK_PREFIX , pkgName ) , String.valueOf( SharedPreferenceHandle.APK_DOWNLOAD ) );
					Toast.makeText( context , BaseDefaultConfig.getString( R.string.download_app_continue ) , 0 ).show();
					downloadAndInstallApp( intent , title );
					break;
				case Constants.DL_STATUS_NOTDOWN:
					if( AppDownloadTypeHandle.isMeEntryVirtualApp( intent ) )
					{
						String meApkPath = getMeApkDownloadPath( pkgName );
						if( meApkPath != null )
						{
							if( !OperateDynamicUtils.checkApkExist( BaseAppState.getActivityInstance() , pkgName ) )
							{
								OperateDynamicUtils.installAPKFile( BaseAppState.getActivityInstance() , meApkPath );
							}
							state = Constants.DL_STATUS_SUCCESS;
							return;
						}
					}
					DlManager.getInstance().getSharedPreferenceHandle()
							.saveValue( StringUtils.concat( SharedPreferenceHandle.DOWNLOAD_APK_PREFIX , pkgName ) , String.valueOf( SharedPreferenceHandle.APK_DOWNLOAD ) );
					if( showDialog )
					{
						if( AppDownloadTypeHandle.startAppStoreDownload( intent , pkgName ) )
						{
						}
						else
						{
							Intent it = new Intent( intent );
							it.putExtra( "title" , title );
							it.putExtra( "bitmap" , bitmap );
							BaseAppState.getActivityInstance().startActivity( it );
						}
					}
					else
					{
						downloadAndInstallApp( intent , title );
					}
					break;
				case Constants.DL_STATUS_SUCCESS:
					String filePath = getDownSuccessFilePath( pkgName );
					if( filePath == null )
					{
						return;
					}
					if( !OperateDynamicUtils.checkApkExist( BaseAppState.getActivityInstance() , pkgName ) )
					{
						OperateDynamicUtils.installAPKFile( BaseAppState.getActivityInstance() , filePath );
					}
					break;
			}
		}
		finally
		{
			int intent_state = intent.getIntExtra( "showOperateIconDownOrInstall" , Constants.DL_STATUS_NOTDOWN );
			if( state != intent_state )
			{
				OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
				client.upateDownloadItemState( pkgName , state );
			}
		}
	}
	
	private List<ResolveInfo> getAllInstallBrowser(
			PackageManager packageMgr )
	{
		Intent intent = ( new Intent( Intent.ACTION_VIEW , Uri.parse( "http://" ) ) );
		intent.addCategory( Intent.CATEGORY_BROWSABLE );
		List<ResolveInfo> allMatches = packageMgr.queryIntentActivities( intent , PackageManager.MATCH_DEFAULT_ONLY );
		return allMatches;
	}
	
	public void dealUrlBrowser(
			Context context ,
			String url )
	{
		PackageManager packageMgr = context.getPackageManager();
		ResolveInfo info = getUserDefinitionBrowers( packageMgr );
		if( info != null )
		{
			gotoUrl( context , info.activityInfo.packageName , url , packageMgr );
		}
		else
		{
			choiceBrowserToVisitUrl( context , url , packageMgr );
		}
	}
	
	private void choiceBrowserToVisitUrl(
			Context context ,
			String url ,
			PackageManager packageMgr )
	{
		boolean existUC = false , existOpera = false , existQQ = false , existBaidu = false , existFirebox = false , existGoogle = false;
		String ucPath = "" , operaPath = "" , qqPath = "" , fireboxPath = "" , baiduPath = "" , googlePath = "";
		//List<PackageInfo> list = packageMgr.getInstalledPackages( 0 );
		List<ResolveInfo> list = getAllInstallBrowser( packageMgr );
		for( int i = 0 ; i < list.size() ; i++ )
		{
			ResolveInfo info = list.get( i );
			String temp = info.activityInfo.applicationInfo.packageName;
			if( temp.equals( "com.tencent.mtt" ) )
			{
				// 存在QQ
				qqPath = temp;
				existQQ = true;
			}
			else if( temp.equals( "com.UCMobile" ) )//com.uc.browser
			{
				// 存在UC
				ucPath = temp;
				existUC = true;
			}
			else if( temp.equals( "com.baidu.browser.apps" ) )//com.baidu.browser.cloud.launcher.RECEIVE
			{
				baiduPath = temp;
				existBaidu = true;
			}
			else if( temp.equals( "com.opera.browser.classic" ) || temp.equals( "com.oupeng.mini.android" ) )//com.opera.mini.android
			{
				// 存在Opera
				operaPath = temp;
				existOpera = true;
			}
			else if( temp.equals( "org.mozilla.firefox_beta" ) || temp.equals( "org.mozilla.firefox" ) )
			{
				fireboxPath = temp;
				existFirebox = true;
			}
			else if( temp.equals( "com.android.chrome" ) )
			{
				// 存在GoogleBroser
				googlePath = temp;
				existGoogle = true;
			}
		}
		if( existQQ )
		{
			gotoUrl( context , qqPath , url , packageMgr );
		}
		else if( existUC )
		{
			gotoUrl( context , ucPath , url , packageMgr );
		}
		else if( existBaidu )
		{
			gotoUrl( context , baiduPath , url , packageMgr );
		}
		else if( existOpera )
		{
			gotoUrl( context , operaPath , url , packageMgr );
		}
		else if( existFirebox )
		{
			gotoUrl( context , fireboxPath , url , packageMgr );
		}
		else if( existGoogle )
		{
			gotoUrl( context , googlePath , url , packageMgr );
		}
		else
		{
			doDefaultBrowser( context , url );
		}
	}
	
	private void doDefaultBrowser(
			Context context ,
			String visitUrl )
	{
		try
		{
			Intent intent = new Intent( Intent.ACTION_VIEW , Uri.parse( visitUrl ) );
			context.startActivity( intent );
		}
		catch( Exception e )
		{
			// 在1.5及以前版本会要求catch(android.content.pm.PackageManager.NameNotFoundException)异常，该异常在1.5以后版本已取消。
			e.printStackTrace();
		}
	}
	
	private void gotoUrl(
			Context context ,
			String packageName ,
			String url ,
			PackageManager packageMgr )
	{
		try
		{
			Intent intent = packageMgr.getLaunchIntentForPackage( packageName );
			intent.setAction( Intent.ACTION_VIEW );
			intent.addCategory( Intent.CATEGORY_DEFAULT );
			intent.setData( Uri.parse( url ) );
			context.startActivity( intent );
		}
		catch( Exception e )
		{
			// 在1.5及以前版本会要求catch(android.content.pm.PackageManager.NameNotFoundException)异常，该异常在1.5以后版本已取消。
			e.printStackTrace();
		}
	}
	
	public void onResume()
	{
		DlManager.getInstance().onResume();
	}
	
	public float getPkgNameCurrentProgress(
			String pkgName )
	{
		return DlManager.getInstance().getPkgNameCurrentProgress( pkgName );
	}
	
	public int getPkgNameCurrentState(
			String pkgName )
	{
		return DlManager.getInstance().getPkgNameCurrentState( pkgName );
	}
	
	public void pauseCurrentDownload(
			final String pkgName )
	{
		DlManager.getInstance().pauseAppDownload( pkgName );
	}
	
	public Bitmap getDownBitmap(
			String pkgName )
	{
		dl_info info = DynamicEntryHelper.getInstance( BaseAppState.getActivityInstance() ).getCoolDLMgrIcon().IconGetInfo( pkgName );
		if( info != null )
		{
			String ImgPath = info.getFilePath();
			if( null != ImgPath && ImgPath.length() > 1 )
			{
				try
				{
					return BitmapFactory.decodeFile( ImgPath );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		Drawable drawable = getPackageIcon( pkgName );
		if( drawable == null )
		{
			return null;
		}
		return drawableToBitmap( drawable );
	}
	
	public Drawable getDownSuccessIcon(
			String pkgName )
	{
		Drawable retDrawable = null;
		dl_info info = DynamicEntryHelper.getInstance( BaseAppState.getActivityInstance() ).getCoolDLMgrIcon().IconGetInfo( pkgName );
		if( info != null )
		{
			String ImgPath = info.getFilePath();
			if( null != ImgPath && ImgPath.length() > 1 )
			{
				retDrawable = Drawable.createFromPath( ImgPath );
			}
			else
			{
				retDrawable = getPackageIcon( pkgName );
			}
		}
		else
		{
			retDrawable = getPackageIcon( pkgName );
		}
		return retDrawable;
	}
	
	/*
	 * 根据已安装包名获取图片，根据未安装路径获取图片
	 * return drawable
	 */
	private Drawable getPackageIcon(
			String pkgName )
	{
		PackageManager pm = BaseAppState.getActivityInstance().getPackageManager();
		PackageInfo pakinfo = pm.getPackageArchiveInfo( StringUtils.concat( OperateDynamicUtils.getDownloadFilePath() , "/" , pkgName , ".apk" ) , PackageManager.GET_ACTIVITIES );
		if( pakinfo != null )
		{
			ApplicationInfo appinfo = pakinfo.applicationInfo;
			return pm.getApplicationIcon( appinfo );
		}
		//静默下载的应用图片得不到的话从这边获取
		Drawable sAIcon = getSADownloadIcon( pkgName );
		if( sAIcon != null )
		{
			return sAIcon;
		}
		// 得到系统 安装的所有程序包的PackageInfo对
		List<ResolveInfo> packs = findActivitiesForPackage( BaseAppState.getActivityInstance() , pkgName );
		if( packs.size() > 0 )
		{
			return packs.get( 0 ).activityInfo.loadIcon( pm );
		}
		String dstFileName = StringUtils.concat( DlApkMangerActivity.ICON_DEFAULT_FOLDER , pkgName , ".png" );
		//			DefaultLayout.ICON_DEFAULT_FOLDER;
		try
		{
			//				item.mIconBitmap = BitmapFactory.decodeStream( BaseAppState.getActivityInstance().getAssets().open( dstFileName ) );
			BitmapDrawable bd = new BitmapDrawable( BaseAppState.getActivityInstance().getResources() , BitmapFactory.decodeStream( BaseAppState.getActivityInstance().getAssets().open( dstFileName ) ) );
			return bd;
		}
		catch( IOException ex )
		{
			return BaseAppState.getActivityInstance().getResources().getDrawable( R.drawable.cool_ml_download_install );
		}
	}
	
	private List<ResolveInfo> findActivitiesForPackage(
			Context context ,
			String packageName )
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		mainIntent.setPackage( packageName );
		final List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
		return apps != null ? apps : new ArrayList<ResolveInfo>();
	}
	
	//通知安装完成的消息
	public boolean showInStallSuccessNotification(
			String pkgName )
	{
		DownloadingItem dlItem = DlManager.getInstance().getDownloadingItem( pkgName );
		if( dlItem == null )
		{
			return false;
		}
		int notifyID = dlItem.notifyID;
		DlManager.getInstance().removeFromDownloadList( pkgName );
		final PackageManager packageManager = BaseAppState.getActivityInstance().getPackageManager();
		final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		mainIntent.setPackage( pkgName );
		final List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
		NotificationManager mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( Context.NOTIFICATION_SERVICE );
		if( apps == null || apps.size() == 0 )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "showInStallSuccessNotification apps.size() == 0 return false pkg=" , pkgName ) );
			mNotificationManager.cancel( notifyID );
			return false;
		}
		ResolveInfo ri = apps.get( 0 );
		ComponentName componentName = new ComponentName( ri.activityInfo.applicationInfo.packageName , ri.activityInfo.name );
		final String title = ri.loadLabel( packageManager ).toString();
		Drawable infoIcon = ri.loadIcon( packageManager );
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		Notification notification;
		Builder notificationBuilder = new Notification.Builder( BaseAppState.getActivityInstance() ).setSmallIcon( R.drawable.download ).setTicker( title );
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
		{
			notification = notificationBuilder.build();
		}
		else
		{
			notification = notificationBuilder.getNotification();
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		notification.flags = 0;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_LAUNCHER );
		intent.setComponent( componentName );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		PendingIntent contentIntent = PendingIntent.getActivity( BaseAppState.getActivityInstance() , 0 , intent , 0 );
		notification.contentIntent = contentIntent;
		RemoteViews contentView = new RemoteViews( BaseAppState.getActivityInstance().getPackageName() , R.layout.dynamicentry_folder_notification );
		contentView.setImageViewBitmap( R.id.notificationImage , getDownBitmap( pkgName ) );
		String titleString = null;
		if( OperateDynamicUtils.getCurLanguage() == 0 )
		{
			titleString = StringUtils.concat( title , " " , BaseDefaultConfig.getString( R.string.dialog_install_success ) );
		}
		else
		{
			titleString = StringUtils.concat( title , BaseDefaultConfig.getString( R.string.dialog_install_success ) );
		}
		contentView.setTextViewText( R.id.notificationTitle , titleString );
		contentView.setViewVisibility( R.id.notificationPercent , View.INVISIBLE );
		contentView.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
		final String tryit = BaseDefaultConfig.getString( R.string.dialog_click_start );
		contentView.setTextViewText( R.id.notificationContent , tryit );
		notification.contentView = contentView;
		mNotificationManager.cancel( notifyID );
		mNotificationManager.notify( notifyID , notification );
		return true;
	}
	
	public String getMeApkDownloadPath(
			String pkgName )
	{
		if( pkgName != null )
		{
			MeApkDownloadManager MeApkDlMgr = MeApkDlMgrBuilder.Build( BaseAppState.getActivityInstance() , "M" , 1 );
			dl_info curDLInfo = MeApkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
			if( curDLInfo != null && curDLInfo.IsDownloadSuccess() )
			{
				return curDLInfo.getFilePath();
			}
		}
		return null;
	}
	
	public synchronized void downloadAndInstallApp(
			Intent intent ,
			final String title )
	{
		final String pkgName = intent.getStringExtra( PKGNAME_ID );
		//dynamicEntry1010 start
		if( AppDownloadTypeHandle.startAppStoreDownload( intent , pkgName ) )
		{
			return;
		}
		//dynamicEntry1010 end
		if( !OperateDynamicUtils.isNetworkAvailable( BaseAppState.getActivityInstance() ) )
		{
			Toast.makeText( BaseAppState.getActivityInstance() , R.string.dynamic_network_error , Toast.LENGTH_SHORT ).show();
		}
		Runnable runnable = new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				DlManager.getInstance().downloadFile( BaseAppState.getActivityInstance() , title , pkgName , false );
			}
		};
		( (Activity)BaseAppState.getActivityInstance() ).runOnUiThread( runnable );
	}
	
	private Drawable getSADownloadIcon(
			String pkgName )
	{
		CoolDLMgr wifidlMgr = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getCoolDLMgrApk();
		if( pkgName == null || wifidlMgr == null )
		{
			return null;
		}
		dl_info info = wifidlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
		if( info == null || info.getFilePath() == null )
		{
			return null;
		}
		PackageManager pm = BaseAppState.getActivityInstance().getPackageManager();
		PackageInfo pakinfo = pm.getPackageArchiveInfo( info.getFilePath() , PackageManager.GET_ACTIVITIES );
		if( pakinfo != null )
		{
			ApplicationInfo appInfo = pakinfo.applicationInfo;
			appInfo.sourceDir = info.getFilePath();
			appInfo.publicSourceDir = info.getFilePath();
			try
			{
				//return pm.getApplicationIcon( appinfo );//此方法会返回机器人
				return appInfo.loadIcon( pm );
			}
			catch( OutOfMemoryError e )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "ApkIconLoader" , e.toString() );
			}
		}
		return null;
	}
	
	public boolean isOperateUninstallItem(
			Intent intent )
	{
		if( intent == null )
			return false;
		if( intent.getStringExtra( PKGNAME_ID ) != null )
		{
			return true;
		}
		return false;
	}
	
	public void MeStartEntery(
			Context context ,
			String pkgName )
	{
		Intent intent = new Intent();
		intent.setClass( context , MainActivity.class );
		//		intent.putExtra( "APP_ID" , index );
		intent.putExtra( "Action" , "detailfolder_pkgname" );
		intent.putExtra( "ActionDescription" , pkgName );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity( intent );
	}
	
	public void dealSaleDownload(
			Context context ,
			String pkgNameString ,
			String title ,
			int downloadType )
	{
		if( ( downloadType & OperateDynamicUtils.ME_ENTRY_FLAG ) == OperateDynamicUtils.ME_ENTRY_FLAG )
		{
			if( pkgNameString != null /*&& getMeApkDownloadPath( pkgNameString ) == null*/)//getMeApkDownloadPath这个不为空，表示下载完成了，也是直接进入微入口界面控制
			{
				MeStartEntery( context , pkgNameString );
				return;
			}
		}
		else if( downloadType == OperateDynamicUtils.WIFI_APPSTORE_DOWNLOAD || downloadType == OperateDynamicUtils.APPSTORE_DOWNLOAD )
		{
			try
			{
				BaseAppState.getActivityInstance().startActivity(
						new Intent( Intent.ACTION_VIEW ).setData( Uri.parse( StringUtils.concat( AppDownloadTypeHandle.APPSTORE_PAKAGENAME_PREFIX , pkgNameString ) ) ) );
				return;
			}
			catch( Exception e2 )
			{
				// TODO: handle exception
			}
		}
		DlManager.getInstance().downloadFile( BaseAppState.getActivityInstance() , title , pkgNameString , false );
	}
}
