import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

// SDK ve JVM seviyeleri sürüm kataloğundan gelir (compileSdk, minSdk, targetSdk, jvmTarget).
// Built-in Kotlin'de kotlin.compilerOptions.jvmTarget, compileOptions.targetCompatibility'yi izler.

internal fun Project.configureAndroid(extension: ApplicationExtension) {
    val javaVersion = JavaVersion.toVersion(libs.versionInt("jvmTarget"))
    extension.apply {
        compileSdk {
            version = release(libs.versionInt("compileSdk"))
        }
        defaultConfig {
            minSdk = libs.versionInt("minSdk")
            targetSdk = libs.versionInt("targetSdk")
        }
        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
        lint {
            abortOnError = true
            checkReleaseBuilds = true
        }
    }
}

internal fun Project.configureAndroid(extension: LibraryExtension) {
    val javaVersion = JavaVersion.toVersion(libs.versionInt("jvmTarget"))
    extension.apply {
        compileSdk {
            version = release(libs.versionInt("compileSdk"))
        }
        defaultConfig {
            minSdk = libs.versionInt("minSdk")
        }
        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
        lint {
            abortOnError = true
            checkReleaseBuilds = true
        }
    }
}
