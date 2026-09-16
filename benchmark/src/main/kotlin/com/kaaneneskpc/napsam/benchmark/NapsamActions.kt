package com.kaaneneskpc.napsam.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

internal const val PACKAGE_NAME = "com.kaaneneskpc.napsam"

/** Ölçüm ve profil üretimi aynı yolları gezer; metinler uygulamanın Türkçe arayüzünden gelir. */
internal const val NAPSAM_BUTTON = "NAPSAM?"
internal const val SAVED_LABEL = "Kaydedilenler"
internal const val BACK_LINK = "Ana ekran"
internal const val PRIVACY_LINK = "Gizlilik"

private const val WAIT_MS = 10_000L
private const val GESTURE_MARGIN_RATIO = 5
private const val FLING_COUNT = 2

/** Ana ekran hazır: karar paneli çizildi (kart yerel motordan hemen sonra gelir). */
internal fun MacrobenchmarkScope.waitForHome() {
    check(device.wait(Until.hasObject(By.text(NAPSAM_BUTTON)), WAIT_MS)) { "Ana ekran açılmadı" }
}

/** Kaydırma ölçümünün gövdesi: aşağı savur, geri dön (web'deki tek uzun sayfa). */
internal fun MacrobenchmarkScope.flingHome() {
    val list = checkNotNull(device.findObject(By.scrollable(true))) { "Kaydırılabilir liste bulunamadı" }
    // Kenardan başlayan savurma sistem geri hareketine dönüşmesin.
    list.setGestureMargin(device.displayWidth / GESTURE_MARGIN_RATIO)
    repeat(FLING_COUNT) {
        list.fling(Direction.DOWN)
        device.waitForIdle()
    }
    repeat(FLING_COUNT) {
        list.fling(Direction.UP)
        device.waitForIdle()
    }
}

/** Profil üretiminde diğer iki ekran da gezilir: Kaydedilenler ve Gizlilik. */
internal fun MacrobenchmarkScope.visitOtherScreens() {
    device.findObject(By.text(SAVED_LABEL))?.click()
    device.wait(Until.hasObject(By.text(BACK_LINK)), WAIT_MS)
    device.pressBack()
    waitForHome()

    val list = device.findObject(By.scrollable(true))
    list?.setGestureMargin(device.displayWidth / GESTURE_MARGIN_RATIO)
    repeat(FLING_COUNT) {
        list?.fling(Direction.DOWN)
        device.waitForIdle()
    }
    device.findObject(By.text(PRIVACY_LINK))?.click()
    device.wait(Until.hasObject(By.text(BACK_LINK)), WAIT_MS)
    device.pressBack()
    waitForHome()
}
