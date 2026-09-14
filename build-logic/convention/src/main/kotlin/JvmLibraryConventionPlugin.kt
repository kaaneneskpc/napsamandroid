import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Saf Kotlin modülü (:core:model, :core:domain). Android bağımlılığı yoktur.
 *
 * Explicit API mode yalnızca bu modüllerde açıktır (karar K13): başka modüllerin
 * kullandığı tipler burada tanımlanır, görünürlük bilinçli seçilmelidir.
 *
 * Testler web'den dışa aktarılan `contract/` klasörünü okur (parite testleri). Klasör
 * test görevinin girdisi olarak bildirilir; içerik değişince testler yeniden çalışır.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(libs.versionInt("jvmTarget"))
                explicitApi()
            }

            val contractDir = rootProject.layout.projectDirectory.dir("contract")
            tasks.withType<Test>().configureEach {
                inputs.dir(contractDir).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("contract")
                systemProperty("napsam.contract.dir", contractDir.asFile.absolutePath)
            }

            dependencies {
                "testImplementation"(libs.library("junit"))
            }
        }
    }
}
