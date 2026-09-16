plugins {
    // AGP build-logic üzerinden sınıf yolunda: sürüm burada yazılmaz (sürüm kataloğunda tanımlı).
    id("com.android.test")
    alias(libs.plugins.baselineprofile)
}

/*
 * Macrobenchmark modülü (spesifikasyon B11): Baseline + Startup Profile üretir ve soğuk açılış /
 * kaydırma ölçümlerini yapar. Testler cihazda (ya da emülatörde) çalışır; `:app`'in `benchmarkRelease`
 * ve `nonMinifiedRelease` türevlerini hedefler. Ayrıntı ve komutlar: benchmark/README.md.
 */
android {
    namespace = "com.kaaneneskpc.napsam.benchmark"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }

    defaultConfig {
        // Macrobenchmark en az API 24 ister; uygulamanın tabanıyla aynı tutuluyor.
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    // Ölçüm APK'sı uygulamayı kendi süreci dışından sürer.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    // Profil üretimi ve ölçüm bağlı cihazda yapılır (CI için Faz 12'de yönetilen cihaz eklenecek).
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.junit)
}
