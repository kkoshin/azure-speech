package com.github.kkoshin.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class STTService actual constructor(subscriptionKey: String, region: String) {
    actual val recognizing: Flow<String> = MutableStateFlow("")

    /**
     * Not Matched will send null
     */
    actual val recognized: Flow<String?> = MutableStateFlow(null)

    actual suspend fun startRecognize() {
        TODO()
    }

    actual suspend fun stopRecognize() {
        TODO()
    }
}