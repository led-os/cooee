package com.cooee.framework.function.DynamicEntry.DLManager;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.Dialog.DynamicEntryDialogConstant;
import com.cooee.launcher.framework.R;

import cool.sdk.DynamicEntry.DynamicEntryHelper;


public class DialogHandle
{
	
	private static int notifyID = 30141229;
	private ArrayList<String> preDownList = new ArrayList<String>();
	
	protected DialogHandle()
	{
	}
	
	//判断两个时间是否处于同一周
	@SuppressWarnings( "deprecation" )
	//进入下载管理器时弹出
	public void popDLManagerDialog()
	{
		String dynamicVersion = DynamicEntryHelper.getInstance( BaseAppState.getActivityInstance() ).getListVersion();
		String curVersion = DlManager.getInstance().getSharedPreferenceHandle().getValue( DynamicEntryDialogConstant.DYNAMIC_VERSION );
		if( dynamicVersion != null && !dynamicVersion.equals( curVersion ) )
		{
			startSmartDownloadDialog( DynamicEntryDialogConstant.DIALOG_SMARTDOWNLOAD , DynamicEntryDialogConstant.DYNAMIC_VERSION );
			DlManager.getInstance().getSharedPreferenceHandle().saveValue( DynamicEntryDialogConstant.DYNAMIC_VERSION , dynamicVersion );
		}
	}
	
	//只下载一个应用到了30%左右时弹出
	public void popDLOneDialog(
			DownloadingItem dlItem )
	{
		if( !dlItem.popSale && BaseAppState.isWifiEnabled( BaseAppState.getActivityInstance() ) && dlItem.progress > 27 && dlItem.progress < 37 )
		{
			OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
			if( client != null )
			{
				boolean isShow = client.showSaleSmartDownloadDialog( dlItem );
				if( isShow )
				{
					dlItem.popSale = true;
				}
			}
		}
	}
	
