package com.deepsoft.shortbarge.driver.view;

import static com.amap.api.fence.GeoFenceClient.GEOFENCE_IN;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_OUT;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_STAYED;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.MapsInitializer;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
import com.deepsoft.shortbarge.driver.broadcast.ArrivalGeofenceEventReceiver;
import com.deepsoft.shortbarge.driver.broadcast.EndGeofenceEventReceiver;
import com.deepsoft.shortbarge.driver.broadcast.StartGeofenceEventReceiver;
import com.deepsoft.shortbarge.driver.constant.Action;
import com.deepsoft.shortbarge.driver.bean.DriverInfoGson;
import com.deepsoft.shortbarge.driver.bean.ResultGson;
import com.deepsoft.shortbarge.driver.bean.TaskGson;
import com.deepsoft.shortbarge.driver.bean.WeatherGson;
import com.deepsoft.shortbarge.driver.bean.message.MessageResponse;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.websocket.WsManager;
import com.deepsoft.shortbarge.driver.widget.BaseApplication;
import com.deepsoft.shortbarge.driver.widget.MyDialog;
//import com.tencent.map.geolocation.TencentGeofence;
//import com.tencent.map.geolocation.TencentGeofenceManager;
//import com.tencent.map.geolocation.TencentLocation;
//import com.tencent.map.geolocation.TencentLocationListener;
//import com.tencent.map.geolocation.TencentLocationManager;
//import com.tencent.map.geolocation.TencentLocationRequest;
//import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
//import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
//import com.tencent.tencentmap.mapsdk.maps.MapView;
//import com.tencent.tencentmap.mapsdk.maps.TencentMap;
//import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
//import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
//import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
//import com.tencent.tencentmap.mapsdk.maps.model.Circle;
//import com.tencent.tencentmap.mapsdk.maps.model.CircleOptions;
//import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
//import com.tencent.tencentmap.mapsdk.maps.model.Marker;
//import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.net.ConnectException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "MainActivity";
    private final static String ACTION_TRIGGER_GEOFENCE_START = "com.deepsoft.shortbarge.driver.broadcast.StartGeofenceEventReceiver";
    private final static String ACTION_TRIGGER_GEOFENCE_END = "com.deepsoft.shortbarge.driver.broadcast.EndGeofenceEventReceiver";
    private final static String ACTION_TRIGGER_GEOFENCE_ARRIVAL = "com.deepsoft.shortbarge.driver.broadcast.ArrivalGeofenceEventReceiver";

    private ApiInterface apiInterface;
    private StartGeofenceEventReceiver startGeofenceEventReceiver;
    private EndGeofenceEventReceiver endGeofenceEventReceiver;
    private ArrivalGeofenceEventReceiver arrivalGeofenceEventReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private SharedPreferences sp;
    private Observable<ResultGson> observable;//轮询任务用的观察者
    private MessageDialog messageDialog;
    private SettingDialog settingDialog;
    private WaitConnectDialog waitConnectDialog;
    private ConnectFailDialog connectFailDialog;

//    private TencentMap mTencentMap;
//    private Marker mCustomMarker = null;
//    private TencentLocationManager mLocationManager;
//    private TencentLocationRequest request;
//    private TencentLocation location;
//    private TencentGeofenceManager geofenceManager;
//    private TencentGeofence.Builder builder;
//    private TencentLocationListener tencentLocationListener;
    private Circle circle1, circle2, circle3;

    private AMapLocation location;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private AMapLocationListener mLocationListener = null;
    private GeoFenceClient mGeoFenceClient;
    private AMap aMap;

    private DriverInfoGson currentDriverInfo;
    private TaskGson currentTask;
    private String truckId, driverId, lang;
    private int currentRetryCount = 0, waitRetryTime = 0, maxConnectCount = 10;// 当前已重试次数// 重试等待时间 //最大重试次数
    private boolean isStart = false, isStopOver = false, isEnd = false;//是否到达起始点

    private List<TaskGson> taskGsonList = new ArrayList<>();
    private MoreTaskAdapter moreTaskAdapter;
    private Map<String, Integer> realgeo = new HashMap<>();

    private MapView main_mv_map;
    private TextView main_tv_arrive, main_tv_vm, main_tv_st_label, main_tv_at_label, main_tv_d_label,
            main_tv_ts_label, main_tv_wt_label, main_tv_ns_label, main_tv_wt, main_tv_ns,
            main_tv_ts, main_tv_dest, main_tv_at, main_tv_st, main_tv_at2_label, main_tv_at2,
            main_tv_ec, main_tv_ln, main_tv_pn, main_tv_truck, main_tv_driver, main_tv_tasknum,
            main_tv_wea, main_tv_date;
    private ImageView main_iv_wea, main_iv_setting;
    private RecyclerView main_rv_tasks;
    private View main_v_isvm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_main);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        waitConnectDialog = new WaitConnectDialog(MainActivity.this);
        settingDialog = new SettingDialog(MainActivity.this);
        connectFailDialog = new ConnectFailDialog(MainActivity.this);
        waitConnectDialog.showWaitConnectDialog(MainActivity.this, getLayoutInflater());

        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");

//        TencentLocationManager.setUserAgreePrivacy(true);
        MapsInitializer.setApiKey("b79f7bfa929d029aaa6f8ddbd964dfcd");
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);

        EventBus.getDefault().register(this);
        WsManager.getInstance().init(token);
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);

        initView();

        main_mv_map.onCreate(savedInstanceState);

        regestBroadcast();
