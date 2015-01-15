package com.netease.engagement.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.service.Utils.EgmUtil;

public class ChoiceListAdapter extends BaseAdapter{
	
	private List<String> mTagNames ;
	private List<String> mChoosedTags ;
	private LayoutInflater mInflater ;
	private Context context;
	
	public ChoiceListAdapter(List<String> tags ,List<String> choosed ,Context context){
		mTagNames = tags ;
		mChoosedTags = choosed ;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null ;
		if(convertView == null){
			 AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                     LinearLayout.LayoutParams.MATCH_PARENT, EgmUtil.dip2px(context, 56));
			convertView = mInflater.inflate(R.layout.view_item_choice_list, null);
			holder = new ViewHolder();
			holder.tagName = (TextView)convertView.findViewById(R.id.tagname);
			holder.select = (ImageView)convertView.findViewById(R.id.select);
			convertView.setLayoutParams(lp);
			convertView.setTag(holder);
		}
		
		holder = (ViewHolder) convertView.getTag();
		holder.tagName.setText(mTagNames.get(position));
		final ImageView select = holder.select ;
		
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				boolean isSelected = select.isSelected();
				select.setSelected(!isSelected);
				if(select.isSelected()){
					mChoosedTags.add(mTagNames.get(position));
				}else{
					mChoosedTags.remove(mTagNames.get(position));
				}
				notifyDataSetChanged();
			}
		});
		
		if(mChoosedTags.contains(mTagNames.get(position))){
			holder.select.setSelected(true);
		}else{
			holder.select.setSelected(false);
		}
		
		return convertView;
	}
	
	public List<String> getChoosedList(){
		return mChoosedTags ;
	}
	
	class ViewHolder{
		TextView tagName ;
		ImageView select ;
	}
}
