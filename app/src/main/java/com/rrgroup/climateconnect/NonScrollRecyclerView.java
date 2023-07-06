package com.rrgroup.climateconnect;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class NonScrollRecyclerView extends RecyclerView {
    public NonScrollRecyclerView(Context context) {
        super(context);
    }

    public NonScrollRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return false;
    }
}

