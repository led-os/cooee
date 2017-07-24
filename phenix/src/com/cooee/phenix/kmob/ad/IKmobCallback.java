package com.cooee.phenix.kmob.ad;


import java.util.List;

import android.graphics.Bitmap;


public interface IKmobCallback
{
	
	void loadAdBmpFinish(
			Bitmap adBmp ,
			String url );
	
	void loadAdDataFinish(
			List<KmobAdData> mAdList );
	
	boolean ifNeedGetPic();
}
