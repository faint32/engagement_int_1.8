package com.netease.android.video.model.mediatype;

import android.util.SparseArray;

public enum MediaType {
	PHOTO(1), VIDEO(2);
	private static SparseArray<MediaType> sReverseMap = new SparseArray<MediaType>();
	private final int mServerValue;

	static {
		MediaType[] types = values();
		for (int i = 0; i < types.length; i++) {
			MediaType type = types[i];
			sReverseMap.put(type.mServerValue, type);
		}
	}

	private MediaType(int vaule) {
		this.mServerValue = vaule;
	}

	public static MediaType fromServerValue(int vaule) {
		return (MediaType) sReverseMap.get(vaule);
	}

	public int toServerValue() {
		return this.mServerValue;
	}
}