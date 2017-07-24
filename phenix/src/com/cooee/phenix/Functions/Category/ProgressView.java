package com.cooee.phenix.Functions.Category;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class ProgressView
{
	
	private ProgressBar mProgressBar;
	private TextView mStateTextView;
	private View mProgressView;
	private Context context;
	
	public ProgressView(
			Context context )
	{
		super();
		this.context = context;
		initProgressViewData();
	}
	
	/**
	 * 填充布局,找控件
	 */
	private void initProgressViewData()
	{
		if( mProgressView == null )
		{
			mProgressView = LayoutInflater.from( context ).inflate( R.layout.default_loading_progress , null );
			mProgressBar = (ProgressBar)mProgressView.findViewById( R.id.startLoader_progressBar );
			mStateTextView = (TextView)mProgressView.findViewById( R.id.startLoader_state );
			mProgressView.setOnTouchListener( new OnTouchListener() {
				
				@Override
				public boolean onTouch(
						View v ,
						MotionEvent event )
				{
					return true;
				}
			} );
		}
	}
	
	/**
	 * 设置加载的进度状态
	 * @param startLoad_stateName 状态的名称
	 */
	public void setLoadState(
			String mLoadStateName )
	{
		if( mStateTextView != null )
		{
			mStateTextView.setText( mLoadStateName );
		}
	}
	
	//智能分类添加运营 , change by shlt@2014/12/25 ADD START
	public void setLoadState(
			int mLoadStateNameId )
	{
		if( mLoadStateNameId != 0 )
		{
			mStateTextView.setText( mLoadStateNameId );
			// zhangjin@2016/05/11 ADD START
			if(
			//
			LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_MARSHMALLOW
			//
			|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE == LauncherDefaultConfig.APPLIST_SYTLE_NOUGAT/* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
			//
			)
			{
				mStateTextView.setTextColor( Color.BLACK );
			}
			// zhangjin@2016/05/11 ADD END
		}
	}
	//智能分类添加运营 , change by shlt@2014/12/25 ADD END
	;
	
	public View getProgressView()
	{
		return mProgressView;
	}
}
