package com.netease.engagement.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentEditPhoto;
import com.netease.engagement.fragment.FragmentEditPhoto.PicUpLoadMode;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.UserInfo;

/**
 * @author lishang
 *1、照片统一上传入口，照片数目可以自己限定，也可采用App默认的
 *2、不处理onActivityResult,所有处理拦截到本层。
 */


public class ActivityPicUploadEntrance extends ActivityEngagementBase {

    
    private final static String PIC_TYPE="pic_type";
    private final static String PIC_UPLOAD_MODE="pic_upload_mode";
    private final static String SERIAL_MAX_COUNT="serial_max_count";
    private final static String MAX_SIZE="max_size";
    private final static String MIN_SIZE="min_size";

    private LinearLayout rootLayout;
    private boolean mPicType;
    private int mUpLoadMode;
    private int Tid;

    public interface PicType {
        public static final boolean PUBLIC_PIC = false;
        public static final boolean PRIVATE_PIC = true;

    }

    public interface ActionMode {

        int DEFAULT_MODE = 0;
        int LIMITE_MODE = 1;
    }
    public static void startActivity(Context context, boolean type) {

        Intent intent = new Intent(context, ActivityPicUploadEntrance.class);
        intent.putExtra(PIC_TYPE, type);
        intent.putExtra(PIC_UPLOAD_MODE, ActionMode.DEFAULT_MODE);
        context.startActivity(intent);
    }

    
    /**
     * 限制上传图片的数量与尺寸
     */
    private int mSerialMaxCount;
    private int mMaxSize;
    private int mMinSize;
    private int mType;

    /**
     * 限制上传图片的数量与尺寸
     * 
     * @param context
     * @param type
     * @param maxSerialCount
     * @param maxSize
     * @param minSize
     */
    public static void startActivityWithLimited(Context context, boolean type, int maxSerialCount,
            int maxSize, int minSize) {

        Intent intent = new Intent(context, ActivityPicUploadEntrance.class);
        intent.putExtra(PIC_TYPE, type);
        intent.putExtra(PIC_UPLOAD_MODE, ActionMode.LIMITE_MODE);
        intent.putExtra(SERIAL_MAX_COUNT, maxSerialCount);
        intent.putExtra(MAX_SIZE, maxSize);
        intent.putExtra(MIN_SIZE, minSize);
        
        context.startActivity(intent);
    }
    
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        
        Intent intent = getIntent();
        if (intent == null)
            return;
        
        mPicType = intent.getBooleanExtra(PIC_TYPE, false);
        mUpLoadMode=intent.getIntExtra(PIC_UPLOAD_MODE, ActionMode.DEFAULT_MODE);
        if(mUpLoadMode==ActionMode.LIMITE_MODE){
            mSerialMaxCount=intent.getIntExtra(SERIAL_MAX_COUNT, EgmConstants.COUNT_MAX_UPLOAD_PICTURE);
            mMaxSize=intent.getIntExtra(MAX_SIZE, EgmConstants.SIZE_MAX_PICTURE);
            mMinSize=intent.getIntExtra(MIN_SIZE, EgmConstants.SIZE_MIN_PICTURE);
            mType = (mPicType == PicType.PRIVATE_PIC) ? EgmConstants.Photo_Type.TYPE_PRIVATE
                    : EgmConstants.Photo_Type.TYPE_PUBLIC;
        }
        rootLayout = new LinearLayout(this);
        rootLayout.setId(R.id.pic_uploat_layout_id);
        setContentView(rootLayout);
        EgmService.getInstance().addListener(mCallBack);

        Tid = EgmService.getInstance().doGetPrivateData();

    };

    EgmCallBack mCallBack = new EgmCallBack() {

        public void onGetPrivateDataSucess(int transactionId,
                com.netease.service.protocol.meta.UserPrivateData obj) {

            if (Tid != transactionId)
                return;
            
            UserInfo mInfo = obj.userInfo;
            FragmentEditPhoto mFragmentEditPhoto = FragmentEditPhoto
                    .newInstance(UserInfo.toJsonString(mInfo), mPicType);
            
            if (mUpLoadMode == ActionMode.LIMITE_MODE) {
                mFragmentEditPhoto.setPicUpLoadMode(PicUpLoadMode.OTHER_MODE);
                mFragmentEditPhoto.setPicUpLoadLimites(mSerialMaxCount, mMaxSize, mMinSize, mType);
            }

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.pic_uploat_layout_id, mFragmentEditPhoto);
            ft.commit();
        }

        public void onGetPrivateDataError(int transactionId, int errCode,
                String err) {
            
            ToastUtil.showToast(ActivityPicUploadEntrance.this, err);
        };

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EgmService.getInstance().removeListener(mCallBack);
    }
    
}