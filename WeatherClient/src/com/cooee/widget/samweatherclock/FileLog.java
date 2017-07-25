package com.cooee.widget.samweatherclock;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;


public class FileLog
{
	
	private static FileLog mFileLog;
	private static String mPath;
	private static Writer mWriter;
	private static FileOutputStream fos;
	private static SimpleDateFormat df;
	private static File mFile;
	
	private FileLog()
	{
		this.mPath = Environment.getExternalStorageDirectory() + File.separator + "WeatherClockLog.txt";
		mFile = new File( mPath );
		this.mWriter = null;
		this.fos = null;
	}
	
	public static FileLog getInstance() throws IOException
	{
		if( mFileLog == null )
		{
			mFileLog = new FileLog();
		}
		//mWriter = new BufferedWriter(new FileWriter(mPath), 2048);  
		fos = new FileOutputStream( mFile );
		// df = new SimpleDateFormat("[yy-MM-dd hh:mm:ss]: ");  
		return mFileLog;
	}
	
	public void close() throws IOException
	{
		mWriter.close();
	}
	
	public void print(
			String log ) throws IOException
	{
		fos.write( log.getBytes() );
		fos.write( "\n".getBytes() );
	}
	
	public void print(
			Class cls ,
			String log ) throws IOException
	{ //如果还想看是在哪个类里可以用这个方法  
		fos.write( log.getBytes() );
		fos.write( "\n".getBytes() );
	}
}