//        initLocationTencent();
        initLocationGaode();

        getDriverInfo();
        getDriverTask();
        getWeatherInfo();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        main_mv_map.onSaveInstanceState(outState);
    }


    private void initLocationGaode(){
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        initMap(aMapLocation.getLatitude(), aMapLocation.getLongitude());

                        if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                            waitConnectDialog.dismiss();
                        settingDialog.setGps(getString(R.string.state_connected));

                        if(isStart) {
                            if (arrivalGeofenceEventReceiver.getIsEnter() && !isAlter) {
                                // 即将到达
                                sendNotice();
                                isAlter = true;
                                Toast.makeText(MainActivity.this, "即将到达", Toast.LENGTH_SHORT).show();
                            }

                            if(location != null
                                    && (location.getLatitude() != aMapLocation.getLatitude()
                                    || location.getLongitude() != aMapLocation.getLongitude())){
                                if(currentTask.getState() != 3 && !isEnd) {
                                    // 动了就运输中
                                    if(currentTask.getStopOver() && isStopOver){//经停站
                                        isStopOver = false;
                                        changeTaskState(currentTask.getTransportTaskId(), 6);
                                        currentTask.setState(6);
                                        Toast.makeText(MainActivity.this, "经停站 运输中", Toast.LENGTH_SHORT).show();
                                    }
                                    changeTaskState(currentTask.getTransportTaskId(), 3);
                                    currentTask.setState(3);
                                    Toast.makeText(MainActivity.this, "运输中", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                // 没动 判断是否在起点终点
                                if (!endGeofenceEventReceiver.getIsEnter() && startGeofenceEventReceiver.getIsEnter()) {
                                    // 在起点的地理围栏处 装卸货
                                    if(currentTask.getState() != 2) {
                                        changeTaskState(currentTask.getTransportTaskId(), 2);
                                        currentTask.setState(2);
                                        Toast.makeText(MainActivity.this, "在起点的地理围栏处 装卸货", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (endGeofenceEventReceiver.getIsEnter() && !startGeofenceEventReceiver.getIsEnter()) {
                                    // 到达终点 装卸货
                                    if(currentTask.getState() != 2) {
                                        if(currentTask.getStopOver() && !isStopOver){
                                            //经停站
                                            changeTaskState(currentTask.getTransportTaskId(), 5);
                                            currentTask.setState(5);
                                            isStopOver = true;
                                            Toast.makeText(MainActivity.this, "经停站 装卸货", Toast.LENGTH_SHORT).show();
                                        }
                                        changeTaskState(currentTask.getTransportTaskId(), 2);
                                        currentTask.setState(2);
                                        isEnd = true;
                                        Toast.makeText(MainActivity.this, "终点 装卸货", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if(currentTask.getState() != 3 && !isEnd) {
                                        // 不在起点 终点围栏 运输中
                                        changeTaskState(currentTask.getTransportTaskId(), 3);
                                        currentTask.setState(3);
                                        Toast.makeText(MainActivity.this, "不在起点终点 运输中", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                        location = aMapLocation;

                    }else {
                        Toast.makeText(MainActivity.this, "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                        if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                            waitConnectDialog.dismiss();
                        connectFailDialog.showConnectFailDialog();
                        settingDialog.setGps(getString(R.string.state_lost));
                    }
                }
            }
        };
        mLocationOption = new AMapLocationClientOption();

        try {
            mLocationClient = new AMapLocationClient(this);
            mLocationClient.setApiKey("b79f7bfa929d029aaa6f8ddbd964dfcd");
            mLocationClient.setLocationListener(mLocationListener);
            // 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
//            mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
            mLocationOption.setInterval(1500);
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setNeedAddress(false);
            mLocationOption.setMockEnable(true);
            mLocationOption.setHttpTimeOut(10000);
            mLocationOption.setLocationCacheEnable(true);
            if(null != mLocationClient){
                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.stopLocation();
                mLocationClient.startLocation();
                setMaker();
            }
        }catch (Exception e){
        }
    }


    private void initGeofenceGaode(){
        mGeoFenceClient = new GeoFenceClient(getApplicationContext());
        //设置希望侦测的围栏触发行为，默认只侦测用户进入围栏的行为
        //public static final int GEOFENCE_IN 进入地理围栏
        //public static final int GEOFENCE_OUT 退出地理围栏
        //public static final int GEOFENCE_STAYED 停留在地理围栏内10分钟
        mGeoFenceClient.setActivateAction(GEOFENCE_IN|GEOFENCE_OUT|GEOFENCE_STAYED);
        GeoFenceListener fenceListenter = new GeoFenceListener() {
            @Override
            public void onGeoFenceCreateFinished(List<GeoFence> list, int i, String s) {
                if(i == GeoFence.ADDGEOFENCE_SUCCESS){//判断围栏是否创建成功
                    for(GeoFence x : list){
                        Log.e(TAG, "围栏成功："+x.getType());
                    }
                } else {
                    for(GeoFence x : list){
                        Log.e(TAG, "围栏失败："+x.getType()+s);
                    }
                }
            }
        };
        mGeoFenceClient.setGeoFenceListener(fenceListenter);//设置回调监听

        mGeoFenceClient.removeGeoFence();
        mGeoFenceClient.addGeoFence("start","起点",
                new DPoint(Double.parseDouble(currentTask.getOriginLat()),
                        Double.parseDouble(currentTask.getOriginLng())),
                Float.parseFloat(currentTask.getOriginFenceRange()),
                10,"进出起点");
        mGeoFenceClient.addGeoFence("end","终点",
                new DPoint(Double.parseDouble(currentTask.getDestinationLat()),
                        Double.parseDouble(currentTask.getDestinationLng())),
                Float.parseFloat(currentTask.getDestinationFenceRange()),
                10,"进出终点");
        mGeoFenceClient.addGeoFence("arrival","预警",
                new DPoint(Double.parseDouble(currentTask.getDestinationLat()),
                        Double.parseDouble(currentTask.getDestinationLng())),
                Float.parseFloat(currentTask.getDestinationWarningRange()),
                10,"进出预警");

        mGeoFenceClient.createPendingIntent(ACTION_TRIGGER_GEOFENCE_START);
        mGeoFenceClient.createPendingIntent(ACTION_TRIGGER_GEOFENCE_END);
        mGeoFenceClient.createPendingIntent(ACTION_TRIGGER_GEOFENCE_ARRIVAL);

        LatLng latLng1 = new LatLng(Double.parseDouble(currentTask.getOriginLat()),Double.parseDouble(currentTask.getOriginLng()));
        LatLng latLng2 = new LatLng(Double.parseDouble(currentTask.getDestinationLat()),Double.parseDouble(currentTask.getDestinationLng()));
        circle1 = aMap.addCircle(new CircleOptions().center(latLng1)
                .radius(Double.parseDouble(currentTask.getOriginFenceRange()))
                .fillColor(0x880000FF)
                .visible(true)
                .zIndex(2));
        circle2 = aMap.addCircle(new CircleOptions().center(latLng2)
                .radius(Double.parseDouble(currentTask.getDestinationFenceRange()))
                .fillColor(0x88FFFF00)
                .visible(true)
                .zIndex(3));
        circle3 = aMap.addCircle(new CircleOptions().center(latLng2)
                .radius(Double.parseDouble(currentTask.getDestinationWarningRange()))
                .fillColor(0x88F15C58)
                .visible(true)
                .zIndex(2));
    }


    private static boolean isAlter = false;
    /*private void initLocationTencent(){
        mLocationManager = TencentLocationManager.getInstance(this);
        tencentLocationListener = new TencentLocationListener() {
            @Override
            public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
                if (TencentLocation.ERROR_OK == i) {
                    if (tencentLocation != null) {
                        initTencentMap(tencentLocation.getLatitude(), tencentLocation.getLongitude());
                        setMaker(tencentLocation.getLatitude(), tencentLocation.getLongitude());

                        if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                            waitConnectDialog.dismiss();
                        settingDialog.setGps(getString(R.string.state_connected));

                        if(isStart) {
                            if (arrivalGeofenceEventReceiver.getIsEnter() && !isAlter) {
                                // 即将到达
                                sendNotice();
                                isAlter = true;
                                Toast.makeText(MainActivity.this, "即将到达", Toast.LENGTH_SHORT).show();
                            }

                            if(location != null
                                && (location.getLatitude() != tencentLocation.getLatitude()
                                ||location.getLongitude() != tencentLocation.getLongitude())){
                                if(currentTask.getState() != 3 && !isEnd) {
                                    // 动了就运输中
                                    if(currentTask.getStopOver() && isStopOver){//经停站
                                        isStopOver = false;
                                        changeTaskState(currentTask.getTransportTaskId(), 6);
                                        currentTask.setState(6);
                                        Toast.makeText(MainActivity.this, "经停站 运输中", Toast.LENGTH_SHORT).show();
                                    }
                                    changeTaskState(currentTask.getTransportTaskId(), 3);
                                    currentTask.setState(3);
                                    Toast.makeText(MainActivity.this, "运输中", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                // 没动 判断是否在起点终点
                                if (!endGeofenceEventReceiver.getIsEnter() && startGeofenceEventReceiver.getIsEnter()) {
                                    // 在起点的地理围栏处 装卸货
                                    if(currentTask.getState() != 2) {
                                        changeTaskState(currentTask.getTransportTaskId(), 2);
                                        currentTask.setState(2);
                                        Toast.makeText(MainActivity.this, "在起点的地理围栏处 装卸货", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (endGeofenceEventReceiver.getIsEnter() && !startGeofenceEventReceiver.getIsEnter()) {
                                    // 到达终点 装卸货
                                    if(currentTask.getState() != 2) {
                                        if(currentTask.getStopOver() && !isStopOver){
                                            //经停站
                                            changeTaskState(currentTask.getTransportTaskId(), 5);
                                            currentTask.setState(5);
                                            isStopOver = true;
                                            Toast.makeText(MainActivity.this, "经停站 装卸货", Toast.LENGTH_SHORT).show();
                                        }
                                        changeTaskState(currentTask.getTransportTaskId(), 2);
                                        currentTask.setState(2);
                                        isEnd = true;
                                        Toast.makeText(MainActivity.this, "终点 装卸货", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if(currentTask.getState() != 3 && !isEnd) {
                                        // 不在起点 终点围栏 运输中
                                        changeTaskState(currentTask.getTransportTaskId(), 3);
                                        currentTask.setState(3);
                                        Toast.makeText(MainActivity.this, "不在起点终点 运输中", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                        location = tencentLocation;
                    }
                } else {
                    Log.e(TAG, "定位失败"+i+" "+s);
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                    settingDialog.setGps(getString(R.string.state_lost));
                }
            }

            @Override
            public void onStatusUpdate(String s, int i, String s1) {}
        };

        request = TencentLocationRequest.create();
        request.setInterval(1500)
                .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO)
                .setAllowGPS(true)
                .setAllowDirection(true)
                .setIndoorLocationMode(true)
                .setLocMode(TencentLocationRequest.HIGH_ACCURACY_MODE)
                .setGpsFirst(true)
                .setGpsFirstTimeOut(5*1000)
                .setAllowCache(true);

        int error = mLocationManager.requestLocationUpdates(request, tencentLocationListener);
        if (error == 0){
            if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                waitConnectDialog.dismiss();
            settingDialog.setGps(getString(R.string.state_connected));
        } else {
            Log.i(TAG, "注册位置监听器失败！");
            if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                waitConnectDialog.dismiss();
            connectFailDialog.showConnectFailDialog();
            settingDialog.setGps(getString(R.string.state_lost));
        }
    }*/


    private void regestBroadcast(){
//        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        startGeofenceEventReceiver = new StartGeofenceEventReceiver();

//        IntentFilter intentFilter1 = new IntentFilter();
//        intentFilter1.addAction(ACTION_TRIGGER_GEOFENCE_START);
//        localBroadcastManager.registerReceiver(startGeofenceEventReceiver, intentFilter1);

        endGeofenceEventReceiver = new EndGeofenceEventReceiver();
//        IntentFilter intentFilter2 = new IntentFilter();
//        intentFilter2.addAction(ACTION_TRIGGER_GEOFENCE_END);
//        localBroadcastManager.registerReceiver(endGeofenceEventReceiver, intentFilter2);

        arrivalGeofenceEventReceiver = new ArrivalGeofenceEventReceiver();
//        IntentFilter intentFilter3 = new IntentFilter();
//        intentFilter3.addAction(ACTION_TRIGGER_GEOFENCE_ARRIVAL);
//        localBroadcastManager.registerReceiver(arrivalGeofenceEventReceiver, intentFilter3);

//        geofenceManager = new TencentGeofenceManager(this);
//        builder = new TencentGeofence.Builder();

        List<ResolveInfo> resolveInfos = getPackageManager().queryBroadcastReceivers(new Intent().setAction(ACTION_TRIGGER_GEOFENCE_START), 0);
        if(resolveInfos != null && !resolveInfos.isEmpty()){
            Log.e(TAG, ACTION_TRIGGER_GEOFENCE_START+" is not empty");
        }else{
            Log.e(TAG, ACTION_TRIGGER_GEOFENCE_START+" is empty");
        }
        List<ResolveInfo> resolveInfos2 = getPackageManager().queryBroadcastReceivers(new Intent().setAction(ACTION_TRIGGER_GEOFENCE_END), 0);
        if(resolveInfos2 != null && !resolveInfos2.isEmpty()){
            Log.e(TAG, ACTION_TRIGGER_GEOFENCE_END+" is not empty");
        }else{
            Log.e(TAG, ACTION_TRIGGER_GEOFENCE_END+" is empty");
        }
        List<ResolveInfo> resolveInfos3 = getPackageManager().queryBroadcastReceivers(new Intent().setAction(ACTION_TRIGGER_GEOFENCE_ARRIVAL), 0);
        if(resolveInfos3 != null && !resolveInfos3.isEmpty()){
            Log.e(TAG, ACTION_TRIGGER_GEOFENCE_ARRIVAL+" is not empty");
        }else{
            Log.e(TAG, ACTION_TRIGGER_GEOFENCE_ARRIVAL+" is empty");
        }
    }


    /*private void initGeofenceTencent(){
        geofenceManager.removeAllFences();

        TencentGeofence startGeofence = builder.setTag("start")
                .setCircularRegion(Double.parseDouble(currentTask.getOriginLat()),
                        Double.parseDouble(currentTask.getOriginLng()),
                        Float.parseFloat(currentTask.getOriginFenceRange()))
                .build();
        Intent receiver1 = new Intent(ACTION_TRIGGER_GEOFENCE_START);
        receiver1.setComponent(new ComponentName("com.deepsoft.shortbarge.driver", ACTION_TRIGGER_GEOFENCE_START));
        receiver1.putExtra("KEY_GEOFENCE_ID", startGeofence.getTag());
        receiver1.putExtra("KEY_GEOFENCE_LAT", startGeofence.getLatitude());
        receiver1.putExtra("KEY_GEOFENCE_LNG", startGeofence.getLongitude());
        PendingIntent pi1 = PendingIntent.getBroadcast(this, (int) (Math.random() * 1E7),
                receiver1, PendingIntent.FLAG_UPDATE_CURRENT);
        geofenceManager.addFence(startGeofence, pi1);
        AlarmManager alarmManager1 = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager1.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi1);

        TencentGeofence endGeofence = builder.setTag("end")
                .setCircularRegion(Double.parseDouble(currentTask.getDestinationLat()),
                        Double.parseDouble(currentTask.getDestinationLng()),
                        Float.parseFloat(currentTask.getDestinationFenceRange()))
                .build();
        Intent receiver2 = new Intent(ACTION_TRIGGER_GEOFENCE_END);
        receiver1.setComponent(new ComponentName("com.deepsoft.shortbarge.driver", ACTION_TRIGGER_GEOFENCE_END));
        receiver2.putExtra("KEY_GEOFENCE_ID", endGeofence.getTag());
        receiver2.putExtra("KEY_GEOFENCE_LAT", endGeofence.getLatitude());
        receiver2.putExtra("KEY_GEOFENCE_LNG", endGeofence.getLongitude());
        PendingIntent pi2 = PendingIntent.getBroadcast(this, (int) (Math.random() * 1E7),
                receiver2, PendingIntent.FLAG_UPDATE_CURRENT);
        geofenceManager.addFence(endGeofence, pi2);
        AlarmManager alarmManager2 = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager2.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi2);

        TencentGeofence arriveGeofence = builder.setTag("arr")
                .setCircularRegion(Double.parseDouble(currentTask.getDestinationLat()),
                        Double.parseDouble(currentTask.getDestinationLng()),
                        Float.parseFloat(currentTask.getDestinationWarningRange()))
                .build();
        Intent receiver3 = new Intent(ACTION_TRIGGER_GEOFENCE_ARRIVAL);
        receiver1.setComponent(new ComponentName("com.deepsoft.shortbarge.driver", ACTION_TRIGGER_GEOFENCE_ARRIVAL));
        receiver3.putExtra("KEY_GEOFENCE_ID", arriveGeofence.getTag());
        receiver3.putExtra("KEY_GEOFENCE_LAT", arriveGeofence.getLatitude());
        receiver3.putExtra("KEY_GEOFENCE_LNG", arriveGeofence.getLongitude());
        PendingIntent pi3 = PendingIntent.getBroadcast(this, (int) (Math.random() * 1E7),
                receiver3, PendingIntent.FLAG_UPDATE_CURRENT);
        geofenceManager.addFence(arriveGeofence, pi3);
        AlarmManager alarmManager3 = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager3.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi3);

        if(circle1 != null && circle2 != null && circle3 != null) {
            circle1.remove();
            circle2.remove();
            circle3.remove();
        }
        LatLng latLng1 = new LatLng(Double.parseDouble(currentTask.getOriginLat()),Double.parseDouble(currentTask.getOriginLng()));
        LatLng latLng2 = new LatLng(Double.parseDouble(currentTask.getDestinationLat()),Double.parseDouble(currentTask.getDestinationLng()));
        circle1 = mTencentMap.addCircle(new CircleOptions().center(latLng1)
                .radius(Double.parseDouble(currentTask.getOriginFenceRange()))
                .fillColor(0x880000FF)
                .clickable(false)
                .visible(true)
                .zIndex(2));
        circle2 = mTencentMap.addCircle(new CircleOptions().center(latLng2)
                .radius(Double.parseDouble(currentTask.getDestinationFenceRange()))
                .fillColor(0x88FFFF00)
                .clickable(false)
                .visible(true)
                .zIndex(3));
        circle3 = mTencentMap.addCircle(new CircleOptions().center(latLng2)
                .radius(Double.parseDouble(currentTask.getDestinationWarningRange()))
                .fillColor(0x88F15C58)
                .clickable(false)
                .visible(true)
                .zIndex(2));
    }*/


    private void initMap(double lat, double lon){
        CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition(
                new CameraPosition(
                    new LatLng(lat, lon),   //中心点坐标，地图目标经纬度
                    15,                  //目标缩放级别
                    0f,                 //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                    0f));               //目标旋转角 0~360° (正北方为0)
        aMap.moveCamera(cameraSigma);

//        mTencentMap.moveCamera(cameraSigma);
//        mTencentMap.setOnMapLoadedCallback(new TencentMap.OnMapLoadedCallback() {
//            public void onMapLoaded() {
//                //第一次渲染成功的回调
//                Log.i(TAG, "map ok");
//                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
//                    waitConnectDialog.dismiss();
//                settingDialog.setGps(getString(R.string.state_connected));
//            }
//        });

//        mTencentMap.setLocationSource(locationSource);
//        mTencentMap.setMyLocationEnabled(true);
    }


    private void initView(){
        main_tv_arrive = findViewById(R.id.main_tv_arrive);
        main_tv_arrive.setOnClickListener(this);
        PressUtil.setPressChange(this, main_tv_arrive);

        main_tv_vm = findViewById(R.id.main_tv_vm);
        main_tv_vm.setOnClickListener(this);
        PressUtil.setPressChange(this, main_tv_vm);

        main_mv_map = findViewById(R.id.main_mv_map);
//        mTencentMap = main_mv_map.getMap();
        if (aMap == null) {
            aMap = main_mv_map.getMap();
        }

        main_rv_tasks = findViewById(R.id.main_rv_tasks);
        main_tv_tasknum = findViewById(R.id.main_tv_tasknum);

        // 有voice message
        main_tv_st_label = findViewById(R.id.main_tv_st_label);
        main_tv_at_label = findViewById(R.id.main_tv_at_label);
        main_tv_d_label = findViewById(R.id.main_tv_d_label);
        main_tv_ts_label = findViewById(R.id.main_tv_ts_label);
        main_tv_st = findViewById(R.id.main_tv_st);
        main_tv_at = findViewById(R.id.main_tv_at);
        main_tv_dest = findViewById(R.id.main_tv_dest);
        main_tv_ts = findViewById(R.id.main_tv_ts);

        // 无voice message
        main_tv_at2_label = findViewById(R.id.main_tv_at2_label);
        main_tv_wt_label = findViewById(R.id.main_tv_wt_label);
        main_tv_ns_label = findViewById(R.id.main_tv_ns_label);
        main_tv_wt = findViewById(R.id.main_tv_wt);
        main_tv_ns = findViewById(R.id.main_tv_ns);
        main_tv_at2 = findViewById(R.id.main_tv_at2);

        // 汽车信息
        main_tv_ec = findViewById(R.id.main_tv_ec);
        main_tv_ln = findViewById(R.id.main_tv_ln);
        main_tv_pn = findViewById(R.id.main_tv_pn);
        main_tv_truck = findViewById(R.id.main_tv_truck);

        // 顶部信息
        main_tv_driver = findViewById(R.id.main_tv_driver);
        main_tv_date = findViewById(R.id.main_tv_date);
        main_tv_wea = findViewById(R.id.main_tv_wea);
        main_iv_wea = findViewById(R.id.main_iv_wea);
        main_iv_setting = findViewById(R.id.main_iv_setting);
        main_iv_setting.setOnClickListener(this);
        PressUtil.setPressChange(this, main_iv_setting);

        main_v_isvm = findViewById(R.id.main_v_isvm);
        main_v_isvm.setVisibility(View.INVISIBLE);

        main_tv_st.setText("");
        main_tv_at.setText("");
        main_tv_arrive.setText(R.string.task_start);

        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1" : "2";
        moreTaskAdapter = new MoreTaskAdapter(R.layout.item_more_task, taskGsonList, lang);
        main_rv_tasks.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
        main_rv_tasks.setAdapter(moreTaskAdapter);
    }


    private void getDriverInfo(){
        apiInterface.getDriverInfo().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "getDriverInfo run: get同步请求 "+ "code="+response.body().getCode()+" msg="+response.body().getMsg());
                ResultGson resultGson = response.body();
                if(resultGson.getSuccess()){
                    List<DriverInfoGson> list = GsonConvertUtil.performTransform(resultGson.getData(), DriverInfoGson.class);
                    DriverInfoGson driverInfoGson = list.get(0);
                    currentDriverInfo = driverInfoGson;
                    main_tv_ln.setText(driverInfoGson.getLicensePlate());
                    lang = sp.getString("locale_language", "en");
                    lang = lang.equals("en") ? "1": "2";
                    if(lang.equals("1")){
                        main_tv_driver.setText(driverInfoGson.getNameEng());
                    }else {
                        main_tv_driver.setText(driverInfoGson.getName());
                    }
                    main_tv_pn.setText(driverInfoGson.getPhone());
                    truckId = ""+driverInfoGson.getTruckId();
                    if(truckId.length() == 1) truckId = "0"+truckId;
                    driverId = ""+driverInfoGson.getDriverId();
                    if(driverId.length() == 1) driverId = "0"+driverId;
                    main_tv_truck.setText(truckId);
                    messageDialog = new MessageDialog(MainActivity.this, truckId, driverId);

                    if(settingDialog != null) {
                        settingDialog.setDriverInfoGson(currentDriverInfo);
                        settingDialog.setServer(getString(R.string.state_connected));
                    }
                }else{
                    if(settingDialog != null) settingDialog.setServer(getString(R.string.state_lost));
                    Log.i(TAG, "getDriverInfo连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverInfo onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                    settingDialog.setGps(getString(R.string.state_lost));
                }
            }
        });
    }


    private void getWeatherInfo(){
        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1": "2";
        String wea = lang.equals("1") ? "2" : "1";
        apiInterface.getWeatherInfo(wea).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "getWeatherInfo run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    List<WeatherGson> list = GsonConvertUtil.performTransform(resultGson.getData(), WeatherGson.class);
                    WeatherGson weatherGson = list.get(0);
                    main_tv_wea.setText(weatherGson.getWeather() + " " + weatherGson.getTemperature());
                    main_tv_date.setText(weatherGson.getDate());
                    main_iv_wea.setColorFilter(Color.WHITE);
                    main_iv_wea.setImageResource(getResources().getIdentifier("wea_"+weatherGson.getIcon(), "drawable",
                            BaseApplication.getApplication().getPackageName()));
                }else{
                    Log.i(TAG, "getWeatherInfo连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getWeatherInfo onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                }
            }
        });
    }


    /**
     * 5s 轮询任务列表
     */
    private void getDriverTask(){
        observable = apiInterface.getDriverTask();
        observable.retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Throwable throwable) throws Exception {
                        if(currentRetryCount < maxConnectCount) {
                            if(waitConnectDialog != null && !waitConnectDialog.getIsShow())
                                waitConnectDialog.showWaitConnectDialog(MainActivity.this, getLayoutInflater());
                            Log.d(TAG, "发生异常 = " + throwable.toString());
                            currentRetryCount++;
                            Log.d(TAG, "重试次数 = " + currentRetryCount);
                            waitRetryTime = 1 + currentRetryCount;
                            Log.d(TAG, "等待时间 = " + waitRetryTime);
                            return Observable.just(1).delay(waitRetryTime, TimeUnit.SECONDS);
                        }else{
                            if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                                waitConnectDialog.dismiss();
                            connectFailDialog.showConnectFailDialog();
                            return Observable.error(new Throwable("重试次数已超过设置次数 = " +currentRetryCount  + "，即不再重试"));
                        }
                    }
                });
            }
        }).repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Object throwable) throws Exception {
                        // 注：此处加入了delay操作符，作用 = 延迟一段时间发送（此处设置 = 5s），以实现轮询间间隔设置
                        return Observable.just(1).delay(5 , TimeUnit.SECONDS);
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<ResultGson>() {
                @Override
                public void onSubscribe(Disposable d) { }

                @Override
                public void onNext(ResultGson value) {
                    Log.i(TAG, "getDriverTask run: get同步请求 " + "code=" + value.getCode() + " msg=" + value.getMsg());
                    ResultGson resultGson = value;
                    lang = sp.getString("locale_language", "en");
                    lang = lang.equals("en") ? "1" : "2";
                    if (resultGson.getSuccess()) {
                        taskGsonList = GsonConvertUtil.performTransform(resultGson.getData(), TaskGson.class);
                        if(taskGsonList != null && taskGsonList.size() != 0) {//任务列表不为空
                            if(currentTask == null //当前任务和请求任务列表的第一个是不同的
                                    || (!currentTask.getTransportTaskId().equals(taskGsonList.get(0).getTransportTaskId()))){
                                isStart = false;
                                main_tv_st.setText(taskGsonList.get(0).getStartTime());
                                main_tv_at.setText(taskGsonList.get(0).getArrivalTime());
                                currentTask = taskGsonList.get(0);
                                if(currentTask.getOriginLat().equals("") || currentTask.getOriginLat() == null
                                        || currentTask.getDestinationLat().equals("") || currentTask.getDestinationLat() == null){
                                    currentTask.setOriginLat("30.5569");
                                    currentTask.setOriginLng("120.929712");
                                    currentTask.setOriginFenceRange("500");//2A-1/2
                                    currentTask.setDestinationLat("30.554674");
                                    currentTask.setDestinationLng("120.960136");
                                    currentTask.setDestinationFenceRange("500");
                                    currentTask.setDestinationWarningRange("1000");//VACON
                                    Log.e(TAG, "测试获取经纬度为null，默认2A-1/2出发前往VACON");
                                    Toast.makeText(MainActivity.this, "测试获取经纬度为null，默认2A-1/2出发前往VACON", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if(lang.equals("1")) {
                                main_tv_dest.setText(currentTask.getNextStationEng() + currentTask.getTaskDura(lang));
                                main_tv_ec.setText(currentTask.getAdminNameEng());
                            }else{
                                main_tv_dest.setText(currentTask.getNextStation() + currentTask.getTaskDura(lang));
                                main_tv_ec.setText(currentTask.getAdminName());
                            }
                            main_tv_ts.setText(currentTask.getTaskState(lang) + currentTask.getTaskStateDuration(lang));
                            main_tv_arrive.setClickable(true);
                        }else{
                            currentTask = new TaskGson();
                            main_tv_dest.setText(currentTask.getDuration());
                            main_tv_ts.setText(""+currentTask.getState());
                            isStart = false;
                            main_tv_arrive.setClickable(false);
                            main_tv_arrive.setAlpha(0.5F);
                        }
                        main_tv_tasknum.setText("" + taskGsonList.size());
                        moreTaskAdapter.setList(taskGsonList);
                        if(settingDialog != null) settingDialog.setServer(getString(R.string.state_connected));
                    }else{
                        if(settingDialog != null) settingDialog.setServer(getString(R.string.state_lost));
                        Log.i(TAG, "getDriverTask连接成功 数据申请失败， msg="+resultGson.getMsg());
                    }
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "getDriverTask onFailure:"+e.toString());
                    if(e instanceof ConnectException){
                        if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                            waitConnectDialog.dismiss();
                        connectFailDialog.showConnectFailDialog();
                        if(settingDialog != null) settingDialog.setServer(getString(R.string.state_lost));
                    }
                }

                @Override
                public void onComplete() { }
            });
    }


    private void changeTaskState(String transportTaskId, Integer state){
        apiInterface.changeTaskState(transportTaskId, state).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.e(TAG, "changeTaskState run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                lang = sp.getString("locale_language", "en");
                lang = lang.equals("en") ? "1": "2";
                if (resultGson.getSuccess()) {
                    currentTask.setState(state);
                    main_tv_ts.setText(currentTask.getTaskState(lang));
                    moreTaskAdapter.notifyItemChanged(0);
                }else{
                    Log.i(TAG, "changeTaskState连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "changeTaskState onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                }
            }
        });
    }


    private void sendNotice(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("transportTaskId", currentTask.getTransportTaskId());
        RequestBody requestBody = RequestBody.create(MediaType.parse("Content-Type, application/json"), new JSONObject(map).toString());
        apiInterface.sendNotice(requestBody).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "sendNotice run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    Log.i(TAG, "sendNotice连接成功 数据申请成功， msg="+resultGson.getMsg());
                }else{
                    Log.i(TAG, "sendNotice连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "sendNotice onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                }
            }
        });
    }


    private void setMaker(){
        BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.mipmap.scatter_car);
