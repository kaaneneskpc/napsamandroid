import com.android.build.api.dsl.LibraryExtension
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * Roborazzi + Robolectric ile JVM üzerinde Compose ekran görüntüsü testleri (spesifikasyon B13).
 *
 * Referans görüntüler `src/test/screenshots` altında repoda tutulur:
 * `recordRoborazziDebug` günceller, `verifyRoborazziDebug` farkı yakalar.
 */
class AndroidScreenshotTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")

            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    testOptions {
                        unitTests {
                            isIncludeAndroidResources = true
                        }
                    }
                }
            }

            extensions.configure<RoborazziExtension> {
                outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
            }

            // Robolectric'in Android çalışma zamanı jar'ını (~150 MB) test sırasında kendisi indirmesi
            // bağlantı kesilince testi düşürüyordu. Jar Gradle'ın depoları ve önbelleğiyle çözülür,
            // Robolectric çevrimdışı modda bu klasörden okur (CI'da da aynı yol).
            val androidAll = configurations.register("robolectricAndroidAll") {
                isCanBeConsumed = false
                isTransitive = false
            }
            val androidAllDir = layout.buildDirectory.dir("robolectric/android-all")
            val syncAndroidAll = tasks.register<Sync>("syncRobolectricAndroidAll") {
                from(androidAll)
                into(androidAllDir)
            }

            tasks.withType<Test>().configureEach {
                inputs.files(syncAndroidAll).withPropertyName("robolectricAndroidAll")
                systemProperty("robolectric.offline", "true")
                systemProperty("robolectric.dependency.dir", androidAllDir.get().asFile.absolutePath)
                // Robolectric, Android çalışma zamanını kurarken (ApplicationSharedMemory) dosya
                // tanımlayıcısına jdk.internal.access.SharedSecrets üzerinden erişir. JDK 17+ modül sistemi
                // bu paketi dışa açmadığı için IllegalAccessException fırlatılıyordu.
                jvmArgs(
                    "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                    "--add-opens=java.base/java.io=ALL-UNNAMED",
                )
            }

            dependencies {
                "robolectricAndroidAll"(libs.library("robolectric-android-all-instrumented"))
                "testImplementation"(platform(libs.library("androidx-compose-bom")))
                "testImplementation"(libs.library("androidx-compose-ui-test-junit4"))
                "testImplementation"(libs.library("androidx-test-ext-junit"))
                "testImplementation"(libs.library("robolectric"))
                "testImplementation"(libs.library("roborazzi"))
                "testImplementation"(libs.library("roborazzi-compose"))
                "testImplementation"(libs.library("roborazzi-junit-rule"))
                // Compose test kuralının kullandığı ComponentActivity manifest kaydı.
                "debugImplementation"(libs.library("androidx-compose-ui-test-manifest"))
            }
        }
    }
}
