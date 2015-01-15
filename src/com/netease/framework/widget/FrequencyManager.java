

package com.netease.framework.widget;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

/**
 * 按使用次数或最近使用的时间管理数据的一个类, 
 * 数据保存于..\catch\FreQuency_{Tag}下
 * @author Panjf
 * @date   2011-10-9
 */
public class FrequencyManager {
	public static final int FRE_COUNTS = 1; //按使用次数
	public static final int FRE_RECENTY = 2;//按最近使用
	
	
	private Context mContext;
	private String mTag;
	private String mFrequencyFile;
	private int mFreType = FRE_RECENTY;
	
	private int mDataCapacity = 0x0000ffff;
	private List<FrequencyInfo> mFreData = new LinkedList<FrequencyInfo>();
	
	public FrequencyManager(Context context, String tag, int type){
		mContext = context;
		mTag = tag;
		mFreType = type;
		mFrequencyFile = mContext.getCacheDir().getPath()+ "/Frequency_" + mTag;
		
		ini();
	}
	
	private void ini() {
		File file = new File(mFrequencyFile);
		if (file.exists()) {
			try {
				FileInputStream in = new FileInputStream(file);
				DataInputStream dis = new DataInputStream(in);
				if(mFreData == null)
					mFreData = new LinkedList<FrequencyInfo>();
				
				mFreData.clear();
				while(dis.available() > 0){
					FrequencyInfo info = new FrequencyInfo();
					info.fre = dis.readLong();
					info.str = dis.readUTF();
					mFreData.add(info);
				}
				
				in.close();
				dis.close();
				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				//Collections.sort(mFreData);
			}
			
		}
	}
	
	
	public void setCapacity(int size){
		mDataCapacity = size;
		if(mFreData.size() > mDataCapacity){
			Collections.sort(mFreData);
			mFreData = mFreData.subList(0, mDataCapacity);
		}
	}
	
	private long getFrecount(long oldFre){
		long fre = oldFre+1;
		if(mFreType == FRE_RECENTY){
			fre = System.currentTimeMillis();
		}
		
		return fre;
	}
	
	public void clearAll(){
		mFreData.clear();
	}
	
	public void addData(String str, long fre){
		if(str == null)
			return;
	
		FrequencyInfo info = new FrequencyInfo(fre, str);
		mFreData.add(info);
	}
	public void addFrequecy(String str){
		FrequencyInfo info = new FrequencyInfo(getFrecount(0), str);
				
		int index = mFreData.indexOf(info);
		if(index != -1){
			info = mFreData.get(index);
			info.fre = getFrecount(info.fre);
		}
		else{
			mFreData.add(info);
		}
	}
	
	public List<FrequencyInfo> getFreData(){
		Collections.sort(mFreData);
		return mFreData;
	}
	
	public FrequencyInfo getFreData(int pos){
		if(pos < mFreData.size())
			return mFreData.get(pos);
		
		return null;
	}
	
	public int getFreDataSize(){
		return mFreData.size();
	}
	
	public void save(){
		Collections.sort(mFreData);
		
		try {
			FileOutputStream fo = new FileOutputStream(mFrequencyFile);
			DataOutputStream dos = new DataOutputStream(fo);
			int size = mFreData.size();
			for(int i = 0; i < size; i++){
				FrequencyInfo data = mFreData.get(i);
				if(data == null)
					continue;
				dos.writeLong(data.fre);
				dos.writeUTF(data.str);
			}
			dos.close();
			fo.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class FrequencyInfo implements Comparable<FrequencyInfo>{
		public long fre;
		public String str;
		
		public FrequencyInfo(){
			this(0, null);
		}
		public FrequencyInfo(long fre, String str){
			this.fre = fre;
			this.str = str;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null)
				return false;
			
			if (this == o) {
	            return true;
	        }
			
	        if (o instanceof FrequencyInfo) {
	        	if(this.str != null && this.str.equals(((FrequencyInfo) o).str))
	        		return true;
	        }
	        
	        return false;
		}
		
		@Override
		public int compareTo(FrequencyInfo another) {
			if(another != null){
				long diff = another.fre - this.fre;
				if(diff == 0)
					return 0;
				else return diff > 0 ? 1:-1;
			}
			return 0;
		}
	}
}
