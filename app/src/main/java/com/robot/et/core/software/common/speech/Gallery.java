package com.robot.et.core.software.common.speech;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.DataConfig;
import com.robot.et.common.UrlConfig;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.network.okhttp.HttpEngine;
import com.robot.et.core.software.common.view.OneImgManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.entity.PictureInfo;
import com.robot.et.util.SharedPreferencesKeys;
import com.robot.et.util.SharedPreferencesUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by houdeming on 2016/9/12.
 * 图库显示
 */
public class Gallery {
    private static final String TAG = "pic";
    private static List<PictureInfo> mInfos = new ArrayList<PictureInfo>();
    // 当前图片的位置（从0开始）
    private static int CURRENT_INDEX = 0;
    // 图片创建时间（用于获取上下张图片）
    private static String createTime;
    private static Context context;
    private static int animId;// 播放动画的id

    // 获取所有图片 第一次去请求图片时，可以不带参数
    public static void getShowPic(Context context) {
        Gallery.context = context;
        // 设置参数 第一次只传机器编号
        HttpEngine.Param[] params = new HttpEngine.Param[]{
                new HttpEngine.Param("robotNumber", SharedPreferencesUtils.getInstance().getString(SharedPreferencesKeys.ROBOT_NUM, "")),
        };
        HttpManager.getPic(params, new PicInfoCallBack() {
            @Override
            public void getPicInfos(List<PictureInfo> infos) {
                Log.i(TAG, "getPicInfos.size==" + infos.size());
                if (infos != null && infos.size() > 0) {
                    // 每次去查图片之前都把缓存的清掉，与服务器保持最新
                    if (mInfos != null && mInfos.size() > 0) {
                        mInfos.clear();
                    }
                    // 保存数据
                    mInfos.addAll(infos);
                    // 初始脚标为0
                    CURRENT_INDEX = 0;
                    animId = 0;
                    showPic(mInfos.get(CURRENT_INDEX));
                } else {
                    noPic();
                }
            }
        });
    }

