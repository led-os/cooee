package com.cooee.phenix;


import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;


/**
 * Specific {@link AppWidgetHost} that creates our {@link LauncherAppWidgetHostView}
 * which correctly captures all long-press events. This ensures that users can
 * always pick up and move widgets.
 */
public class LauncherAppWidgetHost extends AppWidgetHost
{
	
	Launcher mLauncher;
	
	public LauncherAppWidgetHost(
			Launcher launcher ,
			int hostId )
	{
		super( launcher , hostId );
		mLauncher = launcher;
	}
	
	@Override
	protected AppWidgetHostView onCreateView(
			Context context ,
			int appWidgetId ,
			AppWidgetProviderInfo appWidget )
	{
		return new LauncherAppWidgetHostView( context );
	}
	
	@Override
	public void stopListening()
	{
		super.stopListening();
		clearViews();
	}
	
	protected void onProvidersChanged()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// Once we get the message that widget packages are updated, we need to rebind items
				// in AppsCustomize accordingly.
				mLauncher.bindPackagesUpdated( LauncherModel.getSortedWidgetsAndShortcuts( mLauncher ) );
			}
		} ).start();
	}
}
