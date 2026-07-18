package com.matheovignaud.tmc;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

public class TmcPresentation extends Presentation {
    private SurfaceView mSurfaceView;

    public TmcPresentation(Context outerContext, Display display) {
        super(outerContext, display, android.R.style.Theme_NoTitleBar_Fullscreen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TMC", "TmcPresentation onCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.getHolder().setFormat(android.graphics.PixelFormat.RGBA_8888);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("TMC", "Secondary Surface created: " + holder.getSurface());
                nativeSecondarySurfaceCreated(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("TMC", "Secondary Surface changed: " + width + "x" + height);
                nativeSecondarySurfaceChanged(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("TMC", "Secondary Surface destroyed");
                nativeSecondarySurfaceDestroyed();
            }
        });

        setContentView(mSurfaceView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private native void nativeSecondarySurfaceCreated(android.view.Surface surface);
    private native void nativeSecondarySurfaceChanged(int width, int height);
    private native void nativeSecondarySurfaceDestroyed();
}
