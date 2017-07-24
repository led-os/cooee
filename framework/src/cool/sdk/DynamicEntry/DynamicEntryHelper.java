package cool.sdk.DynamicEntry;


import android.content.Context;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.utils.StringUtils;


public class DynamicEntryHelper extends DynamicEntryUpdate
{
	
	public interface DynamicEntryListener
	{
		
		public void onDataChange(
				String json );
	}
	
	private static DynamicEntryHelper instance = null;
	private DynamicEntryListener mListener;
	
	protected DynamicEntryHelper(
			Context context )
	{
		super( context );
	}
	
	public static DynamicEntryHelper getInstance(
			Context context )
	{
		if( instance == null )
		{
			synchronized( DynamicEntryHelper.class )
			{
				if( instance == null )
				{
					instance = new DynamicEntryHelper( context );
				}
			}
		}
		return instance;
	}
	
	public void setListener(
			DynamicEntryListener listener )
	{
		this.mListener = listener;
	}
	
	@Override
	public void OnDataChange() throws Exception
	{
		//		String json = getListString();
		//		Log.v( "COOL" , "DynamicEntryHelper: OnDataChange:" + json );
		//		if(mListener != null){
		//			mListener.onDataChange(json);
		//		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "DynamicEntryHelper: OnDataChange:" , getListString() ) );
		if( OperateDynamicProxy.context != null )
		{
			OperateDynamicProxy.getInstance().parseDynamicUpdateData();
		}
	}
	
	@Override
	public String getEntryID() throws Exception
	{
		if( OperateDynamicProxy.context != null )
		{
			return OperateDynamicProxy.getInstance().getDefaultDynamicID();
		}
		return "";//"1,名称1,类型;2:名称2,类型;";
	}
}
