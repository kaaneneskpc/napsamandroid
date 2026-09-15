import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Roborazzi + Robolectric ile JVM üzerinde Compose ekran görüntüsü testleri (spesifikasyon B13).
 *
 * Referans görüntüler `src/test/screenshots` altında repoda tutulur:
 * `recordRoborazziDebug` günceller, `verifyRoborazziDebug` farkı yakalar.
 */
class AndroidScreenshotTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(AndroidRobolectricConventionPlugin::class.java)
            pluginManager.apply("io.github.takahirom.roborazzi")

            extensions.configure<RoborazziExtension> {
                outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
            }

            dependencies {
                "testImplementation"(platform(libs.library("androidx-compose-bom")))
                "testImplementation"(libs.library("androidx-compose-ui-test-junit4"))
                "testImplementation"(libs.library("roborazzi"))
                "testImplementation"(libs.library("roborazzi-compose"))
                "testImplementation"(libs.library("roborazzi-junit-rule"))
                // Compose test kuralının kullandığı ComponentActivity manifest kaydı.
                "debugImplementation"(libs.library("androidx-compose-ui-test-manifest"))
            }
        }
    }
}
