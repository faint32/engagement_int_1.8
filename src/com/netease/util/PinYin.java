package com.netease.util;

//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.netease.common.config.IConfig;

import android.content.Context;
import android.content.res.AssetManager;

public class PinYin implements IConfig {
	
	
	
	/************************以下 IConfig 配置项*******************************/
	
	// 缺省首字母
	public static char DefaultFristChar = '#';
	
	/************************以上 IConfig 配置项*******************************/
	
	public static final int UNICODE_CN_START = 0x4e00;
	public static final int UNICODE_CN_END = 0x9fa5;
	private static final int BASE_PINYIN_INDEX = 256;
	
	public static final int SECOND_NUM = 16 * 6 * 32;
	private static PinYin pinYin;

	private byte[] pinyinIndex;
	//private Hashtable secondMap;

	private static String[] name = { "zuo", "zun", "zui", "zuan", "zu", "zou", "zong",
			"zi", "zhuo", "zhun", "zhui", "zhuang", "zhuan", "zhuai", "zhua",
			"zhu", "zhou", "zhong", "zhi", "zheng", "zhen", "zhe", "zhao",
			"zhang", "zhan", "zhai", "zha", "zeng", "zen", "zei", "ze", "zao",
			"zang", "zan", "zai", "za", "yun", "yue", "yuan", "yu", "you",
			"yong", "yo", "ying", "yin", "yi", "ye", "yao", "yang", "yan",
			"ya", "xun", "xue", "xuan", "xu", "xiu", "xiong", "xing", "xin",
			"xie", "xiao", "xiang", "xian", "xia", "xi", "wu", "wo", "weng",
			"wen", "wei", "wang", "wan", "wai", "wa", "tuo", "tun", "tui",
			"tuan", "tu", "tou", "tong", "ting", "tie", "tiao", "tian", "ti",
			"teng", "te", "tao", "tang", "tan", "tai", "ta", "suo", "sun",
			"sui", "suan", "su", "sou", "song", "si", "shuo", "shun", "shui",
			"shuang", "shuan", "shuai", "shua", "shu", "shou", "shi", "sheng",
			"shen", "she", "shao", "shang", "shan", "shai", "sha", "seng",
			"sen", "se", "sao", "sang", "san", "sai", "sa", "ruo", "run",
			"rui", "ruan", "ru", "rou", "rong", "ri", "reng", "ren", "re",
			"rao", "rang", "ran", "qun", "que", "quan", "qu", "qiu", "qiong",
			"qing", "qin", "qie", "qiao", "qiang", "qian", "qia", "qi", "pu",
			"po", "ping", "pin", "pie", "piao", "pian", "pi", "peng", "pen",
			"pei", "pao", "pang", "pan", "pai", "pa", "ou", "o", "nuo", "nue",
			"nuan", "nv", "nu", "nong", "niu", "ning", "nin", "nie", "niao",
			"niang", "nian", "ni", "neng", "nen", "nei", "ne", "nao", "nang",
			"nan", "nai", "na", "mu", "mou", "mo", "miu", "ming", "min", "mie",
			"miao", "mian", "mi", "meng", "men", "mei", "me", "mao", "mang",
			"man", "mai", "ma", "luo", "lun", "lue", "luan", "lv", "lu", "lou",
			"long", "liu", "ling", "lin", "lie", "liao", "liang", "lian",
			"lia", "li", "leng", "lei", "le", "lao", "lang", "lan", "lai",
			"la", "kuo", "kun", "kui", "kuang", "kuan", "kuai", "kua", "ku",
			"kou", "kong", "keng", "ken", "ke", "kao", "kang", "kan", "kai",
			"ka", "jun", "jue", "juan", "ju", "jiu", "jiong", "jing", "jin",
			"jie", "jiao", "jiang", "jian", "jia", "ji", "huo", "hun", "hui",
			"huang", "huan", "huai", "hua", "hu", "hou", "hong", "heng", "hen",
			"hei", "he", "hao", "hang", "han", "hai", "ha", "guo", "gun",
			"gui", "guang", "guan", "guai", "gua", "gu", "gou", "gong", "geng",
			"gen", "gei", "ge", "gao", "gang", "gan", "gai", "ga", "fu", "fou",
			"fo", "feng", "fen", "fei", "fang", "fan", "fa", "er", "en", "e",
			"duo", "dun", "dui", "duan", "du", "dou", "dong", "diu", "ding",
			"die", "diao", "dian", "di", "deng", "de", "dao", "dang", "dan",
			"dai", "da", "cuo", "cun", "cui", "cuan", "cu", "cou", "cong",
			"ci", "chuo", "chun", "chui", "chuang", "chuan", "chuai", "chu",
			"chou", "chong", "chi", "cheng", "chen", "che", "chao", "chang",
			"chan", "chai", "cha", "ceng", "ce", "cao", "cang", "can", "cai",
			"ca", "bu", "bo", "bing", "bin", "bie", "biao", "bian", "bi",
			"beng", "ben", "bei", "bao", "bang", "ban", "bai", "ba", "ao",
			"ang", "an", "ai", "a" };
	
