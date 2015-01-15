package com.netease.engagement.widget.richtext;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;

public class RichTextParser {
	
	private List<String> mPatternList;

    public RichTextParser() {
        mPatternList = new LinkedList<String>();
    }

    public void addMatcher(String regular) {
        mPatternList.add(regular);
    }

    public void parseRichText(Spannable sp) {
        if (mListener != null) {
            for (int i = 0; i < mPatternList.size(); i++) {
                Pattern p = Pattern.compile(mPatternList.get(i), Pattern.CASE_INSENSITIVE);
                Matcher matcher = p.matcher(sp);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    if (mListener != null)
                        mListener.onMatched(mPatternList.get(i), start, end);
                }
            }
        }
    }
    
    private OnRichTextMatchListener mListener;
    public interface OnRichTextMatchListener {
        public void onMatched(String regularExp, int start, int end);
    }
    public void setOnRichTextMatchListener(OnRichTextMatchListener listener) {
        mListener = listener;
    }
}
