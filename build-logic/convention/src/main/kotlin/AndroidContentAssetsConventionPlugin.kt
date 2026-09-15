import com.android.build.api.variant.LibraryAndroidComponentsExtension
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * Web'den dışa aktarılan içeriği (`contract/content`) uygulama asset'i yapar (karar K3, spesifikasyon C5.4).
 *
 * Yalnızca `suggestions.json` ve `meta.json` paketlenir (asset yolu `content/`); parite dosyaları
 * APK'ya girmez. Kopya repoda tutulmaz, her derlemede `contract/`'tan üretilir. Testler `contract/`
 * klasörünü `napsam.contract.dir` sistem özelliğiyle okur.
 */
class AndroidContentAssetsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val contractDir = rootProject.layout.projectDirectory.dir("contract")
            val copyContent = tasks.register<CopyContentAssetsTask>("copyContentAssets") {
                content.from(contractDir.file("content/suggestions.json"), contractDir.file("content/meta.json"))
                outputDirectory.set(layout.buildDirectory.dir("generated/napsam/contentAssets"))
            }

            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryAndroidComponentsExtension> {
                    onVariants { variant ->
                        variant.sources.assets?.addGeneratedSourceDirectory(
                            copyContent,
                            CopyContentAssetsTask::outputDirectory,
                        )
                    }
                }
            }

            tasks.withType<Test>().configureEach {
                inputs.dir(contractDir).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("contract")
                systemProperty("napsam.contract.dir", contractDir.asFile.absolutePath)
            }
        }
    }
}

@CacheableTask
abstract class CopyContentAssetsTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val content: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystem: FileSystemOperations

    @TaskAction
    fun copy() {
        fileSystem.sync {
            from(content)
            into(outputDirectory.dir("content"))
        }
    }
}
