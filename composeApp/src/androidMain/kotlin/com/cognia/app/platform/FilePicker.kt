package com.cognia.app.platform

actual class PlatformFilePicker actual constructor() {
    actual suspend fun pickVideoFile(): PickedFile? {
        // Android file picker not yet implemented
        return null
    }
}
