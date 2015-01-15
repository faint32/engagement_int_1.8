package com.netease.engagement.image.explorer.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.image.explorer.ExplorerPhotoType;
import com.netease.engagement.image.explorer.utils.BitmapUtil;
import com.netease.engagement.image.explorer.utils.ILoadBitmapCallback;

public class FileExplorerListAdapter extends BaseAdapter implements ILoadBitmapCallback{
	
	private LayoutInflater	inflater;
	private List<FileExplorerBean> list;
	
	private Bitmap placeHolder;
	public FileExplorerListAdapter(Context context, List<FileExplorerBean> list) {
		inflater = LayoutInflater.from(context);
		this.list = list;
		
		placeHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_photo_placeholder_96x96);
	}
	
	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.file_explorer_item, null);
			viewHolder = new ViewHolder();
			viewHolder.rootRl = (RelativeLayout) convertView.findViewById(R.id.rootRl);
			viewHolder.thumbIv = (ImageView) convertView.findViewById(R.id.thumbIv);
			viewHolder.displayNameTv = (TextView) convertView.findViewById(R.id.displayNameTv);
			viewHolder.picCountTv = (TextView) convertView.findViewById(R.id.picCountTv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		FileExplorerBean bean = list.get(position);
		
		if(position % 2 != 0) {
			viewHolder.rootRl.setBackgroundColor(Color.parseColor("#f8f8f8"));
		} else {
			viewHolder.rootRl.setBackgroundColor(Color.parseColor("#ffffff"));
		}
		
		viewHolder.displayNameTv.setText(bean.getDisplayName());
		viewHolder.picCountTv.setText("( " + bean.getPicCount() + " )");
		
		long origId = bean.getOrigID();
		String path = bean.getData();
		int max = placeHolder.getWidth()*2;
		
		if(!(path.equalsIgnoreCase(viewHolder.thumbIv.getContentDescription().toString()))) {
        	viewHolder.thumbIv.setImageBitmap(placeHolder);
        	BitmapUtil.getInstance().loadBitmap(origId, path, viewHolder.thumbIv, max, ExplorerPhotoType.FILELIST, this);
		}
		return convertView;
	}
	
	static class ViewHolder {
		private RelativeLayout rootRl;
		private ImageView thumbIv;
		private TextView displayNameTv;
		private TextView picCountTv;
	}

	@Override
	public void dealBitmap(ImageView imageView, Bitmap bitmap) {
		if(bitmap != null) {
			imageView.setImageBitmap(bitmap);
//			Bitmap cropBitmap = BitmapUtil.getInstance().cropBitmapFix(bitmap, placeHolder.getWidth(), placeHolder.getHeight());
//			if(cropBitmap != null) {
//				imageView.setImageBitmap(cropBitmap);
//			}
		}
	}

}
