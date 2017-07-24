package com.cooee.framework.function.DynamicEntry;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import cool.sdk.DynamicEntry.DynamicEntryHelper;


public class OperateDynamicUnInstall
{
	
	private static final String PREFERENCE_UNINSTALL = "unInstallData";
	private static final String TIME_KEY = "removedTime";
	private static final String ID_KEY = "dynamic";
	private static final String FOLDERID_KEY = "folderID";
	private static final String FROM_KEY = "from";
	private static final String FROM_INSTALLED = "IsInstalled";
	private SharedPreferences mPref;
	private Context mContext;
	private OperateDynamicProxy mProxy;
	private ArrayList<UnInstallItem> mUnInstallItems = new ArrayList<UnInstallItem>();
	
	// 这里要区分不同位置的图标。如果from =FOLDER,  dynamicID为文件夹的dynamicID,根据这两项能够判断
	//ITEM 来自哪个文件夹 或者原来在桌面上
	// folderID对于来自文件夹的图标 ,folderid = dynamicID,非文件夹的
	// folderID = OperateDynamicUtils.INVALID_FOLDERID
	// isInstalled 仅仅对虚应用有用，表示是安装后，用户的卸载。还是没有安装的卸载。
	class UnInstallItem
	{
		
		public String mPackageName;
		public String dynamicID;
		public long mRemovedTime;
		public int mFrom;
		public String mFolderID;
		public boolean mIsInstalled;
		
		public UnInstallItem()
		{
		}
		
		public JSONObject toJSON()
		{
			JSONObject res = new JSONObject();
			try
			{
				res.put( ID_KEY , dynamicID );
				res.put( TIME_KEY , mRemovedTime );
				res.put( FROM_KEY , mFrom );
				res.put( FOLDERID_KEY , mFolderID );
				res.put( FROM_INSTALLED , mIsInstalled );
				return res;
			}
			catch( Exception e )
			{
				//e.printStackTrace();
			}
			return null;
		}
	}
	
	public OperateDynamicUnInstall(
			Context context ,
			SharedPreferences pref ,
			OperateDynamicProxy proxy )
	{
		mContext = context;
		mPref = pref;
		mProxy = proxy;
		initUnInstallItems();
	}
	
	private void initUnInstallItems()
	{
		mUnInstallItems.clear();
		//String content = mPref.getString( PREFERENCE_UNINSTALL , null );//mPref.getString( PREFERENCE_UNINSTALL , null );
		String content = DynamicEntryHelper.getInstance( mContext ).getString( PREFERENCE_UNINSTALL , null );//mPref.getString( PREFERENCE_UNINSTALL , null );
		if( content == null )
		{
			return;
		}
		JSONObject list;
		try
		{
			list = new JSONObject( content );
			Iterator<?> keys = (Iterator<?>)list.keys();
			String key;
			while( keys.hasNext() )
			{
				UnInstallItem data = new UnInstallItem();
				key = (String)keys.next();
				data.mPackageName = key;
				JSONObject item = list.getJSONObject( key );
				data.dynamicID = item.optString( ID_KEY );
				data.mRemovedTime = item.optLong( TIME_KEY );
				data.mFrom = item.optInt( FROM_KEY );
				data.mFolderID = item.optString( FOLDERID_KEY );
				data.mIsInstalled = item.optBoolean( FROM_INSTALLED );
				mUnInstallItems.add( data );
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	protected ArrayList<UnInstallItem> getUnInstallItems()
	{
		return mUnInstallItems;
	}
	
	// 这里要区分不同位置的图标。如果from =FOLDER,  dynamicID为文件夹的dynamicID,根据这两项能够判断
	//ITEM 来自哪个文件夹 或者原来在桌面上
	// folderID对于来自文件夹的图标 ,folderid = dynamicID,非文件夹的
	// folderID = OperateDynamicUtils.INVALID_FOLDERID
	// isInstalled 仅仅对虚应用有用，表示是安装后，用户的卸载。还是没有安装的卸载。
	protected void addUnInstallItem(
			String dynamicID ,
			String packageName ,
			int from ,
			String folderID ,
			boolean isInstalled )
	{
		if( OperateDynamicUtils.EXPIRED_MS_TIME == 0 )
		{
			return;
		}
		UnInstallItem newItem = new UnInstallItem();
		newItem.dynamicID = dynamicID;
		newItem.mPackageName = packageName;
		newItem.mRemovedTime = System.currentTimeMillis();
		newItem.mFrom = from;
		newItem.mFolderID = folderID;
		newItem.mIsInstalled = isInstalled;
		mUnInstallItems.add( newItem );
		saveUnInstallItems();
	}
	
	protected void saveUnInstallItems()
	{
		JSONObject json = new JSONObject();
		for( UnInstallItem data : mUnInstallItems )
		{
			try
			{
				json.put( data.mPackageName , data.toJSON() );
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		// T卡和系统目录双备份 ，虚应用：入口ID + 包名 ， 网页链接： 入口ID + 网页链接地址
		DynamicEntryHelper.getInstance( mContext ).setValue( PREFERENCE_UNINSTALL , json.toString() );
		//mPref.edit().putString( PREFERENCE_UNINSTALL , json.toString() ).commit();
	}
	
	protected void removeDefaultItems(
			List<OperateDynamicData> content )
	{
		if( content != null && content.size() > 0 )
		{
			Iterator<UnInstallItem> ite = mUnInstallItems.iterator();
			while( ite.hasNext() )
			{
				UnInstallItem uninstall = ite.next();
				for( OperateDynamicData data : content )
				{
					if( data.dynamicType == OperateDynamicUtils.FOLDER )
					{
						for( OperateDynamicItem item : data.mDynamicItems )
						{
							if( uninstall.mPackageName.equals( item.mPackageName ) )
							{
								ite.remove();
								break;
							}
						}
					}
					else
					{
						if( uninstall.mPackageName.equals( data.mPkgnameOrAddr ) )
						{
							ite.remove();
							break;
						}
					}
				}
			}
		}
	}
}
