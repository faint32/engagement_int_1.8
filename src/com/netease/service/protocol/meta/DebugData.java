package com.netease.service.protocol.meta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import com.netease.common.cache.CacheManager;

public class DebugData {
    public static class BaseDataTest {
        public Object data;
        public int code;
        public String message;
       
    } 
    /*
     * 测试数据文件名
     */
    public static String FILENAME_USERINFO_JSON = "UserInfo.json";
    /** 登录后获取的用户信息，包括push所需的数据 */
    public static String FILENAME_LOGIN_USERINFO_JSON = "LoginUserInfo.json";
    /** 推荐列表 */
    public static String FILENAME_RECOMMEND_JSON = "RecommendUserList.json";
    /** 排行榜 */
    public static String FILENAME_RANK_JSON = "RankUserList.json";
    
	private static String[] SourceIcons = new String[] {
		"http://easyread.ph.126.net/qvi8LSd1F1NFlapBeZm3Kg==/6597106050540139069.png",
		"http://easyread.ph.126.net/46qWsCJMcilSC-Md5kH4yw==/6597095055424460027.jpg",
		"http://easyread.ph.126.net/9jIG532sWu6Vk2OgQlVm0w==/6597136836866367258.jpg",
		"http://easyread.ph.126.net/cQvaLfuDtjz_ufiTedhACg==/6597103851517535018.jpg",
		"http://easyread.ph.126.net/QqC5KhVH01WgWt1Sfctl-g==/6597148931494008010.jpg",
		"http://easyread.ph.126.net/UWSCDTNqtpgHRKXs4YfrOQ==/6597101652493936216.jpg",
		"http://easyread.ph.126.net/KOHShclDXaHpgQHfBYnAMg==/14293658970939.jpg",
		"http://easyread.ph.126.net/bcmTe8j3vQmy-_ZLC4D6mQ==/6597092856401102831.jpg",
		"http://easyread.ph.126.net/-rPPgyFG4G3ePzzEDP7-gQ==/6597108249563453390.jpg",
		"http://easyread.ph.126.net/8PjOYC856zA8-MPaEo3fuQ==/6597123642726174424.jpg",
		"http://easyread.ph.126.net/lox-8naVWtmFhaJUcQTJBw==/6597104951028342239.png",
		"http://easyread.ph.126.net/n5Gfs5cVvK4Nq3mGlmO44Q==/18691705302922.png",
		"http://easyread.ph.126.net/iNKo_HCPHLchewSazgRPBA==/6597139035888797604.png",
		"http://easyread.ph.126.net/ciK4txsbh5qwFDJB3FkVjg==/6597129140284147325.png",
		"http://easyread.ph.126.net/evuCyFwDynMdfMFTdn6-fg==/6597128040772519295.png",
		"http://easyread.ph.126.net/jXTCbQeL04fSzCQbVZR6IA==/41781449548391.jpg",
		"http://easyread.ph.126.net/okZC2ItzpfabAi2KW1tobg==/6597103851516717659.png",
		"http://easyread.ph.126.net/PvoobtP_XVTdkIHrO5C75Q==/6597117045657004044.jpg"
	};
	
	private static String[] BookIcons = new String[] {
		"http://easyread.ph.126.net/x5iyhyQrmeTulZnlTfOMAQ==/6597085159820068940.jpg",
		"http://easyread.ph.126.net/AiCRcrThBaARWZazEIKXyw==/6597089557866626928.jpg",
		"http://easyread.ph.126.net/w_fa0TVNQm32TJfhU2RCAA==/65970706670474.jpg",
		"http://easyread.ph.126.net/PxRq8xClqYAFSmkP_P1KDg==/6597150031005905939.jpg",
		"http://easyread.ph.126.net/uod0b7j32gxvQI0et3OXFw==/36283891785117.jpg",
		"http://easyread.ph.126.net/m0OKVktrRlRbyCSpMEHtpA==/6597099453470849292.jpg",
		"http://easyread.ph.126.net/D_OPVtL1f2SxYnHgfpIn4w==/6597071965680352776.jpg",
		"http://easyread.ph.126.net/7s-J3lLsrEZHcOnew1Q-KQ==/6597080761773612874.png",
		"http://easyread.ph.126.net/M7XiWUzTblZO4zHH7aDZDw==/6597075264215243289.jpg",
		"http://easyread.ph.126.net/H1MCCM8mPvxQlUYV7tFE7g==/6597082960795860732.jpg",
		"http://easyread.ph.126.net/bRNqY3DBX8am_eQpELUHYA==/63771684125584.jpg",
		"http://easyread.ph.126.net/zwhlw_wQWDX2s7lNAB0UHA==/61572659036615.jpg",
	};
	
