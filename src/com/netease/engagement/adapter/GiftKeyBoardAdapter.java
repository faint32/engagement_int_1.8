package com.netease.engagement.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.widget.GiftGridView;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.GiftInfo;
import com.netease.service.protocol.meta.LoopBack;

public class GiftKeyBoardAdapter extends PagerAdapter{
	private static final int PAGE_NUM = 8 ;
	
	private String[] mGroupNames ;
	
	private Context mContext ;
	
	private boolean forChat ;
	
	//总页数
	private int pageCount ;
	
	private int firstPageCount ;
	
	public GiftKeyBoardAdapter(Context context,String[] groupNames,boolean forChat){
		this.mGroupNames = groupNames ;
		this.mContext = context ;
		this.forChat = forChat ;
		pageCount = computPageCount();
	}
	
	public int getFirstPageCount(){
		return firstPageCount ;
	}
	
	public int getSecondPageCount(){
		return pageCount - firstPageCount ;
	}
	
	@Override
	public int getCount() {
		return pageCount ;
	}
	
	private int computPageCount(){
		int count = 0 ;
		ArrayList<GiftInfo> list = new ArrayList<GiftInfo>();
		for(int i = 0 ; i < mGroupNames.length ; i++){
			list = GiftInfoManager.getGiftsByGroup(mGroupNames[i]);
			if(list != null){
				int size = list.size();
				int pageNum = size / PAGE_NUM;
				if (size % PAGE_NUM == 0) {
					count = count + pageNum;
				} else {
					count = count + (pageNum + 1);
				}
			}
			if(i == 0){
				firstPageCount = count ;
			}
		}
		return count ;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1 ? true : false ;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		GiftGridView gridView = null ;
		
		if(position < firstPageCount){
			gridView = new GiftGridView(mContext,mGroupNames[0]);
			gridView.setOnItemClickListener(mOnItemClickListener);
			gridView.setGiftInfoList(GiftInfoManager.getGiftsByGroup(mGroupNames[0]));
			gridView.setPageNum(position);
//			container.addView(gridView);
//			return gridView ;
		}else{
			gridView = new GiftGridView(mContext,mGroupNames[1]);
			//要添加在上面，否则不响应点击，why？
			gridView.setOnItemClickListener(mOnItemClickListener);
			gridView.setGiftInfoList(GiftInfoManager.getGiftsByGroup(mGroupNames[1]));
			gridView.setPageNum(pageCount - position - 1);
		}
		container.addView(gridView);
		return gridView ;
	}
	
	private boolean isActived(int listPos){
		int fisrt = GiftInfoManager.getGiftsByGroup(mGroupNames[0]).size();
		int second = firstPageCount * PAGE_NUM ;
		int third = second + GiftInfoManager.getGiftsByGroup(mGroupNames[1]).size();
		if((listPos >= 0 && listPos < fisrt)|| (listPos >= second && listPos < third)){
			return true ;
		}
		return false ;
	}
	
	/**在isActived的情况下使用
	 * @param listPos
	 * @return
	 */
	private String getGroupName(int listPos){
		if(listPos < firstPageCount * PAGE_NUM){
			return mGroupNames[0] ;
		}else{
			return mGroupNames[1] ;
		}
	}
	
	/**在isActived的情况下使用
	 * @param listPos
	 * @return
	 */
	private GiftInfo getGiftInfo(int listPos){
		int fisrt = GiftInfoManager.getGiftsByGroup(mGroupNames[0]).size();
		int second = firstPageCount * PAGE_NUM ;
		if(listPos < fisrt){
			return GiftInfoManager.getGiftsByGroup(mGroupNames[0]).get(listPos);
		}else{
			return GiftInfoManager.getGiftsByGroup(mGroupNames[1]).get(listPos - second);
		}
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			int listPos = ((ViewPager)arg0.getParent()).getCurrentItem()*PAGE_NUM + arg2;
			if(isActived(listPos)){
				int crownId = (Integer)(MemoryDataCenter.getInstance().get(
						MemoryDataCenter.CURRENT_COMPARE_CROWNID));
				
				int price = GiftInfoManager.getCrownPriceById(crownId);
				if(getGroupName(listPos).equals("浪漫礼物") || 
						(getGroupName(listPos).equals("皇冠") && getGiftInfo(listPos).price >= price)){
					GiftInfo info = getGiftInfo(listPos);
					
					//重置礼物被选状态
					refreshPager((ViewPager)arg0.getParent(), info);
					
					//聊天界面发送礼物回调
					if(forChat){
						//changed by echo_chen 2014-07-22 for BUG任务 #140868
//						if(GiftInfoManager.getInstance().isSpecialGift(info.id)){
//							GiftInfoManager.getInstance().reduceSpecialGift(info.id);
//						}
						LoopBack lp = new LoopBack();
						lp.mType = EgmConstants.LOOPBACK_TYPE.send_gift;
						lp.mData = info ;
						EgmService.getInstance().doLoopBack(lp);
					}
				}
			}
		}
	};
	
	public void refreshPager(ViewPager pager, GiftInfo info){
		GiftInfoManager.setChoosedGiftInfo(info);
		
		//仅仅一个GridView的adapter刷新时不够的
		for(int i=0; i < pager.getChildCount(); i++){
			((GiftGridView)pager.getChildAt(i)).getGiftAdapter().setChoosed(info);
		}
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
}
