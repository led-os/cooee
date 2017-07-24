package cool.sdk.BeautyCenterConfig;


import android.content.Context;


/**
 * 运营桌面开关
 * @author liuning
 *
 */
public class BeautyCenterConfigHelper extends BeautyCenterConfigUpdate
{
	
	private static BeautyCenterConfigHelper instance;
	
	protected BeautyCenterConfigHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	public static BeautyCenterConfigHelper getInstance(
			Context context )
	{
		synchronized( BeautyCenterConfigHelper.class )
		{
			if( instance == null )
			{
				instance = new BeautyCenterConfigHelper( context );
			}
		}
		return instance;
	}
}
