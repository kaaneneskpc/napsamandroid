plugins {
    alias(libs.plugins.napsam.jvm.library)
}

dependencies {
    // Yalnızca testlerde: contract/ JSON dosyalarını okumak için. Model kodu bağımlılıksızdır.
    testImplementation(libs.kotlinx.serialization.json)
}
