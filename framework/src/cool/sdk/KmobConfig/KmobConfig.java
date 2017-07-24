package cool.sdk.KmobConfig;


import android.content.Context;

import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.CoolDLMgr;


public class KmobConfig
{
	
	public static final int h12 = 3;
	public static final String h13 = "KmobConfig";
	
	public static CoolDLMgr CoolDLMgr(
			Context context ,
			String moudleName )
	{
		return CoolDLMgr.getInstance( context , StringUtils.concat( moudleName , h12 , "R" ) , h12 , h13 );
	}
}
