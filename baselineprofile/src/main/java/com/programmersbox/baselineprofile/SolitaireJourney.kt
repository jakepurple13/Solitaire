package com.programmersbox.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

private const val TIMEOUT_MS = 5_000L

/**
 * Exercises the main game screen after startup so its hot paths land in the profile:
 * drawing cards, auto move, undo, the settings drawer, and dealing a new game.
 */
fun MacrobenchmarkScope.solitaireJourney() {
    // Wait for the game board (the draw pile shows "<n> Cards").
    device.wait(Until.hasObject(By.textContains("Cards")), TIMEOUT_MS)

    repeat(3) {
        clickOrTap(By.textContains("Cards"), xFraction = 0.93f, yFraction = 0.17f)
        device.waitForIdle()
    }

    clickOrTap(By.text("Auto Move"), xFraction = 0.69f, yFraction = 0.91f)
    device.waitForIdle()

    clickOrTap(By.text("Undo"), xFraction = 0.25f, yFraction = 0.91f)
    device.waitForIdle()

    // Settings drawer.
    clickOrTap(By.desc("Settings"), xFraction = 0.09f, yFraction = 0.08f)
    if (device.wait(Until.hasObject(By.text("Play Style")), TIMEOUT_MS)) {
        device.findObject(By.scrollable(true))?.let {
            it.setGestureMargin(device.displayWidth / 5)
            it.fling(Direction.DOWN)
        }
        device.waitForIdle()
        device.pressBack()
        device.wait(Until.gone(By.text("Play Style")), TIMEOUT_MS)
    }

    // New game dialog -> deal a fresh game.
    clickOrTap(By.desc("New Game"), xFraction = 0.22f, yFraction = 0.08f)
    if (device.wait(Until.hasObject(By.text("Yes")), TIMEOUT_MS)) {
        device.findObject(By.text("Yes")).click()
    }
    device.wait(Until.hasObject(By.textContains("Cards")), TIMEOUT_MS)
    device.waitForIdle()
}

/**
 * Clicks the element matching [selector]. If it can't be found, falls back to tapping a point
 * expressed as a fraction of the screen, measured on a 720x1560 portrait device.
 */
private fun MacrobenchmarkScope.clickOrTap(
    selector: BySelector,
    xFraction: Float,
    yFraction: Float,
) {
    val element = device.wait(Until.findObject(selector), TIMEOUT_MS)
    if (element != null) {
        element.click()
    } else {
        device.click(
            (device.displayWidth * xFraction).toInt(),
            (device.displayHeight * yFraction).toInt(),
        )
    }
}
