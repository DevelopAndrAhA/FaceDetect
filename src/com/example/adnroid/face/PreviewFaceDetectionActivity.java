package com.example.adnroid.face;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class PreviewFaceDetectionActivity extends Activity implements FaceDetectionListener {
	private CameraPreview preview;
	private OverlayView overlay;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		preview = new CameraPreview(this);
		setContentView(preview);
		overlay = new OverlayView(this);
		addContentView(overlay, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}
	
	class OverlayView extends View {
		Face[] faces;
		CameraInfo info = new Camera.CameraInfo();

		public OverlayView(Context context) {
			super(context);
			setFocusable(true);
			Camera.getCameraInfo(0, info);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawColor(Color.TRANSPARENT);
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			paint.setTextSize(Math.min(getWidth(), getHeight()) / 20);
			if (faces != null) {
				for (Face face : faces) {
					Matrix matrix = new Matrix();
					boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
					matrix.setScale(mirror ? -1 : 1, 1);
					matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
					matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);

					// 現在のマトリックスを保存
					int saveCount = canvas.save();
					// 顔認識のマトリックスをキャンバスに反映
					canvas.concat(matrix);
					canvas.drawText("" + face.score, (face.rect.right + face.rect.left) / 2, (face.rect.top + face.rect.bottom) / 2, paint);
					// 矩形を描画
					canvas.drawRect(face.rect, paint);
					// 保存したマトリックスを戻す
					canvas.restoreToCount(saveCount);
				}
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (preview.camera != null) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int maxFace = preview.camera.getParameters().getMaxNumDetectedFaces(); // Galaxy Nexus は 35
					Log.d("TEST", "getMaxNumDetectedFaces:" + maxFace);
					if (maxFace > 0) {
						preview.camera.startFaceDetection();
					} else {
						Toast.makeText(PreviewFaceDetectionActivity.this, "This device does not support face detection", Toast.LENGTH_SHORT).show();
					}
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					preview.camera.stopFaceDetection();
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void onFaceDetection(Face[] faces, Camera camera) {
		Log.d("TEST", "onFaceDetection: faces=" + faces.length);
		overlay.faces = faces;
		overlay.invalidate();
	}
}
