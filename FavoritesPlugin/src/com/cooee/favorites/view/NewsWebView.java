/* Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License. */
package com.cooee.favorites.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.cooee.favorites.R;


/**
 * Custom WebView subclass that enables us to capture events needed for Cordova.
 */
public class NewsWebView extends WebView
{
	
	private GestureDetector mGestureDetector;
	private onScrollListener mListener;
	private float overPosition = 0.15f;
	
	public NewsWebView(
			Context context )
	{
		this( context , null );
	}
	
	public NewsWebView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		mGestureDetector = new GestureDetector( getContext() , new GestureListener() );
		overPosition = context.getResources().getInteger( R.integer.ad_show_postion_in_newsactivity ) / 100.0f;
	}
	
	@Override
	public boolean dispatchTouchEvent(
			MotionEvent ev )
	{
		// TODO Auto-generated method stub
		mGestureDetector.onTouchEvent( ev );
		return super.dispatchTouchEvent( ev );
	}
	
	public void setListener(
			onScrollListener listener )
	{
		this.mListener = listener;
	}
	
	@Override
	protected void onScrollChanged(
			int l ,
			int t ,
			int oldl ,
			int oldt )
	{
		// TODO Auto-generated method stub
		super.onScrollChanged( l , t , oldl , oldt );
		if( mListener != null )
		{
			if( isOutsideArae() )
			{
				mListener.outsideScroll();
			}
			else
			{
				mListener.insideScroll();
			}
		}
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		
		@Override
		public boolean onFling(
				MotionEvent e1 ,
				MotionEvent e2 ,
				float velocityX ,
				float velocityY )
		{
			// TODO Auto-generated method stub
			float deltaX = e2.getX() - e1.getX();
			if( deltaX > getWidth() / 3 )
			{
				if( mListener != null )
				{
					mListener.flingListener();
				}
			}
			return super.onFling( e1 , e2 , velocityX , velocityY );
		}
	}
	
	public boolean isOutsideArae()
	{
		if( ( getScrollY() > getContentHeight() * getScale() * overPosition ) || ( getScrollY() + getHeight() == getContentHeight() * getScale() ) )
		{
			return true;
		}
		return false;
	}
	
	public interface onScrollListener
	{
		
		public void flingListener();
		
		public void outsideScroll();
		
		public void insideScroll();
	}
}
