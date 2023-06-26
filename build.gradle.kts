plugins {
    kotlin("multiplatform") version "1.8.20"
    kotlin("native.cocoapods") version "1.8.20"
    id("com.android.library")
    id("maven-publish")
    signing
}

group = "io.github.kkoshin"
version = "0.1.0-rc.1"

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
    iosSimulatorArm64()
    iosArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "15.2"

        pod("MicrosoftCognitiveServicesSpeech-iOS") {
            version = "~> 1.25"
            packageName = "MicrosoftCognitiveServicesSpeech"
            // 这个moduleName一定要和 framework 的名称一致，或者说与 def 里的一致，不然，无法正确的完成 cinterop
            moduleName = "MicrosoftCognitiveServicesSpeech"
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
    publications {
        publications.withType<MavenPublication> {
            pom {
                name.set("azure-speech")
                description.set("azure speech for Kotlin Multiplatform")
                url.set("https://github.com/kkoshin/azure-speech")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/kkoshin/azure-speech/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("kkoshin")
                        name.set("Ko Shin")
                        email.set("koshin.1116@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/kkoshin/azure-speech")
                    connection.set("scm:git:https://github.com/kkoshin/azure-speech.git")
                    developerConnection.set("scm:git:https://github.com/kkoshin/azure-speech.git")
                }
            }
        }
    }
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

    signing {
        // gpg -K --keyid-format short
        val SIGNING_KEY_ID = System.getenv("SIGNING_KEY_ID")
        // gpg --armor --export-secret-key
        val SIGNING_KEY = System.getenv("SIGNING_KEY")
        val SIGNING_PASSWORD = System.getenv("SIGNING_PASSWORD")
        useInMemoryPgpKeys(SIGNING_KEY_ID, SIGNING_KEY, SIGNING_PASSWORD)
        sign(publishing.publications)
    }
}