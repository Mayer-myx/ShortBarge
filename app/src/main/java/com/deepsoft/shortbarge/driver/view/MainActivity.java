package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
import com.deepsoft.shortbarge.driver.constant.Action;
import com.deepsoft.shortbarge.driver.constant.WsStatus;
import com.deepsoft.shortbarge.driver.gson.DriverInfoGson;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.gson.TaskGson;
import com.deepsoft.shortbarge.driver.gson.UserInfoGson;
import com.deepsoft.shortbarge.driver.gson.WeatherGson;
import com.deepsoft.shortbarge.driver.gson.message.MessageResponse;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.websocket.WsManager;
import com.deepsoft.shortbarge.driver.widget.BaseApplication;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TencentLocationListener {

    private final static String TAG = "MainActivity";

    private ApiInterface apiInterface;
    private TencentMap mTencentMap;
    private Marker mCustomMarker = null;
    private TencentLocationManager mLocationManager;
    private TencentLocationRequest request;
    private TencentLocation tencentLocation;

    private MessageDialog messageDialog;
    private SettingDialog settingDialog;
    private DriverInfoGson currentDriverInfo;
    private TaskGson currentTask;
    private String truckId, driverId, lang;
    private UserInfoGson currentUserInfoGson;
    private SharedPreferences sp;

    private List<TaskGson> taskGsonList = new ArrayList<>();
    private MoreTaskAdapter moreTaskAdapter;

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

        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");

        TencentLocationManager.setUserAgreePrivacy(true);
        EventBus.getDefault().register(this);
        WsManager.getInstance().init(token);

        initView();
        initLocation();

        getDriverInfo();
        getDriverTask();
        getWeatherInfo();
    }


    private void initLocation(){
        request = TencentLocationRequest.create();
        request.setInterval(10000)
                .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME)
                .setAllowGPS(true)
                .setAllowDirection(true)
                .setIndoorLocationMode(true)
                .setLocMode(TencentLocationRequest.HIGH_ACCURACY_MODE)
                .setGpsFirst(true)
                .setGpsFirstTimeOut(5*1000)
                .setAllowCache(true);

        mLocationManager = TencentLocationManager.getInstance(this);
        int error = mLocationManager.requestLocationUpdates(request, this);
        if (error == 0)
            Log.e(TAG, "注册位置监听器成功！");
        else
            Log.e(TAG, "注册位置监听器失败！");
    }


    private void initMap(double lat, double lon){
        CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                new LatLng(lat, lon), //中心点坐标，地图目标经纬度
                15,         //目标缩放级别
                0f,             //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0f));          //目标旋转角 0~360° (正北方为0)
        mTencentMap.moveCamera(cameraSigma);
        //第一次渲染成功的回调
        mTencentMap.setOnMapLoadedCallback(new TencentMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                Log.e(TAG, "map ok");
            }
        });

        //地图上设置定位数据源
        mTencentMap.setLocationSource(locationSource);
        //设置当前位置可见
        mTencentMap.setMyLocationEnabled(true);

        //SDK版本4.3.5新增内置定位标点击回调监听
        mTencentMap.setMyLocationClickListener(new TencentMap.OnMyLocationClickListener() {
            @Override
            public boolean onMyLocationClicked(LatLng latLng) {
                Log.i(TAG, "内置定位标点击回调");
                return true;
            }
        });
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
    }


    private void getDriverInfo(){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
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
                }else{
                    Toast.makeText(MainActivity.this, "getDriverInfo连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverInfo onFailure:"+t);
            }
        });
    }


    private void getWeatherInfo(){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        lang = sp.getString("locale_language", "en");
        lang = lang.equals("en") ? "1": "2";
        String wea = lang.equals("1") ? "2" : "1";
        apiInterface.getWeatherInfo(wea).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.e(TAG, "getWeatherInfo run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    List<WeatherGson> list = GsonConvertUtil.performTransform(resultGson.getData(), WeatherGson.class);
                    WeatherGson weatherGson = list.get(0);
                    main_tv_wea.setText(weatherGson.getWeather() + " " + weatherGson.getTemperature());
                    main_tv_date.setText(weatherGson.getDate());
                    main_iv_wea.setColorFilter(Color.WHITE);
                    Log.e(TAG, ""+weatherGson.getIcon());
                    main_iv_wea.setImageResource(getResources().getIdentifier("wea_"+weatherGson.getIcon(), "drawable",
                            BaseApplication.getApplication().getPackageName()));
                }else{
                    Toast.makeText(MainActivity.this, "getWeatherInfo连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getWeatherInfo onFailure:"+t);
            }
        });
    }


    private void getUserName(){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.getUserName().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.e(TAG, "getUserName run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
//                    currentName = resultGson.getData().toString();
                    main_tv_driver.setText(resultGson.getData().toString());
                }else{
                    Toast.makeText(MainActivity.this, "getUserName连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getUserName onFailure:"+t);
            }
        });
    }


    private void getDriverTask(){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.getDriverTask().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.e(TAG, "getDriverTask run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    taskGsonList = GsonConvertUtil.performTransform(resultGson.getData(), TaskGson.class);
                    currentTask = taskGsonList.get(0);
                    lang = sp.getString("locale_language", "en");
                    lang = lang.equals("en") ? "1": "2";
                    main_tv_st.setText(currentTask.getStartTime());
                    main_tv_at.setText(currentTask.getArrivalTime());
                    main_tv_dest.setText(currentTask.getTaskDura(lang));
                    main_tv_ts.setText(""+currentTask.getTaskState(lang));
                    moreTaskAdapter = new MoreTaskAdapter(R.layout.item_more_task, taskGsonList, lang);
                    main_rv_tasks.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    main_rv_tasks.setAdapter(moreTaskAdapter);
                    main_tv_tasknum.setText(""+taskGsonList.size());
                }else{
                    Toast.makeText(MainActivity.this, "getDriverTask连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getDriverTask onFailure:"+t);
            }
        });
    }


    private void getUserDetail(String id){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.getUserDetail(id).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.e(TAG, "getUserDetail run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    List<UserInfoGson> list = GsonConvertUtil.performTransform(resultGson.getData(), UserInfoGson.class);
                    currentUserInfoGson = list.get(0);
                }else{
                    Toast.makeText(MainActivity.this, "getUserDetail连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getUserDetail onFailure:"+t);
            }
        });
    }


    private void changeTaskState(String transportTaskId, Integer state){
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.changeTaskState(transportTaskId, state).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.e(TAG, "changeTaskState run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    currentTask.setState(state);
                    lang = sp.getString("locale_language", "en");
                    lang = lang.equals("en") ? "1": "2";
                    main_tv_ts.setText(currentTask.getTaskState(lang));
                    moreTaskAdapter.notifyItemChanged(0);
                }else{
                    Toast.makeText(MainActivity.this, "changeTaskState连接成功 数据申请失败， msg="+resultGson.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "changeTaskState onFailure:"+t);
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
        if(mCustomMarker != null) {
            mCustomMarker.remove();
            mCustomMarker = null;
        }
        main_mv_map.onDestroy();
        if(settingDialog != null){
            settingDialog.dismiss();
        }
        if(messageDialog != null){
            messageDialog.dismiss();
        }
        EventBus.getDefault().unregister(this);
        WsManager.getInstance().disconnect();
//        mLocationManager.removeUpdates(MainActivity.this);
        mLocationManager = null;
        request = null;
        locationChangedListener = null;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_tv_arrive:
//                WaitConnectDialog.getInstance().showWaitConnectDialog(this, getLayoutInflater());
                changeTaskState(currentTask.getTransportTaskId(), 8);
                break;
            case R.id.main_tv_vm:
                messageDialog.showMessageDialog(this, getLayoutInflater());
                main_v_isvm.setVisibility(View.INVISIBLE);
                break;
            case R.id.main_iv_setting:
                settingDialog = new SettingDialog(MainActivity.this, currentDriverInfo, currentUserInfoGson);
                settingDialog.showSettingDialog(this, getLayoutInflater());
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MessageResponse messageResponse){
        if(messageResponse.getType() == 1 && tencentLocation != null){
            //返回经纬度
            Action action = new Action("{\"message\":\"{\"driverId\":"+driverId
                    +",\"truckId\":"+truckId
                    +",\"lng\":"+tencentLocation.getLongitude()
                    +",\"lat\":"+tencentLocation.getLatitude()+"}\",\"type\":1}", 1, null);
            WsManager.getInstance().sendReq(action);
        }else if(messageResponse.getType() == 2){
            //聊天消息
            Toast.makeText(this, "有聊天消息", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
            main_v_isvm.setVisibility(View.VISIBLE);
            messageDialog.addData(messageResponse);
        }else{
            Log.e(TAG, "收到消息type="+messageResponse.getType()+"\tmsg="+messageResponse.getMessage());
        }
    }


    /**
     * 位置更新时的回调\
     * @param tencentLocation 新的位置
     * @param i               错误码
     * @param s               错误描述
     */
    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (TencentLocation.ERROR_OK == i) {
            if (tencentLocation != null) {
                if(this.tencentLocation == null){
                    initMap(tencentLocation.getLatitude(), tencentLocation.getLongitude());
                }
                this.tencentLocation = tencentLocation;
                String lat = String.valueOf(tencentLocation.getLatitude());
                String lon = String.valueOf(tencentLocation.getLongitude());
                Log.e(TAG, lat + "---" + lon);
                setMaker(tencentLocation.getLatitude(), tencentLocation.getLongitude());
            }
        } else {
            Log.e(TAG, "定位失败"+i+" "+s);
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
    }

    private LocationSource.OnLocationChangedListener locationChangedListener;
    private LocationSource locationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            locationChangedListener = onLocationChangedListener;
            //开启定位
            int err = mLocationManager.requestLocationUpdates(request, MainActivity.this, Looper.myLooper());
            switch (err) {
                case 1:
                    Log.e(TAG, "设备缺少使用腾讯定位服务需要的基本条件");
                    break;
                case 2:
                    Log.e(TAG, "manifest 中配置的 key 不正确");
                    break;
                case 3:
                    Log.e(TAG, "自动加载libtencentloc.so失败");
                    break;
            }
        }

        @Override
        public void deactivate() {
            //当不需要展示定位点时，需要停止定位并释放相关资源
//            mLocationManager.removeUpdates(MainActivity.this);
            mLocationManager = null;
            request = null;
            locationChangedListener = null;
        }
    };
}