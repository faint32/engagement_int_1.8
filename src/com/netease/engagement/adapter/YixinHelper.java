package com.netease.engagement.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.widget.ProgressTextBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.Utils.SmsObserver;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.YixinAddFriendInfo;
import com.netease.service.transactions.GetMobileVerifyTransaction;

/** 易信相关操作的帮助类，主要是添加好友，注册易信的交互和协议 */
public class YixinHelper {
	/** 去加别人为好友，在用户资料页 */
	public static final int TYPE_TO_ADD_FRIEND = 1;
	/** 被别人加为好友，在小爱助手去看看消息里 */
	public static final int TYPE_BE_ADD_FRIEND = 2;
	/** 聊天页面推荐使用易信，去加好友，已是好友需要跳转到易信聊天 */
	public static final int TYPE_FROM_CHAT_ADD_FRIEND = 3;

	private int mType;
	private Context mContext;
	private FragmentBase mFragment;
	private int mYixinAddFriendTid;
	private int mYixinRegisterTid;
	private int mYixinCheckTid;

	private IYixinCallback mYixinCallback;
	private YixinAddFriendInfo mYixinAddFriendInfo;

	private AlertDialog mTipDialog;
	private View mDialogLayout;
	private TextView mTitleTv;
	private TextView mClose;
	private TextView mContent1, mContent2;
	private View mContentIcon;
	private EditText mInputEt;
	private TextView mBtn;

	/** 绑定手机号layout */
	private View mBindMobileLay;
	/** 手机号输入框 */
	private EditText mPhoneEt;
	/** 验证按钮，组合了文字和进度条的控件 */
	private ProgressTextBar mVerifyBtn;
	/** 验证码输入框 */
	private EditText mVerifyEt;
	/** 重发倒计时60秒 */
	private final int TIME_COUNTER = 60;
	private int mVerifyTid;
	private int mBindTid;
	private long mUid;

	private SmsObserver mSmsObserver;// 监听验证码短信

	/* 点击软键盘外的区域使软键盘隐藏 */
	private InputMethodManager mManager;

	public YixinHelper(Context context, int type) {
		init(context);
		mType = type;
	}

	public YixinHelper(FragmentBase fragment, IYixinCallback callback, int type) {
		mFragment = fragment;
		mYixinCallback = callback;
		init(fragment.getActivity());
		mType = type;
	}

	private void init(Context context) {
		mContext = context;
		mManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		EgmService.getInstance().addListener(mEgmCallback);
		mTipDialog = new AlertDialog.Builder(mContext).create();
		mTipDialog.setCanceledOnTouchOutside(false);

		mDialogLayout = LayoutInflater.from(mContext).inflate(
				R.layout.view_yixin_guide_dialog, null, false);
		mTitleTv = (TextView) mDialogLayout
				.findViewById(R.id.yixin_dialog_title);
		mClose = (TextView) mDialogLayout.findViewById(R.id.yixin_dialog_close);
		mContentIcon = mDialogLayout
				.findViewById(R.id.yixin_dialog_content_icon);
		mContent1 = (TextView) mDialogLayout
				.findViewById(R.id.yixin_dialog_content1);
		mContent2 = (TextView) mDialogLayout
				.findViewById(R.id.yixin_dialog_content2);
		mInputEt = (EditText) mDialogLayout
				.findViewById(R.id.yixin_dialog_input);
		mBtn = (TextView) mDialogLayout.findViewById(R.id.yixin_dialog_btn);

		mClose.setOnClickListener(mClickDismiss);

		// 验证手机号流程
		mBindMobileLay = mDialogLayout.findViewById(R.id.bind_mobile_layout);
		mPhoneEt = (EditText) mDialogLayout
				.findViewById(R.id.input_phone_number);
		mVerifyBtn = (ProgressTextBar) mDialogLayout
				.findViewById(R.id.verify_btn);
		mVerifyBtn.setMax(TIME_COUNTER);

		mVerifyEt = (EditText) mDialogLayout
				.findViewById(R.id.input_verify_number);

	}

