package com.netease.service.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.netease.util.PinYin;


public class ChinesePinyinUtils {
    /** 
     * 将字符串中的中文转化为拼音,其他字符不变 
     *  
     * @param inputString 
     * @return 
     */  
//    public static String getPingYin(String inputString) {  
//        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();  
//        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);  
//        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  
//        format.setVCharType(HanyuPinyinVCharType.WITH_V);  
//
//        char[] input = inputString.trim().toCharArray();  
//        String output = "";  
//
//        try {  
//            for (int i = 0; i < input.length; i++) {  
//                if (java.lang.Character.toString(input[i]).matches(  
//                        "[\\u4E00-\\u9FA5]+")) {  
//                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(  
//                            input[i], format);  
//                    output += temp[0];  
//                } else  
//                    output += java.lang.Character.toString(input[i]).toLowerCase();  
//            }  
//        } catch (BadHanyuPinyinOutputFormatCombination e) {  
//            e.printStackTrace();  
//        }
//        return output;  
//    }  
//
//  
//    public static String converterToFirstSpell(String chinese) {  
//      String pinyinName = "";
//      String firstChar = chinese.substring(0, 1);
//      HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
//      defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
//      defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//      if (firstChar.matches("[\\u4E00-\\u9FA5]+")) {
//          try {
//              String[] temp = PinyinHelper.toHanyuPinyinStringArray(firstChar.toCharArray()[0], defaultFormat);
//              if (temp != null) {
//                  pinyinName += temp[0].charAt(0);
//              } else {
//                  pinyinName += "*";
//              }
//          } catch (BadHanyuPinyinOutputFormatCombination e) {
//              e.printStackTrace();
//          }
//      } else if (isLetter(firstChar)) {
//          pinyinName += firstChar.toUpperCase();
//      } else {
//          pinyinName += "*";
//      }
//      return pinyinName; 
//    }  
    
    /**
     * 用来做比对，显示出来不是全拼的拼音
     * @param c
     * @param inputString
     * @return
     */
    public static String getPingYin(Context c,String inputString) {  
        return PinYin.getInstance().getCustomPinyin(c, inputString);
    } 
    
    /**
     * 拿出首字母
     * @param c
     * @param chinese
     * @return
     */
    public static String converterToFirstSpell(Context c,String chinese) {  
        if(chinese == null || chinese.length() == 0){
            return "#";
        }
        return Character.toString(PinYin.getInstance().getFirstLetter(c, chinese.charAt(0)));
    }
    
    /**
     * 搜索匹配，支持中文首字母，暂时不支持多音字
     * @return
     */
    public static boolean searchCondition(Context c,String condition,String text){
        
        return PinYin.validPinyin(condition, text, getPingYin(c, text));
    }
    
    /**
     * 效率好一点  不用导拼音库
     * @param condition
     * @param text
     * @param textpinyin
     * @return
     */
    public static boolean searchCondition(String condition,String text,String textpinyin){
        
        return PinYin.validPinyin(condition, text, textpinyin);
    }
    
    
    /**
     * 直接两个字符串比较
     * @param c
     * @param s1
     * @param s2
     * @return
     */
    public static int comparePinyin(Context c,String s1,String s2){
        
        String pinyin1 = PinYin.getInstance().getCustomPinyin(c,s1);
        String pinyin2 = PinYin.getInstance().getCustomPinyin(c,s2);
        
        return PinYin.comparePinyin(s1, pinyin1, s2, pinyin2);
    }
    
    public static boolean isNumber(String condition){
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(condition);
        return matcher.matches();
    }
    
    public static boolean isLetter(String condition){
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(condition);
        return matcher.matches();
    }
    
    public static boolean isLetterOrNumber(String condition){
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
        Matcher matcher = pattern.matcher(condition);
        return matcher.matches();
    }
    
//    /**支持多音字, 不支持首字母全拼混合
//     * 
//   * */
//  public static Set<String> getAllPinyinForSearch2(List<String> pinyin) {
//      StringBuilder wholeSpell = new StringBuilder();
//      StringBuilder firstSpell = new StringBuilder();
//      for (String s : pinyin) {
//          if (TextUtils.isEmpty(s)) {
//              continue;
//          }
//          
//          wholeSpell.append(s);
//          firstSpell.append(s.charAt(0));
//      }
//      
//      //if wholeSpell and firstSpell equals, then resultAll only has one item;
//      Set<String> resultAll = new HashSet<String>();
//      resultAll.add(wholeSpell.toString());
//      resultAll.add(firstSpell.toString());
//      return resultAll;
//  }

}
