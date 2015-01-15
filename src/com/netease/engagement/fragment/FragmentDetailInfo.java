package com.netease.engagement.fragment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.Formatter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.compat.LoadingListView;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityChoiceList;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.adapter.DetailInfoAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.view.EgmDatePicker;
import com.netease.engagement.view.EgmDatePickerDialog;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.framework.widget.DialogUtil;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmLocationManager;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;

/**
 * 详细资料列表
 */
public class FragmentDetailInfo extends FragmentBase{
	
	private CustomActionBar mCustomActionBar ;
	
	private LoadingListView mLoadingListView ;
	private DetailInfoAdapter mAdapter ;
	
	private UserInfo mUserInfo ;
	private UserInfoConfig mUserInfoConfig ;
	
	private HashMap<String,String> mInfoMap ;
	
	private LinearLayout mHeaderLayout ;
	private TextView mHeaderTagName ;
	private EditText mHeaderTagContent ;
	
	private Activity mActivity;
	private InputMethodManager mManager;
	
	public static FragmentDetailInfo newInstance(UserInfo userInfo){
		FragmentDetailInfo fragment = new FragmentDetailInfo();
		Bundle args = new Bundle();
		args.putParcelable(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
		fragment.setArguments(args);
		return fragment ;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = this.getArguments();
		mActivity = this.getActivity();
		mActivity.setTitle(getString(R.string.detail_info));
		if(args == null || args.getParcelable(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO) == null){
			return ;
		}
		mUserInfo = args.getParcelable(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO);
		
		mManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	private void initTitle(){
		
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
		mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_page_back_color));
		
		mCustomActionBar.setMiddleTitle(R.string.detail_info);
		mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
		mCustomActionBar.setMiddleTitleSize(20);
		
		mCustomActionBar.setRightAction(-1, R.string.rec_yuanfen_save);
		mCustomActionBar.setRightClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!TextUtils.isEmpty(mHeaderTagContent.getText().toString())){
					mInfoMap.put("nick",mHeaderTagContent.getText().toString());
					EgmService.getInstance().doModifyDetailInfo(mInfoMap);
					showWatting("修改中...");
				}else{
					ToastUtil.showToast(mActivity, R.string.nick_not_null);
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mLoadingListView = (LoadingListView) inflater.inflate(R.layout.view_loading_list,container,false);
		init();
		return mLoadingListView;
	}
	
	private void init(){
		
		LayoutInflater inflater = mActivity.getLayoutInflater();
		mHeaderLayout = (LinearLayout) inflater.inflate(R.layout.item_view_page_list_edit,null);
		mHeaderTagName = (TextView)mHeaderLayout.findViewById(R.id.tag_name);
		mHeaderTagContent = (EditText)mHeaderLayout.findViewById(R.id.tag_content);
		mHeaderTagName.setText(R.string.str_nick);
		mHeaderTagContent.setText(mUserInfo.nick);
		mHeaderTagContent.setSingleLine(true);
		
		
		mLoadingListView.disableLoadingMore();
		mLoadingListView.disablePullToRefresh();
		
		mLoadingListView.getRefreshableView().addHeaderView(mHeaderLayout);
		
		//修改详细资料参数列表
		List<String> mParamsList = Arrays.asList(mActivity.getResources()
				.getStringArray(R.array.main_page_detail_info_param));
		
		mInfoMap = new HashMap<String,String>();
		for(String item : mParamsList){
			mInfoMap.put(item, null);
		}
		
		mUserInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
		if(mUserInfoConfig == null){
			return ;
		}
		
		mAdapter = new DetailInfoAdapter(mActivity,mUserInfo,mUserInfoConfig);
		
		mLoadingListView.getRefreshableView().setAdapter(mAdapter);
		mLoadingListView.getRefreshableView().setOnScrollListener(new OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    hideIme();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});
		
		mLoadingListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				String tagName = ((TextView)arg1.findViewById(R.id.tag_name)).getText().toString();
				if(getString(R.string.favor_date).equals(tagName)){
					/**
					 * 喜欢的约会
					 */
					ActivityChoiceList.lunch(FragmentDetailInfo.this, ActivityChoiceList.FAVOR_DATE, mUserInfo);
				}else if(getString(R.string.interest_hobby).equals(tagName)){
					/**
					 * 兴趣爱好
					 */
					ActivityChoiceList.lunch(FragmentDetailInfo.this, ActivityChoiceList.INTEREST, mUserInfo);
				}else if(getString(R.string.wanna_skill).equals(tagName) || getString(R.string.adept_skill).equals(tagName)){
					/**
					 * 技能
					 */
					ActivityChoiceList.lunch(FragmentDetailInfo.this, ActivityChoiceList.SKILL, mUserInfo);
				}else if(getString(R.string.tag_height).equals(tagName)){
					/**
					 * 身高
					 */
					if (mDialog == null) {
						showDialog(R.string.tag_height);
					}
				}else if(getString(R.string.tag_weight).equals(tagName)){
					/**
					 * 体重
					 */
					if (mDialog == null) {
						showDialog(R.string.tag_weight);
					}
				}else if(getString(R.string.favar_part).equals(tagName)){
					/**
					 * 最满意的部位
					 */
					if (mDialog == null) {
						showDialog(R.string.favar_part);
					}
				}else if(getString(R.string.tag_income).equals(tagName)){
					/**
					 * 收入
					 */
					if (mDialog == null) {
						showDialog(R.string.tag_income);
					}
				}else if(getString(R.string.tag_figure).equals(tagName)){
					/**
					 * 身材
					 */
					if (mDialog == null) {
						showDialog();
					}
				}else if(getString(R.string.colletation).equals(tagName)){
					/**
					 * 星座
					 */
					if (mDialog == null) {
						showDialog(R.string.colletation);
					}
				} else if(getString(R.string.birthday).equals(tagName)){
					// 生日
					if (mDialog == null && mUserInfo.modifyBirthday) {
						showDatePicker();
					}
				} else if(getString(R.string.tag_id).equals(tagName) || getString(R.string.tag_location).equals(tagName)){
					hideIme();
				}
			}
		});
	}
	
	private AlertDialog mDialog = null;
	private OnDismissListener mOnDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			mDialog = null;
		}
	};

	private void showDatePicker() {
		Calendar birthday = Calendar.getInstance();
		birthday.setTimeInMillis(mUserInfo.birthday);
		int birthYear = birthday.get(Calendar.YEAR);
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		int maxYear = curYear - 18;
		maxYear = maxYear < birthYear ? birthYear : maxYear;
		int minYear = curYear - 60;
		minYear = minYear > birthYear ? birthYear : minYear;
		EgmDatePickerDialog dateDialog = new EgmDatePickerDialog(mActivity, mDateSetListener, birthday.get(Calendar.YEAR), birthday.get(Calendar.MONTH) + 1,
				birthday.get(Calendar.DATE), maxYear, minYear);
		dateDialog.setCanceledOnTouchOutside(false);
		dateDialog.setTitle(R.string.reg_title_birthday);
		dateDialog.setOnDismissListener(mOnDismissListener);
		mDialog = dateDialog;
		dateDialog.showDialog();
	}

	private EgmDatePickerDialog.OnDateSetListener mDateSetListener = new EgmDatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(EgmDatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Calendar birthday = Calendar.getInstance();
			birthday.set(year, monthOfYear - 1, dayOfMonth);
			mUserInfo.birthday = birthday.getTimeInMillis();
			mInfoMap.put("birthday", String.valueOf(mUserInfo.birthday));
			mAdapter.notify(mUserInfo);
		}
	};
	
	/** 隐藏软键盘 */
    private void hideIme(){
        if(mManager != null && mActivity.getCurrentFocus() != null && mActivity.getCurrentFocus().getWindowToken() != null){  
            mManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }  
    }
	
	/**
	 * 修改多项数据
	 */
	private void showDialog(){
		View view = mActivity.getLayoutInflater().inflate(R.layout.view_choose_figure, null);
		final NumberPicker pickerCup = (NumberPicker) view.findViewById(R.id.picker_cup);
		TextView title = (TextView)view.findViewById(R.id.title);
		title.setText(R.string.tag_figure);
		TextView cancel = (TextView)view.findViewById(R.id.cancel);
		TextView confirm = (TextView)view.findViewById(R.id.ok);
		
		final String[] cups = UserInfoUtil.getCups(mUserInfoConfig);
		pickerCup.setMinValue(1);
		pickerCup.setMaxValue(cups.length);
		pickerCup.setFormatter(new Formatter() {
			@Override
			public String format(int value) {
				return cups[value - 1];
			}
		});
		pickerCup.setValue(mUserInfo.cup == 0 ? 2 : mUserInfo.cup);
		ImageView cup_up = (ImageView)view.findViewById(R.id.cup_up);
		ImageView cup_down = (ImageView)view.findViewById(R.id.cup_down);
		cup_up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerCup.setValue(pickerCup.getValue() -1);
			}
		});
		cup_down.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerCup.setValue(pickerCup.getValue() + 1);
			}
		});
		
		final NumberPicker pickerBreast = (NumberPicker) view.findViewById(R.id.picker_breast);
		pickerBreast.setMinValue(EgmConstants.Waist_Range.MIN);
		pickerBreast.setMaxValue(EgmConstants.Waist_Range.MAX);
		pickerBreast.setValue(mUserInfo.bust == 0 ? 90 : mUserInfo.bust);
		ImageView bust_up = (ImageView)view.findViewById(R.id.bust_up);
		ImageView bust_down = (ImageView)view.findViewById(R.id.bust_down);
		bust_up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerBreast.setValue(pickerBreast.getValue() -1);
			}
		});
		bust_down.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerBreast.setValue(pickerBreast.getValue() + 1);
			}
		});
		
		final NumberPicker pickerWaist = (NumberPicker) view.findViewById(R.id.picker_waist);
		pickerWaist.setMinValue(EgmConstants.Waist_Range.MIN);
		pickerWaist.setMaxValue(EgmConstants.Waist_Range.MAX);
		pickerWaist.setValue(mUserInfo.waist == 0 ? 70 : mUserInfo.waist);
		ImageView waist_up = (ImageView)view.findViewById(R.id.waist_up);
		ImageView waist_down = (ImageView)view.findViewById(R.id.waist_down);
		waist_up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerWaist.setValue(pickerWaist.getValue() -1);
			}
		});
		waist_down.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerWaist.setValue(pickerWaist.getValue() + 1);
			}
		});
		
		final NumberPicker pickerHip = (NumberPicker) view.findViewById(R.id.picker_hip);
		pickerHip.setMinValue(EgmConstants.Waist_Range.MIN);
		pickerHip.setMaxValue(EgmConstants.Waist_Range.MAX);
		pickerHip.setValue(mUserInfo.hip == 0 ? 90 : mUserInfo.hip);
		ImageView hip_up = (ImageView)view.findViewById(R.id.hip_up);
		ImageView hip_down = (ImageView)view.findViewById(R.id.hip_down);
		hip_up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerHip.setValue(pickerHip.getValue() - 1);
			}
		});
		hip_down.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerHip.setValue(pickerHip.getValue() + 1);
			}
		});
		
		final AlertDialog dialog = DialogUtil.newAlertDiag(mActivity)
		.setView(view)
		.create();
		dialog.setOnDismissListener(mOnDismissListener);
		mDialog = dialog;
		dialog.show();
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int cup = pickerCup.getValue() ;
				int bust = pickerBreast.getValue();
				int waist = pickerWaist.getValue();
				int hip = pickerHip.getValue();
				mUserInfo.cup = cup ;
				mInfoMap.put("cup",String.valueOf(cup));
				mUserInfo.bust = bust ;
				mInfoMap.put("bust",String.valueOf(bust));
				mUserInfo.waist = waist ;
				mInfoMap.put("waist",String.valueOf(waist));
				mUserInfo.hip = hip ;
				mInfoMap.put("hip",String.valueOf(hip));
				mAdapter.notify(mUserInfo);
				dialog.dismiss();
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 修改单项数据
	 * @param id
	 */
	private void showDialog(final int id){
		View view = null ;
		if(id == R.string.tag_income){
			view = mActivity.getLayoutInflater().inflate(R.layout.view_choose_detail_info_income,null);
		}else{
			view = mActivity.getLayoutInflater().inflate(R.layout.view_choose_detail_info, null);
		}
		final NumberPicker pickerView = (NumberPicker) view.findViewById(R.id.picker);
		TextView title = (TextView)view.findViewById(R.id.title);
		title.setText(getString(id));
		TextView cancel = (TextView)view.findViewById(R.id.cancel);
		TextView confirm = (TextView)view.findViewById(R.id.ok);
		ImageView up = (ImageView)view.findViewById(R.id.up);
		ImageView down = (ImageView)view.findViewById(R.id.down);
		
		switch(id){
			case R.string.tag_height:
				/**
				 * 设置身高
				 */
				pickerView.setMinValue(EgmConstants.Height_Range.MIN);
				pickerView.setMaxValue(EgmConstants.Height_Range.MAX);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return String.format(getString(R.string.height_unit), value);
					}
				});
				if(mUserInfo.height != 0){
					pickerView.setValue(mUserInfo.height);
				}else{
					pickerView.setValue(EgmConstants.Height_Range.DEFAULT);
				}
				break;
			case R.string.tag_weight:
				/**
				 * 设置体重
				 */
				pickerView.setMinValue(EgmConstants.Weight_Range.MIN);
				pickerView.setMaxValue(EgmConstants.Weight_Range.MAX);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return String.format(getString(R.string.weight_unit),value);
					}
				});
				if(mUserInfo.weight != 0){
					pickerView.setValue(mUserInfo.weight);
				}else{
					pickerView.setValue(EgmConstants.Weight_Range.DEFAULT);
				}
				break;
			case R.string.favar_part:
				/**
				 * 最喜欢的部位
				 */
				final String[] favarParts = UserInfoUtil.getFavorPart(mUserInfoConfig);
				pickerView.setMinValue(1);
				pickerView.setMaxValue(favarParts.length);
				pickerView.setValue(mUserInfo.satisfiedPart == 0 ? 1 : mUserInfo.satisfiedPart);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return favarParts[value - 1];
					}
				});
				break;
			case R.string.tag_income:
				/**
				 * 收入
				 */
				final String[] incomes = UserInfoUtil.getIncomes(mUserInfoConfig);
				pickerView.setMinValue(1);
				pickerView.setMaxValue(incomes.length);
				pickerView.setValue(mUserInfo.income == 0 ? 1 : mUserInfo.income);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return incomes[value - 1];
					}
				});
				break;
			case R.string.colletation:
				/**
				 * 星座
				 */
				final String[] colletations = UserInfoUtil.getColletations(mUserInfoConfig);
				pickerView.setMinValue(1);
				pickerView.setMaxValue(colletations.length);
				pickerView.setValue(mUserInfo.constellation == 0 ? 1 : mUserInfo.constellation);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return colletations[value - 1];
					}
				});
				break;
				
		}
		
		final AlertDialog dialog = DialogUtil.newAlertDiag(mActivity)
		.setView(view).create();
		dialog.setOnDismissListener(mOnDismissListener);
		mDialog = dialog;
		dialog.show();
		
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		confirm.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				int result = pickerView.getValue();
				switch(id){
					case R.string.tag_height:
						if(result != mUserInfo.height){
							mUserInfo.height = result ;
							mInfoMap.put("height", String.valueOf(result));
							mAdapter.notify(mUserInfo);
						}
						break;
					case R.string.tag_weight:
						if(result != mUserInfo.weight){
							mUserInfo.weight = result ;
							mInfoMap.put("weight", String.valueOf(result));
							mAdapter.notify(mUserInfo);
						}
						break;
					case R.string.favar_part:
						mUserInfo.satisfiedPart = result;
						mInfoMap.put("satisfiedPart", String.valueOf(result));
						mAdapter.notify(mUserInfo);
						break;
					case R.string.tag_income:
						mUserInfo.income = result ;
						mInfoMap.put("income",String.valueOf(result));
						mAdapter.notify(mUserInfo);
						break;
					case R.string.colletation:
						mUserInfo.constellation = result ;
						mInfoMap.put("Constellation",String.valueOf(result));
						mAdapter.notify(mUserInfo);
						break;
				}
				dialog.dismiss();
			}
		});
		up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerView.setValue(pickerView.getValue() -1);
			}
		});
		down.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerView.setValue(pickerView.getValue() + 1);
			}
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initTitle();
		EgmService.getInstance().addListener(mCallBck);
	}
	
	private EgmCallBack mCallBck = new EgmCallBack(){
		@Override
		public void onModifyDetailInfoSucess(int transactionId, UserInfo obj) {
			if(obj != null){
				stopWaiting();
				ToastUtil.showToast(mActivity, R.string.modify_info_suc);
				mActivity.setResult(Activity.RESULT_OK);
				mActivity.finish();
			}
		}
		@Override
		public void onModifyDetailInfoError(int transactionId, int errCode,String err) {
			stopWaiting();
			ToastUtil.showToast(mActivity,err);
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK){
			if(data == null){
				return ;
			}
			int[] ids = data.getIntArrayExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_DATA);
			StringBuilder sb = new StringBuilder();
			String idStr = null ;
			if(ids != null && ids.length > 0){
				for(int i : ids){
					sb.append(i).append(",");
				}
				idStr = sb.substring(0,sb.length() -1);
				sb = null ;
			}else{
				idStr = "-1" ;
			}
			switch(requestCode){
				case ActivityChoiceList.FAVOR_DATE:
					mUserInfo.favorDate = ids ;
					mInfoMap.put("favorDate", idStr);
					break;
				case ActivityChoiceList.INTEREST:
					mUserInfo.hobby = ids ;
					mInfoMap.put("hobby", idStr);
					break;
				case ActivityChoiceList.SKILL:
					mUserInfo.skill = ids ;
					mInfoMap.put("skill", idStr);
					break;
			}
			mAdapter.notify(mUserInfo);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBck);
	}
}
