package com.netease.service.protocol.meta;

import java.util.Random;

public class AdmirerUserInfo {
	public long uid ;//用户id
	public String nick ;//昵称
	public String portraitUrl ;//用户头像原图url
	public String portraitUrl192 ;//用户192x192头像裁剪图url
	public String portraitUrl640 ;//用户640x480头像裁剪图url
	public long intimacy ;//亲密度
	
	public AdmirerUserInfo(){
		Random random = new Random();
        uid = random.nextInt(100000000);
        nick = DebugData.getText();
        portraitUrl = DebugData.getUserImage();
        portraitUrl192 = DebugData.getUserImage();
        portraitUrl640 = DebugData.getUserImage();
        intimacy = random.nextLong();
	}
}
