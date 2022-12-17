package com.deepsoft.shortbarge.driver.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MoreTaskAdapter;
import com.deepsoft.shortbarge.driver.adapter.entity.Task;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.gson.TaskGson;
import com.deepsoft.shortbarge.driver.service.ApiService;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtils;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtils;
import com.deepsoft.shortbarge.driver.widget.MyDialog;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "MainActivity";
    private ApiService apiService;

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

        apiService = RetrofitUtils.getInstance().getRetrofit().create(ApiService.class);

        initView();
//        getDriverTask();
    }

    private void initView(){
        main_tv_arrive = findViewById(R.id.main_tv_arrive);
        main_tv_arrive.setOnClickListener(this);
        PressUtils.setPressChange(this, main_tv_arrive);

        main_tv_vm = findViewById(R.id.main_tv_vm);
        main_tv_vm.setOnClickListener(this);
        PressUtils.setPressChange(this, main_tv_vm);

        main_mv_map = findViewById(R.id.main_mv_map);
        TencentMap mTencentMap = main_mv_map.getMap();
        //第一次渲染成功的回调
        mTencentMap.setOnMapLoadedCallback(new TencentMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                //地图正常显示
                Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_SHORT).show();
            }
        });
        mTencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);

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
                    Log.e(TAG, "run: get同步请求 "+ "code --- > "+response.body().getCode()+"msg  --- >"+response.body().getMsg());
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
        main_mv_map.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_tv_arrive:
                apiService.getLogout().enqueue(new Callback<ResultGson>() {
                    @Override
                    public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                        ResultGson resultGson = response.body();
                        Log.e(TAG, resultGson.toString());
                        Log.e(TAG, resultGson.getData());
                    }

                    @Override
                    public void onFailure(Call<ResultGson> call, Throwable t) {
                        Log.e(TAG, "onFailure:"+t);
                    }
                });
                break;
            case R.id.main_tv_vm:
                MessageDialog.showMessageDialog(this, getLayoutInflater());
                break;
        }
    }
}