package cool.sdk.Uiupdate;


import android.content.Context;

import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.CoolDLMgr;


public class Uiupdate
{
	
	public static final int h12 = 4;
	public static final String h13 = "uiupdate";
	
	public static CoolDLMgr CoolDLMgr(
			Context context ,
			String moudleName )
	{
		return CoolDLMgr.getInstance( context , StringUtils.concat( moudleName , h12 , "D" ) , h12 , h13 );
	}
}
