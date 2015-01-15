package com.netease.engagement.activity;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.adapter.SelectAvatarHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.EgmDatePicker;
import com.netease.engagement.view.EgmDatePickerDialog;
import com.netease.engagement.view.ProfileView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmLocationManager;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.OauthUserInfo;
import com.netease.service.protocol.meta.UserInfo;


/**
* 注册-设置详细信息
* <br>如果是刚注册了进到这个页面，那么在提交详细信息前，需要先到URS登录，才能提交数据。
* @version 1.0
*/
public class ActivityRegisterDetail extends ActivityEngagementBase {
    public static final String EXTRA_REGISTER_SEX_TYPE = "extra_register_sex_type";
    public static final String EXTRA_REGISTER_ACCOUNT = "extra_register_account";
    public static final String EXTRA_REGISTER_PASSWORD = "extra_register_password";
    public static final String EXTRA_REGISTER_ACCOUNT_TYPE = "extra_register_account_type";
    public static final String EXTRA_REGISTER_IS_FROM_REGISTER = "extra_register_is_from_register";
    public static final String EXTRA_CAMARA_PIC_ID = "extra_camara_pic_id";

    private Context mActivity;
//    private LoginHelper mLoginHelper;
    private SelectAvatarHelper mAvatarHelper;
    
    private TextView okTv;
    
    /** 头像 */
    private ProfileView mAvatarIv;
    /** 昵称 */
    private EditText mNickNameEt;
    /** 生日 */
    private TextView mBirthDayTv;
    /** 邀请码输入框 */
    private EditText mInviteEt;
    
    private int mFillTid;
    private int mSexType;
    private String mName;
    private String mPassword;
    private int mAccountType;
    private boolean mIsFromRegister = false;
    private int mMaxYear, mMinYear;
    
    /** 定位 */
    private EgmLocationManager mLocation;
    private int mYear, mMonth, mDay;
    
    /** 头像原图文件的路径 */
    private String mAvatarPath = null;
    /** 裁剪后的头像 */
    private Bitmap mCropAvatar = null;
    /** 头像裁剪坐标：x,y,w,h */
    private String[] mAvatarCoor = null;
    
    private int mLoginTid;
    private String mToken;
    private int mGetOauthUserTid;
    
    /** 如果是从注册入口进入该界面，需要用户名密码，因为需要在这里先登录URS。
     * 如果是从登录入口进入该界面，也需要用户名密码，因为最后要在这里保存用户名密码。 
     * @param accountType 帐号类型
     * @param isFromRegister 是否是从注册入口界面进来的
     */
    public static void startActivity(Context context, 
            int sexType, String account, String password, int accountType, boolean isFromRegister){
        
        Intent intent = new Intent(context, ActivityRegisterDetail.class);
        intent.putExtra(EXTRA_REGISTER_SEX_TYPE, sexType);
        intent.putExtra(EXTRA_REGISTER_ACCOUNT, account);
        intent.putExtra(EXTRA_REGISTER_PASSWORD, password);
        intent.putExtra(EXTRA_REGISTER_ACCOUNT_TYPE, accountType);
        intent.putExtra(EXTRA_REGISTER_IS_FROM_REGISTER, isFromRegister);
        
        context.startActivity(intent);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        this.getWindow().setBackgroundDrawableResource(R.color.app_background);
        this.getActionBar().hide();
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        initData();
        setContentView(R.layout.fragment_register_detail_layout);
        initView();
        
        EgmService.getInstance().addListener(mEgmCallback);
        if(mAccountType == EgmProtocolConstants.AccountType.YiXin){
    			mGetOauthUserTid = EgmService.getInstance().doGetOauthUserInfo();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        
        EgmService.getInstance().removeListener(mEgmCallback);
        mLocation.stop();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(EXTRA_CAMARA_PIC_ID, mAvatarHelper.mCameraImgId);
    }
 
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        mAvatarHelper.mCameraImgId = savedInstanceState.getString(EXTRA_CAMARA_PIC_ID);
    }
    
