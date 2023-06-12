plugins {
    kotlin("multiplatform") version "1.8.21"
    id("com.android.library")
    id("maven-publish")
}

group = "io.github.kkoshin"
version = "0.1.0-LOCAL"

repositories {
    google()
    mavenCentral()
}

/**
 * iosX64 暂没支持
 */
kotlin {
    android {
        publishLibraryVariants("release" /*"debug"*/)
    }
    iosSimulatorArm64 {
        compilations.getByName("main") {
            val MicrosoftCognitiveServicesSpeech by cinterops.creating {
                compilerOpts(
                    "-F${project.projectDir}/src/iosMain/frameworks/MicrosoftCognitiveServicesSpeech.xcframework/ios-arm64_x86_64-simulator",
                    "-framework",
                    "MicrosoftCognitiveServicesSpeech"
                )
            }
        }

        binaries.all {
            // Tell the linker where the framework is located.
            linkerOpts(
                "-F${project.projectDir}/src/iosMain/frameworks",
                "-framework",
                "MicrosoftCognitiveServicesSpeech"
            )
        }
    }
    iosArm64 {
        compilations.getByName("main") {
            val MicrosoftCognitiveServicesSpeech by cinterops.creating {
                compilerOpts(
                    "-F${project.projectDir}/src/iosMain/frameworks/MicrosoftCognitiveServicesSpeech.xcframework/ios-arm64",
                    "-framework",
                    "MicrosoftCognitiveServicesSpeech"
                )
            }
        }

        binaries.all {
            // Tell the linker where the framework is located.
            linkerOpts(
                "-F${project.projectDir}/src/iosMain/frameworks",
                "-framework",
                "MicrosoftCognitiveServicesSpeech"
            )
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.microsoft.cognitiveservices.speech:client-sdk:1.28.0")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }

        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    compileSdkVersion(32)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(32)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}