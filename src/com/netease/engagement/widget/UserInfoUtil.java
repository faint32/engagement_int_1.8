package com.netease.engagement.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.Utils.AreaTable;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.protocol.meta.OptionInfo;
import com.netease.service.protocol.meta.RankUserInfo;
import com.netease.service.protocol.meta.RecommendUserInfo;
import com.netease.service.protocol.meta.SearchUserInfo;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;

public class UserInfoUtil {
	//for test
	public static int getGender(){
		return 0 ;
	}
	
	/**
	 * @return ex. 2014年12月13日
	 */
	public static String getBirthdayText(UserInfo userInfo){
		if(userInfo == null || userInfo.birthday == 0){
			return "" ;
		}
		return TimeFormatUtil.forYMD(userInfo.birthday);
	}
	
	/**
	 * @return ex. 170CM
	 */
	public static String getHeightText(UserInfo userInfo){
		if(userInfo == null || userInfo.height == 0){
			return "" ;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(userInfo.height).append("CM");
		return sb.toString();
	}
	
	/**
	 * @return ex. 50kg
	 */
	public static String getWeightText(UserInfo userInfo){
		if(userInfo == null || userInfo.weight == 0){
			return "" ;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(userInfo.weight).append("KG");
		return sb.toString();
	}
	
	/**
	 * @return ex.  C罩杯 三围74C-43-75
	 */
	public static String getFigureText(UserInfo userInfo ,UserInfoConfig config){
		if(config == null || userInfo == null){
			return "" ;
		}
		StringBuilder sb = new StringBuilder();
		/*if(userInfo.cup != 0 && !TextUtils.isEmpty(getCup(userInfo.cup,config))){
			sb.append(getCup(userInfo.cup,config)).append(EngagementApp.getAppInstance().getString(R.string.cup));
		}*/
		//sb.append(" ").append(EngagementApp.getAppInstance().getString(R.string.sanwei));
		if(userInfo.bust !=0){
			sb.append(userInfo.bust);
		}
		else{
//		    sb.append(String.valueOf(EgmConstants.DEFAULT_BUST));
		}
		
		if(userInfo.cup !=0){
			sb.append(getCup(userInfo.cup,config));
		}
		else{
//            sb.append(String.valueOf(EgmConstants.DEFAULT_CUP));
        }
		
		if(userInfo.waist !=0){
			sb.append("-").append(userInfo.waist);
		}
		else{
//		    sb.append("-").append(String.valueOf(EgmConstants.DEFAULT_WAIST));
        }
		
		if(userInfo.hip !=0){
			sb.append("-").append(userInfo.hip);
		}
		else{
//		    sb.append("-").append(String.valueOf(EgmConstants.DEFAULT_HIP));
        }
		
		return sb.toString();
	}
	
	/**
	 * 女性会员个人主页身材项数据 ex.74C-43-75
	 */
	public static String getFigureSimple(UserInfo userInfo ,UserInfoConfig config){
		if(config == null || userInfo == null){
			return "" ;
		}
		StringBuilder sb = new StringBuilder();
		
		if(userInfo.bust !=0){
            sb.append(userInfo.bust);
        }
        else{
//            sb.append(String.valueOf(EgmConstants.DEFAULT_BUST));
        }
        
        if(userInfo.cup !=0){
            sb.append(getCup(userInfo.cup,config));
        }
        else{
//            sb.append(String.valueOf(EgmConstants.DEFAULT_CUP));
        }
        
        if(userInfo.waist !=0){
            sb.append("-").append(userInfo.waist);
        }
        else{
//            sb.append("-").append(String.valueOf(EgmConstants.DEFAULT_WAIST));
        }
        
        if(userInfo.hip !=0){
            sb.append("-").append(userInfo.hip);
        }
        else{
//            sb.append("-").append(String.valueOf(EgmConstants.DEFAULT_HIP));
        }
		
		return sb.toString();
	}
	
	/**
	 * 星座
	 */
	public static String getConstellation(UserInfo userInfo,UserInfoConfig config){
		if(config == null || userInfo == null){
			return null ;
		}
		for(OptionInfo item : config.constellation){
			if(item.key == userInfo.constellation){
				return item.value;
			}
		}
		return null ;
	}
	
	/**
	 * 获取搜索收入项
	 */
	public static OptionInfo[] getSearchIncome(UserInfoConfig config){
		if(config == null || config.searchIncome == null){
			return null ;
		}
		return config.searchIncome ;
	}
	
	/**
     * 女性会员个人主页身材项数据 ex.74C-43-75
     */
    public static String getFigureSimple(int bust, int cup, int waist, int hip, UserInfoConfig config){
        if(config == null){
            return "" ;
        }
        
        StringBuilder sb = new StringBuilder();
        
        if(bust !=0){
            sb.append(bust);
        }
        else{
            sb.append(String.valueOf(EgmConstants.DEFAULT_BUST));
        }
        
        if(cup !=0){
            sb.append(getCup(cup,config));
        }
        else{
            sb.append(String.valueOf(EgmConstants.DEFAULT_CUP));
        }
        
        if(waist !=0){
            sb.append("-").append(waist);
        }
        else{
            sb.append("-").append(String.valueOf(EgmConstants.DEFAULT_WAIST));
        }
        
        if(hip !=0){
            sb.append("-").append(hip);
        }
        else{
            sb.append("-").append(String.valueOf(EgmConstants.DEFAULT_HIP));
        }
        
        return sb.toString();
    }
    
    /** 26岁 172cm 三围74C-43-75 */
    public static String getDetailStr(Context mContext, UserInfoConfig config, int age, int height, int bust, int cup, int waist, int hip){
        String ageStr = "";
        if(age > 0){
            ageStr = mContext.getString(R.string.rec_female_age, age) + "  ";
        }
        
        String heightStr = "";
        if(height > 0){
            heightStr = mContext.getString(R.string.rec_female_heigh, height) + "  ";
        }
        
        String figure = getFigureSimple(bust, cup, waist, hip, config);
        
        StringBuilder builder = new StringBuilder();
        if(!TextUtils.isEmpty(ageStr)){
            builder.append(ageStr); 
        }
        if(!TextUtils.isEmpty(heightStr)){
            builder.append(heightStr); 
        }
        if(!TextUtils.isEmpty(figure)){
            figure = mContext.getString(R.string.rec_female_figure, figure);
            builder.append(figure); 
        }
        
        return builder.toString();
    }
    
    /** 26岁 172cm 74C-43-75 */
    public static String getDetailStr2(Context mContext, UserInfoConfig config, int age, int height, int bust, int cup, int waist, int hip){
        String ageStr = "";
        if(age > 0){
            ageStr = mContext.getString(R.string.rec_female_age, age) + "  ";
        }
        
        String heightStr = "";
        if(height > 0){
            heightStr = mContext.getString(R.string.rec_female_heigh, height) + "  ";
        }
        
        String figure = getFigureSimple(bust, cup, waist, hip, config);
        
        StringBuilder builder = new StringBuilder();
        if(!TextUtils.isEmpty(ageStr)){
            builder.append(ageStr); 
        }
        if(!TextUtils.isEmpty(heightStr)){
            builder.append(heightStr); 
        }
        if(!TextUtils.isEmpty(figure)){
            builder.append(figure); 
        }
        
        return builder.toString();
    }
	
	/**
	 * 根据整型值返回罩杯大小
	 */
	public static String getCup(int cup,UserInfoConfig config){
		if(config.cup == null || config.cup.length == 0){
			return "";
		}
		for(OptionInfo item : config.cup){
			if(item.key == cup){
				return item.value;
			}
		}
		return "";
	}
	
	/**
	 * 获取女性等级名称
	 */
	public static String getFemaleLevelName(int level,UserInfoConfig config){
		if(config.levelNameFemale == null || config.levelNameFemale.length == 0){
			return "";
		}
		for(OptionInfo item : config.levelNameFemale){
			if(item.key == level){
				return item.value;
			}
		}
		return "";
	}
	
	/**
	 * 获取男性等级名称
	 */
	public static String getMaleLevelName(int level,UserInfoConfig config){
		if(config.levelNameMale == null || config.levelNameMale.length == 0){
			return "";
		}
		for(OptionInfo item : config.levelNameMale){
			if(item.key == level){
				return item.value;
			}
		}
		return "";
	}
	
	/**
	 * 女性选择的最满意的部位
	 */
	public static String getFavorPart(int favorPart,UserInfoConfig config){
		if(config.satisfiedPart == null || config.satisfiedPart.length == 0){
			return "";
		}
		for(OptionInfo item : config.satisfiedPart){
			if(item.key == favorPart){
				return item.value;
			}
		}
		return "";
	}
	
	/**
	 * 男性选择的收入
	 */
	public static String getIncome(int income,UserInfoConfig config){
		if(config.income == null || config.income.length == 0){
			return "";
		}
		for(OptionInfo item : config.income){
			if(item.key == income){
				return item.value;
			}
		}
		return "";
	}
	
	/**
	 * 个人中心首页男性收入
	 */
	public static String getIncomeStr(int income,UserInfoConfig config){
		if(config.income == null || config.income.length == 0){
			return "";
		}
		for(OptionInfo item : config.income){
			if(item.key == income){
				return "收入"+item.value;
			}
		}
		return "";
	}
	
	/**
	 * 通过收入名称获取对应的key
	 * @param income 收入名称
	 * @param config
	 * @return
	 */
    public static int getIncomeKey(String income, UserInfoConfig config){
        if(config.income == null || config.income.length == 0){
            return -1;
        }
        
        for(OptionInfo item : config.income){
            if(item.value.equals(income)){
                return item.key;
            }
        }
        return -1;
    }
	
	/**
	 * 所在地
	 */
	public static String getLocation(Context context ,UserInfo userInfo){
		if(userInfo == null){
			return null ;
		}
		StringBuilder sb = new StringBuilder();
		String province = AreaTable.getProvinceNameById(context, userInfo.province);
		String city = AreaTable.getCityNameById(context, userInfo.province, userInfo.city);
		if(!TextUtils.isEmpty(province)){
			sb.append(province);
		}
		if(!TextUtils.isEmpty(city) && !province.equalsIgnoreCase(city)){ // 排除直辖市和港澳台
			sb.append(" ").append(city);
		}
		return sb.toString();
	}
	
	/**
	 * 获取女性配置中的兴趣列表
	 */
	public static List<String> getFemaleHobbyList(UserInfoConfig config){
		if(config == null || config.hobbyFemale.length ==0){
			return null ;
		}
		List<String> hobbyList = new ArrayList<String>();
		for(int i =0 ;i < config.hobbyFemale.length ;i++){
			hobbyList.add(config.hobbyFemale[i].value);
		}
		return hobbyList ;
	}
	
	/**
	 * 获取男性配置中的兴趣列表
	 */
	public static List<String> getMaleHobbyList(UserInfoConfig config){
		if(config == null || config.hobbyMale.length ==0){
			return null ;
		}
		List<String> hobbyList = new ArrayList<String>();
		for(int i =0 ;i < config.hobbyMale.length ;i++){
			hobbyList.add(config.hobbyMale[i].value);
		}
		return hobbyList ;
	}
	
	/**
	 * 获取配置中的约会列表
	 */
	public static List<String> getDateList(UserInfoConfig config){
		if(config == null || config.favorDate.length ==0){
			return null ;
		}
		List<String> dateList = new ArrayList<String>();
		for(int i =0 ;i < config.favorDate.length ;i++){
			dateList.add(config.favorDate[i].value);
		}
		return dateList ;
	}
	
	/**
	 * 获取配置中的最满意的部位
	 */
	public static String[] getFavorPart(UserInfoConfig config){
		if(config == null || config.satisfiedPart.length ==0){
			return null ;
		}
		String[] parts = new String[config.satisfiedPart.length];
		for(int i =0 ;i < config.satisfiedPart.length ;i++){
			parts[i] = config.satisfiedPart[i].value ;
		}
		return parts ;
	}
	
	/**
	 */
	public static int getFavorPartId(int index ,UserInfoConfig config){
		for(int i =0 ;i < config.satisfiedPart.length ;i++){
			if(index == config.satisfiedPart[i].key){
				return config.satisfiedPart[i].key;
			}
		}
		return -1 ;
	}
	
	/**
	 * 获取配置中的技能列表
	 */
	public static List<String> getSkillList(UserInfoConfig config){
		if(config == null || config.skill.length ==0){
			return null ;
		}
		List<String> skillList = new ArrayList<String>();
		for(int i =0 ;i < config.skill.length ;i++){
			skillList.add(config.skill[i].value);
		}
		return skillList ;
	}
	
	/**
	 * 获取女性用户选择的兴趣列表，转化成了字符串
	 */
	public static String getFemaleHobbyText(UserInfo userInfo,UserInfoConfig config){
		if(getFemaleHobbyList(config) == null || userInfo.hobby == null || userInfo.hobby.length ==0){
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(int k : userInfo.hobby){
			for(OptionInfo item : config.hobbyFemale){
				if(k == item.key){
					sb.append(item.value).append("，");
					break;
				}
			}
		}
		if(sb.length() > 0){
			return sb.substring(0,sb.length() -1);
		}
		return "";
	}
	
	/**
	 * 获取男性用户选择的兴趣列表，转化成了字符串
	 */
	public static String getMaleHobbyText(UserInfo userInfo,UserInfoConfig config){
		if(getMaleHobbyList(config) == null || userInfo.hobby == null || userInfo.hobby.length ==0){
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(int k : userInfo.hobby){
			for(OptionInfo item : config.hobbyMale){
				if(k == item.key){
					sb.append(item.value).append("，");
					break;
				}
			}
		}
		if(sb.length() > 0){
			return sb.substring(0,sb.length() -1);
		}
		return "";
	}
	
	/**
	 * 获取用户选择的约会列表，转化成了字符串
	 */
	public static String getDateText(UserInfo userInfo,UserInfoConfig config){
		if(getDateList(config) == null || userInfo.favorDate == null || userInfo.favorDate.length ==0){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(int k : userInfo.favorDate){
			for(OptionInfo item : config.favorDate){
				if(k == item.key){
					sb.append(item.value).append("，");
					break;
				}
			}
		}
		if(sb.length() > 0){
			return sb.substring(0,sb.length() -1);
		}
		return "";
	}
	
	/**
	 * 获取用户选择的技能列表，转化成了字符串
	 */
	public static String getSkillText(UserInfo userInfo,UserInfoConfig config){
		if(getSkillList(config) == null || userInfo.skill == null || userInfo.skill.length ==0){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(int k : userInfo.skill){
			for(OptionInfo item : config.skill){
				if(k == item.key){
					sb.append(item.value).append("，");
					break;
				}
			}
		}
		if(sb.length() > 0){
			return sb.substring(0,sb.length() -1);
		}
		return "";
	}
	
	/**
	 * @return 详细资料页列表
	 */
	public static List<String> getDetailInfoList(Context context ,UserInfo userInfo,UserInfoConfig config){
		List<String> infoList = new ArrayList<String>();
		infoList.add(String.valueOf(userInfo.uid));
		
		if(!TextUtils.isEmpty(getBirthdayText(userInfo))){
			infoList.add(getBirthdayText(userInfo));
		}else{
			infoList.add("请选择");
		}
		
		if(!TextUtils.isEmpty(getHeightText(userInfo))){
			infoList.add(getHeightText(userInfo));
		}else{
			infoList.add("请选择");
		}
		
		if(userInfo.sex == 0){
			if(!TextUtils.isEmpty(getWeightText(userInfo))){
				infoList.add(getWeightText(userInfo));
			}else{
				infoList.add("请选择");
			}
			
			if(!TextUtils.isEmpty(getFigureText(userInfo,config))){
				infoList.add(getFigureText(userInfo,config));
			}else{
				infoList.add("请选择");
			}
			
			if(!TextUtils.isEmpty(getFavorPart(userInfo.satisfiedPart,config))){
				infoList.add(getFavorPart(userInfo.satisfiedPart,config));
			}else{
				infoList.add("请选择");
			}
			
		}else if(userInfo.sex == 1){
			if(!TextUtils.isEmpty(getIncome(userInfo.income,config))){
				infoList.add(getIncome(userInfo.income,config));
			}else{
				infoList.add("请选择");
			}
		}
		
		if(!TextUtils.isEmpty(getConstellation(userInfo,config))){
			infoList.add(getConstellation(userInfo,config));
		}else{
			infoList.add("请选择");
		}
		
		if(!TextUtils.isEmpty(getLocation(context,userInfo))){
			infoList.add(getLocation(context,userInfo));
		}else{
			infoList.add("请选择");
		}
		
		if(!TextUtils.isEmpty(getDateText(userInfo,config))){
			infoList.add(getDateText(userInfo,config));
		}else{
			infoList.add("请选择");
		}
		
		if(userInfo.sex == 0){
			if(!TextUtils.isEmpty(getFemaleHobbyText(userInfo,config))){
				infoList.add(getFemaleHobbyText(userInfo,config));
			}else{
				infoList.add("请选择");
			}
		}else if(userInfo.sex == 1){
			if(!TextUtils.isEmpty(getMaleHobbyText(userInfo,config))){
				infoList.add(getMaleHobbyText(userInfo,config));
			}else{
				infoList.add("请选择");
			}
		}
		
		if(!TextUtils.isEmpty(getSkillText(userInfo,config))){
			infoList.add(getSkillText(userInfo,config));
		}else{
			infoList.add("请选择");
		}
		return infoList ;
	}
	
	/**
	 * @return 约会对应的id
	 */
	public static int[] getFavorDateIds(List<String> list,UserInfoConfig config){
		if(config == null){
			return null ;
		}
		int[] ids = new int[list.size()];
		for(int i = 0;i< list.size() ;i++){
			for(OptionInfo item : config.favorDate){
				if(list.get(i).equals(item.value)){
					ids[i] = item.key ;
					break;
				}
			}
		}
		return ids ;
	}
	
	/**
	 * @return 女性爱好对应的id
	 */
	public static int[] getFemaleHobbyIds(List<String> list,UserInfoConfig config){
		if(config == null){
			return null ;
		}
		int[] ids = new int[list.size()];
		for(int i = 0;i< list.size() ;i++){
			for(OptionInfo item : config.hobbyFemale){
				if(list.get(i).equals(item.value)){
					ids[i] = item.key ;
					break;
				}
			}
		}
		return ids ;
	}
	
	/**
	 * @return 男性爱好对应的id
	 */
	public static int[] getMaleHobbyIds(List<String> list,UserInfoConfig config){
		if(config == null){
			return null ;
		}
		int[] ids = new int[list.size()];
		for(int i = 0;i< list.size() ;i++){
			for(OptionInfo item : config.hobbyMale){
				if(list.get(i).equals(item.value)){
					ids[i] = item.key ;
					break;
				}
			}
		}
		return ids ;
	}
	
	/**
	 * @return 技能的id
	 */
	public static int[] getSkillIds(List<String> list,UserInfoConfig config){
		if(config == null){
			return null ;
		}
		int[] ids = new int[list.size()];
		for(int i = 0;i< list.size() ;i++){
			for(OptionInfo item : config.skill){
				if(list.get(i).equals(item.value)){
					ids[i] = item.key ;
					break;
				}
			}
		}
		return ids ;
	}
	
	/**
	 * 罩杯列表
	 */
	public static String[] getCups(UserInfoConfig config){
		if(config == null){
			return null ;
		}
		String[] cups = null ;
		if(config.cup != null){
			cups = new String[config.cup.length];
			int i = 0 ;
			for(OptionInfo item : config.cup){
				cups[i++] = item.value ;
			}
			return cups ;
		}
		return null ;
	}
	
	/**
	 * 收入列表
	 */
	public static String[] getIncomes(UserInfoConfig config){
		if(config == null){
			return null ;
		}
		String[] incomes = null ;
		if(config.income != null){
			incomes = new String[config.income.length];
			int i = 0 ;
			for(OptionInfo item : config.income){
				incomes[i++] = item.value ;
			}
			return incomes ;
		}
		return null ;
	}
	
	/**
	 * 星座列表
	 */
	public static String[] getColletations(UserInfoConfig config){
		if(config == null){
			return null ;
		}
		String[] colletations = null ;
		if(config.constellation != null){
			colletations = new String[config.constellation.length];
			int i = 0 ;
			for(OptionInfo item : config.constellation){
				colletations[i++] = item.value ;
			}
			return colletations ;
		}
		return null ;
	}
	
    public static String getRankMaleLevel(RankUserInfo userInfo) {
        if (userInfo != null) {
            return "LV" + userInfo.level + " " + userInfo.levelName;
        } else {
            return "";
        }
    }

    public static String getSearchMaleLevel(SearchUserInfo userInfo) {
        if (userInfo != null) {
            return "LV" + userInfo.level + " " + userInfo.levelName;
        } else {
            return "";
        }
    }

    public static String getRecommendMaleLevel(RecommendUserInfo userInfo) {
        if (userInfo != null) {
            return "LV" + userInfo.level + " " + userInfo.levelName;
        } else {
            return "";
        }
    }
}
