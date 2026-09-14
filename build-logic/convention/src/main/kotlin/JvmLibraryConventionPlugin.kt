import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Saf Kotlin modülü (:core:model, :core:domain). Android bağımlılığı yoktur.
 *
 * Explicit API mode yalnızca bu modüllerde açıktır (karar K13): başka modüllerin
 * kullandığı tipler burada tanımlanır, görünürlük bilinçli seçilmelidir.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(libs.versionInt("jvmTarget"))
                explicitApi()
            }

            dependencies {
                "testImplementation"(libs.library("junit"))
            }
        }
    }
}
