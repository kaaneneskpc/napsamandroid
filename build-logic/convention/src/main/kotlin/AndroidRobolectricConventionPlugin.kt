import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * JVM üzerinde Robolectric testleri (Room, ekran görüntüleri; spesifikasyon B13).
 *
 * Çalışma zamanı Android 16 (SDK 36): modül `src/test/resources/robolectric.properties` içinde
 * `sdk=36` ya da testte `@Config(sdk = [36])` kullanır. Android 17 (SDK 37) çalışma zamanında Compose
 * test altyapısının çağırdığı `InputManager.getInstance()` yok; uygulamanın targetSdk'sı değişmez.
 */
class AndroidRobolectricConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    testOptions {
                        unitTests {
                            isIncludeAndroidResources = true
                        }
                    }
                }
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
                "testImplementation"(libs.library("robolectric"))
                "testImplementation"(libs.library("androidx-test-ext-junit"))
            }
        }
    }
}
