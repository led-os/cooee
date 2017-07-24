package com.cooee.phenix.launcherSettings;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public abstract class PreferenceBaseSettingActivity extends FragmentActivity implements OnItemClickListener
{
	
	private final static String TAG = "PreferenceBaseSettingActivity";
	protected List<Header> mHeaders = new ArrayList<Header>();
	protected ListView mListView;
	private BaseAdapter mAdapter;
	public static final int HEADER_ID_UNDEFINED = -1;
	public static final int HEADER_ID_WORKSPACE_EFFECT_IN_CORE = 0;//单层桌面，桌面切页特效
	public static final int HEADER_ID_LAUNCHER_STYLE = 1;//桌面模式（单双层切换）
	public static final int HEADER_ID_CLASSIFICATION = 2;//智能分类
	public static final int HEADER_ID_WORKSPACE_EFFECT_IN_DRAWER = 3;//双层桌面，桌面切页特效
	public static final int HEADER_ID_APPLIST_EFFECT_IN_DRAWER = 4;//双层桌面，主菜单切页特效
	/**mListView某一项被按键选中，按十字键时，选中item改变因子。1为十字键下方向键，-1为十字键上方向键*/
	private int changeSelectedFactor = 1;//cheyingkun add	//桌面设置界面支持按键【i_0014557】
	
	/**
	 * 设置界面一级菜单的Item信息类
	 */
	public static final class Header
	{
		
		public int id = HEADER_ID_UNDEFINED;
		public boolean visable = true;//是否需要显示
		public int titleRes;
		public CharSequence title;
		public int summaryRes;
		public CharSequence summary;
		public int iconRes;
		public String fragment;
		public boolean canSelected = true;//cheyingkun add	//桌面设置界面支持按键【i_0014557】
		
		public Header()
		{
			// Empty
		}
		
		public CharSequence getTitle(
				Resources res )
		{
			if( titleRes != 0 )
			{
				return res.getText( titleRes );
			}
			return title;
		}
		
		public CharSequence getSummary(
				Resources res )
		{
			if( summaryRes != 0 )
			{
				return res.getText( summaryRes );
			}
			return summary;
		}
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		setContentView( R.layout.launcher_settings_layout );
		findView();
		if( mHeaders != null )
		{
			mHeaders.clear();
		}
		onBuildHeaders( mHeaders );
		if( mHeaders.size() > 0 )
		{
			mAdapter = new HeaderAdapter( this , mHeaders );
			mListView.setAdapter( mAdapter );
		}
	}
	
	private void findView()
	{
		mListView = (ListView)findViewById( R.id.launcher_setting_list );
		//设置监听
		mListView.setOnItemClickListener( this );
		//cheyingkun add start	//桌面设置界面支持按键【i_0014557】
		mListView.setOnItemSelectedListener( new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				mListView.setSelection( getCurrentPositionByCanSelected( position ) );
			}
			
			@Override
			public void onNothingSelected(
					AdapterView<?> parent )
			{
			}
		} );
		mListView.setOnKeyListener( new View.OnKeyListener() {
			
			@Override
			public boolean onKey(
					View v ,
					int keyCode ,
					KeyEvent event )
			{
				if( event.getAction() == KeyEvent.ACTION_DOWN//按下
						&& ( keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP )//十字键上或者下 
				)
				{
					if( mListView != null && mListView.hasFocusable() && mListView.hasFocus() )
					{
						changeSelectedFactorByDpad( event );
						//获取当前选中的item
						int position = mListView.getSelectedItemPosition();
						//获取下一个选中的item
						int nextPosition = getNextPositionByCanSelected( position );
						//如果当前position和下一个position相等,则返回
						if( nextPosition == position )
						{
							return true;
						}
					}
				}
				return false;
			}
		} );
		//cheyingkun add end
	}
	
	public void onBuildHeaders(
			List<Header> headers )
	{
		// Should be overloaded by subclasses
	}
	
	protected void notifyDataSetChanged()
	{
		if( mAdapter != null )
		{
			mAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 点击设置项
	 * @param header
	 * @param position
	 */
	public void onHeaderClick(
			Header header ,
			int position )
	{
		//
	}
	
	/**
	 *解析指定的Header xml文件
	 *
	 * @param resid The XML resource to load and parse.
	 * @param target The list in which the parsed headers should be placed.
	 */
	public void loadHeadersFromResource(
			int resid ,
			List<Header> target )
	{
		XmlResourceParser parser = null;
		try
		{
			parser = getResources().getXml( resid );
			AttributeSet attrs = Xml.asAttributeSet( parser );
			int type;
			while( ( type = parser.next() ) != XmlPullParser.END_DOCUMENT && type != XmlPullParser.START_TAG )
			{
				// Parse next until start tag is found
			}
			String nodeName = parser.getName();
			if( !"preference-headers".equals( nodeName ) )
			{
				throw new RuntimeException( StringUtils.concat( "XML document must start with <preference-headers> tag; found" , nodeName , " at " , parser.getPositionDescription() ) );
			}
			final int outerDepth = parser.getDepth();
			while( ( type = parser.next() ) != XmlPullParser.END_DOCUMENT && ( type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth ) )
			{
				if( type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT )
				{
					continue;
				}
				nodeName = parser.getName();
				if( "header".equals( nodeName ) )
				{
					Header header = new Header();
					TypedArray sa = getResources().obtainAttributes( attrs , R.styleable.LauncherSettingHeader );
					header.id = sa.getResourceId( R.styleable.LauncherSettingHeader_header_id , (int)HEADER_ID_UNDEFINED );
					header.visable = sa.getBoolean( R.styleable.LauncherSettingHeader_header_visible , true );//读取配置中的visible,这里只负责读取xml，不负责判断是否显示，统一集中到refleshSummary中判断
					TypedValue tv = sa.peekValue( R.styleable.LauncherSettingHeader_header_title );
					if( tv != null && tv.type == TypedValue.TYPE_STRING )
					{
						if( tv.resourceId != 0 )
						{
							header.titleRes = tv.resourceId;
						}
						else
						{
							header.title = tv.string;
						}
					}
					tv = sa.peekValue( R.styleable.LauncherSettingHeader_header_summary );
					if( tv != null && tv.type == TypedValue.TYPE_STRING )
					{
						if( tv.resourceId != 0 )
						{
							header.summaryRes = tv.resourceId;
						}
						else
						{
							header.summary = tv.string;
						}
					}
					header.iconRes = sa.getResourceId( R.styleable.LauncherSettingHeader_header_icon , 0 );
					header.fragment = sa.getString( R.styleable.LauncherSettingHeader_header_fragment );
					header.canSelected = sa.getBoolean( R.styleable.LauncherSettingHeader_header_canselected , true );//cheyingkun add	//桌面设置界面支持按键【i_0014557】
					sa.recycle();
					if( header.visable )
					{
						target.add( header );
					}
				}
			}
		}
		catch( XmlPullParserException e )
		{
			throw new RuntimeException( "Error parsing headers" , e );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Error parsing headers" , e );
		}
		finally
		{
			if( parser != null )
				parser.close();
		}
	}
	
	@Override
	public void onItemClick(
			AdapterView<?> parent ,
			View view ,
			int position ,
			long id )
	{
		// TODO Auto-generated method stub
		if( mAdapter != null )
		{
			Object item = mAdapter.getItem( position );
			if( item instanceof Header )
			{
				onHeaderClick( (Header)item , position );
			}
		}
	}
	
	private class HeaderAdapter extends ArrayAdapter<Header>
	{
		
		static final int HEADER_TYPE_CATEGORY = 0;
		static final int HEADER_TYPE_NORMAL = 1;
		private static final int HEADER_TYPE_COUNT = HEADER_TYPE_NORMAL + 1;
		
		private class HeaderViewHolder
		{
			
			ImageView icon;
			TextView title;
			TextView summary;
			ImageView indicator;
		}
		
		private LayoutInflater mInflater;
		
		int getHeaderType(
				Header header )
		{
			if( header.fragment == null )
			{
				return HEADER_TYPE_CATEGORY;
			}
			else
			{
				return HEADER_TYPE_NORMAL;
			}
		}
		
		public HeaderAdapter(
				Context context ,
				List<Header> objects )
		{
			super( context , 0 , objects );
			mInflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		}
		
		public boolean isEnabled(
				int position )
		{
			// TODO Auto-generated method stub
			Header header = getItem( position );
			int type = getHeaderType( header );
			return( type != HEADER_TYPE_CATEGORY );
		}
		
		public int getViewTypeCount()
		{
			// TODO Auto-generated method stub
			return HEADER_TYPE_COUNT;
		}
		
		public int getItemViewType(
				int position )
		{
			// TODO Auto-generated method stub
			Header header = getItem( position );
			return getHeaderType( header );
		}
		
		public int getCount()
		{
			// TODO Auto-generated method stub
			return mHeaders.size();
		}
		
		public Header getItem(
				int position )
		{
			// TODO Auto-generated method stub
			return super.getItem( position );
		}
		
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			HeaderViewHolder holder;
			Header header = getItem( position );
			int headerType = getHeaderType( header );
			View view = null;
			if( convertView == null )
			{
				holder = new HeaderViewHolder();
				switch( headerType )
				{
					case HEADER_TYPE_CATEGORY:
						view = mInflater.inflate( R.layout.launcher_settings_category , parent , false );
						holder.title = (TextView)view.findViewById( R.id.title );
						break;
					case HEADER_TYPE_NORMAL:
						view = mInflater.inflate( R.layout.launcher_settings_header_item , parent , false );
						holder.icon = (ImageView)view.findViewById( R.id.icon );
						holder.title = (TextView)view.findViewById( R.id.title );
						holder.summary = (TextView)view.findViewById( R.id.summary );
						holder.indicator = (ImageView)view.findViewById( R.id.indicator );
						break;
				}
				view.setTag( holder );
			}
			else
			{
				view = convertView;
				holder = (HeaderViewHolder)view.getTag();
			}
			switch( headerType )
			{
				case HEADER_TYPE_CATEGORY:
					holder.title.setText( header.getTitle( getContext().getResources() ) );
					break;
				case HEADER_TYPE_NORMAL:
					updateCommonHeaderView( header , holder );
					break;
			}
			return view;
		}
		
		private void updateCommonHeaderView(
				Header header ,
				HeaderViewHolder holder )
		{
			holder.icon.setImageResource( header.iconRes );
			holder.icon.setBackgroundResource( R.drawable.launcher_setting_item_icon_bg_shape );//桌面设置每一项图标添加圆形背景图（该背景图颜色和桌面设置title bar的颜色一致）
			holder.title.setText( header.getTitle( getContext().getResources() ) );
			CharSequence summary = header.getSummary( getContext().getResources() );
			if( !TextUtils.isEmpty( summary ) )
			{
				holder.summary.setVisibility( View.VISIBLE );
				holder.summary.setText( summary );
			}
			else
			{
				holder.summary.setVisibility( View.GONE );
			}
			//xiatian add start	//桌面设置进入二级菜单的图片和退出二级菜单的图片，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，进入二级菜单的图片为左箭头，退出二级菜单的图片为右箭头。
			//			holder.indicator.setImageResource( R.drawable.launcher_settings_indicator );//xiatian del
			//xiatian add start
			if( isLayoutRtl() )
			{
				holder.indicator.setImageResource( R.drawable.launcher_settings_l2r_indicator );
			}
			else
			{
				holder.indicator.setImageResource( R.drawable.launcher_settings_r2l_indicator );
			}
			//xiatian add end
			//xiatian end
			//cheyingkun add start	//设置默认桌面引导(桌面设置中的改选项不显示箭头)
			if( holder.title != null && holder.title.getText() != null// 
					&& holder.title.getText().toString().equals( LauncherDefaultConfig.getString( R.string.launcher_setting_default_launcher_item_title ) ) )
			{
				holder.indicator.setVisibility( View.GONE );
			}
			else
			{
				holder.indicator.setVisibility( View.VISIBLE );
			}
			//cheyingkun add start	//设置默认桌面引导
		}
	}
	
	//xiatian add start	//桌面设置进入和退出二级菜单的动画，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，从左往右进入二级菜单，从右往左退出二级菜单。
	public boolean isLayoutRtl()
	{
		//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
		//		return( Tools.isLayoutRTL( getWindow().getDecorView() ) );//xiatian del
		return( LauncherAppState.isLayoutRTL() );//xiatian add 
		//xiatian end
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//桌面设置界面支持按键【i_0014557】
	/**
	 * 返回当前可以选中的position
	 * @param position
	 * @return
	 */
	private int getCurrentPositionByCanSelected(
			int position )
	{
		Header header = mHeaders.get( position );
		while( !header.canSelected )
		{
			position += changeSelectedFactor;
			if( position < 0 || position > mHeaders.size() - 1 )
			{
				changeSelectedFactor *= -1;
				position += changeSelectedFactor;
			}
			header = mHeaders.get( position );
		}
		return position;
	}
	
	/**
	 * 返回下一个可以选中的position
	 * @param position
	 * @return
	 */
	private int getNextPositionByCanSelected(
			int position )
	{
		//判断下一个item是否可以选中,并更新position
		Header header;
		do
		{
			position += changeSelectedFactor;
			if( position < 0 || position > mHeaders.size() - 1 )
			{
				changeSelectedFactor *= -1;
				position += changeSelectedFactor;
			}
			header = mHeaders.get( position );
		}
		while( !header.canSelected );
		return position;
	}
	
	/**
	 * 根据十字键按键改变listview选择位置的因子
	 * @param event
	 */
	private void changeSelectedFactorByDpad(
			KeyEvent event )
	{
		if( event.getAction() == KeyEvent.ACTION_DOWN )
		{
			int keyCode = event.getKeyCode();
			switch( keyCode )
			{
				case KeyEvent.KEYCODE_DPAD_DOWN:
					changeSelectedFactor = 1;
					break;
				case KeyEvent.KEYCODE_DPAD_UP:
					changeSelectedFactor = -1;
					break;
				default:
					break;
			}
		}
	}
	//cheyingkun add end
}
