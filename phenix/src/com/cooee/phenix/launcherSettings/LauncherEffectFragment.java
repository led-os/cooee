package com.cooee.phenix.launcherSettings;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.effects.EffectFactory;

public class LauncherEffectFragment extends BaseListPreferenceFragment
{
	
	private static final String TAG = "LauncherEffectFragment";
	private ImageView backButton;
	private TextView title;
	private TextView loadingText;
	private String mKey;
	private String currentValue;
	private int mClickedEntryIndex;
	private List<SelectItemInfo> mItemInfoList = new ArrayList<SelectItemInfo>();
	/**显示项内容*/
	private static List<CharSequence> mEntriesList;
	/**特效实际效果配置*/
	private static List<CharSequence> mEntryConfigList;
	
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
	}
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onActivityCreated( savedInstanceState );
		initData();
		findView();
		LoadItemInfoList( mItemInfoList );
		getListView().setDividerHeight( 0 );
		getListView().setChoiceMode( ListView.CHOICE_MODE_SINGLE );
		getListView().setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// TODO Auto-generated method stub
				currentValue = String.valueOf( position );
				mClickedEntryIndex = position;
				getListView().setItemChecked( mClickedEntryIndex , false );
			}
		} );
		setListAdapter( new ListSelectAdapter( getActivity() , mItemInfoList ) );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "[LauncherEffectFragment] onCreate" );
	}
	
	private void findView()
	{
		backButton = (ImageView)getView().findViewById( R.id.list_fragment_back );
		//xiatian add start	//桌面设置进入二级菜单的图片和退出二级菜单的图片，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，进入二级菜单的图片为左箭头，退出二级菜单的图片为右箭头。
		if( isLayoutRtl() )
		{
			backButton.setImageDrawable( getResources().getDrawable( R.drawable.launcher_settings_r2l_back ) );
		}
		//xiatian add end
		backButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( isFinishActivityOnBackPressed() )
				{
					getActivity().finish();
					return;
				}
				//xiatian start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
				//				onBackPressed();//xiatian del
				onBackPressedWithAnim();//xiatian add
				//xiatian end
			}
		} );
		title = (TextView)getView().findViewById( R.id.list_fragment_title );
		int mTitleStrId = -1;
		//lixiaopeng start //解决桌面设置--屏幕切页特效菜单进入后title栏仍显示为“切换桌面模式”【i_0010508】 
		//		mTitleStrId = R.string.select_launcher_style;//lixiaopeng del
		//WangLei start //桌面和主菜单特效的分离
		//WangLei del start
		//mTitleStrId = R.string.desktop_slide;//lixiaopeng add
		//WangLei del end
		//WangLei add start //桌面和主菜单特效的分离
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			mTitleStrId = R.string.desktop_slide;
		}
		else
		{
			mTitleStrId = R.string.workspace_effect;
		}
		//WangLei add end
		//WangLei end
		//lixiaopeng end
		title.setText( mTitleStrId );
		loadingText = (TextView)getView().findViewById( R.id.loading_text );
		loadingText.setVisibility( View.GONE );
	}
	
	private void initData()
	{
		initConfigDataArray( getActivity() );
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );
		Resources res = getActivity().getResources();
		//WangLei start //桌面和主菜单特效的分离
		//mKey = res.getString( R.string.setting_key_launcher_effects ); //WangLei del
		//WangLei add start
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			mKey = LauncherDefaultConfig.getString( R.string.setting_key_launcher_effects );
		}
		else
		{
			mKey = LauncherDefaultConfig.getString( R.string.setting_key_workspace_effect );
		}
		//WangLei add end
		//WangLei end
		currentValue = prefs.getString( mKey , "0" );
	}
	
	public void LoadItemInfoList(
			List<SelectItemInfo> mItemInfoList )
	{
		if( mEntriesList == null )
		{
			return;
		}
		for( int i = 0 ; i < mEntriesList.size() ; i++ )
		{
			SelectItemInfo itemInfo = new SelectItemInfo();
			itemInfo.title = mEntriesList.get( i );
			itemInfo.value = String.valueOf( i );
			mItemInfoList.add( itemInfo );
		}
	}
	
	private int getValueIndex()
	{
		return findIndexOfValue( currentValue );
	}
	
	public int findIndexOfValue(
			String value )
	{
		if( value != null && mItemInfoList != null )
		{
			for( int i = 0 ; i < mItemInfoList.size() ; i++ )
			{
				if( mItemInfoList.get( i ).value.equals( value ) )
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	private class ListSelectAdapter extends ArrayAdapter<SelectItemInfo>
	{
		
		private LayoutInflater mInflater;
		
		private class Holder
		{
			
			TextView title;
			CheckBox checkBox;
		}
		
		public ListSelectAdapter(
				Context context ,
				List<SelectItemInfo> objects )
		{
			super( context , 0 , objects );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "test" , StringUtils.concat( "count = " , objects.size() ) );
			// TODO Auto-generated constructor stub
			mInflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			mClickedEntryIndex = getValueIndex();
		}
		
		public int getCount()
		{
			// TODO Auto-generated method stub
			return mItemInfoList.size();
		}
		
		@Override
		public SelectItemInfo getItem(
				int position )
		{
			// TODO Auto-generated method stub
			if( position < 0 || position >= mItemInfoList.size() )
			{
				return null;
			}
			return super.getItem( position );
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			Holder holder;
			SelectItemInfo itemInfo = getItem( position );
			View view = null;
			if( convertView == null )
			{
				holder = new Holder();
				view = mInflater.inflate( R.layout.launcher_settings_no_summary_list_select_item , parent , false );
				holder.title = (TextView)view.findViewById( R.id.title );
				holder.checkBox = (CheckBox)view.findViewById( R.id.check_box );
				view.setTag( holder );
			}
			else
			{
				view = convertView;
				holder = (Holder)view.getTag();
			}
			holder.title.setText( itemInfo.title );
			if( position == mClickedEntryIndex )
			{
				holder.checkBox.setChecked( true );
			}
			else
			{
				holder.checkBox.setChecked( false );
			}
			return view;
		}
	}
	
	//xiatian start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
	//xiatian del start
	//	@Override
	//	protected boolean onBackPressed()
	//	{
	//		// TODO Auto-generated method stub
	//		saveValue( mKey , currentValue );
	//		FragmentManager fm = getFragmentManager();
	//		FragmentTransaction transaction = fm.beginTransaction();
	//		transaction.setCustomAnimations( 0 , R.anim.fragment_exit );
	//		transaction.hide( this );
	//		transaction.commit();
	//		return true;
	//	}
	//xiatian del end
	//xiatian add start
	@Override
	protected boolean onBackPressedWithAnim()
	{
		saveValue( mKey , currentValue );
		//xiatian start	//桌面设置进入和退出二级菜单的动画，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，从左往右进入二级菜单，从右往左退出二级菜单。
		//		exitWithAnim( R.anim.anim_launcher_settings_sub_menu_exit );//xiatian del
		//xiatian add start
		if( isLayoutRtl() )
		{
			exitWithAnim( R.anim.anim_launcher_settings_sub_menu_r2l_exit );
		}
		else
		{
			exitWithAnim( R.anim.anim_launcher_settings_sub_menu_l2r_exit );
		}
		//xiatian add end
		//xiatian end
		return true;
	}
	
	@Override
	protected boolean onBackPressedWithOutAnim()
	{
		saveValue( mKey , currentValue );
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.hide( this );
		transaction.commitAllowingStateLoss();//允许Fragment的保存的状态丢失
		return true;
	}
	//xiatian add end
	//xiatian end
	;
	
	//xiatian add start	//fix bug：解决“在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，桌面特效为之前的桌面特效（没有保存刚刚在屏幕切页特效界面中的选择）”的问题。【i_0010438】
	public void saveValue()
	{
		saveValue( mKey , currentValue );
	}
	
	//xiatian add end
	//cheyingkun add start	//优化切页特效配置
	/**
	 * 桌面获取特效配置的字符串(排序后的列表)
	 * @param context
	 * @return
	 */
	public static void initConfigDataArray(
			Context context )
	{
		if( mEntriesList != null )
		{
			return;
		}
		mEntryConfigList = new ArrayList<CharSequence>();
		//chenliang start	//优化桌面设置中切页特效的配置方式：1.去掉无用的配置项“workspace_effect_entries”和“applist_effect_entries” 2.去掉了配置项中“workspace_effect_configs_values”和“applist_effect_configs_values”的索引值
		//chenliang del start
		//		mEntryValuesList = new ArrayList<CharSequence>();
		//		//初始化显示的内容
		//		CharSequence[] mEntries = LauncherDefaultConfig.getStringArray( R.array.workspace_effect_entries );
		//		mEntriesList = Arrays.asList( mEntries );
		//		//初始化特效效果和顺序
		//		CharSequence[] mEntryConfigAndValues = LauncherDefaultConfig.getStringArray( R.array.workspace_effect_configs_values );
		//		//解析mEntryConfigAndValues配置
		//		for( CharSequence charSequence : mEntryConfigAndValues )
		//		{
		//			String[] split = charSequence.toString().split( "," );
		//			if( split != null && split.length == 2 )
		//			{
		//				mEntryConfigList.add( split[0] );
		//				mEntryValuesList.add( split[1] );
		//			}
		//		}
		//		//
		//		//
		//		//mEntryValuesList列表排序
		//		//mEntryValuesList排序时,对应修改mEntriesList的顺序,方便fae调整特效
		//		List<CharSequence> mEntriesListTmp = new ArrayList<CharSequence>();
		//		List<CharSequence> mEntryValuesListTmp = new ArrayList<CharSequence>();
		//		List<CharSequence> mEntryConfigListTmp = new ArrayList<CharSequence>();
		//		int size = mEntryValuesList.size();
		//		for( int i = 0 ; i < size ; i++ )
		//		{
		//			int indexOf = mEntryValuesList.indexOf( String.valueOf( i ) );
		//			if( indexOf != -1 && indexOf < size )
		//			{
		//				mEntryValuesListTmp.add( String.valueOf( i ) );
		//				mEntriesListTmp.add( mEntriesList.get( indexOf ) );
		//				mEntryConfigListTmp.add( mEntryConfigList.get( indexOf ) );
		//			}
		//		}
		//		//把调整过顺序的列表复制过去
		//		mEntriesList = mEntriesListTmp;
		//		mEntryValuesList = mEntryValuesListTmp;
		//		mEntryConfigList = mEntryConfigListTmp;
		//chenliang del end 
		//chenliang add start
		CharSequence[] mEntryConfigs = LauncherDefaultConfig.getStringArray( R.array.workspace_effect_configs_values );
		mEntryConfigList = Arrays.asList( mEntryConfigs );
		mEntriesList = EffectFactory.getEffectEntries( mEntryConfigList );
		//chenliang add end
		//chenliang end
	}
	
	/**
	 * @param context
	 * @return 切页特效显示内容
	 */
	public static List<CharSequence> getEntriesList(
			Context context )
	{
		if( mEntriesList == null )
		{
			initConfigDataArray( context );
		}
		return mEntriesList;
	}
	
	//chenliang del start	//优化桌面设置中切页特效的配置方式：1.去掉无用的配置项“workspace_effect_entries”和“applist_effect_entries” 2.去掉了配置项中“workspace_effect_configs_values”和“applist_effect_configs_values”的索引值
	//	/**
	//	 * @param context
	//	 * @return 显示项存储对应的Value值
	//	 */
	//	public static List<CharSequence> getEntryValuesList(
	//			Context context )
	//	{
	//		if( mEntryValuesList == null )
	//		{
	//			initConfigDataArray( context );
	//		}
	//		return mEntryValuesList;
	//	}
	//chenliang del end
	/**
	 * @param context
	 * @return 切页特效实际效果
	 */
	public static List<CharSequence> getEntryConfigList(
			Context context )
	{
		if( mEntryConfigList == null )
		{
			initConfigDataArray( context );
		}
		return mEntryConfigList;
	}
	//cheyingkun add end	//优化切页特效配置
	
	//chenliang add start	//解决“在安卓7.0手机上，切换系统语言或者字体大小等操作后按back键返回桌面，然后启动应用再返回桌面时偶先有很长的延时”的问题。【c_0004672】
	//在桌面onDestory方法中将这些静态变量置空
	public static void releaseStaticVariable()
	{
		mEntriesList = null;
		mEntryConfigList = null;
	}
	//chenliang add end
}
