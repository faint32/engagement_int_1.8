package com.netease.service.Utils;


public class StackTraceUtil {

   public static String getMethodName() {
       return Thread.currentThread().getStackTrace()[3].getMethodName();
   }

}
