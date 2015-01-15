package com.netease.engagement.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.widget.CustomDialog;
import com.netease.engagement.widget.RecordingView;
import com.netease.engagement.widget.RecordingView.OnRecordListener;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.AudioIntroduce;

/**
 * 个人中心录制语音自我介绍
 */
public class FragmentDialogRecAudio extends FragmentBase{
	
	private TextView mImageClose ;
	//顶部状态
	private TextView mRecordStateTip ;
	//录音组件
	private RecordingView mRecordingView ;
	//重新录制
	private TextView mRe_record ;
	private CustomDialog mCustomDialog;
	
	private int mDuration ;
	private String mFilePath ;
	
	public static FragmentDialogRecAudio newInstance(){
		FragmentDialogRecAudio newFragment = new FragmentDialogRecAudio();
		return newFragment ;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EgmService.getInstance().addListener(mCallBack);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_dialog_audio, container,false);
		initViews(root);
		return root;
	}
	
	private void initViews(View root){
		mImageClose = (TextView)root.findViewById(R.id.close);
		mImageClose.setOnClickListener(mOnClickListener);
		
		mRecordStateTip = (TextView)root.findViewById(R.id.record_state_tip);
		registerForContextMenu(mRecordStateTip);
		
		mRe_record = (TextView)root.findViewById(R.id.re_record);
		mRe_record.setOnClickListener(mOnClickListener);
		
		mRecordingView = (RecordingView)root.findViewById(R.id.audio_record_view);
		mRecordingView.setOnRecordListener(new OnRecordListener(){
			@Override
			public void onRecordStart() {
			}

			@Override
			public void onRecording(long milSec) {//正在录制语音状态
				if(milSec >= RecordingView.LEAST_DURATION){
					mRecordStateTip.setText(R.string.release_to_save);
				}
			}

			@Override
			public void onRecordEnd(boolean success ,long duration,String filePath) {
				if(TextUtils.isEmpty(filePath)){
					return ;
				}
				if(success){
					//界面变化
					mRecordStateTip.setVisibility(View.INVISIBLE);
					mRe_record.setVisibility(View.VISIBLE);
					//上传语音文件
					mDuration = (int)(duration/1000);
					mFilePath = filePath ;
					if(TextUtils.isEmpty(filePath) || getAudioBytes(filePath) == null){
						return ;
					}
					EgmService.getInstance().doUpdateAudioIntroduce(filePath,mDuration );
					//显示正在上传
					showCustomDialog(getActivity(),getString(R.string.on_uploading));
				}else{
					mRecordStateTip.setText(R.string.record_time_short);
				}
			}
		});
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onUpdateAudioSucess(int transactionId, AudioIntroduce obj) {
			if(obj == null){
				return ;
			}
			closeCustomDialog();
			ToastUtil.showToast(getActivity(),R.string.audio_saved);
		}

		@Override
		public void onUpdateAudioError(int transactionId, int errCode,String err) {
			closeCustomDialog();
			showReSendDialog();
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.re_record:
					/**
					 * 重新录制
					*/
					mRecordingView.reRecord();
					mRecordStateTip.setVisibility(View.VISIBLE);
					mRecordStateTip.setText("按住录音");
					break;
				case R.id.close:
					/**
					 * 关闭当前界面
					 */
					getActivity().setResult(Activity.RESULT_OK);
					getActivity().finish();
					break;
			}
		}
	};
	
	private AlertDialog mReSendDialog ;
	private void showReSendDialog(){
		mReSendDialog = EgmUtil.createEgmMenuDialog(
				getActivity(),
				getResources().getString(R.string.rec_yuanfen_send_err_title), 
				new CharSequence[]{getResources().getString(R.string.re_upload)}, 
				new OnClickListener(){
					@Override
					public void onClick(View v) {
						//重新上传语音文件
						EgmService.getInstance().doUpdateAudioIntroduce(mFilePath,mDuration);
						showCustomDialog(getActivity(),getString(R.string.on_uploading));
						mReSendDialog.dismiss();
					}
				});
		mReSendDialog.show();
	}
	
	private byte[] getAudioBytes(String filePath){
		File file = new File(filePath);
		if(!file.exists()){
			ToastUtil.showToast(getActivity(), "录音文件不存在");
			return null ;
		}
		FileInputStream fis = null;
		byte[] data = null ;
		try {
			fis = new FileInputStream(file);
			int length = fis.available() ;
			data = new byte[length];
			fis.read(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data ;
	}
	
	private void showCustomDialog(Context context, String text) {
        if (mCustomDialog != null) {
            mCustomDialog.dismiss();
            mCustomDialog = null;
        }
        mCustomDialog = new CustomDialog(context, text, null);
        mCustomDialog.show();
    }

    private void closeCustomDialog() {
        if (mCustomDialog != null) {
            mCustomDialog.dismiss();
            mCustomDialog = null;
        }
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
}
