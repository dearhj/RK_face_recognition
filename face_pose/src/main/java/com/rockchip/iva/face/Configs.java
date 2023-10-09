package com.rockchip.iva.face;

import android.os.Environment;

public class Configs {

    public final static String TAG = "IvaFaceApp";

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
     * 人脸识别初始化IVA配置文件(如果该路径文件存在会优先raw文件加载)
     */
    public final static String IVA_FACE_POSE_CONFIG_JSON_PATH = Environment.getExternalStorageDirectory()+ "/Android/data/iva/iva_face_pose.json";

    /**
     * 人脸识别初始化IVA配置文件（在res/raw目录下）
     */
    public final static int IVA_FACE_POSE_CONFIG_JSON_RES_ID = R.raw.iva_face_pose;

    /**
     * 人脸库数据存放目录（需要确保有读写权限）
     */
    public final static String IVA_FACE_DATA_DIR_PATH  = Environment.getExternalStorageDirectory()+"/Android/data/iva/face_data";

}
