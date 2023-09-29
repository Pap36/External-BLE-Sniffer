package com.externalblesniffer.ui.selected.datamodel

sealed class UIEvents {
    data class Send(val data: ByteArray): UIEvents() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Send

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}