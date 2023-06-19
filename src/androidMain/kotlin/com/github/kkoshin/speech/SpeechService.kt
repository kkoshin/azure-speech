package com.github.kkoshin.speech

import android.util.Log
import com.microsoft.cognitiveservices.speech.CancellationReason
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionCanceledEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class SpeechService actual constructor(
    subscriptionKey: String,
    region: String,
    speechLanguage: String
) {

    private val TAG = this::class.simpleName

    private val _recognizing: MutableStateFlow<String> = MutableStateFlow("")
    actual val recognizing: Flow<String> = _recognizing

    /**
     * Not Matched will send null
     */
    private val _recognized: MutableStateFlow<String?> = MutableStateFlow(null)
    actual val recognized: Flow<String?> = _recognized

    private val speechConfig by lazy {
        SpeechConfig.fromSubscription(subscriptionKey, region).apply {
            speechRecognitionLanguage = speechLanguage
        }
    }

    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer(speechConfig, AudioConfig.fromDefaultMicrophoneInput()).also {
            it.recognizing.addEventListener { _, e ->
                _recognizing.tryEmit(e.result.text)
            }

            it.recognized.addEventListener { _, e: SpeechRecognitionEventArgs ->
                if (e.result.reason == ResultReason.RecognizedSpeech) {
                    _recognized.tryEmit(e.result.text)
                } else if (e.result.reason == ResultReason.NoMatch) {
                    _recognized.tryEmit(null)
                }
            }

            it.canceled.addEventListener { _, e: SpeechRecognitionCanceledEventArgs ->
                Log.w(TAG, "CANCELED: Reason=" + e.reason)
                if (e.reason == CancellationReason.Error) {
                    Log.e(
                        TAG, "CANCELED: ErrorCode= ${e.errorCode} ErrorDetails= ${e.errorDetails}"
                    )
                }
            }

            it.sessionStopped.addEventListener { _, _ ->
                Log.v(TAG, "Session stopped event.")
            }
        }
    }

    actual suspend fun startRecognize() {
        speechRecognizer.startContinuousRecognitionAsync()
    }

    actual suspend fun stopRecognize() {
        speechRecognizer.stopContinuousRecognitionAsync()
    }

    // ===========================
    private val _synthesisProgress: MutableStateFlow<SynthesisStatus?> = MutableStateFlow(null)
    actual val synthesisProgress: Flow<SynthesisStatus?> = _synthesisProgress

    private val speechSynthesizer: SpeechSynthesizer by lazy {
        SpeechSynthesizer(speechConfig).also {
            it.SynthesisStarted.addEventListener { _, _ ->
                _synthesisProgress.tryEmit(SynthesisStatus.Started)
            }

            it.Synthesizing.addEventListener { _, _ ->
                _synthesisProgress.tryEmit(SynthesisStatus.Synthesizing)
            }

            it.SynthesisCanceled.addEventListener { _, _ ->
                _synthesisProgress.tryEmit(SynthesisStatus.Canceled)
            }

            it.SynthesisCompleted.addEventListener { _, e ->
                e.result.use { result ->
                    _synthesisProgress.tryEmit(SynthesisStatus.Completed(result.audioDurationMills))
                }
            }
        }
    }

    actual suspend fun speak(content: String): Result<Duration> {
        if (content.isBlank()) {
            return Result.success(Duration.ZERO)
        }
        return withContext(Dispatchers.IO) {
            speechSynthesizer.SpeakText(content).use {
                runCatching {
                    if (it.reason == ResultReason.SynthesizingAudioCompleted) {
                        it.audioDurationMills
                    } else {
                        Duration.ZERO
                    }
                }
            }
        }
    }

    actual fun stopSpeak() {
        speechSynthesizer.StopSpeakingAsync().get()
    }

    actual fun destroy() {
        speechRecognizer.close()
        speechSynthesizer.close()
        speechConfig.close()
    }
}

private val SpeechSynthesisResult.audioDurationMills: Duration
    get() = ((audioDuration + 5000) / 10000).milliseconds
