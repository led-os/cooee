package cool.sdk.Category;


import android.content.Context;

import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.CoolDLMgr;


// floder category , change by shlt@2014/12/08 UPD
public class Category
{
	
	public static final int h12 = 4;
	public static final String h13 = "category";
	
	public static CoolDLMgr CoolDLMgr(
			Context context ,
			String moudleName )
	{
		return CoolDLMgr.getInstance( context , StringUtils.concat( moudleName , h12 , "C" ) , h12 , h13 );
	}
}
