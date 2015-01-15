package com.netease.engagement.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.view.ProfileView;
import com.netease.engagement.widget.ProgerssImageView;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.service.Utils.DeviceUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.meta.RecommendUserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;
import com.netease.service.stat.EgmStat;


public class RecommendListAdapter extends BaseAdapter {
    private final float PIC_RATE = 0.75f;
    
    private Context mContext;
    private int mSexType;
    private UserInfoConfig mUserConfig;
    private ArrayList mDataList;
    private View.OnClickListener mClickUser;
    
    private int mPicWidthBig, mPicHeightBig;    // 女性大图图片尺寸
    private int mPicWidthSmall, mPicHeightSmall;// 女性小图图片尺寸
    
    /**
     * 构造函数 
     * @param context
     * @param sexType 女性列表或男性列表，不是当前用户的性别
     */
    public RecommendListAdapter(Context context, int sexType, View.OnClickListener clickUser){
        mContext = context;
        mSexType = sexType;
        mUserConfig = ConfigDataManager.getInstance().getUConfigFromData();
        mClickUser = clickUser;
        
        // 初始化图片尺寸
        mPicWidthBig = DeviceUtil.getScreenWidth(context);
        mPicHeightBig = (int)(mPicWidthBig * PIC_RATE);
        
        mPicWidthSmall = mPicWidthBig / 2;
        mPicHeightSmall = (int) (mPicWidthSmall * PIC_RATE);
    }
    
    /** 填充数据 */
    public void setDataList(ArrayList<RecommendUserInfo> list){
        if(mSexType == EgmConstants.SexType.Male){
            mDataList = list;
        }
        else{   // 女性数据要组装成1大图4小图的结构
            ArrayList<FemaleData> femaleDataList = new ArrayList<FemaleData>();
            int size = list.size();
            
            for(int i = 0; i < size; ){
                FemaleData data = new FemaleData();
                if(i % 5 == 0){ // 1大图
                    data.mLeftInfo = list.get(i);
                    data.mLeftPos = i;
                    i++;
                }
                else{   // 4小图
                    data.mLeftInfo = list.get(i);
                    data.mLeftPos = i;
                    i++;
                    
                    if(i < size){
                        data.mRightInfo = list.get(i);
                        data.mRightPos = i;
                        i++;
                    }
                }
                
                femaleDataList.add(data);
            }
            
            mDataList = femaleDataList;
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
    
    /** 生成男性推荐列表item view */
    private View getMaleView(int position, View convertView){
        MaleViewHolder holder = null;
        RecommendUserInfo info = (RecommendUserInfo)mDataList.get(position);
        
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_male, null, false);
            
            holder = new MaleViewHolder();
            holder.mProfile = (HeadView)convertView.findViewById(R.id.recommend_male_profile);
            holder.mNickName = (TextView)convertView.findViewById(R.id.recommend_male_nickname);
            holder.mAge = (TextView)convertView.findViewById(R.id.recommend_male_age);
            holder.mLevel = (TextView)convertView.findViewById(R.id.recommend_male_level);
            
            convertView.setTag(holder);
            convertView.setOnClickListener(mClickUser);
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
        holder.position=position;//获取位置
        holder.mAlg=info.alg; 
        holder.imgSize=EgmConstants.Img_Size.IMG_SIZE_SMALL;
        holder.mProfile.setImageUrl(info.isVip, HeadView.PROFILE_SIZE_SMALL, info.portraitUrl192,
        		info.sex);
        holder.mNickName.setText(info.nick);
        if(info.usercp > 0){
            holder.mAge.setText(mContext.getString(R.string.rec_age_haoqi, info.age, info.usercp));
        }
        else{
            holder.mAge.setText(mContext.getString(R.string.rec_female_age, info.age));
        }
        holder.mLevel.setText(UserInfoUtil.getRecommendMaleLevel(info));
        
		EgmStat.log(EgmStat.LOG_IMPRESS_MAINPAGE, EgmStat.SCENE_MAINPAGE, holder.mUid, position, EgmStat.SIZE_SMALL,holder.mAlg); 
		
        return convertView;
    }
    
    /** 
     * 生成女性推荐列表item view。
     * 女性列表格式为1大4小，为了view的复用，列表的每一项(item)都为左右两个部分。
     * 大的隐藏掉右半边，小的一左一右。
     */
    private View getFemaleView(int position, View convertView){
        FemaleViewHolder holder = null;
        FemaleData info = (FemaleData)mDataList.get(position);
        
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_female, null, false);
            holder = new FemaleViewHolder();
            
            holder.mLeft = convertView.findViewById(R.id.recommend_female_left);
            setFemaleSmallHolder(holder.mLeftPart, holder.mLeft);
            
            holder.mRight = convertView.findViewById(R.id.recommend_female_right);
            setFemaleSmallHolder(holder.mRightPart, holder.mRight);
            
            convertView.setTag(holder);
        }
        else{
            holder = (FemaleViewHolder)convertView.getTag();
        }
        
