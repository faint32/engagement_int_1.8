
package com.handmark.pulltorefresh.compat;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

public class MultiExpandableAdapterManager {

    private PullToRefreshExpandableListView mRrefreshList;
    private List<BaseExpandableListAdapter> mAdapterList;
    private List<ListViewStatus> mStatusList;

    private int mCurrentAdapterIndex = -1;// 当前使用adapter的索引
    private int mCommonHeaderCount = 1;// 通用header的数量

    public MultiExpandableAdapterManager(PullToRefreshExpandableListView refreshList) {
        mRrefreshList = refreshList;
        mAdapterList = new ArrayList<BaseExpandableListAdapter>();
        mStatusList = new ArrayList<MultiExpandableAdapterManager.ListViewStatus>();
    }

    public void setCommonHeaderCount(int count) {
        mCommonHeaderCount = count;
    }

    public void addAdapter(BaseExpandableListAdapter adapter) {
        mAdapterList.add(adapter);
        mStatusList.add(new ListViewStatus());
    }

    public void addAdapter(BaseExpandableListAdapter adapter, String noContentStr) {
        mAdapterList.add(adapter);
        ListViewStatus status = new ListViewStatus();
        status.mNoContentString = noContentStr;
        mStatusList.add(status);
    }

    public BaseExpandableListAdapter getAdapter(int index) {
        BaseExpandableListAdapter adapter = null;

        if (index < 0 || index > getCount()) {
            return adapter;
        }

        adapter = mAdapterList.get(index);
        return adapter;
    }

    public void setCurrentAdapter(int index) {
        if (index < 0 || index > getCount() || mAdapterList.get(index) == null) {
            // Error
            return;
        }
        // Save before
        if (mCurrentAdapterIndex >= 0 && mStatusList.get(mCurrentAdapterIndex) != null) {
            // Save position
            if (mRrefreshList.getRefreshableView().getFirstVisiblePosition() <= mCommonHeaderCount) {
                mStatusList.get(mCurrentAdapterIndex).itemPosition = 0;
            } else {
                mStatusList.get(mCurrentAdapterIndex).savePosition(
                        mRrefreshList.getRefreshableView());
            }
            // Save status
            mStatusList.get(mCurrentAdapterIndex).saveStatus(mRrefreshList);
        }
        // Load
        mCurrentAdapterIndex = index;

        if (mRrefreshList.getRefreshableView().getFirstVisiblePosition() < mCommonHeaderCount
                || (mRrefreshList.getRefreshableView().getFirstVisiblePosition() == mCommonHeaderCount && mStatusList
                        .get(mCurrentAdapterIndex).topPosition >= 0)) {
            // load common status
            ListViewStatus status = new ListViewStatus();
            status.savePosition(mRrefreshList.getRefreshableView());
            mRrefreshList.getRefreshableView().setAdapter(mAdapterList.get(mCurrentAdapterIndex));
            status.loadPosition(mRrefreshList.getRefreshableView());
        } else {
            // load save status
            mRrefreshList.getRefreshableView().setAdapter(mAdapterList.get(mCurrentAdapterIndex));
            ListViewStatus status = mStatusList.get(mCurrentAdapterIndex);
            if (status.itemPosition <= mCommonHeaderCount) {
                status.itemPosition = mCommonHeaderCount;
                status.topPosition = -1;// 偏移1像素和common状态区分
            }
            status.loadPosition(mRrefreshList.getRefreshableView());
        }
        
        mStatusList.get(mCurrentAdapterIndex).loadStatus(mRrefreshList);
    }

    public int getCurrentAdapterIndex() {
        return mCurrentAdapterIndex;
    }

    public int getCount() {
        return mAdapterList.size();
    }

    public void changeState(int adapterIndex, int loadingState) {
        if (adapterIndex >= 0 && adapterIndex < mStatusList.size()) {
            ListViewStatus status = mStatusList.get(adapterIndex);
            status.loadingStatus = loadingState;
        }
    }

    private class ListViewStatus {

        // 当前ListView加载状态
        boolean hasMore;
        int loadingStatus = LoadingAdapterViewBaseWrap.STATE_IDLE;
        // 当前位置状态
        int itemPosition;// 当前item位置
        int topPosition;// 当前第一个item距离top的高度
        // 存储各个状态的文案
        String mLoadingString;
        String mLoadingMoreString;
        String mLoadingErrorString;
        String mNoContentString;
        String mNoNetWorkString;

        public void savePosition(ListView list) {
            if (list == null)
                return;
            itemPosition = list.getFirstVisiblePosition();
            topPosition = list.getChildAt(0) == null ? 0 : list.getChildAt(0).getTop();
        }

        public void loadPosition(ListView list) {
            if (list == null)
                return;
            list.setSelectionFromTop(itemPosition, topPosition);
        }

        public void saveStatus(PullToRefreshExpandableListView refreshList) {
            loadingStatus = refreshList.getLoadingState();
            hasMore = refreshList.hasMore();
        }

        public void loadStatus(PullToRefreshExpandableListView refreshList) {
            refreshList.setHasMore(hasMore);
            if (refreshList instanceof LoadingExpandableListView) {
                resetLoadingString((LoadingExpandableListView)refreshList);
            }
            refreshList.setLoadingState(loadingStatus);
        }

        /**
         * 重置LoadingListView文案
         */
        public void resetLoadingString(LoadingExpandableListView loadingList) {
            if (!TextUtils.isEmpty(mLoadingString)) {
                loadingList.setNoContentString(mLoadingString);
            }
            if (!TextUtils.isEmpty(mLoadingMoreString)) {
                loadingList.setNoContentString(mLoadingMoreString);
            }
            if (!TextUtils.isEmpty(mLoadingErrorString)) {
                loadingList.setNoContentString(mLoadingErrorString);
            }
            if (!TextUtils.isEmpty(mNoContentString)) {
                loadingList.setNoContentString(mNoContentString);
            }
            if (!TextUtils.isEmpty(mNoNetWorkString)) {
                loadingList.setNoContentString(mNoNetWorkString);
            }
        }
    }

}
