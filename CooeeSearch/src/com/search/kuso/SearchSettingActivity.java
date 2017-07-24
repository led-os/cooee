package com.search.kuso;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cooee.search.R;
import com.search.kuso.adapter.EngineAdapter;

import cool.sdk.kuso.KuSoHelper;
import cool.sdk.kuso.KusoData;
import cool.sdk.kuso.KusoEngineInfo;


public class SearchSettingActivity extends Activity
{
	
	private ListView lv_search_engine;
	private RelativeLayout kuso_back_icon;
	private KusoData kusoData;
	private String head;//图片的url
	private String engine_url; //引擎的url
	//	private ImageLoader imageLoader;
	private List<KusoEngineInfo> list;
	private EngineAdapter apdater;
	private SharedPreferences sp;
	private Editor editor;
	private int k;
	private ImageView radioimage;
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		setContentView( R.layout.kuso_search_setting );
		super.onCreate( savedInstanceState );
		lv_search_engine = (ListView)findViewById( R.id.kuso_lv_search_engine );
		kuso_back_icon = (RelativeLayout)findViewById( R.id.kuso_back_icon );
		//		kuso_radio_engine_state = (ImageView)findViewById( R.id.kuso_radio_engine_state );
		kuso_back_icon.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent( SearchSettingActivity.this , SearchT9Main.class );
				//				Toast.makeText( SearchSettingActivity.this , head , 0 ).show();
				//				intent.putExtra( "head" , head );
				//				intent.putExtra( "engine_url" , engine_url );
				//				intent.putExtra( "k" , k );
				//				setResult( 200 , intent );
				finish();
			}
		} );
		kusoData = KuSoHelper.getInstance( this ).getKusoData();
		if( kusoData.getEngines().size() > 0 )
		{
			list = new ArrayList<KusoEngineInfo>();
			list.addAll( kusoData.getEngines() );
			apdater = new EngineAdapter( list , this );
			lv_search_engine.setAdapter( apdater );
			lv_search_engine.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
			lv_search_engine.requestFocus();
			radioimage = (ImageView)findViewById( R.id.kuso_radio_engine_state );
			lv_search_engine.setOnItemClickListener( new OnItemClickListener() {
				
				@Override
				public void onItemClick(
						AdapterView<?> parent ,
						View view ,
						int position ,
						long id )
				{
					head = list.get( position ).getR3();
					engine_url = list.get( position ).getR4() + list.get( position ).getR5();
					sp = getSharedPreferences( "sp_setting" , Context.MODE_PRIVATE );
					editor = sp.edit();
					editor.putString( "head" , head );
					editor.putString( "engine_url" , engine_url );
					editor.putInt( "k" , position );
					editor.commit();
					for( k = 0 ; k < list.size() ; k++ )
					{
						if( position == k )
						{
							list.get( k ).setR7( true );
						}
						else
						{
							list.get( k ).setR7( false );
						}
					}
					apdater.notifyDataSetChanged();
					Intent intent = new Intent();
					intent.putExtra( "head" , head );
					intent.putExtra( "engine_url" , engine_url );
					intent.putExtra( "k" , k );
					setResult( 200 , intent );
					finish();
				}
			} );
		}
	}
	
	@Override
	public boolean onKeyDown(
			int keyCode ,
			KeyEvent event )
	{
		// TODO Auto-generated method stub
		if( keyCode == KeyEvent.KEYCODE_BACK )
		{
			Intent intent = new Intent();
			intent.putExtra( "head" , head );
			intent.putExtra( "engine_url" , engine_url );
			intent.putExtra( "k" , k );
			setResult( 200 , intent );
			finish();
		}
		return super.onKeyDown( keyCode , event );
	}
}
