package io.rosenpin.screenshotsharer.receivers


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.rosenpin.screenshotsharer.assist.AssistLoggerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            context.startService(Intent(context, AssistLoggerService::class.java))
        }
    }
}
