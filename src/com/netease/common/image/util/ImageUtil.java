package com.netease.common.image.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.backport.webp.WebPFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES10;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.netease.common.cache.file.StoreFile;
import com.netease.common.debug.CheckAssert;
import com.netease.common.image.ImageManager;
import com.netease.common.service.BaseService;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.Utils.EgmUtil;
import com.netease.util.DisplayUtil;

public class ImageUtil {

	private static final boolean DEBUG = false;
	private static final String TAG = "ImageUtil";
	
	/*********************************************************
	 * 
	 * ImageUtil 提供了保存图片、旋转图片等方法
	 * 
	 * 1、保存图片 saveBitmap2File
	 * 2、获取最大可显示图片 getBitmapFromFileLimitSize
	 * 3、
	 * 
	 ********************************************************/
	
	/**
	 * 
	 * 
	 * @param src source path
	 * @param dest destination path
	 * @param limit 
	 * @param quality
	 * @return
	 */
	public static boolean saveResizeTmpFile(String src, String dest, 
			int limit, int quality) {
		boolean ret = false;
		
		BitmapFactory.Options options = new BitmapFactory.Options();

		int dstWidth = -1;
		int dstHeight = -1;
		
		try {
			if (limit > 0) {
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(src, options);
				
				if (options.outWidth > limit || options.outHeight > limit) {
					if (options.outWidth > options.outHeight) {
						dstWidth = limit;
						dstHeight = options.outHeight * limit / options.outWidth;
					}
					else {
						dstWidth = options.outWidth * limit / options.outHeight;
						dstHeight = limit;
					}
				}
				
				options.inJustDecodeBounds = false;
			}
			
			if (DEBUG)
				Log.e(TAG, "saveResizeTmpFile: " + "dw: " + dstWidth
						+ " dh: " + dstHeight
						+ " w: " + options.outWidth
						+ " h: " + options.outHeight);
			
			
			Bitmap bitmap = BitmapFactory.decodeFile(src, options);
			if (bitmap != null) {
				if (dstWidth > 0) { // dstHeight > 0
					Bitmap tmp = Bitmap.createScaledBitmap(bitmap, 
							dstWidth, dstHeight, false);
					if (tmp != null) {
						bitmap.recycle();
						
						bitmap = tmp;
					}
				}
				bitmap = rotateBitmapInNeeded(src,bitmap);
				
				ret = saveBitmap(bitmap, dest, quality, true);
			}
		} catch (Exception e) {
		} catch (Error e) {
		}
		
		return ret;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int width, int height) {
		int inSampleSize = 1;
		if (width > 0) {
			if (height > 0) {
				inSampleSize = Math.min(options.outWidth / width,
						options.outHeight / height);
			} else {
				inSampleSize = options.outWidth / width;
			}
		} else {
			if (height > 0) {
				inSampleSize = options.outHeight / height;
			}
		}
		
		return inSampleSize;
	}
	
	
	/**
	 * 获取精确裁剪图片
	 * 
	 * @param source
	 * @param width
	 * @param height
	 * @return
	 */
	private static Bitmap getThumImage(Bitmap source, int width, int height) {
		if (source == null)
			return source;
		
		if (width <= 0 || height <= 0) {
			return source;
		}
		
		if (source.getWidth() == width) {
			if (source.getHeight() == height || height == 0) {
				return source;
			}
		} else if (source.getHeight() == height && width == 0) {
			return source;
		}
		
		// TODO: 对于获取的图片大小期望大小，则进行拉伸裁剪
//		if (source.getWidth() <= width && source.getHeight() <= height) {
//			return source;
//		}

		// create the matrix to scale it
		Matrix matrix = new Matrix();
		
		float tmp_w = ((float) source.getWidth()) / width;
		float tmp_h = ((float) source.getHeight()) / height;
		float tmp = tmp_w < tmp_h ? tmp_w : tmp_h;
		
		int clipWidth = (int) (width * tmp);
		int clipHeight = (int) (height * tmp);
		
		clipWidth = clipWidth > source.getWidth() ? source.getWidth() : clipWidth;
		clipHeight = clipHeight > source.getHeight() ? source.getHeight() : clipHeight;

		matrix.setScale(1 / tmp, 1 / tmp);

		int pading_x = (source.getWidth() - clipWidth) >> 1;
		int pading_y = source.getHeight() / 3 - clipHeight / 2;
		pading_y = pading_y < 0 ? 0 : pading_y;

		Bitmap thumb = null;
		try {
			thumb = Bitmap.createBitmap(source, pading_x, pading_y,
					clipWidth, clipHeight, matrix, true);

			if (thumb != source) {
				source.recycle();
			}
		} catch (java.lang.OutOfMemoryError e) {
			ImageManager.getInstance().clearCache();
			
			try {
				thumb = Bitmap.createBitmap(source, pading_x, pading_y,
						clipWidth, clipHeight, matrix, true);
				
				if (thumb != source) {
					source.recycle();
				}
			} catch (java.lang.OutOfMemoryError e1) {
				if (thumb != null) {
					thumb.recycle();
				}
				
				thumb = null;
				System.gc();
			}
		}
		
		return thumb == null ? source : thumb;
	}
	
