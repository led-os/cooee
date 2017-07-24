/***/
package com.cooee.phenix.musicpage;


import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.kmob.ad.IKmobCallback;
import com.cooee.phenix.kmob.ad.KmobAdData;
import com.cooee.phenix.kmob.ad.KmobAdItem;
import com.cooee.phenix.kmob.ad.KmobMessage;
import com.cooee.phenix.kmob.ad.KmobUtil;
import com.kmob.kmobsdk.KmobManager;

import cool.sdk.KmobConfig.KmobConfigData;


/**
 * 音乐页添加的文字广告
 * @author gaominghui 2016年7月13日
 */
public class AdTextView extends TextView implements IKmobCallback , OnClickListener
{
	
	private static final String TAG = "AdTextView";
	private Context mContext;
	/**广告交互对象,负责请求广告操作*/
	private KmobMessage mAdMessage = null;
	/**广告交互对象,负责请求广告图片操作*/
	private KmobAdItem mAdItem = null;
	/**新获取的广告数据*/
	private KmobAdData mCurDisplayAd = null;
	/**一次获取的广告数量*/
	public static final int SUMS = 1;
	/*已经展示的广告次数*/
	private long hasShows = 0;
	/**最近一次请求广告数据的时间*/
	public static long lastRequestTime = 0;
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	public AdTextView(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
		mContext = context;
		init( context );
	}
	
	/**
	 *
	 * @param context
	 * @author gaominghui 2016年7月14日
	 */
	public void init(
			Context context )
	{
		//初始化数据
		if( mAdItem == null )
		{
			mAdItem = new KmobAdItem( context , this );
		}
		if( mAdMessage == null )
		{
			mAdMessage = new KmobMessage( mAdItem );
		}
		setOnClickListener( this );
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public AdTextView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	/**
	 * @param context
	 */
	public AdTextView(
			Context context )
	{
		this( context , null );
		// TODO Auto-generated constructor stub
	}
	
	/**
	 *
	 * @see com.cooee.phenix.kmob.ad.IKmobCallback#ifNeedGetPic()
	 * @auther gaominghui  2016年7月13日
	 */
	@Override
	public boolean ifNeedGetPic()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 请求广告数据
	 * @author gaominghui 2016-7-14
	 */
	public void requestAdItem()
	{
		mAdMessage.getKmobMessage( mContext , KmobConfigData.LYRIC_ADPLACE_ID , SUMS );
		lastRequestTime = System.currentTimeMillis();
	}
	
	/**
	 *
	 * @see com.cooee.phenix.kmob.ad.IKmobCallback#loadAdBmpFinish(android.graphics.Bitmap, java.lang.String)
	 * @auther gaominghui  2016年7月13日
	 */
	@Override
	public void loadAdBmpFinish(
			Bitmap arg0 ,
			String arg1 )
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 *
	 * @see com.cooee.phenix.kmob.ad.IKmobCallback#loadAdDataFinish(java.util.List)
	 * @auther gaominghui  2016年7月13日
	 */
	@Override
	public void loadAdDataFinish(
			List<KmobAdData> arg0 )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "loadAdDataFinish!!" );
		for( int i = 0 ; i < arg0.size() ; i++ )
		{
			KmobAdData adData = arg0.get( i );
			boolean enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.LYRIC_ADPLACE_ID , hasShows );
			if( enableShowAd )
			{
				String adText = adData.getHeadline();
				if( adText != null && this.getVisibility() == View.VISIBLE )
				{
					this.setText( adText );
					hasShows += 1;
				}
				mCurDisplayAd = adData;
				adData = null;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , StringUtils.concat( "loadAdDataFinish - hasShows:" , hasShows ) );
			}
		}
	}
	
	/**
	 *
	 * @see android.view.View#setVisibility(int)
	 * @auther gaominghui  2016年7月13日
	 */
	@Override
	public void setVisibility(
			int visibility )
	{
		// TODO Auto-generated method stub
		if( visibility == View.VISIBLE && this.getText() != null )
		{
			hasShows += 1;
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "setVisible - hasShows:" , hasShows ) );
		}
		super.setVisibility( visibility );
	}
	
	/**
	 * @return the hasShows
	 */
	public long getHasShows()
	{
		return hasShows;
	}
	
	/**
	 * @param hasShows the hasShows to set
	 */
	public void setHasShows(
			long hasShows )
	{
		this.hasShows = hasShows;
	}
	
	/**
	 *
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @auther gaominghui  2016年7月14日
	 */
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( this.getVisibility() == View.VISIBLE && mCurDisplayAd != null && mCurDisplayAd.getHeadline() != null )
		{
			KmobManager.onClickDone( mCurDisplayAd.getAdid() , mCurDisplayAd.getOtherInfo() , true );
		}
	}
}
