import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.firebase.appdistribution")
    id("com.github.triplet.play")
    id("com.google.gms.google-services")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 31

    ndkVersion = "21.3.6528147"

    defaultConfig {
        applicationId = "io.homeassistant.companion.android"
        minSdk = 21
        targetSdk = 31

        versionName = "LOCAL"
        versionCode = 1

        manifestPlaceholders["sentryRelease"] = "$applicationId@$versionName"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    firebaseAppDistribution {
        serviceCredentialsFile = "firebaseAppDistributionServiceCredentialsFile.json"
        releaseNotesFile = "./app/build/outputs/changelogBeta"
        groups = "continuous-deployment"
    }

//    val NESTOR_KEYSTORE_PASSWORD: String by project
//    val NESTOR_KEYSTORE_ALIAS: String by project
//    val AMAP_KEY: String by project
    val NESTOR_KEYSTORE_PASSWORD = System.getenv("NESTOR_KEYSTORE_PASSWORD")
    val NESTOR_KEYSTORE_ALIAS = System.getenv("NESTOR_KEYSTORE_ALIAS")
    val AMAP_KEY = System.getenv("NESTOR_KEYSTORE_PASSWORD")

    signingConfigs {
        create("release") {
            storeFile = file("../nestor.keystore")
            storePassword = NESTOR_KEYSTORE_PASSWORD
            keyAlias = NESTOR_KEYSTORE_ALIAS
            keyPassword = NESTOR_KEYSTORE_PASSWORD
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        named("debug").configure {
            //applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["amapkey"] = AMAP_KEY
        }
        named("release").configure {
            isDebuggable = false
            isJniDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["amapkey"] = AMAP_KEY
        }
    }
    flavorDimensions.add("version")
    productFlavors {
//        create("minimal") {
//            applicationIdSuffix = ".minimal"
//            versionNameSuffix = "-minimal"
//        }
        create("full") {
            applicationIdSuffix = ""
            versionNameSuffix = "-full"
        }
//        create("quest") {
//            applicationIdSuffix = ".quest"
//            versionNameSuffix = "-quest"
//            minSdk = 23
//        }

        // Generate a list of application ids into BuildConfig
        val values = productFlavors.joinToString {
            "\"${it.applicationId ?: defaultConfig.applicationId}${it.applicationIdSuffix}\""
        }

        defaultConfig.buildConfigField("String[]", "APPLICATION_IDS", "{$values}")
    }

    playConfigs {
//        register("minimal") {
//            enabled.set(false)
//        }
        register("full") {
            enabled.set(false)
        }
//        register("quest") {
//            enabled.set(false)
//        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    lint {
        abortOnError = false
        disable += "MissingTranslation"
    }

    kapt {
        correctErrorTypes = true
    }
}

play {
    serviceAccountCredentials.set(file("playStorePublishServiceCredentialsFile.json"))
    track.set("beta")
    resolutionStrategy.set(ResolutionStrategy.IGNORE)
    // We will depend on the wear commit.
    commit.set(true)
}

dependencies {
    implementation(project(":common"))

    implementation("com.github.Dimezis:BlurView:version-1.6.6")
    implementation("org.altbeacon:android-beacon-library:2.19.3")
    implementation("com.maltaisn:icondialog:3.3.0")
    implementation("com.maltaisn:iconpack-community-material:5.3.45")
    implementation("com.vdurmont:emoji-java:5.1.1") {
        exclude(group = "org.json", module = "json")
    }

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    implementation("com.google.dagger:hilt-android:2.41")
    kapt("com.google.dagger:hilt-android-compiler:2.41")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
    implementation("com.google.android.material:material:1.5.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.picasso:picasso:2.8")

    "fullImplementation"("com.google.android.gms:play-services-location:19.0.1")
    "fullImplementation"("com.google.firebase:firebase-core:20.0.2")
    "fullImplementation"("com.google.firebase:firebase-iid:21.1.0")
    "fullImplementation"("com.google.firebase:firebase-messaging:23.0.0")
    "fullImplementation"("io.sentry:sentry-android:5.6.3")
    "fullImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.0")
    "fullImplementation"("com.google.android.gms:play-services-wearable:17.1.0")
    "fullImplementation"("androidx.wear:wear-remote-interactions:1.0.0")
    "fullImplementation"("com.amap.api:location:5.6.2")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.webkit:webkit:1.4.0")

    implementation("com.google.android.exoplayer:exoplayer-core:2.15.1")
    implementation("com.google.android.exoplayer:exoplayer-hls:2.15.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.15.1")
    "fullImplementation"("com.google.android.exoplayer:extension-cronet:2.15.1")
    implementation("com.google.android.exoplayer:extension-cronet:2.15.1") {
        exclude(group = "com.google.android.gms", module = "play-services-cronet")
    }
    implementation("org.chromium.net:cronet-embedded:95.4638.50")

    implementation("androidx.compose.animation:animation:1.1.1")
    implementation("androidx.compose.compiler:compiler:1.1.1")
    implementation("androidx.compose.foundation:foundation:1.1.1")
    implementation("androidx.compose.material:material:1.1.1")
    implementation("androidx.compose.material:material-icons-core:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.1.1")
    implementation("androidx.compose.runtime:runtime:1.1.1")
    implementation("androidx.compose.ui:ui:1.1.1")
    implementation("androidx.compose.ui:ui-tooling:1.1.1")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.navigation:navigation-compose:2.4.0-rc01")
    implementation("com.google.android.material:compose-theme-adapter:1.1.3")
    implementation("com.google.accompanist:accompanist-appcompat-theme:0.23.1")

    implementation("com.mikepenz:iconics-core:5.3.3")
    implementation("com.mikepenz:iconics-compose:5.3.3")
    implementation("com.mikepenz:community-material-typeface:6.4.95.0-kotlin@aar")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.7.4")
    implementation("com.github.AppDevNext:ChangeLog:3.4")
}

// Disable to fix memory leak and be compatible with the configuration cache.
configure<GoogleServicesPluginConfig> {
    disableVersionCheck = true
}
