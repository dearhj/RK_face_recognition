package com.android.area_detection;

import android.content.Context;
import android.util.Log;

import com.rockchip.iva.RockIva;
import com.rockchip.iva.RockIvaCallback;
import com.rockchip.iva.RockIvaImage;
import com.android.area_detection.utils.FileUtil;
import com.android.area_detection.utils.ImageBufferQueue;

import java.io.File;

public class IvaAreaDetectionManager {

    private RockIva rockiva = null;
    private ImageBufferQueue mBufferQueue;
    private final Context mContext;

    public IvaAreaDetectionManager(Context context) {
        mContext = context;
    }

    public void initForAreaDetection() {
        String jsonStr;

        String jsonPath = Configs.IVA_AREA_DETECTION_CONFIG_JSON_PATH;
        File jsonFile = new File(jsonPath);
        if (jsonFile.canRead()) {
            Log.d(Configs.TAG, "read area detection iva config from " + jsonPath);
            jsonStr = FileUtil.readFile(jsonPath);
        } else {
            Log.d(Configs.TAG, "read area detection iva config from raw");
            jsonStr = FileUtil.readFileFromResRaw(mContext, Configs.IVA_AREA_DETECTION_CONFIG_JSON_RES_ID);
        }

        if (jsonStr == null) {
            Log.e(Configs.TAG, "iva area detection json file read fail! " + jsonPath);
            return;
        }
        rockiva = new RockIva();
        int ret = rockiva.init(jsonStr);
        System.out.println("执行这里了吗？？？？？？   ret = " + ret);
        if (ret != 0) {
            Log.e(Configs.TAG, "iva init fail " + ret);
            return;
        }

        mBufferQueue = new ImageBufferQueue(3);
        ret = mBufferQueue.init(Configs.CAMERA_IMAGE_WIDTH, Configs.CAMERA_IMAGE_HEIGHT, RockIvaImage.PixelFormat.YUV420SP_NV21);
        if (ret != 0) {
            Log.e(Configs.TAG, "iva image buffer queue init fail " + ret);
        }

    }

    public void setCallback(RockIvaCallback callback) {
        if (rockiva == null) {
            return;
        }
        rockiva.setCallback(callback);
    }

    public void release() {
        if (rockiva != null) {
            rockiva.release();
        }
        if (mBufferQueue != null) {
            mBufferQueue.release();
        }
    }


    public RockIva getIva() {
        return rockiva;
    }

    public ImageBufferQueue getBufferQueue() {
        return mBufferQueue;
    }

}
