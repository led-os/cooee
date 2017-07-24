package com.cooee.phenix;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.ShortcutInfo;


// cheyingkun add start //TCardMount
/**
 * T卡挂载信息	(保存,读取,清空挂载信息到文件)
 * @author cheyingkun
 */
public class TCardMountManager
{
	
	/**保存的文件名*/
	private final String MOUNT_FILE_NAME = "TCardMount.log";
	private final String TAG = "TCardMount";
	/**挂载信息map key为intent value为灰化以后的bitmap*/
	private Map<Intent , Bitmap> mountInfo = new HashMap<Intent , Bitmap>();
	private Context mContext;
	//cheyingkun add start	//针对开关机会发送T卡挂载信息的手机,添加关机和保存信息的判断
	private boolean shuttingDown = false;//正在关机
	private static TCardMountManager mTCardMountManager;
	private Handler mHandlerForLauncher = null;//launcher类中的handler
	
	//	private boolean hasTCardMountInfo = false;//是否存在挂载信息(写完文件改为true,清空文件改为false)//cheyingkun del	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
	//cheyingkun add end
	private TCardMountManager(
			Context mContext )
	{
		this.mContext = mContext;
	}
	
	public static TCardMountManager getInstance(
			Context context )
	{
		if( mTCardMountManager == null && context != null )
		{
			synchronized( TCardMountManager.class )
			{
				if( mTCardMountManager == null && context != null )
				{
					mTCardMountManager = new TCardMountManager( context );
				}
			}
		}
		return mTCardMountManager;
	}
	
	//cheyingkun add start	//startloader_progressBar
	public void setLauncherHandler(
			Handler mHandler )
	{
		this.mHandlerForLauncher = mHandler;
	}
	//cheyingkun add end
	;
	
