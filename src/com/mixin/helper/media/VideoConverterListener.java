package com.mixin.helper.media;

public interface VideoConverterListener {

	// execute sail
	public void onStart();

	// execute ing
	public void onStep(int step);

	// OK
	public void onFinish();

	// execute Error， 一般错误
	public void onError(int errorCode, String errorMsg);

	// execute fault, 致命错误
	public void onFault(String faultMsg);
}
