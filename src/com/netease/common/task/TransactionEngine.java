package com.netease.common.task;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.netease.common.config.IConfig;

/**
 * 
 *
 */
public class TransactionEngine implements IConfig {
	
	/************************以下 IConfig 配置项*******************************/
	
	/**
	 * 线程优先级
	 */
	public static int Priority = Thread.NORM_PRIORITY - 1;
	
	/**
	 * 核心线程个数
	 */
	public static int CoreThreadCount = 6;
	
	/************************以上 IConfig 配置项*******************************/
	
	/**************************私有属性信息***********************************/
	private static int Transaction_Group_ID = 0;
	
	boolean mStop;
	Map<Integer, Transaction> mTransactionMap;
	LinkedBlockingQueue<Runnable> mTransactionQueue;
	ThreadPoolExecutor mThreadPool;

	boolean mTaskRuning;
	
	private static TransactionEngine mInstance; 
	
	/************************以下 公共方法*******************************/
	
//	private TransactionEngine() {
//		this(Thread.NORM_PRIORITY, 3);
//	}
	
	public TransactionEngine(int priority, final int coreThreadCount) {
		mTransactionQueue = new LinkedBlockingQueue<Runnable>();
		mTransactionMap = new ConcurrentHashMap<Integer, Transaction>();
		
		mThreadPool = new ThreadPoolExecutor(coreThreadCount, 30, 2000, 
				TimeUnit.MICROSECONDS, mTransactionQueue);
		mThreadPool.setThreadFactory(new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setPriority(coreThreadCount);
				return thread;
			}
		});
	}
	
	public static TransactionEngine Instance() {
		if (mInstance == null) {
			mInstance = new TransactionEngine(Priority, CoreThreadCount);
		}
		return mInstance;
	}

	/**
	 * 获取下一事物分组的id
	 * @return
	 */
	public static synchronized int getNextGroupID() {
		if (Transaction_Group_ID >= Short.MAX_VALUE) {
			Transaction_Group_ID = 0;
		}
		
		return ++Transaction_Group_ID;
	}
	
	/**
	 * 获取下一事物分组的id，并占用连续size大小group id
	 * @param size
	 * @return
	 */
	public static synchronized int getNextGroupID(int size) {
		if (size <= 0) {
			return Transaction_Group_ID;
		}
		
		if (Transaction_Group_ID + size >= Short.MAX_VALUE) {
			Transaction_Group_ID = 0;
		}
		
		int nextGroupID = Transaction_Group_ID + 1;
		Transaction_Group_ID += size;
		
		return nextGroupID;
	}
	
	/**
	 * 关闭事务引擎
	 */
	public void shutdown() {
		mStop = true;
		if (mThreadPool != null) {
			try {
				mTransactionMap.clear();
				mTransactionQueue.clear();
				mThreadPool.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 进入执行队列
	 * 
	 * @param tx
	 */
	public void beginTransaction(Transaction tx) {
		if (tx != null) {
			tx.setTransactionEngine(this);
			Integer key = Integer.valueOf(tx.getId());
			
			mTransactionMap.put(key, tx);
			
			try {
				mThreadPool.execute(tx);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
				if (tx instanceof AsyncTransaction) {
					((AsyncTransaction) tx).onTransactionError(0, null);
				}
				else {
					tx.notifyError(0, null);
				}
			}
		}
	}

	/**
	 * 结束执行
	 * 
	 * @param tx
	 */
	public void endTransaction(Transaction tx) {
		Integer key = Integer.valueOf(tx.getId());
		
		mTransactionMap.remove(key);
	}

	/**
	 * 添加一个等待事物
	 * 
	 * @param tx
	 */
	public void addWaitTransaction(Transaction tx) {
		if (tx != null) {
			Integer key = Integer.valueOf(tx.getId());
			tx.setTransactionEngine(this);
			
			mTransactionMap.put(key, tx);
		}
	}
	
	/**
	 * 按事物ID调整优先级
	 * @param tId
	 * @param priority
	 */
	public void adjustPriorityByTransactionID(int tid, int priority) {
		Transaction t = null;
		Integer key = Integer.valueOf(tid);
		
		t = mTransactionMap.get(key);
		
		if (t != null) {
			t.setPriority(priority);
		}		
	}
	
	/**
	 * 按照事务id查询事务
	 * 
	 * @param tid
	 * @return
	 */
	public Transaction getTransaction(Integer tid) {
		return mTransactionMap.get(tid);
	}
	
	/**
	 * 按组ID调整优先级
	 * @param gid
	 * @param priority
	 */
	public void adjustPriorityByGroupID(int gid, int priority) {
		Transaction t = null;
		Set<Entry<Integer, Transaction>> set = mTransactionMap.entrySet();

		for (Entry<Integer, Transaction> entry : set) {
			t = entry.getValue();

			if (t != null && t.getGroupID() == gid) {
				t.setPriority(priority);
			}
		}
	}
	

	/**
	 * 按分组取消事务执行
	 * 
	 * @param gId
	 */
	public void cancelTransactionByGroupID(int gid) {
		if (gid <= 0) {
			return ;
		}
		
		Transaction t = null;
		LinkedList<Integer> tids = new LinkedList<Integer>();
		
		Set<Entry<Integer, Transaction>> set = mTransactionMap.entrySet();

		for (Entry<Integer, Transaction> entry : set) {
			t = entry.getValue();

			if (t != null && t.getGroupID() == gid) {
				t.doCancel();
				tids.add(entry.getKey());
			}
		}
		
		if (tids.size() > 0) {
			for (Integer key : tids) {
				mTransactionMap.remove(key);
			}
		}
	}

	/**
	 * 取消事务执行
	 * 
	 * @param tId
	 */
	public void cancelTransaction(int tId) {
		if (tId <= 0) {
			return ;
		}
		
		Transaction trans = null;
		Integer key = Integer.valueOf(tId);
		
		trans = mTransactionMap.remove(key);
		
		if (trans != null) {
			trans.doCancel();
		}
	}

	/**
	 * 取消所有事务
	 */
	public void cancelAllTransactions() {
		mTransactionQueue.clear();
		Transaction t = null;
		
		Set<Entry<Integer, Transaction>> set = mTransactionMap.entrySet();

		for (Entry<Integer, Transaction> entry : set) {
			t = entry.getValue();

			if (t != null) {
				t.doCancel();
			}
		}
		
		mTransactionMap.clear();
	}
	
	/************************以上公共方法*******************************/
	


}
