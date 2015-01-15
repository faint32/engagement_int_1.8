package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 删除语音自我介绍
 */
public class AudioVedioSwitchTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	private int mMode;
	public AudioVedioSwitchTransaction(int mode) {
		super(TRANSACTION_SWITCH_AUDIO_VIDEO);
		this.mMode=mode;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createAudioVideoSwitch(mMode);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }
}
