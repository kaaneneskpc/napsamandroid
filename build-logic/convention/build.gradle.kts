import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.kaaneneskpc.napsam.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.roborazzi.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.napsam.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.napsam.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.napsam.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.napsam.android.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidRobolectric") {
            id = libs.plugins.napsam.android.robolectric.get().pluginId
            implementationClass = "AndroidRobolectricConventionPlugin"
        }
        register("androidContentAssets") {
            id = libs.plugins.napsam.android.content.get().pluginId
            implementationClass = "AndroidContentAssetsConventionPlugin"
        }
        register("androidScreenshot") {
            id = libs.plugins.napsam.android.screenshot.get().pluginId
            implementationClass = "AndroidScreenshotTestConventionPlugin"
        }
        register("androidRoom") {
            id = libs.plugins.napsam.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("hilt") {
            id = libs.plugins.napsam.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.napsam.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
