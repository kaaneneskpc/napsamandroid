package com.kaaneneskpc.napsam.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Soğuk açılış ölçümü (spesifikasyon B11: soğuk açılış < 800 ms, açılış → ilk öneri < 3 sn).
 *
 * `timeToInitialDisplayMs` ilk kareyi, `timeToFullDisplayMs` ilk önerinin göründüğü anı verir
 * (ana ekran `ReportDrawnWhen` ile bildirir). İki ölçüm profilsiz ve Baseline Profile ile yapılır;
 * aradaki fark profilin katkısıdır.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun acilisProfilsiz() = measure(CompilationMode.None())

    @Test
    fun acilisBaselineProfilIle() = measure(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    private fun measure(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = ITERATIONS,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        waitForHome()
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
