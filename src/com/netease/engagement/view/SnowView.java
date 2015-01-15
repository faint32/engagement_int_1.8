package com.netease.engagement.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.netease.common.http.filedownload.FileDownloadListener;
import com.netease.common.http.filedownload.FileDownloadManager;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.BaseDownLoadManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class SnowView extends View {
	
	// 整个动效的总时长
	public static int TOTAL_DURATION = 5000;  
	// 产生雪花的时间区域
	private static int SNOW_CREATE_DURATION = 2500;
	// 雪花飘落下来的最小时间
	private static int MIN_SNOW_DURATION = 1500;
	
	// 最少的雪花数量
	private static int MIN_SNOW_COUNT = 20;
	// 随机生成的雪花数量
	private static int RANDOM_SONW_COUNT = 20;
	// 实际的雪花数量 ＝ 最少的雪花数量 ＋ 随机生成的雪花数量
	private int snowCount;
	// 保存实际生成的雪花
	private List<Snow> snows = new ArrayList<Snow>();
	
	
	// 雪花图片
	private Bitmap bitmap;
	// 画笔
	private final Paint mPaint = new Paint();
	// 随即生成器
	private static final Random random = new Random();
	
	// 屏幕的高度和宽度
	private int viewHeight = 0;
	private int viewWidth = 0;
	
	private String url;
	
	private boolean isSnowing = false;
	
	private Timer timer;
	private long startTime;

	public SnowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SnowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public boolean isSnowing() {
		return isSnowing;
	}

	public void setSnowing(boolean isSnowing) {
		this.isSnowing = isSnowing;
	}

	public void SetView(int height, int width) {
		viewHeight = height;
		viewWidth = width;
	}
	
	public void snow(String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (isSnowing) {
			return;
		}
		this.url = url;
		
		downloadImage(url);
	}
	
	private void snow() {
		addRandomSnow();
		
		startTime = System.currentTimeMillis();
		
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		
		isSnowing = true;
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SnowView.this.post(new Runnable() {
					@Override
					public void run() {
						long currentTime = System.currentTimeMillis();
						if (currentTime - startTime > SnowView.TOTAL_DURATION) {
							isSnowing = false;
							if (timer != null) {
								timer.cancel();
								timer = null;
							}
						}
						SnowView.this.invalidate();
					}
				});
				
			}
		}, 0, 50);
	}

	private void addRandomSnow() {
		
		snows.clear();
		
		long currentTime = System.currentTimeMillis();
		
		snowCount = MIN_SNOW_COUNT + random.nextInt(RANDOM_SONW_COUNT);
		
		for(int i =0; i< snowCount; i++){
			
			int startDelay = random.nextInt(SNOW_CREATE_DURATION);
			long startTime = currentTime + startDelay;
			float duration = MIN_SNOW_DURATION + random.nextInt(TOTAL_DURATION - startDelay - MIN_SNOW_DURATION);
			
			float vy = viewHeight / duration;
			if (vy == 0) {
				vy = 1;
			}
			
			float startX = random.nextInt(viewWidth);
			float endX = random.nextInt(viewWidth);
			float vx = (endX - startX) / duration;
			
			Snow snow = new Snow(startTime, startX, vx, vy);
			snows.add(snow);
		}
	}


	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isSnowing) {
			long currentTime = System.currentTimeMillis();
			for (int i=0; i<snowCount; i++) {
				Snow snow = snows.get(i);
				if (currentTime < snow.startTime) {
					continue;
				}
				
				long deltaTime = currentTime - snow.startTime;
				
				float x = (float)(snow.startX + snow.vx * deltaTime);
				float y = (float)(snow.vy * deltaTime);
				
				if (bitmap != null) {
					canvas.drawBitmap(bitmap, x, y, mPaint);
				}
			}
		}
	}

	
	class Snow {
		public long startTime;
		public float startX;
		public float vx;
		public float vy;
		
		public Snow(long startTime, float startX, float vx, float vy) {
			this.startTime = startTime;
			this.startX = startX;
			this.vx = vx;
			this.vy = vy;
		}
	}
	
	
	
	// 雪花图片目录
	public static final String SNOW_ZIP_DIR = EngagementApp.getAppInstance().getFilesDir().getPath()+"/snow_dir";
	
	private void downloadImage(final String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		
		final String imageName = url.substring(url.lastIndexOf("/")+1);
		final File desFile = new File(SNOW_ZIP_DIR + "/" + imageName);
		
		if(desFile.exists()){
			bitmap = BitmapFactory.decodeFile(desFile.getPath());
			snow();
			return ;
		} else {
			File dirFile = new File(SNOW_ZIP_DIR);
			if(!dirFile.exists()){
				dirFile.mkdirs();
			}
			try {
				desFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(FileDownloadManager.getInstance().checkIsDownloading(url)){
				return ;
			}
			FileDownloadManager.getInstance().downloadFile(url, null, null,new FileDownloadListener() {
				@Override
				public void onSuccess(String path) {
					if(BaseDownLoadManager.copyToData(path, desFile.getPath())){
						if (url.equalsIgnoreCase(SnowView.this.url)) {
							bitmap = BitmapFactory.decodeFile(desFile.getPath());
							snow();
						}
					}
				}
				@Override
				public void onFailed(String err, int errCode) {
					if(desFile.exists()){
						desFile.delete();
					}
				}
				@Override
				public void onProgress(long current, long total, int percent,
						int speed) {
				}
			});
		}
	}
}
