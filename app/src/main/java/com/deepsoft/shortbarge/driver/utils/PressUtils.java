package com.deepsoft.shortbarge.driver.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class PressUtils {
    public static void setPressChange(Context context, View... views) {
        if (context !=null && views !=null){
            for (View view : views) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                v.setAlpha(0.6f);
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_OUTSIDE:
                                v.setAlpha(1.0f);
                                break;
                        }
                        return false;
                    }
                });
            }
        }
    }
}