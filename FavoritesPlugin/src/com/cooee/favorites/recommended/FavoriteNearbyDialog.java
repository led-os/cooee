package com.cooee.favorites.recommended;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.cooee.favorites.R;


public class FavoriteNearbyDialog extends Dialog
{
	
	//	private Context context = null;
	RotateAnimation rotateAnimation;
	
	public FavoriteNearbyDialog(
			Context context ,
			boolean cancelable ,
			OnCancelListener cancelListener )
	{
		super( context , cancelable , cancelListener );
		//		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public FavoriteNearbyDialog(
			Context context )
	{
		super( context , R.style.CustomProgressDialog );
		//		this.context = context;
		LayoutInflater inflater = LayoutInflater.from( context ).cloneInContext( context );
		View view = inflater.inflate( R.layout.nearbydialog , null );
		ImageView img_loading = (ImageView)view.findViewById( R.id.loading );
		rotateAnimation = (RotateAnimation)AnimationUtils.loadAnimation( context , R.anim.totate__drawable );
		img_loading.setAnimation( rotateAnimation );
		setContentView( view );
	}
}
