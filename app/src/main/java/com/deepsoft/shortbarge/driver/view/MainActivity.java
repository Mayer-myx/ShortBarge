package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
import com.deepsoft.shortbarge.driver.bean.GeoInfo;
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
import com.tencent.map.geolocation.TencentGeofence;
import com.tencent.map.geolocation.TencentGeofenceManager;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

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
    private TencentMap mTencentMap;
    private Marker mCustomMarker = null;
    private TencentLocationManager mLocationManager;
    private TencentLocationRequest request;
    private TencentLocation location;
    private TencentGeofenceManager geofenceManager;
    private TencentGeofence.Builder builder;
    private TencentGeofence startGeofence, endGeofence, arriveGeofence1, arriveGeofence2;
    private StartGeofenceEventReceiver startGeofenceEventReceiver;
    private EndGeofenceEventReceiver endGeofenceEventReceiver;
    private ArrivalGeofenceEventReceiver arrivalGeofenceEventReceiver;

    private MessageDialog messageDialog;
    private SettingDialog settingDialog;
    private WaitConnectDialog waitConnectDialog;
    private ConnectFailDialog connectFailDialog;
    private DriverInfoGson currentDriverInfo;
    private TaskGson currentTask;
    private String truckId, driverId, lang;
    private SharedPreferences sp;
    private int currentRetryCount = 0, waitRetryTime = 0, maxConnectCount = 10;// 当前已重试次数// 重试等待时间 //最大重试次数
    private boolean isStart = false;//是否到达起始点
    private Observable<ResultGson> observable;//轮询任务用的观察者

    private List<TaskGson> taskGsonList = new ArrayList<>();
    private MoreTaskAdapter moreTaskAdapter;
    private List<GeoInfo> geoInfoList = new ArrayList<>();
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

    private TencentLocationListener tencentLocationListener;


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

        TencentLocationManager.setUserAgreePrivacy(true);
        EventBus.getDefault().register(this);
        WsManager.getInstance().init(token);
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);

        initView();
        initData();

        regestBroadcast();
        initLocation();

        getDriverInfo();
        getDriverTask();
        getWeatherInfo();
    }


    private static boolean isAlter = false;
    private void initLocation(){
        mLocationManager = TencentLocationManager.getInstance(this);
        tencentLocationListener = new TencentLocationListener() {
            @Override
            public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
                if (TencentLocation.ERROR_OK == i) {
                    if (tencentLocation != null) {
                        initMap(tencentLocation.getLatitude(), tencentLocation.getLongitude());
                        setMaker(tencentLocation.getLatitude(), tencentLocation.getLongitude());

                        if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                            waitConnectDialog.dismiss();
                        settingDialog.setGps(getString(R.string.state_connected));

                        if(isStart) {
                            if (!endGeofenceEventReceiver.getIsEnter() && startGeofenceEventReceiver.getIsEnter()) {
                                // 在起点的地理围栏处 不动 装卸货
                                if(location != null
                                    && location.getLatitude() == tencentLocation.getLatitude()
                                    && location.getLongitude() == tencentLocation.getLongitude()
                                    && currentTask.getState() != 2) {
                                        changeTaskState(currentTask.getTransportTaskId(), 2);
                                        currentTask.setState(2);
                                }
                            } else if (!endGeofenceEventReceiver.getIsEnter() && !startGeofenceEventReceiver.getIsEnter()) {
                                // 退出起点终点地理围栏 则运输中
                                if(location != null
                                    && location.getLatitude() != tencentLocation.getLatitude()
                                    && location.getLongitude() != tencentLocation.getLongitude()
                                    && currentTask.getState() != 3) {
                                        changeTaskState(currentTask.getTransportTaskId(), 3);
                                        currentTask.setState(3);
                                        isAlter = false;
                                }
                            } else if (endGeofenceEventReceiver.getIsEnter() && !startGeofenceEventReceiver.getIsEnter()) {
                                if(location != null
                                    && location.getLatitude() == tencentLocation.getLatitude()
                                    && location.getLongitude() == tencentLocation.getLongitude()) {
                                        // 到达终点且不动 装卸货
                                        if(currentTask.getState() != 2) {
                                            changeTaskState(currentTask.getTransportTaskId(), 2);
                                            currentTask.setState(2);
                                        }
                                }else {
                                    //在终点动了and经停站 继续运输
                                    changeTaskState(currentTask.getTransportTaskId(), 3);
                                    currentTask.setState(3);
                                }
                            }

                            if (arrivalGeofenceEventReceiver.getIsEnter() && !isAlter) {
                                // 即将到达
                                sendNotice();
                                isAlter = true;
                                Toast.makeText(MainActivity.this, "即将到达！", Toast.LENGTH_SHORT).show();
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
        request.setInterval(2*1000)
                .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME)
                .setAllowGPS(true)
                .setAllowDirection(true)
                .setIndoorLocationMode(true)
                .setLocMode(TencentLocationRequest.HIGH_ACCURACY_MODE)
                .setGpsFirst(true)
                .setGpsFirstTimeOut(5*1000)
                .setAllowCache(true);

        int error = mLocationManager.requestLocationUpdates(request, tencentLocationListener);
        if (error == 0){
            Log.i(TAG, "注册位置监听器成功！");
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
    }


    private void regestBroadcast(){
        startGeofenceEventReceiver = new StartGeofenceEventReceiver();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(ACTION_TRIGGER_GEOFENCE_START);
        registerReceiver(startGeofenceEventReceiver, intentFilter1);

        endGeofenceEventReceiver = new EndGeofenceEventReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(ACTION_TRIGGER_GEOFENCE_END);
        registerReceiver(endGeofenceEventReceiver, intentFilter2);

        arrivalGeofenceEventReceiver = new ArrivalGeofenceEventReceiver();
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction(ACTION_TRIGGER_GEOFENCE_ARRIVAL);
        registerReceiver(arrivalGeofenceEventReceiver, intentFilter4);

        geofenceManager = new TencentGeofenceManager(this);
        builder = new TencentGeofence.Builder();
    }


    private void initGeofence(String sta){
        geofenceManager.removeAllFences();

        startGeofence = builder.setTag("start") // 设置 Tag，即围栏别名
                .setCircularRegion(location.getLatitude(),
                        location.getLongitude(),
                        500) // 设置圆心和半径，纬度，经度，半径 500米
                .build();
        Intent receiver1 = new Intent(ACTION_TRIGGER_GEOFENCE_START);
        receiver1.putExtra("KEY_GEOFENCE_ID", startGeofence.getTag());
        receiver1.putExtra("KEY_GEOFENCE_LAT", startGeofence.getLatitude());
        receiver1.putExtra("KEY_GEOFENCE_LNG", startGeofence.getLongitude());
        PendingIntent pi1 = PendingIntent.getBroadcast(this, (int) (Math.random() * 1E7),
                receiver1, PendingIntent.FLAG_UPDATE_CURRENT);
        geofenceManager.addFence(startGeofence, pi1);

        endGeofence = builder.setTag("end")
                .setCircularRegion(geoInfoList.get(realgeo.get(sta)).getLat(),
                        geoInfoList.get(realgeo.get(sta)).getLng(),
                        geoInfoList.get(realgeo.get(sta)).getR())
                .build();
        Intent receiver2 = new Intent(ACTION_TRIGGER_GEOFENCE_END);
        receiver2.putExtra("KEY_GEOFENCE_ID", endGeofence.getTag());
        receiver2.putExtra("KEY_GEOFENCE_LAT", endGeofence.getLatitude());
        receiver2.putExtra("KEY_GEOFENCE_LNG", endGeofence.getLongitude());
        PendingIntent pi2 = PendingIntent.getBroadcast(this, (int) (Math.random() * 1E7),
                receiver2, PendingIntent.FLAG_UPDATE_CURRENT);
        geofenceManager.addFence(startGeofence, pi2);

        arriveGeofence2 = builder.setTag("arr2")
                .setCircularRegion(geoInfoList.get(realgeo.get(sta)).getLat(),
                        geoInfoList.get(realgeo.get(sta)).getLng(),
                        2000)
                .build();
        Intent receiver4 = new Intent(ACTION_TRIGGER_GEOFENCE_ARRIVAL);
        receiver4.putExtra("KEY_GEOFENCE_ID", arriveGeofence2.getTag());
        receiver4.putExtra("KEY_GEOFENCE_LAT", arriveGeofence2.getLatitude());
        receiver4.putExtra("KEY_GEOFENCE_LNG", arriveGeofence2.getLongitude());
        PendingIntent pi4 = PendingIntent.getBroadcast(this, (int) (Math.random() * 1E7),
                receiver4, PendingIntent.FLAG_UPDATE_CURRENT);
        geofenceManager.addFence(arriveGeofence2, pi4);
    }


    private void initMap(double lat, double lon){
        CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                new LatLng(lat, lon), //中心点坐标，地图目标经纬度
                15,         //目标缩放级别
                0f,             //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0f));          //目标旋转角 0~360° (正北方为0)
        mTencentMap.moveCamera(cameraSigma);
        mTencentMap.setOnMapLoadedCallback(new TencentMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                //第一次渲染成功的回调
                Log.i(TAG, "map ok");
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
                settingDialog.setGps(getString(R.string.state_connected));
            }
        });

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
        mTencentMap = main_mv_map.getMap();

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


    private void initData(){
        //MCHE 车间
        geoInfoList.add(new GeoInfo(120.954953, 30.557037, 209F));
        //BPHE
        geoInfoList.add(new GeoInfo(120.961169, 30.554363, 86F));
        //VACON
        geoInfoList.add(new GeoInfo(120.960136, 30.554674, 66F));

        //2B-1/2 仓库
        geoInfoList.add(new GeoInfo(120.930238, 30.556578, 45F));
        //2A-1/2
        geoInfoList.add(new GeoInfo(120.929712, 30.5569, 40F));
        //1D-1/2 1C-1/2
        geoInfoList.add(new GeoInfo(120.928751,30.557507, 50F));
        //1C-3/4
        geoInfoList.add(new GeoInfo(120.928086,30.556515, 50F));

        realgeo.put("MCHE", 0);
        realgeo.put("BPHE", 1);
        realgeo.put("VACON", 2);
        realgeo.put("2B-1/2", 3);
        realgeo.put("2A-1/2", 4);
        realgeo.put("1D-1/2", 5);
        realgeo.put("1C-1/2", 5);
        realgeo.put("1C-3/4", 6);
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
                        main_tv_ec.setText(driverInfoGson.getEmergencyContactEng());
                        main_tv_driver.setText(driverInfoGson.getNameEng());
                    }else {
                        main_tv_ec.setText(driverInfoGson.getEmergencyContact());
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
                            if(currentTask == null //当前任务和请求任务列表的第一个不同
                                    || (!currentTask.getTransportTaskId().equals(taskGsonList.get(0).getTransportTaskId()))){
                                isStart = false;
                                main_tv_st.setText(taskGsonList.get(0).getStartTime());
                                main_tv_at.setText(taskGsonList.get(0).getArrivalTime());
                            }
                            currentTask = taskGsonList.get(0);
                            if (location != null) {
                                initGeofence(currentTask.getNextStationEng());
                            }

                            if(lang.equals("1")) {
                                main_tv_dest.setText(currentTask.getNextStationEng() + currentTask.getTaskDura(lang));
                            }else{
                                main_tv_dest.setText(currentTask.getNextStation() + currentTask.getTaskDura(lang));
                            }
                            main_tv_ts.setText(currentTask.getTaskState(lang));
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


    private void setMaker(double lat, double lon){
        LatLng position = new LatLng(lat, lon);
        BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.mipmap.scatter_car);
        if(mCustomMarker == null) {
            mCustomMarker = mTencentMap.addMarker(new MarkerOptions(position)
                    .icon(custom)
                    .flat(true));
        }else {
            mCustomMarker.setPosition(position);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        main_mv_map.onStart();
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
        main_mv_map.onStop();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        main_mv_map.onRestart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(geofenceManager != null) {
            geofenceManager.removeAllFences();
            geofenceManager.destroy();
        }
        if(mCustomMarker != null) {
            mCustomMarker.remove();
            mCustomMarker = null;
        }
        if(tencentLocationListener != null) {
            mLocationManager.removeUpdates(tencentLocationListener);
        }
        mLocationManager = null;
        request = null;
//        locationChangedListener = null;
        main_mv_map.onDestroy();
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
        switch (view.getId()){
            case R.id.main_tv_arrive:
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss", Locale.CHINA);
                lang = sp.getString("locale_language", "en");
                lang = lang.equals("en") ? "1": "2";
                String btn_text = main_tv_arrive.getText().toString();
                if(btn_text.equals(getString(R.string.task_start))){//开始任务
                    isStart = true;
                    LocalTime localTime = LocalTime.now();
                    main_tv_st.setText(localTime.format(formatter));
                    if(taskGsonList.size() == 1){
                        main_tv_arrive.setText(R.string.task_finish);
                    }else {
                        main_tv_arrive.setText(R.string.task_continue);
                    }
                }else{//完成
                    if(currentTask.getState() == 2) {//装卸货状态后才能完成
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
//                settingDialog.showSettingDialog(this, getLayoutInflater());
//                break;
//            case R.id.main_mv_map:
                mLocationManager.requestSingleFreshLocation(null, tencentLocationListener, Looper.getMainLooper());
                Toast.makeText(this, "定位当前位置", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MessageResponse messageResponse){
        if(messageResponse.getType() == 1 && location != null){
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


//    private LocationSource.OnLocationChangedListener locationChangedListener;
//    private LocationSource locationSource = new LocationSource() {
//        @Override
//        public void activate(OnLocationChangedListener onLocationChangedListener) {
//            locationChangedListener = onLocationChangedListener;
//            int err = mLocationManager.requestLocationUpdates(request, tencentLocationListener, Looper.myLooper());
//            switch (err) {
//                case 1:
//                    Log.e(TAG, "设备缺少使用腾讯定位服务需要的基本条件");
//                    break;
//                case 2:
//                    Log.e(TAG, "manifest 中配置的 key 不正确");
//                    break;
//                case 3:
//                    Log.e(TAG, "自动加载libtencentloc.so失败");
//                    break;
//            }
//        }
//
//        @Override
//        public void deactivate() {
//            //当不需要展示定位点时，需要停止定位并释放相关资源
//            if(tencentLocationListener != null) {
//                mLocationManager.removeUpdates(tencentLocationListener);
//            }
//            mLocationManager = null;
//            request = null;
//            locationChangedListener = null;
//        }
//    };


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