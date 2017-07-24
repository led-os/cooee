package com.cooee.favorites.news;


import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;


public class NewsViewPager extends ViewPager
{
	
	private boolean collapse;
	private ArrayList<PullToRefreshLayout> mNewsListsView;
	private boolean isCanScroll = false;
	
	public NewsViewPager(
			Context context ,
			ArrayList<PullToRefreshLayout> newsLists )
	{
		super( context );
		// TODO Auto-generated constructor stub
		mNewsListsView = newsLists;
	}
	
	public boolean hasScrollToHead()
	{
		// TODO Auto-generated method stub
		PullToRefreshLayout layout = getCurrentPage();
		if( layout != null )
		{
			if( layout.isScrollToTop() )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return true;
		}
	}
	
	public void notifyDataSetChanged(
			String categoryId )//categoryId为null，所有listview都更新
	{
		for( PullToRefreshLayout view : mNewsListsView )
		{
			if( categoryId == null || view.getCategoryId().equals( categoryId ) )
			{
				view.notifyDataSetChanged();
			}
		}
	}
	
	public void endFetch(
			String categoryId )
	{
		if( categoryId == null )
		{
			return;
		}
		for( PullToRefreshLayout view : mNewsListsView )
		{
			if( view.getCategoryId().equals( categoryId ) )
			{
				view.loadmoreFinish( PullToRefreshLayout.SUCCEED );
				break;
			}
		}
	}
	
	public void endRefresh(
			String categoryId )
	{
		if( categoryId == null )
		{
			return;
		}
		for( PullToRefreshLayout view : mNewsListsView )
		{
			if( view.getCategoryId().equals( categoryId ) )
			{
				view.refreshFinish( PullToRefreshLayout.SUCCEED );
				break;
			}
		}
	}
	
	public boolean startRefresh(
			String categoryId )
	{
		if( categoryId == null )
		{
			return false;
		}
		Log.v( "news" , "startRefresh = " + categoryId );
		for( PullToRefreshLayout view : mNewsListsView )
		{
			if( view.getCategoryId().equals( categoryId ) )
			{
				view.startRefresh();
				return true;
			}
		}
		return false;
	}
	
	public boolean isCurrentPage(
			String categoryId )
	{
		PullToRefreshLayout currentPage = getCurrentPage();
		if( currentPage != null && currentPage.getCategoryId().equals( categoryId ) )
		{
			return true;
		}
		return false;
	}
	
	public PullToRefreshLayout getCurrentPage()
	{
		int index = getCurrentItem();
		if( index < 0 || index >= mNewsListsView.size() )
			return null;
		return mNewsListsView.get( index );
	}
	
	public void moveCurrentPageToTop()
	{
		PullToRefreshLayout currentPage = getCurrentPage();
		if( currentPage != null )
		{
			currentPage.moveListViewToTop();
		}
	}
	
	public void setScanScroll(
			boolean isCanScroll )
	{
		this.isCanScroll = isCanScroll;
	}
	
	@Override
	public boolean onTouchEvent(
			MotionEvent arg0 )
	{
		// TODO Auto-generated method stub
		if( isCanScroll )
		{
			return super.onTouchEvent( arg0 );
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent arg0 )
	{
		// TODO Auto-generated method stub
		if( isCanScroll )
		{
			return super.onInterceptTouchEvent( arg0 );
		}
		else
		{
			return false;
		}
	}
}
