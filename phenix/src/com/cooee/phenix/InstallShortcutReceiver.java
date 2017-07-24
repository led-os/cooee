package com.cooee.phenix;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.PendingInstallShortcutInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.phenix.util.ZhiKeShortcutManager;
import com.iLoong.launcher.MList.MeLauncherInterface;


public class InstallShortcutReceiver extends BroadcastReceiver
{
	
	private static final String TAG = "InstallShortcutReceiver";
	private static final boolean DBG = false;
	public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	public static final String DATA_INTENT_KEY = "intent.data";
	public static final String LAUNCH_INTENT_KEY = "intent.launch";
	public static final String NAME_KEY = "name";
	public static final String ICON_KEY = "icon";
	public static final String ICON_RESOURCE_NAME_KEY = "iconResource";
	public static final String ICON_RESOURCE_PACKAGE_NAME_KEY = "iconResourcePackage";
	// The set of shortcuts that are pending install
	public static final String APPS_PENDING_INSTALL = "apps_to_install";
	public static final int NEW_SHORTCUT_BOUNCE_DURATION = 450;
	public static final int NEW_SHORTCUT_STAGGER_DELAY = 85;
	private static final int INSTALL_SHORTCUT_SUCCESSFUL = 0;
	private static final int INSTALL_SHORTCUT_IS_DUPLICATE = -1;
	// A mime-type representing shortcut data
	public static final String SHORTCUT_MIMETYPE = "com.cooee.phenix/shortcut";
	private static Object sLock = new Object();
	
	private static void addToStringSet(
			SharedPreferences sharedPrefs ,
			SharedPreferences.Editor editor ,
			String key ,
			String value )
	{
		Set<String> strings = sharedPrefs.getStringSet( key , null );
		if( strings == null )
		{
			strings = new HashSet<String>( 0 );
		}
		else
		{
			strings = new HashSet<String>( strings );
		}
		strings.add( value );
		editor.putStringSet( key , strings );
	}
	
