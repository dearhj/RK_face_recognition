package com.rockchip.iva.plate;

import android.graphics.Rect;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

public class PlateResult implements Cloneable {

    public String mPlateStr = null;
    public Rect mVehicleBox = null;
    public Rect mPlateBox = null;

    public PlateResult(String plateResult) {
        parsePlateResult(plateResult);
    }

    public int parsePlateResult(String result) {
        JSONObject jobj = JSONObject.parseObject(result);
        int objNum = (int)JSONPath.eval(jobj, "$.plateResult.objNum");
        if (objNum > 0) {
            String plateStr = (String)JSONPath.eval(jobj, "$.plateResult.plateInfo[0].plateStr");
            Object plateInfo = JSONPath.eval(jobj, "$.plateResult.plateInfo[0]");
            int vehicle_left = (int)JSONPath.eval(plateInfo, "$.vehicleRect.left");
            int vehicle_top = (int)JSONPath.eval(plateInfo, "$.vehicleRect.top");
            int vehicle_right = (int)JSONPath.eval(plateInfo, "$.vehicleRect.right");
            int vehicle_bottom = (int)JSONPath.eval(plateInfo, "$.vehicleRect.bottom");
            int plate_left = (int)JSONPath.eval(plateInfo, "$.plateRect.left");
            int plate_top = (int)JSONPath.eval(plateInfo, "$.plateRect.top");
            int plate_right = (int)JSONPath.eval(plateInfo, "$.plateRect.right");
            int plate_bottom = (int)JSONPath.eval(plateInfo, "$.plateRect.bottom");
            mPlateBox = new Rect(plate_left, plate_top, plate_right, plate_bottom);
            mVehicleBox = new Rect(vehicle_left, vehicle_top, vehicle_right, vehicle_bottom);
            mPlateStr = plateStr;
        }
        return 0;
    }

    public void setPlateStr(String plateStr) {
        this.mPlateStr = plateStr;
    }
}
