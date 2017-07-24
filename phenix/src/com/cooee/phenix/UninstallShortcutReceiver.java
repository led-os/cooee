package com.cooee.phenix;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.util.ZhiKeShortcutManager;


public class UninstallShortcutReceiver extends BroadcastReceiver
{
	
	private static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
	// The set of shortcuts that are pending uninstall
	private static ArrayList<PendingUninstallShortcutInfo> mUninstallQueue = new ArrayList<PendingUninstallShortcutInfo>();
	// Determines whether to defer uninstalling shortcuts immediately until
	// disableAndFlushUninstallQueue() is called.
	private static boolean mUseUninstallQueue = false;
	private static ArrayList<Integer> removeItemFolderIdList = new ArrayList<Integer>();//cheyingkun add	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(删除的图标在文件夹里)
	
	private static class PendingUninstallShortcutInfo
	{
		
		Intent data;
		
		public PendingUninstallShortcutInfo(
				Intent rawData )
		{
			data = rawData;
		}
	}
	
	public void onReceive(
			Context context ,
			Intent data )
	{
		if( !ACTION_UNINSTALL_SHORTCUT.equals( data.getAction() ) )
		{
			return;
		}
		//cheyingkun add start	//解决“连续点击发送双开应用广播的按钮，回到桌面后图标和开关状态不匹配”的问题【c_0004466】
		//把LauncheronPause之后的zhike双开广播过滤出来并保存,在onResume时处理
		Intent intent = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_INTENT );
		if( intent != null )
		{
			ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( context );
			if( mZhiKeShortcutManager.isLauncherPaused() && mZhiKeShortcutManager.isZhiKeShortcut( intent.getComponent() ) )
			{
				mZhiKeShortcutManager.addIntentToShortcutIntentList( data );
				return;
			}
		}
		//cheyingkun add end
		PendingUninstallShortcutInfo info = new PendingUninstallShortcutInfo( data );
		if( mUseUninstallQueue )
		{
			mUninstallQueue.add( info );
		}
		else
		{
			processUninstallShortcut( context , info );
		}
	}
	
	static void enableUninstallQueue()
	{
		mUseUninstallQueue = true;
	}
	
	static void disableAndFlushUninstallQueue(
			Context context )
	{
		mUseUninstallQueue = false;
		Iterator<PendingUninstallShortcutInfo> iter = mUninstallQueue.iterator();
		while( iter.hasNext() )
		{
			processUninstallShortcut( context , iter.next() );
			iter.remove();
		}
	}
	
	private static void processUninstallShortcut(
			Context context ,
			PendingUninstallShortcutInfo pendingInfo )
	{
		final Intent data = pendingInfo.data;
		LauncherAppState.setApplicationContext( context.getApplicationContext() );
		LauncherAppState app = LauncherAppState.getInstance();
		synchronized( app )
		{ // TODO: make removeShortcut internally threadsafe
			removeShortcut( context , data );
		}
	}
	
	private static void removeShortcut(
			Context context ,
			Intent data )
	{
		Intent intent = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_INTENT );
		String name = data.getStringExtra( Intent.EXTRA_SHORTCUT_NAME );
		boolean duplicate = data.getBooleanExtra( Launcher.EXTRA_SHORTCUT_DUPLICATE , true );
		if( intent != null && name != null )
		{
			final ContentResolver cr = context.getContentResolver();
			Cursor c = cr.query(
					LauncherSettings.Favorites.CONTENT_URI ,
					new String[]{ LauncherSettings.Favorites._ID , LauncherSettings.Favorites.INTENT , LauncherSettings.Favorites.CONTAINER } ,
					StringUtils.concat( LauncherSettings.Favorites.TITLE , "=?" ) ,
					new String[]{ name } ,
					null );
			//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(默认配置title额外查询)
			ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( context );
			if(
			//
			( intent != null/* //cheyingkun add	//解决“添加插件后删除，桌面重启”的问题【i_0014438】(非空判断)*/)
			//
			&& mZhiKeShortcutManager.isZhiKeShortcut( intent.getComponent() )
			//
			)
			{
				//xiatian add start	//解决“在桌面有双开应用时，进入设置关闭双开，再不返回到桌面的前提下重启手机，重启后桌面“最终”不显示双开图标（先显示双开应用，后删除双开图标），但是设置中的双开开关为开”的问题。
				String zhikeShortcutSettingKey = mZhiKeShortcutManager.getZhikeShortcutSettingKey( intent );
				mZhiKeShortcutManager.setZhikeShortcutSettingValues( zhikeShortcutSettingKey , 0 );
				//xiatian add end
				if( c.getCount() <= 0 )
				{
					String zhikeShortcutResourceTitle = mZhiKeShortcutManager.getZhikeShortcutResourceTitle( name );
					//默认配置的快捷方式,title不是应用名称,已经经过了处理,这里要额外查询
					c = cr.query(
							LauncherSettings.Favorites.CONTENT_URI ,
							new String[]{ LauncherSettings.Favorites._ID , LauncherSettings.Favorites.INTENT , LauncherSettings.Favorites.CONTAINER } ,
							StringUtils.concat( LauncherSettings.Favorites.TITLE , "=?" ) ,
							new String[]{ zhikeShortcutResourceTitle } ,
							null );
				}
			}
			final int containerIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CONTAINER );//cheyingkun add	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(删除的图标在文件夹里)
			//cheyingkun add end
			final int intentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.INTENT );
			final int idIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites._ID );
			boolean changed = false;
			try
			{
				while( c.moveToNext() )
				{
					try
					{
						//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(log)
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Intent intentForQuery = Intent.parseUri( c.getString( intentIndex ) , 0 );
							filterEquals( intent , intentForQuery );
						}
						//cheyingkun add end
						if( intent.filterEquals( Intent.parseUri( c.getString( intentIndex ) , 0 ) ) )
						{
							final long id = c.getLong( idIndex );
							final Uri uri = LauncherSettings.Favorites.getContentUri( id , false );
							cr.delete( uri , null , null );
							changed = true;
							//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(删除的图标在文件夹里)
							//删除文件夹里的图标,需要判断是否需要解散文件夹
							final long container = c.getLong( containerIndex );
							if( container != LauncherSettings.Favorites.CONTAINER_DESKTOP && container != LauncherSettings.Favorites.CONTAINER_HOTSEAT )
							{
								if( !removeItemFolderIdList.contains( container ) )
								{
									removeItemFolderIdList.add( (int)container );
								}
							}
							//cheyingkun add end
							if( !duplicate )
							{
								break;
							}
						}
					}
					catch( URISyntaxException e )
					{
						// Ignore
					}
				}
			}
			finally
			{
				c.close();
			}
			if( changed )
			{
				cr.notifyChange( LauncherSettings.Favorites.CONTENT_URI , null );
				Toast.makeText( context , context.getString( R.string.shortcut_uninstalled , name ) , Toast.LENGTH_SHORT ).show();//本地化，待修改。
			}
		}
	}
	
	//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(log)
	private static void filterEquals(
			Intent intent1 ,
			Intent intent2 )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			ComponentName component1 = intent1.getComponent();
			String action1 = intent1.getAction();
			String type1 = intent1.getType();
			String package1 = intent1.getPackage();
			Set<String> categories1 = intent1.getCategories();
			String dataString1 = intent1.getDataString();
			//
			ComponentName component2 = intent2.getComponent();
			String action2 = intent2.getAction();
			String type2 = intent2.getType();
			String package2 = intent2.getPackage();
			Set<String> categories2 = intent2.getCategories();
			String dataString2 = intent2.getDataString();
			Log.d( "" , StringUtils.concat( " cyk filterEquals component1: " , component1.toString() ) );
			Log.w( "" , StringUtils.concat( " cyk filterEquals component2: " , component2.toString() ) );
			Log.d( "" , StringUtils.concat( " cyk filterEquals action1: " , action1 ) );
			Log.w( "" , StringUtils.concat( " cyk filterEquals action2: " , action2 ) );
			Log.d( "" , StringUtils.concat( " cyk filterEquals type1: " , type1 ) );
			Log.w( "" , StringUtils.concat( " cyk filterEquals type2: " , type2 ) );
			Log.d( "" , StringUtils.concat( " cyk filterEquals package1: " , package1 ) );
			Log.w( "" , StringUtils.concat( " cyk filterEquals package2: " , package2 ) );
			Log.d( "" , StringUtils.concat( " cyk filterEquals categories1: " , categories1.toArray() ) );
			Log.w( "" , StringUtils.concat( " cyk filterEquals categories2: " , categories2.toArray() ) );
			Log.d( "" , StringUtils.concat( " cyk filterEquals dataString1: " , dataString1 ) );
			Log.w( "" , StringUtils.concat( " cyk filterEquals dataString2: " , dataString2 ) );
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(删除的图标在文件夹里)
	public static ArrayList<Integer> getRemoveItemFolderIdList()
	{
		return removeItemFolderIdList;
	}
	
	public static void setRemoveItemFolderIdList(
			ArrayList<Integer> removeItemFolderIdList )
	{
		UninstallShortcutReceiver.removeItemFolderIdList = removeItemFolderIdList;
	}
	//cheyingkun add end
}