	/**
	 * 从资源包中解图
	 * 
	 * @param res
	 * @param resId
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getBitmap(Resources res, int resId, int width, int height) {
		CheckAssert.checkNull(res);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		
		if (width > 0 || height > 0) {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res, resId, options);
			
			options.inSampleSize = calculateInSampleSize(options, width, height);
			
			options.inJustDecodeBounds = false;
		} else {
			options.inJustDecodeBounds = false;
		}
		
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeResource(res, resId, options);
			bitmap = getThumImage(bitmap, width, height);
		} catch (OutOfMemoryError e) {
			ImageManager.getInstance().clearCache();
			
			try {
				bitmap = BitmapFactory.decodeResource(res, resId, options);
				bitmap = getThumImage(bitmap, width, height);
			} catch (java.lang.OutOfMemoryError e1) {
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
		
		return bitmap;
	}
	
	/**
	 * 从流中解图
	 * 
	 * @param in
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getBitmap(InputStream in, int width, int height) {
		Bitmap bitmap = null;
		
		if (in != null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			
			if (width > 0 || height > 0) {
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(in, null, options);
                try {
                    in.reset();
                } catch (IOException e) {
                }
				
				options.inSampleSize = calculateInSampleSize(options,
						width, height);
				
				options.inJustDecodeBounds = false;
			} else {
				options.inJustDecodeBounds = false;
			}
			
			try {
				bitmap = BitmapFactory.decodeStream(in, null, options);
				bitmap = getThumImage(bitmap, width, height);
			} catch (OutOfMemoryError e) {
				ImageManager.getInstance().clearCache();
				
				try {
					bitmap = BitmapFactory.decodeStream(in, null, options);
					bitmap = getThumImage(bitmap, width, height);
				} catch (java.lang.OutOfMemoryError e1) {
					if (bitmap != null) {
						bitmap.recycle();
						bitmap = null;
					}
				}
			}
		}
		return bitmap;
	}
	
	/**
	 * 从文件中解图
	 * 
	 * @param in
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getBitmap(File file, int width, int height) {
        Bitmap bitmap = null;

        if (file != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();

            if (width > 0 || height > 0) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getPath(), options);

                options.inSampleSize = calculateInSampleSize(options, width, height);

                options.inJustDecodeBounds = false;
            } else {
                options.inJustDecodeBounds = false;
            }
            int degree = getRotateDegree(file.getPath());

            try {
                bitmap = BitmapFactory.decodeFile(file.getPath(), options);
                bitmap = getThumImage(bitmap, width, height);
                if (degree > 0) {
                    bitmap = rotateBitmap(bitmap, degree);
                }
            } catch (OutOfMemoryError e) {
                ImageManager.getInstance().clearCache();

                try {
                    options.inSampleSize *=2;
                    bitmap = BitmapFactory.decodeFile(file.getPath(), options);
                    bitmap = getThumImage(bitmap, width, height);
                    if (degree > 0) {
                        bitmap = rotateBitmap(bitmap, degree);
                    }
                } catch (java.lang.OutOfMemoryError e1) {
                    System.gc();
                    try {
                        options.inSampleSize *=2;
                        bitmap = BitmapFactory.decodeFile(file.getPath(), options);
                        bitmap = getThumImage(bitmap, width, height);
                        if (degree > 0) {
                            bitmap = rotateBitmap(bitmap, degree);
                        }
                    } catch (java.lang.OutOfMemoryError e2) {
                        if (bitmap != null) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                }
            }
        }
        
        if (bitmap == null) {
        	try {
				bitmap = WebPFactory.decode(file);
			} catch (Exception e) {
			} catch (Error e) {
			}
        }
		
		return bitmap;
	}
	
	/**
	 * 圆角图标
	 * 
	 * @param source
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap source, float cornerSize) {
		if (cornerSize <= 0) {
			cornerSize = 5.0F;
		}
		
		int width = source.getWidth();
		int height = source.getHeight();
		
		Rect rect = new Rect(0, 0, width, height);
		RectF rectF = new RectF(rect);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xFF424242);

		Bitmap outBmp = null;
		try {
			outBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(outBmp);
			
			canvas.drawARGB(0, 0, 0, 0);
			canvas.drawRoundRect(rectF, cornerSize, cornerSize, paint);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(source, rect, rect, paint);
			
			source.recycle();
		} catch (java.lang.OutOfMemoryError e) {
			ImageManager.getInstance().clearCache();
			
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(0xFF424242);

			try {
				outBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
				Canvas canvas = new Canvas(outBmp);
				canvas.drawARGB(0, 0, 0, 0);
				canvas.drawRoundRect(rectF, cornerSize, cornerSize, paint);
				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
				canvas.drawBitmap(source, rect, rect, paint);
				
				source.recycle();
			} catch (java.lang.OutOfMemoryError e1) {
				if (outBmp != null) {
					outBmp.recycle();
					outBmp = null;
				}
			}
		}

		return outBmp;
	}
	
	/**
	 * 圆形图标
	 * 
	 * @param source
	 * @return
	 */
	public static Bitmap getCircleBitmap(Bitmap source) {
		int size = Math.min(source.getWidth(), source.getHeight());
		int left = source.getWidth() > size ? (source.getWidth() - size) / 2 : 0;
		int top = source.getHeight() > size ? (source.getHeight() - size) / 2 : 0;
		
		Rect rect = new Rect(left, top, size, size);
		RectF rectF = new RectF(0, 0, size, size);
		Paint paint = new Paint();
		
		Bitmap outBmp = null;
		try {
			outBmp = Bitmap.createBitmap(size, size, Config.ARGB_8888);
			Canvas canvas = new Canvas(outBmp);
			paint.setAntiAlias(true);
			canvas.drawCircle(size / 2, size / 2, size / 2, paint);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(source, rect, rectF, paint);
			
			source.recycle();
		} catch (java.lang.OutOfMemoryError e) {
			try {
				outBmp = Bitmap.createBitmap(size, size, Config.ARGB_8888);
				paint = new Paint();
				Canvas canvas = new Canvas(outBmp);
				paint.setAntiAlias(true);
				canvas.drawCircle(size / 2, size / 2, size / 2, paint);
				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
				canvas.drawBitmap(source, rect, rectF, paint);
				
				source.recycle();
			} catch (java.lang.OutOfMemoryError e1) {
				if (outBmp != null) {
					outBmp.recycle();
					outBmp = null;
				}
			}
		}

		return outBmp;
	}
	
