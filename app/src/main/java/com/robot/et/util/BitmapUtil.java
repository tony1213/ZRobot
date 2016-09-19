package com.robot.et.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by houdeming on 2016/9/12.
 */
public class BitmapUtil {
    private static final String TAG = "bitmap";

    // byte数组转bitmap
    public static Bitmap byte2Bitmap(byte[] data) {
        Bitmap bitmap = null;
        if (data != null && data.length != 0) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bitmap;
    }

    // bitmap转byte数组
    public static byte[] bitmap2Byte(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    // 压缩图片
    public static Bitmap decodeToBitMap(byte[] data, int width, int height, int compressValue) {
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, width, height), compressValue, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
                return bmp;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error:" + ex.getMessage());
        }
        return null;
    }
}
