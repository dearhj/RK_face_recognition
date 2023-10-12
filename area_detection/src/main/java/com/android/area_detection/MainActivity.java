package com.android.area_detection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PERMISSION_CALLBACK = 0;
    private static final String TAG = "RKAreaDetection";

    int lastStartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Button faceRecognition = findViewById(R.id.button_area_detection_recognition);
        faceRecognition.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        lastStartId = view.getId();
        if (requestPermissions()) {
            startActivity(lastStartId);
        }
    }

    public boolean requestPermissions() {
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            this.requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    },
                    PERMISSION_CALLBACK);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(lastStartId);
        } else {
            Toast.makeText(this, "权限被拒绝。", Toast.LENGTH_SHORT).show();
        }
    }

    public void startActivity(int id) {
        Intent intent = null;

        if (id == R.id.button_area_detection_recognition) {
            intent = new Intent(getApplicationContext(), AreaDetectionActivity.class);
        }
        if (intent != null) {
            Log.d(TAG, "startActivity:" + intent);
            startActivity(intent);
        }
    }

}
