package com.cooee.phenix.camera.control;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.camera.R;
import com.cooee.phenix.camera.inte.IAdDisplayer;
import com.cooee.phenix.kmob.ad.IKmobCallback;
import com.cooee.phenix.kmob.ad.KmobAdData;
import com.cooee.phenix.kmob.ad.KmobAdItem;
import com.cooee.phenix.kmob.ad.KmobMessage;
import com.cooee.phenix.kmob.ad.KmobUtil;

import cool.sdk.KmobConfig.KmobConfigData;


/**
 * 用于管理广告展示的类,负责广告的请求、展示和隐藏等功能
 * @author yangtianyu  2016-7-1
 */
public class AdManager implements IKmobCallback
{
	
	private static final String TAG = "AdManager";
	private Context mContext;
	/**非广告展示情况下显示的界面*/
	private View normalView;
	/**广告展示界面*/
	private IAdDisplayer adView;
	/**最近一次请求广告数据的时间*/
	private long mLastRequestTime = 0;
	/**是否为第一次展示广告*/
	private boolean isFirstDisplay = true;
	/**广告交互对象,负责请求广告操作*/
	private KmobMessage mAdMessage = null;
	/**广告交互对象,负责请求广告图片操作*/
	private KmobAdItem mAdItem = null;
	/**广告数据表,在请求广告图片期间作为临时容器保存广告数据*/
	private Map<String , KmobAdData> tempAdInfoMap = null;
	/**新获取的广告数据*/
	private List<KmobAdData> mNewAdInfoList = null;
	/**一次获取的广告数量*/
	public static final int SUMS = 3;
	
	public AdManager(
			Context context ,
			View normalView ,
			IAdDisplayer adView )
	{
		this.mContext = context;
		this.normalView = normalView;
		this.adView = adView;
		mAdItem = new KmobAdItem( context , this );
		mAdMessage = new KmobMessage( mAdItem );
	}
	
	/**
	 * 请求广告数据
	 * @author yangtianyu 2016-7-1
	 */
	private void requestAdItem()
	{
		mLastRequestTime = System.currentTimeMillis();
		mAdMessage.getKmobMessage( mContext , KmobConfigData.CAMERA_ADPLACE_ID , SUMS );
	}
	
	/**
	 * 显示广告展示页
	 * @author yangtianyu 2016-7-1
	 */
	private void showAdView()
	{
		normalView.setVisibility( View.GONE );
		if( adView instanceof View )
		{
			( (View)adView ).setVisibility( View.VISIBLE );
		}
		adView.showAdView();
	}
	
	/**
	 * 隐藏广告展示页
	 * @author yangtianyu 2016-7-1
	 */
	private void hideAdView()
	{
		normalView.setVisibility( View.VISIBLE );
		if( adView instanceof View )
		{
			( (View)adView ).setVisibility( View.GONE );
		}
		adView.dispose();
		adView.hideAdView();
	}
	
	private Bitmap resizeAdBitmap(
			Bitmap bitmap ,
			boolean recycle )
	{
		Bitmap orig = bitmap;
		CameraView.logI( StringUtils.concat( TAG , ",setTextureRegion - orig.isRecycled():" , orig.isRecycled() ) );
		if( bitmap == null || bitmap.isRecycled() )
		{
			return null;
		}
		int width = (int)mContext.getResources().getDimension( R.dimen.camera_page_camera_preview_layout_width );
		int height = (int)mContext.getResources().getDimension( R.dimen.camera_page_camera_preview_layout_height );
		bitmap = Bitmap.createScaledBitmap( orig , (int)width , (int)height , true );
		CameraView.logI( StringUtils.concat( TAG , ",setTextureRegion - bitmap:" + bitmap ) );
		if( bitmap != orig && recycle )
		{
			orig.recycle();
			CameraView.logI( StringUtils.concat( TAG , ",setTextureRegion - orig has recycle !!! orig:" + orig ) );
		}
		return bitmap;
	}
	
