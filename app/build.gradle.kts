import java.util.Properties

plugins {
    alias(libs.plugins.napsam.android.application)
    alias(libs.plugins.napsam.android.compose)
    alias(libs.plugins.napsam.hilt)
    alias(libs.plugins.napsam.contract.tests)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

/*
 * Sürüm imzası (spesifikasyon B10.1.5, docs/RELEASE.md): upload anahtar deposu repoya girmez. Bilgiler repo
 * kökündeki git'e girmeyen `keystore.properties` dosyasından, yoksa ortam değişkenlerinden (CI) okunur.
 * Hiçbiri yoksa sürüm hata ayıklama anahtarıyla imzalanır (PR denetimi, ölçüm); o paket Play'e yüklenemez.
 * Bilgiler eksik verilirse derleme durur: yarım yapılandırma sessizce hata ayıklama anahtarına düşmez.
 */
val uploadSigning: Map<String, String>? = run {
    val file = rootProject.file("keystore.properties")
    val properties = Properties().apply { if (file.isFile) file.inputStream().use(::load) }
    val values = mapOf(
        "storeFile" to "NAPSAM_UPLOAD_STORE_FILE",
        "storePassword" to "NAPSAM_UPLOAD_STORE_PASSWORD",
        "keyAlias" to "NAPSAM_UPLOAD_KEY_ALIAS",
        "keyPassword" to "NAPSAM_UPLOAD_KEY_PASSWORD",
    ).mapValues { (key, env) ->
        properties.getProperty(key)?.takeIf { it.isNotBlank() }
            ?: providers.environmentVariable(env).orNull?.takeIf { it.isNotBlank() }
    }
    val missing = values.filterValues { it == null }.keys
    when {
        missing.isEmpty() -> values.mapValues { it.value.orEmpty() }
        missing.size == values.size -> null
        else -> throw GradleException("Sürüm imzası eksik yapılandırılmış, eksik alanlar: $missing (docs/RELEASE.md)")
    }
}

android {
    namespace = "com.kaaneneskpc.napsam"

    signingConfigs {
        if (uploadSigning != null) {
            create("upload") {
                storeFile = rootProject.file(uploadSigning.getValue("storeFile"))
                storePassword = uploadSigning.getValue("storePassword")
                keyAlias = uploadSigning.getValue("keyAlias")
                keyPassword = uploadSigning.getValue("keyPassword")
            }
        }
    }

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
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("debug")
        }
    }
}

androidComponents {
    // Baseline Profile eklentisi (1.5.0) profil toplama türevini `initWith(release)` ile kurup yalnızca eski
    // `isMinifyEnabled`'ı kapatıyor; AGP 9'un `optimization` ayarı açık kaldığı için sınıf adları karışıyor ve
    // toplanan kurallar sürüm derlemesine eşlenemiyordu. Bu geri çağrı eklentininkinden sonra çalışır.
    finalizeDsl { android ->
        android.buildTypes.findByName("nonMinifiedRelease")?.optimization {
            enable = false
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
