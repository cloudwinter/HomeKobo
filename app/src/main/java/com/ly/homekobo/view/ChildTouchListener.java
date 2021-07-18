package com.ly.homekobo.view;

import android.view.MotionEvent;

public interface ChildTouchListener {
    public void onTopTouch(MotionEvent event);

    public void onBottomTouch(MotionEvent event);
}
