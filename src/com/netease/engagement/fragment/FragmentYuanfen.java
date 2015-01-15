package com.netease.engagement.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityYuanfen;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.TopicDataManager;
import com.netease.engagement.widget.CustomDialog;
import com.netease.engagement.widget.RecordingView;
import com.netease.engagement.widget.RecordingView.OnRecordListener;
import com.netease.engagement.widget.SlideSwitchView;
import com.netease.engagement.widget.SlideSwitchView.Position;
import com.netease.engagement.widget.SlideSwitchView.StateChangerListener;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.YuanfenInfo;

/**
 * 碰缘分界面
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class FragmentYuanfen extends FragmentBase{
    /** 全局变量，碰缘分是否打开。碰缘分页面会改变开关状态，主页的快捷工具栏也需要用到这个状态 */
    public static boolean mIsYuanfenOpen = false;
    private final int MAX_TEXT_COUNT = 400;
    private final int ANIM_DURATION = 500;
    
    private Activity mActivity;
    private InputMethodManager mManager;
    
    private View mTitleBar;
    private TextView mTitleTv;
    private TextView mTitleLeftTv;
    
    private View mTabBar;
    
    private LinearLayout mCloseTipLl;
    private TextView mOpenTv;
    
    
    private View mContentLayout;
    private View mVoicePart;
    private View mTextPart;
    
    private TextView mTipTv;
    private RecordingView mRecordView;
    private TextView mRerecordView;
    
    private TextView mHelpMeTv;
    private TextView mTextCountTv;
    private EditText mTextEdit;
    private TextView mSaveTextTv;
    
    private SlideSwitchView mSwitchView;
    

    
    /** 等待框 */
    private CustomDialog mCustomDialog;
    
    private YuanfenInfo mYuanfenInfo = new YuanfenInfo();
    /** 录音文件路径 */
    private String mFilePath;
    /** 录音时长 */
    private int mDuration;
    /** 重新录制之前的本地录音文件地址 */
    private String mOldFilePath;
    
    /** 帮我写文字模板 */
    private List<String> mHelpMeTextList;
    /** 帮我写文字index */
    private int mHelpMeIndex = 0;
    /** 是否需要提示已开启 */
    private boolean mIsTip = false;
    private int mInformSwitchTid;
    private int mInformTypeTid;
    private int mSendTid;
    private int mGetTid;
    
    private boolean isFromComeInTip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mActivity = this.getActivity();
        EgmService.getInstance().addListener(mEgmCallBack);
        MediaPlayerWrapper.getInstance().doBindService(EngagementApp.getAppInstance());
        mManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        TopicDataManager topicManager = TopicDataManager.getInstance();
        mHelpMeTextList = topicManager.getYuanFen();
        
        isFromComeInTip = getArguments().getBoolean(ActivityYuanfen.EXTRA_IS_FROM_COME_IN_TIP);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_yuanfen_layout, container, false);
        return view;
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        
        EgmService.getInstance().removeListener(mEgmCallBack);
