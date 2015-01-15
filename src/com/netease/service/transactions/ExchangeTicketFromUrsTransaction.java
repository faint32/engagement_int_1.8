package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.common.task.example.StringAsyncTransaction;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.TicketInfo;
import com.netease.util.EnctryUtil;

/** 和URS用token换ticket */
public class ExchangeTicketFromUrsTransaction extends StringAsyncTransaction {
    private String productId, key, token;

    public ExchangeTicketFromUrsTransaction(String productId, String key, String token) {
        super(EgmBaseTransaction.TRANSACTION_TYPE_EXCHANGE_TICKET);
        
        this.productId = productId;
        this.key = key;
        this.token = token;
    }

    @Override
    public void onTransact() {
        String params = null;
        
        try {
            params = EnctryUtil.encryptForAES("token=" + token, key);
        } 
        catch (Exception e) {}
        
        THttpRequest request = EgmProtocol.getInstance().createExchangeTicketRequest(productId, params);
        sendRequest(request);
    }
    
    
    
    @Override
    protected void onTransactionSuccess(int code, Object obj){
        String ticket = null;
        TicketInfo info = null;
        
        if(obj != null && obj instanceof String){
            Gson gson = new Gson();
            info = gson.fromJson((String)obj, new TypeToken<TicketInfo>(){}.getType());
        }
        
        if(info != null){
            try {
                ticket = EnctryUtil.decryptForAES(info.ticket, key);
            } catch (Exception e) { }
        }
        
        if(ticket != null){
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, ticket);
        }
        else{
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }

    @Override
    protected void onTransactionError(int errCode, Object obj) {
        notifyError(errCode, obj);
    }
}
