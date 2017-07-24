package com.cooee.favorites.clings;


import java.util.Locale;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooee.favorites.R;
import com.cooee.favorites.manager.FavoritesManager;


/**
 * //cheyingkun add whole view	//酷生活引导页
 * @author cheyingkun 酷生活引导页
 */
public class FavoritesClingsView extends LinearLayout
{
	
	private View mView;
	private Button mButton;
	private ImageView logoImg;
	public final static String FAVORITE_CLING_KEY = "showFavoriteClingsView";
	
	public FavoritesClingsView(
			Context context ,
			boolean switchEnableClings ,
			boolean enable_s5 )
	{
		super( context );
		LayoutInflater inflater = LayoutInflater.from( context ).cloneInContext( context );
		if( switchEnableClings )
		{
			if( enable_s5 )
			{
				mView = inflater.inflate( R.layout.favorite_clings_s5 , null );
			}
			else
			{
				mView = inflater.inflate( R.layout.favorite_clings , null );
			}
		}
		else
		{
			mView = null;
		}
		if( switchEnableClings && mView != null )
		{
			logoImg = (ImageView)mView.findViewById( R.id.favorites_clings_logo );
			if( enable_s5 )
			{
				if( isZh() )
				{
					logoImg.setBackgroundResource( R.drawable.favorite_clings_logo_zh_s5 );
				}
				else
				{
					logoImg.setBackgroundResource( R.drawable.favorite_clings_logo_en_s5 );
				}
			}
			else
			{
				if( isZh() )
				{
					logoImg.setBackgroundResource( R.drawable.favorite_clings_logo_zh );
				}
				else
				{
					logoImg.setBackgroundResource( R.drawable.favorite_clings_logo_en );
				}
			}
			mButton = (Button)mView.findViewById( R.id.favorites_cling_button );
			mButton.setClickable( true );
			mButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					FavoritesManager.getInstance().startFavoritesClingRemoveAnimation();
				}
			} );
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT );
			mView.setLayoutParams( layoutParams );
			addView( mView );
		}
	}
	
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
	
	public ObjectAnimator getRemoveAnimation()
	{
		//cheyingkun add start	//修改酷生活S5引导页动画。
		float alphaValue = getResources().getInteger( R.integer.favorite_clings_dismiss_alpha ) * 1.0f / 100;
		float scaleValue = getResources().getInteger( R.integer.favorite_clings_dismiss_scale ) * 1.0f / 100;
		//cheyingkun add end
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , alphaValue );
		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat( "scaleX" , scaleValue );
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat( "scaleY" , scaleValue );
		ObjectAnimator anim = new ObjectAnimator();
		anim.setTarget( this );
		anim.setValues( alpha , scaleX , scaleY );
		anim.setDuration( getResources().getInteger( R.integer.favorite_clings_anim_dismiss_duration ) );
		setLayerType( LAYER_TYPE_HARDWARE , null );
		anim.addListener( new AnimatorListenerAdapter() {
			
			public void onAnimationEnd(
					android.animation.Animator animation )
			{
				//移除
				ViewGroup parent = (ViewGroup)FavoritesClingsView.this.getParent();
				if( parent != null )
				{
					parent.removeView( FavoritesClingsView.this );
				}
				//状态还原
				FavoritesClingsView.this.setAlpha( 1f );
				FavoritesClingsView.this.setScaleX( 1f );
				FavoritesClingsView.this.setScaleY( 1f );
				//标记位
				SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( FavoritesManager.getInstance().getContainerContext() );
				mSharedPreferences.edit().putBoolean( FAVORITE_CLING_KEY , false ).commit();
			};
		} );
		return anim;
	}
}
