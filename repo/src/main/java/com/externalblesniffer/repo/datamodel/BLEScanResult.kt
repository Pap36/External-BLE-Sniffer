package com.externalblesniffer.repo.datamodel

data class BLEScanResult(
    val rssi: Int,
    val adv_type: Int,
    val addr_type: Int,
    val addr: ByteArray,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BLEScanResult

        if (adv_type != other.adv_type) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = adv_type
        result = 31 * result + data.contentHashCode()
        return result
    }
}