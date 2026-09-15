plugins {
    alias(libs.plugins.napsam.android.feature)
    alias(libs.plugins.napsam.android.screenshot)
    alias(libs.plugins.napsam.contract.tests)
}

android {
    namespace = "com.kaaneneskpc.napsam.feature.home"
}

dependencies {
    implementation(projects.core.ui)

    testImplementation(testFixtures(projects.core.model))
}
