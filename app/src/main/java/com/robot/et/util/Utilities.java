package com.robot.et.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
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

    // 中文的数字转int
    public static int chineseNumber2Int(String str) {
        int result = 0;
        if (!TextUtils.isEmpty(str)) {
            int temp = 1;//存放一个单位的数字如：十万
            int count = 0;//判断是否有chArr
            int strNum = 0;// 是否有数字
            char[] cnArr = new char[]{'一', '二', '三', '四', '五', '六', '七', '八', '九', '两'};
            char[] chArr = new char[]{'十', '百', '千', '万', '亿'};
            int length = str.length();
            int cnArrLength = cnArr.length;
            int chArrLength = chArr.length;
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < length; i++) {
                char c = str.charAt(i);
                if (Character.isDigit(c)) {// 是数字
                    buffer.append(c);
                } else {
                    boolean b = true;//判断是否是chArr
                    for (int j = 0; j < cnArrLength; j++) {//非单位，即数字
                        if (c == cnArr[j]) {
                            if (0 != count) {//添加下一个单位之前，先把上一个单位值添加到结果中
                                result += temp;
                                count = 0;
                            }
                            // 下标+1，就是对应的值
                            if (j == cnArrLength - 1) {
                                temp = 2;
                            } else {
                                temp = j + 1;
                            }
                            b = false;
                            strNum = 1;
                            break;
                        }
                    }
                    if (b) {//单位{'十','百','千','万','亿'}
                        for (int j = 0; j < chArrLength; j++) {
                            if (c == chArr[j]) {
                                switch (j) {
                                    case 0:
                                        temp *= 10;
                                        break;
                                    case 1:
                                        temp *= 100;
                                        break;
                                    case 2:
                                        temp *= 1000;
                                        break;
                                    case 3:
                                        temp *= 10000;
                                        break;
                                    case 4:
                                        temp *= 100000000;
                                        break;
                                    default:
                                        break;
                                }
                                count++;
                                strNum = 1;
                            }
                        }
                    }
                    if (i == str.length() - 1) {//遍历到最后一个字符
                        if (strNum != 0) {
                            result += temp;
                        }
                    }
                }
            }
            String tempDistance = buffer.toString();
            if (!TextUtils.isEmpty(tempDistance)) {
                if (TextUtils.isDigitsOnly(tempDistance)) {
                    result = Integer.parseInt(tempDistance);
                }
            }
        }
        return result;
    }
}
