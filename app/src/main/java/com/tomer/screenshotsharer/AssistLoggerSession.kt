package com.tomer.screenshotsharer

import android.animation.Animator
import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView

import android.content.Context.WINDOW_SERVICE

class AssistLoggerSession(context: Context) : VoiceInteractionSession(context) {
	
	override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
		super.onHandleAssist(data, structure, content)
		Log.d("Data", data!!.toString())
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
	
	private fun shareBitmap(bitmap: Bitmap?) {
		if (!canWriteExternalPermission()) {
			val mainActivity = Intent(context, MainActivity::class.java)
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(mainActivity)
			return
		}
		val pathToScreenshot = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap,
				"screenshot", null)
		val bmpUri = Uri.parse(pathToScreenshot)
		val shareIntent = Intent(android.content.Intent.ACTION_SEND)
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
