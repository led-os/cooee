// xiatian add whole file //设置默认桌面引导（该Activity用来清空之前选择的默认桌面）
package com.cooee.framework.function.DefaultLauncher;


import android.app.Activity;
import android.os.Bundle;


public class DefaultLauncherActivity extends Activity
{
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		finish();
	}
}
