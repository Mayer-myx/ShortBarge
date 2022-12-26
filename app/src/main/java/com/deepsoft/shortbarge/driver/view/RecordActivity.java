package com.deepsoft.shortbarge.driver.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RecordActivity extends AppCompatActivity {

    public static final String TAG = "RecordActivity";

    private boolean isRecording = false, isPlaying = false;
    private String fileName = null;
    public static final String[] dateParse = {"零","一","二","三","四","五","六","七","八","九","十"};
    private File mFileDir = null, mLikeDir = null;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.focusNotAle(getWindow());
        setContentView(R.layout.activity_record);
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());

        mFileDir = getAppSpecificMusicStorageDir(this.getApplication(), "record");
        mLikeDir = getAppSpecificDocumentStorageDir(getApplication(),"likes");
        Log.d(TAG, "initView: "+mFileDir.getPath());
    }


    public void Recording(View view) {
        if(isRecording){
            stopRecord();
            isRecording = false;
        }
        else {
            startRecord();
            isRecording = true;
        }
    }


    private void startRecord() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("-yyyy-MM-dd-hh-mm-ss-");
        String date = sDateFormat.format(new java.util.Date());
        fileName = mFileDir.getPath() + "/"+"record"+ date +".3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
            Toast.makeText(this,"正在录音",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        recorder.start();
    }


    @Nullable
    public File getAppSpecificMusicStorageDir(Context context, String albumName) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), albumName);
        if(file.exists()){
            Log.i(TAG, "Directory exist");
        } else if (file == null || !file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }


    @Nullable
    public File getAppSpecificDocumentStorageDir(Context context, String albumName) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), albumName);
        if(file.exists()){
            Log.i(TAG, "Directory exist");
        }
        else if (file == null || !file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }


    private void stopRecord() {
        recorder.stop();
        recorder.release();
        Toast.makeText(this,"结束录音", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "stopRecord: ==========================");
        listCurrent();
        Log.d(TAG, "stopRecord: ==========================");
        recorder = null;
    }


    private void listCurrent(){
        File[] files = mFileDir.listFiles();
        for(File f:files){
            Log.d(TAG, "initPath: "+f.getName()+"\n");
        }
    }


    public void PlayCurrent(View view) {
        if(isPlaying){
            stopPlaying();
            Toast.makeText(this,"结束播放", Toast.LENGTH_SHORT).show();
            isPlaying = false;
        } else {
            startPlaying();
            Toast.makeText(this,"开始播放", Toast.LENGTH_SHORT).show();
            isPlaying = true;
        }
    }


    private void startPlaying() {
        player = new MediaPlayer();
        if(fileName != null && !fileName.equals("")){
            try {
                player.setDataSource(fileName);
                player.prepare();
                player.start();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }
        }else {
            Toast.makeText(this,"当前没有缓存的音频有哟，先录一段试试吧^_^",Toast.LENGTH_SHORT).show();
        }

    }


    private void stopPlaying() {
        player.stop();
        player.release();
        player = null;
    }
}