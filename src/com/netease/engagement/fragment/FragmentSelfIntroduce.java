package com.netease.engagement.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.util.SoftInputUtil;

/**
 * 自我介绍
 */
public class FragmentSelfIntroduce extends FragmentBase{
	
	private String mContent ;
	private EditText mEditContent ;
	private TextView mText_Tip ;
	
	private CustomActionBar mCustomActionBar ;
	
	private InputMethodManager mManager;
	
	public static FragmentSelfIntroduce newInstance(String content){
		FragmentSelfIntroduce fragment = new FragmentSelfIntroduce();
		Bundle args = new Bundle();
		args.putString(EgmConstants.BUNDLE_KEY.SELF_PAGE_CONTENT, content);
		fragment.setArguments(args);
		return fragment ;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		mContent = args.getString(EgmConstants.BUNDLE_KEY.SELF_PAGE_CONTENT);
		
		mManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_self_intro,container,false);
		init(root);
		return root;
	}
	
	private void init(View root){
		getActivity().setTitle(R.string.self_introduce);
		mText_Tip = (TextView)root.findViewById(R.id.txt_num_tip);
		mEditContent = (EditText)root.findViewById(R.id.self_intr_edit);
		
		mEditContent.setText(mContent);
		mEditContent.setSelection(mEditContent.getText().length());
		
		mText_Tip.setText(String.format(getString(R.string.txt_num_tip),mEditContent.getText().length()));
		mEditContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EgmConstants.SELF_INTRODUCE_TEXT_MAX_LENGTH)});
		mEditContent.addTextChangedListener(mTextWatcher);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        mCustomActionBar.setMiddleTitle(R.string.self_introduce);
        mCustomActionBar.setMiddleTitleSize(20);
        
        mCustomActionBar.setRightAction(-1, R.string.rec_yuanfen_save);
        mCustomActionBar.setRightClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
            	if(mManager != null){  
                    mManager.hideSoftInputFromWindow(mEditContent.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                EgmService.getInstance().doModifyIntroduce(mEditContent.getText().toString());
            }
        });
		EgmService.getInstance().addListener(mCallBack);
		
		SoftInputUtil.showInputDelayed(mManager, mEditContent);
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){

		@Override
		public void onModifyIntrSucess(int transactionId, int code) {
			Toast.makeText(getActivity(),"修改自我介绍成功",Toast.LENGTH_SHORT).show();
			getActivity().setResult(Activity.RESULT_OK);
			getActivity().finish();
		}

		@Override
		public void onModifyIntroError(int transactionId, int errCode,
				String err) {
			ToastUtil.showToast(getActivity(),err);
		}
	};

	private TextWatcher mTextWatcher = new TextWatcher(){
		@Override
		public void afterTextChanged(Editable s) {
			mText_Tip.setText(String.format(getString(R.string.txt_num_tip), s.length()));
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,int after) {
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count) {
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
}
