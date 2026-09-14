plugins {
    alias(libs.plugins.napsam.android.library)
    alias(libs.plugins.napsam.android.room)
    alias(libs.plugins.napsam.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.kaaneneskpc.napsam.core.data"
}

dependencies {
    api(projects.core.domain)
}
