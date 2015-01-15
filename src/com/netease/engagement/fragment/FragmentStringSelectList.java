package com.netease.engagement.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.protocol.meta.OptionInfo;

/** String类型的选择列表 */
public class FragmentStringSelectList extends FragmentBase {
    private ActivityEngagementBase mActivity;
    
    private String mTitle;
    private OptionInfo[] mDatas;
    private View.OnClickListener mClickSelect;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (ActivityEngagementBase)getActivity();
        
        CustomActionBar actionBar = mActivity.getCustomActionBar();
        actionBar.setMiddleTitle(mTitle);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = (ListView)inflater.inflate(R.layout.fragment_select_position_layout, container, false);
        
        listView.setAdapter(new StringListAdapter());
        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mClickSelect != null){
                    mClickSelect.onClick(view);
                }
                
                clickBack();
            }
        });
        
        return listView;
    }
    
    public void setParams(String title, OptionInfo[] datas, View.OnClickListener l){
        mTitle = title;
        mDatas = datas;
        mClickSelect = l;
    }
    
    public class StringListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            int count = 0;
            
            if(mDatas != null){
                count = mDatas.length;
            }
            
            return count;
        }

        @Override
        public Object getItem(int position) {
            Object item = null;
            
            if(mDatas != null && position < mDatas.length){
                item = mDatas[position];
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
            
            ((TextView)convertView).setText(mDatas[position].value);
            convertView.setTag(mDatas[position]);
            
            return convertView;
        }
    }
}
