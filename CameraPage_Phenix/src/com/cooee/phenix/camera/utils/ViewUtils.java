package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ViewUtils
{
	
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
			( (RelativeLayout.LayoutParams)params ).topMargin += topMargin;
		}
		else if( params instanceof LinearLayout.LayoutParams )
		{
			( (LinearLayout.LayoutParams)params ).topMargin += topMargin;
		}
		else if( params instanceof FrameLayout.LayoutParams )
		{
			( (FrameLayout.LayoutParams)params ).topMargin += topMargin;
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
	
	public static void setBackgroundByTag(
			View view ,
			boolean up )
	{
		Object tag = view.getTag();
		if( tag == null )
		{
			//			if( up )
			//				view.setBackgroundResource( R.drawable.camera_page_edit_button_focus_shape );
			//			else
			//				view.setBackgroundResource( R.drawable.camera_page_edit_button_normal_shape );
		}
		else if( tag instanceof BitmapDrawable[] )
		{
			BitmapDrawable[] bgs = (BitmapDrawable[])tag;
			if( up )
				view.setBackgroundDrawable( bgs[0] );
			else
				view.setBackgroundDrawable( bgs[1] );
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
				parent.removeView( child );
			}
		}
	}
	
	public static int dp2px(
			Context context ,
			int dpValue )
	{
		return (int)context.getResources().getDisplayMetrics().density * dpValue;
	}
}
