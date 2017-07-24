package com.cooee.phenix;


/**
 * Handles scrolling while dragging
 *
 */
public interface DragScroller
{
	
	//cheyingkun start //光感循环切页(德盛伟业)
	//cheyingkun del start
	//	void scrollLeft();
	//	void scrollRight();
	//cheyingkun del end
	//cheyingkun add start
	void scrollLeft(
			boolean isLoop );
	
	void scrollRight(
			boolean isLoop );
	
	//cheyingkun add end
	//cheyingkun end
	/**
	 * The touch point has entered the scroll area; a scroll is imminent.
	 * This event will only occur while a drag is active.
	 *
	 * @param direction The scroll direction
	 */
	boolean onEnterScrollArea(
			int x ,
			int y ,
			int direction );
	
	/**
	 * The touch point has left the scroll area.
	 * NOTE: This may not be called, if a drop occurs inside the scroll area.
	 */
	boolean onExitScrollArea();
}
