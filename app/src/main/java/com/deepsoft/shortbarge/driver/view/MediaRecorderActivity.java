package com.deepsoft.shortbarge.driver.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MediaRecorderActivity extends AppCompatActivity implements View.OnClickListener, Chronometer.OnChronometerTickListener {

    private static final String TAG = "MediaRecorderActivity";

    private Button btnStart, btnEnd, btnPlay, btnGoPause;
    private Chronometer chronometer;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private File mFile;
    private String filename, name;
    private int pause, current = 0;
    public static String FormatMiss(int time) {
        String hh = time / 3600 > 9 ? time / 3600 + "" : "0" + time / 3600;
        String mm = (time % 3600) / 60 > 9 ? (time % 3600) / 60 + "" : "0" + (time % 3600) / 60;
        String ss = (time % 3600) % 60 > 9 ? (time % 3600) % 60 + "" : "0" + (time % 3600) % 60;
        return hh + ":" + mm + ":" + ss;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_media_recorder);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        mFile = new File(this.getApplication().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "record");
        if(mFile.exists()){
            Log.i(TAG, "Directory exist");
        } else if (mFile == null || !mFile.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        initView();
        initData();
    }

    private void initView(){
        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);
        btnEnd = findViewById(R.id.btn_end);
        btnEnd.setOnClickListener(this);
        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        btnGoPause = findViewById(R.id.btn_go_pause);
        btnGoPause.setOnClickListener(this);
        chronometer = findViewById(R.id.chronometer);
    }


    private void initData() {
        //正数计时设置初始值（重置）
        chronometer.setBase(0);
        //正数计时事件监听器，时间发生变化时可进行操作
        chronometer.setOnChronometerTickListener(this);
        //设置格式(默认"MM:SS"格式)
        chronometer.setFormat("%s");
        chronometer.setText(FormatMiss(current));
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                current++;
                chronometer.setText(FormatMiss(current));
            }
        });
    }


    //开始录制
    public void startSound() {
        name = "audio";
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = dateFormat.format(new Date());
        filename = mFile.getPath() + "/" + time + name + ".mp3";
        mMediaRecorder.setOutputFile(filename);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioEncodingBitRate(192000);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare()failed");
            e.printStackTrace();
        }
        mMediaRecorder.start();
    }

    /**
     * 结束录制
     */
    public void stopSound() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            File[] files = mFile.listFiles();
            for(File f : files){
                Log.e(TAG, "initPath: "+f.getName()+"\n");
            }
            mMediaRecorder = null;
        }
    }

    //播放
    private void playSound() {
        mMediaPlayer = new MediaPlayer();
        if(filename != null && !filename.equals("")) {
            try {
                mMediaPlayer.setDataSource(filename);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mMediaPlayer.start();
                    }
                });
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (mMediaPlayer.isPlaying()) {
                            Log.i(TAG, "onCompletion:正在播放");
                        } else {
                            mMediaPlayer.release();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"当前没有音频",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                startSound();//开始录制
                chronometer.start();//计时开始
                break;
            case R.id.btn_end:
                stopSound();//结束录制
                chronometer.stop();//计时结束
                break;
            case R.id.btn_play:
                //播放音频
                playSound();
                break;
            case R.id.btn_go_pause:
//                if (pause == 0) {//暂停状态
//                    btnGoPause.setText("继续播放");
//                    mMediaPlayer.pause();
//                    pause = 1;
//                } else {//播放状态
//                    btnGoPause.setText("暂停播放");
//                    mMediaPlayer.start();
//                    pause = 0;
//                }
                uploadFile();
                break;
        }
    }

    private void uploadFile(){
        File file = new File(filename);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        Log.e(TAG, "filename="+filename+" file="+file.getPath()+" "+file.getName());
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", filename, requestFile);

        ApiInterface apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.uploadFile(body).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                ResultGson resultGson = response.body();
                Log.i(TAG, "uploadFile onResponse: "+resultGson.getMsg()+resultGson.getData());
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.i(TAG, "uploadFile onFailure: "+t);
            }
        });
    }
}

