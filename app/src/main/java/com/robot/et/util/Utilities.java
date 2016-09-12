package com.robot.et.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.Hashtable;

/**
 * Created by houdeming on 2016/8/4.
 */
public class Utilities {
    private static final String TAG = "Utilities";

    //是否连接网络
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //生成二维码图片
    public static Bitmap createQRCode(String content) {
        Bitmap bitmap = null;
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            Hashtable<EncodeHintType, String> hst = new Hashtable<EncodeHintType, String>();
            hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // 图片大小 800x800
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 800, 800, hst);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    }
                }
            }
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            // 通过像素数组生成bitmap,具体参考api
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        } catch (Exception e) {
            Log.i(TAG, "createQRCode Exception==" + e.getMessage());
        }
        return bitmap;
    }
}
