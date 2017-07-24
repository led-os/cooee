package com.cooee.phenix;


import android.view.View;


public interface ILauncherTransitionable
{
	
	View getContent();
	
	void onLauncherTransitionPrepare(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace );
	
	void onLauncherTransitionStart(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace );
	
	void onLauncherTransitionStep(
			Launcher l ,
			float t );
	
	void onLauncherTransitionEnd(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace );
}
