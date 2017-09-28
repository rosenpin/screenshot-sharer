package com.tomer.screenshotsharer

import android.os.Bundle
import android.service.voice.VoiceInteractionSessionService

class AssistLoggerSessionService : VoiceInteractionSessionService() {
	override fun onNewSession(args: Bundle) = AssistLoggerSession(this)
}
