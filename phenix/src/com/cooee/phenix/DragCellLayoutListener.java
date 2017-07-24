package com.cooee.phenix;


import android.view.MotionEvent;

public interface DragCellLayoutListener
{
	
	
	public boolean isInDeleteDropTarget(
			MotionEvent ev );
	
	public void onDropDeleteDropTarget();
}
