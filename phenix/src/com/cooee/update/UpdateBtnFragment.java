package com.cooee.update;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cooee.phenix.R;


public class UpdateBtnFragment extends Fragment
{
	
	private UpdateBtnCallBack mCallBack;
	private Button mUpdateBtn;
	
	@Override
	public View onCreateView(
			LayoutInflater inflater ,
			ViewGroup container ,
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		return inflater.inflate( R.layout.uiupdate_update_btn , container , false );
	}
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onActivityCreated( savedInstanceState );
		mUpdateBtn = (Button)this.getActivity().findViewById( R.id.updateBtn );
		mUpdateBtn.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				if( mCallBack != null )
				{
					mCallBack.updateBtnClick();
				}
			}
		} );
	}
	
	public void setCallBack(
			UpdateBtnCallBack callBack )
	{
		this.mCallBack = callBack;
	}
	
	public void setClickable(
			boolean clickable )
	{
		if( mUpdateBtn != null )
		{
			mUpdateBtn.setClickable( clickable );
			mUpdateBtn.setBackgroundResource( R.drawable.uiupdate_update_btn_close_unchecked_shape );
		}
	}
	
	public static interface UpdateBtnCallBack
	{
		
		public void updateBtnClick();
	}
}
