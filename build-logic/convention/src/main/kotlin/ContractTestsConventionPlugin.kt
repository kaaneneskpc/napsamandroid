import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/**
 * Testler web'den dışa aktarılan `contract/` klasörünü okur (parite testleri). Yol `napsam.contract.dir`
 * sistem özelliğiyle verilir; klasör test görevinin girdisidir, içerik değişince testler yeniden çalışır.
 */
class ContractTestsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val contractDir = rootProject.layout.projectDirectory.dir("contract")
            tasks.withType<Test>().configureEach {
                inputs.dir(contractDir).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("contract")
                systemProperty("napsam.contract.dir", contractDir.asFile.absolutePath)
            }
        }
    }
}
