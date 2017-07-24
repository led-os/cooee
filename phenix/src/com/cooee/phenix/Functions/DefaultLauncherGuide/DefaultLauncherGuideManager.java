// xiatian add whole file //设置默认桌面引导
package com.cooee.phenix.Functions.DefaultLauncherGuide;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.cooee.framework.function.DefaultLauncher.DefaultLauncherActivity;
import com.cooee.phenix.DragController;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.Workspace;
import com.cooee.util.DefaultDialog;


public class DefaultLauncherGuideManager
{
	
	private static final String DEVICE_MANUFACTURER = Build.MANUFACTURER.toLowerCase();
	private Context mApplicationContext = null;
	private Activity mActivity = null;
	private Launcher mLauncher = null;
	private static DefaultLauncherGuideManager mInstance = null;
	private WindowManager mWindowManager;
	private ActivityManager mActivityManager;
	private DefaultDialog mDefaultLauncherGuideDialog = null;
	private final int MSG_START = 0;
	private final int MSG_SHOW_GUIDE_DIALOG = 1;
	private static final int MSG_SHOW_TIPS_DIALOG = 2;
	private static final int MSG_HIDE_TIPS_DIALOG = 3;
	private final int MSG_END = 10000;
	private static final long SHOW_GUIDE_DIALOG_DELAY_FRIST_TIME = 10000;
	private static final long SHOW_GUIDE_DIALOG_DELAY_NOT_FRIST_TIME = 5000;
	private View mTipsDialogLayout;
	private View mTipsDialogView;
	private static final long MESSAGE_SHOW_TIPS_DIALOG_DELAY_TIME = 800;
	private static final long MESSAGE_HIDE_TIPS_DIALOG_DELAY_TIME = 8000;
	private static final long HANDLER_LOOP_DELAY_TIME = 200;
	
	public DefaultLauncherGuideManager(
			Context mApplicationContext ,
			Activity mActivity ,
			Launcher mLauncher )
	{
		this.mApplicationContext = mApplicationContext;
		this.mActivity = mActivity;
		this.mLauncher = mLauncher;
		mWindowManager = (WindowManager)mApplicationContext.getSystemService( Context.WINDOW_SERVICE );
		mActivityManager = (ActivityManager)mApplicationContext.getSystemService( Context.ACTIVITY_SERVICE );
		mInstance = this;
	}
	
	public static DefaultLauncherGuideManager getInstance()
	{
		return mInstance;
	}
	
	public void checkDefaultLauncherAndShowGuideDialog(
			boolean mIsFirst ,
			Context mContext )
	{
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
		if( ( mSharedPreferences.getBoolean( "mIsAllredalyShowDefaultLauncherGuideDialog" , false ) == false ) && isDefaultLauncher( mContext ) == false )
		{
			sendMessageShowGuideDialog( mIsFirst );
		}
	}
	
	private void sendMessageShowGuideDialog(
			boolean mIsFristTime )
	{
		mHandler.removeMessages( MSG_SHOW_GUIDE_DIALOG );
		Message msg = mHandler.obtainMessage( MSG_SHOW_GUIDE_DIALOG );
		long mDelay = SHOW_GUIDE_DIALOG_DELAY_NOT_FRIST_TIME;
		if( mIsFristTime )
		{
			mDelay = SHOW_GUIDE_DIALOG_DELAY_FRIST_TIME;
		}
		mHandler.sendMessageDelayed( msg , mDelay );
	}
	