	private static String[] ArticleIcons = new String[] {
		"http://easyread.ph.126.net/isWq7t5BU9St5e4B-FidLg==/42880960536517.jpg",
		"http://easyread.ph.126.net/F2f_fKypPTr9KN1crpQTmw==/58274124801149.jpg",
		"http://easyread.ph.126.net/rz-WKNG06Cud4xonAeFKJg==/12094634900368.jpg",
		"http://easyread.ph.126.net/6OKCYKu_P9esb_dzXOWyzg==/16492681521666.jpg",
		"http://easyread.ph.126.net/IoQu8IeyidlZJMWiVWXRXg==/32985355878446.jpg",
		"http://easyread.ph.126.net/SaiEwzVvQjKxHOSzD296dQ==/60473148197609.jpg",
		"http://easyread.ph.126.net/muy4wqp2YODAQ7U6WtBJ5w==/65970706214030.jpg",
		"http://easyread.ph.126.net/p4J9Dx9f3QM6q2zmcH0hQw==/47279007192108.jpg",
		"http://easyread.ph.126.net/_-GkYiNpF19SY7heAntjzg==/47279007192109.jpg",
		"http://easyread.ph.126.net/ZFgGOiNAS-3hc1Ljda_0Dg==/6597075264215424345.jpg",
		"http://easyread.ph.126.net/s6g5U9LN6lKW9o8BT87ztA==/6597134637843324969.jpg",
		"http://easyread.ph.126.net/t0wgyO82TEGWfGaI8A6Ivw==/6597111548099078719.jpg",
		"http://easyread.ph.126.net/Pk1G0v6tdWGpwkUhrGOJ2w==/50577541454400.jpg",
		"http://easyread.ph.126.net/BHLUcxy8yrlsxk-HDBr0EA==/51677053082178.jpg",
		"http://easyread.ph.126.net/7clkB1ro9YnQ6uheRAhyaQ==/28587308653706.jpg",
		"http://easyread.ph.126.net/dA3OXZW1jhKyrj3mgEFc0Q==/6597124742238610014.jpg",
		"http://easyread.ph.126.net/2M9LJDkoYBtKNyuNUUgnZA==/6597139035889712256.jpg",
		"http://easyread.ph.126.net/Xn6NIzDOuZXytcy4Hid-Og==/6597111548099017658.jpg",
		"http://easyread.ph.126.net/5-TP3jYnvGkIAQIETHVPCw==/6597112647610641016.jpg",
		"http://easyread.ph.126.net/aQxOqI8JEi90SrztKYLtLw==/6597106050540874321.jpg",
		"http://easyread.ph.126.net/KojOZoGA5hPdjnChUaEgXQ==/6597106050540874322.jpg",
		"http://easyread.ph.126.net/dfoq766Nozv0_HF9pSgoyA==/6597157727587350507.jpg",
		"http://easyread.ph.126.net/lztMF_zQLE4KVBSSD3Pd2A==/61572660811058.jpg",
		"http://easyread.ph.126.net/1Hv1HD8bjLPQppgH1ZkfMA==/17592194023572.jpg",
		"http://easyread.ph.126.net/tXVw0ELw864d_EPbIIBdwQ==/61572660811060.jpg",
		"http://easyread.ph.126.net/N0iJapNgtzv7Ec70iHz3nw==/64871193726692.jpg",
		"http://easyread.ph.126.net/F84TZOpJlLot8BmtcPjWBA==/30786331831642.jpg",
		"http://easyread.ph.126.net/OE70nbQWrnb7q6saw4d2rQ==/30786331831644.jpg",
		"http://easyread.ph.126.net/9eu3ucXyZQt8CTTgRalB7Q==/6597101652494291035.jpg",
		"http://easyread.ph.126.net/-ILD8eVfSifRqyrXrEUTYw==/6597134637843178619.jpg",
		"http://easyread.ph.126.net/bB9sqmEHF1pJpy4LEIpusA==/6597126941261784211.jpg",
		"http://easyread.ph.126.net/-xcjkbN4wTVAcE3IYoNeNw==/65970705303893.jpg",
		"http://easyread.ph.126.net/4b40KyoDbu4qwQkMgoxkKg==/51677052952047.jpg",
		"http://easyread.ph.126.net/BjRdz1ViDYwSyaQL5onNDA==/42880959817647.jpg",
		"http://easyread.ph.126.net/bAztuxavylDcoGvpEjDI5g==/26388286333308.jpg",
	};
	
