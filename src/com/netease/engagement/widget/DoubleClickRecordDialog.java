package com.netease.engagement.widget;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.dataMgr.MsgDataManager;
import com.netease.engagement.fragment.FragmentPrivateSession;
import com.netease.engagement.widget.RecordingViewDoubleClick.OnRecordListener;
import com.netease.service.media.AudioRecorderWrapper;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.protocol.EgmProtocolConstants;

/**
 * @author lishang
 */
public class DoubleClickRecordDialog extends AlertDialog {

	private LinearLayout root;
	private RecordingViewDoubleClick sendView;
	private TextView recordDuration;
	private TextView cancle;

	private FragmentPrivateSession fragmentContext;
	protected String mFilePath;
	private AudioRecorderWrapper mAudioRecorderWrapper;
	protected long mStartTime;

	private long duration;
	Context context;

	public DoubleClickRecordDialog(Context context, int theme) {
		super(context, R.style.CustomDialogTranspatent);
		this.context = context;
	}

	public DoubleClickRecordDialog(Context context) {
		super(context, R.style.CustomDialogTranspatent);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_dialog_double_click_voice);
		init();
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		getWindow().setAttributes(params);
	}

	private void init() {

		startRecord();
		cancle = (TextView) findViewById(R.id.record_cancle);
		cancle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopRecord();
				dismiss();
			}
		});

		recordDuration = (TextView) findViewById(R.id.recording_duration);
		sendView = (RecordingViewDoubleClick) findViewById(R.id.record_send);
		sendView.setOnSendClickListener(new OnRecordListener() {

			@Override
			public void onRecordStart() {
				// startRecord();
			}

            @Override
            public void onRecordSend() {
                mAudioRecorderWrapper.stop();
                if (fragmentContext != null && fragmentContext.getActivity() != null
                        && fragmentContext.isAdded()) {
                    fragmentContext.sendAudio(mFilePath, String.valueOf(duration / 1000));
                }
                dismiss();
            }

			@Override
            public void onRecordOverFlow() {
                mAudioRecorderWrapper.stop();
                if (fragmentContext != null && fragmentContext.getActivity() != null
                        && fragmentContext.isAdded()) {
                    fragmentContext.sendAudio(mFilePath, String.valueOf(60));
                }
                dismiss();
            }

			@Override
			public void onRecordEnd() {
				dismiss();
			}

			@Override
			public void onRecording(long milSec) {
				duration = milSec;
				recordDuration.setText(formatTimeCreator(milSec));
			}
		});
	}

	public void setFragmentContext(FragmentPrivateSession fragment) {
		this.fragmentContext = fragment;

	}

	private String formatTimeCreator(long duration) {
		long second = duration / 1000;

		if (second < 60) {
			return String.format("00:%02d", second);
		} else {
			return new String("01:00");
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		stopRecord();
		String filePath = mAudioRecorderWrapper.getFilePath();
		if (!TextUtils.isEmpty(filePath)) {
			File audioFile = new File(filePath);
			if (audioFile.exists()) {
				audioFile.delete();
			}
		}
	}

	public void stopRecord() {
		mAudioRecorderWrapper.stop();
		sendView.stopRecord();
		dismiss();
	}

	/**
	 * 进来就录音，省的等待，浪费时间 也避免录音不完整
	 */
	public void startRecord() {

		mAudioRecorderWrapper = AudioRecorderWrapper.getInstance();
		mStartTime = System.currentTimeMillis();
		mFilePath = MsgDataManager.getInstance().convertPath(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO,
				String.valueOf(System.currentTimeMillis()));
		mAudioRecorderWrapper.setFilePath(mFilePath);
		MediaPlayerWrapper.getInstance().stop();// 停止播放
		mAudioRecorderWrapper.start();// 开始录音
	}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        
        if (!hasFocus) {
            stopRecord();
            dismiss();
            String filePath = mAudioRecorderWrapper.getFilePath();
            if (!TextUtils.isEmpty(filePath)) {
                File audioFile = new File(filePath);
                if (audioFile.exists()) {
                    audioFile.delete();
                }
            }
        }
    }
}
