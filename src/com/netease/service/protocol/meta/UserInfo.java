package com.netease.service.protocol.meta;

import java.util.Random;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

public class UserInfo implements Parcelable{
    
    private static final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
 
	public long uid;//用户ID
	/** 邮箱帐号 */
	public String account;
	public int sex;//性别 0:女 1:男
	public int age;//年龄
	public long birthday;//生日
	public int weight;//体重
	public int height;//身高
	public int constellation;//星座
	public int satisfiedPart;//最满意部位(女)
	public int bust;//三围-胸围(女)
	public int waist;//三围-腰围(女)
	public int hip;//三围-臀围(女)
	public int cup;//三围-罩杯(女)
	public int[] favorDate;//喜欢的约会方式
	public int[] hobby;//兴趣爱好
	public int[] skill;//女：想学技能，男：所擅长的技能
	public String socialUrl;//社交网络网址
	public int province;//省
	public int city;//市
	public int district;//区
	public int level;//当前等级
	public int nextLevel;//下个等级
	public String levelName;//等级对应封号
	public long usercp;//当前魅力值（豪气值），不会减少
	public long nextUsercp;//下一级魅力值（豪气值）
	public String nick;//用户昵称
	public boolean isVip;//是否vip
	public long vipEndTime ;//vip的结束时间
	public boolean hasPortrait;// 是否有头像（没有头像或未通过审核）
	public String portraitUrl;//用户头像原图url
	public String portraitUrl192;//用户192x192头像裁剪图url
	public String portraitUrl640;//用户640x480头像裁剪图url
	public String voiceIntroduce;//录音链接(女)
	public int duration;//录音时长（单位：秒）(女)
	public long visitTimes;//访问人气(女)
	public long praise;//照片赞数(女)
	public int photoCount;//公开照片数
	public int privatePhotoCount;//私照数(女)
	public int giftCount;//礼物数（男性送出，女性收到）
	public int  adorerCount;//爱慕者数(女性才有)
	public String introduce;//自我介绍
	public double balance;//当前现金值(女)
	public double accumulateMoney;//累计现金值(女)
	public long expireTime;//现金值过期时间(女)
	public long coinBalance;//金币余额(男)
	public int income;//收入
	public long regTime;//注册日期
	public int status;//用户状态：0 正常 1冻结
	public int crownId;//皇冠图片Id(就是礼物ID)
	public boolean isNew;//是否新用户
	public int portraitStatus; //用户头像状态：0 未上传 1 待审核 2审核成功 3审核失败
	public String portraitTips;//头像状态信息描述
	
	// 1.6
	public ChatSkillInfo[] chatSkills; // 聊天技
	public boolean modifyBirthday; // 是否能改生日：true能修改，false不能改
	public int introduceType; // 目前用户选择的自我介绍展示类型：0 视频 1语音
	public String videoIntroduce; // 视频自我介绍url
	public int videoDuration; // 视频自我介绍时长（单位：秒）
	public String videoCover; // 视频自我介绍封面图片url
	public boolean hasVideoAuth; // 是否通过真人认证
	
	public static String toJsonString(UserInfo user) {
        Gson gson = new Gson();
        return gson.toJson(user);
    }
	
	public UserInfo() {
        super();
        if (DEBUG) {
            Random random = new Random();
            
            uid = random.nextInt(100000000);
            //sex = random.nextInt(2);
            sex = UserInfoUtil.getGender() ;
            age = random.nextInt(60);
            birthday = System.currentTimeMillis();
            weight = random.nextInt(200)%(200-30+1) + 30; 
            height = random.nextInt(250)%(250-120+1) + 120;
            constellation = random.nextInt(12);
            satisfiedPart = random.nextInt(10);
            bust = random.nextInt(100);
            waist = random.nextInt(100);
            hip = random.nextInt(100);
            cup = random.nextInt(10);
            
            int favorDateCount = random.nextInt(10);
            if (favorDateCount > 0) {
                favorDate = new int[favorDateCount];
                for (int i = 0; i < favorDateCount; i++) {
                	//此处改为不随机生成，因为要保证唯一，方便测试
                    favorDate[i] = i;
                }
            }
            
            int hobbyDateCount = random.nextInt(10);
            if (hobbyDateCount > 0) {
                hobby = new int[hobbyDateCount];
                for (int i = 0; i < hobbyDateCount; i++) {
                	//此处改为不随机生成，因为要保证唯一，方便测试
                    hobby[i] = i;
                }
            }
            
            int skillCount = random.nextInt(10);
            if (skillCount > 0) {
                skill = new int[skillCount];
                for (int i = 0; i < skillCount; i++) {
                	//此处改为不随机生成，因为要保证唯一，方便测试
                    skill[i] = i;
                }
            }
            socialUrl = DebugData.getText();
            province =  random.nextInt(51)%(21-11+1) + 11;
            city =  random.nextInt(99);
            district = random.nextInt(99);
            level = random.nextInt(10);
            nextLevel = random.nextInt(10);
            levelName = DebugData.getText();
            usercp = random.nextInt(1000000);
            nextUsercp = random.nextInt(2000000)%(2000000-100000+1) + 100000;
            nick = DebugData.getText()+"test";
            isVip = random.nextBoolean();
            vipEndTime = System.currentTimeMillis() + 100000;
            portraitUrl = DebugData.getUserImage();
            portraitUrl192 = DebugData.getUserImage();
            portraitUrl640 = DebugData.getUserImage();
            voiceIntroduce = "http://audio.x1.126.net/buck-x1-prod/audio/2014/03/05/11/14/14/8/fjoorj_1393989254492.mp3";
            duration = random.nextInt(60);
            visitTimes = random.nextInt(100000000);
            praise = random.nextInt(100000000);
            photoCount = random.nextInt(100000000);
            privatePhotoCount = random.nextInt(100000000);
            giftCount =  random.nextInt(100000000);
            adorerCount = random.nextInt(100000000);
            introduce = DebugData.getText();
            balance = random.nextDouble();
            accumulateMoney = random.nextDouble();
            expireTime = System.currentTimeMillis() + 30*24*60*60*1000;
            coinBalance = random.nextInt(100000000);
            income = random.nextInt(10); 
            regTime = random.nextInt(100000000);
            status = random.nextInt(2);
            crownId = 104;
            modifyBirthday = random.nextBoolean();
            hasVideoAuth = random.nextBoolean();
        }
    }
	
