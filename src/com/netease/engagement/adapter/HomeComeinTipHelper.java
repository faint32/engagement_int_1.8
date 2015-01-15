package com.netease.engagement.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityPageInfo;
import com.netease.engagement.activity.ActivityPicUploadEntrance;
import com.netease.engagement.activity.ActivitySelectPosition;
import com.netease.engagement.activity.ActivityYuanfen;
import com.netease.engagement.activity.ActivityPicUploadEntrance.PicType;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentHome;
import com.netease.engagement.fragment.FragmentYuanfen;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.engagement.view.ProfileView;
import com.netease.engagement.view.ShareDialog;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoginUserInfo;
import com.netease.service.protocol.meta.PortraitInfo;
import com.netease.service.protocol.meta.RecommendListInfo;
import com.netease.service.protocol.meta.UserInfo;


/**
 * 推荐弹层逻辑
 * 1、从注册入口进入的时候，只谈注册弹层
 * 2、非注册入口，头像弹层实时显示，其他弹层，只显示一次。
 * 3、每次获取推荐数据的时候，都会回调弹层逻辑，只有第一次全部遍历，以后的只遍历弹头像
 */
public class HomeComeinTipHelper {
	private Activity mContext;
	private FragmentBase mFragment;
	private int mComeinTipPriority = ComeinTipPriority.min;

	private int mChangeAvatarTid;
	private int mUploadPositionTid;
	private int mGetUserInfoTid;

	private UploadPictureHelper mUploadPictureHelper;
	private SelectAvatarHelper mAvatarHelper;

	private Dialog mTipDialog;


	/** 刚进入主页的弹窗提示的优先级，绑定手机号不放在这里处理 */
	private interface ComeinTipPriority {
		/** 上传头像 */
		public int avatar = 1;
		/** 上传地理位置 */
		public int position = 2;
		/** 上传照片 */
		public int uploadPicture = 3;
		/** 开启碰缘分 */
		public int yuanfen = 4;
		/** 完善个人资料 */
		public int selfinfo = 5;
		/** 更新照片 */
		public int updatePicture = 6;
		
		public int levelchange=7;

		/** 最低优先级 */
		public int min = 1000;
	}

	public HomeComeinTipHelper(FragmentBase fragment,
			ManagerAccount.Account accout, UploadPictureHelper helper,
			SelectAvatarHelper avatarHelper) {

		mFragment = fragment;
		mContext = fragment.getActivity();
		mUploadPictureHelper = helper;
		mAvatarHelper = avatarHelper;

		mTipDialog = new Dialog(mContext, R.style.CustomDialog);
		mTipDialog.setCanceledOnTouchOutside(false);
	}

	public void registerCallback() {
		mUploadPictureHelper.registerCallback();
		EgmService.getInstance().addListener(mEgmCallBack);
	}

	public void removeCallback() {
		mUploadPictureHelper.removeCallback();
		EgmService.getInstance().removeListener(mEgmCallBack);
	}

	// 上传头像，特殊处理，不过现实内容需要改变，由后端传回 portraitStatus 可以省略了


