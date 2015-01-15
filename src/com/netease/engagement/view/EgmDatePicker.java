package com.netease.engagement.view;

import java.util.Calendar;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.Formatter;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.netease.date.R;


public class EgmDatePicker extends LinearLayout {
    private final int MIN_YEAR = 1900;
    private final int MAX_YEAR = 2100;
    
    private final int MIN_MONTH = 1;
    private final int MAX_MONTH = 12;
    
    private final int MIN_DAY = 1;
    
    private final String YEAR = "年";
    private final String MONTH = "月";
    
    
    private NumberPicker mYearPicker;
    private NumberPicker mMonthPicker;
    private NumberPicker mDayPicker;
    
    private int mMaxYear = MAX_YEAR, mMinYear = MIN_YEAR;
    
    private Calendar mCurrentDate;
    private OnDateChangedListener mOnDateChangedListener;
    
    public EgmDatePicker(Context context) {
        this(context, null);
    }
    
    public EgmDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EgmDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        LayoutInflater.from(context).inflate(R.layout.view_egm_date_picker, this, true);
        mYearPicker = (NumberPicker)findViewById(R.id.picker_year);
        mMonthPicker = (NumberPicker)findViewById(R.id.picker_month);
        mDayPicker = (NumberPicker)findViewById(R.id.picker_day);
        
        mCurrentDate = Calendar.getInstance();
        
        initPicker();
    }

    private void initPicker(){
        mYearPicker.setFormatter(mYearFormatter);
        mYearPicker.setMinValue(mMinYear);
        mYearPicker.setMaxValue(mMaxYear);
        mYearPicker.setOnValueChangedListener(onNumberChangeListener);
        
        mMonthPicker.setFormatter(mMonthFormatter);
        mMonthPicker.setMinValue(MIN_MONTH);
        mMonthPicker.setMaxValue(MAX_MONTH);
        mMonthPicker.setOnValueChangedListener(onNumberChangeListener);
        
        mDayPicker.setMinValue(MIN_DAY);
        setMaxDay();
        mDayPicker.setOnValueChangedListener(onNumberChangeListener);
    }
    
    private void setMaxDay(){
        int maxDay = mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        mDayPicker.setMaxValue(maxDay);
    }
    
    /**
     * Initialize the state. If the provided values designate an inconsistent
     * date the values are normalized before updating the spinners.
     *
     * @param year The initial year.
     * @param monthOfYear The initial month <strong>starting from zero</strong>.
     * @param dayOfMonth The initial day of the month.
     * @param onDateChangedListener How user is notified date is changed by
     *            user, can be null.
     */
    public void init(int year, int month, int day, OnDateChangedListener onDateChangedListener) {
        updateDate(year, month, day);
        setMaxDay();
        mOnDateChangedListener = onDateChangedListener;
    }
    
    public void init(int year, int month, int day) {
        updateDate(year, month, day);
        setMaxDay();
    }
    
    public void setYearLimit(int max, int min){
        mMaxYear = max;
        mMinYear = min;
        
        mYearPicker.setMinValue(mMinYear);
        mYearPicker.setMaxValue(mMaxYear);
    }
    
    public int getYear(){
        return mYearPicker.getValue();
    }
    
    public int getMonth(){
        return mMonthPicker.getValue();
    }
    
    public int getDay(){
        return mDayPicker.getValue();
    }
    
    public void updateDate(int year, int month, int day){
        mYearPicker.setValue(year);
        mMonthPicker.setValue(month);
        mDayPicker.setValue(day);
        
        mCurrentDate.set(year, month - 1, day);
    }
    
    private OnValueChangeListener onNumberChangeListener = new OnValueChangeListener(){
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            if(picker == mYearPicker){
                mCurrentDate.set(Calendar.YEAR, newVal);
                setMaxDay();
            }
            else if(picker == mMonthPicker){
                mCurrentDate.set(Calendar.MONTH, newVal - 1);
                setMaxDay();
            }
            else{
                mCurrentDate.set(Calendar.DATE, newVal);
            }
            
            if(mOnDateChangedListener != null){
                mOnDateChangedListener.onDateChanged(EgmDatePicker.this, getYear(), getMonth(), getDay());
            }
        }
    };
    
    private final Formatter mYearFormatter = new Formatter(){
        @Override
        public String format(int value) {
            return value + YEAR;
        }
    };
    
    private final Formatter mMonthFormatter = new Formatter(){
        @Override
        public String format(int value) {
            return value + MONTH;
        }
    };
    
    /** The callback used to indicate the user changes\d the date. */
    public interface OnDateChangedListener {

        /**
         * Called upon a date change.
         *
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param month The month that was set (0-11) for compatibility
         *            with {@link java.util.Calendar}.
         * @param day The day of the month that was set.
         */
        void onDateChanged(EgmDatePicker view, int year, int month, int day);
    }
}
