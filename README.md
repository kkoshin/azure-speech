### azure-speech wrapper
wrapper for https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-sdk

### Install
[![Gradle Package](https://github.com/kkoshin/azure-speech/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/kkoshin/azure-speech/actions/workflows/gradle-publish.yml)

To use the package inside your application, just add the github repository to your repository list.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "Github Packages"
            url = uri("https://maven.pkg.github.com/kkoshin/azure-speech")
            credentials {
                // your github username
                username = GITHUB_USER
                // https://github.com/settings/tokens
                password = GITHUB_TOKEN
            }
        }
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
