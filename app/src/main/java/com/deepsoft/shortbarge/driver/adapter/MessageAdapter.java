package com.deepsoft.shortbarge.driver.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.deepsoft.shortbarge.driver.gson.MessageGson;

import java.util.List;
import com.deepsoft.shortbarge.driver.R;

public class MessageAdapter extends BaseQuickAdapter<MessageGson, BaseViewHolder> {

    private String resp, driver;

    public MessageAdapter(int layoutResId, @Nullable List<MessageGson> data, String resp, String driver) {
        super(layoutResId, data);
        this.resp = resp;
        this.driver = driver;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, MessageGson messageGson) {
        baseViewHolder.setText(R.id.it_tv_y, messageGson.getMsg())
                .setText(R.id.it_tv_m, messageGson.getMsg())
                .setText(R.id.item_tv_resp, resp)
                .setText(R.id.item_tv_car, driver);
    }
}
