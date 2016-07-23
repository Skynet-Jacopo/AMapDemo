package com.example.liuqun.amapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.example.liuqun.amapdemo.utils.AMapUtil;
import com.example.liuqun.amapdemo.utils.ToastUtil;

public class LocationActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,
        View.OnClickListener, AMap.OnMarkerClickListener, GeocodeSearch.OnGeocodeSearchListener, RouteSearch.OnRouteSearchListener {

    private static final String TAG = "LocationActivity";
    public static final int BUS  = 1;
    public static final int CAR  = 2;
    public static final int WALK = 3;

    private AMap                      aMap;
    private MapView                   mMapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient        mlocationClient;
    private AMapLocationClientOption  mLocationOption;

    private LinearLayout mLLBus;
    private LinearLayout mLLCar;
    private LinearLayout mLLWalk;

    private LatLng mCurrentlatLng;//当前定位地理坐标
    private LatLng mTargetLatLng;//目的地地理坐标

    private String mCurrentCityName;//当前城市(用于路径规划,逆地理编码获取)
    private LatLonPoint mStartPoint;//起点，
    private LatLonPoint mEndPoint;//终点，

    private GeocodeSearch geocoderSearch;

    private RouteSearch      mRouteSearch;
    private TextView mTvBusTime;
    private TextView mTvCarTime;
    private TextView mTvWalkTime;

//    //uiHandler在主线程中创建，所以自动绑定主线程
//    private Handler uiHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            String time = (String) msg.obj;
//            mTvBusTime.setText(time);
//            Log.d("", "handleMessage: "+time);
//        }
//    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        assert mMapView != null;
        mMapView.onCreate(savedInstanceState);
        getIntentData();
        init();
        initEvent();
    }

    private void getIntentData() {
//        mTargetLatLng = new LatLng(36.608867, 116.963587);
        Intent intent = getIntent();
        Bundle bundle =intent.getExtras();
        mTargetLatLng = new LatLng(bundle.getDouble("TargetLatitude"),bundle.getDouble
                ("TargetLongitude"));
    }

    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }
        mLLBus = (LinearLayout) findViewById(R.id.llayout_bus);
        mLLCar = (LinearLayout) findViewById(R.id.llayout_car);
        mLLWalk = (LinearLayout) findViewById(R.id.llayout_walk);

        mTvBusTime = (TextView) findViewById(R.id.tv_bus_time);
        mTvCarTime = (TextView) findViewById(R.id.tv_car_time);
        mTvWalkTime = (TextView) findViewById(R.id.tv_walk_time);

        //

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        //逆地理编码
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(mEndPoint, 200,GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);


    }

    private void initEvent() {
        mLLBus.setOnClickListener(this);
        mLLCar.setOnClickListener(this);
        mLLWalk.setOnClickListener(this);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 自定义系统定位蓝点
//        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        // 自定义定位图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.nbsearch_location_map));
//        // 自定义精度范围的圆形边框颜色
//        myLocationStyle.strokeColor(Color.BLUE);
//        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
//        // 自定义精度范围的圆形边框宽度
//        myLocationStyle.strokeWidth(2);
//        // 将自定义的 myLocationStyle 对象添加到地图上
//        aMap.setMyLocationStyle(myLocationStyle);

        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        MarkerOptions otMarkerOptions = new MarkerOptions();
        otMarkerOptions.position(mTargetLatLng);
        otMarkerOptions.visible(true);//设置可见
        otMarkerOptions.title("未知商铺")
                .snippet("未知：36.608867, 116.963587");//里面的内容自定义
        otMarkerOptions.draggable(true);

        mEndPoint =new LatLonPoint(mTargetLatLng.latitude,mTargetLatLng.longitude);
        //标记图标
        otMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.loca));

        aMap.addMarker(otMarkerOptions);
        aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        //中心移到标记地点
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(mTargetLatLng));
        // 设置地图可视缩放大小
        aMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        aMap.getUiSettings().setCompassEnabled(true);// 设置指南针
        aMap.getUiSettings().setScaleControlsEnabled(true);// 设置比例尺
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getApplicationContext());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //是否返回地址信息
            mLocationOption.setNeedAddress(true);
            mLocationOption.setOffset(true);
            //是否只定位一次
            mLocationOption.setOnceLocation(false);
            //设置是否强制刷新WIFI，默认为强制刷新
            mLocationOption.setWifiActiveScan(true);
            //是否允许模拟位置
            mLocationOption.setMockEnable(false);
            //定位时间间隔
            mLocationOption.setInterval(2000);

            //给定位客户端对象设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            //绑定监听
            mlocationClient.setLocationListener(this);
            //开启定位
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
//                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                mCurrentlatLng = new LatLng(amapLocation.getLatitude(),
                        amapLocation.getLongitude());
                MarkerOptions otMarkerOptions = new MarkerOptions();
                otMarkerOptions.position(mCurrentlatLng);
                otMarkerOptions.visible(true);//设置可见
                otMarkerOptions.title("当前位置");
                otMarkerOptions.draggable(true);
                otMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.loca));
                aMap.addMarker(otMarkerOptions);

                aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件

                mStartPoint =new LatLonPoint(mCurrentlatLng.latitude,mCurrentlatLng.longitude);
                Log.d(TAG, "onLocationChanged: "+mCurrentlatLng.latitude+"  "+mCurrentlatLng
                        .longitude);

                searchRouteResult(BUS, RouteSearch.BusDefault);
                searchRouteResult(CAR,RouteSearch.DrivingDefault);
                searchRouteResult(WALK,RouteSearch.WalkDefault);

