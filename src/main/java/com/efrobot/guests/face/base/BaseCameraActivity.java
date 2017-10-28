package com.efrobot.guests.face.base;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;
import com.efrobot.library.mvp.utils.L;

import java.util.ArrayList;
import java.util.List;

import dou.helper.CameraHelper;
import dou.helper.CameraParams;
import dou.utils.DLog;
import dou.utils.DeviceUtil;
import dou.utils.ToastUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by mac on 16/7/13.
 */
public abstract class BaseCameraActivity extends BaseActivity implements CameraHelper.PreviewFrameListener {
    private SurfaceView camera_view;
    private SurfaceView draw_view;
    protected CameraHelper mCameraHelper;
    protected YMFaceTrack faceTrack;

    protected int iw = 0, ih;
    private float scale_bit;
    private boolean showFps = false;
    private List<Float> timeList = new ArrayList<Float>();
    protected boolean stop = false;
    //camera_max_width值为-1时, 找大于640分辨率为屏幕宽高等比
    private int camera_max_width = 640;

    public void initCamera(boolean isShow) {
        L.e("initCamera", "initCamera");
        camera_view = (SurfaceView) findViewById(R.id.camera_preview);
        draw_view = (SurfaceView) findViewById(R.id.pointView);
        draw_view.setZOrderOnTop(true);
        draw_view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //预设Camera参数，方便扩充
        CameraParams params = new CameraParams();
        //优先使用的camera Id,
        params.firstCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        if(isShow) {
            params.surfaceView = camera_view;
        }
        params.preview_width = camera_max_width;
//        params.preview_width = 640;
//        params.preview_height = 480;

        params.camera_ori = 0;
        params.camera_ori_front = 0;
        if (DeviceUtil.getModel().equals("Nexus 6")) {
            params.camera_ori_front = 180;
            GuestsApplication.reverse_180 = true;
        }

        params.previewFrameListener = this;
        mCameraHelper = new CameraHelper(this, params);
    }

    public synchronized void stopTrack() {

        if (faceTrack == null) {
            DLog.d("already release track");
            return;
        }
        stop = true;
        faceTrack.onRelease();
        faceTrack = null;
        DLog.d("release track success");
    }


    public synchronized void startTrack() {
        L.e("startTrack", "startTrack");
        if (faceTrack != null) {
            DLog.d("already init track");
            return;
        }

        stop = false;
        mContext = this;
        iw = 0;//重新调用initCameraMsg的开关
        faceTrack = new YMFaceTrack();

        /**此处默认初始化，initCameraMsg()处会根据设备设置自动更改设置
         *人脸识别数据库之前保存在应用目录的cache目录下，可以通过另一个初始化检测器的函数
         *public boolean initTrack(Context mContext, int orientation, int resizeScale, String db_dir)
         *通过指定保存db的目录来自定义
         **/
        //人脸抠图专用接口（一般不输出此接口），需要在initTrack之前调用 默认为1
//        faceTrack.setCropScale(2);

        //人脸抠图专用接口（一般不输出此接口），需要在initTrack之前调用 默认为10
//        faceTrack.setCropMaxCache(10);

        //人脸抠图专用（一般不输出此接口），是否需要保留原图，需要在initTrack之前调用 默认为false
//        faceTrack.setCropBg(true);

        //设置人脸检测距离，默认近距离，需要在initTrack之前调用
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);

        //license激活版本初始化
        int result = faceTrack.initTrack(this, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640,
                SenseConfig.appid, SenseConfig.appsecret);

        //普通有效期版本初始化
//        int result = faceTrack.initTrack(this, YMFaceTrack.FACE_180, YMFaceTrack.RESIZE_WIDTH_640);

        //设置人脸识别置信度，设置75，不允许修改

        if (result == 0) {
            faceTrack.setRecognitionConfidence(75);
            new ToastUtil(this).showSingletonToast("初始化检测器成功");
        } else {
            new ToastUtil(this).showSingletonToast("初始化检测器失败 result:" + result);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        L.e("onResume", "startTrack()");
        startTrack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.e("onPause", "stopTrack()");
//        stopTrack();
    }


    int camera_fps;
    int camera_count;
    long camera_long = 0;

    boolean save = false;


