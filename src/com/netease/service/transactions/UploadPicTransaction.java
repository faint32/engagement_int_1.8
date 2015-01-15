package com.netease.service.transactions;


import java.io.File;
import java.net.URI;

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.common.image.util.ImageUtil;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.PictureInfo;

public class UploadPicTransaction extends EgmBaseTransaction{
	
	private static final boolean DEBUG = false ;
	
	private String mFilePath ;
	private int picType ;
	private int isCamera ;
	private String mFilePathTemp;

	public UploadPicTransaction(String filePath ,int picType, int isCamera) {
		super(TRANSACTION_UPLOAD_PIC);
		this.mFilePath = filePath ;
		this.picType = picType ;
		this.isCamera = isCamera;
	}
	
	@Override
	public void onTransact() {
	    if(!TextUtils.isEmpty(mFilePath)){
            File file = null;
            String path = null;
            if (URLUtil.isFileUrl(mFilePath)) {
                file = new File(URI.create(mFilePath));
            } else {
                file = new File(mFilePath);
            }
            if(file != null){
                path = file.getPath();
                if(!TextUtils.isEmpty(path)){
                    String tempFile = EgmUtil.getCacheDir().toString();
                    if(!TextUtils.isEmpty(tempFile)){
                        tempFile += System.currentTimeMillis() + "_temp.jpg";
                        if(ImageUtil.saveResizeTmpFile(path, tempFile, EgmProtocolConstants.SIZE_MAX_PICTURE, EgmProtocolConstants.PIC_QULITY)){
                            File fileTemp = new File(tempFile);
                            if(fileTemp.exists()){
                                mFilePathTemp = tempFile;
                            }
                        }
                    }
                }
            }
        }
		THttpRequest request;
		request = EgmProtocol.getInstance().createUploadPic(TextUtils.isEmpty(mFilePathTemp)?mFilePath:mFilePathTemp, picType, isCamera);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		PictureInfo data = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement jsonObject = ((JsonElement)obj).getAsJsonObject().get("pictureInfo");
            data = gson.fromJson(jsonObject, PictureInfo.class);
        }
        if(!TextUtils.isEmpty(mFilePathTemp)){
            File file = new File(mFilePathTemp);
            if(file.exists()){
                file.delete();
            }
        }
        if (data != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, data);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
	@Override
	protected void onEgmTransactionError(int errCode, Object obj) {
	    if(!TextUtils.isEmpty(mFilePathTemp)){
            File file = new File(mFilePathTemp);
            if(file.exists()){
                file.delete();
            }
        }
	    super.onEgmTransactionError(errCode, obj);
	}

}
