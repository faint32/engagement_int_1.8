package com.netease.engagement.widget.emot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.netease.date.R;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.EmotConfigManager;
import com.netease.service.protocol.meta.EmotConfigResult;
import com.netease.service.protocol.meta.EmoticonGroupInfo;
import com.netease.service.protocol.meta.EmotionInfo;
import com.netease.share.sticker.model.EmojiUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;

/**
 * 表情管理器，负责表情名称、图标的访问，表情使用频率管理，以及对文本中的表情（文字）Span化。
 * 实现了多套表情的接口
 */
public class EmoticonMgr {

	public static String[] EMOTICON_GROUP_NAME ;
	private static EmoticonMgr sInstance;
	private Context mContext;

	private static LinkedHashMap<String,LinkedHashMap<String, String>> groupName_to_emot ;

	private static Pattern mEmoticonPattern = Pattern.compile(EmotSpan.REGULAR);
	
	private static EmotConfigResult mEmotConfigResult ;

	public static EmoticonMgr getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new EmoticonMgr(context);
		}
		return sInstance;
	}

	private EmoticonMgr(Context context) {
		mContext = context;
	}

	/**
	 * 通过表情名称获取表情Icon图像。
	 */
	public int getIcon(String emotName){
		Context context = EngagementApp.getAppInstance().getApplicationContext();
		String resTypeName = context.getResources().getResourceTypeName(R.drawable.icon_delete);
		String emotId = getIconId(emotName);
		if(!TextUtils.isEmpty(emotId)){
			int resIdentifier = context.getResources().getIdentifier(emotId, resTypeName, context.getPackageName());
			if(resIdentifier != 0){
				return resIdentifier ;
			}
		}
		return 0 ;
	}
	
	/**
	 * 通过表情名称获取表情Icon图像。  Icon图像保存在assert中
	 */
	public Bitmap getIconBitmap(String emotName) {
		Context context = EngagementApp.getAppInstance().getApplicationContext();
		Drawable d = EmojiUtil.getDrawableByNv(context, emotName);
		if (d == null) {
			return null;
		}
		Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
		return bitmap;
	}
	
	public String getIconId(String emotName){
		Iterator<Entry<String,LinkedHashMap<String,String>>> set = groupName_to_emot.entrySet().iterator();
		while(set.hasNext()){
			LinkedHashMap<String,String> map = set.next().getValue();
			if(map.get(emotName) != null){
				return map.get(emotName);
			}
		}
		return null ;
	}

	public int setEmoticonSpan(Spannable sp, float textHeight) {
		return setEmoticonSpan(sp, null, textHeight);
	}
	
	public ArrayList<String> getNamesByGroup(String groupName){
		if(groupName_to_emot != null && groupName_to_emot.get(groupName)!= null){
			ArrayList<String> emotNames = new ArrayList<String>();
			Iterator<String> iterator = groupName_to_emot.get(groupName).keySet().iterator();
			while(iterator.hasNext()){
				emotNames.add((String) iterator.next());
			}
			return emotNames;
		}
		return null ;
	}

	/**
	 * 将一段文字中的表情（名称字符）转换为表情Span。
	 */
	public int setEmoticonSpan(Spannable sp, int[] select, float textHeight) {
		int spanCount = 0;
		if (sp == null || sp.length() < 2)
			return spanCount;

		Matcher emoMatcher = mEmoticonPattern.matcher(sp);

		EmotSpan[] spans = sp.getSpans(0, sp.length(), EmotSpan.class);

		while (true) {
			if (!emoMatcher.find())
				break;

			int begin = emoMatcher.start();
			int end = emoMatcher.end();
			if (begin < end) {
				String phrase = sp.subSequence(begin, end).toString();
				if (isSpanExist(spans, sp, phrase, begin, end)) // 已经设置过的表情Span则不再处理。
					continue;
//				int resId = getIcon(phrase);
				Bitmap bitmap = getIconBitmap(phrase);
				if (bitmap != null) {
					/* 去掉背景Span */
					BackgroundColorSpan[] bgcSpns = sp.getSpans(begin, end,BackgroundColorSpan.class);
					if (bgcSpns != null) {
						for (int i = 0; i < bgcSpns.length; i++)
							sp.removeSpan(bgcSpns[i]);
					}

					/* 换上表情Span */
					sp.setSpan(new EmotSpan(mContext, bitmap, ImageSpan.ALIGN_BOTTOM, phrase,(int)(textHeight)), 
							begin, 
							end,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					/*
					 * 之前被选中的文字如果含有表情的名称，那么现在它已经被替代为icon的span，就去掉icon上的选中效果（更改select）
					 */
					//暂时注释掉
					/*if (select != null) {
						if (select[0] > begin && select[0] < end) {
							select[0] = end;
						}
						if (select[1] > begin && select[1] < end) {
							select[1] = end;
						}
					}*/
					spanCount++;
				}
			}
		}
		return spanCount + spans.length;
	}

	/**
	 * 判断指定的表情Span是否存在。
	 * 
	 * @param spans
	 *            表情Span列表
	 * @param sp
	 * @param phrase
	 *            表情Span名称
	 * @param begin
	 *            表情Span起始位置
	 * @param end
	 *            表情Span结束位置
	 * @return
	 */
	private boolean isSpanExist(EmotSpan[] spans, Spannable sp,
			String phrase, int begin, int end) {
		if (spans == null || spans.length == 0)
			return false;

		int count = spans.length;
		for (int i = 0; i < count; i++) {
			int spanStart = sp.getSpanStart(spans[i]);
			int spanEnd = sp.getSpanEnd(spans[i]);

			if (spanStart != begin || spanEnd != end)
				continue;

			if (spanStart == begin && spanEnd == end
					&& spans[i].mPhrase.equals(phrase))
				return true;
		}

		return false;
	}

	static {
		groupName_to_emot = new LinkedHashMap<String,LinkedHashMap<String, String>>();
		
		mEmotConfigResult = EmotConfigManager.getInstance().getEmotConfigFromData();
		
		if(mEmotConfigResult != null){
			EmoticonGroupInfo[] emotGroupInfo = mEmotConfigResult.emoticonGroupList ;
			
			if(emotGroupInfo != null && emotGroupInfo.length > 0){
				EMOTICON_GROUP_NAME = new String[emotGroupInfo.length];
				int i = 0 ;
				for(EmoticonGroupInfo item : emotGroupInfo){
					LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
					EMOTICON_GROUP_NAME[i++] = item.emoticonGroupName ;
					EmotionInfo[] infos = item.emoticons ;
					for(EmotionInfo info : infos){
						map.put(info.name,info.id);
					}
					groupName_to_emot.put(item.emoticonGroupName,map);
				}
			}
		}
	}
}
