package com.matheovignaud.tmc;

import org.libsdl.app.SDLActivity;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.app.ActivityOptions;
import android.util.Log;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;

public class TmcActivity extends SDLActivity {
    // private TmcPresentation mPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        dm.registerDisplayListener(new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                checkSecondaryDisplay();
            }

            @Override
            public void onDisplayRemoved(int displayId) {}

            @Override
            public void onDisplayChanged(int displayId) {}
        }, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSecondaryDisplay();
    }

    private void checkSecondaryDisplay() {
        Log.d("TMC", "Checking for secondary displays...");
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        Log.d("TMC", "Found " + displays.length + " displays.");
        if (displays.length > 1) {
            Log.d("TMC", "Starting TmcTouchActivity on display[1]: " + displays[1].getName());
            Intent intent = new Intent(this, TmcTouchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityOptions options = ActivityOptions.makeBasic();
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                options.setLaunchDisplayId(displays[1].getDisplayId());
            }
            startActivity(intent, options.toBundle());
        }
    }

    public void requestSecondaryDisplay() {
        // Defer to onResume or manual launch
    }

    @Override
    protected String[] getArguments() {
        List<String> args = new ArrayList<>();
        
        SharedPreferences prefs = getSharedPreferences("tmc_settings", Context.MODE_PRIVATE);
        
        int windowScale = prefs.getInt("window_scale", 3);
        args.add("--window_scale=" + windowScale);
        
        int internalScale = prefs.getInt("internal_scale", 1);
        args.add("--internal_scale=" + internalScale);
        
        int upscaleMethodIndex = prefs.getInt("upscale_method", 0);
        String upscaleMethod = "nearest";
        switch (upscaleMethodIndex) {
            case 1: upscaleMethod = "linear"; break;
            case 2: upscaleMethod = "xbrz"; break;
        }
        args.add("--upscale_method=" + upscaleMethod);
        
        return args.toArray(new String[0]);
    }

    @Override
    protected String[] getLibraries() {
        return new String[] {
            "SDL3",
            "main"
        };
    }
}
