package com.netease.common.receiver;

import com.netease.util.PlatformUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PlatformUtil.updateCurrentNetworkInfo(context);
	}

}
