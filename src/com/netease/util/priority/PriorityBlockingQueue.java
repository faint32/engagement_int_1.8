package com.netease.util.priority;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PriorityBlockingQueue<T extends Priority> {

	/**
	 * 并发参考
	 * http://blog.csdn.net/fw0124/article/details/6672522	
	 */
//	private static final int MAX_EMERGENCY_SIZE = 1;
//	private static final int MAX_HIGHER_SIZE = 6;
//	private static final int MAX_HIGH_SIZE = 12;
	
	private static final Object[] mQueueLock = new Object[0];
	
	LinkedList<T> mEmergencyQueue;
	LinkedList<T> mHigherQueue;
	LinkedList<T> mHighQueue;
	LinkedList<T> mNormalQueue;
	LinkedList<T> mLowQueue;
	
	public PriorityBlockingQueue() {
		mEmergencyQueue = new LinkedList<T>();
		mHigherQueue = new LinkedList<T>();
		mHighQueue = new LinkedList<T>();
		mNormalQueue = new LinkedList<T>();
		mLowQueue = new LinkedList<T>();
	}
	
	/**
	 * 检索并移除此队列的头部，如果此队列不存在任何元素，则一直等待。
	 * 
	 * @return
	 */
	public T take() {
		synchronized (mQueueLock) {
			if (mNormalQueue.size() > 0
					|| mLowQueue.size() > 0
					|| mHighQueue.size() > 0
					|| mHigherQueue.size() > 0
					|| mEmergencyQueue.size() > 0) {
				return poll();
			} else {
				try {
					mQueueLock.wait();
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				return poll();
			}
		}
	}

	private void checkSame(LinkedList<T> queue, LinkedList<T> target, T obj) {
		for (int i = queue.size() - 1; i >= 0; i--) {
			T tmp = queue.get(i);
			if (tmp.equals(obj)) {
				queue.remove(i);
				target.add(tmp);
			}
		}
	}
	
	private void checkSame(LinkedList<T> queue, LinkedList<T> target, Class clazz) {
		for (int i = queue.size() - 1; i >= 0; i--) {
			T tmp = queue.get(i);
			if (clazz.isInstance(tmp)) {
				queue.remove(i);
				target.add(tmp);
			}
		}
	}

	/**
	 * 检索并移除此队列的头，如果此队列为空，则返回 null。
	 * 
	 * @return
	 */
	public T poll() {
		synchronized (mQueueLock) {
			return remove(0);
		}
	}

	/**
	 * 将指定的元素添加到队列的尾部.
	 * 
	 * @param o
	 */
	public void put(T o) {
		put(o, Priority.NORMAL);
	}
	
	/**
	 * 附带优先级和是否逆向进行放入
	 * @param o
	 * @param priority 0-4
	 * @see Priority
	 */
	public void put(T o, int priority) {
		if (o == null)
			return;
		
		boolean forceHeader = (priority & Priority.HRADER_TAG) != 0;
		priority &= 0xFF;
		
		synchronized (mQueueLock) {
			switch (priority) {
			case Priority.EMERGENCY:
				if (forceHeader) {
					mEmergencyQueue.addFirst(o);
				}
				else {
					mEmergencyQueue.add(o);
				}
				break;
				
			case Priority.HIGHER:
				if (forceHeader) {
					mHigherQueue.addFirst(o);
				}
				else {
					mHigherQueue.add(o);
				}
				
				break;
				
			case Priority.HIGH:
				if (forceHeader) {
					mHigherQueue.addFirst(o);
				}
				else {
					mHigherQueue.add(o);
				}
				
				break;

			case Priority.LOW:
				if (forceHeader) {
					mLowQueue.addFirst(o);
				}
				else {
					mLowQueue.add(o);
				}
				break;
				
//			case Priority.NORMAL:
			default:
				if (forceHeader) {
					mNormalQueue.addFirst(o);
				}
				else {
					mNormalQueue.add(o);
				}
				break;
			}
			
			mQueueLock.notifyAll();
		}
		
	}
	
	public boolean remove(T o) {
		if (o == null) {
			return false;
		}
		
		synchronized (mQueueLock) {
			return mEmergencyQueue.remove(o) 
					|| mHigherQueue.remove(o)
					|| mHighQueue.remove(o)
					|| mNormalQueue.remove(o)
					|| mLowQueue.remove(o);
		}
	}
	
	public T remove(int index) {
		synchronized (mQueueLock) {
			if (index >= 0) {
				if (index < mEmergencyQueue.size()) {
					return mEmergencyQueue.remove(index);
				}
				index -= mEmergencyQueue.size();
				
				if (index < mHigherQueue.size()) {
					return mHigherQueue.remove(index);
				}
				
				index -= mHigherQueue.size();
				
				if (index < mHighQueue.size()) {
					return mHighQueue.remove(index);
				}
				
				index -= mHighQueue.size();
				if (index < mNormalQueue.size()) {
					return mNormalQueue.remove(index);
				}
				
				index -= mNormalQueue.size();
				if (index < mLowQueue.size()) {
					return mLowQueue.remove(index);
				}
			}
			
			return null;
		}
	}
	
	public void adjustRequestPriorityByGID(int gid, int priority) {
		if (gid < 0) {
			return ;
		}
		
		boolean header = (priority & Priority.HRADER_TAG) != 0;
		priority &= 0xFF;
		
		synchronized (mQueueLock) {
			LinkedList<T> target = getPriorityQueue(priority);
			
			if (header) {
				adjustPriority(mLowQueue, target, gid, header, priority);
				adjustPriority(mNormalQueue, target, gid, header, priority);
				adjustPriority(mHighQueue, target, gid, header, priority);
				adjustPriority(mHigherQueue, target, gid, header, priority);
				adjustPriority(mEmergencyQueue, target, gid, header, priority);
			}
			else {
				adjustPriority(mEmergencyQueue, target, gid, header, priority);
				adjustPriority(mHigherQueue, target, gid, header, priority);
				adjustPriority(mHighQueue, target, gid, header, priority);
				adjustPriority(mNormalQueue, target, gid, header, priority);
				adjustPriority(mLowQueue, target, gid, header, priority);
			}
		}
	}
	
	private LinkedList<T> getPriorityQueue(int priority) {
		priority &= 0xFF;
		switch (priority) {
		case Priority.EMERGENCY:
			return mEmergencyQueue;
			
		case Priority.HIGHER:
			return mHigherQueue;
			
		case Priority.HIGH:
			return mHighQueue;
			
		case Priority.LOW:
			return mLowQueue;
		}
		
		return mNormalQueue;
	}

	public void lowPriority() {
		synchronized (mQueueLock) {
			adjustPriority(mLowQueue, mNormalQueue, Priority.NORMAL);
			adjustPriority(mEmergencyQueue, mLowQueue, Priority.LOW);
			adjustPriority(mHigherQueue, mLowQueue, Priority.LOW);
			adjustPriority(mHighQueue, mLowQueue, Priority.LOW);
			adjustPriority(mNormalQueue, mLowQueue, Priority.LOW);
		}
	}
	
	private void adjustPriority(LinkedList<T> v, LinkedList<T> target, int priority) {
		for (int i = 0; i < v.size(); i++) {
			Priority obj = (Priority)v.get(i);
			if (obj != null) {
				obj.setPriority(priority);
			}
			target.add(v.get(i));
		}
		v.clear();
	}
	
	private void adjustPriority(LinkedList<T> v, LinkedList<T> target, int gid, boolean header, int priority) {
		if (v == target) {
			return ;
		}
		
		List<T> tmp = new LinkedList<T>();
		
		for (int i = v.size() - 1; i >= 0; i--) {
			T obj = v.get(i);
			if (obj != null && obj.getGroupID() == gid) {
				v.remove(i);
				
				obj.setPriority(priority);
				tmp.add(obj);
//				if (header) {
//					target.add(0, obj);
//				}
//				else {
//					target.add(obj);
//				}
			}
		}
		
		if (tmp.size() > 0) {
			Collections.reverse(tmp);
			
			target.addAll(0, tmp);
		}
	}

	public void clear() {
		synchronized (mQueueLock) {
			mEmergencyQueue.clear();
			mHigherQueue.clear();
			mHighQueue.clear();
			mNormalQueue.clear();
			mLowQueue.clear();
			mQueueLock.notifyAll();
		}
	}
	
	public LinkedList<T> removeSameObjects(Class clazz) {
		LinkedList<T> target = null;
		
		if (clazz != null) {
			target = new LinkedList<T>();
			
			synchronized (mQueueLock) {
				checkSame(mEmergencyQueue, target, clazz);
				checkSame(mHigherQueue, target, clazz);
				checkSame(mHighQueue, target, clazz);
				checkSame(mNormalQueue, target, clazz);
				checkSame(mLowQueue, target, clazz);
			}
		}
		
		return target;
	}
	
	public LinkedList<T> removeSameObjects(T obj) {
		LinkedList<T> target = new LinkedList<T>();
		return removeSameObjects(target, obj);
	}
	
	public LinkedList<T> removeSameObjects(LinkedList<T> target, T obj) {
		if (target == null) {
			target = new LinkedList<T>();
		}
		
		synchronized (mQueueLock) {
			checkSame(mEmergencyQueue, target, obj);
			checkSame(mHigherQueue, target, obj);
			checkSame(mHighQueue, target, obj);
			checkSame(mNormalQueue, target, obj);
			checkSame(mLowQueue, target, obj);
		}
		
		return target;
	}

}
