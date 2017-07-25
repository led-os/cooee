package com.cooee.app.cooeeweather.view;


import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooee.app.cooeeweather.dataentity.ForeignCitysEntity;
import com.cooee.app.cooeeweather.dataentity.InlandCitysEntity;
import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.util.ResolverUtil;
import com.cooee.widget.samweatherclock.AppConfig;
import com.cooee.widget.samweatherclock.MainActivity;
import com.cooee.widget.samweatherclock.R;


public class WeatherAddPost extends Activity implements OnItemClickListener
{
	
	private enum StringType
	{
		LATIN , HANZI , MIX
	};
	
	private static final String TAG = "com.cooee.weather.WeatherAddPost";
	private final String CITY_CONTENT_URI = "content://com.cooee.app.cooeeweather.dataprovider/citys";
	private LayoutInflater mInflater;
	private LinearLayout mpopcityLinear;
	private String[] popcitys = null;
	private EditText mEditText;
	private ListView mListView;
	private MyAdapter mListAdapter;
	private Cursor mCursor = null;
	private StringType mEditContentType = StringType.LATIN; // 输入框中内容的类型
	// private List<Map<String, Object>> mListData = new ArrayList<Map<String,
	// Object>>();;
	private boolean found = false;
	private boolean supportForeign = true;
	private Context mContext;
	private boolean popularForeignCitys = false;
	private static final int FINISH_LOAD_ARRAY = 100;
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// TODO Auto-generated method stub
			super.handleMessage( msg );
			if( msg.what == FINISH_LOAD_ARRAY )
			{
				GridView gridview = (GridView)findViewById( R.id.gridview );
				gridview.setAdapter( new citysAdapter( mContext ) );
				if( AppConfig.getInstance( mContext ).isHuaweiStyle() )
				{
					mListView.setBackgroundColor( Color.WHITE );
					( (TextView)findViewById( R.id.title ) ).setTextColor( Color.BLACK );
					findViewById( R.id.title_line ).setBackgroundColor( Color.BLACK );
					findViewById( R.id.popviewlayout ).setBackgroundColor( Color.WHITE );
					gridview.setBackgroundColor( Color.WHITE );
				}
			}
		}
	};
	
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mContext = this;
		setContentView( R.layout.add_post_layout );
		try
		{
			Properties pro = new Properties();
			pro.load( this.getAssets().open( "config.properties" ) );
			supportForeign = Boolean.parseBoolean( pro.getProperty( "supportForeign" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		//Log.i( "andy" , "thread id = " + Thread.currentThread().getId() );
		mListView = (ListView)findViewById( R.id.listview_add );
		mListAdapter = new MyAdapter( this );
		mListView.setAdapter( mListAdapter );
		mListView.setOnItemClickListener( this );
		if( AppConfig.getInstance( mContext ).isMerge() )
		{
			new Thread() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					super.run();
					//Log.i( "andy" , " mHandler thread id = " + Thread.currentThread().getId() );
					if( AppConfig.getInstance( mContext ).isPosition() )
					{
						String city = ResolverUtil.getLocatedCity( mContext );
						if( city != null )
						{
							if( ResolverUtil.checkCityIsForeignCity( mContext,city ) )
							{
								popcitys = mContext.getResources().getStringArray( R.array.popcitys_foreign );
								popularForeignCitys = true;
							}
							else
							{
								popcitys = mContext.getResources().getStringArray( R.array.popcitys_default );
								popularForeignCitys = false;
							}
						}
						else
						{
							if( isZh() )
							{
								popcitys = mContext.getResources().getStringArray( R.array.popcitys_default );
								popularForeignCitys = false;
							}
							else
							{
								popcitys = mContext.getResources().getStringArray( R.array.popcitys_foreign );
								popularForeignCitys = true;
							}
						}
					}
					else
					{
						if( isZh() )
						{
							popcitys = getResources().getStringArray( R.array.popcitys_default );
							popularForeignCitys = false;
						}
						else
						{
							popcitys = getResources().getStringArray( R.array.popcitys_foreign );
							popularForeignCitys = true;
						}
					}
					mHandler.sendEmptyMessage( FINISH_LOAD_ARRAY );
				}
			}.start();
		}
		else
		{
			popcitys = getResources().getStringArray( R.array.popcitys_default );
			GridView gridview = (GridView)findViewById( R.id.gridview );
			gridview.setAdapter( new citysAdapter( mContext ) );
			if( AppConfig.getInstance( mContext ).isHuaweiStyle() )
			{
				mListView.setBackgroundColor( Color.WHITE );
				( (TextView)findViewById( R.id.title ) ).setTextColor( Color.BLACK );
				findViewById( R.id.title_line ).setBackgroundColor( Color.BLACK );
				findViewById( R.id.popviewlayout ).setBackgroundColor( Color.WHITE );
				gridview.setBackgroundColor( Color.WHITE );
			}
		}
		System.out.println( "shlt , supportForeign : " + supportForeign );
		mEditText = (EditText)findViewById( R.id.add_post_text );
		// mEditText.setFocusable(true);
		// mEditText.setFocusableInTouchMode(true);
		mEditText.clearFocus();
		// mEditText.requestFocus();
		( (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE ) ).showSoftInput( mEditText , InputMethodManager.SHOW_FORCED );
		// 设置edit的图片
		mEditText.setBackgroundResource( R.drawable.editbox_background_normal );
		mEditText.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(
					CharSequence s ,
					int start ,
					int before ,
					int count )
			{
				Log.v( TAG , "s = " + s + ", start = " + start + ", before = " + before + ", count = " + count );
				String str = "" + s;
				if( mCursor != null )
				{
					mCursor.close();
					mCursor = null;
				}
				// 鍘婚櫎鍓嶅悗绌烘牸
				str = str.trim();
				// 鍒ゆ柇涓虹函姹夊瓧杩樻槸鎷奸煶
				int bl = str.getBytes().length;
				int l = str.length();
				if( bl == l * 3 )
				{
					mEditContentType = StringType.HANZI;
				}
				else if( bl == l )
				{
					mEditContentType = StringType.LATIN;
				}
				else
				{
					mEditContentType = StringType.MIX;
				}
				if( !str.equals( "" ) && !"none".equals( str ) )
				{ // str涓嶄负绌�
					String selection = null;
					if( mEditContentType == StringType.HANZI )
					{
						selection = InlandCitysEntity.NAME + " LIKE " + "'%" + str + "%'";
					}
					else if( mEditContentType == StringType.LATIN && AppConfig.getInstance( mContext ).isMerge() )
					{
						selection = ForeignCitysEntity.CITY_EN + " LIKE " + "'%" + str + "%'";
					}
					else
					{
						selection = InlandCitysEntity.NAME + " LIKE " + "'%" + "其他语言处理" + "%'";
					}
					// if (mEditContentType != StringType.MIX) {
					if( true )
					{
						if( AppConfig.getInstance( mContext ).isMerge() && mEditContentType == StringType.LATIN )
						{
							mCursor = getContentResolver().query( Uri.parse( CITY_CONTENT_URI ) , ForeignCitysEntity.projection_abroad_en , selection , null , null );
						}
						else if( mEditContentType == StringType.HANZI )
						{
							mCursor = getContentResolver().query( Uri.parse( CITY_CONTENT_URI ) , InlandCitysEntity.projection , selection , null , null );
						}
						else
						{
							mCursor = getContentResolver().query( Uri.parse( CITY_CONTENT_URI ) , InlandCitysEntity.projection , selection , null , null );
						}
					}
					mpopcityLinear = (LinearLayout)findViewById( R.id.popviewlayout );
					mpopcityLinear.setVisibility( View.INVISIBLE );
				}
				else
				{
					mpopcityLinear = (LinearLayout)findViewById( R.id.popviewlayout );
					mpopcityLinear.setVisibility( View.VISIBLE );
				}
				// popviewlayout
				// getListData();
				mListAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void beforeTextChanged(
					CharSequence s ,
					int start ,
					int count ,
					int after )
			{
			}
			
			@Override
			public void afterTextChanged(
					Editable s )
			{
			}
		} );
		// mListView
		/*GridView gridview = (GridView)findViewById( R.id.gridview );
		gridview.setAdapter( new citysAdapter( this ) );
		if( AppConfig.getInstance( this ).isHuaweiStyle() )
		{
			mListView.setBackgroundColor( Color.WHITE );
			( (TextView)this.findViewById( R.id.title ) ).setTextColor( Color.BLACK );
			this.findViewById( R.id.title_line ).setBackgroundColor( Color.BLACK );
			this.findViewById( R.id.popviewlayout ).setBackgroundColor( Color.WHITE );
			gridview.setBackgroundColor( Color.WHITE );
		}*/
	}
	
	public void onBackClick(
			View v )
	{
		finish();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		// finish();
	}
	
	@Override
	public void onDestroy()
	{
		if( mCursor != null )
		{
			mCursor.close();
			mCursor = null;
		}
		if( found )
		{
			Intent data = new Intent();
			// 璇锋眰浠ｇ爜鍙互鑷繁璁剧疆锛岃繖閲岃缃垚20
			/*if( AppConfig.getInstance( mContext ).isMerge() )
			{
				if(checkCityIsForeignCity( postCode )){
					data.putExtra( "foreignCity" , true );
				}
			}*/
			setResult( 1 , data );
		}
		else
		{
			Intent data = new Intent();
			setResult( 0 , data );
		}
		super.onDestroy();
	}
	
	public void onDoneClick(
			View v )
	{
		String postCode = mEditText.getText().toString();
		postCode = postCode.trim(); // 鍘绘帀鍓嶅悗绌烘牸
		Log.v( TAG , "add postCode = " + postCode );
		if( !postCode.equals( "" ) )
		{
			// 鍏堝湪鏁版嵁搴撲腑鏌ユ壘鏄惁涔嬪墠宸茬粡娣诲姞杩�
			ContentResolver resolver = getContentResolver();
			Cursor cursor = null;
			found = false;
			Uri uri = Uri.parse( MainActivity.POSTALCODE_URI );
			cursor = resolver.query( uri , PostalCodeEntity.projection , PostalCodeEntity.POSTAL_CODE + " = '" + postCode + "'" + " and " + PostalCodeEntity.USER_ID + " = '0'" , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					found = true;
				}
				cursor.close();
			}
			// if(postCode.equals(getResources().getString(R.string.defaultcity))){
			// Intent intent = new Intent();
			// intent.setAction("com.cooee.weather.data.action.ADD_DAFAULT_CITY");
			// sendBroadcast(intent);
			// }
			// 娌℃壘鍒帮紝鍒欐坊鍔�
			Intent data = new Intent();
			if( AppConfig.getInstance( mContext ).isMerge() )
			{
				if( ResolverUtil.checkCityIsForeignCity( mContext, postCode ) )
				{
					data.putExtra( "foreignCity" , true );
				}
			}
			if( !found )
			{
				ContentValues values = new ContentValues();
				values.put( PostalCodeEntity.POSTAL_CODE , postCode );
				values.put( PostalCodeEntity.USER_ID , 0 ); // 0涓洪粯璁serId
				resolver.insert( uri , values );
				data.putExtra( "citys" , postCode );
				// 请求代码可以自己设置，这里设置成20
				setResult( 1 , data );
			}
			else
			{
				data.putExtra( "citys" , postCode );
				// 请求代码可以自己设置，这里设置成20
				setResult( 1 , data );
			}
		}
		finish();
	}
	
	private class MyAdapter extends BaseAdapter
	{
		
		private Context mContext;
		private LayoutInflater mInflater;
		
		public MyAdapter(
				Context context )
		{
			this.mContext = context;
			mInflater = LayoutInflater.from( mContext );
		}
		
		@Override
		public int getCount()
		{
			if( mCursor != null )
			{
				if( mCursor.getCount() == 0 && !"".equals( isConSpeCharacters( mEditText.getText().toString().trim() ) ) )
				{
					/*
					 * mCursor.close(); mCursor = null;
					 */
					return 1;// 杩欓噷涓昏鏄墠闈㈢殑mCursor娌℃湁閲婃斁涓�洿涓嶄负绌猴紝
				}
				else
				{
					return mCursor.getCount();
				}
			}
			else
			{
				return 0;
			}
		}
		
		@Override
		public Object getItem(
				int arg0 )
		{
			return arg0;
		}
		
		@Override
		public long getItemId(
				int position )
		{
			return position;
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			ViewHolder holder = null;
			if( convertView == null )
			{
				convertView = mInflater.inflate( R.layout.add_post_item_layout , null );
				holder = new ViewHolder();
				holder.city = (TextView)convertView.findViewById( R.id.textview_city );
				// holder.city_pinyin = (TextView)
				// convertView.findViewById(R.id.textview_city_pinyin);//(涓浗澶╂皵缃戝煄甯傛暟鎹簱)
				// holder.province = (TextView)
				// convertView.findViewById(R.id.textview_province);//(涓浗澶╂皵缃戝煄甯傛暟鎹簱)
				convertView.setTag( holder );
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();
			}
			if( mCursor.getCount() == 0 )// sxd 淇敼鍩庡競
			{
				if( supportForeign )
				{
					holder.city.setText( mEditText.getText().toString().trim() );
				}
				else
				{
					holder.city.setText( R.string.city_fault );
				}
				// holder.city_pinyin.setText("");
				// holder.province.setText("");
				return convertView;
			}
			mCursor.moveToPosition( position );
			if( mCursor.getColumnName( 0 ).equals( "city" ) )
			{
				String name = mCursor.getString( 0 );
				holder.city.setText( name );
			}
			else
			{
				if( AppConfig.getInstance( mContext ).isMerge() )
				{
					StringBuffer name = new StringBuffer( mCursor.getString( ForeignCitysEntity.CITY_EN_INDEX ) );
					name.append( "," );
					name.append( mCursor.getString( ForeignCitysEntity.COUNTRY_EN_INDEX ) );
					holder.city.setText( name );
				}
			}
			if( AppConfig.getInstance( WeatherAddPost.this ).isHuaweiStyle() )
			{
				convertView.setBackgroundColor( Color.WHITE );
				holder.city.setTextColor( Color.BLACK );
			}
			/*
			 * //(浠ヤ笅涓浗澶╂皵缃戝煄甯傛暟鎹簱) String city_pinyin = mCursor.getString(3);
			 * long province_id = mCursor.getInt(0); if (name.indexOf(".") ==
			 * -1) { holder.city.setText(name); } else {
			 * holder.city.setText(SinaCitysEntity.getPrefix(name) + " - " +
			 * SinaCitysEntity.getSuffix(name)); } if (mEditContentType ==
			 * StringType.LATIN) { if (city_pinyin.indexOf(".") == -1) {
			 * holder.city_pinyin.setText(" " + city_pinyin); } else {
			 * holder.city_pinyin.setText(" " +
			 * SinaCitysEntity.getPrefix(city_pinyin) + " - " +
			 * SinaCitysEntity.getSuffix(city_pinyin)); } } else {
			 * holder.city_pinyin.setText(""); }
			 * holder.province.setText(provinces[(int) province_id]);
			 */
			return convertView;
		}
		
		public final class ViewHolder
		{
			
			public TextView city;
		}
	}
	
	@Override
	public void onItemClick(
			AdapterView<?> parent ,
			View view ,
			int position ,
			long id )
	{
		if( mCursor != null )
		{
			String name = null;
			boolean foreignCity = false;
			if( mCursor.getCount() == 0 )
			{
				if( supportForeign )
				{
					name = mEditText.getText().toString().trim();
				}
				else
				{
					return;
				}
			}
			else
			{
				if( mCursor.getColumnName( 0 ).equals( "city" ) )
				{
					mCursor.moveToPosition( position );
					name = mCursor.getString( 0 );
					foreignCity = false;
				}
				else if( AppConfig.getInstance( mContext ).isMerge() )
				{
					mCursor.moveToPosition( position );
					name = mCursor.getString( ForeignCitysEntity.CITY_EN_INDEX );
					foreignCity = true;
				}
			}
			// String city_num = mCursor.getString(2);
			// Log.v(TAG, "onItemClick name = " + name + " ,city_num = " +
			// city_num);
			// 鐩存帴鍙栧悗缂�紝鍒灏辨棤闇�慨鏀逛簡
			// String postCode = CitysEntity.getSuffix(name);
			String postCode = name;
			// 鍏堝湪鏁版嵁搴撲腑鏌ユ壘鏄惁涔嬪墠宸茬粡娣诲姞杩�
			ContentResolver resolver = getContentResolver();
			Cursor cursor = null;
			boolean found = false;
			Uri uri = Uri.parse( MainActivity.POSTALCODE_URI );
			cursor = resolver.query( uri , PostalCodeEntity.projection , PostalCodeEntity.POSTAL_CODE + " = '" + postCode + "'" + " and " + PostalCodeEntity.USER_ID + " = '0'" , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					found = true;
				}
			}
			// if(postCode.equals(getResources().getString(R.string.defaultcity))){
			// Intent intent = new Intent();
			// intent.setAction("com.cooee.weather.data.action.ADD_DAFAULT_CITY");
			// sendBroadcast(intent);
			// }
			// 没找到，则添加
			if( !found )
			{
				ContentValues values = new ContentValues();
				values.put( PostalCodeEntity.POSTAL_CODE , postCode );
				values.put( PostalCodeEntity.USER_ID , 0 ); // 0涓洪粯璁serId
				// values.put(PostalCodeEntity.CITY_NUM, city_num);
				resolver.insert( uri , values );
				Intent data = new Intent();
				data.putExtra( "citys" , postCode );
				data.putExtra( "foreignCity" , foreignCity );
				// 请求代码可以自己设置，这里设置成20
				setResult( 1 , data );
			}
			else
			{
				Intent data = new Intent();
				data.putExtra( "citys" , postCode );
				data.putExtra( "foreignCity" , foreignCity );
				// 请求代码可以自己设置，这里设置成20
				setResult( 1 , data );
				Toast.makeText( getApplicationContext() , postCode + " " + this.getResources().getString( R.string.cityexitmessage ) , Toast.LENGTH_SHORT ).show();
			}
			finish();
		}
	}
	
	private class citysAdapter extends BaseAdapter
	{
		
		private Context mcontext = null;
		private ProgressDialog mProgressDialog = null;
		private boolean canPosition = true;
		
		public citysAdapter(
				Context context )
		{
			mcontext = context;
			mInflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		}
		
		public final int getCount()
		{
			if( popcitys.length > 0 )
			{
				return popcitys.length;
			}
			return 0;
		}
		
		public final Object getItem(
				int position )
		{
			return null;
		}
		
		public final long getItemId(
				int position )
		{
			return position;
		}
		
		@Override
		public View getView(
				final int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			ViewHolder holder = new ViewHolder();
			// TextView label = (TextView)convertView;
			// 鎴戜滑娴嬭瘯鍙戠幇锛岄櫎绗竴涓猚onvertView澶栵紝鍏朵綑鐨勯兘鏄疦ULL锛屽洜姝ゅ鏋滄病鏈塿iew锛屾垜浠渶瑕佸垱寤�
			/*
			 * if(convertView == null){ convertView = new TextView(context); int
			 * itemId = R.layout.popcity_item; //convertView.set label =
			 * (TextView)convertView; } label.setText(popcitys[position]);
			 */
			if( convertView == null )
			{
				convertView = mInflater.inflate( R.layout.popcity_item , null );
				holder.textView = (TextView)convertView.findViewById( R.id.textview_city );
				holder.flag = true;
				convertView.setTag( holder );
			}
			else
			{
				holder.flag = false;
				holder = (ViewHolder)convertView.getTag();
			}
			if( holder != null )
			{
				holder.textView.setText( popcitys[position] );
				holder.textView.setTextSize( 18 );
				holder.textView.setTextColor( 0xffc3c3c3 );
			}
			convertView.setOnTouchListener( new OnTouchListener() {
				
				@Override
				public boolean onTouch(
						View v ,
						MotionEvent event )
				{
					// TODO Auto-generated method stub
					ViewHolder holder = new ViewHolder();
					final int UPDATE = 1;
					final int FAILURE = 2;
					final int SUCCESS = 3;
					final Handler positionHandler = new Handler() {
						
						public void handleMessage(
								Message msg )
						{
							if( mProgressDialog != null && mProgressDialog.isShowing() )
								mProgressDialog.dismiss();
							switch( msg.what )
							{
								case UPDATE:
									Toast.makeText( getApplicationContext() , mcontext.getResources().getString( R.string.position_updated ) , Toast.LENGTH_SHORT ).show();
									finish();
									break;
								case FAILURE:
									Toast.makeText( getApplicationContext() , mcontext.getResources().getString( R.string.position_failure ) , Toast.LENGTH_SHORT ).show();
									break;
								case SUCCESS:
									Toast.makeText( getApplicationContext() , mcontext.getResources().getString( R.string.position_success ) , Toast.LENGTH_SHORT ).show();
									finish();
									break;
								default:
									break;
							}
							canPosition = true;
						};
					};
					if( event.getAction() == MotionEvent.ACTION_DOWN )
					{
						holder.textView = (TextView)v.findViewById( R.id.textview_city );
						holder.textView.setTextSize( 24 );
						holder.textView.setTextColor( 0xffa2d853 );
					}
					else if( event.getAction() == MotionEvent.ACTION_UP )
					{
						if( 0 == position && AppConfig.getInstance( mcontext ).isPosition() )
						{
							if( !canPosition )
								return true;
							canPosition = false;
							mProgressDialog = new ProgressDialog( mcontext );
							mProgressDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );
							mProgressDialog.setMessage( getResources().getString( R.string.positioning ) );
							mProgressDialog.setIndeterminate( false );
							mProgressDialog.getWindow().setBackgroundDrawable( new ColorDrawable( 0x00000000 ) );
							mProgressDialog.getWindow().setLayout( 30 , 30 );
							mProgressDialog.setCancelable( false );
							mProgressDialog.show();
							String postCode = ResolverUtil.getLocatedCity( mcontext );
							if( postCode != null )
							{
								new Thread() {
									
									public void run()
									{
										String postCode = ResolverUtil.getLocatedCity( mcontext );
										String tmpPostCode = AppConfig.getInstance( mcontext ).getDefaultCity();
										Log.i( "weatherDataService" , "run() ---defaultCity = " + tmpPostCode );
										boolean success = false;
										if( tmpPostCode != null && !tmpPostCode.equalsIgnoreCase( postCode ) )
										{
											success = ResolverUtil.updateLocatedCity( WeatherAddPost.this , tmpPostCode );
											if( success )
											{
												postCode = tmpPostCode;
											}
										}
										Intent data = new Intent();
										data.putExtra( "citys" , postCode );
										if( AppConfig.getInstance( mContext ).isMerge() )
										{
											if( ResolverUtil.checkCityIsForeignCity( mContext,postCode ) )
											{
												data.putExtra( "foreignCity" , true );
											}
										}
										setResult( 1 , data );
										if( success )
										{
											positionHandler.obtainMessage( UPDATE ).sendToTarget();
										}
										else
										{
											positionHandler.obtainMessage( FAILURE ).sendToTarget();
										}
									};
								}.start();
							}
							else
							{
								new Thread() {
									
									public void run()
									{
										ResolverUtil.addLocatedCity( mcontext );
										String postCode = ResolverUtil.getLocatedCity( mcontext );
										Log.i( "weatherDataService" , "addLocatedCity ---postCode = " + postCode );
										if( postCode != null )
										{
											Intent data = new Intent();
											data.putExtra( "citys" , postCode );
											if( AppConfig.getInstance( mContext ).isMerge() )
											{
												if(  ResolverUtil.checkCityIsForeignCity( mContext, postCode ) )
												{
													data.putExtra( "foreignCity" , true );
												}
											}
											// 请求代码可以自己设置，这里设置成20
											setResult( 1 , data );
											positionHandler.obtainMessage( SUCCESS ).sendToTarget();
										}
										else
										{
											positionHandler.obtainMessage( FAILURE ).sendToTarget();
										}
									}
								}.start();
							}
						}
						else
						{
							Log.i( "cityTest" , "position = " + position );
							holder.textView = (TextView)v.findViewById( R.id.textview_city );
							holder.textView.setTextSize( 18 );
							holder.textView.setTextColor( 0xffc3c3c3 );
							String postCode = popcitys[position];
							ContentResolver resolver = getContentResolver();
							Cursor cursor = null;
							boolean found = false;
							Uri uri = Uri.parse( MainActivity.POSTALCODE_URI );
							cursor = resolver.query(
									uri ,
									PostalCodeEntity.projection ,
									PostalCodeEntity.POSTAL_CODE + " = '" + postCode + "'" + " and " + PostalCodeEntity.USER_ID + " = '0'" ,
									null ,
									null );
							if( cursor != null )
							{
								if( cursor.moveToFirst() )
								{
									found = true;
								}
								cursor.close();
							}
							// 娌℃壘鍒帮紝鍒欐坊鍔�
							if( !found )
							{
								ContentValues values = new ContentValues();
								values.put( PostalCodeEntity.POSTAL_CODE , postCode );
								values.put( PostalCodeEntity.USER_ID , 0 ); // 0涓洪粯璁serId
								// values.put(PostalCodeEntity.CITY_NUM, city_num);
								resolver.insert( uri , values );
								Intent data = new Intent();
								data.putExtra( "citys" , postCode );
								if( popularForeignCitys )
								{
									data.putExtra( "foreignCity" , popularForeignCitys );
								}
								// 请求代码可以自己设置，这里设置成20
								setResult( 1 , data );
							}
							else
							{
								Intent data = new Intent();
								data.putExtra( "citys" , postCode );
								if( popularForeignCitys )
								{
									data.putExtra( "foreignCity" , popularForeignCitys );
								}
								setResult( 1 , data );
								Toast.makeText( getApplicationContext() , postCode + " " + mcontext.getResources().getString( R.string.cityexitmessage ) , Toast.LENGTH_SHORT ).show();
							}
							finish();
						}
					}
					else
					{
						holder.textView = (TextView)v.findViewById( R.id.textview_city );
						holder.textView.setTextSize( 18 );
						holder.textView.setTextColor( 0xffc3c3c3 );
						if( AppConfig.getInstance( WeatherAddPost.this ).isHuaweiStyle() )
						{
							holder.textView.setTextColor( Color.BLACK );
						}
					}
					return true;
				}
			} );
			if( AppConfig.getInstance( WeatherAddPost.this ).isHuaweiStyle() )
			{
				convertView.setBackgroundColor( Color.WHITE );
				holder.textView.setTextColor( Color.BLACK );
			}
			return convertView;
		}
	}
	
	public static class ViewHolder
	{
		
		public TextView textView;
		public boolean flag;
	}
	
	
	
	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { // TODO
	 * Auto-generated method stub if(keyCode==KeyEvent.KEYCODE_HOME ) { return
	 * true; } return super.onKeyUp(keyCode, event); }
	 */
	private String isConSpeCharacters(
			String string )
	{
		// TODO Auto-generated method stub
		if( string.replaceAll( "[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*_*\\s*" , "" ).length() == 0 )
		{
			//如果不包含特殊字符
			return string;
		}
		return "";
	}
	
	/**
	 * 
	 *功能：判断当前系统语言是否为中文
	 * @return
	 * @author gaominghui 2015年10月20日
	 */
	private boolean isZh()
	{
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if( language.endsWith( "zh" ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
