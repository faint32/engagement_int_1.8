package com.netease.engagement.dataMgr;

import com.google.gson.Gson;

public class MsgAttach {
	
	public String smallImagePath ;
	public String audioPath ;
	public String videoPath ;
	public int matchType;//命中关键字类型
	public String tips;//命中关键字提示语 
	public String mediaResUrl; // 阅后即焚多媒体资源

	public static String toJsonString(MsgAttach attach){
		String result = null ;
		if(attach != null){
			Gson gson = new Gson();
			result = gson.toJson(attach);
		}
		return result ;
	}
	
	public static MsgAttach toMsgAttach(String json){
		MsgAttach attach = null ;
		Gson gson = new Gson();
		attach = gson.fromJson(json,MsgAttach.class);
		return attach ;
	}
	
	public String getSmallImagePath() {
		return smallImagePath;
	}

	public void setSmallImagePath(String smallImagePath) {
		this.smallImagePath = smallImagePath;
	}

	public String getAudioPath() {
		return audioPath;
	}

	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public int getMatchType() {
		return matchType;
	}

	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}
	
	public String getMediaResUrl() {
		return mediaResUrl;
	}

	public void setMediaResUrl(String mediaResUrl) {
		this.mediaResUrl = mediaResUrl;
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}
}
