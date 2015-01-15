package com.netease.engagement.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import com.netease.common.http.filedownload.FileDownloadListener;
import com.netease.common.http.filedownload.FileDownloadManager;
import com.netease.engagement.dataMgr.BaseDownLoadManager;
import com.netease.share.gif.GifImageView;
import com.netease.share.sticker.model.StickerCategory;
import com.netease.share.sticker.model.StickerManager;
import com.netease.share.sticker.util.DependentUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class FaceImageView extends FrameLayout {
	
    public ProgressBar mProgressBar;
    public GifImageView mImageView;
    
    private String faceId;
    private String url;

    public FaceImageView(Context context) {
        this(context, null, 0);
    }

    public FaceImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFaceId() {
		return faceId;
	}
	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public void showProgressBar() {
    	mProgressBar.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
    }
    
    public void showImageView() {
    	mProgressBar.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);
    }
    
    public void showImageViewWithProgess() {
    	mProgressBar.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.VISIBLE);
    }
    
    public void restoreState(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
    }
    
    protected void init(){
        mImageView = new GifImageView(this.getContext()); 
        
        LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.addView(mImageView, lp1);
        
        mProgressBar = new ProgressBar(this.getContext());
        LayoutParams lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp2.gravity = Gravity.CENTER;
        this.addView(mProgressBar, lp2);
    }

	public void setFaceImage(String faceId, String url) {
		
		if (TextUtils.isEmpty(faceId)) {
			setSystemBitmap("sticker/emoji_error.png");
			return;
		}
		
		if (faceId.equals(this.faceId)) {
			return;
		}
		
		this.faceId = faceId;
		this.url = url;
		
		int end;
		for (end = 0; end < faceId.length(); end++) {
			char c = faceId.charAt(end);
			if (c >= '0' && c <= '9') {
				break;
			}
		}
		String categoryName = faceId.substring(0, end);

		if (StickerManager.getInstance().isCategoryInUse(categoryName)) { // 表情包已在本地（系统表情包、已下载表情包）

			StickerCategory stickerCategory = StickerManager.getInstance().getCategory(categoryName);
			if (stickerCategory.system()) {
				String fileName = "sticker/"+categoryName+"/"+faceId+".png";
				setSystemBitmap(fileName);
			} else {
				String faceIdWithSuffix = faceId + stickerCategory.getSuffix();
				String path = DependentUtils.getNvStickerPath() + stickerCategory.getName() + "/" + faceIdWithSuffix;
				setDownLoadBitmap(path);
			}

		} else { // 表情包未下载
			
			if (TextUtils.isEmpty(url)) {
				setSystemBitmap("sticker/emoji_error.png");
				return;
			}
			
			String faceIdWithSuffix = faceId + url.substring(url.lastIndexOf("."), url.length());
			String path = DependentUtils.getNvStickerTempPath() + "/" + faceIdWithSuffix;
			File desFile = new File(path);
			if (desFile.exists()) {
				setDownLoadBitmap(path);
			} else {
				new DownLoadFaceImageTask(this, url, path).download();
			}
		}
	}
	
	private void setSystemBitmap(String fileName) {
		try {
			InputStream is = this.getContext().getAssets().open(fileName);
			Bitmap b = BitmapFactory.decodeStream(is);
			mImageView.setImageBitmap(b);
			showImageView();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setDownLoadBitmap(String path) {
		try {
			if (path.contains("gif")) {
				mImageView.loadAsPath(path);
			} else {
				InputStream is = new FileInputStream(path);
				Bitmap b = BitmapFactory.decodeStream(is);
				mImageView.setImageBitmap(b);
			}
			showImageView();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	class DownLoadFaceImageTask {
		private WeakReference<FaceImageView> weekFaceImageView;
		private String downloadUrl;
		private String desFilePath;
		
		public DownLoadFaceImageTask(FaceImageView faceImageView, String url, String desFilePath) {
			this.weekFaceImageView = new WeakReference<FaceImageView>(faceImageView);
			this.downloadUrl = url;
			this.desFilePath = desFilePath;
		}
		
		public void download() {
			
			final File desFile = new File(desFilePath);
			if(desFile.exists()){
				return ;
			} else {
				try {
					desFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					FileDownloadManager.getInstance().downloadFile(url, null, null,new FileDownloadListener() {
						@Override
						public void onSuccess(final String path) {
							if(BaseDownLoadManager.copyToData(path, desFile.getPath())){
								dealDownLoadSuccess();
							} else {
								dealDownLoadFail();
							}
						}

						@Override
						public void onFailed(String err, int errCode) {
							dealDownLoadFail();
						}

						@Override
						public void onProgress(long current, long total, int percent, int speed) {
						}
					});
				}
			}).start();
		}
		
		private void dealDownLoadSuccess() {
			if (weekFaceImageView != null) {
				if (downloadUrl.equals(weekFaceImageView.get().getUrl())) {
					FaceImageView.this.post(new Runnable() {
						@Override
						public void run() {
							FaceImageView.this.setDownLoadBitmap(desFilePath);
						}
					});
				}
			}
		}
		
		private void dealDownLoadFail() {
			if (weekFaceImageView != null) {
				if (downloadUrl.equals(weekFaceImageView.get().getUrl())) {
					FaceImageView.this.post(new Runnable() {
						@Override
						public void run() {
							FaceImageView.this.setSystemBitmap("sticker/emoji_error.png");
						}
					});
				}
			}
		}
		
	}
	
}
