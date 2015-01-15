package com.netease.util.date;

import java.util.Calendar;

public class FormatTime {

	public static final int TYPE_JUST = 0x00; // 刚刚，
	public static final int TYPE_SECONDS = 0x01; // 几秒前，
	public static final int TYPE_MINUTES = 0x02; // 几分前，
	public static final int TYPE_HOURS = 0x04; // 几小时前，
	public static final int TYPE_DAYS = 0x05; // 几天前，
	public static final int TYPE_MONTHS = 0x06; // 几个月前
	public static final int TYPE_YEARS = 0x07; // 几个月前
	
	private int mType;
	private int mInterval;
	
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;
	private int mSecond;
	
	public FormatTime(int type) {
		mType = type;
	}
	
	public FormatTime(Calendar calender) {
		mYear = calender.get(Calendar.YEAR);
		mMonth = calender.get(Calendar.MONTH);
		mDay = calender.get(Calendar.DAY_OF_MONTH);
		mHour = calender.get(Calendar.HOUR_OF_DAY);
		mMinute = calender.get(Calendar.MINUTE);
		mSecond = calender.get(Calendar.SECOND);
	}
	
	public int getType() {
		return mType;
	}
	
	public void setType(int type) {
		this.mType = type;
	}
	
	public int getInterval() {
		return mInterval;
	}
	
	public void setInterval(int interval) {
		this.mInterval = interval;
	}
	
	public int getYear() {
		return mYear;
	}
	
	public void setYear(int year) {
		this.mYear = year;
	}
	
	public int getMonth() {
		return mMonth;
	}
	
	public void setMonth(int month) {
		this.mMonth = month;
	}
	
	public int getDay() {
		return mDay;
	}
	
	public void setDay(int day) {
		this.mDay = day;
	}
	
	public int getHour() {
		return mHour;
	}
	
	public void setHour(int hour) {
		this.mHour = hour;
	}
	
	public int getMinute() {
		return mMinute;
	}
	
	public void setMinute(int minute) {
		this.mMinute = minute;
	}
	
	public int getSecond() {
		return mSecond;
	}
	
	public void setSecond(int second) {
		this.mSecond = second;
	}

	/**
	 * this time < nowTime
	 * @param nowTime
	 */
	public void compareSetType(FormatTime nowTime) {
		int interval = nowTime.mYear - mYear;
		if (interval > 0) {
			setType(TYPE_YEARS);
		} else {
			interval = nowTime.mMonth - mMonth;
			if (interval > 0) {
				setType(TYPE_MONTHS);
			} else {
				interval = nowTime.mDay - mDay;
				if (interval > 0) {
					setType(TYPE_DAYS);
				} else {
					interval = nowTime.mHour - mHour;
					if (interval > 0) {
						setType(TYPE_HOURS);
					} else {
						interval = nowTime.mMinute - mMinute;
						if (interval > 0) {
							setType(TYPE_MINUTES);
						} else {
							interval = nowTime.mSecond - mSecond;
							if (interval > 0) {
								setType(TYPE_SECONDS);
							} else {
								setType(TYPE_JUST);
							}
						}
					}
				}
			}
		}
		
		mInterval = interval;
	}
	
}
