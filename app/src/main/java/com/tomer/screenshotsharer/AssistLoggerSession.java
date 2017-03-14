package com.tomer.screenshotsharer;

import android.animation.Animator;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;
import android.view.View;
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
        } else {
            shareBitmap(screenshot);
            finish();
        }
    }

    private void showPreviewAndFinish(final Bitmap screenshot) {
        final WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        final View rootView = getLayoutInflater().inflate(R.layout.image_view, null);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_view);
        imageView.setAlpha(0.0f);
        imageView.setImageBitmap(screenshot);
        ViewGroup.LayoutParams params =
            new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                0,
                PixelFormat.TRANSLUCENT);
        windowManager.addView(rootView, params);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                imageView.animate().alpha(1.0f).setDuration(700).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        shareBitmap(screenshot);
                        windowManager.removeView(rootView);
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
        });
    }

    private void shareBitmap(Bitmap bitmap) {
        if (!canWriteExternalPermission()) {
            Intent mainActivity = new Intent(getContext(), MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(mainActivity);
            return;
        }
        String pathToScreenshot =
            MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap,
                "screenshot", null);
        Uri bmpUri = Uri.parse(pathToScreenshot);
        final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "");
        shareIntent.setType("image/jpeg");
        Intent finalShareIntent =
            Intent.createChooser(shareIntent, "Select the app you want to share the screenshot to");
        finalShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(finalShareIntent);
    }

    private boolean canWriteExternalPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
