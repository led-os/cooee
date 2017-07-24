package com.cooee.framework.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * @author zhangjin
 *没有分类只是普通的Util
 */
public class ShellUtils
{
	
	static public String sync_do_exec(
			String cmd )
	{
		String s = "\n";
		try
		{
			java.lang.Process p = Runtime.getRuntime().exec( cmd );
			// zhangjin@2015/12/04 这里的返回值改成错误信息 UPD START
			//BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			BufferedReader in = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );
			// zhangjin@2015/12/04 UPD END
			String line = null;
			while( ( line = in.readLine() ) != null )
			{
				s = StringUtils.concat( s , line , "\n" );
			}
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
}