	public static String[] UserIcons = new String[] {
		"http://tp2.sinaimg.cn/2712690137/180/5662079584/1",
		"http://tp3.sinaimg.cn/2427535502/180/22830401779/0",
		"http://tp1.sinaimg.cn/2820306040/180/40023406927/1",
		"http://tp2.sinaimg.cn/3103104597/180/40007389377/0",
		"http://tp4.sinaimg.cn/1733877955/180/5666458814/0",
		"http://tp4.sinaimg.cn/1640891203/180/40024511484/1",
		"http://tp2.sinaimg.cn/1797985317/180/5668306709/0",
		"http://tp4.sinaimg.cn/1969576095/180/40022463769/0",
		"http://tp4.sinaimg.cn/1871976471/180/5667936450/0",
		"http://tp4.sinaimg.cn/2047065507/180/5668269220/0",
		"http://tp4.sinaimg.cn/2509988247/180/40004851889/0",
		"http://tp1.sinaimg.cn/2163129860/180/5663191806/0",
		"http://tp3.sinaimg.cn/2812980150/180/40023914782/0",
		"http://tp1.sinaimg.cn/2417637784/180/40025699970/0",
		"http://tp2.sinaimg.cn/2011216021/180/40027079111/0",
		"http://tp2.sinaimg.cn/2137823245/180/5667950286/1",
		"http://tp3.sinaimg.cn/1667624514/180/40027662984/0",
		"http://tp2.sinaimg.cn/1713532505/180/5667019965/1",
	};
	
	public static String[] SourceIds = new String[] {
		"932c56bbeb1f4d2da78458dbf4e38727_1",
		"a5f4d968ae5549cdb714f6bf331e3232_1",
		"e9d2582740e0450d9f6d950df85c51fb_1",
		"770f63704d1641eeb3ce622021ca6cb8_3",
		"5ca4f3d6-c104-44a3-8823-463636d2143b_1",
		"308df5e8-e977-4d31-9cff-9df43cfef386_1",
		"6cd25665d9b040db8fb0fa88fc477632_1",
		"6cade6ff7cfc43ef93299241148a1593_1",
		"220967f8-19c1-48f5-baa6-cca22ef0ba00_1",
		"7842fc04461b4253ba8c89e37a178700_1",
		"552be6392fcd4655b8886afc9d83a381_1",
		"0623c270-c519-4d2a-8882-4aaf94138b46_1",
		"d9675a17-d0fa-493e-b6d6-eae9d02e0c68_1",
		"c64e7f97-c947-4732-8586-ecf8cb48cc51_1",
		"c7e553d721f54f928694eb9281958a29_1",
		"28c1d136-fad7-4ec6-a740-b9941b2b2b3d_1",
		"82f2ab51-1586-437b-9f0e-61c4333adaea_1",
		"adaa19f6-4115-4bb2-8096-394d405a6223_1",
		"10579bed-6554-4da9-90a1-375e4664b932_1",
		"067062db-5a08-43e6-ad38-8d14b1d2a665_1",
	};
	
