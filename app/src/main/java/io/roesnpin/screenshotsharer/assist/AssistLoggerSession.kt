package io.rosenpin.screenshotsharer.assist

import android.animation.Animator
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import io.rosenpin.screenshotsharer.R
import io.rosenpin.screenshotsharer.prefs.DB_NAME
import io.rosenpin.screenshotsharer.prefs.KEY_SAVE_SCREENSHOT
import io.rosenpin.screenshotsharer.prefs.KEY_SHOW_PREVIEW
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AssistLoggerSession(context: Context) : VoiceInteractionSession(context) {
    private val prefs by lazy {
        context.getSharedPreferences(DB_NAME, Context.MODE_PRIVATE)
    }
    private val showPreview by lazy {
        prefs.getBoolean(KEY_SHOW_PREVIEW, false)
    }
    private val saveScreenshot by lazy {
        prefs.getBoolean(KEY_SAVE_SCREENSHOT, true)
    }

    override fun onHandleAssist(state: AssistState) {
        super.onHandleAssist(state)
        Log.d("Data", state.assistData.toString())
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        super.onHandleScreenshot(screenshot)
        Log.d(AssistLoggerSession::class.java.simpleName, "Received screenshot")

        if (showPreview) {
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

    private fun saveImage(
        context: Context,
        bitmap: Bitmap?,
        fileName: String,
        saveScreenshotInStorage: Boolean
    ): String? {
        if (bitmap == null) return null

        return if (saveScreenshotInStorage) {
            saveToExternalStorage(context, bitmap, fileName)
        } else {
            saveToCache(context, bitmap, fileName)
        }
    }

    private fun saveToCache(context: Context, bitmap: Bitmap, fileName: String): String {
        val directory = context.cacheDir
        val imageFile = File(directory, fileName)

        FileOutputStream(imageFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
        }

        return imageFile.absolutePath
    }

    private fun saveToExternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
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
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

                MediaScannerConnection.scanFile(context, arrayOf(uri.toString()), null, null)
            }
            return uri.toString()
        }

        return null
    }

    private fun shareBitmap(bitmap: Bitmap?) {
        val time = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault()).format(Date())
        val fileName = "screenshot-$time.jpg"

        val path = saveImage(context, bitmap, fileName, saveScreenshot) ?: run {
            Log.e("Screenshot", "Failed to save image")
            return
        }

        val bmpUri: Uri = if (saveScreenshot) {
            // Save the image using the original method and get a content URI
            Uri.parse(path)
        } else {
            // Save the image to cache and get a content URI using FileProvider
            val file = File(path)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read permission
            putExtra(Intent.EXTRA_STREAM, bmpUri)
            type = "image/jpeg"
        }

        val finalShareIntent = Intent.createChooser(shareIntent, "Share screenshot to").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Log.d("Screenshot", "Sharing image to $path")
        context.startActivity(finalShareIntent)
    }
}