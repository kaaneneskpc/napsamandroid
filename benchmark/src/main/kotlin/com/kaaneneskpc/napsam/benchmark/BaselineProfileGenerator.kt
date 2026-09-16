package com.kaaneneskpc.napsam.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline + Startup Profile üretir (spesifikasyon B11.1).
 *
 * `./gradlew :app:generateReleaseBaselineProfile` → `app/src/release/generated/baselineProfiles/`.
 * Gezilen yol kullanıcının ilk dakikasıdır: açılış, ana ekranda kaydırma, Kaydedilenler ve Gizlilik.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        waitForHome()
        flingHome()
        visitOtherScreens()
    }
}
