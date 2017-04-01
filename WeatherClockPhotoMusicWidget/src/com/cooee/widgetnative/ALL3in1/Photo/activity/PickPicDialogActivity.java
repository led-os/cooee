package com.cooee.widgetnative.ALL3in1.Photo.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooee.widgetnative.ALL3in1.R;
import com.cooee.widgetnative.ALL3in1.Photo.PhotoManager;


/**
 * 长得像dialog的activity
 * @author cheyingkun
 */
public class PickPicDialogActivity extends Activity implements View.OnClickListener
{
	
	private TextView vViewImg = null;
	private ImageView vLine = null;
	private Uri mImgUri = null;
	/**是否正在显示图片,用来决定是否需要显示查看图片的点击选项*/
	private boolean mIfShowViewImg = false;
	//
	public static final String INTENT_REQUEST = "intent_request_all";
	public static final int PICTURE = 10101;
	public static final int ALBUM = 10102;
	
	@Override
	protected void onCreate(
			android.os.Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		PhotoManager mPhotoManager = PhotoManager.getInstance( getApplication() );
		mPhotoManager.isOpenActivity = false;
		mIfShowViewImg = mPhotoManager.ifShowViewImg();
		View dialogView = View.inflate( this , R.layout.photo_pick_pic_dialog_activity_layout , null );
		// YANGTIANYU@2016/05/05 ADD START
		// 增加开关控制是否显示相册选项 【c_0004226】
		if( !mPhotoManager.isShowChooseAlbum() )
		{
			View llChooseAlbum = dialogView.findViewById( R.id.ll_photo_choose_album );
			llChooseAlbum.setVisibility( View.GONE );
		}
		// YANGTIANYU@2016/05/05 ADD END
		vViewImg = (TextView)dialogView.findViewById( R.id.dialog_view_photos );
		vLine = (ImageView)dialogView.findViewById( R.id.dialog_line );
		if( !mIfShowViewImg )
		{
			hideViewItem();
		}
		else
		{
			showViewItem();
			setCurUri( mPhotoManager.getCurUri() );
		}
		setContentView( dialogView );
	}
	
	/**
	 * 隐藏浏览图片选项
	 * @author yangtianyu 2016-4-11
	 */
	private void hideViewItem()
	{
		if( vViewImg != null )
			vViewImg.setVisibility( View.GONE );
		if( vLine != null )
			vLine.setVisibility( View.GONE );
	}
	
	/**
	 * 显示浏览图片选项
	 * @author yangtianyu 2016-4-11
	 */
	private void showViewItem()
	{
		if( vViewImg != null )
			vViewImg.setVisibility( View.VISIBLE );
		if( vLine != null )
			vLine.setVisibility( View.VISIBLE );
	}
	
	/**
	 * 设置浏览图片时使用的Uri
	 * @param imgUri 浏览图片时打开的图片Uri
	 * @author yangtianyu 2016-4-11
	 */
	private void setCurUri(
			Uri imgUri )
	{
		this.mImgUri = imgUri;
	}
	
	@Override
	public void onClick(
			View v )
	{
		Intent picture = null;
		Log.d( "" , "cyk onClick: " + v );
		switch( v.getId() )
		{
			case R.id.dialog_photo_choose_album://相册
				picture = new Intent();// "android.intent.action.ALBUM_PICK" 
				ComponentName cmp = new ComponentName( "com.android.gallery3d" , "com.android.gallery3d.app.AlbumPicker" );
				picture.setComponent( cmp );
				startActivityForResult( picture , ALBUM );
				break;
			case R.id.dialog_choose_img://图片
				picture = new Intent( Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
				startActivityForResult( picture , PICTURE );
				break;
			case R.id.dialog_view_photos:
				if( mImgUri != null )
				{
					Intent enterIntent = new Intent( Intent.ACTION_VIEW , mImgUri );
					Log.d( "" , "cyk mImgUri: " + mImgUri );
					enterIntent.putExtra( "fromWidget" , true );
					startActivity( enterIntent );
				}
				this.finish();
				break;
			case R.id.dialog_cancel:
				this.finish();
				break;
			case R.id.pick_pic_dialog_layout:
				this.finish();
				break;
			default:
				break;
		}
	}
	
	@Override
	protected void onActivityResult(
			int requestCode ,
			int resultCode ,
			android.content.Intent data )
	{
		Log.d( "" , " cyk onActivityResult: data " + data );
		if( data != null )
		{
			Intent broadIntent = new Intent();
			switch( requestCode )
			{
				case PICTURE:
					broadIntent.putExtra( PhotoManager.PICTURE_URI , data.getDataString() );
					break;
				default:
					break;
			}
			broadIntent.putExtra( INTENT_REQUEST , requestCode );
			Log.d( "" , " cyk sendBroadcast broadIntent: " + broadIntent );
			PhotoManager.getInstance( this ).setChangeImageIntent( broadIntent );
		}
		finish();
	}
}
