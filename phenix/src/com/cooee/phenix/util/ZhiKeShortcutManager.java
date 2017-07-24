package com.cooee.phenix.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherProvider;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ItemInfo;


/**
 * cheyingkun add whole file	//配置可以通过广播删除的快捷方式【智科】【c_0004455】
 * */
public class ZhiKeShortcutManager
{
	
	private Context mContext;
	private static ZhiKeShortcutManager mZhiKeShortcutManager;
	private static String TAG = "ZhiKeShortcutManager";
	private ComponentName configZhikeShortcutComponentName = null;
	private String configZhikeShortcutIntentExtraKey = null;
	private HashMap<String , ZhikeShortcutConfigEntry> map = null;
	//cheyingkun add start	//解决“连续点击发送双开应用广播的按钮，回到桌面后图标和开关状态不匹配”的问题【c_0004466】
	ArrayList<Intent> shortcutIntentList = new ArrayList<Intent>();//保存添加删除广播的intent
	private boolean isLauncherPaused = false;//onPause状态标记
	private SharedPreferences mPreferences = null;
	public static final String DATA_INTENT_KEY = "intent.data";
	public static final String LAUNCH_INTENT_KEY = "intent.launch";
	public static final String ICON_KEY = "icon";
	public static final String ICON_RESOURCE_NAME_KEY = "iconResource";
	public static final String ICON_RESOURCE_PACKAGE_NAME_KEY = "iconResourcePackage";
	
	//cheyingkun add end
	private ZhiKeShortcutManager(
			Context mContext )
	{
		this.mContext = mContext;
		initConfig();
	}
	
	public static ZhiKeShortcutManager getInstance(
			Context mContext )
	{
		if( mZhiKeShortcutManager == null )
		{
			synchronized( TAG )
			{
				if( mZhiKeShortcutManager == null )
				{
					mZhiKeShortcutManager = new ZhiKeShortcutManager( mContext );
				}
			}
		}
		return mZhiKeShortcutManager;
	}
	
	private void initConfig()
	{
		//双开应用包类名
		configZhikeShortcutComponentName = ComponentName.unflattenFromString( mContext.getResources().getString( R.string.config_zhike_shortcut_componentName ) );
		//区分双开哪个应用的key
		configZhikeShortcutIntentExtraKey = mContext.getResources().getString( R.string.config_zhike_shortcut_intent_extra_key );
		map = new HashMap<String , ZhikeShortcutConfigEntry>();
		String[] stringArray = mContext.getResources().getStringArray( R.array.zhike_shortcut_config_values_list );
		for( String string : stringArray )
		{
			if( string != null )
			{
				String[] split = string.split( "," );
				if( split != null && split.length == 3 )
				{
					ZhikeShortcutConfigEntry values = new ZhikeShortcutConfigEntry( split[0] , split[1] , StringUtils.concat(
							LauncherProvider.ITEM_TITLE_RESOURCE_NAME_KEY ,
							"com.cooee.phenix:string/" ,
							split[2] ) );
					map.put( split[0] , values );
				}
			}
		}
		mPreferences = mContext.getSharedPreferences( "zhikeIntent" , Context.MODE_PRIVATE );
	}
	
	public boolean isZhiKeShortcut(
			ComponentName mComponentName )
	{
		if( mComponentName != null )
		{
			return mComponentName.equals( configZhikeShortcutComponentName );
		}
		return false;
	}
	
	/**
	 * 通过intent获取设置界面需要修改值的key
	 * @param mIntent
	 * @return
	 */
	public String getZhikeShortcutSettingKey(
			Intent mIntent )
	{
		if( mIntent != null && configZhikeShortcutIntentExtraKey != null )
		{
			String mapKey = mIntent.getStringExtra( configZhikeShortcutIntentExtraKey );
			if( map != null )
			{
				ZhikeShortcutConfigEntry zhikeShortcutConfigEntry = map.get( mapKey );
				if( zhikeShortcutConfigEntry != null )
				{
					return zhikeShortcutConfigEntry.getSettingKey();
				}
			}
		}
		return null;
	}
	
