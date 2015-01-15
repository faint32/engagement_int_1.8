package com.netease.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

public class AudioUtil {

	/**
	 * 
	 * 
	 * @param context
	 * @param state
	 * @param ringerMode
	 * @return
	 */
	public static int setMuteAll(Context context, boolean state, int ringerMode) {
		AudioManager audiomanager = (AudioManager) context.getSystemService(
				Context.AUDIO_SERVICE);
		
//		int i = ringerMode;
//		audiomanager.setStreamSolo(AudioManager.STREAM_SYSTEM, state);
//		if (state) {
//			i = 0;
//			ringerMode = audiomanager.getRingerMode();
//		}
//		
//		if (i != audiomanager.getRingerMode())
//			audiomanager.setRingerMode(i);
//		audiomanager.setStreamMute(AudioManager.STREAM_SYSTEM, state);
		
		if (state) {
			audiomanager.requestAudioFocus(onAudioFocusChangeListener, 
					AudioManager.STREAM_MUSIC, 
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		}
		else {
			audiomanager.abandonAudioFocus(onAudioFocusChangeListener);
		}
		
		return ringerMode;
	}
	
	static OnAudioFocusChangeListener onAudioFocusChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				// 获得音频焦点
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				// 展示失去音频焦点，暂停播放等待重新获得音频焦点
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// 失去音频焦点，无需停止播放，降低声音即可
				break;
			}
		}
	};
	
}
