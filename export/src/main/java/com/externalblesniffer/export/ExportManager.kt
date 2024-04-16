package com.externalblesniffer.export

import android.content.Context
import android.net.Uri
import com.externalblesniffer.repo.ScanResults
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    private val scanResults: ScanResults,
    @ApplicationContext private val context: Context,
){
    @OptIn(ExperimentalSerializationApi::class)
    fun exportResults(uri: Uri) {
        val results = scanResults.usbResults + scanResults.bleResults
        context.contentResolver.openOutputStream(uri)?.use {
            Json.encodeToStream(results, it)
            it.bufferedWriter().use { writer ->
                writer.flush()
            }
        }
    }

}