        if(info.mRightInfo == null){    // 大图
            holder.mRight.setVisibility(View.GONE);
            setFemaleSmallData(holder.mLeftPart, info.mLeftInfo, true);
            holder.mLeftPart.position=info.mLeftPos;
            holder.mLeftPart.imgSize=EgmConstants.Img_Size.IMG_SIZE_BIG;
            holder.mLeftPart.mAlg=info.mLeftInfo.alg;
            holder.mLeft.setOnClickListener(mClickUser);
			EgmStat.log(EgmStat.LOG_IMPRESS_MAINPAGE, EgmStat.SCENE_MAINPAGE, info.mLeftInfo.uid,
					holder.mLeftPart.position, EgmStat.SIZE_BIG,holder.mLeftPart.mAlg);
        }
        else{   // 小图
            holder.mRight.setVisibility(View.VISIBLE);
            setFemaleSmallData(holder.mLeftPart, info.mLeftInfo, false);
            setFemaleSmallData(holder.mRightPart, info.mRightInfo, false);
           
            holder.mLeftPart.position=info.mLeftPos;
            holder.mLeftPart.imgSize=EgmConstants.Img_Size.IMG_SIZE_SMALL;
            holder.mLeftPart.mAlg=info.mLeftInfo.alg; 
            
            holder.mRightPart.position=info.mRightPos;
            holder.mRightPart.imgSize=EgmConstants.Img_Size.IMG_SIZE_SMALL;
            holder.mRightPart.mAlg=info.mRightInfo.alg;
            
			EgmStat.log(EgmStat.LOG_IMPRESS_MAINPAGE, EgmStat.SCENE_MAINPAGE, info.mLeftInfo.uid,
					holder.mLeftPart.position, EgmStat.SIZE_SMALL,holder.mLeftPart.mAlg);
			EgmStat.log(EgmStat.LOG_IMPRESS_MAINPAGE, EgmStat.SCENE_MAINPAGE, info.mRightInfo.uid,
					holder.mRightPart.position, EgmStat.SIZE_SMALL,holder.mRightPart.mAlg);
  
            holder.mLeft.setOnClickListener(mClickUser);
            holder.mRight.setOnClickListener(mClickUser);
        }
        
        return convertView;
    }
    
    /** 女性推荐列表半个item的viewHolder初始化 */
    private void setFemaleSmallHolder(FemaleSmallViewHolder holder, View smallView){
        holder.mProfile = (ProgerssImageView)smallView.findViewById(R.id.recommend_female_profile);
        holder.mPictureCount = (TextView)smallView.findViewById(R.id.recommend_female_picture_count);
        holder.mNickName = (TextView)smallView.findViewById(R.id.recommend_female_nickname);
        holder.mLevel = (TextView)smallView.findViewById(R.id.recommend_female_level);
        holder.mDetail = (TextView)smallView.findViewById(R.id.recommend_female_detail);
        holder.mNew = (TextView)smallView.findViewById(R.id.recommend_female_new);
        
        holder.mProfile.mImageView.setScaleType(ScaleType.FIT_XY);
        holder.mProfile.mImageView.setDefaultResId(R.drawable.icon_photo_loaded_fail_with_bg);
        
        smallView.setTag(holder);
    }
    
    /** 女性推荐列表半个item填充数据 */
    private void setFemaleSmallData(FemaleSmallViewHolder holder, RecommendUserInfo info, boolean isBig){
        holder.mUid = info.uid;
        
        // 私照数
        if(info.privatePhotoCount > 0){
            holder.mPictureCount.setVisibility(View.VISIBLE);
            String count = EgmUtil.trimWithEllipsis(String.valueOf(info.privatePhotoCount), 5);
            holder.mPictureCount.setText(mContext.getString(R.string.rec_private_pic_count, count));
        }
        else{
            holder.mPictureCount.setVisibility(View.INVISIBLE);
        }
        
        // 昵称
        holder.mNickName.setText(info.nick);
        if(info.isNew){ // 新用户标识
            holder.mNew.setVisibility(View.VISIBLE);
        }
        else{
            holder.mNew.setVisibility(View.GONE);
        }
        
        // 头像
        LayoutParams lp = holder.mProfile.getLayoutParams();
        if(isBig){  // 大图
            lp.width = mPicWidthBig;
            lp.height = mPicHeightBig;
            
            // 详情
            String detail = UserInfoUtil.getDetailStr(mContext, mUserConfig, info.age, info.height, 
                    info.bust, info.cup, info.waist, info.hip);
            holder.mDetail.setText(detail);
            
            // 等级
            holder.mLevel.setVisibility(View.VISIBLE);
            holder.mLevel.setText(mContext.getString(R.string.rec_female_level, info.level, info.levelName));
        }
        else{   // 小图
            lp.width = mPicWidthSmall;
            lp.height = mPicHeightSmall;
            
            // 详情
            String detail = UserInfoUtil.getDetailStr(mContext, null, info.age, info.height, 0, 0, 0, 0);
            holder.mDetail.setText(detail);
            
            // 等级
            holder.mLevel.setVisibility(View.INVISIBLE);
        }
        
        holder.mProfile.mImageView.setLoadingImage(null);   // 先清掉复用view的缓存
        holder.mProfile.mImageView.setServerClipSize(lp.width, lp.height);
        holder.mProfile.mImageView.setLoadingImage(info.portraitUrl640);
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
        
        public long mUid;    // 点击时可以从view的tag里取到uid
        public int position;//点击时获取点击位置，方便日志统计
        public int imgSize;//尺寸
        public String mAlg;//尺寸
    }
    
    public class FemaleViewHolder {
        public View mLeft, mRight;
        public FemaleSmallViewHolder mLeftPart;
        public FemaleSmallViewHolder mRightPart;
        
        public FemaleViewHolder(){
            mLeftPart = new FemaleSmallViewHolder();
            mRightPart = new FemaleSmallViewHolder();
        }
    }
    
    public class FemaleSmallViewHolder {
        public ProgerssImageView mProfile;
        public TextView mPictureCount;
        public TextView mNickName;
        public TextView mLevel;
        public TextView mDetail;
        public TextView mNew;
        
        public int position;//点击时获取点击位置，方便日志统计
        public int imgSize;//尺寸
        public String mAlg;//推荐等级
        
        public long mUid;    // 点击时可以从view的tag里取到uid
    }
    
    public class FemaleData{
        public RecommendUserInfo mLeftInfo;
        public int mLeftPos;
        public RecommendUserInfo mRightInfo;
        public int mRightPos;
    }
}

