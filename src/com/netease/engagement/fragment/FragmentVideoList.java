package com.netease.engagement.fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.VIDEO_SIZE_MY_SHOW;
import com.netease.engagement.dataMgr.VideoInfo;
import com.netease.engagement.fragment.FragmentSendVideo.OnSendVideoListener;
import com.netease.engagement.image.video.AsyncImageLoader;
import com.netease.engagement.image.video.ImageLoadCallBack;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;

/**
 * 聊天界面视频列表
 */
public class FragmentVideoList extends FragmentBase implements LoaderCallbacks<Cursor>{
	
	public static FragmentVideoList newInstance(){
		FragmentVideoList fragment = new FragmentVideoList();
		return fragment ;
	}
	
	private static Pattern RESOLUTION = Pattern.compile("(\\d+)x(\\d+)");
	
	private ImageView mTitleBack ;
	private TextView mTitleMiddle ;
	private TextView mTitleRight ;
	
	private GridView mGridView ;
	
	private VideoListAdapter mAdapter ;
	
	private static final int COLUM_NUM = 4 ;
	private int mItemWidth ;
	
	private static final String TAG = FragmentVideoList.class.getSimpleName();
	private int mLoadId = TAG.hashCode() ;
	
	//标记是否有文件被选中
	private boolean tag = false ;
	//标记被选中文件的位置
	private int mPos = -1 ;
	private int mFromType = EgmConstants.SELEC_VIDEO_TYPE.TYPE_CHAT;
	
	/**
	 * 视频缩略图相关信息
	 */
	private static String[] thumbColumns = new String[]{
		MediaStore.Video.Thumbnails.DATA,
		MediaStore.Video.Thumbnails.VIDEO_ID
	};
	
