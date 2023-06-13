package com.github.kkoshin.speech

import MicrosoftCognitiveServicesSpeech.SPXAudioConfiguration
import MicrosoftCognitiveServicesSpeech.SPXCancellationReason_Error
import MicrosoftCognitiveServicesSpeech.SPXResultReason_NoMatch
import MicrosoftCognitiveServicesSpeech.SPXResultReason_RecognizedSpeech
import MicrosoftCognitiveServicesSpeech.SPXSpeechConfiguration
import MicrosoftCognitiveServicesSpeech.SPXSpeechRecognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class STTService actual constructor(subscriptionKey: String, region: String) {

    private val TAG = this::class.simpleName

    private val speechRecognizer by lazy {
        SPXSpeechRecognizer(SPXSpeechConfiguration(subscription = subscriptionKey, region = region).apply {
            speechRecognitionLanguage = "en-US"
        }, SPXAudioConfiguration()).also { reco ->
            reco.addRecognizingEventHandler { _, e ->
                _recognizing.tryEmit(e?.result?.text ?: "")
            }

            reco.addRecognizedEventHandler { _, e ->
                if (e?.result?.reason == SPXResultReason_RecognizedSpeech) {
                    _recognized.tryEmit(e.result.text)
                } else if (e?.result?.reason == SPXResultReason_NoMatch) {
                    _recognized.tryEmit(null)
                }
            }

            reco.addCanceledEventHandler { _, e ->
                if (e?.reason == SPXCancellationReason_Error) {
                    println(
                        "CANCELED: ErrorCode= ${e.errorCode} ErrorDetails= ${e.errorDetails}"
                    )
                }
            }
            reco.addSessionStoppedEventHandler { _, _ ->
                println(
                    "Session stopped event."
                )
            }
        }
    }

    private val _recognizing: MutableStateFlow<String> = MutableStateFlow("")
    actual val recognizing: Flow<String> = _recognizing

    /**
     * Not Matched will send null
     */
    private val _recognized: MutableStateFlow<String?> = MutableStateFlow(null)
    actual val recognized: Flow<String?> = _recognized

    actual suspend fun startRecognize() {
        speechRecognizer.startContinuousRecognition()
    }

    actual suspend fun stopRecognize() {
        speechRecognizer.stopContinuousRecognition()
    }
}