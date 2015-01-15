package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.ChatSkillInfo;
import com.netease.service.protocol.meta.LoopBack;

public class FragmentChatSkill extends FragmentBase {
	
	private final int MAX = 6;
	
	private CustomActionBar mCustomActionBar;
	
	private ListView listview;
	private ChatSkillAdapter adapter;
	
	private ArrayList<ChatSkillInfo> list;
	private HashSet<Integer> set;
	
	public static FragmentChatSkill newInstance(){
		FragmentChatSkill fragment = new FragmentChatSkill();
		return fragment ;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EgmService.getInstance().addListener(mCallBack);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        
        mCustomActionBar.setLeftClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clickBack();
			}
		});
        
        mCustomActionBar.setMiddleTitle(R.string.rec_chatskill_choose);
        mCustomActionBar.setMiddleTitleSize(20);
        
        mCustomActionBar.setRightAction(0,R.string.save);
        mCustomActionBar.setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	int selected[] = new int[FragmentChatSkill.this.set.size()];
            	int i=0;
            	for (int id : FragmentChatSkill.this.set) {
            		selected[i] = id;
            		i++;
            	}
            	EgmService.getInstance().doUpdateChatSkills(selected);
            }
        });
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_chatskill_layout, container, false);
		
		listview = (ListView) view.findViewById(R.id.listview);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ChatSkillInfo info = FragmentChatSkill.this.list.get(position);
				if (FragmentChatSkill.this.set.contains(info.id)) {
					FragmentChatSkill.this.set.remove(info.id);
					adapter.notifyDataSetChanged();
				} else {
					if (FragmentChatSkill.this.set.size() >= MAX) {
						FragmentChatSkill.this.showToast(R.string.rec_chatskill_max_choose);
					} else {
						FragmentChatSkill.this.set.add(info.id);
						adapter.notifyDataSetChanged();
					}
				}
			}
		});
		
		this.showWatting(this.getActivity().getString(R.string.common_tip_is_waitting));
		EgmService.getInstance().doGetChatSkills();
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
	
	private void initData(ArrayList<ChatSkillInfo> obj) {
		list = obj;
    	set = new HashSet<Integer>();
    	for (int i=0; i<list.size(); i++) {
    		ChatSkillInfo info = list.get(i);
    		if (info.selected == 1) {
    			set.add(info.id);
    		}
    	}
    	adapter = new ChatSkillAdapter(this.getActivity());
    	listview.setAdapter(adapter);
    	
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		
		/**读取用户聊天技的列表*/
	    public void onGetTalkSkillsSuccess(int transactionId, ArrayList<ChatSkillInfo> obj){
	    	stopWaiting();
	    	initData(obj);
	    }
	    
		/**读取用户聊天技的列表*/
	    public void onGetTalkSkillsError(int transactionId,int errCode, String err){
	    	stopWaiting();
	    	ToastUtil.showToast(getActivity(), err);
	    }
	    
	    /**更新用户聊天技的列表*/
	    public void onUpdateTalkSkillsSuccess(int transactionId,int code){
	    	stopWaiting();
			ToastUtil.showToast(getActivity(), "提交成功");
			LoopBack lp = new LoopBack();
			lp.mType = EgmConstants.LOOPBACK_TYPE.update_talk_skill ;
			EgmService.getInstance().doLoopBack(lp);
			clickBack();
	    }
	    
	    /**更新用户聊天技的列表*/
	    public void onUpdateTalkSkillsError(int transactionId,int errCode, String err){
	    	stopWaiting();
			ToastUtil.showToast(getActivity(), err);
	    }
	};
	
	class ChatSkillAdapter extends BaseAdapter {
		
		private Context context;
		private LayoutInflater inflater;
		
		public ChatSkillAdapter(Context context) {
			this.context = context;
			inflater = LayoutInflater.from(context); 
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder; 
	        if (convertView == null) 
	        { 
	        	AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
	                     LinearLayout.LayoutParams.MATCH_PARENT, EgmUtil.dip2px(context, 56));
	        	convertView = inflater.inflate(R.layout.view_item_choice_list, null);
	            viewHolder = new ViewHolder(); 
	            viewHolder.tv = (TextView) convertView.findViewById(R.id.tagname);
	            viewHolder.iv = (ImageView) convertView.findViewById(R.id.select);
	            convertView.setLayoutParams(lp);
	            convertView.setTag(viewHolder); 
	        } else
	        { 
	            viewHolder = (ViewHolder) convertView.getTag(); 
	        } 
	        
	        ChatSkillInfo info = FragmentChatSkill.this.list.get(position);
	        viewHolder.tv.setText(info.name);
	        if (FragmentChatSkill.this.set.contains(info.id)) {
	        	viewHolder.iv.setSelected(true);
	        } else {
	        	viewHolder.iv.setSelected(false);
	        }
	        
			return convertView;
		}
	}
	
	static class ViewHolder 
	{ 
		public TextView tv;
		public ImageView iv;
	}
}
