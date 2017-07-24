package com.coco.theme.themebox.util;


import java.util.ArrayList;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


// @2014/11/21 ADD START by gaominghui
public class ChineseCharactorToPinYin
{
	
	private HanyuPinyinOutputFormat format = null;
	private static ChineseCharactorToPinYin sInstance;
	
	public ChineseCharactorToPinYin()
	{
		format = new HanyuPinyinOutputFormat();
		format.setCaseType( HanyuPinyinCaseType.LOWERCASE );
		format.setToneType( HanyuPinyinToneType.WITHOUT_TONE );
		format.setVCharType( HanyuPinyinVCharType.WITH_U_UNICODE );
	}
	
	public static ChineseCharactorToPinYin getInstance()
	{
		synchronized( ChineseCharactorToPinYin.class )
		{
			if( sInstance != null )
			{
				return sInstance;
			}
			sInstance = new ChineseCharactorToPinYin();
			return sInstance;
		}
	}
	
	public ArrayList<String> get(
			String inputString )
	{
		char[] input = inputString.trim().toCharArray();
		ArrayList<String> result = new ArrayList<String>();
		StringBuilder mBuilder = new StringBuilder();
		try
		{
			for( int i = 0 ; i < input.length ; i++ )
			{
				if( Character.toString( input[i] ).matches( "[\u4E00-\u9FA5]+" ) )
				{
					if( mBuilder.length() > 0 )
					{
						result.add( mBuilder.toString() );
						mBuilder.delete( 0 , mBuilder.length() );
					}
					String[] temp = PinyinHelper.toHanyuPinyinStringArray( input[i] , format );
					result.add( temp[0].toLowerCase() );
				}
				else
				{
					mBuilder.append( Character.toString( input[i] ).toLowerCase() );
					//result.add( Character.toString( input[i] ).toLowerCase() );
				}
			}
			if( mBuilder.length() > 0 )
			{
				result.add( mBuilder.toString() );
				mBuilder.delete( 0 , mBuilder.length() );
			}
		}
		catch( BadHanyuPinyinOutputFormatCombination e )
		{
			e.printStackTrace();
		}
		return result;
	}
}
//@2014/11/21 ADD END by gaominghui
