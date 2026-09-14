plugins {
    alias(libs.plugins.napsam.android.library)
    alias(libs.plugins.napsam.android.compose)
    alias(libs.plugins.napsam.android.screenshot)
}

android {
    namespace = "com.kaaneneskpc.napsam.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.kotlinx.collections.immutable)
}
