package cool.sdk.kuso;


import android.content.Context;
import cool.sdk.download.CoolDLMgr;


public class KuSo
{
	
	public static final int h12 = 3;
	public static final String h13 = "KuSo";
	
	public static CoolDLMgr CoolDLMgr(
			Context context ,
			String moudleName )
	{
		return CoolDLMgr.getInstance( context , moudleName + h12 + "R" , h12 , h13 );
	}
}
