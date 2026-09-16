// Eklentiler burada yalnızca sınıf yoluna konur; modüller convention plugin'ler üzerinden uygular.
// AGP 9 kendi başına Kotlin Gradle eklentisini 2.2.10'da tutar. kotlin-jvm'in 2.4.20 ile
// burada bildirilmesi, tüm projede Kotlin 2.4.20'nin kullanılmasını sağlar.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.spotless)
}

spotless {
    val ktlintVersion = libs.versions.ktlint.get()

    // Çıktı klasörleri taramaya hiç girmez: `targetExclude` dosyaları sonradan eler ama klasörü yine gezer; paralel
    // çalışan KSP `build/generated` klasörünü o sırada silip yeniden kurunca görev "okunamadı" hatasıyla düşüyordu.
    fun sources(pattern: String) = fileTree(rootDir) {
        include(pattern)
        exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**")
    }

    kotlin {
        target(sources("**/*.kt"))
        ktlint(ktlintVersion).customRuleSets(listOf(libs.compose.rules.ktlint.get().toString()))
    }
    kotlinGradle {
        target(sources("**/*.gradle.kts"))
        ktlint(ktlintVersion)
    }
}
