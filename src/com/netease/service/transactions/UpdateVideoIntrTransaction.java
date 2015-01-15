package com.netease.service.transactions;

import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmHttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.VideoIntroduce;


public class UpdateVideoIntrTransaction extends EgmBaseTransaction {

	private String mVideo;
	private String mCover;
	private long mDuration;
	private int mIsCamera;
	private Parcelable mParam;
	
	/**
	 * 
	 * @param video 视频
	 * @param cover 视频封面
	 * @param duration 毫秒
	 * @param isCamera 是否从相机
	 * @param param
	 */
	public UpdateVideoIntrTransaction(String video, String cover, long duration,
			int isCamera, Parcelable param) {
		super(TARNSACTION_TYPE_UPDATE_VIDEO);
		
		mVideo = video;
		mCover = cover;
		mDuration = duration;
		mIsCamera = isCamera;
		mParam = param; 
	}

	@Override
	public void onTransact() {
		EgmHttpRequest request = EgmProtocol.getInstance().createUploadVideo(
				mVideo, mCover, (int) (mDuration / 1000), mIsCamera);
		sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		VideoIntroduce videoIntr = null;
//        if (obj != null && obj instanceof JsonElement) {
//            Gson gson = new Gson();
//            JsonElement json = (JsonElement)obj;
//            videoIntr = gson.fromJson(json, VideoIntroduce.class);
//        }
//        
//        if (videoIntr != null) {
//        	videoIntr.videoDuration = (int) mDuration;
//            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, videoIntr);
//        } else {
//            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
//                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
//        }
		
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, videoIntr);
    }
	
}
