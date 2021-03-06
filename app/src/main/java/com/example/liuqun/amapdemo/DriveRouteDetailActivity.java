package com.example.liuqun.amapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.example.liuqun.amapdemo.adapter.ItemDriveDetialAdapter;
import com.example.liuqun.amapdemo.utils.AMapUtil;

import java.util.List;

public class DriveRouteDetailActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {

    private AMap               aMap;
    private MapView            mapView;
    private DrivePath          mDrivePath;
    private DriveRouteResult   mDriveRouteResult;
    private AMapLocationClient mlocationClient;
    private LatLng             mCurrentlatLng;
    private ImageView          mIvUp;
    private LinearLayout       menu;
    private LinearLayout       view;
    private boolean            isUp;
    private TextView           mTvPathTitle;
    private TextView           mTvPathDes;
    private ListView           mLvPathDetial;
    private LinearLayout mLLayoutTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_route_detail);

        mapView = (MapView) findViewById(R.id.route_map);
        assert mapView != null;
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        getIntentData();
        init();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mDrivePath = intent.getParcelableExtra("drive_path");
        mDriveRouteResult = intent.getParcelableExtra("drive_result");
    }

    private void init() {
        mapView = (MapView) findViewById(R.id.route_map);
        if (aMap == null) {
            assert mapView != null;
            aMap = mapView.getMap();
            setUpMap();
        }
        aMap.clear();// 清理地图上的所有覆盖物

        DriveRouteColorfulOverLay drivingRouteOverlay = new DriveRouteColorfulOverLay(
                aMap, mDrivePath,
                mDriveRouteResult.getStartPos(),
                mDriveRouteResult.getTargetPos(), null);
        drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
        drivingRouteOverlay.setIsColorfulline(false);//是否用颜色展示交通拥堵情况，默认true
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomToSpan();

        mIvUp = (ImageView) findViewById(R.id.iv_up);
        mLLayoutTitle = (LinearLayout) findViewById(R.id.llayout_title);
        view = (LinearLayout) findViewById(R.id.llayout_map);
        menu = (LinearLayout) findViewById(R.id.llayout_route_detail);
        isUp = false;
        mIvUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                UpButton();
            }
        });
        mLLayoutTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpButton();
            }
        });

        mTvPathTitle = (TextView) findViewById(R.id.path_title);
        mTvPathDes = (TextView) findViewById(R.id.path_des);
        mLvPathDetial = (ListView) findViewById(R.id.route_detail);

        List<DriveStep> steps = mDrivePath.getSteps();
        String          road  = "";
        for (int i = 0; i < steps.size(); i++) {
            road += steps.get(i).getRoad() + " ";
        }
        mTvPathTitle.setText("途径:" + road);
        int dis = (int) mDrivePath.getDistance();
        int dur = (int) mDrivePath.getDuration();
        String des = AMapUtil.getFriendlyTime(dur) + " . " + AMapUtil.getFriendlyLength
                (dis);
        mTvPathDes.setText(des);

        ItemDriveDetialAdapter adapter = new ItemDriveDetialAdapter(getApplicationContext(),
                mDrivePath);
        mLvPathDetial.setAdapter(adapter);
    }

    private void UpButton() {
        //设定上拉View的高度为主视图的2/5
        int h;
        h = view.getHeight() * 3 / 5;

        if (!isUp) {
            //这里得分两条设置否则是还不到效果的，LinearLayout.LayoutParams(宽, 高)，这个参数主要是用来显示平移后视图的显示方式。
            LinearLayout.LayoutParams lp  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            //lp.setMargins(left, top, right, bottom)可作参考。
            //设定菜单视图向上平移距离
            lp.setMargins(0, -h, 0, 0);
            //设定主视图顶端和底端同时向上平移距离,如果bottom不设为h的话,底端的按钮不会随主视图一起向上平移，搞了半天才弄出来，就是卡在这个参数没设置，靠！
            lp2.setMargins(0, -h, 0, h);
            menu.setLayoutParams(lp);
            view.setLayoutParams(lp2);
            mIvUp.setImageResource(R.drawable.arrow);
            isUp = true;

        } else if (isUp) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            //两个视图返回原位置
            lp.setMargins(0, 0, 0, 0);
            menu.setLayoutParams(lp);
            view.setLayoutParams(lp);
            mIvUp.setImageResource(R.drawable.arrow_up);
            isUp = false;
        }
    }

    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getApplicationContext());
            //绑定监听
            mlocationClient.setLocationListener(this);
            //开启定位
            mlocationClient.startLocation();
        }

    }

    @Override
    public void deactivate() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        mCurrentlatLng = new LatLng(aMapLocation.getLatitude(),
                aMapLocation.getLongitude());
        //中心移到标记地点
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(mCurrentlatLng));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mapView.onSaveInstanceState(outState);
    }
}
