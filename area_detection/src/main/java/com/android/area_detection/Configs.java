package com.android.area_detection;

import android.os.Environment;

public class Configs {

    public final static String TAG = "IvaAreaDetectionApp";

    /**
     * 摄像头ID
     */
    public final static int CAMERA_ID = 1;

    /**
     * 摄像头图像宽度
     */
    public final static int CAMERA_IMAGE_WIDTH = 1280;

    /**
     * 摄像头图像高度
     */
    public final static int CAMERA_IMAGE_HEIGHT = 720;


    /**
     * 区域检测初始化IVA配置文件(如果该路径文件存在会优先raw文件加载)
     */
    public final static String IVA_AREA_DETECTION_CONFIG_JSON_PATH = Environment.getExternalStorageDirectory()+ "/Android/data/iva/iva_area_detection.json";

    /**
     * 区域检测初始化IVA配置文件（在res/raw目录下）
     */
    public final static int IVA_AREA_DETECTION_CONFIG_JSON_RES_ID = R.raw.iva_area_detection;

}
