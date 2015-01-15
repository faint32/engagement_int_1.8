
package com.netease.engagement.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.service.protocol.meta.RankListInfo;
import com.netease.service.protocol.meta.RankUserInfo;
import com.netease.service.stat.EgmStat;

public class RankUserListAdapter extends BaseAdapter {

    private Context mContext;
    private int mSexType;
    private int mRankId;
    private int mRankType;
    private ArrayList<RankUserInfo> mDataList;
    private RankListInfo mListInfo;
    private View.OnClickListener mClickUser;

    /**
     * 构造函数
     * 
     * @param context
     * @param sexType 女性列表或男性列表，不是当前用户的性别
     * @param rankType 日榜还是月榜
     */
    public RankUserListAdapter(Context context, int sexType, int rankId, int rankType,
            View.OnClickListener clickUser) {
        mContext = context;
        mSexType = sexType;
        mRankId = rankId;
        mClickUser = clickUser;
        mRankType = rankType;
    }

    public void clearDateList() {
        if (mDataList != null) {
            mDataList.clear();
            mDataList = null;
        }
    }

    /** 填充数据 */
    public void addDataList(RankListInfo info) {

        mListInfo = info;
        if (mDataList == null) {
            mDataList = info.userList;
        } else {
            mDataList.addAll(info.userList);
        }

        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (mSexType == EgmConstants.SexType.Male) {
            view = getMaleView(position, convertView);
        } else {
            view = getFemaleView(position, convertView);
        }

        return view;
    }

    /** 生成男性排行列表item view */
    private View getMaleView(int position, View convertView) {
        MaleViewHolder holder = null;
        RankUserInfo info = mDataList.get(position);
        String rankListType = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_rank_male, null,
                    false);

            holder = new MaleViewHolder();
            holder.mProfile = (HeadView)convertView.findViewById(R.id.rank_male_profile);
            holder.mNickName = (TextView)convertView.findViewById(R.id.rank_male_nickname);
            holder.mAge = (TextView)convertView.findViewById(R.id.rank_male_age);
            holder.mLevel = (TextView)convertView.findViewById(R.id.rank_male_level);
            holder.mNumberTag = (TextView)convertView.findViewById(R.id.rank_number_tag);

