plugins {
    alias(libs.plugins.napsam.android.library)
    alias(libs.plugins.napsam.android.room)
    alias(libs.plugins.napsam.hilt)
    alias(libs.plugins.napsam.android.content)
    alias(libs.plugins.napsam.android.robolectric)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.kaaneneskpc.napsam.core.data"
}

dependencies {
    api(projects.core.domain)

    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)

    testImplementation(testFixtures(projects.core.model))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.turbine)
}
