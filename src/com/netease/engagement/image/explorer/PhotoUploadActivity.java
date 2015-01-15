package com.netease.engagement.image.explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.IsCameraPhotoFlag;
import com.netease.engagement.image.explorer.adapter.ImageExplorerBean;
import com.netease.engagement.image.explorer.utils.BitmapExecutorService;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.PictureInfo;

public class PhotoUploadActivity extends ActivityEngagementBase implements IOnSelectPictureListener{
    public static final String EXTRA_BUCKET_DISPLAY_NAME = "bucketDisplayName";
    public static final String EXTRA_MAX_COUNT = "maxCount";
    public static final String EXTRA_PHOTO_TYPE = "photoType";
    public static final String EXTRA_PIC_MAX_SIZE = "pic_max_size";
    public static final String EXTRA_PIC_MIN_SIZE = "pic_min_size";
	
	private final int CONTAINER_ID = R.id.activity_upload_picture_container_id;
	
	private FragmentManager mFragmentManager;
	
	private static String displayName;
	
	// 当前每次允许上传最多的张数
	private static int maxCount; 
	// 私照 or 公照
	private static int photoType; 
	// 当前已勾选了的张数
	private static int checkedCount;
	
	private int mMaxSize, mMinSize;    // 图片的最大最小尺寸
	
	/** 
	 * 用于记录该文件目录下，照片的在MediaStore.Images.Media中的ID
	 * 通过该ID可以查询出该图片的文件路径（MediaStore.Images.Media.DATA）
	 */
	private static List<ImageExplorerBean> picList = new ArrayList<ImageExplorerBean>(); 
	
	// 用于记录列表的check状态
	private static SparseBooleanArray checkStatus;
	
	// 用于记录已checked的图片ID, 点击上传时遍历这个列表
	private SparseArray<ImageExplorerBean> checkedId = new SparseArray<ImageExplorerBean>();
	
	// 用于记录当前GalleryPhotoUploadFragment所显示图片的id
	private String currentGalleryPhotoPath;
	
	private GridPhotoUploadFragment gridFragment;
	private GalleryPhotoUploadFragment galleryFragment;
	
	private int mTid;
	
	public static void startActivity(Context context, String displayName, int maxCount, int photoType, int max, int min){
	    Intent intent = new Intent(context, PhotoUploadActivity.class);
        intent.putExtra(EXTRA_BUCKET_DISPLAY_NAME, displayName);
        intent.putExtra(EXTRA_MAX_COUNT, maxCount);
        intent.putExtra(EXTRA_PHOTO_TYPE, photoType);
        intent.putExtra(EXTRA_PIC_MAX_SIZE, max);
        intent.putExtra(EXTRA_PIC_MIN_SIZE, min);
        
        context.startActivity(intent);
	}
	
	public static void startActivityForSelectPicture(Activity context, String displayName, 
	        int maxCount, int photoType, int max, int min, int requestCode){
	    
        Intent intent = new Intent(context, PhotoUploadActivity.class);
        intent.putExtra(EXTRA_BUCKET_DISPLAY_NAME, displayName);
        intent.putExtra(EXTRA_MAX_COUNT, maxCount);
        intent.putExtra(EXTRA_PHOTO_TYPE, photoType);
        intent.putExtra(EXTRA_PIC_MAX_SIZE, max);
        intent.putExtra(EXTRA_PIC_MIN_SIZE, min);
        
        context.startActivityForResult(intent, requestCode);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.getActionBar().hide();
		
		Intent intent = getIntent();
		displayName = intent.getStringExtra(EXTRA_BUCKET_DISPLAY_NAME);
		maxCount = intent.getIntExtra(EXTRA_MAX_COUNT, 1);
		photoType = intent.getIntExtra(EXTRA_PHOTO_TYPE, -1);
		mMaxSize = intent.getIntExtra(EXTRA_PIC_MAX_SIZE, EgmConstants.SIZE_MAX_PICTURE);
		mMinSize = intent.getIntExtra(EXTRA_PIC_MIN_SIZE, EgmConstants.SIZE_MIN_PICTURE);
		checkedCount = 0;
		
		initPicIdList();
		
		mFragmentManager = getFragmentManager();
		LinearLayout linear = new LinearLayout(this);
		linear.setId(CONTAINER_ID);
	    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
	    		LinearLayout.LayoutParams.MATCH_PARENT,
	            LinearLayout.LayoutParams.MATCH_PARENT);
	    linear.setLayoutParams(lp);
	    setContentView(linear);
	     