//        LatLng position = new LatLng(lat, lng);
        /*if(mCustomMarker == null) {
            mCustomMarker = mTencentMap.addMarker(new MarkerOptions(position)
                    .icon(custom)
                    .flat(true));
        }else {
            mCustomMarker.setPosition(position);
        }*/

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(custom);
        myLocationStyle.interval(1500);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);

//        if(markerOption == null) {
//            markerOption = new MarkerOptions();
//            markerOption.position(position);
//            markerOption.icon(custom);
//        }else {
//            markerOption.position(position);
//        }
    }


    @Override
    protected void onStart() {
        super.onStart();
//        main_mv_map.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        main_mv_map.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        main_mv_map.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
//        main_mv_map.onStop();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
//        main_mv_map.onRestart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(geofenceManager != null) {
//            geofenceManager.removeAllFences();
//            geofenceManager.destroy();
//        }
//        if(mCustomMarker != null) {
//            mCustomMarker.remove();
//            mCustomMarker = null;
//        }
//        if(tencentLocationListener != null) {
//            mLocationManager.removeUpdates(tencentLocationListener);
//        }
//        mLocationManager = null;
//        request = null;
//        main_mv_map.onDestroy();

        mLocationClient.stopLocation();
        mLocationClient.onDestroy();

        if(settingDialog != null){
            settingDialog.dismiss();
            settingDialog = null;
        }
        if(messageDialog != null){
            messageDialog.dismiss();
            messageDialog = null;
        }
        if(waitConnectDialog != null){
            waitConnectDialog.dismiss();
            waitConnectDialog = null;
        }
        EventBus.getDefault().unregister(this);
        WsManager.getInstance().disconnect();
        apiInterface = null;
        observable = null;
//        localBroadcastManager.unregisterReceiver(startGeofenceEventReceiver);
//        localBroadcastManager.unregisterReceiver(endGeofenceEventReceiver);
//        localBroadcastManager.unregisterReceiver(arrivalGeofenceEventReceiver);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss", Locale.CHINA);
        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1": "2";
        switch (view.getId()){
            case R.id.main_tv_arrive:
                String btn_text = main_tv_arrive.getText().toString();
                if(btn_text.equals(getString(R.string.task_start))){//开始任务
                    if(location != null
                            && !currentTask.getOriginLat().equals("")
                            && currentTask.getOriginLat() != null) {
                        isStart = true;
                        isEnd = false;
                        isStopOver = false;
                        isAlter = false;
                        LocalTime localTime = LocalTime.now();
                        main_tv_st.setText(localTime.format(formatter));
                        if(taskGsonList.size() == 1){
                            main_tv_arrive.setText(R.string.task_continue);
                        } else {
                            main_tv_arrive.setText(R.string.task_continue);
                        }
//                        initGeofenceTencent();
                        initGeofenceGaode();
                    }else{
                        Toast.makeText(this, "还未获取当前定位或任务对象有问题", Toast.LENGTH_SHORT).show();
                    }
                }else{//完成
                    if(currentTask.getState() == 2 && isEnd) {//装卸货状态才能完成
                        LocalTime localTime = LocalTime.now();
                        main_tv_at.setText(localTime.format(formatter));
                        taskGsonList.remove(currentTask);
                        moreTaskAdapter.setList(taskGsonList);
                        if (taskGsonList.size() == 0) {
                            currentTask = new TaskGson();
                            main_tv_arrive.setClickable(false);
                            main_tv_arrive.setAlpha(0.5F);
                        } else {
                            currentTask = taskGsonList.get(0);
                            main_tv_arrive.setClickable(true);
                        }
                        main_tv_dest.setText(currentTask.getNextStation() + currentTask.getTaskDura(lang));
                        main_tv_st.setText(currentTask.getStartTime());
                        main_tv_at.setText(currentTask.getArrivalTime());
                        main_tv_tasknum.setText("" + taskGsonList.size());
                        main_tv_arrive.setText(R.string.task_start);
                        isStart = false;
                    }else{
                        Toast.makeText(this, getString(R.string.hint_check_load), Toast.LENGTH_SHORT).show();
                        if(location != null){
                            Toast.makeText(this, "当前经纬度："+location.getLongitude()+" "+location.getLatitude(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case R.id.main_tv_vm:
                if(main_v_isvm.getVisibility() == View.VISIBLE) {
                    messageDialog.showMessageDialog(this, getLayoutInflater());
                    main_v_isvm.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.main_iv_setting:
                settingDialog.showSettingDialog(this, getLayoutInflater());
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MessageResponse messageResponse){
        if(messageResponse.getType() == 1 && location != null && currentDriverInfo != null){
            //返回经纬度
            WsManager.getInstance().sendReq(new Action("{\"driverId\":"+currentDriverInfo.getDriverId()
                    +",\"truckId\":"+currentDriverInfo.getTruckId()
                    +",\"lng\":"+ location.getLongitude()
                    +",\"lat\":"+ location.getLatitude()+"}", 1, null));
        }else if(messageResponse.getType() == 2){
            //聊天消息
            WsManager.getInstance().sendReq(new Action(messageResponse.getMessage(), 3, null));
            Log.e(TAG, "收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
            main_v_isvm.setVisibility(View.VISIBLE);
            main_tv_vm.setAlpha(1F);
            messageDialog.addData(messageResponse);
        }else{
            Log.e(TAG, "else收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
        }
    }


    /**
     * 链接错误dialog
     */
    private class ConnectFailDialog extends MyDialog {

        public ConnectFailDialog(@NonNull Context context) {
            super(context);
        }

        public ConnectFailDialog(@NonNull Context context, int themeResId) {
            super(context, themeResId);
        }

        public void showConnectFailDialog(){
            View dialog_wait_connect = getLayoutInflater().inflate(R.layout.dialog_connect_fail, null);
            this.setContentView(dialog_wait_connect);
            this.show();

            TextView dialog_fail_tv_exit = dialog_wait_connect.findViewById(R.id.dialog_fail_tv_exit);
            dialog_fail_tv_exit.setOnClickListener(v->{
                this.dismiss();
                MainActivity.this.finish();
                MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
            });
            TextView dialog_fail_tv_retry = dialog_wait_connect.findViewById(R.id.dialog_fail_tv_retry);
            dialog_fail_tv_retry.setOnClickListener(v->{
                this.dismiss();
                getDriverInfo();
                getDriverTask();
                getWeatherInfo();
            });
        }
    }
}