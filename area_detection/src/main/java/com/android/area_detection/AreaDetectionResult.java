package com.android.area_detection;

import android.annotation.SuppressLint;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

import java.util.ArrayList;
import java.util.List;

public class AreaDetectionResult {

    public ObjectInfo objectInfo = null;

    public List<ObjectInfo> objectInfoList = null;

    public AreaDetectionResult(String plateResult) {
        parseAreaDetectionResult(plateResult);
    }

    @SuppressLint("DefaultLocale")
    public void parseAreaDetectionResult(String result) {
        JSONObject job = JSONObject.parseObject(result);
        int objNum = (int) JSONPath.eval(job, "$.baResult.objNum");
        if (objNum > 0) {
            objectInfoList = new ArrayList<>();
            for (int i = 1; i <= objNum; i++) {
                Object baInfo = JSONPath.eval(job, String.format("$.baResult.triggerObjects[%d]", i - 1));
                int obj_left = (int) JSONPath.eval(baInfo, "$.objInfo.rect.left");
                int obj_top = (int) JSONPath.eval(baInfo, "$.objInfo.rect.top");
                int obj_right = (int) JSONPath.eval(baInfo, "$.objInfo.rect.right");
                int obj_bottom = (int) JSONPath.eval(baInfo, "$.objInfo.rect.bottom");
                int score = (int) JSONPath.eval(baInfo, "$.objInfo.score");
                int triggerRules = (int) JSONPath.eval(baInfo, "$.triggerRules");
                objectInfo = new ObjectInfo(triggerRules, score, obj_left, obj_bottom, obj_right, obj_top);
//                objectInfo.right = obj_right;
//                objectInfo.left = obj_left;
//                objectInfo.top = obj_top;
//                objectInfo.bottom = obj_bottom;
//                objectInfo.score = score;
//                objectInfo.triggerRules = triggerRules;
//                mObjBox = new Rect(obj_left, obj_top, obj_right, obj_bottom);
//                objectInfo.rect = mObjBox;
//                objectInfo.objId = (int) JSONPath.eval(baInfo, "$.objInfo.objId");
//                objectInfo.frameId = (int) JSONPath.eval(baInfo, "$.objInfo.frameId");
//                objectInfo.score = (int) JSONPath.eval(baInfo, "$.objInfo.score");
//                objectInfo.type = (int) JSONPath.eval(baInfo, "$.objInfo.type");
//                objectInfo.triggerRules = (int) JSONPath.eval(baInfo, "$.triggerRules");
//                objectInfo.ruleID = (int) JSONPath.eval(baInfo, "$.firstTrigger.ruleID");
//                objectInfo.triggerType = (int) JSONPath.eval(baInfo, "$.firstTrigger.triggerType");
                objectInfoList.add(objectInfo);
            }
        }
    }
}
