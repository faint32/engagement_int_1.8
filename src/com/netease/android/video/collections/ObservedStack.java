package com.netease.android.video.collections;

import java.util.Iterator;
import java.util.LinkedList;

public class ObservedStack<T> implements Iterable<T> {
	private LinkedList<T> mLinkedList = new LinkedList<T>();
	private ObservedStack.StackObserver<T> mStackObserver;

	public boolean add(T paramT) {
		boolean bool = this.mLinkedList.add(paramT);
		if (this.mStackObserver != null)
			this.mStackObserver.onItemAdded(paramT);
		return bool;
	}

	protected T getLast() {
		T object = null;
		if (this.mLinkedList.size() > 0) {
			object = this.mLinkedList.getLast();
		}
		return object;
	}

	public boolean isEmpty() {
		return this.mLinkedList.isEmpty();
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private T mCurrentItem;
			Iterator<T> mIterator = mLinkedList.iterator();

			@Override
			public boolean hasNext() {
				return this.mIterator.hasNext();
			}

			@Override
			public T next() {
				this.mCurrentItem = this.mIterator.next();
				return this.mCurrentItem;
			}

			@Override
			public void remove() {
				this.mIterator.remove();
				if (mStackObserver != null) {
					mStackObserver.onItemRemoved(this.mCurrentItem);
				}
			}
		};
	}

	public boolean remove(T paramT) {
		boolean success = this.mLinkedList.remove(paramT);
		if ((success) && (this.mStackObserver != null))
			this.mStackObserver.onItemRemoved(paramT);
		return success;
	}

	public void setStackObserver(
			ObservedStack.StackObserver<T> paramStackObserver) {
		this.mStackObserver = paramStackObserver;
	}

	public int size() {
		return this.mLinkedList.size();
	}

	public abstract interface StackObserver<T> {
		public abstract void onItemAdded(T paramT);

		public abstract void onItemRemoved(T paramT);
	}
}