	public static String[] BookIds = new String[] {
		"ed0e8eed01bd4156948d4dea3ee022c8_4",
		"nb_000BOPAA_4",
		"1a3c32f6142f44d39d728dad3ffadf62_4",
		"30e904377ed94ca4a03f1808c5ebe181_4",
		"d62d6e7e28af448b8f0419f4a2aec4b6_4",
		"nb_000BOOad_00011100_4",
		"2824a37bd1124b5ca791cd00778ab32e_4",
		"0dd635710c7a46d398b37adc57a0e6c9_4",
		"84129041d3b44b9c83cab42ef79f93fa_4",
		"9a57968c35bf4a4e93c9576c7c1a6d3c_4",
		"bbcbc2674bc646a093adaecb813078f9_4",
		"d9983dc1aa3d4c94b1d8644d10362316_4",
		"7c4fba07-18a4-4648-9159-4b3be1aa5f6c_4",
		"c6ff99972b8443e79fb2ebbfeb2262ae_4",
		"5d62c0065174453eac4cf1b67c3b4614_4",
		"f5674451146141b7bf98e4431b046ba6_4",
		"eb6ca828d1814976a7aece9e70af4646_4",
		"7af13630d0d64084b09599ac34387c25_4",
	};
	
	public static String[] ArticleIds = new String[] {
		"fbf21507e4f048b882b0ea2e224cbe91_1",
		"745b22777328486f82f3649ed6efc97f_1",
		"5517b82c15ea430fa43d1ce0c712c26e_1",
		"bb822157e14b487896280dc0f5e16c6f_1",
		"20383f4f85584cca99aadf5598fa3945_1",
		"ef4ecbf8a1fb47bfa9d02db7cc1a1062_1",
		"9323bc3cc32149e6b5547df7133f0cd4_1",
		"48c3f64d448a4dafad6ace83defb493f_1",
		"ce228ab681ef4f58894477d993882dd1_1",
		"9497a2ed039c42ec90f2568db786c0dd_1",
		"a9653de494104e5e9a47ef989bfbb495_1",
		"192384b0eef64b7a848d3e02fe026a7d_1",
		"ae32fd53bd5448bfbf8a93d8ce86554b_1",
		"50371d189c6c4ca8b419cd86f99675f2_1",
		"884baacd54fb4cd19c161a05f19a2921_1",
		"4d350a741ac44564895ba4ab414ffa3a_1",
		"1d06ba95324e4a4dab14d9d3731afa23_1",
		"05927a5ebae24cf5b6113bd662436fec_1",
		"bdb8c8787eaf42feb02eaf9c2d04d846_1",
		"7ac750a265b34528b52faade56146dde_1",
		"c964b556834b486fbb8cbf079124547c_1",
	};
	
	private static String TEXT = "2010年欧洲金球奖和世界足球先生合并为FIFA金球奖，" +
            "在欧足联主席普拉蒂尼的提议下，欧足联从2010-11赛季，携手欧洲体育联盟ESM举办欧洲年度最佳球员的评选，" +
            "欧洲53个国家和地区的体育名记投票选出UEFA赛季MVP。2010-11赛季的欧足联最佳球员是巴萨巨星梅西，" +
            "2011-12赛季的欧足联最佳球员，则是在欧洲杯帮助西班牙夺冠的伊涅斯塔。2012-13赛季的欧足联最佳球员10人候选，" +
            "也在7月9日正式揭晓。欧冠冠军拜仁成为最大赢家，托马斯-穆勒、罗本、里贝里、施魏因斯泰格4人入围10人名单。" +
            "多特蒙德中锋莱万多夫斯基也得到评选人的青睐，德甲一共5人，占据半壁江山。足坛两大巨星：巴萨10号梅西，皇马头牌C罗，" +
            "也进入候选之列。2012-13赛季拿到英格兰职业球员工会最佳球员、英格兰记者协会最佳球员、英超官方赛季最佳球员的热刺红星贝尔，" +
            "也跻身10大候选之列；英超最佳射手、率领曼联夺得联赛冠军的范佩西，众望所归的进入10人名单。上赛季率领巴黎圣日耳曼夺得法甲冠军的伊布，" +
            "则是法甲独苗。值得一提的是，10人候选名单，没有意甲球员的身影。在那场决赛中，英国首相卡梅伦也出现在了中央球场的皇家包厢中，" +
            "并见证了这个历史性的时刻，称赞穆雷带来了一个历史性的胜利。他在接受英国媒体采访时称：" +
            "“这对穆雷来说是个梦幻的一天，对于英国人以及英国的网球都是这样。我们在周日上午的时候，还在疑问，我们的梦想能够变成现实满？但是下午的比赛就让我们梦想成真了。”";
	
