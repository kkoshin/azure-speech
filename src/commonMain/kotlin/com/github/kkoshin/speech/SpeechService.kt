package com.github.kkoshin.speech

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

expect class SpeechService(
    subscriptionKey: String,
    region: String,
    speechLanguage: String = "en-US"
) {
    val recognizing: Flow<String>

    /**
     * Not Matched will send null
     */
    val recognized: Flow<String?>

    /**
     * default is null
     * https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/how-to-speech-synthesis?tabs=browserjs%2Cterminal&pivots=programming-language-java#subscribe-to-synthesizer-events
     */
    val synthesisProgress: Flow<SynthesisStatus?>

    suspend fun startRecognize()

    suspend fun stopRecognize()

    suspend fun speak(content: String): Result<Duration>

    fun stopSpeak()

    fun destroy()
}

sealed class SynthesisStatus {
    object Started : SynthesisStatus() {
        override fun toString(): String {
            return "Started"
        }
    }

    object Canceled : SynthesisStatus() {
        override fun toString(): String {
            return "Canceled"
        }
    }

    object Synthesizing : SynthesisStatus() {
        override fun toString(): String {
            return "Synthesizing"
        }
    }

    class Completed(val duration: Duration) : SynthesisStatus() {
        override fun toString(): String {
            return "Completed duration: $duration"
        }
    }
}