	/**
	 * 开启碰缘分提示。因为碰缘分需要在从服务器获取状态后再判断是否提示，所以不放在showComeinTip()里统一处理。
	 * 注意：要保证先执行showComeinTip()，再执行本方法，这样才能确保提示的优先级的正确性。
	 */
	public void showYuanfenTip() {
		
		if(mTipDialog.isShowing()){
			return;
		}
		
		mComeinTipPriority = ComeinTipPriority.yuanfen;
		EgmPrefHelper.putTipOpenYuanfenTime(mContext,
				System.currentTimeMillis());

		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.view_dialog_open_yuanfen_tip, null, false);
		layout.findViewById(R.id.open_yuanfen_ok).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mTipDialog != null) {
							mTipDialog.dismiss();
						}
						ActivityYuanfen.startActivity(mContext, true);
					}
				});
		mTipDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
			}
		});

		mTipDialog.setCancelable(true);
		mTipDialog.setContentView(layout);
		mTipDialog.show();
	}

	/** 显示刚进入主界面时的提示 */
	public void showRegisterComeinTip() {
		// 男性新手礼
		//
		if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {
			showRegisterMaleRegisterTip();
		} else {
			showRegisterFemaleUploadPicTip();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (mComeinTipPriority) {
		case ComeinTipPriority.avatar:
			mAvatarHelper.onActivityResult(requestCode, resultCode, data);
			onSelectedAvatar();
			break;
		case ComeinTipPriority.position:
			if (resultCode == Activity.RESULT_CANCELED || data == null) {
				ToastUtil.showToast(mContext,
						R.string.rec_tip_select_position_faild);
				;
			} else if (requestCode == EgmConstants.REQUEST_SELECT_POSITION) {
				mProvinceCode = data.getExtras().getInt(
						ActivitySelectPosition.EXTRA_RESULT_PROVINCE_CODE, 0);
				mProvince = data.getExtras().getString(
						ActivitySelectPosition.EXTRA_RESULT_PROVINCE, "");
				mCityCode = data.getExtras().getInt(
						ActivitySelectPosition.EXTRA_RESULT_CITY_CODE, 0);
				mCity = data.getExtras().getString(
						ActivitySelectPosition.EXTRA_RESULT_CITY, "");

				onSelectedPosition();
			}
			break;
		}

	}

	/** 注册完成后男性新手礼提示 */
	private void showRegisterMaleRegisterTip() {
		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.view_dialog_freshmen_gif_tip, null, false);
		layout.findViewById(R.id.freshman_gif_ok).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mTipDialog != null) {
							mTipDialog.dismiss();
						}
					}
				});

		mTipDialog.setCancelable(true);
		mTipDialog.setContentView(layout);
		mTipDialog.show();
	}

	/** 注册完成后女性传照片提示 */
	private void showRegisterFemaleUploadPicTip() {
		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.view_upload_picture_dialog_layout, null, false);
		layout.findViewById(R.id.upload_dialog_title).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mTipDialog != null) {
							mTipDialog.dismiss();
						}
					}
				});

		layout.findViewById(R.id.upload_dialog_btn).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mTipDialog != null) {
							mTipDialog.dismiss();
						}

						if (mUploadPictureHelper != null) {
							mUploadPictureHelper.showUploadUpPicEntrance();
						}
					}
				});

		mTipDialog.setCancelable(true);
		mTipDialog.setContentView(layout);
		mTipDialog.show();
	}

	/**
	 * 上传照片提示
	 * 
	 * @param isUpdate
	 *            true:更新照片；false:上传照片
	 */
	private void showUploadPictureTip(boolean isUpdate) {
		
		if(mTipDialog.isShowing()){
			return;
		}
		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.view_dialog_select_picture, null, false);

		TextView title1 = (TextView) layout
				.findViewById(R.id.select_picture_title1);
		TextView title2 = (TextView) layout
				.findViewById(R.id.select_picture_title2);

		if (isUpdate) { // 更新照片
			title1.setText(R.string.rec_tip_update_pic_content1);
			title2.setText(R.string.rec_tip_update_pic_content2);
			EgmPrefHelper.putTipUpdatePicTime(mContext,
					System.currentTimeMillis());
		} else { // 上传照片
			if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {
				title1.setText(R.string.rec_tip_upload_pic_content1);
				title2.setText(R.string.rec_tip_upload_pic_content2);
			} else {
				title1.setText(R.string.rec_tip_upload_pic_content1_female);
				title2.setText(R.string.rec_tip_upload_pic_content2_female);
			}

			EgmPrefHelper.putTipUploadPicTime(mContext,
					System.currentTimeMillis());
		}

        layout.findViewById(R.id.upload_pic_private).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mTipDialog != null) {
                    mTipDialog.dismiss();
                }
                ActivityPicUploadEntrance.startActivity(mContext, PicType.PRIVATE_PIC);
            }
        });

        layout.findViewById(R.id.upload_pic_public).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mTipDialog != null) {
                    mTipDialog.dismiss();
                }
                ActivityPicUploadEntrance.startActivity(mContext, PicType.PUBLIC_PIC);
            }
        });

		mTipDialog.setCancelable(true);
		mTipDialog.setContentView(layout);
		mTipDialog.show();
	}

	private TextView mPositionTv;
	private View mPositionOkBtn;
	private int mProvinceCode, mCityCode; // 取得的位置信息
	private String mProvince, mCity;

	/** 补充地理位置提示 */
	private void showPositionTip() {
		
		if(mTipDialog.isShowing()){
			return;
		}
		// 初始化数据，否则下次进来会有上次的数据
		mProvinceCode = 0;
		mProvince = null;
		mCityCode = 0;
		mCity = null;

		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.view_dialog_select_position, null, false);

		// 选择地理位置
		mPositionTv = (TextView) layout.findViewById(R.id.select_position);
		mPositionTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mFragment != null){
					ActivitySelectPosition.startActivityForResult(mFragment,
							EgmConstants.REQUEST_SELECT_POSITION);
				}
			}
		});

		// 上传地理位置
		mPositionOkBtn = layout.findViewById(R.id.select_position_ok);
		mPositionOkBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showWatting(null,
						mContext.getString(R.string.common_tip_is_waitting));
				mUploadPositionTid = EgmService.getInstance().doUploadLocation(
						null,
						null,
						mProvinceCode > 0 ? String.valueOf(mProvinceCode)
								: null,
						mCityCode > 0 ? String.valueOf(mCityCode) : null, null);
			}
		});

		mTipDialog.setContentView(layout);
		mTipDialog.setCancelable(false);

		mTipDialog.show();
	}

	/** 获取到地理位置以后的处理 */
	private void onSelectedPosition() {
		if (!TextUtils.isEmpty(mProvince)) {
			mPositionOkBtn.setEnabled(true); // 确认按钮可以点，也就是可以上传
			if (TextUtils.isEmpty(mCity) || // 港澳台
					mProvince.equalsIgnoreCase(mCity)) { // 直辖市
				mPositionTv.setText(mProvince);
			} else {
				mPositionTv.setText(mProvince + mCity);
			}
		} else {
			mPositionOkBtn.setEnabled(false);
		}
	}

	/** 改头像对话框上的头像 */
	private ProfileView mAvatarIv;
	/** 改头像对话框上的确认按钮 */
	private View mAvatarOkBtn;

	/** 获取到头像以后的处理 */
	private void onSelectedAvatar() {
		if (mAvatarHelper.mCropAvatar != null
				&& !TextUtils.isEmpty(mAvatarHelper.mAvatarPath)
				&& mAvatarHelper.mAvatarCoor != null
				&& mAvatarHelper.mAvatarCoor.length == 4) {

			mAvatarOkBtn.setEnabled(true); // 确认按钮可以点，也就是可以上传
			mAvatarIv.setImage(false, ProfileView.PROFILE_SIZE_LARGE,
					mAvatarHelper.mCropAvatar);
			mAvatarIv.setBackgroundColor(mContext.getResources().getColor(
					R.color.transparent));
		} 
	}


	/** 完善个人资料 */
	private void showSelfInfoTip(final UserInfo mSelfUserInfo) {
		if(mTipDialog.isShowing()){
			return;
		}
		
		EgmPrefHelper.putTipSelfInfoTime(mContext, System.currentTimeMillis());

		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.view_dialog_self_info, null, false);
		TextView title1 = (TextView) layout.findViewById(R.id.selfinfo_title1);
		TextView title2 = (TextView) layout.findViewById(R.id.selfinfo_title2);

		if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {
			title1.setText(R.string.rec_tip_self_info_content1);
			title2.setText(R.string.rec_tip_self_info_content2);
		} else {
			title1.setText(R.string.rec_tip_self_info_content1_female);
			title2.setText(R.string.rec_tip_self_info_content2_female);
		}

		layout.findViewById(R.id.self_info_ok).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mTipDialog != null) {
							mTipDialog.dismiss();
						}
						ActivityPageInfo.startActivity(mFragment,
								String.valueOf(ActivityPageInfo.DETAIL_INFO),
								mSelfUserInfo);
					}
				});

		mTipDialog.setCancelable(true);
		mTipDialog.setContentView(layout);
		mTipDialog.show();
	}

	/** 个人资料填写完整的是否大于三项 */
	private boolean isSelfInfoInsufficient(UserInfo info) {
		int satisfyCount = 0;

		if (info == null)
			return true; // 用户信息取不到，就不用去补全资料了

		if (!TextUtils.isEmpty(info.nick)) { // 有昵称
			satisfyCount++;
		}

		if (info.height > 0) { // 有身高
			satisfyCount++;
		}

		if (info.weight > 0) {
			satisfyCount++;
		}

		if (info.bust > 0 || info.waist > 0 || info.hip > 0 || info.cup > 0) { // 有身材
			satisfyCount++;
		}

		if (info.satisfiedPart > 0) { // 有最满意的部位
			satisfyCount++;
		}

		if (info.constellation > 0) { // 有星座
			satisfyCount++;
		}

		if (info.favorDate != null && info.favorDate.length > 0) { // 有喜欢的约会
			satisfyCount++;
		}

		if (info.hobby != null && info.hobby.length > 0) { // 有兴趣爱好
			satisfyCount++;
		}

		if (info.skill != null && info.skill.length > 0) { // 有想学技能
			satisfyCount++;
		}

		if (!TextUtils.isEmpty(info.socialUrl)) { // 有社交网络
			satisfyCount++;
		}

		if (satisfyCount > 3)
			return true;

		return false;
	}

	/**
	 * 当前时间和上次进入时间相比是否是隔了多少天
	 * 
	 * @param lastTime
	 *            上次的时间
	 * @param interval
	 *            间隔的天数
	 */
	private boolean isAfterDay(long lastTime, int interval) {
		boolean first = false;
		long now = System.currentTimeMillis();

		if (lastTime <= 0) {
			first = true;
		} else if (now > lastTime) {
			long delta = now - lastTime;
			long day = 3600 * 24 * 1000;
			if (delta > interval * day) {
				first = true;
			}
		}

		return first;
	}

	private ProgressDialog mWaitingProgress;

	protected void showWatting(String title, String message) {
		if (mWaitingProgress != null)
			stopWaiting();

		mWaitingProgress = ProgressDialog.show(mContext, title, message, true,
				true);
	}

	protected void stopWaiting() {
		if (mWaitingProgress != null) {
			mWaitingProgress.dismiss();
			mWaitingProgress = null;
		}
	}

	/**
	 * 弹层提示、完成相关Transaction后的回调入口
	 */
	private EgmCallBack mEgmCallBack = new EgmCallBack() {
		/** 修改会员头像 */
		@Override
		public void onModifyProfileSucess(int transactionId, PortraitInfo obj) {
			if (mChangeAvatarTid != transactionId)
				return;

			stopWaiting();

			if (mTipDialog.isShowing()) {
				mTipDialog.dismiss();
			}

			ToastUtil.showToast(mContext, R.string.upload_pic_suc);
		}

		@Override
		public void onModifyProfileError(int transactionId, int errCode,
				String err) {
			if (mChangeAvatarTid != transactionId)
				return;

			stopWaiting();
			ToastUtil.showToast(mContext, err);
		}

		/** 上传地理位置 */
		@Override
		public void onUploadLocation(int transactionId) {
			if (mUploadPositionTid != transactionId)
				return;

			stopWaiting();

			if (mTipDialog.isShowing()) {
				mTipDialog.dismiss();
			}

			ToastUtil.showToast(mContext,
					R.string.rec_tip_upload_position_success);
			
		}

		@Override
		public void onUploadLocationError(int transactionId, int errCode,
				String err) {
			if (mUploadPositionTid != transactionId)
				return;

			stopWaiting();

			if (mTipDialog.isShowing()) {
				mTipDialog.dismiss();
			}

			ToastUtil.showToast(mContext, err);
		}
	};

	/**
	 * 发送网络请求获取弹层相关信息
	 * 
	 * @return 当前Transaction TID
	 */
	public int getUserInfoDetailForTips() {
		mGetUserInfoTid = EgmService.getInstance().doGetUserInfoDetail(ManagerAccount.getInstance().getCurrentId());
		return mGetUserInfoTid;
	}

	/**
	 * @param info
	 * 上传头像原因 没头像的情况下上传头像，主要分为以下几个状况
	 * 1、没有头像 
	 * 2、审核不通过 
	 * 3、照片库命中
	 */
	public void showUploadAvatarTip(int portraitStatus, String portraitTips) {

		if(mTipDialog.isShowing()){
			return;
		}
		
			mComeinTipPriority = ComeinTipPriority.avatar; 
			mAvatarHelper.init();

			View layout = LayoutInflater.from(mContext).inflate(R.layout.view_dialog_select_avatar, null, false);

			TextView tipTitle = (TextView) layout.findViewById(R.id.select_avatar_tip);
			TextView tipContent = (TextView) layout.findViewById(R.id.select_avatar_tip_2);
			//未上传头像	
			if(EgmConstants.PortraitStatus.NotUpload==portraitStatus)
			{
				tipTitle.setText(R.string.rec_tip_select_avatar3);
			}
			tipContent.setText(portraitTips);

			mAvatarIv = (ProfileView) layout.findViewById(R.id.select_avatar);
			mAvatarIv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mAvatarHelper != null) {
						mAvatarHelper.changeAvatar();
					}
				}
			});

			mAvatarOkBtn = layout.findViewById(R.id.select_avatar_ok);
			mAvatarOkBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					showWatting(null,
							mContext.getString(R.string.common_tip_is_waitting));
					// 上传到服务器
					mChangeAvatarTid = EgmService.getInstance()
							.doModifyPortrait(mAvatarHelper.mAvatarPath,
									mAvatarHelper.mAvatarCoor[0],
									mAvatarHelper.mAvatarCoor[1],
									mAvatarHelper.mAvatarCoor[2],
									mAvatarHelper.mAvatarCoor[3]);
				}
			});

			mTipDialog.setCancelable(false);
			mTipDialog.setContentView(layout);
			mTipDialog.show();
			
			// 为了防止双击穿透，显示遮罩
			((FragmentHome)mFragment).showCoverToIncept();
			
			// 上传结束，对话框消失的时候移除遮罩
			mTipDialog.setOnDismissListener(new OnDismissListener() {
                
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // 移除遮罩
                    ((FragmentHome)mFragment).hideCoverToIncept();
                }
            });
			
	}
	/**
	 * 用于聊天中禁止没头像的用户聊天引导用户传头像时使用，退出聊天即回收helper,不影响其它逻辑
	 * @param title
	 * @param content
	 * @param 返回按钮的回调
	 */
	public Dialog showUploadAvatarTipChat(String title,String content,View.OnClickListener backListener) {

		if(mTipDialog != null && mTipDialog.isShowing()){
			return mTipDialog;
		}
		
			mComeinTipPriority = ComeinTipPriority.avatar; 
			mAvatarHelper.init();

			View layout = LayoutInflater.from(mContext).inflate(R.layout.view_dialog_select_avatar, null, false);

			TextView tipTitle = (TextView) layout.findViewById(R.id.select_avatar_tip);
			TextView tipContent = (TextView) layout.findViewById(R.id.select_avatar_tip_2);

			tipTitle.setText(title);
			tipContent.setText(content);

			mAvatarIv = (ProfileView) layout.findViewById(R.id.select_avatar);
			mAvatarIv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mAvatarHelper != null) {
						mAvatarHelper.changeAvatar();
					}
				}
			});

			mAvatarOkBtn = layout.findViewById(R.id.select_avatar_ok);
			mAvatarOkBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
				    
                showWatting(null, mContext.getString(R.string.common_tip_is_waitting));
					
                // 上传到服务器
                if (mAvatarHelper == null || mAvatarHelper.mAvatarCoor == null
                        || mAvatarHelper.mAvatarPath == null) {
                    ToastUtil.showToast(mContext, R.string.avatar_select_failed_tips);
                    mAvatarOkBtn.setEnabled(false);
                    return;
                }
                mChangeAvatarTid = EgmService.getInstance().doModifyPortrait(
                        mAvatarHelper.mAvatarPath, 
                        mAvatarHelper.mAvatarCoor[0],
                        mAvatarHelper.mAvatarCoor[1], 
                        mAvatarHelper.mAvatarCoor[2],
                        mAvatarHelper.mAvatarCoor[3]);
            }
			});
