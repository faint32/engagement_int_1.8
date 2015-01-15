package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 上传地理位置
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class UploadLoctaionTransaction extends EgmBaseTransaction {
    private String latitude, longtitude;
    private String provinceCode, cityCode, districtCode;

    public UploadLoctaionTransaction(String latitude, String longtitude, String provinceCode, String cityCode, String districtCode) {
        super(TRANSACTION_TYPE_UPLOAD_LOCATION);
        
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.provinceCode = provinceCode;
        this.cityCode = cityCode;
        this.districtCode = districtCode;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createUploadLocationRequest(latitude, longtitude, 
                provinceCode, cityCode, districtCode);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        super.onEgmTransactionSuccess(code, obj);
        
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
    }
}
