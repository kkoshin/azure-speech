### azure-speech wrapper
wrapper for https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-sdk

### Install
[![Gradle Package](https://github.com/kkoshin/azure-speech/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/kkoshin/azure-speech/actions/workflows/gradle-publish.yml)

To use the package inside your application, just add the github repository to your repository list.

> Add the credentials section if the repository isn't public.

Current supported Targets:
- Android 
- iosarm64
- iossimulatorarm64

```kotlin
allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "Github Packages"
            url = uri("https://maven.pkg.github.com/kkoshin/azure-speech")
            credentials {
                username = GITHUB_USER
                password = GITHUB_TOKEN
            }
        }
    }
}

dependencies {
    implementation("io.github.kkoshin:azure-speech:$version")
}
```