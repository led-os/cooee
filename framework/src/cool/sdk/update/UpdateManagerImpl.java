package cool.sdk.update;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.Statistics.StatisticsBXUpdate;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.Category.CategoryHelper;
import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.KmobConfig.KmobConfigHelper;
import cool.sdk.MicroEntry.MicroEntryHelper;
import cool.sdk.Pspread.PspreadHelper;
import cool.sdk.download.manager.DlMethod;
import cool.sdk.kuso.KuSoHelper;
import cool.sdk.log.CoolLog;
import cool.sdk.statistics.StatisticsUpdate;


// floder category , change by shlt@2014/12/08 UPD
public class UpdateManagerImpl extends UpdateManager
{
	
	protected static List<UpdateListener> mListeners = new ArrayList<UpdateListener>();
	private static boolean isLoadFinish = false;//cheyingkun add	//允许更新方法添加免责声明和桌面加载完成的判断
	
	public static interface UpdateListener
	{
		
		public void Update();
		
		public void UpdateSync(
				boolean updateNow );
	}
	
	public static void addListener(
			UpdateListener listener )
	{
		if( listener != null )
		{
			mListeners.add( listener );
		}
	}
	
	public static void addUniqueListener(
			UpdateListener listener )
	{
		removeListenerByClass( listener.getClass() );
		if( listener != null )
		{
			mListeners.add( listener );
		}
	}
	
	public static void removeListener(
			UpdateListener listener )
	{
		mListeners.remove( listener );
	}
	
	public static void removeListenerByClass(
			Class target )
	{
		for( int i = mListeners.size() - 1 ; i >= 0 ; i-- )
		{
			UpdateListener listener = mListeners.get( i );
			if( listener.getClass() == target )
			{
				mListeners.remove( listener );
			}
		}
	}
	
	public static void clearListeners()
	{
		mListeners.clear();
	}
	
	public static void Update(
			Context context )
	{
		try
		{
			for( UpdateListener listener : mListeners )
			{
				listener.Update();
			}
			MicroEntryHelper.getInstance( context ).Update();
			DynamicEntryHelper.getInstance( context ).Update();
			CategoryHelper.getInstance( context ).Update();
			KmobConfigHelper.getInstance( context ).Update();//kmob 专属页
			KuSoHelper.getInstance( context ).Update();//kuso add
			StatisticsBXUpdate.getInstance( context ).Update();//StatisticsBXUpdate add
			PspreadHelper.getInstance( context ).Update();
		}
		catch( Exception e )
		{
		}
	}
	
	public static void UpdateSync(
			Context context )
	{
		try
		{
			for( UpdateListener listener : mListeners )
			{
				listener.UpdateSync( false );
			}
			MicroEntryHelper.getInstance( context ).UpdateSync( false );
			DynamicEntryHelper.getInstance( context ).UpdateSync( false );
			CategoryHelper.getInstance( context ).UpdateSync( false );
			KmobConfigHelper.getInstance( context ).UpdateSync( false );//kmob 专属页
			KuSoHelper.getInstance( context ).UpdateSync( false );//kuso add
			StatisticsBXUpdate.getInstance( context ).Update( false );//StatisticsBXUpdate add
			PspreadHelper.getInstance( context ).UpdateSync( false );
		}
		catch( Exception e )
		{
		}
	}
	
	public static void UpdateOver(
			Context context )
	{
	}
	
