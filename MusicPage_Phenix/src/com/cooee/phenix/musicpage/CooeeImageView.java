package com.cooee.phenix.musicpage;


// MusicPage
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.kmob.ad.IKmobCallback;
import com.cooee.phenix.kmob.ad.KmobAdData;
import com.cooee.phenix.kmob.ad.KmobAdItem;
import com.cooee.phenix.kmob.ad.KmobMessage;
import com.cooee.phenix.kmob.ad.KmobUtil;
import com.cooee.phenix.musicandcamerapage.utils.BitmapUtils;
import com.kmob.kmobsdk.KmobManager;

import cool.sdk.KmobConfig.KmobConfigData;


public class CooeeImageView extends ImageView implements IKmobCallback
{
	
	private static final String TAG = "CooeeImageView";
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
	private Bitmap defaultTurntableBitmap = null;
	private Bitmap defaultAlumbBitmap = null;
	/**mask之后的广告bitmap*/
	public Bitmap[] mAdBitmaps = null;
	private boolean isShowingAd = false;
	private Bitmap upBitmap = null;
	private Bitmap downbBitmap = null;
	
	public CooeeImageView(
			Context context )
	{
		this( context , null );
	}
	
	public CooeeImageView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public CooeeImageView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		mContext = context;
		init( mContext );
	}
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 * @param defStyleRes
	 */
	//	public CooeeImageView(
	//			Context context ,
	//			AttributeSet attrs ,
	//			int defStyleAttr ,
	//			int defStyleRes )
	//	{
	//		super( context , attrs , defStyleAttr , defStyleRes );
	//		// TODO Auto-generated constructor stub
	//	}
	@Override
	public void setTranslationY(
			float translationY )
	{
		super.setTranslationY( translationY );
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
		//setOnClickListener( this );
	}
	
	/**
	 * 请求广告数据
	 * @author gaominghui 2016-7-14
	 */
	public void requestAdItem()
	{
		mAdMessage.getKmobMessage( mContext , KmobConfigData.ALBUM_ADPLACE_ID , SUMS );
		lastRequestTime = System.currentTimeMillis();
	}
	
	public void setImageBitmap(
			boolean up )
	{
		//ViewUtils.printStackTrace( "andy" );
		if( mCurDisplayAd != null && mAdBitmaps != null && mAdBitmaps.length == 2 )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "setImageBitmap up:" , up ) );
			if( up )
			{
				if( null != mAdBitmaps[0] && !mAdBitmaps[0].isRecycled() ) //gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
					this.setImageBitmap( mAdBitmaps[0] );
			}
			else
			{
				if( null != mAdBitmaps[1] && !mAdBitmaps[1].isRecycled() ) //gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
					this.setImageBitmap( mAdBitmaps[1] );
			}
			isShowingAd = true;
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.kmob.ad.IKmobCallback#loadAdBmpFinish(android.graphics.Bitmap, java.lang.String)
	 * @auther gaominghui  2016年7月14日
	 */
	@Override
	public void loadAdBmpFinish(
			Bitmap arg0 ,
			String arg1 )
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "loadAdBmpFinish adBmp = " + arg0 );
		if( arg1 != null && mCurDisplayAd != null && mCurDisplayAd.getHiimg().equals( arg1 ) )
		{
			mCurDisplayAd.setAdHiimg( arg0 );
		}
		if( mAdBitmaps != null && mAdBitmaps.length == 2 )
		{
			upBitmap = mAdBitmaps[0];
			downbBitmap = mAdBitmaps[1];
		}
		if( defaultTurntableBitmap == null )
		{
			defaultTurntableBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.music_page_turntable_bg_anim );
		}
		if( defaultAlumbBitmap == null )
		{
			defaultAlumbBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.music_page_default_album );
		}
		mAdBitmaps = BitmapUtils.maskAdBitmap( arg0 , defaultTurntableBitmap , defaultAlumbBitmap );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "this.getTag():" , this.getTag() ) );
		if( this.getTag() == null )
		{
			setImageBitmap( true );
			hasShows++;
		}
		if( mAdBitmaps != null && mAdBitmaps.length == 2 )
		{
			if( upBitmap != null && mAdBitmaps[0] != upBitmap )
			{
				upBitmap.recycle();
				upBitmap = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
			}
			if( downbBitmap != null && mAdBitmaps[1] != downbBitmap )
			{
				downbBitmap.recycle();
				downbBitmap = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
			}
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.kmob.ad.IKmobCallback#ifNeedGetPic()
	 * @auther gaominghui  2016年7月14日
	 */
	@Override
	public boolean ifNeedGetPic()
	{
		// TODO Auto-generated method stub
		return false;
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
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "hasShows:" , hasShows ) );
	}
	
	/**
	 * @return the isShowingAd
	 */
	public boolean isShowingAd()
	{
		return isShowingAd;
	}
	
	/**
	 * @param isShowingAd the isShowingAd to set
	 */
	public void setShowingAd(
			boolean isShowingAd )
	{
		this.isShowingAd = isShowingAd;
		// gaominghui@2016/11/04 ADD START 当当天广告次数展示到了之后，应该把和广告相关的bitmap都回收掉，避免内存泄漏
		if( !this.isShowingAd )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "setShowingAd hasShows:" , hasShows , "-enableShowTimes:" , KmobUtil.getInstance().enableShowTimes( KmobConfigData.ALBUM_ADPLACE_ID ) ) );
			if( hasShows >= KmobUtil.getInstance().enableShowTimes( KmobConfigData.ALBUM_ADPLACE_ID ) )
			{
				if( upBitmap != null && !upBitmap.isRecycled() )
				{
					upBitmap.recycle();
					upBitmap = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
				}
				if( downbBitmap != null && !downbBitmap.isRecycled() )
				{
					downbBitmap.recycle();
					downbBitmap = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
				}
				if( mAdBitmaps != null && mAdBitmaps.length == 2 )
				{
					if( mAdBitmaps[0] != null && !mAdBitmaps[0].isRecycled() )
					{
						mAdBitmaps[0].recycle();
						mAdBitmaps[0] = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
					}
					if( mAdBitmaps[1] != null && !mAdBitmaps[1].isRecycled() )
					{
						mAdBitmaps[1].recycle();
						mAdBitmaps[1] = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
					}
					mAdBitmaps = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
				}
				if( defaultTurntableBitmap != null && !defaultTurntableBitmap.isRecycled() )
				{
					defaultTurntableBitmap.recycle();
					defaultTurntableBitmap = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
				}
				if( defaultAlumbBitmap != null && !defaultAlumbBitmap.isRecycled() )
				{
					defaultAlumbBitmap.recycle();
					defaultAlumbBitmap = null;//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
				}
			}
		}
		// gaominghui@2016/11/04 ADD END  当当天广告次数展示到了之后，应该把和广告相关的bitmap都回收掉，避免内存泄漏
	}
	
	/**
	 *
	 * @see com.cooee.phenix.kmob.ad.IKmobCallback#loadAdDataFinish(java.util.List)
	 * @auther gaominghui  2016年7月15日
	 */
	@Override
	public void loadAdDataFinish(
			List<KmobAdData> arg0 )
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "loadAdDataFinish !!arg0 = " + arg0 );
		String url = null;
		List<String> urlList = new ArrayList<String>();
		if( !arg0.isEmpty() && arg0.size() == 1 )
		{
			KmobAdData adData = arg0.get( 0 );
			url = arg0.get( 0 ).getHiimg();
			mCurDisplayAd = adData;
			urlList.add( url );
		}
		arg0.clear();
		mAdItem.getAdBitmap( urlList );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "loadAdDataFinish - urlList:" , urlList.toString() ) );
	}
	
	public boolean onAdClick()
	{
		if( mCurDisplayAd != null && isShowingAd )
		{
			KmobManager.onClickDone( mCurDisplayAd.getAdid() , mCurDisplayAd.getOtherInfo() , true );
		}
		return true;
	}
	
	/**
	 *
	 * @see android.view.View#setAlpha(float)
	 * @auther gaominghui  2016年7月27日
	 */
	@Override
	public void setAlpha(
			float alpha )
	{
		// TODO Auto-generated method stub
		/*if( alpha == 0 )
		{
			Log.e( TAG , "setAlpha alpha==0  !!!!!" );
		}
		if( alpha == 1 )
		{
			Log.e( TAG , "setAlpha alpha==1  !!!!!" );
			//ViewUtils.printStackTrace( "CooeeImageView" );
		}*/
		super.setAlpha( alpha );
	}
}
