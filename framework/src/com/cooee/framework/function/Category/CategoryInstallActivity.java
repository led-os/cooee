package com.cooee.framework.function.Category;


import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooee.launcher.framework.R;


public class CategoryInstallActivity extends Activity
{
	
	//private RelativeLayout layout;
	private Timer timer = null;
	private TimerTask timerTask = null;
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		openWindowViews();
		timer = new Timer();
		timerTask = new TimerTask() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				CategoryInstallActivity.this.stopTimer();
				CategoryInstallActivity.this.finish();
			}
		};
		timer.schedule( timerTask , 1000 * 3 );
	}
	
	private void stopTimer()
	{
		if( timer != null )
		{
			timer.cancel();
			timer = null;
		}
		if( timerTask != null )
		{
			timerTask.cancel();
			timerTask = null;
		}
	}
	
	private void openWindowViews()
	{
		Bundle bundle = getIntent().getExtras();
		this.setContentView( R.layout.category_install_success );
		android.view.WindowManager.LayoutParams p = getWindow().getAttributes();
		Point realSize = new Point();
		Display display = getWindowManager().getDefaultDisplay();
		display.getRealSize( realSize );
		p.y = realSize.y / 5;
		getWindow().setAttributes( p );
		//layout = (RelativeLayout)this.findViewById( R.id.category_install_success );
		String pn = bundle.getString( "packageName" );
		String folderName = bundle.getString( "folderName" );
		ImageView image = (ImageView)this.findViewById( R.id.install_img );
		TextView install_title = (TextView)findViewById( R.id.install_title );
		TextView classified_folder = (TextView)findViewById( R.id.classified_folder );
		classified_folder.setText( folderName );
		try
		{
			PackageManager Pkgmanger = this.getPackageManager();
			image.setImageDrawable( Pkgmanger.getApplicationIcon( pn ) );
			PackageInfo pkgInfo = Pkgmanger.getPackageInfo( pn , 0 );
			install_title.setText( Pkgmanger.getApplicationLabel( pkgInfo.applicationInfo ) );
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			stopTimer();
			finish();
		}
	}
}
