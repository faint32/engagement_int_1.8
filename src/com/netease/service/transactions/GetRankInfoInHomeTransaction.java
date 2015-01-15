
package com.netease.service.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.RankListInfoInHome;
import com.netease.service.protocol.meta.RecommendActivityListInfo;

/**
 * @author lishang 获取Home级排行榜页面信息：不同榜单的背景，前三名信息等
 */
public class GetRankInfoInHomeTransaction extends EgmBaseTransaction {
    private String mUserId;
    public GetRankInfoInHomeTransaction(String userId) {
        super(EgmBaseTransaction.TRANSACTION_GET_RANK_LIST_INFO_IN_HOME);
        this.mUserId=userId;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetRankListInHomeRequest(mUserId);
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        RankListInfoInHome mRankListInfo = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            mRankListInfo = gson.fromJson((JsonElement)obj, RankListInfoInHome.class);
        }
        if (mRankListInfo.rankInfoList != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, mRankListInfo);
        } else {
            notifyDataParseError();
        }
    }
}
