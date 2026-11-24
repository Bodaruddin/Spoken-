package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.ViewParent
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import java.util.WeakHashMap

/**
 * Reference-counted screenshot, screen-recording, and Recents thumbnail blocker
 * for sensitive examination surfaces using Android's WindowManager.LayoutParams.FLAG_SECURE.
 *
 * Note: Cloud/streaming Android emulators render the preview via display screen-capture,
 * so setting FLAG_SECURE inside an emulator turns the preview completely black.
 * Physical Android phones receive the full FLAG_SECURE protection so students cannot capture
 * screenshots or record the exam screen.
 */
internal object ExamScreenshotGuard {
    private val lockCounts = WeakHashMap<Window, Int>()

    internal fun isEmulatorBuild(
        fingerprint: String = Build.FINGERPRINT.orEmpty(),
        model: String = Build.MODEL.orEmpty(),
        manufacturer: String = Build.MANUFACTURER.orEmpty(),
        brand: String = Build.BRAND.orEmpty(),
        device: String = Build.DEVICE.orEmpty(),
        product: String = Build.PRODUCT.orEmpty(),
        hardware: String = Build.HARDWARE.orEmpty(),
        board: String = Build.BOARD.orEmpty()
    ): Boolean {
        val fp = fingerprint.lowercase()
        val mdl = model.lowercase()
        val mfr = manufacturer.lowercase()
        val br = brand.lowercase()
        val dev = device.lowercase()
        val prod = product.lowercase()
        val hw = hardware.lowercase()
        val brd = board.lowercase()

        return fp.startsWith("generic") ||
            fp.startsWith("unknown") ||
            fp.contains("emulator") ||
            fp.contains("sdk_gphone") ||
            fp.contains("aosp_cf_") ||
            fp.contains("redroid") ||
            mdl.contains("google_sdk") ||
            mdl.contains("emulator") ||
            mdl.contains("android sdk built for") ||
            mdl.contains("sdk_gphone") ||
            mdl.contains("cuttlefish") ||
            mdl.contains("waydroid") ||
            mdl.contains("redroid") ||
            mfr.contains("genymotion") ||
            hw.contains("goldfish") ||
            hw.contains("ranchu") ||
            hw.contains("cutf") ||
            hw.contains("vsoc") ||
            hw.contains("vbox86") ||
            hw.contains("redroid") ||
            brd.contains("goldfish") ||
            brd.contains("cutf") ||
            dev.contains("emu64") ||
            dev.contains("vsoc") ||
            dev.contains("redroid") ||
            prod.contains("sdk_gphone") ||
            prod.contains("google_sdk") ||
            prod.contains("sdk_x86") ||
            prod.contains("cf_x86") ||
            prod.contains("cf_arm") ||
            prod.contains("aosp_cf_") ||
            prod.contains("redroid") ||
            prod.contains("vbox86p") ||
            prod.contains("emulator") ||
            prod.contains("simulator") ||
            (br.startsWith("generic") && dev.startsWith("generic"))
    }

    @Synchronized
    fun acquire(window: Window?, activity: Activity? = null) {
        if (window == null || isEmulatorBuild()) return
        val nextCount = (lockCounts[window] ?: 0) + 1
        lockCounts[window] = nextCount
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.setRecentsScreenshotEnabled(false)
        }
    }

    @Synchronized
    fun release(window: Window?, activity: Activity? = null) {
        if (window == null) return
        val currentCount = lockCounts[window] ?: 0
        if (currentCount == 0) return
        if (currentCount <= 1) {
            lockCounts.remove(window)
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity?.setRecentsScreenshotEnabled(true)
            }
        } else {
            lockCounts[window] = currentCount - 1
        }
    }

    @Synchronized
    fun isProtected(window: Window?): Boolean {
        if (window == null) return false
        return (window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun findDialogWindow(view: View): Window? {
    var parent: ViewParent? = view.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) {
            return parent.window
        }
        parent = parent.parent
    }
    return null
}

/**
 * Blocks screenshots, screen recordings, and app-switcher previews on the current host
 * [Activity] window while [enabled] is true and this Composable stays in composition.
 */
@Composable
internal fun ExamScreenshotProtectionEffect(enabled: Boolean = true) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = view.context.findActivity() ?: context.findActivity()
    val window = activity?.window

    DisposableEffect(enabled, window, activity) {
        if (!enabled || window == null) {
            onDispose { }
        } else {
            ExamScreenshotGuard.acquire(window, activity)
            onDispose {
                ExamScreenshotGuard.release(window, activity)
            }
        }
    }
}

/**
 * Applies [WindowManager.LayoutParams.FLAG_SECURE] to an enclosing Compose Dialog's own
 * [Window] (such as an AlertDialog inside the exam screen) so screenshots cannot capture overlays.
 */
@Composable
internal fun DialogScreenshotProtectionEffect(enabled: Boolean = true) {
    val view = LocalView.current
    val dialogWindow = findDialogWindow(view)

    DisposableEffect(enabled, dialogWindow) {
        if (!enabled || dialogWindow == null) {
            onDispose { }
        } else {
            ExamScreenshotGuard.acquire(dialogWindow)
            onDispose {
                ExamScreenshotGuard.release(dialogWindow)
            }
        }
    }
}
