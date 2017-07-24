package com.iLoong.launcher.desktop;


import android.content.Context;

import com.cooee.phenix.R;


// cheyingkun add start //免责声明布局(免责声明管理类)
/**
 * 免责声明管理类
 * @author cheyingkun
 */
public class DisclaimerManager
{
	
	private static DisclaimerManager mDisclaimerManager;
	private static Context mContext;
	/**launcher启动时弹出的dialog*/
	public static int LAUNCHRE_ONCREATE_DISCLAIMER = 0;
	/**点击智能分类弹出的免责声明*/
	public static int VISIT_NETWORK_DISCLAIMER_CATEGORY = 1;
	/**下载apk时弹出免责声明*/
	public static int VISIT_NETWORK_DISCLAIMER_DOWNLOAD_APK = 2;
	/**点击搜索框时弹出免责声明*/
	public static int VISIT_NETWORK_DISCLAIMER_SEARCH = 3;
	
	private DisclaimerManager()
	{
	}
	
	public static DisclaimerManager getInstance(
			Context mContext )
	{
		if( mDisclaimerManager == null && mContext != null )
		{
			synchronized( DisclaimerManager.class )
			{
				if( mDisclaimerManager == null && mContext != null )
				{
					mDisclaimerManager = new DisclaimerManager();
				}
			}
		}
		if( mContext != null )
		{
			DisclaimerManager.mContext = mContext;
		}
		return mDisclaimerManager;
	}
	
	/**
	 * @param style dialog当前样式
	 * Launcher启动时,用这个{@link Disclaimer.LAUNCHRE_ONCREATE_DISCLAIMER}
	 * 智能分类,用这个{@link Disclaimer.VISIT_NETWORK_DISCLAIMER_CATEGORY}
	 * 下载apk时,用这个{@link Disclaimer.VISIT_NETWORK_DISCLAIMER_DOWNLOAD_APK}
	 * 点击搜索框时,用这个{@link Disclaimer.VISIT_NETWORK_DISCLAIMER_SEARCH}
	 */
	public void showDisclaimer(
			int style ,
			Disclaimer.OnClickListener l )
	{
		boolean isShow = Disclaimer.isNeedShowDisclaimer();
		if( isShow )
		{
			Disclaimer disclaimer = new Disclaimer( mContext , R.style.Disclaimer_dialog , style );
			disclaimer.setOnClickListener( l );
			disclaimer.show();
		}
	}
}
// cheyingkun add end
