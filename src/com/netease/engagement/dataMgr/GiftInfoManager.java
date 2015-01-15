package com.netease.engagement.dataMgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.common.image.task.ImageTransaction;
import com.netease.date.R;
import com.netease.engagement.app.EngagementApp;
import com.netease.service.Utils.StreamUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.GiftConfigResult;
import com.netease.service.protocol.meta.GiftGroupInfo;
import com.netease.service.protocol.meta.GiftInfo;
import com.netease.service.protocol.meta.SpecialGift;
/**
 * 管理礼物数据
 */
public class GiftInfoManager {
	
	private static final String GIFT_CONFIG_FILE = "giftconfig.json";
	
	private static String GIFT_ICON_URL = "http://yimg.nos.netease.com/images/gift/";
	
	private static String mGiftVersion;
	
	private static List<GiftGroupInfo> mGiftGroup ;
	private static String[] mGiftGroupName;
	
	private static SparseArray<GiftInfo> mGiftMap = new SparseArray<GiftInfo>(); // all gift
	
	private static boolean mInited;
	
	private static boolean mFetchGiftConfig;
	
	private static void checkInit() {
		if (! mInited) {
			synchronized (GiftInfoManager.class) {
				if (! mInited) {
					GiftConfigResult result = getGiftConfigFromData();
					
					if (result != null) {
						updateGiftConfig(result);
						
						mInited = true;
					}
				}
			}
		}
	}
	
	private static void updateGiftConfig(GiftConfigResult result) {
		if (result == null) {
			return ;
		}
		
		if (! TextUtils.isEmpty(result.giftUrl)) {
			GIFT_ICON_URL = result.giftUrl;
		}
		
		if (result.giftGroup == null
				|| result.giftGroup.length == 0) {
			return ;
		}
		
		mGiftVersion = result.version;
		
		int length = result.giftGroup.length;
		
		List<GiftGroupInfo> list = new ArrayList<GiftGroupInfo>();
		
		String[] names = new String[length];
		for (int i = 0; i < length; i++) {
			GiftGroupInfo group = result.giftGroup[i];
			for (GiftInfo info : group.gifts) {
				mGiftMap.append(info.id, info);
			}
			
			names[i] = group.giftGroupName;
			list.add(group);
		}
		
		mGiftGroup = list;
		mGiftGroupName = names;
	}
	
	public static String getGiftIconUrl(long id) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(GIFT_ICON_URL).append(id).append(".png");
		
