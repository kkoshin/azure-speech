### azure-speech wrapper
wrapper for https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-sdk

### Install
[![Gradle Package](https://github.com/kkoshin/azure-speech/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/kkoshin/azure-speech/actions/workflows/gradle-publish.yml)

To use the package inside your application, just add the github repository to your repository list.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        //...
        mavenCentral()
    }
}
```

Update dependencies in your KMM module. Current supported Targets:
- Android
- iosarm64
- iossimulatorarm64

```kotlin
// build.gradle.kts
cocoapods {
    // ...
    pod("MicrosoftCognitiveServicesSpeech-iOS") {
        version = "~> 1.25"
        packageName = "MicrosoftCognitiveServicesSpeech"
        moduleName = "MicrosoftCognitiveServicesSpeech"
    }
}

sourceSets {
    val commonMain by getting {
        dependencies {
            //...
            implementation("io.github.kkoshin:azure-speech:$version")
        }
    }
}
```
