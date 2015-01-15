package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.AudioIntroduce;

/**
 * 上传语音自我介绍
 */
public class UpdateAudioIntrTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	private String audioPath ;
	private int duration ;
	
	public UpdateAudioIntrTransaction(String audioPath ,int duration) {
		super(TARNSACTION_TYPE_UPDATE_AUDIO);
		this.audioPath = audioPath ;
		this.duration = duration ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createUpdateAudio(audioPath,duration);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        AudioIntroduce audioIntr = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            audioIntr = gson.fromJson(json, AudioIntroduce.class);
        }
        if (audioIntr != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, audioIntr);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }

}
