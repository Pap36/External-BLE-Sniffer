package com.externalblesniffer.repo

import android.util.Log
import java.util.Locale

object Utils {

    private val HEX_CHARS = "0123456789abcdef".toCharArray()
    fun ByteArray.encodeHex(prefixOx: Boolean = false): String {
        val hex = CharArray(2 * this.size)
        this.forEachIndexed { i, byte ->
            val unsigned = 0xff and byte.toInt()
            hex[2 * i] = HEX_CHARS[unsigned / 16]
            hex[2 * i + 1] = HEX_CHARS[unsigned % 16]
        }
        return hex.joinToString("").uppercase(Locale.US).let {
            when (prefixOx) {
                true -> "0x$it"
                false -> it
            }
        }
    }

    /**
     * Converts a UShort to a byte array.
     */
    fun UShort.toByteArray() = ByteArray(2) {
        (this.toInt() shr (8 - it * 8)).toByte()
    }
}