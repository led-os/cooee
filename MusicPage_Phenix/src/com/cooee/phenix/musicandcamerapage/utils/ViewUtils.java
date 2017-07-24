package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicpage.CooeeImageView;
import com.cooee.phenix.musicpage.R;


public class ViewUtils
{
	
	private static final String TAG = "ViewUtils";
	
	public static int getTextHeight(
			TextView view )
	{
		FontMetrics fm = view.getPaint().getFontMetrics();
		float textHeight = ( Math.abs( fm.bottom ) + Math.abs( fm.top ) );
		fm = null;
		return (int)textHeight;
	}
	
	public static void setViewTopMargin(
			View view ,
			int topMargin )
	{
		LayoutParams params = view.getLayoutParams();
		if( params instanceof RelativeLayout.LayoutParams )
		{
			( (RelativeLayout.LayoutParams)params ).topMargin = topMargin;
		}
		else if( params instanceof LinearLayout.LayoutParams )
		{
			( (LinearLayout.LayoutParams)params ).topMargin = topMargin;
		}
		else if( params instanceof FrameLayout.LayoutParams )
		{
			( (FrameLayout.LayoutParams)params ).topMargin = topMargin;
		}
		view.setLayoutParams( params );
		params = null;
	}
	
	public static int getMarginTop(
			View view )
	{
		LayoutParams params = view.getLayoutParams();
		if( params instanceof RelativeLayout.LayoutParams )
		{
			return ( (RelativeLayout.LayoutParams)params ).topMargin;
		}
		else if( params instanceof LinearLayout.LayoutParams )
		{
			return ( (LinearLayout.LayoutParams)params ).topMargin;
		}
		else if( params instanceof FrameLayout.LayoutParams )
		{
			return ( (FrameLayout.LayoutParams)params ).topMargin;
		}
		return 0;
	}
	
	public static void setRotation(
			View view ,
			float rotation )
	{
		float oldRotation = view.getRotation();
		if( oldRotation != rotation )
			view.setRotation( rotation );
	}
	
	public static void setTranslationY(
			View view ,
			float translationY )
	{
		if( view.getTranslationY() != translationY )
			view.setTranslationY( translationY );
	}
	
	public static void setAlpha(
			View view ,
			float alpha )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "alpha:" , alpha ) );
		if( view.getAlpha() != alpha )
		{
			view.setAlpha( alpha );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "view:" + view , "-alpha:" , alpha ) );
			//	ViewUtils.printStackTrace( "lucky" );
		}
	}
	
	public static void clearAnimation(
			View view )
	{
		if( view.getAnimation() != null )
			view.clearAnimation();
	}
	
	public static void setVisibility(
			View view ,
			int visibility )
	{
		if( view.getVisibility() != visibility )
			view.setVisibility( visibility );
	}
	
	public static void setImageResourceByTag(
			CooeeImageView view ,
			boolean up )
	{
		Object tag = view.getTag();
		if( tag == null )
		{
			if( up )
				view.setImageResource( R.drawable.music_page_album_up );
			else
				view.setImageResource( R.drawable.music_page_album_down );
		}
		else if( tag instanceof BitmapDrawable[] )
		{
			BitmapDrawable[] bgs = (BitmapDrawable[])tag;
			if( up )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , StringUtils.concat( "tag instanceof BitmapDrawable[] view.hasFocus():" , view.hasFocus() , "-view.isVisble:" , view.getVisibility() ) );
				view.setImageDrawable( bgs[0] );
			}
			else
			{
				view.setImageDrawable( bgs[1] );
			}
		}
	}
	
	public static void addView(
			ViewGroup parent ,
			View child ,
			int index )
	{
		if( parent != null && child != null )
		{
			ViewParent childParent = child.getParent();
			if( childParent == null )
			{
				parent.addView( child , index );
			}
			else if( childParent == parent )
			{
				if( parent.getChildAt( index ) != child )
				{
					parent.removeView( child );
					parent.addView( child , index );
				}
			}
		}
	}
	
	public static void removeView(
			ViewGroup parent ,
			View child )
	{
		if( parent != null && child != null )
		{
			ViewParent childParent = child.getParent();
			if( childParent == parent )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( "andy" , "child =  " + child );
				parent.removeView( child );
			}
		}
	}
	
	/**
	 * 打印方法调用堆栈，方便调试
	 * @param tag
	 */
	public static void printStackTrace(
			String tag )
	{
		String info = null;
		ByteArrayOutputStream baos = null;
		PrintStream printStream = null;
		try
		{
			baos = new ByteArrayOutputStream();
			printStream = new PrintStream( baos );
			new Throwable().printStackTrace( printStream );
			byte[] data = baos.toByteArray();
			info = new String( data );
			data = null;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		long threadId = Thread.currentThread().getId();
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( tag , StringUtils.concat( "Thread.getName()=" , Thread.currentThread().getName() , " id=" , threadId , " state=" + Thread.currentThread().getState() ) );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( tag , StringUtils.concat( "Stack[" , info , "]" ) );
	}
	
	public static int dp2px(
			Context context ,
			int dpValue )
	{
		return (int)context.getResources().getDisplayMetrics().density * dpValue;
	}
}
