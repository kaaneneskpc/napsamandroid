plugins {
    alias(libs.plugins.napsam.jvm.library)
    `java-test-fixtures`
}

dependencies {
    // Yalnızca testlerde: contract/ JSON dosyalarını okumak için (testFixtures, :core:domain testleri de
    // kullanır). Model kodu bağımlılıksızdır.
    testFixturesApi(libs.kotlinx.serialization.json)
}
