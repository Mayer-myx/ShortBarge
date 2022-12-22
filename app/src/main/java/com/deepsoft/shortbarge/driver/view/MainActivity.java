package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
import com.deepsoft.shortbarge.driver.gson.DriverInfoGson;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.gson.TaskGson;
import com.deepsoft.shortbarge.driver.gson.WeatherGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "MainActivity";
    private ApiInterface apiInterface;
    private TencentMap mTencentMap;

    private LocationManager mLocationMgr;
    private Criteria mCriteria = new Criteria();
    private Handler mHandler = new Handler();
    private boolean isLocationEnable = false;

    private List<TaskGson> taskGsonList = new ArrayList<>();
    private MoreTaskAdapter moreTaskAdapter;

    private MapView main_mv_map;
    private TextView main_tv_arrive, main_tv_vm, main_tv_st_label, main_tv_at_label, main_tv_d_label,
            main_tv_ts_label, main_tv_wt_label, main_tv_ns_label, main_tv_wt, main_tv_ns,
            main_tv_ts, main_tv_dest, main_tv_at, main_tv_st, main_tv_at2_label, main_tv_at2,
            main_tv_ec, main_tv_ln, main_tv_pn, main_tv_truck, main_tv_driver, main_tv_tasknum,
            main_tv_wea, main_tv_date;
    private ImageView main_iv_wea;
    private RecyclerView main_rv_tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_main);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        initView();
        getDriverInfo();
        getUserName();
        getDriverTask();
        getWeatherInfo();
    }


    /**
     * 初始化定位服务
     */
    private void initLocation() {
        // 从系统服务中获取定位管理器
        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 设置定位精确度。Criteria.ACCURACY_COARSE表示粗略，Criteria.ACCURACY_FIN表示精细
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否需要海拔信息
        mCriteria.setAltitudeRequired(true);
        // 设置是否需要方位信息
        mCriteria.setBearingRequired(true);
        // 设置是否允许运营商收费
        mCriteria.setCostAllowed(true);
        // 设置对电源的需求
        mCriteria.setPowerRequirement(Criteria.POWER_LOW);
        // 获取定位管理器的最佳定位提供者
        String bestProvider = mLocationMgr.getBestProvider(mCriteria, true);
        if (mLocationMgr.isProviderEnabled(bestProvider)) { // 定位提供者当前可用
            Toast.makeText(this, "正在获取" + bestProvider + "定位对象", Toast.LENGTH_SHORT).show();
            Log.e(TAG, String.format("定位类型=%s", bestProvider));
            beginLocation(bestProvider);
            isLocationEnable = true;
        } else { // 定位提供者暂不可用
            Toast.makeText(MainActivity.this, ""+bestProvider+"定位不可用", Toast.LENGTH_SHORT).show();
            isLocationEnable = false;
        }
    }

    /**
     * 设置定位结果文本
     */
    private void setLocationText(Location location) {
        if (location != null) {
            Log.e(TAG, String.format("定位对象经度：%f，纬度：%f，速度：%f米，精度：%d米",
                    location.getLongitude(), location.getLatitude(),
                    location.getSpeed(), Math.round(location.getAccuracy())));

            initMap(location.getLatitude(), location.getLongitude());
            setMaker(location.getLatitude(), location.getLongitude());
        } else {
            Toast.makeText(this, "暂未获取到定位对象", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始定位
     */
    private void beginLocation(String method) {
        // 检查当前设备是否已经开启了定位功能
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请授予定位权限并开启定位功能", Toast.LENGTH_SHORT).show();
            return;
        }
        // 设置定位管理器的位置变更监听器
        mLocationMgr.requestLocationUpdates(method, 300, 0, mLocationListener);
        // 获取最后一次成功定位的位置信息
        Location location = mLocationMgr.getLastKnownLocation(method);
        setLocationText(location);
    }

    /**
     * 定义一个位置变更监听器
     */
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setLocationText(location);
        }

        @Override
        public void onProviderDisabled(String arg0) {}

        @Override
        public void onProviderEnabled(String arg0) {}

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
    };

    /**
     * 定义一个刷新任务，若无法定位则每隔一秒就尝试定位
     */
    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            if (!isLocationEnable) {
                initLocation();
                mHandler.postDelayed(this, 1000);
            }
        }
    };


    private void initMap(double lat, double lon){
        CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                new LatLng(lat, lon), //中心点坐标，地图目标经纬度
                15,         //目标缩放级别
                0f,             //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0f));           //目标旋转角 0~360° (正北方为0)
        mTencentMap.moveCamera(cameraSigma);
        //第一次渲染成功的回调
        mTencentMap.setOnMapLoadedCallback(new TencentMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                //地图正常显示
                Log.e(TAG, "map ok");
            }
        });
        mTencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);
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
                    main_tv_ln.setText(driverInfoGson.getLicensePlate());
                    main_tv_ec.setText(driverInfoGson.getEmergencyContactEng());
                    main_tv_pn.setText(driverInfoGson.getEmergencyPhone());
                    String truckId = driverInfoGson.getTruckId();
                    if(truckId.length() == 1) truckId = "0"+truckId;
                    main_tv_truck.setText(truckId);
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
        apiInterface.getWeatherInfo("2").enqueue(new Callback<ResultGson>() {
            // todo: 这里我直接写了1-中文，2-英文，但是这个实际上要根据系统的！
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                Log.e(TAG, "getWeatherInfo run: get同步请求 " + "code=" + response.body().getCode() + " msg=" + response.body().getMsg());
                ResultGson resultGson = response.body();
                if (resultGson.getSuccess()) {
                    List<WeatherGson> list = GsonConvertUtil.performTransform(resultGson.getData(), WeatherGson.class);
                    WeatherGson weatherGson = list.get(0);
                    main_tv_wea.setText(weatherGson.getWeather() + " " + weatherGson.getTemperature());
                    main_tv_date.setText(weatherGson.getDate());
                    Glide.with(MainActivity.this)
                            .load(weatherGson.getIcon())
                            .into(main_iv_wea);
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
                    moreTaskAdapter = new MoreTaskAdapter(R.layout.item_more_task, taskGsonList);
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


    private void setMaker(double lat, double lon){
        //创建Marker对象之前，设置属性
        LatLng position = new LatLng(lat, lon);
        BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.mipmap.scatter_car);
        Marker mCustomMarker = mTencentMap.addMarker(new MarkerOptions(position)
                .icon(custom)
                .alpha(0.7f)
                .flat(true)
                .clockwise(false)
                .rotation(0));
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

        mHandler.removeCallbacks(mRefresh); // 移除定位刷新任务
        initLocation();
        mHandler.postDelayed(mRefresh, 100); // 延迟100毫秒启动定位刷新任务
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
        main_mv_map.onDestroy();
        if (mLocationMgr != null) {
            // 移除定位管理器的位置变更监听器
            mLocationMgr.removeUpdates(mLocationListener);
        }

        apiInterface.getLogout().enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                ResultGson resultGson = response.body();
                Log.i(TAG, "onResponse: "+resultGson.getMsg());
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.i(TAG, "onFailure: "+t);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_tv_arrive:
                WaitConnectDialog.getInstance().showWaitConnectDialog(this, getLayoutInflater());
                break;
            case R.id.main_tv_vm:
                MessageDialog.getInstance().showMessageDialog(this, getLayoutInflater());
                break;
        }
    }
}