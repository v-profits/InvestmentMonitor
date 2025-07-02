plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.investmentmonitor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.investmentmonitor"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

//    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
//    implementation ("com.squareup.moshi:moshi:1.12.0")
//    implementation ("com.squareup.moshi:moshi-kotlin:1.12.0")
//    implementation ("com.squareup.moshi:moshi-adapters:1.12.0")

    implementation (libs.androidx.viewpager2)

    implementation (libs.androidx.cardview)

    implementation (libs.androidx.recyclerview)

    implementation (libs.poi)
    implementation (libs.poi.ooxml)
    // Если нужен поддержка для обработки старых форматов Excel (.xls), добавьте:
    implementation (libs.poi.scratchpad)

    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

//    implementation ("com.caverock:androidsvg:1.4")

//    implementation ("com.github.bumptech.glide:glide:4.12.0")
//    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation (libs.jsoup) // Проверьте актуальную версию

    implementation(libs.okhttp)

    implementation(libs.glide)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
//    kapt("com.github.bumptech.glide:compiler:4.12.0")
    kapt(libs.compiler)

//    ависимости для выполнения HTTP-запросов и синтаксического анализа JSON
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    implementation (libs.androidx.appcompat.v130)
    implementation (libs.material.v140)
    implementation (libs.androidx.constraintlayout.v204)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}