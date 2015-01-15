package com.netease.common.share;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.netease.common.share.db.ShareBindTable;

public class ShareBind implements Comparable<ShareBind>, Parcelable {
	
	public static final int CURSOR_TYPE_DEFAULT = 0x00;
	
	public static final int JSON_TYPE_DEFAULT = 0x00;
	
	private ShareType mShareType;
	private String mAccessToken;
	private String mRefreshToken;
	private long mExpires;
	private long mBindTime;
	private String mName;
	private String mUserID;
	private String mProfile;
	private String mDomainUrl;
	private int mState;
	private String mJson;
	/*内存值，给UI上层使用*/
	private boolean mIsBind = true;
	
	public static String[] Projection = new String[]{
		BaseColumns._ID,
		ShareBindTable.C_SHARE_TYPE,
		ShareBindTable.C_ACCESS_TOKEN,
		ShareBindTable.C_REFRESH_TOKEN,
		ShareBindTable.C_EXPIRES,
		ShareBindTable.C_BIND_TIME,
		ShareBindTable.C_NAME,
		ShareBindTable.C_USERID,
		ShareBindTable.C_PROFILE,
		ShareBindTable.C_DOMAIN,
		ShareBindTable.C_STATE,
		ShareBindTable.C_JSON,
	};
	
	private static final int C_ID = 0; // long 
	private static final int C_SHARE_TYPE = 1; // int
	private static final int C_ACCESS_TOKEN = 2; // string
	private static final int C_REFRESH_TOKEN = 3; // string
	private static final int C_EXPIRES = 4; // long
	private static final int C_BIND_TIME = 5; // int
	private static final int C_NAME = 6; // string
	private static final int C_USERID = 7; // string
	private static final int C_PROFILE = 8; // string
	private static final int C_DOMAIN = 9; // string
	private static final int C_STATE = 10; // int
	private static final int C_JSON = 11; // json string
	
	public Object[] getCursorRow() {
		return new Object[] {
				Integer.valueOf(0),
				Integer.valueOf(mShareType.value()),
				mAccessToken,
				mRefreshToken,
				mExpires,
				mBindTime,
				mName,
				mUserID,
				mProfile,
				mDomainUrl,
				Integer.valueOf(mState),
				mJson,
		};
	}
	
	public ShareBind(ShareType shareType) {
		mBindTime = System.currentTimeMillis();
		
		mShareType = shareType;
	}
	
	public ShareBind(ShareType shareType, boolean isBind) {
		mBindTime = System.currentTimeMillis();
		
		mShareType = shareType;
		mIsBind = isBind;
		
		if (! mIsBind) {
			mState = -1;
		}
	}
	
	public ShareBind(ShareType shareType, String userId, String accessToken) {
		mBindTime = System.currentTimeMillis();
		
		mShareType = shareType;
		mUserID = userId;
		mAccessToken = accessToken;
	}
	
	public ShareBind(JSONObject json, int jsonType) {
		mShareType = ShareType.valueOf(json.optInt(ShareBindTable.C_SHARE_TYPE));
		mAccessToken = json.optString(ShareBindTable.C_ACCESS_TOKEN);
		mRefreshToken = json.optString(ShareBindTable.C_REFRESH_TOKEN);
		mExpires = json.optLong(ShareBindTable.C_EXPIRES);
		mBindTime = json.optLong(ShareBindTable.C_BIND_TIME);
		mName = json.optString(ShareBindTable.C_NAME);
		mUserID = json.optString(ShareBindTable.C_USERID);
		mProfile = json.optString(ShareBindTable.C_PROFILE);
		mDomainUrl = json.optString(ShareBindTable.C_DOMAIN);
		mState = json.optInt(ShareBindTable.C_STATE);
		mJson = json.optString(ShareBindTable.C_JSON);
	}
	
