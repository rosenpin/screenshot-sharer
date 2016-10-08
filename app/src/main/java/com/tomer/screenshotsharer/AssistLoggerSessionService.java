package com.tomer.screenshotsharer;

import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;
import android.util.Log;

public class AssistLoggerSessionService extends VoiceInteractionSessionService {
    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        Log.d(AssistLoggerSessionService.class.getSimpleName(), "Received");
        return (new AssistLoggerSession(this));
    }
}