		return buffer.toString();
	}
	
	public static String getGiftIconUrl(String id) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(GIFT_ICON_URL).append(id).append(".png");
		
		return buffer.toString();
	}
	
	private static String getCrownIconUrl(long id, boolean big) {
		StringBuffer buffer = new StringBuffer();
//		buffer.append(GIFT_ICON_URL).append(big ? "big" : "small").append(id).append(".png");
		buffer.append(GIFT_ICON_URL).append("big").append(id).append(".png");
		
		return buffer.toString();
	}
	
	public static void getGiftConfig(){
		checkInit();
		
		if (! mFetchGiftConfig) {
			mFetchGiftConfig = true;
			EgmService.getInstance().addListener(mCallBack);
			
			EgmService.getInstance().doGetGiftConfig(mGiftVersion);
		}
	}
	
	private static EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetGiftConfigSucess(int transactionId, GiftConfigResult obj) {
			EgmService.getInstance().removeListener(mCallBack);
			
			if(obj == null){
				mFetchGiftConfig = false;
				return ;
			}
			
			updateGiftConfig(obj);
			GiftDownLoadManager.getInstance().downLoadGiftZip(
					obj.version, obj.url);
			
			mFetchGiftConfig = false;
		}
		
		@Override
		public void onGetGiftConfigError(int transactionId, int errCode,String err) {
			EgmService.getInstance().removeListener(mCallBack);
			
			mFetchGiftConfig = false;
		}
	};
	
	/**
	 * 判断是不是新手礼物
	 * @param info
	 * @return
	 */
	public static boolean isSpecialGift(int giftId){
		checkInit();
		
		GiftInfo info = mGiftMap.get(giftId);
		if (info != null) {
			return info.specialGift > 0;
		}
		else if (giftId > 0) {
			getGiftConfig();
		}
		
        return false;
	}
	
	/**
	 * 判断新手礼物是否剩余
	 * @param giftId
	 * @return
	 */
	public static boolean isSpecialGiftLeft(int giftId) {
		checkInit();
		
		GiftInfo info = mGiftMap.get(giftId);
		if (info != null) {
			return info.specialGift > 0 && info.isVisible();
		}
		else if (giftId > 0) {
			getGiftConfig();
		}
		
		return true;
	}
	
	/**
	 * 将对应id的新手礼物的个数减少一个
	 * @param giftId
	 */
	public static void reduceSpecialGift(int giftId){
		checkInit();
		
		GiftInfo info = mGiftMap.get(giftId);
		if (info != null) {
			if (info.specialGift > 0) {
				info.times--;
			}
		}
		else if (giftId > 0) {
			getGiftConfig();
		}
	}
	
	/**
	 * 根据礼物分组名称获取礼物列表
	 * 
	 * 需要移除不能使用的特殊礼物
	 */
	public static ArrayList<GiftInfo> getGiftsByGroup(String groupName){
		checkInit();
		
		if (mGiftGroupName == null || mGiftGroupName.length == 0) {
			getGiftConfig();
		}
		else {
			int size = mGiftGroupName.length;
			for (int i = 0; i < size; i++) {
				if (groupName.equals(mGiftGroupName[i])) {
					ArrayList<GiftInfo> infos = new ArrayList<GiftInfo>();
					
					GiftInfo[] items = mGiftGroup.get(i).gifts;
					size = items.length;
					for (int j = 0; j < size; j++) {
						GiftInfo info = items[j];
						if (info.isVisible()) {
							infos.add(items[j]);
						}
					}
					
					return infos;
				}
			}
		}
		
		return null ;
	}
	
	/**
	 * 获取礼物分组名称
	 */
	public static String[] getGroupNames(){
		checkInit();
		
		if(mGiftGroupName == null || mGiftGroupName.length == 0){
			getGiftConfig();
			
			return new String[0];
		}
		
		return mGiftGroupName ;
	}
	
	/**
	 * 获取指定id的皇冠的价格
	 */
	public static int getCrownPriceById(int crownId){
		checkInit();
		
		GiftInfo info = mGiftMap.get(crownId);
		if (info != null) {
			return info.price;
		}
		else if (crownId > 0) {
			getGiftConfig();
		}
		
		return -1 ;
	}
	
	/**
	 * 判断是不是皇冠
	 */
	public static boolean isCrown(int giftId){
		checkInit();
		
		GiftInfo info = mGiftMap.get(giftId);
		
		if (info != null) {
			return info.isCrown();
		}
		else if (giftId > 0) {
			getGiftConfig();
		}
		
		return false ;
	}
	
	/**
	 * 获取礼物或皇冠信息
	 * @param id
	 * @return
	 */
	public static GiftInfo getGiftInfoById(int id) {
		checkInit();
		
		GiftInfo info = mGiftMap.get(id);
		if (info == null && id > 0) {
			getGiftConfig();
		}
		
		return info;
	}

	/**
	 * 获取最贵的皇冠的id
	 */
	public static GiftInfo getMostExpensiveCrown(){
		checkInit();
		
		GiftInfo info = null;
		
		ArrayList<GiftInfo> crownList = getGiftsByGroup("皇冠");
		if (crownList != null && crownList.size() > 0) {
			for (int i = crownList.size() - 1; i >= 0; i--) {
				info = crownList.get(i);
				if (info.isVisible()) {
					break;
				}
			}
		}
		
		return info;
	}
	
	
	/**
	 * 将礼物配置信息存储到data下面
	 */
	public static void saveGiftConfigToData(String data){
		try {
			//文件不存在的时候会自动创建文件
			FileOutputStream fos = EngagementApp.getAppInstance().openFileOutput(
					GIFT_CONFIG_FILE, Context.MODE_PRIVATE);
			if(fos != null){
				byte[] d = data.getBytes("utf-8");
				fos.write(d);
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 */
	private static GiftConfigResult getGiftConfigFromData() {
		try {
			Context context = EngagementApp.getAppInstance();
			
			FileInputStream fis = context.openFileInput(GIFT_CONFIG_FILE);
			
			String json = StreamUtil.readString(fis);
			
			if (!TextUtils.isEmpty(json)) {
				Gson gson = new Gson();
				return gson.fromJson(json, GiftConfigResult.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void updateSpecialGifts(SpecialGift[] specialGifts) {
		checkInit();
		
		if (specialGifts == null) {
			return;
		}

		for (SpecialGift gift : specialGifts) {
			GiftInfo info = mGiftMap.get(gift.giftId);

			if (info != null) {
				info.times = gift.times;
			}
			else if (gift.giftId > 0) {
				getGiftConfig();
			}
		}
	}

	public static void setGiftInfo(int giftId, ImageView imageView) {
//		checkInit();
		
		setGiftInfo(giftId, imageView, true);
	}
	
	public static void setGiftInfo(int giftId, GiftInfo info, ImageView view) {
//		checkInit();
		
		setGiftInfo(giftId, view, false);
	}
	
	public static void setCrownInfo(long id, boolean big, ImageView view) {
		String url = getCrownIconUrl(id, big);
		
		ImageViewAsyncCallback c = new ImageViewAsyncCallback(view, url) {
			@Override
			public boolean isTransport(ImageTransaction trans) {
				return true;
			}
		};
		view.setTag(c);
	}
	
	private static void setGiftInfo(int giftId, ImageView view, 
			boolean customDefault) {
		String path = GiftDownLoadManager.GIFT_ZIP_DIR +"/" + giftId + ".png";
		File file = new File(path);
		
		String url = null;
		if (file.exists()) {
			url = Uri.fromFile(file).toString();
		}
		else {
			if (! customDefault) {
				view.setImageResource(R.drawable.icon_message_gift);
			}
			
			url = getGiftIconUrl(giftId);
		}
		
		ImageViewAsyncCallback c = new ImageViewAsyncCallback(view, url) {
			@Override
			public boolean isTransport(ImageTransaction trans) {
				return true;
			}
		};
		view.setTag(c);
	}

	private static GiftInfo mChoosedInfo;
	
	public static void setChoosedGiftInfo(GiftInfo info) {
		mChoosedInfo = info;
	}
	
	public static GiftInfo getChoosedGiftInfo() {
		return mChoosedInfo;
	}
}