            convertView.setTag(holder);
            convertView.setOnClickListener(mClickUser);
        } else {
            holder = (MaleViewHolder)convertView.getTag();
        }

        if (position % 2 == 0) { // 偶数
            convertView.setBackgroundResource(R.drawable.btn_common_bg_selector);
        } else {
            convertView.setBackgroundResource(R.drawable.bg_rank_item_even);
        }

        holder.mUid = info.uid;
        holder.positon = position;
        holder.mRankId = mRankId;
        holder.mProfile.setImageUrl(info.isVip, HeadView.PROFILE_SIZE_SMALL, info.portraitUrl192,
                EgmConstants.SexType.Male);
        holder.mNickName.setText(info.nick);
        if (mRankId == EgmConstants.RankID.STRENGTH_MALE) {
            // 男性的实力榜不显示豪气值
            holder.mAge.setText(String.format("%d岁", info.age));
        } else {
            if (info.usercp > 0) {
                holder.mAge.setText(mContext.getString(R.string.rec_age_haoqi, info.age,
                        info.usercp));
            } else {
                holder.mAge.setText(mContext.getString(R.string.rec_female_age, info.age));
            }
        }
        holder.mLevel.setText(UserInfoUtil.getRankMaleLevel(info));

        int rank = position + 1;
        holder.mNumberTag.setText(String.valueOf(rank));
        if (rank < 4) { // 前三
            holder.mNumberTag.setBackgroundResource(R.drawable.bg_pgrank_rank_number_yellow);
        } else {
            holder.mNumberTag.setBackgroundResource(R.drawable.bg_pgrank_rank_number_gray);
        }

        if (mRankType == EgmConstants.RankListType.RANK_LIST_DAY) {
            rankListType = EgmStat.RANK_LIST_DAY;
        } else if (mRankType == EgmConstants.RankListType.RANK_LIST_MONTH) {
            rankListType = EgmStat.RANK_LIST_MONTH;
        }
        // 默认处理方式
        if (!isKnownRankId(mRankId)) {
            holder.mAge.setText(mContext.getString(R.string.rec_female_age, info.age));
        }

        holder.logName = mListInfo.logName;
        EgmStat.log(EgmStat.LOG_IMPRESS_RANK, EgmStat.SCENE_TOP_LIST, holder.mUid, position,
                holder.logName, rankListType);
        return convertView;
    }

    /**
     * 生成女性排行列表item view。
     */
    private View getFemaleView(int position, View convertView) {
        FemaleViewHolder holder = null;
        RankUserInfo info = mDataList.get(position);
        String rankListType = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_rank_list_female,
                    null, false);
            holder = new FemaleViewHolder();

            holder.mProfile = (HeadView)convertView.findViewById(R.id.rank_female_profile);
            holder.mCrown = (ImageView)convertView.findViewById(R.id.rank_crown);
            holder.mNickName = (TextView)convertView.findViewById(R.id.rank_female_nickname);
            holder.mLevel = (TextView)convertView.findViewById(R.id.rank_female_level);
            holder.mAgeHeight = (TextView)convertView.findViewById(R.id.rank_female_age_height);
            holder.mCharmValue = (TextView)convertView.findViewById(R.id.rank_female_charm);
            holder.mPictureCount = (TextView)convertView.findViewById(R.id.rank_female_pic_count);
            holder.mNumberTag = (TextView)convertView.findViewById(R.id.rank_number_tag);
            holder.mNewMenber = (TextView)convertView.findViewById(R.id.rank_female_new);

            convertView.setTag(holder);
        } else {
            holder = (FemaleViewHolder)convertView.getTag();
        }

        if (position % 2 == 0) { // 偶数
            convertView.setBackgroundResource(R.drawable.btn_common_bg_selector);
        } else {
            convertView.setBackgroundResource(R.drawable.bg_rank_item_even);
        }

        holder.mUid = info.uid;
        holder.positon = position;
        holder.mRankId = mRankId;
        // 头像
        holder.mProfile.setImageUrl(info.isVip, HeadView.PROFILE_SIZE_SMALL, info.portraitUrl192,
                EgmConstants.SexType.Female);

        if (info.crownId > 0) { // 有皇冠
        	GiftInfoManager.setCrownInfo(info.crownId, false, holder.mCrown);
        	holder.mCrown.setVisibility(View.VISIBLE);
        }
        else {
        	holder.mCrown.setVisibility(View.GONE);
        }

        // 昵称
        holder.mNickName.setText(info.nick);
        if (info.isNew) { // 新用户标识
            holder.mNewMenber.setVisibility(View.VISIBLE);
        } else {
            holder.mNewMenber.setVisibility(View.INVISIBLE);
        }

        // 等级
        holder.mLevel.setText("LV" + info.level + info.levelName);

        // 年龄和身高
        String detail = UserInfoUtil
                .getDetailStr(mContext, null, info.age, info.height, 0, 0, 0, 0);
        holder.mAgeHeight.setText(detail);

        // 魅力值
        if (mRankId == EgmConstants.RankID.STAR) { // 红人榜显示人气值
            holder.mCharmValue.setText(mContext.getString(R.string.rank_visit_value,
                    info.visitTimes));
        } else if (mRankId == EgmConstants.RankID.PRIVATE_PIC_FEMALE) {

            holder.mAgeHeight.setText(mContext.getString(R.string.rank_age, info.age));

            // 私照榜显示私照人气
            if (info.value > 0) {
                holder.mCharmValue.setText(mContext.getString(R.string.rank_private_pic_chamming,
                        info.value));
            } else {
                holder.mCharmValue.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.mCharmValue.setText(mContext.getString(R.string.rank_charm_value, info.usercp));
        }

        // 私照数
        if (info.privatePhotoCount > 0) {
            holder.mPictureCount.setVisibility(View.VISIBLE);
            holder.mPictureCount.setText(mContext.getString(R.string.rank_private_pic_count,
                    info.privatePhotoCount));
        } else {
            holder.mPictureCount.setVisibility(View.GONE);
        }

        int rank = position + 1;
        holder.mNumberTag.setText(String.valueOf(rank));
        if (rank < 4) { // 前三
            holder.mNumberTag.setBackgroundResource(R.drawable.bg_pgrank_rank_number_yellow);
        } else {
            holder.mNumberTag.setBackgroundResource(R.drawable.bg_pgrank_rank_number_gray);
        }
        if (mRankType == EgmConstants.RankListType.RANK_LIST_DAY) {
            rankListType = EgmStat.RANK_LIST_DAY;
        } else if (mRankType == EgmConstants.RankListType.RANK_LIST_MONTH) {
            rankListType = EgmStat.RANK_LIST_MONTH;
        }

        // 默认处理方式
        if (!isKnownRankId(mRankId)) {
            holder.mCharmValue.setVisibility(View.INVISIBLE);
        }
        holder.logName = mListInfo.logName;
        EgmStat.log(EgmStat.LOG_IMPRESS_RANK, EgmStat.SCENE_TOP_LIST, holder.mUid, position,
                holder.logName, rankListType);
        return convertView;
    }

    @Override
    public int getCount() {
        int count = 0;

        if (mDataList != null) {
            count = mDataList.size();
        }

        return count;
    }

    @Override
    public Object getItem(int position) {
        Object item = null;

        if (mDataList != null && position >= 0 && position < mDataList.size()) {
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

        public long mUid; // 点击时可以从view的tag里取到uid
        public int positon;
        public int mRankId;
        public String logName;
    }

    public String getRankLogName(int mRankId) {
        String mRankName = null;
        switch (mRankId) {
            case EgmConstants.RankID.NEW_FEMALE:
                mRankName = EgmStat.LIST_F_NEW;
                break;
            case EgmConstants.RankID.HOT:
                mRankName = EgmStat.LIST_F_HOT;
                break;
            case EgmConstants.RankID.STAR:
                mRankName = EgmStat.LIST_F_STAR;
                break;
            case EgmConstants.RankID.TOP_FEMALE:
                mRankName = EgmStat.LIST_F_TOP;
                break;
            case EgmConstants.RankID.NEW_MALE:
                mRankName = EgmStat.LIST_M_NEW;
                break;
            case EgmConstants.RankID.STRENGTH_MALE:
                mRankName = EgmStat.LIST_M_STRENGTH;
                break;
            case EgmConstants.RankID.TOP_MALE:
                mRankName = EgmStat.LIST_M_TOP;
                break;
            default:
                break;
        }
        return mRankName;
    }

    public class FemaleViewHolder {

        public HeadView mProfile;
        public ImageView mCrown;
        public TextView mNickName;
        public TextView mPictureCount;
        public TextView mLevel;
        public TextView mAgeHeight; // 年龄和身高
        public TextView mCharmValue;// 魅力值
        public TextView mNumberTag;
        public TextView mNewMenber;

        public long mUid; // 点击时可以从view的tag里取到uid
        public int positon;
        public int mRankId;
        public String logName;
    }

    private boolean isKnownRankId(int rankId) {
        return rankId <= 7;
    }
}
