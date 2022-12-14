package com.deepsoft.shortbarge.driver.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.deepsoft.shortbarge.driver.R;
import com.deepsoft.shortbarge.driver.adapter.entity.Task;

import java.util.List;

public class MoreTaskAdapter extends BaseQuickAdapter<Task, BaseViewHolder> {

    public MoreTaskAdapter(int layoutResId, @Nullable List<Task> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, Task task) {
        baseViewHolder.setText(R.id.item_tv_st, task.getStart_time())
                .setText(R.id.item_tv_at, task.getArrival_time())
                .setText(R.id.item_tv_dest, task.getDuration())
                .setText(R.id.item_tv_ts, task.getState());
        if(task.getState() == 1){
            baseViewHolder.findView(R.id.item_tv_isemer).setVisibility(View.VISIBLE);
            baseViewHolder.findView(R.id.item_bg).setBackgroundResource(R.drawable.round_rectangle_red);
        }else{
            baseViewHolder.findView(R.id.item_tv_isemer).setVisibility(View.GONE);
            baseViewHolder.findView(R.id.item_bg).setBackgroundResource(R.drawable.round_line_rect_gray);
        }
    }
}
