package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
import com.deepsoft.shortbarge.driver.adapter.entity.Task;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.gson.TaskGson;
import com.deepsoft.shortbarge.driver.service.ApiService;
import com.deepsoft.shortbarge.driver.utils.LocationUtils;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtils;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.utils.GPS.GPSLocationListener;
import com.deepsoft.shortbarge.driver.utils.GPS.GPSLocationManager;
import com.deepsoft.shortbarge.driver.utils.GPS.GPSProviderStatus;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.AlphaAnimation;
import com.tencent.tencentmap.mapsdk.maps.model.Animation;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "MainActivity";
    private ApiService apiService;
    private GPSLocationManager gpsLocationManager;
    private static Location myLocation;
    private TencentMap mTencentMap;
    private LocationUtils locationUtils;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private List<Task> taskList = new ArrayList<>();
    private MoreTaskAdapter moreTaskAdapter;

    private MapView main_mv_map;
    private TextView main_tv_arrive, main_tv_vm;
    private RecyclerView main_rv_tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_main);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        initView();
//        getDriverTask();


        // 已经申请过权限，做想做的事
        Log.e(TAG, "已获得权限");
        apiService = RetrofitUtil.getInstance().getRetrofit().create(ApiService.class);
        sp = getSharedPreferences("Di-Truck", Context.MODE_PRIVATE);
        initMap(Double.valueOf(sp.getString("Latitude", "")), Double.valueOf(sp.getString("Longitude", "")));

        locationUtils = LocationUtils.getInstance(MainActivity.this);
        locationUtils.getLocation(new LocationUtils.LocationCallBack() {
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
        PressUtils.setPressChange(this, main_tv_arrive);

        main_tv_vm = findViewById(R.id.main_tv_vm);
        main_tv_vm.setOnClickListener(this);
        PressUtils.setPressChange(this, main_tv_vm);

        main_mv_map = findViewById(R.id.main_mv_map);
        mTencentMap = main_mv_map.getMap();

        main_rv_tasks = findViewById(R.id.main_rv_tasks);
        moreTaskAdapter = new MoreTaskAdapter(R.layout.item_more_task, taskList);
        main_rv_tasks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        main_rv_tasks.setAdapter(moreTaskAdapter);
    }


    private void getDriverTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<TaskGson>response = apiService.getDriverTask().execute();
                    Log.i(TAG, "run: get同步请求 "+ "code --- > "+response.body().getCode()+"msg  --- >"+response.body().getMsg());
                    TaskGson taskGson = response.body();
                    for(int i = 0; i < taskGson.getData().size(); i++){
                        TaskGson.DataDTO dataDTO = taskGson.getData().get(i);
                        taskList.add(new Task(dataDTO.getTransportTaskId(), dataDTO.getState(), dataDTO.getStartTime(),
                                dataDTO.getArrivalTime(), dataDTO.getDuration(), dataDTO.getNextStation(), dataDTO.getNextStationEng()));
                    }
                    moreTaskAdapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
        locationUtils.stop();
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
        apiService.getLogout().enqueue(new Callback<ResultGson>() {
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