package com.netease.service.Utils;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;

import com.netease.util.date.FormatTime;
import com.netease.util.date.TimeUtil;


@SuppressLint("DefaultLocale") public class TimeFormatUtil {

    public static String getTimeFormat(Context context, long time) {
        String value = null;
        
        FormatTime formatTime = TimeUtil.getFormatTime(time);
        switch (formatTime.getType()) {
            case FormatTime.TYPE_JUST:
            case FormatTime.TYPE_SECONDS:
                value = "刚刚";
                break;
            case FormatTime.TYPE_MINUTES:
                value = formatTime.getInterval() +"分钟前";
                break;
            case FormatTime.TYPE_HOURS:
                value = formatTime.getInterval() + "小时前";
                break;
            case FormatTime.TYPE_DAYS:
                int days = formatTime.getInterval();
                if (days <= 3) {
                    value = days + "天前";
                    break;
                }
            case FormatTime.TYPE_MONTHS:
            case FormatTime.TYPE_YEARS:
            	Calendar calendar = Calendar.getInstance();
            	calendar.setTimeInMillis(time);
            	
                value = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR),
                		calendar.get(Calendar.MONTH) + 1, 
                		calendar.get(Calendar.DAY_OF_MONTH));
                break;
        }
        
        return value;
    }
    
    public static String covert2DisplayTime(long time) {
        String value = null;
        
        Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(time);
        
        FormatTime formatTime = TimeUtil.getFormatTime(time);
        switch (formatTime.getType()) {
            case FormatTime.TYPE_JUST:
            case FormatTime.TYPE_SECONDS:
            case FormatTime.TYPE_MINUTES:
            case FormatTime.TYPE_HOURS:
            	value = String.format("%02d:%02d", 
                		calendar.get(Calendar.HOUR_OF_DAY),
                		calendar.get(Calendar.MINUTE));
                break;
            case FormatTime.TYPE_DAYS:
                int days = formatTime.getInterval();
                if (days < 3) {
                	if (days == 2) {
                		value = "前天";
                	}
                	else {
                		value = "昨天";
                	}
                	
                    value = value + getInterval(calendar);
                    break;
                }
            case FormatTime.TYPE_MONTHS:
            	value = String.format("%02d月%02d日", 
                		calendar.get(Calendar.MONTH) + 1, 
                		calendar.get(Calendar.DAY_OF_MONTH));
            	break;
            case FormatTime.TYPE_YEARS:
                value = String.format("%04d年%02d月%02d日", 
                		calendar.get(Calendar.YEAR),
                		calendar.get(Calendar.MONTH) + 1, 
                		calendar.get(Calendar.DAY_OF_MONTH));
                break;
        }
        
        return value;
    }
    
    /** 2014年12月13日 */
    public static String forYMD(long time){
        Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(time);
    	
    	return String.format("%04d年%02d月%02d日", 
        		calendar.get(Calendar.YEAR),
        		calendar.get(Calendar.MONTH) + 1, 
        		calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    /** 2014.12.13 */
    public static String forYMDDotFormat(long time){
        Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(time);
    	
    	return String.format("%04d.%02d.%02d", 
        		calendar.get(Calendar.YEAR),
        		calendar.get(Calendar.MONTH) + 1, 
        		calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    
	private static String getInterval(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		if (hour >= 0 && hour < 7) {
			return "凌晨";
		} else if (hour >= 7 && hour < 12) {
			return "上午";
		} else if (hour >= 12 && hour < 19) {
			return "下午";
		} else {
			return "晚上";
		}
	}

}
