package com.netease.framework.skin;

import java.net.MalformedURLException;
import java.net.URL;

import android.webkit.URLUtil;

public class UrlToPath implements IPathConvert {

	@Override
	public String convert(String url) {
		String path = null;
		
		if (URLUtil.isNetworkUrl(url)) {
			String file = null;
			try {
				URL Url = new URL(url);
				file = Url.getFile();
				path = SkinConfig.DEST_ROOT + file;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		return path;
	}

}
