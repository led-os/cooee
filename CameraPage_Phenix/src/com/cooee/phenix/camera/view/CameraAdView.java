package com.cooee.phenix.camera.view;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.camera.R;
import com.cooee.phenix.camera.control.AdManager;
import com.cooee.phenix.camera.inte.IAdDisplayer;
import com.cooee.phenix.kmob.ad.KmobAdData;
import com.cooee.phenix.kmob.ad.KmobUtil;
import com.kmob.kmobsdk.KmobManager;

import cool.sdk.KmobConfig.KmobConfigData;


public class CameraAdView extends FrameLayout implements IAdDisplayer
{
	
	private static final String TAG = "CameraAdView";
	/**广告图片View*/
	private ImageView vAdImage = null;
	/**指示器*/
	private ViewGroup vIndicatorGroup = null;
	/**广告已经展示的次数*/
	private long mTimesAdShown = 0;
	/**当前正在轮播的广告数据数组*/
	private List<KmobAdData> mCurAdInfoList = null;
	/**新获取到的广告数据数组*/
	private List<KmobAdData> mNewAdInfoList = null;
	/**当前正在展示的广告的index*/
	private int mCurAdIndex = -1;
	/**更换广告图片的runnable*/
	private ChangeAdRunnable mChangeAdRunnable = new ChangeAdRunnable();
	/**更新广告图片的handler*/
	private Handler mHandler = new Handler();
	/**更换图片的时间间隔*/
	private int change_ad_time_span = 3000;
	/**上下文*/
	private Context mContext = null;
	/**touchDown时的X轴坐标*/
	private int downX = -1;
	/**手指向右滑动,即滑动到前一张*/
	private boolean moveToRight = false;
	/**是否能够响应点击*/
	private boolean canClick = true;
	/**保存的日期,用于和当前日期比较是否为同一天*/
	private String mLastDate = null;
	/**当前日期格式化的format*/
	private SimpleDateFormat mCurDateFormat = null;
	/**SharedPreferences文件名*/
	private final static String SHARED_PREF_NAME = "camera_page_phenix";
	/**日期字段的key*/
	private final static String DATE_KEY = "last_date";
	
	public CameraAdView(
			Context context )
	{
		super( context );
		if( isInEditMode() )
			return;
		this.mContext = context;
		initDateUtil();
		initAdLayout( context );
	}
	
