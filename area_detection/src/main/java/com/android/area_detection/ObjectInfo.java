package com.android.area_detection;

public class ObjectInfo {
    public int triggerRules;
    public int score;
    public int left;
    public int right;
    public int top;
    public int bottom;

    public ObjectInfo(int mTriggerRules, int mScore, int mLeft, int mBottom, int mRight, int mTop) {
        triggerRules = mTriggerRules;
        score = mScore;
        left = mLeft;
        right = mRight;
        bottom = mBottom;
        top = mTop;
    }
}
