package com.deepsoft.shortbarge.driver.view;

import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.MessageAdapter;
import com.deepsoft.shortbarge.driver.bean.message.MessageGson;
import com.deepsoft.shortbarge.driver.bean.message.MessageList;
import com.deepsoft.shortbarge.driver.constant.Action;
import com.deepsoft.shortbarge.driver.bean.ResultGson;
import com.deepsoft.shortbarge.driver.retrofit.ApiInterface;
import com.deepsoft.shortbarge.driver.utils.GsonConvertUtil;
import com.deepsoft.shortbarge.driver.utils.PressUtil;
import com.deepsoft.shortbarge.driver.utils.RetrofitUtil;
import com.deepsoft.shortbarge.driver.websocket.WsManager;
import com.deepsoft.shortbarge.driver.widget.BaseApplication;
//import com.deepsoft.shortbarge.driver.widget.LogHandler;
import com.deepsoft.shortbarge.driver.widget.MyDialog;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
    private File mFile;
    private String filename, name, driverId;
    private int msgSize;
    private List<MessageGson> messageGsonList = new ArrayList<>();

    private MessageAdapter messageAdapter = null;
    private LinearLayoutManager layoutManager;
    private ImageView dialog_vm_iv_close;
    private TextView dialog_vm_tv_sent;
    private RecyclerView dialog_vm_rv;


    public MessageDialog(@NonNull Context context, String driverId){
        super(context);
        this.driverId = driverId;
    }


    public void showMessageDialog(Context context, LayoutInflater layoutInflater){
        View dialog_message = layoutInflater.inflate(R.layout.dialog_voice_message, null);
        this.setContentView(dialog_message);
        this.setCanceledOnTouchOutside(false);
        this.show();

        msgSize = 15;

        this.context = context;
        mFile = new File(BaseApplication.getApplication().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "record");
        if(mFile.exists()){
            Log.i(TAG, "Directory exist");
//            LogHandler.writeFile(TAG, "Directory exist");
        } else if (mFile == null || !mFile.mkdirs()) {
            Log.e(TAG, "Directory not created");
//            LogHandler.writeFile(TAG, "Directory not created");
        }

        dialog_vm_iv_close = dialog_message.findViewById(R.id.dialog_vm_iv_close);
        dialog_vm_iv_close.setOnClickListener(this);
        PressUtil.setPressChange(context, dialog_vm_iv_close);
        dialog_vm_tv_sent = dialog_message.findViewById(R.id.dialog_vm_tv_sent);
        dialog_vm_tv_sent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        view.setAlpha(0.6f);
                        startSound();
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setAlpha(1.0f);
                        msgSize++;
                        stopSound();
                        uploadFile();
                        break;
                }
                return true;
            }
        });

        dialog_vm_rv = dialog_message.findViewById(R.id.dialog_vm_rv);
        layoutManager = new LinearLayoutManager(context);
        dialog_vm_rv.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(R.layout.item_message, messageGsonList, context);
        dialog_vm_rv.setAdapter(messageAdapter);
        getChatMsgList(msgSize);
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
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", filename, requestFile);
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.uploadFile(body).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                ResultGson resultGson = response.body();
                Log.i(TAG, "uploadFile onResponse: "+resultGson.getMsg()+resultGson.getData());
//                LogHandler.writeFile(TAG, "uploadFile onResponse: "+resultGson.getMsg()+resultGson.getData());

                if(resultGson.getSuccess()) {
                    //聊天消息
                    WsManager.getInstance().sendReq(new Action("{\"msg\":\"" + resultGson.getData().toString()
                            + "\",\"msgType\":2}", 2, null));

                    getChatMsgList(msgSize);
                }else{
                    Toast.makeText(context, "语音消息上传失败", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.i(TAG, "uploadFile onFailure: "+t);
//                LogHandler.writeFile(TAG, "uploadFile onFailure: "+t);
            }
        });
    }


    private void getChatMsgList(int pageSize){
        HashMap<String, Object> map = new HashMap<>();
        map.put("pageNo", 1);
        map.put("pageSize", pageSize);
        map.put("driverId", driverId);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new JSONObject(map).toString());
        apiInterface = RetrofitUtil.getInstance().getRetrofit().create(ApiInterface.class);
        apiInterface.getChatMsgList(requestBody).enqueue(new Callback<ResultGson>() {
            @Override
            public void onResponse(Call<ResultGson> call, Response<ResultGson> response) {
                ResultGson resultGson = response.body();
                if(resultGson.getSuccess()){
                    List<MessageList> list = GsonConvertUtil.performTransform(resultGson.getData(), MessageList.class);
                    messageGsonList = GsonConvertUtil.performTransform(list.get(0).getList(), MessageGson.class);
                    Collections.reverse(messageGsonList);
                    Log.e(TAG, "getChatMsgList连接成功 resultGson.getData()="+resultGson.getData());
//                    LogHandler.writeFile(TAG, "getChatMsgList连接成功 resultGson.getData()="+resultGson.getData());
                    messageAdapter.setList(messageGsonList);
                    layoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, Integer.MIN_VALUE);
                }else{
                    Log.i(TAG, "getChatMsgList连接成功 数据申请失败， msg="+resultGson.getMsg());
//                    LogHandler.writeFile(TAG, "getChatMsgList连接成功 数据申请失败， msg="+resultGson.getMsg());
                }
            }

            @Override
            public void onFailure(Call<ResultGson> call, Throwable t) {
                Log.e(TAG, "getChatMsgList onFailure:"+t);
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
//            LogHandler.writeFile(TAG, filename+"prepare()failed");
            e.printStackTrace();
        }
        mMediaRecorder.start();
    }


    /**
     * 结束录制
     */
    public void stopSound() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMediaRecorder.release();
            File[] files = mFile.listFiles();
            for(File f : files){
                Log.i(TAG, "initPath: "+f.getName());
//                LogHandler.writeFile(TAG, "initPath: "+f.getName());
            }
            mMediaRecorder = null;
        }
    }
}
