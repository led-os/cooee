package com.search.kuso;


import java.util.ArrayList;
import java.util.List;

import com.t9search.model.PinyinUnit;
import com.t9search.model.T9PinyinUnit;
import com.t9search.util.PinyinUtil;


public class PinyinUtils
{
	
	public static boolean match(
			List<PinyinUnit> srcUnit ,
			List<PinyinUnit> dstUnit )
	{
		int srcIdx = 0;
		int dstIdx = 0;
		int srcStrIdx = 0;
		int dstStrIdx = 0;
		for( ; srcIdx < srcUnit.size() && dstIdx < dstUnit.size() ; )
		{
			PinyinUnit src = srcUnit.get( srcIdx );
			List<T9PinyinUnit> srcList = src.getT9PinyinUnitIndex();
			String srcPinyin = srcList.get( 0 ).getPinyin().toLowerCase();
			PinyinUnit dst = dstUnit.get( dstIdx );
			List<T9PinyinUnit> dstList = dst.getT9PinyinUnitIndex();
			String dstPinyin = dstList.get( 0 ).getPinyin().toLowerCase();
			char srcStr = srcPinyin.charAt( srcStrIdx );
			char dstStr = dstPinyin.charAt( dstStrIdx );
			if( srcStr == dstStr )
			{
				srcStrIdx++;
			}
			dstStrIdx++;
			if( srcStrIdx == srcPinyin.length() )
			{
				srcIdx++;
				srcStrIdx = 0;
			}
			if( dstStrIdx == dstPinyin.length() )
			{
				dstIdx++;
				dstStrIdx = 0;
			}
		}
		return srcIdx == srcUnit.size();
	}
	
	public static boolean match(
			String searchStr ,
			String dstString )
	{
		List<PinyinUnit> srcUnit = new ArrayList<PinyinUnit>();
		PinyinUtil.chineseStringToPinyinUnit( searchStr , srcUnit );
		List<PinyinUnit> dstUnit = new ArrayList<PinyinUnit>();
		PinyinUtil.chineseStringToPinyinUnit( dstString , dstUnit );
		int srcIdx = 0;
		int dstIdx = 0;
		int srcStrIdx = 0;
		int dstStrIdx = 0;
		for( ; srcIdx < srcUnit.size() && dstIdx < dstUnit.size() ; )
		{
			PinyinUnit src = srcUnit.get( srcIdx );
			List<T9PinyinUnit> srcList = src.getT9PinyinUnitIndex();
			String srcPinyin = srcList.get( 0 ).getPinyin().toLowerCase();
			PinyinUnit dst = dstUnit.get( dstIdx );
			List<T9PinyinUnit> dstList = dst.getT9PinyinUnitIndex();
			String dstPinyin = dstList.get( 0 ).getPinyin().toLowerCase();
			char srcStr = srcPinyin.charAt( srcStrIdx );
			char dstStr = dstPinyin.charAt( dstStrIdx );
			if( srcStr == dstStr )
			{
				srcStrIdx++;
			}
			dstStrIdx++;
			if( srcStrIdx == srcPinyin.length() )
			{
				srcIdx++;
				srcStrIdx = 0;
			}
			if( dstStrIdx == dstPinyin.length() )
			{
				dstIdx++;
				dstStrIdx = 0;
			}
		}
		return srcIdx == srcUnit.size();
	}
}
