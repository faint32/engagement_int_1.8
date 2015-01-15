package com.netease.engagement.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
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
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;



public class FragmentFeedBack extends FragmentBase {
    
    private EditText mEditContent ;
    private TextView mText_Tip ;
    
    private CustomActionBar mCustomActionBar ;
    
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_a_selector);
        mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.purple_dark));
        mCustomActionBar.setLeftAction(R.drawable.bar_btn_back_a, R.string.back);
        mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        mCustomActionBar.setMiddleTitle(R.string.setting_fb_title);
        mCustomActionBar.setMiddleTitleSize(20);
        
        mCustomActionBar.setRightBackgroundResource(R.drawable.titlebar_a_selector);
        mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.purple_dark));
        mCustomActionBar.setRightAction(-1, R.string.rec_yuanfen_save);
        mCustomActionBar.setRightClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //上传反馈
                if(!TextUtils.isEmpty(mEditContent.getText().toString().trim())){
                    showWatting(getResources().getString(R.string.req_waiting));
                    EgmService.getInstance().doFeedBack(mEditContent.getText().toString().trim());
                } else {
                    showToast(R.string.setting_fb_no_content);
                }
            }
        });
        mCustomActionBar.setLeftClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                InputMethodManager im = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (im.isActive() && getActivity().getCurrentFocus() != null) {
                    im.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
        
        EgmService.getInstance().addListener(mCallBack);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_feedback,container,false);
        init(root);
        return root;
    }
    
    private void init(View root){
        mText_Tip = (TextView)root.findViewById(R.id.txt_num_tip);
        mEditContent = (EditText)root.findViewById(R.id.self_intr_edit);
        mText_Tip.setText(String.format(getString(R.string.txt_num_tip),mEditContent.getText().length()));
        mEditContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EgmConstants.SELF_INTRODUCE_TEXT_MAX_LENGTH)});
        mEditContent.addTextChangedListener(mTextWatcher);
    }
    
    private EgmCallBack mCallBack = new EgmCallBack(){
        
        @Override
        public void onFeedBack(int transactionId) {
            stopWaiting();
            showToast(R.string.setting_fb_sucess);
            InputMethodManager im = (InputMethodManager)getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (im.isActive() && getActivity().getCurrentFocus() != null) {
                im.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
            getFragmentManager().popBackStack();
        };
        @Override
        public void onFeedBackError(int transactionId, int errCode, String err) {
            stopWaiting();
            showToast(err);
        };
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
