package com.netease.common.share;

public class ShareBindFriend {

	public static final int GENDER_UNKNOWN = 0x0;
	public static final int GENDER_FEMALE = 0x1;
	public static final int GENDER_MALE = 0x2;
	
	private String mUserId; // 用户id
	private String mName; // 显示名
	private String mPinyin; // 拼音
	private String mProfile; // 头像
	private int mGender; // 性别
	private int mVip; // 不为0表示 vip
	private String mDesp; // 简介
	private String mAtName; //腾讯微博@
	
	/**
	 * 用户id
	 * @return
	 */
	public String getUserId() {
		return mUserId;
	}
	
	public void setUserId(String userId) {
		this.mUserId = userId;
	}
	
	/**
	 * 显示名
	 * @return
	 */
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		this.mName = name;
	}
	
	/**
	 * 拼音
	 * @return
	 */
	public String getPinyin() {
		return mPinyin;
	}

	public void setPinyin(String pinyin) {
		this.mPinyin = pinyin;
	}

	/**
	 * 头像
	 * @return
	 */
	public String getProfile() {
		return mProfile;
	}
	
	public void setProfile(String profile) {
		this.mProfile = profile;
	}
	
	/**
	 * 性别
	 * GENDER_UNKNOWN 0 未知
	 * GENDER_FEMALE 1 女
	 * GENDER_MALE 2 男
	 * 
	 * @return
	 */
	public int getGender() {
		return mGender;
	}
	
	public void setGender(int gender) {
		this.mGender = gender;
	}
	
	/**
	 * 是否vip
	 * @return
	 */
	public boolean isVip() {
		return mVip != 0;
	}

	public int getVip() {
		return mVip;
	}

	public void setVip(int vip) {
		this.mVip = vip;
	}

	/**
	 * 描述
	 * @return
	 */
	public String getDesp() {
		return mDesp;
	}

	public void setDesp(String desp) {
		this.mDesp = desp;
	}
	
	/**
     * 腾讯微博@
     * @return
     */
    public String getAtName() {
        return mAtName;
    }

    public void setAtName(String name) {
        this.mAtName = name;
    }   
	
}
