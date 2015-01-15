package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 获取碰缘分状态数据
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class SendYuanfenTransaction extends EgmBaseTransaction {
    /** 碰缘分类型 0，语音；1，文字 */
    private int type;
    private String voicePath;
    private String text;
    private int duration;   //录音时长（单位：秒）(女)

    
    public SendYuanfenTransaction(int type, String voicePath, String text, int duration){
        super(TRANSACTION_TYPE_SEND_YUANFEN);
        
        this.type = type;
        this.voicePath = voicePath;
        this.text = text;
        this.duration = duration;
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createSendYuanfenRequest(type, voicePath, text, duration);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        super.onEgmTransactionSuccess(code, obj);
        
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
    }
}
