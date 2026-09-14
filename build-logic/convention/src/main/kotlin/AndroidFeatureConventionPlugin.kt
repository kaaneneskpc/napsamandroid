import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Özellik modülü: library + Compose + Hilt.
 *
 * Bağımlılık yönü (spesifikasyon B2): feature → core:model, core:designsystem, core:domain.
 * Feature modülleri birbirine ve core:data'ya bağımlı olamaz.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("napsam.android.library")
            pluginManager.apply("napsam.android.compose")
            pluginManager.apply("napsam.hilt")

            dependencies {
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:domain"))
                "implementation"(project(":core:model"))

                "implementation"(libs.library("androidx-hilt-lifecycle-viewmodel-compose"))
                "implementation"(libs.library("androidx-lifecycle-runtime-compose"))
                "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
                "implementation"(libs.library("kotlinx-collections-immutable"))

                "testImplementation"(libs.library("kotlinx-coroutines-test"))
                "testImplementation"(libs.library("turbine"))
            }
        }
    }
}
