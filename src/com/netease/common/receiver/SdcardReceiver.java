package com.netease.common.receiver;

import com.netease.util.PlatformUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SdcardReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PlatformUtil.updateSdcardMounted();
	}

}
