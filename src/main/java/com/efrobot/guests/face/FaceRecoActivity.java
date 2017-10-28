package com.efrobot.guests.face;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.SimpleArrayMap;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;
import com.efrobot.guests.face.base.BaseCameraActivity;
import com.efrobot.guests.face.model.User;
import com.efrobot.guests.face.util.DrawUtil;
import com.efrobot.library.mvp.utils.L;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.FileUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.net.NetFaceTrack;

public class FaceRecoActivity extends BaseCameraActivity {


    private SimpleArrayMap<Integer, YMFace> trackingMap;

    boolean threadBusy = false;
    boolean saveImage = false;

    private Thread thread;

    boolean pause = false;
    NetFaceTrack netFaceTrack;
    String ip;
    boolean net = false;

    TextView faceId;

    private boolean isShowSurfaceView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_reco_2);
        setCamera_max_width(-1);

        isShowSurfaceView = getIntent().getBooleanExtra("isShowSurfaceView", true);

        if(!isShowSurfaceView) {
            GuestsApplication.from(this).faceRecoActivity = this;
        }

        if (net) {
            initView();
            initCamera(isShowSurfaceView);
            showFps(true);
            initView();
            netFaceTrack = NetFaceTrack.getInstance("http://114.80.100.145:8080",
                    "04b8bcfb39b069e3963b26de0319d810", "ab465c795d404fc4f23aed9e938f5e0f3f9f5edf");
        } else {
            initView();
            initCamera(isShowSurfaceView);
            showFps(true);
            initView();
        }
    }

    void inputIp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        final EditText et = new EditText(mContext);
        et.setGravity(Gravity.CENTER);
        et.setHintTextColor(0xffc6c6c6);
        builder.setTitle(R.string.dalog_notice).setView(et)
                .setMessage("请输入本地Ip")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ip = et.getText().toString();
                        if (!StringUtils.isEmpty(ip.trim())) {
                        } else {
                            inputIp();
                            return;
                        }

                        FileUtil.writeFile("/sdcard/rsnet_config.txt", ip);

                        netFaceTrack = NetFaceTrack.getInstance(ip, "12345", "abcdefg");
                        initView();
                        initCamera(true);
                        showFps(true);
                        initView();

                    }
                });
        builder.create().show();
    }

    public void initView() {
        TextView title = (TextView) findViewById(R.id.page_title);
        Button page_right = (Button) findViewById(R.id.page_right);
        title.setText(R.string.start_1);
        page_right.setText(R.string.unlock_insert_face);
        page_right.setVisibility(View.GONE);

        faceId = (TextView) findViewById(R.id.face_id);

    }


    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        if(isShowSurfaceView) {
            DrawUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, false);
        }
        L.e("drawAnim", "正在识别中 cameraId:" + cameraId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (trackingMap != null && trackingMap.size() != 0) {
            trackingMap.clear();
        }
        trackingMap = new SimpleArrayMap<Integer, YMFace>();
        DrawUtil.updateDataSource();

    }

    int frame = 0;

    boolean isHasFaceId = false;

    @Override
    protected List<YMFace> analyse(final byte[] bytes, final int iw, final int ih) {

        if (pause) return null;
        if (faceTrack == null) return null;
        frame++;
//        final List<YMFace> faces = new ArrayList<>();
//        YMFace face1 = faceTrack.track(bytes, iw, ih);
//        if (face1 != null) faces.add(face1);
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);


        if (faces != null && faces.size() > 0) {
            DLog.e("threadBusy = " + threadBusy + "  stop = " + stop + "  frame = " + frame);
            if (!threadBusy && !stop && frame >= 10) {
                frame = 0;

                if (trackingMap.size() > 50) trackingMap.clear();
                //只对最大人脸框进行识别
                int maxIndex = 0;
                for (int i = 1; i < faces.size(); i++) {
                    if (faces.get(maxIndex).getRect()[2] <= faces.get(i).getRect()[2]) {
                        maxIndex = i;
                    }
                }

                final YMFace ymFace = faces.get(maxIndex);
                final int anaIndex = maxIndex;
                final int trackId = ymFace.getTrackId();
                final float[] rect = ymFace.getRect();
                final float[] headposes = ymFace.getHeadpose();

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            threadBusy = true;
                            final byte[] yuvData = new byte[bytes.length];
                            System.arraycopy(bytes, 0, yuvData, 0, bytes.length);

                            boolean next = true;

                            if ((Math.abs(headposes[0]) > 30
                                    || Math.abs(headposes[1]) > 30
                                    || Math.abs(headposes[2]) > 30)) {
                                //角度不佳不再识别
                                next = false;
                            }

                            if (next && !net) {
                                for (int i = 0; i < faces.size(); i++) {
                                    final YMFace ymFace = faces.get(i);
                                    final int trackId = ymFace.getTrackId();
                                    if (!trackingMap.containsKey(trackId) ||
                                            trackingMap.get(trackId).getPersonId() <= 0 ||
                                            trackingMap.get(trackId).getPersonId() > 0) {
                                        long time = System.currentTimeMillis();
                                        int identifyPerson = faceTrack.identifyPerson(i);
                                        int confidence = faceTrack.getRecognitionConfidence();
                                        DLog.d("identify end " + identifyPerson + " time :" + (System.currentTimeMillis() - time) + " con = " + confidence);
                                        if(identifyPerson != 11001) {
                                            isHasFaceId = true;
                                            Message msg = new Message();
                                            msg.what = 108;
                                            msg.obj = identifyPerson;
                                            if(GuestsApplication.from(FaceRecoActivity.this).ultrasonicService != null) {
                                                if(GuestsApplication.from(FaceRecoActivity.this).ultrasonicService.mHandle != null) {
                                                    GuestsApplication.from(FaceRecoActivity.this).ultrasonicService.mHandle.sendMessage(msg);
                                                }

                                            }
//                                            handle.sendMessage(msg);
                                        } else {
                                            if(isHasFaceId) {
                                                isHasFaceId = false;
                                                handle.sendEmptyMessageDelayed(1, 1000);
                                            }
                                        }

                                        saveImageFromCamera(identifyPerson, yuvData);
                                        ymFace.setIdentifiedPerson(identifyPerson, confidence);
                                        trackingMap.put(trackId, ymFace);
                                    }
                                }
                                next = false;
                                //使用本地就不再使用云端,可直接删除云端部分
                            }

                            //TODO for 云端api
                            if (next && !pause) {

                                int width_add = (int) (rect[2] / 4);

                                while (rect[0] - width_add < 0 || rect[1] - width_add < 0 ||
                                        rect[0] + rect[2] + width_add > iw ||
                                        rect[1] + rect[3] + width_add > ih) {
                                    width_add--;
                                    if (width_add == 0) break;
                                }

                                String name = null;
                                File bitmapFile = new File("/sdcard/cachebitmap.jpg");
                                Bitmap image = BitmapUtil.getBitmapFromYuvByte(yuvData, iw, ih);

                                Matrix matrix = new Matrix();
                                if ((int) rect[2] + 2 * width_add > 300) {
                                    float bit = 300 / (rect[2] + 2 * width_add);
                                    matrix.postScale(bit, bit);
                                }
                                Bitmap head_bmp = Bitmap.createBitmap(image, (int) rect[0] - width_add, (int) rect[1] - width_add,
                                        (int) rect[2] + 2 * width_add, (int) rect[3] + 2 * width_add, matrix, true);

                                BitmapUtil.saveBitmap(head_bmp, bitmapFile);

                                String result = netFaceTrack.faceDetaction(bitmapFile, "", null);
                                String face_id = "";
                                face_id = new JSONObject(result).getJSONArray("faces")
                                        .getJSONObject(0).getString("face_id");

                                result = netFaceTrack.faceIdentification(face_id, "12dc1d1962f9818a2a9959cd7fd3c7ef", null);
                                DLog.d(result);

                                name = new JSONObject(result).
                                        getJSONArray("candidates").getJSONObject(0).getString("name");
                                DLog.d("name = " + name);
                                if (!StringUtils.isEmpty(name)) {
                                    pause = true;
                                    final String last_name = name;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (StringUtils.isEmpty(last_name)) {
                                            } else {
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                builder.setCancelable(false);
                                                builder.setTitle(R.string.dalog_notice)
                                                        .setMessage("你好：" + last_name)
                                                        .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                pause = false;
                                                            }
                                                        });
                                                builder.create().show();
                                            }
                                        }
                                    });
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            threadBusy = false;
                        }
                    }
                });
                thread.start();
            }

            for (int i = 0; i < faces.size(); i++) {
                final YMFace ymFace = faces.get(i);
                final int trackId = ymFace.getTrackId();
                if (trackingMap.containsKey(trackId)) {
                    YMFace face = trackingMap.get(trackId);
                    ymFace.setIdentifiedPerson(face.getPersonId(), face.getConfidence());
                }
            }
        }
        return faces;
    }

    private Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0) {
                String thId = String.valueOf(msg.obj);
                DLog.d("检测到该人脸:" + thId);
                if (DrawUtil.userList != null && DrawUtil.userList.size() > 0) {
                    for (int j = 0; j <DrawUtil.userList.size() ; j++) {
                        User user = DrawUtil.userList.get(j);
                        String userId = user.getPersonId();
                        DLog.d("userId = " + userId);
                        if(userId.equals(thId)) {
                            faceId.setText(user.getName());
                        }
                    }

                } else {
                    DLog.d("人脸库为空");
                }
            } else if(msg.what == 1) {
                faceId.setText("");
            }
        }
    };

    public void topClick(View view) {
        switch (view.getId()) {
            case R.id.page_cancle:
                stopCamera();
                finish();
                break;
            case R.id.page_right://TODO 录入人脸
                stopCamera();
                trackingMap.clear();
                startActivity(new Intent(this, RegisterImageCameraActivity.class));
                break;
        }
    }

    @Override
    protected void onPause() {
        //等待线程结束再执行super中释放检测器
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        stopCamera();
        finish();
    }

    public void saveImageFromCamera(int personId, byte[] yuvBytes) {
        if (!saveImage) return;
        File tmpFile = new File("/sdcard/img/fr/out");
        if (!tmpFile.exists()) tmpFile.mkdirs();
        tmpFile = new File("/sdcard/img/fr/out" + "/img_" + System.currentTimeMillis() + "_" + personId + ".jpg");
        saveImage(tmpFile, yuvBytes);
    }

    private void saveImage(File file, byte[] yuvBytes) {

        FileOutputStream fos = null;
        try {
            YuvImage image = new YuvImage(yuvBytes, ImageFormat.NV21, iw, ih, null);
            fos = new FileOutputStream(file);
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopSelf() {
        stopCamera();
        finish();
    }

}
