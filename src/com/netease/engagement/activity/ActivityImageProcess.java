package com.netease.engagement.activity;

import java.io.File;
import java.net.URI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.LinearLayout;

import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.image.zoom.ImageZoomView;

public class ActivityImageProcess extends ActivityEngagementBase implements
		OnClickListener{

    public static final String TAG = ActivityImageProcess.class.getSimpleName();
    
    private int mWidth;
    private int mHeight;
    
    // 过滤类型
    private int mType = 0;
    private String mImageToBeSend;
	private Uri imageUri;
	
	private Bitmap mBitmap;
	//private Image img;

	private ImageZoomView mZoomView;
	private LinearLayout mFilterLayout;
	//private LinkedList<FilterInfo> mFilterList = new LinkedList<FilterInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_process);
		findViews();
		if (init()) {
		    //processByType(mType);
		}
	}

	private void findViews() {
		mFilterLayout = (LinearLayout) findViewById(R.id.filter_layout);
	}

	private boolean init() {
	    boolean bRes = false;
	    
		Display display = getWindowManager().getDefaultDisplay();
		mWidth = display.getWidth();
		if (mWidth > 480)
			mWidth = 480;
		mHeight = display.getHeight();
		if (mHeight > 800)
			mHeight = 800;

		Intent intent = getIntent();
		imageUri = intent.getData();
		mImageToBeSend = intent.getStringExtra(EgmConstants.EXTRA_PATH);
		mType = intent.getIntExtra(EgmConstants.EXTRA_DURATION, 0);
		
		mBitmap = ImageUtil.getBitmapFromUriLimitSize(this, imageUri,
				(mWidth > mHeight) ? mWidth : mHeight);
		
		// 获取照片的旋转角度
        int degree = 0;
        degree = ImageUtil.getRotateDegree(this, imageUri);
		mBitmap = ImageUtil.rotateBitmap(mBitmap, degree);

		if (mBitmap == null) {
			Uri uritobesend = Uri.fromFile(new File(mImageToBeSend));
			mBitmap = ImageUtil.getBitmapFromUriLimitSize(this, uritobesend,
					(mWidth > mHeight) ? mWidth : mHeight);
		}

		checkBitmap();

		if (mBitmap == null) {
			finish();
			return bRes;
		}

		mZoomView = (ImageZoomView) findViewById(R.id.imagezoomview_img);

		if (mBitmap != null)
			mZoomView.setImageBitmap(mBitmap);

		/*img = new Image(mBitmap);
		mFilterList.add(new FilterInfo(R.drawable.image_process_normal, getString(R.string.imagepreview_type_normal), new CvNormal()));
		mFilterList.add(new FilterInfo(R.drawable.image_process_qingse, getString(R.string.imagepreview_type_amaro), new CvAmaro(ActivityImageProcess.this, R.drawable.image_process_bb1024)));
		mFilterList.add(new FilterInfo(R.drawable.image_process_shentou, getString(R.string.imagepreview_type_hudson), new CvHudson(ActivityImageProcess.this, R.drawable.image_process_hdb)));
		mFilterList.add(new FilterInfo(R.drawable.image_process_poxiao, getString(R.string.imagepreview_type_rise), new CvRise(ActivityImageProcess.this, R.drawable.image_process_bb1024)));
		mFilterList.add(new FilterInfo(R.drawable.image_process_heibai, getString(R.string.imagepreview_type_inkwell), new CvInkWell()));
		mFilterList.add(new FilterInfo(R.drawable.image_process_natie, getString(R.string.imagepreview_type_earlybird), new CvEarlyBird(ActivityImageProcess.this, R.drawable.image_process_ol)));
		mFilterList.add(new FilterInfo(R.drawable.image_process_jianghuang, getString(R.string.imagepreview_type_kelvin), new CvKelvin()));
		mFilterList.add(new FilterInfo(R.drawable.image_process_liuli, getString(R.string.imagepreview_type_nashille), new CvNashille()));
		mFilterList.add(new FilterInfo(R.drawable.image_process_aicao, getString(R.string.imagepreview_type_walden), new CvWalden(ActivityImageProcess.this, R.drawable.image_process_ol2)));
		mFilterList.add(new FilterInfo(R.drawable.image_process_chenjiang, getString(R.string.imagepreview_type_lomo), new CvXproII(ActivityImageProcess.this, R.drawable.image_process_ol)));*/
        
		/*Iterator<FilterInfo> iterator = mFilterList.iterator();
		int tag = 0;
		while (iterator.hasNext()) {
		    FilterInfo ifn = iterator.next();

		    LinearLayout view = (LinearLayout)View.inflate(this, R.layout.item_view_image_process, null);
		    
		    ImageView img = (ImageView)view.findViewById(R.id.image);
		    Drawable d = SkinManager.getInstance(null).getDrawable(ifn.res);
		    img.setImageDrawable(d);
		    
		    TextView tv = (TextView)view.findViewById(R.id.text);
		    tv.setText(ifn.name);
		    
		    view.setOnClickListener(this);
		    view.setTag(tag++);
			mFilterLayout.addView(view);
		}*/
		
		getSupportActionBar().setTitle("滤镜");
		bRes = true;
		
		return bRes;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.activity_image_process, menu);
	    //Drawable d = SkinManager.getInstance(null).getDrawable(R.drawable.icon_action_done_selector);
	    Drawable d = getResources().getDrawable(R.drawable.icon_action_done_selector);
	    //必须在此处重新声明，在xml中声明不起作用
	    menu.findItem(R.id.action_done).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	    menu.findItem(R.id.action_done).setIcon(d);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    boolean flag = true;
	    switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                flag = true;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
	    
	    Intent intent = new Intent();
        intent.setData(imageUri);
        intent.putExtra(EgmConstants.EXTRA_PATH, mImageToBeSend);
        intent.putExtra(EgmConstants.EXTRA_OPERATE, flag);
        intent.putExtra(EgmConstants.EXTRA_DURATION, mType);
        setResult(RESULT_OK, intent);
        
        String path;
        if (URLUtil.isFileUrl(mImageToBeSend)) {
            path = new File(URI.create(mImageToBeSend)).getAbsolutePath();
        }  else {
            path = mImageToBeSend;
        }
        
        ImageUtil.saveBitMaptoFile(mBitmap, path);
        
        /*if (img.getImage() != null)
            ImageUtil.saveBitMaptoFile(img.getImage(), path);
        else
            ImageUtil.saveBitMaptoFile(mBitmap, path);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 200);
        
        return true;
	}

	@Override
	public void onClick(View v) {
		int type = -1;

		type = (Integer)v.getTag();
		mType = type;
		//processByType(type);
	}
	
	/**
	 * 通过滤镜类型处理图片
	 * @param type
	 */
	/*private void processByType(int type) {
	    FilterInfo info = mFilterList.get(type);
        IImageFilter filter = info.filter;
        
        if (filter != null) {
            img = filter.process(img);
            img.copyPixelsFromBuffer();
        }

        if (img.getImage() != null) {
            mZoomView.setImageBitmap(img.getImage());
        } else if (mBitmap != null) {
            mZoomView.setImageBitmap(mBitmap);
        } else {
            System.gc();
        }
        refreshFilterButton(type);
	}*/

	private void refreshFilterButton(int type) {

		for (int i = 0; i < mFilterLayout.getChildCount(); i++) {
			LinearLayout ll = (LinearLayout) mFilterLayout.getChildAt(i);
			if (ll.getTag().equals(type)) {
			    ll.getChildAt(0).setSelected(true);
			} else {
			    ll.getChildAt(0).setSelected(false);
			}
		}
	}

	@Override
	public void finish() {
		recyle();
		super.finish();
	}

	private void recyle() {
		if (mZoomView != null)
			mZoomView.setImageBitmap(null);

		if (mBitmap != null)
			mBitmap.recycle();
		mBitmap = null;
	}
	
	private void checkBitmap() {
		if (mBitmap == null || mBitmap.isRecycled())
			return;

		int w = mBitmap.getWidth();
		int h = mBitmap.getHeight();
		float sw = (float) mWidth / w;
		float sh = (float) mHeight / h;
		float scale = 1.0f;
		Matrix m = new Matrix();
		if (sw < 1 || sh < 1) {
			if (sw < sh)
				scale = sw;
			else
				scale = sh;
			m.setScale(scale, scale);
			try {
				mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, w, h, m, true);
			} catch (OutOfMemoryError e) {
			}
		}
	}

	/*private class FilterInfo {
        public int res;
        public String name;
        public IImageFilter filter;

        public FilterInfo(int res, String name, IImageFilter filter) {
            this.res = res;
            this.name = name;
            this.filter = filter;
        }
    }*/
}
