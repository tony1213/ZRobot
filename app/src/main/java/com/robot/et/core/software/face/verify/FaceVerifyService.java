package com.robot.et.core.software.face.verify;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.FaceRequest;
import com.iflytek.cloud.RequestListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class FaceVerifyService extends Service {

	// 预览帧数据存储数组
	private byte[] nv21;
	// Camera nv21格式预览帧的尺寸，默认设置640*480
	private int PREVIEW_WIDTH = 640;
	private int PREVIEW_HEIGHT = 480;
	private FaceRequest mFaceRequest;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mFaceRequest = new FaceRequest(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.robot.et.face.detector");
		registerReceiver(receiver, filter);
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("com.robot.et.face.verify")) {
				nv21 = intent.getByteArrayExtra("nv21");
				if (null==nv21) {
					return;
				}
				// 拷贝到临时数据中
				byte[] tmp = new byte[nv21.length];
				System.arraycopy(nv21, 0, tmp, 0, nv21.length);
				verify(Bitmap2Bytes(RotateDeg90(decodeToBitMap(tmp))));
			}
		}
	};

	private void verify(byte[] mImageData) {
		if (null != mImageData) {
			// 设置用户标识，格式为6-18个字符（由字母、数字、下划线组成，不得以数字开头，不能包含空格）。
			// 当不设置时，云端将使用用户设备的设备ID来标识终端用户。
			mFaceRequest.setParameter(SpeechConstant.AUTH_ID, "q123456");
			mFaceRequest.setParameter(SpeechConstant.WFR_SST, "verify");
			mFaceRequest.sendRequest(mImageData, new RequestListener() {

				@Override
				public void onEvent(int arg0, Bundle arg1) {
					Log.e("face", "error:onEvent");
				}

				@Override
				public void onCompleted(SpeechError arg0) {
					if (arg0 != null)
						Log.e("face", "error:" + arg0.getErrorCode());
				}

				@Override
				public void onBufferReceived(byte[] arg0) {
					try {
						String result = new String(arg0, "utf-8");
						JSONObject object = new JSONObject(result);
						int ret = object.getInt("ret");
						if (ret != 0) {
							Log.e("face","验证失败");
							return;
						}
						if ("success".equals(object.get("rst"))) {
							if (object.getBoolean("verf")) {
								Toast.makeText(getApplicationContext(),"通过验证，欢迎回来！", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(getApplicationContext(),"验证不通过", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(getApplicationContext(), "验证失败",Toast.LENGTH_SHORT).show();
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private Bitmap decodeToBitMap(byte[] data) {
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21,PREVIEW_WIDTH, PREVIEW_HEIGHT, null);
			if (image != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, PREVIEW_WIDTH,PREVIEW_HEIGHT), 80, stream);
				Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
				stream.close();
				return bmp;
			}
		} catch (Exception ex) {
			Log.e("face", "Error:" + ex.getMessage());
		}
		return null;
	}

	private Bitmap RotateDeg90(Bitmap bmp) {
		// 定义矩阵对象
		Matrix matrix = new Matrix();
		// 缩放原图
		matrix.postScale(1f, 1f);
		// 向左旋转45度，参数为正则向右旋转
		matrix.postRotate(-90);
		// bmp.getWidth(), 500分别表示重绘后的位图宽高
		Bitmap dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),bmp.getHeight(), matrix, true);
		return dstbmp;
	}

	private byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
}
