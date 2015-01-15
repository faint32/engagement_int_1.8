package com.netease.engagement.fragment;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityMoneyAccount;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.ShareDialog;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ApplyWithdrawError;
import com.netease.service.protocol.meta.MoneyAccountInfo;

/**
 * 个人中心——账户
 * @version 1.0
 */
public class FragmentMoneyAccount extends FragmentBase implements OnClickListener {
    private ActivityEngagementBase mActivity;
    private FragmentManager mFragmentManager;
    
    private View mLayout;
    /** 当前现金值 */
    private TextView mCurrentCashTv;
    /** 历史记录 */
    private TextView mHistoryTv;
    /** 过期时间 */
    private TextView mExpireDayTv;
    /** 申请状态 */
    private TextView mApplyStatusTv;
    /** 累计魅力值 */
    private TextView mAccumulateCharmTv;
    /** 累计现金值 */
    private TextView mAccumulateCashTv;
    /** 查看余额 */
    private View mCheckBalance;
    /** 申请提现 */
    private TextView mCashApply;
    private CheckBox mCheckBox;
    
    private int mGetInfoTid;
    private int mWithdrawTid;
    private boolean mHasData = false;
    private boolean mIsFromShortcut = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mActivity = (ActivityEngagementBase)this.getActivity();
        Bundle argument = this.getArguments();
        if(argument != null){
            mIsFromShortcut = argument.getBoolean(ActivityMoneyAccount.EXTRA_IS_SHORTCUT, false);
        }
        
        EgmService.getInstance().addListener(mEgmCallback);
        mFragmentManager = getFragmentManager();
        