    private boolean isExcuteAutoFocus = false;
    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                if(msg.what == 1) {
                    startAutoFocus();
                }
        }
    };

    private void startAutoFocus() {
        if(mCamera != null) {
            isExcuteAutoFocus = true;
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera cameraA) {
                    if (success) {
                        DLog.e("myAutoFocusCallback: " + success);

                        {
                            Camera.Parameters parameters = mCamera.getParameters();
                            parameters.setPictureFormat(PixelFormat.JPEG);
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//1连续对焦
                            cameraA.setParameters(parameters);
//                            mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
                        }

                    } else {
                        DLog.e("myAutoFocusCallback: success..." + success);
                        cameraA.autoFocus(this);
                    }
                }
            });
        }
    }


    private  Camera mCamera;
    @Override
    public void onPreviewFrame(final byte[] bytes, Camera camera) {
        L.e("onPreviewFrame", "onPreviewFrame");
        if (camera_long == 0) camera_long = System.currentTimeMillis();
        camera_count++;
        if (System.currentTimeMillis() - camera_long > 1000) {
            camera_fps = camera_count;
            camera_count = 0;
            camera_long = 0;
        }

//        if(!isExcuteAutoFocus) {
//            Message msg = new Message();
//            msg.what = 1;
//            mCamera = camera;
//            mHandle.sendMessageDelayed(msg, 2000);
//        }
//        DLog.d("camera_fps  = " + camera_fps);

//        if (camera_count == 20 && !save) {
//            save = true;
//            final int iw = mCameraHelper.getPreviewSize().width;
//            final int ih = mCameraHelper.getPreviewSize().height;
//            final byte[] yuvData = new byte[bytes.length];
//            System.arraycopy(bytes, 0, yuvData, 0, bytes.length);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    CameraUtil.saveFromPreview(yuvData, "/sdcard/test.jpg",
//                            mCameraHelper.getPreviewSize().width, mCameraHelper.getPreviewSize().height);
//                }
//            }).start();
//        }

        initCameraMsg();
        if (!stop) {
            runTrack(bytes);
        }
    }


    protected abstract void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps);

    protected abstract List<YMFace> analyse(byte[] bytes, int iw, int ih);

    private void initCameraMsg() {
        if (iw == 0) {

            int surface_w = camera_view.getLayoutParams().width;
            int surface_h = camera_view.getLayoutParams().height;

            iw = mCameraHelper.getPreviewSize().width;
            ih = mCameraHelper.getPreviewSize().height;
            int orientation = 0;
            ////注意横屏竖屏问题
            DLog.d(getResources().getConfiguration().orientation + " : " + Configuration.ORIENTATION_PORTRAIT);
            if (sw < sh) {
                scale_bit = surface_w / (float) ih;
                if (mCameraHelper.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    orientation = YMFaceTrack.FACE_270;
                } else {
                    orientation = YMFaceTrack.FACE_90;
                }
            } else {
                scale_bit = surface_h / (float) ih;
                orientation = YMFaceTrack.FACE_0;

                if (GuestsApplication.reverse_180) {
                    orientation += 180;
                }
            }
            if (faceTrack == null) {
                iw = 0;
                return;
            }

            faceTrack.setOrientation(orientation);
            ViewGroup.LayoutParams params = draw_view.getLayoutParams();
            params.width = surface_w;
            params.height = surface_h;
            draw_view.requestLayout();

        }
    }


    public int getCameraId() {
        return mCameraHelper.getCameraId();
    }

    public int switchCamera() {
        int result = mCameraHelper.switchCamera();
        iw = 0;
        return result;
    }

    public void stopCamera() {
        mCameraHelper.stopCamera();
        draw_view.setVisibility(View.GONE);
        mHandle.removeCallbacksAndMessages(null);
    }

    public void stopPreview() {
        DLog.e("stopPreview");
        mCameraHelper.stopPreview();
    }

    public void startPreview() {
        DLog.e("startPreview");
        mCameraHelper.startPreview();
    }

    protected void showFps(boolean show) {
        showFps = show;
    }

    protected int getDoomW(int tar) {
        if (sw >= 1080) return tar;
        return sw * tar / 1080;
    }

    public void setCamera_max_width(int width) {
        this.camera_max_width = width;
    }

    private void runTrack(byte[] data) {
        try {
            long time = System.currentTimeMillis();
            final List<YMFace> faces = analyse(data, iw, ih);
            String str = "";
            StringBuilder fps = new StringBuilder();
            if (showFps) {
                fps.append("fps = ");
                long now = System.currentTimeMillis();
                float than = now - time;
                timeList.add(than);
                if (timeList.size() >= 20) {
                    float sum = 0;
                    for (int i = 0; i < timeList.size(); i++) {
                        sum += timeList.get(i);
                    }
                    fps.append(String.valueOf((int) (1000f * timeList.size() / sum)))
                            .append(" camera ")
                            .append(camera_fps);
                    timeList.remove(0);
                }
            }
            final String fps1 = fps.toString() + str;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawAnim(faces, draw_view, scale_bit, getCameraId(), fps1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