    private void initData(){
        mActivity = this;
        
        Bundle extra = getIntent().getExtras();
        mSexType = extra.getInt(EXTRA_REGISTER_SEX_TYPE);
        mName = extra.getString(EXTRA_REGISTER_ACCOUNT);
        mPassword = extra.getString(EXTRA_REGISTER_PASSWORD);
        mAccountType = extra.getInt(EXTRA_REGISTER_ACCOUNT_TYPE);;
        mIsFromRegister = extra.getBoolean(EXTRA_REGISTER_IS_FROM_REGISTER);
        
        mLocation = new EgmLocationManager(mActivity);
//        mLoginHelper = new LoginHelper(mActivity, mLoginSuccess, mLoginError, true);
        mAvatarHelper = new SelectAvatarHelper(mActivity, EgmConstants.SIZE_MAX_AVATAR, 
                mSexType == EgmConstants.SexType.Female ? EgmConstants.SIZE_MIN_AVATAR_FEMALE : EgmConstants.SIZE_MIN_AVATAR_MALE, 
                        true);
        
        Calendar rightnow = Calendar.getInstance();
        mYear = rightnow.get(Calendar.YEAR);
        /* 年龄限制在18-60岁 */
        mMaxYear = mYear - 18;
        mMinYear = mYear - 60;
        mYear = mMaxYear;
        mMonth = 1;
        mDay = 1;
    }
    
    private void setAvatar(Bitmap avatar){
        if(mAvatarIv == null)
            return;
        
        mAvatarIv.setImage(false, ProfileView.PROFILE_SIZE_LARGE, avatar);
        mAvatarIv.setBackgroundColor(mActivity.getResources().getColor(R.color.transparent));
    }
    
    private void initView(){
        TextView backTv = (TextView)findViewById(R.id.title_back);
        backTv.setOnClickListener(mClickBack);
        backTv.setBackgroundResource(R.drawable.titlebar_c_selector);
        backTv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.bar_btn_back_b), null, null, null);
        backTv.setTextColor(getResources().getColor(R.color.white));
        
        okTv = (TextView)findViewById(R.id.title_right);
        okTv.setOnClickListener(mClickFinish);
        okTv.setText(R.string.done);
        okTv.setEnabled(false);
        okTv.setBackgroundResource(R.drawable.titlebar_c_selector);
        okTv.setTextColor(getResources().getColor(R.color.titlebar_disable_black));
        
        
        TextView title = (TextView)findViewById(R.id.title_title);
        title.setText(R.string.register);
        
        mAvatarIv = (ProfileView)findViewById(R.id.register_avatar_iv);
        mNickNameEt = (EditText)findViewById(R.id.register_nikename);
        mNickNameEt.addTextChangedListener(mTextWatcher);
        
        mBirthDayTv = (TextView)findViewById(R.id.register_birthday);
        mBirthDayTv.addTextChangedListener(mTextWatcher);
        mInviteEt = (EditText)findViewById(R.id.register_invite);
        
        if(mSexType == EgmConstants.SexType.Male){ // 男性不显示邀请码输入框
            mInviteEt.setVisibility(View.GONE);
        }
        
        mAvatarIv.setOnClickListener(mClickAvatar);
        mBirthDayTv.setOnClickListener(mClickBirthday);
        
    }

    private void doUpload(){
        String nick = mNickNameEt.getText().toString();
        String birthday = mBirthDayTv.getText().toString();
        String invite = mInviteEt.getText().toString();
        
        if(checkNick(nick) && checkBirthday(birthday)){
            if(mSexType == EgmConstants.SexType.Male ||                           // 男性
                    (mSexType == EgmConstants.SexType.Female && checkAvatar())){  // 女性且头像没问题
                
                showWatting(null, getString(R.string.common_tip_is_updating), false);
                Calendar birthdate = Calendar.getInstance();
                birthdate.set(mYear, mMonth, mDay);
                
                //  取push所需的参数，在transaction自动完成保存工作，所以ui只需要发出请求，无需做处理
                EgmService.getInstance().doGetPushParams();
                
                if(mAvatarPath == null || mAvatarCoor == null){ // 男性用户可能没有头像
                    mFillTid = EgmService.getInstance().doFillUserInfo(
                            mName, mPassword, mToken, mAccountType, 
                            mSexType, nick, 
                            null, null, null, null, null, 
                            birthdate.getTimeInMillis(), 
                            invite, 
                            mLocation.mLatitude > 0 ? String.valueOf(mLocation.mLatitude) : null, 
                            mLocation.mLongtitude > 0 ? String.valueOf(mLocation.mLongtitude) : null, 
                            mLocation.mProvinceCode > 0 ? String.valueOf(mLocation.mProvinceCode) : null, 
                            mLocation.mCityCode > 0 ? String.valueOf(mLocation.mCityCode) : null, 
                            mLocation.mDistrictCode > 0 ? String.valueOf(mLocation.mDistrictCode) : null);
                }
                else{
                    mFillTid = EgmService.getInstance().doFillUserInfo(
                            mName, mPassword, mToken, mAccountType, 
                            mSexType, nick, 
                            mAvatarPath, 
                            mAvatarCoor[0], mAvatarCoor[1], 
                            mAvatarCoor[2], mAvatarCoor[3], 
                            birthdate.getTimeInMillis(), 
                            invite, 
                            mLocation.mLatitude > 0 ? String.valueOf(mLocation.mLatitude) : null, 
                            mLocation.mLongtitude > 0 ? String.valueOf(mLocation.mLongtitude) : null, 
                            mLocation.mProvinceCode > 0 ? String.valueOf(mLocation.mProvinceCode) : null, 
                            mLocation.mCityCode > 0 ? String.valueOf(mLocation.mCityCode) : null, 
                            mLocation.mDistrictCode > 0 ? String.valueOf(mLocation.mDistrictCode) : null);
                }
            }
        }
    }
    
    private boolean checkAvatar(){
        boolean isValid = false;
        if(mSexType == EgmConstants.SexType.Male){
         	isValid = true;
        }else{
	        if(mCropAvatar == null || TextUtils.isEmpty(mAvatarPath) || 
	                mAvatarCoor == null || mAvatarCoor.length < 4){
	        	//v1.0.2后，改成检查头像，昵称，生日合法后，完成按钮才可点，所以toast可以注解掉。
	//            ToastUtil.showToast(mActivity, R.string.reg_tip_avatar_empty);
	        }
	        else{
	            isValid = true;
	        }
        }
        
        return isValid;
    }
    
    private boolean checkNick(String nick){
        boolean isValid = false;
        
        if(TextUtils.isEmpty(nick)){
        	//v1.0.2后，改成检查头像，昵称，生日合法后，完成按钮才可点，所以toast可以注解掉。
//            ToastUtil.showToast(mActivity, R.string.reg_tip_nick_empty);
        }
        else{
            isValid = true;
        }
        
        return isValid;
    }
    
    private boolean checkBirthday(String birthday){
        boolean isValid = false;
        
        if(TextUtils.isEmpty(birthday)){
        	//v1.0.2后，改成检查头像，昵称，生日合法后，完成按钮才可点，所以toast可以注解掉。
//            ToastUtil.showToast(mActivity, R.string.reg_tip_birthday_empty);
        }
        else{
            isValid = true;
        }
        
        return isValid;
    }
    
    private final View.OnClickListener mClickAvatar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mAvatarHelper.changeAvatar();
        }
    };
    
    private final View.OnClickListener mClickBack = new OnClickListener(){
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    
    private final View.OnClickListener mClickBirthday = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDatePicker();
        }
    };
    
    private View.OnClickListener mClickFinish = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 如果是刚注册了进到这个页面，那么需要先到URS登录，才能提交数据
