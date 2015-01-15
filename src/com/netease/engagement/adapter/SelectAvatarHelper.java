package com.netease.engagement.adapter;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityUtil;
import com.netease.engagement.activity.ImageCropActivity;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.image.cropimage.ActivityCropImage;
import com.netease.engagement.image.explorer.FileExplorerActivity;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.EgmDBProviderExport;


/**
 * 选择头像辅助类
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class SelectAvatarHelper {
    /** 调用该类的使用者，activity或者fragment */
    private Object mMaster;
    private Context mContext;
    private int mMin, mMax;
    public String mCameraImgId = null;
    private AlertDialog mMenuDialog;
    /** 是否要进行裁剪 */
    private boolean mIsCrop = true;
    private String mDialogTitle;
    
    /** 头像原图文件的路径 */
    public String mAvatarPath = null;
    /** 裁剪后的头像 */
    public Bitmap mCropAvatar = null;
    /** 头像裁剪坐标：x,y,w,h */
    public String[] mAvatarCoor = null;
    
    
    /** 头像原图文件的路径拷贝 */
    public String oldmAvatarPath = null;
    /** 头像裁剪坐标：x,y,w,h 拷贝*/
    public String[] oldmAvatarCoor = null;
    
    
    public SelectAvatarHelper(Object context, int max, int min, boolean isCrop){
        mMaster = context;
        mMin = min;
        mMax = max;
        mIsCrop = isCrop;
        
        if(mMaster instanceof Activity){
            mContext = (Activity)context;
        }
        else if(mMaster instanceof FragmentBase){
            mContext = ((FragmentBase)context).getActivity();
        }
        else{
            return;
        }
        
        mDialogTitle = mContext.getString(R.string.reg_title_upload_avatar);
    }
    
    /** 初始化公有数据，防止生命周期内下次使用时用到上次的数据 */
    public void init(){
        mCropAvatar = null;
        mAvatarCoor = oldmAvatarCoor;
        mAvatarPath = oldmAvatarPath;
    }
    
    public void setDialogTitle(String title){
        mDialogTitle = title;
    }
    
    public void changeAvatar(){
        init(); // 清掉上一次的数据
        
        if(mMenuDialog == null){
            mMenuDialog = EgmUtil.createEgmMenuDialog(mContext, 
                    mDialogTitle, 
                    mContext.getResources().getStringArray(R.array.register_change_avatar), 
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int which = (Integer)view.getTag();
                            
                            switch (which) {
                            case 0:
                                changeAvatarFromCamera();
                                break;
                            case 1:
                                changeAvatarFromAlbum();
                                break;
                            default:
                                break;
                            }
                            
                            if(mMenuDialog.isShowing()){
                                mMenuDialog.dismiss();
                            }
                        }
                    });
        }
        
        mMenuDialog.setCanceledOnTouchOutside(false);
        mMenuDialog.show();
    }

    private void changeAvatarFromCamera(){
        mCameraImgId = String.valueOf(System.currentTimeMillis());  //用来生成图片名称
        
        if(mMaster instanceof Activity){
            ActivityUtil.capturePhotoForResult((Activity)mMaster, 
                    EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mCameraImgId), 
                    EgmConstants.REQUEST_CAPTURE_PHOTO_AVATAR);
        }
        else if(mMaster instanceof FragmentBase){
            ActivityUtil.capturePhotoForResult((FragmentBase)mMaster, 
                    EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mCameraImgId), 
                    EgmConstants.REQUEST_CAPTURE_PHOTO_AVATAR);
        }
    }
    
    private void changeAvatarFromAlbum(){
        if(mMaster instanceof Activity){
            FileExplorerActivity.startForSelectPicture((Activity)mMaster, 
                    EgmConstants.Photo_Type.TYPE_AVATAR, 
                    EgmConstants.REQUEST_SELECT_PICTURE_AVATAR, 
                    EgmConstants.SIZE_MAX_PICTURE, EgmConstants.SIZE_MIN_PICTURE);
        }
        else if(mMaster instanceof FragmentBase){
            FileExplorerActivity.startForSelectPicture((FragmentBase)mMaster, 
                    EgmConstants.Photo_Type.TYPE_AVATAR, 
                    EgmConstants.REQUEST_SELECT_PICTURE_AVATAR, 
                    EgmConstants.SIZE_MAX_PICTURE, EgmConstants.SIZE_MIN_PICTURE);
        }
        
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {

            mAvatarCoor=oldmAvatarCoor;
            mAvatarPath=oldmAvatarPath;
            return;
        }
        
        switch(requestCode){
            case EgmConstants.REQUEST_SELECT_PICTURE_AVATAR:   // 从相册中选取
            case EgmConstants.REQUEST_CAPTURE_PHOTO_AVATAR:    // 从相机拍照
                Uri uri;
                
                if(requestCode == EgmConstants.REQUEST_SELECT_PICTURE_AVATAR){
                    if(data == null){
                        Toast.makeText(mContext, R.string.reg_tip_avatar_get_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uri = data.getData();        //获得图片的uri 
                }
                else{
                	if (TextUtils.isEmpty(mCameraImgId)) {
            			return;
            		}
                	
                    uri = Uri.fromFile(new File(EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA, mCameraImgId)));
                }
                
                // 判断图片是否符合尺寸要求，过大的图片进行缩小
                Bitmap result = ImageUtil.legitimateImageSize(mContext, uri, mMax, mMin);
                if(result == null){
                    Toast.makeText(mContext, R.string.reg_tip_avatar_too_small, Toast.LENGTH_SHORT).show();
                }
                else{
                    mAvatarPath = ImageUtil.getBitmapFilePath(result, EgmConstants.TEMP_PROFILE_NAME);// 要先存到本地，否则图片过大会失败
                    
                    if(mIsCrop){
                        if(mMaster instanceof Activity){
                        	ImageCropActivity.actionGetCropImage((Activity)mMaster, EgmConstants.REQUEST_CROP_IMAGE, mAvatarPath, EgmConstants.SIZE_MIN_AVATAR_CROPED);
                        	//ActivityCropImage.actionGetCropImage((Activity)mMaster, EgmConstants.REQUEST_CROP_IMAGE, mAvatarPath, false, mMin);// 图片裁剪
                        }
                        else if(mMaster instanceof FragmentBase){
                        	ImageCropActivity.actionGetCropImage((FragmentBase)mMaster, EgmConstants.REQUEST_CROP_IMAGE, mAvatarPath,EgmConstants.SIZE_MIN_AVATAR_CROPED);
                          //  ActivityCropImage.actionGetCropImage((FragmentBase)mMaster, EgmConstants.REQUEST_CROP_IMAGE, mAvatarPath, false, mMin);// 图片裁剪
                        }
                    }
                    else{
                        mCropAvatar = result;
                    }
                }
                break;
                
            case EgmConstants.REQUEST_CROP_IMAGE:   // 裁剪后的图片
                if(data == null){
                    Toast.makeText(mContext, R.string.reg_tip_avatar_get_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Bundle bundle = data.getExtras();
                String coordination = bundle.getString(ActivityCropImage.CROP_COORDINATE);
                
                // 裁剪失败               
                if (coordination == null)
                    return;
                
                mAvatarCoor = coordination.split("&");
                oldmAvatarCoor=mAvatarCoor;
                
                String path = data.getExtras().getString(ActivityCropImage.EXTRA_DATA);
                oldmAvatarPath=path;
                
                mCropAvatar = ImageUtil.getBitmapFromFile(path);

                break;
        }
    }
}