//            } else {
//                Toast.makeText(LocationActivity.this, "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 显示位置信息
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setClass(this, RouteActivity.class);
        Bundle bundle =new Bundle();
        bundle.putDouble("CurrentLatitude",mCurrentlatLng.latitude);
        bundle.putDouble("CurrentLongitude",mCurrentlatLng.longitude);
        bundle.putDouble("TargetLatitude",mTargetLatLng.latitude);
        bundle.putDouble("TargetLongitude",mTargetLatLng.longitude);
        bundle.putString("city",mCurrentCityName);
        intent.putExtras(bundle);
        switch (v.getId()) {
            case R.id.llayout_bus:
                intent.putExtra("which", BUS);
                startActivity(intent);
                break;
            case R.id.llayout_car:
                intent.putExtra("which", CAR);
                startActivity(intent);
                break;
            case R.id.llayout_walk:
                intent.putExtra("which", WALK);
                startActivity(intent);
                break;
        }
    }


    /**
     * 开始搜索路径规划方案获取最短时间 显示
     */
    public void searchRouteResult(int routeType, int mode) {
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == BUS) {// 公交路径规划
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, mode,
                    mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        } else if (routeType == CAR) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        } else if (routeType == WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }

    //逆地理编码回调接口
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        mCurrentCityName = regeocodeResult.getRegeocodeAddress().getCity();
    }
    //地理编码回调接口
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mTvBusTime.setText(AMapUtil.getFriendlyTime((int) result.getPaths().get(0)
                            .getDuration())+"");
                } else if (result.getPaths() == null) {
                    ToastUtil.show(getApplicationContext(), R.string.no_result);
                }
            } else {
                ToastUtil.show(getApplicationContext(), R.string.no_result);
            }
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mTvCarTime.setText(AMapUtil.getFriendlyTime((int) result.getPaths().get(0)
                            .getDuration()));
                } else if (result.getPaths() == null) {
                    ToastUtil.show(getApplicationContext(), R.string.no_result);
                }
            } else {
                ToastUtil.show(getApplicationContext(), R.string.no_result);
            }
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mTvWalkTime.setText(AMapUtil.getFriendlyTime((int) result.getPaths().get(0)
                            .getDuration()));
                } else if (result.getPaths() == null) {
                    ToastUtil.show(getApplicationContext(), R.string.no_result);
                }
            } else {
                ToastUtil.show(getApplicationContext(), R.string.no_result);
            }
        }
    }
}

