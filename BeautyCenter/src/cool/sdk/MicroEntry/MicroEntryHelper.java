package cool.sdk.MicroEntry;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.iLoong.launcher.MList.MELOG;

import cool.sdk.MicroEntry.MicroEntryLog.MicroEntryLogItem;
import cool.sdk.download.CoolDLMgr;


public class MicroEntryHelper extends MicroEntryUpdate
{
	
	// 取消不必要的循环，加快运行速度
	boolean IsMeUpdateDisclaimer = true;
	Context mContext = null;
	
	protected MicroEntryHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	static MicroEntryHelper instance = null;
	
	public static MicroEntryHelper getInstance(
			Context context )
	{
		synchronized( MicroEntryHelper.class )
		{
			if( instance == null )
			{
				instance = new MicroEntryHelper( context );
			}
		}
		return instance;
	}
	
	private static final int startIndex = 10005;
	private static final int endtIndex = 10006;
	
	@Override
	public void OnDataChange() throws Exception
	{
		// TODO Auto-generated method stub
		String resJson = getListString();
		Log.v( "ME_RTFSC" , "MicroEntryHelper ThemeBox2.2: OnDataChange:" + resJson );
		boolean[] visible = { false , false };
		if( resJson != null )
		{
			try
			{
				JSONObject list = new JSONObject( resJson );
				Iterator<?> keys = (Iterator<?>)list.keys();
				String key;
				while( keys.hasNext() )
				{
					key = (String)keys.next();
					JSONObject item = list.getJSONObject( key );
					// r1 list 数字 入口ID [入口ID是客户端标示或者定位入口的唯一ID。]
					// r2 list 对象，字符 英文名称
					// r3 list 对象，字符 中文名称
					// r4 list 对象，字符 繁体名称
					// r5 list 对象，数字 应用程序列表 0:不显示 1:显示
					// r6 list 对象，数字 桌面 0:不显示 1:显示
					// r7 list 对象，字符 图标地址url
					// r8 list 对象，字符 入口url
					// r9 list 对象，数字 快捷方式显示屏幕位置x
					// r10 list 对象，数字 快捷方式显示屏幕位置y
					int r1 = item.getInt( "r1" );
					String r2 = item.getString( "r2" );
					String r3 = item.getString( "r3" );
					String r4 = item.getString( "r4" );
					int r5 = item.getInt( "r5" );
					int r6 = item.getInt( "r6" );
					String r7 = item.getString( "r7" );
					String r8 = item.getString( "r8" );
					int r9 = item.getInt( "r9" );
					int r10 = item.getInt( "r10" );
					setValue( r1 + "r5" , r5 );
					setValue( r1 + "r8" , r8 );
					if( r1 >= startIndex && r1 <= endtIndex )
					{
						if( 1 == r5 )
						{
							visible[r1 - startIndex] = true;
							setValue( r1 + "en" , r2 );
							setValue( r1 + "zh" , r3 );
							MELOG.v( "ME_RTFSC" , r1 + "en" + r2 );
							MELOG.v( "ME_RTFSC" , r1 + "zh" + r3 );
						}
						else
						{
							visible[r1 - startIndex] = false;
						}
					}
				}
			}
			catch( Exception e )
			{
			}
		}
		UpdateActiveOrDelItemList( visible );
		for( int i = 0 ; i < visible.length ; i++ )
		{
			MELOG.v( "ME_RTFSC" , "visible[" + i + "]:" + visible[i] );
			if( true == visible[i] )
			{
				setValue( ( startIndex + i ) + "" , "TRUE" );
			}
			else
			{
				setValue( ( startIndex + i ) + "" , "FALSE" );
			}
		}
	}
	
	private void UpdateActiveOrDelItemList(
			boolean[] visible )
	{
		// TODO Auto-generated method stub
		List<MicroEntryLogItem> ActiveItemList = new ArrayList<MicroEntryLog.MicroEntryLogItem>();
		List<MicroEntryLogItem> DeleteItemList = new ArrayList<MicroEntryLog.MicroEntryLogItem>();
		String LanguageType = context.getResources().getConfiguration().locale.getLanguage();
		for( int i = 0 ; i < visible.length ; i++ )
		{
			String IsShow = getString( ( startIndex + 1 ) + "" );
			if( true == visible[i] )
			{
				if( null != IsShow && IsShow.equals( "FALSE" ) )
				{
					MicroEntryLogItem activeItem = new MicroEntryLogItem();
					activeItem.id = startIndex + 1;
					activeItem.type = 2;
					activeItem.name = getString( startIndex + i + LanguageType );
					ActiveItemList.add( activeItem );
				}
			}
			else
			{
				if( null != IsShow && IsShow.equals( "TRUE" ) )
				{
					MicroEntryLogItem delItem = new MicroEntryLogItem();
					delItem.id = startIndex + 1;
					delItem.type = 3;
					delItem.name = getString( startIndex + i + LanguageType );
					DeleteItemList.add( delItem );
				}
			}
		}
		if( ActiveItemList.size() >= 1 )
		{
			MELOG.v( "ME_RTFSC" , "ActiveItemList.size()" + ActiveItemList.size() );
			MicroEntryLog.LogActive( context , ActiveItemList );
		}
		if( DeleteItemList.size() >= 1 )
		{
			MELOG.v( "ME_RTFSC" , "DeleteItemList.size()" + DeleteItemList.size() );
			MicroEntryLog.LogDelete( context , DeleteItemList );
		}
	}
	
	public static boolean shouldExit(
			Context context )
	{
		for( int i = 1 ; i <= 4 ; i++ )
		{
			CoolDLMgr dlmgr = MicroEntry.CoolDLMgr( context , "M" , i );
			if( dlmgr.dl_mgr.getTaskCount() > 0 )
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String getEntryID() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
}
