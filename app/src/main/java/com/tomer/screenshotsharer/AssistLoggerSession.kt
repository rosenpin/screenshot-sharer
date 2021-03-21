package com.tomer.screenshotsharer

import android.animation.Animator
import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.ContentValues
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AssistLoggerSession(context: Context) : VoiceInteractionSession(context) {
	
	override fun onHandleAssist(state: AssistState) {
		super.onHandleAssist(state)
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
			Log.d("Data", state.assistData.toString())
		}
	}
	
	override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
		super.onHandleAssist(data, structure, content)
		Log.d("Data", data.toString())
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
		val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
		val rootView = layoutInflater.inflate(R.layout.image_view, null)
		val imageView = rootView.findViewById(R.id.image_view) as ImageView
		imageView.alpha = 0.0f
		imageView.setImageBitmap(screenshot)
		val params = WindowManager.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				0,
				PixelFormat.TRANSLUCENT)
		windowManager.addView(rootView, params)
		Handler().post {
			imageView.animate().alpha(1.0f).setDuration(700).setListener(object : Animator.AnimatorListener {
				override fun onAnimationStart(animation: Animator) {
				
				}
				
				override fun onAnimationEnd(animation: Animator) {
					shareBitmap(screenshot)
					windowManager.removeView(rootView)
					finish()
				}
				
				override fun onAnimationCancel(animation: Animator) {
				
				}
				
				override fun onAnimationRepeat(animation: Animator) {
				
				}
			})
		}
	}
	
	private fun saveImage(bitmap: Bitmap?, fileName: String): String? {
		if (bitmap == null)
			return null
		
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
			val contentValues = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
				put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
				put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots")
			}
			val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
			if (uri != null) {
				context.contentResolver.openOutputStream(uri).use {
					if (it == null)
						return@use
					
					bitmap.compress(Bitmap.CompressFormat.PNG, 85, it)
					it.flush()
					it.close()
					
					// add pic to gallery
					MediaScannerConnection.scanFile(context, arrayOf(uri.toString()), null, null)
				}
			}
			return uri.toString()
		}
		
		val filePath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES + "/Screenshots"
		).absolutePath
		
		Toast.makeText(this.context, filePath, Toast.LENGTH_LONG).show()
		val dir = File(filePath)
		if (!dir.exists()) dir.mkdirs()
		val file = File(dir, fileName)
		val fOut = FileOutputStream(file)
		
		bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
		fOut.flush()
		fOut.close()
		
		// add pic to gallery
		MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
		return filePath
	}
	
	private fun shareBitmap(bitmap: Bitmap?) {
		if (!canWriteExternalPermission()) {
			val mainActivity = Intent(context, MainActivity::class.java)
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(mainActivity)
			return
		}
		val time = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault()).format(Date())
		val fileName = "screenshot-$time"
		
		val path = saveImage(bitmap, fileName) ?: return
		
		val bmpUri = Uri.parse(path)
		val shareIntent = Intent(Intent.ACTION_SEND)
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
		shareIntent.putExtra(Intent.EXTRA_TEXT, "")
		shareIntent.type = "image/jpeg"
		val finalShareIntent = Intent.createChooser(shareIntent, "Select the app you want to share the screenshot to")
		finalShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context.startActivity(finalShareIntent)
	}
	
	private fun canWriteExternalPermission(): Boolean {
		val permission = "android.permission.WRITE_EXTERNAL_STORAGE"
		val res = context.checkCallingOrSelfPermission(permission)
		return res == PackageManager.PERMISSION_GRANTED
	}
}