	private static char[] firstLetters = new char[] {
		'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z',
		'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z',
		'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z',
		'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z',
		'Z', 'Z', 'Z', 'Z', 'Y', 'Y', 'Y', 'Y', 'Y',
		'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y',
		'Y', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X',
		'X', 'X', 'X', 'X', 'X', 'X', 'W', 'W', 'W',
		'W', 'W', 'W', 'W', 'W', 'W', 'T', 'T', 'T',
		'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T',
		'T', 'T', 'T', 'T', 'T', 'T', 'T', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'R', 'R',
		'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R',
		'R', 'R', 'R', 'Q', 'Q', 'Q', 'Q', 'Q', 'Q',
		'Q', 'Q', 'Q', 'Q', 'Q', 'Q', 'Q', 'Q', 'P',
		'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P',
		'P', 'P', 'P', 'P', 'P', 'P', 'O', 'O', 'N', 'N',
		'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
		'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
		'N', 'N', 'N', 'M', 'M', 'M', 'M', 'M', 'M', 'M',
		'M', 'M', 'M', 'M', 'M', 'M', 'M', 'M', 'M',
		'M', 'M', 'M', 'L', 'L', 'L', 'L', 'L', 'L', 'L',
		'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L',
		'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L',
		'L', 'K', 'K', 'K', 'K', 'K', 'K', 'K', 'K',
		'K', 'K', 'K', 'K', 'K', 'K', 'K', 'K', 'K',
		'K', 'J', 'J', 'J', 'J', 'J', 'J', 'J', 'J',
		'J', 'J', 'J', 'J', 'J', 'J', 'H', 'H', 'H',
		'H', 'H', 'H', 'H', 'H', 'H', 'H', 'H', 'H',
		'H', 'H', 'H', 'H', 'H', 'H', 'H', 'G', 'G',
		'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G',
		'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'F', 'F',
		'F', 'F', 'F', 'F', 'F', 'F', 'F', 'E', 'E', 'E',
		'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D',
		'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D',
		'D', 'D', 'C', 'C', 'C', 'C', 'C', 'C', 'C',
		'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C',
		'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C',
		'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C',
		'C', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B',
		'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'A',
		'A', 'A', 'A', 'A'
	};

	private PinYin() {

	}
	
	public static PinYin getInstance() {
		if (pinYin == null) {
			pinYin = new PinYin();
		}
		
		return pinYin;
	}

	public void release(){
		if(pinyinIndex != null){
			pinyinIndex = null;
		}
	}
	