	private static void addToInstallQueue(
			SharedPreferences sharedPrefs ,
			PendingInstallShortcutInfo info )
	{
		synchronized( sLock )
		{
			try
			{
				JSONStringer json = new JSONStringer().object().key( DATA_INTENT_KEY ).value( info.getData().toUri( 0 ) ).key( LAUNCH_INTENT_KEY ).value( info.getLaunchIntent().toUri( 0 ) )
						.key( NAME_KEY ).value( info.getName() );
				if( info.getIcon() != null )
				{
					byte[] iconByteArray = ItemInfo.flattenBitmap( info.getIcon() );
					json = json.key( ICON_KEY ).value( Base64.encodeToString( iconByteArray , 0 , iconByteArray.length , Base64.DEFAULT ) );
				}
				if( info.getIconResource() != null )
				{
					json = json.key( ICON_RESOURCE_NAME_KEY ).value( info.getIconResource().resourceName );
					json = json.key( ICON_RESOURCE_PACKAGE_NAME_KEY ).value( info.getIconResource().packageName );
				}
				json = json.endObject();
				SharedPreferences.Editor editor = sharedPrefs.edit();
				if( DBG )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "Adding to APPS_PENDING_INSTALL: " , json.toString() ) );
				addToStringSet( sharedPrefs , editor , APPS_PENDING_INSTALL , json.toString() );
				editor.commit();
			}
			catch( org.json.JSONException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "Exception when adding shortcut: " , e.toString() ) );
			}
		}
	}
	
	public static void removeFromInstallQueue(
			SharedPreferences sharedPrefs ,
			ArrayList<String> packageNames )
	{
		synchronized( sLock )
		{
			Set<String> strings = sharedPrefs.getStringSet( APPS_PENDING_INSTALL , null );
			if( DBG )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "APPS_PENDING_INSTALL: " , strings , ", removing packages: " , packageNames ) );
			}
			if( strings != null )
			{
				Set<String> newStrings = new HashSet<String>( strings );
				Iterator<String> newStringsIter = newStrings.iterator();
				while( newStringsIter.hasNext() )
				{
					String json = newStringsIter.next();
					try
					{
						JSONObject object = (JSONObject)new JSONTokener( json ).nextValue();
						Intent launchIntent = Intent.parseUri( object.getString( LAUNCH_INTENT_KEY ) , 0 );
						String pn = launchIntent.getPackage();
						if( pn == null )
						{
							pn = launchIntent.getComponent().getPackageName();
						}
						if( packageNames.contains( pn ) )
						{
							newStringsIter.remove();
						}
					}
					catch( org.json.JSONException e )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , StringUtils.concat( "Exception reading shortcut to remove: " , e.toString() ) );
					}
					catch( java.net.URISyntaxException e )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , StringUtils.concat( "Exception reading shortcut to remove: " , e.toString() ) );
					}
				}
				sharedPrefs.edit().putStringSet( APPS_PENDING_INSTALL , new HashSet<String>( newStrings ) ).commit();
			}
		}
	}
	
	private static ArrayList<PendingInstallShortcutInfo> getAndClearInstallQueue(
			SharedPreferences sharedPrefs )
	{
		synchronized( sLock )
		{
			Set<String> strings = sharedPrefs.getStringSet( APPS_PENDING_INSTALL , null );
			if( DBG )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "Getting and clearing APPS_PENDING_INSTALL: " , strings.toArray() ) );
			if( strings == null )
			{
				return new ArrayList<PendingInstallShortcutInfo>();
			}
			ArrayList<PendingInstallShortcutInfo> infos = new ArrayList<PendingInstallShortcutInfo>();
			for( String json : strings )
			{
				try
				{
					JSONObject object = (JSONObject)new JSONTokener( json ).nextValue();
					Intent data = Intent.parseUri( object.getString( DATA_INTENT_KEY ) , 0 );
					Intent launchIntent = Intent.parseUri( object.getString( LAUNCH_INTENT_KEY ) , 0 );
					String name = object.getString( NAME_KEY );
					String iconBase64 = object.optString( ICON_KEY );
					String iconResourceName = object.optString( ICON_RESOURCE_NAME_KEY );
					String iconResourcePackageName = object.optString( ICON_RESOURCE_PACKAGE_NAME_KEY );
					if( iconBase64 != null && !iconBase64.isEmpty() )
					{
						byte[] iconArray = Base64.decode( iconBase64 , Base64.DEFAULT );
						Bitmap b = BitmapFactory.decodeByteArray( iconArray , 0 , iconArray.length );
						data.putExtra( Intent.EXTRA_SHORTCUT_ICON , b );
					}
					else if( iconResourceName != null && !iconResourceName.isEmpty() )
					{
						Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();
						iconResource.resourceName = iconResourceName;
						iconResource.packageName = iconResourcePackageName;
						data.putExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE , iconResource );
					}
					data.putExtra( Intent.EXTRA_SHORTCUT_INTENT , launchIntent );
					PendingInstallShortcutInfo info = new PendingInstallShortcutInfo( data , name , launchIntent );
					infos.add( info );
				}
				catch( org.json.JSONException e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "Exception reading shortcut to add: " , e.toString() ) );
				}
				catch( java.net.URISyntaxException e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "Exception reading shortcut to add: " , e.toString() ) );
				}
			}
			sharedPrefs.edit().putStringSet( APPS_PENDING_INSTALL , new HashSet<String>() ).commit();
			return infos;
		}
	}
	
	// Determines whether to defer installing shortcuts immediately until
	// processAllPendingInstalls() is called.
	private static boolean mUseInstallQueue = false;
	
	public void onReceive(
			Context context ,
			Intent data )
	{
		if( !LauncherDefaultConfig.SWITCH_ENABLE_SHOW_APP_AUTO_CREATE_SHORTCUT //cheyingkun add	//是否显示应用自动创建的快捷方式【c_0003466】
				|| !ACTION_INSTALL_SHORTCUT.equals( data.getAction() ) )
		{
			return;
		}
		if( DBG )
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Got INSTALL_SHORTCUT: " , data.toUri( 0 ) ) );
		Intent intent = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_INTENT );
		if( intent == null )
		{
			return;
		}
		//cheyingkun add start	//解决“连续点击发送双开应用广播的按钮，回到桌面后图标和开关状态不匹配”的问题【c_0004466】
		//把LauncheronPause之后的zhike双开广播过滤出来并保存,在onResume时处理
		ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( context );
		if( mZhiKeShortcutManager.isLauncherPaused() && mZhiKeShortcutManager.isZhiKeShortcut( intent.getComponent() ) )
		{
			mZhiKeShortcutManager.addIntentToShortcutIntentList( data );
			return;
		}
		//cheyingkun add end
		// This name is only used for comparisons and notifications, so fall back to activity name
		// if not supplied
		String name = data.getStringExtra( Intent.EXTRA_SHORTCUT_NAME );
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			if( intent.getComponent() != null )
			{
				ComponentName comp = intent.getComponent();
				if( comp.getPackageName().equals( context.getPackageName() ) )
				{
					if( MeLauncherInterface.getInstance().MeIsMicroEntry( comp.getClassName() ) )
					{
						return;
					}
				}
			}
		}
		if( name == null )
		{
			try
			{
				PackageManager pm = context.getPackageManager();
				ActivityInfo info = pm.getActivityInfo( intent.getComponent() , 0 );
				name = info.loadLabel( pm ).toString();
			}
			catch( PackageManager.NameNotFoundException nnfe )
			{
				return;
			}
		}
		//xiatian start	//添加保护，防止强转抛异常。
		//xiatian del start
		//		Bitmap icon = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON );
		//		Intent.ShortcutIconResource iconResource = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE );
		//xiatian del end
		//xiatian add start
		Bitmap icon = null;
		Parcelable bitmap = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON );
		if( bitmap != null && bitmap instanceof Bitmap )
		{
			icon = (Bitmap)bitmap;
		}
		Intent.ShortcutIconResource iconResource = null;
		Parcelable iconParcel = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE );
		if( iconParcel != null && iconParcel instanceof Intent.ShortcutIconResource )
		{
			iconResource = (Intent.ShortcutIconResource)iconParcel;
		}
		//xiatian add end
		//xiatian end
		// Queue the item up for adding if launcher has not loaded properly yet
		LauncherAppState.setApplicationContext( context.getApplicationContext() );
		LauncherAppState app = LauncherAppState.getInstance();
		boolean launcherNotLoaded = ( app.getDynamicGrid() == null );
		PendingInstallShortcutInfo info = new PendingInstallShortcutInfo( data , name , intent );
		info.setIcon( icon );
		info.setIconResource( iconResource );
		String spKey = LauncherAppState.getSharedPreferencesKey();
		SharedPreferences sp = context.getSharedPreferences( spKey , Context.MODE_PRIVATE );
		addToInstallQueue( sp , info );
		if( !mUseInstallQueue && !launcherNotLoaded )
		{
			flushInstallQueue( context );
		}
	}
	
	static void enableInstallQueue()
	{
		mUseInstallQueue = true;
	}
	
	static void disableAndFlushInstallQueue(
			Context context )
	{
		mUseInstallQueue = false;
		if( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_APP_AUTO_CREATE_SHORTCUT )//cheyingkun add	//是否显示应用自动创建的快捷方式【c_0003466】
		{
			flushInstallQueue( context );
		}
	}
	
	static void flushInstallQueue(
			Context context )
	{
		String spKey = LauncherAppState.getSharedPreferencesKey();
		SharedPreferences sp = context.getSharedPreferences( spKey , Context.MODE_PRIVATE );
		ArrayList<PendingInstallShortcutInfo> installQueue = getAndClearInstallQueue( sp );
		if( !installQueue.isEmpty() )
		{
			Iterator<PendingInstallShortcutInfo> iter = installQueue.iterator();
			ArrayList<ItemInfo> addShortcuts = new ArrayList<ItemInfo>();
			ArrayList<ItemInfo> addShortcutsNeed2Combine = new ArrayList<ItemInfo>();//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
			int result = INSTALL_SHORTCUT_SUCCESSFUL;
			String duplicateName = "";
			while( iter.hasNext() )
			{
				final PendingInstallShortcutInfo pendingInfo = iter.next();
				//final Intent data = pendingInfo.data;
				final Intent intent = pendingInfo.getLaunchIntent();
				final String name = pendingInfo.getName();
				//cheyingkun start	//应用自动创建快捷方式时，优化判断是否存在的方法。【c_0003813】
				//cheyingkun del start
				//				//xiatian start	//fix bug：解决“单层模式下，多米音乐会自动生成一个和原图标入口相同的快捷方式”的问题。【i_0010976】
				//				//				final boolean exists = LauncherModel.shortcutExists( context , name , intent );//xiatian del
				//				exists = LauncherModel.shortcutExistsIntensify( context , name , intent );//xiatian add
				//				//xiatian end
				//cheyingkun del end
				final boolean exists = LauncherAppState.getInstance().getModel().shortcutExistsByWorkspaceItems( name , intent );//cheyingkun add
				//cheyingkun end
				// yangxiaoming start 2015-05-18 由于i_0011035难复现，特在此打上Log
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "i_0011035" , StringUtils.concat( "InstallShortcutReceiver - flushInstallQueue - name:" , name , "-exists:" , exists ) );
					//					Launcher.getLogCat( "i_0011035" );
				}
				// yangxiaoming end
				//final boolean allowDuplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);
				// TODO-XXX: Disable duplicates for now
				if( !exists /* && allowDuplicate */)
				{
					// Generate a shortcut info to add into the model
					ShortcutInfo info = getShortcutInfo( context , pendingInfo.getData() , pendingInfo.getLaunchIntent() );
					addShortcuts.add( info );
					//xiatian add start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
					addShortcutsNeed2Combine.add( info );//接受广播“ACTION_INSTALL_SHORTCUT”，自动生成快捷方式时，若是EXTRA_SHORTCUT_ICON，则在将原始图片存入数据库后，再对info的icon加背板、蒙版和盖板
					//xiatian add end
				}
				/*
				else if (exists && !allowDuplicate) {
				    result = INSTALL_SHORTCUT_IS_DUPLICATE;
				    duplicateName = name;
				}
				*/
			}
			// Notify the user once if we weren't able to place any duplicates
			if( result == INSTALL_SHORTCUT_IS_DUPLICATE )
			{
				Toast.makeText( context , context.getString( R.string.shortcut_duplicate , duplicateName ) , Toast.LENGTH_SHORT ).show();//本地化，待修改。
			}
			// Add the new apps to the model and bind them
			if( !addShortcuts.isEmpty() )
			{
				LauncherAppState app = LauncherAppState.getInstance();
				//xiatian start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
				//接受广播“ACTION_INSTALL_SHORTCUT”，自动生成快捷方式时，若是EXTRA_SHORTCUT_ICON，则在将原始图片存入数据库后，再对info的icon加背板、蒙版和盖板
				//				app.getModel().addAndBindAddedApps( context , addShortcuts , null );//xiatian del
				app.getModel().addAndBindAddedItems( context , addShortcuts , addShortcutsNeed2Combine , null , true , !app.getModel().isLoaderTaskRunning() );//xiatian add 
				//xiatian end
			}
		}
	}
	
	private static ShortcutInfo getShortcutInfo(
			Context context ,
			Intent data ,
			Intent launchIntent )
	{
		if( launchIntent.getAction() == null )
		{
			launchIntent.setAction( Intent.ACTION_VIEW );
		}
		else if( launchIntent.getAction().equals( Intent.ACTION_MAIN ) && launchIntent.getCategories() != null && launchIntent.getCategories().contains( Intent.CATEGORY_LAUNCHER ) )
		{
			launchIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		}
		LauncherAppState app = LauncherAppState.getInstance();
		return app.getModel().infoFromShortcutIntent( context , data , null );
	}
}
