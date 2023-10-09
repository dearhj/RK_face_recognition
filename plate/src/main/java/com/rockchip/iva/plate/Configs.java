package com.rockchip.iva.plate;

import android.os.Environment;

import com.rockchip.iva.plate.R;

public class Configs {

    public final static String TAG = "IvaFaceApp";

    /**
     * 摄像头ID
     */
    public final static int CAMERA_ID = 0;

    /**
     * 摄像头图像宽度
     */
    public final static int CAMERA_IMAGE_WIDTH = 1920;

    /**
     * 摄像头图像高度
     */
    public final static int CAMERA_IMAGE_HEIGHT = 1080;


    /**
     * 车辆车牌识别初始化IVA配置文件(如果该路径文件存在会优先raw文件加载)
     */
    public final static String IVA_FACE_RECOG_CONFIG_JSON_PATH = Environment.getExternalStorageDirectory()+ "/Android/data/iva/iva_plate.json";

    /**
     * 车辆车牌识别初始化IVA配置文件（在res/raw目录下）
     */
    public final static int IVA_FACE_RECOG_CONFIG_JSON_RES_ID = R.raw.iva_plate;

}
