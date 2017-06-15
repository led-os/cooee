package cool.sdk.MicroEntry;


import android.content.Context;
import cool.sdk.download.CoolDLMgr;


public class MicroEntry
{
	
	public static final int h12 = 4;
	public static final String h13 = "microentrance";
	
	public static CoolDLMgr CoolDLMgr(
			Context context ,
			String moudleName ,
			int entryId )
	{
		return CoolDLMgr.getInstance( context , moudleName + entryId + h12 + "M" , h12 , h13 + entryId );
	}
}
