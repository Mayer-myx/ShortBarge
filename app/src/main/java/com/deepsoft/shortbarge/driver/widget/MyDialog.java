package com.deepsoft.shortbarge.driver.widget;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.deepsoft.shortbarge.driver.utils.NavigationBarUtil;

public class MyDialog extends Dialog {

    public MyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void show() {
        NavigationBarUtil.focusNotAle(getWindow());

        super.show();
        NavigationBarUtil.hideNavigationBar(getWindow());
        NavigationBarUtil.clearFocusNotAle(getWindow());
    }
}