	public static boolean allowUpdate(
			Context context )
	{
		//cheyingkun add start	//允许更新方法添加免责声明和桌面加载完成的判断
		boolean isNeedShowDisclaimer = false;
		try
		{
			Class<?> cls = Class.forName( "com.iLoong.launcher.desktop.Disclaimer" );
			Method method = cls.getMethod( "isNeedShowDisclaimer" );
			isNeedShowDisclaimer = (Boolean)method.invoke( cls );
		}
		catch( Throwable t )
		{
			t.getStackTrace();
			isNeedShowDisclaimer = false;
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , "allowUpdate class or method NotFoundException" );
		}
		return !isNeedShowDisclaimer && isLoadFinish;
		//cheyingkun add end
	}
	
	//ME_RTFSC  [start]
	private static long PerUpdateTime = 0L;
	private static int UpdateCount = 0;
	private static long curDay = -1;
	
	public static Boolean IsNetworkAvailableLocal(
			Context context )
	{
		final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		if( info != null && info.isAvailable() )
		{
			return true;
		}
		return false;
	}
	
	//ME_RTFSC  [end]
	public static void Resume(
			final Context context )
	{
		final CoolLog Log = new CoolLog( context );
		if( !allowUpdate( context ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , "Resume allowUpdate = false" );
			return;
		}
		long day = SystemClock.elapsedRealtime() / ( 1000 * 60 * 60 * 24 );
		if( day != curDay )
		{
			curDay = day;
			//	if(UpdateCount >=5)
			{
				UpdateCount = 0;
			}
		}
		long time = ( SystemClock.elapsedRealtime() - PerUpdateTime );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "UpdateManagerImpl  Resume:UpdateCount:" , UpdateCount , "-time=" , time / ( 1000 * 60 ) , ":" , time / 1000 % 60 ) );
		if( IsNetworkAvailableLocal( context ) && UpdateCount < 10 && SystemClock.elapsedRealtime() - PerUpdateTime > 1000 * 60 * 5 )
		{
			if( 0 != UpdateCount )
			{
				PerUpdateTime = SystemClock.elapsedRealtime();
				new Thread( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						try
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "COOL" , StringUtils.concat( "SystemClock.elapsedRealtime():" , SystemClock.elapsedRealtime() ) );
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "COOL" , "== UpdateSync  and  checkCategoryRecommend ==" );
							if( DlMethod.IsWifiConnected( context ) && UpdateCount < 4 )
							{
								MicroEntryHelper.getInstance( context ).UpdateForSuccess( true );
								CategoryHelper.getInstance( context ).UpdateForSuccess( true );
								DynamicEntryHelper.getInstance( context ).UpdateForSuccess( true );
								KmobConfigHelper.getInstance( context ).UpdateForSuccess( true );//kmob 专属页
								StatisticsBXUpdate.getInstance( context ).UpdateForSuccess( true );//StatisticsBXUpdate add
								StatisticsUpdate.getInstance( context ).UpdateForSuccess( true );
							}
							else
							{
								/********
								 * 检测当天顯示次數
								 *********/
								StatisticsUpdate statisticsUpdate = StatisticsUpdate.getInstance( context );
								long thisDay = (long)Calendar.getInstance().get( Calendar.DAY_OF_YEAR );
								//当前Update的天
								long curUpdateDay = statisticsUpdate.getLong( "curUpdateDay" , 0L );
								//当前天Update次数
								long curDayUpdateCnt = statisticsUpdate.getLong( "curDayUpdateCnt" , 0L );
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "COOL" , StringUtils.concat( "check Success: thisDay:" , thisDay , "-curUpdateDay:" , curUpdateDay , "-curDayUpdateCnt:" , curDayUpdateCnt ) );
								if( curUpdateDay != thisDay )
								{
									//天改变
									curUpdateDay = thisDay;
									statisticsUpdate.setValue( "curUpdateDay" , curUpdateDay );
									curDayUpdateCnt = 0L;
									statisticsUpdate.setValue( "curDayUpdateCnt" , 0L );
								}
								if( curDayUpdateCnt < 2 )
								{
									statisticsUpdate.setValue( "curDayUpdateCnt" , curDayUpdateCnt + 1 );
									MicroEntryHelper.getInstance( context ).UpdateForSuccess( true );
									CategoryHelper.getInstance( context ).UpdateForSuccess( true );
									DynamicEntryHelper.getInstance( context ).UpdateForSuccess( true );
									KmobConfigHelper.getInstance( context ).UpdateForSuccess( true );//kmob 专属页
									StatisticsBXUpdate.getInstance( context ).UpdateForSuccess( true );//StatisticsBXUpdate add
									StatisticsUpdate.getInstance( context ).UpdateForSuccess( true );
								}
							}
							CategoryHelper.getInstance( context ).checkCategoryRecommend();
							DynamicEntryHelper.getInstance( context ).checkDynamicEntryIcon();
							// 防止以上2个函数运行时间太长， 导致RESUME 重入。放到开始位置。
							//PerUpdateTime = SystemClock.elapsedRealtime();
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "ME_RTFSC" , StringUtils.concat( "UpdateManager.UpdateSync - UpdateCount:" , UpdateCount , "-PerUpdateTime:" , PerUpdateTime ) );
							//CategoryHelper.getInstance( context ).checkCategoryRecommend();
						}
						catch( Exception e )
						{
							// TODO: handle exception
						}
					}
				} ).start();
			}
			UpdateCount += 1;
		}
	}
	
	//cheyingkun add start	//允许更新方法添加免责声明和桌面加载完成的判断
	public static void setLoadFinish(
			boolean isLoadFinish )
	{
		UpdateManagerImpl.isLoadFinish = isLoadFinish;
	}
	//cheyingkun add end
}
