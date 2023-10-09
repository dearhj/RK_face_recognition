package com.rockchip.iva.face;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.rockchip.iva.RockIva;
import com.rockchip.iva.RockIvaCallback;
import com.rockchip.iva.RockIvaImage;
import com.rockchip.iva.face.utils.ImageBufferQueue;
import com.rockchip.iva.face.utils.RkCameraUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FacePoseActivity extends AppCompatActivity implements RkCameraUtils.CameraPreviewCallback {

    private static final int HANDLE_FACE_SEARCH = 1;
    private static final int HANDLE_SHOW_FPS = 1;
    private static final int HANDLE_SHOW_RESULT = 2;


    private ArrayList<RockIvaHeadInfo> mTrackedFaceArray;

    private RkCameraUtils rgbUtil;
    private SurfaceView mSurfaceView = null;
    private TextView mFpsNum1;
    private TextView mFpsNum2;
    private TextView mFpsNum3;
    private TextView mFpsNum4;

    private IvaFaceManager ivaFaceManager = null;
    private ImageBufferQueue bufferQueue = null;
    private RockIva rockiva = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // hiddend navigation
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_face_recognition);

        initView();

        initCamera();
        ivaFaceManager = new IvaFaceManager(getApplicationContext());
        ivaFaceManager.initForFacePose();
        ivaFaceManager.setCallback(mIvaCallback);
        bufferQueue = ivaFaceManager.getBufferQueue();
        rockiva = ivaFaceManager.getIva();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ivaFaceManager.release();
        bufferQueue = null;
        rockiva = null;
    }

    private void initCamera() {
        rgbUtil = new RkCameraUtils();
        boolean res = rgbUtil.initCamera(Configs.CAMERA_ID, Configs.CAMERA_IMAGE_WIDTH, Configs.CAMERA_IMAGE_HEIGHT,
                0, 0, 0, false);
        if (!res) {
            popupCameraAlertDialog();
        }
        rgbUtil.setCameraCallback(this);
    }

    private void popupCameraAlertDialog() {
        // Popup setting dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(FacePoseActivity.this);
        builder.setTitle(R.string.settings_no_camera);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }

    private void initView() {
        mFpsNum1 = findViewById(R.id.fps_num1);
        mFpsNum2 = findViewById(R.id.fps_num2);
        mFpsNum3 = findViewById(R.id.fps_num3);
        mFpsNum4 = findViewById(R.id.fps_num4);
        mSurfaceView = findViewById(R.id.surfaceViewCamera1);
        mTrackResultView = findViewById(R.id.canvasView);
    }

    private void popupAuthAlertDialog() {
        // Popup setting dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(FacePoseActivity.this);
        builder.setTitle("错误未授权，请先申请授权");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        rgbUtil.stopCamera();
        rgbUtil.destroyPreviewView();
    }

    @Override
    public void onResume() {
        super.onResume();
        rgbUtil.createPreviewView(mSurfaceView);
    }

    private void updateMainUI(int what, Object data) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = data;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == HANDLE_SHOW_FPS) {
                float fps = (float) msg.obj;

                DecimalFormat decimalFormat = new DecimalFormat("00.00");
                String fpsStr = decimalFormat.format(fps);
                mFpsNum1.setText(String.valueOf(fpsStr.charAt(0)));
                mFpsNum2.setText(String.valueOf(fpsStr.charAt(1)));
                mFpsNum3.setText(String.valueOf(fpsStr.charAt(3)));
                mFpsNum4.setText(String.valueOf(fpsStr.charAt(4)));
            } else if (msg.what == HANDLE_SHOW_RESULT) {
                showResults();
            }
        }
    };


    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_FACE_SEARCH: {

                } break;
            }
            return true;
        }
    };

    int frameId = 0;

    @Override
    public void onCameraPreview(byte[] data, Camera camera) {
        frameId += 1;
        if (bufferQueue == null || rockiva == null) {
            return;
        }
        ImageBufferQueue.ImageBuffer imageBuffer = bufferQueue.getFreeBuffer();
        if (imageBuffer != null) {
            imageBuffer.mImage.setImageData(data);
            imageBuffer.mImage.frameId = frameId;
            rockiva.pushFrame(imageBuffer.mImage);
            bufferQueue.postBuffer(imageBuffer);
        }
    }

    int count = 0;
    long oldTime = System.currentTimeMillis();
    long currentTime;

    void updateFps() {
        if (++count >= 30) {
            currentTime = System.currentTimeMillis();
            float fps = count * 1000.f / (currentTime - oldTime);
//                    Log.d(TAG, "current fps = " + fps);
            oldTime = currentTime;
            count = 0;
            updateMainUI(HANDLE_SHOW_FPS, fps);
        }
    }

    RockIvaCallback mIvaCallback = new RockIvaCallback() {
        @Override
        public void onResultCallback(String result, int execureState) {
            Log.d(Configs.TAG, ""+result);

            JSONObject jobj = JSONObject.parseObject(result);
            ArrayList<RockIvaHeadInfo> faceList = new ArrayList<>();
            if (JSONPath.contains(jobj, "$.facePostureResult")) {
                updateFps();
                int num = (int)JSONPath.eval(jobj, "$.facePostureResult.objNum");
                for (int i = 0; i < num; i++) {
                    Object faceInfoJobj =  JSONPath.eval(jobj, String.format("$.facePostureResult.headInfo[%d]", i));
                    RockIvaHeadInfo headInfo = RockIvaHeadInfo.parse(faceInfoJobj.toString());
                    faceList.add(headInfo);
                }
                mTrackedFaceArray = faceList;
                updateMainUI(HANDLE_SHOW_RESULT, null);
            }
        }

        @Override
        public void onReleaseCallback(List<RockIvaImage> images) {
            for (RockIvaImage image : images) {
//                Log.d(Configs.TAG, "release image " + image.toString());
                bufferQueue.releaseBuffer(image);
            }
        }
    };

    private ImageView mTrackResultView;
    private Bitmap mTrackResultBitmap = null;
    private Canvas mTrackResultCanvas = null;
    private Paint mTrackResultPaint = null;
    private Paint mTrackResultTextPaint = null;

    private PorterDuffXfermode mPorterDuffXfermodeClear;
    private PorterDuffXfermode mPorterDuffXfermodeSRC;

    public static int sp2px(float spValue) {
        Resources r = Resources.getSystem();
        final float scale = r.getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }

    private void showResults() {

        int width = mTrackResultView.getWidth();
        int height = mTrackResultView.getHeight();

        if (mTrackResultBitmap == null) {

            mTrackResultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mTrackResultCanvas = new Canvas(mTrackResultBitmap);

            //用于画线
            mTrackResultPaint = new Paint();
            mTrackResultPaint.setColor(Color.YELLOW);
            mTrackResultPaint.setStrokeJoin(Paint.Join.ROUND);
            mTrackResultPaint.setStrokeCap(Paint.Cap.ROUND);
            mTrackResultPaint.setStrokeWidth(3);
            mTrackResultPaint.setStyle(Paint.Style.STROKE);
            mTrackResultPaint.setTextAlign(Paint.Align.LEFT);
            mTrackResultPaint.setTextSize(sp2px(10));
            mTrackResultPaint.setTypeface(Typeface.SANS_SERIF);
            mTrackResultPaint.setFakeBoldText(false);

            //用于文字
            mTrackResultTextPaint = new Paint();
            mTrackResultTextPaint.setColor(0xff06ebff);
            mTrackResultTextPaint.setStrokeWidth(2);
            mTrackResultTextPaint.setTextAlign(Paint.Align.LEFT);
            mTrackResultTextPaint.setTextSize(sp2px(20));
            mTrackResultTextPaint.setTypeface(Typeface.SANS_SERIF);
            mTrackResultTextPaint.setFakeBoldText(false);

            mPorterDuffXfermodeClear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
            mPorterDuffXfermodeSRC = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        }

        // clear canvas
        mTrackResultPaint.setXfermode(mPorterDuffXfermodeClear);
        mTrackResultCanvas.drawPaint(mTrackResultPaint);
        mTrackResultPaint.setXfermode(mPorterDuffXfermodeSRC);

        //detect result
        ArrayList<RockIvaHeadInfo> faceList = mTrackedFaceArray;
        if (faceList != null && faceList.size() > 0) {
            for (int n = 0; n < faceList.size(); n++) {
                RockIvaHeadInfo face = faceList.get(n);
                Rect drawRect = RockIva.convertRectRatioToPixel(width, height, face.headRect, RockIvaImage.TransformMode.FLIP_H);
                mTrackResultCanvas.drawRect(drawRect, mTrackResultPaint);
                String drawStr = "";
                drawStr += "id:" + face.objId;
                drawStr += " - " + face.facePoseState.toString();
                mTrackResultCanvas.drawText(drawStr, drawRect.left,
                        drawRect.top-20, mTrackResultTextPaint);
            }
        }
        mTrackResultView.setScaleType(ImageView.ScaleType.FIT_XY);
        mTrackResultView.setImageBitmap(mTrackResultBitmap);
    }
}
