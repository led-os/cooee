package com.cooee.phenix.Functions.DynamicEntry.Dialog;


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.function.DynamicEntry.DLManager.SharedPreferenceHandle;
import com.cooee.framework.function.DynamicEntry.Dialog.DynamicEntryDialogConstant;
import com.cooee.framework.function.DynamicEntry.Dialog.DynamicEntrySmartDownloadInfo;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.DynamicGrid;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ShortcutInfo;

import cool.sdk.SAManager.SAHelper;
import cool.sdk.download.manager.dl_info;


public class DynamicEntryDialogView
{
	
	private boolean normalOpen = false;;
	private View nextDialog = null;
	private View smartDialog = null;
	
	public View getDynamicEntryView(
			Context context ,
			String smartInfo ,
			int dialog_id )
	{
		// TODO Auto-generated method stub
		switch( dialog_id )
		{
			case DynamicEntryDialogConstant.DIALOG_SMARTDOWNLOAD:
				if( smartInfo != null )
				{
					return smartDownloadDialog( context , smartInfo );
				}
				break;
			case DynamicEntryDialogConstant.DIALOG_DOWNLOADONE:
				if( smartInfo != null )
				{
					return downloadNextDialog( context , smartInfo );
				}
				break;
		}
		return null;
	}
	
	private View downloadNextDialog(
			final Context context ,
			String res )
	{
		if( nextDialog == null )
		{
			LayoutInflater inflater = LayoutInflater.from( context );
			nextDialog = inflater.inflate( R.layout.dynamic_smart_download_dialog , null );
		}
		//d_layout = this.findViewById( R.id.dialog_layout );
		nextDialog.setVisibility( View.VISIBLE );
		nextDialog.findViewById( R.id.icons ).setVisibility( View.GONE );
		nextDialog.findViewById( R.id.next_download ).setVisibility( View.VISIBLE );
		TextView title = (TextView)nextDialog.findViewById( R.id.dialog_title );
		title.setText( R.string.dynamic_other_download );
		final DynamicEntrySmartDownloadInfo sdInfo = new DynamicEntrySmartDownloadInfo();
		String last_name = processSmartItem( res , sdInfo );
		if( last_name == null )
		{
			return null;
		}
		ImageView next_icon = (ImageView)nextDialog.findViewById( R.id.next_icon );
		TextView next_name = (TextView)(TextView)nextDialog.findViewById( R.id.next_title );
		TextView next_size = (TextView)(TextView)nextDialog.findViewById( R.id.next_size );
		Bitmap bitmap = createBitmap( context , sdInfo.getPath() );
		if( bitmap == null )
		{
			return null;
		}
		next_icon.setImageBitmap( bitmap );
		//mergeImage( bitmap , null , next_icon );
		next_name.setText( sdInfo.getTitle() );
		int size = sdInfo.getSize();
		DecimalFormat decimalFormat = new DecimalFormat( "0.00" );
		String Ssize = StringUtils.concat( decimalFormat.format( (double)size / 1024 ) , "M" );
		next_size.setText( StringUtils.concat( LauncherDefaultConfig.getString( R.string.dynamic_next_size ) , Ssize ) );
		Button next_button = (Button)nextDialog.findViewById( R.id.next_button );
		next_button.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				Runnable runnable = new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						//						DlManager.getInstance().downloadFile( LauncherAppState.getActivityInstance() , sdInfo.getTitle() , sdInfo.getPkgName() , false );
						DlManager.getInstance().getDownloadHandle().dealSaleDownload( LauncherAppState.getActivityInstance() , sdInfo.getPkgName() , sdInfo.getTitle() , sdInfo.getDownloadType() );
					}
				};
				( (Activity)LauncherAppState.getActivityInstance() ).runOnUiThread( runnable );
				nextDialog.setVisibility( View.GONE );
			}
		} );
		ImageView image_cancel = (ImageView)nextDialog.findViewById( R.id.dialog_cancel );
		image_cancel.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				nextDialog.setVisibility( View.GONE );
			}
		} );
		nextDialog.setOnTouchListener( new OnTouchListener() {
			
			@Override
			public boolean onTouch(
					View v ,
					MotionEvent event )
			{
				// TODO Auto-generated method stub
				if( v.getId() == R.id.dialog_layout )
					return false;
				else
				{
					return true;
				}
			}
		} );
		return nextDialog;
	}
	
	private String processSmartItem(
			String res ,
			DynamicEntrySmartDownloadInfo info )
	{
		try
		{
			JSONObject item = new JSONObject( res );
			String last_name = item.optString( DynamicEntryDialogConstant.LAST_NAME );
			if( last_name != null && !last_name.equals( "" ) )
			{
				String pkgName = item.optString( "pkgName" );
				String title = item.optString( "title" );
				String path = item.optString( "path" );
				int size = item.optInt( "size" );
				int downloadType = item.optInt( "downloadType" );
				info.setPkgName( pkgName );
				info.setTitle( title );
				info.setPath( path );
				info.setSize( size );
				info.setDownloadType( downloadType );
				return last_name;
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private ShortcutInfo createShortcutInfo(
			Bitmap bitmap ,
			String title )
	{
		Bitmap bmp = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bitmap , LauncherAppState.getInstance().getContext() , true , false );
		Intent intent = new Intent( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER );
		intent.putExtra( OperateDynamicMain.FOLDER_VERSION , "install" );
		return OperateDynamicMain.createShortcutInfo( bmp , title , intent , 0 );
	}
	
	public Bitmap createBitmap(
			Context context ,
			String path )
	{
		int iconSize = Utilities.sIconWidth > 0 ? Utilities.sIconWidth : LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size );
		Bitmap bmp = findBitmap( context , path );
		if( bmp != null )
		{
			return Bitmap.createScaledBitmap( bmp , iconSize , iconSize , true );
		}
		return null;
	}
	
	public Bitmap findBitmap(
			Context context ,
			String path )
	{
		Bitmap bitmap = null;
		File file = new File( path );
		if( file.exists() )
		{
			bitmap = BitmapFactory.decodeFile( path );
		}
		else
		{
			try
			{
				bitmap = BitmapFactory.decodeStream( context.getAssets().open( path ) );
			}
			catch( IOException ex )
			{
				ex.printStackTrace();
			}
		}
		return bitmap;
	}
	
	private View smartDownloadDialog(
			final Context context ,
			String smartInfo )
	{
		if( smartDialog == null )
		{
			LayoutInflater inflater = LayoutInflater.from( context );
			smartDialog = inflater.inflate( R.layout.dynamic_smart_download_dialog , null );
		}
		//d_layout = smartDialog.findViewById( R.id.dialog_layout );
		//加载icon
		smartDialog.setVisibility( View.VISIBLE );
		final BubbleTextView icon_zero = (BubbleTextView)smartDialog.findViewById( R.id.icon0 );
		final BubbleTextView icon_one = (BubbleTextView)smartDialog.findViewById( R.id.icon1 );
		final BubbleTextView icon_two = (BubbleTextView)smartDialog.findViewById( R.id.icon2 );
		final BubbleTextView icon_three = (BubbleTextView)smartDialog.findViewById( R.id.icon3 );
		final ImageView dialog_cancel = (ImageView)smartDialog.findViewById( R.id.dialog_cancel );
		//设置每个icon的图片和title ybh@2014/12/22 ADD START
		final ArrayList<DynamicEntrySmartDownloadInfo> list = processSmartDownloadInfo( context , smartInfo );
		if( list.size() == 0 )
		{
			return null;
		}
		if( !normalOpen )
		{
			TextView title = (TextView)smartDialog.findViewById( R.id.dialog_title );
			title.setText( R.string.dynamic_smart_download_desc );
		}
		IconCache mIconCache = LauncherAppState.getInstance().getIconCache();
		ShortcutInfo info0 = createShortcutInfo( list.get( 0 ).getBitmap() , list.get( 0 ).getTitle() );
		if( info0 == null )
		{
			return null;
		}
		icon_zero.applyFromShortcutInfo( info0 , mIconCache );
		icon_zero.setOperateIconLoadDone( Constants.DL_STATUS_SUCCESS );
		icon_zero.setVisibility( View.VISIBLE );
		if( list.size() > 1 )
		{
			ShortcutInfo info1 = createShortcutInfo( list.get( 1 ).getBitmap() , list.get( 1 ).getTitle() );
			if( info1 == null )
			{
				return null;
			}
			icon_one.applyFromShortcutInfo( info1 , mIconCache );
			icon_one.setOperateIconLoadDone( Constants.DL_STATUS_SUCCESS );
			icon_one.setVisibility( View.VISIBLE );
		}
		else
		{
			icon_one.setVisibility( View.GONE );
			icon_two.setVisibility( View.GONE );
			icon_three.setVisibility( View.GONE );
		}
		if( list.size() > 2 )
		{
			ShortcutInfo info2 = createShortcutInfo( list.get( 2 ).getBitmap() , list.get( 2 ).getTitle() );
			if( info2 == null )
			{
				return null;
			}
			icon_two.applyFromShortcutInfo( info2 , mIconCache );
			icon_two.setOperateIconLoadDone( Constants.DL_STATUS_SUCCESS );
			icon_two.setVisibility( View.VISIBLE );
		}
		else
		{
			icon_two.setVisibility( View.GONE );
			icon_three.setVisibility( View.GONE );
		}
		if( list.size() > 3 )
		{
			ShortcutInfo info3 = createShortcutInfo( list.get( 3 ).getBitmap() , list.get( 3 ).getTitle() );
			if( info3 == null )
			{
				return null;
			}
			icon_three.applyFromShortcutInfo( info3 , mIconCache );
			icon_three.setOperateIconLoadDone( Constants.DL_STATUS_SUCCESS );
			icon_three.setVisibility( View.VISIBLE );
		}
		else
		{
			icon_three.setVisibility( View.GONE );
		}
		//设置每个icon的图片和title ybh@2014/12/22 ADD END
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				if( v == icon_zero )
				{
					processClick( context , list , 0 );
				}
				else if( v == icon_one )
				{
					processClick( context , list , 1 );
				}
				else if( v == icon_two )
				{
					processClick( context , list , 2 );
				}
				else if( v == icon_three )
				{
					processClick( context , list , 3 );
				}
				smartDialog.setVisibility( View.GONE );
			}
		};
		LauncherAppState app = LauncherAppState.getInstance();
		DynamicGrid mDynamicGrid = app.getDynamicGrid();
		if( mDynamicGrid != null )
		{
			DeviceProfile grid = mDynamicGrid.getDeviceProfile();
			if( grid != null )
			{
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
				{
					icon_zero.setTextColor( context.getResources().getColor( android.R.color.black ) );
					icon_zero.setShadowsEnabled( false );
					icon_one.setTextColor( context.getResources().getColor( android.R.color.black ) );
					icon_one.setShadowsEnabled( false );
					icon_two.setTextColor( context.getResources().getColor( android.R.color.black ) );
					icon_two.setShadowsEnabled( false );
					icon_three.setTextColor( context.getResources().getColor( android.R.color.black ) );
					icon_three.setShadowsEnabled( false );
				}
				else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
				{
					int mCellWidth = grid.getSignleViewAvailableWidthPx();
					int mCellHeight = grid.getSignleViewAvailableHeightPx();
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( mCellWidth , mCellHeight );
					icon_zero.setLayoutParams( params );
					icon_one.setLayoutParams( params );
					icon_two.setLayoutParams( params );
					icon_three.setLayoutParams( params );
					//cheyingkun add start	//飞利浦图标样式适配480*854带虚拟按键手机(调整布局、替换图标)。【c_0003557】
					int heightPind = 0;
					if( LauncherAppState.getInstance().isVirtualMenuShown() )
					{
						heightPind = (int)( -icon_zero.getLineHeight() * 1.45f );
					}
					else
					//cheyingkun add end
					{
						heightPind = (int)( -icon_zero.getLineHeight() * 1.75f );//1.75f是调出来的，暂时未知为什么不能匹配bubbletextview中的
					}
					icon_zero.setCompoundDrawablePadding( heightPind );
					icon_one.setCompoundDrawablePadding( heightPind );
					icon_two.setCompoundDrawablePadding( heightPind );
					icon_three.setCompoundDrawablePadding( heightPind );
				}
			}
		}
		icon_zero.setOnClickListener( listener );
		icon_one.setOnClickListener( listener );
		icon_two.setOnClickListener( listener );
		icon_three.setOnClickListener( listener );
		dialog_cancel.setOnClickListener( listener );
		smartDialog.setOnTouchListener( new OnTouchListener() {
			
			@Override
			public boolean onTouch(
					View v ,
					MotionEvent event )
			{
				// TODO Auto-generated method stub
				if( v.getId() == R.id.dialog_layout )
					return false;
				else
				{
					return true;
				}
			}
		} );
		return smartDialog;
	}
	
	private void processClick(
			Context context ,
			ArrayList<DynamicEntrySmartDownloadInfo> list ,
			int index )
	{
		String filePath = DlManager.getInstance().getDownloadHandle().getDownSuccessFilePath( list.get( index ).getPkgName() );
		if( filePath == null )
		{
			return;
		}
		if( !OperateDynamicUtils.checkApkExist( context , list.get( 0 ).getPkgName() ) )
		{
			OperateDynamicUtils.installAPKFile( context , filePath );
		}
	}
	
	private ArrayList<DynamicEntrySmartDownloadInfo> processSmartDownloadInfo(
			Context context ,
			String smartInfo )
	{
		ArrayList<DynamicEntrySmartDownloadInfo> list = new ArrayList<DynamicEntrySmartDownloadInfo>();
		if( smartInfo.equals( DynamicEntryDialogConstant.DYNAMIC_VERSION ) )
		{
			List<dl_info> dl_info_list = SAHelper.getInstance( LauncherAppState.getActivityInstance() ).getSuccessButNotInstallList();
			if( dl_info_list != null )
			{
				int count = 0;
				for( dl_info info : dl_info_list )
				{
					String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
					if( pkgName != null )
					{
						String value = DlManager.getInstance().getSharedPreferenceHandle().getValue( StringUtils.concat( SharedPreferenceHandle.SILENTDOWNLOAD_PREFIX , pkgName ) );
						if( value != null && value.equals( String.valueOf( SharedPreferenceHandle.SIENT_SHOW ) ) )
						{
							if( count == 0 )
							{
								count++;
								continue;
							}
							else
							{
								String title = DlManager.getInstance().getWifiSAHandle().getTitleName( info );
								Bitmap bitmap = DlManager.getInstance().getDownloadHandle().getDownBitmap( pkgName );
								list.add( new DynamicEntrySmartDownloadInfo( pkgName , title , null , bitmap ) );
							}
						}
					}
				}
			}
			return list;
		}
		try
		{
			JSONObject content = new JSONObject( smartInfo );
			normalOpen = content.optBoolean( "normal" );
			JSONArray infos = content.getJSONArray( "SmartDownloadInfo" );
			for( int j = 0 ; j < infos.length() ; j++ )
			{
				JSONObject item = infos.getJSONObject( j );
				String pkgName = item.optString( "pkgName" );
				String title = item.optString( "title" );
				String path = item.optString( "path" );
				Bitmap bitmap = findBitmap( context , path );
				list.add( new DynamicEntrySmartDownloadInfo( pkgName , title , path , bitmap ) );
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
}
