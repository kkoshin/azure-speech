package com.github.kkoshin.speech

import MicrosoftCognitiveServicesSpeech.SPXAudioConfiguration
import MicrosoftCognitiveServicesSpeech.SPXCancellationReason_Error
import MicrosoftCognitiveServicesSpeech.SPXResultReason_NoMatch
import MicrosoftCognitiveServicesSpeech.SPXResultReason_RecognizedSpeech
import MicrosoftCognitiveServicesSpeech.SPXResultReason_SynthesizingAudioCompleted
import MicrosoftCognitiveServicesSpeech.SPXSpeechConfiguration
import MicrosoftCognitiveServicesSpeech.SPXSpeechRecognizer
import MicrosoftCognitiveServicesSpeech.SPXSpeechSynthesisResult
import MicrosoftCognitiveServicesSpeech.SPXSpeechSynthesizer
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class SpeechService actual constructor(
    subscriptionKey: String,
    region: String,
    speechLanguage: String
) {

    private val speechConfig by lazy {
        SPXSpeechConfiguration(
            subscription = subscriptionKey,
            region = region
        ).apply {
            speechRecognitionLanguage = speechLanguage
        }
    }

    private val speechRecognizer by lazy {
        SPXSpeechRecognizer(speechConfig, SPXAudioConfiguration()).also { reco ->
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

    // =========================

    private val _synthesisProgress: MutableStateFlow<SynthesisStatus?> = MutableStateFlow(null)
    actual val synthesisProgress: Flow<SynthesisStatus?> = _synthesisProgress

    private val speechSynthesizer: SPXSpeechSynthesizer by lazy {
        SPXSpeechSynthesizer(speechConfig).also {
            it.addSynthesisStartedEventHandler { _, _ ->
                _synthesisProgress.tryEmit(SynthesisStatus.Started)
            }

            it.addSynthesizingEventHandler { _, _ ->
                _synthesisProgress.tryEmit(SynthesisStatus.Synthesizing)
            }

            it.addSynthesisCanceledEventHandler { _, _ ->
                _synthesisProgress.tryEmit(SynthesisStatus.Canceled)
            }

            it.addSynthesisCompletedEventHandler { _, e ->
                e!!.result.usePinned { result ->
                    _synthesisProgress.tryEmit(SynthesisStatus.Completed(result.get().audioDurationMills))
                }
            }
        }
    }

    actual suspend fun speak(content: String): Result<Duration> {
        if (content.isEmpty()) {
            return Result.success(Duration.ZERO)
        }
        return speechSynthesizer.speakText(content).usePinned {
            runCatching {
                if (it.get().reason == SPXResultReason_SynthesizingAudioCompleted) {
                    it.get().audioDurationMills
                } else {
                    Duration.ZERO
                }
            }
        }
    }

    actual fun stopSpeak() {
        speechSynthesizer.stopSpeaking()
    }

    actual fun destroy() {
        // TODO(Jiangc): 释放资源
    }
}

private val SPXSpeechSynthesisResult.audioDurationMills: Duration
    get() = (audioDuration * 1000).milliseconds
