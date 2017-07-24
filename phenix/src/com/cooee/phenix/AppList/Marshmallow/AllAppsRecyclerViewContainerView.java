package com.cooee.phenix.AppList.Marshmallow;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.BubbleTextView.BubbleTextShadowHandler;
import com.cooee.phenix.ClickShadowView;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.Launcher;


/**
 * A container for RecyclerView to allow for the click shadow view to be shown behind an icon that
 * is launching.
 */
public class AllAppsRecyclerViewContainerView extends FrameLayout implements BubbleTextShadowHandler
{
	
	private final ClickShadowView mTouchFeedbackView;
	
	public AllAppsRecyclerViewContainerView(
			Context context )
	{
		this( context , null );
	}
	
	public AllAppsRecyclerViewContainerView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public AllAppsRecyclerViewContainerView(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
		Launcher launcher = (Launcher)context;
		DeviceProfile grid = launcher.getDeviceProfile();
		mTouchFeedbackView = new ClickShadowView( context );
		// Make the feedback view large enough to hold the blur bitmap.
		int size = grid.getIconHeightSizePx() + mTouchFeedbackView.getExtraSize();
		addView( mTouchFeedbackView , size , size );
	}
	
	@Override
	public void setPressedIcon(
			BubbleTextView icon ,
			Bitmap background )
	{
		if( icon == null || background == null )
		{
			mTouchFeedbackView.setBitmap( null );
			mTouchFeedbackView.animate().cancel();
		}
		else if( mTouchFeedbackView.setBitmap( background ) )
		{
			mTouchFeedbackView.alignWithIconView( icon , (ViewGroup)icon.getParent() );
			mTouchFeedbackView.animateShadow();
		}
		////		 zhangjin@2016/05/12 ADD START
		//				try
		//				{
		//					if( background != null )
		//					{
		//						FileOutputStream fos = new FileOutputStream( Environment.getExternalStorageDirectory().getAbsolutePath() + "//" + icon.getText() + ".png" );
		//						background.compress( Bitmap.CompressFormat.PNG , 90 , fos );
		//						fos.flush();
		//						fos.close();
		//					}
		//				}
		//				catch( Exception e )
		//				{
		//				}
		//		// zhangjin@2016/05/12 ADD END
	}
}
