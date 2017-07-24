package com.cooee.phenix.iconhouse.provider;


import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.theme.ThemeManager;


/**
 * @author zhangjin
 *为图标列表提供支持，比如说提供实时图标，提供切换动画
 */
public abstract class IconHouseProvider
//
implements IOnThemeChanged//zhujieping add  //需求：桌面动态图标支持随主题变化
{
	
	protected UpdateListener mUpdateListener;
	protected ComponentName mTarget;
	protected String themePath = null;//zhujieping add  //需求：桌面动态图标支持随主题变化

	/** 是否需要替换图标*/
	protected boolean mNeedChange = false;
	//zhujieping add start//mBitmap的释放在线程中，mBitmap更新使用是在ui线程，将需要释放的图片保存，等mBitmap在ui线程更新使用后，在ui线程释放，保持同步【c_0004692】
	protected ArrayList<Bitmap> mRecycleBitmap = new ArrayList<Bitmap>();
	protected Bitmap mBitmap = null;
	//zhujieping add end
	
	public ComponentName getTarget()
	{
		return mTarget;
	}
	
	public void setTarget(
			ComponentName target )
	{
		this.mTarget = target;
	}
	
	public static interface UpdateListener
	{
		
		public boolean isCmpVisible(
				ComponentName cmp );
		
		public void onUpdate(
				ComponentName cmp );
	}
	
	public void performUpdate()
	{
		if( mUpdateListener != null )
		{
			mUpdateListener.onUpdate( mTarget );
		}
		//zhujieping add start //mBitmap的释放在线程中，mBitmap更新使用是在ui线程，将需要释放的图片保存，等mBitmap在ui线程更新使用后，在ui线程释放，保持同步【c_0004692】
		for( Bitmap bmp : mRecycleBitmap )
		{
			if( bmp != null && bmp != mBitmap && !bmp.isRecycled() )
			{
				bmp.recycle();
			}
		}
		mRecycleBitmap.clear();
		//zhujieping add end
	}
	
	public UpdateListener getUpdateListener()
	{
		return mUpdateListener;
	}
	
	public void setUpdateListener(
			UpdateListener updateListener )
	{
		this.mUpdateListener = updateListener;
	}
	
	public abstract Bitmap getBitmap();
	
	public boolean isNeedAnim()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public Animator getChangeAnim(
			AnimatorListener animatorListener ,
			AnimatorUpdateListener updateListener )
	{
		return null;
	}
	
	/**
	 * 主动播放动画
	 */
	public void playAnim()
	{
		// TODO Auto-generated method stub
	}
	
	//zhujieping add start //需求：桌面动态图标支持随主题变化
	public Bitmap getCurrentThemeBitmap(
			Resources resources ,
			int resourceId )
	{
		Bitmap bmp = null;
		if( !TextUtils.isEmpty( themePath ) && !ThemeManager.getInstance().currentThemeIsSystemTheme() )
		{
			bmp = ThemeManager.getInstance().getBitmap( themePath + resources.getResourceEntryName( resourceId ) + ".png" );
		}
		if( bmp == null || bmp.isRecycled() )
		{
			bmp = BitmapFactory.decodeResource( resources , resourceId );
		}
		return bmp;
	}
	//zhujieping add end
}
