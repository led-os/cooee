package com.iLoong.launcher.MList;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;

import cool.sdk.MicroEntry.MicroEntryHelper;
import cool.sdk.MicroEntry.MicroEntryLog;
import cool.sdk.MicroEntry.MicroEntryLog.MicroEntryLogItem;
import cool.sdk.log.CoolLog;


public class MeLauncherInterface
{
	
	Class<?> mActivityClass[] = { Main_FirstActivity.class , Main_SecondActivity.class , Main_ThreeActivity.class , Main_FourthActicity.class };
	static MeLauncherInterface instance = null;
	
	public static MeLauncherInterface getInstance()
	{
		synchronized( MeLauncherInterface.class )
		{
			if( instance == null )
			{
				instance = new MeLauncherInterface();
			}
		}
		return instance;
	}
	
	static ProgressDialog builder = null;
	
	public void setDialog(
			Context context ,
			int mIconResID )
	{
		builder = new ProgressDialog( context );
		if( 0 != mIconResID )
		{
			builder.setTitle( R.string.cool_ml_MeIcon_uninstall );
			builder.setIcon( mIconResID );
		}
		builder.setMessage( LauncherDefaultConfig.getString( R.string.cool_ml_MeIcon_uninstalling ) );
		builder.setCanceledOnTouchOutside( false );
		builder.setOnCancelListener( new OnCancelListener() {
			
			@Override
			public void onCancel(
					DialogInterface arg0 )
			{
				// TODO Auto-generated method stub
				MELOG.v( "ME_RTFSC" , "setOnCancelListener onCancel " );
			}
		} );
		builder.show();
	}
	
