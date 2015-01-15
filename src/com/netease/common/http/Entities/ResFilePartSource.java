package com.netease.common.http.Entities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class ResFilePartSource implements PartSource {

	public static final String NAME_PREFIX = "android_res://";
	
	InputStream mInputStream;
	String mName;
	
	public ResFilePartSource(Context context, String name) throws FileNotFoundException {
		this(context, name, null);
	}
	
	public ResFilePartSource(Context context, String name, String partName) throws FileNotFoundException {
		if (context == null || name == null) {
			throw new FileNotFoundException("Context is empty.");
		}
		
		try {
			int id = Integer.parseInt(name.substring(NAME_PREFIX.length()));
			mInputStream = context.getResources().openRawResource(id);
			mName = partName != null ? partName : context.getResources().getResourceName(id);
		} catch (Exception e) {
			throw new FileNotFoundException(name + " res not found");
		}
	}
	
	@Override
	public long getLength() {
		try {
			return mInputStream.available();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getFileName() {
		return mName;
	}

	@Override
	public InputStream createInputStream() throws IOException {
		return mInputStream;
	}

}
