package com.netease.android.service;

import java.util.LinkedList;
import java.util.List;

public class PendingMediaStoreSerializer {
	private static final String FILENAME = "pending_media.json";
	private static final String TAG = "PendingMediaStoreSerializer";
	private static PendingMediaStoreSerializer sInstance;
	private final List<Runnable> mDeserializationListeners = new LinkedList<Runnable>();
	private volatile boolean mDeserialized = false;

	private PendingMediaStoreSerializer() {
	}

	/** @deprecated */
	private static void createInstance() {
		if (sInstance == null) {
			sInstance = new PendingMediaStoreSerializer();
		}
	}

	public static PendingMediaStoreSerializer getInstance() {
		if (sInstance == null)
			createInstance();
		return sInstance;
	}

	// ERROR //
	public void addDeserializationListener(Runnable runnable) {
		mDeserializationListeners.add(runnable);
	}

	/** @deprecated */
	public void serialize() {
	}

	public void serializeAsync() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				serialize();
			}
		}).start();
	}
}