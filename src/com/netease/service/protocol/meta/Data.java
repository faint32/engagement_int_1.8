package com.netease.service.protocol.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Data {
	public static String[] urls = new String[]{
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/30/1/lzzdvr_1396258290098.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/04/03/17/43/07/6/dopcox_1396518187651.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/04/03/17/43/07/6/orfycz_1396518187595.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/04/03/17/43/07/6/hfzmef_1396518187659.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/04/03/17/43/11/0/gpzjdr_1396518191091.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/37/33/2/vvgqsr_1396258653207.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/37/39/9/jruors_1396258659965.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/37/40/0/itwqaw_1396258660071.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/37/47/7/cuutrs_1396258667730.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/37/41/2/ucfwal_1396258661253.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/37/51/2/jfioag_1396258671195.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/38/19/6/eofhfo_1396258699577.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/37/51/2/qobsjg_1396258671241.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/21/4/sblyby_1396258281419.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/23/0/szxowr_1396258282984.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/23/0/wzjcaq_1396258283045.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/23/6/rcfyom_1396258283644.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/29/5/nvlkpb_1396258289506.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/38/3/qycrcq_1396258298301.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/29/1/mjoigj_1396258289171.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/30/7/haqmla_1396258290747.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/30/4/uiusph_1396258290442.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/33/7/refdlh_1396258293749.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/35/5/odijqy_1396258295484.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/38/2/ncfyou_1396258298239.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/36/7/qiqmoz_1396258296755.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/44/3/dajaja_1396258304307.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/38/1/qpnrxg_1396258298174.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/41/1/frfmcs_1396258301177.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/40/9/fvahdb_1396258300931.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/42/1/peljjv_1396258302112.jpg",
				"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/44/8/dxhodf_1396258304855.jpg"
	};
	
	public static List<String> getImages(int size){
		List<String> result = new ArrayList<String>();
		for(int i = 0 ;i < size; i++){
			result.add(urls[i]);
		}
		return result;
	}
	
	public static String[] shortStrings = new String[]{
		"健身美体",
		"享受美食",
		"喝咖啡",
		"派对舞会",
		"短期旅游",
		"K歌",
		"电影",
		"音乐",
		"运动",
		"摄影",
		"弹钢琴",
		"瑜伽",
		"游泳"
	};
	
	public static String getText(){
		Random random = new Random();
		String result = shortStrings[random.nextInt(shortStrings.length)];
		return result ;
	}
}
