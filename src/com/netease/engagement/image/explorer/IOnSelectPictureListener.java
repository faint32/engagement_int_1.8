package com.netease.engagement.image.explorer;

public interface IOnSelectPictureListener {
	public void onNonePictureSelected();
	
	/**
	 * 图片被选中
	 * @param pos
	 * @return true:完成选中后的操作，确认选中；false：取消选中
	 */
	public boolean onPictureSelected(int pos);
	
	public void onPictureDisseletced(int pos);
	
	public void onOverMaxPictureSelected();
	
	public void onSlidePictureSelected(int pos);
}