        doGetAccountInfo();
    }
    
    @Override
    public void onResume(){
        super.onResume();
        
        CustomActionBar actionBar = mActivity.getCustomActionBar();
        actionBar.setLeftVisibility(View.INVISIBLE);
        actionBar.setMiddleTitle(R.string.account);
        
        actionBar.setRightVisibility(View.VISIBLE);
        actionBar.setRightAction(-1, R.string.close);
        actionBar.setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBack();
//                if(mIsFromShortcut){
//                    mActivity.overridePendingTransition(0, R.anim.push_down_out);
//                }
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mLayout != null){
            ViewGroup parent = (ViewGroup) mLayout.getParent();  
            if (parent != null) {  
                parent.removeView(mLayout);  
            }   
            
            return mLayout;
        }
        
        mLayout = inflater.inflate(R.layout.fragment_money_account_layout, container, false);
        
        mCurrentCashTv = (TextView)mLayout.findViewById(R.id.money_current_cash);
        mHistoryTv = (TextView)mLayout.findViewById(R.id.money_history);
        mExpireDayTv = (TextView)mLayout.findViewById(R.id.money_expire_day);
        mApplyStatusTv = (TextView)mLayout.findViewById(R.id.money_apply_status);
        mAccumulateCharmTv = (TextView)mLayout.findViewById(R.id.money_accumulate_charm);
        mAccumulateCashTv = (TextView)mLayout.findViewById(R.id.money_accumulate_cash);
        mCheckBalance = mLayout.findViewById(R.id.money_check_balance);
        mCashApply = (TextView)mLayout.findViewById(R.id.money_cash_apply);
        mCheckBox = (CheckBox)mLayout.findViewById(R.id.money_accept_license);
        
        // 查看条款
        mLayout.findViewById(R.id.money_check_license).setOnClickListener(this);
     
        // 历史记录
        mHistoryTv.setOnClickListener(this);
        
        // 申请提现
        mCashApply.setOnClickListener(this);
        
        // 查询网易宝余额
        mCheckBalance.setOnClickListener(this);
        
        mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCashApply.setEnabled(isChecked);
            }
        });
        
        return mLayout;
    }
    
    @Override
    public void onClick(View view) {
    	switch (view.getId()) {
		case R.id.money_cash_apply:
			if(mHasData){
                doWithdraw();
            }
            else{
                showToast(R.string.net_error);
            }
			break;
			
		case R.id.money_history:
			gotoHistory();
			break;
			
		case R.id.money_check_license:
			gotoCheckLicence();
			break;
			
		case R.id.money_check_balance:
			gotoBalance();
			break;
		}
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        EgmService.getInstance().removeListener(mEgmCallback);
    }
    
    /** 填充界面数据 */
    private void fillUIData(MoneyAccountInfo info){
        if(info == null){
            String empty = "- -";
            mCurrentCashTv.setText(empty);
            mExpireDayTv.setVisibility(View.INVISIBLE);
            mApplyStatusTv.setVisibility(View.INVISIBLE);
            mAccumulateCharmTv.setText(empty);
            mAccumulateCashTv.setText(empty);
            mCashApply.setEnabled(false);   // 不可提现
            
            mHistoryTv.setVisibility(View.INVISIBLE);   // 没有数据时不显示历史记录入口
        }
        else{
            mCurrentCashTv.setText(String.valueOf(info.money));
            mAccumulateCharmTv.setText(String.valueOf(info.usercp));
            mAccumulateCashTv.setText(String.valueOf(info.totalMoney));
            
            if(info.totalMoney < 0.1){  // 没有历史记录
                mHistoryTv.setVisibility(View.INVISIBLE);
            }
            else{
                mHistoryTv.setVisibility(View.VISIBLE);
            }
            
            if(info.money <= 0){
                mExpireDayTv.setVisibility(View.INVISIBLE);
                mCashApply.setEnabled(false);
            }
            else{
                mExpireDayTv.setVisibility(View.VISIBLE);
                mExpireDayTv.setText(mActivity.getString(R.string.account_money_expire_date) + TimeFormatUtil.forYMD(info.expireTime));
                mCashApply.setEnabled(mCheckBox.isChecked());
            }
            
            if(TextUtils.isEmpty(info.isApplying)){
                mApplyStatusTv.setVisibility(View.INVISIBLE);
            }
            else{
                mApplyStatusTv.setVisibility(View.VISIBLE);
                mApplyStatusTv.setText(info.isApplying);
            }
        }
    }
    
    private void doGetAccountInfo(){
        showWatting(mActivity.getString(R.string.common_tip_is_waitting));
        mGetInfoTid = EgmService.getInstance().doGetMoneyAccount();
    }
    
    private void doWithdraw(){
        showWatting(mActivity.getString(R.string.common_tip_is_waitting));
        mWithdrawTid = EgmService.getInstance().doApplyWithdraw();
    }
    
    /** 查看历史记录 */
    private void gotoHistory(){
        FragmentMoneyHistory frag = new FragmentMoneyHistory();
        
        mFragmentManager.beginTransaction()
            .replace(ActivityMoneyAccount.CONTAINER_ID, frag)
            .addToBackStack(null)
            .commit();
    }
    
    private void gotoCheckLicence(){
        FragmentWeb frag = new FragmentWeb();
        frag.setUrl(EgmConstants.URL_CASH_TERMS, false, false);
        
        mFragmentManager.beginTransaction()
            .replace(ActivityMoneyAccount.CONTAINER_ID, frag)
            .addToBackStack(null)
            .commit();
    }
    
    /**
     * 前往查看余额
     * @param isLogin 是否登录网易宝（如果成功获取到ticket则可以登录）
     */
    private void gotoBalance(){
//        if(mUserInfoConfig == null)
//            return;
        
//        FragmentWeb frag = new FragmentWeb();
//        frag.setUrl(mUserInfoConfig.epayUrl, true);//"https://epay.163.com/servlet/controller?operation=main", true);
//        
//        this.getFragmentManager().beginTransaction()
//            .replace(ActivityMoneyAccount.CONTAINER_ID, frag)
//            .addToBackStack(null)
//            .commit();
        ActivityWeb.startCheckBalance(mActivity);
    }
    
    /** 前往验证身份证 */
    private void gotoIdentityAuth(int code, ApplyWithdrawError info){
        FragmentIdentityAuth frag = new FragmentIdentityAuth();
        frag.setErrorInfo(code, info);
        frag.setAuthCallback(new IAuth(){
            @Override
            public void onAuthSuccess() {
                doWithdraw();
            }

            @Override
            public void onAuthError() {}
        });
        
        mFragmentManager.beginTransaction()
            .replace(ActivityMoneyAccount.CONTAINER_ID, frag)
            .addToBackStack(null)
            .commit();
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        /** 个人中心女性兑现帐户 */
        @Override
        public void onGetMoneyAccount(int transactionId, MoneyAccountInfo info){
            if(mGetInfoTid != transactionId)
                return;
            
            mHasData = true;
            stopWaiting();
            fillUIData(info);
            
            if(!TextUtils.isEmpty(info.lastMonth) && !TextUtils.isEmpty(info.lastMoney)){
            	Calendar calendar = Calendar.getInstance();
            	calendar.add(Calendar.MONTH, -1);
            	String date = DateFormat.format("yyyyMM", calendar.getTime()).toString();
            	if (!info.lastMonth.equalsIgnoreCase(date)) {
            		return;
            	}
            	long myId = ManagerAccount.getInstance().getCurrentId();
            	if (!EgmPrefHelper.getUserMoneyShareFlag(FragmentMoneyAccount.this.getActivity().getApplicationContext(), myId, info.lastMoney)) {
            		boolean b = new ShareDialog().showMoney(FragmentMoneyAccount.this, info.lastMoney);
            		if (b) {
            			EgmPrefHelper.putUserMoneyShareFlag(FragmentMoneyAccount.this.getActivity().getApplicationContext(), myId, info.lastMoney);
            		}
            	}
            }
          }
        @Override
        public void onGetMoneyAccountError(int transactionId, int errCode, String err){
            if(mGetInfoTid != transactionId)
                return;
            
            mHasData = false;
            stopWaiting();
            showToast(R.string.account_money_data_error);
            fillUIData(null);
        }
        
        /** 个人中心女性申请提现 */
        @Override
        public void onApplyWithdraw(int transactionId){
            if(mWithdrawTid != transactionId)
                return;
            
            stopWaiting();
            mApplyStatusTv.setVisibility(View.VISIBLE);
            mApplyStatusTv.setText(R.string.account_money_withdrawing);
            mCashApply.setText(R.string.account_money_cash_apply);
            
            
            // 申请提现成功，只是申请成功，提现还需审核，不是当场把钱打入账户，所以界面上的数值还是没有变化
            showToast(R.string.account_money_withdraw_success);
        }
        @Override
		public void onApplyWithdrawError(int transactionId,  int errCode,
				String err, ApplyWithdrawError info){
            if(mWithdrawTid != transactionId)
                return;
            stopWaiting();
            
            switch(errCode){
//                case EgmServiceCode.TRANSACTION_WITHDRAW_LEVEAL_TOO_LOW:    // 用户等级不够，不能提现
//                    showToast(R.string.account_money_level_low_error);
//                    break;
//                case EgmServiceCode.TRANSACTION_WITHDRAW_MISS_DATE:         // 错过申请提交日期
//                    showToast(R.string.account_money_miss_date_error);
//                    break;
//                case EgmServiceCode.TRANSACTION_WITHDRAW_NOT_CASH_INCOME:   // 用户还没有现金收入
//                    showToast(R.string.account_money_no_income_error);
//                    break;
                case EgmServiceCode.TRANSACTION_WITHDRAW_NEED_IDENTITY_AUTH:    // 需要上传身份证资料
                case EgmServiceCode.TRANSACTION_WITHDRAW_IDENTITY_VIDEO_ERROR:    // 身份信息已通过审核，请完成视频验证
                    gotoIdentityAuth(errCode, info);
                    break;
                default:
                    showToast(err);
                    break;
            }
        }
    };
    
    /** 验证身份证接口 */
    public interface IAuth{
        public void onAuthSuccess();
        public void onAuthError();
    }
}
