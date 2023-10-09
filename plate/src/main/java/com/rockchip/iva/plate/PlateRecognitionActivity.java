package com.rockchip.iva.plate;

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
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.rockchip.iva.plate.R;
import com.rockchip.iva.RockIva;
import com.rockchip.iva.RockIvaCallback;
import com.rockchip.iva.RockIvaImage;
import com.rockchip.iva.plate.utils.RkCameraUtils;
import com.rockchip.iva.plate.utils.ImageBufferQueue;

import java.util.List;

public class PlateRecognitionActivity extends AppCompatActivity implements RkCameraUtils.CameraPreviewCallback {

    private static final int HANDLE_SHOW_RESULT = 1;

//    private SparseArray<FaceResult> mTrackedFaceArray;
    private PlateResult mLastPlateResult = null;

    private RkCameraUtils rgbUtil;
    private SurfaceView mSurfaceView = null;

    private IvaPlateManager ivaFaceManager = null;
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

        setContentView(R.layout.activity_plate_recognition);

        initView();

//        mTrackedFaceArray = new SparseArray<>();

        ivaFaceManager = new IvaPlateManager(getApplicationContext());
        ivaFaceManager.initForRecog();
        ivaFaceManager.setCallback(mIvaCallback);
        bufferQueue = ivaFaceManager.getBufferQueue();
        rockiva = ivaFaceManager.getIva();

        initCamera();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(PlateRecognitionActivity.this);
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
        mSurfaceView = findViewById(R.id.surfaceViewCamera1);
        mTrackResultView = (ImageView) findViewById(R.id.canvasView);
    }

    private void popupAuthAlertDialog() {
        // Popup setting dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(PlateRecognitionActivity.this);
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

    private void updateMainUI() {
        Message msg = mHandler.obtainMessage();
        msg.what = HANDLE_SHOW_RESULT;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == HANDLE_SHOW_RESULT) {
                showResults();
            }
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

    RockIvaCallback mIvaCallback = new RockIvaCallback() {
        @Override
        public void onResultCallback(String result, int execureState) {
//            Log.d(Configs.TAG, ""+result);
            PlateResult plateResult = new PlateResult(result);
            mLastPlateResult = plateResult;
            updateMainUI();
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
            mTrackResultPaint = new android.graphics.Paint();
            mTrackResultPaint.setColor(Color.YELLOW);
            mTrackResultPaint.setStrokeJoin(android.graphics.Paint.Join.ROUND);
            mTrackResultPaint.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            mTrackResultPaint.setStrokeWidth(3);
            mTrackResultPaint.setStyle(android.graphics.Paint.Style.STROKE);
            mTrackResultPaint.setTextAlign(android.graphics.Paint.Align.LEFT);
            mTrackResultPaint.setTextSize(sp2px(10));
            mTrackResultPaint.setTypeface(Typeface.SANS_SERIF);
            mTrackResultPaint.setFakeBoldText(false);

            //用于文字
            mTrackResultTextPaint = new android.graphics.Paint();
            mTrackResultTextPaint.setColor(0xff06ebff);
            mTrackResultTextPaint.setStrokeWidth(2);
            mTrackResultTextPaint.setTextAlign(android.graphics.Paint.Align.LEFT);
            mTrackResultTextPaint.setTextSize(sp2px(20));
            mTrackResultTextPaint.setTypeface(Typeface.SANS_SERIF);
            mTrackResultTextPaint.setFakeBoldText(false);

            mPorterDuffXfermodeClear = new android.graphics.PorterDuffXfermode(PorterDuff.Mode.CLEAR);
            mPorterDuffXfermodeSRC = new android.graphics.PorterDuffXfermode(PorterDuff.Mode.SRC);
        }

        // clear canvas
        mTrackResultPaint.setXfermode(mPorterDuffXfermodeClear);
        mTrackResultCanvas.drawPaint(mTrackResultPaint);
        mTrackResultPaint.setXfermode(mPorterDuffXfermodeSRC);

        //detect result
        if (mLastPlateResult != null) {
            PlateResult plateResult = mLastPlateResult;
            if (plateResult.mPlateBox != null ) {
                if (plateResult.mVehicleBox != null) {
                    Rect drawVehicleRect = RockIva.convertRectRatioToPixel(width, height, plateResult.mVehicleBox, RockIvaImage.TransformMode.FLIP_H);
                    mTrackResultCanvas.drawRect(drawVehicleRect, mTrackResultPaint);
                }
                Rect drawPlateRect = RockIva.convertRectRatioToPixel(width, height, plateResult.mPlateBox, RockIvaImage.TransformMode.FLIP_H);
                mTrackResultCanvas.drawRect(drawPlateRect, mTrackResultPaint);
                if (plateResult.mPlateStr != null) {
                    String drawStr = "";
                    drawStr += plateResult.mPlateStr;
                    mTrackResultCanvas.drawText(drawStr, drawPlateRect.left, drawPlateRect.top-20, mTrackResultTextPaint);
                }
            }
        }
        mTrackResultView.setScaleType(ImageView.ScaleType.FIT_XY);
        mTrackResultView.setImageBitmap(mTrackResultBitmap);
    }

//    private void updateCurFaceList(List<RockIvaFaceInfo> faceInfos) {
//
//        SparseArray<FaceResult> newFaceList = new SparseArray<>();
//        for (RockIvaFaceInfo faceInfo : faceInfos) {
//            int trackId = faceInfo.objId;
//            FaceResult face = mTrackedFaceArray.get(trackId);
//            if (face == null) {
//                face = new FaceResult();
//            }
//            face.setFaceInfo(faceInfo);
//            newFaceList.append(trackId, face);
//        }
//
//        mTrackedFaceArray = newFaceList;
//    }
}
