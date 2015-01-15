package com.netease.engagement.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.view.ProfileView;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.service.protocol.meta.SearchUserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;
import com.netease.service.stat.EgmStat;


public class SearchUserListAdapter extends BaseAdapter {
    
    private Context mContext;
    private int mSexType;
    private ArrayList<SearchUserInfo> mDataList;
    private UserInfoConfig mUserConfig;
    
    /**
     * 构造函数 
     * @param context
     * @param sexType 女性列表或男性列表，不是当前用户的性别
     */
    public SearchUserListAdapter(Context context, int sexType){
        mContext = context;
        mSexType = sexType;
        mUserConfig = ConfigDataManager.getInstance().getUConfigFromData();
    }
    
    public void clearDateList(){
        if(mDataList != null){
            mDataList.clear();
            mDataList = null;
        }
    }
    
    /** 填充数据 */
    public void addDataList(ArrayList<SearchUserInfo> list){
        if(mDataList == null){
            mDataList = list;
        }
        else{
            mDataList.addAll(list);
        }
        
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        
        if(mSexType == EgmConstants.SexType.Male){
            view = getMaleView(position, convertView);
        }
        else{
            view = getFemaleView(position, convertView);
        }
        
        return view;
    }
    
    /** 生成男性排行列表item view */
    private View getMaleView(int position, View convertView){
        MaleViewHolder holder = null;
        SearchUserInfo info = mDataList.get(position);
        
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_rank_male, null, false);
            
            holder = new MaleViewHolder();
            holder.mProfile = (HeadView)convertView.findViewById(R.id.rank_male_profile);
            holder.mNickName = (TextView)convertView.findViewById(R.id.rank_male_nickname);
            holder.mAge = (TextView)convertView.findViewById(R.id.rank_male_age);
            holder.mLevel = (TextView)convertView.findViewById(R.id.rank_male_level);
            holder.mNumberTag = (TextView)convertView.findViewById(R.id.rank_number_tag);
            
            holder.mNumberTag.setVisibility(View.GONE);
            
            convertView.setTag(holder);
        }
        else{
            holder = (MaleViewHolder)convertView.getTag();
        }
        
        if(position % 2 == 0){  // 偶数
            convertView.setBackgroundResource(R.drawable.btn_common_bg_selector);
        }
        else{
            convertView.setBackgroundResource(R.drawable.bg_rank_item_even);
        }
        
        holder.mUid = info.uid;
        holder.position=position;
        holder.mProfile.setImageUrl(info.isVip, HeadView.PROFILE_SIZE_SMALL, info.portraitUrl192,
        		EgmConstants.SexType.Male);
        holder.mNickName.setText(info.nick);
        if(info.usercp > 0){
            holder.mAge.setText(mContext.getString(R.string.rec_age_haoqi, info.age, info.usercp));
        }
        else{
            holder.mAge.setText(mContext.getString(R.string.rec_female_age, info.age));
        }
        holder.mLevel.setText(UserInfoUtil.getSearchMaleLevel(info));
        EgmStat.log(EgmStat.LOG_IMPRESS_SEARCH, EgmStat.SCENE_SEARCH, holder.mUid, position); 
        return convertView;
    }
    
    /** 
     * 生成女性列表item view。
     */
    private View getFemaleView(int position, View convertView){
        FemaleViewHolder holder = null;
        SearchUserInfo info = mDataList.get(position);
        
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_list_female, null, false);
            holder = new FemaleViewHolder();
            
            holder.mProfile = (HeadView)convertView.findViewById(R.id.rank_female_profile);
            holder.mNickName = (TextView)convertView.findViewById(R.id.rank_female_nickname);
            holder.mAgeHeight = (TextView)convertView.findViewById(R.id.rank_female_age_height);
            holder.mCharmValue = (TextView)convertView.findViewById(R.id.rank_female_charm);
            holder.mPictureCount = (TextView)convertView.findViewById(R.id.rank_female_pic_count);
            holder.mRightPart = convertView.findViewById(R.id.rank_right_part);
            
            // 魅力值
            holder.mCharmValue.setVisibility(View.GONE);
            holder.mRightPart.setVisibility(View.GONE);
            
            convertView.setTag(holder);
        }
        else{
            holder = (FemaleViewHolder)convertView.getTag();
        }
        
        if(position % 2 == 0){  // 偶数
            convertView.setBackgroundResource(R.drawable.btn_common_bg_selector);
        }
        else{
            convertView.setBackgroundResource(R.drawable.bg_rank_item_even);
        }
        
        holder.mUid = info.uid;
        holder.position=position;
        
        // 头像
        holder.mProfile.setImageUrl(info.isVip, HeadView.PROFILE_SIZE_SMALL, info.portraitUrl192,
        		EgmConstants.SexType.Female);
        
        // 昵称
        holder.mNickName.setText(info.nick);
        
        // 年龄和身高和身材
        String detail = UserInfoUtil.getDetailStr2(mContext, mUserConfig, info.age, info.height, info.bust, info.cup, info.waist, info.hip);
        holder.mAgeHeight.setText(detail);
        
        // 私照数
        if(info.privatePhotoCount > 0){
            holder.mPictureCount.setVisibility(View.VISIBLE);
            holder.mPictureCount.setText(mContext.getString(R.string.rank_private_pic_count, info.privatePhotoCount));
        }
        else{
            holder.mPictureCount.setVisibility(View.GONE);
        }
		EgmStat.log(EgmStat.LOG_IMPRESS_SEARCH, EgmStat.SCENE_SEARCH, holder.mUid, position);     
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
    
    public class MaleViewHolder {
        public HeadView mProfile;
        public TextView mNickName;
        public TextView mAge;
        public TextView mLevel;
        public TextView mNumberTag;
        
        public long mUid;    // 点击时可以从view的tag里取到uid
        public int position;//点击时获取点击位置，方便日志统计
    }
    
    public class FemaleViewHolder {
        public HeadView mProfile;
        public TextView mNickName;
        public TextView mPictureCount;
        public TextView mAgeHeight; // 年龄和身高
        public TextView mCharmValue;// 魅力值
        public View mRightPart;
        
        public long mUid;    // 点击时可以从view的tag里取到uid
        public int position;//点击时获取点击位置，方便日志统计
    }
}

