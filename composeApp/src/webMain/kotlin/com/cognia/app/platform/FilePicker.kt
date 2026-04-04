package com.cognia.app.platform

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
actual class PlatformFilePicker actual constructor() {
    actual suspend fun pickVideoFile(): PickedFile? = suspendCancellableCoroutine { cont ->
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "video/*"

        input.onchange = { _ ->
            val file = input.files?.get(0)
            if (file == null) {
                if (cont.isActive) cont.resume(null)
            } else {
                val reader = FileReader()
                reader.onload = {
                    val arrayBuffer = reader.result.unsafeCast<ArrayBuffer>()
                    val bytes = arrayBufferToByteArray(arrayBuffer)
                    if (cont.isActive) cont.resume(PickedFile(name = file.name, bytes = bytes))
                    Unit
                }
                reader.onerror = {
                    if (cont.isActive) cont.resume(null)
                    Unit
                }
                reader.readAsArrayBuffer(file)
            }
            Unit
        }

        input.click()
    }
}

private fun arrayBufferToByteArray(buffer: ArrayBuffer): ByteArray {
    val int8 = Int8Array(buffer)
    return int8.unsafeCast<ByteArray>()
}
