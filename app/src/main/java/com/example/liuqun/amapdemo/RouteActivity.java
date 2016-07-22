package com.example.liuqun.amapdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.example.liuqun.amapdemo.adapter.BusResultListAdapter;
import com.example.liuqun.amapdemo.utils.AMapUtil;
import com.example.liuqun.amapdemo.utils.ToastUtil;

import java.util.List;

public class RouteActivity extends AppCompatActivity implements View.OnClickListener,
        RouteSearch.OnRouteSearchListener, GeocodeSearch.OnGeocodeSearchListener {

    private static final String TAG  = "RouteActivity";
    //用于标记上一界面点击的Icon是哪个
    public static final  int    BUS  = 1;
    public static final  int    CAR  = 2;
    public static final  int    WALK = 3;
    private ImageView mIvBus;
    private ImageView mIvCar;
    private ImageView mIvWalk;

    private Context          mContext;
    private DriveRouteResult mDriveRouteResult;//自驾回调结果
    private BusRouteResult   mBusRouteResult;//公交回调结果
    private WalkRouteResult  mWalkRouteResult;//步行回调结果
    private RouteSearch      mRouteSearch;
    private String mCurrentCityName;//当前城市名称
    private LatLonPoint mStartPoint;//起点，
    private LatLonPoint mEndPoint;//终点，
    private ListView    mBusResultList;
    //逆地理编码
    private GeocodeSearch geocoderSearch;

    private ProgressDialog progDialog = null;// 搜索时进度条
    private RelativeLayout mRlayoutCarAndWalk;
    private TextView       mTvPathTitle;
    private TextView       mTvPathDes;
    private TextView mTxtEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        mContext = this.getApplicationContext();
        initView();
        initEvent();

    }

    private void initView() {

        mIvBus = (ImageView) findViewById(R.id.iv_bus);
        mIvCar = (ImageView) findViewById(R.id.iv_car);
        mIvWalk = (ImageView) findViewById(R.id.iv_walk);
        mTxtEnd = (TextView) findViewById(R.id.txt_end);

        mRlayoutCarAndWalk = (RelativeLayout) findViewById(R.id.rlayout_car_walk);
        mTvPathTitle = (TextView) findViewById(R.id.tv_path_title);
        mTvPathDes = (TextView) findViewById(R.id.tv_path_des);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "initView: " + bundle.get("Latitude"));
        int which = (int) bundle.get("which");
        Log.d(TAG, "initView: " + which);
        mStartPoint = new LatLonPoint(bundle.getDouble("CurrentLatitude"), bundle.getDouble
                ("CurrentLongitude"));
        mEndPoint = new LatLonPoint(bundle.getDouble("TargetLatitude"), bundle.getDouble
                ("TargetLongitude"));
        mCurrentCityName = (String) bundle.get("city");
        //逆地理编码获取目的地的地址信息
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        RegeocodeQuery query = new RegeocodeQuery(mEndPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        mBusResultList = (ListView) findViewById(R.id.list_route);
        switch (which) {
            case BUS:
                mRlayoutCarAndWalk.setVisibility(View.GONE);
                mBusResultList.setVisibility(View.VISIBLE);
                mIvBus.setImageResource(R.drawable.bus02);
                searchRouteResult(BUS, RouteSearch.BusDefault);
                break;
            case CAR:
                mRlayoutCarAndWalk.setVisibility(View.VISIBLE);
                mBusResultList.setVisibility(View.GONE);
                mIvCar.setImageResource(R.drawable.car02);
                searchRouteResult(CAR, RouteSearch.DrivingDefault);
                break;
            case WALK:
                mRlayoutCarAndWalk.setVisibility(View.VISIBLE);
                mBusResultList.setVisibility(View.GONE);
                mIvWalk.setImageResource(R.drawable.man02);
                searchRouteResult(WALK, RouteSearch.WalkDefault);
                break;
        }

    }

    private void initEvent() {
        mIvBus.setOnClickListener(this);
        mIvCar.setOnClickListener(this);
        mIvWalk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_bus:
                mRlayoutCarAndWalk.setVisibility(View.GONE);
                mBusResultList.setVisibility(View.VISIBLE);
                mIvBus.setImageResource(R.drawable.bus02);
                mIvCar.setImageResource(R.drawable.car01);
                mIvWalk.setImageResource(R.drawable.man01);
                searchRouteResult(BUS, RouteSearch.BusDefault);
                break;
            case R.id.iv_car:
                mRlayoutCarAndWalk.setVisibility(View.VISIBLE);
                mBusResultList.setVisibility(View.GONE);
                mIvBus.setImageResource(R.drawable.bus01);
                mIvCar.setImageResource(R.drawable.car02);
                mIvWalk.setImageResource(R.drawable.man01);
                searchRouteResult(CAR, RouteSearch.DrivingDefault);
                break;
            case R.id.iv_walk:
                mRlayoutCarAndWalk.setVisibility(View.VISIBLE);
                mBusResultList.setVisibility(View.GONE);
                mIvBus.setImageResource(R.drawable.bus01);
                mIvCar.setImageResource(R.drawable.car01);
                mIvWalk.setImageResource(R.drawable.man02);
                searchRouteResult(WALK, RouteSearch.WalkDefault);
                break;
        }
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        showProgressDialog();
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

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                } else if (result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
//        } else {
//            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        dissmissProgressDialog();
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mDriveRouteResult = result;
                    final DrivePath drivePath = mDriveRouteResult.getPaths().get(0);
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + " . " + AMapUtil.getFriendlyLength
                            (dis);
                    mTvPathDes.setText(des);
//                    mRouteDetailDes.setVisibility(View.VISIBLE);
//                    int taxiCost = (int) mDriveRouteResult.getTaxiCost();
//                    mTvPathTitle.setText("打车约"+taxiCost+"元");
                    List<DriveStep> steps = drivePath.getSteps();
                    String          road  = "";
                    for (int i = 0; i < steps.size(); i++) {
                        road += steps.get(i).getRoad()+" ";
                    }
                    mTvPathTitle.setText("途径:" + road);
                    mRlayoutCarAndWalk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    DriveRouteDetailActivity.class);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result",
                                    mDriveRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
//        } else {
//            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        dissmissProgressDialog();
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+" . "+AMapUtil.getFriendlyLength(dis);
                    mTvPathDes.setText(des);
                    Log.d(TAG, "onWalkRouteSearched: "+AMapUtil.getFriendlyTime(dur));
                    List<WalkStep> steps = walkPath.getSteps();
                    String         road  = "";
                    for (int i = 0; i < steps.size(); i++) {
                        road += steps.get(i).getRoad()+" ";
                    }
                    mTvPathTitle.setText("途径:" + road);
                    mRlayoutCarAndWalk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    WalkRouteDetailActivity.class);
                            intent.putExtra("walk_path", walkPath);
                            intent.putExtra("walk_result",
                                    mWalkRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
//        } else {
//            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    //逆地理编码回调接口
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

        mTxtEnd.setText(regeocodeResult.getRegeocodeAddress().getFormatAddress()+"附近");

    }
    //地理编码结果回调
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
}
