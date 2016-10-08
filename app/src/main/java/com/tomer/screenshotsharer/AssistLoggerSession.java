package com.tomer.screenshotsharer;

import android.animation.Animator;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import static android.content.Context.WINDOW_SERVICE;

public class AssistLoggerSession extends VoiceInteractionSession {

    public AssistLoggerSession(Context context) {
        super(context);
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
        super.onHandleAssist(data, structure, content);
        Log.d("Data", data.toString());
    }

    @Override
    public void onHandleScreenshot(Bitmap screenshot) {
        super.onHandleScreenshot(screenshot);
        Log.d(AssistLoggerSession.class.getSimpleName(), "Received screenshot");
        if (Settings.canDrawOverlays(getContext())) {
            showPreviewAndFinish(screenshot);
            shareBitmap(screenshot);
        } else {
            shareBitmap(screenshot);
            finish();
        }
    }

    private void showPreviewAndFinish(Bitmap screenshot) {
        final WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        final ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(screenshot);
        ViewGroup.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_DIM_BEHIND, -2);
        windowManager.addView(imageView, params);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.animate().alpha(0f).setDuration(500).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        windowManager.removeView(imageView);
                        finish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        }, 3000);
    }

    private void shareBitmap(Bitmap bitmap) {
        if (!canWriteExternalPermission()) {
            Intent mainActivity = new Intent(getContext(), MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(mainActivity);
            return;
        }
        String pathToScreenshot = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "screenshot", null);
        Uri bmpUri = Uri.parse(pathToScreenshot);
        final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "");
        shareIntent.setType("image/png");
        getContext().startActivity(Intent.createChooser(shareIntent, "Select the app you want to share the screenshot to"));
    }

    private boolean canWriteExternalPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
