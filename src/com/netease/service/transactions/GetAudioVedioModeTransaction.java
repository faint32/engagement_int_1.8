package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.AudioVideoSelfMode;

/**
 * 删除语音自我介绍
 */
public class GetAudioVedioModeTransaction extends EgmBaseTransaction{

	public GetAudioVedioModeTransaction() {
		super(TRANSACTION_GET_AUDIO_VIDEO);
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createGetAudioVideoMode();
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {

        AudioVideoSelfMode modeInfo = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            modeInfo = gson.fromJson((JsonElement)obj, AudioVideoSelfMode.class);
        }
        if (modeInfo != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, modeInfo);
        } else {
            notifyDataParseError();
        }

    }
}
