package com.example.adnroid.face;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	protected Context context;
	protected Camera camera;
	
	CameraPreview(Context context) {
		super(context);
		this.context = context;
		getHolder().addCallback(this);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("TEST", "surfaceCreated");
		if (camera == null) {
			try {
				camera = Camera.open(0);
			} catch (RuntimeException e) {
				((Activity)context).finish();
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		if (camera != null) {
			camera.setPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					Log.d("TEST", "onPreviewFrame: preview: data=" + data);
				}
			});
			camera.setOneShotPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					Log.d("TEST", "onPreviewFrame: short preview: data=" + data);
				}
			});
			camera.setErrorCallback(new ErrorCallback() {
				@Override
				public void onError(int error, Camera camera) {
					Log.d("TEST", "onError: error=" + error);
				}
			});
			camera.setFaceDetectionListener((FaceDetectionListener)context);
		}
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			camera.release();
			camera = null;
			((Activity)context).finish();
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d("TEST", "surfaceChanged");
		if (camera == null) {
			((Activity)context).finish();
		} else {
			camera.stopPreview();
			setPreviewSize(width, height);
			camera.startPreview();
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("TEST", "surfaceDestroyed");
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
	
	private void setPreviewSize(int width, int height) {
		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> supported = params.getSupportedPreviewSizes();
		if (supported != null) {
			for (Camera.Size size : supported) {
				if (size.width <= width && size.height <= height) {
					params.setPreviewSize(size.width, size.height);
					camera.setParameters(params);
					break;
				}
			}
		}
	}
}