	private static String[] bigImages = new String[]{
		"http://easyread.ph.126.net/BjRdz1ViDYwSyaQL5onNDA==/42880959817647.jpg",
		"http://easyread.ph.126.net/Pk1G0v6tdWGpwkUhrGOJ2w==/50577541454400.jpg",
		"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/30/1/lzzdvr_1396258290098.jpg",
		"http://easyread.ph.126.net/ZFgGOiNAS-3hc1Ljda_0Dg==/6597075264215424345.jpg",
		"http://pic.x1.126.net/buck-x1-prod/pic/2014/04/03/17/43/07/6/dopcox_1396518187651.jpg",
		"http://easyread.ph.126.net/5-TP3jYnvGkIAQIETHVPCw==/6597112647610641016.jpg",
		"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/38/19/6/eofhfo_1396258699577.jpg",
		"http://pic.x1.126.net/buck-x1-prod/pic/2014/03/31/17/31/42/1/peljjv_1396258302112.jpg",
	};
	
	private static Random mRandom = new Random(); 
	
	/**
	 * 
	 * @return
	 */
	public static final String getSourceImage() {
		int index = mRandom.nextInt(SourceIcons.length);
		return SourceIcons[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getSourceId() {
		int index = mRandom.nextInt(SourceIds.length);
		return SourceIds[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getBookImage() {
		int index = mRandom.nextInt(BookIcons.length);
		return BookIcons[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getBookId() {
		int index = mRandom.nextInt(BookIds.length);
		return BookIds[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getArticleImage() {
		int index = mRandom.nextInt(ArticleIcons.length);
		return ArticleIcons[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getBigImage() {
		int index = mRandom.nextInt(bigImages.length);
		return bigImages[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getArticleId() {
		int index = mRandom.nextInt(ArticleIds.length);
		return ArticleIds[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getUserImage() {
		int index = mRandom.nextInt(UserIcons.length);
		return UserIcons[index];
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getText() {
		int start = mRandom.nextInt(TEXT.length() - 10);
		int length = mRandom.nextInt(10);
		return TEXT.substring(start, start + length);
	}
	
	/**
	 * 
	 * @return
	 */
	public static final String getLongText() {
		int start = mRandom.nextInt(TEXT.length());
		int end = mRandom.nextInt(TEXT.length());
		int min = Math.min(start, end);
		int max = Math.max(start, end);
		
		return TEXT.substring(min, max);
	}
	
	public static int randomInt(int min,int max) {    
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;    
        return s;
    }
	
	 public static String getTestDataPath() {
	        File file = null; 
	        String path = null;
	        path = CacheManager.getRoot();
	        file = new File(path);
	        if(!file.exists()){
	            file.mkdirs();
	        }
	        path = file.toString() + File.separator +"testdata/";
	        if (!new File(path).exists()) {
	            new File(path).mkdirs();
	        }
	        return path;
	    }
	 
    public static void saveTestData(String fileName, String data) {
        File file = new File(getTestDataPath() + fileName);
        if (file != null) {
            if (file.exists()) {
                file.delete();
            }
            OutputStream outStream = null;
            try {
                outStream = new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }

            if (outStream != null) {
                try {
                    byte[] d = data.getBytes("utf-8");
                    outStream.write(d);
                    outStream.flush();
                    outStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        outStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