	/**
	 * 获取中英文首字母
	 * @param context
	 * @param gb2312
	 * @return
	 */
	public char getFirstLetter(Context context, char gb2312) {
		if (gb2312 < UNICODE_CN_START) {
			// '0' -> 48; '9' -> 57; 'a' -> 97; 'z' -> 122; 'A' -> 65; 'Z' -> 90;
//			if (gb2312 <= '0') {
//				
//			}
//			else if (gb2312 <= '9') {
//				
//			}
			if (gb2312 < 'A') {
				gb2312 = DefaultFristChar;
			} else if (gb2312 <= 'Z') {
//				gb2312 = gb2312;
			} else if (gb2312 < 'a') {
				gb2312 = DefaultFristChar;
			} else if (gb2312 <= 'z') {
				gb2312 -= 32;
			} else {
				gb2312 = DefaultFristChar;
			}
		}
		else if (gb2312 > UNICODE_CN_END) {
			gb2312 = DefaultFristChar;
		}
		else {
			int index = getCustomPinyin(context, gb2312);
			index = name.length - (index - BASE_PINYIN_INDEX);
			if (index < 0 || index >= name.length) {
				gb2312 = DefaultFristChar;
			}
			else {
				gb2312 = firstLetters[index];
			}
		}
		
		return gb2312;
	}
	
//	public String getPinyin(String gb2312) {
//		if (null == gb2312 || "".equals(gb2312.trim())) {
//			return gb2312;
//		}
//		char[] chars = gb2312.toCharArray();
//		StringBuffer retuBuf = new StringBuffer();
//		for (int i = 0, Len = chars.length; i < Len; i++) {
//			retuBuf.append(getPinyin(chars[i]));
//		} // end of for
//		return retuBuf.toString();
//	}
//
//	public String getPinyin(char ch) {
//		if (pinyinIndex == null) {
//			
//			try {
//				InputStream bi = getClass().getResourceAsStream("/u2pinyin.dat");
//				pinyinIndex = new byte[(UNICODE_CN_END - UNICODE_CN_START + 1) * 2];
//				bi.read(pinyinIndex);
//				bi.close();
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				if (Log.DEBUG && Log.debug_level <= Log.DEBUG_LEVEL_3) {
//					e.printStackTrace();
//				}
//				return "" + ch;
//			}
//		}
//		
//		int index = (pinyinIndex[(ch - UNICODE_CN_START) * 2] << 8 
//				| (0xff & pinyinIndex[(ch - UNICODE_CN_START) * 2 + 1]));
//		
//		if (index == 0) {
//			
//			return "?";
//		}
//		
//		return name[name.length - index];
//	}
//	
//	/**
//	 * 判断字符是不是中文
//	 * @param c
//	 * @return 是返回true,不是返会false
//	 */
//	public boolean isChinese(char c) {
//		if (c >= UNICODE_CN_START && c <= UNICODE_CN_END) {
//			
//			return true;
//		}
//		
//		return false;
//	}
//
//	public void getCnStringAscii(String cnStr, StringBuffer pinyin) {
//
//		if (pinyin != null) {
//			pinyin.setLength(0);
//		} else {
//			pinyin = new StringBuffer();
//		}
//
//		char[] chars = cnStr.toCharArray();
//		for (int i = 0; i < chars.length; i++) {
//			if (chars[i] >= UNICODE_CN_START && chars[i] <= UNICODE_CN_END) {
//				pinyin.append(getPinyin(chars[i]));
//			}
//			else if (chars[i] >= 0 && chars[i] <= 255) {
//				if (chars[i] >= 65 && chars[i] <= 90) {
//					pinyin.append((char) (chars[i] + 32)); // 转成小写字母
//				} else {
//					pinyin.append(chars[i]);
//				}
//			}
//			else {
//				pinyin.append(chars[i]);
//			}
//		}
//	}
//	
	public String getCustomPinyin(Context context, String gb2312) {
		if (null == gb2312 || "".equals(gb2312.trim())) {
			return gb2312;
		}
//		char[] chars = gb2312.toCharArray();
		StringBuffer retuBuf = new StringBuffer();
		getCustomPinyin(context, gb2312, retuBuf);
//		for (int i = 0, Len = chars.length; i < Len; i++) {
//			retuBuf.append(getPinyin(chars[i]));
//		} // end of for
		return retuBuf.toString();
	}
	
	private char getCustomPinyin(Context context, char ch) {
		if (pinyinIndex == null) {
			
			try {
				AssetManager am = context.getAssets();
				InputStream bi = am.open("u2pinyin.dat");
//				InputStream bi = getClass().getResourceAsStream("/u2pinyin.dat");
				
				pinyinIndex = new byte[(UNICODE_CN_END - UNICODE_CN_START + 1) * 2];
				bi.read(pinyinIndex);
				bi.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return BASE_PINYIN_INDEX;
			}
		}
		
		if (ch < UNICODE_CN_START || ch > UNICODE_CN_END) {
			return ch;
		}
		
		int index = (pinyinIndex[(ch - UNICODE_CN_START) * 2] << 8 
				| (0xff & pinyinIndex[(ch - UNICODE_CN_START) * 2 + 1]));
		
		return (char) (index + BASE_PINYIN_INDEX);
	}
	
	public void getCustomPinyin(Context context, String cnStr, StringBuffer pinyin) {
		if (pinyin != null) {
			pinyin.setLength(0);
		} else {
			pinyin = new StringBuffer();
		}

		char[] chars = cnStr.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] >= UNICODE_CN_START && chars[i] <= UNICODE_CN_END) {
				pinyin.append(getCustomPinyin(context, chars[i]));
			}
			else if (chars[i] >= 0 && chars[i] <= 255) {
				if (chars[i] >= 65 && chars[i] <= 90) {
					pinyin.append((char) (chars[i] + 32)); // 转成小写字母
				} else {
					pinyin.append(chars[i]);
				}
			}
			else {
				pinyin.append(chars[i]);
			}
		}
	}
	
	private static boolean validPinyin(String regex, int regexIndex, 
			String plain, String pinyin, int index) {
		
		int i = regexIndex, j = index;
		if (index == 0) {
			for (int k = 0; k < plain.length(); k++) {
				if (validPinyin(regex, regexIndex, plain, pinyin, k + 1)) {
					return true;
				}
			}
		}
		for (; i < regex.length() && j < plain.length(); ) {
			if (regex.charAt(i) == plain.charAt(j)) {
				j++;
				i++;
			}
			else {
				if (plain.charAt(j) >= UNICODE_CN_START 
						&& plain.charAt(j) <= UNICODE_CN_END) {
					int ix = name.length - (pinyin.charAt(j) - BASE_PINYIN_INDEX);
					if (ix < 0 || ix >= name.length) {
						return false;
					}
					String py = name[ix];
					for (int k = 0; k < py.length(); k++) {
						if (regex.charAt(i + k) == py.charAt(k)) {
							if(validPinyin(regex, i + k + 1, plain, pinyin, j + 1)) {
								return true;
							}
						} else {
							return false;
						}
					}
					return false;
				} 
				else if (plain.charAt(j) == ' ' 
						|| plain.charAt(j) == '　') {
					j++;
				}
				else if (plain.charAt(j) >= 65 && plain.charAt(j) <= 90) {
					if (plain.charAt(j) + 32 == regex.charAt(i)) {
						j++;
						i++;
					} else {
						return false;
					}
				}
				else {
					return false;
				}
			}
		}
		
		if (i == regex.length()) {
			return true;
		}
		
		return false;
	}
	
	public static boolean validPinyin(String regex, String plain, String pinyin) {
//		if (regex.length() == 0) {
//			return true;
//		}
//		
//		regex = regex.trim().toLowerCase();
		
		if (plain == null || plain.length() == 0 || pinyin == null
				|| plain.length() != pinyin.length()) {
			return false;
		}
		
		return validPinyin(regex, 0, plain, pinyin, 0);
	}
	
	public static int comparePinyinFrist(String plain1, String py1, String plain2, String py2) {
		if (plain1 == null || py1 == null 
				|| plain1.length() != py1.length()) {
			return -1;
		}
		if (plain2 == null || py2 == null
				|| plain2.length() != py2.length()) {
			return 1;
		}
//		char[] pc1 = plain1.toCharArray();
//		char[] pc2 = plain2.toCharArray();
		
		for (int i = 0; i < plain1.length() && i < plain2.length(); i++) {
			if (plain1.charAt(i) != plain2.charAt(i)) {
				if (plain1.charAt(i) >= UNICODE_CN_START 
						&& plain1.charAt(i) <= UNICODE_CN_END) {
					if (plain2.charAt(i) >= UNICODE_CN_START 
							&& plain2.charAt(i) <= UNICODE_CN_END) {
						if (py1.charAt(i) != py2.charAt(i)) {
							return py1.charAt(i) < py2.charAt(i) ? -1 : 1; 
						}
					} else {
//						if (py2.charAt(i) >= 97 && py2.charAt(i) <= 122) {
//							int ix = name.length - (py1.charAt(i) - BASE_PINYIN_INDEX);
//							if (ix < 0 || ix >= name.length) {
//								return -1;
//							}
//							String py = name[ix];
////							if (py.charAt(0) == py2.charAt(i)) {
////								return -1;
////							}
////							return py.charAt(0) < py2.charAt(i) ? 1 : -1; 
//							return py.charAt(0) < py2.charAt(i) ? 1 : -1; 
//						}
						return -1;
					}
				} else {
					if (plain2.charAt(i) >= UNICODE_CN_START 
							&& plain2.charAt(i) <= UNICODE_CN_END) {
//						if (py1.charAt(i) >= 97 && py1.charAt(i) <= 122) {
//							int ix = name.length - (py2.charAt(i) - BASE_PINYIN_INDEX);
//							if (ix < 0 || ix >= name.length) {
//								return 1;
//							}
//							String py = name[ix];
////							if (py1.charAt(i) == py.charAt(0)) {
////								return 1;
////							}
////							return py1.charAt(i) < py.charAt(0) ? 1 : -1; 
//							return py1.charAt(i) <= py.charAt(0) ? 1 : -1; 
//						}
						return 1;
					} else {
						if (py1.charAt(i) != py2.charAt(i)) {
							return py1.charAt(i) < py2.charAt(i) ? -1 : 1; 
						}
					}
				}
			}
		}
		
		if (plain1.length() == plain2.length()) {
			return 0;
		}
		
		return plain1.length() < plain2.length() ? 1 : -1;
	}
	
	public static int comparePinyin(String plain1, String py1, String plain2, String py2) {
		if (plain1 == null || py1 == null 
				|| plain1.length() != py1.length()) {
			return -1;
		}
		if (plain2 == null || py2 == null
				|| plain2.length() != py2.length()) {
			return 1;
		}
//		char[] pc1 = plain1.toCharArray();
//		char[] pc2 = plain2.toCharArray();
		
		for (int i = 0; i < plain1.length() && i < plain2.length(); i++) {
			if (plain1.charAt(i) != plain2.charAt(i)) {
				if (plain1.charAt(i) >= UNICODE_CN_START 
						&& plain1.charAt(i) <= UNICODE_CN_END) {
					if (plain2.charAt(i) >= UNICODE_CN_START 
							&& plain2.charAt(i) <= UNICODE_CN_END) {
						if (py1.charAt(i) != py2.charAt(i)) {
							return py1.charAt(i) < py2.charAt(i) ? 1 : -1; 
						}
					} else {
						if (py2.charAt(i) >= 97 && py2.charAt(i) <= 122) {
							int ix = name.length - (py1.charAt(i) - BASE_PINYIN_INDEX);
							if (ix < 0 || ix >= name.length) {
								return -1;
							}
							String py = name[ix];
//							if (py.charAt(0) == py2.charAt(i)) {
//								return -1;
//							}
//							return py.charAt(0) < py2.charAt(i) ? 1 : -1; 
							return py.charAt(0) < py2.charAt(i) ? 1 : -1; 
						}
						return -1;
					}
				} else {
					if (plain2.charAt(i) >= UNICODE_CN_START 
							&& plain2.charAt(i) <= UNICODE_CN_END) {
						if (py1.charAt(i) >= 97 && py1.charAt(i) <= 122) {
							int ix = name.length - (py2.charAt(i) - BASE_PINYIN_INDEX);
							if (ix < 0 || ix >= name.length) {
								return 1;
							}
							String py = name[ix];
//							if (py1.charAt(i) == py.charAt(0)) {
//								return 1;
//							}
//							return py1.charAt(i) < py.charAt(0) ? 1 : -1; 
							return py1.charAt(i) <= py.charAt(0) ? 1 : -1; 
						}
						return 1;
					} else {
						if (py1.charAt(i) != py2.charAt(i)) {
							return py1.charAt(i) < py2.charAt(i) ? 1 : -1; 
						}
					}
				}
			}
		}
		
		if (plain1.length() == plain2.length()) {
			return 0;
		}
		
		return plain1.length() < plain2.length() ? 1 : -1;
	}
}