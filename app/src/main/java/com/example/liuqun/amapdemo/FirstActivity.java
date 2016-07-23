package com.example.liuqun.amapdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.amap.api.maps.model.LatLng;

/**
 * 已知商铺坐标,跳转到地面界面
 */
public class FirstActivity extends AppCompatActivity implements View.OnClickListener {

    private LatLng mTargetLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        mTargetLatLng = new LatLng(36.608867, 116.963587);
        findViewById(R.id.btn_map).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent =new Intent(this,LocationActivity.class);
        Bundle bundle =new Bundle();
        bundle.putDouble("TargetLatitude",mTargetLatLng.latitude);
        bundle.putDouble("TargetLongitude",mTargetLatLng.longitude);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
