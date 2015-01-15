package com.netease.engagement.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;

/**
 * 首页列表
 */
public class PageListAdapter extends BaseAdapter {

	private Context mContext;
	private List<String> mTagNames;
	private List<String> mTagContents;

	public PageListAdapter(Context context, List<String> tagNames,
			List<String> tagContents) {
		mContext = context;
		this.mTagNames = tagNames;
		this.mTagContents = tagContents;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_view_page_list,
					null);
			holder = new ViewHolder();
			holder.tag_name = (TextView) convertView.findViewById(R.id.tag_name);
			holder.tag_content = (TextView) convertView.findViewById(R.id.tag_content);
			holder.tag_new = (TextView) convertView.findViewById(R.id.tag_new);
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		holder.tag_name.setText(mTagNames.get(position));
		if (mTagNames.get(position).equals(mContext.getString(R.string.rec_chatskill_title))) {
			long uid = ManagerAccount.getInstance().getCurrentId();
			if (EgmPrefHelper.getNewFunctionLiaoTianJiFlag(mContext, uid)) {
				holder.tag_new.setVisibility(View.VISIBLE);
			} else {
				holder.tag_new.setVisibility(View.GONE);
			}
		}
		
		if (mTagContents.size() < position + 1) {
			holder.tag_content.setVisibility(View.GONE);
		} else {
			if(!TextUtils.isEmpty(mTagContents.get(position))){
				holder.tag_content.setVisibility(View.VISIBLE);
				holder.tag_content.setText(mTagContents.get(position));
			}else{
				holder.tag_content.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	class ViewHolder {
		TextView tag_name;
		TextView tag_content;
		TextView tag_new;
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
}
