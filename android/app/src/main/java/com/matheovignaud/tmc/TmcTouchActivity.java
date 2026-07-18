package com.matheovignaud.tmc;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.util.Log;

public class TmcTouchActivity extends Activity {
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TMC", "TmcTouchActivity onCreate");

        mSurfaceView = new SurfaceView(this);
        mSurfaceView.getHolder().setFormat(android.graphics.PixelFormat.RGBA_8888);
        mSurfaceView.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_MOVE) {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    nativeSecondaryTouchEvent(action, event.getX(i), event.getY(i), event.getPointerId(i));
                }
            } else {
                nativeSecondaryTouchEvent(action, event.getX(event.getActionIndex()), event.getY(event.getActionIndex()), event.getPointerId(event.getActionIndex()));
            }
            return true;
        });
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("TMC", "Secondary Activity Surface created");
                nativeSecondarySurfaceCreated(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("TMC", "Secondary Activity Surface changed: " + width + "x" + height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("TMC", "Secondary Activity Surface destroyed");
                nativeSecondarySurfaceDestroyed();
            }
        });

        setContentView(mSurfaceView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private native void nativeSecondarySurfaceCreated(android.view.Surface surface);
    private native void nativeSecondarySurfaceDestroyed();
    private native void nativeSecondaryTouchEvent(int action, float x, float y, int pointerId);
}
