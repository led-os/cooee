// xiatian add whole file //安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
package com.cooee.phenix.AppList.Marshmallow;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.os.Handler;

import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.util.ComponentKey;


/**
 * The default search implementation.
 */
public class DefaultAppSearchAlgorithm
{
	
	private static final Pattern SPLIT_PATTERN = Pattern.compile( "[\\s|\\p{javaSpaceChar}]+" );
	private final List<AppInfo> mApps;
	protected final Handler mResultHandler;
	
	public DefaultAppSearchAlgorithm(
			List<AppInfo> apps )
	{
		mApps = apps;
		mResultHandler = new Handler();
	}
	
	public void cancel(
			boolean interruptActiveRequests )
	{
		if( interruptActiveRequests )
		{
			mResultHandler.removeCallbacksAndMessages( null );
		}
	}
	
	public void doSearch(
			final String query ,
			final AllAppsSearchBarController.Callbacks callback )
	{
		final ArrayList<ComponentKey> result = getTitleMatchResult( query );
		mResultHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				callback.onSearchResult( query , result );
			}
		} );
	}
	
	protected ArrayList<ComponentKey> getTitleMatchResult(
			String query )
	{
		// Do an intersection of the words in the query and each title, and filter out all the
		// apps that don't match all of the words in the query.
		final String queryTextLower = query.toLowerCase();
		final String[] queryWords = SPLIT_PATTERN.split( queryTextLower );
		final ArrayList<ComponentKey> result = new ArrayList<ComponentKey>();
		for( AppInfo info : mApps )
		{
			if( matches( info , queryWords ) )
			{
				result.add( info.toComponentKey() );
			}
		}
		return result;
	}
	
	protected boolean matches(
			AppInfo info ,
			String[] queryWords )
	{
		String title = info.getTitle().toString();
		String[] words = SPLIT_PATTERN.split( title.toLowerCase() );
		for( int qi = 0 ; qi < queryWords.length ; qi++ )
		{
			boolean foundMatch = false;
			for( int i = 0 ; i < words.length ; i++ )
			{
				if( words[i].startsWith( queryWords[qi] ) )
				{
					foundMatch = true;
					break;
				}
			}
			if( !foundMatch )
			{
				// If there is a word in the query that does not match any words in this
				// title, so skip it.
				return false;
			}
		}
		return true;
	}
}
