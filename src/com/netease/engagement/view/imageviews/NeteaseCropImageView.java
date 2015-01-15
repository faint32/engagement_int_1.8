package com.netease.engagement.view.imageviews;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.netease.common.image.util.ImageUtil;

public class NeteaseCropImageView extends MultiTouchZoomableImageView{
private static final int MARGIN = 50;
	
	private int outputX;
	private int outputY;

	Paint shadowPaint;
	Paint linePaint;
	
	private Bitmap shadowBitmap;
	private Rect drawRect;
    private Rect selection;
    private StringBuilder mOrinCoordInfo = new StringBuilder();
    
	// Programatic entry point
	public NeteaseCropImageView(Context context) {
		super(context);
		initCropImageView(context);
	}

	// XML entry point
	public NeteaseCropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCropImageView(context);
	}
	
	protected void initCropImageView(Context context) {
		shadowPaint = new Paint();
		shadowPaint.setColor(Color.argb((int) (255 * 0.6), 0, 0, 0));
		
		linePaint = new Paint();
		linePaint.setColor(0xffffffff);
		linePaint.setAntiAlias(true);
		
		drawRect = new Rect();
		
		transIgnoreScale = true;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		if (changed) {
			selection = updateSelection();
		}
	}
	
	public void setOutput(int outputX, int outputY) {
		this.outputX = outputX;
		this.outputY = outputY;
	}
		
	public byte[] getCroppedImage() {
		Bitmap cropped = getCroppedBitmap();
		if (cropped == null) {
			return null;
		}
		
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        cropped.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        if (cropped != mBitmap) {
        	cropped.recycle();
        }
        
        byte[] data = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
	
	public boolean saveCroppedIamge(String path) {
		Bitmap cropped = getCroppedBitmap();
        return ImageUtil.saveBitmap(cropped, path, cropped != mBitmap);
	}
	
	public boolean saveOriginalImage(String path) {
		if (!composeCoordinateInfo()) {
			return false;
		}
		
		return ImageUtil.saveBitmap(mBitmap, path, false);
	}
	
	public boolean composeCoordinateInfo() {
		Bitmap bitmap = getImageBitmap();
		if (bitmap == null) {
			return false;
		}
		if (selection == null) {
			return false;
		}
		
        Matrix matrix = getImageViewMatrix();
        
        
        float transX = getValue(matrix, Matrix.MTRANS_X);
        float transY = getValue(matrix, Matrix.MTRANS_Y);
        float scale = getValue(matrix, Matrix.MSCALE_X);
        
        int x = (int) ((selection.left - transX) / scale);
        int y = (int) ((selection.top - transY) / scale) ;
        int width = (int) (selection.width() / scale);
        int height = (int) (selection.height() / scale);
        
        x = (x >= 0 ? x : 0);
        y = (y >= 0 ? y : 0);
        width = (width <= bitmap.getWidth() - x ? width : bitmap.getWidth() - x);
        height = (height <= bitmap.getHeight() - y ? height : bitmap.getHeight() - y);
        
        mOrinCoordInfo.setLength(0);
        mOrinCoordInfo.append(x).append('&').append(y).append('&').append(width).append('&').append(height);
        return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		  if (selection != null) {
			if (shadowBitmap == null) {
				shadowBitmap = Bitmap.createBitmap(getMeasuredWidth(),
						getMeasuredHeight(), Config.ARGB_8888);
				Canvas mCanvas = new Canvas(shadowBitmap);
				mCanvas.save();
				mCanvas.drawARGB(0, 0, 0, 0);
				shadowPaint.setColor(Color.argb((int) (255 * 0.6), 0, 0, 0));
				shadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				drawRect.set(0, 0, getRight(), getBottom());
				mCanvas.drawRect(drawRect, shadowPaint);

				shadowPaint.setColor(0xff000000);
				shadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				shadowPaint.setStrokeWidth(2);
				shadowPaint.setAntiAlias(true);
				shadowPaint.setXfermode(new PorterDuffXfermode(
						android.graphics.PorterDuff.Mode.XOR));
				mCanvas.translate(0, 0);
				mCanvas.drawCircle((selection.left + selection.right) / 2,
						(selection.top + selection.bottom) / 2,
						(selection.right - selection.left) / 2, shadowPaint);
				mCanvas.restore();
				
				PathEffect effects = new DashPathEffect(new float[] { 8, 4,
						8, 4 }, 1);
				linePaint.setPathEffect(effects);
				linePaint.setAntiAlias(true);
				linePaint.setStrokeWidth(2);
				linePaint.setStyle(Style.STROKE);
				mCanvas.drawCircle((selection.left + selection.right) / 2,
						(selection.top + selection.bottom) / 2,
						(selection.right - selection.left) / 2, linePaint);
			}
			   
			canvas.drawBitmap(shadowBitmap, 0, 0, new Paint());  
	   }
		
	}
	
	@Override
	protected Rect updateSelection() {
		if (outputX <= 0 || outputY <= 0) {
			return null;
		}
		Rect selection;
		
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		float outputRatio = ((float)outputY) / outputX;
		float screenRatio = ((float)viewHeight) / viewWidth;
		if (outputRatio < screenRatio) {
			int width = viewWidth - MARGIN * 2;
			int height = outputY * width / outputX;
			int x = MARGIN;
			int y = (viewHeight - height) / 2;
			selection = new Rect(x, y, x + width, y + height);
		} else {
			int height =  viewHeight - MARGIN * 2;
			int width = outputX * height / outputY;
			int y = MARGIN;
			int x = (viewWidth - width) / 2;
			selection = new Rect(x, y, x + width, y + height);
		}
		
		return selection;
	}
	
	/***
	 * Determine whether the clip area size is meet with the requirement.
	 * @return values[0]: the width area of the original image for cropping image.
	 * 		   values[1]: the height area of the original image for cropping image.
	 */
	public int[] isMeetRequired() {
		 	Matrix matrix = getImageViewMatrix();
	        float scale = getValue(matrix, Matrix.MSCALE_X);
	        int width = (int) (selection.width() / scale);
	        int height = (int) (selection.height() / scale);
	        
	        int[] values = new int[2];
	        values[0] = width;
	        values[1] = height;
	        return values;
	}
	
	public String getOriginalCoordInfo() {
		return mOrinCoordInfo.toString();
	}
	
	
	private Bitmap getCroppedBitmap() {
		Bitmap bitmap = getImageBitmap();
		if (bitmap == null) {
			return null;
		}
		if (selection == null) {
			return bitmap;
		}
		
        Matrix matrix = getImageViewMatrix();
        
        
        float transX = getValue(matrix, Matrix.MTRANS_X);
        float transY = getValue(matrix, Matrix.MTRANS_Y);
        float scale = getValue(matrix, Matrix.MSCALE_X);
        
        int x = (int) ((selection.left - transX) / scale);
        int y = (int) ((selection.top - transY) / scale) ;
        int width = (int) (selection.width() / scale);
        int height = (int) (selection.height() / scale);
        
        x = (x >= 0 ? x : 0);
        y = (y >= 0 ? y : 0);
        width = (width <= bitmap.getWidth() - x ? width : bitmap.getWidth() - x);
        height = (height <= bitmap.getHeight() - y ? height : bitmap.getHeight() - y);
        
        mOrinCoordInfo.setLength(0);
        mOrinCoordInfo.append(x).append('&').append(y).append('&').append(width).append('&').append(height);
		
        float outputRatio = ((float)outputY) / outputX;
		float screenRatio = ((float)height) / width;
		if (outputRatio < screenRatio) {
			height = (int) (width * outputRatio);
		} else {
			width = (int) (height / outputRatio);
		}
        
        Matrix m = new Matrix();
        final float sx = outputX  / (float)width;
        m.setScale(sx, sx);
        try{
        	return Bitmap.createBitmap(bitmap, x, y, width, height, m, false);
        }catch(Exception e){
        	
        	return null;
        }
        
	}
	
	@Override
	protected void center(boolean vertical, boolean horizontal, boolean animate) {
		if (mBitmap == null)
			return;
		if (selection == null) {
			invalidate();
			return;
		}

		Matrix m = getImageViewMatrix();

		float [] topLeft  = new float[] { 0, 0 };
		float [] botRight = new float[] { mBitmap.getWidth(), mBitmap.getHeight() };

		translatePoint(m, topLeft);
		translatePoint(m, botRight);

		float deltaX = 0, deltaY = 0;
		
		if (vertical) {
			if (topLeft[1] > selection.bottom) {
				deltaY = selection.bottom - topLeft[1];
			} else if (botRight[1] < selection.top) {
				deltaY = selection.top - botRight[1];
			}
		}

		if (horizontal) {
			if (topLeft[0] > selection.right) {
				deltaX = selection.right - topLeft[0];
			} else if (botRight[0] < selection.left) {
				deltaX = selection.left - botRight[0];
			}
		}

		postTranslate(deltaX, deltaY);
		if (animate) {
			Animation a = new TranslateAnimation(-deltaX, 0, -deltaY, 0);
			a.setStartTime(SystemClock.elapsedRealtime());
			a.setDuration(250);
			setAnimation(a);
		}
		setImageMatrix(getImageViewMatrix());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = super.onTouchEvent(event);
		
		if(mBitmap != null) {
			if(event.getAction() == MotionEvent.ACTION_UP) {
                if (fling()) {
                    if (checkImagePosition(false)) {
                        stopFling();
                    }
                } else {
                    checkImagePosition(true);
                }
			}
		}
		
		return handled;
	}
	
	
//	// Sets the bitmap for the image and resets the base
//	public void setImageBitmap(final Bitmap bitmap) {
//		super.setImageBitmap(bitmap, selection);
//		invalidate();
//	}
	
	/**
	 * 
	 *
	 * @return
	 */
	protected boolean checkImagePosition(boolean scroll) {

		boolean translate = false;
		if(mBitmap == null || selection == null){
			return translate;
		}
		Matrix m = getImageViewMatrix();

		float [] topLeft  = new float[] { 0, 0 };
		float [] botRight = new float[] { mBitmap.getWidth(), mBitmap.getHeight() };

		translatePoint(m, topLeft);
		translatePoint(m, botRight);
		float transX = 0.0f;
		float transY = 0.0f;

		if (topLeft[0] >= selection.left && botRight[0] <= selection.right) {
			transY = 0;
			translate = false;
			center(true, true, false);
		} else if(topLeft[0] > selection.left){
			transX = selection.left - topLeft[0];
			translate = true;
		} else if(botRight[0] < selection.right){
			transX = selection.right - botRight[0];
			translate = true;
		}

		if (topLeft[1] >= selection.top && botRight[1] <= selection.bottom) {
			transY = 0;
			translate = false;
			center(true, true, false);
		} else if(topLeft[1] > selection.top){
			transY = selection.top - topLeft[1];
			translate = true;
		} else if(botRight[1] < selection.bottom){
			transY = selection.bottom - botRight[1];
			translate = true;
		}

		if(scroll && translate){
			scrollBy(transX, transY, 200);
		}

		return translate;
	}

    @Override
    protected void onScrollFinish() {
    	checkImagePosition(true);
    }
}