	public boolean hasVideo() {
		return introduceType == 1 && ! TextUtils.isEmpty(videoIntroduce);
	}
	
	public boolean hasAudio() {
		return introduceType == 2 && ! TextUtils.isEmpty(voiceIntroduce);
	}
	
	public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }
        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
    
    private UserInfo(Parcel in) {
        uid = in.readLong();
        sex = in.readInt();
        age = in.readInt();
        birthday = in.readLong();
        weight = in.readInt();
        height = in.readInt();
        constellation = in.readInt();
        satisfiedPart = in.readInt();
        bust = in.readInt();
        waist = in.readInt();
        hip = in.readInt();
        cup = in.readInt();
        
        favorDate = in.createIntArray();
        hobby = in.createIntArray();
        skill = in.createIntArray();
        
    	socialUrl = in.readString();
    	province = in.readInt();
    	city = in.readInt();
    	district = in.readInt();
    	level = in.readInt();
    	nextLevel = in.readInt();
    	levelName = in.readString();
    	usercp = in.readLong();
    	nextUsercp = in.readLong();
    	nick = in.readString();
    	isVip = (Boolean)in.readValue(boolean.class.getClassLoader());
    	vipEndTime = in.readLong() ;
    	portraitUrl = in.readString();
    	portraitUrl192 = in.readString();
    	portraitUrl640 = in.readString();
    	voiceIntroduce = in.readString();
    	duration = in.readInt();
    	visitTimes = in.readLong();
    	praise = in.readLong();
    	photoCount = in.readInt();
    	privatePhotoCount = in.readInt();
    	giftCount = in.readInt();
    	adorerCount = in.readInt();
    	introduce = in.readString();
    	balance = in.readLong();
    	accumulateMoney = in.readLong();
    	expireTime = in.readLong();
    	coinBalance = in.readLong();
    	income = in.readInt();
    	regTime = in.readLong();
    	status = in.readInt();
    	crownId = in.readInt();
    	isNew = (Boolean)in.readValue(boolean.class.getClassLoader());
    	portraitStatus = in.readInt();
    	modifyBirthday = (Boolean)in.readValue(boolean.class.getClassLoader());
    	hasVideoAuth = (Boolean)in.readValue(boolean.class.getClassLoader());
    	introduceType = in.readInt();
    }
    
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(uid);
		dest.writeInt(sex);
		dest.writeInt(age);
		dest.writeLong(birthday);
		dest.writeInt(weight);
		dest.writeInt(height);
		dest.writeInt(constellation);
		dest.writeInt(satisfiedPart);
		dest.writeInt(bust);
		dest.writeInt(waist);
		dest.writeInt(hip);
		dest.writeInt(cup);
		dest.writeIntArray(favorDate);
		dest.writeIntArray(hobby);
		dest.writeIntArray(skill);
		dest.writeString(socialUrl);
		dest.writeInt(province);
		dest.writeInt(city);
		dest.writeInt(district);
		dest.writeInt(level);
		dest.writeInt(nextLevel);
		dest.writeString(levelName);
		dest.writeLong(usercp);
		dest.writeLong(nextUsercp);
		dest.writeString(nick);
    	dest.writeValue(isVip);
    	dest.writeLong(vipEndTime);
    	dest.writeString(portraitUrl);
    	dest.writeString(portraitUrl192);
    	dest.writeString(portraitUrl640);
    	dest.writeString(voiceIntroduce);
    	dest.writeInt(duration);
    	dest.writeLong(visitTimes);
    	dest.writeLong(praise);
    	dest.writeInt(photoCount);
    	dest.writeInt(privatePhotoCount);
    	dest.writeInt(giftCount);
    	dest.writeInt(adorerCount);
    	dest.writeString(introduce);
    	dest.writeDouble(balance);
    	dest.writeDouble(accumulateMoney);
    	dest.writeLong(expireTime);
    	dest.writeLong(coinBalance);
    	dest.writeInt(income);
    	dest.writeLong(regTime);
    	dest.writeInt(status);
    	dest.writeInt(crownId);
    	dest.writeValue(isNew);
    	dest.writeInt(portraitStatus);
    	dest.writeValue(modifyBirthday);
    	dest.writeValue(hasVideoAuth);
    	dest.writeInt(introduceType);
	}
	
   public static String generateTestUserInfo(){
        Gson gson = new Gson();
        UserInfo userInfo = new UserInfo();
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = userInfo;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(DebugData.FILENAME_USERINFO_JSON, jsonString);
        return jsonString;    
    }
	   
}

