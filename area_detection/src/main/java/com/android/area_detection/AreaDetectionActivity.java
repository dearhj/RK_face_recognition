package com.android.area_detection;


//import android.annotation.SuppressLint;
import static com.android.area_detection.SoundPlayUtils.play;
import static com.android.area_detection.SoundPlayUtils.release;
import static com.android.area_detection.SoundPlayUtils.setupPlayer;

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
//import android.os.Handler;
//import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockchip.iva.RockIva;
import com.rockchip.iva.RockIvaCallback;
import com.rockchip.iva.RockIvaImage;
import com.android.area_detection.utils.ImageBufferQueue;
import com.android.area_detection.utils.RkCameraUtils;

//import java.text.DecimalFormat;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AreaDetectionActivity extends AppCompatActivity implements RkCameraUtils.CameraPreviewCallback {
//    private static final int HANDLE_SHOW_FPS = 1;
//    private static final int HANDLE_SHOW_RESULT = 2;


    private RkCameraUtils rgbUtil;
    private SurfaceView mSurfaceView = null;
//    private TextView mFpsNum1;
//    private TextView mFpsNum2;
//    private TextView mFpsNum3;
//    private TextView mFpsNum4;
    private int height = 0;
    private int width = 0;

    private IvaAreaDetectionManager ivaAreaDetectionManager = null;
    private ImageBufferQueue bufferQueue = null;
    private RockIva rockiva = null;
    private boolean firstPreview = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // hiddend navigation
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_area_detection);

        initView();

        initCamera();
        ivaAreaDetectionManager = new IvaAreaDetectionManager(getApplicationContext());
        ivaAreaDetectionManager.initForAreaDetection();
        ivaAreaDetectionManager.setCallback(mIvaCallback);
        bufferQueue = ivaAreaDetectionManager.getBufferQueue();
        rockiva = ivaAreaDetectionManager.getIva();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        ivaAreaDetectionManager.release();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AreaDetectionActivity.this);
        builder.setTitle("没有发现相机");
        builder.setPositiveButton(android.R.string.ok,
                (DialogInterface.OnClickListener) (dialog, which) -> finish());
        builder.show();
    }

    private void initView() {
//        mFpsNum1 = findViewById(R.id.fps_num1);
//        mFpsNum2 = findViewById(R.id.fps_num2);
//        mFpsNum3 = findViewById(R.id.fps_num3);
//        mFpsNum4 = findViewById(R.id.fps_num4);
        mSurfaceView = findViewById(R.id.surfaceViewCamera1);
        mTrackResultView = findViewById(R.id.canvasView);
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

//    private void updateMainUI(int what, Object data) {
//        Message msg = mHandler.obtainMessage();
//        msg.what = what;
//        msg.obj = data;
//        mHandler.sendMessage(msg);
//    }

//    @SuppressLint("HandlerLeak")
//    private final Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            if (msg.what == HANDLE_SHOW_FPS) {
//                float fps = (float) msg.obj;
//                DecimalFormat decimalFormat = new DecimalFormat("00.00");
//                String fpsStr = decimalFormat.format(fps);
//                mFpsNum1.setText(String.valueOf(fpsStr.charAt(0)));
//                mFpsNum2.setText(String.valueOf(fpsStr.charAt(1)));
//                mFpsNum3.setText(String.valueOf(fpsStr.charAt(3)));
//                mFpsNum4.setText(String.valueOf(fpsStr.charAt(4)));
//            } else if (msg.what == HANDLE_SHOW_RESULT) {
//                showResults();
//            }
//        }
//    };

    int frameId = 0;
    Rect drawRect = new Rect();

    @Override
    public void onCameraPreview(byte[] data, Camera camera) {
        if (firstPreview) {
            setupPlayer();
            SoundPlayUtils.init(this);
            firstPreview = false;
            width = mTrackResultView.getWidth();
            height = mTrackResultView.getHeight();

            drawRect.top = (int) Math.round(height * 0.2);
            drawRect.left = (int) Math.round(width * 0.2);
            drawRect.bottom = (int) Math.round(height * 0.8);
            drawRect.right = (int) Math.round(width * 0.8);
            System.out.println("这里的数据为 " + width + "    " + height);
            initPaint();
            areaDirectPaint(drawRect);
        }
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

//    int count = 0;
//    long oldTime = System.currentTimeMillis();
//    long currentTime;
//
//    void updateFps() {
//        if (++count >= 30) {
//            currentTime = System.currentTimeMillis();
//            float fps = count * 1000.f / (currentTime - oldTime);
////            Log.d(Configs.TAG, "current fps = " + fps);
//            oldTime = currentTime;
//            count = 0;
//            updateMainUI(HANDLE_SHOW_FPS, fps);
//        }
//    }

    int logNum = 0;
    private AreaDetectionResult mLastAreaDetectionResult = null;

    RockIvaCallback mIvaCallback = new RockIvaCallback() {
        @Override
        public void onResultCallback(String result, int execureState) {
            logNum += 1;
            if (logNum > 7) {
                logNum = 0;
                Log.d(Configs.TAG, "" + result + " execureState = " + execureState);
            }
            mLastAreaDetectionResult = new AreaDetectionResult(result);
            showResults();
        }

        @Override
        public void onReleaseCallback(List<RockIvaImage> images) {
            for (RockIvaImage image : images) {
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

    private void initPaint() {
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
        }
    }

    private void areaDirectPaint(Rect drawRect) {
        mTrackResultCanvas.drawRect(drawRect, mTrackResultPaint);
        mTrackResultView.setScaleType(ImageView.ScaleType.FIT_XY);
        mTrackResultView.setImageBitmap(mTrackResultBitmap);
    }

    private void showResults() {
        mPorterDuffXfermodeClear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mPorterDuffXfermodeSRC = new PorterDuffXfermode(PorterDuff.Mode.SRC);

        // clear canvas
        mTrackResultPaint.setXfermode(mPorterDuffXfermodeClear);
        mTrackResultCanvas.drawPaint(mTrackResultPaint);
        mTrackResultPaint.setXfermode(mPorterDuffXfermodeSRC);

        mTrackResultPaint.setColor(Color.YELLOW);
        mTrackResultCanvas.drawRect(drawRect, mTrackResultPaint);

        if (mLastAreaDetectionResult != null) {
            AreaDetectionResult areaDetectionResult = mLastAreaDetectionResult;
            mTrackResultPaint.setColor(Color.GREEN);
            if (areaDetectionResult.objectInfoList != null) {
                for (ObjectInfo objectInfo : areaDetectionResult.objectInfoList) {
                    if(objectInfo.triggerRules != 0) mTrackResultPaint.setColor(Color.RED);
                    else mTrackResultPaint.setColor(Color.GREEN);
                    Rect drawObjectRect = RockIva.convertRectRatioToPixel(width, height, new Rect(objectInfo.left, objectInfo.top, objectInfo.right, objectInfo.bottom), RockIvaImage.TransformMode.FLIP_H);
                    mTrackResultCanvas.drawText(resultStr(objectInfo.triggerRules), drawObjectRect.left, drawObjectRect.top - 20, mTrackResultTextPaint);
                    mTrackResultCanvas.drawRect(drawObjectRect, mTrackResultPaint);
                    sound(objectInfo.triggerRules);
                }
            }
        }
        mTrackResultView.setScaleType(ImageView.ScaleType.FIT_XY);
        mTrackResultView.setImageBitmap(mTrackResultBitmap);
    }

    boolean isPlay = false;
    Handler handler = new Handler();
    private void sound(int result){
        if(result != 0 && !isPlay) {
            play(this);
            isPlay = true;
            handler.postDelayed(() -> isPlay = false, 500);
        }
    }

    private String resultStr(int str){
        if(str != 0) return "入侵者！";
        else return "";
    }
}
