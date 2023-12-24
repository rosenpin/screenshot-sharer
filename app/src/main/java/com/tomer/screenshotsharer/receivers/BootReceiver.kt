package com.tomer.screenshotsharer.receivers


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tomer.screenshotsharer.assist.AssistLoggerService

class BootReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action == Intent.ACTION_BOOT_COMPLETED)
			context.startService(Intent(context, AssistLoggerService::class.java))
	}
}
