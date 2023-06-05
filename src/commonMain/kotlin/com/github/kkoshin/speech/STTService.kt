package com.github.kkoshin.speech

import kotlinx.coroutines.flow.Flow

expect class STTService(subscriptionKey: String, region: String) {
    val recognizing: Flow<String>

    /**
     * Not Matched will send null
     */
    val recognized: Flow<String?>

    suspend fun startRecognize()

    suspend fun stopRecognize()
}