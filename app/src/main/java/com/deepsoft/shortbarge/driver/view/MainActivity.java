package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
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
import com.amap.api.maps2d.model.PolylineOptions;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
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
import com.deepsoft.shortbarge.driver.widget.LogHandler;
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

    private ApiInterface apiInterface;
    private SharedPreferences sp;
    private Observable<ResultGson> observable;//轮询任务用的观察者
    private MessageDialog messageDialog;
    private SettingDialog settingDialog;
    private WaitConnectDialog waitConnectDialog;
    private ConnectFailDialog connectFailDialog;

    private AMapLocation location = null;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private AMapLocationListener aMapLocationListener;
    private AMap aMap;
    private Circle circle1, circle2, circle3, circle4;
    private DPoint ori, dest, stop;
    private LatLng latLng1 = new LatLng(30.548730,120.954280),
            latLng2 = new LatLng(30.549946763571644, 120.94785337363145),
            latLng3 = new LatLng(30.549113, 120.923496);
    private float ori_r, dest_r, dest_warn, stop_r, start_distance = -1.0F, end_distance = -1.0F, stop_distance = -1.0F;

    private DriverInfoGson currentDriverInfo;
    private TaskGson currentTask;
    private String truckNo, driverId, lang;
    private int currentRetryCount = 0, waitRetryTime = 0, maxConnectCount = 10;// 当前已重试次数// 重试等待时间 //最大重试次数
    private static boolean isStart = false, isStopOver = false, isAlter = false, isChange = false;

    private List<TaskGson> taskGsonList = new ArrayList<>(), whiteTaskList = new ArrayList<>();
    private MoreTaskAdapter moreTaskAdapter;

    private MapView main_mv_map;
    private TextView main_tv_arrive, main_tv_vm, main_tv_ts, main_tv_dest, main_tv_at, main_tv_st,
            main_tv_ec, main_tv_pn, main_tv_truck, main_tv_driver, main_tv_tasknum, main_tv_wea;
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
        aMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();

                    if (aMapLocation.getErrorCode() == 0 && aMapLocation.getAccuracy() <= 500) {
                        location = aMapLocation;
                        LogHandler.writeFile(TAG, "acc=" + aMapLocation.getAccuracy()+" type="+aMapLocation.getLocationType());
                        start_distance = CoordinateConverter.calculateLineDistance(ori,
                                new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                        end_distance = CoordinateConverter.calculateLineDistance(dest,
                                new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));

                        if(currentTask != null) {
                            if (currentTask.getStopOver())
                                stop_distance = CoordinateConverter.calculateLineDistance(stop,
                                        new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));

                            LogHandler.writeFile(TAG, "start="+start_distance+" end="+end_distance+" stop="+stop_distance);
                            LogHandler.writeFile(TAG, "isStart=" + isStart + " isChange="+isChange+" currentTask.getState()=" + currentTask.getState());

                            if (isStart) {
                                // 即将到达：经过起点 && 到预警围栏内 && 之前没预警过
                                if (end_distance <= dest_warn && !isAlter && currentTask.getState() > 2 && isChange) {
                                    LogHandler.writeFile(TAG, "即将到达 预警");
                                    sendNotice(currentTask.getTransportTaskId());
                                    isAlter = true;
                                    isChange = false;
                                }

                                // 有经停站
                                if (currentTask.getStopOver()){
                                    if(aMapLocation.getSpeed() <= 4) {
                                        // 经停站装卸货5：在经停站范围内 && 未停过 && 速度小 && 运输中3
                                        if (stop_distance >= 0 && stop_distance <= stop_r && currentTask.getState() == 3 && !isStopOver) {
                                            LogHandler.writeFile(TAG, "经停站装卸货");
                                            changeTaskState(currentTask.getTransportTaskId(), 5);
                                            isStopOver = true;
                                            Toast.makeText(MainActivity.this, "经停站装卸货", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {// 经停站继续运输6：停过了 && 状态5
                                        if (currentTask.getState() == 5 && isStopOver) {
                                            LogHandler.writeFile(TAG, "经停站继续运输");
                                            changeTaskState(currentTask.getTransportTaskId(), 6);
                                            Toast.makeText(MainActivity.this, "经停站继续运输", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                float dis1 = CoordinateConverter.calculateLineDistance(new DPoint(30.548730,120.954280),
                                        new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                                float dis2 = CoordinateConverter.calculateLineDistance(new DPoint(30.549946763571644, 120.94785337363145),
                                        new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                                float dis3 = CoordinateConverter.calculateLineDistance(new DPoint(30.549113, 120.923496),
                                        new DPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                                if(dis1 <= 30 || dis2 <= 30 || dis3 <= 30) {
                                    if (currentTask.getState() == 2) {
                                        LogHandler.writeFile(TAG, "进入辅助围栏 运输中");
                                        changeTaskState(currentTask.getTransportTaskId(), 3);
                                        isChange = true;
                                        Toast.makeText(MainActivity.this, "进入辅助围栏", Toast.LENGTH_SHORT).show();
                                    }
                                }else if (start_distance <= ori_r) {//起点
                                    if (currentTask.getState() == 1) {//待运输
                                        LogHandler.writeFile(TAG, "起点装卸货");
                                        changeTaskState(currentTask.getTransportTaskId(), 2);
                                        Toast.makeText(MainActivity.this, "起点装卸货", Toast.LENGTH_SHORT).show();
                                    }
                                }
//                                else {//运输
//                                    if (currentTask.getState() == 2) {//起点装卸货
//                                        LogHandler.writeFile(TAG, "运输中");
//                                        changeTaskState(currentTask.getTransportTaskId(), 3);
//                                    }
//                                }
                            }
                        }
                    }else {
                        //Toast.makeText(MainActivity.this, "location Error, ErrCode:" + aMapLocation.getErrorCode()
                        //        + ", errInfo:" + aMapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:" + aMapLocation.getErrorCode()
                                + ", errInfo:" + aMapLocation.getErrorInfo());
                        LogHandler.writeFile("AmapError","location Error, ErrCode:" + aMapLocation.getErrorCode()
                                + ", errInfo:" + aMapLocation.getErrorInfo());
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
            mLocationOption.setOnceLocationLatest(true);
            if(null != mLocationClient){
                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.stopLocation();
                mLocationClient.startLocation();
                setMaker();
            }
        }catch (Exception e){
        }
    }


    private void clearCircle(){
        if(circle1 != null) circle1.remove();
        if(circle2 != null) circle2.remove();
        if(circle3 != null) circle3.remove();
        if(circle4 != null) circle4.remove();
        circle1 = null;
        circle2 = null;
        circle3 = null;
        circle4 = null;
    }


    private void drawGeofenceGaode(LatLng latLng1, LatLng latLng2, double r1, double r2, double r3){
        if(circle1 == null && circle2 == null && circle3 == null) {
            Log.e(TAG, "null");
            circle1 = aMap.addCircle(new CircleOptions().center(latLng1)
                    .radius(r1)
                    .fillColor(0x50FFFF00)
                    .strokeWidth(1f));
            circle2 = aMap.addCircle(new CircleOptions().center(latLng2)
                    .radius(r2)
                    .fillColor(0x50FFFF00)
                    .strokeWidth(1f)
                    .zIndex(2));
            circle3 = aMap.addCircle(new CircleOptions().center(latLng2)
                    .radius(r3)//预警
                    .fillColor(0x50F15C58)
                    .strokeWidth(1f)
                    .zIndex(1));
        }else{
            circle1.setCenter(latLng1);
            circle1.setRadius(r1);
            circle2.setCenter(latLng2);
            circle2.setRadius(r2);
            circle3.setCenter(latLng2);
            circle3.setRadius(r3);
            Log.e(TAG, "circle1="+circle1.getCenter()+" "+circle1.getRadius());
            Log.e(TAG, "circle2="+circle2.getCenter()+" "+circle2.getRadius());
            Log.e(TAG, "circle3="+circle3.getCenter()+" "+circle3.getRadius());
        }
    }


    private void drawGeofenceStop(LatLng latLng, double r){
        if(circle4 == null) {
            circle4 = aMap.addCircle(new CircleOptions().center(latLng)
                    .radius(r)
                    .fillColor(0x50FFFF00)
                    .strokeWidth(1f));
        }else{
            circle4.setCenter(latLng);
            circle4.setRadius(r);
        }
    }


    private void drawLine(){
        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(30.54995, 120.922067));
        latLngs.add(new LatLng(30.548846, 120.923982));
        latLngs.add(new LatLng(30.547312, 120.926642));
        latLngs.add(new LatLng(30.548606, 120.927844));
        latLngs.add(new LatLng(30.545963, 120.931835));
        latLngs.add(new LatLng(30.551803, 120.945997));
        latLngs.add(new LatLng(30.549881, 120.945997));
        latLngs.add(new LatLng(30.549493, 120.954323));
        latLngs.add(new LatLng(30.548116, 120.954205));
        aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(10).color(Color.rgb(226, 0, 15)));
    }


    private void setMaker(){
        BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.mipmap.scatter_car);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(custom);
        myLocationStyle.interval(1000);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        drawLine();
        helpCirecle();
        if(!currentTask.getOriginLat().equals("") && currentTask.getOriginLat() != null)
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.5*Double.parseDouble(currentTask.getOriginLat()) + 0.5*Double.parseDouble(currentTask.getDestinationLat()),
                    0.5*Double.parseDouble(currentTask.getOriginLng()) + 0.5*Double.parseDouble(currentTask.getDestinationLng())), 15));
        else
            aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }


    private void helpCirecle(){
        aMap.addCircle(new CircleOptions().center(latLng1)
                .radius(30)
                .fillColor(0x50FF00FF)
                .strokeWidth(1f));
        aMap.addCircle(new CircleOptions().center(latLng2)
                .radius(30)
                .fillColor(0x50FF00FF)
                .strokeWidth(1f));
        aMap.addCircle(new CircleOptions().center(latLng3)
                .radius(30)
                .fillColor(0x50FF00FF)
                .strokeWidth(1f));
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
        moreTaskAdapter = new MoreTaskAdapter(R.layout.item_more_task, whiteTaskList, lang);
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
                    truckNo = ""+driverInfoGson.getTruckNo();
                    if(truckNo.length() == 1) truckNo = "0"+ truckNo;
                    driverId = ""+driverInfoGson.getDriverId();
                    if(driverId.length() == 1) driverId = "0"+driverId;
                    main_tv_truck.setText(truckNo);
                    messageDialog = new MessageDialog(MainActivity.this, driverId);

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
                if(connectFailDialog != null && connectFailDialog.getIsShow())
                    connectFailDialog.dismiss();

            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverInfo onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
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
//                    main_tv_date.setText(weatherGson.getDate());
                    main_iv_wea.setColorFilter(Color.WHITE);
                    main_iv_wea.setImageResource(getResources().getIdentifier("wea_"+weatherGson.getIcon(), "drawable",
                            BaseApplication.getApplication().getPackageName()));
                }else{
                    Log.i(TAG, "getWeatherInfo连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
                if(connectFailDialog != null && connectFailDialog.getIsShow())
                    connectFailDialog.dismiss();
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
     * 30s 轮询更新 任务列表
     */
    private void getDriverTask(){
        observable = apiInterface.getDriverTask();
        observable.retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Throwable throwable) throws Exception {
//                        if(currentRetryCount < maxConnectCount) {
                        if(throwable instanceof ConnectException){
                            if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                                waitConnectDialog.dismiss();
                            connectFailDialog.showConnectFailDialog();
                        }else{
                            if(waitConnectDialog != null && !waitConnectDialog.getIsShow())
                                waitConnectDialog.showWaitConnectDialog(MainActivity.this, getLayoutInflater());
                        }
                        Log.d(TAG, "发生异常 = " + throwable.toString());
                        currentRetryCount++;
                        Log.d(TAG, "重试次数 = " + currentRetryCount);
                        waitRetryTime = 1 + currentRetryCount;
                        Log.d(TAG, "等待时间 = " + waitRetryTime);
                        LogHandler.writeFile(TAG, "发生异常 = " + throwable.toString()+"重试次数 = " + currentRetryCount+"等待时间 = " + waitRetryTime);
                        return Observable.just(1).delay(waitRetryTime, TimeUnit.SECONDS);
//                        }else{
//                            if(waitConnectDialog != null && waitConnectDialog.getIsShow())
//                                waitConnectDialog.dismiss();
//                            connectFailDialog.showConnectFailDialog();
//                            return Observable.error(new Throwable("重试次数已超过设置次数 = " +currentRetryCount  + "，即不再重试"));
//                        }
                    }
                });
            }
        }).repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Object throwable) throws Exception {
                        // 注：此处加入了delay操作符，作用 = 延迟一段时间发送（此处设置 = 30s），以实现轮询间间隔设置
                        return Observable.just(1).delay(30 , TimeUnit.SECONDS);
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
                        Log.e(TAG, ""+resultGson.getData());

                        if(!isStart && taskGsonList != null && taskGsonList.size() != 0){
                            if (main_tv_arrive.getText().equals(getString(R.string.task_finish))) {
                                main_tv_arrive.setText(R.string.task_start);
                                main_tv_arrive.setClickable(true);
                                main_tv_arrive.setAlpha(1.0F);
                            }
                            if (taskGsonList.get(0).getState() != 1) {
                                if (taskGsonList.size() != 1) main_tv_arrive.setText(R.string.task_continue);
                                else main_tv_arrive.setText(R.string.task_finish);
                                isStart = true;
                                main_tv_arrive.setClickable(true);
                                main_tv_arrive.setAlpha(1.0F);
                            }
                        }

                        if (taskGsonList != null && taskGsonList.size() != 0) {//任务列表不为空
                            whiteTaskList = new ArrayList<>(taskGsonList);
                            whiteTaskList.remove(0);

                            //更新数据
                            if (currentTask == null //当前任务和请求任务列表的第一个是不同的
                                    || !currentTask.getTransportTaskId().equals(taskGsonList.get(0).getTransportTaskId())) {

                                main_tv_st.setText(taskGsonList.get(0).getStartTime());
                                main_tv_at.setText(taskGsonList.get(0).getArrivalTime());
                            }

                            currentTask = taskGsonList.get(0);
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

                            if(currentTask.getState() != 1){
                                if(currentTask.getState() > 2) isChange = true;
                                clearCircle();
                                drawGeofenceGaode(new LatLng(Double.parseDouble(currentTask.getOriginLat()),Double.parseDouble(currentTask.getOriginLng())),
                                        new LatLng(Double.parseDouble(currentTask.getDestinationLat()),Double.parseDouble(currentTask.getDestinationLng())),
                                        Double.parseDouble(currentTask.getOriginFenceRange()),
                                        Double.parseDouble(currentTask.getDestinationFenceRange()),
                                        Double.parseDouble(currentTask.getDestinationWarningRange()));
                                if(currentTask.getStopOver())
                                    drawGeofenceStop(new LatLng(Double.parseDouble(currentTask.getStopLat()),Double.parseDouble(currentTask.getStopLng())),
                                            Double.parseDouble(currentTask.getStopFenceRange()));
                            }

                            if(aMap != null)
                                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.5*Double.parseDouble(currentTask.getOriginLat()) + 0.5*Double.parseDouble(currentTask.getDestinationLat()),
                                        0.5*Double.parseDouble(currentTask.getOriginLng()) + 0.5*Double.parseDouble(currentTask.getDestinationLng())), 15));

                            if(isStart) {
                                if (taskGsonList.size() != 1) main_tv_arrive.setText(R.string.task_continue);
                                else main_tv_arrive.setText(R.string.task_finish);
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
                        moreTaskAdapter.setList(whiteTaskList);

                        if(settingDialog != null) settingDialog.setServer(getString(R.string.state_connected));
                    }else{
                        if(settingDialog != null) settingDialog.setServer(getString(R.string.state_lost));
                        Log.i(TAG, "getDriverTask连接成功 数据申请失败， msg="+resultGson.getMsg());
                        LogHandler.writeFile(TAG, "getDriverTask连接成功 数据申请失败， msg="+resultGson.getMsg());
                    }

                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    if(connectFailDialog != null && connectFailDialog.getIsShow())
                        connectFailDialog.dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "getDriverTask onFailure:"+e.toString());
                    LogHandler.writeFile(TAG, "getDriverTask onFailure:"+e.toString());
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


    /**
     * 点击按钮 立即更新：任务列表
     */
    private void getDriverTaskOnce(String startTime){
        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1": "2";
        apiInterface.getDriverTaskOnce().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "getDriverTaskOnce run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    taskGsonList = GsonConvertUtil.performTransform(resultGson.getData(), TaskGson.class);

                    if(taskGsonList != null && taskGsonList.size() != 0) {
                        whiteTaskList = new ArrayList<>(taskGsonList);
                        whiteTaskList.remove(0);

                        currentTask = taskGsonList.get(0);
                        ori = new DPoint(Double.parseDouble(currentTask.getOriginLat()),
                                Double.parseDouble(currentTask.getOriginLng()));
                        ori_r = Float.parseFloat(currentTask.getOriginFenceRange());
                        dest = new DPoint(Double.parseDouble(currentTask.getDestinationLat()),
                                Double.parseDouble(currentTask.getDestinationLng()));
                        dest_r = Float.parseFloat(currentTask.getDestinationFenceRange());
                        dest_warn = Float.parseFloat(currentTask.getDestinationWarningRange());

                        if(lang.equals("1")) {
                            main_tv_dest.setText(currentTask.getNextStationEng() + currentTask.getTaskDura(lang));
                            main_tv_ec.setText(currentTask.getAdminNameEng());
                        }else{
                            main_tv_dest.setText(currentTask.getNextStation() + currentTask.getTaskDura(lang));
                            main_tv_ec.setText(currentTask.getAdminName());
                        }
                        main_tv_st.setText(startTime);
                        main_tv_at.setText(currentTask.getArrivalTime());
                        main_tv_pn.setText(currentTask.getAdminPhone());
                        main_tv_ts.setText(currentTask.getTaskState(lang) + currentTask.getTaskStateDuration(lang));
                        main_tv_arrive.setClickable(true);

                        if(aMap != null)
                            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.5*Double.parseDouble(currentTask.getOriginLat()) + 0.5*Double.parseDouble(currentTask.getDestinationLat()),
                                    0.5*Double.parseDouble(currentTask.getOriginLng()) + 0.5*Double.parseDouble(currentTask.getDestinationLng())), 15));

                        clearCircle();
                        drawGeofenceGaode(new LatLng(Double.parseDouble(currentTask.getOriginLat()),Double.parseDouble(currentTask.getOriginLng())),
                                new LatLng(Double.parseDouble(currentTask.getDestinationLat()),Double.parseDouble(currentTask.getDestinationLng())),
                                Double.parseDouble(currentTask.getOriginFenceRange()),
                                Double.parseDouble(currentTask.getDestinationFenceRange()),
                                Double.parseDouble(currentTask.getDestinationWarningRange()));
                        if(currentTask.getStopOver())
                            drawGeofenceStop(new LatLng(Double.parseDouble(currentTask.getStopLat()),Double.parseDouble(currentTask.getStopLng())),
                                    Double.parseDouble(currentTask.getStopFenceRange()));

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
                    moreTaskAdapter.setList(whiteTaskList);
                    if (taskGsonList == null || taskGsonList.size() != 1) main_tv_arrive.setText(R.string.task_continue);
                    else main_tv_arrive.setText(R.string.task_finish);
                }else{
                    Log.i(TAG, "getDriverTaskOnce连接成功 数据申请失败， msg="+resultGson.getMsg());
                    LogHandler.writeFile(TAG, "getDriverTaskOnce连接成功 数据申请失败， msg="+resultGson.getMsg());
                }

                if(settingDialog != null) settingDialog.setServer(getString(R.string.state_connected));

                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverTaskOnce onFailure:"+t);
                LogHandler.writeFile(TAG, "getDriverTaskOnce onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                }
            }
        });
    }


    /**
     * 任务状态切换 立即更新：状态 目的地 紧急联系人 电话
     */
    private void getDriverTaskUpdate(){
        apiInterface.getDriverTaskOnce().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    taskGsonList = GsonConvertUtil.performTransform(resultGson.getData(), TaskGson.class);
                    lang = sp.getString("locale_language", "en");
                    lang = lang.equals("en") ? "1": "2";
                    if (taskGsonList != null && taskGsonList.size() != 0) { //任务列表不为空
                        whiteTaskList = new ArrayList<>(taskGsonList);
                        whiteTaskList.remove(0);
                        currentTask = taskGsonList.get(0);
                        if (lang.equals("1")) {
                            main_tv_ec.setText(currentTask.getAdminNameEng());
                            main_tv_dest.setText(currentTask.getNextStationEng() + currentTask.getTaskDura(lang));
                        } else {
                            main_tv_ec.setText(currentTask.getAdminName());
                            main_tv_dest.setText(currentTask.getNextStation() + currentTask.getTaskDura(lang));
                        }
                        main_tv_pn.setText(currentTask.getAdminPhone());
                        main_tv_ts.setText(currentTask.getTaskState(lang) + currentTask.getTaskStateDuration(lang));
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {}
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void changeTaskState(String transportTaskId, Integer state){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CHINA);
        apiInterface.changeTaskState(transportTaskId, state).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "changeTaskState run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    if(state != 7){
                        currentTask.setState(state);
                        getDriverTaskUpdate();
                    }
                    else {
                        LogHandler.writeFile(TAG, "changeTaskState getSuccess"+transportTaskId+" state="+state+" isAlter="+isAlter+" isStopOver="+isStopOver);
                        main_tv_at.setText(LocalTime.now().format(formatter));
                        isStopOver = false;
                        isAlter = false;
                        isChange = false;
                        if(main_tv_arrive.getText().toString().equals(getString(R.string.task_continue))){//继续
                            LogHandler.writeFile(TAG, "继续"+transportTaskId);
                            Toast.makeText(MainActivity.this, transportTaskId + "完成任务", Toast.LENGTH_SHORT).show();

                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    Looper.prepare();
                                    try {
                                        Thread.sleep(500);
                                        getDriverTaskOnce(LocalTime.now().format(formatter));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }else{//完成
                            LogHandler.writeFile(TAG, "完成"+transportTaskId);
                            Toast.makeText(MainActivity.this, "当前所有任务完成！", Toast.LENGTH_SHORT).show();
                            clearCircle();

                            main_tv_dest.setText("null");
                            main_tv_ts.setText("null");
                            main_tv_st.setText("null");
                            main_tv_at.setText("null");

                            main_tv_arrive.setClickable(false);
                            main_tv_arrive.setAlpha(0.5F);
                            isStart = false;
                            currentTask = null;

                            if(taskGsonList.size() != 0) {
                                taskGsonList = new ArrayList<>();
                                whiteTaskList = new ArrayList<>();
                            }
                            main_tv_tasknum.setText("" + taskGsonList.size());
                        }
                    }
                } else {
                    if(currentTask != null) {
                        LogHandler.writeFile(TAG, "当前状态码" + currentTask.getState() + "\n" + transportTaskId + "任务 状态码" + state + " 提交失败" + resultGson.getMsg());
                        Toast.makeText(MainActivity.this, "当前状态码" + currentTask.getState() + "\n"
                                + transportTaskId + "任务 状态码" + state + " 提交失败" + resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                    }
//                    if(state == 7) changeTaskState(transportTaskId, state);
                    Log.i(TAG, "changeTaskState连接成功 数据申请失败， msg="+resultGson.getMsg());
                    LogHandler.writeFile(TAG, "changeTaskState连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
                if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                    waitConnectDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "changeTaskState onFailure:"+t);
                LogHandler.writeFile(TAG, "changeTaskState onFailure:"+t);
                if(t instanceof ConnectException){
                    if(waitConnectDialog != null && waitConnectDialog.getIsShow())
                        waitConnectDialog.dismiss();
                    connectFailDialog.showConnectFailDialog();
                }
            }
        });
    }


    private void sendNotice(String transportTaskId){
        HashMap<String, Object> map = new HashMap<>();
        map.put("transportTaskId", transportTaskId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new JSONObject(map).toString());
        apiInterface.sendNotice(requestBody).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.i(TAG, "sendNotice run: post同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    Log.i(TAG, "sendNotice连接成功 数据申请成功， msg="+resultGson.getMsg());
                    LogHandler.writeFile(TAG, "sendNotice连接成功 数据申请成功， msg="+resultGson.getMsg());
                }else{
                    Log.i(TAG, "sendNotice连接成功 数据申请失败， msg="+resultGson.getMsg());
                    LogHandler.writeFile(TAG, "sendNotice连接成功 数据申请失败， msg="+resultGson.getMsg());
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
                String btn_text = main_tv_arrive.getText().toString();
                if(btn_text.equals(getString(R.string.task_start))){//开始任务
                    if(location != null && currentTask != null) {
                        isStart = true;
                        isStopOver = false;
                        isAlter = false;

                        getDriverTaskOnce(LocalTime.now().format(formatter));
                    }else{
                        Toast.makeText(this, "还未获取当前定位或任务有问题", Toast.LENGTH_SHORT).show();
                    }
                } else {//继续 完成
                    if (location != null && currentTask != null) {
                        end_distance = CoordinateConverter.calculateLineDistance(dest,
                                new DPoint(location.getLatitude(), location.getLongitude()));
                        if (end_distance <= dest_r)
                            changeTaskState(currentTask.getTransportTaskId(), 7);
                        else Toast.makeText(this, "没到终点", Toast.LENGTH_SHORT).show();
                        LogHandler.writeFile(TAG, "点击继续/完成 end_distance=" + end_distance + " dest_r" + dest_r + " end_distance <= dest_r?" + (end_distance <= dest_r));
                    }else{
                        Toast.makeText(this, "还未获取当前定位或任务有问题", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.main_tv_vm:
                messageDialog.showMessageDialog(this, getLayoutInflater());
                main_v_isvm.setVisibility(View.INVISIBLE);
                break;
            case R.id.main_iv_setting:
                settingDialog.showSettingDialog(this, getLayoutInflater());
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MessageResponse messageResponse){
        if(messageResponse.getType() == 1){//返回经纬度
            if(location != null && currentDriverInfo != null) {
                if(currentTask == null || currentTask.getState() != 2){
                    WsManager.getInstance().sendReq(new Action("{\"driverId\":" + currentDriverInfo.getDriverId()
                            + ",\"truckId\":" + currentDriverInfo.getTruckId()
                            + ",\"lng\":" + location.getLongitude()
                            + ",\"lat\":" + location.getLatitude() + "}", 1, null));
                    LogHandler.writeFile(TAG, "经纬度 发送真实信息");
                }else {
                    WsManager.getInstance().sendReq(new Action("{\"driverId\":" + currentDriverInfo.getDriverId()
                            + ",\"truckId\":" + currentDriverInfo.getTruckId()
                            + ",\"lng\":" + currentTask.getOriginLng()
                            + ",\"lat\":" + currentTask.getOriginLat() + "}", 1, null));
                    LogHandler.writeFile(TAG, "经纬度 发送起点信息");
                }
            }
        }else if(messageResponse.getType() == 2){
            //聊天消息
            WsManager.getInstance().sendReq(new Action(messageResponse.getMessage(), 3, null));
            Log.e(TAG, "收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
            LogHandler.writeFile(TAG, "收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
            main_v_isvm.setVisibility(View.VISIBLE);
            main_tv_vm.setAlpha(1F);
        }else{
            Log.e(TAG, "else收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
            LogHandler.writeFile(TAG, "else收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
        }
    }


    /**
     * 链接错误dialog
     */
    private class ConnectFailDialog extends MyDialog {

        private boolean isShow = false;

        public ConnectFailDialog(@NonNull Context context) {
            super(context);
        }

        public ConnectFailDialog(@NonNull Context context, int themeResId) {
            super(context, themeResId);
        }

        public boolean getIsShow(){
            return isShow;
        }

        public void showConnectFailDialog(){
            View dialog_wait_connect = getLayoutInflater().inflate(R.layout.dialog_connect_fail, null);
            this.setContentView(dialog_wait_connect);
            this.show();
            isShow = true;

            TextView dialog_fail_tv_exit = dialog_wait_connect.findViewById(R.id.dialog_fail_tv_exit);
            dialog_fail_tv_exit.setOnClickListener(v->{
                this.dismiss();
                MainActivity.this.finish();
                MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
            });
            TextView dialog_fail_tv_retry = dialog_wait_connect.findViewById(R.id.dialog_fail_tv_retry);
            dialog_fail_tv_retry.setOnClickListener(v->{
                this.dismiss();
                Looper.prepare();
                getDriverInfo();
                getDriverTask();
                getWeatherInfo();
            });
        }


        @Override
        public void dismiss() {
            this.isShow = false;
            super.dismiss();
        }
    }
}