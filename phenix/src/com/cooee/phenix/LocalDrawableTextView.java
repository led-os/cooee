/* Copyright (C) 2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License. */
package com.cooee.phenix;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.theme.ThemeManager;
import com.cooee.util.Tools;


/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class LocalDrawableTextView extends TextView
{
	
	
	public LocalDrawableTextView(
			Context context )
	{
		this( context , null , 0 );
	}
	
	public LocalDrawableTextView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public LocalDrawableTextView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		if( TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH ) )
		{
			return;
		}
		final Resources.Theme theme = context.getTheme();
		int drawableLeft = -1 , drawableTop = -1 , drawableRight = -1 , drawableBottom = -1 , drawableStart = -1 , drawableEnd = -1;
		TypedArray a = theme.obtainStyledAttributes( attrs , com.android.internal.R.styleable.TextView , defStyle , 0 );
		int n = a.getIndexCount();
		for( int i = 0 ; i < n ; i++ )
		{
			int attr = a.getIndex( i );
			if( attr  == com.android.internal.R.styleable.TextView_drawableLeft)
			{
				drawableLeft = a.getResourceId( attr , -1 );
			}
			else if( attr  ==  com.android.internal.R.styleable.TextView_drawableTop)
			{
				drawableTop = a.getResourceId( attr , -1 );
			}
			else if( attr  ==  com.android.internal.R.styleable.TextView_drawableRight)
			{
				drawableRight = a.getResourceId( attr , -1 );
			}
			else if( attr  ==  com.android.internal.R.styleable.TextView_drawableBottom)
			{
				drawableBottom = a.getResourceId( attr , -1 );
			}
			else if( Build.VERSION.SDK_INT >= 17 && attr  ==  com.android.internal.R.styleable.TextView_drawableStart)
			{
				drawableStart = a.getResourceId( attr , -1 );
			}
			else if(Build.VERSION.SDK_INT >= 17 && attr  == com.android.internal.R.styleable.TextView_drawableEnd)
			{
				drawableEnd = a.getResourceId( attr , -1 );
			}
		}
		a.recycle();
		if( drawableLeft > 0 || drawableTop > 0 || drawableRight > 0 || drawableBottom > 0 || drawableStart > 0 || drawableEnd > 0 )
		{
			Drawable left = getDrawbleFromLocal( context , drawableLeft );
			Drawable top = getDrawbleFromLocal( context , drawableTop );
			Drawable right = getDrawbleFromLocal( context , drawableRight );
			Drawable bottom = getDrawbleFromLocal( context , drawableBottom );
			if( Build.VERSION.SDK_INT >= 17 )
			{
				Drawable start = drawableStart > 0 ? getDrawbleFromLocal( context , drawableStart ) : left;
				Drawable end = drawableEnd > 0 ? getDrawbleFromLocal( context , drawableEnd ) : right;
				if( top != null || start != null || end != null || bottom != null )
					setCompoundDrawablesRelativeWithIntrinsicBounds( start , top , end , bottom );
			}
			else
			{
				if( left != null || top != null || right != null || bottom != null )
					setCompoundDrawablesWithIntrinsicBounds( left , top , right , bottom );
			}
		}


	}
	
	public Drawable getDrawbleFromLocal(
			Context context ,
			int drawableId )
	{
		if( drawableId <= 0 )
		{
			return null;
		}
		String name = context.getResources().getResourceEntryName( drawableId );
		Drawable drawable = null;
		Drawable currentDrawable = context.getResources().getDrawable( drawableId );
		if( name.endsWith( "selector" ) )
		{
			name = name.replace( "selector" , "" );
			Bitmap bmp = ThemeManager.getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/" , name , "icon_normal.png" ) );
			if( bmp == null )
			{
				return null;
			}
			bmp = Tools.resizeBitmap( bmp , currentDrawable.getIntrinsicWidth() , currentDrawable.getIntrinsicHeight() );
			FastBitmapDrawable normal = new FastBitmapDrawable( bmp );
			bmp = ThemeManager.getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/" , name , "icon_focus.png" ) );
			Drawable focus = null;
			if( bmp != null )
			{
				bmp = Tools.resizeBitmap( bmp , currentDrawable.getIntrinsicWidth() , currentDrawable.getIntrinsicHeight() );
				focus = new FastBitmapDrawable( bmp );
			}
			drawable = new StateListDrawable();
			if( focus != null )
			{
				( (StateListDrawable)drawable ).addState( new int[]{ android.R.attr.state_pressed , android.R.attr.state_enabled } , focus );
				( (StateListDrawable)drawable ).addState( new int[]{ android.R.attr.state_focused , android.R.attr.state_enabled } , focus );
			}
			( (StateListDrawable)drawable ).addState( new int[]{} , normal );
		}
		else
		{
			Bitmap bmp = ThemeManager.getBitmapFromLocal( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_THEME_PATH , "/theme/" , name , ".png" ) );
			if( bmp != null )
			{
				bmp = Tools.resizeBitmap( bmp , currentDrawable.getIntrinsicWidth() , currentDrawable.getIntrinsicHeight() );
				drawable = new FastBitmapDrawable( bmp );
			}
		}
		return drawable;
	}
}
