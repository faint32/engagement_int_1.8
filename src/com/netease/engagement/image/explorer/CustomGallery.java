package com.netease.engagement.image.explorer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;
import android.widget.Toast;

import com.netease.engagement.image.explorer.adapter.GalleryPhotoUploadAdapter;

public class CustomGallery extends Gallery {
	
private Context context;
	
    boolean is_first=false;
    boolean is_last=false;

	public CustomGallery(Context context) {
		super(context);
		this.context = context;
	}

	public CustomGallery(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		this.context = context;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		GalleryPhotoUploadAdapter ia = (GalleryPhotoUploadAdapter) this.getAdapter();
		
		if(ia == null) {
			return true;
		}
		
		
		int position = ia.getOwnposition();
		
		int count = ia.getCount();
		int kEvent;
		if (e2.getX() > e1.getX()) {
			// Check if scrolling left
			if (position == 0 && is_first) {
				Toast.makeText(this.getContext(), "已到第一张", Toast.LENGTH_SHORT).show();
			} else if (position == 0) {
				is_first = true;
			} else {
				is_last = false;
			}

			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			// Otherwise scrolling right
			if (position == count - 1 && is_last) {
				Toast.makeText(this.getContext(), "已到最后一张", Toast.LENGTH_SHORT).show();
			} else if (position == count - 1) {
				is_last = true;
			} else {
				is_first = false;
			}

			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return true;
	}
}
