package com.tomer.screenshotsharer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    @BindView(R.id.assistant)
    ToggleButton assistant;
    @BindView(R.id.storage)
    ToggleButton storage;
    @BindView(R.id.preview)
    ToggleButton preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAssistant();
        checkStorageAccess();
        checkPreview();
        assistant.setClickable(false);
        storage.setClickable(false);
        preview.setClickable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkStorageAccess();
    }

    private void checkAssistant() {
        String currentAssistant = Settings.Secure.getString(getContentResolver(), "voice_interaction_service");
        if ((currentAssistant != null)
                && (currentAssistant.equals(getPackageName() + "/." + AssistLoggerService.class.getSimpleName())
                || currentAssistant.contains(getPackageName())))
            assistant.setChecked(true);
        else
            assistant.setChecked(false);
        assistant.setOnTouchListener(this);
    }

    private void checkStorageAccess() {
        if (canWriteExternalPermission())
            storage.setChecked(true);
        else {
            storage.setChecked(false);
            storage.setOnTouchListener(this);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void checkPreview() {
        if (Settings.canDrawOverlays(this))
            preview.setChecked(true);
        else
            preview.setChecked(false);
        preview.setOnTouchListener(this);
    }

    private boolean canWriteExternalPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.assistant:
                startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
                Toast.makeText(this, "Select " + getString(R.string.app_name) + " as your assist app", Toast.LENGTH_SHORT).show();
                break;
            case R.id.storage:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                break;
            case R.id.preview:
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
                Toast.makeText(this, "Permit drawing over other apps for previews", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}
