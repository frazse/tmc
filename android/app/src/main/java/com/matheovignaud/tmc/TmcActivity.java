package com.matheovignaud.tmc;

import org.libsdl.app.SDLActivity;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.content.Context;

public class TmcActivity extends SDLActivity {
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
