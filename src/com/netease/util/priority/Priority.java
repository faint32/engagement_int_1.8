package com.netease.util.priority;

/**
 *
 * 用于控制任务或者Http处理请求，以下具体策略：
 * 1、使用类型计数，判断是否需要对列表进行排序；
 * 2、当一个任务完成后对队列按优先级进行排序；
 * 3、默认任务优先级为NORMAL；
 * 4、优先级排序：
 * 		a、排序过程中EMERGENCY任务超过一定数量时(其窗口大小为1，参考数量)，则前面的自动降级为HIGH，
 * 			并将后面的置顶(单请求队列组合为EMERGENCY、HIGH)
 * 		b、排序过程中HIGHER任务超过一定数量时(其窗口大小为6，参考数量)，则前面的自动降级为HIGH，
 * 			并将后面的置顶（排在EMERGENCY之后），(多请求队列组合为EMERGENCY、HIGH)
 * 		c、已经排序过的HIGH任务，超过一定数量时(其窗口大小为12，参考数量)，自动降低优先级到NORMAL
 * 		d、HIGH任务，栈式（先进后出）处理，注：如果为可优先比较的执行后进先出原则
 * 		e、NORMAL和LOW任务，队列（先进先出）处理
 * 5、对所有任务取消或者降级；
 * 
 * EMERGENCY > HIGHER > HIGH > NORMAL > LOW
 */

public interface Priority {
	public static final int LOW = 0; // 先入先出策略
	public static final int NORMAL = 1; // 先入先出策略
	public static final int HIGH = 2; // 先入先入策略
	public static final int HIGHER = 3; // 先入先入策略
	public static final int EMERGENCY = 4; // 先入先入策略 
	
	public static final int HRADER_TAG = 0x0100; // 头部标记
	
	/**
	 * 设置优先级
	 * @param priority
	 */
	public void setPriority(int priority);
	
	/**
	 * 获取优先级
	 * @param priority
	 */
	public int getPriority();
	
	/**
	 * 分组ID
	 */
	public int getGroupID();
}