	public String getFullJson() {
		JSONObject json = new JSONObject();
		try {
			json.put(ShareBindTable.C_SHARE_TYPE, mShareType.value());
			json.put(ShareBindTable.C_ACCESS_TOKEN, mAccessToken);
			json.put(ShareBindTable.C_REFRESH_TOKEN, mRefreshToken);
			json.put(ShareBindTable.C_EXPIRES, mExpires);
			json.put(ShareBindTable.C_BIND_TIME, mBindTime);
			json.put(ShareBindTable.C_NAME, mName);
			json.put(ShareBindTable.C_USERID, mUserID);
			json.put(ShareBindTable.C_PROFILE, mProfile);
			json.put(ShareBindTable.C_DOMAIN, mDomainUrl);
			json.put(ShareBindTable.C_STATE, mState);
			json.put(ShareBindTable.C_JSON, mJson);
		} catch (JSONException e) {
		}
		
		return json.toString();
	}
	
	public ShareBind(Cursor cursor, int cursorType) {
		mShareType = ShareType.valueOf(cursor.getInt(C_SHARE_TYPE));
		mAccessToken = cursor.getString(C_ACCESS_TOKEN);
		mRefreshToken = cursor.getString(C_REFRESH_TOKEN);
		mExpires = cursor.getLong(C_EXPIRES);
		mBindTime = cursor.getLong(C_BIND_TIME);
		mName = cursor.getString(C_NAME);
		mUserID = cursor.getString(C_USERID);
		mProfile = cursor.getString(C_PROFILE);
		mDomainUrl = cursor.getString(C_DOMAIN);
		mState = cursor.getInt(C_STATE);
		mJson = cursor.getString(C_JSON);
	}
	
	@Override
	public int compareTo(ShareBind another) {
		int value = mShareType.value();
		int avalue = another.mShareType.value();
		
		int ret = 0;
		if (value > avalue) {
			ret = 1;
		} else if (value < avalue) {
			ret = -1;
		}
		
		return ret;
	}
	
	public boolean isValid() {
		return mState >= 0;
	}
	
	public boolean isInvalid() {
		return mState < 0;
	}
	
	public void setInvalid() {
		mState = -1;
	}
	
	public String getAccessToken() {
		return mAccessToken;
	}

	public void setAccessToken(String accessToken) {
		this.mAccessToken = accessToken;
	}

	public String getRefreshToken() {
		return mRefreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.mRefreshToken = refreshToken;
	}
	
	public void setExpires(long expires) {
		mExpires = expires;
	}
	
	public long getExpires() {
		return mExpires;
	}
	
	public long getBindTime() {
		return mBindTime;
	}
	
	public void setBindTime(long time) {
		mBindTime = time;
	}
	
	public int getState() {
		return mState;
	}
	
	public void setState(int value) {
		mState = value;
	}
	
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}
	
	public void setUserID(String userId) {
		mUserID = userId;
	}
	
	public String getUserID() {
		return mUserID;
	}

	public String getProfile() {
		return mProfile;
	}

	public void setProfile(String profile) {
		this.mProfile = profile;
	}

	public String getDomainUrl() {
		return mDomainUrl;
	}

	public void setDomainUrl(String domainUrl) {
		this.mDomainUrl = domainUrl;
	}

	public ShareType getShareType() {
		return mShareType;
	}
	
	public void setIsBind(boolean isBind) {
		mIsBind = isBind;
	}
	
	public boolean isBind() {
		return mIsBind;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public ShareBind(Parcel source) {
		mShareType = ShareType.valueOf(source.readInt());
		mAccessToken = source.readString();
		mRefreshToken = source.readString();
		mExpires = source.readLong();
		mBindTime = source.readLong();
		mName = source.readString();
		mUserID = source.readString();
		mProfile = source.readString();
		mDomainUrl = source.readString();
		mState = source.readInt();
		mJson = source.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mShareType.value());
		dest.writeString(mAccessToken);
		dest.writeString(mRefreshToken);
		dest.writeLong(mExpires);
		dest.writeLong(mBindTime);
		dest.writeString(mName);
		dest.writeString(mUserID);
		dest.writeString(mProfile);
		dest.writeString(mDomainUrl);
		dest.writeInt(mState);
		dest.writeString(mJson);
	}

	public static final Parcelable.Creator<ShareBind> CREATOR = new Parcelable.Creator<ShareBind>() {

		@Override
		public ShareBind createFromParcel(Parcel source) {
			return new ShareBind(source);
		}

		@Override
		public ShareBind[] newArray(int size) {
			return new ShareBind[size];
		}

	};
}
