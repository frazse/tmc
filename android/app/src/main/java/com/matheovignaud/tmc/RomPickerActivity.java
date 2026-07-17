package com.matheovignaud.tmc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.os.Build;

public class RomPickerActivity extends Activity {
    private static final int PICK_ROM_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File romFile = new File(getExternalFilesDir(null), "baserom.gba");
        if (romFile.exists()) {
            launchGame();
            return;
        }

        showRomPickerUi();
    }

    private void launchGame() {
        Intent intent = new Intent(this, TmcActivity.class);
        startActivity(intent);
        finish();
    }

    private void showRomPickerUi() {
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        container.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText("The Legend of Zelda: The Minish Cap\n\nBase ROM (baserom.gba) not found.\nThis is required to extract game assets.");
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(40, 0, 40, 60);
        tv.setTextSize(18);

        Button btn = new Button(this);
        btn.setText("Select ROM (.gba)");
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btn.setLayoutParams(btnLp);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_ROM_REQUEST);
        });

        container.addView(tv);
        container.addView(btn);
        root.addView(container);

        setContentView(root);

        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_ROM_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                if (copyRom(uri)) {
                    launchGame();
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
