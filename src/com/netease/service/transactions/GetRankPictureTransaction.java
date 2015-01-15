package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.RankPictureInfo;

/** 排行榜背景图片 */
public class GetRankPictureTransaction extends EgmBaseTransaction {
    private String version;

    public GetRankPictureTransaction(String version) {
        super(TRANSACTION_TYPE_RANK_PICTURE);
        
        this.version = version;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetRankPictureRequest(version);
        sendRequest(request);
    }
    
    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        RankPictureInfo info = null;
        
        if(obj != null && obj instanceof JsonElement){
            info = RankPictureInfo.fromGson((JsonElement)obj);
        }
        
        if(info != null){
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, info);
        }
        else{
            notifyDataParseError();
        }
    }

    
}
