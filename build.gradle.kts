import java.io.File
import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("multiplatform") version "1.8.20"
    kotlin("native.cocoapods") version "1.8.20"
    id("com.android.library")
    id("maven-publish")
}

group = "io.github.kkoshin"
version = "0.1.0-rc.1"
val GITHUB_USERID_REPOSITORY = "kkoshin/azure-speech"

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
    repositories {
//        maven {
//            name = "OSSRH"
//            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//            credentials {
//                username = System.getenv("MAVEN_USERNAME")
//                password = System.getenv("MAVEN_PASSWORD")
//            }
//        }
        maven {

            val localFile = File(rootProject.rootDir, "local.properties")
            val localProp = Properties().apply {
                if (localFile.exists()) {
                    load(FileInputStream(localFile))
                }
            }

            name = "GitHubPackages"
            /** Configure path of your package repository on Github
             ** Replace GITHUB_USERID with your/organisation Github userID
             ** and REPOSITORY with the repository name on GitHub
             */
            url = uri("https://maven.pkg.github.com/$GITHUB_USERID_REPOSITORY")
            credentials {
                username = localProp["gpr.usr"]?.toString() ?: System.getenv("USERNAME")
                password = localProp["gpr.key"]?.toString() ?: System.getenv("TOKEN")
            }
        }
    }
}