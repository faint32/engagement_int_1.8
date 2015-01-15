package com.netease.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.SharedPreferences;

public class PreferenceUtils {
	  private static final Method sApplyMethod = findApplyMethod();  
	  
	  private static Method findApplyMethod() {  
	        try {  
	            Class cls = SharedPreferences.Editor.class;  
	            return cls.getMethod("apply");  
	        } catch (NoSuchMethodException unused) {  
	            // fall through  
	        }  
	        return null;  
	    }  
	  
	  public static boolean apply(SharedPreferences.Editor editor) {  
	        if (sApplyMethod != null) {  
	            try {  
	                sApplyMethod.invoke(editor);  
	                return true;  
	            } catch (InvocationTargetException unused) {  
	                // fall through  
	            	unused.printStackTrace();
	            } catch (IllegalAccessException unused) {  
	                // fall through  
	            	unused.printStackTrace();
	            } catch (Exception io) {
	            	io.printStackTrace();
	            }
	        }  
	        return editor.commit();  
	    }  
}
