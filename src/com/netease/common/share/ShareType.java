package com.netease.common.share;

/**
 * 以后再加的话，数字只能网上加
 * @author dingding
 *
 */
public enum ShareType {

	Sina(1),
	Netease(2),
	Qqmblog(3),
	Tencent(4),
	Renren(5),
	Douban(6),
	Kaixin(7),
	Sohu(8),
	;
	
	private int value = 0;

    private ShareType(int value) {
        this.value = value;
    }

    public static ShareType valueOf(int value) {
        switch (value) {
        case 1:
            return Sina;
        case 2:
        	return Netease;
        case 3:
        	return Qqmblog;
        case 4:
        	return Tencent;
        case 5:
            return Renren;
        case 6:
        	return Douban;
        case 7:
            return Kaixin;
        case 8:
        	return Sohu;
        default:
            return null;
        }
    }

    public int value() {
        return this.value;
    }
	
}
