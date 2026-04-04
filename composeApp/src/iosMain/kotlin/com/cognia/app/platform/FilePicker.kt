package com.cognia.app.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume

actual class PlatformFilePicker actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun pickVideoFile(): PickedFile? = suspendCancellableCoroutine { cont ->
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeMovie)
        )

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                if (url == null) {
                    if (cont.isActive) cont.resume(null)
                    return
                }

                // Start security-scoped resource access
                val accessing = url.startAccessingSecurityScopedResource()

                val data = NSData.dataWithContentsOfURL(url)
                if (data != null && cont.isActive) {
                    val bytes = data.toByteArray()
                    val name = url.lastPathComponent ?: "video.mp4"
                    cont.resume(PickedFile(name = name, bytes = bytes))
                } else {
                    if (cont.isActive) cont.resume(null)
                }

                if (accessing) url.stopAccessingSecurityScopedResource()
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                if (cont.isActive) cont.resume(null)
            }
        }

        picker.delegate = delegate
        picker.allowsMultipleSelection = false

        val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVc?.presentViewController(picker, animated = true, completion = null)

        cont.invokeOnCancellation {
            picker.dismissViewControllerAnimated(true, null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return bytes
}
