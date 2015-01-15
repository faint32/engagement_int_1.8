package com.netease.engagement.widget;

import com.netease.date.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ProgressImageView extends ImageView {
	
	private BitmapDrawable bitmapDrawable;
	private float startAngle;
	private float sweepAngle;
	
	private Paint paint;
	private RectF rectF;
	private Path path;
	

	public ProgressImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public ProgressImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProgressImageView(Context context) {
		super(context);
		init();
	}
	
	public void init() {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				
		paint = new Paint();
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		rectF = new RectF();
		path = new Path();
		bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_pgrecommendlist_color_circle_red);
	}
	
	public void setAngle(float startAngle, float sweepAngle) {
		this.startAngle = startAngle;
		this.sweepAngle = sweepAngle;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (startAngle==0 && sweepAngle==0) {
			return;
		}
		float center_X = getWidth() / 2;
		float center_Y = getHeight() / 2;
		float r = getWidth() / 2;
		try {
			canvas.save();
			rectF.set(center_X - r, center_Y - r, center_X + r, center_Y + r);
			path.reset();
			path.moveTo(center_X, center_Y);
			path.lineTo(center_X, 0);
			path.arcTo(rectF, startAngle, sweepAngle);
			path.lineTo(center_X, center_Y);
			path.close();
			canvas.clipPath(path);
			bitmapDrawable.setBounds(0, 0, getWidth(), getHeight());
			bitmapDrawable.draw(canvas);
			canvas.restore();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
	}
	
}
