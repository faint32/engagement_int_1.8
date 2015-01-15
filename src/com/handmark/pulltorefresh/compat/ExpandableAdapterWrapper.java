
package com.handmark.pulltorefresh.compat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;

/**
 * BaseExpandableListAdapter对普通的BaseAdapter的包装类，
 * 使ExpandableListView可以使用普通的BaseAdapter
 * @author MR
 *
 */
public class ExpandableAdapterWrapper extends BaseExpandableListAdapter {

    private BaseAdapter mAdapter;

    public ExpandableAdapterWrapper(BaseAdapter adapter) {
        mAdapter = adapter;
    }

    public BaseAdapter getActualAdapter(){
        return mAdapter;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mAdapter.getItem(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mAdapter.getCount();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mAdapter.getItemId(groupPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        return mAdapter.getView(groupPosition, convertView, parent);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
    
    public BaseAdapter getInnerAdapter(){
        return mAdapter;
    }

}
