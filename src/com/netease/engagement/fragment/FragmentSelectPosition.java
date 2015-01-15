package com.netease.engagement.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.date.R;

/**
 * 地理位置选择列表界面
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class FragmentSelectPosition extends FragmentBase{
    private Activity mActivity;
    private OnItemClickListener mItemClick;
    private ArrayList<String> mDataList;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = this.getActivity();
        
        if(mDataList == null)
            return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ListView listView = (ListView)inflater.inflate(R.layout.fragment_select_position_layout, container, false);
        
        LocationAdapter adapter = new LocationAdapter(mDataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mItemClick);
        
        return listView;
    }
    
    public void setDataList(ArrayList<String> dataList){
        mDataList = dataList;
    }
    
    public void setOnListItemClickListener(OnItemClickListener l){
        mItemClick = l;
    }
    
    public class LocationAdapter extends BaseAdapter{
        private ArrayList<String> mDataList;
        
        public LocationAdapter(ArrayList<String> list){
            mDataList = list;
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
            
            if(mDataList != null){
                item = mDataList.get(position);
            }
            
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.view_position_list_item, null, false);
            }
            
            ((TextView)convertView).setText(mDataList.get(position));
            
            return convertView;
        }
    }
}
