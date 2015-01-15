package com.netease.engagement.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;

/**
 * 举报
 */
public class FragmentComplain extends FragmentBase{

	public static FragmentComplain newInstance(long postId){
		FragmentComplain fragment = new FragmentComplain();
		Bundle bundle = new Bundle();
		bundle.putLong(EgmConstants.BUNDLE_KEY.USER_ID, postId);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	private CustomActionBar mCustomActionBar ;
	
	private ScrollView scrollView;
	
	private View mAdvertisementLayout ;
	private View mPornLayout ;
	private View mInsultLayout ;
	private View mShamLayout ;
	private View mCheatLayout ;
	private View mPoliticalLayout ;
	private View mSexTradeLayout ;
	private View mComplainLayout ;
	
	
	private View mLastLayout ;
	
	
	
	private EditText mEditContent ;
	private TextView mTxtNumTip ;
	
	private LinearLayout mContentLayout;
	
	private static final int MAX_NUM = 2000 ;
	
	private int mType = -1;
	private long mPostId ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		mPostId = this.getArguments().getLong(EgmConstants.BUNDLE_KEY.USER_ID);
		EgmService.getInstance().addListener(mCallBack);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        
        mCustomActionBar.setLeftClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clickBack();
			}
		});
        
        mCustomActionBar.setMiddleTitle(R.string.jubao);
        mCustomActionBar.setMiddleTitleSize(20);
        
        mCustomActionBar.setRightAction(0,R.string.complain_commit);
        mCustomActionBar.setRightTitleSize(20);
        mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.info_more_data_txt_color));
        mCustomActionBar.setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEditContent.getText().toString().trim();
                if(mType != -1){
                	hideKeyboard();
                    EgmService.getInstance().doComplain(mType, content, mPostId);
                    showWatting("提交中...");
                }else{
                    ToastUtil.showToast(getActivity(),"请输入举报类型");
                }
            }
        });
	}
	
	/**
	 * 隐藏键盘
	 */
	private void hideKeyboard(){
		InputMethodManager mImm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		mImm.hideSoftInputFromWindow(mEditContent.getWindowToken(), 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		scrollView = (ScrollView) inflater.inflate(R.layout.fragment_complain_layout,container,false);
		init(scrollView);
		return scrollView;
	}
	
	private void init(View root){
		if(root == null){
			return ;
		}
		
		
		mAdvertisementLayout = (View)root.findViewById(R.id.advertisement);
		mPornLayout = (View)root.findViewById(R.id.porn);
		mInsultLayout = (View)root.findViewById(R.id.insult);
		mShamLayout = (View)root.findViewById(R.id.sham);
		mCheatLayout = (View)root.findViewById(R.id.cheat);
		mPoliticalLayout = (View)root.findViewById(R.id.political);
		mSexTradeLayout = (View)root.findViewById(R.id.sextrade);
		mComplainLayout = (View)root.findViewById(R.id.complain);
		
		((TextView)mAdvertisementLayout.findViewById(R.id.tv)).setText(R.string.complain_type_advertisement);
		((TextView)mPornLayout.findViewById(R.id.tv)).setText(R.string.complain_type_porn);
		((TextView)mInsultLayout.findViewById(R.id.tv)).setText(R.string.complain_type_insult);
		((TextView)mShamLayout.findViewById(R.id.tv)).setText(R.string.complain_type_sham);
		((TextView)mCheatLayout.findViewById(R.id.tv)).setText(R.string.complain_type_cheat);
		((TextView)mPoliticalLayout.findViewById(R.id.tv)).setText(R.string.complain_type_political);
		((TextView)mSexTradeLayout.findViewById(R.id.tv)).setText(R.string.complain_type_sextrade);
		((TextView)mComplainLayout.findViewById(R.id.tv)).setText(R.string.complain_type_complain);
		
		mAdvertisementLayout.setOnClickListener(mOnClickListener);
		mPornLayout.setOnClickListener(mOnClickListener);
		mInsultLayout.setOnClickListener(mOnClickListener);
		mShamLayout.setOnClickListener(mOnClickListener);
		mCheatLayout.setOnClickListener(mOnClickListener);
		mPoliticalLayout.setOnClickListener(mOnClickListener);
		mSexTradeLayout.setOnClickListener(mOnClickListener);
		mComplainLayout.setOnClickListener(mOnClickListener);
		
		mLastLayout = mComplainLayout ;
		mLastLayout.setSelected(true);
		
		mEditContent = (EditText)root.findViewById(R.id.content);
		mEditContent.addTextChangedListener(mTextWatcher);
		mEditContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NUM)});
		mEditContent.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					scrollView.postDelayed(new Runnable() {
						@Override
						public void run() {
							scrollView.fullScroll(ScrollView.FOCUS_DOWN);
						}
					}, 500);
				}
				return false;
			}
		});
		
		mTxtNumTip = (TextView)root.findViewById(R.id.num_tip);
		mTxtNumTip.setText(String.valueOf(0));
		
		mContentLayout = (LinearLayout)root.findViewById(R.id.content_layout);
	}
	
	private TextWatcher mTextWatcher = new TextWatcher(){
		@Override
		public void afterTextChanged(Editable s) {
			mTxtNumTip.setText(String.valueOf(s.length()));
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,int after) {
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count) {
		}
	};
	
	private OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			if (mLastLayout != null) {
				mLastLayout.findViewById(R.id.iv).setVisibility(View.INVISIBLE);
			}
			
			switch(v.getId()){
				case R.id.advertisement: // 商业广告
					mType = EgmConstants.Complain_Type.COMPLAIN_ADVERTISEMENT ;
					mLastLayout = mAdvertisementLayout ;
					break;
				case R.id.porn: // 淫秽图片（对应原来的色情交易）
					mType = EgmConstants.Complain_Type.COMPLAIN_PORN ;
					mLastLayout = mPornLayout ;
					break;
				case R.id.insult: // 文字语音不文明侮辱他人
					mType = EgmConstants.Complain_Type.COMPLAIN_INSULT ;
					mLastLayout = mInsultLayout ;
					break;
				case R.id.sham: // 资料虚假
					mType = EgmConstants.Complain_Type.COMPLAIN_SHAM ;
					mLastLayout = mShamLayout ;
					break;
				case R.id.cheat: // 诈骗（对应原来的诈骗钱财）
					mType = EgmConstants.Complain_Type.COMPLAIN_CHEAT ;
					mLastLayout = mCheatLayout ;
					break;
				case R.id.political: // 不当政治言论
					mType = EgmConstants.Complain_Type.COMPLAIN_POLITICAL ;
					mLastLayout = mPoliticalLayout ;
					break;
				case R.id.sextrade: // 性交易
					mType = EgmConstants.Complain_Type.COMPLAIN_SEXTRADE ;
					mLastLayout = mSexTradeLayout ;
					break;
				case R.id.complain: // 其他（对应原来的举报投诉）
					mType = EgmConstants.Complain_Type.COMPLAIN_COMPLAINT ;
					mLastLayout = mComplainLayout ;
					break;
			}
			
			mLastLayout.findViewById(R.id.iv).setVisibility(View.VISIBLE);
			
			if (mContentLayout.getVisibility() == View.INVISIBLE) {
				mContentLayout.setVisibility(View.VISIBLE);
				Animation alpha = AnimationUtils.loadAnimation(FragmentComplain.this.getActivity(), R.anim.alpha_0_to_1);
				mContentLayout.startAnimation(alpha);
			} else {
				hideKeyboard();
			}
		}
	};
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onComplainSucess(int transactionId, int code) {
			stopWaiting();
			ToastUtil.showToast(getActivity(), "提交成功");
			getActivity().finish();
		}

		@Override
		public void onComplainError(int transactionId, int errCode, String err) {
			stopWaiting();
			ToastUtil.showToast(getActivity(), err);
		}
	};
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
}
