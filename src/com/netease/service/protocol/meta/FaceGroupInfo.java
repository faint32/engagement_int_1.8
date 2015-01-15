package com.netease.service.protocol.meta;

import java.util.List;

/**
 * 贴图表情组对象
 */
public class FaceGroupInfo {
	public String id;//贴图表情组id
	public String name;//表情组名称
	public String version;//表情组版本
	public int index;//表情组索引，用于在表情界面排序
	public String url;//表情组图片包下载地址
	public List<FaceInfo> faceList;//表情组包含的表情对象

}
