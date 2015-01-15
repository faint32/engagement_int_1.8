package com.netease.engagement.widget.richtext;

import android.text.Spannable;

public interface IRichText {

    /**
     * 设置富文本
     * 
     * @param str
     */
    public void setRichText(CharSequence cs);

    /**
     * 添加正则匹配
     * 
     * @param regular
     */
    public void addMatcher(String regular);

    /**
     * 开关富文本效果
     * 
     * @param enable
     */
    public void setRichTextEnable(boolean enable);

    /**
     * 将显示效果转换为富文本
     */
    public void toRichText(Spannable sp);
}