	/**
	 * 设置key对应的系统设置界面的开关状态
	 * @param key
	 */
	public void setZhikeShortcutSettingValues(
			String key ,
			int values )
	{
		if( key != null )
		{
			android.provider.Settings.System.putInt( mContext.getContentResolver() , key , values );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( " setZhikeShortcutSettingValues key:" , key , " values: " , values ) );
		}
	}
	
	/**
	 * 获取shortcut保存在数据库中的title
	 * @param shortcutName
	 * @return
	 */
	public String getZhikeShortcutResourceTitle(
			String shortcutName )
	{
		Collection<ZhikeShortcutConfigEntry> values = map.values();
		for( ZhikeShortcutConfigEntry zhikeShortcutConfigEntry : values )
		{
			//获取title
			String title = null;
			String resourceTitle = zhikeShortcutConfigEntry.getResourceTitle();
			Resources mResources = LauncherAppState.getInstance().getContext().getResources();
			String mTitleResourceName = resourceTitle.subSequence( LauncherProvider.ITEM_TITLE_RESOURCE_NAME_KEY.length() , resourceTitle.length() ).toString();
			int titleId = mResources.getIdentifier( mTitleResourceName , null , null );
			if( titleId > 0 )
			{
				title = LauncherDefaultConfig.getString( titleId );
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( " getZhikeShortcutResourceTitle title: " , title , " shortcutName: " , shortcutName ) );
			//如果title和shortcutName相同,则返回getResourceTitle
			//因为默认配置的快捷方式,数据库中的title存的是getResourceTitle
			if( title != null && title.equals( shortcutName ) )
			{
				return zhikeShortcutConfigEntry.getResourceTitle();
			}
		}
		return null;
	}
	
	class ZhikeShortcutConfigEntry
	{
		
		String appKey;//区分是哪个app
		String settingKey;//设置中的key
		String resourceTitle;//保存在数据库中的title
		
		public ZhikeShortcutConfigEntry(
				String appKey ,
				String settingKey ,
				String resourceTitle )
		{
			this.appKey = appKey;
			this.settingKey = settingKey;
			this.resourceTitle = resourceTitle;
		}
		
		public String getAppKey()
		{
			return appKey;
		}
		
		public String getSettingKey()
		{
			return settingKey;
		}
		
		public String getResourceTitle()
		{
			return resourceTitle;
		}
	}
	
	//cheyingkun add start	//解决“连续点击发送双开应用广播的按钮，回到桌面后图标和开关状态不匹配”的问题【c_0004466】
	public boolean isLauncherPaused()
	{
		return isLauncherPaused;
	}
	
	public void setLauncherPaused(
			boolean isLauncherPaused )
	{
		this.isLauncherPaused = isLauncherPaused;
	}
	
	/**
	 * 把LauncheronPause之后的zhike双开广播过滤出来并保存
	 * @param data
	 */
	public void addIntentToShortcutIntentList(
			Intent data )
	{
		showShortcutIntentList();
		//根据包类名和title匹配删除shortcutIntentList已有的intent
		Intent shortcutIntent_data = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_INTENT );
		String name_data = data.getStringExtra( Intent.EXTRA_SHORTCUT_NAME );
		ArrayList<Intent> reoveIntent = new ArrayList<Intent>();
		for( Intent intent : shortcutIntentList )
		{
			Intent shortcutIntent_list = intent.getParcelableExtra( Intent.EXTRA_SHORTCUT_INTENT );
			String name_list = intent.getStringExtra( Intent.EXTRA_SHORTCUT_NAME );
			if( shortcutIntent_data != null && shortcutIntent_list != null //
					&& shortcutIntent_data.getComponent() != null && shortcutIntent_list.getComponent() != null//
					&& shortcutIntent_data.getComponent().equals( shortcutIntent_list.getComponent() )//包类名匹配
					&& name_data != null && name_list != null && name_data.equals( name_list )//title匹配
			)
			{
				reoveIntent.add( intent );
			}
		}
		for( Intent intent : reoveIntent )
		{
			shortcutIntentList.remove( intent );
		}
		//把传入的intent加入shortcutIntentList
		shortcutIntentList.add( data );
		Set<String> sets = new HashSet<String>();
		for( Intent intent : shortcutIntentList )
		{
			String str = intent2String( intent );
			if( !TextUtils.isEmpty( str ) )
			{
				sets.add( str );
			}
		}
		mPreferences.edit().putStringSet( "shortcutIntentList" , sets ).commit();
		showShortcutIntentList();
	}
	
	/**
	 * 在onResume时处理shortcutIntentList广播
	 */
	public synchronized void clearShortcutIntentList()
	{
		showShortcutIntentList();
		if( shortcutIntentList.size() == 0 )
		{
			Set<String> temp = mPreferences.getStringSet( "shortcutIntentList" , null );
			if( temp != null )
			{
				for( String item : temp )
				{
					Intent intent = string2Intent( item );
					if( intent != null )
					{
						shortcutIntentList.add( intent );
					}
				}
			}
		}
		for( Intent intent : shortcutIntentList )
		{
			mContext.sendBroadcast( intent );
		}
		shortcutIntentList.clear();
		mPreferences.edit().remove( "shortcutIntentList" ).commit();
	}
	
	private void showShortcutIntentList()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( TAG , StringUtils.concat( " showShortcutIntentList: " , shortcutIntentList.size() ) );
			for( Intent intent : shortcutIntentList )
			{
				Log.d( TAG , StringUtils.concat( " intent: " , intent , " getExtras: " , intent.getExtras().toString() ) );
			}
			Log.e( TAG , " showShortcutIntentList: " );
		}
	}
	
	//cheyingkun add end
	private String intent2String(
			Intent intent )
	{
		try
		{
			JSONStringer json = new JSONStringer().object().key( DATA_INTENT_KEY ).value( intent.toUri( 0 ) ).key( LAUNCH_INTENT_KEY )
					.value( ( (Intent)intent.getParcelableExtra( Intent.EXTRA_SHORTCUT_INTENT ) ).toUri( 0 ) );
			Parcelable bitmap = intent.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON );
			Bitmap icon = null;
			if( bitmap != null && bitmap instanceof Bitmap )
			{
				icon = (Bitmap)bitmap;
			}
			Intent.ShortcutIconResource iconResource = null;
			Parcelable iconParcel = intent.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE );
			if( iconParcel != null && iconParcel instanceof Intent.ShortcutIconResource )
			{
				iconResource = (Intent.ShortcutIconResource)iconParcel;
			}
			if( icon != null )
			{
				byte[] iconByteArray = ItemInfo.flattenBitmap( icon );
				json = json.key( ICON_KEY ).value( Base64.encodeToString( iconByteArray , 0 , iconByteArray.length , Base64.DEFAULT ) );
			}
			if( iconResource != null )
			{
				json = json.key( ICON_RESOURCE_NAME_KEY ).value( iconResource.resourceName );
				json = json.key( ICON_RESOURCE_PACKAGE_NAME_KEY ).value( iconResource.packageName );
			}
			json = json.endObject();
			return json.toString();
		}
		catch( org.json.JSONException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Exception when adding shortcut - JSONException: " , e.toString() ) );
		}
		return null;
	}
	
	private Intent string2Intent(
			String json )
	{
		if( TextUtils.isEmpty( json ) )
		{
			return null;
		}
		try
		{
			JSONObject object = (JSONObject)new JSONTokener( json ).nextValue();
			Intent data = Intent.parseUri( object.getString( DATA_INTENT_KEY ) , 0 );
			Intent launchIntent = Intent.parseUri( object.getString( LAUNCH_INTENT_KEY ) , 0 );
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
			return data;
		}
		catch( org.json.JSONException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Exception reading shortcut to add - JSONException: " , e.toString() ) );
		}
		catch( java.net.URISyntaxException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Exception reading shortcut to add - URISyntaxException: " , e.toString() ) );
		}
		return null;
	}
	
	//xiatian add start	//解决“未智能分类模式下关闭双开开关的前提下，智能分类，并在智能分类模式打开双开开关，等双开图标创建后，再退出智能分类模式，这时桌面没双开图标但是系统设置中的双开开关是打开状态”的问题【c_0004640】
	public void closeAllZhikeShortcutSwitchs()
	{
		Collection<ZhikeShortcutConfigEntry> mZhikeShortcutConfigEntryList = map.values();
		for( ZhikeShortcutConfigEntry mZhikeShortcutConfigEntryItem : mZhikeShortcutConfigEntryList )
		{
			setZhikeShortcutSettingValues( mZhikeShortcutConfigEntryItem.getSettingKey() , 0 );
		}
	}
	//xiatian add end
}