	/**
	 * 视频相关信息
	 */
	private static String[] mediaColumns = new String[]{
		MediaStore.Video.Media._ID,
		MediaStore.Video.Media.DATA,
		MediaStore.Video.Media.TITLE,
		MediaStore.Video.Media.DURATION,
		MediaStore.Video.Media.SIZE,
		MediaStore.Video.Media.RESOLUTION
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent =  getActivity().getIntent();
		if(intent != null){
			mFromType = intent.getIntExtra(EgmConstants.BUNDLE_KEY.SELECT_VIDEO_TYPE, EgmConstants.SELEC_VIDEO_TYPE.TYPE_CHAT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_video_list,container,false);
		init(root);
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getLoaderManager().restartLoader(mLoadId,null,this);
	}

	private void init(View root){
		mTitleBack = (ImageView)root.findViewById(R.id.back);
		mTitleBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		mTitleMiddle = (TextView)root.findViewById(R.id.title_middle);
		mTitleMiddle.setText(R.string.str_video);
		mTitleRight = (TextView)root.findViewById(R.id.title_right);
		if(mFromType == EgmConstants.SELEC_VIDEO_TYPE.TYPE_CHAT){
			mTitleRight.setText(R.string.send_txt);
		} else{
			mTitleRight.setText(R.string.confirm);
		}
		mTitleRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//发送视频
				if(mPos != -1){
					VideoInfo info = (VideoInfo) mAdapter.getItem(mPos);
					if (!checkVideoLegal(info)) {
						return;
					}
					
					Intent intent = new Intent();
					intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH,info.filePath);
					intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_DURATION,info.duration);
					getActivity().setResult(Activity.RESULT_OK,intent);
					getActivity().finish();
					return ;
				}else{
					ToastUtil.showToast(getActivity(),"请选择要上传的视频");
				}
			}
		});
		
		mItemWidth = getActivity().getResources().getDisplayMetrics().widthPixels / COLUM_NUM ;
		
		mGridView = (GridView)root.findViewById(R.id.gridview);
		mGridView.setPadding(
				EgmUtil.dip2px(getActivity(),2),
				EgmUtil.dip2px(getActivity(),6), 
				EgmUtil.dip2px(getActivity(),2), 
				EgmUtil.dip2px(getActivity(),6));
		
		mGridView.setNumColumns(COLUM_NUM);
		mGridView.setColumnWidth(mItemWidth);
		mGridView.setOnItemClickListener(mOnItemClickListener);
		
		mAdapter = new VideoListAdapter(getActivity(),null);
		
		mGridView.setAdapter(mAdapter);
	}
	
	private boolean checkVideoLegal(VideoInfo info) {
		if(mFromType == EgmConstants.SELEC_VIDEO_TYPE.TYPE_CHAT){
			if(info.getSize() > EgmConstants.Video_Size.MAX_SIZE){
				ToastUtil.showToast(getActivity(), R.string.send_video_size_limit);
				return false;
			}
			if(info.getDuration() > EgmConstants.Video_Size.MAX_DURATION){
				ToastUtil.showToast(getActivity(),R.string.send_video_time_limit);
				return false;
			}
			else {
				return true;
			}
		} 
		
		if(info.getDuration() > EgmConstants.VIDEO_SIZE_MY_SHOW.MAX_DURATION){
			ToastUtil.showToast(getActivity(),R.string.video_time_max);
			return false;
		}
		if(info.getDuration() < EgmConstants.VIDEO_SIZE_MY_SHOW.MIN_DURATION){
			ToastUtil.showToast(getActivity(),R.string.video_time_min);
			return false;
		}
		if(info.getSize() < EgmConstants.VIDEO_SIZE_MY_SHOW.MIN_SIZE){
			ToastUtil.showToast(getActivity(), R.string.video_size_min);
			return false;
		}
		if(info.getSize() > EgmConstants.VIDEO_SIZE_MY_SHOW.MAX_SIZE){
			ToastUtil.showToast(getActivity(), R.string.video_size_max);
			return false;
		}
		
		String res = info.getResolution();
		if (TextUtils.isEmpty(res)) {
			ToastUtil.showToast(getActivity(), R.string.video_size_min);
			return true;
		}
		
		Matcher matcher = RESOLUTION.matcher(res);
		if (matcher.find()) {
			try {
				String width = matcher.group(1);
				String height = matcher.group(2);
				
				int min = VIDEO_SIZE_MY_SHOW.MIN_RESOLUTION;
				if (Integer.parseInt(width) < min
						|| Integer.parseInt(height) < min) {
					ToastUtil.showToast(getActivity(), R.string.video_size_min);
					return false;
				}
			} catch (Exception e) {
			}
		}
		
		return true;
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//进入到单个视频界面
			VideoInfo info = (VideoInfo) mAdapter.getItem(position);
			if(info != null){
				FragmentSendVideo fragment = FragmentSendVideo.newInstance(info);
				fragment.setOnSendVideoListener(new OnSendVideoListener(){
					@Override
					public void onSendVideo(VideoInfo info) {
						//回调
						if (! checkVideoLegal(info)) {
							return ;
						}
						Intent intent = new Intent();
						intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH,info.filePath);
						intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_DURATION,info.duration);
						getActivity().setResult(Activity.RESULT_OK,intent);
						getActivity().finish();
						return ;
					}
				});
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				ft.addToBackStack(null);
				ft.replace(R.id.activity_chat_video_list_container_id, fragment).commit();  // bug fix #140923  by gzlichangjie
			}
		}
	};
	
	class VideoListAdapter extends CursorAdapter{

		private GridView.LayoutParams lp ;
		private LayoutInflater inflater ;
		
		public VideoListAdapter(Context context, Cursor c) {
			super(context, c,false);
			lp = new GridView.LayoutParams(mItemWidth,mItemWidth);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View convertView, Context arg1, Cursor cursor) {
			final ViewHolder holder = (ViewHolder) convertView.getTag();
			//获取文件路径
			String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
			String videoId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
			
			Bitmap thumb = AsyncImageLoader.getInstance().loadBitmap(filePath,videoId,new ImageLoadCallBack(){
				@Override
				public void onImageLoaded(Bitmap bitmap) {
					holder.thumb.setImageBitmap(bitmap);
				}
			});
			if(thumb != null){
				holder.thumb.setImageBitmap(thumb);
			}else{
				//先填充一张默认的视频缩略图
				ViewCompat.setBackground(holder.thumb,getActivity().getResources().getDrawable(R.drawable.bg_photo_placeholder));
			}
			
			// bug fix #141117
			holder.unSelected.setTag(Integer.valueOf(cursor.getPosition()));
			if(mPos == Integer.valueOf(cursor.getPosition())) {
				holder.unSelected.setVisibility(View.GONE);
				holder.selected.setVisibility(View.VISIBLE);
			} else {
				holder.unSelected.setVisibility(View.VISIBLE);
				holder.selected.setVisibility(View.GONE);
			}
			
			long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
			holder.duration.setText(getDuration(duration));
		}

		@Override
		public View newView(Context context, final Cursor arg1, ViewGroup arg2) {
			View view = inflater.inflate(R.layout.item_view_video_list,null);
			view.setLayoutParams(lp);
			
			ViewHolder holder = new ViewHolder();
			holder.thumb = (ImageView) view.findViewById(R.id.thumb);
			holder.thumb.setScaleType(ScaleType.CENTER_CROP);
			holder.unSelected = (ImageView)view.findViewById(R.id.unselected);
			holder.selected = (ImageView)view.findViewById(R.id.selected);
			holder.duration = (TextView)view.findViewById(R.id.duration);
			
			final ImageView unSelected = holder.unSelected ;
			final ImageView selected = holder.selected ;
			
			unSelected.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					//判断是否已经有文件被选中
					if(tag){
						ToastUtil.showToast(getActivity(),R.string.send_one_video_only);
						return ;
					}
					tag = true ;
					mPos = (Integer) unSelected.getTag();
					unSelected.setVisibility(View.GONE);
					selected.setVisibility(View.VISIBLE);
				}
			});
			selected.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					tag = false ;
					mPos = -1 ;
					unSelected.setVisibility(View.VISIBLE);
					selected.setVisibility(View.GONE);
				}
			});
			
			view.setTag(holder);
			return view;
		}
		
		@Override
		public VideoInfo getItem(int position) {
			VideoInfo info = new VideoInfo();
			Cursor cursor = getCursor();
			if(cursor.moveToPosition(position)){
				info.filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
				info.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
				info.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
				info.resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
			}
			return info ;
		}
		
		/**
		 * 获取缩略图路径
		 * @param videoId
		 * @return
		 */
		private String getThumbPath(int videoId){
			String thumbPath = null ;
			Cursor thumbCursor = getActivity().getContentResolver().query(
					MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, 
					thumbColumns, 
					MediaStore.Video.Thumbnails.VIDEO_ID + "=" + videoId, 
					null, 
					null);
			
			if (thumbCursor.moveToFirst()){
				thumbPath = thumbCursor.getString(
						thumbCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
			}
			return thumbPath ;
		}
		
		/**
		 * 获取视频文件缩略图
		 * @param filePath
		 * @return
		 */
		private Bitmap getVideoThumb(String filePath){
			Bitmap thumb = null ;
			if(!TextUtils.isEmpty(filePath)){
				thumb = ThumbnailUtils.createVideoThumbnail(filePath,
						MediaStore.Video.Thumbnails.MINI_KIND);
			}
			return thumb ;
		}
		
		/**
		 * 格式化显示时间
		 * @param duration
		 * @return
		 * xx:xx:xx
		 */
		private String getDuration(long duration){
			StringBuilder sb = new StringBuilder();
			int hours = 0 ;
			int minutes = 0 ;
			int seconds = 0 ;
			
			seconds = (int)(duration / 1000) ;
			hours = seconds/(60 * 60);
			//hour不一定显示
			if(hours > 0){
				sb.append(hours > 9 ? String.valueOf(hours): "0"+hours).append(":");
			}
			seconds = seconds - hours * 60 * 60 ;
			minutes = seconds/60 ;
			//分钟一定要显示
			sb.append(minutes > 9 ? String.valueOf(minutes):"0"+minutes).append(":");
			seconds = seconds - minutes * 60 ;
			sb.append(seconds > 9 ? seconds : "0"+seconds);
			return sb.toString();
		}
		
		class ViewHolder{
			ImageView thumb ;
			ImageView unSelected ;
			ImageView selected ;
			TextView duration ;
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
				getActivity(),
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				mediaColumns,
				null,
				null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AsyncImageLoader.getInstance().clear();
	}
}