	public void LogDelete(
			final Context context ,
			final String compName )
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				MELOG.v( "ME_RTFSC" , "========  LogDelete =========" );
				try
				{
					MyR RR = MyR.getMyR( context );
					if( RR == null )
					{
						return;
					}
					String[] name = {
							context.getString( RR.string.cool_ml_app_name1 ) ,
							context.getString( RR.string.cool_ml_app_name2 ) ,
							context.getString( RR.string.cool_ml_app_name3 ) ,
							context.getString( RR.string.cool_ml_app_name4 ) };
					List<MicroEntryLogItem> DeleteItemList = new ArrayList<MicroEntryLog.MicroEntryLogItem>();
					for( int i = 0 ; i < 4 ; i++ )
					{
						if( mActivityClass[i].getName().equals( compName ) )
						{
							boolean isHide = false;
							isHide = PreferenceManager.getDefaultSharedPreferences( context ).getBoolean( StringUtils.concat( "ME_HIDE:" , mActivityClass[i].getName() ) , false );
							if( true == isHide )
							{
								MicroEntryLogItem myItem = new MicroEntryLogItem();
								myItem.id = i + 1;
								myItem.type = 2;
								myItem.name = name[i];
								MELOG.v( "ME_RTFSC" , StringUtils.concat( "myItem.name:" , myItem.name ) );
								DeleteItemList.add( myItem );
							}
						}
					}
					if( DeleteItemList.size() >= 1 )
					{
						MELOG.v( "ME_RTFSC" , StringUtils.concat( "DeleteItemList.size():" , DeleteItemList.size() ) );
						MicroEntryLog.LogDelete( context , DeleteItemList );
					}
				}
				catch( Exception e )
				{
					// TODO: handle exception
					MELOG.e( "ME_RTFSC" , StringUtils.concat( "OnModeHideToShow error:" , e.toString() ) );
				}
			}
		} ).start();
	}
	
	public void canelDialog()
	{
		if( builder != null )
		{
			builder.dismiss();
			builder = null;
		}
	}
	
	//	public static void MEIconFilter(
	//			String PackageName ,
	//			String ClassName ,
	//			Iterator<ResolveInfo> ite )
	//	{
	//		// TODO Auto-generated method stub
	//		try
	//		{
	//			//MELOG.v( "ME_RTFSC" , "MEIconFilter getPackageName():" + iLoongLauncher.getInstance().getPackageName() );
	//			if( PackageName.equals( iLoongLauncher.getInstance().getPackageName() ) )
	//			{
	//				ComponentName componentName = new ComponentName( PackageName , ClassName );
	//				if( true == PreferenceManager.getDefaultSharedPreferences( iLoongLauncher.getInstance() ).getBoolean( "HIDE:" + componentName.toString() , false ) )
	//				{
	//					MELOG.v( "ME_RTFSC" , " MEIconFilter  ClassName:" + ClassName );
	//					ite.remove();
	//				}
	//			}
	//		}
	//		catch( Exception e )
	//		{
	//			// TODO: handle exception
	//			MELOG.v( "ME_RTFSC" , "MEIconFilter Error:" + e.toString() );
	//		}
	//	}
	String MEClassName = null;
	private int isNotMeIcon = -1;
	private int MeIconCanDel = 0;
	private int MeIconCanNotDel = 1;
	
	public int MeIconOnDropOptType(
			Context mContext ,
			ComponentName compname )
	{
		int ret = isNotMeIcon;
		if( null != compname )
		{
			String LauncherPkgName = mContext.getApplicationContext().getPackageName();
			MEClassName = compname.getClassName();
			if( ( compname.getPackageName().equals( LauncherPkgName ) ) && ( MEClassName.equals( "com.iLoong.launcher.MList.Main_FirstActivity" ) || MEClassName
					.equals( "com.iLoong.launcher.MList.Main_SecondActivity" ) || MEClassName.equals( "com.iLoong.launcher.MList.Main_ThreeActivity" ) || MEClassName
						.equals( "com.iLoong.launcher.MList.Main_FourthActicity" ) ) )
			{
				if( !MicroEntryHelper.getInstance( mContext ).allowDeleteEntry() )
				{
					return MeIconCanNotDel;
				}
				else
				{
					return MeIconCanDel;
				}
			}
		}
		return ret;
	}
	
	public boolean MeIsMicroEntry(
			String mClassName )
	{
		// TODO Auto-generated method stub
		if( mClassName.equals( "com.iLoong.launcher.MList.Main_FirstActivity" ) || mClassName.equals( "com.iLoong.launcher.MList.Main_SecondActivity" ) || mClassName
				.equals( "com.iLoong.launcher.MList.Main_ThreeActivity" ) || mClassName.equals( "com.iLoong.launcher.MList.Main_FourthActicity" ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int MeGetIconResIDByClass(
			String mClassName )
	{
		if( mClassName.endsWith( "com.iLoong.launcher.MList.Main_FirstActivity" ) )
		{
			return R.drawable.cool_ml_wonderful_game;
		}
		else if( mClassName.endsWith( "com.iLoong.launcher.MList.Main_SecondActivity" ) )
		{
			return R.drawable.cool_ml_software;
		}
		else if( mClassName.endsWith( "com.iLoong.launcher.MList.Main_ThreeActivity" ) )
		{
			return R.drawable.cool_ml_ku_store;
		}
		else if( mClassName.endsWith( "com.iLoong.launcher.MList.Main_FourthActicity" ) )
		{
			return R.drawable.cool_ml_know;
		}
		else
		{
			return 0;
		}
	}
	
	//	public boolean MedelToHide(
	//			View3D view ,
	//			ShortcutInfo sInfo )
	//	{
	//		// TODO Auto-generated method stub
	//		try
	//		{
	//			ComponentName compname = sInfo.getIntent().getComponent();
	//			if( null != compname )
	//			{
	//				MEClassName = compname.getClassName();
	//				MELOG.v( "ME_RTFSC" , "MedelToHide compname:" + compname.toShortString() );
	//				MELOG.v( "ME_RTFSC" , "MedelToHide getPackageName():" + iLoongLauncher.getInstance().getPackageName() );
	//				if( ( compname.getPackageName().equals( iLoongLauncher.getInstance().getPackageName() ) ) && ( MEClassName.equals( "com.iLoong.launcher.MList.Main_FirstActivity" ) || MEClassName
	//						.equals( "com.iLoong.launcher.MList.Main_SecondActivity" ) || MEClassName.equals( "com.iLoong.launcher.MList.Main_ThreeActivity" ) || MEClassName
	//							.equals( "com.iLoong.launcher.MList.Main_FourthActicity" ) ) )
	//				{
	//					view.remove();
	//					Root3D.deleteFromDB( sInfo );
	//					SendMsgToAndroid.sendOurToastMsg( R3D.getString( RR.string.hide_me_icon ) );
	//					PreferenceManager.getDefaultSharedPreferences( iLoongLauncher.getInstance() ).edit().putBoolean( "HIDE:" + compname.toString() , true ).commit();
	//					new Thread( new Runnable() {
	//						
	//						@Override
	//						public void run()
	//						{
	//							// TODO Auto-generated method stub
	//							MicroEntryHelper.getInstance( iLoongLauncher.getInstance() ).UpdateDeleteItemByHide( 1 , MEClassName );
	//						}
	//					} ).start();
	//					return true;
	//				}
	//			}
	//		}
	//		catch( Exception e )
	//		{
	//			// TODO: handle exception
	//			MELOG.v( "ME_RTFSC" , "MedelToHide Error:" + e.toString() );
	//		}
	//		return false;
	//	}
	//	public void OnModeChangeMeUpdate(
	//			int PerMode ,
	//			int CurMode )
	//	{
	//		try
	//		{
	//			MELOG.v( "ME_RTFSC" , "PerMode:" + PerMode + " CurMode:" + CurMode );
	//			if( AppList3D.APP_LIST3D_SHOW == PerMode && AppList3D.APP_LIST3D_HIDE == CurMode )
	//			{
	//				new Thread( new Runnable() {
	//					
	//					@Override
	//					public void run()
	//					{
	//						// TODO Auto-generated method stub
	//						OnModeShowToHide();
	//					}
	//				} ).start();
	//			}
	//			if( AppList3D.APP_LIST3D_HIDE == PerMode && AppList3D.APP_LIST3D_SHOW == CurMode )
	//			{
	//				new Thread( new Runnable() {
	//					
	//					@Override
	//					public void run()
	//					{
	//						// TODO Auto-generated method stub
	//						OnModeHideToShow();
	//					}
	//				} ).start();
	//			}
	//		}
	//		catch( Exception e )
	//		{
	//			// TODO: handle exception
	//			MELOG.e( "ME_RTFSC" , "OnModeChangeMeUpdate error:" + e.toString() );
	//		}
	//	}
	//	public void OnModeShowToHide()
	//	{
	//		MELOG.v( "ME_RTFSC" , "====  OnModeShowToHide  ======" );
	//		try
	//		{
	//			for( int i = 0 ; i < 4 ; i++ )
	//			{
	//				ComponentName componentName = new ComponentName( iLoongApplication.getInstance() , mActivityClass[i] );
	//				MELOG.v( "ME_RTFSC" , "KEY" + componentName.toString() );
	//				boolean isHide = false;
	//				if( componentName.getPackageName().equals( "com.cool.launcher" ) || componentName.getPackageName().equals( "com.cooee.unilauncher" ) || componentName.getPackageName().equals(
	//						"com.cooee.Mylauncher" ) )
	//				{
	//					isHide = PreferenceManager.getDefaultSharedPreferences( iLoongLauncher.getInstance() ).getBoolean( "HIDE:" + componentName.toString() , false );
	//				}
	//				else if( componentName.getPackageName().equals( "com.cooee.launcherS4" ) || componentName.getPackageName().equals( "com.cooee.launcherS5" ) )
	//				{
	//					isHide = iLoongLauncher.getInstance().prefs.getBoolean( "HIDE:" + componentName.toString() , false );
	//				}
	//				if( true == isHide )
	//				{
	//					MicroEntryHelper.getInstance( iLoongApplication.getInstance() ).setValue( mActivityClass[i].getName() , "true" );
	//				}
	//				else
	//				{
	//					MicroEntryHelper.getInstance( iLoongApplication.getInstance() ).setValue( mActivityClass[i].getName() , "false" );
	//				}
	//			}
	//		}
	//		catch( Exception e )
	//		{
	//			// TODO: handle exception
	//			MELOG.e( "ME_RTFSC" , "OnModeShowToHide error:" + e.toString() );
	//		}
	//	}
	//	
	//	public void OnModeHideToShow()
	//	{
	//		MELOG.v( "ME_RTFSC" , "========  OnModeHideToShow =========" );
	//		try
	//		{
	//			Context context = iLoongApplication.getInstance();
	//			MyR RR = MyR.getMyR( context );
	//			if( RR == null )
	//			{
	//				return;
	//			}
	//			String[] name = {
	//					context.getString( RR.string.cool_ml_app_name1 ) ,
	//					context.getString( RR.string.cool_ml_app_name2 ) ,
	//					context.getString( RR.string.cool_ml_app_name3 ) ,
	//					context.getString( RR.string.cool_ml_app_name4 ) };
	//			List<MicroEntryLogItem> DeleteItemList = new ArrayList<MicroEntryLog.MicroEntryLogItem>();
	//			for( int i = 0 ; i < 4 ; i++ )
	//			{
	//				ComponentName componentName = new ComponentName( iLoongApplication.getInstance() , mActivityClass[i] );
	//				boolean isHide = false;
	//				if( componentName.getPackageName().equals( "com.cool.launcher" ) || componentName.getPackageName().equals( "com.cooee.unilauncher" ) )
	//				{
	//					isHide = PreferenceManager.getDefaultSharedPreferences( iLoongLauncher.getInstance() ).getBoolean( "HIDE:" + componentName.toString() , false );
	//				}
	//				else if( componentName.getPackageName().equals( "com.cooee.launcherS4" ) || componentName.getPackageName().equals( "com.cooee.launcherS5" ) )
	//				{
	//					isHide = iLoongLauncher.getInstance().prefs.getBoolean( "HIDE:" + componentName.toString() , false );
	//				}
	//				MELOG.v( "ME_RTFSC" , "KEY" + componentName.toString() + "VALUE:" + isHide );
	//				if( true == isHide && MicroEntryHelper.getInstance( iLoongApplication.getInstance() ).getString( mActivityClass[i].getName() , "" ).equals( "false" ) )
	//				{
	//					MicroEntryLogItem myItem = new MicroEntryLogItem();
	//					myItem.id = i + 1;
	//					myItem.type = 2;
	//					myItem.name = name[i];
	//					MELOG.v( "ME_RTFSC" , "myItem" + myItem.name );
	//					DeleteItemList.add( myItem );
	//				}
	//				//处理完成后，对存入的值直接清除为空字符串
	//				//setValue( mActivityClass[i].getName() , "" );
	//			}
	//			if( DeleteItemList.size() >= 1 )
	//			{
	//				MELOG.v( "ME_RTFSC" , "DeleteItemList.size()" + DeleteItemList.size() );
	//				MicroEntryLog.LogDelete( context , DeleteItemList );
	//			}
	//		}
	//		catch( Exception e )
	//		{
	//			// TODO: handle exception
	//			MELOG.e( "ME_RTFSC" , "OnModeHideToShow error:" + e.toString() );
	//		}
	//	}
	public void ShowNotificationifExist(
			Context mContext )
	{
		try
		{
			MicroEntryHelper.getInstance( mContext ).ShowNotifcation();
		}
		catch( Exception e )
		{
			// TODO: handle exception
			MELOG.e( "ME_RTFSC" , StringUtils.concat( "ShowNotificationifExist error:" , e.toString() ) );
		}
	}
	
	public void SetMEConfig()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				try
				{
					File file = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , "/cooee/ME_CFG" ) );
					if( file.exists() )
					{
						FileInputStream inStream = new FileInputStream( file );//读文件
						byte[] buffer = new byte[inStream.available()];
						inStream.read( buffer );
						String json = new String( buffer , "utf-8" );
						JSONObject jsonObject = new JSONObject( json );
						String SDK_LOG = jsonObject.getString( "SDK_LOG" );
						String ME_LOG = jsonObject.getString( "ME_LOG" );
						if( ME_LOG.equals( "true" ) )
						{
							MELOG.setEnableLog( true );
						}
						if( SDK_LOG.equals( "true" ) )
						{
							CoolLog.setEnableLog( true );
						}
						inStream.close();
					}
				}
				catch( Exception e )
				{
					// TODO: handle exception
					MELOG.e( "ME_RTFSC" , StringUtils.concat( "ERROR:" , e.toString() ) );
				}
			}
		} ).start();
	}
}
