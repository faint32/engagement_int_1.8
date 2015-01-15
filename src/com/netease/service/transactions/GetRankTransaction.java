package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.RankListInfo;


public class GetRankTransaction extends EgmBaseTransaction {
    private int rankId;
    private int page;
    private int rankType;

    public GetRankTransaction(int rankId, int rankType, int page) {
        super(TRANSACTION_TYPE_GET_RANK_LIST);
        
        this.rankId = rankId;
        this.page = page;
        this.rankType = rankType;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetRankRequest(rankId, rankType, page);
        sendRequest(request);
    }
    
    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        RankListInfo info = null;
        
        if(obj != null && obj instanceof JsonElement){
            Gson gson = new Gson();
            info = gson.fromJson((JsonElement)obj, new TypeToken<RankListInfo>(){}.getType());
        }
        
        if(info != null && info.userList != null){
        		int totalPage = (int)Math.ceil(1f*info.totalCount/info.count);
        		if(page < totalPage){
        			info.count = info.userList.size();
        		}
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, info);
        }
        else{
            notifyDataParseError();
        }
    }
}
