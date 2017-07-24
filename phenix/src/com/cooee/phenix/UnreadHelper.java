// xiatian add whole file //添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
package com.cooee.phenix;


import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.DisplayMetrics;
import android.util.Log;

import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.update.UpdateIconManager;


public class UnreadHelper
{
	
	private static final String TAG = "UnreadHelper";
	public static final String UNREAD_INFO = "unread_info_of_mms_call";
	private Paint mPaint = new Paint();
	private Canvas sCanvas = new Canvas();
	private int MAX_SIZE = 100;
	private float mFewTextSize;
	private float mManytextSize;
	private int mOffsetY = 0;
	private int mFewTextSizeScale = 13;//小于99个时，显示字体的缩放值
	private int mManytextSizeScale = 9;//大于99个时，显示字体的缩放值
	private int mUnreadMmsAndSmsCount;
	private int mUnreadMissCallCount;
	private ComponentName mUnreadMmsAndSmsComponentName;
	private ComponentName mUnreadMissCallComponentName;
	private Context mContext;
	private Handler mLauncherHandler;
	private Launcher mLauncher = null;
	public static boolean mIsAlreadyOnChangeBySelf = false;
	private int REFRESH_UNREAD_INFO_DELAY = 1000; //WangLei add //更新未读短信和未接电话的线程延迟执行时间
	private String mDialerComponent; //配置的电话应用包类名
	private String mSmsComponent; //配置的短信应用包类名
	
	public UnreadHelper(
			Context mContext )
	{
		if( mContext != null )
		{
			this.mContext = mContext;
			ContentResolver mContentResolver = this.mContext.getContentResolver();
			mContentResolver.registerContentObserver( Uri.parse( "content://mms-sms/" ) , true , UnreadSmsContentObserver );
			mContentResolver.registerContentObserver( CallLog.Calls.CONTENT_URI , false , UnAnsweredCallsContentObserver );
			REFRESH_UNREAD_INFO_DELAY = LauncherDefaultConfig.getInt( R.integer.config_refresh_unread_info_delay ); //WangLei add //更新未读短信和未接电话的线程延迟执行时间
			mDialerComponent = LauncherDefaultConfig.getString( R.string.config_unread_call_component );
			mSmsComponent = LauncherDefaultConfig.getString( R.string.config_unread_sms_component );
			setUnreadMissCallComponentName( ComponentName.unflattenFromString( mDialerComponent ) );
			setUnreadMmsAndSmsComponentName( ComponentName.unflattenFromString( mSmsComponent ) );
		}
	}
	
	public void initialize(
			Launcher mLauncher )
	{
		this.mLauncher = mLauncher;
		if( mLauncher != null )
		{
			mLauncherHandler = mLauncher.getHandler();
		}
	}
	
	public void setUnreadMissCallComponentName(
			ComponentName mUnreadMissCallComponentName )
	{
		this.mUnreadMissCallComponentName = mUnreadMissCallComponentName;
		mUnreadMissCallCount = getUnreadNumByComponentName( mUnreadMissCallComponentName );
	}
	
	public void setUnreadMmsAndSmsComponentName(
			ComponentName mUnreadMmsAndSmsComponentName )
	{
		this.mUnreadMmsAndSmsComponentName = mUnreadMmsAndSmsComponentName;
		mUnreadMmsAndSmsCount = getUnreadNumByComponentName( mUnreadMmsAndSmsComponentName );
	}
	
	private int getUnreadNumByComponentName(
			ComponentName mComponentName )
	{
		if( ( mComponentName == null ) || ( !( ( mComponentName.equals( mUnreadMmsAndSmsComponentName ) ) || mComponentName.equals( mUnreadMissCallComponentName ) ) ) )
		{
			return 0;
		}
		SharedPreferences mUnreadInfoSharedPreferences = (SharedPreferences)mContext.getSharedPreferences( UNREAD_INFO , Context.MODE_PRIVATE );
		int mDefValue = 0;
		if( mComponentName.equals( mUnreadMmsAndSmsComponentName ) )
		{
			mDefValue = mUnreadMmsAndSmsCount;
		}
		else if( mComponentName.equals( mUnreadMissCallComponentName ) )
		{
			mDefValue = mUnreadMissCallCount;
		}
		return mUnreadInfoSharedPreferences.getInt( mComponentName.toString() , mDefValue );
	}
	
	private int getUnreadNumByComponentName(
			ItemInfo mItemInfo )
	{
		ComponentName mComponentName = null;
		if( mItemInfo != null )
		{
			Intent mIntent = mItemInfo.getIntent();
			if( mIntent != null )
			{
				mComponentName = mIntent.getComponent();
			}
		}
		return getUnreadNumByComponentName( mComponentName );
	}
	
