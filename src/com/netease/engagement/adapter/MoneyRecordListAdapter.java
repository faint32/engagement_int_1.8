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
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.protocol.meta.MoneyRecordListInfo.MoneyRecordInfo;


public class MoneyRecordListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MoneyRecordInfo> mDataList;
    
    public MoneyRecordListAdapter(Context context){
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_money_record, null, false);
            
            viewHolder = new ViewHolder();
            viewHolder.mDateTv = (TextView)convertView.findViewById(R.id.record_date);
            viewHolder.mContentTv = (TextView)convertView.findViewById(R.id.record_content);
            viewHolder.mValueTv = (TextView)convertView.findViewById(R.id.record_value);
            viewHolder.mTypeTv = (TextView)convertView.findViewById(R.id.record_type);
            
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        
        MoneyRecordInfo info = mDataList.get(position);
        if(info != null){
            viewHolder.mDateTv.setText(TimeFormatUtil.forYMDDotFormat(info.date));
            viewHolder.mContentTv.setText(info.message);
            
            if(info.recordType == EgmConstants.MoneyType.INCOME){   // 收入
                viewHolder.mTypeTv.setText(R.string.account_money_history_tab_income);
                viewHolder.mValueTv.setText("+" + info.amount);
                viewHolder.mValueTv.setTextColor(mContext.getResources().getColor(R.color.red));
            }
            else{   
                if(info.recordType == EgmConstants.MoneyType.DEDUCT){   // 扣除
                    viewHolder.mTypeTv.setText(R.string.account_money_history_tab_deduct);
                }
                else{
                    viewHolder.mTypeTv.setText(R.string.account_money_history_tab_cash);    // 提现
                }
                
                viewHolder.mValueTv.setText("-" + info.amount);
                viewHolder.mValueTv.setTextColor(mContext.getResources().getColor(R.color.pri_money_account_cash_text_color));
            }
        }
        
        if(position % 2 == 0){  // 偶数
            convertView.setBackgroundResource(R.drawable.bg_rank_item_even);
        }
        else{
            convertView.setBackgroundResource(R.drawable.btn_common_bg_selector);
        }
        
        return convertView;
    }
    
    public void clearDataList(){
        if(mDataList != null){
            mDataList.clear();
            mDataList = null;
        }
    }
    
    public void addData(ArrayList<MoneyRecordInfo> list){
        if(list == null)
            return;
        
        if(mDataList == null){
            mDataList = list;
        }
        else{
            mDataList.addAll(list);
        }
        
        this.notifyDataSetChanged();
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
        Object obj = null;
        if(mDataList != null && position < mDataList.size()){
            obj = mDataList.get(position);
        }
        
        return obj;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        public TextView mDateTv;
        public TextView mContentTv;
        public TextView mValueTv;
        public TextView mTypeTv;
    }
}
