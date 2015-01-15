package com.netease.engagement.adapter;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;

/**
 * 详细资料列表adapter
 * 改为使用相同的布局
 */
public class DetailInfoAdapter extends BaseAdapter{
	private LayoutInflater inflater ;
	private UserInfo mUserInfo ;
	private UserInfoConfig mConfig ;
	private List<String> mTagNames ;
	private List<String> mTagContents ;
	private Context mContext ;
	
	public DetailInfoAdapter(Context context,UserInfo userInfo ,UserInfoConfig config){
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context ;
		mUserInfo = userInfo ;
		mConfig = config ;
		switch(userInfo.sex){
			case 0:
				mTagNames = Arrays.asList(context.getResources().getStringArray(R.array.main_page_detail_girl));
				break;
			case 1:
				mTagNames = Arrays.asList(context.getResources().getStringArray(R.array.main_page_detail_man));
				break;
		}
		mTagContents = UserInfoUtil.getDetailInfoList(mContext,mUserInfo, mConfig);
	}
	class ViewHolder{
		TextView tagName ;
		TextView tagContent ;
		TextView tagIndicator ;
	}
	
	@Override
	public int getCount() {
		return mTagNames == null ? 0 : mTagNames.size();
	}

	@Override
	public Object getItem(int position) {
		return mTagNames == null ? null : mTagNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = inflateTextView();
		}
		fillTextView(convertView,position);
		return convertView;
	}
	
	/**
	 * @return
	 */
	private View inflateTextView(){
		View view = inflater.inflate(R.layout.item_view_page_list_detail_info, null);
		ViewHolder holder = new ViewHolder();
		holder.tagName = (TextView) view.findViewById(R.id.tag_name);
		holder.tagContent = (TextView)view.findViewById(R.id.tag_content);
		holder.tagIndicator = (TextView)view.findViewById(R.id.tag_indicator);
		view.setTag(holder);
		return view ;
	}
	
	/**
	 * 填充内容
	 * @param view
	 * @param position
	 */
	private void fillTextView(View view , int position){
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.tagName.setText(mTagNames.get(position));
		holder.tagContent.setText(mTagContents.get(position));
		if(mTagNames.get(position).equals(mContext.getResources().getString(R.string.favor_date)) 
				||mTagNames.get(position).equals(mContext.getResources().getString(R.string.interest_hobby))
				||mTagNames.get(position).equals(mContext.getResources().getString(R.string.wanna_skill))
				||mTagNames.get(position).equals(mContext.getResources().getString(R.string.adept_skill))){
			holder.tagIndicator.setVisibility(View.VISIBLE);
		}else{
			holder.tagIndicator.setVisibility(View.GONE);
		}
	}
	
	public void notify(UserInfo userInfo){
		mTagContents = UserInfoUtil.getDetailInfoList(mContext,userInfo, mConfig);
		this.notifyDataSetChanged();
	}
}
