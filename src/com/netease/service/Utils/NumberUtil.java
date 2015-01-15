package com.netease.service.Utils;


public class NumberUtil {
    
    
    public static String getNumber(int number) {
        String value = null;
        
        if (0 > number) {
            value = String.valueOf(0);
        } else if (0 <= number && number < 10000) {
            value = String.valueOf(number);
        } else if (number >= 10000) {
            number = number/10000;
            value = String.valueOf(number) + "万";
        }
        
        return value;
    }

}
