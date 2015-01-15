package com.netease.util.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class TimeUtil {
	
	private static final HashSet<String> mSimpleDateFormatKeys;
	private static final Map<String, DateFormat> mSimpleDateFormats;
	
	static {
		mSimpleDateFormats = new HashMap<String, DateFormat>();
		
		mSimpleDateFormatKeys = new HashSet<String>();
		mSimpleDateFormatKeys.add("yyyy-MM-dd HH:mm:ss");
		mSimpleDateFormatKeys.add("HH:mm:ss");
	}
	
	/**
	 * 重设内存缓存DateFormat类型
	 * 
	 * @param keys not null;
	 */
	public static void resetSimpleFormatKeys(Collection<String> keys) {
		mSimpleDateFormatKeys.clear();
		mSimpleDateFormats.clear();
		
		mSimpleDateFormatKeys.addAll(keys);
	}
	
	/**
	 * 获取简单Date Format
	 * 
	 * @param format
	 * @return
	 */
	public static DateFormat getSimpleDateFormat(String format) {
		DateFormat dateFormat = null;
		
		dateFormat = mSimpleDateFormats.get(format);
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat(format);
			
			if (mSimpleDateFormatKeys.contains(format)) {
				mSimpleDateFormats.put(format, dateFormat);
			}
		}
		
		return dateFormat;
	}
	
	/**
	 * 获取Format Time
	 * 
	 * @param time
	 * @return
	 */
	public static FormatTime getFormatTime(long time) {
		FormatTime formatTime;
		
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		
		long interval = now - time;
		
		if (interval <= 0) {
			formatTime = new FormatTime(FormatTime.TYPE_JUST);
		}
		else {
			FormatTime nowTime = new FormatTime(calendar);
			
			calendar.setTimeInMillis(time);
			formatTime = new FormatTime(calendar);
			formatTime.compareSetType(nowTime);
		}

		return formatTime;
	}
	
}
