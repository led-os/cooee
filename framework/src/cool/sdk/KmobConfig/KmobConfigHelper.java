package cool.sdk.KmobConfig;


import android.content.Context;


/**
 * 广告运营配置开关
 * @author gaominghui
 *
 */
public class KmobConfigHelper extends KmobConfigUpdate
{
	
	private static KmobConfigHelper instance;
	
	protected KmobConfigHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	public static KmobConfigHelper getInstance(
			Context context )
	{
		synchronized( KmobConfigHelper.class )
		{
			if( instance == null )
			{
				instance = new KmobConfigHelper( context );
			}
		}
		return instance;
	}
}
