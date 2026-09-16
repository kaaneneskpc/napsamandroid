package com.kaaneneskpc.napsam.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Ana ekranda kaydırma akıcılığı (spesifikasyon B11: jank oranı < %1).
 *
 * `frameOverrunMs` P50/P90/P99 değerleri raporlanır; kare bütçesini aşan değerler takılmadır.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun anaEkranKaydirma() = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
        startupMode = StartupMode.WARM,
        iterations = ITERATIONS,
        setupBlock = {
            startActivityAndWait()
            waitForHome()
        },
    ) {
        flingHome()
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
