package com.cognia.app.platform

data class PickedFile(
    val name: String,
    val bytes: ByteArray
)

expect class PlatformFilePicker() {
    suspend fun pickVideoFile(): PickedFile?
}