	//下载时，把下载的包名存到文件中
	public void saveDownloadAppTime(
			String pkgName )
	{
		SharedPreferences preferences = OperateDynamicProxy.context.getSharedPreferences( "DynamicEntry" , Context.MODE_PRIVATE );
		String content = preferences.getString( "dl_Time_info" , null );
		try
		{
			JSONObject newItem = new JSONObject();
			newItem.put( "pkgName" , pkgName );
			newItem.put( "time" , System.currentTimeMillis() );
			JSONObject res = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			if( content != null )
			{
				JSONObject array = new JSONObject( content );
				JSONArray list = array.getJSONArray( "dl_time" );
				for( int i = list.length() - 1 ; i > -1 ; i-- )
				{
					JSONObject item = list.getJSONObject( i );
					String pName = item.getString( "pkgName" );
					if( OperateDynamicUtils.checkApkExist( OperateDynamicProxy.context , pName ) )
					{
						continue;
					}
					if( jsonArray.length() < 6 )
					{
						jsonArray.put( item );
					}
					else
					{
						break;
					}
				}
				jsonArray.put( newItem );
				res.put( "dl_time" , jsonArray );
			}
			else
			{
				jsonArray.put( newItem );
				res.put( "dl_time" , jsonArray );
			}
			preferences.edit().putString( "dl_Time_info" , res.toString() ).commit();
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	//点击状态栏的处理
	public void doMsgDLinstall(
			Context context ,
			Bundle bundle )
	{
		int index = bundle.getInt( "index" , -1 );
		ArrayList<String> nameList = bundle.getStringArrayList( "nameList" );
		if( index != 3 )
		{
			String filePath = DlManager.getInstance().getDownloadHandle().getDownSuccessFilePath( nameList.get( 0 ) );
			if( filePath == null )
			{
				//Fixed Me 出现这样的情况应该如何处理呢？目前是直接返回
				return;
			}
			if( !OperateDynamicUtils.checkApkExist( context , nameList.get( 0 ) ) )
			{
				OperateDynamicUtils.installAPKFile( context , filePath );
			}
		}
		else
		{
			Intent intentTemp = new Intent();
			intentTemp.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
			intentTemp.putExtra( Constants.SHOW_WHICH_VIEW , Constants.SHOW_INSTALL_VIEW );
			intentTemp.putExtra( "moudleName" , "DAPP" );
			intentTemp.setClassName( context , Constants.DLLIST_ACTIVITY_CLASS_NAME );
			BaseAppState.getActivityInstance().startActivity( intentTemp );
		}
	}
	
	//点击正在下载的状态栏，进入下载管理器
	public void doMsgAllDLing(
			Context context ,
			Bundle bundle )
	{
		Intent intentTemp = new Intent();
		intentTemp.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
		intentTemp.putExtra( "moudleName" , "DAPP" );
		intentTemp.setClassName( context , Constants.DLLIST_ACTIVITY_CLASS_NAME );
		BaseAppState.getActivityInstance().startActivity( intentTemp );
	}
	
	public boolean isListEqual(
			ArrayList<String> left ,
			ArrayList<String> right )
	{
		if( left.size() != right.size() )
		{
			return false;
		}
		else
		{
			for( int i = 0 ; i < left.size() ; i++ )
			{
				if( !left.get( i ).equals( right.get( i ) ) )
				{
					return false;
				}
			}
			return true;
		}
	}
	
	//改变正在下载的状态栏
	public void doStatusDownloading(
			DownloadingItem dlItem )
	{
		ArrayList<String> nameList = new ArrayList<String>();
		DlNotifyManager mDlNotifyManager = DlManager.getInstance().getDlNotifyManager();
		if( mDlNotifyManager != null )
		{
			Iterator<Entry<String , DownloadingItem>> iter = mDlNotifyManager.getDownLoadingListIter();
			if( iter != null )
			{
				while( iter.hasNext() )
				{
					Map.Entry<String , DownloadingItem> entry = (Map.Entry<String , DownloadingItem>)iter.next();
					DownloadingItem item = entry.getValue();
					if( item != null && item.state == Constants.DL_STATUS_ING && item.isWifiReDownload == false )
					{
						nameList.add( item.packageName );
					}
				}
			}
		}
		Context context = OperateDynamicProxy.context;
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		if( nameList.size() > 0 )
		{
			if( nameList.contains( dlItem.packageName ) )
			{
				notificationManager.cancel( dlItem.notifyID );
			}
			if( !isListEqual( preDownList , nameList ) )//这样减少刷新的频率
			{
				String title = BaseDefaultConfig.getString( R.string.notify_downloading );
				title += nameList.size();
				title += BaseDefaultConfig.getString( R.string.dynamic_application );
				// gaominghui@2016/12/14 ADD START 兼容android4.0
				Notification notification;
				Builder notificationBuilder = new Notification.Builder( context ).setSmallIcon( OperateDynamicProxy.getInstance().getLauncherIcon() ).setTicker( title );
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
				{
					notification = notificationBuilder.build();
				}
				else
				{
					notification = notificationBuilder.getNotification();
				}
				// gaominghui@2016/12/14 ADD END 兼容android4.0
				notification.flags = Notification.FLAG_ONGOING_EVENT;
				notification.flags |= Notification.FLAG_NO_CLEAR;
				RemoteViews contentView = new RemoteViews( BaseAppState.getActivityInstance().getPackageName() , R.layout.operate_sa_download_notifaction );
				contentView.setTextViewText( R.id.notificationTitle , title );
				contentView.setImageViewResource( R.id.notificationImage , OperateDynamicProxy.getInstance().getLauncherIcon() );
				contentView.setViewVisibility( R.id.process , View.GONE );
				contentView.setViewVisibility( R.id.btn_check , View.GONE );
				contentView.setViewVisibility( R.id.btn_run , View.GONE );
				if( nameList.size() < 2 )
				{
					contentView.setViewVisibility( R.id.icon_1 , View.VISIBLE );
					contentView.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 0 ) ) );
					contentView.setViewVisibility( R.id.icon_2 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.icon_3 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.icon_4 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.icon_5 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.text_icon , View.INVISIBLE );
				}
				else if( nameList.size() < 3 )
				{
					contentView.setViewVisibility( R.id.icon_1 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_2 , View.VISIBLE );
					contentView.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 0 ) ) );
					contentView.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					contentView.setViewVisibility( R.id.icon_3 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.icon_4 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.icon_5 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.text_icon , View.INVISIBLE );
				}
				else if( nameList.size() < 4 )
				{
					contentView.setViewVisibility( R.id.icon_1 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_2 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_3 , View.VISIBLE );
					contentView.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 0 ) ) );
					contentView.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					contentView.setImageViewBitmap( R.id.icon_3 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 2 ) ) );
					contentView.setViewVisibility( R.id.icon_4 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.icon_5 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.text_icon , View.INVISIBLE );
				}
				else if( nameList.size() < 5 )
				{
					contentView.setViewVisibility( R.id.icon_1 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_2 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_3 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_4 , View.VISIBLE );
					contentView.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 0 ) ) );
					contentView.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					contentView.setImageViewBitmap( R.id.icon_3 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 2 ) ) );
					contentView.setImageViewBitmap( R.id.icon_4 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 3 ) ) );
					contentView.setViewVisibility( R.id.icon_5 , View.INVISIBLE );
					contentView.setViewVisibility( R.id.text_icon , View.INVISIBLE );
				}
				else
				{
					contentView.setViewVisibility( R.id.icon_1 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_2 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_3 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_4 , View.VISIBLE );
					contentView.setViewVisibility( R.id.icon_5 , View.VISIBLE );
					contentView.setImageViewBitmap( R.id.icon_1 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 0 ) ) );
					contentView.setImageViewBitmap( R.id.icon_2 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 1 ) ) );
					contentView.setImageViewBitmap( R.id.icon_3 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 2 ) ) );
					contentView.setImageViewBitmap( R.id.icon_4 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 3 ) ) );
					contentView.setImageViewBitmap( R.id.icon_5 , DlManager.getInstance().getDownloadHandle().getDownBitmap( nameList.get( 4 ) ) );
					if( nameList.size() < 6 )
					{
						contentView.setViewVisibility( R.id.text_icon , View.INVISIBLE );
					}
					else
					{
						contentView.setViewVisibility( R.id.text_icon , View.VISIBLE );
					}
				}
				notification.contentView = contentView;
				Intent intentTem = new Intent();
				intentTem.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
				Bundle bundle = new Bundle();
				bundle.putString( Constants.MSG , Constants.MSG_ALL_DLING );
				intentTem.putExtras( bundle );
				PendingIntent contentIntent = PendingIntent.getActivity( context , notifyID , intentTem , PendingIntent.FLAG_UPDATE_CURRENT );
				notification.contentIntent = contentIntent;
				//notificationManager.cancel( notifyID );
				notificationManager.notify( notifyID , notification );
				preDownList.clear();
				preDownList.addAll( nameList );
			}
		}
		else
		{
			notificationManager.cancel( notifyID );
			preDownList.clear();
		}
	}
	
	public static int getDialogNotifyId()
	{
		return notifyID;
	}
	
	public void startSmartDownloadDialog(
			int dialogid ,
			String info )
	{
		Intent smartIntent = new Intent( "com.cooee.DynamicEntry.Dialog.Disclaimer" );
		Bundle mBundle = new Bundle();
		mBundle.putInt( DynamicEntryDialogConstant.DIALOG_ID , dialogid );
		mBundle.putString( DynamicEntryDialogConstant.SMART_INFO , info );
		smartIntent.putExtras( mBundle );
		BaseAppState.getActivityInstance().startActivity( smartIntent );
	}
}
