plugins {
    alias(libs.plugins.napsam.android.application)
    alias(libs.plugins.napsam.android.compose)
    alias(libs.plugins.napsam.hilt)
    alias(libs.plugins.napsam.contract.tests)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.kaaneneskpc.napsam"

    defaultConfig {
        applicationId = "com.kaaneneskpc.napsam"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            // Geçici: ölçüm (macrobenchmark) ve deneme derlemeleri kurulabilsin diye hata ayıklama
            // anahtarıyla imzalanır. Gerçek imzalama Faz 13'te (keystore repoda değil).
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

baselineProfile {
    // Profil yalnızca istenince üretilir; sürüm derlemesi cihaz gerektirmez (`:app:generateReleaseBaselineProfile`).
    automaticGenerationDuringBuild = false
    // Startup Profile'ı dex yerleşimini sıralamak için de kullan.
    dexLayoutOptimization = true
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.feature.home)
    implementation(projects.feature.privacy)
    implementation(projects.feature.saved)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    // Baseline Profile'ı ilk açılışta yükler.
    implementation(libs.androidx.profileinstaller)

    baselineProfile(projects.benchmark)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(projects.core.model))
}
