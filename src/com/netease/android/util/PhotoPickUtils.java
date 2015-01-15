package com.netease.android.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class PhotoPickUtils {

	private static Bitmap innerScaledBitmap(Uri path, Context mContext,
			int wPix, int hPix) throws Exception {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inInputShareable = true;
		options.inPurgeable = true;
		InputStream is = null;
		try {
			is = mContext.getContentResolver().openInputStream(path);
			BitmapFactory.decodeStream(is, null, options);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		double ratio = Math.max((double) options.outWidth / wPix,
				(double) options.outHeight / hPix);
		if (ratio > 1) {
			options.inSampleSize = (int) ratio;
		} else {
			ratio = 1d;
		}
		options.inSampleSize = (int) ratio;
		options.inJustDecodeBounds = false;
		// options.inPreferredConfig = Bitmap.Config.RGB_565;
		try {
			is = mContext.getContentResolver().openInputStream(path);
			bitmap = BitmapFactory.decodeStream(is, null, options);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		if (bitmap == null) {// 用原始的方式再试一遍
			try {
				is = mContext.getContentResolver().openInputStream(path);
				bitmap = BitmapFactory.decodeStream(is);
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		return bitmap;
	}

	/**
	 * 按照比例计算图片最大长度
	 * @param path
	 * @param wPix
	 * @param hPix
	 * @return
	 * @throws Exception
	 */
	public static int getMaxLength(Context context,Uri path, long px) throws Exception {
		int maxlength = 0;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		InputStream is = null;
		try {
			is = context.getContentResolver().openInputStream(path);
			BitmapFactory.decodeStream(is, null, options);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		double width = options.outWidth;
		double height = options.outHeight;
		double ratio = height > width ? height / width : width / height;
//			double maxlength = Math.sqrt(px / ratio);
		maxlength = (int)(Math.sqrt(px / ratio) * ratio);
		return maxlength;
	}
	
//	/**
//	 * 从文件解析出Bitmap格式的图片
//	 * 
//	 * @param path
//	 * @param maxWidth
//	 *            最大宽像素
//	 * @param maxHeight
//	 *            最大高像素
//	 * @return
//	 * @throws FileNotFoundException
//	 */
//	public static InputStream[] decodeFile(Uri path, final Context mContext,
//			int maxWidth, int maxHeight, final Map<String, String> postParams)
//			throws Exception {
//		// BufferedOutputStream bos = null;
//		int orientationAngle = 0;// 图片原始角度
//		int rotateAngle = Integer.parseInt(postParams.get("rotateAngle"));// 旋转角度
//		int filterNo = Integer.parseInt(postParams.get("filterNo"));// 选中的滤镜序列号
//		boolean isHdr = Boolean.parseBoolean(postParams.get("isHdr"));// 是否选中对比度
//		boolean repost = Boolean.parseBoolean(postParams.get("repost"));// 失败时重发标志，只要控制不另外生成滤镜图片了
//		int dofType = Integer.parseInt(postParams.get("dofType"));
//		float posX = Float.parseFloat(postParams.get("posX"));
//		float posY = Float.parseFloat(postParams.get("posY"));
//		float size = Float.parseFloat(postParams.get("size"));
//		float degree = Float.parseFloat(postParams.get("degree"));
//		final String queueId = postParams.remove("queueId");
//		System.gc();
//		ByteArrayOutputStream baos = null;
//		ByteArrayInputStream bais = null;
//		Bitmap bitmap = null;
//		InputStream[] result = new InputStream[2];
//		try {
//			Cursor cursor = mContext.getContentResolver().query(path, null,
//					null, null, null);// 根据Uri从数据库中找
//			if (cursor != null) {// 把游标移动到首位，因为这里的Uri是包含ID的所以是唯一的不需要循环找指向第一个就是了
//				cursor.moveToFirst();
//				String orientation = cursor.getString(cursor
//						.getColumnIndex("orientation"));// 获取旋转的角度
//				if (orientation != null && !"".equals(orientation)) {
//					orientationAngle = Integer.parseInt(orientation);
//					rotateAngle -= orientationAngle;
//				}
//			}
//			InputStream is = mContext.getContentResolver()
//					.openInputStream(path);
//			bitmap = scaledBitmap(path, mContext, maxWidth, maxHeight);
//			Bitmap bitmap1 = applyFilter(bitmap, mContext, filterNo, rotateAngle,
//					isHdr, dofType, posX, posY, size, degree, true);
//			boolean hasFilter = false;
//			if(bitmap1 != bitmap){
//				hasFilter = true;
//			}
//			bitmap = bitmap1;
//			Log.v("PhotoPickUtils", "decodeFile sucess:" + bitmap);
//			boolean hasWaterMark = false;
//			if (postParams.containsKey("waterMarkUrl")) {// 生成贴纸水印
//				hasWaterMark = true;
//				float[] matrixValues = new Gson().fromJson(
//						postParams.get("matrixValues"), float[].class);
//				bitmap = applyWaterMark(postParams.get("waterMarkUrl"), bitmap,
//						matrixValues,
//						Float.parseFloat(postParams.get("stickerViewHeight")),
//						Float.parseFloat(postParams.get("stickerViewWidth")),
//						Float.parseFloat(postParams.get("sampleViewHeight")),
//						Float.parseFloat(postParams.get("sampleViewWidth")),orientationAngle,hasFilter);
//			}
//			baos = new ByteArrayOutputStream();
//			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)) {
//				// bos = new BufferedOutputStream(fos);
//				bais = new ByteArrayInputStream(baos.toByteArray());
//				result[0] = bais;
//				result[1] = is;
//				// bos.write(baos.toByteArray());
//			}
//			final String fromExplorer = postParams.remove(FROM_EXPLORE);
//			if (!repost
//					&& (filterNo != 0 || rotateAngle != 0 || isHdr || dofType != -1 || hasWaterMark)) {
//				byte[] buf = baos.toByteArray();
//				if (orientationAngle != 0) {// 真正旋转过的图片（魅族等机器拍摄时，默认会旋转90度）
//					ByteArrayOutputStream baoss = null;
//					Bitmap savedBit = null;
//					try {
//						Image uploadImg = new Image(bitmap);
//						uploadImg.rotate(orientationAngle);
//						savedBit = uploadImg.getImage();
//						if (savedBit != bitmap && savedBit != null) {
//							baoss = new ByteArrayOutputStream();
//							if (savedBit.compress(Bitmap.CompressFormat.JPEG,
//									100, baoss)) {
//								buf = baoss.toByteArray();
//							}
//						}
//					} catch (Throwable t) {
//						t.printStackTrace();
//						System.gc();
//					} finally {
//						if (savedBit != null) {
//							savedBit.recycle();
//						}
//						if (baoss != null) {
//							try {
//								baoss.close();
//							} catch (IOException e) {
//							}
//						}
//					}
//				}
//				final ByteArrayInputStream baiss = new ByteArrayInputStream(buf);
//				final boolean hasWaterMarkFinal  = hasWaterMark;
//				new Thread() {
//					public void run() {
//						File file = new File(
//								getCustomDirectory(APP_PIC_DIR)
//										+ "filter"
//										+ new SimpleDateFormat("yyyyMMddHHmm")
//												.format(new Date()) + ".jpg");
//						try {
//							FileOutputStream fos = new FileOutputStream(file);
//							int bytesRead = 0;
//							byte[] buffer = new byte[8192];
//							while ((bytesRead = baiss.read(buffer, 0, 8192)) != -1) {
//								fos.write(buffer, 0, bytesRead);
//							}
//							fos.flush();
//							fos.close();
//							// 在指定的目录下扫描生成缩略图
//							if (hasMnt()) {
//								file = new File("/mnt" + file.getPath());
//							}
//							mContext.sendBroadcast(new Intent(
//									Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
//											.fromFile(file)));
//							if(hasWaterMarkFinal && !postParams.get("allowView").equals("100")){//非私有的贴纸文章
//								Intent intent = new Intent(PostInfo.WATER_MAKR_SHARE_INTENT);
//								intent.putExtra("filePath", file.getPath());
//								intent.putExtra("watermarkname", postParams.get("watermarkname"));
//								intent.putExtra("queueId", queueId);
//								if (fromExplorer != null) {
//									intent.putExtra(FROM_EXPLORE, true);
////									while (LofterApplication.getInstance()
////											.getTopActivity() instanceof PostActivity) {// LOFTER-15630，有些机型上,发布页面destroy的比较慢
////										sleep(10L);
////									}
//								}
//								if(ActivityUtils.isNetworkAvailable(mContext)){//有网络才有分享弹窗
//									mContext.sendBroadcast(intent);
//								}
//							}
//							// MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
//							// file.getPath(), file.getName(), "Lofter filter");
//						} catch (Exception e) {
//						}
//					}
//				}.start();
//			}
//		} catch (OutOfMemoryError err) {
//			err.printStackTrace();
//			System.gc();
//			System.gc();
//		} catch (Exception err1) {
//			err1.printStackTrace();
//			System.gc();
//			System.gc();
//		} finally {
//			if (bitmap != null) {
//				bitmap.recycle();
//			}
//			if (baos != null) {
//				try {
//					baos.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//		return result;
//	}

//	private static Bitmap applyFilter(Bitmap bitmap, Context mContext,
//			int filterNo, int rotateAngle, boolean isHdr, int dofType,
//			float posX, float posY, float size, float degree, boolean track) {
//		PhotoPickUtils picker = new PhotoPickUtils(mContext);
//
//		List<IImageFilter> filters = new ArrayList<IImageFilter>();
//		filters.add(picker.filterArray.get(filterNo));
//		if (dofType == 0) {
//			filters.add(new CvRadialDOF(posX, posY, size));
//		} else if (dofType == 1) {
//			filters.add(new CvLinearDOF(posX, posY, degree, size));
//		}
//		Image img = new Image(bitmap, isHdr);
//		Bitmap result = null;
//		for (IImageFilter filter : filters) {
//			img = filter.process(img);
//			img.copyPixelsFromBuffer();
//			result = img.getImage();
//			if (img.image != null && img.image != img.destImage
//					&& !img.image.isRecycled()) {
//				img.image.recycle();
//				img.image = null;
//				System.gc();
//				System.gc();
//			}
//			img = new Image(result, isHdr);
//		}
//		if (rotateAngle != 0) {// 需要旋转
//			img.rotate(rotateAngle);
//			result = img.getImage();
//			if (img.image != null && !img.image.isRecycled()) {
//				img.image.recycle();
//				img.image = null;
//				System.gc();
//			}
//		}
//		return result;
//	}

	/**
	 * @param path
	 * @param mContext
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 * @throws Exception
	 */
	public static Bitmap scaledBitmap(Uri path, Context mContext, int maxWidth,
			int maxHeight) throws Exception {
		Bitmap image = innerScaledBitmap(path, mContext, maxWidth, maxHeight);
		Bitmap result = scaledBitmap(image, maxWidth, maxHeight);
		if (result != null && result != image) {
			image.recycle();
			System.gc();
			Log.v("PhotoPickUtils", "recyle");
		}
		return result;
	}

	/**
	 * 缩放,中间部分,为了保证图片清晰，先按照比例截取较大的图，然后截取中间部分
	 * 
	 * @param path
	 * @param mContext
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public static Bitmap scaledCenterBitmap(Uri path, Context mContext,
			int maxWidth, int maxHeight) {
		// Bitmap bitmap = null;
		// try {
		// bitmap = innerScaledBitmap(path, mContext, maxWidth, maxHeight,true);
		// if(bitmap != null){
		// int orignWidth = bitmap.getWidth();
		// int orignHeight = bitmap.getHeight();
		// int width = orignWidth;
		// int height = orignHeight;
		// int x = 0;
		// int y = 0;
		//
		// if(height > maxHeight){
		// height = maxHeight;
		// }
		// if(width > maxWidth){
		// width = maxWidth;
		// }
		//
		// if(orignWidth > orignHeight){
		// x = (orignWidth - width) / 2;
		// }else{
		// y = (orignHeight - height) / 2;
		// }
		// bitmap = bitmap.createBitmap(bitmap, x, y, width, height);
		// // if(width > maxWidth){
		// // bitmap = bitmap.createScaledBitmap(bitmap, Math.min(maxHeight,
		// maxWidth), Math.min(maxHeight, maxWidth), true);
		// // }
		// }
		// } catch (Exception e) {
		// Log.e("PhotoPickUtils", "scaledSquareCenterBitmap error");
		// }

		Bitmap bitmap = null;
		InputStream is = null;
		try {
			is = mContext.getContentResolver().openInputStream(path);
			bitmap = BitmapFactory.decodeStream(is);
			// bitmap = BitmapFactory.decodeStream(is, null, options);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		if (bitmap != null) {
			bitmap = scaleCrop(bitmap, maxHeight, maxWidth, true);
		}
		return bitmap;
	}

	public static Bitmap scaleCrop(Bitmap source, int newHeight, int newWidth,
			boolean center) {
		if (source == null) {
			return source;
		}
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();

		// Compute the scaling factors to fit the new height and width,
		// respectively.
		// To cover the final image, the final scaling will be the bigger
		// of these two.
		float xScale = (float) newWidth / sourceWidth;
		float yScale = (float) newHeight / sourceHeight;
		float scale = Math.max(xScale, yScale);

		// Now get the size of the source bitmap when scaled
		float scaledWidth = scale * sourceWidth;
		float scaledHeight = scale * sourceHeight;

		// Let's find out the upper left coordinates if the scaled bitmap
		// should be centered in the new size give by the parameters
		float left = (newWidth - scaledWidth) / 2;
		float top = 0;
		if (center) {
			top = (newHeight - scaledHeight) / 2;
		}

		// The target rectangle for the new, scaled version of the source bitmap
		// will now
		// be
		RectF targetRect = new RectF(left, top, left + scaledWidth, top
				+ scaledHeight);

		// Finally, we create a new bitmap of the specified size and draw our
		// new,
		// scaled bitmap onto it.
		Bitmap dest = Bitmap.createBitmap(newWidth, newHeight,
				source.getConfig());
		Canvas canvas = new Canvas(dest);
		canvas.drawBitmap(source, null, targetRect, null);

		return dest;
	}

	/**
	 * 图片添加边框
	 * 
	 * @param bitmap
	 * @param right
	 * @param bottom
	 * @param borderWidth
	 * @param color
	 */
	public static void drawBorder(Bitmap bitmap, float borderWidth, int color) {
		if (bitmap == null) {
			return;
		}
		Canvas canvas = new Canvas(bitmap);
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(borderWidth);
		// paint.setColor(Color.RED);
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(rect, paint);
	}

	public static Bitmap scaledBitmap(Bitmap image, int maxWidth, int maxHeight) {
		if (image == null) {
			return null;
		}
		int maxPix = Math.max(image.getWidth(), image.getHeight());
		int defPix = Math.max(maxWidth, maxHeight);
		if (maxPix > defPix) {// 等比压缩完的像素还未符合要求
			float rationW = (float) image.getWidth() / maxWidth;
			float rationH = (float) image.getHeight() / maxHeight;
			if (rationW > rationH) {
				image = Bitmap.createScaledBitmap(image, maxWidth,
						(int) (image.getHeight() / rationW), true);
			} else {
				image = Bitmap.createScaledBitmap(image,
						(int) (image.getWidth() / rationH), maxHeight, true);
			}
		}
		Log.v("PhotoPickUtils", "compressed width:" + image.getWidth()
				+ ",height:" + image.getHeight());
		return image;
	}

	/** 获得目录,如果不存在，则创建 **/
	public static String getCustomDirectory(String custDir) {
		String dir = getSDPath() + custDir;
		String substr = dir.substring(0, 4);
		if (substr.equals("/mnt")) {
			dir = dir.replace("/mnt", "");
		}
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		return dir;
	}
	
	/**** 取SD卡路径不带/ ****/
	private static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		if (sdDir != null) {
			return sdDir.toString();
		} else {
			return "";
		}
	}
	
	public static boolean hasMnt(){
		String mnt = getSDPath().substring(0, 4);
		if (mnt.equals("/mnt")) {
			return true;
		}
		return false;
	}
	
	public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
		return ThumbnailUtils.extractThumbnail(source, width, height);
	}

	public static Bitmap extractThumbnail(String filePath, int width, int height) {
		if (filePath.startsWith("file://")) {
			filePath = Uri.parse(filePath).getPath();
		}
		Bitmap source = BitmapFactory.decodeFile(filePath);
		return extractThumbnail(source, width, height);
	}

	/**
	 * 保存图片
	 * 
	 * @param bm
	 * @param fileName
	 */
	public static void savePhoto(Bitmap bm, String folder, String fileName) {
		if (bm == null) {
			return;
		}
		File dirFile = new File(folder);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File photo = new File(folder + File.separator + fileName);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(photo));
			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.flush();
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

    /**
     * Method to rotate an image by the specified number of degrees
     * 
     * @param rotateDegrees
     */
    public static Bitmap rotateBitmap (Bitmap bitmap,int rotateDegrees){
        Matrix mtx = new Matrix();
        mtx.postRotate(rotateDegrees);
        Bitmap destImage = bitmap.copy(bitmap.getConfig(), true);
        int width = bitmap.getWidth();
    	int height = bitmap.getHeight();
        destImage = Bitmap.createBitmap(destImage, 0, 0, width, height, mtx, true);
        bitmap.recycle();
        return destImage;
    }
	
	/**
	 * 合成贴图
	 * 
	 * @param originalBitmap
	 * @param stickerBitmap
	 * @param matrix
	 * @param dstRect
	 * @param stickerViewHeight
	 * @param stickerViewWidth
	 * @param targetHeight
	 * @param targetWidth
	 * @return
	 */
	public static Bitmap handleSticker(Bitmap originalBitmap,
			Bitmap stickerBitmap, Matrix matrix, float stickerViewHeight,
			float stickerViewWidth, float targetHeight, float targetWidth,boolean hasFilter) {
		System.gc();
		if (originalBitmap == null) {
			return null;
		}
		if (matrix == null || stickerBitmap == null) {
			return originalBitmap;
		}

		Matrix scaleMatrix = new Matrix(matrix);
		float scale = originalBitmap.getHeight() / targetHeight;

//		Bitmap scaleStickerBitmap = Bitmap.createBitmap(
//				(int) (stickerViewWidth * scale),
//				(int) (stickerViewHeight * scale), Config.ARGB_8888);
//		Bitmap resultBitmap = originalBitmap.copy(originalBitmap.getConfig(),true);
		Bitmap resultBitmap = originalBitmap;
		
		// 如果没有做滤镜，则复制一份，因为直接修改sd卡中图片会报错
		if(!hasFilter || (android.os.Build.VERSION.SDK_INT > 10)){
			resultBitmap = originalBitmap.copy(originalBitmap.getConfig(),true);
		}
//		Canvas canvas = new Canvas(scaleStickerBitmap);
		Canvas canvas = new Canvas(resultBitmap);
		// 抗锯齿
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
		// 针对原图，计算缩放
		scaleMatrix.postScale(scale, scale);
		// 水印在原图的位置
		scaleMatrix.postTranslate(-(stickerViewWidth - targetWidth) * scale / 2, -(stickerViewHeight - targetHeight) * scale / 2);
		canvas.drawBitmap(stickerBitmap, scaleMatrix, null);

		// save all clip
		canvas.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		canvas.restore();// 存储
//		Bitmap resultBitmap = originalBitmap.copy(originalBitmap.getConfig(),
//				true);
//		Canvas resultCanvas = new Canvas(resultBitmap);
//
//		resultCanvas.drawBitmap(scaleStickerBitmap,
//				-(stickerViewWidth - targetWidth) * scale / 2,
//				-(stickerViewHeight - targetHeight) * scale / 2, null);
//		// save all clip
//		resultCanvas.save(Canvas.ALL_SAVE_FLAG);// 保存
//		// store
//		resultCanvas.restore();// 存储
		
//		if(originalBitmap != null){
//			originalBitmap.recycle();
//		}
//		if(stickerBitmap != null){
//			stickerBitmap.recycle();
//		}
//		if(scaleStickerBitmap != null){
//			scaleStickerBitmap.recycle();
//		}
		return resultBitmap;
	}
}
