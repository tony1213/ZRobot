package com.robot.et.core.software.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.robot.et.R;

import java.io.IOException;
import java.util.Date;

@SuppressLint("ShowToast")
public class CustomCameraView extends FrameLayout implements SurfaceHolder.Callback, AutoFocusCallback {
    private Context context = null;
    private Camera camera = null;
    private SurfaceHolder surface_holder = null;
    private SurfaceView surface_camera = null;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private PreviewFrameLayout frameLayout = null;
    private ICamera iCamera;

    //  NONE：无 FOCUSING：正在聚焦. FOCUSED:聚焦成功 FOCUSFAIL：聚焦失败
    private enum MODE {
        NONE, FOCUSING, FOCUSED, FOCUSFAIL
    }

    private MODE mode = MODE.NONE;// 默认模式

    public CustomCameraView(Context context) {
        super(context);
    }

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.camera_preview_frame, this);
        frameLayout = (PreviewFrameLayout) findViewById(R.id.frame_layout);
        surface_camera = (SurfaceView) findViewById(R.id.camera_preview);
        surface_holder = surface_camera.getHolder();
        surface_holder.addCallback(this);
    }

    public void setCameraCallBack(ICamera iCamera) {
        this.iCamera = iCamera;
    }

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    ShutterCallback myShutterCallback = new ShutterCallback() {
        //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
        public void onShutter() {
            Log.i("camera", "myShutterCallback  onShutter: ");
            if (iCamera != null) {
                iCamera.onShutter();
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (checkCameraHardware()) {
            camera = getCameraInstance();
        }
        try {
            camera.setPreviewDisplay(surface_holder);
        } catch (IOException e) {
            Log.i("camera", "surfaceCreated IOException==" + e.getMessage());
        }
        updateCameraParameters();
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null && holder != null) {
            camera.stopPreview();
            camera.release();
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        viewWidth = MeasureSpec.getSize(widthSpec);
        viewHeight = MeasureSpec.getSize(heightSpec);
        super.onMeasure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    private boolean checkCameraHardware() {
        if (context != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras(); // get cameras number
            Log.i("camera", "getCameraInstance cameraCount==" + cameraCount);
            int cameraId = cameraInfo.facing;
            Log.i("camera", "getCameraInstance cameraId==" + cameraId);
            // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
            if (cameraId == 0) {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } else {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
        } catch (Exception e) {
            // 摄像头打开失败
            Log.i("camera", "getCameraInstance Exception==" + e.getMessage());
        }
        return c;
    }

    private void updateCameraParameters() {
        if (camera != null) {
            Camera.Parameters p = camera.getParameters();
            long time = new Date().getTime();
            p.setGpsTimestamp(time);
            Size previewSize = findBestPreviewSize(p);
//      p.setPreviewSize(previewSize.width, previewSize.height);
//      p.setPictureSize(previewSize.width, previewSize.height);
            frameLayout.setAspectRatio((double) previewSize.width / previewSize.height);
            if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                camera.setDisplayOrientation(90);
                p.setRotation(90);
            }
            //p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//加上可以自动聚焦
            camera.setParameters(p);
            // camera.cancelAutoFocus();// 一定要加上这句，才可以连续聚集

        }
    }

    /**
     * 找到最合适的显示分辨率 （防止预览图像变形）
     *
     * @param parameters
     * @return
     */
    private Size findBestPreviewSize(Camera.Parameters parameters) {
        //系统支持的所有预览分辨率
        String previewSizeValueString = null;
        previewSizeValueString = parameters.get("preview-size-values");
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }
        if (previewSizeValueString == null) { // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
            return camera.new Size(getScreenWH().widthPixels, getScreenWH().heightPixels);
        }
        float bestX = 0;
        float bestY = 0;

        float tmpRadio = 0;
        float viewRadio = 0;

        if (viewWidth != 0 && viewHeight != 0) {
            viewRadio = Math.min((float) viewWidth, (float) viewHeight) / Math.max((float) viewWidth, (float) viewHeight);
        }
        // System.out.println("CustomCameraView previewSizeValueString COMMA_PATTERN = "
        // + previewSizeValueString);

        String[] COMMA_PATTERN = previewSizeValueString.split(",");
        for (String prewsizeString : COMMA_PATTERN) {
            prewsizeString = prewsizeString.trim();

            int dimPosition = prewsizeString.indexOf('x');
            if (dimPosition == -1) {
                continue;
            }

            float newX = 0;
            float newY = 0;

            try {
                newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
                newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
            } catch (NumberFormatException e) {
                continue;
            }

            float radio = Math.min(newX, newY) / Math.max(newX, newY);
            if (tmpRadio == 0) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            } else if (tmpRadio != 0 && (Math.abs(radio - viewRadio)) < (Math.abs(tmpRadio - viewRadio))) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            }
        }

        if (bestX > 0 && bestY > 0) {
            // System.out.println("CustomCameraView previewSizeValueString bestX = " +
            // bestX + ", bestY = " + bestY);
            return camera.new Size((int) bestX, (int) bestY);
        }
        return null;
    }

    protected DisplayMetrics getScreenWH() {
        DisplayMetrics dMetrics = new DisplayMetrics();
        dMetrics = this.getResources().getDisplayMetrics();
        return dMetrics;
    }

    // 拍照
    public void takePicture() {
        if (mode == MODE.FOCUSFAIL || mode == MODE.FOCUSING) {
            return;
        }

        if (camera != null) {
            camera.takePicture(myShutterCallback, null, new PictureCallback() {

                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (iCamera != null) {
                        iCamera.onTakePictureInfo(data);
                    }
                }
            });
            mode = MODE.NONE;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAutoFocus(boolean success, Camera _camera) {
        if (success) {
            Log.i("camera", "onAutoFocus");
            mode = MODE.FOCUSED;
            if (iCamera != null) {
                iCamera.onAutoFocus();
            }
        } else {
            mode = MODE.FOCUSFAIL;
        }
    }
}
