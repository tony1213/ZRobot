package com.robot.et.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by Tony on 2016/7/26.
 */
public class FileUtils {
    /**
     * 读取asset目录下文件。
     * @return content
     */
    public static String readFile(Context context, String file, String code) {
        if (null==context){
            Log.e("FileUtils","context is null");
            return "";
        }else {
            AssetManager am=context.getAssets();
            int len = 0;
            byte[] buf = null;
            String result = "";
            try {
                InputStream in = am.open(file);
                len = in.available();
                buf = new byte[len];
                in.read(buf, 0, len);
                result = new String(buf, code);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
