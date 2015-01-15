
package com.netease.service.protocol.meta;

/**
 * 排行榜Home级数据结构：
 */
public class AudioVideoSelfMode {

    public int introduceType; // 目前用户选择的自我介绍展示类型：0 还未录制 1 视频 2语音
    public String videoIntroduce; // 视频自我介绍url
    public int videoDuration; // 视频自我介绍时长（单位：秒）
    public String videoCover; // 视频自我介绍封面图片url
    public String voiceIntroduce; // 录音链接(女)
    public int duration; // 录音时长（单位：秒）(女)
}
