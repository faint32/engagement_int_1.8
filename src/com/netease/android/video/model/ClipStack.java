package com.netease.android.video.model;

import java.util.Iterator;

import com.netease.android.video.collections.ObservedStack;
import com.netease.android.video.model.Clip.ClipState;

public class ClipStack extends ObservedStack<Clip> {
	public Clip getLastClip() {
		return (Clip) getLast();
	}

	public int getTotalClipLength() {
		Iterator<Clip> iterator = iterator();
		int totalLength = 0;
		while (iterator.hasNext()) {
			Clip clip = iterator.next();
			if (clip.getState() != ClipState.INVALID) {
				totalLength = (int) (totalLength + Math.max(0L,
						clip.getDuration()));
			}
		}
		return totalLength;
	}
}