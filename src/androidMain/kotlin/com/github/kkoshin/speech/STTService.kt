package com.github.kkoshin.speech

import android.util.Log
import com.microsoft.cognitiveservices.speech.CancellationReason
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SessionEventArgs
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionCanceledEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Semaphore

actual class STTService actual constructor(subscriptionKey: String, region: String) {

    private val TAG = this::class.simpleName

    private val _recognizing: MutableStateFlow<String> = MutableStateFlow("")
    actual val recognizing: Flow<String> = _recognizing

    /**
     * Not Matched will send null
     */
    private val _recognized: MutableStateFlow<String?> = MutableStateFlow(null)
    actual val recognized: Flow<String?> = _recognized

    private val stopTranslationWithFileSemaphore = Semaphore(1)

    private val speechRecognizer by lazy {
        SpeechRecognizer(
            SpeechConfig.fromSubscription(subscriptionKey, region).apply {
                speechRecognitionLanguage = "en-US"
            },
            AudioConfig.fromDefaultMicrophoneInput()
        ).apply {
            recognizing.addEventListener { _, e ->
                _recognizing.tryEmit(e.result.text)
            }

            recognized.addEventListener { _, e: SpeechRecognitionEventArgs ->
                if (e.result.reason == ResultReason.RecognizedSpeech) {
                    _recognized.tryEmit(e.result.text)
                } else if (e.result.reason == ResultReason.NoMatch) {
                    _recognized.tryEmit(null)
                }
            }

            canceled.addEventListener { _, e: SpeechRecognitionCanceledEventArgs ->
                Log.w(TAG, "CANCELED: Reason=" + e.reason)
                if (e.reason == CancellationReason.Error) {
                    Log.e(
                        TAG,
                        "CANCELED: ErrorCode= ${e.errorCode} ErrorDetails= ${e.errorDetails}"
                    )
                }
//                stopTranslationWithFileSemaphore.release()
            }

            sessionStopped.addEventListener { _, _ ->
                Log.v(TAG, "Session stopped event.")
//                stopTranslationWithFileSemaphore.release()
            }
        }
    }

    actual suspend fun startRecognize() {
        speechRecognizer.startContinuousRecognitionAsync()
//        stopTranslationWithFileSemaphore.acquire()
//        speechRecognizer.stopContinuousRecognitionAsync()
    }

    actual suspend fun stopRecognize() {
        speechRecognizer.stopContinuousRecognitionAsync()
    }
}