    // 获取上一张图片
    public static void showLastOnePic(Context context) {
        Gallery.context = context;
        if (mInfos != null && mInfos.size() > 0) {
            // 如果当前是第一张的话，就不再去查了
            if (CURRENT_INDEX <= 0) {
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "已经是第一张了呢，看看下一张吧");
                return;
            }
            CURRENT_INDEX--;

            if (CURRENT_INDEX < mInfos.size() && CURRENT_INDEX >= 0) {
                animId = R.anim.anim_pic_last;
                showPic(mInfos.get(CURRENT_INDEX));
                return;
            }
            SpeechImpl.getInstance().startListen();

        } else {
            noPic();
        }
    }

    // 获取下一张图片
    public static void showNextPic(Context context) {
        Gallery.context = context;
        if (mInfos != null && mInfos.size() > 0) {
            CURRENT_INDEX++;
            if (CURRENT_INDEX < mInfos.size() && CURRENT_INDEX >= 0) {
                animId = R.anim.anim_pic_next;
                showPic(mInfos.get(CURRENT_INDEX));
            } else {// 如果当前是最后一张的话，去查新的
                // 设置参数
                HttpEngine.Param[] params = new HttpEngine.Param[]{
                        new HttpEngine.Param("robotNumber", SharedPreferencesUtils.getInstance().getString(SharedPreferencesKeys.ROBOT_NUM, "")),
                        new HttpEngine.Param("updateType", "down"),
                        new HttpEngine.Param("createTime", createTime)
                };
                HttpManager.getPic(params, new PicInfoCallBack() {
                    @Override
                    public void getPicInfos(List<PictureInfo> infos) {
                        if (infos != null && infos.size() > 0) {
                            mInfos.addAll(infos);
                            if (CURRENT_INDEX < mInfos.size()) {
                                animId = R.anim.anim_pic_next;
                                showPic(mInfos.get(CURRENT_INDEX));
                            }
                        } else {
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "已经没有照片了呢，再拍几张吧");
                        }
                    }
                });
            }
        } else {
            noPic();
        }
    }

    // 显示图片
    private static void showPic(PictureInfo info) {
        if (info != null) {
            final String picName = info.getPicName();
            createTime = info.getCreateTime();
            String url = getUrl(picName);
            loadPic(url, new BitmapCallBack() {
                @Override
                public void getBitmap(Bitmap bitmap) {
                    if (bitmap != null) {
                        Message msg = picHandler.obtainMessage();
                        msg.obj = bitmap;
                        picHandler.sendMessage(msg);
                    }
                }
            });
        }
        SpeechImpl.getInstance().startListen();
    }

    // 主线程处理图片
    private static Handler picHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = (Bitmap) msg.obj;
            if (bitmap != null) {
                ViewCommon.initView();
                OneImgManager.showPhoto(context, animId, bitmap);
            }
        }
    };

    // 没有照片
    private static void noPic() {
        DataConfig.isLookPhoto = false;
        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "还没有照片呢，快去拍一张吧");
    }

    // 图片信息的回调
    public interface PicInfoCallBack {
        void getPicInfos(List<PictureInfo> infos);
    }

    public interface IPicInfo {
        // 获取图片信息
        void getPicInfo(PictureInfo info);
    }

    // 下载的图片回调
    public interface BitmapCallBack {
        void getBitmap(Bitmap bitmap);
    }

    // 获取下载的连接
    private static String getUrl(String picName) {
        String robotNum = SharedPreferencesUtils.getInstance().getString(SharedPreferencesKeys.ROBOT_NUM, "");
        String urlGet = UrlConfig.LOAD_PIC_PATH + "?fileName=" + picName + "&robotNumber=" + robotNum;
        return urlGet;
    }

    // 下载网络图片
    private static void loadPic(final String url, final BitmapCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream outputStream = null;//图片下载保存到本地，需要输出流
                InputStream inputStream = null;
                try {
                    URL httpURL = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) httpURL.openConnection();
                    connection.setReadTimeout(5000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);//表示允许获取输入流
                    inputStream = connection.getInputStream();//获取输入流
                    File downloadFile = null;
                    //设置下载图片的文件名，我们以下载图片时的系统时间作为其文件名
                    String fileName = String.valueOf(System.currentTimeMillis());
                    //判断sd卡是否挂载
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File sdPath = Environment.getExternalStorageDirectory();//获取sd卡的路径
                        downloadFile = new File(sdPath, fileName);//在sd卡目录下创建一个文件，就是我们下载的图片文件
                        outputStream = new FileOutputStream(downloadFile);
                    }
                    byte[] b = new byte[20 * 1024];//创建一个20K字节大小的缓冲区
                    int len = 0;
                    if (outputStream != null) {
                /*
                 * InputStream read(byte)方法：从输入流中读取一定数量的字节，并将其存储在缓冲区数组 b中。以整数形式返回
				 * 实际读取的字节数。如果 b 的长度为 0，则不读取任何字节并返回 0；否则，尝试读取至少一个字节。
				 * 如果因为流位于文件末尾而没有可用的字节，则返回值 -1；否则，至少读取一个字节并将其存储在 b 中
				 * */
                        while ((len = inputStream.read(b)) != -1) {
                            outputStream.write(b, 0, len);
                        }
                    }
                    Bitmap bitmap = BitmapFactory.decodeFile(downloadFile.getAbsolutePath());//解析我们下载的文件
                    callBack.getBitmap(bitmap);
                } catch (MalformedURLException e) {
                    Log.i(TAG, "loadPic MalformedURLException==" + e.getMessage());
                } catch (IOException e) {
                    Log.i(TAG, "loadPic IOException==" + e.getMessage());
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            Log.i(TAG, "outputStream.close() IOException==" + e.getMessage());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.i(TAG, "inputStream.close() IOException==" + e.getMessage());
                        }
                    }
                }
            }
        }).start();
    }
}
