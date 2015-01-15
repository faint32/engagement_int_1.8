package com.netease.service.transactions;



import java.util.List;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.engagement.dataMgr.EmotConfigManager;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.EmotConfigResult;
import com.netease.service.protocol.meta.FaceGroupInfo;
import com.netease.share.sticker.model.CategoryData;
import com.netease.share.sticker.model.StickerHelper;
import com.netease.share.sticker.util.DependentUtils;

/**
 * 获取表情配置数据
 */
public class GetEmotConfigTransaction extends EgmBaseTransaction{

	private String version ;
	private String faceVersion ;
	
	public GetEmotConfigTransaction(String version, String faceVersion) {
		super(TARNSACTION_GET_EMOT_CONFIG);
		this.version = version ;
		this.faceVersion = faceVersion ;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createGetEmotConfig(version, faceVersion);
	    sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		EmotConfigResult result = null ;
        if (obj != null && obj instanceof JsonObject) {
            Gson gson = new Gson();
            JsonObject json = (JsonObject)obj;
            result = gson.fromJson(json, EmotConfigResult.class);
            EmotConfigManager.getInstance().saveEmojiConfigToData(json.toString());
            
            if (result.faceGroupList!=null && result.faceGroupList.size()>0) {
	            String stickersFile = StickerHelper.configFilePath(DependentUtils.getUid(), CategoryData.NV_CONFIG_FILE);
	            String stickers = DependentUtils.loadAsString(stickersFile);
	            if (TextUtils.isEmpty(stickers)) { // 本地为空，直接保存
	            	JsonArray array = json.getAsJsonArray("faceGroupList");
	            	String s = array.toString();
	            	DependentUtils.save(stickersFile, s);
	            } else { // 本地不为空，合并后保存
	            	List<FaceGroupInfo> faceGroupList = gson.fromJson(stickers, new TypeToken<List<FaceGroupInfo>>(){}.getType());
	            	List<FaceGroupInfo> downLoadFaceGroupList = result.faceGroupList;
	            	
	            	// 插入新的表情组
	            	for(int i=0; i<downLoadFaceGroupList.size(); i++) {
	            		FaceGroupInfo downLoadFaceGroupInfo = downLoadFaceGroupList.get(i);
	            		int index = downLoadFaceGroupInfo.index - 3 - 1;  // －3:减去三个系统内置的  －1:从1开始计数（不是0）
	            		if (index>=0 && index<=faceGroupList.size()) {
	            			faceGroupList.add(index, downLoadFaceGroupInfo);
	            		} else {
	            			faceGroupList.add(downLoadFaceGroupInfo);
	            		}
	            	}
	            	
	            	// 删除指定的表情组
	            	if (result.deleteFaceList!=null && result.deleteFaceList.size()>0) {
		            	if (faceGroupList!=null && faceGroupList.size()>0) {
		            		for (int i=0; i<result.deleteFaceList.size(); i++) {
		            			String deleteGroupName = result.deleteFaceList.get(i);
		            			if (!TextUtils.isEmpty(deleteGroupName)) {
			            			for (int j=0; j<faceGroupList.size(); j++) {
			            				FaceGroupInfo groupInfo = faceGroupList.get(j);
			            				if (deleteGroupName.equalsIgnoreCase(groupInfo.name)) {
			            					faceGroupList.remove(j);
			            					break;
			            				}
			            			}
		            			}
		            		}
		            	}
	            	}
	            	
	            	String s = gson.toJson(faceGroupList, new TypeToken<List<FaceGroupInfo>>(){}.getType());
	            	DependentUtils.save(stickersFile, s);
	            }
            }
        }
        if (result != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, result);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }

	@Override
	protected void onEgmTransactionError(int errCode, Object obj) {
		super.onEgmTransactionError(errCode, obj);
	}
}
