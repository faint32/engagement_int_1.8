package com.netease.service.protocol.meta;

import java.util.List;

public class EmotConfigResult {
	public EmoticonGroupInfo[] emoticonGroupList ;//多组表情包，每组是一个压缩包
	public String url ;//礼物图片压缩包地址
	public String version ;//礼物图片的版本
	public String faceVersion ;//当前贴图表情的版本
	public List<FaceGroupInfo> faceGroupList;//贴图表情组更新列表
	public List<String> deleteFaceList;//需要删除的表情组id列表
}
