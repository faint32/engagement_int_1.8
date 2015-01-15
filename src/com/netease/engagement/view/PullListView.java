package com.netease.engagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.netease.date.R;


public class PullListView extends PullToRefreshListView{
    private Context mContext;
    
    public PullListView(Context context,
            com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode) {
        
        super(context, mode);
        
        mContext = context;
        enablePullFromStart();
    }

    public PullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;
        enablePullFromStart();
    }

    public PullListView(Context context) {
        super(context);
        
        mContext = context;
        enablePullFromStart();
    }

    public PullListView(Context context,
            com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode,
            com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle style) {
        
        super(context, mode, style);
        
        mContext = context;
        enablePullFromStart();
    }
    
    public void enablePullFromStart(){
        setMode(Mode.PULL_FROM_START);
    }
    
    /** 正在加载 */
    @Override
    public View getLoadingView() {
        View v = View.inflate(mContext, R.layout.view_list_loading, null);
        
        return v;
    }
    
    /** 加载失败 */
    @Override
    public View getLoadingErrorView() {
        View v = View.inflate(mContext, R.layout.view_common_empty, null);
        
        TextView text = (TextView)v.findViewById(R.id.empty_text);
        text.setText(R.string.common_reload_tip);
        
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reLoad();
            }
        });
        
        return v;
    }
    
    /** 加载后没有内容 */
    @Override
    public View getNoContentView() {
        View v = View.inflate(mContext, R.layout.view_common_empty, null);
        
        ImageView icon = (ImageView)v.findViewById(R.id.empty_image);
        icon.setVisibility(View.GONE);
        TextView text = (TextView)v.findViewById(R.id.empty_text);
        text.setText(R.string.common_content_empty_tip);
        
        return v;
    }
    
    /** 底部加载更多 */
    @Override
    public View getLoadingFooterView() {
        View v = View.inflate(mContext, R.layout.view_list_footer_load_more, null);
        
        return v;
    }
}