//        MediaPlayerWrapper.getInstance().doUnbindService(mActivity);
        
        // 如果老的录音文件还在那就把它删掉
        deleteOldAudioFile();
    }
    
    private void initView(View view){
        mTitleBar = view.findViewById(R.id.yuanfen_title_bar);
        mTitleTv = (TextView)mTitleBar.findViewById(R.id.yuanfen_title);
        mTitleBar.findViewById(R.id.yuanfen_complete).setOnClickListener(mClickComplete);
        mTitleLeftTv = (TextView) mTitleBar.findViewById(R.id.yuanfen_more);
        mTitleLeftTv.setOnClickListener(mClickMore);
        
        
        mCloseTipLl = (LinearLayout) view.findViewById(R.id.yuanfen_close_tip_view);
        mOpenTv = (TextView) view.findViewById(R.id.yuanfen_open);
        mOpenTv.setOnClickListener(mClickOpen);
        
        
        mTabBar = view.findViewById(R.id.yuanfen_tab_bar);
        mSwitchView=(SlideSwitchView)view.findViewById(R.id.slide_switch);
        mSwitchView.setOnStateChangerListener(new StateChangerListener() {
            
            @Override
            public void onStateChanged(boolean position) {
                hideIme();
                int type = mSwitchView.getPostion() ? EgmConstants.YuanfenType.Text
                        : EgmConstants.YuanfenType.Voice;
                switchTabType(type);
            }
        });
        
        
        mTipTv = (TextView)view.findViewById(R.id.yuanfen_state_tip);
        
        mContentLayout = view.findViewById(R.id.yuanfen_content);
        
        mVoicePart = mContentLayout.findViewById(R.id.yuanfen_voice_part);
        mTextPart = mContentLayout.findViewById(R.id.yuanfen_text_part);
        
        mRecordView = (RecordingView)mVoicePart.findViewById(R.id.yuanfen_record);
        mRecordView.setOnRecordListener(mRecordListener);
        
        mRerecordView = (TextView)mVoicePart.findViewById(R.id.yuanfen_re_record);
        mRerecordView.setOnClickListener(mClickRerecord);
        
        mHelpMeTv = (TextView)mTextPart.findViewById(R.id.yuanfen_help_me);
        mHelpMeTv.setOnClickListener(mClickHelpMe);
        mTextCountTv = (TextView)mTextPart.findViewById(R.id.yuanfen_text_count);
        mTextCountTv.setText(String.valueOf(MAX_TEXT_COUNT));
        mTextEdit = (EditText)mTextPart.findViewById(R.id.yuanfen_text_edit);
        mTextEdit.addTextChangedListener(mTextWatcher);
        mSaveTextTv = (TextView)mTextPart.findViewById(R.id.yuanfen_text_save);
        mSaveTextTv.setOnClickListener(mClickSaveText);
        
        
    }
    
    private void updateState(YuanfenInfo info){
        if(info != null && info.isOpen){
        	
        	mTitleLeftTv.setVisibility(View.VISIBLE);
        	mCloseTipLl.setVisibility(View.GONE);
            
            mTabBar.setVisibility(View.VISIBLE);
            mContentLayout.setVisibility(View.VISIBLE);
			mTitleTv.setVisibility(View.VISIBLE);

            boolean position = info.type == EgmConstants.YuanfenType.Voice ? Position.LEFT
                    : Position.RIGHT;
            mSwitchView.setPositionStateInstance(position);
            switchTabType(info.type);
            // 切换
            if(!TextUtils.isEmpty(info.voiceUrl)){  // 有语音
                mTipTv.setText("");
                mRecordView.setPlayInitState(info.duration * 1000, info.voiceUrl);
                mRerecordView.setVisibility(View.VISIBLE);
            }
            else{   // 没有语音，则是录制状态
                mTipTv.setText(R.string.rec_yuanfen_press_record);
                mRecordView.setRecordState();
                mRerecordView.setVisibility(View.INVISIBLE);
            }
            
            if(!TextUtils.isEmpty(info.text)){  // 有文字
                mTextEdit.setText(info.text);
            }

            // 设置标题
            if(info.type == EgmConstants.YuanfenType.Voice){
                if(!TextUtils.isEmpty(info.voiceUrl)){  // 有语音
//                    mTitleTv.setText(R.string.rec_yuanfen_voice_title2);
                }
                else{   // 没有语音
//                    mTitleTv.setText(R.string.rec_yuanfen_voice_title1);
                }
            }
            else{
                
                if(isFromComeInTip) {
                	helpMeText();
                	isFromComeInTip = false;
                }
            }
        }
        else{   
            // 从碰缘分弹层入口进入的时候， 防止闪烁
            if (isFromComeInTip) {
                mCloseTipLl.setVisibility(View.GONE);
                return;
            }
            //其他关闭状态
            mTitleLeftTv.setVisibility(View.GONE);
            mCloseTipLl.setVisibility(View.VISIBLE);

            mTitleTv.setVisibility(View.GONE);
            mTabBar.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.GONE);
        }
    }
    
    /** 切换Tab */
    private void switchTabType(int type){
        if(type == EgmConstants.YuanfenType.Voice){
            mVoicePart.setVisibility(View.VISIBLE);
            mTextPart.setVisibility(View.GONE);
        }
        else{
            mVoicePart.setVisibility(View.GONE);
            mTextPart.setVisibility(View.VISIBLE);
            mRecordView.stopPlay(); // 切换到文字，把可能存在的语音播放关掉
        }
    }
    
    private TextWatcher mTextWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            int size = s.toString().length();
            int left = MAX_TEXT_COUNT - size;
            
            mTextCountTv.setText(String.valueOf(left));
            if(left == 400){
                mTextCountTv.setTextColor(getResources().getColor(R.color.content_text));
            }
            else if(left > 0){
                mTextCountTv.setTextColor(getResources().getColor(R.color.home_yuanfen_text_count_tip1));
            }
            else{   // 0
                mTextCountTv.setTextColor(getResources().getColor(R.color.home_yuanfen_text_count_tip2));
            }
        }
    };
    
    /** 帮我写 */
    private final View.OnClickListener mClickHelpMe = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideIme();
            
            helpMeText();
        }
    };
    
    private void helpMeText() {
    	if(mHelpMeTextList != null && mHelpMeTextList.size() > 0){
            if(mHelpMeIndex >= mHelpMeTextList.size()){
                mHelpMeIndex = 0;
            }
            
            mHelpMeTv.setText(R.string.rec_yuanfen_help_me_change);
            
            String text = mHelpMeTextList.get(mHelpMeIndex);
            mTextEdit.setText(text);
            
            mHelpMeIndex++;
        }
    }
    
    
    
    private final View.OnClickListener mClickComplete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	complete();
        }
    };
    
    private final View.OnClickListener mClickMore = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	showMoreDialog();
        }
    };
    
    private AlertDialog mSendPicDialog ;
	private void showMoreDialog(){
		if(mSendPicDialog == null){
			mSendPicDialog = EgmUtil.createEgmMenuDialog(FragmentYuanfen.this.mActivity, 
					FragmentYuanfen.this.mActivity.getString(R.string.more), 
					FragmentYuanfen.this.mActivity.getResources().getStringArray(R.array.rec_yuanfen_more_array), 
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int which = (Integer)view.getTag();
                            switch (which) {
	                            case 0: 
	                            	doInformSwitcher(false);    // 关闭
	                                break;
                            }
                            if (mSendPicDialog != null) {
	                            if(mSendPicDialog.isShowing()){
	                            	mSendPicDialog.dismiss();
	                            }
	                            mSendPicDialog = null;
                            }
                        }
                    });
		}
		mSendPicDialog.show();
	}
    
	private final View.OnClickListener mClickOpen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	doInformSwitcher(true);    // 打开
        }
    };
    
    /** 重新录制 */
    private OnClickListener mClickRerecord = new OnClickListener(){
        @Override
        public void onClick(View v) {
            // 先别把本地文件删掉，等到上传成功了再删，否则切换状态后，服务器没有给录音url，就无法播放了
            mRecordView.reRecordRetainFile(); 
            mOldFilePath = mFilePath;
            mTipTv.setVisibility(View.VISIBLE);
            mTipTv.setText(R.string.rec_yuanfen_press_record);
            mRerecordView.setVisibility(View.INVISIBLE);
        }
    };
    
    private View.OnClickListener mClickSaveText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String content = mTextEdit.getText().toString().trim();
            
            if(TextUtils.isEmpty(content)){
                showToast(R.string.rec_yuanfen_tip_text_empty);
            }
            else{
                doSendYuanfen(EgmConstants.YuanfenType.Text, content, 0);
            }
        }
    };
    
    private final OnRecordListener mRecordListener = new OnRecordListener(){
        @Override
        public void onRecordStart() {
            mTipTv.setText("正在录音");
        }

        @Override
        public void onRecording(long milSec) {//正在录制语音状态
            if(milSec >= RecordingView.LEAST_DURATION){
                mTipTv.setVisibility(View.VISIBLE);
                mTipTv.setText(R.string.release_to_save);
            }
        }

        @Override
        public void onRecordEnd(boolean suc, long duration, String filePath) {
            if(TextUtils.isEmpty(filePath)){
                return ;
            }
            
            if(suc){
                //界面变化
                mTipTv.setVisibility(View.INVISIBLE);
                mRerecordView.setVisibility(View.VISIBLE);
                
                if(getAudioBytes(filePath) == null){
                    return;
                }
                
                mFilePath = filePath;
                mDuration = (int)(duration/1000);
                doSendYuanfen(EgmConstants.YuanfenType.Voice, filePath, mDuration);
            }
            else{
                mTipTv.setText(R.string.record_time_short);
            }
        }
    };
    
    private void doGetYuanfen(){
        showWatting(null, getString(R.string.common_tip_is_waitting), false);
        mGetTid = EgmService.getInstance().doGetYuanfenInfo();
    }
    
    private void doInformSwitcher(boolean isOpen){
        showWatting(getString(R.string.common_tip_is_waitting));
        mInformSwitchTid = EgmService.getInstance().doInformYuanfenSwitcher(isOpen);
    }
    
    private void doInformType(int type){
        showWatting(null, getString(R.string.common_tip_is_waitting), false);
        mInformTypeTid = EgmService.getInstance().doInformYuanfenType(type);
    }
    
    /**
     * 上传碰缘分数据
     * @param type
     * @param content 语音的话是语音的文件地址；文字的话是文字内容
     * @param duration 语音的时长
     */
    private void doSendYuanfen(int type, String content, int duration){
        showCustomDialog(mActivity,getString(R.string.on_uploading));   //显示正在上传
        
        if(type == EgmConstants.YuanfenType.Voice){
            mSendTid = EgmService.getInstance().doSendYuanfen(type, content, null, duration);
        }
        else{
            mSendTid = EgmService.getInstance().doSendYuanfen(type, null, content, duration);
        }
    }
    
    private byte[] getAudioBytes(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            ToastUtil.showToast(mActivity, "录音文件不存在");
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
    
    /** 删除老的录音文件 */
    private void deleteOldAudioFile(){
        if(!TextUtils.isEmpty(mOldFilePath)){
            File audioFile = new File(mOldFilePath);
            if(audioFile.exists()){
                audioFile.delete();
            }
        }
    }
    
    /** 显示正在上传等待框 */
    private void showCustomDialog(Context context, String text) {
        if (mCustomDialog != null) {
            mCustomDialog.dismiss();
            mCustomDialog = null;
        }
        mCustomDialog = new CustomDialog(context, text, null);
        mCustomDialog.setCancelable(false);
        mCustomDialog.show();
    }
    
    /** 关闭正在上传等待框 */
    private void closeCustomDialog() {
        if (mCustomDialog != null) {
            mCustomDialog.dismiss();
            mCustomDialog = null;
        }
    }
    
    private AlertDialog mDialog;
    private void showResendVoice(){
        if(mDialog == null){
            mDialog = EgmUtil.createEgmMenuDialog(mActivity, 
                    getString(R.string.rec_yuanfen_send_err_title), 
                    new CharSequence[]{getString(R.string.rec_yuanfen_resend)}, 
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                            doSendYuanfen(EgmConstants.YuanfenType.Voice, mFilePath, mDuration);
                        }
                    });
        }
        
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }
    
    /** 隐藏软键盘 */
    private void hideIme(){
        if(mManager != null && mTextEdit.getWindowToken() != null){  
            mManager.hideSoftInputFromWindow(mTextEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }  
    }
    
    public void complete() {
        boolean isOpen = false;
        if (mYuanfenInfo.isOpen) {
            if (mSwitchView.getPostion() == Position.LEFT) {
                if (mYuanfenInfo.duration > 0 && !TextUtils.isEmpty(mYuanfenInfo.voiceUrl)) {
                    isOpen = true;
                    doInformType(EgmConstants.YuanfenType.Voice);
                } else {
                    showToast("语音" + getString(R.string.rec_yuanfen_tip_not_open));
                }
            } else if (mSwitchView.getPostion() == Position.RIGHT) {
                if (!TextUtils.isEmpty(mYuanfenInfo.text)) {
                    isOpen = true;
                    doInformType(EgmConstants.YuanfenType.Text);
                } else {
                    showToast("文字" + getString(R.string.rec_yuanfen_tip_not_open));
                }
            }
        }
        
        mIsYuanfenOpen = isOpen;
        if (mIsTip && isOpen) { // 开关打开了
            showToast(R.string.rec_yuanfen_tip_open);
        }

        mRecordView.stopPlay(); // 不管是否在语音界面，都把可能存在的播放给关掉

        clickBack();
    }
    
    private EgmCallBack mEgmCallBack = new EgmCallBack(){
        /** 获取碰缘分 */
        @Override
        public void onGetYuanfenInfo(int transactionId, YuanfenInfo info){
            if(mGetTid != transactionId)
                return;
            
            stopWaiting();
            
            mYuanfenInfo = info;
            
            //如果碰缘分没开启，并且是从引导层进入碰缘分，则主动帮用户开启
            if (!mYuanfenInfo.isOpen && isFromComeInTip) {
                doInformSwitcher(true);
            }
            updateState(info);
        }
        @Override
        public void onGetYuanfenInfoError(int transactionId, int errCode, String err){
            if(mGetTid != transactionId)
                return;
            
            stopWaiting();
            updateState(null);
            showToast(err);
        }
        
        /** 通知服务器碰缘分开关状态 */
        @Override
        public void onInformYuanfenSwitcher(int transactionId, Boolean isOpen){
            if(mInformSwitchTid != transactionId)
                return;
            
            stopWaiting();
            
            mYuanfenInfo.isOpen = isOpen;
            mIsTip = true;
            
            updateState(mYuanfenInfo);
        }
        @Override
        public void onInformYuanfenSwitcherError(int transactionId, int errCode, String err){
            if(mInformSwitchTid != transactionId)
                return;
            
            stopWaiting();
            showToast(err);
        }
        
        /** 通知服务器碰缘分类型 */
        @Override
        public void onInformYuanfenType(int transactionId, Integer type){
            if(mInformTypeTid != transactionId)
                return;
            
            stopWaiting();
            mIsTip = true;
            mYuanfenInfo.type = type;
            updateState(mYuanfenInfo);
        }
        @Override
        public void onInformYuanfenTypeError(int transactionId, int errCode, String err){
            if(mInformTypeTid != transactionId)
                return;
            
            stopWaiting();
            showToast(err);
        }
        
        /** 上传碰缘分数据 */
        @Override
        public void onSendYuanfen(int transactionId){
            if(mSendTid != transactionId)
                return;
            
            //上传成功，关闭dialog
            closeCustomDialog();
            showToast(R.string.rec_yuanfen_tip_save);
            mIsTip = true;
            
            // 更新存在内存里的mYuanfenInfo
            if(mYuanfenInfo.type == EgmConstants.YuanfenType.Voice){
                mYuanfenInfo.voiceUrl = mFilePath;
                mYuanfenInfo.duration = mDuration;
                
                // 新的录音上传成功了，再把老的录音文件删掉
                deleteOldAudioFile();
            }
            else{
                mYuanfenInfo.text = mTextEdit.getText().toString();
            }
        }
        @Override
        public void onSendYuanfenError(int transactionId, int errCode, String err){
            if(mSendTid != transactionId)
                return;
            
            closeCustomDialog();
            showToast(err);
            
            if(mYuanfenInfo.type == EgmConstants.YuanfenType.Voice){
                showResendVoice();
            }
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        
        // view渲染成功后，再请求，万一无效呢
        doGetYuanfen();
    }
}