	/**
	 * 页面切换到广告展示页所在的页面时所需要进行的处理
	 * @author yangtianyu 2016-7-1
	 */
	public void onPageMoveIn()
	{
		long adShowTimes = adView.getTimesAdShown();
		boolean enableRequestAd = KmobUtil.getInstance().enableRequestAd( KmobConfigData.CAMERA_ADPLACE_ID , adShowTimes , mLastRequestTime );
		CameraView.logI( StringUtils.concat( TAG , ",enableRequestAd:" , enableRequestAd ) );
		if( enableRequestAd )
			requestAdItem();
		boolean enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.CAMERA_ADPLACE_ID , adShowTimes );
		if( enableShowAd )
			// visible状态暂时不用改,因为不知道当前是否已经有广告了,在第一次获取广告图片后修改
			adView.showAdView();
	}
	
	/**
	 * 页面从广告展示页切换到其他页面时,广告展示页所需要进行的处理
	 * @author yangtianyu 2016-7-1
	 */
	public void onPageMoveOut()
	{
		boolean enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.CAMERA_ADPLACE_ID , adView.getTimesAdShown() );
		if( enableShowAd )
		{
			// 只是当前未显示
			adView.hideAdView();
		}
		else
		{
			// 完全隐藏,不再显示
			hideAdView();
		}
	}
	
	/**
	 * 点击广告展示页
	 * @author yangtianyu 2016-7-1
	 */
	public void onClick()
	{
		adView.onClick();
	}
	
	/**
	 * 开始预览后,广告需要进行的处理
	 * @author yangtianyu 2016-7-2
	 */
	public void startPreview()
	{
		adView.hideAdView();
	}
	
	/**
	 * 关闭预览后,广告需要进行的处理
	 * @author yangtianyu 2016-7-2
	 */
	public void stopPreview()
	{
		adView.showAdView();
	}
	
	@Override
	public boolean ifNeedGetPic()
	{
		return false;
	}
	
	@Override
	public void loadAdBmpFinish(
			Bitmap arg0 ,
			String arg1 )
	{
		KmobAdData data = tempAdInfoMap.get( arg1 );
		if( data == null )
			return;
		arg0 = resizeAdBitmap( arg0 , true );
		if( arg0 == null )
			return;
		data.setAdHiimg( arg0 );
		// YANGTIANYU@2016/08/19 ADD START
		// 移除操作提前,会根据map中是否有数据来判断本次广告是否完全获取完成
		tempAdInfoMap.remove( arg1 );
		// YANGTIANYU@2016/08/19 ADD END
		if( isFirstDisplay )
		{
			adView.addAdItem( data );
			if( normalView.getVisibility() == View.VISIBLE )
				showAdView();
		}
		else
		{
			if( mNewAdInfoList == null )
			{
				mNewAdInfoList = new ArrayList<KmobAdData>();
			}
			mNewAdInfoList.add( data );
			// YANGTIANYU@2016/08/19 UPD START
			// 不再凑满广告数量后更换,而是本次获取广告操作中的所有广告已经全部获取后进行更换
			//if( mNewAdInfoList.size() == SUMS )
			if( tempAdInfoMap.isEmpty() )
			// YANGTIANYU@2016/08/19 UPD END
			{
				adView.notifyAdChanged( mNewAdInfoList );
				mNewAdInfoList = null;
			}
		}
		// YANGTIANYU@2016/08/19 DEL START
		//tempAdInfoMap.remove( arg1 );
		// YANGTIANYU@2016/08/19 DEL END
	}
	
	@Override
	public void loadAdDataFinish(
			List<KmobAdData> arg0 )
	{
		isFirstDisplay = !adView.isShowing();
		String key = null;
		List<String> urlList = new ArrayList<String>();
		if( tempAdInfoMap == null )
		{
			tempAdInfoMap = new HashMap<String , KmobAdData>();
		}
		tempAdInfoMap.clear();
		for( int i = 0 ; i < arg0.size() ; i++ )
		{
			key = arg0.get( i ).getHiimg();//拿高清图片的url
			tempAdInfoMap.put( key , arg0.get( i ) );
			urlList.add( key );
		}
		//		CameraView.logI( TAG + " , urlList size = " + urlList.size() + "; changAd = " + changeAd );
		mAdItem.getAdBitmap( urlList );
		// YANGTIANYU@2016/08/19 ADD START
		// 清除之前获取到的广告数据 
		// i_0014333 相机页广告三张广告显示的是同一张
		clearAdInfoList();
		// YANGTIANYU@2016/08/19 ADD END
	}
	
	/**
	 * 清除之前获取到且未能满足展示条件的广告图片
	 * @author yangtianyu 2016-8-19
	 */
	private void clearAdInfoList()
	{
		if( mNewAdInfoList != null )
		{
			Bitmap adBmp = null;
			for( int i = 0 ; i < mNewAdInfoList.size() ; i++ )
			{
				adBmp = mNewAdInfoList.get( i ).getAdHiimg();
				if( adBmp != null && !adBmp.isRecycled() )
				{
					adBmp.recycle();
					adBmp = null;
				}
			}
			mNewAdInfoList.clear();
		}
	}
}
