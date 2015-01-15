package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.SearchListInfo;

/** 搜索 */
public class SearchTransaction extends EgmBaseTransaction {
    private int ageBegin, ageEnd, constellation, income, page;
    private int provinceCode;
    private int hasPrivatePic;

    public SearchTransaction(int ageBegin, int ageEnd, int constellation, int provinceCode, int hasPrivatePic, int income, int page) {
        super(TRANSACTION_TYPE_SEARCH);
        
        this.ageBegin = ageBegin;
        this.ageEnd = ageEnd;
        this.constellation = constellation;
        this.provinceCode = provinceCode;
        this.hasPrivatePic = hasPrivatePic;
        this.income = income;
        this.page = page;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createSearchRequest(ageBegin, ageEnd, constellation, provinceCode, hasPrivatePic, income, page);
        sendRequest(request);
    }
    
    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        SearchListInfo info = null;
        
        if(obj != null && obj instanceof JsonElement){
            Gson gson = new Gson();
            info = gson.fromJson((JsonElement)obj, new TypeToken<SearchListInfo>(){}.getType());
        }
        
        if(info != null && info.searchList != null){
	        	int totalPage = (int)Math.ceil(1f*info.totalCount/info.count);
	    		if(page < totalPage){
	    			info.count = info.searchList.size();
	    		}
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, info);
        }
        else{
            notifyDataParseError();
        }
    }
}
