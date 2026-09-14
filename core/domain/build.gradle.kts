plugins {
    alias(libs.plugins.napsam.jvm.library)
}

dependencies {
    api(projects.core.model)
    // Repository arayüzlerindeki Flow için; Android bağımlılığı değildir.
    api(libs.kotlinx.coroutines.core)

    testImplementation(testFixtures(projects.core.model))
}
