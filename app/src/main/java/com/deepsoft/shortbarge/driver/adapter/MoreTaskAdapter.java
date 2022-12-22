package com.deepsoft.shortbarge.driver.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.gson.TaskGson;

import java.util.List;

public class MoreTaskAdapter extends BaseQuickAdapter<TaskGson, BaseViewHolder> {

    public MoreTaskAdapter(int layoutResId, @Nullable List<TaskGson> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, TaskGson task) {
        baseViewHolder.setText(R.id.item_tv_st, task.getStartTime())
                .setText(R.id.item_tv_at, task.getArrivalTime())
                .setText(R.id.item_tv_dest, task.getDuration())
                .setText(R.id.item_tv_ts, ""+task.getState());
        if(task.getState() == 1){
            baseViewHolder.findView(R.id.item_tv_isemer).setVisibility(View.VISIBLE);
            baseViewHolder.findView(R.id.item_bg).setBackgroundResource(R.drawable.round_line_rect_red);
        }else{
            baseViewHolder.findView(R.id.item_tv_isemer).setVisibility(View.INVISIBLE);
            baseViewHolder.findView(R.id.item_bg).setBackgroundResource(R.drawable.round_line_rect_gray);
        }
    }
}
