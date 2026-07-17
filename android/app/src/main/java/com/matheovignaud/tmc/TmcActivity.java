package com.matheovignaud.tmc;

import org.libsdl.app.SDLActivity;

public class TmcActivity extends SDLActivity {
    @Override
    protected String[] getLibraries() {
        return new String[] {
            "SDL3",
            "main"
        };
    }
}