	public void saveUnreadNumByComponentName(
			ComponentName mComponentName ,
			int mUnreadNum )
	{
		SharedPreferences mUnreadInfoPreference = (SharedPreferences)mContext.getSharedPreferences( UNREAD_INFO , Context.MODE_PRIVATE );
		SharedPreferences.Editor editor = mUnreadInfoPreference.edit();
		editor.putInt( mComponentName.toString() , mUnreadNum );
		editor.commit();
	}
	
	public void onChangeBySelf()
	{
		//每次桌面onCreate之后，都需要主动调用一次该方法，用来主动查询未接来电和未读信息，否则在未接来电和未读信息的ContentObserver变化之前，不显示相关提示
		if( mIsAlreadyOnChangeBySelf == true )
		{
			return;
		}
		mIsAlreadyOnChangeBySelf = true;
		UnreadSmsContentObserver.onChange( true );
		UnAnsweredCallsContentObserver.onChange( true );
	}
	
	public void onTerminate(
			ContentResolver mContentResolver )
	{
		mContentResolver.unregisterContentObserver( UnreadSmsContentObserver );
		mContentResolver.unregisterContentObserver( UnAnsweredCallsContentObserver );
	}
	
	ContentObserver UnreadSmsContentObserver = new ContentObserver( new Handler() ) {
		
		@Override
		public void onChange(
				boolean selfChange )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "mUnreadMmsContentObserver , onChange" );
			// 大数据操作 在线程中 进行  
			if( mLauncherHandler == null && mLauncher != null )
			{
				mLauncherHandler = mLauncher.getHandler();
			}
			if( mLauncherHandler != null )
			{
				mLauncherHandler.removeCallbacks( updateUnreadMmsRunnable );
				mLauncherHandler.postDelayed( updateUnreadMmsRunnable , REFRESH_UNREAD_INFO_DELAY );
			}
			super.onChange( selfChange );
		}
	};
	
	private void updateUnreadMmsAndSmsNum()
	{
		mUnreadMmsAndSmsCount = getUnreadSmsNum() + getUnreadMmsNum();
		if( mLauncher != null )
		{
			mLauncher.updateUnreadNumberByComponent( mUnreadMmsAndSmsComponentName , mUnreadMmsAndSmsCount );
		}
	}
	
	private int getUnreadSmsNum()
	{
		int ret = 0;
		Cursor mCursor = null;
		if( mContext == null )
		{
			return ret;
		}
		try
		{
			mCursor = mContext.getContentResolver().query( Uri.parse( "content://sms" ) , null , "type = 1 and read = 0" , null , null );
			if( mCursor != null )
			{
				ret = mCursor.getCount();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( mCursor != null )
			{
				mCursor.close();
				mCursor = null;
			}
		}
		return ret;
	}
	
	private int getUnreadMmsNum()
	{
		int ret = 0;
		Cursor mCursor = null;
		if( mContext == null )
		{
			return ret;
		}
		try
		{
			mCursor = mContext.getContentResolver().query( Uri.parse( "content://mms/inbox" ) , null , "read = 0 AND (m_type = 132 OR m_type = 130)" , null , null );
			if( mCursor != null )
			{
				ret = mCursor.getCount();//未读彩信数目   
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( mCursor != null )
			{
				mCursor.close();
				mCursor = null;
			}
		}
		return ret;
	}
	
	ContentObserver UnAnsweredCallsContentObserver = new ContentObserver( new Handler() ) {
		
		@Override
		public void onChange(
				boolean selfChange )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "mUnAnsweredCallsContentObserver , onChange" );
			// 大数据操作 在线程中 进行  
			if( mLauncherHandler == null && mLauncher != null )
			{
				mLauncherHandler = mLauncher.getHandler();
			}
			if( mLauncherHandler != null )
			{
				mLauncherHandler.removeCallbacks( updateUnreadMisscallRunnable );
				mLauncherHandler.postDelayed( updateUnreadMisscallRunnable , REFRESH_UNREAD_INFO_DELAY );
			}
			super.onChange( selfChange );
		}
	};
	
	private void updateUnReadMissCallNum()
	{
		mUnreadMissCallCount = getUnreadMissCallNum();
		if( mLauncher != null )
		{
			mLauncher.updateUnreadNumberByComponent( mUnreadMissCallComponentName , mUnreadMissCallCount );
		}
	}
	
	private int getUnreadMissCallNum()
	{
		int ret = 0;
		Cursor mCursor = null;
		if( mContext == null )
		{
			return ret;
		}
		try
		{
			mCursor = mContext.getContentResolver().query(
					CallLog.Calls.CONTENT_URI ,
					new String[]{ Calls.TYPE , Calls.NEW } ,
					" type=? and new=?" ,
					new String[]{ String.valueOf( Calls.MISSED_TYPE ) , "1" } ,
					"date desc" );
			if( mCursor != null )
			{
				ret = mCursor.getCount();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( mCursor != null )
			{
				mCursor.close();
				mCursor = null;
			}
		}
		return ret;
	}
	
	/**
	 * Set this as the current Launcher activity object for the loader.
	 */
	public Bitmap getBitmapWithNum(
			Context context ,
			ItemInfo info ,
			Bitmap origin )
	{
		int mUnreadNum = getUnreadNumByComponentName( info );
		if( mUnreadNum <= 0 )
		{
			return origin;
		}
		Bitmap bitmapWithNum = origin.copy( Bitmap.Config.ARGB_8888 , true );
		Canvas canvas = new Canvas( bitmapWithNum );
		Bitmap mSubBitmap = drawSubBitmap( context , mUnreadNum );
		canvas.drawBitmap( mSubBitmap , bitmapWithNum.getWidth() - mSubBitmap.getWidth() , 0 , new Paint() );
		if( mSubBitmap != null && !mSubBitmap.isRecycled() )
		{
			mSubBitmap.recycle();
		}
		return bitmapWithNum;
	}
	
	public Bitmap drawSubBitmap(
			Context context ,
			int drawText )
	{
		mPaint.setAntiAlias( true );
		mPaint.setColor( Color.WHITE );
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		final float density = metrics.density;
		mFewTextSize = mFewTextSizeScale * density;
		mManytextSize = mManytextSizeScale * density;
		String endstr = drawText < MAX_SIZE ? ( String.valueOf( drawText ) ) : "99+";
		if( drawText < MAX_SIZE )
		{
			mPaint.setTextSize( mFewTextSize );
		}
		else
		{
			mPaint.setTextSize( mManytextSize );
		}
		Drawable mIcon = context.getResources().getDrawable( R.drawable.icon_tip_unread_bg_shape );
		int width = mIcon.getIntrinsicWidth();
		int height = mIcon.getIntrinsicHeight();
		int textWidth = (int)mPaint.measureText( endstr , 0 , endstr.length() );
		if( textWidth > width )
		{
			width = textWidth + 6;
		}
		final Bitmap thumb = Bitmap.createBitmap( width , height , Bitmap.Config.ARGB_8888 );
		final Canvas canvas = sCanvas;
		canvas.setBitmap( thumb );
		Rect r = new Rect( 0 , 0 , width , height );
		mIcon.setBounds( r );
		mIcon.draw( canvas );
		int x = (int)( ( width - textWidth ) * 0.5 );
		FontMetrics fm = mPaint.getFontMetrics();
		int y = (int)( ( height - ( fm.descent - fm.ascent ) ) * 0.5 - fm.ascent );
		canvas.drawText( endstr , x , y - mOffsetY , mPaint );
		return thumb;
	}
	
	//WangLei add start //优化更新未读信息和未接电话的方法
	Runnable updateUnreadMmsRunnable = new Runnable() {
		
		@Override
		public void run()
		{
			updateUnreadMmsAndSmsNum();
		}
	};
	Runnable updateUnreadMisscallRunnable = new Runnable() {
		
		@Override
		public void run()
		{
			updateUnReadMissCallNum();
		}
	};
	//WangLei add end
	;
	
	// zhangjin@2015/12/24 ADD START 增加
	public Bitmap getTipsBitmap(
			Context context ,
			ItemInfo info ,
			Bitmap origin )
	{
		if( isCmpHasTips( info ) == false )
		{
			return origin;
		}
		Bitmap bitmapWithNum = origin.copy( Bitmap.Config.ARGB_8888 , true );
		Canvas canvas = new Canvas( bitmapWithNum );
		Bitmap mSubBitmap = drawSubBitmap( context , 1 );
		canvas.drawBitmap( mSubBitmap , bitmapWithNum.getWidth() - mSubBitmap.getWidth() , 0 , new Paint() );
		if( mSubBitmap != null && !mSubBitmap.isRecycled() )
		{
			mSubBitmap.recycle();
		}
		return bitmapWithNum;
	}
	
	private boolean isCmpHasTips(
			ItemInfo mItemInfo )
	{
		if( UpdateIconManager.getInstance().isLauncherUpdateIcon( mItemInfo ) && UpdateIconManager.getInstance().isHasUpdate() )
		//		if( UpdateIconManager.getInstance().isLauncherUpdateIcon( mItemInfo ) )
		{
			return true;
		}
		return false;
	}
	
	public void changeUpdateIcon()
	{
		mLauncher.changeUpdateIcon();
	}
	// zhangjin@2015/12/24 ADD END
}
