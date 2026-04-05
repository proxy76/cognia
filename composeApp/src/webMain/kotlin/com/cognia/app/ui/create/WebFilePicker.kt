package com.cognia.app.ui.create

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

/**
 * Web file picker implementation using the HTML `<input type="file">` element.
 * Creates a hidden input, triggers a click, and reads the file bytes via FileReader.
 */
class WebFilePicker : PlatformFilePicker {
    override fun pickFile(accept: String, onResult: (FilePickerResult?) -> Unit) {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = accept
        input.style.display = "none"

        input.onchange = {
            val file = input.files?.get(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val arrayBuffer = reader.result as ArrayBuffer
                    val uint8 = Uint8Array(arrayBuffer)
                    val byteArray = ByteArray(uint8.length) { i -> uint8[i].toByte() }
                    onResult(FilePickerResult(file.name, byteArray))
                    document.body?.removeChild(input)
                    Unit
                }
                reader.onerror = {
                    onResult(null)
                    document.body?.removeChild(input)
                    Unit
                }
                reader.readAsArrayBuffer(file)
            } else {
                onResult(null)
                document.body?.removeChild(input)
            }
            Unit
        }

        document.body?.appendChild(input)
        input.click()
    }
}
