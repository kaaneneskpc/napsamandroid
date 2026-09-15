plugins {
    alias(libs.plugins.napsam.android.library)
    alias(libs.plugins.napsam.android.compose)
    alias(libs.plugins.napsam.android.robolectric)
    alias(libs.plugins.napsam.contract.tests)
}

android {
    namespace = "com.kaaneneskpc.napsam.core.ui"
}

// Öneriye özgü, birden fazla ekranın paylaştığı arayüz: rozet ve kategori metinleri, öneri kartı.
dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)

    testImplementation(testFixtures(projects.core.model))
}