//			View avatarBackBtn = layout.findViewById(R.id.select_avatar_back);
//			avatarBackBtn.setVisibility(View.VISIBLE);
//			avatarBackBtn.setOnClickListener(backListener);
//			
//			View avatarBackDiv = layout.findViewById(R.id.select_avatar_back_div);
//			avatarBackDiv.setVisibility(View.VISIBLE);
			

			mTipDialog.setCancelable(false);
			mTipDialog.setContentView(layout);
			mTipDialog.show();
			
			return mTipDialog;
	}

	/**
	 * @param 弹层的主要逻辑
	 * 男性用户：升级、降级、个人资料少于三项
	 * 女性用户：头像、升级、地理位置、传照片、碰缘分、个人信息、更新照片
	 */
	public void showComeinTips(LoginUserInfo info) {

		if(mTipDialog.isShowing())
			return;
		if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {

			LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
			long uid = ManagerAccount.getInstance().getCurrentId();
			if (status.getUid() != uid) {
				status.clear();
			} else {
				if (status.getType() == LevelChangeType.Male_Level_Down
						|| status.getType() == LevelChangeType.Male_Level_Up_1) {
					new ShareDialog().showLevel(mFragment, status.getType(),
							status.getOldLevel(), status.getNewLevel());
					status.clear();
					return;
				}
			}
			
			// 只弹出一次
            if (info.userInfo.province <= 0) { //地理位置
                showPositionTip();
                mComeinTipPriority = ComeinTipPriority.position;
            }
			
		} else {// 女性

			/**设置碰缘分状态*/
			if (mFragment != null && mFragment instanceof FragmentHome)
				((FragmentHome) mFragment).setYuanfenShortcutState(info.isOpenFate);
			FragmentYuanfen.mIsYuanfenOpen = info.isOpenFate;
			
			/**更新升级状态*/
			LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
			long uid = ManagerAccount.getInstance().getCurrentId();
			if (status.getUid() != uid) {
				status.clear();
			}

            if (!info.userInfo.hasPortrait) {
                //从未上传头像
                if (info.userInfo.portraitStatus == 0) {
                    showUploadAvatarTip(info.userInfo.portraitStatus, info.userInfo.portraitTips);
                    mComeinTipPriority = ComeinTipPriority.avatar;
                }
            } else if (info.userInfo.province <= 0) {
                // 上传地理位置
                showPositionTip();
                mComeinTipPriority = ComeinTipPriority.position;
            } else if (!info.userInfo.hasPortrait) {
                // 头像审核不通过
                if (info.userInfo.portraitStatus == 3) {
                    showUploadAvatarTip(info.userInfo.portraitStatus, info.userInfo.portraitTips);
                    mComeinTipPriority = ComeinTipPriority.avatar;
                }
            } else if (status.getType() == LevelChangeType.Female_Level_Up) {
                // 有头像,级别变化，弹升级弹层
                new ShareDialog().showLevel(mFragment, status.getType(), status.getOldLevel(),
                        status.getNewLevel());
                status.clear();
                mComeinTipPriority = ComeinTipPriority.levelchange;
            } else if (info.userInfo.photoCount == 0 
					&& info.userInfo.privatePhotoCount == 0
					&& isAfterDay(EgmPrefHelper.getTipUploadPicTime(mContext),3)) { // 没有照片并且3天内未进行过此类提示
				// 从未上传图片，提示上传图片
				showUploadPictureTip(false);
				mComeinTipPriority = ComeinTipPriority.uploadPicture;
			} else if (!info.isOpenFate 
					&&isAfterDay(EgmPrefHelper.getTipOpenYuanfenTime(mContext), 1)) {// 补充是否开启
				// 未开启碰缘分，提示开启碰缘分
				showYuanfenTip();
				mComeinTipPriority = ComeinTipPriority.yuanfen;
			} else if (!isSelfInfoInsufficient(info.userInfo)
					&& isAfterDay(EgmPrefHelper.getTipSelfInfoTime(mContext),10)) { 
				// 详细资料里已填写的少于或等于三项
				showSelfInfoTip(info.userInfo);
				mComeinTipPriority = ComeinTipPriority.selfinfo;
			} else if (isAfterDay(EgmPrefHelper.getUpdatePicTime(mContext), 30) 
					&& isAfterDay(EgmPrefHelper.getTipUpdatePicTime(mContext),10)) { 
				// 十天内未进行过此类提示并且最近一次更新照片在三十天前，提示更新照片库
				showUploadPictureTip(true);
				mComeinTipPriority = ComeinTipPriority.updatePicture;
			}
		} 
	}
	

	/** 推荐页实时更新标记，主要用于头像与升级、降级 */
	private static boolean flagUpdateProfileByRec;

	public void updateByRecommend(RecommendListInfo info) {
		
		if(! isUpdateByRecommend())
			return;
		
		if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female) {
			/** 更新升级状态 */
			LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
			long uid = ManagerAccount.getInstance().getCurrentId();
			if (status.getUid() != uid) {
				status.clear();
			}

			if (!info.hasPortrait) {
				// 没有头像,弹上传头像弹层
                if (info.portraitStatus == 3) {
                    showUploadAvatarTip(info.portraitStatus, info.portraitTips);
                    mComeinTipPriority = ComeinTipPriority.avatar;
                }

			} else if (status.getType() == LevelChangeType.Female_Level_Up) {
				// 有头像,级别变化，弹升级弹层
				new ShareDialog().showLevel(mFragment, status.getType(),
						status.getOldLevel(), status.getNewLevel());
				status.clear();
				mComeinTipPriority = ComeinTipPriority.levelchange;
			}
		}else {
			//男性用户实时仅保留升级、降级提示
			LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
			long uid = ManagerAccount.getInstance().getCurrentId();
			if (status.getUid() != uid) {
				status.clear();
			} else {
				if (status.getType() == LevelChangeType.Male_Level_Down
						|| status.getType() == LevelChangeType.Male_Level_Up_1) {
					new ShareDialog().showLevel(mFragment, status.getType(),
							status.getOldLevel(), status.getNewLevel());
					status.clear();
					return;
				}
			}
		}
	}
	/**
	 * @return 获取实时推荐弹层权限
	 */
	public boolean isUpdateByRecommend() {

		return flagUpdateProfileByRec;
	}

	/**
	 * 打开实时推荐弹层权限
	 */
	public void openUpdateByRecommend() {

		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

			@Override
			public void run() {
				flagUpdateProfileByRec = true;
			}
			// 延时3S再打开推荐
		}, 3000);

	}

	/**
	 * 关闭实时推荐弹层权限
	 */
	public void closeUpdateByRecommend() {
		flagUpdateProfileByRec = false;
	}
	}