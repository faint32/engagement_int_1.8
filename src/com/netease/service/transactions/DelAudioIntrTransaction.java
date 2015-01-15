package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.AudioIntroduce;

/**
 * 删除语音自我介绍
 */
public class DelAudioIntrTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	public DelAudioIntrTransaction() {
		super(TARNSACTION_DEL_AUDIO_INTR);
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createDelAudio();
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }
}
