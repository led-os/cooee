package com.iLoong.launcher.SetupMenu;


import java.text.Collator;
import java.util.ArrayList;


public class cut
{
	
	private final static Collator sCollator = Collator.getInstance();
	static
	{
		System.loadLibrary( "cut" );
	}
	
	/*
	 * sort： 排序函数
	 * type： 0：大到小 1：小到大
	 * key： 排序键值
	 * sortkey： 返回排序的索引
	*/
	public static native int sort(
			int type ,
			String[] key ,
			int[] sortkey );
	
	public static native int sort(
			int type ,
			int[] key ,
			int[] sortkey );
	
	/*
	 * bmp： 转换rgba8888 像素格式 为 bmp文件流
	 * w:图像宽 
	 * h:图像高
	 * pixel：rgba像素
	 * bmp： bmp文件
	 * return：bmp文件大小
	*/
	public static native int bmp(
			int w ,
			int h ,
			int[] pixel ,
			byte[] bmp );
	
	public static void sortNameAfterInstall(
			String[] nameKey ,
			int[] installKey ,
			int[] sortArray )
	{
		ArrayList<String> tempNameKeyArrayList = new ArrayList<String>();
		int[] tempSortArray = new int[sortArray.length];
		int sortsize = 0;
		for( int i = 1 ; i < sortArray.length ; i++ )
		{
			if( installKey[sortArray[i - 1]] == installKey[sortArray[i]] )
			{
				if( tempNameKeyArrayList.size() == 0 )
				{
					tempNameKeyArrayList.add( nameKey[sortArray[i - 1]] );
					tempNameKeyArrayList.add( nameKey[sortArray[i]] );
					tempSortArray[sortsize] = sortArray[i - 1];
					sortsize++;
					tempSortArray[sortsize] = sortArray[i];
					sortsize++;
				}
				else
				{
					tempNameKeyArrayList.add( nameKey[sortArray[i]] );
					tempSortArray[sortsize] = sortArray[i];
					sortsize++;
				}
			}
			else
			{
				if( tempNameKeyArrayList.size() != 0 )
				{
					String[] tempNameKeyArray = new String[sortsize];
					int[] tempSort = new int[sortsize];
					for( int k = 0 ; k < sortsize ; k++ )
					{
						tempNameKeyArray[k] = tempNameKeyArrayList.get( k );
					}
					sortByAlpha( 1 , tempNameKeyArray , tempSort );
					for( int m = 0 ; m < sortsize ; m++ )
					{
						tempSort[m] = tempSortArray[tempSort[m]];
					}
					for( int j = 0 ; j < sortsize ; j++ )
					{
						sortArray[i - j - 1] = tempSort[sortsize - j - 1];
					}
					sortsize = 0;
					tempNameKeyArrayList.clear();
				}
			}
		}
		if( tempNameKeyArrayList.size() != 0 )
		{
			String[] tempNameKeyArray = new String[sortsize];
			int[] tempSort = new int[sortsize];
			for( int k = 0 ; k < sortsize ; k++ )
			{
				tempNameKeyArray[k] = tempNameKeyArrayList.get( k );
			}
			sortByAlpha( 1 , tempNameKeyArray , tempSort );
			for( int m = 0 ; m < sortsize ; m++ )
			{
				tempSort[m] = tempSortArray[tempSort[m]];
			}
			for( int j = 0 ; j < sortsize ; j++ )
			{
				sortArray[sortArray.length - j - 1] = tempSort[sortsize - j - 1];
			}
			sortsize = 0;
			tempNameKeyArrayList.clear();
		}
	}
	
	public static void sortByAlpha(
			int type ,
			String[] key ,
			int[] sortKey )
	{
		Record[] records = new Record[key.length];
		for( int i = 0 ; i < key.length ; i++ )
		{
			records[i] = new Record();
			records[i].key = key[i];
			records[i].originalIndex = i;
		}
		quickSort( records , 0 , key.length - 1 , type , sortKey );
		for( int i = 0 ; i < key.length ; i++ )
		{
			sortKey[i] = records[i].originalIndex;
		}
		records = null;
	}
	
	private static int partition(
			Record[] arr ,
			int low ,
			int high ,
			int type ,
			int[] result )
	{
		String pivot = arr[low].key;//采用子序列的第一个元素作为枢纽元素 
		while( low < high )
		{
			//从后往前栽后半部分中寻找第一个小于枢纽元素的元素 
			while( low < high && ( ( sCollator.compare( arr[high].key , pivot ) >= 0 && type == 1 ) || ( sCollator.compare( arr[high].key , pivot ) <= 0 && type == 0 ) ) )
			{
				--high;
			}
			//将这个比枢纽元素小的元素交换到前半部分 
			swap( arr , low , high , result );
			//从前往后在前半部分中寻找第一个大于枢纽元素的元素 
			while( low < high && ( ( sCollator.compare( arr[low].key , pivot ) < 0 && type == 1 ) || ( sCollator.compare( arr[low].key , pivot ) > 0 && type == 0 ) ) )
			{
				++low;
			}
			swap( arr , low , high , result );//将这个枢纽元素大的元素交换到后半部分 
		}
		return low;//返回枢纽元素所在的位置 
	}
	
	private static void swap(
			Record arr[] ,
			int a ,
			int b ,
			int[] result )
	{
		if( a == b )
			return;
		Record s;
		s = arr[a];
		arr[a] = arr[b];
		arr[b] = s;
	}
	
	private static void quickSort(
			Record[] records ,
			int low ,
			int high ,
			int type ,
			int[] result )
	{
		if( low < high )
		{
			int n = partition( records , low , high , type , result );
			quickSort( records , low , n , type , result );
			quickSort( records , n + 1 , high , type , result );
		}
	}
	
	static class Record
	{
		
		public String key;
		public int originalIndex;
	}
}