	/**
	 * 根据传入的ShortcutInfo给挂载信息map赋值
	 * @param shortcuts
	 * @return 不在T卡的应用列表,需要变亮
	 */
	public void setMountInfo(
			final ArrayList<ShortcutInfo> shortcuts )
	{
		//应用移动到sd卡时,会发送sd卡挂载信息,而且包名只有要挂在的应用一个,所以要判断是应用搬家,还是真正的挂载sd卡
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "TCardMount" , StringUtils.concat( " isSDCardExist: " , LauncherAppState.isSDCardExist() ) );
		//应用搬家时，通过log发现sd卡时存在的。这时，把原先的sd卡应用列表加到移动到sd卡的应用上
		if( !LauncherAppState.isSDCardExist() )
		{
			mountInfo.clear();//cheyingkun add	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
		}
		Set<Intent> keySet = mountInfo.keySet();
		//循环挂载信息,如果已经存在挂载信息,不重复put
		ArrayList<ComponentName> mountInfoCN = new ArrayList<ComponentName>();
		for( Intent intent : keySet )
		{
			mountInfoCN.add( intent.getComponent() );
		}
		for( ShortcutInfo shortcut : shortcuts )
		{
			//cheyingkun start	//修改T卡挂载逻辑,保存图标从以前的灰色图标变为保存正常的图标
			//			mountInfo.put( shortcut.getIntent() , shortcut.getIcon( new IconCache( mLauncher ) ) );//cheyingkun del
			if( !mountInfoCN.contains( shortcut.getIntent().getComponent() ) )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "intent ComponentName=" , shortcut.getIntent().getComponent().toString() ) );
				Bitmap icon = shortcut.getIcon();
				if( icon == null )
				{
					shortcut.updateIcon( LauncherAppState.getInstance().getIconCache() );//使用同一个iconcache
					icon = shortcut.getIcon();
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , " icon == null " );
				}
				mountInfo.put( shortcut.getIntent() , icon );//cheyingkun add//保存亮的图片
			}
			//cheyingkun end
		}
		//移除不在sd卡的挂载信息
		ArrayList<Intent> removeIntent = new ArrayList<Intent>();
		PackageManager packageManager = LauncherAppState.getInstance().getContext().getPackageManager();
		for( Intent intent : keySet )
		{
			if( !LauncherAppState.isAppInstalledSdcard( intent.getComponent().getPackageName() , packageManager ) )//如果不在sd卡,则移除intent的key
			{
				removeIntent.add( intent );
			}
		}
		for( Intent intent : removeIntent )
		{
			mountInfo.remove( intent );
		}
	}
	
	/**
	 * 从文件中读取挂载信息到挂载信息的map中
	 */
	public void readFromFile()
	{
		synchronized( TCardMountManager.class )//cheyingkun add	//TCardMountUpdateAppBitmapOptimization(读写文件添加同步保护)
		{
			mountInfo.clear();
			DataInputStream statsMount = null;
			try
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "开始读取文件" );
				statsMount = new DataInputStream( mContext.openFileInput( MOUNT_FILE_NAME ) );
				int N = statsMount.readInt();
				for( int i = 0 ; i < N ; i++ )
				{
					String strIntent = statsMount.readUTF();//读取intent信息
					int bytesLength = statsMount.readInt();//读取bytes长度
					byte[] bytes = new byte[bytesLength];
					statsMount.read( bytes );//读取固定长度的bytes
					try
					{
						Intent intent = Intent.parseUri( strIntent , 0 );
						mountInfo.put( intent , BytesToBimap( bytes ) );
					}
					catch( URISyntaxException e )
					{
						e.printStackTrace();
					}
				}
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "读取文件结束: " , N ) );//cheyingkun add
			}
			catch( FileNotFoundException e )
			{
				// not a problem
			}
			catch( IOException e )
			{
				// more of a problem
			}
			finally
			{
				if( statsMount != null )
				{
					try
					{
						statsMount.close();
					}
					catch( IOException e )
					{
					}
				}
			}
		}
	}
	
	/**
	 * 把挂载信息的map写到文件中
	 */
	public void writeToFile()
	{
		synchronized( TCardMountManager.class )//cheyingkun add	//TCardMountUpdateAppBitmapOptimization(读写文件添加同步保护)
		{
			removeRepeatMountInfo();//删除mountInfo中重复ComponentName的项	//cheyingkun add	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
			DataOutputStream statsMount = null;
			try
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "开始写入文件" );
				statsMount = new DataOutputStream( mContext.openFileOutput( MOUNT_FILE_NAME , Context.MODE_PRIVATE ) );
				Set<Intent> keySet = mountInfo.keySet();
				int n = mountInfo.size();
				statsMount.writeInt( n );
				for( Intent intent : keySet )
				{
					statsMount.writeUTF( intent.toUri( 0 ) );//写入intent序列化后的字符串
					byte[] bytes = BitmapToBytes( mountInfo.get( intent ) );
					statsMount.writeInt( bytes.length );//写入bytes的长度
					statsMount.write( bytes );//写入bites
				}
				statsMount.close();
				statsMount = null;
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "写入文件结束: " , n ) );//cheyingkun add
			}
			catch( FileNotFoundException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "unable to create statsMount data: " + e.toString() ) );
			}
			catch( IOException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "unable to write to statsMount data: " + e.toString() ) );
			}
			finally
			{
				if( statsMount != null )
				{
					try
					{
						statsMount.close();
					}
					catch( IOException e )
					{
					}
				}
			}
		}
	}
	
	/**
	 * Bitmap转byte[]  
	 * @param bmp 要转换的bitmap
	 * @return
	 */
	public byte[] BitmapToBytes(
			Bitmap bmp )
	{
		return ItemInfo.flattenBitmap( bmp );
	}
	
	/**
	 * byte数组转换成bitmap
	 * @param b
	 * @return
	 */
	public Bitmap BytesToBimap(
			byte[] b )
	{
		if( b != null && b.length != 0 )//cheyingkun add(添加非空判断)
		{
			return BitmapFactory.decodeByteArray( b , 0 , b.length );
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * 清空挂载信息
	 * @throws FileNotFoundException 
	 */
	public void clearMountInfoAddFile()
	{
		mountInfo.clear();
		DataOutputStream clearFile = null;
		try
		{
			clearFile = new DataOutputStream( mContext.openFileOutput( MOUNT_FILE_NAME , Context.MODE_PRIVATE ) );
			clearFile.writeUTF( "" );
			clearFile.close();
			clearFile = null;
		}
		catch( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if( clearFile != null )
			{
				try
				{
					clearFile.close();
				}
				catch( IOException e )
				{
				}
			}
		}
	}
	
	public Map<Intent , Bitmap> getMountInfo()
	{
		return mountInfo;
	}
	
	/**
	 * 判断packageName的应用是否安装在T卡
	 * @param apps	T卡的应用列表
	 * @param packageName	要判断应用的packageName
	 * @return true 安装在T卡,false 安装在内存
	 */
	public boolean isAppInstalledInTCard(
			ArrayList<AppInfo> apps ,
			String packageName )
	{
		if( apps == null || apps.size() == 0 || packageName == null )
		{
			return false;
		}
		for( AppInfo appInfo : apps )
		{
			Intent intent = appInfo.getIntent();
			if( intent != null )
			{
				ComponentName component = intent.getComponent();
				if( component != null )
				{
					if( component.getPackageName().equals( packageName ) )
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	//cheyingkun add start	//针对开关机会发送T卡挂载信息的手机,添加关机和保存信息的判断
	//cheyingkun del start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
	//	public boolean getHasTCardMountInfo()
	//	{
	//		return hasTCardMountInfo;
	//	}
	//	
	//	public void setHasTCardMountInfo(
	//			boolean hasTCardMountInfo )
	//	{
	//		this.hasTCardMountInfo = hasTCardMountInfo;
	//	}
	//cheyingkun del end
	public boolean getShuttingDown()
	{
		return shuttingDown;
	}
	
	public void setShuttingDown(
			boolean shuttingDown )
	{
		this.shuttingDown = shuttingDown;
	}
	
	/**
	 * 是否响应T卡挂载
	 * @return 如果正在关机并且已经保存了挂载信息,返回false 否则返回true.false 不响应,true 响应*/
	public boolean canTCardMount()
	{
		//cheyingkun start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
		//		return !( hasTCardMountInfo && shuttingDown );//cheyingkun del
		return !shuttingDown;//如果正在关机,不进行T卡挂载和安装//cheyingkun add
		//cheyingkun end
	}
	
	//cheyingkun add end
	//cheyingkun add start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
	/**
	 * 初始化挂载信息文件
	 * @param sdCardAllApps 
	 */
	public void initSDCardAppsToFile(
			ArrayList<AppInfo> sdCardAllApps )
	{
		Set<Intent> keySet = mountInfo.keySet();
		ArrayList<ComponentName> componentName = new ArrayList<ComponentName>();
		for( Intent intent : keySet )
		{
			componentName.add( intent.getComponent() );
		}
		for( AppInfo appInfo : sdCardAllApps )
		{
			if( appInfo.getIconBitmap() != null && appInfo.getIntent() != null )
			{
				if( !componentName.contains( appInfo.getComponentName() ) )
				{
					mountInfo.put( appInfo.getIntent() , appInfo.getIconBitmap() );
				}
			}
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "TCardMount" , StringUtils.concat( "mountInfo.size: " , mountInfo.size() ) );
		if( mountInfo.size() > 0 )
		{
			writeToFile();
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
	private void removeRepeatMountInfo()
	{
		if( mountInfo != null && mountInfo.size() > 0 )
		{
			ArrayList<ComponentName> mComponentName = new ArrayList<ComponentName>();
			ArrayList<Intent> removeIntent = new ArrayList<Intent>();
			Set<Intent> keySet = mountInfo.keySet();
			for( Intent intent : keySet )
			{
				ComponentName component = intent.getComponent();
				if( !mComponentName.contains( component ) )
				{
					mComponentName.add( component );
				}
				else
				{
					removeIntent.add( intent );
				}
			}
			for( Intent intent : removeIntent )
			{
				mountInfo.remove( intent );
			}
		}
	}
	
	/**
	 * 根据传入的包名删除对应的挂载信息
	 */
	public void removeMountInfoByPackageName(
			ArrayList<String> packageName )
	{
		if( mountInfo != null && packageName != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "TCardMount" , StringUtils.concat( "mountInfo.size() : " , mountInfo.size() ) );
				Log.d( "TCardMount" , StringUtils.concat( "packageName.size() : " , packageName.size() ) );
			}
			ArrayList<Intent> removeIntent = findIntentInMountInfo( packageName );
			for( Intent intent : removeIntent )
			{
				mountInfo.remove( intent );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "mountInfo.remove : " , intent.getComponent() ) );
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "TCardMount" , StringUtils.concat( "mountInfo.size() : " , mountInfo.size() ) );
		}
	}
	
	//cheyingkun add end
	/**
	 * 根据包名查询挂载信息中的intent
	 * @param packageName 包名
	 * @return 返回包名相同的intent列表
	 */
	public ArrayList<Intent> findIntentInMountInfo(
			ArrayList<String> packageName )
	{
		ArrayList<Intent> findIntent = new ArrayList<Intent>();
		if( mountInfo != null && mountInfo.size() > 0 )
		{
			Set<Intent> mountIntentSet = mountInfo.keySet();
			for( Intent intent : mountIntentSet )
			{
				if( packageName.contains( intent.getComponent().getPackageName() ) )//如果挂载信息的包名在报名列表中
				{
					findIntent.add( intent );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "findIntent.add( : " , intent.toUri( 0 ) ) );
				}
			}
		}
		return findIntent;
	}
	
	/**
	 * 根据传入的列表信息发送消息,更新应用图标
	 * @param unavailable 要更新的应用图标的列表
	 */
	public void sendRefreshAppBitmapMessage(
			ArrayList<AppInfo> unavailable )
	{
		//cheyingkun add start	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
		if( mHandlerForLauncher != null )
		{
			Message msg = new Message();
			msg.obj = unavailable.clone();
			msg.what = Launcher.TCARDMOUNT_UPDATE_APP_BITMAP;
			mHandlerForLauncher.sendMessage( msg );
		}
		//cheyingkun add end
		unavailable.clear();//cheyingkun add	//RestartMobileTCardAppDismiss bug:0009466
	}
	
	/**
	 * ShortcutInfo是否包含在apps中(通过className比较)
	 * @return
	 */
	public boolean appsContainsOf(
			ShortcutInfo info ,
			List<ResolveInfo> apps )
	{
		if( info == null || info.getIntent() == null || info.getIntent().getComponent() == null )
		{
			return false;
		}
		String classname = info.getIntent().getComponent().getClassName();
		if( classname == null )
		{
			return false;
		}
		// gaominghui@2016/12/07 ADD START 重启手机偶现桌面报停【自测】
		if( apps != null )
		{
			for( ResolveInfo app : apps )
			{
				if( app.activityInfo != null && classname.equals( app.activityInfo.name ) )
				{
					return true;
				}
			}
		}
		// gaominghui@2016/12/07 ADD END  重启手机偶现桌面报停【自测】
		return false;
	}
	//cheyingkun add end
}
//cheyingkun add end