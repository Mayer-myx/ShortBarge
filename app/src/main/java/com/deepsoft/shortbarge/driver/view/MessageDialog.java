package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MessageAdapter;
import com.deepsoft.shortbarge.driver.gson.MessageGson;
import com.deepsoft.shortbarge.driver.gson.ResultGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.widget.BaseApplication;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageDialog extends MyDialog implements View.OnClickListener{

    private final static String TAG = "MessageDialog";

    private ApiInterface apiInterface;
    private Context context;

    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private File mFile;
    private String filename, name, uploadUrl;
    private int fileTime;
    private List<MessageGson> messageGsons = new ArrayList<>();

    private MessageAdapter messageAdapter;
    private ImageView dialog_vm_iv_close;
    private TextView dialog_vm_tv_sent;
    private RecyclerView dialog_vm_rv;


    public MessageDialog(@NonNull Context context) {
        super(context);
    }

    public MessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }


//    private MessageDialog(){
//    }
//
//
//    public static MessageDialog getInstance(){
//        if(messageDialog == null){
//            synchronized (MessageDialog.class){
//                if(messageDialog == null)
//                    messageDialog = new MessageDialog();
//            }
//        }
//        return messageDialog;
//    }


    public void showMessageDialog(Context context, LayoutInflater layoutInflater){
        View dialog_message = layoutInflater.inflate(R.layout.dialog_voice_message, null);
        this.setContentView(dialog_message);
        this.setCanceledOnTouchOutside(false);
        this.show();

        this.context = context;
        mFile = new File(BaseApplication.getApplication().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "record");
        if(mFile.exists()){
            Log.i(TAG, "Directory exist");
        } else if (mFile == null || !mFile.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }

        dialog_vm_iv_close = dialog_message.findViewById(R.id.dialog_vm_iv_close);
        dialog_vm_iv_close.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_vm_iv_close);
        dialog_vm_tv_sent = dialog_message.findViewById(R.id.dialog_vm_tv_sent);
        dialog_vm_tv_sent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                long startTime = System.currentTimeMillis(), endTime;
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        view.setAlpha(0.6f);
                        startTime = System.currentTimeMillis();
                        startSound();
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setAlpha(1.0f);
                        endTime = System.currentTimeMillis();
                        fileTime = (int) (endTime - startTime) / 1000;
                        stopSound();
                        uploadFile();
                        break;
                }
                return true;
            }
        });

        for(int i = 0;i < 5; i++){
            MessageGson messageGson = new MessageGson();
            messageGson.setMsg("hhhhhh");
            messageGson.setType(1);
            messageGsons.add(messageGson);
        }
        dialog_vm_rv = dialog_message.findViewById(R.id.dialog_vm_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        dialog_vm_rv.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(R.layout.item_message, messageGsons, "03", "02");
        dialog_vm_rv.setAdapter(messageAdapter);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_vm_iv_close:
                this.dismiss();
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
                uploadUrl = resultGson.getData().toString();
                Log.i(TAG, "uploadFile onResponse: "+resultGson.getMsg()+resultGson.getData());
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.i(TAG, "uploadFile onFailure: "+t);
            }
        });
    }


    /**
     * 开始录制
     */
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


    /**
     * 播放
     */
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
            Toast.makeText(context, "当前没有音频",Toast.LENGTH_SHORT).show();
        }
    }
}
