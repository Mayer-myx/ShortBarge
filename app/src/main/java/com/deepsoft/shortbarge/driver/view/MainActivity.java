package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.fence.GeoFenceClient;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.MapsInitializer;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
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
    private SharedPreferences sp;
    private Observable<ResultGson> observable;//轮询任务用的观察者
    private MessageDialog messageDialog;
    private SettingDialog settingDialog;
    private WaitConnectDialog waitConnectDialog;
    private ConnectFailDialog connectFailDialog;

    private AMapLocation location;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private AMapLocationListener aMapLocationListener;
    private AMap aMap;
    private Circle circle1, circle2, circle3;
    private DPoint ori, dest, stop;
    private float ori_r, dest_r, dest_warn, stop_r;

    private DriverInfoGson currentDriverInfo;
    private TaskGson currentTask;
    private String truckId, driverId, lang;
    private int currentRetryCount = 0, waitRetryTime = 0, maxConnectCount = 10;// 当前已重试次数// 重试等待时间 //最大重试次数
    private static boolean isStart = false, isStopOver = false, isEnd = false, isAlter = false, isUpdate = true;//是否到达起始点

    private List<TaskGson> taskGsonList = new ArrayList<>();
    private MoreTaskAdapter moreTaskAdapter;

    private MapView main_mv_map;
    private TextView main_tv_arrive, main_tv_vm, main_tv_ts, main_tv_dest, main_tv_at, main_tv_st,
            main_tv_ec, main_tv_pn, main_tv_truck, main_tv_driver, main_tv_tasknum, main_tv_wea, main_tv_date;
    private ImageView main_iv_wea, main_iv_setting;
    private RecyclerView main_rv_tasks;
    private View main_v_isvm;


    @RequiresApi(api = Build.VERSION_CODES.O)
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

        EventBus.getDefault().register(this);
        WsManager.getInstance().init(token);
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);

        MapsInitializer.setApiKey("b79f7bfa929d029aaa6f8ddbd964dfcd");
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);

        initView();
        main_mv_map.onCreate(savedInstanceState);

        initLocationGaode();
        regestBroadcast();

        getDriverInfo();
        getDriverTask();
        getWeatherInfo();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        main_mv_map.onSaveInstanceState(outState);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initLocationGaode(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CHINA);
        aMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                            waitConnectDialog.dismiss();
                        settingDialog.setGps(getString(R.string.state_connected));

                        if(isStart && !isEnd && currentTask != null) {
                            float start_distance = CoordinateConverter.calculateLineDistance(ori,
                                    new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                            float end_distance = CoordinateConverter.calculateLineDistance(dest,
                                    new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                            float stop_distance = 0.0F;
                            if(currentTask.getStopOver()){
                                stop_distance = CoordinateConverter.calculateLineDistance(stop,
                                        new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                            }

                            if(end_distance <= dest_warn && !isAlter && currentTask.getState() > 2){
                                // 即将到达：经过起点 && 到预警围栏内 && 之前没预警过
                                sendNotice();
                                isAlter = true;
                                Toast.makeText(MainActivity.this, "即将到达"+"\nstart_distance="+start_distance+"\nend_distance"+end_distance+"\nacc="+aMapLocation.getAccuracy(), Toast.LENGTH_SHORT).show();
                            }

                            if(currentTask.getStopOver() && stop_distance <= stop_r){
                                //经停站
                                if(isStopOver && aMapLocation.getSpeed() <= 5){
                                    if(currentTask.getState() != 5) {
                                        changeTaskState(currentTask.getTransportTaskId(), 5);
                                        currentTask.setState(5);
                                        isStopOver = true;
                                        Toast.makeText(MainActivity.this, "经停站速度<=3 装卸货", Toast.LENGTH_SHORT).show();
                                    }else if(!isStopOver){
                                        if(currentTask.getState() != 6){
                                            changeTaskState(currentTask.getTransportTaskId(), 6);
                                            currentTask.setState(6);
                                            isStopOver = false;
                                            Toast.makeText(MainActivity.this, "经停站速度>3 继续运输中", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }

                            if(aMapLocation.getSpeed() <= 5){
                                if (start_distance <= ori_r) {//起点
                                    if(currentTask.getState() != 2) {
                                        changeTaskState(currentTask.getTransportTaskId(), 2);
                                        currentTask.setState(2);
//                                    Toast.makeText(MainActivity.this, "起点速度<=3 装卸货"+"\nstart_distance="+start_distance+"\nend_distance"+end_distance+"\nspeed="+aMapLocation.getSpeed(), Toast.LENGTH_SHORT).show();
                                    }
                                }else if(end_distance <= dest_r) {//终点
                                    if(currentTask.getState() != 5) {//不在经停站
                                        isEnd = true;
                                        isUpdate = false;
                                        changeTaskState(currentTask.getTransportTaskId(), 7);
                                        currentTask.setState(7);
                                        changeTaskState(currentTask.getTransportTaskId(), 8);
                                        currentTask.setState(8);
                                        LocalTime localTime = LocalTime.now();
                                        main_tv_at.setText(localTime.format(formatter));
                                        Toast.makeText(MainActivity.this, "终点速度<=3 完成任务!", Toast.LENGTH_SHORT).show();
                                    }
                                }else{//运输 红绿灯
                                    if(currentTask.getState() == 2) {
                                        changeTaskState(currentTask.getTransportTaskId(), 3);
                                        currentTask.setState(3);
                                    }
                                }
                            }else{//运输 动了
                                if(currentTask.getState() == 2) {
                                    changeTaskState(currentTask.getTransportTaskId(), 3);
                                    currentTask.setState(3);
                                }
                            }

//                            if (start_distance <= ori_r) {
//                                // 在起点 不动 装卸货
//                                if(currentTask.getState() != 2 && aMapLocation.getSpeed() <= 3) {
//                                    changeTaskState(currentTask.getTransportTaskId(), 2);
//                                    currentTask.setState(2);
////                                    Toast.makeText(MainActivity.this, "起点速度<=3 装卸货"+"\nstart_distance="+start_distance+"\nend_distance"+end_distance+"\nspeed="+aMapLocation.getSpeed(), Toast.LENGTH_SHORT).show();
//                                }else if(currentTask.getState() != 3 && aMapLocation.getSpeed() > 3){
//                                    changeTaskState(currentTask.getTransportTaskId(), 3);
//                                    currentTask.setState(3);
////                                    Toast.makeText(MainActivity.this, "起点速度>3 运输中"+"\nstart_distance="+start_distance+"\nend_distance"+end_distance+"\nspeed="+aMapLocation.getSpeed(), Toast.LENGTH_SHORT).show();
//                                }
//                            } else if (end_distance <= dest_r) {
//                                // 到达终点 不动 装卸货
//                                if(currentTask.getState() != 5 && aMapLocation.getSpeed() <= 3) {
//                                    isEnd = true;
//                                    isUpdate = false;
//                                    changeTaskState(currentTask.getTransportTaskId(), 8);
//                                    currentTask.setState(8);
//                                    LocalTime localTime = LocalTime.now();
//                                    main_tv_at.setText(localTime.format(formatter));
//                                    Toast.makeText(MainActivity.this, "终点速度<=3 完成任务!", Toast.LENGTH_SHORT).show();
//                                }else if(currentTask.getState() != 5){
//                                    if (currentTask.getState() != 3 && currentTask.getState() != 6) {
//                                        changeTaskState(currentTask.getTransportTaskId(), 3);
//                                        currentTask.setState(3);
//                                        Toast.makeText(MainActivity.this, "终点速度>3 运输中" + "\nstart_distance=" + start_distance + "\nend_distance" + end_distance + "\nspeed=" + aMapLocation.getSpeed(), Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            } else {
//                                // 不在起点 终点围栏 运输中
//                                if(currentTask.getState() != 3 && currentTask.getState() != 5 && currentTask.getState() != 6) {
//                                    changeTaskState(currentTask.getTransportTaskId(), 3);
//                                    currentTask.setState(3);
////                                    Toast.makeText(MainActivity.this, "不在起点终点 运输中" + "\nstart_distance=" + start_distance + "\nend_distance" + end_distance+"\nspeed="+aMapLocation.getSpeed(), Toast.LENGTH_SHORT).show();
//                                }
//                            }
                        }

                        location = aMapLocation;
                    }else {
                        Toast.makeText(MainActivity.this, "location Error, ErrCode:" + aMapLocation.getErrorCode()
                                + ", errInfo:" + aMapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:" + aMapLocation.getErrorCode()
                                + ", errInfo:" + aMapLocation.getErrorInfo());

                        if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                            waitConnectDialog.dismiss();
                        connectFailDialog.showConnectFailDialog();
                        settingDialog.setGps(getString(R.string.state_lost));
                    }
                }
            }
        };

        try {
            mLocationClient = new AMapLocationClient(this);
            mLocationClient.setApiKey("b79f7bfa929d029aaa6f8ddbd964dfcd");
            mLocationClient.setLocationListener(aMapLocationListener);
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setInterval(1000);
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


    private void drawGeofenceGaode(LatLng latLng1, LatLng latLng2){
        if(circle1 != null && circle2 != null && circle3 != null) {
            circle1.setCenter(latLng1);
            circle2.setCenter(latLng2);
            circle3.setCenter(latLng2);
        }else{
            circle1 = aMap.addCircle(new CircleOptions().center(latLng1)
                    .radius(Double.parseDouble(currentTask.getOriginFenceRange()))
                    .fillColor(0x500000FF)
                    .strokeWidth(1f));
            circle2 = aMap.addCircle(new CircleOptions().center(latLng2)
                    .radius(Double.parseDouble(currentTask.getDestinationFenceRange()))
                    .fillColor(0x50FFFF00)
                    .strokeWidth(1f)
                    .zIndex(2));
            circle3 = aMap.addCircle(new CircleOptions().center(latLng2)
                    .radius(Double.parseDouble(currentTask.getDestinationWarningRange()))
                    .fillColor(0x50F15C58)
                    .strokeWidth(1f)
                    .zIndex(1));
        }
    }


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


    private void setMaker(){
        BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.mipmap.scatter_car);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(custom);
        myLocationStyle.interval(1000);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(13));
    }


    private void initView(){
        main_tv_arrive = findViewById(R.id.main_tv_arrive);
        main_tv_arrive.setOnClickListener(this);
        PressUtil.setPressChange(this, main_tv_arrive);

        main_tv_vm = findViewById(R.id.main_tv_vm);
        main_tv_vm.setOnClickListener(this);
        PressUtil.setPressChange(this, main_tv_vm);

        main_mv_map = findViewById(R.id.main_mv_map);
        aMap = main_mv_map.getMap();

        main_rv_tasks = findViewById(R.id.main_rv_tasks);
        main_tv_tasknum = findViewById(R.id.main_tv_tasknum);

        // 有voice message
        main_tv_st = findViewById(R.id.main_tv_st);
        main_tv_at = findViewById(R.id.main_tv_at);
        main_tv_dest = findViewById(R.id.main_tv_dest);
        main_tv_ts = findViewById(R.id.main_tv_ts);

        // 汽车信息
        main_tv_ec = findViewById(R.id.main_tv_ec);
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
                    lang = sp.getString("locale_language", "en");
                    lang = lang.equals("en") ? "1": "2";
                    if(lang.equals("1")){
                        main_tv_driver.setText(driverInfoGson.getNameEng());
                    }else {
                        main_tv_driver.setText(driverInfoGson.getName());
                    }
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
                        List<TaskGson> tmp = new ArrayList<>();

                        if(!isStart && main_tv_arrive.getText().equals(getString(R.string.task_finish))){
                            if (taskGsonList != null && taskGsonList.size() != 0) {
                                main_tv_arrive.setText(R.string.task_start);
                                main_tv_arrive.setClickable(true);
                                main_tv_arrive.setAlpha(1.0F);
                            }
                        }

                        if(isUpdate) {// 暂停轮询更新字段
                            if (taskGsonList != null && taskGsonList.size() != 0) {//任务列表不为空
                                tmp = new ArrayList<>(taskGsonList);
                                tmp.remove(0);

                                //更新数据
                                if (currentTask == null //当前任务和请求任务列表的第一个是不同的
                                        || !currentTask.getTransportTaskId().equals(taskGsonList.get(0).getTransportTaskId())) {

                                    main_tv_st.setText(taskGsonList.get(0).getStartTime());
                                    main_tv_at.setText(taskGsonList.get(0).getArrivalTime());
                                }

                                currentTask = taskGsonList.get(0);
                                if (currentTask.getOriginLat().equals("") || currentTask.getOriginLat() == null
                                        || currentTask.getDestinationLat().equals("") || currentTask.getDestinationLat() == null) {
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

                                ori = new DPoint(Double.parseDouble(currentTask.getOriginLat()),
                                        Double.parseDouble(currentTask.getOriginLng()));
                                ori_r = Float.parseFloat(currentTask.getOriginFenceRange());
                                dest = new DPoint(Double.parseDouble(currentTask.getDestinationLat()),
                                        Double.parseDouble(currentTask.getDestinationLng()));
                                dest_r = Float.parseFloat(currentTask.getDestinationFenceRange());
                                dest_warn = Float.parseFloat(currentTask.getDestinationWarningRange());

                                if(currentTask.getStopOver()){
                                    stop = new DPoint(Double.parseDouble(currentTask.getStopLat()),
                                            Double.parseDouble(currentTask.getStopLng()));
                                    stop_r = Float.parseFloat(currentTask.getStopFenceRange());
                                }

                                if (lang.equals("1")) {
                                    main_tv_dest.setText(currentTask.getNextStationEng() + currentTask.getTaskDura(lang));
                                    main_tv_ec.setText(currentTask.getAdminNameEng());
                                } else {
                                    main_tv_dest.setText(currentTask.getNextStation() + currentTask.getTaskDura(lang));
                                    main_tv_ec.setText(currentTask.getAdminName());
                                }
                                main_tv_pn.setText(currentTask.getAdminPhone());
                                main_tv_ts.setText(currentTask.getTaskState(lang) + currentTask.getTaskStateDuration(lang));
                                main_tv_arrive.setClickable(true);

                                if(isStart) {
                                    if (taskGsonList.size() != 1) {
                                        main_tv_arrive.setText(R.string.task_continue);
                                    } else {
                                        main_tv_arrive.setText(R.string.task_finish);
                                    }
                                }
                            } else {//任务列表为空
                                currentTask = null;
                                main_tv_dest.setText("null");
                                main_tv_ts.setText("null");
                                main_tv_st.setText("null");
                                main_tv_at.setText("null");
                                main_tv_arrive.setText(R.string.task_finish);
                                main_tv_arrive.setClickable(false);
                                main_tv_arrive.setAlpha(0.5F);
                            }

                            main_tv_tasknum.setText("" + taskGsonList.size());
                            moreTaskAdapter.setList(tmp);
                        }

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


    private void getDriverTaskOnce(){
        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1": "2";
        apiInterface.getDriverTaskOnce().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "getDriverTaskOnce run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    taskGsonList = GsonConvertUtil.performTransform(resultGson.getData(), TaskGson.class);
                    List<TaskGson> tmp = new ArrayList<>();

                    if(taskGsonList != null && taskGsonList.size() != 0) {//任务列表不为空
                        tmp = new ArrayList<>(taskGsonList);
                        tmp.remove(0);

                        //更新数据
                        if(currentTask == null //当前任务和请求任务列表的第一个是不同的
                                || (!currentTask.getTransportTaskId().equals(taskGsonList.get(0).getTransportTaskId()))){

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

                            ori = new DPoint(Double.parseDouble(currentTask.getOriginLat()),
                                    Double.parseDouble(currentTask.getOriginLng()));
                            ori_r = Float.parseFloat(currentTask.getOriginFenceRange());
                            dest = new DPoint(Double.parseDouble(currentTask.getDestinationLat()),
                                    Double.parseDouble(currentTask.getDestinationLng()));
                            dest_r = Float.parseFloat(currentTask.getDestinationFenceRange());
                            dest_warn = Float.parseFloat(currentTask.getDestinationWarningRange());

                            main_tv_st.setText(currentTask.getStartTime());
                            main_tv_at.setText(currentTask.getArrivalTime());
                        }

                        if(lang.equals("1")) {
                            main_tv_dest.setText(currentTask.getNextStationEng() + currentTask.getTaskDura(lang));
                            main_tv_ec.setText(currentTask.getAdminNameEng());
                        }else{
                            main_tv_dest.setText(currentTask.getNextStation() + currentTask.getTaskDura(lang));
                            main_tv_ec.setText(currentTask.getAdminName());
                        }
                        main_tv_pn.setText(currentTask.getAdminPhone());
                        main_tv_ts.setText(currentTask.getTaskState(lang) + currentTask.getTaskStateDuration(lang));
                        main_tv_arrive.setClickable(true);

                        if(taskGsonList.size() != 1){
                            main_tv_arrive.setText(R.string.task_continue);
                        } else {
                            main_tv_arrive.setText(R.string.task_finish);
                        }

                    }else{//任务列表为空
                        currentTask = null;
                        main_tv_dest.setText("null");
                        main_tv_ts.setText("null");
                        main_tv_st.setText("null");
                        main_tv_at.setText("null");
                        main_tv_arrive.setText(R.string.task_finish);
                        main_tv_arrive.setClickable(false);
                        main_tv_arrive.setAlpha(0.5F);
                    }

                    main_tv_tasknum.setText("" + taskGsonList.size());
                    moreTaskAdapter.setList(tmp);

                    if(settingDialog != null) settingDialog.setServer(getString(R.string.state_connected));
                }else{
                    Log.i(TAG, "getDriverTaskOnce连接成功 数据申请失败， msg="+resultGson.getMsg());
                    if(settingDialog != null) settingDialog.setServer(getString(R.string.state_connected));
                }
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverTaskOnce onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                }
            }
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
                    main_tv_ts.setText(currentTask.getTaskState(lang) + currentTask.getTaskStateDuration(lang));
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
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new JSONObject(map).toString());
        apiInterface.sendNotice(requestBody).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Toast.makeText(MainActivity.this, "sendNotice run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "sendNotice run: post同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
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
                Toast.makeText(MainActivity.this, "sendNotice onFailure:"+t, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "sendNotice onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                }
            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
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
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CHINA);
        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1": "2";
        switch (view.getId()){
            case R.id.main_tv_arrive:
                isUpdate = true;
                String btn_text = main_tv_arrive.getText().toString();
                LocalTime localTime = LocalTime.now();
                if(btn_text.equals(getString(R.string.task_start))){//开始任务
                    if(location != null && currentTask != null
                            && !currentTask.getOriginLat().equals("")
                            && currentTask.getOriginLat() != null) {
                        isStart = true;
                        isEnd = false;
                        isStopOver = false;
                        isAlter = false;
                        main_tv_st.setText(localTime.format(formatter));

                        drawGeofenceGaode(new LatLng(Double.parseDouble(currentTask.getOriginLat()),Double.parseDouble(currentTask.getOriginLng())),
                                new LatLng(Double.parseDouble(currentTask.getDestinationLat()),Double.parseDouble(currentTask.getDestinationLng())));

                        if (taskGsonList.size() != 1) {
                            main_tv_arrive.setText(R.string.task_continue);
                        } else {
                            main_tv_arrive.setText(R.string.task_finish);
                        }
                    }else{
                        Toast.makeText(this, "还未获取当前定位或任务有问题", Toast.LENGTH_SHORT).show();
                    }
                } else if(btn_text.equals(getString(R.string.task_continue))) {
                    if(isEnd) {
                        Toast.makeText(this, "正在获取下一任务", Toast.LENGTH_SHORT).show();

                        getDriverTaskOnce();

                        isEnd = false;
                        isStopOver = false;
                        isAlter = false;
                        main_tv_st.setText(localTime.format(formatter));

                        drawGeofenceGaode(new LatLng(Double.parseDouble(currentTask.getOriginLat()),Double.parseDouble(currentTask.getOriginLng())),
                                new LatLng(Double.parseDouble(currentTask.getDestinationLat()),Double.parseDouble(currentTask.getDestinationLng())));

                        if (taskGsonList.size() == 1) {
                            main_tv_arrive.setText(R.string.task_finish);
                        }
                    }else{
                        Toast.makeText(this, getString(R.string.hint_check_load), Toast.LENGTH_SHORT).show();
                        if(location != null){
                            Toast.makeText(this, "当前经纬度："+location.getLongitude()+" "+location.getLatitude(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {//完成
                    if(isEnd) {
                        Toast.makeText(this, "所有任务完成！", Toast.LENGTH_SHORT).show();
                        main_tv_at.setText(localTime.format(formatter));

                        currentTask = null;
                        main_tv_dest.setText("null");
                        main_tv_ts.setText("null");
                        main_tv_st.setText("null");
                        main_tv_at.setText("null");

                        main_tv_arrive.setClickable(false);
                        main_tv_arrive.setAlpha(0.5F);
                        isStart = false;
                        isEnd = false;
                        isStopOver = false;
                        isAlter = false;

                        taskGsonList.remove(0);
                        main_tv_tasknum.setText("" + taskGsonList.size());
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