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
import com.deepsoft.shortbarge.driver.bean.message.MessageBean;

import java.io.IOException;
import java.util.List;
import com.deepsoft.shortbarge.driver.R;

public class MessageAdapter extends BaseQuickAdapter<MessageBean, BaseViewHolder> {

    private Context context;
    private String resp, driver;
    private MediaPlayer mediaPlayer;

    public MessageAdapter(int layoutResId, @Nullable List<MessageBean> data, Context context,
                          String resp, String driver) {
        super(layoutResId, data);
        this.resp = resp;
        this.driver = driver;
        this.context = context;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, MessageBean messageBean) {
        baseViewHolder.setText(R.id.it_tv_y, messageBean.getMsg())
                .setText(R.id.it_tv_m, messageBean.getMsg())
                .setText(R.id.item_tv_resp, resp)
                .setText(R.id.item_tv_car, driver);

        if(getItemPosition(messageBean) == 0){
            baseViewHolder.findView(R.id.item_mess).setPadding(0, 20, 0, 0);
        }

        if(messageBean.getType()){
            baseViewHolder.findView(R.id.it_mes_y_layout).setVisibility(View.GONE);
            baseViewHolder.findView(R.id.it_mes_m_layout).setVisibility(View.VISIBLE);
        }else{
            baseViewHolder.findView(R.id.it_mes_y_layout).setVisibility(View.VISIBLE);
            baseViewHolder.findView(R.id.it_mes_m_layout).setVisibility(View.GONE);
        }

        baseViewHolder.findView(R.id.it_tv_m).setOnClickListener(v->{
            playSound(messageBean.getUrl());
        });
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
            Toast.makeText(context, "当前没有音频",Toast.LENGTH_SHORT).show();
        }
    }
}
