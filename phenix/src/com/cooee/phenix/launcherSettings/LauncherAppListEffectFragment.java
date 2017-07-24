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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.effects.EffectFactory;


public class LauncherAppListEffectFragment extends BaseListPreferenceFragment
{
	
	private static final String TAG = "LauncherAllAppEffectFragment";
	private ImageView mBackButton;
	private TextView mTitle;
	private TextView mLoadingText;
	private String mKey;
	private String mCurrentValue;
	private int mClickEntryIndex;
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
		initView();
		loadItemInfoList( mItemInfoList );
		getListView().setDividerHeight( 0 );
		getListView().setChoiceMode( ListView.CHOICE_MODE_SINGLE );
		initEvent();
		setListAdapter( new ListSelectAdapter( getActivity() , mItemInfoList ) );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "[LauncherAllAppEffectFragment] onCreate" );
	}
	
	private void initData()
	{
		initConfigDataArray( getActivity() );
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );
		Resources res = getActivity().getResources();
		mKey = LauncherDefaultConfig.getString( R.string.setting_key_applist_effect );
		mCurrentValue = prefs.getString( mKey , "0" );
	}
	
	private void initView()
	{
		mBackButton = (ImageView)getView().findViewById( R.id.list_fragment_back );
		//xiatian add start	//桌面设置进入二级菜单的图片和退出二级菜单的图片，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，进入二级菜单的图片为左箭头，退出二级菜单的图片为右箭头。
		if( isLayoutRtl() )
		{
			mBackButton.setImageDrawable( getResources().getDrawable( R.drawable.launcher_settings_r2l_back ) );
		}
		//xiatian add end
		mTitle = (TextView)getView().findViewById( R.id.list_fragment_title );
		mTitle.setText( R.string.applist_effect );
		mLoadingText = (TextView)getView().findViewById( R.id.loading_text );
		mLoadingText.setVisibility( View.GONE );
	}
	
	public void loadItemInfoList(
			List<SelectItemInfo> itemInfoList )
	{
		if( mEntriesList == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "mEntries is null or mEntries's length is 0" );
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
	
	private void initEvent()
	{
		mBackButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( isFinishActivityOnBackPressed() )
				{
					getActivity().finish();
					return;
				}
				// TODO Auto-generated method stub
				onBackPressedWithAnim();
			}
		} );
		getListView().setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// TODO Auto-generated method stub
				mCurrentValue = String.valueOf( position );
				mClickEntryIndex = position;
				getListView().setItemChecked( mClickEntryIndex , false );
			}
		} );
	}
	
	private int getValueIndex()
	{
		return findIndexOfalue( mCurrentValue );
	}
	
	public int findIndexOfalue(
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
			mInflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			mClickEntryIndex = getValueIndex();
		}
		
		@Override
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
			View view;
			SelectItemInfo itemInfo = getItem( position );
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
			if( position == mClickEntryIndex )
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
	
	@Override
	protected boolean onBackPressedWithAnim()
	{
		// TODO Auto-generated method stub
		saveValue( mKey , mCurrentValue );
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
		// TODO Auto-generated method stub
		saveValue( mKey , mCurrentValue );
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.hide( this );
		transaction.commitAllowingStateLoss();
		return true;
	}
	
	public void saveValue()
	{
		saveValue( mKey , mCurrentValue );
	}
	
	//cheyingkun add start	//优化切页特效配置
	/**
	 * 主菜单获取特效配置的字符串(排序后的列表)
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
		//		CharSequence[] mEntries = LauncherDefaultConfig.getStringArray( R.array.applist_effect_entries );
		//		mEntriesList = Arrays.asList( mEntries );
		//		//初始化特效效果和顺序
		//		CharSequence[] mEntryConfigAndValues = LauncherDefaultConfig.getStringArray( R.array.applist_effect_configs_values );
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
		CharSequence[] mEntryConfigs = LauncherDefaultConfig.getStringArray( R.array.applist_effect_configs_values );
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
}
