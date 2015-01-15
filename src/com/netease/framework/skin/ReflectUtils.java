package com.netease.framework.skin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {
	
	/**
	 * 获取成员变量属性
	 * @param paramClass
	 * @param paramString
	 * @return
	 */
	public static Field getField(Class<?> paramClass, String paramString) {
		Field field = null;
		
		try {
			field = paramClass.getDeclaredField(paramString);
			field.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return field;
	}
	
	/**
	 * 获取成员变量值
	 * @param paramClass
	 * @param paramObject
	 * @param paramString
	 * @return
	 */
	public static Object fieldGet(Class<?> paramClass, Object paramObject, String paramString) {
		Object obj = null;
		
		try {
			Field field = paramClass.getDeclaredField(paramString);
			field.setAccessible(true);
			obj = field.get(paramObject);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return obj;
	}

	/**
	 * 获取成员变量值
	 * @param paramField
	 * @param paramObject
	 * @return
	 */
	public static Object fieldGet(Field paramField, Object paramObject) {
		Object obj = null;
		
		try {
			paramField.setAccessible(true);
			obj = paramField.get(paramObject);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return obj;
	}

	/**
	 * 设置成员变量值
	 * @param paramClass
	 * @param paramObject1
	 * @param paramString
	 * @param paramObject2
	 * @return
	 */
	public static boolean fieldSet(Class<?> paramClass, Object paramObject1, String paramString, Object paramObject2) {
		boolean bRes = false;
		try {
			Field localField = paramClass.getDeclaredField(paramString);
			localField.setAccessible(true);
			localField.set(paramObject1, paramObject2);
			bRes = true;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return bRes;
	}

	/**
	 * 设置成员变量值
	 * @param paramField
	 * @param paramObject1
	 * @param paramObject2
	 * @return
	 */
	public static boolean fieldSet(Field paramField, Object paramObject1, Object paramObject2) {
		boolean bRes = false;
		
		try {
			paramField.setAccessible(true);
			paramField.set(paramObject1, paramObject2);
			return true;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return bRes;
	}

	/**
	 * 获取方法
	 * @param paramClass
	 * @param paramString
	 * @param paramArrayOfClass
	 * @return
	 */
	public static Method getMethod(Class<?> paramClass, String paramString, Class<?>[] paramArrayOfClass) {
		Method method = null;
		
		try {
			method = paramClass.getDeclaredMethod(paramString, paramArrayOfClass);
			method.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		return method;
	}

	/**
	 * 调用方法
	 * @param paramMethod
	 * @param paramObject
	 * @param paramArrayOfObject
	 * @return
	 */
	public static Object invokeMethod(Method paramMethod, Object paramObject, Object[] paramArrayOfObject) {
		Object obj = null;
		
		try {
			paramMethod.setAccessible(true);
			obj = paramMethod.invoke(paramObject, paramArrayOfObject);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	/**
	 * 调用方法
	 * @param paramClass
	 * @param paramObject
	 * @param paramString
	 * @param paramArrayOfClass
	 * @param paramArrayOfObject
	 * @return
	 */
	public static Object invokeMethod(Class<?> paramClass, Object paramObject, String paramString, Class<?>[] paramArrayOfClass, Object[] paramArrayOfObject) {
		Object obj = null;
		Method method = null;
		
		method = getMethod(paramClass, paramString, paramArrayOfClass);
		if (null != method) {
			obj = invokeMethod(method, paramObject, paramArrayOfObject);
		}
		
		return obj;
	}
}
