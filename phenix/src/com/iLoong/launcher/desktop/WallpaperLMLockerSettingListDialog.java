package com.iLoong.launcher.desktop;


import java.util.ArrayList;
import java.util.List;

import com.cooee.phenix.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


public class WallpaperLMLockerSettingListDialog
{
	
	private Context mContext;
	private Dialog mDialog;
	private Button mChooser_cancel;
	private LinearLayout mContentLayout;
	private List<SettingItem> mSettingItems;
	private Display display;
	
	public WallpaperLMLockerSettingListDialog(
			Context context )
	{
		this.mContext = context;
		WindowManager windowManager = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		display = windowManager.getDefaultDisplay();
	}
	
	public WallpaperLMLockerSettingListDialog builder()
	{
		// 获取Dialog布局
		View view = LayoutInflater.from( mContext ).inflate( R.layout.wallpaper_lm_locker_setting_list_layout , null );
		// 设置Dialog最小宽度为屏幕宽度
		view.setMinimumWidth( display.getWidth() );
		// 获取自定义Dialog布局中的控件
		mContentLayout = (LinearLayout)view.findViewById( R.id.lLayout_content );
		mChooser_cancel = (Button)view.findViewById( R.id.wallpaper_chooser_cancel );
		mChooser_cancel.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				mDialog.dismiss();
			}
		} );
		// 定义Dialog布局和参数
		mDialog = new Dialog( mContext , R.style.WallpaperSettingDialogStyle );
		mDialog.setContentView( view );
		Window dialogWindow = mDialog.getWindow();
		dialogWindow.setGravity( Gravity.LEFT | Gravity.BOTTOM );
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes( lp );
		return this;
	}
	
	public WallpaperLMLockerSettingListDialog addSettingItem(
			int titleId ,
			OnWallpaperSettingItemClickListener listener )
	{
		if( mSettingItems == null )
		{
			mSettingItems = new ArrayList<SettingItem>();
		}
		String title = mContext.getString( titleId );
		mSettingItems.add( new SettingItem( title , listener ) );
		return this;
	}
	
	/** 设置条目布局 */
	private void setFunctionItems()
	{
		if( mSettingItems == null || mSettingItems.size() <= 0 )
		{
			return;
		}
		int size = mSettingItems.size();
		Resources resources = mContext.getResources();
		// 循环添加条目
		for( int i = 1 ; i <= size ; i++ )
		{
			SettingItem mSettingItem = mSettingItems.get( i - 1 );
			final OnWallpaperSettingItemClickListener listener = (OnWallpaperSettingItemClickListener)mSettingItem.itemClickListener;
			Button button = new Button( mContext );
			button.setText( mSettingItem.mTitle );
			button.setTextSize( 18 );
			button.setGravity( Gravity.CENTER );
			//背景色
			button.setBackgroundResource( R.drawable.wallpaper_lm_locker_setting_list_item_bg_selector );
			//字体颜色
			button.setTextColor( resources.getColor( R.color.alertdialog_text_color ) );
			//高度
			float scale = resources.getDisplayMetrics().density;
			int height = (int)( 45 * scale + 0.5f );
			button.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT , height ) );
			//点击事件
			button.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					listener.onClick();
					mDialog.dismiss();
				}
			} );
			mContentLayout.addView( button );
			//添加分割线
			if( i != size )
			{
				TextView mHorizontalLine = new TextView( mContext );
				//分割线的高度设为1px即可。
				LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , 1 );
				mHorizontalLine.setBackgroundResource( R.color.alertdialog_text_line_color );
				mContentLayout.addView( mHorizontalLine , layoutParams );
			}
		}
	}
	
	public void show()
	{
		setFunctionItems();
		mDialog.show();
	}
	
	public interface OnWallpaperSettingItemClickListener
	{
		
		void onClick();
	}
	
	public class SettingItem
	{
		
		String mTitle;
		OnWallpaperSettingItemClickListener itemClickListener;
		
		public SettingItem(
				String title ,
				OnWallpaperSettingItemClickListener itemClickListener )
		{
			this.mTitle = title;
			this.itemClickListener = itemClickListener;
		}
	}
}
