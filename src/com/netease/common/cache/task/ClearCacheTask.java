package com.netease.common.cache.task;

import com.netease.common.cache.CacheManager;
import com.netease.common.task.TransTypeCode;
import com.netease.common.task.Transaction;

public class ClearCacheTask extends Transaction {

	public ClearCacheTask() {
		super(TransTypeCode.TYPE_CLEAR_CACHE);
	}

	@Override
	public void onTransact() {
		CacheManager.deleteCacheSync();
	}

}
