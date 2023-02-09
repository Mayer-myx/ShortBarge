package com.deepsoft.shortbarge.driver.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.bean.message.MessageGson;
import com.deepsoft.shortbarge.driver.utils.PressUtil;

public class MessageAdapter extends BaseQuickAdapter<MessageGson, BaseViewHolder> {

    private Context context;
    private MediaPlayer mediaPlayer;

    public MessageAdapter(int layoutResId, @Nullable List<MessageGson> data, Context context) {
        super(layoutResId, data);
        this.context = context;
    }


    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, MessageGson messageGson) {
        StringBuilder sb = new StringBuilder("");
        if(messageGson.getMsgType() == 2) {
            long duration = getAudioTime("http://"+messageGson.getMsg());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes);
            seconds += minutes * 60;
            long len = seconds > 60 ? 60 : seconds;
            for (int j = 0; j < len; j++)
                sb.append(" ");
            sb = new StringBuilder(sb.toString()+seconds+"”");
        }
        Log.i("MessageAdapter", "id="+messageGson.getChatMessageId()+" time="+sb+" msg="+messageGson.getMsg());

        baseViewHolder.setText(R.id.it_tv_y, messageGson.getMsg())
                .setText(R.id.it_tv_m, sb.toString())
                .setText(R.id.item_tv_resp, ""+messageGson.getFrom())
                .setText(R.id.item_tv_car, ""+messageGson.getFrom());

        if(getItemPosition(messageGson) == 0){
            baseViewHolder.findView(R.id.item_mess).setPadding(0, 20, 0, 0);
        }

        if(messageGson.getMsgType() == 2){
            baseViewHolder.findView(R.id.it_mes_y_layout).setVisibility(View.GONE);
            baseViewHolder.findView(R.id.it_mes_m_layout).setVisibility(View.VISIBLE);
        }else{
            baseViewHolder.findView(R.id.it_mes_y_layout).setVisibility(View.VISIBLE);
            baseViewHolder.findView(R.id.it_mes_m_layout).setVisibility(View.GONE);
        }

        PressUtil.setPressChange(context, baseViewHolder.findView(R.id.it_tv_m));
        PressUtil.setPressChange(context, baseViewHolder.findView(R.id.it_tv_y));
        baseViewHolder.findView(R.id.it_tv_m).setOnClickListener(v->{
            playSound("http://"+messageGson.getMsg());
        });
    }


    private long getAudioTime(String fileName){
        mediaPlayer = new MediaPlayer();
        long mediaPlayerDuration = 0L;
        if(fileName != null && !fileName.equals("")) {
            try {
                mediaPlayer.setDataSource(fileName);
                mediaPlayer.prepare();
                mediaPlayerDuration = mediaPlayer.getDuration();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(context, "没有音频",Toast.LENGTH_SHORT).show();
        }

        return mediaPlayerDuration;
    }


    /**
     * 播放
     */
    private void playSound(String fileName) {
        mediaPlayer = new MediaPlayer();
        if(fileName != null && !fileName.equals("")) {
            try {
                mediaPlayer.setDataSource(fileName);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (mediaPlayer.isPlaying()) {
                            Log.i("MessageAdapter", "onCompletion:正在播放");
                        } else {
                            mediaPlayer.release();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(context, "没有音频",Toast.LENGTH_SHORT).show();
        }
    }
}
