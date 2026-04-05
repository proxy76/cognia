package com.cognia.app.ui.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Platform-specific file picker result.
 */
data class FilePickerResult(
    val fileName: String,
    val fileBytes: ByteArray,
)

/**
 * Interface for platform-specific file picker implementations.
 * On web: uses JS `<input type="file">` / FileReader API
 * On mobile: uses native picker (stub for now)
 */
interface PlatformFilePicker {
    fun pickFile(accept: String = "video/*", onResult: (FilePickerResult?) -> Unit)
}

/**
 * Default no-op file picker for platforms that don't have a native implementation yet.
 */
class NoOpFilePicker : PlatformFilePicker {
    override fun pickFile(accept: String, onResult: (FilePickerResult?) -> Unit) {
        // No-op on platforms where file picker isn't implemented yet
        onResult(null)
    }
}

/**
 * Composition local to inject the platform-specific file picker.
 * Web sets this via CompositionLocalProvider in the App composable.
 */
val LocalPlatformFilePicker = staticCompositionLocalOf<PlatformFilePicker> { NoOpFilePicker() }
