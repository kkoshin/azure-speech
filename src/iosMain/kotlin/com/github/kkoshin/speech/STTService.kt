package com.github.kkoshin.speech

import MicrosoftCognitiveServicesSpeech.SPXAudioConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class STTService actual constructor(subscriptionKey: String, region: String) {

    private val TAG = this::class.simpleName

    private val config = SPXAudioConfiguration()

    private val _recognizing: MutableStateFlow<String> = MutableStateFlow("")
    actual val recognizing: Flow<String> = _recognizing

    /**
     * Not Matched will send null
     */
    private val _recognized: MutableStateFlow<String?> = MutableStateFlow(null)
    actual val recognized: Flow<String?> = _recognized

    actual suspend fun startRecognize() {
    }

    actual suspend fun stopRecognize() {

    }
}