//            String token = EgmPrefHelper.getURSToken(mActivity);
        	 if(mAccountType == EgmProtocolConstants.AccountType.YiXin){
        		 mToken = EgmProtocol.getInstance().getUrsToken();
        		 doUpload();
        	 } else {
            mToken = ManagerAccount.getInstance().getCurrentAccountToken();
            
            if(TextUtils.isEmpty(mToken)){
                mLoginTid = EgmService.getInstance().doLoginURS(mName, mPassword);
            }
            else{
                doUpload();
            }
        	 }
        }
    };

//    private LoginHelper.ILoginSuccess mLoginSuccess = new LoginHelper.ILoginSuccess() {
//        @Override
//        public void onLoginSuccess() {
//            doUpload();
//        }
//    };
    
//    private LoginHelper.ILoginError mLoginError = new LoginHelper.ILoginError() {
//        @Override
//        public void onLoginError(int errCode, String errStr) {
//            stopWaiting();
//            ToastUtil.showToast(mActivity, R.string.reg_tip_fill_userinfo_error);
//        }
//    };
    
    private EgmDatePickerDialog.OnDateSetListener mDateSetListener = new EgmDatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(EgmDatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            
            updateBirthdayEt();
        }
    };
    
    private TextWatcher mTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
		
		@Override
		public void afterTextChanged(Editable arg0) {
			setFinishBtnEnableOrNot();
		}
	};
    
	private void setFinishBtnEnableOrNot() {
		if(checkBirthday(mBirthDayTv.getText().toString()) && 
				checkNick(mNickNameEt.getText().toString()) && checkAvatar()) {
			okTv.setEnabled(true);
			okTv.setBackgroundResource(R.drawable.titlebar_c_selector);
			okTv.setTextColor(getResources().getColor(R.color.white));
		} else {
			okTv.setEnabled(false);
			okTv.setTextColor(getResources().getColor(R.color.titlebar_disable_black));
		}
	}
	
    private void updateBirthdayEt(){
        String birthdayStr = mActivity.getResources().getString(R.string.birthday_format, mYear, mMonth, mDay);
        mBirthDayTv.setText(birthdayStr);
    }
    
    private void showDatePicker(){
        EgmDatePickerDialog dateDialog = new EgmDatePickerDialog(mActivity,
                mDateSetListener,
                mYear, mMonth, mDay, 
                mMaxYear, mMinYear);
        
        dateDialog.setCanceledOnTouchOutside(false);
        dateDialog.setTitle(R.string.reg_title_birthday);
        dateDialog.showDialog();
    }
    
    private void gotoRecommend(int portraitStatus){
        ActivityHome.startActivity(mActivity, mIsFromRegister, portraitStatus);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        mAvatarHelper.onActivityResult(requestCode, resultCode, data);
        
        if(mAvatarHelper.mCropAvatar != null){  // 获取并裁剪成功
            mAvatarCoor = mAvatarHelper.mAvatarCoor;
            mAvatarPath = mAvatarHelper.mAvatarPath;
            mCropAvatar = mAvatarHelper.mCropAvatar;
            
            setAvatar(mCropAvatar);
            
            setFinishBtnEnableOrNot();
        }
    }
    
    /* 点击软键盘外的区域使软键盘隐藏 */
    private InputMethodManager  mManager;
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        if(event.getAction() == MotionEvent.ACTION_DOWN){  
            if(mManager != null && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null){  
                mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }  
        }  
        return super.onTouchEvent(event);  
    }  
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
    	 /** 登录URS成功 */
        @Override
        public void onLoginURS(int transactionId, String token){
            if(transactionId != mLoginTid)
                return;
            
            mToken = token;
            
            doUpload();
        }
        @Override
        public void onLoginURSError(int transactionId, int errCode, String err){
            if(transactionId != mLoginTid)
                return;
            stopWaiting();
            ToastUtil.showToast(mActivity, R.string.reg_tip_fill_userinfo_error);
        }
        @Override
        public void onFillUserInfo(int transactionId, UserInfo userInfo){
            if(mFillTid != transactionId)
                return;
            
            stopWaiting();
            
            finish(); // 结束当前activity
            gotoRecommend(userInfo.portraitStatus);
        }
        
        @Override
        public void onFillUserInfoError(int transactionId, int errCode, String err){
            if(mFillTid != transactionId)
                return;
            
            stopWaiting();
            switch(errCode){
                case EgmServiceCode.TRANSACTION_ACCOUNT_PICTURE_FILE_TOO_BIG: 
                case EgmServiceCode.TRANSACTION_ACCOUNT_PICTURE_SIZE_ILLEGAL:
                case EgmServiceCode.TRANSACTION_ACCOUNT_PICTURE_CROP_COORDINATE_ERROR: 
                    ToastUtil.showToast(mActivity, R.string.reg_tip_avatar_size_error);
                    break;
                    
                case EgmServiceCode.TRANSACTION_ACCOUNT_NICKNAME_ILLEGAL: 
                    ToastUtil.showToast(mActivity, R.string.reg_tip_nick_invalid);
                    break;
                    
                case EgmServiceCode.TRANSACTION_INVITE_CODE_ILLEGAL:
                    ToastUtil.showToast(mActivity, R.string.reg_tip_inviteCode_invalid);
                    break;
                    
                case EgmServiceCode.TRANSACTION_COMMON_REGISTER_ALREADY:
                    ToastUtil.showToast(mActivity, err);
                    break;
                    
                default:
                    ToastUtil.showToast(mActivity, err);
                    break;
            }
        }
        @Override
        public void onGetOauthUserInfo(int transactionId, OauthUserInfo info) {
        		if(mGetOauthUserTid != transactionId)
        			return;
        	
	        	if(mNickNameEt != null && info != null && !TextUtils.isEmpty(info.nick)){
	        		mNickNameEt.setText(info.nick);
	        		mNickNameEt.setSelection(info.nick.length());
	        	}
        };
        @Override
        public void onGetOauthUserInfoError(int transactionId, int errCode, String err) {
        	if(mGetOauthUserTid != transactionId)
    			return;
        	 	ToastUtil.showToast(mActivity, err);
        };
    };
}
