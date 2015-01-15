package com.netease.engagement.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentHome;
import com.netease.engagement.fragment.FragmentHomeChat;
import com.netease.engagement.fragment.FragmentHomeChatGirl;
import com.netease.engagement.fragment.FragmentHomeDiscover;
import com.netease.engagement.fragment.FragmentHomeMyself;
import com.netease.engagement.fragment.FragmentHomeRecommend;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;


/**
 * 主页PageView的Fragment的Adapter
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class HomePageFragmentAdapter extends FragmentStatePagerAdapter{
	
	private FragmentHome fragmentHome;

    public HomePageFragmentAdapter(FragmentManager fm) {
        super(fm);
    }
    
    public FragmentHome getFragmentHome() {
		return fragmentHome;
	}
	public void setFragmentHome(FragmentHome fragmentHome) {
		this.fragmentHome = fragmentHome;
	}

	@Override
    public Fragment getItem(int index) {
        FragmentBase fragment = null;
        
        switch(index){
            case EgmConstants.INDEX_RECOMMEND:
            	FragmentHomeRecommend fragmentHomeRecommend = new FragmentHomeRecommend();
            	fragmentHomeRecommend.setFragmentHome(fragmentHome);
                fragment = fragmentHomeRecommend;
                break;
            case EgmConstants.INDEX_DISCOVER:
                fragment = new FragmentHomeDiscover();
                break;
            case EgmConstants.INDEX_CHAT:
            	int gender = ManagerAccount.getInstance().getCurrentGender() ;
            	switch(gender){
	            	case EgmConstants.SexType.Female:
	            		fragment = FragmentHomeChatGirl.newInstance();
	            		break;
	            	case EgmConstants.SexType.Male:
	            		fragment = FragmentHomeChat.newInstance();
	            		break;
            	}
            	break;
            case EgmConstants.INDEX_MYSELF:
                fragment = FragmentHomeMyself.newInstance();
                break;
        }
        return fragment;
    }

    @Override
	public Object instantiateItem(ViewGroup container, int arg1) {
		return super.instantiateItem(container, arg1);
	}

	@Override
    public int getCount() {
        return 4;
    }
}
