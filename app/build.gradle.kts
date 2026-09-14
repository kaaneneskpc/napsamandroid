plugins {
    alias(libs.plugins.napsam.android.application)
    alias(libs.plugins.napsam.android.compose)
    alias(libs.plugins.napsam.hilt)
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
        }
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.feature.home)
    implementation(projects.feature.saved)
    implementation(projects.feature.settings)
    implementation(projects.feature.suggestion)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