	    if (findViewById(CONTAINER_ID) != null && savedInstanceState == null) {
	    	
	    	gridFragment = new GridPhotoUploadFragment();
	    	gridFragment.setPictureType(photoType);
	    	
	    	mFragmentManager.beginTransaction()
            .add(CONTAINER_ID, gridFragment)
            .commit();
	    	gridFragment.setOnGridPhotoClickListener(onGridPhotoClickListener);
	    	gridFragment.setOnUploadClickListener(onUploadClickForGridListener);
	    }
	    
	    EgmService.getInstance().addListener(mEgmCallback);
	}
	
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    
	    EgmService.getInstance().removeListener(mEgmCallback);
	}
	
	private void initPicIdList() {
	    picList.clear();
        
        Cursor cursor;
        
        String[] projection = { MediaStore.Images.Media._ID,
                                MediaStore.Images.Media.DATA};
        
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
        
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " desc";
        
        CursorLoader cLoader = new  CursorLoader(this,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, new String[] {displayName}, sortOrder);
        cursor = cLoader.loadInBackground();
        
        try {
        	while(cursor.moveToNext()) {
            
        		int origId = cursor.getInt(cursor.getColumnIndexOrThrow(projection[0]));
        		String path = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]));
            
        		picList.add(new ImageExplorerBean(origId, path));
        	}
        }  finally {
        	if(cursor != null) {
        		cursor.close();
        	}
        } 
        
	}
	
	public static void initCheckStatus(int size) {
		if(checkStatus == null) {
			checkStatus = new SparseBooleanArray();
		} else {
			checkStatus.clear();
		}
		
		for(int i = 0; i < size; i++) {
			checkStatus.append(i, false);
		}
	}
	
	public static List<ImageExplorerBean> getPicList() {
        return picList;
    }
	
	public static int getMaxCount() {
		return maxCount;
	}
	
	public static int getCheckedCount() {
		return checkedCount;
	}
	
	public static String getDisplayName() {
		return displayName;
	}
	
	public static SparseBooleanArray getCheckSatus() {
		return checkStatus;
	}

	/** 多张上传时当前上传的指示器 */
	private int mUploadIndex = 0;
	/** 上传成功的数量 */
	private int mUploadSuccess = 0;
	
	/** 上传图片，嵌套函数 */
	private void doUploadPicture(){
	    if(mUploadIndex >= checkedId.size()){  // 传完了
            stopWaiting();
            String result = getString(R.string.rec_upload_picture_result, mUploadSuccess, checkedId.size() - mUploadSuccess);
            Toast.makeText(PhotoUploadActivity.this, result, Toast.LENGTH_SHORT).show();
            finish();
        }
        else{   // 还没传完
            int key = checkedId.keyAt(mUploadIndex);
            // 如果图片尺寸过大，需先压缩后再上传
            Uri uri = Uri.fromFile(new File(checkedId.get(key).getPath()));
            String path = ImageUtil.legitimateImageSizeToPath(this, uri, EgmConstants.SIZE_MAX_PICTURE, EgmConstants.SIZE_MIN_PICTURE);
            
            if(path == null){  // 图片不合格，跳过进入下一张
                mUploadIndex++;
                doUploadPicture();
            }
            else{
                mTid = EgmService.getInstance().upLoadPicture(path, photoType, IsCameraPhotoFlag.OtherPhoto);
            }
        }
	    
	}
	
	/*
	 * 当点击GridView中其中一张图片，则跳到GalleryPhotoUploadFragment
	 */
	private OnClickListener onGridPhotoClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		    int pos = (Integer) v.getTag();
		    
		    if(photoType == EgmConstants.Photo_Type.TYPE_AVATAR){ // 头像类型为选择一张图片
		        Intent intent = new Intent();
		        String path = picList.get(pos).getPath();
		        intent.setData(Uri.fromFile(new File(path)));
		        setResult(Activity.RESULT_OK, intent);
		        finish();
		    }
		    else{
		        galleryFragment = new GalleryPhotoUploadFragment();
	            
	            Bundle bundle = new Bundle();
	            bundle.putInt("pos", pos);
	            bundle.putInt("photoType", photoType);
	            galleryFragment.setArguments(bundle);
	            galleryFragment.setOnUploadClickForGalleryListener(onUploadClickForGalleryListener);
	            mFragmentManager.beginTransaction()
	            .replace(CONTAINER_ID, galleryFragment)
	            .addToBackStack(null)
	            .commit();
		    }
		}
	};
	
	
	/*
	 * 有两个点击上传的listener的原因：
	 * 因为在GridUpload的fragment情况下，如果没有选中任何图片，是什么都不上传
	 * 而在GalleryUpload的fragment情况下，如果没有选中任务图片，是默认上传当前那张图片的
	 * 所以区分开这两个上传的listener
	 */
	
    //点击GridPhotoUploadFragment的“上传”按钮的回调函数
	private OnClickListener onUploadClickForGridListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(checkedId.size() != 0) {
				if(photoType == EgmConstants.Photo_Type.TYPE_CHAT_PUBLIC_PIC){
					int key = checkedId.keyAt(mUploadIndex);
			        Uri uri = Uri.fromFile(new File(checkedId.get(key).getPath()));
			        Intent intent = new Intent();
			        intent.setData(uri);
			        setResult(Activity.RESULT_OK,intent);
			        finish();
			        return ;
			    }
				gridFragment.setUploadTvEnable(false);
			    showWatting(null, getString(R.string.common_tip_is_updating), false);
			    doUploadPicture();
			} 
			else { 
				//For test：当checkId中没有元素（即没有选中任何图片），因为视觉决定上传按钮不需要press状态，所以没有将fragment中的uploadTv设成不能点击
			}
		}
	};
    //点击GalleryPhotoUploadFragment的“上传”按钮的回调函数
	private OnClickListener onUploadClickForGalleryListener = new OnClickListener() {	
		@Override
		public void onClick(View v) {
			
			//聊天界面发送公开照片
			if(photoType == EgmConstants.Photo_Type.TYPE_CHAT_PUBLIC_PIC
					&& !TextUtils.isEmpty(currentGalleryPhotoPath)){
				Uri uri = Uri.fromFile(new File(currentGalleryPhotoPath));
		        Intent intent = new Intent();
		        intent.setData(uri);
		        setResult(Activity.RESULT_OK,intent);
		        finish();
		        return ;
		    }
			
			
			if(checkedId.size() != 0) {
				galleryFragment.setUploadTvEnable(false);
				showWatting(null, getString(R.string.common_tip_is_updating), false);
                doUploadPicture();
			} 
			else {   //当checkId中没有元素（即没有选中任何图片），默认上传当前显示的图片
				
			    if(TextUtils.isEmpty(currentGalleryPhotoPath))
			        return;
			    Uri uri = Uri.fromFile(new File(currentGalleryPhotoPath));
			    if(ImageUtil.isPictureTooSamll(PhotoUploadActivity.this, uri, mMaxSize, mMinSize)){   // 太小，不上传
			        Toast.makeText(PhotoUploadActivity.this, R.string.reg_tip_avatar_too_small, Toast.LENGTH_SHORT).show();
			    }
			    else{
			        // 只有选择一张，为了复用doUploadPicture函数，所以制造一个checkedId。这个完成后，界面销毁，checkedId不会再被使用，否则会乱套.
			    	galleryFragment.setUploadTvEnable(false);
			        checkedId.put(0, new ImageExplorerBean(0, currentGalleryPhotoPath));
			        doUploadPicture();
			    }
			}
		}
	};
	
	@Override
	public void onNonePictureSelected() {
	}

	@Override
	public boolean onPictureSelected(int pos) {
	    // 选中之前要先判断会不会太小，如果太小就不能选择
	    Uri uri = Uri.fromFile(new File(picList.get(pos).getPath()));
        if(ImageUtil.isPictureTooSamll(this, uri, mMaxSize, mMinSize)){   // 太小，不选中
            Toast.makeText(PhotoUploadActivity.this, R.string.reg_tip_avatar_too_small, Toast.LENGTH_SHORT).show();
            return false;   // 取消选中
        }
        else{
            checkedCount++;
            checkStatus.put(pos, true);
            checkedId.put(pos, picList.get(pos));
            
            if(gridFragment != null) {
                gridFragment.setCurrentPicCount();
            }
            if(galleryFragment != null) {
                galleryFragment.setCurrentPicCount();
            }
            
            return true;    // 确认选中
        }
	}

	@Override
	public void onPictureDisseletced(int pos) {
		checkedCount--;
		checkStatus.put(pos, false);
		checkedId.remove(pos);
		
		if(gridFragment != null) {
			gridFragment.setCurrentPicCount();
		}
		if(galleryFragment != null) {
			galleryFragment.setCurrentPicCount();
		}
	}

	@Override
	public void onOverMaxPictureSelected() {
		Toast.makeText(PhotoUploadActivity.this, "最多只能选择" + maxCount + "张", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSlidePictureSelected(int pos) {
	    currentGalleryPhotoPath = picList.get(pos).getPath();
	}	
	
	private EgmCallBack mEgmCallback = new EgmCallBack(){
	    /**上传公开照和私照*/
	    @Override
        public void onUploadPicSucess(int transactionId, PictureInfo obj){
	        if(mTid != transactionId)
	            return;
	        
	        EgmPrefHelper.putUpdatePicTime(PhotoUploadActivity.this, java.lang.System.currentTimeMillis());
	        
	        mUploadSuccess++;
	        mUploadIndex++;
	        doUploadPicture();
	    }
	    @Override
        public void onUploadPicError(int transactionId, int errCode, String err){
	        if(mTid != transactionId)
                return;
	        
	        String tipStr;
            String result = getString(R.string.rec_upload_picture_result, mUploadSuccess, checkedId.size() - mUploadSuccess);
	        
            if(gridFragment != null) {
            	gridFragment.setUploadTvEnable(true);
            }
            if(galleryFragment != null) {
            	galleryFragment.setUploadTvEnable(true);
            }
            
            switch(errCode){
	            // 有多张的话后续的就不要传了，停止
                case EgmServiceCode.TRANSACTION_PICTURE_PUBLIC_LIMIT:  // 公共照片最多只能上传10张
                case EgmServiceCode.TRANSACTION_PICTURE_PRIVATE_LIMIT: // 私密照片最多只能上传300张
                case EgmServiceCode.TRANSACTION_PICTURE_FULL:          // 相册已满
                    stopWaiting();
                    tipStr = err + " " + result;
                    Toast.makeText(PhotoUploadActivity.this, tipStr, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                    
                // 有多张的话还可以接着传
                case EgmServiceCode.TRANSACTION_PICTURE_FILE_TOO_BIG:  // 照片大小超过限制
                case EgmServiceCode.TRANSACTION_PICTURE_SIZE_ILLEGAL:  // 图片尺寸不符合要求
                default:
                    mUploadIndex++;
                    doUploadPicture();
                    break;
            }
	    }
	};
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			BitmapExecutorService.closeService();
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
