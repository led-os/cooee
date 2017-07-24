package cool.sdk.DynamicEntry;


import android.content.Context;

import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.CoolDLMgr;


public class DynamicEntry
{
	
	public static final int h12 = 4;
	public static final String h13 = "dynamicentrance";
	
	public static CoolDLMgr CoolDLMgr(
			Context context ,
			String moudleName )
	{
		return CoolDLMgr.getInstance( context , StringUtils.concat( moudleName , h12 , "D" ) , h12 , h13 );
	}
}
