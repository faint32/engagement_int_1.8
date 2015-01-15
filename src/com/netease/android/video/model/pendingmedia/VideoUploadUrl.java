package com.netease.android.video.model.pendingmedia;

import java.util.Date;

public class VideoUploadUrl {
	private final Date mExpires;
	private final String mJob;
	private final String mUrl;

	public VideoUploadUrl(String paramString1, String paramString2,
			Date paramDate) {
		this.mUrl = paramString1;
		this.mJob = paramString2;
		this.mExpires = paramDate;
	}

	public Date getExpires() {
		return this.mExpires;
	}

	public String getJob() {
		return this.mJob;
	}

	public String getUrl() {
		return this.mUrl;
	}
}