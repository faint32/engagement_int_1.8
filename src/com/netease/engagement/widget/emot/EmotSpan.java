package com.netease.engagement.widget.emot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class EmotSpan extends ImageSpan{
	
	public static final String REGULAR = "\\[(\\S+?)\\]" ;
	String mPhrase;
	int mSize;
//	int resourceId;
	Context context;

//	public EmotSpan(Context context, int resourceId,
//			int verticalAlignment, String phrase, int height) {
//		super(context, resourceId, verticalAlignment);
//		mPhrase = phrase;
//		mSize = height;
//		this.resourceId = resourceId;
//		this.context = context;
//	}
	
	public EmotSpan(Context context, Bitmap bitmap,
			int verticalAlignment, String phrase, int height) {
		super(context, bitmap, verticalAlignment);
		mPhrase = phrase;
		mSize = height;
		this.context = context;
	}

	public EmotSpan(Context context, Bitmap bitmap) {
		super(context, bitmap);
	}

	/**
	 * 设置表情图片的大小。
	 */
	@Override
	public Drawable getDrawable() {
		Drawable d = super.getDrawable();
		if (d != null && mSize != 0) {
			// 设置大小
			int w = d.getIntrinsicWidth();
			if (mSize < w) {
				d.setBounds(0, 0, mSize, mSize);
			}
		}
		return d;
	}
}
