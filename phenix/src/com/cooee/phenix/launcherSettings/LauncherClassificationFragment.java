package com.cooee.phenix.launcherSettings;


import java.util.ArrayList;
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
import com.cooee.phenix.Functions.Category.OperateHelp;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.iLoong.launcher.desktop.Disclaimer;
import com.iLoong.launcher.desktop.DisclaimerManager;


public class LauncherClassificationFragment extends BaseListPreferenceFragment
{
	
	private static final String TAG = "LauncherClassificationFragment";
	//智能分类值常量
	private static final int VALUE_NONE = -1;//
	private static final int VALUE_CLASSIFICATION = 0;//智能分类
	private static final int VALUE_RECOVER = 1;//分类恢复
	private ImageView backButton;
	private TextView title;
	private String mKey;
	private String currentValue;
	private int mClickedEntryIndex;
	private List<SelectItemInfo> mItemInfoList = new ArrayList<SelectItemInfo>();
	private CharSequence[] mEntries;//显示项内容
	private CharSequence[] msubEntries;//显示副项内容
	private CharSequence[] mEntryValues;//显示项存储对应的Value值
	private String lastClassficationTime = null;
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onActivityCreated( savedInstanceState );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "onActivityCreated" );
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
				if( !( position == VALUE_RECOVER && lastClassficationTime == null/*mClickedEntryIndex == VALUE_RECOVER*/) )
				{
					mClickedEntryIndex = position;
					getListView().setItemChecked( mClickedEntryIndex , false );
					ClassifyByIndex( mClickedEntryIndex );
				}
			}
		} );
		setListAdapter( new ListSelectAdapter( getActivity() , mItemInfoList ) );
	}
	
	private void ClassifyByIndex(
			int index )
	{
		saveValue( mKey , String.valueOf( index ) );
		switch( index )
		{
			case VALUE_CLASSIFICATION:
				// cheyingkun add start //免责声明布局(智能分类时的免责声明判断)
				if( Disclaimer.isNeedShowDisclaimer() )
				{//智能分类前弹出免责声明
					DisclaimerManager.getInstance( getActivity() ).showDisclaimer( DisclaimerManager.VISIT_NETWORK_DISCLAIMER_CATEGORY , new Disclaimer.OnClickListener() {
						
						@Override
						public void onClickDisclaimerDialogButton(
								View v ,
								int currentStyle )
						{
							if( DisclaimerManager.VISIT_NETWORK_DISCLAIMER_CATEGORY == currentStyle )
							{
								switch( v.getId() )
								{
									case R.id.dialog_button_positive:
										OperateHelp.getInstance( getActivity() ).startCategory();
										getActivity().finish();
										break;
								}
							}
						}
					} );
				}
				else
				{
					OperateHelp.getInstance( getActivity() ).startCategory();
					getActivity().finish();
				}
				// cheyingkun add end
				break;
			case VALUE_RECOVER:
				OperateHelp.getInstance( getActivity() ).stopCategory();
				getActivity().finish();// cheyingkun add //免责声明布局(智能分类)
				break;
			default:
				break;
		}
	}
	
	private void initData()
	{
		mEntries = LauncherDefaultConfig.getStringArray( R.array.launcher_classification_entries );
		msubEntries = LauncherDefaultConfig.getStringArray( R.array.launcher_classification_sub_entries );
		mEntryValues = LauncherDefaultConfig.getStringArray( R.array.launcher_classification_entry_values );
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );
		Resources res = getActivity().getResources();
		mKey = LauncherDefaultConfig.getString( R.string.setting_key_launcher_classification );
		currentValue = prefs.getString( mKey , "1" );
		// zhujieping@2015/04/10 UPD START
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( getActivity() );
		lastClassficationTime = sp.getString( OperateHelp.ClassificationTime , null );
		// zhujieping@2015/04/10 UPD END
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
		title.setText( R.string.intelligent_classification );
	}
	
	/**
	 * 加载显示项SelectItemInfo的内容
	 * @param mItemInfoList
	 */
	public void LoadItemInfoList(
			List<SelectItemInfo> mItemInfoList )
	{
		if( mEntries == null )
		{
			return;
		}
		for( int i = 0 ; i < mEntries.length ; i++ )
		{
			SelectItemInfo itemInfo = new SelectItemInfo();
			itemInfo.title = mEntries[i];
			itemInfo.value = mEntryValues[i];
			if( msubEntries != null && msubEntries.length > i )
			{
				itemInfo.summary = msubEntries[i];
			}
			mItemInfoList.add( itemInfo );
		}
	}
	
	private int getValueIndex()
	{
		return findIndexOfValue( currentValue );
	}
	
	/**
	 * 通过存储的值找出对应的索引
	 * @param value
	 * @return
	 */
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
			TextView summary;
			CheckBox checkBox;
		}
		
		public ListSelectAdapter(
				Context context ,
				List<SelectItemInfo> objects )
		{
			super( context , 0 , objects );
			// TODO Auto-generated constructor stub
			mInflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			mClickedEntryIndex = getValueIndex();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "test" , StringUtils.concat( "ListSelectAdapter - mClickedEntryIndex = " , mClickedEntryIndex ) );
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
				view = mInflater.inflate( R.layout.launcher_settings_list_select_item , parent , false );
				holder.title = (TextView)view.findViewById( R.id.title );
				holder.summary = (TextView)view.findViewById( R.id.summary );
				holder.checkBox = (CheckBox)view.findViewById( R.id.check_box );
				view.setTag( holder );
			}
			else
			{
				view = convertView;
				holder = (Holder)view.getTag();
			}
			holder.title.setText( itemInfo.title );
			if( position == VALUE_RECOVER && lastClassficationTime == null/*( mClickedEntryIndex == VALUE_RECOVER || mClickedEntryIndex == VALUE_NONE )*/)
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "getView - mClickedEntryIndex = " , mClickedEntryIndex ) );
				holder.title.setTextColor( getResources().getColor( R.color.launcher_setting_classification_item_text_normal_color ) );
				holder.summary.setTextColor( getResources().getColor( R.color.launcher_setting_classification_item_text_normal_color ) );
			}
			else
			{
				holder.title.setTextColor( getResources().getColor( R.color.launcher_setting_classification_item_text_clickable_color ) );
				holder.summary.setTextColor( getResources().getColor( R.color.launcher_setting_item_summary_title_color ) );
			}
			holder.summary.setText( itemInfo.summary );
			holder.checkBox.setVisibility( View.GONE );
			return view;
		}
	}
	
	//xiatian start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
	//xiatian del start
	//	@Override
	//	protected boolean onBackPressed()
	//	{
	//		// TODO Auto-generated method stub
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
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.hide( this );
		transaction.commitAllowingStateLoss();//允许Fragment的保存的状态丢失
		return true;
	}
	//xiatian add end
	//xiatian end
}
