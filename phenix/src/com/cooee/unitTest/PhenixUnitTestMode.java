package com.cooee.unitTest;


import junit.framework.Assert;
import android.test.ApplicationTestCase;

import com.cooee.phenix.LauncherApplication;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class PhenixUnitTestMode extends ApplicationTestCase<LauncherApplication>
{
	
	private LauncherApplication mApplication;
	
	public PhenixUnitTestMode()
	{
		super( LauncherApplication.class );
		// TODO Auto-generated constructor stub
	}
	
	public void startTest() throws Exception
	{
		createApplication();
		mApplication = getApplication();
		LauncherDefaultConfig.setApplicationContext( mApplication.getApplicationContext() );
		Assert.assertEquals( true , ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE ) );
	}
}
