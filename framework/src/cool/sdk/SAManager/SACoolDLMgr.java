package cool.sdk.SAManager;


import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.common.MyMethod;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_task;
import cool.sdk.log.CoolLog;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public abstract class SACoolDLMgr extends UpdateHelper
{
	
	final CoolLog Log;
	
	protected SACoolDLMgr(
			Context context )
	{
		super( context , "SilentCoolDLMgr" , new UpdateConfig() );
		// TODO Auto-generated constructor stub
		Log = new CoolLog( context );
	}
	
	@Override
	protected boolean OnUpdate(
			Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	CoolDLMgr dlMgrApk;
	Object dlMgrApkSync = new Object();
	
	public CoolDLMgr getCoolDLMgrApk()
	{
		synchronized( dlMgrApkSync )
		{
			if( dlMgrApk == null )
			{
				dlMgrApk = CoolDLMgr.getInstance( context , StringUtils.concat( "SA" , 4 , "D" ) , 4 , "SA" );
				dlMgrApk.setIsSilentDownload( true );
				dlMgrApk.setCheckPathEverytime( false );
				dlMgrApk.dl_mgr.setDataBasePath( dlMgrApk.getExternalPath() );
				dlMgrApk.dl_mgr.setDownloadPath( dlMgrApk.getExternalPath() );
				dlMgrApk.dl_mgr.setMaxConnectionCount( 1 );
			}
		}
		return dlMgrApk;
	}
	
	void T1IsShow(
			boolean isShow )
	{
		if( isShow )
		{
			setValue( "T1IsShow" , 1L );
		}
		else
		{
			setValue( "T1IsShow" , (Long)null );
		}
	}
	
	boolean T1IsShow()
	{
		return getLong( "T1IsShow" ) != null;
	}
	
	void T1IsClick(
			boolean isClick )
	{
		if( isClick )
		{
			setValue( "T1IsClick" , 1L );
		}
		else
		{
			setValue( "T1IsClick" , (Long)null );
		}
	}
	
	boolean T1IsClick()
	{
		return getLong( "T1IsClick" ) != null;
	}
	
	void T2IsShow(
			boolean isShow )
	{
		if( isShow )
		{
			setValue( "T2IsShow" , 1L );
		}
		else
		{
			setValue( "T2IsShow" , (Long)null );
		}
	}
	
	boolean T2IsShow()
	{
		return getLong( "T2IsShow" ) != null;
	}
	
	void T2IsClick(
			boolean isClick )
	{
		if( isClick )
		{
			setValue( "T2IsClick" , 1L );
		}
		else
		{
			setValue( "T2IsClick" , (Long)null );
		}
	}
	
	boolean T2IsClick()
	{
		return getLong( "T2IsClick" ) != null;
	}
	
	void T3IsShow(
			boolean isShow )
	{
		if( isShow )
		{
			setValue( "T3IsShow" , 1L );
		}
		else
		{
			setValue( "T3IsShow" , (Long)null );
		}
	}
	
	boolean T3IsShow()
	{
		return getLong( "T3IsShow" ) != null;
	}
	
	void T3IsClick(
			boolean isClick )
	{
		if( isClick )
		{
			setValue( "T3IsClick" , 1L );
		}
		else
		{
			setValue( "T3IsClick" , (Long)null );
		}
	}
	
	boolean T3IsClick()
	{
		return getLong( "T3IsClick" ) != null;
	}
	
	public static final String DEFAULT_VERSION = "0";
	static final int DownloadCntPerDay = 3;//每天下载几个apk
	static final int showNotifyCntPerDay = 1;//每天弹出几次
	
	public interface finishCb
	{
		
		void onFinish();
	}
	
	public synchronized boolean checkSilent(
			final finishCb finishCb )
	{
		if( !allowSilentDownload() )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "checkSilent: allowSilentDownload = false" );
			return false;
		}
		if( getCoolDLMgrApk().dl_mgr.getTaskCount() > 0 )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "checkSilent: is downloading" );
			return false;
		}
		long thisDay = System.currentTimeMillis() / ( 1000 * 60 * 60 * 24 );
		long curDay = getLong( "curDay" , 0L );
		long curDayDownloadCnt = getLong( "curDayDownloadCnt" , 0L );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , StringUtils.concat( "thisDay:" , thisDay , "-curDay:" , curDay , "-curDayDownloadCnt:" , curDayDownloadCnt ) );
		if( curDay != thisDay )
		{
			curDay = thisDay;
			setValue( "curDay" , thisDay );
			curDayDownloadCnt = 0L;
			setValue( "curDayDownloadCnt" , 0L );
		}
		if( curDayDownloadCnt >= DownloadCntPerDay )
		{
			checkNotify();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "curDayDownloadCnt is full, no need download" );
			return false;
		}
		if( doSilentDownload( (int)( DownloadCntPerDay - curDayDownloadCnt ) , new SilentDownloadSuccess() {
			
			public synchronized void onSuccess(
					CoolDLResType type ,
					String name ,
					dl_info info )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , StringUtils.concat( "SilentDownload Success:" , name ) );
				successCnt++;
				//更新当天下载数
				Long curDayDownloadCnt = getLong( "curDayDownloadCnt" , 0L );
				setValue( "curDayDownloadCnt" , curDayDownloadCnt + 1 );
				//更新本运营周期下载数
				Long curOperateVersionDownloadCnt = getLong( "curOperateVersionDownloadCnt" , 0L );
				setValue( "curOperateVersionDownloadCnt" , curOperateVersionDownloadCnt + 1 );
				if( this.successCnt == 1 )
				{
					if( curOperateVersionDownloadCnt == 0 )
					{
						//检查当前运营周期
						String curOperateVersion = getString( "curOperateVersion" , DEFAULT_VERSION );
						if( DEFAULT_VERSION.equals( curOperateVersion ) )
						{
							//当前运营周期
							String thisOperateVersion = getOperateVersion();
							setValue( "curOperateVersion" , thisOperateVersion );
						}
						checkNotify( FLAG_CHECK_HOUR | FLAG_CHECK_DAY_SHOW_CNT );
					}
				}
				onFinishCheck();
			}
			
			@Override
			public void onFinish()
			{
				// TODO Auto-generated method stub
				//更新当天下载数
				Long curDayDownloadCnt = getLong( "curDayDownloadCnt" , 0L );
				//更新本运营周期下载数
				Long curOperateVersionDownloadCnt = getLong( "curOperateVersionDownloadCnt" , 0L );
				//检查当前运营周期
				String curOperateVersion = getString( "curOperateVersion" , DEFAULT_VERSION );
				if( DEFAULT_VERSION.equals( curOperateVersion ) )
				{
					//当前运营周期
					String thisOperateVersion = getOperateVersion();
					setValue( "curOperateVersion" , thisOperateVersion );
				}
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , StringUtils.concat(
							"SilentDownload successCnt:" ,
							this.successCnt ,
							"-downloadCnt:" ,
							this.downloadCnt ,
							"-curDayDownloadCnt:" ,
							curDayDownloadCnt ,
							"-curOperateVersionDownloadCnt:" ,
							curOperateVersionDownloadCnt ) );
				if( getNeedDownloadCnt() == 0 )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "SA" , "all app is download!" );
					setValue( "all app is download" , 1L );
				}
				checkNotify();
				finishCb.onFinish();
			}
		} ) <= 0 )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "all app is download!" );
			setValue( "all app is download" , 1L );
			checkNotify();
			return false;
		}
		return true;
	}
	
	public synchronized void checkNotify()
	{
		checkNotify( -1 );
	}
	
	public static final int FLAG_CHECK_DOWNLOADING = 0x1;
	public static final int FLAG_CHECK_HOUR = 0x2;
	public static final int FLAG_CHECK_DAY_SHOW_CNT = 0x4;
	public static final int FLAG_CHECK_OPERATE_PERIOD_CHANGE = 0x8;
	
	public synchronized void checkNotify(
			int flag )
	{
		if( !allowSilentDownload() )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "checkNotify: allowSilentDownload = false" );
			return;
		}
		if( ( flag & FLAG_CHECK_DOWNLOADING ) == FLAG_CHECK_DOWNLOADING )
		{
			if( getCoolDLMgrApk().dl_mgr.getTaskCount() > 0 )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , "checkNotify: is downloading" );
				return;
			}
		}
		if( ( flag & FLAG_CHECK_HOUR ) == FLAG_CHECK_HOUR )
		{
			/********
			 * 检测小时是否满足
			 *********/
			//当前天
			long thisHour = (long)Calendar.getInstance().get( Calendar.HOUR_OF_DAY );
			if( thisHour >= 18 && thisHour < 23 )
			{
				//18点到23点弹出
				//			if( DlMethod.IsNetworkAvailable( context ) )
				//			{
				//				//网络连接状态下弹出
				//			}
				//			else
				//			{
				//				Log.v( "SA" , "checkNotify: 18-23: network unavailable" );
				//				return;
				//			}
			}
			else if( thisHour >= 23 )
			{
				//23点到24点弹出
			}
			else
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , "checkNotify: hour is not in 18-24" );
				return;
			}
		}
		int cntOnOperateChange = 0;
		long curOperateVersionDownloadCnt = getLong( "curOperateVersionDownloadCnt" , 0L );
		if( ( flag & FLAG_CHECK_OPERATE_PERIOD_CHANGE ) == FLAG_CHECK_OPERATE_PERIOD_CHANGE )
		{
			/********
			 * 检测运营周期
			 *********/
			//当前运营周期
			String thisOperateVersion = getOperateVersion();
			//当前show的运营周期
			String curOperateVersion = getString( "curOperateVersion" , DEFAULT_VERSION );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , StringUtils.concat(
						"checkNotify: thisOperateVersion=" ,
						thisOperateVersion ,
						"-curOperateVersion:" ,
						curOperateVersion ,
						"-curOperateVersionDownloadCnt:" ,
						curOperateVersionDownloadCnt ) );
			if( !curOperateVersion.equals( thisOperateVersion ) )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , "checkNotify: Operate Period change" );
				//T1/T2/T3未显示
				//if( !T1IsShow() || !T2IsShow() || !T3IsShow() )
				{
					cntOnOperateChange = getSuccessButNotInstallList().size();//下载完未安装的应用
				}
				//运营周期改变
				curOperateVersion = thisOperateVersion;
				setValue( "curOperateVersion" , curOperateVersion );
				curOperateVersionDownloadCnt = 0L;
				setValue( "curOperateVersionDownloadCnt" , 0L );
				clearOnOperateChanged();
				T1IsShow( false );
				T2IsShow( false );
				T3IsShow( false );
				T1IsClick( false );
				T2IsClick( false );
				T3IsClick( false );
				setValue( "WhichShowT1T2T3" , (Integer)null );
				setValue( "all app is download" , (Long)null );
				setValue( "T2Day" , (Long)null );
			}
		}
		/********
		 * 检测当天顯示次數
		 *********/
		long thisDay = (long)Calendar.getInstance().get( Calendar.DAY_OF_YEAR );
		//当前show的天
		long curShowDay = getLong( "curShowDay" , 0L );
		//当前天show次数
		long curDayShowCnt = getLong( "curDayShowCnt" , 0L );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , StringUtils.concat( "checkNotify - thisShowDay:" , thisDay , "-curShowDay:" , curShowDay , "-curDayShowCnt:" , curDayShowCnt ) );
		if( curShowDay != thisDay )
		{
			//天改变
			curShowDay = thisDay;
			setValue( "curShowDay" , curShowDay );
			curDayShowCnt = 0L;
			setValue( "curDayShowCnt" , 0L );
		}
		if( ( flag & FLAG_CHECK_DAY_SHOW_CNT ) == FLAG_CHECK_DAY_SHOW_CNT )
		{
			if( curDayShowCnt >= showNotifyCntPerDay )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , "notify is show today." );
				return;
			}
		}
		if( cntOnOperateChange > 0 )
		{
			setValue( "curDayShowCnt" , curDayShowCnt + 1 );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "show notify on Operate Period change" );
			showNotify( NotifyType.OpChange );
			return;
		}
		if( getLong( "all app is download" ) != null )//静默应用是否全部下载
		{
			//是
			if( T2IsShow() )//T2通知是否弹过
			{
				//是
				if( T2IsClick() )//T2通知是否点击
				{
					//是
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "SA" , "checkNotify: to be show T3: not show because T2IsClick" );
				}
				else
				{
					//否
					Long T2Day = getLong( "T2Day" , thisDay );
					if( Math.abs( thisDay - T2Day ) >= 3 )//当前时间T4-T3>=3天
					{
						//是
						if( getSuccessButNotInstallList().size() > 0 )//下载完的应用是否有未安装的
						{
							//是
							if( T3IsShow() )//T3是否弹
							{
								//是
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "SA" , "checkNotify: not show because T3IsShow" );
							}
							else
							{
								//否
								//弹T3通知
								T3IsShow( true );
								setValue( "WhichShowT1T2T3" , 3 );
								setValue( "curDayShowCnt" , curDayShowCnt + 1 );
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "SA" , "showNotify on t3" );
								showNotify( NotifyType.T3 );
							}
						}
						else
						{
							//否
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "SA" , "checkNotify: to be show T3: we haven't find app to be install." );
						}
					}
					else
					{
						//否
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "SA" , StringUtils.concat( "checkNotify: to be show T3:  not show because invalid condition: thisDay-T2Day<T3,thisDay:" , thisDay , "-T2Day:" , T2Day ) );
					}
				}
			}
			else
			{
				//否
				setValue( "T2Day" , thisDay );//当前时间T2
				if( getSuccessButNotInstallList().size() > 0 )//下载完的应用是否有未安装的
				{
					//是
					//弹T2通知
					T2IsShow( true );
					setValue( "WhichShowT1T2T3" , 2 );
					setValue( "curDayShowCnt" , curDayShowCnt + 1 );
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "SA" , "showNotify on t2" );
					showNotify( NotifyType.T2 );
				}
				else
				{
					//否
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "SA" , "checkNotify: to be show T2: we haven't find app to be install." );
				}
			}
		}
		else
		{
			//否
			if( curOperateVersionDownloadCnt > 0 )//静默是否下载完第一个app
			{
				//是
				//T1通知是否弹过
				if( T1IsShow() )
				{
					//是
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "SA" , "checkNotify: not show because T1IsShow" );
				}
				else
				{
					//否
					//setValue( "T1Day" , thisDay );//当前时间T1
					if( getSuccessButNotInstallList().size() > 0 )//app1是否未安装
					{
						//是
						//弹T1通知
						T1IsShow( true );
						setValue( "WhichShowT1T2T3" , 1 );
						setValue( "curDayShowCnt" , curDayShowCnt + 1 );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "SA" , "showNotify on t1" );
						showNotify( NotifyType.T1 );
					}
					else
					{
						//否
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "SA" , "checkNotify: to be show T1: we haven't find app to be install." );
					}
				}
			}
			else
			{
				//否
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , "checkNotify: we haven't download one app." );
			}
		}
	}
	
	public abstract class SilentDownloadSuccess implements CoolDLCallback
	{
		
		protected int downloadCnt = 0;
		protected int successCnt = 0;
		protected int failCnt = 0;
		
		public abstract void onFinish();
		
		void onFinishCheck()
		{
			if( successCnt + failCnt != downloadCnt )
			{
				return;
			}
			onFinish();
		}
		
		public synchronized void addDownloadCnt()
		{
			++downloadCnt;
		}
		
		@Override
		public synchronized void onDoing(
				CoolDLResType type ,
				String name ,
				dl_info info )
		{
			// TODO Auto-generated method stub
		}
		
		@Override
		public synchronized void onSuccess(
				CoolDLResType type ,
				String name ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , StringUtils.concat( "SilentDownload Success:" , name ) );
			successCnt++;
			onFinishCheck();
		}
		
		@Override
		public synchronized void onFail(
				CoolDLResType type ,
				String name ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , StringUtils.concat( "SilentDownload Fail:" , name ) );
			failCnt++;
			onFinishCheck();
		}
	}
	
	public synchronized void clearOnOperateChanged()
	{
		CoolDLMgr coolDLMgr = getCoolDLMgrApk();
		coolDLMgr.dl_mgr.stopAllTask();
		List<dl_info> infoList = coolDLMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
		for( dl_info info : infoList )
		{
			if( !info.IsDownloadSuccess() )
			{
				coolDLMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , (String)info.getValue( "p2" ) , true );
			}
		}
		SAService.stopCheck( context );
	}
	
	public synchronized int getNeedDownloadCnt()
	{
		int downCnt = 0;
		CoolDLMgr coolDLMgr = getCoolDLMgrApk();
		Set<DownloadItem> items = getSilentDownloadList();
		for( DownloadItem item : items )
		{
			if( MyMethod.IsPackageInstalled( context , item.pkgName ) )
			{
				continue;
			}
			dl_info info = coolDLMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , item.pkgName );
			if( info == null || !info.IsDownloadSuccess() )
			{
				downCnt++;
			}
		}
		return downCnt;
	}
	
	public synchronized List<dl_info> getSuccessButNotInstallList()
	{
		CoolDLMgr coolDLMgr = getCoolDLMgrApk();
		List<dl_info> infoList = coolDLMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
		Iterator<dl_info> iter = infoList.iterator();
		while( iter.hasNext() )
		{
			dl_info info = iter.next();
			if( info.IsDownloadSuccess() && !MyMethod.IsPackageInstalled( context , (String)info.getValue( "p2" ) ) && !ApkIsUninstall( info ) )
			{
				if( DlManager.getInstance().getDlInfo( (String)info.getValue( "p2" ) ) != null )//不为空说明在手动下载的列表中
				{
					iter.remove();
				}
			}
			else
			{
				iter.remove();
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , StringUtils.concat( "infoList size= " , infoList.size() ) );
		return infoList;
	}
	
	public synchronized void removeSlientItem(
			String pkg )
	{
		CoolDLMgr coolDLMgr = getCoolDLMgrApk();
		coolDLMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , pkg , true );
	}
	
	//如果是卸载的应用。就不要再加进来了
	public boolean ApkIsUninstall(
			dl_info info )
	{
		Object install_state = info.getValue( SAHelper.WIFISA_INSTALL_STATE );
		int state = 0;
		if( install_state != null )
		{
			state = ( (Integer)install_state ).intValue();
		}
		if( state == SAHelper.WIFISA_UNSTALL )
		{
			return true;
		}
		return false;
	}
	
	public synchronized int doSilentDownload(
			int taskCnt ,
			SilentDownloadSuccess cb )
	{
		int downCnt = 0;
		CoolDLMgr coolDLMgr = getCoolDLMgrApk();
		Set<DownloadItem> items = getSilentDownloadList();
		for( DownloadItem item : items )
		{
			if( downCnt == taskCnt )
			{
				break;
			}
			if( MyMethod.IsPackageInstalled( context , item.pkgName ) )
			{
				continue;
			}
			dl_info info = coolDLMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , item.pkgName );
			if( info != null )
			{
				//正在下载
				if( info.getDownloadState() != 0 )
				{
					continue;
				}
				//下载完
				if( info.IsDownloadSuccess() )
				{
					File file = new File( info.getFilePath() );
					if( file.exists() && file.length() == info.getTotalBytes() )
					{
						continue;
					}
				}
			}
			dl_task task = coolDLMgr.ResDownloadNewTask( CoolDLResType.RES_TYPE_APK , item.pkgName , "SilentDownloadCB" , cb , item.h12 , item.h13 );
			task.setValue( "ENName" , item.ENName );
			task.setValue( "CNName" , item.CNName );
			task.setValue( "TWName" , item.TWName );
			if( 0 == coolDLMgr.ResDownloadStart( task ) )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , StringUtils.concat( "SilentDownload pkgName:" , item.pkgName , "-h12:" , item.h12 , "-h13:" , item.h13 ) );
				cb.addDownloadCnt();
				downCnt++;
			}
		}
		return downCnt;
	}
	
	public static class DownloadItem
	{
		
		String pkgName;
		String ENName;
		String CNName;
		String TWName;
		int h12;
		String h13;
		
		public DownloadItem(
				String pkgName ,
				String ENName ,
				String CNName ,
				String TWName ,
				int h12 ,
				String h13 )
		{
			this.pkgName = pkgName;
			this.ENName = ENName;
			this.CNName = CNName;
			this.TWName = TWName;
			this.h12 = h12;
			this.h13 = h13;
		}
		
		@Override
		public int hashCode()
		{
			// TODO Auto-generated method stub
			return pkgName.hashCode();
		}
		
		@Override
		public String toString()
		{
			// TODO Auto-generated method stub
			return StringUtils.concat( pkgName , "(h12:" , h12 , "-h13:" , h13 , ")" );
		}
	}
	
	protected abstract boolean allowSilentDownload();
	
	protected abstract String getOperateVersion();
	
	protected abstract Set<DownloadItem> getSilentDownloadList();
	
	//wifi1118 start
	//T1为
	//T2为
	//T3为
	public enum NotifyType
	{
		T1 , T2 , T3 , OpChange ,
	}
	
	//wifi1118 end
	protected abstract void showNotify(
			NotifyType type );
	
	public synchronized void clickNotify(
			int WhichShowT1T2T3 )
	{
		//int WhichShowT1T2T3 = getInt( "WhichShowT1T2T3" , 0 );
		switch( WhichShowT1T2T3 )
		{
			case 0:
				break;
			case 1:
				T1IsClick( true );
				break;
			case 2:
				T2IsClick( true );
				break;
			case 3:
				T3IsClick( true );
				break;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , StringUtils.concat( "clickNotify - WhichShowT1T2T3:" , WhichShowT1T2T3 ) );
	}
}
