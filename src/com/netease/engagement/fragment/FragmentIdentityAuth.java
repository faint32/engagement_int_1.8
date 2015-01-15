package com.netease.engagement.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityVideoRec;
import com.netease.engagement.adapter.SelectAvatarHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentMoneyAccount.IAuth;
import com.netease.engagement.image.video.AsyncImageLoader;
import com.netease.engagement.image.video.ImageLoadCallBack;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ApplyWithdrawError;

/** 验证身份证 */
public class FragmentIdentityAuth extends FragmentBase 
		implements OnClickListener, OnLongClickListener, ImageLoadCallBack {
	
    private EditText mEditName;
    private EditText mEditNumber;
    
    
    /**
     * 按钮
     */
    private View mIdentityVideo;
    private View mIdentityFront;
    private View mIdentityContrary;
    
    /**
     * 预览图片
     */
    private ImageView mFrontPicture;
    private ImageView mContraryPicture;
    private ImageView mVideoPicture;
    
    private ActivityEngagementBase mActivity;
    private SelectAvatarHelper mPictureHelper;
    private boolean mIsClickFront = true;
    
    private String mFrontPath, mContraryPath, mVideoPath;
    private int mTid;
    
    private IAuth mAuthCallback;
    private InputMethodManager mManager;
    
    // 仅修改视频
    private boolean mModifyVideo;
    
    // 申请提现的出错code
    private int mErrCode;
    
    // 申请提现的出错信息
    private ApplyWithdrawError mErrInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (ActivityEngagementBase)getActivity();
        
        mPictureHelper = new SelectAvatarHelper(this, EgmConstants.SIZE_MAX_PICTURE, EgmConstants.SIZE_MIN_PICTURE, false);
        mManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        EgmService.getInstance().addListener(mEgmCallback);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (ActivityEngagementBase)getActivity();
        CustomActionBar actionBar = mActivity.getCustomActionBar();
        actionBar.setLeftVisibility(View.VISIBLE);
        actionBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (! onBackPressed()) {
            		clickBack();
            	}
            }
        });
        actionBar.setMiddleTitle(R.string.account_money_auth_fragment_title);
        actionBar.setRightVisibility(View.VISIBLE);
        actionBar.setRightAction(-1, R.string.done);
        actionBar.setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAuthIdentity();
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ScrollView view = (ScrollView) inflater.inflate(
        		R.layout.fragment_identity_auth_layout, container, false);
        
        mEditName = (EditText)view.findViewById(R.id.auth_input_name);
        mEditNumber = (EditText)view.findViewById(R.id.auth_input_number);
        
        mIdentityFront = view.findViewById(R.id.auth_front_text);
        mIdentityContrary = view.findViewById(R.id.auth_contrary_text);
        mIdentityVideo = view.findViewById(R.id.auth_video_text);
        
        mFrontPicture = (ImageView)view.findViewById(R.id.auth_front_picture_iv);
        mContraryPicture = (ImageView)view.findViewById(R.id.auth_contrary_picture_iv);
        mVideoPicture = (ImageView) view.findViewById(R.id.auth_video_picture_iv);
        
        mFrontPicture.setOnLongClickListener(this);
        mContraryPicture.setOnLongClickListener(this);
        mVideoPicture.setOnLongClickListener(this);
        
        // 选择身份证正面照片
        mIdentityFront.setOnClickListener(this);
        // 选择身份证背面照片
        mIdentityContrary.setOnClickListener(this);
        // 录制视频
        mIdentityVideo.setOnClickListener(this);
        
        fillSampleData(view);
        
        if (mModifyVideo) { // 身份证认证通过，视频不通过的情况
        	mEditName.setText(mErrInfo.name);
        	mEditNumber.setText(mErrInfo.idCardNo);
        	mFrontPicture.setTag(new ImageViewAsyncCallback(mFrontPicture, mErrInfo.idCardPic1));
        	mContraryPicture.setTag(new ImageViewAsyncCallback(mContraryPicture, mErrInfo.idCardPic2));
        	
        	mIdentityFront.setEnabled(false);
        	mIdentityContrary.setEnabled(false);
        	
        	mFrontPicture.setEnabled(false);
        	mContraryPicture.setEnabled(false);
        	
        	mEditName.setEnabled(false);
        	mEditNumber.setEnabled(false);
        	
        	showErrMessage();
        	
        	view.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					setVideoFocus(view);
				}
			}, 100);
        }
        
        return view;
    }
    
    private void setVideoFocus(ScrollView view) {
    	mIdentityVideo.requestFocus();
    	
    	int offset = getActivity().getResources().getDimensionPixelSize(
    			R.dimen.identity_auth_video_scroll);
		view.scrollBy(0, offset);
    }
    
    @Override
    public boolean onBackPressed() {
    	Activity context = getActivity();
    	
    	String content = context.getString(R.string.account_money_auth_exist);
    	
    	View.OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int which = (Integer) v.getTag();
				
				if (which == DialogInterface.BUTTON_POSITIVE) {
					clickBack();
					
					ToastUtil.showToast(mActivity, 
							R.string.account_money_auth_exist_msg);
				}
			}
		};
    	
    	AlertDialog dialog = EgmUtil.createEgmBtnDialog(context, null, 
    			content, context.getString(R.string.cancel),  
    			context.getString(R.string.confirm), listener, true);
    	dialog.show();
    	
    	return true;
    }
    
    private void showErrMessage() {
    	if (! TextUtils.isEmpty(mErrInfo.errMessage)) {
    		Activity context = getActivity();
    		
    		AlertDialog dialog = EgmUtil.createEgmBtnDialog(context, null, 
    				mErrInfo.errMessage, null, 
    				context.getString(R.string.confirm), null, true);
    		
    		dialog.show();
    	}
    }
    
    private void fillSampleData(View view) {
    	if (mErrInfo != null && (! TextUtils.isEmpty(mErrInfo.sampleDesc) 
        		|| ! TextUtils.isEmpty(mErrInfo.samplePicUrl))) {
        	TextView sampleDesc = (TextView) view.findViewById(R.id.sample_desc_text);
        	ImageView sampleImage = (ImageView) view.findViewById(R.id.sample_desc_img);
        	
        	if (TextUtils.isEmpty(mErrInfo.sampleDesc)) {
        		sampleDesc.setVisibility(View.GONE);
        	}
        	else {
        		sampleDesc.setText(mErrInfo.sampleDesc);
        	}
        	
        	if (TextUtils.isEmpty(mErrInfo.samplePicUrl)) {
        		sampleImage.setVisibility(View.GONE);
        	}
        	else {
        		sampleImage.setTag(new ImageViewAsyncCallback(sampleImage, 
        				mErrInfo.samplePicUrl));
        	}
        }
    }
    
    @Override
    public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.auth_front_text:
    		hideIme();
             
            mIsClickFront = true;
            mPictureHelper.init();
            mPictureHelper.setDialogTitle(mActivity.getString(R.string.account_money_auth_upload_title));
            mPictureHelper.changeAvatar();
    		break;
    		
    	case R.id.auth_contrary_text:
    		hideIme();
            
            mIsClickFront = false;
            mPictureHelper.init();
            mPictureHelper.setDialogTitle(mActivity.getString(R.string.account_money_auth_upload_title));
            mPictureHelper.changeAvatar();
    		break;
    		
    	case R.id.auth_video_text:
    		hideIme();
    		
    		ActivityVideoRec.startActivityForResult(this, 10,
    				EgmConstants.REQUEST_RECORD_VIDEO);
    		break;
    	}
    }
    
    @Override
	public boolean onLongClick(View v) {
    	switch (v.getId()) {
    	case R.id.auth_front_picture_iv:
    		return showDeleteDialog(mFrontPath, mFrontPicture);
    		
    	case R.id.auth_contrary_picture_iv:
    		return showDeleteDialog(mContraryPath, mContraryPicture);
    		
    	case R.id.auth_video_picture_iv:
    		return showDeleteDialog(mVideoPath, mVideoPicture);
    	}
    	
		return false;
	}
    
    private boolean showDeleteDialog(final String path, final ImageView imageView) {
    	if (TextUtils.isEmpty(path)) {
    		return false;
    	}
    	
    	Activity activity = getActivity();
    	
    	AlertDialog dialog = EgmUtil.createEgmMenuDialog(activity,
    			activity.getString(R.string.operation), 
    			new String[] { activity.getString(R.string.delete) },
    			
    			new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (path == mFrontPath) {
							mFrontPicture.setImageBitmap(null);
							mFrontPath = null;
						}
						else if (path == mContraryPath) {
							mContraryPicture.setImageBitmap(null);
							mContraryPath = null;
						}
						else if (path == mVideoPath) {
							mVideoPicture.setImageBitmap(null);
							mVideoPath = null;
						}
					}
				}, true);
    	
    	dialog.show();
    	
    	return true;
    }
    
    public void setErrorInfo(int code, ApplyWithdrawError info) {
    	mErrCode = code;
    	mErrInfo = info;
    	
    	if (code == EgmServiceCode.TRANSACTION_WITHDRAW_IDENTITY_VIDEO_ERROR
    			&& info != null
    			&& ! TextUtils.isEmpty(info.name)
    			&& ! TextUtils.isEmpty(info.idCardNo)
    			&& ! TextUtils.isEmpty(info.idCardPic1)
    			&& ! TextUtils.isEmpty(info.idCardPic2)) {
    		mModifyVideo = true;
    	}
    	else {
    		mModifyVideo = false;
    	}
    }
    
    public void setAuthCallback(IAuth callback){
        mAuthCallback = callback;
    }
    
    /** 隐藏软键盘 */
    private void hideIme(){
        if(mManager != null && mActivity.getCurrentFocus() != null 
        		&& mActivity.getCurrentFocus().getWindowToken() != null) {  
            mManager.hideSoftInputFromWindow(
            		mActivity.getCurrentFocus().getWindowToken(), 
            		InputMethodManager.HIDE_NOT_ALWAYS);
        }  
    }
    
    @Override
	public void onImageLoaded(Bitmap bitmap) {
    	mVideoPicture.setImageBitmap(bitmap);
	}
    
    /** 上传身份证信息 */
    private void doAuthIdentity(){
        String name = mEditName.getText().toString();
        String number = mEditNumber.getText().toString();
        
        if(!TextUtils.isEmpty(name) && 
                !TextUtils.isEmpty(number) && 
                !TextUtils.isEmpty(mFrontPath) && 
                !TextUtils.isEmpty(mContraryPath) &&
                !TextUtils.isEmpty(mVideoPath)){
            
            showWatting(mActivity.getString(R.string.common_tip_is_updating));
            mTid = EgmService.getInstance().doAuthIdentity(name, number, 
            		mFrontPath, mContraryPath, mVideoPath);
        }
        else if (mErrCode == EgmServiceCode.TRANSACTION_WITHDRAW_IDENTITY_VIDEO_ERROR
        		&& !TextUtils.isEmpty(mVideoPath)) {
        	showWatting(mActivity.getString(R.string.common_tip_is_updating));
            mTid = EgmService.getInstance().doAuthIdentity(null, null, 
            		null, null, mVideoPath);
        }
        else{
            showToast(R.string.account_money_auth_info_no_full);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == EgmConstants.REQUEST_RECORD_VIDEO) {
    		if(resultCode != Activity.RESULT_OK){
        		return ;
        	}
    		
    		if(data == null){
				return ;
			}
			
    		String filePath = data.getStringExtra(EgmConstants.EXTRA_PATH);
			int duration = data.getIntExtra(EgmConstants.EXTRA_DURATION, 0);
			
			if(!TextUtils.isEmpty(filePath) && duration > 0) {
				AsyncImageLoader.getInstance().loadBitmap(filePath, "0", this);
				
				mVideoPath = filePath;
			}
    	}
    	else {
    		mPictureHelper.onActivityResult(requestCode, resultCode, data);
            
            if(mPictureHelper.mCropAvatar != null && !TextUtils.isEmpty(mPictureHelper.mAvatarPath)){
                if(mIsClickFront){
                    String pathFront = EgmUtil.getCacheDir() + "identity_front.jpg";
                    if(EgmUtil.copyFile(mPictureHelper.mAvatarPath, pathFront)){
                        mFrontPath = pathFront;
                    }
                    
                    mFrontPicture.setImageBitmap(mPictureHelper.mCropAvatar);
                }
                else{
                    String pathContrary = EgmUtil.getCacheDir() + "identity_contrary.jpg";
                    if(EgmUtil.copyFile(mPictureHelper.mAvatarPath, pathContrary)){
                        mContraryPath = pathContrary;
                    }
                    
                    mContraryPicture.setImageBitmap(mPictureHelper.mCropAvatar);;
                }
            }
    	}
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        /** 验证身份证 */
        @Override
        public void onAuthIdentity(int transactionId){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            
            if(mAuthCallback != null){
                mAuthCallback.onAuthSuccess();
            }
            
            clickBack();
        }
        @Override
        public void onAuthIdentityError(int transactionId, int errCode, String err){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            switch(errCode){
                case EgmServiceCode.TRANSACTION_WITHDRAW_PIC_DIMENS_ERROR:  // 身份证照片尺寸不符合要求
                case EgmServiceCode.TRANSACTION_WITHDRAW_PIC_SIZE_ERROR:    // 身份证照片大小不符合要求
                case EgmServiceCode.TRANSACTION_WITHDRAW_IDENTITY_NUM_ERROR:// 身份证号码错误
                case EgmServiceCode.TRANSACTION_WITHDRAW_IDENTITY_NAME_ERROR:   // 真实姓名错误
                default:
                    showToast(err);
            }
        }
    };


}
