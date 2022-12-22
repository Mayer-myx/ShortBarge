package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.gson.TaskGson;
import com.deepsoft.shortbarge.driver.gson.TaskList;
import com.deepsoft.shortbarge.driver.gson.WeatherGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.LocationUtil;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.utils.GPS.GPSLocationManager;
import com.google.gson.Gson;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "MainActivity";
    private ApiInterface apiInterface;
    private GPSLocationManager gpsLocationManager;
    private static Location myLocation;
    private TencentMap mTencentMap;
    private LocationUtil locationUtils;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String driverId, truckId, licensePlate, emergencyContact, emergencyContactEng, emergencyPhone;

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
        getUserName();
        getIntentData();
        getDriverTask();
        getWeatherInfo();

        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        initMap(Double.valueOf(sp.getString("Latitude", "")), Double.valueOf(sp.getString("Longitude", "")));

        locationUtils = LocationUtil.getInstance(MainActivity.this);
        locationUtils.getLocation(new LocationUtil.LocationCallBack() {
            @Override
            public void setLocation(Location location) {
                if (location != null){
                    Log.e(TAG, "经度:" + location.getLongitude());
                    Log.e(TAG, "纬度:" + location.getLatitude());
                    editor = sp.edit();
                    editor.putString("Longitude",String.valueOf(location.getLongitude()));
                    editor.putString("Latitude",String.valueOf(location.getLatitude()));
                    editor.commit();    // 更新经纬度
                    myLocation = location;
                    initMap(myLocation.getLatitude(), myLocation.getLongitude());
                }
            }
        }, 1);
        setMaker(30.306225,120.36931666666666);
//        gpsLocationManager = GPSLocationManager.getInstances(MainActivity.this);
//        gpsLocationManagersLocationManager.start(new MyListener());
    }


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


    private void getIntentData(){
        Intent intent = getIntent();
        driverId = intent.getStringExtra("driverId");
        truckId = intent.getStringExtra("truckId");
        licensePlate = intent.getStringExtra("licensePlate");
        emergencyContact = intent.getStringExtra("emergencyContact");
        emergencyContactEng = intent.getStringExtra("emergencyContactEng");
        emergencyPhone = intent.getStringExtra("emergencyPhone");

        main_tv_ln.setText(licensePlate);
        main_tv_ec.setText(emergencyContact + emergencyContactEng);
        main_tv_pn.setText(emergencyPhone);
        if(truckId.length() == 1) truckId = "0"+truckId;
        main_tv_truck.setText(truckId);
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
    }


    @Override
    protected void onPause() {
        super.onPause();
        main_mv_map.onPause();
//        gpsLocationManager.stop();
    }


    @Override
    protected void onStop() {
        super.onStop();
        main_mv_map.onStop();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        locationUtils.stop();
        main_mv_map.onRestart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        main_mv_map.onDestroy();
        locationUtils.stop();
//        gpsLocationManager.stop();
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