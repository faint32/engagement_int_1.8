package com.netease.engagement.fragment;

import net.simonvt.numberpicker.NumberPicker;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.SlideSwitch;
import com.netease.engagement.widget.SlideSwitch.OnChangedListener;
import com.netease.service.preferMgr.EgmPrefHelper;


public class FragmentSettingMsg extends FragmentBase {
    private LinearLayout mAllSubLay;
    private LinearLayout mTimeLay;
    private View mSetPeriod;
    private AlertDialog mAgeDialog;
    private TextView mHour;
    private int mStartHour = 23, mEndHour = 9;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CustomActionBar customActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftAction(R.string.back);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        customActionBar.setMiddleTitle(R.string.setting_msg);
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
        customActionBar.setLeftClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_set_pushmsg,container,false);
        initViews(root);
        return root;
    }
    private void initViews(View view) {
        mAllSubLay = (LinearLayout)view.findViewById(R.id.set_all_sub_lay);
        mTimeLay = (LinearLayout)view.findViewById(R.id.set_time_lay);
        if(EgmPrefHelper.getPushOn(getActivity())){
            mAllSubLay.setVisibility(View.VISIBLE);
        } else{
            mAllSubLay.setVisibility(View.GONE); 
        }
        if(EgmPrefHelper.getNoDisturbingOn(getActivity())){
            mTimeLay.setVisibility(View.VISIBLE);
        } else{
            mTimeLay.setVisibility(View.GONE); 
        }
        
        SlideSwitch sw = (SlideSwitch)view.findViewById(R.id.switch_msg);
        sw.setCheck(EgmPrefHelper.getPushOn(getActivity()));
        sw.setOnChangedListener(new OnChangedListener() {
            @Override
            public void OnChanged(View v, boolean checkState) {
                if(checkState){
                    mAllSubLay.setVisibility(View.VISIBLE);
                } else{
                    mAllSubLay.setVisibility(View.GONE); 
                }
                EgmPrefHelper.putPushOn(getActivity(), checkState);
                
            }
        });
        
        sw = (SlideSwitch)view.findViewById(R.id.switch_sound);
        sw.setCheck(EgmPrefHelper.getSoundOn(getActivity()));
        sw.setOnChangedListener(new OnChangedListener() {
            
            @Override
            public void OnChanged(View v, boolean checkState) {
                EgmPrefHelper.putSoundOn(getActivity(), checkState);
            }
        });
        
        sw = (SlideSwitch)view.findViewById(R.id.switch_vibrate);
        sw.setCheck(EgmPrefHelper.getShockOn(getActivity()));
        sw.setOnChangedListener(new OnChangedListener() {
            
            @Override
            public void OnChanged(View v, boolean checkState) {
                EgmPrefHelper.putShockOn(getActivity(), checkState);
            }
        });
        
        sw = (SlideSwitch)view.findViewById(R.id.switch_no_disturbing);
        sw.setCheck(EgmPrefHelper.getNoDisturbingOn(getActivity()));
        sw.setOnChangedListener(new OnChangedListener() {
            
            @Override
            public void OnChanged(View v, boolean checkState) {
                if(checkState){
                    mTimeLay.setVisibility(View.VISIBLE);
                } else{
                    mTimeLay.setVisibility(View.GONE); 
                }
                EgmPrefHelper.putNoDisturbingOn(getActivity(), checkState);
            }
        }); 
        
        mSetPeriod = view.findViewById(R.id.set_period);
        mSetPeriod.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showTimeDialog();
            }
        });
        
        mHour = (TextView)view.findViewById(R.id.set_period_text);
        mHour.setText(getString(R.string.msg_set_period, EgmPrefHelper.getNoDisturbingStart(getActivity()),EgmPrefHelper.getNoDisturbingEnd(getActivity())));
        
        sw = (SlideSwitch)view.findViewById(R.id.switch_audio_play_mode);
        sw.setCheck(EgmPrefHelper.getReceiverModeOn(getActivity()));
        sw.setOnChangedListener(new OnChangedListener() {
            
            @Override
            public void OnChanged(View v, boolean checkState) {
                EgmPrefHelper.putReceiverModeOn(getActivity(), checkState);
            }
        });
    }
    private void showTimeDialog(){
        if(mAgeDialog == null){
            mAgeDialog = new AlertDialog.Builder(getActivity()).create();
            View layout = LayoutInflater.from(getActivity()).inflate(R.layout.view_age_picker_dialog, null, false);
            
            TextView title = (TextView)layout.findViewById(R.id.date_picker_title_text);
            title.setText(R.string.msg_set_period_title);
            
            final NumberPicker mLowPicker, mHeighPicker;
            mLowPicker = (NumberPicker)layout.findViewById(R.id.search_age_picker_low);
            mLowPicker.setMinValue(1);
            mLowPicker.setMaxValue(24);
            mLowPicker.setValue(EgmPrefHelper.getNoDisturbingStart(getActivity()));
            mHeighPicker = (NumberPicker)layout.findViewById(R.id.search_age_picker_heigh);
            mHeighPicker.setMinValue(1);
            mHeighPicker.setMaxValue(24);
            mHeighPicker.setValue(EgmPrefHelper.getNoDisturbingEnd(getActivity()));
            
            View okBtn = layout.findViewById(R.id.search_select_age_ok);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mStartHour = mLowPicker.getValue();
                    mEndHour = mHeighPicker.getValue();

                    mHour.setText(getString(R.string.msg_set_period, mStartHour,mEndHour));
//                    mInputAge.setText(mLowAge + "-" + mHeighAge);
                    mAgeDialog.dismiss();
                    EgmPrefHelper.putNoDisturbingStart(getActivity(), mStartHour);
                    EgmPrefHelper.putNoDisturbingEnd(getActivity(), mEndHour);
                }
            });
            View cancleBtn = layout.findViewById(R.id.search_select_age_cancel);
            cancleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAgeDialog.dismiss();
                }
            });
            
            mAgeDialog.setView(layout);
        }
        
        mAgeDialog.show();
    }
}
