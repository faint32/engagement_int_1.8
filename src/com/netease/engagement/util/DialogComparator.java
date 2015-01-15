package com.netease.engagement.util;

import java.util.Comparator;

public class DialogComparator implements Comparator<DialogInfo> {

	public int compare(DialogInfo o1, DialogInfo o2) {  
		if (o1.getType() > o2.getType()) {
			return 1;
		} else if (o1.getType() < o2.getType()) {
			return -1;
		} else {
			return 0;
		}
	}
	
}
