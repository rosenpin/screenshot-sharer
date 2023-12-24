package com.tomer.screenshotsharer

import android.animation.Animator
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AssistLoggerSession(context: Context) : VoiceInteractionSession(context) {

    override fun onHandleAssist(state: AssistState) {
        super.onHandleAssist(state)
        Log.d("Data", state.assistData.toString())
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        super.onHandleScreenshot(screenshot)
        Log.d(AssistLoggerSession::class.java.simpleName, "Received screenshot")
        if (Settings.canDrawOverlays(context)) {
            showPreviewAndFinish(screenshot)
        } else {
            shareBitmap(screenshot)
            finish()
        }
    }

    private fun showPreviewAndFinish(screenshot: Bitmap?) {
        if (screenshot == null) {
            Toast.makeText(context, "Screenshot is null", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setUiEnabled(true)

        val rootView = layoutInflater.inflate(R.layout.image_view, null)
        rootView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val imageView = rootView.findViewById(R.id.image_view) as ImageView
        imageView.setImageBitmap(screenshot)

        Log.d("Screenshot", "Showing preview")
        setContentView(rootView)
        imageView.alpha = 0.0f
        imageView.animate().alpha(1.0f).setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    shareBitmap(screenshot)
                    finish()
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
    }

    private fun saveImage(bitmap: Bitmap?, fileName: String): String? {
        if (bitmap == null)
            return null
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/Screenshots"
            )
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        uri?.let {
            context.contentResolver.openOutputStream(it).use { outputStream ->
                outputStream?.let { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, stream)
                    stream.flush()
                    stream.close()

                    // Add pic to gallery
                    MediaScannerConnection.scanFile(context, arrayOf(uri.toString()), null, null)
                }
            }
            return uri.toString()
        }

        return null
    }

    private fun shareBitmap(bitmap: Bitmap?) {
        val time = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault()).format(Date())
        val fileName = "screenshot-$time.png"

        val path = saveImage(bitmap, fileName) ?: run {
            Log.e("Screenshot", "Failed to save image")
            return
        }

        Log.d("Screenshot", "Saved image to $path")

        val bmpUri = Uri.parse(path)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
        shareIntent.putExtra(Intent.EXTRA_TEXT, "")
        shareIntent.type = "image/jpeg"
        val finalShareIntent = Intent.createChooser(shareIntent, "Share screenshot to")
        finalShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Log.d("Screenshot", "Sharing image")
        context.startActivity(finalShareIntent)
    }
}