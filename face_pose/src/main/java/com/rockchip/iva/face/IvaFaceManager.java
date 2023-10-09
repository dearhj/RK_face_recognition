package com.rockchip.iva.face;

import android.content.Context;
import android.util.Log;

import com.rockchip.iva.RockIva;
import com.rockchip.iva.RockIvaCallback;
import com.rockchip.iva.RockIvaImage;
import com.rockchip.iva.face.utils.FileUtil;
import com.rockchip.iva.face.utils.ImageBufferQueue;

import java.io.File;

public class IvaFaceManager {

    private RockIva rockiva = null;
    private ImageBufferQueue mBufferQueue;
    private Context mContext;

    public IvaFaceManager(Context context) {
        mContext = context;
    }

    public int initForFacePose() {
        String jsonStr = null;

        String jsonPath = Configs.IVA_FACE_POSE_CONFIG_JSON_PATH;
        File jsonFile = new File(jsonPath);
        if (jsonFile.canRead()) {
            Log.d(Configs.TAG, "read face pose iva config from " + jsonPath);
            jsonStr = FileUtil.readFile(jsonPath);
        } else {
            Log.d(Configs.TAG, "read face pose iva config from raw");
            jsonStr = FileUtil.readFileFromResRaw(mContext, Configs.IVA_FACE_POSE_CONFIG_JSON_RES_ID);
        }

        if (jsonStr == null) {
            Log.e(Configs.TAG, "iva face json file read fail! " + jsonPath);
            return -1;
        }
        rockiva = new RockIva();
        int ret = rockiva.init(jsonStr);
        if (ret != 0) {
            Log.e(Configs.TAG, "iva init fail " + ret);
            return -1;
        }

        mBufferQueue = new ImageBufferQueue(3);
        ret = mBufferQueue.init(Configs.CAMERA_IMAGE_WIDTH, Configs.CAMERA_IMAGE_HEIGHT, RockIvaImage.PixelFormat.YUV420SP_NV21);
        if (ret != 0) {
            Log.e(Configs.TAG, "iva image buffer queue init fail " + ret);
            return -1;
        }

        return 0;
    }

    public int setCallback(RockIvaCallback callback) {
        if (rockiva == null) {
            return -1;
        }
        rockiva.setCallback(callback);
        return 0;
    }

    public int release() {
        if (rockiva != null) {
            rockiva.release();
        }
        if (mBufferQueue != null) {
            mBufferQueue.release();
        }
        return 0;
    }


    public RockIva getIva() {
        return rockiva;
    }

    public ImageBufferQueue getBufferQueue() {
        return mBufferQueue;
    }

}