	private void showGuideDialog()
	{
		//xiatian add start	//需求：在长按抬起图标的情况下和在Reordering模式下，5秒后再弹出“设置默认桌面引导”。
		if( isNeedToShowGuideDialogLater() )
		{
			checkDefaultLauncherAndShowGuideDialog( false , mApplicationContext );
			return;
		}
		//xiatian add end
		SharedPreferences mSharedPreferences = mLauncher.getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
		mSharedPreferences.edit().putBoolean( "mIsAllredalyShowDefaultLauncherGuideDialog" , true ).commit();
		if( mDefaultLauncherGuideDialog == null )
		{
			mDefaultLauncherGuideDialog = new DefaultDialog( mActivity );
			mDefaultLauncherGuideDialog.setTitle( R.string.default_launcher_guide_dialog_title );
			mDefaultLauncherGuideDialog.setContentText( R.string.default_launcher_guide_dialog_content );
			mDefaultLauncherGuideDialog.setPositiveButtonText( R.string.default_launcher_guide_dialog_ok );
			mDefaultLauncherGuideDialog.setNegativeButtonText( R.string.default_launcher_guide_dialog_cancel );
			mDefaultLauncherGuideDialog.setOnClickListener( new DefaultDialog.OnClickListener() {
				
				@Override
				public void onClickPositive(
						View v )
				{
					mDefaultLauncherGuideDialog.dismiss();
					showLauncherSelecetDialogWithTipsDialog();
				}
				
				@Override
				public void onClickNegative(
						View v )
				{
				}
				
				@Override
				public void onClickExit(
						View v )
				{
				}
			} );
		}
		mDefaultLauncherGuideDialog.show();
	}
	
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			super.handleMessage( msg );
			switch( msg.what )
			{
				case MSG_SHOW_GUIDE_DIALOG:
				{
					showGuideDialog();
				}
					break;
				case MSG_SHOW_TIPS_DIALOG:
				{
					showTipsDialogWithAnim();
				}
					break;
				case MSG_HIDE_TIPS_DIALOG:
				{
					hideTipsDialogWithAnim();
				}
					break;
			}
		}
	};
	
	private ResolveInfo getCurDefaultLauncher(
			Context mContext )
	{
		ResolveInfo curInfo = null;
		PackageManager localPackageManager = mContext.getPackageManager();
		Intent localIntent = new Intent( Intent.ACTION_MAIN );
		localIntent.addCategory( Intent.CATEGORY_HOME );
		List localList = localPackageManager.queryIntentActivities( localIntent , 0 );
		ArrayList localArrayList1 = new ArrayList();
		ArrayList localArrayList2 = new ArrayList();
		Iterator localIterator1 = localList.iterator();
		Iterator localIterator2;
		IntentFilter localIntentFilter;
		while( localIterator1.hasNext() )
		{
			curInfo = (ResolveInfo)localIterator1.next();
			localArrayList1.clear();
			localArrayList2.clear();
			localPackageManager.getPreferredActivities( localArrayList1 , localArrayList2 , curInfo.activityInfo.packageName );
			localIterator2 = localArrayList1.iterator();
			while( localIterator2.hasNext() )
			{
				localIntentFilter = (IntentFilter)localIterator2.next();
				if( localIntentFilter.hasAction( Intent.ACTION_MAIN ) && localIntentFilter.hasCategory( Intent.CATEGORY_HOME ) )
				{
					return curInfo;
				}
			}
		}
		return null;
	}
	
	public boolean isDefaultLauncher(
			Context mContext )
	{
		ResolveInfo resolveInfo = getCurDefaultLauncher( mContext );
		if( resolveInfo != null )
		{
			String str = resolveInfo.activityInfo.applicationInfo.packageName;
			Launcher mLauncher = (Launcher)LauncherAppState.getActivityInstance();
			if( mLauncher != null && str.equals( mLauncher.getPackageName() ) )
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean showLauncherSelecetDialog(
			Context mContext )
	{
		boolean mIsNeedShowSettingsDefaultLauncherTips = false;
		if( isXiaomiDevices() )
		{
			showLauncherSelecetDialogForXiaoMi( mContext );
		}
		else if( isHuaWeiDevices() )
		{
			showLauncherSelecetDialogForHuaWei( mContext );
		}
		else
		{
			clearPreDefaultSetting( mContext );
			PackageManager p = mContext.getPackageManager();
			ComponentName cn = new ComponentName( (Launcher)LauncherAppState.getActivityInstance() , DefaultLauncherActivity.class.getName() );
			p.setComponentEnabledSetting( cn , PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP );
			Intent selector = new Intent( Intent.ACTION_MAIN );
			selector.addCategory( Intent.CATEGORY_HOME );
			selector.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			mContext.startActivity( selector );
			p.setComponentEnabledSetting( cn , PackageManager.COMPONENT_ENABLED_STATE_DEFAULT , PackageManager.DONT_KILL_APP );
			mIsNeedShowSettingsDefaultLauncherTips = true;
		}
		return mIsNeedShowSettingsDefaultLauncherTips;
	}
	
	/**
	 * 功能：判断手机上是否只有一个桌面
	 * liwenxia add
	 * @param mContext
	 * @return
	 */
	public boolean isOnlyLauncher(
			Context mContext )
	{
		List<String> name = new ArrayList<String>();
		PackageManager p = mContext.getPackageManager();
		Intent intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_HOME );
		List<ResolveInfo> ResolveInfos = p.queryIntentActivities( intent , PackageManager.MATCH_DEFAULT_ONLY );
		for( ResolveInfo resolveInfo : ResolveInfos )
		{
			name.add( resolveInfo.activityInfo.packageName );
		}
		if( name.size() == 1 )
		{
			return true;
		}
		return false;
	}
	
	private void clearPreDefaultSetting(
			Context mContext )
	{
		ResolveInfo resolveInfo = getCurDefaultLauncher( mContext );
		if( resolveInfo != null )
		{
			String str = resolveInfo.activityInfo.applicationInfo.packageName;
			if( !str.equals( ( (Launcher)LauncherAppState.getActivityInstance() ).getPackageName() ) )
			{
				PackageManager p = mContext.getPackageManager();
				ComponentName cn = new ComponentName( ( (Launcher)LauncherAppState.getActivityInstance() ).getPackageName() , DefaultLauncherActivity.class.getName() );
				p.setComponentEnabledSetting( cn , PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP );
				Intent selector = new Intent( Intent.ACTION_MAIN );
				selector.addCategory( Intent.CATEGORY_HOME );
				selector.addCategory( Intent.CATEGORY_DEFAULT );
				p.resolveActivity( selector , PackageManager.GET_RESOLVED_FILTER );
				p.setComponentEnabledSetting( cn , PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP );
			}
		}
	}
	
	private boolean isHuaWeiDevices()
	{
		if( DEVICE_MANUFACTURER.equalsIgnoreCase( "huawei" ) )
		{
			return true;
		}
		return false;
	}
	
	private boolean isXiaomiDevices()
	{
		if( DEVICE_MANUFACTURER.equalsIgnoreCase( "xiaomi" ) )
		{
			return true;
		}
		return false;
	}
	
	private void showLauncherSelecetDialogForXiaoMi(
			Context mContext )
	{
		Intent intent = new Intent( "android.intent.action.MAIN" );
		intent.addCategory( "android.intent.category.HOME" );
		intent.setComponent( new ComponentName( "android" , "com.android.internal.app.ResolverActivity" ) );
		intent.getIntExtra( "u" , 0 );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		mContext.startActivity( intent );
	}
	
	private void showLauncherSelecetDialogForHuaWei(
			Context mContext )
	{
		Intent localIntent1 = new Intent( "android.intent.action.MAIN" );
		Intent localIntent2 = localIntent1.setClassName( "com.android.settings" , "com.android.settings.Settings$PreferredListSettingsActivity" );
		localIntent2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		mContext.startActivity( localIntent2 );
	}
	
	private void showLauncherSelecetDialogWithTipsDialog()
	{
		if( showLauncherSelecetDialog( mApplicationContext ) )
		{
			showTipsDialog();
		}
	}
	
	private void showTipsDialog()
	{
		if( mHandler != null )
		{
			mHandler.postDelayed( mRunable , HANDLER_LOOP_DELAY_TIME );
		}
		if( mTipsDialogLayout == null )
		{
			mTipsDialogLayout = LayoutInflater.from( mApplicationContext ).inflate( R.layout.default_launcher_tips_dialog_layout , null );
			mTipsDialogView = mTipsDialogLayout.findViewById( R.id.default_launcher_tips_dialog_view );
		}
		WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
		mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		mParams.format = PixelFormat.RGBA_8888;
		mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		mParams.width = mWindowManager.getDefaultDisplay().getWidth();
		mParams.height = LayoutParams.WRAP_CONTENT;
		mParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		mParams.x = 0;
		mParams.y = 0;
		mWindowManager.addView( mTipsDialogLayout , mParams );
		mTipsDialogLayout.invalidate();
		mWindowManager.updateViewLayout( mTipsDialogLayout , mParams );
		sendMessageShowTipsDialog();
	}
	
	private void sendMessageShowTipsDialog()
	{
		mHandler.removeMessages( MSG_SHOW_TIPS_DIALOG );
		Message msg = mHandler.obtainMessage( MSG_SHOW_TIPS_DIALOG );
		mHandler.sendMessageDelayed( msg , MESSAGE_SHOW_TIPS_DIALOG_DELAY_TIME );
	}
	
	private void showTipsDialogWithAnim()
	{
		if( mTipsDialogView == null )
		{
			return;
		}
		Animation animation = AnimationUtils.loadAnimation( mApplicationContext , R.anim.anim_default_launcher_tips_dialog_in );
		animation.setAnimationListener( new AnimationListener() {
			
			@Override
			public void onAnimationStart(
					Animation animation )
			{
			}
			
			@Override
			public void onAnimationRepeat(
					Animation animation )
			{
			}
			
			@Override
			public void onAnimationEnd(
					Animation arg0 )
			{
				sendMessageHideTipsDialog();
			}
		} );
		mTipsDialogView.startAnimation( animation );
		mTipsDialogView.setVisibility( View.VISIBLE );
	}
	
	private void sendMessageHideTipsDialog()
	{
		mHandler.removeMessages( MSG_HIDE_TIPS_DIALOG );
		Message msg = mHandler.obtainMessage( MSG_HIDE_TIPS_DIALOG );
		mHandler.sendMessageDelayed( msg , MESSAGE_HIDE_TIPS_DIALOG_DELAY_TIME );
	}
	
	private void hideTipsDialogWithAnim()
	{
		if( mTipsDialogView == null )
		{
			return;
		}
		Animation animationOut = AnimationUtils.loadAnimation( mApplicationContext , R.anim.anim_default_launcher_tips_dialog_out );
		animationOut.setAnimationListener( new AnimationListener() {
			
			@Override
			public void onAnimationStart(
					Animation arg0 )
			{
			}
			
			@Override
			public void onAnimationRepeat(
					Animation arg0 )
			{
			}
			
			@Override
			public void onAnimationEnd(
					Animation arg0 )
			{
				removeTipsDialog();
			}
		} );
		mTipsDialogView.startAnimation( animationOut );
		mTipsDialogView.setVisibility( View.INVISIBLE );
	}
	
	private void removeTipsDialog()
	{
		if( mTipsDialogLayout != null )
		{
			mWindowManager.removeView( mTipsDialogLayout );
			mTipsDialogLayout = null;
		}
		if( mHandler != null )
		{
			mHandler.removeCallbacks( mRunable );
			mHandler.removeMessages( MSG_SHOW_TIPS_DIALOG );
			mHandler.removeMessages( MSG_HIDE_TIPS_DIALOG );
		}
	}
	
	private Runnable mRunable = new Runnable() {
		
		@Override
		public void run()
		{
			mHandler.postDelayed( this , HANDLER_LOOP_DELAY_TIME );
			String processName;
			processName = mActivityManager.getRunningAppProcesses().get( 0 ).processName;
			if( !"system:ui".equals( processName ) )
			{
				removeTipsDialog();
			}
		}
	};
	
	public void closeDefaultLauncherGuideDialog()
	{
		if( mDefaultLauncherGuideDialog != null )
		{
			mDefaultLauncherGuideDialog.dismiss();
		}
	}
	
	public void removeMessageShowGuideDialog()
	{
		mHandler.removeMessages( MSG_SHOW_GUIDE_DIALOG );
	}
	
	//xiatian add start	//需求：在长按抬起图标的情况下和在Reordering模式下，5秒后再弹出“设置默认桌面引导”。
	private boolean isNeedToShowGuideDialogLater()
	{
		boolean ret = false;
		DragController mDragController = mLauncher.getDragController();
		Workspace mWorkspace = mLauncher.getWorkspace();
		if(
		//
		( mDragController != null && mDragController.isDragging() )
		//
		|| ( mWorkspace != null && mWorkspace.isReordering( true ) ) )
		{
			ret = true;
		}
		return ret;
	}
	//xiatian add end
}
