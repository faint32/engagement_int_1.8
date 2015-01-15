package com.netease.engagement.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.netease.date.R;

/**
 * 自定义的日期选择Dialog
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class EgmDatePickerDialog extends AlertDialog {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    
    private final EgmDatePicker mDatePicker;
    private final OnDateSetListener mDateSetListener;
    private View view;
    
    /** The callback used to indicate the user is done filling in the date. */
    public interface OnDateSetListener {
        void onDateSet(EgmDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }
    
    public EgmDatePickerDialog(Context context, 
            OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, int maxYear, int minYear) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth, maxYear, minYear);
    }
    
    public EgmDatePickerDialog(Context context, int theme, 
            OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, int maxYear, int minYear) {
        super(context, theme);
    
        mDateSetListener = callBack;
        Context themeContext = getContext();
    
        LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_date_picker_dialog, null);
        mDatePicker = (EgmDatePicker) view.findViewById(R.id.datePicker);
        
        if(maxYear > minYear && minYear > 0){
            mDatePicker.setYearLimit(maxYear, minYear);
        }
        
        mDatePicker.init(year, monthOfYear, dayOfMonth, null);
        
        setButton();
    }
    
    public void showDialog() {
        //自己实现show方法，主要是为了把setContentView方法放到show方法后面，否则会报错。
        show();
        setContentView(view);
    }
    
    public void setTitle(String title) {
        //获取自己定义的title布局并赋值。
        ((TextView) view.findViewById(R.id.date_picker_title)).setText(title);
    }
    
    @Override
    public void setTitle(int res) {
        //获取自己定义的title布局并赋值。
        ((TextView) view.findViewById(R.id.date_picker_title)).setText(res);
    }
    
    private void setButton() {
        view.findViewById(R.id.date_picker_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateSetListener != null) {
                    mDatePicker.clearFocus();
                    mDateSetListener.onDateSet(mDatePicker, mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDay());
                }
                dismiss();
            }
        });
        
        view.findViewById(R.id.date_picker_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    
    /**
    * Gets the {@link DatePicker} contained in this dialog.
    * 
    * @return The calendar view.
    */
    public EgmDatePicker getDatePicker() {
        return mDatePicker;
    }
    
    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }
    
    
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDay());
        return state;
    }
    
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        
        mDatePicker.init(year, month, day, null);
    }
}