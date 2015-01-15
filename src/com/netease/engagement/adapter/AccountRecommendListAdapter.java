package com.netease.engagement.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.widget.ProgerssImageView;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.service.Utils.DeviceUtil;
import com.netease.service.protocol.meta.RecommendUserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;

/**
 * 登录前的推荐列表Adapter
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class AccountRecommendListAdapter extends BaseAdapter {
    private final float PIC_RATE = 0.75f;
    
    private Context mContext;
    private ArrayList<RecommendUserInfo> mDataList;
    private int mPicWidthBig, mPicHeightBig;    // 女性大图图片尺寸
    private UserInfoConfig mUserConfig;
    
    public AccountRecommendListAdapter(Context context){
        mContext = context;
        // 初始化图片尺寸
        mPicWidthBig = DeviceUtil.getScreenWidth(context);
        mPicHeightBig = (int)(mPicWidthBig * PIC_RATE);
        mUserConfig = ConfigDataManager.getInstance().getUConfigFromData();
    }
    
    /** 填充数据 */
    public void setDataList(ArrayList<RecommendUserInfo> list){
        mDataList = list;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FemaleViewHolder holder = null;
        RecommendUserInfo info = mDataList.get(position);
        
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_female_small, null, false);
            convertView.setBackgroundResource(R.drawable.item_empty_selector);
            
            holder = new FemaleViewHolder();
            holder.mProfile = (ProgerssImageView)convertView.findViewById(R.id.recommend_female_profile);
            holder.mPictureCount = (TextView)convertView.findViewById(R.id.recommend_female_picture_count);
            holder.mNickName = (TextView)convertView.findViewById(R.id.recommend_female_nickname);
            holder.mLevel = (TextView)convertView.findViewById(R.id.recommend_female_level);
            holder.mDetail = (TextView)convertView.findViewById(R.id.recommend_female_detail);
            holder.mNew = (TextView)convertView.findViewById(R.id.recommend_female_new);
            
            holder.mProfile.mImageView.setScaleType(ScaleType.CENTER_CROP);
            
            LayoutParams lp = holder.mProfile.getLayoutParams();
            lp.width = mPicWidthBig;
            lp.height = mPicHeightBig;
            
            convertView.setTag(holder);
        }
        else{
            holder = (FemaleViewHolder)convertView.getTag();
        }
        
        // 头像
        holder.mProfile.mImageView.setServerClipSize(mPicWidthBig, mPicHeightBig);
        holder.mProfile.mImageView.setLoadingImage(info.portraitUrl640);
        
        // 昵称
        holder.mNickName.setText(info.nick);
        if(info.isNew){ // 新用户标识
            holder.mNew.setVisibility(View.VISIBLE);
        }
        else{
            holder.mNew.setVisibility(View.GONE);
        }
        
        // 私照数
        if(info.privatePhotoCount > 0){
            holder.mPictureCount.setVisibility(View.VISIBLE);
            holder.mPictureCount.setText(mContext.getString(R.string.rec_private_pic_count, 
            		String.valueOf(info.privatePhotoCount)));
        }
        else{
            holder.mPictureCount.setVisibility(View.INVISIBLE);
        }
        
        // 等级
        holder.mLevel.setText(mContext.getString(R.string.rec_female_level, info.level, info.levelName));
        
        // 详情
        String detail = UserInfoUtil.getDetailStr(mContext, mUserConfig, info.age, info.height, 
                info.bust, info.cup, info.waist, info.hip);
        holder.mDetail.setText(detail);
        
        return convertView;
    }
    
    @Override
    public int getCount() {
        int count = 0;
        
        if(mDataList != null){
            count = mDataList.size();
        }
        
        return count;
    }

    @Override
    public Object getItem(int position) {
        Object item = null;
        
        if(mDataList != null && position >= 0 && position < mDataList.size()){
            item = mDataList.get(position);
        }
        
        return item;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public class FemaleViewHolder {
        public ProgerssImageView mProfile;
        public TextView mPictureCount;
        public TextView mNickName;
        public TextView mLevel;
        public TextView mDetail;
        public TextView mNew;
    }
}
