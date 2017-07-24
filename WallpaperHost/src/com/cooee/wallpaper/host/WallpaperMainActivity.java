package com.cooee.wallpaper.host;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.cooee.wallpaper.host.util.SystemBarTintManager;
import com.cooee.wallpaper.wrap.DynamicImageView;
import com.cooee.wallpaper.wrap.WallpaperConfigString;
import com.umeng.analytics.MobclickAgent;


public class WallpaperMainActivity extends Activity
{
	
	FrameLayout mParent;
	DynamicImageView mImageView;
	private final int MSG_START_CHANGE_WALLPPAPER = 0;
	private final int MSG_FINISH_ACTIVITY_WAIT_ANIM_FINISH = 1;
	private WallpaperHost mHost;
	public final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 0x00000100;//View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	public final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 0x00000400;//View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	public final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 0x00000200;//View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	public final int FLAG_TRANSLUCENT_STATUS = 0x04000000;// WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
	public final int FLAG_TRANSLUCENT_NAVIGATION = 0x08000000;//WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
	private boolean isFirst = true;
	private Handler mHandler = new Handler() {
		
		public void handleMessage(
				android.os.Message msg )
		{
			if( msg.what == MSG_START_CHANGE_WALLPPAPER )
			{
				start( getIntent() );
			}
			else if( msg.what == MSG_FINISH_ACTIVITY_WAIT_ANIM_FINISH )
			{
				finish();
			}
		};
	};
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		setContentView( R.layout.layout_main );
		overridePendingTransition( 0 , 0 );
	}
	
	private void start(
			Intent intent )
	{
		if( mImageView == null )
		{
			mParent = (FrameLayout)findViewById( R.id.view_parent );
			mImageView = new DynamicImageView( this );
			mImageView.setImageDrawable( getResources().getDrawable( R.drawable.onekeychangewallpaper ) );
			int iconSize = intent.getIntExtra( WallpaperConfigString.LAUNCHER_ICON_SIZEPX , (int)getResources().getDimension( R.dimen.icon_size ) );
			int textSize = (int)intent.getFloatExtra( WallpaperConfigString.LAUNCHER_ICON_TEXT_SIZE , getResources().getDimension( R.dimen.text_size ) );
			int padding = intent.getIntExtra( WallpaperConfigString.LAUNCHER_ICON_TEXT_PADDING , 0 );
			Paint paint = new Paint();
			paint.setTextSize( textSize );
			FontMetrics metrics = paint.getFontMetrics();
			int textHeight = (int)( metrics.descent - metrics.ascent );
			Rect rect = intent.getSourceBounds();
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( iconSize , iconSize );//mImageView.getLayoutParams();
			int paddingTop = intent.getIntExtra( "paddingTop" , -1 );
			//			params.width = params.height = iconSize;
			if( rect != null )
			{
				params.leftMargin = rect.left + ( rect.width() - iconSize ) / 2;
				params.topMargin = rect.top + ( paddingTop != -1 ? paddingTop : ( rect.height() - iconSize - textHeight - padding ) / 2 ) - ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? 0 : getStatusBarHeight() );//因为从桌面传过来的坐标是相对屏幕的，这边要考虑是否是全屏，设置状态栏透明的方法是根据sdk的版本来设置的
			}
			else
			{
				params.gravity = Gravity.CENTER;
			}
			mParent.addView( mImageView , params );
			mImageView.playAnim();
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					WallpaperHostManager.getInstance( WallpaperMainActivity.this ).init( null );//config都放到这个里面
					mHost = WallpaperHost.getInstance( WallpaperMainActivity.this , WallpaperMainActivity.this );
					//这段会在onresume中被调用，放到这边是为了判断umeng开关
					if( mHost.getConfig().getBoolean( WallpaperConfigString.ENABLE_UMENG , true ) )
						MobclickAgent.onResume( WallpaperMainActivity.this );
					runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							mHost.start( WallpaperMainActivity.this , mParent , mImageView );
						}
					} );
				}
			} ).start();
		}
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		if( isFirst )
		{
			isFirst = false;
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )//sdk大于19才有效
				transStausBar();
			mHandler.sendEmptyMessageDelayed( MSG_START_CHANGE_WALLPPAPER , 40 );//这个延时执行，是为了透明的父更快显示出来，降低点击后对桌面的操作时间
		}
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		if( mHost != null && mHost.getConfig() != null && mHost.getConfig().getBoolean( WallpaperConfigString.ENABLE_UMENG , true ) )
			MobclickAgent.onPause( this );
	}
	
	public int getStatusBarHeight()
	{
		int result = 0;
		int resourceId = getResources().getIdentifier( "status_bar_height" , "dimen" , "android" );
		if( resourceId > 0 )
		{
			result = getResources().getDimensionPixelSize( resourceId );
		}
		return result;
	}
	
	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		if( mImageView != null && mImageView.getVisibility() == View.VISIBLE )//等动画结束才关掉activity
		{
			mImageView.stopAnim();
			mHandler.sendEmptyMessageDelayed( MSG_FINISH_ACTIVITY_WAIT_ANIM_FINISH , 40 );
			return;
		}
		super.finish();
		overridePendingTransition( 0 , 0 );
	}
	
	@Override
	protected void onNewIntent(
			//按home键关闭后，点击图标后，重新开始换壁纸
			Intent intent )
	{
		// TODO Auto-generated method stub
		super.onNewIntent( intent );
		overridePendingTransition( 0 , 0 );
		if( mImageView == null || mImageView.getVisibility() != View.VISIBLE )//表示上一次换壁纸完成，否则直接显示上次的view
		{
			if( mParent != null )
				mParent.removeAllViews();
			if( mImageView != null )
			{
				mImageView.setVisibility( View.GONE );
				mImageView = null;
			}
			start( intent );
		}
	}
	
	@Override
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		if( mHost != null )
		{
			mHost.onBackPressed();
		}
		else
		{
			super.onBackPressed();
		}
	}
	
	public void transStausBar()
	{
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
		{
			setTranslucentStatus( true );
			SystemBarTintManager tintManager = new SystemBarTintManager( this );
			tintManager.setStatusBarTintEnabled( true );
			tintManager.setStatusBarTintColor( 0x00000000 );
			//			tintManager.setStatusBarTintResource( mCurrColor );//通知栏所需颜色
		}
	}
	
	@TargetApi( 19 )
	private void setTranslucentStatus(
			boolean on )
	{
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if( on )
		{
			winParams.flags |= bits;
		}
		else
		{
			winParams.flags &= ~bits;
		}
		win.setAttributes( winParams );
	}
}
