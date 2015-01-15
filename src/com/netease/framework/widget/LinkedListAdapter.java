package com.netease.framework.widget;

import java.util.LinkedList;
import java.util.List;

import android.widget.BaseAdapter;

/**
 * 一个简单的Adapter封装, 方便内存数据在listview等控件中的使用
 * 
 * @author Panjf
 * @date   2011-10-9
 * @param <T>
 */
public abstract class LinkedListAdapter<T> extends BaseAdapter {
	private LinkedList<T> 	mList;
	
	public LinkedListAdapter(){
		mList = new LinkedList<T>();
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public T getItem(int position) {
		if(position < 0 || position >= mList.size())
			return null;
		
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		return null;
//	}
	
	public void addTail(T object){
		if(object == null)
			return;
		
		synchronized (mList) {
			mList.addLast(object);
			notifyDataSetChanged();
		}
	}
	
	public void addTail(List<T> objects){
		if(objects == null || objects.size() == 0)
			return;
		
		synchronized (mList) {
			mList.addAll(objects);
			notifyDataSetChanged();
		}
	}
	
	public void addTail(T[] objects){
		if(objects == null || objects.length == 0)
			return;
		
		synchronized (mList) {
			for(int i = 0; i < objects.length; i++)
				mList.addLast(objects[i]);
			
			notifyDataSetChanged();
		}
	}
	
	public void addHead(T object){
		if(object == null)
			return;
		
		synchronized (mList) {
			mList.addFirst(object);
			notifyDataSetChanged();
		}
	}
	
	public void addHead(List<T> objects){
		if(objects == null || objects.size() == 0)
			return;
		
		synchronized (mList) {
			for(int i = objects.size()-1; i >= 0; i--)
				mList.addFirst(objects.get(i));
			
			notifyDataSetChanged();
		}
	}
	
	public void addHead(T[] objects){
		if(objects == null || objects.length == 0)
			return;
		
		synchronized (mList) {
			for(int i = objects.length-1; i >= 0; i--)
				mList.addFirst(objects[i]);
			
			notifyDataSetChanged();
		}
	}
	
	public void addHead(T[] objects, int begin, int size){
		if(objects == null || objects.length == 0)
			return;
		
		if(begin < 0)
			begin = 0;
		if(size >= objects.length - begin)
			size = objects.length - begin;
		
		synchronized (mList) {
			for(int i = size - 1 ; i >= begin; i--)
				mList.addFirst(objects[i]);
			
			notifyDataSetChanged();
		}
	}
	
	public void delete(int position){
		if(position < 0 || position >= mList.size())
			return;
		synchronized (mList) {
			mList.remove(position);
			
			notifyDataSetChanged();
		}
	}
	
	public void clear(){
		synchronized (mList) {
			mList.clear();
			
			notifyDataSetChanged();
		}
	}
	public void replace(int position ,T object){
		if(null == object || position < 0 || position >= mList.size())
			return;
		synchronized (mList) {
			mList.set(position, object);
			
			notifyDataSetChanged();
		}
	}
	
	public LinkedList<T> getLinkedList(){
		return mList;
	}
}
