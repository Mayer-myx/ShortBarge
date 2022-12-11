package com.deepsoft.shortbarge.driver.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.widget.MyDialog;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;

public class MainActivity extends AppCompatActivity {

    private MapView main_mv_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_main);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        initView();
        showWaitConnectDialog();
    }

    private void initView(){
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
    }

    private void showWaitConnectDialog(){
        View dialog_wait_connect = this.getLayoutInflater().inflate(R.layout.dialog_wait_connect, null);
        final MyDialog dialog = new MyDialog(this);
        dialog.setContentView(dialog_wait_connect);
        dialog.show();
        //放在show()之后，不然有些属性是没有效果的，比如height和width
        Window dialogWindow = dialog.getWindow();
        WindowManager m = getWindowManager();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.height = 720;
        p.width = 1000;
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);
        dialog.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        main_mv_map.onStart();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        main_mv_map.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        main_mv_map.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        main_mv_map.onStop();
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        main_mv_map.onRestart();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        main_mv_map.onDestroy();
    }

}