	/**
     * 圆形图标,不回收source,保证原图是方形，否则会变形
     * 
     * @param source
     * @return
     */
    public static Bitmap getCircleBitmapNoRecycleSource(Bitmap source,int circlesize) {
        int size = circlesize >0 ? circlesize : Math.min(source.getWidth(), source.getHeight());
//        int left = source.getWidth() > size ? (source.getWidth() - size) / 2 : 0;
//        int top = source.getHeight() > size ? (source.getHeight() - size) / 2 : 0;
        
        Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
        RectF rectF = new RectF(0, 0, size, size);
        Paint paint = new Paint();
        
        Bitmap outBmp = null;
        try {
            outBmp = Bitmap.createBitmap(size, size, Config.ARGB_8888);
            Canvas canvas = new Canvas(outBmp);
            paint.setAntiAlias(true);
            canvas.drawCircle(size / 2, size / 2, size / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(source, rect, rectF, paint);
            
        } catch (java.lang.OutOfMemoryError e) {
            try {
                outBmp = Bitmap.createBitmap(size, size, Config.ARGB_8888);
                paint = new Paint();
                Canvas canvas = new Canvas(outBmp);
                paint.setAntiAlias(true);
                canvas.drawCircle(size / 2, size / 2, size / 2, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(source, rect, rectF, paint);
                
            } catch (java.lang.OutOfMemoryError e1) {
                if (outBmp != null) {
                    outBmp.recycle();
                    outBmp = null;
                }
            }
        }

        return outBmp;
    }
	
	/**
	 * 保存图片到文件
	 * @param bmp
	 * @param path
	 * @param format
	 * @return
	 */
	public static boolean saveBitMaptoFile(Bitmap bmp, String path){
		if(bmp == null || bmp.isRecycled())
			return false;
		
		OutputStream stream = null;
		try {
			File file = new File(path);
			File filePath = file.getParentFile();
			if(!filePath.exists()){
				filePath.mkdirs();
			}
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			stream = new FileOutputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		CompressFormat format = CompressFormat.JPEG;
		if (bmp.hasAlpha()) {
			format = CompressFormat.PNG;
		}
		
		return bmp.compress(format, 100, stream);
	}
	
	/**
	 * 保存图片到文件
	 * 
	 * @param bmp
	 * @param path
	 * @param format
	 * @return
	 */
	public static boolean saveBitmap2File(Bitmap bmp, int quality, StoreFile file) {
		boolean ret = false;
		OutputStream stream = null;
		try {
			stream = file.openOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (stream != null) {
			CompressFormat format = CompressFormat.JPEG;
			if (bmp.hasAlpha()) {
				format = CompressFormat.PNG;
			}
			
			ret = bmp.compress(format, quality, stream);
		}
		
		file.close();
		return ret;
	}
	
	/**
	 * 解大图内存不足时尝试5次, samplesize增大
	 * 
	 * @author panjf
	 * @param file
	 * @param max 宽或高的最大值, <= 0 , 能解多大解多大, > 0, 最大max, 内存不足解更小
	 * @return
	 */
	public static Bitmap getBitmapFromFileLimitSize(String file, int max) {
		if (file == null)
			return null;
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;

		if(max > 0){
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高
			bm = BitmapFactory.decodeFile(file, options);
			options.inJustDecodeBounds = false;
	
			float blW = (float) options.outWidth / max;
			float blH = (float) options.outHeight / max;
	
			if (blW > 1 || blH > 1) {
				if(blW > blH )
					options.inSampleSize = (int) (blW + 0.9f);
				else
					options.inSampleSize = (int) (blH + 0.9f);
			}
		}
		
		int i = 0;
		while (i <= 10) {
			i++;
			try {
				bm = BitmapFactory.decodeFile(file, options);
				break;
			} catch (OutOfMemoryError e) {
				ImageManager.getInstance().clearCache();
				
				options.inSampleSize++;
				e.printStackTrace();
			}
		}
		return bm;
	}
	
	/** 从文件中解出bitmap */
	public static Bitmap getBitmapFromFile(String file) {
        if (TextUtils.isEmpty(file))
            return null;
        
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeFile(file);
        } 
        catch (OutOfMemoryError e) {}
        
        return bm;
    }
	
	/**
     * 解大图内存不足时尝试5次, samplesize增大
     * 
     * @author panjf
     * @param file
     * @param shortmax 短边的最大值, <= 0 , 能解多大解多大, > 0, 最大max, 内存不足解更小
     * @return
     */
    public static Bitmap getBitmapFromFileLimitShortSize(String file, int shortmax) {
        if (file == null)
            return null;
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        if(shortmax > 0){
            options.inJustDecodeBounds = true;
            // 获取这个图片的宽和高
            bm = BitmapFactory.decodeFile(file, options);
            options.inJustDecodeBounds = false;
    
            int changdu = options.outWidth < options.outHeight ? options.outWidth : options.outHeight;
            float bl = (float) changdu / shortmax;
    
            options.inSampleSize = (int) (bl + 0.9f);
        }
        
        int i = 0;
        while (i <= 10) {
            i++;
            try {
                bm = BitmapFactory.decodeFile(file, options);
                break;
            } catch (OutOfMemoryError e) {
                ImageManager.getInstance().clearCache();
                
                options.inSampleSize++;
                e.printStackTrace();
            }
        }
        return bm;
    }
	
	/**
	 * 解大图内存不足时尝试5次, samplesize增大
	 * 
	 * @author panjf
	 * @param context
	 * @param uri
	 * @param max
	 * @return
	 */
	public static Bitmap getBitmapFromUriLimitSize(Context context, Uri uri, int max) {
		InputStream in = null;
		try {
			in = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;

		if(max > 0){
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高
			bm = BitmapFactory.decodeStream(in, null, options);
			options.inJustDecodeBounds = false;
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			float blW = (float) options.outWidth / max;
			float blH = (float) options.outHeight / max;
	
			if (blW > 1 || blH > 1) {
				if(blW > blH )
					options.inSampleSize = (int) (blW + 0.9f);
				else
					options.inSampleSize = (int) (blH + 0.9f);
			}
		}
		
		int i = 0;
		while (i <= 10) {
			i++;
			try {
				try {
					in = context.getContentResolver().openInputStream(uri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return null;
				}
				
				bm = BitmapFactory.decodeStream(in, null, options);
				break;
			} catch (OutOfMemoryError e) {
				options.inSampleSize++;
				e.printStackTrace();
			}finally{
				if(in != null){
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return bm;
	}
	
	/**
	 * 旋转图片
	 * 
	 * @param b
	 * @param degrees
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2, (float) b
					.getHeight() / 2);

			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b
						.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				// We have no memory to rotate. Return the original bitmap.
				ex.printStackTrace();
			}
		}
		return b;
	}
	
	/**
     * 缩放成指定大小的图片
     * @param file
     * @param destW
     * @param destH
     * @return
     */
    public static Bitmap getScalBitmapToSize(String file, int destW, int destH) {
        if(file == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        Bitmap bm = BitmapFactory.decodeFile(file, options);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        float blW = (float) options.outWidth / destW;
        float blH = (float) options.outHeight / destH;
        

        options.inSampleSize = 1;
        float bl = (blW > blH ? blW : blH);
        if (bl >= 2 && bl < 4) {
                options.inSampleSize = 2;
        } else if (bl >= 4){
            options.inSampleSize = 4;
        }
        try {
             bm = BitmapFactory.decodeFile(file, options);
        } catch (OutOfMemoryError e) {
             e.printStackTrace();
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bm;
    }
    
	public static Bitmap getBitmapFromDrawable(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		
		return bitmap;
	}
	
	/**
     * 处理原图，使之符合产品的尺寸要求。
     * @param source 处理前的图片
     * @param max 最大的长或宽的值
     * @param min 最小的长或宽的值
     * @return 处理后的符合要求的图片，null则原图太小，无法处理成符合要求的图片。
     */
    public static Bitmap legitimateImageSize(Bitmap source, float max, float min){
        Bitmap result = source;
        
        if(source != null){
            float width = source.getWidth();
            float height = source.getHeight();
            
            if(width < min || height < min ){
                result = null;
            }
            else{
                float ratioW = width / max;
                float ratioH = height / max;
                
                float dstWidth, dstHeight;
                
                if (ratioW > 1 || ratioH > 1) { // 至少有一边超出最大值
                    if(ratioW > ratioH ){ // 宽比长大
                        dstWidth = max;
                        dstHeight = dstWidth / width * height;
                    }
                    else{   // 长比宽大
                        dstHeight = max;
                        dstWidth = dstHeight / height * width;
                    }
                    
                    // 按比例缩小后还需再判断是否会小于最小值
                    if(dstWidth < min || dstHeight < min){  // 缩小后不合格了
                        result = null;
                    }
                    else{ 
                        try {
                            result = Bitmap.createScaledBitmap(source, (int)dstWidth, (int)dstHeight, false);
                        } catch (OutOfMemoryError e) {
                            ImageManager.getInstance().clearCache();
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 处理原图，使之符合产品的尺寸要求。从uri中获取压缩后的图片（如果超出限制），避免内存溢出。
     * @param context
     * @param uri
     * @param max 最大的长或宽的值
     * @param min 最小的长或宽的值
     * @return 处理后的符合要求的图片，null则原图太小，无法处理成符合要求的图片。
     */
    public static Bitmap legitimateImageSize(Context context, Uri uri, float max, float min) {
        Bitmap result = null;
        InputStream in = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
        } 
        catch (FileNotFoundException e) {
            return null;
        }
        
        // 有的图片被系统旋转过，需要转回来
        int degree = getRotateDegree(context, uri);
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options); // 获取这个图片的宽和高
        
        if(in != null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        float width = options.outWidth;
        float height = options.outHeight;
        
        if(width < min || height < min ){   // 小于最小值，不合格
            result = null;
        }
        else{
            float ratioW = width / max;
            float ratioH = height / max;
            
            int inSampleSize = (int)Math.max(ratioH, ratioW);   // float转int，去小数部分，正值变小
            inSampleSize = inSampleSize == 0 ? 1 : inSampleSize;
            
            // 读取bitmap，有可能过大导致内存溢出，因此尝试压缩读取
            for(int i = 0; i < 5; i++){ // 最多缩小两次
                try {
                    options.inSampleSize = inSampleSize;
                    options.inJustDecodeBounds = false;
                    in = context.getContentResolver().openInputStream(uri);
                    result = BitmapFactory.decodeStream(in, null, options);
                    break;
                } 
                catch (OutOfMemoryError e) {
                    options.inSampleSize++;
                } 
                catch (FileNotFoundException e) {
                    return null;
                }
                finally{
                    if(in != null){
                        try {
                            in.close();
                        } catch (IOException e) {}
                    }
                }
            }
            result = rotateBitmap(result, degree);
            
            if (ratioW > 1 || ratioH > 1) { // 至少有一边超出最大值
                result = legitimateImageSize(result, max, min); // insampleSize导致还有可能偏大，需再精确缩小一次
            }
        }
        
        return result;
    }
    
    /**
     * 处理原图，使之符合产品的尺寸要求。从uri中获取压缩后的图片（如果超出限制），避免内存溢出。
     * @param context
     * @param uri
     * @param max 最大的长或宽的值
     * @param min 最小的长或宽的值
     * @return 处理后的符合要求的图片的地址，null则原图太小，无法处理成符合要求的图片。
     */
    public static String legitimateImageSizeToPath(Context context, Uri uri, float max, float min) {
        String result = uri.getPath();
        InputStream in = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
        } 
        catch (FileNotFoundException e) {
            return null;
        }
        
        // 有的图片被系统旋转过，需要转回来
        int degree = getRotateDegree(context, uri);
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options); // 获取这个图片的宽和高
        
        if(in != null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        float width = options.outWidth;
        float height = options.outHeight;
        
        if(width < min || height < min ){   // 小于最小值，不合格
            result = null;
            Toast.makeText(context, R.string.reg_tip_avatar_too_small, Toast.LENGTH_SHORT).show();
        }
        else{
            float ratioW = width / max;
            float ratioH = height / max;
            Bitmap bitmap = null;
            
            if (ratioW > 1 || ratioH > 1) { // 至少有一边超出最大值
                int inSampleSize = (int)Math.max(ratioH, ratioW);   // float转int，去小数部分，正值变小
                inSampleSize = inSampleSize == 0 ? 1 : inSampleSize;
                
                for(int i = 0; i < 2; i++){ // 最多缩小两次
                    try {
                        options.inSampleSize = inSampleSize;
                        options.inJustDecodeBounds = false;
                        in = context.getContentResolver().openInputStream(uri);
                        bitmap = BitmapFactory.decodeStream(in, null, options);
                        break;
                    } 
                    catch (OutOfMemoryError e) {
                        options.inSampleSize++;
                    } 
                    catch (FileNotFoundException e) {
                        return null;
                    }
                    finally{
                        if(in != null){
                            try {
                                in.close();
                            } catch (IOException e) {}
                        }
                    }
                }
                
                bitmap = rotateBitmap(bitmap, degree);
                bitmap = legitimateImageSize(bitmap, max, min); // insampleSize导致还有可能偏大，需再精确缩小一次
                
                result = ImageUtil.getBitmapFilePath(bitmap, EgmConstants.TEMP_PROFILE_NAME);
                bitmap.recycle();
            }
        }
        
        return result;
    }
    
    /**
     * 把Bitmap存到文件上，并得到存储路径
     * @param bitmap
     * @param fileName 文件名
     * @return 存储路径
     */
    public static String getBitmapFilePath(Bitmap bitmap, String fileName){
        String path = EgmUtil.getCacheDir() + fileName;
        
        if(ImageUtil.saveBitMaptoFile(bitmap, path)){
            return path ;
        }
        
        return null ;
    }
    
    /**
     * 聊天界面，获取显示的缩略图
     * @param activity
     * @param uri
     * @param maxWidth
     * @param minWidth
     * @return
     * @throws IOException
     */
    public static Bitmap getBmpSizeLimit(Activity activity ,String uri ,int max ,int min) 
    	throws IOException
    {
    	Bitmap result = null;
        try {
            int degree = getRotateDegree(activity, Uri.parse(uri));
            
            result = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true ;
            options.inSampleSize = 1 ;
       
            //因为从流中读取的时候会报SkImageDecoder::Factory returned null的错误，所以改为从文件中来读取
            String filePath = getAlbumImagePath(activity ,Uri.parse(uri));
            BitmapFactory.decodeFile(filePath,options);
            //图片符合要求，直接返回
            if(options.outWidth <= max && options.outWidth >= min
            		&& options.outHeight <= max && options.outHeight >= min){
            	options.inJustDecodeBounds = false ;
            	result = BitmapFactory.decodeFile(filePath);
            	return result ;
            }else{
            	//长边尺寸是短边尺寸的两倍
            	if(options.outWidth/options.outHeight >= 2 || options.outHeight/options.outWidth >=2){
            		if(options.outWidth > max && options.outHeight > max){
            			//将短边压缩到max
            			int minLength = Math.min(options.outWidth,options.outHeight);
            			options.inSampleSize = minLength/min ;
            			options.inJustDecodeBounds = false ;
            			result = BitmapFactory.decodeFile(filePath,options);
            			result = scaleBitmap(result ,max ,false);
            			//将图片从长边中间裁剪max的宽度
            			if(result.getWidth() > result.getHeight()){
            				int start = (result.getWidth() - max)/2 ;
            				result = Bitmap.createBitmap(result,start,0,max,result.getHeight());
            			}else if(result.getWidth() < result.getHeight()){
            				int start = (result.getHeight() - max)/2 ;
            				result = Bitmap.createBitmap(result,0,start,result.getWidth(),max);
            			}
            		}else if(options.outWidth < min && options.outHeight < min){
            			//将图片的短边放大到min
            			result = BitmapFactory.decodeFile(filePath);
            			result = scaleBitmap(result,min,false);
            			//如果图片的长边大于max，则进行剪裁
            			if(result.getWidth() > max){
            				int start = (result.getWidth() - max)/2 ;
            				result = Bitmap.createBitmap(result,start,0,max,result.getHeight());
            			}else if(result.getHeight() > max){
            				int start = (result.getHeight() - max)/2 ;
            				result = Bitmap.createBitmap(result,0,start,result.getWidth(),max);
            			}
            		}else{
            			result = BitmapFactory.decodeFile(filePath);
            			int maxLength = Math.max(options.outWidth,options.outHeight);
            			int minLength = Math.min(options.outWidth,options.outHeight);
            			
            			if(maxLength >= max && minLength > min){
            				//直接截取中间部分
            				if(options.outWidth > options.outHeight){
            					int start = (options.outWidth - max)/2 ;
            					result = Bitmap.createBitmap(result,start,0,max,result.getHeight());
            				}else{
            					int start = (options.outHeight - max)/2 ;
            					result = Bitmap.createBitmap(result,0,start,result.getWidth(),max);
            				}
            			}else if(maxLength < max){
            				//将短边放大到min
            				result = scaleBitmap(result,min,false);
            				if(result.getWidth() > max){
            					//进行剪裁
            					int start = (result.getWidth() - max)/2 ;
            					result = Bitmap.createBitmap(result,start,0,max,result.getHeight());
            				}else if(result.getHeight() > max){
            					//进行剪裁
            					int start = (result.getHeight() - max)/2 ;
            					result = Bitmap.createBitmap(result,0,start,result.getWidth(),max);
            				}
            			}else if(maxLength >= max && minLength <= min){
            				//将长边进行剪裁
            				if(result.getWidth() > max){
            					//进行剪裁
            					int start = (result.getWidth() - max)/2 ;
            					result = Bitmap.createBitmap(result,start,0,max,result.getHeight());
            				}else if(result.getHeight() > max){
            					//进行剪裁
            					int start = (result.getHeight() - max)/2 ;
            					result = Bitmap.createBitmap(result,0,start,result.getWidth(),max);
            				}
            				//获得的图片小边<min
            				//将图片按照小边进行放大
            				result = scaleBitmap(result,min,false);
            				//将图片按照长边进行剪裁
            				//将长边进行剪裁
            				if(result.getWidth() > max){
            					//进行剪裁
            					int start = (result.getWidth() - max)/2 ;
            					result = Bitmap.createBitmap(result,start,0,max,result.getHeight());
            				}else if(result.getHeight() > max){
            					//进行剪裁
            					int start = (result.getHeight() - max)/2 ;
            					result = Bitmap.createBitmap(result,0,start,result.getWidth(),max);
            				}
            			}
            		}
            	}else{
            		if(options.outWidth > max && options.outHeight > max){
            			int maxLength = Math.max(options.outWidth,options.outHeight);
            			options.inSampleSize = maxLength/max ;
            			options.inJustDecodeBounds = false ;
            			result = BitmapFactory.decodeFile(filePath,options);
            			//按照最长边来进行缩放
            			result = scaleBitmap(result,max,true);
            			if(degree != 0){
            				result = rotateBitmap(result,degree);
            			}
            			
            		}else if(options.outWidth < min && options.outHeight < min){
            			result = BitmapFactory.decodeFile(filePath);
            			//按照最短边来进行缩放
            			result = scaleBitmap(result,min,false);
            			if(degree != 0){
            				result = rotateBitmap(result,degree);
            			}
            		}else{
            			if(options.outWidth > max || options.outHeight > max){
            				//将长边压缩到max
            				result = BitmapFactory.decodeFile(filePath);
            				result = scaleBitmap(result,max,true);
            			}else if(options.outWidth < min || options.outHeight < min){
            				//将短边放大到min
            				result = BitmapFactory.decodeFile(filePath);
            				result = scaleBitmap(result,min,false);
            			}
            			if(degree != 0){
            				result = rotateBitmap(result,degree);
            			}
            		}
            	}
            }
        } catch (OutOfMemoryError e) {
            ImageManager.getInstance().clearCache();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return result ;
    }
    
    /**
     * 获取相册中图片的真实存储地址
     * @param activity
     * @param uri
     * @return
     */
    public static String getAlbumImagePath(Activity activity ,Uri uri){
		String srcFile = null ;
		if (uri.toString().startsWith("file://")) {
			srcFile = uri.toString().replace("file://", "");
		} else {
			Cursor cursor = activity.getContentResolver().query(uri, null,null, null, null);
			cursor.moveToFirst();
			srcFile = cursor.getString(1);
			cursor.close();
		}
		return srcFile ;
	}
    
	/**
	 * 按照指定宽度进行修正
	 * @param bitmap
	 * @param width
	 * asMax :按照最长边来进行缩放
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap src,int max,boolean asMax){
		if(src == null){
			return null ;
		}
		
		int bmpWidth = src.getWidth() ;
		int bmpHeight = src.getHeight() ;
		
		int length = 0 ;
		if(asMax){
			length = Math.max(bmpWidth, bmpHeight);
		}else{
			length = Math.min(bmpWidth, bmpHeight);
		}
		
		if(length == max){
			return src ;
		}
		
		Bitmap result = null ;
		Matrix matrix = new Matrix() ;
		
		float scale = max *1.0f/length ;
		matrix.postScale(scale, scale);
		
		try {
            result = Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),matrix,false);
        } catch (OutOfMemoryError e) {
            ImageManager.getInstance().clearCache();
            e.printStackTrace();
        }
		
		return result ;
	}
    
    /**
     * 判断图片长宽是否小于指定的最小值
     * @param context
     * @param uri
     * @param min
     * @return
     */
    public static boolean isPictureTooSamll(Context context, Uri uri, float max, float min) {
        InputStream in = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
        } 
        catch (FileNotFoundException e) {
            return true;
        }
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options); // 获取这个图片的宽和高
        
        if(in != null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        float width = options.outWidth;
        float height = options.outHeight;
        
        // 压缩后，可能小边小于min
        if(width > max || height > max){    
            if(width > height){
                height = max * height / width;
            }
            else{
                width = max * width / height;
            }
        }
        
        if(width < min || height < min ){   // 小于最小值
            return true;
        }
        
        return false;
    }
    
    public static int getRotateDegree(Context context, Uri uri) {
        if (uri == null) {
            return 0;
        }
        String file = uri.getPath();
        if (TextUtils.isEmpty(file)) {
            return 0;
        }
        ExifInterface exif;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        int degree = 0;
        if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
            // We only recognize a subset of orientation tag values.
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
        } else {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String orientationDb = cursor.getString(cursor
                        .getColumnIndex("orientation"));
                cursor.close();
                if (!TextUtils.isEmpty(orientationDb)) {
                    degree = Integer.parseInt(orientationDb);
                }
            }
        }
        
        return degree;
    }
    /**
     * 
     * @param file fileName
     * @return
     */
    public static int getRotateDegree(String file) {
        if (TextUtils.isEmpty(file)) {
            return 0;
        }
        ExifInterface exif;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        int degree = 0;
        if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
            // We only recognize a subset of orientation tag values.
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
        }
        
        return degree;
    }
    
	public static boolean saveBitmap(Bitmap bitmap, String path, int quality,
			boolean recyle) {
		if (bitmap == null || TextUtils.isEmpty(path)) {
			return false;
		}
		
		File file = new File(path);
		
		File parent = file.getParentFile();
		if (parent != null && ! parent.exists()) {
			parent.mkdirs();
		}
		
		BufferedOutputStream os = null;
		try {
			FileOutputStream fos = new FileOutputStream(file);
			os = new BufferedOutputStream(fos);
			if (quality <= 0) {
				quality = 95;
			}
			
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
			
			return true;
		} catch (Exception e) {
			
		} catch (Error e) {
			
		}
		finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
			if (recyle) {
				bitmap.recycle();
			}
		}
		
		return false;
	}

	public static boolean saveBitmap(Bitmap bitmap, String path, boolean recyle) {
		if (bitmap == null || TextUtils.isEmpty(path)) {
			return false;
		}
		
		File file = new File(path);
		
		File parent = file.getParentFile();
		if (parent != null && ! parent.exists()) {
			parent.mkdirs();
		}

		BufferedOutputStream os = null;
		try {
			FileOutputStream fos = new FileOutputStream(file);
			os = new BufferedOutputStream(fos);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 70, os);
			return true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (recyle) {
				bitmap.recycle();
			}
		}
	}
    
    private static int textureSize;

	public static final int getTextureSize() {
		if (textureSize > 0) {
			return textureSize;
		}

		int[] params = new int[1];
		GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, params, 0);
		textureSize = params[0];

		return textureSize;
	}
	
	public static Bitmap decodeSampledForDisplay(String pathName) {
		return decodeSampledForDisplay(pathName, true);
	}
	
	public static Bitmap rotateBitmapInNeeded(String path, Bitmap srcBitmap) {
		if (TextUtils.isEmpty(path) || srcBitmap == null) {
			return null;
		}

		ExifInterface localExifInterface;
		try {
			localExifInterface = new ExifInterface(path);
			int rotateInt = localExifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			float rotate = getImageRotate(rotateInt);
			if (rotate != 0) {
				Matrix matrix = new Matrix();
				matrix.postRotate(rotate);
				Bitmap dstBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
						srcBitmap.getWidth(), srcBitmap.getHeight(), matrix,
						false);
				if (dstBitmap != null) {
					if (srcBitmap != null && !srcBitmap.isRecycled()) {
						srcBitmap.recycle();
					}
					return dstBitmap;
				}
			}
		} catch (IOException e) {
			
		} catch (OutOfMemoryError e) {
		}
		
		return srcBitmap;
	}
	
	public static float MAX_BITMAP_RATIO = 5.0F;
	public static Bitmap decodeSampledForDisplay(String pathName,
			boolean withTextureLimit) {
		float ratio = MAX_BITMAP_RATIO;
		int screenWidth = DisplayUtil.getDisplayWidth(BaseService.getServiceContext());
		int screenHeight = DisplayUtil.getDisplayHeight(BaseService.getServiceContext());
		int[][] reqBounds = new int[][] {
				new int[] { screenWidth * 4, screenHeight },
				new int[] { screenWidth, screenHeight * 4 },
				new int[] { screenWidth * 2, screenHeight * 2 }, };

		// decode bound
		int[] bound = decodeBound(pathName);
		// pick request bound
		int[] reqBound = pickReqBoundWithRatio(bound, reqBounds, ratio);

		int width = bound[0];
		int height = bound[1];
		int reqWidth = reqBound[0];
		int reqHeight = reqBound[1];

		// calculate sample size
		int sampleSize = calculateSampleSize(width, height, reqWidth, reqHeight);

		if (withTextureLimit) {
			// adjust sample size
			sampleSize = adjustSampleSizeWithTexture(sampleSize, width, height);
		}

		int RETRY_LIMIT = 5;
		Bitmap bitmap = decodeSampled(pathName, sampleSize);
		while (bitmap == null && RETRY_LIMIT > 0) {
			sampleSize++;
			RETRY_LIMIT--;
			bitmap = decodeSampled(pathName, sampleSize);
		}

		return bitmap;
	}
	
	public static Bitmap decodeSampled(String pathName, int sampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();

		// RGB_565
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		// sample size
		options.inSampleSize = sampleSize;

		try {
			return BitmapFactory.decodeFile(pathName, options);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private static int[] pickReqBoundWithRatio(int[] bound, int[][] reqBounds,
			float ratio) {
		float hRatio = bound[1] == 0 ? 0 : (float) bound[0] / (float) bound[1];
		float vRatio = bound[0] == 0 ? 0 : (float) bound[1] / (float) bound[0];

		if (hRatio >= ratio) {
			return reqBounds[0];
		} else if (vRatio >= ratio) {
			return reqBounds[1];
		} else {
			return reqBounds[2];
		}
	}
	
	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options}
	 * object when decoding bitmaps using the decode* methods from
	 * {@link BitmapFactory}. This implementation calculates the closest
	 * inSampleSize that will result in the final decoded bitmap having a width
	 * and height equal to or larger than the requested width and height. This
	 * implementation does not ensure a power of 2 is returned for inSampleSize
	 * which can be faster when decoding but results in a larger bitmap which
	 * isn't as useful for caching purposes.
	 * 
	 * @param width
	 * @param height
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateSampleSize(int width, int height, int reqWidth,
			int reqHeight) {
		// can't proceed
		if (width <= 0 || height <= 0) {
			return 1;
		}
		// can't proceed
		if (reqWidth <= 0 && reqHeight <= 0) {
			return 1;
		} else if (reqWidth <= 0) {
			reqWidth = (int) (width * reqHeight / (float) height + 0.5f);
		} else if (reqHeight <= 0) {
			reqHeight = (int) (height * reqWidth / (float) width + 0.5f);
		}

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee a final image
			// with both dimensions larger than or equal to the requested height
			// and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
			if (inSampleSize == 0) {
				inSampleSize = 1;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			final float totalPixels = width * height;

			// Anything more than 2x the requested pixels we'll sample down
			// further
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}

		return inSampleSize;
	}

	public static int calculateSampleSize(String imagePath, int totalPixel) {
		int[] bound = decodeBound(imagePath);
		return calculateSampleSize(bound[0], bound[1], totalPixel);
	}

	public static int calculateSampleSize(InputStream is, int totalPixel) {
		int[] bound = decodeBound(is);
		return calculateSampleSize(bound[0], bound[1], totalPixel);
	}

	public static int calculateSampleSize(int width, int height, int totalPixel) {
		int ratio = 1;

		if (width > 0 && height > 0) {
			ratio = (int) Math.sqrt((float) (width * height) / totalPixel);
			if (ratio < 1) {
				ratio = 1;
			}
		}

		return ratio;
	}

	private static final int roundup2n(int x) {
		if ((x & (x - 1)) == 0) {
			return x;
		}
		int pos = 0;
		while (x > 0) {
			x >>= 1;
			++pos;
		}
		return 1 << pos;
	}
	
	public static final int adjustSampleSizeWithTexture(int sampleSize,
			int width, int height) {
		int textureSize = getTextureSize();

		if ((textureSize > 0)
				&& ((width > sampleSize) || (height > sampleSize))) {
			while ((width / (float) sampleSize) > textureSize
					|| (height / (float) sampleSize) > textureSize) {
				sampleSize++;
			}

			sampleSize = roundup2n(sampleSize);
		}

		return sampleSize;
	}
	
	public static float getImageRotate(int rotate) {
		float f;
		if (rotate == 6) {
			f = 90.0F;
		} else if (rotate == 3) {
			f = 180.0F;
		} else if (rotate == 8) {
			f = 270.0F;
		} else {
			f = 0.0F;
		}

		return f;
	}

	public static int[] decodeBound(String pathName) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		return new int[] { options.outWidth, options.outHeight };
	}

	public static int[] decodeBound(InputStream is) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, options);

		return new int[] { options.outWidth, options.outHeight };
	}
	

}
