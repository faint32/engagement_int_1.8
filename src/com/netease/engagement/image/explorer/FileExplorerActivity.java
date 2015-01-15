package com.netease.engagement.image.explorer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.image.explorer.adapter.FileExplorerBean;
import com.netease.engagement.image.explorer.adapter.FileExplorerListAdapter;
import com.netease.engagement.widget.CustomActionBar;

public class FileExplorerActivity extends ActivityEngagementBase {
    public static final String EXTRA_MAX_COUNT = "max_count";
    public static final String EXTRA_PICTURE_TYPE = "picture_type";
    public static final String EXTRA_PICTURE_MAX_SIZE = "picture_max_size";
    public static final String EXTRA_PICTURE_MIN_SIZE = "picture_min_size";
	
	private ListView fileExplorerLv;
	
	private FileExplorerListAdapter mAdapter;

	private List<FileExplorerBean> list = new ArrayList<FileExplorerBean>();
	
	private int maxCount = 1; //控制从不同地方进入图片上传时的允许上传最多张数
	
	private int photoType; //私照 or 公照
	
	private int maxSize, minSize;  // 照片的最大最小尺寸
	
	//用于记录已出现过的BUCKET_DISPLAY_NAME
	private Set<String> set = new HashSet<String>();
	
	/** 获取一张图片 */
    public static void startForSelectPicture(FragmentBase fragment, int picType, int requestCode, int maxSize, int minSize){
        Intent intent = new Intent(fragment.getActivity(), FileExplorerActivity.class);
        intent.putExtra(EXTRA_PICTURE_TYPE, picType);
        intent.putExtra(EXTRA_PICTURE_MAX_SIZE, maxSize);
        intent.putExtra(EXTRA_PICTURE_MIN_SIZE, minSize);
        
        fragment.startActivityForResult(intent, requestCode);
    }
    
    /** 获取一张图片 */
    public static void startForSelectPicture(Activity activity, int picType, int requestCode, int maxSize, int minSize){
        Intent intent = new Intent(activity, FileExplorerActivity.class);
        intent.putExtra(EXTRA_PICTURE_TYPE, picType);
        intent.putExtra(EXTRA_PICTURE_MAX_SIZE, maxSize);
        intent.putExtra(EXTRA_PICTURE_MIN_SIZE, minSize);
        
        activity.startActivityForResult(intent, requestCode);
    }
    
    /**
     * for聊天界面发送公开照片
     */
    public static void startForUploadPicture(FragmentBase fragment ,int picType ,int requestCode, int maxSize, int minSize){
    	Intent intent = new Intent(fragment.getActivity(),FileExplorerActivity.class);
    	intent.putExtra(EXTRA_PICTURE_TYPE, picType);
    	intent.putExtra(EXTRA_PICTURE_MAX_SIZE, maxSize);
        intent.putExtra(EXTRA_PICTURE_MIN_SIZE, minSize);
    	fragment.startActivityForResult(intent,requestCode);
    }
    
    /** 上传图片 */
    public static void startForUploadPicture(Context context, int picType, int maxCount, int maxSize, int minSize){
        Intent intent = new Intent(context, FileExplorerActivity.class);
        intent.putExtra(EXTRA_MAX_COUNT, maxCount);
        intent.putExtra(EXTRA_PICTURE_TYPE, picType);
        intent.putExtra(EXTRA_PICTURE_MAX_SIZE, maxSize);
        intent.putExtra(EXTRA_PICTURE_MIN_SIZE, minSize);
        
        context.startActivity(intent);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		CustomActionBar actionBar = this.getCustomActionBar();
		actionBar.setMiddleTitle(R.string.album);
		actionBar.setRightVisibility(View.INVISIBLE);
		actionBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
		
		setContentView(R.layout.file_explorer);
		
		initUI();
		initData();
	}
	
	private void initUI() {
	    Intent intent = getIntent();
		maxCount = intent.getIntExtra(EXTRA_MAX_COUNT, 1);
		photoType = intent.getIntExtra(EXTRA_PICTURE_TYPE, -1);
		maxSize = intent.getIntExtra(EXTRA_PICTURE_MAX_SIZE, EgmConstants.SIZE_MAX_PICTURE);
		minSize = intent.getIntExtra(EXTRA_PICTURE_MIN_SIZE, EgmConstants.SIZE_MIN_PICTURE);
		
		fileExplorerLv = (ListView) findViewById(R.id.fileExplorerLv);
		fileExplorerLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				if(photoType == EgmConstants.Photo_Type.TYPE_AVATAR){
				    PhotoUploadActivity.startActivityForSelectPicture(FileExplorerActivity.this, list.get(position).getDisplayName(), 
				            maxCount, photoType, maxSize, minSize, EgmConstants.REQUEST_SELECT_PICTURE);
				}
				else if(photoType == EgmConstants.Photo_Type.TYPE_CHAT_PUBLIC_PIC){
					PhotoUploadActivity.startActivityForSelectPicture(FileExplorerActivity.this, list.get(position).getDisplayName(), 
				            maxCount, photoType, maxSize, minSize, EgmConstants.REQUEST_SELECT_PICTURE);
				}
				else{
				    PhotoUploadActivity.startActivity(FileExplorerActivity.this, list.get(position).getDisplayName(),
				            maxCount, photoType, maxSize, minSize);
				    finish();
				}
				
			}
		});
	}
	

	private void initData() {
		Cursor cursor;
		cursor = getCursor();
		
		if(cursor != null) {
			String displayName;
			long origID;
			int picCount;
			String data;
			
			while(cursor.moveToNext()) {
				displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
				origID = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
				data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				picCount = cursor.getInt(cursor.getColumnIndex("count(" + MediaStore.Images.Media._ID + ")"));
				
				if(!set.contains(displayName)) {
					set.add(displayName);
					list.add(new FileExplorerBean(displayName, origID, picCount, data));
				}
			}
			cursor.close();
			
			if(list != null && list.size() > 0) {
				mAdapter = new FileExplorerListAdapter(this, list);
				fileExplorerLv.setAdapter(mAdapter);
			}
		}
	}
	
	private Cursor getCursor() {
		Cursor cursor;
		String[] projection = {
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA,
				"count(" + MediaStore.Images.Media._ID + ")"
		};
		
		String selection = " 0 == 0) group by " 
				+ MediaStore.Images.Media.BUCKET_DISPLAY_NAME +" -- (";

		
		CursorLoader cLoader = new  CursorLoader(this,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection, selection, null, null);
		cursor = cLoader.loadInBackground();
		
		return cursor;
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            setResult(Activity.RESULT_CANCELED, data);
        }
        else if(requestCode == EgmConstants.REQUEST_SELECT_PICTURE){
            setResult(Activity.RESULT_OK, data);
            finish();
        }
        
        
	}
}
