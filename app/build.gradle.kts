import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile =
    rootProject.file(
        "keystore.properties"
    )

val keystoreProperties =
    Properties().apply {
        if (
            keystorePropertiesFile.exists()
        ) {
            keystorePropertiesFile
                .inputStream()
                .use {
                    load(it)
                }
        }
    }

android {
    namespace = "top.role.android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "top.role.android"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (
            keystorePropertiesFile.exists()
        ) {
            create("release") {
                storeFile =
                    rootProject.file(
                        keystoreProperties
                            .getProperty(
                                "storeFile"
                            )
                    )

                storePassword =
                    keystoreProperties
                        .getProperty(
                            "storePassword"
                        )

                keyAlias =
                    keystoreProperties
                        .getProperty(
                            "keyAlias"
                        )

                keyPassword =
                    keystoreProperties
                        .getProperty(
                            "keyPassword"
                        )
            }
        }
    }

    buildTypes {
        release {
            if (
                keystorePropertiesFile.exists()
            ) {
                signingConfig =
                    signingConfigs
                        .getByName(
                            "release"
                        )
            }

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

ksp {
    arg(
        "room.schemaLocation",
        "$projectDir/schemas",
    )
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.convex.mobile) { isTransitive = true }
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    implementation("net.java.dev.jna:jna:5.17.0@aar")

    implementation("com.tencent.mm.opensdk:wechat-sdk-android:6.8.40")

    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
}