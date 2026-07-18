package com.matheovignaud.tmc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.os.Build;

public class LauncherActivity extends Activity {
    private static final int PICK_ROM_REQUEST = 1;
    private SharedPreferences mPrefs;
    
    private Spinner mWindowScaleSpinner;
    private Spinner mInternalScaleSpinner;
    private Spinner mUpscaleMethodSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("tmc_settings", Context.MODE_PRIVATE);

        File romFile = new File(getExternalFilesDir(null), "baserom.gba");
        if (!romFile.exists()) {
            showRomPickerUi();
        } else {
            showLauncherUi();
        }
    }

    private void applyImmersiveMode() {
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }

    private void launchGame() {
        android.util.Log.d("TMC", "launchGame() button pressed");
        // Save current selections
        mPrefs.edit()
            .putInt("window_scale", mWindowScaleSpinner.getSelectedItemPosition() + 1)
            .putInt("internal_scale", mInternalScaleSpinner.getSelectedItemPosition() + 1)
            .putInt("upscale_method", mUpscaleMethodSpinner.getSelectedItemPosition())
            .apply();

        Intent intent = new Intent(this, TmcActivity.class);
        startActivity(intent);
    }

    private void showRomPickerUi() {
        LinearLayout root = createBaseLayout();

        TextView tv = new TextView(this);
        tv.setText("The Legend of Zelda: The Minish Cap\n\nBase ROM (baserom.gba) not found.\nThis is required to extract game assets.");
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(40, 40, 40, 60);
        tv.setTextSize(18);

        Button btn = new Button(this);
        btn.setText("Select ROM (.gba)");
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_ROM_REQUEST);
        });

        root.addView(tv);
        root.addView(btn);
        setContentView(root);
        applyImmersiveMode();
    }

    private void showLauncherUi() {
        LinearLayout root = createBaseLayout();

        TextView title = new TextView(this);
        title.setText("The Minish Cap Launcher");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        root.addView(title);

        // Window Scale
        root.addView(createLabel("Window Scale (Display Size):"));
        mWindowScaleSpinner = new Spinner(this);
        String[] windowScales = {"1x", "2x", "3x", "4x", "5x", "6x"};
        mWindowScaleSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, windowScales));
        mWindowScaleSpinner.setSelection(mPrefs.getInt("window_scale", 3) - 1);
        root.addView(mWindowScaleSpinner);

        // Internal Scale
        root.addView(createLabel("Internal Resolution (Multiplier):"));
        mInternalScaleSpinner = new Spinner(this);
        String[] internalScales = {"1x (Native GBA)", "2x", "3x", "4x (High Res)"};
        mInternalScaleSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, internalScales));
        mInternalScaleSpinner.setSelection(mPrefs.getInt("internal_scale", 1) - 1);
        root.addView(mInternalScaleSpinner);

        // Upscale Method
        root.addView(createLabel("Upscale Method:"));
        mUpscaleMethodSpinner = new Spinner(this);
        String[] methods = {"Nearest (Sharp)", "Linear (Smooth)", "xBRZ (Pixel Art AI)"};
        mUpscaleMethodSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods));
        mUpscaleMethodSpinner.setSelection(mPrefs.getInt("upscale_method", 0));
        root.addView(mUpscaleMethodSpinner);

        // Buttons
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        btnRow.setPadding(0, 40, 0, 0);

        Button launchBtn = new Button(this);
        launchBtn.setText("Launch Game");
        launchBtn.setOnClickListener(v -> launchGame());
        btnRow.addView(launchBtn);

        Button changeRomBtn = new Button(this);
        changeRomBtn.setText("Change ROM");
        changeRomBtn.setOnClickListener(v -> showRomPickerUi());
        btnRow.addView(changeRomBtn);

        root.addView(btnRow);
        setContentView(root);
        applyImmersiveMode();
    }

    private LinearLayout createBaseLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(60, 60, 60, 60);
        return root;
    }

    private TextView createLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(0, 20, 0, 5);
        return tv;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_ROM_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                if (copyRom(uri)) {
                    showLauncherUi();
                    return;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean copyRom(Uri uri) {
        try {
            File dest = new File(getExternalFilesDir(null), "baserom.gba");
            InputStream in = getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(dest);
            byte[] buf = new byte[1024 * 1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy ROM: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