	/**
	 * 设置验证按钮的状态。
	 * 
	 * @param progress
	 *            小于零则为待验证状态，否则为重发状态，值为等待下次重发的进度。
	 */
	private void setVerifyBtnState(int progress) {
		if (mVerifyBtn == null)
			return;

		if (progress < 0) { // 验证
			mVerifyBtn.setClickable(true);
			mVerifyBtn.setText(mContext.getString(R.string.phone_verify));
			mVerifyBtn.setProgress(TIME_COUNTER);
		} else { // 重发
			mVerifyBtn.setText(mContext.getString(R.string.resend));
			progress = TIME_COUNTER - progress;
			mVerifyBtn.setProgress(progress);
			if (mVerifyBtn.getMax() == progress) { // 进度满了
				mVerifyBtn.setClickable(true);
			} else {
				mVerifyBtn.setClickable(false);
			}
		}
	}

	private View.OnClickListener mClickVerify = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String mobile = mPhoneEt.getText().toString();

			if (checkMobile(mobile)) {
				// if(mFragment != null){
				// mFragment.showWatting(null,
				// mContext.getString(R.string.common_tip_is_waitting), false);
				// }
				showWaitting();
				mVerifyTid = EgmService.getInstance().doGetMobileVerifyCode(
						GetMobileVerifyTransaction.TYPE_YIXIN_BIND, mobile);
			}
		}
	};

	/** 判断输入的手机号是否合法 */
	private boolean checkMobile(String mobile) {
		boolean legal = false;

		if (!TextUtils.isEmpty(mobile)) {
			if (EgmUtil.isValidatePhoneNum(mobile)) {
				legal = true;
			} else {
				ToastUtil.showToast(mContext,
						R.string.reg_tip_mobile_format_invalid);
			}
		} else {
			ToastUtil.showToast(mContext, R.string.reg_tip_mobile_is_empty);
		}

		return legal;
	}

	public void removeCallback() {
		EgmService.getInstance().removeListener(mEgmCallback);
	}

	/** 添加易信好友 */
	public void addFriend(long uid) {
		showWaitting();
		mUid = uid;
		mYixinAddFriendTid = EgmService.getInstance().doYixinAddFriend(uid);
	}

	/** 启动易信 */
	public boolean startYixin() {
		return EgmUtil.startAppByPackageName(mContext,
				EgmConstants.YIXIN_PACKAGE);
	}

	private ProgressDialog mWaitingProgress;

	/** 查看小爱助手的去看看消息 */
	public void checkYixinAddFriend() {
		showWaitting();
		mYixinCheckTid = EgmService.getInstance().doYixinCheck();
	}

	/** 好友申请已发送 */
	private void onHasSendApply() {
		if (EgmUtil.isAppInstall(mContext, EgmConstants.YIXIN_PACKAGE)) {
			buildHasSendLayout();
		} else {
			buildInstallLayout();
		}

		mTipDialog.setView(mDialogLayout);// .setContentView(mDialogLayout);
		mTipDialog.show();
	}

	/** 需要验证手机号 */
	private void onNeedBindMobile() {
		buildBindMobileLayout();
		mTipDialog.setView(mDialogLayout);// .setContentView(mDialogLayout);
		mTipDialog.show();
	}

	/** 未注册 */
	private void onNoRegister() {
		buildRegisterLayout();

		mTipDialog.setView(mDialogLayout);
		mTipDialog.show();
	}

	/** 双方已是好友 */
	public void onIsFriend() {
		if (EgmUtil.isAppInstall(mContext, EgmConstants.YIXIN_PACKAGE)) { // 已安装易信
			if (mType == TYPE_FROM_CHAT_ADD_FRIEND) {
				startYixin();
			} else {
				ToastUtil.showToast(mContext, R.string.yixin_tip_alread_friend);
				if (mYixinCallback != null) {
					mYixinCallback.onAlreadyFriend();
				}
			}
			if (mTipDialog.isShowing()) {
				mTipDialog.dismiss();
			}
		} else { // 未安装易信
			buildInstallLayout();
			mTipDialog.setView(mDialogLayout);
			mTipDialog.show();
		}
	}

	/** 查看小爱助手添加好友去看看消息后处于已注册的状态 */
	private void onCheckRegister() {
		if (EgmUtil.isAppInstall(mContext, EgmConstants.YIXIN_PACKAGE)) { // 已安装易信，去启动易信
			startYixin();
		} else { // 未安装，则去引导安装
			buildInstallLayout();
			mTipDialog.setView(mDialogLayout);
			mTipDialog.show();
		}
	}

	public Handler smsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SmsObserver.MSG_VERIFYCODE:
				if (mVerifyEt != null
						&& mVerifyEt.getVisibility() == View.VISIBLE) {
					String code = (String) msg.obj;
					mVerifyEt.setText(code.trim());
					mContext.getContentResolver().unregisterContentObserver(
							mSmsObserver);
				}
				break;
			}
		};
	};

	/** 创建绑定手机号页面 */
	private void buildBindMobileLayout() {
		mTitleTv.setText(R.string.yixin_check_mobile);
		mContent1.setText(R.string.yixin_free_chat);
		mContentIcon.setVisibility(View.GONE);
		mContent2.setVisibility(View.GONE);
		mInputEt.setVisibility(View.GONE);
		mPhoneEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					mVerifyBtn.setClickable(false);
				} else {
					mVerifyBtn.setClickable(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		mVerifyBtn.setOnClickListener(mClickVerify);
		setVerifyBtnState(-1);
		mVerifyBtn.setClickable(false);
		mBtn.setText(R.string.yixin_check_next);
		mBtn.setOnClickListener(mClickNext);
		mBindMobileLay.setVisibility(View.VISIBLE);
		mDialogLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideIme();
			}
		});
		// String phoneNumber = PlatformUtil.getPhoneNumber(mContext);
		// if(!TextUtils.isEmpty(phoneNumber)){
		// mPhoneEt.setText(phoneNumber.trim());
		// }
		mSmsObserver = new SmsObserver(mContext, smsHandler);
		mContext.getContentResolver().registerContentObserver(
				SmsObserver.SMS_INBOX, true, mSmsObserver);
	}

	/** 创建注册易信界面 */
	private void buildRegisterLayout() {
		mTitleTv.setText(R.string.yixin_set_password);
		mContent1.setText(R.string.yixin_free_chat);
		mContentIcon.setVisibility(View.GONE);
		mDialogLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideIme();
			}
		});

		if (mYixinAddFriendInfo != null) {
			mContent2.setVisibility(View.VISIBLE);
			mContent2.setText(mContext.getString(R.string.yixin_account_is)
					+ mYixinAddFriendInfo.mobile);
		}

		mInputEt.setVisibility(View.VISIBLE);
		mInputEt.setText("");
		mBtn.setText(R.string.yixin_register);
		mBtn.setOnClickListener(mClickRegister);
		mBindMobileLay.setVisibility(View.GONE);
	}

	/** 创建安装易信对话界面 */
	private void buildInstallLayout() {
		if (mType == TYPE_TO_ADD_FRIEND) {
			mTitleTv.setText(R.string.yixin_add_friend_apply_has_send);
		} else {
			mTitleTv.setText(R.string.yixin_add_friend_has_not_install);
		}

		mContent2.setVisibility(View.GONE);
		mInputEt.setVisibility(View.GONE);
		mContentIcon.setVisibility(View.VISIBLE);
		mContent1.setText(R.string.yixin_download_yixin);
		mBtn.setText(R.string.yixin_install);
		mBtn.setOnClickListener(mClickInstall);
		mBindMobileLay.setVisibility(View.GONE);
	}

	/** 创建好友申请已发送对话界面 */
	private void buildHasSendLayout() {
		mTitleTv.setText(R.string.yixin_add_friend_apply_has_send);
		mContent2.setVisibility(View.GONE);
		mInputEt.setVisibility(View.GONE);
		mContentIcon.setVisibility(View.VISIBLE);
		mContent1.setText(R.string.yixin_start_chat);
		mBtn.setText(R.string.confirm);
		mBtn.setOnClickListener(mClickDismiss);
		mBindMobileLay.setVisibility(View.GONE);
	}

	/** 创建注册成功并且已安装了易信的对话界面 */
	private void buildRegisterAndLaunchyLayout() {
		if (mType == TYPE_TO_ADD_FRIEND || mType == TYPE_FROM_CHAT_ADD_FRIEND) {
			buildHasSendLayout();
		} else if (mType == TYPE_BE_ADD_FRIEND) {
			mTitleTv.setText(R.string.yixin_register_success);
			mContent1.setVisibility(View.GONE);
			mContent2.setVisibility(View.GONE);
			mInputEt.setVisibility(View.GONE);
			mContentIcon.setVisibility(View.VISIBLE);
			mBtn.setText(R.string.yixin_goto_yixin);
			mBtn.setOnClickListener(mClickLaunch);
			mBindMobileLay.setVisibility(View.GONE);
		}
	}

	private void doRegister() {
		String password = mInputEt.getText().toString();

		if (mYixinAddFriendInfo != null && checkPassword(password)) {
			showWaitting();
			mYixinRegisterTid = EgmService.getInstance().doYixinRegister(
					mYixinAddFriendInfo.mobile, password,
					mYixinAddFriendInfo.AESKey);
		}
	}

	private void showWaitting() {
		if (mFragment != null) {
			mFragment.showWatting(mContext
					.getString(R.string.common_tip_is_waitting));
		} else {
			mWaitingProgress = ProgressDialog.show(mContext, null,
					mContext.getString(R.string.common_tip_is_waitting), true,
					true);
			mWaitingProgress.setCancelable(false);
		}
	}

	private void stopWaitting() {
		if (mWaitingProgress != null) {
			mWaitingProgress.dismiss();
			mWaitingProgress = null;
		}

		if (mFragment != null) {
			mFragment.stopWaiting();
		}
	}

	/** 检查密码的合法性 */
	private boolean checkPassword(String psw) {
		boolean legal = false;

		if (!TextUtils.isEmpty(psw)) {
			int length = psw.length();
			if (length >= 6 && length <= 16) {
				legal = true;
			} else {
				ToastUtil.showToast(mContext,
						R.string.reg_tip_password_is_illegal);
			}
		} else {
			ToastUtil.showToast(mContext, R.string.reg_tip_password_is_empty);
		}

		return legal;
	}

	/** 点击关闭窗口 */
	private View.OnClickListener mClickDismiss = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mTipDialog.isShowing()) {
				mTipDialog.dismiss();
			}
		}
	};

	/** 点击安装易信 */
	private View.OnClickListener mClickInstall = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Uri uri = Uri.parse(EgmConstants.URL_YIXIN);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			mContext.startActivity(intent);

			if (mTipDialog.isShowing()) {
				mTipDialog.dismiss();
			}
		}
	};

	/** 点击注册易信 */
	private View.OnClickListener mClickRegister = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			InputMethodManager im;
			Object o = mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (o != null) {
				im = (InputMethodManager) o;
				if (im != null && im.isActive()) {
					im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}

			doRegister();
		}
	};
	/** 验证手机号点击下一步 */
	private View.OnClickListener mClickNext = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			InputMethodManager im;
			Object o = mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (o != null) {
				im = (InputMethodManager) o;
				if (im != null && im.isActive()) {
					im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
			String mobile = mPhoneEt.getText().toString();
			String verify = mVerifyEt.getText().toString();
			String invite = "";

			if (checkMobile(mobile) && checkVerifyCode(verify)) {
				// showWatting(null,
				// mContext.getString(R.string.reg_tip_waitting_bind), false);
				showWaitting();
				mBindTid = EgmService.getInstance().doBindYixin(mobile, verify);
			}
		}
	};

	private boolean checkVerifyCode(String verify) {
		boolean legal = false;

		if (!TextUtils.isEmpty(verify)) {
			legal = true;
		} else {
			ToastUtil.showToast(mContext, R.string.reg_tip_verify_is_empty);
		}

		return legal;
	}

	/** 点击启动易信 */
	private View.OnClickListener mClickLaunch = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startYixin();
		}
	};

	private void hideIme() {
		if (mManager != null && mInputEt != null) {
			mManager.hideSoftInputFromWindow(mInputEt.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	private EgmCallBack mEgmCallback = new EgmCallBack() {
		/** 易信添加好友成功 */
		@Override
		public void onYixinAddFriend(int transactionId, YixinAddFriendInfo info) {
			if (mYixinAddFriendTid != transactionId)
				return;

			stopWaitting();
			if (info == null) { // 好友申请已发送成功
				onHasSendApply();
			} else { // 自己未注册
				mYixinAddFriendInfo = info;
				onNoRegister();
			}
		}

		@Override
		public void onYixinAddFriendError(int transactionId, int errCode,
				String err) {
			if (mYixinAddFriendTid != transactionId)
				return;

			stopWaitting();
			switch (errCode) {
			case EgmServiceCode.TRANSACTION_YIXIN_SEND_FRIEND_APPLY: // 对方不是易信用户，和添加成功的区别是对方已是易信用户
				onHasSendApply();
				break;

			case EgmServiceCode.TRANSACTION_YIXIN_MOBILE_BINE_BY_OTHER:
				ToastUtil.showToast(mContext,
						R.string.yixin_tip_account_is_occupy);
				break;

			case EgmServiceCode.TRANSACTION_YIXIN_IS_FRIEND:
				onIsFriend();
				break;
			case EgmServiceCode.TRANSACTION_YIXIN_NO_BIND_MOBILE:
				onNeedBindMobile();
				break;
			case EgmServiceCode.TRANSACTION_YIXIN_APPLY_ONCE:
			case EgmServiceCode.TRANSACTION_YIXIN_FRIEND_OVERFLOW:
			default:
				ToastUtil.showToast(mContext, err);
				break;
			}
		}

		/** 注册易信 */
		@Override
		public void onYixinRegister(int transactionId) {
			if (mYixinRegisterTid != transactionId)
				return;

			stopWaitting();

			if (EgmUtil.isAppInstall(mContext, EgmConstants.YIXIN_PACKAGE)) { // 已安装易信
				buildRegisterAndLaunchyLayout();
			} else {
				buildInstallLayout();
			}
		}

		@Override
		public void onYixinRegisterError(int transactionId, int errCode,
				String err) {
			if (mYixinRegisterTid != transactionId)
				return;

			stopWaitting();

			if (mTipDialog.isShowing()) {
				mTipDialog.dismiss();
			}
			ToastUtil.showToast(mContext, err);
		}

		/** 查看小爱助手易信添加好友 */
		@Override
		public void onYixinCheck(int transactionId, YixinAddFriendInfo info) {
			if (mYixinCheckTid != transactionId)
				return;

			stopWaitting();
			if (info == null) { // 查看成功，属于已注册
				onCheckRegister();
			} else { // 自己未注册
				mYixinAddFriendInfo = info;
				onNoRegister();
			}
		}

		@Override
		public void onYixinCheckError(int transactionId, int errCode, String err) {
			if (mYixinCheckTid != transactionId)
				return;

			stopWaitting();
			switch (errCode) {
			case EgmServiceCode.TRANSACTION_YIXIN_NO_BIND_MOBILE: // 对方不是易信用户，和添加成功的区别是对方已是易信用户
				onNeedBindMobile();
				break;
			case EgmServiceCode.TRANSACTION_YIXIN_HAS_REGISTER:
				onCheckRegister();
				break;
			case EgmServiceCode.TRANSACTION_YIXIN_MOBILE_BINE_BY_OTHER:
				ToastUtil.showToast(mContext,
						R.string.yixin_tip_account_is_occupy);
				break;
			default:
				ToastUtil.showToast(mContext, err);
				break;
			}
		}

		// 验证码
		@Override
		public void onGetMobileVerify(int transactionId, Object nullObj) {
			if (transactionId != mVerifyTid)
				return;

			stopWaitting();
			mCountDownTimer.start();
			ToastUtil.showToast(mContext, R.string.reg_tip_verify_code_send);
		}

		@Override
		public void onGetMobileVerifyError(int transactionId, int errCode,
				String err) {
			if (transactionId != mVerifyTid)
				return;

			stopWaitting();
			switch (errCode) {
			case EgmServiceCode.TRANSACTION_MOBILE_BIND_YUEHUI_ALREADY:
				ToastUtil
						.showToast(mContext, R.string.reg_tip_register_already);
				break;
			case EgmServiceCode.TRANSACTION_SMS_REQUIRE_TOO_MANY:
				ToastUtil.showToast(mContext,
						R.string.reg_tip_verify_code_too_many);
				break;
			default:
				ToastUtil.showToast(mContext, err);
				break;
			}
		}

		// 绑定手机号
		@Override
		public void onBindYixin(int transactionId, YixinAddFriendInfo info) {
			if (transactionId != mBindTid)
				return;

			// ToastUtil.showToast(mContext,
			// R.string.reg_tip_mobile_bind_success);
			stopWaitting();

			if (info == null) { // 绑定易信成功
				if (mType != TYPE_BE_ADD_FRIEND) {
					if (mUid > 0) {
						addFriend(mUid);
					}
				} else {
					if (EgmUtil.isAppInstall(mContext, EgmConstants.YIXIN_PACKAGE)) { // 已安装易信
						buildRegisterAndLaunchyLayout();
					} else {
						buildInstallLayout();
					}
				}
			} else { // 自己未注册
				mYixinAddFriendInfo = info;
				onNoRegister();
			}
		}

		@Override
		public void onBindYixinError(int transactionId, int errCode, String err) {
			if (transactionId != mBindTid)
				return;

			stopWaitting();
			switch (errCode) {
			case EgmServiceCode.TRANSACTION_MOBILE_BIND_YUEHUI_ALREADY:
				ToastUtil.showToast(mContext, R.string.reg_tip_register_already);
				break;
			case EgmServiceCode.TRANSACTION_VERIFY_CODE_ERROR:
				ToastUtil.showToast(mContext, R.string.reg_tip_verify_error);
				break;
			case EgmServiceCode.TRANSACTION_MOBILE_FORMAT_ERROR:
				ToastUtil.showToast(mContext,
						R.string.reg_tip_mobile_format_invalid);
				break;
			// case EgmServiceCode.TRANSACTION_YIXIN_NO_REGISTER:
			// onNoRegister();
			// break;
			default:
				ToastUtil.showToast(mContext, err);
				break;
			}
		}
	};

	public interface IYixinCallback {
		/** 添加好友时双方已是好友 */
		public void onAlreadyFriend();
	}

	/** 倒计时器，倒计时60秒，每隔一秒给出一个通知 */
	private CountDownTimer mCountDownTimer = new CountDownTimer(
			TIME_COUNTER * 1000, 1000) {
		@Override
		public void onTick(long millisUntilFinished) {
			int progress = (int) (millisUntilFinished / 1000);
			setVerifyBtnState(progress);
		}

		@Override
		public void onFinish() {
			setVerifyBtnState(0);
		}
	};
}
