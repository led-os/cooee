package com.cooee.phenix.AppList.KitKat;


import android.view.MotionEvent;
import android.view.View;

import com.cooee.phenix.Launcher;


public interface AppsView
{
	
	public void reset();
	
	public boolean shouldContainerScroll(
			MotionEvent ev );
	
	public void onLauncherTransitionPrepare(
			Launcher l ,
			boolean animated ,
			boolean toWorkspace );
	
	public void setAlpha(
			float alpha );
	
	public void setTranslationY(
			float translationY );
	
	public float getTranslationY();
	
	public void setTranslationX(
			float translationY );
	
	public float getTranslationX();
	
	public void setVisibility(
			int visibility );
	
	public void setScaleX(
			float scaleX );
	
	public void setScaleY(
			float scaleY );
	
	public boolean requestFocus();
	
	public boolean post(
			Runnable action );
	
	public View getContentView();
	
	public void preparePull();
	
	public void startAppsSearch();
	
	public int getVisibility();
}
