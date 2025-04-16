// Здесь я подключаю плагины Kotlin и Android для сборки приложения
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    // Пространство имён моего приложения (используется в AndroidManifest и других файлах)
    namespace = "com.penzov.dangerdetectorvosk"

    // Указываю версию SDK, с которой буду компилировать проект (актуальная - 34)
    compileSdk = 34

    defaultConfig {
        // Уникальный идентификатор приложения (используется в Google Play, если буду публиковать)
        applicationId = "com.penzov.dangerdetectorvosk"

        // Минимальная поддерживаемая версия Android — я выбираю 34, как указал ранее
        // ⚠️ Если хочу поддержку более старых устройств — лучше снизить до 26-28
        minSdk = 26
        targetSdk = 34

        // Версия кода и отображаемая версия приложения
        versionCode = 1
        versionName = "1.0"

        // Тестовый раннер по умолчанию
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Разрешаю использовать поддержку векторных изображений
        vectorDrawables {
            useSupportLibrary = true
        }

        // Обязательно для Vosk — указываю какие ABI архитектуры нужно включить
        // Vosk работает с "armeabi-v7a" и "arm64-v8a", поэтому исключаю другие
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        release {
            // Отключаю минификацию, чтобы легче было отлаживать и видеть ошибки
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Важно! Для Vosk требуется Java 11 — обновляю версии компилятора
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Также обновляю JVM Target для Kotlin на 11
    kotlinOptions {
        jvmTarget = "11"
    }

    // Оставляю включённый Jetpack Compose, как у меня уже был
    buildFeatures {
        compose = true
    }

    // Оставляю настройки компилятора Compose без изменений
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    // Исключаю лишние лицензии, чтобы не было конфликтов при сборке
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }
}

dependencies {
    // Основные зависимости Jetpack Compose и AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Зависимости для тестирования
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 🧠 Подключаю библиотеку Vosk для оффлайн распознавания речи
    implementation("com.alphacephei:vosk-android:0.3.47")

    // 🔄 Добавляю поддержку корутин — пригодятся для работы с распознаванием речи в фоне
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
