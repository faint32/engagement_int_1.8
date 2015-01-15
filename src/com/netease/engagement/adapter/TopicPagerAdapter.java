package com.netease.engagement.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.TopicDataManager;
import com.netease.engagement.fragment.FragmentPrivateSession;
import com.netease.service.db.manager.ManagerAccount;


/**
 * 话题pager
 */
public class TopicPagerAdapter extends PagerAdapter{
	
	private FragmentPrivateSession fragment ;
	
	public TopicPagerAdapter(FragmentPrivateSession fragment){
		this.fragment = fragment ;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ListView listView = new ListView(fragment.getActivity());
		container.addView(listView);
		List<String> list = null ;
		
		switch(ManagerAccount.getInstance().getCurrentGender()){
			case EgmConstants.SexType.Female:
				if(position == 0){
					list = TopicDataManager.getInstance().getHelloGirl();
				}else if(position == 1){
					list = TopicDataManager.getInstance().getWannaGift();
				}
				break;
			case EgmConstants.SexType.Male:
				if(position == 0){
					list = TopicDataManager.getInstance().getHelloMan();
				}else if(position == 1){
					list = TopicDataManager.getInstance().getHummerMan();
				}
				break;
		}
		if(list != null){
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					fragment.getActivity(),
					R.layout.item_view_topic_pager,
					getRandomList(list));
			
			listView.setAdapter(adapter);
		}
		listView.setOnItemClickListener(mOnItemClickListener);
		return listView;
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
//			String topic = (String)(parent.getAdapter().getItem(position));
//			if(fragment != null && !TextUtils.isEmpty(topic)){
//				fragment.hideTopicLayout();
//				fragment.setSendTopic(topic);
//			}
		}
	};
	
	private List<String> getRandomList(List<String> src){
		Random random = new Random();
		int length = src.size();
		Set<Integer> indexSet = new HashSet<Integer>();
		while(indexSet.size() < 10){
			int index = random.nextInt(length);
			indexSet.add(index);
			if(indexSet.size() == 10){
				break ;
			}
		}
		List<String> result = new ArrayList<String>();
		for(Integer index : indexSet){
			result.add(src.get(index));
		}
		return result ;
	}
}
