package com.netease.framework.skin;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

public interface IResources {
	/**
	 * 通过资源ID获取Drawable
	 * @param id
	 * @return
	 * @throws Resources.NotFoundException
	 */
	public Drawable getDrawable(int id) throws Resources.NotFoundException;

	/**
	 * 通过资源ID获取颜色值
	 * @param id
	 * @return
	 * @throws Resources.NotFoundException
	 */
	public int getColor(int id) throws Resources.NotFoundException;

	/**
	 * 通过资源ID获取ColorStateList
	 * @param id
	 * @return
	 * @throws Resources.NotFoundException
	 */
	public ColorStateList getColorStateList(int id) throws Resources.NotFoundException;

	/**
	 * 获取尺寸大小
	 * @param id
	 * @return
	 * @throws Resources.NotFoundException
	 */
	public float getDimension(int id) throws Resources.NotFoundException;
	
	public boolean getBoolean(int id) throws NotFoundException;
	
	public int getInteger(int id) throws NotFoundException;
	
	/**
	 * 通过资源ID获取转换后的id值
	 * @param id
	 * @return
	 */
	public int getIdentifier(int id);
}