	public CameraAdView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		if( isInEditMode() )
			return;
		this.mContext = context;
		initDateUtil();
		initAdLayout( context );
	}
	
	public CameraAdView(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
		if( isInEditMode() )
			return;
		this.mContext = context;
		initDateUtil();
		initAdLayout( context );
	}
	
	/**
	 * 初始化日期检查时所需要的对象
	 * @author yangtianyu 2016-8-9
	 * i_0014123
	 */
	private void initDateUtil()
	{
		mCurDateFormat = new SimpleDateFormat( "yyyyMMdd" );
		SharedPreferences sharedPref = mContext.getSharedPreferences( SHARED_PREF_NAME , Context.MODE_PRIVATE );
		mLastDate = sharedPref.getString( DATE_KEY , mCurDateFormat.format( Calendar.getInstance().getTime() ) );
	}
	
	/**
	 * 初始化广告界面
	 * @param context
	 * @author yangtianyu 2016-8-9
	 */
	private void initAdLayout(
			Context context )
	{
		LayoutInflater mInflater = LayoutInflater.from( context );
		ViewGroup adViewGroup = (ViewGroup)mInflater.inflate( R.layout.camera_page_view_ad_view , null );
		vAdImage = (ImageView)adViewGroup.findViewById( R.id.camera_page_ad_view );
		vIndicatorGroup = (ViewGroup)adViewGroup.findViewById( R.id.camera_page_ad_indicator_layout );
		addView( adViewGroup );
	}
	
	/**
	 * 检查日期是否出现变化,日期变化后保存该日期并将展示次数清空
	 * @author yangtianyu 2016-8-9
	 * i_0014123
	 */
	private void checkDateForTimes()
	{
		String curDate = mCurDateFormat.format( Calendar.getInstance().getTime() );
		if( !mLastDate.equals( curDate ) )
		{
			CameraView.logI( StringUtils.concat( TAG , "datechanged  curDate:" , curDate , "-mLastDate:" , mLastDate ) );
			mTimesAdShown = 0;
			SharedPreferences sharedPref = mContext.getSharedPreferences( SHARED_PREF_NAME , Context.MODE_PRIVATE );
			Editor editor = sharedPref.edit();
			editor.putString( DATE_KEY , curDate );
			editor.commit();
			mLastDate = curDate;
		}
	}
	
	@Override
	public boolean onTouchEvent(
			android.view.MotionEvent event )
	{
		switch( event.getAction() )
		{
			case MotionEvent.ACTION_DOWN:
				// YANGTIANYU@2016/08/19 ADD START
				// 只显示一张图片时不记录按下的位置,不屏蔽桌面切页,也不进行滑动判断
				boolean enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.CAMERA_ADPLACE_ID , mTimesAdShown );
				if( enableShowAd && mCurAdInfoList.size() > 1 )
				{
					// YANGTIANYU@2016/08/19 ADD END
					getParent().requestDisallowInterceptTouchEvent( true );
					downX = (int)event.getX();
				}
				break;
			case MotionEvent.ACTION_UP:
				// YANGTIANYU@2016/08/19 ADD START
				// 不进行广告滑动判断时,允许点击
				if( downX < 0 )
				{
					canClick = true;
					break;
				}
				// YANGTIANYU@2016/08/19 ADD END
				int deltX = (int)( event.getX() - downX );
				downX = -1;
				if( Math.abs( deltX ) > 10 && mCurAdInfoList.size() > 1 )
				{
					if( deltX > 0 )
						moveToRight = true;
					postNewRunnable( 0 );
					canClick = false;
				}
				else
				{
					canClick = true;
				}
				break;
			default:
				break;
		}
		return super.onTouchEvent( event );
	}
	
	@Override
	public boolean isShowing()
	{
		return mCurAdInfoList != null && !mCurAdInfoList.isEmpty();
	}
	
	@Override
	public long getTimesAdShown()
	{
		// YANGTIANYU@2016/08/09 ADD START
		// i_0014123
		checkDateForTimes();
		// YANGTIANYU@2016/08/09 ADD END
		return mTimesAdShown;
	}
	
	@Override
	public void addAdItem(
			KmobAdData newAdItem )
	{
		if( mCurAdInfoList == null )
			mCurAdInfoList = new ArrayList<KmobAdData>();
		if( newAdItem == null || mCurAdInfoList.size() >= AdManager.SUMS )
			return;
		mCurAdInfoList.add( newAdItem );
		int adSize = mCurAdInfoList.size();
		if( adSize == 1 )
		{
			postNewRunnable( 0 );
		}
		if( adSize >= 2 )
		{
			vIndicatorGroup.setVisibility( VISIBLE );
			ImageView indicator;
			for( int i = 0 ; i < adSize ; i++ )
			{
				indicator = (ImageView)vIndicatorGroup.getChildAt( i );
				if( indicator != null )
				{
					indicator.setImageResource( mCurAdIndex == i ? R.drawable.camera_page_ad_selected : R.drawable.camera_page_ad_unselected );
					indicator.setVisibility( VISIBLE );
				}
			}
		}
	}
	
	@Override
	public void notifyAdChanged(
			List<KmobAdData> newAdList )
	{
		mNewAdInfoList = newAdList;
	}
	
	@Override
	public void hideAdView()
	{
		mHandler.removeCallbacks( mChangeAdRunnable );
	}
	
	@Override
	public void showAdView()
	{
		if( mChangeAdRunnable == null )
		{
			mChangeAdRunnable = new ChangeAdRunnable();
		}
		mHandler.postDelayed( mChangeAdRunnable , change_ad_time_span );
		// YANGTIANYU@2016/08/18 ADD START
		// 从其他页面滑动到相机页或者关闭预览时,如果此时可以展示广告且存在广告,则展示次数加1
		// i_0014332 当只获取到一张广告时，广告会一直展示不消失.
		if( mCurAdInfoList != null && !mCurAdInfoList.isEmpty() )
		{
			mTimesAdShown++;
			CameraView.logI( StringUtils.concat( TAG , ",changeAdImage mTimesAdShown:" , mTimesAdShown ) );
		}
		// YANGTIANYU@2016/08/18 ADD END
	}
	
	@Override
	public void onClick()
	{
		if( !canClick )
			return;
		KmobAdData mCurDisplayAd = mCurAdInfoList.get( mCurAdIndex );
		if( mCurDisplayAd != null )
		{
			KmobManager.onClickDone( mCurDisplayAd.getAdid() , mCurDisplayAd.getOtherInfo() , true );
		}
	}
	
	@Override
	public void dispose()
	{
		vAdImage.setImageResource( R.drawable.camera_page_view_bg );
		clearCurAdList();
		ImageView indicator = null;
		for( int i = 0 ; i < vIndicatorGroup.getChildCount() ; i++ )
		{
			indicator = (ImageView)vIndicatorGroup.getChildAt( i );
			if( indicator != null )
			{
				indicator.setImageResource( R.drawable.camera_page_ad_unselected );
				indicator.setVisibility( GONE );
			}
		}
	}
	
	/**
	 * 清除当前广告数据数组,并回收资源图片
	 * @author yangtianyu 2016-7-1
	 */
	private void clearCurAdList()
	{
		if( mCurAdInfoList == null )
			return;
		Bitmap tmpBitmap = null;
		for( int i = 0 ; i < mCurAdInfoList.size() ; i++ )
		{
			tmpBitmap = mCurAdInfoList.get( i ).getAdHiimg();
			if( tmpBitmap != null && !tmpBitmap.isRecycled() )
			{
				tmpBitmap.recycle();
				tmpBitmap = null;
			}
		}
		mCurAdInfoList.clear();
	}
	
	private class ChangeAdRunnable implements Runnable
	{
		
		@Override
		public void run()
		{
			// YANGTIANYU@2016/08/09 ADD START
			// i_0014123
			checkDateForTimes();
			// YANGTIANYU@2016/08/09 ADD END
			boolean enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.CAMERA_ADPLACE_ID , mTimesAdShown );
			CameraView.logI( StringUtils.concat( TAG , ",ChangeAdRunnable enableShowAd:" , enableShowAd ) );
			if( enableShowAd && mCurAdInfoList != null )
			{
				changeListIfNeeded();
				changeAdImage();
				updateIndicator();
				postNewRunnable( change_ad_time_span );
			}
			else
			{
				vIndicatorGroup.setVisibility( INVISIBLE );
			}
		}
		
		/**
		 * 当存在新广告数据且本次广告切换将切换至第一个广告时,用新广告数据替换掉旧数据
		 * @author yangtianyu 2016-7-1
		 */
		private void changeListIfNeeded()
		{
			if( mNewAdInfoList == null )
				return;
			if( mCurAdInfoList == null || mCurAdInfoList.size() <= 0 )
			{
				mCurAdInfoList = mNewAdInfoList;
				mNewAdInfoList = null;
				mCurAdIndex = -1;
			}
			else if( mCurAdIndex == mCurAdInfoList.size() - 1 )
			{
				clearCurAdList();
				mCurAdInfoList = mNewAdInfoList;
				mNewAdInfoList = null;
				mCurAdIndex = -1;
			}
		}
		
		/**
		 * 更换广告图片
		 * @author yangtianyu 2016-7-1
		 */
		private void changeAdImage()
		{
			if( mCurAdInfoList.size() <= 0 )
				return;
			int adSize = mCurAdInfoList.size();
			if( adSize == 1 )
			{
				mCurAdIndex = 0;
			}
			else
			{
				if( moveToRight )
				{
					mCurAdIndex--;
					moveToRight = false;
				}
				else
					mCurAdIndex++;
				mCurAdIndex = mCurAdIndex >= adSize ? 0 : mCurAdIndex;
				mCurAdIndex = mCurAdIndex < 0 ? adSize - 1 : mCurAdIndex;
				mTimesAdShown++;
			}
			CameraView.logI( StringUtils.concat( TAG , ",changeAdImage mCurAdIndex:" , mCurAdIndex ) );
			CameraView.logI( StringUtils.concat( TAG , ",changeAdImage mTimesAdShown:" , mTimesAdShown ) );
			vAdImage.setImageBitmap( mCurAdInfoList.get( mCurAdIndex ).getAdHiimg() );
		}
		
		/**
		 * 更新指示器
		 * @author yangtianyu 2016-7-1
		 */
		private void updateIndicator()
		{
			// 指示器状态根据广告图片数量设置,避免指示器显示异常
			int size = mCurAdInfoList.size();
			int childSize = vIndicatorGroup.getChildCount();
			ImageView indicator = null;
			if( size <= 1 )
			{
				vIndicatorGroup.setVisibility( INVISIBLE );
			}
			else
			{
				vIndicatorGroup.setVisibility( VISIBLE );
				for( int i = 0 ; i < childSize ; i++ )
				{
					indicator = (ImageView)vIndicatorGroup.getChildAt( i );
					int isVisible = GONE;
					if( i < size )
					{
						isVisible = VISIBLE;
						indicator.setImageResource( i == mCurAdIndex ? R.drawable.camera_page_ad_selected : R.drawable.camera_page_ad_unselected );
					}
					indicator.setVisibility( isVisible );
				}
			}
		}
	}
	
	/**
	 * 加入新的定时runnable
	 * @author yangtianyu 2016-7-1
	 */
	private void postNewRunnable(
			long delay )
	{
		mHandler.removeCallbacks( mChangeAdRunnable );
		mHandler.postDelayed( mChangeAdRunnable , delay );
	}
}
