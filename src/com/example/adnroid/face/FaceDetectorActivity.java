package com.example.adnroid.face;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class FaceDetectorActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new FaceView(this));
	}

	class FaceView extends View {
		private int index;
		private boolean useOriginal;
		private Bitmap[] bitmap = new Bitmap[3];
		int width;
		int height;
		public FaceView(Context context) {
			super(context);
			// 16bit にしないと認識しなかったり誤認識したりする
			Options opts = new Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			bitmap[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.image0, opts);
			bitmap[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.image1, opts);
			bitmap[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.image2, opts);

			setFocusable(true);
			setClickable(true);
			Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			width = display.getWidth();
			height = display.getHeight();
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			int bmpWidth = bitmap[index].getWidth();
			int bmpHeight = bitmap[index].getHeight();
			
			float ratioW = (float)width / bmpWidth;
			float ratioH = (float)height / bmpHeight;
			float ratio = Math.min(ratioW, ratioH);
			
			int dstWidth = (int)(ratio * bmpWidth);
			int dstHeight = (int)(ratio * bmpHeight);
			int x = (width - dstWidth) / 2;
			int y = (height - dstHeight) / 2;
			long start, end;
			Bitmap scaledBitmap =  Bitmap.createScaledBitmap(bitmap[index], dstWidth, dstHeight, false);
			canvas.drawBitmap(scaledBitmap, x, y, null);
			
			FaceDetector.Face faces[] = new FaceDetector.Face[10];
			FaceDetector detector;
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setTextSize(Math.min(getWidth(), getHeight()) / 20);
			FontMetrics fm = paint.getFontMetrics();
			if (useOriginal) {
				canvas.drawText("Original witdh:" + bmpWidth + " height:" + bmpHeight, 0, -fm.ascent, paint);
				detector = new FaceDetector(bmpWidth, bmpHeight, faces.length);
				start = System.currentTimeMillis();
				detector.findFaces(bitmap[index], faces);
				end = System.currentTimeMillis();
			} else {
				canvas.drawText("Scaled witdh:" + dstWidth + " height:" + dstHeight, 0, -fm.ascent, paint);
				detector = new FaceDetector(dstWidth, dstHeight, faces.length);
				start = System.currentTimeMillis();
				detector.findFaces(scaledBitmap, faces);
				end = System.currentTimeMillis();
			}
			canvas.drawText("Time:" + (end - start) + " ms", 0, -fm.ascent * 2 + fm.descent, paint);
			
			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			for (int i = 0; i < faces.length; i++) {
				if (faces[i] == null) {
					break;
				}
				float eyedist = faces[i].eyesDistance();
				PointF mid = new PointF();
				faces[i].getMidPoint(mid);
				if (useOriginal) {
					mid.x *= ratio;
					mid.y *= ratio;
					eyedist *= ratio;
				}
				RectF rect = new RectF();
				rect.left = x + mid.x - eyedist;
				rect.top = y + mid.y - eyedist / 2;
				rect.right = x + mid.x + eyedist;
				rect.bottom = y + mid.y + eyedist * 1.5f;
				canvas.drawRect(rect, paint);
				String confidence = "" + faces[i].confidence();
				if (confidence.length() > 4) {
					confidence = confidence.substring(0, 4);
				}
				canvas.drawText(confidence, (rect.left + rect.right) / 2, (rect.top + rect.bottom) / 2, paint);
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				index = ++index % bitmap.length;
				if (index == 0) {
					useOriginal = !useOriginal;
				}
				invalidate();
				return true;
			}
			return false;
		}
	}
	
}
